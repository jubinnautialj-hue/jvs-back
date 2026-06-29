<template>
  <div class="jvs-flow-node">
    <ul class="jvs-flow-node-list" v-if="approverList && approverList.length > 0">
      <li v-for="(node, index) in nodeList" :key="'node-item-'+index" class="jvs-flow-node-list-item">
        <el-input v-model="node.name" disabled size="mini" :style="`width: ${item.nodeNameWidth ? (item.nodeNameWidth+'%') : '120px'};`"></el-input>
        <el-select v-model="node.props.mode" placeholder="请选择" size="mini" disabled style="margin-left: 10px;width: 50px;max-width: 80px;">
          <el-option
            v-for="mit in modeTypeList"
            :key="mit.value"
            :label="mit.label"
            :value="mit.value">
          </el-option>
        </el-select>
        <el-select v-model="node.props.type" placeholder="请选择" size="mini" disabled style="margin-left: 10px;width: 100px;max-width: 120px;">
          <el-option
            v-for="ait in approvalTypeList"
            :key="ait.text"
            :label="ait.text"
            :value="ait.label">
          </el-option>
        </el-select>
        <div v-if="['USER_FIELD', 'DEPT_FIELD'].indexOf(node.props.type) > -1 && checkedCom[node.id]" style="margin-left: 10px;flex: 1;">
          <el-select v-if="node.props.type == 'USER_FIELD'" v-model="checkedCom[node.id]" multiple :disabled="!node.canDynamicApprover" clearable size="mini" @change="userComChange(node, approverList[index])" class="mutiple-select">
            <el-option v-for="(com, cix) in userComList" :key="'user-com-'+cix" :label="com.label" :value="com.prop"></el-option>
          </el-select>
          <el-select v-if="node.props.type == 'DEPT_FIELD'" v-model="checkedCom[node.id]" multiple :disabled="!node.canDynamicApprover" clearable size="mini" @change="userComChange(node, approverList[index])" class="mutiple-select">
            <el-option v-for="(com, cix) in deptComList" :key="'dept-com-'+cix" :label="com.label" :value="com.prop"></el-option>
          </el-select>
        </div>
        <div v-else style="margin-left: 10px;flex: 1;">
          <userForm
            prop="approvers"
            :selectable="true"
            :infoable="true"
            :disabled="([approvalType.ASSIGN_USER, 'ASSIGN_CARBON_COPY', approvalType.SELF_SELECT, approvalType.ROLE, approvalType.JOB, approvalType.DEPT].indexOf(node.props.type) == -1) || (!node.canDynamicApprover) || item.disabled"
            :form="approverList[index]"
            :type="getSelectType(node.props.type)"
            :resetRadom="resetRadom"
            :userAllList="userList"
            :departmentList="departmentList"
            :roleOption="roleOption"
            :postList="postList"
            :personnelScopes="(node && node.props && node.props.personnelScope && node.props.personnelScope.type == 'CUSTOM') ? node.props.personnelScope.personnelScopes : []"
            @change="formChange"
          ></userForm>
        </div>
      </li>
    </ul>
    <div v-if="canDynamicAddNode" style="margin-top: 10px;">
      <div v-if="dynamicNode" class="jvs-flow-node-list">
        <div style="margin: 0;display: flex;align-items: center;border-top: 0;" class="jvs-flow-node-list-item">
          <el-input v-model="dynamicNode.name" size="mini" style="width: 180px;" :disabled="item.disabled" @change="formChange"></el-input>
          <el-select v-model="dynamicNode.props.type" placeholder="请选择" size="mini" :disabled="item.disabled" style="max-width: 140px;margin-left: 10px;" @change="dynamicAddNodeTypeChange">
            <el-option
              v-for="ait in approvalTypeList"
              v-show="[approvalType.SELF, approvalType.SELF_SELECT].indexOf(ait.label) == -1"
              :key="ait.text"
              :label="ait.text"
              :value="ait.label">
            </el-option>
          </el-select>
          <el-select v-model="dynamicNode.props.mode" v-if="[approvalType.ROLE, approvalType.JOB, approvalType.DEPT].indexOf(dynamicNode.props.type) > -1 || ([approvalType.ASSIGN_USER, 'ASSIGN_CARBON_COPY'].indexOf(dynamicNode.props.type) > -1 && dynamicNode.props.targetObj.personnels && dynamicNode.props.targetObj.personnels.length > 1)" :disabled="item.disabled" placeholder="请选择" size="mini" style="margin-left: 10px;max-width: 180px;">
            <el-option
              v-for="mit in modeTypeList"
              v-show="mit.value != 'DEFAULT'"
              :key="mit.value"
              :label="mit.label"
              :value="mit.value">
            </el-option>
          </el-select>
          <div style="margin-left: 10px;flex: 1;" v-if="[approvalType.ASSIGN_USER, 'ASSIGN_CARBON_COPY', approvalType.ROLE, approvalType.JOB, approvalType.DEPT].indexOf(dynamicNode.props.type) > -1">
            <userForm
              ref="dynamicNodeUser"
              prop="personnels"
              :selectable="true"
              :infoable="true"
              :disabled="[approvalType.ASSIGN_USER, 'ASSIGN_CARBON_COPY', approvalType.ROLE, approvalType.JOB, approvalType.DEPT].indexOf(dynamicNode.props.type) == -1 || item.disabled"
              :form="dynamicNode.props.targetObj"
              :type="getSelectType(dynamicNode.props.type)"
              :resetRadom="resetRadom"
              :userAllList="userList"
              :departmentList="departmentList"
              :roleOption="roleOption"
              :postList="postList"
              :personnelScopes="(dynamicNode && dynamicNode.props && dynamicNode.props.personnelScope && dynamicNode.props.personnelScope.type == 'CUSTOM') ? dynamicNode.props.personnelScope.personnelScopes : []"
              @change="dynamicAddNodeChangeUser"
            ></userForm>
          </div>
        </div>
      </div>
      <el-button v-else v-show="bindFlowId" size="mini" type="primary" @click="addFlowRowHandle">新增节点</el-button>
    </div>
    <el-input v-show="false" v-model="bindFlowId"></el-input>
  </div>
</template>
<script>
import { getFlowNodeListById } from '../api'
import { getAllUserComList } from '@/views/flowable/api/flowable'
import enumConst, { approvalType, approvalMode } from '@/views/flowable/views/design/common/enumConst'
import userForm from './userForm'
export default {
  name: 'jvs-flow-node',
  components: {
    userForm
  },
  props: {
    formRef: {
      type: String,
      default: 'ruleForm'
    },
    item: {
      type: Object
    },
    data: {
      type: [Array, String]
    },
    originOption: {
      type: Object
    },
    defalutSet: {
      type: Object
    },
    rowData: {
      type: Object
    },
    // 用户列表
    userList : {
      type: Array,
      default: () => {
        return []
      }
    },
    // 角色列表
    roleOption: {
      type: Array,
      default: () => {
        return []
      }
    },
    // 部门列表
    departmentList: {
      type: Array,
      default: () => {
        return []
      }
    },
    // 岗位列表
    postList: {
      type: Array,
      default: () => {
        return []
      }
    },
    resetRadom: {
      type: Number
    },
    designId: {
      type: String
    },
    forms: {
      type: Object
    },
    dataModelId: {
      type: String
    },
    changeRandom: {
      type: Number
    },
    changeDomItem: {
      type: Object
    },
    isView: {
      type: Boolean
    },
    execsList: {
      type: Array
    },
    jvsAppId:  {
      type: String
    },
    dataTriggerFresh: {
      type: Number
    },
    openByButton: {
      type: Object
    }
  },
  computed: {
    userComIds () {
      return this.userComList.filter(item => {
        return item.prop
      })
    },
    deptComIds () {
      return this.deptComList.filter(item => {
        return item.prop
      })
    }
  },
  data () {
    return {
      nodeList: [],
      approverList: [],
      modeTypeList: [
        {label: ' ', value: 'DEFAULT'},
        {label: '会签', value: approvalMode.AND},
        {label: '或签', value: approvalMode.OR},
        {label: '顺签', value: approvalMode.NEXT}
      ],
      approvalType: approvalType,
      approvalTypeList: [
        { label: approvalType.ASSIGN_USER, text: "指定人员" },
        { label: 'ASSIGN_CARBON_COPY', text: '指定抄送人'},
        { label: approvalType.SELF_SELECT, text: "发起人自选" },
        { label: approvalType.LEADER_TOP, text: "连续多级主管", disabled: false },
        { label: approvalType.LEADER, text: "直属主管", disabled: false },
        { label: approvalType.ROLE, text: "角色" },
        { label: approvalType.SELF, text: "发起人自己" },
        { label: approvalType.JOB, text: "岗位" },
        { label: approvalType.DEPT, text: "部门" },
        { label: 'USER_FIELD', text: "成员字段" },
        { label: 'DEPT_FIELD', text: "部门字段" },
      ],
      bindFlowId: '', // 绑定的流程id
      enumConst: enumConst,
      canDynamicAddNode: false,
      dynamicNode: null,
      userComList: [],
      deptComList: [],
      checkedCom: {}
    }
  },
  async created () {
    await this.getAllUserComListHandle()
    this.init(true)
  },
  methods: {
    init (bool) {
      console.log(this.data)
      if(this.data) {
        if(typeof this.data == 'string') {
          this.getFlowNodeList(this.data)
        }else{
          if(this.forms[this.item.prop+'_flowId']) {
            this.bindFlowId = this.forms[this.item.prop+'_flowId']
            this.getFlowNodeList(this.bindFlowId, this.data)
          }
          if(this.forms[this.item.prop+'_dynamicNode']) {
            this.dynamicNode = this.forms[this.item.prop+'_dynamicNode']
          }
        }
      }else{
        this.approverList = []
        if(!bool) {
          this.$emit('setFlowNodeData', this.approverList, this.bindFlowId, this.dynamicNode)
        }
      }
      this.$forceUpdate()
    },
    getFlowNodeList (id, data) {
      this.bindFlowId = id
      getFlowNodeListById(id, (this.forms.jvsFlowTask && (this.openByButton && this.openByButton.type == 'oa_workflow_restart')) ? {taskId: this.forms.jvsFlowTask.id}: null).then(res => {
        if(res.data && res.data.code == 0 && res.data.data) {
          this.nodeList = res.data.data.nodes
          // console.log(this.nodeList)
          this.canDynamicAddNode = res.data.data.canDynamicAddNode
          this.approverList = []
          this.nodeList.filter(rit => {
            let tp = {
              nodeId: rit.id,
              approvers: (rit.props && rit.props.targetObj && rit.props.targetObj.personnels) ? rit.props.targetObj.personnels : [],
            }
            if(data && data.length > 0) {
              data.filter(dit => {
                if(dit.nodeId && dit.nodeId == tp.nodeId) {
                  tp.approvers = dit.approvers
                }
              })
            }
            if(['USER_FIELD', 'DEPT_FIELD'].indexOf(rit.props.type) > -1) {
              this.$set(this.checkedCom, rit.id, [])
              tp.approvers.filter(ap => {
                if(rit.props.type == 'USER_FIELD') {
                  if(this.userComIds.indexOf(ap.id) > -1) {
                    this.checkedCom[rit.id].push(ap.id)
                  }
                }
                if(rit.props.type == 'DEPT_FIELD') {
                  if(this.deptComIds.indexOf(ap.id) > -1) {
                    this.checkedCom[rit.id].push(ap.id)
                  }
                }
              })
              this.userComChange(rit, tp)
            }
            this.approverList.push(tp)
          })
          this.$emit('setFlowNodeData', this.approverList, this.bindFlowId, this.dynamicNode)
          this.$forceUpdate()
        }
      })
    },
    getSelectType (type) {
      let str = ''
      switch(type) {
        case approvalType.ASSIGN_USER: str = 'user';break;
        case 'ASSIGN_CARBON_COPY': str = 'user';break;
        case approvalType.ROLE: str = 'role';break;
        case approvalType.JOB: str = 'job';break;
        case approvalType.DEPT: str = 'department';break;
        default: str = 'user';break;
      }
      return str
    },
    formChange () {
      this.$emit('setFlowNodeData', this.approverList, this.bindFlowId, this.dynamicNode)
    },
    reset() {
      this.init()
    },
    addFlowRowHandle () {
      let obj = {
        name: '审批人',
        id: this.getRandomId(),
        nodeForm: {formId: "", sendUserForm: true, version: "" },
        type: enumConst.nodeType.SP
      }
      let props = JSON.parse(JSON.stringify(enumConst.getDefaultNodeProps(obj.type)))
      props.btn = JSON.parse(JSON.stringify(enumConst.nodeButtonList))
      obj.props = props
      this.dynamicNode = JSON.parse(JSON.stringify(obj))
      this.formChange()
      this.$forceUpdate()
    },
    getRandomId(){
      return (Math.floor(Math.random() * (99999 - 10000)) + 10000).toString() + new Date().getTime().toString().substring(5, 11)
    },
    dynamicAddNodeTypeChange () {
      if(this.$refs.dynamicNodeUser) {
        this.$refs.dynamicNodeUser.clearUser()
      }
      this.$set(this.dynamicNode.props.targetObj, 'personnels', [])
      if([enumConst.approvalType.ROLE, enumConst.approvalType.JOB, approvalType.DEPT].indexOf(this.dynamicNode.props.type) > -1) {
        this.$set(this.dynamicNode.props, 'mode', 'AND')
      }else{
        this.$set(this.dynamicNode.props, 'mode', 'DEFAULT')
      }
      this.formChange()
    },
    dynamicAddNodeChangeUser () {
      if(this.dynamicNode.props.type == enumConst.approvalType.ASSIGN_USER){
        if(this.dynamicNode.props.targetObj.personnels.length < 2) {
          this.$set(this.dynamicNode.props, 'mode', 'DEFAULT')
        }else{
          this.$set(this.dynamicNode.props, 'mode', 'AND')
        }
      }
      this.formChange()
      this.$forceUpdate()
    },
    async getAllUserComListHandle () {
      this.userComList = []
      this.deptComList = []
      if(this.jvsAppId && this.dataModelId) {
        await getAllUserComList(this.jvsAppId, this.dataModelId).then(res => {
          if(res.data && res.data.code == 0) {
            res.data.data.filter(rit => {
              if(rit.type == 'user') {
                this.userComList.push(rit)
              }
              if(rit.type == 'department' || rit.type == 'dept') {
                this.deptComList.push(rit)
              }
            })
          }
        })
      }
    },
    userComChange (node, item) {
      let list = []
      if(node.props.type === 'DEPT_FIELD') {
        for(let i in this.deptComList) {
        if(this.checkedCom[node.id].indexOf(this.deptComList[i].prop) > -1) {
          list.push({
            id: this.deptComList[i].prop,
            name: this.deptComList[i].label,
            type: this.deptComList[i].type == 'department' ? 'dept' : this.deptComList[i].type
          })
        }
      }
      }else{
        for(let i in this.userComList) {
          if(this.checkedCom[node.id].indexOf(this.userComList[i].prop) > -1) {
            list.push({
              id: this.userComList[i].prop,
              name: this.userComList[i].label,
              type: this.userComList[i].type == 'department' ? 'dept' : this.userComList[i].type
            })
          }
        }
      }
      this.$set(item, 'approvers',  list)
    },
  },
  watch: {
    resetRadom: {
      handler (newVal, oldVal) {
        if(newVal > -1) {
          this.reset()
        }
      }
    },
    data: {
      handler (newVal, oldVal) {
        if(newVal) {
          if(typeof newVal == 'string') {
            this.getFlowNodeList(newVal)
          }else{
            this.approverList = newVal
            if(this.forms[this.item.prop+'_flowId']) {
              this.bindFlowId = this.forms[this.item.prop+'_flowId']
            }
          }
        }else{
          this.approverList = []
          this.$emit('setFlowNodeData', this.approverList, this.bindFlowId, this.dynamicNode)
        }
        this.$forceUpdate()
      }
    }
  }
}
</script>
<style lang="scss">
.jvs-flow-node{
  width: 100%;
  ul, li{
    margin: 0;
    padding: 0;
  }
  .jvs-flow-node-list{
    .jvs-flow-node-list-item{
      display: flex;
      align-items: center;
      margin-top: 15px;
      border-bottom: 1px solid #f5f5f5;
      padding-bottom: 10px;
      .is-disabled{
        .el-input__inner{
          padding: 0 10px;
          background-color: #fff!important;
          border-color: #fff!important;
          height: 100%!important;
        }
      }
      .mutiple-select{
        width: 100%;
        height: 36px;
        .el-input{
          .el-input__inner{
            height: 36px!important;
          }
        }
      }
    }
    .jvs-flow-node-list-item:nth-of-type(1){
      margin-top: 0;
      border-top: 1px solid #f5f5f5;
      padding-top: 10px;
    }
  }
}
</style>
