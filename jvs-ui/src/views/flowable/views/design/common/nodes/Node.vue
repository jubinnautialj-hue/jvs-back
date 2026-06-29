<template>
  <div :class="['node', (isRoot || !show) && 'root', showError && 'node-error-state', currentNode && 'currentNode', (!show && spacialClass) && spacialClass]">
    <div v-if="show" @click="$emit('selected')" :class="{'node-body': true, 'error': showError}" >
      <!-- 遮罩及删除 -->
      <div v-if="!isRoot" class="node-back-box">
        <div class="delete-icon" @click="$emit('delNode')">
          <svg aria-hidden="true">
              <use xlink:href="#icon-jvs-shanchuyonghu"></use>
            </svg>
        </div>
      </div>
      <!-- 顶部箭头 -->
      <div v-if="!isRoot" class="top-point"></div>
      <!-- 底部圆点 -->
      <div class="bottom-point"></div>
      <!-- 节点内容 -->
      <div class="node-body-box">
        <div class="node-body-header">
          <div class="svg-icon" :style="`background: ${headerBgc};`">
            <svg aria-hidden="true">
              <use :xlink:href="`#${headerIcon}`"></use>
            </svg>
          </div>
          <ellipsis class="name" hover-tip :content="title"/>
        </div>
        <div class="node-body-content">
          <span class="placeholder" v-if="(content || '').trim() === ''">{{placeholder}}</span>
          <ellipsis :row="1" :content="content" v-else style="flex: 1;"></ellipsis>
          <i class="el-icon-arrow-right"></i>
        </div>
        <div class="node-error" v-if="showError">
          <el-tooltip effect="dark" :content="errorInfo" placement="top-start">
            <i class="el-icon-warning-outline"></i>
          </el-tooltip>
        </div>
      </div>
      <span v-if="currentNode" class="lightLine lightLine1"></span>
      <span v-if="currentNode" class="lightLine lightLine2"></span>
      <span v-if="currentNode" class="lightLine lightLine3"></span>
      <span v-if="currentNode" class="lightLine lightLine4"></span>
    </div>
    <div class="node-footer">
      <div class="btn">
        <insert-button @insertNode="type => $emit('insertNode', type)"></insert-button>
      </div>
    </div>
  </div>
</template>

<script>
import InsertButton from '../InsertButton.vue'
import ellipsis from '../components/Ellipsis.vue'

export default {
  name: "Node",
  components: {InsertButton, ellipsis},
  props:{
    //是否为根节点
    isRoot: {
      type: Boolean,
      default: false
    },
    //是否显示节点体
    show: {
      type: Boolean,
      default: true
    },
    //节点内容区域文字
    content: {
      type: String,
      default: ""
    },
    title:{
      type: String,
      default: "标题"
    },
    placeholder:{
      type: String,
      default: "请设置"
    },
    //节点体左侧图标
    leftIcon: {
      type: String,
      default: undefined
    },
    //头部图标
    headerIcon:{
      type: String,
      default: ''
    },
    //头部背景色
    headerBgc:{
      type: String,
      default: '#576a95'
    },
    //是否显示错误状态
    showError:{
      type: Boolean,
      default: false
    },
    errorInfo:{
      type: String,
      default: '无信息'
    },
    currentNode: {
      type: Boolean
    },
    spacialClass: {
      type: String
    }
  },
  data() {
    return {}
  },
  methods: {}
}
</script>

<style lang="scss" scoped>
.root{
  &:before{
    display: none !important;
  }
}
.node-error-state{
  .node-body{
    box-shadow: 0px 0px 5px 0px #F56C6C !important;
  }
}
.node{
  padding: 0 50px;
  width: 200px;
  position: relative;
  &:before{
    content: '';
    position: absolute;
    top: -12px;
    left: 50%;
    -webkit-transform: translateX(-50%);
    transform: translateX(-50%);
    width: 0;
    border-style: solid;
    border-width: 8px 6px 4px;
    border-color: #CACACA transparent transparent;
    background: #F5F5F7;
  }
  .node-body{
    width: 200px;
    height: 80px;
    padding: 10px 12px 12px 12px;
    background: #FFFFFF;
    border-radius: 4px;
    border: 1px solid #fff;
    cursor: pointer;
    box-sizing: border-box;
    position: relative;
    z-index: 4;
    .node-back-box{
      position: absolute;
      width: 100%;
      height: 112px;
      left: 0;
      top: -32px;
      z-index: 4;
      .delete-icon{
        position: absolute;
        top: 0;
        right: 0;
        width: 28px;
        height: 28px;
        background: #FFFFFF;
        box-shadow: 0px 2px 8px 0px rgba(54,59,76,0.2);
        border-radius: 4px;
        display: none;
        align-items: center;
        justify-content: center;
        svg{
          width: 16px;
          height: 16px;
        }
      }
    }
    .top-point{
      position: absolute;
      left: calc(50% - 4px);
      top: -6px;
      width: 0;
      height: 0;
      border-left: 4px solid transparent;
      border-right: 4px solid transparent;
      border-top: 6px solid #C2C5CF;
    }
    .bottom-point{
      position: absolute;
      left: calc(50% - 3px);
      bottom: -4px;
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: #C2C5CF;
    }
    .node-body-header{
      display: flex;
      align-items: center;
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
      .name{
        font-family: Microsoft YaHei, Microsoft YaHei;
        font-weight: 400;
        font-size: 14px;
        color: #363B4C;
        width: 144px;
        display: inline-block;
      }
    }
    .node-body-content{
      margin-top: 10px;
      width: 100%;
      height: 24px;
      line-height: 24px;
      padding: 0 8px;
      background: #F5F6F7;
      border-radius: 50px;
      box-sizing: border-box;
      font-family: Microsoft YaHei-Regular, Microsoft YaHei;
      font-weight: 400;
      font-size: 12px;
      color: #363B4C;
      display: flex;
      align-items: center;
      i{
        color: #6F7588;
        font-size: 14px;
      }
      .placeholder{
        flex: 1;
        color: #6F7588;
      }
    }
    .node-error{
      position: absolute;
      right: -40px;
      top: 20px;
      font-size: 25px;
      color: #F56C6C;
    }
    &:hover{
      border-color: #1E6FFF;
      .delete-icon{
        display: flex;
      }
    }
  }
  .node-footer{
    position: relative;
    z-index: 1;
    .btn{
      width: 100%;
      display: flex;
      padding: 20px 0;
      justify-content: center;
      box-sizing: border-box;
    }
    &::before{
      content: "";
      position: absolute;
      top: 0;
      left: 50%;
      bottom: 0;
      z-index: -1;
      width: 0px;
      height: 100%;
      border-left: 1px solid #C2C5CF;
    }
    .btn:active{
      box-shadow: none;
    }
  }
}
.branch-node{
  .start-item.has-children-item, .end-item.has-children-item{
    // 最后一个节点
    >div:nth-last-of-type(5){
      .node-footer{
        &::before{
          height: calc(100% - 20px);
          top: -20px;
        }
      }
    }
  }
}
</style>
