package cn.bctools.design.permission;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.entity.dto.AppRoleDto;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * @author bctools.cn
 * 应用权限校验
 */
@Data
@Slf4j
@Component
@AllArgsConstructor
public class DesignPermissionHandler implements BasePermissionHandlerHandler {

    JvsAppService jvsAppService;

    /**
     * 需要校验应用设计权限的请求
     */
    private static final String APP_PERMISSION_VERIFICATION_PATTERN = "/app/design/{appId}/**";

    /**
     * 校验应用是否有权限，如果有权限才继续向下，如果发布后，进行缓存处理
     * 验证条件：
     * 当前用户是应用管理员 && 该管理员自己创建的应用
     *
     * @param appId
     */
    @Override
    public boolean check(UserDto userDto, String appId, JvsApp jvsApp, String requestUri, Map<String, Object> variablesAttribute) throws BusinessException {
        // 校验设计权限
        if (PATH_MATCHER.matchStart(APP_PERMISSION_VERIFICATION_PATTERN, requestUri)) {
            //  是自己创建的应用、作为应用管理员或开发者，则放行
            AppRoleDto appRole = Optional.ofNullable(jvsApp.getRole()).orElseGet(AppRoleDto::new);
            if (UserCurrentUtils.getUserId().equals(jvsApp.getCreateById()) ||
                    jvsAppService.checkRole(appRole.getAdminMember(), UserCurrentUtils.getCurrentUser()) ||
                    jvsAppService.checkRole(appRole.getDevMember(), UserCurrentUtils.getCurrentUser())) {
                return Boolean.TRUE;
            }
            throw new BusinessException("没有权限操作");
        }
        return false;
    }


}
