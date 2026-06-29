<template>
  <el-form
    v-bind="$attrs"
    :model="formData"
    :rules="formRules"
    ref="form"
    size="mini"
    label-position="top"
    class="standard-form">
    <el-form-item
      v-for="(item, key) in filterFormConf"
      :label="item.label && $hit(item.label)"
      :title="item.label && $hit(item.label)"
      :key="key"
      :prop="key"
      class="form-item-control"
      :style="[item.tips && 'padding-right: 30px', item.formItemStyle]"
      :class="item.cormItemClass"
    >
      <template v-if="typeLimit.includes(item.type)">
        <div v-if="['textarea', 'listMap'].indexOf(item.type) > -1" style="width: 100%;">
          <!-- textarea输入框 -->
          <el-input v-if="item.type === 'textarea'" type="textarea" v-model="formData[key]" :placeholder="item.attrs['placeholder']"></el-input>
          <!-- listMap -->
          <listMap v-if="item.type == 'listMap'" :form="formData" :item="item" :prop="key" @change="setChangeVal"></listMap>
        </div>
        <customComponent v-else :item="item" v-model="formData[key]" :pramaKey="key" @changeVal="changeVal" size="mini"></customComponent>
      </template>
      <template v-if="item.slot">
        <slot v-if="!isFn(item.slot)" :name="item.slot"></slot>
        <JsxRender v-if="typeof item.slot === 'function'" :render="item.slot" @changeVal="changeVal" :key="key"></JsxRender>
      </template>
      <template v-if="item.tips">
        <el-tooltip
          effect="dark"
          :show-after="200"
          popper-class="tools-tips"
          :content="$hit(item.tips)"
          placement="bottom">
          <div class="form-item-tips">
            <Icon name="infomation" size="20" />
          </div>
        </el-tooltip>
      </template>
    </el-form-item>
  </el-form>
</template>

<script>
import customComponent from './customComponent'
import JsxRender from './jsx-render.vue'
import listMap from './listMap'
export default {
  name:'StandardForm',
  props: {
    formConf: {
      type: Object,
      default: () => {}
    },
    formData: {
      type: Object,
      default: () => {
        return {}
      }
    }
  },
  components:{customComponent, JsxRender, listMap},
  data() {
    return {
        typeLimit: [
          'input',
          'textarea',
          'input-number',
          'select',
          'switch',
          'radio',
          'radio-group',
          'checkbox',
          'checkbox-group',
          'button',
          'button-group',
          'time-select',
          'date-picker',
          'color-picker',
          'listMap'
        ]
    };
  },
  computed: {
    filterFormConf(){
      const newConf = {}
      Object.keys(this.formConf).map((key) => {
        if (typeof this.formConf[key].when === 'function') {
          if (this.formConf[key].when(this.formData)) {
            newConf[key] = this.formConf[key]
          }
        } else {
          newConf[key] = this.formConf[key]
        }
      })
      return newConf
    },
    formRules() {
      const rules = {}
      Object.keys(this.formConf).map((key) => {
        if (this.formConf[key].rules) {
          rules[key] = this.formConf[key].rules
        }
      })
      return rules
    }
  },
  watch:{},
  created() {

  },
  mounted() {
    const { validate, validateField, resetFields, clearValidate } = this.$refs.form
    const obj = { validate, validateField, resetFields, clearValidate }
    Object.keys(obj).map((key) => {
      this[key] = obj[key]
    })
  },
  methods: {
    changeVal(val){
      // this.$set(this.formData,val.key,val.val)
      this.$emit("changeFormData",val)
      // 切换的时候清除提示
      this.$nextTick(()=>{
        this.$refs.form.clearValidate()
      })
      // this.$forceUpdate()
    },
    isFn(value) {
      return typeof value === 'function'
    },
    setChangeVal (list, prop) {
      this.$set(this.formData, prop, list)
    }
  }
};
</script>

<style scoped lang="scss">

.form-item-control {
  position: relative;
  padding-right: 30px;
  box-sizing: border-box;
  margin-bottom: 16px;
  .el-form-item__label{}
  /deep/.el-form-item__content{
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    flex: 1;
    min-height: 32px;
  }
}
:deep(.form-item-tips) {
  position: absolute;
  top: 8px;
  // top: 50%;
  // transform: translateY(-50%);
  right: -24px;
  height: 28px;
  display: flex;
  align-items: center;
  font-size: 20px;
  cursor: pointer;
  color: $color-grey4;
  width: 20px;
  height: 20px;
}
:deep(.block-radio-group .el-radio) {
  line-height: 30px;
  display: block;
}
:deep(.el-radio){
  display: flex;
  align-items: center;
}
:deep(.custom-radio-inline-flex){
  .el-radio-group{
    display: inline-flex;
  }
}
 .standard-form{
   padding: 0 20px;
   //padding-right: 30px;
   //box-sizing: border-box;
   .el-form-item--mini.el-form-item{
     margin-bottom: 10px;
   }
 }
</style>
