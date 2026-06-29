export default {
  table: {
    fresh: "刷新",
    freshCash: "刷新缓存",
    keyword: "请输入关键词",
    add: "新增",
    plus: "添加",
    edit: "编辑",
    view: "查看",
    info: "详情",
    preview: "预览",
    delete: "删除",
    oprate: "操作",
    enable: "启用",
    disabled: "禁用",
    import: "导入",
    remove: "移除",
    unload: "卸载",
    deploy: "发布",
  },
  form: {
    submit: "提交",
    search: "查询",
    save: "保存",
    reset: "重置",
    cancel: "取消",
  },
  common: {
    all: "全部",
    search: "搜索",
    message: "消息",
    submit: "提交",
    confirm: "确定",
    cancel: "取消",
    explain: "说明",
    tip: "提示",
    tips: "小贴士",
    success: "成功",
    fail: "失败",
    pass: "通过",
    nopass: "不通过",
    yes: "是",
    no: "否",
    addSuccess: "新增成功",
    editSuccess: "编辑成功",
    delSuccess: "删除成功",
    uploadSuccess: "上传成功",
    saveSuccess: "保存成功",
    copySuccess: "复制成功",
    freshSuccess: "刷新成功",
    setSuccess: "设置成功",
    removeSuccess: "移出成功",
    oprateSuccess: "操作成功",
    importSuccess: "导入成功",
    deploySuccess: "发布成功",
    unloadSuccess: "卸载成功",
    sysSuccess: "同步成功",
    copyFail: "复制失败",
    devWarning: "开发人员在源代码开发时配置使用，非管理员请勿修改",
    learnMore: "了解更多",
    deleteConfirm: "此操作将永久删除此数据, 是否继续?",
    all_dept: "所有部门",
    curr_dept: "当前部门",
    curr_dept_tree: "当前部门及以下",
    all_job: "所有岗位",
    curr_job: "当前岗位",
    self: "用户自己创建数据",
    customize: "自定义权限",
    rightDrawer: {
      search: "搜索",
      searchResult: "搜索结果",
      emptyCon: "暂无内容",
      message: "消息通知",
      notify: "通知",
      allRead: "全部已读",
      allMessage: "历史消息",
      close: "收起",
      usercenter: "个人中心",
      accountInfo: "账户设置",
      joinOrg: "加入组织",
      createOrg: "创建组织",
      accSet: "账户设置",
      sigOut: "退出登录",
      switchTenant: "切换租户",
      accountSetting: {
        user: "个人信息",
        userColumn: {
          userTitle: "个人中心",
          avatar: "头像",
          nickName: "昵称",
          rolePermision: "角色权限",
          deptName: "部门",
          employeeNo: "职工编号",
          level: "等级",
          jobName: "岗位",
        },
        account: "账号管理",
        accountColumn: {
          accountTitle: "账号管理",
          bind: "绑定账号",
          bindPhone: "绑定手机号",
          phonePlaceholder: "请输入手机号",
          vcPlaceholder: "请输入验证码",
          bindEmail: "绑定邮箱",
          emailPlaceholder: "请输入邮箱",
          bindThird: "绑定第三方账号",
          WECHAT_MP: "微信",
          WX_ENTERPRISE: "企业微信",
          Ding: "钉钉",
          LDAP: "LDAP",
        },
        password: "设置密码",
        passwordColumn: {
          passwordTitle: "设置密码",
          passText: "设置登录密码",
          passPlaceholder: "请输入密码",
          surePlaceholder: "确认密码",
        },
        button: {
          saveInfo: "保存信息",
          next: "下一步",
          code: "获取验证码",
          bind: "绑定",
          unbind: "解绑",
          confirm: "确定",
          cancel: "取消",
        },
        pleaseUse: "请使用",
        scanCode: "扫码",
        followOff: "关注公众号",
        ldapColumn: {
          account: {
            label: "账号",
            placeholder: "请输入LDAP账号",
          },
          password: {
            label: "密码",
            placeholder: "请输入LDAP密码",
          },
        },
        imgValid: "只能上传png、jpeg、jpg格式的图片",
        unbPhone: "即将解除手机号绑定，是否继续？",
        phone: "手机",
        phoneSuccess: "绑定手机成功",
        unbEmail: "即将解除邮箱绑定，是否继续？",
        email: "邮箱",
        emailSuccess: "绑定邮箱成功",
        regain: "s重新获取",
        unbStrStart: "即将解除",
        unbStrEnd: "绑定，是否继续？",
        unbSuccess: "已成功解绑",
        wxbindSuccess: "微信绑定成功",
        ldapSuccess: "绑定LDAP成功",
        passSuccess: "设置密码成功",
        secondValid: "两次密码不一致,请重新输入!",
      },
    },
    login: {
      tips: "提示",
      outInfo: "是否退出系统, 是否继续？",
      welLogin: "欢迎登录",
      welReg: "注册账号",
      passLogin: "密码登录",
      codeLogin: "验证码登录",
      scanLogin: "扫码登录",
      freshTip: "二维码已失效,点击刷新",
      regTip: "点击注册按钮将视为您同意",
      terms: "使用条款",
      goLogin: "去登录",
      otherType: "其他登录方式",
      loginNow: "立即登录",
      regNow: "注册",
      placeholder: {
        username: "请输入账号",
        account: "请输入LDAP账号",
        phone: "请输入手机号",
        password: "请输入密码",
        code: "请输入验证码",
        getcode: "获取验证码",
        sendcode: "发送验证码",
        resendcoed: "重新发送",
        wechat: "请使用微信扫码关注公众号登录",
        logging: "正在登录...",
        ding: "请使用钉钉扫码登录",
        wxenter: "请使用企业微信扫码登录",
      },
    },
  },
  topNav: {
    appCenter: "应用中心",
    myApp: "我的应用",
    modeUser: {
      GA: "正式模式",
      BETA: "测试模式",
      DEV: "开发模式",
      title: "模式切换",
      GA_tip: "真实用户使用应用功能",
      BETA_tip: "模拟不同的人员使用应用测试权限等",
      DEV_tip: "用于配置和新增设计",
      user: "模拟人员 (非必选)",
      userPlaceholder: "请选择模拟人员",
    },
    system: "平台管理",
    platform: "运维设置",
    support: {
      title: "咨询支持",
      desc: "版本价格",
    },
  },
  localMenu: {
    // 运维设置
    platform: "平台",
    platformInfo: "平台信息",
    tenantOrg: "租户组织",
    loginLog: "登录日志",
    opeLog: "操作日志",
    mobileFeedback: "移动端意见反馈",
    appAuthCenter: "应用授权中心",
    extDev: "扩展开发",
    logicCom: "逻辑组件",
    formulaCom: "基础函数",
    termManage: "终端管理",
    appDic: "应用字典",
    sysDic: "系统字典",
    iconLib: "图标库",
    appHelp: "应用帮助",
    threeLogin: "三方应用登录对接",
    devConfig: "开发配置",
    gatewayConfig: "网关配置",
    gateRoute: "网关自定义路由表",
    gateIgnore: "网关加密忽略",
    gateIgnoreIp: "网关加密忽略(ip)",
    gateXssIg: "网关XSS忽略",
    gateErr: "网关错误码",
    gateIgConfig: "网关权限忽略配置",
    loginIp: "登录IP限制",
    // 平台管理
    organize: "组织",
    basicInfo: "基本信息",
    teamManage: "团队管理",
    roleManage: "角色管理",
    userManage: "用户管理",
    userReview: "用户审核",
    posManage: "岗位管理",
    annManage: "公告管理",
    messManage: "消息管理",
    usrLevel: "用户等级",
    deluser: "删除用户",
    system: "系统",
    sessManage: "会话管理",
    appOpLog: "应用操作日志",
    fileManage: "文件管理",
    sysSpace: "系统空间",
    delTenant: "注销组织",
    dev: "开发者",
    offTemp: "公众号消息模板",
    smsTemp: "短信消息模板",
    offAcc: "公众号自动回复",
    sysSet: "系统设置",
    envVars: "环境变量",
    agencyCenter: "代理中心",
    processManage: "流程管理",
  },
  messagePush: {
    triggerError: "请选择触发条件",
    editPlaceholder: "请输入内容...",
    rules: {
      name: "请填写模板名称",
      receiver: "请选择发送对象",
      type: "请选择发送方式",
      titleHtml: "请填写消息标题",
      contentHtml: "请填写消息内容",
      templateCode: "请选择模板",
      template: "请填写模板内容",
      value: "请选择变量对应字段",
    },
    settingTypeOptions: {
      created: "创建成功",
      edited: "编辑成功",
      deleted: "删除成功",
      flow_created: "启动流程",
      flow_approval_results: "审批结果",
      flow_approval_node: "审批节点",
      any: "任意字段",
      specified_field: "指定字段",
      2: "已通过",
      3: "已拒绝",
      4: "已终止",
    },
    name: {
      label: "模板名称",
    },
    enabled: {
      label: "是否启用",
    },
    timeLimit: {
      label: "限制催办频率",
      placeholder: "维度",
      tip: "时长",
      dicData: {
        MINUTE: "分钟",
        HOUR: "小时",
        DAY: "天",
      },
    },
    triggerSetting: {
      label: "当",
      edit: "编辑成功",
      approval: "到达",
      approvalText: "时",
      results: "为",
      resultsText: "时",
    },
    receiver: {
      label: "发送给",
      placeholder: "选择发送对象",
    },
    type: {
      label: "发送方式",
    },
    systemForm: {
      titleHtml: {
        label: "站内信设置",
        placeholder: "消息标题",
        button: "插入字段",
      },
      contentHtml: {
        label: "站内信内容",
        placeholder: "消息内容",
        button: "插入字段",
      },
    },
    WECHAT_MP: {
      label: "公众号设置",
      templateCode: {
        label: "选择模板",
        placeholder: "请选择模板",
      },
      template: {
        label: "模板内容",
      },
      templateDataList: {
        label: "变量集合",
        placeholder: "请选择字段名称",
      },
    },
    WECHAT_ENTERPRISE: {
      label: "企业微信设置",
      contentHtml: {
        label: "企业微信消息内容",
        placeholder: "消息内容",
        button: "插入字段",
      },
    },
    DING: {
      label: "钉钉设置",
      contentHtml: {
        label: "钉钉消息内容",
        placeholder: "消息内容",
        button: "插入字段",
      },
    },
    EMAIL: {
      label: "EMAIL设置",
      titleHtml: {
        label: "邮件标题",
        placeholder: "消息标题",
        button: "插入字段",
      },
      contentHtml: {
        label: "邮件内容",
        placeholder: "消息内容",
        button: "插入字段",
      },
    },
    SMS: {
      label: "短信设置",
      templateCode: {
        label: "选择模板",
        placeholder: "请选择模板",
      },
      template: {
        label: "模板内容",
      },
      variables: {
        label: "变量集合",
        placeholder: "请选择字段名称",
      },
    },
  },
  appBasicInfo: {
    title: {
      basic: "基本信息",
      resource: "资源容量",
      other: "其他应用",
    },
    basic: {
      version: "版本",
      devices: "设备号",
      deviceBoolean: "是否集群部署",
      dateTime: "最近启动时间",
      copy: "复制",
    },
    resource: {
      notsupport: "不支持",
      used: "已使用",
      notlimit: "无限制",
    },
  },
  tenant: {
    desc: {
      line1:
        "解释：租户组织可以理解为部署在云端的客户，通常是一个公司不需要开发能力，也可以快速构建业务，本地不需要做任何部署，而此业务模式可以快速复制到不同公司。对于这个公司的就是一个租户。",
      line2:
        "注意：租户组织禁用后,租户下的用户无法登陆此租户。 租户管理员只允许有一个，注销后也可以通过平台恢复。",
      more: "了解更多",
    },
    userSelect: "人员选择",
    delTenant: "确定删除该租户？",
    enableStr: "确认启用该租户？",
    disableStr: "确认禁用用该租户？",
    setsuccess: "设置成功",
    setapp: "设置应用",
    addTenantSuccess: "新增租户成功",
    editTenantSuccess: "编辑租户成功",
    passwoedPlaceholder: "请输入密码",
    suggest: "建议",
    picsizeLimit: "图片大小不能超过20M",
    expand: "扩展设置",
    column: {
      shortName: {
        label: "系统名称",
        placeholder: "请输入系统名称",
        tips: "系统名称建议使用10字汉字，此名称将被作为登录页、页面标题、首页信息展示。",
      },
      name: {
        label: "公司全称",
        placeholder: "请输入公司全称",
      },
      adminUserName: {
        label: "超级管理员",
      },
      adminUserImg: {
        label: "管理员头像",
      },
      enable: {
        label: "状态",
        dicData: {
          true: "启用",
          false: "禁用",
        },
      },
      phone: {
        label: "手机",
        placeholder: "请填写系统管理员的手机号",
        tips: "手机号默认为管理员帐号",
      },
      addr: {
        label: "公司地址",
      },
      descMsg: {
        label: "公司简介",
      },
      adminUserAccount: {
        label: "管理员",
        placeholder: "请填写该租户管理员账号",
      },
      defaultPassword: {
        label: "用户初始密码",
        placeholder: "请填写用户初始密码",
      },
      defaultIndexUrl: {
        label: "默认首页",
        tips: "用户登录后，访问的第一个页面，默认情况为动态配置页面。可根据业务系统选择不一样的首页地址。具体填写请联系运维部署人员。",
      },
      loginTypes: {
        label: "登录类型",
        placeholder: "请选择登录类型",
        dicData: {
          password: "帐号密码",
          phone: "手机号",
          wx_qr: "微信二维码",
          app_qr: "APP二维码",
        },
      },
      secret: {
        tips1: "请填写微信的AppSecret",
        tips2: "详情帮助",
      },
      createTime: {
        label: "创建时间",
      },
      title: {
        label: "页面配置",
      },
      bgImg: {
        label: "背景图",
      },
    },
    leftTips: [
      {
        title: "1、拥有组织的独立数据存储",
        desc: "我们的组织拥有独立的数据存储系统，确保所有关键信息和资料的安全与私密性。",
      },
      {
        title: "2、定义自己的门户",
        desc: "组织拥有自定义的门户，充分展示组织的形象、文化和业务特色。",
      },
      {
        title: "3、更便捷的管理自己的组织",
        desc: "我们采用先进的管理系统，实现了对组织内部各项事务的便捷管理。",
      },
    ],
  },
  systemlogs: {
    column: {
      accountName: {
        label: "账号",
      },
      realName: {
        label: "姓名",
      },
      loginType: {
        label: "登录类型",
      },
      ip: {
        label: "IP",
      },
      clientName: {
        label: "终端",
      },
      userAgent: {
        label: "设备",
      },
      operateTime: {
        label: "时间",
      },
      searchTime: {
        label: "时间",
      },
      userId: {
        label: "用户ID",
      },
      status: {
        label: "状态",
        dicData: {
          true: "成功",
          false: "失败",
        },
      },
    },
  },
  appOperationLog: {
    column: {
      userName: {
        label: "姓名",
      },
      deptName: {
        label: "部门",
      },
      roles: {
        label: "角色",
      },
      account: {
        label: "账号",
      },
      jvsAppName: {
        label: "应用名",
      },
      modelName: {
        label: "模型名",
      },
      type: {
        label: "操作类型",
      },
      buttonName: {
        label: "按钮名称",
      },
      createTime: {
        label: "时间",
      },
    },
  },
  errlogs: {
    all: "全部",
    err: "待处理",
    view: "详细信息",
    deal: "处理",
    column: {
      businessName: {
        label: "服务名",
      },
      functionName: {
        label: "功能名",
      },
      operationType: {
        label: "操作类型",
      },
      userName: {
        label: "帐户",
      },
      userId: {
        label: "用户ID",
      },
      methodName: {
        label: "方法名",
      },
      api: {
        label: "API地址",
      },
      status: {
        label: "状态",
        dicData: {
          true: "成功",
          false: "失败",
        },
      },
      clientName: {
        label: "终端",
      },
      ip: {
        label: "请求ip地址",
      },
      tid: {
        label: "TID",
      },
      createDate: {
        label: "错误时间",
      },
      dateRange: {
        label: "日期时间范围",
      },
      startTime: {
        label: "开始时间",
      },
      endTime: {
        label: "endTime",
      },
      className: {
        label: "类名",
      },
      env: {
        label: "环境参数",
      },
      consumingTime: {
        label: "耗时",
      },
      parameters: {
        label: "请求json",
      },
      returnObj: {
        label: "返回json",
      },
      exceptionMessage: {
        label: "错误信息",
      },
      handleUser: {
        label: "处理人",
      },
      elements: {
        label: "详细信息",
      },
    },
  },
  opinion: {
    column: {
      createByHeadImg: {
        label: "头像",
      },
      createBy: {
        label: "用户",
      },
      createTime: {
        label: "创建时间",
      },
      text: {
        label: "内容",
      },
      img: {
        label: "图片",
      },
    },
  },
  application: {
    desc: "开发人员通过源代码开发将终端应用集成到平台中。",
    suggest: "建议",
    picsizeLimit: "图片大小不能超过20M",
    addSuccess: "新增终端成功",
    editSuccess: "修改终端成功",
    delDitSuccess: "删除字典成功",
    enableStr: "确定启用此终端？",
    disableStr: "确定禁用此终端？",
    enableSuccess: "启用成功",
    disableSuccess: "禁用成功",
    column: {
      name: {
        label: "终端名称",
        placeholder: "请输入终端名称",
      },
      describes: {
        label: "描述",
        placeholder: "请输入描述",
      },
      appKey: {
        label: "appId",
        placeholder: "请输入appId",
      },
      appSecret: {
        label: "app_secret",
      },
    },
  },
  dictionaries: {
    dicItem: "字典项",
    column: {
      systemType: {
        label: "字典类型",
        placeholder: "请选择或输入字典类型",
        dicData: {
          SYSTEM: "系统内置",
          SYSTEM_tip: "可控制数据表的状态，如待支付、支付、已发货等",
          BIZ: "业务类型",
          BIZ_tip: "业务服务中完全自定义，可能会与它类型重复",
        },
      },
      type: {
        label: "类型",
        placeholder: "请输入类型",
        tip: "类型，建议使用英文名",
      },
      description: {
        label: "描述",
        placeholder: "请输入描述",
      },
      remarks: {
        label: "备注",
      },
      createTime: {
        label: "创建时间",
      },
      createBy: {
        label: "创建人",
      },
    },
  },
  appicationhelp: {
    addHelp: "新增帮助",
    column: {
      label: {
        label: "标识",
        placeholder: "请输入标识",
      },
      content: {
        label: "内容",
        placeholder: "请输入内容",
      },
      name: {
        label: "名称",
        placeholder: "请输入名称",
      },
      type: {
        label: "类型",
        placeholder: "请输入类型",
      },
    },
  },
  devconfig: {
    buttonText: "更新",
    opsuccess: "刷新成功",
    column: {
      createHeader: {
        title: "创建用户是否生成头像",
        desc: "当新用户创建时，系统可以自动为用户生成一个默认的头像，也可以允许用户自定义上传头像。此功能为用户提供了一个个性化的界面体验，同时也有助于提升用户识别度。",
      },
      isDev: {
        title: "是否是开发模式",
        desc: "开发模式是一种特殊的运行模式，主要用于系统的开发和调试。在开发模式下，系统会提供完善的日志输出和错误提示，以帮助开发人员快速定位和解决问题。同时，开发模式可能会关闭某些性能优化和缓存机制。",
      },
      ding: {
        title: "配置钉钉的异常消息通知",
        desc: "当系统发生异常或错误时，通过钉钉发送消息通知是一种及时、有效的提醒方式。配置钉钉的异常消息通知涉及设置钉钉机器人的Webhook地址、密钥等，以便系统能够将异常信息发送到指定的钉钉群或用户。这有助于运维人员快速响应和处理系统问题，提高系统的稳定性和可用性。",
      },
      mybatis: {
        title: "启用数据查询缓存优化（mybatis plus 二级缓存）",
        desc: "为了提高数据查询的性能和响应速度，系统可以配置缓存机制来缓存查询结果。mybatis plus 是一个流行的MyBatis增强工具，它支持二级缓存功能。启用并配置二级缓存后，这可以大大减少数据库访问次数，提高系统的吞吐量和响应速度。只在多台服务时有明显效果。",
      },
    },
  },
  gateway: {
    column: {
      id: {
        label: "id",
        placeholder: "请输入id",
      },
      ip: {
        label: "ip",
      },
      path: {
        label: "地址",
        placeholder: "请输入path",
      },
      remark: {
        label: "备注",
        placeholder: "请输入备注",
      },
      uri: {
        label: "转发",
        placeholder: "请输入转发",
      },
      code: {
        label: "code",
      },
      msg: {
        label: "错误信息",
      },
      matchingMethod: {
        label: "匹配方式",
      },
    },
  },
  group: {
    addGroup: "添加团队",
    editGroup: "修改团队",
    remove: "移出",
    addMember: "添加成员",
    desc: "表单，工作流、话权限等功能，可以通过团队团名称进行过滤和设置。",
    userSelect: "人员选择",
    removeConfirm: "将删除此团队，是否继续？",
    addGroupSuccess: "添加团队成功",
    editGroupSuccess: "修改团队成功",
    delGroupSuccess: "删除团队成功",
    removeUserConfirm: "将移出该用户团队，是否继续？",
    delUserSuccess: "移出成功",
    addUserSuccess: "添加成员成功",
    column: {
      name: {
        label: "团队名称",
        placeholder: "名称不能为空",
      },
      principal: {
        label: "团队负责人",
      },
      headImg: {
        label: "头像",
      },
      realName: {
        label: "姓名",
        placeholder: "请输入用户名",
      },
      phone: {
        label: "手机号",
        placeholder: "请输入手机号",
      },
      roleName: {
        label: "角色",
      },
      deptName: {
        label: "部门",
      },
      jobName: {
        label: "岗位",
      },
    },
  },
  role: {
    member: "成员",
    role: "角色",
    datapermision: "数据权限",
    permisionSet: "权限配置",
    permisionserve: "资源维护",
    desc1: "相关资源权限，目前包括[平台管理]和[运维管理]",
    desc2:
      "权限资源分为租户级资源和平台级资，平台级资源:登陆的用户是顶级租户时，则认为此用户是平台级用户，平台级管理员授权此用户权限，将包含[运维管理]部分，租户级资源将不包含[运维管理]部门如何开发",
    group: "分组",
    type: "分类",
    funcpermision: "功能权限",
    all: "所有人",
    deptName: "部门名称",
    belowDept: "当前部门及以下",
    addRole: "新增角色",
    addGroup: "新增分组",
    setRange: "设置范围",
    removeDept: "移出部门",
    removeUser: "移出用户",
    addMember: "添加成员",
    addMemberSuccess: "添加成员成功",
    editMemberSuccess: "修改成员成功",
    editRole: "修改角色",
    delRole: "删除角色",
    userSelect: "成员选择",
    manageRange: "管理范围",
    selectDept: "选择部门",
    deptSelect: "部门选择",
    editGroup: "修改分组",
    delGroup: "删除分组",
    delRoleConfirm: "将删除此角色，是否继续？",
    delGroupConfirm: "将删除此分组，是否继续？",
    removeUserConfirm: "移除后，用户重新登陆即可生效",
    below: "及以下",
    roleDesc1:
      "开启[加入组织后是否自动赋予该权限]后，创建用户时，或邀请用户进入组织时，将自动赋予此角色权限，可提前赋予权限",
    roleDesc2: "建议在权限赋予操作时，尽量选择角色赋予，方便后续人员变更",
    roleAdd: "添加系统角色",
    roleAddSuccess: "添加角色成功",
    roleEdit: "修改系统角色",
    roleEditSuccess: "修改角色成功",
    perAdd: "添加资源",
    perEdit: "修改资源",
    perDelConfirm: "确定删除此资源？",
    dataDesc: "如果不勾选数据权限功能，默认查询所有数据",
    column: {
      realName: {
        label: "姓名",
      },
      phone: {
        label: "手机号",
      },
      jobName: {
        label: "岗位",
      },
      deptName: {
        label: "部门",
      },
      deptRange: {
        label: "管理范围",
      },
      name: {
        label: "部门名称",
      },
      below: {
        label: "当前部门及以下",
      },
      roleName: {
        label: "角色名称",
        placeholder: "请输入角色名称",
        error: "名称重复",
        rule: "角色名称不能为空",
      },
      roleDesc: {
        label: "角色描述",
        placeholder: "请输入角色描述",
        rule: "角色描述不能为空",
      },
      memberScope: {
        label: "成员范围",
        rule: "成员范围不能为空",
        dicData: {
          USER: "人员",
          DEPT: "部门",
          ALL: "所有人",
        },
      },
      autoGrant: {
        label: "加入组织后是否自动赋予该权限",
      },
      roleGroupId: {
        label: "角色分组",
        placeholder: "请选择角色分组",
      },
    },
    perColumn: {
      clientName: {
        label: "分组",
      },
      groupName: {
        label: "分类",
      },
      type: {
        label: "资源类型",
        dicData: {
          tenant: "系统资源",
          platform: "平台资源",
        },
      },
      name: {
        label: "资源名称",
      },
      permission: {
        label: "资源标识",
      },
      api: {
        label: "接口地址",
      },
    },
    dataColumn: {
      deptIds: "请选择部门",
      jobIds: "请选择岗位",
      checkList: "请选择",
      success: "授权成功",
    },
    groupColumn: {
      name: {
        label: "分组名称",
        placeholder: "请输入分组名称",
      },
    },
  },
  department: {
    invite: "邀请加入",
    inviteTip: "可通过下列任意方式邀请员工加入组织",
    inviteCode: "邀请码",
    inviteDesc: "员工可通过输入邀请码申请加入组织",
    inviteCopy: "复制邀请码",
    inviteTime: "当前邀请有效期30分钟有效",
    inviteRep: "重新生成",
    inviteInfo: "需要管理员审核",
    userRepeat: "用户名重复",
    role: "角色",
    userNameValidate1: "用户名不能超过64位字符",
    userNameValidate2:
      "用户名只能包含：中文、英文、数字和下划线，且不能以下划线开头结尾",
    importUser: "导入用户",
    男: "男",
    女: "女",
    date: "选择日期",
    editPass: "修改密码",
    phoneErr: "手机号码格式错误！",
    deptName: "部门名称",
    deptLead: "部门负责人",
    deptAdd: "添加部门",
    deptEdit: "修改部门",
    deptAddChild: "添加子部门",
    offName: "公司名称",
    offLead: "公司负责人",
    offAdd: "添加公司",
    offEdit: "修改公司",
    userAddSuccess: "新增用户成功",
    userEditSuccess: "编辑用户成功",
    userDelSuccess: "删除用户成功",
    userDelConfirm: "确定删除该用户？",
    userDelConfirmFlag: "删除后，用户无法登录当前系统。确定删除该用户？",
    removeConfirm: "是否删除此数据，是否继续？",
    removeOrgSuccess: "删除机构成功",
    passEditSuccess: "修改密码成功",
    above: "同上",
    fileLimit: "上传文件的格式只能是 xls、xlsx格式，且文件大小不能超过 10MB！",
    fileFormat: "文件格式错误，仅支持上传xls、xlsx格式文件!",
    fileSize: "上传的文件大小不能超过 10MB!",
    uploadDialog: {
      desc: "点击或者拖动文件到虚线框内上传",
      info: "支持xls，xlsx等类型的文件",
      tip: "上传的Excel表符合以下规范：",
      tipli1: "文件大小不超过",
      tipli1_1: "10M",
      tipli1_2: "，且单个sheet页数据量不超过5000行",
      tipli2: "仅支持",
      tipli2_1: "（*.xls和*.xlsx）",
      tipli2_2: "文件",
      tipli3: "请确保您需要导入的sheet表头中",
      tipli3_1: "不包含空的单元格",
      tipli3_2: "，否则该sheet页数据系统将不做导入",
      tipli4: "批量导入的数据不支持“内置变量”作为条件的过滤",
      tipli5: "导入文件",
      tipli5_1: "不支持Excel公式计算",
      tipli5_2: "，如SUM，=H2*J2等",
    },
    topButton: {
      invite: "邀请用户",
      addUser: "添加用户",
      addDept: "添加部门",
      addOff: "添加公司",
      importUser: "导入用户",
      exportTmp: "导出模板",
      sys: "同步",
      org: "组织",
    },
    lineButton: {
      dataPer: "数据权限",
      editPass: "修改密码",
    },
    leftButton: {
      edit: "修改部门",
      addChild: "添加子部门",
      editOff: "修改公司",
      addOff: "添加公司",
      addMember: "添加成员",
    },
    userColumn: {
      headImg: {
        label: "头像",
      },
      realName: {
        label: "姓名",
        placeholder: "请输入姓名",
      },
      accountName: {
        label: "账号",
        placeholder: "请输入账号",
      },
      sex: {
        label: "性别",
        dicData: {
          male: "男",
          female: "女",
          unknown: "保密",
        },
      },
      phone: {
        label: "手机号",
        placeholder: "请输入手机号",
      },
      deptId: {
        label: "部门",
      },
      deptName: {
        label: "部门",
      },
      email: {
        label: "邮箱",
        placeholder: "请输入邮箱",
        rule: "请输入正确的邮箱地址",
      },
      jobId: {
        label: "岗位",
        placeholder: "请输入或选择岗位",
      },
      jobName: {
        label: "岗位",
      },
      levelId: {
        label: "账号等级",
      },
      employeeNo: {
        label: "职工编号",
      },
      createTime: {
        label: "创建时间",
      },
      lockFlag: {
        label: "状态",
        tip: "用户锁定后，将无法登录",
        dicData: {
          false: "未锁定",
          true: "锁定",
        },
        activetext: "锁定",
        inactivetext: "未锁定",
      },
      birthday: {
        label: "生日",
      },
      hireDate: {
        label: "入职日期",
      },
      roleNames: {
        label: "角色",
      },
      roleIds: {
        label: "角色",
      },
    },
    importColumn: {
      accountName: {
        label: "用户名",
        placeholder: "请输入用户名",
      },
      realName: {
        label: "姓名",
        placeholder: "请输入真名",
      },
      nickName: {
        label: "昵称",
      },
      sex: {
        label: "性别",
        dicData: {
          男: "男",
          女: "女",
        },
      },
      email: {
        label: "邮件",
        placeholder: "请输入正确的邮箱地址",
      },
      phone: {
        label: "手机号",
        placeholder: "请输入手机号",
      },
      roleId: {
        label: "角色",
      },
      jobId: {
        label: "岗位",
        placeholder: "请输入或选择岗位",
      },
      employeeNo: {
        label: "职工编号",
      },
      birthday: {
        label: "生日",
      },
      hireDate: {
        label: "入职日期",
      },
      status: {
        label: "锁定",
        dicData: {
          false: "未锁定",
          true: "锁定",
        },
        activetext: "锁定",
        inactivetext: "未锁定",
      },
    },
    deptColumn: {
      parentId: {
        label: "上级部门",
        placeholder: "部门名称不能为空",
      },
      name: {
        label: "部门名称",
        placeholder: "部门不能为空",
      },
      sort: {
        label: "排序",
        placeholder: "排序不能为空",
      },
      leaderId: {
        label: "部门负责人",
      },
      deptCode: {
        label: "部门代码",
      },
    },
    passColumn: {
      password: {
        label: "密码",
        placeholder: "请输入密码",
        rule: "密码不能超过15位",
      },
      rePassword: {
        label: "确认密码",
        placeholder: "请输入密码",
        rule: "两次密码不一致",
      },
    },
  },
  postList: {
    postAdd: "添加岗位",
    postEdit: "修改岗位",
    userPosEdit: "修改用户岗位",
    memberAdd: "添加成员",
    dataDesc: "默认只查询同级部门数据",
    deptIds: "请选择部门",
    jobIds: "请选择岗位",
    userSelect: "人员选择",
    delPosConfirm: "将删除此岗位，是否继续？",
    delPosSuccess: "删除岗位成功",
    addPosSuccess: "添加岗位成功",
    editPosSuccess: "修改岗位成功",
    delUserPosConfirm: "将移出该用户岗位，是否继续？",
    addMemberSuccess: "添加成员成功",
    column: {
      realName: {
        label: "姓名",
        placeholder: "请输入姓名",
      },
      phone: {
        label: "手机号",
        placeholder: "请输入手机号",
      },
      roleName: {
        label: "角色",
      },
      deptName: {
        label: "部门",
      },
      jobName: {
        label: "岗位",
      },
      name: {
        label: "岗位名称",
        placeholder: "名称不能为空",
      },
      dataScopeType: {
        label: "数据权限",
      },
      postId: {
        label: "岗位",
        tip: "修改岗位后数据权限将改变",
      },
    },
  },
  dataPermision: {
    column: {
      name: {
        label: "标签名",
      },
      annotation: {
        label: "描述",
      },
      createBy: {
        label: "创建人",
      },
      authority: {
        label: "权限",
      },
      roleIds: {
        label: "角色",
      },
      post: {
        label: "岗位",
      },
      dept: {
        label: "部门数据权限",
        dicData: {
          all: "所有部门",
          oneself: "当前部门",
          subordinate: "当前部门及以下",
          custom: "自定义权限",
        },
      },
      custom: {
        label: "自定义权限",
      },
      createTime: {
        label: "创建时间",
      },
      label: {
        label: "字段(表名.字段名)",
      },
      value: {
        label: "值(使用逗号分割)",
      },
    },
  },
  bulletin: {
    mess: "消息公告",
    wx: "小程序公告",
    addMess: "新增消息公告",
    addWx: "新增小程序公告",
    selectImg: "选择图片",
    img: "图片",
    text: "文本",
    isLt10M: "上传图片大小不能超过 10MB!",
    isImg: "只能上传图片！",
    deploy: "已发布",
    unload: "未发布",
    column: {
      title: {
        label: "标题",
        placeholder: "请输入标题",
      },
      startTime: {
        label: "生效时间",
        placeholder: "请选择生效时间",
      },
      endTime: {
        label: "失效时间",
        placeholder: "请选择失效时间",
      },
      type: {
        label: "类型",
      },
      appKeys: {
        label: "终端选择",
        tableLabel: "终端",
        placeholder: "请选择终端",
      },
      contentType: {
        label: "公告类型",
        placeholder: "请选择公告类型",
      },
      userIds: {
        label: "指定人员",
      },
      banner: {
        label: "Banner",
        placeholder: "Banner不能为空",
      },
      content: {
        label: "内容",
        placeholder: "内容不能为空",
      },
      publish: {
        label: "状态",
      },
      createTime: {
        label: "创建时间",
      },
    },
  },
  message: {
    sms: "短信消息",
    smsAdd: "新增短信消息",
    email: "邮件消息",
    emailAdd: "新增邮件消息",
    interior: "站内信",
    interiorAdd: "新增站内信",
    WECHAT_MP_MESSAGE: "公众号消息",
    WECHAT_MP_MESSAGEAdd: "新增公众号消息",
    WX_ENTERPRISE: "企业微信消息",
    WX_ENTERPRISEAdd: "新增企业微信消息",
    DING_H5: "钉钉消息",
    DING_H5Add: "新增钉钉消息",
    resend: "重发",
    send: "发送",
    notSend: "未发送",
    sending: "发送中",
    saveSend: "保存并发送",
    sendimgMess: "消息发送中",
    resendMess: "消息重新发送中",
    delConfirm: "确认删除此消息吗？",
    column: {
      title: {
        label: "标题",
        placeholder: "请输入标题",
      },
      status: {
        label: "发送状态",
        dicData: {
          0: "失败",
          1: "成功",
          2: "未发送",
          3: "发送中",
        },
      },
      createTime: {
        label: "创建时间",
      },
      sendType: {
        label: "发送类型",
        placeholder: "请选择发送类型",
        dicData: {
          email: "邮箱",
          sms: "短信",
          interior: "站内信",
          WECHAT_MP_MESSAGE: "公众号",
          WX_ENTERPRISE: "企业微信",
          DING_H5: "钉钉",
        },
      },
      sendMessageType: {
        label: "消息类型",
        placeholder: "请选择消息类型",
        dicData: {
          broadcast: "广播消息",
          warning: "警告消息",
          notification: "通知消息",
          system: "系统消息",
          business: "业务消息",
        },
      },
      recipients: {
        label: "收件人",
        placeholder: "请选择收件人",
      },
      content: {
        label: "内容",
        placeholder: "请输入内容",
      },
      smsContent: {
        placeholder: "请输入短信内容",
      },
      emailContent: {
        label: "邮件内容",
        placeholder: "请输入邮件内容",
      },
      interContent: {
        label: "站内信内容",
        placeholder: "请输入站内信内容",
      },
      messContent: {
        label: "消息内容",
        placeholder: "请输入消息内容",
      },
    },
    smsColumn: {
      personnel: {
        label: "收件人",
        placeholder: "请选择收件人",
      },
      templateId: {
        label: "选择模板",
        placeholder: "请选择模板",
      },
      templateContent: {
        label: "详细内容",
      },
      paramName: {
        label: "参数名",
        placeholder: "请输入",
      },
    },
  },
  userLevel: {
    sort: {
      label: "排序",
    },
    name: {
      label: "等级名称",
    },
    indexUrl: {
      label: "首页地址",
    },
  },
  userdel: {
    recover: "恢复",
    recoverSuccess: "恢复用户成功",
    del: "彻底删除",
    delSuccess: "彻底删除用户成功",
    comfirm1: "此操作无法撤销，请确认如下影响：",
    comfirm2: "OA流程数据，系统异常",
    comfirm3: "业务数据，系统异常",
    comfirm4: "若您只想限制其访问操作能力，建议不要彻底删除成员。",
  },
  onlineUser: {
    endSession: "结束会话",
    outConfirm: "是否强制退出该用户？",
    outSuccess: "该用户成功退出",
    column: {
      headImg: {
        label: "头像",
      },
      realName: {
        label: "姓名",
      },
      email: {
        label: "邮箱",
      },
      clientName: {
        label: "终端名称",
      },
      expiresAt: {
        label: "有效时间",
      },
      phone: {
        label: "手机号",
      },
      ip: {
        label: "IP",
      },
      userAgent: {
        label: "设备信息",
      },
      accountName: {
        label: "账号",
      },
      loginType: {
        label: "登录类型",
      },
      jobName: {
        label: "岗位",
      },
      deptName: {
        label: "部门",
      },
      employeeNo: {
        label: "职工编号",
      },
      expiresAtEnd: {
        label: "过期时间",
      },
    },
  },
  file: {
    upload: "上传文件",
    del: "删除文件",
    copy: "复制链接",
    fileUp: "文件上传",
    fileUpTip1: "点击上传、或将文件拖到此处",
    fileUpTip2: "最大支持20M的文件",
    column: {
      bucketName: {
        label: "桶名",
        placeholder: "请选择桶名",
      },
      label: {
        label: "标签",
      },
      fileType: {
        label: "文件类型",
      },
      fileName: {
        label: "文件名",
      },
      filePath: {
        label: "文件路径",
      },
      fileSize: {
        label: "大小",
      },
      createTime: {
        label: "创建时间",
      },
    },
  },
  wxmessagetemplate: {
    desc: "模板消息仅用于公众号向用户发送重要的服务通知，只能用于符合其要求的服务场景中，如信用卡刷卡通知，商品购买成功通知等。不支持广告等营销类消息以及其它所有可能对用户造成骚扰的消息。",
    sysBtn: "同步消息模板",
    emptyDesc: "未完善公众号配置信息，无法配置公众号消息模板",
    column: {
      title: {
        label: "模板标题",
      },
      primaryIndustry: {
        label: "一级行业",
      },
      deputyIndustry: {
        label: "二级行业",
      },
      content: {
        label: "模板内容",
      },
      example: {
        label: "模板示例",
      },
    },
  },
  shortmessagetemplate: {
    desc: "短信服务目前只支持使用阿里云短信，国内短信秒级可达，99%到达率。国内短信与工信部携号转网平台实时互联。",
    sysBtn: "同步消息模板",
    setBind: "设置为绑定模板",
    can: "可用",
    no: "不可用",
    yes: "是",
    column: {
      templateName: {
        label: "模板名称",
      },
      auditStatus: {
        label: "状态",
      },
      loginCode: {
        label: "验证码模板",
      },
      orderId: {
        label: "工单ID",
      },
      templateCode: {
        label: "短信模板CODE",
      },
      templateContent: {
        label: "模板内容",
      },
      reason: {
        label: "审核备注",
      },
      createDate: {
        label: "创建时间",
      },
    },
  },
  envariable: {
    desc: "环境变量在本平台的使用方式",
    column: {
      type: {
        label: "类型",
        dicData: {
          text: "文本环境变量",
          file: "文件环境变量",
          key: "键值对环境变量",
        },
      },
      label: {
        label: "键",
        placeholder: "请输入键",
        regularMessage: "只能输入中文和英文",
      },
      value: {
        label: "值",
        placeholder: "值不能为空",
      },
      remark: {
        label: "说明",
      },
    },
    leftTips: [
      {
        title: "文本环境变量",
        desc: "文本环境变量主要用于在公式、逻辑、工作流、表单、列表等设计器中动态插入或引用特定的文本信息。它们允许用户根据实际需求定义和配置文本值，使得在设计和运行时，设计器可以自动替换这些变量为实际的文本内容。通过这种方式，用户可以轻松地实现文本的个性化、动态化和可配置化，提高设计的灵活性和可维护性。",
      },
      {
        title: "文件环境变量",
        desc: `文件环境变量主要用于与文件相关的操作，如打印、导出模板等。这些变量通常用于指定文件的路径、名称、格式等属性，以便在运行时正确地定位和处理文件。通过使用文件环境变量，用户可以实现文件路径的动态化和可配置化，减少硬编码带来的维护成本，并提高系统的可移植性和可扩展性。`,
      },
      {
        title: "键值对环境变量",
        desc: "键值对环境变量主要用于逻辑节点配置中，用于存储和引用键值对形式的配置信息。它们通常以“键=值”的形式存在，其中键用于标识特定的配置项，值则是对应的配置值。通过使用键值对环境变量，用户可以实现配置的灵活性和可定制性，方便地对逻辑节点的行为进行细粒度的控制。同时，由于配置信息存储在环境变量中，也便于进行统一的管理和维护。",
      },
    ],
  },
  trilateralconfig: {
    title: "三方应用登录对接",
    chooseIcon: "选择图标",
    equal: "等于",
    test: "模拟测试",
    addRow: "添加一行",
    fieldName: "字段名",
    chinese: "显示中文名",
    column: {
      name: {
        label: "平台名称",
        placeholder: "请输入平台名称",
      },
      enableDefault: {
        label: "默认登录",
      },
      type: {
        label: "类型",
        placeholder: "请选择或输入类型",
      },
      authorize: {
        label: "授权的api",
        placeholder: "请输入授权的api",
      },
      accessToken: {
        label: "获取token的api",
        placeholder: "请输入获取token的api",
      },
      userInfo: {
        label: "获取用户信息的api",
        placeholder: "请输入获取用户信息的api",
      },
      clientId: {
        label: "clientid",
        placeholder: "请输入clientid",
      },
      clientSecret: {
        label: "secret",
        placeholder: "请输入secret",
      },
      redirectUri: {
        label: "回调地址",
        placeholder: "请输入回调地址",
      },
      scope: {
        label: "权限范围",
        placeholder: "请输入权限范围",
      },
      loginIndex: {
        label: "登陆路由页",
        placeholder: "请输入登陆路由页",
      },
      filedJson: {
        label: "用户字段映射",
      },
      pullUserUrlMethod: {
        label: "用户同步接口地址api(get)",
        placeholder:
          "输入用户接口地址，用于手动同步并更新用户管理列表中用户信息",
      },
      appKey: {
        label: "客户端",
        placeholder: "请选择客户端",
      },
      iconUrl: {
        label: "平台图标",
      },
      deptUrl: {
        label: "组织同步的api",
        placeholder: "组织同步接口地址",
      },
      deptSyncList: {
        label: "组织同步映射关系",
      },
      extensionJson: {
        label: "扩展字段",
      },
      logoutUri: {
        label: "退出地址",
        placeholder: "系统点击退出时，自动调用三方系统进行退出",
      },
      parameterType: {
        label: "获取token参数传递",
        placeholder: "请选择获取token参数传递",
      },
      urlType: {
        label: "获取token方法",
        placeholder: "请选择获取token方法",
      },
    },
    mappingColumn: {
      uuid: "uuid",
      username: "用户名称",
      nickname: "用户昵称",
      avatar: "用户头像",
      blog: "用户网址",
      company: "所在公司",
      location: "位置",
      email: "用户邮箱",
      remark: "用户备注",
      phone: "手机号",
      enabled: "状态",
      account: "账号",
      code: "响应码",
      data: "数据字段",
      deptId: "部门ID",
      deptName: "部门名称",
      deptCode: "部门编号",
      parentId: "上级部门",
      sort: "排序",
    },
  },
  systeminfo: {
    basic: "基本信息",
    orgName: "组织域名：",
    comName: "公司名称：",
    orgAdmin: "组织管理员：",
    entNumber: "企业人数",
    teamNumber: "团队数",
    deptNumber: "部门数",
    userNumber: "昨日使用人数",
    capacity: "资源容量",
    manage: "应用管理",
    seven: "近7天使用人数趋势",
  },
  orglogout: {
    cancel: "注销组织",
    cancelTip1:
      "解散后，你的后台管理账号和通讯录将会被删除；（如有需要，请先导出数据）",
    cancelTip2:
      "该操作不可撤销，请谨慎操作。同时删除的包括云盘文件，应用数据（基础平台，知识库，任务，项目，视频会议等），请做好相应的备份。",
    diss: "解散组织",
    trans: "转移组织",
    transTip1: "将组织转移给企业其他成员，转移后该成员将拥有企业的最高权限。",
    transTip2:
      "1.不能转移给自己；2.被转移的成员必须绑定手机号；3.转移后，自己将变为普通用户。",
    confirm: "确认解散组织？",
    transConfirm: "确认转移组织？",
    transSuccess: "转移成功",
  },
  settings: {
    tip1: "通过编辑内容或关键词规则，快速进行自动回复设置。如具备开发能力，可更灵活地使用该功能。配置自动回复之后，将立即对所有用户生效。",
    tip2: "查看文档",
    tip3: "有新粉丝关注时，会自动发送信息",
    follow: "关注回复",
    keyword: "关键词回复",
    warning: "未完善公众号配置信息，无法配置公众号消息",
    rightnow: "立即配置",
    jump: "点击跳转",
    column: {
      key: {
        label: "关键字",
        placeholder: "请输入关键字",
      },
      description: {
        label: "图文消息描述",
        placeholder: "请输入图文消息描述",
      },
      title: {
        label: "图文消息标题",
        placeholder: "请输入图文消息标题",
      },
      subscribeUrl: {
        label: "图文消息图片",
      },
      url: {
        label: "跳转链接",
      },
      welcomeText: {
        label: "欢迎语",
        placeholder: "请输入欢迎语",
      },
      keywordText: {
        label: "关键字信息",
        placeholder: "请输入关键字信息",
      },
      fileList: {
        label: "图片",
      },
    },
  },
  systemconfig: {
    notConfig: "未配置",
    chooseImg: "选择图片",
    tutorial: "设置教程",
    getMPInfo: "获取公众号开发信息",
    auth: "授权配置",
    last: "上一条：",
    next: "下一条：",
    dot: "、",
    editMess: "编辑消息通知",
    setMess: "配置消息通知",
    pendingApproval: "待审批人",
    success: "成功",
    config: {
      WORKFLOW_TODO_REMINDER: {
        title: "工作流待办提醒",
        desc: "设置待办事项的消息通知模板，包含发送方式和对应方式所需要的消息内容。",
      },
      WORKFLOW_REMINDER: {
        title: "催办提醒",
        desc: "设置催办提醒的消息通知模板，包含发送方式和对应方式所需要的消息内容。",
      },
      SMS_CONFIGURATION: {
        title: "短信配置",
        desc: "短信配置后，可发送短信消息，也支持手机号密码登录。",
        tips: [
          "短信配置后可实现手机验证码登陆,绑定手机号.集成&自动化短信组件,任务管理,企业文档,短信信息通知等功能",
          '请在<a href="https://www.aliyun.com/product/sms" target="blank" style="color: #3471FF;">阿里云短信服务</a>获取以下配置信息',
          '签名: 必须与国内文档短信中"签名名称"相同,如果不相同将无法发送成功',
          "在阿里云短信服务[国内消息]->[模板管理]中添加模板后.再(短信消息模板'跳短信消息路由')中同步消息模板即可",
          "阿里云短信模板审核时长一般2小时内完成，近期平均完成审核时长约1小时，如遇升级核验、审核任务较多、非工作时间，审核时间可能会延长，请耐心等待",
          "审核工作时间：周一至周日9:00-21:00（法定节假日顺延）",
        ],
      },
      MAIL_CONFIGURATION: {
        title: "邮件配置",
        desc: "邮件配置后，可使用逻辑组件灵活配置发送邮件内容，或发送用户邀请邮件。",
        tips: [
          "开启QQ邮箱的协议：登录QQ邮箱 -- 设置 -- 账户 -- POP3/IMAP/SMTP/Exchange/CardDAV/CalDAV服务，开启服务：POP3/SMTP服务",
          "生成授权码",
        ],
      },
      NAIL_APPLICATION_CONFIGURATION: {
        title: "钉钉H5应用",
        desc: "配置接入钉钉H5应用或钉钉扫码登录",
        tips: [
          "登录钉钉开发平台（开发者后台），依次进入：“应用开发”/“企业内部开发”/“创建应用”，填写“应用名称”、“应用描述”等关键信息，创建“H5微应用”或“小程序”。",
          "完成钉钉应用添加后，即返回钉钉应用列表，双击对于的钉钉应用后，即可获取钉钉应用的令牌信息（主要关注：AppKey、AppSecret）。",
          "钉钉开发平台，依次进入：“应用开发”/“企业内部开发”/“${钉钉应用}”/“权限管理”，之后从权限类别列表中选择“获取凭证”或者在搜索框填写“获取用户token”过滤，查找定位至“调用OpenApp专有API时需要具备的权限”，之后点击“申请权限”即可。",
          "钉钉开发平台，依次进入：“应用开发”/“企业内部开发”/“${钉钉应用}”/“钉钉登录与分享”/“接入登录”，填写回调域名，用于扫描登录后跳转回应用程序。",
        ],
      },
      ENTERPRISE_WECHAT_APPLICATION_CONFIGURATION: {
        title: "企业微信应用",
        desc: "配置企业微信扫码登录或企业微信组织结构信息同步",
      },
      WECHAT_OFFICIAL_ACCOUNT_CONFIGURATION: {
        title: "微信公众号",
        desc: "配置公众号平台用于消息接收发送；公众号扫码登录",
        tips: [
          "首先要申请微信公众号（服务号并且要认证300元/年）",
          "登录微信公众号后台->开发->基本设置,获取公众号开发信息",
          "微信授权配置，进入微信后台->设置->公众号设置->功能设置",
          "开启消息代理，微信模版消息配置",
        ],
      },
      LDAP_CONFIGURATION: {
        title: "LDAP",
        desc: "配置LDAP服务器相关信息",
      },
      BACKGROUND_PERSONALIZED_CONFIGURATION: {
        title: "后台个性化配置",
        desc: "设置系统名称logo/icon登录界面",
        tips: [
          "自定义此租户组织的名称 ,多租户切换时和登陆用户浏览器的顶部将展示一下此名称",
          "用户初密码修改后,在添加用户时用户不需要再次设置密码,默认密码将为此处设置的密码.为保证用户安全此密码请勿设置简单",
          "系统不会提示用户必须强制修改密码,建议用户自行登陆后修改登陆密码",
        ],
      },
      PERSONALIZED_CONFIGURATION_IOT_PLATFORMS: {
        title: "物联网个性化配置",
        desc: "设置系统名称logo/icon登录界面",
      },
      ENTERPRISE_DOCUMENT_ENTERPRISE_CONFIGURATION: {
        title: "企业文档企业个性化配置",
        desc: "设置系统名称logo/icon登录界面",
      },
      PERSONALIZED_CONFIGURATION_OF_ENTERPRISE_PLANNING: {
        title: "企业计划个性化配置",
        desc: "设置系统名称logo/icon登录界面",
      },
      PERSONALIZED_CONFIGURATION_OF_EMAIL_SERVICE: {
        title: "邮件服务个性化配置",
        desc: "设置系统名称logo/icon登录界面",
      },
      RULE_PERSONALIZATION_CONFIGURATION: {
        title: "规则个性化配置",
        desc: "设置系统名称logo/icon登录界面",
      },
      PERSONALIZED_CONFIGURATION_OF_DATA_INTELLIGENCE_WAREHOUSE: {
        title: "数据智仓个性化配置",
        desc: "设置系统名称logo/icon登录界面",
      },
      PERSONALIZED_CONFIGURATION_FOR_VIDEO_CONFERENCING: {
        title: "企业会议个性化配置",
        desc: "设置系统名称logo/icon登录界面",
      },
      AUTOMATION_CONFIGURATION: {
        title: "逻辑引擎个性化配置",
        desc: "设置系统名称logo/icon登录界面",
      },
      CAS_CONFIGURATION: {
        title: "Cas个性化配置",
        desc: "设置系统名称logo/icon登录界面",
      },
      APS_CONFIGURATION: {
        title: "APS个性化配置",
        desc: "设置系统名称logo/icon登录界面",
        tips: [
          "自定义此租户组织的名称 ,多租户切换时和登陆用户浏览器的顶部将展示一下此名称",
          "用户初密码修改后,在添加用户时用户不需要再次设置密码,默认密码将为此处设置的密码.为保证用户安全此密码请勿设置简单",
          "系统不会提示用户必须强制修改密码,建议用户自行登陆后修改登陆密码",
        ],
      },
    },
    viewImageInfo: ["获取公众号开发信息说明", "授权配置说明"],
    basicOption: {
      defaultPassword: {
        label: "用户初始密码",
        placeholder: "请输入用户初始密码",
        tip: "创建的用户默认使用此密码进行登录，如果用户已经存在请使用邀请用户加入组织",
      },
      watermark: {
        label: "水印",
        tip: "默认将用户登录昵称做为水印显示",
      },
      skipLogin: {
        label: "仅通过三方登录",
        tip: "在三方登录对接开启默认登录的情况下，开启时本系统不再提供独立登录入口，只能通过三方系统登录；关闭时，可通过三方系统登录，也可通过本系统独立登录",
      },
      systemName: {
        label: "系统名称",
      },
      domainName: {
        label: "服务地址",
        tip: "例：http://www.xxx.com",
      },
      icon: {
        label: "ICON",
        tip: "建议 32 * 32",
      },
      logo: {
        label: "LOGO",
        tip: "建议 200 * 60",
      },
      bgImg: {
        label: "背景图",
        tip: "建议 1920 * 1080",
      },
    },
    smsOption: {
      accessKeyId: {
        label: "accessKeyId",
        placeholder: "请输入accessKeyId",
      },
      accessKeySecret: {
        label: "accessKeySecret",
        placeholder: "请输入accessKeySecret",
      },
      signature: {
        label: "签名（signature）",
        placeholder: "请输入signature",
      },
    },
    emailOption: {
      from: {
        label: "邮箱地址",
        placeholder: "请输入邮箱地址",
      },
      host: {
        label: "邮箱域名",
        placeholder: "请输入邮箱域名",
      },
      pass: {
        label: "密码",
        placeholder: "请输入密码",
      },
    },
    dingOption: {
      agentId: {
        label: "AgentId",
        placeholder: "请输入钉钉H5微应用的AgentId",
      },
      appId: {
        label: "appKey",
        placeholder: "请输入钉钉H5微应用的appKey",
      },
      appSecret: {
        label: "appSecret",
        placeholder: "请输入钉钉H5微应用的appSecret",
      },
      enableScan: {
        label: "开启扫码登录",
      },
      redirectUri: {
        label: "重定向地址",
        placeholder: "请输入重定向地址",
      },
    },
    wxOption: {
      appId: {
        label: "企业ID",
        placeholder: "请输入企业微信的corp_id（企业id）",
      },
      agentId: {
        label: "AgentId",
        placeholder: "请输入企业微信应用的AgentId",
      },
      appSecret: {
        label: "Secret",
        placeholder: "请输入企业微信应用的Secret",
      },
      enableScan: {
        label: "开启扫码登录",
      },
      redirectUri: {
        label: "重定向地址",
        placeholder: "请输入重定向地址",
      },
      baseApiUrl: {
        label: "企业微信地址",
        placeholder: "请输入企业微信地址",
      },
      openUrl: {
        label: "开放平台地址",
        placeholder: "请输入开放平台地址",
      },
      scanUrl: {
        label: "扫码登录地址",
        placeholder: "请输入扫码登录地址",
      },
    },
    wechatMpOption: {
      appKey: {
        label: "appKey",
        placeholder: "请输入appKey",
      },
      appSecret: {
        label: "appSecret",
        placeholder: "请输入appSecret",
      },
      token: {
        label: "token",
      },
      aesKey: {
        label: "EncodingAESKey",
      },
      enableScan: {
        label: "开启扫码登录",
      },
    },
    LDAPOption: {
      base: {
        label: "base",
        placeholder: "请输入base",
      },
      urls: {
        label: "urls",
        placeholder: "请输入urls",
        tip: "多个url以英文逗号,分隔",
      },
      username: {
        label: "用户名",
        placeholder: "请输入用户名",
      },
      password: {
        label: "密码",
        placeholder: "请输入密码",
      },
      accountAttribute: {
        label: "账号字段",
        tip: "为空则默认使用uid",
      },
    },
    CASOption: {
      cas_server_url_prefix: {
        label: "cas_server_url_prefix",
        placeholder: "请输入cas_server_url_prefix",
      },
      cas_server_url_login: {
        label: "cas_server_url_login",
        placeholder: "请输入cas_server_url_login",
      },
      client_host_url: {
        label: "client_host_url",
        placeholder: "请输入client_host_url",
      },
    },
  },
  proxylist: {
    add: "新增代理",
    leave: "离职代理",
    forever: "永久",
    revoke: "撤销",
    normal: "普通代理",
    rightNow: "本代理立即生效",
    addSuccess: "新增代理成功",
    confirm: "确定要撤销吗？",
    delSuccess: "撤销成功",
    leaveSuccess: "新增离职代理成功",
    column: {
      userName: {
        label: "被代理人",
        label_search: "用户名",
      },
      proxyUserName: {
        label: "代理人",
      },
      beginTime: {
        label: "代理时间",
      },
      status: {
        label: "状态",
        dicData: {
          1: "待生效",
          2: "代理中",
          3: "已过期",
          4: "已撤销",
        },
      },
      createBy: {
        label: "创建人",
      },
      createTime: {
        label: "创建时间",
      },
      description: {
        label: "说明",
      },
      userId: {
        label: "被代理人",
        placeholder: "请选择被代理人",
      },
      proxyUserId: {
        label: "代理人",
        placeholder: "请选择代理人",
      },
      proxyTime: {
        label: "代理时间",
        startplaceholder: "开始时间",
        endplaceholder: "结束时间",
        placeholder: "请选择代理时间",
      },
    },
    leaveColumn: {
      userId: {
        label: "离职人",
        placeholder: "请选择离职人",
      },
      proxyUserId: {
        label: "代理人",
        placeholder: "请选择代理人",
      },
    },
  },
  processmanage: {
    topButton: {
      exportExcel: "导出审批表",
      batchStop: "批量终止",
    },
    add: "增员",
    reduce: "减员",
    trans: "转交",
    stop: "终止",
    user: "审批人员",
    approvalProgress: "审批进度",
    reduceUser: "减少人员",
    transUser: "转交给",
    chooseUser: "请选择用户",
    column: {
      name: {
        label: "流程名称",
      },
      taskCode: {
        label: "流程编号",
      },
      jvsAppId: {
        label: "应用名称",
      },
      title: {
        label: "流程标题",
      },
      createDeptName: {
        label: "发起人单位名称",
      },
      taskStatus: {
        label: "审批状态",
        dicData: {
          1: "待审批",
          2: "已通过",
        },
      },
      currentNodeName: {
        label: "当前环节",
      },
      createBy: {
        label: "发起人",
      },
      createTime: {
        label: "创建时间",
      },
      reason: {
        label: "终止原因",
      },
    },
  },
  loginIP: {
    column: {
      ip: {
        label: "ip地址",
      },
      time: {
        label: "时间段规则",
      },
      status: {
        label: "状态",
      },
    },
  },
};
