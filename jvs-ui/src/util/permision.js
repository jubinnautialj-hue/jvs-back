import store from '@/store'
import { getStore } from "@/util/store.js";
export default {
  install (Vue, options) {
    // 权限匹配
    /**
     *
     * @param {String} str 权限标识
     */
    Vue.prototype.$permissionMatch = function (str) {
      let t = false;
      let permissionsList = getStore({name: 'permissions'})
      if(!permissionsList) {
        return false
      }
      if(permissionsList.indexOf(str) == -1) {
        if (!str || str == '') {
          t = true;
        }else {
          t = false
        }
      }else {
        t = true
      }
      return t
    }
    // 根据权限标识返回对应中文名
    /**
     *
     * @param {String} flag 权限标识
     * @param {String} text 默认文字，即匹配失败或无需匹配时的显示
     */
    Vue.prototype.$permissionLabel = function (flag, text) {
      let temp = text
      if(store.state.user && store.state.user.permissions) {
        let keys = Object.keys(store.state.user.permissions)
        if(keys.indexOf(flag) > -1 && store.state.user.permissions[flag]) {
          temp = store.state.user.permissions[flag]
        }
      }
      return temp
    }
    // 校验功能名是否支持
    /**
     *
     * @param {String} name 功能名
     */
    Vue.prototype.$functionEnable = function (name) {
      // 如果没有licence标识直接跳过
      let permissionsList = getStore({name: 'permissions'})
      if(permissionsList.indexOf('jvs_license') == -1) {
        return true
      }
      return store.getters.functionsObj[name] > -1
      // return true
    }
  }
}
