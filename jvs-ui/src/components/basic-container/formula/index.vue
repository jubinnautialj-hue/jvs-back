<template>
  <div>
    <el-dialog
      ref="formulaDialog"
      :modal="modal"
      :class="{
        'formula-component-dialog': true,
        'formula-component-dialog-modal': modal,
      }"
      :visible.sync="formulaVisible"
      append-to-body
      :before-close="handleClose"
      :title="headerTitle"
      :width="useCaseString == 'RULE' ? '1025px' : '900px'"
      :close-on-click-modal="false"
      @opened="_initialize"
    >
      <div :class="{'formula-panel-box': true, 'formula-panel-box-rule': useCaseString == 'RULE'}" v-if="formulaVisible">
        <!-- 表达式 -->
        <div class="formula-panel-top">
          <div class="formula-panel formula-panel-editor">
            <p class="formula-panel-title" style="position: relative;">
              {{showLabel}}
              <span class="formula-panel-subtitle">=</span>
              <span v-show="testResult !== null" class="formula-test-result">
                <el-popover
                  v-model="testResultShow"
                  placement="right"
                  width="500"
                  trigger="hover"
                >
                  <div class="result-box">
                    <h4 style="display:flex;justify-content: space-between;align-items: center;">
                      <span>测试结果</span>
                      <span>
                        <el-button v-if="testResult" icon="el-icon-document-copy" type="text" @click="copyTestHandle(testResult)" style="margin-left: 10px;cursor: pointer;">复制</el-button>
                      </span>
                    </h4>
                    <div class="result-text">
                      <p v-if="testResult == 'number'" style="padding:10px;margin:0;">{{testResult}}</p>
                      <json-viewer v-else :value="testResult || ''"></json-viewer>
                    </div>
                  </div>
                  <span slot="reference" class="formula-test-result-text">测试结果</span>
                </el-popover>
              </span>
              <span v-if="useCaseString == 'RULE'" class="formula-set">
                <span>校验</span>
                <el-select v-model="checkType" clearable size="mini">
                  <el-option label="不能为空" value="cannot_be_empty"></el-option>
                  <el-option label="不能为空数组" value="cannot_be_empty_array"></el-option>
                </el-select>
              </span>
              <span :class="{'formula-desc': true, 'show': showEditInput, 'rule': useCaseString == 'RULE'}">
                <el-input v-if="showEditInput" size="mini" v-model="descstring" class="name-text" placeholder="在此填写公式描述"></el-input>
                <span v-else class="name-text" :title="descstring">{{descstring}}</span>
                <div :class="{'edit-icon': true, 'editing': showEditInput}" @click="showEditInput=!showEditInput;">
                  <svg aria-hidden="true">
                    <use xlink:href="#jvs-ui-icon-bianji1"></use>
                  </svg>
                </div>
              </span>
            </p>
            <div class="formula-panel-content">
              <codeBlockEditor ref="formulaEditor" :matchResult="matchResult" :nodeDataList="nodeDataList" @matchWords="matchWords" @matchClick="matchClick" @change="coderChange"></codeBlockEditor>
            </div>
          </div>
        </div>
        <!-- 字段、函数、说明 -->
        <div class="formula-panel-bottom"  @mouseleave="currentData = null">
          <!-- 字段列表 -->
          <div v-if="funcList.length == 0" class="formula-panel loading"></div>
          <div v-if="funcList.length == 0" class="formula-panel loading"></div>
          <div v-if="funcList.length > 0" :class="{'formula-panel': true, 'formula-panel-rule': useCaseString == 'RULE'}" v-for="(item, index) in funcList" :key="'funcList-item-'+index">
            <p v-if="false" class="formula-panel-title" @mouseover="currentData = null">
              <span class="formula-panel-title-text">{{item.title}}</span>
            </p>
            <!-- list类型 -->
            <div class="formula-panel-content" v-if="item.type == 'list'">
              <div class="formula-panel-item-filed">
                <a class="formula-filed-item" v-for="(idata, idx) in item.data" :key="'data-item-'+idx" @click="addVariable(idata, index)" @mouseover="showInfo(idata)">
                  <span class="formula-filed-title">{{idata.shortName || idata.name}}</span>
                  <div :class="{'formula-filed-capsule': true,
                    'blue': idata.jvsParamType == 'text',
                    'yellow': idata.jvsParamType == 'number',
                    'green': idata.jvsParamType == 'date',
                    'purple': idata.jvsParamType == 'array',
                    'red': idata.jvsParamType == 'object',
                    'pink': idata.jvsParamType == 'bool',
                    'dark-yellow': idata.jvsParamType == 'file',
                    'cyan': idata.jvsParamType == 'unknown' || idata.jvsParamType == 'any',
                  }">{{idata.jvsParamType | getLabeType}}</div>
                </a>
              </div>
            </div>
            <!-- 树形结构 -->
            <div class="formula-panel-content" v-if="item.type == 'tree'">
              <div class="formula-panel-item-filed">
                <el-collapse>
                  <el-collapse-item v-for="it in item.data" :title="it.title" :name="it.title" :key="'tree-data-item-'+it.title">
                    <template slot="title"><span @mouseover="currentData = null">{{it.title}}</span></template>
                    <ul class="function-panel-filed-list">
                      <li :class="{'function-panel-filed-li': true, 'function-panel-filed-li-haschildren': (idata.children && idata.children.length > 0)}" v-for="(idata, idx) in it.data" :key="'treedata-item-'+idx">
                        <treeNode :idata="idata" :index="index" @click="addVariable" @show="showInfo"></treeNode>
                      </li>
                    </ul>
                  </el-collapse-item>
                </el-collapse>
              </div>
            </div>
          </div>
          <!-- 函数描述 -->
          <div class="formula-panel formula-panel-func-desc" v-if="funcList && funcList.length < 3">
            <p class="formula-panel-title" style="overflow: hidden;text-overflow: ellipsis;white-space: pre;">
              <span class="formula-panel-title-text" :title="currentData ? currentData.name : ''">{{currentData ? currentData.name : '说明'}}</span>
            </p>
            <div class="formula-panel-content info-section" v-if="currentData">
              <!-- 详情 -->
              <div>
                <p v-if="currentData.type">{{currentData.type}}</p>
                <div v-if="currentData.info" class="info-text" v-html="currentData.info" :style="currentData.info.includes('</') ? '' : 'padding: 0 15px;'"></div>
              </div>
            </div>
            <div class="formula-panel-content info-section" v-else>
              <!-- 详情 -->
              <div>
                <p>请从左侧面板选择字段名和函数，或输入函数</p>
                <p>公式编辑举例：<span class="fun">SUM</span><span class="fun">(</span><span class="param">参数1</span>,<span class="param">参数2</span><span class="fun">)</span></p>
                <p><a @click="helpCenter('all-formula')">查看基本公式的帮助文档</a></p>
              </div>
            </div>
          </div>
        </div>
        <!-- 底部按钮栏 -->
        <div class="footer-button">
          <el-button size="mini" type="primary" :loading="subLoading" @click="submitHandle">确定</el-button>
          <el-button size="mini" :loading="testLoding" @click="testHandle" style="background: #36B452;color: #fff;">测试</el-button>
          <el-button size="mini" @click="handleClose">取消</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 测试公式填写参数 -->
    <el-dialog
      title="动态参数"
      :visible.sync="testParamVisible"
      :before-close="testParamClose">
      <div v-if="testParamVisible">
        <jvs-form :option="testParamOption" :formData="testParam" @submit="testParamSubmit" @cancalClick="testParamClose">
          <template v-for="item in formSlotColumn" :slot="item.prop+'Form'">
            <div :key="item.prop" style="flex:1;width:100%;height: 200px;position: relative;">
              <jsonEditor lang="jsonArray" :code="testParam[item.prop]" :prop="item.prop" @change="jsonChange"></jsonEditor>
            </div>
          </template>
        </jvs-form>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import {getFuncList, addEditExec, getExec, jiexiExec, testExec} from './api'
import treeNode from './treeNode.vue'
import codeBlockEditor from './codeEditor'
import jsonEditor from '@/components/basic-assembly/jsonEditor'
export default {
  components: { treeNode, codeBlockEditor, jsonEditor },
  computed: {
    headerTitle() {
      return this.title || '公式设置'
    },
    showLabel() {
      return this.label
    },
    modal() {
      if (this.hasModal) {
        return true;
      } else {
        return false;
      }
    },
    propData () {
      return this.props
    },
    requestPrefix () {
      return this.apiPrefix
    },
    useCaseString () {
      return this.useCase
    },
  },
  filters: {
    getLabeType(type) {
      let str = ''
      switch(type) {
        case 'text':
          str = '文字';break;
        case 'number':
          str = '数字';break;
        case 'date':
          str = '时间';break;
        case 'array':
          str = '数组';break;
        case 'object':
          str = '对象';break;
        case 'bool':
          str = '布尔';break;
        case 'file':
          str = '文件';break;
        case 'any':
        case 'unknown':
          str = '未知';break;
        default: ;break;
      }
      return str
    }
  },
  data() {
    return {
      formulaVisible: false,
      coder: null, // 编辑器实例
      options: {   // 默认配置
        mode: 'javasctipt', // 语言
        // 缩进格式
        tabSize: 2,
        // 主题，对应主题库 JS 需要提前引入
        // theme: 'cobalt',
        // 显示行号
        lineNumbers: true,
        line: true
      },
      code: '', // 内容
      funcList: [],
      keyCode: -1,
      currentData: null,
      nodeDataList: {},
      subLoading: false,
      matchResult: [],
      descstring: '',
      showEditInput: false,  
      testLoding: false,
      testResult: null,
      testResultShow: false,
      testParamVisible: false,
      testParam: null,
      testMapParamType: null,
      testParamOption: {
        formAlign: 'top',
        emptyBtn: false,
        column: []
      },
      formSlotColumn: [],
      checkType: ''
    }
  },
  methods: {
    async init () {
      await this.getFuncListHandle()
      if(this.execId) {
        this.getExecInfo()
      }else{
        this.formulaVisible = true
      }
    },
    // 根据公式id获取详情
    getExecInfo () {
      getExec(this.execId).then(res => {
        if(res.data && res.data.code == 0) {
          if(res.data.data) {
            this.descstring = res.data.data.description || ''
            this.checkType = res.data.data.checkType || ''
            this.code = res.data.data.body || ''
            // 兼容codemirror的历史数据
            this.code = this.code.replaceAll(/[\u0000-\u001f\u007f-\u009f\u00ad\u061c\u200b\u200e\u200f\u2022\u2028\u2029\ufeff\ufff9-\ufffc]/gi, '\0')
            this.code = this.code.replaceAll(/\0\0/gi, '\0')
          }
        }
        this.formulaVisible = true
      })
    },
    handleClose() {
      this.formulaVisible = false
      if(this.afterCLose) {
        this.afterCLose(this.$refs.formulaDialog)
      }
    },
    // 初始化富文本
    _initialize () {
      let str = ``
      if(this.code) {
        if(this.code.includes('\0') > -1 || this.code.includes('\u0000') > -1) {
          this.code = this.code.replaceAll(/\0/gi, '<>&nbsp;<>')
          this.code = this.code.replaceAll(/\u0000/gi, '<>&nbsp;<>')
          let list = this.code.split('<>')
          for(let i in list) {
            if(list[i]) {
              if(list[i].startsWith('$')) {
                let key = list[i].slice(2, list[i].length-1)
                let item = {}
                this.findParamItem(key, item)
                if(item.id) {
                  str += `${item.param ? item.id : item.name}`
                }else{
                  str += (list[i] == '&nbsp;' ? '' : list[i])
                }
              }else{
                str += (list[i] == '&nbsp;' ? '' : list[i])
              }
            }
          }
        }else{
          str = this.code
        }
        str = str.replaceAll(/`/g, '')
        str = str.replaceAll(/\$\{(\w+)\}/gi, '$1')
      }
      this.$refs.formulaEditor.init(str)
    },
    // 获取全部函数
    async getFuncListHandle () {
      let nodeDataList = {}
      this.funcList = []
      await getFuncList(this.requestPrefix, this.useCaseString, this.propData.designId, this.propData.extendJson).then(res => {
        if(res.data && res.data.code == 0) {
          for(let i in res.data.data) {
            let obj = {
              title: i
            }
            if(typeof res.data.data[i] == 'object') {
              if(res.data.data[i] instanceof Array) {
                obj.type = 'list'
                obj.data = res.data.data[i]
                for(let o in obj.data) {
                  this.$set(nodeDataList, obj.data[o].id, {...obj.data[o], parent: (i+1)})
                }
              }else{
                obj.type = 'tree'
                let temp = []
                for(let k in res.data.data[i]) {
                  temp.push({
                    title: k,
                    data: res.data.data[i][k]
                  })
                  this.setParentHandle(res.data.data[i][k], this.funcList.length+1, nodeDataList, 0)
                }
                obj.data = temp
              }
            }
            this.funcList.push(obj)
          }
          // console.log(nodeDataList)
          this.nodeDataList = nodeDataList
        }else{
          this.handleClose()
        }
      })
    },
    // 添加字段变量
    addVariable (item, index) {
      if(item.param) {
        this.$refs.formulaEditor.insertContent(item.name, '`' + item.id + '`', 'variable', index)
      }else{
        this.$refs.formulaEditor.insertContent(null, item.name, 'func', index)
      }
    },
    // 确定
    submitHandle () {
      let htmlstr = this.$refs.formulaEditor.validateResult()
      if(htmlstr) {
        let obj = {...this.propData}
        if(this.execId) {
          obj.id = this.execId
        }
        if(this.checkType) {
          obj.checkType = this.checkType
        }
        obj.description = this.descstring
        obj.useCase = this.useCaseString
        obj.body = htmlstr.replace(/\u00a0/gi, '').replace(/\u200b/gi, '') // 去除零宽空格
        this.subLoading = true
        addEditExec(this.requestPrefix, obj).then(res => {
          if(res.data && res.data.code == 0){
            if(this.afterSave) {
              this.afterSave(this.$refs.formulaDialog, res.data.data)
            }
          }
          this.subLoading = false
        }).catch(e => {
          this.subLoading = false
        })
      }
    },
    testHandle () {
      let htmlstr = this.$refs.formulaEditor.getExpression().expression
      this.testLoding = true
      jiexiExec({body: htmlstr}).then(res => {
        if(res.data && res.data.code == 0) {
          console.log(res.data.data)
          this.testParamOption.column = []
          this.formSlotColumn = []
          if(res.data.data && res.data.data.length > 0) {
            this.testLoding = false
            this.testMapParamType = {}
            for(let i in res.data.data) {
              if(this.nodeDataList[res.data.data[i]]) {
                this.$set(this.testMapParamType, this.nodeDataList[res.data.data[i]].id, this.nodeDataList[res.data.data[i]].jvsParamType)
                let tob = {
                  label: this.nodeDataList[res.data.data[i]].name,
                  prop: this.nodeDataList[res.data.data[i]].id
                }
                switch(this.nodeDataList[res.data.data[i]].jvsParamType) {
                  case 'text': tob.type = 'input';break;
                  case 'number': tob.type = 'inputNumber';break;
                  case 'date': tob.type = 'input';break;
                  case 'boolean': tob.type = 'switch';break;
                  case 'object': tob.type = 'jsonEditor';break;
                  case 'array': tob.type = 'jsonEditor';break;
                  case 'file': tob.type = 'jsonEditor';break;
                  default: tob.type = 'jsonEditor';break;
                }
                if(tob.type == 'jsonEditor') {
                  tob.formSlot = true
                  this.formSlotColumn.push(tob)
                }
                this.testParamOption.column.push(tob)
              }
            }
            if(this.testParamOption.column.length > 0) {
              this.testParam = {}
              this.testParamVisible = true
            }
          }else{
            this.testFunc()
          }
        }else{
          this.testLoding = false
        }
      })
    },
    testFunc () {
      let obj = {body: this.$refs.formulaEditor.getExpression().expression}
      if(this.testParam) {
        obj.map = this.testParam
      }
      if(this.testMapParamType) {
        obj.mapParamType = this.testMapParamType
      }
      testExec(obj).then(res => {
        this.testLoding = false
        this.testParamOption.submitLoading = false
        if(res.data && res.data.code == 0) {
          console.log(res.data.data)
          this.testResult = res.data.data
          this.testResultShow = true
          if(this.testParamVisible) {
            this.testParamClose()
          }
        }
      }).catch(e => {
        this.testLoding = false
        this.testParamOption.submitLoading = false
      })
    },
    // 添加说明
    showInfo (item) {
      this.currentData = item
    },
    helpCenter(str) {
      this.$openUrl('', '_blank', str)
    },
    setParentHandle (list, parent, nodeDataList, index) {
      for(let i in list) {
        list[i].level = index
        this.$set(nodeDataList, list[i].id, {...list[i], parent: parent})
        if(list[i].children && list[i].children.length > 0) {
          this.setParentHandle(list[i].children, parent, nodeDataList, index+1)
        }
      }
    },
    findParamItem (key, item) {
      for(let i in this.funcList) {
        if(this.funcList[i].data && this.funcList[i].data.length > 0) {
          for(let j in this.funcList[i].data) {
            if(this.funcList[i].data[j].data && this.funcList[i].data[j].data.length > 0) {
              this.funcList[i].data[j].data.filter(fit => {
                if(fit.id == key) {
                  this.$set(item, 'id', fit.id)
                  this.$set(item, 'name', fit.name)
                  this.$set(item, 'param', fit.param)
                  this.$set(item, 'parentIndex', i)
                }
              })
            }
          }
        }
      }
    },
    matchWords (matchword) {
      let temp = []
      for(let k in this.nodeDataList) {
        if(this.nodeDataList[k].name.includes(matchword) || this.nodeDataList[k].id.includes(matchword) || (this.nodeDataList[k].shortName && this.nodeDataList[k].shortName.includes(matchword))) {
          let color = ''
          let typeLabel = ''
          switch (this.nodeDataList[k].jvsParamType) {
            case 'text': color = 'blue'; typeLabel = '文字';break;
            case 'number': color = 'yellow'; typeLabel = '数字';break;
            case 'date': color = 'green'; typeLabel = '时间';break;
            case 'array': color = 'purple'; typeLabel = '数组';break;
            case 'object': color = 'red'; typeLabel = '对象';break;
            case 'bool': color = 'pink'; typeLabel = '布尔';break;
            case 'file': color = 'dark-yellow'; typeLabel = '文件';break;
            case 'any':
            case 'unknown': color = 'cyan'; typeLabel = '未知';break;
            default: ;break;
          }
          temp.push({...this.nodeDataList[k], color: color, typeLabelName: typeLabel})
        }
      }
      this.matchResult = temp
    },
    matchClick (data) {
      let { index, item } = data
      this.addVariable(item, (index > 0 ? (index - 1) : index))
    },
    coderChange (coder) {
      this.testResult = null
    },
    copyTestHandle (result) {
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
    jsonChange (con, prop) {
      if(con == 'error') {
        this.$set(this.testParam, prop, '')
      }else{
        this.$set(this.testParam, prop, con)
      }
    },
    testParamSubmit () {
      this.testParamOption.submitLoading = true
      this.testFunc()
    },
    testParamClose () {
      this.testParam = null
      this.testMapParamType = null
      this.testParamVisible = false
    }
  },
};
</script>
<style lang="scss" scoped>
.result-box{
  padding: 0 20px;
  padding-bottom: 15px;
  h4{
    margin: 0;
    height: 32px;
    line-height: 32px;
    margin-top: 10px;
  }
  .result-text{
    margin-top: 5px;
    background: #f5f7fa;
    overflow: hidden;
    border-radius: 5px;
    max-height: 160px;
    overflow-y: auto;
    /deep/.jv-container{
      background: #f5f7fa;
      max-height: calc(100vh - 300px);
      overflow: hidden;
      overflow-y: auto;
    }
    section{
      padding: 10px;
    }
    /deep/.jv-container .jv-code{
      padding: 10px;
    }
  }
}
</style>
<style lang="scss">
.formula-component-dialog {
  .el-dialog {
    margin-top: calc(50vh - 340px)!important;
    .el-dialog__body{
      padding: 10px 20px!important;
    }
    .formula-panel-box{
      width: 858px;
      .cm-field{
        display: inline-block;
        border-radius: 4px;
        padding: 0 5px;
        margin: 1px;
        color: #fff;
        font-size: 12px;
        line-height: 20px;
        font-family: Helvetica Neue,Helvetica,PingFang SC,Hiragino Sans GB,Microsoft YaHei,Arial,sans-serif;
      }
      .cm-field-function{
        color: #651573;
        padding: 0;
      }
      .cm-field-param1{
        background: #1B64E5;
      }
      .cm-field-param2{
        background: #E6A23C;
      }
      .cm-field-param3{
        background: #2ea320;
      }
      .cm-bracket{
        color: #997;
      }
      .formula-panel-title {
        margin: 0;
        padding: 0 10px;
        font-size: 12px;
        font-weight: 500;
        line-height: 30px;
        color: rgba(0,0,0,.8);
        border-bottom: 1px solid rgba(17, 31, 44, 8%);
      }
      .formula-panel-content{
        margin: 0;
        padding: 10px 0;
        overflow-y: auto;
        flex: 1 1;
      }
      .formula-panel-top{
        border: 1px solid rgba(17,31,44,.08);
        border-radius: 8px;
        width: 100%;
        .formula-panel{
          flex: none;
          height: 230px;
          display: flex;
          flex-direction: column;
          .formula-panel-title{
            margin: 0;
            padding: 0 10px;
            font-size: 12px;
            font-weight: 500;
            line-height: 30px;
            color: rgba(0,0,0,.8);
            border-bottom: 1px solid rgba(17,31,44,.08);
            display: flex;
            align-items: center;
            .formula-panel-subtitle{
              font-size: 12px;
              margin-left: 5px;
            }
          }
          .formula-panel-content{
            padding: 0;
            height: 200px;
            overflow: hidden;
            .formula-editor{
              height: 100%;
              box-sizing: border-box;
            }
          }
        }
        .formula-test-result{
          flex: 1;
          max-width: 50%;
          overflow: hidden;
          margin-left: 10px;
          .formula-test-result-text{
            max-width: 100%;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: pre;
            color: #36B452;
            box-sizing: border-box;
            cursor: pointer;
          }
        }
        .formula-set{
          width: 210px;
          position: absolute;
          right: 40%;
          top: 2px;
          height: 26px;
          overflow: hidden;
          display: flex;
          align-items: center;
          .el-select{
            margin-left: 4px;
            .el-input{
              .el-input__inner{
                border: 0;
                background: #F5F6F7;
                height: 26px;
                line-height: 26px;
              }
            }
          }
        }
        .formula-desc{
          position: absolute;
          right: 0;
          top: 2px;
          width: 50%;
          max-width: 50%;
          height: 26px;
          overflow: hidden;
          display: flex;
          align-items: center;
          border-radius: 4px;
          &.show{
            background: #F5F6F7;
            right: 10px;
          }
          &.rule{
            width: calc(60% - 230px);
            max-width: calc(60% - 230px);
          }
          .name-text{
            text-align: right;
            flex: 1;
            font-family: Microsoft YaHei-Regular, Microsoft YaHei;
            font-weight: 400;
            font-size: 14px;
            color: #363B4C;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: pre;
            .el-input__inner{
              border: 0;
              height: 30px;
              line-height: 30px;
              background: transparent;
            }
          }
          .edit-icon{
            width: 30px;
            height: 30px;
            border-radius: 4px;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            svg{
              width: 16px;
              height: 16px;
              fill: #6F7588;
            }
            &.editing{
              svg{
                fill: #1E6FFF;
              }
            }
          }
        }
      }
      .formula-panel-bottom{
        display: flex;
        flex: auto;
        margin-top: 10px;
        flex-wrap: nowrap;
        color: #333;
        overflow: hidden;
        justify-content: space-between;
        .formula-panel{
          flex: none;
          width: 250px;
          height: 310px;
          display: flex;
          flex-direction: column;
          border: 1px solid rgba(17,31,44,.08);
          border-radius: 8px;
          margin-left: 10px;
          .formula-panel-title{
            height: 46px;
            line-height: 46px;
            border-bottom: none;
            border-bottom: 1px solid rgba(17,31,44,.08);
            .formula-panel-title-text{
              flex: 1 1;
              overflow: hidden;
              font-family: Microsoft YaHei-Regular, Microsoft YaHei;
              font-weight: 400;
              font-size: 14px;
              color: #363B4C;
            }
          }
          .formula-panel-content{
            .blue{
              color: #0089ff;
              background: #ebf6ff;
            }
            .yellow{
              color: #ff9200;
              background: #fff8eb;
            }
            .green{
              color: #00b853;
              background: #ebfaf2;
            }
            .purple{
              color: #5c72ff;
              background: #f2f4ff;
            }
            .red{
              color: #ff7357;
              background: #fff4f2;
            }
            .cyan{
              color: #70acc3;
              background: #eaf0f1;
            }
            .pink{
              color: #FF69B4;
              background: #fdf3ff;
            }
            .dark-yellow{
              color: #ebc60f;
              background: #f7f7f7;
            }
            .formula-panel-item-filed{
              list-style-type: none;
              .formula-filed-item{
                position: relative;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
                text-decoration: none;
                color: #333;
                padding: 0 6px 0 18px;
                display: block;
                height: 32px;
                line-height: 32px;
                cursor: pointer;
                font-size: 12px;
                .formula-filed-title{
                  color: #171a1d;
                  font-size: 12px;
                  text-overflow: ellipsis;
                  white-space: nowrap;
                  overflow: hidden;
                  display: block;
                  width: 146px;
                  margin-right: 40px;
                }
                .formula-filed-capsule{
                  position: absolute;
                  top: 4px;
                  right: 8px;
                  bottom: 4px;
                  text-align: center;
                  padding: 0 8px;
                  line-height: 24px;
                  border-radius: 4px;
                }
              }
              .formula-filed-item:hover{
                background: rgba(126,134,142,.08);
                color: rgba(0,0,0,.75);
              }
              .el-collapse{
                border: 0;
                .el-collapse-item{
                  .el-collapse-item__header{
                    cursor: pointer;
                    display: flex;
                    align-items: center;
                    height: 26px;
                    line-height: 26px;
                    border: 0;
                    padding: 0 10px;
                    span{
                      overflow: hidden;
                      text-overflow: ellipsis;
                      white-space: pre;
                      max-width: calc(100% - 26px);
                      display: inline-block;
                    }
                    .el-collapse-item__arrow{
                      margin-left: 5px;
                      vertical-align: super;
                    }
                  }
                  .el-collapse-item__wrap{
                    border-bottom: 0;
                    .el-collapse-item__content{
                      padding-bottom: 0;
                    }
                  }
                }
              }
              .function-panel-filed-list{
                margin: 0;
                padding: 0;
                color: #333;
                .function-panel-filed-li{
                  cursor: pointer;
                  padding: 0!important;
                  line-height: 32px;
                  min-height: 32px;
                  color: #171a1d;
                  position: relative;
                  .self-item{
                    width: 100%;
                    padding: 0 5px 0 22px;
                    box-sizing: border-box;
                  }
                  .short-name{
                    width: 100%;
                    padding: 0 5px 0 22px;
                    box-sizing: border-box;
                    color: #b5b8be;
                    font-size: 12px;
                    line-height: 16px;
                    padding-bottom: 4px;
                  }
                  .formula-func-title{
                    margin-right: 50px;
                    display: block;
                    text-overflow: ellipsis;
                    white-space: nowrap;
                    overflow: hidden;
                    color: #6F7588;
                  }
                  .formula-func-capsule{
                    position: absolute;
                    top: 4px;
                    right: 8px;
                    bottom: 4px;
                    text-align: center;
                    padding: 0 8px;
                    line-height: 24px;
                    border-radius: 4px;
                    height: 24px;
                  }
                  .formula-func-capsule.yellow{
                    color: #ff9200;
                    background: #fff8eb;
                  }
                }
                .function-panel-filed-li:hover {
                  background: #fff!important;
                }
                .formlula-tree-node:hover{
                  >.self-item, >.short-name{
                    background: rgba(126,134,142,.08);
                  }
                }
                .function-panel-filed-li.function-panel-filed-li-haschildren{
                  height: auto;
                }
              }
            }
          }
          .info-section{
            padding: 16px;
            p{
              position: relative;
              margin: 0;
              min-height: 24px;
              font-family: Microsoft YaHei-Regular, Microsoft YaHei;
              font-weight: 400;
              font-size: 12px;
              color: #363B4C;
              line-height: 16px;
              padding-left: 5px;
              span.param{
                display: inline-block;
                border-radius: 4px;
                padding: 3px 8px;
                color: #fff;
                font-family: Microsoft YaHei-Regular, Microsoft YaHei;
                font-weight: 400;
                font-size: 14px;
                color: #FFFFFF;
                background: #1B64E5;
                margin: 0 5px;
              }
              span.fun{
                display: inline-block;
                font-size: 12px;
                line-height: 16px;
                color: #651573;
              }
              a{
                display: inline-block;
                height: 24px;
                font-family: Microsoft YaHei-Regular, Microsoft YaHei;
                font-weight: 400;
                font-size: 12px;
                color: #1E6FFF;
                line-height: 24px;
                text-decoration: underline;
                cursor: pointer;
              }
            }
            p::before{
              content: "";
              position: absolute;
              display: block;
              left: 0;
              top: 11px;
              width: 2px;
              height: 2px;
              background: #363B4C;
              border-radius: 100%;
              overflow: hidden;
            }
            p:nth-last-of-type(1)::before{
              display: none;
            }
            .info-text{
              font-size: 12px;
              line-height: 20px;
            }
          }
        }
        .formula-panel:nth-of-type(1) {
          margin-left: 0;
        }
        .formula-panel-func-desc{
          width: 310px;
          background: #F5F6F7;
          border-radius: 4px;
          border: 0;
        }
        .formula-panel.loading{
          background-color: rgba(255, 255, 255, 0.8);
          background-image: url('../../../../public/jvs-ui-public/img/loading.gif');
          background-repeat: no-repeat;
          background-position: center;
          background-position: center;
          //background-size: 200px 160px;
        }
        .formula-panel-rule.formula-panel-rule:nth-of-type(1){
          width: 375px;
        }
      }
      .footer-button{
        display: flex;
        justify-content: center;
        align-items: center;
        margin-top: 10px;
      }
    }
    .formula-panel-box-rule{
      width: 983px!important;
    }
  }
}
</style>
