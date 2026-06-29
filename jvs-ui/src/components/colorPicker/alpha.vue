<template>
  <div :class="['vc-alpha-slider', 'transparent']">
    <div ref="barElement" class="vc-alpha-slider__bar" :style="getBackgroundStyle" @click="onClickSider">
      <div :class="['vc-alpha-slider__bar-pointer']" ref="cursorElement" :style="getCursorStyle" >
        <div class="vc-alpha-slider__bar-handle"></div>
      </div>
    </div>
  </div>
</template>

<script>
import { Color, rgbaColor } from "./utils/color";
import { DOMUtils, DragEventOptions } from "@aesoper/normal-utils";
import { merge } from "lodash";
export default {
  name: 'alpha',
  props: {
    colorProp: {
      type: Object
    },
    show: {
      type: Boolean,
      default: false
    },
  },
  computed: {
    getBackgroundStyle () {
      let startColor = rgbaColor(this.state.red, this.state.green, this.state.blue, 0)
      let endColor = rgbaColor(this.state.red, this.state.green, this.state.blue, 100)
      return { background: `linear-gradient(to right, ${startColor} , ${endColor})` }
    },
    getCursorStyle () {
      let left = this.getCursorLeft()
      return { left: left + "px", top: 0, }
    }
  },
  data () {
    return {
      color: null,
      state: {
        red: null,
        green: null,
        blue: null,
        alpha: null,
      },
      barRect: null,
      cursorWidth: null,
    }
  },
  created () {
    this.color = this.colorProp ? this.colorProp : new Color()
    this.state = {
      red: this.color.red,
      green: this.color.green,
      blue: this.color.blue,
      alpha: this.color.alpha,
    }
  },
  mounted () {
    let dragConfig = {
      drag: (event) => {
        this.onMoveBar(event);
      },
      end: (event) => {
        this.onMoveBar(event);
      },
    }
    if(this.$refs.barElement && this.$refs.cursorElement) {
      this.barRect = this.$refs.barElement.getBoundingClientRect()
      this.cursorWidth = this.$refs.cursorElement.offsetWidth
      DOMUtils.triggerDragEvent(this.$refs.barElement, dragConfig)
    }
  },
  methods: {
    getCursorLeft () {
      if(this.barRect) {
        let alpha = this.state.alpha / 100;
        return Math.round(alpha * (this.barRect.width - this.cursorWidth) + this.cursorWidth / 2)
      }
      return 0
    },
    onClickSider (event) {
      let target = event.target
      if(target !== this.$refs.barElement) {
        this.onMoveBar(event);
      }
    },
    onMoveBar (event) {
      event.stopPropagation();
      if(this.$refs.barElement && this.$refs.cursorElement) {
        this.cursorWidth = this.$refs.cursorElement.offsetWidth
        let left = event.clientX - this.barRect.left
        left = Math.max(this.cursorWidth / 2, left)
        left = Math.min(left, this.barRect.width - this.cursorWidth / 2)
        const alpha = Math.round(((left - this.cursorWidth / 2) / (this.barRect.width - this.cursorWidth)) * 100)
        this.color.alpha = alpha
        this.state.alpha = alpha
        this.$emit("change", alpha)
      }
    },
  },
  watch: {
    colorProp: {
      handler(value, oldval) {
        if(value){
          this.color = value;
          merge(this.state, {
            red: value.red,
            green: value.green,
            blue: value.blue,
            alpha: value.alpha,
          })
        }
      },
      deep: true
    }
  }
}
</script>
<style lang="scss" scoped>
.vc-alpha-slider {
  position: relative;
  width: 100%;
  height: 14px;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.08);
  border-radius: 15px;

  &.is-vertical {
    width: 14px;
    height: 100%;
    display: inline-block;
    transform: rotate(180deg);
  }

  &.transparent {
    background-image: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAwAAAAMCAIAAADZF8uwAAAAGUlEQVQYV2M4gwH+YwCGIasIUwhT25BVBADtzYNYrHvv4gAAAABJRU5ErkJggg==);
    background-repeat: repeat;
  }

  &__bar {
    position: relative;
    width: 100%;
    height: 100%;
    border-radius: 15px;

    &-pointer {
      position: absolute;
      width: 14px;
      height: 14px;
    }

    &-handle {
      width: 12px;
      height: 12px;
      border-radius: 50%;
      transform: translate(-7px, -2px);
      // background-color: #f8f8f8;
      border: 2px solid #FFFFFF;
      margin-top: 1px;
      // box-shadow: 0 1px 4px 0 rgba(0, 0, 0, 0.37);
      box-shadow: 0px 0px 2px 0px rgba(0,0,0,0.3);
      cursor: pointer;

      &.vertical {
        transform: translate(0, -7px);
        margin-top: 0;
      }
    }
  }
}
</style>