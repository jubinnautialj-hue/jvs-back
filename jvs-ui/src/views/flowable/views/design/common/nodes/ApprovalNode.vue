<template>
  <node
    :title="config.name"
    :show-error="showError"
    :content="content"
    :error-info="errorInfo"
    placeholder="请设置审批人"
    header-bgc="#FFF2E6"
    header-icon="icon-jvs-shenpiren"
    :currentNode="config.currentNode"
    @selected="$emit('selected')"
    @delNode="$emit('delNode')" @insertNode="type => $emit('insertNode', type)"
  />
</template>

<script>
import Node from './Node'

export default {
  name: "ApprovalNode",
  props:{
    config:{
      type: Object,
      default: () => {
        return {}
      }
    }
  },
  components: {Node},
  data() {
    return {
      showError: false,
      errorInfo: ''
    }
  },
  computed:{
    content(){
      let config = this.config.props
      switch (config.type){
        case "ASSIGN_USER":
          if (config.targetObj && config.targetObj.personnels && config.targetObj.personnels.length > 0){
            let texts = []
            config.targetObj.personnels.forEach(org => texts.push(org.name))
            return String(texts).replaceAll(',', '、')
          }else {
            return '请指定审批人'
          }
        case "SELF":
          return '发起人自己'
        case "SELF_SELECT":
          if (config.targetObj && config.targetObj.personnels && config.targetObj.personnels.length > 0){
            let texts = []
            config.targetObj.personnels.forEach(org => texts.push(org.name))
            return String(texts).replaceAll(',', '、')
          }else {
            return '发起人自选' // (config.targetObj && config.targetObj.multiple) ? '发起人自选多人':'发起人自选一人'
          }
        case "LEADER_TOP":
        case "LEADER":
          let str = ''
          if(config.leader) {
            switch(config.leader.leaderSource) {
              case 'SEND_USER': str = `发起人`;break;
              case 'FLOW_NODE': str = `审批节点`;break;
              case 'USER_FIELD': str = `成员字段`;break;
              default: str = '发起人';break;
            }
          }
          return config.leader.leaderLevel > 1 ? `${str}的第 ` + config.leader.leaderLevel + ' 级主管' : `${str}的直接主管`
        case "ROLE":
          if (config.targetObj && config.targetObj.personnels && config.targetObj.personnels.length > 0){
            return '角色-'+String(config.targetObj.personnels.map((r) => { return r.name; })).replaceAll(',', '、')
          }else {
            return '请选择角色'
          };
        case "JOB":
          if (config.targetObj && config.targetObj.personnels && config.targetObj.personnels.length > 0){
            return '岗位-'+String(config.targetObj.personnels.map((r) => { return r.name; })).replaceAll(',', '、')
          }else {
            return '请选择岗位'
          };
        case "DEPT":
          if (config.targetObj && config.targetObj.personnels && config.targetObj.personnels.length > 0){
            return '部门-'+String(config.targetObj.personnels.map((r) => { return r.name; })).replaceAll(',', '、')
          }else {
            return '请选择部门'
          };
        case "USER_FIELD":
          if (config.targetObj && config.targetObj.personnels && config.targetObj.personnels.length > 0){
            return '成员字段-'+String(config.targetObj.personnels.map((r) => { return r.name; })).replaceAll(',', '、')
          }else {
            return '请选择成员字段'
          };
        case "DEPT_FIELD":
          if (config.targetObj && config.targetObj.personnels && config.targetObj.personnels.length > 0){
            return '部门字段-'+String(config.targetObj.personnels.map((r) => { return r.name; })).replaceAll(',', '、')
          }else {
            return '请选择部门字段'
          };
        default: return '未知设置项'
      }
    }
  },
  methods: {
    //校验数据配置的合法性
    validate(err){
      try {
        return this.showError = !this[`validate_${this.config.props.assignedType}`](err)
      } catch (e) {
        return true;
      }
    },
    validate_ASSIGN_USER(err){
      if(this.config.targetObj.personnels.length > 0){
        return true;
      }else {
        this.errorInfo = '请指定审批人员'
        err.push(`${this.config.name} 未指定审批人员`)
        return false
      }
    },
    validate_SELF_SELECT(err){
      return true;
    },
    validate_LEADER_TOP(err){
      return true;
    },
    validate_LEADER(err){
      return true;
    },
    validate_ROLE(err){
      if (this.config.targetObj.personnels.length <= 0){
        this.errorInfo = '请指定负责审批的系统角色'
        err.push(`${this.config.name} 未指定审批角色`)
        return false
      }
      return true;
    },
    validate_SELF(err){
      return true;
    },
    validate_REFUSE(err){
      return true;
    }
  }
}
</script>

<style scoped>

</style>
