<template>
  <div class="auth-apply-center">
    <div class="title">应用授权中心</div>
    <div class="help-list">
      <div v-for="(item,index) in helpList" :key="index" class="help-item" :style="{'background-color':item.color}">
        <div class="help-left">
          <svg xmlns="http://www.w3.org/2000/svg" :viewBox="item.viewBox" width="74" height="74" style="border-color: rgba(187,187,187,1);border-width: 0px;border-style: solid" filter="none">
            <g>
              <path :d="item.path" fill="rgba(255,255,255,1)"></path>
            </g>
          </svg>
        </div>
        <div class="help-right">
          <div class="name">{{item.label}}</div>
          <div class="desc">{{item.desc}}</div>
        </div>
      </div>
    </div>
    <div class="apply-info" v-loading="authLoading">
      <div class="apply-title">
        <div>应用信息</div>
        <div class="company-info">
          <div class="name">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="64 64 896 896" width="24" height="24" style="border-color: rgba(187,187,187,1);border-width: 0px;margin-right:10px;border-style: solid" filter="none">
              <g>
                <path d="M946.5 505L560.1 118.8l-25.9-25.9a31.5 31.5 0 0 0-44.4 0L77.5 505a63.9 63.9 0 0 0-18.8 46c.4 35.2 29.7 63.3 64.9 63.3h42.5V940h691.8V614.3h43.4c17.1 0 33.2-6.7 45.3-18.8a63.6 63.6 0 0 0 18.7-45.3c0-17-6.7-33.1-18.8-45.2zM568 868H456V664h112v204zm217.9-325.7V868H632V640c0-22.1-17.9-40-40-40H432c-22.1 0-40 17.9-40 40v228H238.1V542.3h-96l370-369.7 23.1 23.1L882 542.3h-96.1z" fill="rgba(16.065,16.065,16.065,1)"></path>
              </g>
            </svg>
            <span class="name">公司名称:</span>
            <span>{{data.companyNmae}}</span>
          </div>
          <div class="device"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 512" width="24" height="24" style="border-color: rgba(187,187,187,1);border-width: 0px;margin-right:10px;border-style: solid" filter="none">
            <g>
              <path d="M592 0H48C21.5 0 0 21.5 0 48v320c0 26.5 21.5 48 48 48h245.1v32h-160c-17.7 0-32 14.3-32 32s14.3 32 32 32h384c17.7 0 32-14.3 32-32s-14.3-32-32-32h-160v-32H592c26.5 0 48-21.5 48-48V48c0-26.5-21.5-48-48-48zm-16 352H64V64h512v288z" fill="rgba(16.065,16.065,16.065,1)"></path>
            </g>
          </svg>
            <span class="name">设备凭证号:</span>
            <span style="display: inline-block;width: calc(100% - 130px);overflow:hidden;white-space: pre;text-overflow: ellipsis;">{{data.deviceNumber}}</span>
          </div>
          <div class="copy"
            v-clipboard:copy="data.copyDevNumber"
            v-clipboard:success="onCopy"
            v-clipboard:error="onCopyError">
            <span>复制</span>
            <i class="el-icon-document-copy"></i>
          </div>
        </div>
      </div>
      <div v-if="applyList && applyList.length > 0" class="apply-list">
        <div class="apply-list-item" v-for="(item,index) in applyList" :key="index" :class="{'noAuth':!item.isAuth}">
          <div class="apply-list-icon" :style="{backgroundColor: (item.bgColor ? item.bgColor : (item.isAuth?'#F5FAFF':'#FAF9F9'))}">
            <svg xmlns="http://www.w3.org/2000/svg" :viewBox="getSvgProp(item.icon).viewBox" width="40" height="40" :style="getSvgProp(item.icon).style" filter="none">
              <g>
                <path :d="getSvgProp(item.icon).path" :fill="item.isAuth? getSvgProp(item.icon).activeFill : getSvgProp(item.icon).fill"></path>
              </g>
            </svg>
          </div>
          <div class="apply-list-title">
            {{item.title}}
          </div>
          <div class="apply-list-desc">
            {{item.desc}}
          </div>
          <div class="apply-list-version">
            <div class="version-info">
              <div class="version-icon"></div>
              <div class="version-left">
                版本:&nbsp;V{{data.version}}
              </div>
              <div class="version-right">
                {{item.version || '试用版'}}
              </div>
            </div>
            <!-- v-if="item.isAuth" -->
            <div class="version-auth" @click="openSetAuth(item)">
              授权
              <i class="iconfont icon-shezhi"></i>
            </div>
          </div>
        </div>
      </div>
    </div>
    <el-dialog
      title="提示"
      :visible.sync="dialogVisible"
      width="468px"
      custom-class="role-add dialog-no-header dialog-center"
      :close-on-click-modal="false"
      :before-close="handleClose">
      <el-input v-model="authDevice" type="textarea" placeholder="填写授权码" :autosize="{ minRows: 4, maxRows: 8}"></el-input>
      <div class="license_tips" v-if="authCode!=0">{{getTips}}</div>
      <div slot="footer" class="dialog-footer">
        <el-button size="small" :loading="butLoading" type="primary" @click="updateLicense" :disabled='!authDevice'>确定</el-button>
        <el-button size="small" @click="handleClose">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getStore } from "@/util/store";
import moment from 'moment'
import { getLicense, veriLicense } from '@/api/common'
import _ from 'lodash'
export default {
  data() {
    return {
      dianlao:'dianlao',
      house:'house',
      helpList:[{
        viewBox: '0 0 540 540',
        path: 'M504 256c0 136.997-111.043 248-248 248S8 392.997 8 256C8 119.083 119.043 8 256 8s248 111.083 248 248zM262.655 90c-54.497 0-89.255 22.957-116.549 63.758-3.536 5.286-2.353 12.415 2.715 16.258l34.699 26.31c5.205 3.947 12.621 3.008 16.665-2.122 17.864-22.658 30.113-35.797 57.303-35.797 20.429 0 45.698 13.148 45.698 32.958 0 14.976-12.363 22.667-32.534 33.976C247.128 238.528 216 254.941 216 296v4c0 6.627 5.373 12 12 12h56c6.627 0 12-5.373 12-12v-1.333c0-28.462 83.186-29.647 83.186-106.667 0-58.002-60.165-102-116.531-102zM256 338c-25.365 0-46 20.635-46 46 0 25.364 20.635 46 46 46s46-20.636 46-46c0-25.365-20.635-46-46-46z',
        icon:'wenhao',
        label:'如何进行应用授权',
        desc:'通过与商务沟通，获取授权码，授权码分为短期授权码和永久授权码。授权码填写至用应用授权处，校验成功，完成授权。',
        color:'#4AD1A0'
      },{
        viewBox: '0 0 24 24',
        path: 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z',
        icon:'dui',
        label:'应用授权的优势',
        desc:'完成应用授权成功后，授权用户可体验完整应用功能，同时系统将不限制使用人员，可自行根据用户需求添加使用用户。',
        color:'#FDB472'
      },{
        viewBox: '0 0 32 32',
        path: 'M16 29.333c-7.364 0-13.333-5.969-13.333-13.333s5.969-13.333 13.333-13.333 13.333 5.969 13.333 13.333-5.969 13.333-13.333 13.333zM16 26.667c5.891 0 10.667-4.776 10.667-10.667s-4.776-10.667-10.667-10.667v0c-5.891 0-10.667 4.776-10.667 10.667s4.776 10.667 10.667 10.667v0zM9.333 20.427c2.99-0.221 5.642-1.511 7.609-3.484l0-0c1.973-1.967 3.263-4.62 3.482-7.57l0.002-0.040c2.161 1.455 3.564 3.893 3.564 6.658 0 4.418-3.582 8-8 8-2.765 0-5.203-1.403-6.64-3.536l-0.018-0.029z',
        icon:'duibi',
        label:'未授权与授权应用区别',
        desc:'未授权与授权应用都可进行使用，未授权应用对部分功能使用次数、使用人数有限制，授权应用则可体验完整系统功能。',
        color:'#78B8FF'
      }],
      applyList:[],
      data:{
        companyNmae:'',
        deviceNumber:'',
        copyDevNumber:'',
      },
      dialogVisible:false,
      authDevice:'',
      authLoading:false,
      deviceList:[],
      authList:[],
      butLoading:false,
      currentApply:{},
      isPass:false,
      authCode:'0',
      licenseTips:{
        '-100':'授权已过期',
        '-101':'授权即将到期',
        '-102':'设备未授权',
        '-103':'验证未通过',
        '-104':'应用未授权',
        '-105':'授权码格式错误',
        '-106':'授权码不正确'
      },
      myInfo:{}
    };
  },
  created() {
    this.getApplyAuth(false)
  },
  mounted() {
    this.myInfo = getStore({ name: "userInfo" });
  },
  methods: {
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
          default: return {
            path: 'M880 112H144c-17.7 0-32 14.3-32 32v736c0 17.7 14.3 32 32 32h736c17.7 0 32-14.3 32-32V144c0-17.7-14.3-32-32-32zM513.1 518.1l-192 161c-5.2 4.4-13.1.7-13.1-6.1v-62.7c0-2.3 1.1-4.6 2.9-6.1L420.7 512l-109.8-92.2a7.63 7.63 0 0 1-2.9-6.1V351c0-6.8 7.9-10.5 13.1-6.1l192 160.9c3.9 3.2 3.9 9.1 0 12.3zM716 673c0 4.4-3.4 8-7.5 8h-185c-4.1 0-7.5-3.6-7.5-8v-48c0-4.4 3.4-8 7.5-8h185c4.1 0 7.5 3.6 7.5 8v48z',
            viewBox: '64 64 896 896',
            style: 'border-color: rgba(187,187,187,1);border-width: 0px;border-style: solid',
            activeFill: 'rgba(68.08500000000001,154.01999999999998,250.92,1)',
            fill: 'rgba(194.055,195.07500000000002,196.095,1)'
          };
      }
    },
    init(){
      this.applyList.forEach((appItem,appIndex)=>{
        this.authList.forEach((item,index)=>{
          if(appItem.id==item.app){
            appItem.date = item.expireDate
            appItem.isAuth = ['EXPIRING_ERROR','PASS'].indexOf(item.state)!=-1
            appItem.version = item.version
          }
        })
      })
    },
    getApplyAuth(flag){
      getLicense().then(res=>{
        if(res.data.code == 0 ){
          if(!flag){
            this.data.companyNmae = res.data.data.name
            this.data.version = res.data.data.version
          }
          this.applyList = res.data.data.apps || []
          this.authList = res.data.data.apps || []
          this.deviceList = res.data.data.deviceCode
          this.data.deviceNumber = this.data.copyDevNumber = this.deviceList[0]
          this.init()
          this.authLoading = false
          this.$forceUpdate()
        }
      })
    },
    handleClose(){
      this.authDevice = ''
      this.authCode = '0'
      this.butLoading = false
      this.dialogVisible = false
    },
    openSetAuth(item){
      this.currentApply = item
      this.dialogVisible = true
    },
    verLicense: _.debounce(
      function () {
        veriLicense({
          code:this.authDevice,
          clientId:this.myInfo.clientId
        }).then(res=>{
          this.authCode = res.data.code+''
          if(res.data.code == 0){
            this.isPass = true
            this.$notify({
              title: ("修改license成功"),
              type: 'success',
              position: 'bottom-right'
            });
            this.authLoading = true
            setTimeout(()=>{
              this.getApplyAuth(true)
            },1000*1.5)
            this.handleClose()
          }else{
            this.isPass = false
          }
        }).catch(e => {
          this.isPass = false
          this.butLoading = false
        })
      }, 500
    ),
    updateLicense(){
      this.butLoading = true
      this.verLicense()
    },
    getTime(val){
      let today = new Date()
      let lastDay = new Date(val)
      let surplusDay = moment(lastDay).diff(today, 'day')
      // 大于100年代表永久
      if(surplusDay>(365*100)){
        return '永久'
      }else if(surplusDay>1000){
        return `剩余${Math.floor(surplusDay/365)}年`
      }else{
        return `剩余${surplusDay}天`
      }
    },
    onCopy () {
      this.$notify({
        title: '提示',
        message: '复制成功',
        position: 'bottom-right',
        type: 'success'
      });
    },
    onCopyError () {}
  },
  computed:{
    getTips(){
      return this.licenseTips[this.authCode]
    },
  }
};
</script>

<style scoped lang="scss">
.auth-apply-center{
  background: #F5faff;
  height: 100vh;
  width: 100vw;
  box-sizing: border-box;
  .title{
    font-size: 28px;
    color: #101010;
    padding: 20px 44px 0px;
  }
  .help-list{
    display: flex;
    justify-content: space-between;
    margin-top: 34px;
    padding: 0px 44px;
    .help-item{
      width: 500px;
      height: 175px;
      border-radius: 20px;
      display: flex;
      align-items: center;
      padding: 34px;
      box-sizing: border-box;
      &:nth-child(2n){
        margin: 0px 20px;
      }
      .help-left{
        width: 74px;
        height: 74px;
      }
      .help-right{
        color: #fff;
        margin-left: 26px;
        .name{
          font-size: 20px;
        }
        .desc{
          line-height:1.5;
          font-size: 14px;
          margin-top: 10px;
          letter-spacing: 2px;
        }
      }
    }
  }
  .apply-info{
    background-color: #fff;
    height: calc(100vh - 300px);
    margin-top: 41px;
    padding: 20px 44px 20px;
    .apply-title{
      font-size: 28px;
      color: #101010;
      display: flex;
      align-items: center;
      word-break: keep-all;
      .company-info{
        margin-left: 147px;
        display: flex;
        font-size: 14px;
        color: #c2c3c4;
        embed{
          margin-right: 10px;
        }
        .name{
          align-items: center;
          display: flex;
          .el-icon-s-home{
            font-size: 24px;
            color: #101010;
            margin-right: 5px;
          }
          .name{
            letter-spacing: 1px;
          }
        }
        .device{
          margin-left: 50px;
          align-items: center;
          display: flex;
          max-width: 50vw;
          .name{
            letter-spacing: 1px;
          }
          .el-icon-s-platform{
            font-size: 24px;
            color: #101010;
            margin-right: 5px;
          }
        }
        .copy{
          margin-left: 50px;
          color: #449AFB;
          cursor: pointer;
          display: flex;
          align-items: center;
          .el-icon-document-copy{
            margin-left: 4px;
            font-size: 16px;
          }
        }
      }
    }
    .apply-list{
      margin-top: 85px;
      display: flex;
      flex-wrap: wrap;
      .apply-list-item{
        position: relative;
        border-radius: 20px;
        border:1px solid #E5e5e5;
        min-height: 222px;
        width: 455px;
        margin-right: 20px;
        margin-bottom: 50px;
        padding: 43px 10px 6px 20px;
        box-sizing: border-box;
        &:last-child{
          margin-right: 0px;
        }
        .apply-list-icon{
          background-color: #F5Faff;
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
        }
        .apply-list-title{
          color: #101010;
          font-size: 18px;
          font-weight: 600;
        }
        .apply-list-desc{
          font-size: 14px;
          color: #c2c3c4;
          margin-top: 12px;
          margin-bottom: 10px;
          line-height: 1.5;
          height: 70px;
          display: flex;
          align-items: center;
        }
        .apply-list-version{
          display: flex;
          justify-content: space-between;
          align-items: center;
          .version-info{
            display: flex;
            align-items: center;
            flex: 2;
            margin-right: 120px;
            position: relative;
            font-size: 14px;
            letter-spacing: 1px;
            .version-icon{
              width: 60px;
              height: 60px;
              background-image: url('./img/icon1.png');
            }
            .version-left{
              width: 70%;
              height: 30px;
              position: absolute;
              background-image: url('./img/auth-left.png');
              background-repeat: no-repeat;
              left: 49px;
              display: flex;
              align-items: center;
              padding-left: 15px;
              color: #E8D29B;
            }
            .version-right{
              height: 30px;
              width: 135px;
              position: absolute;
              background-image: url('./img/auth.png');
              background-repeat: no-repeat;
              right: 0px;
              display: flex;
              align-items: center;
              justify-content: center;
              padding-left: 4px;
              background-size: 100% 100%;
              box-sizing: border-box;
            }
          }
          .version-auth{
            color: #449AFB;
            font-size: 14px;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            position: absolute;
            right: 10px;
            i{
              font-size: 18px;
              margin-left: 5px;
            }
          }
        }
      }
      .noAuth{
        .apply-list-title{
          color: #C2C3C4;
        }
        .apply-list-version{
          .version-info{
            .version-icon{
              background-image: url('./img/icon.png');
            }
            .version-left{
              color: #c2c3c4;
            }
            .version-right{
              background-image: url('./img/no-auth.png');
            }
          }
        }
      }
    }
  }
  .el-dialog__wrapper {
    transition-duration: .0s;
  }
  .dialog-footer{
    text-align: center;
  }
  /deep/textarea{
    resize: none;
  }
}
.license_tips{
  color: red;
  font-size: 12px;
  margin-top: 5px;
}
</style>

