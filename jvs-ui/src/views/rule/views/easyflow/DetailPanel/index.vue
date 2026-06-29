<template>
  <div :class="{'detailpannel': true, 'detailpannel-trans': status !='canvas-selected'}">
    <div class="detailpannel-box">
      <div v-if="status=='node-selected'" class="pannel" id="node_detailpannel">
        <h4 class="node-text-info">
          <div class="node-text-info-item">
            <svg aria-hidden="true" :style="'width: 20px;height: 20px;margin-right: 8px;'+ (['START', 'start'].indexOf(node.node.type) > -1  ? 'fill: #3471ff;': '')">
              <use :xlink:href="'#' + (['START', 'start'].indexOf(node.node.type) > -1 ? 'icon-layers' : node.node.ico)"></use>
            </svg>
            <el-input v-if="showEditInput" size="mini" v-model="node.node.name" class="name-text"></el-input>
            <span v-else class="name-text">{{node.node.name}}</span>
            <div :class="{'edit-icon': true, 'editing': showEditInput}" @click="showEditName(!showEditInput)">
              <svg aria-hidden="true">
                <use xlink:href="#jvs-ui-icon-bianji1"></use>
              </svg>
            </div>
            <!-- <i class="el-icon-info" v-if="node && node.functionInfo && node.functionInfo.explain" @click="openExplain(node)"></i> -->
          </div>
          <el-popover placement="bottom" trigger="hover" popper-class="custom-right-tool-poper">
            <div class="explain-div" v-html="node.node.data.explain"></div>
            <svg slot="reference" aria-hidden="true" style="margin-left: 16px;cursor: pointer;fill: #C2C5CF;width: 16px;height: 16px;">
              <use xlink:href="#icon-jvs-lujing"></use>
            </svg>
          </el-popover>
        </h4>
        <div class="node-desc-info">
          <div class="label">描述</div>
          <el-input v-model="node.node.desc" type="textarea" placeholder="请填写描述" @change="changeDesc"></el-input>
        </div>
        <div class="block-container">
          <div v-if="variableItems.length > 0" class="variable-list">
            <h5>变量名称</h5>
            <el-row>
              <el-tooltip v-for="(item, index) in variableItems" :key="item.key+index" class="item" effect="dark" placement="top">
                <div slot="content">
                  <span v-if="item.description">{{item.description}}</span>
                  <br v-if="item.description" />
                  <span>{{(item.parent && item.parent.length > 0) ? (item.parent.join('.') + '.' + item.key) : item.key}}</span>
                </div>
                <el-button
                  type="primary"
                  size="mini"
                  @click="onCopy"
                  v-clipboard:copy="copyVariable(item)"
                  v-clipboard:success="onCopy"
                  v-clipboard:error="onError"
                >
                  {{item.key}}
                </el-button>
              </el-tooltip>
            </el-row>
          </div>
          <settingForm ref="settingForm" :key="node.id" :ruleInfo="ruleInfo" :info="node.node.data" :testData="node.node.testData" :activeCanvas="activeCanvas" @saveSetting="saveSettingHandle" @updateNode="updateNodeHandle" />
        </div>
      </div>
      <div v-if="status=='edge-selected'" class="pannel" id="edge_detailpannel">
        <jvs-form :option="expOption" :formData="expForm" style="padding:10px;" @submit="handleChangeExpression" @cancalClick="cancalHandle"></jvs-form>
      </div>
    </div>
  </div>
</template>

<script>
import eventBus from "../../../utils/eventBus";
import settingForm from "../../design/setting"
import { getStore } from '@/util/store'
export default {
  components: {settingForm},
  props: {
    ruleInfo: Object,
    isClickCanvas: Number,
    activeCanvas: String,
    activeEl: Object
  },
  data() {
    return {
      status: "canvas-selected",
      showGrid: false,
      page: {},
      graph: {},
      item: {},
      node: {},
      selectType: "", // 选中方式 click hover
      expForm: {},
      expOption: {
        emptyBtn: false,
        cancal: true,
        formAlign: 'top',
        column: [
          {
            label: '条件表达式',
            prop: 'expression',
            type: 'textarea',
            rules: [
              { required: false, message: '请输入表达式', trigger: 'blur' },
            ]
          }
        ]
      },
      variableItems: [], // 变量
      showEditInput: false
    };
  },
  created() {
    this.bindEvent();
    if(this.activeEl) {
      this.selectType = 'click'
      this.status = 'node-selected'
      this.item = this.activeEl.node;
      this.node = JSON.parse(JSON.stringify(this.activeEl));
    }
  },
  methods: {
    bindEvent() {
      eventBus.$on("nodeselectchange", data => {
        // butterfly处理节点数据
        let item = null
        if(data && data.options && data.options.node) {
          data.options.node.data.id = data.options.node.id
          item = data.options.node
        }
        if(item && ['START', 'END', 'start', 'end'].indexOf(item.type) == -1) {
          this.selectType = 'click'
          if(item.type == 'line') {
            if(item.label) {
              this.$set(this.expForm, 'expression', item.label)
            }else{
              this.$set(this.expForm, 'expression', "")
            }
            this.status = 'edge-selected'
          }else{
            this.status = 'node-selected'
            this.item = data.options.node
          }
          this.node = JSON.parse(JSON.stringify(data.options))
        }else{
          this.status = 'canvas-selected'
        }
        this.getVariableList()
        this.$forceUpdate()
        if (this.$refs.settingForm) {
          this.$refs.settingForm.testReset()
        }
      })
    },
    handleChangeExpression(form) {
      // 更新
      this.$set(this.node, 'label', form.expression)
      this.$emit('fresh', this.node)
      this.cancalHandle()
    },
    // 组件设置---提交
    saveSettingHandle (settingData, info) {
      let model = this.item.data
      if(!model.body) {
        model.body = {}
      }
      model.body = JSON.parse(JSON.stringify(settingData))
      model.parameters = JSON.parse(JSON.stringify(info.parameters))
      this.updateNodeHandle(model, '', 'submit')
      this.cancalHandle()
    },
    cancalHandle () {
      this.selectType = ""
      this.expForm = {}
      this.node = {}
      this.status = 'canvas-selected'
    },
    closeDetail () {
      this.cancalHandle()
    },
    // 获取变量
    getVariableList () {
      if(getStore({ name: 'variableItems' })) {
        this.variableItems = getStore({ name: 'variableItems' })
      }else{
        this.variableItems = []
      }
    },
    // 复制变量
    copyVariable (item) {
      let str = ""
      str = (item.parent && item.parent.length > 0) ? (item.parent.join('.') + '.' + item.key) : item.key
      return str
    },
    // 复制
    onCopy (e) {
      console.log(e.text)
    },
    onError (e) {
      console.log(e)
    },
    // 打开说明
    openExplain (node) {
      if(node && node.functionInfo && node.functionInfo.explain) {
        this.$openUrl(node.functionInfo.explain, '_blank')
      }
    },
    // 更新节点参数
    updateNodeHandle (node, fromType, optype) {
      this.$set(node, 'label', this.node.name)
      this.$set(node, 'name', this.node.name)
      if(optype == 'submit') {
        this.$set(this.node.node, 'data', JSON.parse(JSON.stringify(node)))
      }
      // 更新
      this.$emit('fresh', this.node, fromType)
      this.cancalHandle()
    },
    // 修改名称
    showEditName (bool) {
      this.showEditInput = bool
      if(!bool) {
        this.node.node.data.label = this.node.name
        this.node.node.data.name = this.node.name
        this.$emit('fresh', this.node, 'editName')
      }
    },
    // 修改备注
    changeDesc () {
      this.$emit('fresh', this.node, 'editDesc')
    }
  },
  watch: {
    isClickCanvas: {
      handler(newVal, oldVal){
        if(newVal > -1) {
          this.status = 'canvas-selected'
        }
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.explain-div{
  padding: 2px 6px;
  font-family: Microsoft YaHei-Regular, Microsoft YaHei;
  font-weight: 400;
  font-size: 14px;
  color: #6F7588;
  line-height: 18px;
  max-width: 300px;
}
.detailpannel {
  height: 100%;
  position: absolute;
  top: 0;
  right: 0px;
  z-index: 200;
  width: 0px;
  transition: width .3s;
  overflow: hidden;
}
.detailpannel-trans{
  width: 472px;
  background: #FFFFFF;
  box-shadow: -5px 0px 10px 0px rgba(54,59,76,0.1);
  border-radius: 4px;
  transition: width .3s;
  /deep/.setting-info{
    .form-item-btn{
      width: 472px;
      transition: width .3s;
    }
  }
}
.detailpannel-box{
  height: 100%;
  overflow: hidden;
  width: 472px;
  float: right;
  position: relative;
  .pannel{
    height: 100%;
    overflow: hidden;
    .node-text-info{
      height: 72px;
      margin: 0;
      padding: 0 24px;
      display: flex;
      align-items: center;
      box-sizing: border-box;
      .node-text-info-item{
        width: 384px;
        height: 32px;
        padding: 0 8px;
        background: #F5F6F7;
        border-radius: 4px;
        display: flex;
        align-items: center;
        box-sizing: border-box;
        .name-text{
          flex: 1;
          font-family: Microsoft YaHei-Regular, Microsoft YaHei;
          font-weight: 400;
          font-size: 14px;
          color: #363B4C;
          /deep/.el-input__inner{
            padding: 0;
            border: 0;
            height: 32px;
            line-height: 32px;
            background: transparent;
          }
        }
        .edit-icon{
          margin-left: 8px;
          width: 24px;
          height: 24px;
          border-radius: 4px;
          display: flex;
          align-items: center;
          justify-content: center;
          cursor: pointer;
          svg{
            width: 16px;
            height: 16px;
            fill: #1E6FFF;
          }
          &.editing{
            background-color: #1E6FFF;
            svg{
              fill: #fff;
            }
          }
        }
      }
    }
    .node-desc-info{
      padding: 0 24px;
      border-bottom: 1px solid #EEEFF0;
      padding-bottom: 20px;
      .label{
        font-family: Source Han Sans-Bold, Source Han Sans;
        font-weight: 700;
        font-size: 14px;
        color: #363B4C;
        line-height: 18px;
        padding-bottom: 8px;
      }
      /deep/.el-textarea{
        .el-textarea__inner{
          border: 0;
          background: #F5F6F7;
          max-height: 52px;
        }
      }
    }
  }
}
.detailpannel .block-container {
  margin-top: 16px;
  padding: 0 24px;
  padding-right: 20px;
  height: calc(100% - 88px - 94px);
  box-sizing: border-box;
  overflow: hidden;
  overflow-y: auto;
}
</style>
