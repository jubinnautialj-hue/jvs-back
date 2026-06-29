import request from '@/router/axios'
let base = '/mgr/jvs-design/'

// 发布到模板
export function pubTemplate(data) {
  return request({
    url: `/mgr/jvs-design/design/template/save`,
    method: "post",
    data: data
  });
}

// 新增应用目录
export function addCataType(data) {
  return request({
    url: `/mgr/jvs-design/app/design/${data.appId}/JvsApp/add/type`,
    method: "post",
    data: data
  });
}

// 修改应用目录
export function editCataType(data) {
  return request({
    url: `/mgr/jvs-design/app/design/${data.appId}/JvsApp/update/type`,
    method: "post",
    data: data
  });
}

// 删除应用目录
export function delCataType(params) {
  return request({
    url: `/mgr/jvs-design/app/design/${params.appId}/JvsApp/del/type/${params.id}`,
    method: "delete"
  });
}

// 修改应用设计名称
export function editDesign(data) {
  return request({
    url: `/mgr/jvs-design/app/design/${data.appId}/JvsApp/update`,
    method: "put",
    params: data
  });
}

// 删除设计
export function delDesign(params) {
  return request({
    url: `/mgr/jvs-design/app/design/${params.appId}/JvsApp/del`,
    method: "delete",
    params: params
  });
}

// 分页查询
export const pageList = (params) => {
  return request({
    url: base + `/base/JvsApp/page`,
    method: 'get',
    params:  params
  })
}

// 获取应用菜单信息
export const getAppDetail = (appId) => {
  return request({
    url: `/mgr/jvs-design/use/${appId}`,
    method: 'get',
  })
}

// 新增
export const add = (data) => {
  return request({
    url: base + `base/JvsApp/save`,
    method: 'post',
    data:  data
  })
}

// 编辑
export const edit = (data) => {
  return request({
    url: base + `/app/manage/${data.id}/edit`,
    method: 'put',
    data:  data
  })
}

// 新增模板
export const addTemplate = (data) => {
  return request({
    url: base + `base/JvsAppTemplate/deploy/primitive`,
    method: 'post',
    data:  data
  })
}

// 编辑模板
export const editTemplate = (data) => {
  return request({
    url: base + `base/JvsAppTemplate`,
    method: 'put',
    data:  data
  })
}

// 下载模板
export const downloadTemplate = (data) => {
  return request({
    url: base + `base/JvsAppTemplate/download/${data.id}`,
    method: 'get',
    responseType: 'arraybuffer',
  })
}

// 删除应用
export const del = (id, params) => {
  return request({
    url: base + `/app/manage/${id}/del`,
    method: 'delete',
    params: params
  })
}

// 删除应用模板
export const delTemplate = (templateId) => {
  return request({
    url: base + `base/JvsAppTemplate/del/${templateId}`,
    method: 'delete',
  })
}

// 分页查询模板
export const templateList = (params) => {
  return request({
    url: base + `base/JvsAppTemplate/list`,
    method: 'get',
    params: params
  })
}

// 获取推荐应用
export const getRecommendApp = (params) => {
  return request({
    url: base + `base/JvsAppTemplate/recommend`,
    method: 'get',
    params: params
  })
}

// 获取模板类型
export const getTemplateType = () => {
  return request({
    url: base + `base/JvsAppTemplate/types`,
    method: 'get'
  })
}

// 获取模板详情
export const getTemplateDetail = (templateId) => {
  return request({
    url: base + `base/JvsAppTemplate/detail/${templateId}`,
    method: 'get'
  })
}

// // 发布
// export const publish = (id) => {
//   return request({
//     url: base + `/app/manage/${id}/deploy`,
//     method: 'put',
//   })
// }

// 发布到模板
export const publishToTemplate = (data) => {
  return request({
    url: base + `base/JvsAppTemplate/deploy`,
    method: 'post',
    data: data
  })
}

// // 卸载
// export const unload = (id) => {
//   return request({
//     url: base + `/app/manage/${id}/unload`,
//     method: 'put',
//   })
// }


/**
 * 应用管理相关请求
 * applicationPage.vue
 */

// 应用详情
export const getApplicationDetail = (id) => {
  return request({
    url: `/mgr/jvs-design/base/JvsApp/${id}/detail`,
    method: 'get'
  })
}

// 应用菜单详情
export const getApplicationMenu = (appId) => {
  return request({
    url: `/mgr/jvs-design/app/use/${appId}`,
    method: 'get'
  })
}

// 获取所有的逻辑列表
export function getAllRule (id, params) {
  return request({
    url: `/mgr/jvs-design/app/design/${id}/rule/all`,
    method: 'get',
    params: params
  })
}

// 查询所有模型--设计
export const getAllModel = (appId) => {
  return request({
    url: `/mgr/jvs-design/app/design/${appId}/data/model/all`,
    method: 'get'
  })
}

// 查询模型下所有字段--设计
export const getModelAllFields = (jvsAppId, modelId, designId) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/data/model/list/${modelId}`,
    method: 'get',
    headers: {
      designId: designId
    }
  })
}

// 查询模型下所有字段--使用
export const getUseModelAllFields = (jvsAppId, modelId, designId) => {
  return request({
    url: `/mgr/jvs-design/app/use/${jvsAppId}/data/model/list/${modelId}`,
    method: 'get',
    headers: {
      designId: designId
    }
  })
}

// 模型的消息通知获取字段
export const getModelAllFieldsByNotice = (jvsAppId, modelId, designId) => {
  return request({
    url: `/mgr/jvs-design/base/notice/${jvsAppId}/${modelId}/fields`,
    method: 'get',
    headers: {
      designId: designId
    }
  })
}

// 获取联动模型下的所有字段
export const getLinkModelAllFields = (jvsAppId, modelId, designId) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/data/model/field/dataLinkage/${modelId}`,
    method: 'get',
    headers: {
      designId: designId
    }
  })
}

// 查询数据集分页
export const getDataModel = (params) => {
  let obj = {}
  for(let i in params) {
    if(i != 'appId') {
      obj[i] = params[i]
    }
  }
  return request({
    url: `/mgr/jvs-design/app/design/${params.appId}/data/model/list`,
    method: 'get',
    params:  obj
  })
}

// 获取这个模型的索引
export const getModelIndex = (appId, modelId) => {
  return request({
    url: `/mgr/jvs-design/app/design/${appId}/data/model/indexField/${modelId}`,
    method: 'get'
  })
}

// 数据集添加索引--设计
export const addIndexField = (appId, modelId, data) => {
  return request({
    url: `/mgr/jvs-design/app/design/${appId}/data/model/indexField/${modelId}`, // `/mgr/jvs-design/app/design/${jvsAppId}/data/model/indexField/${modelId}/${fieldKey}`,
    method: 'post',
    data: data
  })
}

// 数据集添加索引--跳过权限
export const addBaseIndexField = (modelId, fieldKey) => {
  return request({
    url: `/mgr/jvs-design/base/data/model/indexField/${modelId}/${fieldKey}`,
    method: 'post',
  })
}

// 获取所有设计数据
export const getAllUse = (appId) => {
  return request({
    url: `/mgr/jvs-design/app/design/${appId}/JvsApp/design/all`,
    method: 'get'
  })
}

// 获取分页设计数据
export const getPageUseDesign = (appId, params) => {
  return request({
    url: `/mgr/jvs-design/app/design/${appId}/JvsApp/design/type/page`,
    method: 'get',
    params
  })
}

// 获取应用密钥
export const getSecret = (appId) => {
  return request({
    url: `/mgr/jvs-design/app/manage/${appId}/secret`,
    method: 'get',
  })
}

// 获取应用所有工作流
export const getAllWorkflowByApp = (appId) => {
  return request({
    url: `/mgr/jvs-design/app/design/${appId}/workflow`,
    method: 'get',
  })
}

// 分页获取应用工作流
export const getPageWorkflowByApp = (appId, params) => {
  return request({
    url: `/mgr/jvs-design/app/design/${appId}/workflow/page`,
    method: 'get',
    params: params
  })
}

// 获取应用所有工作流
export const getAllWorkflowByAppUse = (appId) => {
  return request({
    url: `/mgr/jvs-design/base/workflow/design/all?appId=${appId}`,
    method: 'get',
  })
}


// 获取帐号密码ip的mysql数据
export const getMysql = (params, jvsAppId) => {
  return request({
    url: `/mgr/jvs-design/app/${jvsAppId}/JvsApp/sync/ip`,
    method: 'post',
    data: params
  })
}

// 同步数据
export const syncData = (params, jvsAppId) => {
  return request({
    url: `/mgr/jvs-design/app/${jvsAppId}/JvsApp/sync/sync`,
    method: 'post',
    data: params
  })
}

// 同步操作返回进度
export const getSyncProcess = (jvsAppId) => {
  return request({
    url: `/mgr/jvs-design/app/${jvsAppId}/JvsApp/sync/sync`,
    method: 'get',
  })
}

// 应用操作日志分页
export const getAppLogPage = (params) => {
  let obj = {}
  for(let i in params) {
    if(i != 'jvsAppId') {
      obj[i] = params[i]
    }
  }
  return request({
    url: `/mgr/jvs-design/app/manage/${params.jvsAppId}/jvsLog/page`,
    method: 'get',
    params:  obj
  })
}


// 设置应用详情
export const setLongtext = (params) => {
  return request({
    url: base + `app/manage/${params.id}/text`,
    method: 'put',
    data: params
  })
}

// 指定首页
export const setIndexPage = (data) => {
  return request({
    url: `/mgr/jvs-auth//tenant/default/index`,
    method: 'put',
    data: data
  })
}

// 查询自定义页面接入列表
export const getListExternalPage = (jvsAppId, params) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/external/page/list`,
    method: 'get',
    params: params
  })
}


// 新增自定义页面接入
export const addExternalPage = (jvsAppId, data) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/external/page`,
    method: 'post',
    data: data
  })
}

// 修改自定义页面接入
export const editExternalPage = (jvsAppId, data) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/external/page`,
    method: 'put',
    data: data
  })
}

// 查询单个自定义页面接入
export const getExternalPage = (jvsAppId, id) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/external/page/${id}`,
    method: 'get'
  })
}

// 删除自定义页面接入
export const delExternalPage = (jvsAppId, id) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/external/page/${id}`,
    method: 'delete'
  })
}

// 授权自定义页面接入
export const grantExternalPage = (jvsAppId, data) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/external/page/grant`,
    method: 'post',
    data: data
  })
}

// 新建图表
export const createChartPage = (data) => {
  return request({
    url: `/mgr/jvs-design/app/design/${data.jvsAppId}/chart/design`,
    method: 'post',
    data: data
  })
}

// 新增大屏
export function createScreenPage (data) {
  return request({
    url: base + `/app/design/${data.jvsAppId}/screen/save`,
    method: 'post',
    data: data
  })
}

// 新增报表
export function createReportPage (data) {
  return request({
    url: base + `/app/design/${data.jvsAppId}/report/design`,
    method: 'post',
    data: data
  })
}


// 获取应用所有设计关系
export const getAppRelation = (jvsAppId) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/JvsApp/design/relation/all `,
    method: 'get'
  })
}

// 删除模型字段
export const delModelField = (jvsAppId, dataModelId, fieldKey) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/data/model/${dataModelId}/${fieldKey}`,
    method: 'delete'
  })
}

// 删除模型
export const delModel = (jvsAppId, dataModelId) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/data/model/${dataModelId}`,
    method: 'delete'
  })
}

// 获取应用所有快捷回复
export const getFlowReply = (jvsAppId) => {
  return request({
    url: `/mgr/jvs-design/base/workflow/quick/reply/${jvsAppId}`,
    method: 'get'
  })
}

// 新增快捷回复
export function createFlowReply (jvsAppId, data) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/workflow/quick/reply`,
    method: 'post',
    data: data
  })
}

// 修改快捷回复
export function editFlowReply (jvsAppId, data) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/workflow/quick/reply`,
    method: 'put',
    data: data
  })
}

// 删除快捷回复
export function delFlowReply (jvsAppId, id) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/workflow/quick/reply/${id}`,
    method: 'delete'
  })
}

// 获取应用级标识列表
export const getIdentificationPage = (jvsAppId, params) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/identification/page`,
    method: 'get',
    params: params
  })
}

// 新增应用级标识
export const addIdentification = (jvsAppId, data) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/identification`,
    method: 'post',
    data: data
  })
}

// 修改应用级标识
export const editIdentification = (jvsAppId, data) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/identification`,
    method: 'put',
    data: data
  })
}

// 删除应用标识
export const delIdentification = (appId, id) => {
  return request({
    url: `/mgr/jvs-design/app/design/${appId}/identification/${id}`,
    method: 'delete'
  })
}

// 获取当前应用模型分页
export const getDataModelListByApp = (jvsAppId, params) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/data/model/page`,
    method: 'get',
    params: params
  })
}

// 获取当前应用逻辑分页
export const getRuleListByApp = (jvsAppId, params) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/rule/page/list`,
    method: 'get',
    params: params
  })
}

// 获取应用使用中的版本
export const getAppVersion = (jvsAppId) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/version/use`,
    method: 'get',
  })
}

// 开发版本提交测试
export function devSubmitBeta (jvsAppId, data) {
  return request({
    url:  `/mgr/jvs-design/app/design/${jvsAppId}/version/submit/beta`,
    method: 'post',
    data: data
  })
}

// 测试版本发布到线上
export function betaSubmitGA (jvsAppId, versionId, data) {
  return request({
    url:  `/mgr/jvs-design/app/design/${jvsAppId}/version/publish/${versionId}`,
    method: 'post',
    data: data
  })
}

// 线上版本暂停服务
export function GASuspend (jvsAppId) {
  return request({
    url:  `/mgr/jvs-design/app/design/${jvsAppId}/version/suspend`,
    method: 'post'
  })
}

// 线上版本启用服务
export function betaUse (jvsAppId) {
  return request({
    url:  `/mgr/jvs-design/app/design/${jvsAppId}/version/use`,
    method: 'post'
  })
}

// 获取历史版本列表
export const getHistroyVersion = (jvsAppId, versionType) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/version/history/${versionType}`,
    method: 'get',
  })
}

// 回退到历史版本
export const backHistroyVersion = (jvsAppId, data) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/version/switch/version`,
    method: 'post',
    data: data
  })
}

// 查看详情
export const getDetailVersion = (jvsAppId, versionId) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/version/detail/${versionId}`,
    method: 'get'
  })
}

// 获取应用菜单下的资源集合
export const getAppMenuPermissionList = (jvsAppId) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/permission/operation`,
    method: 'get'
  })
}
// 获取应用下的列表页
export const getAppPageList = (jvsAppId) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/permission/page`,
    method: 'get'
  })
}
// 查询权限组集合
export const getAppPermissionGroupList = (jvsAppId, type) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/permission/group/list/${type}`,
    method: 'get'
  })
}
// 新增权限组基本信息
export const addAppPermissionGroup = (jvsAppId, data) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/permission/group`,
    method: 'post',
    data: data
  })
}
// 修改权限组基本信息
export const editAppPermissionGroup = (jvsAppId, data) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/permission/group`,
    method: 'put',
    data: data
  })
}
// 删除权限组基本信息
export const delAppPermissionGroup = (jvsAppId, groupId) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/permission/group/${groupId}`,
    method: 'delete'
  })
}
// 保存权限设置
export const saveAppPermissionGroup = (jvsAppId, groupId, data) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/permission/${groupId}`,
    method: 'put',
    data: data
  })
}
// 查询权限组的权限配置
export const getAppPermissionByGroup = (jvsAppId, groupId) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/permission/${groupId}`,
    method: 'get'
  })
}

// 查询自己发起的最近5条迭代任务进度
export const getMyTaskProcess = (params) => {
  return request({
    url: `/mgr/jvs-design/base/app/template/task/progress`,
    method: 'get',
    params: params
  })
}

// 查询进度详情
export const getTaskProcessDetail = (taskId) => {
  return request({
    url: `/mgr/jvs-design/base/app/template/task/progress/${taskId}/detail`,
    method: 'get'
  })
}

// 查询指定应用最近5条迭代进度
export const getAppTaskProcess = (appId, params) => {
  return request({
    url: `/mgr/jvs-design/app/design/${appId}/template/task/progress`,
    method: 'get',
    params: params
  })
}

// 应用-功能升级[轻应用版本迭代]  
export const upgradeVersion = (jvsAppId) => {
  return request({
    url: `/mgr/jvs-design/app/manage/${jvsAppId}/upgrade/feature/version`,
    method: 'put'
  })
}

// 获取模型可选字段类型
export const getFiledType = (appId) => {
  return request({
    url: `/mgr/jvs-design/app/design/${appId}/data/model/optional/field/type`,
    method: 'get',
  })
}

// 创建模型
export const addDataModel = (appId, data) => {
  return request({
    url: `/mgr/jvs-design/app/design/${appId}/data/model`,
    method: 'post',
    data: data
  })
}

// 修改模型
export const editDataModel = (appId, data) => {
  return request({
    url: `/mgr/jvs-design/app/design/${appId}/data/model`,
    method: 'put',
    data: data
  })
}

// 依据模型字段自动生成列表和表单设计
export const genCrudDesign = (appId, modelId) => {
  return request({
    url: `/mgr/jvs-design/app/design/${appId}/data/model/generate/crud/design/${modelId}`,
    method: 'post'
  })
}

// 应用推荐接口
export const starApplication = (appId, data) => {
  return request({
    url: `/mgr/jvs-design/app/manage/${appId}/recommendation?recommend=${data.recommend}`,
    method: 'put'
  })
}