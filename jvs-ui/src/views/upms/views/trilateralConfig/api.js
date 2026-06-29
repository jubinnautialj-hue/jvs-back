import request from '@/router/axios'

// 三方应用登录对接 分页
export const getOtherOauth = (params) => {
  return request({
    url: `/mgr/jvs-auth/other/oauth/page`,
    method: 'get',
    params:  params
  })
}
// 三方应用登录对接 数据编辑
export const editOtherOauth = (data) => {
  return request({
    url: `/mgr/jvs-auth//other/oauth`,
    method: 'put',
    data:  data
  })
}
// 三方应用登录对接 数据新增
export const addOtherOauth = (data) => {
  return request({
    url: `/mgr/jvs-auth//other/oauth`,
    method: 'post',
    data:  data
  })
}
// 三方应用登录对接 数据删除
export const deleteOtherOauth = (id) => {
  return request({
    url: `/mgr/jvs-auth//other/oauth/${id}`,
    method: 'delete',
  })
}
