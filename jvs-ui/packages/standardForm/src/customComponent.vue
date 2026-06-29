<template>
   <component
    :is="`el-${item.type}`"
    :clearable="['input'].includes(item.type)"
    v-bind="{ ...item.attrs }"
    v-on="{ ...item.events }"
    v-model="getValue"
    size="mini"
  >
    <template v-if="item.text">{{ $hit(item.text) }}</template>
    <template v-if="item.type === 'select'">
      <el-option
        v-for="(option, index) in item.option.list"
        :key="index"
        :label="$hit(item.option.label ? option[item.option.label] : option)"
        :value="item.option.value ? option[item.option.value] : option"
      ></el-option>
    </template>
    <template v-if="item.type === 'radio-group'">
      <el-radio
        v-for="(radio, index) in item.radio.list"
        :key="index"
        :label="item.radio.value ? radio[item.radio.value] : radio"
        v-bind="{ ...item.radio.attrs }"
        >{{
          $hit(item.radio.label
            ? radio[item.radio.label]
            : item.radio.value
            ? radio[item.radio.value]
            : radio)
        }}</el-radio
      >
    </template>
    <template v-if="item.type === 'checkbox-group'">
      <el-checkbox
        v-for="(checkbox, index) in item.checkbox.list"
        :key="index"
        :label="item.checkbox.value ? checkbox[item.checkbox.value] : checkbox"
        v-bind="{ ...item.checkbox.attrs }"
        >{{
          $hit(item.checkbox.label
            ? checkbox[item.checkbox.label]
            : item.checkbox.value
            ? checkbox[item.checkbox.value]
            : checkbox)
        }}</el-checkbox
      >
    </template>
  </component>
</template>

<script>
export default {
  name:'customComponent',
  props:{
    value:{
      typeof:Object,
      default(){return {}}
    },
    item:{
      typeof:Object,
      default(){return {}}
    },
    pramaKey:{
      type:String
    }
  },
  data() {
    return {

    };
  },
  created() {

  },
  mounted() {

  },
  computed:{
    getValue:{
      get(){
        return this.value
      },
      set(newVal){
        this.$emit('changeVal',{
          key:this.pramaKey,
          val:newVal
        })
      }
    },
  },
  watch:{
    getValue:{
      handler(newVal,oldVal){
        this.$emit('changeVal',{
          key:this.pramaKey,
          val:newVal
        })
      },
      immediate:true
    }
  },
  methods: {

  }
};
</script>

<style scoped lang="scss">

</style>
