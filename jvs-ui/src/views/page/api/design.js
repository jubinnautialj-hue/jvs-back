import request from '@/router/axios'

// 详情
export const getDesignInfo = (jvsAppId, menuId) => {
  return request({
      url: `/mgr/jvs-design/app/design/${jvsAppId}/page/detail/${menuId}`,
      method: 'get'
  })
}

// 被引用时查询详情
export const getDesignInfoBindByOther = (jvsAppId, menuId) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/page/detail/${menuId}`,
    method: 'get'
  })
}

// 更新
export const updateDesignInfo = (jvsAppId, id, data) => {
  return request({
      url: `/mgr/jvs-design/app/design/${jvsAppId}/page/update/${id}`,
      method: 'POST',
      data: data
  })
}

// // 部署的列表页详情
// export const getDesignTableInfo = (menuId) => {
//   return request({
//       url: `/mgr/jvs-design/design/deployed/${menuId}`,
//       method: 'get'
//   })
// }
// 部署的列表页详情
export const getDesignTableInfo = (jvsAppId, menuId) => {
  return request({
      url: `/mgr/jvs-design/app/use/${jvsAppId}/crudPage/${menuId}`,
      method: 'get'
  })
}
// 列表页预览
export const previewPage = (jvsAppId, menuId) => {
  return request({
      url: `/mgr/jvs-design/app/design/${jvsAppId}/page/detail/${menuId}`,
      method: 'get'
  })
}
// 查询单条数据详情
export const getSingleData = (jvsAppId, modelId, dataId, designId, pageDesignId) => {
  return request({
    url: `/mgr/jvs-design/app/use/${jvsAppId}/dynamic/data/query/single/${modelId}/${dataId}`,
    method: 'get',
    headers: {
      'designId': designId,
      'pageDesignId': pageDesignId
    }
  })
}

// 删除单条数据
export const delSingleData = (jvsAppId, modelId, dataId, designId) => {
  return request({
    url: `/mgr/jvs-design/app/use/${jvsAppId}/dynamic/data/delete/${modelId}/${dataId}`,
    method: 'delete',
    headers: {
      'designId': designId,
      'operator': encodeURI('删除')
    }
  })
}

// 批量删除数据
export const delMultipleData = (jvsAppId, modelId, data, designId) => {
  return request({
    url: `/mgr/jvs-design/app/use/${jvsAppId}/dynamic/data/batch/delete/${modelId}`,
    method: 'post',
    headers: {
      'designId': designId,
      'operator': encodeURI('删除')
    },
    data: data
  })
}

// 导出
export const exportData = (jvsAppId, modelId, params) => {
  return request({
    url: `/mgr/jvs-design/app/use/${jvsAppId}/dynamic/data/export/${modelId}`,
    method: 'get',
    responseType: 'arraybuffer',
    headers: {
      'designId': params.designId,
      'operator': encodeURI('导出'),
    },
    params: params
  })
}

// 批量导出
export const exportListData = (jvsAppId, modelId, data, params) => {
  return request({
    url: `/mgr/jvs-design/app/use/${jvsAppId}/dynamic/data/export/${modelId}`,
    method: 'post',
    responseType: 'blob',
    headers: {
      'designId': params.designId,
      'operator': encodeURI('导出'),
    },
    data: data,
    params: params
  })
}

// 下载模板
export const downloadTemplate = (jvsAppId, modelId, params) => {
  return request({
    url: `/mgr/jvs-design/app/use/${jvsAppId}/dynamic/data/download/excel/template/${modelId}`,
    method: 'get',
    responseType: 'arraybuffer',
    headers: {
      'designId': params.designId,
      'operator': encodeURI('下载模板'),
    },
    params: params
  })
}

// 新建逻辑引擎
export const createRule = (data, headers) => {
  let temp = {
    url: `/mgr/jvs-design/app/design/${data.jvsAppId}/rule`,
    method: 'put',
    params: data
  }
  if(headers) {
    temp.headers = headers
  }
  return request(temp)
}
