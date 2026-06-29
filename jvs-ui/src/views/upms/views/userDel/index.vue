<template>
  <div class="user-del-page">
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
        <jvs-button size="mini" type='text' v-if="scope.row.cancelFlag" permisionFlag="jvs_delete_recovery_user" @click="disableHandle(scope.row)">{{$langt('userdel.recover')}}</jvs-button>
        <jvs-button size="mini" type='text' permisionFlag="jvs_delete_thorough_user" @click="deleteHandle(scope.row)"><span style="color: #F56C6C;">{{$langt('userdel.del')}}</span></jvs-button>
      </template>
      <template slot="headImg" slot-scope="scope">
        <img v-if="scope.row.headImg" :src="scope.row.headImg" alt="" style="display: inline-block;width: 40px;height: 40px;border-radius: 50%;overflow: hidden;">
      </template>
    </jvs-table>
  </div>
</template>
<script>
import { tableOption } from './option'
import {deleteUser, enableDisUser, getDeptUserList} from "@/views/upms/views/department/api";
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
      pageheadertitle: '删除用户', // fixed me  表名
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
    })
  },
  methods: {
    // 彻底删除用户
    deleteHandle(row) {
      this.$confirm(`<div>${this.$langt('userdel.comfirm1')}</div>
<p>* ${this.$langt('userdel.comfirm2')}</p>
<p>* ${this.$langt('userdel.comfirm3')}</p>
<p>* ${this.$langt('userdel.comfirm4')}</p>
`, this.$langt('common.tip'), {
        confirmButtonText: this.$langt('common.confirm'),
        cancelButtonText: this.$langt('common.cancel'),
        dangerouslyUseHTMLString: true,
        type: 'warning'
      }).then(() => {
        deleteUser(row.id).then(res => {
          if (res.data.code==0) {
            this.$notify({
              title: this.$langt('common.tip'),
              message: this.$langt('userdel.delSuccess'),
              position: 'bottom-right',
              type: 'success'
            });
            this.getList()
          }
        })
      }).catch(_ => {})
    },
    // 恢复用户
    disableHandle (row) {
      enableDisUser(row.userId, row.cancelFlag).then(res => {
        if (res.data.code==0) {
          this.$notify({
            title: this.$langt('common.tip'),
            message: this.$langt('userdel.recoverSuccess'),
            position: 'bottom-right',
            type: 'success'
          });
          this.getList()
        }
      })
      return
      let str = '恢复'
      // if(!row.cancelFlag) {
      //   str = '锁定'
      // }else{
      //   str = '解除锁定'
      // }
      let tips = '确定'+str+'该用户？'
      if(!row.cancelFlag) {
        tips = `锁定后，用户无法登录当前系统。确定锁定该用户？`
      }
      this.$confirm(tips, this.$langt('common.tip'), {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        enableDisUser(row.userId, row.cancelFlag).then(res => {
          if (res.data.code==0) {
            this.$notify({
              title: this.$langt('common.tip'),
              message: str + '用户成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.getList()
          }
        })
      }).catch(_ => {})
    },
    // 表格分页查询
    getList (page) {
      this.tableLoading = true
      let obj={
        size: this.page.pageSize,
        current: this.page.currentPage,
        cancelFlag: true
      }
      getDeptUserList( Object.assign(this.queryParams, obj), '').then(({ data }) => {
        this.tableLoading=false
        if(data.code == 0 && data.data) {
          this.tableData = data.data.records
          // console.log(this.tableData)
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
    // 关闭 弹窗
    handleClose() {
      this.formData = {
        appKeys:[],
        contentType: 'IMG',
        type: 'PC'
      }
      this.dialogVisible = false
    },
  }
}
</script>
<style lang="scss">
.user-del-page{
  .el-dialog.is-fullscreen{
    .el-dialog__body{
      padding: 0;
    }
  }
}
</style>
