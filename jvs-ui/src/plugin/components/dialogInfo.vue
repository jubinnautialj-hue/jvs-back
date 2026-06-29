<template>
  <!-- 挂载表单 -->
  <div class="form-show-info-dialog">
    <el-dialog
      :title="formTitle"
      :visible.sync="formVisible"
      :fullscreen="(formType == 'normalForm' || formType == 'detailForm') ? formOption.fullscreen: false"
      :class="{'form-fullscreen-dialog': (formType == 'normalForm' || formType == 'detailForm') ? (formOption.fullscreen ? hasTabItem(formOption.fullscreen) : formOption.fullscreen): true,
        'drawer-popup-dialog': (!formOption.fullscreen && formOption.popupType == 'drawer'),
        'form-with-log': (['add'].indexOf(openType) == -1 && formOption.dataLogEnable)}"
      append-to-body
      :width="(formOption.popupWidth ? (formOption.popupWidth + '%') : '')"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :before-close="handleCloseForm">
      <el-row v-if="tagSetting || ['add'].indexOf(openType) == -1" class="tag-tool-bar" style="position:absolute;">
        <el-popover
          v-if="['add'].indexOf(openType) == -1 && formOption.logsEnable !== false"
          placement="bottom"
          v-model="logshow"
          trigger="click">
          <div v-show="logshow" class="time-line-info-box">
            <el-timeline>
              <el-timeline-item
                v-for="(item, index) in logList"
                :key="'log-list-item-'+index"
                :timestamp="item.timestamp">
                <div class="line-item">
                  <div class="line-item-top">
                    <div class="left">
                      <img :src="item.headImg" alt="">
                      <span>{{item.userName}}</span>
                    </div>
                    <div class="right">
                      <span>{{item.timestamp}}</span>
                    </div>
                  </div>
                  <div class="line-item-content" v-html="item.content"></div>
                </div>
              </el-timeline-item>
            </el-timeline>
          </div>
          <span slot="reference" class="log-tag-span-box">
            <svg aria-hidden="true" @click.stop="getFormEditLogsHandle">
              <use xlink:href="#jvs-ui-icon-lishijilu"></use>
            </svg>
          </span>
        </el-popover>
        <el-popover
          v-if="tagSetting"
          v-model="tagshow"
          placement="bottom"
          trigger="click">
          <div v-show="tagshow" class="tag-set-info-box">
            <h3>标签</h3>
            <div id="formTag" style="overflow:hidden;width: 380px;">
              <h4 style="background:#409EFF;color:#fff;font-size:18px;margin:0;padding:15px 10px;font-weight:normal;border-radius: 4px 4px 0 0;">
                <span>{{(tagDataTransform && tagSetting.title.businessId) ? tagSetting.title.businessId : (tagSetting.title.text || '主标题')}}</span>
                <br/>
                <span style="font-size:12px;">{{(tagDataTransform && tagSetting.subTitle.businessId) ? tagSetting.subTitle.businessId : (tagSetting.subTitle.text || '')}}</span>
              </h4>
              <div style="display:flex;justify-content:space-between;padding:10px 15px;background:#f5f7fa;
                border-radius: 0 0 4px 4px;border-top:0;overflow:hidden;box-sizing: content-box;">
                <div style="font-size: 15px;">
                  <p v-for="(fi, fix) in tagSetting.fieldList" :key="'tag-field-item-'+fix">
                    <b style="color:#333;">{{fi.text}}</b>
                    <span v-if="fi.text && fi.businessId && tagDataTransform" style="color:#666;">{{':'+fi.businessId}}</span>
                  </p>
                </div>
                <div style="text-align:right;">
                  <span id="tagQRcode" ref="tagQRcode" style="display:block;width:130px;height:130px;"></span>
                  <!-- <span style="color:#c0c4cc;">created by jvs</span> -->
                </div>
              </div>
            </div>
            <div style="width: 100%;display: flex;justify-content: flex-end;align-items: center;margin-top: 10px;">
              <el-button size="mini" type="text" @click="downloadTag">下载图片</el-button>
            </div>
          </div>
          <span slot="reference" class="log-tag-span-box">
            <svg aria-hidden="true" @click.stop="getTagDetail">
              <use xlink:href="#jvs-ui-icon-changguikapian"></use>
            </svg>
          </span>
        </el-popover>
      </el-row>
      <div :class="{'form-box': true, 'form-box-2': (['add'].indexOf(openType) == -1 && formOption.dataLogEnable)}">
        <div class="form-body">
          <jvs-form
            ref="ruleForm"
            :class="{'show-form': true, 'disabled-show-form': (openType == 'view')}"
            v-if="(formType == 'normalForm' || formType == 'detailForm') && formVisible"
            :option="formOption"
            :formData="formData"
            :rowData="rowData"
            :designId="formDesignId"
            :dataModelId="dataModelId"
            :execsList="execsList"
            :jvsAppId="jvsAppId"
            :associationSettingsFields="associationSettingsFields"
            @submit="formSubmit"
            @cancalClick="handleCloseForm">
            <!-- 自定义按钮 -->
            <template slot="formButton" v-if="true || formOption.flag">
              <span v-if="hasPrint && printTemplateList && printTemplateList.length > 0" style="margin: 0 10px;">
                <el-popover
                  v-if="printTemplateList.length > 1"
                  placement="right"
                  trigger="hover">
                  <div class="print-list-items">
                    <ul>
                      <li v-for="pi in printTemplateList" :key="pi.id" @click="printHandle(pi)">
                        <span>{{pi.name}}</span>
                      </li>
                    </ul>
                  </div>
                  <jvs-button v-if="hasPrint" slot="reference" size="mini">打印</jvs-button>
                </el-popover>
                <jvs-button v-if="hasPrint && printTemplateList.length == 1" slot="reference" size="mini" @click="printHandle(printTemplateList[0])">打印</jvs-button>
              </span>
              <jvs-button size="mini" v-for="(item, index) in formOption.btnSetting" v-if="['submit', 'empty', 'print', 'cancel'].indexOf(item.buttonType) == -1 && item.enable" :key="item.name+'slotbtn'+index" :loading="item.loading" @click="slotbtnClickHandle(item, index)">{{item.name}}</jvs-button>
            </template>
            <template v-else slot="formButton">
              <span v-if="hasPrint && printTemplateList && printTemplateList.length > 0" style="margin-left:10px;">
                <el-popover
                  v-if="printTemplateList.length > 1"
                  placement="right"
                  trigger="hover">
                  <div class="print-list-items">
                    <ul>
                      <li v-for="pi in printTemplateList" :key="pi.id" @click="printHandle(pi)">
                        <span>{{pi.name}}</span>
                      </li>
                    </ul>
                  </div>
                  <jvs-button v-if="hasPrint" slot="reference" size="mini">打印</jvs-button>
                </el-popover>
                <jvs-button v-if="hasPrint && printTemplateList.length == 1" size="mini" @click="printHandle(printTemplateList[0])">打印</jvs-button>
              </span>
            </template>
          </jvs-form>
          <!-- 详情表单打印 -->
          <el-row v-if="openType == 'view' && printTemplateList && printTemplateList.length > 0" style="display:flex;align-items:center;justify-content: center;">
            <span v-if="printTemplateList.length > 1">
              <el-popover
                placement="right"
                trigger="hover">
                <div class="print-list-items">
                  <ul>
                    <li v-for="pi in printTemplateList" :key="pi.id" @click="printHandle(pi)">
                      <span>{{pi.name}}</span>
                    </li>
                  </ul>
                </div>
                <jvs-button slot="reference" size="mini">打印</jvs-button>
              </el-popover>
            </span>
            <jvs-button v-if="printTemplateList.length == 1" size="mini" @click="printHandle(printTemplateList[0])">打印</jvs-button>
          </el-row>
        </div>
        <div class="form-log-box" v-if="['add'].indexOf(openType) == -1 && formOption.dataLogEnable">
          <formLog v-if="formVisible" :jvsAppId="jvsAppId" :dataModelId="dataModelId" :dataId="rowData.id"></formLog>
        </div>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getFormInfo } from '@/views/page/api/formlist'
import { getSingleData } from '@/views/page/api/design'
import { getUserInfo } from '@/api/admin/user'
import { getDefaultData } from '@/views/page/util/common'
import { ruleRun } from '@/api/common'
import { getAvailableTemplate } from '@/views/print/api/index'
export default {
  name: 'form-show-info',
  props: {
    title: {
      type: String
    },
    jvsAppId: {
      type: String
    },
    dataModelId: {
      type: String
    },
    getEcho: {
      type: [Boolean, String]
    }
  },
  data(){
    return {
      // 表单
      formType: '',
      formTitle: '',
      formVisible: false,
      formData: {},
      formOption: {
        column: []
      },
      hasPrint: false,
      userInfo: {},
      dataId: null,
      rowData: null,
      openType: '',
      tagSetting: null, // 标签设置
      tagshow: false,
      tagDataTransform: false,
      logshow: false,
      logList: [],
      associationSettingsFields: [],
      execsList: [],
      designData: null,
      selectFormItems: [], // 表单里的下拉选择项
      printTemplateList: [],
    }
  },
  created () {
    if(this.$store.getters && this.$store.getters.userInfo) {
      this.userInfo = JSON.parse(JSON.stringify(this.$store.getters.userInfo))
    }else{
      this.getUserInfo()
    }
  },
  methods: {
    async childFormInit (code, row, type) {
      console.log(row)
      this.formDesignId = code
      this.openType = type
      if(row && row.id) {
        this.dataId = row.id
        this.rowData = JSON.parse(JSON.stringify(row))
      }else {
        this.dataId = null
      }
      await this.getFormInfoHandle()
      if(!this.designData.form || !this.designData.form.formdata) {
        return false
      }
      this.getSelectItem(this.designData.form.formdata)
      if(!this.formTitle) {
        this.formTitle = this.designData.name
      }
      this.formType = this.designData.form.formType
      this.getFormColumn(this.designData.form.formType, this.designData)
      if(this.designData.form.formdata[0].formsetting && this.designData.form.formdata[0].formsetting.dataEchoRequest) {
        let rdata = row ? JSON.parse(JSON.stringify(row)) : {}
        if(rdata && rdata.jvsEnabledButtons) {
          delete rdata.jvsEnabledButtons
        }
        rdata.dataModelId = this.dataModelId
        let header = {
          formDesignId: this.formDesignId
        }
        ruleRun(this.jvsAppId, this.designData.form.formdata[0].formsetting.dataEchoRequest, rdata, header).then(res => {
          if(res && res.data && res.headers["output_format"] && res.data.data){
            let name = decodeURI(res.headers["output_format"])
            if(res.data.data.originalName) {
              name = res.data.data.originalName
            }
            this.ruleDownLoad(name, res.data.data)
          }else if(res && res.data && res.headers["output_type"] == 'preview' && res.data.data) {
            this.previewFile(res.data.data)
          }else{
            if(res.data && res.data.code == 0 && res.data.data) {
              this.formData = JSON.parse(JSON.stringify(getDefaultData(res.data.data, this.designData.form.formdata[0].forms, this.userInfo)))
              if(this.formData && this.formOption.formTitleKey && this.formData[this.formOption.formTitleKey]) {
                this.formTitle = this.formData[this.formOption.formTitleKey]
              }
              if(this.formData && this.formData.jvsEnabledButtons && this.formData.jvsEnabledButtons.length > 0) {
                if(this.formOption.btnSetting) {
                  let delIndexs = []
                  for(let i in this.formOption.btnSetting) {
                    if(this.formOption.btnSetting[i].buttonType == 'print' && this.formData.jvsEnabledButtons.indexOf(this.formOption.btnSetting[i].permissionFlag) == -1) {
                      this.hasPrint = false
                    }else if(this.formOption.btnSetting[i].buttonType == 'submit' && this.formData.jvsEnabledButtons.indexOf(this.formOption.btnSetting[i].permissionFlag) == -1) {
                      this.formOption.submitBtn = false
                    }else if(this.formOption.btnSetting[i].buttonType == 'empty' && this.formData.jvsEnabledButtons.indexOf(this.formOption.btnSetting[i].permissionFlag) == -1) {
                      this.formOption.emptyBtn = false
                    }else{
                      if(this.formData.jvsEnabledButtons.indexOf(this.formOption.btnSetting[i].permissionFlag) == -1) {
                        delIndexs.push(i)
                      }
                    }
                  }
                  if(delIndexs && delIndexs.length > 0) {
                    this.formOption.btnSetting = this.formOption.btnSetting.filter((fit, fix) => {
                      if(delIndexs.indexOf(fix+'') == -1) {
                        return fit
                      }
                    })
                  }
                }
              }else{
                if(item.position == 'line') {
                  this.formOption.btnHide = true
                }
              }
            }else{
              this.formData = getDefaultData(rdata, this.designData.form.formdata[0].forms, this.userInfo)
            }
            if(this.hasPrint) {
              this.getAvailableTemplateHandle()
            }
            if(res.data.msg && res.headers["output_status"]) {
              this.$notify({
                title: '提示',
                message: res.data.msg,
                position: 'bottom-right',
                type: (res && res.data && res.headers["output_status"] == 'false') ? 'error' : 'success',
                duration: (res && res.data && res.headers["output_status"] == 'false' && res.headers["message_close"] == 'false') ? 0 : 4500
              });
            }
            this.formVisible = true
          }  
        }).catch(e => {
          this.formVisible = true
        })
      }else{
        if(this.getEcho && this.formDesignId && row && row.id) {
          getSingleData(this.jvsAppId, this.dataModelId, row.id, this.formDesignId, this.rowData.id).then(res => {
            if (res.data && res.data.code == 0) {
              this.formData = JSON.parse(JSON.stringify(getDefaultData(res.data.data, this.designData.form.formdata[0].forms, this.userInfo)))
              if(this.formData && this.formOption.formTitleKey && this.formData[this.formOption.formTitleKey]) {
                this.formTitle = this.formData[this.formOption.formTitleKey]
              }
              if(this.formData && this.formData.jvsEnabledButtons && this.formData.jvsEnabledButtons.length > 0) {
                if(this.formOption.btnSetting) {
                  for(let i in this.formOption.btnSetting) {
                    if(this.formOption.btnSetting[i].buttonType == 'print' && this.formData.jvsEnabledButtons.indexOf(this.formOption.btnSetting[i].permissionFlag) == -1) {
                      this.hasPrint = false
                    }else if(this.formOption.btnSetting[i].buttonType == 'submit' && this.formData.jvsEnabledButtons.indexOf(this.formOption.btnSetting[i].permissionFlag) == -1) {
                      this.formOption.submitBtn = false
                    }else if(this.formOption.btnSetting[i].buttonType == 'empty' && this.formData.jvsEnabledButtons.indexOf(this.formOption.btnSetting[i].permissionFlag) == -1) {
                      this.formOption.emptyBtn = false
                    }else{
                      if(this.formData.jvsEnabledButtons.indexOf(this.formOption.btnSetting[i].permissionFlag) == -1) {
                        this.formOption.btnSetting.splice(i, 1)
                      }
                    }
                  }
                }
              }else{
                this.formOption.btnHide = true
              }
              if(this.hasPrint) {
                this.getAvailableTemplateHandle()
              }
              this.formVisible = true
            }
          })
        }else{
          this.formData = row
          this.formData = JSON.parse(JSON.stringify(getDefaultData(this.formData, this.designData.form.formdata[0].forms, this.userInfo)))
          this.formVisible = true
        }
      }
    },
    async getFormInfoHandle () {
      await getFormInfo(this.jvsAppId, this.formDesignId).then(res => {
        if(res.data.code == 0) {
          if(res.data.data.name) {
            this.formTitle = res.data.data.name
          }
          if(res.data.data.tagSetting && res.data.data.tagSetting.openTag) {
            this.tagSetting = res.data.data.tagSetting
          }
          if(res.data.data.associationSettingsFields) {
            this.associationSettingsFields = res.data.data.associationSettingsFields
          }
          this.execsList = []
          if(res.data.data.viewJson) {
            this.designData = {
              form: JSON.parse(res.data.data.viewJson)
            }
            if(this.designData.form.execs) {
              this.execsList = this.designData.form.execs
            }
          }else{
            this.$notify({
              title: '提示',
              message: '功能设计不完整，请联系管理员设计！',
              position: 'bottom-right',
              type: 'warning'
            })
          }
        }
      })
    },
    // 表单配置
    getFormColumn (type, item) {
      if(item.form.formdata && item.form.formdata.length > 0) {
        // 兼容历史设计数据
        this.formatFormItem(item.form.formdata)
        if(type == 'normalForm' || type == 'detailForm') {
          this.formOption = this.formatFormOption(type, item.form.formdata[0].forms, item.form.formdata[0].formsetting)
        }else{
          let ct = []
          for(let i in item.form.column) {
            let obj = {
              defaultData: item.form.column[i].defaultData,
              formOption: {},
              label: item.form.column[i].label,
              name: item.form.column[i].name,
              show: item.form.column[i].show || true
            }
            obj.formOption = {
              btnSetting: item.form.formdata[i].formsetting.btnSetting,
              size: item.form.formdata[i].formsetting.formsize,
              formAlign: item.form.formdata[i].formsetting.labelposition,
              labelWidth: item.form.formdata[i].formsetting.labelwidth + '',
              column: item.form.formdata[i].forms,
            }
            ct.push(obj)
          }
          this.formOption = {
            type: 'card',
            column: ct,
            formdata: item.form.formdata
          }
        }
      }else{
        this.formOption = {
          btnHide: true,
          column: []
        }
      }
      if(this.formOption.btnSetting) {
        for(let i in this.formOption.btnSetting) {
          if(this.formOption.btnSetting[i].buttonType == 'print' && this.formOption.btnSetting[i].enable) {
            this.hasPrint = true
          }
        }
      }
    },
    // 格式化表单配置项
    formatFormOption (type, forms, formsetting) {
      let temp = {
        column: JSON.parse(JSON.stringify(forms)),
        btnSetting: formsetting.btnSetting,
        size: formsetting.formsize,
        formAlign: formsetting.labelposition,
        labelWidth: formsetting.labelwidth + 'px',
        fullscreen: formsetting.fullscreen,
        cancal: false,
        flag: formsetting.flag,
        submitBtn: formsetting.submitBtn,
        submitLoading: false,
        emptyBtn: formsetting.emptyBtn,
        popupType: formsetting.popupType || 'dialog',
        popupWidth: formsetting.popupWidth || 50,
        logsEnable: formsetting.logsEnable === false ? false : true,
        dataLogEnable: formsetting.dataLogEnable ? true : false,
        formTitleKey: formsetting.title ? formsetting.title : ''
      }
      if(type == 'detailForm') {
        temp.disabled = true
        temp.btnHide = true
      }else{
        temp.disabled = false
        temp.btnHide = false
      }
      let hasSub = false
      let hasEmpt = false
      let hasCancel = false
      for(let i in temp.btnSetting) {
        if(temp.btnSetting[i].buttonType == 'submit') {
          if(temp.btnSetting[i].enable) {
            hasSub = true
            temp.submitBtn = true
            temp.submitBtnText = temp.btnSetting[i].name || '提交'
          }
        }
        if(temp.btnSetting[i].buttonType == 'empty') {
          if(temp.btnSetting[i].enable) {
            hasEmpt = true
            temp.emptyBtn = true
            temp.emptyBtnText = temp.btnSetting[i].name || '重置'
          }
        }
        if(temp.btnSetting[i].buttonType == 'cancel') {
          if(temp.btnSetting[i].enable) {
            hasCancel = true
            temp.cancal = true
            temp.cancalBtnText = temp.btnSetting[i].name || '取消'
          }
        }
      }
      if(hasSub) {
        temp.submitBtn = true
      }else{
        temp.submitBtn = false
      }
      if(hasEmpt) {
        temp.emptyBtn = true
      }else{
        temp.emptyBtn = false
      }
      if(hasCancel) {
        temp.cancal = true
      }else{
        temp.cancal = false
      }
      return temp
    },
    // 兼容历史设计数据
    formatFormItem (formdata) {
      for(let i in formdata) {
        if(formdata[i].forms) {
          for(let j in formdata[i].forms) {
            let item = this.getItemByValOfArr(formdata[i].forms[j].prop, 'aliasColumnName', formdata[i].autoTableFields)
            // 字典 来源 和 配置
            if(formdata[i].forms[j].url || formdata[i].forms[j].dicUrl) {
              if(this.menuId) {
                formdata[i].forms[j].dicUrl = formdata[i].forms[j].url
              }else{
                formdata[i].forms[j].dicUrl = ''
                formdata[i].forms[j].url = ''
              }
              if(item) {
                if(!formdata[i].forms[j].props) {
                  formdata[i].forms[j].props = {
                    label: '',
                    value: ''
                  }
                }
                if(!formdata[i].forms[j].props.label && item.displayField) {
                  formdata[i].forms[j].props.label = item.displayField.columnName
                }
                if(!formdata[i].forms[j].props.value && item.associatedFields) {
                  formdata[i].forms[j].props.value = item.associatedFields.columnName
                }
              }
            }else{
              if(!formdata[i].forms[j].props && ['user', 'role', 'department', 'group', 'job'].indexOf(formdata[i].forms[j].type) == -1) {
                formdata[i].forms[j].props = {
                  label: 'label',
                  value: 'value'
                }
              }
            }
            if(formdata[i].forms[j].type == 'select') {
              if(item) {
                // 单选 多选
                if(item.correspondence == "ONE_TO_N") {
                  formdata[i].forms[j].multiple = true
                }else{
                  formdata[i].forms[j].multiple = false
                }
              }
            }
            // 日期  时间 配置
            if(formdata[i].forms[j].type == 'datePicker' || formdata[i].forms[j].type == 'timePicker') {
              if(item) {
                formdata[i].forms[j].datetype = item.dataType
                if(item) {
                  switch(item.dbJavaType) {
                    case 'field_date_time':
                      formdata[i].forms[j].datetype = 'datetime';
                      formdata[i].forms[j].format = "yyyy-MM-dd hh:mm:ss";
                      formdata[i].forms[j].valueFormat = "yyyy-MM-dd hh:mm:ss";
                      break;
                    case 'field_date':
                      formdata[i].forms[j].datetype = 'date';
                      formdata[i].forms[j].format = "yyyy-MM-dd";
                      formdata[i].forms[j].valueFormat = "yyyy-MM-dd";
                      break;
                    case 'field_time':
                      formdata[i].forms[j].format = "HH:mm:ss";
                      formdata[i].forms[j].valueFormat = "HH:mm:ss";
                      break;
                    default : formdata[i].forms[j].datetype = 'datetime';
                      formdata[i].forms[j].format = "yyyy-MM-dd hh:mm:ss";
                      formdata[i].forms[j].valueFormat = "yyyy-MM-dd hh:mm:ss";
                      break;
                  }
                }
              }
            }
            if(formdata[i].forms[j].type == 'user') {
              formdata[i].forms[j].allowinput = false
            }
            // 开关
            if(formdata[i].forms[j].type == 'SWITCH') {
              formdata[i].forms[j].type = 'switch'
            }
            // 只读文本
            // if(formdata[i].forms[j].type == 'inputReadOnly') {
            //   console.log(formdata[i].forms[j])
            // }
          }
        }
      }
    },
    // 获取select项，表单值为数组
    getSelectItem (list) {
      let temp = []
      for(let i in list) {
        for(let j in list[i].forms) {
          if(list[i].forms[j].type == 'select' && list[i].forms[j].multiple) {
            temp.push(list[i].forms[j].prop)
          }
        }
      }
      this.selectFormItems = temp
    },
    // 根据val获取数据对应项
    getItemByValOfArr (val, attr, list) {
      for(let i in list) {
        if(list[i][attr] == val) {
          return list[i]
        }
      }
      return false
    },
    // 关闭表单
    handleCloseForm () {
      this.formData = {}
      this.formVisible = false
    },
    getUserInfo() {
      getUserInfo().then(res => {
        if(res.data.code == 0) {
          this.userInfo = res.data.data
        }
      })
    },
    // 表单提交
    formSubmit (formsdata) {
      this.$emit('submit', this.formData)
      this.handleCloseForm()
    },
    // 自定义按钮事件
    slotbtnClickHandle (row, index) {
      if(!row.loading) {
        this.$set(row, 'loading', false)
      }
      let validate = true
      if(row.validateable) {
        validate = this.$refs.ruleForm.validateForm()
      }
      if(!validate) {
        return false
      }
      let tp = null
      tp = JSON.parse(JSON.stringify(row))
      if(this.$store.getters.labelValue && this.$store.getters.labelValue.requestContentType) {
        tp.requestContentType = this.$store.getters.labelValue.requestContentType[tp.requestContentType]
      }
      let tob = JSON.parse(JSON.stringify(this.formData))
      for(let k in tob) {
        if(tob[k] && tob[k] instanceof Array) {
          for(let n in tob[k]) {
            if(tob[k][n] === null || tob[k][n] === undefined) {
              tob[k].splice(n, 1)
            }
          }
        }
      }
      if(tp) {
        if(tp.secret) {
          let rdata = JSON.parse(JSON.stringify(this.formData))
          rdata.dataModelId = this.dataModelId
          row.loading = true
          ruleRun(this.jvsQueryData.jvsAppId, tp.secret, rdata, {designId: this.formDesignId}).then(res => {
            row.loading = false
            if(res && res.data && res.headers["output_format"] && res.data.data){
              let name = decodeURI(res.headers["output_format"])
              if(res.data.data.originalName) {
                name = res.data.data.originalName
              }
              this.ruleDownLoad(name, res.data.data)
              this.getListData()
            }else if(res && res.data && res.headers["output_type"] == 'preview' && res.data.data) {
              this.previewFile(res.data.data)
              this.getListData()
            }else{
              if(res.data && res.data.code == 0) {
                if(res.data.msg && res.headers["output_status"]) {
                  this.$notify({
                    title: '提示',
                    message: res.data.msg,
                    position: 'bottom-right',
                    type: (res && res.data && res.headers["output_status"] == 'false') ? 'error' : 'success',
                    duration: (res && res.data && res.headers["output_status"] == 'false' && res.headers["message_close"] == 'false') ? 0 : 4500
                  });
                }else{
                  this.$notify({
                    title: '提示',
                    message: tp.name + '成功',
                    position: 'bottom-right',
                    type: 'success'
                  })
                }
                if(res.data.data) {
                  for(let k in res.data.data) {
                    this.$set(this.formData, k, res.data.data[k])
                  }
                }
                if(res && res.data && res.headers["output_status"] == 'false') {
                  return false
                }
                if(row.closeable !== false) {
                  this.handleCloseForm()
                }
              }
            }
          }).catch(e => {
            row.loading = false
          })
        }else{
          if(row.closeable !== false) {
            this.handleCloseForm()
          }
        }
      }
    },
    // 表单获取可打印的模板列表
    getAvailableTemplateHandle () {
      getAvailableTemplate(this.jvsAppId, this.formDesignId).then(res => {
        if(res && res.data && res.data.code == 0) {
          this.printTemplateList = res.data.data
        }
      })
    },
  }
}
</script>
<style lang="scss" scoped>
/deep/.show-form{
  .el-form-item{
    padding: 0 20px;
  }
}
.form-fullscreen-dialog{
  /deep/.el-dialog__body{
    padding: 0;
    height: calc(100% - 45px);
    .tag-tool-bar{
      right: 50px;
      top: 10px;
    }
    .form-log-box{
      width: 30%;
    }
  }
}
.el-dialog__wrapper.form-with-log{
  /deep/.el-dialog{
    .el-dialog__body{
      padding: 0;
    }
  }
}
</style>
