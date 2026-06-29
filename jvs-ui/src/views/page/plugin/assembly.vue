<template>
  <el-card class="assemblycont">
    <div class="assemblycontzzc" :style="{'z-index':zzcindex}"></div>
      <el-collapse v-model="activeNames" style="widht:100%;" class="assemblycont-tool-type-list">
        <el-collapse-item title="基础组件" name="1">
          <div v-for='(item,index) in data' :key="index">
            <div :class="{'assemblycont_item': true, 'assemblycont_item_disable': item.disabled}"
              :draggable="item.disabled ? false : true"
              @click="click(item)"
              @dragstart="dragstart(item)"
              @dragend="dragend">
              <i v-if="item.type == 'positionMap'" class="el-icon-location-outline icon" style="font-size: 20px;color: #b9b9b9;"></i>
              <svg v-else class="icon" aria-hidden="true">
                <use :xlink:href="'#'+item.icon"></use>
              </svg>
              <span>{{item.name}}</span>
              <el-popover
                v-if="assemblyInfoData[item.type] && assemblyInfoData[item.type].desc"
                placement="right"
                trigger="hover">
                <div class="assembly-info-con-box" v-html="assemblyInfoData[item.type].desc"></div>
                <span slot="reference" class="info-back-box">
                  <svg aria-hidden="true">
                    <use xlink:href="#icon-jvs-biaozhu"></use>
                  </svg>
                </span>
              </el-popover>
            </div>
          </div>
        </el-collapse-item>
        <el-collapse-item title="高级组件" name="2">
          <div v-for='(item,index) in seniorData' :key="index">
            <div :class="{'assemblycont_item': true, 'assemblycont_item_disable': item.disabled}"
              :draggable="item.disabled ? false : true"
              @click="click(item)"
              @dragstart="dragstart(item)"
              @dragend="dragend">
              <svg class="icon" aria-hidden="true">
                <use :xlink:href="'#'+item.icon"></use>
              </svg>
              <span>{{item.name}}</span>
              <el-popover
                v-if="assemblyInfoData[item.type] && assemblyInfoData[item.type].desc"
                placement="right"
                trigger="hover">
                <div class="assembly-info-con-box" v-html="assemblyInfoData[item.type].desc"></div>
                <span slot="reference" class="info-back-box">
                  <svg aria-hidden="true">
                    <use xlink:href="#icon-jvs-biaozhu"></use>
                  </svg>
                </span>
              </el-popover>
            </div>
          </div>
        </el-collapse-item>
        <el-collapse-item title="扩展组件" name="3">
          <div  v-for='(item,index) in currencyData' :key="'currency'+index">
            <div :class="{'assemblycont_item': true, 'assemblycont_item_disable': item.disabled}"
              :draggable="item.disabled ? false : true"
              @click="click(item)"
              @dragstart="dragstart(item)"
              @dragend="dragend">
              <svg class="icon" aria-hidden="true">
                <use :xlink:href="'#'+item.icon"></use>
              </svg>
              <span>{{item.name}}</span>
              <el-popover
                v-if="assemblyInfoData[item.type] && assemblyInfoData[item.type].desc"
                placement="right"
                trigger="hover">
                <div class="assembly-info-con-box" v-html="assemblyInfoData[item.type].desc"></div>
                <span slot="reference" class="info-back-box">
                  <svg aria-hidden="true">
                    <use xlink:href="#icon-jvs-biaozhu"></use>
                  </svg>
                </span>
              </el-popover>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
  </el-card>
</template>

<script>
import MInput from './assembly/input'
import MTextarea from './assembly/textarea'
import MInputNumber from './assembly/inputNumber'
import MSelect from './assembly/select'
import Mswitch from './assembly/switch'
import MSlider from './assembly/slider'
import Mtimeselect from './assembly/timeselect'
import MTimepicker from './assembly/timepicker'
import MDatePicker from './assembly/datePicker'
import MRadio from './assembly/radio'
import MCheckbox from './assembly/checkbox'
import MDivider from './assembly/divider'
import MP from './assembly/p'

import MImageUpload from './assembly/imageUpload'
import MFileUpload from './assembly/fileUpload'

import MColorSelect from './assembly/colorSelect'
import MIconSelct from './assembly/iconSelect'

import MTab from './assembly/tab'
import MStep from './assembly/step'
import MTableForm from './assembly/tableform'

import MDepartment from './assembly/department'
import MRole from './assembly/role'
import MUser from './assembly/user'
import MPost from './assembly/post'
import MChinaArea from './assembly/chinaArea'

// 详情
import MInputReadOnly from './assembly/inputreadonly'
import MTextareaReadOnly from './assembly/textareareadonly'
import MImage from './assembly/image'
import MFile from './assembly/file'
import MBox from './assembly/box'
import MLink from './assembly/link'
import MIframe from './assembly/iframe'

import MHtmlEditor from './assembly/htmlEditor'
import MButton from './assembly/button'
import MCascader from './assembly/cascader'
import MDatasource from './assembly/datasource'
import MFormbox from './assembly/formbox'

import MReportTable from './assembly/reportTable'
import MTimeLine from './assembly/timeline'
import MChildrenForm from './assembly/childrenForm'
import MConnectForm from './assembly/connectForm'

import MSarialNumber from './assembly/serialNumber'
import MPositionMap from './assembly/map'
import MSignature from './assembly/signature'
import MFlowTable from './assembly/flowTable'
import MFlowNode from './assembly/flowNode'
import MJsonEditor from './assembly/jsonEditor'
import MPageTable from './assembly/pageTable'
import MBluetoothBeacon from './assembly/bluetoothBeacon'
import MDynamicForm from './assembly/dynamicForm'

import {assemblyInfo} from './assembly/assemblyInfo'

export default {
  props: {
    drag2: {
      type: String,
      default: () => {
        return '1'
      }
    },
    // 设计器类型 form填写表单，detail详情表单
    type: {
      type: String,
      default: 'form'
    },
    // 是否为列表页详情按钮
    isDetail: {
      type: Boolean,
      default: () => {
        return false
      }
    },
    // 客户端类型
    supportedClientType: {
      type: String,
      default: 'pc'
    }
  },
  components: {
  },
  data () {
    return {
      activeNames: ['1', '2', '3'],
      lj: false,
      conItems: [
        { type: 'tab', name: '选项卡', icon: 'jvs-ui-icon-xuanxiangka' },
        { type: 'step', name: '步骤条', icon: 'icon-mobanwenjian'},
        { type: 'formbox', name: '表单卡片', icon: 'icon-danjuhao'},
        { type: 'childrenForm', name: '子表单', icon: 'icon-danchuangxuanze'},
        { type: 'tableForm', name: '表格', icon: 'icon-biaoge', support: 'pc'},
        { type: 'reportTable', name: '静态表格', icon: 'icon-biaoge', support: 'pc'},

      ],
      basicItems: [
        { type: 'input', name: '单行文本', icon: 'jvs-ui-icon-danhangwenben' },
        { type: 'textarea', name: '多行文本', icon: 'jvs-ui-icon-duohangwenben' },
        { type: 'divider', name: '分割线', icon: 'jvs-ui-icon-fengexian' },
        { type: 'p', name: '小标题', icon: 'jvs-ui-icon-xiaobiaoti' },
        { type: 'select', name: '下拉框', icon: 'jvs-ui-icon-xialakuang' },
        { type: 'inputNumber', name: '计数器', icon: 'jvs-ui-icon-jishuqi' },
        // { type: 'slider', name: '滑块', icon: 'icon-kaiguan', disabled: true },
        { type: 'switch', name: '开关', icon: 'jvs-ui-icon-kaiguan' },
        { type: 'datePicker', name: '日期', icon: 'jvs-ui-icon-riqi' },
        { type: 'timeSelect', name: '固定时间', icon: 'jvs-ui-icon-gudingshijian' },
        { type: 'timePicker', name: '任意时间', icon: 'jvs-ui-icon-renyishijian' },
        { type: 'radio', name: '单选', icon: 'jvs-ui-icon-danxuan' },
        { type: 'checkbox', name: '多选', icon: 'jvs-ui-icon-duoxuan' },
        {type: 'image', name: '查看图片', icon: 'jvs-ui-icon-tupian'},
        {type: 'file', name: '查看文件', icon: 'jvs-ui-icon-wenjian'},
        { type: 'colorSelect', name: '颜色选择', icon: 'jvs-ui-icon-yansexuanze', disabled: true },
        { type: 'iconSelect', name: '图标选择', icon: 'jvs-ui-icon-tubiaoxuanze' },  
        { type: 'box', name: '描述框', icon: 'jvs-ui-icon-miaoshukuang'},
        { type: 'link', name: '链接', icon: 'jvs-ui-icon-lianjie'},
      ],
      seniorItems: [
        { type: 'cascader', name: '级联选择', icon: 'jvs-ui-icon-jilianxuanze'},
        { type: 'htmlEditor', name: '富文本', icon: 'jvs-ui-icon-fuwenben'},
        // { type: 'datasource', name: '自定义选择', icon: 'icon-jilianxuanze', support: 'pc'},
        { type: 'imageUpload', name: '上传图片', icon: 'jvs-ui-icon-shangchuantupian' },
        { type: 'fileUpload', name: '上传文件', icon: 'jvs-ui-icon-shangchuanwenjian' },
        { type: 'tab', name: '选项卡', icon: 'jvs-ui-icon-xuanxiangka' },
        // { type: 'step', name: '步骤条', icon: 'icon-mobanwenjian'}, // 暂时屏蔽！！！！！！！！
        { type: 'tableForm', name: '表格', icon: 'jvs-ui-icon-biaoge', support: 'pc'},
        // { type: 'reportTable', name: '静态表格', icon: 'icon-biaoge', support: 'pc'}, // 暂时屏蔽！！！！！！！！
        { type: 'button', name: '按钮', icon: 'jvs-ui-icon-anniu'},
        { type: 'iframe', name: '网页', icon: 'jvs-ui-icon-wangye'},
        { type: 'serialNumber', name: '流水号', icon: 'jvs-ui-icon-liushuihao'},
        { type: 'positionMap', name: '定位', icon: 'jvs-ui-icon-dingwei'},
        { type: 'signature', name: '手写签名', icon: 'jvs-ui-icon-shouxieqianming'},
        { type: 'jsonEditor', name: 'JSON', icon: 'jvs-ui-icon-Josnbianyiqi'},
        { type: 'pageTable', name: '列表页', icon: 'jvs-ui-icon-liebiaoye', support: 'pc'},
        { type: 'bluetoothBeacon', name: '蓝牙信标', icon: 'jvs-ui-icon-bluetooth', }
      ],
      containData: [], // 容器组件
      data: [],
      seniorData: [],
      currencyData: [],
      currency: [
        { type: 'department', name: '部门选择', icon: 'jvs-ui-icon-bumenxuanze', disabled: false },
        { type: 'role', name: '角色选择', icon: 'jvs-ui-icon-yonghuxuanze', disabled: false },
        { type: 'user', name: '用户选择', icon: 'jvs-ui-icon-yonghuxuanze', support: 'pc', disabled: false },
        { type: 'job', name: '岗位选择', icon: 'jvs-ui-icon-gangweixuanze', disabled: false },
        // { type: 'chinaArea', name: '地区选择', icon: 'icon-danxuan', support: 'pc' },
        { type: 'timeline', name: '时间线', icon: 'jvs-ui-icon-shijianxian', disabled: false},
        // { type: 'connectForm', name: '关联表单', icon: 'icon-danchuangxuanze', disabled: false},
        // { type: 'flowTable', name: '流程设计', icon: 'icon-fenzhi', disabled: false},
        { type: 'flowNode', name: '动态流程', icon: 'jvs-ui-icon-dongtailiucheng', disabled: false},
        { type: 'dynamicForm', name: '动态表单', icon: 'jvs-ui-icon-changguikapian', disabled: false},
      ],
      detail: [
        {type: 'inputReadOnly', name: '文本框', icon: 'icon-danhang'},
        {type: 'textareaReadOnly', name: '文本域', icon: 'icon-duohangwenbenxiawu50021'},
        {type: 'image', name: '图片', icon: 'icon-tupian-copy'},
        {type: 'file', name: '文件', icon: 'icon-wenjian3'},
        // {type: 'tableReadOnly', name: '表格'},
        {type: 'tab', name: '选项卡', icon: 'icon-xuanxiangka2'},
        {type: 'box', name: '描述框', icon: 'icon-miaoshu'},
        {type: 'link', name: '链接', icon: 'icon-lianjie1'},
        {type: 'iframe', name: '页面嵌入', icon: 'icon-wangye'}
      ],
      activeName: '',
      tabList: [],
      isCollapse: true,
      assemblyInfoData: assemblyInfo
    }
  },
  watch: {
    supportedClientType (val) {
      this.getData()
    }
  },
  created () {
    // console.log(this.detail)
    // console.log(this.supportedClientType)
    if(this.type == 'detail') {
      this.data = this.detail
    }else{
      this.getData()
    }
  },
  computed: {
    zzcindex () {
      return this.drag2==='1'? -10:10
    }
  },
  methods: {
    // 获取左侧控件
    getData() {
      if (this.supportedClientType !== 'pc') {
        let con = this.conItems.filter(item => {
          return item.support !== 'pc'
        })
        this.containData = [...con]
        const arr = this.basicItems.filter(item => {
          return item.support !== 'pc'
        })
        this.data = [...arr]
        const stp = this.seniorItems.filter(item => {
          return item.support !== 'pc'
        })
        this.seniorData = [...stp]
        const currencyTemp = this.currency.filter(item => {
          return item.support !== 'pc'
        })
        this.currencyData = [...currencyTemp]
      } else {
        this.containData = [...this.conItems]
        this.data = [...this.basicItems]
        this.seniorData = [...this.seniorItems]
        this.currencyData = [...this.currency]
      }
    },
    click (item) {
      this.activeName=''
      this.tabList=[]
      if (item.disabled) {
        return false
      }
      this.$emit('setcom', this.getobj(item))
      this.$emit('addcom')
    },
    getobj (data) {
      let obj={}
      // 表单
      if (data.type==='input') {
        obj=new MInput()
      } else if (data.type==='textarea') {
        obj=new MTextarea()
      } else if (data.type==='inputNumber') {
        obj=new MInputNumber()
      } else if (data.type==='select') {
        obj=new MSelect()
      } else if (data.type==='switch') {
        obj=new Mswitch()
      } else if (data.type==='slider') {
        obj=new MSlider()
      } else if (data.type==='timeSelect') {
        obj=new Mtimeselect()
      } else if (data.type==='timePicker') {
        obj=new MTimepicker()
      } else if (data.type==='datePicker') {
        obj=new MDatePicker()
      } else if (data.type==='radio') {
        obj=new MRadio()
      } else if (data.type==='checkbox') {
        obj=new MCheckbox()
      } else if (data.type==='tableForm') {
        obj=new MTableForm()
      } else if (data.type==='divider') {
        obj=new MDivider()
      } else if (data.type==='p') {
        obj=new MP()
      } else if (data.type==='imageUpload') {
        obj=new MImageUpload()
      } else if (data.type==='fileUpload') {
        obj=new MFileUpload()
      } else if (data.type==='colorSelect') {
        obj=new MColorSelect()
      } else if (data.type==='iconSelect') {
        obj=new MIconSelct()
      } else if (data.type==='tab') {
        obj=new MTab()
      } else if (data.type==='step') {
        obj=new MStep()
      } else if (data.type==='formbox') {
        obj=new MFormbox()
      } else if (data.type==='department') {
        obj=new MDepartment()
      } else if (data.type==='role') {
        obj=new MRole()
      } else if (data.type==='user') {
        obj=new MUser()
      } else if (data.type==='job') {
        obj=new MPost()
      } else if (data.type==='chinaArea') {
        obj=new MChinaArea()
      }
      // 详情
      else if (data.type==='inputReadOnly') {
        obj=new MInputReadOnly()
      } else if (data.type==='textareaReadOnly') {
        obj=new MTextareaReadOnly()
      } else if (data.type==='image') {
        obj=new MImage()
      } else if (data.type==='file') {
        obj=new MFile()
      } else if (data.type==='box') {
        obj=new MBox()
      } else if (data.type==='link') {
        obj=new MLink()
      } else if (data.type==='iframe') {
        obj=new MIframe()
      }
      else if (data.type==='htmlEditor') {
        obj=new MHtmlEditor()
      } else if (data.type==='button') {
        obj=new MButton()
      }  else if (data.type==='cascader') {
        obj=new MCascader()
      }  else if (data.type==='datasource') {
        obj=new MDatasource()
      }
      else if (data.type==='reportTable') {
        obj=new MReportTable()
      }
      else if (data.type==='timeline') {
        obj=new MTimeLine()
      }
      else if (data.type === 'childrenForm') {
        obj=new MChildrenForm()
      }
      else if (data.type === 'connectForm') {
        obj=new MConnectForm()
      }
      else if (data.type === 'serialNumber') {
        obj=new MSarialNumber()
      }else if (data.type === 'positionMap') {
        obj=new MPositionMap()
      }else if (data.type === 'signature') {
        obj=new MSignature()
      }else if (data.type === 'flowTable') {
        obj=new MFlowTable()
      }else if (data.type === 'flowNode') {
        obj=new MFlowNode()
      }else if (data.type === 'jsonEditor') {
        obj=new MJsonEditor()
      }else if (data.type === 'pageTable') {
        obj=new MPageTable()
      }else if (data.type === 'bluetoothBeacon') {
        obj=new MBluetoothBeacon()
      }else if  (data.type === 'dynamicForm') {
        obj=new MDynamicForm()
      }
      obj.name = data.name
      if(this.isDetail) {
        obj.disabled = true
        if(obj.type == 'tableForm') {
          obj.addBtn = false
          obj.delBtn = false
        }
      }
      return obj
    },
    dragstart (data) {
      this.$emit("setdrag", { type: 'drag1', value: '2' });
      let obj=this.getobj(data)
      this.$emit('setcom', obj)
    },
    dragend () {
      this.$emit('reset')
    },
    drop () {
      this.lj=false
      this.$emit('deletecom')
    },
    dragover () {
      this.lj=true
    },
    dragleave () {
      this.lj=false
    },
    handleClick () {
      this.setTabList()
    },
    showTab (active) {
      this.activeName=active
      this.setTabList()
    },
    setTabList () {
      if (this.activeName=='basic') {
        this.tabList=this.data
      } else if (this.activeName=='normal') {
        this.tabList=this.currency
      } else {
        this.tabList=[]
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.assemblycont {
  position: relative;
  z-index: 999;
  height: 100%;
  width: 100%;
  background: none;
}
.assemblycontzzc {
  position: absolute;
  top: 0;
  right: 0;
  left: 0;
  bottom: 0;
  background-color: transparent;
}

.assemblycont_item {
  height: 36px;
  border-radius: 3px;
  padding: 9px 8px;
  background: #F5F6F7;
  font-family: Microsoft YaHei-Regular, Microsoft YaHei;
  font-weight: 400;
  font-size: 14px;
  color: #363B4C;
  display: flex;
  align-items: center;
  box-sizing: border-box;
  transition: 0.3s;
  cursor: move;
  overflow: hidden;
  position: relative;
  .icon {
    width: 18px;
    height: 18px;
    fill: currentColor;
    overflow: hidden;
    cursor: pointer;
    margin-right: 12px;
  }
  .info-back-box{
    position: absolute;
    top: 0;
    right: 0;
    svg{
      width: 16px;
      height: 16px;
      fill: #6F7588;
    }
    cursor: pointer;
  }
}
.assemblycont_item:hover {
  transition: 0.3s;
  background: #EEEFF0;
  color: #1E6FFF;
  svg{
    fill: #1E6FFF;
  }
}

.assemblycont_item_disable{
  cursor: no-drop;
}

.assembly-info-con-box{
  padding: 0 10px;
  font-size: 12px;
}

/deep/.assemblycont-tool-type-list{
  border: 0;
  .el-collapse-item__wrap, .el-collapse-item__header{
    border: 0;
    background: none;
  }
  .el-collapse-item__header{
    height: 20px;
    font-family: Source Han Sans-Bold, Source Han Sans;
    font-weight: 700;
    font-size: 14px;
    color: #363B4C;
    line-height: 20px;
    position: relative;
    .el-collapse-item__arrow{
      position: absolute;
      right: 0;
      top: 3px;
    }
    .is-active{
      transform: rotate(90deg);
    }
  }
  .el-collapse-item__wrap{
    padding-top: 12px;
    .el-collapse-item__content{
      width: 100%;
      display: flex;
      flex-wrap: wrap;
      >div{
        width: calc(50% - 4px);
        margin-bottom: 8px;
      }
      >div:nth-of-type(2n+1) {
        margin-right: 8px;
      }
      .assemblycont_item{
        .icon-xiaobiaoti:before{
          font-size: 13px
        }
        .icon-guan:before{
          font-size: 12px;
          width: 18px;
          display: block;
        }
        .icon-lianjie1:before, .icon-wangye:before{
          font-size: 16px;
        }
      }
    }
  }
}
</style>
