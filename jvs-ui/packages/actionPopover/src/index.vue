<template>
    <teleport to="body" v-if="visible">
        <transition name="zoomIn">
            <div
                ref="actionPopover"
                class="action-popover"
                :class="popoverCustomClass"
                :style="{
                    width: rectInfo.width + 'px',
                    height: rectInfo.height + 'px',
                    top: rectInfo.top + 'px',
                    left: rectInfo.left + 'px',
                    transformOrigin: transformOriginStr,
                    zIndex: zIndex
                }">
                <slot></slot>
                <div v-if="isCenterDirection" class="close" @click="close">
                    <Icon name="close" />
                </div>
            </div>
        </transition>
    </teleport>
</template>

<script>
import { getPopoverActivePointByDirection } from '@/utils/direction'
import teleport from '../../teleport/src/index.vue'
export default {
    name:'ActionPopover',
    components:{teleport},
    props:{
        popoverCustomClass: {
            type: String
        },
        closeOnClickOutside: {
            type: Boolean,
            default: true
        },
        zIndex: {
            type: Number,
            default: 2001
        },
    },
  data() {
    return {
        visible:false,
        isCenterDirection:false,
        rectInfo:{
            width: 200,
            height: 200,
            top: 0,
            left: 0
        },
        transformOriginStr:''
    };
  },
  created() {

  },
  mounted() {
    document.addEventListener('click', this.clickOutsideEvent)
  },
    onUnmounted(){
        document.removeEventListener('click', this.clickOutsideEvent)
    },
  methods: {
    toggle(component, element) {
        if (this.visible) {
            this.close()
        } else {
            this.open(component, element)
        }
    },
    async open(component, element){
        setTimeout(()=>{
            if (!component.actionSetting) return
            const { actionSetting } = component
            const { actionType, actionClickType, actionClickValue } = actionSetting
            if (actionType === 1 && actionClickType === 1) {
                const { w, h, direction } = actionClickValue
                const [endX, endY, fromX, fromY] = getPopoverActivePointByDirection(element, {
                        width: w || 200,
                        height: h || 200
                }, direction)
                this.rectInfo = {
                    width: w || 200,
                    height: h || 200,
                    left: endX,
                    top: endY
                }
                this.transformOriginStr = `${fromX - endX}px ${fromY - endY}px`
                this.visible = true
                this.isCenterDirection = direction === 0
            }
        },1000 * 0.2)
    },
    close(){
        this.visible = false
        this.isCenterDirection = false
    },
    clickOutsideEvent(e){
        if (!this.closeOnClickOutside) return
        // 监听，除了点击自己，点击其他地方将自身隐藏
        const contentWrap = document.getElementById("actionPopover");
        if (this.visible && contentWrap && !contentWrap.contains(e.target)) {
            this.visible = false
        }
    }
  }
};
</script>

<style scoped lang="scss">
.action-popover {
  position: fixed;
  top: 0;
  left: 0;
  min-width: 40px;
  min-height: 40px;
  z-index: 2001;
  transition: all .4s ease-in-out;
}
.close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  font-size: 22px;
  color: #929499;
  position: absolute;
  top: 0;
  right: 0;
  cursor: pointer;
  transition: transform .3s ease-in-out;
  &:hover {
    color: #a2a4a9;
    transform: rotate(-90deg);
  }
}
</style>
