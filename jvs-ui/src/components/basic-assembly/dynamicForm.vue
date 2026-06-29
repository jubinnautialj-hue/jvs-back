<template>
  <div class="dynamic-form-box">
    <div class="dynamic-form-list">
      <div v-for="(row, index) in data" :key="'dynamic-form-list-item'+index" class="dynamic-form-list-item">
        <div class="head">
          <div class="body-item">
            <span class="label">组件类型</span>
            <el-select v-model="row.type" @change="typeChange(row, index)">
              <el-option v-for="attr in attrList" :label="attr.label" :value="attr.type" :key="'dy-attr-'+attr.type+'-'+index" size="mini"></el-option>
            </el-select>
          </div>
          <div class="head-right">
            <svg :class="{'close-icon': (openIndex && openIndex.indexOf(index) > -1)}" aria-hidden="true" @click="openCloseIndex(index)">
              <use xlink:href="#icon-jvs-danchuang-shouqi"></use>
            </svg>
            <svg aria-hidden="true" style="width: 14px;height: 14px;" @click="deleteGroup(index)">
              <use xlink:href="#icon-jvs-danchuangguanbi1"></use>
            </svg>
          </div>
        </div>
        <el-collapse-transition>
          <div v-show="!(openIndex && openIndex.indexOf(index) > -1)" class="body">
            <!-- 基础属性 -->
            <div class="basic-attr">
              <div class="body-item">
                <span class="label">字段名称</span>
                <el-input v-model="row.prop" size="mini"></el-input>
              </div>
              <div v-if="row.showFrom && row.showFrom.indexOf('label') > -1" class="body-item">
                <span class="label">中文名</span>
                <el-input v-model="row.label" size="mini"></el-input>
              </div>
              <div v-if="row.showFrom && row.showFrom.indexOf('disabled') > -1 && ['image'].indexOf(row.type) == -1" class="body-item">
                <span class="label">状态</span>
                <el-radio-group v-model="row.disabled" size="mini" class="radio-group-row">
                  <el-radio-button :label="false">普通</el-radio-button>
                  <el-radio-button :label="true">只读</el-radio-button>
                </el-radio-group>
              </div>
              <div v-if="row.showFrom && row.showFrom.indexOf('defaultValue') > -1 && ['timeSelect', 'timePicker', 'datePicker','image', 'imageUpload', 'fileUpload'].indexOf(row.type) == -1" class="body-item">
                <span class="label">默认值</span>
                <!-- 正常组件 -->
                <el-input v-if='["switch","slider", "inputNumber"].indexOf(row.type) === -1' v-model="row.defaultValue" type="textarea" size="mini"></el-input>
                <!-- 开关 -->
                <el-switch
                  v-if='row.type == "switch"'
                  style="width:100%"
                  v-model="row.defaultValue"
                  size="mini"
                >
                </el-switch>
                <!-- 数字 -->
                <el-input-number
                  v-if='["slider", "inputNumber"].indexOf(row.type) > -1'
                  :min="row.min"
                  :max="row.max"
                  :step="row.step"
                  :step-strictly="row.stepstrictly"
                  :precision="row.precision"
                  style="width: 100%;"
                  v-model="row.defaultValue"
                  size="mini">
                </el-input-number>
              </div>
              <div v-if="row.showFrom && row.showFrom.indexOf('span') > -1" class="body-item">
                <span class="label">控件宽度</span>
                <el-slider
                  v-model="row.span"
                  :min="1"
                  :max="24"
                  :step="1"
                  :show-input="false"
                  :range="false"
                ></el-slider>
              </div>
              <div v-if="row.showFrom && row.showFrom.indexOf('rules') > -1 && row.rules && row.rules.length > 0" class="body-item">
                <span class="label">必填校验</span>
                <el-switch size="mini" v-model="row.rules[0].required"></el-switch>
              </div>
              <div v-if="row.showFrom && row.showFrom.indexOf('rules') > -1 && row.rules && row.rules.length > 0 && row.rules[0].required" class="body-item">
                <span class="label">失败提示</span>
                <el-input size="mini" v-model="row.rules[0].message"></el-input>
              </div>
            </div>
            <!-- 特殊属性 -->
            <div class="self-attr">
              <div v-if="row.showFrom && row.showFrom.indexOf('pickeroptions') > -1" class="body-item">
                <span class="label">开始时间</span>
                <el-time-picker v-model="row.pickeroptions.start" placeholder="开始时间" value-format='HH:mm' format='HH:mm' size="mini">
                </el-time-picker>
              </div>
              <div v-if="row.showFrom && row.showFrom.indexOf('pickeroptions') > -1" class="body-item">
                <span class="label">结束时间</span>
                <el-time-picker v-model="row.pickeroptions.end" placeholder="结束时间" value-format='HH:mm' format='HH:mm' size="mini"></el-time-picker>
              </div>
              <div v-if="row.showFrom && row.showFrom.indexOf('pickeroptions') > -1" class="body-item">
                <span class="label">步长</span>
                <el-time-picker v-model="row.pickeroptions.step" :picker-options="{ selectableRange: '00:01:00 - 06:00:00' }" placeholder="步长" value-format='HH:mm' format='HH:mm' size="mini"></el-time-picker>
              </div>
              <div v-if="row.showFrom && row.showFrom.indexOf('multiple') > -1" class="body-item">
                <span class="label">可否多选</span>
                <el-switch v-model="row.multiple" size="mini"></el-switch>
              </div>
              <div v-if="row.showFrom && row.showFrom.indexOf('datatype') > -1" class="body-item">
                <span class="label">数据类型</span>
                <el-select v-model="row.datatype" placeholder="请选择" size="mini" @change="dataTypeChange(row, index)">
                  <el-option label="配置数据" value="option"></el-option>
                  <!-- <el-option v-if="row.type == 'cascader'" label="系统字典" value="system"></el-option> -->
                </el-select>
              </div>
              <div v-if="row.showFrom && row.showFrom.indexOf('option') > -1" class="body-item">
                <span class="label"></span>
                <div style="flex: 1;">
                  <el-table
                    v-if="row.showFrom.indexOf('option') > -1 && row.datatype == 'option'"
                    border
                    :data="row.dicData"
                    class="tb-edit"
                    style="width: 100%"
                    highlight-current-row
                    size='mini'
                  >
                    <el-table-column label="显示值">
                      <template slot-scope="scope">
                        <el-input
                          size="mini"
                          v-model="scope.row.label"
                          placeholder="请输入内容"
                          @change="optionLabelChange(scope.row)"
                        ></el-input>
                      </template>
                    </el-table-column>
                    <el-table-column label="传递值">
                      <template slot-scope="scope">
                        <div style="display: flex;align-items: center;">
                          <el-input v-model="scope.row.value" size="mini" placeholder="请输入内容"></el-input>
                          <el-tooltip class="item" effect="dark" content="传递值均为字符串" placement="top" style="margin-left: 5px;">
                            <i class="el-icon-info"></i>
                          </el-tooltip>
                        </div>
                      </template>
                    </el-table-column>
                    <el-table-column label="操作" :width="55">
                      <template slot-scope="scope">
                        <div>
                          <el-button size="mini" type="text" @click="handleDelete(scope.$index, scope.row, index)">删除</el-button>
                        </div>
                      </template>
                    </el-table-column>
                  </el-table>
                  <div v-if="row.showFrom.indexOf('option') !== -1 && row.datatype == 'option'" class="bottom-button">
                    <div class="button" @click="addoption(index)">
                      <div class="icon">
                        <svg aria-hidden="true">
                          <use xlink:href="#jvs-ui-icon-xinjian"></use>
                        </svg>
                      </div>
                      <span>新增一行</span>
                    </div>
                  </div>
                </div>
              </div>
              <div v-if="row.showFrom && row.showFrom.indexOf('max') > -1" class="body-item">
                <span class="label">最大值</span>
                <el-input-number v-model="row.max"></el-input-number>
              </div>
              <div v-if="row.showFrom && row.showFrom.indexOf('step') > -1" class="body-item">
                <span class="label">步长</span>
                <el-input-number :precision="2" v-model="row.step" size="mini"></el-input-number>
              </div>
              <div v-if="row.showFrom && row.showFrom.indexOf('unit') > -1" class="body-item">
                <span class="label">显示单位</span>
                <el-input v-model="row.unit" size="mini"></el-input>
              </div>
              <div v-if="row.showFrom && row.showFrom.indexOf('precision') > -1" class="body-item">
                <span class="label">精确小数</span>
                <el-input-number :min="0" :max="10" :step="1" stepStrictly v-model="row.precision" size="mini"></el-input-number>
              </div>
              <div v-if="row.showFrom && row.showFrom.indexOf('datetype') > -1" class="body-item">
                <span class="label">选择单位</span>
                <el-select v-model="row.datetype" placeholder="请选择" size="mini">
                  <el-option label="年月日-单个" value="date"></el-option>
                  <el-option label="年周-单个" value="week"></el-option>
                  <el-option label="年月-单个" value="month"></el-option>
                  <el-option label="年-单个" value="year"></el-option>
                  <el-option label="多日期" value="dates"></el-option>
                  <el-option label="年月日时分秒-单个" value="datetime"></el-option>
                  <el-option label="年月日时分秒-范围" value="datetimerange"></el-option>
                  <el-option label="年月日-范围" value="daterange"></el-option>
                  <el-option label="年月-范围" value="monthrange"></el-option>
                </el-select>
              </div>
              <div v-if="row.showFrom && row.showFrom.indexOf('limit') > -1" class="body-item">
                <span class="label">最大允许</span>
                <el-input-number style="width: 100%;" v-model="row.limit" :min="0" size="mini" @change="uploadLimitChange(row, index)"></el-input-number>
              </div>
              <div v-if="row.showFrom && row.showFrom.indexOf('fileType') > -1" class="body-item">
                <span class="label">文件类型</span>
                <el-input v-model="row.fileType" size="mini" placeholder="输入文件后缀名"></el-input>
              </div>
              <div v-if="row.showFrom && row.showFrom.indexOf('fileType') > -1" style="margin-left: 72px;text-align: left;font-size: 12px;color: #909398;">
                <span>多个文件类型后缀名以英文逗号分割</span>
              </div>
              <div v-if="row.showFrom && row.showFrom.indexOf('fileSize') > -1" class="body-item">
                <span class="label">文件最大</span>
                <el-input-number style="width: 100%;" v-model="row.fileSize" :min="0" size="mini" :precision="0" :controls="false"></el-input-number>
                <span style="margin-left: 10px;color: #909398;font-size: 12px;">MB</span>
              </div>
            </div>
          </div>
        </el-collapse-transition>
      </div>
    </div>
  </div>
</template>
<script>
import { getDynamicFormTypeAttr } from '../api'
// 表单项
import MInput from '@/plugin/assembly/input'
import MTextarea from '@/plugin/assembly/textarea'
import MInputNumber from '@/plugin/assembly/inputNumber'
import MSelect from '@/plugin/assembly/select'
import MSwitch from '@/plugin/assembly/switch'
import MSlider from '@/plugin/assembly/slider'
import Mtimeselect from '@/plugin/assembly/timeselect'
import MTimepicker from '@/plugin/assembly/timepicker'
import MDatePicker from '@/plugin/assembly/datePicker'
import MRadio from '@/plugin/assembly/radio'
import MCheckbox from '@/plugin/assembly/checkbox'
import MImageUpload from '@/plugin/assembly/imageUpload'
import MFileUpload from '@/plugin/assembly/fileUpload'
import MImage from '@/plugin/assembly/image'
import MFile from '@/plugin/assembly/file'
import MSarialNumber from '@/plugin/assembly/serialNumber'
import MSignature from '@/plugin/assembly/signature'
export default{
  name: 'dynamic-form',
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
  },
  data () {
    return {
      attrList: [],
      openIndex: []
    }
  },
  created () {
    getDynamicFormTypeAttr().then(res => {
      if(res.data && res.data.code == 0) {
        this.attrList = res.data.data
      }
    })
  },
  methods: {
    openCloseIndex (index) {
      let isIndex = this.openIndex.indexOf(index)
      if(isIndex > -1) {
        this.openIndex.splice(isIndex, 1)
      }else{
        this.openIndex.push(index)
      }
      this.$forceUpdate()
    },
    deleteGroup (index) {
      this.$emit('deleteCom', index)
      let isIndex = this.openIndex.indexOf(index)
      if(isIndex > -1) {
        this.openIndex.splice(isIndex, 1)
      }
      this.$forceUpdate()
    },
    typeChange (row, index) {
      this.$emit('comTypeChange', row, index)
    },
    // 数据类型切换
    dataTypeChange (row, index) {
      if(row.datatype == 'option') {
        this.$set(this.data[index], 'url', '')
        this.$set(this.data[index], 'props', { label: '', value: '' })
      }
      this.$forceUpdate()
    },
    // 添加选项
    addoption (index) {
      if(!this.data[index].dicData) {
        this.$set(this.data[index], 'dicData', [])
      }
      this.data[index].dicData.push({label: '新的选择', value: ('newValue' + this.data[index].dicData.length)})
      this.$forceUpdate()
    },
    // 删除选项
    handleDelete (dix, dic, index) {
      if(this.data[index].dicData && this.data[index].dicData.length > 0) {
        this.data[index].dicData.splice(dix, 1)
        this.$forceUpdate()
      }
    },
    uploadLimitChange (row, index) {
      if(row.limit < 2) {
        this.$set(this.data[index], 'multipleUpload', false)
      }
      this.$forceUpdate()
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
        obj=new MSwitch()
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
      } else if (data.type==='imageUpload') {
        obj=new MImageUpload()
      } else if (data.type==='fileUpload') {
        obj=new MFileUpload()
      } else if (data.type==='image') {
        obj=new MImage()
      } else if (data.type==='file') {
        obj=new MFile()
      } else if (data.type === 'signature') {
        obj=new MSignature()
      } else if (data.type === 'serialNumber') {
        obj=new MSarialNumber()
      }
      let showFromItem = null
      this.attrList.filter(attr => {
        if(attr.type == data.type) {
          showFromItem = attr
        }
      })
      if(showFromItem && showFromItem.showFrom) {
        obj.showFrom = showFromItem.showFrom
      }
      return obj
    },
  }
}
</script>
<style lang="scss" scoped>
.dynamic-form-box{
  width: 100%;
  .dynamic-form-list{
    width: 100%;
    .dynamic-form-list-item{
      background: #F5F6F7;
      border-radius: 4px;
      width: 100%;
      .body-item{
        display: flex;
        align-items: center;
        overflow: hidden;
        .label{
          margin-right: 16px;
          min-width: 56px;
          display: block;
          word-break: keep-all;
          text-align: justify;
          box-sizing: border-box;
          font-family: Source Han Sans-Regular, Source Han Sans;
          font-weight: 400;
          font-size: 14px;
          color: #363B4C;
        }
        /deep/.el-input__inner, /deep/.el-textarea__inner{
          background-color: #fff;
        }
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
      }
      .head{
        height: 56px;
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 8px 16px;
        overflow: hidden;
        box-sizing: border-box;
        .head-right{
          svg{
            width: 16px;
            height: 16px;
            margin-left: 16px;
            cursor: pointer;
            transition: all .3s;
          }
          .close-icon{
            transform: rotateZ(-90deg);
          }
        }
      }
      .body{
        border-top: 1px solid #EEEFF0;
        padding: 16px;
        width: 100%;
        box-sizing: border-box;
        .self-attr{
          margin-top: 16px;
        }
        .body-item{
          &+.body-item{
            margin-top: 16px;
          }
        }
      }
      &+.dynamic-form-list-item{
        margin-top: 16px;
      }
    }
  }
}
</style>
