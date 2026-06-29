<template>
  <div class="step-box">
    <div class="step-header"></div>
    <div v-for="(item, key) in options.stepList" :key="key" class="step-item" :style="`width: calc((100% - ${(options.stepList.length - 1) * 10}px) / 4)`">
      <div class="item-title">
        <div>{{ item.stepName }}</div>
        <svg t="1684138193605" class="icon-arrow" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="2539" width="40" height="40"><path d="M429.44 1024L896 512 429.44 0H128l466.56 512L128 1024h301.44z" fill="#fff" p-id="2540"></path></svg>
      </div>
      <div :class="`item-content ${getContentClass(item.stepContentList.length)}`">
        <div
            v-for="(it, index) in item.stepContentList"
            :key="it + index"
            :class="`list-item item-${index} ${it.expandDirection === 'length' ? 'direction-colum' : 'direction-row'}`"
            @click="openURL(it)"
        >
          <img :src="it.image" alt=""/>
          <div style="padding: 0 10px;display: flex;align-items: center;flex-direction: column">
            <p class="list-item-title" :style="it.expandDirection === 'length' ? 'text-align: center' : ''">{{it.title}}</p>
            <p class="list-item-description" :title="it.description">{{it.description}}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name:'StepCard',
  props:{
    componentSetting: {
      type: Object,
      required: true
    },
    options: {
      type: Object,
      required: true
    },
    w: {
      type: Number,
      default() {
        return 0
      }
    },
    h: {
      type: Number,
      default() {
        return 0
      }
    },
    i: {
      type: String,
      default() {
        return ''
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
  },
  methods: {
    getImgStyle() {
      return ''
    },
    // 格式化图片宽度
    getImgW(e, index) {
      let width = 0
      switch (e) {
        case 1:
          width = this.w / 24;
          break;
        case 2:
          width = this.w / 58;
          break;
        case 3:
          width = this.w / 58;
          break;
        case 4:
          width = this.w / 64;
          break;
      }
      if (index !== 2 && e === 3) {
        width = this.w / 32
      }
      return (width * 100) + '%'
    },
    // 描述字体大小
    getDescFontSize() {
      return 12
    },
    openURL(item) {
      if (this.isLock) {
        window.open(item.jumpSettings.targetUrl,item.jumpSettings.jumpMethod)
      }
    },
    getContentClass(type) {
      let str = ''
      switch (type) {
        case 1:
          str = 'one-piece';
          break;
        case 2:
          str = 'two-piece';
          break;
        case 3:
          str = 'three-piece';
          break;
        case 4:
          str = 'multiple-piece';
          break;
      }
      return str
    },
  }
};
</script>

<style scoped lang="scss">
.step-box{
  display: flex;
  grid-column-gap: 10px;
  height: 100%;
  position: relative;
  width: 100%;
  .step-header{
    height: 40px;
    width: 100%;
    position: absolute;
    top: 0;
    left: 0;
    background: linear-gradient(to right, #2e5adb, #6e90f6);
  }
  .step-item{
    width: 100%;
    height: 100%;
    .item-title{
      color: #fff;
      position: relative;
      margin-bottom: 10px;
      height: 40px;
      line-height: 40px;
      text-align: center;
      .icon-arrow{
        position: absolute;
        right: -32px;
        top: 0;
      }
    }
    .item-content{
      display: grid;
      height: calc(100% - 50px);
      width: 100%;
      .list-item{
        position: relative;
        display: flex;
        cursor: pointer;
        border-radius: 4px;
        box-shadow: 0 0 8px #f6f6f6;
        height: 100%;
        width: 100%;
        transition: 0.2s;
        overflow: auto;
        &:hover{
          box-shadow: 0 0 10px #e7e7e7;
          transition: 0.2s;
        }
        img{
          height: calc(100% - 80px);
          max-height: 150px;
        }
        i{
          cursor: pointer;
          position: absolute;
          top: 10px;
          right: 10px;
          color: #1f5fff;
          padding: 5px;
          transition: 0.2s;
          &:hover{
            box-shadow: 0 0 10px #e7e7e7;
            transition: 0.2s;
          }
        }
        .list-item-title{
          font-size: 13px;
          line-height: 24px;
          font-weight: bold;
          //margin-bottom: 10px;
        }
        .list-item-description{
          line-height: 20px;
          font-size: 12px;
          width: 80%;
          text-overflow: ellipsis;
          overflow: hidden;
          display: -webkit-box;
          -webkit-line-clamp: 1;
          -webkit-box-orient: vertical;
          color: #a2a3a5;
        }
      }
      .list-item::-webkit-scrollbar{
        width: 4px;
        height: 4px;
      }
      .list-item::-webkit-scrollbar-thumb{
        border-radius: 20px;
      }
      .list-item:hover::-webkit-scrollbar{
        background: #eee;
      }
      .list-item:hover::-webkit-scrollbar-thumb{
        background: #ccc;
      }
      .direction-colum{
        flex-direction: column;
        align-items: center;
        //justify-content: center;
        justify-content: space-evenly;
      }
      .direction-row{
        align-items: center;
        justify-content: center;
      }
    }
    .one-piece{
      //height: 100%;
      grid-template-rows: 100%;
      .direction-colum{
        img{
          //width: 80%!important;
        }
      }
    }
    .two-piece{
      grid-template-rows: calc(50% - 6px) calc(50% - 6px);
      grid-row-gap: 10px;
    }
    .three-piece{
      grid-template-rows: calc(50% - 6px) calc(50% - 6px);
      grid-template-columns: auto auto;
      grid-column-gap: 10px;
      grid-row-gap: 10px;
      .item-2{
        grid-column-start: 1;
        grid-column-end: 3;
      }
    }
    .multiple-piece{
      grid-template-rows: calc(50% - 6px) calc(50% - 6px);
      grid-template-columns: auto auto;
      grid-column-gap: 10px;
      grid-row-gap: 10px;
    }
    &:last-child{
      svg{
        display: none;
      }
    }
  }
}
</style>
