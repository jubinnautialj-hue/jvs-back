import request from '@/router/axios'

export const getModelIdMessage  = (jvsAppId, modelId)=>{
  return request({
    url:`/mgr/jvs-design/app/design/${jvsAppId}/data/notice/${modelId}`,
    method:'get'
  })
}

// 保存消息模板--设计
export const saveModelMessage = (jvsAppId, data)=>{
  return request({
    url:`/mgr/jvs-design/app/design/${jvsAppId}/data/notice`,
    method: data.id ? 'put' : 'post',
    data
  })
}

// 保存消息模板--跳过权限
export const saveBaseModelMessage = (data)=>{
  return request({
    url:`/mgr/jvs-design/base/data/notice`,
    method:'post',
    data
  })
}

export const delModelMessage = (jvsAppId, id)=>{
  return request({
    url:`/mgr/jvs-design/app/design/${jvsAppId}/data/notice/${id}`,
    method:'delete'
  })
}

export const getDesignNode = (id)=>{
  return request({
    url:`/mgr/jvs-design/base/workflow/design/nodes/${id}`,
    method:'get'
  })
}

export const getModelDetail = (jvsAppId, id)=>{
  return request({
    url:`/mgr/jvs-design/app/design/${jvsAppId}/data/model/detail/${id}`,
    method:'get'
  })
}

// 获取消息发送类型列表
export const getMessageSendType = ()=>{
  return request({
    url:`/mgr/jvs-design/base/notice/type`,
    method:'get'
  })
}

// 获取公众号消息模板
export const getWxmpTemplateList = ()=>{
  return request({
    url:`/mgr/jvs-auth/tenant/admin/base/WECHAT_MP/message`,
    method:'get'
  })
}

// 获取短信消息模板
export const getSmsTemplateList = ()=>{
  return request({
    url:`/mgr/jvs-auth/tenant/admin/base/SMS/message?size=500&current=1`,
    method:'get'
  })
}

// 检查配置是否配置了如果有配置则可以使用，未配置，则不能使用
export const getEnableTypes = ()=>{
  return request({
    url:`/mgr/jvs-auth/tenant/admin/base/check`,
    method:'get'
  })
}
