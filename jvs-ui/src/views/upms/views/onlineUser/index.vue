<template>
  <div class="app-manage-box">
    <div class="app-manage">
      <jvs-table
        pageheadertitle='在线用户'
        :index="false"
        :option="option"
        :page="page"
        :data="tableData"
        :loading="tableLoading"
        @on-load="getList"
        @search-change="getList"
      >
        <template slot="menu" slot-scope="scope">
          <jvs-button type="text" size="mini" @click="handleView(scope.row)">{{$langt('table.view')}}</jvs-button>
          <jvs-button v-if="permissionsList.indexOf('jvs_session_close') > -1" type="text" size="mini" @click="outUSerHandle(scope.row)"><span style="color: #F56C6C;">{{$langt('onlineUser.endSession')}}</span></jvs-button>
        </template>
        <template slot="headImg" slot-scope="scope">
          <img v-if="scope.row && scope.row.headImg" :src="scope.row.headImg" style="display:inline-block;width:40px;height:40px;border-radius:50%;overflow:hidden;">
        </template>
        <template slot="expiresAt" slot-scope="scope">
          <span v-if="scope.row && scope.row.expiresAt" >{{getFormatTime(scope.row.expiresAt)}}</span>
        </template>
      </jvs-table>
    </div>
    <el-dialog
      :title="$langt('table.info')"
      :visible.sync="dialogVisible"
      :close-on-click-modal="false"
      :before-close="handleClose">
      <el-form v-if="formData" :label-width="is_zh ? '80px' : '140px'" :model="formData">
        <el-form-item :label="$langt('onlineUser.column.headImg.label')">
          <img v-if="formData.headImg" :src="formData.headImg" style="display:inline-block;width:40px;height:40px;border-radius:50%;overflow:hidden;" alt="">
        </el-form-item>
        <el-form-item :label="$langt('onlineUser.column.accountName.label')">
          {{formData.accountName}}
        </el-form-item>
        <el-form-item :label="$langt('onlineUser.column.realName.label')">
          {{formData.realName}}
        </el-form-item>
        <el-form-item :label="$langt('onlineUser.column.phone.label')">
          {{formData.phone}}
        </el-form-item>
        <el-form-item :label="$langt('onlineUser.column.loginType.label')">
          {{formData.loginType}}
        </el-form-item>
        <el-form-item :label="$langt('onlineUser.column.userAgent.label')">
          {{formData.userAgent}}
        </el-form-item>
        <el-form-item :label="$langt('onlineUser.column.jobName.label')">
          {{formData.jobName}}
        </el-form-item>
        <el-form-item :label="$langt('onlineUser.column.deptName.label')">
          {{formData.deptName}}
        </el-form-item>
        <el-form-item :label="$langt('onlineUser.column.employeeNo.label')">
          {{formData.employeeNo}}
        </el-form-item>
        <el-form-item :label="$langt('onlineUser.column.expiresAtEnd.label')">
          {{(formData.expiresAt) ? getFormatTime(formData.expiresAt) : ''}}
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button size="mini" @click="dialogVisible = false">{{$langt('common.cancel')}}</el-button>
        <el-button size="mini" type="primary" @click="dialogVisible = false">{{$langt('common.confirm')}}</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import moment from 'moment'
import { tableOption } from './option'
import { getOnlineUserList, outUser} from './api'
import { getStore } from "@/util/store.js";
export default {
  name: 'app-manage',
  components: {},
  data () {
    return {
      formData: {},
      dialogVisible: false,
      queryParams: {},
      option: tableOption,
      tableData: [],
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
      },
      tableLoading: false,
      permissionsList: [],
      is_zh: false
    }
  },
  created () {
    let language = navigator.language || navigator.userLanguage;
    if(/zh/i.test(language) || /zh-CN/i.test(language) || /zh-TW/i.test(language) || /zh-HK/i.test(language)) {
      this.is_zh = true
    }
    this.option.column.filter(col => {
      if(col.label) {
        col.label = this.$langt(`onlineUser.column.${col.prop}.label`)
      }
    })
    this.permissionsList = getStore({name: 'permissions'})
  },
  methods: {
    // 查看数据 弹窗关闭
    handleClose() {
      this.dialogVisible = false
    },
    // 获取数据
    getList (page) {
      let query = {
        current: this.page.currentPage,
        size: this.page.pageSize
      }
      this.tableLoading = true
      getOnlineUserList(query).then(res => {
        if (res.data.code == 0) {
          this.page.total=res.data.data.total
          this.page.currentPage=res.data.data.current
          this.tableData=res.data.data.records
          this.tableLoading = false
        }
      })
    },
    // 获取时间
    getFormatTime(time) {
      return moment(time).format('YYYY-MM-DD HH:mm:ss')
    },
    // 查看详情
    handleView(obj) {
      this.formData = JSON.parse(JSON.stringify(obj))
      this.dialogVisible = true
    },
    // 强制退出
    outUSerHandle (row) {
      this.$confirm(this.$langt('onlineUser.outConfirm'), this.$langt('common.tip'), {
        confirmButtonText: this.$langt('common.confirm'),
        cancelButtonText: this.$langt('common.cancel'),
        type: 'warning'
      }).then(() => {
        outUser(row.jvs).then(res => {
          if(res.data.code == 0) {
            this.$notify({
              title: this.$langt('common.tip'),
              message: this.$langt('onlineUser.outSuccess'),
              position: 'bottom-right',
              type: 'success'
            });
            this.getList()
          }
        })
      }).catch(e => {})
    }
  }
}
</script>
<style lang="scss" scoped>
.base-type-list{
  padding: 0;
  margin: 0;
  li{
    display: flex;
    align-items: center;
    margin: 0;
    //margin-bottom: 10px;
    height: 32px;
    line-height: 32px;
    cursor: pointer;
    padding: 6px 24px;
    i{
      margin-right: 10px;
      font-size: 14px!important;
    }
  }
  li:hover{
    background: #F5F7FA;
  }
  li:nth-last-of-type(1) {
    margin-bottom: 0;
  }
}
</style>
<style lang="scss">
.app-manage-box {
  position: relative;
  height: 100%;
}
</style>
