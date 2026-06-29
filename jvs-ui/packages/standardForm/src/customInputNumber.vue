<template>
  <el-input-number v-model="getNewVal" :controls-position="controlsPosition" :min="min" :max="max" class="custom-input-number" :style="customStyle" size="mini"/>
</template>

<script>
export default {
    name:'customInputNumber',
    props:{
        value:{
            type:[String,Number]
        },
        controlsPosition:{
            type:[String],
            default:'right'
        },
        min:{
            type:[Number]
        },
        max:{
            type:[Number]
        },
        customStyle:{
            type:String
        },
        pramaKey:{
            type:String
        }
    },
  data() {
    return {
      getNewVal:this.value
    };
  },
  watch:{
    getNewVal:{
      handler(newVal,oldVal){
        this.$emit('changeVal',{
          key:this.pramaKey,
          val:newVal
        },this)
      },
      immediate:true
    }
  },
  created() {

  },
  mounted() {

  },
  computed:{
  },
  methods: {

  }
};
</script>

<style scoped lang="scss">
/deep/.custom-input-number{
  width: 100% ;
}
</style>
