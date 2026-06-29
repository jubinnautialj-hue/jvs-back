<template>
  <div class="err-code-manage">
    <jvs-table
      :option="tableOption"
      :loading="tableLoading"
      :data="tableData"
      pageheadertitle='网关路由表'
      :page="page"
      @on-load="getList"
      @search-change="searchChange"
      @delRow="delRowHandle"
      @addRow="addRowHandle"
      @editRow="editRowHandle"
    >
      <template slot="menuLeft">
        <jvs-button type="primary" permisionFlag="jvs_gateway_route_refresh" @click="handleRefresh()">{{$langt('table.freshCash')}}</jvs-button>
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
import {addGatewayRoute, delGatewayRoute, editGatewayRoute, getGatewayRouteList, refreshGatewayRouteList} from "./api";

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
        addBtn: this.$permissionMatch("jvs_gateway_route_add"),
        editBtn: this.$permissionMatch("jvs_gateway_route_edit"),
        viewBtn: this.$permissionMatch(""),
        delBtn: this.$permissionMatch("jvs_gateway_route_delete"),
        cancal: false,
        page: true,
        submitLoading: false,
        column: [
          {
            label: 'id',
            prop: 'id',
            rules: [{ required: true, message: '请输入id', trigger: 'blur' },]
          },
          {
            label: 'path',
            prop: 'path',
            rules: [{ required: true, message: '请输入path', trigger: 'blur' },]
          },
          {
            label: '备注',
            prop: 'remark',
            rules: [{ required: true, message: '请输入备注', trigger: 'blur' },]
          },
          {
            label: '转发',
            prop: 'uri',
            rules: [{ required: true, message: '请输入转发', trigger: 'blur' },]
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
      if(col.rules && col.rules.length > 0) {
        col.rules[0].message = this.$langt(`gateway.column.${col.prop}.placeholder`)
      }
    })
  },
  methods: {
    // 刷新
    handleRefresh() {
      refreshGatewayRouteList().then(res => {
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
      editGatewayRoute(row).then(res => {
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
      addGatewayRoute(form).then(res => {
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
      delGatewayRoute(row.id).then(res => {
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
      getGatewayRouteList(Object.assign(query, temp)).then(res => {
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