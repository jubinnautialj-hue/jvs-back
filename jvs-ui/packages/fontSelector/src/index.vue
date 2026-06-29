<template>
  <div class="wrapper">
    <el-select
      :placeholder="$hit('请选择相关字体库')"
      clearable
      filterable
      allow-create
      default-first-option
      v-bind="$attrs"
      v-model="fontFamily"
    >
      <el-option
        v-for="(item,index) in fontList"
        :key="`${item.cn}-${index}`"
        :value="item.en"
        :label="item.cn"
        :style="{ fontFamily: item.en }"
      >
        <span style="float: left; font-weight: bold">{{ item.cn }}</span>
        <span style="float: right; color: #8492a6; font-size: 13px; margin-left: 36px">{{
          item.en
        }}</span>
      </el-option>
    </el-select>
    <div v-if="showRefresh" :class="['icon-refresh', rotate && 'rotate']" @click="refresh">
      <Icon name="refresh" size="1em" />
    </div>
  </div>
</template>

<script>

export default {
    name:'FontSelector',
    props:{
      value:{
        type:[String,Number]
      },
      showRefresh: {
        type: Boolean,
        default: false
      },
      showHarmonyFont: {
        type: Boolean,
        default: false
      }
    },
  data() {
    return {
      rotate:false,
      fontFamily:this.value
    };
  },
  created() {

  },
  mounted() {    
    this.$store.dispatch('updateFontFamilyList')
  },
  watch:{
    fontFamily:{
      handler(newVal,oldVal){
        this.$emit('update:value',newVal,this)
      },
      immediate:true
    }
  },
  computed:{
    fontList(){
      const outerFont = []
      if (this.showHarmonyFont || this.$globalFormData.loadHarmonyOSFont) {
        outerFont.push({
          cn: '鸿蒙OS(外部)',
          en: 'HarmonyOS_Regular'
        })
      }
      return [...outerFont, ...this.$store.state.baseConfig.fontFamilyList]
    }
  },
  methods: {
    refresh(){
      this.rotate = true      
      this.$store.dispatch('updateFontFamilyList')
      setTimeout(() => {
          this.rotate = false
      }, 500)
    }
  }
};
</script>

<style scoped lang="scss">
.wrapper {
  display: inline-flex;
  align-items: center;
  .icon-refresh {
    display: inline-flex;
    align-items: center;
    font-size: 20px;
    color: #aaa2b3;
    cursor: pointer;
    margin-left: 8px;
    &.rotate {
      transform: rotate(360deg);
      transition: transform 0.4s ease-in-out;
    }
  }
}
</style>
