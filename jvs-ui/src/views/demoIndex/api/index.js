import request from "@/router/axios"

// 查询全部模型数据
export const getPageDataList = (jvsAppId, modelId) => {
  let tp = {
    url: `/mgr/jvs-design/app/use/${jvsAppId}/dynamic/data/query/list/${modelId}`,
    method: 'post'
  }
  return request(tp)
}

// 分页查询模型数据
export const getModelDataByPage=(jvsAppId, modelId, data) => {
  let tp = {
    url: `/mgr/jvs-design/app/use/${jvsAppId}/dynamic/data/query/page/${modelId}`,
    method: 'post',
    data: data,
  }
  return request(tp)
}

// 查询单条数据详情
export const getSingleData = (jvsAppId, modelId, dataId) => {
  return request({
    url: `/mgr/jvs-design/app/use/${jvsAppId}/dynamic/data/query/single/${modelId}/${dataId}`,
    method: 'get',
  })
}

// 新增单条数据
export const addSingleData = (jvsAppId, modelId, data) => {
  return request({
    url: `/mgr/jvs-design/app/use/${jvsAppId}/dynamic/data/save/${modelId}`,
    method: 'post',
    data: data
  })
}

// 修改单条数据
export const editSingleData = (jvsAppId, modelId, dataId, data) => {
  return request({
    url: `/mgr/jvs-design/app/use/${jvsAppId}/dynamic/data/update/${modelId}/${dataId}`,
    method: 'post',
    data: data
  })
}

// 删除单条数据
export const delSingleData = (jvsAppId, modelId, dataId) => {
  return request({
    url: `/mgr/jvs-design/app/use/${jvsAppId}/dynamic/data/delete/${modelId}/${dataId}`,
    method: 'delete'
  })
}