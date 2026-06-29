import request from '@/router/axios'

// 获取基础信息
export const getBasicDataInfo = () => {
  return request({
    url: `/mgr/jvs-auth//tenant/admin/base`,
    method: 'get',
  })
}

// 获取资源信息
export const getResourceList = () => {
  return request({
    url: `/mgr/jvs-design/base/tenant/admin`,
    method: 'get',
  })
}
