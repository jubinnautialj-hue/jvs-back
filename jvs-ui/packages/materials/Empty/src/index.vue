<template>
  <div
    v-if="componentSetting.name || componentSetting.customText"
    class="wrapper"
    :style="{
      textShadow: (`0 0 1px ${tranRenderColor(componentSetting.titleColor)}` || componentSetting.textShadow),
      padding: componentSetting.padding + 'px',
      ...positionCSS
    }">
    <div class="label-text" 
      :style="{
        fontSize: `${componentSetting.titleSize || 16}px`,
        fontWeight: (componentSetting.bold === false) ? '' : 'bold',
        fontStyle: componentSetting.slant ? 'italic' : '',
        background: tranRenderColor(componentSetting.titleColor),
        textDecoration: componentSetting.underline ? `underLine ${getLastColor(componentSetting.titleColor)}` : '',
      }"
    >
      {{componentSetting.name || componentSetting.customText}}
    </div>
  </div>
</template>

<script>
import mapPosition from '../../../positionSelector/mapPosition'
import { tranRenderColor, getLastColor } from '@/components/colorPicker/utils/common'
export default {
    name: 'Label',
    props:{
      componentSetting: {
        type: Object,
        required: true
      }
    },
  data() {
    return {

    };
  },
  computed:{
    positionCSS () {
      return mapPosition(this.componentSetting.position)
    }
  },
  methods: {
    tranRenderColor (color) {
      return tranRenderColor(color)
    },
    getLastColor (color) {
      return getLastColor(color)
    },
  }
};
</script>

<style scoped lang="scss">
.wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  .label-text{
    background-clip: text !important;
    -webkit-background-clip:text !important;
    -webkit-text-fill-color: transparent;
  }
}
</style>
