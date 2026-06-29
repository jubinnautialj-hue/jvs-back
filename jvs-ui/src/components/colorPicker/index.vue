<!-- 使用这个函数后需要配套使用 tranRenderColor进行颜色选择 -->
<template>
  <el-popover
    ref="popoverRef"
    v-model="colorPickerVisible"
    :show-arrow="false"
    trigger="click"
    width="338"
    :placement="popoverPos"
    popper-class="color-picker-popover-box"
  >
    <div class="color-picker-back" @click.stop="outClick"></div>
    <div v-if="colorPickerVisible" class="color-picker-popover" @click.stop="stopHandle">
      <div class="jvs-color-picker">
        <div v-if="openGradual" class="color-type-box">
          <div class="color-type-mark" :style="getMarkStyle"></div>
          <div class="color-type-item" :class="[colorType == 'pure' && 'color-type-active']" @click="changeColorType('pure')">纯色</div>
          <div class="color-type-item" :class="[colorType == 'gradual' && 'color-type-active']" @click="changeColorType('gradual')">渐变</div>
        </div>
        <!-- 渐变色才有的配置项 -->
        <div class="gradual-color-box" v-if="colorType == 'gradual'">
          <div class="gradual-silder-box">
            <div
              class="gradual-silder-bar"
              ref="graSilgerBarRef"
              :style="{ background: getGtadualStyle }"
              @click.stop="addGradualPoint"
            >
              <div
                class="gradual-color-node"
                v-for="(item, index) in gradualRenderColorList"
                :class="[item.id == currentGradualColor.id && 'color-active']"
                :key="index"
                :id="`gradual-node-${index}`"
                :style="{ left: item.left == 100 ? `calc(${item.left}% - 14px)` : `${item.left}%`, }"
                @click.stop="setCurrentItem(item)"
                @mousedown.stop="topResize($event, item, index)"
              ></div>
            </div>
          </div>
          <div class="gradual-icon-box" @click="reverseArr">
            <svg class="gradual-icon">
              <use xlink:href="#bi-shuipingfanzhuan"></use>
            </svg>
          </div>
          <div class="gradual-icon-box" @click="addRatio">
            <svg class="gradual-icon">
              <use xlink:href="#bi-xuanzhuan"></use>
            </svg>
          </div>
        </div>
        <board ref="colorBoardRef" :colorProp="state ? state.color : new Color()" :colorType="colorType" @change="onBoardChange"></board>
        <div class="color-silder-box">
          <div v-if="isSupported" class="color-zilla" @click="openPicker">
            <svg  class="color-zilla-icon">
              <use xlink:href="#bi-xiguan"></use>
            </svg>
          </div>
          <div class="siler-box">
            <hue :color="state ? state.color : new Color()" @change="hueChange"></hue>
            <alpha :colorProp="state ? state.color : new Color()" @change="alphaChange"></alpha>
          </div>
          <div class="silder-color-box">
            <div class="color-box-bg"></div>
            <div class="color-box" :style="getBgColorStyle"></div>
          </div>
        </div>
        <display ref="displayRef" :color="state ? state.color : new Color()" :colorTypeProp="showColorType" :rgbTypeProp="rgbType" @change="typeChange"></display>
      </div>
      <div class="common-use-box">
        <div class="common-title">通用</div>
        <div class="color-list-box">
          <div
            class="color-item"
            v-for="(item, index) in getCurrentColorList"
            :key="index"
            @click="setColor(item)"
            :style="setColorBackGround(item)"
          ></div>
          <template v-if="colorType=='pure'">
            <div class="color-item back-trans" @click="setAlpha"></div>
            <div class="color-item" @click="resetColorFun">
              <svg class="icon">
                <use xlink:href="#bi-huifu"></use>
              </svg>
            </div>
          </template>
        </div>
      </div>
    </div>
    <div
      slot="reference"
      class="jvs-color-picker-show-box"
      ref="colorButtonRef"
      :class="[theme == 'light' && 'light-color-picker-show-box', , theme=='dark' && 'dark-color-picker-show-box']"
      :style="{ width: width }"
    >
    <div class="show-color-box-wrap">
      <div class="show-color-box-bg"></div>
      <div class="show-color-box" :style="{background: getShowColorBg}"></div>
      <div class="show-color-hex">{{getShowHex}}</div>
    </div>
    <div class="close-box" v-if="getShowHex && getShowHex != resetColor" @click.stop="resetColorFun">
      <svg class="close-icon"> 
        <use xlink:href="#jvs-public-danchuangguanbi"></use>
      </svg>
    </div>
  </div>  
  </el-popover>
</template>

<script>
import board from "./board.vue";
import hue from "./hue.vue";
import alpha from "./alpha.vue";
import display from "./display.vue";
import { Color } from "./utils/color";
import tinycolor from "tinycolor2";
import { colorList, gradualColorList } from "./utils/commonColor";
import { cloneDeep } from "lodash";
import { generateUUID } from "./utils/common";
import {
  transColor,
  getAlpha,
  getHexColorAlpha,
  decToHex,
  analysGradual,
  byColorTypeAndTypeTransColor,
} from "./utils/common";
export default {
  name: "jvsColorPicker",
  components: { board, hue, alpha, display },
  props: {
    modelValue: {
      type: [String, Object],
      // default: "#000000",
    },
    prop: String,
    resetColor:{
      type:String,
      default:"#000000"
    },
    theme: {
      type: String,
      default: "",
    },
    size: {
      type: String,
      default: "default",
    },
    width: {
      type: String,
      default: "130px",
    },
    openGradual: {
      type: Boolean,
      default: true
    },
    form: Object,
    allowEmpty: {
      type: Boolean,
      default: false
    }
  },
  data () {
    return {
      Color: Color,
      colorPickerVisible: false,
      colorPickerVisibleShow: false,
      state: {
        color: null,
        hex: null,
        rgb: null,
      },
      colorType: 'pure',
      markStyle: {
        width: "132px",
        height: "28px",
      },
      colorInstance: null,
      oldColor: null,
      showColorType: 'hex',
      rgbType: 'rgb',
      oldAlpha: 100,
      isCLickSet: false,
      currentGradualColor: {},
      gradualRenderColorList: [],
      gradualRatio: 0,
      gradualDrag: false,
      popoverPos: 'bottom-end',
      isSupported: 'EyeDropper' in window,
      open: true,
      sRGBHex: '',
    }
  },
  computed: {
    modelValueCompute () {
      return this.modelValue ? this.modelValue : '#000000'
    },
    getShowColorBg () {
      let str = ""
      if(this.colorType == "pure" && this.modelValueCompute.startsWith("#")) {
        let getHexObj = getHexColorAlpha(this.modelValueCompute);
        str = transColor(getHexObj.color, getHexObj.alpha).renderColor
      }else if(this.modelValueCompute.startsWith("rgb")) {
        str = this.modelValueCompute
      }else if(this.modelValueCompute) {
        let arr = analysGradual(this.modelValueCompute)
        str = this.getGtadualColorStr(arr)
      }
      if(this.allowEmpty && !this.modelValue) {
        str = ''
      }
      return str
    },
    getShowHex () {
      let str = ''
      if(this.colorType != "pure") {
        str = "渐变色"
      } else {
        str = tinycolor(transColor(this.modelValueCompute, 100).renderColor).toHexString()
      }
      if(this.allowEmpty && !this.modelValue) {
        str = ''
      }
      return str
    },
    getMarkStyle () {
      let obj = {
        top: "4px",
        left: "8px",
      }
      if(this.colorType != "pure") {
        obj.left = parseInt(this.markStyle.width) + 16 + "px"
      }
      return {
        ...this.markStyle,
        ...obj,
      }
    },
    getBgColorStyle () {
      return {
        background: this.state.rgb
      }
    },
    getCurrentColorList () {
      if(this.colorType == "pure") {
        return colorList;
      }else{
        return gradualColorList;
      }
    },
    getGtadualColor () {
      return this.getGtadualColorStr(this.gradualRenderColorList)
    },
    getGtadualStyle () {
      let strColor = "linear-gradient(to right, ";
      let list = JSON.parse(JSON.stringify(this.gradualRenderColorList))
      list.sort((a, b) => {
        return a.left - b.left
      })
      list.map((item, index) => {
        if (index != 0) {
          strColor += ","
        }
        strColor += item.renderColor + " " + item.left + "%"
      });
      strColor += ")"
      return strColor
    },
  },
  created () {
    this.oldColor = this.modelValueCompute + "";
    let copyModelVal = this.modelValueCompute + "";
    this.gradualRenderColorList = [];
    if(!copyModelVal.startsWith("linear-gradient(")) {
      this.colorType = "pure";
      const colorCssArr = getAlpha(this.oldColor);
      if(this.oldColor.startsWith("#")) {
        const getHexObj = getHexColorAlpha(this.oldColor);
        copyModelVal = getHexObj.color;
        this.oldAlpha = getHexObj.alpha;
        this.showColorType = "hex";
        this.rgbType = "rgb";
      } else {
        this.showColorType = "css";
        this.rgbType = this.oldColor.substr(0, 3);
      }
      if(this.rgbType == "hsb" && colorCssArr.length != 0) {
        copyModelVal = tinycolor({
          h: colorCssArr[0],
          s: colorCssArr[1],
          v: colorCssArr[2],
        }).toHexString();
      }
      this.colorInstance = new Color(copyModelVal);
      this.state = {
        color: this.colorInstance,
        hex: this.colorInstance.toHexString(),
        rgb: this.colorInstance.toRgbString(),
      };
      if(this.showColorType == "css") {
        this.oldAlpha = colorCssArr[3] || 100;
      }
      this.state.color.alpha = this.oldAlpha;
    }else {
      this.colorType = "gradual";
      const directionRegex = /linear-gradient\(([^,]+)/;
      const directionMatch = directionRegex.exec(copyModelVal);
      const direction = parseInt(directionMatch ? directionMatch[1].trim() : "0");
      this.gradualRatio = direction;
      this.gradualRenderColorList = analysGradual(copyModelVal);
      this.currentGradualColor = this.gradualRenderColorList[0];
      if(this.currentGradualColor.color.startsWith("#")) {
        this.showColorType = "hex";
        this.rgbType = "rgb";
      }else {
        this.showColorType = "css";
        this.rgbType = this.currentGradualColor.color.substr(0, 3);
      }
      this.resetStateColor(this.currentGradualColor);
    }
    this.isCLickSet = true
  },
  mounted () {
    window.addEventListener("keydown", this.handleEvent);
    window.addEventListener("scroll", this.handleScroll, true);
  },
  unmounted () {
    window.removeEventListener("keydown", this.handleEvent);
    window.removeEventListener("scroll", this.handleScroll, true);
  },
  methods: {
    handleScroll () {
      this.colorPickerVisible = false
    },
    getGtadualColorStr (arr) {
      let strColor = `linear-gradient(${this.gradualRatio}deg, `
      arr.sort((a, b) => {
        return a.left - b.left;
      }).map((item, index) => {
        if(index != 0) {
          strColor += ","
        }
        if(item.color.startsWith("#")) {
          if (item.alpha == 100) {
            strColor += item.color
          } else {
            strColor += tinycolor(item.color).toHexString() + decToHex(item.alpha)
          }
        } else {
          strColor += item.color
        }
        strColor += " " + item.left + "%"
        });
      strColor += ")"
      return strColor
    },
    handleEvent (event) {
      if(event.keyCode == 46) {
        if(this.gradualRenderColorList.length > 2) {
          let index = this.gradualRenderColorList.findIndex((item) => item.id == this.currentGradualColor.id)
          if(index == this.gradualRenderColorList.length - 1) {
            this.currentGradualColor = this.gradualRenderColorList[index - 1];
          }else {
            this.currentGradualColor = this.gradualRenderColorList[index + 1];
          }
          this.gradualRenderColorList.splice(index, 1);
        }
      }
    },
    setColorBackGround (item) {
      if(item.type == "base") {
        return "background:" + item.color
      }else {
        return `background:linear-gradient(180deg,${item.color[0]}, ${item.color[1]})`
      }
    },
    changeColorType (val) {
      window.tinycolor = tinycolor
      if(this.colorType != val) {
        this.colorType = val
        if(val == "gradual") {
          let color = this.modelValueCompute
          let { renderColor, renderColor1 } = transColor(this.modelValueCompute, this.oldAlpha)
          let firstId = generateUUID()
          let secondColor = transColor(color, 0).renderColor1
          this.gradualRenderColorList = [
            {
              id: firstId,
              color: color,
              renderColor: renderColor,
              left: 0,
              alpha: this.oldAlpha,
            },
            {
              id: generateUUID(),
              color: secondColor,
              renderColor: renderColor1,
              left: 100,
              alpha: 0,
            },
          ];
          this.currentGradualColor = {
            id: firstId,
            color: color,
            renderColor: renderColor,
            left: 0,
            alpha: this.oldAlpha,
          }
          this.resetStateColor(this.currentGradualColor)
          // 这里也需要更新颜色值
          this.$emit("update:modelValue", this.getGtadualColor, this.prop, this.form)
        }else {
          let updateColorStr = this.gradualRenderColorList[0].color
          this.oldAlpha = this.gradualRenderColorList[0].alpha
          this.state.color.alpha = this.oldAlpha
          this.$emit("update:modelValue", updateColorStr, this.prop, this.form)
        }
      }
    },
    reverseArr () {
      let copyRnederList = cloneDeep(this.gradualRenderColorList);
      copyRnederList = copyRnederList.reverse();
      copyRnederList.map((item, index) => {
        item.left = this.gradualRenderColorList[index].left;
      });
      this.gradualRenderColorList = cloneDeep(copyRnederList);
    },
    onBoardChange (saturation, brightness) {
      this.state.color.saturation = saturation
      this.state.color.brightness = brightness
      this.updateColor()
      this.$forceUpdate()
    },
    setColor (item) {
      this.isCLickSet = true
      if(item.type == "base") {
        const colorStr = this.colorTranRGB(item.color, this.oldAlpha)
        this.$emit("update:modelValue", colorStr, this.prop, this.form)
      }else {
        this.gradualRenderColorList = []
        item.color.map((cItem, index) => {
          const colorStr = this.colorTranRGB(cItem, 100)
          const { renderColor } = transColor(colorStr, 100)
          this.gradualRenderColorList.push({
            id: generateUUID(),
            color: cItem,
            renderColor: renderColor,
            left: index != 0 ? 100 : 0,
            alpha: 100,
          })
        })
        this.currentGradualColor = this.gradualRenderColorList[0]
        this.resetStateColor(this.currentGradualColor)
      }
    },
    /**
     * 对颜色进行rgb hsb hsl 的值进行对应转换
     * @param value 需要转换的值
     * @param alpha 透明度
     */
    colorTranRGB (value, alpha) {
      let colorStr = value
      let obj = null
      if(this.showColorType != "hex") {
        switch(this.rgbType) {
          case "hsl":
            obj = tinycolor(value).toHsl()
            colorStr = `hsla(${obj.h}, ${parseInt(obj.s * 100)}, ${parseInt(obj.l * 100)}, ${alpha}%)`
            break;
          case "hsb":
            obj = tinycolor(value).toHsv()
            colorStr = `hsba(${obj.h}, ${parseInt(obj.s * 100)}, ${parseInt(obj.v * 100)}, ${alpha}%)`
            break;
          default:
            obj = tinycolor(value).toRgb()
            colorStr = `rgba(${obj.r}, ${obj.g}, ${obj.b}, ${alpha}%)`
            break;
        }
      }
      return colorStr
    },
    hueChange (val) {
      this.$set(this.state.color, 'hue', val)
    },
    alphaChange (val) {
      this.oldAlpha = val
      this.$forceUpdate()
    },
    updateColor () {
      if(this.colorType == "pure") {
        let str = this.getRgbTypeStr()
        if(str.startsWith("#")) {
          let newAlpha = this.oldAlpha
          if(newAlpha != this.state.color.alpha) {
            newAlpha = this.state.color.alpha
          }
          this.$emit("update:modelValue", str + (newAlpha == 100 ? "" : decToHex(newAlpha)), this.prop, this.form)
        }else {
          this.$emit("update:modelValue", str, this.prop, this.form)
        }
      }else {
        let str = this.getRgbTypeStr()
        this.currentGradualColor.alpha = this.state.color.alpha
        let { renderColor } = transColor(this.state.color.toHexString(), this.currentGradualColor.alpha)
        let index = this.gradualRenderColorList.findIndex((item) => item.id == this.currentGradualColor.id)
        this.gradualRenderColorList[index].color = str
        this.gradualRenderColorList[index].renderColor = renderColor
        this.currentGradualColor.renderColor = renderColor
        this.gradualRenderColorList[index].alpha = this.state.color.alpha
        this.$emit("update:modelValue", str, this.prop, this.form)
      }
    },
    /**
     * 获取当前type下对应的颜色展示值
     */
    getRgbTypeStr () {
      let str = this.state.color.toHexString()
      if(this.showColorType != "hex") {
        str = `${this.rgbType}a(`
        this.state.color[this.rgbType.toLocaleUpperCase()].map((item, index) => {
          if(index > 0) str += ", "
          if(index == 3) {
            str += parseInt(item * 100) + "%"
          }else if(index > 0 && index < 3) {
            if(this.rgbType != "rgb") {
              str += parseInt(item * 100)
            }else {
              str += item
            }
          }else {
            str += item
          }
        })
        str += ")"
      }
      return str;
    },
    typeChange (key, val) {
      this.isCLickSet = true
      if(key == "colorType") {
        this.showColorType = val
      }else {
        this.rgbType = val
      }
      this.updateColor()
    },
    setCurrentItem (item) {
      if(this.gradualDrag) return
      this.currentGradualColor = item
      this.resetStateColor(this.currentGradualColor)
    },
    /**
    * 对渐变色选中后的颜色重新渲染
    */
    resetStateColor (item) {
      const colorCssArr = getAlpha(item.color);
      let copyModelVal = item.color;
      if(this.rgbType == "hsb" && colorCssArr.length != 0) {
        copyModelVal = tinycolor({
          h: colorCssArr[0],
          s: colorCssArr[1],
          v: colorCssArr[2],
        }).toHexString()
      }
      this.colorInstance = new Color(copyModelVal);
      this.state = {
        color: this.colorInstance,
        hex: this.colorInstance.toHexString(),
        rgb: this.colorInstance.toRgbString(),
      }
      this.oldAlpha = item.alpha
      this.state.color.alpha = item.alpha
    },
    addGradualPoint(event) {
      if(this.gradualDrag) return
      let left = parseInt((event.offsetX / this.$refs.graSilgerBarRef.clientWidth) * 100)
      let renderColor, alpha, colorList
      if(this.showColorType != "hex") {
        colorList = getAlpha(this.gradualRenderColorList[0].color)
      }else {
        colorList = getAlpha(tinycolor(this.gradualRenderColorList[0].color).toRgbString())
      }
      if(left < this.gradualRenderColorList[0].left) {
        alpha = this.gradualRenderColorList[0].alpha
      }else {
        alpha = Math.abs(left - 100)
      }
      switch(this.rgbType) {
        case "hsl":
          renderColor = `hsl(${colorList[0]}deg ${colorList[1]}% ${colorList[2]}% / ${alpha}%)`;
          break;
        case "hsb":
          renderColor = `hwb(${colorList[0]}deg ${colorList[1]}% ${colorList[2]}% / ${alpha}%)`;
          break;
        default:
          renderColor = `rgba(${colorList[0]}, ${colorList[1]}, ${colorList[2]}, ${alpha}%)`;
          break;
      }
      this.gradualRenderColorList.push({
        id: generateUUID(),
        color: this.gradualRenderColorList[0].color,
        renderColor: renderColor,
        left: left,
        alpha: alpha,
      })
    },
    addRatio () {
      this.gradualRatio = (this.gradualRatio + 45) % 360;
    },
    topResize(e, item, index) {
      // 处理拖动的时候不能选择文字
      document.onselectstart = function () {
        return false
      }
      const startY = e.clientX
      const startWidth = (this.$refs.graSilgerBarRef.clientWidth - 14) * (item.left / 100)
      const mouseMove = (documentE) => {
        this.gradualDrag = true
        let moveLeft = startWidth + (documentE.clientX - startY) * 1
        if(moveLeft > this.$refs.graSilgerBarRef.clientWidth - 14) {
          moveLeft = this.$refs.graSilgerBarRef.clientWidth - 14
        }
        if(parseInt((moveLeft / (this.$refs.graSilgerBarRef.clientWidth - 14)) * 100) > parseInt(((this.$refs.graSilgerBarRef.clientWidth - 14) / this.$refs.graSilgerBarRef.clientWidth) * 100) ) {
          item.left = 100
        }else if(parseInt((moveLeft / (this.$refs.graSilgerBarRef.clientWidth - 14)) * 100) < 0) {
          item.left = 0
        }else {
          item.left = parseInt((moveLeft / (this.$refs.graSilgerBarRef.clientWidth - 14)) * 100)
        }
      }
      const mouseUp = () => {
        document.removeEventListener("mousemove", mouseMove)
        document.removeEventListener("mouseup", mouseUp)
        // 拖拽完记得重新设置可以选中
        document.onselectstart = function () {
          return true;
        }
        setTimeout(() => {
          this.gradualDrag = false;
        }, 0)
      };
      document.addEventListener("mousemove", mouseMove)
      document.addEventListener("mouseup", mouseUp)
    },
    resetColorFun () {
      this.changeColorType('pure')
      this.isCLickSet = true
      let colorStr = this.colorTranRGB(this.resetColor, 100)
      if(this.allowEmpty) {
        this.isCLickSet = false
        colorStr = ''
      }
      this.$emit("update:modelValue", colorStr, this.prop, this.form)
    },
    setAlpha () {
      this.oldAlpha = 0
      this.state.color.alpha = 0
      this.$forceUpdate()
    },
    async openPicker (e) {
      const val = e ? e.target.value : null
      if(val) {
        this.sRGBHex = val
      }else{
        // 参考官方，如果它没有在安全环境中运行，则无法访问EyeDropper API（本地ip端口不可访问，请使用localhost调试）
        // https://developer.mozilla.org/en-US/docs/Web/API/EyeDropper
        const eyeDropper = new window.EyeDropper() // 初始化一个EyeDropper对象
        try{
          const result = await eyeDropper.open() // 开始拾取颜色
          this.sRGBHex = result.sRGBHex
        }catch (e) {
          console.log('用户取消了取色')
        }
      }
    },
    outClick () {
      this.colorPickerVisible = false
    },
    stopHandle (e) {
      return false
    },
  },
  watch: {
    sRGBHex: {
      handler (newV, oldV) {
        this.isCLickSet = true
        let colorStr = byColorTypeAndTypeTransColor(
          newV,
          this.oldAlpha,
          this.showColorType,
          this.rgbType
        )
        if(this.colorType == "pure") {
          this.$emit("update:modelValue", colorStr, this.prop, this.form)
        }else {
          const { renderColor } = transColor(newV, this.currentGradualColor.alpha)
          this.currentGradualColor.color = colorStr;
          this.currentGradualColor.renderColor = renderColor;
          this.gradualRenderColorList.filter((item) => {
            if(item.id == this.currentGradualColor.id) {
              item.color = this.currentGradualColor.color;
              item.renderColor = this.currentGradualColor.renderColor;
            }
          })
          this.resetStateColor(this.currentGradualColor);
        }
      }
    },
    gradualRenderColorList: {
      handler(newV, oldV) {
        if(this.colorType != "pure") {
          this.$emit("update:modelValue", this.getGtadualColor, this.prop, this.form)
        }
      },
      deep: true
    },
    gradualRatio: {
      handler(newV, oldV) {
        if(this.colorType != "pure") {
          this.$emit("update:modelValue", this.getGtadualColor, this.prop, this.form)
        }
      },
      deep: true
    },
    modelValueCompute: {
      handler(value, oldV) {
        let equal, newV = value + ""
        if(newV.startsWith("linear-gradient")) {
          if(!this.currentGradualColor.color) {
            if(this.gradualRenderColorList.length == 0) {
              let directionRegex = /linear-gradient\(([^,]+)/;
              let directionMatch = directionRegex.exec(newV);
              let direction = parseInt(directionMatch ? directionMatch[1].trim() : '0');
              this.gradualRatio = direction
              this.gradualRenderColorList = analysGradual(newV)   
            }
            this.currentGradualColor = this.gradualRenderColorList[0]
          }
          if(!this.currentGradualColor.color) {
            let directionRegex = /linear-gradient\(([^,]+)/;
            let directionMatch = directionRegex.exec(newV);
            let direction = parseInt(directionMatch ? directionMatch[1].trim() : '0');
            this.gradualRatio = direction
            this.gradualRenderColorList = analysGradual(newV)
            this.currentGradualColor = this.gradualRenderColorList[0]
          }
          newV = this.currentGradualColor.color + ""
        }
        let copyValue = newV + ""
        if(newV.startsWith("#") && !(this.currentGradualColor.color || "").startsWith("linear-gradient")) {
          const getHexObj = getHexColorAlpha(copyValue)
          copyValue = getHexObj.color
          if(value.startsWith('linear-gradient')){
            this.oldAlpha = this.currentGradualColor.alpha
          }else{
            this.oldAlpha = getHexObj.alpha
          }
          if(!this.isCLickSet) this.showColorType = "hex"
          equal = tinycolor.equals(newV, this.oldColor)
        }else {
          this.showColorType = "css";
          this.rgbType = newV.substring(0, 3)
          const colorCssArr = getAlpha(newV), oldColorArr = getAlpha(this.oldColor)
          let obj = {}, oldObj = {}
          switch(this.rgbType) {
            case "hsl":
              obj = { h: colorCssArr[0], s: colorCssArr[1], l: colorCssArr[2] };
              oldObj = { h: oldColorArr[0], s: oldColorArr[1], l: oldColorArr[2] };
              break;
            case "hsb":
              obj = { h: colorCssArr[0], s: colorCssArr[1], v: colorCssArr[2] };
              oldObj = { h: oldColorArr[0], s: oldColorArr[1], v: oldColorArr[2] };
              copyValue = tinycolor({
                h: colorCssArr[0],
                s: colorCssArr[1],
                v: colorCssArr[2],
              }).toHexString();
              break;
            default:
              obj = { r: colorCssArr[0], g: colorCssArr[1], b: colorCssArr[2] };
              oldObj = { r: oldColorArr[0], g: oldColorArr[1], b: oldColorArr[2] };
              break;
          }
          if(this.showColorType == "css" && colorCssArr.length == 4) {
            this.oldAlpha = colorCssArr[3];
          }
          equal = tinycolor.equals( tinycolor(obj).toHex(), tinycolor(oldObj).toHex() )
        }
        if(!equal) {
          this.oldColor = newV + "";
          if(this.isCLickSet) {
            this.colorInstance = new Color(copyValue);
            this.state.color = this.colorInstance;
          }
          this.state.color.alpha = this.oldAlpha;
        }
        this.isCLickSet = false;
      },
      deep: true
    },
    'state.color': {
      handler(newV, oldV) {
        this.state.hex =  this.state.color.hex
        this.state.rgb = this.state.color.toRgbString()
        if(!this.isCLickSet) this.updateColor()
      },
      deep: true
    }
  }
}
</script>
<style lang="scss" scoped>
.jvs-color-picker-show-box {
  width: 130px;
  height: 36px;
  padding: 8px 16px;
  box-sizing: border-box;
  background: #f5f6f7;
  border-radius: 4px 4px 4px 4px;
  display: flex;
  align-items: center;
  cursor: pointer;
  position: relative;
  .show-color-box-wrap{
    display: flex;
    align-items: center;
    cursor: pointer;
    position: relative;
    .show-color-box-bg {
      position: absolute;
      width: 20px;
      min-width: 20px;
      height: 20px;
      border-radius: 4px 4px 4px 4px;
      border: 1px solid #c2c5cf;
      background-image: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAwAAAAMCAIAAADZF8uwAAAAGUlEQVQYV2M4gwH+YwCGIasIUwhT25BVBADtzYNYrHvv4gAAAABJRU5ErkJggg==);
      background-repeat: repeat;
      z-index: 0;
    }
    .show-color-box {
      z-index: 1;
      width: 20px;
      min-width: 20px;
      height: 20px;
      border-radius: 4px 4px 4px 4px;
      border: 1px solid #c2c5cf;
    }
    .show-color-hex {
      font-size: 14px;
      color: #3d3d3d;
      margin-left: 8px;
    }
  }
  .close-box{
    border-radius: 50%;
    background-color: #eeeff0;
    display: flex;
    align-items: center;
    cursor: pointer;
    visibility: hidden;
    .close-icon{
      width: 16px;
      height: 16px;
    }
  }
  &:hover{
    .close-box{
      visibility: visible;
    }
  }
}
.light-color-picker-show-box {
  background-color: #fff;
}
.dark-color-picker-show-box{
  background-color: #242732;
  .show-color-box-wrap{
    .show-color-hex{
      color: #f5f6f7;
    }
  }
  .close-box{
    background-color: #242732;
  }
}
.jvs-color-picker {
  width: 336px;
  padding: 12px 24px;
  box-sizing: border-box;
  background-color: #fff;
  .color-type-box {
    user-select: none;
    height: 36px;
    background: #f5f6f7;
    border-radius: 4px 4px 4px 4px;
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    grid-column-gap: 8px;
    // display: flex;
    // align-items: center;
    padding: 4px 8px;
    box-sizing: border-box;
    position: relative;
    width: 100%;
    .color-type-mark {
      z-index: 0;
      position: absolute;
      background-color: #fff;
      border-radius: 4px 4px 4px 4px;
      transition: all 0.2s;
    }
    .color-type-item {
      position: relative;
      z-index: 1;
      cursor: pointer;
      font-size: 14px;
      color: #363b4c;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 4px 4px 4px 4px;
      &:hover {
        background-color: #e4e7ea;
      }
    }
    .color-type-active {
      color: #1e6fff;
      &:hover {
        background-color: transparent !important;
      }
    }
  }
  .gradual-color-box {
    display: flex;
    align-items: center;
    margin-top: 8px;
    .gradual-silder-box {
      width: 200px;
      height: 12px;
      margin-right: 16px;
      border-radius: 20px;
      background-image: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAwAAAAMCAIAAADZF8uwAAAAGUlEQVQYV2M4gwH+YwCGIasIUwhT25BVBADtzYNYrHvv4gAAAABJRU5ErkJggg==);
      background-repeat: repeat;
      .gradual-silder-bar {
        width: 100%;
        height: 100%;
        position: relative;
        border-radius: 20px;
        .gradual-color-node {
          position: absolute;
          cursor: pointer;
          width: 14px;
          height: 14px;
          box-shadow: 0px 0px 2px 0px rgba(0, 0, 0, 0.3);
          border: 2px solid #ffffff;
          box-sizing: border-box;
          border-radius: 50%;
          transform: translate(0px, -1px);
        }
        .color-active {
          box-shadow: 0px 0px 2px 0px rgba(0, 0, 0, 0);
          &::after {
            position: absolute;
            content: "";
            width: 14px;
            height: 14px;
            border-radius: 50%;
            border: 2px solid #1e6fff;
            transform: translate(-4px, -4px);
            box-shadow: 0px 0px 2px 0px rgba(0, 0, 0, 0.3);
          }
        }
      }
    }
    .gradual-icon-box {
      width: 32px;
      min-width: 32px;
      height: 32px;
      border-radius: 4px 4px 4px 4px;
      cursor: pointer;
      display: flex;
      justify-content: center;
      align-items: center;
      // background: #F5F6F7;
      .gradual-icon {
        width: 16px;
        height: 16px;
      }
      &:hover {
        background: #f5f6f7;
      }
    }
    .gradual-icon-box+.gradual-icon-box {
      margin-left: 8px;
    }
  }
  .color-silder-box {
    display: flex;
    align-items: center;
    height: 40px;
    width: 100%;
    .color-zilla {
      height: 32px;
      width: 32px;
      min-width: 32px;
      margin-right: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 4px 4px 4px 4px;
      &:hover {
        background: #f5f6f7;
      }
      .color-zilla-icon {
        width: 20px;
        height: 20px;
        cursor: pointer;
      }
    }
    .siler-box {
      width: 100%;
      height: 40px;
    }
    .silder-color-box {
      width: 40px;
      height: 40px;
      min-width: 40px;
      margin-left: 8px;
      border-radius: 4px 4px 4px 4px;
      border: 1px solid #eeeff0;
      position: relative;
      overflow: hidden;
      .color-box {
        width: 100%;
        height: 100%;
        z-index: 1;
        position: relative;
      }
      .color-box-bg {
        position: absolute;
        width: 100%;
        height: 100%;
        background-image: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAwAAAAMCAIAAADZF8uwAAAAGUlEQVQYV2M4gwH+YwCGIasIUwhT25BVBADtzYNYrHvv4gAAAABJRU5ErkJggg==);
        background-repeat: repeat;
        z-index: 0;
      }
    }
  }
}
.common-use-box {
  width: 336px;
  border-top: 1px solid #eeeff0;
  padding: 16px 24px;
  box-sizing: border-box;
  background-color: #fff;
  .common-title {
    font-size: 14px;
    color: #363b4c;
    margin-bottom: 11px;
  }
  .color-list-box {
    display: grid;
    grid-column-gap: 9px;
    grid-row-gap: 8px;
    grid-template-columns: repeat(9, 1fr);
    .color-item {
      height: 24px;
      border-radius: 4px;
      border: 1px solid #eeeff0;
      box-sizing: border-box;
      cursor: pointer;
      position: relative;
      display: flex;
      align-items: center;
      justify-content: center;
      &:hover {
        &::after {
          width: 26px;
          height: 26px;
          content: "";
          position: absolute;
          border-radius: 4px;
          left: -2px;
          top: -2px;
          box-shadow: 0 0 0 2px #b7d1ff !important;
        }
      }
      .icon{
        width: 16px;
        height: 16px;
      }
    }
    .back-trans{
      background-image: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAwAAAAMCAIAAADZF8uwAAAAGUlEQVQYV2M4gwH+YwCGIasIUwhT25BVBADtzYNYrHvv4gAAAABJRU5ErkJggg==);
    }
  }
}
.color-picker-back{
  position: fixed;
  width: 100vw;
  height: 100vh;
  left: 0;
  top: 0;
}
</style>
<style lang="scss">
.color-picker-popover-box {
  padding: 0px !important;
  border-radius: 4px !important;
  overflow: hidden;
  .color-picker-popover {
    border-radius: 4px;
    position: relative;
  }
}
.color-picker-popover-box[data-popper-placement^="bottom"] {
  transform: translateY(-12px);
}
.color-picker-popover-box[data-popper-placement^="top"] {
  transform: translateY(12px);
}
.color-picker-popover-box[data-popper-placement^="left"] {
  transform: translateX(12px);
}
.color-picker-popover-box[data-popper-placement^="right"] {
  transform: translateX(-12px);
}
.test-box {
  width: 100%;
  height: 50px;
}
</style>