<template>
  <div>
    <el-dialog
      ref="loginComDialog"
      :modal="modal"
      :class="{'login-component-dialog': true, 'login-component-dialog-modal': modal}"
      :visible.sync="loginVisible"
      append-to-body
      :before-close="handleClose">
      <div class="jvs-loginCom" v-if="loginVisible" :style="setStyle">
        <div v-if="tenantVisible" :class="{'user-tenant-list': true}">
          <div v-if="tenantLoading" class="tenant-loading-back"></div>
          <p v-for="item in usertenantList" :key="'tenant-item'+item.id" @click="tenantLoginHandle(item)">
            <img v-if="item.icon" :src="item.icon" alt="" />
            <span v-else class="empty-logo-image">
              <i class="el-icon-office-building"></i>
            </span>
            <span>{{item.shortName}}</span>
          </p>
        </div>
        <div v-else class="login">
          <div class="login-top">
            <h5>{{formType == 'login' ? $langt('common.login.welLogin') : $langt('common.login.welReg')}}</h5>
          </div>
          <div :class="{'login-center': true, 'code-login-center': (['weixin', 'dd', 'wxenterprise'].indexOf(loginType) > -1)}" v-if="!fresh">
            <div v-if="formType == 'login'" class="top-right">
              <svg v-if="(hasLoginRight('wx') || hasLoginRight('wxmp')) && ['namepass', 'phone'].indexOf(loginType) > -1" class="jvs-login-icon" aria-hidden="true" @click="changeType('weixin')">
                <use xlink:href="#icon-jvs-saomadenglu"></use>
              </svg>
              <svg v-if="['namepass', 'phone'].indexOf(loginType) == -1" class="jvs-login-icon" aria-hidden="true" @click="changeType('namepass')">
                <use xlink:href="#icon-jvs-mimadenglu1"></use>
              </svg>
            </div>
            <div class="jvs-loginCom-form">
              <!-- 密码 & 手机验证码 -->
              <div v-if="['namepass', 'phone'].indexOf(loginType) > -1" class="change-tab-list">
                <div :class="{'change-tab-list-item': true, 'active': loginType == 'namepass'}" @click="changeType('namepass')">{{$langt('common.login.passLogin')}}</div>
                <div v-if="hasLoginRight('phone')" :class="{'change-tab-list-item': true, 'active': loginType == 'phone'}" @click="changeType('phone')">{{$langt('common.login.codeLogin')}}</div>
              </div>
              <el-form ref="loginForm" :rules="loginRules" :model="loginForm" label-width="0">
                <!-- 账号 -->
                <el-form-item v-if="loginType == 'namepass' || loginType == 'register'" prop="username"><el-input v-model="loginForm.username" :placeholder="$langt('common.login.placeholder.username')" size="small" @keyup.enter.native="handleLogin"></el-input></el-form-item>
                <!-- LDAP账号 -->
                <el-form-item v-if="loginType == 'ldap'" prop="account"><el-input v-model="loginForm.account" :placeholder="$langt('common.login.placeholder.account')" size="small" @keyup.enter.native="handleLogin"></el-input></el-form-item>
                <!-- 手机号 -->
                <el-form-item v-if="loginType == 'phone' || formType == 'register'" prop="phone"><el-input v-model="loginForm.phone" auto-complete="off" :placeholder="$langt('common.login.placeholder.phone')" size="small" @keyup.enter.native="handleLogin"></el-input></el-form-item>
                <!-- 密码 -->
                <el-form-item v-if="loginType == 'namepass' || loginType == 'ldap'" prop="password">
                  <el-input v-model="loginForm.password" :type="passwordType" auto-complete="off" :placeholder="$langt('common.login.placeholder.password')" size="small" @keyup.enter.native="handleLogin"><i class="el-icon-view el-input__icon" slot="suffix" @click="showPassword"></i></el-input>
                </el-form-item>
                <!-- 手机验证码 -->
                <el-form-item v-if="loginType == 'phone' || loginType == 'register'" prop="code">
                  <div class="code-row">
                    <div style="flex: 1;">
                      <el-input v-model="loginForm.code" auto-complete="off" :placeholder="$langt('common.login.placeholder.code')" size="small" :maxlength="code.len" @keyup.enter.native="handleLogin"></el-input>
                    </div>
                    <div class="code-row-button">
                      <el-button v-if="loginType == 'register'" size="mini" :disabled="regcodeobj.disabled" @click.native.prevent="getPhoneCode('regcodeobj')">{{regcodeobj.text}}</el-button>
                      <el-button v-else size="mini" :disabled="yzmobj.disabled" @click.native.prevent="getPhoneCode('yzmobj')">{{yzmobj.text}}</el-button>
                    </div>
                  </div>
                </el-form-item>
                <!-- 扫码登录 -->
                <div class="code-box">
                  <!-- 微信二维码登录 -->
                  <div v-if="loginType == 'weixin' && wxType =='wx'" class="code-info">
                    <div class="fir-title">{{$langt('common.login.scanLogin')}}</div>
                    <div class="sec-title" style="margin-bottom: 16px;">{{$langt('common.login.placeholder.wechat')}}</div>
                    <iframe id="weixinCode" :src="`/auth/just/login/WECHAT_OPEN?client_id=${client_id}&url=`+ callbackUrl" scrolling="no"  frameborder="0" width="210" height="210"></iframe>
                    <div class="expires-box" @click="freshWeixin" style="cursor:pointer;z-index: 9;top: 73px;"></div>
                  </div>
                  <!-- 微信公众号扫码 -->
                  <div v-if="loginType == 'weixin' && OfficQrcodeUrl && wxType=='wxmp'" v-loading="QRLoading" :element-loading-text="$langt('common.login.placeholder.logging')" class="code-info">
                    <div class="fir-title">{{$langt('common.login.scanLogin')}}</div>
                    <div class="sec-title">{{$langt('common.login.placeholder.wechat')}}</div>
                    <img id="weixinCode" :src="OfficQrcodeUrl"  @click="freshWeixin" scrolling="no"  frameborder="0" height="210" width="210" style="cursor:pointer;" />
                    <div v-if="isExpires" class="expires-box" @click="freshWeixin">{{$langt('common.login.freshTip')}}</div>
                  </div>
                  <!-- 钉钉扫码登录 -->
                  <div v-if="loginType == 'dd' && ddCodeUrl" class="code-info">
                    <div class="fir-title">{{$langt('common.login.scanLogin')}}</div>
                    <div class="sec-title">{{$langt('common.login.placeholder.ding')}}</div>
                    <div class="iframe-box dd">
                      <iframe id="ddLogin" :src="ddCodeUrl" scrolling="no" frameborder="0"></iframe>
                    </div>
                  </div>
                  <!-- 企业微信扫码登录 -->
                  <div v-if="loginType == 'wxenterprise'" class="code-info">
                    <div class="fir-title">{{$langt('common.login.scanLogin')}}</div>
                    <div class="sec-title">{{$langt('common.login.placeholder.wxenter')}}</div>
                    <div class="iframe-box dd">
                      <iframe id="wxenterpriseCode" :src="`/auth/just/login/WECHAT_ENTERPRISE?client_id=${client_id}&url=`+ callbackUrl" scrolling="no"  frameborder="0" height="220"></iframe>
                    </div>
                  </div>
                </div>
                <!-- 按钮 -->
                <el-form-item v-if="formType == 'login' ? (['weixin', 'dd', 'wxenterprise', 'app', 'third'].indexOf(loginType) == -1) : true" class="login-form-item-button">
                  <el-button v-if="formType == 'login' && ['weixin', 'dd', 'wxenterprise', 'app', 'third'].indexOf(loginType) == -1" type="primary" size="small" :loading="submitLoading" class="login-submit-button" @click.native="handleLogin">{{$langt('common.login.loginNow')}}</el-button>
                  <el-button v-if="formType == 'register'" type="primary" size="small" :loading="submitLoading" class="login-submit-button" @click.native="handleRegister">{{$langt('common.login.regNow')}}</el-button>
                </el-form-item>
                <el-row v-if="formType == 'register'" class="note-text">
                  <p>
                    <span>{{$langt('common.login.regTip')}}<jvs-button type="text" @click="openRule">{{$langt('common.login.terms')}}</jvs-button></span>
                  </p>
                  <p>
                    <span><jvs-button type="text" @click="changeFormType('namepass', 'login')">{{$langt('common.login.goLogin')}}</jvs-button></span>
                  </p>
                </el-row>
                <el-row v-if="formType == 'login' && ( hasLoginRight('wx') || hasLoginRight('wxmp') || hasLoginRight('wxenterprise')  || hasLoginRight('ldap') || hasLoginRight('dd') || (hasOtherTypes && hasOtherTypes.length > 0))" class="other-type-item">
                  <el-divider content-position="center">{{$langt('common.login.otherType')}}</el-divider>
                  <p :class="{'little-img': (originTypeCount < 3 && hasOtherTypes && hasOtherTypes.length > 0)}">
                    <!-- 微信扫码 -->
                    <svg v-if="(hasLoginRight('wx') || hasLoginRight('wxmp')) && loginType != 'weixin'" class="other-login-icon" aria-hidden="true" @click="changeType('weixin')">
                      <use xlink:href="#icon-jvs-weixin-2"></use>
                    </svg>
                    <!-- 企业微信扫码 -->
                    <svg v-if="hasLoginRight('wxenterprise') && loginType != 'wxenterprise'" class="other-login-icon" aria-hidden="true" @click="changeType('wxenterprise')">
                      <use xlink:href="#icon-jvs-qiyeweixin"></use>
                    </svg>
                    <!-- LADAP -->
                    <svg v-if="hasLoginRight('ldap') && loginType != 'ldap'" class="other-login-icon" aria-hidden="true" @click="changeType('ldap')">
                      <use xlink:href="#icon-jvs-LADP"></use>
                    </svg>
                    <!-- 钉钉扫码 -->
                    <svg v-if="hasLoginRight('dd') && loginType != 'dd'" class="other-login-icon" aria-hidden="true" @click="changeType('dd')">
                      <use xlink:href="#icon-jvs-dingding"></use>
                    </svg>
                    <img v-if="hasOtherTypes && hasOtherTypes.length > 0 && (originTypeCount < 3) && loginType != hasOtherTypes[0].type" :src="hasOtherTypes[0].logo" @click="otherClick(hasOtherTypes[0])">
                    <img v-if="hasOtherTypes && hasOtherTypes.length > 1 && (originTypeCount < 2) && loginType != hasOtherTypes[1].type" :src="hasOtherTypes[1].logo" @click="otherClick(hasOtherTypes[1])">
                    <img v-if="hasOtherTypes && hasOtherTypes.length > 2 && (originTypeCount == 0) && loginType != hasOtherTypes[2].type" :src="hasOtherTypes[2].logo" @click="otherClick(hasOtherTypes[2])">
                    <el-popover
                      v-if="hasOtherTypes && hasOtherTypes.length > 0 && (originTypeCount == 2 ? (hasOtherTypes.length > 1) : (hasOtherTypes.length > (2 - originTypeCount)))"
                      placement="left"
                      trigger="click">
                      <div class="other-type-image-list">
                        <img v-for="(img, inx) in hasOtherTypes" :src="img.logo" :key="img.name" v-show="inx > (2 - originTypeCount)" @click="otherClick(img)">
                      </div>
                      <span slot="reference" class="more-other">
                        <i class="el-icon-more"></i>
                      </span>
                    </el-popover>
                  </p>
                </el-row>
              </el-form>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>

</template>
<script>
import QRcode from "../../QRcode/index"
import { randomLenNum,encryption } from "@/util/util";
import { getOtherTypeList } from '@/api/common'
import {
  getPhone,
  getRegPhoneCode,
  getCanLogin,
  getOffLoginQcode,
  checkQrcodeState,
  codeLogin,
  getInfoByLoginType
} from "@/api/login"
import { enCodeKey, client_id } from "@/const/const"
export default {
  components: { 'qr-code': QRcode},
  computed: {
    setStyle () {
      let style = ''
      if(this.right) {
        style += `right:${this.right};`
      }else{
        if(this.left) {
          style += `left: ${this.left};`
        }else{
          style += `left: calc(50% - 230px);`
        }
      }
      if(this.top) {
        style += `top:${this.top};`
      }else{
        if(this.bottom) {
          style += `bottom:${this.bottom};`
        }else{
          style += `top: calc(50% - 300px);`
        }
      }
      return style
    },
    modal () {
      if(this.hasModal) {
        return true
      }else{
        return false
      }
    },
    hasOtherTypes () {
      let temp = []
      this.otherTypeList.filter(fit => {
        if(this.hasLoginRight(fit.type)) {
          temp.push(fit)
        }
      })
      return temp
    },
    originTypeCount () {
      let count = 0
      if(this.hasLoginRight('wx') || this.hasLoginRight('wxmp')) {
        count += 1
      }
      if(this.hasLoginRight('wxenterprise')) {
        count += 1
      }
      if(this.hasLoginRight('ldap')) {
        count += 1
      }
      if(this.hasLoginRight('dd')) {
        count += 1
      }
      return count
    }
  },
  data() {
    var validateUserName = (rule, value, callback) => {
      if (value.length > 64) {
        callback(new Error('用户名不能超过64位字符'));
      } else {
        if(/^(?!_)(?!.*?_$)[a-zA-Z0-9_\u4e00-\u9fa5]+$/.test(value)) {
          callback();
        }else{
          callback(new Error('用户名包含中英文、数字和下划线，且不能下划线开头结尾'));
        }
      }
    };
    return {
      loginVisible: false,
      loginForm: {
        username: "", // "admin",
        account: "",
        password: "", // "123456",
        code: "",
        redomStr: "",
        // company: 1,
        phone: '',
        wxcode: "",
        namecode: ''
      },
      loginRules: {
        username: [
          { required: true, message: " ", trigger: "blur" },
          { validator: validateUserName, trigger: 'blur' }
        ],
        account: [
          { required: true, message: " ", trigger: "blur" },
        ],
        password: [
          { required: true, message: " ", trigger: "blur" },
        ],
        phone: [{ required: true, message: " ", trigger: "blur" }],
        code: [
          { required: true, message: " ", trigger: "blur" },
        ],
      },
      loginType: 'weixin', // 'namepass'
      passwordType: "password",
      loginTypes: [],
      submitLoading: false,
      code: {
        src: "/code",
        value: "",
        len: 6,
        namecodelen: 4,
        type: "image"
      },
      yzmobj: {
        text: this.$langt('common.login.placeholder.getcode'),
        time: 180,
        disabled: false
      },
      regcodeobj: {
        text: this.$langt('common.login.placeholder.getcode'),
        time: 180,
        disabled: false
      },
      qrType: 'weixin',
      formType: 'login', // register
      fresh: false,
      usertenantList: [], // 用户的租户列表
      tenantVisible: false, // 租户列表展示
      tenantLoading: false,
      randomString: '',
      callbackUrl: '',
      // 公众号二维码地址
      wxType:'',
      isExpires:false, //二维码是否过期
      OfficQrcodeUrl:'',
      QRcodeUUId:'',
      QRCheckSetInterval:null,
      QRChcekSetTimeout:null,
      QRLoading:false,
      ddCodeUrl: '',
      ddInfo: null,
      client_id: client_id,
      otherTypeList: []
    }
  },
  methods: {
    async init () {
      if(this.canlogin && this.canlogin.length > 0) {
        this.loginTypes = this.canlogin
      }else{
        await getCanLogin().then(res => {
          if(res.data && res.data.code == 0) {
            this.loginTypes = res.data.data
          }
        })
      }
      if(this.loginTypes.indexOf('password') > -1) {
        this.loginType = 'namepass'
      }else{
        this.loginType = 'phone'
      }
      // 微信
      if(this.loginTypes.indexOf('wx') > -1) {
        this.loginType = 'weixin'
        this.wxType = 'wx'
      }
      this.loginVisible = true
      if(this.switchTenant) {
        this.tenantVisible = true
        this.usertenantList = this.switchList
      }else{
        this.$store.dispatch("LogOut", 'notAll')
      }
      // 微信公众号
      if(this.loginTypes.indexOf('wxmp')>-1){
        this.loginType = 'weixin'
        this.wxType = 'wxmp'
        this.QRLoading = false
        if(!this.switchTenant){
          this.getOffLoginQcode()
        }
      }
    },
    // 获取二维码
    getOffLoginQcode(){
      let uuid = this.getUUId(),uuidNew = []
      uuid.split('').forEach(item => {
        if(item!='-'){
          uuidNew.push(item)
        }
      });
      this.QRcodeUUId = uuidNew.join('')
      getOffLoginQcode(this.QRcodeUUId).then(res=>{
        if(res.data.code==0){
          this.OfficQrcodeUrl = res.data.data
          this.isExpires = false
          this.startCheckState()
        }
      })
    },
    // 检查登录状态
    startCheckState(){
      if(this.QRCheckSetInterval){
        clearInterval(this.QRCheckSetInterval)
      }
      if(this.QRChcekSetTimeout){
        clearTimeout(this.QRChcekSetTimeout)
      }
      // this.QRChcekSetTimeout = setTimeout(()=>{
      //   this.isExpires = true
      //   if(this.QRCheckSetInterval){
      //     clearInterval(this.QRCheckSetInterval)
      //   }
      // },1000 * 60 * 0.05)
      this.QRCheckSetInterval = setInterval(()=>{
        checkQrcodeState(this.QRcodeUUId).then(res=>{
          if(res.data.code == 0 && res.data.data.checkStatus){
            this.QRLoading = true
            if(this.QRCheckSetInterval){
              clearInterval(this.QRCheckSetInterval)
            }
            this.QRcodeLogin(this.QRcodeUUId)
          }else if(res.data.code == 0 && res.data.data.isPastDue){
            this.isExpires = true
            if(this.QRCheckSetInterval){
              clearInterval(this.QRCheckSetInterval)
            }
          }
        })
      },1000 * 2)
    },
    QRcodeLogin(id){
      let tp = {
        data: JSON.stringify(Object.assign({ id: id}))
      }
      let temp = encryption({
        data: tp,
        key: enCodeKey,
        param: ["data"]
      });
      codeLogin(('WECHAT_MP@'+temp.data)).then(res=>{
        this.getTenantByUserList(res.data)
      })
    },
    getUUId(){
      return URL.createObjectURL(new Blob()).substr(-36)
    },
    handleClose () {
      if(this.QRCheckSetInterval){
        clearInterval(this.QRCheckSetInterval)
      }
      this.submitLoading = false
      this.tenantLoading = false
      this.loginVisible = false
    },
    resetLogin () {
      this.$refs.loginForm.resetFields()
      this.fresh = true
      this.$nextTick( () => {
        this.loginForm = {
          username: "",
          password: "",
          code: "",
          redomStr: "",
          phone: '',
          wxcode: "",
          namecode: ''
        }
        this.fresh = false
      })
    },

    hasLoginRight (type) {
      let bool = false
      if(this.loginTypes.indexOf(type) > -1) {
        bool = true
      }
      return bool
    },
    showPassword () {
      this.passwordType == ''
        ? (this.passwordType = 'password')
        : (this.passwordType = '')
    },
    // 切换登录方式
    changeType (type) {
      if(this.QRCheckSetInterval){
        clearInterval(this.QRCheckSetInterval)
      }
      if(type=='weixin' && this.wxType=='wxmp'){
        this.getOffLoginQcode()
      }
      if (type !== 'weixin' && this.$refs.QRCode) {
        this.$refs.QRCode.clear()
      }
      if(type == 'dd') {
        // 钉钉二维码
        this.getDDCode()
      }else{
        this.ddCodeUrl = ''
      }
      this.loginType = type
      this.resetLogin()
      // this.getQRcodeUrl()
    },
    // 获取验证码
    getPhoneCode (attr) {
      if(this.loginForm.phone && !this.loginForm.phone.includes(" ")){
        let func = null
        if(this.loginType == 'register') {
          func = getRegPhoneCode
        }else{
          func = getPhone
        }
        if(func) {
          func({ phone: this.loginForm.phone }).then(({ data }) => {
            // console.log(data)
            if (data.code === 0) {
              this.$set(this[attr], 'disabled', true)
              let time = this[attr].time
              let setInt = setInterval(() => {
                time--
                if (time <= 0) {
                  this.$set(this[attr], 'disabled', false)
                  clearInterval(setInt)
                  this.$set(this[attr], 'text', this.$langt('common.login.placeholder.sendcode'))
                } else {
                  this.$set(this[attr], 'text', (time + `s${this.$langt('common.login.placeholder.resendcoed')}`) )
                }
              }, 1000)
            }
          }).catch(err => {
            this.refreshCode();
          })
        }
      }
    },
    // 获取二维码
    getQRcodeUrl (val) {
      let _this = this
      if (this.loginType === 'weixin') {
        setTimeout(() => {
          _this.$refs.QRCode.init(_this.qrType)
        }, 1)
      }
    },
    // 刷新验证码
    refreshCode () {
      this.loginForm.code = ''
      this.loginForm.randomStr = randomLenNum(this.code.len, true)
      this.code.type === 'text'
        ? (this.code.value = randomLenNum(this.code.len))
        : (this.code.src = `${this.codeUrl}?randomStr=${this.loginForm.randomStr}`)
    },
    // 登录提交
    handleLogin () {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          let subData = {}
          if(this.loginType == 'ldap') {
            subData = { loginType: 'LDAP', account: this.loginForm.account, password: this.loginForm.password }
          }else{
            let temp = {}
            if(this.queryData) {
              temp = JSON.parse(JSON.stringify(this.queryData))
            }
            subData = Object.assign(temp, this.loginForm, { loginType: this.loginType, code: this.loginType === 'namepass' ? this.loginForm.namecode : this.loginForm.code })
          }
          this.submitLoading = true
          this.$store.dispatch("LoginByUsername", subData).then((data) => {
            this.getTenantByUserList(data)
          }).catch((err) => {
            if(this.loginType != 'ldap') {
              // this.$message.error(err.message)
              this.$notify({
                title: '提示',
                message: err.message,
                position: 'bottom-right',
                type: 'error'
              });
            }
            this.refreshCode()
            this.submitLoading = false
          })
        }
      });
    },
    // 微信登录成功回调
    loginWX (code, type) {
      if (type == 'weixin') {
        codeGetUserInfo({ code: code }).then(({ data }) => {
          if (data.code === 0) {
            wxOpenidLogin({ openId: data.data.openid }).then(response => {}).catch(error => {
              // this.getQRcodeUrl()
            });
          }
        })
      } else {
        appLogin(code).then(response => {}).catch(error => {
          // this.getQRcodeUrl() 
        });
      }
    },
    // 获取该用户下所有的租户列表
    getTenantByUserList (data) {
      // console.log(data)
      let list = []
      if(data.userDto && data.userDto.tenants) {
        list = data.userDto.tenants
        if(list.length > 0) {
          // 只有一个租户 或指定租户 直接进去
          if(list.length == 1 || (data.userDto.tenantId && data.userDto.tenant)) {
            if(data.userDto && data.userDto.callBackUrl && localStorage.getItem('loginQuery')){
              this.$openUrl(`${data.userDto.callBackUrl}?access_token=${data.access_token}&refresh_token=${data.refresh_token}`, '_self') // 'Bearer ' +
              localStorage.setItem('loginQuery', '')
              this.submitLoading = false
            }else{
              this.$store.commit("SET_SwitchTenant", false)
              this.setUserInfoData(data)
              if(this.successClose !== false) {
                this.handleClose()
              }
              // 后续操作
              if(this.afterLogin) {
                this.afterLogin(this.$refs.loginComDialog, data)
              }
            }
          }else{
            this.submitLoading = false
            this.usertenantList = list
            this.$store.commit("SET_SwitchTenant", true)
            this.tenantVisible = true
          }
        }
      }
    },
    // 切换租户登录
    tenantLoginHandle (item) {
      if(item.id) {
        this.tenantLoading = true
        this.$store.dispatch('RefreshToken', item.id).then(data => {
          if(data) {
            if(data.userDto && data.userDto.callBackUrl && localStorage.getItem('loginQuery')){
              this.$openUrl(`${data.userDto.callBackUrl}?access_token=${data.access_token}&refresh_token=${data.refresh_token}`, '_self') // 'Bearer ' +
              localStorage.setItem('loginQuery', '')
              this.submitLoading = false
            }else{
              this.setUserInfoData(data)
              if(this.successClose !== false) {
                this.handleClose()
              }
              // 后续操作
              if(this.afterLogin) {
                this.afterLogin(this.$refs.loginComDialog, data)
              }
            }
          }else{
            this.submitLoading = false
          }
        }).catch(() => {
          this.submitLoading = false
          this.tenantLoading = false
        });
      }
    },
    // 登录成功写入数据
    setUserInfoData (data) {
      this.$store.commit("SET_ACCESS_TOKEN", data.access_token);
      this.$store.commit("SET_REFRESH_TOKEN", data.refresh_token);
      this.$store.commit("SET_EXPIRES_IN", data.expires_in);
      this.$store.commit("CLEAR_LOCK");
      this.$store.commit("SET_USER_INFO", data.userDto);
      this.$store.commit("SET_ROLES", data.roles || []);
      this.$store.commit("SET_PERMISSIONS", data.permissions || []);

      this.$store.commit("SET_TENANTId", data.userDto.tenantId);
      this.$store.commit("DEL_ALL_TAG"); // 关闭之前打开的所有tag
      this.$store.commit("SET_THEME_NAME", ""); // 清除主题
      this.$store.commit("SET_TENANTINFO", data.userDto.tenant)
    },
    // 注册
    changeFormType (type, formtype) {
      this.loginType = type
      this.formType = formtype
      this.resetLogin()
    },
    // 注册提交
    handleRegister () {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.submitLoading = true
          this.$store.dispatch("LoginByUsername", Object.assign({}, this.loginForm, { loginType: this.loginType, code: this.loginType === 'namepass' ? this.loginForm.namecode : this.loginForm.code })).then((data) => {
            this.getTenantByUserList(data)
          }).catch(() => {
            this.refreshCode()
            this.submitLoading = false
          })
        }
      });
      // if(this.afterRegister) {
      //   this.afterRegister()
      // }
    },
    // 刷新二维码
    freshWeixin () {
      if(this.wxType == 'wxmp'){
        this.getOffLoginQcode()
      }else{
        $('#weixinCode').attr('src', $('#weixinCode').attr('src'))
      }
    },
    // 使用条款
    openRule () {
      this.$openUrl('/agreement/policies.html', '_blank')
    },
    // 钉钉二维码生成
    getDDCode () {
      getInfoByLoginType({type: 'dd'}).then(res => {
        if(res && res.data && res.data.code == 0) {
          this.ddInfo = res.data.data
          let url = encodeURIComponent(res.data.data.redirectUri || `${location.origin}/#/login/dingtalk/scanback`);
          let goto = encodeURIComponent(`https://oapi.dingtalk.com/connect/oauth2/sns_authorize?appid=${res.data.data.appId}&response_type=code&scope=snsapi_login&state=STATE&redirect_uri=${url}`)
          this.ddCodeUrl = `https://login.dingtalk.com/login/qrcode.htm?goto=${goto}&style=border:none;background-color:#FFFFFF;`
          this.$forceUpdate()
        }
      })
    },
    // 钉钉扫码回调
    dingdingBackHandle (event) {
      var origin = event.origin;
      if( origin == "https://login.dingtalk.com" ) { //判断是否来自ddLogin扫码事件。
        var loginTmpCode = event.data;
        //获取到loginTmpCode后就可以在这里构造跳转链接进行跳转了
        console.log("loginTmpCode", loginTmpCode);
        let url = encodeURIComponent(this.ddInfo.redirectUri || `${location.origin}/#/login/dingtalk/scanback`);
        let goto = encodeURIComponent(`https://oapi.dingtalk.com/connect/oauth2/sns_authorize?appid=${this.ddInfo.appId}&response_type=code&scope=snsapi_login&state=STATE&redirect_uri=${url}`)
        this.$openUrl(`https://oapi.dingtalk.com/connect/oauth2/sns_authorize?appid=${this.ddInfo.appId}&response_type=code&scope=snsapi_login&state=STATE&redirect_uri=${goto}&loginTmpCode=${loginTmpCode}`, '_self')
      }
    },
    getOtherTypeListHandle () {
      getOtherTypeList().then(res => {
        if(res.data && res.data.code == 0) {
          this.otherTypeList = res.data.data
        }
      })
    },
    otherClick (item) {
      this.$openUrl(`/auth/just/oauth2?stats=${item.type}`, '_self')
    }
  },
  created () {
    this.callbackUrl = location.origin + '/%23/login/callback?back=login'
    this.refreshCode();
    let randomStr = Math.random() + ""
    this.randomString = '/' + (randomStr.slice(2, randomStr.length))
    localStorage.setItem('loginRandom', this.randomString)
    // 监听钉钉扫码
    if (typeof window.addEventListener != 'undefined') {
      window.addEventListener('message', this.dingdingBackHandle, false);
    } else if (typeof window.attachEvent != 'undefined') {
      window.attachEvent('onmessage', this.dingdingBackHandle);
    }
    this.getOtherTypeListHandle()
  },
  destroyed () {
    this.handleClose()
  }
}
</script>
<style lang="scss">
.login-component-dialog{
  .el-dialog{
    overflow: hidden;
    background: none;
    box-shadow: none;
    width: 100%;
    height: 100vh;
    margin: 0!important;
    .el-dialog__header{
      display: none!important;
    }
    .el-dialog__body{
      padding: 0;
      margin: 0;
      width: 100%;
      height: 100%;
    }
  }
  .jvs-loginCom{
    width: 390px;
    height: 562px;
    position: absolute;
    h1,h2,h3,h4,h5,p{
      margin: 0;
      padding: 0;
    }
    .login{
      width: 100%;
      height: 100%;
      box-sizing: border-box;
      .login-top{
        height: 62px;
        overflow: hidden;
        h5{
          font-size: 21px;
          font-family: Microsoft YaHei, Microsoft YaHei;
          font-weight: 500;
          line-height: 62px;
          color: #222222;
          text-align: center;
        }
      }
      .login-center{
        border-radius: 6px;
        box-shadow: 0px 4px 10px 0px rgba(54,59,76,0.15);
        background-color: #fff;
        margin-top: 16px;
        box-sizing: border-box;
        padding: 4px;
        height: calc(100% - 78px);
        .top-right{
          width: 100%;
          height: 72px;
          overflow: hidden;
          position: relative;
          .jvs-login-icon{
            width: 72px;
            height: 72px;
            position: absolute;
            right: 0;
            top: 0;
            cursor: pointer;
          }
        }
        .jvs-loginCom-form{
          padding: 0 36px;
          margin-top: -7px;
          position: relative;
          .change-tab-list{
            display: flex;
            align-items: center;
            font-size: 16px;
            font-family: Microsoft YaHei-Regular, Microsoft YaHei;
            font-weight: 400;
            color: #363B4C;
            margin-bottom: 40px;
            .change-tab-list-item{
              height: 21px;
              line-height: 21px;
              padding-bottom: 16px;
              margin-left: 32px;
              position: relative;
              cursor: pointer;
            }
            .change-tab-list-item.active{
              font-size: 16px;
              font-family: Microsoft YaHei-Bold, Microsoft YaHei;
              font-weight: 700;
            }
            .active::after{
              content: "";
              display: block;
              position: absolute;
              left: 0;
              bottom: 0;
              width: 100%;
              height: 2px;
              background: #1E6FFF;
              border-radius: 10px 0px 10px 0px;
            }
            .change-tab-list-item:nth-of-type(1){
              margin-left: 0;
            }
          }
          .el-form{
            .el-form-item {
              margin-bottom: 24px;
            }
            .el-input {
              input {
                border: 1px solid #F5F6F7;
                height: 40px;
                border-radius: 4px;
                background: #F5F6F7;
                font-size: 14px;
                font-family: Source Han Sans-Regular, Source Han Sans;
                font-weight: 400;
                color: #6F7588;
                box-sizing: border-box;
              }
              .el-input__inner::placeholder{
                font-size: 14px;
                font-family: Source Han Sans-Regular, Source Han Sans;
                font-weight: 400;
                color: #6F7588;
              }
            }
            .el-form-item.is-error{
              .el-input__inner{
                border-color: #FF194C;
              }
            }
            .code-row{
              height: 40px;
              margin: 0;
              display: flex;
              align-items: center;
              .code-row-button{
                border-radius: 4px;
                width: 96px;
                background: #F5F6F7;
                margin-left: 16px;
                .el-button{
                  width: 100%;
                  height: 40px;
                  font-size: 14px;
                  font-family: Source Han Sans-Regular, Source Han Sans;
                  font-weight: 400;
                  color: #1E6FFF;
                  border: 0;
                  background: none;
                }
              }
            }
            .login-form-item-button{
              margin-top: 40px;
              height: 40px;
              margin-bottom: 0;
              .login-submit-button{
                width: 100%;
                height: 40px;
                background: #1E6FFF;
                font-size: 14px;
                font-family: Source Han Sans-Regular, Source Han Sans;
                font-weight: 400;
                color: #FFFFFF;
                border-radius: 4px;
              }
            }
          }
          .code-box{
            width: 220px;
            min-height: auto;
            margin-left: calc(50% - 110px);
            position: relative;
            .code-info{
              display: flex;
              align-items: center;
              justify-content: center;
              flex-direction: column;
              .fir-title{
                font-size: 20px;
                font-family: Microsoft YaHei-Regular, Microsoft YaHei;
                font-weight: 400;
                color: #363B4C;
              }
              .sec-title{
                font-size: 14px;
                font-family: Microsoft YaHei, Microsoft YaHei;
                font-weight: 400;
                color: #6F7588;
                margin-top: 12px;
                text-align: center;
              }
              .iframe-box{
                margin-top: 16px;
                width: 220px;
                height: 220px;
                box-sizing: border-box;
                overflow: hidden;
                position: relative;
              }
              .iframe-box.dd{
                padding-left: 5px;
                iframe{
                  position: absolute;
                  width: 210px;
                  height: 250px;
                  top: -40px;
                }
              }
            }
            .expires-box{
              position: absolute;
              cursor: pointer;
              top: 57px;
              width: 210px;
              height: 210px;
              display: flex;
              align-items: center;
              justify-content: center;
              background-color: rgba(255,255,255,0.8);
              color: black;
              font-size: 16px;
              font-weight: 600;
            }
          }
          .btntab{
            display: flex;
            align-items: center;
            justify-content: space-between;
            p{
              width: 50%;
              .el-button, span{
                font-size: 14px;
                font-family: Microsoft YaHei, Microsoft YaHei;
                font-weight: 400;
              }
            }
            p:nth-last-of-type(1){
              text-align: right;
            }
          }
          .note-text{
            font-size: 18px;
            color: #868BA1;
            display: flex;
            justify-content: space-between;
            align-items: center;
            p:nth-of-type(1){
              flex: 1;
            }
            .el-button{
              font-size: 18px;
            }
          }
          .other-type-item{
            margin-top: 40px;
            box-sizing: border-box;
            .el-divider{
              margin: 0;
              margin-top: 10px;
              .el-divider__text{
                height: 18px;
                font-size: 14px;
                font-family: Microsoft YaHei-Regular, Microsoft YaHei;
                font-weight: 400;
                color: #6F7588;
              }
            }
            p{
              display: flex;
              justify-content: center;
              align-items: center;
              margin-top: 34px;
              img, .other-login-icon{
                display: block;
                width: 36px;
                height: 36px;
                cursor: pointer;
                margin-left: 10px;
              }
              .other-login-icon:nth-of-type(1){
                margin-left: 0;
              }
              .more-other{
                display: block;
                width: 36px;
                height: 36px;
                cursor: pointer;
                margin-left: 10px;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 24px;
                background: #ebeef5;
                border-radius: 50%;
                overflow: hidden;
                i{
                  color: #fff;
                }
              }
            }
            .little-img{
              img{
                width: 36px;
                height: 36px;
              }
              .more-other{
                width: 36px;
                height: 36px;
                font-size: 20px;
              }
            }
          }
        }
      }
      .code-login-center{
        .jvs-loginCom-form{
          margin-top: -40px;
        }
      }
    }
    .user-tenant-list{
      border-radius: 6px;
      box-shadow: 0px 4px 10px 0px rgba(54,59,76,0.15);
      padding: 0 20px;
      position: relative;
      height: 100%;
      overflow-y: auto;
      p{
        border-radius: 6px;
        width: 100%;
        height: 60px;
        overflow: hidden;
        margin: 0;
        padding: 0 10px;
        margin-top: 20px;
        cursor: pointer;
        box-sizing: border-box;
        display: flex;
        align-items: center;
        background: #fff;
        //border: 1px solid #EFF2F7;
        img{
          display: block;
          width: 40px;
          height: 40px;
          border: 0;
          //background: #868BA1;
          //margin-right: 10px;
          margin: 0 30px;
        }
        span{
          display: block;
          width: 200px;
          color: #fff;
          text-align: left;
          color: #333333;
        }
        .empty-logo-image{
          display: block;
          width: 40px;
          height: 40px;
          background: #EFF2F7; // #F6F6F6;
          text-align: center;
          //margin-right: 10px;
          margin: 0 30px;
          i{
            font-size: 24px;
            line-height: 40px;
            text-align: center;
            color: #868BA1;
          }
        }
      }
      p:hover{
        background: #EFF2F7;
      }
    }
    .user-tenant-list::-webkit-scrollbar{
      display: none;
    }
    .tenant-loading-back{
      position: absolute;
      width: 100%;
      height: 560px;
      top: 0;
      left: 0;
      box-sizing: border-box;
      background-color: rgba(255, 255, 255, 0.8);
      background-image: url('../../../../public/jvs-ui-public/img/loading.gif');
      background-repeat: no-repeat;
      background-position: center;
      background-position: center;
      //background-size: 300px 240px;
    }
  }
}
.login-component-dialog-modal{
  .el-dialog{
    position: unset;
    width: 50%;
    height: auto;
    .el-dialog__body{
      width: auto;
    }
  }
}
.other-type-image-list{
  display: flex;
  width: 220px;
  flex-wrap: wrap;
  box-sizing: border-box;
  padding-top: 10px;
  padding-right: 20px;
  img{
    display: block;
    width: 30px;
    height: 30px;
    cursor: pointer;
    margin-left: 20px;
    margin-bottom: 15px;
  }
}
</style>
