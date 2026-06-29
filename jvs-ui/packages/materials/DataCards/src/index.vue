<template>
  <div class="data-cards-box" :style="getChartIconPos()">
    <div class="card-left" v-if="componentSetting.useIcon !== false" :style="getIconBoxStyle()">
      <i class="select-iconfont" v-if="componentSetting.iconObj && componentSetting.iconObj.type != 'custom' && componentSetting.iconObj.value" :class="componentSetting.iconObj.value" :style="{background: getIconBackColor()}"></i>
      <div class="select-icon-img" v-if="!(componentSetting.iconObj && componentSetting.iconObj.type == 'material')" :style="getIconImgStyle()"></div>
    </div>
    <div class="card-right" :style="{alignItems: (componentSetting.contentAlign == 'left' ? 'start' : 'center')}">
      <div class="name" :style="{
        fontSize: (componentSetting.titleSize || '14') + 'px',
        fontWeight: componentSetting.titleBold ? 'bold' : '',
        fontStyle: componentSetting.titleSlant ? 'italic' : '',
        background: tranRenderColor(componentSetting.titleColor || '#6f7588'),
        textDecoration: componentSetting.titleUnderline ? `underLine ${getLastColor(componentSetting.titleColor || '#6f7588')}` : ''}">
        {{componentSetting.componentName}}
      </div>
      <div class="content" :style="{
        fontSize: (componentSetting.contentSize || '28') + 'px',
        fontWeight: (componentSetting.contentBold === false ? '' : '700'),
        fontStyle: componentSetting.contentSlant ? 'italic' : '',
        background: tranRenderColor(componentSetting.contentColor || '#363b4c'),
        marginTop: componentSetting.contentSpac,
        textDecoration: componentSetting.contentUnderline ? `underLine ${getLastColor(componentSetting.contentColor || '#363b4c')}` : ''}">
        {{options.data}}
        <span v-if="options.data" class="unit-span" :style="{
          fontSize: (componentSetting.unitSize || '12') + 'px',
          fontWeight: componentSetting.unitBold ? 'bold' : 'normal',
          fontStyle: componentSetting.unitSlant ? 'italic' : 'normal',
          background: tranRenderColor(componentSetting.unitColor || '#6f7588'),
          marginTop: componentSetting.unitSpac,
          textDecoration: componentSetting.unitUnderline ? `underLine ${getLastColor(componentSetting.unitColor || '#6f7588')}` : ''}">
          {{componentSetting.unitText}}
        </span>
      </div>
    </div>
  </div>
</template>

<script>
import { tranRenderColor, getLastColor } from '@/components/colorPicker/utils/common'
export default {
  name: "DataCards",
  props: {
    componentSetting: {
      type: Object,
      default() {
        return {}
      }
    },
    options: {
      type: Object,
      default() {
        return {}
      }
    },
  },
  data() {
    return {}
  },
  methods: {
    tranRenderColor (color) {
      return tranRenderColor(color)
    },
    getLastColor (color) {
      return getLastColor(color)
    },
    getChartIconPos () {
      let styleStr = ''
      if(this.componentSetting.iconPos == 'right' && this.componentSetting.useIcon) {
        styleStr += `justify-content:space-between;flex-direction: row-reverse;padding-right:16px;box-sizing: border-box;`
      }else{
        styleStr += `justify-content:${this.componentSetting.cardAlign == 'center' ? 'center' : 'start'};`
      }
      return styleStr
    },
    getIconBoxStyle () {
      let styleStr = ''
      let borderRadius = this.componentSetting.backRadius
      switch (this.componentSetting.backRadiusType) {
        case 'tech':
          borderRadius = 4
          break;
        case 'large':
          borderRadius = 32
          break
        case 'none':
          borderRadius = 0
          break
        default:
          break;
      }
      styleStr += `border-radius: ${borderRadius}px;`
      if(this.componentSetting.iconBackColor) {
        if(this.componentSetting.iconBackType=='pureColor') {
          styleStr += `background: ${tranRenderColor(this.componentSetting.iconBackColor)};`
        }else{
          styleStr += `background: ${tranRenderColor(this.componentSetting.iconBackColor,15)};`
        }
      }
      return styleStr
    },
    getIconBackColor () {
      if(this.componentSetting.iconBackType == 'pureColor') {
        return tranRenderColor(this.componentSetting.iconColor)
      }else{
        return tranRenderColor(this.componentSetting.iconBackColor)
      }
    },
    getIconImgStyle () {
      let styleStr = ''
      styleStr += `background-image: url(${(this.componentSetting.iconObj && this.componentSetting.iconObj.backImg) ? this.componentSetting.iconObj.backImg : this.componentSetting.cardImg});`
      if(this.componentSetting.iconObj) {
        if(this.componentSetting.iconObj.pos) {
          let posArr =  this.componentSetting.iconObj.pos.split('-')
          if(posArr.length == 1) {
            styleStr += 'background-position:'+posArr[0]+' center;'
          }else{
            styleStr += 'background-position:'+posArr[0]+' '+posArr[1]+';'
          }
        }else{
          styleStr += 'background-position: center;'
        }
        styleStr += `transform:rotate(${this.componentSetting.iconObj.rotate}deg) `
        if(this.componentSetting.iconObj.horizontalFlip) {
          styleStr += 'scaleY(-1)'
        }
        if(this.componentSetting.iconObj.verticalFlip) {
          styleStr += 'scaleX(-1)'
        }
        styleStr += ';'
      }else{
        styleStr += 'background-position: center;'
      }
      return styleStr
    }
  }
}
</script>

<style lang="scss" scoped>
.data-cards-box{
  display: flex;
  align-items: center;
  height: 100%;
  width: 100%;
  user-select: none;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
  overflow: hidden;
  border-radius: 4px;
  .card-left{
    margin: 0 16px;
    width: 64px;
    min-width: 64px;
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
    .select-iconfont{
      font-size: 36px;
      -webkit-background-clip: text !important;
      -webkit-text-fill-color: transparent !important;
    }
    .select-icon-img{
      width: 100%;
      height: 100%;
      background-repeat: no-repeat;
      background-size: 36px 36px;
    }
  }
  .left-img{
    margin: 0 20px;
    min-width: 64px;
    min-height: 64px;
    width: 64px;
    height: 64px;
  }
  .card-right{
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-direction: column;
    padding: 15px;
    box-sizing: border-box;
    .name{
      background-clip: text !important;
      -webkit-background-clip:text !important;
      -webkit-text-fill-color: transparent;
    }
    .content{
      background-clip: text !important;
      -webkit-background-clip:text !important;
      -webkit-text-fill-color: transparent;
    }
    .unit-span{
      display: inline-block;
      background-clip: text !important;
      -webkit-background-clip:text !important;
      -webkit-text-fill-color: transparent;
    }
  }
}
</style>
