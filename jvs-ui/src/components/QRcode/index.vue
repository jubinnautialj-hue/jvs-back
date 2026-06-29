<template>
  <div class='weixin'>
    <div
      class="weixincode"
      v-loading="URLLoading"
    >
      <img
        v-show="RQURL"
        :src="RQURL"
        @click="init"
      />
      <div class="tips-box">
        <div v-if="showContent.title">{{showContent.title}}</div>
        <div v-if="showContent.content">{{bottomtext ? bottomtext :  showContent.content}}</div>
      </div>
    </div>
  </div>
</template>

<script>
import { getQRcode, getCheck, appQrLogin, appCheck } from '@/api/login'
import { client_id } from "@/const/const"
export default {
  // import引入的组件需要注入到对象中才能使用
  components: {},
  props: {
    loginType: { type: String, default: "" }, ReqType: { type: String, default: '' },
    bottomtext: {
      type: String,
      default: ''
    }
  },
  data () {
    // 这里存放数据
    return {
      // 二维码拿到的
      RQURL: '',
      URLLoading: false,
      isReq: false,
      getStatusSetInterval: null,
      UUID: '',
      showContent: {
        title: '',
        content: '请使用微信扫码登录'
      },
      qrType: 'weixin'
    }
  },
  // 监听属性 类似于data概念
  computed: {},
  // 监控data中的数据变化
  watch: {},
  // 方法集合
  methods: {
    clear () {
      clearInterval(this.getStatusSetInterval)
    },
    init (type) {
      if (type) {
        this.qrType = type
      }
      this.showContent = {
        title: '',
        content: this.qrType == 'weixin' ? '请使用微信扫码登录' : '请使用APP扫码登录'
      }
      this.clear()
      this.isReq = false
      this.RQURL = `/auth/just/login/WECHAT_OPEN?client_id=${client_id}`
      // this.getQRcodeUrl()
    },
    getQRcodeUrl () {
      this.URLLoading = true
      if (this.qrType == 'weixin') {
        let query = {}
        if(this.$route && this.$route.query) {
          query = this.$route.query
        }
        getQRcode(query).then(res => {
          this.URLLoading = false
          if (res.data.code !== 500 && res.data.data) {
            this.RQURL = res.data.data.imgData
            this.UUID = res.data.data.wxUUID
            this.getStatusSetInterval = setInterval(() => {
              if (!this.isReq) {
                this.getLoginStatus()
              }
            }, 1000)
          }else{
            this.RQURL = 'https://tool.oschina.net/action/qrcode/generate?data=http%3A%2F%2F10.0.1.127%2F%23%2Flogin&output=image%2Fgif&error=L&type=0&margin=0&size=4'
          }
        }).catch(res => {
          console.log(res)
        })
      } else {
        appQrLogin().then(res => {
          this.URLLoading = false
          if (res.data.code !== 500) {
            this.RQURL = res.data.data.imgData
            this.UUID = res.data.data.appUUID
            this.getStatusSetInterval = setInterval(() => {
              if (!this.isReq) {
                this.getLoginStatus()
              }
            }, 3000)
          }
        })
      }
    },
    CheckImgExists (imgurl) {
      var ImgObj = new Image(); //判断图片是否存在  
      ImgObj.src = imgurl;
      //存在图片
      if (ImgObj.fileSize > 0 || (ImgObj.width > 0 && ImgObj.height > 0)) {
        return true;
      } else {
        return false;
      }
    },
    getLoginStatus () {
      this.isReq = true
      if (this.qrType == 'weixin') {
        let time = new Date().getTime()
        getCheck({ uuid: this.UUID, '_': time }).then(res => {
          if (res.data.status === 405) {
            this.isReq = true
            clearInterval(this.getStatusSetInterval)
            this.$emit("submit", res.data.result.code, 'weixin')
          } if (res.data.status === 403) {
            clearInterval(this.getStatusSetInterval)
            this.init(this.qrType)
          } else if (res.data.status === 404) {
            this.showContent = res.data.msg
            this.isReq = false
          } else {
            this.isReq = false
          }
        }).catch(res => {
          this.isReq = false
        })
      } else {
        appCheck({ uuid: this.UUID, }).then(res => {
          if (res.data.data.code == 0) {
            if (res.data.data.isLogin) {
              clearInterval(this.getStatusSetInterval)
              this.$emit("submit", {
                scanType: res.data.data.scanType,
                // userId: res.data.data.userId,
                uuid: this.UUID
              }, 'app')
            } else {
              this.isReq = false
            }
          } else {
            clearInterval(this.getStatusSetInterval)
            this.init(this.qrType)
          }
        }).catch(res => {
          this.isReq = false
        })
      }
    }
  }
}
</script>
<style lang='scss' scoped>
.weixinlogin {
  width: 0;
  height: 0;
  border-top: 100px solid #c0c4cc;
  border-left: 100px solid transparent;
  position: absolute;
  top: 0;
  right: 0;
}
.weixin {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 340px;
}
.weixincode {
  width: 300px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  img {
    display: block;
    width: 100%;
    height: 100%;
  }
}
.tips-box {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 100%;
  border-radius: 50px;
  height: 50px;
  box-sizing: border-box;
  color: #999;
}
</style>