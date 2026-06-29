<template>
  <div class="custom-view-page">
    <div class="top">
      <div class="left">
        <div v-if="bulletin && bulletin.content && bulletin.content.bulletin" class="notice">
          <el-alert :title="bulletin.content.bulletin" type="warning" :closable="false"></el-alert>
        </div>
        <div :class="{'info-card': true, 'only-info-card': !(bulletin && bulletin.content && bulletin.content.bulletin)}">
          <div v-for="(item, index) in blockList" :key="'statistic-item-'+index" class="info-card-item">
            <div>
              <div class="fir">{{item.title}}</div>
              <div>
                <b>{{Number(item.price).toFixed(2)}}</b>
                <span>{{item.unit}}</span>
              </div>
            </div>
            <div class="plot-container">
              <div :id="'container'+(Number(index)+1)"></div>
            </div>
          </div>
        </div>
      </div>
      <div class="right">
        <div class="kf-img">
          <img src="./img/kefu.png" alt="">
        </div>
        <div>
          <h4>在线客服</h4>
          <p>联系电话：139 8360 7003</p>
        </div>
        <div class="qr-img">
          <img src="./img/qrcode.jpg">
        </div>
      </div>
    </div>
    <div class="center">
      <div class="left">
        <div class="box">
          <h4>常用模块</h4>
          <div class="app-list-box">
            <!-- /#/other/inside?token=${$store.getters.access_token}&source=${client_id} -->
            <div class="app-item" @click="$openUrl(`http://sdw.bctools.cn`, '_blank')">
              <div class="app-img">
                <img src="./img/数据智仓.png">
              </div>
              <div class="app-name">数据智仓</div>
            </div>
            <div class="app-item" @click="$openUrl(`http://knowledge.bctools.cn`, '_blank')">
              <div class="app-img">
                <img src="./img/协同文档.png">
              </div>
              <div class="app-name">协同文档</div>
            </div>
            <div class="app-item" @click="$openUrl(`https://meeting.bctools.cn`, '_blank')">
              <div class="app-img">
                <img src="./img/视频会议.png">
              </div>
              <div class="app-name">视频会议</div>
            </div>
            <div class="app-item" @click="$openUrl(`http://mailbox.bctools.cn`, '_blank')">
              <div class="app-img">
                <img src="./img/邮件体系.png">
              </div>
              <div class="app-name">邮件体系</div>
            </div>
            <div class="app-item" @click="$openUrl(`http://teamwork.bctools.cn`, '_blank')">
              <div class="app-img">
                <img src="./img/执行计划.png">
              </div>
              <div class="app-name">执行计划</div>
            </div>
            <div class="app-item" @click="$openUrl(`http://jvs-rules.bctools.cn`, '_blank')">
              <div class="app-img">
                <img src="./img/规则引擎.png">
              </div>
              <div class="app-name">规则引擎</div>
            </div>
          </div>
        </div>
        <div class="box">
          <h4 class="line-h4">
            <div>待办任务</div>
            <div @click="viewTaskList">
              <span>更多</span>
              <i class="el-icon-arrow-right"></i>
            </div>
          </h4>
          <div :class="{'task-list-box': true, 'empty-block': !(jvsDesign && jvsDesign.JVS_DESIGN_MGR || true)}">
            <task-list v-if="true || jvsDesign && jvsDesign.JVS_DESIGN_MGR" :pageSize="7" />
          </div>
        </div>
        <div class="box">
          <h4 class="line-h4">
            <div>流程申请</div>
            <div @click="viewTaskList">
              <span>更多</span>
              <i class="el-icon-arrow-right"></i>
            </div>
          </h4>
          <div :class="{'apply-content': true, 'empty-block': !(applyData && applyData.length > 0)}">
            <div class="type-list">
              <div v-for="(item, key) in applyData" :key="key">
                <div class="entry-item-card" @click="applyHandle(item)">
                  <img v-if="item.icon" :src="item.icon" class="icon" alt="" >
                  <div style="margin-left: 12px">
                    <el-tooltip class="item" effect="dark" :content="item.name" placement="top-start">
                      <span>{{item.name}}</span>
                    </el-tooltip>
                    <div style="font-size: 13px;color: #a2a3a5;height: 24px;line-height: 24px">{{item.description}}</div>
                  </div>
                </div>
            </div>
            </div>
          </div>
        </div>
      </div>
      <div class="right">
        <div class="box">
          <h4>分析占比</h4>
          <!-- <h4 class="line-h4">
            <div>分析占比</div>
            <div>
              <span>更多</span>
              <i class="el-icon-arrow-right"></i>
            </div>
          </h4> -->
          <div id="rotateContainer" style="height: 300px;"></div>
        </div>
        <div class="box">
          <h4>日程</h4>
          <div style="padding-top: 5px;">
            <div class="mc-tool">
              <i class="el-icon-d-arrow-left" @click="calendarClick('last', 'year')"></i>
              <i class="el-icon-arrow-left" @click="calendarClick('last', 'month')"></i>
              <span>{{getDay(calenderDay)}}</span>
              <i class="el-icon-arrow-right" @click="calendarClick('next', 'month')"></i>
              <i class="el-icon-d-arrow-right" @click="calendarClick('next', 'year')"></i>
            </div>
            <div class="my-calendar">
              <date-table
                selection-mode="day"
                :first-day-of-week="7"
                :value="calenderDay"
                :default-value="currentDay"
                :date="calenderDay"
                @pick="handleDatePick">
              </date-table>
            </div>
          </div>
        </div>
        <div class="box">
          <h4>消息通知</h4>
          <!-- <h4 class="line-h4">
            <div>消息通知</div>
            <div>
              <span>更多</span>
              <i class="el-icon-arrow-right"></i>
            </div>
          </h4> -->
          <div :class="{'message-list-box': true, 'empty-block': !(messageList && messageList.length > 0)}">
            <div v-for="(item, index) in messageList" :key="'message-item-'+index" @click="viewMessDetail(item)">
              <div class="mess-item">
                <div class="lt">
                  <span>{{item.subClass}}</span>
                  <span class="line-bar">|</span>
                  <span class="con">{{item.msgContent.title}}</span>
                </div>
                <div class="rt">
                  <span class="top-tab-span-span">{{item.createTime}}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 消息详情 -->
    <el-dialog
      title="消息详情"
      :visible.sync="dialogVisible"
      :before-close="handleCloseMessage"
    >
      <div class="dialog-cont" v-if="dialogVisible">
        <div class="mess-title">
          <span>{{messData.msgContent.title}}</span>
        </div>
        <el-row style="display:flex;justify-content: center;height: 42px;align-items: center;padding: 0 4px;text-align: center;">
          <span style="margin-right: 10px;">{{messData.createTime}}</span>
        </el-row>
        <el-row>
          <section v-html="messData.msgContent.content"></section>
        </el-row>
      </div>
    </el-dialog>

    <!-- 流程申请 -->
    <el-dialog
      :title="title"
      width="75%"
      :fullscreen="true"
      append-to-body
      :visible.sync="flowVisible"
      :before-close="flowClose">
      <div v-if="flowVisible && afterSubmit" class="submit-after-dialog">
        <p><i class="el-icon-success"></i></p>
        <h4>提交成功</h4>
        <p><span>数据已提交并保存</span></p>
        <el-row>
          <el-button size="mini" type="primary" @click="againOpen">继续提交</el-button>
          <el-button size="mini" @click="flowClose">关闭</el-button>
        </el-row>
      </div>
      <div v-if="flowVisible && !afterSubmit">
        <iframe
          :src="formUrl"
          class="apply-form-iframe"
          scrolling="auto"
          v-if="isOut"
        >
        </iframe>
        <showForm v-else :formUrl="formUrl" :formId="formId" :approverList="approverList" @close="closeHandle"/>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import DateTable from 'element-ui/packages/date-picker/src/basic/date-table'
import { TinyLine, TinyArea, Liquid, Pie } from '@antv/g2plot';
import { messageaPage } from "@/api/admin/message";
import { entryList } from "@/views/flowable/api/flowable";
import TaskList from "@/views/flowable/views/taskList";
import ShowForm from "@/views/flowable/views/componet/info";
import { startProcess } from "@/views/flowable/views/componet/api";
import { getNewBulletin, getStatisticsBlock, getStatisticsData } from './api'
import {client_id} from '@/const/const'
export default {
  components: { DateTable, TaskList, ShowForm },
  props: {
    jvsDesign: {
      type: Object
    }
  },
  filters: {
    getlabelbyvalue: function(value) {
      let str = value
      let arr = [
        {label: '广播消息', value: 'broadcast'},
        {label: '警告消息', value: 'warning'},
        {label: '通知消息', value: 'notification'},
        {label: '系统消息', value: 'system'},
        {label: '业务消息', value: 'business'}
      ]
      for(let i in arr) {
        if(arr[i].value == value) {
          str = arr[i].label
        }
      }
      return str
    }
  },
  data () {
    return {
      bulletin: {},
      blockList: [],
      messageList: [],
      currentDay: new Date(),
      calenderDay: new Date(),
      plot: null,
      rotatePlot: null,
      plotRotateData: [],
      dialogVisible: false,
      messData: {}, // 行数据
      applyData: [], // 流程申请
      title: '',
      formId: '',
      formUrl: '',
      flowVisible: false,
      afterSubmit: false,
      approverList: [],
      isOut: false,
      client_id: client_id
    }
  },
  created () {
    console.log(this.jvsDesign)
    this.getNewBulletinHandle()
    // this.getMessage()
    this.getEntryList()
  },
  mounted () {
    this.getStatisticsDataHandle()
  },
  methods: {
    // 获取最新公告
    getNewBulletinHandle () {
      getNewBulletin().then(res => {
        if(res.data && res.data.code == 0) {
          this.bulletin = res.data.data
          if(res.data.data && res.data.data.content)
          this.bulletin.content = JSON.parse(res.data.data.content)
        }
      })
    },
    // 获取展示数据
    getStatisticsDataHandle () {
      getStatisticsBlock().then(res => {
        if(res.data && res.data.code == 0) {
          this.blockList = res.data.data
          this.$nextTick(() => {
            this.initPlot(this.blockList)
          })
        }
      })
      getStatisticsData().then(res => {
        if(res.data && res.data.code == 0) {
          this.plotRotateData = res.data.data
          this.$nextTick(() => {
            this.rotatePlot = new Pie('rotateContainer', {
              appendPadding: 10,
              data: this.plotRotateData,
              angleField: 'value',
              colorField: 'type',
              radius: 0.9,
              label: {
                type: 'inner',
                offset: '-30%',
                content: ({ percent }) => `${(percent * 100).toFixed(0)}%`,
                style: {
                  fontSize: 14,
                  textAlign: 'center',
                },
              },
              interactions: [{ type: 'element-active' }],
            });
            this.rotatePlot.render();
          })
        }
      })
    },
    initPlot (list) {
      this.plot = {}
      for(let i in list) {
        let index = Number(i) + 1
        let ftype = TinyLine
        let option = {}
        switch(index){
          case 1:
            ftype = TinyLine;
            option = {
              height: 78,
              autoFit: false,
              data: list[i].data,
              smooth: true
            };
            break;
          case 2:
            ftype = TinyArea;
            option = {
              height: 78,
              autoFit: false,
              data: list[i].data,
              smooth: true
            };
            break;
          case 3:
            ftype = Liquid;
            option = {
              height: 78,
              percent: list[i].proportion,
              outline: {
                border: 2,
                distance: 2,
              },
              wave: {
                count: 4,
                length: 20,
              }
            };
            break;
          default:
            ftype = TinyLine;
            option = {
              height: 78,
              autoFit: false,
              data: list[i].data,
              smooth: true
            };
            break;
        }
        this.plot['plot'+1] = new ftype(`container${index}`, option)
        this.plot['plot'+1].render();
      }
    },
    handleDatePick(value) {
      this.calenderDay = value
    },
    getMessage () {
      let obj = {
        current: 1,
        size: 4,
      }
      messageaPage(obj).then(res=>{
        if(res.data.code == 0){
          this.messageList = JSON.parse(JSON.stringify(res.data.data.records))
          this.messageList.forEach(item => {
            item.msgContent = JSON.parse(item.msgContent)
          })
        }
      })
    },
    getDay (date) {
      return date.getFullYear() + '-' + ((date.getMonth()+1) > 9 ? (date.getMonth()+1) : ('0'+(date.getMonth()+1))) + '-' + (date.getDate() > 9 ? date.getDate() : ('0'+date.getDate()))
    },
    calendarClick (type, unit) {
      let y = this.calenderDay.getFullYear()
      let m = this.calenderDay.getMonth() + 1
      let d = this.calenderDay.getDate()
      if(unit == 'year') {
        if(type == 'last') {
          y -= 1
        }
        if(type == 'next') {
          y += 1
        }
      }
      if(unit == 'month') {
        if(type == 'last') {
          if(m > 1) {
            m -= 1
          }else{
            m = 12
            y -= 1
          }
        }
        if(type == 'next') {
          if(m < 11) {
            m += 1
          }else{
            m = 1
            y += 1
          }
        }
      }
      this.calenderDay = new Date(y+'-'+m+'-'+d)
    },
    viewMessDetail (data) {
      this.messData = data
      this.dialogVisible = true
    },
    // 关闭消息详情
    handleCloseMessage () {
      this.dialogVisible = false
      this.messData = {}
    },
    getEntryList () {
      this.applyData = []
      entryList().then(res => {
        if(res.data && res.data.code == 0 && res.data.data) {
          res.data.data.filter(rit => {
            if(rit.flowDesigns && rit.flowDesigns.length > 0) {
              this.applyData = this.applyData.concat(rit.flowDesigns)
            }
          })
        }
      })
    },
    // 关闭流程申请表单
    flowClose () {
      this.flowVisible = false
      this.afterSubmit = false
      this.title = ""
    },
    closeHandle (bool) {
      if(bool) {
        this.afterSubmit = true
      }
    },
    againOpen () {
      this.afterSubmit = false
      this.flowVisible = false
      this.flowVisible = true
    },
    // 申请
    applyHandle (it) {
      this.title = it.name
      this.approverList = []
      if(it.formId) {
        this.formId = it.formId
        this.formUrl = `/page-design-ui/#/form/info?id=${it.formId}&dataModelId=${it.dataModelId}&flowId=${it.id}&jvsAppId=${it.jvsAppId}&api=use&canDynamicAddNode=${it.canDynamicAddNode}`
        this.isOut = false
        if(it.manualNodes && it.manualNodes.length > 0) {
          for(let i in it.manualNodes) {
            // 发起人自选
            if((it.manualNodes[i].props && it.manualNodes[i].props.type == "SELF_SELECT" && (!it.manualNodes[i].props.targetObj.personnels || it.manualNodes[i].props.targetObj.personnels.length == 0))
              || (it.manualNodes[i].props.type == "ASSIGN_USER" && it.extend.enableDynamicApprover)) {
              this.approverList.push({
                nodeId: it.manualNodes[i].id,
                nodeName: it.manualNodes[i].name,
                approvers: it.manualNodes[i].props.targetObj.personnels || [],
                props: it.manualNodes[i].props
              })
            }
          }
        }
        this.flowVisible = true
      }else{
        this.$confirm('确认启动流程？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          let temp = {
            id: it.id
          }
          startProcess(temp).then(res => {
            if(res.data && res.data.code == 0) {
              // this.$message.success('启动成功')
              this.$notify({
                title: '提示',
                message: '启动成功',
                position: 'bottom-right',
                type: 'success'
              });
            }
          })
        }).catch(() => {});
      }
    },
    // 打开工作台
    viewTaskList () {
      this.$emit('openworkplace', true)
      window.parent.postMessage({command: 'openWorkplace'}, '*')
    }
  }
}
</script>

<style lang="scss" scoped>
.custom-view-page{
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  padding: 7px 5px;
  overflow: hidden;
  overflow-y: auto;
  .top{
    display: flex;
    justify-content: space-between;
    .left{
      width: 65%;
      .notice{
        margin-bottom: 15px;
      }
      .info-card{
        display: flex;
        align-items: center;
        justify-content: space-between;
        .info-card-item{
          flex: 1;
          display: flex;
          align-items: center;
          justify-content: space-between;
          background-color: #fff;
          border-radius: 10px;
          overflow: hidden;
          padding: 20px;
          padding-bottom: 15px;
          margin-left: 15px;
          div{
            font-size: 14px;
            color: #222;
            font-weight: 600;
            b{
              font-size: 34px;
              color: #232325;
            }
            span{
              font-size: 12px;
              color: #333;
              margin-left: 5px;
              font-weight: normal;
              word-break: keep-all;
            }
          }
          .fir{
            margin-bottom: 15px;
          }
          /deep/.plot-container{
            flex: 1;
            margin-left: 10px;
            height: 78px;
            .g2-html-annotation{
              font-size: 14px!important;
            }
          }
        }
        .info-card-item:nth-of-type(1){
          margin-left: 0;
        }
      }
      .only-info-card{
        .info-card-item{
          height: 150px;
          box-sizing: border-box;
        }
      }
    }
    .right{
      width: calc(35% - 15px);
      display: flex;
      align-items: center;
      justify-content: space-around;
      background-color: #fff;
      border-radius: 10px;
      overflow: hidden;
      h4, p{
        margin: 0;
        padding: 0;
      }
      h4{
        color: #323852;
        font-size: 18px;
      }
      p{
        color: #838196;
        margin-top: 5px;
        font-size: 14px;
      }
      .kf-img{
        display: block;
        width: 110px;
        height: 110px;
        overflow: hidden;
        img{
          display: block;
          width: 100%;
          height: 100%;
        }
      }
      .qr-img{
        display: block;
        width: 150px;
        height: 150px;
        overflow: hidden;
        img{
          display: block;
          width: 100%;
          height: 100%;
        }
      }
    }
  }
  .center{
    margin-top: 15px;
    display: flex;
    justify-content: space-between;
    .box{
      padding: 10px 20px;
      background-color: #fff;
      border-radius: 10px;
      overflow: hidden;
      margin-top: 15px;
      h4{
        padding: 10px 0;
        margin: 0;
        color: #222836;
        font-size: 20px;
      }
      .line-h4{
        border-bottom: 1px solid #F4F3F8;
        display: flex;
        align-items: center;
        justify-content: space-between;
        box-sizing: border-box;
        span, i{
          font-size: 12px;
          color: #CAC9CE;
          font-weight: normal;
        }
        i{
          margin-left: 5px;
        }
        div:nth-last-of-type(1) {
          cursor: pointer;
        }
      }
      .message-list-box{
        font-size: 14px;
        color: #222;
        margin-top: -10px;
        .mess-item{
          line-height: 32px;
          height: 32px;
          padding: 10px 0;
          display: flex;
          align-items: center;
          cursor: pointer;
          .lt{
            flex: 1;
            word-break: keep-all;
            display: flex;
            align-items: center;
            overflow: hidden;
            .con{
              flex: 1;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: pre;
            }
          }
          .rt{
            font-size: 12px;
            color: #a2a3a5;
            margin-left: 10px;
          }
          .line-bar{
            margin: 0 10px;
          }
        }
      }
    }
    .box:nth-of-type(1){
      margin-top: 0;
    }
    .left{
      width: 70%;
      .box{
        .task-list-box{
          /deep/.task-list{
            .jvs-table-top, .tablepagination{
              display: none;
            }
            .table-body-box{
              padding: 0;
              .el-table::before{
                display: none;
              }
              .el-table__header-wrapper{
                display: none;
              }
              .el-table__body-wrapper{
                height: auto!important;
                max-height: 308px;
                colgroup{
                  col:nth-of-type(1) {
                    display: none;
                  }
                }
                tbody{
                  tr{
                    td{
                      border: 0;
                      height: 44px;
                    }
                    td:nth-of-type(1) {
                      display: none;
                    }
                  }
                }
              }
            }
          }
        }
        .app-list-box{
          display: flex;
          justify-content: space-between;
          margin: 20px 0;
          .app-item{
            cursor: pointer;
            width: 6.5rem;
            min-width: 6.5rem;
            height: 7.375rem;
            display: flex;
            align-items: center;
            flex-direction: column;
            justify-content: center;
            .app-img{
              height: 3.125rem;
              display: flex;
              align-items: center;
              justify-content: center;
              img{
                display: block;
                width: 100%;
                height: 100%;
              }
            }
          }
          .app-item:hover{
            box-shadow: 0 0.375rem 1.875rem 0.0625rem rgba(0, 140, 255, .1);
            border-radius: 1rem 1rem 1rem 1rem;
          }
        }
      }
    }
    .right{
      width: calc(30% - 15px);
      .box{
        .my-calendar, .mc-tool{
          display: flex;
          align-items: center;
          justify-content: center;
          /deep/.el-date-table{
            width: 100%;
          }
        }
        .mc-tool{
          margin-bottom: 10px;
          font-size: 14px;
          width: 100%;
          padding: 0 20px;
          box-sizing: border-box;
          span{
            text-align: center;
            flex: 1;
          }
          i{
            margin-left: 10px;
            color: #3471ff;
            font-weight: bold;
            background-color: #f1f4fd;
            padding: 5px;
            border-radius: 50%;
            overflow: hidden;
            cursor: pointer;
          }
          i:nth-of-type(1){
            margin-left: 0;
          }
        }
      }
    }
  }
  .empty-block{
    min-height: 160px;
    background-image: url('../../const/img/emptyImage.svg');
    background-repeat: no-repeat;
    background-position: center;
    background-size: 260px 123px;
    position: relative;
    padding-top: 10px;
  }
  .empty-block::after{
    content: '暂无数据';
    line-height: 30px;
    color: #909399;
    font-size: 12px;
    text-align: center;
    display: block;
    width: 100%;
    position: absolute;
    top: 148px;
  }
}
.mess-title{
  font-size: 24px;
  color: #333333;
  text-align: center;
}
/deep/.apply-content{
  max-height: 200px;
  overflow: hidden;
  padding: 0 10px;
  padding-bottom: 10px;
  .el-input-group{
    width: 300px;
    .el-input-group__append{
      background-color: #ffffff;
      padding: 0 16px;
    }
  }
  .category-list{
    margin: 20px 0;
    display: flex;
    .category-item{
      color: #a2a3a5;
      padding: 6px 16px;
      cursor: pointer;
      border-radius: 4px;
    }
    .category-item-active{
      background-color: #e6e6e6;
      color: #333333;
    }
  }
  .type-list{
    display: flex;
    flex-wrap: wrap;
    .entry-item-card{
      position: relative;
      width: 230px;
      height: 68px;
      display: flex;
      align-items: center;
      background: #fff;
      box-shadow: 0 0 8px #f0f0f0;
      border-radius: 6px;
      padding-right: 10px;
      margin-bottom: 40px;
      box-sizing: border-box;
      cursor: pointer;
      margin: 27px;
      margin-left: 0;
      margin-bottom: 0;
      &:hover{
        box-shadow: 0 0 8px #e0e0e0;
        span{
          color: #3471FF;
        }
      }
      .icon{
        width:40px;
        height:40px;
        margin-left: 10px;
      }
      i{
        font-size: 36px;
        margin-left: 10px;
        color: #FC723C;
      }
      span{
        font-size: 13px;
        //margin-left: 20px;
        color: #333;
        word-break: keep-all;
        //display: inline-block;
        width: 135px;
        overflow: hidden;
        white-space: pre;
        text-overflow: ellipsis;
      }
    }
  }
}
</style>
