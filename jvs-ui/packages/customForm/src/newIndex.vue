<template>
  <div class="com-style-set-box">
    <div class="com-style-items-box" v-for="(item,index) in optionsList" :key="index">
      <template v-if="item.showCom">
        <div class="com-style-items-title">
          <div class="left" @click.stop="changeItemOpenStatus(item)">
            <svg class="arrow-icon" :style="`${item.isOpen ? '' : 'transform: rotate(-90deg);'}`">
              <use xlink:href="#icon-jvs-a-zu4495"></use>
            </svg>
            <div class="title">{{item.label}}</div>
          </div>
          <div class="right" v-if="item.showSwitch">
            <el-switch v-model="modelValue[item.switchKey]"></el-switch>
          </div>
        </div>
        <div class="com-style-items-content" :class="[item.isOpen && 'com-style-item-content-open']">
          <div class="com-style-items">
            <div class="com-style-item" v-for="(rItem,rIndex) in item.renderList" :key="rIndex">
              <template v-if="!rItem.eventFun || rItem.eventFun()">
                <div class="com-style-item-title">
                  <div class="label-left">
                    <svg class="check-icon" v-if="rItem.useCheckBox" @click.stop="changeCheckStatus(rItem)">
                      <use :xlink:href="`#${modelValue[rItem.props]?'jvs-public-a-zu3739':'jvs-public-a-juxing2862biankuang'}`"></use>
                    </svg>
                    <div class="label">{{rItem.label}}</div>
                  </div>
                  <div class="label-right" v-if="['legend','tooltip','titleOver','switch'].includes(rItem.comType)">
                    <el-switch v-model="modelValue[rItem.props]" @change="$emit('update', modelValue)"></el-switch>
                  </div>
                </div>
                <div class="com-style-item-container" v-if="!rItem.showCom || rItem.showCom()">
                  <el-input v-if="rItem.comType=='input' || rItem.comType=='title'" class="com-style-input" v-model="modelValue[rItem.props]" :placeholder="`请输入${rItem.label}`" @change="$emit('update', modelValue)"></el-input>
                  <div v-if="rItem.comType=='silder'" style="display: flex;align-items: center;">
                    <el-slider v-model="modelValue[rItem.props]" style="flex: 1;" @change="$emit('update', modelValue)"></el-slider>
                    <div class="input-box">
                      <el-input-number class="slider-input" v-model="modelValue[rItem.props]" size="small" :controls="false" @change="$emit('update', modelValue)"/>
                      <div class="percent">%</div>
                    </div>
                  </div>
                  <el-select v-model="modelValue[rItem.props]" class="com-style-input" v-if="rItem.comType=='select'" @change="$emit('update', modelValue)">
                    <el-option v-for="(item,index) in rItem.dicData" :label="item.label" :value="item.value" :key="index"></el-option>
                  </el-select>
                  <el-input-number v-if="rItem.comType=='inputNumber'" class="com-style-input" controls-position="right" v-model="modelValue[rItem.props]" :min="rItem.min" @change="$emit('update', modelValue)"></el-input-number>
                  <position-selector v-if="rItem.comType == 'positionSelector'" :value="modelValue[rItem.props]" @change="positionChange"></position-selector>
                  <div class="title-style-box title-style-grid" v-if="rItem.comType=='titleStyle'">
                    <div class="title-style-item" :style="{'grid-template-columns':`repeat(${modelValue[rItem.sizeColor]?3:2},1fr)`}">
                      <el-select class="com-style-input" v-model="modelValue[rItem.size]" allow-create  filterable>
                        <el-option v-for="(item,index) in fontSizeList" :key="index" :label="item.label" :value="item.value"></el-option>
                      </el-select>
                      <jvs-colorpicker v-if="modelValue[rItem.sizeColor]" :modelValue="modelValue[rItem.sizeColor]" :resetColor="rItem.resetColor" width="100%" :prop="rItem.sizeColor" @update:modelValue="changeHandle"></jvs-colorpicker>
                      <div class="com-style-other">
                        <div class="action-box" :class="[modelValue[rItem.sizeBold] && 'active']" @click="modelValue[rItem.sizeBold] = !modelValue[rItem.sizeBold]">
                          <i class="bi-iconfont bi-jiacu"></i>
                        </div>
                        <div class="action-box" :class="[modelValue[rItem.sizeSlant] && 'active']" @click="modelValue[rItem.sizeSlant] = !modelValue[rItem.sizeSlant]">
                          <i class="bi-iconfont bi-qiexie"></i>
                        </div>
                        <div class="action-box" :class="[modelValue[rItem.sizeUnderline] && 'active']" @click="modelValue[rItem.sizeUnderline] = !modelValue[rItem.sizeUnderline]">
                          <i class="bi-iconfont bi-a-11-01"></i>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div class="title-style-box title-style-grid" v-if="['backCom','iconBackCom'].includes(rItem.comType)">
                    <div class="title-style-item" :style="{'grid-template-columns':'135.33px 1fr'}">
                      <jvs-colorpicker :modelValue="modelValue[rItem.sizeColor]" :resetColor="rItem.resetColor" width="100%" :prop="rItem.sizeColor" @update:modelValue="changeHandle"></jvs-colorpicker>
                      <div v-if="rItem.comType=='iconBackCom'" class="dic-select-box" :style="{'grid-template-columns':`repeat(${rItem.dicData.length},1fr)`}">
                        <div class="content-select-item" v-for="(dItem,dIndex) in rItem.dicData" :key="dIndex" :class="[modelValue[rItem.props]==dItem.value && 'select-active']" 
                          @click="changeSelectValue(rItem,dItem)">
                          <div class="select-label">{{dItem.label}}</div>
                          <el-popover 
                            trigger="hover"
                            placement="bottom"
                            popper-class="show-button-img-box">
                            <div class="shwo-button-popver-body">
                              <img class="show-button-img" :src="openPopover(rItem,dIndex,dItem)">
                            </div>
                            <svg slot="reference" class="explain-icon">
                              <use xlink:href="#bi-a-shuoming1x"></use>
                            </svg>
                          </el-popover>
                        </div>
                      </div>
                      <jvs-back-img v-if="rItem.comType=='backCom'" :backImgOpt="modelValue[rItem.backImgProps]" :imgListOptions="backImgList" @change='val=>backImgChange(val,rItem)'></jvs-back-img>
                    </div>
                  </div>
                  <div class="title-style-box title-style-grid" v-if="rItem.comType=='iconCom'">
                    <div class="title-style-item">
                      <jvs-select-icon :iconOption="modelValue[rItem.props]" :imgListOptions="cardImgList" width="100%" @change="val=>iconChange(val,rItem)"></jvs-select-icon>
                      <jvs-colorpicker :modelValue="modelValue[rItem.sizeColor]" :resetColor="rItem.resetColor" width="100%" :prop="rItem.sizeColor" @update:modelValue="changeHandle"></jvs-colorpicker>
                    </div>
                  </div>
                  <div class="theme-body" v-if="rItem.comType=='theme'">
                    <el-popover
                      placement="bottom"
                      popper-class="theme-popover"
                      :width="300"
                    >
                      <div class="theme-list-box" style="height: 400px;">
                        <el-scrollbar ref="themeScrollbar" style="height: 100%;">
                          <div class="theme-list-item" v-for="(item,index) in themeList" :key="index">
                            <div class="theme-list-item-box" @click="changeTheme(item)" :class="[defaultTheme.name === item.name && 'active']">
                              <div v-for="(cItem,cIndex) in item.list" :key="cIndex" :style="{background:cItem}" class="theme-item"></div>
                            </div>
                          </div>
                        </el-scrollbar>
                      </div>
                      <div slot="reference" class="theme-box">
                        <div class="theme-items">
                          <div v-for="(item,index) in defaultTheme.list" :key="index" class="theme-item" :style="{background:item}"></div>
                        </div>
                        <svg class="icon">
                          <use xlink:href="#bi-xiala"></use>
                        </svg>
                      </div>
                    </el-popover>
                    <div class="open-gradient" @click="changeGradient(rItem)">
                      <svg class="check-icon">
                        <use :xlink:href="`#${modelValue[rItem.checkProps]?'jvs-public-a-zu3739':'jvs-public-a-juxing2862biankuang'}`"></use>
                      </svg>
                      <span class="check-label">开启渐变</span>
                    </div>
                  </div>
                  <div class="legend-box" v-if="rItem.comType=='legend' && modelValue[rItem.props]">
                    <div class="legend-items" style="align-items: start;">
                      <div class="legend-list">
                        <div v-for="(item,index) in legendList" :key="index" class="legend-item" :class="[item.value==modelValue.legendPos && 'active']" @click="changeLegendPos(item)">
                            <svg class="icon">
                              <use v-bind:xlink:href="`#${item.icon}`"></use>
                            </svg>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div class="legend-box" v-if="rItem.comType=='traget'">
                    <div v-for="(item,index) in modelValue[rItem.props]" :key="index" class="traget-item">
                      <div class="input-box-name">
                        <el-input v-model="item.tragetName" placeholder="输入目标线名称" @change="$emit('update', modelValue)"></el-input>
                      </div>
                      <div class="input-box-value">
                        <el-input-number v-model="item.tragetValue" :min="0" controls-position="right" style="width:100%;" @change="$emit('update', modelValue)"></el-input-number>
                      </div>
                      <jvs-colorpicker :modelValue="item.tragetColor" :resetColor="rItem.resetColor" prop="tragetColor" :form="item" @update:modelValue="changeHandle"></jvs-colorpicker>
                      <div class="del-box" @click="delTraget(index)">
                        <svg class="del-icon">
                          <use xlink:href="#bi-shanchu"></use>
                        </svg>
                      </div>
                    </div>
                    <div class="add-traget-box">
                      <span class="add-traget-text" @click="addTraget">
                        <svg class="add-icon">
                          <use xlink:href="#bi-a-zu5758"></use>
                        </svg>
                        添加目标线
                      </span>
                    </div>
                  </div>
                  <div class="dic-select-box" v-if="rItem.comType=='contentSelect'" :style="rItem.gridStyle">
                    <div v-for="(dItem,dIndex) in rItem.dicData" :key="dIndex"
                      class="content-select-item" 
                      :class="[modelValue[rItem.props]==dItem.value && 'select-active']" 
                      @click="changeSelectValue(rItem,dItem)">
                      <i class="bi-iconfont" :class="dItem.icon" v-if="dItem.icon"></i>
                      <div class="select-label">{{dItem.label}}</div>
                      <el-input class="radius-input" v-if="dItem.isInput" v-model="modelValue[rItem.size]"></el-input>
                      <el-popover
                        v-if="dItem.explain" 
                        trigger="hover"
                        placement="bottom"
                        popper-class="show-button-img-box">
                        <div class="shwo-button-popver-body">
                          <img class="show-button-img" :src="openPopover(rItem,dIndex,dItem)">
                        </div>
                        <svg slot="reference" class="explain-icon">
                          <use xlink:href="#bi-a-shuoming1x"></use>
                        </svg>
                      </el-popover>
                    </div>
                  </div>
                  <div class="title-style-box title-style-grid fixed-grid-box" v-if="rItem.comType=='borderCom'" :style="{'grid-template-columns':`repeat(3,1fr)`}">
                    <div class="grid-item">
                      <div class="title">样式</div>
                      <el-select v-model="modelValue.borderStyle"  ref="borderTypeRef" :class="['com-style-input border-select', modelValue.borderStyle && 'with-border']" @change="borderTypeChange">
                        <el-option label="实线" value="solid" style="display: flex; align-items: center;">
                          <div style="border-top:2px solid #000;width: 100%;height:0px;"></div>
                        </el-option>
                        <el-option label="虚线" value="dashed" style="display: flex; align-items: center;">
                          <div style="border-top:2px dashed #000;width: 100%;height:0px;"></div>
                        </el-option>
                        <el-option label="斑点" value="dotted" style="display: flex; align-items: center;">
                          <div style="border-top:2px dotted #000;width: 100%;height:0px;"></div>
                        </el-option>
                      </el-select>
                    </div>
                    <div class="grid-item">
                      <div class="title">粗细</div>
                      <el-select v-model="modelValue.borderWidth"  class="com-style-input">
                        <el-option label="1" value="1"></el-option>
                        <el-option label="2" value="2"></el-option>
                        <el-option label="3" value="3"></el-option>
                      </el-select>
                    </div>
                    <div class="grid-item">
                      <div class="title">颜色</div>
                      <jvs-colorpicker :modelValue="modelValue.borderColor" :reset-color="rItem.resetColor" :openGradual="false" prop="borderColor" @update:modelValue="changeHandle"></jvs-colorpicker>
                    </div>
                  </div>
                  <div class="title-style-box title-style-grid fixed-grid-box" v-if="rItem.comType=='radiusCom'" :style="{'grid-template-columns':`1fr 36px`}">
                    <div class="radius-items-box">
                      <div class="radius-item">
                        <svg class="icon">
                          <use xlink:href="#bi-zuoshang"></use>
                        </svg>
                        <el-input-number v-model="modelValue.radiusTopLeft" @change="raduisChange" :min="0" class="com-style-input" controls-position="right"></el-input-number>
                      </div>
                      <div  class="radius-item">
                        <svg class="icon">
                          <use xlink:href="#bi-youshang"></use>
                        </svg>
                        <el-input-number v-model="modelValue.radiusTopRight" @change="raduisChange" :min="0" class="com-style-input" controls-position="right"></el-input-number>
                      </div>
                      <div  class="radius-item">
                        <svg class="icon">
                          <use xlink:href="#bi-zuoxia"></use>
                        </svg>
                        <el-input-number v-model="modelValue.radiusBottomLeft" @change="raduisChange" :min="0" class="com-style-input" controls-position="right"></el-input-number>
                      </div>
                      <div  class="radius-item">
                        <svg class="icon">
                          <use xlink:href="#bi-youxia"></use>
                        </svg>
                        <el-input-number v-model="modelValue.radiusBottomRight" @change="raduisChange" :min="0" class="com-style-input" controls-position="right"></el-input-number>
                      </div>
                    </div>
                    <div class="radius-quick-box">
                      <div class="radius-button" :class="[modelValue.useLockBorder && 'lock-radius']" @click.stop="quickSetBorderRaduis">
                        <svg class="icon">
                          <use :xlink:href="`#${modelValue.useLockBorder ? 'bi-suoding':'bi-quxiaosuoding'}`"></use>
                        </svg>
                      </div>
                    </div>
                  </div>
                  <div class="title-style-box title-style-grid fixed-grid-box" v-if="rItem.comType=='shadowCom'" :style="{'grid-template-columns':`repeat(4,1fr)`}">
                    <div class="grid-item">
                      <div class="title">X</div>
                      <el-input-number v-model="modelValue.shadowX" :min="0" class="com-style-input" controls-position="right"></el-input-number>
                    </div>
                    <div class="grid-item">
                      <div class="title">Y</div>
                      <el-input-number v-model="modelValue.shadowY" :min="0" class="com-style-input" controls-position="right"></el-input-number>
                    </div>
                    <div class="grid-item">
                      <div class="title">模糊</div>
                      <el-input-number v-model="modelValue.shadowFuzzy" :min="0" class="com-style-input" controls-position="right"></el-input-number>
                    </div>
                    <div class="grid-item">
                      <div class="title">颜色</div>
                      <jvs-colorpicker :modelValue="modelValue.shadowColor" :reset-color="rItem.resetColor" :openGradual="false" prop="shadowColor" @update:modelValue="changeHandle"></jvs-colorpicker>
                    </div>
                  </div>
                  <div class="title-style-box title-style-grid chart-grid-box" v-if="rItem.comType == 'chartSelect'">
                    <div class="select-item" v-for="(item,index) in rItem.dicData" :key="index" :class="[item.value == modelValue[rItem.props] && 'select-item-active']"
                      @click="changeChartType(rItem,item)">
                      <img :src="item.icon"/>
                      <div class="chart-name">{{item.label}}</div>
                    </div>
                  </div>
                </div>
              </template>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script>
import { themeList, processTypeList } from './config.js'
import infoPng from '../cover/info.png'
import staticPng from '../cover/staticLogo.png'
import pureColorImg from '@/assets/images/backimg/pureColor.png'
import tranColorImg from '@/assets/images/backimg/tranColor.png'
import defaultRaduis from '@/assets/images/backimg/defaultRaduis.png'
import bigRaduis from '@/assets/images/backimg/bigRaduis.png'
import noneRaduis from '@/assets/images/backimg/noneRaduis.png'
import { intersection } from 'lodash'
import materialAttribute from './materialAttribute'
const sortKey =  [
  'componentName', 'name', 'title', 'cardTitle', 'titleSize','contentSize', 'contentAlign', 'contentSpac',
  'position', 'contentBackColor', 'contentBackType',
  'unitText', 'unit', 'unitSize', 'unitColor', 'unitBold', 'unitSlant', 'unitUnderline',
  'isRoam', 'defaultZoom', 'maxZoom', 'minZoom', 'hideOverlap','rootName','showType','showTextType','stepType','step','inRadius',
  'outRadius','tiny','dataZoom','theme', 'showLegend','maxMark','minMark','showTooltip',' backImg','tragetLines',
]
const iconKey = ['iconPos','iconObj','iconBackType','backRadiusType']
const cardKey = ['cardAlign', 'useBack','useBorder','useBorderRadius','useShadow']
import { cardImgList } from "../cardImg";
import { backImgList } from "../backImg";
export default {
  name: 'CustomForm',
  props: {
    itemInfo: Object,
    componentSetting: Object,
    id:String
  },
  data () {
    return {
      cardImgList: cardImgList,
      backImgList: backImgList,
      modelValue: {},
      uploadData: { module: `/${this.id}/cardBackImg` },
      uploadHeaders: { Authorization: 'Bearer '+ this.$store.getters.access_token },
      legendList: [
        {
          icon:'bi-a-zu9440',
          label:'下左',
          value:"bottom-left"
        },
        {
          icon:'bi-a-zu9439',
          label:'底部',
          value:"bottom"
        },
        {
          icon:'bi-a-zu9438',
          label:'下右',
          value:"bottom-right"
        },
        {
          icon:'bi-a-zu9437',
          label:'上左',
          value:"top-left"
        },
        {
          icon:'bi-a-zu9436',
          label:'顶部',
          value:"top"
        },
        {
          icon:'bi-a-zu9435',
          label:'上右',
          value:"top-right"
        },
        {
          icon:'bi-a-zu9434',
          label:'右下',
          value:"right-bottom"
        },
        {
          icon:'bi-a-zu9433',
          label:'右侧',
          value:"right"
        },
        {
          icon:'bi-a-zu9432',
          label:'右上',
          value:"right-top"
        },
        {
          icon:'bi-a-zu9431',
          label:'左下',
          value:"left-bottom"
        },
        {
          icon:'bi-a-zu9430',
          label:'左侧',
          value:"left"
        },
        {
          icon:'bi-a-zu9429',
          label:'左上',
          value:"left-top"
        }
      ],
      optionsList: [],
      fontSizeList: [
        {
          label:'12px',
          value:'12'
        },
        {
          label:'14px',
          value:'14'
        },
        {
          label:'16px',
          value:'16'
        },
        {
          label:'18px',
          value:'18'
        },
        {
          label:'20px',
          value:'20'
        },
        {
          label:'22px',
          value:'22'
        },
        {
          label:'24px',
          value:'24'
        },
        {
          label:'26px',
          value:'26'
        },
        {
          label:'28px',
          value:'28'
        },
        {
          label:'30px',
          value:'30'
        },
        {
          label:'32px',
          value:'32'
        },
        {
          label:'34px',
          value:'34'
        }
      ],
      showImg: '',
      defaultTheme: {},
      themeList: themeList,
    }
  },
  created () {
    if(this.componentSetting) {
      this.modelValue = this.componentSetting
      if(materialAttribute[this.itemInfo.material]) {
        let defaultModel = JSON.parse(JSON.stringify(materialAttribute[this.itemInfo.material]))
        this.modelValue = Object.assign(defaultModel, this.modelValue)
      }else{
        let defaultModel = JSON.parse(JSON.stringify(materialAttribute.public))
        this.modelValue = Object.assign(defaultModel, this.modelValue)
      }
      if(['DataCards', 'ProcessManagement'].indexOf(this.itemInfo.material) > -1) {
        if(this.modelValue.cardImg) {
          if(!this.modelValue.iconObj) {
            this.$set(this.modelValue, 'iconObj', {
              isCheckBackImg: true,
              backImgType: 'material',
              pos:'center',
              backImg: this.modelValue.cardImg,
              horizontalFlip: false,
              verticalFlip: false,
              rotate:0
            })
          }else{
            this.$set(this.modelValue.iconObj, 'backImg', this.modelValue.cardImg)
          }
        }
      }
      if(!this.modelValue.cardBackImg) {
        if(this.modelValue.backImg) {
          this.$set(this.modelValue, 'useBack', true)
          this.$set(this.modelValue, 'cardBackImg', {
            isCheckBackImg: true,
            backImgType: 'custom',
            pos: 'center',
            backImg: this.modelValue.backImg,
            horizontalFlip: false,
            verticalFlip: false,
            rotate: 0
          })
        }
      }else{
        if(this.modelValue.cardBackImg.backImgType !== 'material') {
          this.$set(this.modelValue.cardBackImg, 'backImg', this.modelValue.backImg)
          this.$set(this.modelValue.cardBackImg, 'isCheckBackImg', true)
          this.$set(this.modelValue.cardBackImg, 'backImgType', 'custom')
        }
      }
      if(this.modelValue.legend != null && this.modelValue.legend != undefined) {
        this.$set(this.modelValue, 'showLegend', this.modelValue.legend)
      }
      if(!this.modelValue.name && this.modelValue.titleTxt) {
        this.$set(this.modelValue, 'name', this.modelValue.titleTxt)
        this.$set(this.modelValue, 'titleTxt', '')
      }
      if(!this.modelValue.name && this.modelValue.customText) {
        this.$set(this.modelValue, 'name', this.modelValue.customText)
        this.$set(this.modelValue, 'customText', '')
      }
      if(this.itemInfo.material == 'ProcessManagement') {
        if(!this.modelValue.cardImg && !this.modelValue.iconObj) {
          this.$set(this.modelValue, 'iconObj', {
            isCheckBackImg: true,
            backImgType: 'material',
            pos:'center',
            backImg: cardImgList[0],
            horizontalFlip: false,
            verticalFlip: false,
            rotate:0
          })
        }else{
          if(this.modelValue.iconObj && this.modelValue.iconObj.type == 'custom' && !this.modelValue.iconObj.backImg) {
            this.$set(this.modelValue.iconObj, 'backImg', cardImgList[0])
          }
        }
      }
    }
    this.setOption()
  },
  methods: {
    getShowAttr () {
      let arr = []
      if(materialAttribute[this.itemInfo.material]) {
        arr = Object.keys(materialAttribute[this.itemInfo.material])
      }else{
        arr = Object.keys(materialAttribute.public)
      }
      return arr
    },
    /**
   * 设置配置项的列表
   * label 为展示标题
   * comType 展示组件类型
   * dicData 下拉选择框的数据
   * eventFun 是否有联动属性，用于显示隐藏
   */
    setOption () {
      this.optionsList = []
      let arr = this.getShowAttr()
      let keys = (arr && arr.length > 0) ? arr : Object.keys(this.modelValue)
      this.optionsList.push({
        label: '指标内容',
        isOpen: true,
        showCom: true,
        showSwitch: false,
        showKeys: sortKey,
        renderList: []
      })
      if(['DataCards','InfoCard', 'ProcessManagement'].includes(this.itemInfo.material)) {
        this.optionsList.push({
          label: '图标设置',
          isOpen: true,
          showKeys: iconKey,
          showSwitch: true,
          showCom: true,
          switchKey: 'useIcon',
          renderList: []
        })
      }
      this.optionsList.push({
        label: '组件容器设置',
        isOpen: true,
        showCom: true,
        showKeys: cardKey,
        showSwitch: false,
        renderList: []
      })
      this.optionsList.filter((item)=>{
        let renderKeys = intersection(item.showKeys, keys)
        renderKeys.filter( (key, kIndex) => {
          let label='', comType = '', dicData = [], eventFun = null,
          size = null, sizeColor = null, sizeBold = null, sizeSlant = null, sizeUnderline = null, min = null, max = null,
           gridStyle = null, resetColor = null, backImgProps=null, useCheckBox = false, showCom = null, checkProps = null;
          switch (key) {
            case 'name':
            case 'componentName':
              label = '标题'
              comType = this.itemInfo.material == 'ProcessManagement' ? 'select' : 'title'
              if(this.itemInfo.material == 'ProcessManagement') {
                dicData = processTypeList
              }
              break;
            case "titleSize":
              label = '标题文本格式'
              comType = 'titleStyle'
              size='titleSize'
              if(!['InfoCard','StaticCard','DataCards', 'ProcessManagement'].includes(this.itemInfo.material)){
                sizeColor='titleColor'
                sizeBold="bold"
                sizeSlant="slant"
                sizeUnderline="underline"
              }else{
                sizeColor='titleColor'
                sizeBold="titleBold"
                sizeSlant="titleSlant"
                sizeUnderline="titleUnderline"
              }
              resetColor = '#000000'
              break      
            case 'cardTitle':
              label = '标题内容'
              comType = 'title'
              break
            case 'position':
              label = '文本对齐'
              comType = 'positionSelector'
              break
            case 'contentSize':
              label = '数据文本格式'
              comType = 'titleStyle'
              size='contentSize'
              sizeColor='contentColor'
              sizeBold="contentBold"
              sizeSlant="contentSlant"
              sizeUnderline="contentUnderline"
              resetColor = '#000000'
              break
            case 'contentBackType':
              label = '内容背景色'
              comType = 'iconBackCom'
              sizeColor = 'contentBackColor'
              resetColor = '#F5F6F7'
              dicData = [
                {label:'纯色模式', value:'pureColor'},
                {label:'透亮模式', value:'tranColor'},
              ]
              break
            case 'unit':
            case 'unitText':
              label = '单位'
              comType = 'input'
              break
            case 'unitSize':
              label = '单位文本格式'
              comType = 'titleStyle'
              size='unitSize'
              sizeColor='unitColor'
              sizeBold="unitBold"
              sizeSlant="unitSlant"
              sizeUnderline="unitUnderline"
              break
            case 'chartTheme':
            case 'theme':
              label = '主题'
              comType = 'theme'
              if(this.itemInfo.material == 'CardChart'){
                checkProps = 'chartUseThemeGradient'
              }else{
                checkProps = 'useThemeGradient'
              }
              let themeArr = themeList.filter((item)=> item.value == this.modelValue[key])
              this.defaultTheme = themeArr.length > 0 ? themeArr[0] : themeList[0]
              break;
            case 'inRadius':
              label = '内半径'
              comType = 'silder'
              break
            case 'outRadius':
              label = '外半径'
              comType = 'silder'
              break
            case 'showLegend':
              if(this.modelValue.type != 'ChartHistogram') {
                label = '图例'
                comType='legend'
              }
              break;
            case 'showTooltip':
              label = "提示信息"
              comType='tooltip'
              break
            case 'stepType':
              if(this.modelValue.type == 'ChartHistogram') {
                label = "展示模式"
                comType = 'select'
                dicData = this.modelValue.stepDic
              }
              break
            case 'step':
              if(this.modelValue.type == 'ChartHistogram') {
                label = "步长范围"
                comType = 'inputNumber'
                min = 1
                eventFun = ()=>{
                  return this.modelValue.stepType=='stepSize'
                }
              }
              break
            case 'segm':
              if(this.modelValue.type == 'ChartHistogram') {
                label = "分组数量"
                comType = 'inputNumber'
                eventFun = ()=>{
                  return this.modelValue.stepType=='group'
                }
              }
              break
            case 'showType':
              if(!['ChartHistogram'].includes(this.modelValue.type)) {
                label = '显示方式'
                comType = 'select'
                dicData = [{ label: '分组显示', value: 'base' }, { label: '堆积显示', value: 'stack' },]
              }
              break
            case 'showSummary':
              label ="是否汇总"
              comType = 'switch'
              break
            case 'showSummaryName':
              label ="汇总名称"
              comType = 'input'
              break
            case 'showSummaryPlace':
              label ="汇总缺省"
              comType = 'input'
              break
            case 'showSummaryCompany':
              label ="汇总单位"
              comType = 'input'
              break
            case "tragetLines":
              label = '目标线'
              comType = 'traget'
              break
            case 'minMark':
              label = '最小刻度'
              min = 0
              comType = 'inputNumber'
              break
            case 'maxMark':
              label = '最大刻度'
              min = 0
              comType = 'inputNumber'
              break
            case 'rootName':
              label = '根节点名称'
              comType = 'input'
              break
            case 'tiny':
              label = '是否启用迷你'
              comType = 'switch'
              break
            case 'dataZoom':
              label = '是否启用数据缩放'
              comType = 'switch'
              break
            case 'isRoam':
              label = '缩放平移'
              comType = 'select'
              dicData = [{label:'不开启',value:false},{label:'开启所有',value:true},{label:'开启缩放',value:'scale'},{label:'开启平移',value:'move'}]
              break
            case 'defaultZoom':
              label = '默认比例'
              min = 1
              comType = 'inputNumber'
              break
            case 'maxZoom':
              if([true,'scale'].includes(this.modelValue.isRoam)) {
                label = '最大比例'
                min = 1
                comType = 'inputNumber'
              }
              break
            case 'minZoom':
              if([true,'scale'].includes(this.modelValue.isRoam)) {
                label = '最小比例'
                min = 1
                comType = 'inputNumber'
              }
              break
            case 'hideOverlap':
              label = '重叠隐藏'
              comType = 'titleOver'
              break
            case "showTextType":
              if(['ChartHistogram', 'percent'].includes(this.modelValue.type)) {
                label = '占比显示方式'
                comType = 'select'
                dicData = [{label:'固定显示',value:'resident'},{label:'移入显示',value:'hover'}]
                break
              }
              break
            case 'contentAlign':
              label = '内容对齐方式'
              comType = 'contentSelect'
              dicData = [
                {label:'向左对齐',value:'left',icon:'bi-xiangzuoduiqi1'},{label:'居中对齐',value:'center',icon:'bi-juzhongduiqi2'}
              ]
              gridStyle = {'grid-template-columns':`repeat(${dicData.length},1fr)`}
              break
            case 'contentSpac':
              label = '间距'
              comType = 'contentSelect'
              dicData = [
                {label:'适中',value:'4px',icon:'bi-shizhong1'},
                {label:'紧凑',value:'0px',icon:'bi-jincou1'},
                {label:'宽松',value:'16px',icon:'bi-kuansong1'}
              ]
              gridStyle = {'grid-template-columns':`repeat(${dicData.length},1fr)`}
              break
            
            case 'iconPos':
              label = '图标位置'
              comType = 'contentSelect'
              dicData = [
                {label:'左侧',value:'left',icon:'bi-zuoce1'},
                {label:'右侧',value:'right',icon:'bi-youce1'},
              ]
              gridStyle = {'grid-template-columns':`repeat(${dicData.length},1fr)`}
              break
            case 'iconObj':
              label = '图标选择'
              comType = 'iconCom'
              sizeColor = 'iconColor'
              resetColor = '#000000'
              break
            case 'iconBackType':
              label = '图标背景色'
              comType = 'iconBackCom'
              sizeColor = 'iconBackColor'
              resetColor = '#ffffff'
              dicData = [
                {label:'纯色模式',value:'pureColor'},
                {label:'透亮模式',value:'tranColor'},
              ]
              break
            case 'backRadiusType':
              label = '背景圆角'
              comType = 'contentSelect'
              size = 'backRadius'
              dicData = [
                {label:'标准',value:'tech',explain:true},
                {label:'大',value:'large',explain:true},
                {label:'无',value:'none',explain:true},
                {label:'自定义',value:'custom',isInput:true},
              ]
              gridStyle = {'grid-template-columns':`84px 84px 84px 148px`}
              break
            case 'cardAlign':
              label = '内容在组件中位置'
              comType = 'contentSelect'
              dicData = [
                {label:'横向居左',value:'left',icon:'bi-jincou1'},
                {label:'横向居中',value:'center',icon:'bi-jincou1'},
              ]
              gridStyle = {'grid-template-columns':`1fr 1fr`}
              break
            case 'useBack':
              label = '背景'
              comType = 'backCom'
              sizeColor = 'cardBackColor'
              resetColor = '#ffffff'
              backImgProps = 'cardBackImg'
              useCheckBox = true
              showCom = ()=>{
                return this.modelValue[key]
              }
              break
            case 'useBorder':
              label = '边框'
              comType = 'borderCom'
              useCheckBox = true
              resetColor = '#ffffff'
              showCom = ()=>{
                return this.modelValue[key]
              }
              break
            case 'useBorderRadius':
              label = '圆角'
              comType = 'radiusCom'
              useCheckBox = true
              showCom = ()=>{
                return this.modelValue[key]
              }
              break
            case 'useShadow':
              label = '阴影'
              comType = 'shadowCom'
              useCheckBox = true
              resetColor = '#ffffff'
              showCom = ()=>{
                return this.modelValue[key]
              }
              break
            default:
              break;
          }
          if(label) {
            item.renderList.push({
              label: label,
              props: key,
              comType,
              dicData,
              eventFun,
              size,
              sizeColor,
              sizeBold,
              sizeSlant,
              sizeUnderline,
              min,max,
              gridStyle,
              resetColor,
              useCheckBox,
              backImgProps,
              showCom,
              checkProps
            })
          }
        })
        if(item.renderList.length == 0) {
          item.showCom = false
        }
      })
    },
    changeTheme (item) {
      this.defaultTheme = item
      this.modelValue.theme = item.value || 'default'
      this.$emit('update', this.modelValue)
    },
    changeLegendPos (item) {
      this.modelValue.legendPos = item.value
      this.$emit('update', this.modelValue)
    },
    titleColorChange (val, item) {
      this.modelValue[item] = val
    },
    addTraget () {
      this.modelValue.tragetLines.push({
        tragetName:'',
        tragetValue:'',
        tragetColor:undefined,
      })
      this.$emit('update', this.modelValue)
    },
    delTraget (index) {
      this.modelValue.tragetLines.splice(index,1)
      this.$emit('update', this.modelValue)
    },
    handleSuccess (res, file) {
      this.uploadLoading = false
      if(res && res.code == 0) {
        this.$set(this.modelValue, 'cardImg', res.data.fileLink)
      }
    },
    delBackImg (prop) {
      if(this.itemInfo.material == 'InfoCard'){
        this.modelValue[prop] = infoPng
      }else if(this.itemInfo.material == 'StaticCard'){
        this.modelValue[prop] = staticPng
      }else{
        this.modelValue[prop] = ''
      }
    },
    changeItemOpenStatus (item) {
      item.isOpen = !item.isOpen
      this.$forceUpdate()
    },
    changeSelectValue (item, dItem) {
      this.modelValue[item.props] = dItem.value
    },
    openPopover (rItem, dIndex, dItem) {
      let showImg = ''
      switch (rItem.props) {
        case 'iconBackType':
          if(dItem.value=='pureColor'){
            showImg = pureColorImg
          }else{
            showImg = tranColorImg
          }
          break;
        case 'backRadiusType':
          if(dItem.value=='tech'){
            showImg = defaultRaduis
          }else if(dItem.value=='large'){
            showImg = bigRaduis
          }else{
            showImg = noneRaduis
          }
          break
        default:
          break;
      }
      return showImg
    },
    iconChange (val, rItem) {
      this.modelValue[rItem.props] = val
    },
    backImgChange (val, rItem) {
      this.modelValue[rItem.backImgProps] = val
    },
    changeCheckStatus (rItem) {
      this.modelValue[rItem.props] = !this.modelValue[rItem.props]
      if(this.modelValue[rItem.props] && rItem.props == 'useBorder') {
        this.$nextTick(()=>{
          this.borderTypeChange()
        })
      }
    },
    borderTypeChange(){
      this.$refs.borderTypeRef[0].$el.children[0].children[0].setAttribute('style',`width:100%;border-top:2px ${this.modelValue.borderStyle} #000;`)
    },
    quickSetBorderRaduis () {
      this.modelValue.radiusTopLeft = 4
      this.modelValue.radiusTopRight = 4
      this.modelValue.radiusBottomLeft = 4
      this.modelValue.radiusBottomRight = 4
      this.modelValue.useLockBorder = !this.modelValue.useLockBorder
      this.$forceUpdate()
    },
    raduisChange (val) {
      if(this.modelValue.useLockBorder) {
        this.modelValue.radiusTopLeft = val
        this.modelValue.radiusTopRight = val
        this.modelValue.radiusBottomLeft = val
        this.modelValue.radiusBottomRight = val
      }
    },
    changeHandle (value, prop, form) {
      if(form) {
        this.$set(form, prop, value)
        this.$emit('update', this.modelValue)
      }else{
        this.$set(this.modelValue, prop, value)
      }
    },
    changeGradient (item) {
      this.modelValue[item.checkProps] = !this.modelValue[item.checkProps]
      this.$emit('update', this.modelValue)
    },
    changeChartType (rItem, item) {
      this.modelValue[rItem.props] = item.value
      this.$emit('update', this.modelValue)
      this.$forceUpdate()
    },

    positionChange (val) {
      this.modelValue.position = val
      this.$emit('update', this.modelValue)
    }
  },
  watch: {
    id: {
      handler(newVal, oldVal) {
        if(newVal != oldVal) {
          this.setOption()
        }
      }
    },
    modelValue: {
      handler(newVal, oldVal) {
        this.$emit('update', this.modelValue)
      }
    }
  }
}
</script>
<style lang="scss" scoped>
.com-style-set-box{
  /deep/.el-input:not(.radius-input){
    .el-input__inner{
      border: 0;
    }
  }
  .com-style-items-box{
    .com-style-items-title{
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 8px;
      padding: 15px 16px 0px;
      .left{
        display: flex;
        align-items: center;
        cursor: pointer;
        user-select: none;
        .arrow-icon{
          width: 14px;
          height: 14px;
          min-width: 14px;
        }
        .title{
          font-family: Source Han Sans-Bold, Source Han Sans;
          font-size: 14px;
          margin-left: 4px;
          color: #363b4c;
        }
      }
    }
    .com-style-items-content{
      display: grid;
      flex-direction: column;
      grid-template-rows: 0fr;
      overflow: hidden;
      transition: all 0.4s;
      .com-style-items{
        overflow: hidden;
        padding: 0px 16px;
        .com-style-item{
          margin-bottom: 16px;
          .com-style-item-title{
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 8px;
            user-select: none;
            .label-left{
              display: flex;
              align-items: center;
              .check-icon{
                width: 16px;
                height: 16px;
                min-width: 16px;
                margin-right: 8px;
                cursor: pointer;
              }
              .label{
                font-size: 14px;
                color: #363b4c;
              }
            }
          }
          .com-style-item-container{
            .title-style-box{
              .title-style-item{
                display: flex;
                align-items: center;
                .color-picker-com{
                  height: 30px;
                }
                .com-style-other{
                  height: 36px;
                  background: #F5F6F7;
                  border-radius: 4px 4px 4px 4px;
                  padding: 4px 8px;
                  display: grid;
                  grid-column-gap: 8px;
                  grid-template-columns: repeat(3,1fr);
                  box-sizing: border-box;
                  .action-box{
                    width: 32px;
                    height: 28px;
                    border-radius: 4px 4px 4px 4px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    cursor: pointer;
                    &:hover{
                      background: #eeeff0;
                    }
                    .bi-iconfont{
                      color: #363B4C;
                    }
                  }
                  .active{
                    background: #ffffff !important;
                    .bi-iconfont{
                      color: #1E6FFF;
                    }
                  }
                }
                .card-img-list{
                  width: 100%;
                  display: flex;
                  flex-wrap: wrap;
                  img{
                    width: 32px;
                    height: 32px;
                    cursor: pointer;
                    margin-right: 20px;
                  }
                }
              }
              .title-style-item+.title-style-item{
                margin-top: 12px;
              }
            }
            .title-style-grid{
              .title-style-item{
                display: grid;
                grid-column-gap: 17px;
                grid-template-columns: repeat(3,1fr);
              }
              .grid-item,.radius-items-box{
                .el-input-number.com-style-input{
                  /deep/.el-input-number__decrease,/deep/.el-input-number__increase{
                    display: none;
                  }
                  /deep/.el-input__inner{
                    padding: 0px 15px !important;
                  }
                }
                .border-select.with-border{
                  position: relative;
                  /deep/.el-input{
                    display: flex;
                    align-items: center;
                    height: 36px;
                    .el-input__inner{
                      height: 0;
                      width: calc(100% - 40px)!important;
                      margin-left: 10px;
                    }
                  }
                }
              }
              .grid-item{
                .title{
                  font-size: 14px;
                  color: #363B4C;
                  margin-bottom: 6px;
                }
              }
              .radius-items-box{
                display: grid;
                grid-template-columns: repeat(2,1fr);
                grid-column-gap: 16px;
                grid-row-gap: 8px;
                .el-input-number.com-style-input{
                  /deep/.el-input__inner{
                    padding: 0px 15px 0px 48px !important;
                  }
                }
                .radius-item{
                  position: relative;
                  .icon{
                    width: 16px;
                    height: 16px;
                    position: absolute;
                    z-index: 2;
                    left: 16px;
                    top: 50%;
                    transform: translateY(-50%);
                  }
                }
              }
              .radius-quick-box{
                display: flex;
                align-items: center;
                .radius-button{
                  width: 36px;
                  height: 36px;
                  border-radius: 4px;
                  background: #F5F6F7;
                  cursor: pointer;
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  .icon{
                    width: 16px;
                    height: 16px;                    
                  }
                }
                .lock-radius{
                  background: #1E6FFF;
                }
              }
            }
            .fixed-grid-box{
              display: grid;
              grid-column-gap: 17px;
            }
            .theme-body{
              overflow: hidden;
              grid-template-columns: 328px 1fr;
              display: grid;
              grid-column-gap: 16px;
              user-select: none;
              .theme-box{
                display: flex;
                align-items: center;
                justify-content: space-between;
                cursor: pointer;
                height: 48px;
                background: #F5F6F7;
                border-radius: 4px 4px 4px 4px;
                padding: 0px 8px;
                box-sizing: border-box;
                .theme-items{
                  display: flex;
                  align-items: center;
                  height: 36px;
                  background: #FFFFFF;
                  border-radius: 4px 4px 4px 4px;
                  padding: 0px 8px;
                  .theme-item{
                    width: 32px;
                    height: 24px;
                    border-radius: 4px 4px 4px 4px;
                  }
                  .theme-item+.theme-item{
                    margin-left: 12px;
                  }
                }
                .icon{
                  width: 16px;
                  height: 16px;
                }
              }
              .open-gradient{
                display: flex;
                align-items: center;
                cursor: pointer;
                .check-icon{
                  width: 16px;
                  height:16px;
                }
                .check-label{
                  color: #363B4C;
                  font-size: 14px;
                  margin-left: 8px;
                }
              }
            }
            .dic-select-box{
              height: 36px;
              background: #F5F6F7;
              border-radius: 4px 4px 4px 4px;
              padding: 4px 8px;
              box-sizing: border-box;
              display: grid;
              grid-column-gap: 8px;
              user-select: none;
              .content-select-item{
                display: flex;
                align-items: center;
                justify-content: center;
                cursor: pointer;
                border-radius: 4px 4px 4px 4px;
                i{
                  font-size: 20px;
                  color: #6f7588;
                }
                .select-label{
                  font-size: 14px;
                  color: #363b4c;
                  margin-left: 8px;
                }
                .explain-icon{
                  width: 16px;
                  min-width: 16px;
                  height: 16px;
                  margin-left: 4px;
                  outline: none !important;
                }
                &:hover{
                  background-color: #eeeff0;
                }
                .radius-input{
                  width: 56px;
                  height: 24px;
                  margin-left: 8px;
                  /deep/.el-input__inner{
                    height: 24px;
                    line-height: 24px;
                  }
                }
              }
              .select-active{
                background: #FFFFFF !important;
                i{
                  color: #2d79fe;
                }
                .select-label{
                  color: #2D79FE;
                }
                .radius-input{
                  /deep/.el-input__inner{
                    background: #F5F6F7;
                  }
                }
              }
            }
            .legend-box{
              background: #F5F6F7;
              border-radius: 4px 4px 4px 4px;
              padding: 7px 16px 7px;
              box-sizing: border-box;
              .legend-items{
                display: flex;
                align-items: center;
                justify-content: space-between;
                .label{
                  font-size: 14px;
                  color: #363B4C;
                }
                .legend-list{
                  display: grid;
                  grid-template-columns: repeat(7,1fr);
                  grid-column-gap: 16px;
                  grid-row-gap: 16px;
                  .legend-item{
                    border-radius: 4px 4px 4px 4px;
                    width: 40px;
                    height: 40px;
                    background-color: #FFFFFF;
                    border: 1px solid transparent;
                    box-sizing: border-box;
                    .icon{
                      width: 40px;
                      height: 40px;
                      cursor: pointer;
                    }
                  }
                  .active{
                    border-color: #1E6FFF;
                  }
                }
              }
              .legend-items+.legend-items{
                margin-top: 12px;
              }
              .traget-item{
                display: flex;
                align-items: center;
                justify-content: center;
                margin-bottom: 8px;
                height: 30px;
                line-height: 30px;
                .input-box-name{
                  width: 144px;
                  /deep/.el-input{
                    .el-input__inner{
                      height: 32px;
                      line-height: 32px;
                      border: 1px solid #DCDFE6;
                    }
                  }
                }
                .input-box-value{
                  width: 104px;
                  margin-left: 16px;
                  /deep/.el-input-number{
                    .el-input__inner{
                      height: 32px;
                      padding-left: 8px;
                      text-align: left;
                      border: 1px solid #DCDFE6;
                    }
                    .el-input-number__decrease, .el-input-number__increase{
                      line-height: 15px;
                    }
                    .el-input-number__increase{
                      top: 2px;
                    }
                    .el-input-number__decrease{
                      bottom: 2px;
                    }
                  }
                }
                .color-picker-com{
                  margin-left: 16px;
                  height: 30px;
                  box-sizing: border-box;
                }
                .del-box{
                  margin-left: 16px;
                  width: 24px;
                  height: 24px;
                  background: #FFFFFF;
                  border-radius: 4px 4px 4px 4px;
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  cursor: pointer;
                  .del-icon{
                    min-width: 16px;
                    width: 16px;
                    height: 16px;
                  }
                }
              }
              .add-traget-box{
                display: flex;
                align-items: center;
                .add-traget-text{
                  color: #1E6FFF;
                  cursor: pointer;
                  display: flex;
                  align-items: center;
                  .add-icon{
                    width: 16px;
                    height: 16px;
                    margin-right: 4px;
                  }

                }
              }
            }
            .input-box{
              position: relative;
              height: 32px;
              .el-input-number{
                height: 32px!important;
                span{
                  display: none;
                }
                /deep/.el-input__inner{
                  padding-right: 32px;
                  input{
                    text-align: left;
                  }
                }
              }
              .slider-input{
                background: #F5F6F7;
                border-radius: 4px;
                margin-left: 16px;
                width: 88px;
                /deep/.el-input__inner{
                  box-shadow: none;
                  background-color: transparent;
                }
              }
              .percent{
                position: absolute;
                right: 10px;
                top: 50%;
                transform: translateY(-50%);
                font-size: 14px;
                color: #6f7588;
              }
              /deep/.el-slider__button{
                box-shadow: 0px 0px 2px 0px rgba(54,59,76,0.15);
                border: 1px solid #C2C5CF;
                width: 16px;
                height: 16px;
              }
            }
          }
          .com-style-input{
            background: #F5F6F7;
            border-radius: 4px;
            width: 100%;
            height: 36px;
            /deep/.el-input__inner{
              box-shadow: none;
              background-color: transparent;
            }
            /deep/.el-input__inner:has(.el-input__inner:focus){
              box-shadow: 0 0 0 1px #1E6FFF inset !important;
            }
            /deep/.el-select__inner{
              height: 36px;
              box-shadow: none;
              background-color: transparent;
            }
            /deep/.el-select__inner.is-focused{
              box-shadow: 0 0 0 1px #1E6FFF inset !important;
            }
            /deep/.el-input__inner{
              text-align: left;
            }
          }
        }
      }
    }
    .com-style-item-content-open{
      grid-template-rows: 1fr;
    }
  }
  .com-style-items-box+.com-style-items-box{
    border-top: 1px solid #eeeff0;

  }
}
</style>
<style lang="scss">
.show-button-img-box{
  min-width: 88px !important;
  padding: 4px !important;
  width: auto !important;
  .shwo-button-popver-body{
    height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    .show-button-img{
      height: 24px;
    }
  }
}
.theme-popover{
  padding: 11px 0px !important;
  background-color: #F5F6F7 !important;
  .theme-list-box{
    .theme-list-item{
      display: flex;
      align-items: center;
      width: 100%;
      margin-bottom: 8px;
      padding: 0px 11px;
      box-sizing: border-box;
      .theme-list-item-box{
        background: #FFFFFF;
        border-radius: 4px 4px 4px 4px;
        padding: 0px 8px;
        display: flex;
        height: 36px;
        align-items: center;
        cursor: pointer;
        border: 1px solid transparent;
        box-sizing: border-box;
        .theme-item{
          width: 32px;
          height: 24px;
          border-radius: 4px 4px 4px 4px;
        }
        .theme-item+.theme-item{
          margin-left: 12px;
        }
      }
      .active{
        border: 1px solid #1E6FFF;
      }
    }
  }
  .el-popper__arrow{
    &::before{
      background: #F5F6F7 !important;
    }
  }
}
</style>
