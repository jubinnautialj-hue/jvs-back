package cn.bctools.design.permission;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.design.project.entity.JvsApp;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.Map;

/**
 * 检查轻应用请求地址权限的处理器接口
 *
 * @author bctools.cn
 */
public interface BasePermissionHandlerHandler {
    /**
     * The constant PATH_MATCHER.
     */
    PathMatcher PATH_MATCHER = new AntPathMatcher();

    /**
     * 检查应用是否有权限
     * 如果异常直接报错返回
     *
     * @param userDto            用户对象
     * @param appId              the app id
     * @param jvsApp             应用
     * @param requestUri         请求对象
     * @param variablesAttribute 请求参数
     * @return the boolean
     * @throws BusinessException the business exception
     */
    boolean check(UserDto userDto, String appId, JvsApp jvsApp, String requestUri, Map<String, Object> variablesAttribute) throws BusinessException;

}
