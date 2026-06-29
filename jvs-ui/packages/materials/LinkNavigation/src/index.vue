<template>
  <div class="link-navgation">
    <div class="link-list-box">
      <div class="link-list-item" v-for="(lit, lix) in linkList" :key="lit.name+'-item-'+lix" @click="enterLink(lit)" :style="getStyle(lix, linkList.length)">
        <div>
          <img v-if="typeof lit.icon == 'string'" :src="lit.icon" />
          <div v-else>
            <i class="select-iconfont" v-if="lit.icon && lit.icon.type != 'custom' && lit.icon.value" :class="lit.icon.value" :style="{background: getIconBackColor(lit)}"></i>
            <div class="select-icon-img" v-if="!(lit.icon && lit.icon.type == 'material')" :style="getIconImgStyle(lit)"></div>
          </div>
          <span>{{lit.name}}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { tranRenderColor } from '@/components/colorPicker/utils/common'
export default {
  name: 'LinkNavigation',
  props: {
    w: Number,
    layWidth: Number,
    element: {
      type: Object
    },
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
    isLock:{
      type:Boolean,
      default:false
    }
  },
  data () {
    return {
      linkList: []
    }
  },
  created () {
    if(this.componentSetting.linkList) {
      this.linkList = this.componentSetting.linkList
    }
  },
  methods: {
    enterLink (item) {
      this.$openUrl(`${item.url}`, '_blank')
    },
    getStyle (index, length) {
      let colMargin0 = 20 // 外部列间距
      let colNum = 24 // 外部行的列数
      let colWidth = (this.layWidth - (colMargin0 * (colNum + 1))) / colNum // 外部每列的宽
      
      let str = 'box-sizing: border-box;'
      let minWidth = 74 // 最小宽
      let maxWidth = 128 // 最大宽
      let baseMagin = 20 // 固定间距
      let useCount = 0 // 每行个数

      let totalW =  Math.round(colWidth * this.w + Math.max(0, this.w - 1) * colMargin0)
      if(!this.isLock) {
        totalW = totalW -2 // 2：外部列的padding1
      }
      totalW = totalW - 48 //  48：该组件的padding24
      let minwNumber = Math.floor(totalW / minWidth)
      let maxwNumber = Math.floor(totalW / maxWidth)
      if((totalW % minWidth === 0)) {
        minwNumber -= 1
      }
      if(totalW % maxWidth === 0) {
        maxwNumber -= 1
      }
      // 判断最小宽+间距总和
      if(((Math.min(minwNumber, length) - 1) * baseMagin + Math.min(minwNumber, length) * minWidth) > totalW) {
        minwNumber -= 1
        while(minwNumber == 0) {
          if(!(((Math.min(minwNumber, length) - 1) * baseMagin + Math.min(minwNumber, length) * minWidth) > totalW)) {
            break;
          }
          minwNumber -= 1
        }
        if(!(minwNumber > 0)) {
          minwNumber = 0
        }
      }
      // 判断最大宽+间距总和
      if(((Math.min(maxwNumber, length) - 1) * baseMagin + Math.min(maxwNumber, length) * maxWidth) > totalW) {
        maxwNumber -= 1
        while(maxwNumber == 0) {
          if(!(((Math.min(maxwNumber, length) - 1) * baseMagin + Math.min(maxwNumber, length) * maxWidth) > totalW)) {
            break;
          }
          maxwNumber -= 1
        }
        if(!(maxwNumber > 0)) {
          maxwNumber = 0
        }
      }
      useCount = Math.max(minwNumber, maxwNumber)
      // console.log(minwNumber, maxwNumber, useCount, totalW, this.w)
      if(useCount > 0) {
        let itemWidth = Number.parseInt((totalW - ((useCount - 1) * baseMagin)) / useCount)
        str += `width: ${itemWidth}px;`
        if((Number(index)+1) % useCount === 0) {
          str += `margin-right: 0;`
        }
      }
      return str
    },
    getIconBackColor (item) {
      return tranRenderColor(item.iconColor)
    },
    getIconImgStyle (item) {
      let styleStr = ''
      styleStr += `background-image: url(${(item.icon && item.icon.backImg) ? item.icon.backImg : this.componentSetting.cardImg});`
      if(item.icon) {
        if(item.icon.pos) {
          let posArr =  item.icon.pos.split('-')
          if(posArr.length == 1) {
            styleStr += 'background-position:'+posArr[0]+' center;'
          }else{
            styleStr += 'background-position:'+posArr[0]+' '+posArr[1]+';'
          }
        }else{
          styleStr += 'background-position: center;'
        }
        styleStr += `transform:rotate(${item.icon.rotate}deg) `
        if(item.icon.horizontalFlip) {
          styleStr += 'scaleY(-1)'
        }
        if(item.icon.verticalFlip) {
          styleStr += 'scaleX(-1)'
        }
        styleStr += ';'
      }else{
        styleStr += 'background-position: center;'
      }
      return styleStr
    }
  },
  watch: {
    componentSetting: {
      handler (newVal, oldVal) {
        this.linkList = []
        if(this.componentSetting.linkList) {
          this.linkList = this.componentSetting.linkList
        }
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.link-navgation{
  width: 100%;
  height: 100%;
  padding: 16px 24px;
  box-sizing: border-box;
  overflow: hidden;
  .link-list-box{
    height: 100%;
    box-sizing: border-box;
    display: flex;
    flex-wrap: wrap;
    overflow: hidden;
    overflow-y: auto;
    &::-webkit-scrollbar{
      display: none;
    }
    .link-list-item{
      width: 64px;
      height: 64px;
      margin-right: 20px;
      margin-bottom: 20px;
      border-radius: 6px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-direction: column;
      cursor: pointer;
      >div{
        max-width: 100%;
        display: flex;
        flex-direction: column;
        align-items: center;
        border-radius: 4px;
        box-sizing: border-box;
        cursor: pointer;
        overflow: hidden;
        text-align: center;
        img{
          width: 32px;
          height: 32px;
          display: inline-block;
        }
        .select-iconfont{
          font-size: 36px;
          -webkit-background-clip: text !important;
          -webkit-text-fill-color: transparent !important;
        }
        .select-icon-img{
          width: 36px;
          height: 36px;
          background-repeat: no-repeat;
          background-size: 36px 36px;
        }
        span{
          width: 100%;
          margin-top: 4px;
          font-size: 14px;
          font-family: Source Han Sans-Regular, Source Han Sans;
          color: #363b4c;
          max-width: 100%;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
      &:hover{
        background-color: #eeeff0;
      }
    }
    .link-list-item:hover{
      background: #f5f7fa;
    }
  }
}
</style>