<template>
  <node
    :title="config.name || '发起人'" 
    :is-root="true"
    :content="content"
    placeholder="所有人"
    header-bgc="#E4EDFF"
    header-icon="icon-jvs-faqiren"
    :currentNode="config.currentNode"
    @selected="$emit('selected')"
    @insertNode="type => $emit('insertNode', type)"/>
</template>

<script>
import Node from './Node'

export default {
  name: "RootNode",
  components: {Node},
  props:{
    config:{
      type: Object,
      default: () => {
        return {}
      }
    }
  },
  computed:{
    content(){
      if (this.config.props && this.config.props.purviews && this.config.props.purviews.length > 0){
        let texts = []
        let allBool = false
        this.config.props.purviews.filter(pi => {
          if(pi.personType == 'all') {
            allBool = true
          }else{
            pi.personnels.forEach(org => texts.push(org.name))
          }
        })
        return allBool ? '所有人' : texts.length > 0 ? String(texts).replaceAll(',', '、') : '请选择发起人'
      } else {
        return '所有人'
      }
    }
  },
  data() {
    return {
    }
  },
  methods: {}
}
</script>

<style scoped>

</style>
