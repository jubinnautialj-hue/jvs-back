<template>
  <div class="system-config-page">
    <div class="form-title">
      系统设置
    </div>
    <div class="config-list">
      <div class="config-list-item" v-for="(item,index) in platConfigList" :key="'plat-config-'+index" @click="handleOtherConfig(item)" v-show="needConfigList.indexOf(item.type) > -1">
        <div class="config-img">
          <img :src="item.imgSrc" alt=""/>
        </div>
        <div class="config-footer">
          <div class="config-list-title">
            <span class="text" :title="item.title">{{item.title}}</span>
            <el-tag v-if="alreadyTypes.indexOf(item.type) == -1" type="warning" size="mini" style="margin-left: 10px">{{$langt('systemconfig.notConfig')}}</el-tag>
          </div>
          <div class="config-list-desc">
            <span :title="item.desc">{{item.desc}}</span>
          </div>
        </div>
      </div>
      <div class="config-list-item" v-for="(item,index) in configList" :key="index" @click="handleConfig(item)"
        v-if="['WORKFLOW_TODO_REMINDER', 'WORKFLOW_REMINDER'].indexOf(item.type) > -1 ? (jvsDesign && jvsDesign.JVS_DESIGN_MGR) : true">
        <div class="config-img">
          <img :src="item.imgSrc" alt=""/>
        </div>
        <div class="config-footer">
          <div class="config-list-title">
            <span class="text" :title="item.title">{{item.title}}</span>
            <el-tag v-if="alreadyTypes.indexOf(item.type) == -1" type="warning" size="mini" style="margin-left: 10px">{{$langt('systemconfig.notConfig')}}</el-tag>
          </div>
          <div class="config-list-desc">
            <span :title="item.desc">{{item.desc}}</span>
          </div>
        </div>
      </div>
      <div class="config-list-item" v-for="(item,index) in otherConfigList" :key="'other-config-'+index" @click="handleOtherConfig(item)">
        <div class="config-img">
          <img :src="item.imgSrc" alt=""/>
        </div>
        <div class="config-footer">
          <div class="config-list-title">
            <span class="text" :title="item.title">{{item.title}}</span>
            <el-tag v-if="alreadyTypes.indexOf(item.type) == -1" type="warning" size="mini" style="margin-left: 10px">{{$langt('systemconfig.notConfig')}}</el-tag>
          </div>
          <div class="config-list-desc">
            <span :title="item.desc">{{item.desc}}</span>
          </div>
        </div>
      </div>
    </div>
    <el-drawer
      direction="rtl"
      append-to-body
      :title="formTitle"
      :size="800"
      :modal="false"
      custom-class="custom-setting-drawer"
      :visible.sync="dialogVisible"
      :before-close="handleClose">
      <div v-if="dialogVisible">
        <div v-if="currentTips && currentTips.length > 0" class="text-tips-list">
          <div class="tips">
            <div class="title">
              <span class="el-icon-info" style="color: #376DFF;"></span>
              <span style="margin-left: 8px;">{{$langt('common.tips')}}</span>
            </div>
            <ul class="tips-list">
              <li v-for="(tip, tix) in currentTips" :key="'tips-item-'+tix">
                <div>
                  <i>{{Number(tix)+1}}{{$langt('systemconfig.dot')}}</i>
                  <span v-html="tip"></span>
                </div>
              </li>
            </ul>
          </div>
        </div>
        <jvs-form ref="ruleForm" :option="option" :formData="formData" @submit="submitHandle" @cancalClick="handleClose"></jvs-form>
      </div>
    </el-drawer>
    <!-- 创建  修改  消息通知 -->
    <el-drawer
      :title="dialogTempTitle"
      :visible.sync="dialogTempVisible"
      direction="rtl"
      :size="800"
      :modal="false"
      custom-class="custom-setting-drawer"
      append-to-body
      :before-close="handleTempClose"
      :close-on-click-modal="false">
      <div v-if="currentTips && currentTips.length > 0" class="text-tips-list">
        <div class="tips">
          <div class="title">
            <span class="el-icon-info" style="color: #376DFF;"></span>
            <span style="margin-left: 8px;">{{$langt('common.tips')}}</span>
          </div>
          <ul class="tips-list">
            <li v-for="(tip, tix) in currentTips" :key="'tips-item-'+tix">
              <div>
                <i>{{Number(tix)+1}}{{$langt('systemconfig.dot')}}</i>
                <span v-html="tip"></span>
              </div>
            </li>
          </ul>
        </div>
      </div>
      <message-push v-if="dialogTempVisible" :customSet="true" :fieldKeyList="fieldKeyOptions" :timeLimit="currentType == 'WORKFLOW_REMINDER'" @submit="submitMessage" @closeDialog="handleTempClose" ref="messagePush" style="margin-bottom: 72px;"></message-push>
      <el-row class="form-item-btn" style="display:flex;align-items:center;">
        <el-button type="primary" size="mini" @click="submitTempHandle">{{$langt('form.submit')}}</el-button>
        <el-button size="mini" @click="handleTempClose">{{$langt('form.cancel')}}</el-button>
      </el-row>
    </el-drawer>
    <!-- 平台级配置 -->
    <el-drawer
      direction="rtl"
      :title="platFormTitle"
      append-to-body
      :size="800"
      :modal="false"
      custom-class="custom-setting-drawer"
      :visible.sync="platDialogVisible"
      :before-close="platHandleClose">
      <div v-if="currentTips && currentTips.length > 0" class="text-tips-list">
        <div class="tips">
          <div class="title">
            <span class="el-icon-info" style="color: #376DFF;"></span>
            <span style="margin-left: 8px;">{{$langt('common.tips')}}</span>
          </div>
          <ul class="tips-list">
            <li v-for="(tip, tix) in currentTips" :key="'tips-item-'+tix">
              <div>
                <i>{{Number(tix)+1}}{{$langt('systemconfig.dot')}}</i>
                <span v-html="tip"></span>
              </div>
            </li>
          </ul>
        </div>
      </div>
      <div v-if="platDialogVisible">
        <jvs-form :class="{'item-nobottom': (['BACKGROUND_PERSONALIZED_CONFIGURATION', 'APS_CONFIGURATION'].indexOf(currentType) > -1)}" ref="platRuleForm" :option="platFormOption" :formData="platFormData" @submit="platHandleConfirm" @cancalClick="platHandleClose">
          <template slot="iconForm">
            <div :class="{'sys-set-image-select': platFormData.icon}">
              <img v-if="platFormData.icon" :src="platFormData.icon" alt="">
              <div v-else class="choose-box" @click="imageSelectHandle('icon')">
                <svg class="icon" aria-hidden="true" style="width: 24px;height: 24px;">
                  <use xlink:href="#icon-jvs-a-huaban12"></use>
                </svg>
                <div>{{$langt('systemconfig.chooseImg')}}</div>
              </div>
              <div v-if="platFormData.icon" class="delete-image-box">
                <i class="delete-image-icon el-icon-delete" @click="deleteImgProp('icon')"></i>
              </div>
            </div>
          </template>
          <template slot="logoForm">
            <div :class="{'sys-set-image-select': platFormData.logo}">
              <img v-if="platFormData.logo" :src="platFormData.logo" alt="">
              <div v-else class="choose-box" @click="imageSelectHandle('logo')">
                <svg class="icon" aria-hidden="true" style="width: 24px;height: 24px;">
                  <use xlink:href="#icon-jvs-a-huaban12"></use>
                </svg>
                <div>{{$langt('systemconfig.chooseImg')}}</div>
              </div>
              <div v-if="platFormData.logo" class="delete-image-box">
                <i class="delete-image-icon el-icon-delete" @click="deleteImgProp('logo')"></i>
              </div>
            </div>
          </template>
          <template slot="bgImgForm">
            <div :class="{'sys-set-image-select': platFormData.bgImg}">
              <img v-if="platFormData.bgImg" :src="platFormData.bgImg" alt="">
              <div v-else class="choose-box" @click="imageSelectHandle('bgImg')">
                <svg class="icon" aria-hidden="true" style="width: 24px;height: 24px;">
                  <use xlink:href="#icon-jvs-a-huaban12"></use>
                </svg>
                <div>{{$langt('systemconfig.chooseImg')}}</div>
              </div>
              <div v-if="platFormData.bgImg" class="delete-image-box">
                <i class="delete-image-icon el-icon-delete" @click="deleteImgProp('bgImg')"></i>
              </div>
            </div>
          </template>
          <template slot="title1Form">
            <div class="title-item-tool">
              <div>
                <svg class="icon" aria-hidden="true" style="width: 16px;height: 16px;">
                  <use xlink:href="#icon-jvs-rongqi"></use>
                </svg>
                <span>{{$langt('systemconfig.getMPInfo')}}</span>
              </div>
              <div class="button" @click="viewImage(0)">{{$langt('systemconfig.tutorial')}}</div>
            </div>
          </template>
          <template slot="title2Form">
            <div class="title-item-tool">
              <div>
                <svg class="icon" aria-hidden="true" style="width: 16px;height: 16px;">
                  <use xlink:href="#icon-jvs-rongqi"></use>
                </svg>
                <span>{{$langt('systemconfig.auth')}}</span>
              </div>
              <div class="button" @click="viewImage(1)">{{$langt('systemconfig.tutorial')}}</div>
            </div>
          </template>
        </jvs-form>
      </div>
    </el-drawer>
    <imageSelect
      ref="logoSelect"
      :title="$langt('systemconfig.chooseImg')"
      :dialogVisible="chooseAble"
      :paramInfo="{'module': 'application'}"
      @handleConfirm="imageSelectConfirm"
      @handleClose="imageSelectClose"
    ></imageSelect>

    <!-- 教程图片 -->
    <el-dialog
      class="viewInfoDialog"
      :title="viewImageInfo[viewIndex].label"
      :visible.sync="viewVisible"
      append-to-body
      :modal="false"
      :before-close="viewClose">
      <div v-if="viewVisible">
        <img class="img" :src="viewImageInfo[viewIndex].img" alt="">
        <div class="click-tool">
          <div v-if="viewIndex > 0" class="last" @click="viewImage(viewIndex-1)">
            <i class="el-icon-arrow-left"></i>
            <span>{{$langt('systemconfig.last')}}{{viewImageInfo[viewIndex-1].label}}</span>
          </div>
          <div v-else></div>
          <div v-if="viewIndex < viewImageInfo.length-1" class="next" @click="viewImage(viewIndex+1)">
            <span>{{$langt('systemconfig.next')}} {{viewImageInfo[viewIndex+1].label}}</span>
            <i class="el-icon-arrow-right"></i>
          </div>
          <div v-else></div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>

import { getSysConfig, saveSysConfig, getSysConfigInfo } from "@/views/upms/views/inviteUser/api";
import { getDynamicDesign, getDynamicResource } from '@/api/admin/home'
import store from "@/store";
import config from "@/views/upms/views/otherConfig/img/config.png";
import logo from "@/views/upms/views/otherConfig/img/logo.png";
import daiban from "@/views/upms/views/otherConfig/img/待办.png";
import cuiban from "@/views/upms/views/otherConfig/img/催办.png";
import duanxin from "@/views/upms/views/otherConfig/img/短信.png";
import youjian from "@/views/upms/views/otherConfig/img/邮件.png";
import ding from "@/views/upms/views/otherConfig/img/钉钉.png";
import qw from "@/views/upms/views/otherConfig/img/企微.png";
import gzh from "@/views/upms/views/otherConfig/img/公众号.png";
import getwxinfo from "@/views/upms/views/otherConfig/img/getwxinfo.png"
import wxsq from "@/views/upms/views/otherConfig/img/wxsq.png"
import {getNoticeFields} from "@/views/upms/views/notice/api";
import { getDomain } from "@/api/login"
import MessagePush from "@/views/page/components/basic-design/messagePush";
import { client_id } from '@/const/const'
import { basicOption, basic, dingOption, DING_H5, wxOption, WX_ENTERPRISE, wechatMpOption, WECHAT_MP, LDAPOption, LDAP, CASOption, CAS } from '@/views/upms/views/otherConfig/config'
import imageSelect from "@/components/basic-assembly/ImageSelect";
export default {
  name: "EditUser",
  components: {
    MessagePush,
    imageSelect
  },
  data () {
    return {
      currentType: '',
      configList: [
        { type: 'WORKFLOW_TODO_REMINDER', title: '工作流待办提醒', desc: '设置待办事项的消息通知模板，包含发送方式和对应方式所需要的消息内容。', imgSrc: daiban, permisionFlag: 'jvs_base_notice' },
        { type: 'WORKFLOW_REMINDER', title: '催办提醒', desc: '设置催办提醒的消息通知模板，包含发送方式和对应方式所需要的消息内容。', imgSrc: cuiban, permisionFlag: 'jvs_base_notice' },
        { type: 'SMS_CONFIGURATION', title: '短信配置', desc: '短信配置后，可发送短信消息，也支持手机号密码登录。', imgSrc: duanxin, permisionFlag: 'jvs_other_operating_system_config',
          tips:[
            '短信配置后可实现手机验证码登陆,绑定手机号.集成&自动化短信组件,任务管理,企业文档,短信信息通知等功能',
            '请在<a href="https://www.aliyun.com/product/sms" target="blank" style="color: #3471FF;">阿里云短信服务</a>获取以下配置信息',
            '签名: 必须与国内文档短信中"签名名称"相同,如果不相同将无法发送成功',
            "在阿里云短信服务[国内消息]->[模板管理]中添加模板后.再(短信消息模板'跳短信消息路由')中同步消息模板即可",
            '阿里云短信模板审核时长一般2小时内完成，近期平均完成审核时长约1小时，如遇升级核验、审核任务较多、非工作时间，审核时间可能会延长，请耐心等待',
            '审核工作时间：周一至周日9:00-21:00（法定节假日顺延）'
          ]},
        { type: 'MAIL_CONFIGURATION', title: '邮件配置', desc: '邮件配置后，可使用逻辑组件灵活配置发送邮件内容，或发送用户邀请邮件。', imgSrc: youjian, permisionFlag: 'jvs_other_operating_system_config',
          tips: [
            '开启QQ邮箱的协议：登录QQ邮箱 -- 设置 -- 账户 -- POP3/IMAP/SMTP/Exchange/CardDAV/CalDAV服务，开启服务：POP3/SMTP服务',
            '生成授权码'
          ]},
      ],
      dialogVisible: false,
      dialogTempVisible: false,
      dialogTempTitle: '',
      formTitle: '',
      formData: {},
      basicOption: {
        cancal: false,
        emptyBtn: false,
        submitLoading: false,
        submitBtnText: this.$langt('form.save'),
        formAlign: 'top',
        column: [
          {
            label: '组织名称',
            prop: 'shortName',
            rules: [
              { required: true, message: '请输入组织名称', trigger: 'blur' },
            ]
          },
          {
            label: '公司全称',
            prop: 'name',
            rules: [
              { required: true, message: '请输入公司全称', trigger: 'blur' },
            ]
          },
          {
            label: '用户初始密码',
            prop: 'defaultPassword',
            tips: {
              position: 'bottom',
              text: '创建的用户默认使用此密码进行登录，如果用户已经存在请使用邀请用户加入组织'
            },
            rules: [
              { required: true, message: '请输入用户初始密码', trigger: 'blur' },
            ]
          },
          {
            label: '水印',
            prop: 'watermark',
            type: 'switch',
            tips: {
              position: 'bottom',
              text: '默认将用户登录昵称做为水印显示'
            },
          },
        ]
      },
      smsOption: {
        cancal: false,
        emptyBtn: false,
        submitLoading: false,
        submitBtnText: this.$langt('form.save'),
        formAlign: 'top',
        column: [
          // {
          //   label: '是否启用',
          //   type: 'switch',
          //   prop: 'enable',
          // },
          {
            label: 'accessKeyId',
            prop: 'accessKeyId',
            rules: [
              { required: true, message: '请输入accessKeyId', trigger: 'blur' },
            ]
          },
          {
            label: 'accessKeySecret',
            prop: 'accessKeySecret',
            rules: [
              { required: true, message: '请输入accessKeySecret', trigger: 'blur' },
            ]
          },
          {
            label: '签名（signature）',
            prop: 'signature',
            rules: [
              { required: true, message: '请输入signature', trigger: 'blur' },
            ]
          },
        ]
      },
      emailOption: {
        cancal: false,
        emptyBtn: false,
        submitLoading: false,
        submitBtnText: this.$langt('form.save'),
        formAlign: 'top',
        column: [
          // {
          //   label: '是否启用',
          //   type: 'switch',
          //   prop: 'enable',
          // },
          {
            label: '邮箱地址',
            prop: 'from',
            rules: [
              { required: true, message: '请输入邮箱地址', trigger: 'blur' },
            ]
          },
          {
            label: '邮箱域名',
            prop: 'host',
            rules: [
              { required: true, message: '请输入邮箱域名', trigger: 'blur' },
            ]
          },
          {
            label: '密码',
            prop: 'pass',
            rules: [
              { required: true, message: '请输入密码', trigger: 'blur' },
            ]
          },
        ]
      },
      option: {},
      fieldKeyOptions: [],
      originData: {
        name: '',
        triggerSetting:{
          type:''
        },
        type: ['SYSTEM'],
        extend: [
          {type: 'SYSTEM', title: '', content: '', titleHtml: '', contentHtml: ''}
        ]
      },
      otherConfigList: [
        { type: 'NAIL_APPLICATION_CONFIGURATION', title: '钉钉H5应用', desc: '配置接入钉钉H5应用或钉钉扫码登录', imgSrc: ding,
          tips: [
          '登录钉钉开发平台（开发者后台），依次进入：“应用开发”/“企业内部开发”/“创建应用”，填写“应用名称”、“应用描述”等关键信息，创建“H5微应用”或“小程序”。',
          '完成钉钉应用添加后，即返回钉钉应用列表，双击对于的钉钉应用后，即可获取钉钉应用的令牌信息（主要关注：AppKey、AppSecret）。',
          '钉钉开发平台，依次进入：“应用开发”/“企业内部开发”/“${钉钉应用}”/“权限管理”，之后从权限类别列表中选择“获取凭证”或者在搜索框填写“获取用户token”过滤，查找定位至“调用OpenApp专有API时需要具备的权限”，之后点击“申请权限”即可。',
          '钉钉开发平台，依次进入：“应用开发”/“企业内部开发”/“${钉钉应用}”/“钉钉登录与分享”/“接入登录”，填写回调域名，用于扫描登录后跳转回应用程序。'
        ]},
        { type: 'ENTERPRISE_WECHAT_APPLICATION_CONFIGURATION', title: '企业微信应用', desc: '配置企业微信扫码登录或企业微信组织结构信息同步', imgSrc: qw },
        { type: 'WECHAT_OFFICIAL_ACCOUNT_CONFIGURATION', title: '微信公众号', desc: '配置公众号平台用于消息接收发送；公众号扫码登录', imgSrc: gzh,
          tips: [
            '首先要申请微信公众号（服务号并且要认证300元/年）',
            '登录微信公众号后台->开发->基本设置,获取公众号开发信息',
            '微信授权配置，进入微信后台->设置->公众号设置->功能设置',
            '开启消息代理，微信模版消息配置'
          ]},
        { type: 'LDAP_CONFIGURATION', title: 'LDAP', desc: '配置LDAP服务器相关信息', imgSrc: logo },
      ],
      platConfigList: [
        { type: 'BACKGROUND_PERSONALIZED_CONFIGURATION', title: '后台个性化配置', desc: '设置系统名称logo/icon登录界面', imgSrc: logo,
          tips: [
            '自定义此租户组织的名称 ,多租户切换时和登陆用户浏览器的顶部将展示一下此名称',
            '用户初密码修改后,在添加用户时用户不需要再次设置密码,默认密码将为此处设置的密码.为保证用户安全此密码请勿设置简单',
            '系统不会提示用户必须强制修改密码,建议用户自行登陆后修改登陆密码'
          ]},
        { type: 'PERSONALIZED_CONFIGURATION_IOT_PLATFORMS', title: '物联网个性化配置', desc: '设置系统名称logo/icon登录界面', imgSrc: logo },
        { type: 'ENTERPRISE_DOCUMENT_ENTERPRISE_CONFIGURATION', title: '企业文档企业个性化配置', desc: '设置系统名称logo/icon登录界面', imgSrc: logo },
        { type: 'PERSONALIZED_CONFIGURATION_OF_ENTERPRISE_PLANNING', title: '企业计划个性化配置', desc: '设置系统名称logo/icon登录界面', imgSrc: logo },
        { type: 'PERSONALIZED_CONFIGURATION_OF_EMAIL_SERVICE', title: '邮件服务个性化配置', desc: '设置系统名称logo/icon登录界面', imgSrc: logo },
        { type: 'RULE_PERSONALIZATION_CONFIGURATION', title: '规则个性化配置', desc: '设置系统名称logo/icon登录界面', imgSrc: logo },
        { type: 'PERSONALIZED_CONFIGURATION_OF_DATA_INTELLIGENCE_WAREHOUSE', title: '数据智仓个性化配置', desc: '设置系统名称logo/icon登录界面', imgSrc: logo },
        { type: 'PERSONALIZED_CONFIGURATION_FOR_VIDEO_CONFERENCING', title: '企业会议个性化配置', desc: '设置系统名称logo/icon登录界面', imgSrc: logo},
        { type: 'AUTOMATION_CONFIGURATION', title: '逻辑引擎个性化配置', desc: '设置系统名称logo/icon登录界面', imgSrc: logo },
        { type: 'CAS_CONFIGURATION', title: 'Cas个性化配置', desc: '设置系统名称logo/icon登录界面', imgSrc: logo},
        { type: 'APS_CONFIGURATION', title: 'APS个性化配置', desc: '设置系统名称logo/icon登录界面', imgSrc: logo,
          tips: [
            '自定义此租户组织的名称 ,多租户切换时和登陆用户浏览器的顶部将展示一下此名称',
            '用户初密码修改后,在添加用户时用户不需要再次设置密码,默认密码将为此处设置的密码.为保证用户安全此密码请勿设置简单',
            '系统不会提示用户必须强制修改密码,建议用户自行登陆后修改登陆密码'
          ]
        },
      ],
      platFormTitle: '',
      platDialogVisible: false,
      platFormOption: {},
      currentItem: null,
      platFormData: {
        enable: true,
      },
      alreadyTypes: [],
      imageSelectProp: '',
      chooseAble: false,
      jvsDesign: null,
      currentTips: [],
      needConfigList: ['BACKGROUND_PERSONALIZED_CONFIGURATION'],
      domainInfo: null,
      viewIndex: 0,
      viewVisible: false,
      viewImageInfo: [
        {
          label: '获取公众号开发信息说明',
          img: getwxinfo
        },
        {
          label: '授权配置说明',
          img: wxsq
        }
      ]
    }
  },
  created() {
    this.langInit()
    // 获取可选字段
    getNoticeFields().then(res => {
      if(res && res.data && res.data.code == 0 && res.data.data) {
        this.fieldKeyOptions = res.data.data
      }
    })
    this.getDomainHandle()
    this.getDynamicResource()
    this.getSysConfigInfoHandle()
    this.getDynamicDesign()
  },
  methods: {
    langInit () {
      this.smsOption.column.filter(col => {
        if(col.label) {
          col.label = this.$langt(`systemconfig.smsOption.${col.prop}.label`)
        }
        if(col.rules && col.rules.length > 0) {
          col.rules[0].message = this.$langt(`systemconfig.smsOption.${col.prop}.placeholder`)
        }
      })
      this.emailOption.column.filter(col => {
        if(col.label) {
          col.label = this.$langt(`systemconfig.emailOption.${col.prop}.label`)
        }
        if(col.rules && col.rules.length > 0) {
          col.rules[0].message = this.$langt(`systemconfig.emailOption.${col.prop}.placeholder`)
        }
      })
      this.configList.filter(col => {
        col.title = this.$langt(`systemconfig.config.${col.type}.title`)
        col.desc = this.$langt(`systemconfig.config.${col.type}.desc`)
        if(col.tips && col.tips.length > 0) {
          col.tips = this.$langt(`systemconfig.config.${col.type}.tips`)
        }
      })
      this.otherConfigList.filter(col => {
        col.title = this.$langt(`systemconfig.config.${col.type}.title`)
        col.desc = this.$langt(`systemconfig.config.${col.type}.desc`)
        if(col.tips && col.tips.length > 0) {
          col.tips = this.$langt(`systemconfig.config.${col.type}.tips`)
        }
      })
      this.platConfigList.filter(col => {
        col.title = this.$langt(`systemconfig.config.${col.type}.title`)
        col.desc = this.$langt(`systemconfig.config.${col.type}.desc`)
        if(col.tips && col.tips.length > 0) {
          col.tips = this.$langt(`systemconfig.config.${col.type}.tips`)
        }
      })
      this.viewImageInfo.filter((col, cindex) => {
        col.label = this.$langt(`systemconfig.viewImageInfo[${cindex}]`)
      })
    },
    handleClose() {
      this.dialogVisible = false
      this.currentItem = null
    },
    handleTempClose() {
      this.dialogTempVisible = false
      this.currentItem = null
    },
    // 消息模板提交
    submitMessage (data) {
      let temp = {
        clientId: client_id,
        content: '',
        tenantId: store.state.common.tenantId,
        type: this.currentType
      }
      if(this.currentItem) {
        temp = JSON.parse(JSON.stringify(this.currentItem))
      }
      temp.content = JSON.stringify(data)
      saveSysConfig(temp).then(res=>{
        if(res.data.code == 0){
          this.$notify({
            title: this.$langt('common.tip'),
            message: this.dialogTempTitle+this.$langt('systemconfig.success'),
            position: 'bottom-right',
            type: 'success'
          })
          this.getSysConfigInfoHandle()
          this.handleTempClose()
        }
      })
    },
    handleConfig(obj) {
      this.currentType = obj.type
      if(obj.tips) {
        this.currentTips = obj.tips
      }else{
        this.currentTips = []
      }
      switch (obj.type) {
        case 'WORKFLOW_TODO_REMINDER':
          this.openTemplate(obj)
          break;
        case 'WORKFLOW_REMINDER':
          this.openTemplate(obj)
          break;
        case 'SMS_CONFIGURATION':
          this.formTitle = obj.title
          this.option = JSON.parse(JSON.stringify(this.smsOption))
          this.getSMSConfig()
          break;
        case 'MAIL_CONFIGURATION':
          this.formTitle = obj.title
          this.option = JSON.parse(JSON.stringify(this.emailOption))
          this.getMailConfig()
          break;
        default: ;break;
      }
    },
    // 打开配置
    openTemplate (item) {
      getSysConfig(item.type).then(res =>{
        if(res.data && res.data.code == 0) {
          this.currentItem = {
            clientId: client_id,
            content: '',
            tenantId: store.state.common.tenantId,
            type: this.currentType
          }
          let data = JSON.parse(JSON.stringify(this.originData))
          data.triggerSetting.type = item.type == 'WORKFLOW_TODO_REMINDER' ? 'FLOW_REMIND' : 'FLOW_URGE'
          if(res.data.data) {
            this.currentItem = res.data.data
            if(res.data.data.content) {
              data = JSON.parse(res.data.data.content)
              this.dialogTempTitle = this.$langt('systemconfig.editMess')
            }else{
              this.dialogTempTitle = this.$langt('systemconfig.setMess')
            }
          }
          if(!data.receiver) {
            data.receiver = [{
              id: 'pendingApproval',
              name: this.$langt('systemconfig.pendingApproval'),
              type: 'system_field'
            }]
          }
          if(this.currentType == 'WORKFLOW_REMINDER' && !data.timeLimit) {
            data.timeLimit = { type: 'MINUTE', limit: 0 }
          }
          this.dialogTempVisible = true
          this.$nextTick(()=>{
            this.$refs.messagePush.init(data)
          })
        }
      })
    },
    // 获取短信配置
    getSMSConfig() {
      getSysConfig(this.currentType).then(res => {
        if (res.data && res.data.code == 0 && res.data.data) {
          this.currentItem = res.data.data
          if(res.data.data.content) {
            this.$set(this, 'formData', JSON.parse(res.data.data.content))
          }
          this.$set(this.formData, 'enable', true)
          this.dialogVisible = true
        }else{
          this.currentItem = {
            clientId: client_id,
            content: '',
            tenantId: store.state.common.tenantId,
            type: this.currentType
          }
          this.dialogVisible = true
        }
      })
    },
    // 获取邮箱配置
    getMailConfig() {
      getSysConfig(this.currentType).then(res => {
        if (res.data && res.data.code == 0 && res.data.data) {
          this.currentItem = res.data.data
          if(res.data.data.content) {
            this.$set(this, 'formData', JSON.parse(res.data.data.content))
          }
          this.$set(this.formData, 'enable', true)
          this.dialogVisible = true
        }else{
          this.currentItem = {
            clientId: client_id,
            content: '',
            tenantId: store.state.common.tenantId,
            type: this.currentType
          }
          this.dialogVisible = true
        }
      })
    },
    getSysConfigInfoHandle () {
      getSysConfigInfo().then(res => {
        if(res.data && res.data.code == 0) {
          this.alreadyTypes = res.data.data
        }
      })
    },
    submitHandle(form) {
      let temp = JSON.parse(JSON.stringify(this.currentItem))
      switch (this.currentType) {
        case 'SMS_CONFIGURATION':
          temp.content = JSON.stringify(form)
          saveSysConfig(temp).then(res => {
            if (res.data && res.data.code == 0) {
              this.$notify({
                title: this.$langt('common.tip'),
                message: this.$langt('common.saveSuccess'),
                position: 'bottom-right',
                type: 'success'
              })
              this.getSysConfigInfoHandle()
              this.handleClose()
            }
          })
          break;
        case 'MAIL_CONFIGURATION':
          temp.content = JSON.stringify(form)
          saveSysConfig(temp).then(res => {
            if (res.data && res.data.code == 0) {
              this.$notify({
                title: this.$langt('common.tip'),
                message: this.$langt('common.saveSuccess'),
                position: 'bottom-right',
                type: 'success'
              })
              this.getSysConfigInfoHandle()
              this.handleClose()
            }
          })
          break;
      }
    },
    submitTempHandle() {
      this.$refs.messagePush.submitMessage()
    },
    handleOtherConfig (obj) {
      this.currentType = obj.type
      this.platFormTitle = obj.title
      if(obj.tips) {
        this.currentTips = obj.tips
      }else{
        this.currentTips = []
      }
      getSysConfig(obj.type).then(res => {
        if (res.data && res.data.code == 0) {
          if(res.data.data) {
            this.currentItem = JSON.parse(JSON.stringify(res.data.data))
            if(this.currentItem.content) {
              this.$set(this, 'platFormData', JSON.parse(this.currentItem.content))
            }
            this.$set(this.platFormData, 'enable', true)
            if(this.platFormData.content) {
              delete this.platFormData.content
            }
          }else{
            this.currentItem = {
              clientId: client_id,
              content: '',
              tenantId: store.state.common.tenantId,
              type: this.currentType
            }
          }
          switch(obj.type) {
            case 'NAIL_APPLICATION_CONFIGURATION':
              this.platFormOption = JSON.parse(JSON.stringify(dingOption));
              this.optionLangt('dingOption')
              if(!this.currentItem.content) {
                this.platFormData = JSON.parse(JSON.stringify(DING_H5))
              }
              break;
            case 'ENTERPRISE_WECHAT_APPLICATION_CONFIGURATION':
              this.platFormOption = JSON.parse(JSON.stringify(wxOption))
              this.optionLangt('wxOption')
              if(!this.currentItem.content) {
                this.platFormData = JSON.parse(JSON.stringify(WX_ENTERPRISE))
              }
              break;
            case 'WECHAT_OFFICIAL_ACCOUNT_CONFIGURATION':
              this.platFormOption = JSON.parse(JSON.stringify(wechatMpOption))
              this.optionLangt('wechatMpOption')
              if(!this.currentItem.content) {
                this.platFormData = JSON.parse(JSON.stringify(WECHAT_MP))
              }
              break;
            case 'LDAP_CONFIGURATION':
              this.platFormOption = JSON.parse(JSON.stringify(LDAPOption))
              this.optionLangt('LDAPOption')
              if(!this.currentItem.content) {
                this.platFormData = JSON.parse(JSON.stringify(LDAP))
              }
              break;
            case 'CAS_CONFIGURATION':
              this.platFormOption = JSON.parse(JSON.stringify(CASOption))
              this.optionLangt('CAS_CONFIGURATION')
              if(!this.currentItem.content) {
                this.platFormData = JSON.parse(JSON.stringify(CAS))
              }
              break;
            case 'BACKGROUND_PERSONALIZED_CONFIGURATION':
            case 'PERSONALIZED_CONFIGURATION_IOT_PLATFORMS':
            case 'ENTERPRISE_DOCUMENT_ENTERPRISE_CONFIGURATION':
            case 'PERSONALIZED_CONFIGURATION_OF_ENTERPRISE_PLANNING':
            case 'PERSONALIZED_CONFIGURATION_OF_EMAIL_SERVICE':
            case 'RULE_PERSONALIZATION_CONFIGURATION':
            case 'PERSONALIZED_CONFIGURATION_OF_DATA_INTELLIGENCE_WAREHOUSE':
            case 'PERSONALIZED_CONFIGURATION_FOR_VIDEO_CONFERENCING':
            case 'AUTOMATION_CONFIGURATION':
            case 'APS_CONFIGURATION':
              this.platFormOption = JSON.parse(JSON.stringify(basicOption))
              if(['BACKGROUND_PERSONALIZED_CONFIGURATION', 'APS_CONFIGURATION'].indexOf(obj.type) > -1){
                this.platFormOption.column = [{
                  label: '用户初始密码',
                  prop: 'defaultPassword',
                  tips: {
                    position: 'bottom',
                    text: '创建的用户默认使用此密码进行登录，如果用户已经存在请使用邀请用户加入组织'
                  },
                  rules: [
                    { required: true, message: '请输入用户初始密码', trigger: 'blur' },
                  ]
                }].concat(this.platFormOption.column)
              }
              this.platFormOption.column.filter(col => {
                if(col.prop == 'domainName') {
                  col.display = this.domainInfo ? this.domainInfo.multiTenantMode : false
                  col.append = this.domainInfo ? ('.' + this.domainInfo.domain) : ''
                }
              })
              this.optionLangt('basicOption')
              if(!this.currentItem.content) {
                this.platFormData = JSON.parse(JSON.stringify(basic))
              }
              if(['BACKGROUND_PERSONALIZED_CONFIGURATION', 'APS_CONFIGURATION'].indexOf(obj.type) > -1) {
                if(this.platFormData.skipLogin !== false) {
                  this.$set(this.platFormData, 'skipLogin', true)
                }
              }
              break;
            default:break;
          }
          this.$set(this.platFormOption, 'submitLoading', false)
          this.platFormOption.emptyBtn = false
          this.platDialogVisible = true
          this.$forceUpdate()
        }
      })
    },
    imageSelectHandle (prop) {
      this.imageSelectProp = prop
      this.chooseAble = true
      this.$refs.logoSelect.init()
    },
    imageSelectConfirm (value) {
      if(value && value.fileLink) {
        this.$set(this.platFormData, this.imageSelectProp, value.fileLink)
      }
      this.imageSelectClose()
    },
    imageSelectClose () {
      this.chooseAble = false
      this.imageSelectProp = ''
    },
    platHandleConfirm (form) {
      let temp = JSON.parse(JSON.stringify(this.currentItem))
      let subData = JSON.parse(JSON.stringify(this.platFormData))
      subData = Object.assign(subData, form)
      let basicType = []
      this.platConfigList.filter(pit => {
        basicType.push(pit.type)
      })
      if(basicType.indexOf(this.currentType) > -1) {
        subData.icon = form.icon
        subData.logo = form.logo
        subData.bgImg = form.bgImg
      }
      temp.content = JSON.stringify(subData)
      this.$set(this.platFormOption, 'submitLoading', true)
      saveSysConfig(temp).then(res => {
        if (res.data && res.data.code == 0) {
          this.$notify({
            title: this.$langt('common.tip'),
            message: this.$langt('common.saveSuccess'),
            position: 'bottom-right',
            type: 'success'
          })
          this.$set(this.platFormOption, 'submitLoading', false)
          this.getSysConfigInfoHandle()
          this.platHandleClose()
        }
      }).catch(e => {
        this.$set(this.platFormOption, 'submitLoading', false)
      })
    },
    platHandleClose () {
      this.platFormData = {}
      this.platFormOption = {}
      this.platDialogVisible = false
      this.currentType = ''
      this.platFormTitle = ''
      this.viewClose()
    },
    getDynamicDesign () {
      getDynamicDesign().then(res => {
        if(res.data && res.data.code == 0) {
          this.jvsDesign = res.data.data
        }
      })
    },
    deleteImgProp (prop) {
      this.$set(this.platFormData, prop, '')
    },
    getDynamicResource () {
      this.needConfigList = ['BACKGROUND_PERSONALIZED_CONFIGURATION', 'APS_CONFIGURATION']
      getDynamicResource().then(res => {
        if(res.data && res.data.code == 0) {
          if(res.data.data) {
            res.data.data.filter(it => {
              if(it.type) {
                this.needConfigList.push(it.type)
              }
            })
          }
        }
      })
    },
    // 获取域名对应设置信息
    getDomainHandle () {
      getDomain().then(res => {
        if(res.data && res.data.code == 0) {
          if(res.data.data){
            this.domainInfo = res.data.data
          }
        }
      })
    },
    viewImage (index) {
      this.viewIndex = index
      this.viewVisible = true
    },
    viewClose () {
      this.viewIndex = 0
      this.viewVisible = false
    },
    optionLangt (optionkey) {
      this.platFormOption.column.filter(col => {
        if(col.label) {
          col.label = this.$langt(`systemconfig.${optionkey}.${col.prop}.label`)
        }
        if(col.tips && col.tips.text) {
          col.tips.text = this.$langt(`systemconfig.${optionkey}.${col.prop}.tip`)
        }
        if(col.rules && col.rules.length > 0) {
          col.rules[0].message = this.$langt(`systemconfig.${optionkey}.${col.prop}.placeholder`)
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.system-config-page {
  padding: 16px 24px 16px 8px;
  background-color: #ffffff;
  height: calc(100vh - 80px);
  box-sizing: border-box;
  overflow-y: auto;
  .form-title{
    font-weight: 600;
    font-size: 18px;
    margin-bottom: 16px;
    display: none;
  }
  .config-list{
    display: flex;
    flex-wrap: wrap;
    .config-list-item{
      cursor: pointer;
      position: relative;
      border-radius: 6px;
      overflow: hidden;
      border: 1px solid #EEEFF0;
      width: calc(20% - 16px);
      margin-left: 16px;
      margin-bottom: 16px;
      box-sizing: border-box;
      .config-img{
        img{
          display: block;
          width: 100%;
          // height: 176px;
        }
      }
      .config-footer{
        width: 100%;
        padding: 14px 16px;
        box-sizing: border-box;
        .config-list-title{
          width: 100%;
          height: 23px;
          line-height: 23px;
          font-family: Source Han Sans-Bold, Source Han Sans;
          font-weight: 700;
          font-size: 16px;
          color: #363B4C;
          overflow: hidden;
          display: flex;
          align-items: center;
          .text{
            flex: 1;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }
        .config-list-desc{
          margin-top: 4px;
          margin-bottom: 13px;
          display: flex;
          height: 17px;
          font-family: Source Han Sans-Regular, Source Han Sans;
          font-weight: 400;
          font-size: 12px;
          color: #6F7588;
          line-height: 17px;
          overflow: hidden;
          span{
            display: block;
            width: 100%;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }
      }
      .config-list-config{
        text-align: right;
        i{
          margin-left: 6px;
        }
      }
    }
    .noAuth{
      display: none;
    }
  }
  .form-page{
    border: 1px solid #DCDFE6;
    padding: 20px 15px;
    border-radius: 5px;
    width: 50%;
    /deep/.jvs-form .el-form-item.form-btn-bar .el-form-item__content{
      text-align: left!important;
    }
    /deep/.el-form-item{
      padding: 0 32px;
    }
    /deep/.el-alert--info.is-light{
      margin-bottom: 12px;
    }
    .relation-list{
      display: flex;
      margin-bottom: 16px;
      :last-child{
        margin-bottom: 0;
      }
    }
    .alert-box{
      i{
        margin-right: 8px;
        color: #3471ff;
      }
    }
    .form-title{
      font-weight: 600;
      font-size: 18px;
      margin-bottom: 16px;
    }
  }
}
.sys-set-image-select{
  position: relative;
  display: block;
  width: 128px;
  height: 128px;
  img{
    display: block;
    width: 128px;
    height: 128px;
  }
  .delete-image-box{
    display: none;
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 32px;
    background: #363B4C;
    border-radius: 0px 0px 4px 4px;
    opacity: 0.5;
    align-items: center;
    justify-content: center;
    .delete-image-icon{
      font-size: 16px;
      color: #fff;
      cursor: pointer;
    }
  }
  &:hover{
    .delete-image-box{
      display: flex;
    }
  }
}
.text-tips-list{
  margin-bottom: 16px;
  .tips{
    .title{
      font-family: Microsoft YaHei-Regular, Microsoft YaHei;
      font-weight: 400;
      font-size: 16px;
      color: #1E6FFF;
    }
    .secTitle{
      color: #20252B;
      margin-top: 10px;
      font-size: 15px;
      text-indent: 26px;
    }
    .tips-list{
      padding: 16px;
      background: #EDF4FF;
      border-radius: 4px;
      overflow: hidden;
      margin: 0;
      list-style: none;
      margin-top: 8px;
      li{
        margin: 0;
        padding: 0;
        color: #363B4C;
        list-style: none;
        font-size: 14px;
        display: flex;
        align-items: center;
        display: flex;
        div{
          display: flex;
        }
        i{
          line-height: 32px;
          font-style: normal;
          font-family: Microsoft YaHei-Regular, Microsoft YaHei;
          font-weight: 400;
        }
        span{
          line-height: 32px;
          font-family: Microsoft YaHei-Regular, Microsoft YaHei;
          font-weight: 400;
        }
      }
    }
  }
}
/deep/.custom-setting-drawer{
  box-shadow: -5px 0px 10px 0px rgba(54,59,76,0.1);
  border-radius: 4px;
  .el-drawer__header{
    height: 68px;
    padding: 0 24px;
    border-bottom: 1px solid #EEEFF0;
    margin-bottom: 0;
    span{
      font-family: Microsoft YaHei-Bold, Microsoft YaHei;
      font-weight: 700;
      font-size: 20px;
      color: #363B4C;
    }
    i{
      color: #6F7588;
    }
  }
  .el-drawer__body{
    padding: 16px 24px;
    box-sizing: border-box;
    .form-item-btn{
      position: fixed;
      width: 800px;
      height: 72px;
      right: 0;
      bottom: 0;
      border-top: 1px solid #EEEFF0;
      background: #fff;
      z-index: 20000;
      display: flex;
      align-items: center;
      padding: 0 24px;
      box-sizing: border-box;
      .el-button{
        height: 32px;
      }
      .form-btn-bar{
        margin-bottom: 0;
      }
    }
    .jvs-form{
      padding-bottom: 72px;
      .el-form-item{
        .choose-box{
          width: 128px;
          height: 128px;
          background: #F5F6F7;
          border-radius: 4px 4px 4px 4px;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          cursor: pointer;
          >div{
            height: 17px;
            font-family: Source Han Sans, Source Han Sans;
            font-weight: 400;
            font-size: 12px;
            color: #6F7588;
            line-height: 17px;
            margin-top: 9px;
          }
        }
        .title-item-tool{
          display: flex;
          align-items: center;
          justify-content: space-between;
          height: 20px;
          div{
            span{
              margin-left: 10px;
              font-family: Microsoft YaHei-Regular, Microsoft YaHei;
              font-weight: 400;
              font-size: 16px;
              color: #363B4C;
            }
          }
          .button{
            cursor: pointer;
            font-family: Microsoft YaHei-Regular, Microsoft YaHei;
            font-weight: 400;
            font-size: 14px;
            color: #1E6FFF;
          }
        }
      }
      &.item-nobottom{
        .el-form-item{
          margin-bottom: 0;
        }
      }
    }
  }
}
.viewInfoDialog{
  left: calc(50% - 768px);
  top: calc(50% - 242px);
  width: 736px;
  height: 483px;
  box-shadow: 0px 4px 10px 0px rgba(54,59,76,0.15);
  border-radius: 4px 4px 4px 4px;
  border: 1px solid #EEEFF0;
  box-sizing: border-box;
  overflow: hidden;
  /deep/.el-dialog{
    background: #1E6FFF;
    margin: 0!important;
    padding: 0;
    width: 100%;
    border-radius: 0!important;
    .el-dialog__header{
      height: 18px;
      line-height: 18px;
      padding: 24px 24px 16px 24px;
      .el-dialog__title{
        font-family: Microsoft YaHei-Bold, Microsoft YaHei;
        font-weight: 700;
        font-size: 14px;
        color: #FFFFFF;
      }
      .el-dialog__headerbtn{
        .el-dialog__close{
          color: #fff;
        }
      }
      &::before{
        display: none;
      }
    }
    .el-dialog__body{
      padding: 24px;
      padding-top: 0;
      .img{
        display: block;
        width: 100%;
        height: 367px;
        background: #F6F7F8;
        border-radius: 4px;
        overflow: hidden;
      }
      .click-tool{
        margin-top: 16px;
        display: flex;
        align-items: center;
        justify-content: space-between;
        .last, .next{
          display: flex;
          align-items: center;
          color: #fff;
          cursor: pointer;
        }
        .last{
          i{
            margin-right: 8px;
          }
        }
        .next{
          i{
            margin-left: 8px;
          }
        }
      }
    }
  }
}
</style>
