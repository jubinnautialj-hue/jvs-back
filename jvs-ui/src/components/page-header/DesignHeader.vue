<template>
  <div class="design-header-box">
    <div class="header-left">
      <slot v-if="leftIconSlot" name="leftIcon"></slot>
      <div v-else>
        <svg v-if="type === 'rule'" t="1648178009751" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="1387"><path d="M491.660728 374.985915a115.010473 115.010473 0 1 0 115.010473 115.010474 115.010473 115.010473 0 0 0-115.010473-115.010474z m0 0" fill="#1296db" p-id="1388"></path><path d="M1022.70587 291.039364V213.009751h-92.821957a142.006501 142.006501 0 0 0-113.16143-113.16143V0h-77.659805v97.629469h-184.904298V0h-79.508848v97.629469h-184.904297V0H211.715621v100.587938a142.006501 142.006501 0 0 0-110.942578 110.942578H0.554913v79.508848h97.629469v184.904298H0.554913v77.289996h97.629469v184.904298H0.554913v77.289996h100.21813a142.006501 142.006501 0 0 0 110.942578 110.942579v94.671h78.029614V931.91766h184.904297v92.08234h77.289997V931.91766h184.904297v92.08234H814.133823v-94.671a142.006501 142.006501 0 0 0 110.942578-110.942579h93.191766v-78.029613H932.472573v-184.904298h90.603106v-79.508848H932.472573v-184.904298z m-269.960274 403.830986a38.977826 38.977826 0 0 1-56.210907 53.992055l-97.999277-99.108703a193.040087 193.040087 0 1 1 54.361863-55.101481z m0 0" fill="#1296db" p-id="1389"></path></svg>
        <svg v-else-if="type === 'flow'" aria-hidden="true">
          <use :xlink:href="'#' + 'icon-fenzhi'"></use>
        </svg>
        <svg v-else aria-hidden="true">
          <use :xlink:href="'#' + infoData.icon"></use>
        </svg>
      </div>
      {{ appName }}
      <i class="el-icon-arrow-right icon-style"/>
      <el-input v-if="showEdit" v-model="infoData.name" @blur="showEdit = false " size="mini" style="width:200px;"></el-input>
      <span v-else style="display: flex;align-items: center;">
        {{ infoData.name }}
        <svg aria-hidden="true" @click="showEdit = true" style="width: 16px;height: 16px;margin: 0 0 0 10px;cursor: pointer;">
          <use xlink:href="#icon-jvs-bianji1"></use>
        </svg>
      </span>
    </div>
    <div class="header-center">
      <el-tabs v-model="activeName" @tab-click="handleClick">
        <el-tab-pane v-for="(item, key) in tabList" :key="item.name+key" :name="item.name" v-if="item.display !== false">
          <span slot="label"><i :class="item.icon" style="margin-right: 6px;"/>{{ item.label }}</span>
        </el-tab-pane>
      </el-tabs>
    </div>
    <div class="header-right">
      <slot name="right"></slot>
      <jvs-button v-if="!(hasSave === false)" type="primary" :loading="saveLoading" @click="handleSave">保存ctrl+s</jvs-button>
      <!-- <div class="help-entry" @click="handleHelp">
        <svg aria-hidden="true">
          <use xlink:href="#icon-jvs-yidong"></use>
        </svg>
        <span>教程</span>
      </div> -->
      <slot name="rightButton"></slot>
    </div>
  </div>
</template>

<script>
import {getAppInfoById} from "@/views/page/api/newDesign";

export default {
  name: "DesignHeader",
  props: {
    infoData: {
      type: Object
    },
    tabList: {
      type: Array
    },
    type: {
      type: String
    },
    currentTab: {
      type: String
    },
    hasSave: {
      type: Boolean,
      default: () => {
        return true
      }
    },
    leftIconSlot: {
      type: Boolean
    },
    saveLoading: {
      type: Boolean
    }
  },
  computed: {},
  watch: {
    currentTab(val) {
      this.activeName = val
    }
  },
  data () {
    return {
      appName: '',
      activeName: '',
      showEdit: false
    }
  },
  created() {
    this.getAppInfo()
    this.autoOpenVideo()
    if(this.currentTab) {
      this.activeName = this.currentTab
    }
  },
  mounted() {
    document.addEventListener('keydown', this.ssSaveHandle)
  },
  beforeDestroy() {
    document.removeEventListener('keydown', this.ssSaveHandle)
  },
  methods: {
    // tab 切换
    handleTabChange(str) {
      this.activeName = str
    },
    // 获取应用列表
    getAppInfo() {
      if(this.$route.query && this.$route.query.jvsAppId) {
        getAppInfoById(this.$route.query.jvsAppId).then(res => {
          if (res.data && res.data.code == 0 && res.data.data) {
            this.appName = res.data.data.name
            if(res.data.data.enableVersionFeature) {
              for(let i in this.tabList) {
                if(this.tabList[i].name == 'permission' && this.tabList[i].label == '页面权限') {
                  this.$set(this.tabList[i], 'display', false)
                }
              }
            }
          }
        })
      }
    },
    handleClick(e) {
      this.$emit('tabSelect', e.name)
    },
    // 保存
    handleSave() {
      this.$emit('handleSave')
    },
    // 帮助
    handleHelp() {
      let str = ''
      switch (this.type) {
        case 'rule':
          str = 'rule-help';
          break;
        case 'chart':
          str = 'chart-help';
          break;
        case 'flow':
          str = 'flow-help';
          break;
        case 'form':
          str = 'video-form-help';
          break;
        case 'list':
          str = 'list-help';
          break;
        default:break;
      }
      if (str.split('-')[0] === 'video') {
        this.$videoOpen({
          title: '表单帮助',
          dicStr: str ? str : ''
        })
      } else {
        this.$openUrl('', '_blank', str)
      }
    },
    autoOpenVideo() {
      if(this.type == 'form' && this.$store.getters.autoOpenedDict.indexOf('video-form-help') > -1) {
        this.$videoOpen({
          title: '表单帮助',
          dicStr: 'video-form-help'
        })
      }
      if(this.type == 'list' && this.$store.getters.autoOpenedDict.indexOf('video-list-help') > -1) {
        this.$videoOpen({
          title: '列表帮助',
          dicStr: 'video-list-help'
        })
      }
    },
    ssSaveHandle (e) {
      let key = window.event.keyCode ? window.event.keyCode : window.event.which
      if(key === 83 && e.ctrlKey) {
        this.handleSave()
        e.preventDefault()
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.design-header-box{
  padding: 0 20px;
  height: 56px;
  background-color: #ffffff;
  display: flex;
  align-items: center;
  color: #333333;
  font-size: 14px;
  box-sizing: border-box;
  .header-left{
    display: flex;
    align-items: center;
    width: 30%;
    height: 18px;
    font-family: Microsoft YaHei-Regular, Microsoft YaHei;
    font-weight: 400;
    font-size: 14px;
    color: #363B4C;
    line-height: 18px;
    svg{
      margin-right: 10px;
      width: 32px;
      height: 32px;
    }
    .icon-style{
      margin: 0 6px;
    }
    /deep/.el-input{
      .el-input__inner{
        width: 176px;
        height: 28px;
        border: 0;
        background: #F5F6F7;
        border-radius: 4px;
        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
        font-weight: 400;
        font-size: 14px;
        color: #363B4C;
        overflow: hidden;
      }
    }
  }
  /deep/.header-center{
    display: flex;
    justify-content: center;
    width: 40%;
    .el-tabs__header{
      margin: 0;
    }
    .el-tabs__item{
      font-family: Microsoft YaHei-Regular, Microsoft YaHei;
      font-weight: 400;
      font-size: 14px;
      color: #363B4C;
    }
    .el-tabs__nav-wrap:after{
      height: 0;
    }
    .el-tabs__item:hover{
      color: #363B4C;
    }
    .el-tabs__item.is-active{
      color: #1E6FFF;
    }
    .el-tabs__active-bar{
      background: #1E6FFF;
      border-radius: 2px 0px 2px 0px;
    }
  }
  .header-right{
    display: flex;
    align-items: center;
    justify-content: flex-end;
    width: 30%;
    .el-button{
      border: 0;
      height: 32px;
      border-radius: 4px;
      font-family: Source Han Sans-Regular, Source Han Sans;
      font-weight: 400;
      font-size: 14px;
    }
    .help-entry{
      margin-left: 17px;
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      font-family: Source Han Sans-Regular, Source Han Sans;
      font-weight: 400;
      font-size: 12px;
      color: #3D3D3D;
      cursor: pointer;
      svg{
        width: 14px;
        height: 14px;
        margin-right: 5px;
      }
      span{
        word-break: keep-all;
      }
    }
  }
}
</style>
