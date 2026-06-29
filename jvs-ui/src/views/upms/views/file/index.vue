<template>
  <div class="file-manage" v-if="show">
    <jvs-table
      pageheadertitle='文件管理'
      :option="option"
      :data="tableData"
      :page="page"
      :loading="tableLoading"
      :selectable="true"
      :isClearSelect="isClearSelect"
      @on-load="getList"
      @search-change="searchChange"
      @delRow="deleteRow"
      @selection-change="selectChange"
    >
      <template slot="menuLeft">
        <jvs-button type="primary" size="mini" icon="el-icon-plus" permisionFlag="jvs_file_upload" @click="dialogVisible = true">{{$langt('file.upload')}}</jvs-button>
        <jvs-button type="primary" size="mini" permisionFlag="jvs_file_delete" :disabled="!selectedList || selectedList.length == 0" @click="multipleDelete">{{$langt('file.del')}}</jvs-button>
      </template>
      <template slot="menuRight">
        <el-row class="table-show-right-tool">
          <!-- 表格工具 -->
          <p class="search-p">
            <el-input :placeholder="$langt('table.keyword')" prefix-icon="el-icon-search" size="mini" v-model="queryParams.fileName" @keyup.enter.native="searchChange(Object.assign(queryParams, {current: 1}))" @blur="searchChange(Object.assign(queryParams, {current: 1}));" clearable @clear="searchChange(Object.assign(queryParams, {current: 1}));"></el-input>
          </p>
          <p @click="searchChange(Object.assign(queryParams, {current: 1}))" style="font-size: 14px;">
            <i class="el-icon-refresh" style="cursor:pointer;"></i>
            <span>{{$langt('table.fresh')}}</span>
          </p>
        </el-row>
      </template>
      <template slot="tableTop">
        <div class="list-use-body-top">
          <div class="left">
            <div :class="{'left-item': true, 'active': !queryParams.label}" @click="retrievalSearch('')">{{$langt('common.all')}}</div>
            <div :class="{'left-item': true, 'active': item == queryParams.label}" v-for="item in retrievalOption" :key="'retrieval-item-'+item" @click="retrievalSearch(item)">{{item}}</div>
          </div>
          <div class="right"></div>
        </div>
      </template>
      <template slot="menu" slot-scope="scope">
        <span style="margin-right: 10px;">
          <!-- <jvs-button type="text" size="mini" v-if="['png', 'jpg', 'jpeg', 'pdf'].indexOf(scope.row.fileType) > -1" @click="viewItem(scope.row, 'preview')">预览</jvs-button> -->
          <jvs-button v-if="false" type="text" size="mini" @click="viewItem(scope.row, 'preview')">{{$langt('table.preview')}}</jvs-button>
          <!-- <jvs-button type="text" size="mini" @click="viewItem(scope.row, 'download')">下载</jvs-button> -->
          <jvs-button type="text" size="mini" permisionFlag="jvs_file_link" @click="viewItem(scope.row, 'copy')">{{$langt('file.copy')}}</jvs-button>
        </span>
      </template>
      <template slot="fileType" slot-scope="scope">
        <img style="height: 30px;" :src="scope.row.thumbnail" alt=""/>
      </template>
    </jvs-table>
    <el-dialog
      :title="$langt('file.fileUp')"
      v-if="dialogVisible"
      :visible.sync="dialogVisible"
      append-to-body
      :close-on-click-modal="false"
      :before-close="handleClose">
      <jvs-form ref="ruleForm" :option="uploadOption" :formData="uploadForm" @formChange="formChange" @submit="handleSubmit"></jvs-form>
      <el-upload
        class="upload-demo"
        style="text-align:center;"
        drag
        ref="uploadBtn"
        :action="`/mgr/jvs-auth/upload/${paramData.bucketName}`"
        :data="paramData"
        :headers="header"
        :file-list="fileList"
        :auto-upload="false"
        :before-upload="beforeUpload"
        :on-change="onChange"
        :on-success="successHandle"
        :on-error="errHandle"
      >
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">{{$langt('file.fileUpTip1')}}</div>
        <div class="el-upload__tip" slot="tip" v-if="limitShow" style="color: #f56c6c;font-size: 12px;">{{$langt('file.fileUpTip2')}}</div>
      </el-upload>
      <el-row style="display:flex;justify-content:center;align-items:center;margin-top:10px;">
        <jvs-button size="mini" type="primary" :disabled="fileList.length == 0" @click="submitFile">{{$langt('form.submit')}}</jvs-button>
        <jvs-button size="mini" @click="handleClose">{{$langt('form.cancel')}}</jvs-button>
      </el-row>
    </el-dialog>
  </div>
</template>
<script>
import {getFiles, preview, download, getbuckets, deleteFile, mulDeleteFile} from './api'
import { tableOption } from './option'
import { getLabelList } from '@/components/api'
import store from "@/store";
import {Base64} from "js-base64";
export default {
  data() {
    return {
      // 上传参数
      paramData: {
        module: '/jvs-ui/file/'
      },
      tableData: [], // 表格数据
      tableLoading: false,
      option: tableOption, // 表格配置
      queryParams: {},
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
      },
      uploadForm: {},
      uploadOption: { // 对应表单设置
        labelWidth: '80px',
        submitBtn: false,
        emptyBtn: false,
        btnHide: true,
        cancal: false,
        column: [
          {
            label: '桶名',
            prop: 'bucketName',
            type: 'select',
            dicData: [],
            search: true,
            filterable: true,
            allowcreate: true,
            rules: [
              { required: true, message: "请选择桶名", trigger: "blur" },
            ]
          },
          {
            label: '标签',
            prop: 'label',
            type: 'select',
            allowcreate: true,
            filterable: true,
            dicData: [],
          }
        ]
      },
      fileList: [],
      show: false,
      dialogVisible: false,
      limitShow: false,
      currentFile: null,
      header: {
        "Authorization": 'Bearer ' + store.getters.access_token
      },
      retrievalOption: [],
      isClearSelect: -1,
      selectedList: []
    }
  },
  methods: {
    formChange(e) {
      this.paramData.bucketName = e.bucketName
      this.paramData.label = e.label
    },
    //   获取数据
    getList (page) {
      let query={
        current: this.page.currentPage,
        size: this.page.pageSize
      }
      this.tableLoading = true
      let temp = JSON.parse(JSON.stringify(this.queryParams))
      getFiles(Object.assign(query, temp)).then(res => {
        if (res.data.code==0) {
          this.page.total=res.data.data.total
          this.page.currentPage=res.data.data.current
          this.tableData=res.data.data.records
          this.tableLoading = false
        }
      }).catch(e => {
        this.page.total=0
        this.page.currentPage=1
        this.tableData=[]
        this.tableLoading = false
      })
    },
    searchChange (form) {
      this.queryParams = form
      this.getList()
    },
    beforeUpload (file) {
      if(file.size > 20971520) {
        this.limitShow = true
        return false
      }else{
        this.limitShow = false
      }
      this.currentFile = file
    },
    onChange (file, fileList) {
      this.fileList = fileList
    },
    successHandle (res, file, fileList) {
      if(res.code == 0) {
        this.$notify({
          title: this.$langt('common.tip'),
          message: this.$langt('common.uploadSuccess'),
          position: 'bottom-right',
          type: 'success'
        });
        this.getList()
        this.handleClose()
      }else{
        this.$refs.uploadBtn.clearFiles()
        this.$notify({
          title: this.$langt('common.tip'),
          message: res.msg,
          position: 'bottom-right',
          type: 'error'
        });
      }
    },
    errHandle (err, file, fileList) {
      this.$refs.uploadBtn.clearFiles()
      this.$notify({
        title: this.$langt('common.tip'),
        message: err,
        position: 'bottom-right',
        type: 'error'
      });
    },
    viewItem (row, type) {
      if(['preview', 'copy'].indexOf(type) > -1) {
        preview(row.bucketName, {fileName: row.filePath}).then(res => {
          if(res.data.code == 0) {
            if(type == 'preview') {
              // this.$openUrl(res.data.data, "_blank")
              let url = Base64.encode(res.data.data)
              this.$openUrl(`http://file.preview.jvs.bctools.cn/onlinePreview?forceUpdatedCache=true&officePreviewType=pdf&url=${url}`, "_blank")
            }
            if(type == 'copy') {
              let input = document.createElement("input");
              input.value = res.data.data;
              document.body.appendChild(input);
              input.select();
              document.execCommand("Copy");
              document.body.removeChild(input);
              // console.log(document.execCommand("Copy"))
              if (document.execCommand("Copy") == true) {
                this.$notify({
                  title: this.$langt('common.tip'),
                  message: this.$langt('common.copySuccess'),
                  position: 'bottom-right',
                  type: 'success'
                });
              }
            }
          }
        })
      }
      if(type == 'download') {
        this.$openUrl(`/mgr/jvs-auth/bytes/${row.bucketName}?fileName=${row.filePath}`, "_self")
      }
    },
    onCopy (e) {
      console.log(e.text)
    },
    onError (e) {
      console.log(e)
    },
    async getBuckConst () {
      await getbuckets().then(res => {
        if(res.data.code == 0) {
          let temp = []
          for(let i in res.data.data) {
            temp.push({
              label: res.data.data[i],
              value: res.data.data[i]
            })
          }
          this.option.column.filter(item => {
            if(item.prop == "bucketName") {
              item.dicData = temp
            }
          })
          this.uploadOption.column.filter(item => {
            if(item.prop == "bucketName") {
              item.dicData = temp
            }
          })
        }
      })
    },
    handleClose () {
      this.limitShow = false
      this.$refs.uploadBtn.clearFiles()
      this.dialogVisible = false
      this.currentFile = null
      this.uploadForm = {}
    },
    submitFile () {
      this.$refs['ruleForm'].submitForm('ruleForm');
    },
    handleSubmit() {
      this.$refs.uploadBtn.submit()
    },
    retrievalSearch (item) {
      if(item != this.queryParams.label) {
        this.$set(this.queryParams, 'label', item)
        this.getList()
      }
    },
    deleteRow (data) {
      if(data.id) {
        deleteFile({id: data.id}).then(res => {
          if(res.data && res.data.code == 0) {
            this.$notify({
              title: this.$langt('common.tip'),
              message: this.$langt('common.delSuccess'),
              position: 'bottom-right',
              type: 'success'
            })
            this.getList()
          }
        })
      }
    },
    selectChange (data) {
      this.selectedList = data
    },
    multipleDelete () {
      let ids = []
      this.selectedList.filter(sit => {
        ids.push(sit.id)
      })
      if(ids && ids.length > 0) {
        mulDeleteFile({ids: ids}).then(res => {
          if(res.data && res.data.code == 0) {
            this.$notify({
              title: this.$langt('common.tip'),
              message: this.$langt('common.delSuccess'),
              position: 'bottom-right',
              type: 'success'
            })
            this.isClearSelect = Math.random()
            this.getList()
          }
        })
      }
    },
    getLabelList () {
      getLabelList().then(res => {
        if (res.data && res.data.code === 0 && res.data.data) {
          this.retrievalOption = [...res.data.data]
          let tp = []
          res.data.data.filter(rit => {
            tp.push({label: rit, value: rit})
          })
          this.uploadOption.column.filter(fit => {
            if(fit.prop == 'label') {
              fit.dicData = tp
            }
          })
        }
      })
    }
  },
  async created () {
    this.option.column.filter(col => {
      if(col.label) {
        col.label = this.$langt(`file.column.${col.prop}.label`)
      }
    })
    this.uploadOption.column.filter(col => {
      if(col.label) {
        col.label = this.$langt(`file.column.${col.prop}.label`)
      }
      if(col.rules && col.rules.length > 0) {
        col.rules[0].message = this.$langt(`file.column.${col.prop}.placeholder`)
      }
    })
    this.getLabelList()
    await this.getBuckConst()
    this.show = true
  }
}
</script>
