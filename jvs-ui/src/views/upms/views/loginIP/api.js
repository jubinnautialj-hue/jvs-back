import request from "@/router/axios";

// 分页查询
export const getPageList = (params) => {
  return request({
    url: `/mgr/jvs-auth/login/rules/page`,
    method: 'get',
    params: params
  })
}

// 新增
export const add = (data) => {
  return request({
    url: `/mgr/jvs-auth/login/rules/add`,
    method: 'post',
    data: data
  })
}

// 编辑
export const edit = (data) => {
  return request({
    url: `/mgr/jvs-auth/login/rules/update`,
    method: 'put',
    data: data
  })
}

// 删除
export const del = (id) => {
  return request({
    url: `/mgr/jvs-auth/login/rules/delete/${id}`,
    method: 'delete',
  })
}
