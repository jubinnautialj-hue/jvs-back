<template>
  <el-form
    :model="formDatas"
    :ref="refs || defalutSet.refs"
    :option="option || defalutSet.option"
    :class="{'jvs-form': true, 'jvs-form-autoflexable': (option.labelWidth == 'auto' || option.labelwidth == 'auto' || option.formAuto ), 'jvs-form-transparent': option.useElStyle}"
    :size="$store.state.params.form.size || option.size || option.formsize || 'mini'"
    :inline="option.inline || defalutSet.option.inline"
    :label-position="option.formAlign || defalutSet.option.formAlign"
    :label-width="option.labelWidth || defalutSet.option.labelWidth"
    :disabled='option.disabled'
    :validateOnRuleChange="false"
    @submit.native.prevent
  >
    <slot name="formTop"></slot>
    <el-row v-if="!formLoading" style="width: 100%">
      <!-- 搜索条件组 -->
      <el-col
        v-for="item in option.column"
        :key="['childrenForm', 'connectForm'].indexOf(item.type) > -1 ? ('children-form' + item.prop) : item.prop"
        :span="isSearch==true?(item.searchSpan || option.searchSpan || 24):(item.span || option.span || 24)"
        v-show="item.display == false ? item.display : true"
        :class="[item.type && ('form-column-'+item.type), item.hideLabel && 'no-label-form-item']"
        v-if="displayExpressHandle(item)"
      >
        <el-form-item
          :class='{
            "form-item-no-label": ( (!item.label && item.type != "tab") || (["tableForm","divider","p","section"].indexOf(item.type) > -1) ),
            "form-item-no-label-tab": (!item.label && item.type == "tab"),
            "reportTable-item": item.type == "reportTable",
            "before-append-item": item.beforeSlot,
            "form-item-no-label-nopadding": (!item.label && ["childrenForm","connectForm"].indexOf(item.type) > -1),
            "slot-label-item": item.labelSlot
          }'
          :label='item.type == "tableForm" ? (item.editable ? item.label : "") : item.label'
          :prop="item.prop"
          v-if="(item.prop !== freshDom) && (item.type !='title' && (!item.children || item.children.length == 0) || item.type == 'formbox') && (item.permisionFlag ? permissionsList.indexOf(item.permisionFlag) > -1 : true) && (item.display == false ? item.display : true)"
          :rules="requireExpressHandle(item.rules, item)"
        >
          <span v-if="item.labelSlot" slot="label" class="slot-label">
            <slot :name="item.prop+'Label'"></slot>
          </span>
          <span v-if="item.beforeSlot" class="before-append-content">
            <slot :name="item.prop+'Before'"></slot>
          </span>
          <FormItem
            v-if="!item.formSlot && !item.appendSlot"
            :form="formDatas"
            :item="{...item, disabledControl: disabledExpressHandle(item, 'formDatas')}"
            :originOption="option"
            :defalutSet="defalutSet"
            @formChange="formChange"
            :roleOption="roleOption"
            :userList="userList"
            :departmentList="departmentList"
            :postList="postList"
            :rowData="rowData"
            :resetRadom="resetRadom"
            :designId="designId"
            :dataModelId="dataModelId"
            :changeRandom="changeRandom"
            :changeDomItem="changeDomItem"
            :isView="isView"
            :execsList="execsList"
            :jvsAppId="jvsAppId"
            :dataTriggerFresh="dataTriggerFresh"
            :ruleChange="ruleChange"
            :openByButton="openByButton"
            @validateHandle="validateHandle"
            @reInitData="reInitData"
          />
          <!-- 自定义列插槽 -->
          <div v-if="item.formSlot">
            <slot :name="item.prop+'Form'"></slot>
            <!-- 右侧提示 -->
            <el-tooltip
              v-if="item.tipsEnable !== false && item.tips && item.tips.position == 'right' && item.tips.text"
              class="form-item-tooltip"
              effect="dark"
              :content="item.tips.text"
              placement="top"
            >
              <i class="el-icon-question"></i>
            </el-tooltip>
          </div>
          <!-- 后置插槽 -->
          <div v-if="item.appendSlot">
            <span class="form-append">
              <FormItem
                v-if="!item.formSlot"
                :form="formDatas"
                :item="{...item, disabledControl: disabledExpressHandle(item, 'formDatas')}"
                :originOption="option"
                :defalutSet="defalutSet"
                @formChange="formChange"
                :roleOption="roleOption"
                :userList="userList"
                :departmentList="departmentList"
                :postList="postList"
                :rowData="rowData"
                :resetRadom="resetRadom"
                :designId="designId"
                :dataModelId="dataModelId"
                :changeRandom="changeRandom"
                :changeDomItem="changeDomItem"
                :isView="isView"
                :execsList="execsList"
                :jvsAppId="jvsAppId"
                :dataTriggerFresh="dataTriggerFresh"
                :ruleChange="ruleChange"
                :openByButton="openByButton"
                @validateHandle="validateHandle"
                @reInitData="reInitData"
              >
                <slot :name="item.prop+'Append'" :slot="item.prop+'AppendItem'"></slot>
              </FormItem>
            </span>
          </div>
          <!-- 换行提示 -->
          <el-row
            v-if="item.tipsEnable !== false && item.tips && item.tips.position == 'bottom'"
            class="form-item-tips"
          >
            <span v-html="item.tips.text"></span>
          </el-row>
        </el-form-item>
        <h5 v-if="item.type == 'title'">{{item.label}}</h5>
        <!-- 子表单项 -->
        <el-row v-if="item.type != 'formbox' && item.children && item.children.length > 0 && displayExpressHandle(item)" style="display:flex;flex-wrap:wrap;">
          <el-form-item
            :label="item.label"
            :prop="item.prop"
            :rules="requireExpressHandle(item.rules, item)"
            v-if="(item.prop !== freshDom) && (item.display == false ? item.display : true)"
            :class='(!item.label || (["tableForm","divider","p","tab","section"].indexOf(item.type) > -1))? "form-item-no-label" : ""'
            style="width:100%;"
          >
            <FormItem
              v-if="!item.formSlot"
              :form="formDatas"
              :item="{...item, disabledControl: disabledExpressHandle(item, 'formDatas')}"
              :originOption="option"
              :defalutSet="defalutSet"
              @formChange="formChange"
              :roleOption="roleOption"
              :userList="userList"
              :departmentList="departmentList"
              :postList="postList"
              :rowData="rowData"
              :resetRadom="resetRadom"
              :designId="designId"
              :dataModelId="dataModelId"
              :changeRandom="changeRandom"
              :changeDomItem="changeDomItem"
              :isView="isView"
              :execsList="execsList"
              :jvsAppId="jvsAppId"
              :dataTriggerFresh="dataTriggerFresh"
              :ruleChange="ruleChange"
              :openByButton="openByButton"
              @validateHandle="validateHandle"
              @reInitData="reInitData"
            >
              <!-- 后置插槽 -->
              <span v-if="item.appendSlot">
                <slot :name="item.prop+'Append'" :slot="item.prop+'AppendItem'"></slot>
              </span>
            </FormItem>
            <!-- 自定义列插槽 -->
            <div v-if="item.formSlot">
              <slot :name="item.prop+'Form'"></slot>
              <!-- 右侧提示 -->
              <el-tooltip
                v-if="item.tipsEnable !== false && item.tips && item.tips.position == 'right' && item.tips.text"
                class="form-item-tooltip"
                effect="dark"
                :content="item.tips.text"
                placement="top"
              >
                <i class="el-icon-question"></i>
              </el-tooltip>
            </div>
            <!-- 换行提示 -->
            <el-row
              v-if="item.tipsEnable !== false && item.tips && item.tips.position == 'bottom'"
              class="form-item-tips"
            >
              <span v-html="item.tips.text"></span>
            </el-row>
          </el-form-item>
          <el-col
            v-for="it in item.children"
            :key="it.prop"
            :span="isSearch==true?(it.searchSpan || option.searchSpan || 24):(it.span || option.span || 24)"
            v-if="linkbindHandle(formDatas[item.prop], it.linkbind) && displayExpressHandle(it)"
          >
            <el-form-item
              :label="it.label"
              :prop="it.prop"
              v-if="(it.prop !== freshDom) && (it.permisionFlag ? permissionsList.indexOf(it.permisionFlag) > -1 : true) && (!option.isSearch || (option.isSearch && it.search == true)) && (it.display == false ? it.display : true)"
              :rules="requireExpressHandle(it.rules, it)"
              :class='{
                "form-item-no-label": (!it.label || (["tableForm","divider","p","tab","section"].indexOf(it.type) > -1)),
                "reportTable-item": it.type == "reportTable"
              }'
            >
              <FormItem
                v-if="!it.formSlot"
                :form="formDatas"
                :item="{...it, disabledControl: disabledExpressHandle(it, 'formDatas')}"
                :formRef="refs || defalutSet.refs"
                :originOption="option"
                :defalutSet="defalutSet"
                @formChange="formChange"
                :roleOption="roleOption"
                :userList="userList"
                :departmentList="departmentList"
                :postList="postList"
                :rowData="rowData"
                :resetRadom="resetRadom"
                :designId="designId"
                :dataModelId="dataModelId"
                :changeRandom="changeRandom"
                :changeDomItem="changeDomItem"
                :isView="isView"
                :execsList="execsList"
                :jvsAppId="jvsAppId"
                :dataTriggerFresh="dataTriggerFresh"
                :ruleChange="ruleChange"
                :openByButton="openByButton"
                @validateHandle="validateHandle"
                @reInitData="reInitData"
              >
                <!-- 后置插槽 -->
                <span v-if="it.appendSlot">
                  <slot :name="it.prop+'Append'" :slot="it.prop+'AppendItem'"></slot>
                </span>
              </FormItem>
              <!-- 自定义列插槽 -->
              <div v-if="it.formSlot">
                <slot :name="it.prop+'Form'"></slot>
                <!-- 右侧提示 -->
                <el-tooltip
                  v-if="it.tipsEnable !== false && it.tips && it.tips.position == 'right' && it.tips.text"
                  class="form-item-tooltip"
                  effect="dark"
                  :content="it.tips.text"
                  placement="top"
                >
                  <i class="el-icon-question"></i>
                </el-tooltip>
              </div>
              <!-- 换行提示 -->
              <el-row
                v-if="it.tipsEnable !== false && it.tips && it.tips.position == 'bottom'"
                class="form-item-tips"
              >
                <span v-html="it.tips.text"></span>
              </el-row>
            </el-form-item>
          </el-col>
        </el-row>
      </el-col>
      <!-- 按钮组 -->
      <el-col
        :span="option.isSearch ? 6 :  24"
        v-if="option.column.length > 0 && option.btnHide!==true"
        class="form-item-btn">
        <el-form-item class="form-btn-bar">
          <template v-if="isSearch">
            <el-button
              size="small"
              v-if="!(option.searchBtn == false)"
              type="primary"
              icon="el-icon-search"
              @click="submitForm(refs || defalutSet.refs)"
              :loading="option.submitLoading"
            >{{option.searchBtnText || defalutSet.option.searchBtnText}}</el-button>
            <el-button
              size="small"
              v-if="!(option.emptyBtn == false)"
              @click="resetForm(refs || defalutSet.refs)"
            >{{option.emptyBtnText || defalutSet.option.emptyBtnText}}</el-button>
          </template>
          <template v-if="!isSearch">
            <el-button
              size="small"
              v-if="!(option.submitBtn == false)"
              type="primary"
              @click="submitForm(refs || defalutSet.refs)"
              :loading="option.submitLoading"
            >{{option.submitBtnText || defalutSet.option.submitBtnText}}</el-button>
            <el-button
              size="mini"
              v-if="!(option.emptyBtn == false)"
              @click="resetForm(refs || defalutSet.refs)"
            >{{option.emptyBtnText || defalutSet.option.emptyBtnText}}</el-button>
            <el-button
              size="small"
              v-if="!(option.cancal == false)"
              @click="$emit('cancalClick')"
            >{{option.cancalBtnText || defalutSet.option.cancalBtnText}}</el-button>
          </template>
          <slot name="formButton"></slot>
        </el-form-item>
      </el-col>
    </el-row>
    <div v-if="formLoading" class="jvs-form-loading-modal"></div>
  </el-form>
</template>

<script>
import FormItem from './formitem'
import {getDeptList, getRoleList, getPostList} from '../api'
import {doExec} from '@/components/basic-container/formula/api'
import {dataModelTriggering} from '@/components/api'
import { getStore } from "@/util/store.js"
export default {
  name: "jvsForm",
  components: { FormItem },
  props: {
    // 是否为搜索表单
    isSearch: {
      type: Boolean,
      default: false
    },
    // 表单绑定
    refs: {
      type: String,
      default: 'ruleForm'
    },
    // 表单对象
    formData: {
      type: Object,
      default: () => {
        return {}
      }
    },
    // 表单数据
    defalutFormData: {
      type: Object,
      default: () => {
        return {}
      }
    },
    // 表单设置
    option: {
      type: Object,
      default: () => {
        return {
          formAlign: 'right', //对其方式
          inline: false, // 表单项是否可以同行,当垂直方向空间受限且表单较简单时，可以在一行内放置表单
          labelWidth: 'auto', // label宽
          searchBtn: true,
          searchBtnText: '',
          submitBtn: true, // 提交按钮是否显示，默认显示
          submitBtnText: '保存', // 提交按钮文字，默认 提交
          emptyBtn: true, // 重置按钮，默认显示
          emptyBtnText: '', // 重置按钮文字，默认 重置
          isSearch: false, // 是否为搜索表单
          cancal: true, // 取消按钮， 默认显示
          cancalBtnText: '取消',
          column: [ // 字段
            {
              label: '', // 文字
              prop: '', // 字段
              type: '', // 文本类型，默认input
              dicData: [], // 字典数据
              span: 24, // 表单项栅格比，默认24
              formSlot: false, // 是否自定义
              permisionFlag: '', // 权限标识
              rules: [], // 校验规则
              linkbind: '', // 父级联动控制对应的表单值
            }
          ],
        }
      }
    },
    rowData: {
      type: Object
    },
    designId: {
      type: String
    },
    dataModelId: {
      type: String
    },
    isView: {
      type: Boolean
    },
    execsList: {
      type: Array
    },
    jvsAppId:  {
      type: String
    },
    associationSettingsFields: {
      type: Array
    },
    initNotRequest: {
      type: Boolean
    },
    openByButton: {
      type: Object
    },
    notLoading: {
      type: Boolean
    },
  },
  computed: {
    // formDatas: {
    //   get: function () {
    //     return this.formData
    //   },
    //   set: function () { }
    // }
  },
  data () {
    return {
      formDatas: {}, // 表单对象
      defalutSet: {
        refs: 'ruleForm',
        option: {
          formAlign: 'right', //对其方式
          inline: false, // 表单项是否可以同行,当垂直方向空间受限且表单较简单时，可以在一行内放置表单
          labelWidth: 'auto', // label宽
          searchBtn: true, // 搜索按钮是否显示，默认显示
          searchBtnText: this.$langt('form.search'), // 搜索按钮，默认查询
          submitBtn: true, // 提交按钮是否显示，默认显示
          submitBtnText: this.$langt('form.submit'), // 提交按钮文字，默认 提交
          emptyBtn: true, // 重置按钮，默认显示
          emptyBtnText: this.$langt('form.reset'), // 重置按钮文字，默认 重置
          cancalBtnText: this.$langt('form.cancel'), // 取消按钮文字， 默认 取消
        }
      },
      roleOption: [], // 角色列表
      userList: [], // 用户列表
      departmentList: [], // 部门列表
      postList: [], // 岗位列表
      clearAll: false, // 重置是否为初始对象{}
      resetRadom: -1, // 通知子项重置随机数 -1不重置
      resetData: "", // 原始数据，用于重置
      freshDom: '',
      changeRandom: -1,
      changeDomItem: null,
      deptBool: false,
      roleBool: false,
      postBool: false,
      userBool: false,
      dataLinkageBool: false,
      formulaBool: false,
      parentDom: null,
      dataTriggerFresh: -1,
      permissionsList: [],
      formLoading: false,
      ruleChange: -1,
    }
  },
  async created () {
    if (this.notLoading !== true) {
      this.formLoading = true
    }
    this.permissionsList = getStore({name: 'permissions'})
    this.resetData = JSON.stringify(this.formData)
    this.formDatas = this.formData
    if (this.defalutFormData) {
      for (let i in this.defalutFormData) {
        this.formDatas[i] = this.defalutFormData[i]
      }
    }
    if(JSON.stringify(this.formDatas) == '{}') {
      this.clearAll = true
    }
    await this.getConst()
    if(!this.isView && !this.initNotRequest && (this.dataLinkageBool || this.formulaBool)) {
      this.dataInitHandle(null, null, null, 'init')
    }else{
      this.formLoading = false
    }
  },
  methods: {
    validateForm () {
      let validBool = false
      let formName = this.refs || this.defalutSet.refs
      this.$refs[formName].validate((valid, obj) => {
        if(valid) {
          validBool = true
        }else{
          // 二次校验
          let submit = true
          Object.keys(obj).forEach(item => {
            if (this.formDatas[item] === undefined || this.formDatas[item] === null || this.formDatas[item].length === 0) {
              submit = false
            } else {
              let comType = ''
              let itemCom = null
              this.option.column.filter(comItem => {
                if(comItem.prop == item) {
                  comType = comItem.type
                  itemCom = JSON.parse(JSON.stringify(comItem))
                }
              })
              if(['user', 'role', 'department', 'group', 'job'].indexOf(comType) > -1 || (comType == 'cascader' && itemCom && itemCom.pickType == 'tree')){
                this.$refs[formName].clearValidate(item)
              }else{
                submit = false
              }
            }
          })
          if(!submit) {
            console.log('error submit!!')
            validBool = false
          }else{
            validBool = true
          }
        }
      })
      return validBool
    },
    submitForm (formName) {
      // this.$refs[formName].validate((valid) => {
      //   if (valid) {
      //     this.$emit('submit', this.formDatas)
      //   } else {
      //     console.log('error submit!!')
      //     return false;
      //   }
      // });
      this.$refs[formName].validate((valid, obj) => {
        if (valid) {
          this.$emit('submit', this.formDatas)
        } else {
          // 二次校验
          let submit = true
          Object.keys(obj).forEach(item => {
            if (this.formDatas[item] === undefined || this.formDatas[item] === null || this.formDatas[item].length === 0) {
              submit = false
            } else {
              let comType = ''
              let itemCom = null
              this.option.column.filter(comItem => {
                if(comItem.prop == item) {
                  comType = comItem.type
                  itemCom = JSON.parse(JSON.stringify(comItem))
                }
              })
              if(['user', 'role', 'department', 'group', 'job'].indexOf(comType) > -1 || (comType == 'cascader' && itemCom && itemCom.pickType == 'tree')){
                this.$refs[formName].clearValidate(item)
              }else{
                submit = false
              }
            }
          })
          if (!submit) {
            console.log('error submit!!')
            return false;
          }
          this.$emit('submit', this.formDatas)
        }
      });
    },
    resetForm (formName) {
      if (this.option.isSearch === true) {
        this.resetRadom = Math.random()
        this.formDatas = {}
        this.submitForm(formName)
      } else {
        if(this.clearAll) {
          this.formDatas = {}
        }else{
          this.$refs[formName].resetFields()
        }
        this.resetRadom = Math.random()
        if(this.resetData.startsWith('{')) {
          this.$set(this, 'formDatas', JSON.parse(this.resetData))
        }
      }
      this.$emit('reset', formName)
    },
    formChange (form, item, beforeValue) {
      this.$emit('formChange', form, item, beforeValue)
      // 仅 第一层级文本数字组件判断失焦后的值，改变才触发change
      if(item && form[item.prop] != beforeValue) {
        this.$set(this.formData, item.prop, form[item.prop]) // 重置后需要赋值
        this.changeRandom = Math.random()
        this.changeDomItem = item
      }else{
        this.ruleChange = Math.random()
      }
    },
    async getConst () {
      // 优化：表单内无对应公共组件不发请求
      this.eachDomTree(this.option.column)
      if(this.deptBool) { // 默认数据填充的部门只有id，需要查询
        await getDeptList().then(res => {
          if(res.data.code == 0) {
            this.departmentList = res.data.data
            this.eachDeptData(this.departmentList)
          }
        })
      }
      if(this.roleBool) {
        await getRoleList().then(res => {
          if(res.data.code == 0) {
            this.roleOption = res.data.data
          }
        })
      }
      if(this.postBool) {
        await getPostList().then(res => {
          if(res.data.code == 0) {
            this.postList = res.data.data
          }
        })
      }
    },
    // 联动控制
    linkbindHandle (val, bind) {
      let bool = true
      if(bind && ['boolean', 'number'].indexOf(typeof bind) == -1) {
        let arr = (bind instanceof Array) ? bind : bind.split(',')
        if(val instanceof Array) {
          let tb = false
          for(let i in val) {
            if(arr.indexOf(val[i]) > -1 || arr.indexOf((val[i] + '')) > -1) {
              tb = true
            }
          }
          bool = tb
        }else{
          if(arr.indexOf(val) > -1 || arr.indexOf((val + '')) > -1) {
            bool = true
          }else{
            bool = false
          }
        }
      }else{
        if(bind || bind === 0 || bind === false) {
          if(val || val === 0 || val === false) {
            if(bind !== val) {
              bool = false
            }
          }else{
            bool = true
          }
        }else{
          bool = true
        }
      }
      return bool
    },
    validateHandle (data) {
      let type = data.type
      let item = data.item
      if(['user', 'role', 'department', 'group', 'job', 'htmlEditor', 'cascader', 'imageUpload', 'fileUpload', 'inputNumber', 'htmlEditor'].indexOf(item.type) === -1 && !item.searchable) {
        this.freshDom = item.prop
      }
      this.$nextTick( () => {
        if(type == 'clear') {
          this.$refs[this.refs || this.defalutSet.refs].clearValidate(item.prop)
        }else{
          this.$refs[this.refs || this.defalutSet.refs].validateField(item.prop)
        }
        this.freshDom = ''
      })
      this.$forceUpdate()
    },
    // 表达式控制显示
    displayExpressHandle (item) {
      let bool = false
      let formStr = 'this.formDatas' // 表单值参数名
      if(item.displayExpress && item.displayExpress.length > 0) {
        let list = item.displayExpress
        let temp = []
        let needValidateSpecial = false // 是否单独校验tab项的值
        for(let i in list) {
          let prop = (formStr + '.') // 控制字段名
          if(list[i].parent && list[i].parent.length > 0) {
            let pdomList = []
            this.findParentList(this.option.column, list[i].parent.join('.'), pdomList)
            if(pdomList.length > 0 && pdomList[0].type == 'tab' && pdomList[0].detachData) {
              if(pdomList.length >  1) {
                if(pdomList[1].prop) {
                  prop += (pdomList[1].prop + '.')
                  needValidateSpecial = true
                }
              }
            }else{
              prop += list[i].parent.join('.')
              prop += '.'
            }
          }
          prop += list[i].prop
          // 校验层级表单值是否为undefined
          let tpr = ''
          let exValidate = true
          // 存在父级且需要父级数据
          if(prop.split('.').length > 3) {
            if(needValidateSpecial) {
              let pt = []
              let pl = prop.split('.')
              pt = pl.slice(2, pl.length-1)
              tpr += formStr
              for(let p in pt) {
                tpr += ('.' + pt[p])
                if(eval(tpr) == undefined) {
                  exValidate = false
                  break;
                }
              }
            }else{
              tpr += formStr
              for(let p in list[i].parent) {
                tpr += ('.' + list[i].parent[p])
                if(eval(tpr) == undefined) {
                  exValidate = false
                  break;
                }
              }
            }
          }
          if(exValidate) {
            let tv = JSON.stringify(list[i].value.split(','))
            let isArrayData = false
            try {
              let pda = eval(prop)
              if(pda && typeof pda == 'object' && pda instanceof Array) {
                isArrayData = true
                let isIn = false
                for(let pix in pda) {
                  if(tv.indexOf(pda[pix]+'') > -1) {
                    isIn = true
                  }
                }
                temp.push(isIn)
              }
            } catch (error) {
              console.log(error)
            }
            if(!isArrayData) {
              tv += '.indexOf( '
              let tp = (tv + prop + ' + ' + "''" + ')')
              tp += (' > -1')
              temp.push(tp)
            }
          }
        }
        if(temp.length > 0) {
          if(eval(temp.join(` ${item.showOperator || '||'} `))) {
            bool = true
          }
        }
      }else{
        bool = true
      }
      return bool
    },
    // 联动或公式初始化
    dataInitHandle (prop, parentKey, index, optype, tableType) {
      if(this.designId) {
        let hasDataTrigger = false // 是否执行联动
        if(prop) {
          let tp = {
            prop: prop,
          }
          if(parentKey) {
            tp.parentKey = parentKey
          }
          let tlist = this.dataTriggerEnableDom(tp) // 同级的联动组件
          tlist.filter(tit => {
            if(tit.dataLinkageList && tit.dataLinkageEnable !== false) {
              tit.dataLinkageList.filter(tid => {
                // 当前组件作为同级中的联动条件
                if(tid.value == prop) {
                  hasDataTrigger = true
                }
              })
            }
          })
        }
        if(optype == 'init' || tableType) {
          hasDataTrigger = true
        }
        let formTemp = {}
        for(let k in this.formDatas) {
          if(!(k.endsWith('_1')) && ((this.associationSettingsFields && this.associationSettingsFields.length > 0) ? (this.associationSettingsFields.indexOf(k) > -1) : true)) {
            this.$set(formTemp, k, this.formDatas[k])
          }
        }
        if(this.dataModelId && hasDataTrigger) {
          let triobj = {
            params: formTemp
          }
          if(prop) {
            triobj.modifiedField = prop
          }
          if(parentKey) {
            triobj.parentKey = parentKey.split('.')
          }
          if(index > -1) {
            triobj.index = index
          }
          dataModelTriggering(this.jvsAppId, this.designId, this.dataModelId, triobj, optype == 'init' ? {init: true} : (tableType ? {tableType: tableType} : null) ).then(res => {
            if(res.data && res.data.code == 0) {
              if(res.data.data) {
                // console.log('联动。。。。', res.data.data)
                for(let i in res.data.data) {
                  this.$set(this.formDatas, i , res.data.data[i])
                }
                this.dataTriggerFresh = Math.random()
                let formTempExec = {}
                for(let k in this.formDatas) {
                  if(!(k.endsWith('_1')) && ((this.associationSettingsFields && this.associationSettingsFields.length > 0) ? (this.associationSettingsFields.indexOf(k) > -1) : true)) {
                    this.$set(formTempExec, k, this.formDatas[k])
                  }
                }
                // 公式
                let obp = {
                  params: formTempExec
                }
                if(prop) {
                  obp.modifiedField = prop
                }
                let tempParentKey = ''
                if(parentKey) {
                  tempParentKey = parentKey
                  this.findParentDom(this.option.column, parentKey, 'each')
                  if(this.parentDom) {
                    if(this.parentDom.parentDetachDataProp) {
                      if(this.parentDom.parentDetachDataProp == 'thisIsEmptyStringValue') {
                        if(this.parentDom.detachData) {
                          tempParentKey = ''
                        }else{
                          tempParentKey = `${this.parentDom.prop}`
                        }
                      }else{
                        if(this.parentDom.detachData) {
                          tempParentKey = `${this.parentDom.parentDetachDataProp}`
                        }else{
                          tempParentKey = `${this.parentDom.parentDetachDataProp}.${this.parentDom.prop}`
                        }
                      }
                    }
                    if(this.parentDom.type == 'tableForm') {
                      if(formTempExec[this.parentDom.prop] && formTempExec[this.parentDom.prop].length == 1 && formTemp[this.parentDom.prop] && formTemp[this.parentDom.prop].length > 1) {
                        let newObj = JSON.parse(JSON.stringify(formTempExec[this.parentDom.prop][0]))
                        this.$set(formTempExec, this.parentDom.prop, formTemp[this.parentDom.prop])
                        this.$set(formTempExec[this.parentDom.prop], index, newObj)
                      }
                    }
                  }
                  obp.parentKey = tempParentKey.split('.')
                }
                if(index > -1) {
                  obp.index = index
                }
                if(optype == 'init' || (this.execsList && this.execsList.indexOf(tempParentKey ? tempParentKey+'.'+prop : prop) > -1)) {
                  doExec('jvs-design', this.designId, 'formItemValue', JSON.parse(JSON.stringify(obp)), optype == 'init' ? {init: true} : (tableType ? {tableType: tableType} : null) ).then(res => {
                    if(res.data && res.data.code == 0) {
                      if(res.data.data) {
                        // console.log('公式。。。。', res.data.data)
                        for(let i in res.data.data) {
                          this.$set(this.formDatas, i , res.data.data[i])
                        }
                        this.dataTriggerFresh = Math.random()
                        if(optype == 'init') {
                          this.formLoading = false
                        }
                      }
                    }
                  })
                }
              }
            }
          })
        }else{
          let obp = {
            params: formTemp
          }
          if(prop) {
            obp.modifiedField = prop
          }
          let tempParentKey = ''
          if(parentKey) {
            tempParentKey = parentKey
            this.findParentDom(this.option.column, parentKey, 'each')
            if(this.parentDom) {
              if(this.parentDom.parentDetachDataProp) {
                if(this.parentDom.parentDetachDataProp == 'thisIsEmptyStringValue') {
                  if(this.parentDom.detachData) {
                    tempParentKey = ''
                  }else{
                    tempParentKey = `${this.parentDom.prop}`
                  }
                }else{
                  if(this.parentDom.detachData) {
                    tempParentKey = `${this.parentDom.parentDetachDataProp}`
                  }else{
                    tempParentKey = `${this.parentDom.parentDetachDataProp}.${this.parentDom.prop}`
                  }
                }
              }
            }
            obp.parentKey = tempParentKey.split('.')
          }
          if(index > -1) {
            obp.index = index
          }
          if(optype == 'init' || (this.execsList && this.execsList.indexOf(tempParentKey ? tempParentKey+'.'+prop : prop) > -1)) {
            doExec('jvs-design', this.designId, 'formItemValue', JSON.parse(JSON.stringify(obp)), (tableType ? {tableType: tableType} : null)).then(res => {
              if(res.data && res.data.code == 0) {
                if(res.data.data) {
                  for(let i in res.data.data) {
                    this.$set(this.formDatas, i , res.data.data[i])
                  }
                  this.dataTriggerFresh = Math.random()
                  if(optype == 'init') {
                    this.formLoading = false
                  }
                }
              }
            })
          }
        }
      }else{
        this.formLoading = false
      }
    },
    reInitData (prop, parentKey, index, tableType) {
      if(prop) {
        this.dataInitHandle(prop, parentKey, index, null, tableType)
        this.changeRandom = Math.random()
        this.changeDomItem = {prop: prop, parentKey: parentKey}
      }
      if(prop == '' && parentKey == '') {
        this.dataInitHandle('', '')
      }
    },
    eachDomTree(list, parentDom, parentKey, parentType) {
      for(let i in list) {
        // 加入自定义校验
        if(list[i].regularExpression) {
          let required = false
          if(list[i].rules && list[i].rules.length > 0 && list[i].rules[0].required) {
            required = true
          }
          if(list[i].rules && list[i].rules.length < 2) {
            let str = '/' + list[i].regularExpression + '/'
            list[i].rules.push({
              validator: function(rule, value, callback) {
                if(eval(str).test(value)) {
                  callback()
                }else{
                  let msg = '正则校验不通过'
                  if(list[i].regularMessage) {
                    msg = list[i].regularMessage
                  }
                  if(required == false && !value) {
                    callback()
                  }else{
                    callback(new Error(msg));
                  }
                }
              },
              trigger: ['blur', 'change']
            })
          }
        }
        if(['department'].indexOf(list[i].type) > -1) {
          this.deptBool = true
        }
        if(['role', 'flowNode'].indexOf(list[i].type) > -1) {
          this.roleBool = true
        }
        if(['job', 'flowNode'].indexOf(list[i].type) > -1) {
          this.postBool = true
        }
        if(['user'].indexOf(list[i].type) > -1) {
          this.userBool = true
        }
        if(list[i].dataLinkageEnable !== false && list[i].dataLinkageModelId) {
          this.dataLinkageBool = true
        }
        if(list[i].formula) {
          this.formulaBool = true
        }
        if(parentDom && parentDom.length > 0) {
          list[i].parentDom = parentDom
        }
        if(list[i].rules && list[i].rules.length > 0 && (!list[i].disabled && !list[i].searchable && ['imageUpload', 'fileUpload'].indexOf(list[i].type) == -1)) {
          this.$set(list[i].rules[0], 'trigger', ['change', 'blur'])
        }
        if(!list[i].parentDom && ['tab', 'tableForm', 'pageTable'].indexOf(list[i].type) == -1 && list[i].display === false) {
          let mul = false
          if(['checkbox'].indexOf(list[i].type) > -1 || list[i].multiple) {
            mul = true
          }
          if(list[i].defaultValue || list[i].defaultValue === false || list[i].defaultValue === "" || list[i].defaultValue === 0) {
            if(this.formDatas[list[i].prop] === null || this.formDatas[list[i].prop] === undefined) {
              this.$set(this.formDatas, list[i].prop, mul ? list[i].defaultValue.split(',') : list[i].defaultValue)
            }
          }
        }
        if(!list[i].parentKey && !list[i].parentType && parentKey && parentType) {
          list[i].parentKey = parentKey
          list[i].parentType = parentType
        }
        if(['tab', 'step'].indexOf(list[i].type) > -1) {
          for(let c in list[i].column) {
            let tl = [
              {
                prop: list[i].prop,
                label: list[i].label,
                type: list[i].type,
                detachData: list[i].detachData || false
              }
            ]
            list[i].dicData.filter(ddt => {
              if(ddt.name == c) {
                tl.push(ddt)
              }
            })
            this.eachDomTree(list[i].column[c], tl, list[i].parentKey ? (list[i].parentKey + '.' + list[i].prop) : list[i].prop, list[i].type)
          }
        }
        if(['tableForm', 'reportTable'].indexOf(list[i].type) > -1 && list[i].tableColumn && list[i].tableColumn.length > 0){
          if(list[i].formId) {
            this.dataLinkageBool = true
          }
          this.eachDomTree(list[i].tableColumn, null, list[i].parentKey ? (list[i].parentKey + '.' + list[i].prop) : list[i].prop, list[i].type)
        }
        if(list[i].children && list[i].children.length > 0) {
          this.eachDomTree(list[i].children)
        }
      }
    },
    dataTriggerEnableDom (dom) {
      if(dom.parentKey) {
        let list = []
        this.findParentDom(this.option.column, dom.parentKey)
        if(this.parentDom) {
          let parentType = this.parentDom.type
          switch(parentType) {
            case 'tableForm':
              if(this.parentDom.tableColumn) {
                list = this.parentDom.tableColumn.filter(item => {
                  return (item.dataLinkageList && item.dataLinkageList.length > 0)
                })
              }
            break;
            default: list = this.option.column.filter(item => { return (item.dataLinkageList && item.dataLinkageList.length > 0)});break;
          }
        }else{
          list = this.option.column.filter(item => { return (item.dataLinkageList && item.dataLinkageList.length > 0)})
        }
        return list
      }else{
        return this.option.column.filter(item => {
          return (item.dataLinkageList && item.dataLinkageList.length > 0)
        })
      }
    },
    findParentDom (list, key, eachType, parent) {
      if(list && list.length > 0) {
        list.filter(item => {
          if(key) {
            let pks = key.split('.')
            if(pks && pks.length > 0 && item.prop == pks[pks.length - 1]) {
              if(eachType == 'each' && parent) {
                if(parent.detachData) {
                  let parentDetachDataProp = ''
                  let pk = item.parentKey.split('.')
                  parent.dicData.filter(dit => {
                    if(dit.name == pk[pk.length-1] && dit.prop) {
                      parentDetachDataProp = dit.prop
                    }
                  })
                  let tp = []
                  for(let p in pk) {
                    if(pk[p] == parent.prop) {
                      break
                    }
                    tp.push(pk[p])
                  }
                  if(parentDetachDataProp) {
                    tp.push(parentDetachDataProp)
                  }
                  item.parentDetachDataProp = tp.join('.') || 'thisIsEmptyStringValue'
                }
              }
              this.parentDom = item
            }
          }
          if(['tab', 'step'].indexOf(item.type) > -1) {
            if(item.detachData) {
              let pks = key.split('.')
              let parentDetachDataProp = ''
              item.dicData.filter(dit => {
                if(dit.name == pks[pks.length-1]) {
                  if(dit.prop) {
                    parentDetachDataProp = dit.prop
                  }
                  let tp = []
                  if(item.parentKey) {
                    let pk = item.parentKey.split('.')
                    for(let p in pk) {
                      if(pk[p] == parent.prop) {
                        break
                      }
                      tp.push(pk[p])
                    }
                  }
                  if(parentDetachDataProp) {
                    tp.push(parentDetachDataProp)
                  }
                  item.parentDetachDataProp = tp.join('.') || 'thisIsEmptyStringValue'
                  this.parentDom = item
                }
              })
            }
            for(let j in item.column) {
              if(item.column[j] && item.column[j].length > 0) {
                this.findParentDom(item.column[j], key, eachType, item)
              }
            }
          }
        })
      }
    },
    // 表达式控制禁用
    disabledExpressHandle (item, formDatas) {
      let bool = false
      let formStr = `this.${formDatas}` // 表单值参数名
      if(item.disabledExpress && item.disabledExpress.length > 0) {
        let list = item.disabledExpress
        let temp = []
        let needValidateSpecial = false // 是否单独校验tab项的值
        for(let i in list) {
          let prop = (formStr + '.') // 控制字段名
          if(list[i].parent && list[i].parent.length > 0) {
            let pdomList = []
            this.findParentList(this.option.column, list[i].parent.join('.'), pdomList)
            if(pdomList.length > 0 && pdomList[0].type == 'tab' && pdomList[0].detachData) {
              if(pdomList.length >  1) {
                if(pdomList[1].prop) {
                  prop += (pdomList[1].prop + '.')
                  needValidateSpecial = true
                }
              }
            }else{
              prop += list[i].parent.join('.')
              prop += '.'
            }
          }
          prop += list[i].prop
          // 校验层级表单值是否为undefined
          let tpr = ''
          let exValidate = true
          // 存在父级且需要父级数据
          if(prop.split('.').length > 3) {
            if(needValidateSpecial) {
              let pt = []
              let pl = prop.split('.')
              pt = pl.slice(2, pl.length-1)
              tpr += formStr
              for(let p in pt) {
                tpr += ('.' + pt[p])
                if(eval(tpr) == undefined) {
                  exValidate = false
                  break;
                }
              }
            }else{
              tpr += formStr
              for(let p in list[i].parent) {
                tpr += ('.' + list[i].parent[p])
                if(eval(tpr) == undefined) {
                  exValidate = false
                  break;
                }
              }
            }
          }
          if(exValidate) {
            let tv = JSON.stringify(list[i].value.split(','))
            let isArrayData = false
            try {
              let pda = eval(prop)
              if(pda && typeof pda == 'object' && pda instanceof Array) {
                isArrayData = true
                let isIn = false
                for(let pix in pda) {
                  if(tv.indexOf(pda[pix]+'') > -1) {
                    isIn = true
                  }
                }
                temp.push(isIn)
              }
            } catch (error) {
              console.log(error)
            }
            if(!isArrayData) {
              tv += '.indexOf( '
              let tp = (tv + prop + ' + ' + "''" + ')')
              tp += (' > -1')
              temp.push(tp)
            }
          }
        }
        if(temp.length > 0) {
          if(eval(temp.join(` ${item.showOperator || '||'} `))) {
            bool = true
          }
        }
      }else{
        bool = false
      }
      return bool
    },
    // 表达式控制必填校验
    requireExpressHandle (rules, item) {
      if(item.requireExpress && item.requireExpress.length > 0) {
        let bool = false
        let formStr = `this.formDatas`
        let list = item.requireExpress
        let temp = []
        let needValidateSpecial = false // 是否单独校验tab项的值
        for(let i in list) {
          let prop = (formStr + '.') // 控制字段名
          if(list[i].parent && list[i].parent.length > 0) {
            let pdomList = []
            this.findParentList(this.option.column, list[i].parent.join('.'), pdomList)
            if(pdomList.length > 0 && pdomList[0].type == 'tab' && pdomList[0].detachData) {
              if(pdomList.length >  1) {
                if(pdomList[1].prop) {
                  prop += (pdomList[1].prop + '.')
                  needValidateSpecial = true
                }
              }
            }else{
              prop += list[i].parent.join('.')
              prop += '.'
            }
          }
          prop += list[i].prop
          // 校验层级表单值是否为undefined
          let tpr = ''
          let exValidate = true
          if(prop.split('.').length > 3) {
            if(needValidateSpecial) {
              let pt = []
              let pl = prop.split('.')
              pt = pl.slice(2, pl.length-1)
              tpr += formStr
              for(let p in pt) {
                tpr += ('.' + pt[p])
                if(eval(tpr) == undefined) {
                  exValidate = false
                  break;
                }
              }
            }else{
              tpr += formStr
              for(let p in list[i].parent) {
                tpr += ('.' + list[i].parent[p])
                if(eval(tpr) == undefined) {
                  exValidate = false
                  break;
                }
              }
            }
          }
          if(exValidate) {
            let tv = JSON.stringify(list[i].value.split(','))
            let isArrayData = false
            try {
              let pda = eval(prop)
              if(pda && typeof pda == 'object' && pda instanceof Array) {
                isArrayData = true
                let isIn = false
                for(let pix in pda) {
                  if(tv.indexOf(pda[pix]+'') > -1) {
                    isIn = true
                  }
                }
                temp.push(isIn)
              }
            } catch (error) {
              console.log(error)
            }
            if(!isArrayData) {
              tv += '.indexOf( '
              let tp = (tv + prop + ' + ' + "''" + ')')
              tp += (' > -1')
              temp.push(tp)
            }
          }
        }
        if(temp.length > 0) {
          if(eval(temp.join(` ${item.showOperator || '||'} `))) {
            bool = true
          }
        }
        if(bool) {
          if(rules && rules.length > 0) {
            this.$set(rules[0], 'required', true)
            if(this.$refs[this.refs || this.defalutSet.refs]) {
              this.$refs[this.refs || this.defalutSet.refs].validateField(item.prop)
            }
          }
        }else{
          if(rules && rules.length > 0) {
            this.$set(rules[0], 'required', false)
            if(this.$refs[this.refs || this.defalutSet.refs]) {
              this.$refs[this.refs || this.defalutSet.refs].clearValidate(item.prop)
            }
          }
        }
      }
      // if(rules && rules.length > 0 && (!item.disabled && !item.searchable && ['imageUpload', 'fileUpload'].indexOf(item.type) == -1)) {
      //   this.$set(rules[0], 'trigger', ['change', 'blur'])
      // }
      return rules
    },
    // 获取所有父节点信息
    findParentList (list, key, result) {
      if(list && list.length > 0) {
        list.filter(item => {
          if(key && item.parentKey == key) {
            item.parentDom.filter(ppit => {
              result.push(ppit)
            })
          }
          if(['tab', 'step'].indexOf(item.type) > -1) {
            for(let j in item.column) {
              if(item.column[j] && item.column[j].length > 0) {
                this.findParentList(item.column[j], key, result)
              }
            }
          }
        })
      }
    },
    eachDeptData (list, parent) {
      for(let i in list) {
        if(list[i].parentId && !list[i].parentNameList) {
          list[i].parentNameList = []
        }
        if(parent && parent.parentNameList) {
          list[i].parentNameList = JSON.parse(JSON.stringify(parent.parentNameList))
        }
        list[i].parentNameList.push(list[i].name)
        if(list[i].children && list[i].children.length > 0) {
          this.eachDeptData(list[i].children, list[i])
        }
      }
    }
  }
};
</script>

<style lang="scss">
.jvs-form {
  h5 {
    margin: 0;
    padding: 0;
    margin-bottom: 10px;
  }
  .el-form-item.form-btn-bar {
    // margin: 10px;
    .el-form-item__content {
      text-align: center;
      margin-left: 0 !important;
    }
  }
  .no-label-form-item{
    >.el-form-item{
      >.el-form-item__label{
        display: none;
      }
      >.el-form-item__content{
        margin-left: 0!important;
      }
    }
  }
}
.jvs-form-autoflexable {
  .el-form-item {
    display: flex;
    margin: 0 10px;
    .el-form-item__label-wrap {
      margin-left: 0 !important;
      word-break: keep-all;
    }
    .el-form-item__content {
      margin-left: 0 !important;
      flex: 1;
      overflow: hidden;
      div {
        .el-input,
        .el-select {
          width: 100%;
        }
      }
    }
  }
}
.form-item-tooltip {
  display: inline-block;
  margin: 0 10px;
}
.form-item-no-label-tab{
  >.el-form-item__content{
    margin-left: 0!important;
  }
}
.form-item-no-label{
  .el-form-item__content{
    margin-left: 0!important;
  }
}
.form-item-no-label-nopadding{
  padding: 0!important;
}
.form-item-tips{
  font-size: 12px;
  color: #c3c3c3;
  width: 100%;
}
.reportTable-item{
  display: flex;
  flex-wrap: wrap;
  .el-form-item__content{
    margin-left: 0!important;
    width: 100%;
  }
}
.before-append-item{
  .el-form-item__content{
    display: flex;
    align-items: center;
  }
}
.jvs-form-loading-modal{
  width: 100%;
  height: 100%;
  position: absolute;
  left: 0;
  top: 0;
  background-color: #fff;
  background-image: url('../../styles/loading.gif');
  background-repeat: no-repeat;
  background-position: center;
  z-index: 9;
}
</style>
