package cn.bctools.auth.entity.enums;

/**
 * @author guojing
 */

public enum ModelEnum {
    /**
     * 三方系统通过url携带code码跳转指定页面.后端通过code码,向方三系统获取用户信息.
     * 三方系统通过ip白名单校验是否能调用服务
     */
    token,
    /**
     * 1 三方系统通过code码跳转到登陆页
     * 2 后端通过code码获取access_token
     * 3 通过access_token获取用户信息
     */
    oauth2
}
