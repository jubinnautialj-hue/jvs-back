<template>
  <div class="code-dev-page-list">
    <div class="tips" style="margin-top: 0;">
      <div class="title">
        <span class="el-icon-info" style="color: #1E6FF;font-size: 14px;"></span>
        <span style="margin-left: 8px;">源码开发标识管理，用于二次开发集成方便迁移</span>
      </div>
      <div class="con-box">
        <div class="secTitle">
          <span>温馨提示：</span>
        </div>
        <ul class="tips-list" style="margin-top: 0;">
          <li>
            <span class="dot"></span>
            <span>开发调试阶段，可以添加多个"应用开发人员"，添加人员不需要管理员审核</span>
          </li>
          <li>
            <span class="dot"></span>
            <span>源代码可以轻松集成到平台所使用的数据源中，同时也要了解数据模型和组件格式的要求</span>
          </li>
          <li>
            <span class="dot"></span>
            <span>支持 API 集成，数据模型提供了内部调用api，逻辑引擎提供了内部外部无权限api</span>
          </li>
          <li>
            <span class="dot"></span>
            <span>使用版本控制系统（如 Git）来管理你的源代码</span>
          </li>
          <li>
            <span class="dot"></span>
            <span>逻辑引擎标识支持RESTful API方式.（/xxx/xxx）</span>
          </li>
          <li>
            <span class="dot"></span>
            <span>调用方式为【请求地址】/标识</span>
          </li>
        </ul>
      </div>
    </div>
    <div class="app-header">
      <div style="display: flex;align-items: center">
        <svg class="icon" aria-hidden="true">
          <use xlink:href="#icon-jvs-rongqi"></use>
        </svg>
        <span>配置标识</span>
      </div>
    </div>
    <div class="app-info app-info-text">
      <div class="col-2">
        <div class="label">请求地址</div>
        <div class="con">
          <span>{{ (appIdent && appIdent.id) ? `${origin}/rule/openapi/${appIdent.identifier}/{ruleIdentify}` : `${origin}/rule/openapi/{appIdentifier}/{ruleIdentify}`}}</span>
          <b v-if="appIdent && appIdent.id" class="el-icon-copy-document" @click="copyStr((appIdent && appIdent.id) ? `${origin}/rule/openapi/${appIdent.identifier}/{ruleIdentify}` : `${origin}/rule/openapi/{appIdentifier}/{ruleIdentify}`)"></b>
        </div>
      </div>
      <div class="col-2">
        <div class="label">应用标识</div>
        <div v-if="appIdent && appIdent.identifier" class="con">
          <span>{{ (appIdent && appIdent.identifier) ? appIdent.identifier : ''}}</span>
          <span>
            <b class="el-icon-edit-outline" @click="setRowHandle(appIdent)" style="margin-right: 10px;"></b>
            <b class="el-icon-copy-document" @click="copyStr(appIdent.identifier)"></b>
          </span>
        </div>
        <div v-else class="con">
          <span></span>
          <b class="el-icon-edit-outline" @click="setRowHandle({designType: 'app'})"></b>
        </div>
      </div>
    </div>
    <div class="top-bottom-splite"></div>
    <div class="code-dev-page-bottom">
      <div v-if="!appIdent" style="position: relative;width: 100%;height: 100%;">
        <div style="width: 365px;position: absolute;left: 0;right: 0;margin: 0 auto;margin-top: -28px;transform: translateX(30px);">
          <img :src="emptyImg" alt="" >
          <div style="margin-top: 16px;text-indent: 86px;font-size: 16px;font-family: Microsoft YaHei, Microsoft YaHei;font-weight: 400;color: #363B4C;">请先配置应用标识</div>
        </div>
      </div>
      <div v-if="appIdent" style="height: 100%;">
        <div class="type-list">
          <div v-for="tp in typeList" :key="tp.value" :class="{'type-list-item': true, 'active': tp.value == activeType}" @click="typeChange(tp)">
            <span>{{tp.label}}</span>
          </div>
        </div>
        <jvs-table :page="page" :loading="tableLoading" :option="pageOption" :data="tableData" @on-load="getList" @search-change="searchChange">
          <template slot="menuLeft">
            <jvs-button type="primary" size="mini" @click="setRowHandle({designType: activeType})">新增</jvs-button>
          </template>
          <template slot="menu" slot-scope="scope">
            <jvs-button size="mini" type="text" @click="setRowHandle(scope.row)">编辑</jvs-button>
            <jvs-button size="mini" type="text" style="color: #f56c6c" @click="delRowHandle(scope.row)">删除</jvs-button>
          </template>
        </jvs-table>
      </div>
    </div> 
    <el-dialog
      :title="title"
      :visible.sync="dialogVisible"
      append-to-body
      :before-close="handleClose">
      <div v-if="dialogVisible">
        <jvs-form :option="formOption" :formData="rowData" @submit="rowSubmit" @formChange="formChange" @cancalClick="handleClose">
          <template slot="designIdForm">
            <div style="display: flex;align-items: center;">
              <span v-if="rowData.designName" style="max-width: calc(100% - 70px);margin-right: 10px;text-overflow: ellipsis;overflow: hidden;white-space: pre;">{{rowData.designName}}</span>
              <jvs-button v-if="rowData.designType != 'app'" size="mini" type="primary" :disabled="!rowData.designType" @click="selectDesign">选择</jvs-button>
            </div>
          </template>
        </jvs-form>
      </div>
    </el-dialog>
    <el-dialog
      title="选择设计"
      width="60%"
      :visible.sync="selectVisible"
      append-to-body
      :before-close="selectClose">
      <div v-if="selectVisible">
        <jvs-table
          :page="selectPage"
          :loading="selectLoading"
          :option="selectOption"
          :data="designList"
          @on-load="getSelectList"
          @search-change="selectSearch"
          @row-click="selectSubmit"
        ></jvs-table>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getIdentificationPage, getDataModelListByApp, getRuleListByApp,
  addIdentification, editIdentification, delIdentification } from './api'
import emptyImg from '@/const/img/codeDev.png'
export default {
  components: {},
  props: {
    appInfo: {
      type: Object
    }
  },
  data() {
    return {
      tableLoading: false,
      queryParam: {},
      rowData: {},
      title: '',
      tableData: [],
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
      },
      pageOption: {
        addBtn: false,
        editBtn: false,
        viewBtn: false,
        delBtn: false,
        search: true,
        page: true,
        menuWidth: 150,
        column: [
          {
            label: '设计名称',
            prop: 'designName',
            search: true,
          },
          {
            label: '设计ID',
            prop: 'designId'
          },
          {
            label: '设计类型',
            prop: 'designType',
            search: false,
            type: 'select',
            dicData: [
              {label: '应用', value: 'app'},
              {label: '数据模型', value: 'data'},
              {label: '业务逻辑', value: 'rule'}
            ]
          },
          {
            label: '标识符',
            prop: 'identifier',
            search: true,
          }
        ]
      },
      dialogVisible: false,
      formOption: {
        emptyBtn: false,
        submitLoading: false,
        column: [
          {
            label: '设计类型',
            prop: 'designType',
            disabled: true,
            type: 'select',
            dicData: [
              {label: '应用', value: 'app'},
              {label: '数据模型', value: 'data'},
              {label: '业务逻辑', value: 'rule'}
            ],
            rules: [
              { required: true, message: '请选择设计类型', trigger: 'change' }
            ],
          },
          {
            label: '选择设计',
            prop: 'designId',
            formSlot: true,
            type: 'select',
            rules: [
              { required: true, message: '请选择设计', trigger: 'change' }
            ],
          }, 
          {
            label: '标识符',
            prop: 'identifier',
            rules: [
              { required: true, message: '请输入标识符', trigger: 'blur' }
            ],
          }
        ]
      },
      designList: [],
      selectVisible: false,
      selectPage: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
      },
      selectLoading: false,
      selectOption: {
        addBtn: false,
        editBtn: false,
        viewBtn: false,
        delBtn: false,
        search: true,
        page: true,
        menu: false,
        column: [
          {
            label: '名称',
            prop: 'name',
            search: true
          },
          {
            label: 'ID',
            prop: 'id',
            hide: false
          },
          {
            label: '类型',
            prop: 'reqType',
            type: 'select',
            dicData: [
              { label: '事件触发', value: 'Source_code_development_docking_logic'},
              { label: 'API触发', value: 'External_API_logic'},
              { label: '定时触发', value: 'Timing_logic'},
              { label: '监听触发', value: 'Listening_logic'},
              { label: '默认', value: 'Low_code_logic'},
            ],
            search: true,
          },
          {
            label: 'secret',
            prop: 'secret',
            hide: false
          }
        ]
      },
      selectParam: {},
      activeType: 'data',
      typeList: [
        {label: '模型标识', value: 'data'},
        {label: '逻辑标识', value: 'rule'}
      ],
      origin: '',
      appIdent: null,
      emptyImg: emptyImg
    };
  },
  created () {
    this.getAppIndent()
  },
  methods: {
    getAppIndent () {
      getIdentificationPage(this.appInfo.id, {designType: 'app'}).then(res => {
        if(res.data && res.data.code == 0) {
          if(res.data.data && res.data.data.records && res.data.data.records.length > 0) {
            this.appIdent = res.data.data.records[0]
            this.$forceUpdate()
          }
        }
      })
    },
    getList() {
      this.tableLoading = true
      let obj = {
        current: this.page.currentPage,
        size: this.page.pageSize
      }
      if(this.activeType) {
        obj.designType = this.activeType
      }
      getIdentificationPage(this.appInfo.id, Object.assign(obj, this.queryParam)).then(res => {
        if(res.data && res.data.code == 0) {
          this.tableData = res.data.data.records
          this.page.currentPage = res.data.data.current
          this.page.total = res.data.data.total
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
      this.getList()
    },
    setRowHandle (row) {
      if(row) {
        this.rowData = JSON.parse(JSON.stringify(row))
        if(row.id) {
          this.title = '修改'
        }else{
          this.title = '新增'
        }
      }else{
        this.title = '新增'
      }
      if(this.rowData) {
        if(this.rowData.designType == 'app') {
          this.$set(this.rowData, 'designId', this.appInfo.id)
          this.$set(this.rowData, 'designName', this.appInfo.name)
        }
      }
      this.dialogVisible = true
    },
    delRowHandle (row) {
      this.$confirm('确定删除此项', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        delIdentification(this.appInfo.id, row.id).then(res => {
          if(res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: '删除成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.getList()
          }
        })
      }).catch(e => {})
    },
    handleClose () {
      this.dialogVisible = false
      this.rowData = {}
      this.title = ''
    },
    rowSubmit () {
      console.log(this.rowData)
      let fun = addIdentification
      if(this.rowData.id) {
        fun = editIdentification
      }
      this.formOption.submitLoading = true
      fun(this.appInfo.id, this.rowData).then(res => {
        if(res.data && res.data.code == 0) {
          this.$notify({
            title: '提示',
            message: `${this.rowData.id ? '修改' : '新增'}成功`,
            position: 'bottom-right',
            type: 'success'
          })
          this.formOption.submitLoading = false
          if(this.rowData.designType == 'app') {
            this.getAppIndent()
          }
          this.handleClose()
          this.getList()
        }
      }).catch(e => {
        this.formOption.submitLoading = false
      })
    },
    formChange (form, item, before) {
      if(item.prop == 'designType') {
        if(this.rowData.designType) {
          if(before ? form[item.prop] != before[item.prop] : true) {
            this.$set(this.rowData, 'designId', '')
            this.$set(this.rowData, 'designName', '')
          }
          if(this.rowData.designType == 'app') {
            this.$set(this.rowData, 'designId', this.appInfo.id)
            this.$set(this.rowData, 'designName', this.appInfo.name)
          }
        }else{
          this.$set(this.rowData, 'designId', '')
          this.$set(this.rowData, 'designName', '')
        }
      }
    },
    selectDesign () {
      this.selectParam = {}
      this.selectPage.currentPage = 1
      this.selectPage.total = 0
      this.selectOption.column.filter(cit => {
        if(cit.prop == 'id') {
          cit.hide = this.rowData.designType == 'rule' ? true : false
        }
        if(cit.prop == 'secret') {
          cit.hide = this.rowData.designType == 'rule' ? false : true
        }
        if(cit.prop == 'reqType') {
          cit.hide = this.rowData.designType == 'rule' ? false : true
          cit.search = this.rowData.designType == 'rule' ? true : false
        }
      })
      this.selectVisible = true
    },
    selectClose () {
      this.selectVisible = false
    },
    getSelectList () {
      this.selectLoading = true
      let fun = null
      switch(this.rowData.designType) {
        case 'data': fun = getDataModelListByApp; break;
        case 'rule': fun = getRuleListByApp; break;
        default: ;break;
      }
      if(fun) {
        fun(this.appInfo.id, Object.assign({size: this.selectPage.pageSize, current: this.selectPage.currentPage}, this.selectParam)).then(res => {
          if(res.data && res.data.code == 0) {
            this.designList = res.data.data.records
            this.selectPage.currentPage = res.data.data.current
            this.selectPage.total = res.data.data.total
            this.selectLoading = false
          } else {
            this.selectLoading = false
          }
        }).catch(err => {
          this.selectLoading = false
        })
      }
    },
    selectSearch (form) {
      this.selectParam = form
      this.getSelectList()
    },
    selectSubmit (data) {
      let { row } = data
      this.$set(this.rowData, 'designId', this.rowData.designType == 'rule' ? row.secret : row.id)
      this.$set(this.rowData, 'designName', row.name)
      this.selectClose()
    },
    typeChange (tab) {
      if(tab.value != this.activeType) {
        this.activeType = tab.value
        this.page.currentPage = 1
        this.getList()
      }
    },
    copyStr (str) {
      const text = document.createElement('input')
      text.value = str
      document.body.appendChild(text)
      text.select()
      document.execCommand('Copy')
      document.body.removeChild(text)
      this.$notify({
        title: '提示',
        message: '复制成功',
        position: 'bottom-right',
        type: 'success'
      })
    },
  },
};
</script>

<style lang="scss" scoped>
.code-dev-page-list{
  height: 100%;
  overflow: hidden;
  overflow-y: auto;
  padding-right: 10px;
  box-sizing: border-box;
  .top-bottom-splite{
    margin-top: 24px;
    width: 100%;
    height: 8px;
    background: #F5F6F7;
  }
  .code-dev-page-bottom{
    height: 50vh; // calc(100% - 420px);
    .type-list{
      margin-top: 16px;
      margin-bottom: 16px;
      display: flex;
      align-items: center;
      .type-list-item{
        margin-left: 32px;
        font-size: 16px;
        font-family: Microsoft YaHei, Microsoft YaHei;
        font-weight: 400;
        color: #6F7588;
        padding-bottom: 6px;
        cursor: pointer;
      }
      .type-list-item:nth-of-type(1){
        margin-left: 0;
      }
      .active{
        font-size: 16px;
        font-family: Microsoft YaHei, Microsoft YaHei;
        font-weight: 700;
        color: #1E6FFF;
        position: relative;
      }
      .active::after{
        display: block;
        content: "";
        width: 100%;
        height: 2px;
        background: #1E6FFF;
        border-radius: 2px 0px 2px 0px;
        position: absolute;
        bottom: 0;
        left: 0;
      }
    }
  }
  .jvs-table{
    height: calc(100% - 38px);
    .jvs-table-top .search-form{
      border-bottom: 0;
      padding-top: 0;
    }
    /deep/.table-body-box{
      padding: 0;
      height: calc(100% - 100px - 68px);
      .el-table{
        height: 100%;
        .el-table__header-wrapper{
          tr{
            th{
              background: #f6f6f6;
            }
          }
        }
      }
    }
    /deep/.el-table__body-wrapper{
      height: calc(100% - 48px)!important;
    }
  }
}
.permission-page{
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
      span {
        color: #3471ff;
        cursor: pointer;
      }
    }
    .permission-header{
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .resource-list{
      margin-top: 16px;
      display: flex;
      align-items: center;
      .el-tag{
        margin-bottom: 10px;
        margin-right: 10px;
      }
    }
    .permission-content{
      margin-top: 16px;
      max-height: calc(100vh - 200px);
      overflow-y: auto;
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
        .user-select{

        }
      }
    }
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
    /deep/ .el-select{
      margin-right: 10px;
      .el-input{
        width: 200px;
      }
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
