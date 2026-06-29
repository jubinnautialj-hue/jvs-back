<template>
  <div :class="{'role-list-box': true, 'group-box': !roleId}" style="height:100%;">
    <div class="table-header-left">
      <div :class="{'member': true, 'active': isPermission == 'member'}" @click="handleLeftMenu('member')">{{$langt('role.member')}}</div>
      <div v-if="false" class="permission" @click="handleLeftMenu('data')">{{$langt('role.datapermision')}}</div>
      <div v-if="permissionsList.indexOf('jvs_role_permission_edit') > -1 && roleId" :class="{'permission': true, 'active': isPermission == 'menu'}" @click="handleLeftMenu('menu')">{{$langt('role.permisionSet')}}</div>
    </div>
    <div v-if="isPermission == 'menu'" class="table-header-right">
      <div v-if="permissionsList.indexOf('jvs_role_permission_edit') > -1" class="save-btn">
        <jvs-button type="primary" size="mini" @click="handleSubmit">{{$langt('form.save')}}</jvs-button>
        <jvs-button v-if="permissionsList.indexOf('jvs_permission_add') > -1" icon="el-icon-plus" type="primary" size="mini" @click="addPermissionCfg">{{$langt('role.permisionserve')}}</jvs-button>
      </div>
    </div>
    <div v-show="isPermission == 'menu'" :class="{'menu-permission': true, 'is_en': !is_zh}">
      <div class="desc-info">
        <div>{{$langt('role.desc1')}}</div>
        <div>{{$langt('role.desc2')}}</div>
      </div>
      <div class="permission-table">
        <div class="table-title">
          <div class="title-item" style="width: 160px;box-sizing: border-box;">{{$langt('role.group')}}</div>
          <div class="title-item" style="width: 160px;box-sizing: border-box;">{{$langt('role.type')}}</div>
          <div class="title-item" style="width: calc(100% - 370px);box-sizing: border-box;">{{$langt('role.funcpermision')}}</div>
          <div style="width: 50px;box-sizing: border-box;">
            <el-checkbox :disabled="permissionsList.indexOf('jvs_role_permission_edit') == -1" v-model="checkAll" :indeterminate="isIndeterminate" @change="checkAllChange"></el-checkbox>
          </div>
        </div>
        <div class="table-body">
          <div v-if="permissionLoading" class="loading-back"/>
          <div class="table-body-group" v-for="gItem in menuListTemp" :key="gItem.id">
            <div class="group-cell">{{ gItem.name }}</div>
            <div class="group-con">
              <div class="table-body-item" v-for="(menu, key) in gItem.children" :key="key">
                <div class="category">{{ menu.name }}</div>
                <div class="object">
                  <div class="object-item" v-for="(obj, objKey) in menu.children" :key="objKey">
                    <div class="object-item-name">
                      <span :class="{'check-label': true, 'isCheck': obj.check}" @click="obj.check = !obj.check;handleCheckChange(menu, menu.children);">
                        <span class="check"></span>
                        <span class="label">{{obj.name}}</span>
                      </span>
                      <el-tooltip v-if="obj.remark" class="item" effect="dark" :content="obj.remark" placement="top">
                        <i class="el-icon-question" style="cursor: pointer;margin-left: 5px;color: #909399;"></i>
                      </el-tooltip>
                    </div>
                  </div>
                </div>
                <span :class="{'check-label': true, 'isCheck': menu.check, 'indeterminate': (menu.isIndeterminate && !menu.check)}" style="width: 50px;" @click="menu.check = !menu.check;handleCheckAllChange(menu);">
                  <span class="check"></span>
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <jvs-table
      v-if="isPermission == 'member'"
      :class="{'role-table': true, 'show-all-user-role-table': (rowData.memberScope == 'ALL'), 'show-dept-role-table': (rowData.memberScope == 'DEPT')}"
      pageheadertitle='系统角色'
      ref="multipleTable"
      :data="tableData"
      tooltipEffect="dark"
      style="width: 100%;height:100%;"
      :loading="tableLoading"
      :option="getOption"
      :page="page"
      :defalutFormData="queryParams"
      @search-change="searchChange"
    >
      <template slot="tableTop">
        <div v-if="rowData.memberScope === 'ALL'" class="role-table-top">
          <span>{{$langt('role.all')}}</span>
          <!-- <el-alert title="所有人" type="info" center :closable="false"></el-alert> -->
        </div>
        <div v-if="rowData.memberScope == 'DEPT'" class="role-table-top dept-top">
          <div class="header">
            <div class="cell">{{$langt('role.deptName')}}</div>
            <div class="cell">
              <el-checkbox :disabled="permissionsList.indexOf('jvs_role_edit') == -1" v-model="deptCheckAll" :indeterminate="deptIndeterminate" @change="deptAllChange">{{$langt('role.belowDept')}}</el-checkbox>
            </div>
            <div class="menu-cell">{{$langt('table.oprate')}}</div>
          </div>
        </div>
      </template>
      <template slot="menuLeft">
      </template>
      <template slot="menuRight">
        <jvs-button v-if="permissionsList.indexOf('jvs_role_add') > -1" icon="el-icon-plus" type="primary" size="mini" @click="addGroup">{{$langt('role.addGroup')}}</jvs-button>
        <jvs-button v-if="permissionsList.indexOf('jvs_role_add') > -1" icon="el-icon-plus" type="primary" size="mini" @click="addRole">{{$langt('role.addRole')}}</jvs-button>
        <jvs-button v-if="permissionsList.indexOf('jvs_permission_add') > -1" icon="el-icon-plus" type="primary" size="mini" @click="addPermissionCfg">{{$langt('role.permisionserve')}}</jvs-button>
      </template>
      <template slot="menu" slot-scope="scope">
        <jvs-button v-if="roleId && rowData.memberScope == 'USER'" type="text" size="mini" @click="setRange(scope.row)">{{$langt('role.setRange')}}</jvs-button>
        <jvs-button v-if="roleId && permissionsList.indexOf('jvs_role_delete_user') > -1" type="text" size="mini" @click="removeUser(scope.row)">{{`${rowData.memberScope == 'DEPT' ? $langt('role.removeDept') : $langt('role.removeUser')}`}}</jvs-button>
      </template>
      <template slot="below" slot-scope="scope">
        <el-checkbox v-model="scope.row.below" :disabled="permissionsList.indexOf('jvs_role_edit') == -1" @change="deptRowCheckChange(scope.row)"></el-checkbox>
      </template>
      <template slot="deptRange" slot-scope="scope">
        <div v-if="scope.row.scopes" style="overflow: hidden;text-overflow: ellipsis;">
          <span>{{getScopesName(scope.row.scopes)}}</span>
        </div>
      </template>
    </jvs-table>
    <div class="role-tree">
      <el-tree
        ref="groupRoleTree"
        :data="systemRole"
        node-key="id"
        default-expand-all
        @node-click="roleHandleClick"
        :expand-on-click-node="true"
        :loading="roleLoading"
        :props="defaultProps"
      >
        <span class="customize-tree-node" slot-scope="{ node, data }" >
          <span>
            <i :class="node.icon"></i>
            <span :class="'customize-tree-node-label customize-tree-node-label'+ (data.parent == -1 ? 1 : 2)" :style="'width:'+((193 - (18 * (data.parent == -1 ? 1 : 2))) > 0 ? (193 - (18 * (data.parent == -1 ? 1 : 2))) : 0) +'px;'">{{ node.label }}</span>
          </span>
          <span class="more-icon">
            <el-popover
              popper-class="hover-popver-list"
              placement="right"
              width="50"
              v-model="data.moretool"
              @show="roleHandleClick(data)"
              trigger="click">
              <ul v-if="data.extend && data.extend.type == 'group'" class="base-type-list">
                  <li v-if="permissionsList.indexOf('jvs_role_edit') > -1" @click.stop="editGroup(data)">
                  <span>{{$langt('role.editGroup')}}</span>
                </li>
                <li v-if="permissionsList.indexOf('jvs_role_delete') > -1" @click.stop="() => delGroup(data)">
                  <span style="color: #F56C6C;">{{$langt('role.delGroup')}}</span>
                </li>
              </ul>
              <ul v-else class="base-type-list">
                <li v-if="rowData.memberScope != 'ALL' && permissionsList.indexOf('jvs_role_add_user') > -1" @click.stop="addUser(data)">
                  <span>{{$langt('role.addMember')}}</span>
                </li>
                  <li v-if="permissionsList.indexOf('jvs_role_edit') > -1" @click.stop="editRole(data)">
                  <span>{{$langt('role.editRole')}}</span>
                </li>
                <li v-if="permissionsList.indexOf('jvs_role_delete') > -1" @click.stop="() => delRow(data)">
                  <span style="color: #F56C6C;">{{$langt('role.delRole')}}</span>
                </li>
              </ul>
              <i slot="reference" class="el-icon-more iconhover" @click.stop="moreRole(data)"></i>
            </el-popover>
          </span>
        </span>
      </el-tree>
    </div>
    <div v-if="!roleId" class="empty-view">
      <img src="/jvs-ui-public/img/contentEmpty.png" alt=""/>
      <div>{{$langt('common.rightDrawer.emptyCon')}}</div>
    </div>
    <!-- 添加角色，修改角色 -->
    <role-form ref="roleForm" :list="tableData" @reFresh="reFresh"></role-form>
    <data-authority :dataAuthVisible="dataAuthVisible" :authList="authList" :isBindList="isBindList" :roleData="roleData" @dataAuthClose="dataAuthClose"/>
    <userSelector
      ref="userSelector"
      :userEnable="rowData.memberScope === 'USER'"
      :deptEnable="rowData.memberScope === 'DEPT'"
      :currentActiveName="rowData.memberScope === 'USER' ? 'user' : 'dept'"
      :selectable="true"
      :dialogTitle="$langt('role.userSelect')"
      @submit="addCheckUSer"
    />
    <PermissionSetting ref="permissionConfig" @getTreeData="freshPermission"></PermissionSetting>
    <!-- 成员的管理范围 -->
    <el-dialog
      :title="$langt('role.manageRange')"
      :visible.sync="dialogVisible"
      :before-close="handleClose">
      <div v-if="dialogVisible">
        <div>
          <el-button size="mini" type="primary" icon="el-icon-plus" @click="addDeptList">{{$langt('role.selectDept')}}</el-button>
        </div>
        <div style="margin-top: 10px;">
          <div v-for="(item, index) in deptList" :key="item.id" style="display: flex;align-items: center;font-size: 14px;height: 42px;line-height: 42px;">
            <span style="flex: 1;overflow: hidden;text-overflow: ellipsis;white-space: pre;">{{item.name}}</span>
            <span style="width: 150px;">
              <el-checkbox v-model="item.below">{{$langt('role.belowDept')}}</el-checkbox>
              <i class="el-icon-delete" style="color: #F56C6C;margin-left: 10px;cursor: pointer;" @click="delDeptList(index, deptList)"></i>
            </span>
          </div>
        </div>
        <div style="display: flex;align-items:center;justify-content: center;">
          <el-button size="mini" type="primary" @click="deptRangeSubmit">{{$langt('common.confirm')}}</el-button>
          <el-button size="mini" @click="handleClose">{{$langt('common.cancel')}}</el-button>
        </div>
      </div>
    </el-dialog>
    <!-- 角色分组 -->
    <el-dialog
      :title="$langt(`role.${(groupForm && groupForm.id) ? 'editGroup' : 'addGroup'}`)"
      :visible.sync="groupVisible"
      :before-close="groupClose">
      <div v-if="groupVisible">
        <jvs-form :option="groupFormOption" :formData="groupForm" @submit="groupSubmit" @cancalClick="groupClose"></jvs-form>
      </div>
    </el-dialog>
    <userSelector
      ref="deptSelector"
      :userEnable="false"
      :deptEnable="true"
      :currentActiveName="'dept'"
      :dialogTitle="$langt('role.deptSelect')"
      @submit="addCheckDept"
    />
  </div>
</template>

<script>
import {tableOption} from '../../const/role/index.js'
import userSelector from '@/components/basic-assembly/userSelector'
import roleForm from './roleForm'
import {
  bindMenuAuth,
  delRoleId,
  editUserRole,
  getAllDataAuth,
  getMenuAuth,
  getRoleDataAuth,
  getRoleGroupList,
  addRoleGroup,
  removeRoleGroup,
  getUserByRoleId,
  removeUSerByRoleId,
  getPermissionTree,
  getDeptByRoleId,
  addDeptRole,
  editDeptRole,
  removeDeptByRoleId,
  saveDeptRange
} from './api'
import PageHeader from '@/components/page-header/PageHeader.vue';
import DataAuthority from "./dataAuthority";
import MenuAuthority from "./menuAuthority";
import PermissionSetting from "@/views/upms/views/role/permissionSetting"
import { getStore } from "@/util/store.js";

export default {
  components: { PermissionSetting, MenuAuthority, DataAuthority, 'role-form': roleForm, PageHeader, userSelector },
  computed: {
    getOption () {
      let option = JSON.parse(JSON.stringify(tableOption))
      if(this.rowData && this.rowData.memberScope == 'DEPT') {
        option.column = JSON.parse(JSON.stringify(this.deptOptionColumn))
      }else{
        option.column = JSON.parse(JSON.stringify(tableOption.column))
      }
      option.column.filter(col => {
      if(col.label) {
        col.label = this.$langt(`role.column.${col.prop}.label`)
      }
    })
      return option
    },
    deptIndeterminate () {
      let bool = false
      if(this.rowData && this.rowData.memberScope == 'DEPT') {
        if(this.tableData && this.tableData.length > 0) {
          let count = 0
          this.tableData.filter(row => {
            if(row.below) {
              count += 1
            }
          })
          if(count > 0 && count < this.tableData.length) {
            bool = true
          }
        }
      }
      return bool
    }
  },
  data () {
    // 这里存放数据
    return {
      tableLoading: false,
      permissionLoading: false,
      option: {},
      tableData: [],
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
      },
      queryParams: {},
      dataAuthVisible: false,
      authList: [], // 数据权限列表
      isBindList: [],
      roleData: {},
      rowData: {}, // 行数据
      title: '',
      dataOption: [], // 数据权限
      dept: {}, // 部门权限对象
      deptIds: [],
      pathArr: [],
      perLoading: false,
      defaultProps: {
        label: 'name'
      },
      roleLoading: false,
      systemRole: [], // 系统角色列表
      lastRole: null,
      roleId: undefined,
      terminal: {},
      isPermission: 'member',
      menuList: [], //菜单列表
      menuListTemp: [], //菜单列表 带check标识
      permissionList: [], //权限列表
      checkAll: false, //所有功能全选标识
      isIndeterminate: false, //所有功能部分选中标识
      submitData: [],
      permissionsList: [],
      deptOptionColumn: [
        {
          label: '部门名称',
          prop: 'name'
        },
        {
          label: '当前部门及以下',
          prop: 'below',
          slot: true,
          showOverflow: false
        }
      ],
      deptCheckAll: false,
      dialogVisible: false,
      currentMember: null,
      deptList: [],
      is_zh: false,
      groupVisible: false,
      groupForm: null,
      groupFormOption: {
        emptyBtn: false,
        column: [
          {
            label: '角色分组',
            prop: 'name',
            rules: [ { required: true, message: '请输入角色分组', trigger: 'blur' } ]
          }
        ]
      }
    }
  },
  // 方法集合
  methods: {
    // 添加权限配置
    addPermissionCfg() {
      this.$refs.permissionConfig.openDialog()
    },
    // 获取全选状态
    getCheckAllStatus() {
      let count = 0
      let length = 0
      this.menuListTemp.forEach(item => {
        if(item.children && item.children.length > 0) {
          item.children.forEach(it => {
            if(it.children && it.children.length > 0) {
              length += item.children.length
              it.children.forEach(ict => {
                if(ict.check) {
                  count++
                }
              })
            }
          })
        }
      })
      this.$set(this, 'checkAll', count === length)
      this.$set(this, 'isIndeterminate', count > 0 && count < length)
    },
    // 勾选操作对象
    handleCheckObj(arr) {
      arr.forEach(item => {
        if(item.children) {
          item.children.forEach(it => {
            let count = 0
            if(it.children && it.children.length > 0) {
              it.children.forEach(ict => {
                if(ict.check) {
                  count++
                }
              })
            }
            this.$set(it, 'isIndeterminate', count > 0 && count < (it.children ? it.children.length : 0))
            this.$set(it, 'check', count === (it.children ? it.children.length : 0))
            if(it.children) {
              this.handleCheckObj(it.children)
            }
          })
        }
      })
    },
    // 勾选拥有功能权限
    handleCheckPermission(arr) {
      let index = -1
      arr.forEach(item => {
        index = this.permissionList.findIndex(it => {
          return it == item.id
        })
        item.check = index > -1
        if (item.children && item.children.length > 0) {
          this.handleCheckPermission(item.children)
        }
      })
    },
    // 获取角色菜单权限
    getMenuAuth () {
      this.permissionLoading = true
      getMenuAuth(this.roleId).then(res => {
        if (res.data && res.data.code == 0 && res.data.data) {
          this.permissionList = [...res.data.data]
          this.handleCheckPermission(this.menuListTemp)
          this.handleCheckObj(this.menuListTemp)
        }
      }).finally(() => {
        this.permissionLoading = false
      })
    },
    // 提交数据
    handleSubmit() {
      let paramArr = []
      this.submitData.filter(item => {
        paramArr.push(item.id)
      })
      bindMenuAuth(paramArr, this.roleId).then(res => {
        if (res.data && res.data.code == 0) {
          this.$notify({
            title: this.$langt('common.tip'),
            message: this.$langt('common.oprateSuccess'),
            position: 'bottom-right',
            type: 'success'
          });
        }
      })
    },
    // 递归获取提交数据
    getSubmitData(arr) {
      arr.forEach(item => {
        if(item.check) {
          this.submitData.push(item)
        }
        if (item.children && item.children.length > 0) {
          this.getSubmitData(item.children)
        }
      })
    },
    // 获取带 check 标识菜单列表
    getMenuListTemp(bool) {
      const arr = [...this.menuList]
      this.filterMenuList(arr, bool)
      this.menuListTemp = arr
    },
    // 递归获取带 check 标识菜单列表
    filterMenuList(arr, bool) {
      arr.forEach(item => {
        this.$set(item, 'isIndeterminate', !!bool)
        this.$set(item, 'check', !!bool)
        if(item.children && item.children.length > 0) {
          this.filterMenuList(item.children, bool)
        }
      })
    },
    // 所有权限全选
    checkAllChange(e) {
      this.getMenuListTemp(e)
      this.submitData = []
      this.getSubmitData(this.menuListTemp)
    },
    // 功能 check 事件
    handleCheckChange(obj, arr) {
      let count = 0
      arr.filter(item => {
        if(item.check) {
          count++
        }
      })
      this.$set(obj, 'isIndeterminate', (count > 0 && count < arr.length))
      this.$set(obj, 'check', count == arr.length)
      this.submitData = []
      this.getSubmitData(this.menuListTemp)
    },
    // 行数据全选
    handleCheckAllChange(obj) {
      obj.children.forEach(item => {
        this.$set(item, 'check', obj.check)
      })
      this.$set(obj, 'isIndeterminate', false)
      this.submitData = []
      this.getSubmitData(this.menuListTemp)
    },
    // 顶部左侧按钮
    handleLeftMenu (type) {
      this.isPermission = type
    },
    addCheckUSer (list) {
      const arr = list.map(item => {
        return item.id
      })
      if(this.rowData.memberScope == 'USER') {
        editUserRole(this.rowData.id, arr).then(res => {
          if(res.data.code == 0) {
            this.$notify({
              title: this.$langt('common.tip'),
              message: this.$langt('role.editMemberSuccess'),
              position: 'bottom-right',
              type: 'success'
            });
            this.$refs.userSelector.closeDialog()
            this.getList()
          }
        })
      }
      if(this.rowData.memberScope == 'DEPT') {
        addDeptRole(this.rowData.id, arr).then(res => {
          if(res.data.code == 0) {
            this.$notify({
              title: this.$langt('common.tip'),
              message: this.$langt('role.addMemberSuccess'),
              position: 'bottom-right',
              type: 'success'
            });
            this.$refs.userSelector.closeDialog()
            this.getList()
          }
        })
      }
    },
    addUser (data) {
      if(!data.memberScope) {
        data.memberScope = (data.extend && data.extend.memberScope) ? data.extend.memberScope : 'USER'
      }
      this.rowData = data
      if(data) {
        data.moretool = false
      }
      this.$refs.userSelector.openDialog()
    },
    // 数据权限管理
    async addDataAuthority(data) {
      this.roleData = JSON.parse(JSON.stringify(data))
      await getRoleDataAuth(this.roleData.id).then(res => {
        if (res.data && res.data.code == 0 && res.data.data) {
          this.isBindList = [...res.data.data]
        }
      })
      await getAllDataAuth().then(res => {
        if (res.data && res.data.code == 0 && res.data.data) {
          const arr = [...res.data.data]
          arr.forEach(aItem => {
            if(aItem.value) {
              for(let ix in aItem.value) {
                let item = aItem.value[ix]
                item.check = false
                item.dataId = ''
                item.disabled = true
                item.remark = {
                  createByIds: [],
                  dataScopeType: '',
                  deptIds: [],
                  jobIds: [],
                  userId: ''
                }
                item.roleId = this.roleData.id
                for(let index in this.isBindList) {
                  if (this.isBindList[index].dataId === item.id) {
                    item.check = true
                    item.disabled = false
                    item.dataId = this.isBindList[index].dataId
                    item.remark = this.isBindList[index].remark
                    item.roleId = this.isBindList[index].roleId
                  }
                }
              }
            }
          })
          this.authList = [...arr]
          this.dataAuthVisible = true
        }
      })
    },
    // 关闭数据权限管理弹窗
    dataAuthClose() {
      this.dataAuthVisible = false
    },
    searchChange (form) {
      this.queryParams = form
      this.getList()
    },
    getList (page) {
      this.tableLoading=true
      let obj = {
        current: this.page.currentPage,
        size: this.page.pageSize,
        roleId: this.roleId
      }
      let fun = getUserByRoleId
      if(this.rowData.memberScope == 'DEPT') {
        fun = getDeptByRoleId
        this.deptCheckAll = false
      }
      fun(obj).then(({ data }) => {
        this.tableLoading=false
        if (data.code===0) {
          this.tableData=data.data.records
          this.page.total = data.data.total
          this.page.currentPage = data.data.current
          if(this.rowData.memberScope == 'DEPT') {
            this.setDeptCheckAll(this.tableData)
          }
        }
      }).catch(err => {
        this.tableLoading=false
      })
    },
    roleHandleClick (data, node, dom) {
      this.isPermission = 'member'
      if(data && data.extend && data.extend.type != 'group') {
        this.page.currentPage = 1
        if(data) {
          if(!data.memberScope) {
            data.memberScope = (data.extend && data.extend.memberScope) ? data.extend.memberScope : 'USER'
          }
          this.rowData = JSON.parse(JSON.stringify(data))
          if(this.roleId == data.id) {
          }else{
            this.roleId = data.id
          }
        } else {
          this.rowData = {}
          this.roleId = undefined
        }
        this.getMenuAuth()
        this.getList()
      }else{
        this.roleId = ''
      }
    },
    // 删除
    delRow (row) {
      this.$confirm(this.$langt('role.delRoleConfirm'), this.$langt('common.tip'), {
        confirmButtonText: this.$langt('common.confirm'),
        cancelButtonText: this.$langt('common.cancel'),
        type: 'warning'
      }).then(() => {
        delRoleId(row.id).then(({ data }) => {
          if (data.code===0) {
            this.$notify({
              title: this.$langt('common.tip'),
              message: this.$langt('common.delSuccess'),
              position: 'bottom-right',
              type: 'success'
            })
            this.roleId = ''
            this.getRoleListHandle()
            this.getList()
          }
        })
      }).catch(() => {});
    },
    // 刷新
    reFresh () {
      this.getRoleListHandle()
    },
    // 新增
    addRole () {
      this.$refs.roleForm.init('add', null)
    },
    // 修改
    editRole (row) {
      // console.log(row)
      if(!row.memberScope) {
        row.memberScope = 'USER'
      }
      let tp = JSON.parse(JSON.stringify(row.extend))
      if(tp.roleGroupId == '-1') {
        tp.roleGroupId = ''
      }
      this.$refs.roleForm.init('edit', tp)
    },
    // 获取角色列表
    getRoleListHandle () {
      this.roleLoading = true
      getRoleGroupList().then(res => {
        if(res.data.code == 0) {
          this.systemRole = res.data.data
          if(this.systemRole && this.systemRole.length > 0) {
            if(!this.systemRole[0].memberScope) {
              this.systemRole[0].memberScope = (this.systemRole[0].extend && this.systemRole[0].extend.memberScope) ? this.systemRole[0].extend.memberScope : 'USER'
            }
            if(!this.roleId) {
              if(this.systemRole[0].extend && this.systemRole[0].extend.type == 'group') {
                if(this.systemRole[0].children && this.systemRole[0].children.length > 0) {
                  this.$nextTick(() => {
                    this.roleHandleClick(this.systemRole[0].children[0])
                    this.getMenuAuth()
                    this.$refs.groupRoleTree.setCurrentKey(this.roleId)
                  })
                }
              }else{
                this.$nextTick(() => {
                  this.roleHandleClick(this.systemRole[0])
                  this.$refs.groupRoleTree.setCurrentKey(this.roleId)
                  this.getMenuAuth()
                })
              }
            }else{
              this.$nextTick(() => {
                this.$refs.groupRoleTree.setCurrentKey(this.roleId)
              })
            }
          }
          this.roleLoading = false
        }else{
          this.roleLoading = false
        }
      }).catch(e => {
        this.roleLoading = false
      })
    },
    // 角色更多
    moreRole (item) {
      if(this.lastRole) {
        this.lastRole.moretool = false
      }
      this.lastRole = item
    },
    // 移除角色下的用户
    removeUser (row) {
      this.$confirm(this.$langt('role.removeUserConfirm'), this.$langt('common.tip'), {
        confirmButtonText: this.$langt('common.confirm'),
        cancelButtonText: this.$langt('common.cancel'),
        type: 'warning'
      }).then(() => {
        let fun = removeUSerByRoleId
        let id = row.userId
        if(this.rowData.memberScope == 'DEPT') {
          fun = removeDeptByRoleId
          id = row.deptId
        }
        fun(this.roleId, id).then(res => {
          this.$notify({
            title: this.$langt('common.tip'),
            message: this.$langt('common.removeSuccess'),
            position: 'bottom-right',
            type: 'success'
          });
          this.getList()
        })
      }).catch(() => {});
    },
    // 获取资源树
    getPermissionTree () {
      getPermissionTree().then(res => {
        if(res.data && res.data.code == 0) {
          this.menuList = res.data.data
          this.getMenuListTemp()
        }
      })
    },
    freshPermission () {
      this.getList()
      this.getMenuAuth()
    },
    deptAllChange (e) {
      let list = JSON.parse(JSON.stringify(this.tableData))
      list.filter(item => {
        item.below = e
      })
      editDeptRole(this.roleId, list).then(res => {
        if(res.data && res.data.code == 0) {
          this.$notify({
            title: this.$langt('common.tip'),
            message: this.$langt('common.setSuccess'),
            position: 'bottom-right',
            type: 'success'
          })
          this.getList()
        }
      })
    },
    deptRowCheckChange (row) {
      let list = JSON.parse(JSON.stringify(this.tableData))
      list.filter(item => {
        if(item.deptId == row.deptId) {
          item.below = row.below
        }
      })
      this.setDeptCheckAll(list)
      editDeptRole(this.roleId, list).then(res => {
        if(res.data && res.data.code == 0) {
          this.$notify({
            title: this.$langt('common.tip'),
            message: this.$langt('common.setSuccess'),
            position: 'bottom-right',
            type: 'success'
          })
          this.getList()
        }
      })
    },
    setDeptCheckAll (list) {
      let bool = true
      if(list.length > 0) {
        list.filter(li => {
          if(!li.below) {{
            bool = false
          }}
        })
      }else{
        bool = false
      }
      this.deptCheckAll = bool
      this.$forceUpdate()
    },
    setRange (row) {
      this.currentMember = JSON.parse(JSON.stringify(row))
      if(row.scopes) {
        this.deptList = JSON.parse(JSON.stringify(row.scopes))
      }
      this.dialogVisible = true
    },
    handleClose () {
      this.currentMember = null
      this.deptList = []
      this.dialogVisible = false
    },
    addDeptList () {
      this.$refs.deptSelector.openDialog(this.deptList)
    },
    addCheckDept (data) {
      let alreadyIds = []
      this.deptList.filter(dit => {
        alreadyIds.push(dit.id)
      })
      data.filter(dd => {
        if(alreadyIds.indexOf(dd.id) == -1) {
          this.deptList.push(dd)
        }
      })
      this.$forceUpdate()
    },
    delDeptList (index, list) {
      list.splice(index, 1)
      this.$forceUpdate()
    },
    deptRangeSubmit () {
      let scopes = []
      this.deptList.filter(dit => {
        scopes.push({
          deptId: dit.id,
          name: dit.name,
          type: dit.type,
          below: dit.below || false
        })
      })
      saveDeptRange({
        roleId: this.rowData.id,
        userId: this.currentMember.userId,
        scopes: scopes,
      }).then(res => {
        if(res.data && res.data.code == 0) {
          this.$notify({
            title: this.$langt('common.tip'),
            message: this.$langt('common.setSuccess'),
            position: 'bottom-right',
            type: 'success'
          })
          this.getList()
          this.handleClose()
        }
      })
    },
    getScopesName (list) {
      let temp = []
      list.filter(li => {
        if(li.name) {
          temp.push(li.below ? `${li.name} ${this.$langt('role.below')}` : li.name)
        }
      })
      return temp.join(',')
    },
    // 新增
    addGroup () {
      this.groupForm = {}
      this.groupVisible = true
    },
    // 修改
    editGroup (row) {
      this.groupForm = {
        name: row.name || '',
        id: row.id
      }
      this.groupVisible = true
    },
    groupSubmit () {
      addRoleGroup(this.groupForm).then(res => {
        this.$notify({
          title: this.$langt('common.tip'),
          message: this.$langt(this.groupForm.id ? 'common.editSuccess' : 'common.addSuccess'),
          position: 'bottom-right',
          type: 'success'
        })
        this.getRoleListHandle()
        this.getList()
        this.groupClose()
      })
    },
    groupClose () {
      this.groupForm = null
      this.groupVisible = false
    },
    // 删除
    delGroup (row) {
      this.$confirm(this.$langt('role.delGroupConfirm'), this.$langt('common.tip'), {
        confirmButtonText: this.$langt('common.confirm'),
        cancelButtonText: this.$langt('common.cancel'),
        type: 'warning'
      }).then(() => {
        removeRoleGroup(row.id).then(({ data }) => {
          if(data.code === 0) {
            this.$notify({
              title: this.$langt('common.tip'),
              message: this.$langt('common.delSuccess'),
              position: 'bottom-right',
              type: 'success'
            })
            this.getRoleListHandle()
            this.getList()
          }
        })
      }).catch(() => {})
    },
  },
  created () {
    let language = navigator.language || navigator.userLanguage;
    if(/zh/i.test(language) || /zh-CN/i.test(language) || /zh-TW/i.test(language) || /zh-HK/i.test(language)) {
      this.is_zh = true
    }
    this.permissionsList = getStore({name: 'permissions'})
    this.getRoleListHandle()
    this.getPermissionTree()
    this.groupFormOption.column.filter(col => {
      if(col.label) {
        col.label = this.$langt(`role.groupColumn.${col.prop}.label`)
      }
      if(col.rules && col.rules.length > 0) {
        col.rules[0].message = this.$langt(`role.groupColumn.${col.prop}.placeholder`)
      }
    })
  }
}
</script>
<style lang='scss' scoped>
.role-list-box{
  /deep/.role-table{
    .jvs-table-titleTop{
      //width: calc(100% - 250px);
      margin-left: 250px;
      .table-header-left{
        font-size: 14px;
        display: flex;
        align-items: center;
        .member{
          background-color: #f4f6f8;
          border-radius: 4px;
          padding: 4px 20px;
          margin-right: 16px;
          cursor: pointer;
        }
        .permission{
          background-color: #f4f6f8;
          border-radius: 4px;
          padding: 4px 20px;
          margin-right: 16px;
          cursor: pointer;
        }
      }
      .table-top{
        min-height: 20px;
      }
    }
    .el-table{
      width: calc(100% - 250px);
      margin-left: 250px;
    }
    .role-table-top{
      margin-left: 250px;
      // padding: 0 20px;
      width: calc(100% - 250px);
      box-sizing: border-box;
    }
    .dept-top{
      color: #333333;
      font-size: 14px;
      .header{
        background-color: #F6F6F6;
        height: 48px;
        line-height: 48px;
        border-radius: 6px;
        overflow: hidden;
        display: flex;
        align-items: center;
        .cell{
          padding: 0 10px;
          flex: 1;
          box-sizing: border-box;
        }
        .menu-cell{
          width: 200px;
          padding: 0 10px;
          box-sizing: border-box;
        }
      }
    }
  }
  /deep/.show-all-user-role-table{
    .table-body-box{
      display: none;
    }
    .tablepagination{
      display: none;
    }
  }
  /deep/.show-dept-role-table{
    .table-body-box{
      .el-table__header-wrapper{
        display: none;
      }
    }
  }
}
.group-box{
  .table-header-left{
    display: none;
  }
  /deep/.role-table{
    .table-body-box, .tablepagination{
      display: none;
    }
  }
  .empty-view{
    position: absolute;
    left: calc(50% - 95px);
    top: calc(50% - 155px);
    text-align: center;
    z-index: 1;
    img{
      display: block;
    }
    div{
      height: 18px;
      font-family: Microsoft YaHei-Regular, Microsoft YaHei;
      font-weight: 400;
      font-size: 14px;
      color: #3D3D3D;
      line-height: 18px;
    }
  }
}
.page-item{
  position: relative;
  padding: 20px;
  border-radius: 6px;
  background-color: #ffffff;
  .title{
    font-weight: 600;
    font-size: 16px;
    margin-bottom: 16px;
  }
  .role{
    margin-top: 20px;
    .role-box{
      height: 100%;
      border: 1px solid #e7e7e7;
      margin-bottom: 30px;
      border-radius: 6px;
      padding: 15px;
      font-size: 14px;
      .role-title{
        margin-bottom: 12px;
      }
      //line-height: 30px;
    }
  }
}
//@import url(); 引入公共css类
.treebox {
  height: calc(100% - 178px);
  overflow: hidden;
  overflow-y: auto;
  background: #fff;
  padding: 0 20px;
  h3 {
    font-size: 16px;
    margin: 10px 0;
    padding: 0;
  }
}
.base-type-list{
  padding: 0;
  margin: 0;
  li{
    display: flex;
    align-items: center;
    margin: 0;
    height: 32px;
    line-height: 32px;
    //margin-bottom: 10px;
    cursor: pointer;
    padding: 6px 24px;
    i{
      margin-right: 10px;
      font-size: 14px!important;
    }
  }
  li:hover{
    background: #F5F7FA;
  }
  li:nth-last-of-type(1) {
    margin-bottom: 0;
  }
}
.application-list{
  padding: 20px;
  display: flex;
  align-items: center;
  background: #fff;
  position: relative;
  .el-button{
    border-color: transparent;
    background: #F3F3F3;
    color: #333333;
    margin-left: 20px;
  }
  .el-button:nth-of-type(1){
    margin-left: 0;
  }
  .el-button--primary{
    background: #3471FF;
    color: #fff;
  }
  .divider-line{
    position: absolute;
    width: calc(100% - 40px);
    border-bottom: 1px solid #DCDFE6;
    bottom: 0;
    left: 20px;
    margin: 0;
  }
}
</style>
<style lang="scss">
.el-checkbox__input.is-checked + .el-checkbox__label{
  color: #333!important;
}
.el-checkbox__input.is-disabled+span.el-checkbox__label{
  color: #C0C4CC!important;
}
.common-border{
  border-right: 1px solid #eee;
  border-bottom: 1px solid #eee;
}
.menuAuthorityDialog {
  .el-dialog.is-fullscreen {
    scrollbar-width: none; /* firefox */
    -ms-overflow-style: none; /* IE 10+ */
    overflow: hidden;
  }
  .el-dialog.is-fullscreen::-webkit-scrollbar {
    display: none; /* Chrome Safari */
  }
  .el-dialog__header{
    padding: 0;
  }
  .el-dialog__body{
    padding: 0;
    background: #ffffff;
    height: 100%;
    box-sizing: border-box;
    overflow: hidden;
    .el-card {
      height: 100%;
      .el-card__body{
        background: #f0f2f5;
        padding: 0!important;
        height: 100%;
        .pageheader-top{
          padding: 10px;
          background: #fff;
          border-radius: 5px;
        }
      }
    }
  }
  .treebox:hover::-webkit-scrollbar{
    width: 8px;
  }
}
.menuAuthorityDialog{
  .transverse-tree-node {
    .el-tree-node{
      width: 100%;
      .el-tree-node__children{
        .el-tree-node{
          width: 100%;
          .el-tree-node__children{
            // display: flex;
            flex-wrap: wrap;
            .el-tree-node{
              width: auto; // 100%;
              .el-tree-node{
                width: auto;
              }
            }
          }
        }
      }
    }
  }
}
.role-list-box{
  background-color: #fff;
  position: relative;
  .table-header-right{
    position: absolute;
    top: 20px;
    right: 20px;
    font-size: 12px;
    display: flex;
    align-items: center;
  }
  .table-header-left{
    position: absolute;
    top: 20px;
    left: 270px;
    font-size: 12px;
    display: flex;
    align-items: center;
    .member{
      background-color: #f4f6f8;
      border-radius: 4px;
      padding: 4px 20px;
      margin-right: 16px;
      line-height: 20px;
      cursor: pointer;
    }
    .permission{
      background-color: #f4f6f8;
      border-radius: 4px;
      padding: 4px 20px;
      margin-right: 16px;
      cursor: pointer;
      line-height: 20px;
    }
    .active{
      background-color: #3471FF;
      color: #fff;
    }
  }
  .menu-permission{
    width: calc(100% - 270px);
    height: calc(100% - 70px);
    position: absolute;
    top: 70px;
    left: 270px;
    &.is_en{
      height: calc(100% - 50px);
      top: 50px;
      .desc-info{
        height: 96px;
      }
      .permission-table{
        height: calc(100% - 107px)!important;
      }
    }
    .desc-info{
      font-size: 13px;
      line-height: 24px;
      color: #a2a3a5;
      height: 48px;
      margin-bottom: 10px;
    }
    .permission-table{
      width: calc(100% - 20px);
      border-radius: 4px;
      //box-shadow: 0 0 5px #eee;
      font-size: 13px;
      border: 1px solid #eee;
      .el-checkbox__input.is-disabled + span.el-checkbox__label{
        color: #333333!important;
      }
      .table-title{
        width: 100%;
        background-color: #f9fafc;
        display: flex;
        align-items: center;
        border-bottom: 1px solid #eee;
        .title-item{
          border-right: 1px solid #eee;
          text-align: left;
          padding: 10px 20px;
          color: #a2a3a5;
        }
      }
      .table-body{
        height: calc(100vh - 258px);
        overflow-y: auto;
        position: relative;
        .loading-back{
          position: absolute;
          width: 100%;
          height: 100%;
          top: 0;
          left: 0;
          box-sizing: border-box;
          background-color: rgba(255, 255, 255, 0.8);
          background-image: url('../../../../../public/jvs-ui-public/img/loading.gif');
          background-repeat: no-repeat;
          background-position: center;
          //background-size: 200px 160px;
          z-index: 1;
        }
        .table-body-group{
          display: flex;
          align-items: center;
          border-bottom: 1px solid #eee;
          .group-cell{
            width: 160px;
            text-align: center;
            box-sizing: border-box;
          }
          .group-con{
            width: calc(100% - 160px);
            border-left: 1px solid #eee;
          }
          .table-body-item:nth-last-of-type(1) {
            border-bottom: 0;
            .category, .object, .check-box{
              border-bottom: 0;
            }
          }
        }
        .table-body-group:nth-last-of-type(1){
          border-bottom: 0;
        }
        .table-body-item{
          display: flex;
          border-bottom: 1px solid #eee;
          .category{
            display: flex;
            align-items: center;
            padding: 10px 20px;
            width: 160px;
            box-sizing: border-box;
            border-right: 1px solid #eee;
          }
          .object{
            width: calc(100% - 210px);
            box-sizing: border-box;
            display: flex;
            border-right: 1px solid #eee;
            flex-wrap: wrap;
            .object-item{
              display: flex;
              box-sizing: border-box;
              min-width: 200px;
              .object-item-name{
                display: flex;
                align-items: center;
                padding: 10px 20px;
                box-sizing: border-box;
              }
            }
          }
          .check-box{
            width: 50px;
            display: flex;
            align-items: center;
            justify-content: center;
            box-sizing: border-box;
            border-bottom: 1px solid #eee;
          }
          .check-label{
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            .check{
              display: inline-block;
              position: relative;
              border: 1px solid #dcdfe6;
              border-radius: 2px;
              box-sizing: border-box;
              width: 14px;
              height: 14px;
              background-color: #fff;
              z-index: 1;
              transition: border-color .25s cubic-bezier(.71,-.46,.29,1.46),background-color .25s cubic-bezier(.71,-.46,.29,1.46);
            }
            .check::after {
              box-sizing: content-box;
              content: "";
              border: 1px solid #fff;
              border-left: 0;
              border-top: 0;
              height: 7px;
              left: 4px;
              position: absolute;
              top: 1px;
              transform: rotate(45deg) scaleY(0);
              width: 3px;
              transition: transform .15s ease-in .05s;
              transform-origin: center;
            }
            .label{
              margin-left: 10px;
            }
          }
          .isCheck{
            .check{
              background: #3471FF;
              border-color: #3471FF;
            }
            .check::after{
              transform: rotate(45deg) scaleY(1);
            }
          }
          .indeterminate{
            .check{
              background: #3471FF;
              border-color: #3471FF;
            }
            .check::before{
              content: "";
              position: absolute;
              display: block;
              background-color: #fff;
              height: 2px;
              transform: scale(.5);
              left: 0;
              right: 0;
              top: 5px;
            }
          }
          .isDisabled{
            .check{
              background-color: #edf2fc;
              border-color: #dcdfe6;
              cursor: not-allowed;
            }
            .check::after{
              cursor: not-allowed;
              border-color: #c0c4cc;
            }
          }
        }
      }
      .table-body::-webkit-scrollbar {
        display: none; /* Chrome Safari */
        width: 0;
        scrollbar-width: none; /* firefox */
        -ms-overflow-style: none; /* IE 10+ */
      }
    }
  }
  .role-tree {
    position: absolute;
    top: 0;
    left: 0;
    width: 250px;
    height: calc(100% - 10px);
    overflow: hidden;
    overflow-y: auto;
    padding-right: 10px;
    padding-top: 20px;
    padding-bottom: 20px;
    box-sizing: border-box;
    .customize-tree-node {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: space-between;
      font-size: 14px;
      padding-right: 8px;
      .customize-tree-node-label{
        display: block;
        overflow: hidden;
        text-overflow: ellipsis;
        width: 174px;
      }
      .more-icon{
        display: none;
      }
    }
    .customize-tree-node:hover{
      .more-icon{
        display: block;
      }
    }
    .el-tree{
      min-height: calc(100% - 19px);
      .role-list-header{
        font-size: 14px;
        color: #a2a3a5;
        // margin-top: 12px;
        line-height: 36px;
      }
    }
    .el-tree-node.is-current{
      >.el-tree-node__content{
        background-color: #F5F7FA;
      }
    }
  }
  .role-tree::-webkit-scrollbar{
    display: none;
  }
}
</style>
