<template>
  <div class="message-manage">

    <jvs-table
      pageheadertitle='消息管理'
      :option="option"
      :data="tableData"
      :page="page"
      :loading="tableLoading"
      @on-load="getList"
      @search-change="searchHandle"
    >
      <template slot="menuLeft">
        <jvs-button v-if="$permissionMatch('jvs_message_sms') && configArr.indexOf('SMS') > -1" type="primary" size="mini" @click="addMessage($langt('message.smsAdd'), 'sms')" icon="el-icon-plus">{{$langt('message.sms')}}</jvs-button>
        <jvs-button v-if="$permissionMatch('jvs_message_email') && configArr.indexOf('EMAIL') > -1" type="primary" size="mini" @click="addMessage($langt('message.emailAdd'), 'email')" icon="el-icon-plus">{{$langt('message.email')}}</jvs-button>
        <jvs-button v-if="$permissionMatch('jvs_message_insede')" type="primary" size="mini" @click="addMessage($langt('message.interiorAdd'), 'interior')" icon="el-icon-plus">{{$langt('message.interior')}}</jvs-button>
        <jvs-button v-if="$permissionMatch('jvs_message_wx_mp') && configArr.indexOf('WECHAT_MP_MESSAGE') > -1" type="primary" size="mini" @click="addMessage($langt('message.WECHAT_MP_MESSAGEAdd'), 'WECHAT_MP_MESSAGE')" icon="el-icon-plus">{{$langt('message.WECHAT_MP_MESSAGE')}}</jvs-button>
        <jvs-button v-if="$permissionMatch('jvs_message_wx_enterprise') && configArr.indexOf('WX_ENTERPRISE') > -1" type="primary" size="mini" @click="addMessage($langt('message.WX_ENTERPRISEAdd'), 'WX_ENTERPRISE')" icon="el-icon-plus">{{$langt('message.WX_ENTERPRISE')}}</jvs-button>
        <jvs-button v-if="$permissionMatch('jvs_message_dingding') && configArr.indexOf('DING_H5') > -1" type="primary" size="mini" @click="addMessage($langt('message.DING_H5Add'), 'DING_H5')" icon="el-icon-plus">{{$langt('message.DING_H5')}}</jvs-button>
      </template>
      <template slot="menu" slot-scope="scope">
        <jvs-button type="text" size="mini" v-if="$permissionMatch('jvs_message_detail')" @click="editHandle(scope.row, 'view')">{{$langt('table.view')}}</jvs-button>
        <jvs-button type="text" size="mini" v-if="$permissionMatch('jvs_message_repeat') && scope.row.status === 1" :disabled="retryDisable" @click="retryMessageHandle(scope.row)">{{$langt('message.resend')}}</jvs-button>
        <jvs-button type="text" size="mini" v-if="$permissionMatch('jvs_message_edit') && [0, 2].indexOf(scope.row.status) > -1" @click="editHandle(scope.row, 'edit')">{{$langt('table.edit')}}</jvs-button>
        <jvs-button type="text" size="mini" v-if="$permissionMatch('jvs_message_send') && [0, 2].indexOf(scope.row.status) > -1" :disabled="retryDisable" @click="sendMessageHandle(scope.row, scope.index)">{{$langt('message.send')}}</jvs-button>
        <jvs-button type="text" size="mini" v-if="$permissionMatch('jvs_message_delete') && [0, 2].indexOf(scope.row.status) > -1" @click="deleteMessageHandle(scope.row)">{{$langt('table.delete')}}</jvs-button>
      </template>
      <template slot="recipients" slot-scope="scope">
        <span>
          <el-tag v-for="(item, index) in scope.row.recipients" :key="item+index" v-show="item.name">{{item.name}}</el-tag>
        </span>
      </template>
      <template slot="status" slot-scope="scope">
        <span :style="scope.row.status == 0 ? 'color: #F56C6C;' : (scope.row.status === 1 ? 'color: #67C23A;' : 'color: #E6A23C;')">
          {{scope.row.status == 0 ? $langt('table.fail') : (scope.row.status === 1 ? $langt('common.success') : (scope.row.status === 2 ? $langt('message.notSend') : $langt('message.sending')))}}
        </span>
      </template>
      <template slot="sendMessageType" slot-scope="scope">
        <span :style="'color:'+getValByAttrOfDic(scope.row.sendMessageType, 'value', 'color')">{{$langt(`message.column.sendMessageType.dicData[${scope.row.sendMessageType}]`)}}</span>
      </template>
    </jvs-table>
    <el-dialog
      v-if="dialogVisible"
      :visible.sync="dialogVisible"
      :before-close="handleClose"
      append-to-body
      fullscreen
      :title="title"
      class="body-zero-padding"
      :close-on-click-modal="false"
    >
      <sms ref="sms" v-if="dialogType === 'sms'" :isAdd="isAdd" :isView="isView" :rowData="rowData" @editSubmit="editSubmit"/>
      <we-chat-mp-message v-if="dialogType === 'WECHAT_MP_MESSAGE'" :isAdd="isAdd" :isView="isView" :rowData="rowData" @editSubmit="editSubmit"/>
      <email v-if="dialogType === 'email'" :isAdd="isAdd" :isView="isView" :rowData="rowData" @editSubmit="editSubmit"/>
      <interior v-if="dialogType === 'interior'" :isAdd="isAdd" :isView="isView" :rowData="rowData" @editSubmit="editSubmit"/>
      <wx-enterprise v-if="dialogType === 'WX_ENTERPRISE'" :isAdd="isAdd" :isView="isView" :rowData="rowData" @editSubmit="editSubmit"/>
      <ding v-if="dialogType === 'DING_H5'" :isAdd="isAdd" :isView="isView" :rowData="rowData" @editSubmit="editSubmit"/>
      <jvs-form v-if="false" ref="messInfoForm" :option="searchOption" :formData="rowData" @submit="editSubmit">
        <!-- 联系人 -->
        <template slot="recipientsForm">
          <!-- 用户组件 -->
          <userForm :sendType="rowData.sendType" :defaultValue="rowData.recipients" @userSelectChange="userSelectChange"/>
        </template>
        <!-- 内容 -->
        <template slot="contentForm">
          <!-- 短信 -->
          <el-input
            v-if="rowData && rowData.sendType == 'sms'"
            type="textarea"
            :rows="2"
            :placeholder="$langt('message.column.smsContent.placeholder')"
            v-model="rowData.content">
          </el-input>
          <!-- 邮件、站内信 -->
          <div v-if="rowData && rowData.sendType !== 'sms'">
            <htmlContent ref="htmlDesigner" :editorProp="editor" :searchOption="searchOption" :rowData="rowData" @clear="clear" @validate="validate" />
            <div v-if="contentValidate" class="el-form-item__error">{{$langt('message.column.content.placeholder')}}</div>
          </div>
        </template>
        <template slot="formButton">
          <jvs-button size="mini" type="primary" v-if="rowData && rowData.id" :loading="sendLoading" @click="saveSend">{{$langt('message.saveSend')}}</jvs-button>
          <jvs-button size="mini" @click="handleClose">{{$langt('form.cancel')}}</jvs-button>
        </template>
      </jvs-form>
    </el-dialog>
  </div>
</template>
<script>
import { tableOption, messFormOption } from './option'
import sms from './components/sms'
import interior from './components/interior'
import weChatMpMessage from './components/wechatMpMessage'
import {getMessage, addMessage, editMessage, sendMessage, retryMessage, deleteMessage} from '../../api/message'
import userForm from './userForm'
import htmlContent from './html'
import Email from "./components/email";
import WxEnterprise from "@/views/upms/views/message/components/wxEnterprise";
import Ding from "@/views/upms/views/message/components/ding";
import {getTenantConfig} from "@/api/common";
export default {
  name: 'message-manage',
  components: {Ding, WxEnterprise, Email, userForm, htmlContent, sms, weChatMpMessage, interior},
  data () {
    return {
      isAdd: false,
      isView: false,
      // 查询条件
      queryParams: {
        tid: '',
        functionName: '',
        userName: '',
        timerange: [],
        ip: '',
        content: '',
        api: ''
      },
      tableData: [], // 表格数据
      tableLoading: false,
      option: tableOption, // 表格配置
      searchOption: messFormOption, // 查询表单配置
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
      },
      rowData: {}, // 行数据
      viewData: [], // 查看数据
      dialogVisible: false,
      dialogType: '',
      reverse: true,
      dataViewList: [],
      dataViewLoading: false,
      viewDataLoading: false,
      currentNode: {
        id: ''
      }, // 当前节点
      title: '',
      editor: null, // 富文本
      contentValidate: false,
      retryDisable: false, // 发送禁用
      subForm: null,
      sendLoading: false,
      configArr: [], // 消息类型配置
    }
  },
  created() {
    this.option.column.filter(col => {
      if(col.label) {
        col.label = this.$langt(`message.column.${col.prop}.label`)
      }
      if(col.rules && col.rules.length > 0) {
        col.rules[0].message = this.$langt(`message.column.${col.prop}.placeholder`)
      }
      if(col.dicData && col.dicData.length > 0) {
        col.dicData.filter(dit => {
          dit.label = this.$langt(`message.column.${col.prop}.dicData.${dit.value}`)
        })
      }
    })
    this.searchOption.column.filter(col => {
      if(col.label) {
        col.label = this.$langt(`message.column.${col.prop}.label`)
      }
      if(col.rules && col.rules.length > 0) {
        col.rules[0].message = this.$langt(`message.column.${col.prop}.placeholder`)
      }
      if(col.dicData && col.dicData.length > 0) {
        col.dicData.filter(dit => {
          dit.label = this.$langt(`message.column.${col.prop}.dicData.${dit.value}`)
        })
      }
    })

    this.getTenantConfig()
  },
  methods: {
    // 获取消息类型配置
    getTenantConfig() {
      getTenantConfig().then(res => {
        if (res.data && res.data.code == 0) {
          this.configArr = res.data.data || []
        }
      })
    },
    // 新增
    addMessage (title, type) {
      this.isAdd = true
      this.isView = false
      this.title = title
      this.dialogVisible = true
      this.dialogType = type
    },
    //   获取数据
    getList (page) {
      let query={
        current: this.page.currentPage,
        size: this.page.pageSize
      }
      this.tableLoading = true
      let temp = JSON.parse(JSON.stringify(this.queryParams))
      temp = Object.assign(temp, query)
      getMessage(temp).then(res => {
        if (res.data.code==0) {
          this.tableData=res.data.data.records
          for(let i in this.tableData) {
            // if(this.tableData[i].recipients) {
            //   this.tableData[i].recipients = JSON.parse(this.tableData[i].recipients)
            // }
          }
          this.tableLoading=false
          this.page.total=res.data.data.total
          this.page.currentPage=res.data.data.current
        }
      })
    },
    // 编辑
    editHandle (row, type) {
      this.isAdd = false
      if(type == 'view') {
        this.title = this.$langt('table.view')
        // this.searchOption.disabled = true
        this.isView = true
      }
      if(type == 'edit') {
        this.title = this.$langt('table.edit')
        this.isView = false
        // this.searchOption.disabled = false
      }
      this.rowData = JSON.parse(JSON.stringify(row))
      this.dialogType = row.sendType
      this.dialogVisible = true
    },
    // 提交编辑
    editSubmit (form, type) {
      if (this.isAdd) {
        addMessage(form).then(res => {
          if(res.data.code == 0) {
            this.$notify({
              title: this.$langt('common.tip'),
              message: this.$langt('common.addSuccess'),
              position: 'bottom-right',
              type: 'success'
            });
            this.getList()
            this.handleClose()
          }
        }).catch(e => {
        })
      } else {
        editMessage(form).then(res => {
          if(res.data.code == 0) {
            this.$notify({
              title: this.$langt('common.tip'),
              message: this.$langt('common.editSuccess'),
              position: 'bottom-right',
              type: 'success'
            });
            this.getList()
            if(type == 'send') {
              this.sendMessageHandle(form)
            }else{
              this.handleClose()
            }
          }
        }).catch(e => {
          this.sendLoading = false
        })
      }
    },
    // 保存并发送
    saveSend () {
      this.sendLoading = true
      this.editSubmit(this.rowData, 'send')
    },
    // 关闭弹框
    handleClose () {
      this.dialogVisible=false
      this.subForm = null
      this.searchOption.submitLoading = false
      this.sendLoading = false
      this.$forceUpdate()
    },
    // 搜索
    searchHandle (form) {
      this.queryParams = form
      this.getList()
    },
    // 发送
    sendMessageHandle (row, index) {
      // this.$set(this.tableData[index], 'status', true)
      // this.$forceUpdate()
      this.retryDisable = true
      if(!row.id) {
        row.id = this.rowData.id
      }
      sendMessage(row.id).then(res => {
        if(res.data.code == 0) {
          this.$notify({
            title: this.$langt('common.tip'),
            message: this.$langt('message.sendimgMess'),
            position: 'bottom-right',
            type: 'success'
          });
          this.retryDisable = false
          this.getList()
          this.sendLoading = false
          this.handleClose()
          this.$forceUpdate()
        }
      }).catch(e => {
        this.retryDisable = false
        this.sendLoading = false
      })
    },
    // 重发
    retryMessageHandle (row) {
      this.retryDisable = true
      retryMessage(row.id).then(res => {
        if(res.data.code == 0) {
          this.$notify({
            title: this.$langt('common.tip'),
            message: this.$langt('message.resendMess'),
            position: 'bottom-right',
            type: 'success'
          });
          this.getList()
          this.retryDisable = false
        }
      }).catch(e => {
        this.retryDisable = false
      })
    },
    // 删除
    deleteMessageHandle (row) {
      this.$confirm(this.$langt('message.delConfirm'), this.$langt('common.tip'), {
        confirmButtonText: this.$langt('common.confirm'),
        cancelButtonText: this.$langt('common.cancel'),
        type: 'warning'
      }).then(() => {
        deleteMessage(row.id).then(res => {
          if(res.data.code == 0) {
            this.$notify({
              title: this.$langt('common.tip'),
              message: this.$langt('common.delSuccess'),
              position: 'bottom-right',
              type: 'success'
            });
            this.getList()
          }
        })
      }).catch(e => {})
    },
    // 选择的用户改变
    userSelectChange (data) {
      this.rowData.recipients = data
      if(this.$refs.messInfoForm) {
        let _this = this
        if(this.$refs.messInfoForm.$refs.ruleForm) {
          let _this = this
          if(data.length > 0) {
            setTimeout(function (){
              _this.$refs.messInfoForm.$refs.ruleForm.clearValidate(['recipients'])
            }, 30)
          }
        }
      }
    },
    // 字典颜色
    getValByAttrOfDic (val1, attr1, attr2) {
      let str = ''
      let dicData = [
        {label: '广播消息', value: 'broadcast', color: '#f56c6c'},
        {label: '警告消息', value: 'warning', color: '#ed1b24'},
        {label: '通知消息', value: 'notification', color: '#e6e1e8'},
        {label: '系统消息', value: 'system', color: '#aeaeae'},
        {label: '业务消息', value: 'business', color: '#4a8ff0'}
      ]
      for(let i in dicData) {
        if(dicData[i][attr1] == val1) {
          str = dicData[i][attr2]
        }
      }
      return str
    },
    // 清除校验
    clear (props) {
      this.$refs.messInfoForm.$refs.ruleForm.clearValidate(props)
    },
    // 手动校验
    validate (props) {
      this.$refs.messInfoForm.$refs.ruleForm.validateField(props)
    }
  },
  watch: {
    'rowData.sendType' : {
      handler (newVal, oldVal) {
        let show = false
        if(newVal) {
          show = true
        }else{
          show = false
        }
        this.searchOption.column.filter(item => {
          if(item.prop == 'recipients') {
            item.display = show
          }
        })
        this.$forceUpdate()
      }
    }
  }
}
</script>
<style lang="scss" scoped>
.body-zero-padding{
  /deep/.el-dialog{
    .el-dialog__body{
      padding: 20px 0;
    }
  }
}
</style>
<style lang="scss">
.log-manage {
  height: 100%;
  .el-form {
    .el-date-editor--datetimerange.el-input__inner {
      width: auto;
    }
  }
  .timeline-box {
    display: flex;
    justify-content: space-between;
    .el-timeline-funciton {
      width: 20%;
      .el-timeline-item__content{
        span{
          cursor: pointer;
        }
      }
    }
    .info-item {
      width: 60%;
      .el-row{
        display: flex;
        margin: 20px 0;
        i{
          font-style: normal;
          min-width: 100px;
          text-align: right;
        }
        span, .jv-container{
          flex: 1;
        }
      }
    }
  }
  .timeline-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-left: 20%;
  }
  .data-timeline{
    width: 40%;
    position: relative;
    .data-timeline-loading{
      position: absolute;
      width: 100%;
    }
  }
}
</style>
