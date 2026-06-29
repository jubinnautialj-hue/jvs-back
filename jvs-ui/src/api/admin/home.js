import request from '@/router/axios'
import { client_id} from '@/const/const'

//获取所有系统列表
export const getAllSystemList = (clientId) => {
  return request({
    url: `/mgr/jvs-design/base/use/menu`,
    method: "get",
    // params: clientId
  });
};

// 退出登录
export const loginoutHandle = () => {
  return request({
    url: `/auth/token/logout`,
    method: "get"
  });
};

// 获取当前租户信息
export const getTenantInfo= () => {
  return request({
    url: `/mgr/jvs-auth/index/this/tenant`,
    method: 'get'
  })
}

// 修改主题
export function editTheme(data) {
  return request({
    url: `/mgr/jvs-auth/tenant/theme`,
    method: "put",
    data: data
  });
}

// 获取前端跳转链接字典
export const getSystemHelpDict = (type) => {
  return request({
    url: `/mgr/jvs-auth/public/dict/type/${type}`,
    method: 'get'
  })
}

// 获取应用公告列表
export const getBulletinList = (params) => {
  return request({
    url: `/mgr/jvs-auth//index/bulletin/${client_id}`,
    method: 'get',
    params: params
  })
}

// 获取用户信息
export const getUserByCookie = () => {
  return request({
    url: `/auth/token`,
    method: "get",
    params: {
      client_id: client_id
    }
  });
};

// 获取服务资源
export const getDynamicResource = () => {
  return request({
    url: `/mgr/jvs-auth/dynamicResource`,
    method: "get",
    params: {
      client_id: client_id
    }
  });
}

// 是否有低代码资源
export const getDynamicDesign = () => {
  return request({
    url: `/mgr/jvs-auth/dynamicResource/design`,
    method: "get"
  });
}

// 切换模式
export function changeModeUser(data) {
  return request({
    url: `/mgr/jvs-design/base/JvsApp/switch/mode`,
    method: "post",
    data: data
  });
}