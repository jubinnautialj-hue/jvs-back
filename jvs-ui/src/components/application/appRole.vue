<template>
  <div class="app-role-page">
    <jvs-table
      class="app-role-table"
      refs="multipleTable"
      :data="tableData"
      :loading="tableLoading"
      :option="option"
      @on-load="getAppRolePage"
      @search-change="searchChange"
    >
      <template slot="menuLeft">
        <jvs-button size="mini" type="primary" @click="handleUserSelectOpen">添加人员</jvs-button>
      </template>
      <template slot="menuRight">
        <p style="cursor:pointer;display: flex;align-items: center;" @click="searchChange(queryParams)">
          <span>刷新</span>
          <i class="el-icon-refresh" style="cursor:pointer;margin-left:3px;"></i>
        </p>
      </template>
      <template slot="headImg" slot-scope="scope">
        <img style="width: 30px;height: 30px;border-radius: 30px;" :src="scope.row.headImg" alt=""/>
      </template>
    </jvs-table>
    <userSelector
      ref="userSelector"
      :userEnable="true"
      @submit="addCheckUSer">
    </userSelector>
  </div>
</template>

<script>

import {getAppRolePage, setAppRole} from "@/api/application";
import UserSelector from "@/components/basic-assembly/userSelector";

export default {
  name: "appRole",
  components: {
    UserSelector
  },
  data () {
    return {
      tableData: [],
      option: {
        addBtn: false,
        showOverflow: true,
        menu: false,
        submitLoading: false,
        column: [
          {
            label: "姓名",
            prop: "realName",
          },
          {
            label: "头像",
            prop: "headImg",
            slot: true
          }
        ]
      },
      queryParams: {},
      tableLoading: false,
      userIds: [],
    }
  },
  created() {
  },
  methods: {
    // 添加人员 提交
    addCheckUSer(checkList) {
      this.userIds = checkList.map(item => {
        return item.id
      })
      this.$forceUpdate()
      setAppRole(this.userIds).then(res => {
        if (res.data && res.data.code == 0) {
          this.$notify({
            title: '提示',
            message: '添加成功',
            position: 'bottom-right',
            type: 'success'
          });
          this.getAppRolePage()
        }
      })
      this.$refs.userSelector.closeDialog()
    },
    // 打开人员选择组件
    handleUserSelectOpen() {
      this.$refs.userSelector.openDialog()
    },
    // 搜素回调
    searchChange (form) {
      this.queryParams = form
      this.getAppRolePage()
    },
    getAppRolePage() {
      this.tableLoading = true
      getAppRolePage().then(res => {
        if (res.data && res.data.code == 0 && res.data.data) {
          this.tableLoading = false
          this.tableData = [...res.data.data]
        }
      })
    },
  }
}
</script>

<style lang="scss" scoped>
.app-role-page{
  border-radius: 6px;
  background-color: #ffffff;
}
</style>
