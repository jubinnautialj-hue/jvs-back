package cn.bctools.auth.entity.enums;

/**
 * 服务的动态资源
 * 如果服务没有，则不返回次资源
 *
 * @author guojing
 */
public enum DynamicResourceEnum {
    /**低代码*/
    JVS_DESIGN_MGR("低代码", "jvs-design-mgr"),
    /**数据智仓*/
    JVS_CHART_MGR("数据智仓", "jvs-chart-mgr"),
    /**企业文档*/
    jvs_document_service("企业文档", "document-mgr"),
    /**风控管理*/
    jvs_risk_service("风控管理", "risk-mgr"),
    /**企业协同*/
    jvs_teamwork_ultimate_service("企业协同", "teamwork-ultimate"),
    ;


    public String name;
    public String serviceName;

    DynamicResourceEnum(String name, String serviceName) {
        this.name = name;
        this.serviceName = serviceName;
    }

}
