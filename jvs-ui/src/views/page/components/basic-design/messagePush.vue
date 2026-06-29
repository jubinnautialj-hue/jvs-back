<template>
  <div class="message-push-box">
    <el-form :model="messageForm" :rules="messageRules" ref="messageForm" class="message-push-form outside" size="mini" label-position="top">
      <el-form-item :label="$langt('messagePush.name.label')" prop="name">
        <el-input v-model="messageForm.name" size="mini" :placeholder="$langt('messagePush.name.label')"></el-input>
      </el-form-item>
      <el-form-item :label="$langt('messagePush.enabled.label')" prop="enabled">
        <el-switch v-model="messageForm.enabled"></el-switch>
      </el-form-item>
      <el-form-item v-if="customSet && timeLimit" :label="$langt('messagePush.timeLimit.label')" prop="timeLimit">
        <el-select
          v-model="messageForm.timeLimit.type"
          size="mini"
          :placeholder="$langt('messagePush.timeLimit.placeholder')"
          @change="timeLimitTypeChange"
          style="width: 100px"
        >
          <el-option :value="timeLimitType.MINUTE" :label="$langt('messagePush.timeLimit.dicData.MINUTE')"></el-option>
          <el-option :value="timeLimitType.HOUR" :label="$langt('messagePush.timeLimit.dicData.HOUR')"></el-option>
          <el-option :value="timeLimitType.DAY" :label="$langt('messagePush.timeLimit.dicData.DAY')"></el-option>
        </el-select>
        <span style="margin: 0 10px">{{$langt('messagePush.timeLimit.tip')}}:</span>
        <el-input-number
          :min="1"
          :max="messageForm.timeLimit.type === 'MINUTE' ? 59 : (messageForm.timeLimit.type === 'HOUR' ? 23 : 30)"
          :step="1"
          size="mini"
          :precision="0"
          v-model="messageForm.timeLimit.limit"
        ></el-input-number>
        <span style="margin-left: 5px;">{{ messageForm.timeLimit.type === timeLimitType.HOUR ? $langt('messagePush.timeLimit.dicData.HOUR') : (messageForm.timeLimit.type == timeLimitType.DAY ? $langt('messagePush.timeLimit.dicData.DAY') : $langt('messagePush.timeLimit.dicData.MINUTE')) }}</span>
      </el-form-item>
      <el-form-item v-if="!customSet" :label="$langt('messagePush.triggerSetting.label')" prop="triggerSetting">
        <div class="message-push-box-from">
          <el-select v-model="messageForm.triggerSetting.type" style="width: 232px;" @change="typeChange">
            <el-option
              v-for="item in settingTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
          <!-- 编辑成功 -->
          <div class="right" v-if="messageForm.triggerSetting.type=='edited'">
            <el-select v-model="messageForm.triggerSetting.condition.type" @change="fieldKeysChange" style="width: 200px;">
              <el-option
                v-for="item in fieldTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value">
              </el-option>
            </el-select>
            <el-select v-if="messageForm.triggerSetting.condition.type=='specified_field'"
              v-model="messageForm.triggerSetting.condition.fieldKeys" multiple :collapse-tags="true">
              <el-option
                v-for="item in fieldKeyOptions"
                :key="item.fieldKey"
                :label="item.fieldName"
                :value="item.fieldKey">
              </el-option>
            </el-select>
            <span class="text">{{$langt('messagePush.triggerSetting.edit')}}</span>
          </div>
          <!-- 审批节点 -->
          <div class="right" v-if="messageForm.triggerSetting.type=='flow_approval_node'">
            <span class="text">{{$langt('messagePush.triggerSetting.approval')}}</span>
            <el-select
              v-model="messageForm.triggerSetting.condition.nodeIds" multiple class="right-select" :collapse-tags="true">
              <el-option
                v-for="item in options"
                :key="item.id"
                :label="item.name"
                :value="item.id">
              </el-option>
            </el-select>
            <span class="text">{{$langt('messagePush.triggerSetting.approvalText')}}</span>
          </div>
          <!-- 审批结果 -->
          <div class="right" v-if="messageForm.triggerSetting.type=='flow_approval_results'">
            <span class="text">{{$langt('messagePush.triggerSetting.results')}}</span>
            <el-select
              v-model="messageForm.triggerSetting.condition.flowTaskStatus" multiple class="right-select">
              <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value">
              </el-option>
            </el-select>
            <span class="text">{{$langt('messagePush.triggerSetting.resultsText')}}</span>
          </div>
        </div>
      </el-form-item>
      <el-form-item v-if="!customSet" :label="$langt('messagePush.receiver.label')" prop="receiver">
        <div class="person-box">
          <span v-if="messageForm.receiver.length==0" class="placeholder">{{$langt('messagePush.receiver.placeholder')}}</span>
          <el-tag size="mini" closable @close="()=>{tagDel(item,index)}" v-for="(item,index) in messageForm.receiver" :key="index">{{item.name}}</el-tag>
          <svg aria-hidden="true" @click="openSendPer">
            <use xlink:href="#jvs-ui-icon-xinjian"></use>
          </svg>
        </div>
      </el-form-item>
      <el-form-item :label="$langt('messagePush.type.label')" prop="type">
        <div>
          <el-checkbox-group v-model="messageForm.type" size="mini" @change="messageTypeChange">
            <el-checkbox v-for="mi in typeList" :key="mi.prop" :label="mi.prop" :disabled="mi.disabled">{{mi.label}}</el-checkbox>
          </el-checkbox-group>
        </div>
      </el-form-item>
      <div v-for="(eit, eix) in messageForm.extend" :key="'message-extend-item-'+eix" v-show="messageForm.type.indexOf(eit.type) > -1">
        <!-- 站内信 -->
        <el-form v-if="eit.type == 'SYSTEM'" :model="eit" :rules="systemRules" ref="systemForm" class="message-push-form" size="mini" label-position="top">
          <p class="title-bar-box"><i></i><span>{{$langt('messagePush.systemForm.titleHtml.label')}}</span></p>
          <el-form-item label="站内信标题" prop="titleHtml" class="aciton-label">
            <div slot="label" class="aciton-container">
              <span>{{$langt('messagePush.systemForm.titleHtml.placeholder')}}</span>
              <el-popover
                trigger="click"
                v-model="titleVisible">
                <div class="popover-list">
                  <template v-for="(item,index) in fieldKeyOptions">
                    <div class="popover-list-item"  :key="index" v-if="item.fieldName" @click="popoverClick(item,'titleEditor', 'titleVisible')">
                      {{item.fieldName}}
                    </div>
                  </template>
                </div>
                <jvs-button slot="reference" type='text'>{{$langt('messagePush.systemForm.titleHtml.button')}}</jvs-button>
              </el-popover>
            </div>
            <!-- <el-input v-model="messageForm.title"></el-input> -->
            <div id="messageTitle"></div>
          </el-form-item>
          <el-form-item :label="$langt('messagePush.systemForm.contentHtml.label')" prop="contentHtml" class="aciton-label">
            <div slot="label" class="aciton-container">
              <span>{{$langt('messagePush.systemForm.contentHtml.placeholder')}}</span>
              <el-popover
                trigger="click"
                v-model="contentVisible">
                <div class="popover-list">
                  <template v-for="(item,index) in fieldKeyOptions">
                    <div class="popover-list-item"  :key="index" v-if="item.fieldName" @click="popoverClick(item,'contentEditor', 'contentVisible')">
                      {{item.fieldName}}
                    </div>
                  </template>
                </div>
                <jvs-button slot="reference" type='text'>{{$langt('messagePush.systemForm.contentHtml.button')}}</jvs-button>
              </el-popover>
            </div>
            <!-- <el-input v-model="messageForm.content" type="textarea"
            :autosize="{ minRows: 4, maxRows: 4}"></el-input> -->
            <div id="messageContent"></div>
          </el-form-item>
        </el-form>
        <!-- end  站内信 -->
        <!-- 微信公众号 -->
        <el-form v-if="eit.type == 'WECHAT_MP'" :model="eit" :rules="wecRules" ref="wechatForm" class="message-push-form" size="mini" label-position="top">
          <p class="title-bar-box"><i></i><span>{{$langt('messagePush.WECHAT_MP.label')}}</span></p>
          <el-form-item :label="$langt('messagePush.WECHAT_MP.templateCode.label')" prop="templateCode">
            <el-select v-model="eit.templateCode" :placeholder="$langt('messagePush.WECHAT_MP.templateCode.placeholder')" @change="wxmpCodeChange(eit)" style="width:50%;">
              <el-option
                v-for="item in wxmpTpList"
                :key="item.templateId"
                :label="item.title"
                :value="item.templateId">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item :label="$langt('messagePush.WECHAT_MP.template.label')" prop="template">
            <el-input v-model="eit.template" type="textarea" autosize @change="getTemplateVaribles(eit.type)" @blur="getTemplateVaribles(eit.type)" disabled></el-input>
          </el-form-item>
        </el-form>
        <el-form v-if="eit.type == 'WECHAT_MP' && eit.wechatMp.templateDataList && eit.wechatMp.templateDataList.length > 0" :model="eit.wechatMp" class="message-push-form" size="mini" label-position="top">
          <el-form-item :label="$langt('messagePush.WECHAT_MP.templateDataList.label')" prop="templateDataList">
            <el-row :key="eit.template">
              <el-col :span="12" class="row-list" v-for="(titem, tindex) in eit.wechatMp.templateDataList" :key="'wechat-item-variable-'+tindex">
                <el-form :model="titem" :rules="variableRules" ref="variableForm" class="message-push-form" size="mini" label-position="right">
                  <el-form-item :label="titem.name" prop="value" class="aciton-label" style="display:flex;">
                    <el-select v-model="titem.value" :placeholder="$langt('messagePush.WECHAT_MP.templateDataList.placeholder')" style="margin-left:10px;" filterable :allow-create="false">
                      <el-option
                        v-for="item in fieldKeyOptions"
                        :key="'choose-item-'+eix+'-'+tindex+'-'+item.fieldKey"
                        :label="item.fieldName"
                        :value="item.fieldKey">
                      </el-option>
                    </el-select>
                  </el-form-item>
                </el-form>
              </el-col>
            </el-row>
          </el-form-item>
        </el-form>
        <!-- end  微信公众号 -->
        <!-- 企业微信 -->
        <el-form v-if="eit.type == 'WECHAT_ENTERPRISE'" :model="eit" :rules="wxEnterpriseRules" ref="wechatEnterPriseForm" class="message-push-form" size="mini" label-position="top">
          <p class="title-bar-box"><i></i><span>{{$langt('messagePush.WECHAT_ENTERPRISE.label')}}</span></p>
          <el-form-item :label="$langt('messagePush.WECHAT_ENTERPRISE.contentHtml.label')" prop="contentHtml" class="aciton-label">
            <div slot="label" class="aciton-container">
              <span>{{$langt('messagePush.WECHAT_ENTERPRISE.contentHtml.placeholder')}}</span>
              <el-popover
                trigger="click"
                v-model="wxEnterpriseVisible">
                <div class="popover-list">
                  <template v-for="(item,index) in fieldKeyOptions">
                    <div class="popover-list-item"  :key="index" v-if="item.fieldName" @click="popoverClick(item,'wxEnterpriseEditor', 'wxEnterpriseVisible')">
                      {{item.fieldName}}
                    </div>
                  </template>
                </div>
                <jvs-button slot="reference" type='text'>{{$langt('messagePush.WECHAT_ENTERPRISE.contentHtml.button')}}</jvs-button>
              </el-popover>
            </div>
            <div id="wechatEnterpriseText"></div>
          </el-form-item>
        </el-form>
        <!-- end  企业微信 -->
        <!-- 钉钉 -->
        <el-form v-if="eit.type == 'DING'" :model="eit" :rules="dingdingRules" ref="dingdingForm" class="message-push-form" size="mini" label-position="top">
          <p class="title-bar-box"><i></i><span>{{$langt('messagePush.DING.label')}}</span></p>
          <el-form-item :label="$langt('messagePush.DING.contentHtml.label')" prop="contentHtml" class="aciton-label">
            <div slot="label" class="aciton-container">
              <span>{{$langt('messagePush.DING.contentHtml.placeholder')}}</span>
              <el-popover
                trigger="click"
                v-model="dingVisible">
                <div class="popover-list">
                  <template v-for="(item,index) in fieldKeyOptions">
                    <div class="popover-list-item"  :key="index" v-if="item.fieldName" @click="popoverClick(item,'dingEditor', 'dingVisible')">
                      {{item.fieldName}}
                    </div>
                  </template>
                </div>
                <jvs-button slot="reference" type='text'>{{$langt('messagePush.DING.contentHtml.button')}}</jvs-button>
              </el-popover>
            </div>
            <div id="dingdingText"></div>
          </el-form-item>
        </el-form>
        <!-- end  钉钉 -->
        <!-- EMAIL -->
        <el-form v-if="eit.type == 'EMAIL'" :model="eit" :rules="systemRules" ref="emailForm" class="message-push-form" size="mini" label-position="top">
          <p class="title-bar-box"><i></i><span>{{$langt('messagePush.EMAIL.label')}}</span></p>
          <el-form-item :label="$langt('messagePush.EMAIL.titleHtml.label')" prop="titleHtml" class="aciton-label">
            <div slot="label" class="aciton-container">
              <span>{{$langt('messagePush.EMAIL.titleHtml.placeholder')}}</span>
              <el-popover
                trigger="click"
                v-model="emailtitleVisible">
                <div class="popover-list">
                  <template v-for="(item,index) in fieldKeyOptions">
                    <div class="popover-list-item"  :key="index" v-if="item.fieldName" @click="popoverClick(item, 'emailtitleEditor',  'emailtitleVisible')">
                      {{item.fieldName}}
                    </div>
                  </template>
                </div>
                <jvs-button slot="reference" type='text'>{{$langt('messagePush.EMAIL.titleHtml.button')}}</jvs-button>
              </el-popover>
            </div>
            <!-- <el-input v-model="messageForm.title"></el-input> -->
            <div id="emailTitle"></div>
          </el-form-item>
          <el-form-item :label="$langt('messagePush.EMAIL.contentHtml.label')" prop="contentHtml" class="aciton-label">
            <div slot="label" class="aciton-container">
              <span>{{$langt('messagePush.EMAIL.contentHtml.placeholder')}}</span>
              <el-popover
                trigger="click"
                v-model="emailcontentVisible">
                <div class="popover-list">
                  <template v-for="(item,index) in fieldKeyOptions">
                    <div class="popover-list-item"  :key="index" v-if="item.fieldName" @click="popoverClick(item, 'emailcontentEditor', 'emailcontentVisible')">
                      {{item.fieldName}}
                    </div>
                  </template>
                </div>
                <jvs-button slot="reference" type='text'>{{$langt('messagePush.EMAIL.contentHtml.button')}}</jvs-button>
              </el-popover>
            </div>
            <!-- <el-input v-model="messageForm.content" type="textarea"
            :autosize="{ minRows: 4, maxRows: 4}"></el-input> -->
            <div id="emailContent"></div>
          </el-form-item>
        </el-form>
        <!-- end  EMAIL -->
        <!-- 短信 -->
        <el-form v-if="eit.type == 'SMS'" :model="eit" :rules="wecRules" ref="smsForm" class="message-push-form" size="mini" label-position="top">
          <p class="title-bar-box"><i></i><span>{{$langt('messagePush.SMS.label')}}</span></p>
          <el-form-item :label="$langt('messagePush.SMS.templateCode.label')" prop="templateCode">
            <el-select v-model="eit.templateCode" :placeholder="$langt('messagePush.SMS.templateCode.placeholder')" @change="smsCodeChange(eit)" style="width:50%;">
              <el-option
                v-for="item in smsTpList"
                :key="item.templateCode"
                :label="item.templateName"
                :value="item.templateCode">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item :label="$langt('messagePush.SMS.template.label')" prop="template">
            <el-input v-model="eit.template" type="textarea" autosize @change="getTemplateVaribles(eit.type)" @blur="getTemplateVaribles(eit.type)" disabled></el-input>
          </el-form-item>
          <el-form-item :label="$langt('messagePush.SMS.variables.label')" prop="variables" v-if="eit.variables && eit.variables.length > 0">
            <el-row :key="eit.templateCode">
              <el-col :span="12" class="row-list" v-for="(titem, tindex) in eit.variables" :key="'wechat-item-variable-'+tindex">
                <el-form :model="titem" :rules="variableRules" ref="variableForm" class="message-push-form" size="mini" label-position="right">
                  <el-form-item :label="titem.name" prop="value" class="aciton-label" style="display:flex;">
                    <el-select v-model="titem.value" :placeholder="$langt('messagePush.SMS.variables.placeholder')" style="margin-left:10px;">
                      <el-option
                        v-for="item in fieldKeyOptions"
                        :key="'choose-item-'+eix+'-'+tindex+'-'+item.fieldKey"
                        :label="item.fieldName"
                        :value="item.fieldKey">
                      </el-option>
                    </el-select>
                  </el-form-item>
                </el-form>
              </el-col>
            </el-row>
          </el-form-item>
        </el-form>
        <!-- end  短信 -->
      </div>
    </el-form>
    <userSelector ref="userSelector" :selectable="true"
      :userEnable="true"
      :roleEnable="true"
      :systemEnable="true"
      :jvsAppId="$route.query.jvsAppId"
      :dataModelId="modelId"
      @submit="addCheckUSer"></userSelector>
  </div>
</template>

<script>
import { getDesignNode, saveModelMessage, saveBaseModelMessage, getMessageSendType, getWxmpTemplateList, getSmsTemplateList, getEnableTypes } from '../../api/message'
import { getModelAllFieldsByNotice } from '@/components/template/api'
import userSelector from './userSelector.vue'
import E from "wangeditor";

export default {
  props:{
    modelId:{
      type:String,
      default:''
    },
    designId:{
      type:String,
      default:''
    },
    workflowId:{
      type:String,
      default:''
    },
    customSet: {
      type: Boolean,
      default: false
    },
    fieldKeyList: {
      type: Array
    },
    timeLimit: {
      type: Boolean
    }
  },
  components:{userSelector},
  computed: {
    otherTypeList () {
      return this.typeList.filter(it => {
        return it.prop != 'SYSTEM'
      })
    }
  },
  data() {
    var checkTriggerSetting = (rule, value, callback) => {
      if(!this.messageForm.triggerSetting.type){
        return  callback(new Error(this.$langt('messagePush.triggerError')));
      }
      switch (this.messageForm.triggerSetting.type) {
        case 'edited':
          if(this.messageForm.triggerSetting.condition.type){
            if(this.messageForm.triggerSetting.condition.type=='specified_field'){
              if(this.messageForm.triggerSetting.condition.fieldKeys.length==0){
                return callback(new Error(this.$langt('messagePush.triggerError')));
              }
            }
          }else{
            return callback(new Error(this.$langt('messagePush.triggerError')));
          }
          break;
        case 'flow_approval_node':
          if(this.messageForm.triggerSetting.condition.nodeIds.length==0){
            return callback(new Error(this.$langt('messagePush.triggerError')));
          }
          break;
        case 'flow_approval_results':
          if(this.messageForm.triggerSetting.condition.flowTaskStatus.length==0){
            return callback(new Error(this.$langt('messagePush.triggerError')));
          }
          break;
        default:
          break;
      }
      return callback()
    };
    return {
      messageForm:{
        name: '',
        enabled: false,
        triggerSetting:{
          type:'',
          condition:{
            type:'',
            fieldKeys:[],
            flowTaskStatus:[],
            nodeIds:[]
          }
        },
        receiver:[],
        type: ['SYSTEM'],
        extend: [
          {type: 'SYSTEM', title: '', content: '', titleHtml: '', contentHtml: ''},
          {type: 'WECHAT_MP', templateCode: '', template: '', wechatMp: { templateDataList: [] }}
        ]
      },
      messageRules:{
        name: [{ required: true, message: '请填写模板名称', trigger: 'blur' }],
        triggerSetting:[{ required: true, validator: checkTriggerSetting, trigger: 'blur' }],
        receiver:[{ required: true, message: '请选择发送对象', trigger: 'change' }],
        type:[{ required: true, message: '请选择发送方式', trigger: 'change' }],
      },
      systemRules: {
        titleHtml:[
          { required: true, message: '请输入消息标题', trigger: 'blur' },
        ],
        contentHtml:[
          { required: true, message: '请输入消息内容', trigger: 'blur' },
        ],
      },
      wecRules: {
        templateCode: [ { required: true, message: '请选择模板', trigger: 'change' } ],
        template: [ { required: true, message: '请填写模板内容', trigger: 'blur' } ],
      },
      variableRules: {
        value: [ { required: true, message: '请选择变量对应字段', trigger: 'change' } ]
      },
      settingTypeOptions: [{
        label:'创建成功',
        value:"created"
      },{
        label:'编辑成功',
        value:"edited"
      },{
        label:'删除成功',
        value:"deleted"
      },{
        label:'启动流程',
        value:"flow_created"
      },{
        label:'审批结果',
        value:"flow_approval_results"
      },{
        label:'审批节点',
        value:"flow_approval_node"
      }],
      fieldTypeOptions: [{
        label:'任意字段',
        value:'any'
      },{
        label:'指定字段',
        value:'specified_field'
      }],
      statusOptions: [{
        label:'已通过',
        value:'2'
      },{
        label:'已拒绝',
        value:'3'
      },{
        label:'已终止',
        value:'4'
      }],
      fieldKeyOptions: [],
      options: [],
      titleEditor: null,
      titleVisible: false,
      contentEditor: null,
      contentVisible:false,
      typeList: [],
      wxEnterpriseEditor: null,
      wxEnterpriseVisible: false,
      wxEnterpriseRules: {
        contentHtml: [ { required: true, message: '请填写消息内容', trigger: 'blur' } ]
      },
      dingEditor: null,
      dingVisible: false,
      dingdingRules: {
        contentHtml: [ { required: true, message: '请填写消息内容', trigger: 'blur' } ]
      },
      emailtitleEditor: null,
      emailtitleVisible:false,
      emailcontentEditor: null,
      emailcontentVisible:false,
      wxmpTpList: [],
      smsTpList: [],
      enableTypeList: [],
      timeLimitType: {
        MINUTE: 'MINUTE',
        DAY: 'DAY',
        HOUR: 'HOUR'
      }
    };
  },
  async created() {
    this.langtInit()
    this.typeList = []
    await getEnableTypes().then(res => {
      if(res && res.data && res.data.code == 0 && res.data.data) {
       this.enableTypeList = res.data.data
      }
    })
    getMessageSendType().then(res => {
      if(res && res.data && res.data.code == 0 && res.data.data) {
        res.data.data.filter(item => {
          let key = Object.keys(item)
          let obj = {
            label: item[key[0]],
            prop: key[0],
            disabled: true
          }
          if(this.enableTypeList.indexOf(key[0]) > -1 || key[0] == 'SYSTEM') {
            obj.disabled = false
          }else{
            if(key[0] == 'DING' && this.enableTypeList.indexOf('DING_H5') > -1) {
              obj.disabled = false
            }
            if(key[0] == 'WECHAT_ENTERPRISE' && this.enableTypeList.indexOf('WX_ENTERPRISE') > -1) {
              obj.disabled = false
            }
          }
          this.typeList.push(obj)
        })
      }
    })
    if(this.customSet && this.timeLimit && !this.messageForm.timeLimit) {
      this.$set(this.messageForm, 'timeLimit', { type: this.timeLimitType.MINUTE, limit: 0})
    }
    this.getWxmpTemplateList()
    this.getSmsTemplateList()
    this.$forceUpdate()
  },
  mounted() {
    if(this.modelId) {
      this.getModelId()
    }
    if(this.workflowId) {
      this.getNode()
    }
    if(this.customSet) {
      this.fieldKeyOptions = this.fieldKeyList
    }
  },
  beforeDestroy(){
    if(this.titleEditor){
      this.titleEditor.destroy()
      this.titleEditor = null
    }
    if(this.contentEditor){
      this.contentEditor.destroy()
      this.contentEditor = null
    }
    if(this.wxEnterpriseEditor){
      this.wxEnterpriseEditor.destroy()
      this.wxEnterpriseEditor = null
    }
    if(this.dingEditor){
      this.dingEditor.destroy()
      this.dingEditor = null
    }
    if(this.emailtitleEditor){
      this.emailtitleEditor.destroy()
      this.emailtitleEditor = null
    }
  },
  methods: {
    langtInit () {
      for(let m in this.messageRules) {
        if(this.messageRules[m][0].message) {
          this.messageRules[m][0].message = this.$langt(`messagePush.rules.${m}`)
        }
      }
      for(let s in this.systemRules) {
        if(this.systemRules[s][0].message) {
          this.systemRules[s][0].message = this.$langt(`messagePush.rules.${s}`)
        }
      }
      for(let w in this.wecRules) {
        if(this.wecRules[w][0].message) {
          this.wecRules[w][0].message = this.$langt(`messagePush.rules.${w}`)
        }
      }
      for(let v in this.variableRules) {
        if(this.variableRules[v][0].message) {
          this.variableRules[v][0].message = this.$langt(`messagePush.rules.${v}`)
        }
      }
      this.settingTypeOptions.filter(col => {
        col.label = this.$langt(`messagePush.settingTypeOptions.${col.value}`)
      })
      this.fieldTypeOptions.filter(col => {
        col.label = this.$langt(`messagePush.settingTypeOptions.${col.value}`)
      })
      this.statusOptions.filter(col => {
        col.label = this.$langt(`messagePush.settingTypeOptions.${col.value}`)
      })
    },
    init(val){
      if(val) {
        this.messageForm = JSON.parse(JSON.stringify(val))
        if(!this.messageForm.enabled) {
          this.$set(this.messageForm, 'enabled', false)
        }
        if(this.customSet && this.timeLimit && !this.messageForm.timeLimit) {
          this.$set(this.messageForm, 'timeLimit', { type: this.timeLimitType.MINUTE, limit: 0})
        }
        if(this.messageForm.triggerSetting.condition) {
          this.messageForm.triggerSetting.condition = JSON.parse(val.triggerSetting.condition)
        }
        this.messageForm.type = []
        if(this.messageForm.extend) {
          this.messageForm.extend.filter(item => {
            this.messageForm.type.push(item.type)
            // 站内信
            if(item.type == 'SYSTEM') {
              this.$nextTick( ()=> {
                // 标题编辑器
                if(!this.titleEditor) {
                  this.createEditor('titleEditor', 'messageTitle', 'SYSTEM')
                }
                // 内容编辑器
                if(!this.contentEditor) {
                  this.createEditor('contentEditor', 'messageContent', 'SYSTEM')
                }
                if(item.titleHtml) {
                  this.titleEditor.txt.html(item.titleHtml)
                }
                if(item.contentHtml) {
                  this.contentEditor.txt.html(item.contentHtml)
                }
              })
            }
            // 微信公众号
            if(item.type == 'WECHAT_MP') {
              if(!item.templateCode) {
                item.templateCode = ''
              }
            }
            // 企业微信
            if(item.type == 'WECHAT_ENTERPRISE') {
              if(!item.wxEnterprise) {
                item.wxEnterprise = { TEXT: "" }
              }
              this.$nextTick( ()=> {
                // 企业微信文本内容
                this.createEditor('wxEnterpriseEditor', 'wechatEnterpriseText', 'WECHAT_ENTERPRISE')
                if(item.contentHtml) {
                  this.wxEnterpriseEditor.txt.html(item.contentHtml)
                }
              })
            }
            // 钉钉
            if(item.type == 'DING') {
              if(!item.ding) {
                item.ding = { TEXT: "" }
              }
              this.$nextTick( ()=> {
                // 钉钉文本内容
                this.createEditor('dingEditor', 'dingdingText', 'DING')
                if(item.contentHtml) {
                  this.dingEditor.txt.html(item.contentHtml)
                }
              })
            }
            // 邮件
            if(item.type == 'EMAIL') {
              this.$nextTick( ()=> {
                // 标题编辑器
                this.createEditor('emailtitleEditor', 'emailTitle', 'EMAIL')
                // 内容编辑器
                this.createEditor('emailcontentEditor', 'emailContent', 'EMAIL')
                if(item.titleHtml) {
                  this.emailtitleEditor.txt.html(item.titleHtml)
                }
                if(item.contentHtml) {
                  this.emailcontentEditor.txt.html(item.contentHtml)
                }
              })
            }
          })
        }else{
          this.messageForm.extend = []
          if(this.messageForm.titleHtml || this.messageForm.contentHtml) {
            this.messageForm.type = ['SYSTEM']
            let tobj = { type: 'SYSTEM', title: '', content: '', titleHtml: '', contentHtml: '' }
            if(this.messageForm.titleHtml) {
              this.titleEditor.txt.html(this.messageForm.titleHtml)
              tobj.titleHtml = this.messageForm.titleHtml
            }
            delete this.messageForm.titleHtml
            delete this.messageForm.title
            if(this.messageForm.contentHtml) {
              this.contentEditor.txt.html(this.messageForm.contentHtml)
              tobj.contentHtml = this.messageForm.contentHtml
            }
            delete this.messageForm.contentHtml
            delete this.messageForm.content
            this.messageForm.extend.push(tobj)
          }
        }
      }else{
        // 新增时默认有站内信
        this.$nextTick( ()=> {
          // 标题编辑器
          this.createEditor('titleEditor', 'messageTitle', 'SYSTEM')
          // 内容编辑器
          this.createEditor('contentEditor', 'messageContent', 'SYSTEM')
        })
      }
      this.$forceUpdate()
    },
    fieldKeysChange(){
      this.messageForm.triggerSetting.condition.fieldKeys = []
    },
    typeChange(){
      this.messageForm.triggerSetting.condition = {
        type:'',
        fieldKeys:[],
        flowTaskStatus:[],
        nodeIds:[]
      }
    },
    addCheckUSer(val){
      this.messageForm.receiver = val
    },
    tagDel(val,index){
      this.messageForm.receiver.splice(index,1)
    },
    openSendPer(){
      this.$refs.userSelector.openDialog(this.messageForm.receiver)
    },
    getModelId(){
      getModelAllFieldsByNotice(this.$route.query.jvsAppId, this.modelId, this.designId).then(res=>{
        if(res.data.code == 0 ){
          for(let i in res.data.data) {
            if(!res.data.data[i].fieldKey) {
              res.data.data[i].fieldKey = res.data.data[i].id
              res.data.data[i].fieldName = res.data.data[i].shortName || res.data.data[i].name
            }
          }
          this.fieldKeyOptions = res.data.data
        }
      })
    },
    getNode(){
      getDesignNode(this.workflowId).then(res=>{
        if(res.data.code == 0){
          this.options = res.data.data
        }
      })
    },
    submitMessage(){
      let sysBool = true
      let wechatBool = true
      let variableBool = true
      let wxentpBool = true
      let dingBool = true
      let emailBool = true
      let smsBool = true
      if(this.messageForm.type.indexOf('WECHAT_MP') > -1 || this.messageForm.type.indexOf('SMS') > -1) {
        if(this.$refs['variableForm']) {
          for(let i in this.$refs['variableForm']) {
            this.$refs['variableForm'][i].validate( (valid) => {
              variableBool &= valid
            })
          }
        }
      }
      if(this.messageForm.type.indexOf('SYSTEM') > -1) {
        if(this.$refs['systemForm'] && this.$refs['systemForm'].length > 0) {
          this.$refs['systemForm'][0].validate((valid) => {
            sysBool = valid
          })
        }
      }
      if(this.messageForm.type.indexOf('WECHAT_MP') > -1) {
        if(this.$refs['wechatForm'] && this.$refs['wechatForm'].length > 0) {
          this.$refs['wechatForm'][0].validate((valid) => {
            wechatBool = valid
          })
        }
      }
      if(this.messageForm.type.indexOf('WECHAT_ENTERPRISE') > -1) {
        if(this.$refs['wechatEnterPriseForm'] && this.$refs['wechatEnterPriseForm'].length > 0) {
          this.$refs['wechatEnterPriseForm'][0].validate((valid) => {
            wxentpBool = valid
          })
        }
      }
      if(this.messageForm.type.indexOf('DING') > -1) {
        if(this.$refs['dingdingForm'] && this.$refs['dingdingForm'].length > 0) {
          this.$refs['dingdingForm'][0].validate((valid) => {
            dingBool = valid
          })
        }
      }
      if(this.messageForm.type.indexOf('EMAIL') > -1) {
        if(this.$refs['emailForm'] && this.$refs['emailForm'].length > 0) {
          this.$refs['emailForm'][0].validate((valid) => {
            emailBool = valid
          })
        }
      }
      if(this.messageForm.type.indexOf('SMS') > -1) {
        if(this.$refs['smsForm'] && this.$refs['smsForm'].length > 0) {
          this.$refs['smsForm'][0].validate((valid) => {
            smsBool = valid
          })
        }
      }
      this.$refs['messageForm'].validate((valid) => {
        if (valid && sysBool && wechatBool && variableBool && wxentpBool && dingBool && emailBool && smsBool) {
          let obj = JSON.parse(JSON.stringify(this.messageForm))
          obj.triggerSetting.condition = JSON.stringify(this.messageForm.triggerSetting.condition)
          let temp = []
          obj.extend.filter(it => {
            if(this.messageForm.type.indexOf(it.type) > -1) {
              temp.push(it)
            }
          })
          obj.extend = temp
          delete obj.type
          if(this.customSet) {
            this.$emit('submit', obj)
          }else{
            if(this.customSet) {
              saveBaseModelMessage({modelId:this.modelId, ...obj}).then(res=>{
                if(res.data.code == 0){
                  this.$emit('closeDialog',true)
                }
              })
            }else{
              saveModelMessage(this.$route.query.jvsAppId, {
                modelId:this.modelId,
                ...obj,
              }).then(res=>{
                if(res.data.code == 0){
                  this.$emit('closeDialog',true)
                }
              })
            }
          }
        }
      });
    },
    popoverClick (item, editor, visible){
      this[visible] = false
      this[editor].cmd.do('insertHTML', `<span style="background:rgba(126,134,142,.2);font-size:14px;padding:3px 5px;border-radius:5px;" title="${item.fieldKey}">${item.fieldName}</span><span>&nbsp;</span>`)
    },
    messageTypeChange () {
      if(this.messageForm.type && this.messageForm.type.length > 0) {
        if(!this.messageForm.extend) {
          this.messageForm.extend = []
        }
      }
      // 站内信
      if(this.messageForm.type.indexOf('SYSTEM') > -1) {
        let bool = true
        this.messageForm.extend.filter(it => {
          if(it.type == 'SYSTEM') {
            bool = false
          }
        })
        if(bool) {
          this.messageForm.extend.push({type: 'SYSTEM', title: '', content: '', titleHtml: '', contentHtml: ''})
          this.$nextTick( ()=> {
            this.createEditor('titleEditor', 'messageTitle', 'SYSTEM')
            this.createEditor('contentEditor', 'messageContent', 'SYSTEM')
          })
        }
      }
      // 微信公众号
      if(this.messageForm.type.indexOf('WECHAT_MP') > -1) {
        let bool = true
        this.messageForm.extend.filter(it => {
          if(it.type == 'WECHAT_MP') {
            bool = false
            if(it.wechatMp && (!it.wechatMp.templateDataList || it.wechatMp.templateDataList.length == 0)) {
              this.getTemplateVaribles(it.type)
            }
          }
        })
        if(bool) {
          this.messageForm.extend.push({type: 'WECHAT_MP', templateCode: '', template: '', wechatMp: { clientCode: '', templateDataList: [] }})
        }
      }
      // 企业微信
      if(this.messageForm.type.indexOf('WECHAT_ENTERPRISE') > -1) {
        let bool = true
        this.messageForm.extend.filter(it => {
          if(it.type == 'WECHAT_ENTERPRISE') {
            bool = false
          }
        })
        if(bool) {
          this.messageForm.extend.push({type: 'WECHAT_ENTERPRISE', contentHtml: '', wxEnterprise: { TEXT: '' }})
          this.$nextTick( ()=> {
            this.createEditor('wxEnterpriseEditor', 'wechatEnterpriseText', 'WECHAT_ENTERPRISE')
          })
        }
      }
      // 钉钉
      if(this.messageForm.type.indexOf('DING') > -1) {
        let bool = true
        this.messageForm.extend.filter(it => {
          if(it.type == 'DING') {
            bool = false
          }
        })
        if(bool) {
          this.messageForm.extend.push({type: 'DING', contentHtml: '', ding: { TEXT: '' }})
          this.$nextTick( ()=> {
            this.createEditor('dingEditor', 'dingdingText', 'DING')
          })
        }
      }
      // email
      if(this.messageForm.type.indexOf('EMAIL') > -1) {
        let bool = true
        this.messageForm.extend.filter(it => {
          if(it.type == 'EMAIL') {
            bool = false
          }
        })
        if(bool) {
          this.messageForm.extend.push({type: 'EMAIL', titleHtml: '', contentHtml: ''})
          this.$nextTick( ()=> {
            this.createEditor('emailtitleEditor', 'emailTitle', 'EMAIL')
            this.createEditor('emailcontentEditor', 'emailContent', 'EMAIL')
          })
        }
      }
      // 短信
      if(this.messageForm.type.indexOf('SMS') > -1) {
        let bool = true
        this.messageForm.extend.filter(it => {
          if(it.type == 'SMS') {
            bool = false
          }
        })
        if(bool) {
          this.messageForm.extend.push({type: 'SMS', templateCode : '', template: '',  variables: [] })
        }
      }
      this.$forceUpdate()
    },
    getTemplateVaribles (type) {
      if(this.messageForm.extend) {
        this.messageForm.extend.filter((item, index) => {
          if(item.type == type) {
            if(type == 'WECHAT_MP') {
              if(item.wechatMp) {
                this.$set(this.messageForm.extend[index].wechatMp, 'templateDataList', this.formatVariableListBySymbol('{{}}', item.template))
              }
            }
            if(type == 'SMS') {
              this.$set(this.messageForm.extend[index], 'variables', this.formatVariableListBySymbol('${}', item.template))
            }
          }
        })
      }
      this.$forceUpdate()
    },
    formatVariableListBySymbol (symbol, content) {
      let regexp;
      let result = []
      if(symbol == '{{}}') {
        regexp = /\{{(.+?)\}}/g
        let vars = content.match(regexp)
        if(vars) {
          for(let v of vars) {
            let str = v
            if(v.includes('.')) {
              str = v.split('.')[0]
              str = str.slice(2)
            }else{
              str = str.slice(2, str.length - 2)
            }
            result.push({name: str, value: ''})
          }
        }
      }
      if(symbol == '${}') {
        regexp = /\$\{(.+?)\}/g
        let vars = content.match(regexp)
        if(vars) {
          for(let v of vars) {
            let str = v
            if(v.includes('.')) {
              str = v.split('.')[0]
              str = str.slice(2)
            }else{
              str = str.slice(2, str.length - 1)
            }
            result.push({name: str, value: ''})
          }
        }
      }
      return result
    },
    createEditor (name, domId, type) {
      if(this[name]){
        this[name].destroy()
        this[name] = null
      }
      this[name] = new E('#'+domId)
      this[name].config.placeholder = this.$langt('messagePush.editPlaceholder')
      this[name].config.menus = []
      this[name].config.showFullScreen = false
      this[name].config.height= 140
      this[name].config.onchange = (newHtml)=>{
        this.messageForm.extend.filter(it => {
          if(it.type == type) {
            if(['SYSTEM', 'EMAIL'].indexOf(type) > -1) {
              if(['messageTitle', 'emailTitle'].indexOf(domId) > -1) {
                it.titleHtml = newHtml
              }
              if(['messageContent', 'emailContent'].indexOf(domId) > -1) {
                it.contentHtml = newHtml
              }
            }
            if(['WECHAT_ENTERPRISE', 'DING'].indexOf(type) > -1) {
              it.contentHtml = newHtml
            }
            this.$forceUpdate()
          }
        })
      }
      this[name].create()
    },
    getWxmpTemplateList () {
      getWxmpTemplateList().then(res => {
        if(res.data && res.data.code == 0 && res.data.data) {
          this.wxmpTpList = res.data.data
        }
      })
    },
    getSmsTemplateList () {
      getSmsTemplateList().then(res => {
        if(res.data && res.data.code == 0 && res.data.data) {
          this.smsTpList = res.data.data.filter(item => {
            return item.auditStatus === 'AUDIT_STATE_PASS'
          })
        }
      })
    },
    wxmpCodeChange (item) {
      this.wxmpTpList.filter(wit => {
        if(wit.templateId == item.templateCode) {
          item.template = wit.content
          this.getTemplateVaribles(item.type)
        }
      })
      this.$forceUpdate()
    },
    smsCodeChange (item) {
      this.smsTpList.filter(sit => {
        if(sit.templateCode == item.templateCode) {
          item.template = sit.templateContent
          this.getTemplateVaribles(item.type)
        }
      })
    },
    timeLimitTypeChange(e) {
      this.messageForm.timeLimit.limit = 0
    },
  }
};
</script>

<style scoped lang="scss">
.message-push-box{
  .el-form--label-top{
    /deep/.el-form-item__label{
      padding: 0px !important;
      margin-bottom: 8px;
      font-family: Microsoft YaHei-Bold, Microsoft YaHei;
      font-weight: 700;
      font-size: 14px;
      color: #363B4C;
      &::before{
        color: #FF194C;
      }
    }
    /deep/.el-form-item__content{
      .el-input__inner{
        height: 36px;
        background: #F5F6F7;
        border-radius: 4px;
        border: 0;
      }
    }
  }
  .message-push-form{
    .el-form-item{
      margin-bottom: 16px;
    }
    .person-box{
      min-height: 36px;
      background: #F5F6F7;
      border-radius: 4px;
      padding-right: 40px;
      padding-left: 8px;
      position: relative;
      .placeholder{
        font-family: Source Han Sans-Regular, Source Han Sans;
        font-weight: 400;
        font-size: 14px;
        color: #6F7588;
      }
      /deep/.el-tag{
        transition: none !important;
        margin-right: 8px;
        background: #fff;
        height: 28px;
        line-height: 20px;
        border: 0;
        padding: 4px 8px;
        box-sizing: border-box;
        font-family: Source Han Sans-Regular, Source Han Sans;
        font-weight: 400;
        font-size: 14px;
        color: #363B4C;
        .el-tag__close{
          background: #fff;
          color: #6F7588;
          font-size: 16px;
          font-weight: bold;
        }
      }
      svg{
        width: 16px;
        height: 16px;
        fill: #6F7588;
        position: absolute;
        right: 12px;
        top: 10px;
        cursor: pointer;
      }
    }
    .message-push-box-from{
      width: 100%;
      display: flex;
      .right{
        margin-left: 8px;
        display: flex;
        align-items: center;
        flex-wrap: nowrap;
        overflow: hidden;
        .text{
          font-family: Source Han Sans-Regular, Source Han Sans;
          font-weight: 400;
          font-size: 14px;
          color: #363B4C;
          word-break: keep-all;
        }
        .el-select{
          margin-right: 8px;
          /deep/.el-select__tags{
            height: 36px;
            .el-tag{
              background: #fff;
              border: 0;
              height: calc(100% - 8px);
              margin: 4px 0 4px 8px;
              .el-select__tags-text{
                font-family: Source Han Sans-Regular, Source Han Sans;
                font-weight: 400;
                font-size: 14px;
                color: #363B4C;
              }
              .el-tag__close{
                background: #fff;
                color: #6F7588;
                font-size: 16px;
                font-weight: bold;
              }
            }
          }
        }
        .right-select{
          margin: 0px 10px;
          width: 100%;
        }
      }
    }
    /deep/.aciton-label{
      .el-form-item__label{
        display: flex;
        .aciton-container{
          width: 100%;
          display: flex;
          align-items: center;
          justify-content: space-between;
          .el-button--text{
            font-family: Source Han Sans-Regular, Source Han Sans;
            font-weight: 400;
            font-size: 14px;
            color: #1E6FFF;
          }
        }
      }
    }
    .message-select{
      width: 100%;
    }
    /deep/.el-textarea__inner{
      resize: none;
      border: 0;
    }
    .title-bar-box{
      display: flex;
      align-items: center;
      margin: 0;
      margin-left: -14px;
      padding: 0;
      margin-bottom: 12px;
      i{
        display: block;
        width: 3px;
        height: 16px;
        background: #1E6FFF;
        border-radius: 0px 4px 4px 0px;
        margin-right: 13px;
      }
      span{
        font-family: Microsoft YaHei-Bold, Microsoft YaHei;
        font-weight: 700;
        font-size: 14px;
        color: #363B4C;
      }
    }
    .el-checkbox-group{
      /deep/.el-checkbox{
        &+.el-checkbox{
          margin-left: 0;
        }
        &:nth-last-of-type(1){
          margin-right: 0;
        }
      }
    }
    &:not(.outside){
      background: #F5F6F7;
      border-radius: 4px;
      padding: 12px 16px;
      margin-bottom: 16px;
      /deep/.el-form-item{
        .el-form-item__label{
          height: 20px;
          font-family: Source Han Sans-Medium, Source Han Sans;
          font-weight: 500;
          font-size: 14px;
          color: #363B4C;
          line-height: 20px;
        }
        .el-form-item__content{
          .w-e-toolbar{
            display: none;
          }
          .w-e-text-container{
            border: 0!important;
            border-radius: 4px;
          }
          .el-input__inner, .el-textarea__inner{
            background: #fff;
          }
        }
        .el-form--label-right{
          margin-bottom: 0;
          padding: 0;
          .el-form-item__label{
            height: 36px;
            line-height: 36px;
          }
        }
      }
    }
  }
}
.popover-list{
  max-height: 200px;
  overflow-y: auto;
  .popover-list-item{
    display: flex;
    align-items: center;
    padding: 5px 10px;
    cursor: pointer;
    font-size: 14px;
    &:hover{
      background-color: #eef1f6;
    }
  }
}
</style>
