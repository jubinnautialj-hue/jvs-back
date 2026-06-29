<template>
  <div class="short-message-temp-page">
    <platform-page-header title="短信消息模板" :is-more="true" :desc="$langt('shortmessagetemplate.desc')"/>
    <jvs-table
      :option="tableOption"
      :loading="tableLoading"
      :data="tableData"
      pageheadertitle='请求日志'
      @on-load="getList"
      @search-change="searchChange"
    >
      <template slot="menuLeft" slot-scope="scope">
          <jvs-button size="mini" type="primary" @click="handleAsync()">{{$langt('shortmessagetemplate.sysBtn')}}</jvs-button>
      </template>
      <template slot="menu" slot-scope="scope">
        <jvs-button type="text" size="mini" v-if="scope.row.auditStatus === 'AUDIT_STATE_PASS'" @click="handleSetBand(scope.row)">{{$langt('shortmessagetemplate.setBind')}}</jvs-button>
      </template>
      <template slot="auditStatus" slot-scope="scope">
        <span>{{scope.row.auditStatus === 'AUDIT_STATE_PASS' ? $langt('shortmessagetemplate.can') : $langt('shortmessagetemplate.no')}}</span>
      </template>
      <template slot="loginCode" slot-scope="scope">
        <span>{{scope.row.loginCode ? $langt('shortmessagetemplate.yes') : '-'}}</span>
      </template>
    </jvs-table>
  </div>
</template>
<script>
import {getShortMessageTempList, setBand} from "@/views/upms/views/shortMessageTemplate/api";
import PlatformPageHeader from "@/components/page-header/PlatformPageHeader";

export default {
  name: 'short-message-template',
  components: {PlatformPageHeader},
  data () {
    return {
      tableLoading: false,
      tableData: [],
      tableOption: {
        showOverflow: true,
        addBtn: false,
        editBtn: false,
        viewBtn: false,
        delBtn: false,
        search: false,
        // menu: false,
        page: false,
        cancal: false,
        column: [
          {
            label: '模板名称',
            prop: 'templateName',
          },
          {
            label: '状态',
            prop: 'auditStatus',
            slot: true
          },
          {
            label: '验证码模板',
            prop: 'loginCode',
            slot: true
          },
          {
            label: '工单ID',
            prop: 'orderId',
          },
          {
            label: '短信模板CODE',
            prop: 'templateCode',
          },
          {
            label: '模板内容',
            prop: 'templateContent'
          },
          {
            label: '审核备注',
            prop: 'reason',
          },
          {
            label: '创建时间',
            prop: 'createDate',
            type: 'datePicker',
            datetype: 'datetime',
            format: "yyyy-MM-dd HH:mm:ss",
            valueFormat: "yyyy-MM-dd HH:mm:ss",
          },
        ]
      },
    }
  },
  created() {
    this.tableOption.column.filter(col => {
      if(col.label) {
        col.label = this.$langt(`shortmessagetemplate.column.${col.prop}.label`)
      }
    })
  },
  methods: {
    getList () {
      this.tableLoading = true
      const params = {
        size: 20,
        current: 1,
      }
      getShortMessageTempList(params).then(res => {
        if (res.data && res.data.code == 0) {
          this.tableData = res.data.data || []
        }
      }).finally(res => {
        this.tableLoading = false
      })
    },
    // 设置为绑定模板
    handleSetBand(row) {
      const params = {
        templateCode: row.templateCode
      }
      setBand(params).then(res => {
        if (res.data && res.data.code == 0) {
          this.$notify({
            title: this.$langt('common.tip'),
            message: this.$langt('common.setSuccess'),
            position: 'bottom-right',
            type: 'success'
          });
          this.getList()
        }
      })
    },
    handleAsync() {
      this.tableLoading = true
      const params = {
        new: true
      }
      getShortMessageTempList(params).then(res => {
        if (res.data && res.data.code == 0) {
          this.tableData = res.data.data || []
          this.$notify({
            title: this.$langt('common.tip'),
            message: this.$langt('common.sysSuccess'),
            position: 'bottom-right',
            type: 'success'
          })
        }
      }).finally(res => {
        this.tableLoading = false
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
.short-message-temp-page{
  background-color: #FFFFFF;
  /deep/.platform-page-header{
    padding: 0;
    padding-bottom: 15px;
    .title{
      display: none;
    }
  }
  /deep/.el-alert--info.is-light{
    width: 790px;
  }
  /deep/.table-body-box{
    .el-table{
      .el-table__body-wrapper{
        height: calc(100vh - 232px)!important;
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
}
</style>
