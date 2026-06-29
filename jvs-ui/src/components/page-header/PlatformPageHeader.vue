<template>
  <div class="platform-page-header">
    <div class="title">{{title}}</div>
    <el-alert
      style="width: fit-content;"
      :closable="false"
      type="info">
      <template slot="title">
        <div class="alert-box">
          <i class="el-icon-info"/>
          <div>
            <slot name="desc"></slot>
            <span>{{ desc }}</span><span v-if="isMore" style="color: #3471ff;cursor: pointer;">{{$langt('common.learnMore')}}</span>
          </div>
        </div>
      </template>
    </el-alert>
  </div>
</template>

<script>
export default {
  name: "PlatformPageHeader",
  props: {
    title: {
      type: String
    },
    desc: {
      type: String
    },
    isMore: {
      type: Boolean
    },
  }
}
</script>

<style lang="scss" scoped>
.alert-box{
  display: flex;
  i{
    padding: 4px 0;
  }
}
</style>
