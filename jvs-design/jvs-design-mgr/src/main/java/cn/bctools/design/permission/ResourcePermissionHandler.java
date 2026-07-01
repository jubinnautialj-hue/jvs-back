package cn.bctools.design.permission;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.design.crud.entity.DesignRole;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.data.util.RoleUtils;
import cn.bctools.design.identification.entity.Identification;
import cn.bctools.design.identification.service.IdentificationService;
import cn.bctools.design.menu.service.AppMenuService;
import cn.bctools.design.permission.service.DesignPermissionService;
import cn.bctools.design.permission.service.PermissionSettingService;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.util.CurrentAppUtils;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.web.utils.WebUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static cn.bctools.design.filter.DesignWebMvcConfig.RULE_API_VERIFICATION_PATTERN;

/**
 * @author bctools.cn
 * 应用权限校验
 */
@Data
@Slf4j
@Component
@AllArgsConstructor
public class ResourcePermissionHandler implements BasePermissionHandlerHandler {

    private final PermissionSettingService permissionSettingService;
    private final AppMenuService appMenuService;
    private final IdentificationService service;
    private final DesignPermissionService designPermissionService;
    /**
     * 需要鉴权的应用使用权限的请求
     * {type}不是变量，可根据type的值知道应该从那个缓存或数据库查询数据
     */
    private static final String APP_USE_PERMISSION_VERIFICATION_PATTERN = "/app/use/{appId}/{type}/**/{resourcesId}/**";
    private static final String APP_USE_IDENTIFICATION_PATTERN = "/app/identification/use/dynamic/data/**";
    /**
     * 资源id（如表单id、列表id等）
     */
    private static final String RESOURCES_ID = "resourceId";

    /**
     * 匹配 自定义接入权限并且需要加载权限
     *
     * @return
     */
    public static Boolean matcher() {
        try {
            String string = WebUtils.getRequest().getRequestURI().toString();
            if (PATH_MATCHER.matchStart(RULE_API_VERIFICATION_PATTERN, string)) {
                //并且一定要有登录
                UserCurrentUtils.getCurrentUser();
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }


    @Override
    public boolean check(UserDto userDto, String appId, JvsApp jvsApp, String requestUri, Map<String, Object> variablesAttribute, String method) throws BusinessException {
        if (PATH_MATCHER.matchStart(APP_USE_IDENTIFICATION_PATTERN, requestUri)) {
            // 是自己创建的应用，则放行
            if (UserCurrentUtils.getUserId().equals(jvsApp.getCreateById())) {
                return Boolean.TRUE;
            }
            //区分不同的操作，分割标识
            String identifier;
            if (method.toLowerCase().equals("DELETE".toLowerCase())) {
                int i = requestUri.lastIndexOf("/");
                int i1 = requestUri.substring(0, i).lastIndexOf("/");
                identifier = requestUri.substring(i1 + 1, i);
            } else {
                identifier = requestUri.substring(requestUri.lastIndexOf("/") + 1);
            }
            JvsApp app = CurrentAppUtils.getApp();
            Identification one = service.getOne(Wrappers.query(new Identification().setIdentifier(identifier).setJvsAppId(app.getId()).setDesignType(DesignType.data)));
            Boolean operationPermissions = designPermissionService.getOperationPermissions(one);
            if (Boolean.FALSE.equals(operationPermissions)) {
                throw new BusinessException("没有权限操作");
            } else {
                return true;
            }
        }
        if (PATH_MATCHER.matchStart(APP_USE_PERMISSION_VERIFICATION_PATTERN, requestUri)) {
            String resourceId = null;
            // 优先从路径变量中获取资源id
            if (MapUtils.isNotEmpty(variablesAttribute)) {
                resourceId = Optional.ofNullable(variablesAttribute.get(RESOURCES_ID)).map(String::valueOf).orElseGet(() -> null);
            }
            // 是自己创建的应用，则放行
            if (UserCurrentUtils.getUserId().equals(jvsApp.getCreateById())) {
                return Boolean.TRUE;
            }
            // 校验资源权限
            if (StringUtils.isNotBlank(resourceId)) {
                String type = requestUri.split("/")[4];
                if (Boolean.FALSE.equals(RoleUtils.hasPermit(getRoleByDb(type, resourceId, appId)))) {
                    throw new BusinessException("没有权限操作");
                } else {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }


    /***
     * 获取权限
     * @param type
     * @param resourceId
     * @param appId
     * @return
     */
    private List<DesignRole> getRoleByDb(String type, String resourceId, String appId) {
        switch (type) {
            case "form":
            case "crudPage":
            case "screen":
            case "jvsAppUrl":
                return appMenuService.getDesignMenu(resourceId, appId).getRoles();
            case "chart":
                // 图表资源权限不在这里获取
                return Collections.emptyList();
            default:
                return Collections.emptyList();
        }
    }


}
