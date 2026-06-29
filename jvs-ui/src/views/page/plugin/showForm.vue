<template>
  <div style="padding: 0 10px;">
    <jvs-form class="show-form" v-if="type == 'normal'" :formData="formData" :option="option" :designId="designId" :jvsAppId="jvsAppId" @submit="submitHandle" :isView="true" @cancalClick="$emit('close', true);">
      <!-- 自定义按钮 -->
      <template slot="formButton">
        <jvs-button size="mini" v-if="hasPrint" @click="printHandle">打印</jvs-button>
        <jvs-button size="mini" v-for="(item, index) in option.btnSetting" v-if="['submit', 'empty', 'print', 'cancel'].indexOf(item.buttonType) == -1 && item.enable" :key="item.name+'slotbtn'+index" @click="slotbtnClickHandle(item, index)">{{item.name}}</jvs-button>
      </template>
    </jvs-form>
    <jvs-form-level v-if="type == 'level'" :option="option" @submit="submitHandle"></jvs-form-level>
    <jvs-form-step v-if="type == 'step'" :option="option" @submit="submitHandle"></jvs-form-step>
    <!-- 流程表单 -->
    <div v-if="type == 'flowable'">
      <h4>提交信息</h4>
      <jvs-form :option="basicOption" @submit="submitHandle">
        <!-- 自定义按钮 -->
        <template slot="formButton">
          <jvs-button size="mini" v-for="(item, index) in basicOption.btnSetting" :key="item.name+'slotbtn'+index" @click="slotbtnClickHandle(item, index)">{{item.name}}</jvs-button>
        </template>
      </jvs-form>
      <h4>审核</h4>
      <jvs-form :option="formOption" @submit="submitHandle">
        <!-- 自定义按钮 -->
        <template slot="formButton">
          <jvs-button size="mini" v-for="(item, index) in formOption.btnSetting" :key="item.name+'slotbtn'+index" @click="slotbtnClickHandle(item, index)" v-show="item.enable">{{item.name}}</jvs-button>
        </template>
      </jvs-form>
    </div>
  </div>
</template>
<script>
export default {
  name: 'show-form',
  props: {
    type: {
      type: String,
      default: 'normal'
    },
    option: {
      type: Object,
      default: () => {
        return {}
      }
    },
    designId: {
      type: String
    },
    flowableData: {
      type: Object
    },
    formData: {
      type: Object
    },
    jvsAppId: {
      type: String
    }
  },
  data () {
    return {
      basicOption: {
        disabled: true,
        btnHide: true,
        column: []
      },
      formOption: {
        cancal: false,
        submitBtn: false,
        emptyBtn: false,
        column: []
      },
      hasPrint: false
    }
  },
  created () {
    if(this.flowableData) {
      if(this.flowableData.formsetting) {
        this.basicOption = JSON.parse(JSON.stringify(this.flowableData.formsetting))
        this.basicOption.column = []
        this.basicOption.disabled = true
        this.basicOption.btnHide = true

        this.formOption = JSON.parse(JSON.stringify(this.flowableData.formsetting))
        this.formOption.column = []
        this.formOption.submitBtn = false
        this.formOption.emptyBtn = false
        this.formOption.cancal = false
      }
      if(this.flowableData.basic) {
        this.basicOption.column = this.flowableData.basic
      }
      if(this.flowableData.form) {
        this.formOption.column = this.flowableData.form
      }
    }
    if(this.option.btnSetting) {
      for(let i in this.option.btnSetting) {
        if(this.option.btnSetting[i].buttonType == 'print' && this.option.btnSetting[i].enable) {
          this.hasPrint = true
        }
        if(this.option.btnSetting[i].buttonType == 'cancel' && this.option.btnSetting[i].enable) {
          this.option.cancal = true
          this.option.cancalBtnText = this.option.btnSetting[i].name || '取消'
        }
        if(this.option.btnSetting[i].buttonType == 'submit' && this.option.btnSetting[i].enable) {
          this.option.submitBtn = true
          this.option.submitBtnText = this.option.btnSetting[i].name || '提交'
        }
        if(this.option.btnSetting[i].buttonType == 'empty' && this.option.btnSetting[i].enable) {
          this.option.emptyBtn = true
          this.option.emptyBtnText = this.option.btnSetting[i].name || '重置'
        }
      }
    }
  },
  methods: {
    submitHandle (obj) {
      this.$emit('submit', obj)
      console.log(obj)
    },
    // 自定义按钮事件
    slotbtnClickHandle (row, index) {},
    printHandle () {
      console.log('预览打印。。。。')
    }
  }
}
</script>
<style lang="scss" scoped>
/deep/.show-form{
  .el-form-item{
    padding: 0 20px;
  }
  .el-tabs{
    .el-tab-pane{
      .el-form-item, .form-item-no-label{
        padding: 0;
        .table-form{
          .table-body-box{
            padding: 0;
          }
        }
      }
    }
  }
  .jvs-step{
    .step-bar-content{
      .el-form-item, .form-item-no-label{
        padding: 0;
        .table-form{
          .table-body-box{
            padding: 0;
          }
        }
      }
    }
  }
}
</style>
