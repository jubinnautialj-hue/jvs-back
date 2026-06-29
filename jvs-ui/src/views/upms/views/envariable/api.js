import request from "@/router/axios";

// 分页查询环境变量列表
export const getEnvVariableList=(params) => {
  return request({
    url: `/mgr/jvs-auth/environment/variable/page`,
    method: 'get',
    params: params
  })
}

// 新增
export const addEnvVariable=(data) => {
  return request({
    url: `/mgr/jvs-auth/environment/variable`,
    method: 'post',
    data: data
  })
}

// 修改
export const editEnvVariable=(data) => {
  return request({
    url: `/mgr/jvs-auth/environment/variable`,
    method: 'put',
    data: data
  })
}

// 删除
export const delEnvVariable=(id) => {
  return request({
    url: `/mgr/jvs-auth/environment/variable/${id}`,
    method: 'delete'
  })
}