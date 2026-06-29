<template>
  <div class="wrapper document-item-box"
    :style="{
      borderRadius:componentSetting.boxRadius+'px',
      ...positionCSS
    }"
    :class="[isLock && 'document-item-hover']" @click="openURL"  >
    <div class="document-content-box" :style="{
      padding:componentSetting.contentPadding+'px'
    }">
      <img :src="getIconUrl" class="document-img">
      <div class="document-title" :title="options.name">{{ options.name }}</div>
      <div class="document-desc" :title="options.description">{{ options.description }}</div>
    </div>
    <div class="document-button-box">
      <el-button type="text">{{ componentSetting.btnText || '查看全部'}}</el-button>
    </div>
  </div>
</template>

<script>
import mapPosition from '../../../positionSelector/mapPosition'
import { getByIdLibrary } from '@/api'
export default {
  name:'StandardCards',
  props:{
    componentSetting: {
      type: Object,
      required: true
    },
    options: {
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
      dataObj:{}
    };
  },
  created() {

  },
  mounted() {
    this.init()
  },
  watch:{
    'componentSetting.requestId':{
      handler(newVal,oldVal){
        this.init()
      }
    }
  },
  computed:{
    positionCSS(){
      return mapPosition(this.componentSetting.position)
    },
    getIconUrl(){
      let url = ''
      url = this.componentSetting.coverLink
      if(!url){
        url='http://jvsoss.bctools.cn/jvs-public/1/test/dynamic/2023/03/15/2023-03-15820744143398408192-Default.jpg'
      }
      return url;
    }
  },
  methods: {
    init(){
      console.log(this.options)
      return
      getByIdLibrary({
        ids:this.componentSetting.requestId
      }).then(res=>{
        if(res.data.code == 0){
          if(res.data.data.length>0){
            this.dataObj = res.data.data[0]
          }
        }
      })
    },
    openURL(){
      if (this.isLock) {
        window.open(this.options.targetUrl,this.options.jumpMethod)
        // let openUrl = `/#/view?id=${this.componentSetting.requestId}`
        // if(this.componentSetting.btnClickType==1){
        //     window.open(openUrl,'_blank')
        // }else{
        //     window.open(openUrl,'_self')
        // }
      }
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
.document-item-box{
  display: flex;
  justify-content: center;
  flex-direction: column;
  align-items: center;
  overflow: hidden;
  .document-content-box{
    width: 100%;
    height: calc(100% - 40px);
    display: flex;
    justify-content: center;
    flex-direction: column;
    align-items: center;
    .document-img{
      height: 70px;
      width: 70px;
    }
    .document-title{
      margin: 16px 10px 0px;
      font-size: 20px;
      font-weight: 500;
      width: 100%;
      color: #0e1421;
      box-sizing: border-box;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      text-align: center;
    }
    .document-desc{
      margin-top: 12px;
      padding: 0px 10px;
      width: 100%;
      box-sizing: border-box;
      height: 34px;
      line-height: 17px;
      font-size: 12px;
      color: #758296;
      text-align: center;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-box-orient: vertical;
      -webkit-line-clamp: 2;
      word-break: break-all;
    }
  }
  .document-button-box{
    border-top: 1px solid #F4F4F4;
    width: 100%;
    text-align: center;
    height: 40px;
  }  
}
.document-item-hover{
  cursor: pointer;
  &:hover{
    box-shadow: 0 4px 26px #99999929;
  }
}
</style>
