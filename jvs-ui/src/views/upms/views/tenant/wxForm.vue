<template>
  <div class="tenant-info-form">
    <div class="top">
      <el-alert
        :closable="false"
        type="info">
        <template slot="title">
          <div class="alert-box">
            <i class="el-icon-info"/>
            <span>{{$langt('settings.tip1')}}<span> {{$langt('settings.tip2')}}</span></span>
          </div>
        </template>
      </el-alert>
    </div>
    <div class="tab-list">
      <div :class="{'tab-item': true, 'active': activeName == 'follow'}" @click="changeTab('follow')">{{$langt('settings.follow')}}</div>
      <div :class="{'tab-item': true, 'active': activeName == 'keyword'}" @click="changeTab('keyword')">{{$langt('settings.keyword')}}</div>
    </div>
    <div v-show="activeName == 'follow'" class="follow-box">
      <div class="follow-tip">{{$langt('settings.tip3')}}</div>
      <div v-if="userForm && isConfig" class="info-box">
        <div class="left">
          <jvs-form
            ref="userForms"
            refs="userForm"
            :option="formOption"
            :formData="userForm"
            :defalutFormData="userForm"
            @submit="submitHandle"
          >
          </jvs-form>
        </div>
        <div class="wx-img">
          <img src="@/components/application/img/wxImg.png" alt=""/>
        </div>
      </div>
      <div v-else class="wx-massage">
        <el-alert :title="$langt('settings.warning')" type="warning" :closable="false"></el-alert>
        <div v-if="false" class="wx-btn">
          <jvs-button type="primary" icon="el-icon-video-play" @click="handleConfig">{{$langt('settings.rightnow')}}</jvs-button>
        </div>
      </div>
    </div>
    <div v-show="activeName == 'keyword'" class="keyword-box">
      <div class="left">
        <jvs-table
          pageheadertitle="关键字管理"
          :option="taboption"
          :data="tableData"
          :loading="tableLoading"
          @on-load="getList"
          @search-change="searchChange"
          @addRow="addRowHandle"
          @editRow="editRowHandle"
          @delRow="delRowHandle"
        >
          <template slot="subscribeUrl" slot-scope="scope">
            <img :src="scope.row.picUrl.fileLink" width="80px" height="50px">
          </template>
          <template slot="url" slot-scope="scope">
            <span @click="handleUrl(scope.row.url)" style="cursor: pointer;color: #3471FF">{{$langt('settings.jump')}}</span>
          </template>
        </jvs-table>
      </div>
      <div class="wx-img">
        <img src="@/components/application/img/wxkey.png" alt=""/>
      </div>
    </div>
    <div class="footer-button">
      <el-button type="primary" :loading="formOption.submitLoading" @click="submitClick">{{$langt('form.submit')}}</el-button>
    </div>
  </div>
</template>
<script>
import PageHeader from '@/components/page-header/PageHeader.vue';
import store from "@/store";
export default {
  name: "tenant-form",
  props: {
    title: {
      type: String
    },
    keywordJson:{
      type:Array,
      default(){
        return []
      }
    },
    subscribeUrl:{
      type:Array,
      default(){
        return []
      }
    },
    rowData: {
      type: Object,
      default(){
        return {
          welcomeText:'',
          keywordText:''
        }
      }
    },
    submitType: {
      type: String
    },
    permisionFlag: {
      type: String
    },
    isConfig: {
      type: Boolean
    }
  },
  components: {
    PageHeader
  },
  data() {
    return {
      activeName: 'follow',
      tableLoading: false,
      taboption: {
        menuAlign: 'center',
        align: 'center',
        // showOverflow: true,
        search: true,
        addBtn: this.$permissionMatch(""),
        editBtn: this.$permissionMatch(""),
        delBtn: this.$permissionMatch(""),
        viewBtn: false,
        cancal: false,
        // page: true,
        submitLoading: false,
        column: [
          {
            label: "关键字",
            prop: "key",
            rules: [{ required: true, message: '请输入关键字', trigger: 'blur' }],
          },
          {
            label: "图文消息描述",
            prop: "description",
            hide: true,
            rules: [{ required: true, message: '请输入图文消息描述', trigger: 'blur' }],
          },
          {
            label: "图文消息标题",
            prop: "title",
            rules: [{ required: true, message: '请输入图文消息标题', trigger: 'blur' }],
          },
          {
            label: "图文消息图片",
            prop: "subscribeUrl",
            type: 'imageUpload',
            fileList:[],
            action:'/mgr/jvs-auth/upload/jvs-public',
            limit:1,
            headers:{
              "Authorization": 'Bearer ' + store.getters.access_token
            },
            uploadHttp: {
              parameters: {
                module: '/jvs-ui/wxsetting/'
              }
            },
            slot:true
          },
          {
            label: "跳转链接",
            prop: "url",
            slot:true
          }
        ]
      }
      ,
      tableData: JSON.parse(JSON.stringify(this.keywordJson)),
      page:{
        currentPage:1,
        size:20,
        total:0
      },
      userForm: JSON.parse(JSON.stringify(this.rowData)),
      disSubmit: false,
      formOption: {
        btnHide: true,
        cancal: false,
        submitLoading: false,
        emptyBtn:false,
        submitBtn:true,
        formAlign: 'top',
        column: [
          {
            label: '欢迎语',
            prop: 'welcomeText',
            type:'htmlEditor',
            autoSize: {minRows:4,maxRows:4},
            showwordlimit: true,
            rules: [
              { required: true, message: '请输入欢迎语', trigger: 'blur' }
            ]
          },
          {
            label: '关键字信息',
            prop: 'keywordText',
            search: true,
            type:'htmlEditor',
            autoSize: {minRows:4,maxRows:4},
            searchSpan: 4,
            rules: [
              { required: true, message: '请输入关键字信息', trigger: 'blur' }
            ]
          },
          {
            label: '图片',
            prop: 'fileList',
            type: 'imageUpload',
            action: '/mgr/jvs-auth/upload/jvs-public',
            fileList: [],
            limit: 3,
            headers: {
              Authorization: 'Bearer ' + store.getters.access_token
            },
            uploadHttp: {
              parameters: {
                module: '/jvs-ui/wxsetting/'
              }
            },
          },
        ]
      }
    };
  },
  methods: {
    changeTab (tab) {
      this.activeName = tab
      this.$forceUpdate()
    },
    searchChange(){

    },
    getList() {
    },
    addRowHandle(form){
      console.log(form)
      this.taboption.column[3].fileList=[]
      this.$forceUpdate()
      this.keywordJson.push(
        {
          ...form,
          picUrl:{
            bucketName:'jvs-public',
            fileName:form.subscribeUrl ? form.subscribeUrl[0].fileName : '',
            originalName:form.subscribeUrl ? form.subscribeUrl[0].name : '',
            fileLink:form.subscribeUrl ? form.subscribeUrl[0].url : ''
          }
        }
      )
    },
    delRowHandle(val,index){
      this.keywordJson.splice(index,1)
    },
    editRowHandle(val,index){
      this.keywordJson.splice(index,1,{
        ...val,
        picUrl:{
          bucketName:'jvs-public',
          fileName:val.subscribeUrl[0].fileName,
          originalName:val.subscribeUrl[0].name,
          fileLink:val.subscribeUrl[0].url
        }
      })
    },
    // 配置公众号
    handleConfig() {},
    // 提交
    submitClick () {
      if(this.$refs.userForms) {
        this.$refs.userForms.submitForm('userForm')
      }
    },
    submitHandle (form) {
      form.keywordJson = JSON.parse(JSON.stringify(this.keywordJson))
      this.$emit('nextStep',form)
    },
    // 点击跳转
    handleUrl(url) {
      this.$openUrl(url, 'blank')
    }
  },
  created () {
    this.taboption.column.filter(col => {
      if(col.label) {
        col.label = this.$langt(`settings.column.${col.prop}.label`)
      }
      if(col.rules && col.rules.length > 0) {
        col.rules[0].message = this.$langt(`settings.column.${col.prop}.placeholder`)
      }
    })
    this.formOption.column.filter(col => {
      if(col.label) {
        col.label = this.$langt(`settings.column.${col.prop}.label`)
      }
      if(col.rules && col.rules.length > 0) {
        col.rules[0].message = this.$langt(`settings.column.${col.prop}.placeholder`)
      }
    })

  },
  watch:{
    keywordJson(newVal,oldVal){
      this.tableData =  JSON.parse(JSON.stringify(this.keywordJson)),
        this.$forceUpdate()
    },
    subscribeUrl(newVal,oldVal){
      this.fileList =  JSON.parse(JSON.stringify(this.subscribeUrl)),
        this.$forceUpdate()
    },
    rowData(newVal,oldVal){
      this.userForm = JSON.parse(JSON.stringify(this.rowData))
      this.$forceUpdate()
    }
  }
}
</script>
<style lang="scss" scoped>
.tenant-info-form{
  width: 100%;
  height: 100%;
  .top{
    padding: 16px 24px;
    background-color: #fff;
    /deep/.el-alert--info.is-light{
      background: #F2F7FF;
      border-radius: 4px;
      .el-icon-info{
        margin-right: 8px;
        color: #1E6FFF;
      }
      span{
        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
        font-weight: 400;
        font-size: 14px;
        color: #363B4C;
        >span{
          color: #1E6FFF;
          cursor: pointer;
          text-decoration: underline;
        }
      }
    }
  }
  .tab-list{
    width: 100%;
    padding: 0 24px;
    border-bottom: 1px solid #EEEFF0;
    box-sizing: border-box;
    display: flex;
    align-items: center;
    .tab-item{
      margin-right: 40px;
      height: 18px;
      padding: 16px 0;
      font-family: Microsoft YaHei-Regular, Microsoft YaHei;
      font-weight: 400;
      font-size: 14px;
      color: #6F7588;
      line-height: 18px;
      position: relative;
      cursor: pointer;
      &.active{
        font-family: Microsoft YaHei-Bold, Microsoft YaHei;
        font-weight: 700;
        font-size: 14px;
        color: #1E6FFF;
      }
      &.active::after{
        content: '';
        width: 100%;
        height: 2px;
        background: #1E6FFF;
        border-radius: 2px 0px 2px 0px;
        position: absolute;
        bottom: -1px;
        left: 0;
      }
    }
  }
  .follow-box{
    height: calc(100% - 210px);
    padding: 0 24px;
    .follow-tip{
      margin: 16px 0;
      font-family: Source Han Sans-Regular, Source Han Sans;
      font-weight: 400;
      font-size: 14px;
      color: #363B4C;
      line-height: 20px;
    }
    .info-box{
      height: calc(100% - 52px);
      display: flex;
      justify-content: space-between;
      .left{
        flex: 1;
        margin-right: 88px;
        /deep/.jvs-form{
          .el-form-item__label{
            font-family: Microsoft YaHei-Bold, Microsoft YaHei;
            font-weight: 700;
            font-size: 14px;
            color: #363B4C;
          }
          .w-e-text-container{
            height: 150px!important;
          }
          .w-e-text{
            min-height: unset!important;
          }
        }
      }
      .wx-img{
        height: 100%;
        margin-right: 50px;
        img{
          display: block;
          height: 100%;
        }
      }
    }
  }
  .keyword-box{
    height: calc(100% - 210px);
    padding: 16px 24px;
    display: flex;
    justify-content: space-between;
    .left{
      flex: 1;
      margin-right: 47px;
      overflow: hidden;
      /deep/.jvs-table{
        height: 100%;
        .jvs-table-top{
          height: unset;
        }
        .table-body-box{
          height: calc(100% - 52px);
          .el-table{
            height: 100%;
            .el-table__body-wrapper{
              height: calc(100% - 40px)!important;
            }
          }
        }
      }
    }
    .wx-img{
      height: 100%;
      margin-right: 24px;
      img{
        display: block;
        height: 100%;
      }
    }
  }
  .wx-massage{
    width: 400px;
    margin-bottom: 16px;
  }
  .wx-btn{
    margin: 16px 0;
  }
  .footer-button{
    position: absolute;
    left: 0;
    bottom: 0;
    height: 72px;
    background: #fff;
    width: 100%;
    display: flex;
    align-items: center;
    padding: 0 24px;
    border-top: 1px solid #EEEFF0;
    box-sizing: border-box;
    /deep/.el-button{
      width: 88px;
      height: 32px;
      background: #1E6FFF;
      border-radius: 4px;
      span{
        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
        font-weight: 400;
        font-size: 14px;
        color: #fff;
      }
    }
  }
}

</style>
