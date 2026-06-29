import request from "@/router/axios";

// 分页查询网关加密忽略列表
export const getOpinionList=(params) => {
  return request({
    url: `/mgr/jvs-auth/platform/opinion/page`,
    method: 'get',
    params: params
  })
}
