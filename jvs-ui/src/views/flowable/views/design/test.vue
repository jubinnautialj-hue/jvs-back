<template>
  <div class="process-design-box process-test-box">
    <design-header :infoData="testData" :currentTab="currentTab" :tabList="tabList" type="flow">
      <template slot="rightButton">
        <jvs-button size="mini" type="primary" @click="reTestClick">重新发起测试</jvs-button>
        <jvs-button size="mini" @click="backClick">返回</jvs-button>
      </template>
    </design-header>
    <div class="process-box">
      <div class="flow-design justview-flow-design" :style="'transform: scale('+ 100 / 100 +');'">
        <process-tree ref="process-tree" :process="flowDesign" @selectedNode="selectNode"/>
      </div>
      <div v-show="historyList && historyList.length > 0" class="process-right">
        <el-timeline>
          <el-timeline-item v-for="(item, index) in historyList" :key="item.nodeId+'_'+index" :timestamp="item.time" placement="top">
            <div class="">
              <p style="position:relative;display: flex;align-items: center;justify-content: space-between">
                <el-tooltip class="item" effect="dark" :content="item.nodeName" placement="top-start">
                  <span style="max-width: 40%;overflow:hidden;white-space: nowrap;text-overflow: ellipsis;font-weight: 600;">{{item.nodeName}}</span>
                </el-tooltip>
                <el-tag size="mini" type="info" effect="dark" color="#ff943e" v-if="item.mode && item.nodeType !== 'AUTOMATION' && getModeLabel(item.mode)" style="margin-left:10px;">{{ getModeLabel(item.mode) }}</el-tag>
                <el-tag size="mini" type="info" effect="dark" v-if="item.durationTime" style="margin-left:10px;">{{ item.durationTime }}</el-tag>
                <jvs-button v-if="item.dataVersion && item.nodeName !== '结束' && !item.terminated && item.formId && item.nodeType !== 'AUTOMATION'" size="mini" type="text" @click="viewChange(item)">提交记录</jvs-button>
              </p>
              <div style="margin-bottom: 24px;" v-for="(it, key) in item.approveResultDtos" :key="key">
                <p v-if="item.approveResultDtos && item.approveResultDtos.length > 0" style="position:relative;">
                  <span>{{index > 0 ? '审批人' : '发起人'}}：</span>
                  <el-tooltip class="item" effect="dark" :content="it.userName" placement="top-start">
                    <span style="max-width: 40%;overflow:hidden;position: absolute;white-space: nowrap;text-overflow: ellipsis;top: 0;right: 16px;">{{it.userName}}</span>
                  </el-tooltip>
                </p>
                <p v-if="item.approveResultDtos && item.approveResultDtos.length > 0 && index > 0" style="position:relative;">
                  <span>审批状态：</span>
                  <span style="position: absolute;top: 0;right: 16px;">{{getTypeEnumLabel(it)}}</span>
                </p>
                <p v-if="item.approveResultDtos && item.approveResultDtos.length > 0 && it.opinion && it.opinion.content" style="position:relative;">
                  <span>审批意见：</span>
                  <el-tooltip class="item" effect="dark" :content="it.opinion.content" placement="top-start">
                    <span style="max-width: 70px;overflow:hidden;position: absolute;white-space: nowrap;text-overflow: ellipsis;top: 0;right: 16px;">{{it.opinion.content}}</span>
                  </el-tooltip>
                </p>
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
      <div class="flow-test-tips">
        <span>点击发起人节点，发起流程测试</span>
        <i class="el-icon-thumb"></i>
      </div>
    </div>
    <!-- 变更信息 -->
    <changeInfo v-if="taskInfo" ref="changeInfo" :jvsAppId="taskInfo.jvsAppId"></changeInfo>
    <!-- 表单 -->
    <el-dialog
      :title="title"
      v-if="dialogVisible"
      append-to-body
      class="process-test-form-dialog"
      :visible.sync="dialogVisible"
      :before-close="handleClose">
      <iframe
        :src="formUrl"
        class="apply-form-iframe"
        scrolling="auto"
        v-if="isOut"
      >
      </iframe>
      <showForm v-else :isTest="true" :taskNode="itemData" :rowData="rowData" :formUrl="formUrl" :btn="btn" :flowDesign="flowDesign" :defaultFormData="defaultFormData" :approverList="approverList" :loading="loading" :extendInfo="infoData" @submit="submitHandle" @close="closeHandle"/>
    </el-dialog>
    <!-- 选择人员 -->
    <userSelector
      ref="processUserSelector"
      :isRadio="true"
      :selectable="false"
      :autoClose="true"
      :currentActiveName="currentActiveName"
      :userEnable="true"
      :deptEnable="false"
      :roleEnable="false"
      :deptable="false"
      @submit="chooseHandle">
    </userSelector>
  </div>
</template>
<script>
import DesignHeader from "@/components/page-header/DesignHeader"
import ProcessTree from './process/ProcessTree.vue'
import { queryDetailHistoryInfo, testStart, queryEchoForm, testExecute, reTestStart } from '../../api/flowable'
import showForm from '../componet/info'
import changeInfo from '../componet/changeInfo.vue'
import userSelector from '@/components/basic-assembly/userSelector'
export default {
  props: {
    testData: {
      type: Object
    },
    infoData: {
      type: Object
    }
  },
  components: { ProcessTree, DesignHeader, showForm, changeInfo, userSelector },
  data() {
    return {
      currentTab: 'pageSetting',
      tabList: [
        { name: 'pageSetting', label: '流程测试', icon: 'el-icon-data-line' },
      ],
      historyList: [],
      flowDesign: null, // 流程图节点信息
      title: '',
      formUrl: '',
      dialogVisible: false,
      isOut: false,
      approverList: [],
      itemData: null,
      taskId: '',
      dataId: '',
      taskInfo: null,
      btn: [],
      defaultFormData: null,
      resetBool: false,
      loading: false,
      currentActiveName: 'user',
      rowData: null
    }
  },
  created () {
    this.init()
    this.resetBool = false
  },
  methods: {
    init () {
      let designInfo = JSON.parse(this.testData.designBody)
      designInfo.currentNode = true
      this.flowDesign = designInfo
      this.historyList = []
    },
    // 点击节点
    selectNode (node) {
      if(node.currentNode && ['ROOT', 'SP'].indexOf(node.type) > -1) {
        this.itemData = node
        // 发起人节点
        if(node.type == 'ROOT') {
          this.applyHandle({
            name: node.name,
            formId: (node.nodeForm && node.nodeForm.formId) ? node.nodeForm.formId : '',
            id: this.testData.id,
            dataModelId: this.testData.dataModelId,
            manualNodes: this.testData.manualNodes,
            jvsAppId: this.testData.jvsAppId
          })
        }else{
          this.dealHandle(this.taskInfo)
        }
      }
    },
    // 发起流程
    applyHandle (it) {
      this.title = it.name
      this.approverList = []
      // 无发起人表单时可以选人
      this.formUrl = `/page-design-ui/#/form/info?dataModelId=${it.dataModelId}&flowId=${it.id}&jvsAppId=${it.jvsAppId}`
      if(it.formId) {
        this.formUrl += `&id=${it.formId}`
      }
      this.isOut = false
      if(it.manualNodes && it.manualNodes.length > 0) {
        for(let i in it.manualNodes) {
          // 发起人自选
          if((it.manualNodes[i].props && it.manualNodes[i].props.type == "SELF_SELECT" && (!it.manualNodes[i].props.targetObj.personnels || it.manualNodes[i].props.targetObj.personnels.length == 0))
            || (it.manualNodes[i].props.type == "ASSIGN_USER" && this.infoData.enableDynamicApprover)) {
            this.approverList.push({
              nodeId: it.manualNodes[i].id,
              nodeName: it.manualNodes[i].name,
              approvers: it.manualNodes[i].props.targetObj.personnels || [],
              props: it.manualNodes[i].props
            })
          }
        }
      }
      this.btn = []
      this.dialogVisible = true
    },
    // 办理流程
    async dealHandle (taskDataInfo) {
      this.approverList = []
      this.title = this.itemData.name
      this.formUrl = `/page-design-ui/#/form/info?dataModelId=${this.testData.dataModelId}&formType=flowable&taskId=${this.taskId}&jvsAppId=${taskDataInfo.jvsAppId}`
      if(taskDataInfo.formId) {
        this.formUrl += `&id=${taskDataInfo.formId}`
        await this.getEchoFormData(this.dataId)
      }
      if(taskDataInfo.nodes && taskDataInfo.nodes.length > 0) {
        taskDataInfo.nodes.filter(nit => {
          if(nit.nodeId == this.itemData.id) {
            if(nit.formId) {
              this.formUrl += `&id=${nit.formId}`
            }
            this.formUrl += `&nodeId=${this.itemData.id}`
          }
        })
        await this.getEchoFormData(this.dataId)
      }
      if(this.dataId) {
        this.formUrl += `&dataId=${this.dataId}`
      }
      if(this.testData.id) {
        this.formUrl += `&flowId=${this.testData.id}`
      }
      if(taskDataInfo.nodeId) {
        this.formUrl += `&nodeId=${taskDataInfo.nodeId}`
      }
      this.isOut = false
      this.$set(this.rowData, 'id', this.taskId)
      this.$set(this.rowData, 'nodeId', this.itemData.id)
      this.dialogVisible = true
    },
    // 表单提交
    submitHandle (data) {
      this.loading = true
      // 启动
      if(this.itemData.type == 'ROOT') {
        let temp = {
          id: data.id,
          data: data.data,
          userId: data.data.processApplyUserId
        }
        if(data.approvers) {
          temp.approvers = data.approvers
        }
        if(this.resetBool) {
          reTestStart(temp).then(res => {
            if(res.data && res.data.code == 0) {
              this.$notify({
                title: '提示',
                message: '重新启动成功',
                position: 'bottom-right',
                type: 'success'
              });
              this.handleClose()
              if(res.data.data) {
                this.taskId = res.data.data.taskId
                this.dataId = res.data.data.dataId
                this.rowData = {flowManualNodes: res.data.data.flowManualNodes}
                this.queryDetailHistoryInfoHandle(this.taskId)
                this.loading = false
              }
            }else{
              this.loading = false
            }
          }).catch(e => {
            this.loading = false
          })
        }else{
          testStart(temp).then(res => {
            if(res.data && res.data.code == 0) {
              this.$notify({
                title: '提示',
                message: '启动成功',
                position: 'bottom-right',
                type: 'success'
              });
              this.handleClose()
              if(res.data.data) {
                this.taskId = res.data.data.taskId
                this.dataId = res.data.data.dataId
                this.rowData = {flowManualNodes: res.data.data.flowManualNodes}
                this.queryDetailHistoryInfoHandle(this.taskId)
                this.loading = false
              }
            }else{
              this.loading = false
            }
          }).catch(e => {
            this.loading = false
          })
        }
      }
      if(this.itemData.type == 'SP') {
        let msg = data.buttonName
        let obj = data.data
        let temp = {}
        for(let i in obj.data) {
          if(i == 'processApplyUserId') {
            obj.userId = obj.data[i]
          }else{
            temp[i] = obj.data[i]
          }
        }
        obj.data = temp
        obj.nodeId = this.itemData.id
        testExecute(obj).then(res => {
          if(res.data && res.data.code == 0) {
            this.rowData = {flowManualNodes: res.data.data.flowManualNodes}
            // this.$message.success(msg+'成功')
            this.$notify({
              title: '提示',
              message: msg+'成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.queryDetailHistoryInfoHandle(this.taskId)
            this.handleClose()
            this.loading = false
          }else{
            this.loading = false
            // this.$message.error(res.data.msg)
            this.$notify({
              title: '提示',
              message: res.data.msg,
              position: 'bottom-right',
              type: 'error'
            });
          }
        }).catch(e => {
          this.loading = false
        })
      }
    },
    // 流程进度
    queryDetailHistoryInfoHandle (taskId) {
      queryDetailHistoryInfo(taskId).then(res => {
        if(res.data && res.data.code == 0) {
          this.taskInfo = res.data.data
          this.historyList = res.data.data.flowTaskProgress.filter(item => {
            return item.nodeType !== 'CS'
          })
          this.flowDesign = res.data.data.flowDesign
          this.getBtn(this.flowDesign)
        }
      })
    },
    // 获取按钮
    getBtn(obj) {
      if (this.taskInfo && obj.id === (this.taskInfo.nodeId ? this.taskInfo.nodeId : ((this.taskInfo.nodes && this.taskInfo.nodes.length > 0) ? this.taskInfo.nodes[0].nodeId: ''))) {
        if (obj.id === '10000') {
          this.btn.push({ name: '提交', loading: false},)
        } else {
          this.btn = obj.props.btn.filter(item => {
            return item.enable
          })
          let isNodeBtn = true
          let buttons = []
          if(this.taskInfo.nodes && this.taskInfo.nodes.length > 0) {
            this.taskInfo.nodes.filter(nit => {
              if(nit.nodeId == obj.id) {
                if(nit.approvalType == 2 && obj.props.appendApproval) {
                  isNodeBtn = false
                  this.btn = obj.props.appendApproval.btn.filter(item => {
                    return item.enable
                  })
                }
                if(nit.buttons && nit.buttons.length > 0) {
                  buttons = nit.buttons
                }
              }
            })
          }
          if(isNodeBtn) {
            this.btn = obj.props.btn.filter(item => {
              return item.enable
            })
          }
          if(buttons && buttons.length > 0) {
            this.btn.filter(bit => {
              for(let t in buttons) {
                if(bit.operation == buttons[t].operation && buttons[t].displayName) {
                  bit.name = buttons[t].displayName
                }
              }
            })
          }
        }
        return
      }
      if (obj.node) {
        this.getBtn(obj.node)
      }
      if (obj.conditions && obj.conditions.length > 0) {
        obj.conditions.forEach(item => {
          this.getBtn(item.node || {})
        })
      }
      if(obj.parallels && obj.parallels.length > 0) {
        obj.parallels.forEach(item => {
          this.getBtn(item.node || {})
        })
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
        default:
          label = '处理中';break;
      }
      if(row.transfer) {
        label = `转交(${row.transfer.proxyUserName})`
      }
      return label
    },
    viewChange (item) {
      let temp = JSON.parse(JSON.stringify(item))
      this.$set(temp, 'dataId', this.dataId)
      this.$set(temp, 'dataModelId', this.testData.dataModelId)
      this.$refs.changeInfo.openDialog(temp)
    },
    // 表单回显
    async getEchoFormData (dataId) {
      this.defaultFormData = null
      if(dataId) {
        await queryEchoForm(this.testData.jvsAppId, this.testData.dataModelId, dataId, this.taskInfo.formId).then(res => {
          if(res.data.code == 0) {
            this.defaultFormData = res.data.data
          }
        })
      }
    },
    // 关闭表单
    handleClose () {
      this.dialogVisible = false
      this.title = ""
      this.itemData= null
    },
    closeHandle (bool) {
      if(bool) {
        this.handleClose()
      }
    },
    reTestClick () {
      this.init()
      this.resetBool = true
      let node = this.flowDesign
      this.itemData = node
      this.btn = []
      this.defaultFormData = null
      this.applyHandle({
        name: node.name,
        formId: (node.nodeForm && node.nodeForm.formId) ? node.nodeForm.formId : '',
        id: this.testData.id,
        dataModelId: this.testData.dataModelId,
        manualNodes: this.testData.manualNodes,
        jvsAppId: this.testData.jvsAppId
      })
    },
    backClick () {
      this.$emit('close', true)
    },
    chooseHandle (select) {
      if(select && select.length > 0) {
        let temp = {
          id: this.testData.id,
          userId: select[0].id
        }
        if(this.itemData.type == 'ROOT'){
          testStart(temp).then(res => {
            if(res.data && res.data.code == 0) {
              this.$notify({
                title: '提示',
                message: '启动成功',
                position: 'bottom-right',
                type: 'success'
              });
              if(res.data.data) {
                this.taskId = res.data.data.taskId
                this.dataId = res.data.data.dataId
                this.queryDetailHistoryInfoHandle(this.taskId)
              }
            }
          })
        }
      }
    }
  }
}
</script>
<style scoped lang="scss">
.process-box{
  height: calc(100vh - 56px);
  display: flex;
  background: #F5F6F7;
  position: relative;
  overflow: auto;
  .justview-flow-design{
    padding: 10px 0;
    flex: 1;
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
  .process-right{
    padding: 0 20px;
    width: 320px;
    height: 100%;
    overflow-y: auto;
    background: #fff;
    padding-top: 10px;
    .el-tag--dark.el-tag--info{
      border: none;
    }
    .right-title{
      font-weight: bold;
      margin-bottom: 30px;
    }
    .el-timeline{
      padding: 0;
    }
  }
  .flow-test-tips{
    position: absolute;
    top: 10px;
    left: 10px;
    font-size: 14px;
    span{
      color: #909399;
    }
    i{
      margin-left: 5px;
      transform: rotate(90deg);
    }
  }
}
/deep/.process-test-form-dialog{
  .el-dialog__body{
    padding: 10px 0!important;
    .form-show-info{
      .jvs-form{
        .el-col{
          padding: 0 10px;
          .el-form-item.form-btn-bar{
            .el-form-item__content {
              text-align: center;
            }
          }
        }
      }
    }
  }
}
</style>
