<template>
  <div class="wx-message-temp-page">
    <platform-page-header title="公众号消息模板" :is-more="true" :desc="$langt('wxmessagetemplate.desc')"/>
    <jvs-table
      v-if="isConfig"
      :option="tableOption"
      :loading="tableLoading"
      :data="tableData"
      pageheadertitle='请求日志'
      @on-load="getList"
      @search-change="searchChange"
    >
      <template slot="menuLeft" slot-scope="scope">
        <jvs-button size="mini" type="primary" @click="handleAsync()">{{$langt('wxmessagetemplate.sysBtn')}}</jvs-button>
      </template>
    </jvs-table>
    <div v-else class="tips-massage">
      <el-alert :title="$langt('wxmessagetemplate.emptyDesc')" type="warning" :closable="false"></el-alert>
    </div>
  </div>
</template>
<script>
import {getWxMessageTempList} from "@/views/upms/views/wxMessageTemplate/api";
import PlatformPageHeader from "@/components/page-header/PlatformPageHeader";
import { getSysConfig } from '@/views/upms/views/inviteUser/api'
export default {
  name: 'wx-message-template',
  components: {PlatformPageHeader},
  data () {
    return {
      isConfig: false,
      tableLoading: false,
      tableData: [],
      tableOption: {
        showOverflow: true,
        addBtn: false,
        editBtn: false,
        viewBtn: false,
        delBtn: false,
        search: false,
        menu: false,
        page: false,
        cancal: false,
        column: [
          {
            label: '模板标题',
            prop: 'title',
          },
          {
            label: '一级行业',
            prop: 'primaryIndustry'
          },
          {
            label: '二级行业',
            prop: 'deputyIndustry',
          },
          {
            label: '模板内容',
            prop: 'content',
          },
          {
            label: '模板示例',
            prop: 'example',
          },
        ]
      },
    }
  },
  created() {
    this.tableOption.column.filter(col => {
      if(col.label) {
        col.label = this.$langt(`wxmessagetemplate.column.${col.prop}.label`)
      }
    })
    getSysConfig('WECHAT_OFFICIAL_ACCOUNT_CONFIGURATION').then(res => {
      if(res.data && res.data.code == 0 && res.data.data) {
        this.isConfig = true
      }
    })
  },
  methods: {
    getList () {
      this.tableLoading = true
      getWxMessageTempList().then(res => {
        if (res.data && res.data.code == 0) {
          this.tableData = res.data.data || []
          this.tableLoading = false
        }
      })
    },
    handleAsync() {
      const params = {
        new: true
      }
      this.tableLoading = true
      getWxMessageTempList(params).then(res => {
        if (res.data && res.data.code == 0) {
          this.tableData = res.data.data || []
          this.tableLoading = false
          this.$notify({
            title: this.$langt('common.tip'),
            message: this.$langt('common.sysSuccess'),
            position: 'bottom-right',
            type: 'success'
          })
        }
      })
    },
    searchChange (form) {
      this.queryParams = form
      this.getList()
    },
  }
}
</script>
<style lang="scss" scoped>
.wx-message-temp-page{
  background-color: #FFFFFF;
  /deep/.platform-page-header{
    padding: 0;
    padding-bottom: 15px;
    .title{
      display: none;
    }
  }
  /deep/.el-alert--info.is-light{
    width: 1270px;
  }
  /deep/.table-body-box{
    .el-table{
      .el-table__body-wrapper{
        height: calc(100vh - 190px)!important;
      }
    }
  }
  .header{
    padding: 20px 20px 0;
    .form-title{
      font-weight: 600;
      font-size: 18px;
      margin-bottom: 16px;
    }
    .header-desc{
      color: #a2a3a5;
      font-size: 14px;
    }
  }
  .tips-massage{
    padding: 20px;
    box-sizing: border-box;
    .el-alert{
      width: 1270px;
    }
  }
}
</style>
