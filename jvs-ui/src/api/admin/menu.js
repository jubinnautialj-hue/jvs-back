import request from "@/router/axios";
const base = "/mgr/jvs-auth";
// 收藏菜单
export const GetMenu = () => {
  return request({
    url: base + "/menu/favorites/list",
    method: "get"
  });
};


// 修改收藏菜单的顺序
export const sortMenuList = data => {
  return request({
    url: base + "/menu/favorites/list",
    method: "patch",
    data: data
  });
};

// 拖动排序
export const draggableMenuSort = (jvsAppId, data)=>{
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/JvsApp/sort`,
    method:'post',
    data
  })
}

// pc  移动端 显示隐藏
export const mobilePcDisplay = (jvsAppId, data)=>{
  return request({
    url:`/mgr/jvs-design/app/design/${jvsAppId}/JvsApp/display`,
    method:'put',
    params: data
  })
}
