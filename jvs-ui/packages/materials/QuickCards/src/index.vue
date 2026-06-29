<template>
  <div class="wrapper"
       :style="{
      borderRadius:componentSetting.boxRadius+'px',
      ...positionCSS
    }">
    <LibraryItem :componentSetting="componentSetting" :options="options" :isLock="isLock" :data="list"></LibraryItem>
  </div>
</template>

<script>
import mapPosition from '../../../positionSelector/mapPosition'
import LibraryItem from '../../LibraryItem/src/index.vue'
import { getByIdLibrary } from '@/api'
export default {
  name:'QuickCards',
  components:{LibraryItem},
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
      list:{}
    };
  },
  computed:{
    positionCSS(){
      return mapPosition(this.componentSetting.position)
    }
  },
  watch:{
    'componentSetting.requestId':{
      handler(newVal,oldVal){
        this.init()
      }
    }
  },
  created() {

  },
  mounted() {
    this.init()
  },
  methods: {
    init(){
      return
      getByIdLibrary({
        ids:this.componentSetting.requestId
      }).then(res=>{
        if(res.data.code==0){
          this.list = res.data.data[0]
        }
      })
    }
  }
};
</script>

<style scoped lang="scss">
.wrapper {
  position: relative;
  width: 100%;
  height: 100%;
}
</style>
