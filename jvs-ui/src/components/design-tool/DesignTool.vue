<template>
  <div :class="{ 'design-tool': true, 'header-top-open': show, 'header-top-close': !show }">
    <div :class="{ 'pageheader-top': true, 'pageheader-top-open': show }">
      <div class="left-box">
        <span v-if="dataModelId" style="display:flex;align-items:center;">
          <span style="display:inline-block;width:70px;">模型名称：</span>
          <span v-if="!dataModelNameEditShow">{{dataModelName}}</span>
          <i class="el-icon-edit" v-if="!dataModelNameEditShow" style="margin-left:5px;cursor:pointer;" @click="editName"></i>
          <el-input v-if="dataModelNameEditShow" ref="editDataModelName" size="mini" v-model="dataModelNameEdit" @blur="editNameSub" style="width:150px;"></el-input>
          <i class="el-icon-setting" style="margin-left:5px;cursor:pointer;" @click="modelSetHandle"></i>
        </span>
        <span v-else>工具栏</span>
        <slot name="left"></slot>
      </div>
      <div class="right-box">
        <slot name="right"></slot>
      </div>
    </div>
    <slot name="bottom"></slot>
    <el-dialog
      title="数据模型配置"
      append-to-body
      :visible.sync="dialogVisible"
      :close-on-click-modal="false"
      :before-close="handleClose">
      <div v-if="dialogVisible" :class="{'data-model-set': true, 'field': activeName == 'field'}">
        <el-tabs v-model="activeName">
          <el-tab-pane label="模型字段" name="field">
            <jvs-table class="hide-top-jvs-table" :option="fieldOption" :data="fieldList" @on-load="getModelFieldHandle">
            </jvs-table>
          </el-tab-pane>
          <el-tab-pane label="索引管理" name="index">
            <div class="index-box">
              <div class="index-top">
                <div></div>
                <div>
                  <el-button type="primary" icon="el-icon-plus" size="mini" @click="addOneGroup">添加组</el-button>
                </div>
              </div>
              <div class="index-list">
                <div v-for="(data, index) in modelIndexList" :key="'data-index-item-'+index" class="index-list-item">
                  <div :class="{'index-list-item-top': true, error: (data.name ? (repeatList.indexOf(data.name) > -1) : true)}">
                    <div class="left">
                      <el-input v-model="data.name" placeholder="请输入索引名称" size="mini"></el-input>
                      <el-checkbox v-model="data.repetitionAllowed" size="mini">唯一键</el-checkbox>
                      <span class="error-tip">{{data.name ? (repeatList.indexOf(data.name) > -1 ? '索引名称存在重复' : '') : '索引名称不能为空'}}</span>
                    </div>
                    <div class="right">
                      <el-button type="primary" icon="el-icon-plus" size="mini" @click="addFieldOfGroup(data, index)">添加索引</el-button>
                      <el-button icon="el-icon-copy-document" size="mini" @click="copyOneGroup(data, index, modelIndexList)">复制组</el-button>
                      <el-button type="info" size="mini" @click="deleteOneGroup(index, modelIndexList)">删除组</el-button>
                    </div>
                  </div>
                  <div class="index-list-item-body">
                    <div v-for="(field, findex) in data.fields" :key="'data-index-item'+index+'-field-item-'+findex" class="index-list-item-body-item">
                      <el-select v-model="field.key" placeholder="请选择字段" size="mini" filterable>
                        <el-option v-for="key in fieldList" :key="'data-index-item'+index+'-field-item-'+findex+'-'+key.fieldKey" :label="key.fieldKey" :value="key.fieldKey">
                          <span style="float: left">{{key.fieldName}}</span>
                          <span style="float: right">{{key.fieldKey}}</span>
                        </el-option>
                      </el-select>
                      <el-select v-model="field.sort" size="mini">
                        <el-option label="升序" value="asc"></el-option>
                        <el-option label="降序" value="desc"></el-option>
                      </el-select>
                      <i class="el-icon-delete" style="margin-left: 10px;cursor: pointer;" @click="deleteOneGroup(findex, data.fields)"></i>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
          <!-- <el-tab-pane label="唯一性配置" name="only">
            <el-alert
              show-icon
              title=""
              type="warning"
              :closable="false">
              <template slot="title">
                <div class="warning-alter-box">
                  唯一性设置会<span>清除系统所有数据</span>，请谨慎操作。或在管理员要求下再进行添加。
                </div>
              </template>
            </el-alert>
            <jvs-form :option="onlyOption" :formData="settingForm" style="margin-top: 15px;">
              <template slot="formulaForm">
                <el-button size="mini" @click="openButtonFormula">配置</el-button>
              </template>
            </jvs-form>
          </el-tab-pane> -->
          <el-tab-pane label="数据脱敏配置" name="tuomin" style="margin-right: 0;">
            <jvs-form :option="settingOption" :formData="settingForm">
              <template slot="formulaForm">
                <el-button size="mini" @click="openButtonFormula">配置</el-button>
              </template>
              <template slot="userListForm">
                <div v-if="settingForm.userList && settingForm.userList.length > 0">
                  <el-tag v-for="(item, index) in settingForm.userList" :key="item.id" :type="item.type=='user'? '': 'info'" closable style="margin-right: 10px;" @close="deleteUser(index, settingForm.userList)">{{item.name}}</el-tag>
                </div>
                <div style="margin-top: 10px;">
                  <el-button size="mini" @click="openUser('userList')">选择人员/角色</el-button>
                </div>
              </template>
            </jvs-form>
          </el-tab-pane>
          <el-tab-pane v-if="!enableVersionFeature" label="数据权限配置" name="datapermission">
            <div class="permission-body">
              <p>默认有权限的用户可以看所有数据。</p>
              <div style="display: flex;align-items: center;justify-content: space-between;">
                <p>可根据实际场景调整数据权限功能。</p>
                <jvs-button type="primary" @click="addPermissionGroup" icon="el-icon-plus">添加权限组</jvs-button>
              </div>
              <div class="permission-content">
                <div class="permission-content-item" v-for="(item, key) in permissionList" :key="key">
                  <div style="width: 100%; background-color: #DCDFE6;height: 1px;margin-bottom: 16px;"/>
                  <el-form label-width="80px" label-position="top">
                    <el-button size="mini" type="primary" icon="el-icon-plus" @click="handleUserSelectOpen(item, key)">选择人员/角色/部门</el-button>
                    <div style="position: relative; padding: 10px 0">
                      <el-tag
                        v-for="tag in item.personnels"
                        size="small"
                        style="margin-right: 4px;margin-bottom: 4px;"
                        :key="tag.id"
                        @close="handleDelUser(tag.id, item.personnels)"
                        closable>
                        {{getTagName(tag)}}
                      </el-tag>
                    </div>
                      <el-form-item label="数据权限" class="cust-radio-line">
                        <el-checkbox-group v-model="item.scopeList">
                          <el-checkbox v-for="(op, index) in dataPermissionList" :key="op.value+index" :label="op.value">{{op.label}}</el-checkbox>
                          <el-button v-if="item.scopeList.indexOf('form_item') > -1" size="mini" type="text" style="margin-left:10px;" @click="setFieldCondition(item)">设置条件</el-button>
                        </el-checkbox-group>
                      </el-form-item>
                  </el-form>
                  <jvs-button class="del-btn" type="info" @click="deleteGroupItem(item, key)">删除权限组</jvs-button>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
      <el-row v-if="['field', 'index'].indexOf(activeName) == -1" class="botton-row">
        <el-button size="mini" @click="handleClose">取消</el-button>
        <el-button size="mini" type="primary" @click="modelSetSubmit">提交</el-button>
      </el-row>
      <el-row v-if="activeName == 'index'" class="botton-row">
        <el-button size="mini" @click="handleClose">取消</el-button>
        <el-button type="primary" size="mini" :loading="addIndexFieldLoading" @click="addIndex">更新索引</el-button>
      </el-row>
    </el-dialog>
    <userSelector
      ref="userSelector"
      :userEnable="true"
      :roleEnable="true"
      :currentActiveName="'user'"
      :selectable="true"
      :dialogTitle="'人员选择'"
      @submit="addCheckUSer"
    />
    <userSelector
      ref="userPermissionSelector"
      :selectable="true"
      :userEnable="true"
      :roleEnable="true"
      :dept-enable="true"
      @submit="addCheckUSerPermission">
    </userSelector>
    <!-- 设置条件 -->
    <el-dialog
      title="设置条件"
      width="800px"
      append-to-body
      :visible.sync="condiVisible"
      :close-on-click-modal="false"
      :before-close="condiClose">
      <div v-if="condiVisible" class="condi-box">
        <h4>数据会按照如下条件进行筛选</h4>
        <div v-if="currentItem && custFilterList && custFilterList.length > 0">
          <el-row v-for="(condi, index) in condiList" :key="'condi'+index">
            <el-select v-model="condi.key" placeholder="请选择" size="mini" v-if="custFilterList && custFilterList.length > 0" @change="keyChange(index)">
              <el-option
                v-for="it in custFilterList"
                :key="it.fieldDto.fieldKey+'-'+index"
                v-show="needShow(it.fieldDto.fieldKey)"
                :label="it.fieldDto.fieldName"
                :value="it.fieldDto.fieldKey">
                <span style="float: left">{{ it.fieldDto.fieldName }}</span>
                <span style="float: right; color: #8492a6; font-size: 13px">{{ it.fieldDto.fieldKey }}</span>
              </el-option>
            </el-select>
            <el-select v-model="condi.operator" placeholder="请选择" size="mini">
              <el-option label="等于" value="eq" v-if="getAttrVal('dataQueryTypes', condi.key).indexOf('eq') > -1"></el-option>
              <el-option label="不等于" value="ne" v-if="getAttrVal('dataQueryTypes', condi.key).indexOf('ne') > -1"></el-option>
              <el-option label="包含" value="in" v-if="getAttrVal('dataQueryTypes', condi.key).indexOf('in') > -1"></el-option>
              <el-option label="不包含" value="notIn" v-if="getAttrVal('dataQueryTypes', condi.key).indexOf('notIn') > -1"></el-option>
            </el-select>
            <el-input v-if="getAttrVal('values', condi.key).length == 0" v-model="condi.value" size="mini"></el-input>
            <el-select v-else v-model="condi.value" placeholder="请选择或输入" size="mini" filterable allow-create multiple collapse-tags>
              <el-option v-for="(fi, fix) in getAttrVal('values', condi.key)" :key="'fitem-'+fix" :label="fi.name" :value="fi.value"></el-option>
            </el-select>
            <i class="del-button-row el-icon-delete" @click="delItemOfList(condiList, index)"></i>
          </el-row>
          <el-button icon="el-icon-plus" type="text" @click="addCondi">添加</el-button>
        </div>
        <p class="sub-row">
          <el-button type="primary" size="mini" @click="submitCondi">确定</el-button>
          <el-button size="mini" @click="condiClose">取消</el-button>
        </p>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import saveicon from "@/const/img/保存.png"
import closeicon from "@/const/img/关闭.png"
import {getModelName, editModelName, editModelSetting, getModelFieldList} from "@/components/api"
import userSelector from '@/components/basic-assembly/userSelector'
import { getEncryptionList } from '@/views/page/api/form'
import { getModelIndex, addIndexField } from "@/components/template/api";
import { getDataModelDataFilter} from "@/views/page/api/list";
import {getAppInfoById} from "@/views/page/api/newDesign";
export default {
  components: { userSelector },
  name: "PageHeader",
  props: {
    title: {
      type: String
    },
    needLoading: {
      type: Boolean,
      default: false
    },
    dataModelId: {
      type: String
    },
    jvsAppId: {
      type: String
    },
    designId: {
      type: String
    },
    dataModelFields: {
      type: Array
    },
    fresh: {
      type: Boolean
    }
  },
  computed: {
    repeatList () {
      let repeat = []
      let temp = []
      let newVal = this.modelIndexList
      for(let i in newVal) {
        if(newVal[i].name) {
          if(temp.indexOf(newVal[i].name) > -1 && repeat.indexOf(newVal[i].name) == -1) {
            repeat.push(newVal[i].name)
          }else{
            temp.push(newVal[i].name)
          }
        }
      }
      return repeat
    }
  },
  data () {
    return {
      show: true,
      saveIcon: saveicon,
      closeIcon: closeicon,
      saveLoading: false,
      timer: null,
      dataModelName: '',
      dataModelNameEdit: '',
      dataModelNameEditShow: false,
      dialogVisible: false,
      settingForm: {},
      fieldList: [],
      fieldOption: {
        addBtn: false,
        search: false,
        viewBtn: false,
        editBtn: false,
        delBtn: false,
        hideTop: false,
        menu: false,
        column: [
          {
            label: '字段名',
            prop: 'fieldKey'
          },
          {
            label: '说明',
            prop: 'fieldName'
          }
        ]
      },
      onlyOption: {
        btnHide: true,
        formAlign: 'top',
        column: [
          {
            label: '校验内容设置',
            prop: 'formula',
            formSlot: true
          },
          {
            label: '单用户只能提交一次',
            prop: 'once',
            type: 'switch'
          },
        ]
      },
      settingOption: {
        formAlign: 'top',
        btnHide: true,
        column: [
          {
            label: '是否脱敏',
            prop: 'encryption',
            type: 'switch'
          },
          {
            label: '数据权限设置',
            prop: 'userList',
            formSlot: true
          },
          {
            label: '脱敏字段（注：非文字类字段，脱敏后，将自动隐藏对应控件）',
            prop: 'encryptionFields',
            type: 'tableForm',
            border: true,
            editable: true,
            addBtn: true,
            delBtn: true,
            align: 'left',
            menuAlign: 'left',
            displayExpress: [ { prop: 'encryption', value: 'true' } ],
            iconBtn: true,
            tableColumn: [
              {
                label: '字段名称',
                prop: 'fieldKey',
                type: 'select',
                dicData: [],
                props: {
                  label: 'fieldName',
                  value: 'fieldKey'
                }
              },
              {
                label: '脱敏正则',
                prop: 'encryptionExpress',
                type: 'select',
                dicData: []
              }
            ]
          }
        ]
      },
      userKey: '',
      activeName: 'field',
      permissionList: [],
      dataPermissionList: [
        {label: '全部数据', value: 'all'},
        {label: '本人提交', value: 'self'},
        {label: '本部门提交', value: 'curr_dept'},
        {label: '下级部门提交', value: 'curr_dept_tree'},
        {label: '提交\\流转\\抄送包含本人', value: 'flowTaskPersons'},
        {label: '根据表单内容设置过滤条件', value: 'form_item'},
      ],
      condiVisible: false,
      condiList: [],
      userIds: [],
      options: [],
      userList: [],
      custFilterList: [],
      enableVersionFeature: false,
      modelIndexList: [],
      addIndexFieldLoading: false,
    }
  },
  methods: {
    openClose () {
      let bool=this.show
      // this.show = !bool
    },
    saveHandle () {
      this.$emit("save", true)
      if(this.needLoading) {
        this.saveLoading = true
        if(this.timer) {
          clearTimeout(this.timer)
        }
        let _this =  this
        this.timer = setTimeout(() => {
          _this.saveLoading = false
        }, 2000)
      }
    },
    closeHandle () {
      this.$emit("close", true)
    },
    editName () {
      this.dataModelNameEdit = this.dataModelName
      this.dataModelNameEditShow = true
      this.$nextTick(()=>{
        this.$refs.editDataModelName.focus()
      })
    },
    editNameSub () {
      if(this.dataModelNameEdit != this.dataModelName) {
        editModelName(this.$route.query.jvsAppId, this.dataModelNameEdit, this.dataModelId).then(res => {
          if(res.data && res.data.code == 0) {
            this.dataModelName = this.dataModelNameEdit
            this.dataModelNameEditShow = false
            this.dataModelNameEdit = ''
            this.$notify({
              title: '提示',
              message: '修改成功',
              position: 'bottom-right',
              type: 'success'
            });
          }
        })
      }else{
        this.dataModelNameEditShow = false
        this.dataModelNameEdit = ''
      }
    },
    getModelInfo () {
      if(this.dataModelId) {
        getModelName(this.$route.query.jvsAppId, this.dataModelId).then(res => {
          if(res.data && res.data.code == 0 && res.data.data) {
            this.dataModelName = res.data.data.name
            if(res.data.data.setting) {
              this.settingForm = res.data.data.setting
            }
            if(res.data.data.role) {
              this.permissionList = res.data.data.role
            }
            this.$forceUpdate()
          }
        })
      }
    },
    modelSetHandle () {
      this.settingOption.column.filter(item => {
        if(item.prop == 'encryptionFields') {
          item.tableColumn[0].dicData = this.dataModelFields
        }
      })
      if(this.settingForm.once != true) {
        this.$set(this.settingForm, 'once', false)
      }
      if(!this.settingForm.formula) {
        this.$set(this.settingForm, 'formula', '')
      }
      if(!this.settingForm.formulaContent) {
        this.$set(this.settingForm, 'formulaContent', '')
      }
      if(!this.settingForm.userList) {
        this.$set(this.settingForm, 'userList', [])
      }
      getDataModelDataFilter(this.$route.query.jvsAppId, this.dataModelId).then(res => {
        if(res.data && res.data.code == 0) {
          this.custFilterList = res.data.data
        }
      })
      getModelIndex(this.$route.query.jvsAppId, this.dataModelId).then(res => {
        if(res.data && res.data.code == 0) {
          this.modelIndexList = res.data.data || []
          this.dialogVisible = true
        }
      })
    },
    // 配置公式
    openButtonFormula () {
      this.$setFormula({
        title: '公式设置',
        hasModal: true,
        label: this.dataModelName,
        execId: this.settingForm.formula ? this.settingForm.formula : '',
        apiPrefix: 'jvs-design',
        useCase: 'thisDataItemValue',
        props: {
          jvsAppId: this.jvsAppId,
          designId: this.dataModelId,
          businessId: this.dataModelId
        },
        afterSave: (dialog, data) => {
          if(data && data.id) {
            this.$set(this.settingForm, 'formula', data.id)
            this.$set(this.settingForm, 'formulaContent', data.body)
          }
          dialog.handleClose()
        }
      })
    },
    modelSetSubmit () {
      editModelSetting(this.$route.query.jvsAppId, this.dataModelId, {setting: this.settingForm, role: this.permissionList}).then(res => {
        if(res.data && res.data.code == 0) {
          this.$notify({
            title: '提示',
            message: '设置成功',
            position: 'bottom-right',
            type: 'success'
          });
          this.handleClose()
          this.getModelInfo()
        }
      })
    },
    handleClose () {
      this.permissionList = []
      this.dialogVisible = false
      this.activeName = 'field'
      this.settingForm = {}
      this.fieldList = []
      this.modelIndexList = []
    },
    openUser (userKey) {
      this.userKey = userKey
      if(!this.settingForm[userKey]) {
        this.$set(this.settingForm, userKey, [])
      }
      this.$refs.userSelector.openDialog(this.settingForm[userKey])
    },
    addCheckUSer (list) {
      this.$set(this.settingForm, this.userKey, list)
    },
    deleteUser (index, arr) {
      arr.splice(index, 1)
    },
    getTmExList () {
      getEncryptionList().then(res => {
        if(res.data && res.data.code == 0) {
          this.settingOption.column.filter(item => {
            if(item.prop == 'encryptionFields') {
              item.tableColumn[1].dicData = res.data.data
            }
          })
        }
      })
    },
    getModelFieldHandle () {
      getModelFieldList(this.$route.query.jvsAppId, this.dataModelId).then(res => {
        if(res.data && res.data.code == 0) {
          this.fieldList = res.data.data
        }
      })
    },
    // 添加索引
    addIndex () {
      if(!(this.repeatList && this.repeatList.length > 0)) {
        let bool = true
        this.modelIndexList.filter(item => {
          if(!item.name) {
            bool = false
          }
        })
        if(bool) {
          this.addIndexFieldLoading = true
          addIndexField(this.$route.query.jvsAppId, this.dataModelId, this.modelIndexList).then(res => {
            if(res.data && res.data.code == 0) {
              this.$notify({
                title: '提示',
                message: '更新索引成功',
                position: 'bottom-right',
                type: 'success'
              })
            }
            this.addIndexFieldLoading = false
          }).catch(e => {
            this.addIndexFieldLoading = false
          })
        }
      }
    },
    // 获取tag中文标识
    getTagName(obj) {
      let header = ''
      switch (obj.type) {
        case 'user':
          break;
        case 'role':
          header = '（角色）';
          break;
        case 'dept':
          header = '（部门）';
          break;
        case 'group':
          header = '（群组）';
          break;
        default: break;
      }
      return header + obj.name
    },
    // 打开人员选择组件
    handleUserSelectOpen(item, key) {
      this.$refs.userPermissionSelector.openDialog(item.personnels)
      this.key = key
    },
    // 删除标签
    handleDelUser(id, arr) {
      const index = arr.findIndex(item => {
        return item.id === id
      })
      arr.splice(index, 1)
    },
    // 添加人员 提交
    addCheckUSerPermission(checkList) {
      this.permissionList[this.key].personnels = checkList
      this.$refs.userPermissionSelector.closeDialog()
    },
    // 添加权限组
    addPermissionGroup() {
      const obj = {
        id: new Date().getTime(),
        operation: [],
        personnels: [],
        scopeList: [],
        conditionList: []
      }
      this.permissionList.push(obj)
    },
    // 删除单个权限组
    deleteGroupItem(obj, key) {
      this.permissionList.splice(key, 1)
    },
    // 设置条件
    setFieldCondition (item) {
      if(item.conditionList) {
        this.condiList = JSON.parse(JSON.stringify(item.conditionList))
        this.condiList.filter(item => {
          if(typeof item.value == 'string') {
            if(!item.value) {
              item.value = []
            }else{
              item.value = [item.value]
            }
          }
        })
      }
      this.currentItem = item
      this.condiVisible = true
    },
    // 关闭设置条件
    condiClose () {
      this.condiList = []
      this.condiVisible = false
      this.currentItem = null
    },
    addCondi () {
      this.condiList.push({})
      this.$forceUpdate()
    },
    delItemOfList (list, index) {
      list = list.splice(index, 1)
      this.$forceUpdate()
    },
    submitCondi () {
      for(let i in this.permissionList) {
        if(this.permissionList[i].id == this.currentItem.id) {
          this.$set(this.permissionList[i], 'conditionList', this.condiList)
        }
      }
      this.condiClose()
    },
    getAttrVal (attr, val) {
      let temp = []
      for(let i in this.custFilterList) {
        if(this.custFilterList[i].fieldDto['fieldKey'] == val) {
          if(this.custFilterList[i][attr]) {
            temp = this.custFilterList[i][attr]
          }
        }
      }
      return temp
    },
    needShow (key) {
      let bool = true
      this.condiList.filter(item => {
        if(item.key && item.key == key) {
          bool = false
        }
      })
      return bool
    },
    keyChange (index) {
      this.$set(this.condiList[index], 'value', [])
    },
    // 获取应用列表
    getAppInfo() {
      if(this.$route.query && this.$route.query.jvsAppId) {
        getAppInfoById(this.$route.query.jvsAppId).then(res => {
          if (res.data && res.data.code == 0 && res.data.data) {
            if(res.data.data.enableVersionFeature) {
              this.enableVersionFeature = true
            }
          }
        })
      }
    },
    addOneGroup () {
      this.modelIndexList.push({
        name: '',
        repetitionAllowed: false,
        fields: []
      })
      this.$forceUpdate()
    },
    addFieldOfGroup (row, index) {
      row.fields.push({
        key: '',
        sort: 'asc'
      })
    },
    copyOneGroup (row, index, list) {
      let copy = JSON.parse(JSON.stringify(row))
      copy.name += '_copy'
      list.splice(index+1, 0, copy)
      this.$forceUpdate()
    }, 
    deleteOneGroup (index, list) {
      list.splice(index, 1)
      this.$forceUpdate()
    }
  },
  created () {
    this.getAppInfo()
    this.getModelInfo()
    this.getTmExList()
  },
  watch: {
    fresh: {
      handler(newVal, oldVal) {
        if(newVal != oldVal && !newVal) {
          this.getModelInfo()
        }
      }
    }
  }
};
</script>
<style lang="scss" scoped>
.design-tool {
  padding: 0 20px;
  height: 56px;
  box-sizing: border-box;
  background: #F5F6F7;
  display: flex;
  align-items: center;
}
*::-webkit-scrollbar {
  width: 4px;
  height: 4px;
}
*::-webkit-scrollbar-thumb {
  border-radius: 10px;
  -webkit-box-shadow: inset 0 0 5px #dcdfe6, 0.2;
  background: rgba(0,0,0,0.1);
}
*::-webkit-scrollbar-track {
  -webkit-box-shadow: inset 0 0 5px #dcdfe6, 0.2;
  border-radius: 0;
  background: rgba(0,0,0,0.1);
}
.pageheader-top {
  display: flex;
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  position: relative;
  .pageheader-line {
    width: 4px;
    height: 22px;
    background: #3471ff;
    border-radius: 2px;
    cursor: pointer;
    margin: 0 20px;
  }
  .title {
    font-size: 18px;
    font-weight: 600;
    font-family: Microsoft YaHei, Microsoft YaHei;
    color: #333333;
    cursor: pointer;
    margin-right: 20px;
  }
  .left-box{
    position: absolute;
    left: 0;
  }
  .left-box, .right-box {
    display: flex;
    align-items: center;
    font-size: 14px;
  }
  .right-box{
    p{
      img{
        display: block;
        width: 20px;
        height: 20px;
        cursor: pointer;
      }
    }
    .right-icon-close{
      img{
        width: 18px;
        height: 18px;
      }
    }
  }
}
.header-top-close {
  height: 44px;
  .pageheader-line {
    width: 20px;
    height: 4px;
  }
}
.el-dialog__wrapper:not(.user-info-list-dialog){
  /deep/.el-dialog{
    height: 73.5vh;
    position: absolute;
    left: 0;
    right: 0;
    top: 0;
    bottom: 0;
    margin: auto;
    .el-dialog__body{
      height: calc(100% - 48px);
      padding-right: 0;
      box-sizing: border-box;
      position: relative;
    }
  }
}
.data-model-set{
  height: calc(100% - 60px + 24px);
  &.field{
    height: 100%;
  }
  .el-tabs{
    height: 100%;
    /deep/.el-tabs__header{
      margin-bottom: 16px;
      .el-tabs__nav-wrap{
        &::after{
          height: 1px;
          background-color: #EEEFF0;
        }
        .el-tabs__item{
          font-family: Microsoft YaHei-Regular, Microsoft YaHei;
          font-weight: 400;
          font-size: 14px;
          color: #6F7588;
          height: 26px;
          line-height: 18px;
          &.is-active{
            font-family: Microsoft YaHei-Bold, Microsoft YaHei;
            font-weight: 700;
            color: #1E6FFF;
          }
        }
        .el-tabs__active-bar{
          background: #1E6FFF;
          border-radius: 2px 0px 2px 0px;
          overflow: hidden;
        }
      }
    }
    /deep/.el-tabs__content{
      height: calc(100% - 43px);
      .el-tab-pane{
        margin-right: 24px;
        height: 100%;
        .el-alert--warning{
          height: 36px;
          box-sizing: border-box;
        }
        .warning-alter-box{
          font-family: Microsoft YaHei-Regular, Microsoft YaHei;
          font-weight: 400;
          font-size: 12px;
          color: #6F7588;
          span{
            color: #FF194C;
          }
        }
        .jvs-form{
          height: 100%;
          overflow: hidden;
          overflow-y: auto;
          .el-form-item{
            .el-form-item__label{
              font-family: Microsoft YaHei-Bold, Microsoft YaHei;
              font-weight: 700;
              font-size: 14px;
              color: #363B4C;
            }
          }
          .form-column-tableForm{
            padding-right: 24px;
            .slot-label-item{
              margin-bottom: 0;
              display: flex;
              flex-direction: column;
              .el-form-item__label{
                margin-left: 100px;
              }
            }
            .el-form-item{
              margin-bottom: 0;
              .jvs-form-item{
                padding: 16px;
                border-radius: 4px;
                background: #F5F6F7;
                overflow: hidden;
              }
              .table-form{
                .el-form-item{
                  margin: 0;
                  .el-form-item__content{
                    margin-left: 0!important;
                  }
                  .jvs-form-item{
                    padding: 0;
                  }
                }
                .jvs-table{
                  background: transparent;
                }
                .table-body-box{
                  background: transparent;
                  .el-table{
                    background: transparent;
                    &::before{
                      visibility: hidden;
                    }
                    .el-table__header-wrapper{
                      border: 0;
                      .el-table__header{
                        .headerclass{
                          th{
                            background: #F5F6F7;
                            height: 20px;
                            padding: 0;
                            line-height: 20px;
                            .cell{
                              font-family: Source Han Sans-Regular, Source Han Sans;
                              font-weight: 400;
                              font-size: 14px;
                              color: #363B4C;
                              text-align: left;
                              padding: 0;
                              padding-right: 16px;
                            }
                          }
                        }
                      }
                    }
                    .el-table__body-wrapper{
                      min-height: unset;
                      .el-table__body{
                        tr{
                          background: transparent;
                          td{
                            padding: 0;
                            padding-top: 8px;
                            height: 32px;
                            line-height: 32px;
                            border: 0;
                            .cell{
                              padding: 0;
                              padding-right: 16px;
                              .el-form-item__content{
                                min-height: unset;
                                line-height: 32px;
                                .el-input{
                                  height: 32px;
                                  .el-input__inner{
                                    height: 32px;
                                    line-height: 32px;
                                    background: #fff;
                                  }
                                }
                                .jvs-color-picker-show-box{
                                  height: 32px;
                                  background: #fff;
                                }
                              }
                            }
                            &.table-index-column{
                              .cell{
                                text-indent: 10px;
                              }
                            }
                            &:nth-last-of-type(1){
                              .cell{
                                padding-right: 0;
                                text-align: center;
                              }
                            }
                          }
                          &:hover{
                            td{
                              background: none;
                            }
                          }
                        }
                      }
                      .el-table__empty-block{
                        display: none;
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
  }
  .jvs-table.hide-top-jvs-table/deep/{
    height: calc(100% - 27px);
    .jvs-table-titleTop{
      display: none;
    }
    .table-body-box{
      height: 100%;
      padding: 0;
      .el-table{
        height: 100%;
        .el-table__header-wrapper{
          tr{
            th{
              font-family: Microsoft YaHei-Bold, Microsoft YaHei;
              font-weight: 700;
              font-size: 14px;
              color: #363B4C;
              background: #F5F6F7;
              .cell{
                padding: 0 24px;
              }
            }
          }
        }
        .el-table__body-wrapper{
          height: calc(100% - 40px);
          .el-table__body{
            .el-table__cell{
              .cell{
                padding: 0 24px;
                font-family: Microsoft YaHei-Regular, Microsoft YaHei;
                .el-button--text{
                  font-family: Microsoft YaHei-Regular, Microsoft YaHei;
                  font-weight: 400;
                  font-size: 14px;
                  color: #1E6FFF;
                }
              }
            }
          }
        }
      }
    }
  }
  .permission-body{
    padding: 20px 30px;
    background-color: #ffffff;
    border-radius: 6px;
    .title {
      font-weight: bold;
      font-size: 16px;
      margin-bottom: 16px;
    }
    p {
      color: #b3b3b3;
      margin: 10px 0;
      span {
        color: #3471ff;
        cursor: pointer;
      }
    }
    .permission-header{
      margin-top: 32px;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .permission-content{
      margin-top: 16px;
      max-height: calc(100vh - 200px);
      overflow-y: auto;
      position: relative;
      padding-bottom: 30px;
      box-sizing: border-box;
      .permission-content-item{
        position: relative;
        .del-btn{
          position: absolute;
          right: 20px;
          top: 16px;
          cursor: pointer;
        }
      }
    }
    .cust-radio-line{
      /deep/.el-checkbox-group{
        .el-checkbox:nth-last-of-type(1){
          margin-left: 0;
        }
      }
    }
  }
  .index-box{
    height: 100%;
    .index-top{
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
    .index-list{
      padding-right: 10px;
      margin-top: 12px;
      height: calc(100% - 40px);
      box-sizing: border-box;
      overflow: hidden;
      overflow-y: auto;
      .index-list-item{
        border-top: 1px solid #dcdfe6;
        padding-top: 16px;
        .index-list-item-top{
          display: flex;
          align-items: center;
          justify-content: space-between;
          &.error{
            /deep/.el-input{
              .el-input__inner{
                border-color: #F56C6C;
              }
            }
          }
          .left, .right{
            display: flex;
            align-items: center;
            position: relative;
          }
          .left{
            .el-checkbox{
              margin: 0 12px;
            }
            .error-tip{
              position: absolute;
              bottom: -16px;
              left: 0;
              font-size: 12px;
              color: #F56C6C;
            }
          }
        }
        &+.index-list-item{
          margin-top: 16px;
        }
        .index-list-item-body{
          .index-list-item-body-item{
            display: flex;
            align-items: center;
            margin-top: 16px;
            .el-select{
              margin-right: 10px;
            }
          }
        }
      }
    }
  }
}
.botton-row{
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 60px;
  background: #fff;
  border-top: 1px solid #EEEFF0;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  .el-button{
    margin: 0;
    margin-right: 16px;
  }
}
.condi-box{
  h4{
    color: #666;
    padding: 0;
    margin: 0;
    font-weight: normal;
  }
  .el-row{
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 10px;
    /deep/ .el-select, .el-input{
      margin-right: 10px;
      .el-input{
        width: 200px;
        margin-right: 0;
      }
    }
    /deep/ .el-input{
      width: 200px;
      margin-right: 10px;
    }
    .del-button-row{
      cursor: pointer;
      margin-left: 10px;
    }
  }
  p{
    display: flex;
    justify-content: center;
    align-items: center;
  }
}
</style>
