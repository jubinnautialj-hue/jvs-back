import request from '@/router/axios'
let base = '/mgr/jvs-auth/'

// 获取帮助列表
export const getAppHelpList = (params) => {
  return request({
    // url: base + `help/all`,
    url: base + `platform/help/all`,
    method: 'get',
    params: params
  })
}

// 修改帮助
export const editHelp = (data) => {
  return request({
    url: base + `platform/help`,
    method: 'put',
    data: data
  })
}


// 新增帮助
export const addHelp = (data) => {
  return request({
    url: base + `platform/help`,
    method: 'post',
    data: data
  })
}


// 新增帮助
export const deleteHelp = (id) => {
  return request({
    url: base + `platform/help/${id}`,
    method: 'delete',
  })
}
