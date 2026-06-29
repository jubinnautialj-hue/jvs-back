<template>
  <div class="app-rule-list">
    <applicationPageVue ref="applicationManage" active="5" :systemId="$route.query.jvsAppId"></applicationPageVue>
  </div>
</template>
<script>
import applicationPageVue from '@/components/template/applicationPage.vue'
export default {
  components: { applicationPageVue },
  data() {
    return {
      // 
    }
  },
  mounted () {
    this.$refs.applicationManage.init(this.$route.query.jvsAppId)
  }
}
</script>
<style lang="scss" scoped>
.app-rule-list{
  height: 100%;
  box-sizing: border-box;
  overflow: hidden;
  /deep/.application-manage-page{
    height: calc(100% - 16px);
    .left-menu{
      display: none;
    }
    .type-tab-bar{
      width: 100%;
      .info-box{
        padding-bottom: 20px;
        .card-list{
          grid-template-columns: 260px 260px 260px 260px 260px;
          grid-row-gap: 20px;
          grid-column-gap: 30px;
        }
      }
    }
  }
}
</style>