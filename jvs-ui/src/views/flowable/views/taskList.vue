<template>
  <div class="task-list" v-if="isMounted">
    <jvs-table
      ref="taskTable"
      pageheadertitle="待办任务"
      :selectable="true"
      :option="option"
      :data="tableData"
      :page="page"
      :loading="tableLoading"
      @on-load="getList"
      @search-change="searchHandle"
      @selection-change="selectChange"
    >
      <template slot="menuLeft">
        <jvs-button type="primary" size="mini" @click="batchHandle">批量处理</jvs-button>
      </template>
      <template slot="menu" slot-scope="scope">
        <div>
          <!-- <jvs-button type="text" size="mini" v-if="scope.row.taskStatus == 1" @click="claimHandle(scope.row)">签收</jvs-button> -->
          <jvs-button type="text" size="mini" v-if="scope.row.taskStatus == 1" @click="dealHandle(scope.row)">办理</jvs-button>
          <!-- <jvs-button type="text" size="mini" v-if="true ||scope.row.taskStatus == 2" @click="sendHandle(scope.row)">委派</jvs-button> -->
          <!-- <jvs-button type="text" size="mini" v-if="scope.row.taskStatus == 2" @click="dealHandle(scope.row)">取消签收</jvs-button> -->
          <!-- 不一定会进表单。。 -->
          <jvs-button type="text" size="mini" @click="dealHandle(scope.row, 'view')">进度</jvs-button>
        </div>
      </template>
    </jvs-table>
    <el-dialog
      :visible.sync="dialogVisible"
      append-to-body
      :before-close="handleClose"
      :title="taskFormType == 'view' ? '进度' : '办理'"
      class="drawer-popup-dialog flow-task-info-dialog"
      width="95%"
      :modal="true"
      :close-on-click-modal="false"
      :fullscreen="dialogFullScreen"
    >
      <taskForm
        v-if="dialogVisible"
        :rowData="rowData"
        :taskFormType="taskFormType"
        @close="closeHandle"
        @fullscreenChange="dialogFullScreen=!dialogFullScreen;"
        @closeHandle="handleClose"
      />
    </el-dialog>
    <el-dialog
      v-if="viewVisible"
      :visible.sync="viewVisible"
      append-to-body
      title="查看进度"
      width="70%"
      :before-close="viewClose"
    >
      <img :src="imgSrc" alt="" style="display: block; width: 100%; height: 100%" />
    </el-dialog>
    <el-dialog
      v-if="userVisible"
      :visible.sync="userVisible"
      append-to-body
      title="委派"
      width="90%"
      :before-close="userClose"
    >
      <userListTool :form="rowData.assignee" prop="id" />
      <el-row
        style="
          margin-top: 20px;
          display: flex;
          justify-content: center;
          align-items: center;
        "
      >
        <jvs-button size="mini" type="primary" @click="assignHandle"
          >确定</jvs-button
        >
        <jvs-button size="mini" @click="userClose">取消</jvs-button>
      </el-row>
    </el-dialog>
    <!-- 批量审批 -->
    <el-dialog
      title="批量处理"
      :visible.sync="opinionDialogVisible"
      append-to-body
      :before-close="batchClose">
      <div v-if="opinionDialogVisible">
        <el-form ref="ruleForm" :rules="opinionRules" :model="opinionForm" label-width="80px" label-position="top">
          <el-form-item label="审批意见" prop="content">
            <el-input
              type="textarea"
              :rows="3"
              placeholder="请输入内容"
              v-model="opinionForm.content">
            </el-input>
          </el-form-item>
        </el-form>
        <el-row style="display:flex;justify-content: center;align-items: center;margin-top: 20px;">
          <jvs-button size="mini" type="primary" :loading="agreeLoading" :disabled="refuseLoading" @click="agreeRefuseHandle('PASS')">同意</jvs-button>
          <jvs-button size="mini" type="primary" :loading="refuseLoading" :disabled="agreeLoading" @click="agreeRefuseHandle('REFUSE')">拒绝</jvs-button>
        </el-row>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { queryTaskList, claimTask, assignTask, batchExecute } from "../api/flowable"
import taskForm from './taskForm'
import userListTool from '../components/util/user'
export default {
  name: 'task-list',
  components: { taskForm, userListTool },
  props: {
    pageSize: {
      type: Number,
      default: ()=> {
        return 0
      }
    },
    notRequireData: {
      type: Boolean
    }
  },
  data () {
    return {
      // 查询条件
      queryParams: {},
      tableData: [], // 表格数据
      tableLoading: false,
      option: {
        // align: 'center',
        // menuAlign: 'center',
        page: true,
        addBtn: false,
        viewBtn: false,
        editBtn: false,
        delBtn: false,
        showOverflow: true,
        search: true,
        searchBtnText: '查询',
        cancal: false,
        menuWidth: this.pageSize ? '80px': '',
        column: [
          {
            label: '流程标题',
            prop: 'title',
            search: true,
            searchSpan: 4,
            width:350
          },
          {
            label: '流程名称',
            prop: 'name',
            search: true,
            searchSpan: 4
          },
          // {
          //   label: '流程编号',
          //   prop: 'taskCode',
          //   search: true,
          //   searchSpan: 4
          // },
          {
            label: '当前环节',
            prop: 'nodeName',
          },
          {
            label: '上一节点',
            prop: 'previousNodeName',
          },
          {
            label: '上一节点审批人',
            prop: 'previousApproverName',
          },
          {
            label: '上一节点审批时间',
            prop: 'previousApproveTime',
          },
          {
            label: '发起人',
            prop: 'createBy',
            search: true,
            searchSpan: 4
          },
          // {
          //   label: '状态',
          //   prop: 'taskStatus',
          //   hide: true,
          //   search: true,
          //   type: 'select',
          //   dicData: [
          //     {label: '待审批', value: 1},
          //     {label: '已通过', value: 2},
          //     {label: '已拒绝', value: 3},
          //     {label: '已终止', value: 4}
          //   ],
          //   searchSpan: 4
          // },
          {
            label: '创建时间',
            prop: 'createTime',
            datetype: 'datetime',
            type: "datePicker",
            format: "yyyy-MM-dd hh:mm:ss",
            valueFormat: "yyyy-MM-dd hh:mm:ss",
          }
        ]
      },
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
        pageSizes: [20, 50, 100, 200, 500, 1000]
      },
      rowData: {},
      dialogVisible: false,
      viewVisible: false,
      imgSrc: '',
      userVisible: false,
      taskFormType: '',
      isMounted: false,
      selectIds: [],
      opinionDialogVisible: false,
      opinionForm: {},
      opinionRules: {
        content: [
          { required: true, message: '请输入审批意见', trigger: 'change' }
        ],
      },
      agreeLoading: false,
      refuseLoading: false,
      dialogFullScreen: false,
    }
  },
  methods: {
    // 获取数据
    getList (page) {
      if(this.notRequireData) {
        return false
      }
      let query={
        current: this.page.currentPage,
        size: this.pageSize > 0 ? this.pageSize : this.page.pageSize
      }
      this.tableLoading=true
      let temp= {}
      if(this.queryParams.name) {
        temp.flowName = this.queryParams.name
      }
      if(this.queryParams.title) {
        temp.title = this.queryParams.title
      }
      if(this.queryParams.createBy) {
        temp.sendUser = this.queryParams.createBy
      }
      if(this.queryParams.taskCode) {
        temp.taskCode = this.queryParams.taskCode
      }
      temp=Object.assign(temp, query)
      queryTaskList(temp).then(res => {
        if (res.data.code==0) {
          this.tableData=res.data.data.records
          this.tableLoading=false
          this.page.total=res.data.data.total
          this.page.currentPage=res.data.data.current
        }
      }).catch(e => {
        this.tableLoading=false
      })
    },
    // 搜索
    searchHandle (form) {
      this.queryParams=form
      this.getList()
    },
    // 处理
    dealHandle (row, type) {
      if (type) {
        this.taskFormType=type
      } else {
        this.taskFormType=""
      }
      this.rowData=row
      this.dialogVisible=true
    },
    handleClose () {
      this.dialogVisible=false
      this.dialogFullScreen=false
    },
    viewClose () {
      this.viewVisible=false
      this.imgSrc=''
    },
    // 签收
    claimHandle (row) {
      this.rowData=row
      claimTask(row.taskId).then(res => {
        if (res.data.code==0) {
          // this.$message.success("签收成功")
          this.$notify({
            title: '提示',
            message: '签收成功',
            position: 'bottom-right',
            type: 'success'
          });
          this.getList()
        }
      })
    },
    // 委派
    sendHandle (row) {
      this.rowData=row
      this.userVisible=true
    },
    // 关闭委派弹框
    userClose () {
      this.userVisible=false
    },
    // 委派提交
    assignHandle () {
      assignTask(this.rowData.taskId, this.rowData.assignee.id).then(res => {
        if (res.data.code==0) {
          this.$notify({
            title: '提示',
            message: '委派成功',
            position: 'bottom-right',
            type: 'success'
          });
          this.getList()
        }
      })
      this.userClose()
    },
    closeHandle (bool) {
      if (bool) {
        this.handleClose()
        this.getList()
        this.$emit('fresh')
      }
    },
    selectChange (rows) {
      this.selectIds = []
      rows.filter(r => {
        this.selectIds.push(r.taskNodeId)
      })
    },
    batchHandle () {
      let needNotice = false
      this.tableData.filter(tit => {
        let index = this.selectIds.indexOf(tit.taskNodeId)
        if(index > -1 && tit.nodeId == '10000') {
          needNotice = true
        }
      })
      if(needNotice) {
        this.$confirm('当前环节为发起人时不可进行批量处理，是否继续？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.tableData.filter(tit => {
            let index = this.selectIds.indexOf(tit.taskNodeId)
            if(index > -1 && tit.nodeId == '10000') {
              this.selectIds.splice(index, 1)
              this.$refs.taskTable.$refs.multipleTable.toggleRowSelection(tit)
            }
          })
          if(this.selectIds.length > 0) {
            this.opinionDialogVisible = true
          }else{
            this.$notify({
              title: '提示',
              message: '请重新选择进行批量处理',
              position: 'bottom-right',
              type: 'warning'
            })
          }
        }).catch(e => {})
      }else{
        this.opinionDialogVisible = true
      }
    },
    batchClose () {
      this.opinionDialogVisible = false
      this.agreeLoading = false
      this.refuseLoading = false
      this.opinionForm = {}
    },
    agreeRefuseHandle (type) {
      if(type == 'PASS') {
        this.agreeLoading = true
      }else{
        this.refuseLoading = true
      }
      batchExecute({
        ids: this.selectIds,
        nodeOperationType: type,
        opinion: this.opinionForm
      }).then(res => {
        if(res && res.data && res.data.code == 0) {
          this.$notify({
            title: '提示',
            message: '批量处理成功',
            position: 'bottom-right',
            type: 'success'
          });
          this.batchClose()
          this.getList()
          this.selectIds = []
          if(this.$refs.taskTable) {
            this.$refs.taskTable.clearSelect()
          }
        }
      }).catch(e => {
        this.batchClose()
        this.getList()
      })
    },
  },
  mounted () {
    let that=this
    window.addEventListener('message', (e) => {
      if (e.data.type==='setLocalStorage') {
        that.$store.commit("SET_ACCESS_TOKEN", e.data.data.TOKEN)
        that.$store.commit("SET_USER_INFO",{tenantId:e.data.data.tenantId})
        that.isMounted=true
      }
    })
    if (that.$store.getters.access_token) {
      that.isMounted=true
    }
  }
}
</script>
