<template>
  <div
    class="wrapper"
    :style="{
      fontSize: componentSetting.textFontSize + 'px',
      color: componentSetting.textColor,
      textShadow: componentSetting.textShadow,
      padding: componentSetting.padding + 'px',
      fontFamily: componentSetting.fontFamily,
      ...positionCSS,
      ...textHollowStyle
    }">
    {{now}}
  </div>
</template>

<script>
import mapPosition from '../../../positionSelector/mapPosition'
export default {
    name:'Clock',
    props: {
        componentSetting: {
            type: Object,
            required: true
        }
  },
  data() {
    return {
        now:null,
        timer:null
    };
  },
  watch:{
    "componentSetting.duration":{
        handler(newVal,oldVal){
            window.clearInterval(this.timer)
            this.init()
        }
    }
  },
  created() {
    this.now = this.getNowTime()
  },
  mounted() {
    this.init()
  },
  computed:{
    positionCSS(){
        return mapPosition(this.componentSetting.position)
    },
    textHollowStyle(){
        return this.componentSetting.textHollow ? {
        '-webkit-text-fill-color': this.componentSetting.textHollowBg,
        '-webkit-text-stroke': `${this.componentSetting.textHollowBorder}px ${this.componentSetting.textColor}`
        } : {}
    }
  },
  methods: {
    getNowTime () {
      const h = new Date().getHours()
      const m = new Date().getMinutes()
      return `${h}:${m < 10 ? '0' + m : m}`
    },
    init(){
        this.now = this.getNowTime()
        this.timer = window.setInterval(() => {
            this.now = this.getNowTime()
        }, this.componentSetting?.duration || 5000)
    }
  },
  beforeUnmount() {
    window.clearInterval(this.timer)
  },
};
</script>

<style scoped lang="scss">
.wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
}
</style>
