package cn.bctools.gateway.entity;

/**
 * 资源分类,
 * 租户资源,和平台资源
 * 如果启用了多租户模式, 就默认返回租户
 *
 * @author xh
 */
public enum TypeEnum {
    /**
     * 租户资源,  只有非顶级租户才能使用
     */
    tenant,
    /**
     * 平台资源, 包含开发后台的功能
     */
    platform
}
