<template>
  <div class="standard-color-picker">
    <div v-if="!isMobile" class="color-picker-wrapper">
        <el-color-picker v-model="newColor" :show-alpha="showAlpha" @change="colorChange" size="mini"></el-color-picker>
        <label for="color" v-if="newColor">{{ newColor }}</label>
    </div>
    <el-input v-else v-model="newColor" :placeholder="$hit('colorPlaceholder')"></el-input>
  </div>
</template>

<script>
export default {
    name:'standardColorPicker',
    props:{
      color:{
        type:String,
      },
      showAlpha:{
        type:Boolean,
        default:false
      }
    },
  data() {
    return {
      newColor:'',
    };
  },
  created() {

  },
  mounted() {

  },
  watch:{
    value:{
      handler(){
        this.newColor = this.color
      },
      immediate:true
    }
  },
  computed:{
    isMobile(){
        let flag = navigator.userAgent.match(/(phone|pad|pod|iPhone|iPod|ios|iPad|Android|Mobile|BlackBerry|IEMobile|MQQBrowser|JUC|Fennec|wOSBrowser|BrowserNG|WebOS|Symbian|Windows Phone)/i)
        return flag
    }
  },
  methods: {
    colorChange(){
      this.$emit('update:color',this.newColor,this)
      this.$nextTick(()=>{
        this.$emit("change")
      })
    }
  }
};
</script>

<style scoped lang="scss">
.standard-color-picker{
    .color-picker-wrapper {
        display: flex;
        align-items: center;
        label {
            font-weight: bold;
            padding-left: 10px;
            margin-bottom: 0;
        }
    }
}
</style>
