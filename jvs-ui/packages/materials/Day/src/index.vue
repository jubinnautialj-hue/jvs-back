<template>
    <div
        class="wrapper"
            :style="{
            fontSize: componentSetting.textFontSize + 'px',
            color: componentSetting.textColor,
            textShadow: componentSetting.textShadow,
            padding: componentSetting.padding + 'px',
            fontFamily: componentSetting.fontFamily,
            ...positionCSS
        }">
        <div class="text-wrapper">
        <div class="day">{{ day }}</div>
        <div class="tts" v-if="componentSetting.showTTS1 && ttsText1" :style="{ fontSize: componentSetting.ttsFontSize + 'px'}">{{ttsText1}}</div>
        <div class="tts" v-if="componentSetting.showTTS2 && ttsText2" :style="{ fontSize: componentSetting.ttsFontSize + 'px'}">{{ttsText2}}</div>
        </div>
    </div>
</template>

<script>
import dayjs from 'dayjs'
import mapPosition from '../../../positionSelector/mapPosition'
export default {
    name: 'Day',
    props: {
        componentSetting: {
            type: Object,
            required: true
        }
    },
  data() {
    return {
        day:null,
        timer:null,
        ttsText1:'',
        ttsText2:''
    };
  },
  created() {

  },
  watch:{
    addWatch:{
        handler(newVal,oldVal){
            window.clearInterval(this.timer)
            this.init()
            this.getTTSText()
        }
    }
  },
  mounted() {
    this.day = this.getDayjsText()
    this.init()
    this.getTTSText()
  },
  computed:{
    addWatch(){
        return {
            duration:this.componentSetting.duration,
            formatter:this.componentSetting.formatter,
            custom:this.componentSetting.custom,
            chineseWeekDay:this.componentSetting.chineseWeekDay,
            showTTS1:this.componentSetting.showTTS1,
            showTTS2:this.componentSetting.showTTS2
        }
    },
    positionCSS(){
        return mapPosition(this.componentSetting.position)
    }
  },
  methods: {
    init(){
        this.day = this.getDayjsText();
        this.timer = window.setInterval(() => {
            this.day = this.getDayjsText()
        }, this.componentSetting?.duration * 1000 || 5000)
    },
    getDayjsText () {
      let text = ''
      let formatterText = this.componentSetting.formatter === '自定义' ? this.componentSetting.custom : this.componentSetting.formatter
      try {
        if (this.componentSetting.chineseWeekDay && formatterText.includes('dddd')) {
          // 强制转换星期为中文
          const chineseWeek = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
          formatterText = formatterText.replace('dddd', () => {
            try {
              return chineseWeek[new Date().getDay()]
            } catch {
              return 'dddd'
            }
          })
        }
        text = dayjs().format(formatterText)
      } catch (e) {
        text = 'Dayjs格式化失败，请检查格式化文本'
        console.error(e)
      }
      return text
    },
    async getTTSText(){
        try {
            if (this.componentSetting.showTTS1) {
                const res = await fetch('https://timor.tech/api/holiday/tts/tomorrow')
                const { tts } = await res.json()
                ttsText1.value = tts
            }
            if (this.componentSetting.showTTS2) {
                const res = await fetch('https://timor.tech/api/holiday/tts/next')
                const { tts } = await res.json()
                ttsText2.value = tts
            }
        } catch {
            //
        }
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
  .text-wrapper {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: space-around;
    height: 100%;
  }
}
</style>
