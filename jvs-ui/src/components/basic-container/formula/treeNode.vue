<template>
  <div :class="{'formlula-tree-node': true, 'param-node': idata.param}">
    <div @click="nodeClick(idata, index)" @mouseover="showHandle(idata)" class="self-item">
      <span class="formula-func-title show-type" :style="idata.level ? `text-indent: ${idata.level * 10}px;` : ''">{{idata.name}}</span>
      <div :class="{'formula-func-capsule': true,
        'blue': idata.jvsParamType == 'text',
        'yellow': idata.jvsParamType == 'number',
        'green': idata.jvsParamType == 'date',
        'purple': idata.jvsParamType == 'array',
        'red': idata.jvsParamType == 'object',
        'pink': idata.jvsParamType == 'bool',
        'dark-yellow': idata.jvsParamType == 'file',
        'cyan': idata.jvsParamType == 'unknown' || idata.jvsParamType == 'any',
      }">{{idata.jvsParamType | getLabeType}}</div>
    </div>
    <div v-if="(!idata.param) && idata.shortName" class="short-name">{{idata.shortName}}</div>
    <div v-if="idata.children && idata.children.length > 0">
      <ul class="function-panel-filed-list">
        <li :class="{'function-panel-filed-li': true, 'function-panel-filed-li-haschildren': (idata.children && idata.children.length > 0)}" v-for="(idatac, idxc) in idata.children" :key="'treedata-item-children-'+idxc">
          <treeNode :idata="idatac" :index="index" @click="click" @show="show"></treeNode>
        </li>
      </ul>
    </div>
  </div>
</template>
<script>
import treeNode from './treeNode.vue'
export default {
  name: 'treeNode',
  components: {treeNode},
  props: {
    idata: Object,
    index: Number
  },
  filters: {
    getLabeType(type) {
      let str = ''
      switch(type) {
        case 'text':
          str = '文字';break;
        case 'number':
          str = '数字';break;
        case 'date':
          str = '时间';break;
        case 'array':
          str = '数组';break;
        case 'object':
          str = '对象';break;
        case 'bool':
          str = '布尔';break;
        case 'file':
          str = '文件';break;
        case 'any':
        case 'unknown':
          str = '未知';break;
        default: ;break;
      }
      return str
    }
  },
  data () {
    return {}
  },
  methods: {
    nodeClick(item, index) {
      this.$emit('click', item, index)
    },
    showHandle (item) {
      this.$emit('show', item)
    },
    click (item, index) {
      this.$emit('click', item, index)
    },
    show (item) {
      this.$emit('show', item)
    }
  }
}
</script>