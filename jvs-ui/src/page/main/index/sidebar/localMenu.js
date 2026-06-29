export const localMenu =  {
  platformMenu: [
    {
      name: '平台',
      icon: 'jvs-ui-icon-pingtai',
      langKey: 'platform',
      children: [
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-pingtaixinxi",
            url: "/jvs-upms-ui/appBasicInfo",
            name: "平台信息",
          },
          name: "平台信息",
          permisionFlag: "jvs_platform",
          langKey: 'platformInfo'
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-zuhuzuzhi",
            url: "/jvs-upms-ui/tenant",
            name: "租户组织",
          },
          name: "租户组织",
          permisionFlag: "jvs_tenant",
          langKey: 'tenantOrg'
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-denglurizhi1",
            url: "/jvs-upms-ui/systemlogs",
            name: "登录日志",
          },
          name: "登录日志",
          permisionFlag: "jvs_platform_login_log",
          langKey: 'loginLog'
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-caozuorizhi",
            url: "/jvs-upms-ui/errlogs",
            name: "操作日志",
          },
          name: "操作日志",
          permisionFlag: "jvs_platform_operation_log",
          langKey: 'opeLog'
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-yidongduanyijianfankui",
            url: "/jvs-upms-ui/opinion",
            name: "移动端意见反馈",
          },
          name: "移动端意见反馈",
          permisionFlag: "jvs_opinion_page",
          langKey: 'mobileFeedback'
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-yingyongshouquanzhongxin",
            url: "/jvs-upms-ui/auth",
            name: "应用授权中心",
          },
          name: "应用授权中心",
          permisionFlag: "", // 仅源码不需要 jvs_license
          langKey: 'appAuthCenter'
        },
      ]
    },
    {
      name: '扩展开发',
      icon: 'jvs-ui-icon-kuozhankaifa',
      langKey: 'extDev',
      children: [
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-luojizujian",
            url: "/jvs-upms-ui/basicFunction",
            name: "基础函数",
          },
          name: "基础函数",
          permisionFlag: "jvs_function",
          langKey: 'formulaCom'
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-zhongduanguanli",
            url: "/jvs-upms-ui/application",
            name: "终端管理",
          },
          name: "终端管理",
          permisionFlag: "jvs_apply",
          langKey: 'termManage'
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-yingyongzidian",
            url: "/jvs-upms-ui/appDictionary",
            name: "应用字典",
          },
          name: "应用字典",
          permisionFlag: "jvs_app_tree",
          langKey: 'appDic'
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-xitongzidian",
            url: "/jvs-upms-ui/dictionaries",
            name: "系统字典",
          },
          name: "系统字典",
          langKey: 'sysDic'
        },
        {
          extend: {
            newWindow: true,
            icon: "jvs-ui-icon-tubiaoku",
            url: "/jvs-upms-ui/iconList",
            name: "图标库",
          },
          name: "图标库",
          langKey: 'iconLib'
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-yingyongbangzhu",
            url: "/jvs-upms-ui/appicationhelp",
            name: "应用帮助",
          },
          name: "应用帮助",
          langKey: 'appHelp'
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-kaifapeizhi",
            url: "/jvs-upms-ui/devconfig",
            name: "开发配置",
          },
          name: "开发配置",
          langKey: 'devConfig'
        },
      ]
    },
    {
      name: '网关配置',
      icon: 'jvs-ui-icon-wangguanpeizhi',
      langKey: 'gatewayConfig',
      children: [
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-wangguanzidingyiluyoubiao",
            url: "/jvs-upms-ui/gatewayRouter",
            name: "网关自定义路由表",
          },
          name: "网关自定义路由表",
          permisionFlag: "jvs_gateway_route",
          langKey: 'gateRoute'
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-wangguanXSShulve",
            url: "/jvs-upms-ui/gatewayIgnore",
            name: "网关加密忽略",
          },
          name: "网关加密忽略",
          permisionFlag: "jvs_gateway_encode",
          langKey: 'gateIgnore'
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-wangguanjiamihulve",
            url: "/jvs-upms-ui/gatewayIgnoreIp",
            name: "网关加密忽略(ip)",
          },
          name: "网关加密忽略(ip)",
          permisionFlag: "jvs_gateway_ip",
          langKey: 'gateIgnoreIp'
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-wangguanXSShulve",
            url: "/jvs-upms-ui/xssIgnore",
            name: "网关XSS忽略",
          },
          name: "网关XSS忽略",
          permisionFlag: "jvs_gateway_xss",
          langKey: 'gateXssIg'
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-wangguancuowuma",
            url: "/jvs-upms-ui/errCode",
            name: "网关错误码",
          },
          name: "网关错误码",
          permisionFlag: "jvs_gateway_code",
          langKey: 'gateErr'
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-wangguanquanxianhulvepeizhi",
            url: "/jvs-upms-ui/gatewayUrl",
            name: "网关权限忽略配置",
          },
          name: "网关权限忽略配置",
          permisionFlag: 'jvs_gateway_ignore_path',
          langKey: 'gateIgConfig'
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-duoyuyanwenan",
            url: "/jvs-upms-ui/loginIP",
            name: "登录IP限制",
          },
          name: "登录IP限制",
          permisionFlag: 'jvs_login_rules',
          langKey: 'loginIp'
        },
      ]
    }
  ],
  systemMenu: [
    {
      name: '组织',
      icon: 'jvs-ui-icon-zuzhi',
      langKey: 'organize',
      children: [
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-jibenxinxi",
            url: "/jvs-upms-ui/systeminfo",
            name: "基本信息",
          },
          name: "基本信息",
          langKey: 'basicInfo'
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-tuanduiguanli",
            url: "/jvs-upms-ui/group",
            name: "团队管理",
          },
          name: "团队管理",
          permisionFlag: "jvs_group",
          langKey: 'teamManage'
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-jiaoseguanli",
            url: "/jvs-upms-ui/role",
            name: "角色管理",
          },
          name: "角色管理",
          permisionFlag: "jvs_role",
          langKey: 'roleManage'
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-yonghuguanli",
            url: "/jvs-upms-ui/department",
            name: "用户管理",
          },
          name: "用户管理",
          permisionFlag: "jvs_user",
          langKey: 'userManage'
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-yonghushenhe",
            url: "/jvs-upms-ui/usercheck",
            name: "用户审核",
          },
          name: "用户审核",
          permisionFlag: "jvs_role_user_examine",
          langKey: 'userReview'
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-gangweiguanli",
            url: "/jvs-upms-ui/postList",
            name: "岗位管理",
          },
          name: "岗位管理",
          permisionFlag: "jvs_job",
          langKey: 'posManage'
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-gonggaoguanli",
            url: "/jvs-upms-ui/bulletin",
            name: "公告管理",
          },
          name: "公告管理",
          permisionFlag: "jvs_bulletin",
          langKey: 'annManage'
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-xiaoxiguanli",
            url: "/jvs-upms-ui/message",
            name: "消息管理",
          },
          name: "消息管理",
          permisionFlag: "jvs_message",
          langKey: 'messManage'
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-yonghudengji",
            url: "/jvs-upms-ui/userLevel",
            name: "用户等级",
          },
          name: "用户等级",
          permisionFlag: "jvs_level",
          langKey: 'usrLevel'
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-shanchuyonghu",
            url: "/jvs-upms-ui/userdel",
            name: "删除用户",
          },
          name: "删除用户",
          permisionFlag: "jvs_delete_user",
          langKey: 'deluser'
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-OArenwuzujian",
            url: "/jvs-upms-ui/proxylist",
            name: "代理中心",
          },
          name: "代理中心",
          permisionFlag: "jvs_flow_proxy",
          langKey: 'agencyCenter'
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-OAzujian",
            url: "/jvs-upms-ui/processmanage",
            name: "流程管理",
          },
          name: "流程管理",
          permisionFlag: "jvs_flow_task_manage",
          langKey: 'processManage'
        },
      ],
    },
    {
      name: '系统',
      icon: 'jvs-ui-icon-xitong',
      langKey: 'system',
      children: [
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-huihuaguanli",
            url: "/jvs-upms-ui/onlineUser",
            name: "会话管理",
          },
          name: "会话管理",
          permisionFlag: "jvs_session",
          langKey: 'sessManage',
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-denglurizhi",
            url: "/jvs-upms-ui/loginlogs",
            name: "登录日志",
          },
          name: "登录日志",
          permisionFlag: "jvs_login_log",
          langKey: 'loginLog'
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-yingyongcaozuorizhi",
            url: "/jvs-upms-ui/appOperationLog",
            name: "应用操作日志",
          },
          name: "应用操作日志",
          permisionFlag: "jvs_app_log",
          langKey: 'appOpLog'
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-wenjianguanli",
            url: "/jvs-upms-ui/file",
            name: "文件管理",
          },
          name: "文件管理",
          permisionFlag: "jvs_file",
          langKey: 'fileManage'
        },
        // {
        //   extend: {
        //     newWindow: false,
        //     icon: "jvs-ui-icon-xitongkongjian",
        //     url: "/jvs-upms-ui/systemspace",
        //     name: "系统空间",
        //   },
        //   name: "系统空间",
        //   permisionFlag: "jvs_space",
        //   langKey: 'sysSpace'
        // },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-zhuxiaozuzhi",
            url: "/jvs-upms-ui/orglogout",
            name: "注销组织",
          },
          name: "注销组织",
          permisionFlag: "jvs_unregister_organization",
          langKey: 'delTenant'
        }
      ],
    },
    {
      name: '开发者',
      icon: 'jvs-ui-icon-kaifazhe',
      langKey: 'dev',
      children: [
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-gongzhonghaoxiaoximoban",
            url: "/jvs-upms-ui/wxmessagetemplate",
            name: "公众号消息模板",
          },
          name: "公众号消息模板",
          permisionFlag: "jvs_message_wx_mp_template",
          langKey: 'offTemp'
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-duanxinxiaoximoban",
            url: "/jvs-upms-ui/shortmessagetemplate",
            name: "短信消息模板",
          },
          name: "短信消息模板",
          permisionFlag: "jvs_message_sms_template",
          langKey: 'smsTemp'
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-gongzhonghaozidonghuifu",
            url: "/jvs-upms-ui/settings",
            name: "公众号自动回复",
          },
          name: "公众号自动回复",
          permisionFlag: "jvs_message_wx_mp_auth_reply",
          langKey: 'offAcc'
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-xitongshezhi1",
            url: "/jvs-upms-ui/systemconfig",
            name: "系统设置",
          },
          name: "系统设置",
          permisionFlag: "jvs_base_config",
          langKey: 'sysSet'
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-huanjingbianliang",
            url: "/jvs-upms-ui/envariable",
            name: "环境变量",
          },
          name: "环境变量",
          permisionFlag: "jvs_variable",
          langKey: 'envVars'
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-luojizujian",
            url: "/jvs-upms-ui/autoExtend",
            name: "逻辑组件",
          },
          name: "逻辑组件",
          permisionFlag: "jvs_rule_extend",
          langKey: 'logicCom'
        },
        {
          extend: {
            newWindow: false,
            icon: "jvs-ui-icon-sanfangyingyongdengluduijie",
            url: "/jvs-upms-ui/trilateralconfig",
            name: "三方应用登录对接",
          },
          name: "三方应用登录对接",
          permisionFlag: "jvs_other_operating_system_login",
          langKey: 'threeLogin'
        },
      ],
    }
  ]
}
