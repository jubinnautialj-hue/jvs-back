<template>
  <div class="template-more-page">
    <div class="template-more-header">
      <div class="header-content">
        <div class="title">行业解决方案</div>
        <div class="description">
          <p>由我们为您带来行业解决方案，并能根据您的需求提供灵活、高效的贴身定制服务，让最终交付的方案能够完全贴合您的业务。</p>
          <p>方案完全基于平台进行设计、开发，平台提供了安全、可靠、稳定的运行环境，为您的业务保驾护航。</p>
        </div>
        <div class="bottom-tool">
          <!-- <div v-if="$permissionMatch('jvs_app_template_add') && $functionEnable('应用模板上传')" class="bt-button" @click="handleAddTemp">
            <svg class="icon" aria-hidden="true">
              <use xlink:href="#icon-jvs-xinjian-ffffff"></use>
            </svg>
            <span>添加模板</span>
          </div> -->
          <div v-if="$permissionMatch('jvs_app_template_upload')" v-show="(modeUserInfo && modeUserInfo.userId) ? false : true" class="upload-btn">
            <el-upload
              v-if="$functionEnable('应用模板上传')"
              class="upload-demo"
              :action="`/mgr/jvs-design/base/JvsAppTemplate/fileUpload`"
              :headers="header"
              :on-success="handleTemplateSuccess"
              :before-upload="beforeTempUpload"
              multiple
              :limit="3"
              :show-file-list="false">
              <div class="bt-button">
                <svg class="icon" aria-hidden="true">
                  <use xlink:href="#icon-jvs-daoru-ffffff"></use>
                </svg>
                <span>导入模板</span>
              </div>
            </el-upload>
          </div>
        </div>
      </div>
    </div>
    <div class="template-more-center">
      <div class="left" @click="backAppCneter">
        <svg class="icon" aria-hidden="true">
          <use xlink:href="#icon-jvs-a-zu4954"></use>
        </svg>
        <span>返回上一级</span>
      </div>
    </div>
    <div class="template-more-box">
      <div class="other-template-list">
        <div class="other-template-list-top">
          <div class="template-types">
            <div :class="{'types-item': true, 'active': !currentType}" @click="handleTypeClick(undefined)">全部</div>
            <div v-for="(item, key) in templateTypes" :key="key" :class="{'types-item': true, 'active': currentType == item}" @click="handleTypeClick(item)">{{item}}</div>
          </div>
          <div class="search-box">
            <el-input size="mini" prefix-icon="el-icon-search" placeholder="关键字搜索模板" v-model="queryParams.name" @input="templatePage.current=1;getTemplateList();"></el-input>
          </div>
        </div>
        <div class="template-list">
          <div v-if="templateListLoading" class="loading-back"/>
          <div class="template-item" v-for="(item, key) in templateList" :key="key">
            <div style="cursor: pointer;" @click="handleClick(item)">
              <img style="display: block;height: 150px;width: 100%;object-fit: cover;" :src="item.imgs[0]" alt=""/>
              <div class="item-tag">
                <el-tag size="mini">{{ item.primitive ? '原生应用' : '轻应用' }}</el-tag>
              </div>
              <div class="item-content">
                <div class="item-header">
                  <h5>{{ item.name }}</h5>
                </div>
                <div class="item-description">
                  {{ item.description }}
                </div>
              </div>
            </div>
            <div class="bottom-div">
              <div class="left">
                <div class="bottom-div-left-button" @click="handleClick(item)">模板详情</div>
              </div>
            </div>
            <div class="more-handle">
              <el-popover
                v-if="$permissionMatch('jvs_app_template_edit') || $permissionMatch('jvs_app_template_down') || $permissionMatch('jvs_app_template_delete')"
                placement="right"
                width="50"
                trigger="click">
                <ul class="application-manage-tool-list" v-if="$permissionMatch('jvs_app_template_edit')">
                  <li v-if="$permissionMatch('jvs_app_template_edit')" v-show="(modeUserInfo && modeUserInfo.userId) ? false : true" @click="handleEditTemp(item)">
                    <svg class="icon" aria-hidden="true" style="margin-right: 12px;width: 15px;height: 15px;">
                      <use :xlink:href="'#'+ 'icon-edit-filling'"></use>
                    </svg>编辑模板
                  </li>
                </ul>
                <ul class="application-manage-tool-list" v-if="$permissionMatch('jvs_app_template_down') && $functionEnable('应用模板下载') && !item.primitive">
                  <li v-if="$permissionMatch('jvs_app_template_down')" @click="handleDownloadTemp(item)">
                    <svg class="icon" aria-hidden="true" style="margin-right: 12px;width: 15px;height: 15px;">
                      <use :xlink:href="'#'+ 'icon-decline-filling'"></use>
                    </svg>下载模板
                  </li>
                </ul>
                <ul class="application-manage-tool-list" v-if="$permissionMatch('jvs_app_template_delete')" v-show="(modeUserInfo && modeUserInfo.userId) ? false : true">
                  <li v-if="$permissionMatch('jvs_app_template_delete')" @click="handleDeleteTemp(item)">
                    <svg class="icon" aria-hidden="true" style="margin-right: 12px;width: 15px;height: 15px;fill: #F56C6C;">
                      <use :xlink:href="'#'+ 'icon-delete-filling'"></use>
                    </svg><span style="color: #F56C6C;">删除模板</span>
                  </li>
                </ul>
                <i class="el-icon-more" slot="reference"></i>
              </el-popover>
              <div v-else style="color: #c5c5c5; font-size: 12px">{{ getTempSize(item.size) }}</div>
            </div>
          </div>
        </div>
        <div class="temp-pagination">
          <el-pagination
            background
            layout="prev, pager, next"
            :page-size="templatePage.size"
            :pager-count="5"
            :current-page="templatePage.current"
            @current-change="templatePageChange"
            :total="templatePage.total">
          </el-pagination>
        </div>
      </div>
    </div>
    <el-dialog
      class="template-dialog"
      :title="title"
      append-to-body
      :visible.sync="dialogVisible"
      top="6vh"
      :before-close="handleClose">
      <div style="padding: 0 10px;" v-if="dialogVisible">
        <jvs-form :option="formOption" :formData="formData" @submit="handleSubmit" @cancalClick="handleClose">
          <template slot="logoForm">
            <div v-if="formData.logo" class="select-image-show">
              <img :src="formData.logo" alt="">
              <i v-if="submitType !== 'template'" class="el-icon-delete delete-select-image-tool" @click="delIamgeSelect('logo')"></i>
            </div>
            <jvs-button v-else @click="chooseImage('logo')">选择图片</jvs-button>
          </template>
          <template slot="longTextForm">
            <!-- 富文本 -->
            <div v-if="dialogVisible" class="set-detail-box">
              <div id="tempEditor" style="z-index: 10001;width:100%;"></div>
              <div class="footer-btn"></div>
            </div>
          </template>
          <template slot="imgsForm">
            <div style="display: flex;margin-bottom: 12px;width: 100%;flex-wrap: wrap">
              <div class="select-image-show" style="margin: 0 30px 20px 0;" v-for="(item, key) in imgDesc" :key="key">
                <img :src="item" alt="">
                <i class="el-icon-delete delete-select-image-tool" @click="delIamgeSelect('imgs', item)"></i>
              </div>
            </div>
            <jvs-button v-if="['editTemp', 'template', 'addTemplate'].indexOf(submitType) > -1" @click="chooseImage('imgs')">选择图片</jvs-button>
          </template>
          <template slot="bannerForm">
            <div v-if="['editTemp', 'template'].indexOf(submitType) > -1 && formData.banner" class="select-image-show">
              <img :src="formData.banner" alt="">
              <i class="el-icon-delete delete-select-image-tool" @click="delIamgeSelect('banner')"></i>
            </div>
            <jvs-button v-else @click="chooseImage('banner')">选择图片</jvs-button>
          </template>
          <template slot="freeForm">
            <el-switch v-model="formData.free"></el-switch>
          </template>
          <template slot="priceForm">
            <el-input-number :controls="false" :disabled="formData.free" :min="0" size="mini" v-model="formData.price"></el-input-number>
          </template>
          <template slot="typeForm">
            <el-input size="mini" v-model="formData.type"></el-input>
          </template>
        </jvs-form>
      </div>
    </el-dialog>
    <imageSelect
      ref="logoSelect"
      title="选择图片"
      :dialogVisible="chooseAble"
      :paramInfo="{'module': 'application'}"
      @handleConfirm="handleConfirm"
      @handleClose="chooseAble = false;"
    ></imageSelect>
    <template-detail
      ref="templateDetail"
      :detailVisible="detailVisible"
      :modeUserInfo="modeUserInfo"
      @handleDetailClose="handleDetailClose"
      @handleSelect="handleSelect"
      @openDialog="openDialog"
    />
    <!-- 安装列表 -->
    <install-list :installListVisible="installListVisible" @close="installListVisible=false;"></install-list>
  </div>
</template>

<script>
import {
  add, addTemplate,
  delTemplate,
  downloadTemplate, edit, editTemplate,
  getTemplateDetail,
  getTemplateType, publishToTemplate,
  templateList
} from "@/components/template/api";
import store from "@/store";
import TemplateDetail from "./templateDetail";
import imageSelect from "@/components/basic-assembly/ImageSelect";
import E from "wangeditor";
import installList from "./installList"
import {getStore} from "@/util/store";
export default {
  components: {
    TemplateDetail,
    imageSelect,
    installList
  },
  name: "templateMore",
  props: {
    jvsDesign: {
      type: Object
    }
  },
  data () {
    return {
      editor: null,
      queryParams: {},
      submitType: 'create',
      imgDesc: [],
      chooseAble: false,
      title: '', // 弹框标题
      dialogVisible: false,
      formOption: {
        formAlign: 'top',
        emptyBtn: false,
        submitBtnText: '确定',
        column: [
          {
            label: '名称',
            prop: 'name',
            maxlength: 30,
            rules: [
              { required: true, message: '请输入名称', trigger: 'blur' }
            ],
          },
          // {
          //   label: "分类",
          //   prop: "type",
          //   type: "select",
          //   dicUrl: '/mgr/jvs-auth//api/dict/type/jvsapp',
          //   dicData: [],
          //   rules: [
          //     { required: true, message: '请选择分类', trigger: 'blur' }
          //   ],
          // },
          {
            label: 'logo（建议64 × 64）',
            prop: 'logo',
            formSlot: true
          },
          {
            label: '描述',
            type: 'textarea',
            prop: 'description'
          },
          {
            label: '说明',
            prop: 'longText',
            formSlot: true,
          },
          {
            label: '图片说明',
            prop: 'imgs',
            formSlot: true,
          },
          {
            label: 'banner',
            prop: 'banner',
            formSlot: true
          },
          {
            label: '是否免费',
            prop: 'free',
            display: false,
            formSlot: true,
          },
          {
            label: '价格（元）',
            prop: 'price',
            display: false,
            formSlot: true,
          },
          {
            label: '类型',
            prop: 'type',
            display: false,
            formSlot: true,
          }
        ]
      },
      formData: {},
      currentType: undefined, // 当前选中类型
      templateTypes: [], // 模板类型
      header: {
        "Authorization": 'Bearer ' + store.getters.access_token
      },
      templatePage: {
        total: 0,
        size: 10,
        current: 1
      },
      templateList: [],
      templateListLoading: false,
      detailVisible: false,
      installListVisible: false,
      modeUserInfo: null
    }
  },
  async created () {
    if(this.jvsDesign && this.jvsDesign.JVS_DESIGN_MGR && this.$permissionMatch('jvs_app')) {
      this.modeUserInfo = getStore({ name: 'modeUserInfo' })
    }
    this.dataLoading = true
    await this.getTemplateType()
    await this.getTemplateList()
  },
  methods: {
    // 初始化富文本
    initEditor () {
      let _this = this
      if(_this.editor) {
        _this.editor.destroy()
      }
      _this.editor = new E('#tempEditor')
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
        _this.formData.longText = newHtml
      }
      _this.editor.config.uploadImgServer = '/mgr/jvs-auth/upload/jvs-public'
      _this.editor.config.uploadImgParams = {
        module: '/jvs-ui/templateCenter/'
      }
      _this.editor.config.uploadImgHeaders = {
        Authorization: ('Bearer '+this.$store.getters.access_token)
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
      _this.editor.txt.html(_this.formData.longText)
      this.$forceUpdate()
    },
    // 选择图片
    chooseImage (prop) {
      this.imgProp = prop
      this.chooseAble = true
      this.$refs.logoSelect.init()
    },
    // 确认图片
    handleConfirm (value) {
      this.chooseAble = false;
      if(value && value.fileLink) {
        if (this.imgProp === 'logo') {
          this.$set(this.formData, this.imgProp, value.fileLink)
        }
        if (this.imgProp === 'imgs') {
          this.imgDesc.push(value.fileLink)
        }
        if (this.imgProp === 'banner') {
          this.$set(this.formData, this.imgProp, value.fileLink)
        }
      }
    },
    // 删除已选图片
    delIamgeSelect (prop, src) {
      if (prop === 'logo' || prop === 'banner') {
        this.$set(this.formData, prop, '')
      }
      if (prop === 'imgs') {
        if (this.imgDesc.indexOf(src) > -1) {
          this.imgDesc.splice(this.imgDesc.indexOf(src), 1)
        }
      }
    },
    // 提交应用表单
    handleSubmit (form) {
      console.log(form)
      form.imgs = [...this.imgDesc]
      if (this.submitType === 'create') {
        add(form).then(res => {
          this.$notify({
            title: '提示',
            message: '新增成功',
            position: 'bottom-right',
            type: 'success'
          });
          this.dialogVisible = false
          this.getMyApplication()
        })
      }
      if (this.submitType === 'edit') {
        edit(form).then(res => {
          this.$notify({
            title: '提示',
            message: '编辑成功',
            position: 'bottom-right',
            type: 'success'
          });
          this.dialogVisible = false
          this.getMyApplication()
        })
      }
      if (this.submitType === 'template') {
        if (!(form.imgs && form.imgs.length > 0)) {
          this.$notify({
            title: '提示',
            message: '请上传图片说明',
            position: 'bottom-right',
            type: 'error'
          });
          return
        }
        this.$confirm('是否确认发布？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          publishToTemplate(form).then(res => {
            if (res.data && res.data.code == 0) {
              this.$notify({
                title: '提示',
                message: '发布成功',
                position: 'bottom-right',
                type: 'success'
              });
              this.dialogVisible = false
              this.getTemplateList()
            }
          })
        }).catch(e => {})
      }
      if (this.submitType === 'addTemplate') {
        addTemplate(form).then(res => {
          if (res.data && res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: '新增成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.dialogVisible = false
            this.getTemplateList()
          }
        })
      }
      if (this.submitType === 'editTemp') {
        editTemplate(form).then(res => {
          if (res.data && res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: '编辑成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.dialogVisible = false
            this.getTemplateList()
          }
        })
      }
    },
    handleClose () {
      this.dialogVisible = false
      this.formOption.column.forEach((item, index) => {
        item.disabled = false
      })
      this.formData = {}
      this.fileList = []
    },
    // 获取模板应用大小
    getTempSize(size) {
      return (size / 1024).toFixed(2) + 'K'
    },
    // 删除模板
    handleDeleteTemp(obj) {
      this.$confirm('此操作将永久删除此数据, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        delTemplate(obj.id).then(res => {
          if(res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: '删除成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.getTemplateList()
          }
        })
      }).catch(e => {})
    },
    // 下载模板
    handleDownloadTemp(obj) {
      this.$openUrl(`/mgr/jvs-design/base/JvsAppTemplate/download/${obj.id}`, '_blank', null, 'url')
    },
    // 下载文件
    downloadFile(filename, content) {
      var elink = document.createElement('a')
      if(filename) {
        elink.download = filename
      }
      elink.style.display = 'none'

      var blob = new Blob([content],{})
      elink.href = URL.createObjectURL(blob)
      document.body.appendChild(elink)
      elink.click()
      document.body.removeChild(elink)
    },
    // 编辑模板
    handleEditTemp(obj) {
      getTemplateDetail(obj.id).then(res => {
        this.formData = JSON.parse(JSON.stringify(res.data.data))
        this.imgDesc = [...res.data.data.imgs]
        this.formOption.column.forEach((item, index) => {
          if (item.prop === 'imgs') {
            item.display = true
          } else {
            item.disabled = false
          }
          if (index > 3) {
            item.display = true
          }
        })
        this.title = '编辑应用模板'
        this.submitType = 'editTemp'
        this.dialogVisible = true
        this.$nextTick(() => {
          this.initEditor()
        })
      })
    },
    // 模板类型点击事件
    handleTypeClick (type) {
      this.templatePage.current = 1
      this.currentType = type
      this.getTemplateList()
    },
    // 关闭模板详情弹窗
    handleDetailClose() {
      this.detailVisible = false
    },
    // 选择模板
    handleSelect(detailInfo, callback) {
      const params = {
        templateId: detailInfo.id,
        online: detailInfo.online,
        platformId: detailInfo.platformId
      }
      add(params).then(res => {
        if (res.data && res.data.code == 0) {
          this.showInstallList()
          callback(true)
        } else {
          callback(false)
        }
      }).catch(err => {
        callback(false)
      })
    },
    // 添加应用模板
    handleAddTemp() {
      this.formOption.column.forEach((item, index) => {
        if (item.prop === 'imgs') {
          console.log(item)
          item.display = true
        } else {
          // item.disabled = true
        }
        if (index > 3) {
          item.display = false
        }
      })
      this.title = '添加应用模板'
      this.dialogVisible = true
      this.submitType = 'addTemplate'
      this.$nextTick(() => {
        this.initEditor()
      })
    },
    // 文件上传成功
    handleTemplateSuccess (response, fileList) {
      // console.log(response)
      if (response && response.code === 0) {
        this.getTemplateList()
      }
    },
    // 上传模板前的处理
    beforeTempUpload(file) {
      // 取消校验
      return true
    },
    // 获取模板类型
    getTemplateType() {
      getTemplateType().then(res => {
        if (res.data && res.data.code == 0) {
          this.templateTypes = res.data.data || []
        }
      })
    },
    templatePageChange(val) {
      this.templatePage.current = val
      this.getTemplateList()
    },
    // 获取推荐应用模板
    getTemplateList() {
      this.templateListLoading = true
      const params = {
        size: this.templatePage.size,
        current: this.templatePage.current,
        type: this.currentType,
      }
      if (this.queryParams.name) {
        params.name = this.queryParams.name
      }
      templateList(params).then(res => {
        if (res.data && res.data.data && res.data.code == 0) {
          this.templateList = res.data.data.records || []
          this.templatePage.total = res.data.data.total
        }
        this.templateListLoading = false
      })
    },
    // 点击模板
    handleClick(obj) {
      this.$refs.templateDetail.geTemplateDetail(obj) 
    },
    backAppCneter () {
      this.$emit('openTemplate', true)
    },
    openDialog (bool) {
      this.detailVisible = bool
      if(!bool) {
        this.$notify({
          title: '提示',
          message: '未查询到数据',
          position: 'bottom-right',
          type: 'warning'
        });
      }
    },
    showInstallList () {
      this.installListVisible = true
    }
  }
}
</script>

<style lang="scss" scoped>
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
.application-manage-tool-list{
  padding: 0;
  margin: 0;
  li{
    height: 32px;
    line-height: 32px;
    cursor: pointer;
    //padding-left: 20px;
    padding: 6px 24px;
    text-align: left;
  }
  li:hover{
    background: #eff2f7;
  }
}
.template-more-page{
  background: #fff;
  width: 100%;
  height: 100%;
  overflow: hidden;
  overflow-y: auto;
  .template-more-header{
    height: 260px;
    padding: 47px 260px;
    box-sizing: border-box;
    background: url(../../const/application/templatemoretop.png) no-repeat;
    background-size: cover;
    .header-content{
      .title{
        height: 46px;
        font-size: 32px;
        font-family: Source Han Sans-Bold, Source Han Sans;
        font-weight: 700;
        color: #363B4C;
        line-height: 46px;
      }
      .description{
        margin-top: 19px;
        height: 40px;
        font-size: 14px;
        font-family: Source Han Sans-Regular, Source Han Sans;
        font-weight: 400;
        color: #6F7588;
        line-height: 20px;
        p{
          line-height: 20px;
          margin: 0;
        }
      }
      .bottom-tool{
        margin-top: 25px;
        display: flex;
        align-items: center;
        .bt-button{
          width: 104px;
          height: 36px;
          background: #1E6FFF;
          border-radius: 4px;
          display: flex;
          align-items: center;
          justify-content: center;
          // margin-left: 12px;
          cursor: pointer;
          .icon{
            width: 16px;
            height: 16px;
            fill: #fff;
          }
          span{
            margin-left: 3px;
            font-size: 14px;
            font-family: Microsoft YaHei-Regular, Microsoft YaHei;
            font-weight: 400;
            color: #FFFFFF;
          }
        }
      }
    }
  }
  .template-more-center{
    padding: 0 260px;
    margin-top: 16px;
    display: flex;
    align-items: center;
    .left{
      display: flex;
      align-items: center;
      height: 20px;
      line-height: 20px;
      font-size: 14px;
      font-family: Source Han Sans, Source Han Sans;
      font-weight: 400;
      color: #6F7588;
      cursor: pointer;
      svg{
        display: block;
        width: 16px;
        height: 16px;
        margin-right: 5px;
      }
    }
  }
  .template-more-box{
    position: relative;
    margin-top: 18px;
    padding: 0 260px;
    .other-template-list{
      padding-bottom: 20px;
      .other-template-list-top{
        display: flex;
        justify-content: space-between;
        .template-types{
          display: flex;
          flex-wrap: wrap;
          width: 80%;
          .types-item{
            height: 32px;
            padding: 0 14px;
            line-height: 32px;
            margin-bottom: 8px;
            margin-right: 10px;
            border-radius: 4px;
            text-align: center;
            box-sizing: border-box;
            cursor: pointer;
            font-size: 14px;
            font-family: Source Han Sans-Regular, Source Han Sans;
            font-weight: 400;
            color: #6F7588;
            &.active{
              font-size: 16px;
              font-family: Source Han Sans-Medium, Source Han Sans;
              font-weight: 500;
              color: #1E6FFF;
              background: #E4EDFF;
            }
            &:hover{
              color: #1E6FFF;
              background: #E4EDFF;
            }
          }
        }
        .search-box{
          width: 224px;
          height: 36px;
          background: #F5F6F7;
          border-radius: 4px;
          /deep/.el-input{
            font-size: 14px;
            font-family: Source Han Sans-Regular, Source Han Sans;
            font-weight: 400;
            color: #6F7588;
            .el-input__inner{
              height: 36px;
              border: 0;
              padding-left: 36px;
              background: #F5F6F7;
              border-radius: 4px;
            }
            .el-input__prefix{
              margin-top: 2px;
              left: 5px;
              .el-icon-search{
                font-size: 14px;
                color: #6F7588;
              }
            }
          }
          /deep/.el-input::placeholder{
            font-size: 14px;
            font-family: Source Han Sans-Regular, Source Han Sans;
            font-weight: 400;
            color: #6F7588;
          }
        }
      }
      .template-list{
        box-sizing: border-box;
        align-items: center;
        display: grid;
        grid-template-columns: calc(20% - 12.8px) calc(20% - 12.8px) calc(20% - 12.8px) calc(20% - 12.8px) calc(20% - 12.8px);
        grid-row-gap: 16px;
        grid-column-gap: 16px;
        .loading-back{
          position: absolute;
          height: 100%;
          top: 0;
          left: 0;
          box-sizing: border-box;
          background-color: rgba(255, 255, 255, 0.8);
          background-image: url('../../../public/jvs-ui-public/img/loading.gif');
          background-repeat: no-repeat;
          background-position: center;
          background-position: center;
          //background-size: 200px 160px;
          z-index: 1000;
        }
        .template-item{
          position: relative;
          overflow: hidden;
          box-sizing: border-box;
          background: #F5F6F7;
          border-radius: 6px;
          display: flex;
          flex-direction: column;
          justify-content: space-between;
          overflow: hidden;
          .item-tag{
            position: absolute;
            top: 16px;
            right: -1px;
            width: 48px;
            height: 20px;
            box-sizing: border-box;
            overflow: hidden;
            .el-tag{
              background: #E4EDFF;
              font-size: 12px;
              font-family: Source Han Sans-Regular, Source Han Sans;
              font-weight: 400;
              color: #1E6FFF;
              border-radius: 4px 0px 0px 4px;
            }
          }
          .more-handle{
            color: #6F7588;
            position: absolute;
            right: 16px;
            bottom: 26px;
            font-size: 16px;
            cursor: pointer;
          }
          .item-content{
            padding: 16px;
            .item-header{
              display: flex;
              align-items: center;
              h5{
                height: 23px;
                font-size: 16px;
                font-family: Source Han Sans, Source Han Sans;
                font-weight: 700;
                color: #363B4C;
                line-height: 23px;
                margin: 0;
              }
            }
            .item-description{
              margin-top: 8px;
              height: 40px;
              font-size: 14px;
              font-family: Source Han Sans, Source Han Sans;
              font-weight: 400;
              color: #6F7588;
              line-height: 20px;
              word-wrap: break-word;
              overflow: hidden;
              text-overflow: ellipsis;
              display: -webkit-box;
              -webkit-line-clamp: 2;
              -webkit-box-orient: vertical;
              color: #a2a3a5;
            }
          }
          .bottom-div{
            margin: 0 16px 16px 16px;
            display: flex;
            align-items: center;
            .left{
              .bottom-div-left-button{
                width: 124px;
                height: 36px;
                line-height: 36px;
                text-align: center;
                background: #1E6FFF;
                border-radius: 6px;
                font-size: 14px;
                font-family: Source Han Sans-Regular, Source Han Sans;
                font-weight: 400;
                color: #fff;
                cursor: pointer;
              }
            }
          }
        }
      }
      .temp-pagination{
        margin-top: 16px;
        text-align: right;
      }
      h3{
        display: flex;
        justify-content: space-between;
        align-items: center;
        span{
          font-size: 18px;
        }
      }
    }
  }
  .template-more-box::-webkit-scrollbar{
    display: none;
  }
}
</style>
