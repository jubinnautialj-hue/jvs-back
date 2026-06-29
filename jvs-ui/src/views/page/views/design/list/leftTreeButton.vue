<template>
  <div class="button-info">
    <tableForm
      :option="option"
      :data="tableData"
      @nameChange="buttonNameValidate"
      @typeChange="typeChangeHandle"
    >
      <!-- 按钮名称 -->
      <template slot="nameItem" slot-scope="scope">
        <div>
          <el-input v-model="scope.row.name" placeholder="请输入" size="mini"></el-input>
        </div>
      </template>
      <!-- 按钮类型 -->
      <template slot="typeItem" slot-scope="scope">
        <div>
          <el-select size="mini" v-model="scope.row.type" placeholder="请选择">
            <el-option
              v-for="item in buttonTypeItemshow()"
              :key="item.value+'-btn-item'"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </div>
      </template>
      <!-- 操作栏 -->
      <template slot="menuBtn" slot-scope="scope">
        <span class="sort-item" style="margin-right: 15px;">
          <i v-if="scope.index > 0" class="el-icon-top" :style="(scope.index < tableData.length -1) ? 'margin-right:10px;' : ''" @click="sortHandle('up', scope.index)"></i>
          <i v-if="scope.index < tableData.length -1" class="el-icon-bottom" @click="sortHandle('down', scope.index)"></i>
        </span>
        <jvs-button type="text" @click="openButtonFormula(scope.row, scope.index)" size="mini">公式</jvs-button>
        <jvs-button type="text" @click="configBtnHandle(scope.row, scope.index)" size="mini">设计</jvs-button>
        <jvs-button type="text" @click="deleteRow(scope.row, scope.index)" size="mini">删除</jvs-button>
      </template>
    </tableForm>
    <el-row style="margin-top: 10px">
      <jvs-button size="mini" @click="addRowHandle">添加按钮</jvs-button>
    </el-row>
    <!-- 配置表单 -->
    <!-- 选择配置页面类型 -->
    <el-dialog
      v-if="formTypeDialogVisible"
      :visible.sync="formTypeDialogVisible"
      :before-close="handleCloseformTypeDialog"
      append-to-body
      title="设计"
      :close-on-click-modal="false"
    >
      <!-- 请求配置 -->
      <div v-if="['btn_network_request'].indexOf(currentBtn.type) > -1">
        <dataSourceForm :sourceType="sourceType" labelText="数据来源类型" :form="{http: currentBtn.netHttp}" @submit="netSubmitHandle"></dataSourceForm>
      </div>
      <el-row v-if="['btn_external_link_address', 'btn_embedded_address'].indexOf(currentBtn.type) > -1" style="display:flex;align-items:center;">
        <span class="el-form-item__label" style="width:90px;">地址</span>
        <el-input size="mini" v-model="currentBtn.address"></el-input>
      </el-row>
      <el-row v-if="['btn_network_request'].indexOf(currentBtn.type) == -1" style="display: flex; justify-content: center">
        <jvs-button type="primary" size="mini" @click="formTypeSubmit" style="margin-top: 10px">确定</jvs-button>
        <jvs-button size="mini" @click="handleCloseformTypeDialog" style="margin-top: 10px">取消</jvs-button>
      </el-row>
    </el-dialog>
  </div>
</template>
<script>
import tableForm from '../../../components/basic-design/tableForm'
import normalForm from '../../../components/basic-design/normalForm'

// 表单项
import MInput from '../../../plugin/assembly/input'
import MTextarea from '../../../plugin/assembly/textarea'
import MInputNumber from '../../../plugin/assembly/inputNumber'
import MSwitch from '../../../plugin/assembly/switch'
import MTimepicker from '../../../plugin/assembly/timepicker'
import MDatePicker from '../../../plugin/assembly/datePicker'
import MSelect from '../../../plugin/assembly/select'
import MUser from '../../../plugin/assembly/user'
import MDepartment from '../../../plugin/assembly/department'
import MRole from '../../../plugin/assembly/role'
import MPost from '../../../plugin/assembly/post'

import MInputReadOnly from '../../../plugin/assembly/inputreadonly'

import dataSourceForm from '../../../plugin/datasource'
import {getButtonFormId} from "../../../api/newDesign";
import { createRule } from "@/views/page/api/design";

import {guid} from "@/util/util";

export default {
  name: 'button-info',
  components: { tableForm, normalForm, dataSourceForm },
  props: {
    menuName: {
      type: String
    },
    data: {
      type: Array,
      default: () => {
        return []
      }
    },
    designColumn: {
      type: Array
    },
    infoData: {
      type: Object
    },
    designId: {
      type: String
    },
    dataModelId: {
      type: String
    },
  },
  computed: {
    tableData: {
      get () {
        let temp = this.data
        let index = -1
        let delItem = null
        for(let i in temp) {
          if(!temp[i].type) {
            temp[i].type = 'btn_rule_design'
          }
          if(["btn_form"].indexOf(temp[i].type) > -1) {
            if(!temp[i].form || !temp[i].form.formdata || temp[i].form.formdata.length == 0) {
              this.initDefaultForm(temp[i])
            }else{
              if(temp[i].form) {
                for(let j in temp[i].form.formdata) {
                  if(!temp[i].form.formdata[j].forms) {
                    temp[i].form.formdata[j].forms = this.eachColumnList(temp[i].form.formType, temp[i]).column
                  }
                  if(!temp[i].form.formdata[j].formJson) {
                    temp[i].form.formdata[j].formJson = this.eachColumnList(temp[i].form.formType, temp[i]).formJson
                  }
                  this.formatFormItem(temp[i].form.formdata)
                }
              }
            }
            if(!temp[i].formType) {
              temp[i].formType = temp[i].type == 'normalForm'
            }
          }
          if(!temp[i].permissionFlag) {
            temp[i].permissionFlag = temp[i].position + '-' + temp[i].type + '-' + guid()
          }
        }
        if(index > -1) {
          temp.splice(index, 1)
        }
        if(delItem) {
          temp.push(delItem)
        }
        return temp
      },
      set () { }
    },
  },
  data () {
    return {
      // 按钮设置表单配置
      option: {
        addBtn: false,
        viewBtn: false,
        delBtn: false,
        editBtn: false,
        page: false,
        border: true,
        cancal: false,
        column: [
          {
            label: '按钮名称',
            prop: 'name',
            // disabled: true
            needSlot: true,
            watch: true
          },
          {
            label: '按钮类型',
            prop: 'type',
            type: 'select',
            dicData: [],
            disabled: true,
            watch: true,
            needSlot: true
          },
        ]
      },
      currentBtn: {}, // 当前按钮
      formTypeDialogVisible: false, // 选择表单类型
      // 网络请求
      networkForm: {
        type: '',
        url: '',
        headers: [],
        body: ''
      },
      // 网络请求配置
      networkOption: {
        btnHide: true,
        column: [
          {
            label: '请求类型',
            prop: 'type',
            type: 'select',
            dicData: [
              {label: 'GET', value: 'GET'},
              {label: 'POST', value: 'POST'},
              {label: 'DELETE', value: 'DELETE'},
              {label: 'PUT', value: 'PUT'}
            ]
          },
          {
            label: '地址',
            prop: 'url'
          },
          {
            label: '请求头',
            prop: 'headers',
            formSlot: true
          }
        ]
      },
      // 按钮类型列表
      buttonTypeList: [],
      sourceType: 'data_origin_request', // 数据来源类型， 默认为网络请求
      boolShow: true, // 表单设计内容 显隐
      formId: '',
    }
  },
  methods: {
    // 添加按钮
    addRowHandle () {
      this.tableData.push({
        id: 'BTN'+new Date().getTime(),
        name: '按钮名称',
        position: 'top',
        type: 'btn_rule_design',
        netHttp: {}, // 网络请求
        secret: null, // 逻辑引擎唯一标识
        // 进一步配置的数据
        disabled: false,
        fineGrainedType: '',
        form: {
          formdata: [],
          formType: ''
        },
        enable: true,
        isDefault: false
      })
      this.$emit('permissionHandle', true)
    },
    // 删除按钮
    deleteRow (row, index) {
      this.tableData.splice(index, 1)
      this.$emit('permissionHandle', true)
    },
    // 配置按钮
    configBtnHandle (row, index) {
      if(['btn_form'].indexOf(row.type) > -1) {
        if (row.formId) {
          let str = ''
          str = location.origin + (`/page-design-ui/#/form?jvsAppId=${this.infoData.jvsAppId}&id=`+row.formId + (this.dataModelId ? `&dataModelId=${this.dataModelId}` : '') + `&isDetail=${row.type === 'btn_detail'}&isAddForm=${row.position == 'top'}&origin=leftTree`)
          this.$openUrl(str, '_blank')
        }else {
          getButtonFormId(this.infoData.jvsAppId, this.dataModelId, this.designId, row.name).then(res => {
            if (res.data && res.data.code == 0) {
              row.formId = res.data.data
              this.$emit('handleSave')
              let str = ''
              str = location.origin + (`/page-design-ui/#/form?jvsAppId=${this.infoData.jvsAppId}&id=`+res.data.data + (this.dataModelId ? `&dataModelId=${this.dataModelId}` : '') + `&isDetail=${row.type === 'btn_detail'}&isAddForm=${row.position == 'top'}&origin=leftTree`)
              this.$openUrl(str, '_blank')
            }
          })
        }
      }
      if(row.form) {
        if(!row.form.formdata) {
          row.form.formdata = []
        }
      }
      if(!row.formName) {
        row.formName = row.type + row.formType + '_' + new Date().getTime()
      }
      if(['btn_network_request', 'btn_embedded_address', 'btn_external_link_address', 'btn_rule_design'].indexOf(row.type) > -1) {
        this.currentBtn=row
        // 逻辑引擎
        if(['btn_rule_design'].indexOf(this.currentBtn.type) > -1) {
          if (this.currentBtn.secret) {
            this.$openUrl(`/rule-design-ui/#/ruleDesign?id=${this.currentBtn.secret}&componentId=${row.id}&jvsAppId=${this.infoData.jvsAppId}&name=${row.name}`, '_blank')
          } else {
            createRule({jvsAppId: this.infoData.jvsAppId, componentId: row.id, name: row.name, designId: this.designId, componentType: 'page'}).then(res => {
              if (res.data && res.data.code == 0) {
                this.currentBtn.secret = res.data.data
                this.data = this.tableData
                this.$openUrl(`/rule-design-ui/#/ruleDesign?id=${res.data.data}&componentId=${row.id}&jvsAppId=${this.infoData.jvsAppId}&name=${row.name}`, '_blank')
                this.$emit('handleSave', true)
              }
            })
          }
        }
        // 网络请求 处理数据回显
        if(['btn_network_request'].indexOf(this.currentBtn.type) > -1) {
          this.formTypeDialogVisible = true
        }
        // 内嵌地址  外链地址
        if(["btn_embedded_address", "btn_external_link_address"].indexOf(this.currentBtn.type) > -1) {
          this.formTypeDialogVisible = true
        }
      }
    },
    // 按钮类型改变
    typeChangeHandle (data) {
      let item=data.item
      let row=data.row
      if(row.type == 'NETWORK') {
        row.fineGrainedType = 'NETWORK'
      }
      if(row.type == 'FORM') {
        row.fineGrainedType = 'FORM'
      }
    },
    // 关闭选择表单类型
    handleCloseformTypeDialog () {
      this.formTypeDialogVisible=false
    },
    // 确定表单类型
    formTypeSubmit () {
      if(this.currentBtn.type == 'NETWORK') {
        let temp = JSON.parse(JSON.stringify(this.networkForm))
        let obj = {}
        for(let i in this.networkForm.headers) {
          obj[this.networkForm.headers[i].key] = this.networkForm.headers[i].value
        }
        temp.headers = obj
        this.currentBtn.networkForm = temp
      }
      this.handleCloseformTypeDialog()
    },
    getConst () {
      // 按钮类型dic
      this.buttonTypeList = this.setDicData('buttonType', 'type')
    },
    setDicData (key, prop) {
      let labelValue = this.$store.getters.labelValue[key]
      let temp = []
      let hasPrint = false
      for(let i in labelValue) {
        temp.push({
          label: labelValue[i],
          value: i
        })
        if(i == 'btn_print') {
          hasPrint = true
        }
      }
      if(!hasPrint) {
        temp.push({
          label: '打印',
          value: 'btn_print'
        })
      }
      this.option.column.filter(item => {
        if(item.prop == prop) {
          item.dicData = temp
        }
      })
      return temp
    },
    // 按钮类型根据位置和是否默认过滤
    buttonTypeItemshow () {
      let temp = []
      for(let i in this.buttonTypeList) {
        let bool = false
        switch (this.buttonTypeList[i].value) {
          case 'btn_embedded_address' :
          case 'btn_external_link_address':
          case 'btn_network_request':
          case 'btn_rule_design':
          case 'btn_form':
            bool = true;
            break;
          default : bool = false;break;
        }
        if(bool) {
          temp.push(this.buttonTypeList[i])
        }
      }
      return temp
    },
    // 遍历字段列表生成表单项
    eachColumnList (type, currentBtn) {
      let temp = []
      let formJson = {}
      for(let i in currentBtn.form.formdata[0].autoTableFields) {
        let obj = {}
        switch(currentBtn.form.formdata[0].autoTableFields[i].componentType) {
          case 'input':
            obj = new MInput();
            formJson[currentBtn.form.formdata[0].autoTableFields[i].aliasColumnName] = "";
            break;
          case 'textarea':
            obj = new MTextarea();
            formJson[currentBtn.form.formdata[0].autoTableFields[i].aliasColumnName] = "";
            break;
          case 'inputNumber':
            obj = new MInputNumber();
            // 整数 或 小数
            if(currentBtn.form.formdata[0].autoTableFields[i].isFloat == true) {
              obj.precision = 4
              formJson[currentBtn.form.formdata[0].autoTableFields[i].aliasColumnName] = 1.0001;
            }else{
              obj.precision = 0
              formJson[currentBtn.form.formdata[0].autoTableFields[i].aliasColumnName] = 1;
            }
            break;
          case 'SWITCh':
            obj = new MSwitch();
            formJson[currentBtn.form.formdata[0].autoTableFields[i].aliasColumnName] = false;
            break;
          case 'timePicker':
            obj = new MTimepicker();
            formJson[currentBtn.form.formdata[0].autoTableFields[i].aliasColumnName] = "10:00:00";
            break;
          case 'datePicker':
            obj = new MDatePicker();
            formJson[currentBtn.form.formdata[0].autoTableFields[i].aliasColumnName] = "2021-02-05 10:00:00";
            break;
          case 'select':
            obj = new MSelect();
            obj.multiple = false;
            formJson[currentBtn.form.formdata[0].autoTableFields[i].aliasColumnName] = "";
            if(currentBtn.form.formdata[0].autoTableFields[i].associatedFieldHttp) {
              obj.url = currentBtn.form.formdata[0].autoTableFields[i].associatedFieldHttp
              obj.datatype = 'url'
            }
            if(currentBtn.form.formdata[0].autoTableFields[i].associatedFields) {
              obj.props.value = currentBtn.form.formdata[0].autoTableFields[i].associatedFields.columnName
            }
            if(currentBtn.form.formdata[0].autoTableFields[i].displayField) {
              obj.props.label = currentBtn.form.formdata[0].autoTableFields[i].displayField.columnName
            }
            if(currentBtn.form.formdata[0].autoTableFields[i].advancedSettings && currentBtn.form.formdata[0].autoTableFields[i].advancedSettings.dictionary) {
              obj.dicData = currentBtn.form.formdata[0].autoTableFields[i].advancedSettings.dictionary
            }
            break;
          case 'inputReadOnly':
            obj = new MInputReadOnly();
            formJson[currentBtn.form.formdata[0].autoTableFields[i].aliasColumnName] = "";
            break;
          case 'user':
            obj = new MUser();
            formJson[currentBtn.form.formdata[0].autoTableFields[i].aliasColumnName] = "";
            break;
          case 'department':
            obj = new MDepartment();
            formJson[currentBtn.form.formdata[0].autoTableFields[i].aliasColumnName] = "";
            break;
          case 'role':
            obj = new MRole();
            formJson[currentBtn.form.formdata[0].autoTableFields[i].aliasColumnName] = "";
            break;
          case 'post':
            obj = new MPost();
            formJson[currentBtn.form.formdata[0].autoTableFields[i].aliasColumnName] = "";
            break;
          default :
            obj = new MInput();
            formJson[currentBtn.form.formdata[0].autoTableFields[i].aliasColumnName] = "";
            break;
        }
        obj.label = currentBtn.form.formdata[0].autoTableFields[i].columnComment
        obj.prop = currentBtn.form.formdata[0].autoTableFields[i].aliasColumnName
        temp.push(obj)
      }
      return {column: temp, formJson: JSON.stringify(formJson)}
    },
    // 生成默认表单
    initDefaultForm (currentBtn) {
      // 表单
      if (["btn_form"].indexOf(currentBtn.type) > -1) {
        if(!currentBtn.form) {
          currentBtn.form = {}
        }
        // 新增的表单按钮 填充 autoTableFields
        if(!currentBtn.form.formdata || currentBtn.form.formdata.length == 0) {
          currentBtn.form.formdata = [
            {
              forms: [],
              formsetting: {
                labelposition: 'top',
                labelwidth: 80,
                formsize: 'mini',
                btnSetting: [],
                fullscreen: false
              },
              autoTableFields: []
            }
          ]
        }
        if(!currentBtn.form.formType) {
          currentBtn.form.formType = 'normalForm'
        }
        let tob = this.eachColumnList(currentBtn.form.formType, currentBtn)
        let defaultForm = {
          forms: tob.column,
          formsetting: {
            labelposition: 'top',
            labelwidth: 80,
            formsize: 'mini',
            btnSetting: [],
            fullscreen: false
          },
          formJson: tob.formJson,
        }
        if(!currentBtn.form) {
          currentBtn.form = {
            type: '',
            formdata: [defaultForm],
            autoTableFields: []
          }
        }
        if(!currentBtn.form.formdata || currentBtn.form.formdata.length == 0 || !currentBtn.form.formdata[0].forms) {
          currentBtn.form.formdata = [defaultForm]
        }
      }
    },
    //网络请求配置
    netSubmitHandle (form) {
      if(form) {
        this.currentBtn.netHttp = form.http
        this.handleCloseformTypeDialog()
      }
    },
    // 兼容历史数据
    formatFormItem (formdata) {
      for(let i in formdata) {
        if(formdata[i].forms) {
          for(let j in formdata[i].forms) {
            // 下拉配置
            let item = this.getItemByValOfArr(formdata[i].forms[j].prop, 'aliasColumnName', formdata[i].autoTableFields)
            if(item) {
              if(formdata[i].forms[j].type == 'select') {
                if(!formdata[i].forms[j].props) {
                  formdata[i].forms[j].props = {
                    label: 'label',
                    value: 'value'
                  }
                }
                if(item.displayField) {
                  formdata[i].forms[j].props.label = item.displayField.columnName
                }
                if(item.associatedFields) {
                  formdata[i].forms[j].props.value = item.associatedFields.columnName
                }
                if(formdata[i].forms[j].url) {
                  formdata[i].forms[j].dicUrl = formdata[i].forms[j].url
                  formdata[i].forms[j].datatype = 'url'
                }
              }
              // 必填校验
              if(item.isNullable == 'NO'){
                if(formdata[i].forms[j].rules && formdata[i].forms[j].rules.length > 0) {
                  formdata[i].forms[j].rules[0].required = true
                  formdata[i].forms[j].rules[0].message = item.columnComment + '不能为空'
                }else{
                  formdata[i].forms[j].rules.push({required: true, message: (item.columnComment + '不能为空'), trigger: ["blur", "change"]})
                }
              }
            }
            // 兼容switch
            if(formdata[i].forms[j].type == 'SWITCH') {
              formdata[i].forms[j].type = 'switch'
            }
          }
        }
      }
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
    // 按钮名称重复校验
    buttonNameValidate (data) {
      let index = data.index
      let row = data.row
      let count = 0
      for(let i in this.tableData) {
        if(this.tableData[i].name == row.name) {
          count++
        }
      }
      if(count > 1 || !row.name) {
        let name = '新增按钮' + new Date().getTime()
        this.$set(this.tableData[index], 'name', name)
      }
      this.$emit('permissionHandle', true)
    },
    // 重排顺序
    sortHandle (type, index) {
      index = Number.parseInt(index)
      let own = JSON.parse(JSON.stringify(this.tableData[index]))
      let change = null
      if(type == 'up') {
        change = JSON.parse(JSON.stringify(this.tableData[index-1]))
        this.$set(this.tableData, (index-1), own)
        this.$set(this.tableData, index, change)
      }
      if(type == 'down') {
        change = JSON.parse(JSON.stringify(this.tableData[index+1]))
        this.$set(this.tableData, (index+1), own)
        this.$set(this.tableData, index, change)
      }
    },
    // 按钮配置公式
    openButtonFormula (item, index) {
      this.$setFormula({
        title: '公式设置',
        hasModal: true,
        label: item.name,
        execId: item.formula ? item.formula : '',
        apiPrefix: 'jvs-design',
        useCase: 'leftTreeButtonDisplay',
        props: {
          jvsAppId: this.infoData.jvsAppId,
          designId: this.designId,
          businessId: item.permissionFlag
        },
        afterSave: (dialog, data) => {
          if(data && data.id) {
            this.$set(this.tableData[index], 'formula', data.id)
            this.$emit('handleSave')
          }
          dialog.handleClose()
        }
      })
    },
  },
  created () {
    this.getConst()
  },
  watch: {
    'networkForm.type': {
      handler(newVal, oldVal) {
        this.networkOption.column.filter(item => {
          if(item.prop == 'body') {
            if(newVal == 'get') {
              item.display = false
            }else{
              item.display = true
            }
          }
        })
      }
    }
  }
}
</script>
<style lang="scss" scoped>
.design-title-bar {
  position: fixed;
  background: #fff;
  top: 0;
  left: 0;
  z-index: 9;
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-sizing: border-box;
  .el-row {
    .el-select,
    .el-button {
      margin-left: 20px;
    }
  }
}
.design-form-cont{
  margin-top: 10px;
  height: calc(100% - 54px);
  overflow: hidden;
}
.sort-item{
  i{
    cursor: pointer;
  }
}
</style>
<style lang="scss">
.form-design-no-header-dialog{
  height: 100%;
  overflow: hidden;
  .el-dialog__header{
    display: none!important;
  }
  .el-dialog__body{
    padding: 0;
    height: 100%;
    overflow: hidden;
    padding: 8px 10px;
    background: #f0f2f5;
    box-sizing: border-box;
  }
  .title-page-header{
    margin-top: 0;
    z-index: 99999999;
    position: relative;
  }
  .form-design-tool{
    font-size: 25px;
    cursor: pointer;
    color: #353535;
  }
}
.select-multiple-table{
  .jvs-table{
    .jvs-table-titleTop{
      display: none;
    }
    .table-body-box{
      .el-table{
        margin-top: 0;
        .el-table__header-wrapper{
          margin-top: 0;
        }
        .el-table__body-wrapper{
          height: auto!important;
        }
      }
    }
  }
}
</style>
