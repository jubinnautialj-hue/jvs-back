<template>
  <el-dialog
    class="custom-header-dialog"
    width="50%"
    :title="(formData && formData.id) ? '修改模型' : '创建模型'"
    :visible.sync="dialogVisible"
    append-to-body
    :close-on-click-modal="false"
    :before-close="handleClose">
    <div v-if="dialogVisible" class="custom-header-dialog-body-box">
      <jvs-form :formData="formData" :option="formOption" @formChange="dataModelChange" @submit="createSubmit" @cancalClick="handleClose">
      </jvs-form>
    </div>
  </el-dialog>
</template>
<script>
import { getFiledType, addDataModel, editDataModel } from '@/components/template/api'
import pinyin from 'js-pinyin'
export default {
  name: 'createDataModelDialog',
  props: {
    generateCrudDesign: {
      type: Boolean,
      default: false
    },
    menuId: {
      type: String
    },
    jvsAppId: {
      type: String
    },
  },
  components: { addDataModel },
  data () {
    return {
      dialogVisible: false,
      formData: {
        fields: []
      },
      formOption: {
        emptyBtn: false,
        formAlign: 'top',
        submitLoading: false,
        column: [
          {
            label: '名称',
            prop: 'name',
            rules: { required: true, message: '请输入名称', trigger: 'blur' },
          },
          {
            label: '字段',
            prop: 'fields',
            type: 'tableForm',
            editable: true,
            addBtn: true,
            delBtn: true,
            iconBtn: true,
            tableColumn: [
              {
                label: '显示名称',
                prop: 'fieldName',
                rules: [{ required: true, message: '请输入显示名称', trigger: 'blur' },]
              },
              {
                label: '字段名',
                prop: 'fieldKey',
                rules: [{ required: true, message: '请输入字段名', trigger: 'blur' },],
                regularExpression: '^[A-Za-z]+[A-Za-z0-9_]*$',
                regularMessage: '字段名不能包含空格和特殊符号'
              },
              {
                label: '字段类型',
                prop: 'fieldType',
                type: 'select',
                dicData: [],
                clearable: false,
                defaultValue: 'input',
                rules: [{ required: true, message: '请选择字段类型', trigger: 'change' },]
              }
            ]
          },
        ]
      },
    }
  },
  methods: {
    async init (data) {
      await this.getFieldList()
      if(data) {
        this.formData = data
      }else{
        this.formData = {
          fields: [],
        }
      }
      if(this.generateCrudDesign) {
        this.$set(this.formData, 'generateCrudDesign', this.generateCrudDesign)
      }
      if(this.menuId) {
        this.$set(this.formData, 'menuId', this.menuId)
      }
      this.dialogVisible = true
    },
    async getFieldList () {
      if(this.jvsAppId) {
        await getFiledType(this.jvsAppId).then(res => {
          if(res.data && res.data.code == 0) {
            let temp = []
            if(res.data.data) {
              res.data.data.filter(rit => {
                let key = Object.keys(rit)[0]
                temp.push({
                  label: rit[key],
                  value: key
                })
              })
              this.formOption.column.filter(col => {
                if(col.prop == 'fields') {
                  col.tableColumn.filter(tit => {
                    if(tit.prop == 'fieldType') {
                      tit.dicData = temp
                    }
                  })
                }
              })
            }
          }
        })
      }
    },
    dataModelChange () {
      if(this.formData.fields && this.formData.fields.length > 0) {
        this.formData.fields.filter(fit => {
          if(fit.fieldName && !fit.fieldKey) {
            let name = ''
            name = pinyin.getFullChars(fit.fieldName)
            name = name[0].toLowerCase() + name.slice(1, name.length)
            fit.fieldKey = name
          }
        })
      }
      this.$forceUpdate()
    },
    createSubmit () {
      if(this.jvsAppId) {
        this.formOption.submitLoading = true
        let func = addDataModel
        if(this.formData.id) {
          func = editDataModel
        }
        func(this.jvsAppId, this.formData).then(res => {
          this.formOption.submitLoading = false
          if(res.data && res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: this.formData.id ? '修改模型成功' : '创建模型成功',
              position: 'bottom-right',
              type: 'success'
            })
            if(!(this.generateCrudDesign && (!this.formData.fields || this.formData.fields.length == 0))) {
              this.$emit('success')
            }
            this.handleClose()
          }
          this.formOption.submitLoading = false
        }).catch(e => {
          this.formOption.submitLoading = false
        })
      }
    },
    handleClose () {
      this.dialogVisible = false
      this.formData = null
    }
  },
}
</script>
<style lang="scss" scoped>
/deep/.custom-header-dialog-body-box{
  height: 100%;
  overflow: hidden;
  .jvs-form{
    margin-top: 16px;
    height: calc(100% - 16px);
    .el-row{
      display: flex;
      flex-direction: column;
      height: 100%;
      .form-column-tableForm{
        flex: 1;
        overflow: hidden;
        overflow-y: auto;
      }
    }
    .el-form-item:not(.form-btn-bar) {
      margin-left: 20px;
      margin-right: 20px;
    }
    .el-form-item__label{
      font-family: Microsoft YaHei-Bold, Microsoft YaHei;
      font-weight: 700;
      font-size: 14px;
      color: #363B4C;
    }
    .form-column-tableForm{
      margin-bottom: 0;
      .el-form-item{
        .jvs-form-item{
          padding: 16px;
          border-radius: 4px;
          background: #F5F6F7;
          overflow: hidden;
        }
        .table-form{
          .el-form-item{
            margin: 0;
            .jvs-form-item{
              padding: 0;
            }
          }
          .jvs-table{
            background: transparent;
          }
          .table-body-box{
            background: transparent;
            .el-table{
              background: transparent;
              &::before{
                visibility: hidden;
              }
              .el-table__header-wrapper{
                border: 0;
                .el-table__header{
                  .headerclass{
                    th{
                      background: #F5F6F7;
                      height: 20px;
                      padding: 0;
                      line-height: 20px;
                      .cell{
                        font-family: Source Han Sans-Regular, Source Han Sans;
                        font-weight: 400;
                        font-size: 14px;
                        color: #363B4C;
                        text-align: left;
                        padding: 0;
                        padding-right: 16px;
                      }
                    }
                  }
                }
              }
              .el-table__body-wrapper{
                min-height: unset;
                .el-table__body{
                  tr{
                    background: transparent;
                    td{
                      padding: 0;
                      padding-top: 8px;
                      height: 32px;
                      line-height: 32px;
                      border: 0;
                      .cell{
                        padding: 0;
                        padding-right: 16px;
                        .el-form-item__content{
                          min-height: unset;
                          line-height: 32px;
                          .el-input{
                            height: 32px;
                            .el-input__inner{
                              height: 32px;
                              line-height: 32px;
                              background: #fff;
                            }
                          }
                          .jvs-color-picker-show-box{
                            height: 32px;
                            background: #fff;
                          }
                        }
                      }
                      &.table-index-column{
                        .cell{
                          text-indent: 10px;
                        }
                      }
                      &:nth-last-of-type(1){
                        .cell{
                          padding-right: 0;
                          text-align: center;
                        }
                      }
                    }
                    &:hover{
                      td{
                        background: none;
                      }
                    }
                  }
                }
                .el-table__empty-block{
                  display: none;
                }
              }
            }
          }
        }
      }
      .el-form-item__error{
        top: calc(100% - 18px);
        left: unset;
        right: 2px;
      }
    }
    .form-item-btn{
      border-top: 1px solid #EEEFF0;
      height: 60px;
      line-height: 60px;
      .form-btn-bar{
        height: 100%;
        margin: 0;
        margin-right: 24px;
        display: flex;
        justify-content: flex-end;
        align-items: center;
        .el-form-item__content{
          line-height: 60px;
          display: flex;
          flex-direction: row-reverse;
          .el-button{
            margin-left: 16px;
          }
        }
      }
    }
  }
}
</style>
