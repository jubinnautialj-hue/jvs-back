<template>
  <el-dialog
    title="选择创建逻辑类型"
    :visible.sync="createDialog"
    append-to-body
    :before-close="handleRuleTypeClose"
    width="950px"
    class="custom-header-dialog"
    >
    <div class="rule-type-box" v-if="createDialog">
      <div class="rule-type-item" v-for="(item, key) in ruleTypes" :key="key" @click="selectedRuleType(item)">
        <img :src="item.img" alt=""/>
        <div class="item-name">{{ item.name }}</div>
        <div class="item-desc" :style="key < 3 ? 'text-align: center' : ''">{{ item.desc }}</div>
      </div>
    </div>
  </el-dialog>
</template>
<script>
import { SavaJSON } from '@/views/rule/api/rule'
let Async_logic = require('@/const/img/Async_logic.png')
let Listening_logic = require('@/const/img/Listening_logic.png')
let External_API_logic = require('@/const/img/External_API_logic.png')
let Timing_logic = require('@/const/img/Timing_logic.png')
export default {
  name: 'createRuleDialog',
  props:{
    jvsAppId: {
      type: String,
    }
  },
  data () {
    return {
      createDialog: false, // 创建逻辑弹窗开关
      ruleTypes: [
        { type: 'Source_code_development_docking_logic', img: Timing_logic, name: '事件触发', desc: '在系统内部调用,可使用表单、列表、工作流、当前用户等功能。用于逻辑复用或源码扩展二次开发使用', },
        { type: 'External_API_logic', img: External_API_logic, name: 'API触发', desc: '其它三方系统需要与平台业务数据进行链接时使用。可下载三方调用示例', },
        { type: 'Timing_logic', img: Async_logic, name: '定时触发', desc: '业务数据需要周期性执行时使用', },
        { type: 'Listening_logic', img: Listening_logic, name: '监听触发', desc: '根据消费mq的数据进行执行。常用于不实现异步传递信息', },
      ],
    }
  },
  methods: {
    init () {
      this.createDialog = true
    },
    // 选择逻辑
    selectedRuleType (obj) {
      if(['Listening_logic', 'Async_logic'].indexOf(obj.type) > -1) {
        this.$notify({
          title: '提示',
          message: '正在开发',
          position: 'bottom-right',
          type: 'warning'
        });
      }else{
        this.submitRule(obj)
      }
    },
    submitRule (info) {
      let params = {
        jvsAppId: this.jvsAppId,
        reqType: info.type
      }
      SavaJSON(this.jvsAppId, params).then(res => {
        if(res.data && res.data.code == 0) {
          this.$openUrl(`/rule-design-ui/#/ruleDesign?id=${res.data.data}&name=未命名逻辑&jvsAppId=${this.jvsAppId}`, '_blank')
          this.handleRuleTypeClose()
        }
      })
    },
    // 选择逻辑类型弹窗 关闭
    handleRuleTypeClose() {
      this.createDialog = false
    },
  }
}
</script>
<style lang="scss" scoped>
.rule-type-box{
  display: flex;
  align-items: center;
  justify-content: space-evenly;
  padding: 46px 0;
  .rule-type-item{
    border-radius: 4px;
    cursor: pointer;
    width: 180px;
    height: 250px;
    box-shadow: 0 0 5px #eee;
    //text-align: center;
    display: flex;
    flex-direction: column;
    align-items: center;
    img{
      margin: 20px 0 20px;
      width: 90px;
      height: 90px;
    }
    .item-name{
      color: #333;
      font-weight: bold;
      line-height: 26px;
    }
    .item-desc{
      width: 80%;
      color: #a2a3a5;
      line-height: 20px;
      font-size: 12px;
    }
    &:hover{
      box-shadow: 0 0 5px #ddd;
    }
  }
}
</style>