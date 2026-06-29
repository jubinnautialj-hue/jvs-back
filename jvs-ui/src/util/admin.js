import { getRedirectUri } from '@/api/login'
import store from "@/store"
export default {
  getScreen: function () {
    var width = document.body.clientWidth
    if (width >= 1200) {
      return 3 // 大屏幕
    } else if (width >= 992) {
      return 2 // 中屏幕
    } else if (width >= 768) {
      return 1 // 小屏幕
    } else {
      return 0 // 超小屏幕
    }
  }
}

export async function getRedirectInfo () {
  let access_token = store.getters.access_token
  let bool = false
  let url = ''
  if(!access_token) {
    await getRedirectUri().then(res => {
      if(res.data && res.data.code == 0 && res.data.data) {
        console.log(res.data.data)
        let type = res.data.data.type
        let back = res.data.data.domain
        if(back && !(back.startsWith('http'))) {
          back = location.protocol + '//' + back
        }
        let redirect_uri = encodeURIComponent(location.href.split(location.origin)[1])
        let callbackUrl = encodeURIComponent(`${back}/#/other/${type}?redirect_uri=${redirect_uri}`)
        url = `/auth/just/oauth2?stats=${type}&callbackUrl=${callbackUrl}`
        bool = true
      }else{
        bool = false
      }
    }).catch(e => {
      bool = false
    })
  }else{
    bool = false
  }
  return {stop: bool, url: url}
}