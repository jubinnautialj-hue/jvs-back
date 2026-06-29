import request from '@/router/axios'

// 获取表单设计详情
export const getFlowableForm=(jvsAppId, id) => {
  return request({
    url: `/mgr/jvs-design/base/workflow/${jvsAppId}/form/${id}`,
    method: 'get'
  })
}

// 启动流程
export const startProcess=(data, formId) => {
  return request({
    url: `/mgr/jvs-design/base/workflow/task/start`,
    method: 'post',
    data: data,
    headers: {
      'designId': formId
    }
  })
}

// 重启流程
export const restartProcess=(data, headers) => {
  return request({
    url: `/mgr/jvs-design/base/workflow/task/restart`,
    method: 'post',
    data: data,
    headers: headers
  })
}

// 查询任务详情
export const getTaskInfo=(id) => {
  return request({
    url: `/mgr/jvs-design/base/workflow/task/${id}`,
    method: 'get'
  })
}

// 完成任务
export const completeProcess=(data, headers) => {
  let tob = {
    url: `/mgr/jvs-design/base/workflow/task/execute`,
    method: 'post',
    data: data
  }
  if(headers) {
    tob.headers = headers
  }
  return request(tob)
}

// 获取流程统计数据
export const getTaskStatistic=() => {
  return request({
    url: `/mgr/jvs-design/base/workflow/task/statistic`,
    method: 'get'
  })
}

// 获取代理列表总数
export const getProxyNumber=() => {
  return request({
    url: `/mgr/jvs-design/base/workflow/proxy/total`,
    method: 'get'
  })
}

// 获取任务当前审批节点的下级审批节点集合
export const getNextNodeList=(taskId, nodeId, params) => {
  return request({
    url: `/mgr/jvs-design/base/workflow/task/node/next/${taskId}/${nodeId}`,
    method: 'get',
    params: params
  })
}

// 审批点击同意弹窗判断是否能动态加节点
export const getDynamicNode=(taskId, nodeId) => {
  return request({
    url: `/mgr/jvs-design/base/workflow/task/node/dynamic/node/${taskId}/${nodeId} `,
    method: 'get'
  })
}

// 保存任务
export const saveProcess=(taskId, data) => {
  return request({
    url: `/mgr/jvs-design/task/save/${taskId}`,
    method: 'post',
    data: data
  })
}

// 指派任务
export const zhipaiProcess=(taskId, userId) => {
  return request({
    url: `/mgr/jvs-design/task/assign/${taskId}/to/${userId}`,
    method: 'post'
  })
}

// 委派
export const weipaiProcess=(taskId, userId) => {
  return request({
    url: `/mgr/jvs-design/task/delegate/${taskId}/to/${userId}`,
    method: 'post'
  })
}
