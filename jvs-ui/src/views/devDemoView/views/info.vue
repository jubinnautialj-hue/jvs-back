<template>
  <div class="dev-demo-view-info">
    <div class="info-box">
      <div class="top">
        <div class="top-body">
          <div class="header">
            <div class="title">{{infoData.ziYuanMingChen_1 || infoData.ziYuanMingChen}}</div>
            <div class="opration">
              <div class="collect">
                <i></i>
                <span>收藏</span>
              </div>
              <div class="share">
                <i></i>
                <span>分享</span>
              </div>
            </div>
          </div>
          <div class="body">
            <i class="xuke-icon"></i>
            <span>使用许可（请仔细阅读）：</span>
            <span class="text-info">数据使用许可</span>
          </div>
          <div class="list-details">
            <div v-for="item in detailList" :key="item.prop" class="list-details-item">
              <span>{{item.label}}：</span>
              <span class="text-primary">{{infoData[item.prop+'_1'] || infoData[item.prop]}}</span>
            </div>
          </div>
          <div class="body-bottom">
            <div class="visit-number">{{infoData.liuLanLiang || 0}}次</div>
            <div class="download-number">{{infoData.xiaZaiLiang || 0}}次</div>
          </div>
        </div>
      </div>
      <div class="center">
        <ul class="tab-heading">
          <li v-for="tab in tabList" :key="tab.name" :class="{'active': activeName == tab.name}" @click="activeName = tab.name;">{{tab.label}}</li>
        </ul>
        <div class="panel-body">
          <div v-show="activeName == 'basic'" class="basic-info">
            <div v-for="item in basicColumn" :key="'table-item-'+item.prop" :class="'table-item-cell table-item-'+(item.span || 12)">
              <div class="title">{{item.label}}</div>
              <div class="value" :title="infoData[item.prop+'_1'] || infoData[item.prop]">{{infoData[item.prop+'_1'] || infoData[item.prop]}}</div>
            </div>
          </div>
          <div v-show="activeName == 'data'" class="data-info">
            <div class="data-info-item">
              <div v-for="dtitle in dataColumn" :key="dtitle.prop">{{dtitle.label}}</div>
            </div>
            <div v-for="row in (infoData.tableForm1702970296529 || [])" :key="row.id" class="data-info-item">
              <div v-for="item in dataColumn" :key="row.id+'-'+item.prop">{{row[item.prop+'_1'] || row[item.prop]}}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import { getSingleData } from '../api/index'
export default {
  name: 'devDemoViewInfo',
  data () {
    return {
      queryParam: {},
      infoData: {},
      detailList: [
        {label: '来源部门', prop: 'buMenMingChen'},
        {label: '重点领域', prop: 'shuJuXingYe'},
        {label: '目录发布时间', prop: 'faBuRiQi'},
        {label: '目录更新时间', prop: 'updateTime'},
        {label: '开放类型', prop: 'kaiFangLeiXing'},
      ],
      activeName: 'basic',
      tabList: [
        {label: '基本信息', name: 'basic'},
        {label: '数据项', name: 'data'}
      ],
      basicColumn: [
        {label: '来源部门', prop: 'buMenMingChen', span: 12},
        {label: '数据量', prop: 'shuJuRongLiang', span: 12},
        {label: '开放状态', prop: 'kaiFangLeiXing', span: 12},
        {label: '所属行业', prop: 'shuJuXingYe', span: 12},
        {label: '目录发布时间', prop: 'faBuRiQi', span: 12},
        {label: '数据更新时间', prop: 'updateTime', span: 12},
        {label: '更新频率', prop: 'gengXinPinLv', span: 12},
        {label: '用户评分', prop: 'pingfen', span: 12},
        {label: '部门电话', prop: 'phone', span: 12},
        {label: '部门邮箱', prop: 'email', span: 12},
        {label: '标签', prop: 'biaoqian', span: 24},
        {label: '描述', prop: 'ziYuanMiaoShu', span: 24},
      ],
      dataColumn: [
        {label: '英文信息项名', prop: 'yingWenXinXiXiangMing'},
        {label: '中文信息项名', prop: 'zhongWenXinXiXiangMing'},
        {label: '数据类型', prop: 'shuJuLeiXing'},
        {label: '中文描述', prop: 'zhongWenMiaoShu'}
      ]
    }
  },
  created () {
    if(this.$route.query) {
      this.queryParam = this.$route.query
      this.getInfoData()
    }
  },
  methods: {
    getInfoData () {
      if(this.queryParam.jvsAppId && this.queryParam.dataModelId && this.queryParam.id) {
        getSingleData(this.queryParam.jvsAppId, this.queryParam.dataModelId, this.queryParam.id).then(res => {
          if(res.data && res.data.code == 0) {
            this.infoData = res.data.data
            console.log(res.data.data)
          }
        })
      }
    }
  }
}
</script>
<style lang="scss" scoped>
.dev-demo-view-info{
  background-color: #F2F4F5;
  width: 100%;
  height: 100vh;
  overflow: hidden;
  .info-box{
    padding-top: 40px;
    width: 1200px;
    margin: 0 auto;
    font-size: 14px;
    .top{
      position: relative;
      margin-bottom: 15px;
      background-color: #fff;
      border: 1px solid #eee;
      .top-body{
        padding: 25px 30px;
        .header{
          margin-bottom: 6px;
          display: flex;
          align-items: center;
          justify-content: space-between;
          .title{
            font-size: 18px;
            margin: 5px 0;
            font-weight: 400;
            color: #1B8EF8;
          }
          .opration{
            display: flex;
            align-items: center;
            .collect, .share{
              cursor: pointer;
              position: relative;
              display: flex;
              align-items: center;
              i{
                display: inline-block;
                margin-right: 5px;
                background-size: 100%;
                background-repeat: no-repeat;
              }
            }
            .collect{
              margin-right: 10px;
              i{
                width: 14px;
                height: 14px;
                background-image: url(../img/shoucang.png);
              }
            }
            .share{
              i{
                width: 12px;
                height: 12px;
                background-image: url(../img/fenxiang.png);
              }
            }
          }
        }
        .body{
          background-color: #f6faff;
          padding: 6px 15px;
          color: #6d6d6d;
          display: flex;
          align-items: center;
          .xuke-icon{
            display: block;
            width: 18px;
            height: 18px;
            background-image: url(../img/书签.png);
            background-size: 100%;
          }
          .text-info{
            text-decoration: underline #FF861C;
            color: #FF861C;
            cursor: pointer;
          }
        }
        .list-details{
          margin: 10px 0;
          padding-left: 15px;
          display: flex;
          align-items: center;
          .list-details-item{
            padding-right: 29px;
            .text-primary{
              color: #3d79d1;
            }
          }
        }
        .list-details:nth-last-child(1){
          margin-bottom: 0;
        }
        .body-bottom{
          overflow: hidden;
          .visit-number{
            float: right;
            padding-left: 25px;
            height: 20px;
            line-height: 20px;
            color: #999;
            background: url(../img/eye.png) left center no-repeat;
          }
          .download-number{
            float: right;
            padding-left: 25px;
            height: 20px;
            line-height: 20px;
            color: #999;
            margin-right: 15px;
            background: url(../img/download.png) left center no-repeat;
          }
        }
      }
    }
    .center{
      .tab-heading{
        background: #65A4FF;
        height: 42px;
        margin: 0;
        padding: 0;
        >li{
          float: left;
          margin-top: 2px;
          height: 40px;
          line-height: 40px;
          text-align: center;
          width: 110px;
          cursor: pointer;
          color: #fff;
        }
        .active{
          background-color: #fff;
          color: #1B8EF8;
        }
      }
      .panel-body{
        padding: 15px 30px;
        background-color: #fff;
        .basic-info{
          display: flex;
          align-items: center;
          flex-wrap: wrap;
          border: 1px solid #ddd;
          border-bottom: 0;
          border-right: 0;
          box-sizing: border-box;
          .table-item-cell{
            display: flex;
            align-items: center;
            border-bottom: 1px solid #ddd;
            .title, .value{
              word-break: keep-all;
              padding: 5px;
              box-sizing: border-box;
              height: 40px;
              line-height: 30px;
            }
            .title{
              background-color: #f6fafe;
              width: 34%;
              text-indent: 40px;
              border-right: 1px solid #ddd;
            }
            .value{
              width: 66%;
              text-indent: 20px;
              border-right: 1px solid #ddd;
            }
          }
          .table-item-12{
            width: 50%;
          }
          .table-item-24{
            width: 100%;
            .title{
              background-color: #f6fafe;
              width: 17%;
            }
            .value{
              width: 83%;
              overflow: hidden;
              white-space: pre;
              text-overflow: ellipsis;
            }
          }
        }
        .data-info{
          border: 1px solid #ddd;
          border-right: 0;
          .data-info-item{
            display: flex;
            align-items: center;
            >div{
              width: 25%;
              padding: 10px 8px;
              text-align: center;
              box-sizing: border-box;
              border-right: 1px solid #ddd;
            }
          }
          .data-info-item:nth-of-type(2n+1) {
            background-color: #eee;
          }
        }
      }
    }
  }
}
</style>