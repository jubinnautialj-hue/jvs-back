import request from '@/router/axios'

// 获取角色列表
export const getRoleList = () => {
  return request({
      url: `/crud-design/role/list`,
      method: 'get',
  })
}

// 默认请求
export const sendRequire = (url, method, data) => {
  let obj = {
    url: url,
    method: method
  }
  if(data) {
    if(method == 'get' || method == 'delete') {
      obj.params = data
    }else{
      obj.data = data
    }
  }
  return request(obj)
}

// 获取所有key对应值
export const getKeyValue= () => {
  return request({
      url: `/mgr/jvs-design/base/page/labelValue`,
      method: 'get'
  })
}

// 自定义请求
export const sendMyRequire = (http, data) => {
  let headers = http.headers || {}
  headers['Content-Type'] = http.requestContentType
  let obj = {
    url: http.url,
    method: http.httpMethod,
    headers: headers
  }
  if(data) {
    if(http.requestContentType == 'application/x-www-form-urlencoded') {
      obj.params = data
    }else{
      obj.data = data
    }
  }
  return request(obj)
}

// 提交数据权限
export const saveDataPermission = (jvsAppId, data, modelId) => {
  return request({
      url: `/mgr/jvs-design/app/design/${jvsAppId}/data/model/data/filter/${modelId}`,
      method: 'post',
      data: data
  })
}

// 获取数据权限
export const getDataPermission = (jvsAppId, modelId) => {
  return request({
      url: `/mgr/jvs-design/app/design/${jvsAppId}/data/model/data/filter/${modelId}`,
      method: 'get'
  })
}

// 获取数据权限根据表单内容设置过滤条件
export const getDataModelDataFilter= (jvsAppId, modelId) => {
  return request({
      url: `/mgr/jvs-design/app/design/${jvsAppId}/data/model/DataConditionType/${modelId}`,
      method: 'get'
  })
}

// 获取应用下的所有列表页
export const getAllPageByApplication= (jvsAppId) => {
  return request({
      url: `/mgr/jvs-design/app/design/${jvsAppId}/page/all`,
      method: 'get'
  })
}

// 根据条件获取关联模型数据
export const pageGetLinkModelData = (jvsAppId, data) => {
  return request({
      url: `/mgr/jvs-design/app/use/${jvsAppId}/dynamic/data/query/page/linkage`,
      method: 'post',
      data: data
  })
}