<template>
  <div class="list-design-print">
    <div style="display:flex;align-items:center;margin-bottom:10px;">
      <!-- <el-button type="primary" size="mini" @click="oprateTemplate(null)">新建模板</el-button> -->
      <el-button type="primary" size="mini" @click="importVisible = true;">点击上传</el-button>
    </div>
    <el-table
      :data="templateList"
      style="border: 1px solid #EBEEF5;border-bottom: 0;">
      <el-table-column label="名称" prop="name" width="280"></el-table-column>
      <el-table-column label="类型" prop="designType">
        <template slot-scope="scope">
          <span>{{scope.row.designType == 1 ? '上传文件' : '自定义'}}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="enableFlag">
        <template slot-scope="scope">
          <span>{{scope.row.enableFlag ? '启用' : '禁用'}}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template  slot-scope="scope">
          <jvs-button type="text" @click="oprateTemplate(scope.row)">编辑</jvs-button>
          <jvs-button type="text" v-if="scope.row.designType != 1" @click="designPrintHandle(scope.row, 'design')">设计</jvs-button>
          <jvs-button type="text" @click="disableEnableTemplate(scope.row)">{{scope.row.enableFlag ? '禁用' : '启用'}}</jvs-button>
          <jvs-button type="text" @click="delHandle(scope.row)">删除</jvs-button>
        </template>
      </el-table-column>
    </el-table>
    <div v-if="false" class="list-item-box">
      <div class="item-container">
        <div class="pri-temp-list" v-if="freshBool">
          <p v-for="item in templateList" :key="'pri-template-'+item.id">
            <span>{{item.name}}</span>
            <span v-if="item.designType == 0">
              <el-button size="mini" type="text" @click="designPrintHandle(item, 'design')">设计</el-button>
              <el-button size="mini" type="text" @click="designPrintHandle(item, 'print')">打印</el-button>
              <el-button size="mini" type="text" @click="delHandle(item)">删除</el-button>
            </span>
            <span v-if="item.designType == 1">
              <el-button size="mini" type="text" @click="viewPrintHandle(item)">打印</el-button>
              <el-button size="mini" type="text" @click="delHandle(item)">删除</el-button>
            </span>
          </p>
        </div>
      </div>
    </div>
    <!-- 模板参数 -->
    <el-dialog
      :class="{'hide-header-dialog': dialogType != 'param'}"
      :title="title"
      :fullscreen="dialogType == 'param' ? false : true"
      append-to-body
      :visible.sync="dialogVisible"
      :close-on-click-modal="false"
      :before-close="handleClose">
      <div v-if="dialogVisible" style="height:100%;position:relative;">
        <jvs-form v-if="dialogType == 'param'" :option="templateFormOption" :formData="templateForm" @submit="templateSubmit"></jvs-form>
      </div>
    </el-dialog>
    <el-dialog
      title="导入打印模板"
      :visible.sync="importVisible"
      width="720px"
      append-to-body
      :close-on-click-modal="false"
      :before-close="handleCloseImport">
      <div class="import-data-box">
        <el-upload
          ref="fileUpload"
          class="import-data-upload"
          :action="`/mgr/jvs-design/app/design/${$route.query.jvsAppId}/print/template/${identity}/upload/jvs-public`"
          :headers="{'Authorization': 'Bearer ' + $store.getters.access_token}"
          :on-success="handleSuccess"
          :on-error="upErrHandle"
          :before-upload="beforeUpload"
          :show-file-list="false"
          drag
          multiple>
          <div class="el-upload__text">
            <svg aria-hidden="true" style="width: 24px; height: 24px;margin-bottom: 16px">
              <use xlink:href="#icon-upload"></use>
            </svg>
            <div>点击或者拖动文件到虚线框内上传</div>
            <div style="color: #a2a3a5;font-size: 12px;margin-top: 8px;">仅支持.docx类型的文件</div>
          </div>
        </el-upload>
        <div class="upload-explain">
          <span style="color: #a2a3a5;">上传的文件符合以下规范：</span>
          <ul>
            <li style="list-style: disc">文件大小不超过<span>5M</span></li>
            <li style="list-style: disc">仅支持<span>*.docx</span>文件</li>
          </ul>
          <div style="text-indent: 12px;"><span style="color: #409EFF;cursor: pointer;font-weight:normal;" @click="openFieldInfo">点击查看字段比对信息</span></div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { saveTemplate, getTemplateAll, deleteTemplate } from './api'
export default {
  name: 'print-list-design',
  props: {
    identity: {
      type: String
    },
    dataModelId: {
      type: String
    }
  },
  data() {
    return {
      tableLoading: false,
      templateList: [],
      tableOption: {
        viewBtn: false,
        column: [
          {
            label: '名称',
            prop: 'name',
            rules: [
              { required: true, message: '请输入模板名称', trigger: 'blur' },
              // { max: 6, message: '名称不得超过6个字符', trigger: 'blur' }
            ],
          },
          {
            label: '类型',
            prop: 'designType',
            dicData: [
              {label: '自定义', value: 0},
              {label: 'docx', value: 1}
            ]
          },
          {
            label: '状态',
            prop: 'status'
          }
        ]
      },
      dialogType: '',
      title: '',
      dialogVisible: false,
      templateForm: {},
      templateFormOption: {
        cancal: false,
        submitLoading: false,
        column: [
          {
            label: '模板名称',
            prop: 'name',
            rules: [
              { required: true, message: '请输入模板名称', trigger: 'blur' },
              // { max: 6, message: '名称不得超过6个字符', trigger: 'blur' }
            ],
          }
        ]
      },
      freshBool: true,
      importVisible: false
    }
  },
  methods: {
    // 获取该标识下所有的模板
    getList () {
      this.tableLoading = true
      getTemplateAll(this.$route.query.jvsAppId, this.identity).then(res => {
        if(res.data && res.data.code == 0) {
          // console.log(res.data.data)
          this.$set(this, 'templateList', res.data.data)
          this.templateList = res.data.data
          this.tableLoading = false
          this.$forceUpdate()
        }
      })
    },
    // 新增  编辑  模板
    oprateTemplate (item) {
      this.dialogType = 'param'
      if(item){
        this.title = '编辑'
        this.templateForm = JSON.parse(JSON.stringify(item))
      }else{
        this.templateForm = {
          designType: 0,
          enableFlag: false
        }
        this.title = '新增'
      }
      this.dialogVisible = true
    },
    // 模板设置提交
    templateSubmit (form) {
      this.templateFormOption.submitLoading = true
      saveTemplate(this.$route.query.jvsAppId, {...this.templateForm, designId: this.identity}).then(res => {
        if(res.data && res.data.code == 0) {
          // this.$message.success(this.title + '模板成功')
          this.$notify({
            title: '提示',
            message: this.title + '模板成功',
            position: 'bottom-right',
            type: 'success'
          });
          this.handleClose()
          this.templateFormOption.submitLoading = false
          this.getList()
        }else{
          this.templateFormOption.submitLoading = false
        }
      }).catch(e => {
        this.templateFormOption.submitLoading = false
      })
    },
    // 关闭弹框
    handleClose () {
      this.dialogVisible = false
      this.templateForm = {}
    },
    // 启用  禁用
    disableEnableTemplate (row) {
      let str = '启用'
      let temp = JSON.parse(JSON.stringify(row))
      if(temp.enableFlag) {
        str = '禁用'
        temp.enableFlag = false
      }else{
        str = '启用'
        temp.enableFlag = true
      }
      this.title = str
      this.templateForm = temp
      this.templateSubmit(temp)
    },
    // 删除
    delHandle (item) {
      this.$confirm('确定删除此模板？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteTemplate(this.$route.query.jvsAppId, item.id).then(res => {
          if(res.data && res.data.code == 0) {
            // this.$message.success('删除成功')
            this.$notify({
              title: '提示',
              message: '删除成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.getList()
          }
        })
      }).catch(e => {})
    },
    // 上传成功
    handleSuccess(res, file, fileList) {
      if(res.code == 0) {
        this.getList()
        this.$refs.fileUpload.clearFiles()
        this.handleCloseImport()
      }
    },
    upErrHandle (err, file, fileList) {
      this.$refs.fileUpload.clearFiles()
      // this.$message.error(err)
      this.$notify({
        title: '提示',
        message: err,
        position: 'bottom-right',
        type: 'error'
      });
    },
    beforeUpload (file) {
      if(file.name) {
        let idx = file.name.lastIndexOf('.')
        let suffix = file.name.substring(idx+1)
        if(suffix.toLowerCase() != 'docx') {
          // this.$message.error('文件只能是docx类型')
          this.$notify({
            title: '提示',
            message: '文件只能是docx类型',
            position: 'bottom-right',
            type: 'error'
          });
          this.$refs.fileUpload.clearFiles()
          return false
        }
      }
      if(file.size > 1024*1024*5) {
        // this.$message.error('文件大小不能超过5M')
        this.$notify({
          title: '提示',
          message: '文件大小不能超过5M',
          position: 'bottom-right',
          type: 'error'
        });
        this.$refs.fileUpload.clearFiles()
        return false
      }
    },
    // 设计
    designPrintHandle (row, type) {
      this.$openUrl(`/jvs-print-ui/#/print/design?id=${row.id}&name=${row.name}&designId=${row.designId}&dataModelId=${this.dataModelId}&jvsAppId=${this.$route.query.jvsAppId}`, '_blank')
    },
    handleCloseImport () {
      this.importVisible = false
    },
    openFieldInfo () {
      this.$openUrl(`/jvs-print-ui/#/print/fields?designId=${this.identity}&dataModelId=${this.dataModelId}&jvsAppId=${this.$route.query.jvsAppId}`, '_blank')
    }
  },
  created () {
    if(this.identity) {
      this.getList()
    }
  }
}
</script>
<style lang="scss" scoped>
.import-data-box{
  .import-data-upload{
    text-align: center;
    /deep/.el-upload-dragger{
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #f7f7f7;
      width: 600px;
    }
  }
  .upload-explain{
    margin-top: 16px;
    font-size: 12px;
    ul{
      padding: 0 16px;
      margin: 8px 0;
      li{
        line-height: 20px;
        span{
          font-weight: bold;
        }
      }
    }
  }
}
</style>
