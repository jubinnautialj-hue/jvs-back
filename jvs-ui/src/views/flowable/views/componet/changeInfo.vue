<template>
  <el-dialog
    title="提交记录"
    append-to-body
    :visible.sync="dialogVisible"
    :fullscreen="dialogWidth == '100%'"
    :width="dialogWidth"
    :before-close="handleClose">
    <div v-if="dialogVisible">
      <jvs-form :option="formOption" :formData="formData" class="show-form" :jvsAppId="jvsAppId"></jvs-form>
    </div>
  </el-dialog>
</template>
<script>
import {queryChangeInfo, queryEchoForm} from '../../api/flowable'
import {getFlowableForm} from './api'
export default {
  name: 'change-info-form',
  props: {
    jvsAppId: {
      type: String
    }
  },
  data () {
    return {
      dialogVisible: false,
      data: {
        name: '',
        formType: '',
        id: '',
        viewJson: null
      },
      selectFormItems: [], // 表单里的下拉选择项
      formData: {},
      formOption: {
        submitBtn: false,
        emptyBtn: false,
        cancal: false,
        column: []
      },
      dialogWidth: '50%'
    }
  },
  methods: {
    openDialog (info) {
      this.getFlowableFormHandle(this.jvsAppId, info.formId)
      this.queryChangeInfoHandle(info.dataModelId, info.dataId, info.dataVersion ? info.dataVersion : 1, info)
    },
    handleClose () {
      this.dialogVisible = false
      this.formData = {}
    },
    queryChangeInfoHandle (dataModelId, dataId, version) {
      // queryEchoForm
      queryChangeInfo(this.jvsAppId, dataModelId, dataId, version).then(res => {
        if(res.data && res.data.code == 0) {
          this.$set(this, 'formData', res.data.data)
          this.dialogVisible = true
          this.$forceUpdate()
        }
      })
    },
    // 获取表单结构
    getFlowableFormHandle (jvsAppId, id) {
      getFlowableForm(jvsAppId, id).then(res => {
        if(res.data.code == 0) {
          if(res.data.data.viewJson) {
            let view = JSON.parse(res.data.data.viewJson)
            this.data.viewJson = view
            this.initForm(this.data.viewJson)
          }
        }
      })
    },
    initForm (formDesign) {
      this.getSelectItem(formDesign.formdata)
      this.getFormColumn(formDesign)
    },
    // 获取select项，表单值为数组
    getSelectItem (list) {
      let temp = []
      for(let i in list) {
        for(let j in list[i].forms) {
          if(list[i].forms[j].type == 'select') {
            temp.push(list[i].forms[j].prop)
          }
        }
      }
      this.selectFormItems = temp
    },
    // 表单配置
    getFormColumn (formDesign) {
      if(formDesign.formdata && formDesign.formdata.length > 0) {
        let forms = formDesign.formdata[0].forms
        this.formOption = this.formatFormOption(forms, formDesign.formdata[0].formsetting)
        this.formOption.disabled =  true
        this.disableEachColumn(this.formOption.column)
        if(formDesign.formdata[0].formsetting.fullscreen) {
          this.dialogWidth = '100%'
        }else{
          if(formDesign.formdata[0].formsetting.popupWidth) {
            this.dialogWidth = formDesign.formdata[0].formsetting.popupWidth ? (formDesign.formdata[0].formsetting.popupWidth + '%') : '50%'
          }
        }
      }
    },
    // 格式化表单配置项
    formatFormOption (forms, formsetting) {
      let btlist = []
      let temp = {
        column: JSON.parse(JSON.stringify(forms)),
        btnSetting: btlist,
        size: formsetting.formsize,
        formAlign: formsetting.labelposition,
        labelWidth: formsetting.labelwidth + 'px',
        fullscreen: formsetting.fullscreen,
        submitBtn: false,
        emptyBtn: false,
        submitLoading: false,
        btnHide: true,
        cancal: false
      }
      for(let c in temp.column) {
        if(temp.column[c].type == 'SWITCH') {
          temp.column[c].type = 'switch'
        }
      }
      if(temp.column && temp.column.length > 0) {
        for(let i in temp.column) {
          temp.column[i].disabled = true
        }
      }
      return temp
    },
    disableEachColumn (list) {
      for(let i in list) {
        list[i].disabled = true
        if(['tab', 'step'].indexOf(list[i].type) > -1) {
          for(let c in list[i].column) {
            this.disableEachColumn(list[i].column[c])
          }
        }
        if(['tableForm', 'reportTable'].indexOf(list[i].type) > -1 && list[i].tableColumn && list[i].tableColumn.length > 0){
          list[i].addBtn = false
          list[i].delBtn = false
          this.disableEachColumn(list[i].tableColumn)
        }
        if(list[i].children && list[i].children.length > 0) {
          this.disableEachColumn(list[i].children)
        }
      }
    }
  },
}
</script>
<style lang="scss" scoped>
/deep/.show-form{
  .el-form-item{
    padding: 0 20px;
  }
}
</style>