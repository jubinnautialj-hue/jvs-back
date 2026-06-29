<template>
  <div class="position-selector">
    <div
      class="icon-wrapper"
      :style="mode === 1 ? 'width: 90px;' : 'width: 60px;'"
    >
      <div
        v-for="item in valueList"
        :key="item.value"
        class="icon"
        :class="{ active: current === item.value }"
        @click="handleIconClick(item.value)"
      >
        <Icon
          :name="item.value === 5 ? 'full-screen' : 'arrow-up'"
          :style="`transform: rotate(${item.rotate}deg)`"
          size="20"
        />
      </div>
    </div>
    <span v-if="cnText" class="text">{{ $hit(cnText) }}</span>
    <span v-if="enText" class="text">{{ enText }}</span>
  </div>
</template>

<script>
export default {
  name: "PositionSelector",
  props: {
    value: {
      type: Number,
    },
    showChineseText: {
      type: Boolean,
      default: true,
    },
    showEnglishText: {
      type: Boolean,
      default: false,
    },
    mode: {
      type: Number,
      default: 1,
    },
  },
  data() {
    return {
      current: 5,
      mode1: [
        {
          value: 1,
          rotate: -45,
          cn: "左上",
          en: "Top Left",
        },
        {
          value: 2,
          rotate: 0,
          cn: "顶部水平居中",
          en: "Top Center",
        },
        {
          value: 3,
          rotate: 45,
          cn: "右上",
          en: "Top Right",
        },
        {
          value: 4,
          rotate: -90,
          cn: "左侧垂直居中",
          en: "Center Left",
        },
        {
          value: 5,
          rotate: 0,
          cn: "居中",
          en: "Center",
        },
        {
          value: 6,
          rotate: 90,
          cn: "右侧垂直居中",
          en: "Center Right",
        },
        {
          value: 7,
          rotate: -135,
          cn: "左下",
          en: "Bottom Left",
        },
        {
          value: 8,
          rotate: 180,
          cn: "底部水平居中",
          en: "Bottom Center",
        },
        {
          value: 9,
          rotate: 135,
          cn: "右下",
          en: "Bottom Right",
        },
      ],
      mode2: [
        {
          value: 1,
          rotate: -45,
          cn: "左上",
          en: "Top Left",
        },
        {
          value: 2,
          rotate: 45,
          cn: "右上",
          en: "Top Right",
        },
        {
          value: 3,
          rotate: -135,
          cn: "左下",
          en: "Bottom Left",
        },
        {
          value: 4,
          rotate: 135,
          cn: "右下",
          en: "Bottom Right",
        },
      ],
    };
  },
  computed: {
    valueList () {
      return this.mode == 1 ? this.mode1 : this.mode2
    },
    cnText() {
      let cn = ''
      if(this.showChineseText) {
        this.valueList.filter(item => {
          if(item.value === this.current) {
            cn = item.cn
          }
        })
      }
      return cn
    },
    enText() {
      let en = ''
      if(this.showEnglishText) {
        this.valueList.filter(item => {
          if(item.value === this.current) {
            en = item.en
          }
        })
      }
      return en
    },
  },
  created () {
    this.current = this.value || 5
  },
  mounted() {},
  watch: {
    value: {
      handler(newVal, oldVal) {
        if(newVal != oldVal) {
          this.current = newVal
        }
      },
      immediate: true,
    },
  },
  methods: {
    handleIconClick (value) {
      this.current = value
      this.$emit("change", value)
      this.$emit("update:value", value, this)
      this.$forceUpdate()
    },
  },
};
</script>

<style scoped lang="scss">
.position-selector {
  display: inline-flex;
  align-items: center;
  .icon-wrapper {
    display: flex;
    flex-wrap: wrap;
    width: 90px;
    .icon {
      width: 30px;
      height: 30px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      color: #898992;
      &.active {
        color: $color-primary;
      }
      &:not(.active):hover {
        color: darken($color-primary, 50%);
      }
    }
  }
  .text {
    margin: 0 8px;
    font-size: 14px;
    font-weight: bold;
    color: #262626;
  }
}
</style>
