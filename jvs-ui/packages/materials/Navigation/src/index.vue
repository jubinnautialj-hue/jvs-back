<template>
  <div class="wrapper" :style="{
      backgroundColor:componentSetting.boxBackgroundColor,
      ...positionCSS
    }">
    <div class="navigation-left">
      <div class="title">
        {{ componentSetting.titleTxt }}
      </div>
      <div class="desc">
        {{ componentSetting.descTxt }}
      </div>
      <div v-if="options.navigationType === 'search'" class="search-box">
        <el-input v-model="searchVal" size="small" placeholder="请输入关键词" class="search-input"></el-input>
        <div class="search-button" @click="searchClick" :style="{backgroundColor:componentSetting.boxBackgroundColor}">搜索</div>
      </div>
      <div v-if="options.navigationType === 'jump'">
        <div class="jump-button" @click="jumpClick">立即体验</div>
      </div>
    </div>
    <div  class="navigation-right">
      <img :src="componentSetting.rightImg">
    </div>
  </div>
</template>

<script>
import mapPosition from '../../../positionSelector/mapPosition'
export default {
    name:'Navigation',
    props:{
      componentSetting: {
        type: Object,
        required: true
      },
      options: {
        type: Object,
        required: true,
        default: () => {
          return {}
        }
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
      searchVal:''
    };
  },
  created() {

  },
  computed:{
    positionCSS(){
      return mapPosition(this.componentSetting.position)
    },
  },
  mounted() {
  },
  methods: {
    searchClick(){
      if(this.isLock && this.searchVal){
        // this.$eventBus.$emit('searchClick',this.searchVal)
        window.open(this.options.targetUrl + this.searchVal, this.options.jumpMethod)
      }
    },
    jumpClick() {
      window.open(this.options.targetUrl, this.options.jumpMethod)
    }
  }
};
</script>

<style scoped lang="scss">
.wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  .navigation-left{
    min-width: 570px;
    max-width: 670px;
    .title{
      font-size: 38px;
      font-weight: 700;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      color:#FFF;
    }
    .desc{
      margin-top: 16px;
      line-height: 20px;
      font-size: 14px;
      font-weight: 400;
      color: #FFF;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .search-box{
      width: 520px;
      height: 60px;
      margin-top: 32px;
      display: flex;
      align-items: center;
      background-color: #fff;
      border-radius: 30px;
      position: relative;
      padding: 5px;
      box-sizing: border-box;
      .search-input{
        box-sizing: border-box;
        background: white;
        /deep/input{
          border: none !important;
          box-shadow: none !important;
        }
        
      }
      .search-button{
        right: 0px;
        width: 122px;
        min-width: 122px;
        border-radius: 25px;
        background: #1f5fff;
        text-align: center;
        font-size: 16px;
        font-weight: 700;
        line-height: 50px;
        color: #FFF;
        cursor: pointer;
        &:hover{
          opacity: 0.8;
        }
      }
    }
    .jump-button{
      margin-top: 20px;
      width: 122px;
      min-width: 122px;
      border-radius: 25px;
      background: #FFF;
      text-align: center;
      font-size: 16px;
      font-weight: 700;
      line-height: 50px;
      color: #1f5fff;
      cursor: pointer;
      &:hover{
        opacity: 0.8;
      }
    }
  }
  .navigation-right{
    margin-left: 13px;
    margin-top: 10px;
    width: 550px;
    height: 380px;
    max-height: 100%;
    img{
      height: 100%;
      width: 100%;
      object-fit: contain;
    }
  }
}
@media screen and (max-width:1000px){
  .wrapper{
    flex-wrap: wrap;
    .navigation-left{
      min-width: 100%;
      max-width: 100%;
      text-align: center;
      .title{
        height: 29px;
        line-height: 29px;
        font-size: 20px;
        font-weight: 700;
        margin-top: 32px;
      }
      .search-box{
        text-align: center;
        width: 236px;
        height: 32px;
        overflow: hidden;
        margin: 15px auto 0;
        .search-button{
          right: 0px;
          width: 67px;
          min-width: 67px;
          border-radius: 25px;
          background: #1f5fff;
          text-align: center;
          font-size: 12px;
          font-weight: 700;
          line-height: 28px;
          color: #FFF;
          cursor: pointer;
          &:hover{
            opacity: 0.8;
          }
        }
      }
    }
   .navigation-right{
      width: 231px;
      height: 212px
   } 
  }
}
</style>
