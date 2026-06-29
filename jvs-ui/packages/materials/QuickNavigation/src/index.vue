<template>
  <div id="quickNavigation" style="height: 100%;width: 100%;">
    <div class="card-title">{{options.name}}</div>
    <div :key="renderIndex" class="quick-navigation-box" :style="`grid-template-columns:${gridColumn};`">
      <div class="card-item" :style="`width: ${imgWidth}px;height: ${imgWidth}px;`" v-for="(item, key) in options.itemList" :key="key" @click="openURL(item)">
<!--      <div class="card-item" v-for="(item, key) in options.itemList" :key="key" @click="openURL(item)">-->
        <img :style="`width: ${imgWidth * 0.45}px;height: ${imgWidth * 0.45}px;`" :src="item.image" alt=""/>
        <span style="font-size: 12px">{{item.description}}</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name:'QuickNavigation',
  props:{
    componentSetting: {
      type: Object,
      required: true
    },
    options: {
      type: Object,
      required: true
    },
    h: {
      type: Number,
      default() {
        return 0
      }
    },
    rowHeight: {
      type: Number,
      default() {
        return 0
      }
    },
    w: {
      type: Number,
      default() {
        return 0
      }
    },
    isLock:{
      type:Boolean,
      default:false
    }
  },
  data() {
    return {
      imgWidth: 0,
      rightWidth: 0,
      gridColumn: 'auto auto',
      renderIndex: 0,
    };
  },
  created() {
    window.addEventListener('resize',this.onWindowSizeChange)
  },
  mounted() {
    this.onWindowSizeChange()
  },
  watch:{
    w(val) {
      this.$nextTick(() => {
        this.getRightWidth(val)
      })
    }
  },
  computed:{
    colNum() {
      return this.h
    },
    rowNum() {
      return this.w
    },
  },
  methods: {
    getRightWidth(val) {
      const num1 = window.screen.width / 24 * val - 120
      const num2 = document.getElementsByClassName('card-item')[0].clientWidth + 10
      let multiple = Math.floor(num1 / num2) > document.getElementsByClassName('card-item').length ? document.getElementsByClassName('card-item').length : Math.floor(num1 / num2)
      this.gridColumn = ''
      // console.log(multiple)
      for (let i=0;i<multiple;i++) {
        this.gridColumn += 'auto '
      }
      this.renderIndex++
    },
    openURL(item) {
      if (item.jumpSettings && this.isLock) {
        window.open(item.jumpSettings.targetUrl,item.jumpSettings.jumpMethod)
      }
    },
    onWindowSizeChange() {
      this.imgWidth = Math.round((window.screen.width / 24) + 10)

      this.$nextTick(() => {
        this.getRightWidth(this.w)
      })
    }
  }
};
</script>

<style scoped lang="scss">
.card-title{
  font-size: 16px;
  padding: 10px 20px 0;
  font-weight: bold;
}
.quick-navigation-box{
  position: relative;
  padding: 10px 10px 0;
  height: calc(100% - 40px);
  display: grid;
  text-align: center;
  align-items: center;
  flex-wrap: wrap;
  grid-row-gap: 10px;
  grid-column-gap: 10px;
  overflow: auto;
  &:hover{
    //padding-right: 0;
  }
  .card-item{
    cursor: pointer;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    align-items: center;
    font-size: 12px;
    border-radius: 6px;
    padding: 10px;
    transition: 0.2s;
    box-shadow: 0 0 8px #f0f0f0;
    &:hover{
      box-shadow: 0 0 8px #e0e0e0;
      //box-shadow: 0 0 8px #eeeeee;
      transition: 0.2s;
    }
    span{
      font-size: 14px;
      height: 20px;
      line-height: 20px;
    }
  }
}
.quick-navigation-box::-webkit-scrollbar{
  width: 2px;
  height: 2px;
}
.quick-navigation-box::-webkit-scrollbar-thumb{
  border-radius: 20px;
}
.quick-navigation-box:hover::-webkit-scrollbar{
  background: #eee;
}
.quick-navigation-box:hover::-webkit-scrollbar-thumb{
  background: #ccc;
}
</style>
