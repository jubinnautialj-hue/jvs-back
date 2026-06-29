<template>
  <div style="cursor:pointer;">
    <div class="contdiv"
      :style="contdivstyle"
      @dragenter="dragenter"
      @dragleave="dragleave"
      @dragover="dragover"
      @drop="drop"
      @dragover.prevent
    ></div>
    <el-form
      :model="dynamicValidateForm"
      ref="dynamicValidateForm"
      class="demo-dynamic"
      style="width:100%;"
      :label-position="formsetting.labelposition"
      :label-width="formsetting.labelwidth+'px'"
      :size="formsetting.formsize || 'mini'"
      >
      <el-table
        ref="myTable"
        empty-text='拖动左侧的组件到表格中'
        :key="tablekey"
        :border='data.border'
        :data="dynamicValidateForm.domains"
        class="tb-edit design-table-form"
        style="width: 100%"
        highlight-current-row
        :id="tablekey"
        >
          <el-table-column :align="'center'" type="index" width="50" label="序号">
            <template slot="header">
              <div class="headeritem" style="cursor: default;">序号</div>
            </template>
          </el-table-column>
          <el-table-column :align="item.align || data.align || 'center'" v-for="(item,index) in data.tableColumn"  :label="item.label + (item.rules.length > 0 ? '(必填)':'(可选)') " :key="'column'+index" :width="item.width">
            <template slot="header">
              <div
                :class="{'headeritem': true}"
                @dragstart.stop="dragstart(index,item)"
                @dragend.stop="dragend(index, data)"
                @dragover="dragoverColumn(index, item)"
                @dragleave="dragleaveColumn(index, item)"
                @drop="dropfun(index)"
                draggable
                @dragover.prevent
              >
                <span @click.stop="set(item)" style="cursor: pointer;">
                  <span style="color:#F56C6C" v-if='item.rules.length > 0 && item.rules[0].required'>*</span>
                  <span>{{item.label}}</span>
                </span>
                <div class="handle-table-btn">
                  <span class="type-name">{{item.name}}</span>
                  <i class="el-icon-setting set-item-first" @click.stop="set(item)"></i>
                  <i class="el-icon-copy-document copy-item-first" @click.stop="copyFormsItem(item, index)"></i>
                  <el-popconfirm title="确定删除吗？" @confirm="del(item)">
                    <i class="el-icon-delete del-item-first" slot="reference" @click.stop=""></i>
                  </el-popconfirm>
                </div>
              </div>
            </template>
            <template scope="scope">
              <div v-if="data.editable">
                <el-form-item
                  :key="scope.$index"
                  :rules="item.rules"
                  :prop="'domains.' + scope.$index + '.' + item.prop"
                  class="tableform-no-label-item"
                >
                  <FormItem :item='item' :form="scope.row" :isView="true" :designId="designId" />
                </el-form-item>
              </div>
              <div v-else>
                <span>{{scope.row[item.prop+'_1'] ? scope.row[item.prop+'_1'] : scope.row[item.prop]}}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column :align="data.align || 'center'" label="操作" :fixed="data.menuFix" v-if="tableFormOption.menu !== false" :width="tableFormOption.menuWidth">
            <template slot="header">
              <div class="headeritem" style="cursor: default;">{{data.menuText === '' ? '' : $langt('table.oprate')}}</div>
            </template>
            <template scope="scope">
              <div style="width:100%;justify-content: center;">
                <jvs-button v-if="data.addBtnOrigin != 'table' && !data.editable && data.editBtn" size="mini" type="text" @click="openTableBtnFormColumn('edit', scope.row, scope.$index)">{{data.editBtnText || '编辑'}}</jvs-button>
                <jvs-button v-if="data.addBtnOrigin != 'table' && !data.editable && data.viewBtn" size="mini" type="text" @click="openTableBtnFormColumn('view', scope.row, scope.$index)">{{data.viewBtnText || '详情'}}</jvs-button>
                <jvs-button v-if="data.delBtn" size="mini" type="text" @click="removeDomain(scope.row)">{{data.delBtnText || '删除'}}</jvs-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      <el-form-item class="tableform-no-label-item" style="margin-top:10px;" v-if="data.addBtn">
        <el-button @click="addHandle">{{data.addBtnText || '新增'}}</el-button>
      </el-form-item>
    </el-form>

    <!-- 打开表单 -->
    <el-dialog
      v-if="(data.addBtn && data.addBtnOrigin == 'form') || (!data.editable && (data.editBtn || data.viewBtn))"
      :title="openFormTitle"
      :visible.sync="openFormVisible"
      append-to-body
      :before-close="openFormClose">
      <div v-if="openFormVisible">
        <jvs-form :option="openFormOption" :formData="openForm" @submit="openFormSubmit" @cancalClick="openFormClose"></jvs-form>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { deleteExec } from '@/components/basic-container/formula/api'
import {guid} from "@/util/util";
import { copyFormulaComponent } from '@/views/page/api/newDesign'
export default {
  components: {
    FormItem: () => import('@/components/basic-assembly/formitem')
  },
  props: {
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
          tableColumn: []
        }
      }
    },
    // 左侧被拉动的组件 如果在table 中放开 则添加到table 中
    com: {
      type: Object
    },
    action: {
      type: String,
      default: () => {
        return ""
      }
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
  },
  computed: {
    column () {
      return this.data.tableColumn
    },
    contdivstyle(){
      let obj = {}
      // obj.background = this.drag3 === '1' ? 'rgba(0,0,0,0.1)' : 'rgba(0,0,0,0.2)'
      // 左侧或者 中间拖动的时候 要 大等于 10
      obj['z-index'] =  this.drag1 !== '1' ? 100 : -1

      return obj
    },
    tableFormOption () {
      let temp = {}
      if(this.data.editable) {
        if(this.data.delBtn) {
          temp.menu = true
        }else{
          temp.menu = false
        }
      }else{
        if(this.data.addBtnOrigin == 'table') {
          if(this.data.delBtn) {
            temp.menu = true
          }else{
            temp.menu = false
          }
        }else{
          if(this.data.editBtn || this.data.viewBtn || this.data.delBtn) {
            temp.menu = true
          }else{
            temp.menu = false
          }
        }
      }
      if(this.data.disabled) {
        temp.menu = false
      }
      if(this.data.page && this.data.editable) {
        temp.page = true
      }else{
        temp.page = false
      }
      if(this.data.menuFix) {
        temp.menuFix = this.data.menuFix
      }
      if(temp.menuAlign != 'left') {
        temp.menuAlign = 'center'
      }
      temp.indexLabel = '序号'
      temp.border = false
      temp.hideTop = true
      if(!this.data.editable) {
        if(this.data.addBtnOrigin == 'table') {
          if(this.data.delBtn) {
            temp.menuWidth = 80
          }
        }else{
          if(this.data.editBtn && this.data.viewBtn && this.data.delBtn) {
            temp.menuWidth = 120
          }else{
            temp.menuWidth = 80
          }
        }
      }
      if(this.data && this.data.iconBtn) {
        temp.menuWidth = 20
        temp.menuText = ''
        temp.indexWidth = 44
      }
      return temp
    },
  },
  data() {
    return {
      tablekey: 'tablekey' + new Date().getTime(),
      dynamicValidateForm: {
        domains: []
      },
      columnDragStartIndex: -1,
      columnDragDom: null,
      columnDragTargetIndex: -1,
      columnDragTargetDom: null,
      scrollX: 0,
      openFormTitle: '',
      openFormWidth: '',
      openFormVisible: false,
      openForm: null,
      openFormIndex: -1,
      openFormOption: {
        emptyBtn: false,
        column: []
      },
    }
  },
  created () {
    this.dealWidth()
  },
  methods: {
    dragenter () {
      console.log('进入目标>>>')
      // this.$emit('setmub', true)
    },
    dragover () {
      // 当在表格上滑动的时候
      this.$emit('setdrag3', '2')
      // console.log('在表格上>>>')
    },
    dragleave () {
      this.$emit('setdrag3', '1')
      console.log('移出目标>>>')
      // this.$emit('setmub', false)
    },
    drop () {
      // todo 进行添加操作 并且 hxindex 置为 -3
      if (['tableForm','p','divider','tab','section', 'step', 'formbox', 'htmlEditor', 'reportTable', 'pageTable'].indexOf(this.com.type) == -1 ) {
        if(this.com.type === 'imageUpload' || this.com.type === 'fileUpload'){
          this.com.action = this.action
        }
        if(this.data.addcolumn){
          this.data.addcolumn({...this.com, span: 24})
        }else{
          if(this.data.formId) {
            this.com.formId = this.data.formId
          }
          if(this.data.dataModelId) {
            this.com.dataModelId = this.data.dataModelId
          }
          this.com.parentType = this.data.type
          this.com.parentKey =  (this.data.parentKey ? (this.data.parentKey + '.') : '') + this.data.prop
          this.data.tableColumn.push({...this.com, span: 24})
        }
        this.$emit('setdrag3', '2')
        this.$nextTick(() => {
          document.querySelector(`#${this.tablekey} .el-table__body-wrapper`).scrollTo({left: document.querySelector(`#${this.tablekey} .el-table__body-wrapper`).scrollWidth})
          this.scrollX = document.querySelector(`#${this.tablekey} .el-table__body-wrapper`).scrollLeft
        })
      } else if (this.startindex === '-1') {
        // 中间的拖动到表格中 不处理
      } else {
        if(this.com.type === 'divider') {
          this.$notify({
            title: '提示',
            message: '不能在表格中添加分割线',
            position: 'bottom-right',
            type: 'error'
          });
        }else if(this.com.type === 'p') {
          this.$notify({
            title: '提示',
            message: '不能在表格中添加文字',
            position: 'bottom-right',
            type: 'error'
          });
        }else if(this.com.type === 'tab') {
          this.$notify({
            title: '提示',
            message: '不能在表格中添加选项卡',
            position: 'bottom-right',
            type: 'error'
          });
        }else if(this.com.type === 'section') {
          this.$notify({
            title: '提示',
            message: '不能在表格中添加作用域',
            position: 'bottom-right',
            type: 'error'
          });
        }else if(this.com.type === 'step'){
          this.$notify({
            title: '提示',
            message: '不能在表格中添加步骤条',
            position: 'bottom-right',
            type: 'error'
          });
        }else if(this.com.type === 'formbox'){
          this.$notify({
            title: '提示',
            message: '不能在表格中添加表单卡片',
            position: 'bottom-right',
            type: 'error'
          });
        }else if(this.com.type == 'htmlEditor'){
          this.$notify({
            title: '提示',
            message: '不能在表格中添加富文本',
            position: 'bottom-right',
            type: 'error'
          });
        }else if(this.com.type == 'reportTable'){
          this.$notify({
            title: '提示',
            message: '不能在表格中添加静态表格',
            position: 'bottom-right',
            type: 'error'
          });
        }else if(this.com.type == 'flowTable'){
          this.$notify({
            title: '提示',
            message: '不能在表格中添加流程设计',
            position: 'bottom-right',
            type: 'error'
          });
        }else if(this.com.type == 'flowNode'){
          this.$notify({
            title: '提示',
            message: '不能在表格中添加动态流程',
            position: 'bottom-right',
            type: 'error'
          });
        }else if(this.com.type == 'pageTable'){
          this.$notify({
            title: '提示',
            message: '不能在表格中添加列表页',
            position: 'bottom-right',
            type: 'error'
          });
        }else{
          this.$notify({
            title: '提示',
            message: '不能在表格中添加表格',
            position: 'bottom-right',
            type: 'error'
          });
        }
      }
      this.$emit('chonzhihxindex')
      console.log(this.data)
      console.log('方>>>')
    },
    submitForm(formName) {
      console.log(this.dynamicValidateForm)
      this.$refs[formName].validate(valid => {
        if (valid) {
          console.log(this.dynamicValidateForm)
          alert("submit!");
        } else {
          console.log("error submit!!");
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
      this.scrollX = document.querySelector(`#${this.tablekey} .el-table__body-wrapper`).scrollLeft
      this.$emit('clickitem', data)
      this.$nextTick(() => {
        document.querySelector(`#${this.tablekey} .el-table__body-wrapper`).scrollTo({left: this.scrollX})
      })
    },
    // 复制表单项
    copyFormsItem(item, index) {
      let copy = JSON.parse(JSON.stringify(item))
      item.id = guid()
      item.prop = item.type + guid()
      let needPlaceFormula = false
      if(item.defaultOrigin && item.defaultOrigin == 'formmula' && item.formula) {
        needPlaceFormula = true
      }
      if(needPlaceFormula) {
        copyFormulaComponent(this.jvsAppId, this.designId, copy).then(res => {
          if(res.data && res.data.code == 0) {
            copy = res.data.data
            if(index > -1) {
              this.data.tableColumn.splice(index, 0, copy)
            }else{
              this.data.tableColumn.push(copy)
            }
            this.$nextTick(() => {
              this.set(item)
            })
          }
        })
      }else{
        if(index > -1) {
          this.data.tableColumn.splice(index, 0, copy)
        }else{
          this.data.tableColumn.push(copy)
        }
        this.$nextTick(() => {
          this.set(item)
        })
      }
    },
    del (data) {
      if(data.formula) {
        deleteExec(this.designId, data.formula).then(res => {
          if(res.data && res.data.code == 0) {
            (this.data.deletecolumn && this.data.deletecolumn(data.prop)) || ( this.data.tableColumn = this.data.tableColumn.filter(item => item.prop != data.prop) )
          }
        })
      }else{
        (this.data.deletecolumn && this.data.deletecolumn(data.prop)) || ( this.data.tableColumn = this.data.tableColumn.filter(item => item.prop != data.prop) )
      }
      this.scrollX = document.querySelector(`#${this.tablekey} .el-table__body-wrapper`).scrollLeft
      this.$nextTick(() => {
        document.querySelector(`#${this.tablekey} .el-table__body-wrapper`).scrollTo({left: this.scrollX})
      })
    },
    // 在组件中的
    dragstart(index, data) {
      this.columnDragStartIndex = index;
      this.columnDragDom = data
    },
    dragoverColumn(index, data) {
      this.columnDragTargetIndex = index
      this.columnDragTargetDom = data
    },
    // 离开大div时
    dragleaveColumn(index, data) {
      this.columnDragTargetIndex = -1
      this.columnDragTargetDom = null
    },
    // 在div 中放下时
    dropfun() {
      if(this.columnDragTargetIndex > -1 && this.columnDragStartIndex > -1) {
        // 不移动
        if(this.columnDragTargetIndex == this.columnDragStartIndex) {
          return false
        }
        let temp = JSON.parse(JSON.stringify(this.data.tableColumn))
        temp.splice(this.columnDragStartIndex, 1)
        let tit = JSON.parse(JSON.stringify(this.columnDragDom))
        if(!tit.min && tit.min !== 0) {
          tit.min = -Infinity
        }
        if(!tit.max && tit.max !== 0) {
          tit.max = Infinity
        }
        temp.splice(this.columnDragTargetIndex, 0, tit)
        this.data.tableColumn = temp
        this.$forceUpdate()
        this.scrollX = document.querySelector(`#${this.tablekey} .el-table__body-wrapper`).scrollLeft
        this.$nextTick(() => {
          document.querySelector(`#${this.tablekey} .el-table__body-wrapper`).scrollTo({left: this.scrollX})
        })
      }
    },
    dragend(index, data) {
      // 重置数据
      this.columnDragStartIndex = -1
      this.columnDragDom = null
      this.columnDragTargetIndex = -1
      this.columnDragTargetDom = null
    },
    addHandle () {
      if(this.data.addBtnOrigin == 'form') {
        if(this.data.addBtnFormCode) {
          // 
        }else{
          this.openTableBtnFormColumn('add')
        }
      }else if(this.data.addBtnOrigin == 'table') {
        // 
      }else{
        if(this.data.editable) {
          this.dynamicValidateForm.domains.push({})
        }else{
          let adrow = {}
          this.data.tableColumn.filter(item => {
            if(!this.data.editable) {
              if(item.defaultValue !== null && item.defaultValue !== undefined) {
                this.$set(adrow, item.prop, item.defaultValue)
              }
            }
          })
          this.dynamicValidateForm.domains.push(adrow)
        }
      }
      this.$forceUpdate()
    },
    openTableBtnFormColumn (type, row, index) {
      let title = ''
      if(type == 'add') {
        title = this.data.addBtnText ? this.data.addBtnText : '新增'
      }
      if(type == 'edit') {
        title = this.data.editBtnText ? this.data.editBtnText : '编辑'
      }
      if(type == 'view') {
        title = this.data.viewBtnText ? this.data.viewBtnText : '详情'
      }
      this.openFormTitle = title
      this.openFormWidth = this.data[type+'PopupWidth'] ? (this.data[type+'PopupWidth'] + '%') : '50%'
      this.openFormOption.column = JSON.parse(JSON.stringify(this.data.tableColumn))
      if(type == 'view') {
        this.openFormOption.disabled = true
        this.openFormOption.btnHide = true
      }else{
        this.openFormOption.disabled = false
        this.openFormOption.btnHide = false
      }
      if(row) {
        this.openForm = JSON.parse(JSON.stringify(row))
      }else{
        this.openForm = {}
      }
      if(index > -1) {
        this.openFormIndex = index
      }
      this.openFormVisible = true
    },
    openFormSubmit () {
      if(this.openFormIndex > -1) {
        this.$set(this.dynamicValidateForm.domains, this.openFormIndex, this.openForm)
        this.openFormClose()
        this.$forceUpdate()
      }else{
        this.dynamicValidateForm.domains.push(JSON.parse(JSON.stringify(this.openForm)))
        this.openFormClose()
        this.$forceUpdate()
      }
    },
    openFormClose () {
      this.openFormVisible = false
      this.openFormTitle = ''
      this.openFormWidth = ''
      this.openForm = null
    },
    dealWidth () {
      for(let i in this.data.tableColumn) {
        if(this.isDetail) {
          this.data.tableColumn[i].disabled = true
        }
        this.data.tableColumn[i].width = this.data.tableColumn[i].span * ((this.data.columWidth && this.data.columWidth > 8) ? this.data.columWidth : 8)
      }
    }
  },
  watch: {
    data: {
      handler() {
        this.tablekey = 'tablekey' + new Date().getTime()
        this.dealWidth()
        this.$nextTick(() => {
          document.querySelector(`#${this.tablekey} .el-table__body-wrapper`).scrollTo({left: this.scrollX})
        })
      },
      deep: true
    }
  }
};
</script>

<style lang="scss" scoped>
.headeritem {
  position: relative;
  box-sizing: border-box;
  &.active{
    background: #DDEAFF;
    >.handle-table-btn{
      display: flex;
    }
  }
  &:not(.active):hover{
    background: #F5F6F7;
    >.handle-table-btn{
      display: flex;
      .set-item-first{
        border-left: 0;
      }
    }
  }
  .handle-table-btn{
    height: 24px;
    position: absolute;
    right: 0px;
    top: 0px;
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
    .set-item-first, .copy-item-first, .del-item-first{
      padding: 0 8px;
      font-size: 14px;
      cursor: pointer;
      border-left: 1px solid #D7D8DB;
    }
    .del-item-first{
      margin-top: 5px;
    }
  }
}
.cont{
  position: relative;
  width: 100%;
  height: 117px;
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

.table-form{
  .el-form-item{
    margin-bottom: 0;
  }
}

/deep/.tb-edit{
  .el-form-item{
    margin-bottom: 0;
  }
  .el-table__body-wrapper{
    height: auto!important;
    .el-table__empty-block{
      width: 100%!important;
    }
  }
  .el-table__body-wrapper::-webkit-scrollbar{
    height: 8px;
  }
  .el-table__body-wrapper::-webkit-scrollbar-thumb{
    border-radius: 20px;
  }
  .el-table__fixed-right{
    padding-bottom: 4px;
  }
  .cell{
    >div{
      width: 100%;
    }
    .el-radio-group, .el-checkbox-group{
      width: 100%;
      div{
        display: flex;
        flex-wrap: wrap;
        .el-radio, .el-checkbox{
          min-width: 50%;
          margin-right: 0;
          text-align: left;
        }
      }
    }
  }
}
/deep/.tableform-no-label-item{
  .el-form-item__content{
    margin-left: 0!important;
  }
}
.design-table-form{
  /deep/.el-table__header{
    .el-table__cell{
      padding: 0!important;
      .cell{
        padding: 0;
        .headeritem{
          padding: 10px;
          cursor: move;
        }
      }
    }
    .el-table__cell:nth-last-of-type(2){
      padding: 0 10px!important;
    }
  }
}
</style>