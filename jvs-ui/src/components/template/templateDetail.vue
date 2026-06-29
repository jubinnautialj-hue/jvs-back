<template>
  <el-dialog
    :title="title"
    :visible.sync="detailVisible"
    class="detail-dialog"
    append-to-body
    :before-close="handleClose">
    <div v-show="isInstall" class="install-box">
      <div class="install-process">
        <el-progress :stroke-width="26" :percentage="proportion"></el-progress>
        <div class="process-text">{{ syncMessage }}</div>
      </div>
    </div>
    <div v-if="!isInstall && detailInfo" class="detail-box">
      <div v-if="applyLoading" class="loading-back"/>
      <div class="detail-left">
        <div class="detail-left-top-back"></div>
        <img :src="detailInfo.logo" alt="">
        <div class="detail-tag" v-if="detailInfo.version">
          <el-tag :type="detailInfo.free ? 'success' : 'warning'" size="mini" style="margin-right: 12px">{{ detailInfo.free ? '免费' : '付费' }}</el-tag>
          <el-tag type="info" size="mini">{{detailInfo.version}}</el-tag>
        </div>
        <div class="detail-title">{{detailInfo.name}}</div>
        <div class="detail-description">{{ detailInfo.description }}</div>
        <!-- <div class="detail-name">{{ detailInfo.platform }}</div> -->
        <div class="detail-name" style="margin-top: 16px;">创建时间：{{ detailInfo.createTime }}</div>
        <div class="detail-name" v-if="detailInfo.size">应用大小：{{ getTempSize(detailInfo.size) }}</div>
        <div v-show="(modeUserInfo && modeUserInfo.userId) ? false : true" class="detail-button">
          <el-button v-if="detailInfo && !detailInfo.primitive" :loading="applyLoading" style="width: 100%" size="mini" type="primary" @click="handleSelect">应用此模板</el-button>
        </div>
      </div>
      <div v-if="false" class="detail-images">
        <el-carousel height="720px" class="img-carousel">
          <el-carousel-item v-for="(item, key) in detailInfo.imgs" :key="key">
            <el-image
              class="item-img"
              :src="item"
              :preview-src-list="detailInfo.imgs">
            </el-image>
          </el-carousel-item>
        </el-carousel>
      </div>
      <!-- 富文本 -->
      <div id="tempDetail" class="detail-right">
        <p></p>
      </div>
    </div>
  </el-dialog>
</template>

<script>
import { getTemplateDetail } from "@/components/template/api";
import E from "wangeditor";

export default {
  name: "templateDetail",
  props: {
    detailVisible: {
      type: Boolean,
      default() {
        return false
      }
    },
    modeUserInfo: {
      type: Object
    }
  },
  watch: {
    detailVisible(val) {
      if (val) {
        this.isInstall = false
        this.isClose = false
      }
    }
  },
  data () {
    return {
      detailInfo: {},
      isClose: false,
      proportion: 0,
      syncMessage: '',
      applyLoading: false,
      title: '模板详情',
      isInstall: false,
      previewEditor: null,
      longText: '',
    }
  },
  methods: {
    // 初始化预览富文本
    initPreviewEditor() {
      let _this = this
      if(_this.previewEditor) {
        _this.previewEditor.destroy()
      }
      _this.previewEditor = new E('#tempDetail')
      _this.previewEditor.config.menus = []
      _this.previewEditor.config.zIndex = 80
      _this.previewEditor.config.height = 720
      _this.previewEditor.config.placeholder = ''
      _this.previewEditor.config.showFullScreen = false
      _this.previewEditor.create()
      _this.previewEditor.txt.html(_this.longText)
      _this.previewEditor.disable()
    },
    // 获取模板应用大小
    getTempSize(size) {
      let str = ''
      if(size < 1024) {
        str = size + 'B'
      }else{
        if(size < (1024 * 1024)) {
          str = Number.parseFloat(size / 1024).toFixed(2) + 'KB'
        }else{
          if(size < (1024 * 1024 * 1024)) {
            str = Number.parseFloat(size / 1024 / 1024).toFixed(2) + 'MB'
          }else{
            str = Number.parseFloat(size / 1024 / 1024 / 2024).toFixed(2) + 'GB'
          }
        }
      }
      return str
    },
    // 获取模板详情
    geTemplateDetail(obj) {
      this.applyLoading = true
      getTemplateDetail(obj.id).then(res => {
        if(res.data && res.data.code == 0 && res.data.data) {
          this.detailInfo = JSON.parse(JSON.stringify(res.data.data))
          this.longText = this.detailInfo.longText
          this.$nextTick(() => {
            this.initPreviewEditor()
          })
          this.$emit('openDialog', true)
        }else{
          this.$emit('openDialog', false)
        }
        this.applyLoading = false
      }).catch(err => {
        this.applyLoading = false
      })
    },
    handleClose() {
      this.isClose = true
      this.title = '模板详情'
      this.$emit('handleDetailClose')
      this.detailInfo = {}
      if(this.previewEditor) {
        this.previewEditor.txt.html(`<p></p>`)
      }
    },
    percentInit() {
      this.proportion = 0
      this.syncMessage = ''
    },
    handleSelect() {
      this.$emit('handleSelect', this.detailInfo, (result) => {
        if (result) {
          this.handleClose()
        }
      })
    },
    handleApply() {
      this.applyLoading = false
    }
  }
}
</script>

<style lang="scss" scoped>
.el-dialog__wrapper.detail-dialog{
  /deep/.el-dialog{
    height: calc(100vh - 380px);
    margin-top: 190px!important;
    border-radius: 6px!important;
    overflow: hidden;
    .el-dialog__header{
      height: 48px;
      background: #F5F6F7;
      box-sizing: border-box;
      .el-dialog__title{
        font-size: 14px;
        font-family: Microsoft YaHei-Bold, Microsoft YaHei;
        font-weight: 700;
        color: #363B4C;
      }
      .el-dialog__headerbtn{
        top: 14px;
        .el-dialog__close{
          color: #575E73!important;
          font-size: 20px;
        }
      }
    }
    .el-dialog__header::before{
      display: none!important;
    }
    .el-dialog__body{
      height: calc(100% - 48px);
      box-sizing: border-box;
      padding: 0!important;
      overflow: hidden;
    }
  }
  .install-box{
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    .install-process{
      height: 50px;
      width: 50%;
      /deep/.el-progress-bar{
        padding-right: 70px;
        margin-right: -70px;
      }
      .el-progress-bar__inner:before{
        content:"";
        width:100%;
        height:100%;
        display:block;
        background-image:repeating-linear-gradient(-45deg,hsla(0,0%,100%,.15) 50%,transparent 0,transparent 50%,hsla(0,0%,100%,.15) 0,hsla(0,0%,100%,.15) 75%,transparent 0,transparent);
        background-size:40px 40px;
        animation:mymove 5s linear infinite;
      }
      .el-progress__text{
        font-size: 16px!important;
      }
      @keyframes mymove{
        0%   {background-position: 0;}
        25%  {background-position: 50px;}
        50%  {background-position: 100px;}
        75%  {background-position: 150px;}
        100% {background-position: 200px;}
      }
      .process-text{
        text-align: center;
        margin-top: 10px;
        height: 20px;
        line-height: 20px;
        font-weight: bold;
      }
    }
  }
  /deep/.detail-box{
    display: flex;
    width: 100%;
    height: 100%;
    box-sizing: border-box;
    .loading-back{
      position: absolute;
      width: 100%;
      height: 100%;
      top: 0;
      left: 0;
      box-sizing: border-box;
      background-color: rgba(255, 255, 255, 0.8);
      background-image: url('../../../public/jvs-ui-public/img/loading.gif');
      background-repeat: no-repeat;
      background-position: center;
      background-position: center;
      //background-size: 200px 160px;
      z-index: 11;
    }
    .detail-left{
      width: 280px;
      box-sizing: border-box;
      padding: 24px;
      position: relative;
      background: #F5F6F7;
      .detail-left-top-back{
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 64px;
        background: linear-gradient(179deg, rgba(30,111,255,0.05) 0%, rgba(30,111,255,0) 100%);
      }
      img {
        width: 56px;
        height: 56px;
        border-radius: 6px;
      }
      .detail-tag{
        margin-top: 17px;
        display: flex;
        font-size: 14px;
        .el-tag{
          width: 48px;
          height: 20px;
          text-align: center;
          color: #fff;
          border: 0;
        }
        .el-tag--success{
          background-color: #36B452;
        }
        .el-tag--warning{
          background-color: #FF9736;
        }
        .el-tag--info{
          background: #E4EDFF;
          color: #1E6FFF;
        }
      }
      .detail-title {
        margin-top: 16px;
        height: 23px;
        line-height: 23px;
        font-size: 16px;
        font-family: Source Han Sans-Bold, Source Han Sans;
        font-weight: 700;
        color: #363B4C;
      }
      .detail-description {
        margin-top: 12px;
        text-align: justify;
        font-size: 12px;
        font-family: Source Han Sans-Regular, Source Han Sans;
        font-weight: 400;
        color: #6F7588;
        line-height: 17px;
      }
      .detail-name {
        margin-top: 8px;
        height: 17px;
        font-size: 12px;
        font-family: Source Han Sans-Regular, Source Han Sans;
        font-weight: 400;
        color: #363B4C;
        line-height: 17px;
        span{
          color: #36B452;
        }
      }
      .detail-button{
        margin-top: 24px;
        .el-button{
          height: 36px;
          background: #1E6FFF;
          border-radius: 6px;
        }
      }
    }
    .detail-right{
      width: calc(100% - 280px);
      height: 100%;
      padding: 16px 24px;
      box-sizing: border-box;
      overflow: hidden;
      overflow-y: auto;
      div::-webkit-scrollbar{
        display: none;
      }
    }
    .w-e-text-container{
      border: none!important;
      //height: auto!important;
    }
    .w-e-toolbar{
      border: none!important;
    }
    .detail-images{
      width: calc(100% - 480px);
      //display: flex;
      //justify-content: center;
      .image-none{
        margin-top: 10vh;
        text-align: center;
      }
      .img-carousel{
        //background-color: #94a8a7;
        //margin-top: 20px;
        text-align: center;
        vertical-align: middle;
        //width: 100%;
        .item-img{
          height: 100%;
        }
      }
    }
  }
}
</style>
