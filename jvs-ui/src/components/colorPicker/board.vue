<template>
  <div
    ref="boardElement"
    :class="['vc-saturation']"
    :style="{ backgroundColor: state.hueColor }"
    @mousedown="onClickBoard"
    @mousemove="onDrag"
    @mouseup="onDragEnd"
  >
    <div class="vc-saturation__white"></div>
    <div class="vc-saturation__black"></div>
    <div class="vc-saturation__cursor" :style="getCursorStyle">
      <div></div>
    </div>
  </div>
</template>

<script>
import { clamp, Color } from './utils/color'
import { merge } from "lodash";
export default {
  name: 'board',
  props: {
    colorProp: {
      type: Object
    },
    colorType: String
  },
  computed: {
    getCursorStyle () {
      return {
        top: this.cursorTop + "px",
        left: this.cursorLeft + "px",
      }
    },
  },
  data () {
    return {
      hueHsv: {
        h: 0,
        s: 1,
        v: 1,
      },
      colorInstance: null,
      state: null,
      hueColor: new Color(this.hueHsv).toHexString(),
      cursorTop: 0,
      cursorLeft: 0,
      mousedown: false,
    }
  },
  created () {
    this.colorInstance = new Color()
    if(this.colorProp) {
      this.$set(this.hueHsv, 'h', this.colorProp.hue)
      this.colorInstance = this.colorProp
      this.state =  {
        color: this.colorInstance,
        hex: this.colorInstance.toHexString(),
        rgb: this.colorInstance.toRgbString(),
      }
      merge(this.state, {
        hueColor: new Color({ h: this.colorProp.hue, s: 1, v: 1 }).toHexString(),
        saturation: this.colorProp.saturation,
        brightness: this.colorProp.brightness,
      })
    }
  },
  mounted () {
    this.updatePosition()
  },
  methods: {
    onClickBoard (event) {
      this.mousedown = true
      this.handleDrag(event)
      document.body.style.userSelect = 'none'
    },
    onDrag (event) {
      if(this.mousedown) {
        this.handleDrag(event)
      }
    },
    onDragEnd () {
      this.mousedown = false
      document.body.style.userSelect = 'auto'
    },
    handleDrag (event) {
      if(this.$refs.boardElement) {
        let rect = this.$refs.boardElement.getBoundingClientRect()
        let left = event.clientX - rect.left
        let top = event.clientY - rect.top
        left = clamp(left, 0, rect.width)
        top = clamp(top, 0, rect.height)
        let saturation = left / rect.width
        let bright = clamp(-(top / rect.height) + 1, 0, 1)
        this.cursorLeft = left
        this.cursorTop = top
        this.state.saturation = saturation
        this.state.brightness = bright
        this.$emit("change", saturation, bright)
      }
    },
    updatePosition () {
      if(this.$refs.boardElement) {
        let rect = this.$refs.boardElement.getBoundingClientRect()
        this.cursorLeft = this.state.saturation * rect.width
        this.cursorTop = (1 - this.state.brightness) * rect.height
      }
    }
  },
  watch: {
    colorType: {
      handler(value, oldv) {
        if(value != oldv) {
          this.updatePosition()
        }
      }
    },
    colorProp: {
      handler(value, oldv) {
        if(value){
          this.$set(this.hueHsv, 'h', value.hue)
          merge(this.state, {
            hueColor: new Color({ h: value.hue, s: 1, v: 1 }).toHexString(),
            saturation: value.saturation,
            brightness: value.brightness,
          })
          if(!this.mousedown) {
            this.updatePosition()
          }
          this.$forceUpdate()
        }
      },
      deep: true
    }
  }
}
</script>
<style lang="scss" scoped>
.vc-saturation {
  position: relative;
  margin-top: 8px;
  margin-bottom: 18px;
  width: 100%;
  height: 200px;
  border-radius: 4px 4px 4px 4px;
  border: 1px solid #EEEFF0;
  &__chrome {
    border-top-left-radius: 5px;
    border-top-right-radius: 5px;
    border-color: transparent;
  }

  &__hidden {
    overflow: hidden;
  }

  &__white,
  &__black {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    border-radius: 2px 4px 4px 4px;
  }

  &__black {
    background: linear-gradient(0deg, #000, transparent);
  }

  &__white {
    background: linear-gradient(90deg, #fff, hsla(0, 0%, 100%, 0));
  }

  &__cursor {
    position: absolute;

    div {
      transform: translate(-5px, -5px);
      box-shadow: 0 1px 4px 0 rgba(0, 0, 0, 0.37);
      width: 10px;
      height: 10px;
      border: 1px solid white;
      border-radius: 50%;
      cursor: pointer;
    }
  }
}
</style>