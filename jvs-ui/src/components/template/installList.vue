<template>
  <el-dialog
      :title="appInfo ? '发布列表' : '安装列表'"
      :visible.sync="installListVisible"
      class="install-list-dialog"
      append-to-body
      :close-on-click-modal="false"
      :before-close="installListClose">
      <div v-if="installListVisible" class="install-list-box">
        <div class="top-back"></div>
        <div class="left">
          <div v-for="item in installStautsList" :key="item.value" :class="{'left-item': true, 'active': item.value == activeStauts}" @click="installStateChange(item.value)">{{item.label}}</div>
        </div>
        <div class="right">
          <div v-for="row in installListData" :key="row.id" class="install-item">
            <div class="left-box">
              <div class="img-title">
                <img src="/jvs-ui-public/img/app.png" alt="">
                <span :title="row.summary">{{row.summary}}</span>
              </div>
              <div class="process-bar">
                <div class="bar">
                  <div class="bar-number" :style="`width: ${row.ratio || 0}%;`">
                    <div v-if="(row.ratio || 0) > 0" class="end-blobk"></div>
                  </div>
                </div>
                <div class="text">{{row.ratio || 0}}%</div>
              </div>
            </div>
            <div class="show-time">{{row.createTime}}</div>
            <div class="button-tool">
              <span @click="viewInstall(row)">查看详情</span>
            </div>
          </div>
        </div>
      </div>
      <!-- 详情 -->
      <install-process
        ref="templateDetail"
        :processVisible="processVisible"
        :installInfo="currentRow"
        :processList="processList"
        @close="processClose">
      </install-process>
    </el-dialog>
</template>
<script>
import { getMyTaskProcess, getTaskProcessDetail, getAppTaskProcess } from './api'
import installProcess from "./installProcess"
export default {
  name: "installList",
  components: {
    installProcess
  }, 
  props: {
    installListVisible: {
      type: Boolean
    },
    appInfo: {
      type: Object
    }
  },
  data () {
    return {
      activeStauts: 'PROCESSING',
      installStautsList: [
        {label: '进行中', value: 'PROCESSING'},
        {label: '已完成', value: 'SUCCESS'},
        {label: '安装失败', value: 'FAILURE'}
      ],
      installListData: [],
      processVisible: false,
      currentRow: null,
      processList: [],
      timer: null,
      processTimer: null
    }
  },
  methods: {
    async showInstallList () {
      await this.queryInstallList()
      if(this.timer) {
        clearInterval(this.timer)
        this.timer = null
      }
      this.timer = setInterval(() => {
        this.queryInstallList()
      }, 3000)
    },
    installStateChange (value) {
      this.activeStauts = value
      this.queryInstallList()
      if(value == 'PROCESSING') {
        if(!this.timer) {
          this.timer = setInterval(() => {
            this.queryInstallList()
          }, 3000)
        }
      }else{
        if(this.timer) {
          clearInterval(this.timer)
          this.timer = null
        }
      }
      this.$forceUpdate()
    },
    async queryInstallList () {
      if(this.appInfo && this.appInfo.id) {
        await getAppTaskProcess(this.appInfo.id, {progress: this.activeStauts}).then(res => {
          if(res.data && res.data.code == 0 && res.data.data) {
            this.installListData = res.data.data
            if(this.currentRow) {
              this.installListData.filter(it => {
                if(it.id == this.currentRow.id) {
                  this.currentRow = JSON.parse(JSON.stringify(it))
                  this.$forceUpdate()
                }
              })
            }
          }else{
            this.installListData = []
          }
        })
      }else{
        await getMyTaskProcess({progress: this.activeStauts}).then(res => {
          if(res.data && res.data.code == 0 && res.data.data) {
            this.installListData = res.data.data
            if(this.currentRow) {
              this.installListData.filter(it => {
                if(it.id == this.currentRow.id) {
                  this.currentRow = JSON.parse(JSON.stringify(it))
                  this.$forceUpdate()
                }
              })
            }
          }else{
            this.installListData = []
          }
        })
      }
    },
    installListClose () {
      this.activeStauts = 'PROCESSING'
      if(this.timer) {
        clearInterval(this.timer)
        this.timer = null
      }
      this.$emit('close')
    },
    async viewInstall (row) {
      this.currentRow = JSON.parse(JSON.stringify(row))
      await this.getInstallInfo(this.currentRow)
      this.processVisible = true
      this.freshProcess()
    },
    async getInstallInfo (row) {
      await getTaskProcessDetail(row.id).then(res => {
        if(res.data && res.data.code == 0) {
          this.processList = res.data.data
          if(this.processList.length > 0) {
            if(this.processList[this.processList.length-1].code == 'END') {
              if(this.processTimer) {
                clearInterval(this.processTimer)
                this.processTimer = null
              }
            }
          }
        }
      })
    },
    freshProcess () {
      if(this.processTimer) {
        clearInterval(this.processTimer)
        this.processTimer = null
      }
      let _this = this
      this.processTimer = setInterval(() => {
        _this.getInstallInfo(_this.currentRow)
      }, 2000)
    },
    processClose () {
      this.processVisible = false
      this.currentRow = null
      this.processList = []
      if(this.processTimer) {
        clearInterval(this.processTimer)
        this.processTimer = null
      }
    }
  },
  watch: {
    installListVisible: {
      handler(newVal, oldVal) {
        if(newVal) {
          this.showInstallList()
        }
      }
    }
  }
}
</script>
<style lang="scss" scoped>
.el-dialog__wrapper.install-list-dialog{
  /deep/.el-dialog{
    min-width: 1000px;
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
    .install-list-box{
      height: 100%;
      display: flex;
      position: relative;
      .top-back{
        width: 168px;
        height: 54px;
        background: linear-gradient(179deg, rgba(30,111,255,0.05) 0%, rgba(30,111,255,0) 100%);
        position: absolute;
        left: 0;
        top: 0;
      }
      .left{
        width: 168px;
        background: #F5F6F7;
        border-radius: 0px 0px 0px 6px;
        padding: 0 8px;
        padding-top: 9px;
        box-sizing: border-box;
        .left-item{
          position: relative;
          height: 36px;
          border-radius: 4px;
          line-height: 36px;
          font-size: 14px;
          font-family: Microsoft YaHei, Microsoft YaHei;
          font-weight: 400;
          color: #363B4C;
          text-indent: 24px;
          margin-top: 14px;
          cursor: pointer;
          &.active{
            background: #DDEAFF;
            color: #1E6FFF;
          }
          &:hover{
            background: #DDEAFF;
          }
        }
      }
      .right{
        flex: 1;
        height: 100%;
        padding: 0 24px;
        box-sizing: border-box;
        overflow: hidden;
        overflow-y: auto;
        .install-item{
          width: 100%;
          height: 68px;
          border-bottom: 1px solid #EEEFF0;
          display: flex;
          align-items: center;
          justify-content: space-between;
          .left-box{
            display: flex;
            align-items: center;
          }
          .img-title{
            display: flex;
            align-items: center;
            width: 300px;
            img{
              display: block;
              width: 28px;
              height: 28px;
              border-radius: 6px;
              overflow: hidden;
              margin-right: 8px;
            }
            span{
              font-size: 14px;
              font-family: Source Han Sans-Medium, Source Han Sans;
              font-weight: 500;
              color: #363B4C;
              width: calc(100% - 36px);
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: pre;
            }
          }
          .process-bar{
            display: flex;
            align-items: center;
            .bar{
              width: 200px;
              height: 18px;
              background: #FFFFFF;
              border-radius: 4px;
              border: 1px solid #C2C5CF;
              padding: 2px;
              box-sizing: border-box;
              .bar-number{
                height: 12px;
                background: linear-gradient(-90deg, #1E6FFF 0%, rgba(30,111,255,0) 100%);
                border-radius: 4px;
                position: relative;
                .end-blobk{
                  position: absolute;
                  width: 12px;
                  height: 12px;
                  background: #1E6FFF;
                  border-radius: 4px;
                  top: 0;
                  right: 0;
                }
              }
            }
            .text{
              width: 38px;
              margin-left: 8px;
              font-size: 14px;
              font-family: Source Han Sans-Regular, Source Han Sans;
              font-weight: 400;
              color: #363B4C;
            }
          }
          .button-tool{
            display: flex;
            align-items: center;
            span{
              cursor: pointer;
              font-size: 12px;
              font-family: Microsoft YaHei-Regular, Microsoft YaHei;
              font-weight: 400;
              color: #1E6FFF;
            }
          }
        }
      }
    }
  }
}
</style>