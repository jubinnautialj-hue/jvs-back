import request from '@/router/axios'

// 分页查询字典列表刷新二级缓存
export const refreshCache = () => {
  return request({
    url: `/mgr/jvs-auth/platform/develop/config/refresh/mybatis/cache`,
    method: 'get'
  })
}

// 查询其他配置信息
export const getOtherInfo = () => {
  return request({
    url: `/mgr/jvs-auth/platform/info/dev`,
    method: 'get'
  })
}

// 保存其他配置信息
export const saveOtherInfo = (data) => {
  return request({
    url: `/mgr/jvs-auth/platform/develop/config`,
    method: 'post',
    data: data
  })
}