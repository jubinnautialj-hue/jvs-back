<template>
  <div ref="groupBox" class="custom-group-box" :style="`width: ${itemData.options.width}px;height: ${itemData.options.height}px;`">
    <div class="custom-group">
      <div class="custom-group-header">
        <div class="custom-group-header-text">{{itemData.options.label}}</div>
        <div class="custom-group-header-tool" @mouseenter="headerToolEnter" @mouseleave="headerToolLeave">
          <div class="edit" @click="editGroup">
            <svg aria-hidden="true">
              <use xlink:href="#jvs-ui-icon-bianji1"></use>
            </svg>
          </div>
          <div class="delete" @click="delGroup">
            <svg aria-hidden="true">
              <use xlink:href="#jvs-ui-icon-shanchuyonghu"></use>
            </svg>
          </div>
        </div>
      </div>
      <div class="custom-group-body" v-html="itemData.options.desc.replace(/\n/g, '<br/>')"></div>
      <div class="group-scal" @mousedown="scalHandle">
        <div></div>
      </div>
      <!-- 节点移入分组显示提示 -->
      <div v-if="entryGroup && entryGroup.length > 0 && entryGroup.indexOf(itemData.id) > -1" class="join-group-tip">
        <span>松开鼠标左键即可加入该分组</span>
      </div>
    </div>
  </div>
</template>

<script>
import eventBus from "../../../utils/eventBus"
export default {
  name: "custom-group",
  props: {
    itemData: {
      type: Object,
    },
  },
  data () {
    return {
      entryGroup: []
    }
  },
  mounted() {
    eventBus.$on('setData', this.setData)
    this.$refs.groupBox.addEventListener('mouseenter', this.mouseenter)
    this.$refs.groupBox.addEventListener('mouseleave', this.mouseleave)
  },
  onUnmounted() {
    eventBus.$off('setData', this.setData)
    this.$refs.groupBox.removeEventListener('mouseenter', this.mouseenter)
    this.$refs.groupBox.removeEventListener('mouseleave', this.mouseleave)
  },
  methods: {
    setData (key, value) {
      this[key] = value
      this.$forceUpdate()
    },
    mouseenter () {
      if(this.entryGroup && this.entryGroup.length > 0) {
        return false
      }
      eventBus.$emit('setData', 'selectedNodeIds', this.itemData.options.nodes)
    },
    mouseleave () {
      eventBus.$emit('setData', 'selectedNodeIds', [])
    },
    headerToolEnter () {
      eventBus.$emit('setGroup', this.itemData.id, 'draggable', false)
    },
    headerToolLeave () {
      eventBus.$emit('setGroup', this.itemData.id, 'draggable', true)
    },
    editGroup () {
      eventBus.$emit('editDelGroup', 'edit', this.itemData.id)
    },
    delGroup () {
      eventBus.$emit('editDelGroup', 'del', this.itemData.id)
    },
    scalHandle () {
      eventBus.$emit('editDelGroup', 'scal', this.itemData.id)
    },
  },
};
</script>

<style lang="scss" scoped>
.custom-group-box{
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.1);
  box-sizing: border-box;
  overflow: hidden;
  cursor: move;
  position: absolute;
  .custom-group{
    position: relative;
    width: 100%;
    height: 100%;
    .custom-group-header{
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding-left: 16px;
      background: rgba(0, 0, 0, 0.3);
      overflow: hidden;
      .custom-group-header-text{
        color: #fff;
        box-sizing: border-box;
        max-width: calc(100% - 58px);
      }
      .custom-group-header-tool{
        display: flex;
        align-items: center;
        width: 48px;
        height: 24px;
        z-index: 550;
        .edit, .delete{
          margin-right: 6px;
          width: 18px;
          height: 18px;
          border-radius: 4px;
          cursor: pointer;
          display: flex;
          align-items: center;
          justify-content: center;
          overflow: hidden;
          &:hover{
            background: #DEEAFF;
            svg{
              fill: #1E6FFF;
            }
          }
        }
        svg{
          width: 14px;
          height: 14px;
          fill: #fff;
        }
      }
    }
    .custom-group-body{
      padding-right: 20px;
      max-height: calc(100% - 32px);
      padding: 10px;
      color: #fff;
      font-size: 12px;
      box-sizing: border-box;
      overflow: auto;
    }
    .group-scal{
      position: absolute;
      right: 0;
      bottom: -5px;
      width: 10px;
      height: 20px;
      transform: rotateZ(45deg);
      border-left: 1px solid #000;
      padding: 5px;
      box-sizing: border-box;
      cursor: nw-resize;
      div{
        width: 5px;
        height: 10px;
        border-left: 1px solid #000;
      }
    }
    .join-group-tip{
      position: absolute;
      top: 32px;
      left: 0;
      width: 100%;
      height: calc(100% - 32px);
      background: #DDEAFF;
      opacity: 0.7;
      z-index: 502;
      display: flex;
      align-items: center;
      justify-content: center;
      span{
        font-family: Source Han Sans-Regular, Source Han Sans;
        font-weight: 400;
        font-size: 12px;
        color: #000;
      }
    }
  }
}
</style>
