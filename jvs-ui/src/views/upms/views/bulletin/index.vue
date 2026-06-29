<template>
  <div class="bulletin-page">
    <jvs-table
      :pageheadertitle='pageheadertitle'
      :option="option"
      :data="tableData"
      :loading="tableLoading"
      :page="page"
      @on-load="getList"
      @search-change="searchChange"
      @addRow="addRowHandle"
      @editRow="editRowHandle"
      @delRow="delRowHandle"
    >
      <template slot="menuLeft" slot-scope="scope">
        <jvs-button size="mini" type="primary" icon="el-icon-plus" permisionFlag="jvs_bulletin_add" @click="addEditHandle('add', null)">{{$langt('bulletin.addMess')}}</jvs-button>
        <jvs-button size="mini" type="primary" icon="el-icon-plus" permisionFlag="jvs_bulletin_add_mini" @click="addEditHandle('addWx', null)">{{$langt('bulletin.addWx')}}</jvs-button>
      </template>
      <template slot="menuRight">
        <el-row class="table-show-right-tool">
          <!-- 表格工具 -->
          <p class="search-p">
            <el-input :placeholder="$langt('table.keyword')" prefix-icon="el-icon-search" size="mini" v-model="queryParams.keywords" @keyup.enter.native="searchChange(Object.assign(queryParams, {current: 1}))" @blur="searchChange(Object.assign(queryParams, {current: 1}));" clearable @clear="searchChange(Object.assign(queryParams, {current: 1}));"></el-input>
          </p>
          <p @click="searchChange(Object.assign(queryParams, {current: 1}))" style="font-size: 14px;">
            <i class="el-icon-refresh-right" style="cursor:pointer;"></i>
            <span>{{$langt('table.fresh')}}</span>
          </p>
        </el-row>
      </template>
      <template slot="publish" slot-scope="scope">
        <el-tag size="mini" :type="scope.row.publish ? '' : 'info'">{{getIsDeployText(scope.row.publish)}}</el-tag>
      </template>
      <template slot="menu" slot-scope="scope">
        <jvs-button size="mini" type="text" permisionFlag="jvs_bulletin_edit" @click="addEditHandle('edit', scope.row)">{{$langt('table.edit')}}</jvs-button>
        <jvs-button size="mini" type="text" permisionFlag="jvs_bulletin_undeploy" v-if="scope.row.publish" @click="unload(scope.row)">{{$langt('table.unload')}}</jvs-button>
        <jvs-button size="mini" type="text"  v-if="!scope.row.publish && $permissionMatch('jvs_bulletin_deploy')" @click="deploy(scope.row)">{{$langt('table.deploy')}}</jvs-button>
      </template>
    </jvs-table>
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      top="6vh"
      width="60%"
      fullscreen
      :close-on-click-modal="false"
      :before-close="handleClose">
      <div v-if="dialogVisible" style="padding: 24px;">
        <jvs-form :option="formOption" :formData="formData" @submit="handleSubmit" @cancalClick="handleClose">
<!--          <template slot="typeForm">-->
<!--            <el-radio v-model="formData.type" label="PC">PC</el-radio>-->
<!--            <el-radio v-model="formData.type" label="MOBILE">MOBILE</el-radio>-->
<!--          </template>-->
          <template slot="contentForm">
            <div v-if="formData.contentType === 'TEXT'" id="appEditor" style="z-index: 1000;width:100%;"></div>
            <div v-else>
              <div v-if="formData.content" class="select-image-show">
                <img :src="formData.content" alt="">
                <i class="el-icon-delete delete-select-image-tool" @click="delImgSelect('content')"></i>
              </div>
              <jvs-button v-else @click="chooseImage('content')">{{$langt('bulletin.selectImg')}}</jvs-button>
            </div>
<!--            <el-upload-->
<!--              v-else-->
<!--              class="avatar-uploader"-->
<!--              :action="`/mgr/jvs-auth/upload/${paramData.bucketName}`"-->
<!--              list-type="picture-card"-->
<!--              :data="paramData"-->
<!--              :headers="header"-->
<!--              :on-success="handleImgSuccess"-->
<!--              :on-remove="handleRemove"-->
<!--              :show-file-list="false"-->
<!--              :before-upload="beforeUpload">-->
<!--              <img v-if="formData.contentType === 'IMG' && formData.content" :src="formData.content" style="width: 100%;height: 100%" alt="">-->
<!--              <i v-else class="el-icon-plus"></i>-->
<!--            </el-upload>-->
          </template>
          <template slot="bannerForm">
            <div v-if="formData.banner" class="select-image-show">
              <img :src="formData.banner" alt="">
              <i class="el-icon-delete delete-select-image-tool" @click="delImgSelect('banner')"></i>
            </div>
            <jvs-button v-else @click="chooseImage('banner')">{{$langt('bulletin.selectImg')}}</jvs-button>
          </template>
          <template slot="appKeysForm">
            <el-checkbox-group v-model="formData.appKeys">
              <el-checkbox v-for="(item, key) in appList" :key="key" :label="item.appKey">
                {{ item.name }}
              </el-checkbox>
            </el-checkbox-group>
          </template>
          <template slot="contentTypeForm">
            <el-radio v-model="formData.contentType" label="IMG" @change="handleRadioChange">{{$langt('bulletin.img')}}</el-radio>
            <el-radio v-model="formData.contentType" label="TEXT" @change="handleRadioChange">{{$langt('bulletin.text')}}</el-radio>
          </template>
        </jvs-form>
      </div>
    </el-dialog>
    <imageSelect ref="imgSelect"
      :title="$langt('bulletin.selectImg')"
      :dialogVisible="chooseAble"
      :paramInfo="{'module': 'application'}"
      @handleConfirm="handleConfirm"
      @handleClose="chooseAble = false;"
    ></imageSelect>
  </div>
</template>
<script>
import imageSelect from '@/components/basic-assembly/ImageSelect'
import { tableOption } from './option'
import {pageList, add, edit, del, deploy, unload, getAppList, getDetail} from './api'
import E from "wangeditor";
import store from "@/store";
import {client_id} from "@/const/const";
export default {
  components: {imageSelect},
  props: {
    propData: {
      type: Object
    }
  },
  data () {
    return {
      header: {
        "Authorization": 'Bearer ' + store.getters.access_token
      },
      paramData: {
        bucketName: 'jvs-public',
        module: `${client_id}/bulletin/${store.getters.tenantId}`,
        label: ''
      }, // 上传参数
      dialogVisible: false,
      submitType: 'add',
      dialogTitle: '',
      tableLoading: false, // loading显示
      tableData: [], // 列表数据
      pageheadertitle: '公告管理', // fixed me  表名
      option: tableOption,
      appList: [],
      formData: {
        appKeys:[],
        contentType: 'IMG',
        type: 'PC'
      },
      formOption: {
        formAlign: 'top',
        emptyBtn: false,
        submitBtnText: this.$langt('common.confirm'),
        submitLoading: false,
        column: [
          // {
          //   label: "终端类型",
          //   prop: "type",
          //   formSlot: true,
          //   rules: [{ required: true, message: '请选择公告类型', trigger: 'change' }],
          // },
          {
            label: "标题",
            prop: "title",
            display: true,
            rules: [{ required: true, message: '请输入标题', trigger: 'change' }],
          },
          {
            label: "生效时间",
            prop: "startTime",
            datetype: "datetime",
            type: "datePicker",
            format: "yyyy-MM-dd hh:mm:ss",
            valueFormat: "yyyy-MM-dd hh:mm:ss",
            rules: [{ required: true, message: '请选择生效时间', trigger: 'change' }],
          },
          {
            label: "失效时间",
            prop: "endTime",
            datetype: "datetime",
            type: "datePicker",
            format: "yyyy-MM-dd hh:mm:ss",
            valueFormat: "yyyy-MM-dd hh:mm:ss",
            rules: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
          },
          {
            label: "终端选择",
            tableLabel: '终端',
            prop: "appKeys",
            formSlot: true,
            rules: [{ required: true, message: '请选择终端', trigger: 'change' }],
          },
          {
            label: "公告类型",
            prop: "contentType",
            formSlot: true,
            rules: [{ required: true, message: '请选择公告类型', trigger: 'change' }],
          },
          {
            label: "指定人员",
            prop: "userIds",
            type: 'user',
            multiple: true
          },
          {
            label: "Banner",
            prop: "banner",
            formSlot: true,
            rules: [{ required: true, message: 'Banner不能为空', trigger: 'change' }],
          },
          {
            label: "内容",
            prop: "content",
            formSlot: true,
            rules: [{ required: true, message: '内容不能为空', trigger: 'change' }],
          },
        ]
      },
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
      },
      queryParams: {}, // 查询条件
      editor: null,
      previewEditor: null,
      chooseAble: false,
      imgProp: '',
    }
  },
  watch: {
    formData: {
      handler(newVal, oldVal) {
        // this.formOption.column[1].display = newVal.type === 'PC';
      },
      deep: true
    }
  },
  created () {
    this.option.column.filter(col => {
      if(col.label) {
        if(col.prop == 'appKeys') {
          col.label = this.$langt(`bulletin.column.${col.prop}.tableLabel`)
        }else{
          col.label = this.$langt(`bulletin.column.${col.prop}.label`)
        }
      }
      if(col.rules && col.rules.length > 0) {
        col.rules[0].message = this.$langt(`bulletin.column.${col.prop}.placeholder`)
      }
    })
    this.formOption.column.filter(col => {
      if(col.label) {
        col.label = this.$langt(`bulletin.column.${col.prop}.label`)
      }
      if(col.rules && col.rules.length > 0) {
        col.rules[0].message = this.$langt(`bulletin.column.${col.prop}.placeholder`)
      }
    })
    this.getAppList()
  },
  methods: {
    // 确认图片
    handleConfirm (value) {
      this.chooseAble = false;
      if(value && value.fileLink) {
        this.$set(this.formData, this.imgProp, value.fileLink)
      }
    },
    // 删除已选图片
    delImgSelect (prop, src) {
      if (prop === 'content' || prop === 'banner') {
        this.$set(this.formData, prop, '')
      }
    },
    // 选择图片
    chooseImage (prop) {
      this.imgProp = prop
      this.chooseAble = true
      this.$refs.imgSelect.init()
    },
    //
    handleRadioChange(e) {
      this.$set(this.formData, 'content', '')
      if (e === 'TEXT') {
        this.editor = null
        this.$nextTick(() => {
          this.initEditor()
        })
      } else {
      }
    },
    // 图片上传成功
    handleImgSuccess(res, file, fileList) {
      this.formData.content = res.data.fileLink
      this.$forceUpdate()
    },
    // 删除图片操作
    handleRemove(file, fileList) {
      this.formData.content = ''
    },
    // banner上传成功
    handleBannerSuccess(res, file, fileList) {
      this.formData.banner = res.data.fileLink
      this.$forceUpdate()
    },
    // 删除banner操作
    handleBannerRemove(file, fileList) {
      this.formData.banner = ''
    },
    // 上传图片前的处理
    beforeUpload(file) {
      const isJPG = ['image/jpeg', 'image/png'].indexOf(file.type) > -1
      const isLt10M = file.size / 1024 / 1024 < 10;
      if (!isLt10M) {
        this.$notify({
          title: this.$langt('common.tip'),
          message: this.$langt('bulletin.isLt10M'),
          position: 'bottom-right',
          type: 'error'
        });
      }
      if (!isJPG) {
        this.$notify({
          title: this.$langt('common.tip'),
          message: this.$langt('bulletin.isImg'),
          position: 'bottom-right',
          type: 'error'
        });
      }
      return isLt10M && isJPG;
    },
    // 获取应用列表
    getAppList() {
      getAppList().then(res => {
        if(res.data && res.data.code == 0 && res.data.data) {
          // this.appList = [...res.data.data]
          this.appList = res.data.data.filter(item => {
            return item.appKey !== 'weixinminiapp'
          })
          this.option.column.filter(col => {
            if(col.prop == 'appKeys') {
              col.dicData = this.appList
            }
          })
        }
      })
    },
    // 初始化预览富文本
    initPreviewEditor() {
      let _this = this
      if(_this.previewEditor) {
        _this.previewEditor.destroy()
      }
      _this.previewEditor = new E('#appDetail')
      _this.previewEditor.config.menus = []
      _this.previewEditor.config.zIndex = 80
      _this.previewEditor.config.showFullScreen = false
      _this.previewEditor.create()
      _this.previewEditor.txt.html(_this.formData.content)
      _this.previewEditor.disable()
    },
    // 初始化富文本
    initEditor () {
      let _this = this
      if(_this.editor) {
        _this.editor.destroy()
      }
      _this.editor = new E('#appEditor')
      // _this.editor.config.height = $('.set-detail-box').height()
      _this.editor.config.height = 500
      _this.editor.config.uploadImgShowBase64 = true
      _this.editor.config.menus = [
        'head',
        'bold',
        'fontSize',
        'fontName',
        'italic',
        'underline',
        'strikeThrough',
        'indent',
        'lineHeight',
        'foreColor',
        'backColor',
        'link',
        'list',
        'justify',
        'quote',
        'emoticon',
        'image',
        'table',
        'code',
        'splitLine',
        'undo',
        'redo',
      ]
      _this.editor.config.onchange = function (newHtml) {
        // if(_this.formData.content) {
        //   _this.formData.content = newHtml
        // }
        _this.formData.content = newHtml
      }
      _this.editor.config.uploadImgServer = '/mgr/jvs-auth/upload/jvs-public'
      _this.editor.config.uploadImgHeaders = {
        Authorization: ('Bearer '+this.$store.getters.access_token)
      }
      _this.editor.config.uploadImgParams = {
        module: '/jvs-ui/bulletin/'
      }
      _this.editor.config.uploadFileName = 'file'
      _this.editor.config.uploadImgHooks = {
        // 图片上传并返回了结果，图片插入已成功
        success: function(xhr) {
          console.log('success', xhr)
        },
        // 图片上传并返回了结果，但图片插入时出错了
        fail: function(xhr, editor, resData) {
          console.log('fail', resData)
        },
        // 上传图片出错，一般为 http 请求的错误
        error: function(xhr, editor, resData) {
          console.log('error', xhr, resData)
        },
        // 图片上传并返回了结果，想要自己把图片插入到编辑器中
        // 例如服务器端返回的不是 { errno: 0, data: [...] } 这种格式，可使用 customInsert
        customInsert: function(insertImgFn, result) {
          // insertImgFn 可把图片插入到编辑器，传入图片 src ，执行函数即可
          if(result.code == 0 && result.data && result.data.fileLink) {
            let url = result.data.fileLink.indexOf('?') ? result.data.fileLink.split('?')[0] : result.data.fileLink
            insertImgFn(url)
          }
        }
      }
      _this.editor.create()
      _this.editor.txt.html(_this.formData.content)
      this.$forceUpdate()
    },
    // 表单提交
    handleSubmit(form) {
      this.formOption.submitLoading = true
      if (form.contentType !== 'IMG') {
        form.content = this.editor.txt.html()
      }
      if (this.submitType === 'add' || this.submitType === 'addWx') {
        this.addRowHandle(form)
      } else {
        this.editRowHandle(form)
      }
    },
    // 发布
    deploy(row) {
      deploy(row.id).then(res => {
        if(res.data && res.data.code == 0) {
          this.$notify({
            title: this.$langt('common.tip'),
            message: this.$langt('common.deploySuccess'),
            position: 'bottom-right',
            type: 'success'
          });
          this.getList()
        }
      })
    },
    // 卸载
    unload(row) {
      unload(row.id).then(res => {
        if(res.data && res.data.code == 0) {
          this.$notify({
            title: this.$langt('common.tip'),
            message: this.$langt('common.unloadSuccess'),
            position: 'bottom-right',
            type: 'success'
          });
          this.getList()
        }
      })
    },
    // 显示状态
    getIsDeployText(val) {
      return val ? this.$langt('bulletin.deploy') : this.$langt('bulletin.unload')
    },
    // 表格分页查询
    getList (page) {
      let obj={
        size: this.page.pageSize,
        current: this.page.currentPage
      }
      pageList(Object.assign(obj, this.queryParams)).then(res => {
        if (res.data.code==0) {
          this.tableData=res.data.data.records
          this.page.currentPage=res.data.data.current
          this.page.total=res.data.data.total
        }
      })
    },
    // 条件查询
    searchChange (form) {
      this.queryParams=form
      this.getList()
    },
    // 新增/编辑弹窗打开
    async addEditHandle(type, row) {
      if (type === 'add') {
        this.submitType = 'add'
        this.dialogTitle = this.$langt('bulletin.mess')
        this.formData.type = 'PC'
        this.dialogVisible = true
        this.formOption.column.forEach((item, index) => {
          item.display = index !== 5
        })
      }
      if (type === 'addWx') {
        this.submitType = 'addWx'
        this.dialogTitle = this.$langt('bulletin.wx')
        this.formData.type = 'MOBILE'
        this.formData.contentType = 'TEXT'
        this.handleRadioChange('TEXT')
        this.formOption.column.forEach((item, index) => {
          item.display = (index !== 3 && index !== 4)
        })
        this.dialogVisible = true
      }
      if (type === 'edit') {
        // console.log(row)
        // return
        await getDetail(row.id).then(res => {
          if (res.data && res.data.code == 0) {
            if(row.type === 'PC') {
              this.formOption.column.forEach((item, index) => {
                item.display = index !== 5
              })
            } else {
              this.formOption.column.forEach((item, index) => {
                item.display = (index !== 3 && index !== 4)
              })
            }
            this.formData = JSON.parse(JSON.stringify(res.data.data))
            this.dialogVisible = true
          }
        })
        if (this.formData.contentType === 'TEXT') {
          this.$nextTick(() => {
            this.initEditor()
          })
          this.dialogVisible = true
        }
        // this.formData.appKeys = row.appKeys || []
        this.submitType = 'edit'
        this.dialogTitle = this.$langt('table.edit')
      }
    },
    // 关闭 弹窗
    handleClose() {
      this.formData = {
        appKeys:[],
        contentType: 'IMG',
        type: 'PC'
      }
      this.dialogVisible = false
    },
    // 新增
    addRowHandle (form) {
      add(form).then(res => {
        if(res.data.code == 0) {
          this.$notify({
            title: this.$langt('common.tip'),
            message: this.$langt('common.addSuccess'),
            position: 'bottom-right',
            type: 'success'
          });
          this.handleClose()
          this.getList()
        }
        this.formOption.submitLoading = false
      }).catch(err => {
        this.formOption.submitLoading = false
      })
    },
    // 编辑
    editRowHandle (form) {
      edit(form).then(res => {
        if(res.data.code == 0) {
          this.$notify({
            title: this.$langt('common.tip'),
            message: this.$langt('common.editSuccess'),
            position: 'bottom-right',
            type: 'success'
          });
          this.handleClose()
          this.getList()
        }
        this.formOption.submitLoading = false
      }).catch(err => {
        this.formOption.submitLoading = false
      })
    },
    // 删除
    delRowHandle (row) {
      del(row).then(res => {
        if(res.data.code == 0) {
          this.$notify({
            title: this.$langt('common.tip'),
            message: this.$langt('common.delSuccess'),
            position: 'bottom-right',
            type: 'success'
          });
          this.getList()
        }
      })
    }
  }
}
</script>
<style lang="scss">
.select-image-show{
  position: relative;
  img{
    display: block;
    width: 120px;
    height: 120px;
  }
  .delete-select-image-tool{
    position: absolute;
    top: 3px;
    left: 123px;
    cursor: pointer;
    color: #F56C6C;
  }
}
</style>
