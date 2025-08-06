package cn.bctools.auth.service;

import cn.bctools.auth.component.OtherAuthComponent;
import cn.bctools.auth.component.SmsEmailComponent;
import cn.bctools.auth.component.UserInfoComponent;
import cn.bctools.auth.component.UserRoleComponent;
import cn.bctools.auth.config.TransitionConfig;
import cn.bctools.auth.entity.*;
import cn.bctools.auth.login.enums.LoginTypeEnum;
import cn.bctools.common.entity.dto.DeptDto;
import cn.bctools.common.entity.dto.TenantsDto;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.entity.dto.UserInfoDto;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.SysApplyConfig;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.gateway.entity.SysConfigs;
import cn.bctools.gateway.entity.TenantPo;
import cn.bctools.log.utils.IpUtils;
import cn.bctools.oauth2.dto.CustomUser;
import cn.bctools.redis.utils.RedisUtils;
import cn.bctools.web.utils.IpUtil;
import cn.bctools.web.utils.WebUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 此操作直接查库
 * 根据不同类型获取用户并组装用户的详细信息，包含权限信息。和租户信息
 *
 * @author guojing
 */
@Slf4j
@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    SmsEmailComponent smsComponent;
    UserService userService;
    UserInfoComponent userInfoComponent;
    TenantService tenantService;
    DeptService deptService;
    UserExtensionService userExtensionService;
    UserTenantService userTenantService;
    ApplyService applyService;
    OtherAuthComponent justAuthComponent;
    RoleService roleService;
    UserRoleComponent userRoleComponent;
    RedisUtils redisUtils;
    UserLevelService userLevelService;
    SysConfigsService sysConfigService;
    TransitionConfig transitionConfig;


    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        try {
            User info = userService.info(username);
            return getUserDetails(info, "帐号密码");
        } catch (Exception e) {
            log.error("用户登录失败", e);
            throw new AccountExpiredException(e.getMessage());
        }
    }


    /**
     * 标识使用@分割  如phone@18888888888_0000
     * wx@3049
     *
     * @param appId
     * @param identification
     * @return
     */
    public UserDetails loadUserByOtherAuth(String identification, String appId) {
        try {
            String[] split = identification.split(StringPool.AT);
            String loginType = split[0];
            return getUserDetails(loginType, split[1], appId);
        } catch (Exception e) {
            log.error("用户登录失败，{},{}", identification, appId, e);
            throw new AccountExpiredException(e.getMessage());
        }
    }

    /**
     * 独立出来，避免数据不存在情况无法回滚
     *
     * @param loginType
     * @param s
     * @param appId
     * @return
     */
    UserDetails getUserDetails(String loginType, String s, String appId) {
        User user = justAuthComponent.getUser(loginType, s, appId);
        return getUserDetails(user, loginType);
    }

    /**
     * 根据当前用户选择登录的用户
     *
     * @param info      用户对象
     * @param loginType 用户登陆方式
     * @return 返回oauth 用户对象
     */
    public UserDetails getUserDetails(User info, String loginType) {
        HttpServletRequest request = WebUtils.getRequest();
        UserDto userDto = BeanCopyUtil.copy(info, UserDto.class);
        log.debug("登录的用户信息：{}", userDto);
        //设置用户登陆ip地址
        setIp(userDto, request);
        userDto.setId(info.getId());
        LoginTypeEnum loginTypeEnum = LoginTypeEnum.getType(loginType);
        if (ObjectNull.isNotNull(loginTypeEnum)) {
            loginType = loginTypeEnum.getDesc();
        }
        //查询用户扩展信息
        userDto.setLoginType(loginType);
        // 设置登录客户端信息
        //获取租户
        SysConfigs configs = switchTenantConfig(info.getId());
        //获取当前用户在哪些租户下
        Map<String, TenantsDto> map = getTenants(info.getId(), configs);
        userDto.setTenants(map.values().stream().collect(Collectors.toList()));
        Map<String, Object> body = new HashMap<>();
        userExtensionService.list(Wrappers.<UserExtension>lambdaQuery().eq(UserExtension::getUserId, info.getId())).forEach(e -> body.putAll(e.getExtension()));
        //设置扩展参数
        userDto.setExceptions(body);
        return getCustomUser(info.getId(), userDto, configs, map);
    }

    /**
     * 根据请求数据,获取这次的租户id
     *
     * @param userId
     * @return
     */
    public SysConfigs switchTenantConfig(String userId) {
        SysConfigs sysConfigs = new SysConfigs();
        HttpServletRequest request = WebUtils.getRequest();
        String tenantId = request.getParameter("switch");
        //匹配域名
        if (ObjectNull.isNull(tenantId)) {
            if (!"1".equals(TenantContextHolder.getTenantId())) {
                tenantId = TenantContextHolder.getTenantId();
            }
        }
        if (ObjectNull.isNull(tenantId)) {
            tenantId = (String) redisUtils.get("tenant:" + userId);
        }
        if (ObjectNull.isNull(tenantId)) {
            tenantId = "1";
        }
        TenantContextHolder.setTenantId(tenantId);
        //获取终端
        String clientId = request.getParameter("client_id");
        return sysConfigs.setTenantId(tenantId).setClientId(clientId);
    }

    public CustomUser getCustomUser(String userId, UserDto userDto, SysConfigs configs, Map<String, TenantsDto> map) {
        //选择当前登陆的租户
        TenantsDto tenant = map.get(configs.getTenantId());
        userDto.setTenantId(configs.getTenantId());
        //获取租户的详细信息
        //设置租户
        userDto.setTenant(tenant);
        userDto.setAdminFlag(userDto.getTenant().getAdminFlag());
        userDto.setPlatformAdmin(userDto.getTenant().getPlatformAdmin());
        userDto.setJobName(tenant.getJobName());
        userDto.setJobId(tenant.getJobId());
        //如果没有选租户，则直接不查询数据权限和租户权限.直接返回
        UserInfoDto<UserDto> dataInfo = userInfoComponent.getUserInfoDto(userDto, tenant);
        dataInfo.setUserDto(userDto);
        return BeanCopyUtil.copy(dataInfo, CustomUser.class);
    }

    /**
     * 根据用户id获取租户信息
     *
     * @param userId  用户id
     * @param configs 请求配置的数据值
     * @return
     */
    private Map<String, TenantsDto> getTenants(String userId, SysConfigs configs) {
        //清除租户信息
        TenantContextHolder.clear();
        //根据租户查询配置的Logo icon 等数据
        ConfigsTypeEnum configsTypeEnum = Arrays.stream(ConfigsTypeEnum.values()).filter(e -> ObjectNull.isNotNull(e.clientId)).filter(e -> e.clientId.equals(configs.getClientId())).findFirst().orElseGet(() -> ConfigsTypeEnum.BACKGROUND_PERSONALIZED_CONFIGURATION);

        Map<String, SysConfigs> configsMap = sysConfigService.list(new LambdaQueryWrapper<SysConfigs>().eq(SysConfigs::getType, configsTypeEnum)).stream().collect(Collectors.toMap(SysConfigs::getTenantId, Function.identity()));

        Map<String, TenantsDto> map = new HashMap<>(30);
        Map<String, UserTenant> userTenantMap = userTenantService.list(Wrappers.query(new UserTenant().setCancelFlag(false).setUserId(userId))).stream().collect(Collectors.toMap(UserTenant::getTenantId, Function.identity()));
        if (userTenantMap.isEmpty()) {
            throw new BusinessException("用户已注销请联系管理员");
        }
        //完善logo名称icon
        //过滤出来启用的租户的数据
        Map<String, UserTenant> finalUserTenantMap = userTenantMap;
        tenantService.list(new LambdaQueryWrapper<TenantPo>().eq(TenantPo::getEnable, true).in(TenantPo::getId, userTenantMap.keySet())).stream().forEach(e -> {
            UserTenant value = finalUserTenantMap.get(e.getId());
            //设置是否是超级管理员,或平台管理员
            TenantsDto tenant = BeanCopyUtil.copy(value, TenantsDto.class);
            tenant.setTopTenant(ObjectNull.isNull(e.getParentId()));
            tenant.setId(e.getId());
            tenant.setAdminFlag(e.getAdminUserId().equals(userId));
            tenant.setPlatformAdmin(tenant.getAdminFlag() && ObjectNull.isNull(e.getParentId()));
            tenant.setEmployeeNo(value.getEmployeeNo());
            tenant.setLevel(value.getAccountLevel());
            //根据用户等级查询是否有自定义首页
            tenant.setLevelId(value.getAccountLevelId());
            tenant.setJobId(value.getJobId());
            tenant.setJobName(value.getJobName());
            List<String> deptIds = value.getDeptId();
            TenantContextHolder.setTenantId(e.getId());
            if (ObjectNull.isNotNull(deptIds)) {
                //获取部门
                List<DeptDto> collect = deptService.listByIds(deptIds).stream().map(v -> new DeptDto().setDeptId(v.getId()).setDeptName(v.getName()).setDeptCode(v.getDeptCode())).collect(Collectors.toList());
                tenant.setDept(collect);
            } else {
                tenant.setDept(new ArrayList<>());
            }
            SysConfigs sysConfigs = configsMap.get(e.getId());
            if (ObjectNull.isNotNull(sysConfigs)) {
                SysApplyConfig config = (SysApplyConfig) BeanCopyUtil.copy(sysConfigs.getContent(), SysApplyConfig.class).setEnable(true);
                //租户配
                tenant.setIcon(config.getIcon());
                tenant.setShortName(e.getName());
                tenant.setLogo(config.getLogo());
                tenant.setSystemName(config.getSystemName());
                tenant.setLoginDomain(config.getDomainName());
            } else {
                SysApplyConfig config = new SysApplyConfig();
                tenant.setIcon(config.getIcon());
                tenant.setShortName(e.getName());
                tenant.setLogo(config.getLogo());
            }
            map.put(e.getId(), tenant);
        });
        //没有租户,直接无法登陆
        if (map.isEmpty()) {
            throw new BusinessException("用户不在组织内");
        }
        if (map.containsKey(configs.getTenantId())) {
            TenantContextHolder.setTenantId(configs.getTenantId());
        } else {
            //如果是第一次登陆
            configs.setTenantId(map.keySet().iterator().next());
        }
        return map;
    }


    public void setIp(UserDto userDto, HttpServletRequest request) {
        String userAgent = request.getHeader("User-agent");
        userDto.setUserAgent(userAgent);
        String ipAddr = IpUtil.getIpAddr(request);
        if (transitionConfig.getTransition()) {
            //转换IP地址
            String transition = IpUtils.transition(ipAddr, userDto.getClientId());
            userDto.setIp(transition);
        } else {
            userDto.setIp(ipAddr);
        }
    }

}
