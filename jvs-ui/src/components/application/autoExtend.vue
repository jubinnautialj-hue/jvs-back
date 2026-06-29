<template>
  <div class="auto-extend-page">
    <div v-if="$functionEnable('集成自动化扩展')" class="extend-content">
      <div class="extend-tab">
        <div :class="{'extend-tab-item': true, 'active': !currentType}" @click="searchTypeChange('')">默认</div>
        <div v-for="ritem in ruleGroupList" :key="ritem.value" :class="{'extend-tab-item': true, 'active': ritem.value == currentType}" @click="searchTypeChange(ritem.value)">{{ritem.key}}</div>
      </div>
      <div class="extend-right">
        <div v-if="permissionsList.indexOf('jvs_rule_extend_add') > -1" class="extend-btn">
          <jvs-button type="primary" permisionFlag="jvs_rule_extend_add" @click="handleAddExtend">http扩展</jvs-button>
          <jvs-button type="primary" permisionFlag="jvs_rule_extend_add" @click="webserviceHandle">webservice扩展</jvs-button>
        </div>
        <div class="default-extend-list">
          <jvs-table
          :option="tableOption"
          :loading="tableLoading"
          :data="tableData"
          :page="page"
          @on-load="getExtend"
          @search-change="searchChange"
        >
          <template slot="name" slot-scope="scope">
            <div class="icon-name">
              <svg aria-hidden="true">
                <use :xlink:href="`#${scope.row.icon}`"></use>
              </svg>
              <span>{{scope.row.name}}</span>
            </div>
          </template>
          <template slot="status" slot-scope="scope">
            <div :class="{'status-tag': true, 'enable': scope.row.status}">
              <div class="dot"></div>
              <div>{{scope.row.status === false ? '未启用' : '启用'}}</div>
            </div>
          </template>
          <template slot="flag" slot-scope="scope">
            <span>{{scope.row.id ? '否' : '是'}}</span>
          </template>
          <template slot="menu" slot-scope="scope">
            <span>
              <el-button v-if="!scope.row.id" type="text" @click="handleEditExtend(scope.row, 'view')">查看</el-button>
              <el-button v-if="permissionsList.indexOf('jvs_rule_extend_edit') > -1 && scope.row.id" type="text" :style="currentType ? '' : 'color: #D7D8DB;'" @click="handleEditExtend(scope.row)">编辑</el-button>
              <el-button v-if="permissionsList.indexOf('jvs_rule_extend_delete') > -1 && scope.row.id" type="text" :style="currentType ? 'color: #FF194C;' : 'color: #D7D8DB;'" @click="handleDel(scope.row)">删除</el-button>
            </span>
          </template>
        </jvs-table>
      </div>
      
      </div>
    </div>
    <div v-else class="no-permission-img">
      <img src="@/const/img/noPermission.jpg" alt=""/>
    </div>
    <el-dialog
      :class="{'function-oprate-dialog': true, 'webservice': dialogType == 'webservice'}"
      :title="title"
      :visible.sync="dialogVisible"
      append-to-body
      :close-on-click-modal="false"
      :before-close="handleClose">
      <div v-if="dialogVisible" style="width: 100%;height: 100%;">
        <div v-if="dialogType == 'webservice'" class="function-oprate-dialog-body webservice-body">
          <div class="top">
            <jvs-form ref="webForm" :option="webserviceOption" :formData="webserviceForm" @submit="webSubmit"></jvs-form>
          </div>
          <div class="footer">
            <div class="footer-button">
              <div class="ftb" @click="handleClose">取消</div>
              <el-button class="ftb submit" :loading="option.submitLoading" @click="webSubmitHandle">确定</el-button>
            </div>
          </div>
        </div>
        <div v-else class="function-oprate-dialog-body">
          <div class="top">
            <jvs-form ref="extendForm" :option="option" :formData="formData" @submit="extendFormSubmit" @cancalClick="handleClose">
              <template slot="ruleGroupForm">
                <div>
                  <div style="display:flex;align-items:center;">
                    <el-select v-model="formData.ruleGroup" size="mini" clearable filterable allow-create placeholder="类型分组" @change="ruleGroupChange" style="width: 100%;">
                      <el-option v-for="item in ruleGroupList" :label="item.key" :value="item.value" :key="item.value"></el-option>
                    </el-select>
                  </div>
                </div>
              </template>
            </jvs-form>
          </div>
          <div class="slider-bar"></div>
          <div class="other-set">
            <div class="tab">
              <div :class="{'tab-item': true, 'active': currentTab == 'request'}" @click="currentTab='request';">接口请求</div>
              <div :class="{'tab-item': true, 'active': currentTab == 'response'}" @click="currentTab='response';">接口返回</div>
            </div>
            <div v-show="currentTab == 'request'" class="other-set-body">
              <jvs-form ref="otherForm" :option="otherOption" :formData="formData" @formChange="otherChange" @submit="otherSubmit">
                <template slot="urlLabel">
                  <span style="display: inline-block;height: 18px;line-height: 18px;">接口地址<span style="margin-left: 10px;color: #6F7588;font-size: 12px;font-family: Microsoft YaHei-Regular, Microsoft YaHei;;font-weight: 400;">(支持外部接口 http:// 和微服务lb://test/index 和环境变量<span>{</span>{变量名}})</span></span>
                </template>
              </jvs-form>
              <div class="other-set-tab">
                <div v-if="!(formData.ruleGroup == 'webservice')" :class="{'extend-tab-item': true, 'active': otherSetTab == 'Query'}" @click="otherSetTab='Query';">Query</div>
                <div v-if="['POST', 'PUT'].indexOf(formData.method) > -1 || (formData.ruleGroup == 'webservice')" :class="{'extend-tab-item': true, 'active': otherSetTab == 'Body'}" @click="otherSetTab='Body';">Body</div>
                <div :class="{'extend-tab-item': true, 'active': otherSetTab == 'Headers'}" @click="otherSetTab='Headers';">Headers</div>
                <div v-if="!(formData.ruleGroup == 'webservice')" :class="{'extend-tab-item': true, 'active': otherSetTab == 'Path'}" @click="otherSetTab='Path';">Path</div>
              </div>
              <div v-if="(['POST', 'PUT'].indexOf(formData.method) > -1 || formData.ruleGroup == 'webservice') && otherSetTab == 'Body'" class="other-body-div">
                <div style="height: 200px;position: relative;border-radius: 4px;overflow: hidden;">
                  <jsonEditor lang="jsonArray" :disabled="option.disabled" :code="formData.body" prop="body" @change="jsonChange"></jsonEditor>
                </div>
                <div style="margin: 16px 0;">
                  <el-button type="primary" :disabled="option.disabled" size="mini" @click="formatBodyList('body')">解析body</el-button>
                </div>
                <div class="body-list-box">
                  <div class="tip">以解析后的字段为准</div>
                  <table-tree
                    :option="Object.assign(bodyOption, {disabled: option.disabled})"
                    :formData="formData"
                    prop="fieldList">
                  </table-tree>
                </div>
              </div>
              <div>
                <div v-show="otherSetTab == 'Query'" class="query-list">
                  <div class="header">
                    <span class="el-input">参数名称</span>
                    <span class="el-input">参数显示</span>
                    <span class="el-input">参数描述</span>
                    <span class="el-input">测试值</span>
                    <span style="width: 50px;">必填</span>
                    <span style="width: 50px;">缓存</span>
                    <span v-if="!option.disabled" style="width: 16px;"></span>
                  </div>
                  <div v-if="formData.queryList && formData.queryList.length > 0" class="body">
                    <div v-for="(query, qix) in formData.queryList" :key="'query-item-'+qix" class="body-item">
                      <el-input v-model="query.key" :disabled="option.disabled" placeholder="请输入参数名称" size="mini"></el-input>
                      <el-input v-model="query.label" :disabled="option.disabled" placeholder="请输入参数显示" size="mini"></el-input>
                      <el-input v-model="query.explain" :disabled="option.disabled" placeholder="请输入参数描述" size="mini"></el-input>
                      <el-input v-model="query.testValue" :disabled="option.disabled" placeholder="请输入测试值" size="mini"></el-input>
                      <span style="width: 50px;">
                        <el-checkbox v-model="query.necessity" :disabled="option.disabled" size="mini"></el-checkbox>
                      </span>
                      <span style="width: 50px;">
                        <el-checkbox v-model="query.cache" size="mini"></el-checkbox>
                      </span>
                      <div v-if="!option.disabled" class="delete-icon-button" @click="deleteLine(qix, 'queryList')">
                        <span class="border-line"></span>
                      </div>
                    </div>
                  </div>
                  <div v-if="!option.disabled" class="bottom-button" @click="addLineHandle('queryList')">
                    <div class="button">
                      <div class="icon">
                        <svg aria-hidden="true">
                          <use xlink:href="#jvs-ui-icon-xinjian"></use>
                        </svg>
                      </div>
                      <span>新增一行</span>
                    </div>
                  </div>
                </div>
                <div v-show="otherSetTab == 'Headers'" class="header-list">
                  <div class="header">
                    <span class="el-input">参数名称</span>
                    <span class="el-input">参数显示</span>
                    <span class="el-input">参数描述</span>
                    <span class="el-input">测试值</span>
                    <span style="width: 50px;">必填</span>
                    <span style="width: 50px;">缓存</span>
                    <span v-if="!option.disabled" style="width: 16px;"></span>
                  </div>
                  <div v-if="formData.headerList && formData.headerList.length > 0" class="body">
                    <div v-for="(query, qix) in formData.headerList" :key="'query-item-'+qix" class="body-item">
                      <el-input v-model="query.key" :disabled="option.disabled" placeholder="请输入参数名称" size="mini"></el-input>
                      <el-input v-model="query.label" :disabled="option.disabled" placeholder="请输入参数显示" size="mini"></el-input>
                      <el-input v-model="query.explain" :disabled="option.disabled" placeholder="请输入参数描述" size="mini"></el-input>
                      <el-input v-model="query.testValue" :disabled="option.disabled" placeholder="请输入测试值" size="mini"></el-input>
                      <span style="width: 50px;">
                        <el-checkbox v-model="query.necessity" :disabled="option.disabled" size="mini"></el-checkbox>
                      </span>
                      <span style="width: 50px;">
                        <el-checkbox v-model="query.cache" size="mini"></el-checkbox>
                      </span>
                      <div v-if="!option.disabled" class="delete-icon-button" @click="deleteLine(qix, 'headerList')">
                        <span class="border-line"></span>
                      </div>
                    </div>
                  </div>
                  <div v-if="!option.disabled" class="bottom-button" @click="addLineHandle('headerList')">
                    <div class="button">
                      <div class="icon">
                        <svg aria-hidden="true">
                          <use xlink:href="#jvs-ui-icon-xinjian"></use>
                        </svg>
                      </div>
                      <span>新增一行</span>
                    </div>
                  </div>
                </div>
                <div v-show="otherSetTab == 'Path'" class="header-list">
                  <div class="header">
                    <span style="width: 20%;">参数名称</span>
                    <span class="el-input">参数显示</span>
                    <span class="el-input">参数描述</span>
                    <span class="el-input">测试值</span>
                    <span style="width: 50px;">缓存</span>
                  </div>
                  <div v-if="formData.pathList && formData.pathList.length > 0" class="body">
                    <div v-for="(query, qix) in formData.pathList" :key="'query-item-'+qix" class="body-item">
                      <span style="width: 20%;">{{query.key}}</span>
                      <el-input v-model="query.label" :disabled="option.disabled" placeholder="请输入参数显示" size="mini"></el-input>
                      <el-input v-model="query.explain" :disabled="option.disabled" placeholder="请输入变量描述" size="mini"></el-input>
                      <el-input v-model="query.testValue" :disabled="option.disabled" placeholder="请输入测试值" size="mini"></el-input>
                      <span style="width: 50px;">
                        <el-checkbox v-model="query.cache" size="mini" @change="pathCacheChange(query.cache)"></el-checkbox>
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div v-show="currentTab == 'response'" class="other-set-body">
              <div style="margin-bottom: 8px;display: flex;align-items: center;">
                <span style="font-family: Microsoft YaHei-Regular, Microsoft YaHei;font-weight: 400;font-size: 14px;color: #363B4C;margin-right: 10px;">是否启用mock</span>
                <el-switch v-model="formData.enableMock"></el-switch>
              </div>
              <div class="other-body-div">
                <div style="height: 200px;position: relative;border-radius: 4px;overflow: hidden;">
                  <jsonEditor lang="jsonArray" :disabled="option.disabled" :code="formData.response" prop="response" @change="jsonChange"></jsonEditor>
                </div>
                <div style="margin: 16px 0;">
                  <el-button type="primary" :disabled="option.disabled" size="mini" @click="formatBodyList('response')">解析数据</el-button>
                </div>
                <div class="body-list-box">
                  <div class="tip">以解析后的字段为准</div>
                  <table-tree
                    :option="Object.assign(responseOption, {disabled: option.disabled})"
                    :formData="formData"
                    prop="responseList">
                  </table-tree>
                </div>
              </div>
            </div>
          </div>
          <div v-if="testResult" class="center">
            <div class="jvs-form">
              <div class="el-form-item" style="display: flex;align-items: center;">
                <div class="el-form-item__label" style="flex: 1;text-align: left;">测试结果</div>
                <div>
                  <el-button icon="el-icon-document-copy" type="text" @click="copyTestJsonHandle(testResult)" style="margin-left: 10px;cursor: pointer;">复制</el-button>
                </div>
              </div>
              <div class="result-text">
                <p v-if="testResult == 'number'" style="padding:10px;margin:0;">{{testResult}}</p>
                <json-viewer v-else :value="testResult || ''"></json-viewer>
              </div>
            </div>
          </div>
          <div v-if="!option.disabled" class="footer">
            <div class="footer-button">
              <div class="ftb" @click="handleClose">取消</div>
              <el-button class="ftb test" :loading="testLoading" @click="testHandle">测试</el-button>
              <el-button class="ftb submit" :loading="option.submitLoading" @click="submitHandle">确定</el-button>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {addExtend, delExtend, editExtend, getExtendRule, getExtendTypes, getPlatRuleGrouop, testRule, parseWSDL, getDeleteCachesOptions} from "@/api/application";
import getIconLib from "@/plugin/iconLib";
import { getStore } from "@/util/store.js"
import jsonEditor from '@/components/basic-assembly/jsonEditor'
import tableTree from '@/components/basic-assembly/tableTree';
import { getRegExpList } from '@/views/rule/api/rule'
export default {
  name: "autoExtend",
  components: {
    jsonEditor,
    tableTree
  },
  props: {
    useByComponent: {
      type: Boolean
    }
  },
  data () {
    return {
      extendTypes: [],
      option: {
        emptyBtn: false,
        formAlign: 'top',
        submitLoading: false,
        btnHide: true,
        column: [
          {
            label: '扩展名称',
            prop: 'name',
            span: 12,
            rules: [
              { required: true, message: '请输入名称', trigger: 'blur' }
            ],
          },
          {
            label: '类型分组',
            prop: 'ruleGroup',
            type: 'select',
            formSlot: true,
            span: 12,
            rules: [
              { required: true, message: '请选择或输入类型分组', trigger: ['blur', 'change'] }
            ],
          },
          {
            label: "图标",
            prop: "icon",
            type: 'iconSelect',
            span: 12,
          },
          {
            label: '缓存时间',
            prop: 'cacheTime',
            type: 'inputNumber',
            span: 12,
            unit: '分钟',
            controlsposition: 'right'
          },
          {
            label: '需要清除的插件',
            prop: 'deleteCaches',
            type: 'select',
            // formSlot: true,
            span: 12,
            dicData: [],
            props: {
              label: 'label',
              value: 'value',
              secTitle: 'type'
            } 
          },
          {
            label: '是否启用',
            prop: 'status',
            type: 'switch',
            span: 12,
          },
          {
            label: '描述',
            type: 'textarea',
            rows: 1,
            prop: 'explainInfo',
            rules: [
              { required: true, message: '请输入描述', trigger: 'blur' }
            ],
          },
        ],
      },
      formData: {
        fieldList: [],
        responseList: [],
        method: 'GET',
        cacheTime: 0,
      },
      dialogVisible: false,
      title: '',
      ruleGroupList: [],
      ruleGroupUrl: '',
      permissionsList: [],
      currentType: '',
      tableData: [],
      tableLoading: false,
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
      },
      queryParams: {},
      tableOption: {
        addBtn: false,
        viewBtn: false,
        editBtn: false,
        delBtn: false,
        menuWidth: '120px',
        page: true,
        search: true,
        column: [
          {
            label: '名称',
            prop: 'name',
            slot: true,
            search: true
          },
          {
            label: '描述',
            prop: 'explainInfo',
            type: 'textarea'
          },
          {
            label: '是否启用',
            prop: 'status',
            slot: true,
            width: '120px'
          },
          {
            label: '是否内置',
            prop: 'flag',
            slot: true,
            width: '100px'
          },
        ]
      },
      currentTab: 'request',
      otherOption: {
        btnHide: true,
        formAlign: 'top',
        column: [
          {
            label: '接口地址',
            prop: 'url',
            rules: [
              { required: true, message: '请输入接口地址', trigger: 'blur' }
            ],
            labelSlot: true,
          },
          {
            label: '请求方式',
            prop: 'method',
            type: 'radio',
            dicData: [
              {label: 'GET', value: 'GET'},
              {label: 'POST', value: 'POST'},
              {label: 'DELETE', value: 'DELETE'},
              {label: 'PUT', value: 'PUT'}
            ],
            rules: [
              { required: true, message: '请选择请求方式', trigger: 'change' }
            ],
            display: false
          },
          {
            label: '命名空间',
            prop: 'namespace',
            rules: [
              { required: true, message: '请输入命名空间', trigger: 'change' }
            ],
            display: false
          }
        ]
      },
      otherSetTab: 'Query',
      testResult: null,
      testLoading: false,
      validateExtend: false,
      validateOther: false,
      bodyOption: {
        rowKey: 'path',
        pathKey: 'key',
        hasChildren: {
          prop: 'inputType',
          value: 'array,object',
          add: 'object'
        },
        childrenType: 'childrenType',
        itemsPathEnd: '[0]',
        column: [
          {
            label: '字段名称',
            prop: 'key',
            width: '200px'
          },
          {
            label: '显示名称',
            prop: 'label'
          },
          {
            label: '字段描述',
            prop: 'explain'
          },
          {
            label: '字段类型',
            prop: 'inputType',
            type: 'select',
            dicData: [
              {label: 'string', value: 'string'},
              {label: 'integer', value: 'integer'},
              {label: 'boolean', value: 'boolean'},
              {label: 'array', value: 'array'},
              {label: 'object', value: 'object'},
              {label: 'number', value: 'number'},
              {label: 'file', value: 'file'}
            ]
          },
          {
            label: '必填',
            prop: 'necessity',
            type: 'switch',
            showCheck: true,
            disabledRow: true,
            width: '50px'
          },
          {
            label: '缓存key',
            prop: 'cache',
            type: 'switch',
            showCheck: true,
            disabledRow: true,
            width: '80px'
          },
          {
            label: '默认值',
            prop: 'defaultValue',
            disabledRow: true
          },
          {
            label: '校验规则',
            prop: 'rule',
            disabledRow: true,
            clearable: true,
            type: 'select',
            dicData: [],
            props: {
              label: 'name',
              value: 'expression'
            }
          },
        ]
      },
      responseOption: {
        rowKey: 'path',
        pathKey: 'key',
        hasChildren: {
          prop: 'inputType',
          value: 'array,object',
          add: 'object'
        },
        childrenType: 'childrenType',
        itemsPathEnd: '[0]',
        column: [
          {
            label: '字段名称',
            prop: 'key',
            width: '200px'
          },
          {
            label: '显示名称',
            prop: 'label'
          },
          {
            label: '字段描述',
            prop: 'explain'
          },
          {
            label: '字段类型',
            prop: 'inputType',
            type: 'select',
            dicData: [
              {label: 'string', value: 'string'},
              {label: 'integer', value: 'integer'},
              {label: 'boolean', value: 'boolean'},
              {label: 'array', value: 'array'},
              {label: 'object', value: 'object'},
              {label: 'number', value: 'number'}
            ]
          },
          {
            label: '必填',
            prop: 'necessity',
            type: 'switch',
            showCheck: true,
            disabledRow: true,
            width: '50px'
          },
          {
            label: '成功标志位',
            prop: 'cache',
            type: 'switch',
            showCheck: true,
            disabledRow: true,
            width: '100px'
          },
          {
            label: '成功条件',
            prop: 'condition',
            type: 'select',
            dicData: [
              {label: '等于空', value: 'null'},
              {label: '等于true', value: true},
              {label: '等于false', value: false},
              {label: '等于200', value: 200},
              {label: '等于0', value: 0},
            ]
          },
        ]
      },
      dialogType: '',
      webserviceForm: {},
      webserviceOption: {
        btnHide: true,
        formAlign: 'top',
        submitLoading: true,
        column: [
          {
            label: '解析地址',
            prop: 'url',
            type: 'textarea',
            rules: [
              { required: true, message: '请输入解析地址', trigger: 'change' }
            ],
          }
        ]
      }
    }
  },
  async created() {
    this.permissionsList = getStore({name: 'permissions'})
    await getIconLib()
    await this.getExtendTypes()
    this.getRegExpListHandle()
  },
  methods: {
    // 新增一行数据
    addField() {
      this.isEdit = false
      this.formData.fieldList.push({
        id: new Date().getTime(),
        necessity: false,
        inputType: '',
        explain: '',
        key: ''
      })
    },
    // 获取参数类型
    getExtendTypes() {
      getExtendTypes().then(res => {
        if (res.data && res.data.code == 0 && res.data.data) {
          this.extendTypes = [...res.data.data]
        }
      })
      getPlatRuleGrouop().then(res => {
        if (res.data && res.data.code == 0 && res.data.data) {
          this.ruleGroupList = res.data.data
        }
      })
      getDeleteCachesOptions().then(res => {
        if (res.data && res.data.code == 0 && res.data.data) {
          let temp = []
          res.data.data.filter(rit => {
            temp.push({
              label: rit.name,
              value: rit.name,
              type: rit.type || ''
            })
          })
          this.option.column.filter(col => {
            if(col.prop == 'deleteCaches') {
              col.dicData = temp
            }
          })
        }
      })
    },
    getExtend() {
      let query = {
        current: this.page.currentPage,
        size: this.page.pageSize
      }
      if(this.currentType) {
        query.ruleGroup = this.currentType
      }
      let temp = JSON.parse(JSON.stringify(this.queryParams))
      this.tableLoading = true
      getExtendRule(Object.assign(query, temp)).then(res => {
        if(res.data && res.data.code == 0 && res.data.data) {
          this.tableData = res.data.data.records
          this.page.total = res.data.data.total
          this.page.currentPage = res.data.data.current || 1
          this.tableLoading = false
        }
      })
    },
    searchChange (form) {
      this.queryParams = form
      this.getExtend()
    },
    handleAddExtend() {
      this.option.disabled = false
      this.otherOption.disabled = false
      this.title = '新建扩展'
      this.otherOption.column.filter(col => {
        if(col.prop == 'method') {
          col.display = true
        }
        if(col.prop == 'namespace') {
          col.display = false
        }
      })
      this.dialogVisible = true
    },
    // 删除扩展
    handleDel(obj) {
      this.$confirm('确认删除？').then(_ => {
        delExtend(obj).then(res => {
          if(res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: '删除成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.getExtend()
          }
        })
      }).catch(_ => {})
    },
    handleEditExtend(obj, optype) {
      if (obj.fieldLists) {
        delete obj.fieldLists
      }
      if(!obj.responseList) {
        obj.responseList = []
      }
      this.formData = JSON.parse(JSON.stringify(obj))
      this.formData.fieldList = obj.fieldList || []
      let id = new Date().getTime()
      this.formData.fieldList.forEach(item => {
        item.id = id
        id++
      })
      if(optype == 'view') {
        this.option.disabled = true
        this.otherOption.disabled = true
      }else{
        this.option.disabled = false
        this.otherOption.disabled = false
        this.title = '编辑扩展'
      }
      this.otherOption.column.filter(col => {
        if(col.prop == 'method') {
          col.display = (obj.ruleGroup == 'webservice' ? false : true)
        }
        if(col.prop == 'namespace') {
          col.display = (obj.ruleGroup == 'webservice' ? true : false)
        }
      })
      if(obj.ruleGroup == 'webservice') {
        this.otherSetTab = 'Body'
      }
      this.dialogVisible = true
      this.isEdit = true
    },
    handleClose() {
      this.formData = {
        fieldList: []
      }
      this.currentTab = 'request'
      this.otherSetTab = 'Query'
      this.dialogVisible = false
      this.ruleGroupUrl = ''
      this.dialogType = ''
      this.webserviceForm = {}
    },
    submitHandle () {
      this.$refs.extendForm.submitForm('ruleForm')
      this.$refs.otherForm.submitForm('ruleForm')
      if(this.validateExtend && this.validateOther) {
        this.submitForm(this.formData)
      }
    },
    extendFormSubmit (form) {
      this.validateExtend = true
    },
    submitForm(form) {
      if(this.isEdit) {
        editExtend(this.formData).then(res => {
          if (res.data && res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: '修改成功',
              position: 'bottom-right',
              type: 'success'
            });
            if(this.useByComponent) {
              this.$emit('freshList')
            }else{
              this.getExtend()
            }
            this.dialogVisible = false
          }
        })
      } else {
        addExtend(this.formData).then(res => {
          if (res.data && res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: '新增成功',
              position: 'bottom-right',
              type: 'success'
            });
            if(this.useByComponent) {
              this.$emit('freshList')
            }else{
              this.getExtend()
            }
            this.dialogVisible = false
          }
        })
      }
    },
    ruleGroupChange () {
      this.ruleGroupUrl = ''
      this.ruleGroupList.filter(item => {
        if(item.value == this.formData.ruleGroup) {
          this.ruleGroupUrl = item.url
        }
      })
    },
    searchTypeChange (type) {
      if(type != this.currentType) {
        this.page.currentPage = 1
        this.currentType = type
        this.getExtend()
      }
    },
    otherSubmit () {
      this.validateOther = true
    },
    addLineHandle (key) {
      if(!this.formData[key]) {
        this.$set(this.formData, key, [])
      }
      this.formData[key].push({})
      this.$forceUpdate()
    },
    deleteLine (index, key) {
      this.formData[key].splice(index, 1)
      this.$forceUpdate()
    },
    jsonChange (con, prop) {
      if(con == 'error') {
        this.$set(this.formData, prop, '')
      }else{
        this.$set(this.formData, prop, con)
      }
    },
    formatBodyList (prop) {
      let list = []
      if(this.formData[prop]) {
        let json = JSON.parse(this.formData[prop])
        this.eachObject(json, list)
        this.$set(this.formData, `${prop == 'body' ? 'field' : prop}List`, list)
      }
      this.$forceUpdate()
    },
    eachObject(obj, list, parent, parentItem) {
      if(typeof obj == 'object' && (obj instanceof Array)) {
        for(let index in obj) {
          if((obj[index] instanceof Array) ? (parentItem.childrenType == 'array') : (parentItem.childrenType == typeof obj[index])) {
            this.eachObject(obj[index], list, parent)
          }
        }
      }else{
        for(let k in obj) {
          if(typeof obj[k] == 'object' && (obj[k] instanceof Array)) {
            let tp = {
              explain: '',
              key: k
            }
            if(parent) {
              tp.path = (parent + '.' + k)
            }
            tp.inputType = 'array'
            tp.disabled = true
            if(obj[k].length > 0) {
              tp.childrenType = typeof obj[k][0]
              if(typeof obj[k][0] == 'object' && obj[k][0] instanceof Array) {
                tp.childrenType = 'array'
              }
              if(tp.childrenType == 'object') {
                let items = {path: tp.path+'[0]',key: 'Items', inputType: tp.childrenType, children: [], childrenType: tp.childrenType}
                this.eachObject(obj[k], items.children, items.path, items)
                tp.children = [items]
              }else if(tp.childrenType == 'array') {
                let items = {path: tp.path+'[0]',key: 'Items', inputType: tp.childrenType, children: [], childrenType: tp.childrenType}
                let citem = {path: items.path+'[0]',key: 'Items', children: []}
                if(obj[k][0] && obj[k][0].length > 0) {
                  if(typeof obj[k][0][0] == 'object' && obj[k][0][0] instanceof Array) {
                    citem.inputType = 'array'
                    citem.childrenType = 'array'
                  }else{
                    citem.inputType = typeof obj[k][0][0]
                    citem.childrenType = citem.inputType
                  }
                }
                for(let c in obj[k]) {
                  if(citem.childrenType == 'array' ? (obj[k][c] instanceof Array) : (typeof obj[k][c] == citem.childrenType)) {
                    this.eachObject(obj[k][c], citem.children, citem.path, citem)
                  }
                }
                items.children = [citem]
                tp.children = [items]
              }else{
                let items = {path: tp.path+'[0]',key: 'Items', inputType: tp.childrenType}
                tp.children = [items]
              }
            }
            let index = -1
            list.filter((li, lx) => {
              if(li.key == tp.key) {
                index = lx
              }
            })
            if(index == -1) {
              list.push(tp)
            }
          }else{
            let tp = {
              explain: '',
              key: k
            }
            if(parent) {
              tp.path = (parent + '.' + k)
            }else{
              tp.path = k
            }
            if(typeof obj[k] == 'object') {
              tp.disabled = true
              tp.inputType = 'object'
              let keys = Object.keys(obj[k])
              if(keys && keys.length > 0) {
                tp.children = []
                this.eachObject(obj[k], tp.children, tp.path ? tp.path : k)
              }
            }else{
              tp.inputType = (typeof obj[k])
            }
            let index = -1
            list.filter((li, lx) => {
              if(li.key == tp.key) {
                index = lx
              }
            })
            if(index == -1) {
              list.push(tp)
            }
          }
        }
      }
    },
    otherChange (form, item) {
      if(item.prop == 'method') {
        this.otherSetTab = 'Query'
      }
      if(item.prop == 'url' && this.formData.url) {
        let list = []
        let urlstr = this.formData.url
        if(urlstr.includes('?')) {
          let ix = urlstr.indexOf('?')
          urlstr = urlstr.slice(0, ix)
        }
        let urls = urlstr.split('/')
        this.$set(this.formData, 'pathList', [])
        for(let i in urls) {
          if(urls[i].includes('{')) {
            let str = urls[i].replace(/\{/g, ' ')
            str = str.replace(/\}/g, ' ')
            let tl = str.split(' ')
            tl.filter(tit => {
              if(tit && list.indexOf(tit) == -1) {
                list.push(tit)
              }
            })
          }
        }
        for(let l in list) {
          this.formData.pathList.push({
            key: list[l],
            explain: ''
          })
        }
        this.$forceUpdate()
      }
    },
    testHandle () {
      this.testLoading = true
      testRule(this.formData).then(res => {
        if(res.data && res.data.code == 0) {
          this.testResult = res.data.data
          this.testLoading = false
        }else{
          this.testLoading = false
        }
      }).catch(e => {
        this.testLoading = false
      })
    },
    // 复制测试结果
    copyTestJsonHandle (result) {
      const text = document.createElement('input')
      text.value = typeof result == 'string' ? result : JSON.stringify(result)
      document.body.appendChild(text)
      text.select()
      document.execCommand('Copy')
      document.body.removeChild(text)
      this.$notify({
        title: '提示',
        message: '复制成功',
        position: 'bottom-right',
        type: 'success'
      });
    },
    getRegExpListHandle () {
      getRegExpList().then(res => {
        if(res.data.code == 0 && res.data.data) {
          this.bodyOption.column.filter(item => {
            if(item.prop == 'rule') {
              item.dicData = res.data.data
            }
          })
        }
      })
    },
    pathCacheChange (bool) {
      this.formData.pathList.filter(path => {
        this.$set(path, 'cache', bool)
      })
      this.$forceUpdate()
    },
    webserviceHandle () {
      this.dialogType = 'webservice'
      this.title = '新建webservice扩展'
      this.dialogVisible = true
    },
    webSubmitHandle () {
      this.$refs.webForm.submitForm('ruleForm')
    },
    webSubmit () {
      this.webserviceOption.submitLoading = true
      parseWSDL(this.webserviceForm).then(res => {
        if(res && res.data && res.data.code == 0) {
          this.webserviceOption.submitLoading = false
          this.$notify({
            title: '提示',
            message: '新增成功',
            position: 'bottom-right',
            type: 'success'
          });
          this.handleClose()
          this.page.currentPage = 1
          this.currentType = 'webservice'
          this.getExtend()
        }else{
          this.webserviceOption.submitLoading = false
        }
      }).catch(e => {
        this.webserviceOption.submitLoading = false
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.auto-extend-page{
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  /deep/.extend-content{
    flex: 1;
    width: 100%;
    padding: 0;
    border-radius: 6px;
    background-color: #fff;
    display: flex;
    overflow: hidden;
    .extend-tab{
      width: 200px;
      height: 100%;
      overflow: hidden;
      overflow-y: auto;
      box-sizing: border-box;
      .extend-tab-item{
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
    .extend-right{
      margin-left: 20px;
      flex: 1;
      overflow: hidden;
      .default-extend-list{
        height: calc(100% - 46px);
      }
    }
    .title{
      font-weight: 600;
      font-size: 16px;
      margin-bottom: 16px;
    }
    .explain{
      font-size: 12px;
      margin-bottom: 20px;
      font-family: "Source Han Sans";
    }
    .extend-massage{
      width: 560px;
    }
    .extend-btn{
      margin: 20px 0;
      margin-top: 0;
    }
    .el-table__body-wrapper{
      height: 500px!important;
    }
    .table-top{
      display: none;
    }
    .default-extend-list{
      flex: 1;
      box-sizing: border-box;
      overflow: hidden;
      .jvs-table{
        width: 100%;
        height: 100%;
        .jvs-table-titleTop{
          height: unset;
        }
        .table-body-box{
          height: calc(100% - 68px - 56px);
          .el-table{
            height: 100%;
            .el-table__body-wrapper{
              height: calc(100% - 40px)!important;
            }
          }
        }
      }
      .extend-item{
        display: flex;
        border-radius: 4px;
        position: relative;
        border: 1px solid #e7e7e7;
        margin: 0 30px 30px 0;
        padding: 15px;
        //width: 145px;
        width: 45%;
        cursor: pointer;
        &:hover{
          border: 1px solid #3471ff;
          transition: 0.3s;
          .more-icon{
            transition: 0.3s;
            color: #3471ff;
          }
        }
        .more-icon{
          font-size: 14px;
          position: absolute;
          bottom: 2px;
          right: 6px;
          color: #ffffff;
        }
        .icon{
          background-color: #f1f3ff;
          padding: 6px;
          border-radius: 4px;
          width: 24px;
          height: 24px;
          fill: #606266;
          transition: 0.3s;
          margin-right: 10px;
        }
        .extend-item-text{
          height: 16px;
          color: #333333;
          font-size: 12px;
          width: 96px;
          word-wrap: break-word;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 1;
          -webkit-box-orient: vertical;
          margin-bottom: 4px;
        }
        .extend-item-explainInfo{
          color: #99a9bf;
          font-size: 12px;
          width: 94px;
          word-wrap: break-word;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 1;
          -webkit-box-orient: vertical;
        }
      }
    }
  }
  .no-permission-img{
    background-color: #FFFFFF;
    height: calc(100vh - 375px)!important;
  }
}
.auto-extend-page::-webkit-scrollbar{
  display: none;
}
.application-manage-tool-list{
  padding: 0;
  margin: 0;
  li{
    height: 32px;
    line-height: 32px;
    cursor: pointer;
    padding-left: 20px;
    text-align: left;
  }
  li:hover{
    background: #eff2f7;
  }
}
.icon-name{
  display: flex;
  align-items: center;
  svg{
    width: 32px;
    height: 32px;
    border-radius: 6px;
    margin-right: 16px;
  }
  span{
    font-family: Source Han Sans-Regular, Source Han Sans;
    font-weight: 400;
    font-size: 14px;
    color: #363B4C;
  }
}
.status-tag{
  display: flex;
  align-items: center;
  font-family: Source Han Sans-Regular, Source Han Sans;
  font-weight: 400;
  font-size: 14px;
  color: #6F7588;
  .dot{
    width: 8px;
    height: 8px;
    border-radius: 50%;
    overflow: hidden;
    margin-right: 6px;
    background: #C2C5CF;
  }
  &.enable{
    color: #36B452;
    .dot{
      background: #36B452;
    }
  }
}
.function-oprate-dialog.el-dialog__wrapper{
  /deep/.el-dialog{
    width: 1000px;
    height: 812px;
    margin-top: calc(50vh - 406px)!important;
    border-radius: 6px!important;
    overflow: hidden;
    .el-dialog__header{
      height: 48px;
      background: #F5F6F7;
      border-radius: 6px 6px 0px 0px;
      padding: 0 0 0 24px;
      .el-dialog__title{
        height: 18px;
        font-family: Microsoft YaHei-Bold, Microsoft YaHei;
        font-weight: 700;
        font-size: 14px;
        color: #363B4C;
        line-height: 18px;
      }
      .el-dialog__headerbtn{
        top: 10px;
        right: 17px;
        font-size: 20px;
        .el-dialog__close{
          color: #575E73;;
        }
      }
    }
    .el-dialog__header::before{
      display: none!important;
    }
    .el-dialog__body{
      height: calc(100% - 48px);
      padding: 0!important;
      .function-oprate-dialog-body{
        height: 100%;
        overflow: hidden;
        box-sizing: border-box;
        display: flex;
        flex-direction: column;
        .jvs-form{
          .el-row{
            .el-col-12{
              width: calc(50% - 12px);
            }
            .el-col-12:nth-of-type(2n) {
              margin-left: 24px;
            }
          }
          .el-form-item{
            .el-form-item__label{
              font-family: Microsoft YaHei-Bold, Microsoft YaHei;
              font-weight: 700;
              font-size: 14px;
              color: #363B4C;
            }
          }
          .el-col:nth-last-of-type(1) .el-form-item:not(.is-required){
            margin-bottom: 0;
          }
        }
        .top{
          margin-top: 16px;
          padding: 0 32px;
          box-sizing: border-box;
          display: flex;
          align-items: center;
          justify-content: space-between; 
        }
        .slider-bar{
          width: 100%;
          height: 8px;
          background: #F5F6F7;
        }
        .other-set{
          flex: 1;
          margin: 16px 32px;
          overflow: hidden;
          overflow-y: auto;
          .bottom-button{
            margin-top: 8px;
            .button{
              width: 80px;
              display: flex;
              align-items: center;
              cursor: pointer;
              .icon{
                width: 16px;
                height: 16px;
                background: #1E6FFF;
                border-radius: 4px;
                display: flex;
                align-items: center;
                justify-content: center;
                margin-right: 4px;
                svg{
                  width: 12px;
                  height: 12px;
                  fill: #fff;
                }
              }
              span{
                font-family: Microsoft YaHei-Regular, Microsoft YaHei;
                font-weight: 400;
                font-size: 14px;
                color: #1E6FFF;
                line-height: 18px;
              }
            } 
          }
          .delete-icon-button{
            width: 16px;
            height: 16px!important;
            background: #36B452;
            border-radius: 4px;
            display: flex;
            align-items: center;
            justify-content: center;
            box-sizing: border-box;
            cursor: pointer;
            .border-line{
              width: 10px;
              height: 2px;
              background: #fff;
              border-radius: 2px;
            }
          }
          .tab{
            display: flex;
            align-items: center;
            border-bottom: 1px solid #EEEFF0;
            .tab-item{
              width: 104px;
              height: 36px;
              line-height: 36px;
              text-align: center;
              background: #FFFFFF;
              border-radius: 4px 4px 0px 0px;
              border: 1px solid #EEEFF0;
              border-bottom: 0;
              cursor: pointer;
              &.active{
                font-family: Microsoft YaHei-Bold, Microsoft YaHei;
                font-weight: 700;
                font-size: 14px;
                color: #1E6FFF;
                background: #F5F6F7;
              }
              &+.tab-item{
                margin-left: 10px;
              }
            }
          }
          .other-set-body{
            background: #F5F6F7;
            border: 1px solid #EEEFF0;
            border-top: 0;
            padding: 16px;
            box-sizing: border-box;
            .jvs-form{
              .el-form-item__label{
                font-family: Microsoft YaHei-Regular, Microsoft YaHei;
                font-weight: 400;
                font-size: 14px;
                color: #363B4C;
              }
              .el-input__inner{
                background: #fff;
              }
            }
          }
          .other-set-tab{
            display: flex;
            align-items: center;
            border-bottom: 1px solid #EEEFF0;
            margin-bottom: 16px;
            .extend-tab-item{
              padding-bottom: 7px;
              font-family: Microsoft YaHei-Regular, Microsoft YaHei;
              font-weight: 400;
              font-size: 14px;
              color: #6F7588;
              cursor: pointer;
              position: relative;
              &+.extend-tab-item{
                margin-left: 40px;
              }
              &.active{
                font-weight: 700;
                color: #1E6FFF;
                &::after{
                  content: '';
                  position: absolute;
                  left: 0;
                  bottom: -1px;
                  width: 100%;
                  height: 2px;
                  background: #1E6FFF;
                  border-radius: 2px 0px 2px 0px;
                }
              }
            }
          }
          .query-list, .header-list{
            .header, .body-item{
              display: flex;
              align-items: center;
              overflow: hidden;
              .el-input{
                flex: 1;
                overflow: hidden;
                margin-right: 16px;
                .el-input__inner{
                  border: 0;
                }
              }
            }
            .body-item{
              margin-top: 8px;
            }
          }
          .other-body-div{
            .body-list-box{
              .tip{
                margin-bottom: 8px;
                height: 16px;
                font-family: Microsoft YaHei-Regular, Microsoft YaHei;
                font-weight: 400;
                font-size: 12px;
                color: #6F7588;
                line-height: 16px;
              }
              .el-table{
                .el-table__header-wrapper{
                  tr th{
                    background-color: #EEEFF0;
                  }
                }
                .el-table__body-wrapper{
                  .el-table__body{
                    .el-input__inner{
                      border: 0;
                      background: #F5F6F7;
                    }
                    .el-table__row:hover{
                      .el-table__cell{
                        background-color: #fff;
                      }
                    }
                  }
                }
              }
            }
          }
        }
        .center{
          padding: 0 32px;
          box-sizing: border-box;
          .jvs-form{
            .el-form-item{
              margin-bottom: 0;
            }
          }
          .result-text{
            max-height: 100px;
            overflow: hidden;
            overflow-y: auto;
            .jv-code{
              padding: 20px;
            }
          }
        }
        .footer{
          width: 100%;
          height: 60px;
          background: #FFFFFF;
          border-radius: 0px 0px 6px 6px;
          box-sizing: border-box;
          border-top: 1px solid #EEEFF0;
          display: flex;
          align-items: center;
          justify-content: flex-end;
          .footer-button{
            display: flex;
            align-items: center;
            .ftb{
              width: 60px;
              height: 32px;
              line-height: 32px;
              padding: 0;
              text-align: center;
              background: #F5F6F7;
              border-radius: 4px 4px 4px 4px;
              cursor: pointer;
              font-family: Microsoft YaHei-Regular, Microsoft YaHei;
              font-weight: 400;
              font-size: 14px;
              color: #363B4C;
              margin-right: 16px;
              cursor: pointer;
            }
            .submit{
              background: #1E6FFF;
              color: #fff;
            }
            .test{
              background: #36B452;
              color: #fff;
            }
          }
        }
      }
    }
  }
  &.webservice{
    /deep/.el-dialog{
      width: 500px;
      height: 400px;
      margin-top: calc(50vh - 200px)!important;
      border-radius: 6px!important;
      overflow: hidden;
      .webservice-body{
        display: flex;
        flex-direction: column;
        .top{
          display: block;
          flex: 1;
          overflow: hidden;
        }
      }
    }
  }
}
</style>
