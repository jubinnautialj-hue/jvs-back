import request from "@/router/axios";

// 获取数据集列表
export function getDataModel(param) {
  return request({
    url: "/mgr/jvs-design/base/tenant/admin/datamodel/page",
    method: "get",
    param: param
  });
}
// 删除数据集
export function deleteDataModel(param) {
  return request({
    url: `/mgr/jvs-design//app/design/${param.appId}/dynamic/data/${param.id}`,
    method: "delete",
  });
}
