package cn.bctools.auth.controller;

import cn.bctools.auth.api.dto.UserRangTypeDto;
import cn.bctools.auth.api.dto.SearchDto;
import cn.bctools.auth.api.dto.UserSelectedDto;
import cn.bctools.auth.component.SmsEmailComponent;
import cn.bctools.auth.component.UserRoleComponent;
import cn.bctools.common.entity.dto.DeptDto;
import cn.bctools.common.enums.AutoCreateUserHeadImgConfig;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.utils.jvs.JvsSystemConfig;
import cn.bctools.auth.entity.*;
import cn.bctools.auth.entity.po.TenantUserData;
import cn.bctools.auth.feign.SelectedApiImpl;
import cn.bctools.auth.mapper.UserInviteMapper;
import cn.bctools.auth.mapper.UserTenantMapper;
import cn.bctools.auth.service.*;
import cn.bctools.auth.util.AvatarUtils;
import cn.bctools.auth.vo.*;
import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.*;
import cn.bctools.common.utils.function.Get;
import cn.bctools.database.util.IdGenerator;
import cn.bctools.database.util.SqlFunctionUtil;
import cn.bctools.gateway.entity.TenantPo;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.template.OssTemplate;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.util.DateUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The type User controller.
 *
 * @author Administrator
 * @Description: 用户管理
 */
@Slf4j
@RestController
@RequestMapping("/user")
@AllArgsConstructor
@Api(tags = "用户管理")
public class UserController {

    /**
     * The constant QR_EXPIRED_MIN.
     */
    public static final int QR_EXPIRED_MIN = 30;

    /**
     * The Redis utils.
     */
    RedisUtils redisUtils;
    /**
     * The Sms component.
     */
    SmsEmailComponent smsComponent;
    /**
     * The Job service.
     */
    JobService jobService;
    /**
     * The User service.
     */
    UserService userService;
    /**
     * The User dept service.
     */
    UserDeptService userDeptService;
    /**
     * The Password encoder.
     */
    PasswordEncoder passwordEncoder;
    /**
     * The Selected api.
     */
    SelectedApiImpl selectedApi;
    /**
     * The Dept service.
     */
    DeptService deptService;
    /**
     * The Oss template.
     */
    OssTemplate ossTemplate;
    /**
     * The Tenant service.
     */
    TenantService tenantService;
    /**
     * The User tenant service.
     */
    UserTenantService userTenantService;
    /**
     * The User role component.
     */
    UserRoleComponent userRoleComponent;
    /**
     * The User role service.
     */
    UserRoleService userRoleService;
    /**
     * The User extension service.
     */
    UserExtensionService userExtensionService;
    /**
     * The User group service.
     */
    UserGroupService userGroupService;
    /**
     * The User invite mapper.
     */
    UserInviteMapper userInviteMapper;
    /**
     * The User level service.
     */
    UserLevelService userLevelService;
    /**
     * The Jvs system config.
     */
    JvsSystemConfig jvsSystemConfig;
    /**
     * The Sys config service.
     */
    SysConfigsService sysConfigService;

    /**
     * Users r.
     *
     * @param ids the ids
     * @return the r
     */
    @Log(back = false)
    @ApiOperation("所有用户的列表")
    @PostMapping("/all")
    public R<List<UserVo>> users(@RequestBody List<String> ids) {
        //传递参数为空
        if (ObjectNull.isNull(ids)) {
            return R.ok();
        }
        // 查询用户租户信息
        Map<String, UserTenant> userTenantMap = userTenantService.list(new LambdaQueryWrapper<UserTenant>().in(UserTenant::getUserId, ids))
                .stream()
                .collect(Collectors.toMap(UserTenant::getUserId, Function.identity()));
        if (ObjectUtil.isEmpty(userTenantMap)) {
            return R.ok(Collections.emptyList());
        }
        // 查询用户基本信息
        Map<String, User> userMap = userService.list(new LambdaQueryWrapper<User>().in(User::getId, ids)).stream().collect(Collectors.toMap(User::getId, Function.identity()));
        // User与UserTenant都有cancel_flag字段, 需要以UserTenant的为准, 需要注意这里copy的顺序
        List<UserVo> list = ids.stream().map(userTenantMap::get)
                .filter(ObjectNull::isNotNull)
                .map(userTenant -> BeanCopyUtil.copy(UserVo.class, userMap.get(userTenant.getUserId()), userTenant).setId(userTenant.getUserId()).setUserId(userTenant.getUserId()))
                .collect(Collectors.toList());
        return R.ok(list);
    }

    /**
     * Users r.
     *
     * @param page       the page
     * @param userTenant the user tenant
     * @return the r
     */
    @Log(back = false)
    @ApiOperation(value = "用户列表", notes = "组织机构管理，当前选择部门，下面的人员有哪些，显示到右侧")
    @GetMapping("/page")
    public R<Page<UserVo>> users(PageDTO<TenantUserData> page, UserTenantPageVo userTenant) {
        Page<UserVo> userVoPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal(), page.isSearchCount());

        List<String> allChildId = null;
        List<String> userids = null;
        if (ObjectNull.isNotNull(userTenant.getDeptId())) {
            List<String> list = deptService.getAllChildId(userTenant.getDeptId());
            userids = userDeptService.list(new LambdaQueryWrapper<UserDept>().in(ObjectNull.isNotNull(list), UserDept::getDeptId, list)).stream().map(e -> e.getUserId()).collect(Collectors.toList());
        }
        QueryWrapper<Object> queryWrapper = Wrappers.query();
        if (ObjectNull.isNotNull(allChildId)) {
            for (int i = 0; i < allChildId.size(); i++) {
                String trim = allChildId.get(i).trim();
                queryWrapper.or(objectQueryWrapper -> {
                    objectQueryWrapper.apply(SqlFunctionUtil.jsonContains(UserTenantMapper.SYS_USER_TENANT_ALIAS + ".dept_id", trim, "$"))
                            .isNotNull(UserTenantMapper.SYS_USER_TENANT_ALIAS + ".dept_id")
                            .eq(ObjectNull.isNotNull(userTenant.getSex()), UserTenantMapper.SYS_USER_ALIAS + ".sex", userTenant.getSex())
                            .eq(UserTenantMapper.SYS_USER_TENANT_ALIAS + ".cancel_flag", userTenant.getCancelFlag())
                            .and(ObjectNull.isNotNull(userTenant.getKeywords()), wrapper -> wrapper
                                    .like(UserTenantMapper.SYS_USER_TENANT_ALIAS + ".real_name", userTenant.getKeywords())
                                    .or().like(UserTenantMapper.SYS_USER_TENANT_ALIAS + ".phone", userTenant.getKeywords())
                                    .or().like(UserTenantMapper.SYS_USER_ALIAS + ".account_name", userTenant.getKeywords())
                            );
                });
            }
        } else {
            queryWrapper.eq(ObjectNull.isNotNull(userTenant.getSex()), UserTenantMapper.SYS_USER_ALIAS + ".sex", userTenant.getSex())
                    .in(ObjectNull.isNotNull(userids), UserTenantMapper.SYS_USER_TENANT_ALIAS + ".user_id", userids)
                    .eq(UserTenantMapper.SYS_USER_TENANT_ALIAS + ".cancel_flag", userTenant.getCancelFlag())
                    .and(ObjectNull.isNotNull(userTenant.getKeywords()), wrapper -> wrapper
                            .like(UserTenantMapper.SYS_USER_TENANT_ALIAS + ".real_name", userTenant.getKeywords())
                            .or().like(UserTenantMapper.SYS_USER_TENANT_ALIAS + ".phone", userTenant.getKeywords())
                            .or().like(UserTenantMapper.SYS_USER_ALIAS + ".account_name", userTenant.getKeywords())
                    );
        }
        userTenantService.tenantUsers(page, queryWrapper);
        if (ObjectUtil.isEmpty(page.getRecords())) {
            return R.ok(userVoPage);
        }
        Map<String, UserVo> map = new LinkedHashMap<>(page.getRecords().size());
        page.getRecords().forEach(e -> {
            UserVo userVo = BeanCopyUtil.copy(e, UserVo.class);
            userVo.setId(e.getUserId());
            userVo.setLevel(e.getAccountLevel());
            userVo.setLevelId(e.getAccountLevelId());
            map.put(userVo.getId(), userVo);
        });
        Map<String, List<Role>> roleByUserId = userRoleComponent.getUserRoleIds(map.keySet());
        List list = new ArrayList();
        for (String s : map.keySet()) {
            UserVo userVo = map.get(s).setRoleNames(roleByUserId.get(s).stream().map(Role::getRoleName).distinct().collect(Collectors.toList()));
            if (ObjectNull.isNotNull(userVo.getDeptId())) {
                userVo.setDeptName(deptService.listByIds(userVo.getDeptId()).stream().map(Dept::getName).collect(Collectors.toList()));
            }
            list.add(userVo);
        }
        userVoPage.setRecords(list);
        userVoPage.setTotal(page.getTotal());
        return R.ok(userVoPage);
    }

    /**
     * 用户搜索
     *
     * @param key    真实姓名或手机号(模糊搜索)
     * @param deptId 部门id
     * @return 用户信息集合 r
     */
    @Log
    @ApiOperation(value = "用户搜索", notes = "用户搜索操作,目前只提供手机号或姓名搜索")
    @GetMapping("/user/search")
    public R<List<User>> search(@RequestParam(required = false) String key, @RequestParam(required = false) String deptId) {
        if (StringUtils.isNotBlank(deptId)) {
            List<String> userIds = userTenantService.list(Wrappers.<UserTenant>lambdaQuery()
                            .select(UserTenant::getUserId)
                            .eq(UserTenant::getDeptId, deptId))
                    .stream().map(UserTenant::getUserId).collect(Collectors.toList());
            if (ObjectUtil.isNotEmpty(userIds)) {
                return R.ok(userService.listByIds(userIds));
            }
        } else {
            List<User> list = userService.list(Wrappers.<User>lambdaQuery()
                    .select(User::getId, User::getRealName, User::getPhone, User::getHeadImg)
                    .orderByDesc(User::getCreateTime)
                    .like(ObjectUtil.isNotEmpty(key), User::getRealName, key)
                    .or()
                    .like(ObjectUtil.isNotEmpty(key), User::getPhone, key));
            return R.ok(list);
        }
        return R.ok(Collections.emptyList());
    }

    /**
     * Search r.
     *
     * @param searchDto the search dto
     * @return the r
     */
    @Log
    @ApiOperation(value = "用户选择", notes = "多纬度选择对象")
    @PostMapping("/user/selected")
    public R<UserSelectedDto> search(@RequestBody SearchDto searchDto) {
        return selectedApi.search(searchDto);
    }

    /**
     * 通过名称快速搜索
     *
     * @param name 名称简称
     * @return r r
     */
    @ApiOperation(value = "用户姓名搜索", notes = "支持拼音简称搜索")
    @GetMapping("/user/short/{name}")
    public R searchShort(@PathVariable String name) {
        Map<String, Map<String, Object>> list = userTenantService.list(new LambdaQueryWrapper<UserTenant>()
                .select(UserTenant::getUserId, UserTenant::getRealName)
                .like(UserTenant::getRealName, name)
                .last(" limit 6")).stream().collect(Collectors.toMap(UserTenant::getUserId, BeanCopyUtil::beanToMap));
        String headImg = Get.name(User::getHeadImg);
        userService.list(new LambdaQueryWrapper<User>().select(User::getHeadImg, User::getId).in(User::getId, list.keySet())).forEach(e -> {
            list.get(e.getId()).put(headImg, e.getHeadImg());
        });
        return R.ok(list.values());
    }

    /**
     * Delete r.
     *
     * @param id the id
     * @return the r
     */
    @Log
    @ApiOperation(value = "彻底删除用户", notes = "把用户从当前租户下删除")
    @DeleteMapping("/user/{id}")
    public R delete(@PathVariable String id) {
        //判断是否是自己
        if (UserCurrentUtils.getUserId().equals(id)) {
            return R.failed("不能删除自己");
        }
        userTenantService.remove(Wrappers.query(new UserTenant().setUserId(id)));
        TenantContextHolder.clear();
        // 已无租户信息，则删除该用相关信息
        if (userTenantService.count(Wrappers.<UserTenant>lambdaQuery().eq(UserTenant::getUserId, id)) == 0) {
            userService.removeById(id);
            userExtensionService.remove(Wrappers.<UserExtension>lambdaQuery().eq(UserExtension::getUserId, id));
            userGroupService.clearUser(id);
            userInviteMapper.delete(Wrappers.<UserInvite>lambdaQuery().eq(UserInvite::getUserId, id));
            userRoleComponent.clearUser(id);
        }
        return R.ok();
    }

    /**
     * Search page rang r.
     *
     * @param page the page
     * @param vo   the vo
     * @return the r
     */
    @Log
    @ApiOperation(value = "用户选择", notes = "多纬度选择对象")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "key", value = "多纬度查询条件"),
            @ApiImplicitParam(name = "all", value = "false-只查询未删除的用户；true-查询所有用户(包括已删除的)"),
            @ApiImplicitParam(name = "deptId", value = "部门id"),
            @ApiImplicitParam(name = "jobId", value = "岗位id"),
    })
    @PostMapping("/user/selected/page")
    public R searchPageRang(Page<UserTenant> page,
                            @RequestBody UserSelectedVo vo) {
        page.setSize(vo.getSize());
        page.setCurrent(vo.getCurrent());
        //如果有指定范围，则直接使用指定范围的查询结果
        String deptId = vo.getDeptId();
        UserRangTypeDto rangType = vo.getRangType();
        List<String> rangIds = vo.getRangIds();
        String jobId = vo.getJobId();
        String key = vo.getKey();
        Boolean all = vo.getAll();
        List<String> userIds = null;
        List<String> depts = null;
        List<String> jobs = null;
        if (ObjectNull.isNotNull(rangType)) {
            switch (rangType) {
                case user:
                    userIds = rangIds;
                    break;
                case job:
                    jobs = rangIds;
                    break;
                case dept:
                    //获取所有的部门
                    depts = rangIds.stream().flatMap(e -> deptService.getAllChildId(e).stream()).collect(Collectors.toList());
                    break;
                case role:
                    //根据这个角色查询用户
                    userIds = userRoleService.list(new LambdaQueryWrapper<UserRole>().in(UserRole::getRoleId, rangIds)).stream()
                            .map(UserRole::getUserId).collect(Collectors.toList());
                    break;
                case currentDept:
                    depts = deptService.getAllChildId(UserCurrentUtils.getDept().stream().map(DeptDto::getDeptId).filter(ObjectNull::isNotNull).collect(Collectors.toList()));
            }
        }
        if (ObjectNull.isNotNull(deptId)) {
            depts = deptService.getAllChildId(deptId);
        }

        LambdaQueryWrapper<UserTenant> in = new LambdaQueryWrapper<UserTenant>()
                .select(UserTenant::getId, UserTenant::getUserId, UserTenant::getPhone, UserTenant::getRealName)
//                .like(ObjectNull.isNotNull(deptId), UserTenant::getDeptId, deptId)
                .eq(ObjectNull.isNotNull(jobId), UserTenant::getJobId, jobId)
                .in(ObjectNull.isNotNull(userIds), UserTenant::getUserId, userIds);
        if (ObjectNull.isNotNull(depts)) {
            in.isNotNull(UserTenant::getDeptId);
            List<String> userids = userDeptService.list(new LambdaQueryWrapper<UserDept>().in(UserDept::getDeptId, depts)).stream().map(UserDept::getUserId).collect(Collectors.toList());
            in.in(ObjectNull.isNotNull(userids), UserTenant::getUserId, userids);
        }
        page = userTenantService.page(page, in
                .in(ObjectNull.isNotNull(jobs), UserTenant::getJobId, jobs)
                // 默认查询未删除的用户
                .eq(ObjectNull.isNull(all) || Boolean.FALSE.equals(all), UserTenant::getCancelFlag, false)
                .and(ObjectNull.isNotNull(key), e -> e.like(ObjectNull.isNotNull(key), UserTenant::getRealName, key).or().like(ObjectNull.isNotNull(key), UserTenant::getPhone, key))
                .orderByDesc(UserTenant::getCreateTime)
        );
        if (ObjectNull.isNull(page.getRecords())) {
            return R.ok(page);
        }
        Map<String, UserTenant> ids = page.getRecords().stream().collect(Collectors.toMap(UserTenant::getUserId, Function.identity()));
        List<User> users = userService.list(new LambdaQueryWrapper<User>().select(User::getId, User::getHeadImg, User::getRealName).in(User::getId, ids.keySet()));
        Page<User> userPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        userPage.setRecords(users);
        return R.ok(userPage);
    }

    /**
     * Search page r.
     *
     * @param page   the page
     * @param key    the key
     * @param all    the all
     * @param deptId the dept id
     * @param jobId  the job id
     * @return the r
     */
    @GetMapping("/user/selected/page")
    public R searchPage(Page<UserTenant> page, @RequestParam(value = "key", required = false) String key, @RequestParam(value = "all", required = false) Boolean all, String deptId, String jobId) {
        page = userTenantService.page(page, new LambdaQueryWrapper<UserTenant>()
                .select(UserTenant::getId, UserTenant::getUserId, UserTenant::getPhone, UserTenant::getRealName)
                .eq(ObjectNull.isNotNull(deptId), UserTenant::getDeptId, deptId)
                .eq(ObjectNull.isNotNull(jobId), UserTenant::getJobId, jobId)
                // 默认查询未删除的用户
                .eq(ObjectNull.isNull(all) || Boolean.FALSE.equals(all), UserTenant::getCancelFlag, false)
                .and(ObjectNull.isNotNull(key), e -> e.like(ObjectNull.isNotNull(key), UserTenant::getRealName, key).or().like(ObjectNull.isNotNull(key), UserTenant::getPhone, key))
                .orderByDesc(UserTenant::getCreateTime)
        );
        if (ObjectNull.isNull(page.getRecords())) {
            return R.ok(page);
        }
        Map<String, UserTenant> ids = page.getRecords().stream().collect(Collectors.toMap(UserTenant::getUserId, Function.identity()));
        List<User> users = userService.listByIds(ids.keySet());
        Page<User> userPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        userPage.setRecords(users);
        return R.ok(userPage);
    }

    /**
     * User r.
     *
     * @param userId the user id
     * @return the r
     * @throws JsonProcessingException the json processing exception
     */
    @Log
    @ApiOperation(value = "获取用户详情", notes = "组织机构管理，点击用户详情操作")
    @GetMapping("/user/{userId}")
    public R<UserVo> user(@PathVariable String userId) throws JsonProcessingException {
        User user = userService.getById(userId);
        if (Objects.isNull(user)) {
            return R.ok();
        }
        UserTenant userTenant = userTenantService.getOne(Wrappers.<UserTenant>lambdaQuery().eq(UserTenant::getUserId, userId));
        if (Objects.isNull(userTenant)) {
            return R.failed("用户未加入当前组织");
        }
        ObjectMapper o = new ObjectMapper();
        Object v = o.readTree(JSONObject.toJSONString(user));
        ((ObjectNode) (v)).setAll((ObjectNode) o.readTree(JSONObject.toJSONString(userTenant)));
        String result = o.writerWithDefaultPrettyPrinter().writeValueAsString(v);
        UserVo userVo = com.alibaba.fastjson2.JSONObject.parseObject(result, UserVo.class);
        return R.ok(userVo);
    }

    /**
     * Disabled r.
     *
     * @param userId the user id
     * @return the r
     */
    @Log
    @ApiOperation(value = "禁用用户", notes = "组织机构管理，可对用户进行移除，为不影响功能使用，只做注销不做删除,目前不支持注销后找回")
    @DeleteMapping("/users/disabled/{userId}")
    public R<Boolean> disabled(@PathVariable String userId) {
        //判断是否是自己
        if (UserCurrentUtils.getUserId().equals(userId)) {
            return R.failed("不能删除自己");
        }
        userTenantService.update(Wrappers.<UserTenant>lambdaUpdate().set(UserTenant::getCancelFlag, true).eq(UserTenant::getUserId, userId));
        return R.ok(true, "禁用成功");
    }

    /**
     * Enable r.
     *
     * @param userId the user id
     * @return the r
     */
    @Log
    @ApiOperation(value = "启用用户", notes = "组织机构管理，可对用户进行移除，为不影响功能使用，只做注销不做删除,目前不支持注销后找回")
    @DeleteMapping("/users/enable/{userId}")
    public R<Boolean> enable(@PathVariable String userId) {
        userTenantService.update(Wrappers.<UserTenant>lambdaUpdate().set(UserTenant::getCancelFlag, false).eq(UserTenant::getUserId, userId));
        return R.ok(true);
    }

    /**
     * Pass word r.
     *
     * @param resetPasswordDto the reset password dto
     * @param userId           the user id
     * @return the r
     */
    @Log
    @ApiOperation(value = "超级管理员修改用户密码", notes = "只有超级管理员才能修改用户密码")
    @PostMapping("/users/update/password/{userId}")
    public R passWord(@RequestBody ResetPasswordDto resetPasswordDto, @PathVariable String userId) {
        //只有平台超级管理员才能修改
        if (!resetPasswordDto.getPassword().equals(resetPasswordDto.getRePassword())) {
            return R.failed("密码不匹配");
        }
        //添加密码难度
        PasswordUtil.checkPassword(resetPasswordDto.getPassword());
        // appId为与前端对应的应用id
        String decodedPassword = PasswordUtil.decodedPassword(resetPasswordDto.getRePassword(), SpringContextUtil.getKey());
        String encodedPassword = passwordEncoder.encode(decodedPassword);
        User byId = userService.getById(userId);
        if (ObjectNull.isNull(byId)) {
            return R.failed("用户不存在");
        } else {
            // 修改密码
            userService.updateInfo(User::getPassword, encodedPassword, userId);
            return R.ok().setMsg("密码修改成功");
        }
    }

    /**
     * Save r.
     *
     * @param vo the vo
     * @return the r
     */
    @Log
    @ApiOperation(value = "新增用户", notes = "后台新增的用户，默认类型为1，另外在租户中间表中进行添加一行处理")
    @PostMapping("/save")
    @Transactional(rollbackFor = Exception.class)
    public R<UserVo> save(@RequestBody UserVo vo) {
        if (StringUtils.isBlank(vo.getAccountName())) {
            //手机号前面加随机号
            vo.setAccountName(IdGenerator.getIdStr(36));
        }
        User user = BeanCopyUtil.copy(vo, User.class);
        UserTenant userTenant = BeanCopyUtil.copy(vo, UserTenant.class);
        //用户是否使用默认头像问题
        AutoCreateUserHeadImgConfig autoCreateUserHeadImgConfig = sysConfigService.getConfig(ConfigsTypeEnum.AUTO_CREATE_USER_HEAD_IMG);
        if (autoCreateUserHeadImgConfig.getEnable()) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            AvatarUtils.generateImg(vo.getRealName(), output);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(output.toByteArray());
            BaseFile baseFile = ossTemplate.putFile("jvs-public", vo.getRealName() + ".jpg", byteArrayInputStream, "headImg");
            String link = ossTemplate.fileJvsPublicLink(baseFile.getFileName());
            user.setHeadImg(link);
        }
        userTenant.setAccountLevelId(vo.getLevelId());
        user = userService.saveUser(user, userTenant);
        //将部门保存到一个新的表中
        if (ObjectNull.isNotNull(userTenant.getDeptId())) {
            String id = user.getId();
            String tenantId = UserCurrentUtils.getCurrentUser().getTenantId();
            List<UserDept> collect = userTenant.getDeptId().stream().map(e -> {
                return new UserDept().setUserId(id).setDeptId(e).setTenantId(tenantId);
            }).collect(Collectors.toList());
            userDeptService.saveBatch(collect);
        }
        userRoleComponent.grandDefaultSysRole(user.getId());
        UserVo userVo = BeanCopyUtil.copy(UserVo.class, user, userTenant);
        return R.ok(userVo);
    }

    /**
     * Invite r.
     *
     * @return the r
     */
    @Log
    @ApiOperation(value = "生成邀请码", notes = "添加用户的两种方式，通过邀请码进入和通过直接添加,邀请生成规则为租户域名,加租户时间加密串")
    @GetMapping("/get/invite")
    public R<InviteVo> invite() {
        //默认设置为10分钟有效
        String tenantId = UserCurrentUtils.getCurrentUser().getTenantId();
        TenantPo tenantPo = tenantService.getById(tenantId);
        //将邀请码放到缓存中，         过期时间30分钟
        String code = IdGenerator.getIdStr(36).toUpperCase();
        Date date = new Date(System.currentTimeMillis() + 30 * 60 * 1000);
        String format = DateUtils.format(date);
        String invite = SysConstant.redisKey("invite", code);
        InviteVo inviteVo = new InviteVo().setCode(code).setStatus(false).setContent(UserCurrentUtils.getRealName() + " 邀请您参加组织\n" +
                                                                                     "组织名称：【" + tenantPo.getName() + "】\n" +
                                                                                     "有效期：" + format + "\n" +
                                                                                     "\n" +
                                                                                     "邀请码：\n" +
                                                                                     code + "\n");
        inviteVo.setTenantId(tenantId);
        redisUtils.setExpire(invite, inviteVo, QR_EXPIRED_MIN, TimeUnit.MINUTES);
        return R.ok(inviteVo);
    }

    /**
     * Invite status r.
     *
     * @param status the status
     * @param code   the code
     * @return the r
     */
    @Log
    @ApiOperation(value = "设置邀请码是否需要审核默认不需要")
    @GetMapping("/get/invite/{status}/{code}")
    public R<InviteVo> inviteStatus(@PathVariable Boolean status, @PathVariable String code) {
        InviteVo o = (InviteVo) redisUtils.get(SysConstant.redisKey("invite", code));
        o.setStatus(status);
        String invite = SysConstant.redisKey("invite", code);
        redisUtils.setExpire(invite, o, QR_EXPIRED_MIN, TimeUnit.MINUTES);
        return R.ok();
    }

    /**
     * Update r.
     *
     * @param vo the vo
     * @return the r
     */
    @Log
    @ApiOperation(value = "修改用户", notes = "后台修改用户")
    @PutMapping("/update")
    @Transactional(rollbackFor = Exception.class)
    public R update(@RequestBody UserVo vo) {
        if (ObjectNull.isNull(vo.getRealName())) {
            return R.failed("用户名不能修改为空");
        }
        String tenantId = TenantContextHolder.getTenantId();
        User user = BeanCopyUtil.copy(vo, User.class);
        if (ObjectNull.isNotNull(vo.getUserId())) {
            user.setId(vo.getUserId());
        }
        // 手机号校验
        String phone = user.getPhone();
        if (StringUtils.isNotBlank(phone)) {
            long count = userService.count(Wrappers.<User>lambdaQuery().eq(User::getPhone, phone).ne(User::getId, user.getId()));
            if (count >= 1) {
                return R.failed("手机号已被使用");
            }
        }
        String accountName = user.getAccountName();
        if (StringUtils.isNotBlank(accountName)) {
            long count = userService.count(Wrappers.<User>lambdaQuery().eq(User::getAccountName, accountName).ne(User::getId, user.getId()));
            if (count >= 1) {
                return R.failed("帐号已经存在");
            }
        }
        UserTenant userTenant = BeanCopyUtil.copy(vo, UserTenant.class);

        String jobId = userTenant.getJobId();
        if (ObjectUtil.isNotEmpty(jobId)) {
            Job job = jobService.getById(jobId);
            if (Objects.isNull(job)) {
                log.error("该岗位不存在, 岗位id: {}", jobId);
                return R.failed("该岗位不存在");
            }
            userTenant.setJobName(job.getName());
        } else {
            userTenant.setJobName(null);
        }

        String levelId = vo.getLevelId();
        if (ObjectUtil.isNotEmpty(levelId)) {
            UserLevel userLevel = userLevelService.getById(levelId);
            if (Objects.isNull(userLevel)) {
                log.error("该用户等级不存在, 用户等级id: {}", levelId);
                return R.failed("该用户等级不存在");
            }
            userTenant.setAccountLevel(userLevel.getName());
            userTenant.setAccountLevelId(levelId);
        }
        AutoCreateUserHeadImgConfig autoCreateUserHeadImgConfig = sysConfigService.getConfig(ConfigsTypeEnum.AUTO_CREATE_USER_HEAD_IMG);
        //用户是否使用默认头像问题
        if (autoCreateUserHeadImgConfig.getEnable() && "/jvs-ui-public/img/headImg.png".equals(user.getHeadImg())) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            AvatarUtils.generateImg(vo.getRealName(), output);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(output.toByteArray());
            BaseFile baseFile = ossTemplate.putFile("jvs-public", vo.getRealName() + ".png", byteArrayInputStream, "headImg");
            String link = ossTemplate.fileJvsPublicLink(baseFile.getFileName());
            user.setHeadImg(link);
        }
        UserTenant updateUserTenant = userTenantService.getOne(Wrappers.query(new UserTenant().setUserId(user.getId()).setTenantId(tenantId)));
        userTenant.setId(updateUserTenant.getId());
        userTenantService.updateById(userTenant);
        //将部门保存到一个新的表中
        if (ObjectNull.isNotNull(userTenant.getDeptId())) {
            String id = user.getId();
            //删除历史的用户部门数据
            userDeptService.remove(Wrappers.query(new UserDept().setUserId(id)));
            List<UserDept> collect = userTenant.getDeptId().stream().map(e -> {
                return new UserDept().setUserId(id).setDeptId(e).setTenantId(tenantId);
            }).collect(Collectors.toList());
            userDeptService.saveBatch(collect);
        }
        userService.updateById(user);
        return R.ok();
    }


    /**
     * Download excel template.
     *
     * @param response the response
     */
    @ApiOperation("下载模板")
    @GetMapping("/template/excel/download")
    public void downloadExcelTemplate(HttpServletResponse response) {
        userService.downloadExcelTemplate(response);
    }

    /**
     * Import user r.
     *
     * @param file the file
     * @return the r
     */
    @ApiOperation("导入")
    @PostMapping("/import")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> importUser(@RequestPart("file") MultipartFile file) {
        UserDto currentUser = UserCurrentUtils.getCurrentUser();
        userService.importUserExcel(currentUser, file);
        return R.ok(true, "导入成功");
    }


}
