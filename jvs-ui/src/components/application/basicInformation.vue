<template>
  <div class="basic-info-page">
    <div v-if="dataLoading" class="loading-back"/>
    <div class="page-item">
      <div class="title">
        <svg class="icon" aria-hidden="true">
          <use xlink:href="#icon-jvs-rongqi"></use>
        </svg>
        <span>{{$langt('appBasicInfo.title.basic')}}</span>
      </div>
      <div class="content">
        <div class="left">
          <div v-if="basicInfo.name" class="name">{{basicInfo.name}}</div>
          <div class="version">
            <span>{{$langt('appBasicInfo.basic.version')}}</span><span v-if="basicInfo.version">: V{{basicInfo.version}}</span>
          </div>
        </div>
        <div class="right">
          <div class="content-item">
            <div class="con-title">{{$langt('appBasicInfo.basic.devices')}}</div>
            <div class="con">
              <span>{{basicInfo.devices}}</span>
              <span v-if="basicInfo && basicInfo.devices" class="copy" @click="handleCopy(basicInfo.devices)">{{$langt('appBasicInfo.basic.copy')}}</span>
            </div>
          </div>
          <div class="content-item">
            <div class="con-title">{{$langt('appBasicInfo.basic.deviceBoolean')}}</div>
            <div class="con">
              <span>{{ (basicInfo && basicInfo.devices && basicInfo.devices.indexOf(',') > -1) ? '是' : '否' }}</span>
            </div>
          </div>
          <div class="content-item">
            <div class="con-title">{{$langt('appBasicInfo.basic.dateTime')}}</div>
            <div class="con">
              <span>{{ basicInfo.dateTime }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-if="jvsDesign && jvsDesign.JVS_DESIGN_MGR" class="page-item">
      <div class="title">
        <svg class="icon" aria-hidden="true">
          <use xlink:href="#icon-jvs-rongqi"></use>
        </svg>
        <span>{{$langt('appBasicInfo.title.resource')}}</span>
      </div>
      <div class="resource">
        <div class="resource-item" v-for="(item, key) in basicInfo.list" :key="key">
          <div class="item-data">
            <div class="res-title">{{item.name}}</div>
            <div class="res-number">{{item.maxSize == -1 ? $langt('appBasicInfo.resource.notsupport') : item.size}}</div>
          </div>
          <div class="item-footer">
            <div class="res-label">{{$langt('appBasicInfo.resource.used')}}</div>
            <div class="item-process">
              <el-progress :stroke-width="8" style="width: calc(100% - 70px)" :show-text="false" :percentage="getProcess(item, 'number')" color="#1E6FFF"></el-progress>
              <div class="process-tag">{{ getProcess(item) }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="page-item">
      <div class="title">
        <svg class="icon" aria-hidden="true">
          <use xlink:href="#icon-jvs-rongqi"></use>
        </svg>
        <span>{{$langt('appBasicInfo.title.other')}}</span>
      </div>
      <div class="other-link-list">
        <div v-for="(item, index) in linkList" :key="'other-app-item-'+index" class="other-link-item">
          <div class="box" @click="enterLink(item)">
            <img :src="item.iconUrl" alt="">
            <div class="name">{{item.name}}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getPlatInfo } from "@/api/application";
import { getDynamicDesign, getDynamicResource } from '@/api/admin/home'
export default {
  name: "basicInfo",
  data () {
    return {
      basicInfo: {},
      dataLoading: false,
      isAuth:false,
      jvsDesign: null,
      linkList: [],
    }
  },
  async created() {
    this.getPlatInfo()
    this.getDynamicDesign()
    this.getDynamicResource()
  },
  methods: {
    // 复制地址
    handleCopy(url) {
      const text = document.createElement('input')
      text.value = url
      document.body.appendChild(text)
      text.select()
      document.execCommand('Copy')
      document.body.removeChild(text)
      this.$notify({
        title: '提示',
        message: '复制成功',
        position: 'bottom-right',
        type: 'success'
      });
    },
    // 获取资源已使用进度
    getProcess(obj, type) {
      if (obj.maxSize == -1) {
        return type === 'number' ? 0 : this.$langt('appBasicInfo.resource.notsupport')
      } else if (obj.maxSize == 0) {
        return type === 'number' ? 0 : this.$langt('appBasicInfo.resource.notlimit')
      } else {
        let number = (obj.size / obj.maxSize) * 100
        if (number < 1) {
          number = 1
        } else {
          number = number.toFixed(0)
        }
        return type === 'number' ? Number(number) : number + '%'
      }
    },
    getPlatInfo () {
      getPlatInfo().then(res => {
        if (res.data && res.data.code == 0) {
          this.basicInfo = JSON.parse(JSON.stringify(res.data.data))
        }
        console.log(this.basicInfo)
        this.dataLoading = false
      }).catch(err => {
        this.dataLoading = false
      })
    },
    getDynamicDesign () {
      getDynamicDesign().then(res => {
        if(res.data && res.data.code == 0) {
          this.jvsDesign = res.data.data
        }
      })
    },
    getDynamicResource () {
      getDynamicResource().then(res => {
        if(res.data && res.data.code == 0) {
          this.linkList = res.data.data
        }
      })
    },
    enterLink (item) {
      this.$openUrl(`${item.url}`, '_blank')
    }
  }
}
</script>

<style lang="scss" scoped>
.basic-info-page{
  width: 1000px;
  padding-top: 4px;
  height: 100vh;
  margin-left: calc(50% - 500px);
  position: relative;
  overflow-y: auto;
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
    z-index: 1;
  }
  .page-item{
    position: relative;
    border-radius: 6px;
    background-color: #ffffff;
    &+.page-item{
      margin-top: 32px;
    }
    .title{
      margin-bottom: 16px;
      display: flex;
      align-items: center;
      .icon{
        display: block;
        width: 16px;
        height: 16px;
        margin-right: 10px;
      }
      span{
        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
        font-weight: 400;
        font-size: 16px;
        color: #363B4C;
      }
    }
    .content{
      height: 96px;
      display: flex;
      justify-content: space-between;
      border-radius: 4px;
      background-image: url(./img/basicinfobg.png);
      background-size: 100%;
      .left{
        display: flex;
        align-items: center;
        justify-content: center;
        flex-direction: column;
        padding-left: 32px;
        .name{
          min-width: 64px;
          height: 21px;
          font-family: Microsoft YaHei-Bold, Microsoft YaHei;
          font-weight: 700;
          font-size: 16px;
          color: #363B4C;
          line-height: 21px;
          margin-bottom: 8px;
        }
        .version{
          width: 125px;
          height: 26px;
          background-image: url(./img/basicinfoversion.png);
          background-size: 100% 100%;
          background-repeat: no-repeat;
          padding-right: 5px;
          display: flex;
          align-items: center;
          span{
            line-height: 26px;
            font-family: Microsoft YaHei-Regular, Microsoft YaHei;
            font-weight: 400;
            font-size: 14px;
            color: #1E6FFF;
          }
          span:nth-of-type(1){
            margin-left: 28px;
          }
        }
      }
      .right{
        display: flex;
        align-items: center;
        margin-right: 28px;
        .content-item{
          &+.content-item{
            margin-left: 80px;
          }
          .con-title{
            font-family: Source Han Sans-Regular, Source Han Sans;
            font-weight: 400;
            font-size: 12px;
            color: #6F7588;
            margin-bottom: 8px;
          }
          .con{
            height: 20px;
            font-family: Source Han Sans-Medium, Source Han Sans;
            font-weight: 500;
            font-size: 14px;
            color: #363B4C;
            .copy{
              font-family: Source Han Sans-Regular, Source Han Sans;
              font-weight: 400;
              font-size: 14px;
              color: #1E6FFF;
              margin-left: 8px;
              cursor: pointer;
            }
          }
        }
      }
    }
    .resource{
      display: flex;
      flex-wrap: wrap;
      .resource-item{
        flex: 1;
        border-radius: 4px;
        border: 1px solid #EEEFF0;
        overflow: hidden;
        &+.resource-item{
          margin-left: 17px;
        }
        .item-data{
          display: flex;
          align-items: center;
          justify-content: space-between;
          height: 88px;
          padding: 0 23px;
          background: linear-gradient( -270deg, #F5F6F7 0%, rgba(245,246,247,0) 100%);
          border-radius: 3px 3px 0px 0px;
          box-sizing: border-box;
          .res-title{
            font-family: Source Han Sans-Regular, Source Han Sans;
            font-weight: 400;
            font-size: 14px;
            color: #363B4C;
          }
          .res-number{
            font-family: Source Han Sans-Bold, Source Han Sans;
            font-weight: 700;
            font-size: 32px;
            color: #363B4C;
          }
        }
        .item-footer{
          border-top: 1px solid #EEEFF0;
          padding: 13px 24px;
          box-sizing: border-box;
          .res-label{
            height: 20px;
            font-family: Source Han Sans-Regular, Source Han Sans;
            font-weight: 400;
            font-size: 14px;
            color: #363B4C;
            line-height: 20px;
          }
          .item-process{
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-top: 8px;
            /deep/.el-progress-bar{
              .el-progress-bar__outer{
                background: #E4EDFF;
              }
            }
            .process-tag{
              width: 62px;
              margin-left: 8px;
              height: 20px;
              font-family: Source Han Sans-Regular, Source Han Sans;
              font-weight: 400;
              font-size: 14px;
              color: #1E6FFF;
              line-height: 20px;
              text-align: right;
            }
          }
        }
      }
    }
    .other-link-list{
      display: flex;
      .other-link-item{
        display: flex;
        width: 172px;
        height: 56px;
        background: #F5F6F7;
        border-radius: 6px;
        .box{
          width: 100%;
          padding: 0 16px;
          display: flex;
          align-items: center;
          border-radius: 6px;
          box-sizing: border-box;
          overflow: hidden;
          cursor: pointer;
          img{
            display: block;
            width: 32px;
            height: 32px;
          }
          .name{
            margin-left: 12px;
            width: calc(100% - 44px);
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            font-family: Source Han Sans-Medium, Source Han Sans;
            font-weight: 500;
            font-size: 14px;
            color: #3D3D3D;
          }
        }
        .box:hover{
          background-color: #f5f7fa;
        }
        &+.other-link-item{
          margin-left: 16px;
        }
      }
    }
  }
}
.basic-info-page::-webkit-scrollbar{
  display: none;
}
</style>
