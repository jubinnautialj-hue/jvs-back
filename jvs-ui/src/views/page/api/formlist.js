import request from '@/router/axios'

// 列表
export const getPageList=(jvsAppId, params, orders) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/form/page${orders ? orders : ''}`,
    method: 'get',
    params: params
  })
}

// 归类列表
export const getTypeList=() => {
  return request({
    url: `/mgr/jvs-design/form/design/types`,
    method: 'get'
  })
}

// 新增
export const addForm=(jvsAppId, data, headers) => {
  let appid = ''
  let obj = null
  if(typeof jvsAppId == 'string') {
    appid = jvsAppId
    obj = data
  }
  if(typeof jvsAppId == 'object') {
    appid = jvsAppId.jvsAppId
    obj = jvsAppId
  }
  let temp = {
    url: `/mgr/jvs-design/app/design/${appid}/form`,
    method: 'post',
    data: obj,
  }
  if(headers) {
    temp.headers = headers
  }
  return request(temp)
}

// 编辑
export const editForm=(jvsAppId, data) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/form`,
    method: 'put',
    data: data
  })
}

// 获取详情
export const getDetail=(jvsAppId, id) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/form/${id}`,
    method: 'get',
    // params: params
  })
}

// 获取设计数据结构
export const getDataStr=(jvsAppId, id) => {
  return request({
    url: `/mgr/jvs-design/app/use/${jvsAppId}/dynamic/data/fields/${id}`,
    method: 'get',
    // params: params
  })
}

// 部署
export const deployForm=(jvsAppId, id) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/form/deploy/${id}`,
    method: 'put',
  })
}

// 卸载
export const undeployForm=(jvsAppId, id) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/form/unload/${id}`,
    method: 'post',
  })
}

// 删除
export const delForm=(jvsAppId, id) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/form/del/${id}`,
    method: 'delete'
  })
}

// // 部署的表单详情
// export const getFormInfo=(key) => {
//   return request({
//     url: `/mgr/jvs-design/form/design/deployed/${key}`,
//     method: 'get'
//   })
// }

// 部署的表单详情
export const getFormInfo=(jvsAppId, id) => {
  return request({
    url: `/mgr/jvs-design/app/use/${jvsAppId}/form/${id}`,
    method: 'get'
  })
}

// 获取二维码标签数据
export const getFormTagData=(jvsAppId, designId, useCase, data) => {
  return request({
    url: `/mgr/jvs-design/app/use/${jvsAppId}/form/code/data/${designId}/${useCase}`,
    method: 'post',
    data: data
  })
}

// 表单变更记录
export function getFormEditLogs(jvsAppId, dataModelId, dataId) {
  return request({
    url: `/mgr/jvs-design/app/use/${jvsAppId}/data/log/change/${dataModelId}/${dataId}`,
    method: "get"
  });
}