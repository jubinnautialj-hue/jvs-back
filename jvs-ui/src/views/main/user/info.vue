<template>
  <div class="account-info-setting">
    <div class="account-menu-box">
      <div v-for="item in tabList" :key="item.name" :class="{'menu-item': true, 'active': switchStatus == item.name}" @click="switchTab(item)">
        <svg class="icon" aria-hidden="true">
          <use :xlink:href="(switchStatus == item.name) ? item.activeIcon: item.icon"></use>
        </svg>
        <span>{{$langt(`common.rightDrawer.accountSetting.${item.name}`) || item.label}}</span>
      </div>
    </div>
    <div class="menu-item-info-box">
      <div class="user-box">
        <div class="user-title">{{$langt(`common.rightDrawer.accountSetting.${switchStatus}Column.${switchStatus}Title`)}}</div>
        <div v-if="switchStatus == 'user'">
          <div class="user-item">
            <div class="user-item-name">{{$langt(`common.rightDrawer.accountSetting.${switchStatus}Column.avatar`) || '头像'}}</div>
            <div class="user-item-content">
              <el-upload
                ref="headImgUpload"
                class="avatar-uploader"
                action="/mgr/jvs-auth/upload/jvs-public"
                list-type="picture"
                accept=".jpg,.jpeg,.png"
                :file-list="imgFileList"
                :show-file-list="false"
                :data="{module: '/jvs-ui/userheadimg/'}"
                :on-remove="headImgRemove"
                :headers="headers"
                :before-upload="beforeUploadUpload"
                :on-success="headImgSuccessHandle"
                :on-error="headImgErrHandle"
              >
                <img v-if="userdata.headImg" :src="userdata.headImg" class="avatar">
                <i v-else class="el-icon-picture-outline" style="font-size:18px;"></i>
              </el-upload>
            </div>
          </div>
          <div class="user-item">
            <div class="user-item-name">{{$langt(`common.rightDrawer.accountSetting.${switchStatus}Column.nickName`) || '昵称'}}</div>
            <div class="user-item-content">
              <el-input ref="editName" v-if="editNameShow" v-model="realName" size="mini" @blur="realNameEdit" class="user-name-input"></el-input>
              <span v-else>
                <span @dblclick="editRealName">{{userdata.realName}}</span>
                <svg class="edit-name-icon" aria-hidden="true" @click="editRealName">
                  <use xlink:href="#icon-jvs-bianji1"></use>
                </svg>
              </span>
              
            </div>
          </div>
          <div v-for="(item,index) in userList" :key="index" class="user-item">
            <div class="user-item-name">{{$langt(`common.rightDrawer.accountSetting.${switchStatus}Column.${item.props || 'rolePermision'}`) || item.label}}</div>
            <div class="user-item-content">
              <span>{{(userdata && item.props && userdata[item.props]) || '--'}}</span>
            </div>
          </div>
          <div>
            <el-button type="primary" class="submit-button" :loading="saveUserLoading" @click="saveUserInfo">{{$langt(`common.rightDrawer.accountSetting.button.saveInfo`) || '保存信息'}}</el-button>
          </div>
        </div>
        <div v-if="switchStatus == 'account'">
          <div class="s-title">{{$langt(`common.rightDrawer.accountSetting.${switchStatus}Column.bind`) || '绑定账号'}}</div>
          <div class="bind phone-box">
            <div class="title">{{$langt(`common.rightDrawer.accountSetting.${switchStatus}Column.bindPhone`) || '绑定手机号'}}</div>
            <div class="bind-input">
              <el-input :placeholder="$langt(`common.rightDrawer.accountSetting.${switchStatus}Column.phonePlaceholder`) || '请输入手机号'" v-model="phoneYzm.value" :readonly="!!phoneYzm.phone" @input="bindInputChange('phone')"></el-input>
              <el-button type="primary" @click="bindOrNext('phone')" :class="[phoneYzm.phone&&'unbind']" v-if="phoneYzm.step==1"
              :disabled="!phoneYzm.phone && !isMobile(phoneYzm.value)">{{phoneYzm.phone?($langt(`common.rightDrawer.accountSetting.button.unbind`) || '解绑'):($langt(`common.rightDrawer.accountSetting.button.next`) || '下一步')}}</el-button>
            </div>
            <div class="bind-input" v-if="phoneYzm.step==2&& phoneYzm.value">
              <el-input :placeholder="$langt(`common.rightDrawer.accountSetting.${switchStatus}Column.vcPlaceholder`) || '请输入验证码'"  v-model="phoneYzm.yzm"></el-input>
              <el-button class="send-button" @click="sendYzm('phone')">{{phoneYzm.text}}</el-button>
            </div>
            <div class="buttons-box" v-if="phoneYzm.step==2&& phoneYzm.value">
              <el-button type="primary" :disabled="!phoneYzm.yzm" @click="bindPhoneOrEmail('phone')">{{$langt(`common.rightDrawer.accountSetting.button.bind`) || '绑定'}}</el-button>
              <el-button class="cancal" @click="cancalUnBind('phone')">{{$langt(`common.rightDrawer.accountSetting.button.cancel`) || '取消'}}</el-button>
            </div>
          </div>
          <div class="bind email-box">
            <div  class="title">{{$langt(`common.rightDrawer.accountSetting.${switchStatus}Column.bindEmail`) || '绑定邮箱'}}</div>
              <div class="bind-input">
                <el-input :placeholder="$langt(`common.rightDrawer.accountSetting.${switchStatus}Column.emailPlaceholder`) || '请输入邮箱'" v-model="emailYzm.value" :readonly="!!emailYzm.email"  @input="bindInputChange('email')"></el-input>
                <el-button type="primary" :class="[emailYzm.email&&'unbind']" @click="bindOrNext('email')"  v-if="emailYzm.step==1"
                :disabled="!phoneYzm.email && !isEmail(emailYzm.value)">{{emailYzm.email?($langt(`common.rightDrawer.accountSetting.button.unbind`) || '解绑'):($langt(`common.rightDrawer.accountSetting.button.next`) || '下一步')}}</el-button>
              </div>
              <div class="bind-input" v-if="emailYzm.step==2 && emailYzm.value">
                <el-input :placeholder="$langt(`common.rightDrawer.accountSetting.${switchStatus}Column.vcPlaceholder`) || '请输入验证码'" v-model="emailYzm.yzm"></el-input>
                <el-button class="send-button" @click="sendYzm('email')">{{emailYzm.text}}</el-button>
              </div>
              <div class="buttons-box" v-if="emailYzm.step==2 && emailYzm.value">
                <el-button type="primary" :disabled="!emailYzm.yzm" @click="bindPhoneOrEmail('email')">{{$langt(`common.rightDrawer.accountSetting.button.bind`) || '绑定'}}</el-button>
                <el-button class="cancal" @click="cancalUnBind('email')">{{$langt(`common.rightDrawer.accountSetting.button.cancel`) || '取消'}}</el-button>
              </div>
          </div>
          <div class="s-title">{{$langt(`common.rightDrawer.accountSetting.${switchStatus}Column.bindThird`) || '绑定第三方账号'}}</div>
            <div class="other-bind">
              <div class="other-item" v-for="(item,index) in otherList" :key="index">
                <svg class="other-icon">
                  <use v-bind:xlink:href="`#${item.icon}`"></use>
                </svg>
                <span class="label">{{$langt(`common.rightDrawer.accountSetting.${switchStatus}Column.${item.props}`) || item.label}}</span>
                <el-button :class="{'binding': exceptions[item.props]}"  @click="bindStatusChange(item)">{{exceptions[item.props]?($langt(`common.rightDrawer.accountSetting.button.unbind`) || '解绑'):($langt(`common.rightDrawer.accountSetting.button.bind`) || '绑定')}}</el-button>
              </div>
            </div>
        </div>
        <div v-if="switchStatus == 'password'" class="password-box">
          <div class="tips">{{$langt(`common.rightDrawer.accountSetting.${switchStatus}Column.passText`) || '设置登录密码'}}</div>
          <el-input class="pwd-input" :placeholder="$langt(`common.rightDrawer.accountSetting.${switchStatus}Column.passPlaceholder`) || '请输入密码'" type="password" v-model="pwd"></el-input>
          <el-input class="pwd-input" :placeholder="$langt(`common.rightDrawer.accountSetting.${switchStatus}Column.surePlaceholder`) || '确认密码'" type="password" v-model="pwds"></el-input>
          <div class="buttons-box">
            <el-button type="primary" :disabled="!(pwd && pwds)" :loading="editPassLoading" @click="submitPwd">{{$langt(`common.rightDrawer.accountSetting.button.confirm`) || '确定'}}</el-button>
            <el-button class="cancal" @click="cancalPwd">{{$langt(`common.rightDrawer.accountSetting.button.cancel`) || '取消'}}</el-button>
          </div>
        </div>
      </div>
    </div>

    <el-dialog
      :visible.sync="bindVisible"
      width="475px"
      append-to-body
      :show-close="false"
      :close-on-click-modal="false"
      class="user-info-bind-dialog-wrapper"
      :customClass="`bind-dialog ${bindItem.props=='LDAP' ? 'LDAP-dialog': ''}`">
      <div v-if="bindVisible">
        <div class="bind-dialog-header">
          <div class="bind-title">{{$langt(`common.rightDrawer.accountSetting.button.bind`)+' '}}{{$langt(`common.rightDrawer.accountSetting.accountColumn.${bindItem.props}`)}}</div>
          <svg class="userInfo-close" @click="handleBindClose">
            <use xlink:href="#icon-jvs-danchuangguanbi1"></use>
          </svg>
        </div>
        <div class="bind-body">
          <div class="qcode-box">
            <div class="qcode-tips" v-if="bindItem.props != 'LDAP'">{{$langt(`common.rightDrawer.accountSetting.pleaseUse`)}}{{bindItem.props=='WECHAT_MP' ? $langt(`common.rightDrawer.accountSetting.accountColumn.${bindItem.props}`) : bindItem.props=='Ding'? $langt(`common.rightDrawer.accountSetting.accountColumn.${bindItem.props}`) : $langt(`common.rightDrawer.accountSetting.accountColumn.WX_ENTERPRISE`)}}{{$langt(`common.rightDrawer.accountSetting.scanCode`)}}{{bindItem.props == 'wxmp' ? $langt(`common.rightDrawer.accountSetting.followOff`) : ''}}</div>
            <div class="qcode-body-box" v-if="bindItem.props!='LDAP'">
              <img id="weixinCode"  @click="freshWeixin" :src="OfficQrcodeUrl" v-if="bindItem.props == 'WECHAT_MP' && OfficQrcodeUrl" scrolling="no"  frameborder="0">
              <div class="qcode-mark" v-if="bindItem.props == 'wxmp' && isExpires" @click="freshWeixin">
                <div class="tips">{{$langt(`common.login.freshTip`)}}</div>
              </div>
              <div class="iframe-box" v-if="hasLoginRight(bindItem.props,['Ding','WX_ENTERPRISE'])">
                <!-- 钉钉扫码登录 -->
                <iframe id="ddLogin" class="dd-box" v-if="bindItem.props == 'Ding' && ddCodeUrl" :src="ddCodeUrl"  scrolling="no" frameborder="0"></iframe>
                <!-- 企业微信扫码登录 -->
                <iframe id="wxenterpriseCode" class="wxenter-box" v-if="bindItem.props == 'WX_ENTERPRISE'" :src="`/auth/just/login/WECHAT_ENTERPRISE?client_id=${client_id}&url=`+ callbackUrl" scrolling="no"  frameborder="0"></iframe>
              </div>
            </div>
            <div class="ldap-box" v-else>
              <el-form ref="ldapFormRef" :model="ldapForm" :rules="ldapRules" label-position="top">
                <el-form-item :label="$langt(`common.rightDrawer.accountSetting.ldapColumn.account.label`)" prop="account">
                  <el-input v-model="ldapForm.account" :placeholder="$langt(`common.rightDrawer.accountSetting.ldapColumn.account.placeholder`)"/>
                </el-form-item>
                <el-form-item :label="$langt(`common.rightDrawer.accountSetting.ldapColumn.password.label`)" prop="password">
                  <el-input v-model="ldapForm.password" :placeholder="$langt(`common.rightDrawer.accountSetting.ldapColumn.password.placeholder`)" type="password"/>
                </el-form-item>
              </el-form>
            </div>
          </div>
          <div v-if="bindItem.props == 'LDAP'" class="dialog__footer">
            <div class="button-box">
              <el-button class="cancal" @click="handleBindClose">{{$langt(`form.cancel`)}}</el-button>
              <el-button type="primary" @click="bindLADP">{{$langt(`common.rightDrawer.accountSetting.button.bind`)}}</el-button>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>


<script>
import { editUser } from './api'
import { isMobile, isEmail } from "@/util/validate"
import { getUserInfo, sendPhoneCode, sendEmailCode, bindPhone, unbindPhone, bindEmail, unbindEmail, bindWX, unbindWX, bindLDAP, unbindLDAP, unbindQW, unbindDD, setPassWord } from "@/api/admin/user";
import { getOffLoginQcode, checkQrcodeState, getInfoByLoginType  } from '@/api/login'
import { encryption } from "@/util/util";
import { enCodeKey, client_id, enCodePasswordKey } from "@/const/const"
import { mapState } from 'vuex'
import store from "@/store";
export default {
  computed: {
    ...mapState({
      userInfo: state => state.user.userInfo
    }),
  },
  data () {
    return {
      client_id: client_id,
      callbackUrl: '',
      ddCodeUrl: '',
      ddInfo: {
        appId: ''
      },
      switchStatus: 'user',
      tabList: [
        {label: '个人信息', name: 'user', icon: '#icon-jvs-a-gerenxinxi-xuanzhong1', activeIcon: '#icon-jvs-gerenxinxi-xuanzhong'},
        {label: '账号管理', name: 'account', icon: '#icon-jvs-zhanghuguanli-weixuanzhong', activeIcon: '#icon-jvs-zhanghuguanli-xuanzhong'},
        {label: '设置密码', name: 'password', icon: '#icon-jvs-shezhimima-weixuanzhong', activeIcon: '#icon-jvs-shezhimima-xuanzhong'}
      ],
      userdata: {},
      editNameShow: false,
      realName: '',
      imgFileList: [],
      headers: {
        tenantId: this.$store.getters.userInfo.tenantId,
        Authorization: ('Bearer '+this.$store.getters.access_token)
      },
      userList: [
        { label:'角色权限', props:'' },
        { label:'部门', props:'deptName' },
        { label:'职工编号', props:'employeeNo' },
        { label:'等级', props:'level' },
        { label:'岗位', props:'jobName' }
      ],
      saveUserLoading: false,
      exceptions: {},
      isMobile: isMobile,
      isEmail: isEmail,
      pwd:'',
      pwds:'',
      phoneYzm:{
        step:1,
        text: this.$langt(`common.rightDrawer.accountSetting.button.code`),
        phone:'',
        value:'',
        yzm:'',
        time: 180,
        disabled: false
      },
      emailYzm:{
        step:1,
        text: this.$langt(`common.rightDrawer.accountSetting.button.code`),
        email:'',
        value:'',
        yzm:'',
        time: 180,
        disabled: false
      },
      otherList: [
        {
          icon:'icon-jvs-weixin-2',
          label:'微信',
          props:"WECHAT_MP"
        },
        {
          icon:'icon-jvs-qiyeweixin',
          label:'企业微信',
          props:"WX_ENTERPRISE"
        },
        {
          icon:'icon-jvs-dingding',
          label:'钉钉',
          props:"Ding"
        },
        {
          icon:'icon-jvs-LADP',
          label:'LDAP',
          props:"LDAP"
        }
      ],
      bindVisible: false,
      bindItem: {
        icon: '',
        label: '',
        props: ''
      },
      phoneYzmInterval: null,
      emailYzmInterval: null,
      QRCheckSetInterval: null,
      OfficQrcodeUrl:'',
      isExpires:false,
      QRcodeUUId:'',
      ldapForm: {
        account: '',
        passowrd: ''
      },
      ldapRules: {
        account:[{ required: true, message: this.$langt(`common.rightDrawer.accountSetting.ldapColumn.account.placeholder`), trigger: "blur" }],
        password:[{ required: true, message: this.$langt(`common.rightDrawer.accountSetting.ldapColumn.password.placeholder`), trigger: "blur" }]
      },
      editPassLoading: false
    }
  },
  created () {
    this.callbackUrl = location.origin + '/%23/login/callback?back=bind'
    this.getUserInfo()
    // 监听钉钉扫码
    if (typeof window.addEventListener != 'undefined') {
      window.addEventListener('message', this.dingdingBackHandle, false);
    } else if (typeof window.attachEvent != 'undefined') {
      window.attachEvent('onmessage', this.dingdingBackHandle);
    }
  },
  methods: {
    switchTab (tab) {
      this.switchStatus = tab.name
    },
    headImgRemove (file, fileList) {
      this.imgFileList = fileList
      this.$set(this.userdata, 'headImg', '')
    },
    beforeUploadUpload(file){
      let ocrFileType = ['png','jpeg','jpg']
      let fileType = file.name.split(".").slice(-1)[0].toLowerCase()
      if(!ocrFileType.includes(fileType)){
        this.$notify({
          title: this.$langt('common.tip'),
          message: this.$langt(`common.rightDrawer.accountSetting.imgValid`),
          type: 'warning',
          position: 'bottom-right'
        });
        return false
      }
      return true
    },
    headImgSuccessHandle (res, file, fileList) {
      if(res.code == 0 && res.data && res.data.fileLink) {
        this.$set(this.userdata, 'headImg', res.data.fileLink)
        this.imgFileList = [{
          url: res.data.fileLink
        }]
      }else{
        this.$refs.headImgUpload.clearFiles()
        this.$notify({
          title: this.$langt('common.tip'),
          message: res.msg,
          position: 'bottom-right',
          type: 'error'
        });
      }
    },
    headImgErrHandle (err, file, fileList) {
      this.$refs.headImgUpload.clearFiles()
      this.$notify({
        title: this.$langt('common.tip'),
        message: err,
        position: 'bottom-right',
        type: 'error'
      });
    },
    // 刷新token
    freshToken () {
      store.dispatch('RefreshToken', store.getters.tenantId).then(res => {
        window.parent.postMessage({command: 'fresh'}, '*')
      }).catch(e => {
        store.dispatch("LogOut").then(() => {
          sessionStorage.setItem('lastUrl', lastUrl)
          router.push({ path: "/login" });
          window.parent.postMessage({command: 'loginOut'}, '*')
        });
      })
    },
    // 用户信息
    getUserInfo () {
      getUserInfo().then(res => {
        if(res.data.code == 0) {
          this.$set(this, 'userdata', res.data.data)
          this.accountName = this.userdata.accountName
          this.realName = this.userdata.realName
          if(this.userdata.phone) {
            this.$set(this.phoneYzm, 'phone', this.userdata.phone)
            this.$set(this.phoneYzm, 'value', this.userdata.phone)
          }
          if(this.userdata.email) {
            this.$set(this.emailYzm, 'email', this.userdata.email)
            this.$set(this.emailYzm, 'value', this.userdata.email)
          }
          this.exceptions = this.userdata.exceptions
        }
      })
    },
    editRealName () {
      this.editNameShow = true
      this.$nextTick(() => {
        this.$refs.editName.focus()
      })
    },
    realNameEdit () {
      if(this.realName) {
        this.editNameShow = false
      }
      if(this.realName && this.realName != this.userdata.realName) {
        this.$set(this.userdata, 'realName', this.realName)
      }
    },
    saveUserInfo () {
      this.saveUserLoading = true
      let temp = JSON.parse(JSON.stringify(this.userdata))
      if(temp.sex) {
        delete temp.sex
      }
      editUser(temp).then(res =>{
        if(res.data && res.data.code == 0) {
          this.$notify({
            title: this.$langt('common.tip'),
            message: this.$langt('common.saveSuccess'),
            position: 'bottom-right',
            type: 'success'
          });
          this.getUserInfo()
          this.saveUserLoading = false
          this.freshToken()
        }else{
          this.saveUserLoading = false
        }
      }).catch(e => {
        this.saveUserLoading = false
      })
    },
    bindInputChange (type) {
      if(type=='phone'){
        if(!this.phoneYzm.value){
          this.phoneYzm.step = 1
        }
      }else{
        if(!this.emailYzm.value){
          this.emailYzm.step = 1
        }
      }
    },
    bindOrNext (type) {
      let str = this.$langt(`common.rightDrawer.accountSetting.unbPhone`)
      let func = unbindPhone
      let label = this.$langt(`common.rightDrawer.accountSetting.phone`)
      let isBind = !!this.phoneYzm.phone
      if(type=='email'){
        func = unbindEmail
        str = this.$langt(`common.rightDrawer.accountSetting.unbEmail`)
        label = this.$langt(`common.rightDrawer.accountSetting.email`)
        isBind = !!this.emailYzm.email
      }
      if(!isBind){
        if(type=='phone' && this.phoneYzm.value){
          this.phoneYzm.step = 2
        }else if(this.emailYzm.value){
          this.emailYzm.step = 2
        }
      }else{
        this.allUnBind(str,func,label)
      }
    },
    sendYzm (type) {
      if(type=='phone'){
        if(this.phoneYzm.disabled) return
        this.phoneYzmInterval = setInterval(()=>{
          this.phoneYzm.time -= 1
          this.phoneYzm.text = this.phoneYzm.time + this.$langt(`common.rightDrawer.accountSetting.regain`)
          this.phoneYzm.disabled = true
          if(this.phoneYzm.time == 0){
            clearInterval(this.phoneYzmInterval)
            this.phoneYzm.time = 180
            this.phoneYzm.text = this.$langt(`common.rightDrawer.accountSetting.button.code`)
            this.phoneYzm.disabled = false
          }
        },1000)
        sendPhoneCode(this.phoneYzm.value).then(res => {}).catch(err => {
          if(this.phoneYzmInterval) {
            clearInterval(this.phoneYzmInterval)
          }
          this.phoneYzm.time = 180
          this.phoneYzm.text = this.$langt(`common.rightDrawer.accountSetting.button.code`)
          this.phoneYzm.disabled = false
        })
      }else{
        if(this.emailYzm.disabled) return
        this.emailYzmInterval = setInterval(()=>{
          this.emailYzm.time -= 1
          this.emailYzm.text = this.emailYzm.time + this.$langt(`common.rightDrawer.accountSetting.regain`)
          this.emailYzm.disabled = true
          if(this.emailYzm.time == 0){
            clearInterval(this.emailYzmInterval)
            this.emailYzm.time = 180
            this.emailYzm.text = this.$langt(`common.rightDrawer.accountSetting.button.code`)
            this.emailYzm.disabled = false
          }
        },1000)
        sendEmailCode({email: this.emailYzm.value}).then(res => {}).catch(err => {
          if(this.emailYzmInterval) {
            clearInterval(this.emailYzmInterval)
          }
          this.emailYzm.time = 180
          this.emailYzm.text = this.$langt(`common.rightDrawer.accountSetting.button.code`)
        })
      }
    },
    bindPhoneOrEmail (type) {
      let obj = {
        email: this.emailYzm.value,
        code: this.emailYzm.yzm
      }
      let func = bindEmail
      let str = this.$langt(`common.rightDrawer.accountSetting.emailSuccess`)
      if(type=='phone'){
        func = bindPhone
        str = this.$langt(`common.rightDrawer.accountSetting.phoneSuccess`)
        obj = {
          phone: this.phoneYzm.value,
          code: this.phoneYzm.yzm
        }
      }
      func(obj).then(res=>{
        if(res.data.code == 0) {
          this.$notify({
            title: this.$langt('common.tip'),
            message: str,
            type: 'success',
            position: 'bottom-right'
          });
          this.getUserInfo()
          this.freshToken()
        }
      })
    },
    cancalUnBind (type) {
      this.resetYzm(type)
    },
    resetYzm (types) {
      if(this.phoneYzmInterval && types.indexOf('phone')!=-1){
        clearInterval(this.phoneYzmInterval)
      }
      if(this.emailYzmInterval&& types.indexOf('email')!=-1){
        clearInterval(this.emailYzmInterval)
      }
      if(types.indexOf('phone')!=-1){
        this.phoneYzm = {
          step:1,
          text: this.$langt(`common.rightDrawer.accountSetting.button.code`),
          phone:'',
          value:'',
          yzm:'',
          time: 180,
          disabled: false
        }
      }
      if(types.indexOf('email')!=-1){
        this.emailYzm = {
          step:1,
          text: this.$langt(`common.rightDrawer.accountSetting.button.code`),
          email:'',
          yzm:'',
          value:'',
          time: 180,
          disabled: false
        }
      }
    },
    bindStatusChange (item) {
      if(this.exceptions[item.props]){
        let func = null
        let str = ''
        switch(item.props) {
          case 'WECHAT_MP':
            str = "即将解除微信绑定，是否继续？"
            func = unbindWX
            break;
          case 'WX_ENTERPRISE':
            str = "即将解除企业微信绑定，是否继续？"
            func = unbindQW
            break;
          case 'Ding':
            str = "即将解除钉钉绑定，是否继续？"
            func = unbindDD
            break;
          case 'LDAP':
            str = "即将解除LDAP绑定，是否继续？"
            func = unbindLDAP
            break;
          default:
            break;
        }
        str = this.$langt(`common.rightDrawer.accountSetting.unbStrStart`) + this.$langt(`common.rightDrawer.accountSetting.accountColumn.${item.props}`) + this.$langt(`common.rightDrawer.accountSetting.unbStrEnd`)
        this.allUnBind(str,func,item.label)
      }else{
        if(item.props=='Ding'){
          this.getDDCode()
        }else if(item.props=='WECHAT_MP'){
          this.getOffLoginQcodeF()
        }
        this.bindItem = item
        this.bindVisible = true
      }
    },
    allUnBind (str, func, label) {
      this.$confirm(str, this.$langt('common.tip'), {
        confirmButtonText: this.$langt('common.confirm'),
        cancelButtonText: this.$langt('common.cancel'),
        type: 'warning'
      }).then(() => {
        if(func) {
          func().then( (res) =>{
            if(res.data.code == 0) {
              this.$notify({
                title: this.$langt('common.tip'),
                message: this.$langt(`common.rightDrawer.accountSetting.unbSuccess`) + label,
                type: 'success',
                position: 'bottom-right'
              })
              this.getUserInfo()
              this.freshToken()
            }
          })
        }
      }).catch(e => {})
    },
    handleBindClose () {
      if(this.QRCheckSetInterval) {
        clearInterval(this.QRCheckSetInterval)
      }
      this.bindVisible = false
    },
    freshWeixin () {
      this.getOffLoginQcodeF()
    },
    getOffLoginQcodeF () {
      let uuid = this.getUUId()
      let uuidNew = []
      uuid.split('').forEach(item => {
        if(item!='-'){
          uuidNew.push(item)
        }
      })
      this.QRcodeUUId = uuidNew.join('')
      getOffLoginQcode(this.QRcodeUUId).then( res =>{
        if(res.data.code==0) {
          this.OfficQrcodeUrl = res.data.data
          this.isExpires = false
          this.startCheckState()
        }
      })
    },
    getUUId () {
      return URL.createObjectURL(new Blob()).substr(-36)
    },
    startCheckState () {
      if(this.QRCheckSetInterval) {
        clearInterval(this.QRCheckSetInterval)
      }
      this.QRCheckSetInterval = setInterval(()=> {
        checkQrcodeState(this.QRcodeUUId).then(res=> {
          if(res.data.code == 0 && res.data.data.checkStatus) {
            if(this.QRCheckSetInterval) {
              clearInterval(this.QRCheckSetInterval)
            }
            let tp = {
              data: JSON.stringify({id: this.QRcodeUUId})
            }
            let temp = encryption({
              data: tp,
              key: enCodeKey,
              param: ["data"]
            })
            bindWX({ code: temp.data, appId: client_id }).then(res=> {
              if(res.data.code == 0) {
                this.$notify({
                  title: this.$langt('common.tip'),
                  message: this.$langt(`common.rightDrawer.accountSetting.wxbindSuccess`),
                  type: 'success',
                  position: 'bottom-right'
                })
                this.getUserInfo()
                this.freshToken()
                this.handleBindClose()
              }else{
                this.OfficQrcodeUrl = ''
                this.freshWeixin()
              }
            }).catch(() => {
              this.OfficQrcodeUrl = ''
              this.freshWeixin()
            })
          }else if(res.data.code == 0 && res.data.data.isPastDue){
            this.isExpires = true
            if(this.QRCheckSetInterval) {
              clearInterval(this.QRCheckSetInterval)
            }
          }
        })
      },1000 * 2)
    },
    hasLoginRight (data, checkData, isAll) {
      if(typeof data == 'object') {
        if(typeof checkData == 'object'){
          let sum = 0
          checkData.forEach((itemd)=>{
            if(data.indexOf(itemd) != -1){
              sum ++
            }
          })
          if(isAll){
            return sum == checkData.length()
          }else{
            return !!sum
          }
        }else{
          return data.indexOf(checkData) != -1
        }
      }else{
        if(typeof checkData == 'object') {
          return checkData.indexOf(data) != -1
        }else{
          return checkData == data
        }
      }
    },
    getDDCode () {
      getInfoByLoginType({type: 'dd'}).then(res => {
        if(res && res.data && res.data.code == 0) {
          this.ddInfo = res.data.data
          let url = encodeURIComponent(`${location.origin}/#/login/dingtalk/bindback`);
          let goto = encodeURIComponent(`https://oapi.dingtalk.com/connect/oauth2/sns_authorize?appid=${res.data.data.appId}&response_type=code&scope=snsapi_login&state=STATE&redirect_uri=${url}`)
          this.ddCodeUrl = `https://login.dingtalk.com/login/qrcode.htm?goto=${goto}&style=border:none;background-color:#FFFFFF;`
        }
      })
    },
    dingdingBackHandle (event) {
      var origin = event.origin;
      //判断是否来自ddLogin扫码事件。
      if(origin == 'https://login.dingtalk.com') {
        var loginTmpCode = event.data;
        //获取到loginTmpCode后就可以在这里构造跳转链接进行跳转了
        let url = encodeURIComponent(`${location.origin}/#/login/dingtalk/bindback`);
        let goto = encodeURIComponent(`https://oapi.dingtalk.com/connect/oauth2/sns_authorize?appid=${this.ddInfo.appId}&response_type=code&scope=snsapi_login&state=STATE&redirect_uri=${url}`)
        this.$openUrl(`https://oapi.dingtalk.com/connect/oauth2/sns_authorize?appid=${this.ddInfo.appId}&response_type=code&scope=snsapi_login&state=STATE&redirect_uri=${goto}&loginTmpCode=${loginTmpCode}`, '_self')
      }
    },
    bindLADP () {
      if(this.$refs.ldapFormRef) {
        this.$refs.ldapFormRef.validate((valid) => {
          if(valid) {
            let userInfo = JSON.parse(JSON.stringify(this.ldapForm))
            let tp = {
              data: JSON.stringify(userInfo)
            }
            let temp = encryption({
              data: tp,
              key: enCodeKey,
              param: ["data"]
            })
            bindLDAP({ code: temp.data, appId: client_id }).then(res => {
              if(res.data.code == 0) {
                this.$notify({
                  title: this.$langt('common.tip'),
                  message: this.$langt(`common.rightDrawer.accountSetting.ldapSuccess`),
                  type: 'success',
                  position: 'bottom-right'
                });
                this.getUserInfo()
                this.freshToken()
                this.handleBindClose()
              }
            }).catch(() => {})
          }
        })
      }
    },
    submitPwd () {
      if(this.pwd == this.pwds){
        let obj = {
          password: this.pwd,
          rePassword: this.pwds
        }
        let temp = encryption({
          data: obj,
          key: enCodePasswordKey,
          param: ['password']
        })
        temp = encryption({
          data: temp,
          key: enCodePasswordKey,
          param: ['rePassword']
        })
        this.editPassLoading = true
        setPassWord(temp).then(res => {
          if(res.data.code == 0) {
            this.$notify({
              title: this.$langt('common.tip'),
              message: this.$langt(`common.rightDrawer.accountSetting.passSuccess`),
              type: 'success',
              position: 'bottom-right'
            });
            this.editPassLoading = false
          }else{
            this.editPassLoading = false
          }
        }).catch(() => {
          this.editPassLoading = false
        })
      }else{
        this.$confirm(this.$langt(`common.rightDrawer.accountSetting.secondValid`), this.$langt('common.tip'), {
          confirmButtonText: this.$langt('common.confirm'),
          cancelButtonText: this.$langt('common.cancel'),
          type: 'warning'
        })
      }
    },
    cancalPwd () {
      this.pwd = ''
      this.pwds = ''
    },
  }
}
</script>
<style lang="scss" scoped>
.account-info-setting {
  width: 100%;
  height: 100%;
  display: flex;
  border-top: 1px solid #EEEFF0;
  box-sizing: border-box;
  overflow: hidden;
  .account-menu-box{
    width: 168px;
    background: #F5F6F7;
    padding: 23px 8px;
    box-sizing: border-box;
    .menu-item{
      padding: 5px 0;
      min-height: 36px;
      border-radius: 4px;
      color: #363b4c;
      font-size: 14px;
      font-family: Source Han Sans-Regular, Source Han Sans;
      display: flex;
      align-items: center;
      padding-left: 18px;
      box-sizing: border-box;
      cursor: pointer;
      .icon{
        width: 16px;
        height: 16px;
        margin-right: 9px;
      }
      &.active{
        background: #DDEAFF;
        color: #1e6fff;
      }
      &:not(.active):hover{
        background: #EEEFF0;
      }
      &+.menu-item{
        margin-top: 16px;
      }
    }
  }
  .menu-item-info-box{
    width: calc(100% - 168px);
    height: 100%;
    overflow: auto;
    padding: 24px;
    box-sizing: border-box;
    .user-box{
      .user-title{
        color: #363b4c;
        font-size: 16px;
        font-family:  Source Han Sans-Bold, Source Han Sans;
      }
      .user-item{
        display: flex;
        align-items: center;
        margin: 16px 0;
        .user-item-name{
          width: 64px;
          font-size: 14px;
          color: #6f7588;
          font-family: Source Han Sans-Regular, Source Han Sans;
        }
        .user-item-content{
          margin-left: 32px;
          font-size: 14px;
          color: #363b4c;
          font-family: Source Han Sans-Regular, Source Han Sans;
          display: flex;
          align-items: center;
          .avatar-uploader{
            cursor: pointer;
            overflow: hidden;
            .el-upload{
              border: 0;
            }
            .avatar{
              width: 56px;
              height: 56px;
              border-radius: 10px;
              overflow: hidden;
            }
          }
          .edit-name-icon{
            margin-left: 8px;
            width: 16px;
            height: 16px;
            cursor: pointer;
          }
          .user-name-input{
            .el-input__inner{
              height: 30px;
              background: #F5F6F7;
              border-radius: 6px;
              border: 0;
              font-size: 14px;
              color: #363B4C;
              font-family: Source Han Sans-Regular, Source Han Sans;
            }
          }
        }
      }
      .submit-button{
        margin-top: 16px;
        height: 32px;
        line-height: 32px;
        font-size: 13px;
        padding: 0 15px;
      }
      .s-title{
        color: #363b4c;
        font-family:  Source Han Sans-Bold, Source Han Sans;
        font-size: 14px;
        margin-top: 24px;
      }
      .bind{
        margin-top: 16px;
        margin-bottom: 8px;
        .title{
          font-size: 14px;
          color: #363b4c;
          font-family: Source Han Sans-Regular, Source Han Sans;
        }
        .bind-input{
          display: flex;
          align-items: center;
          margin-top: 8px;
          .el-input{
            height: 36px;
            width: 320px;
            margin-right: 16px;
            .el-input__inner{
              border: 0;
              background: #F5F6F7;
            }
          }
          .el-button{
            height: 36px;
            font-family: Source Han Sans-Regular, Source Han Sans;
          }
          .unbind{
            background: #F5F6F7;
            border-radius: 6px;
            border-color: #F5F6F7;
            color: #1E6FFF;
            font-size: 14px;
            &:hover{
              background: #EEEFF0;
              border-color: #EEEFF0;
            }
          }
          .send-button{
            background: #F5F6F7;
            border-radius: 6px;
            border-color: #F5F6F7;
            color: #1E6FFF;
            font-size: 14px;
            width: 102px;
            padding: 0;
            &:hover{
              background: #EEEFF0;
              border-color: #EEEFF0;
            }
          }
        }
        .buttons-box{
          margin-top: 12px;
          .cancal{
            background: #F5F6F7;
            border-color: #F5F6F7;
            color: #494f6A;
            &:hover{
              background: #EEEFF0;
              border-color: #EEEFF0;
              color: #494f6A;
            }
          }
        }
        .bind-input+.bind-input{
          margin-top: 12px;
        }
      }
      .other-bind{
        margin-top: 16px;
        .other-item{
          display: flex;
          align-items: center;
          font-family: Source Han Sans-Regular, Source Han Sans;
          .other-icon{
            width: 32px;
            height: 32px;
          }
          .label{
            margin-left: 16px;
            width: 60px;
            font-size: 14px;
            color: #363B4C;
          }
          button{
            margin-left: 32px;
            background: #F5F6F7;
            border-color: #F5F6F7;
            color: #363B4C;
            &:hover{
              color: #363B4C;
              background: #EEEFF0;
            }
          }
          .binding{
            color: #1E6FFF;
            &:hover{
              color: #1E6FFF;
            }
          }
        }
        .other-item+.other-item{
          margin-top: 16px;
        }
      }
      .pwd-input{
        width: 320px;
        height: 36px;
        margin-top: 12px;
        background: #F5F6F7;
        border-radius: 6px;
        .el-input::placeholder{
          color: #6F7588;
          font-family: Source Han Sans-Regular, Source Han Sans;
        }
        .el-input__inner{
          background-color: transparent;
          border: 0;
          font-family: Source Han Sans-Regular, Source Han Sans;
          color: #6F7588;
        }
      }
      .tips{
        color: #363B4C;
        font-size: 14px;
        margin-top: 16px;
        margin-bottom: 4px;
        font-family: Source Han Sans-Regular, Source Han Sans;
      }
      .buttons-box{
        margin-top: 12px;
        .el-button{
          height: 32px;
          padding: 0 15px;
          line-height: 32px;
          font-size: 13px;
        }
        .cancal{
          background: #F5F6F7;
          border-color: #F5F6F7;
          color: #494f6A;
          &:hover{
            background: #EEEFF0;
            border-color: #EEEFF0;
            color: #494f6A;
          }
        }
      }
    }
    .password-box{
      .pwd-input{
        width: 320px;
        height: 36px;
        margin-top: 12px;
        background: #F5F6F7;
        border-radius: 6px 6px 6px 6px;
        /deep/.el-input__inner{
          background-color: transparent !important;
          border: 0;
          font-family: Source Han Sans-Regular, Source Han Sans;
        }
      }
      .tips{
        color: #363B4C;
        font-size: 14px;
        margin-top: 16px;
        margin-bottom: 4px;
        font-family: Source Han Sans-Regular, Source Han Sans;
      }
      .buttons-box{
        margin-top: 12px;
        .el-button{
          padding: 0 15px;
          font-size: 13px;
          font-family: Source Han Sans-Regular, Source Han Sans;
        }
        .cancal{
          background: #F5F6F7;
          border-color: #F5F6F7;
          color: #494f6A;
          &:hover{
            background: #EEEFF0;
            border-color: #EEEFF0;
            color: #494f6A;
          }
        }
      }
    }
  }
}
</style>
<style lang="scss">
.el-dialog__wrapper.user-info-bind-dialog-wrapper .el-dialog.bind-dialog{
  border-radius: 6px;
  height: 425px;
  margin-top: 0!important;
  top: calc(50% - 217px);
  .el-dialog__header{
    display: none!important;
  }
  .el-dialog__body{
    padding: 0!important;
  }
  .bind-dialog-header{
    display: flex;
    align-items: center;
    margin-right: 0px;
    padding: 16px 32px;
    justify-content: space-between;
    border-bottom: 1px solid #F5F6F7;
    .userInfo-close{
      width: 16px;
      height: 16px;
      cursor: pointer;
      color: #575E73;
    }
    .bind-title{
      color:#363B4C;
      font-size: 16px;
      font-family: Source Han Sans-Bold, Source Han Sans;
    }
  }
  .bind-body{
    .qcode-box{
      display: flex;
      flex-direction: column;
      align-items: center;
      margin-bottom: 32px;
      .qcode-title{
        font-size: 20px;
        font-family: Source Han Sans-Regular, Source Han Sans;
        color: #363B4C;
        text-align: center;
      }
      .qcode-tips{
       font-family: Source Han Sans-Regular, Source Han Sans;
        font-size: 14px;
        color: #6F7588;
        text-align: center;
        margin-top: 6px;
      }
      .qcode-body-box{
        width: 220px;
        height: 220px;
        position: relative;
        margin-top: 16px;
        img{
          width: 100%;
          cursor: pointer;
        }
        .qcode-mark{
          position: absolute;
          z-index: 2;
          top: 0px;
          left: 0px;
          background: transparent;
          width: 220px;
          height: 220px;
          cursor: pointer;
          display: flex;
          align-items: center;
          justify-content: center;
          background-color: rgba(255,255,255,0.8);
          .tips{
            font-family: Source Han Sans-Regular, Source Han Sans;
            font-size: 14px;
          }
        }
        .iframe-box{
          width: 220px;
          height: 230px;
          display: flex;
          justify-content: center;
          align-items: center;
          overflow: hidden;
          position: relative;
          margin-top: 10px;
          box-sizing: border-box;
          .dd-box{
            position: absolute;
            width: 210px;
            height: 250px;
            top: -40px;
          }
          .wxenter-box{
            position: absolute;
            width: 220px;
            height: 260px;
            top: -44px;
          }
        }
      }
      .ldap-box{
        width: 100%;
        padding: 20px 32px 0;
        box-sizing: border-box;
        .el-form{
          font-family: Source Han Sans-Regular, Source Han Sans;
          .el-form-item__label{
            line-height: 22px;
            padding-bottom: 8px;
            font-size: 13px;
          }
        }
        .el-input{
          height: 36px;
          margin-bottom: 2px;
          .el-input__inner{
            border: 0;
            background-color: #F5F6F7;
          }
        }
      }
    }
  }
}
.el-dialog__wrapper.user-info-bind-dialog-wrapper .el-dialog.LDAP-dialog{
  height: 321px;
  top: calc(50% - 160px);
  .qcode-box{
    margin-bottom: 0px!important;
  }
  .dialog__footer{
    border-top: 1px solid #EEEFF0;
    padding: 16px 16px 12px;
    .button-box{
      text-align: right;
      .el-button{
        height: 32px;
        padding: 0 15px;
        font-size: 13px;
        font-family: Source Han Sans-Regular, Source Han Sans;
      }
      .cancal{
        background: #F5F6F7;
        border-color: #F5F6F7;
        color: #494f6A;
        &:hover{
          background: #EEEFF0;
          border-color: #EEEFF0;
          color: #494f6A;
        }
      }
    }
  }
}
</style>
