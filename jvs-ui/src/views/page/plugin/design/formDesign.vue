<template>
  <div class="form-design-box">
    <div class="designer">
      <div class="assembly-designer">
        <Assembly
          :drag2="drag2"
          :isDetail="isDetail"
          @addcom="addcom"
          @setcom="setcom"
          @setdrag="setdrag"
          @deletecom="deletecom"
          @reset="reset"
        />
      </div>
      <div class="form-designer">
        <design-tool
          ref="topBar"
          :dataModelId="dataModelId"
          :jvsAppId="jvsAppId"
          :designId="designId"
          :mobileCode="mobileCode"
          :infoData="data"
          :rowData="data"
          :isDetail="isDetail"
          :formType="formType"
          :dataModelFields="tableOption"
          :saveLoading="saveLoading"
          @close="closeHandle"
          @getColumnNum="getColumnNum"
          @qingkong="qingkong"
          @connection="connection"
          @openBtnSetting="openBtnSetting"
        />
        <Forms
          ref="forms"
          :fromData="data"
          :formsetting="formsetting"
          :drag1="drag1"
          :drag2="drag2"
          :com="com"
          :formJson="formdata.formJson"
          :freshBoolean="dialogVisible"
          :masterTable="masterTable"
          :jsqlJson="formdata.jsqlJson"
          :customizeJsqlJson="formdata.customizeJsqlJson"
          :designId="designId"
          :isDetail="isDetail"
          :dataModelFields="tableOption"
          @deletecom="deletecom"
          @setdrag="setdrag"
          @reset="reset"
          @setdelcom="setdelcom"
          @addcom="addcom"
          @copy="copyCom"
          @jiaohuan="jiaohuan"
          @setformcom="setformcom"
          @setdialogVisible="setdialogVisible"
          @save="save"
          @qingkong="qingkong"
          @downloadvue="downloadvue"
          @freshFormData="freshFormDataHandle"
          @setJSQL="setJSQLHandle"
        />
      </div>
      <div class="attribute-designer">
        <AttrFrom
          :levelOption="levelOption"
          :fields="fields"
          :form="formcom"
          :jvsAppId="jvsAppId"
          :dataModelId="dataModelId"
          :designId="designId"
          :designName="designName"
          :formsetting="formsetting"
          :isDetail="isDetail"
          :isAddForm="isAddForm"
          :createOrigin="createOrigin"
          :drag2="drag2"
          :activeForm="activeName"
          :isFlowable="isFlowable"
          :flowableDom="flowableDom"
          :fineGrainedType="fineGrainedType"
          :columnNameList="columnNameList"
          :tableOption="tableOption"
          :allTable="allTable"
          :classifyDictList="classifyDictList"
          :systemDictList="systemDictList"
          :databaseName="selectDataSourceName"
          :isFlowDesign="isFlowDesign"
          :formType="formType"
          :domList="data"
          :showBtnSetting="showBtnSetting"
          @deletecom="deletecom"
          @updateForms="updateForms"
          @flowChange="flowChange"
          @changeActiveForm="changeActiveForm"
          @getColumnNum="getColumnNum"
          @autoSave="autoSave"
        />
      </div>
    </div>
    <el-dialog
      title="预览表单"
      :fullscreen="formsetting.fullscreen"
      :class="{'form-fullscreen-dialog': (formOption.fullscreen ? hasTabItem(formOption.fullscreen) : formOption.fullscreen)}"
      v-if="dialogVisible"
      :visible.sync="dialogVisible"
      top="5vh"
      :width="formOption.popupWidth ? (formOption.popupWidth + '%') : '70%'"
      :before-close="handleClose"
      :append-to-body="appendToBodyBool"
      :close-on-click-modal="false"
    >
      <ShowFrom type="normal" :option="formOption" :designId="designId" :jvsAppId="jvsAppId" @submit="saveForm" @close="closeHandle" />
    </el-dialog>
  </div>
</template>

<script>
// import dow from './dow'
import Assembly from '../assembly.vue'
import Forms from '../forms'
import AttrFrom from '../attributeform'
import ShowFrom from '../showForm'
import { getClassifyDict, getSystemDict } from '@/api/newDesign'
import DesignTool from "../../components/design/designTool";
import {guid} from "@/util/util";
import { copyFormulaComponent } from '@/views/page/api/newDesign'
export default {
  name: 'formdesign',
  components: {
    Assembly, Forms, AttrFrom, ShowFrom, DesignTool
  },
  props: {
    // 表单类型
    type: {
      type: String,
      default: 'normal'
    },
    jvsAppId: {
      type: String
    },
    designId: {
      type: String
    },
    dataModelId: {
      type: String
    },
    mobileCode: {
      type: String
    },
    designName: {
      type: String
    },
    // 多级、步骤 表单配置
    levelOption: {
      type: Object,
      default: () => {
        return {}
      }
    },
    // 表单结构
    formdata: {
      type: Object
    },
    // 激活表单key
    activeName: {
      type: String,
      default: ''
    },
    fields: {
      type: Array,
      default: () => {
        return []
      }
    },
    isFlowable: {
      type: Boolean,
      default: () => {
        return false
      }
    },
    // 是否为列表页详情按钮
    isDetail: {
      type: Boolean,
      default: () => {
        return false
      }
    },
    // 是否为列表页新增按钮
    isAddForm: {
      type: Boolean,
      default: () => {
        return false
      }
    },
    tableOption: {
      type: Array
    },
    flowableDom: {
      type: Object,
      default: () => {
        return {}
      }
    },
    fineGrainedType: {
      type: String
    },
    columnNameList: {
      type: Array
    },
    masterTable: {
      type: String
    },
    allTable: {
      type: Array
    },
    selectDataSourceName: {
      type: String
    },
    isFlowDesign: {
      type: Boolean
    },
    formType: {
      type: String
    },
    saveLoading: {
      type: Boolean
    },
    createOrigin: {
      type: String
    }
  },
  data () {
    return {
      // 是否正在拖动左侧的组件 1 没有动  2 已经拖动没有进入中间的界面  3拖动进入中间的页面 4 进入设计页面中的组件
      drag1: '1',
      // 是否正在拖动中间的组件 1 没有动  2 已经拖动没有进度其他组件  3 进入其他组件
      drag2: '1',
      // 左边 被操作的组件
      com: {},
      // 中间正在被拖动的组件的 如果拖动到垃圾桶 将会删除它
      delcom: {},
      // 表单列表
      data: [],
      // 表单设置
      formsetting: {
        labelposition: 'top',
        labelwidth: 80,
        formsize: 'mini',
        fullscreen: false
      },
      // 中间被点击的组件
      formcom: {
        showFrom: [],
        rules: []
      },
      // 预览弹框
      dialogVisible: false,
      // 弹框是否加到最外层
      appendToBodyBool: true,
      departmentList: [], // 部门列表
      userList: [], // 用户列表
      roleOption: [], // 角色列表
      formOption: {}, // 表单配置对象
      classifyDictList: [],
      systemDictList: [],
      jsqlJson: '',
      customizeJsqlJson: '',
      showBtnSetting: -1
    }
  },
  created () {
    if(this.formdata) {
      this.data = this.formdata.forms
      this.formsetting = this.formdata.formsetting
      if(this.formsetting.submitBtn !== false) {
        this.$set(this.formsetting, 'submitBtn', true)
      }
      if(this.formsetting.emptyBtn !== false) {
        this.$set(this.formsetting, 'emptyBtn', true)
      }
      this.$set(this.formsetting, 'cancal', false)
    }
    // 分类字典
    this.getClassifyDictHandle()
    // 系统字典
    // this.getSystemDictHandle()
  },
  mounted () {
    if (this.isDetail) {
      this.data.forEach(item => {
        item.disabled = true
      })
    }
  },
  methods: {
    getColumnNum(e) {
      this.data.forEach(item => {
        item.span = e
      })
    },
    // 左边的全部重置 左边的结束拖动时调用
    reset () {
      this.drag1='1'
      this.drag2='1'
      // 重置中间的横线
      this.$refs.forms.chonzhihxindex()
    },
    // 设置拖动状态
    setdrag (data) {
      this[data.type]=data.value
    },
    // 设置操作的组件
    setcom (data) {
      data.disabled = this.isDetail
      // console.log('最外层。。。。。')
      // console.log(data)
      this.com=data
    },
    // 设置被删除的组件
    setdelcom (data) {
      this.delcom=data
    },
    // 设置表单组件数据项
    setformcom (data) {
      this.formcom=data
      if(!this.formcom.tips) {
        this.formcom.tips = {
          position: "right",
          text: ""
        }
      }
    },
    // 清空
    qingkong () {
      this.data=[]
      this.formcom={
        showFrom: [],
        rules: [],
        tips: {
          position: "right",
          text: ""
        }
      }
      this.$emit('clear')
    },
    connection () {
      this.$emit('connection')
    },
    // 删除组件
    deletecom () {
      // 如果删除的是 被选中的 则把 选中的也清空
      if (this.delcom.prop===this.formcom.prop) {
        this.formcom={
          showFrom: [],
          rules: [],
          tips: {
            position: "right",
            text: ""
          }
        }
      }
      this.data=this.data.filter(item => item.prop!==this.delcom.prop)
      if(this.data.length == 0) {
        this.$emit('clear')
      }
    },
    // 增加一个组件
    addcom (index=-1) {
      if (index==-1) {
        this.data.push(this.com)
      } else {
        this.data.splice(index, 0, this.com)
      }
    },
    // 复制一个组件
    copyCom (item, index) {
      let copy = JSON.parse(JSON.stringify(item))
      item.id = guid()
      item.prop = item.type + guid()
      let needPlaceFormula = false
      if(item.defaultOrigin && item.defaultOrigin == 'formmula' && item.formula) {
        needPlaceFormula = true
      }else{
        if(['tab'].indexOf(item.type) > -1) {
          if(item.dicData && item.dicData.length > 0) {
            for(let i in item.dicData) {
              if(item.column && item.column[item.dicData[i].name] && item.column[item.dicData[i].name].length > 0) {
                for(let j in item.column[item.dicData[i].name]) {
                  if(item.column[item.dicData[i].name][j].defaultOrigin && item.column[item.dicData[i].name][j].defaultOrigin == 'formmula' && item.column[item.dicData[i].name][j].formula) {
                    needPlaceFormula = true
                    break
                  }else{
                    if(['tableForm'].indexOf(item.column[item.dicData[i].name][j].type) > -1) {
                      if(item.column[item.dicData[i].name][j].tableColumn && item.column[item.dicData[i].name][j].tableColumn.length > 0) {
                        for(let t in item.column[item.dicData[i].name][j].tableColumn) {
                          if(item.column[item.dicData[i].name][j].tableColumn[t].defaultOrigin && item.column[item.dicData[i].name][j].tableColumn[t].defaultOrigin == 'formmula' && item.column[item.dicData[i].name][j].tableColumn[t].formula) {
                            needPlaceFormula = true
                            break
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
        if(['tableForm'].indexOf(item.type) > -1) {
          if(item.tableColumn && item.tableColumn.length > 0) {
            for(let t in item.tableColumn) {
              if(item.tableColumn[t].defaultOrigin && item.tableColumn[t].defaultOrigin == 'formmula' && item.tableColumn[t].formula) {
                needPlaceFormula = true
                break
              }
            }
          }
        }
      }
      if(needPlaceFormula) {
        copyFormulaComponent(this.jvsAppId, this.designId, copy).then(res => {
          if(res.data && res.data.code == 0) {
            copy = res.data.data
            if(index > -1) {
              this.data.splice(index, 0, copy)
            }else{
              this.data.push(copy)
            }
            this.$nextTick(() => {
              this.$refs.forms.clickitem(item)
            })
          }
        })
      }else{
        if(index > -1) {
          this.data.splice(index, 0, copy)
        }else{
          this.data.push(copy)
        }
        this.$nextTick(() => {
          this.$refs.forms.clickitem(item)
        })
      }
    },
    // 交换位置
    jiaohuan (start, end) {
      let arr=this.data
      // JSON.parse(JSON.stringify(this.data))
      let aa=arr[start]
      if(Math.abs(start-end) == 1) {
        arr[start]=arr[end]
        arr[end]=aa
      }else{
        arr.splice(start, 1)
        arr.splice(end < 0 ? 0 : end, 0, aa)
      }
      this.data=arr
    },
    // 保存
    save (formJson, isRuleSave) {
      let data = []
      for(let d in this.data) {
        data.push(Object.assign({}, this.data[d]))
      }
      for(let i in data) {
        if(data[i].type == 'switch') {
          data[i].type = 'SWITCH'
        }
      }
      this.$emit('save', {
        formsetting: this.formsetting,
        forms: data,
        formJson: JSON.stringify(formJson),
        autoTableFields: this.columnNameList,
        isFlowable: this.isFlowable,
        flowableDom: this.flowableDom,
        customizeJsqlJson: this.customizeJsqlJson,
        jsqlJson: this.jsqlJson
      }, isRuleSave)
    },
    // 下载
    download (filename, text) {
      let element=document.createElement('a');
      element.setAttribute('href', 'data:text/plain;charset=utf-8,'+encodeURIComponent(text));
      element.setAttribute('download', filename);
      element.style.display='none';
      document.body.appendChild(element);
      element.click();
      document.body.removeChild(element);
    },
    // 下载源码
    downloadvue () {
      let filename='hello.vue'
      let text=dow(this.formsetting, this.data)
      // console.log(text)
      this.download(filename, text)
    },
    saveForm (form) {
      console.log(form)
    },
    // 预览的 对话框
    setdialogVisible () {
      this.$emit('yulan', true)
      this.formOption = JSON.parse(JSON.stringify(this.formsetting))
      this.formOption.cancal =  false
      this.formOption.column = this.data
      this.formOption.formAlign = this.formOption.labelposition
      this.formOption.labelWidth = this.formOption.labelwidth == 'auto' ? this.formOption.labelwidth : (this.formOption.labelwidth + 'px')
      if(this.type == 'normal') {
        // console.log(this.formOption)
        this.dialogVisible=true
        // 流程设计器---预览时需调整外层弹框的关闭按钮 z-index
        let dom = $("#flowableDesignDialog .el-dialog__header .el-dialog__headerbtn", parent.document)
        if(dom) {
          dom.css({'z-index': 0})
        }
      }
    },
    // 关闭预览
    handleClose () {
      this.dialogVisible=false
      let dom = $("#flowableDesignDialog .el-dialog__header .el-dialog__headerbtn", parent.document)
      if(dom) {
        dom.css({'z-index': 9})
      }
    },
    // 里层通知关闭弹框
    closeHandle (bool) {
      if (bool) {
        this.handleClose()
      }
    },
    // 改变表单结构
    updateForms (data) {
      this.$emit('updateForms', data)
    },
    // 改变flowable设置
    flowChange (data) {
      this.$emit('flowChange', data)
    },
    // 改变激活表单key
    changeActiveForm (str) {
      this.$emit('changeActiveForm', str)
    },
    // 获取分类字典
    getClassifyDictHandle () {
      getClassifyDict().then(res => {
        if(res.data.code == 0) {
          this.classifyDictList = res.data.data
          // console.log(res.data.data)
        }
      })
    },
    getSystemDictHandle () {
      getSystemDict().then(res => {
        if(res.data.code == 0) {
          this.systemDictList = res.data.data
        }
      })
    },
    freshFormDataHandle(bool) {
      this.$emit("freshFormData", bool)
    },
    // 判断是否有tab组件
    hasTabItem (bool) {
      let result = bool
      if(this.formOption && this.formOption.column) {
        for(let i in this.formOption.column) {
          if(this.formOption.column[i].type == 'tab' && this.formOption.column[i].dicData && this.formOption.column[i].dicData.length > 4) {
            result = false
          }
          if(this.formOption.column[i].type == 'tableForm' && !this.formOption.column[i].editable) {
            result = false
          }
        }
      }
      return bool // result
    },
    setJSQLHandle (code) {
      this.customizeJsqlJson = code
    },
    openBtnSetting () {
      this.showBtnSetting = Math.random()
    },
    autoSave () {
      if(this.$refs.forms) {
        this.$refs.forms.save(true)
      }
    }
  },
  watch: {
    data: function () {
      this.$emit("setForm", this.data)
    },
    levelOption: function (newVal, oldVal) {
      console.log(newVal)
    }
  }
}
</script>

<style lang="scss" scoped>
.form-design-box{
  height: 100%;
  overflow: hidden;
  .designer{
    height: 100%;
    overflow: hidden;
    display: flex;
    .assembly-designer{
      width: 280px;
      height: 100%;
      background: linear-gradient( 180deg, #F2F7FF 0%, #FFFFFF 15%);
      padding: 16px 16px 0 24px;
      box-sizing: border-box;
      overflow: hidden;
      .assemblycont{
        width: 240px;  
        overflow-y: auto;
        /deep/.el-card__body{
          padding: 0;
        }
      }
      .assemblycont::-webkit-scrollbar{
        display: none;
      }
    }
    .form-designer{
      position: relative;
      z-index: 1000;
      width: calc(100% - 600px);
    }
    .attribute-designer{
      width: 320px;
      position: relative;
      z-index: 999;
      background: #fff;
      height: 100%;
      overflow: hidden;
      /deep/.el-tabs{
        border-top: 1px solid #f5f6f7;
        .el-tabs__header{
          margin: 0;
          .el-tabs__item{
            width: 56px;
            height: 50px;
            line-height: 50px;
            font-family: Microsoft YaHei, Microsoft YaHei;
            font-weight: 400;
            font-size: 14px;
            color: #6F7588;
            box-sizing: content-box;
            &:hover{
              color: #6F7588;
            }
          }
          .el-tabs__item.is-active{
            color: #3471FF;
            font-family: Microsoft YaHei-Bold, Microsoft YaHei;
            font-weight: 700;
          }
          .el-tabs__active-bar{
            min-width: 56px;
            height: 3px;
            background: #1E6FFF;
            border-radius: 2px 0px 2px 0px;
          }
          .el-tabs__nav-wrap{
            padding: 0 24px;
          }
          .el-tabs__nav-wrap::after{
            height: 1px;
            background: #EEEFF0;
          }
        }
      }
    }
  }
}
</style>
<style lang="scss">
.titleCol {
  scrollbar-width: none; /* firefox */
  -ms-overflow-style: none; /* IE 10+ */
  overflow-x: hidden;
  overflow-y: auto;
}
.titleCol::-webkit-scrollbar {
  display: none; /* Chrome Safari */
}
</style>
