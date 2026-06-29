<template>
  <custom-dialog
    v-model="dialogVisible"
    :title="$hit('全局设置')"
    width="min(480px, 98vw)"
    height="min(520px, 90vh)"
    customClass="global-config-dialog"
    @close="close"
  >
    <!-- <WarnLock /> -->
    <el-form ref="form" label-position="top" size="mini">
      <el-form-item :label="$hit('壁纸')">
        <backgroundSelector
          isFullScreen
          recommendVideo
          :background.sync="formData.background"
        />
        <backgroundFilterSelector
          v-if="formData.background.includes('url')"
          :filter.sync="formData.backgroundFilter"
        />
      </el-form-item>
      <el-form-item :label="$hit('杂项')">
        <div class="form-row-control">
          <div class="label">{{ $hit('语言') }}</div>
          <div class="content">
            <el-select v-model="formData.lang">
              <el-option
                v-for="lang in langList"
                :label="lang.label"
                :value="lang.value"
                :key="lang.value"
              ></el-option>
            </el-select>
          </div>
        </div>
        <div class="form-row-control">
          <div class="label">{{ $hit('组件间隔') }}</div>
          <div class="content flex-center-y">
            <el-input-number
              v-model="formData.gutter"
              controls-position="right"
              :min="0"
              :max="50"
              style="width: 100px"
            >
            </el-input-number>
            <span class="font-control">px</span>
          </div>
        </div>
        <div class="form-row-control">
          <div class="label">{{ $hit('最小宽度') }}</div>
          <div class="content flex-center-y">
            <el-input-number
              v-model="formData.minWidth"
              controls-position="right"
              style="width: 100px"
            >
            </el-input-number>
            <span class="font-control">px</span>
          </div>
        </div>
        <div class="form-row-control" v-if="false">
          <div class="label">{{ $hit('全局字体') }}</div>
          <div class="content">
            <div>
              <div class="flex-center-y">
                <FontSelector
                  v-model="formData.globalFontFamily"
                  show-refresh
                  :show-harmony-font="formData.loadHarmonyOSFont"
                  style="width: 100%"
                />
              </div>
              <div class="flex-center-y" style="width: 242px;justify-content: space-between;margin: 4px 0 8px">
                <el-checkbox v-model="formData.loadHarmonyOSFont">
                  加载鸿蒙字体(外部)
                </el-checkbox>
                <Tips :content="$hit('勾选此项会在页面进入后加载鸿蒙字体,然后可以在字体选择器中选择或输入HarmonyOS_Regular,初次设置需刷新页面')" />
              </div>
            </div>
          </div>
        </div>
        <div class="form-row-control">
          <div class="label">{{ $hit('网站标题') }}</div>
          <div class="content flex-center-y">
            <el-input
              v-model="formData.siteTitle"
              :placeholder="$hit('自定义网站的标题')"
              clearable
            ></el-input>
            <Tips :content="$hit('siteTitleTips')" />
          </div>
        </div>
        <div class="form-row-control">
          <div class="label ellipsis" :title="$hit('禁用动画')">{{ $hit('禁用动画') }}</div>
          <div class="content flex-center-y">
            <el-switch
              v-model="formData.disabledDialogAnimation"
              style="width: 150px"
            ></el-switch>
            <Tips :content="$hit('disabledDialogAnimationTips')" />
          </div>
        </div>
      </el-form-item>
      <el-form-item :label="$hit('全局CSS注入')">
        <el-input
          v-model="formData.css"
          type="textarea"
          rows="4"
          :placeholder="$hit('injectCSSPlaceholder')"
        />
      </el-form-item>
      <el-form-item :label="$hit('全局JS注入')">
        <el-input
          v-model="formData.js"
          type="textarea"
          rows="4"
          :placeholder="$hit('injectJSPlaceholder')"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="footer" style="text-align: right; padding: 12px">
        <el-button type="button" class="dynamic-button" @click="close">{{ $hit('取消') }}</el-button>
        <el-button type="button" class="dynamic-button btn-primary" @click="submit">{{ $hit('确认') }}</el-button>
      </div>
    </template>
  </custom-dialog>
</template>

<script>
import { langList } from '@/lang/i18n'
import i18n from '@/lang/i18n'
export default {
    name:"globalConfig",
    props: {
        visible: {
            type: Boolean,
            default: false
        },
        globalFormData:{
            type:Object,
            default: null
        }
    },
  data() {
    return {
        dialogVisible:false,
        formData:{
          lang:''
        },
        langList,
    };
  },
  watch:{
    visible(newVal,oldVal){
        if(newVal){
            this.dialogVisible = true
        }else{
            this.dialogVisible = false
        }
    },
    "globalFormData":{
        handler(newVal,oldVal){
            if(newVal){
                if(JSON.stringify(newVal)=="{}"){
                    this.formData = this.$globalFormData
                }else{
                    this.formData = newVal
                }
            }else{
                this.formData = this.$globalFormData
            }
        },
        immediate:true
    },
    "formData.css":{
        handler(newVal,oldVal){
            const injectCSSEl = document.querySelector('#injectCSS')
            if(injectCSSEl){
                injectCSSEl.innerHTML = newVal || ''
            }else{
                let cssDom = document.createElement('style')
                cssDom.id = "injectCSS"
                cssDom.innerHTML = newVal || ''
                document.head.appendChild(cssDom)
            }
        },
        immediate:true
    },
    "formData.js":{
        handler(newVal,oldVal){
            const injectJSEl = document.querySelector('#injectJS')
            if(injectJSEl){
                injectJSEl.innerHTML = newVal || ''
            }else{
                let jsDom = document.createElement('script')
                jsDom.id = "injectJS"
                jsDom.innerHTML = newVal || ''
                document.head.appendChild(jsDom)
            }
        },
        immediate:true
    },
    "formData.lang":{
        handler(newVal,oldVal){
            if(newVal)  i18n.locale = newVal
           
        },
        immediate:true
    }
  },
  created() {

  },
  mounted() {

  },
  methods: {
    close(){
        this.dialogVisible = false
        this.$emit('update:visible',this.dialogVisible)
    },
    submit(){
        document.title = this.formData.siteTitle || '动态配置首页'
        this.$emit('update:globalFormData',this.formData)
        this.close()
    }
  }
};
</script>

<style lang="scss" scoped>
:deep(.el-form-item__label) {
  display: inline-flex !important;
  color: rgb(43, 43, 43);
  font-weight: bold;
  line-height: 1 !important;
  font-size: 16px;
  position: relative;
  &:after {
    position: absolute;
    content: '';
    left: 0;
    width: 100%;
    bottom: 0;
    height: 8px;
    background: rgba($color-warning, 0.2);
  }
}
.font-control {
  margin-left: 8px;
  font-weight: bold;
}
</style>

<style lang="scss">
.global-config-dialog .easy-dialog-body {
  padding: 5px 20px !important;
}
</style>
