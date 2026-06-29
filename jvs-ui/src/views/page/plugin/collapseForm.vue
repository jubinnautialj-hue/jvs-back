<template>
  <div class="tab-form-designer" style="width:100%;overflow:hidden;">
    <div class="cont">
      <div class="contdiv"
        :style="contdivstyle"
        @dragenter="dragenter"
        @dragleave="dragleave"
        @dragover="dragover"
        @dragover.prevent
      ></div>
      <el-form
        style="width:100%;"
        :model="dynamicValidateForm"
        ref="dynamicValidateForm"
        class="demo-dynamic"
        :label-position="formsetting.labelposition"
        :label-width="formsetting.labelwidth+'px'"
        :size="formsetting.formsize || 'mini'"
        >
        <!-- 折叠面板 -->
        <el-collapse v-model="collapseKey" :accordion="data.accordion" class="tab-design-collapse" @change="handleClick">
          <el-collapse-item
            v-for="(ti, tix) in data.dicData"
            :key="'tab'+ti.name"
            :title="ti.label"
            :name="ti.name"
          >
            <template slot="title">
              <div style="flex: 1;overflow: hidden;display: flex;align-items: center;justify-content: space-between;margin-right: 10px;">
                <span>{{ti.label}}</span>
                <i v-if="data.detachData" class="el-icon-setting" style="margin-left: 5px;" @click.stop="tabItemSetting(ti, tix)"></i>
              </div>
            </template>
            <template>
              <div style="position: relative;">
                <div class="contdiv"
                  :style="contdivstyle"
                  @drop="drop(ti.name)"
                  @dragover.prevent
                ></div>
                <el-row :gutter="10" v-if="data.column[ti.name] && data.column[ti.name].length>0">
                  <el-col v-for="(ci, cinx) in data.column[ti.name]" :key="ci.prop" :span="ci.span">
                    <div
                      :class="{
                        'tab-item-form-item': true,
                        'tab-item-form-item-active': (actid === ci.prop && actCom && actCom.id == ci.id),
                        'source': startindexTabItem == cinx,
                        'target': endindexTabitem == cinx
                      }"
                      draggable
                      @dragstart.stop="dragstartTabItem(cinx,ci, ti.name)"
                      @dragend.stop="dragendTabItem(cinx, ti.name)"
                      @drop="dropfunTabItem(cinx, ti.name)"
                      @dragover="dragover2(cinx, ti.name)"
                      @dragover.prevent
                      @click.stop="set(ci)"
                    >
                      <span style="color:#F56C6C" v-if='ci.rules.length > 0 && ci.rules[0].required && ["tableForm", "flowTable", "flowNode", "step", "formbox", "divider","p","tab","tableReadOnly", "reportTable"].indexOf(ci.type) !== -1'>*</span>
                      <div :class="['box', 'step', 'formbox', 'tableForm', 'flowTable', 'flowNode', 'reportTable'].indexOf(ci.type) === -1 ? 'headeritem no-label-item' : 'headeritem'">
                        <el-form-item
                          style="flex:1;display:flex;"
                          :label="ci.label"
                          v-if='(["tableForm", "flowTable", "flowNode", "step", "formbox", "divider","p","tab","tableReadOnly", "reportTable"].indexOf(ci.type) == -1)'
                          :rules="ci.rules"
                          :label-width="formsetting.labelwidth+'px'"
                          :class='(!ci.label || (["tableForm", "flowTable", "flowNode", "divider","p","tab","section","step", "reportTable"].indexOf(ci.type) > -1))? "nolabel-form-item" : ""'
                        >
                          <!-- 一般项 -->
                          <!-- ,"box","link","iframe" -->
                          <FormItem  :item='ci' v-if='["tableForm", "flowTable", "flowNode", "step", "formbox", "divider","p","tab","checkbox","tableReadOnly", "reportTable"].indexOf(ci.type) == -1' :originOption="{...formsetting, formAlign: formsetting.labelposition}" :isView="true" :designId="designId" />
                          <FormItem :form='dynamicValidateForm[ti.value]' :item='ci' v-if='ci.type === "checkbox"' :isView="true" :designId="designId" />
                        </el-form-item>
                        <el-form-item
                          style="flex:1;"
                          label-width="90px"
                          :label="ci.label"
                          v-if='ci.type === "tableReadOnly"'
                          :rules="ci.rules"
                        >
                          <FormItem style="flex:1;"  :item='ci' :isView="true" :designId="designId" />
                        </el-form-item>
                        <!-- 分割线 -->
                        <el-divider style="flex:1;" v-if="ci.type === 'divider'" :content-position='ci.contentposition'>{{ci.text}}</el-divider>
                        <!-- 文字 -->
                        <p style="flex:1;" v-if="ci.type === 'p'"  :style="{'text-align': ci.contentposition,'font-size':ci.fontsize+'px',color:ci.textcolor}">{{ci.text}}</p>
                        <!-- 表格 -->
                        <el-form-item v-if='ci.type==="tableForm"' :label="ci.label" label-width="90px" :class="{'play-table-form-item': ci.type == 'tableForm', 'nolabel-form-item': !ci.label}" style="flex:1;">
                          <div v-if="ci.type === 'tableForm'">
                            <tableForm
                            :com='com'
                            :data='ci'
                            :drag1='drag1'
                            :drag2='drag2'
                            :drag3='drag3Tab'
                            :startindex='startindex'
                            :formsetting="formsetting"
                            :isDetail="isDetail"
                            :designId="designId"
                            @setdrag3='setdrag3'
                            @chonzhihxindex='chonzhihxindex'
                            @clickitem='clickitem'
                            :action="action"
                            :designnerType='designnerType'
                            />
                          </div>
                        </el-form-item>
                        <!-- 流程设计 -->
                        <el-form-item v-if='ci.type==="flowTable"' style="flex:1;" :label="ci.label" label-width="90px">
                          <div>
                            <flowTable
                              :com='com'
                              :data='ci'
                              :formsetting="formsetting"
                              :isDetail="isDetail"
                              :designId="designId"
                              @clickitem='clickitem'
                              :action="action"
                              :designnerType='designnerType'
                              />
                          </div>
                        </el-form-item>
                        <!-- 执行流程 -->
                        <el-form-item v-if='ci.type==="flowNode"' style="flex:1;" :label="ci.label" label-width="90px">
                          <div>
                            <flowNode
                              :com='com'
                              :data='ci'
                              :formsetting="formsetting"
                              :isDetail="isDetail"
                              :designId="designId"
                              @clickitem='clickitem'
                              :action="action"
                              :designnerType='designnerType'
                              />
                          </div>
                        </el-form-item>
                        <!-- 步骤条 -->
                        <el-form-item v-if='ci.type==="step"' :label="ci.label" label-width="90px" :class="{'nolabel-form-item': !ci.label}" style="flex:1;">
                          <div>
                            <stepBar
                              :com='com'
                              :data='ci'
                              :drag1='drag1'
                              :drag2='drag2'
                              :drag3='drag3Tab'
                              :startindex='startindex'
                              :outClick="actid"
                              :formsetting="formsetting"
                              :isDetail="isDetail"
                              :designId="designId"
                              @setdrag3='setdrag3'
                              @chonzhihxindex='chonzhihxindex'
                              @clickitem='clickitem'
                              :action="action"
                              :designnerType='designnerType'
                              />
                          </div>
                        </el-form-item>
                        <!-- 卡片 控制块 -->
                        <el-form-item :label="ci.type == 'formbox' ? ci.label : ''" label-width="90px" :class="{'sectionItem': true,  'nolabel-form-item': !ci.label}" style="flex:1;" v-if='ci.hasChildren || ci.type == "formbox"'>
                          <div>
                            <sectionForm
                              :com='com'
                              :data='ci'
                              :drag1='drag1'
                              :drag2='drag2'
                              :drag3='drag3Tab'
                              :startindex='startindex'
                              :formsetting="formsetting"
                              :isDetail="isDetail"
                              :designId="designId"
                              @setdrag3='setdrag3'
                              :outClick="actid"
                              @chonzhihxindex='chonzhihxindex'
                              @clickitem='clickitem'
                              :action="action"
                              :designnerType='designnerType'
                              />
                          </div>
                        </el-form-item>
                        <!-- 静态表格  报表类 -->
                        <el-form-item class="form-item-reportTable" v-if='ci.type==="reportTable"' :label="ci.label">
                          <div>
                            <reportTable
                              :com='com'
                              :data='ci'
                              :drag1='drag1'
                              :drag2='drag2'
                              :drag3='drag3Tab'
                              :startindex='startindex'
                              :formsetting="formsetting"
                              :isDetail="isDetail"
                              :designId="designId"
                              @setdrag3='setdrag3'
                              @chonzhihxindex='chonzhihxindex'
                              @clickitem='clickitem'
                              :action="action"
                              :designnerType='designnerType'
                            />
                          </div>
                        </el-form-item>
                        <div class="handle-tab-btn">
                          <div class="type-name">{{ci.name}}</div>
                          <i class="el-icon-copy-document copy-item-first" @click.stop="copyFormsItem(ci, cinx, ti.name)"></i>
                          <el-popconfirm title="确定删除吗？" @confirm="del(ci, ti.name)">
                            <i class="el-icon-delete del-item-first" slot="reference" @click.stop=""></i>
                          </el-popconfirm>
                        </div>
                      </div>
                    </div>
                  </el-col>
                </el-row>
                <div v-else @dragover="dragCollapseEnter(ti.name)" class="empty-tab-item"></div>
              </div>
            </template>
          </el-collapse-item>
        </el-collapse>
      </el-form>
    </div>

    <!-- tab项设置 -->
    <el-dialog
      :title="currentSetting ? currentSetting.label : '选项设置'"
      append-to-body
      :visible.sync="dialogVisible"
      :before-close="handleClose">
      <div v-if="dialogVisible">
        <el-form :model="currentSetting" label-width="80px">
          <el-form-item label="数据字段">
            <el-select v-model="currentSetting.prop" placeholder="请选择或输入数据字段" size="mini" clearable allow-create filterable>
              <el-option  v-for='(item,index) in dataModelFields' :key="item.fieldKey + index" :label="item.fieldKey" :value="item.fieldKey">
                <span style="float: left">{{ item.fieldName }}</span>
                <span style="float: right; color: #8492a6; font-size: 13px">{{ item.fieldKey}}</span>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="显示控制">
            <el-button size="mini" @click="setShowHandle('show')">设置</el-button>
          </el-form-item>
          <el-form-item label="禁用控制">
            <el-button size="mini" @click="setShowHandle('disabled')">设置</el-button>
          </el-form-item>
        </el-form>
        <div style="display: flex;align-items: center;justify-content: center;">
          <el-button type="primary" size="mini" @click="settingSubmit">确定</el-button>
          <el-button size="mini" @click="handleClose">取消</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 显示表达式设置 -->
    <el-dialog
      :title="showDisabledType == 'disabled' ? '禁用控制' : (showDisabledType == 'require' ? '动态校验' : '显示控制')"
      append-to-body
      :visible.sync="showExpressVisible"
      :close-on-click-modal="false"
      :before-close="showExpressClose">
      <div v-if="showExpressVisible" class="show-express">
        <div class="left">
          <el-tree
            :data="domTreeData"
            :props="{value: 'prop', children: 'children'}"
            :expand-on-click-node="false"
            :default-expand-all="true"
            @node-click="domNodeClick"></el-tree>
        </div>
        <div class="right">
          <jvs-form :formData="showExpressForm" :option="showExpressOption"></jvs-form>
        </div>
      </div>
      <el-row style="display: flex;justify-content: center;align-items:center;">
        <jvs-button size="mini" type="primary" @click="saveShowExpress">保存</jvs-button>
        <jvs-button size="mini" @click="showExpressClose">取消</jvs-button>
      </el-row>
    </el-dialog>
  </div>
</template>

<script>
import tableForm from './tableForm'
import flowTable from './flowTable'
import flowNode from './flowNode'
import reportTable from './reportTable'
import { deleteExec } from '@/components/basic-container/formula/api'
import {guid} from "@/util/util";
import { copyFormulaComponent } from '@/views/page/api/newDesign'
export default {
  components: {
    FormItem: () => import('@/components/basic-assembly/formitem'),
    tableForm,
    flowTable,
    flowNode,
    sectionForm: () => import('.//sectionForm'),
    stepBar: () => import('.//stepBar'),
    reportTable
  },
  props: {
    // 当前选中的组件
    clickformcom: {
      type: Object,
      default: () => {
        return {}
      }
    },
    // 设计器类型
    designnerType: {
      type: String,
      default: () => {
        return ""
      }
    },
    // 被选中的 id值
    drag1: {
      type: String,
      default: () => {
        return '1'
      }
    },
    drag2: {
      type: String,
      default: () => {
        return '1'
      }
    },
    // 1 没有拖动到 表格中  2 已经拖动到表格中
    drag3: {
      type: String,
      default: () => {
        return '1'
      }
    },
    // 用来判断是否是中间内容的拖动 为 -1 时 就不是  大于 0 的 都是 (此时不进行处理)
    startindex: {
      type: Number,
      default: () => {
        return -1
      }
    },
    field: {
      type: String,
      default: () => {
        return ''
      }
    },
    // 表格的对象
    data: {
      type: Object,
      default: () => {
        return {
          activeName: '',
          dicData: [],
          column: []
        }
      }
    },
    // 左侧被拉动的组件 如果在tab 中放开 则添加到tab 中
    com: {
      type: Object
    },
    action: {
      type: String,
      default: () => {
        return ""
      }
    },
    outClick: {
      type: String
    },
    outCom: {
      type: Object
    },
    formsetting: {
      type: Object
    },
    // 是否为列表页详情按钮
    isDetail: {
      type: Boolean,
      default: () => {
        return false
      }
    },
    designId: {
      type: String
    },
    jvsAppId: {
      type: String
    },
    dataModelFields: {
      type: Array
    },
    domList: {
      type: Array,
      default: () => {
        return []
      }
    },
  },
  computed: {
    column () {
      return this.data.column
    },
    dicData () {
      return this.data.dicData
    },
    contdivstyle(){
      let obj = {}
      // obj.background = this.drag3 === '1' ? 'rgba(0,0,0,0.1)' : 'rgba(0,0,0,0.2)'
      // 左侧或者 中间拖动的时候 要 大等于 10
      obj['z-index'] =  this.drag1 !== '1' ? 100 : -1

      return obj
    }
  },
  data() {
    return {
      startindexTabItem: -1,
      endindexTabitem: -1,
      // 拖动到内部组件  1 没有  2 有
      dragtabitem: '1',
      tabkey: '',
      dynamicValidateForm: {},
      drag3Tab: '1',
      detailTypeList: ["inputReadOnly", "textareaReadOnly", "image", "file", "tableReadOnly"],
      // 被点击选中的id值
      actid: '0',
      actCom: null,
      dialogVisible: false,
      currentSetting: null,
      currentSettingIndex: -1,
      showExpressVisible: false,
      showDisabledType: '',
      domTreeData: [],
      showExpressForm: {
        relation: []
      },
      showExpressOption: {
        cancal: false,
        btnHide: true,
        column: [
          {
            label: '逻辑判定',
            prop: 'showOperator',
            type: 'radio',
            dicData: [
              {label: '且', value: '&&'},
              {label: '或', value: '||'}
            ],
            defaultValue: '||'
          },
          {
            label: '',
            prop: 'relation',
            type: 'tableForm',
            editable: true,
            addBtn: false,
            delBtn: true,
            hideTop: true,
            align: 'left',
            menuAlign: 'left',
            tableColumn: [
              {
                label: '字段名',
                prop: 'label',
                disabled: true
              },
              {
                label: '字段名',
                prop: 'prop',
                hide: true
              },
              {
                label: '比较值',
                prop: 'value'
              }
            ]
          }
        ]
      },
      collapseKey: ''
    };
  },
  methods: {
    dragenter () {
      // console.log('进入目标>>>')
      // this.$emit('setmub', true)
    },
    dragover () {
      // 当在选项卡上滑动的时候
      this.$emit('setdrag3', '2')
      // // console.log('在选项卡上>>>')
    },
    dragleave () {
      this.$emit('setdrag3', '1')
      // console.log('移出目标>>>')
      // this.$emit('setmub', false)
    },
    dragCollapseEnter (name) {
      console.log('进入目标>>>', name)
    },
    // 在组件中的
    dragstartTabItem(index, data, tabkey) {
      this.$emit('setdelcom', data)
      this.$emit('setformcom', data)
      this.$emit('designtype', this.designnerType)
      this.startindexTabItem = index;
      // console.log("开始拖动 小div", index);
      // console.log(index, data)
    },
    dragendTabItem() {
      // 重置数据
      // console.log('中间的end ------------')
      this.startindexTabItem = -1
      this.endindexTabitem = -1
      this.drag3Tab = '1'
      this.$emit("reset")
    },
    // 在组件中滑动
    dragover2(index) {
      if (index !== this.startindexTabItem) {
        this.endindexTabitem = index;
      } else {
        this.endindexTabitem = -1;
      }
    },
    dropfunTabItem(index, tabkey) {
      let bool = this.currentDesignerDealItem(true)
      // console.log(this.startindexTabItem, this.endindexTabitem)
      if(bool) {
        if (
          this.startindexTabItem !== this.endindexTabitem &&
          this.startindexTabItem != -1 &&
          this.endindexTabitem != -1
        ) {
          this.jiaohuanHandle(this.startindexTabItem, this.endindexTabitem, tabkey);
        }
      }else{
        this.startindexTabItem = -1
        this.endindexTabitem = -1
      }
      this.drag3Tab = '1'
    },
    // 交换位置
    jiaohuanHandle (start, end, tabkey) {
      let arr=this.data.column[tabkey ? tabkey : this.tabkey]
      let aa=arr[start]
      if(Math.abs(start-end) == 1) {
        arr[start]=arr[end]
        arr[end]=aa
      }else{
        arr.splice(start, 1)
        arr.splice(end < 0 ? 0 : end, 0, aa)
      }
      this.$set(this.data.column, tabkey ? tabkey : this.tabkey, arr)
    },
    drop (name) {
      let bool = this.currentDesignerDealItem(false)
      // console.log(bool)
      if(bool) {
        // todo 进行添加操作 并且 hxindex 置为 -3
        if (['tab'].indexOf(this.com.type) == -1 ) {
          if(this.com.type === 'imageUpload' || this.com.type === 'fileUpload'){
            this.com.action = this.action
          }
          if(this.data.addcolumn){
            this.data.addcolumn(name, this.com)
          }else{
            if(!this.data.column[name]) {
              this.data.column[name] = []
            }
            this.com.parentType = this.data.type
            this.com.parentKey = this.data.parentKey ? (this.data.parentKey + '.') : '' + this.data.prop + '.' + name
            if(this.com.type == 'flowTable') {
              this.com.span = 24
              this.com.tableColumn.filter(ci => {
                ci.parentKey = (this.com.parentKey+'.'+this.com.prop)
              })
            }
            if(this.com.type == 'flowNode') {
              this.com.span = 24
            }
            this.data.column[name].push(this.com)
          }
          this.$emit('setdrag3', '2')
          this.initForm()
        } else if (this.startindex === '-1') {
          // 中间的拖动到表格中 不处理
        } else {
          if(this.com.type === 'section'){
            // this.$message.error('不能在选项卡中添加作用域')
            this.$notify({
              title: '提示',
              message: '不能在选项卡中添加作用域',
              position: 'bottom-right',
              type: 'error'
            });
          }else{
            // this.$message.error('不能在选项卡中添加选项卡')
            this.$notify({
              title: '提示',
              message: '不能在选项卡中添加选项卡',
              position: 'bottom-right',
              type: 'error'
            });
          }
        }
      }
      this.$emit('chonzhihxindex')
    },
    submitForm(formName) {
      // console.log(this.dynamicValidateForm)
      this.$refs[formName].validate(valid => {
        if (valid) {
          // console.log(this.dynamicValidateForm)
          alert("submit!");
        } else {
          // console.log("error submit!!");
          return false;
        }
      });
    },
    resetForm(formName) {
      this.$refs[formName].resetFields();
    },
    removeDomain(item) {
      var index = this.dynamicValidateForm.domains.indexOf(item);
      if (index !== -1) {
        this.dynamicValidateForm.domains.splice(index, 1);
      }
    },
    set (data) {
      this.actid = data.prop
      this.actCom = JSON.parse(JSON.stringify(data))
      this.$emit('clickitem', data)
    },
    del (data, tabkey) {
      if(data.formula) {
        deleteExec(this.designId, data.formula).then(res => {
          if(res.data && res.data.code == 0) {
            if(this.data.deletecolumn) {
              this.data.deletecolumn(tabkey || this.tabkey, data.prop)
            }else{
              this.data.column[tabkey || this.tabkey || this.data.activeName] = this.data.column[tabkey || this.tabkey || this.data.activeName].filter(item => item.prop != data.prop)
            }
          }
        })
      }else{
        if(this.data.deletecolumn) {
          this.data.deletecolumn(tabkey || this.tabkey, data.prop)
        }else{
          this.data.column[tabkey || this.tabkey || this.data.activeName] = this.data.column[tabkey || this.tabkey || this.data.activeName].filter(item => item.prop != data.prop)
        }
      }
      this.$forceUpdate()
    },
    handleClick (act) {
      if(this.data.accordion) {
        this.tabkey = act
        this.collapseKey = act
        this.data.activeName = this.tabkey
      }else{
        this.collapseKey = act
        if(act.length == 1) {
          this.tabkey = act[0]
          if(this.data.handleClick) {
            this.data.handleClick(this.tabkey)
          }else{
            this.data.activeName = this.tabkey
          }
        }else{
          this.data.activeName = ''
        }
      }
    },
    // 选中中间的组件
    clickitem (data) {
      // console.log('data..')
      // console.log(data)
      this.$emit('setformcom', data)
      this.$emit('clickitem', data) // --------选项卡嵌套表格，点击设计行内组件
    },
    // 拖动到表格
    setdrag3(data){
      this.drag3Tab = data
      this.$emit('setdrag3', data)
    },
    chonzhihxindex () {
      this.hxindex = -3
    },
    initForm() {
      this.data.column = this.initColumn(this.data.dicData, this.data.column)
      for(let i in this.data.column) {
        if(!this.dynamicValidateForm[i]) {
          this.dynamicValidateForm[i] = {}
        }
        let obj = {}
        for(let j in this.data.column[i]) {
          let item = this.data.column[i][j]
          if(item.showFrom.indexOf("sqlType") === -1) {
            item.showFrom.push('sqlType')
          }
          if(this.isDetail) {
            item.disabled = true
          }
          if(!item.sqlType) {
            item.sqlType = 'varchar'
          }
          obj[item.prop] = null
          if(item.type === 'checkbox' || item.type === 'imageUpload' || item.type === 'fileUpload' || item.type === 'image' || item.type === 'file'){
            obj[item.prop] = []
          }
          if(item.type === 'department' || item.type === 'role' || item.type === 'user'){
            obj[item.prop] = []
          }
          if(item.type === 'imageUpload' || item.type === 'fileUpload') {
            item.action = this.action
            this.$forceUpdate()
          }
          this.dynamicValidateForm[i] = obj
        }
      }
      // console.log(this.dynamicValidateForm)
    },
    initColumn (dicData, column) {
      let list = this.getValueOfDicData(dicData)
      for(let i in dicData) {
        if(!column[dicData[i].value]) {
          column[dicData[i].value] = []
        }else{
          // 配置改变key值
          let keys = Object.keys(column)
          if(keys.indexOf(dicData[i].value) == -1) {
            for(let j in keys) {
              if(list.indexOf(keys[j]) == -1) {
                column[dicData[i].value] = JSON.parse(JSON.stringify(column[keys[j]]))
                delete column[keys[j]]
              }
            }
          }
        }
      }
      return column
    },
    getValueOfDicData (dicData) {
      let temp = []
      for(let i in dicData) {
        temp.push(dicData[i].name)
      }
      return temp
    },
    // 判断当前设计和组件是否能组合
    currentDesignerDealItem (needMessage) {
      // console.log(this.designnerType)
      if(this.designnerType == 'infoForm') {
        if((this.com.type && this.detailTypeList.indexOf(this.com.type) == -1) || !this.com.type) {
          if(needMessage) {
            this.$notify({
              title: '提示',
              message: '基本信息不可放入表单组件！',
              position: 'bottom-right',
              type: 'warning'
            });
          }
          this.overlapping = true
          return false
        }else{
          this.overlapping = false
          return true
        }
      }else if(this.designnerType == 'dataForm'){
        console.log(this.com)
        if((this.com.type && this.detailTypeList.indexOf(this.com.type) == -1) || !this.com.type) {
          this.overlapping = false
          return true
        }else{
          if(needMessage) {
            this.$notify({
              title: '提示',
              message: '表单设计不可放入详情组件！',
              position: 'bottom-right',
              type: 'warning'
            });
          }
          this.overlapping = true
          return false
        }
      }else{
        this.overlapping = false
        return true
      }
    },
    tabItemSetting (item, index) {
      this.currentSetting = JSON.parse(JSON.stringify(item))
      this.currentSettingIndex = index
      this.dialogVisible = true
    },
    settingSubmit () {
      if(this.currentSettingIndex > -1) {
        if(this.currentSetting.prop) {
          this.$set(this.data.dicData[this.currentSettingIndex], 'prop', this.currentSetting.prop)
        }else{
          this.$set(this.data.dicData[this.currentSettingIndex], 'prop', '')
        }
        if(this.currentSetting.disabledExpress) {
          this.$set(this.data.dicData[this.currentSettingIndex], 'disabledExpress', this.currentSetting.disabledExpress)
        }
        if(this.currentSetting.requireExpress){
          this.$set(this.data.dicData[this.currentSettingIndex], 'requireExpress', this.currentSetting.requireExpress)
        }
        if(this.currentSetting.displayExpress){
          this.$set(this.data.dicData[this.currentSettingIndex], 'displayExpress', this.currentSetting.displayExpress)
        }
      }
      this.handleClose()
    },
    handleClose () {
      this.currentSetting = null
      this.currentSettingIndex = -1
      this.dialogVisible = false
    },
    // 设置显示控制
    setShowHandle (oprate) {
      this.showDisabledType = oprate
      if(oprate == 'disabled') {
        if(this.currentSetting.disabledExpress) {
          this.$set(this.showExpressForm, 'relation', this.currentSetting.disabledExpress)
        }
        this.domTreeData = []
        this.getDomTree(this.domList, this.domTreeData)
      }else if(oprate == 'require') {
        if(this.currentSetting.requireExpress) {
          this.$set(this.showExpressForm, 'relation', this.currentSetting.requireExpress)
        }
        this.domTreeData = []
        this.getDomTree(this.domList, this.domTreeData)
      }else{
        if(this.currentSetting.displayExpress) {
          this.$set(this.showExpressForm, 'relation', this.currentSetting.displayExpress)
        }
        this.domTreeData = []
        this.getDomTree(this.domList, this.domTreeData)
      }
      this.$set(this.showExpressForm, 'showOperator', this.currentSetting.showOperator)
      this.showExpressVisible = true
    },
    showExpressClose () {
      this.showExpressVisible = false
      this.showExpressForm = {
        relation: []
      }
    },
    // 获取设计dom树
    getDomTree (list, result, prop, comParentKey) {
      for(let i in list) {
        if(["p", "divider", "box", "tableForm", "reportTable", "button", "link", "iframe"].indexOf(list[i].type) == -1) {
          let temp = {
            label: list[i].label,
            prop: list[i].prop
          }
          if(prop) {
            temp.parent = prop
          }
          if(["formbox"].indexOf(list[i].type) == -1 && list[i].children && list[i].children.length > 0) {
            temp.children = []
            let pa = []
            if(prop) {
              pa  = prop
            }
            pa.push(list[i].prop)
            this.getDomTree(list[i].children, temp.children, pa)
          }
          if(["tab", "step"].indexOf(list[i].type) > -1) {
            temp.children = []
            for(let t in list[i].dicData) {
              if(list[i].dicData[t].name && list[i].column && list[i].column[list[i].dicData[t].name] && list[i].column[list[i].dicData[t].name].length > 0) {
                let tp = {
                  label: list[i].dicData[t].label,
                  prop: list[i].dicData[t].name,
                  children: []
                }
                let dp = []
                if(prop) {
                  dp = prop
                }
                tp.parent = [...dp, list[i].prop]
                if(comParentKey) {
                  this.getDomTree(list[i].column[list[i].dicData[t].name] , result, prop, comParentKey)
                }else{
                  this.getDomTree(list[i].column[list[i].dicData[t].name] , tp.children, [...tp.parent, list[i].dicData[t].name])
                  temp.children.push(tp)
                }
              }
            }
          }
          if(!comParentKey) {
            result.push(temp)
          }
        }else{
          if(['tableForm'].indexOf(list[i].type) > -1 && comParentKey) {
            let pros = comParentKey.split('.')
            if(list[i].prop == pros[pros.length-1]) {
              list[i].tableColumn.filter(ti => {
                result.push({
                  label: ti.label,
                  prop: ti.prop
                })
              })
            }
          }
        }
      }
    },
    domNodeClick (data) {
      let temp = {
        label: data.label,
        prop: data.prop,
        value: ''
      }
      if(data.parent) {
        temp.parent = data.parent
      }
      this.showExpressForm.relation.push(temp)
    },
    // 保存
    saveShowExpress () {
      let temp = []
      if(this.showExpressForm.relation && this.showExpressForm.relation.length > 0) {
        temp = this.showExpressForm.relation
      }
      if(this.showDisabledType == 'disabled') {
        this.$set(this.currentSetting, 'disabledExpress', temp)
      }else if(this.showDisabledType == 'require'){
        this.$set(this.currentSetting, 'requireExpress', temp)
      }else{
        this.$set(this.currentSetting, 'displayExpress', temp)
      }
      this.$set(this.currentSetting, 'showOperator', this.showExpressForm.showOperator || '||')
      this.showExpressClose()
    },
    // 复制表单项
    copyFormsItem(item, index, act) {
      let copy = JSON.parse(JSON.stringify(item))
      item.id = guid()
      item.prop = (item.type + guid())
      let needPlaceFormula = false
      if(item.defaultOrigin && item.defaultOrigin == 'formmula' && item.formula) {
        needPlaceFormula = true
      }else{
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
              this.data.column[act].splice(index, 0, copy)
            }else{
              this.data.column[act].push(copy)
            }
            this.$nextTick(() => {
              this.set(item)
            })
          }
        })
      }else{
        if(index > -1) {
          this.data.column[act].splice(index, 0, copy)
        }else{
          this.data.column[act].push(copy)
        }
        this.$nextTick(() => {
          this.set(item)
        })
      }
    },
  },
  watch: {
    outClick: {
      handler(newVal, oldVal) {
        if(newVal != this.actid) {
          this.actid = '0'
        }
      }
    },
    outCom: {
      handler(newVal, oldVal) {
        if(newVal) {
          if(newVal.prop != this.actid || newVal.id != this.actCom.id) {
            this.actid = '0'
            this.actCom = null
          }
        }else{
          this.actid = '0'
          this.actCom = null
        }
      }
    },
    data: {
      handler() {
        if(this.data.accordion) {
          if(!this.collapseKey) {
            this.collapseKey = this.data.activeName
          }
        }else{
          if(this.data.activeName && !(this.collapseKey && this.collapseKey.length > 0)) {
            this.collapseKey = [this.data.activeName]
          }
        }
        for(let i in this.data.column) {
          if(this.isDetail && this.data.column[i] && this.data.column[i].length > 0) {
            this.data.column[i].filter(tit => {
              tit.disabled = true
            })
          }
        }
      },
      deep: true
    }
  },
  mounted() {
    this.tabkey = this.data.activeName
    if(this.data.status == 'collapse') {
      if(this.data.accordion) {
        this.collapseKey = this.data.activeName
      }else{
        this.collapseKey = [this.data.activeName]
      }
    }
  },
  created() {
    this.initForm()
  }
};
</script>

<style lang="scss" scoped>
.headeritem {
  display: block;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  .el-form-item{
    margin: 0;
  }
}
.cont{
  position: relative;
  width: 100%;
  overflow: hidden;
  box-sizing: border-box;
  margin: 0;
  margin-top: 10px;
  /deep/.el-form:not(.el-form--label-top){
    .tab-item-form-item{
      padding-top: 34px;
    }
  }
}
.contdiv{
  position: absolute;
  top: 0;
  right: 0;
  left: 0;
  bottom: 0;
  z-index: 100;
  transition: all 0.5s;
  border-radius: 3px;
  overflow: hidden;
}
.handle-tab-btn{
  height: 24px;
  position: absolute;
  right: 1px;
  top: 1px;
  background-color: #fff;
  border-radius: 0px 6px 0px 6px;
  display: none;
  align-items: center;
  z-index: 1;
  vertical-align: middle;
  .type-name{
    line-height: 24px;
    padding: 0 8px;
    font-family: Microsoft YaHei-Regular, Microsoft YaHei;
    font-weight: 400;
    font-size: 12px;
    color: #363B4C;
  }
  .copy-item-first, .del-item-first{
    padding: 0 8px;
    font-size: 14px;
    cursor: pointer;
    border-left: 1px solid #D7D8DB;
  }
  .del-item-first{
    margin-top: 5px;
  }
}
.tab-item-form-item{
  position: relative;
  padding: 16px 16px 0;
  border-radius: 6px;
  box-sizing: border-box;
  overflow: hidden;
  cursor: move;
}
.tab-item-form-item-active{
  background: #DDEAFF;
  .handle-tab-btn{
    display: flex;
  }
}
.tab-item-form-item:not(.tab-item-form-item-active):hover {
  background: #F5F6F7;
  .handle-tab-btn{
    display: flex;
    .type-name{
      display: none;
    }
    .copy-item-first{
      border-left: 0;
    }
  }
}
.empty-tab-item{
  height: 115px;
  border: 1px dashed #3a88ed;
  margin-bottom: 16px;
}
.empty-tab-item::after{
  content: '请拖入组件';
  color: #959595;
  display: block;
  text-align: center;
  line-height: 115px;
}
/deep/.no-label-item{
  .el-form-item {
    display: block!important;
    .el-form-item__content {
      margin-left: 0!important;
      flex: 1;
    }
  }
}
.tab-design-collapse{
  margin-bottom: 1px;
  /deep/.el-collapse-item{
    .el-collapse-item__header{
      display: flex!important;
    }
  }
}
</style>