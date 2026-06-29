<template>
  <div :class="['vc-hue-slider']">
    <div ref="barElement" class="vc-hue-slider__bar" @click="onClickSider">
      <div ref="cursorElement" :class="[ 'vc-hue-slider__bar-pointer']" :style="getCursorStyle">
        <div class="vc-hue-slider__bar-handle"></div>
      </div>
    </div>
  </div>
</template>

<script>
import { DOMUtils, DragEventOptions } from "@aesoper/normal-utils";
import { merge } from "lodash";
export default {
  name: "hue",
  props: {
    color: Object,
  },
  computed: {
    getCursorStyle () {
      let left = this.getCursorLeft()
      return {
        left: left + "px",
        top: 0,
      }
    }
  },
  data() {
    return {
      state: {
        hue: 0,
        show: false,
      },
      barRect: null,
    };
  },
  created () {
    this.state.hue = this.color ? this.color.hue : 0
  },
  mounted () {
    let dragConfig = {
      drag: (event) => {
        this.onMoveBar(event)
      },
      end: (event) => {
        this.onMoveBar(event)
      },
    }
    if(this.$refs.barElement && this.$refs.cursorElement) {
      this.barRect = this.$refs.barElement.getBoundingClientRect();
      DOMUtils.triggerDragEvent(this.$refs.barElement, dragConfig);
    }
  },
  methods: {
    getCursorLeft () {
      if(this.barRect && this.$refs.cursorElement) {
        let offsetWidth = this.$refs.cursorElement.offsetWidth
        if(this.state.hue === 360) {
          return this.barRect.width - offsetWidth / 2
        }
        return ( ((this.state.hue % 360) * (this.barRect.width - offsetWidth)) / 360 + offsetWidth / 2 )
      }
      return 0
    },
    onClickSider (event) {
      let target = event.target;
      if (target !== this.$refs.barElement) {
        this.onMoveBar(event)
      }
    },
    onMoveBar (event) {
      event.stopPropagation()
      if(this.barRect && this.$refs.cursorElement) {
        let offsetWidth = this.$refs.cursorElement.offsetWidth
        let left = event.clientX - this.barRect.left
        left = Math.min(left, this.barRect.width - offsetWidth / 2)
        left = Math.max(offsetWidth / 2, left)
        let hue = Math.round(((left - offsetWidth / 2) / (this.barRect.width - offsetWidth)) * 360)
        this.state.hue = hue
        this.$emit("change", hue)
        this.$forceUpdate()
      }
    }
  },
  watch: {
    color: {
      handler(value, oldv) {
        if(value) {
          merge(this.state, { hue: value.hue })
          this.$forceUpdate()
        }
      },
      deep: true
    }
  }
}
</script>
<style lang="scss" scoped>
.vc-hue-slider {
  position: relative;
  margin-bottom: 12px;
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
    background: linear-gradient(
      to right,
      rgb(255, 0, 0) 0%,
      rgb(255, 255, 0) 16.66%,
      rgb(0, 255, 0) 33.33%,
      rgb(0, 255, 255) 50%,
      rgb(0, 0, 255) 66.66%,
      rgb(255, 0, 255) 83.33%,
      rgb(255, 0, 0) 100%
    );
    background: -webkit-linear-gradient(
      left,
      rgb(255, 0, 0) 0%,
      rgb(255, 255, 0) 16.66%,
      rgb(0, 255, 0) 33.33%,
      rgb(0, 255, 255) 50%,
      rgb(0, 0, 255) 66.66%,
      rgb(255, 0, 255) 83.33%,
      rgb(255, 0, 0) 100%
    );
    background: -moz-linear-gradient(
      left,
      rgb(255, 0, 0) 0%,
      rgb(255, 255, 0) 16.66%,
      rgb(0, 255, 0) 33.33%,
      rgb(0, 255, 255) 50%,
      rgb(0, 0, 255) 66.66%,
      rgb(255, 0, 255) 83.33%,
      rgb(255, 0, 0) 100%
    );
    background: -ms-linear-gradient(
      left,
      rgb(255, 0, 0) 0%,
      rgb(255, 255, 0) 16.66%,
      rgb(0, 255, 0) 33.33%,
      rgb(0, 255, 255) 50%,
      rgb(0, 0, 255) 66.66%,
      rgb(255, 0, 255) 83.33%,
      rgb(255, 0, 0) 100%
    );

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
      border: 2px solid #ffffff;
      margin-top: 1px;
      box-shadow: 0px 0px 2px 0px rgba(0, 0, 0, 0.3);
      cursor: pointer;

      &.vertical {
        transform: translate(0, -7px);
        margin-top: 0;
      }
    }
  }
}
</style>
