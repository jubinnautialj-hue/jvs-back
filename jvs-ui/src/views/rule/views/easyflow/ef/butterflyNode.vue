<template>
  <div
    ref="node"
    :style="nodeContainerStyle"
    :class="nodeContainerClass" 
    @mouseup="changeNodeSite"
    @mouseover="showTool"
    @mouseenter="nodeEnter"
    @mouseleave="nodeLeave"
    @click.stop="clickNode"
  >
    <!-- 节点顶部锚点占位 -->
    <div v-if="['START', 'start'].indexOf(node.type) == -1" class="top-endpoint"></div>
    <!-- 节点类型的图标 -->
    <div class="top">
      <div class="ef-node-left-ico flow-node-drag">
        <svg aria-hidden="true" :style="(['START', 'start'].indexOf(node.type) > -1  ? 'fill: #3471ff;': '')">
          <use :xlink:href="'#' + (['START', 'start'].indexOf(node.type) > -1 ? 'icon-layers' : node.ico)"></use>
        </svg>
      </div>
      <!-- 节点名称 -->
      <div class="ef-node-text" :show-overflow-tooltip="true">{{ node.name }}</div>
      <!-- 节点状态图标 -->
      <div class="ef-node-right-ico" v-if="node.testData">
        <el-popover
          v-if="node.state === 'success'"
          placement="right"
          width="700"
          trigger="hover"
          @after-enter="showEditorFieldAnyWay('ruleParamInEditor', node.testData.parameterIn, node.testData.parameterInDesc)">
          <div class="result-box">
            <h4 style="display:flex;justify-content: space-between;align-items: center;">
              <span>执行结果</span>
              <span>
                <span v-if="node.testData.time || node.testData.time == 0" style="font-size: 13px;color:#409EFF;">{{node.testData.time}}ms</span>
                <el-button v-if="node.testData.type == 'number' || node.testData.value" icon="el-icon-document-copy" type="text" @click="copyTestJsonHandle(node.testData.value)" style="margin-left: 10px;cursor: pointer;">复制</el-button>
              </span>
            </h4>
            <div class="result-text">
              <p v-if="node.testData.type == 'number'" style="padding:10px;margin:0;">{{node.testData.value}}</p>
              <!-- <img v-else-if="isImage(node.testData.value)" :src="node.testData.value" alt="" style="width: 100%;height: 160px;display: block;"> -->
              <json-viewer v-else :value="node.testData.value || ''"></json-viewer>
            </div>
            <h4 v-if="node.testData.parameterIn || node.testData.parameterIn == ''" style="display:flex;justify-content: space-between;align-items: center;">
              <span>执行入参</span>
              <span>
                <el-button icon="el-icon-document-copy" type="text" @click="copyTestJsonHandle(node.testData.parameterIn)" style="margin-left: 10px;cursor: pointer;">复制</el-button>
              </span>
            </h4>
            <div v-if="node.testData.parameterIn || node.testData.parameterIn == ''" class="result-text">
              <div class="rule-param-json-editor">
                <jsonEditor ref="ruleParamInEditor" lang="json" :disabled="true" :code="formatJson(node.testData.parameterIn)" prop="parameterIn" :showPrintMargin="node.testData.parameterInDesc ? true : false"></jsonEditor>
              </div>
            </div>
            <h4 v-if="node.testData.parameter || node.testData.parameter == ''" style="display:flex;justify-content: space-between;align-items: center;">
              <span>执行变量</span>
            </h4>
            <div v-if="node.testData.parameter || node.testData.parameter == ''" class="result-text">
              <json-viewer :value="node.testData.parameter || ''"></json-viewer>
            </div>
          </div>
          <i slot="reference" class="el-icon-circle-check el-node-state-success"></i>
        </el-popover>
        <el-popover
          v-if="node.state === 'error'"
          placement="right"
          width="700"
          trigger="hover"
          @after-enter="showEditorFieldAnyWay('ruleParamInEditor', node.testData.parameterIn, node.testData.parameterInDesc)">
          <div class="result-box">
            <h4 v-if="node.testData.parameterIn || node.testData.parameterIn == ''" style="display:flex;justify-content: space-between;align-items: center;">
              <span>执行入参</span>
              <span>
                <el-button icon="el-icon-document-copy" type="text" @click="copyTestJsonHandle(node.testData.parameterIn)" style="margin-left: 10px;cursor: pointer;">复制</el-button>
              </span>
            </h4>
            <div v-if="node.testData.parameterIn || node.testData.parameterIn == ''" class="result-text">
              <!-- <json-viewer :value="node.testData.parameterIn || ''"></json-viewer> -->
              <div class="rule-param-json-editor">
                <jsonEditor ref="ruleParamInEditor" lang="json" :disabled="true" :code="formatJson(node.testData.parameterIn)" prop="parameterIn" :showPrintMargin="node.testData.parameterInDesc ? true : false"></jsonEditor>
              </div>
            </div>
            <h4 style="display:flex;justify-content: space-between;align-items: center;">
              <span>执行结果</span>
              <span>
                <span v-if="node.testData.time || node.testData.time == 0" style="font-size: 13px;color:#409EFF;">{{node.testData.time}}ms</span>
                <el-button v-if="node.testData.type == 'number' || node.testData.value" icon="el-icon-document-copy" type="text" @click="copyTestJsonHandle(node.testData.value)" style="margin-left: 10px;cursor: pointer;">复制</el-button>
              </span>
            </h4>
            <div v-if="!(node.testData.value === undefined)" class="result-text">
              <p v-if="node.testData.type == 'number'" style="padding:10px;margin:0;">{{node.testData.value}}</p>
              <!-- <img v-else-if="isImage(node.testData.value)" :src="node.testData.value" alt="" style="width: 100%;height: 160px;display: block;"> -->
              <json-viewer v-else :value="node.testData.value || ''"></json-viewer>
            </div>
            <h4>错误信息</h4>
            <div class="result-text">
              <!-- <section v-html="node.testData"></section> -->
              <el-input
                type="textarea"
                :rows="8"
                disabled
                v-model="node.testData.errorMessage">
              </el-input>
            </div>
          </div>
          <i slot="reference" class="el-icon-circle-close el-node-state-error"></i>
        </el-popover>
        <i v-if="node.state === 'warning'" class="el-icon-warning-outline el-node-state-warning"></i>
        <i v-if="node.state === 'running'" class="el-icon-loading el-node-state-running"></i>
      </div>
    </div>
    <!-- 锚点 -->
    <!-- <div v-if="!disabled && !(node && node.data && node.data.functionName == '提示消息')" class="ef-node-bottom-point flow-node-drag"></div> -->
    <div v-show="!disabled && !(node && node.data && node.data.functionName == '提示消息')" class="source-endpoint" :id="`source${node.id}`">
      <div class="endpoint"></div>
    </div>
    <div class="target-endpoint" :id="`target${node.id}`">
      <div class="endpoint"></div>
    </div>
    <!-- 浮动工具 -->
    <div v-if="!disabled && ['START', 'start'].indexOf(node.type) == -1" v-show="toolShow && moveCount == 0" class="tool-bar" @mouseleave="hideTool">
      <div class="tool-bar-list">
        <el-popover v-if="node && node.data && node.data.explain" placement="top" trigger="hover" popper-class="custom-right-tool-poper">
          <div v-html="node.data.explain" style="max-width: 256px;"></div>
          <span slot="reference">
            <svg class="normal-svg" aria-hidden="true">
              <use xlink:href="#icon-jvs-a-miaoshu1x"></use>
            </svg>
            <svg class="active-svg" aria-hidden="true">
              <use xlink:href="#icon-jvs-a-miaoshu-xuanzhong1x"></use>
            </svg>
          </span>
        </el-popover>
        <span v-if="['循环容器'].indexOf(node.data.functionName) == -1" @click.stop="copyNode(node)">
          <svg class="color-svg" aria-hidden="true">
            <use xlink:href="#jvs-ui-icon-fuzhi1"></use>
          </svg>
        </span>
        <span @click.stop="delNode(node)">
          <svg class="color-svg" aria-hidden="true">
            <use xlink:href="#jvs-ui-icon-shanchuyonghu"></use>
          </svg>
        </span>
      </div>
    </div>
  </div>
</template>

<script>
import eventBus from "../../../utils/eventBus"
import jsonEditor from '@/components/basic-assembly/jsonEditor'
export default {
  name: 'flow-node',
  components: {
    jsonEditor
  },
  props: {
    // 这里可以拿到mockdata里的当前节点的数据
    itemData: {
      type: Object,
    },
    // 原生的节点数据（不推荐使用这个）
    canvasNode: {
      type: Object
    }
  },
  computed: {
    node () {
      return this.itemData.node
    },
    disabled () {
      return this.itemData.disabled
    },
    nodeContainerClass () {
      return {
        'jvs-rule-node': true,
        'ef-node-container': true,
        // 'ef-node-container-start': this.node.type === 'START' || this.node.type === 'END',
        // 'ef-node-container-judge': this.node.type === 'JUDGE',
        // 'ef-node-container-normal': this.node.type === 'VARIABLE' || this.node.type === 'STRATEGY',
        'ef-node-active': this.isClickCanvas > -1 ? false : (this.activeElement.type=='node'? this.activeElement.nodeId === this.node.id : false),
        'jvs-rule-node-selected': (this.selectedNodeIds && this.selectedNodeIds.indexOf(this.node.id) > -1)
      }
    },
    // 节点容器样式
    nodeContainerStyle () {
      return {
        top: this.node.top,
        left: this.node.left
      }
    },
    nodeIcoClass () {
      var nodeIcoClass={}
      if(this.node.ico){
        if(!this.node.ico.startsWith('<')) {
          nodeIcoClass[this.node.ico]=true
        }
      }
      if(this.node.ico.startsWith('<') || this.node.ico.startsWith('http')) {
        nodeIcoClass['svgIcon']=true
      }
      // 添加该class可以推拽连线出来，viewOnly 可以控制节点是否运行编辑
      nodeIcoClass['flow-node-drag']=this.node.viewOnly? false:true
      return nodeIcoClass
    }
  },
  data () {
    return {
      toolShow: false,
      activeElement: {},
      isClickCanvas: -1,
      selectedNodeIds: [],
      moveCount: 0, // 节点是否在移动
    }
  },
  mounted() {
    eventBus.$on('setData', this.setData)
  },
  onUnmounted() {
    eventBus.$off('setData', this.setData)
  },
  methods: {
    nodeEnter() {
      if(this.activeElement && this.activeElement.nodeId) return
      eventBus.$emit('setLineColor', 'highLight', this.canvasNode)
    },
    nodeLeave() {
      if(this.activeElement && this.activeElement.nodeId) return
      eventBus.$emit('setLineColor', 'unHighlight', this.canvasNode)
    },
    setData (key, value) {
      this[key] = value
      this.$forceUpdate()
    },
    // 点击节点
    clickNode () {
      eventBus.$emit('clickNode', this.node)
    },
    // 鼠标移动后抬起
    changeNodeSite () {
      // 避免抖动
      if (this.node.left==this.$refs.node.style.left&&this.node.top==this.$refs.node.style.top) {
        return;
      }
      this.$emit('changeNodeSite', {
        nodeId: this.node.id,
        left: this.$refs.node.style.left,
        top: this.$refs.node.style.top,
      })
    },
    // 复制执行结果
    copyTestJsonHandle (result) {
      const text = document.createElement('input')
      text.value = typeof result == 'string' ? result : JSON.stringify(result)
      document.body.appendChild(text)
      text.select()
      document.execCommand('Copy')
      document.body.removeChild(text)
      this.$notify({
        title: '提示',
        message: '复制成功',
        position: 'bottom-right',
        type: 'success'
      });
    },
    isImage (value) {
      let bool = false
      if(value && value.startsWith('http')) {
        if(value.split('?')[0].includes('.')) {
          let tp = value.split('?')[0].split('.')
          if(['png', 'jpg', 'jpeg', 'git', 'PNG', 'JPG', 'JPEG', 'GIF'].indexOf(tp[tp.length - 1]) > -1) {
            bool = true
          }
        }
      }
      return bool
    },
    copyNode (node) {
      eventBus.$emit('copyNode', node) 
    },
    delNode (node) {
      eventBus.$emit('delNode', node)
    },
    showTool () {
      this.toolShow = true
    },
    hideTool () {
      this.toolShow = false
    },
    formatJson (obj) {
      let str = typeof obj == 'string' ? obj : JSON.stringify(obj)
      return str
    },
    showEditorFieldAnyWay (ref, data, desc) {
      if(this.$refs[ref]) {
        this.$refs[ref].showEditorFieldAnyWay(data, desc)
      }
    }
  }
}
</script>
<style lang="scss" scoped>
.result-box{
  padding: 0 20px;
  padding-bottom: 15px;
  h4{
    margin: 0;
    height: 32px;
    line-height: 32px;
    margin-top: 10px;
  }
  .result-text{
    margin-top: 5px;
    background: #f5f7fa;
    overflow: hidden;
    border-radius: 5px;
    max-height: 200px;
    overflow-y: auto;
    /deep/.jv-container{
      background: #f5f7fa;
      max-height: calc(100vh - 300px);
      overflow: hidden;
      overflow-y: auto;
    }
    section{
      padding: 10px;
    }
    /deep/.jv-container .jv-code{
      padding: 10px;
    }
  }
}
.ef-node-bottom-point{
  position: absolute;
  left: calc(50% - 4px);
  bottom: -6px;
  width: 6px;
  height: 6px;
  background: #fff;
  border: 2px solid #1879FF;
  border-radius: 50%;
  cursor: crosshair;
  // display: none;
}
.tool-bar{
  position: absolute;
  width: 100%;
  height: 75px;
  top: -32px;
  right: 0;
  .tool-bar-list{
    display: flex;
    align-items: center;
    justify-content: flex-end;
    span{
      display: flex;
      align-items: center;
      justify-content: center;
      width: 28px;
      height: 28px;
      background-color: #FFFFFF;
      box-shadow: 0px 4px 10px 0px rgba(0,0,0,0.3);
      border-radius: 4px;
      overflow: hidden;
      cursor: pointer;
      svg{
        width: 16px;
        height: 16px;
      }
      .normal-svg{
        display: block;
      }
      .active-svg{
        display: none;
      }
      &:hover{
        background-color: #DEEAFF;
        .normal-svg{
          display: none;
        }
        .active-svg{
          display: block;
        }
        .color-svg{
          fill: #1e6fff;
        }
      }
    }
    span+span{
      margin-left: 4px;
    }
  }
}
.rule-param-json-editor{
  width: 100%;
  height: 200px;
  background-color: #2d2d2d;
  /deep/.json-editor-entity-showPrintMargin{
    width: 500px;
    .ace_error{
      background-image: none;
    }
    .ace_scroller{
      overflow: unset;
    }
    .ace_text-layer{
      overflow: unset;
    }
  }
}
// 节点在选区中的样式
.jvs-rule-node.jvs-rule-node-selected{
  border: 2px solid #FF8636;
}
</style>
<style lang="scss">
.jvs-rule-node.ef-node-container{
  border: 2px solid #fff;
  box-sizing: border-box;
  .top-endpoint{
    position: absolute;
    left: calc(50% - 4px);
    top: -4px;
    width: 0;
    height: 0;
    border: 0;
    border-radius: 0;
    background: none;
    border-top: 8px solid #C2C5CF;
    border-left: 4px solid transparent;
    border-right: 4px solid transparent;
    border-bottom: 4px solid transparent;
    z-index: 500;
    cursor: default;
  }
  .top{
    width: 100%;
    height: 100%;
    padding: 6px;
    display: flex;
    align-items: center;
    box-sizing: border-box;
    .ef-node-left-ico{
      width: 24px;
      height: 24px;
      box-sizing: border-box;
      background: #E4EDFF;
      border-radius: 4px;
      overflow: hidden;
      display: flex;
      align-items: center;
      justify-content: center;
      svg{
        width: 14px;
        height: 14px;
      }
    }
    .ef-node-right-ico{
      display: block;
      width: 16px;
      height: 16px;
      position: relative;
      z-index: 1;
      i{
        cursor: pointer;
        font-size: 16px;
      }
    }
  }
}
.jvs-rule-node.ef-node-container:hover{
  background-color: #1E6FFF;
  .top{
    .ef-node-left-ico{
      background: rgba(255,255,255,0.5);
    }
    .ef-node-text{
      color: #fff;
    }
  }
  // .ef-node-bottom-point{
  //   display: block;
  // }
}
.jvs-rule-node.ef-node-active{
  background-color: #fff;
}
</style>
