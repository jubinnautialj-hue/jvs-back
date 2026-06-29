<template>
  <div class="display-color-box">
    <div class="hex-color-box">
      <div :class="['hex-color-input', hexFocus && 'is-focus']">
        <template v-if="colorType == 'hex'">
          <el-input ref="hexInputRef" v-model="hex" @focus="hexFocus=true;" @blur="changeHex" @keyup.enter="hexEnter" maxlength="9"></el-input>
        </template>
        <template v-if="colorType == 'css'">
          <el-input ref="hexInputRef" v-model="rgba" @focus="hexFocus=true;" @blur="changeCss" @keyup.enter="hexEnter" ></el-input>
        </template>
      </div>
      <el-popover
        v-model="hexVisible"
        :show-arrow="false"
        trigger="click"
        placement="bottom"
        popper-class="color-popover">
        <div>
          <div class="popover-item" :class="[item.value == colorType && 'active']" v-for="(item,index) in colorList" :key="index" @click="setValue(item, 'colorType')">
            {{item.label}}
          </div>
        </div>
        <div slot="reference" class="color-type-box">
          <span>{{colorTypeLabel}}</span>
          <svg class="down-icon">
            <use xlink:href="#bi-xiala"></use>
          </svg>
        </div>
      </el-popover>
      
    </div>
    <div class="rgb-color-box">
      <div :class="['rgb-color-input', rgbFocus && 'is-focus']">
        <el-input v-model="R" @focus="rgbFocus=true;" @blur="rgbBlur" @keyup.enter="rgbEnter('r')" maxlength="3"></el-input>
        <el-input v-model="G" @focus="rgbFocus=true;" @blur="rgbBlur" @keyup.enter="rgbEnter('g')" maxlength="3"></el-input>
        <el-input v-model="B" @focus="rgbFocus=true;" @blur="rgbBlur" @keyup.enter="rgbEnter('b')" maxlength="3"></el-input>
        <el-input v-model="A" @focus="rgbFocus=true;" @blur="aBlur" maxlength="4"></el-input>
      </div>
      <el-popover
        v-model="rgbVisible"
        :show-arrow="false"
        trigger="click"
        placement="bottom"
        popper-class="color-popover">
        <div>
          <div class="popover-item" :class="[item.value == rgbType && 'active']" v-for="(item,index) in rgbList" :key="index" @click="setValue(item, 'rgbType')">
            {{item.label}}
          </div>
        </div>
        <div slot="reference" class="down-box">
          <svg class="down-icon">
            <use xlink:href="#bi-xiala"></use>
          </svg>
        </div>
      </el-popover>
    </div>
    <div class="rgb-color-box rgb-tips-box">
      <div class="rgb-color-input">
        <div class="tips-item" v-for="(item,index) in getRgbItem.strArr" :key="index">
          {{item}}
        </div>
      </div>
      <div class="down-box"></div>
    </div>
  </div>
</template>

<script>
import tinycolor from "tinycolor2";
import { cloneDeep } from 'lodash';
export default {
  name: 'display',
  props: {
    color: Object,
    colorTypeProp:{
      type:String,
      default:'hex'
    },
    rgbTypeProp:{
      type:String,
      default:'rgb'
    }
  },
  computed: {
    getRgbItem () {
      return this.rgbList.find(item=>item.value === this.rgbType)
    },
    colorTypeLabel () {
      return this.colorList.find(item=>item.value === this.colorType).label
    }
  },
  data () {
    return {
      colorType: 'hex',
      colorList: [
        { label:"HEX", value:'hex' },
        { label:"CSS", value:'css' }
      ],
      rgbType: 'rgb',
      rgbList: [
        { label:"RGB", value:'rgb', strArr:['R','G','B','A'] },
        { label:"HSL", value:'hsl', strArr:['H','S','L','A'] },
        { label:"HSB", value:'hsb', strArr:['H','S','B','A'] }
      ],
      R: 0,
      G: 0,
      B: 0,
      copyRGB: [],
      A: '100%',
      hex: '',
      rgba: '',
      state: {
        color: this.color,
        hex: this.color ? this.color.hex : '',
        alpha: Math.round(this.color ? this.color.alpha : 100),
        rgba: this.color ? this.color.RGB : '',
        previewBgColor: this.color ? this.color.toRgbString() : '',
      },
      hexVisible: false,
      hexFocus: false,
      rgbVisible: false,
      rgbFocus: false,
    }
  },
  created () {
    this.state = {
      color: this.color,
      hex: this.color ? this.color.hex : '',
      alpha: Math.round(this.color ? this.color.alpha : 100),
      rgba: this.color ? this.color.RGB : '',
      previewBgColor: this.color ? this.color.toRgbString() : '',
    }
    this.colorType = this.colorTypeProp
    this.rgbType = this.rgbTypeProp
    this.setColor()
  },
  methods: {
    setColor(){
      if(this.state.hex){
        this.hex = this.state.hex.startsWith('#') ? this.state.hex : ('#' + this.state.hex)
      }
      this.setRgb()
    },
    setRgb () {
      const newState = cloneDeep(this.state)
      if(newState.rgba) {
        this.copyRGB = newState.rgba
        this.rgba = `${this.rgbType}a(`
        if(this.rgbType == 'rgb') {
          this.R = this.copyRGB[0]
          this.G = this.copyRGB[1]
          this.B = this.copyRGB[2]
          this.rgba += `${newState.rgba[0]},`
          this.rgba += ` ${newState.rgba[1]},`
          this.rgba += ` ${newState.rgba[2]},`
        }else{
          this.R = newState.color.hueValue
          this.rgba += `${newState.color.hueValue},`
          if(this.rgbType == 'hsb') {
            this.G = parseInt(newState.color.saturationValue*100)
            this.B = parseInt(newState.color.brightnessValue*100)
          }
          if(this.rgbType == 'hsl'){
            this.G = parseInt(newState.color.hslSaturationValue*100)
            this.B = parseInt(newState.color.lightnessValue*100)
          }
          this.rgba += ` ${this.G},`
          this.rgba += ` ${this.B},`
        }        
        this.rgba += ` ${newState.alpha}%)`
        this.A = newState.alpha+'%'
      }
    },
    changeHex () {
      if(this.hex != this.state.hex) {
        if(!this.hex) {
          this.setColor()
        }else{
          const _hex = this.hex.replace('#','').replace(/[^a-zA-Z0-9]/g, '')
          if(tinycolor(_hex).isValid()) {
            this.state.color.hex = _hex;
          }else {
            this.state.color.hex = "000000";
          }
          this.state.color.alpha = this.state.alpha
        }
      }
      this.hexFocus = false
    },
    hexEnter () {
      this.$refs.hexInputRef.blur()
    },
    changeCss () {
      const { isPass, valArr } = this.checkColorValue(this.rgba)
      if(isPass) {
        switch(this.rgbType) {
          case 'rgb':
            this.state.color.hex = tinycolor({ r:valArr[0], g:valArr[1], b:valArr[2] }).toHex();
            break;
          case 'hsl':
            this.state.color.hex = tinycolor({ h: valArr[0], s: valArr[1], l: valArr[2] }).toHex();
            break
          case 'hsb':
            this.state.color.hex = tinycolor({ h: valArr[0], s: valArr[1], v: valArr[2] }).toHex();
            break
          default:
            break;
        }
        if(valArr.length == 4) {
          this.state.color.alpha = valArr[3]
        }
      }else {
        this.setColor()
      }
      this.hexFocus = false
    },
    /**
     * 检验输入的值是否符合规则
     * @param {string} value
    */
    checkColorValue (value) {
      let isPass = true,colorRegex
      let valArr = this.getAlpha(value)
      switch(this.rgbType) {
        case 'rgb':
          colorRegex = /^rgb\s*\(\s*(\d{1,3})\s*,\s*(\d{1,3})\s*,\s*(\d{1,3})\s*\)$|^rgba\s*\(\s*(\d{1,3})\s*,\s*(\d{1,3})\s*,\s*(\d{1,3})\s*,\s*((100|[1-9]?\d(?:\.\d{1,2})?)|0(?:\.\d{1,2})?)(%?)\s*\)$/
          isPass = colorRegex.test(value)
          if(isPass){
            valArr.map((item,index)=>{
              if(item>255) item = 255
            })
          }
          break;
        case 'hsl':
          colorRegex = /^hsl\s*\(\s*(\d{1,3}|[1-2]\d{2}|3[0-5]\d|360)\s*,\s*([0-9]{1,2}|100)%\s*,\s*([0-9]{1,2}|100)%\s*\)$|^hsla\s*\(\s*(\d{1,3}|[1-2]\d{2}|3[0-5]\d|360)\s*,\s*([0-9]{1,2}|100)(%?)\s*,\s*([0-9]{1,2}|100)(%?)\s*,\s*((100|[1-9]?\d(?:\.\d{1,2})?)|0(?:\.\d{1,2})?)(%?)\s*\)$/;
          isPass = colorRegex.test(value)
          if(isPass){
            valArr.map((item,index)=>{
              if(item>360 && index ==0) item = 360
              if(item>100 && index !=0 ) item = 100
            })
          }
          break
        case 'hsb':
          colorRegex = /^hsb\s*\(\s*(\d{1,3}|[1-2]\d{2}|3[0-5]\d|360)\s*,\s*([0-9]{1,2}|100)%\s*,\s*([0-9]{1,2}|100)%\s*\)$|^hsba\s*\(\s*(\d{1,3}|[1-2]\d{2}|3[0-5]\d|360)\s*,\s*([0-9]{1,2}|100)(%?)\s*,\s*([0-9]{1,2}|100)(%?)\s*,\s*((100|[1-9]?\d(?:\.\d{1,2})?)|0(?:\.\d{1,2})?)(%?)\s*\)$/;
          isPass = colorRegex.test(value)
          if(isPass){
            valArr.map((item,index)=>{
              if(item>360 && index ==0) item = 360
              if(item>100 && index !=0 ) item = 100
            })
          }
          break
        default:
          break;
      }
      return { isPass,valArr }
    },
    /**
     * 获取css模式下面的括号里的颜色值
     * @param {string} value
    */
    getAlpha (value) {
      // 取出括号中间的值
      const match = value.match(/\(([^)]+)\)/);
      if (match) {
        const valuesStr = match[1];
        const valuesArr = valuesStr.split(',').map((value,index) => {
          if(index == (valuesStr.split(',').length-1)) {
            let valueTrim = value.trim().replace('%', '')
            return parseFloat(valueTrim) * (value.includes('%') ? 1: 100)
          }else{
            return value.trim()
          }
        })
        return valuesArr.map(Number);
      }
      return []
    },
    setValue (item, type) {
      if(type == 'colorType') {
        this.colorType = item.value
        this.$emit('change', 'colorType', item.value)
      }else{
        this.rgbType = item.value
        this.setColor()
        this.$emit('change', 'rgbType', item.value)
      }
      this.hexVisible = false
      this.rgbVisible = false
    },
    rgbBlur () {
      const reg = /^(?:0|(?:[1-9]\d*))$/
      if(reg.test(this.R) && reg.test(this.G) && reg.test(this.B)) {
        if(this.rgbType == 'rgb') {
          if(this.R > 255) {
            this.R = 255
          }
          if(this.G > 255) {
            this.G = 255
          }
          if(this.B > 255) {
            this.B = 255
          }
          this.state.color.hex = tinycolor({ r: this.R, g: this.G, b: this.B }).toHex()
        }else{
          if(this.R > 360) {
            this.R = 360
          }
          if(this.G > 100) {
            this.G = 100
          }
          if(this.B > 100) {
            this.B = 100
          }
          this.state.color.hue =  this.R
          if(this.rgbType == 'hsl'){
            this.state.color.hslSaturation = this.G / 100
            this.state.color.lightness = this.B / 100
          }else{
            this.state.color.saturation = this.G / 100
            this.state.color.brightness = this.B / 100
          }
        }
        this.state.color.alpha = this.state.alpha
      }else{
        this.setColor()
      }
      this.rgbFocus = false
    },
    rgbEnter (type) {
      this.rgbBlur()
    },
    aBlur () {
      if(!this.A.endsWith('%')) {
        this.A = this.A.replaceAll('%','') + '%'
      }
      const newA = this.A.replaceAll('%','').replace(/[^0-9]+/g, '')
      if(newA.length == 0) {
        this.state.alpha = 0
        newA = '0%'
      }else{
        if(newA != this.state.alpha){
          this.state.color.alpha = parseInt(newA)
          this.state.alpha = parseInt(newA)
        }
      }
      this.rgbFocus = false
    },
  },
  watch: {
    color: {
      handler(value) {
        if(value) {
          this.state.color = value
          this.state.alpha = Math.round(this.state.color.alpha)
          this.state.hex = this.state.color.hex
          this.state.rgba = this.state.color.RGB
        }
      },
      deep:  true
    },
    colorTypeProp: {
      handler(newV) {
        this.colorType = newV
      }
    },
    rgbTypeProp: {
      handler(newV) {
        this.rgbType = newV
      }
    },
    'state.color': {
      handler(newV, oldV) {
        if(newV) {
          this.state.previewBgColor = this.state.color.toRgbString()
          this.setColor()
        }
      },
      deep: true
    }
  }
}
</script>
<style lang="scss" scoped>
.display-color-box{
  margin-top: 18px;
  .hex-color-box{
    height: 36px;
    margin-bottom: 8px;
    display: grid;
    grid-template-columns: 192px 88px;
    grid-column-gap: 8px;
    .hex-color-input{
      display: flex;
      align-items: center;
      height: 36px;
      background: #F5F6F7;
      border-radius: 4px 4px 4px 4px;
      /deep/.el-input{
        .el-input__inner{
          background-color: transparent;
          box-shadow: none;
          border: 0;
        }
      }
      &.is-focus{
        box-shadow: 0 0 0 1px #1E6FFF !important;
      }
    }
    .color-type-box{
      height: 36px;
      background: #F5F6F7;
      border-radius: 4px 4px 4px 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      span{
        font-size: 14px;
        color: #363b4c;
      }
      .down-icon{
        width: 16px;
        min-width: 16px;
        height: 16px;
        margin-left: 16px;
      }
    }
  }
  .rgb-color-box{
    height: 36px;
    background: #F5F6F7;
    border-radius: 4px 4px 4px 4px;
    display: flex;
    align-items: center;
    padding: 2px 4px;
    box-sizing: border-box;
    &:has(.is-focus){
      box-shadow: 0 0 0 1px #1E6FFF !important;
    }
    .rgb-color-input{
      display: grid;
      width: 100%;
      grid-template-columns: repeat(4,1fr);
      /deep/.el-input{
        .el-input__inner{
          background-color: transparent;
          box-shadow: none;
          border: 0;
          padding: 0;
          text-align: center;
        }
      }
    }
    .down-box{
      width: 32px;
      min-width: 32px;
      height: 32px;
      border-radius: 4px 4px 4px 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      .down-icon{
        width: 16px;
        min-width: 16px;
        height: 16px;
      }
      &:hover{
        background: #EEEFF0;
      }
    }
  }
  .rgb-tips-box{
    padding: 2px 4px;
    box-sizing: border-box;
    background: transparent;
    height: 24px;
    .down-box{
      width: 32px;
      min-width: 32px;
      cursor: default;
      &:hover{
        background: transparent;
      }
    }
    .rgb-color-input{
      .tips-item{
        color: #6f7588;
        font-size: 14px;
        padding-left: 11px;
      }
    }
  }
}
</style>
<style lang="scss">
.color-popover{
  min-width: 88px !important;
  width: 88px !important;
  padding: 8px !important;
  box-sizing: border-box;
  .popover-item{
    height: 32px;
    line-height: 32px;
    padding: 0px 8px;
    box-sizing: border-box;
    border-radius: 4px 4px 4px 4px;
    cursor: pointer;
    font-size: 14px;
    color: #363b4c;
    &:hover{
      background-color: #eeeff0;
    }
  }
  .active{
    background: #E4EDFF;
    color: #1E6FFF;
    &:hover{
      background-color: #E4EDFF;
    }
  }
  .popover-item+.popover-item{
    margin-top: 4px;
  }
}
.color-popover[data-popper-placement^=bottom]{
  transform:translateY(-12px);
}
.color-popover[data-popper-placement^=top]{
  transform:translateY(12px);
}
.color-popover[data-popper-placement^=left]{
  transform:translateX(12px);
}
.color-popover[data-popper-placement^=right]{
  transform:translateX(-12px);
}
</style>