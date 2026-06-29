<template>
  <div :class="{'oprationlog-manage': true, 'change-mode-type': (jvsDesign && jvsDesign.JVS_DESIGN_MGR && $permissionMatch('jvs_app'))}">
    <div v-if="jvsDesign && jvsDesign.JVS_DESIGN_MGR && $permissionMatch('jvs_app')" class="mode-type-list">
      <div v-for="ritem in modeList" :key="ritem.value" :class="{'mode-type-list-item': true, 'active': ritem.value == currentType}" @click="searchTypeChange(ritem.value)">{{$langt(`topNav.modeUser.${ritem.value}`)}}</div>
    </div>
    <jvs-table
      :option="tableOption"
      :loading="tableLoading"
      :data="tableData"
      pageheadertitle='环境变量'
      :page="page"
      @on-load="getList"
      @search-change="searchChange"
      @addRow="addRow"
      @editRow="editRow"
      @delRow="delRow"
    >
      <template slot="menuLeft">
        <el-button size="mini" type="primary" @click="btnClick({type: 'text'}, 'add')">{{$langt('table.add')}}</el-button>
      </template>
      <template slot="menu" slot-scope="scope">
        <el-button size="mini" type="text" @click="btnClick(scope.row, 'edit')">{{$langt('table.edit')}}</el-button>
        <el-button size="mini" type="text" @click="btnClick(scope.row, 'view')">{{$langt('table.view')}}</el-button>
      </template>
    </jvs-table>
    <el-dialog
      class="custom-header-dialog"
      :visible.sync="dialogVisible"
      :before-close="handleClose"
      :title="$langt(`table.${dialogType}`)"
      append-to-body
      :close-on-click-modal="false"
    >
      <div v-if="dialogVisible" class="form-dialog-box">
        <div class="left">
          <div class="title">
            <i class="el-icon-info"></i>
            <span>{{$langt('envariable.desc')}}</span>
          </div>
          <div v-show="rowData.type == tip.type" v-for="(tip, tix) in leftTips" :key="tix" class="left-tip-item">
            <div class="tip-title">{{tip.title}}</div>
            <div class="tip-desc">{{tip.desc}}</div>
          </div>
        </div>
        <div class="right">
          <jvs-form ref="myform" refs="myform" :option="formOption" :formData="rowData" @formChange="formChange" @submit="submitHandle">
            <template slot="valueForm">
              <div v-if="rowData.type == 'key'" class="key-map-list">
                <div v-if="keysList && keysList.length > 0" :class="{'list': true, 'needpadding': (keysList && keysList.length > 0)}">
                  <div v-for="(kv, kx) in keysList" :key="'k-v-item-'+kx" class="key-map-item">
                    <el-input v-model="kv.key" size="mini" clearable placeholder="key" @change="changeValidate"></el-input>
                    <el-input v-model="kv.value" size="mini" clearable placeholder="value" @change="changeValidate"></el-input>
                    <i class="el-icon-delete" @click="deleteKV(kx, keysList)"></i>
                  </div>
                </div>
                <div class="add-button">
                  <el-button type="primary" size="mini" @click="addKV">{{$langt('table.add')}}</el-button>
                </div>
              </div>
              <FormItem
                v-else
                :form="rowData"
                :item="{ prop: 'value', disabled: dialogType == 'view', type: (rowData.type == 'file' ? 'fileUpload' : 'textarea'), limit: 1}"
                :originOption="formOption"
                @formChange="formChange"
                @uploadChange="uploadChange"
              />
            </template>
          </jvs-form>
        </div>
      </div>
      <div v-if="dialogType != 'view'" class="foot-button">
      <el-button size="mini" @click="clickHandle('reset')">{{$langt('form.reset')}}</el-button>
      <el-button size="mini" type="primary" @click="clickHandle('submit')">{{$langt('form.save')}}</el-button>
    </div>
    </el-dialog>
  </div>
</template>
<script>
import {getEnvVariableList, addEnvVariable, editEnvVariable, delEnvVariable} from './api'
import FormItem from '@/components/basic-assembly/formitem'
import { getStore } from "@/util/store.js";
import { getDynamicDesign } from '@/api/admin/home'
export default {
  name: 'oprationlog-manage',
  components: { FormItem },
  data () {
    return {
      tableLoading: false,
      queryParams: {},
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
      },
      tableData: [],
      tableOption: {
        showOverflow: true,
        addBtn: false,
        editBtn: false,
        viewBtn: false,
        search: true,
        page: true,
        cancal: false,
        dialogWidth: '50%',
        column: [
          {
            label: '类型',
            prop: 'type',
            span: 24,
            search: true,
            type: 'select',
            clearable: false,
            dicData: [
              {label: '文本环境变量', value: 'text'},
              {label: '文件环境变量', value: 'file'},
              {label: '键值对环境变量', value: 'key'}
            ]
          },
          {
            label: '键',
            prop: 'label',
            span: 24,
            search: true,
            editDisabled: true,
            rules: [{ required: true, message: '请输入键', trigger: ['change', 'blur'] }],
            regularExpression: '^[\u4e00-\u9fa5a-zA-Z]{0,}$',
            regularMessage: '只能输入中文和英文'
          },
          {
            label: '值',
            prop: 'value',
            type: 'textarea',
            formSlot: true,
            rules: [{ required: true, message: '值不能为空', trigger: ['change', 'blur'] }]
          },
          {
            label: '说明',
            prop: 'remark',
            type: 'textarea'
          }
        ]
      },
      dialogType: 'add',
      dialogVisible: false,
      rowData: null,
      formOption: {
        cancal: false,
        submitLoading: false,
        column: []
      },
      leftTips: [
        {
          type: 'text',
          title: '文本环境变量',
          desc: '文本环境变量主要用于在公式、逻辑、工作流、表单、列表等设计器中动态插入或引用特定的文本信息。它们允许用户根据实际需求定义和配置文本值，使得在设计和运行时，设计器可以自动替换这些变量为实际的文本内容。通过这种方式，用户可以轻松地实现文本的个性化、动态化和可配置化，提高设计的灵活性和可维护性。'
        },
        {
          type: 'file',
          title: '文件环境变量',
          desc: `文件环境变量主要用于与文件相关的操作，如打印、导出模板等。这些变量通常用于指定文件的路径、名称、格式等属性，以便在运行时正确地定位和处理文件。通过使用文件环境变量，用户可以实现文件路径的动态化和可配置化，减少硬编码带来的维护成本，并提高系统的可移植性和可扩展性。`
        },
        {
          type: 'key',
          title: '键值对环境变量',
          desc: '键值对环境变量主要用于逻辑节点配置中，用于存储和引用键值对形式的配置信息。它们通常以“键=值”的形式存在，其中键用于标识特定的配置项，值则是对应的配置值。通过使用键值对环境变量，用户可以实现配置的灵活性和可定制性，方便地对逻辑节点的行为进行细粒度的控制。同时，由于配置信息存储在环境变量中，也便于进行统一的管理和维护。'
        }
      ],
      keysList: [],
      jvsDesign: null,
      currentType: 'GA',
      modeList: [
        { label: '开发模式', value: 'DEV' },
        { label: '测试模式', value: 'BETA' },
        { label: '正式模式', value: 'GA'}
      ]
    }
  },
  created () {
    let modeUser = getStore({ name: 'modeUserInfo' })
    if(modeUser) {
      this.currentType = modeUser.mode
    }
    this.getDynamicDesign()
    this.leftTips.filter((col, cix) => {
      col.title = this.$langt(`envariable.leftTips[${cix}].title`)
      col.desc = this.$langt(`envariable.leftTips[${cix}].desc`)
    })
    this.tableOption.column.filter(col => {
      if(col.label) {
        col.label = this.$langt(`envariable.column.${col.prop}.label`)
      }
      if(col.rules && col.rules.length > 0) {
        col.rules[0].message = this.$langt(`envariable.column.${col.prop}.placeholder`)
      }
      if(col.dicData && col.dicData.length > 0) {
        col.dicData.filter(dit => {
          dit.label = this.$langt(`envariable.column.${col.prop}.dicData.${dit.value}`)
        })
      }
      if(col.regularMessage) {
        col.regularMessage = this.$langt(`envariable.column.${col.prop}.regularMessage`)
      }
    })
    this.formOption.column = JSON.parse(JSON.stringify(this.tableOption.column))
    this.formOption.column.filter(col => {
      if(col.prop == 'type') {
        col.defaultValue = 'text'
      }
    })
    
  },
  methods: {
    getList (page) {
      let query={
        current: this.page.currentPage,
        size: this.page.pageSize
      }
      if(this.currentType) {
        query.mode = this.currentType
      }
      this.tableLoading = true
      let temp = JSON.parse(JSON.stringify(this.queryParams))
      getEnvVariableList(Object.assign(query, temp)).then(res => {
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
    addRow (row) {
      row.mode = this.currentType
      addEnvVariable(row).then(res => {
        if(res.data && res.data.code == 0) {
          this.$notify({
            title: this.$langt('common.tip'),
            message: this.$langt('common.addSuccess'),
            position: 'bottom-right',
            type: 'success'
          })
          this.handleClose()
          this.getList()
        }
      })
    },
    editRow (row) {
      row.mode = this.currentType
      editEnvVariable(row).then(res => {
        if(res.data && res.data.code == 0) {
          this.$notify({
            title: this.$langt('common.tip'),
            message: this.$langt('common.editSuccess'),
            position: 'bottom-right',
            type: 'success'
          })
          this.handleClose()
          this.getList()
        }
      })
    },
    delRow (row, index) {
      delEnvVariable(row.id).then(res => {
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
    },
    btnClick (data, type) {
      this.keysList = []
      this.rowData = JSON.parse(JSON.stringify(data))
      if(this.rowData.type == 'key' && this.rowData.value) {
        for(let k in this.rowData.value) {
          this.keysList.push({
            key: k,
            value: this.rowData.value[k]
          })
        }
      }
      this.dialogType = type
      if(type == 'view') {
        this.formOption.disabled = true
      }else{
        this.formOption.disabled = false
      }
      this.dialogVisible = true
    },
    clickHandle (type) {
      if(this.$refs.myform) {
        if(type == 'reset') {
          this.$refs.myform.resetForm('myform')
        }
        if(type == 'submit') {
          this.$refs.myform.submitForm('myform')
        }
      }
    },
    formChange (form, item, before) {
      if(item.prop == 'type' && form[item.prop] != before) {
        if(this.rowData.type == 'text') {
          this.$set(this.rowData, 'value', '')
        }
        if(this.rowData.type == 'file') {
          this.$set(this.rowData, 'value', [])
        }
        if(this.rowData.type == 'key') {
          this.$set(this.rowData, 'value', {})
        }
      }
    },
    submitHandle () {
      if(this.rowData.type == 'key') {
        this.rowData.value = {}
        this.keysList.filter(it => {
          if(it.key && it.value) {
            this.$set(this.rowData.value, it.key, it.value)
          }
        })
        if(!this.rowData.value || JSON.stringify(this.rowData.value) == '{}') {
          if(this.$refs.myform) {
            this.$set(this.rowData, 'value', null)
            this.$refs.myform.$refs.myform.validateField('value')
          }
          return false
        }
      }
      if(this.rowData.id) {
        this.editRow(this.rowData)
      }else{
        this.addRow(this.rowData)
      }
    },
    handleClose () {
      this.dialogVisible = false
      this.rowData = null
      this.keysList = []
    },
    addKV () {
      this.keysList.push({})
      this.$forceUpdate()
    },
    deleteKV (index, list) {
      list.splice(index, 1)
      this.$forceUpdate()
    },
    changeValidate () {
      this.rowData.value = {}
      this.keysList.filter(it => {
        if(it.key && it.value) {
          this.$set(this.rowData.value, it.key, it.value)
        }
      })
      if(this.$refs.myform) {
        if(!this.rowData.value || JSON.stringify(this.rowData.value) == '{}') {
          this.$set(this.rowData, 'value', null)
          this.$refs.myform.$refs.myform.validateField('value')
        }else{
          this.$refs.myform.$refs.myform.clearValidate('value')
        }
      }
    },
    uploadChange (data) {
      if(this.rowData.type == 'file' && this.$refs.myform) {
        if(this.rowData.value && this.rowData.value.length > 0) {
          this.$refs.myform.$refs.myform.clearValidate('value')
        }else{
          this.$refs.myform.$refs.myform.validateField('value')
        }
      }
    },
    getDynamicDesign () {
      getDynamicDesign().then(res => {
        if(res.data && res.data.code == 0) {
          this.jvsDesign = res.data.data
        }
      })
    },
    searchTypeChange (type) {
      if(type != this.currentType) {
        this.page.currentPage = 1
        this.currentType = type
        this.getList()
      }
    }
  }
}
</script>
<style lang="scss" scoped>
.oprationlog-manage.change-mode-type{
  width: 100%;
  height: 100%;
  display: flex;
  /deep/.jvs-table{
    flex: 1;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    .jvs-table-top{
      height: unset;
    }
    .table-body-box{
      flex: 1;
      overflow: hidden;
    }
  }
  .mode-type-list{
    margin-right: 20px;
    width: 200px;
    height: 100%;
    overflow: hidden;
    overflow-y: auto;
    box-sizing: border-box;
    .mode-type-list-item{
      padding: 0 10px;
      height: 34px;
      line-height: 34px;
      font-family: Microsoft YaHei-Regular, Microsoft YaHei;
      font-weight: 400;
      font-size: 14px;
      color: #6F7588;
      cursor: pointer;
      position: relative;
      word-break: keep-all;
      &+.extend-tab-item{
        margin-top: 10px;
      }
      &.active{
        font-weight: 700;
        color: #606266;
        background: #EFF2F7;
      }
      &:hover{
        background: #EFF2F7;
      }
    }
  }
}
.form-dialog-box{
  width: 624px;
  display: flex;
  .left{
    padding: 16px 16px 16px 24px;
    width: 240px;
    background: #EDF4FF;
    box-sizing: border-box;
    .title{
      display: flex;
      align-items: flex-start;
      i{
        line-height: 18px;
        font-size: 16px;
        color: #1E6FFF;
        margin-right: 2px;
      }
      span{
        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
        font-weight: 400;
        font-size: 14px;
        color: #1E6FFF;
        line-height: 18px;
        word-break: break-word;
      }
    }
    .left-tip-item{
      margin-top: 12px;
      .tip-title{
        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
        font-weight: 400;
        font-size: 12px;
        color: #363B4C;
        line-height: 16px;
      }
      .tip-desc{
        margin-top: 4px;
        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
        font-weight: 400;
        font-size: 12px;
        color: #6F7588;
        line-height: 20px;
        word-break: break-word;
      }
    }
  }
  .right{
    flex: 1;
    margin-left: 24px;
    margin-right: 32px;
    padding-top: 24px;
    overflow: hidden;
    .key-map-list{
      .list{
        &.needpadding{
          margin-bottom: 10px;
        }
        .key-map-item{
          display: flex;
          align-items: center;
          /deep/.el-input{
            &+.el-input{
              margin-left: 10px;
            }
          }
          .el-icon-delete{
            margin-left: 10px;
            cursor: pointer;
          }
          &+.key-map-item{
            margin-top: 10px;
          }
        }
      }
    }
    /deep/.jvs-form{
      .form-item-btn{
        display: none;
      }
    }
  }
}
.foot-button{
  width: 100%;
  height: 60px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  border-top: 1px solid #EEEFF0;
  box-sizing: border-box;
}
</style>