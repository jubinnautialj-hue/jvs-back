<template>
  <!-- :title="editId ? $hit('编辑组件') : $hit('添加卡片')" -->
   <custom-dialog
    :value.sync="dialogVisible"
    width="min(600px, 98vw)"
    height="min(600px, 90vh)"
    @close="close">
    <div class="material-config-tab">
      <div v-if="(formAttributes && formAttributes.length > 0) || JSON.stringify(componentFormConf) !== '{}'" :class="{'tab': true, 'active': activeTab == 'data'}" @click="activeTab='data';">数据</div>
      <div :class="{'tab': true, 'active': activeTab == 'style'}" @click="activeTab='style';">样式</div>
    </div>
    <div :key="editId" class="material-config-wapper">
      <div v-show="activeTab == 'data'" class="form-wrapper scrollbar1">
        <ComponentForm ref="componentForm" :formAttributes="formAttributes" :queryFormData="queryFormData" :comInfo="formData" @change="updateComForm"></ComponentForm>
      </div>
      <div v-show="activeTab == 'style'" class="form-wrapper scrollbar1" v-if="formData.componentSetting">
        <!-- <CustomForm ref="customForm" :formAttributes="customAttributes" :customFormData="formData.componentSetting" @removeBanner="removeBanner" @addBanner="addBanner" @changeFormData="changeCustomFormData"></CustomForm> -->
        <CustomForm :componentSetting="formData.componentSetting" :id="component.i" :itemInfo="formData" @update="updateHandle"></CustomForm>
      </div>
      <div v-show="activeTab == 'data'" v-if="JSON.stringify(componentFormConf) !== '{}'">
        <div class="form-wrapper scrollbar1" v-if="formData.componentSetting">
          <StandardForm
            :formData="formData.componentSetting"
            :formConf="componentFormConf"
            @changeFormData="changeFormData"
            ref="form"
            label-width="100px"
          ></StandardForm>
        </div>
      </div>
    </div>
    <template #footer>
      <div v-if="!editId" class="footer" style="text-align: right; padding: 12px">
        <el-button class="dynamic-button btn-primary" type="button" @click="submit">{{'确认'}}</el-button>
        <el-button class="dynamic-button" type="button" @click="close">{{'取消'}}</el-button>
      </div>
    </template>
   </custom-dialog>
</template>

<script>
import { uid,clone } from '@/utils'
import Setting from '../../materials/setting'
import {getCurrentComponentOptions} from "@/page/main/dynaIndex/api";
import axios from "axios"
export default {
  name:"materialConfig",
  props:{
    isLock:{
        type:Boolean,
        default:false
    },
    reqUrl:{
      type:String,
      default(){
        return ''
      }
    },
    componentList: {
      type: Array,
      default() {
        return []
      }
    }
  },
  data() {
    return {
      customAttributes:[], // 自定义表单配置
      customAttributesTemp:[], // 自定义表单配置
      formAttributes:[], // 动态表单配置
      editId:'',
      dialogVisible:false,
      formData:{},
      options:{},
      DEFAULT_SETTING:{
        position: 1,
        affixInfo: {
          mode: 1,
          x: 10,
          y: 10
        },
        w: 12,
        h: 2,
        background: 'transparent',
        backgroundFilter: 'brightness(0.8)',
        material: 'Label',
        borderRadius: 4,
        boxShadow: '',
        zIndex: 10,
        customId: ''
      },
      componentFormConf:{},
      component: {}, // 当前选择组件
      queryFormData: {}, // 组件配置表单
      activeTab: '',
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
    };
  },
  created() {
    this.$set(this.$data,'formData',this.DEFAULT_SETTING)
  },
  methods: {
    // 获取动态表单配置
    getFormAttributes(material) {
      this.componentList.forEach(item => {
        if (item.type === material) {
          this.formAttributes = item.formAttributes
        }
      })
    },
    open (item = {}) {
      this.queryFormData = null
      if(item.queryParams) {
        let obj = {}
        item.queryParams.forEach(it => {
          obj[it.prop] = it.value
        })
        this.queryFormData = JSON.parse(JSON.stringify(obj))
      }
      this.component = JSON.parse(JSON.stringify(item))
      
      this.getFormAttributes(item.material)
      // 新增直接创建
      if(!item.i) {
        this.submit()
      }
      // 直接新增组件，无需配置表单
      if(['StepCard', 'QuickNavigation', 'MediaCards'].indexOf(item.material) === -1) {
        this.$set(this.$data,'dialogVisible',true)
      }else{
        this.submit()
      }
      if(item.i) {
        this.editId = item.i
        this.formData = {
          ...JSON.parse(JSON.stringify(item))
        }
        this.customAttributesTemp = this.formData.customAttributes ? this.formData.customAttributes : clone(Setting[this.formData.material].customAttributes)
        this.updateComponentSetting(true)
      }else{
        this.editId = ''
        this.formData = {
          ...JSON.parse(JSON.stringify(this.DEFAULT_SETTING)),
          material:item.material,
          componentMetaData:item.componentMetaData,
          boxShadow: this.$globalFormData.background.includes('url') ? '' : this.DEFAULT_SETTING.boxShadow
        }
        this.customAttributesTemp = clone(Setting[this.formData.material].customAttributes)
        this.updateComponentSetting()
      }
      if((this.formAttributes && this.formAttributes.length > 0) || JSON.stringify(this.componentFormConf) !== '{}') {
        this.activeTab = 'data'
      }else{
        this.activeTab = 'style'
      }
    },
    close(){
      this.activeTab = ''
      this.$set(this.$data,'dialogVisible',false)
      this.$emit('close')
    },
    updateComponentSetting (justFormOnly = false) {
      if (!justFormOnly) {
        this.formData = Object.assign({},this.formData,Setting[this.formData.material].global)
        this.formData.componentSetting = JSON.parse(JSON.stringify(Setting[this.formData.material].formData))
        if(this.formData.material === 'Banner' && this.formData.componentSetting.bannerList[0].banner === '') {
          this.formData.componentSetting.bannerList[0].banner = `data:image/png;base64,${this.defaultBannerList[0].image}`
        }
      }
      this.customAttributes = clone(this.customAttributesTemp)
      this.componentFormConf = clone(
        typeof Setting[this.formData.material].formConf === 'function'
          ? (Setting[this.formData.material].formConf)(this.formData.componentSetting)
          : Setting[this.formData.material].formConf
      )
    },
    updateComForm (val) {
      for(let k in val) {
        this.$set(this.queryFormData, k, val[k])
      }
      this.submit('change')
    },
    updateHandle (componentSetting) {
      if(this.editId) {
        this.submit('change', componentSetting)
      }else{
        this.$set(this.formData, 'componentSetting', componentSetting)
      }
    },
    submitHandle (oprate) {
      if(oprate == 'change') {
        if(this.editId) {
          this.$emit("editComponent",{
            ...this.formData,
            i: this.editId
          })
        }
        return false
      }
      if(this.editId) {
        this.$emit("editComponent",{
          ...this.formData,
          i: this.editId
        })
      }else{
        this.$emit("addComponent",{
          ...this.formData,
          i: uid()
        })
      }
      this.close()
      this.formData = {
        ...JSON.parse(JSON.stringify(this.DEFAULT_SETTING)),
        // options: this.$refs.componentForm.getFormData,
        boxShadow: this.$globalFormData.background.includes('http') ? '' : this.DEFAULT_SETTING.boxShadow
      }
      this.updateComponentSetting()
      if(this.isLock) {
        this.$emit('update:isLock',this.isLock)
      }
    },
    dataURLtoFile(dataurl, filename) {
      // 获取到base64编码
      const arr = dataurl.split(',')
      // 将base64编码转为字符串
      const bstr = window.atob(arr[1])
      let n = bstr.length
      const u8arr = new Uint8Array(n) // 创建初始化为0的，包含length个元素的无符号整型数组
      while (n--) {
        u8arr[n] = bstr.charCodeAt(n)
      }
      return new File([u8arr], filename, {
        type: 'image/png'
      })
    },
    uploadImg(dataUrl, filename, item) {
      let formData = new FormData();
      formData.append('file', this.dataURLtoFile(dataUrl, filename))
      formData.append('module','/jvs-ui/banner')
      axios({
        url: '/mgr/jvs-auth/upload/jvs-public',
        method: 'post',
        headers: {
          serialize:false,
          'type':'FormData',
          'Content-Type': 'application/x-www-form-urlencoded',
          'Authorization': this.$homePageOptions.reqHeaders['Authorization']
        },
        data: formData,
      }).then(res => {
        if (res.data && res.data.code == 0) {
          item.banner = res.data.data.fileLink
        }
      })
    },
    submit (oprate, componentSetting) {
      const arr = []
      const {material, componentMetaData} = this.component
      if(this.$refs.componentForm) {
        const formData = JSON.parse(JSON.stringify(this.$refs.componentForm.getFormData()))
        Object.keys(formData).forEach(prop => {
          arr.push({
            prop: prop,
            value: formData[prop]
          })
        })
      }
      if(this.formData.material === 'Banner') {
        this.formData.componentSetting.bannerList.forEach((item, index) => {
          if (item.banner.indexOf('base64') > -1) {
            this.uploadImg(item.banner, String(index+1), item)
          }
        })
      }
      const params = {
        type: material,
        componentMetaData: componentMetaData,
        queryParams: arr
      }
      if(this.reqUrl) {
        getCurrentComponentOptions(this.reqUrl, params).then(res => {
          if(res.data && res.data.code == 0) {
            this.options = res.data.data
            this.formData.options = res.data.data || {}
            this.formData.queryParams = [...arr]
            if(oprate == 'change' && componentSetting) {
              this.$set(this.formData, 'componentSetting', componentSetting)
            }
            let requestBool = true
            if(['ChartHistogram', 'ChartLine', 'ChartPie'].indexOf(material) > -1) {
              if(arr.length > 1) {
                arr.filter(ait => {
                  if(ait.prop != 'mockData' && !ait.value) {
                    requestBool = false
                  }
                })
              }else{
                requestBool = false
              }
              if(!requestBool) {
                if(!this.queryFormData) {
                  this.queryFormData = {
                    mockData: JSON.stringify(material == 'ChartPie' ? this.mockPie : this.mockDemo)
                  }
                }else{
                  if(!this.queryFormData.mockData) {
                    this.queryFormData.mockData = JSON.stringify(material == 'ChartPie' ? this.mockPie : this.mockDemo)
                  }
                }
                if(this.queryFormData.mockData) {
                  let mock = JSON.parse(this.queryFormData.mockData)
                  this.options = mock
                  this.formData.options = mock
                }
              }   
            }
            if(this.$refs.componentForm) {
              this.$refs.componentForm.$refs.form.validate((valid)=>{
                if(valid){
                  this.submitHandle(oprate)
                }
              })
            }else {
              this.submitHandle(oprate)
            }
          }
        })
      }
    },
    changeFormData(val){
      this.$set(this.formData.componentSetting,val.key,val.val)
      // this.$forceUpdate()
      this.updateComponentSetting(true)
    },
    changeCustomFormData(val){
      this.$set(this.formData,'componentSetting',val)
      // this.$forceUpdate()
      this.updateComponentSetting(true)
    },
    removeBanner(key) {
      this.customAttributesTemp.splice(key, 1)
      this.$set(this.formData,'customAttributes',this.customAttributesTemp)
      this.$forceUpdate()
    },
    addBanner() {
      this.customAttributesTemp.push({
        prop: 'bannerList',
        type: 'list',
        column: [
          {
            prop: 'banner',
            type: 'banner',
            label: '轮播图' + (this.customAttributesTemp.length + 1)
          },
        ]
      })
      this.$set(this.formData,'customAttributes',this.customAttributesTemp)
      this.$forceUpdate()
    },
  }
};
</script>

<style scoped lang="scss">
/deep/.el-form-item__label{
  font-weight: bold;
}
.material-config-tab{
  display: flex;
  align-items: center;
  border-bottom: 1px solid #EEEFF0;
  height: 54px;
  padding: 0 24px;
  box-sizing: border-box;
  .tab{
    height: 100%;
    line-height: 54px;
    margin-right: 56px;
    font-family: Microsoft YaHei-Regular, Microsoft YaHei;
    font-weight: 400;
    font-size: 16px;
    color: #6F7588;
    cursor: pointer;
    &.active{
      font-family: Microsoft YaHei-Bold, Microsoft YaHei;
      font-weight: 700;
      font-size: 16px;
      color: #1E6FFF;
      position: relative;
      &::after{
        content: '';
        width: 100%;
        height: 3px;
        background: #1E6FFF;
        border-radius: 2px 0px 2px 0px;
        position: absolute;
        bottom: -1px;
        left: 0;
      }
    }
  }
}
.material-config-wapper{
  max-height: calc(100% - 54px);
  overflow: hidden;
  overflow-y: auto;
  .config-form-title{
    padding: 0 16px;
    font-weight: bold;
    color: #333;
    margin-bottom: 20px;
  }
  .config-title {
    font-size: 20px;
    font-weight: bold;
    color: #3c2554;
    margin-bottom: 10px;
  }
  .material-text {
    font-size: 14px;
    text-transform: uppercase;
    color: #aaa;
    margin-left: 8px;
  }
  .form-row-control{
    .label {
      width: 84px;
      text-align: right;
      color: #606266;
      font-weight: 200;
      font-size: 14px;
      margin-right: 8px;
    }
    .content{
      .unit {
        margin-left: 6px;
        font-size: 14px;
        color: #787885;
      }
    }
  }
}
</style>
