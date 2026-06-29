<template>
  <custom-dialog
    v-model="dialogVisible"
    :title="$hit('组件交互配置')"
    width="min(480px, 98vw)"
    height="min(520px, 90vh)"
  >
    <div>
      <el-form label-width="90px" size="mini">
        <el-form-item :label="$hit('交互行为')">
          <el-select v-model="formData.actionType">
            <el-option :label="$hit('无')" :value="0"></el-option>
            <el-option :label="$hit('鼠标点击')" :value="1"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$hit('鼠标点击')" v-if="formData.actionType === 1">
          <el-select v-model="formData.actionClickType">
            <el-option :label="$hit('显示新组件(Toggle)')" :value="1"></el-option>
            <el-option :label="$hit('跳转链接')" :value="2"></el-option>
            <!-- <el-option :label="$hit('运行Javascript脚本')" :value="3" disabled></el-option> -->
          </el-select>
        </el-form-item>
        <el-form-item
          v-if="formData.actionType === 1 && formData.actionClickType === 2"
        >
          <el-input
            v-model="formData.actionClickValue.url"
            :placeholder="$hit('请输入一个可用的跳转链接')"
          ></el-input>
        </el-form-item>
      </el-form>
      <div
        class="action-component-setting"
        v-if="formData.actionType === 1 && formData.actionClickType === 1"
      >
        <div class="title">{{$hit('Toggle组件配置')}}</div>
        <el-form ref="componentSettingForm" class="setting-form1" label-position="top" size="mini">
          <el-form-item :label="$hit('物料组件')">
            <div class="flex-center-y">
              <MaterialSelector
                :value.sync="formData.actionClickValue.material"
              />
              <Tips :content="$hit('actionMaterialTips')" />
            </div>
          </el-form-item>
          <el-form-item :label="$hit('尺寸')">
            <div class="form-row-control">
              <div class="label">Width</div>
              <div class="content">
                <el-input-number
                  v-model="formData.actionClickValue.w"
                  controls-position="right"
                  :min="40"
                  :max="1920"
                  style="width: 100px"
                />
                <span class="unit">PX</span>
              </div>
            </div>
            <div class="form-row-control">
              <div class="label">Height</div>
              <div class="content">
                <el-input-number
                  v-model="formData.actionClickValue.h"
                  controls-position="right"
                  :min="40"
                  :max="1920"
                  style="width: 100px"
                />
                <span class="unit">PX</span>
              </div>
            </div>
          </el-form-item>
          <el-form-item :label="$hit('Popover配置')">
            <div class="form-row-control">
              <div class="label">{{$hit('方向')}}</div>
              <div class="content">
                <el-select v-model="formData.actionClickValue.direction">
                  <el-option
                    v-for="item in directionList"
                    :label="item.label"
                    :value="item.value"
                    :key="item.value"
                  ></el-option>
                </el-select>
              </div>
            </div>
            <div class="form-row-control">
              <div class="label">{{$hit('阴影')}}</div>
              <div class="content">
                <el-input
                  style="width: 100%"
                  v-model="formData.actionClickValue.boxShadow"
                  clearable
                  :placeholder="$hit('shadowPlaceholder')"
                ></el-input>
              </div>
            </div>
            <div class="form-row-control">
              <div class="label">{{$hit('圆角')}}</div>
              <div class="content">
                <el-input-number
                  v-model="formData.actionClickValue.borderRadius"
                  controls-position="right"
                  :min="0"
                  :max="100"
                  style="width: 100px"
                >
                </el-input-number>
                <span class="font-control">px</span>
              </div>
            </div>
          </el-form-item>
          <el-form-item :label="$hit('背景')">
            <backgroundSelector
                :background.sync="formData.actionClickValue.background"
                :w="formData.w"
                :h="formData.h"
                :positionMode="formData.position"
            />
            <backgroundFilterSelector
                v-if="formData.actionClickValue.background.includes('url')"
                :filter.sync="formData.actionClickValue.backgroundFilte"
            />
          </el-form-item>
        </el-form>
        <div class="component-detail-setting">
          <div class="label">{{$hit('组件配置')}}</div>
          <div class="content">
            <StandardForm
              :formData="formData.actionClickValue.componentSetting"
              :formConf="actionClickFormConf"
              @changeFormData="changeFormData"
              ref="form"
              label-width="100px"
            ></StandardForm>
          </div>
        </div>
      </div>
    </div>
    <template #footer>
      <div class="footer" style="text-align: right; padding: 12px">
        <el-button type="button" class="dynamic-button" @click="close">{{$hit('取消')}}</el-button>
        <el-button type="button" class="dynamic-button btn-primary" @click="submit">{{$hit('确认')}}</el-button>
      </div>
    </template>
  </custom-dialog>
</template>

<script>
import Setting from '../../materials/setting'
import { directionList } from '@/utils/direction'
import { clone } from '@/utils'
export default {
  name:"ActionConfig",
  data() {
    return {
      dialogVisible:false,
      DEFAULT_SETTING: {
        actionType: 0,
        actionClickType: 1,
        actionClickValue: {
          url: '',
          material: 'Empty',
          w: 375,
          h: 400,
          background: 'rgba(255, 255, 255, 0.95)',
          backgroundFilter: 'brightness(0.8)',
          direction: 0,
          boxShadow: '0 0 4px #89909c',
          borderRadius: 4,
          componentSetting: JSON.parse(JSON.stringify(Setting.Label.formData))
        }
      },
      formData:{},
      directionList:directionList,
      actionClickFormConf:{},
      componentOptions:{}
    };
  },
  created() {

  },
  mounted() {
    this.formData = JSON.parse(JSON.stringify(this.DEFAULT_SETTING))
  },
  methods: {
    open(params){
      this.componentOptions = params
      if (params.actionSetting && params.actionSetting.actionType) {
        this.formData = JSON.parse(JSON.stringify(params.actionSetting))
      } else {
        this.formData = JSON.parse(JSON.stringify(DEFAULT_SETTING))
      }
      this.dialogVisible = true
      const material = this.formData.actionClickValue.material
      this.actionClickFormConf = clone(typeof Setting[material].formConf === 'function'
        ? (Setting[material].formConf)(this.formData.actionClickValue.componentSetting)
        : Setting[material].formConf)
    },
    close(){
        this.dialogVisible = false
    },
    submit(){
      if(this.formData.actionType){
        if(this.formData.actionClickType === 1){
          this.$refs.form.$refs.form.validate((vaild)=>{
            if(vaild){
              const result = {
                ...this.componentOptions,
                actionSetting: this.formData
              }
              this.$emit('editComponent',result)
              this.close()
            }else{
              return false
            }
          })
        }else if(this.formData.actionClickType === 2){
          if (/[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]/.test(this.formData.actionClickValue.url)) {
            if (!/https?:\/\/[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]/.test(this.formData.actionClickValue.url)) {
              this.formData.actionClickValue.url = 'https://' + this.formData.actionClickValue.url
            }
            const result = {
              ...this.componentOptions,
              actionSetting: this.formData
            }
            this.$emit('editComponent',result)
            this.close()
          } else {
            this.$oneMessage({
              message:'跳转目标URL不合法',
              type:'error'
            })
          }
        }
      }else{
        const result = {
          ...this.componentOptions,
          actionSetting: null
        }
        this.$emit('editComponent',result)
        this.close()
      }
    },
    changeFormData(val){
      this.$set(this.formData.actionClickValue.componentSetting,val.key,val.val)
      this.updateComponentSetting(true)
    }
  }
};
</script>

<style scoped lang="scss">

</style>
