<template>
  <node
    :title="config.name"
    :show-error="showError"
    :content="content"
    :error-info="errorInfo"
    :currentNode="config.currentNode"
    @selected="$emit('selected')"
    @delNode="$emit('delNode')"
    @insertNode="(type) => $emit('insertNode', type)"
    placeholder="消息通知"
    header-bgc="#f56c6c"
  />
</template>

<script>
import Node from "./Node";

export default {
  name: "MessageNode",
  props: {
    config: {
      type: Object,
      default: () => {
        return {};
      },
    },
  },
  components: { Node },
  data() {
    return {
      showError: false,
      errorInfo: "",
    };
  },
  computed: {
    content() {
      return this.config.props.message && this.config.props.message.name ? this.config.props.message.name : "消息通知";
    },
  },
  methods: {
    //校验数据配置的合法性
    validate(err) {
      this.showError = false;
      return !this.showError;
    },
  },
};
</script>

<style scoped>
</style>
