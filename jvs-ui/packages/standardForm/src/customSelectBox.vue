<template>
  <div>
    <el-select v-model="selectVal" size="mini" :placeholder="placeholder" @change="changeVal">
        <el-option v-for="(item,index) in selectList" :key="index" :label="item[optionProp.label]" :value="item[optionProp.value]"></el-option>
    </el-select>
  </div>
</template>

<script>
import {getPublicLibrary} from '@/api'
export default {
    name:"customSelectBox",
    props:{
        value:{
            type:[String,Number],
            default:''
        },
        pramaKey:{
            type:String
        },
        optionProp:{
            type:Object,
            default(){
                return {
                    label:"label",
                    value:"value"
                }
            }
        },
        placeholder:{
            type:String,
            default:'请选择对应的内容'
        }
    },
  data() {
    return {
        selectVal:'',
        selectList:[]
    };
  },
  watch:{
    value:{
        handler(newVal){
            this.selectVal = newVal
        },
        immediate:true
    }
  },
  created() {
    this.init()
  },
  mounted() {
  },
  methods: {
    init(){
        getPublicLibrary({}).then(res=>{
            if(res.data.code==0){
                this.selectList = res.data.data
            }
        })
    },
    changeVal(){
        this.$emit("update:value",this.selectVal)
        this.$emit('changeVal',{key:this.pramaKey,val:this.selectVal},this)
    }
  }
};
</script>

<style scoped lang="scss">

</style>
