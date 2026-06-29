import request from '@/router/axios'

// 保存短信配置
export const saveSMSConfig=(params, tenantId) => {
  return request({
    url: `/mgr/jvs-auth/platform/sys/config/${tenantId}/frame/SMS`,
    method: 'post',
    data: params
  })
}

// 获取短信配置
export const getSMSConfig=() => {
  return request({
    // url: `/mgr/jvs-auth/platform/tenant/admin/base/config/SMS`,
    url: `/mgr/jvs-auth/tenant/admin/base/config/SMS`,
    method: 'get',
  })
}
