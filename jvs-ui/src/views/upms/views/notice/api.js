import request from "@/router/axios";

// 系统消息可用的字段
export function getNoticeFields() {
    return request({
        url: `/mgr/jvs-design/base/notice/fields`,
        method: "get",
    });
}

// 根据类型获取详情
export function getNoticeInfoByType(type) {
    return request({
        url: `/mgr/jvs-design/base/notice/system/detail`,
        method: "get",
        params: {type: type}
    });
}