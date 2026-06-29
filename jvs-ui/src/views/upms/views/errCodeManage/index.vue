<template>
  <div class="err-code-manage">
    <jvs-table
      :option="tableOption"
      :loading="tableLoading"
      :data="tableData"
      pageheadertitle='错误代码管理'
      :page="page"
      @on-load="getList"
      @search-change="searchChange"
      @delRow="delRowHandle"
      @addRow="addRowHandle"
      @editRow="editRowHandle"
    >
      <template slot="menuLeft">
        <jvs-button type="primary" permisionFlag="jvs_gateway_code_refresh" @click="handleRefresh()">{{$langt('table.freshCash')}}</jvs-button>
      </template>
      <template slot="menuRight">
        <div class="table-show-right-tool">
          <p @click="searchChange(queryParams)">
            <i class="el-icon-refresh-right"></i>
            <span>{{$langt('table.fresh')}}</span>
          </p>
        </div>
      </template>
    </jvs-table>
  </div>
</template>

<script>
import {addErrCode, delErrCode, editErrCode, getErrCodeList, refreshErrCodeList} from "./api";

export default {
  name: "index",
  data () {
    return {
      tableData: [],
      queryParams: {},
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
      },
      tableLoading: false,
      tableOption: {
        showOverflow: true,
        search: true,
        // menu: false,
        addBtn: this.$permissionMatch("jvs_gateway_code_add"),
        editBtn: this.$permissionMatch("jvs_gateway_code_edit"),
        viewBtn: this.$permissionMatch("jvs_gateway_code"),
        delBtn: this.$permissionMatch("jvs_gateway_code_delete"),
        cancal: false,
        page: true,
        submitLoading: false,
        column: [
          {
            label: 'code',
            prop: 'code',
          },
          {
            label: '错误信息',
            prop: 'msg',
          },
          {
            label: '备注',
            prop: 'remark',
          },
          {
            label: '匹配方式',
            prop: 'matchingMethod',
            type: 'select',
            dicData: [
              { label: '前匹配', value: 'PreMatch' },
              { label: '后匹配', value: 'PostMatch' },
              { label: '完全匹配', value: 'PerfectMatch' },
            ],
          },
        ]
      },
    }
  },
  created () {
    this.tableOption.column.filter(col => {
      if(col.label) {
        col.label = this.$langt(`gateway.column.${col.prop}.label`)
      }
    })
  },
  methods: {
    // 刷新
    handleRefresh() {
      refreshErrCodeList().then(res => {
        if (res.data && res.data.code == 0) {
          this.$notify({
            title: this.$langt('common.tip'),
            message: this.$langt('common.freshSuccess'),
            position: 'bottom-right',
            type: 'success'
          });
          this.getList()
        }
      })
    },
    // 编辑、查看
    editRowHandle(row) {
      editErrCode(row).then(res => {
        if (res.data && res.data.code == 0) {
          this.$notify({
            title: this.$langt('common.tip'),
            message: this.$langt('common.editSuccess'),
            position: 'bottom-right',
            type: 'success'
          });
          this.getList()
        }
      })
    },
    // 添加
    addRowHandle(form) {
      addErrCode(form).then(res => {
        if (res.data && res.data.code == 0) {
          this.$notify({
            title: this.$langt('common.tip'),
            message: this.$langt('common.addSuccess'),
            position: 'bottom-right',
            type: 'success'
          });
          this.getList()
        }
      })
    },
    // 删除
    delRowHandle (row) {
      delErrCode(row.id).then(res => {
        if (res.data && res.data.code == 0) {
          this.$notify({
            title: this.$langt('common.tip'),
            message: this.$langt('common.delSuccess') ,
            position: 'bottom-right',
            type: 'success'
          });
          this.getList()
        }
      })
    },
    searchChange (form) {
      this.queryParams = form
      this.getList()
    },
    getList (page) {
      let query={
        current: this.page.currentPage,
        size: this.page.pageSize
      }
      this.tableLoading = true
      let temp = JSON.parse(JSON.stringify(this.queryParams))
      getErrCodeList(Object.assign(query, temp)).then(res => {
        if (res.data.code==0) {
          this.page.total=res.data.data.total
          this.page.currentPage=res.data.data.current
          this.tableData=res.data.data.records
          this.tableLoading = false
        }
      })
    },
  }
}
</script>