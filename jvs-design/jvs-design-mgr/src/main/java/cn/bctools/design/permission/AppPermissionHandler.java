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
public class AppPermissionHandler implements BasePermissionHandlerHandler {

    JvsAppService jvsAppService;

    /**
     * 需要校验应用管理权限的请求
     */
    private static final String APP_MANAGE_PERMISSION_VERIFICATION_PATTERN = "/app/manage/{appId}/**";

    @Override
    public boolean check(UserDto userDto, String appId, JvsApp jvsApp, String requestUri, Map<String, Object> variablesAttribute, String method) throws BusinessException {
        // 校验应用管理权限
        if (PATH_MATCHER.matchStart(APP_MANAGE_PERMISSION_VERIFICATION_PATTERN, requestUri)) {
            //校验是否有管理应用权限，如果有权限才继续向下，如果发布后，进行缓存处理
            AppRoleDto appRole = Optional.ofNullable(jvsApp.getRole()).orElseGet(AppRoleDto::new);
            if (UserCurrentUtils.getUserId().equals(jvsApp.getCreateById()) || jvsAppService.checkRole(appRole.getAdminMember(), UserCurrentUtils.getCurrentUser())) {
                //如果应用的创建人和管理员是自己直接返回
                return true;
            }
            throw new BusinessException("没有权限操作");
        }
        return false;
    }

}
