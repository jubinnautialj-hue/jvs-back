package cn.bctools.auth.service.impl;

import cn.bctools.auth.api.api.AuthDeptServiceApi;
import cn.bctools.auth.api.api.AuthJobServiceApi;
import cn.bctools.auth.api.dto.SysDeptDto;
import cn.bctools.auth.api.dto.SysJobDto;
import cn.bctools.auth.entity.*;
import cn.bctools.auth.login.dto.SyncUserDto;
import cn.bctools.auth.mapper.*;
import cn.bctools.auth.service.*;
import cn.bctools.auth.template.user.ImportUserListener;
import cn.bctools.auth.template.user.UserExcelTemplate;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.SysFrameApplyConfig;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.web.excel.ArrayListConvert;
import cn.bctools.web.excel.LocalDateTimeConvert;
import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.FIFOCache;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.URLUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 用户服务
 *
 * @author
 */
@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    UserMapper userMapper;
    UserRoleMapper userRoleMapper;
    UserTenantMapper userTenantMapper;
    UserExtensionMapper userExtensionMapper;
    UserTenantService userTenantService;
    UserExtensionService userExtensionService;
    DeptMapper deptMapper;
    JobMapper jobMapper;
    ArrayListConvert arrayListConvert;
    LocalDateTimeConvert localDateTimeConvert;
    AuthDeptServiceApi deptServiceApi;
    AuthJobServiceApi authJobServiceApi;
    TenantService tenantService;
    SysConfigsService configService;
    PasswordEncoder passwordEncoder;
    UserLevelMapper userLevelMapper;

    public static final String FILE_TYPE = ".xlsx";
    /**
     * 批量同步用户单批次数量
     */
    private static final Integer SYNC_BATCH_SIZE = 500;

    @Override
    public User saveUser(User user, UserTenant userTenant) {
        {
            String phone = user.getPhone();
            long count = this.count(Wrappers.<User>lambdaQuery().eq(User::getPhone, phone));
            if (count > 0) {
                throw new BusinessException("手机号已存在");
            }
        }
        {
            long count = this.count(Wrappers.<User>lambdaQuery().eq(User::getPhone, user.getAccountName()));
            if (count > 0) {
                throw new BusinessException("帐号已存在");
            }
        }
        if (ObjectNull.isNotNull(user.getAccountName())) {
            long vv = this.count(Wrappers.<User>lambdaQuery().eq(User::getAccountName, user.getAccountName()));
            if (vv > 0) {
                throw new BusinessException("帐号已存在");
            }
        }
        if (ObjectNull.isNotNull(userTenant.getJobId())) {
            Job job = jobMapper.selectById(userTenant.getJobId());
            userTenant.setJobName(job.getName());
        }
        if (ObjectNull.isNotNull(userTenant.getAccountLevelId())) {
            UserLevel userLevel = userLevelMapper.selectById(userTenant.getAccountLevelId());
            userTenant.setAccountLevel(userLevel.getName());
        }
        SysFrameApplyConfig config = configService.getConfig(ConfigsTypeEnum.BACKGROUND_PERSONALIZED_CONFIGURATION);
        if (ObjectNull.isNotNull(config.getDefaultPassword())) {
            // 设置初始密码
            String encode = passwordEncoder.encode(config.getDefaultPassword());
            user.setPassword(encode);
        }

        // 保存用户
        this.save(user);
        userTenant.setUserId(user.getId());
        userTenantMapper.insert(userTenant);
        return this.getById(user.getId());
    }

    @Override
    public User info(String username) {
        log.info("用户名密码登录, 用户名: {}, 选择登录租户id: {}", username, TenantContextHolder.getTenantId());
        User user = this.getOne(Wrappers.<User>lambdaQuery().eq(User::getAccountName, username));
        if (ObjectUtil.isEmpty(user)) {
            user = getOne(Wrappers.lambdaQuery(new User().setPhone(username)));
            if (ObjectUtil.isEmpty(user)) {
                throw new RuntimeException("用户名或密码错误");
            }
        }
        // 是否锁定
        if (user.getCancelFlag()) {
            // 所有用户都被禁用时
            throw new BusinessException("用户禁止登录");
        }
        return user;
    }

    @Override
    public List<User> getByRoleIds(List<String> roleIds) {
        if (ObjectUtils.isEmpty(roleIds)) {
            // 角色id集合为空
            return Collections.emptyList();
        }
        StringBuilder builder = new StringBuilder("(");
        for (String roleId : roleIds) {
            if (StringUtils.isBlank(roleId)) {
                // 角色id为null
                return Collections.emptyList();
            }
            builder.append("'" + roleId + "'").append(",");
        }
        // 删除末尾的逗号
        builder.deleteCharAt(builder.length() - 1);
        builder.append(")");
        List<User> userList = userMapper.getByRoleIds(builder.toString());
        if (userList.isEmpty()) {
            return Collections.emptyList();
        }
        return userList;
    }

    @Override
    public User phone(String phone) {
        //找到用户名和密码都匹配的
        User user = getOne(Wrappers.<User>lambdaQuery().eq(User::getPhone, phone));
        if (ObjectUtil.isEmpty(user)) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getCancelFlag()) {
            throw new BusinessException("用户禁止登录");
        }
        return user;
    }

    @Override
    public <T> boolean updateInfo(SFunction<User, T> getter, T value, String userId) {
        if (Objects.isNull(getter) || StringUtils.isBlank(userId)) {
            return false;
        }
        return this.update(Wrappers.<User>lambdaUpdate().set(getter, value).eq(User::getId, userId));
    }

    @Override
    public void downloadExcelTemplate(HttpServletResponse response) {
        // 响应数据
        String fileName = "导入用户" + FILE_TYPE;
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=".concat(URLUtil.encode(fileName, StandardCharsets.UTF_8)));
        response.setStatus(HttpStatus.OK.value());
        try {
            //导出数据
            new ExcelWriterBuilder()
                    .file(response.getOutputStream())
                    .head(UserExcelTemplate.class)
                    .registerConverter(arrayListConvert)
                    .registerConverter(localDateTimeConvert)
                    .sheet(0)
                    .doWrite(Collections.emptyList());
        } catch (Exception ex) {
            log.error("模板下载异常", ex);
        }
    }

    @Override
    public void importUserExcel(UserDto userDto, MultipartFile file) {
        try {
            // 获取所有部门
            List<SysDeptDto> deptTree = deptServiceApi.getAllTree().getData();
            // 获取所有岗位
            List<SysJobDto> sysJobDtos = authJobServiceApi.getAll().getData();
            SysFrameApplyConfig config = configService.getConfig(ConfigsTypeEnum.BACKGROUND_PERSONALIZED_CONFIGURATION);
            String defaultPassword = passwordEncoder.encode(config.getDefaultPassword());
            // 导入数据
            EasyExcel.read(file.getInputStream(), UserExcelTemplate.class, new ImportUserListener(userDto, defaultPassword, deptTree, sysJobDtos)).sheet().doRead();
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("数据导入异常", ex);
            throw new BusinessException("导入失败");
        }
    }

    @Override
    public void pull(SyncUserDto syncUser) {
        if (ObjectNull.isNull(syncUser)) {
            return;
        }
        // 排除已存在的用户
        excludeExistUser(syncUser);
        SysFrameApplyConfig config = configService.getConfig(ConfigsTypeEnum.BACKGROUND_PERSONALIZED_CONFIGURATION);

        // 同步拉取的用户id，默认是openid，保存时修改用户id
        Map<String, String> replaceUserId = syncUser.getUsers().stream().collect(Collectors.toMap(User::getId, u -> IdWorker.get32UUID()));

        // 分批同步用户数据（只新增）
        if (CollectionUtils.isNotEmpty(syncUser.getUsers())) {
            int i = 0;
            int size = syncUser.getUsers().size();
            int batchSize = SYNC_BATCH_SIZE;

            String defaultPassword = passwordEncoder.encode(config.getDefaultPassword());
            while (i < size) {
                long limit = Math.min(i + batchSize, size) - i;
                List<User> currentBatchUserList = syncUser.getUsers()
                        .stream()
                        .skip(i)
                        .limit(limit)
                        .collect(Collectors.toList());
                // 根据手机号查询用户，若用户已存在，则保留已存在的用户id
                List<String> phoneList = currentBatchUserList.stream().map(User::getPhone).collect(Collectors.toList());
                // Map<手机号，已存在的用户id>
                Map<String, String> existsPhoneUserMap;
                if (ObjectNull.isNull(phoneList)) {
                    existsPhoneUserMap = new HashMap<>();
                } else {
                    existsPhoneUserMap = list(Wrappers.<User>lambdaQuery().in(User::getPhone, phoneList).select(User::getId, User::getPhone))
                            .stream()
                            .collect(Collectors.toMap(User::getPhone, User::getId));
                }
                List<User> currentBatchUsers = currentBatchUserList.stream()
                        .filter(user -> {
                            // 如果存在手机号，则根据手机号找到已存在的用户id，替换为最终使用的用户id
                            // 且不保存为新的用户
                            if (ObjectNull.isNotNull(user.getPhone())) {
                                String id = existsPhoneUserMap.get(user.getPhone());
                                if (ObjectNull.isNotNull(id)) {
                                    replaceUserId.put(user.getId(), id);
                                    return false;
                                }
                            }
                            return true;
                        })
                        .map(user ->
                                user.setPassword(defaultPassword).setId(replaceUserId.get(user.getId())))
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(currentBatchUsers)) {
                    saveBatch(currentBatchUsers);
                }
                List<UserExtension> currentBatchUserExtensions = syncUser.getUserExtensions().stream().skip(i).limit(limit).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(currentBatchUserExtensions)) {
                    currentBatchUserExtensions.forEach(userExtension -> userExtension.setUserId(replaceUserId.get(userExtension.getUserId())));
                    userExtensionService.saveBatch(currentBatchUserExtensions);
                }
                i += batchSize;
            }
        }
        // 分批同步用户租户数据（新增或修改）
        if (CollectionUtils.isNotEmpty(syncUser.getUserTenants())) {
            int i = 0;
            int size = syncUser.getUserTenants().size();
            int batchSize = SYNC_BATCH_SIZE;
            while (i < size) {
                long limit = Math.min(i + batchSize, size) - i;
                List<UserTenant> currentBatchUserTenants = syncUser.getUserTenants().stream().skip(i).limit(limit).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(currentBatchUserTenants)) {
                    currentBatchUserTenants.forEach(userTenant -> {
                        String newUserId = replaceUserId.get(userTenant.getUserId());
                        userTenant.setUserId(StringUtils.isNotBlank(newUserId) ? newUserId : userTenant.getUserId());
                    });
                    List<String> userIds = currentBatchUserTenants.stream().map(UserTenant::getUserId).collect(Collectors.toList());
                    Optional<UserTenant> optionalUserTenant = currentBatchUserTenants.stream().filter(userTenant -> ObjectNull.isNotNull(userTenant.getTenantId())).findFirst();
                    List<UserTenant> existsUserTenantList = new ArrayList<>();
                    optionalUserTenant.ifPresent(userTenant -> existsUserTenantList.addAll(userTenantService.list(Wrappers.<UserTenant>lambdaQuery()
                            .eq(UserTenant::getTenantId, userTenant.getTenantId())
                            .in(UserTenant::getUserId, userIds))));

                    // Map<true-新增false-修改，租户信息>
                    Map<Boolean, List<UserTenant>> batchUserTenantMap = currentBatchUserTenants.stream()
                            .peek(userTenant -> {
                                Optional<UserTenant> optionalExistsUserTenant = existsUserTenantList.stream().filter(u -> userTenant.getUserId().equals(u.getUserId())).findFirst();
                                optionalExistsUserTenant.ifPresent(tenant -> userTenant.setId(tenant.getId()));
                            }).collect(Collectors.groupingBy(u -> ObjectNull.isNull(u.getId())));
                    batchUserTenantMap.forEach((k, v) -> {
                        if (k) {
                            userTenantService.saveBatch(v);
                        } else {
                            userTenantService.updateBatchById(v);
                        }
                    });
                }
                i += batchSize;
            }
        }

    }

    /**
     * 排除已存在的用户
     *
     * @param syncUser
     */
    private void excludeExistUser(SyncUserDto syncUser) {
        if (CollectionUtils.isEmpty(syncUser.getUsers())) {
            return;
        }
        String loginType = syncUser.getUserExtensions().get(0).getType();
        // 三方平台可能允许一个用户在多个部门中。所以用户信息可能会重复。对用户信息去重
        List<User> users = syncUser.getUsers().stream().filter(distinctByKey(User::getId)).collect(Collectors.toList());
        List<UserTenant> userTenants = syncUser.getUserTenants().stream().filter(distinctByKey(UserTenant::getUserId)).collect(Collectors.toList());
        List<UserExtension> userExtensions = syncUser.getUserExtensions().stream().filter(distinctByKey(UserExtension::getUserId)).collect(Collectors.toList());

        // 已存在的用户id
        List<String> deptOpenIds = userExtensions.stream().map(UserExtension::getOpenId).collect(Collectors.toList());
        int i = 0;
        int size = deptOpenIds.size();
        int batchSize = SYNC_BATCH_SIZE;
        while (i < size) {
            List<String> batchOpenIds = deptOpenIds.stream().skip(i).limit(Math.min(i + batchSize, size) - i).collect(Collectors.toList());
            List<UserExtension> existExtensions = Optional.ofNullable(userExtensionService.list(Wrappers.<UserExtension>lambdaQuery()
                            .eq(UserExtension::getType, loginType)
                            .in(UserExtension::getOpenId, batchOpenIds)
                            .select(UserExtension::getUserId, UserExtension::getOpenId)))
                    .orElse(new ArrayList<>());
            List<String> existExtensionOpenIds = existExtensions.stream().map(UserExtension::getOpenId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(existExtensionOpenIds)) {
                // 已存在的用户扩展信息不重新同步
                userExtensions.removeIf(user -> existExtensionOpenIds.contains(user.getOpenId()));
                // 已存在的用户不重新同步
                users.removeIf(user -> existExtensionOpenIds.contains(user.getId()));
                // 修改已存在的用户租户信息的部门id和部门名称
                List<String> existExtensionUserIds = existExtensions.stream().map(UserExtension::getUserId).collect(Collectors.toList());
                List<UserTenant> existTenantUsers = userTenantService.list(Wrappers.<UserTenant>lambdaQuery().in(UserTenant::getUserId, existExtensionUserIds));
                if (CollectionUtils.isNotEmpty(existTenantUsers)) {
                    existTenantUsers.forEach(existUserTenant -> {
                                existExtensions.stream().filter(e -> e.getUserId().equals(existUserTenant.getUserId())).findAny().flatMap(ue -> userTenants.stream().filter(userTenant -> userTenant.getUserId().equals(ue.getUserId()) || userTenant.getUserId().equals(ue.getOpenId())).findFirst()).ifPresent(u -> existUserTenant.setDeptId(u.getDeptId()));
                            }
                    );
                    List<String> existTenantUserIds = existTenantUsers.stream().map(UserTenant::getUserId).collect(Collectors.toList());
                    userTenants.removeIf(user -> existTenantUserIds.contains(user.getUserId()) || existExtensionOpenIds.contains(user.getUserId()));
                    userTenants.addAll(existTenantUsers);
                }
            }
            i += batchSize;
        }
        syncUser.setUsers(users);
        syncUser.setUserTenants(userTenants);
        syncUser.setUserExtensions(userExtensions);
    }

    /**
     * 去重
     *
     * @param keyExtractor
     * @param <T>
     * @return
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>(1);
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}
