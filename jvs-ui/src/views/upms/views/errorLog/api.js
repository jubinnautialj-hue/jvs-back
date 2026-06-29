import request from '@/router/axios'

// 操作日志分页查询
export const getLogs = (params) => {
  return request({
    url: `/mgr/jvs-auth/platform/request/page`,
    method: 'get',
    params:  params
  })
}

// 根据链路查询用户业务数据操作日志
export const getLogInfo=(tid) => {
  return request({
    url: `/mgr/jvs-auth/log/modification/${tid}`,
    method: 'get'
  })
}

// 待处理
export const getErrorList=() => {
  return request({
    url: `/mgr/jvs-auth/platform/request/pending/top20`,
    method: 'get'
  })
}

// 处理
export const dealErrorList=(id) => {
  return request({
    url: `/mgr/jvs-auth/platform/request/handler/${id}`,
    method: 'delete'
  })
}