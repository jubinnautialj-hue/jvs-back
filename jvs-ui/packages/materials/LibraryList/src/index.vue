<template>
  <div class="wrappers library-list-box"
    :id="element.i"
    v-resize="resize"
    :style="{
      borderRadius:componentSetting.boxRadius+'px',
      padding:componentSetting.contentPadding+'px',
      ...positionCSS
    }">
    <LibraryItem :componentSetting="componentSetting" :isLock="isLock" v-for="(item,index) in getDataList" :data="item" :key="index" :boxHW="boxHw"></LibraryItem>
    <el-empty v-if="getDataList.length===0" class="list-empty"></el-empty>
  </div>
</template>

<script>
import mapPosition from '../../../positionSelector/mapPosition'
import { getUserLibrary,getByIdLibrary,sendRequestUrl } from '@/api'
import { deCodeKey } from "@/const/const";
import { decryption } from "@/utils/util";
import LibraryItem from '../../LibraryItem/src/index.vue'
export default {
    name:'LibraryList',
    props:{
        element:{
            type: Object,
        },
    componentSetting: {
      type: Object,
      required: true
    },
    isLock:{
      type:Boolean,
      default:false
    }
  },
  components:{LibraryItem},
  data() {
    return {
        boxHw:{width:'100%',height:'100%'},
        dataList:[]
    };
  },
  computed:{
    positionCSS(){
      return mapPosition(this.componentSetting.position)
    },
    gapPadding(){
      return this.componentSetting.contentPadding+'px'
    },
    getDataList(){
        return this.dataList.filter((item,index)=>{return index<this.componentSetting.showNum})
    }
  },
  created() {

  },
  watch:{
    'componentSetting.contentPadding':{
        handler(){
            this.resize()
        }
    }
  },
  // 监听元素变化
  directives: {  // 使用局部注册指令的方式
    resize: { // 指令的名称
      bind(el, binding) { // el为绑定的元素，binding为绑定给指令的对象
        let width = '', height = '';
        function isReize() {
          const style = document.defaultView.getComputedStyle(el);
          if (width !== style.width || height !== style.height) {
            binding.value();  // 关键
          }
          width = style.width;
          height = style.height;
        }
        el.__vueSetInterval__ = setInterval(isReize, 300);
      },
      unbind(el) {
        clearInterval(el.__vueSetInterval__);
      }
    }
  },
  mounted() {
    this.resize()
    this.init()
  },
  methods: {
    async init(){
        switch (this.componentSetting.reqType) {
            case 1:
                getUserLibrary({
                    size:this.componentSetting.showNum
                }).then(res=>{
                    if(res.data.code==0){
                        this.dataList = res.data.data
                    }
                })
                break;
            case 2:
                if(this.componentSetting.reqUrl.indexOf('http://')!=-1 || this.componentSetting.reqUrl.indexOf('https://')!=-1){
                    const res = await fetch(this.componentSetting.reqUrl,{
                        headers:this.$homePageOptions.reqHeaders
                    })
                    const resJson = await res.json()
                    if(resJson.data.code==0){
                        let tp = {
                            data: resJson.data.data
                        }
                        try {
                            let temp = decryption({
                                data: tp,
                                key: deCodeKey,
                                param: ["data"]
                            });
                            resJson.data.data = JSON.parse(temp.data)
                        } catch (error) {
                            
                        }
                        this.dataList = resJson.data.data
                    }
                }else{
                    sendRequestUrl(this.componentSetting.reqUrl).then(res=>{
                        if(res.data.code==0){
                            this.dataList = res.data.data
                        }
                    })
                }
                break;
            default:
                // getByIdLibrary({
                //     ids:this.componentSetting.reqUrl
                // }).then(res=>{
                //     if(res.data.code==0){
                //         this.dataList = res.data.data
                //     }
                // })
                break;
        }
    },
    resize(){
      let viewWidth = document.getElementById(this.element.i).clientWidth - this.componentSetting.contentPadding*2,numNumber = 5
      let domWidth = (viewWidth-this.componentSetting.contentPadding*(numNumber-1))/numNumber      
      while(domWidth<360){
        numNumber--
        domWidth = (viewWidth-this.componentSetting.contentPadding*(numNumber-1))/numNumber
      }
      this.boxHw.width =  parseInt(domWidth) +'px'
      this.boxHw.height = (parseInt(domWidth)/4*3.8<340?340:parseInt(domWidth)/4*3.8) + "px"
    },
  }
};
</script>

<style scoped lang="scss">
.wrappers {
  position: relative;
  width: 100%;
  height: 100%;  
  display: flex;
}
.library-list-box{
    overflow-y: auto;
    box-sizing: border-box;
    grid-gap: v-bind(gapPadding);
    display: flex;
    flex-wrap: wrap;
    align-content: flex-start;
    .list-empty{
        width: 100%;
        height: 100%;
    }
}
</style>
