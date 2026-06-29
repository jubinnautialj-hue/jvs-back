<template>
  <div class="jvs-log">
    <jvs-table
      :option="option"
      :data="tableData"
      :formData="queryParam"
      :page="page"
      :loading="tableLoading"
      pageheadertitle="应用操作日志"
      @on-load="getList"
      @search-change="searchChange"
    >
      <template slot="userName" slot-scope="scope">
        <el-tag v-if="scope.row.userName" size="mini">{{ scope.row.userName }}</el-tag>
      </template>
      <template slot="roles" slot-scope="scope">
        <span>{{scope.row.roles ? scope.row.roles.join(',') : ''}}</span>
      </template>
      <template slot="menu" slot-scope="scope">
        <el-button type="text" size="mini" @click="viewHandle(scope.row)">查看</el-button>
      </template>
    </jvs-table>

    <el-dialog
      class="oprate-log-info-dialog"
      :title="dialogTitle"
      fullscreen
      :visible.sync="dialogVisible"
      :before-close="handleClose">
      <div v-if="dialogVisible" class="oprate-log-info">
        <div class="topForm-box">
          <div class="topForm">
            <jvs-form class="show-form" :option="topFormOption" :formData="rowData"></jvs-form>
          </div>
        </div>
        <div v-if="rowData.beforeJson" class="bottomForm-box">
          <div class="form-content">
            <h4>变更前数据</h4>
            <div :class="{'form-info': true, 'loading': formLoading}">
              <jvs-form :option="bottomFormOption" :formData="rowData.beforeJson"></jvs-form>
            </div>
          </div>
          <div v-if="rowData.afterJson" class="spread-bar"></div>
          <div v-if="rowData.afterJson" class="form-content">
            <h4>变更后数据</h4>
            <div :class="{'form-info': true, 'loading': formLoading}">
              <jvs-form :option="bottomFormOption" :formData="rowData.afterJson"></jvs-form>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {jvsLogPage, jvsLogDetail} from "@/api/application";
export default {
  name: "appOperationLog",
  data () {
    return {
      option: {
        addBtn: false,
        page: true,
        menu: true,
        editBtn: false,
        delBtn: false,
        viewBtn: false,
        search: true,
        showOverflow: true,
        column: [
          {
            label: '姓名',
            prop: 'userName',
            slot: true
          },
          {
            label: '部门',
            prop: 'deptName',
          },
          {
            label: '角色',
            prop: 'roles',
            slot: true,
            showOverflow: true
          },
          {
            label: '账号',
            prop: 'account',
          },
          {
            label: '应用名',
            prop: 'jvsAppName',
            search: true,
          },
          {
            label: '模型名',
            prop: 'modelName',
            search: true,
          },
          {
            label: '操作类型',
            prop: 'type',
          },
          {
            label: '按钮名称',
            prop: 'buttonName',
          },
          {
            label: '时间',
            prop: 'createTime',
            datetype: 'datetime',
            format: "yyyy-MM-dd hh:mm:ss",
            valueFormat: "yyyy-MM-dd hh:mm:ss",
          }
        ]
      },
      queryParam: {},
      tableData: [],
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
      },
      tableLoading: false,
      rowData: null,
      dialogTitle: '',
      dialogVisible: false,
      topFormOption: {
        btnHide: true,
        disabled: true,
        formAlign: 'top',
        column: []
      },
      bottomFormOption: {
        btnHide: true,
        disabled: true,
        formAlign: 'top',
        column: []
      },
      formLoading: false
    }
  },
  created () {
    this.option.column.filter(col => {
      if(col.label) {
        col.label = this.$langt(`appOperationLog.column.${col.prop}.label`)
      }
    })
  },
  methods: {
    searchChange (form) {
      this.queryParam=form
      this.getList()
    },
    getList() {
      let obj={
        current: this.page.currentPage,
        size: this.page.pageSize
      }
      this.tableLoading = true
      jvsLogPage(Object.assign(obj, this.queryParam)).then(res => {
        if (res.data && res.data.code == 0) {
          this.tableData = [...res.data.data.records]
          this.page.total=res.data.data.total
          this.page.currentPage=res.data.data.current
          this.tableLoading = false
        }
      })
    },
    viewHandle (row) {
      this.topFormOption.column = JSON.parse(JSON.stringify(this.option.column))
      for(let i in this.topFormOption.column) {
        this.topFormOption.column[i].span = 4
        this.topFormOption.column[i].disabled = true
      }
      if(row.id) {
        jvsLogDetail(row.id).then(res => {
          if(res.data && res.data.code == 0) {
            if(res.data.data.log) {
              this.rowData = res.data.data.log
            }
            if(res.data.data.form) {
              this.getFormDesign(res.data.data.form)
            }
            this.dialogTitle = '日志详情'
            this.dialogVisible = true
          }
        })
      }
    },
    getFormDesign (form) {
      if(form.viewJson) {
        this.initForm(JSON.parse(form.viewJson))
      }
    },
    initForm (formDesign) {
      if(formDesign.formdata && formDesign.formdata.length > 0) {
        this.bottomFormOption.column = formDesign.formdata[0].forms
        this.bottomFormOption.column.filter(col => {
          col.disabled = true
        })
      }
      this.formLoading = false
    },
    handleClose () {
      this.dialogVisible = false
      this.rowData = null
    }
  }
}
</script>

<style lang="scss" scoped>
.jvs-log{
  border-radius: 6px;
  //margin: 10px;
  background-color: #ffffff;
  /deep/.jvs-table{
    .el-card__body{
      .table-top{
        padding: 0;
      }
    }
  }
}
.oprate-log-info-dialog{
  /deep/.el-dialog.is-fullscreen{
    border-radius: 0!important;
    .el-dialog__body{
      padding: 0!important;
    }
  }
}
.oprate-log-info{
  height: calc(100vh - 60px);
  overflow: hidden;
  background-color: #F5F5F5;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  /deep/.show-form{
    .el-form-item{
      padding: 0 20px;
    }
  }
  .topForm-box{
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #fff;
    .topForm{
      width: 80%;
    }
  }
  .bottomForm-box{
    display: flex;
    justify-content: space-between;
    margin-top: 10px;
    box-sizing: border-box;
    flex: 1;
    background-color: #fff;
    .spread-bar{
      width: 10px;
      background-color: #F5F5F5;
    }
    .form-content{
      padding: 20px;
      flex: 1;
      h4{
        height: 20px;
        line-height: 20px;
        font-size: 18px;
        color: #333333;
        margin: 0;
        padding: 0;
        display: flex;
        align-items: center;
        font-weight: normal;
      }
      h4::before{
        content: "";
        display: block;
        width: 4px;
        height: 20px;
        background: #3471FF;
        margin-right: 20px;
      }
      .form-info{
        margin-top: 10px;
        height: calc(100% - 30px);
        overflow: hidden;
        overflow-y: auto;
      }
      .loading{
        background-image: url('../../../public/jvs-ui-public/img/loading.gif');
        background-repeat: no-repeat;
        background-position: center;
      }
    }
  }
}
</style>
