import request from "@/router/axios";

// 根据邀请码获取 公司信息，邀请人名
export function getTenantInfo(params) {
  return request({
    url: '/gateway/invite',
    method: "get",
    params: params
  });
}

// 用户填写邀请码
export function inviteCode(code) {
  return request({
    url: `/mgr/jvs-auth/invite/${code}`,
    method: "put",
  });
}

// 创建组织
export function createTenant(data) {
  return request({
    url: `/mgr/jvs-auth//tenant/admin/base/tenant`,
    method: "post",
    data: data
  });
}

// 修改租户
export function editTenant(data) {
  return request({
    url: `/mgr/jvs-auth//tenant/admin/base/tenant`,
    method: "put",
    data: data
  });
}

// 获取租户
export function getTenant() {
  return request({
    url: `/mgr/jvs-auth//tenant/admin/base/tenant`,
    method: "get",
  });
}

// 获取配置信息
export function getSysConfig(type) {
  return request({
    url: `/mgr/jvs-auth/platform/sys/config/${type}`,
    method: "get",
  });
}

// 保存配置信息
export function saveSysConfig(data) {
  return request({
    url: `/mgr/jvs-auth/platform/sys/config/${data.type}`,
    method: "post",
    data: data
  });
}

// 获取哪些有配置,用于显示
export function getSysConfigInfo() {
  return request({
    url: `/mgr/jvs-auth/platform/sys/config/config`,
    method: "get",
  });
}