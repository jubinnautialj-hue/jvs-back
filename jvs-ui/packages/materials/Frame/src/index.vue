<template>
  <div class="iframe-box">
    <iframe v-if="queryFormData.url" :src="queryFormData.url" frameborder="0" width="100%" height="100%"></iframe>
  </div>
</template>

<script>
export default {
  name: "Frame",
  props: {
    element: {
      type: Object
    },
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
      queryFormData: {}
    }
  },
  created () {
    if (this.element.queryParams) {
      let obj = {}
      this.element.queryParams.forEach(it => {
        obj[it.prop] = it.value
      })
      this.queryFormData = JSON.parse(JSON.stringify(obj))
    }
    console.log(this.queryFormData)
  },
}
</script>

<style lang="scss" scoped>
.iframe-box{
  width: 100%;
  height: 100%;
}
.show-outline-1{
  .iframe-box{
    padding: 20px;
    box-sizing: border-box;
  }
}

</style>
