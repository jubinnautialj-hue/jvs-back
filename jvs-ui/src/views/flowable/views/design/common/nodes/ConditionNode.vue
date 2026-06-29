<template>
  <div :class="{'node': true, 'node-error-state': showError}">
    <div :class="{'node-body': true, 'error': showError}" @click="nodeClick">
      <!-- 遮罩及复制删除 -->
      <div v-if="!config.defaultCondition" class="node-back-box">
        <el-tooltip effect="light" content="复制条件" placement="top">
          <div class="copy-icon" @click.stop="$emit('copy')">
            <svg>
              <use xlink:href="#icon-jvs-fuzhi"></use>
            </svg>
          </div>
        </el-tooltip>
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
        <div :class="{'node-body-main-header': true, 'defaultCondition-header': config.defaultCondition}">
          <ellipsis class="title" hover-tip :content="config.name ? config.name : ('条件' + level)"/>
          <span class="level">优先级{{ level }}</span>
        </div>
        <div class="node-body-main-content">
          <span class="placeholder">{{ config.defaultCondition ? '其他情况进入此流程' : placeholder }}</span>
          <i class="el-icon-arrow-right"></i>
        </div>
      </div>
      <div class="node-body-right" @click.stop="$emit('rightMove')" v-if="level < size">
        <svg>
          <use xlink:href="#icon-jvs-shousuo"></use>
        </svg>
      </div>
      <div class="node-error" v-if="showError">
        <el-tooltip effect="dark" :content="errorInfo" placement="top-start">
          <i class="el-icon-warning-outline"></i>
        </el-tooltip>
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
import {ValueType} from './config'
export default {
  name: "ConditionNode",
  components: { InsertButton, ellipsis },
  props: {
    config: {
      type: Object,
      default: () => {
        return {}
      }
    },
    //索引位置
    level: {
      type: Number,
      default: 1
    },
    //条件数
    size: {
      type: Number,
      default: 0
    }
  },
  data() {
    return {
      ValueType,
      placeholder: '修改审批条件',
      errorInfo: '',
      showError: false
    }
  },
  computed: {},
  methods: {
    nodeClick () {
      this.$emit('selected')
    },
    //校验数据配置的合法性
    validate(err) {
      const props = this.config
      if (props.groups.length <= 0){
        this.showError = true
        this.errorInfo = '请设置分支条件'
        err.push(`${this.config.name} 未设置条件`)
      }else {
        for (let i = 0; i < props.groups.length; i++) {
          if (props.groups[i].cids.length === 0){
            this.showError = true
            this.errorInfo = `请设置条件组${i}内的条件`
            err.push(`条件 ${this.config.name} 条件组${i}内未设置条件`)
            break
          }else {
            let conditions = props.groups[i].condition
            for (let ci = 0; ci < conditions.length; ci++) {
              let subc = conditions[ci]
              if (subc.values.length === 0){
                this.showError = true
              }else {
                this.showError = false
              }
              if (this.showError){
                this.errorInfo = `请完善条件组${i}内的${subc.name}条件`
                err.push(`条件 ${this.config.name} 条件组${i}内${subc.name}条件未完善`)
                return false
              }
            }
          }
        }
      }
      return !this.showError
    }
  }
}
</script>

<style lang="scss" scoped>
.node-error-state {
  .node-body {
    box-shadow: 0px 0px 5px 0px #F56C6C !important;
  }
}
.node{
  padding: 58px 55px 0;
  width: 200px;
  position: relative;
  .node-body{
    width: 200px;
    height: 80px;
    padding: 14px 12px 12px 12px;
    background: #FFFFFF;
    border-radius: 4px;
    border: 1px solid #fff;
    position: relative;
    z-index: 4;
    cursor: pointer;
    box-sizing: border-box;
    .node-back-box{
      position: absolute;
      width: 100%;
      height: 112px;
      left: 0;
      top: -32px;
      z-index: 4;
      .copy-icon, .delete-icon{
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
      .copy-icon{
        right: 38px;
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
    .node-body-left, .node-body-right{
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
    .node-body-right{
      right: -16px;
      top: 0;
    }
    .node-body-main{
      .node-body-main-header{
        display: flex;
        align-items: center;
        justify-content: space-between;
        height: 18px;
        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
        font-weight: 400;
        font-size: 14px;
        .title{
          color: #14C9C9;
        }
        .level{
          color: #6F7588;
        }
      }
      .node-body-main-content{
        margin-top: 12px;
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
      .copy-icon, .delete-icon{
        display: flex;
      }
      .node-body-left, .node-body-right{
        svg{
          // display: block;
        }
      }
    }
  }
  .node-footer{
    position: relative;
    z-index: 1;
    .btn {
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
