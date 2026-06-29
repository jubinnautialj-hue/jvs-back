import request from '@/router/axios'

// 分页查询应用列表
export const getAppList=(params) => {
  return request({
    url: `/mgr/jvs-auth/platform/apply/page`,
    method: 'get',
    params: params
  })
}

// 新增应用
export const addApp=(data) => {
  return request({
    url: `/mgr/jvs-auth/platform/apply/save`,
    method: 'post',
    data: data
  })
}

// 修改应用
export const editApp=(data) => {
  return request({
    url: `/mgr/jvs-auth/platform/apply`,
    method: 'put',
    data: data
  })
}

// 删除应用
export const delApp=(id) => {
  return request({
    url: `/mgr/jvs-auth/platform/apply/${id}`,
    method: 'delete'
  })
}

// 获取单个应用详情
export const getAppById=(id) => {
  return request({
    url: `/mgr/jvs-auth/platform/apply/${id}`,
    method: 'get'
  })
}

// 启用 禁用  应用
export function enableDisApp(id, cancelFlag) {
  let able = 'enable'
  if(cancelFlag) {
    able = 'disabled'
  }
  return request({
    url: `/mgr/jvs-auth/platform/apply/${able}/${id}`,
    method: "delete"
  });
}
