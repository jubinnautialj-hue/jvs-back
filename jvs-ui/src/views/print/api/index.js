import request from '@/router/axios'
let baseUrl = '/mgr/jvs-design'

/**
 * 查询指定业务标识所有打印模板
 * identity 业务标识
 */
 export const getTemplateAll=(jvsAppId, identity) => {
  return request({
    url: `${baseUrl}/app/design/${jvsAppId}/print/template/${identity}/all`,
    method: 'get'
  })
}

/**
 * 保存打印模板
 */
 export const saveTemplate=(jvsAppId, data) => {
  return request({
    url: `${baseUrl}/app/design/${jvsAppId}/print/template`,
    method: data.id ? 'put' : 'post',
    data: data
  })
}

/**
 * 查询打印模板信息
 * id	模板id
 * identity	业务标识
 */
export const getTemplate=(jvsAppId, id) => {
  return request({
    url: `${baseUrl}/app/use/${jvsAppId}/print/template/info/${id}`,
    method: 'get'
  })
}

/**
 * 删除打印模板
 * id 模板id
 */
 export const deleteTemplate=(jvsAppId, id) => {
  return request({
    url: `${baseUrl}/app/design/${jvsAppId}/print/template/${id}`,
    method: 'delete'
  })
}

/**
 * 获取设计所有可用打印模板
 * identity	业务标识
 */
 export const getAvailableTemplate=(jvsAppId, identity) => {
  return request({
    url: `${baseUrl}/app/use/${jvsAppId}/print/template/${identity}/available`,
    method: 'get'
  })
}

// 获取全部变量
export function getParamList(id) {
  return request({
    url: `/mgr/jvs-design/jvsFunction/params`,
    method: "get",
    params: {
      useCase: 'printFormItemValue.printOtherItemValue',
      id: id
    }
  })
}

// 获取全部变量
export function getWordParamList(id) {
  return request({
    url: `/mgr/jvs-design/jvsFunction/params`,
    method: "get",
    params: {
      useCase: 'printFileFormItemValue.printFileOtherItemValue.printSysItemValue',
      id: id,
      excludeUseCase: 'SYS'
    }
  })
}

// 获取打印数据
export const getPrintFormData=(jvsAppId, modelId, designId, dataId) => {
  return request({
    url: `${baseUrl}/app/use/${jvsAppId}/dynamic/data/query/single/transformation/${modelId}/${designId}/${dataId}`,
    method: 'get'
  })
}

// 获取工作流进度
export const getProcessOfFlow=(taskId) => {
  return request({
    url: `/mgr/jvs-design/base/workflow/print/progress/${taskId}`,
    method: 'get'
  })
}