<template>
  <div class="personal-config-page">
    <div class="form-title">
      个性化应用配置
    </div>
    <div v-if="isConfig">
      <div class="app-info-set-title">
        <div style="display: flex;justify-content: space-between;">
          <span>可以针对每一个终端开通不同的个性化配置，支持LOGO、登录页、名称自定义，支持公众号、企业微信、企业钉钉、idap多种平台集成。</span>
          <i class="el-icon-close close-icon" @click="handleConfig()"></i>
        </div>
        <div class="tip">
          <p>不同终端基础配置时不允许域名相同</p>
          <p>开通了不同三方集成后才能正常使用消息通知</p>
        </div>
      </div>
      <div class="form-box">
        <div class="info-card basic-info">
          <h4>
            <div class="left">
              <span style="margin-right: 20px;">基本设置</span>
              <el-switch v-model="appInfoData[currentApp.app].BASIC.enable" inactive-value="BASIC" size="mini" @change="switchChange"></el-switch>
            </div>
          </h4>
          <div class="el-message el-message--info info-text">
            <i class="el-message__icon el-icon-info"></i>
            <p class="el-message__content">************ <a href="javascript:void(0);" target="_blank">如何获取配置</a></p>
          </div>
          <div class="content-box" v-if="appInfoData[currentApp.app].BASIC.enable !== 'BASIC'">
            <jvs-form ref="basicForm" :option="basicOption" :formData="appInfoData[currentApp.app].BASIC"></jvs-form>
            <div>
              <el-button type="primary" size="mini" :loading="basicLoading" @click="saveInfoHandle('basicForm', currentApp, 'BASIC', appInfoData[currentApp.app])">保存</el-button>
            </div>
          </div>
        </div>
        <div class="info-card dingding-info">
          <h4>
            <div class="left">
              <span style="margin-right: 20px;">钉钉H5应用</span>
              <el-switch v-model="appInfoData[currentApp.app].DING_H5.enable" inactive-value="DING_H5" size="mini"></el-switch>
            </div>
          </h4>
          <div class="el-message el-message--info info-text">
            <i class="el-message__icon el-icon-info"></i>
            <p class="el-message__content">************ <a href="javascript:void(0);" target="_blank">如何获取配置</a></p>
          </div>
          <div class="content-box" v-if="appInfoData[currentApp.app].DING_H5.enable !== 'DING_H5'">
            <jvs-form ref="dingForm" :option="dingOption" :formData="appInfoData[currentApp.app].DING_H5"></jvs-form>
            <div>
              <el-button type="primary" size="mini" :loading="dingLoading" @click="saveInfoHandle('dingForm', currentApp, 'DING_H5', appInfoData[currentApp.app])">保存</el-button>
            </div>
          </div>
        </div>
        <div class="info-card wx-info">
          <h4>
            <div class="left">
              <span style="margin-right: 20px;">企业微信应用</span>
              <el-switch v-model="appInfoData[currentApp.app].WX_ENTERPRISE.enable" inactive-value="WX_ENTERPRISE" size="mini"></el-switch>
            </div>
          </h4>
          <div class="el-message el-message--info info-text">
            <i class="el-message__icon el-icon-info"></i>
            <p class="el-message__content">************ <a href="javascript:void(0);" target="_blank">如何获取配置</a></p>
          </div>
          <div class="content-box" v-if="appInfoData[currentApp.app].WX_ENTERPRISE.enable !== 'WX_ENTERPRISE'">
            <jvs-form ref="wxForm" :option="wxOption" :formData="appInfoData[currentApp.app].WX_ENTERPRISE"></jvs-form>
            <div>
              <el-button type="primary" size="mini" :loading="wxLoading" @click="saveInfoHandle('wxForm', currentApp, 'WX_ENTERPRISE', appInfoData[currentApp.app])">保存</el-button>
            </div>
          </div>
        </div>
        <div class="info-card wechatMp-info">
          <h4>
            <div class="left">
              <span style="margin-right: 20px;">微信公众号</span>
              <el-switch v-model="appInfoData[currentApp.app].WECHAT_MP.enable" inactive-value="WECHAT_MP" size="mini"></el-switch>
            </div>
          </h4>
          <div class="el-message el-message--info info-text">
            <i class="el-message__icon el-icon-info"></i>
            <p class="el-message__content">************ <a href="javascript:void(0);" target="_blank">如何获取配置</a></p>
          </div>
          <div class="content-box" v-if="appInfoData[currentApp.app].WECHAT_MP.enable !== 'WECHAT_MP'">
            <jvs-form ref="wechatMpForm" :option="wechatMpOption" :formData="appInfoData[currentApp.app].WECHAT_MP"></jvs-form>
            <div>
              <el-button type="primary" size="mini" :loading="wechatMpLoading" @click="saveInfoHandle('wechatMpForm', currentApp, 'WECHAT_MP', appInfoData[currentApp.app])">保存</el-button>
            </div>
          </div>
        </div>
        <div class="info-card LDAP-info">
          <h4>
            <div class="left">
              <span style="margin-right: 20px;">LDAP</span>
              <el-switch v-model="appInfoData[currentApp.app].LDAP.enable" inactive-value="LDAP" size="mini"></el-switch>
            </div>
          </h4>
          <div class="el-message el-message--info info-text">
            <i class="el-message__icon el-icon-info"></i>
            <p class="el-message__content">************ <a href="javascript:void(0);" target="_blank">如何获取配置</a></p>
          </div>
          <div class="content-box" v-if="appInfoData[currentApp.app].LDAP.enable !== 'LDAP'">
            <jvs-form ref="LDAPForm" :option="LDAPOption" :formData="appInfoData[currentApp.app].LDAP"></jvs-form>
            <div>
              <el-button type="primary" size="mini" :loading="LDAPLoading" @click="saveInfoHandle('LDAPForm', currentApp, 'LDAP', appInfoData[currentApp.app])">保存</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="apply-list">
      <div class="apply-list-item" v-for="(item,index) in applyList" :key="index" :class="{'noAuth':!item.isAuth}" @click="handleConfig(item)">
<!--        <div class="apply-list-icon" :style="{backgroundColor:item.isAuth?'#F5FAFF':'#FAF9F9'}">-->
<!--          &lt;!&ndash;            <svg-icon :icon-class="item.isAuth?`${item.icon}_active`:item.icon" style="width:40px;height:40px;"></svg-icon>&ndash;&gt;-->
<!--          <svg xmlns="http://www.w3.org/2000/svg" :viewBox="getSvgProp(item.icon).viewBox" width="40" height="40" :style="getSvgProp(item.icon).style" filter="none">-->
<!--            <g>-->
<!--              <path :d="getSvgProp(item.icon).path" :fill="item.isAuth? getSvgProp(item.icon).activeFill : getSvgProp(item.icon).fill"></path>-->
<!--            </g>-->
<!--          </svg>-->
<!--        </div>-->
        <div class="apply-list-icon">
          <img :src="item.imgSrc" alt=""/>
        </div>
        <div class="apply-list-title">
          {{item.title}}
        </div>
        <div class="apply-list-desc">
          {{item.desc}}
        </div>
      </div>
    </div>
  </div>
<!--    <el-alert-->
<!--      :closable="false"-->
<!--      type="info">-->
<!--      <template slot="title">-->
<!--        <div class="alert-box">-->
<!--          <i class="el-icon-info"/>-->
<!--          <span>开启oauth配置后，可通过其它系统免登跳转到本系统中。</span><span style="color: #3471ff;cursor: pointer;">如何获取配置</span>-->
<!--        </div>-->
<!--      </template>-->
<!--    </el-alert>-->
</template>

<script>

import {getLicense} from "@/api/common";
import {getAppSetByTenant, saveAppSetByTenant} from "@/views/upms/views/appBascSetting/api";
import platform from './img/platform.png'
import knowledge from './img/file.png'
import mail from './img/email.png'
import rule from './img/platform.png'
import meeting from './img/meeting.png'
import teamwork from './img/plan.png'

export default {
  name: "PersonalConfig",
  components: {
  },
  data () {
    return {
      personalForm: {},
      currentApp: {},
      appInfoData: {},
      basic: {
        enable: false,
        name: '',
        domain: '',
        icon: [],
        logo: [],
        bgImg: []
      },
      basicLoading: false,
      basicOption: {
        btnHide: true,
        formAlign: 'top',
        column: [
          {
            label: '名称',
            prop: 'name'
          },
          {
            label: '域名',
            prop: 'domain'
          },
          {
            label: 'ICON',
            prop: 'icon',
            type: 'imageUpload',
            limit: 1,
            multipleUpload: false,
            fileList: [],
            action: '/mgr/jvs-auth//upload/jvs-public?module=tenantPicture',
            tips: {
              text: '建议 32 * 32',
              position: 'bottom'
            },
            uploadHttp: {
              parameters: {
                module: '/jvs-ui/tenantPicture/'
              }
            },
          },
          {
            label: 'LOGO',
            prop: 'logo',
            type: 'imageUpload',
            limit: 1,
            multipleUpload: false,
            fileList: [],
            action: '/mgr/jvs-auth//upload/jvs-public?module=tenantPicture',
            tips: {
              text: '建议 200 * 60',
              position: 'bottom'
            },
            uploadHttp: {
              parameters: {
                module: '/jvs-ui/tenantPicture/'
              }
            },
          },
          {
            label: '背景图',
            prop: 'bgImg',
            type: 'imageUpload',
            limit: 1,
            multipleUpload: false,
            fileList: [],
            action: '/mgr/jvs-auth//upload/jvs-public?module=tenantPicture',
            tips: {
              text: '建议 1920 * 1080',
              position: 'bottom'
            },
            uploadHttp: {
              parameters: {
                module: '/jvs-ui/tenantPicture/'
              }
            },
          },
        ]
      },
      DING_H5: {
        enable: false,
        agentId: '', // 钉钉H5微应用的AgentId
        appId: '', // 钉钉H5微应用的appKey
        appSecret: '', // 钉钉H5微应用的appSecret
        enableScan: false, // 是否支持扫码登录
        redirectUri: '/#/login/dingtalk/scanback', // 重定向地址
      },
      dingLoading: false,
      dingOption: {
        btnHide: true,
        formAlign: 'top',
        column: [
          {
            label: 'appKey',
            prop: 'appId',
            rules: [ { required: true, message: '请输入钉钉H5微应用的appKey', trigger: 'blur' } ],
          },
          {
            label: 'appSecret',
            prop: 'appSecret',
            rules: [ { required: true, message: '请输入钉钉H5微应用的appSecret', trigger: 'blur' } ],
          },
          {
            label: 'AgentId',
            prop: 'agentId',
            rules: [ { required: true, message: '请输入钉钉H5微应用的appKey', trigger: 'blur' } ],
          },
          {
            label: '开启扫码登录',
            prop: 'enableScan',
            type: 'switch'
          },
          {
            label: '重定向地址',
            prop: 'redirectUri',
            rules: [ { required: true, message: '请输入重定向地址', trigger: 'blur' } ],
            defaultValue: `/#/login/dingtalk/scanback`,
            disabled: true,
            display: false
          }
        ]
      },
      WX_ENTERPRISE: {
        enable: false,
        enableScan: false, // 是否支持扫码登录
        agentId: '', // 企业微信应用的AgentId
        appId: '', // 企业微信的corp_id（企业id）
        appSecret: '', // 企业微信应用的Secret
        redirectUri: '/auth/just/callback', // 重定向地址
      },
      wxLoading: false,
      wxOption: {
        btnHide: true,
        formAlign: 'top',
        column: [
          {
            label: '企业ID',
            prop: 'appId',
            rules: [ { required: true, message: '请输入企业微信的corp_id（企业id）', trigger: 'blur' } ],
          },
          {
            label: 'AgentId',
            prop: 'agentId',
            rules: [ { required: true, message: '请输入企业微信应用的AgentId', trigger: 'blur' } ],
          },
          {
            label: 'Secret',
            prop: 'appSecret',
            rules: [ { required: true, message: '请输入企业微信应用的Secret', trigger: 'blur' } ],
          },
          {
            label: '开启扫码登录',
            prop: 'enableScan',
            type: 'switch'
          },
          {
            label: '重定向地址',
            prop: 'redirectUri',
            rules: [ { required: true, message: '请输入重定向地址', trigger: 'blur' } ],
            defaultValue: `/auth/just/callback`,
            disabled: true,
            display: false
          }
        ]
      },
      WECHAT_MP: {
        enable: false,
        enableScan: false, // 是否支持扫码登录
        appId: '',
        appSecret: '',
        token: '',
        aesKey: ''
      },
      wechatMpLoading: false,
      wechatMpOption: {
        btnHide: true,
        formAlign: 'top',
        column: [
          {
            label: 'appKey',
            prop: 'appId',
            rules: [ { required: true, message: '请输入appKey', trigger: 'blur' } ],
          },
          {
            label: 'appSecret',
            prop: 'appSecret',
            rules: [ { required: true, message: '请输入appSecret', trigger: 'blur' } ],
          },
          {
            label: 'token',
            prop: 'token',
            // rules: [ { required: true, message: '请输入token', trigger: 'blur' } ],
          },
          {
            label: 'aesKey',
            prop: 'aesKey',
            // rules: [ { required: true, message: '请输入aesKey', trigger: 'blur' } ],
          },
          {
            label: '开启扫码登录',
            prop: 'enableScan',
            type: 'switch'
          }
        ]
      },
      LDAP: {
        enable: false,
        base: '',
        urls: '',
        username: '',
        password: '',
        accountAttribute: ''
      },
      LDAPLoading: false,
      LDAPOption: {
        btnHide: true,
        formAlign: 'top',
        column: [
          {
            label: 'base',
            prop: 'base',
            rules: [ { required: true, message: '请输入base', trigger: 'blur' } ],
          },
          {
            label: 'urls',
            prop: 'urls',
            rules: [ { required: true, message: '请输入urls', trigger: 'blur' } ],
            tips: {
              position: 'bottom',
              text: '多个url以英文逗号,分隔'
            }
          },
          {
            label: '用户名',
            prop: 'username',
            rules: [ { required: true, message: '请输入用户名', trigger: 'blur' } ],
          },
          {
            label: '密码',
            prop: 'password',
            rules: [ { required: true, message: '请输入密码', trigger: 'blur' } ],
          },
          {
            label: '账号字段',
            prop: 'accountAttribute',
            tips: {
              position: 'bottom',
              text: '为空则默认使用uid'
            }
          },
        ]
      },
      applyList:[
        { imgSrc: platform, app: 'frame', title: 'JVS管理后台', desc: '开箱即用的基础脚手架，微服务构架、VUE+ElementUI、支持多租户、消息中心、钉钉企微、扫码密码单点登录、个人中心、移动端配套' },
        { imgSrc: '', app: '', title: '数据智仓', desc: '将复杂的业务逻辑抽离成可视化的配置，让系统业务管理的耦合度降低，更容易被开发和维护' },
        { imgSrc: knowledge, app: 'knowledge', title: '无忧企业文档', desc: '协同编辑+企业知识库+内容管理平台+内容搜索引擎+私有网盘' },
        { imgSrc: meeting, app: 'meeting', title: '无忧视频会议', desc: '基于企业内部私有化部署的在线会议，安全、私密与便捷的高度融合' },
        { imgSrc: mail, app: 'mail', title: '无忧企业邮件', desc: '基于企业内部私有化部署的在线多邮件web客户端，支持各种外部邮箱，将邮件操作与企业内部账号联通，高效、便捷' },
        { imgSrc: teamwork, app: 'teamwork', title: '无忧企业计划', desc: '立志于与提高企业内部执行效率，减少无效会议与沟通的最简单办法' },
        { imgSrc: rule, app: 'risk-gadget', title: '规则引擎', desc: '将复杂的业务逻辑抽离成可视化的配置，让系统业务管理的耦合度降低，更容易被开发和维护' },
      ],
      isConfig: false,
    }
  },
  created() {
  },
  mounted() {
    this.getApplyAuth(false)
  },
  methods: {
    switchChange(e) {
      switch (e) {
        case 'BASIC':
          this.saveInfoHandle('basicForm', this.currentApp, 'BASIC', this.appInfoData[this.currentApp.app]);
          break;
        case 'DING_H5':
          this.saveInfoHandle('dingForm', this.currentApp, 'DING_H5', this.appInfoData[this.currentApp.app]);
          break;
        case 'WX_ENTERPRISE':
          this.saveInfoHandle('wxForm', this.currentApp, 'WX_ENTERPRISE', this.appInfoData[this.currentApp.app]);
          break;
        case 'WECHAT_MP':
          this.saveInfoHandle('wechatMpForm', this.currentApp, 'WECHAT_MP', this.appInfoData[this.currentApp.app]);
          break;
        case 'LDAP':
          this.saveInfoHandle('LDAPForm', this.currentApp, 'LDAP', this.appInfoData[this.currentApp.app]);
          break;
        default:break;
      }
    },
    saveInfoHandle (ref, item, type, data) {
      let bool = true
      if(data[type].enable) {
        if(this.$refs[ref] && this.$refs[ref].length > 0) {
          this.$refs[ref][0].$refs.ruleForm.validate( (valid) => {
            bool = valid
          })
        }
      }
      if(bool) {
        // console.log(this.rowData.id, item.appKey, type, data[type])
        let subData = JSON.parse(JSON.stringify(data[type]))
        switch(type) {
          case 'BASIC':
            this.basicLoading = true;
            // 上传图片数据类型转换
            if(subData.icon && subData.icon.length > 0) {
              subData.icon = subData.icon[0].url
            }else{
              subData.icon = ''
            }
            if(subData.logo && subData.logo.length > 0) {
              subData.logo = subData.logo[0].url
            }else{
              subData.logo = ''
            }
            if(subData.bgImg && subData.bgImg.length > 0) {
              subData.bgImg = subData.bgImg[0].url
            }else{
              subData.bgImg = ''
            }
            break;
          case 'DING_H5': this.dingLoading = true;break;
          case 'WX_ENTERPRISE': this.wxLoading = true;break;
          case 'WECHAT_MP': this.wechatMpLoading = true;break;
          case 'LDAP': this.LDAPLoading = true;break;
          default: ;break;
        }
        saveAppSetByTenant(this.$store.getters.tenantId, item.app, type, subData).then(res => {
          if(res && res.data && res.data.code == 0) {
            switch(type) {
              case 'BASIC': this.basicLoading = false;break;
              case 'DING_H5': this.dingLoading = false;break;
              case 'WX_ENTERPRISE': this.wxLoading = false;break;
              case 'WECHAT_MP': this.wechatMpLoading = false;break;
              case 'LDAP': this.LDAPLoading = false;break;
              default: ;break;
            }
          }
        }).catch(e => {
          switch(type) {
            case 'BASIC': this.basicLoading = false;break;
            case 'DING_H5': this.dingLoading = false;break;
            case 'WX_ENTERPRISE': this.wxLoading = false;break;
            case 'WECHAT_MP': this.wechatMpLoading = false;break;
            case 'LDAP': this.LDAPLoading = false;break;
            default: ;break;
          }
        })
      }
    },
    getAppSetByTenantHandle () {
      getAppSetByTenant(this.$store.getters.tenantId).then(res => {
        if(res && res.data && res.data.code == 0){
          console.log(res.data.data)
          if(res.data.data) {
            this.appInfoData = res.data.data || {}
          }
          this.formatData()
          this.isConfig = true
        }
      })
    },
    formatData () {
      if(!this.appInfoData[this.currentApp.app]) {
        this.$set(this.appInfoData, this.currentApp.app, {
          BASIC: JSON.parse(JSON.stringify(this.basic)),
          DING_H5: JSON.parse(JSON.stringify(this.DING_H5)),
          WX_ENTERPRISE: JSON.parse(JSON.stringify(this.WX_ENTERPRISE)),
          WECHAT_MP: JSON.parse(JSON.stringify(this.WECHAT_MP)),
          LDAP: JSON.parse(JSON.stringify(this.LDAP))
        })
      }else{
        if(!this.appInfoData[this.currentApp.app].BASIC) {
          this.$set(this.appInfoData[this.currentApp.app], 'BASIC', JSON.parse(JSON.stringify(this.basic)))
        }else{
          // 上传图片数据类型转换
          if(this.appInfoData[this.currentApp.app].BASIC.icon) {
            this.$set(this.appInfoData[this.currentApp.app].BASIC, 'icon', [{url: this.appInfoData[this.currentApp.app].BASIC.icon}])
          }else{
            this.$set(this.appInfoData[this.currentApp.app].BASIC, 'icon', [])
          }
          if(this.appInfoData[this.currentApp.app].BASIC.logo) {
            this.$set(this.appInfoData[this.currentApp.app].BASIC, 'logo', [{url: this.appInfoData[this.currentApp.app].BASIC.logo}])
          }else{
            this.$set(this.appInfoData[this.currentApp.app].BASIC, 'logo', [])
          }
          if(this.appInfoData[this.currentApp.app].BASIC.bgImg) {
            this.$set(this.appInfoData[this.currentApp.app].BASIC, 'bgImg', [{url: this.appInfoData[this.currentApp.app].BASIC.bgImg}])
          }else{
            this.$set(this.appInfoData[this.currentApp.app].BASIC, 'bgImg', [])
          }
        }
        if(!this.appInfoData[this.currentApp.app].DING_H5) {
          this.$set(this.appInfoData[this.currentApp.app], 'DING_H5', JSON.parse(JSON.stringify(this.DING_H5)))
        }
        if(!this.appInfoData[this.currentApp.app].WX_ENTERPRISE) {
          this.$set(this.appInfoData[this.currentApp.app], 'WX_ENTERPRISE', JSON.parse(JSON.stringify(this.WX_ENTERPRISE)))
        }
        if(!this.appInfoData[this.currentApp.app].WECHAT_MP) {
          this.$set(this.appInfoData[this.currentApp.app], 'WECHAT_MP', JSON.parse(JSON.stringify(this.WECHAT_MP)))
        }
        if(!this.appInfoData[this.currentApp.app].LDAP) {
          this.$set(this.appInfoData[this.currentApp.app], 'LDAP', JSON.parse(JSON.stringify(this.LDAP)))
        }
      }
    },
    // 配置应用
    handleConfig(currentApp) {
      if (currentApp) {
        console.log(currentApp)
        this.currentApp = JSON.parse(JSON.stringify(currentApp))
        this.getAppSetByTenantHandle()
      } else {
        this.isConfig = false
      }
    },
    init(){
      this.applyList.forEach((appItem,appIndex)=>{
        this.authList.forEach((item,index)=>{
          if(appItem.app==item.app){
            appItem.date = item.expireDate
            appItem.isAuth = ['EXPIRING_ERROR','PASS'].indexOf(item.state)!=-1
          }
        })
      })
      this.$forceUpdate()
    },
    getApplyAuth(flag){
      getLicense({
        // clientId:this.myInfo.clientId
      }).then(res=>{
        if(res.data.code == 0 ){
          // this.applyList = res.data.data.apps || []
          this.authList = res.data.data.apps || []
          this.init()
        }
      })
    },
    // 获取svg的path，viewBox
    getSvgProp(str) {
      switch (str) {
        case 'jvs':
          return {
            path: 'M880 112H144c-17.7 0-32 14.3-32 32v736c0 17.7 14.3 32 32 32h736c17.7 0 32-14.3 32-32V144c0-17.7-14.3-32-32-32zM513.1 518.1l-192 161c-5.2 4.4-13.1.7-13.1-6.1v-62.7c0-2.3 1.1-4.6 2.9-6.1L420.7 512l-109.8-92.2a7.63 7.63 0 0 1-2.9-6.1V351c0-6.8 7.9-10.5 13.1-6.1l192 160.9c3.9 3.2 3.9 9.1 0 12.3zM716 673c0 4.4-3.4 8-7.5 8h-185c-4.1 0-7.5-3.6-7.5-8v-48c0-4.4 3.4-8 7.5-8h185c4.1 0 7.5 3.6 7.5 8v48z',
            viewBox: '64 64 896 896',
            style: 'border-color: rgba(187,187,187,1);border-width: 0px;border-style: solid',
            activeFill: 'rgba(68.08500000000001,154.01999999999998,250.92,1)',
            fill: 'rgba(194.055,195.07500000000002,196.095,1)'
          };
        case 'knowledge':
          return {
            path: 'M928 161H699.2c-49.1 0-97.1 14.1-138.4 40.7L512 233l-48.8-31.3A255.2 255.2 0 0 0 324.8 161H96c-17.7 0-32 14.3-32 32v568c0 17.7 14.3 32 32 32h228.8c49.1 0 97.1 14.1 138.4 40.7l44.4 28.6c1.3.8 2.8 1.3 4.3 1.3s3-.4 4.3-1.3l44.4-28.6C602 807.1 650.1 793 699.2 793H928c17.7 0 32-14.3 32-32V193c0-17.7-14.3-32-32-32zM404 553.5c0 4.1-3.2 7.5-7.1 7.5H211.1c-3.9 0-7.1-3.4-7.1-7.5v-45c0-4.1 3.2-7.5 7.1-7.5h185.7c3.9 0 7.1 3.4 7.1 7.5v45zm0-140c0 4.1-3.2 7.5-7.1 7.5H211.1c-3.9 0-7.1-3.4-7.1-7.5v-45c0-4.1 3.2-7.5 7.1-7.5h185.7c3.9 0 7.1 3.4 7.1 7.5v45zm416 140c0 4.1-3.2 7.5-7.1 7.5H627.1c-3.9 0-7.1-3.4-7.1-7.5v-45c0-4.1 3.2-7.5 7.1-7.5h185.7c3.9 0 7.1 3.4 7.1 7.5v45zm0-140c0 4.1-3.2 7.5-7.1 7.5H627.1c-3.9 0-7.1-3.4-7.1-7.5v-45c0-4.1 3.2-7.5 7.1-7.5h185.7c3.9 0 7.1 3.4 7.1 7.5v45z',
            viewBox: '64 64 896 896',
            style: 'border-color: rgba(232,210,155,1);border-width: 0px;border-style: solid',
            activeFill: 'rgba(232.05,210.11999999999998,155.04,1)',
            fill: 'rgba(194.055,195.07500000000002,196.095,1)'
          };
        case 'teamwork':
          return {
            path: 'M20 6h-4V4c0-1.11-.89-2-2-2h-4c-1.11 0-2 .89-2 2v2H4c-1.11 0-1.99.89-1.99 2L2 19c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V8c0-1.11-.89-2-2-2zm-6 0h-4V4h4v2z',
            viewBox: '0 0 24 24',
            style: 'border-color: rgba(187,187,187,1);border-width: 0px;border-style: solid',
            activeFill: 'rgba(21.93,200.94,250.92,1)',
            fill: 'rgba(194.055,195.07500000000002,196.095,1)'
          };
        default:break;
      }
    },
  }
}
</script>

<style lang="scss" scoped>
.personal-config-page {
  padding: 40px;
  border-radius: 6px;
  background-color: #ffffff;
  height: calc(100vh - 80px);
  overflow-y: auto;
  /deep/.el-form-item{
    padding: 0 32px;
  }
  /deep/.el-alert--info.is-light{
    width: 50%;
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
  .app-info-set-title{
    font-size: 14px;
    .close-icon{
      font-size: 20px;
      cursor: pointer;
    }
    span{
      color: #5e6d82;
    }
    .tip{
      padding: 8px 16px;
      background-color: #ecf8ff;
      border-radius: 4px;
      border-left: 5px solid #50bfff;
      margin: 20px 0;
      p{
        font-size: 14px;
        color: #5e6d82;
        line-height: 1.5em;
        a{
          color: #409eff;
          text-decoration: none;
        }
      }
    }
  }
  .form-box{
    .info-card{
      border: 1px solid #DCDFE6;
      padding: 20px 15px;
      border-radius: 5px;
      width: 50%;
      margin-top: 20px;
      .info-text{
        position: relative;
        top: 0;
        margin-top: 20px;
        background: #F4F4F6;
        i{
          color: #3871F0;
        }
        p{
          color: #9999;
          font-size: 16px;
          a{
            color: #409eff;
            text-decoration: none;
            font-size: 13px;
          }
        }
      }
      h4{
        margin: 0;
        padding: 0;
        display: flex;
        align-items: center;
        justify-content: space-between;
        div{
          display: flex;
          align-items: center;
          .title{
            display: inline-block;
            width: 4px;
            height: 18px;
            background: #3471ff;
            border-radius: 2px;
            margin-right: 10px;
          }
        }
        .right{
          .save-card-icon{
            font-size: 20px;
            margin-left: 10px;
            cursor: pointer;
          }
        }
      }
      .content-box{
        margin-top: 15px;
      }
    }
    .info-card:nth-of-type(1) {
      margin-top: 0;
    }
  }
  .footer-btn{
    //width: 100%;
    //text-align: center;
  }
  .apply-list{
    margin-top: 85px;
    display: flex;
    flex-wrap: wrap;
    .apply-list-item{
      cursor: pointer;
      position: relative;
      border-radius: 20px;
      border:1px solid #E5e5e5;
      //min-height: 222px;
      width: 455px;
      margin-right: 20px;
      margin-bottom: 50px;
      padding: 43px 10px 6px 20px;
      box-sizing: border-box;
      &:last-child{
        margin-right: 0px;
      }
      .apply-list-icon{
        height: 60px;
        width: 60px;
        border-radius: 10px;
        left: 29px;
        position: absolute;
        top: 0px;
        transform: translateY(-50%);
        display: flex;
        align-items: center;
        justify-content: center;
        img{
          //background-color: #fff;
          width: 100%;
        }
      }
      .apply-list-title{
        color: #101010;
        font-size: 16px;
        font-weight: 600;
      }
      .apply-list-desc{
        font-size: 13px;
        //color: #c2c3c4;
        color: #333;
        margin-top: 12px;
        margin-bottom: 10px;
        line-height: 1.5;
        height: 70px;
        display: flex;
        //align-items: center;
      }
      .apply-list-config{
        //padding: 0 12px;
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
}
</style>
