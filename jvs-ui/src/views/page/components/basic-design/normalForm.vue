<template>
  <formDesign
    ref="formDesign"
    :formdata="formdata"
    :jvsAppId="jvsAppId"
    :mobileCode="mobileCode"
    :designId="designId"
    :dataModelId="dataModelId"
    :designName="designName"
    :columnNameList="columnNameList"
    :fields="fields"
    :isDetail="isDetail"
    :isAddForm="isAddForm"
    :createOrigin="createOrigin"
    :isFlowable="isFlowable"
    :tableOption="tableOption"
    :flowableDom="flowableDom"
    :fineGrainedType="fineGrainedType"
    :masterTable="masterTable"
    :allTable="allTable"
    :selectDataSourceName="selectDataSourceName"
    :isFlowDesign="isFlowDesign"
    :formType="formType"
    :saveLoading="saveLoading"
    @save="save"
    @clear="clear"
    @flowChange="flowChange"
    @freshFormData="freshFormDataHandle"
    @connection="connection"
  />
</template>
<script>
import formDesign from '../../plugin/design/formDesign'
export default {
  name: 'normalForm',
  components: { formDesign },
  props: {
    // 表单结构
    formdata: {
      type: Object
    },
    jvsAppId: {
      type: String
    },
    mobileCode: {
      type: String
    },
    designId: {
      type: String
    },
    dataModelId: {
      type: String
    },
    designName: {
      type: String
    },
    // 激活表单key
    activeName: {
      type: String,
      default: ''
    },
    fields: {
      type: Array,
      default: () => {
        return []
      }
    },
    isFlowable: {
      type: Boolean,
      default: () => {
        return false
      }
    },
    // 是否为列表页详情按钮
    isDetail: {
      type: Boolean,
      default: () => {
        return false
      }
    },
    // 是否为列表页新增按钮
    isAddForm: {
      type: Boolean,
      default: () => {
        return false
      }
    },
    flowableDom: {
      type: Object,
      default: () => {
        return {}
      }
    },
    fineGrainedType: {
      type: String
    },
    columnNameList: {
      type: Array
    },
    tableOption: {
      type: Array
    },
    masterTable: {
      type: String
    },
    allTable: {
      type: Array
    },
    selectDataSourceName: {
      type: String
    },
    isFlowDesign: {
      type: Boolean
    },
    formType: {
      type: String
    },
    saveLoading: {
      type: Boolean
    },
    createOrigin: {
      type: String
    }
  },
  data () {
    return {}
  },
  methods: {
    save (data, isRuleSave) {
      this.$emit('save', data, isRuleSave)
    },
    clear () {
      this.$emit('clear')
    },
    // 修改flowable设置
    flowChange (data) {
      this.$emit('flowChange', data)
    },
    freshFormDataHandle(bool) {
      this.$emit("freshFormData", bool)
    },
    handleClear() {
      this.$refs.formDesign.qingkong()
    },
    handleSetColumn(num) {
      this.$refs.formDesign.getColumnNum(num)
    },
    connection () {
      this.$emit('connection')
    },
  }
}
</script>
<style lang="scss" scoped>
</style>
