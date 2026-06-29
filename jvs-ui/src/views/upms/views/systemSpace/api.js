import request from '@/router/axios'


// 获取当前租户空间
export const getSpace=() => {
  return request({
    url: `/mgr/jvs-auth/tenant/admin/base/space`,
    method: 'get',
  })
}
