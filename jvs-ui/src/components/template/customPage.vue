<template>
  <div class="custom-page-list">
    <jvs-table :loading="tableLoading" :option="pageOption" :data="tableData" @on-load="getList" @search-change="searchChange">
      <template slot="menuLeft">
        <jvs-button v-if="modeUserInfo ? modeUserInfo.mode == 'DEV' : true" type="primary" size="mini" @click="setRowHandle(null)">新增</jvs-button>
      </template>
      <template slot="menu" slot-scope="scope">
        <jvs-button v-if="modeUserInfo ? modeUserInfo.mode == 'DEV' : true" size="mini" type="text" @click="setRowHandle(scope.row)">编辑</jvs-button>
        <jvs-button size="mini" type="text" @click="dealRowHandle(scope.row)">权限设置</jvs-button>
        <jvs-button v-if="modeUserInfo ? modeUserInfo.mode == 'DEV' : true" size="mini" type="text" style="color: #f56c6c" @click="delRowHandle(scope.row)">删除</jvs-button>
      </template>
    </jvs-table>
    <el-dialog
      :title="title"
      append-to-body
      :fullscreen="dialogType == 'deal'"
      :visible.sync="settingVisible"
      :before-close="setDetailClose"
    >
      <!-- 新增   修改 -->
      <div v-if="settingVisible && dialogType == 'set'">
        <jvs-form :option="formOption" :formData="rowData" @submit="setDetailSubmit" @cancalClick="setDetailClose"></jvs-form>
      </div>
      <!-- 权限设置 -->
      <div v-if="settingVisible && dialogType == 'deal'" class="permission-page">
        <div class="permission-body">
          <p>应用管理员可直接操作所有应用。<span @click="handleMore('permission-help')">了解更多</span></p>
          <p>应用管理员可创建应用、编辑应用、删除应用、应用权限创建应用是设置的。</p>
          <div class="permission-header">
            <div>
              <jvs-button type="primary" @click="addResources" icon="el-icon-plus">添加标识</jvs-button>
              <jvs-button type="primary" @click="addPermissionGroup" icon="el-icon-plus">添加权限组</jvs-button>
            </div>
            <jvs-button type="primary" @click="savePermision">保存</jvs-button>
          </div>
          <!-- 标识列表 -->
          <div class="resource-list">
            <el-tag v-for="(tag, tix) in operationList" :key="tag.name+'-'+tix" closable @close="delResource(operationList, tix)">{{tag.name}}</el-tag>
          </div>
          <!-- 权限组 -->
          <div class="permission-content">
            <div class="permission-content-item" v-for="(item, key) in permissionList" :key="key">
              <div style="width: 100%; background-color: #DCDFE6;height: 1px;margin-bottom: 16px;"/>
              <el-form label-width="80px" label-position="top">
                <el-radio-group v-model="item.personType" style="margin-right:20px;padding: 10px 0;">
                  <el-radio label="all">所有人全部权限</el-radio>
                  <el-radio label="custom">自定义</el-radio>
                </el-radio-group>
                <el-button v-if="item.personType == 'custom'" size="mini" type="primary" icon="el-icon-plus" @click="handleUserSelectOpen(item, key)">选择人员/角色/部门</el-button>
                <div v-if="item.personType == 'custom'" style="position: relative; padding: 10px 0">
                  <el-tag
                    v-for="tag in item.personnels"
                    size="small"
                    style="margin-right: 4px;margin-bottom: 4px;"
                    :key="tag.id"
                    @close="handleDelUser(tag.id, item.personnels)"
                    closable>
                    {{getTagName(tag)}}
                  </el-tag>
                </div>
                <div v-if="item.personType == 'all'" style="position: relative;padding: 10px 0;"></div>
                <el-form-item label="操作权限" v-if="item.personType != 'all'">
                  <el-checkbox-group v-model="item.operation">
                    <el-checkbox v-for="(op, opKey) in operationList" :key="opKey" :label="op.value">{{ op.name }}</el-checkbox>
                  </el-checkbox-group>
                </el-form-item>
              </el-form>
              <jvs-button v-if="key !== 0" class="del-btn" type="info" @click="deleteGroupItem(item, key)">删除权限组</jvs-button>
            </div>
          </div>
        </div>
        <userSelector
          ref="userSelector"
          :selectable="true"
          :userEnable="true"
          :dept-enable="true"
          :roleEnable="true"
          @submit="addCheckUSer">
        </userSelector>
      </div>
    </el-dialog>
    <!-- 添加变量 -->
    <el-dialog
      title="添加变量"
      :visible.sync="resourceVisible"
      :before-close="resourceClose">
      <div v-if="resourceVisible">
        <jvs-form :option="resourceOption" :formData="resourceForm" @submit="resourceSub" @cancalClick="resourceClose"></jvs-form>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import UserSelector from "@/components/basic-assembly/userSelector";
import { getListExternalPage, addExternalPage, editExternalPage, getExternalPage, delExternalPage, grantExternalPage } from './api'
export default {
  components: {UserSelector},
  props: {
    appInfo: {
      type: Object
    },
    modeUserInfo: {
      type: Object
    }
  },
  data() {
    return {
      tableLoading: false,
      queryParam: {},
      rowData: {},
      title: '',
      tableData: [],
      pageOption: {
        addBtn: false,
        editBtn: false,
        viewBtn: false,
        delBtn: false,
        search: true,
        menuWidth: 150,
        column: [
          {
            label: '页面名称',
            prop: 'name',
            search: true,
            width: '300px'
          },
          {
            label: '页面地址',
            prop: 'url',
            search: true
          }
        ]
      },
      settingVisible: false,
      dialogType: '',
      formOption: {
        emptyBtn: false,
        column: [
          {
            label: '页面名称',
            prop: 'name',
            rules: [ { required: true, message: '请输入项目名称', trigger: 'blur' } ]
          },
          {
            label: '页面地址',
            prop: 'url',
            rules: [ { required: true, message: '请输入页面地址', trigger: 'blur' } ]
          }
        ]
      },
      operationList: [], // 标识集合
      resourceVisible: false,
      resourceForm: {},
      resourceOption: {
        emptyBtn: false,
        column: [
          {
            label: '标识名称',
            prop: 'name',
            rules: [ { required: true, message: '请输入标识名称', trigger: 'blur' } ]
          },
          {
            label: '标识',
            prop: 'value',
            rules: [ { required: true, message: '请输入标识', trigger: 'blur' } ]
          }
        ]
      },
      permissionList: []
    };
  },
  created () {},
  methods: {
    getList() {
      this.tableLoading = true
      getListExternalPage(this.appInfo.id, this.queryParam).then(res => {
        if(res.data && res.data.code == 0) {
          this.tableData = res.data.data
          this.tableLoading = false
        } else {
          this.tableLoading = false
        }
      }).catch(err => {
        this.tableLoading = false
      })
    },
    searchChange (form) {
      this.queryParam = form
      this.getList()
    },
    // 了解更多
    handleMore(str) {
      this.$openUrl('', '_blank', str)
    },
    setRowHandle (row) {
      if(row) {
        this.title = '编辑'
        this.rowData = JSON.parse(JSON.stringify(row))
      }else{
        this.title = '新增'
        this.rowData = {}
      }
      this.dialogType = 'set'
      this.settingVisible = true
    },
    setDetailSubmit (form) {
      if(this.title == '新增') {
        addExternalPage(this.appInfo.id, this.rowData).then(res => {
          if(res.data && res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: '新增成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.setDetailClose()
            this.getList()
          }
        })
      }else{
        editExternalPage(this.appInfo.id, this.rowData).then(res => {
          if(res.data && res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: '修改成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.setDetailClose()
            this.getList()
          }
        })
      }
    },
    setDetailClose () {
      this.settingVisible = false
      this.rowData = {}
      this.permissionList = []
      this.operationList = []
    },
    delRowHandle (row) {
      this.$confirm('确定删除该页面?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          delExternalPage(this.appInfo.id, row.id).then(res => {
            if(res.data && res.data.code == 0) {
              this.$notify({
                title: '提示',
                message: '删除成功',
                position: 'bottom-right',
                type: 'success'
              });
              this.getList()
            }
          })
        }).catch(() => {})
    },
    dealRowHandle (row) {
      getExternalPage(this.appInfo.id, row.id).then(res => {
        if(res.data && res.data.code == 0) {
          if(res.data.data.permissions) {
            this.permissionList = res.data.data.permissions
          }
          if(res.data.data.resources) {
            this.operationList = res.data.data.resources
          }
          this.rowData = JSON.parse(JSON.stringify(row))
          this.dialogType = 'deal'
          this.title = '权限设置'
          this.settingVisible = true
        }
      })
    },
    // 添加标识
    addResources () {
      this.resourceVisible = true
    },
    resourceSub (form) {
      this.operationList.push(this.resourceForm)
      this.resourceClose()
    },
    resourceClose () {
      this.resourceForm = {}
      this.resourceVisible = false
    },
    // 删除标识
    delResource (list, index) {
      list = list.splice(index, 1)
    },
    // 获取tag名字
    getTagName(obj) {
      let header = ''
      switch (obj.type) {
        case 'user':
          break;
        case 'role':
          header = '（角色）';
          break;
        case 'dept':
          header = '（部门）';
          break;
        case 'group':
          header = '（群组）';
          break;
        default: break;
      }
      return header + obj.name
    },
    // 添加权限组
    addPermissionGroup() {
      const obj = {
        id: new Date().getTime(),
        operation: [],
        personType: 'all',
        personnels: [],
        scopeList: [],
        conditionList: []
      }
      this.permissionList.push(obj)
    },
    // 删除单个权限组
    deleteGroupItem(obj, key) {
      this.permissionList.splice(key, 1)
    },
    // 打开人员选择组件
    handleUserSelectOpen(item, key) {
      this.$refs.userSelector.openDialog(item.personnels)
      this.key = key
    },
    // 删除标签
    handleDelUser(id, arr) {
      const index = arr.findIndex(item => {
        return item.id === id
      })
      arr.splice(index, 1)
    },
    // 添加人员 提交
    addCheckUSer(checkList) {
      this.permissionList[this.key].personnels = checkList
      this.$refs.userSelector.closeDialog()
    },
    savePermision () {
      let obj = {
        id: this.rowData.id,
        permissions: this.permissionList,
        resources: this.operationList
      }
      grantExternalPage(this.appInfo.id, obj).then(res => {
        if(res.data && res.data.code == 0) {
          this.$notify({
            title: '提示',
            message: '授权成功',
            position: 'bottom-right',
            type: 'success'
          });
          this.setDetailClose()
          this.getList()
        }
      })
    }
  },
};
</script>

<style lang="scss" scoped>
.custom-page-list{
  margin-top: 15px;
  .jvs-table{
    .jvs-table-top .search-form{
      border-bottom: 0;
      padding-top: 0;
    }
    .table-body-box{
      padding: 0;
      /deep/.el-table{
        .el-table__header-wrapper{
          tr{
            th{
              background: #f6f6f6;
            }
          }
        }
      }
    }
    /deep/.el-table__body-wrapper{
      height: calc(100vh - 355px - 90px)!important;
    }
  }
}
.permission-page{
  .permission-body{
    padding: 20px 30px;
    background-color: #ffffff;
    border-radius: 6px;
    .title {
      font-weight: bold;
      font-size: 16px;
      margin-bottom: 16px;
    }
    p {
      color: #b3b3b3;
      span {
        color: #3471ff;
        cursor: pointer;
      }
    }
    .permission-header{
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .resource-list{
      margin-top: 16px;
      display: flex;
      align-items: center;
      .el-tag{
        margin-bottom: 10px;
        margin-right: 10px;
      }
    }
    .permission-content{
      margin-top: 16px;
      max-height: calc(100vh - 200px);
      overflow-y: auto;
      padding-bottom: 30px;
      box-sizing: border-box;
      .permission-content-item{
        position: relative;
        .del-btn{
          position: absolute;
          right: 20px;
          top: 16px;
          cursor: pointer;
        }
        .user-select{

        }
      }
    }
  }
}
.condi-box{
  h4{
    color: #666;
    padding: 0;
    margin: 0;
    font-weight: normal;
  }
  .el-row{
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 10px;
    /deep/ .el-select{
      margin-right: 10px;
      .el-input{
        width: 200px;
      }
    }
    .del-button-row{
      cursor: pointer;
      margin-left: 10px;
    }
  }
  p{
    display: flex;
    justify-content: center;
    align-items: center;
  }
}
</style>
