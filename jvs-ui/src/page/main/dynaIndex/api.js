import request from "@/router/axios"

// 获取当前这个业务项目有哪些组件,和组件属性
export const getCurrentAppComponents = (reqUrl)=>{
  return request({
    url:`/mgr/${reqUrl}/sys/home/component`,
    method:'get'
  })
}

// 获取当前组件渲染数据
export const getCurrentComponentOptions = (reqUrl, data)=>{
  return request({
    url: `/mgr/${reqUrl}/sys/home/get/data`,
    method: 'post',
    data: data
  })
}

// 获取首页门户列表
export const getIndexHome = ()=>{
  return request({
    url:`/mgr/jvs-design/sys/home/query/all`,
    method:'get',
  })
}

// 获取首页配置
export const getIndexSett = (params)=>{
  return request({
    url:`/mgr/jvs-design/sys/home/query`,
    method:'get',
    params: params
  })
}

// 保存首页配置
export const saveIndexSett = (data)=>{
  return request({
    url:`/mgr/jvs-design/sys/home/save`,
    method:'post',
    data
  })
}

// 获取组件选项数据
export const getComponentDicData = (params, data)=>{
  return request({
    url:`/mgr/jvs-design/sys/home/type`,
    method:'post',
    params,
    data
  })
}

// 删除首页配置
export const delIndexSett = (id)=>{
  return request({
    url:`/mgr/jvs-design/sys/home/delete/${id}`,
    method:'delete',
  })
}

// 门户排序
export const sortHomeList = (data)=>{
  return request({
    url:`/mgr/jvs-design/sys/home/sort`,
    method:'post',
    data
  })
}

// 获取模型字段的设计
export const getModelFields = (modelIdentification, data)=>{
  return request({
    url:`/mgr/jvs-design/base/identification/mappings/field/${modelIdentification}`,
    method:'post',
    data
  })
}
