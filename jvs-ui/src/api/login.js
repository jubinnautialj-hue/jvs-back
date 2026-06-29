import request from "@/router/axios";
import {scope, client_id, client_secret, grant_type} from '@/const/const'

export const loginByUsername = (username, password, code, randomStr, app_client_id, call_back_url, ch) => {
  let pam = {username, password, grant_type, scope, client_id, client_secret}
  if(app_client_id && call_back_url) {
    pam = Object.assign(pam, {app_client_id: app_client_id}, {call_back_url: call_back_url})
  }
  // 渠道
  if(ch) {
    pam.ch = ch
  }
  return request({
    url: `/auth/oauth2/token`,
    headers: {
      isToken: false,
    },
    method: "post",
    params: pam
  });
};

export const refreshToken = (refresh_token, tenantId) => {
  const grant_type = "refresh_token";
  return request({
    url: "/auth/oauth2/token",
    headers: {
      isToken: false,
    },
    method: "post",
    params: { grant_type, scope, client_id, client_secret, refresh_token: refresh_token, switch: tenantId }
  });
};

// 获取系统基本信息
// ico、title、登录背景、logo
export function getDomain(params) {
  return request({
    url: "/auth/api/domain",
    method: "get",
    headers: {
      isToken: false
    },
    params: { client_id }
  });
}

export function getQRcode(params) {
  return request({
    url: "/mgr/jvs-auth/oauth",
    method: "get",
    params: params
  });
}

export function getCheck(data) {
  return request({
    url: "/weixin/check",
    method: "get",
    params: data,
    // 设置超时时间,直接进行下一次请求
    timeout: 30 * 1000
  });
}

export function getAllTentan(data) {
  return request({
    url: "/mgr/jvs-auth/tenant/list",
    method: "get"
  });
}

// 发送验证码
export function getPhone(data) {
  return request({
    url: `/auth/phone/sms/verification/${data.phone}`,
    method: "get"
  });
}

// 注册发送验证码
export function getRegPhoneCode(data) {
  return request({
    url: `/auth/phone/sms/register/${data.phone}`,
    method: "get"
  });
}

// 忘记密码---发送验证码
export function getPhoneCode(data) {
  return request({
    url: `/mgr/jvs-auth/reset/code/${data.phone}/${data.idStr}`,
    method: "get"
  });
}

// 验证码登录
export function codeLogin(data) {
  return request({
    url: "/auth/oauth2/token",
    method: "post",
    headers: {
      isToken: false
    },
    params: { client_id, client_secret, login_other_auth_parameter: data, grant_type: 'jvs' }
  });
}
// 微信登录
export function wxOpenidLogin(data) {
  return request({
    url: "/auth/login/wx/token",
    method: "get",
    headers: {
      isToken: false,
    },
    params: data
  });
}
// APP二维码登录
export function appQrLogin(data) {
  return request({
    url: "/mgr/jvs-auth/app/oauth",
    method: "get",
    headers: {
      isToken: false,
    },
    params: data
  });
}

// 检查APP扫码
export function appCheck(data){
  return request({
    url: `/mgr/jvs-auth/app/check/${data.uuid}`,
    method: "get",
    headers: {
      isToken: false
    },
    params: data
  });
}
// app登录
export function appLogin(data) {
  return request({
    url: "/auth/login/app/token",
    method: "get",
    headers: {
      isToken: false
    },
    params: data
  });
}


export const resetPass = (data) => {
  return request({
    url: "/mgr/jvs-auth/reset/verify/password",
    headers: {
      isToken: false,
      "Content-type": "text/plain",
    },
    method: "post",
    data, // {ciphertext: str}
  });
};


// 获取登录账号关联的所有租户
export function getTenantByUser() {
  return request({
    url: "/mgr/jvs-auth/user/tenants",
    method: "get"
  });
}

// 切换租户登录
export function tenantLogin(tenantId) {
  return request({
    url: "/mgr/jvs-auth/user/switch/tenant?tenantId="+tenantId,
    method: "post"
  });
}

// 获取公告
export function getBulletin(appKey) {
  return request({
    url: `/mgr/jvs-auth//index/bulletin/${appKey}`,
    method: "get"
  });
}

// 获取可登录的方式
export function getCanLogin() {
  return request({
    url: `/auth/just`,
    method: "get",
    headers: {
      isToken: false,
    },
    params: { client_id }
  });
}

// 公众号登录二维码
export const getOffLoginQcode = (uuid) => {
  return request({
    url: `/auth/wx/qr/code/login/${uuid}`,
    method: "get",
    params: { client_id }
  });
}

// 定时检查二维码状态
export const checkQrcodeState = (uuid) =>{
  return request({
    url: `/auth/wx/qr/code/check/${uuid}`,
    method: "get"
  });
}

// 获取钉钉扫码相关信息
export function getInfoByLoginType(params) {
  return request({
    url: `/auth/just/config`,
    method: "get",
    headers: {
      isToken: false,
    },
    params: Object.assign(params, {client_id: client_id})
  });
}

// 获取主租户三方回调信息
export function getRedirectUri () {
  return request({
    url: `/auth/just/redirectUri`,
    method: "get",
    headers: {
      isToken: false,
    },
    params: {
      client_id: client_id
    }
  })
}