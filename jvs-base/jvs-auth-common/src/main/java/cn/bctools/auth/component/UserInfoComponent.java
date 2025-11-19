package cn.bctools.auth.component;

import cn.bctools.auth.entity.*;
import cn.bctools.auth.service.*;
import cn.bctools.common.entity.dto.DeptDto;
import cn.bctools.common.entity.dto.TenantsDto;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.entity.dto.UserInfoDto;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.SysConfigBase;
import cn.bctools.common.enums.SysFrameApplyConfig;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.common.utils.jvs.JvsServiceConfig;
import cn.bctools.common.utils.jvs.JvsSystemConfig;
import cn.bctools.gateway.entity.TenantPo;
import cn.bctools.oss.props.OssProperties;
import cn.bctools.web.utils.WebUtils;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author guojing
 */
@Component
@Slf4j
@AllArgsConstructor
public class UserInfoComponent {

    DeptService deptService;
    UserLevelService userLevelService;
    JobService jobService;
    UserRoleComponent userRoleComponent;
    RoleService roleService;
    PermissionService permissionService;
    UserService userService;
    TenantService tenantService;
    OssProperties ossProperties;
    UserTenantService userTenantService;
    UserExtensionService userExtensionService;
    JvsSystemConfig jvsSystemConfig;
    SysConfigsService configService;

    // 定义域名的正则表达式
    static String DOMAIN_REGEX =
            "^(?!-)(?!.*--)(?!.*-\\.)[A-Za-z0-9-]{1,63}(?!-)(\\.[A-Za-z]{2,})+$";
    static Pattern pattern = Pattern.compile(DOMAIN_REGEX);

    /**
     * 根据用户信息，和租户信息获取资源角色，数据权限对象
     *
     * @param one  当前用户租户
     * @param user 用户基本信息
     * @return 用户详细信息
     */
    public UserInfoDto<UserDto> getUserInfoDto(UserDto user, TenantsDto one) {
        String userId = user.getId();

        user.setDept(one.getDept());
        // 获取资源权限
        List<String> roleIds = null;
        //如果是管理员或平台管理员,不查询角色
//        if (!(one.getPlatformAdmin() || one.getAdminFlag())) {
        roleIds = userRoleComponent.getUserRoleIds(userId);
//        }

        List<String> permission = permissionService.getPermission(one.getPlatformAdmin(), one.getAdminFlag(), one.getTenantId(), roleIds);
        user.setRoleIds(roleIds);
        {
            try {
                HttpServletRequest request = WebUtils.getRequest();
                String header = request.getHeader("host");
                jvsSystemConfig.getIdentificationDomain().forEach(e -> {
                    if (e.equals(header)) {
                        //根据域名判断， 是否存在标识，如果存在，则标识直接去掉轻应用标识权限标识。
                        permission.remove("jvs_app");
                        permission.remove("jvs_platform");
                    }
                });
            } catch (Exception e) {

            }
        }
        // 获取子部门id集合
        List<String> childDeptIds = user.getDept().stream().flatMap(e -> deptService.getAllChildId(e.getDeptId()).stream()).collect(Collectors.toList());
        // 组装用户对象
        return new UserInfoDto<>()
                .setRoles(roleIds)
                .setPermissions(permission)
                .setChildDeptIds(childDeptIds);
    }


    /**
     * 根据用户信息和租户信息查询扩展信息
     * <p>
     *
     * @param user     用户信息
     * @param tenantId 租户id
     * @return 用户信息
     */
    public UserDto getUserInfoDto(User user, String tenantId) {
        //此没有租户
        return this.getUserInfoDto(Collections.singletonList(user), tenantId).get(0);
    }

    /**
     * 根据用户信息和租户信息查询扩展信息
     * <p>
     *
     * @param userList 用户信息集合
     * @param tenantId 租户id
     * @return 用户信息
     */
    public List<UserDto> getUserInfoDto(List<User> userList, String tenantId) {
        //此没有租户
        if (ObjectUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        UserDto userDto;
        List<UserDto> result = new ArrayList<>();
        if (StringUtils.isNotBlank(tenantId)) {
            TenantContextHolder.setTenantId(tenantId);
            // 查询租户相关信息
            Set<String> userIds = userList.stream().map(User::getId).collect(Collectors.toSet());
            Map<String, List<Role>> userRoles = userRoleComponent.getUserRoleIds(userIds);
            List<UserTenant> tenantList = userTenantService.list(Wrappers.<UserTenant>lambdaQuery().in(UserTenant::getUserId, userIds));
            Map<String, UserTenant> userTenantMap = tenantList.stream().collect(Collectors.toMap(UserTenant::getUserId, Function.identity()));
            // 判断是否是超级管理员
            TenantPo thisTenantPo = tenantService.getById(tenantId);
            String adminUserId = thisTenantPo.getAdminUserId();
            for (User user : userList) {
                String userId = user.getId();
                userDto = BeanCopyUtil.copy(UserDto.class, user, userTenantMap.get(userId));
                if (ObjectNull.isNull(userDto.getRealName())) {
                    userDto.setRealName(userDto.getAccountName());
                }
                String headImg = userDto.getHeadImg();
                if ("/jvs-ui-public/img/headImg.png".equals(headImg)) {
                    SysFrameApplyConfig config = configService.getConfig(ConfigsTypeEnum.BACKGROUND_PERSONALIZED_CONFIGURATION);
                    JvsServiceConfig service = jvsSystemConfig.getService().stream().filter(e -> ConfigsTypeEnum.BACKGROUND_PERSONALIZED_CONFIGURATION.equals(e.getName())).findFirst().get();
                    if (pattern.matcher(jvsSystemConfig.getDomain()).matches()) {
                        //直接根据主域名获取前缀获取全地址
                        String url = jvsSystemConfig.getProtocol() + config.getDomainName() + "." + jvsSystemConfig.getDomain();
                        headImg = url + "/" + headImg;
                    } else {
                        //直接根据主域名获取前缀获取全地址
                        String url = jvsSystemConfig.getProtocol() + jvsSystemConfig.getDomain() + ":" + service.getPort();
                        headImg = url + "/" + headImg;
                    }

                } else if (headImg.startsWith("/")) {
                    try {
                        if (StringUtils.isNotBlank(ossProperties.getOutsideEndpoint())) {
                            headImg = ossProperties.getOutsideEndpoint() + headImg;
                        } else {
                            headImg = ossProperties.getEndpoint().trim().startsWith("http") ? ossProperties.getEndpoint().trim() + headImg : "http://" + ossProperties.getEndpoint().trim() + headImg;
                        }
                    } catch (Exception e) {
                        //可能通过逻辑处理无法获取
                    }
                }
                Map<String, Object> body = new HashMap<>();
                userExtensionService.list(Wrappers.<UserExtension>lambdaQuery().eq(UserExtension::getUserId, user.getId())).forEach(e -> {
                    if (ObjectNull.isNotNull(e.getExtension())) {
                        body.putAll(e.getExtension());
                    }
                });
                //设置扩展参数
                userDto.setExceptions(body);
                userDto.setHeadImg(headImg);
                if (userTenantMap.containsKey(userId)) {
                    List<String> id = userTenantMap.get(userId).getDeptId();
                    if (ObjectNull.isNotNull(id)) {
                        List<DeptDto> dtoList =
                                id.stream().map(e -> deptService.getById(e)).filter(ObjectNull::isNotNull).map(e -> new DeptDto().setDeptId(e.getId()).setType(e.getType()).setDeptName(e.getName()).setDeptCode(e.getDeptCode())).collect(Collectors.toList());
                        userDto.setDept(dtoList);
                    }
                }
                List<String> childDeptIds = userDto.getDept().stream().flatMap(e -> deptService.getAllChildId(e.getDeptId()).stream()).collect(Collectors.toList());
                userDto.setChildDeptIds(childDeptIds);
                userDto.setId(userId);
                userDto.setAdminFlag(userId.equalsIgnoreCase(adminUserId));
                userDto.setRoleIds(Optional.ofNullable(userRoles.get(userId)).orElseGet(ArrayList::new).stream().map(Role::getId).collect(Collectors.toList()));
                userDto.setRoleType(Optional.ofNullable(userRoles.get(userId)).orElseGet(ArrayList::new).stream().map(Role::getRoleName).collect(Collectors.toList()));
                result.add(userDto);
            }
        } else {
            result = userList.stream().map(user -> BeanCopyUtil.copy(user, UserDto.class)).collect(Collectors.toList());
        }
        // 密码不返回
        result.forEach(e -> e.setPassword(null));
        return result;
    }

}
