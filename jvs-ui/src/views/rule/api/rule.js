import request from '@/router/axios'

// 保存一个新的 逻辑
export function SavaJSON (jvsAppId, obj) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/rule`,
    method: 'put',
    params: obj
  })
}

// 获取所有逻辑  (分页)
export function getList (jvsAppId, obj) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/rule/page`,
    method: 'get',
    params: obj
  })
}

// 删除
export function delEdition (jvsAppId, id) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/rule/${id}`,
    method: 'delete'
  })
}

// 修改信息
export function editJSON (jvsAppId, id, obj) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/rule/${id}`,
    method: 'put',
    data: obj
  })
}

// 根据key获取一个逻辑的设计信息
export function getTemplatejson (jvsAppId, key) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/rule/${key}`,
    method: 'get'
  })
}

// 获取所有方法
export function getFuncList (data) {
  return request({
    url: `/mgr/jvs-design/app/design/${data.jvsAppId}/rule/custom/function/list`,
    method: 'post',
    data: data
  })
}

// 搜索方法
export function searchFuncList (data) {
  return request({
    url: `/mgr/jvs-design/app/design/${data.jvsAppId}/rule/custom/function/list/search`,
    method: 'post',
    data: data
  })
}

// 保存当前设计信息
export function saveOrUpdateDesign (data) {
  return request({
    url: `/mgr/jvs-design/app/design/${data.jvsAppId}/rule/design`,
    method: 'post',
    data: data
  })
}

// 测试运行逻辑 cron
export function getCronList () {
  return request({
    url: `/mgr/jvs-design/base/rule/cron`,
    method: 'get'
  })
}

// 获取类型资源信息
export function getClassType (key) {
  return request({
    url: `/mgr/jvs-design/base/rule/getClassType`,
    method: 'get'
  })
}

// 方法测试
export function testFunction (id, data) {
  return request({
    url: `/mgr/jvs-design/app/design/${data.jvsAppId}/rule/custom/function/test/${id}`,
    method: 'post',
    data: data
  })
}

// 逻辑测试
export function testLJ (jvsAppId, id, data) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/rule/test/${id}`,
    method: 'post',
    data: data
  })
}

// 获取逻辑测试结果
export function getTestLJ (jvsAppId, id) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/rule/test/${id}`,
    method: 'get'
  })
}

// 逻辑得 启用 和 禁用
export function enable (jvsAppId, id) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/rule/rule/design/enable/${id}`,
    method: 'get'
  })
}

// 分页查询某一个逻辑执行的日志服务
export function pageLog (jvsAppId, key, params) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/rule/log/page/${key}`,
    method: 'get',
    params: params
  })
}

// 获取信息
export function getLogInfo (jvsAppId, id) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/rule/log/by/${id}`,
    method: 'get',
  })
}

// 查询某一个逻辑执行的函数指标
export function pageTarget (jvsAppId, key, params) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/rule/log/index/${key}`,
    method: 'get',
    params: params
  })
}

// 保存逻辑变量
export function saveParameter (jvsAppId, data) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/rule/design/saveParameter`,
    method: 'post',
    data: data
  })
}

// 保存自定义配置参数
export function saveCustomOption (code, data) {
  return request({
    url: `/mgr/jvs-design/base/rule/customOption/${code}`,
    method: 'post',
    data: data
  })
}

// 获取所有逻辑  (分页)
export function getLogCount (jvsAppId, key) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/rule/log/count/${key}`,
    method: 'get',
  })
}

// 数据模型查询
export function getFuncLink (jvsAppId, type, id, field) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/rule/custom/function/link/${type}/${id}/${field}`,
    method: 'get',
  })
}

// 定义结构
export function jvsParamTypePost (data) {
  return request({
    url: `/mgr/jvs-design/base/rule/jvsParamType`,
    method: 'post',
    data: data
  })
}
// 获取定义结构字段类型
export function jvsParamTypeGet () {
  return request({
    url: `/mgr/jvs-design/base/rule/jvsParamTypes`,
    method: 'get'
  })
}

// 执行回放
export function runlogRevert (jvsAppId, key, params) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/rule/log/last/${key}`,
    method: 'get',
    params: params
  })
}

// 根据key获取一个逻辑的设计信息
export function getRunBind (jvsAppId, id, params) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/rule/bind/${id}`,
    method: 'get',
    params: params
  })
}

// 获取逻辑某个画布的某条数据
export function getCanvasRevert (jvsAppId, key, tid, canvasId, data) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/rule/log/${key}/tid/${tid}/${canvasId}`,
    method: 'post',
    data: data
  })
}

// 查询单条执行日志
export function getRunLogById (jvsAppId, id) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/rule/log/${id}`,
    method: 'get',
  })
}

// 复制节点
export function copyRuleNode (jvsAppId ,id, data) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/rule/custom/function/${id}/copy/node`,
    method: 'post',
    data: data
  })
}

// 脱敏正则列表
export function getEncryptionList() {
  return request({
    url: `/mgr/jvs-design/base/form/design/sensitive`,
    method: "get"
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

// 根据时间清除逻辑日志
export function clearRunLogByDateTime (jvsAppId, secret, params) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/rule/log/delete/${secret}`,
    method: 'delete',
    params: params
  })
}

// 根据id清除逻辑日志
export function clearRunLogById (jvsAppId, secret, id) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/rule/log/delete/${secret}/${id}`,
    method: 'delete',
  })
}

// 逻辑引用复制 。只引用当前应用下的逻辑。
export function placecRule(jvsAppId, id, formId) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/rule/copy/${id}/${formId}`,
    method: "put",
  })
}

// 获取当前应用逻辑分页
export const getRuleListByApp = (jvsAppId, params) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/rule/page`,
    method: 'get',
    params: params
  })
}

// 获取动态选择项
export const getSelectOptions = (jvsAppId, componentName, keyName)=>{
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/rule/select/${componentName}/${keyName}`,
    method: 'get',
  })
}