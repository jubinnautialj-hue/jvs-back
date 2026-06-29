<template>
  <el-dialog
    :title="$langt('role.permisionserve')"
    fullscreen
    append-to-body
    class="permission-setting-dialog"
    :visible.sync="dialogVisible"
    :before-close="handleClose">
    <div v-if="dialogVisible" style="width: 100%;height: calc(100vh - 90px);box-sizing: border-box;overflow: hidden;">
      <div v-if="tableLoading" class="table-body-slot-loading"></div>
      <div v-else class="permission-config-box">
        <div class="permission-top">
          <jvs-button icon="el-icon-plus" type="primary" size="mini" permisionFlag="" @click="addPermision(null)">{{$langt('role.perAdd')}}</jvs-button>
        </div>
        <div class="table">
          <el-table
            :data="tableData"
            :span-method="objectSpanMethod"
            border
            style="width: 100%;">
            <el-table-column v-for="item in tableOption.column" :key="item.prop" :label="item.label" :prop="item.prop">
            </el-table-column>
            <el-table-column :label="$langt('table.oprate')" width="100">
              <template slot-scope="scope">
                <jvs-button type="text" size="mini" permisionFlag="" @click="addPermision(scope.row)">{{$langt('table.edit')}}</jvs-button>
                <jvs-button type="text" size="mini" permisionFlag="" @click="deleteRow(scope.row)"><span style="color: #F56C6C">{{$langt('table.delete')}}</span></jvs-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>
    <!-- 资源  新增 修改 -->
    <el-dialog
      :title="isEdit ? $langt('role.perAdd'): $langt('role.perEdit')"
      :visible.sync="formVisible"
      append-to-body
      :before-close="formClose">
      <div v-if="formVisible">
        <jvs-form ref="permissionForm" :option="option" :formData="formData" @submit="handleSubmit" @cancalClick="formClose"></jvs-form>
      </div>
    </el-dialog>
  </el-dialog>
</template>

<script>
import { getPermissionAll, addPermission, editPermission, delPermission } from "@/views/upms/views/role/api";

export default {
  name: "permissionConfig",
  props: {},
  data () {
    return {
      dialogVisible: false,
      tableLoading: false,
      tableOption: {
        viewBtn: false,
        addBtn: false,
        editBtn: false,
        delBtn: false,
        showOverflow: false,
        menuWidth: 100,
        column: [
          {
            label: "分组",
            prop: "clientName"
          },
          {
            label: "分类",
            prop: "groupName",
          },
          {
            label: "资源名称",
            prop: "name",
          },
          {
            label: "资源标识",
            prop: "permission",
          },
          {
            label: "接口地址",
            prop: "api"
          }
        ]
      },
      tableData: [],
      permissionClientCount: {},
      permissionTypeCount: {},
      isEdit: false, // 是否编辑操作
      formVisible: false,
      apiList: [],
      option: {
        emptyBtn: false,
        cancal: false,
        submitBtnText: this.$langt('common.confirm'),
        labelWidth: '100px',
        submitLoading: false,
        formAlign: 'top',
        cancal: true,
        column: [
          {
            label: '分组',
            prop: 'clientName'
          },
          {
            label: '分类',
            prop: 'groupName'
          },
          {
            label: '资源类型',
            prop: 'type',
            type: 'select',
            dicData: [
              {label: '系统资源', value: 'tenant'},
              {label: '平台资源', value: 'platform'},
            ]
          },
          {
            label: '资源名称',
            prop: 'name'
          },
          {
            label: '资源标识',
            prop: 'permission'
          },
          {
            label: '接口地址',
            prop: 'api'
          }
        ]
      },
      formData: {}
    }
  },
  created () {
    this.tableOption.column.filter(col => {
      if(col.label) {
        col.label = this.$langt(`role.perColumn.${col.prop}.label`)
      }
    })
    this.option.column.filter(col => {
      if(col.label) {
        col.label = this.$langt(`role.perColumn.${col.prop}.label`)
      }
      if(col.dicData && col.dicData.length > 0) {
        col.dicData.filter(dit => {
          dit.label = this.$langt(`role.perColumn.${col.prop}.dicData.${dit.value}`)
        })
      }
    })
  },
  methods: {
    // 获取全部资源
    getPermissionAllHandle () {
      this.tableLoading = true
      getPermissionAll().then(res => {
        if(res.data && res.data.code == 0) {
          this.tableData = res.data.data
          this.formatCount(this.tableData)
          this.tableLoading = false
        }
      })
    },
    async openDialog() {
      await this.getPermissionAllHandle()
      this.dialogVisible = true
    },
    formatCount (list) {
      this.permissionClientCount = {}
      this.permissionTypeCount = {}
      list.filter(row => {
        if(!this.permissionClientCount[row.clientName]) {
          this.permissionClientCount[row.clientName] = {
            count: 1,
            id: row.id
          }
        }else{
          this.permissionClientCount[row.clientName] = {
            count: this.permissionClientCount[row.clientName].count + 1,
            id: this.permissionClientCount[row.clientName].id
          }
        }
        if(!this.permissionTypeCount[row.groupName]) {
          this.permissionTypeCount[row.groupName] = {
            count: 1,
            id: row.id
          }
        }else{
          this.permissionTypeCount[row.groupName] = {
            count: this.permissionTypeCount[row.groupName].count + 1,
            id: this.permissionTypeCount[row.groupName].id
          }
        }
      })
    },
    objectSpanMethod({ row, column, rowIndex, columnIndex }) {
      if(columnIndex === 0) {
        if(this.permissionClientCount[row.clientName] && row.id == this.permissionClientCount[row.clientName].id) {
          return {
            rowspan: this.permissionClientCount[row.clientName].count,
            colspan: 1
          }
        }else{
          return {
            rowspan: 0,
            colspan: 1
          }
        }
      }else if(columnIndex === 1) {
        if(this.permissionTypeCount[row.groupName] && row.id == this.permissionTypeCount[row.groupName].id) {
          return {
            rowspan: this.permissionTypeCount[row.groupName].count,
            colspan: 1
          }
        }else{
          return {
            rowspan: 0,
            colspan: 1
          }
        }
      }else{
        return {
          rowspan: 1,
          colspan: 1
        }
      }
    },
    handleClose() {
      this.dialogVisible = false
      this.$set(this, 'formData', {})
      this.tableData = []
    },
    addPermision (row) {
      if(row) {
        this.formData = JSON.parse(JSON.stringify(row))
        this.isEdit = true
      }else{
        this.formData = {}
        this.isEdit = false
      }
      this.formVisible = true
    },
    formClose () {
      this.formVisible = false
      this.formData = {}
      this.option.submitLoading = false
    },
    handleSubmit (form) {
      if(this.formData.id) {
        this.option.submitLoading = true
        editPermission(this.formData).then(res => {
          if(res.data && res.data.code == 0) {
            this.$notify({
              title: this.$langt('common.tip'),
              message: this.$langt('common.editSuccess'),
              position: 'bottom-right',
              type: 'success'
            })
            this.option.submitLoading = false
            this.formClose()
            this.getPermissionAllHandle()
          }
        })
      }else{
        this.option.submitLoading = true
        addPermission(this.formData).then(res => {
          if(res.data && res.data.code == 0) {
            this.$notify({
              title: this.$langt('common.tip'),
              message: this.$langt('common.addSuccess'),
              position: 'bottom-right',
              type: 'success'
            })
            this.option.submitLoading = false
            this.formClose()
            this.getPermissionAllHandle()
          }
        })
      }
    },
    deleteRow (row) {
      this.$confirm(this.$langt('role.perDelConfirm'), this.$langt('common.tip'), {
        confirmButtonText: this.$langt('common.confirm'),
        cancelButtonText: this.$langt('common.cancel'),
        type: 'warning'
      }).then(() => {
        delPermission(row.id).then(res => {
          if(res.data.code == 0) {
            this.$notify({
              title: this.$langt('common.tip'),
              message: this.$langt('common.delSuccess'),
              position: 'bottom-right',
              type: 'success'
            })
            this.getPermissionAllHandle()
          }
        })
      }).catch(e => {})
    }
  }
}
</script>

<style lang="scss" scoped>
.permission-setting-dialog{
  /deep/.el-dialog.is-fullscreen{
    border-radius: 0;
  }
}
.permission-config-box{
  width: 100%;
  height: 100%;
  overflow: hidden;
  .permission-top{
    margin-bottom: 10px;
  }
  .table{
    width: 100%;
    height: calc(100% - 38px);
    overflow: hidden;
    overflow-y: auto;
  }
}
.table-body-slot-loading{
  width: 100%;
  height: 100%;
  background-image: url('../../../../styles/loading.gif');
  background-repeat: no-repeat;
  background-position: center;
}
</style>
