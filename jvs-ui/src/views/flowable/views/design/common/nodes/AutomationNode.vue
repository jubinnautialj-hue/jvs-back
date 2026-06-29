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
    placeholder="未命名逻辑"
    header-icon="icon-jvs-yewuluoji"
    header-bgc="rgba(108,89,253,0.12)"
  />
</template>

<script>
import Node from "./Node";

export default {
  name: "AutoMationNode",
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
      return this.config.props.automation && this.config.props.automation.name ? this.config.props.automation.name : "未命名逻辑";
    },
  },
  methods: {
    //校验数据配置的合法性
    validate(err) {
      this.showError = false;
      if (this.config.props.shouldAdd) {
        this.showError = false;
      } else if (this.config.props.targetObj.personnels.length === 0) {
        this.showError = true;
        this.errorInfo = "请选择需要抄送的人员";
      }
      if (this.showError) {
        err.push(`抄送节点 ${this.config.name} 未设置抄送人`);
      }
      return !this.showError;
    },
  },
};
</script>

<style scoped>
</style>
