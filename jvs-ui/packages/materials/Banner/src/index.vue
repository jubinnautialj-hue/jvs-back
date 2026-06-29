<template>
  <div style="width: 100%;height: 100%">
    <div
        v-if="componentSetting.bannerList.length === 1"
        class="banner-box"
        :style="'background-image:url('+ componentSetting.bannerList[0].banner +');width: 100%;height: 100%;background-size: 100% 100%;background-repeat: no-repeat;'"
    >
    </div>
    <el-carousel v-else style="height: 100%;">
      <el-carousel-item v-for="(item, key) in componentSetting.bannerList" :key="key">
        <div
            class="banner-box"
            :style="'background-image:url('+ item.banner +');width: 100%;height: 100%;background-size: 100% 100%;background-repeat: no-repeat;'"
        >
        </div>
      </el-carousel-item>
    </el-carousel>
  </div>
</template>

<script>

export default {
  name: "Banner",
  props: {
    componentSetting: {
      type: Object,
      default() {
        return {}
      }
    },
    options: {
      type: Object,
      default() {
        return {}
      }
    },
  },
  data() {
    return {
    }
  }
}
</script>

<style lang="scss" scoped>
.banner-box{
  width: 100%;
  height: 100%;
}
/deep/.el-carousel__container{
  height: 100% !important;
}
</style>
