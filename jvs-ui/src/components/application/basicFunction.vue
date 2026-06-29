<template>
  <div class="basic-function-page">
    <div class="basic-function-left">
      <div :class="{'basic-function-left-item': true, 'active': !currentType}" @click="searchTypeChange('')">全部</div>
      <div v-for="ritem in typeList" :key="ritem" :class="{'basic-function-left-item': true, 'active': ritem == currentType}" @click="searchTypeChange(ritem)">{{ritem}}</div>
    </div>
    <div class="extend-content">
      <div class="extend-btn">
        <div>
          <jvs-button type="primary" @click="addEditRow(null)">新建</jvs-button>
        </div>
        <div>
          <p style="cursor:pointer;display: flex;align-items: center;" @click="searchChange(queryParams)">
            <span>刷新</span>
            <i class="el-icon-refresh" style="cursor:pointer;margin-left:3px;"></i>
          </p>
        </div>
      </div>
      <jvs-table
        :option="tableOption"
        :loading="tableLoading"
        :data="tableData"
        :page="page"
        @on-load="getList"
        @search-change="searchChange"
      >
        <template slot="info" slot-scope="scope">
          <div style="width: 100%;overflow: hidden;text-overflow: ellipsis;white-space: pre;">{{scope.row.info | getMessageText}}</div>
        </template>
        <template slot="menu" slot-scope="scope">
          <el-button type="text" @click="addEditRow(scope.row)">编辑</el-button>
          <el-button type="text" style="color: #F56C6C;" @click="deleteRowHandle(scope.row)">删除</el-button>
        </template>
      </jvs-table>
    </div>

    <!-- 新增 修改 -->
    <el-dialog
      class="function-oprate-dialog"
      append-to-body
      :title="dialogType == 'add' ? '新增' : '编辑'"
      :visible.sync="dialogVisible"
      :close-on-click-modal="false"
      :before-close="handleClose">
      <div v-if="dialogVisible" ref="fuctionBody" class="function-oprate-dialog-body">
        <div class="top">
          <jvs-form ref="paramForm" :option="formOption" :formData="rowData" @formChange="formChange" @submit="submitHandle">
            <template slot="parametersForm">
              <div class="parameters-list" :style="(rowData.parameters && rowData.parameters.length > 0 && (!testResult)) ? 'padding-bottom: 23px;' : ''">
                <div class="parameters-list-item">
                  <span style="flex: 1;overflow: hidden;margin-right: 8px;">参数类型</span>
                  <span class="el-input">参数名</span>
                  <b class="oprator" style="margin: 0 8px;color: #fff;">=</b>
                  <span class="el-input">测试值</span>
                  <div class="delete-icon-button" style="background: transparent;"></div>
                </div>
                <div v-if="rowData.parameters && rowData.parameters.length > 0">
                  <div v-for="(item, index) in rowData.parameters" :key="'param-item-'+index" class="parameters-list-item">
                    <el-select v-model="item.type" size="mini" placeholder="请选择类型" style="flex: 1;overflow: hidden;margin-right: 8px;" @change="typeChange(item, index)">
                      <el-option label="文本" value="text"></el-option>
                      <el-option label="数字" value="number"></el-option>
                      <el-option label="布尔" value="bool"></el-option>
                      <el-option label="日期" value="date"></el-option>
                      <el-option label="对象" value="object"></el-option>
                      <el-option label="数组" value="array"></el-option>
                      <el-option label="任意" value="any"></el-option>
                    </el-select>
                    <el-input v-model="item.label" size="mini" placeholder="请输入(例：type)"></el-input>
                    <b class="oprator">=</b>
                    <el-input v-if="['text', 'date', 'object', 'array', 'any'].indexOf(item.type) > -1" v-model="item.value" size="mini" placeholder="请输入(例：1)"></el-input>
                    <el-input-number v-if="item.type == 'number'" v-model="item.value" size="mini" style="flex: 1;"></el-input-number>
                    <el-switch v-if="item.type == 'bool'" v-model="item.value" size="mini" style="flex: 1;"></el-switch>
                    <div class="delete-icon-button" @click="delParam(index, item)">
                      <span class="border-line"></span>
                    </div>
                  </div>
                </div>
                <div v-if="rowData.dynamicParam ? (rowData.parameters && rowData.parameters.length < 1) : true" class="bottom-button">
                  <div class="button" @click="addParameter">
                    <div class="icon">
                      <svg aria-hidden="true">
                        <use xlink:href="#jvs-ui-icon-xinjian"></use>
                      </svg>
                    </div>
                    <span>新增一行</span>
                  </div>
                </div>
              </div>
            </template>
          </jvs-form>
        </div>
        <div v-if="testResult" class="center">
          <div class="jvs-form">
            <div class="el-form-item">
              <div class="el-form-item__label">测试结果</div>
            </div>
            <div class="result-text">
              <p v-if="testResult == 'number'" style="padding:10px;margin:0;">{{testResult}}</p>
              <json-viewer v-else :value="testResult || ''"></json-viewer>
            </div>
          </div>
        </div>
        <div class="footer">
          <div class="footer-button">
            <div class="ftb" @click="handleClose">取消</div>
            <el-button class="ftb test" :loading="testLoading" @click="testHandle">测试</el-button>
            <el-button class="ftb submit" :loading="formOption.submitLoading" @click="submit">确定</el-button>
          </div>
        </div>
      </div>
    </el-dialog>
     <!-- 删除确认 -->
    <el-dialog
      class="custom-header-dialog select-data-dialog"
      title="确认删除函数"
      append-to-body
      :visible.sync="deleteVisible"
      :before-close="deleteClose">
      <div v-if="deleteVisible" class="delete-confirm-box">
        <div class="title">
          <i class="el-icon-warning"></i>
          <span>警告</span>
        </div>
        <div class="content">该操作将<i>永久删除</i>函数"<b>{{`${deleteRow.name}`}}</b>"，同时直接影响关联的<i>所有数据</i>，且<i>无法还原</i>，请谨慎操作！</div>
        <div class="input-box">
          <p>如确定删除，请输入函数名称</p>
          <el-input v-model="deleteRow.appName" size="mini"></el-input>
        </div>
      </div>
      <div v-if="deleteVisible" class="footer">
        <div class="footer-button">
          <div class="ftb" @click="deleteClose">取消</div>
          <div :class="{'ftb submit': true, 'disabled': !(deleteRow.appName && deleteRow.name == deleteRow.appName)}" @click="deleteSubmit">确定</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getFunctionList, addFunction, testFunction, getFunctionType, getFunctionDetail, deleteFunctionById } from "@/api/application";
import PlatformPageHeader from "@/components/page-header/PlatformPageHeader";

export default {
  name: "basicFunction",
  components: {PlatformPageHeader},
  props: {
    useByComponent: {
      type: Boolean
    }
  },
  filters: {
    getMessageText (html) {
      let str = html
      if(html && html.startsWith('<')) {
        let div = document.createElement('div')
        div.innerHTML = html
        str = div.innerText
      }
      return str
    }
  },
  data () {
    return {
      tableLoading: false,
      tableData: [],
      queryParams: {},
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
      },
      tableOption: {
        addBtn: false,
        editBtn: false,
        delBtn: false,
        viewBtn: false,
        showOverflow: true,
        page: true,
        menuWidth: '90px',
        search: true,
        column: [
          {
            label: '名称',
            prop: 'name',
            width: '200px',
            search: true
          },
          {
            label: '小标题',
            prop: 'shortName',
            width: '300px'
          },
          {
            label: '描述',
            prop: 'info',
            slot: true
          },
          {
            label: '分类',
            prop: 'type',
            width: '100px'
          }
        ]
      },
      dialogType: '',
      dialogVisible: false,
      rowData: null,
      formOption: {
        submitLoading: false,
        emptyBtn: false,
        formAlign: 'top',
        btnHide: true,
        column: [
          {
            label: '函数名称',
            prop: 'functionName',
            rules: [ { required: true, message: '请输入函数名称', trigger: 'blur' }, ]
          },
          {
            label: '分类',
            prop: 'type',
            type: 'select',
            dicData: [],
            allowcreate: true,
            filterable: true,
            rules: [ { required: true, message: '请选择或输入分类', trigger: 'change' },]
          },
          {
            label: '函数解释',
            prop: 'info',
            type: 'htmlEditor',
            rules: [ { required: true, message: '请输入函数解释', trigger: 'cahnge' }, ]
          },
          {
            label: '函数简介',
            prop: 'shortName',
            rules: [ { required: true, message: '请输入函数小标题', trigger: 'cahnge' }, ]
          },
          {
            label: '动态参数',
            prop: 'dynamicParam',
            type: 'switch'
          },
          {
            label: '函数参数列表',
            prop: 'parameters',
            formSlot: true,
            rules: [ { required: false, message: '请添加函数参数', trigger: 'change' }, ]
          },
          {
            label: '函数返回类型',
            prop: 'jvsParamType',
            type: 'select',
            dicData: [
              {label: '数组', value: 'array'},
              {label: '布尔', value: 'bool'},
              {label: '日期', value: 'date'},
              {label: '文件', value: 'file'},
              {label: '无', value: 'none'},
              {label: '数字', value: 'number'},
              {label: '对象', value: 'object'},
              {label: '文本', value: 'text'},
              {label: '未知', value: 'unknown'}
            ],
            rules: [ { required: true, message: '请输入函数名称', trigger: 'cahnge' }, ]
          },
          {
            label: '函数体',
            prop: 'body',
            type: 'jsonEditor',
            lang: 'groovy',
            defaultValue: `def groovy(){   return 1};`,
            rules: [ { required: true, message: '请输入函数名称', trigger: 'blur' }, ]
          },
        ]
      },
      testResult: null,
      testLoading: false,
      typeList: [],
      currentType: '',
      deleteVisible: false,
      deleteRow: null
    }
  },
  created() {
    this.getFuncType()
  },
  methods: {
    getList (page) {
      let query={
        current: this.page.currentPage,
        size: this.page.pageSize
      }
      if(this.currentType) {
        query.type = this.currentType
      }
      this.tableLoading = true
      let temp = JSON.parse(JSON.stringify(this.queryParams))
      getFunctionList(Object.assign(query, temp)).then(res => {
        if(res.data.code==0 && res.data.data) {
          this.page.total=res.data.data.total
          this.page.currentPage=res.data.data.current
          this.tableData = res.data.data.records
          this.tableLoading = false
        }else{
          this.tableLoading = false
        }
      }).catch(e => {
        this.tableLoading = false
      })
    },
    searchChange (form) {
      this.queryParams = form
      this.getList()
    },
    searchTypeChange (type) {
      if(type != this.currentType) {
        this.page.currentPage = 1
        this.currentType = type
        this.getList()
      }
    },
    addEditRow (row) {
      if(row) {
        if(!row.parameters) {
          row.parameters = []
        }
        if(row.name && !row.functionName) {
          row.functionName = row.name
        }
        getFunctionDetail(row.name).then(res => {
          if(res.data && res.data.code == 0) {
            this.rowData = Object.assign(JSON.parse(JSON.stringify(row)), res.data.data)
            this.dialogType = 'edit'
            this.dialogVisible = true
          }
        })
      }else{
        this.dialogType = 'add'
        this.rowData = {
          parameters: []
        }
        this.dialogVisible = true
      }
    },
    submit () {
      this.$refs.paramForm.submitForm('ruleForm')
    },
    submitHandle () {
      this.formOption.submitLoading = true
      let str = '新增成功'
      if(this.dialogType == 'edit') {
        str = '编辑'
      }
      addFunction(this.rowData).then(res => {
        if(res.data && res.data.code == 0) {
          this.$notify({
            title: '提示',
            message: str,
            position: 'bottom-right',
            type: 'success'
          })
          this.handleClose()
          this.getList()
        }else{
          this.formOption.submitLoading = false
        }
      }).catch(e => {
        this.formOption.submitLoading = false
      })
    },
    handleClose () {
      this.rowData = null
      this.testResult = null
      this.dialogVisible = false
      this.dialogType = ''
      this.formOption.submitLoading = false
      this.testLoading = false
    },
    addParameter () {
      this.rowData.parameters.push({})
      this.$forceUpdate()
      this.$nextTick(() => {
        this.$refs.fuctionBody.scrollTop = this.$refs.fuctionBody.scrollHeight - this.$refs.fuctionBody.clientHeight
      })
    },
    delParam (index, item) {
      this.rowData.parameters.splice(index, 1)
      this.$forceUpdate()
    },
    testHandle () {
      this.testLoading = true
      testFunction(this.rowData).then(res => {
        if(res.data && res.data.code == 0) {
          console.log(res.data.data)
          this.testResult = res.data.data
          this.testLoading = false
        }else{
          this.testLoading = false
        }
      }).catch(e => {
        this.testLoading = false
      })
    },
    typeChange (item, index) {
      if(['text', 'date'].indexOf(item.type) > -1) {
        this.$set(this.rowData.parameters[index], 'value', '')
      }
      if(item.type == 'bool') {
        this.$set(this.rowData.parameters[index], 'value', false)
      }

      if(item.type == 'number') {
        this.$set(this.rowData.parameters[index], 'value', 0)
      }
    },
    getFuncType () {
      getFunctionType().then(res => {
        if(res.data && res.data.code == 0) {
          this.typeList = res.data.data || []
          let list = []
          this.typeList.filter(tp => {
            list.push({label: tp, value: tp})
          })
          this.formOption.column.filter(col => {
            if(col.prop == 'type') {
              col.dicData = list
            }
          })
        }
      })
    },
    formChange (form, item) {
      if(item && item.prop == 'dynamicParam') {
        if(this.rowData[item.prop]) {
          if(this.rowData.parameters && this.rowData.parameters.length > 1) {
            this.$set(this.rowData, 'parameters', [this.rowData.parameters[0]])
          }
        }
      }
    },
    deleteRowHandle (row) {
      this.deleteRow = JSON.parse(JSON.stringify(row))
      this.deleteVisible = true
    },
    deleteSubmit () {
      if(this.deleteRow.appName && this.deleteRow.appName == this.deleteRow.name) {
        deleteFunctionById(this.deleteRow.id).then(res => {
          this.$notify({
            title: '提示',
            message: '删除成功',
            position: 'bottom-right',
            type: 'success'
          });
          this.searchChange(this.queryParams)
          this.deleteClose()
        })
      }
    },
    deleteClose () {
      this.deleteRow = null
      this.deleteVisible = false
    },
  }, 
}
</script>

<style lang="scss" scoped>
.basic-function-page{
  height: 100%;
  overflow: hidden;
  display: flex;
  .extend-content{
    display: flex;
    flex-direction: column;
    margin-left: 20px;
    flex: 1;
    overflow: hidden;
    .extend-btn{
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      >div{
        display: flex;
        align-items: center;
      }
    }
    /deep/.jvs-table{
      height: calc(100% - 68px - 40px);
      .jvs-table-notitle{
        height: 56px;
      }
      .table-body-box{
        height: calc(100% - 56px);
        .el-table{
          height: 100%;
          .el-table__body-wrapper{
            height: calc(100% - 40px)!important;
          }
        }
      }
    }
  }
  .basic-function-left{
    width: 200px;
    height: 100%;
    overflow: hidden;
    overflow-y: auto;
    box-sizing: border-box;
    .basic-function-left-item{
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
      &+.basic-function-left-item{
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
.parameters-list{
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
  .parameters-list-item{
    display: flex;
    align-items: center;
    justify-content: space-between;
    overflow: hidden;
    .oprator{
      margin: 0 8px;
    }
    .delete-icon-button{
      margin-left: 16px;
    }
    /deep/.el-input{
      flex: 1;
      overflow: hidden;
    }
    &+.parameters-list-item{
      margin-top: 8px;
    }
  }
}
.function-oprate-dialog.el-dialog__wrapper{
  /deep/.el-dialog{
    width: 600px;
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
        padding-bottom: 60px;
        overflow: hidden;
        overflow-y: auto;
        box-sizing: border-box;
        .jvs-form{
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
        .center{
          padding: 0 32px;
          box-sizing: border-box;
          .jvs-form{
            .el-form-item{
              margin-bottom: 0;
            }
          }
          .result-text{
            .jv-code{
              padding: 20px;
            }
          }
        }
        .footer{
          position: absolute;
          left: 0;
          bottom: 0;
          width: 100%;
          height: 60px;
          z-index: 999;
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
}
.delete-confirm-box{
  padding: 20px;
  box-sizing: border-box;
  .title{
    display: flex;
    align-items: center;
    i{
      font-size: 20px;
      color: #FF194C;
    }
    span{
      font-family: Source Han Sans-Bold, Source Han Sans;
      font-weight: 500;
      font-size: 16px;
      color: #FF194C;
      line-height: 20px;
      margin-left: 5px;
    }
  }
  .content{
    padding: 0 20px;
    margin-top: 10px;
    font-family: Source Han Sans-Regular, Source Han Sans;
    font-weight: 400;
    font-size: 14px;
    color: #363B4C;
    i{
      font-style: normal;
      color: #FF194C;
    }
  }
  .input-box{
    margin-top: 10px;
    padding: 0 20px;
    p{
      margin: 0 0 10px 0;
      font-family: Source Han Sans-Regular, Source Han Sans;
      font-weight: 400;
      font-size: 13px;
      color: #363B4C;
    }
  }
}
.select-data-dialog.el-dialog__wrapper{
  /deep/.el-dialog{
    width: 540px;
    height: 240px;
    .el-dialog__header{
      display: none;
    }
    .el-dialog__body{
      height: calc(100% - 48px);
      padding: 0!important;
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
            text-align: center;
            background: #F5F6F7;
            border-radius: 4px 4px 4px 4px;
            cursor: pointer;
            font-family: Microsoft YaHei-Regular, Microsoft YaHei;
            font-weight: 400;
            font-size: 14px;
            color: #363B4C;
            margin-right: 16px;
          }
          .submit{
            background: #1E6FFF;
            color: #fff;
            &.disabled{
              cursor: not-allowed;
            }
          }
        }
      }
    }
  }
}
</style>
