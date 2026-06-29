<template>
  <div class="node">
    <div class="node-body" @click="$emit('selected')">
      <!-- 遮罩及复制删除 -->
      <div v-if="!config.defaultCondition" class="node-back-box">
        <div class="delete-icon" @click.stop="$emit('delNode')">
          <svg>
            <use xlink:href="#icon-jvs-shanchuyonghu"></use>
          </svg>
        </div>
      </div>
      <!-- 顶部箭头 -->
      <div class="top-point"></div>
      <!-- 底部圆点 -->
      <div class="bottom-point"></div>
      <div class="node-body-left" @click.stop="$emit('leftMove')" v-if="level > 1">
        <svg>
          <use xlink:href="#icon-jvs-shouqi"></use>
        </svg>
      </div>
      <div class="node-body-main">
        <div class="node-body-main-header">
          <span class="title">
            <svg>
              <use xlink:href="#icon-jvs-fenzhi"></use>
            </svg>
            <ellipsis class="name" hover-tip :content="config.name ? config.name:('并行任务' + level)"/>
          </span>
        </div>
      </div>
    </div>
    <div class="node-footer">
      <div class="btn">
        <insert-button icon="icon-jvs-a-zu604762" @insertNode="type => $emit('insertNode', type)"></insert-button>
      </div>
    </div>
  </div>
</template>

<script>
import InsertButton from '../InsertButton.vue'
import ellipsis from '../components/Ellipsis.vue'
export default {
  name: "ConcurrentNode",
  components: {InsertButton, ellipsis},
  props:{
    config:{
      type: Object,
      default: () => {
        return {}
      }
    },
    level:{
      type: Number,
      default: 1
    },
    //条件数
    size:{
      type: Number,
      default: 0
    }
  },
  data() {
    return {

    }
  },
  methods: {}
}
</script>

<style lang="scss" scoped>
.node{
  padding: 58px 55px 0;
  width: 64px;
  .node-body{
    height: 24px;
    position: relative;
    border-radius: 4px;
    background: #fff;
    z-index: 4;
    box-sizing: border-box;
    cursor: pointer;
    .node-back-box{
      position: absolute;
      width: 100%;
      height: 56px;
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
    .node-body-left{
      display: flex;
      align-items: center;
      position: absolute;
      height: 100%;
      svg{
        display: none;
        width: 16px;
        height: 16px;
      }
    }
    .node-body-left{
      top: 0;
      left: -16px;
    }
    &:hover{
      border-color: #1E6FFF;
      .delete-icon{
        display: flex;
      }
      .node-body-left{
        svg{
          // display: block;
        }
      }
    }
    .node-body-main{
      height: 100%;
      display: flex;
      align-items: center;
      position: relative;
      .node-body-main-header{
        padding: 5px 8px;
        .title{
          height: 100%;
          font-family: Microsoft YaHei-Regular, Microsoft YaHei;
          font-weight: 400;
          font-size: 12px;
          color: #363B4C;
          display: flex;
          align-items: center;
          svg{
            width: 14px;
            height: 14px;
            margin-right: 2px;
          }
          .name{
            flex: 1;
          }
        }
      }
    }
  }

  .node-footer{
    position: relative;
    z-index: 1;
    .btn{
      width: 100%;
      display: flex;
      height: 24px;
      padding: 18px 0 20px;
      justify-content: center;
    }
    &::before{
      content: "";
      position: absolute;
      top: 0;
      left: 50%;
      bottom: 0;
      z-index: -1;
      width: 0;
      height: 100%;
      border-left: 1px solid #C2C5CF;
    }
    .btn:active{
      box-shadow: none;
    }
  }
}
.start-item:not(.has-children-item), .end-item:not(.has-children-item){
  .node-footer{
    &::before{
      height: calc(100% - 20px);
    }
  }
}
</style>
