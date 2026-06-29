<template>
  <div class="tool-box">
    <design-tool :title="'表单设计'" :needLoading="true" :dataModelId="dataModelId" :dataModelFields="dataModelFields" :fresh="fresh">
      <template slot="right">
        <!-- <p>
          <el-tooltip effect="dark" content="预览" placement="top">
            <i class="el-icon-view form-design-tool" @click="toolClick('preview')"></i>
          </el-tooltip>
        </p> -->
        <slot name="right"></slot>
      </template>
    </design-tool>
  </div>
</template>
<script>
import saveicon from "@/const/img/保存.png"
import titlePageHeader from '@/components/page-header/titlePageHeader'
import designTool from '@/components/design-tool/DesignTool'
import Permission from "../../components/design/permission";
export default {
  name: 'design-bar',
  components: {titlePageHeader, designTool, Permission},
  props: {
    dataModelId: {
      type: String
    },
    dataModelFields: {
      type: Array
    },
    fresh: {
      type: Boolean
    }
  },
  data () {
    return {
      saveIcon: saveicon,
      role: [], // 权限设置
      roleType: true, // 权限类型,true 应用 权限，false 自定义权限
      operationList: [], // 操作权限list
      permissionVisible: false, // 权限弹框
      deptId: [],
      dialogVisible: false,
      submitLoading: false, // 保存loading
    }
  },
  created () {
  },
  methods: {
    // toolClick
    toolClick(type) {
      this.$root.eventBus.$emit('pageEvent', type)
    },
    // 预览
    view(type) {
    },
  }
}
</script>
<style lang="scss" scoped>
.tool-box{
  .form-design-tool{
    font-size: 20px;
    cursor: pointer;
    color: #353535;
  }
  .cont-top {
    overflow: hidden;
    font-size: 12px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    color: #606266;
    background: #fff;
    border-radius: 5px;
    .cont-top-item {
      display: flex;
      justify-content: space-between;
      p {
        margin: 0 10px;
        height: 32px;
        line-height: 32px;
      }
    }
  }
}
</style>
