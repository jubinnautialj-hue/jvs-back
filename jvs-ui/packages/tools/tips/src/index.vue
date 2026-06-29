<template>
  <el-tooltip
    effect="dark"
    placement="bottom"
    :show-after="200"
    popper-class="tools-tips"
    v-bind="$attrs">
    <template #content>
      <span v-if="$attrs.content">{{$attrs.content}}</span>
      <slot v-else></slot>
    </template>
    <div class="tips">
      <Icon name="infomation" size="20" />
    </div>
  </el-tooltip>
</template>

<script>
export default {
    name:'Tips',
  data() {
    return {

    };
  },
  created() {

  },
  mounted() {

  },
  methods: {

  }
};
</script>

<style scoped lang="scss">
.tips {
  margin-left: 8px;
  cursor: pointer;
  color: $color-grey4;
  display: inline-flex;
  align-items: center;
}
</style>
