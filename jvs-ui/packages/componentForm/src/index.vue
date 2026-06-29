<template>
  <div class="component-form">
    <el-form
      v-bind="$attrs"
      :model="formData"
      :rules="formRules"
      ref="form"
      size="mini"
      label-position="top"
      class="component-form">
      <!-- <el-form-item v-if="['ChartHistogram', 'ChartLine', 'ChartPie'].indexOf(comInfo.material) > -1" label="数据来源" prop="dataSourceType">
        <el-radio-group v-model="formData.dataSourceType">
          <el-radio label="model">数据模型</el-radio>
          <el-radio label="mock">模拟数据</el-radio>
        </el-radio-group>
      </el-form-item> -->
      <!-- v-if="['ChartHistogram', 'ChartLine', 'ChartPie'].indexOf(comInfo.material) > -1 ? (formData.dataSourceType == 'model') : true" -->
      <el-form-item
        v-for="(item, key) in formAttributes"
        :label="item.label"
        :title="item.label"
        :key="key"
        :prop="item.prop"
        class="form-item-control"
      >
        <!-- select选择框 -->
        <el-select style="width: 100%" v-if="item.type === 'select'" v-model="formData[item.prop]" clearable @change="formChange(item)">
          <el-option
            v-for="(it, index) in ((item.linkProp && item.linkProp.length > 0) ? dicOrigin[item.prop] : item.dicData)"
            :key="index"
            :label="it.label"
            :value="it.value">
          </el-option>
        </el-select>
        <!-- select多选框 -->
        <el-select multiple style="width: 100%" v-if="item.type === 'selectMultiple'" v-model="formData[item.prop]" clearable @change="formChange(item)">
          <el-option
            v-for="(it, index) in ((item.linkProp && item.linkProp.length > 0) ? dicOrigin[item.prop] : item.dicData)"
            :key="index"
            :label="it.label"
            :value="it.value">
          </el-option>
        </el-select>
        <!-- search选择框 -->
        <el-popover
          v-if="item.type == 'search'"
          v-model="item.focus"
          placement="bottom-start"
          :popper-class="`el-select-dropdown search-input-select-pop ${(searchOrigin[item.prop] && searchOrigin[item.prop].length > 0) ? 'search-input-select-pop-withdata' : ''}`"
          trigger="click">
          <div class="el-select-dropdown__list">
            <div v-for="(it, index) in searchOrigin[item.prop]" :key="index" :class="{'el-select-dropdown__item': true, 'selected': it.value == formData[item.prop]}" @click="setFormValue(item, it)">
              <span>{{it.label}}</span>
            </div>
          </div>
          <div slot="reference" class="el-select" style="width: 100%;">
            <el-input  v-if="item.type === 'search'" v-model="searchKey[item.prop]" placeholder="请输入关键词进行搜索" class="search-input-select" @input="getDicUrlData(item)">
              <i slot="suffix" :class="{'el-select__caret el-input__icon el-icon-arrow-up': true, 'is-reverse': item.focus}"></i>
            </el-input>
          </div>
        </el-popover>
        <!-- input输入框 -->
        <el-input v-if="item.type === 'input'" v-model="formData[item.prop]" @change="formChange(item)"></el-input>
        <!-- input输入框 -->
        <el-input v-if="item.type === 'textarea'" v-model="formData[item.prop]" type="textarea" @change="formChange(item)"></el-input>
        <!-- radio单选框 -->
        <el-radio-group v-if="item.type === 'radio'" v-model="formData[item.prop]" @change="formChange(item)">
          <el-radio v-for="(it, index) in ((item.linkProp && item.linkProp.length > 0) ? dicOrigin[item.prop] : item.dicData)" :key="index" :label="it.value">{{ it.label && $hit(it.label) }}</el-radio>
        </el-radio-group>
        <!-- 树型 -->
        <el-cascader
          style="width: 100%;"
          v-if="item.type === 'tree'"
          v-model="formData[item.prop]"
          size="mini"
          :options="((item.linkProp && item.linkProp.length > 0) ? dicOrigin[item.prop] : item.dicData)"
          clearable
          :show-all-levels="false"
          :collapse-tags="false"
          :disabled="item.disabled"
          :props="{
            expandTrigger: 'hover',
            multiple: false,
            label: item.props ? item.props.label : 'label',
            value: item.props ? item.props.value : 'value',
            emitPath: false,
            checkStrictly: true
          }"
          @change="formChange(item)"
        >
        </el-cascader>
      </el-form-item>
      <!-- 图表类的mock数据 -->
      <!-- && formData.dataSourceType == 'mock' -->
      <el-form-item v-if="['ChartHistogram', 'ChartLine', 'ChartPie'].indexOf(comInfo.material) > -1" label="模拟数据" prop="mockData">
        <div style="height: 400px;position: relative;border-radius: 4px;overflow: hidden;">
          <jsonEditor lang="jsonArray" :code="formData.mockData" prop="mockData" @change="jsonChange"></jsonEditor>
        </div>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { getComponentDicData } from '../../../src/page/main/dynaIndex/api'
import jsonEditor from '../../../src/components/basic-assembly/jsonEditor'
export default {
  name: "ComponentForm",
  props:{
    formAttributes: {
      type: Array,
      default() {
        return []
      }
    },
    queryFormData: {
      type: Object,
      default() {
        return {}
      }
    },
    comInfo: {
      type: Object
    }
  },
  components: { jsonEditor },
  computed: {
    formRules() {
      const rules = {}
      const arr = [...this.formAttributes]
      arr.forEach(item => {
        if (item.validator) {
          item.validator.forEach(it => {
            switch (it.type) {
              case 'required':
                rules[item.prop] = [{
                  required: true,
                  validator: (rule, value, callback) => {
                    if (!this.formData[item.prop]) {
                      callback(new Error(it.message))
                    }
                    callback();
                  },
                }]
                break;
              default:break;
            }
          })
        }
      })
      return rules
    }
  },
  data() {
    return {
      formData: {},
      dicOrigin: {},
      searchKey: {},
      searchOrigin: {},
      mockDemo: {
        xAxis: {
          data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
        },
        series: [
          {
            data: [120, 200, 150, 80, 70, 110, 130],
          }
        ]
      },
      mockPie: {
        data: [
          { value: 1048, name: 'Search Engine' },
          { value: 735, name: 'Direct' },
          { value: 580, name: 'Email' },
          { value: 484, name: 'Union Ads' },
          { value: 300, name: 'Video Ads' }
        ],
      }
    }
  },
  watch: {
    formData: {
      handler(newVal, oldVal) {
        this.$emit('change', newVal)
      },
      deep: true
    },
  },
  created() {
    if(this.queryFormData) {
      this.$set(this, 'formData', JSON.parse(JSON.stringify(this.queryFormData)))
    }
    if(['ChartHistogram', 'ChartLine', 'ChartPie'].indexOf(this.comInfo.material) > -1) {
      // if(!this.formData.dataSourceType) {
      //   this.$set(this.formData, 'dataSourceType', 'model')
      // }
      if(!this.formData.mockData) {
        this.$set(this.formData, 'mockData', JSON.stringify(this.comInfo.material == 'ChartPie' ? this.mockPie : this.mockDemo))
      }
    }
    this.formAttributes.filter(item => {
      if(item.linkProp && item.linkProp.length > 0) {
        this.$set(this.dicOrigin, item.prop, [])
        this.getDicUrlData(item)
      }
      if(item.type == 'search') {
        this.$set(this.searchKey, item.prop, '')
        this.$set(this.searchOrigin, item.prop, [])
        this.getDicUrlData(item)
      }
    })
  },
  methods: {
    getFormData() {
      return this.formData
    },
    getDicUrlData (item) {
      if(this.comInfo.material && item.prop) {
        let temp = JSON.parse(JSON.stringify(this.formData))
        if(item.type == 'search') {
          temp[item.prop] = this.searchKey[item.prop]
        }
        getComponentDicData({name: this.comInfo.componentMetaData.name, type: this.comInfo.material, prop: item.prop}, temp).then(res => {
          if(res.data && res.data.code == 0 && res.data.data) {
            if(item.type == 'search') {
              this.$set(this.searchOrigin, item.prop, res.data.data)
            }else{
              this.$set(this.dicOrigin, item.prop, res.data.data)
            }
          }
        })
      }
    },
    formChange (changeItem) {
      this.formAttributes.filter(item => {
        if(item.linkProp && item.linkProp.length > 0 && item.linkProp.indexOf(changeItem.prop) > -1) {
          this.$set(this.formData, item.prop, '')
          this.getDicUrlData(item)
        }
        if(item.type == 'search') {
          if(this.searchOrigin[item.prop]) {
            this.searchOrigin[item.prop].filter(sit => {
              if(sit.value == this.formData[item.prop]) {
                this.$set(this.searchKey, item.prop, row.label && this.$hit(row.label))
              }
            })
          }
        }
      })
    },
    setFormValue (item, row) {
      this.$set(this.formData, item.prop, row.value)
      this.$set(this.searchKey, item.prop, row.label && this.$hit(row.label))
      this.$set(item, 'focus', false)
    },
    jsonChange (con, prop) {
      if(con == 'error') {
        this.$set(this.formData, prop, '')
      }else{
        this.$set(this.formData, prop, con)
      }
    },
  }
}
</script>

<style lang="scss" scoped>
.component-form{
  padding: 0 10px;
  .el-form-item--mini.el-form-item{
    margin-bottom: 10px;
  }
  .form-item-control{
  }
  .search-input-select{
    transition: all .5s;
  }
}
</style>
<style lang="scss">
.el-popover.search-input-select-pop{
  .popper__arrow{
    left: 21px!important;
  }
}
.el-popover.search-input-select-pop-withdata{
  .popper__arrow{
    left: 35px!important;
  }
}
</style>
