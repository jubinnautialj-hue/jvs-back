<template>
  <div :class="{'table-form': true, 'table-form-noteditable': !item.editable, 'empty-data': !(tableData && tableData.length > 0)}">
    <jvs-table
      v-if="readyShow"
      :pageheadertitle="item.editable ? '' : item.label"
      :option="options"
      :page="page"
      :data="tableData"
      :index="true"
      :editable="item.editable"
      @on-load="getList"
    >
      <template slot="menu" slot-scope="scope">
        <div :style="'width: 100%;display: flex;justify-content: '+ option.menuAlign == 'left' ? 'left' : 'center' + ';'">
          <slot name="menuBtn" :row="scope.row" :index="scope.index"></slot>
        </div>
      </template>
      <template v-for="(titem, tindex) in options.tableColumn" :slot="titem.prop" slot-scope="scope">
        <div :key="item.prop+scope.index+'-'+titem.prop+'node'+tindex" style="width: 100%;">
          <div v-if="titem.text && titem.text.label && scope.row[titem.prop] == titem.text.value" :key="titem.prop+'text'">
            <span>{{titem.text.label}}</span>
          </div>
          <div v-if="titem.needSlot !== true && !(titem.text && titem.text.label && scope.row[titem.prop] == titem.text.value) && displayByBind(titem, scope.row)" :key="item.prop+scope.index+'-'+titem.prop+'item'">
            <el-form
              v-if="!(scope.row[titem.prop] === null || scope.row[titem.prop] === undefined)"
              :model="scope.row"
              :ref="(formRef || 'ruleForm')+(scope.index*options.tableColumn.length+tindex)"
              class="demo-dynamic"
              size='mini'
              @submit.native.prevent
            >
              <el-form-item label="" :prop='titem.prop' :rules='requireExpressHandle(titem.rules, titem, tindex, scope.row, scope.index, options.tableColumn.length)' style="margin-bottom: 0;">
                <slot v-if="titem.formSlot" :name="titem.prop+'TableFormColumn'" :index="scope.index" :row="scope.row"></slot>
                <tableFormItem
                  v-else
                  :tableRowAIndex="scope.index"
                  :style="'justify-content:'+ (options.align == 'center' ? 'center' : 'flex-start')"
                  :form="getDefaultData(scope.row)"
                  :item="{...titem, disabledControl: disabledExpressHandle(titem, scope.row, getDefaultData(scope.row))}"
                  :roleOption="roleOption"
                  :userList="userList"
                  :departmentList="departmentList"
                  :postList="postList"
                  :designId="designId"
                  :isView="isView"
                  :execsList="execsList"
                  :jvsAppId="jvsAppId"
                  :originForm="forms"
                  :dataTriggerFresh="dataTriggerFresh"
                  :changeRandom="changeRandom"
                  :changeDomItem="changeDomItem"
                  :ruleChange="ruleChange"
                  :openByButton="openByButton"
                  @formChange="formChange"
                  @reInitData="reInitData"
                  @validateHandle="validateHandle"
                ></tableFormItem>
              </el-form-item>
            </el-form>
          </div>
          <div v-if="displayByBind(titem, scope.row) && !(titem.text && titem.text.label && scope.row[titem.prop] == titem.text.value) && titem.needSlot === true" :key="titem.prop+'needslotitem'">
            <slot :name="titem.prop+'Item'" :row="scope.row" :index="scope.index"></slot>
          </div>
        </div>
      </template>
    </jvs-table>
  </div>
</template>
<script>
import {getDefaultData} from '@/views/page/util/common'
import {getSelectData} from '@/api/index'
import {sendMyRequire} from '@/api/newDesign'
import {getSystemDictItems, getClassifyDictTree} from '@/api/newDesign'
import {getDeptList, getRoleList, getPostList} from '../api'
import {areaList} from '@/const/chinaArea.js'
import {getUserInfo} from '@/api/admin/user'
export default {
  name: 'table-Form',
  components: {
    // 异步import，formitem引用了tableForm，嵌套时异步引用
    tableFormItem: () => import('@/components/basic-assembly/formitem'),
  },
  props: {
    formRef: {
      type: String,
      default: 'ruleForm'
    },
    item: {
      type: Object
    },
    option: {
      type: Object
    },
    data: {
      type: Array,
      default: () => {
        return []
      }
    },
    originOption: {
      type: Object
    },
    defalutSet: {
      type: Object
    },
    rowData: {
      type: Object
    },
    // 用户列表
    userList : {
      type: Array,
      default: () => {
        return []
      }
    },
    // 角色列表
    roleOption: {
      type: Array,
      default: () => {
        return []
      }
    },
    // 部门列表
    departmentList: {
      type: Array,
      default: () => {
        return []
      }
    },
    // 岗位列表
    postList: {
      type: Array,
      default: () => {
        return []
      }
    },
    resetRadom: {
      type: Number
    },
    designId: {
      type: String
    },
    forms: {
      type: Object
    },
    originForm: {
      type: Object
    },
    dataModelId: {
      type: String
    },
    changeRandom: {
      type: Number
    },
    changeDomItem: {
      type: Object
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
    dataTriggerFresh: {
      type: Number
    },
    tableFormAddHandleIndex: {
      type: Number
    },
    openByButton: {
      type: Object
    }
  },
  computed: {
    options (){
      let temp = this.option // 不可深拷贝，会丢失函数属性字段(自定义正则校验)
      if(temp.column && !temp.tableColumn) {
        temp.tableColumn = temp.column
      }
      for(let i in temp.tableColumn) {
        temp.tableColumn[i].slot = (this.item.addBtnOrigin == 'table' ? false : (this.item.editable !== false))
      }
      if(!temp.column) {
        temp.column = temp.tableColumn
      }
      temp.tableColumn = this.displayExpressHandle(temp.tableColumn)
      temp.column = temp.tableColumn
      if(this.item.editable) {
        if(this.item.delBtn) {
          temp.menu = true
        }else{
          temp.menu = false
        }
      }else{
        if(this.item.addBtnOrigin == 'table') {
          if(this.item.delBtn) {
            temp.menu = true
          }else{
            temp.menu = false
          }
        }else{
          if(this.item.editBtn || this.item.viewBtn || this.item.delBtn) {
            temp.menu = true
          }else{
            temp.menu = false
          }
        }
      }
      if(this.item.disabled) {
        temp.menu = false
      }
      if(false && this.item.page && !this.item.editable) {
        temp.page = true
      }else{
        temp.page = false
      }
      if(this.item.menuFix) {
        temp.menuFix = this.item.menuFix
      }
      if (this.item.menuColor) {
        temp.menuColor = this.item.menuColor
      }
      if(temp.menuAlign != 'left') {
        temp.menuAlign = 'center'
      }
      temp.indexLabel = '序号'
      temp.border = false
      temp.hideTop = true
      if(!this.item.editable) {
        if(this.item.addBtnOrigin == 'table') {
          if(this.item.delBtn) {
            temp.menuWidth = 80
          }
        }else{
          if(this.item.editBtn && this.item.viewBtn && this.item.delBtn) {
            temp.menuWidth = '120px'
          }else{
            temp.menuWidth = '80px'
          }
        }
      }
      if(this.item && this.item.iconBtn) {
        temp.menuWidth = '20px'
        temp.menuText = ''
        temp.indexWidth = '44'
      }
      return temp
    }
  },
  created () {
    if(this.$store.getters && this.$store.getters.userInfo) {
      this.userInfo = JSON.parse(JSON.stringify(this.$store.getters.userInfo))
    }else{
      this.getUserInfo()
    }
    this.init()
  },
  data () {
    return {
      title: '',
      tableData: [],
      userInfo: {},
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
        pageSizes: [20, 50, 100, 200, 500, 1000]
      },
      openType: '', // 弹框类型
      rowIndex: -1, // 行数据index
      loadTimes: -1, // 加载次数
      initData: [],
      readyShow: false,
      reInitIndex: -1,
      ruleChange: -1,
    }
  },
  methods: {
    getUserInfo() {
      getUserInfo().then(res => {
        if(res.data.code == 0) {
          this.userInfo = res.data.data
        }
      })
    },
    // 扩展组件默认值
    getDefaultData(obj) {
      return getDefaultData(obj, this.options.tableColumn, this.userInfo)
    },
    async init () {
      let deptBool = false
      let roleBool = false
      let postBool = false
      for(let i in this.options.tableColumn) {
        if(this.options.tableColumn[i].type == 'department') {
          deptBool = true
        }
        if(this.options.tableColumn[i].type == 'role') {
          roleBool = true
        }
        if(this.options.tableColumn[i].type == 'post') {
          postBool = true
        }
        // 接口字典
        if(this.options.tableColumn[i].dicUrl) {
          await getSelectData(this.options.tableColumn[i].dicUrl).then(res => {
            if(res.data.code == 0) {
               for(let sitem in res.data.data){
                if(typeof sitem == 'string') {
                  this.options.tableColumn[i].dicData.push({
                    label: res.data.data[sitem],
                    value: res.data.data[sitem]
                  })
                }else{
                  this.options.tableColumn[i].dicData.push({
                    label: res.data.data[sitem][this.options.tableColumn[i].props.label ? this.options.tableColumn[i].props.label : 'label'],
                    value: res.data.data[sitem][this.options.tableColumn[i].props.value ? this.options.tableColumn[i].props.value : 'value']
                  })
                }
              }
            }
          })
        }
        // 系统字典
        if(this.options.tableColumn[i].datatype == 'system' && this.options.tableColumn[i].systemDict) {
          await getSystemDictItems(this.options.tableColumn[i].systemDict).then(res => {
            if(res.data.code == 0) {
              this.options.tableColumn[i].dicData = []
               for(let sitem in res.data.data){
                if(typeof res.data.data[sitem] == 'string') {
                  this.options.tableColumn[i].dicData.push({
                    label: res.data.data[sitem],
                    value: res.data.data[sitem]
                  })
                }else{
                  this.options.tableColumn[i].dicData.push({
                    label: res.data.data[sitem][this.options.tableColumn[i].props.label ? this.options.tableColumn[i].props.label : 'label'],
                    value: res.data.data[sitem][this.options.tableColumn[i].props.value ? this.options.tableColumn[i].props.value : 'value']
                  })
                }
              }
            }
          })
        }
        // 级联选择类
        if(this.options.tableColumn[i].type == 'cascader' && this.options.tableColumn[i].dictName) {
          await getClassifyDictTree(this.options.tableColumn[i].dictName).then(res => {
            if(res.data.code == 0 && res.data.data && res.data.data.children) {
              this.options.tableColumn[i].dicData = res.data.data.children
              this.options.tableColumn[i].emitKey = 'uniqueName'
              this.options.tableColumn[i].props = {
                label: 'name',
                value: 'uniqueName',
                children: 'children'
              }
            }
          })
        }
        // 配置字典
        if(this.options.tableColumn[i].dicData) {
          let temp = []
          let bool = false
          for(let j in this.options.tableColumn[i].dicData) {
            if(typeof this.options.tableColumn[i].dicData[j] == 'string') {
              bool = true
              temp.push({
                label: this.options.tableColumn[i].dicData[j],
                value: this.options.tableColumn[i].dicData[j]
              })
            }
          }
          if(bool) {
            this.options.tableColumn[i].dicData = temp
          }
        }
        // 上传
        if(['imageUpload', 'fileUpload'].indexOf(this.options.tableColumn[i].type) > -1) {
          this.options.tableColumn[i].parent = this.item
        }
        // 地区回显
        if(this.options.tableColumn[i].type == 'chinaArea') {
          this.options.tableColumn[i].dicData = areaList
          this.options.tableColumn[i].props = {
            label: 'name',
            value: this.options.tableColumn[i].emitKey ? this.options.tableColumn[i].emitKey : 'code',
            children: 'children'
          }
        }
      }
      // 部门回显
      if(deptBool) {
        await getDeptList().then(res => {
          if(res.data.code == 0) {
            for(let i in this.options.tableColumn) {
              if(this.options.tableColumn[i].type == 'department') {
                this.options.tableColumn[i].dicData = res.data.data
              }
            }
          }
        })
      }
      // 角色回显
      if(roleBool) {
        await getRoleList().then(res => {
          if(res.data.code == 0) {
            for(let i in this.options.tableColumn) {
              if(this.options.tableColumn[i].type == 'role') {
                this.options.tableColumn[i].dicData = res.data.data
              }
            }
          }
        })
      }
      // 岗位回显
      if(postBool) {
        await getPostList().then(res => {
          if(res.data.code == 0) {
            for(let i in this.options.tableColumn) {
              if(this.options.tableColumn[i].type == 'post') {
                this.options.tableColumn[i].dicData = res.data.data
              }
            }
          }
        })
      }
      // if(this.item.formId && !this.isView) {
      //   await this.getDataByFilter()
      // }
      this.readyShow = true
    },
    // 下拉选择change
    valueChange (item, row) {
      this.$emit((item.prop+'Change'), {
        item: item,
        row: row
      })
    },
    // 根据绑定字段值决定显隐
    displayByBind (item, row) {
      let temp = true
      if(item.display) {
        if(typeof item.display.value == 'object' && (item.display.value instanceof Array)) {
          if(item.display.value.indexOf(row[item.display.key]) > -1) {
            temp = true
          }else{
            temp = false
          }
        }else{
          if(row[item.display.key] == item.display.value) {
            temp = true
          }else{
            temp = false
          }
        }
      }else{
        temp = true
      }
      return temp
    },
    async formChange (form, item) {
      if(this.$refs[this.formRef || 'ruleForm'] && this.$refs[this.formRef || 'ruleForm'].length > 0) {
        for(let r in this.$refs[this.formRef || 'ruleForm']) {
          this.$refs[this.formRef || 'ruleForm'][r].validate( (valid) => {
            //
          })
        }
      }
      if(!item) {
        this.ruleChange = Math.random()
      }
      this.$emit('formChange', form, item)
    },
    // 表格数据api来源
    getList () {
      if(this.item.tableEchoRequest && !this.item.editable) {
        // 请求接口
        let tp = JSON.parse(JSON.stringify(this.item.tableEchoRequest))
        if(this.$store.getters.labelValue && this.$store.getters.labelValue.requestContentType) {
          tp.requestContentType = this.$store.getters.labelValue.requestContentType[tp.requestContentType]
        }
        if(tp && tp.url) {
          let query = {}
          if(this.item.page) {
            query.current = this.page.currentPage
            query.size = this.page.pageSize
          }
          if(this.rowData) {
            query = Object.assign(query, this.rowData)
          }
          sendMyRequire(tp, query).then(res => {
            if(res.data.code == 0) {
              if(this.item.page) {
                this.tableData = res.data.data.records
                this.page.currentPage = res.data.data.current
                this.page.total = res.data.data.total
                this.initData = JSON.parse(JSON.stringify(res.data.data.records))
              }else{
                this.tableData = res.data.data
                this.initData = JSON.parse(JSON.stringify(res.data.data))
              }
              this.$forceUpdate()
            }
          })
        }
      }else{
        if(this.loadTimes == -1 && this.data) {
          this.tableData = JSON.parse(JSON.stringify(this.data))
          this.initData = JSON.parse(JSON.stringify(this.data))
          this.$forceUpdate()
        }
      }
      this.loadTimes++
    },
    reset () {
      this.$set(this, 'tableData', this.data)
      this.page = {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
        pageSizes: [20, 50, 100, 200, 500, 1000]
      }
    },
    async getDataByFilter () {
      let tprop = []
      for(let i in this.item.tableColumn) {
        tprop.push(this.item.tableColumn[i].prop)
      }
      let postData = {
        fieldList: tprop,
      }
      let nomptyValue = true
      if(this.item.dataFilterGroupList && this.item.dataFilterGroupList.length > 0) {
        postData.groupConditions = []
        for(let gi in this.item.dataFilterGroupList) {
          let tgarr = []
          for(let df in this.item.dataFilterGroupList[gi]) {
            let dfit = {
              enabledQueryTypes: this.item.dataFilterGroupList[gi][df].enabledQueryTypes,
              fieldKey: this.item.dataFilterGroupList[gi][df].fieldKey,
            }
            if(['cust', 'role', 'department', 'job', 'user'].indexOf(this.item.dataFilterGroupList[gi][df].type) > -1) {
              dfit.value = this.item.dataFilterGroupList[gi][df].value
            }else{
              let tval = null
              if(typeof this.item.dataFilterGroupList[gi][df].value == 'object' && (this.item.dataFilterGroupList[gi][df].value instanceof Array)) {
                if(this.item.dataFilterGroupList[gi][df].value.length > 0) {
                  if(this.item.dataFilterGroupList[gi][df].value.length == 1) {
                    if(this.item.parentType && this.item.parentKey && this.originForm) {
                      tval = this.originForm[this.item.dataFilterGroupList[gi][df].value[0]]
                    }
                  }else{
                    let pdomList = []
                    let pk = this.item.dataFilterGroupList[gi][df].value.slice(0, this.item.dataFilterGroupList[gi][df].value.length - 1)
                    this.findParentList(this.originOption.column, pk.join('.'), pdomList)
                    if(pdomList.length > 0 && pdomList[0].type == 'tab' && pdomList[0].detachData) {
                      if(pdomList.length >  1) {
                        if(pdomList[1].prop) {
                          tval = this.originForm[pdomList[1].prop]
                          if(tval) {
                            tval = tval[this.item.dataFilterGroupList[gi][df].value[this.item.dataFilterGroupList[gi][df].value.length-1]]
                          }
                        }else{
                          tval = this.originForm[this.item.dataFilterGroupList[gi][df].value[this.item.dataFilterGroupList[gi][df].value.length-1]]
                        }
                      }
                    }else{
                      tval = this.originForm
                      this.item.dataFilterGroupList[gi][df].value.filter(vc => {
                        if(tval && tval[vc]) {
                          tval = tval[vc]
                        }else{
                          tval = null
                        }
                      })
                    }
                  }
                }
              }else{
                tval = this.originForm[this.item.dataFilterGroupList[gi][df].value]
              }
              if((tval == undefined || tval == null || tval == '') &&  this.item.dataFilterGroupList[gi][df].enabledQueryTypes != 'isNull') {
                nomptyValue = false
              }else{
                dfit.value = tval
              }
            }
            tgarr.push(dfit)
          }
          postData.groupConditions.push(tgarr)
        }
      }else{
        postData.conditions = []
        if(this.item.dataFilterList) {
          for(let df in this.item.dataFilterList) {
            let dfit = {
              enabledQueryTypes: this.item.dataFilterList[df].enabledQueryTypes,
              fieldKey: this.item.dataFilterList[df].fieldKey,
            }
            if(this.item.dataFilterList[df].type == 'cust') {
              dfit.value = this.item.dataFilterList[df].value
            }else{
              dfit.value = this.forms[this.item.dataFilterList[df].value]
              if((dfit.value == undefined || dfit.value == null || dfit.value == '') && this.item.dataFilterList[df].enabledQueryTypes != 'isNull' ) {
                nomptyValue = false
              }
            }
            postData.conditions.push(dfit)
          }
        }
      }
      if(nomptyValue) {
        await getSelectData(`/mgr/jvs-design/app/use/${this.jvsAppId}/dynamic/data/query/list/${this.item.formId}`, 'post', postData, this.designId).then(res => {
          if(res.data && res.data.code == 0) {
            this.$set(this, 'tableData', res.data.data)
            this.$emit('setTable', this.tableData)
            // console.log('表格的筛选。。。。', res.data.data)
          }
        })
      }
    },
    reInitData (prop, parentKey, index, tableType) {
      this.reInitIndex = index
      this.$emit('reInitData', prop, parentKey, index, tableType)
      this.$emit('resetAddIndex')
    },
    // 表达式控制显示
    disabledExpressHandle (item, row, form) {
      let bool = false
      let formStr = 'row' // 表单值参数名
      if(item.disabledExpress && item.disabledExpress.length > 0) {
        let list = item.disabledExpress
        let temp = []
        for(let i in list) {
          let prop = (formStr + '.') // 控制字段名
          if(list[i].parent && list[i].parent.length > 0) {
            prop += list[i].parent.join('.')
            prop += '.'
          }
          prop += list[i].prop

          // 校验层级表单值是否为undefined
          let tpr = ''
          let exValidate = true
          tpr += formStr
          for(let p in list[i].parent) {
            tpr += ('.' + list[i].parent[p])
            if(eval(tpr) == undefined) {
              exValidate = false
              break;
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
      if(bool && !item.disabled && item.disabledEmpty) {
        this.$set(form, item.prop, '')
      }
      if(this.item.disabled) {
        bool = true
      }
      return bool
    },
    dealInitData (list) {
      for(let i in list) {
        this.options.tableColumn.filter(tit => {
          if(list[i][tit.prop] === null || list[i][tit.prop]  === undefined) {
            switch(tit.type) {
              case 'switch':
                this.$set(list[i], tit.prop, false);
                break;
              case 'select':
                if(tit.multiple) {
                  this.$set(list[i], tit.prop, []);
                }else{
                  this.$set(list[i], tit.prop, '');
                }
                break;
              case 'checkbox':
                this.$set(list[i], tit.prop, []);
                break;
              case 'datePicker':
                if(['dates', 'daterange', 'monthrange', 'datetimerange'].indexOf(tit.datetype) > -1) {
                  this.$set(list[i], tit.prop, []);
                }else{
                  this.$set(list[i], tit.prop, '')
                }
                break;
              case 'timePicker':
                if(tit.isrange) {
                  this.$set(list[i], tit.prop, [])
                }else{
                  this.$set(list[i], tit.prop, '');
                }
                break;
              case 'imageUpload':
              case 'fileUpload':
                if(tit.limit > 1) {
                  this.$set(list[i], tit.prop, [])
                }else{
                  this.$set(list[i], tit.prop, '')
                }
                break;
              default: this.$set(list[i], tit.prop, '');break;
            }
          }
        })
      }
    },
    // 表达式控制必填校验
    requireExpressHandle (rules, item, index, row, rowIndex, length) {
      if(item.requireExpress && item.requireExpress.length > 0) {
        let bool = false
        let formStr = `row`
        let list = item.requireExpress
        let temp = []
        for(let i in list) {
          let prop = (formStr + '.') // 控制字段名
          if(list[i].parent && list[i].parent.length > 0) {
            prop += list[i].parent.join('.')
            prop += '.'
          }
          prop += list[i].prop
          // 校验层级表单值是否为undefined
          let tpr = ''
          let exValidate = true
          tpr += formStr
          for(let p in list[i].parent) {
            tpr += ('.' + list[i].parent[p])
            if(eval(tpr) == undefined) {
              exValidate = false
              break;
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
            if(this.$refs[(this.formRef || 'ruleForm')+(rowIndex * length + index)] && this.$refs[(this.formRef || 'ruleForm')+(rowIndex * length + index)].length > 0) {
              this.$refs[(this.formRef || 'ruleForm')+(rowIndex * length + index)][0].validateField(item.prop)
            }
          }
        }else{
          if(rules && rules.length > 0) {
            this.$set(rules[0], 'required', false)
            if(this.$refs[(this.formRef || 'ruleForm')+(rowIndex * length + index)] && this.$refs[(this.formRef || 'ruleForm')+(rowIndex * length + index)].length > 0) {
              if(rules.length == 1) {
                this.$refs[(this.formRef || 'ruleForm')+(rowIndex * length + index)][0].clearValidate(item.prop)
              }else{
                this.$refs[(this.formRef || 'ruleForm')+(rowIndex * length + index)][0].validateField(item.prop)
              }
            }
          }
        }
      }
      return rules
    },
    // 表达式控制显隐
    displayExpressHandle (tableColumn) {
      let tpList = []
      let originForm = this.originForm || this.forms
      tableColumn.filter(item => {
        let bool = false
        let formStr = 'originForm' // 表单值参数名
        if(item.displayExpress && item.displayExpress.length > 0) {
          let list = item.displayExpress
          let temp = []
          let needValidateSpecial = false // 是否单独校验tab项的值
          for(let i in list) {
            let prop = (formStr + '.') // 控制字段名
            if(list[i].parent && list[i].parent.length > 0) {
              let pdomList = []
              this.findParentList(this.originOption.column, list[i].parent.join('.'), pdomList)
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
        if(bool) {
          tpList.push(item)
        }
      })
      return tpList
    },
    validateHandle (data) {
      let type = data.type
      let item = data.item
      this.$nextTick( () => {
        for(let index in this.tableData) {
          for(let tindex in this.options.tableColumn) {
            if(this.$refs[(this.formRef || 'ruleForm')+(Number(index)*this.options.tableColumn.length+Number(tindex))]) {
              if(type == 'clear') {
                this.$refs[(this.formRef || 'ruleForm')+(Number(index)*this.options.tableColumn.length+Number(tindex))][0].clearValidate(item.prop)
              }else{
                this.$refs[(this.formRef || 'ruleForm')+(Number(index)*this.options.tableColumn.length+Number(tindex))][0].validateField(item.prop)
              }
            }
          }
        }
      })
      this.$forceUpdate()
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
    }
  },
  watch: {
    tableData: {
      handler(newVal, oldVal) {
        this.$emit('setTable', newVal)
      }
    },
    resetRadom: {
      handler (newVal, oldVal) {
        if(newVal > -1) {
          this.reset()
        }
      }
    },
    data: {
      handler (newVal, oldVal) {
        // 优化👌 表格联动或公式返回操作的一行
        if(newVal && newVal.length == 1 && newVal[0]) {
          if(!this.tableData || this.tableData.length == 0) {
            this.tableData = [ {} ]
          }
          if(this.forms[this.item.prop+'_line'] && this.forms[this.item.prop+'_line'].length > 0) {
            for(let i in this.forms[this.item.prop+'_line'][0]) {
              this.$set(this.tableData[this.reInitIndex > -1 ? this.reInitIndex : (this.tableData.length- 1)], i, this.forms[this.item.prop+'_line'][0][i])
            }
            this.$emit('setTable', this.tableData)
          }else{
            this.tableData = this.data
          }
        }else{
          this.tableData = this.data
        }
        if(this.item.editable) {
          this.dealInitData(this.tableData)
        }
      }
    },
    changeRandom: {
      handler (newVal, oldVal) {
        if(newVal > -1 && this.item.formId && !this.isView && this.item.dataFilterEnable !== false) {
          let bool = true
          if(this.changeDomItem) {
            // 本身子组件触发不请求
            if(this.changeDomItem.parentType == this.item.type) {
              let pks = this.changeDomItem.parentKey.split('.')
              if(pks[pks.length - 1] == this.item.prop) {
                bool = false
              }
            }
            // 触发组件不作为条件的不请求
            if(this.item.dataFilterList && this.item.dataFilterList.length > 0) {
              let pool = false
              this.item.dataFilterList.filter(dit => {
                if(dit.type == 'prop') {
                  if(dit.value == this.changeDomItem.prop) {
                    pool = true
                  }
                }
              })
              if(!pool) {
                bool = false
              }
            }else{
              bool = false
            }
            if(this.item.dataFilterGroupList && this.item.dataFilterGroupList.length > 0) {
              bool = true
              let pool = false
              for(let i  in this.item.dataFilterGroupList) {
                if(this.item.dataFilterGroupList[i].length > 0) {
                  this.item.dataFilterGroupList[i].filter(dgit => {
                    if(typeof dgit.value == 'string') {
                      if(dgit.value == this.changeDomItem.prop) {
                        pool = true
                      }
                    }else{
                      if(dgit.value instanceof Array) {
                        if(dgit.value.join('.') == (this.changeDomItem.parentKey ? `${this.changeDomItem.parentKey}.${this.changeDomItem.prop}` : this.changeDomItem.prop)) {
                          pool = true
                        }
                      }
                    }
                  })
                }
              }
              if(!pool) {
                bool = false
              }
            }else{
              if(this.item.dataFilterList && this.item.dataFilterList.length > 0 && bool) {
                bool = true
              }else{
                bool = false
              }
            }
          }
          if(bool) {
            this.getDataByFilter()
          }
        }
      }
    },
    tableFormAddHandleIndex: {
      handler(newVal, oldVal) {
        if(newVal > -1) {
          this.reInitIndex = newVal
        }
      }
    }
  }
}
</script>
