import request from "@/router/axios"

// 获取最新公告
export function getNewBulletin() {
  return request({
    url: `/mgr/jvs-auth/platform/bulletin`,
    method: "get",
  });
}

// 获取统计数据
export function getStatisticsBlock() {
  return request({
    url: `/mgr/jvs-demo/index`,
    method: "get",
  });
}

// 获取统计数据
export function getStatisticsData() {
  return request({
    url: `/mgr/jvs-demo/index/statistics`,
    method: "get",
  });
}