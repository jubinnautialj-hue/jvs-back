<template>
  <div class="code-card-box" :style="{ background: getComBackColorStyle}">
    <div v-if="componentSetting.useBack !== false && !(componentSetting.cardBackImg && componentSetting.cardBackImg.isCheckBackImg === false)" class="code-card-back-img" :style="getComBackImgStyle">
      <img :src="(componentSetting.cardBackImg && componentSetting.cardBackImg.backImg) ? componentSetting.cardBackImg.backImg : componentSetting.backImg" :style="getBackImageStyle">
    </div>
    <div v-if="componentSetting.componentName" class="code-card-title" id="codeCardTitle">
      <div class="title-text" :style="{
        fontSize: `${componentSetting.titleSize || 20}px`,
        fontWeight: (componentSetting.bold === false) ? '' : 'bold',
        fontStyle: componentSetting.slant ? 'italic' : '',
        background: tranRenderColor(componentSetting.titleColor),
        textDecoration: componentSetting.underline ? `underLine ${getLastColor(componentSetting.titleColor)}` : '',
        }"
      >
        {{componentSetting.componentName}}
      </div>
    </div>
    <div v-if="componentSetting.codeImg" class="code-img">
      <img :src="componentSetting.codeImg" alt=""/>
    </div>
  </div>
</template>

<script>
import { tranRenderColor, getLastColor } from '@/components/colorPicker/utils/common'
export default {
  name:'CodeCards',
  props:{
    componentSetting: {
      type: Object,
      required: true
    },
    options: {
      type: Object,
      default() {
        return {}
      }
    },
    isLock:{
      type:Boolean,
      default:false
    }
  },
  data() {
    return {
    };
  },
  created() {

  },
  mounted() {
  },
  watch:{
  },
  computed:{
    getComBackColorStyle () {
      return tranRenderColor((this.componentSetting.useBack && this.componentSetting.cardBackColor) ? this.componentSetting.cardBackColor : '#ffffff')
    },
    getComBackImgStyle () {
      let itemSetting = this.componentSetting
      if(itemSetting.useBack && itemSetting.cardBackImg && itemSetting.cardBackImg.isCheckBackImg) {
        let str = ''
        if(itemSetting.cardBackImg.imgShowType == 'original') {
          if(itemSetting.cardBackImg.pos=='left') {
            str += 'justify-content:start;'
          }else if(itemSetting.cardBackImg.pos=='right') {
            str += 'justify-content:end;'
          }
        }
        str += `transform:rotate(${itemSetting.cardBackImg.rotate}deg) `
        if(itemSetting.cardBackImg.horizontalFlip) {
          str += 'scaleX(-1)'
        }
        if(itemSetting.cardBackImg.verticalFlip) {
          str += 'scaleY(-1)'
        }
        str += ';'
        return str
      }else{
        return ''
      }
    },
    getBackImageStyle () {
      let itemSetting = this.componentSetting
      let str = ''
      if(itemSetting.cardBackImg && itemSetting.cardBackImg.imgShowType != 'original') {
        if(itemSetting.cardBackImg.imgShowType == 'auto') {
          str += 'width:-webkit-fill-available;'
        }else{
          str += 'width:100%;height:100%;'
        }
      }else{
        str += 'max-width:100%;height:100%;max-width:max-content:max-height:max-content;'
      }
      return str
    },
  },
  methods: {
    tranRenderColor (color) {
      return tranRenderColor(color)
    },
    getLastColor (color) {
      return getLastColor(color)
    },
  }
};
</script>

<style scoped lang="scss">
.code-card-box{
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
  .code-card-back-img{
    position: absolute;
    left: 0px;
    top: 0px;
    width: 100%;
    height: 100%;
    z-index: 0;
    background-size: contain;
    background-repeat: no-repeat;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .code-card-title{
    position: relative;
    padding: 10px 20px 0;
    z-index: 1;
    .title-text{
      background-clip: text !important;
    -webkit-background-clip:text !important;
    -webkit-text-fill-color: transparent;
    }
  }
  .code-img{
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: space-evenly;
    position: absolute;
    bottom: 0;
    height: 100%;
    left: 62%;
    width: 25%;
    img{
      border-radius: 4px;
      height: 60%;
      max-width: 100%
    }
  }
}
</style>
