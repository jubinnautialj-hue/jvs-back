<template>
  <div class="wrapper"
    :style="{
      padding:componentSetting.padding+'px',
      ...positionCSS
    }">
    <div class="search-wrapper-box"
      :style="{
        maxWidth: `${componentSetting.maxWidth || 600}px`,
        pointerEvents: isLock ? 'all' : 'none',
      }"
      @contextmenu="contextmenu">
      <div class="search-main-box"
        :style="{
          boxShadow: componentSetting.boxShadow,
          borderRadius: `${componentSetting.boxRadius || 4}px`,
          padding: `0 ${(componentSetting.boxRadius || 4) / 4}px`,
          backdropFilter: componentSetting.backdropBlur ? 'blur(5px)' : 'none',
          height:`${componentSetting.searchHeight}px`
        }">
        <div class="search-input-box-wrapper">
          <el-input class="search-input-box" v-model="searchKey" ref="searchInput" size="small"></el-input>
          <div v-if="searchKey" class="clear-btn" @click="handleClear">
            <Icon name="close" />
          </div>
        </div>
        <div class="search-btn" v-if="componentSetting.btnType==1" :style="{
          color: componentSetting.textColor,
          filter: componentSetting.backdropBlur ? 'drop-shadow(1px 2px 4px #262626)' : 'none'
        }"
          @click="handleSearchBtnClick"
        >
          <Icon name="search"></Icon>
        </div>
        <div class="search-text-btn" v-if="componentSetting.btnType==2" :style="{
          color: componentSetting.btnTextColor || 'rgba(255, 255, 255,1)',
          backgroundColor: componentSetting.btnBackground || 'rgba(31,95,255,1)',
          padding:componentSetting.btnPadding+'px',
          fontSize:componentSetting.btnFontSize+'px',
          borderRadius: `${componentSetting.btnRadius || 4}px`,
          height: `${componentSetting.btnHeight || 32}px`,
        }" @click="handleSearchBtnClick">
          {{ componentSetting.btnContent }}
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import mapPosition from '../../../positionSelector/mapPosition'
export default {
    name:'Search',
    props:{
      componentSetting: {
        type: Object,
        required: true
      },
      element: {
        type: Object,
        required: true
      },
      isLock:{
        type:Boolean,
        default:false
      }
    },
  data() {
    return {
      searchKey:'',
    };
  },
  created() {

  },
  computed:{
    positionCSS(){
      return mapPosition(this.componentSetting.position)
    },
    boxBackground(){
      return this.componentSetting.boxBackground || 'rgba(255,255,255,0.9)'
    },
    inputTextColor(){
      return this.componentSetting.textColor
    }
  },
  mounted() {

  },
  methods: {
    contextmenu (e) {
      if (!this.isLock) {
        e.stopPropagation()
      }
    },
    handleSearchBtnClick(){
      if(this.componentSetting.rememberHistory){
        this.addHistory(this.searchKey)
      }
      let newOpenUrl = window.location.href
      if(this.componentSetting.jumpUrl){
        newOpenUrl = this.componentSetting.jumpUrl
      }
      if(newOpenUrl.indexOf('?')!=-1){
        newOpenUrl += `&searchVal=${this.searchKey}`
      }else{
        newOpenUrl += `?searchVal=${this.searchKey}`
      }
      if(this.componentSetting.jumpType==1){
        window.open(newOpenUrl,'_blank')
      }else{
        window.open(newOpenUrl,'_self')
      }
    },
    addHistory(value){
      const element = JSON.parse(JSON.stringify(this.element))
      const history = element.componentSetting.rememberHistoryList || []
      const index = history.indexOf(value)

      if (!~index) {
        history.unshift(value)
        if (history.length > 10) history.length = 10
        if (this.isAction) {
          element.actionSetting.actionClickValue.componentSetting.rememberHistoryList = history
          // store.updateActionElement(element)
        } else {
          element.componentSetting.rememberHistoryList = history
        }
        this.$eventBus.$emit('editComponent',element)
      }
    },
    handleClear(){
      this.searchKey = ''
    }
  }
};
</script>

<style scoped lang="scss">
.wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
}
.search-wrapper-box {
  width: 100%;
  min-height: 2.4rem;

  transition: all 0.4s cubic-bezier(0.075, 0.82, 0.165, 1);
  position: relative;
  .search-main-box {
    width: 100%;
    height: 100%;
    background: v-bind(boxBackground);
    border: 1px solid #c8c8cc;
    display: flex;
    align-items: center;
  }
  .search-input-box-wrapper {
    flex: 1;
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    position: relative;
    .search-input-box {
      height: 100%;
      /deep/input{
        flex: 1;
        width: 100%;
        height: 100%;
        outline: none;
        border: none;
        background: transparent;
        // padding: 0 20px 0 10px;
        font-size: 1rem;
        font-weight: 500;
        color: #363640;
        vertical-align: middle;
        color: v-bind(inputTextColor);
        &:focus{
          border: none;
          box-shadow: none;
        }
        &::placeholder {
          color: #aab;
          font-size: 12px;
        }
      }
    }
    .clear-btn {
      position: absolute;
      height: 100%;
      right: 0;
      cursor: pointer;
      display: flex;
      width: 28px;
      align-items: center;
      justify-content: center;
      color: #a9a9a9;
    }
  }
  .search-btn {
    width: 2rem;
    height: 2rem;
    margin-right: 5px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: space-around;
    svg path {
      fill: currentColor;
    }
  }
  .search-text-btn{
    display: flex;
    align-items: center;
    cursor: pointer;
  }
}
</style>
