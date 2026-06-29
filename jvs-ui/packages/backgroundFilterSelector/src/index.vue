<template>
  <div class="background-filter-selector">
    <div class="row">
        <div class="label">{{$hit('滤镜')}}</div>
        <div class="content">
        <el-radio-group v-model="mode" @change="handleUpdateValue">
            <el-radio :label="1" size="small">{{$hit('无')}}</el-radio>
            <el-radio :label="2" size="small">{{$hit('亮度')}}</el-radio>
            <el-radio :label="3" size="small">{{$hit('高斯模糊')}}</el-radio>
        </el-radio-group>
        <div v-if="mode === 2">
            <div class="flex-center-y">
            <el-input-number
                v-model="brightness"
                :min="0.1"
                :max="1.5"
                :step="0.1"
                controls-position="right"
                @change="handleUpdateValue"
            ></el-input-number>
            <Tips :content="$hit('brightnessTips')" />
            </div>
        </div>
        <div v-if="mode === 3">
            <div class="flex-center-y">
            <el-input-number
                v-model="blur"
                :min="0"
                :max="100"
                controls-position="right"
                @change="handleUpdateValue"
            ></el-input-number>
            <span style="font-weight: bold; margin: 0 4px">px</span>
            <Tips :content="$hit('blurTips')" />
            </div>
        </div>
        </div>
    </div>
    <div class="row" v-if="isFullScreen">
        <div class="label" data-badge>{{$hit('动画滤镜')}}</div>
        <div class="content">
        <div class="effect-tips">请前往辅助功能设置</div>
        </div>
    </div>
  </div>
</template>

<script>
export default {
    name:'backgroundFilterSelector',
    props: {
        filter: {
            type: String
        },
        isFullScreen: {
            type: Boolean,
            default: false
        }
    },
  data() {
    return {
        mode:1,
        brightness:1,
        blur:5
    };
  },
  watch:{
    filter:{
        handler(newVal){
            if(newVal){
                const match = /[\d.]+/.exec(newVal)
                if (match && match.length >= 0) {
                    if (newVal.includes('brightness')) {
                        this.mode = 2
                        this.brightness = +match[0]
                    } else if (newVal.includes('blur')) {
                        this.mode = 3
                        this.blur = +match[0]
                    }
                    return
                }
            }
            this.mode = 1
        },
        immediate:true
    }
  },
  created() {

  },
  mounted() {

  },
  methods: {
    handleUpdateValue (){
      let output = ''
      switch (this.mode) {
        case 1:
          break
        case 2:
          output = `brightness(${this.brightness})`
          break
        case 3:
          output = `blur(${this.blur}px)`
          break
      }
      this.$emit('update:filter', output)
    }
  }
};
</script>

<style scoped lang="scss">
.background-filter-selector{
    .row {
        display: flex;
        margin-bottom: 10px;
        .label {
            font-weight: bold;
            margin-bottom: 0;
            margin-right: 8px;
            width: 84px;
            text-align: right;
            line-height: 32px;
            &[data-badge] {
                position: relative;
                &:after {
                    content: "";
                    position: absolute;
                    font-size: 12px;
                    height: 8px;
                    width: 8px;
                    background: #f56c6c;
                    border: 1px solid #fff;
                    border-radius: 50%;
                    top: 4px;
                    right: -8px;
                }
            }
        }
        .content {
            flex: 1;
            .el-radio-group{
                display: inline-flex;
                .el-radio{
                    display: flex;
                    align-items: center;
                }
            }
        }
    }
    :deep(.el-radio) {
        margin-bottom: 5px;
    }
    .effect-tips {
        color: $color-grey3;
        cursor: not-allowed;
        padding-left: 4px;
    }
}
</style>
