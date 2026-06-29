<template>
  <div class="process-info">
    <div class="task-form-header">
      <div class="task-form-header-title">{{taskDataInfo.taskName}}</div>
      <div class="task-form-header-tool">
        <el-popover
          v-if="printTemplateList && printTemplateList.length > 0 && defaultFormData && defaultFormData.dataId"
          placement="bottom"
          trigger="hover">
          <div class="print-list-items">
            <ul>
              <li v-for="pi in printTemplateList" :key="pi.id" @click="printHandle(pi)">
                <span>{{pi.name}}</span>
              </li>
            </ul>
          </div>
          <el-button slot="reference" type="text" icon="el-icon-printer">打印</el-button>
        </el-popover>
        <el-button v-if="allTaskList && allTaskList.length > 1" type="text" icon="el-icon-s-operation" style="margin-left: 16px;" @click="viewAllTaskList">历史进度</el-button>
        <el-button type="text" icon="el-icon-user-solid" style="margin-left: 16px;" @click="handleFlowChart">流程详情</el-button>
        <i class="el-icon-full-screen" @click="dialogToolEvent('fullscreenChange')"></i>
        <i class="el-icon-close" @click="dialogToolEvent('closeHandle', true)"></i>
      </div>
    </div>
    <div :class="{'task-form-body': true, 'task-form-body-hideRight': hideProcessList}">
      <div class="left-part">
        <div class="apply-info">
          <div class="apply-info-headimg">
            <el-avatar shape="square" :src="createAvatar"></el-avatar>
          </div>
          <div class="apply-info-right">
            <div class="create-by">
              <span>{{ taskDataInfo.taskTitle || taskDataInfo.taskName }}</span>
              <span v-if="taskDataInfo.taskStatus" :class="`status-tag status-${taskDataInfo.taskStatus}`">{{taskDataInfo.taskStatus | getTaskStatus}}</span>
            </div>
            <div class="apply-info-box">
              <div class="apply-info-content">
                <div class="content-item">提交时间：<div v-if="taskDataInfo">{{ taskDataInfo.createTime }}</div></div>
                <div class="content-item">发起人：<div>{{ createBy }}</div></div>
                <div class="content-item">发起人部门：<div>{{ deptName }}</div></div>
              </div>
              <div v-if="taskDataInfo && taskDataInfo.taskCode" class="apply-info-content">
                <div class="content-item">流程编号：<div>{{ taskDataInfo.taskCode }}</div></div>
              </div>
            </div>
          </div>
        </div>
        <div v-for="fitem in nodeList" :key="fitem.nodeId" v-if="fitem.formId" class="form-info">
          <div v-if="fitem.formId" class="header-title">
            <svg class="icon" aria-hidden="true">
              <use xlink:href="#icon-jvs-rongqi"></use>
            </svg>
            <span>{{nodeList.length > 1 ? fitem.nodeName : '表单信息'}}</span>
          </div>
          <showForm v-if="!fitem.isOut && fitem.formShow && flowDesign" :formUrl="fitem.formUrl" :flowDesign="flowDesign" :defaultFormData="fitem.defaultFormData" :btnHide="btnHide" :fromWorkPlace="fromWorkPlace" @close="closeHandle" />
        </div>
      </div>
      <div class="right-part">
        <div class="right-part-fixed-tool" @click="hideProcessList=!hideProcessList;">
          <i v-if="hideProcessList" class="el-icon-d-arrow-left"></i>
          <i v-else class="el-icon-d-arrow-right"></i>
        </div>
        <div class="right-part-top">
          <div class="right-top-title">
            <svg class="icon" aria-hidden="true">
              <use xlink:href="#icon-jvs-rongqi"></use>
            </svg>
            <span>审批进度</span>
          </div>
          <div class="right-top-btn">
            <span class="right-top-btn-item" @click="handleFlowChart">流程详情</span>
          </div>
        </div>
        <div class="task-time-line">
          <el-timeline>
            <el-timeline-item v-for="(item, index) in historyList" :key="item.nodeId+'_'+index" :color="getNodeColor(item)">
              <div class="timeLine-node" style="align-items: center;">
                <div class="timeLine-node-time">{{item.time || '待审批'}}</div>
                <div v-if="item.nodeName !== '结束'">
                  <i v-if="item.close" class="el-icon-arrow-down" style="margin-left: 10px;cursor: pointer;" @click="item.close=false;$forceUpdate();"></i>
                  <i v-else class="el-icon-arrow-up" style="margin-left: 10px;cursor: pointer;" @click="item.close=true;$forceUpdate();"></i>
                </div>
              </div>
              <el-collapse-transition>
                <div v-show="!item.close" :class="{'timeLine-node-card': true, 'over': item.nodeName == '结束'}">
                  <div class="timeLine-node-card-head">
                    <div class="timeLine-node-card-top">
                      <div class="timeLine-node-card-top-left">
                        <el-tooltip class="item" effect="dark" :content="item.terminated ? (item.terminatedType == 'WITHDRAW' ? '发起人撤回' : '终止') : item.nodeName" placement="top-start">
                          <span style="max-width: 70px;overflow:hidden;white-space: nowrap;text-overflow: ellipsis;font-size: 12px;font-weight: 600;">{{item.terminated ? (item.terminatedType == 'WITHDRAW' ? '发起人撤回' : '终止') : item.nodeName}}</span>
                        </el-tooltip>
                        <el-tag size="mini" type="info" effect="dark" color="#FF9736" v-if="getModeLabel(item.mode)" style="margin-left: 8px;">{{ getModeLabel(item.mode) }}</el-tag>
                      </div>
                      <span v-if="item.dataVersion  && item.nodeName !== '结束' && !item.terminated && item.nodeType !== 'AUTOMATION'" class="timeLine-node-card-top-btn" @click="viewChange(item)">提交记录</span>
                    </div>
                    <div v-if="item.mode == 'AND' && item.modeProps" style="font-size: 12px;margin-top: 10px;color: #6F7588;">
                      <span>{{item.modeProps ? (item.modeProps.value+'%') : '100%'}}</span>
                      <span style="margin-left: 5px;" v-if="item.mode == 'AND' && item.modeProps"> / {{item.passRate || 0}}{{item.modeProps && item.modeProps.countersignRule == 'RATIO' ? '%' : ''}}</span>
                    </div>
                  </div>
                  <div class="timeLine-node-card-item" v-for="(it, key) in item.approveResultDtos" :key="key" v-show="(item.terminated && item.terminatedType !== 'TERMINATION') ? false : true">
                    <div class="node-avatar">
                      <el-avatar size="medium" shape="square" :src="getUserAvatar(it, users)"></el-avatar>
                      <el-tooltip class="item" effect="dark" :content="it.userName" placement="top-start">
                        <span>{{it.userName}}</span>
                      </el-tooltip>
                    </div>
                    <div class="timeLine-node-content">
                      <div v-if="item.approveResultDtos && item.approveResultDtos.length > 0 && index > 0 && !item.terminated" class="timeLine-node-content-div">
                        <span v-if="['ADD_SIGNER', 'REMOVE_SIGNER'].indexOf(it.nodeOperationTypeEnum) == -1" class="label">审批进度：</span>
                        <span :style="`color: ${getTypeColor(it)};`">{{getTypeEnumLabel(it)}}</span>
                      </div>
                      <div v-if="item.approveResultDtos && item.approveResultDtos.length > 0 && it.opinion && it.opinion.content" class="timeLine-node-content-div">
                        <el-tooltip v-if="it.opinion.content.length > 16" class="item" effect="dark" :content="it.opinion.content" placement="top-start">
                          <span>{{it.opinion.content.length > 16 ? it.opinion.content.substring(0, 16) + '...' : it.opinion.content}}</span>
                        </el-tooltip>
                        <span v-else>{{it.opinion.content}}</span>
                      </div>
                      <div v-if="it.appendApproval && it.appendApproval.personnels && it.appendApproval.personnels.length > 0" class="timeLine-node-content-div">
                        <span>{{getPersonName(it.appendApproval.personnels)}}</span>
                      </div>
                      <div v-if="it.transfer && it.transfer.proxyUserName" class="timeLine-node-content-div">
                        <span>{{it.transfer.proxyUserName}}</span>
                      </div>
                      <div v-if="item.terminated && item.terminatedReason" class="timeLine-node-content-div">
                        <el-tooltip v-if="item.terminatedReason.length > 16" class="item" effect="dark" :content="item.terminatedReason" placement="top-start">
                          <span>{{item.terminatedReason.length > 16 ? item.terminatedReason.substring(0, 16) + '...' : item.terminatedReason}}</span>
                        </el-tooltip>
                        <span v-else>{{item.terminatedReason}}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </el-collapse-transition>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </div>
    <!-- 变更信息 -->
    <changeInfo ref="changeInfo" :fromWorkPlace="fromWorkPlace" :jvsAppId="rowData.jvsAppId"></changeInfo>
    <!-- 流程详情 -->
    <el-dialog
      title="流程详情"
      :visible.sync="flowChartVisible"
      append-to-body
      fullscreen
      :close-on-click-modal="false"
      class="flow-progress-info-dialog"
      :before-close="handleFlowChartClose">
      <div class="flow-design justview-flow-design" :style="'transform: scale('+ 100 / 100 +');'">
        <process-tree ref="process-tree" :process="currentFlowDesign ? currentFlowDesign : flowDesign" :fromWorkPlace="fromWorkPlace" />
      </div>
    </el-dialog>
    <!-- 历史进度 -->
    <el-drawer
      title="历史进度"
      append-to-body
      :visible.sync="allTaskInfoVisible"
      direction="rtl"
      :before-close="allTaskInfoClose">
      <div v-if="allTaskInfoVisible" class="all-task-info-list">
        <div v-for="(task, tix) in allTaskInfo" :key="'task-info-item-'+tix">
          <div style="display: flex; align-items:center;justify-content: space-between;">
            <div class="header-title">{{task.taskName}}</div>
            <el-button type="text" icon="el-icon-user-solid" style="margin-left: 16px;" @click="viewCurrentFlowDesign(task.flowDesign)">流程详情</el-button>
          </div>
          <el-timeline>
            <el-timeline-item v-for="(item, index) in task.flowTaskProgress" :key="item.nodeId+'_'+index">
              <div class="timeLine-node" :style="(item.mode == 'AND' && item.modeProps && item.modeProps.countersignRule == 'RATIO') ? 'margin-bottom: 30px;' : ''">
                <el-tooltip class="item" effect="dark" :content="item.terminated ? (item.terminatedType == 'WITHDRAW' ? '发起人撤回' : '终止') : item.nodeName" placement="top-start">
                  <span style="max-width: 70px;overflow:hidden;white-space: nowrap;text-overflow: ellipsis;font-size: 12px;font-weight: 600;">{{item.terminated ? (item.terminatedType == 'WITHDRAW' ? '发起人撤回' : '终止') : item.nodeName}}</span>
                </el-tooltip>
                <el-tag size="mini" type="info" effect="dark" color="#ff943e" v-if="getModeLabel(item.mode)" style="margin-left:10px;">{{ getModeLabel(item.mode) }}</el-tag>
                <div style="position: absolute;left: 0;top: 25px;font-size: 12px;color: #909399;">
                  <span v-if="item.mode == 'AND' && item.modeProps">{{item.modeProps ? (item.modeProps.value+'%') : '100%'}}</span>
                  <span style="margin-left: 5px;" v-if="item.mode == 'AND' && item.modeProps">/ {{item.passRate || 0}}{{item.modeProps && item.modeProps.countersignRule == 'RATIO' ? '%' : ''}}</span>
                </div>
                <div class="timeLine-node-content-right">
                  <div class="time">
                    {{item.time}}
                  </div>
                  <jvs-button v-if="item.dataVersion && item.nodeName !== '结束' && !item.terminated && item.nodeType !== 'AUTOMATION'" size="mini" type="text" @click="viewChange(item)">提交记录</jvs-button>
                </div>
              </div>
              <div class="timeLine-node" v-for="(it, key) in item.approveResultDtos" :key="key" v-show="(item.terminated && item.terminatedType !== 'TERMINATION') ? false : true">
                <div class="node-avatar">
                  <el-avatar size="medium" shape="square" :src="getUserAvatar(it, task.users || [])"></el-avatar>
                </div>
                <div class="timeLine-node-content">
                  <el-tooltip class="item" effect="dark" :content="it.userName" placement="top-start">
                    <span style="color: #a2a3a5;">{{it.userName}}</span>
                  </el-tooltip>
                  <div class="timeLine-node-content-div">
                    <span v-if="item.approveResultDtos && item.approveResultDtos.length > 0 && index > 0 && !item.terminated" :style="`color: ${getTypeColor(it)};`">
                      {{getTypeEnumLabel(it)}}
                    </span>
                    <el-tooltip v-if="item.approveResultDtos && item.approveResultDtos.length > 0 && it.opinion && it.opinion.content" class="item" effect="dark" :content="it.opinion.content" placement="top-start">
                      <span style="font-weight: normal;color: #a2a3a5">（{{it.opinion.content.length > 6 ? it.opinion.content.substring(0, 6) + '...' : it.opinion.content}}）</span>
                    </el-tooltip>
                    <el-tooltip v-if="it.appendApproval && it.appendApproval.personnels && it.appendApproval.personnels.length > 0" class="item" effect="dark" :content="getPersonName(it.appendApproval.personnels)" placement="top-start">
                      <span style="font-weight: normal;color: #a2a3a5">（{{getPersonName(it.appendApproval.personnels)}}）</span>
                    </el-tooltip>
                    <el-tooltip v-if="it.transfer && it.transfer.proxyUserName" class="item" effect="dark" :content="it.transfer.proxyUserName" placement="top-start">
                      <span style="font-weight: normal;color: #a2a3a5">（{{it.transfer.proxyUserName}}）</span>
                    </el-tooltip>
                    <el-tooltip v-if="item.terminated && item.terminatedReason" class="item" effect="dark" :content="item.terminatedReason" placement="top-start">
                      <span style="font-weight: normal;color: #a2a3a5">（{{item.terminatedReason.length > 14 ? item.terminatedReason.substring(0, 14) + '...' : item.terminatedReason}}）</span>
                    </el-tooltip>
                  </div>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </el-drawer>
  </div>
</template>
<script>
import {queryDetailHistoryInfo, queryEchoForm, queryEchoFormFromWorkPlace, getAllTaskList, getAllTaskListInfo} from '../api/flowable'
import showForm from './componet/info'
import ProcessTree from './design/process/ProcessTree.vue'
import changeInfo from './componet/changeInfo.vue'
import { getAvailableTemplate } from '@/views/print/api/index'
export default {
  name: 'process-info',
  components: {ProcessTree, showForm, changeInfo},
  props: {
    rowData: {
      type: Object
    },
    btnHide: {
      type: Boolean,
      default: false
    },
    fromWorkPlace: {
      type: Boolean
    }
  },
  filters: {
    getTaskStatus (status) {
      let label = ''
      switch(status) {
        case  1: label = '待审批';break;
        case  2: label = '已通过';break;
        case  3: label = '已拒绝';break;
        case  4: label = '已终止';break;
        default: ;break;
      }
      return label
    }
  },
  data(){
    return {
      createAvatar: '',
      deptName: '',
      formUrl: '',
      historyList: [],
      startLogOption: {
        addBtn: false,
        menu: false,
        showOverflow: true,
        search: false,
        align: 'center',
        menuAlign: 'center',
        hideTop: true,
        column: [
          {
            label: '发起人',
            prop: 'userName'
          },
          {
            label: '时间',
            prop: 'time',
            slot: true
          }
        ]
      },
      logOption: {
        addBtn: false,
        menu: false,
        showOverflow: true,
        search: false,
        align: 'center',
        menuAlign: 'center',
        hideTop: true,
        column: [
          {
            label: '审批人',
            prop: 'userName'
          },
          {
            label: '审批状态',
            prop: 'nodeOperationTypeEnum',
            slot: true
          },
          {
            label: '时间',
            prop: 'time',
            slot: true
          }
        ]
      },
      isOut: false,
      defaultFormData: {}, // 表单数据值
      formShow: false, // 设计表单显示
      flowDesign: null, // 流程图节点信息
      flowChartVisible: false,
      printTemplateList: [], // 可打印的模板列表
      nodeList: [], // 节点列表
      allTaskList: [],
      allTaskInfo: [],
      allTaskInfoVisible: false,
      currentFlowDesign: null,
      taskDataInfo: {}, // 任务详情数据
      createBy: '',
      hideProcessList: false
    }
  },
  created () {
    if(this.rowData) {
      // 获取所有的任务列表
      this.getAllTaskList ()
      this.init()
      // 获取打印列表
      if(this.rowData.formId) {
        this.getAvailableTemplateHandle(this.rowData.formId)
      }
    }
  },
  methods: {
    // 获取头像
    getUserAvatar(obj, users) {
      const index = users.findIndex(item => {
        return item.id === obj.userId
      })
      return index > -1 ? users[index].headImg : ''
    },
    // 关闭流程详情
    handleFlowChartClose() {
      this.flowChartVisible = false
      this.currentFlowDesign = null
    },
    // 查看流程图
    handleFlowChart() {
      this.flowChartVisible = true
    },
    // 获取审批人
    getUserName(arr) {
      let users = ''
      arr.forEach(item => {
        users += `${item.userName},`
      })
      return users.substring(0, users.length - 1)
    },
    init () {
      if(this.rowData.dataId) {
        this.queryDetailHistoryInfoHandle()
      }
    },
    // 表单回显
    async getEchoFormData (item) {
      if(this.fromWorkPlace) {
        await queryEchoFormFromWorkPlace(this.rowData.id, this.rowData.dataModelId, this.rowData.dataId, item.formId).then(res => {
          if(res.data.code == 0) {
            item.defaultFormData = res.data.data
            item.formShow = true
          }
        })
      }else{
        await queryEchoForm(this.rowData.jvsAppId, this.rowData.dataModelId, this.rowData.dataId, item.formId).then(res => {
          if(res.data.code == 0) {
            item.defaultFormData = res.data.data
            item.formShow = true
          }
        })
      }
    },
    // 流程信息
    async queryDetailHistoryInfoHandle () {
      if(this.rowData.id) {
        await queryDetailHistoryInfo(this.rowData.id).then(res => {
          if(res.data && res.data.code == 0) {
            this.taskDataInfo = res.data.data
            console.log(this.taskDataInfo)
            this.users = [...res.data.data.users] || []
            this.historyList = res.data.data.flowTaskProgress.filter(item => {
              item.close = false
              return item.nodeType !== 'CS'
            })
            const index = this.users.findIndex(item => {
              return item.id === this.historyList[0].approveResultDtos[0].userId
            })
            if(this.users[index].dept && this.users[index].dept.length > 0) {
              let lis = []
              this.users[index].dept.filter(dit => {
                lis.push(dit.deptName)
              })
              this.deptName = lis.join(',')
            }
            this.createBy = this.users[index].realName
            this.createAvatar = this.getUserAvatar(this.historyList[0].approveResultDtos[0], this.users)
            this.flowDesign = res.data.data.flowDesign
            this.nodeList = res.data.data.nodes
          }
        })
        for(let i in this.nodeList) {
          this.nodeList[i].isOut = false
          this.nodeList[i].formShow = false
          if(this.nodeList[i].formId) {
            this.getAvailableTemplateHandle(this.nodeList[i].formId)
            // 表单回显数据
            await this.getEchoFormData(this.nodeList[i])
            this.nodeList[i].formUrl = `/page-design-ui/#/form/info?id=${this.nodeList[i].formId}&dataModelId=${this.rowData.dataModelId}&jvsAppId=${this.rowData.jvsAppId}&api=use`
            if(this.nodeList[i].formUrl) {
              if(this.nodeList[i].formUrl.startsWith('htt')) {
                this.nodeList[i].isOut = true
              }else{
                if(this.nodeList[i].formUrl.startsWith('/page-design-ui')) {
                  this.nodeList[i].isOut = false
                }else{
                  this.nodeList[i].isOut = true
                }
              }
            }
          }
        }
        if(this.nodeList && this.nodeList.length == 1) {
          this.defaultFormData = this.nodeList[0].defaultFormData
        }
        // console.log(this.nodeList)
        this.$forceUpdate()
      }
    },
    closeHandle (bool) {
      if(bool) {
        this.$emit('close', bool)
      }
    },
    getModeLabel (val) {
      let label = ''
      switch(val) {
        case 'AND':
          label = '会签';break;
        case 'OR':
          label = '或签';break;
        case 'NEXT':
          label = '顺签';break;
        default: ;break;
      }
      return label
    },
    getTypeEnumLabel (row) {
      let label = '-'
      switch(row.nodeOperationTypeEnum) {
        case 'PASS':
          label = '已通过';break;
        case 'REFUSE':
          label = '已拒绝';break;
        case 'BACK':
          label = '驳回';break;
        case 'APPEND':
          label = '加签';break;
        case 'ADD_SIGNER':
          label = '添加审批人';break;
        case 'REMOVE_SIGNER':
          label = '移除审批人';break;
        default:
          label = '处理中';break;
      }
      if(row.transfer) {
        label = `转交`
        if(row.transfer.proxy) {
          label = `代理`
        }
      }
      if(row.appendApproval && row.appendApproval.point) {
        if(row.appendApproval.point == 'BEFORE'){
          label = '前加签'
        }
        if(row.appendApproval.point == 'AFTER'){
          label = '后加签'
        }
      }
      return label
    },
    getTypeColor(row) {
      let color = '#1E6FFF'
      switch(row.nodeOperationTypeEnum) {
        case 'PASS':
          color = '#36B452';break;
        case 'REFUSE':
          color = '#FF194C';break;
        case 'BACK':
          color = '#909399';break;
        default:
          color = '#FF9736';break;
      }
      return color
    },
    viewChange (item) {
      let temp = JSON.parse(JSON.stringify(item))
      this.$set(temp, 'dataId', this.rowData.dataId)
      this.$set(temp, 'dataModelId', this.rowData.dataModelId)
      this.$refs.changeInfo.openDialog(temp)
    },
    // 表单获取可打印的模板列表
    getAvailableTemplateHandle (formId) {
      if(formId) {
        getAvailableTemplate(this.rowData.jvsAppId, formId).then(res => {
          if(res && res.data && res.data.code == 0) {
            this.printTemplateList = res.data.data
          }
        })
      }
    },
    printHandle (row) {
      if(this.defaultFormData && this.defaultFormData.dataId) {
        if(row.designType == 1) {
          this.$openUrl(`/mgr/jvs-design/app/use/${this.rowData.jvsAppId}/print/template/file/preview/${row.id}/${this.rowData.dataModelId}/${row.designId}/${this.defaultFormData.dataId}?access_token=${this.$store.getters.access_token}`, '_blank')
        }else{
          this.$openUrl(`/jvs-print-ui/#/print/show?id=${row.id}&name=${row.name}&designId=${row.designId}&dataModelId=${this.rowData.dataModelId}&dataId=${this.defaultFormData.dataId}&jvsAppId=${this.rowData.jvsAppId}&taskId=${this.rowData.id}`, '_blank')
        }
      }
    },
    getPersonName (list) {
      let nameStr  = []
      list.filter(pit => {
        nameStr.push(pit.name)
      })
      return nameStr.join(',')
    },
    getAllTaskList () {
      if(this.rowData.jvsAppId && this.rowData.dataModelId && this.rowData.dataId) {
        getAllTaskList(this.rowData.jvsAppId, this.rowData.dataModelId, this.rowData.dataId).then(res => {
          if(res.data && res.data.code == 0) {
            this.allTaskList = res.data.data
          }
        })
      }
    },
    viewAllTaskList () {
      getAllTaskListInfo(this.allTaskList).then(res => {
        if(res.data && res.data.code == 0) {
          this.allTaskInfo = res.data.data
          this.allTaskInfoVisible = true
        }
      })
    },
    allTaskInfoClose () {
      this.allTaskInfoVisible = false
    },
    viewCurrentFlowDesign (item) {
      this.currentFlowDesign = item
      this.flowChartVisible = true
    },
    dialogToolEvent (event, data) {
      this.$emit(event, data)
    },
    getNodeColor (item) {
      let color = ''
      if(item.nodeType == 'ROOT') {
        color = '#1E6FFF'
      }else{
        if(item.approveResultDtos && item.approveResultDtos.length > 0) {
          switch(item.approveResultDtos[0].nodeOperationTypeEnum) {
            case 'PASS':
              color = '#36B452';break;
            case 'REFUSE':
            case 'TERMINATED':
            case 'TERMINATION':
            case 'BACK':
              color = '#FF194C';break;
            default:
              color = '#FF9736';break;
          }
        }
      }
      return color
    }
  }
}
</script>
<style lang="scss" scoped>
.justview-flow-design{
  /deep/.node{
    .node-body{
      .node-back-box{
        display: none;
      }
    }
    .node-footer{
      .btn{
        visibility: hidden;
      }
    }
  }
  /deep/.branch-node{
    .add-branch-btn{
      .add-branch-btn-div{
        visibility: hidden;
        &::after{
          content: "";
          position: absolute;
          height: 100%;
          border-left: 1px solid #C2C5CF;
          left: 50%;
          top: 0;
          visibility: visible;
        }
      }
    }
  }
}
.all-task-info-list{
  padding: 0 10px;
  box-sizing: border-box;
  height: calc(100vh - 80px);
  overflow: hidden;
  overflow-y: auto;
  .header-title{
    color: #333333;
    font-size: 18px;
    font-weight: 600;
    position: relative;
    padding-left: 23px;
    margin-left: 13px;
  }
  .header-title::before{
    position: absolute;
    content: "";
    width: 4px;
    height: 18px;
    background: #3471ff;
    border-radius: 2px;
    left: 0;
    top: 4px;
    cursor: auto;
  }
  .el-timeline{
    margin-top: 10px;
    padding-left: 10px;
   .el-timeline-item{
     .timeLine-node{
       display: flex;
       position: relative;
       margin-bottom: 16px;
        .el-tag--dark.el-tag--info{
          border: none;
        }
        .node-avatar{
          margin-right: 10px;
        }
        .timeLine-node-content{
          display: flex;
          flex-direction: column;
          width: calc(100% - 46px);
          span{
            margin-bottom: 2px;
            max-width: 70px;
            overflow:hidden;
            white-space: nowrap;
            text-overflow: ellipsis;
            font-size: 12px;
            font-weight: 600;
          }
         .timeLine-node-content-div{
            width: 100%;
            overflow:hidden;
            white-space: nowrap;
            text-overflow: ellipsis;
          }
       }
       .timeLine-node-content-right{
          position: absolute;
          right: 0;
          top: 0;
          text-align: right;
          .time{
            font-size: 12px;
          }
       }
     }
     .el-timeline-item__content{
        .history-item{
          box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
          padding: 8px 10px;
          margin: 20px;
       }
     }
   }
  }
}
</style>
<style lang="scss" scoped>
.process-info{
  height: 100%;
  .task-form-header{
    width: 100%;
    height: 68px;
    border-bottom: 1px solid #EEEFF0;
    padding: 0 24px;
    box-sizing: border-box;
    display: flex;
    align-items: center;
    justify-content: space-between;
    .task-form-header-title{
      height: 26px;
      font-family: Microsoft YaHei-Bold, Microsoft YaHei;
      font-weight: 700;
      font-size: 20px;
      color: #363B4C;
      line-height: 26px;
    }
    .task-form-header-tool{
      display: flex;
      align-items: center;
      i{
        font-size: 18px;
        color: #6F7588;
        cursor: pointer;
        margin-left: 18px;
      }
    }
  }
  .task-form-body{
    display: flex;
    height: calc(100% - 68px);
    overflow: hidden;
    .left-part{
      width: calc(100% - 284px);
      overflow: hidden;
      display: flex;
      flex-direction: column;
      .apply-info{
        padding: 24px;
        display: flex;
        border-bottom: 8px solid #F5F6F7;
        .apply-info-headimg{
          margin-right: 16px;
          .el-avatar{
            width: 56px;
            height: 56px;
          }
        }
        .apply-info-right{
          .create-by{
            height: 21px;
            font-family: Microsoft YaHei-Bold, Microsoft YaHei;
            font-weight: 700;
            font-size: 16px;
            color: #363B4C;
            line-height: 21px;
            display: flex;
            align-items: center;
            .status-tag{
              margin-left: 12px;
              padding: 0 7px;
              color: #fff;
              border-radius: 4px;
              font-family: Source Han Sans-Regular, Source Han Sans;
              font-weight: 400;
              font-size: 14px;
              overflow: hidden;
              &.status-1{
                background-color: #FF9736;
              }
              &.status-2{
                background-color: #36B452;
              }
              &.status-3{
                background-color: #FF194C;
              }
              &.status-4{
                background-color: #E4E7ED;
              }
            }
          }
          .task-code{
            font-family: Microsoft YaHei, Microsoft YaHei;
            font-weight: 400;
            font-size: 12px;
            color: #6F7588;
            line-height: 14px;
          }
          .apply-info-content{
            display: flex;
            margin-top: 14px;
            .content-item{
              display: flex;
              align-items: center;
              font-family: Microsoft YaHei-Regular, Microsoft YaHei;
              font-weight: 400;
              font-size: 14px;
              color: #6F7588;
              margin-left: 80px;
              height: 20px;
              line-height: 20px;
              word-break: keep-all;
              white-space: nowrap;
              div{
                font-family: Source Han Sans-Medium, Source Han Sans;
                font-weight: 500;
                font-size: 14px;
                color: #363B4C;
                word-break: keep-all;
                white-space: nowrap;
              }
            }
            .content-item:nth-of-type(1){
              margin-left: 0;
            }
          }
        }
      }
      .form-info{
        flex: 1;
        padding: 16px 0;
        box-sizing: border-box;
        overflow: hidden;
        overflow-y: auto;
        .header-title{
          display: flex;
          align-items: center;
          height: 21px;
          font-family: Microsoft YaHei-Regular, Microsoft YaHei;
          font-weight: 400;
          font-size: 16px;
          color: #363B4C;
          line-height: 21px;
          padding: 0 20px;
          margin-bottom: 16px;
          .icon{
            width: 16px;
            height: 16px;
            margin-right: 8px;
          }
        }
        .form-show-info{
          padding: 0;
          position: relative;
        }
      }
    }
    .right-part{
      width: 284px;
      background: #F5F6F7;
      position: relative;
      padding: 16px;
      .right-part-fixed-tool{
        position: absolute;
        left: -24px;
        top: calc(50% - 24px);
        width: 24px;
        height: 48px;
        background: #C2C5CF;
        border-radius: 6px 0px 0px 6px;
        text-align: center;
        line-height: 48px;
        cursor: pointer;
        i{
          color: #fff;
          font-size: 14px;
        }
      }
      .right-part-top{
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 24px;
        .right-top-title{
          display: flex;
          align-items: center;
          .icon{
            width: 16px;
            height: 16px;
            margin-right: 8px;
          }
          span{
            height: 21px;
            font-family: Microsoft YaHei-Regular, Microsoft YaHei;
            font-weight: 400;
            font-size: 16px;
            color: #363B4C;
            line-height: 21px;
          }
        }
        .right-top-btn{
          text-align: right;
          .right-top-btn-item{
            cursor: pointer;
            height: 18px;
            font-family: Microsoft YaHei-Regular, Microsoft YaHei;
            font-weight: 400;
            font-size: 14px;
            color: #1E6FFF;
            line-height: 18px;
          }
        }
      }
      .task-time-line{
        height: calc(100% - 45px);
        padding: 0 5px;
        overflow: hidden;
        overflow-y: auto;
      }
      /deep/.el-timeline{
        padding-left: 0;
        .el-timeline-item{
          .el-timeline-item__tail{
            border-color: #C2C5CF;
            height: calc(100% - 28px);
            top: 20px;
          }
          .el-timeline-item__wrapper{
            padding-left: 26px;
            .timeLine-node{
              display: flex;
              position: relative;
              .timeLine-node-time{
                height: 20px;
                font-family: Source Han Sans-Regular, Source Han Sans;
                font-weight: 400;
                font-size: 14px;
                color: #6F7588;
                line-height: 20px;
              }
              .timeLine-node-content{
                display: flex;
                flex-direction: column;
                width: calc(100% - 46px);
                span{
                  margin-bottom: 2px;
                  max-width: 70px;
                  overflow:hidden;
                  white-space: nowrap;
                  text-overflow: ellipsis;
                  font-size: 12px;
                  font-weight: 600;
                }
                .timeLine-node-content-div{
                  width: 100%;
                  overflow:hidden;
                  white-space: nowrap;
                  text-overflow: ellipsis;
                }
              }
              .timeLine-node-content-right{
                position: absolute;
                right: 0;
                top: 0;
                text-align: right;
                .timeLine-node-content-right-btn{
                  height: 20px;
                  font-family: Source Han Sans-Regular, Source Han Sans;
                  font-weight: 400;
                  font-size: 14px;
                  color: #1E6FFF;
                  line-height: 20px;
                  cursor: pointer;
                }
              }
            }
            .timeLine-node-card{
              width: 100%;
              border-radius: 4px;
              background: #FFFFFF;
              overflow: hidden;
              margin-top: 8px;
              padding: 12px 16px;
              box-sizing: border-box;
              .timeLine-node-card-head{
                padding-bottom: 12px;
                border-bottom: 1px solid #EEEFF0;
              }
              .timeLine-node-card-top{
                display: flex;
                align-items: center;
                justify-content: space-between;
                .timeLine-node-card-top-left{
                  display: flex;
                  align-items: center;
                  .el-tag--dark.el-tag--info{
                    border: none;
                    height: 20px;
                    padding: 0 8px;
                  }
                }
                .timeLine-node-card-top-btn{
                  height: 20px;
                  font-family: Source Han Sans-Regular, Source Han Sans;
                  font-weight: 400;
                  font-size: 14px;
                  color: #1E6FFF;
                  line-height: 20px;
                  cursor: pointer;
                }
              }
              .timeLine-node-card-item{
                padding-bottom: 12px;
                border-bottom: 1px solid #EEEFF0;
                .node-avatar{
                  margin-top: 12px;
                  padding: 0 4px;
                  padding-right: 10px;
                  display: flex;
                  align-items: center;
                  height: 32px;
                  background: #F5F6F7;
                  border-radius: 4px;
                  width: fit-content;
                  .el-avatar{
                    margin-right: 8px;
                    width: 24px;
                    height: 24px;
                    border-radius: 4px;
                  }
                  .el-tooltip{
                    height: 20px;
                    font-family: Source Han Sans-Regular, Source Han Sans;
                    font-weight: 400;
                    font-size: 14px;
                    color: #363B4C;
                    line-height: 20px;
                  }
                }
                .timeLine-node-content{
                  overflow: hidden;
                  .timeLine-node-content-div{
                    margin-top: 8px;
                    font-family: Source Han Sans-Regular, Source Han Sans;
                    font-weight: 400;
                    font-size: 12px;
                    line-height: 17px;
                    .label{
                      color: #363B4C;
                    }
                  }
                }
              }
              .timeLine-node-card-item:nth-last-of-type(1){
                border-bottom: 0;
              }
              &.over{
                .timeLine-node-card-head{
                  padding-bottom: 0;
                  border-bottom: 0;
                }
              }
            }
          }
        }
      }
    }
  }
  .task-form-body-hideRight{
    .left-part{
      width: 100%;
    }
    .right-part{
      width: 0;
      padding: 0;
      .right-part-top, /deep/.el-timeline{
        display: none;
      }
    }
  }
  .iframe{
    border: 0;
    width: 100%;
  }
}
.auto-height-table{
  .table-body-box{
    .el-table{
      .el-table__body-wrapper{
        height: auto!important;
      }
    }
  }
}
.print-list-items{
  ul{
    list-style: none;
    margin: 0;
    padding: 0;
  }
  li{
    height: 28px;
    line-height: 28px;
    padding: 6px 24px;
    cursor: pointer;
    transition: 0.3s;
    &:hover{
      transition: 0.3s;
      background-color: #eff2f7;
    }
    i{
      margin-right: 10px;
    }
  }
}
</style>
