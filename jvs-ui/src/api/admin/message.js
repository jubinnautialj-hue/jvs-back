import request from '@/router/axios'

export function messageaPage (query) {
  return request({
    url: '/mgr/message-push/station/inside/notice/page',
    method: 'get',
    params: query
  })
}

// 标记所有为已读
export const messageAllRead = ()=>{
  return request({
    url: `/mgr/message-push/station/inside/notice/all/read`,
    method: 'get'
  })
}

export function deleteMessage (id) {
  return request({
    url: `/mgr/jvs-auth/userlog/hide/${id}`,
    method: 'delete'
  })
}

export function readMessage (id) {
  return request({
    url: `/mgr/jvs-auth/userlog/read/${id}`,
    method: 'put'
  })
}

// 删除消息通知
export function deleteHideMessage (id) {
  return request({
    url: `/mgr/message-push/station/inside/notice/del/single/${id}`,
    method: 'delete'
  })
}