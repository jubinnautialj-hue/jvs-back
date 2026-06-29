<template>
  <div class="user-check-page">
    <jvs-table
      :pageheadertitle='pageheadertitle'
      :option="option"
      :data="tableData"
      :loading="tableLoading"
      :page="page"
      @on-load="getList"
      @search-change="searchChange"
    >
      <template slot="menuRight">
        <el-row class="table-show-right-tool">
          <!-- 表格工具 -->
          <p class="search-p">
            <el-input :placeholder="$langt('table.keyword')" prefix-icon="el-icon-search" size="mini" v-model="queryParams.keywords" @keyup.enter.native="searchChange(queryParams)" @blur="searchChange(queryParams);" clearable @clear="searchChange(queryParams);"></el-input>
          </p>
          <p @click="searchChange(queryParams)" style="font-size: 14px;">
            <i class="el-icon-refresh-right" style="cursor:pointer;"></i>
            <span>{{$langt('table.fresh')}}</span>
          </p>
        </el-row>
      </template>
      <template slot="menu" slot-scope="scope">
        <jvs-button size="mini" type='text' permisionFlag="" @click="handlePass(scope.row, true)">{{$langt('common.pass')}}</jvs-button>
        <jvs-button size="mini" type='text' permisionFlag="" @click="handlePass(scope.row, false)">{{$langt('common.nopass')}}</jvs-button>
      </template>
      <template slot="headImg" slot-scope="scope">
        <img v-if="scope.row.headImg" :src="scope.row.headImg" alt="" style="display: inline-block;width: 40px;height: 40px;border-radius: 50%;overflow: hidden;">
      </template>
    </jvs-table>
  </div>
</template>
<script>
import { tableOption } from './option'
import {enableDisUser, getDeptUserList} from "@/views/upms/views/department/api";
import {checkHandle, getInviteUserPage} from "./api";
export default {
  components: {},
  props: {
    propData: {
      type: Object
    }
  },
  data () {
    return {
      tableLoading: false, // loading显示
      tableData: [], // 列表数据
      pageheadertitle: '用户审核', // fixed me  表名
      option: tableOption,
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
      },
      queryParams: {}, // 查询条件
    }
  },
  watch: {
  },
  created () {
    this.option.column.filter(col => {
      if(col.label) {
        col.label = this.$langt(`department.userColumn.${col.prop}.label`)
      }
      if(col.rules && col.rules.length > 0) {
        col.rules[0].message = this.$langt(`department.userColumn.${col.prop}.placeholder`)
        if(col.rules.length > 1) {
          col.rules[1].message = this.$langt(`department.userColumn.${col.prop}.rule`)
        }
      }
      if(col.dicData && col.dicData.length > 0) {
        col.dicData.filter(dit => {
          dit.label = this.$langt(`department.userColumn.${col.prop}.dicData.${dit.value}`)
        })
      }
    })
  },
  methods: {
    // 禁用 启用
    handlePass (row, bool) {
      checkHandle(row.id, bool).then(res => {
        this.$notify({
          title: this.$langt('common.tip'),
          message: this.$langt('common.oprateSuccess'),
          position: 'bottom-right',
          type: 'success'
        });
        this.getList()
      })
    },
    // 表格分页查询
    getList (page) {
      this.tableLoading = true
      let obj={
        size: this.page.pageSize,
        current: this.page.currentPage,
        cancelFlag: true
      }
      getInviteUserPage(Object.assign(this.queryParams, obj)).then(({ data }) => {
        this.tableLoading=false
        if(data.code == 0 && data.data) {
          this.tableData = data.data.records
          this.page.currentPage = data.data.current
          this.page.total = data.data.total
        }
      }).catch(err => {
        this.tableLoading=false
      })
    },
    // 条件查询
    searchChange (form) {
      this.queryParams=form
      this.getList()
    },
  }
}
</script>
<style lang="scss">
.user-check-page{
  .el-dialog.is-fullscreen{
    .el-dialog__body{
      padding: 0;
    }
  }
}
</style>
