<template>
  <el-drawer
    class="busines-association"
    :with-header="false"
    append-to-body
    style="z-index: 999999999"
    title=""
    :visible.sync="drawerVisible"
  >
    <div class="busines-association-list" v-if="drawerVisible">
      <h3>
        <span>
          <b></b><span>配置规则</span>
        </span>
        <i class="el-icon-close" @click="close"></i>
      </h3>
      <el-tabs v-model="activeName">
        <el-tab-pane v-for="(item, index) in list" :key="item.buttonType+'-'+index" :label="item.name" :name="item.name" v-if="['empty', 'print'].indexOf(item.buttonType) == -1">
          <div v-if="item.association">
            <el-card :class="{'box-card': true, 'box-card-hide': !it.active}" shadow="always" v-for="(it, ix) in item.association" :key="'association-item-'+index+'-'+ix">
              <div slot="header" class="clearfix">
                <span>{{getItemName(it)}}</span>
                <i class="el-icon-close" style="float: right;" @click="deleteOne(ix, item.association)"></i>
                <i :class="{'el-collapse-item__arrow': true, 'el-icon-arrow-right': !it.active, 'el-icon-arrow-down': it.active}" style="float: right;" @click="showhide(it)"></i>
              </div>
              <div class="content">
                <jvs-form :option="formOption" :formData="it">
                  <template slot="dataModelIdForm">
                    <el-select
                      style="width:100%"
                      v-model="it.dataModelId" placeholder="请选择数据模型" size="mini"
                      @change="changeModelPropList(it.dataModelId, ix, it)"
                      filterable
                      clearable
                    >
                      <el-option
                        v-for="dmit in dataModelList"
                        :key="dmit.id"
                        :label="dmit.name"
                        :value="dmit.id">
                      </el-option>
                    </el-select>
                  </template>
                  <template slot="conditionsForm">
                    <div v-if="it.dataModelId">
                      <el-row v-for="(cit, cix) in it.conditions" :key="'condition-item-'+index+'-'+ ix + '-' + cix" class="condition-item">
                        <el-select v-model="cit.prop" placeholder="请选择表单字段" size="mini">
                          <el-option  v-for='(item,index) in domList' :key="item.prop + '-data-filter-' + index" :label="item.label" :value="item.prop">
                            <span style="float: left">{{ item.label }}</span>
                            <span style="float: right; color: #8492a6; font-size: 13px">{{ item.prop}}</span>
                          </el-option>
                        </el-select>
                        <span class="oprator">等于</span>
                        <el-select v-model="cit.fieldKey" placeholder="请选择模型字段" size="mini">
                          <el-option label="空" value="empty"></el-option>
                          <el-option
                            v-for="(mfit, mfix) in modelFieldArray[item.name][ix]"
                            :key="'field-item-' + mfit.fieldKey + '-' + index + '-' + ix + '-' + cix + '-' + mfix"
                            :label="mfit.fieldName"
                            :value="mfit.fieldKey">
                          </el-option>
                        </el-select>
                        <i class="el-icon-delete" @click="deleteCondi(cix, it.conditions)"></i>
                      </el-row>
                      <el-button size="mini" @click="addCondiItem(it.conditions)">添加</el-button>
                    </div>
                  </template>
                  <template slot="fieldListForm">
                    <div v-if="it.dataModelId">
                      <el-row v-for="(cit, cix) in it.fieldList" :key="'field-item-'+index+'-'+ ix + '-' + cix" class="condition-item filed-list-item">
                        <el-select v-model="cit.fieldKey" placeholder="请选择模型字段" size="mini">
                          <el-option
                            v-for="(mfit, mfix) in modelFieldArray[item.name][ix]"
                            :key="'field-item-' + mfit.fieldKey + '-' + index + '-' + ix + '-' + cix + '-' + mfix"
                            :label="mfit.fieldName"
                            :value="mfit.fieldKey">
                          </el-option>
                        </el-select>
                        <span class="oprator">等于</span>
                        <el-select v-model="cit.type" placeholder="请选择" size="mini">
                          <el-option label="表单字段" value="prop"></el-option>
                          <el-option label="公式" value="formula"></el-option>
                          <el-option label="自定义" value="value"></el-option>
                          <el-option label="空" value="empty"></el-option>
                        </el-select>
                        <el-button v-if="cit.type == 'formula'" size="mini" style="margin-left: 10px;" :disabled="!cit.fieldKey" @click="openFormula(cit, modelFieldArray[item.name][ix])">配置公式</el-button>
                        <el-select v-if="cit.type == 'prop'" v-model="cit.prop" placeholder="请选择表单字段" size="mini" style="margin-left: 10px;">
                          <el-option  v-for='(item,index) in domList' :key="item.prop + '-data-filter-' + index" :label="item.label" :value="item.prop">
                            <span style="float: left">{{ item.label }}</span>
                            <span style="float: right; color: #8492a6; font-size: 13px">{{ item.prop}}</span>
                          </el-option>
                        </el-select>
                        <el-input v-if="cit.type == 'value'" size="mini" v-model="cit.value" class="value-input"></el-input>
                        <i class="el-icon-delete" @click="deleteAssoction(cix, it.fieldList)"></i>
                      </el-row>
                      <el-button size="mini" @click="addFieldItem(it.fieldList)">添加</el-button>
                    </div>
                  </template>
                </jvs-form>
              </div>
            </el-card>
          </div>
          <el-button size="mini" type="primary" @click="addAssociationItem(item)" style="margin-top:10px;"><i class="el-icon-plus"></i>添加规则</el-button>
        </el-tab-pane>
      </el-tabs>
    </div>
  </el-drawer>
</template>
<script>
import { getAllModel, getModelAllFields } from '@/components/template/api'
export default {
  props: {
    jvsAppId: {
      type: String
    },
    designId: {
      type: String
    },
    domList: {
      type: Array,
      default: () => {
        return []
      }
    },
    list: {
      type: Array
    }
  },
  data() {
    return {
      activeName: '',
      drawerVisible: false,
      dataModelList: [],
      formOption: {
        btnHide: true,
        formAlign: 'top',
        column: [
          {
            label: '业务类型',
            prop: 'ruleType',
            type: 'select',
            dicData: [
              {label: '新增', value: 'add'},
              {label: '修改', value: 'edit'},
              {label: '删除', value: 'delete'}
            ]
          },
          {
            label: '目标模型',
            prop: 'dataModelId',
            formSlot: true
          },
          {
            label: '数据会按照如下条件进行判断',
            prop: 'conditions',
            formSlot: true,
            displayExpress: [
              {prop: 'ruleType', value: 'edit'}
            ]
          },
          {
            label: '数据会按照如下条件进行判断',
            prop: 'conditions',
            formSlot: true,
            displayExpress: [
              {prop: 'ruleType', value: 'delete'}
            ]
          },
          {
            label: '赋值',
            prop: 'fieldList',
            formSlot: true,
            displayExpress: [
              {prop: 'ruleType', value: 'add'}
            ]
          },
          {
            label: '修改值',
            prop: 'fieldList',
            formSlot: true,
            displayExpress: [
              {prop: 'ruleType', value: 'edit'}
            ]
          }
        ]
      },
      modelFieldArray: {}
    };
  },
  methods: {
    init () {
      if(this.list && this.list.length > 0) {
        this.activeName = this.list[0].name
        this.list.filter(li => {
          if(!this.modelFieldArray[li.name]) {
            this.$set(this.modelFieldArray, li.name, [])
          }
          if(li.association) {
            li.association.filter((asi, asix) => {
              if(asi.dataModelId && !this.modelFieldArray[li.name][asix]) {
                this.changeModelPropList(asi.dataModelId, asix)
              }
            })
          }
        })
      }
      this.getDataModelList()
      this.drawerVisible = true
    },
    close () {
      this.drawerVisible = false
    },
    // 添加规则
    addAssociationItem (item) {
      if(!item.association) {
        this.$set(item, 'association', [])
      }
      let tp = JSON.parse(JSON.stringify(item.association))
      tp.push({
        active: true,
        conditions: [],
        fieldList: []
      })
      this.$set(item, 'association', tp)
      this.modelFieldArray[this.activeName].push([])
      this.$forceUpdate()
    },
    // 展开 / 收起
    showhide (it) {
      this.$set(it, 'active', it.active ? false : true)
      this.$forceUpdate()
    },
    // 删除
    deleteOne (index, arr) {
      arr.splice(index, 1)
      this.modelFieldArray[this.activeName].splice(index, 1)
      this.$forceUpdate()
    },
    getItemName (it) {
      let str = '规则名称'
      if(it.ruleType && it.dataModelId) {
        this.formOption.column[0].dicData.filter(item => {
          if(item.value == it.ruleType) {
            str = item.label
          }
        })
        this.dataModelList.filter(item => {
          if(item.id == it.dataModelId) {
            str += ('：' + item.name)
          }
        })
      }
      return str
    },
    // 添加条件
    addCondiItem (list) {
      list.push({})
      this.$forceUpdate()
    },
    // 删除条件
    deleteCondi (index, arr) {
      arr.splice(index, 1)
      this.$forceUpdate()
    },
    // 添加设置值
    addFieldItem (list) {
      list.push({type: 'prop'})
      this.$forceUpdate()
    },
    // 删除设置值
    deleteAssoction (index, arr) {
      arr.splice(index, 1)
      this.$forceUpdate()
    },
    // 获取当前应用所有模型列表
    getDataModelList () {
      getAllModel(this.jvsAppId).then(res => {
        if(res.data && res.data.code == 0) {
          this.dataModelList = res.data.data
        }
      })
    },
    changeModelPropList (id, index, item) {
      if(id) {
        getModelAllFields(this.jvsAppId, id, this.designId).then(res => {
          if(res.data && res.data.code == 0) {
            this.$set(this.modelFieldArray[this.activeName], index, res.data.data)
          }
        })
      }else{
        this.$set(this.modelFieldArray[this.activeName], index, [])
      }
      if(item) {
        this.$set(item, 'conditions', [])
        this.$set(item, 'fieldList', [])
      }
    },
    // 配置公式
    openFormula (item, list) {
      let name = ''
      list.filter(di => {
        if(di.fieldKey == item.fieldKey) {
          name = di.fieldName
        }
      })
      this.$setFormula({
        title: '公式设置',
        hasModal: true,
        label: name,
        execId: item.formula ? item.formula : '',
        apiPrefix: 'jvs-design',
        useCase: 'formItemValue.dataItemValue',
        props: {
          jvsAppId: this.jvsAppId,
          designId: this.designId,
          businessId: item.fieldKey
        },
        afterSave: (dialog, data) => {
          if(data && data.id) {
            this.$set(item, 'formula', data.id)
            this.$set(item, 'formulaContent', data.body)
          }
          dialog.handleClose()
        }
      })
    },
  }
};
</script>
<style lang="scss" scoped>
.busines-association-list{
  padding: 15px 20px;
  h3{
    height: 20px;
    line-height: 20px;
    color: #333333;
    margin: 0;
    padding: 0;
    font-weight: normal;
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
    span{
      display: flex;
      align-items: center;
      b{
        content: "";
        display: inline-block;
        width: 4px;
        height: 20px;
        background: #3471FF;
        margin-right: 15px;
        font-size: 20px;
      }
    }
    i{
      cursor: pointer;
    }
  }
  .box-card{
    margin: 0 10px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, .1);
    border: 1px solid #EBEEF5!important;
    margin-top: 10px;
    /deep/.el-card__header{
      padding: 8px 15px;
      .clearfix{
        i{
          cursor: pointer;
        }
      }
    }
    .content{
      .condition-item{
        margin-bottom: 10px;
        display:flex;
        justify-content:space-between;
        align-items:center;
        .oprator{
          display: block;
          width: 50px;
          text-align: center;
        }
        .el-icon-delete{
          cursor: pointer;
          margin-left: 10px;
        }
        .el-select{
          flex: 1;
        }
      }
      .condition-item::before, .condition-item::after{
        display: none;
      }
      .filed-list-item{
        .value-input{
          margin-left: 10px;
        }
        .el-select, .el-button, .value-input{
          width: calc(33% - 28px);
        }
      }
    }
  }
  .box-card-hide{
    /deep/ .el-card__body{
      display: none;
    }
  }
  .box-card:nth-of-type(1) {
    margin-top: 0;
  }
}
</style>
