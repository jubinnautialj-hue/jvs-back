package cn.bctools.common.enums;

/**
 * 所有配置的声明
 *
 * @author guojing
 */
public enum ConfigsTypeEnum {

    /**
     * Base configs type enum.
     */
    BASE("平台配置", PlatformConfig.class),

    /**
     * Workflow todo reminder configs type enum.
     */
    WORKFLOW_TODO_REMINDER("工作流待办提醒", SysNoticeConfig.class),

    /**
     * Workflow reminder configs type enum.
     */
    WORKFLOW_REMINDER("工作流催办提醒", SysNoticeConfig.class),

    /**
     * Sms configuration configs type enum.
     */
    SMS_CONFIGURATION("短信配置", SysConfigSms.class),

    /**
     * Mail configuration configs type enum.
     */
    MAIL_CONFIGURATION("邮件配置", SysConfigMail.class),

    /**
     * Nail application configuration configs type enum.
     */
    NAIL_APPLICATION_CONFIGURATION("钉钉应用", SysConfigDing.class),

    /**
     * Enterprise wechat application configuration configs type enum.
     */
    ENTERPRISE_WECHAT_APPLICATION_CONFIGURATION("企业微信", SysConfigEnterriseWeChat.class),

    /**
     * Wechat official account configuration configs type enum.
     */
    WECHAT_OFFICIAL_ACCOUNT_CONFIGURATION("微信公众号", SysConfigWx.class),
    /**
     * Wechat mp message configuration configs type enum.
     */
    WECHAT_MP_MESSAGE_CONFIGURATION("微信公众号自动回复配置", SysConfigWx.class),

    /**
     * Ldap configuration configs type enum.
     */
    LDAP_CONFIGURATION("LDAP配置", SysConfigLdap.class),

//    WECHAT_OFFICIAL_ACCOUNT_CONFIGURATION("微信公众号自动回复配置", SysConfigWx.class),

    /**
     * Enterprise document enterprise configuration configs type enum.
     */
    ENTERPRISE_DOCUMENT_ENTERPRISE_CONFIGURATION("企业文档", SysFrameApplyConfig.class, "knowledge", "document-mgr", "/jvs-ui-public/img/file.png", "协同编辑+内容管理平台+内容搜索引擎+私有网盘"),

    /**
     * Personalized configuration of enterprise planning configs type enum.
     */
    PERSONALIZED_CONFIGURATION_OF_ENTERPRISE_PLANNING("项目管理", SysFrameApplyConfig.class, "teamwork", "teamwork-ultimate-mgr", "/jvs-ui-public/img/plan.png", "减少无效会议与沟通的最简单办法"),

    /**
     * Personalized configuration of email service configs type enum.
     */
    PERSONALIZED_CONFIGURATION_OF_EMAIL_SERVICE("企业邮桶", SysFrameApplyConfig.class, "mail", "mail-mgr", "/jvs-ui-public/img/email.png", "基于企业内部私有化部署的在线多邮件web客户端"),

    /**
     * Rule personalization configuration configs type enum.
     */
    RULE_PERSONALIZATION_CONFIGURATION("规则引擎", SysFrameApplyConfig.class, "rules", "risk-mgr", "/jvs-ui-public/img/rule.png", "将复杂的业务逻辑抽离成可视化的配置耦合度降低"),

    /**
     * Personalized configuration for video conferencing configs type enum.
     */
    PERSONALIZED_CONFIGURATION_FOR_VIDEO_CONFERENCING("视频会议", SysFrameApplyConfig.class, "meeting", "meeting-mgr", "/jvs-ui-public/img/meeting.png", "内部私有化部署的在线会议，安全、私密与便捷的高度融合"),

    /**
     * Personalized configuration iot platforms configs type enum.
     */
    PERSONALIZED_CONFIGURATION_IOT_PLATFORMS("物联网平台", SysFrameApplyConfig.class, "iot", "jvs-iot-core-mgr", "/jvs-ui-public/img/iot.png", "应用和设备之间通过物联网平台实现双向通信和下行控制"),

    /**
     * Personalized configuration of data intelligence warehouse configs type enum.
     */
    PERSONALIZED_CONFIGURATION_OF_DATA_INTELLIGENCE_WAREHOUSE("智能BI", SysFrameApplyConfig.class, "StoredDataWarehouse", "jvs-chart-mgr", "/jvs-ui-public/img/dataSet.png", "无代码构建企业级的数据智能仓库"),

    /**
     * Background personalized configuration configs type enum.
     */
    BACKGROUND_PERSONALIZED_CONFIGURATION("管理后台", SysFrameApplyConfig.class, "frame", "jvs-auth-mgr", "/jvs-ui-public/img/platform.png", "开箱即用的基础脚手架，微服务构架、VUE+ElementUI、支持多租户"),

    /**
     * Automation configuration configs type enum.
     */
    AUTOMATION_CONFIGURATION("逻辑引擎", SysFrameApplyConfig.class, "automation", "jvs-automation-mgr", "/jvs-ui-public/img/platform.png", "开箱即用的基础脚手架，微服务构架、VUE+ElementUI、支持多租户"),

    /**
     * Jvs design mgr configs type enum.
     */
    JVS_DESIGN_MGR("低代码", "jvs-design-mgr"),

    JVS_DESIGN_DEFAULT_MODE("低代码默认模式", "jvs-design-default-mode"),

    /**
     * Appbascsetting configs type enum.
     */
    APPBASCSETTING("移动端", SysConfigApp.class),

    /**
     * Auto create user head img configs type enum.
     */
    AUTO_CREATE_USER_HEAD_IMG("创建用户是否生成头像", AutoCreateUserHeadImgConfig.class),

    /**
     * Error message send ding configs type enum.
     */
    ERROR_MESSAGE_SEND_DING("将异常发送至钉钉", ErrorMessageSendDingConfig.class),
    ;

    /**
     * 解释
     */
    public String msg;
    /**
     * 客户端名称
     */
    public String clientId = null;
    /**
     * 服务名称 , 如果此服务没有启动,不回显此些信息
     */
    public String serviceName = null;
    /**
     * 服务在线资源，用于判断服务是否启动
     */
    public String iconUrl = null;
    /**
     * 应用描述
     */
    public String desc = null;

    /**
     * The Cls.
     */
    public Class<? extends SysConfigBase> cls = SysConfigBase.class;

    ConfigsTypeEnum(String msg) {
        this.msg = msg;
    }

    ConfigsTypeEnum(String msg, Class cls) {
        this.msg = msg;
        this.cls = cls;
    }

    ConfigsTypeEnum(String msg, Class cls, String clientId) {
        this.msg = msg;
        this.cls = cls;
        this.clientId = clientId;
    }

    ConfigsTypeEnum(String msg, Class cls, String clientId, String serviceName) {
        this.msg = msg;
        this.cls = cls;
        this.clientId = clientId;
        this.serviceName = serviceName;
    }

    ConfigsTypeEnum(String msg, Class cls, String clientId, String serviceName, String iconUrl) {
        this.msg = msg;
        this.cls = cls;
        this.clientId = clientId;
        this.serviceName = serviceName;
        this.iconUrl = iconUrl;
    }

    ConfigsTypeEnum(String msg, Class cls, String clientId, String serviceName, String iconUrl, String desc) {
        this.msg = msg;
        this.cls = cls;
        this.clientId = clientId;
        this.serviceName = serviceName;
        this.iconUrl = iconUrl;
        this.desc = desc;
    }

    ConfigsTypeEnum(String msg, String serviceName) {
        this.msg = msg;
        this.serviceName = serviceName;
    }
}
