<template>
  <div class="other-config-page">
    <div class="form-title">
      {{ configTitle }}
    </div>
    <div v-if="false" class="app-info-set-title">
      <div style="display: flex;justify-content: space-between;">
        <span>可以针对每一个终端开通不同的个性化配置，支持LOGO、登录页、名称自定义，支持公众号、企业微信、企业钉钉、idap多种平台集成。</span>
      </div>
      <div class="tip">
        <p>不同终端基础配置时不允许域名相同</p>
        <p>开通了不同三方集成后才能正常使用消息通知</p>
      </div>
    </div>
    <platform ref="appConfig" :routerQuery="routerQuery" @refreshData="getAppSetByTenantHandle"/>
  </div>
</template>

<script>

import Platform from "./components/platform";
import {getAppSetByTenant} from "@/views/upms/views/appBascSetting/api";

export default {
  name: "OtherConfig",
  props: {
    routerQuery: {
      type: Object
    }
  },
  components: {
    Platform,
  },
  data () {
    return {
      currentApp: '',
    }
  },
  created() {
    this.currentApp = this.routerQuery.type ? this.routerQuery.type : ''
    this.getAppSetByTenantHandle()
  },
  mounted() {
  },
  computed: {
    configTitle() {
      let str = ''
      switch (this.currentApp) {
        case "frame":
          str = 'JVS管理后台';
          break;
        case "knowledge":
          str = '无忧企业文档';
          break;
        case "teamwork":
          str = '无忧企业计划';
          break;
        case "RiskPolicy":
          str = 'JVS规则引擎';
          break;
        case "StoredDataWarehouse":
          str = 'JVS数据智仓';
          break;
        default:break;
      }
      return str
    }
  },
  methods: {
    getAppSetByTenantHandle () {
      getAppSetByTenant(this.$store.getters.tenantId).then(res => {
        if(res && res.data && res.data.code == 0){
          this.$refs.appConfig.init(JSON.parse(JSON.stringify(res.data.data[this.currentApp] || {})))
        }
      })
    },
  }
}
</script>

<style lang="scss" scoped>
.other-config-page {
  padding: 40px;
  border-radius: 6px;
  background-color: #ffffff;
  height: calc(100vh - 80px);
  overflow-y: auto;
  .form-title{
    font-weight: 600;
    font-size: 18px;
    margin-bottom: 16px;
  }
  .app-info-set-title{
    font-size: 14px;
    .close-icon{
      font-size: 20px;
      cursor: pointer;
    }
    span{
      color: #5e6d82;
    }
    .tip{
      padding: 8px 16px;
      background-color: #ecf8ff;
      border-radius: 4px;
      border-left: 5px solid #50bfff;
      margin: 20px 0;
      p{
        font-size: 14px;
        color: #5e6d82;
        line-height: 1.5em;
        a{
          color: #409eff;
          text-decoration: none;
        }
      }
    }
  }
}
</style>
