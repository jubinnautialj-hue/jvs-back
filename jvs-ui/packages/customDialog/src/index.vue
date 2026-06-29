<template>
  <div>
    <!-- <div v-if="visible" :class="['custom-dialog-wrapper', (!blur || isLowPreformance) && 'low-performance', customWrapperClass]" @click.self="clickOutsideEvent" /> -->
      <div v-if="visible" :class="['custom-dialog-main', customClass]">
        <div v-if="$slots.title || title" class="custom-dialog-title">
          <slot name="title">
            <div class="custom-dialog-static-title">
              {{ title }}
            </div>
          </slot>
        </div>
        <slot name="close">
          <div :class="['custom-dialog-close', !$slots.title && !title && 'outside-close']" @click.prevent="close(true)">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24">
              <path fill="currentColor" d="M12 10.586l4.95-4.95 1.414 1.414-4.95 4.95 4.95 4.95-1.414 1.414-4.95-4.95-4.95 4.95-1.414-1.414 4.95-4.95-4.95-4.95L7.05 5.636z"/>
            </svg>
          </div>
        </slot>
        <div class="custom-dialog-body">
          <slot></slot>
        </div>
        <div v-if="$slots.footer" class="custom-dialog-footer">
          <slot name="footer"></slot>
        </div>
      </div>
    <div class="custom-dialog-static" ref="staticRect" :style="{ width, height }" />
  </div>
</template>

<script>
export default {
    name:"customDialog",
    props:{
      width: {
        type: String,
        default: '60vw'
      },
      height: {
        type: String,
        default: '70vh'
      },
      customWrapperClass: String,
      customClass: String,
      closeOnClickOutside: {
        type: Boolean,
        default: false
      },
      closeOnPressEscape: {
        type: Boolean,
        default: true
      },
      value: {
        type: Boolean,
        default: false
      },
      title: String,
      listenWindowSizeChange: {
        type: Boolean,
        default: true
      },
      debounceWait: {
        type: Number,
        default: 200
      },
      blur: {
        type: Boolean,
        default: true
      }
    },
  data() {
    return {
      visible:false,
      isLowPreformance:false,
      transformOriginStr:'',
      timerId:null,
      mousePosition:{x:0,y:0},
      timer:null,
      isClicked:false,
      rectInfo:{
        width: 200,
        height: 200,
        top: 0,
        left: 0
      }
    };
  },
  created() {
    if(this.listenWindowSizeChange) window.addEventListener('resize', this.resetSize, true)
  },
  mounted() {},
  watch:{
    'value':{
      handler(newVal,oldVal){
        if (newVal) {
          this.open()
        } else {
          this.close()
        }
      },
      deep:true,
      immediate: true, // 第一次初始化渲染就可以监听到
    }
  },
  methods: {
    open(needEmit = false){
      setTimeout(() => {
        const { width, height } = (this.$refs.staticRect).getBoundingClientRect()
        const fromX = (this.isClicked&& this.mousePosition.x) || window.innerWidth / 2
        const fromY = (this.isClicked&& this.mousePosition.y) || window.innerHeight / 2
        const endX = window.innerWidth / 2 - width / 2
        const endY = window.innerHeight / 2 - height / 2
        this.rectInfo = {
          width,
          height,
          left: endX,
          top: endY
        }
        this.transformOriginStr = `${fromX - endX}px ${fromY - endY}px`
        this.visible = true
        if (needEmit) this.$emit('update:value', true)
      })
      document.addEventListener('click', this.onMouseClick)
      document.addEventListener('keydown', this.onKeyDown)
    },
    onMouseClick(e){
      if(!this.visible) {
        if (this.timerId !== null) window.clearTimeout(this.timerId)
        this.isClicked = true
        this.timerId = window.setTimeout(() => {
          this.isClicked = false
        }, 64)
        this.mousePosition = { x: e.clientX, y: e.clientY }
      }
    },
    onKeyDown(e){
      if (this.closeOnPressEscape && e.key === 'Escape' && this.modelValue) {
        this.$emit('update:value', false)
      }
    },
    close(needEmit = false){
      this.visible = false
      if (needEmit) this.$emit('update:value', false)
      document.removeEventListener('click', this.onMouseClick)
      document.removeEventListener('keydown', this.onKeyDown)
      if(this.listenWindowSizeChange) window.removeEventListener('resize', this.resetSize)
      this.$emit('close')
    },
    resetSize() {
      if(this.visible) {
        if(this.timer) clearTimeout(this.timer)
        this.timer = setTimeout(() => this.open(), this.debounceWait)
      }
    },
    clickOutsideEvent(){
      if (!this.closeOnClickOutside) return
      emit('update:value', false)
    }
  }
};
</script>

<style scoped lang="scss">
.custom-dialog-wrapper {
  position: fixed;
  top: 60px;
  left: 0;
  bottom: 0;
  right: 0;
  z-index: 2000;
}
.custom-dialog-main {
  position: fixed;
  top: 60px;
  right: 0;
  width: 472px;
  height: calc(100vh - 60px);
  z-index: 2000;
  background: #fff;
  display: flex;
  flex-direction: column;
  box-shadow: -5px 0px 10px 0px rgba(54,59,76,0.1);
  border-radius: 4px;
  .custom-dialog-title {
    padding: 0 24px;
    height: 70px;
    border-bottom: 1px solid #EEEFF0;
    box-sizing: border-box;
    display: flex;
    align-items: center;
    display: none;
    .custom-dialog-static-title {
      font-family: Source Han Sans-Bold, Source Han Sans;
      font-weight: 700;
      font-size: 20px;
      color: #363B4C;
    }
  }
  .custom-dialog-close {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    color: #b6b7b9;
    position: absolute;
    top: 11px;
    right: 10px;
    cursor: pointer;
  }
  .custom-dialog-body {
    height: 100%;
    flex: 1;
    overflow-y: auto;
    padding: 5px 10px;
    font-size: 16px;
    .form-wrapper{
      /deep/.el-form{
        padding: 0 16px;
        .el-form-item{
          margin: 0;
          padding: 0;
          margin-top: 16px;
          .el-form-item__label{
            margin-bottom: 8px;
            padding: 0;
          }
          .el-input{
            .el-input__inner{
              background: #F5F6F7;
              border: 0;
            }
          }
        }
      }
    }
  }
  &.enter{
    animation-duration: .3s;
    animation-name: anim-right-popup-show;
    animation-timing-function: cubic-bezier(0.6,0,0.4,1);
  }
  &.leave{
    animation-duration: .3s;
	  animation-name: anim-right-popup-close;
  }
  .custom-dialog-body{
    padding: 0;
  }
  .custom-dialog-footer{
    border-top: 1px solid #EEEFF0;
    .footer{
      text-align: center!important;
      .btn-primary{
        background-color: #1E6FFF;
        color: #fff;
      }
    }
  }
}
.custom-dialog-static {
  position: fixed;
  left: -9999px;
  top: -9999px;
  visibility: hidden;
}

.zoomIn-enter-active {
  animation: zoomInCustom 0.3s forwards cubic-bezier(0.075, 0.82, 0.165, 1);
}
.zoomIn-leave-active {
  animation: zoomInCustom 0.3s reverse forwards ease-in-out;
}
@keyframes zoomInCustom {
  from {
    opacity: 0;
    transform: scale(0);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.fadeIn-enter-active {
  animation: fadeInCustom 0.4s;
}
.fadeIn-leave-active {
  animation: fadeInCustom 0.4s reverse;
}
.fadeOut-leave-active {
  animation: fadeInCustom 0.4s reverse;
}
@keyframes fadeInCustom {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes anim-right-popup-show {
	0% { right: -100%; }
	100% { right: 0; }
}
@keyframes anim-right-popup-close {
	0% { right: 0;}
	100% { right: -100%; }
}
</style>
