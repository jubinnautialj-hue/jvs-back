<template>
  <div id="app">
    <div class="theme-box">
      <router-view @changeTheme="changeThemeHandle" />
    </div>
    <div id="jvs-large-file-upload" v-if="isUpload" :style="{display:!showUpload?'none':''}">
      <div class="jvs-large-file-upload-title">
        <span>文件上传</span>
        <i class="el-icon-close" @click="closeDialog"></i>
      </div>
      <div class="upload-container">
        <div class="upload-file-item" v-for="(item,index) in isPercentage" :key="index">
          <div class="upload-item-title">
            <span class="name-hidden"><i class="el-icon-document"></i>{{item.name}}</span>
            <span v-if="!item.isFinish" :style="{color:item.isfail?'#f56c6c':''}">{{item.percentage | parseIntPer}}%</span>
            <span v-else :style="{color:item.isfail?'#f56c6c':''}">{{item.isfail?'失败':'成功'}}</span>
          </div>
          <el-progress :percentage="item.percentage" :show-text="false" :status="item.isfail?'exception':null" :stroke-width="5"></el-progress>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  import { mapGetters, mapMutations } from 'vuex'
  import {simpleStyle, darkblueStyle, darkredStyle} from '@/const/newTheme' // '@/const/theme'
  import { getStore } from "@/util/store.js";
  import '../public/jvs-ui-public/fonts/font.scss';
  // constants
  import * as globalTypes from '@/store/types/global'
  import bus from '@/util/vuebus'
  import {getTenantInfo} from '@/api/admin/home'
  import { getDomain } from "@/api/login"
  import warterMark from "@/util/waterMark"
  export default {
    name: 'app',
    data() {
      return {
        themeStyle: document.createElement("style"),
        showUpload:true,
        uploadList:[],
        channelBroad:null,
        isFinish:null,
        isClose:false,
      }
    },
    filters:{
      parseIntPer(val){
        return parseInt(val)
      }
    },
    watch: {},
    created() {
      // 切换租户了设置主题
      if(window.self == window.top) {
        if(getStore({name: 'tenantId'}) && !getStore({name: 'tenantInfo'})) {
          getTenantInfo().then(res => {
            if(res.data.code == 0) {
              this.$store.commit("SET_TENANTINFO", res.data.data)
              if(!document.title) {
                document.title = res.data.data.systemName || res.data.data.shortName
              }
              if(res.data.data.themeStyle) {
                let temp = JSON.parse(res.data.data.themeStyle)
                if(temp.themeStyle) {
                  let obj = JSON.parse(temp.themeStyle)
                  let name = obj.name
                  let params = obj.params
                  this.$store.commit("SET_THEME_NAME", name)
                  this.$store.commit("SET_THEME", params)
                }
              }
              this.getDomainHandle()
              // 默认设置深蓝主题
              if(!getStore({name: 'themeName'})) {
                this.$store.commit("SET_THEME_NAME", darkblueStyle.theme)
                this.$store.commit("SET_THEME", darkblueStyle.params)
              }
              this.changeTheme()
            }
          }).catch(e => {
            // 默认设置深蓝主题
            if(!getStore({name: 'themeName'})) {
              this.$store.commit("SET_THEME_NAME", darkblueStyle.theme)
              this.$store.commit("SET_THEME", darkblueStyle.params)
            }
            this.changeTheme()
          })
        }else{
          // 默认设置深蓝主题
          if(!getStore({name: 'themeName'})) {
            this.$store.commit("SET_THEME_NAME", darkblueStyle.theme)
            this.$store.commit("SET_THEME", darkblueStyle.params)
          }
          this.changeTheme()
        }
        if(!document.title) {
          if(getStore({name: 'tenantInfo'})) {
            document.title = getStore({name: 'tenantInfo'}).systemName || getStore({name: 'tenantInfo'}).shortName
          }
        }
      }
      this.$consoleImage()
      window.addEventListener('beforeunload', e => this.beforeunloadFn(e))
    },
    methods: {
      ...mapMutations({
        onChangeTheme: globalTypes.UPDATE_THEME,
      }),
      // 全局 logo样式
      setLogoStyle () {
        let logoStyle =
        `
          #app {
            font-size: ${this.$store.getters.theme.font.size};
            color: ${this.$store.getters.theme.font.color};
          }
          .jvs-left .jvs-logo:hover{
            color: ${this.$store.getters.theme.basic.activeFont};
            // background: ${this.$store.getters.theme.basic.activeColor}!important;
          }
          .cardtipsbox .ulList{
            background: ${this.$store.getters.theme.basic.themeColor};
            color: ${this.$store.getters.theme.basic.fontColor};
          }
          .cardtipsbox .ulList div h3{
            color: ${this.$store.getters.theme.basic.fontColor};
          }
          .cardtipsbox .ulList div ul, .cardtipsbox .ulList div ul li {
            background: ${this.$store.getters.theme.basic.themeColor};
          }
          .cardtipsbox .ulList div ul li div a, .cardtipsbox .ulList div ul li div i{
            color: ${this.$store.getters.theme.basic.fontColor};
          }
          .cardtipsbox .ulList div ul li:hover div{
            background: ${this.$store.getters.theme.basic.activeColor};addThemeIE

          }
          .cardtipsbox ul li div:hover a {
            color: #fff !important
          },
          .cardtipsbox .ulList div ul li div .collected{
            color: ${this.$store.getters.theme.basic.activeFont};
          }
        `
        return logoStyle
      },
      // 菜单样式
      setMenuStyle () {
        let menuStyle =
        // 正常状态
        `
          .divider-line{
            background: ${this.$store.getters.theme.basic.fontColor};
          }
          .el-menu-scrollbar {
            background: ${this.$store.getters.theme.basic.themeColor};
            color: ${this.$store.getters.theme.basic.fontColor};
          }
          .el-menu-scrollbar .el-submenu__title i, .el-menu-scrollbar .el-submenu__title span{
            color: ${this.$store.getters.theme.basic.fontColor};
            font-size: 15px;
          }
          .el-menu-scrollbar .el-menu .el-menu-item span{
            color: ${this.$store.getters.theme.basic.fontColor};
          }
          .el-menu-scrollbar .el-menu .el-menu-item i {
            color: ${this.$store.getters.theme.basic.fontColor};
          }
          .el-menu-scrollbar .el-menu .el-menu-item::before {
            background: ${this.$store.getters.theme.basic.themeColor};
          }
          .el-menu-scrollbar .el-menu .el-menu-item:hover, .el-menu-scrollbar .el-menu .el-menu-item:focus{
            background: ${this.$store.getters.theme.basic.activeColor};
          }
          .el-menu-scrollbar .el-menu .el-menu-item:hover::before{
            background: ${this.$store.getters.theme.basic.activeBefore};
          }
          .el-menu-scrollbar .el-menu .el-menu-item:hover span, .el-menu-scrollbar .el-menu .el-menu-item:focus span {
            color: ${this.$store.getters.theme.basic.activeFont};
          }
          .el-menu-scrollbar .el-submenu__title:hover {
            background: ${this.$store.getters.theme.basic.activeColor};
          }
          .el-menu-scrollbar .el-submenu__title:hover span{
            color: ${this.$store.getters.theme.basic.activeFont};
          }
          .el-menu-scrollbar .el-menu .el-menu-item:hover i, .el-menu-scrollbar .el-menu .el-menu-item:focus i{
            color: ${this.$store.getters.theme.basic.activeBefore};
          }
          .el-menu-scrollbar .el-submenu__title:hover i:not(.el-submenu__icon-arrow){
            color: ${this.$store.getters.theme.basic.activeBefore};
          }
          .el-menu--vertical{
            background: ${this.$store.getters.theme.basic.themeColor};
            color: ${this.$store.getters.theme.basic.fontColor};
          }
          .el-menu--vertical .el-menu-item span, .el-menu--vertical .el-menu-item i{
            color: ${this.$store.getters.theme.basic.fontColor};
          }
          .el-menu--vertical .el-menu-item:hover i, el-menu--vertical .el-menu-item:hover span,
          .el-menu--vertical .el-menu-item:focus i, el-menu--vertical .el-menu-item:focus span {
            color: ${this.$store.getters.theme.basic.fontColor};
          }
          .el-menu--vertical .el-menu-item:hover::before {
            background: ${this.$store.getters.theme.basic.activeBefore || this.$store.getters.theme.basic.fontColor};
          }
          .el-menu--vertical .el-menu-item:hover, .el-menu--vertical .el-menu-item:focus {
            background: ${this.$store.getters.theme.basic.activeColor};
          }
          .el-menu-scrollbar .el-menu .is-active-item, .jvs-sidebar .el-submenu .el-menu-item.is-active-item{
            background: ${this.$store.getters.theme.basic.activeColor};
          }
          .el-menu-scrollbar .el-menu .is-active-item::before {
            background: ${this.$store.getters.theme.basic.activeBefore || this.$store.getters.theme.basic.fontColor};
          }
          .el-menu-scrollbar .el-menu .is-active-item, .jvs-sidebar .el-submenu .el-menu-item.is-active-item span{
            color: ${this.$store.getters.theme.basic.activeFont};
          }
          .jvs-sidebar .el-submenu .el-menu-item.is-active-item i{
            color: ${this.$store.getters.theme.basic.activeBefore || this.$store.getters.theme.basic.fontColor};
          }
        `
        return menuStyle
      },
      // 其他样式
      setOtherStyle () {
        // 顶部系统栏
        // 个人中心
        // 顶部菜单栏
        // 面包屑
        let otherStyle =
        `
          .jvs-tags .jvs-tags__box .lineBox{
            background-color: ${this.$store.getters.theme.topNav.backgroundColor};
            color: ${this.$store.getters.theme.topNav.fontColor};
          }
          .jvs-tags .jvs-tags__box .lineBox .system-list li span, .jvs-tags .jvs-tags__box .lineBox .system-list li i{
            color: ${this.$store.getters.theme.topNav.fontColor};
          }
          .jvs-tags .jvs-tags__box .lineBox .system-list .activeSysItem{
            color: ${this.$store.getters.theme.topNav.activeColor};
            background-color: ${this.$store.getters.theme.topNav.activeBackgroundColor};
          }
          .jvs-tags .jvs-tags__box .lineBox .system-list .activeSysItem span{
            color: ${this.$store.getters.theme.topNav.activeColor};
          }
          .el-dropdown-menu .el-dropdown-menu__item:hover {
            background-color: ${this.$store.getters.theme.basic.activeColor};
            color: ${this.$store.getters.theme.basic.activeFont};
          }
        `
        return otherStyle
      },
      // 加入主题样式
      addTheme (themeStyle) {
        // 菜单
        themeStyle.appendChild(document.createTextNode(this.setMenuStyle()))
        // logo
        themeStyle.appendChild(document.createTextNode(this.setLogoStyle()))
        // 其他
        themeStyle.appendChild(document.createTextNode(this.setOtherStyle()))
      },
      // 加入主题兼容ie
      addThemeIE (themeStyle) {
        // 菜单
        themeStyle.innerHTML = this.setMenuStyle()
        // logo
        themeStyle.innerHTML= this.setLogoStyle()
        // 其他
        themeStyle.innerHTML = this.setOtherStyle()
      },
      // 切换主题
      changeTheme () {
        if(!this.$store.getters.theme.basic) {
          return false
        }
        let head = document.getElementsByTagName("head")[0]
        head.appendChild(this.themeStyle)
        if(this.themeStyle) {
          head.removeChild(this.themeStyle)
        }
        this.themeStyle = document.createElement("style")
        this.themeStyle.type = 'text/css'
        try{
          this.addTheme(this.themeStyle)
        }catch(ex){
          this.addThemeIE(this.themeStyle)
        }
        head.appendChild(this.themeStyle)
      },
      // 切换通知
      changeThemeHandle (bool) {
        // 默认设置深蓝主题
        if(!getStore({name: 'themeName'})) {
          this.$store.commit("SET_THEME_NAME", darkblueStyle.theme)
          this.$store.commit("SET_THEME", darkblueStyle.params)
        }else{
          this.$store.commit("SET_THEME_NAME", darkblueStyle.theme)
          this.$store.commit("SET_THEME", darkblueStyle.params)
        }
        if(bool) {
          this.changeTheme()
        }
      },
      fresh () {
        location.reload()
      },
      goBack () {
        history.pushState(null, null, document.URL)
      },
      // 获取域名对应设置信息
      getDomainHandle () {
        getDomain().then(res => {
          if(res.data && res.data.code == 0) {
            if(res.data.data){
              if(res.data.data.icon) {
                var link = document.createElement('link')
                link.type = 'image/x-icon'
                link.rel = 'shortcut icon'
                link.href = res.data.data.icon
                document.getElementsByTagName('head')[0].appendChild(link);
              }
              if(res.data.data.kkfileUrl) {
                this.$store.commit("SET_KKFILE_URL", res.data.data.kkfileUrl)
              }
            }
          }
        })
      },
      beforeunloadFn (e) {
        this.$store.commit("DEL_TAG_OTHER");
      },
      closeDialog(){
        if(this.isFinish){
          this.isClose = true
          let myInfo = getStore({ name: "userInfo" }) || {}
          this.channelBroad.postMessage({type:'uploadFileClose',data:{ userId: myInfo?.id }},"*")
        }
        this.showUpload = false
      },
      receiveMessage(event){
        let myInfo = getStore({ name: "userInfo" }) || {};
        if(event.data.data.userId != myInfo.id) return
        this.isClose = false
        this.showUpload = true
        switch(event.data.type){
          case "uploadFile":
            this.uploadList = event.data.data.fileList || []
            this.isFinish = event.data.data.isFinish || false
            if(this.isFinish){
              this.closeDialog()
            }
            break
          case 'uploadFileClose':
            if(this.isFinish){
              this.isClose = true
            }
            this.showUpload = false
            break
          case 'default':
            console.log(event)
            break
        }
      },
    },
    computed: {
      ...mapGetters({
        theme: globalTypes.GET_THEME,
      }),
      perTop(){
        let perSum = 0,curPer = 0
        let newIsPercentage = this.isPercentage.filter(item=>{return !item.isfail})
        newIsPercentage.forEach(element => {
          perSum+=100
          curPer+=element.percentage
        });
        return (100 - parseInt(curPer/perSum*100)) +'%'
      },
      isPercentage(){
        return this.uploadList.filter(item=>{return item.percentage<=100})
      },
      isUpload(){
        return this.isPercentage.length>0 && !this.isClose
      },
    },
    mounted () {
      bus.$on('refresh', (e) => {
        if(e) {
          this.fresh()
        }
      })
      if(window.history && window.history.pushState) {
        history.pushState(null, null, document.URL)
        window.addEventListener('popstate', this.goBack, false)
      }
      if(window.self != window.top) {
        this.showUpload = false
        this.channelBroad = new BroadcastChannel('upload-tree')
        this.channelBroad.addEventListener("message",this.receiveMessage,true)
      }else{
        let myInfo = getStore({ name: "userInfo" }) || {};
        let tenantInfo = getStore({name: 'tenantInfo'}) || {}
        if(tenantInfo.watermark && (myInfo.realName || myInfo.accountName)) {
          warterMark.set(myInfo.realName || myInfo.accountName)
        }else{
          warterMark.clear()
        }
      }
    },
    destroyed () {
      window.removeEventListener('popstate', this.goBack, false)
      window.removeEventListener('beforeunload', e => this.beforeunloadFn(e))
      if(window.self != window.top) {
        this.channelBroad.removeEventListener("message",this.receiveMessage,true)
      }
    },
    watch: {
      '$route': {
        handler(to, from){},
        deep: true, // 深度监听
        immediate: true, // 第一次初始化渲染就可以监听到
      }
    }
  }
</script>
<style lang="scss">
  #app {
    width: 100%;
    height: 100vh;
    overflow: hidden;
  }
  ul, li {
    list-style: none;
  }
  // .jvs-contail-font *, .el-dialog__wrapper *, .el-popover *, .el-tooltip__popper *{
  //   font-family: Source Han Sans, Source Han Sans;
  // }
  .ace_editor *{
    font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', 'source-code-pro', monospace;
  }
</style>
<style lang="scss" scoped>
  .theme-text{
    position: fixed;
    right: 0;
    top: 50%;
    z-index: 9999999999999;
  }
  .theme-box {
    width: 100%;
    height: 100%;
  }
  #jvs-large-file-upload{
    position: absolute;
    bottom: 0px;
    right: 0px;
    transition-duration: 0.3s;
    width: 400px;
    min-height: 100px;
    z-index: 999999;
    box-shadow: 0px 0px 10px #888888;
    background-color: #ffffff;
    border-top-left-radius: 5px;
    border-bottom-left-radius: 5px;
    overflow: hidden;
    transition: all 0.1s linear;
    .jvs-large-file-upload-title{
      width: 100%;
      height: 30px;
      background: white;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0px 10px;
      box-sizing: border-box;
      border-bottom: 1px solid #efefef;
      i{
        cursor: pointer;
      }
    }
    .upload-container{
      overflow: auto;
      height: 100%;
      .upload-file-item{
        padding: 0px 10px;
        .upload-item-title{
          display: flex;
          padding:5px 0px;
          justify-content: space-between;
          font-size: 14px;
          .name-hidden{
            width: calc(100% - 30px);
            height: 24px;
            display: inline-block;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: pre;
          }
          i{
            padding: 0px 5px 0px 0px;
          }
        }
      }
    }
  }
</style>
