import request from '@/router/axios'

// 新建页面
export const createPage = (jvsAppId, params, headers) => {
  let id = ''
  let obj = null
  if(typeof jvsAppId == 'string') {
    id = jvsAppId
    obj = params
  }
  if(typeof jvsAppId == 'object') {
    id = jvsAppId.jvsAppId
    obj = jvsAppId
  }
  let temp = {
    url: `/mgr/jvs-design/app/design/${id}/page/create`,
    method: 'post',
    data: obj
  }
  if(headers) {
    temp.headers = headers
  }
  return request(temp)
}

// 修改自定义页面
export const editCustomPage = (jvsAppId, params) => {
  return request({
      url: `/mgr/jvs-design/app/design/${jvsAppId}/jvsAppUrl`,
      method: 'put',
      data: params
  })
}

// 部署
export const deployPage = (jvsAppId, data) => {
  return request({
      url: `/mgr/jvs-design/app/${jvsAppId}/page/design/deploy/${data.id}`,
      method: 'post',
      data: data
  })
}

// 删除
export const deletePage = (jvsAppId, id) => {
  return request({
      url: `/mgr/jvs-design/app/design/${jvsAppId}/page/${id}`,
      method: 'delete'
  })
}

// 卸载菜单
export const unloadPage = (jvsAppId, id) => {
  return request({
      url: `/mgr/jvs-design/app/${jvsAppId}/page/design/unload/${id}`,
      method: 'post'
  })
}

// 修改页面
export const editPage = (jvsAppId, data) => {
  return request({
      url: `/mgr/jvs-design/app/design/${jvsAppId}/page/rename`,
      method: 'put',
      data: data
  })
}

// sql统计
export const getStatistics = (jvsAppId, designId, statisticsCode, data) => {
  return request({
    url: `/mgr/jvs-design/app/${jvsAppId}/datasource/statistics/${designId}/${statisticsCode}`,
    method: "post",
    data: data
  });
};

// 获取详情
export const getDetail=(jvsAppId, params) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/form/${params.name}`,
    method: 'get',
    params: params
  })
}


// 获取所有应用
export const getAllApp=(params) => {
  return request({
    url: `/mgr/jvs-design/app/JvsApp/all`,
    method: 'get',
    params: params
  })
}

// 根据id获取应用详情
export const getAppInfoById=(id) => {
  return request({
    url: `/mgr/jvs-design/base/JvsApp/name/${id}`,
    method: 'get'
  })
}

// 获取列表页按钮formId
export const getButtonFormId=(jvsAppId, dataModelId, designId, buttonName) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/page/generateForm/${dataModelId}/${designId}`,
    method: 'get',
    params: {
      buttonName: encodeURIComponent(buttonName)
    }
  })
}

// 列表页分页查询数据
export const getCrudDataPage=(jvsAppId, params, modelId, designId, fieldId, notification, origin) => {
  let tp = {
    url: `/mgr/jvs-design/app/use/${jvsAppId}/dynamic/data/query/page/${modelId}`,
    method: 'post',
    data: params,
  }
  if(designId) {
    tp.headers = {
      'designId': designId
    }
    if(origin != 'fromForm') {
      tp.headers.pageDesignId = designId
    }
  }
  if(fieldId) {
    tp.headers = {
      'formId': designId,
      'fieldId': fieldId
    }
  }
  if(notification !== null && notification !== undefined) {
    tp.headers.notification = notification
  }
  return request(tp)
}

// 创建自定义页面
export const createCustomPage=(data) => {
  return request({
    url: `/mgr/jvs-design/app/design/${data.jvsAppId}/jvsAppUrl`,
    method: 'post',
    data: data,
  })
}

// 触发规则
export const association=(jvsAppId, dataModelId, designId, permissionFlag, data, operator) => {
  return request({
    url: `/mgr/jvs-design/app/use/${jvsAppId}/data/association/${dataModelId}/${designId}/${permissionFlag}`,
    method: 'post',
    data: data,
    headers: {
      'designId': designId,
      operator: operator
    }
  })
}

// 打开其他列表时保存关联字段
export const updatePageRelation =(jvsAppId, modelId, dataId, headers, data) => {
  return request({
    url: `/mgr/jvs-design/app/use/${jvsAppId}/dynamic/data/update/relation/${modelId}/${dataId}`,
    method: 'post',
    data: data,
    headers: headers
  })
}

// 获取自定义的带参数的url
export const getUrlWithParamter =(jvsAppId, modelId, dataId, data) => {
  return request({
    url: `/mgr/jvs-design/app/use/${jvsAppId}/dynamic/data/${modelId}/button/${dataId}`,
    method: 'post',
    data: data
  })
}

// 获取数据日志
export const getDataLogList = (jvsAppId, dataModelId, dataId) => {
  return request({
    url: `/mgr/jvs-design/app/use/${jvsAppId}/data/log/follow/${dataModelId}/${dataId}`,
    method: 'get'
  })
}

// 添加数据日志
export const addDataLogComment = (jvsAppId, dataModelId, dataId, data) => {
  return request({
    url: `/mgr/jvs-design/app/use/${jvsAppId}/data/log/follow/${dataModelId}/${dataId}`,
    method: 'post',
    data: data
  })
}

// 复制组件时替换公式id
export const copyFormulaComponent = (jvsAppId, designId, data) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/form/copy/component/${designId}`,
    method: 'post',
    data: data
  })
}