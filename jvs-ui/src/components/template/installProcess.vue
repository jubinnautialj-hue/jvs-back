<template>
  <el-dialog
    :visible.sync="processVisible"
    :class="{'install-process-dialog': true, 'install-process-dialog-hide': !openInfo}"
    append-to-body
    :modal="false"
    :show-close="false"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :before-close="handleClose">
    <div v-if="processVisible" class="install-process-info">
      <div class="top">
        <div class="header-tool">
          <svg v-if="openInfo" class="icon" aria-hidden="true" @click="openclose(false)">
            <use xlink:href="#icon-jvs-danchuang-shouqi"></use>
          </svg>
          <svg v-else class="icon" aria-hidden="true" @click="openclose(true)">
            <use xlink:href="#icon-jvs-danchuang-zhankai"></use>
          </svg>
          <svg class="icon" aria-hidden="true" style="width: 14px;height: 14px;" @click="handleClose">
            <use xlink:href="#icon-jvs-danchuangguanbi1"></use>
          </svg>
        </div>
        <img src="/jvs-ui-public/img/app.png" alt="" class="logo">
        <div class="name">{{basicInfo.summary}}</div>
        <div class="process-bar">
          <div class="process-bar-number" :style="`width:${basicInfo.ratio || 0}%;`">
            <div v-if="(basicInfo.ratio || 0) > 0" class="end-block"></div>
          </div>
        </div>
        <div class="process-text">已安装{{basicInfo.ratio || 0}}%</div>
      </div>
      <div ref="dataList" v-show="openInfo" class="content">
        <div
          v-for="(item, index) in processList"
          :key="item.id"
          :class="{'process-item': true,
            'success': item.progress == 'SUCCESS',
            'doing': item.progress == 'PROCESSING',
            'fail': item.progress == 'FAILURE',
          }">
          <div class="process-item-info">
            <div class="icon">
              <svg class="icon" aria-hidden="true">
                <use :xlink:href="getIcon(item.progress)"></use>
              </svg>
            </div>
            <div class="text">{{item.content}}</div>
            <div class="status">{{item.progress | getStatusLabel}}</div>
          </div>
          <div class="process-item-bottom">
            <div v-if="index < processList.length -1" class="process-item-bar"></div>
            <div v-if="item.progress == 'FAILURE' && item.exceptionStackTrace" class="error-text">
              <span class="text">{{item.exceptionStackTrace}}</span>
              <el-popover
                popper-class="err-info-tip-pover"
                placement="top-start"
                title="错误信息"
                trigger="click"
                width="500">
                <div style="padding: 20px;" v-text="item.exceptionStackTrace"></div>
                <span slot="reference" class="viewmore">查看更多>></span>
              </el-popover>
            </div>
          </div>
          <div v-if="['SUCCESS', 'FAILURE'].indexOf(item.progress) == -1" class="process-item-line">
            <div v-if="item.progress == 'PROCESSING'" class="doing-line"></div>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: "installProcess",
  props: {
    processVisible: {
      type: Boolean,
      default() {
        return false
      }
    },
    installInfo: {
      type: Object
    },
    processList: {
      type: Array
    }
  },
  filters: {
    getStatusLabel (status) {
      let str = ''
      switch (status) {
        case 'SUCCESS': str = '已完成';break;
        case 'FAILURE': str = '安装失败';break;
        case 'PROCESSING': str = '进行中';break;
        case 'WAIT': str = '等待中';break;
        default: str = '等待中';break;
      }
      return str
    }
  },
  computed: {
    basicInfo () {
      return this.installInfo
    }
  },
  data () {
    return {
      openInfo: true,
      viewIndex: 0
    }
  },
  methods: {
    handleClose() {
      this.$emit('close', true)
      this.openInfo = true
    },
    openclose (bool) {
      this.openInfo = bool
      this.$forceUpdate()
    },
    getIcon (status) {
      let str = ''
      switch (status) {
        case 'SUCCESS': str = '#icon-jvs-chenggong';break;
        case 'FAILURE': str = '#icon-jvs-shibai';break;
        case 'PROCESSING': str = '#icon-jvs-jinhangzhong';break;
        case 'WAIT': str = '#icon-jvs-dengdaizhong';break;
        default: str = '#icon-jvs-dengdaizhong';break;
      }
      return str
    }
  },
  watch: {
    installInfo: {
      handler(newVal, oldVal) {
        this.$forceUpdate()
      }
    },
    processList: {
      handler(newVal, oldVal) {
        if(newVal.length > 0) {
          if(this.processList[this.processList.length-1].progress == 'SUCCESS') {
            if(this.basicInfo) {
              this.basicInfo.ratio = 100
              if(this.$refs.dataList) {
                this.$refs.dataList.scrollTo({
                  top: (52 * Number(this.processList.length) + 40),
                  behavior: "smooth",
                })
              }
              this.$forceUpdate()
            }
          }
          if(newVal.length > 6 && this.$refs.dataList) {
            for(let i in this.processList) {
              if(this.processList[i].progress == 'PROCESSING') {
                this.$refs.dataList.scrollTo({
                  top: (52 * Number(i - 2) + 40),
                  behavior: "smooth",
                })
              }
            }
          }
        }
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.el-dialog__wrapper.install-process-dialog{
  /deep/.el-dialog{
    margin: 0!important;
    position: absolute;
    right: 42px;
    bottom: 31px;
    width: 500px;
    height: 575px;
    background: #FFFFFF;
    box-shadow: 0px 2px 6px 0px rgba(54,59,76,0.1);
    border-radius: 4px;
    border: 1px solid #EEEFF0;
    overflow: hidden;
    .el-dialog__header{
      display: none!important;
    }
    .el-dialog__body{
      padding: 0!important;
      height: 100%;
    }
  }
}
.install-process-dialog.install-process-dialog-hide{
  /deep/.el-dialog{
    height: 207px;
  }
}
.install-process-info{
  width: 100%;
  height: 100%;
  .top{
    width: 100%;
    height: 207px;
    background-image: url(../../const/application/processinstall.png);
    background-repeat: no-repeat;
    position: relative;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    border-bottom: 1px dashed #B3B4B4;
    position: relative;
    .header-tool{
      position: absolute;
      top: 20px;
      right: 24px;
      display: flex;
      align-items: center;
      .icon{
        width: 16px;
        height: 16px;
        margin-left: 16px;
        cursor: pointer;
      }
    }
    .logo{
      display: block;
      width: 56px;
      height: 56px;
      border-radius: 6px;
    }
    .name{
      margin-top: 16px;
      height: 23px;
      font-size: 16px;
      font-family: Source Han Sans-Bold, Source Han Sans;
      font-weight: 700;
      color: #363B4C;
      line-height: 23px;
    }
    .process-bar{
      margin-top: 20px;
      width: 420px;
      height: 26px;
      background: #FFFFFF;
      border-radius: 4px;
      opacity: 1;
      border: 1px solid #C2C5CF;
      padding: 4px;
      box-sizing: border-box;
      .process-bar-number{
        height: 16px;
        background: linear-gradient(-90deg, #1E6FFF 0%, rgba(30,111,255,0) 100%);
        border-radius: 4px;
        position: relative;
        .end-block{
          position: absolute;
          right: 0;
          top: 0;
          width: 16px;
          height: 16px;
          background: #1E6FFF;
          border-radius: 4px;
        }
      }
    }
    .process-text{
      margin-top: 8px;
      height: 20px;
      font-size: 14px;
      font-family: Source Han Sans-Regular, Source Han Sans;
      font-weight: 400;
      color: #363B4C;
      line-height: 20px;
    }
  }
  .content{
    height: calc(100% - 207px);
    padding: 20px 40px;
    box-sizing: border-box;
    overflow: hidden;
    overflow-y: auto;
    .process-item{
      margin-top: 6px;
      position: relative;
      padding-bottom: 10px;
      .process-item-info{
        height: 20px;
        display: flex;
        align-content: center;
        justify-content: space-between;
        .icon{
          width: 20px;
          height: 20px;
          border-radius: 50%;
          overflow: hidden;
          margin-right: 18px;
        }
        .text{
          flex: 1;
          font-size: 12px;
          font-family: Microsoft YaHei-Regular, Microsoft YaHei;
          font-weight: 400;
          color: #6F7588;
          line-height: 20px;
        }
        .status{
          width: 56px;
          height: 24px; 
          border-radius: 4px;
          text-align: center;
          line-height: 24px;
          padding: 0 4px;
        }
      }
      .process-item-bottom{
        display: flex;
        margin-top: 6px;
        .error-text{
          margin-left: 28px;
          padding-left: 8px;
          width: 380px;
          height: 22px;
          background: #FFEDF1;
          border-radius: 4px;
          font-size: 12px;
          font-family: Microsoft YaHei-Regular, Microsoft YaHei;
          font-weight: 400;
          color: #FF194C;
          display: flex;
          align-items: center;
          height: 22px;
          line-height: 22px;
          overflow: hidden;
          .text{
            width: 168px;
            height: 100%;
            overflow: hidden;
            white-space: pre;
            text-overflow: ellipsis;
          }
          .viewmore{
            color: #1E6FFF;
            margin-left: 5px;
            cursor: pointer;
          }
        }
      }
      .process-item-bar{
        width: 1px;
        height: 16px;
        background: #EEEFF0;
        margin-left: 10px;
      }
      &.success{
        .icon, .process-item-bar{
          background: #21C275;
        }
        .status{
          background: #EFF9F2;
          color: #21C275;
        }
      }
      &.doing{
        .icon, .process-item-bar{
          background: #1E6FFF;
        }
        .status{
          background: rgba(30,111,255,0.08);
          color: #1E6FFF;
        }
      }
      &.fail{
        .icon, .process-item-bar{
          background: #FF194C;
        }
        .status{
          background: #FFEDF1;
          color: #FF194C;
        }
      }
      .process-item-line{
        position: absolute;
        width: 380px;
        height: 2px;
        background: #D2E2FF;
        right: 0;
        top: 28px;
        overflow: hidden;
        .doing-line{
          position: absolute;
          left: 0;
          width: 279px;
          height: 2px;
          background: linear-gradient(90deg,transparent,#1E6FFF);
          animation: animate1 2s linear infinite;
        }
      }
    }
  }
}
@keyframes animate1 {
  0% {
    left: -279px;
  }
  50%, 100% {
    left: calc(100% + 279px);
  }
}

</style>
<style lang="scss">
.err-info-tip-pover{
  .el-popover__title{
    display: none;
  }
}
</style>
