<template>
  <div class="wx-setting">
    <div style="height:100%;" v-loading="loading || saveLoading">
      <wxForm v-if="!loading" ref="wxForm" :rowData="form"  :keywordJson='keywordJson' :subscribeUrl='newUrl' :isConfig="isConfig" @nextStep="nextStep"></wxForm>
    </div>
  </div>
</template>
<script>
import wxForm from '../tenant/wxForm'
import { client_id } from '@/const/const'
import { getSysConfig, saveSysConfig } from '@/views/upms/views/inviteUser/api'
export default {
  components: {wxForm},
  data(){
    return {
      form: {
        welcomeText:'',
        keywordText:'',
        fileList: [],
      },
      keywordJson:[],
      subscribeUrl:[],
      title: "公众号维护",
      show: false,
      dialogVisible: true,
      loading:false,
      saveLoading:false,
      newUrl:[],
      isConfig: false, // 是否配置
      originInfo: null
    }
  },
  mounted (){
    this.init()
  },
  methods:{
    init(){
      this.loading = true
      this.getSysConfig()
    },
    submit(){
      this.$refs.wxForm.$refs.userForms.submitForm('userForm')
    },
    nextStep(formItem){
      let urlList = []
      if (formItem.fileList && formItem.fileList.length > 0) {
        formItem.fileList.forEach((item,index)=>{
          if(item.response){
            urlList.push(item?.response?.data)
          }else{
            urlList.push(item)
          }
        })
      }
      let temp = {
        clientId: client_id,
        content: '',
        tenantId: this.$store.getters.userInfo.tenantId,
        type: 'WECHAT_OFFICIAL_ACCOUNT_CONFIGURATION'
      }
      if(this.originInfo) {
        temp = JSON.parse(JSON.stringify(this.originInfo))
      }
      temp.content = JSON.stringify({...formItem, subscribeUrl: urlList})
      this.saveLoading = true
      saveSysConfig(temp).then(res=>{
        if(res.data.code == 0){
          this.$notify({
            title: '提示',
            message: '保存成功',
            position: 'bottom-right',
            type: 'success'
          })
          this.saveLoading = false
        }
      })
    },
    // 获取配置信息
    getSysConfig () {
      getSysConfig('WECHAT_OFFICIAL_ACCOUNT_CONFIGURATION').then(res => {
        if(res.data && res.data.code == 0 && res.data.data) {
          this.originInfo = res.data.data
          if(res.data.data.content) {
            this.form = JSON.parse(res.data.data.content)
            this.keywordJson = this.form.keywordJson || []
            this.subscribeUrl = this.form.subscribeUrl || []
            this.subscribeUrl.forEach((item,index)=>{
              this.newUrl.push({
                name:item.originalFileName,
                url:item.fileLink,
                bucketName: item.bucketName,
                fileLink: item.fileLink,
                fileName: item.fileName,
                originalFileName: item.originalFileName,
              })
            })
            this.keywordJson.forEach(item=>{
              item.subscribeUrl= [{
                name:item.picUrl.fileName,
                url:item.picUrl.fileLink,
              }]
            })
          }
          this.isConfig = true
        }else{
          this.isConfig = false
        }
        this.$forceUpdate()
      }).finally(res=>{
        this.loading = false
      })
    }
  }
}
</script>
<style lang="scss">
.wx-setting{
  background-color: #fff;
  height: 100%;
  overflow-y: auto;
}
.wx-setting-no-header-dialog{
  .el-dialog__headerbtn{
    display: none;
  }
  .el-dialog.is-fullscreen{
    .el-dialog__body{
      width: 100%;
      left: 0;
      padding: 0;
    }
  }
}
.wx-setting-dialog{
  height: calc(100% - 105px);
  .el-dialog__body{
    height: calc(100% - 105px);
  }
}
</style>
