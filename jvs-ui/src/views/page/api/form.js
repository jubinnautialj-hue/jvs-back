import request from '@/router/axios'

// 名称集
export const getNameList = () => {
  return request({
      url: `/mgr/jvs-design/form/design/names`,
      method: 'get'
  })
}

// 详情
export const getDetailByName = (data) => {
  return request({
      url: `/mgr/jvs-design/form/design`,
      method: 'get',
      params: data
  })
}

// 新增表单
export const addForm = (jvsAppId, data) => {
  return request({
      url: `/mgr/jvs-design/app/design/${jvsAppId}/form`,
      method: 'post',
      data: data
  })
}

// 修改表单
export const updateForm = (jvsAppId, data) => {
  return request({
      url: `/mgr/jvs-design/app/design/${jvsAppId}/form`,
      method: 'put',
      data: data
  })
}

// 检查字段是否为类型变更
export const checkForm = (jvsAppId, data) => {
  return request({
      url: `/mgr/jvs-design/app/design/${jvsAppId}/form/check`,
      method: 'put',
      data: data
  })
}

// 删除表单
export const deleteForm = (jvsAppId, name) => {
  return request({
      url: `/mgr/jvs-design/app/design/${jvsAppId}/form/${name}`,
      method: 'delete'
  })
}

// 发布
export const deployForm = (jvsAppId, id) => {
  return request({
      url: `/mgr/jvs-design/app/design/${jvsAppId}/form/deploy/${id}`,
      method: 'put'
  })
}

// 用户列表
export function getUserList(query) {
  return request({
    url: "/mgr/jvs-auth/usermanager/list",
    method: "get",
    params: query
  });
}

// 获取用户的部门菜单
export function getDeptList() {
  return request({
    url: "/mgr/jvs-auth/dept/all",
    method: "get",
  });
}

// 获取全部角色信息
export function getRoleList() {
  return request({
    url: "/mgr/jvs-auth/role/all",
    method: "GET"
  });
}

// 获取职位列表
export function getPostList() {
  return request({
    url: "/mgr/jvs-auth/job/list",
    method: "GET"
  });
}

// 所有正则
export function getRegExpList(params) {
  return request({
    url: `/mgr/jvs-design/base/form/design/regexp`,
    method: "get",
    params: params
  });
}

// 可关联的表单列表
export function getConnectFormList(jvsAppId) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/form/list`,
    method: "get"
  });
}

// 关联的表单的字段列表
export function getConnectFormProps(jvsAppId, modelId, designId) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/data/field/list?modelId=${modelId}&designId=${designId}`,
    method: "get"
  });
}

// 脱敏正则列表
export function getEncryptionList() {
  return request({
    url: `/mgr/jvs-design/base/form/design/sensitive`,
    method: "get"
  });
}

// 删除已有逻辑
export function delRuleBySecret(jvsAppId, secret) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/rule/secret/${secret}`,
    method: "delete"
  });
}