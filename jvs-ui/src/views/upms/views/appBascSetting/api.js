import request from '@/router/axios'

// 获取租户应用配置
export const getAppSetByTenant=(tenantId) => {
  return request({
    url: `/mgr/jvs-auth/platform/sys/config/tenant/app/${tenantId}`,
    method: 'get',
  })
}

// 保存租户应用配置
export const saveAppSetByTenant=(tenantId, appId, type, data) => {
  return request({
    url: `/mgr/jvs-auth/platform/sys/config/${tenantId}/${appId}/${type}`,
    method: 'post',
    data: data
  })
}
