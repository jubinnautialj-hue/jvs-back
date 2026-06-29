<template>
  <div class="dictionaries-manage">
    <platform-page-header title="终端管理" :is-more="false" :desc="$langt('application.desc')"/>
    <jvs-table
      v-if="$functionEnable('终端管理')"
      :option="tableOption"
      :loading="tableLoading"
      :data="tableData"
      pageheadertitle='终端管理'
      :page="page"
      @on-load="getList"
      @search-change="searchChange"
      @delRow="delRowHandle"
    >
      <template slot="menu" slot-scope="scope">
        <jvs-button size="mini" type="text" permisionFlag="jvs_apply_edit" @click="handleView(scope.row, 'edit')">{{$langt('table.edit')}}</jvs-button>
        <jvs-button size="mini" type="text" permisionFlag="" @click="handleView(scope.row, 'view')">{{$langt('table.view')}}</jvs-button>
        <jvs-button size="mini" type='text' permisionFlag="jvs_apply_disabled" v-if="scope.row.enable" @click="disableHandle(scope.row)">{{$langt('table.disabled')}}</jvs-button>
        <jvs-button size="mini" type='text' permisionFlag="jvs_apply_enable" v-else @click="disableHandle(scope.row)">{{$langt('table.enable')}}</jvs-button>
        <!-- <jvs-button size="mini" type="text" @click="handlePermission(scope.row)">权限</jvs-button> -->
        <jvs-button size="mini" type='text' permisionFlag="jvs_apply_delete" v-if="!scope.row.enable" @click="delRowHandle(scope.row)"><span style="color: #F56C6C;">{{$langt('table.delete')}}</span></jvs-button>
      </template>
      <template slot="menuLeft">
        <jvs-button type="primary" icon="el-icon-plus" permisionFlag="jvs_apply_add" @click="handleAdd(null)">{{$langt('table.add')}}</jvs-button>
      </template>
    </jvs-table>
    <div class="no-permission-img" v-else>
      <img src="@/const/img/noPermission.jpg" alt=""/>
    </div>
    <el-dialog
      :title="title"
      :visible.sync="dialogVisible"
      width="75%"
      :close-on-click-modal="false"
      :before-close="handleClose">
      <div class="client-info-box" v-if="dialogVisible">
        <jvs-form v-if="dialogVisible" :option="formOption" :formData="formData" @submit="handleAddSubmit">
          <template slot="iconForm">
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
              :disabled="title == $langt('table.view') ? true : false"
            >
              <img v-if="iconFileList.length > 0 && iconFileList[0].url" :src="iconFileList[0].url" class="avatar" @click.stop="imgClick">
              <i v-else class="el-icon-plus avatar-uploader-icon"></i>
              <span slot="tip" class="el-upload__tip">{{$langt('application.suggest')}} 32 * 32</span>
            </el-upload>
            <i v-if="iconFileList.length > 0 && iconFileList[0].url" class="el-icon-delete delImg" @click="iconRemove(null, [])"></i>
          </template>
          <template slot="logoForm">
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
              :disabled="title == $langt('table.view') ? true : false"
            >
              <img v-if="logoFileList.length > 0 && logoFileList[0].url" :src="logoFileList[0].url" class="avatar" @click.stop="imgClick">
              <i v-else class="el-icon-plus avatar-uploader-icon"></i>
              <span slot="tip" class="el-upload__tip">{{$langt('application.suggest')}} 200 * 60</span>
            </el-upload>
            <i v-if="logoFileList.length > 0 && logoFileList[0].url" class="el-icon-delete delImg" @click="logoRemove(null, [])"></i>
          </template>
          <template slot="bgImgForm">
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
              :disabled="title == $langt('table.view') ? true : false"
            >
              <img v-if="bgFileList.length > 0 && bgFileList[0].url" :src="bgFileList[0].url" class="avatar" @click.stop="imgClick">
              <i v-else class="el-icon-plus avatar-uploader-icon"></i>
              <div slot="tip" class="el-upload__tip">
                <span v-show="bgImgLimit" style="display:block;color: #F56C6C;">{{$langt('application.picsizeLimit')}}</span>
                <span>{{$langt('application.suggest')}} 1920 * 1080</span>
                </div>
            </el-upload>
            <i v-if="bgFileList.length > 0 && bgFileList[0].url" class="el-icon-delete delImg" @click="bgRemove(null, [])"></i>
          </template>
        </jvs-form>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import {getAppList, addApp, editApp, delApp, getAppById, enableDisApp} from './api'
import tableForm from '@/components/basic-assembly/tableForm'
import PlatformPageHeader from "@/components/page-header/PlatformPageHeader";
export default {
  name: 'application-manage',
  components: {PlatformPageHeader, tableForm},
  data () {
    return {
      title: this.$langt('table.add'),
      dialogVisible: false,
      tableLoading: false,
      queryParams: {},
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
      },
      rowData: {}, // 行数据
      tableData: [],
      formData: {},
      formOption: {
        submitLoading: false,
        cancal: false,
        column: [
          {
            label: '终端名称',
            prop: 'name',
            maxlength: 10,
            disabled: false,
            rules: [{ required: true, message: '请输入终端名称', trigger: ['blur', 'change'] }]
          },
          {
            label: '描述',
            prop: 'describes',
            disabled: false,
            rules: [{ required: true, message: '请输入描述', trigger: 'blur' }],
          },
          {
            label: 'appId',
            prop: 'appKey',
            disabled: false,
            rules: [{ required: true, message: '请输入appId', trigger: 'blur' }],
          },
          {
            label: 'app_secret',
            prop: 'appSecret',
            disabled: true,
            display: false
          },
          // {
          //   label: 'ICON',
          //   prop: 'icon',
          //   span: 24,
          //   hide: true,
          //   formSlot: true,
          // },
          // {
          //   label: 'LOGO',
          //   prop: 'logo',
          //   hide: true,
          //   formSlot: true,
          //   span: 24,
          // },
          // {
          //   label: '背景图',
          //   prop: 'bgImg',
          //   hide: true,
          //   formSlot: true,
          //   span: 24,
          // },
        ]
      },
      tableOption: {
        // align: 'center',
        // menuAlign: 'center',
        showOverflow: true,
        search: true,
        viewBtn: false,
        addBtn: false,
        editBtn: false,
        delBtn: false,
        cancal: false,
        page: true,
        submitLoading: false,
        column: [
          {
            label: '终端名称',
            prop: 'name',
            maxlength: 10,
            search: true,
            searchSpan: 4,
            rules: [{ required: true, message: '请输入终端名称', trigger: ['blur', 'change'] }]
          },
          {
            label: '描述',
            prop: 'describes',
            rules: [{ required: true, message: '请输入描述', trigger: 'blur' }],
          },
          {
            label: 'appId',
            prop: 'appKey',
            disabled: true,
            rules: [{ required: true, message: '请输入appId', trigger: 'blur' }],
          },
          {
            label: 'app_secret',
            prop: 'appSecret',
            hide: true,
            disabled: true,
            rules: [{ required: true, message: '请输入appSecret', trigger: 'blur' }],
            addDisplay: false,
          },
        ]
      },
      iconFileList: [],
      bgFileList: [],
      logoFileList: [],
      imgBase64Array:[],
      bgImgLimit: false, // 背景图大小限制
      headers: {
        tenantId: this.$store.getters.userInfo.tenantId,
        Authorization: ('Bearer '+this.$store.getters.access_token)
      },
    }
  },
  created () {
    this.tableOption.column.filter(col => {
      if(col.label) {
        col.label = this.$langt(`application.column.${col.prop}.label`)
      }
      if(col.rules && col.rules.length > 0) {
        col.rules[0].message = this.$langt(`application.column.${col.prop}.placeholder`)
      }
    })
    this.formOption.column.filter(col => {
      if(col.label) {
        col.label = this.$langt(`application.column.${col.prop}.label`)
      }
      if(col.rules && col.rules.length > 0) {
        col.rules[0].message = this.$langt(`application.column.${col.prop}.placeholder`)
      }
    })
  },
  methods: {
    getList (page) {
      let query={
        current: this.page.currentPage,
        size: this.page.pageSize
      }
      this.tableLoading = true
      let temp = JSON.parse(JSON.stringify(this.queryParams))
      getAppList(Object.assign(query, temp)).then(res => {
        if (res.data.code==0) {
          this.page.total=res.data.data.total
          this.page.currentPage=res.data.data.current
          this.tableData=res.data.data.records
          this.tableLoading = false
        }
      })
    },
    searchChange (form) {
      this.queryParams = form
      this.getList()
    },
    // 关闭弹窗
    handleClose() {
      this.formData = {}
      this.dialogVisible = false
      this.iconFileList = []
      this.bgFileList = []
      this.logoFileList = []
    },
    // 新增终端
    handleAdd () {
      this.title = this.$langt('table.add')
      this.formOption.submitBtn = true
      this.formOption.emptyBtn = true
      this.formOption.column.forEach((item, key) => {
        item.disabled = false
        if (key === 3) {
          item.display = false
        }
      })
      this.dialogVisible = true
    },
    // 查看权限
    handlePermission(row) {
      console.log(row)
    },
    // 查看终端
    handleView(row, type) {
      if(type == 'view') {
        this.title = this.$langt('table.view')
        this.formOption.submitBtn = false
        this.formOption.emptyBtn = false
      }else{
        this.title = this.$langt('table.edit')
        this.formOption.submitBtn = true
        this.formOption.emptyBtn = true
      }
      this.formOption.column.forEach((item, key) => {
        if(type == 'view') {
          item.disabled = true
        }else{
          item.disabled = false
        }
        if (key === 3) {
          item.display = true
        }
      })
      getAppById(row.id).then(res => {
        if (res.data && res.data.code == 0) {
          this.formData = JSON.parse(JSON.stringify(res.data.data))
          if(this.formData.icon) {
            this.iconFileList.push({url: this.formData.icon})
          }
          if(this.formData.bgImg) {
            this.bgFileList.push({url: this.formData.bgImg})
          }
          if(this.formData.logo) {
            this.logoFileList.push({url: this.formData.logo})
          }
          this.dialogVisible = true
        }
      })
    },
    // 新增终端提交
    handleAddSubmit(form) {
      this.formOption.submitLoading = true
      let fun = null
      let str = ''
      if(this.title == this.$langt('table.add')) {
        fun = addApp
        str = this.$langt('application.addSuccess')
      }else{
        fun = editApp
        str = this.$langt('application.editSuccess')
      }
      fun(form).then(res => {
        if(res.data.code == 0) {
          this.$notify({
            title: this.$langt('common.tip'),
            message: str,
            position: 'bottom-right',
            type: 'success'
          });
          this.formOption.submitLoading = false
          this.handleClose()
          this.getList()
        } else {
          this.formOption.submitLoading = false
          this.handleClose()
        }
      }).catch(e => {
        this.formOption.submitLoading = false
        this.handleClose()
      })
    },
    // 删除
    delRowHandle (row) {
      delApp(row.id).then(res => {
        if(res.data.code == 0) {
          this.$notify({
            title: this.$langt('common.tip'),
            message: this.$langt('application.delDitSuccess'),
            position: 'bottom-right',
            type: 'success'
          });
          this.getList()
        }
      })
    },
    icoSuccessHandle (res, file, fileList) {
      if(res.code == 0) {
        this.$notify({
          title: this.$langt('common.tip'),
          message: this.$langt('common.uploadSuccess'),
          position: 'bottom-right',
          type: 'success'
        });
        this.formData.icon = res.data.fileLink
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
      this.formData.icon = ""
    },
    bgSuccessHandle (res, file, fileList) {
      if(res.code == 0) {
        this.$notify({
          title: this.$langt('common.tip'),
          message: this.$langt('common.uploadSuccess'),
          position: 'bottom-right',
          type: 'success'
        });
        this.formData.bgImg = res.data.fileLink
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
      this.formData.bgImg = ""
    },
    logoSuccessHandle (res, file, fileList) {
      if(res.code == 0) {
        this.$notify({
          title: this.$langt('common.tip'),
          message: this.$langt('common.uploadSuccess'),
          position: 'bottom-right',
          type: 'success'
        });
        this.formData.logo = res.data.fileLink
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
      this.formData.logo = ""
    },
    imgClick () {
      // 点击图片不再上传
    },
    // 禁用 启用
    disableHandle (row) {
      let str = this.$langt('application.disableStr')
      if(row.enable) {
        str = this.$langt('application.disableStr')
      }else{
        str = this.$langt('application.enableStr')
      }
      this.$confirm(str, this.$langt('common.tip'), {
        confirmButtonText: this.$langt('common.confirm'),
        cancelButtonText: this.$langt('common.cancel'),
        type: 'warning'
      }).then(() => {
        enableDisApp(row.id, row.enable).then(res => {
          if (res.data.code==0) {
            this.$notify({
              title: this.$langt('common.tip'),
              message: row.enable ? this.$langt('application.disableSuccess') : this.$langt('application.enableSuccess'),
              position: 'bottom-right',
              type: 'success'
            });
            this.getList()
          }
        })
      }).catch(_ => {})
    },
  }
}
</script>
<style lang="scss">
.client-info-box{
  .el-upload__tip{
    margin-left: 10px;
  }
  .avatar-uploader .el-upload {
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
}

</style>
<style lang="scss" scoped>
.dictionaries-manage{
  height: 100%;
  background-color: #ffffff;
  /deep/.platform-page-header{
    padding: 0;
    padding-bottom: 15px;
    .title{
      display: none;
    }
  }
  /deep/.table-body-box{
    .el-table{
      .el-table__body-wrapper{
        height: calc(100vh - 262px)!important;
      }
    }
  }
}
</style>
