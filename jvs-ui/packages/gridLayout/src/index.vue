<template>
  <div class="grid-layout-page" :class="[($homePageOptions.isMoBileView || isMobilePage )&& 'grid-layout-page-mobile']">
    <div v-if="windowWidth>0" class="wrapper" style="width: 100%;">
      <grid-layout
        :layout.sync="$homePageOptions.isMoBileView||isMobilePage?layoutMobile:getNewList"
        :col-num="colNum"
        :row-height="rowHeight"
        :is-draggable="isLock"
        :is-resizable="isLock"
        :use-css-transforms="true"
        :margin="[20,20]"
        @layout-updated="handleLayoutListUpdated"
        >
        <grid-item
          v-for="(item,index) in (($homePageOptions.isMoBileView || isMobilePage) ?layoutMobile:getNewList)"
          :key="index"
          :x="item.x"
          :y="item.y"
          :w="item.w"
          :h="item.h"
          :i="item.i"
          :id="item.id"
          :margin="[0,0,0,0]"
          :style="{'z-index':item.zIndex || 1}"
          :class="[(editItemId == item.i) && 'editing']"
          @resized="resizedEvent(item)">
          <div v-if="!item.refresh && item.componentSetting" class="item-content" :class="[isLock && 'show-outline-1']"
            @mousedown.stop="oepnMouseDown($event,'list',item,index)"
            :key="item.refresh"
            :style="{
              boxShadow: item.boxShadow,
              borderRadius: item.borderRadius + 'px',
              userSelect: item.actionSetting && item.actionSetting.actionType === 1 ? 'none' : 'auto',
              cursor: item.actionSetting && item.actionSetting.actionType === 1 ? 'pointer' : 'default',
              background: getComBackColorStyle(item),
              border: getComBorderStyle(item),
              boxShadow: getComShadowStyle(item),
              borderRadius: getComBorderRadiusStyle(item)
            }"
          >
            <div class="garller-back-img-box" :style="getComBackImgStyle(item)">
              <img v-if="item.componentSetting.useBack && item.componentSetting.cardBackImg.isCheckBackImg" :src="item.componentSetting.cardBackImg.backImg" :style="getBackImageStyle(item)">
            </div>
            <div class="gallery-box-title" v-if="!['DataCards', 'ProcessManagement', 'CodeCards', 'Label','InfoCard','StaticCard','CardChart'].includes(item.material) && (item.componentSetting.name || item.componentSetting.titleTxt)">
              <div class="gallery-title-box" :style="{
                fontSize: `${item.componentSetting.titleSize || 20}px`,
                fontWeight: (item.componentSetting.bold === false) ? '' : 'bold',
                fontStyle: item.componentSetting.slant ? 'italic' : '',
                background: tranRenderColor(item.componentSetting.titleColor),
                textDecoration: item.componentSetting.underline ? `underLine ${getLastColor(item.componentSetting.titleColor)}` : '',
              }">
                {{item.componentSetting.name || item.componentSetting.titleTxt}}
              </div>
            </div>
            <div :style="`flex: 1;position: relative;z-index: 1;
              ${item.material == 'Message' ? `${(!['DataCards', 'ProcessManagement', 'CodeCards', 'Label','InfoCard','StaticCard','CardChart'].includes(item.material) && (item.componentSetting.name || item.componentSetting.titleTxt)) ? 'height: calc(100% - 60px);' : 'height: 100%;'}` : 'overflow: hidden;'}`">
              <component
                :is="item.material"
                :ref="item.material + item.i"
                :element="item"
                :w="item.w"
                :h="item.h"
                :i="item.i"
                :rowHeight="rowHeight"
                :isLock="!isLock"
                :componentSetting="item.componentSetting"
                :options="item.options"
                :isUpdate="isUpdate"
                :layWidth="layWidth"
                :reqUrl="reqUrl"
                @openEvent="openEvent"
              ></component>
            </div>
          </div>
          <div class="hover-back" @click="toolClick('edit', 'list', item, index)"></div>
          <div class="item-tool">
            <div class="tool copy" @click="toolClick('copy', 'list', item, index)">
              <svg class="normal-svg-icon" aria-hidden="true">
                <use xlink:href="#icon-jvs-fuzhi"></use>
              </svg>
            </div>
            <div class="tool fresh" @click="toolClick('refresh', 'list', item, index)">
              <i class="el-icon-refresh-right"></i>
            </div>
            <div class="tool del" @click="toolClick('deleteCom', 'list', item, index)">
              <svg class="normal-svg-icon" aria-hidden="true">
                <use xlink:href="#icon-jvs-shanchuyonghu"></use>
              </svg>
            </div>
          </div>
        </grid-item>
      </grid-layout>
    </div>
    <div class="affix-wrapper">
      <div class="affix-item"
        :class="[isLock && item.position=='2' && 'show-outline-2',isLock && item.position=='3' && 'show-outline-3', isToControlFinishedInit && 'finished-init',item.position=='2'&& 'affix-item-abs']"
        @mousedown.stop="oepnMouseDown($event,'affix',item,index)"
        v-for="(item,index) in affix"
        v-to-control="{
          positionMode: item.affixInfo.mode,
          position:item.position,
          parentSelector:`#${ids}`,
          isAbsolute:item.position=='2',
          moveCursor: false,
          disabled: () => !isLock,
          arrowOptions: {
            lineColor: '#9a98c3',
            size: 12,
            padding: 8
          }
        }"
        :key="item.i"
        :id="item.i || undefined"
        :style="{
          width: `${item.w}px`,
          height: `${item.h}px`,
          zIndex: item.zIndex || 2,
          ...computedPosition(item.affixInfo)
        }"
        @todragend="handleAffixDragend($event, item)"
        @tocontrolend="handleAffixDragend($event, item)"
        @todraginit="handleToControlInit">
        <div v-if="!item.refresh" class="affix-item-content"
          :style="{
          boxShadow: item.boxShadow,
          borderRadius: item.borderRadius + 'px',
          userSelect: item.actionSetting && item.actionSetting.actionType === 1 ? 'none' : 'auto',
          cursor: item.actionSetting && item.actionSetting.actionType === 1 ? 'pointer' : 'default'}"
        >
          <div class="bg"
            :style="{
            background: item.background,
            borderRadius: item.borderRadius + 'px',
            filter: item.background.includes('url') && item.backgroundFilter
            }"
          ></div>
          <component
            :is="item.material"
            :isLock="!isLock"
            :element="item"
            :componentSetting="item.componentSetting"
            :options="item.options"
            :isUpdate="isUpdate"
          ></component>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ToControlDirective } from '@/utils/toControl'
import VueGridLayout from 'vue-grid-layout'
import { tranRenderColor, getLastColor } from '@/components/colorPicker/utils/common'
export default {
  name:'gridLayouts',
  props:{
    colNum:{
      type:[Number,String],
      default:24
    },
    // 是否锁定
    isLock:{
      type:Boolean,
      default:true
    },
    list:{
      type:[Array],
      default:()=>{return []}
    },
    affix:{
      type:[Array],
      default:()=>{return []}
    },
    ids:{
      type:String,
      default:'home-page'
    },
    isReadOnly:{
      type:Boolean,
      default:false
    },
    materialConfigCloseRadom: {
      type: Number
    },
    reqUrl: {
      type: String
    }
  },
  components:{'GridLayout':VueGridLayout.GridLayout,'GridItem':VueGridLayout.GridItem},
  data() {
    return {
      isToControlFinishedInit:false,
      timer:null,
      screenMode:0,
      windowWidth:0,
      windowHeight:0,
      fr:0,
      currentVal:{},
      currentIndex:-1,
      currentType:'list',
      menuList:[{
        label: () => `# ${this.currentVal.material}`,
        customClass: 'title'
      },
      {
        label: () => this.$hit('组件编辑'),
        hidden: () =>
          ['StepCard','QuickNavigation','MediaCards'].includes(this.currentVal.material),
          fn: () => {
            this.$emit('edit', this.currentVal)
          },
          icon: { name: 'edit-box', size: 18 }
      },
      {
        label: () => this.$hit('交互配置'),
        hidden: () => !['Empty','', 'Verse', 'CountDown', 'Weather'].includes(this.currentVal.material),
        fn: () => {
          this.$eventBus.$emit('openActionConfig',this.currentVal)
        },
        icon: { name: 'equalizer', size: 18 }
      },
      {
        label: () => this.$hit('刷新组件'),
        fn: async () => {
          this.$eventBus.$emit('changeRefresh',this.currentType,this.currentIndex,true)
          await this.$nextTick()
          this.$eventBus.$emit('changeRefresh',this.currentType,this.currentIndex,false)
        },
        icon: { name: 'refresh', size: 18 }
      },
      {
        label: () => this.$hit('锁定'),
        fn: () => {
          this.$eventBus.$emit('updateIsLock',false)
        },
        icon: { name: 'lock', size: 18 }
      },
      {
        line: true
      },
      {
        label: () => this.$hit('删除'),
        fn: () => {
          this.$eventBus.$emit('deleteCom',this.currentVal)
        },
        icon: { name: 'delete', size: 18 },
        customClass: 'delete'
      }],
      mousePosition:[-200,-200],
      rowHeight:30,
      defaultHeight: 0,
      defaultWidth: 0,
      isUpdate: false,
      layWidth: 0,
      editItemId: '',
    };
  },
  created() {
    this.defaultHeight = window.screen.height
    this.defaultWidth = window.screen.width
    window.addEventListener('resize',this.onWindowSizeChange)
  },
  mounted() {
    this.onWindowSizeChange()
    this.$eventBus.$on('hideAllRightMenu',()=>{
      this.mousePositionVisible = false
    })
  },
  directives: {
    ToControl: ToControlDirective
  },
  computed:{
    getNewList(){
      if(this.isReadOnly){
        let arr = JSON.parse(JSON.stringify(this.list))
        return arr
      }else{
        return this.list
      }
    },
    // 手机端显示时
    layoutMobile () {
      let arr = JSON.parse(JSON.stringify(this.list))
      let y = 0
      arr.sort((a,b)=>{
        return a.x-b.x
      })
      arr.sort((a,b)=>{
        return a.y-b.y
      })
      for (let i = 0; i < arr.length; i++) {
        arr[i].y = y
        arr[i].w = 12
        if(arr[i].h<8 && ['Document','Navigation','Library'].includes(arr[i].material)){
            arr[i].h = 8
        }
        y += arr[i].h
      }
      return arr
    },
    isMobilePage(){
        let flag = navigator.userAgent.match(/(phone|pad|pod|iPhone|iPod|ios|iPad|Android|Mobile|BlackBerry|IEMobile|MQQBrowser|JUC|Fennec|wOSBrowser|BrowserNG|WebOS|Symbian|Windows Phone)/i)
        return !!flag;
    },
  },
  methods: {
    tranRenderColor (color) {
      return tranRenderColor(color)
    },
    getLastColor (color) {
      return getLastColor(color)
    },
    // 组件的单击事件
    oepnMouseDown(e,type,val,index){
      return false
      if(!this.isLock){
        if(e.button ===2){
          let x = e.clientX
          let y = e.clientY
          this.mousePosition = [x, y];
          this.$eventBus.$emit("openGlobalMenuRight",this.mousePosition)
        }else if(e.button === 0){
          this.$eventBus.$emit('hideGlobalRightMenu')
          if (!this.isLock && val.actionSetting && val.actionSetting.actionType === 1) {
            if (val.actionSetting.actionClickType === 1) {
              this.$eventBus.$emit('updateActionElement',val)
              this.$eventBus.$emit('updateActionPopover',val, e)
            } else if (val.actionSetting.actionClickType === 2) {
              const url = val.actionSetting.actionClickValue.url
              window.open(url)
            }
          }
        }
      }else{
        this.currentType = type
        this.currentVal = val
        this.currentIndex = index
        this.$eventBus.$emit('hideGlobalRightMenu')
        if(e.button ===2){
          let x = e.clientX
          let y = e.clientY
          this.mousePosition = [x, y];
          this.$eventBus.$emit("openComMenuRight",this.menuList,this.mousePosition)
        }
      }
    },
    handleLayoutListUpdated(e){
      // console.log(`布局已经改变`,e)'
    },
    resizedEvent(e) {
    },
    // 渲染数据更新
    updateRenderData(value) {
      this.$nextTick(() => {
        if(this.$refs[value.material + value.i][0].updateChart) {
          this.$refs[value.material + value.i][0].updateChart()
        }
      })
      this.isUpdate = !this.isUpdate
    },
    computedPosition({ mode, x, y }){
      const result = {
        top: 'auto',
        left: 'auto',
        bottom: 'auto',
        right: 'auto'
      }
      if ([1, 2].includes(mode)) {
        result.top = y + 'px'
      } else {
        result.bottom = y + 'px'
      }
      if ([1, 3].includes(mode)) {
        result.left = x + 'px'
      } else {
        result.right = x + 'px'
      }
      return result
    },
    // 计算库宽度
    onWindowSizeChange(){
      this.rowHeight = 30 * window.screen.height / this.defaultHeight
      if (this.timer) window.clearTimeout(this.timer)
      this.timer = window.setTimeout(() => {
        this.setValue()
      }, 200)
      if(document.getElementsByClassName('grid-layout-page') && document.getElementsByClassName('grid-layout-page').length > 0) {
        this.layWidth = document.getElementsByClassName('grid-layout-page')[0].clientWidth
      }
    },
    // 设置值
    setValue() {
      this.screenMode = this.getScreenMode()
      const sw = ~~document.body.offsetWidth
      this.windowWidth = sw<this.$homePageOptions.minWidth?this.$homePageOptions.minWidth:sw
      this.windowHeight = window.innerHeight
      this.fr = this.screenMode === 0 ? sw / 12 : this.screenMode === 1 ? sw / 24 : sw / 36
    },
    // 获取屏幕宽度
    getScreenMode(){
      const w = window.innerWidth
      return w <= 721 ? 0 : w <= 1981 ? 1 : 2
    },
    handleToControlInit(){
      if (!this.isToControlFinishedInit) this.isToControlFinishedInit = true
    },
    handleAffixDragend($event, element){
      const mode = element.affixInfo?.mode || 1
      const { left, top, bottom, right, width, height } = $event
      const rectInfo = {
        i: element.i,
        x: [1, 3].includes(mode) ? left : right,
        y: [1, 2].includes(mode) ? top : bottom,
        w: width,
        h: height
      }
      this.$emit('changeItemAffixRectInfo',rectInfo)
    },
    openEvent (data) {
      this.$emit('openEvent', data)
    },
    async toolClick (type, optype, item, index) {
      if(type == 'edit') {
        this.editItemId = item.i
        this.$emit('edit', item)
      }
      if(type == 'deleteCom') {
        this.$eventBus.$emit('deleteCom', item)
      }
      if(type == 'refresh') {
        this.$eventBus.$emit('changeRefresh', optype, index, true)
        await this.$nextTick()
        this.$eventBus.$emit('changeRefresh', optype, index, false)
      }
      if(type == 'copy') {
        this.$eventBus.$emit('copyCom', item)
      }
    },
    getComBackColorStyle (item) {
      let itemSetting = item.componentSetting
      return tranRenderColor((itemSetting.useBack && itemSetting.cardBackColor) ? itemSetting.cardBackColor : '#ffffff')
    },
    getComBorderStyle (item) {
      let itemSetting = item.componentSetting
      if(itemSetting.useBorder) {
        return `${itemSetting.borderWidth}px ${itemSetting.borderStyle} ${itemSetting.borderColor}`
      }
      return ''
    },
    getComShadowStyle (item) {
      let itemSetting = item.componentSetting
      if(itemSetting.useShadow) {
        return `${itemSetting.shadowX}px ${itemSetting.shadowY}px ${itemSetting.shadowFuzzy}px 0px ${tranRenderColor(itemSetting.shadowColor)}`
      }
      return ''
    },
    getComBorderRadiusStyle (item) {
      let itemSetting = item.componentSetting
      if(itemSetting.useBorderRadius) {
        return `${itemSetting.radiusTopLeft}px ${itemSetting.radiusTopRight}px ${itemSetting.radiusBottomRight}px ${itemSetting.radiusBottomLeft}px`
      }
      return '6px'
    },
    getComBackImgStyle (item) {
      let itemSetting = item.componentSetting
      if(itemSetting.useBack && itemSetting.cardBackImg.isCheckBackImg) {
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
    getBackImageStyle (item) {
      let itemSetting = item.componentSetting
      let str = ''
      if(itemSetting.cardBackImg.imgShowType != 'original') {
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
  beforeDestroy(){
    window.removeEventListener('resize',this.onWindowSizeChange)
  },
  watch: {
    materialConfigCloseRadom: {
      handler(newVal, oldVal) {
        if(newVal > -1) {
          this.editItemId = ''
        }
      }
    }
  }
};
</script>

<style scoped lang="scss">
.grid-layout-page{
  overflow: hidden;
  .wrapper {
    zoom: 1;
    box-sizing: border-box;
    &:after {
      content: '';
      clear: both;
      display: table;
      height: 0;
    }
    .item-content {
      width: 100%;
      height: 100%;
      position: relative;
      display: flex;
      flex-direction: column;
      .garller-back-img-box{
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
      .gallery-box-title{
        position: relative;
        z-index: 1;
        font-size: 16px;
        color: #363B4c;
        padding: 24px 24px 6px;
        box-sizing: border-box;
        user-select: none;
        display: flex;
        align-items: center;
        .gallery-title-box{
          background-clip: text !important;
          -webkit-background-clip:text !important;
          -webkit-text-fill-color: transparent;
        }
      }
      .bg {
        position: absolute;
        width: 100%;
        height: 100%;
        left: 0;
        top: 0;
        background-size: cover;
      }
    }
  }
  .affix-wrapper {
    .affix-item {
      position: fixed;
      transition: none !important;
      max-width: 100%;
      opacity: 0;
      .affix-item-content {
        width: 100%;
        height: 100%;
        position: relative;
        .bg {
          position: absolute;
          width: 100%;
          height: 100%;
          left: 0;
          top: 0;
          background-size: cover;
        }
      }
      &.finished-init {
        opacity: 1;
      }
    }
    .affix-item-abs{
      position: absolute;
    }
  }
}
.grid-layout-page-mobile{
  position: absolute;
  height: 100%;
  >.wrapper{
    height: 100%;
    overflow: auto;
  }
  .vue-grid-layout{
    height: 100% !important;
  }
}
.show-outline-1 {
  outline: 2px dashed $color-primary;
  user-select: none;
}
.show-outline-2 {
  outline: 2px dashed $color-danger;
  user-select: none;
  cursor: move;
}
.show-outline-3 {
  outline: 2px dashed $color-warning;
  user-select: none;
  cursor: move;
}
/deep/.vue-grid-item{
  .item-tool{
    display: none;
  }
  .hover-back{
    display: none;
  }
}
/deep/.vue-grid-item.vue-resizable{
  padding: 1px;
  box-sizing: border-box;
  .item-content{
    background: #fff;
    outline: unset;
    box-sizing: border-box;
  }
  .hover-back{
    position: absolute;
    width: 100%;
    height: 100%;
    left: 0;
    top: 0;
    cursor: pointer;
    z-index: 2;
  }
  .item-tool{
    display: none;
    align-items: center;
    position: absolute;
    top: 6px;
    right: 8px;
    z-index: 2;
    .tool{
      margin-left: 8px;
      width: 28px;
      height: 28px;
      background: #FFFFFF;
      box-shadow: 0px 2px 8px 0px rgba(54,59,76,0.2);
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      svg{
        width: 16px;
        height: 16px;
      }
      i{
        font-size: 18px;
        font-weight: bold;
        color: #363B4C;
      }
    }
  }
  .vue-resizable-handle{
    display: none;
  }
  &.editing, &:hover{
    background: #1E6FFF!important;
    .vue-resizable-handle{
      display: block;
    }
    &::before{
      position: absolute;
      left: calc(50% - 8px);
      top: -1px;
      content: '';
      display: block;
      width: 16px;
      height: 3px;
      background: #1E6FFF;
      border-radius: 1px;
      z-index: 1;
    }
    &::after{
      position: absolute;
      left: calc(50% - 8px);
      bottom: -1px;
      content: '';
      display: block;
      width: 16px;
      height: 3px;
      background: #1E6FFF;
      border-radius: 1px;
      z-index: 1;
    }
    .item-content{
      &::before{
        position: absolute;
        top: calc(50% - 8px);
        left: -2px;
        content: '';
        display: block;
        width: 3px;
        height: 16px;
        background: #1E6FFF;
        border-radius: 1px;
        z-index: 1;
      }
      &::after{
        position: absolute;
        top: calc(50% - 8px);
        right: -2px;
        content: '';
        display: block;
        width: 3px;
        height: 16px;
        background: #1E6FFF;
        border-radius: 1px;
        z-index: 1;
      }
    }
  }
  &:hover{
    .hover-back{
      display: block;
    }
    .item-tool{
      display: flex;
    }
  }
}
</style>
<style>
.vue-grid-item > .vue-resizable-handle {
  width: 24px !important;
  height: 24px !important;
  background: url('data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBzdGFuZGFsb25lPSJubyI/PjwhRE9DVFlQRSBzdmcgUFVCTElDICItLy9XM0MvL0RURCBTVkcgMS4xLy9FTiIgImh0dHA6Ly93d3cudzMub3JnL0dyYXBoaWNzL1NWRy8xLjEvRFREL3N2ZzExLmR0ZCI+PHN2ZyB0PSIxNjMxNjA4MTYyMzEwIiBjbGFzcz0iaWNvbiIgdmlld0JveD0iMCAwIDEwMjQgMTAyNCIgdmVyc2lvbj0iMS4xIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHAtaWQ9IjMzNjExIiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgd2lkdGg9IjIwMCIgaGVpZ2h0PSIyMDAiPjxkZWZzPjxzdHlsZSB0eXBlPSJ0ZXh0L2NzcyI+PC9zdHlsZT48L2RlZnM+PHBhdGggZD0iTTI4Ni4xNjUzMzMgNjcwLjE2NTMzM2E0Mi42NjY2NjcgNDIuNjY2NjY3IDAgMSAxLTYwLjMzMDY2Ni02MC4zMzA2NjZsMjU2LTI1NmE0Mi42NjY2NjcgNDIuNjY2NjY3IDAgMCAxIDU5LjAwOC0xLjI4bDI1NiAyMzQuNjY2NjY2YTQyLjY2NjY2NyA0Mi42NjY2NjcgMCAxIDEtNTcuNjg1MzM0IDYyLjg5MDY2N2wtMjI1Ljg3NzMzMy0yMDcuMDYxMzMzLTIyNy4xMTQ2NjcgMjI3LjExNDY2NnoiIGZpbGw9IiM5YTk4YzMiIHAtaWQ9IjMzNjEyIj48L3BhdGg+PC9zdmc+')
    0 0/24px 24px !important;
  padding: 0 !important;
  transform: rotate(135deg) !important;
  z-index: 10;
}
</style>
