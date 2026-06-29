<template>
  <el-popover placement="bottom-start" width="320" trigger="click">
    <p style="margin: 5px 10px;text-indent: 10px;font-size: 14px;color: #c9c9c9">添加流程节点</p>
    <div class="node-select">
      <div @click="addNode(nodeType.SP)">
        <div class="svg-icon" style="background: #FFF2E6;">
          <svg aria-hidden="true">
            <use xlink:href="#icon-jvs-shenpiren"></use>
          </svg>
        </div>
        <span>审批人</span>
      </div>
      <div @click="addNode(nodeType.CS)">
        <div class="svg-icon" style="background: rgba(54,180,82,0.12);">
          <svg aria-hidden="true">
            <use xlink:href="#icon-jvs-chaosongren"></use>
          </svg>
        </div>
        <span>抄送人</span>
      </div>
      <div @click="addNode(nodeType.CONDITION)">
        <div class="svg-icon" style="background: #14C9C9;">
          <svg aria-hidden="true">
            <use xlink:href="#icon-jvs-tiaojianfenzhi"></use>
          </svg>
        </div>
        <span>条件分支</span>
      </div>
      <div @click="addNode(nodeType.AUTOMATION)">
        <div class="svg-icon" style="background: rgba(108,89,253,0.12);">
          <svg aria-hidden="true">
            <use xlink:href="#icon-jvs-yewuluoji"></use>
          </svg>
        </div>
        <span>业务逻辑</span>
      </div>
      <div @click="addNode(nodeType.PARALLEL)">
        <div class="svg-icon" style="background: #363B4C;">
          <svg aria-hidden="true">
            <use xlink:href="#icon-jvs-binghangfenzhi"></use>
          </svg>
        </div>
        <span>并行分支</span>
      </div>
      <!-- <div @click="addNode(nodeType.MESS)">
        <div class="svg-icon" style="background: ;">
          <svg aria-hidden="true">
            <use xlink:href="#"></use>
          </svg>
        </div>
        <span>消息通知</span>
      </div> -->
    </div>
    <div slot="reference">
      <svg aria-hidden="true" style="width: 24px;height: 24px;cursor: pointer;">
        <use :xlink:href="`#${icon}`"></use>
      </svg>
    </div>
  </el-popover>
</template>

<script>
import { nodeType } from "./enumConst";
export default {
  name: "InsertButton",
  components: {},
  props: {
    icon: {
      type: String,
      default: 'icon-jvs-tianjia1'
    }
  },
  data() {
    return {
      nodeType: nodeType,
    }
  },
  methods: {
    addNode (type) {
      this.$emit('insertNode', type)
    }
  }
}
</script>

<style lang="scss" scoped>
.node-select{
  padding: 0 15px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  >div{
    display: flex;
    align-items: center;
    margin: 5px 5px;
    cursor: pointer;
    padding: 6px 15px;
    background-color: #F8F9F9;
    border-radius: 10px;
    width: 102px;
    position: relative;
    span{
      font-family: Microsoft YaHei, Microsoft YaHei;
      font-weight: 400;
      font-size: 14px;
      color: #363B4C;
    }
    &:hover{
      background-color: #fff;
      box-shadow: 0 0 8px 2px #d6d6d6;
    }
    .svg-icon{
      margin-right: 8px;
      width: 24px;
      height: 24px;
      border-radius: 4px 4px 4px 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      svg{
        width: 16px;
        height: 16px;
      }
    }
  }
}
</style>
