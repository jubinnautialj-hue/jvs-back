<template>
  <div
    class="jvs-logo"
    @mouseover="showCardTip(true)"
    :style="
      'width: '+ $store.getters.theme.logo.width+
      ';height:calc( '+ $store.getters.theme.logo.height+ ' - 0px )' +
      ';line-height:'+ $store.getters.theme.logo.height+
      ';color:'+ $store.getters.theme.logo.color+
      ';font-size:'+ $store.getters.theme.logo.fontSize+
      ';font-weight:'+ $store.getters.theme.logo.fontWeight+
      ';background-color:'+ $store.getters.theme.logo.backgroundColor+';'
    "
  >
    <div v-if="userInfo && userInfo.tenant && (keyCollapse ? userInfo.tenant.icon : userInfo.tenant.logo)" class="logo-image" :style="'background-image:url('+ (keyCollapse ? userInfo.tenant.icon : userInfo.tenant.logo) +')'" @click="indexgo"></div>

    <el-dialog
      :title="bulletin.title + '公告'"
      :visible.sync="dialogVisible"
      width="800px"
      append-to-body
      :before-close="handleClose">
      <div style="max-height: 800px;overflow-y: auto">
        <div>
          <img :src="bulletin.banner" alt="" style="display: block;width: 100%;">
        </div>
        <section v-html="bulletin.content"></section>
      </div>
    </el-dialog>
    <div class="dialog-box" v-if="imgVisible" @click="imgVisible = false">
      <img :src="bulletin.content" alt=""/>
    </div>
  </div>
</template>

<script>
import { mapGetters } from "vuex";
import { getApplicationMenu } from '@/components/template/api'
import { getBulletin } from '@/api/login'
import {client_id} from "@/const/const";
export default {
  name: "logo",
  imgSrc: "",
  title: "",
  props: {
    favList: {
      type: Array,
      default: () => {
        return []
      }
    },
    systemId: {
      type: String
    },
    thisSystem: {
      type: Object
    },
    changeModeUserRadom: {
      type: Number
    },
    justAppInfo: {
      type: Boolean
    }
  },
  data () {
    return {
      ops: {
        vuescroll: {},
        scrollPanel: {},
        rail: {
          keepShow: true
        },
        bar: {
          hoverStyle: true,
          onlyShowBarOnScroll: true, //是否只有滚动的时候才显示滚动条
          background: "#000000",//滚动条颜色
          opacity: 0,//滚动条透明度
          "overflow-x": "hidden"
        }
      },
      imgSrc: this.imgSrc,
      title: this.title,
      cardTip: false,
      cardTipList: [],
      menu: [], // 收藏列表
      timer: null,
      sysInfo: {},
      dialogVisible: false,
      imgVisible: false,
      bulletin: {},
    };
  },
  created () {
    if(this.$route.path != '/myiframe/urlPath' && this.$route.query.login != 'isLogin') {
      getBulletin(client_id).then(res => {
        if (res.data && res.data.code == 0) {
          this.bulletin = res.data.data || {}
          console.log(this.bulletin)
          if(this.bulletin.content && this.bulletin.content.length > 0) {
            this.bulletin.content = this.bulletin.content.replace('<img ', `<img style="max-width: 710px;"`)
            if(this.bulletin.contentType === 'TEXT') {
              this.dialogVisible = true
            }else {
              this.imgVisible = true
            }
          }
        }
      })
    }
  },
  computed: {
    ...mapGetters(["website", "keyCollapse", "menuAll", "system", "userInfo"]), //  "menu"
  },
  methods: {
    async getAllMenuList () {
      const arr = [...this.menuAll]
      let appitem = null
      let index = arr.findIndex(item => {
        if(this.system === item.id && item.extend && item.extend.type == 'jvsapp') {
          appitem = item
        }
        return this.system === item.id
      })
      // 未匹配先查询，否则打开第一个
      if(index == -1 && !this.justAppInfo) {
        if(this.changeModeUserRadom > -1) {
          if(arr.length > 0) {
            index = 0
            appitem = arr[0]
            this.$emit('setAppItem', appitem)
            this.$emit('setAppItem', appitem, 'changeModeUser')
            this.$emit('childMenu', appitem.children)
          }
        }else{
          getApplicationMenu(this.system).then(res => {
            if (res.data && res.data.code == 0 && res.data.data) {
              this.menuAll.push(res.data.data)
              const arr = [...this.menuAll]
              this.$store.commit("SET_MENU_ALL", arr)
              appitem = this.menuAll[arr.length-1]
            }else{
              if(arr.length > 0) {
                index = 0
                appitem = arr[0]
              }
            }
            this.$emit('setAppItem', appitem)
            this.$emit('childMenu', appitem.children)
          }).catch(e => {
            if(arr.length > 0) {
              index = 0
              appitem = arr[0]
              this.$emit('setAppItem', appitem)
              this.$emit('childMenu', appitem.children)
            }
          })
        }
      }
      if(index > -1) {
        this.cardTipList = (this.menuAll[index] && this.menuAll[index].children) ? this.menuAll[index].children : []
      }
      for (let i in this.cardTipList) {
        if (this.cardTipList[i].childList && this.cardTipList[i].childList.length > 0) {
          for (let j in this.cardTipList[i].childList) {
            if (this.isCollect(this.cardTipList[i].childList[j])) {
              this.cardTipList[i].childList[j].collected = true
            } else {
              this.cardTipList[i].childList[j].collected = false
            }
          }
        }
      }
      if(this.cardTipList) {
        this.$emit('childMenu', this.cardTipList)
      }
      if(index > -1) {
        this.$emit('setAppItem', appitem)
      }
      this.$forceUpdate()
    },
    // 跳转主页
    indexgo () {
      this.$emit('closeOther', true)
      if(this.$route.path == '/wel/index') {
        return false
      }
      this.$router.push({
        path: "/"
      })
    },
    // 显示悬浮卡片
    showCardTip (bool) {
      if(this.timer) {
        clearTimeout(this.timer)
      }
      if(bool) {
        let _this = this
        this.timer = setTimeout( () => {
          _this.cardTip = bool
        }, 500)
      }else{
        this.cardTip = bool
      }
    },
    // 打开对应项
    openItem (item, parent) {
      this.$router.push({
        path: this.$router.$jvsRouter.getPath({
          name: item.name,
          src: item.url
        }),
        query: item.query
      })
      this.$emit('childMenu', this.cardTipList) //  parent)
      this.showCardTip(false)
    },
    // 是否被收藏
    isCollect (obj) {
      for (let i in this.menu) {
        if (obj.id == this.menu[i].id) {
          return true
        }
      }
      return false
    },
    handleClose() {
      this.dialogVisible = false
    },
  },
  watch: {
    systemId: {
      handler (newVal, oldVal) {
        if(newVal != oldVal && oldVal != -1) {
          this.getAllMenuList()
        }
      }
    },
    thisSystem: {
      handler (newVal, oldVal) {
        if(newVal) {
          this.sysInfo = newVal
        }
      }
    },
    needFresh: {
      handler (newVal, oldVal) {
        if(newVal != -1) {
          this.getAllMenuList()
        }
      }
    }
  }
};
</script>
<style lang="scss" scoped>
.dialog-box{
  position: fixed;
  top: 0;
  left: 0;
  height: 100vh;
  width: 100vw;
  z-index: 9999;
  text-align: center;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 6px;
  img{
    max-width: 800px;
    min-width: 400px;
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translateY(-50%) translateX(-50%);
    z-index: 10000;
  }
}
</style>
<style lang="scss">
.fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter-active {
  transition: opacity 2.5s;
}
.fade-enter,
.fade-leave-to {
  opacity: 0;
}
.jvs-logo {
  position: fixed;
  top: 0;
  left: 0;
  height: 64px;
  line-height: 64px;
  background-color: #fff;
  font-size: 20px;
  overflow: hidden;
  box-sizing: border-box;
  color: #1890ff;
  z-index: 1024;
  display: flex;
  justify-content: center;
  align-items: center;
  &_title {
    display: block;
    text-align: center;
    font-size: 16px;
    cursor: pointer;
  }
  &_subtitle {
    display: block;
    text-align: center;
    font-size: 18px;
    font-weight: bold;
    color: #fff;
  }
  .logo-image{
    display: block;
    width: 200px;
    height: 50px;
    overflow: hidden;
    cursor: pointer;
    background-size: contain;
    background-repeat: no-repeat;
    background-position: center;
    // margin-right: 5px;
  }
  .jvs-logo_title{
    line-height: 20px;
  }
}
.cardtipsbox {
  position: fixed;
  height: 100%;
  top: 0;
  left: 0;
  width: 100%;
  background: rgba(0, 0, 0, 0);
  box-shadow: 3px 0px 6px #888888;
  .cardtipsbox-top {
    background: #fff;
    // display: flex;
    span {
      display: block;
      text-align: center;
      background: #c5c5c5;
    }
  }
  .ulList {
    background: #fff;
    // width: 60%;
    box-shadow: 0 0 10px #ccc;
    min-height: 100%;
    // overflow-y: scroll;
    height: 100%;
    padding: 20px 40px 60px 40px;
    h3 {
      font-size: 17px;
      color: #333;
      line-height: 20px;
      margin: 0;
      margin: 15px 0px 15px 0px;
    }
    ul {
      display: flex;
      flex-wrap: wrap;
      padding: 0;
      margin: 0;
    }
  }
  ul,
  li {
    list-style: none;
    background: #fff;
    li {
      line-height: 18px;
      display: flex;
      align-items: center;
      width: 25%;
      margin: 0px 0 0px 0px;
      display: flex;
      align-items: center;
      div {
        padding: 8px;
        /*border-radius: 10px;*/
        width: 80%;
        display: flex;
        justify-content: space-between;
      }
      div:hover {
        background: #f5f5f5;
        a {
          color: #000;
          cursor: pointer;
        }
        i {
          display: block !important;
        }
      }
    }
    a {
      font-size: 12px;
      color: #666;
    }
    i {
      margin-left: 5px;
      color: #c5c5c5;
      cursor: pointer;
    }
    .collected {
      color: #1890ff;
    }
  }
}
</style>
