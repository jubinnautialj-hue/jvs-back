<template>
  <div class="task-list" v-if="isMounted">
    <jvs-table
      pageheadertitle="抄送列表"
      :option="option"
      :data="tableData"
      :page="page"
      :loading="tableLoading"
      @on-load="getList"
      @search-change="searchHandle"
    >
      <template slot="menu" slot-scope="scope">
        <div>
          <jvs-button type="text" size="mini" @click="viewHandle(scope.row)">查看进度</jvs-button>
        </div>
      </template>
    </jvs-table>
    <!-- 进度 -->
    <el-dialog
      title="进度"
      :visible.sync="infoVisible"
      v-if="infoVisible"
      append-to-body
      class="drawer-popup-dialog flow-task-info-dialog"
      width="95%"
      :modal="true"
      :close-on-click-modal="false"
      :fullscreen="dialogFullScreen"
      :before-close="infoClose">
      <infoForm
        :rowData="rowData"
        :btnHide="true"
        :fromWorkPlace="fromWorkPlace"
        @fullscreenChange="dialogFullScreen=!dialogFullScreen;"
        @closeHandle="infoClose"
        @close="closeHandle" />
    </el-dialog>
  </div>
</template>
<script>
import { queryCCList } from "../api/flowable"
import infoForm from './info'
export default {
  name: 'task-list',
  components: { infoForm },
  props: {
    fromWorkPlace: {
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
          // {
          //   label: '创建时间',
          //   prop: 'createTime',
          //   datetype: 'datetime',
          //   type: "datePicker",
          //   format: "yyyy-MM-dd hh:mm:ss",
          //   valueFormat: "yyyy-MM-dd hh:mm:ss",
          // }
          {
            label: '抄送时间',
            prop: 'carbonCopyTime',
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
      infoVisible: false,
      isMounted: false,
      dialogFullScreen: false
    }
  },
  methods: {
    // 获取数据
    getList (page) {
      let query={
        current: this.page.currentPage,
        size: this.page.pageSize
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
      queryCCList(temp).then(res => {
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
    }
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
