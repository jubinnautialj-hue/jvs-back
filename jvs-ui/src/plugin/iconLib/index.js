import request from '@/router/axios'
import store from '@/store'
import {loadSVGStyle } from "@/util/util";
// 获取图标库
const getIcon = function() {
  return request({
    url: `/icon/all`,
    method: 'get',
  })
}
const getIconLib = function() {
  let allList = []
  getIcon().then(res => {
    if(res.data && res.data.code == 0 && res.data.data) {
      let data  = res.data.data
      store.commit('setLib', data)
      let tps = []
      data.forEach(ele => {
        if(["阿里云官方图标",'金融服务','未知图标库4','未知的图标库','未知的图标库2','未知的图标库3','任务管理'].indexOf(ele.label)==-1 || window.location.hash.indexOf('chart-design-ui/')==-1){
          tps.push(ele.label)
          if(ele.description){
            loadSVGStyle(ele.description,'js');
          }
          if(ele.value){
            loadSVGStyle(ele.value,'css');
          }
          allList = [...allList, ...[...ele.list]]
        }
      });
      store.commit('setAllType', tps)
      store.commit('setIconList', allList)
    }
  })
}
export default getIconLib
