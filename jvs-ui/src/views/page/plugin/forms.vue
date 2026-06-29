<template>
  <el-card class="elcard">
    <div slot="header" class="clearfix">
      <span>{{title}}</span>
      <jvs-button v-if="formId" size="mini" v-show="needBtn" style="float: right; padding: 3px 0;margin-left:5px" type="text" @click="exportHandle">导出</jvs-button>
      <el-upload
        v-if="formId"
        style="float: right; padding: 3px 0;margin-left:5px;margin-top:-7px;"
        :action="`/mgr/jvs-design/app/${$route.query.jvsAppId}/form/design/import?formId=${formId}`"
        :multiple="false"
        :limit="1"
        ref="uploadBtn"
        :file-list="fileList"
        :show-file-list="false"
        :on-success="uploadSuccess"
        :on-error="errHandle"
        :headers="headers"
      >
      <jvs-button ref="importBtn" size="mini" v-show="needBtn" type="text">导入</jvs-button>
      </el-upload>
      <jvs-button size="mini" v-show="needBtn && formJson" style="float: right; padding: 3px 0;margin-left:5px" type="text" @click="viewData">数据结构</jvs-button>
      <jvs-button size="mini" v-show="needBtn" style="float: right; padding: 3px 0;margin-left:5px" type="text" @click="save">保存</jvs-button>
      <jvs-button size="mini" v-show="needBtn" style="float: right; padding: 3px 0;margin-left:5px" type="text" @click="yulan">预览</jvs-button>
      <!-- <jvs-button style="float: right; padding: 3px 0;margin-left:5px" type="text" @click="()=>this.$emit('downloadvue')">下载源码</jvs-button> -->
    </div>
    <div
      class="elcardzz"
      @dragover="dragover"
      @dragleave="dragleave"
      @drop="dropfun"
      @dragover.prevent
      :style="styleCard"
    ></div>
    <!-- <span>{{drag1}} {{drag2}} {{drag3}} {{startindex}} {{endindex}}</span> -->
    <div class="formscont">
      <div v-if="hxindex== -1 && drag3 !== '2'" class="hxdiv"></div>
      <el-form
        :model="from"
        :label-position="formsetting.labelposition"
        :label-width="formsetting.labelwidth+'px'"
        :size="formsetting.formsize"
        :class="{'jvs-form jvs-form-for-design': true, 'jvs-form-autoflexable': (formsetting.labelWidth == 'auto' || formsetting.labelwidth == 'auto' || formsetting.formAuto ), 'jvs-form-transparent': formsetting.useElStyle}"
      >
        <el-row :gutter="10" v-if="!freshBoolean">
          <el-col v-for="(item,index) in fromData" :key="index" :span="item.span">
            <div
              :class="{'formitem': true, 'clicked': (actid === item.prop && actCom && actCom.id == item.id), 'source': startindex == index, 'target': endindex == index}"
              draggable
              @dragstart="dragstart(index,item)"
              @dragend="dragend(index)"
              @dragover="dragover2(index)"
              @dragleave="dragleave2(index)"
              @drop="dropfun2(index)"
              @dragover.prevent
              @click="clickitem(item)"
            >
              <div :class="{'formitem2': true, 'active-formitem2': (actid === item.prop && actCom && actCom.id == item.id)}">
                <div class="hxdiv" v-if="hxindex==index && drag3 !== '2'"></div>

                <el-form-item :class='(!item.label || (["tableForm", "divider","p","tab","section","step", "reportTable", "flowTable", "flowNode"].indexOf(item.type) > -1))? "nolabel-form-item" : ""'
                  v-if='["tableForm", "formbox", "divider","p","tab","section", "step", "reportTable", "childrenForm", "flowTable", "flowNode"].indexOf(item.type) == -1'
                  :label="item.label"
                  :prop='item.prop'
                  :rules='item.rules'
                  v-model="from[item.prop]"
                  :label-width="(item.type==='iframe' && !item.label)?'0': (formsetting.labelwidth ? (formsetting.labelwidth+'px') :'80px')"
                >
                  <FormItem :item='item' :form='from' :freshBoolean="freshBoolean" :designId="designId" :jvsAppId="$route.query.jvsAppId" :originOption="{...formsetting, formAlign: formsetting.labelposition}" :isView="true" :delcomRandom="delcomRandom" @file="fileListHandle"/>
                </el-form-item>
                <el-divider v-if="item.type === 'divider'" :content-position='item.contentposition'>{{item.text}}</el-divider>
                <p v-if="item.type === 'p'" class="form-item-p" :style="`text-align:${item.contentposition};font-size:${item.fontsize}px;color:${item.textcolor};margin:0;`">
                  <span :style="item.barHide ? 'padding-left: 0;' : ''">
                    <i v-if="item.barHide !== true"></i>
                    <b>{{item.text}}</b>
                  </span>
                </p>
                <!-- 表格 -->
                <el-form-item :class="{'play-table-form-item': item.type == 'tableForm', 'nolabel-form-item': !item.label}" v-if='item.type==="tableForm"' :label="item.label">
                  <div>
                    <tableForm
                      :com='com'
                      :data='item'
                      :drag1='drag1'
                      :drag2='drag2'
                      :drag3='drag3'
                      :startindex='startindex'
                      :formsetting="formsetting"
                      :isDetail="isDetail"
                      :designId="designId"
                      :jvsAppId="$route.query.jvsAppId"
                      :delcomRandom="delcomRandom"
                      @setdrag3='setdrag3'
                      @chonzhihxindex='chonzhihxindex'
                      @clickitem='clickitem'
                      :action="action"
                      />
                  </div>
                </el-form-item>
                <!-- 流程设计 -->
                <el-form-item :class="item.label? '' : 'nolabel-form-item'" v-if='item.type==="flowTable"' :label="item.label">
                  <div>
                    <flowTable
                      :com='com'
                      :data='item'
                      :formsetting="formsetting"
                      :isDetail="isDetail"
                      :designId="designId"
                      @clickitem='clickitem'
                      :action="action"
                      :jvsAppId="$route.query.jvsAppId"
                      :delcomRandom="delcomRandom"
                      />
                  </div>
                </el-form-item>

                <!-- 执行流程 -->
                <el-form-item :class="item.label? '' : 'nolabel-form-item'" v-if='item.type==="flowNode"' :label="item.label">
                  <div>
                    <flowNode
                      :com='com'
                      :data='item'
                      :formsetting="formsetting"
                      :isDetail="isDetail"
                      :designId="designId"
                      :jvsAppId="$route.query.jvsAppId"
                      :delcomRandom="delcomRandom"
                      @clickitem='clickitem'
                      />
                  </div>
                </el-form-item>

                <!-- 选项卡 -->
                <el-form-item :class="{'playfor-cont-item': true, 'nolabel-form-item': !item.label}" v-if='item.type==="tab"' :label="item.label">
                  <div v-if="item.status == 'collapse'">
                    <collapseForm
                      :com='com'
                      :data='item'
                      :drag1='drag1'
                      :drag2='drag2'
                      :drag3='drag3'
                      :startindex='startindex'
                      :outClick="actid"
                      :outCom="actCom"
                      :formsetting="formsetting"
                      :isDetail="isDetail"
                      :designId="designId"
                      :jvsAppId="$route.query.jvsAppId"
                      :dataModelFields="dataModelFields"
                      :domList="fromData"
                      @setdrag3='setdrag3'
                      @chonzhihxindex='chonzhihxindex'
                      @clickitem='clickitem'
                      :action="action"
                      :designnerType='designnerType'
                      :delcomRandom="delcomRandom">
                    </collapseForm>
                  </div>
                  <div v-else>
                    <tabForm
                      :com='com'
                      :data='item'
                      :drag1='drag1'
                      :drag2='drag2'
                      :drag3='drag3'
                      :startindex='startindex'
                      :outClick="actid"
                      :outCom="actCom"
                      :formsetting="formsetting"
                      :isDetail="isDetail"
                      :designId="designId"
                      :jvsAppId="$route.query.jvsAppId"
                      :dataModelFields="dataModelFields"
                      :domList="fromData"
                      @setdrag3='setdrag3'
                      @chonzhihxindex='chonzhihxindex'
                      @clickitem='clickitem'
                      :action="action"
                      :designnerType='designnerType'
                      :delcomRandom="delcomRandom"
                      />
                  </div>
                </el-form-item>

                <!-- 步骤条 -->
                <el-form-item :class="item.label? '' : 'nolabel-form-item'" v-if='item.type==="step"' :label="item.label">
                  <div>
                    <stepBar
                      :com='com'
                      :data='item'
                      :drag1='drag1'
                      :drag2='drag2'
                      :drag3='drag3'
                      :startindex='startindex'
                      :outClick="actid"
                      :outCom="actCom"
                      :formsetting="formsetting"
                      :isDetail="isDetail"
                      :designId="designId"
                      :jvsAppId="$route.query.jvsAppId"
                      :dataModelFields="dataModelFields"
                      @setdrag3='setdrag3'
                      @chonzhihxindex='chonzhihxindex'
                      @clickitem='clickitem'
                      :action="action"
                      :designnerType='designnerType'
                      :delcomRandom="delcomRandom"
                      />
                  </div>
                </el-form-item>

                <!-- 子表 -->
                <el-form-item :label="item.type == 'formbox' ? item.label : ''" :class="item.label? '' : 'nolabel-form-item'" v-if='item.hasChildren || item.type == "formbox"' class="sectionItem">
                  <div>
                    <sectionForm
                      :com='com'
                      :data='item'
                      :drag1='drag1'
                      :drag2='drag2'
                      :drag3='drag3'
                      :startindex='startindex'
                      :outClick="actid"
                      :formsetting="formsetting"
                      :isDetail="isDetail"
                      :designId="designId"
                      :jvsAppId="$route.query.jvsAppId"
                      :dataModelField="dataModelFields"
                      @setdrag3='setdrag3'
                      @chonzhihxindex='chonzhihxindex'
                      @clickitem='clickitem'
                      :action="action"
                      :designnerType='designnerType'
                      :delcomRandom="delcomRandom"
                      />
                  </div>
                </el-form-item>

                <!-- 静态表格  报表类 -->
                <el-form-item class="form-item-reportTable" v-if='item.type==="reportTable"' :label="item.label">
                  <div>
                    <reportTable
                      :com='com'
                      :data='item'
                      :drag1='drag1'
                      :drag2='drag2'
                      :drag3='drag3'
                      :startindex='startindex'
                      :formsetting="formsetting"
                      :isDetail="isDetail"
                      :designId="designId"
                      :jvsAppId="$route.query.jvsAppId"
                      @setdrag3='setdrag3'
                      @chonzhihxindex='chonzhihxindex'
                      @clickitem='clickitem'
                      :action="action"
                      :delcomRandom="delcomRandom"
                      />
                  </div>
                </el-form-item>

                <!-- 子表单 -->
                <div v-if="['childrenForm'].indexOf(item.type) > -1" class="form-item-childrenForm">
                  <div v-if="item.label" class="form-item-childrenForm-label">{{item.label}}</div>
                  <div class="form-item-childrenForm-content"></div>
                </div>
                <!-- 删除按钮 -->
                <div class="handle-btn">
                  <div class="type-name">{{item.name}}</div>
                  <i class="el-icon-copy-document copy-item-first" @click.stop="copyFormsItem(item, index)"></i>
                  <el-popconfirm title="确定删除吗？" @confirm="deleteFormsItem(item)">
                    <i class="el-icon-delete del-item-first" slot="reference" @click.stop=""></i>
                  </el-popconfirm>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </el-form>
      <div v-if="hxindex== -2 && drag3 !== '2'" class="hxdiv"></div>
    </div>
    <el-dialog
      title="数据结构"
      v-if="dialogVisible"
      :visible.sync="dialogVisible"
      append-to-body
      :fullscreen="false"
      class="drawer-popup-dialog json-show-dialog"
      :close-on-click-modal="false"
      :before-close="handleClose">
      <!-- form-fullscreen-dialog  -->
      <div class="row">
        <div>
          <json-viewer
            v-if="formJson || fromFlow"
            style="overflow:auto;flex:1;min-height:300px;"
            :value="formJsonString"
            :expand-depth="2000"
            copyable
            boxed
            sort
            ></json-viewer>
        </div>
      </div>
      <div class="row" v-if="false">
        <div>
          <h4>自定义JSQL</h4>
          <codeEditor class="jsql-codeEditor" prop="customizeJsonCode" :code="customizeJsqlJsonCode" @change="changeHandle"></codeEditor>
        </div>
        <div>
          <h4>JSQL</h4>
          <codeEditor class="jsql-codeEditor" prop="variableJsonCode" :readOnly="true" :code="jsqlJson"></codeEditor>
        </div>
      </div>
    </el-dialog>
  </el-card>
</template>

<script>

import tableForm from './tableForm'
import tabForm from './tabForm'
import collapseForm from './collapseForm'
import sectionForm from './sectionForm'
import stepBar from './stepBar'
import reportTable from './reportTable'
import flowTable from './flowTable'
import flowNode from './flowNode'
import codeEditor from '../views/design/coder' // json编译器
import { deleteExec } from '@/components/basic-container/formula/api'
export default {
  name: "forms",
  components: {
    FormItem: () => import('@/components/basic-assembly/formitem'),
    tabForm, collapseForm, sectionForm, tableForm, stepBar, reportTable, codeEditor, flowTable, flowNode
  },
  // 是否正在拖动组件
  props: {
    designId: {
      type: String
    },
    // 设计器名称
    title: {
      type: String,
      default: () => {
        return "表单设计"
      }
    },
    // 是否显示按钮
    needBtn: {
      type: Boolean,
      default: () => {
        return true
      }
    },
    // 设计器类型
    designnerType: {
      type: String,
      default: () => {
        return ""
      }
    },
    // 当前选中的组件
    clickformcom: {
      type: Object,
      default: () => {
        return {}
      }
    },
    drag1: {
      type: String,
      default: () => {
        return "1";
      }
    },
    drag2: {
      type: String,
      default: () => {
        return "1";
      }
    },
    fromData: {
      type: Array,
      default: () => {
        return [{ name: "111" }];
      }
    },
    formsetting: {
      type: Object,
      default: () => {
        return {
          labelposition: 'top',
          labelwidth: 80,
          formsize: 'mini',
          fullscreen: false
        }
      }
    },
    // 左侧正在被拖动的组件
    com: {
      type: Object
    },
    height: {
      type: Number,
      default: () => {
        return 600
      }
    },
    groupList: {
      type: Array,
      default: () => {
        return [];
      }
    },
    action: {
      type: String,
      default: () => {
        return ""
      }
    },
    formJson: {
      type: String
    },
    freshBoolean: {
      type: Boolean
    },
    masterTable: {
      type: String
    },
    jsqlJson: {
      type: String
    },
    customizeJsqlJson: {
      type: String
    },
    // 是否为列表页详情按钮
    isDetail: {
      type: Boolean,
      default: () => {
        return false
      }
    },
    dataModelFields: {
      type: Array
    }
  },
  data() {
    return {
      startindex: -1,
      endindex: -1,
      // 表单的值
      from: {},
      // 拖动到内部组件  1 没有  2 有
      drag3: '1',
      // -3 为没有 -2 为大的div的下面 -1 为大的div的上面  其他为 小组件
      hxindex: -3,
      // 被点击选中的id值
      actid: '0',
      fileKey: [],
      // 详情组件
      detailTypeList: ["inputReadOnly", "textareaReadOnly", "image", "file", "tableReadOnly", "link"],
      // 是否交叉拖拽  true时不可操作com
      overlapping: false,
      dialogVisible: false,
      fileList: [],
      headers: {
        tenantId: this.$store.getters.userInfo.tenantId,
        Authorization: this.$store.getters.access_token
      },
      formId: '',
      fromFlow: false,
      customizeJsqlJsonCode: '',
      actCom: null,
      delcomRandom: -1
    }
  },
  created () {
    this.initfrom()
    if(this.masterTable) {
      this.formId = this.masterTable
    }
    this.$root.eventBus.$off("toolEvent")
    this.$root.eventBus.$on("toolEvent", type => {
      switch(type) {
        case 'ruleSave':
          this.save(true);
          break;
        case 'save':
          if(this.designnerType == 'infoForm') {
            console.log(111)
          }else{
            this.save(false);
          }
          break;
        case 'preview': this.yulan();break;
        case 'json': this.viewData();break;
        case 'import': console.log(this.$refs.importBtn);this.$refs.importBtn.clickHandle();break;
        case 'export': this.exportHandle();break;
        default: ;break;
      }
    });
    if(this.customizeJsqlJson) {
      this.customizeJsqlJsonCode = this.customizeJsqlJson
    }
  },
  beforeDestory () {
    this.$root.eventBus.$off("toolEvent")
  },
  methods: {
    // Checkbox 需要提前初始化
    initfrom() {
      this.fileKey = []
      let obj = {}
      this.fromData.map(item => {
        if(item.showFrom.indexOf("sqlType") === -1) {
          item.showFrom.push('sqlType')
        }
        if(!item.sqlType) {
          item.sqlType = 'varchar'
        }
        // obj[item.prop] = null
        if (item.type === 'checkbox' || item.type === 'department') {
          obj[item.prop] = []
        }
        if(item.type === 'select') {
          if(item.multiple) {
            this.$set(obj, item.prop, [])
          }else{
            obj[item.prop] = ""
          }
        }
        if (item.type === 'image' || item.type === 'file') { // 图片 文件 列表 提前初始化
          obj[item.prop] = []
        }
        if(item.type === 'imageUpload' || item.type === 'fileUpload') {
          item.action = this.action
        }
        if (item.type === 'image' || item.type === 'file' || item.type === 'imageUpload' || item.type === 'fileUpload') {
          if(item.type === 'imageUpload' || item.type === 'fileUpload') {
            this.fileKey.push({
              key: item.prop,
              list: []
            })
          }
        }
        if(['childrenForm', 'connectForm'].indexOf(item.type) > -1) {
          obj[item.prop] = {}
        }
      })
      // console.log(obj)
      this.from = obj
    },
    // 拖动到内部组件
    setdrag3(data){
      this.drag3 = data
    },
    // 重置数据
    centerreset () {
      this.startindex = -1
      this.endindex = -1
      this.drag3 = '1'
      this.hxindex = -3
      this.actid = 0
      this.actCom = null
    },
    chonzhihxindex () {
      this.hxindex = -3
    },
    // 大div 中滑动时
    dragover() {
      // 中间没有拖动时
      let bool = this.currentDesignerDealItem(false)
      if(bool) {
        this.sortList()
      }
    },
    // 离开大div时
    dragleave() {
      // 当进入到小div中时  或者在操作小div 时 不处理
      if (this.drag1 == "4" || this.drag2 != "1") {
        return;
      }
      this.$emit("setdrag", {type:'drag1',value: '2'});
      this.hxindex = -3;
    },
    // 在div 中放下时
    dropfun() {
      // 不是中间的拖动时 者添加到中间的表单中
      if (this.drag2 === "1") {
        console.log('外层添加。。。。')
        let bool = this.currentDesignerDealItem(true)
        console.log(bool)
        console.log(this.clickformcom)
        if(bool) {
          //-----------------------------------------------------------------
          this.$emit("addcom")
        }else{
          this.$emit('reset')
        }
        this.hxindex = -3;
      }
      this.drag3 = '1'
    },
    // 在组件中的
    dragstart(index, data) {
      this.$emit("setdrag", { type:'drag2',value: '2' });
      this.$emit('setdelcom', data)
      this.$emit('setformcom', data)
      this.$emit('designtype', this.designnerType)
      this.startindex = index;
      console.log("开始拖动 小div", index);
      console.log(index, data)
    },
    dragend() {
      // 重置数据
      console.log('中间的end ------------')
      this.startindex = -1
      this.endindex = -1
      this.drag3 = '1'
      this.$emit("reset")
    },
    // 在组件中滑动
    dragover2(index) {
      // 处理 从左侧拖动到中间组件的逻辑
      if (this.drag2 === "1" && this.drag1 !== "1") {
        this.$emit("setdrag", { type:'drag1',value: '4' });
        this.hxindex = index;
        // todo 增加横线
      }
      // 中间拖动进入到 中间的其他组件
      if (this.drag1 === "1" && this.drag2 !== "1") {
        if (index !== this.startindex) {
          this.endindex = index;
          this.$emit("setdrag", { type:'drag2',value: '3' });
        } else {
          this.endindex = -1;
        }
      }
    },
    dragleave2(index) {
      this.endindex = -1;
    },
    dropfun2(index) {
      let bool = this.currentDesignerDealItem(true)
      if(bool) {
        // 中间的进行拖动交换
        if (this.drag1 === "1") {
          this.$emit("setdrag", { type:'drag2',value: '1' });
          if (
            this.startindex !== this.endindex &&
            this.startindex != -1 &&
            this.endindex != -1
          ) {
            this.$emit("jiaohuan", this.startindex, this.endindex);
          }
        }
        // 从左侧进入到小div中放下  并且没有进入到内部组件中时 进行对应位置的添加
        if (this.drag1 === "4" && this.drag3 != '2') {
          console.log('中间层添加。。。。')
          console.log(this.drag1, this.drag3)
          console.log(index)
          this.$emit("addcom", index)
          this.hxindex = -3;
        }
      }else{
        this.startindex = -1
        this.endindex = -1
      }
      this.drag3 = '1'
    },
    // 选中中间的组件
    clickitem (data) {
      // console.log('data..')
      this.actid = data.prop
      this.actCom = JSON.parse(JSON.stringify(data))
      this.$emit('setformcom', data)
      // console.log(this.actid)
    },
    sub() {
      alert(JSON.stringify(this.from));
    },
    // 预览
    yulan(){
      console.log(this.from)
      this.$emit('setdialogVisible', true)
    },
    // 保存
    save (isRuleSave) {
      this.$emit('save', this.formJsonString, isRuleSave)
    },
    // 查看数据结构
    viewData () {
      this.formJsonString = this.getJson(this.fromData, {})
      // 流程设计器---查看数据结构时需调整外层弹框的关闭按钮 z-index
      let dom = $("#flowableDesignDialog .el-dialog__header .el-dialog__headerbtn", parent.document)
      if(dom) {
        dom.css({'z-index': 0})
        this.fromFlow = true
      }else{
        this.fromFlow = false
      }
      this.dialogVisible = true
    },
    // 关闭预览数据结构
    handleClose () {
      this.dialogVisible = false
      let dom = $("#flowableDesignDialog .el-dialog__header .el-dialog__headerbtn", parent.document)
      if(dom) {
        dom.css({'z-index': 9})
        this.fromFlow = true
      }else{
        this.fromFlow = false
      }
    },
    // 上传文件--监听
    fileListHandle(obj){
      if(this.fileKey.length > 0){
        for(let i in this.fileKey){
          if(this.fileKey[i].key === obj.prop){
            this.fileKey[i].list = obj.fileList
          }
        }
      }
    },
    fromgroupSectionKeyListHandle(obj) {
      this.$emit('fromgroupSectionKeyList', obj)
    },
    sortList () {
      if (this.drag2 === "1") {
        this.$emit("setdrag", {type:'drag1',value: '3'});
        // 一个都没有时 在上面 否则在下面
        if (this.fromData.length === 0) {
          this.hxindex = -1;
        } else {
          this.hxindex = -2;
        }
      }
    },
    // 判断当前设计和组件是否能组合
    currentDesignerDealItem (needMessage) {
      if(this.designnerType == 'infoForm') {
        if((this.clickformcom.type && this.detailTypeList.indexOf(this.clickformcom.type) == -1) || !this.clickformcom.type) {
          if(needMessage) {
            // this.$message.warning('基本信息不可放入表单组件！')
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
        if((this.clickformcom.type && this.detailTypeList.indexOf(this.clickformcom.type) == -1) || !this.clickformcom.type) {
          this.overlapping = false
          return true
        }else{
          if(needMessage) {
            // this.$message.warning('表单设计不可放入详情组件！')
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
    // 遍历字段列表生成数据结构
    getJson (list, formJson) {
      for(let i=0; i < list.length; i++) {
        switch(list[i].type) {
          case 'inputReadOnly':
            formJson[list[i].prop] = "";
            break;
          case 'textareaReadOnly':
            formJson[list[i].prop] = "";
            break;
          case 'input':
            formJson[list[i].prop] = "";
            break;
          case 'textarea':
            formJson[list[i].prop] = "";
            break;
          case 'inputNumber':
            // 整数 或 小数
            if(list[i].isFloat == true) {
              formJson[list[i].prop] = 1.0001;
            }else{
              formJson[list[i].prop] = 1;
            }
            break;
          case 'SWITCH':
            formJson[list[i].prop] = false;
            break;
          case 'switch':
            formJson[list[i].prop] = false;
            break;
          case 'timeSelect':
            formJson[list[i].prop] = "10:00:00";
            break;
          case 'timePicker':
            if(list[i].isrange) {
              formJson[list[i].prop] = ["10:00:00", "12:00:00"];
            }else{
              formJson[list[i].prop] = "10:00:00";
            }
            break;
          case 'datePicker':
            if(["datetimerange", "daterange", "monthrange"].indexOf(list[i].datetype) > -1) {
              formJson[list[i].prop] = ["2021-02-05 10:00:00", "2021-02-05 12:00:00"];
              if(list[i].datetype == 'daterange') {
                formJson[list[i].prop] = ["2021-02-05", "2021-02-06"];
              }
              if(list[i].datetype == 'monthrange') {
                formJson[list[i].prop] = ["2021-02", "2021-06"];
              }
            }else{
              formJson[list[i].prop] = "2021-02-05 10:00:00";
              if(list[i].datetype == "date" || list[i].datetype == "week") {
                formJson[list[i].prop] = "2021-02-05";
              }
              if(list[i].datetype == "month") {
                formJson[list[i].prop] = "2021-02";
              }
              if(list[i].datetype == "year") {
                formJson[list[i].prop] = "2021";
              }
              if(list[i].datetype == "dates") {
                formJson[list[i].prop] = ["2021-03-17", "2021-03-18", "2021-03-19"]
              }
            }
            break;
          case 'select':
          case 'role':
          case 'user':
          case 'job':
          case 'datasource':
            if(list[i].multiple) {
              formJson[list[i].prop] = []
            }else{
              formJson[list[i].prop] = ""
            }
            break;
          case 'inputReadOnly':
            formJson[list[i].prop] = "";
            break;
          case 'department':
          case 'cascader':
          case 'chinaArea':
            if(list[i].multiple) {
              if(list[i].emitPath) {
                formJson[list[i].prop] = [ [1,2,3], [1,2,4] ]
              }else{
                formJson[list[i].prop] = [3, 4]
              }
            }else{
              if(list[i].emitPath) {
                formJson[list[i].prop] = [1,2,3]
              }else{
                formJson[list[i].prop] = 3
              }
            }
            break;
          case 'checkbox':
          case 'timeline':
            formJson[list[i].prop] = [];
            break;
          case 'slider':
            if(list[i].range) {
              formJson[list[i].prop] = [0, 100]
            }else{
              formJson[list[i].prop] = 10
            }
            break;
          case 'tab':
          case 'step':
            if(list[i].detachData) {
              for(let j in list[i].dicData) {
                let name = list[i].dicData[j].name
                if(list[i].dicData[j].prop) {
                  formJson[list[i].dicData[j].prop] = {}
                }
                if(list[i].column[name]) {
                  if(list[i].dicData[j].prop) {
                    formJson[list[i].dicData[j].prop] = this.getJson(list[i].column[name], {})
                  }else{
                    this.getJson(list[i].column[name], formJson)
                  }
                }
              }
            }else{
              let tobj = {};
              for(let j in list[i].dicData) {
                let name = list[i].dicData[j].name
                tobj[name] = {}
                if(list[i].column[name]) {
                  tobj[name] = this.getJson(list[i].column[name], {})
                }
              }
              formJson[list[i].prop] = tobj;
            }
            break;
          case 'tableForm':
            let tob = {};
            tob = this.getJson(list[i].tableColumn, {})
            formJson[list[i].prop] = [tob]
            break;
          case 'formbox':
            formJson[list[i].prop] = [];
            if(list[i].children) {
              let to = this.getJson(list[i].children, {})
              formJson[list[i].prop].push(to)
            }
            break;
          case 'image':
          case 'imageUpload':
          case 'file':
          case 'fileUpload':
            formJson[list[i].prop] = [{name: '', url: '', fileName: ''}];
            break;
          case 'reportTable':
            let rob = {};
            for(let ri in list[i].dicData) {
              let tb = {}
              tb = this.getJson(list[i].tableColumn, {})
              rob[list[i].dicData[ri].value] = tb
            }
            formJson[list[i].prop] = rob;
            break;
          case 'signature':
            formJson[list[i].prop] = [{name: '', url: '', fileName: '', bucketName: ''}];
            break;
          default :
            formJson[list[i].prop] = "";
            break;
        }
        if(list[i].type != 'formbox' && list[i].hasChildren) {
          let to = this.getJson(list[i].children, {})
          formJson = Object.assign(formJson, to)
        }
      }
      return formJson
    },
    uploadSuccess (res, file, fileList) {
      if(res.code == 0) {
        // this.$message.success('导入成功')
        this.$notify({
          title: '提示',
          message: '导入成功',
          position: 'bottom-right',
          type: 'success'
        });
        this.fileList = []
        this.$emit("freshFormData", true)
      }else{
        this.$refs.uploadBtn.clearFiles()
        // this.$message.error(res.msg)
        this.$notify({
          title: '提示',
          message: res.msg,
          position: 'bottom-right',
          type: 'error'
        });
      }
    },
    // 导入失败
    errHandle (err, file, fileList) {
      this.$refs.uploadBtn.clearFiles()
      // this.$message.error(err)
      this.$notify({
        title: '提示',
        message: err,
        position: 'bottom-right',
        type: 'error'
      });
    },
    exportHandle () {
      this.$openUrl(`/mgr/jvs-design/app/${this.$route.query.jvsAppId}/form/design/export/${this.formId}`, '_blank')
    },
    // 删除表单项
    deleteFormsItem(item){
      if(item.formula) {
        deleteExec(this.designId, item.formula).then(res => {
          if(res.data && res.data.code == 0) {
            this.$emit('setdelcom', item)
            // this.$emit('setformcom', item)
            this.$emit('deletecom')
            this.delcomRandom = Math.random()
          }
        })
      }else{
        this.$emit('setdelcom', item)
        // this.$emit('setformcom', item)
        this.$emit('deletecom')
        this.delcomRandom = Math.random()
      }
    },
    // 复制表单项
    copyFormsItem(item, index) {
      this.$emit('copy', item, index)
    },
    // editor赋值
    changeHandle (code) {
      if(code && code != 'error') {
        this.$emit('setJSQL', code)
      }else{
        this.$emit('setJSQL', "")
      }
    },
  },
  computed: {
    // 中间的大的div
    styleCard: function() {
      // drag1 是否正在拖动左侧的组件 1 没有动 2 已经拖动没有进入中间的界面  3拖动进入中间的页面 4 进入设计页面中的组件
      // drag2 是否正在拖动中间的组件 1 没有动  2 已经拖动没有进度其他组件  3 进入其他组件
      let sty = { background: "rgb(0,0,0,0)" };
      if (this.drag1 == "2") {
        // sty.background = 'rgb(0,0,0,0.1)'
      } else if (this.drag1 == "3") {
        // sty.background = 'rgb(0,0,0,0.5)'
        // sty["z-index"] = 10;
      }
      return sty;
    },
    formJsonString: {
      get(){
        let str = {}
        str =  this.getJson(this.fromData, {})
        return str
      },
      set () {}
    }
  },
  watch: {
    // fromData 变化时初始化表单
    fromData () {
      this.initfrom()
    }
  }
};
</script>

<style lang="scss" scoped>
.elcard {
  position: relative;
  height: calc(100% - 72px);
  margin: 0 16px;
  padding: 16px 24px;
  border-radius: 6px;
  box-sizing: border-box;
  /deep/.el-card__header{
    display: none;
  }
  /deep/.el-card__body{
    padding: 0;
    height: 100%;
  }
}

.elcardzz {
  position: absolute;
  top: 58px;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
}

.formscont {
  height: 100%;
  overflow: hidden;
  overflow-y: auto;
  scrollbar-width: none; /* firefox */
  -ms-overflow-style: none; /* IE 10+ */
}
.level-form-design .formscont{
  height: calc(100vh - 272px);
}
.formscont::-webkit-scrollbar {
  display: none; /* Chrome Safari */
  width: 0;
}
.formitem {
  position: relative;
  padding: 16px 16px 1px;
  border-radius: 6px;
  box-sizing: border-box;
  cursor: move;
  /deep/.el-form-item{
    .el-form-item__content{
      .jvs-form-item{
        .el-input__inner, .el-textarea__inner{
          background: #fff;
        }
        .show-disable.el-input-group{
          .el-input__inner{
            border-right: 0;
          }
        }
        .input-number-textcon{
          display: none;
        }
      }
    }
  }
  /deep/.play-table-form-item{
    >.el-form-item__content{
      background: #fff;
      padding: 16px 16px 0 16px;
    }
    &.nolabel-form-item{
      >.el-form-item__content{
        margin-top: 18px;
      }
    }
  } 
  /deep/.playfor-cont-item{
    >.el-form-item__content{
      background: #fff;
      padding: 0 16px;
      padding-bottom: 16px;
    }
    &.nolabel-form-item{
      >.el-form-item__content{
        margin-top: 18px;
      }
    }
  }
  .form-item-p{
    span{
      display: inline-block;
      box-sizing: border-box;
      overflow: hidden;
      position: relative;
      padding-left: 10px;
      i,b{
        font-weight: normal;
        font-style: normal;
      }
      i{
        width: 4px;
        border-radius: 2px;
        height: 20px;
        background-color: rgb(52, 113, 255);
        display: inline-block;
        line-height: 28px;
        position: absolute;
        top: 2px;
        left: 0;
      }
    }
  }
  /deep/.input-number-hide.is-disabled{
    flex: 1;
    width: auto;
  }
  &.clicked{
    background: #DDEAFF;
  }
  &:not(.clicked):hover{
    background: #F5F6F7;
    /deep/.el-form-item{
    .el-form-item__content{
        .jvs-form-item{
          .el-input-group__append{
            background: #fff;
          }
        }
      }
    }
    .handle-btn{
      display: flex;
      .type-name{
        display: none;
      }
      .copy-item-first{
        border-left: 0;
      }
    }
  }
  &.target{
    border-top: 3px solid #DDEAFF;
    border-radius: 0 0 6px 6px;
  }
}
.jvs-form-for-design:not(.el-form--label-top){
  .formitem{
    padding-top: 34px;
  }
}
.formitem2 {
  border-radius: 3px;
  box-sizing: border-box;
  z-index: 100;
  .handle-btn{
    height: 24px;
    position: absolute;
    right: 1px;
    top: 1px;
    background-color: #fff;
    border-radius: 0px 6px 0px 6px;
    display: none;
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
      margin-top: 5px;
      padding: 0 8px;
      font-size: 14px;
      cursor: pointer;
      border-left: 1px solid #D7D8DB;
    }
  }
}
.active-formitem2{
  .handle-btn{
    display: flex;
  }
}
.hxdiv {
  width: 100%;
  height: 0px;
  border-top: 1px dashed #3a88ed;
}

.icons1 {
  position: absolute;
  top: 0;
  right: 0;
}
.json-show-dialog{
  /deep/.el-dialog{
    border-radius: 4px!important;
    .el-dialog__header{
      height: 70px;
      border-bottom: 1px solid #EEEFF0;
      box-sizing: border-box;
      &::before{
        display: none;
      }
      .el-dialog__title{
        font-family: Microsoft YaHei-Bold, Microsoft YaHei;
        font-weight: 700;
        font-size: 20px;
        color: #363B4C;
      }
      .el-dialog__headerbtn{
        font-size: 20px;
        .el-dialog__close{
          color: #6F7588;
        }
      }
    }
    .el-dialog__body{
      width: 100%;
      height: calc(100% - 70px);
      padding: 20px!important;
      position: unset;
      box-sizing: border-box;
      .row{
        height: 100%;
        overflow: hidden;
        overflow-y: auto;
      }
    }
  }
}
</style>
<style lang="scss">
.sectionItem>.el-form-item__content{
  margin-left: 0!important;
}
.nolabel-form-item, .form-item-reportTable{
  .el-form-item__content{
    margin-left: 0!important;
  }
}
.form-item-reportTable{
  width: 100%;
}
.form-item-childrenForm{
  width: 100%;
  .form-item-childrenForm-label{
    line-height: 28px;
    text-align: left;
    padding: 0 0 10px;
    font-size: 14px;
    color: #606266;
  }
  .form-item-childrenForm-content{
    height: 100px;
    box-shadow: 0 0 10px #eff2f7;
  }
}
.jsql-codeEditor{
  width: 100%;
  height: 500px;
  position: relative;
}
</style>
