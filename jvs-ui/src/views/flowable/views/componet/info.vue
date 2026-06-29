<template>
  <!-- 挂载表单 -->
  <div class="form-show-info">
    <jvs-form
      v-if="data.viewJson"
      ref="infoForm"
      :option="formOption"
      :formData="formData"
      :designId="flowableInfo.id"
      :dataModelId="data.dataModelId"
      :jvsAppId="flowableInfo.jvsAppId"
      :execsList="data.viewJson.execs"
      :associationSettingsFields="data.associationSettingsFields || []"
      @submit="formSubmit">
      <!-- 自定义按钮 -->
      <template slot="formButton">
        <jvs-button class="form-footer-btn" size="mini" v-for="(item, index) in btn" :loading="item.loading" :disabled="oprationDisable" :key="item.name+'slotbtn'+index" @click="slotbtnClickHandle(item, index)">{{item.name}}</jvs-button>
      </template>
      <!-- 发起人自选  动态选人 -->
      <template slot="approversForm">
        <div>
          <div v-if="approverList && approverList.length > 0">
            <div v-for="item in approverList" :key="'approver-'+item.nodeId">
              <p><span style="color:#f56c6c;margin-right:4px;">*</span> {{item.nodeName}}</p>
              <el-tag
                type="primary"
                v-for="(user, index) in item.approvers"
                size="mini"
                style="margin: 5px 10px 5px 0"
                @close="item.approvers.splice(index, 1)"
                closable
                :key="'choose-user'+index"
              >{{ user.name }}
              </el-tag>
              <el-button type="primary" size="mini" icon="el-icon-plus" style="margin: 0px" round @click="chooseUser(item)">选择人员</el-button>
            </div>
          </div>
          <div v-if="flowableInfo.canDynamicAddNode == 'true'" style="margin-top: 10px;">
            <div v-if="dynamicAddNode">
              <p style="margin: 0;margin-bottom: 10px;display: flex;align-items: center;">
                <span style="color:#f56c6c;margin-right:4px;">*</span>
                <el-input v-model="dynamicAddNode.name" size="mini" style="width: 180px;"></el-input>
                <el-select v-model="dynamicAddNode.props.type" placeholder="请选择" size="mini" style="margin-left: 10px;" @change="dynamicAddNodeTypeChange('dynamicAddNode')">
                  <el-option
                    v-for="ait in approvalTypeList"
                    :key="ait.text"
                    :label="ait.text"
                    :value="ait.label">
                  </el-option>
                </el-select>
                <el-select v-model="dynamicAddNode.props.mode" v-if="[enumConst.approvalType.ROLE, enumConst.approvalType.JOB].indexOf(dynamicAddNode.props.type) > -1 || (dynamicAddNode.props.type == enumConst.approvalType.ASSIGN_USER && dynamicAddNode.props.targetObj.personnels && dynamicAddNode.props.targetObj.personnels.length > 1)" placeholder="请选择" size="mini" style="margin-left: 10px;">
                  <el-option
                    v-for="mit in modeTypeList"
                    :key="mit.value"
                    :label="mit.label"
                    :value="mit.value">
                  </el-option>
                </el-select>
              </p>
              <el-tag
                type="primary"
                v-for="(user, inx) in dynamicAddNode.props.targetObj.personnels"
                size="mini"
                style="margin: 5px 10px 5px 0"
                @close="dynamicAddNodeDelUser(inx, 'dynamicAddNode')"
                closable
                :key="'choose-user'+inx"
              >{{ user.name }}
              </el-tag>
              <el-button v-if="[enumConst.approvalType.ASSIGN_USER, enumConst.approvalType.ROLE, enumConst.approvalType.JOB].indexOf(dynamicAddNode.props.type) > -1" type="primary" size="mini" icon="el-icon-plus" style="margin: 0px" round @click="chooseNextUser(null, dynamicAddNode, 'dynamicAddNode')">选择人员</el-button>
            </div>
            <el-button v-else size="mini" type="primary" @click="addFlowRowHandle('dynamicAddNode')">新增节点</el-button>
          </div>
        </div>
      </template>
    </jvs-form>
    <div v-else class="empty-img">
      <img src="@/const/img/emptyImage.png" alt=""/>
    </div>
    <el-dialog
      :title="opinionTitle"
      :visible.sync="dialogVisible"
      append-to-body
      :before-close="handleClose">
      <div v-if="dialogVisible">
        <jvs-form v-if="dialogType === 'TRANSFER'" :option="transferOption" :formData="transferForm" @submit="transSubmit" @cancalClick="handleClose"></jvs-form>
        <jvs-form v-if="dialogType === 'APPEND'" :option="appendOption" :formData="appendForm" @submit="appendSubmit" @cancalClick="handleClose"></jvs-form>
      </div>
    </el-dialog>
    <el-dialog
      :title="opinionTitle"
      :visible.sync="opinionDialogVisible"
      append-to-body
      width="50%"
      :before-close="handleBackClose">
      <div v-if="opinionDialogVisible">
        <el-form ref="ruleForm" :rules="opinionRules" :model="opinionForm" label-width="80px" label-position="top">
          <el-form-item v-if="opinionTitle === '退回' && flowManualNodes && flowManualNodes.length > 0" label="退回节点" prop="backNodeId">
            <el-radio-group v-model="opinionForm.backNodeId">
              <el-radio v-for="(item, key) in flowManualNodes" :key="key" :label="item.id">{{ item.name }}</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="currentRow.operation == 'PASS' && ((nextApproverList && nextApproverList.length > 0) || canDynamicAddNode)" label="下一节点审批人" prop="approvers">
            <div v-if="nextApproverList && nextApproverList.length > 0">
              <div v-for="(item, index) in nextApproverList" :key="'approver-'+item.nodeId">
                <p style="margin: 0;"><span v-if="extendInfo && extendInfo.enableDynamicApprover" style="color:#f56c6c;margin-right:4px;">*</span> {{nextNodeList[index].name}}</p>
                <el-tag
                  type="primary"
                  v-for="(user, inx) in item.approvers"
                  size="mini"
                  style="margin: 5px 10px 5px 0"
                  @close="item.approvers.splice(inx, 1)"
                  :closable="item.canDynamicApprover"
                  :key="'choose-user'+inx"
                >{{ user.name }}
                </el-tag>
                <el-button v-if="item.canDynamicApprover && [enumConst.approvalType.ASSIGN_USER, enumConst.approvalType.SELF_SELECT, enumConst.approvalType.ROLE, enumConst.approvalType.JOB].indexOf(nextNodeList[index].props.type) > -1" type="primary" size="mini" icon="el-icon-plus" style="margin: 0px" round @click="chooseNextUser(item, nextNodeList[index])">选择人员</el-button>
              </div>
            </div>
            <div v-if="canDynamicAddNode" style="margin-top: 10px;">
              <div v-if="dynamicNode">
                <p style="margin: 0;display: flex;align-items: center;">
                  <span v-if="extendInfo && extendInfo.enableDynamicApprover" style="color:#f56c6c;margin-right:4px;">*</span>
                  <el-input v-model="dynamicNode.name" size="mini" style="width: 180px;"></el-input>
                  <el-select v-model="dynamicNode.props.type" placeholder="请选择" size="mini" style="margin-left: 10px;" @change="dynamicAddNodeTypeChange('dynamicNode')">
                    <el-option
                      v-for="ait in approvalTypeList"
                      :key="ait.text"
                      :label="ait.text"
                      :value="ait.label">
                    </el-option>
                  </el-select>
                  <el-select v-model="dynamicNode.props.mode" v-if="[enumConst.approvalType.ROLE, enumConst.approvalType.JOB].indexOf(dynamicNode.props.type) > -1 || (dynamicNode.props.type == enumConst.approvalType.ASSIGN_USER && dynamicNode.props.targetObj.personnels && dynamicNode.props.targetObj.personnels.length > 1)" placeholder="请选择" size="mini" style="margin-left: 10px;">
                    <el-option
                      v-for="mit in modeTypeList"
                      :key="mit.value"
                      :label="mit.label"
                      :value="mit.value">
                    </el-option>
                  </el-select>
                </p>
                <el-tag
                  type="primary"
                  v-for="(user, inx) in dynamicNode.props.targetObj.personnels"
                  size="mini"
                  style="margin: 5px 10px 5px 0"
                  @close="dynamicAddNodeDelUser(inx, 'dynamicNode')"
                  :closable="extendInfo && extendInfo.enableDynamicApprover"
                  :key="'choose-user'+inx"
                >{{ user.name }}
                </el-tag>
                <el-button v-if="[enumConst.approvalType.ASSIGN_USER, enumConst.approvalType.ROLE, enumConst.approvalType.JOB].indexOf(dynamicNode.props.type) > -1" type="primary" size="mini" icon="el-icon-plus" style="margin: 0px" round @click="chooseNextUser(null, dynamicNode, 'dynamicNode')">选择人员</el-button>
              </div>
              <el-button v-else size="mini" type="primary" @click="addFlowRowHandle">新增节点</el-button>
            </div>
          </el-form-item>
          <el-form-item :label="opinionTitle + '意见'" :prop="opinionTitle === '退回' ? 'opinion' : ''">
            <el-popover
              v-if="replyList && replyList.length > 0"
              placement="bottom-start"
              trigger="click">
              <div class="reply-list">
                <div v-for="rit in replyList" :key="rit.id" class="reply-list-item" @click="setOpinion(rit)">{{rit.content}}</div>
              </div>
              <el-input
                slot="reference"
                type="textarea"
                :rows="3"
                placeholder="请输入内容"
                v-model="opinionForm.opinion">
              </el-input>
            </el-popover>
            <el-input
              v-else
              type="textarea"
              :rows="3"
              placeholder="请输入内容"
              v-model="opinionForm.opinion">
            </el-input>
          </el-form-item>
          <el-form-item v-if="enableSign && ['PASS', 'REFUSE'].indexOf(currentRow.operation) > -1" label="签名" prop="sign">
            <div class="jvs-form-item">
              <div class="jvs-signature-item">
                <div :class="{'signature-content': true, 'empty-signature': !opinionForm.sign}">
                  <Signature ref="sig" v-model="opinionForm.sign" prop="sign" @submit="signatureSubmit"></Signature>
                </div>
              </div>
            </div>
          </el-form-item>
        </el-form>
        <el-row style="display:flex;justify-content: center;align-items: center;margin-top: 20px;">
          <jvs-button size="mini" type="primary" :loading="backSubmitLoading" @click="backSubmit">确认</jvs-button>
          <jvs-button size="mini" @click="handleBackClose">取消</jvs-button>
        </el-row>
      </div>
    </el-dialog>
    <!-- 选择人员 -->
    <userSelector
      ref="approversUserSelector"
      :selectable="true"
      :autoClose="true"
      :userEnable="true"
      @submit="approverSubmit">
    </userSelector>
    <!-- 选择下级节点人员 -->
    <userSelector
      v-if="currentNode && currentNode.props && currentNode.props.type"
      ref="nextNodeUserSelector"
      :selectable="true"
      :autoClose="true"
      :currentActiveName="[enumConst.approvalType.ASSIGN_USER, enumConst.approvalType.SELF_SELECT].indexOf(currentNode.props.type) > -1 ? 'user': 
        ([enumConst.approvalType.ROLE].indexOf(currentNode.props.type) > -1 ? 'role' : 
        ([enumConst.approvalType.JOB].indexOf(currentNode.props.type) > -1 ? 'job': 'user'))"
      :userEnable="[enumConst.approvalType.ASSIGN_USER, enumConst.approvalType.SELF_SELECT].indexOf(currentNode.props.type) > -1"
      :roleEnable="[enumConst.approvalType.ROLE].indexOf(currentNode.props.type) > -1"
      :jobEnable="[enumConst.approvalType.JOB].indexOf(currentNode.props.type) > -1"
      @submit="nextNodeSubmit">
    </userSelector>
    <!-- 测试loading -->
    <div v-if="loading" class="loading-back"></div>
  </div>
</template>
<script>
import userSelector from '@/components/basic-assembly/userSelector'
import {
  getFlowableForm,
  startProcess,
  completeProcess,
  zhipaiProcess,
  weipaiProcess,
  saveProcess,
  restartProcess,
} from "./api";
import { getNextNodeList, getDynamicNode } from './api'
import userForm from '@/components/basic-assembly/userForm'
import {sendMyRequire, getKeyValue} from '@/api/newDesign'
import {queryFlowManualNodes} from '../../api/flowable'
import enumConst from "../design/common/enumConst";
import { getFlowReply } from '@/components/template/api'
import {getUserInfo} from "@/api/admin/user";
import {getDefaultData} from "@/views/page/util/common";
import Signature from '@/components/basic-assembly/signature'
import { customUpload } from '@/api/index'
export default {
  name: 'form-show-info',
  components: {userForm, userSelector, Signature},
  props: {
    rowData: {
      type: Object
    },
    formUrl: {
      type: String
    },
    formId: {
      type: String
    },
    btnHide: {
      type: Boolean,
      default: false
    },
    isRestart: {
      type: Boolean,
      default: false
    },
    defaultFormData: {
      type: Object
    },
    flowDesign: {
      type: Object
    },
    onlyView: {
      type: Boolean,
      default: false
    },
    approverList: {
      type: Array
    },
    btn: {
      type: Array
    },
    isTest: {
      type: Boolean,
      default: false
    },
    taskNode: {
      type: Object
    },
    loading: {
      type: Boolean
    },
    extendInfo: {
      type: Object
    },
    enableSign: {
      type: Boolean
    }
  },
  data(){
    return {
      opinionRules: {
        backNodeId: [
          { required: true, message: '请选择退回节点', trigger: 'change' }
        ],
        opinion: [
          { required: true, message: '请输入退回意见', trigger: 'change' }
        ],
        sign: [
          { required: true, message: '请手写签名', trigger: 'change' }
        ]
      },
      opinionForm: {},
      backSubmitLoading: false,
      currentRow: null, // 当前点击按钮
      opinionTitle: '审批',
      // btn: [],
      name: '',
      data: {
        name: '',
        formType: '',
        id: '',
        viewJson: null
      },
      // 表单
      formType: '',
      formData: {},
      selectFormItems: [], // 表单里的下拉选择项
      basicOption: {
        disabled: true,
        btnHide: true,
        column: []
      },
      formOption: {
        submitBtn: false,
        emptyBtn: false,
        cancal: false,
        column: []
      },
      flowableInfo: {},
      isFlowable: false,
      processName: '',
      nodeDicData: [], // 节点列表信息
      dialogVisible: false,
      opinionDialogVisible: false,
      assUserForm: {
        userId: ''
      },
      showAssignBool: false,
      count: 0,
      flowData: {},
      buttonList: null,
      btnSetList: [],
      formRemark: '', // 自带的审批意见
      dataTemp: null,
      taskNormalForm: false, // 是否为使用发起人表单的审核节点表单
      currentNode: {}, // 当前用户节点
      oprationDisable: false,
      transferOption: {
        emptyBtn: false,
        submitLoading: false,
        column: [
          {
            label: '代理用户',
            prop: 'proxyUserId',
            type: 'user',
            props: {
              label: 'proxyUserName',
              value: 'proxyUserId'
            }
          },
          {
            label: '说明',
            prop: 'directions',
            type: 'textarea'
          },
        ]
      },
      dialogType: '',
      transferForm: {},
      appendOption: {
        emptyBtn: false,
        submitLoading: false,
        column: [
          {
            label: '加签方式',
            prop: 'point',
            type: 'select',
            dicUrl: ''
            // dicData: [
            //   {label: '前加签', value: 'BEFORE'},
            //   {label: '后加签', value: 'AFTER'}
            // ]
          },
          {
            label: '加签人员',
            prop: 'personnels',
            type: 'user',
            multiple: true,
            infoable: true,
            props: {
              label: 'appendUserName',
              value: 'appendUserId'
            }
          },
          {
            label: '加签理由',
            prop: 'description',
            type: 'textarea'
          },
        ]
      },
      appendForm: {},
      flowManualNodes: [], // 可退回的节点
      enumConst: enumConst,
      nextNodeList: [],
      nextApproverList: [],
      currentOprate: '',
      dynamicAddNode: null,
      dynamicNode: null,
      canDynamicAddNode: false,
      modeTypeList: [
        // {label: ' ', value: 'DEFAULT'},
        {label: '会签', value: enumConst.approvalMode.AND},
        {label: '或签（有一人同意即可）', value: enumConst.approvalMode.OR},
        {label: '按选择顺序依次审批', value: enumConst.approvalMode.NEXT}
      ],
      approvalTypeList: [
        { label: enumConst.approvalType.ASSIGN_USER, text: "指定人员" },
        { label: enumConst.approvalType.LEADER_TOP, text: "连续多级主管", disabled: false },
        { label: enumConst.approvalType.LEADER, text: "直属主管", disabled: false },
        { label: enumConst.approvalType.ROLE, text: "角色" },
        { label: enumConst.approvalType.SELF, text: "发起人自己" },
        { label: enumConst.approvalType.JOB, text: "岗位" },
      ],
      replyList: [],
      userInfo: {}
    }
  },
  created () {
    // 通过url获取参数
    let paramList = []
    if(this.formUrl.split('?').length > 1) {
      paramList = this.formUrl.split('?')[1].split('&')
      if(paramList.length > 0) {
        for(let i in paramList) {
          let tp = paramList[i].split('=')
          this.flowableInfo[tp[0]] = tp[1]
        }
        this.initPage()
        this.getFlowReplyList()
      }
    }
    if(this.$store.getters && this.$store.getters.userInfo) {
      this.userInfo = JSON.parse(JSON.stringify(this.$store.getters.userInfo))
    }else{
      this.getUserInfo()
    }
  },
  methods: {
    getUserInfo() {
      getUserInfo().then(res => {
        if(res.data.code == 0) {
          this.userInfo = res.data.data
        }
      })
    },
    // 关闭退回节点选择弹框
    handleBackClose() {
      this.opinionDialogVisible = false
      this.dynamicNode = null
      this.opinionForm = {}
    },
    // 退回节点选择弹框 确认
    backSubmit () {
      this.$refs['ruleForm'].validate((valid) => {
        if (valid) {
          let obj = {
            id: this.flowableInfo.taskId,
            data: this.formData,
            nodeOperationType: this.currentRow.operation,
            sign: false,
            nodeId: this.flowableInfo.nodeId,
            opinion: { content: this.opinionForm.opinion }
          }
          if(this.opinionForm.sign) {
            this.$set(obj.opinion, 'sign', this.opinionForm.sign)
          }
          if (this.opinionTitle === '退回') {
            obj.backNodeId = this.opinionForm.backNodeId
          }else{
            if(this.nextNodeList && this.nextNodeList.length > 0) {
              obj.approvers = JSON.parse(JSON.stringify(this.nextApproverList))
            }
          }
          if(this.canDynamicAddNode && this.dynamicNode) {
            if([enumConst.approvalType.ASSIGN_USER, enumConst.approvalType.ROLE, enumConst.approvalType.JOB].indexOf(this.dynamicNode.props.type) > -1) {
              if(!this.dynamicNode.props.targetObj.personnels || this.dynamicNode.props.targetObj.personnels.length < 1) {
                this.$notify({
                  title: '提示',
                  message: `请选择${this.dynamicNode.name}的人员`,
                  position: 'bottom-right',
                  type: 'warning'
                });
                return false
              }
            }
            obj.node = JSON.parse(JSON.stringify(this.dynamicNode))
          }
          if(this.isTest) {
            this.$emit('submit', {buttonName: this.currentRow.name, data: obj})
            this.oprationDisable = false
            this.currentRow.loading = false
            this.handleBackClose()
          }else{
            if(this.currentRow.operation == 'PASS' && this.canDynamicAddNode && !this.dynamicNode) {
              this.$confirm('当前未添加后续审批，是否继续？', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
              }).then(() => {
                this.backSubmitLoading = true
                this.oprationDisable = true
                completeProcess(obj, {designId: this.flowableInfo.id}).then(res => {
                  if(res.data.code == 0) {
                    // this.$message.success(this.currentRow.name + '成功')
                    this.$notify({
                      title: '提示',
                      message: this.currentRow.name + '成功',
                      position: 'bottom-right',
                      type: 'success'
                    });
                    this.currentRow.loading = false
                    this.oprationDisable = false
                    this.backSubmitLoading = false
                    this.opinionDialogVisible = false
                    this.$emit('close', true)
                  } else {
                    this.opinionDialogVisible = false
                    this.backSubmitLoading = false
                  }
                }).catch(e => {
                  this.opinionDialogVisible = false
                  this.backSubmitLoading = false
                  this.currentRow.loading = false
                  this.oprationDisable = false
                })
              }).catch(() => {})
            }else{
              this.backSubmitLoading = true
              this.oprationDisable = true
              completeProcess(obj, {designId: this.flowableInfo.id}).then(res => {
                if(res.data.code == 0) {
                  // this.$message.success(this.currentRow.name + '成功')
                  this.$notify({
                    title: '提示',
                    message: this.currentRow.name + '成功',
                    position: 'bottom-right',
                    type: 'success'
                  });
                  this.currentRow.loading = false
                  this.oprationDisable = false
                  this.backSubmitLoading = false
                  this.opinionDialogVisible = false
                  this.$emit('close', true)
                } else {
                  this.opinionDialogVisible = false
                  this.backSubmitLoading = false
                }
              }).catch(e => {
                this.opinionDialogVisible = false
                this.backSubmitLoading = false
                this.currentRow.loading = false
                this.oprationDisable = false
              })
            }
          }
        } else {
          console.log('error submit!!');
          return false;
        }
      });
    },
    async initPage () {
      // 判断是否属于工作流
      if(this.flowableInfo.flowId) {
        this.isFlowable = true
      }
      // 表单结构
      if(this.flowableInfo.id) {
        this.getFlowableFormHandle()
      }else{
        if(this.isTest) {
          this.data.viewJson = {
            formType: 'normalForm',
            formdata: [{
              forms: [],
              formsetting: {}
            }]
          }
          this.initForm(this.data.viewJson)
        }
      }
      this.getFlowManualNodes() // 获取回退节点
      this.getNextNodeList() // 获取下级节点集合
      this.getDynamicNodeInfo() // 获取是否可以动态添加节点
    },
    initForm (formDesign) {
      this.getSelectItem(formDesign.formdata)
      this.formType = formDesign.formType || 'flowable'
      if(this.flowableInfo.taskId && formDesign.formType == 'normalForm') {
        this.taskNormalForm = true
      }
      this.getFormColumn(formDesign.formType, formDesign)
    },
    // 表单配置
    getFormColumn (type, formDesign) {
      if(formDesign.formdata && formDesign.formdata.length > 0) {
        let forms = formDesign.formdata[0].forms
        forms.push({
          label: '',
          prop: 'approvers',
          formSlot: true,
          hideLabel: true
        })
        this.formOption = this.formatFormOption(type, forms, formDesign.formdata[0].formsetting)
        if(this.isTest) {
          this.formOption.column.push({
            label: '选择' + ((this.taskNode && this.taskNode.type == 'SP') ? this.taskNode.name : '发起人'),
            prop: 'processApplyUserId',
            type: 'user'
          })
        }
        this.formData = {}
        // 表单默认值初始化
        if(this.defaultFormData) {
          let temp = getDefaultData(JSON.parse(JSON.stringify(this.defaultFormData)), forms, this.userInfo)
          this.formOption.column.filter(item => {
            if(item.multiple && (item.type && item.type != 'input') && (typeof temp[item.prop] === 'string')) {
              temp[item.prop] = temp[item.prop].split(',')
            }
          })
          this.formData = temp
        }
        if(this.onlyView) {
          this.formOption.disabled =  true
        }
        // console.log('表单实际回显赋值数据。。。。。。')
        // console.log(this.formData)
        // console.log(this.formOption)
      }else{
        // console.log(formDesign)
      }
    },
    // 格式化表单配置项
    formatFormOption (type, forms, formsetting) {
      let btlist = []
      for(let i in formsetting.btnSetting){
        if(["submit", "empty"].indexOf(formsetting.btnSetting[i].buttonType) == -1) {
          formsetting.btnSetting[i].loading = false
          btlist.push(formsetting.btnSetting[i])
        }
      }
      if(this.taskNormalForm) {
        // 需要在节点设计！！！！！！！！！！！！！！！！！！！！！！！！！！！
        btlist = [
          {name: '通过', url: 'ok', enable: true},
          {name: '拒绝', url: 'fail', enable: true},
          // {name: '驳回', url: 'reject', enable: true},
          // {name: '保存', url: 'save', enable: true},
          // {name: '指派', url: 'zhipai', enable: true},
          // {name: '委派', url: 'weipai', enable: true}
        ]
        for(let bi in btlist) {
          btlist[bi].loading = false
        }
      }
      let temp = {
        column: JSON.parse(JSON.stringify(forms)),
        btnSetting: btlist,
        size: formsetting.formsize,
        formAlign: formsetting.labelposition,
        labelWidth: formsetting.labelwidth + 'px',
        fullscreen: formsetting.fullscreen,
        submitBtn: this.taskNormalForm ? false : formsetting.submitBtn,
        emptyBtn: this.taskNormalForm ? false : formsetting.emptyBtn,
        flag: formsetting.falg,
        submitLoading: false,
      }
      for(let c in temp.column) {
        if(temp.column[c].type == 'SWITCH') {
          temp.column[c].type = 'switch'
        }
        if(temp.column[c].type == 'select') {
          temp.column[c].collapsetags = true
        }
      }
      if(type == 'flowable'){
        temp.submitBtn = false
        temp.emptyBtn =  false
        temp.cancal = false
      }else{
        temp.disabled = false
        temp.btnHide = false
        temp.cancal = false
      }
      if(this.btnHide) {
        temp.btnHide = true
        // 不可编辑，组件只读
        if(temp.column && temp.column.length > 0) {
          for(let i in temp.column) {
            temp.column[i].disabled = true
            if(['tab'].indexOf(temp.column[i].type) > -1) {
              for(let j in temp.column[i].column) {
                if(temp.column[i].column[j] && temp.column[i].column[j].length > 0) {
                  temp.column[i].column[j].filter(tcit => {
                    tcit.disabled = true
                  })
                }
              }
            }
            if(temp.column[i].column && temp.column[i].column.length > 0) {
              for(let j in temp.column[i].column) {
                temp.column[i].column[j].disabled = true
              }
            }
            if(temp.column[i].tableColumn && temp.column[i].tableColumn.length > 0) {
              temp.column[i].addBtn = false
              temp.column[i].delBtn = false
              for(let j in temp.column[i].tableColumn) {
                temp.column[i].tableColumn[j].disabled = true
              }
            }
          }
        }
      }
      return temp
    },
    // 获取select项，表单值为数组
    getSelectItem (list) {
      let temp = []
      for(let i in list) {
        for(let j in list[i].forms) {
          if(list[i].forms[j].type == 'select') {
            temp.push(list[i].forms[j].prop)
          }
        }
      }
      this.selectFormItems = temp
    },
    // 表单提交
    formSubmit (formsdata) {
      // console.log(this.isFlowable)
      // console.log(this.formType)
      // 工作流-开始节点(发起人)表单提交
      if(this.isFlowable) {
        // 启动流程
        if(this.formType == 'normalForm') {
          // 校验自选人不为空
          let emptyTips = []
          for(let i in this.approverList) {
            if(!this.approverList[i].approvers || this.approverList[i].approvers.length == 0) {
              emptyTips.push(this.approverList[i].nodeName)
            }
          }
          if(this.flowableInfo.canDynamicAddNode == 'true' && this.dynamicAddNode) {
            if([enumConst.approvalType.ASSIGN_USER, enumConst.approvalType.ROLE, enumConst.approvalType.JOB].indexOf(this.dynamicAddNode.props.type) > -1) {
              if(!this.dynamicAddNode.props.targetObj.personnels || this.dynamicAddNode.props.targetObj.personnels.length < 1) {
                emptyTips.push(this.dynamicAddNode.name)
              }
            }
          }
          if(emptyTips.length > 0) {
            this.$notify({
              title: '提示',
              message: `请选择${emptyTips.join('、')}的人员`,
              position: 'bottom-right',
              type: 'warning'
            });
            return false
          }
          let obj = {
            data: formsdata,
            id: this.flowableInfo.flowId,
          }
          if(this.approverList && this.approverList.length > 0) {
            obj.approvers = this.approverList
          }
          if(this.flowableInfo.canDynamicAddNode == 'true' && this.dynamicAddNode) {
            obj.node = this.dynamicAddNode
          }
          if(this.isTest) {
            if(obj.data.processApplyUserId) {
              this.$emit('submit', obj)
            }else{
              // this.$message.warning(`${(this.taskNode && this.taskNode.type == 'SP') ? this.taskNode.name : '发起人'}不能为空`)
              this.$notify({
                title: '提示',
                message: `${(this.taskNode && this.taskNode.type == 'SP') ? this.taskNode.name : '发起人'}不能为空`,
                position: 'bottom-right',
                type: 'warning'
              });
            }
          }else{
            this.formOption.submitLoading = true
            if (this.isRestart) {
              restartProcess(obj, {designId: this.flowableInfo.id}).then(res => {
                if(res.data.code == 0) {
                  this.$notify({
                    title: '提示',
                    message: '流程重启成功',
                    position: 'bottom-right',
                    type: 'success'
                  });
                  this.$emit('close', true)
                  this.formOption.submitLoading = false
                }
              }).catch(e => {
                this.formOption.submitLoading = false
              })
            } else {
              startProcess(obj, this.formId).then(res => {
                if(res.data.code == 0) {
                  this.$notify({
                    title: '提示',
                    message: '流程启动成功',
                    position: 'bottom-right',
                    type: 'success'
                  });
                  this.$emit('close', true)
                  this.formOption.submitLoading = false
                }
              }).catch(e => {
                this.formOption.submitLoading = false
              })
            }
          }
        }
      }
    },
    // 自定义按钮事件
    slotbtnClickHandle (row, index) {
      let validBool = false
      if(['SAVE', 'REFUSE'].indexOf(row.operation) == -1) {
        if(['BACK', 'TRANSFER','PASS'].indexOf(row.operation) > -1){
          if(row.enableFormValidation){
            validBool = !(this.$refs.infoForm.validateForm())
          }
        }else {
          validBool = !(this.$refs.infoForm.validateForm())
        }
      }
      if(validBool) return false
      if(this.isTest) {
        if(!this.formData.processApplyUserId) {
          // this.$message.warning(`${(this.taskNode && this.taskNode.type == 'SP') ? this.taskNode.name : '发起人'}不能为空`)
          this.$notify({
            title: '提示',
            message: `${(this.taskNode && this.taskNode.type == 'SP') ? this.taskNode.name : '发起人'}不能为空`,
            position: 'bottom-right',
            type: 'warning'
          });
          return
        }
      }
      // 流程提交
      this.currentRow = row
      if(this.isFlowable) {
        if (row.operation === 'BACK') {
          this.opinionTitle = '退回'
          this.$set(this.opinionForm, 'opinion', '退回')
          if(!this.opinionForm.backNodeId && this.flowManualNodes.length == 1) {
            this.$set(this.opinionForm, 'backNodeId', this.flowManualNodes[0].id)
          }
          this.opinionDialogVisible = true
          return
        }
        if (['PASS', 'REFUSE'].indexOf(row.operation) > -1) {
          this.opinionTitle = '审批'
          // const str = row.operation === 'PASS' ? '同意' : '拒绝'
          // this.$set(this.opinionForm, 'opinion', str)
          if(this.canDynamicAddNode && this.dynamicNodeOriginData) {
            this.dynamicNode = JSON.parse(JSON.stringify(this.dynamicNodeOriginData))
          }
          this.opinionDialogVisible = true
          return
        }
        if (row.operation === 'TRANSFER' || row.name == '转交') {
          this.opinionTitle = '转交'
          this.dialogType = row.operation
          this.dialogVisible = true
          return
        }
        if (row.operation === 'APPEND' || row.name == '加签') {
          this.opinionTitle = '加签'
          // console.log(row)
          // console.log(this.rowData)
          this.appendOption.column.filter(item => {
            if(item.prop == 'point') {
              item.dicUrl = `/jvs-design/base/workflow/task/node/append_type/${this.rowData.id}/${this.rowData.nodeId}`
              item.props = {
                label: 'name',
                value: 'value'
              }
            }
          })
          this.dialogType = row.operation
          this.dialogVisible = true
          return
        }
        let obj = {
          id: this.flowableInfo.taskId,
          data: this.formData,
          nodeOperationType: row.operation,
          sign: false,
          nodeId: this.flowableInfo.nodeId
        }
        if(this.isTest) {
          this.$emit('submit', {buttonName: row.name, data: obj})
        }else{
          row.loading = true
          this.oprationDisable = true
          completeProcess(obj, {designId: this.flowableInfo.id}).then(res => {
            if(res.data.code == 0) {
              // this.$message.success(row.name + '成功')
              this.$notify({
                title: '提示',
                message: row.name + '成功',
                position: 'bottom-right',
                type: 'success'
              });
              row.loading = false
              this.oprationDisable = false
              this.$emit('close', true)
            }
          }).catch(e => {
            row.loading = false
            this.oprationDisable = false
          })
        }
        return false
        if(['ok', 'fail', 'reject', 'rollback'].indexOf(row.url) > -1) {
          let tp = ""
          switch(row.url) {
            case "ok": tp = "PASS";break;
            case "fail": tp = "REFUSE";break;
            case "reject": tp = "BACK";break;
            default : ;break;
          }
          if(tp) {
            let obj = {
              id: this.flowableInfo.taskId,
              data: this.formData,
              nodeOperationType: tp,
              sign: false,
              nodeId: this.flowableInfo.nodeId
            }
            row.loading = true
            this.oprationDisable = true
            completeProcess(obj).then(res => {
              if(res.data.code == 0) {
                // this.$message.success(row.name + '成功')
                this.$notify({
                  title: '提示',
                  message: row.name + '成功',
                  position: 'bottom-right',
                  type: 'success'
                });
                row.loading = false
                this.oprationDisable = false
                this.$emit('close', true)
              }
            }).catch(e => {
              row.loading = false
              this.oprationDisable = false
            })
          }
        }else{
          let obj = {
            id: this.flowableInfo.taskId,
            data: this.formData,
          }
          return false
          if(row.type == 'save') {
            saveProcess(this.flowableInfo.taskId, obj).then(res => {
              if(res.data.code == 0) {
                // this.$message.success('保存成功')
                this.$notify({
                  title: '提示',
                  message: '保存成功',
                  position: 'bottom-right',
                  type: 'success'
                });
                this.$emit('close', true)
              }
            })
          }else if(row.type == 'assign') {
            this.dialogVisible = true
          }else if(row.type == 'weipai') {
            weipaiProcess(this.flowableInfo.taskId, this.userId).then(res => {
              if(res.data.code == 0) {
                // this.$message.success('委派成功')
                this.$notify({
                  title: '提示',
                  message: '委派成功',
                  position: 'bottom-right',
                  type: 'success'
                });
                this.$emit('close', true)
              }
            })
          }else{
            let tp = null
            tp = JSON.parse(JSON.stringify(row))
            if(this.$store.getters.labelValue && this.$store.getters.labelValue.requestContentType) {
              tp.requestContentType = this.$store.getters.labelValue.requestContentType[tp.requestContentType]
            }
            if(tp && tp.url) {
              sendMyRequire(tp, this.formData).then(res => {
                if(res.data.code == 0) {
                  // this.$message.success(res.data.msg)
                  this.$notify({
                    title: '提示',
                    message: res.data.msg,
                    position: 'bottom-right',
                    type: 'success'
                  });
                }
              })
            }
          }
        }
      }else{
        let tp = null
        tp = JSON.parse(JSON.stringify(row))
        if(this.$store.getters.labelValue && this.$store.getters.labelValue.requestContentType) {
          tp.requestContentType = this.$store.getters.labelValue.requestContentType[tp.requestContentType]
        }
        if(tp && tp.url) {
          sendMyRequire(tp, this.formData).then(res => {
            if(res.data.code == 0) {
              // this.$message.success(res.data.msg)
              this.$notify({
                title: '提示',
                message: res.data.msg,
                position: 'bottom-right',
                type: 'success'
              });
            }
          })
        }
      }
    },
    // 获取工作流表单
    getFlowableFormHandle () {
      getFlowableForm(this.flowableInfo.jvsAppId, this.flowableInfo.id).then(res => {
        if(res.data.code == 0) {
          if(res.data.data.viewJson) {
            let view = JSON.parse(res.data.data.viewJson)
            this.data.viewJson = view
            this.data.dataModelId = res.data.data.dataModelId
            this.initForm(this.data.viewJson)
          }
        }
      })
    },
    // 关闭指派
    handleClose () {
      this.dialogVisible = false
      this.userForm = {
        userId: ''
      }
      this.transferForm = {}
      this.appendForm = {}
    },
    // 指派提交
    assignSubmit () {
      zhipaiProcess(this.flowableInfo.taskId, this.assUserForm.userId).then(res => {
        if(res.data.code == 0) {
          // this.$message.success('指派成功')
          this.$notify({
            title: '提示',
            message: '指派成功',
            position: 'bottom-right',
            type: 'success'
          });
          this.$emit('close', true)
        }
      })
    },
    // 获取所有label value 对应值
    getKeyValueHandle () {
      getKeyValue().then(res => {
        if(res.data.code == 0) {
          this.labelValue = res.data.data
          this.$store.commit('SET_LabelValue', this.labelValue)
        }
      })
    },
    // 发起人自选人
    chooseUser (item) {
      this.currentNode = JSON.parse(JSON.stringify(item))
      this.$refs.approversUserSelector.openDialog(item.approvers, (item.props.personnelScope && item.props.personnelScope.type == 'CUSTOM') ? item.props.personnelScope.personnelScopes : null)
    },
    // 发起人自选提交
    approverSubmit (list) {
      for(let i in this.approverList) {
        if(this.approverList[i].nodeId == this.currentNode.nodeId) {
          this.$set(this.approverList[i], 'approvers', list.map((s) => {
            return { id: s.id, name: s.name, type: s.type ? s.type : 'user' };
          }))
        }
      }
    },
    // 转交提交
    transSubmit () {
      let obj = {
        id: this.flowableInfo.taskId,
        data: this.formData,
        nodeOperationType: "TRANSFER",
        sign: false,
        nodeId: this.flowableInfo.nodeId,
        transfer: this.transferForm
      }
      if(this.isTest) {
        this.$emit('submit', {buttonName: this.currentRow.name, data: obj})
        this.oprationDisable = false
        this.currentRow.loading = false
        this.$emit('close', true)
        this.handleClose()
      }else{
        this.transferOption.submitLoading = true
        completeProcess(obj, {designId: this.flowableInfo.id}).then(res => {
          if(res.data.code == 0) {
            // this.$message.success("转交成功")
            this.$notify({
              title: '提示',
              message: '转交成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.transferOption.submitLoading = false
            this.currentRow.loading = false
            this.oprationDisable = false
            this.$emit('close', true)
            this.handleClose()
          }
        }).catch(e => {
          this.currentRow.loading = false
          this.oprationDisable = false
          this.transferOption.submitLoading = false
        })
      }
    },
    // 加签提交
    appendSubmit () {
      let obj = {
        id: this.flowableInfo.taskId,
        data: this.formData,
        nodeOperationType: "APPEND",
        sign: false,
        nodeId: this.flowableInfo.nodeId,
        appendApproval: this.appendForm
      }
      if(this.isTest) {
        this.$emit('submit', {buttonName: this.currentRow.name, data: obj})
        this.currentRow.loading = false
        this.oprationDisable = false
        this.$emit('close', true)
        this.handleClose()
      }else{
        this.appendOption.submitLoading = true
        completeProcess(obj, {designId: this.flowableInfo.id}).then(res => {
          if(res.data.code == 0) {
            // this.$message.success("加签成功")
            this.$notify({
              title: '提示',
              message: '加签成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.appendOption.submitLoading = false
            this.currentRow.loading = false
            this.oprationDisable = false
            this.$emit('close', true)
            this.handleClose()
          }
        }).catch(e => {
          this.currentRow.loading = false
          this.oprationDisable = false
          this.appendOption.submitLoading = false
        })
      }
    },
    // 获取退回节点
    getFlowManualNodes () {
      if(this.flowableInfo.taskId && this.flowableInfo.nodeId) {
        queryFlowManualNodes(this.flowableInfo.taskId, this.flowableInfo.nodeId).then(res => {
          if(res.data && res.data.code == 0) {
            this.flowManualNodes = res.data.data
          }
        })
      }
    },
    // 获取当前审批节点的下级审批节点集合
    getNextNodeList () {
      if(this.flowableInfo.taskId && this.flowableInfo.nodeId) {
        getNextNodeList(this.flowableInfo.taskId, this.flowableInfo.nodeId).then(res => {
          if(res.data && res.data.code == 0 && res.data.data) {
              this.nextNodeList = res.data.data
              this.nextApproverList = []
              res.data.data.filter(fit => {
                this.nextApproverList.push({
                  nodeId: fit.id,
                  approvers: fit.props.targetObj.personnels,
                  canDynamicApprover: fit.canDynamicApprover
                })
              })
            }
        })
      }
    },
    chooseNextUser (item, node,  key) {
      this.currentOprate = ''
      if(key) {
        this.currentOprate = key
      }
      this.currentNode = JSON.parse(JSON.stringify(node))
      this.$nextTick( ()=> {
        if(key == 'dynamicAddNode') {
          this.$refs.nextNodeUserSelector.openDialog(this.dynamicAddNode.props.targetObj.personnels, (node.props.personnelScope && node.props.personnelScope.type == 'CUSTOM') ? node.props.personnelScope.personnelScopes : null)
        }else if(key == 'dynamicNode'){
          this.$refs.nextNodeUserSelector.openDialog(this.dynamicNode.props.targetObj.personnels, (node.props.personnelScope && node.props.personnelScope.type == 'CUSTOM') ? node.props.personnelScope.personnelScopes : null)
        }else{
          this.$refs.nextNodeUserSelector.openDialog(item.approvers, (node.props.personnelScope && node.props.personnelScope.type == 'CUSTOM') ? node.props.personnelScope.personnelScopes : null)
        }
      })
    },
    nextNodeSubmit (list) {
      if(this.currentOprate == 'dynamicAddNode') {
        if(list && list.length > 0) {
          if(this.dynamicAddNode.props.type == enumConst.approvalType.ASSIGN_USER && list.length < 2) {
            this.$set(this.dynamicAddNode.props, 'mode', 'DEFAULT')
          }else{
            this.$set(this.dynamicAddNode.props, 'mode', 'AND')
          }
        }else{
          this.$set(this.dynamicAddNode.props, 'mode', 'DEFAULT')
        }
        this.$set(this.dynamicAddNode.props.targetObj, 'personnels', list)
      }else if(this.currentOprate == 'dynamicNode'){
        if(list && list.length > 0) {
          if(this.dynamicNode.props.type == enumConst.approvalType.ASSIGN_USER && list.length < 2) {
            this.$set(this.dynamicNode.props, 'mode', 'DEFAULT')
          }else{
            this.$set(this.dynamicNode.props, 'mode', 'AND')
          }
        }else{
          this.$set(this.dynamicNode.props, 'mode', 'DEFAULT')
        }
        this.$set(this.dynamicNode.props.targetObj, 'personnels', list)
      }else{
        for(let i in this.nextApproverList) {
          if(this.nextApproverList[i].nodeId == this.currentNode.id) {
            this.$set(this.nextApproverList[i], 'approvers', list)
          }
        }
      }
      this.currentNode = {}
      this.currentOprate = ''
      this.$forceUpdate()
    },
    getDynamicNodeInfo () {
      if(this.flowableInfo.taskId && this.flowableInfo.nodeId) {
        getDynamicNode(this.flowableInfo.taskId, this.flowableInfo.nodeId).then(res => {
          if(res.data && res.data.code == 0 && res.data.data) {
              this.canDynamicAddNode = res.data.data.canDynamicAddNode || false
              if(res.data.data.node) {
                this.dynamicNodeOriginData = JSON.parse(JSON.stringify(res.data.data.node))
                this.dynamicNode = JSON.parse(JSON.stringify(res.data.data.node))
              }
            }
        })
      }
    },
    addFlowRowHandle (key) {
      let obj = {
        name: '审批人',
        id: this.getRandomId(),
        nodeForm: {formId: "", sendUserForm: true, version: "" },
        type: enumConst.nodeType.SP
      }
      let props = JSON.parse(JSON.stringify(enumConst.getDefaultNodeProps(obj.type)))
      props.btn = JSON.parse(JSON.stringify(enumConst.nodeButtonList))
      obj.props = props
      if(key == 'dynamicAddNode'){
        this.dynamicAddNode = JSON.parse(JSON.stringify(obj))
      }else{
        this.dynamicNode = JSON.parse(JSON.stringify(obj))
      }
      this.$forceUpdate()
    },
    getRandomId(){
      return (Math.floor(Math.random() * (99999 - 10000)) + 10000).toString() + new Date().getTime().toString().substring(5, 11)
    },
    dynamicAddNodeTypeChange (key) {
      if(key == 'dynamicAddNode') {
        this.$set(this.dynamicAddNode.props.targetObj, 'personnels', [])
        if([enumConst.approvalType.ROLE, enumConst.approvalType.JOB].indexOf(this.dynamicAddNode.props.type) > -1) {
          this.$set(this.dynamicAddNode.props, 'mode', 'AND')
        }else{
          this.$set(this.dynamicAddNode.props, 'mode', 'DEFAULT')
        }
      }else{
        this.$set(this.dynamicNode.props.targetObj, 'personnels', [])
        if([enumConst.approvalType.ROLE, enumConst.approvalType.JOB].indexOf(this.dynamicNode.props.type) > -1) {
          this.$set(this.dynamicNode.props, 'mode', 'AND')
        }else{
          this.$set(this.dynamicNode.props, 'mode', 'DEFAULT')
        }
      }
    },
    dynamicAddNodeDelUser (inx, key) {
      if(key == 'dynamicAddNode') {
        this.dynamicAddNode.props.targetObj.personnels.splice(inx, 1)
        if(this.dynamicAddNode.props.type == enumConst.approvalType.ASSIGN_USER && this.dynamicAddNode.props.targetObj.personnels.length < 2) {
          this.$set(this.dynamicAddNode.props, 'mode', 'DEFAULT')
        }
      }else{
        this.dynamicNode.props.targetObj.personnels.splice(inx, 1)
        if(this.dynamicNode.props.type == enumConst.approvalType.ASSIGN_USER && this.dynamicNode.props.targetObj.personnels.length < 2) {
          this.$set(this.dynamicNode.props, 'mode', 'DEFAULT')
        }
      }
      this.$forceUpdate()
    },
    getFlowReplyList () {
      getFlowReply(this.flowableInfo.jvsAppId).then(res => {
        if(res.data && res.data.code == 0) {
          this.replyList = res.data.data
        }
      })
    },
    setOpinion (item) {
      this.$set(this.opinionForm, 'opinion', item.content)
    },
    // 签字板
    async signatureSubmit (data) {
      if(data) {
        customUpload(`/jvs-system/upload`, data, '签名.png',{
          id:this.flowableInfo.id+'_'+new Date().getTime(),
          path:`/low/code/form/appId_${this.flowableInfo.jvsAppId}`
        }).then(res => {
          if(res && res.data && res.data.code == 0 && res.data.data && res.data.data.fileLink) {
            let obj = {
              name: res.data.data.originalFileName || res.data.data.name,
              url: res.data.data.fileLink,
              fileName: res.data.data.fileName,
              bucketName: res.data.data.bucketName,
              fileSize: res.data.data.fileSize,
            }
            this.$set(this.opinionForm, 'sign', [obj])
          }
        })
      }
    },
  }
}
</script>
<style lang="scss" scoped>
.form-show-info/deep/{
  .empty-img{
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: calc(100vh - 120px);
  }
  .el-form{
    .el-form-item{
      padding: 0 20px;
    }
  }
  .jvs-form .el-form-item.form-btn-bar .el-form-item__content{
    text-align: left;
  }
}
.loading-back{
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background-image: url('../../../../../public/jvs-ui-public/img/loading.gif');
  background-repeat: no-repeat;
  background-position: center;
  background-color: rgba(255,255,255,.5);
  z-index: 99;
}
.reply-list{
  .reply-list-item{
    cursor: pointer;
    height: 32px;
    line-height: 32px;
    padding: 0 10px;
  }
  .reply-list-item:hover{
    background: #eff2f7;
  }
}
</style>
