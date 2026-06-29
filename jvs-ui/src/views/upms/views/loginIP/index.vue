<template>
  <div class="err-code-manage">
    <jvs-table
      :option="tableOption"
      :loading="tableLoading"
      :data="tableData"
      pageheadertitle='网关地址忽略'
      :page="page"
      @on-load="getList"
      @search-change="searchChange"
      @delRow="delRowHandle"
      @addRow="addRowHandle"
      @editRow="editRowHandle"
    >
      <template slot="menuRight">
        <div class="table-show-right-tool">
          <p @click="searchChange(queryParams)">
            <i class="el-icon-refresh-right"></i>
            <span>{{$langt('table.fresh')}}</span>
          </p>
        </div>
      </template>
      <template slot="status" slot-scope="scope">
        <span>{{scope.row.status ? '启用' : '禁用'}}</span>
      </template>
    </jvs-table>
  </div>
</template>

<script>
import { add, del, edit, getPageList, } from "./api";

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
        dialogWidth: '50%',
        showOverflow: true,
        search: true,
        // menu: false,
        addBtn: this.$permissionMatch("jvs_login_rules"),
        viewBtn: false,
        editBtn: this.$permissionMatch("jvs_login_rules"),
        delBtn: this.$permissionMatch("jvs_login_rules"),
        cancal: false,
        page: true,
        submitLoading: false,
        column: [
          {
            label: 'ip地址',
            prop: 'ip',
          },
          {
            label: '时间段规则',
            prop: 'time',
            type: 'timePicker',
            isrange: true
          },
          {
            label: '状态',
            prop: 'status',
            type: 'switch',
            slot: true
          },
        ]
      },
    }
  },
  created () {
    this.tableOption.column.filter(col => {
      if(col.label) {
        col.label = this.$langt(`loginIP.column.${col.prop}.label`)
      }
    })
  },
  methods: {
    // 编辑、查看
    editRowHandle(row) {
      edit(row).then(res => {
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
      add(form).then(res => {
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
      del(row.id).then(res => {
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
      getPageList(Object.assign(query, temp)).then(res => {
        if(res.data.code == 0) {
          this.page.total=res.data.data.total
          this.page.currentPage=res.data.data.current
          this.tableData=res.data.data.records
        }
        this.tableLoading = false
      }).catch(e => {
        this.tableLoading = false
      })
    },
  }
}
</script>