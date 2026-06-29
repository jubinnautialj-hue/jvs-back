export const basicOption = {
  formAlign: 'top',
  column: [
    {
      label: '水印',
      prop: 'watermark',
      type: 'switch',
      tips: {
        position: 'bottom',
        text: '默认将用户登录昵称做为水印显示'
      },
    },
    {
      label: '仅通过三方登录',
      prop: 'skipLogin',
      type: 'switch',
      tips: {
        position: 'right',
        text: '在三方登录对接开启默认登录的情况下，开启时本系统不再提供独立登录入口，只能通过三方系统登录；关闭时，可通过三方系统登录，也可通过本系统独立登录'
      },
    },
    {
      label: '系统名称',
      prop: 'systemName'
    },
    {
      label: '服务地址',
      tips: {
        text: '例：http://www.xxx.com',
        position: 'bottom'
      },
      prop: 'domainName',
      prepend: 'http://',
    },
    {
      label: 'ICON',
      prop: 'icon',
      type: 'imageUpload',
      limit: 1,
      multipleUpload: false,
      fileList: [],
      action: '/mgr/jvs-auth//upload/jvs-public?module=tenantPicture',
      tips: {
        text: '建议 32 * 32',
        position: 'bottom'
      },
      formSlot: true
    },
    {
      label: 'LOGO',
      prop: 'logo',
      type: 'imageUpload',
      limit: 1,
      multipleUpload: false,
      fileList: [],
      action: '/mgr/jvs-auth//upload/jvs-public?module=tenantPicture',
      tips: {
        text: '建议 200 * 60',
        position: 'bottom'
      },
      formSlot: true
    },
    {
      label: '背景图',
      prop: 'bgImg',
      type: 'imageUpload',
      limit: 1,
      multipleUpload: false,
      fileList: [],
      action: '/mgr/jvs-auth//upload/jvs-public?module=tenantPicture',
      tips: {
        text: '建议 1920 * 1080',
        position: 'bottom'
      },
      formSlot: true
    },
  ]
}
export const basic = {
  enable: false,
  skipLogin: true,
  name: '',
  domain: '',
  icon: '',
  logo: '',
  bgImg: ''
}

export const dingOption = {
  formAlign: 'top',
  column: [
    {
      label: 'AgentId',
      prop: 'agentId',
      rules: [{ required: true, message: '请输入钉钉H5微应用的AgentId', trigger: 'blur' }],
    },
    {
      label: 'appKey',
      prop: 'appId',
      rules: [{ required: true, message: '请输入钉钉H5微应用的appKey', trigger: 'blur' }],
    },
    {
      label: 'appSecret',
      prop: 'appSecret',
      rules: [{ required: true, message: '请输入钉钉H5微应用的appSecret', trigger: 'blur' }],
    },
    {
      label: '开启扫码登录',
      prop: 'enableScan',
      type: 'switch'
    },
    {
      label: '重定向地址',
      prop: 'redirectUri',
      rules: [{ required: true, message: '请输入重定向地址', trigger: 'blur' }],
      defaultValue: `/#/login/dingtalk/scanback`,
      disabled: true,
      display: false
    }
  ]
}

export const DING_H5 = {
  enable: false,
  agentId: '', // 钉钉H5微应用的AgentId
  appId: '', // 钉钉H5微应用的appKey
  appSecret: '', // 钉钉H5微应用的appSecret
  enableScan: false, // 是否支持扫码登录
  redirectUri: '/#/login/dingtalk/scanback', // 重定向地址
}
export const wxOption = {
  // btnHide: true,
  formAlign: 'top',
  column: [
    {
      label: '企业ID',
      prop: 'appId',
      rules: [{ required: true, message: '请输入企业微信的corp_id（企业id）', trigger: 'blur' }],
    },
    {
      label: 'AgentId',
      prop: 'agentId',
      rules: [{ required: true, message: '请输入企业微信应用的AgentId', trigger: 'blur' }],
    },
    {
      label: 'Secret',
      prop: 'appSecret',
      rules: [{ required: true, message: '请输入企业微信应用的Secret', trigger: 'blur' }],
    },
    {
      label: '开启扫码登录',
      prop: 'enableScan',
      type: 'switch'
    },
    {
      label: '企业微信地址',
      prop: 'baseApiUrl',
      defaultValue: 'https://qyapi.weixin.qq.com',
      rules: [{ required: true, message: '请输入企业微信地址', trigger: 'blur' }],
    },
    {
      label: '开放平台地址',
      prop: 'openUrl',
      defaultValue: 'https://open.weixin.qq.com',
      rules: [{ required: true, message: '请输入开放平台地址', trigger: 'blur' }],
    },
    {
      label: '扫码登录地址',
      prop: 'scanUrl',
      defaultValue: 'https://open.work.weixin.qq.com',
      rules: [{ required: true, message: '请输入扫码登录地址', trigger: 'blur' }],
    },
    {
      label: '重定向地址',
      prop: 'redirectUri',
      rules: [{ required: true, message: '请输入重定向地址', trigger: 'blur' }],
      defaultValue: `/auth/just/callback`,
      disabled: true,
      display: false
    }
  ]
}

export const WX_ENTERPRISE = {
  enable: false,
  enableScan: false, // 是否支持扫码登录
  agentId: '', // 企业微信应用的AgentId
  appId: '', // 企业微信的corp_id（企业id）
  appSecret: '', // 企业微信应用的Secret
  redirectUri: '/auth/just/callback', // 重定向地址
}
export const wechatMpOption = {
  // btnHide: true,
  formAlign: 'top',
  column: [
    {
      prop: 'title1',
      type: 'p',
      formSlot: true
    },
    {
      label: 'appKey',
      prop: 'appKey',
      rules: [{ required: true, message: '请输入appKey', trigger: 'blur' }],
    },
    {
      label: 'appSecret',
      prop: 'appSecret',
      rules: [{ required: true, message: '请输入appSecret', trigger: 'blur' }],
    },
    {
      prop: 'title2',
      type: 'p',
      formSlot: true
    },
    {
      label: 'token',
      prop: 'token',
      // rules: [ { required: true, message: '请输入token', trigger: 'blur' } ],
    },
    {
      label: 'EncodingAESKey',
      prop: 'aesKey',
      // rules: [ { required: true, message: '请输入EncodingAESKey', trigger: 'blur' } ],
    },
    {
      label: '开启扫码登录',
      prop: 'enableScan',
      type: 'switch'
    }
  ]
}
export const WECHAT_MP = {
  enable: false,
  enableScan: false, // 是否支持扫码登录
  appKey: '',
  appSecret: '',
  token: '',
  aesKey: ''
}
export const LDAPOption = {
  // btnHide: true,
  formAlign: 'top',
  column: [
    {
      label: 'base',
      prop: 'base',
      rules: [{ required: true, message: '请输入base', trigger: 'blur' }],
    },
    {
      label: 'urls',
      prop: 'urls',
      rules: [{ required: true, message: '请输入urls', trigger: 'blur' }],
      tips: {
        position: 'bottom',
        text: '多个url以英文逗号,分隔'
      }
    },
    {
      label: '用户名',
      prop: 'username',
      rules: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
    },
    {
      label: '密码',
      prop: 'password',
      rules: [{ required: true, message: '请输入密码', trigger: 'blur' }],
    },
    {
      label: '账号字段',
      prop: 'accountAttribute',
      tips: {
        position: 'bottom',
        text: '为空则默认使用uid'
      }
    },
  ]
}
export const LDAP = {
  enable: false,
  base: '',
  urls: '',
  username: '',
  password: '',
  accountAttribute: ''
}
export const CASOption = {
  // btnHide: true,
  formAlign: 'top',
  column: [
    {
      label: 'cas_server_url_prefix',
      prop: 'cas_server_url_prefix',
      rules: [{ required: true, message: '请输入cas_server_url_prefix', trigger: 'blur' }],
    },
    {
      label: 'cas_server_url_login',
      prop: 'cas_server_url_login',
      rules: [{ required: true, message: '请输入cas_server_url_login', trigger: 'blur' }],
    },
    {
      label: 'client_host_url',
      prop: 'client_host_url',
      rules: [{ required: true, message: '请输入client_host_url', trigger: 'blur' }],
    }
  ]
}
export const CAS = {
  cas_server_url_prefix: '',
  cas_server_url_login: '',
  client_host_url: ''
}