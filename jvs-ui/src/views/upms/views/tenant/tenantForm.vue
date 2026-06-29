<template>
  <div class="tenant-info-form">
    <PageHeader :title="title" v-if="title">
    </PageHeader>
    <div class="info-box">
      <div class="left-tip">
        <div class="title">
          <i class="el-icon-info"></i>
          <span>{{$langt('common.explain')}}</span>
        </div>
        <div v-for="(tip, tix) in leftTips" :key="tix" class="left-tip-item">
          <div class="tip-title">{{tip.title}}</div>
          <div class="tip-desc">{{tip.desc}}</div>
        </div>
      </div>
      <div class="left">
        <jvs-form
          ref="userForm"
          refs="userForm"
          :option="formOption"
          :formData="userForm"
          :defalutFormData="userForm"
          @submit="submitHandle"
        >
          <template slot="loginTypesForm">
            <el-checkbox-group
              v-model="userForm.loginTypes"
              @change="loginTypesChange"
            >
              <el-checkbox
                v-for="ci in loginTPList"
                :label="ci.value"
                :key="ci.value"
                >{{ $langt(`tenant.column.loginTypes.dicData[${ci.value}]`) }}</el-checkbox
              >
            </el-checkbox-group>
          </template>
          <template slot="passwordForm">
            <el-input
              :placeholder="$langt(`tenant.passwoedPlaceholder`)"
              v-model="userForm.password"
              show-password
              class="hide-password-input"
            ></el-input>
          </template>
          <template slot="iconForm">
            <!-- :auto-upload="false" -->
            <!-- :on-change="iconHandleFileUploaderChange" -->
            <el-upload
              ref="icoUpload"
              class="avatar-uploader"
              action="/mgr/jvs-auth//upload/jvs-public?module=tenantPicture"
              :limit="1"
              list-type="picture"
              accept=".png"
              :file-list="iconFileList"
              :show-file-list="false"
              :data="{module: '/jvs-ui/tenantPicture/'}"
              :on-remove="iconRemove"
              :headers="headers"
              :on-success="icoSuccessHandle"
              :on-error="icoErrHandle"
            >
              <img v-if="iconFileList.length > 0 && iconFileList[0].url" :src="iconFileList[0].url" class="avatar" @click.stop="imgClick">
              <i v-else class="el-icon-plus avatar-uploader-icon"></i>
              <span slot="tip" class="el-upload__tip">{{$langt(`tenant.suggest`)}} 32 * 32</span>
            </el-upload>
            <i v-if="iconFileList.length > 0 && iconFileList[0].url" class="el-icon-delete delImg" @click="iconRemove(null, [])"></i>
          </template>
          <template slot="logoForm">
            <!-- :auto-upload="false" -->
            <!-- :on-change="logoHandleFileUploaderChange" -->
            <el-upload
              ref="logoUpload"
              class="avatar-uploader"
              action="/mgr/jvs-auth//upload/jvs-public?module=tenantPicture"
              :limit="1"
              list-type="picture"
              :file-list="logoFileList"
              accept=".jpg,.jpeg,.png"
              :show-file-list="false"
              :data="{module: '/jvs-ui/tenantPicture/'}"
              :on-remove="logoRemove"
              :headers="headers"
              :on-success="logoSuccessHandle"
              :on-error="logoErrHandle"
            >
              <img v-if="logoFileList.length > 0 && logoFileList[0].url" :src="logoFileList[0].url" class="avatar" @click.stop="imgClick">
              <i v-else class="el-icon-plus avatar-uploader-icon"></i>
              <span slot="tip" class="el-upload__tip">{{$langt(`tenant.suggest`)}} 200 * 60</span>
            </el-upload>
            <i v-if="logoFileList.length > 0 && logoFileList[0].url" class="el-icon-delete delImg" @click="logoRemove(null, [])"></i>
          </template>
          <template slot="bgImgForm">
            <!-- :auto-upload="false" -->
            <!-- :on-change="bgHandleFileUploaderChange" -->
            <el-upload
              ref="bgUpload"
              class="avatar-uploader"
              action="/mgr/jvs-auth//upload/jvs-public?module=tenantPicture"
              :limit="1"
              list-type="picture"
              :file-list="bgFileList"
              accept=".jpg,.jpeg,.png"
              :show-file-list="false"
              :data="{module: '/jvs-ui/tenantPicture/'}"
              :on-remove="bgRemove"
              :headers="headers"
              :on-success="bgSuccessHandle"
              :on-error="bgErrHandle"
            >
              <img v-if="bgFileList.length > 0 && bgFileList[0].url" :src="bgFileList[0].url" class="avatar" @click.stop="imgClick">
              <i v-else class="el-icon-plus avatar-uploader-icon"></i>
              <div slot="tip" class="el-upload__tip">
                <span v-show="bgImgLimit" style="display:block;color: #F56C6C;">{{$langt(`tenant.picsizeLimit`)}}</span>
                <span>{{$langt(`tenant.suggest`)}} 1920 * 1080</span>
               </div>
            </el-upload>
            <i v-if="bgFileList.length > 0 && bgFileList[0].url" class="el-icon-delete delImg" @click="bgRemove(null, [])"></i>
          </template>
        </jvs-form>
      </div>
    </div>
    <div class="foot-button">
      <el-button size="mini" @click="clickHandle('reset')">{{$langt('form.reset')}}</el-button>
      <el-button size="mini" type="primary" @click="clickHandle('submit')">{{$langt('form.save')}}</el-button>
    </div>
  </div>
</template>
<script>
import {addTenant, editTenant} from '../../api/tenant'
import { encryption, decryption } from "@/util/util";
import PageHeader from '@/components/page-header/PageHeader.vue';
import { tableOption } from './option'
import {enCodeKey} from "@/const/const"
export default {
  name: "tenant-form",
  props: {
    title: {
      type: String
    },
    rowData: {
      type: Object
    },
    submitType: {
      type: String
    },
    permisionFlag: {
      type: String
    }
  },
  components: {
    PageHeader
  },
  data() {
    return {
      userForm: {},
      disSubmit: false,
      formOption: {
        formAlign: 'top',
        cancal: false,
        submitLoading: false,
        column: JSON.parse(JSON.stringify(tableOption.column))
      },
      loginTPList: [
        {label: '帐号密码', value: 'password'},
        {label: '手机号', value: 'phone'},
        // {label: '微信二维码', value: 'wx_qr'},
        {label: 'APP二维码', value: 'app_qr'}
      ],
      iconFileList: [],
      bgFileList: [],
      logoFileList: [],
      imgBase64Array:[],
      ownName: '', // 改变前的名字
      bgImgLimit: false, // 背景图大小限制
      headers: {
        tenantId: this.$store.getters.userInfo.tenantId,
        Authorization: ('Bearer '+this.$store.getters.access_token)
      },
      leftTips: [
        {
          title: '1、拥有组织的独立数据存储',
          desc: '我们的组织拥有独立的数据存储系统，确保所有关键信息和资料的安全与私密性。'
        },
        {
          title: '2、定义自己的门户',
          desc: '组织拥有自定义的门户，充分展示组织的形象、文化和业务特色。'
        },
        {
          title: '3、更便捷的管理自己的组织',
          desc: '我们采用先进的管理系统，实现了对组织内部各项事务的便捷管理。'
        }
      ]
    }
  },
  methods: {
    clickHandle (type) {
      if(this.$refs.userForm) {
        if(type == 'reset') {
          this.$refs.userForm.resetForm('userForm')
        }
        if(type == 'submit') {
          this.$refs.userForm.submitForm('userForm')
        }
      }
    },
    // 提交
    submitHandle (form) {
      if(this.disSubmit) {
        return false
      }
      let obj={}
      obj=JSON.parse(JSON.stringify(form))
      obj.roleName=null
      if(obj.password == '******') {
        obj.password = ''
      }else{
        obj = encryption({
          data: obj,
          key: enCodeKey,
          param: ["password"]
        });
      }
      if(obj.appId) {
        obj = encryption({
          data: obj,
          key: enCodeKey,
          param: ["appId"]
        });
      }
      if(obj.secret) {
        obj = encryption({
          data: obj,
          key: enCodeKey,
          param: ["secret"]
        });
      }
      if(!obj.admin) {
        obj.admin = obj.phone
      }
      // if(!obj.loginDomain) {
      //   obj.loginDomain = 'xxx'
      // }
      if(!obj.loginTypes || obj.loginTypes.length == 0) {
        obj.loginTypes = ['password', 'phone']
      }
      this.formOption.submitLoading = true
      if (this.title=='编辑' || this.title == '基础设置' || this.submitType == 'edit') {
        editTenant(obj).then(res => {
          if (res.data.code==0) {
            if(this.submitType == 'edit') {
              this.$notify({
                title: this.$langt('common.tip'),
                message: this.$langt('common.saveSuccess'),
                position: 'bottom-right',
                type: 'success'
              });
            }else{
              this.$notify({
                title: this.$langt('common.tip'),
                message: this.$langt('tenant.editTenantSuccess'),
                position: 'bottom-right',
                type: 'success'
              });
            }
            this.formOption.submitLoading = false
            this.$emit("close", true)
          }
        }).catch(e => {
          this.formOption.submitLoading = false
        })
      } else {
        addTenant(obj).then(res => {
          if (res.data.code==0) {
            this.$notify({
              title: this.$langt('common.tip'),
              message: this.$langt('tenant.addTenantSuccess'),
              position: 'bottom-right',
              type: 'success'
            });
            this.formOption.submitLoading = false
            this.$emit("close", true)
          }
        }).catch(e => {
          this.formOption.submitLoading = false
        })
      }
    },
    // 用户名不可重复
    noRepeatName (name) {
      for(let i in this.tableData) {
        if(name != this.ownName && this.tableData[i].userName == name) {
          this.disSubmit = true
          return false
        }
      }
      this.disSubmit = false
    },
    // 登录类型change
    loginTypesChange (val) {
      this.formOption.column.filter(item => {
        if(['appId', 'secret'].indexOf(item.prop) > -1) {
          if(val.indexOf("wx_qr") > -1) {
            item.display = false // 放了微信一定要放出来！！！！！！！！！！！！！！！！！！ true
          }else{
            item.display = false
          }
        }
      })
    },
    // icon 上传图片
    iconHandleFileUploaderChange (file) {
      const self = this
      let reader = new FileReader()
      reader.readAsDataURL(file.raw)
      reader.onload  = function(event){
        let img_base64 = this.result
        self.imgBase64Array.push(img_base64)
        self.userForm.icon = img_base64
        self.iconFileList = [{
          url: img_base64
        }]
      }
    },
    icoSuccessHandle (res, file, fileList) {
      if(res.code == 0) {
        this.$notify({
          title: this.$langt('common.tip'),
          message: this.$langt('common.uploadSuccess'),
          position: 'bottom-right',
          type: 'success'
        });
        this.userForm.icon = res.data.fileLink
        this.iconFileList = [{
          url: res.data.fileLink
        }]
      }else{
        this.$refs.icoUpload.clearFiles()
        this.$notify({
          title: this.$langt('common.tip'),
          message: res.msg,
          position: 'bottom-right',
          type: 'error'
        });
      }
    },
    icoErrHandle (err, file, fileList) {
      this.$refs.icoUpload.clearFiles()
      this.$notify({
        title: this.$langt('common.tip'),
        message: err,
        position: 'bottom-right',
        type: 'error'
      });
    },
    iconRemove (file, fileList) {
      this.iconFileList = fileList
      this.userForm.icon = ""
    },
    // 背景 上传图片
    bgHandleFileUploaderChange (file) {
      this.bgImgLimit = false
      if(file.size > 20971520) {
        this.bgImgLimit = true
        this.imgBase64Array = []
        this.userForm.bgImg = ""
        this.$refs.bgUpload.clearFiles()
      }else{
        console.log(file)
        // const self = this
        // let reader = new FileReader()
        // reader.readAsDataURL(file.raw)
        // reader.onload  = function(event){
        //   let img_base64 = this.result
        //   self.imgBase64Array.push(img_base64)
        //   self.userForm.bgImg = img_base64
        //   self.bgFileList = [{
        //     url: img_base64
        //   }]
        // }
      }
    },
    bgSuccessHandle (res, file, fileList) {
      if(res.code == 0) {
        this.$notify({
          title: this.$langt('common.tip'),
          message: this.$langt('common.uploadSuccess'),
          position: 'bottom-right',
          type: 'success'
        });
        this.userForm.bgImg = res.data.fileLink
        this.bgFileList = [{
          url: res.data.fileLink
        }]
      }else{
        this.$refs.bgUpload.clearFiles()
        this.$notify({
          title: this.$langt('common.tip'),
          message: res.msg,
          position: 'bottom-right',
          type: 'error'
        });
      }
    },
    bgErrHandle (err, file, fileList) {
      this.$refs.bgUpload.clearFiles()
      this.$notify({
        title: this.$langt('common.tip'),
        message: err,
        position: 'bottom-right',
        type: 'error'
      });
    },
    bgRemove (file, fileList) {
      this.bgFileList = fileList
      this.userForm.bgImg = ""
    },
    // logo 上传图片
    logoHandleFileUploaderChange (file) {
      const self = this
      let reader = new FileReader()
      reader.readAsDataURL(file.raw)
      reader.onload  = function(event){
        let img_base64 = this.result
        self.imgBase64Array.push(img_base64)
        self.userForm.logo = img_base64
        self.logoFileList = [{
          url: img_base64
        }]
      }
    },
    logoSuccessHandle (res, file, fileList) {
      if(res.code == 0) {
        this.$notify({
          title: this.$langt('common.tip'),
          message: this.$langt('common.uploadSuccess'),
          position: 'bottom-right',
          type: 'success'
        });
        this.userForm.logo = res.data.fileLink
        this.logoFileList = [{
          url: res.data.fileLink
        }]
      }else{
        this.$refs.logoUpload.clearFiles()
        this.$notify({
          title: this.$langt('common.tip'),
          message: res.msg,
          position: 'bottom-right',
          type: 'error'
        });
      }
    },
    logoErrHandle (err, file, fileList) {
      this.$refs.logoUpload.clearFiles()
      this.$notify({
        title: this.$langt('common.tip'),
        message: err,
        position: 'bottom-right',
        type: 'error'
      });
    },
    logoRemove (file, fileList) {
      this.logoFileList = fileList
      this.userForm.logo = ""
    },
    // 记录改变前的名字
    changebefore (name) {
      this.ownName = name
    },
    imgClick () {
      // 点击图片不再上传
    }
  },
  created () {
    this.leftTips.filter((col, cix) => {
      col.title = this.$langt(`tenant.leftTips[${cix}].title`)
      col.desc = this.$langt(`tenant.leftTips[${cix}].desc`)
    })
    this.formOption.submitBtn = this.$permissionMatch(this.permisionFlag ? this.permisionFlag : "")
    this.formOption.emptyBtn = this.$permissionMatch(this.permisionFlag ? this.permisionFlag : "")
    this.formOption.column.filter(item => { item.span=24 })
    this.formOption.submitBtnText = this.$langt('form.save')
    if(this.rowData && JSON.stringify(this.rowData) != "{}") {
      this.userForm=JSON.parse(JSON.stringify(this.rowData))
      if(this.userForm.loginTypes) {
        this.loginTypesChange(this.userForm.loginTypes)
      }
      if(this.userForm.appId) {
        this.userForm = decryption({
          data: this.userForm,
          key: enCodeKey,
          param: ["appId"]
        });
      }
      if(this.userForm.secret) {
        this.userForm = decryption({
          data: this.userForm,
          key: enCodeKey,
          param: ["secret"]
        });
      }
      if(this.userForm.icon) {
        this.iconFileList.push({url: this.userForm.icon})
      }
      if(this.userForm.bgImg) {
        this.bgFileList.push({url: this.userForm.bgImg})
      }
      if(this.userForm.logo) {
        this.logoFileList.push({url: this.userForm.logo})
      }
      // console.log(this.userForm)
    }else{
      this.userForm = {
        loginTypes: []
      }
    }
  },
}
</script>
<style lang="scss" scoped>
.info-box {
  background: #fff;
  display: flex;
  .left-tip{
    padding: 16px 16px 16px 24px;
    width: 240px;
    background: #EDF4FF;
    box-sizing: border-box;
    .title{
      display: flex;
      align-items: center;
      i{
        font-size: 16px;
        color: #1E6FFF;
        margin-right: 2px;
      }
      span{
        height: 18px;
        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
        font-weight: 400;
        font-size: 14px;
        color: #1E6FFF;
        line-height: 18px;
      }
    }
    .left-tip-item{
      margin-top: 12px;
      .tip-title{
        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
        font-weight: 400;
        font-size: 12px;
        color: #363B4C;
        line-height: 16px;
      }
      .tip-desc{
        margin-top: 4px;
        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
        font-weight: 400;
        font-size: 12px;
        color: #6F7588;
        line-height: 20px;
        word-break: break-word;
      }
    }
  }
  .left{
    flex: 1;
    margin-left: 24px;
    margin-right: 32px;
    padding-top: 24px;
    overflow: hidden;
    /deep/.jvs-form{
      .form-item-btn{
        display: none;
      }
    }
  }
  .img-list{
    display: flex;
    justify-content: space-between;
    div{
      width: 46%;
      border: 1px solid #e5e5e5;
      border-radius: 10px;
      padding: 10px;
      margin-bottom: 10px;
      cursor: pointer;
    }
    .activeStyle{
      border-color: #409EFF;
      position: relative;
      color: #409EFF;
    }
    .activeStyle::after{
      content: "√";
      position: absolute;
      top: 5px;
      right: 10px;
      color: #409EFF;
      font-size: 20px;
    }
    p{
      margin: 0;
      padding: 0;
      text-align: center;
    }
    img{
      display: block;
      width: 100%;
      height: 200px;
    }
  }
}
.foot-button{
  width: 100%;
  height: 60px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  border-top: 1px solid #EEEFF0;
  box-sizing: border-box;
}
.el-upload__tip{
  margin-left: 10px;
}
.tenant-info-form{
  width: 624px;
  /deep/.avatar-uploader .el-upload {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
  }
  .avatar-uploader .el-upload__tip{
    display: block;
    margin: 0;
  }
  .avatar-uploader .el-upload:hover {
    border-color: #409EFF;
  }
  .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 178px;
    height: 178px;
    line-height: 178px;
    text-align: center;
  }
  .avatar {
    width: 178px;
    height: 178px;
    display: block;
  }
  .delImg{
    position: absolute;
    left: 160px;
    top: 5px;
    z-index: 999;
    cursor: pointer;
  }
  .info-box{
    margin: 0;
    padding: 0;
    .left{
      .el-form{
        .el-row{
          display: flex;
          flex-wrap: wrap;
          .el-col{
            padding: 0 5px;
          }
        }
      }
    }
  }
}
/deep/.hide-password-input{
  .el-input__suffix{
    .el-input__suffix-inner{
      display: none;
    }
  }
}
</style>
