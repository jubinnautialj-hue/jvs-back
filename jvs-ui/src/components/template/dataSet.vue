<template>
  <div class="data-set">
    <!-- <jvs-button type="primary" @click="syncDataModel">同步数据模型</jvs-button> -->
    <div style="margin: 15px 0;display: flex;align-items: center;">
      <jvs-button class="add-button" type="primary" icon="el-icon-plus" @click="addEditHandle(null)">新增</jvs-button>
    </div>
    <jvs-table
      class="data-set-table-list"
      :loading="tableLoading"
      :page="page"
      :option="dataPageOption"
      :data="dataPageList"
      @on-load="getDataModelList"
      @search-change="searchChange"
    >
      <template slot="name" slot-scope="scope">
        <span>
          <span v-if="!scope.row.dataModelNameEditShow">{{scope.row.name}}</span>
          <i class="el-icon-edit" v-if="!scope.row.dataModelNameEditShow && (modeUserInfo ? modeUserInfo.mode == 'DEV' : true)" style="margin-left:5px;cursor:pointer;" @click.stop="editName(scope.row, scope.index)"></i>
          <el-input v-show="scope.row.dataModelNameEditShow" :ref="'editDataModelName'+scope.index" size="mini" v-model="dataModelNameEdit" @blur="editNameSub"></el-input>
        </span>
      </template>
      <template slot="collectionName" slot-scope="scope">
        <span>
          <span v-if="!scope.row.dataModelNoEditShow">{{scope.row.collectionName}}</span>
          <i class="el-icon-edit" v-if="!scope.row.dataModelNoEditShow && (modeUserInfo ? modeUserInfo.mode == 'DEV' : true)" style="margin-left:5px;cursor:pointer;" @click.stop="editNo(scope.row, scope.index)"></i>
          <el-input v-show="scope.row.dataModelNoEditShow" :ref="'editDataModelNo'+scope.index" size="mini" v-model="dataModelNoEdit" @blur="editNoSub"></el-input>
        </span>
      </template>
      <template slot="menu" slot-scope="scope">
        <jvs-button size="mini" type="text" @click="addEditHandle(scope.row)">修改</jvs-button>
        <jvs-button v-if="scope.row.fieldList && scope.row.fieldList.length > 0" size="mini" type="text" @click="handleDetail(scope.row)">详情</jvs-button>
        <jvs-button v-if="scope.row.fieldList && scope.row.fieldList.length > 0" size="mini" type="text" @click="genRowHandle(scope.row)">生成设计</jvs-button>
        <jvs-button size="mini" type="text" @click="deleteDataModel(scope.row)">删除</jvs-button>
      </template>
    </jvs-table>
    <!-- 数据集详情 -->
    <el-dialog
      title="数据集详情"
      append-to-body
      :visible.sync="dataModelDialog"
      :before-close="dataModelClose"
      :close-on-click-modal="false"
    >
      <div v-if="dataModelDialog" :class="{'data-model-set': true, 'field': dataModelActive == 'field'}">
        <el-tabs v-model="dataModelActive">
          <el-tab-pane label="字段管理" name="field">
            <jvs-table class="hide-top-jvs-table" :option="fieldOption" :data="fieldList">
              <template slot="menu" slot-scope="scope">
                <jvs-button size="mini" type="text" @click="delIndex(scope.row)"><span style="color: #F56C6C;">删除</span></jvs-button>
              </template>
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
        </el-tabs>
      </div>
      <el-row v-if="dataModelActive == 'index'" class="botton-row">
        <el-button size="mini" @click="dataModelClose">取消</el-button>
        <el-button type="primary" size="mini" :loading="addIndexFieldLoading" @click="addIndex">更新索引</el-button>
      </el-row>
    </el-dialog>
    <!-- 同步数据模型 -->
    <el-dialog
      title="同步模型"
      class="step-dialog"
      width="900px"
      append-to-body
      :visible.sync="syncDialog"
      :before-close="syncClose"
    >
      <div style="padding: 0 80px">
        <!-- 步骤条 -->
        <stepBar
          v-if="syncDialog"
          :formRef="'ruleForm'"
          :active="stepItem.activeName"
          :formItem="stepItem"
          :validate="validate"
          :forms="stepForms[stepItem.prop]"
          :option="{column: stepItem.dicData}"
          :originOption="formOption"
          :originForm="stepForms"
          @customHandle="customHandle"
          @nextHandle="nextHandle"
        >
          <template slot="first">
            <div class="step-content">
              <el-form ref="stepForm" size="mini" :model="formData" :rules="rules" label-position="top" class="demo-form-inline">
                <el-form-item label="IP地址" prop="ip">
                  <el-input v-model="formData.ip" placeholder="请输入IP地址"></el-input>
                </el-form-item>
                <el-form-item label="库名" prop="database">
                  <el-input v-model="formData.database" placeholder="请输入库名"></el-input>
                </el-form-item>
                <el-form-item label="账号" prop="username">
                  <el-input v-model="formData.username" placeholder="请输入账号"></el-input>
                </el-form-item>
                <el-form-item label="密码" prop="password">
                  <el-input v-model="formData.password" placeholder="请输入密码"></el-input>
                </el-form-item>
              </el-form>
            </div>
          </template>
          <template slot="second">
            <div class="step-content">
              <el-table
                ref="multipleTable"
                :data="tableData"
                border
                tooltip-effect="dark"
                style="width: auto"
                @selection-change="handleSelectionChange">
                <el-table-column
                  type="selection"
                  width="55">
                </el-table-column>
                <el-table-column
                  prop="tableName"
                  label="表名">
                </el-table-column>
                <el-table-column
                  prop="info"
                  label="别名"
                  show-overflow-tooltip
                >
                </el-table-column>
              </el-table>
            </div>
          </template>
          <template slot="third">
            <div class="step-content">
              <div class="step-process">
                <el-progress :text-inside="true" :stroke-width="26" :percentage="proportion"></el-progress>
              </div>
              <div class="step-process-text">{{syncMessage}}</div>
            </div>
          </template>
        </stepBar>
      </div>
    </el-dialog>
    <!-- 新增 修改 数据模型 -->
    <createDataModelDialog
      ref="createDataModelDialog"
      :generateCrudDesign="false"
      :jvsAppId="appInfo ? appInfo.id : ''"
      @success="getDataModelList();"
    ></createDataModelDialog>
  </div>
</template>

<script>
import {getModelIndex, addIndexField, getDataModel, getMysql, getSyncProcess, syncData, delModelField, delModel, getFiledType, genCrudDesign} from "@/components/template/api";
import {editModelName, editModelNo} from "@/components/api";

import StepBar from "@/components/basic-assembly/stepBar";
import createDataModelDialog  from '@/components/template/createDataModelDialog'

import {encryption} from "@/util/util";
import {enCodeKey, enCodePasswordKey} from "@/const/const";
import eventBus from "@/util/vuebus";
export default {
  name: "dataSet",
  components: { StepBar, createDataModelDialog },
  props: {
    appInfo: {
      type: Object
    },
    modeUserInfo: {
      type: Object
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
      proportion: 0,
      syncMessage: '正在进行同步',
      tableData: [],
      formData: {
        // ip: '10.0.0.38:3306',
        // database: 'jvs',
        // username: 'root',
        // password: 'root'
      },
      rules: {
        ip: [
          { required: true, message: '请输入IP地址', trigger: 'blur' },
        ],
        database: [
          { required: true, message: '请输入库名', trigger: 'blur' },
        ],
        username: [
          { required: true, message: '请输入账号', trigger: 'blur' },
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
        ],
      },
      stepForms: {
        demo1: {
          first: {},
          second: {}
        }
      },
      stepItem: {
        label: "",
        prop: "demo1",
        type: "step",
        activeName: "first",
        dicData: [
          {
            label: "配置数据源",
            name: "first",
            btns: [{name: "下一步", type: "next"}]
          }, {
            label: "选择同步数据表",
            name: "second",
            btns: [{name: "上一步", type: "last"}, {name: '同步', type: 'custom'}]
          }, {
            label: "同步结果",
            name: "third",
            // btns: [{name: "上一步", type: "last"}]
          }
        ],
        column: {}
      },
      formOption: {
        submitBtn: false,
        emptyBtn: false,
        cancal: false,
        column: [
          {
            label: "",
            prop: "demo1",
            type: "step",
            activeName: "first",
            dicData: [
              {
                label: "配置数据源",
                name: "first",
                btns: [{name: "下一步", type: "next"}]
              }, {
                label: "选择同步数据表",
                name: "second",
                btns: [{name: "上一步", type: "last"}]
              }, {
                label: "同步结果",
                name: "third",
                btns: [{name: "上一步", type: "last"}]
              }
            ],
          }
        ]
      },
      validate: false,
      dataModelDialog: false,
      syncDialog: false,
      syncing: false,
      dataModelRow: null,
      fieldList: [],
      fieldOption: {
        addBtn: false,
        search: false,
        viewBtn: false,
        editBtn: false,
        delBtn: false,
        hideTop: false,
        column: [
          {
            label: '字段名',
            prop: 'fieldKey'
          },
          {
            label: '说明',
            prop: 'fieldName'
          },
          {
            label: '类型',
            prop: 'fieldType',
            props: {
              label: 'name',
              value: 'type'
            },
            dicData: [
              { type: 'input', name: '单行文本' },
              { type: 'textarea', name: '多行文本' },
              { type: 'divider', name: '分割线' },
              { type: 'p', name: '小标题' },
              { type: 'select', name: '下拉框' },
              { type: 'inputNumber', name: '计数器' },
              { type: 'slider', name: '滑块'},
              { type: 'switch', name: '开关' },
              { type: 'datePicker', name: '日期' },
              { type: 'timeSelect', name: '固定时间' },
              { type: 'timePicker', name: '任意时间' },
              { type: 'radio', name: '单选' },
              { type: 'checkbox', name: '多选' },
              {type: 'image', name: '查看图片' },
              {type: 'file', name: '查看文件' },
              { type: 'colorSelect', name: '颜色选择' },
              { type: 'iconSelect', name: '图标选择' },  
              { type: 'box', name: '描述框' },
              { type: 'link', name: '链接' },
              { type: 'cascader', name: '级联选择' },
              { type: 'htmlEditor', name: '富文本' },
              { type: 'imageUpload', name: '上传图片' },
              { type: 'fileUpload', name: '上传文件' },
              { type: 'tab', name: '选项卡' },
              { type: 'tableForm', name: '表格' },
              { type: 'button', name: '按钮' },
              { type: 'iframe', name: '网页' },
              { type: 'serialNumber', name: '流水号' },
              { type: 'positionMap', name: '定位' },
              { type: 'signature', name: '手写签名' },
              { type: 'jsonEditor', name: 'JSON' },
              { type: 'pageTable', name: '列表页' },
              { type: 'bluetoothBeacon', name: '蓝牙信标' },
              { type: 'department', name: '部门选择' },
              { type: 'role', name: '角色选择' },
              { type: 'user', name: '用户选择' },
              { type: 'job', name: '岗位选择' },
              { type: 'timeline', name: '时间线' },
              { type: 'flowNode', name: '动态流程' },
              { type: 'dynamicForm', name: '动态表单' },
            ]
          }
        ]
      },
      tableLoading: false,
      dataModelNameEdit: '',
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
      },
      dataPageList: [], // 数据集列表数据
      dataPageOption: {
        addBtn: false,
        search: true,
        viewBtn: false,
        editBtn: false,
        page: true,
        delBtn: false,
        hideTop: false,
        column: [
          {
            label: '名称',
            prop: 'name',
            width: 200,
            slot: true,
            search: true
          },
          {
            label: '创建时间',
            prop: 'createTime',
            dateType: "datetime",
            format: "yyyy-MM-dd hh:mm:ss",
            valueFormat: "yyyy-MM-dd hh:mm:ss",
          },
          {
            label: '数据集ID',
            prop: 'id',
            search: true
          },
          {
            label: '数据集名称',
            prop: 'collectionName',
            slot: true,
            search: true
          },
        ]
      }, // 数据集列表配置
      tableNames: [],
      dataModelNoEdit: '',
      dataModelActive: 'field',
      rowData: null,
      modelIndexList: [],
      addIndexFieldLoading: false,
    }
  },
  methods: {
    handleSelectionChange(val) {
      this.tableNames = val.map(item => {
        return item.tableName
      })
    },
    nextHandle(callback) {
      this.$refs.stepForm.validate((valid) => {
        if (valid) {
          let tp = {
            data: JSON.stringify(this.formData)
          }
          let temp = encryption({
            data: tp,
            key: enCodePasswordKey,
            param: ["data"]
          });
          const params = {
            body: temp.data,
          }
          getMysql(params, this.appInfo.id).then(res => {
            if (res.data && res.data.code == 0 && res.data.data) {
              this.tableData = [...res.data.data]
            }
          })
          callback(true)
        } else {
          callback(false)
        }
      });
    },
    customHandle(callback) {
      if (this.tableNames.length === 0) {
        this.$notify({
          title: '提示',
          message: '请勾选需要同步的表',
          position: 'bottom-right',
          type: 'warning'
        });
        callback(false)
      } else {
        this.syncing = true
        syncData(this.tableNames, this.appInfo.id).then(res => {
          if (res.data && res.data.code == 0) {
            this.getSyncProcess()
          }
        })
        callback(true)
      }
    },
    getSyncProcess() {
      getSyncProcess(this.appInfo.id).then(res => {
        if (res.data && res.data.code == 0) {
          this.proportion = res.data.data.proportion
          this.syncMessage = res.data.data.message
          if (res.data.data && !res.data.data.isEnd) {
            this.getSyncProcess()
          } else {
            this.$notify({
              title: '提示',
              message: '数据模型同步成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.getDataModelList()
            this.syncing = false
          }
        } else {
          this.syncing = false
        }
      }).catch(err => {
        this.syncing = false
      })
    },
    // 同步数据模型
    syncDataModel() {
      this.syncDialog = true
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
          addIndexField(this.rowData.appId, this.rowData.id, this.modelIndexList).then(res => {
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
    delIndex (row) {
      this.$confirm("确认删除？", '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        delModelField(this.appInfo.id, row.modelId, row.fieldKey).then(res => {
          if(res.data && res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: '删除成功',
              position: 'bottom-right',
              type: 'success'
            })
            this.getDataModelList()
          }
        })
      })
    },
    // 关闭数据集详情弹框
    dataModelClose() {
      this.dataModelDialog = false
      this.rowData = null
      this.fieldList = []
      this.dataModelActive = 'field'
      this.modelIndexList = []
    },
    // 关闭同步数据模型弹框
    syncClose() {
      this.tableData = []
      this.formData = {}
      if (this.syncing) {
        this.$notify({
          title: '提示',
          message: '数据同步中...',
          position: 'bottom-right',
          type: 'warning'
        });
      } else {
        this.syncDialog = false
      }
    },
    // 查看数据集详情
    handleDetail (row) {
      this.rowData = JSON.parse(JSON.stringify(row))
      getModelIndex(row.appId, row.id).then(res => {
        if(res.data && res.data.code == 0) {
          this.modelIndexList = res.data.data || []
          this.fieldList = [...row.fieldList]
          this.dataModelDialog = true
        }
      })
    },
    editName (row, index) {
      this.editIndex = index
      this.$set(this, 'dataModelRow', JSON.parse(JSON.stringify(row)))
      this.$set(this, 'dataModelNameEdit', row.name)
      this.$set(this.dataPageList[index], 'dataModelNameEditShow', true)
      this.$nextTick(()=>{
        this.$refs['editDataModelName'+index].focus()
      })
      this.$forceUpdate()
    },
    editNameSub () {
      if(this.dataModelNameEdit != this.dataModelRow.name) {
        editModelName(this.appInfo.id, this.dataModelNameEdit, this.dataModelRow.id).then(res => {
          if(res.data && res.data.code == 0) {
            this.$set(this.dataPageList[this.editIndex], 'name', this.dataModelNameEdit)
            this.dataModelNameEdit = ''
            this.dataModelRow = null
            this.$notify({
              title: '提示',
              message: '修改成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.$set(this.dataPageList[this.editIndex], 'dataModelNameEditShow', false)
          }
        })
      }else{
        this.dataModelNameEdit = ''
        this.dataModelRow = null
        this.$set(this.dataPageList[this.editIndex], 'dataModelNameEditShow', false)
      }
    },
    // 获取数据集列表
    getDataModelList(id) {
      this.tableLoading = true
      let obj={
        current: this.page.currentPage,
        size: this.page.pageSize,
        appId: this.appInfo.id
      }
      getDataModel(Object.assign(obj, this.queryParam)).then(res => {
        if(res.data && res.data.code == 0) {
          this.dataPageList = [...res.data.data.records]
          this.page.total = res.data.data.total
          this.page.currentPage = res.data.data.current
          this.tableLoading = false
        } else {
          this.tableLoading = false
        }
      }).catch(err => {
        this.tableLoading = false
      })
    },
    searchChange (form) {
      this.queryParam = form
      this.getDataModelList()
    },
    editNo (row, index) {
      this.editIndex = index
      this.$set(this, 'dataModelRow', JSON.parse(JSON.stringify(row)))
      this.$set(this, 'dataModelNoEdit', row.collectionName)
      this.$set(this.dataPageList[index], 'dataModelNoEditShow', true)
      this.$nextTick(()=>{
        this.$refs['editDataModelNo'+index].focus()
      })
      this.$forceUpdate()
    },
    editNoSub () {
      if(this.dataModelNoEdit != this.dataModelRow.collectionName) {
        editModelNo(this.appInfo.id, this.dataModelRow.id, {collectionName: this.dataModelNoEdit}).then(res => {
          if(res.data && res.data.code == 0) {
            this.$set(this.dataPageList[this.editIndex], 'collectionName', this.dataModelNoEdit)
            this.dataModelNoEdit = ''
            this.dataModelRow = null
            this.$notify({
              title: '提示',
              message: '修改成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.$set(this.dataPageList[this.editIndex], 'dataModelNoEditShow', false)
          }
        })
      }else{
        this.dataModelNoEdit = ''
        this.dataModelRow = null
        this.$set(this.dataPageList[this.editIndex], 'dataModelNoEditShow', false)
      }
    },
    deleteDataModel (row) {
      this.$confirm("确认删除此模型？", '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        delModel(row.appId, row.id).then(res => {
          if(res.data && res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: '删除成功',
              position: 'bottom-right',
              type: 'success'
            })
            this.getDataModelList()
          }
        })
      })
    },
    addEditHandle (row) {
      let addEditForm = {
        fields: []
      }
      if(row) {
        if(row.id) {
          this.$set(addEditForm, 'id', row.id)
        }
        if(row.name) {
          this.$set(addEditForm, 'name', row.name)
        }
        if(row.menuId) {
          this.$set(addEditForm, 'menuId', row.menuId)
        }
        if(row.fieldList && row.fieldList.length > 0) {
          let tlist = []
          row.fieldList.filter(fit => {
            tlist.push({
              fieldKey: fit.fieldKey,
              fieldName: fit.fieldName,
              fieldType: fit.fieldType
            })
          })
          this.$set(addEditForm, 'fields', tlist)
        }
      }
      this.$refs.createDataModelDialog.init(addEditForm)
    },
    genRowHandle (row) {
      this.$confirm('此操作将生成新的设计,是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        genCrudDesign(this.appInfo.id, row.id).then(res => {
          this.$notify({
            title: '提示',
            message: '生成设计成功',
            position: 'bottom-right',
            type: 'success'
          })
          this.getDataModelList(this.appInfo.id)
          eventBus.$emit('freshAllMenu', true)
        })
      }).catch(() => {
      })
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
  }
}
</script>

<style lang="scss" scoped>
.data-set{
  margin-top: 15px;
  .add-button{
    height: 36px;
    background: #1E6FFF;
    border-radius: 4px;
    box-sizing: border-box;
    font-size: 14px;
    font-family: Microsoft YaHei, Microsoft YaHei;
    font-weight: 400;
    color: #FFFFFF;
  }
  .jvs-table.data-set-table-list/deep/{
    .el-card__body{
      .search-form{
        padding-bottom: 0;
        .el-col{
          margin-bottom: 0;
        }
      }
    }
    .el-table__body-wrapper{
      height: calc(100vh - 295px - 212px - 28px - 66px)!important;
    }
  }
  /deep/.jvs-table.hide-top-jvs-table{
    .jvs-table-titleTop{
      display: none;
    }
    .table-body-box{
      padding: 0;
      .el-table{
        .el-table__header-wrapper{
          tr{
            th{
              background: #f6f6f6;
            }
          }
        }
      }
    }
    .el-table__body-wrapper{
      height: calc(100vh - 295px - 84px)!important;
    }
  }
}
.add-edit-model-box{
  /deep/.jvs-form{
    margin-top: 16px;
    .el-form-item:not(.form-btn-bar) {
      margin-left: 20px;
      margin-right: 20px;
    }
    .el-form-item__label{
      font-family: Microsoft YaHei-Bold, Microsoft YaHei;
      font-weight: 700;
      font-size: 14px;
      color: #363B4C;
    }
    .form-column-tableForm{
      margin-bottom: 0;
      .el-form-item{
        .jvs-form-item{
          padding: 16px;
          border-radius: 4px;
          background: #F5F6F7;
          overflow: hidden;
        }
        .table-form{
          .el-form-item{
            margin: 0;
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
      .el-form-item__error{
        top: calc(100% - 18px);
        left: unset;
        right: 2px;
      }
    }
    .form-item-btn{
      border-top: 1px solid #EEEFF0;
      height: 60px;
      line-height: 60px;
      .form-btn-bar{
        height: 100%;
        margin: 0;
        margin-right: 24px;
        display: flex;
        justify-content: flex-end;
        align-items: center;
        .el-form-item__content{
          line-height: 60px;
          display: flex;
          flex-direction: row-reverse;
          .el-button{
            margin-left: 16px;
          }
        }
      }
    }
  }
}
.el-dialog__wrapper{
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
        height: 100%;
        .el-alert--warning{
          height: 36px;
          box-sizing: border-box;
        }
      }
    }
  }
  .jvs-table.hide-top-jvs-table/deep/{
    height: 100%;
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
</style>
<style lang="scss">
.step-content{
  padding: 30px 100px;
  height: 350px;
  .el-table .el-table__body-wrapper{
    height: 300px;
  }
  .step-process{
    margin-top: 100px;
  }
  .step-process-text{
    margin-top: 10px;
    font-size: 16px;
    font-weight: bold;
    width: 100%;
    text-align: center;
  }
}
</style>
