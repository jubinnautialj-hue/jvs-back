import request from "@/router/axios";

// 分页查询网关加密忽略列表
export const getGatewayIgnoreIpList=(params) => {
  return request({
    url: `/mgr/jvs-auth/platform/GatewayIgnoreIp/page`,
    method: 'get',
    params: params
  })
}

// 刷新网关加密忽略
export const refreshGatewayIgnoreIp=(params) => {
  return request({
    url: `/mgr/jvs-auth/platform/GatewayIgnoreIp/refresh`,
    method: 'get',
  })
}

// 新增网关加密忽略
export const addGatewayIgnoreIp=(data) => {
  return request({
    url: `/mgr/jvs-auth/platform/GatewayIgnoreIp/save`,
    method: 'post',
    data: data
  })
}

// 编辑网关加密忽略
export const editGatewayIgnoreIp=(data) => {
  return request({
    url: `/mgr/jvs-auth/platform/GatewayIgnoreIp/edit`,
    method: 'put',
    data: data
  })
}

// 删除网关加密忽略
export const delGatewayIgnoreIp=(id) => {
  return request({
    url: `/mgr/jvs-auth//platform/GatewayIgnoreIp/del/${id}`,
    method: 'delete',
  })
}
