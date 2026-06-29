<template>
  <custom-dialog  
    :value.sync="dialogVisible"
    :title="editId ? $hit('编辑组件') : $hit('添加卡片')"
    width="min(800px, 98vw)"
    height="min(600px, 90vh)">
    <div class="main-config">
      <div class="base-config-wrapper">
        <div class="config-title">{{$hit('基础配置')}}</div>
        <!-- <WarnLock /> -->
        <div class="form-wrapper scrollbar1">
          <el-form ref="form" label-position="top" :model="formData" class="el-form--default" size="mini">
            <el-form-item :label="$hit('物料组件')">
              <MaterialSelector
                :value.sync="formData.material"
                :disabled="!!editId"
                @change="updateComponentSetting(false)"
              />
            </el-form-item>
            <el-form-item :label="$hit('定位模式')" class="inline-form-flex">
              <el-radio-group
                v-model="formData.position"
                :disabled="!!editId"
                style="margin-right: 8px"
              >
                <el-radio :label="1">{{$hit('栅格模式')}}</el-radio>
                <el-radio :label="2">{{$hit('绝对模式')}}</el-radio>
                <el-radio :label="3">{{$hit('浮动模式')}}</el-radio>
              </el-radio-group>
              <Tips>
                <div style="line-height: 1.5">
                  <p>{{$hit('positionTips1')}}</p>
                  <p>{{$hit('positionTips2')}}</p>
                </div>
              </Tips>
            </el-form-item>
            <el-form-item :label="$hit('定位方向')" v-if="formData.position === 2 || formData.position === 3">
              <div class="flex-center-y">
                <PositionSelector
                  :value.sync="formData.affixInfo.mode"
                  :mode="2"
                  :showChineseText="false"
                  @change="handleResetAffix"
                />
                <div>
                  <div class="flex-center-y" style="margin-bottom:4px;">
                    <div class="bold" style="width: 68px; text-align: right; margin: 8px 4px 0">
                      {{ affixY }}
                    </div>
                    <el-input-number
                      v-model="formData.affixInfo.y"
                      controls-position="right"
                      style="width: 100px"
                    />
                  </div>
                  <div class="flex-center-y">
                    <div class="bold" style="width: 68px; text-align: right; margin: 0 4px">
                      {{ affixX }}
                    </div>
                    <el-input-number
                      v-model="formData.affixInfo.x"
                      controls-position="right"
                      style="width: 100px"
                    />
                  </div>
                </div>
                <Tips>
                  <div style="line-height: 1.5">
                    <p>{{$hit('fixedTips1')}}</p>
                    <p>{{$hit('fixedTips2')}}</p>
                  </div>
                </Tips>
              </div>
            </el-form-item>
            <el-form-item :label="$hit('组件尺寸')">
              <div class="form-row-control">
                <div class="label">Width</div>
                <div class="content">
                  <el-input-number
                    v-model="formData.w"
                    controls-position="right"
                    :min="formData.position === 1 ? 1 : 40"
                    :max="formData.position === 1 ? 12 : 1920"
                    style="width: 100px"
                  />
                  <span class="unit">{{ formData.position === 1 ? 'FR' : 'PX' }}</span>
                  <Tips>
                    <div style="line-height: 1.5">
                      <p>{{$hit('sizeUnitTips1')}}</p>
                      <p>{{$hit('sizeUnitTips2')}}</p>
                      <p>{{$hit('sizeUnitTips3')}}</p>
                    </div>
                  </Tips>
                </div>
              </div>
              <div class="form-row-control">
                <div class="label">Height</div>
                <div class="content">
                  <el-input-number
                    v-model="formData.h"
                    controls-position="right"
                    :min="formData.position === 1 ? 1 : 40"
                    :max="formData.position === 1 ? 24 : 1920"
                    style="width: 100px"
                  />
                  <span class="unit">{{ formData.position === 1 ? 'FR' : 'PX' }}</span>
                </div>
              </div>
            </el-form-item>
            <el-form-item :label="$hit('背景')">
              <backgroundSelector
                :background.sync="formData.background"
                :w="formData.w"
                :h="formData.h"
                :positionMode="formData.position"
              />
              <backgroundFilterSelector
                v-if="formData.background.includes('url')"
                :filter.sync="formData.backgroundFilter"
              />
            </el-form-item>
            <el-form-item :label="$hit('其他配置')">
              <div class="form-row-control">
                <div class="label">{{$hit('圆角')}}</div>
                <div class="content">
                  <el-input-number
                    v-model="formData.borderRadius"
                    controls-position="right"
                    :min="0"
                    :max="100"
                    style="width: 100px"
                  />
                  <span class="font-control">px</span>
                </div>
              </div>
              <div class="form-row-control">
                <div class="label">{{$hit('阴影')}}</div>
                <div class="content">
                  <el-input
                    style="width: 100%"
                    v-model="formData.boxShadow"
                    clearable
                    :placeholder="$hit('shadowPlaceholder')"
                  />
                </div>
                <Tips :content="$hit('shadowTips')" />
              </div>
              <div class="form-row-control">
                <div class="label">zIndex</div>
                <div class="content">
                  <el-input-number
                    v-model="formData.zIndex"
                    controls-position="right"
                    :min="0"
                    :max="9999"
                    style="width: 100px"
                  />
                </div>
                <Tips :content="$hit('zIndexTips')" />
              </div>
              <div class="form-row-control">
                <div class="label">{{$hit('ID属性注入')}}</div>
                <div class="content">
                  <el-input
                    v-model="formData.customId"
                    :placeholder="$hit('组件自定义ID')"
                    clearable
                  />
                </div>
                <Tips :content="$hit('customIdTips')" />
              </div>
            </el-form-item>
          </el-form>
        </div>
      </div>
      <div class="component-config-wrapper">
        <div class="config-title">
          {{$hit('组件配置')}}<span class="material-text">#{{ formData.material }}</span>
        </div>
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
      <div class="footer" style="text-align: right; padding: 12px">
        <el-button class="dynamic-button" type="button" @click="close">{{$hit('取消')}}</el-button>
        <el-button class="dynamic-button btn-primary" type="button" @click="submit">{{$hit('确认')}}</el-button>
      </div>
    </template>
  </custom-dialog>
</template>

<script>
import { uid,clone } from '@/utils'
import Setting from '../../materials/setting'
export default {
  name:'baseConfig',
  props:{
    isLock:{
      type:Boolean,
      default:false
    }
  },
  data() {
    return {
      editId:'',
      dialogVisible:false,
      formData:{},
      DEFAULT_SETTING:{
        position: 1,
        affixInfo: {
          mode: 1,
          x: 10,
          y: 10
        },
        w: 6,
        h: 2,
        background: 'transparent',
        backgroundFilter: 'brightness(0.8)',
        material: 'Empty',
        borderRadius: 4,
        boxShadow: '',
        zIndex: 10,
        customId: ''
      },
      componentFormConf:{}
    };
  },
  created() {
    this.$set(this.$data,'formData',this.DEFAULT_SETTING)
  },
  mounted() {
  },
  computed:{
    affixY(){
      [1, 2].includes(this.formData.affixInfo.mode) ? 'TOP' : 'BOTTOM'
    },
    affixX(){
      [1, 3].includes(this.formData.affixInfo.mode) ? 'LEFT' : 'RIGHT'
    }
  },
  watch:{
    'formData.position':{
      handler(newVal,oldVal){
       if(this.formData){
        if (this.editId) return
        if (newVal === 1) {
          this.formData.w = 6
          this.formData.h = 2
        } else {
          this.formData.w = 200
          this.formData.h = 200
        }
       }
      },immediate:true
    }
  },
  methods: {
    open(item = {}){
      this.$set(this.$data,'dialogVisible',true)
      if(item.i){
        this.editId = item.i
        this.formData = {
          ...JSON.parse(JSON.stringify(item))
        }
        this.updateComponentSetting(true)
      }else{
        this.editId = ''
        this.formData = {
          ...JSON.parse(JSON.stringify(this.DEFAULT_SETTING)),
          boxShadow: this.$globalFormData.background.includes('url') ? '' : this.DEFAULT_SETTING.boxShadow
        }
        this.updateComponentSetting()
      }
      
    },
    close(){
      this.$set(this.$data,'dialogVisible',false)
    },
    submit(){
      this.$refs.form.$refs.form.validate((valid)=>{
        if(valid){
          if(this.editId){
            this.$emit("editComponent",{
              ...this.formData,
              i:this.editId
            })
          }else{
            this.$emit("addComponent",{
              ...this.formData,
              i:uid()
            })
          }
          this.close()
          this.formData = {
            ...JSON.parse(JSON.stringify(this.DEFAULT_SETTING)),
            boxShadow: this.$globalFormData.background.includes('http') ? '' : this.DEFAULT_SETTING.boxShadow
          }
          this.updateComponentSetting()
          if (this.isLock) {
            this.$emit('update:isLock',this.isLock)
            this.$notify({
              title: this.$hit('提示'),
              type: 'success',
              message: this.$hit('已自动进入编辑模式，编辑模式可进行组件拖拽与右键菜单配置')
            });
          }
        }
      })
    },
    handleResetAffix() {
      this.formData.affixInfo.x = this.DEFAULT_SETTING.affixInfo?.x
      this.formData.affixInfo.y = this.DEFAULT_SETTING.affixInfo?.y
    },
    updateComponentSetting(justFormOnly = false){
      if (!justFormOnly) {
        this.formData = Object.assign({},this.formData,Setting[this.formData.material].global)
        this.formData.componentSetting = JSON.parse(
          JSON.stringify(Setting[this.formData.material].formData)
        )
      }
      this.componentFormConf = clone(
        typeof Setting[this.formData.material].formConf === 'function'
          ? (Setting[this.formData.material].formConf)(this.formData.componentSetting)
          : Setting[this.formData.material].formConf
      )
    },
    changeFormData(val){
      this.$set(this.formData.componentSetting,val.key,val.val)
      // this.$forceUpdate()
      this.updateComponentSetting(true)
    }
  }
};
</script>

<style scoped lang="scss">
.main-config {
  width: 100%;
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-wrap: wrap;
  .base-config-wrapper,
  .component-config-wrapper {
    width: 50%;
    height: 100%;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    position: relative;
    padding-left: 10px;
    padding-right: 4px;
    .config-title {
      font-size: 20px;
      font-weight: bold;
      color: #3c2554;
      margin-bottom: 10px;
    }
    .form-wrapper {
      height: 100%;
      flex: 1;
      overflow-y: auto;
      padding-top: 5px;
      padding-right: 8px;
      .inline-form-flex{
        /deep/.el-form-item__content{
          display: inline-flex;
          align-items: center;
          .el-radio-group{
            display: inline-flex;
            .el-radio{
              display: flex;
              align-items: center;
            }
          }
        }
      }
      
    }
  }
  .base-config-wrapper {
    border-right: 2px solid #eee;
  }
}
.form-control {
  @include flex-center-y;
  .divider {
    margin: 0 8px;
  }
}
.form-row-control {
  @include flex-center-y;
  margin-bottom: 8px;
  .label {
    width: 84px;
    text-align: right;
    color: rgb(43, 43, 43);
    font-weight: bold;
    font-size: 14px;
    margin-right: 8px;
  }
}
.tips {
  font-size: 18px;
  margin-left: 8px;
  cursor: pointer;
}
.font-control {
  font-size: 14px;
  font-weight: bold;
  margin-left: 4px;
}
.unit {
  margin-left: 6px;
  font-size: 14px;
  color: #787885;
}
.base-config-wrapper {
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
}
.material-text {
  font-size: 14px;
  text-transform: uppercase;
  color: #aaa;
  margin-left: 8px;
}

@media screen and (max-width: 900px) {
  .main-config {
    overflow: auto;
    height: auto;
    .base-config-wrapper,
    .component-config-wrapper {
      width: 100%;
      padding: 0 10px;
      border-right: none;
      margin-bottom: 20px;
    }
  }
}
</style>
