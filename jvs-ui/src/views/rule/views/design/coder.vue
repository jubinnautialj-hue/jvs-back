<template>
  <div class="coder-editor-entity">
    <codeEditor :lang="lang" :code="code" :prop="prop" :onlyJSon="onlyJSon" @change="codeChange"></codeEditor>
  </div>
</template>
<script>
import codeEditor from '@/components/basic-assembly/jsonEditor'
export default {
  name: 'coder-editor',
  components: { codeEditor },
  props: {
    prop: {
      type: String
    },
    code: {
      type: String
    },
    lang: {
      type: String,
      default: () => {
        return 'json'
      }
    },
    onlyJSon: {
      type: Boolean
    }
  },
  methods: {
    codeChange (con) {
      this.$emit('change', con, this.prop)
    }
  }
}
</script>
<style lang="scss" scoped>
  .coder-editor-entity{
    position: absolute;
    width: 100%;
    height: 100%;
  }
</style>