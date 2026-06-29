<template>
  <div class="apply-list">
    <jvs-table
      pageheadertitle='我的申请'
      :option="option"
      :data="tableData"
      :page="page"
      :loading="tableLoading"
      @on-load="getList"
      @search-change="searchHandle"
    >
      <template slot="updateTime" slot-scope="scope">
        <span>{{scope.row.taskStatus == 1 ? '' : scope.row.updateTime}}</span>
      </template>
      <template slot="menu" slot-scope="scope">
        <jvs-button type="text" size="mini" @click="viewHandle(scope.row)">查看进度</jvs-button>
        <jvs-button type="text" size="mini" v-if="scope.row.taskStatus !== 1 && scope.row.extend && scope.row.extend.enableRestart" @click="restartHandle(scope.row)">重启</jvs-button>
        <jvs-button type="text" size="mini" v-if="scope.row.taskStatus == 1 && scope.row.extend && scope.row.extend.enableCancel" @click="cancelDelHandle(scope.row, '撤回')">撤回</jvs-button>
        <jvs-button type="text" size="mini" v-if="scope.row.taskStatus == 1" @click="urgeHandle(scope.row)">催办</jvs-button>
        <!-- <jvs-button type="text" size="mini" v-if="scope.row.ended"  @click="cancelDelHandle(scope.row, '删除')">删除</jvs-button> -->
      </template>
    </jvs-table>
    <!-- 删除  撤回  的理由 -->
    <el-dialog :title="mstr+'理由'" :visible.sync="reasonVisible" v-if="reasonVisible" append-to-body :before-close="reasonClose">
      <el-input
        type="textarea"
        :rows="2"
        placeholder="请输入理由"
        v-model="reasonForm.reason">
      </el-input>
      <el-row style="margin-top:20px;display:flex;align-items:center;justify-content:center;">
        <jvs-button size="mini" type="primary" @click="cacelDelSubmit">确定</jvs-button>
        <jvs-button size="mini" @click="reasonClose">取消</jvs-button>
      </el-row>
    </el-dialog>
    <!-- 进度 -->
    <el-dialog title="进度"
      :visible.sync="infoVisible"
      append-to-body
      class="drawer-popup-dialog flow-task-info-dialog"
      width="95%"
      :modal="true"
      :close-on-click-modal="false"
      :fullscreen="dialogFullScreen"
      :before-close="infoClose">
      <infoForm
        v-if="infoVisible"
        :rowData="rowData"
        :btnHide="true"
        @fullscreenChange="dialogFullScreen=!dialogFullScreen;"
        @closeHandle="infoClose"
        @close="closeHandle" />
    </el-dialog>
    <el-dialog
      :title="title"
      v-if="dialogVisible"
      width="75%"
      append-to-body
      :fullscreen="true"
      :visible.sync="dialogVisible"
      :before-close="handleClose">
      <iframe
        :src="formUrl"
        class="apply-form-iframe"
        scrolling="auto"
        v-if="isOut"
      >
      </iframe>
      <showForm v-else :formUrl="formUrl" :rowData="rowData" :defaultFormData="defaultFormData" :isRestart="true" :approverList="approverList" @close="closeFormHandle"/>
    </el-dialog>
  </div>
</template>
<script>
import {applyList, candelProcess, queryEchoForm, urgeTask} from "../api/flowable"
import showForm from './componet/info'
import infoForm from './info'
import {restartProcess} from "@/views/flowable/views/componet/api";
export default {
  name: 'task-list',
  components: {infoForm, showForm},
  data(){
    return {
      defaultFormData: {},
      title: '',
      formUrl: '',
      dialogVisible: false,
      isOut: false,
      approverList: [],
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
            searchSpan: 4,
            search: true
          },
          // {
          //   label: '流程编号',
          //   prop: 'taskCode',
          //   search: true,
          //   searchSpan: 4,
          //   display:false
          // },
          {
            label: '状态',
            prop: 'taskStatus',
            search: true,
            searchSpan: 4,
            type: 'select',
            dicData: [
              {label: '待审批', value: 1},
              {label: '已通过', value: 2},
              {label: '已拒绝', value: 3},
              {label: '已终止', value: 4}
            ]
          },
          {
            label: '流程发起人',
            prop: 'createBy'
          },
          {
            label: '创建时间',
            prop: 'createTime',
            datetype: 'datetime',
            type: "datePicker",
            format: "yyyy-MM-dd hh:mm:ss",
            valueFormat: "yyyy-MM-dd hh:mm:ss"
          },
          {
            label: '完成时间',
            prop: 'updateTime',
            slot: true,
            datetype: 'datetime',
            type: "datePicker",
            format: "yyyy-MM-dd hh:mm:ss",
            valueFormat: "yyyy-MM-dd hh:mm:ss"
          }
        ]
      },
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
        pageSizes: [20, 50, 100, 200, 500, 1000]
      },
      // 撤回  删除  理由
      reasonVisible: false,
      reasonForm: {},
      rowData: {},
      mstr: '',
      infoVisible: false,
      dialogFullScreen: false,
    }
  },
  methods: {
    // 获取数据
    getList (page) {
      let query={
        current: this.page.currentPage,
        size: this.page.pageSize
      }
      this.tableLoading = true
      let temp = JSON.parse(JSON.stringify(this.queryParams))
      temp = Object.assign(temp, query)
      applyList(temp).then(res => {
        if (res.data.code==0) {
          this.tableData = res.data.data.records
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
      this.queryParams = form
      this.getList()
    },
    // 撤回 删除
    cancelDelHandle (row, str) {
      this.mstr = str
      this.rowData = row
      this.reasonVisible = true
    },
    // 重启
    async restartHandle(row) {
      this.rowData = row
      await queryEchoForm(this.rowData.jvsAppId, this.rowData.dataModelId, this.rowData.dataId, this.rowData.formId).then(res => {
        if(res.data.code == 0) {
          this.defaultFormData = JSON.parse(JSON.stringify(res.data.data))
          this.title = row.name
          this.approverList = []
          if(row.formId) {
            this.formUrl = `/page-design-ui/#/form/info?id=${row.formId}&dataModelId=${row.dataModelId}&flowId=${row.id}&jvsAppId=${row.jvsAppId}`
            this.isOut = false
            if(row.manualNodes && row.manualNodes.length > 0) {
              for(let i in row.manualNodes) {
                // 发起人自选
                if(row.manualNodes[i].props && row.manualNodes[i].props.type == "SELF_SELECT") {
                  this.approverList.push({
                    nodeId: row.manualNodes[i].id,
                    nodeName: row.manualNodes[i].name,
                    approvers: row.manualNodes[i].props.targetObj.personnels,
                    props: row.manualNodes[i].props
                  })
                }
              }
            }
            this.dialogVisible = true
          }else{
            let obj = {
              id: row.id,
            }
            restartProcess(obj).then(res => {
              if(res.data.code == 0) {
                this.$notify({
                  title: '提示',
                  message: '流程重启成功',
                  position: 'bottom-right',
                  type: 'success'
                });
                this.getList()
              }
            }).catch(e => {
            })
          }
        }
      })
    },
    // 关闭表单
    handleClose () {
      this.dialogVisible = false
      this.title = ""
      this.getList()
    },
    closeFormHandle (bool) {
      if(bool) {
        this.handleClose()
      }
    },
    reasonClose () {
      this.reasonVisible = false
      this.reasonForm = {}
    },
    // 确定  撤回 删除
    cacelDelSubmit () {
      let obj = {
        reason: this.reasonForm.reason
      }
      candelProcess(this.rowData.id, obj).then(res => {
        if(res.data.code == 0) {
          this.$notify({
            title: '提示',
            message: this.mstr+"成功",
            position: 'bottom-right',
            type: 'success'
          });
          this.getList()
          this.reasonClose()
        }
      })
    },
    viewHandle (row) {
      this.rowData = row
      this.infoVisible = true
    },
    infoClose () {
      this.infoVisible = false
    },
    closeHandle (bool) {
      if(bool) {
        this.infoClose()
        this.getList()
      }
    },
    // 催办
    urgeHandle (row) {
      urgeTask(row.id).then(res => {
        if(res && res.data && res.data.code == 0) {
          this.$notify({
            title: '提示',
            message: '催办成功',
            position: 'bottom-right',
            type: 'success'
          });
          this.getList()
        }
      })
    }
  }
}
</script>
<style lang="scss">
.form-fullscreen-dialog-allbody{
  .el-dialog.is-fullscreen{
    .el-dialog__body{
      width: 100%;
      left: 0;
      box-sizing: border-box;
      padding: 8px 10px;
    }
  }
}
</style>
