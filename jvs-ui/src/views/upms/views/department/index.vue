<!-- 部门管理 -->
<template>
  <div style="height: 100%;">
    <div class="department-list-manage">
      <jvs-table
        pageheadertitle='组织机构'
        refs="multipleTable"
        :data="tableData"
        tooltipEffect="dark"
        style="width: 100%"
        :loading="tableLoading"
        :option="option"
        :page="page"
        :defalutFormData="queryParams"
        @on-load="getList"
        @size-change="sizeChange"
        @sort-change="handleSort"
        @current-change="currentChange"
        @search-change="searchChange"
      >
        <template slot="menuLeft">
          <div class="table-btn-box">
            <jvs-button type="primary" size="mini" v-if="permissionsList.indexOf('jvs_user_invite') > -1" @click="invitationHandle">{{$langt('department.topButton.invite')}}</jvs-button>
            <jvs-button type="primary" size="mini" v-if="permissionsList.indexOf('jvs_user_add') > -1" @click="editItem(null, 'add', null)">{{$langt('department.topButton.addUser')}}</jvs-button>
            <jvs-button type="primary" size="mini" v-if="permissionsList.indexOf('jvs_dept_add') > -1" @click="dialogVisibleShow('add', null, null, 'dept')">{{$langt('department.topButton.addDept')}}</jvs-button>
            <jvs-button type="primary" size="mini" v-if="permissionsList.indexOf('jvs_dept_add') > -1" @click="dialogVisibleShow('add', null, null, 'branchOffice')">{{$langt('department.topButton.addOff')}}</jvs-button>
            <jvs-button type="primary" size="mini" v-if="permissionsList.indexOf('jvs_user_import') > -1" @click="importUserDialogVisible = true;">{{$langt('department.topButton.importUser')}}</jvs-button>
            <jvs-button type="primary" size="mini" v-if="permissionsList.indexOf('jvs_user_export') > -1" @click="downloadModel">{{$langt('department.topButton.exportTmp')}}</jvs-button>
            <!-- <jvs-button type="primary" size="mini" permisionFlag="jvs_user_sync_dept" @click="sysDept('DINGTALK_INSIDE')" :loading="sysddLoading" :disabled="sysqwLoading">同步钉钉组织</jvs-button> -->
            <!-- <jvs-button type="primary" size="mini" permisionFlag="jvs_user_sync_dept" @click="sysDept('WECHAT_ENTERPRISE_WEB')" :loading="sysqwLoading" :disabled="sysddLoading">同步企业微信组织</jvs-button> -->
            <jvs-button v-for="(item, key) in syncBtns" :key="key" type="primary" size="mini" v-if="permissionsList.indexOf('jvs_user_sync_dept') > -1" @click="sysDept(item, syncObj[item])" :loading="sysqyLoading" :disabled="sysqyLoading">
              {{$langt('department.topButton.sys')}}{{ syncObj[item] }}{{$langt('department.topButton.org')}}
            </jvs-button>
          </div>
        </template>
        <template slot="menuRight">
          <el-row class="table-show-right-tool">
            <!-- 表格工具 -->
            <p class="search-p">
              <el-input :placeholder="$langt('table.keyword')" prefix-icon="el-icon-search" size="mini" v-model="queryParams.keywords" @keyup.enter.native="searchChange(Object.assign(queryParams, {current: 1}))" @blur="searchChange(Object.assign(queryParams, {current: 1}))" clearable @clear="searchChange(Object.assign(queryParams, {current: 1}))"></el-input>
            </p>
            <p @click="searchChange(Object.assign(queryParams, {current: 1}))" style="font-size: 14px;">
              <i class="el-icon-refresh-right" style="cursor:pointer;"></i>
              <span>{{$langt('table.fresh')}}</span>
            </p>
          </el-row>
        </template>
        <template slot="menu" slot-scope="scope">
          <jvs-button type="text" size="mini" v-if="permissionsList.indexOf('jvs_user_edit') > -1" @click="editItem(scope.row, 'edit')">{{$langt('table.edit')}}</jvs-button>
          <jvs-button type="text" size="mini" v-if="permissionsList.indexOf('upms_dept_viewUser') > -1" @click="editItem(scope.row, 'view')">{{$langt('table.view')}}</jvs-button>
          <jvs-button type="text" size="mini" v-if="permissionsList.indexOf('upms_dept_permision_data') > -1 && false" @click="setPermissin(scope.row)">{{$langt('department.lineButton.dataPer')}}</jvs-button>
          <jvs-button size="mini" type='text' v-if="permissionsList.indexOf('jvs_user_delete') > -1 && !scope.row.cancelFlag" @click="disableHandle(scope.row)">{{$langt('table.delete')}}</jvs-button>
          <jvs-button size="mini" type='text' v-if="permissionsList.indexOf('jvs_user_password') > -1" @click="editPassHandle(scope.row)">{{$langt('department.lineButton.editPass')}}</jvs-button>
        </template>
        <template slot="headImg" slot-scope="scope">
          <img v-if="scope.row.headImg" :src="scope.row.headImg" alt="" style="display: inline-block;width: 40px;height: 40px;">
        </template>
        <template slot="roleNames" slot-scope="scope">
          <div style="width: 100%;overflow:hidden;text-overflow: ellipsis;white-space: pre;">{{ scope.row.roleNames && scope.row.roleNames.join(",") }}</div>
        </template>
        <template slot="deptName" slot-scope="scope">
          <div style="width: 100%;overflow:hidden;text-overflow: ellipsis;white-space: pre;">{{ scope.row.deptName && scope.row.deptName.join(",") }}</div>
        </template>
      </jvs-table>
      <div class="treeBox dept-treeBox">
        <div :class="{'treeBox-title': true, 'treeBox-title-check': !selectOneId}" @click="queryAllHandle">{{$langt('common.all')}}</div>
        <el-tree
          ref="deptTree"
          :data="treedata"
          node-key="id"
          default-expand-all
          @node-click="handleNodeClick"
          :expand-on-click-node="false"
          :loading="treeLoading"
          :props="defaultProps"
        >
          <span
            class="customize-tree-node"
            slot-scope="{ node, data }"
          >
            <span>
              <i :class="node.icon"></i>
              <span :class="'customize-tree-node-label customize-tree-node-label'+node.level" :style="'width:'+((253 - (18 * node.level)) > 0 ? (253 - (18 * node.level)) : 0) +'px;'">{{ node.label }}</span>
            </span>
            <span class="more-icon">
              <el-popover
                popper-class="hover-popver-list"
                placement="right"
                width="50"
                v-model="data.moretool"
                trigger="click">
                <ul class="base-type-list">
                  <li v-if="permissionsList.indexOf('jvs_dept_edit') > -1 && data.extend.type == 'dept'" @click.stop="() => dialogVisibleShow('edit', node, data, 'dept')">
                    <span>{{$langt('department.leftButton.edit')}}</span>
                  </li>
                  <li v-if="permissionsList.indexOf('jvs_dept_add') > -1" @click.stop="() => dialogVisibleShow('addchild', node, data, 'dept')">
                    <span>{{$langt('department.leftButton.addChild')}}</span>
                  </li>
                  <li v-if="permissionsList.indexOf('jvs_dept_edit') > -1 && data.extend.type == 'branchOffice'" @click.stop="() => dialogVisibleShow('edit', node, data, 'branchOffice')">
                    <span>{{$langt('department.leftButton.editOff')}}</span>
                  </li>
                  <li v-if="permissionsList.indexOf('jvs_dept_add') > -1" @click.stop="() => dialogVisibleShow('addchild', node, data, 'branchOffice')">
                    <span>{{$langt('department.leftButton.addOff')}}</span>
                  </li>
                  <li v-if="false && permissionsList.indexOf('jvs_user_add') > -1 && data.level != 1" @click.stop="() => editItem(null, 'add', data)">
                    <span>{{$langt('department.leftButton.addMember')}}</span>
                  </li>
                  <li v-if="permissionsList.indexOf('jvs_dept_delete') > -1" @click.stop="() => remove( node ,data, '机构')">
                    <span style="color: #F56C6C;">{{$langt('table.delete')}}</span>
                  </li>
                </ul>
                <i slot="reference" class="el-icon-more iconhover" @click.stop="moreDept(data)"></i>
              </el-popover>
            </span>
          </span>
        </el-tree>
      </div>
    </div>
    <!-- 部门 -->
    <el-dialog
      :title="title"
      width="75%"
      :visible.sync="dialogVisible"
      :before-close="handleClose"
      :close-on-click-modal="true"
      :close-on-press-escape="true"
    >
      <div v-if="dialogVisible">
        <div v-if="formType == 'dept' || formType == 'branchOffice'">
          <jvs-form refs="Form" :option="deptOption" :formData="Form" @submit="doSubmit" @cancalClick="handleClose">
            <template slot="parentIdForm">
              <el-cascader
                style="width:100%;"
                v-model="Form.parentId"
                size="mini"
                :options="treedata"
                clearable
                :show-all-levels="false"
                :props="{
                  expandTrigger: 'hover',
                  checkStrictly: true,
                  multiple: false,
                  children: 'children',
                  label: 'name',
                  value: 'id'
                }"
              >
              </el-cascader>
            </template>
          </jvs-form>
        </div>
      </div>
    </el-dialog>
    <!-- 用户信息 -->
    <el-dialog
      :title="title"
      :visible.sync="userVisible"
      :before-close="userClose"
      :close-on-click-modal="false"
    >
      <jvs-form
        style="padding: 0 60px"
        v-if="userVisible"
        refs="userForm"
        :option="formOption"
        :formData="userForm"
        :defalutFormData="userForm"
        @submit="submitHandle"
      >
        <template slot="accountNameForm">
          <div class="jvs-form-item" style="display:flex;align-items:center;">
            <el-input v-model="userForm.accountName" @blur="noRepeatName(userForm.accountName)"></el-input>
            <span class="el-form-item__error" v-if="disSubmit">{{$langt('department.userRepeat')}}</span>
          </div>
        </template>
        <template slot="roleIdsForm">
          <div class="jvs-form-item" style="display:flex;align-items:center;">
            <el-select v-model="userForm.roleIds" :placeholder="$langt('department.role')" multiple size="mini" style="width: 100%;">
              <el-option
                v-for="sitem in roleLsit"
                :key="sitem.id"
                :label="sitem.roleName"
                :value="sitem.id"
              >
                <span style="float: left">{{ sitem.roleName }}</span>
                <!-- <span style="float: right; color: #8492a6; font-size: 13px">{{ sitem.value }}</span> -->
              </el-option>
            </el-select>
          </div>
        </template>
        <template slot="deptIdForm">
          <div class="jvs-form-item" style="display:flex;align-items:center;">
            <el-cascader
              :disabled="(title == $langt('table.add') && currentNode) ? true : false"
              style="width:100%;"
              v-model="userForm.deptId"
              size="mini"
              :options="deptList"
              :props="{ checkStrictly: true, expandTrigger: 'hover', children: 'children', label: 'name', value: 'id', multiple: true, emitPath: false }"
              clearable
              :show-all-levels="false"
            ></el-cascader>
          </div>
        </template>
      </jvs-form>
    </el-dialog>
    <!-- 导入用户 -->
    <el-dialog
      v-if="importVisible"
      width="75%"
      :title="$langt('department.importUser')"
      :fullscreen="(fileList && fileList.length > 5) ? true : false"
      :class="{'import-user-dialog': true, 'form-fullscreen-dialog user-import-fullscreen-dialog': (fileList && fileList.length > 5)}"
      :visible.sync="importVisible"
      :close-on-click-modal="false"
      :before-close="cancelHandle">
      <jvs-table :option="importOption" :data="fileList">
        <template slot="menu" slot-scope="scope">
          <jvs-button size="mini" type="text" @click="delImportRow(scope.row, scope.index)">{{$langt('table.delete')}}</jvs-button>
        </template>
        <!-- 用户真名 -->
        <template slot="realName" slot-scope="scope">
          <div class="error-tip-item">
            <el-input v-model="scope.row.realName" size="mini" :class="{'err-item': repeatHandle('realName', scope.row.realName)}"></el-input>
          </div>
        </template>
        <!-- 昵称 -->
        <template slot="nickName" slot-scope="scope">
          <el-input v-model="scope.row.nickName" size="mini" :class="{'err-item': repeatHandle('nickName', scope.row.nickName)}"></el-input>
        </template>
        <template slot="accountName" slot-scope="scope">
          <div class="error-tip-item">
            <el-input v-model="scope.row.accountName" size="mini" :class="{'err-item': repeatHandle('accountName', scope.row.accountName)}"></el-input>
          </div>
        </template>
        <!-- 性别 -->
        <template slot="sex" slot-scope="scope">
          <el-radio-group v-model="scope.row.sex">
            <el-radio :label="'男'">{{$langt('department.男')}}</el-radio>
            <el-radio :label="'女'">{{$langt('department.女')}}</el-radio>
          </el-radio-group>
        </template>
        <!-- 邮箱 -->
        <template slot="email" slot-scope="scope">
          <el-input v-model="scope.row.email" size="mini" :class="{'err-item': repeatHandle('email', scope.row.email)}"></el-input>
        </template>
        <!-- 电话 -->
        <template slot="phone" slot-scope="scope">
          <div class="error-tip-item">
            <el-input v-model="scope.row.phone" size="mini" :class="{'err-item': repeatHandle('phone', scope.row.phone)}"></el-input>
          </div>
        </template>
        <template slot="roleId" slot-scope="scope">
          <el-select v-model="scope.row.roleId" :placeholder="$langt('department.role')" multiple size="mini" style="width: 100%;" collapse-tags>
            <el-option
              v-for="sitem in roleLsit"
              :key="sitem.id"
              :label="sitem.roleName"
              :value="sitem.id"
            >
              <span style="float: left">{{ sitem.roleName }}</span>
              <!-- <span style="float: right; color: #8492a6; font-size: 13px">{{ sitem.value }}</span> -->
            </el-option>
          </el-select>
        </template>
        <template slot="deptId" slot-scope="scope">
          <el-cascader
            v-model="scope.row.deptId"
            size="mini"
            :options="deptList"
            :props="{ checkStrictly: true, expandTrigger: 'hover', children: 'children', label: 'name', value: 'id' }"
            clearable
            :show-all-levels="false"
          ></el-cascader>
        </template>
        <template slot="jobId" slot-scope="scope">
           <el-select v-model="scope.row.jobId" :placeholder="$langt('department.userColumn.jobId.label')" size="mini" style="width: 100%;">
            <el-option
              v-for="sitem in allPost"
              :key="sitem.id"
              :label="sitem.name"
              :value="sitem.id"
            >
              <span style="float: left">{{ sitem.name }}</span>
              <!-- <span style="float: right; color: #8492a6; font-size: 13px">{{ sitem.value }}</span> -->
            </el-option>
          </el-select>
        </template>
        <template slot="status" slot-scope="scope">
          <el-switch size="mini" v-model="scope.row.status"></el-switch>
        </template>
        <template slot="employeeNo" slot-scope="scope">
          <el-input size="mini" v-model="scope.row.employeeNo"></el-input>
        </template>
        <template slot="birthday" slot-scope="scope">
          <el-date-picker
            size="mini"
            v-model="scope.row.birthday"
            type="date"
            value-format="yyyy-MM-dd"
            :placeholder="$langt('department.date')">
          </el-date-picker>
        </template>
        <template slot="hireDate" slot-scope="scope">
          <el-date-picker
            size="mini"
            v-model="scope.row.hireDate"
            type="date"
            value-format="yyyy-MM-dd"
            :placeholder="$langt('department.date')">
          </el-date-picker>
        </template>
      </jvs-table>
      <el-row style="display:flex;align-items:center;justify-content:center;margin-top:20px;">
        <jvs-button size="mini" type="primary" :disabled="fileList.length == 0" :loading="importLoading" @click="importHandle">{{$langt('table.import')}}</jvs-button>
        <jvs-button size="mini" @click="cancelHandle">{{$langt('common.cancel')}}</jvs-button>
      </el-row>
    </el-dialog>
    <!-- 邀请 -->
    <invitation ref="invitation"></invitation>
    <!-- 修改密码 -->
    <el-dialog
      :title="$langt('department.editPass')"
      :visible.sync="passVisible"
      :close-on-click-modal="false"
      :before-close="passClose">
      <div v-if="passVisible" class="user-info-content-dialog">
        <jvs-form ref="passForm" :option="passOption" :formData="passForm" @submit="submitPassWord" @cancalClick="passClose">
        </jvs-form>
      </div>
    </el-dialog>
    <!-- 导入数据 -->
    <el-dialog
      :title="$langt('department.importUser')"
      :visible.sync="importUserDialogVisible"
      width="720px"
      append-to-body
      :close-on-click-modal="false"
      :before-close="handleCloseImport">
      <div class="import-data-box">
        <el-upload
        ref="uploadBtn"
          style="display: inline-block;margin: 0 10px;"
          action="/mgr/jvs-auth/user/import"
          accept=".xls,.xlsx"
          :limit="1"
          :on-change="handleFileChange"
          :on-success="importView"
          :on-error="errHandle"
          :show-file-list="false"
          :file-list="fileList"
          :headers="headers"
          class="import-data-upload"
          :before-upload="beforeUpload"
          drag
        >
          <div class="el-upload__text">
            <svg aria-hidden="true" style="width: 24px; height: 24px;margin-bottom: 16px">
              <use xlink:href="#icon-upload"></use>
            </svg>
            <div>{{$langt('department.uploadDialog.desc')}}</div>
            <div style="color: #a2a3a5;font-size: 12px;margin-top: 8px;">{{$langt('department.uploadDialog.info')}}</div>
          </div>
        </el-upload>
        <div class="upload-explain">
          <span style="color: #a2a3a5;">{{$langt('department.uploadDialog.tip')}}</span>
          <ul>
            <li style="list-style: disc">{{$langt('department.uploadDialog.tipli1')}}<span>{{$langt('department.uploadDialog.tipli1_1')}}</span>{{$langt('department.uploadDialog.tipli1_2')}}</li>
            <li style="list-style: disc">{{$langt('department.uploadDialog.tipli2')}}<span>{{$langt('department.uploadDialog.tipli2_1')}}</span>{{$langt('department.uploadDialog.tipli2_2')}}</li>
            <li style="list-style: disc">{{$langt('department.uploadDialog.tipli3')}}<span>{{$langt('department.uploadDialog.tipli3_1')}}</span>{{$langt('department.uploadDialog.tipli3_2')}}</li>
            <li style="list-style: disc">{{$langt('department.uploadDialog.tipli4')}}</li>
            <li style="list-style: disc">{{$langt('department.uploadDialog.tipli5')}}<span>{{$langt('department.uploadDialog.tipli5_1')}}</span>{{$langt('department.uploadDialog.tipli5_2')}}</li>
          </ul>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getDeptList,
  getDeptUserList,
  AddDept,
  addUser,
  editUser,
  enableDisUser,
  editDept,
  deleteDept,
  sysDepartment,
  getSyncBtns
} from './api'
import { getRoleList } from '../role/api'
import { getPostList } from '../postList/api'
import { tableOption, formOptionColumn, importOptionData } from './option'
import {sendMyRequire} from '@/api/newDesign'
import { importUser } from '../../api/user.js'
import invitation from './invitation'
import {setUserPassWord} from '@/api/admin/user'
import { encryption } from "@/util/util";
import {enCodePasswordKey} from "@/const/const"
import {isMobile} from '@/util/validate'
import { getStore } from "@/util/store.js";
var validateReMobile = (rule, value, callback) => {
  if (value) {
    if (!isMobile(value)) {
      callback(new Error(this.$langt('department.phoneErr')));
    } else {
      callback();
    }
  }
  else {
    callback();
  }
};
export default {
  components: {invitation},
  data () {
    var validatePassReg = (rule, value, callback) => {
      if(value && value.length < 16) {
        callback();
      }else{
        if(!value) {
          callback(new Error(this.$langt(`department.passColumn.password.placeholder`)));
        }else{
          callback(new Error(this.$langt(`department.passColumn.password.rule`)));
        }
      }
    };
    var validateRePassReg = (rule, value, callback) => {
      if(value == this.passForm.password) {
        callback();
      }else{
        if(!value) {
          callback(new Error(this.$langt(`department.passColumn.rePassword.placeholder`)));
        }else{
          callback(new Error(this.$langt(`department.passColumn.rePassword.rule`)));
        }
      }
    };
    // 这里存放数据
    return {
      orders: [],
      data: [],
      defaultProps: {
        children: 'children',
        label: 'name',
        sort: 'sort'
      },
      option: tableOption,
      queryParams: {},
      tableLoading: false,
      treeLoading: false,
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
      },
      tableData: [],
      selectOneId: '',
      selectOneData: null,
      dialogVisible: false,
      title: '',
      method: '',
      //部门表单添加、修改
      Form: {
        name: '',
        parentId: null,
        sort: 0
      },
      rules: {
        name: [{ required: true, message: this.$langt('department.deptColumn.name.placeholder') }],
        parentId: [{ required: false, message: this.$langt('department.deptColumn.parentId.placeholder') }],
        sort: [{ required: true, message: this.$langt('department.deptColumn.sort.placeholder') }]
      },
      treedata: [],
      normalizer (node) {
        return {
          id: node.id,
          label: node.name,
          sort: node.sort,
          children: node.children,
        }
      },
      roleLsit: [],
      rowData: null,
      currentList: [], // 当前同级的部门列表
      lastName: '', // 未修改前的名字
      disSubmit: false, // 是否禁止提交
      formType: 'dept',
      deptIds: [],
      cascaderPptions: [],
      customizeList: [],
      deptOption: {
        emptyBtn: false,
        submitBtnText: this.$langt('common.confirm'),
        labelWidth: '100px',
        submitLoading: false,
        formAlign: 'top',
        column: [
          {
            label: '上级部门',
            prop: 'parentId',
            formSlot: true,
            display: false,
            rules: [{ required: false, message: '部门名称不能为空' }]
          },
          {
            label: '部门名称',
            prop: 'name',
            rules: [{ required: true, message: '部门不能为空' }]
          },
          {
            label: '排序',
            prop: 'sort',
            type: 'inputNumber',
            rules: [{ required: false, message: '排序不能为空' }]
          },
          {
            label: '部门负责人',
            prop: 'leaderId',
            type: 'user',
            multiple: false,
            allowinput: false
          },
          {
            label: '部门代码',
            prop: 'deptCode',
          }
        ]
      },
      pathArr: [], // 部门路径结果
      currentId: '',
      lastDept: null,
      ownName: '', // 改变前的名字
      formOption: {
        cancal: false,
        submitLoading: false,
        disabled: false,
        formAlign: 'top',
        column: JSON.parse(JSON.stringify(formOptionColumn))
      },
      repeatPhone: [], // 重复的手机号
      repeatName: [], // 重复的用户名
      deptListImport: [], // 导入用户-部门列表
      headers: {
        tenantId: this.$store.getters.userInfo.tenantId,
        Authorization: 'Bearer ' + this.$store.getters.access_token,
      },
      importLoading: false,
      validateUserName: (rule, value, callback) => {
        if (value.length > 64) {
          callback(new Error(this.$langt('department.userNameValidate1')));
        } else {
          if(/^(?!_)(?!.*?_$)[a-zA-Z0-9_\u4e00-\u9fa5]+$/.test(value)) {
            callback();
          }else{
            callback(new Error(this.$langt('department.userNameValidate2')));
          }
        }
      },
      uploadFile: null,
      cancels: '',
      fileList: [],
      importVisible: false, // 导入弹框
      importOption: importOptionData,
      userVisible: false, // 用户信息弹框
      passVisible: false, // 修改密码弹框
      passForm: {},
      passOption: {
        cancal: true,
        submitLoading: false,
        emptyBtn: false,
        column: [
          {
            label: '密码',
            prop: 'password',
            showpassword: true,
            rules: [
              { required: true, message: '请输入密码', trigger: 'blur' },
              { validator: validatePassReg, trigger: 'blur'}
            ]
          },
          {
            label: '确认密码',
            prop: 'rePassword',
            showpassword: true,
            rules: [
              { required: true, message: '请输入密码', trigger: 'blur' },
              { validator: validateRePassReg, trigger: 'blur'}
            ]
          }
        ]
      },
      importUserDialogVisible: false,
      currentNode: null,
      sysddLoading: false,
      sysqwLoading: false,
      syncBtns: [], //同步按钮
      syncObj: [], //同步按钮对象
      sysqyLoading: false,
      permissionsList: []
    }
  },
  // 监听属性 类似于data概念
  computed: {},
  // 监控data中的数据变化
  watch: {},
  // 方法集合
  methods: {
    langHandle () {
      this.option.column.filter(col => {
        if(col.label) {
          col.label = this.$langt(`department.userColumn.${col.prop}.label`)
        }
        if(col.rules && col.rules.length > 0) {
          col.rules[0].message = this.$langt(`department.userColumn.${col.prop}.placeholder`)
          if(col.rules.length > 1) {
            col.rules[1].message = this.$langt(`department.userColumn.${col.prop}.rule`)
          }
        }
        if(col.dicData && col.dicData.length > 0) {
          col.dicData.filter(dit => {
            dit.label = this.$langt(`department.userColumn.${col.prop}.dicData.${dit.value}`)
          })
        }
        if(col.activetext) {
          col.activetext = this.$langt(`department.userColumn.${col.prop}.activetext`)
        }
        if(col.inactivetext) {
          col.inactivetext = this.$langt(`department.userColumn.${col.prop}.inactivetext`)
        }
      })
      this.formOption.column.filter(item => {
        item.span=24;
        if(item.prop == 'phone') {
          item.rules.push({ validator: validateReMobile, trigger: 'blur' })
        }
        if(item.label) {
          item.label = this.$langt(`department.userColumn.${item.prop}.label`)
        }
        if(item.rules && item.rules.length > 0) {
          item.rules[0].message = this.$langt(`department.userColumn.${item.prop}.placeholder`)
          if(item.rules.length > 1) {
            item.rules[1].message = this.$langt(`department.userColumn.${item.prop}.rule`)
          }
        }
        if(item.dicData && item.dicData.length > 0) {
          item.dicData.filter(dit => {
            dit.label = this.$langt(`department.userColumn.${item.prop}.dicData.${dit.value}`)
          })
        }
      })
      this.importOption.column.filter(col => {
        if(col.label) {
          col.label = this.$langt(`department.importColumn.${col.prop}.label`)
        }
        if(col.rules && col.rules.length > 0) {
          col.rules[0].message = this.$langt(`department.importColumn.${col.prop}.placeholder`)
          if(col.rules.length > 1) {
            col.rules[1].message = this.$langt(`department.importColumn.${col.prop}.rule`)
          }
        }
        if(col.dicData && col.dicData.length > 0) {
          col.dicData.filter(dit => {
            dit.label = this.$langt(`department.importColumn.${col.prop}.dicData.${dit.value}`)
          })
        }
        if(col.activetext) {
          col.activetext = this.$langt(`department.importColumn.${col.prop}.activetext`)
        }
        if(col.inactivetext) {
          col.inactivetext = this.$langt(`department.importColumn.${col.prop}.inactivetext`)
        }
      })
      this.deptOption.column.filter(col => {
        if(col.label) {
          col.label = this.$langt(`department.deptColumn.${col.prop}.label`)
        }
        if(col.rules && col.rules.length > 0) {
          col.rules[0].message = this.$langt(`department.deptColumn.${col.prop}.placeholder`)
        }
      })
      this.passOption.column.filter(col => {
        if(col.label) {
          col.label = this.$langt(`department.passColumn.${col.prop}.label`)
        }
        if(col.rules && col.rules.length > 0) {
          col.rules[0].message = this.$langt(`department.passColumn.${col.prop}.placeholder`)
        }
      })
    },
    handleSort(row) {
      if (row.order !== null) {
        const obj = {
          column: row.prop,
          order: row.order === 'ascending' ? 'asc' : 'desc'
        }
        const index = this.orders.findIndex(item => {
          return item.column === row.prop
        })
        if (index === -1) {
          this.orders.push(obj)
        } else {
          this.orders[index] = obj
        }
        this.getList()
      }
    },
    // 弹窗显示
    dialogVisibleShow (method, node, data, type) {
      if(data) {
        data.moretool = false
      }
      if (this.$refs.Form) {
        this.$refs.Form.resetFields();
      }
      if((type == 'dept' || type == 'branchOffice') && data && node) {
        this.selectOneId = data.id
        this.selectOneData = data
        this.currentList = node.parent.childNodes
        this.deptOption.column[1].label = type == 'dept' ? this.$langt('department.deptName') : this.$langt('department.offName')
        this.deptOption.column[3].label = type == 'dept' ? this.$langt('department.deptLead') : this.$langt('department.offLead')
      }

      this.method = method
      this.formType = type
      this.dialogVisible = true
      if(type == 'dept' || type == 'branchOffice') {
        if (method == 'add') {
          this.title = type == 'dept' ? this.$langt('department.deptAdd') : this.$langt('department.offAdd')
          this.deptOption.column[1].label = type == 'dept' ? this.$langt('department.deptName') : this.$langt('department.offName')
          this.deptOption.column[3].label = type == 'dept' ? this.$langt('department.deptLead') : this.$langt('department.offLead')
          this.Form = {
            name: '',
            parentId: [],
            sort: 0,
            leaderId: '',
            type: type
          }
          this.currentId = ''
          for(let i in this.treedata) {
            this.treedata[i].path = []
            if(this.treedata[i].children && this.treedata[i].children.length > 0) {
              this.getPath(this.treedata[i].children, this.treedata[i].path)
            }
          }
          // this.deptOption.column.filter(it => {
          //   if(it.prop == 'parentId') {
          //     it.display = true
          //   }
          // })
        }else if(method == 'addchild') {
          this.title = type == 'dept' ? this.$langt('department.deptAddChild') : this.$langt('department.offAdd')
          this.Form = {
            name: '',
            parentId: [],
            sort: 0,
            leaderId: '',
            type: type
          }
          this.currentId = data.id
          if(data.parentId) {
            for(let i in this.treedata) {
              this.treedata[i].path = []
              if(this.treedata[i].children && this.treedata[i].children.length > 0) {
                this.getPath(this.treedata[i].children, this.treedata[i].path)
              }
            }
            this.getDeptPath(data.id, this.treedata)
            this.Form.parentId = this.pathArr
            this.Form.parentId.push(data.id)
          }
          // this.deptOption.column.filter(it => {
          //   if(it.prop == 'parentId') {
          //     it.display = true
          //   }
          // })
        }else {
          this.title = type == 'dept' ? this.$langt('department.deptEdit') : this.$langt('department.offEdit')
          let obj = JSON.parse(JSON.stringify(this.selectOneData))
          this.currentId = this.selectOneData.id
          if(obj.parentId) {
            for(let i in this.treedata) {
              this.treedata[i].path = []
              if(this.treedata[i].children && this.treedata[i].children.length > 0) {
                this.getPath(this.treedata[i].children, this.treedata[i].path)
              }
            }
            this.getDeptPath(obj.id, this.treedata)
            obj.parentId = this.pathArr
          }
          this.Form = obj
          this.lastName = this.selectOneData.name
          if(this.Form.parentId == -1) {
            this.Form.parentId = []
          }
          // this.deptOption.column.filter(it => {
          //   if(it.prop == 'parentId') {
          //     if(data.children && data.children.length > 0) {
          //       it.display = false
          //     }else{
          //       it.display = true
          //     }
          //   }
          // })
        }
      }
      if(type == 'branchOffice') {

      }
    },
    // 弹窗关闭的方法
    handleClose () {
      this.dialogVisible = false
      this.deptIds = []
      this.customizeList = []
    },
    // 页码改变
    currentChange (page) {
      this.getList(page)
    },
    // 显示树改变
    sizeChange (page) {
      this.getList(page)
    },
    // 搜素回调
    searchChange (form) {
      this.queryParams = JSON.parse(JSON.stringify(form))
      this.getList({current: 1})
    },
    // 列表加载
    getList (page) {
      this.tableLoading=true
      const arr = [...this.orders]
      let str = ''
      let temp = ''
      let paramStr = ''
      if (arr.length > 0) {
        arr.forEach((item, key) => {
          str = `&${item.order}=${item.column}`
          temp += str
        })
        paramStr = temp.replace(/&/, '?')
      }
      let tp = {
        current: page && page.current ? page.current : this.page.currentPage,
        size: this.page.pageSize,
        cancelFlag: false
      }
      if(this.selectOneId){
        tp.deptId = this.selectOneId
      }
      getDeptUserList( Object.assign(this.queryParams, tp), paramStr).then(({ data }) => {
        this.tableLoading=false
        if(data.code == 0 && data.data) {
          this.tableData = data.data.records
          this.page.currentPage = data.data.current
          this.page.total = data.data.total
        }
      }).catch(err => {
        this.tableLoading=false
      })
    },
    // 编辑
    async editItem (row, type, nodeData) {
      // console.log(nodeData)
      this.currentNode = nodeData
      this.rowData={}
      if (row) {
        this.rowData = row
        this.userForm=JSON.parse(JSON.stringify(this.rowData))
        this.changebefore(this.userForm.accountName)
        if(type == 'view') {
          this.title = this.$langt('table.info')
          this.formOption.disabled = true
        }else{
          await this.getConst()
          this.title = this.$langt('table.edit')
          this.formOption.disabled = false
        }
      } else {
        await this.getConst()
        this.title = this.$langt('table.add')
        let obj = {
          cancelFlag: false,
          deptId: nodeData ? [nodeData.id] : null,
        }
        this.formOption.column.filter(it => {
          if(it.prop == 'deptId') {
            it.formSlot = true
          }
        })
        this.formOption.disabled = false
        this.userForm= obj
      }
      this.resetDeptTree(this.deptList)
      this.userVisible=true
    },
    userClose () {
      this.userVisible = false
    },
    // 用户信息-提交
    submitHandle (form) {
      if(this.disSubmit) {
        return false
      }
      let obj={}
      obj=JSON.parse(JSON.stringify(form))
      obj.roleName=null
      this.formOption.submitLoading = true
      if (this.title == this.$langt('table.edit')) {
        editUser(obj).then(res => {
          if (res.data.code==0) {
            this.$notify({
              title: this.$langt('common.tip'),
              message: this.$langt('department.userEditSuccess'),
              position: 'bottom-right',
              type: 'success'
            });
            this.formOption.submitLoading = false
            this.getList()
            this.userClose()
          }else{
            this.formOption.submitLoading = false
            if(res.data.msg) {
              this.$notify({
                title: this.$langt('common.tip'),
                message: res.data.msg,
                position: 'bottom-right',
                type: 'error'
              });
            }
          }
        }).catch(e => {
          this.formOption.submitLoading = false
        })
      } else {
        addUser(obj).then(res => {
          if (res.data.code==0) {
            this.$notify({
              title: this.$langt('common.tip'),
              message: this.$langt('department.userAddSuccess'),
              position: 'bottom-right',
              type: 'success'
            });
            this.formOption.submitLoading = false
            this.getList()
            this.userClose()
          }
        }).catch(e => {
          this.formOption.submitLoading = false
        })
      }
    },
    // 结点选中
    handleNodeClick (data, node, dom) {
      if(this.selectOneId == data.id) {
        this.selectOneId = ""
        this.$refs.deptTree.setCurrentKey(null)
        this.$forceUpdate()
      }else{
        this.selectOneId = data.id
      }
      this.selectOneData = data
      this.currentList = node.parent.childNodes
      this.page.currentPage = 1
      this.getList(this.page)
    },
    remove (node, data, type) {
      this.$confirm(this.$langt('department.removeConfirm'), this.$langt('common.tip'), {
        confirmButtonText: this.$langt('common.confirm'),
        cancelButtonText: this.$langt('common.cancel'),
        type: 'warning'
      }).then(() => {
        if(type == '机构') {
          deleteDept(data.id).then(({ data }) => {
            if (data.code === 0) {
              this.$notify({
                title: this.$langt('common.tip'),
                message: this.$langt('department.removeOrgSuccess'),
                position: 'bottom-right',
                type: 'success'
              });
              this.getTreeDept()
            } else {
            }
          })
        }
      }).catch(() => {

      });

    },
    // 递归树清掉空的children
    getTree (tree = [], level) {
      let arr = [];
      if (tree.length !== 0) {
        tree.forEach(item => {
          let obj = {};
          obj.name = item.name;
          obj.id = item.id;
          obj.sort = item.sort
          obj.parentId = item.parentId
          obj.leaderId = item.leaderId || item.extend.leaderId
          obj.deptCode = item.extend.deptCode
          obj.level = level
          obj.extend = item.extend
          if (item.children && item.children.length > 0) {
            obj.children = this.getTree(item.children, level+1);
          }
          arr.push(obj);
        });
      }
      return arr
    },
    // 部门提交
    doSubmit () {
      this.deptOption.submitLoading = true
      if(['add', 'addchild'].indexOf(this.method) > -1) {
        let obj = JSON.parse(JSON.stringify(this.Form))
        if(obj.parentId && obj.parentId.length > 0) {
          obj.parentId = obj.parentId[obj.parentId.length - 1]
        }else{
          obj.parentId = -1
        }
        AddDept(obj).then(({ data }) => {
          if (data.code === 0) {
            this.$notify({
              title: this.$langt('common.tip'),
              message: this.$langt('common.addSuccess'),
              position: 'bottom-right',
              type: 'success'
            });
            this.deptOption.submitLoading = false
            this.getTreeDept()
            this.handleClose()
          }
        }).catch(e => {
          this.deptOption.submitLoading = false
        })
      }else if(this.method == 'edit'){
        let obj = JSON.parse(JSON.stringify(this.Form))
        if(obj.parentId && obj.parentId.length > 0) {
          obj.parentId = obj.parentId[obj.parentId.length - 1]
        }else{
          obj.parentId = -1
        }
        if(!obj.parentId) {
        obj.parentId = -1
        }
        editDept(obj).then(({ data }) => {
          if (data.code === 0) {
            this.$notify({
              title: this.$langt('common.tip'),
              message: this.$langt('common.editSuccess'),
              position: 'bottom-right',
              type: 'success'
            });
            this.deptOption.submitLoading = false
            this.getTreeDept()
            this.handleClose()
          }
        }).catch(e => {
          this.deptOption.submitLoading = false
        })
      }
    },
    getTreeDept () {
      this.treeLoading = true;
      getDeptList().then(res => {
        if(res.data.code == 0) {
          if(res.data.data) {
            this.deptList = JSON.parse(JSON.stringify(res.data.data))
            let tp = JSON.parse(JSON.stringify(res.data.data))
            this.deptListImport = [{id: 'ditto', name: this.$langt('department.above')}].concat(tp)
            this.treedata = this.getTree(res.data.data, 1)
          }
          this.treeLoading = false;
          this.$forceUpdate()
        }
      })
    },
    // 记录改变前的名字
    changebefore (name) {
      this.ownName = name
    },
    // 名称去重
    noRepeatName (name) {
      for(let i in this.currentList) {
        if(name == this.currentList[i].data.name) {
          if(name != this.lastName) {
            this.disSubmit = true
            return false
          }
        }
      }
      this.disSubmit = false
    },
    // 禁用 启用
    disableHandle (row) {
      let tips = this.$langt('department.userDelConfirm')
      if(!row.cancelFlag) {
        tips = this.$langt('department.userDelConfirmFlag')
      }
      this.$confirm(tips, this.$langt('common.tip'), {
        confirmButtonText: this.$langt('common.confirm'),
        cancelButtonText: this.$langt('common.cancel'),
        type: 'warning'
      }).then(() => {
        enableDisUser(row.userId, row.cancelFlag).then(res => {
          if (res.data.code==0) {
            this.$notify({
              title: this.$langt('common.tip'),
              message: this.$langt('department.userDelSuccess'),
              position: 'bottom-right',
              type: 'success'
            });
            this.getList()
          }
        })
      }).catch(_ => {})
    },
    // 获取部门id路径
    getPath (list, temp) {
      for(let i in list) {
        if(!list[i].path) {
          list[i].path = []
        }
        if(list[i].parentId != -1) {
          list[i].path = JSON.parse(JSON.stringify(temp))
          list[i].path.push(list[i].parentId)
        }
        if(list[i].id === this.currentId) {
          list[i].disabled = true
        }else{
          list[i].disabled = false
        }
        if(list[i].children && list[i].children.length > 0) {
          this.getPath(list[i].children, list[i].path)
        }
      }
    },
    // 获取路径结果
    getDeptPath (val, list) {
      for(let i in list) {
        if(list[i].id == val) {
          this.pathArr = list[i].path
        }else{
          if(list[i].children && list[i].children.length > 0) {
            this.getDeptPath(val, list[i].children)
          }
        }
      }
    },
    // 部门更多
    moreDept (item) {
      if(this.lastDept) {
        this.lastDept.moretool = false
      }
      this.lastDept = item
    },

    /**
     * 用户
    */
   // 获取角色列表
    getRoleListHandle () {
      getRoleList("userRole").then(res => {
        if (res.data.code==0) {
          this.option.column.filter(item => {
            if (item.prop=='roleName') {
              item.dicData=res.data.data
              this.roleLsit=res.data.data
              this.$forceUpdate()
            }
          })
        }
      })
    },
    //上传文件，获取文件流
    handleFileChange(file) {
      this.uploadFile = file.raw
    },
    httpHandle (param) {
      let formData = new FormData()
      formData.append('file', this.uploadFile)
      let _this = this
      this.instance.post('/mgr/jvs-auth/usermanager/import/preview', formData).then(responce => {
        if(responce.headers && responce.headers.cancels) {
          _this.cancels = responce.headers.cancels
        }
        let res = responce.data
        this.importView(res, null, [])
      })
    },
    // 导入用户
    importView (res, file, fileList) {
      if(res.code == 0) {
        this.$notify({
          title: this.$langt('common.tip'),
          message: this.$langt('common.importSuccess'),
          position: 'bottom-right',
          type: 'success'
        });
        this.getList()
        this.getTreeDept()
        this.$refs.uploadBtn.clearFiles()
        this.handleCloseImport()
        return false
        let temp = []
        temp = res.data
        if(temp.length > 0) {
          for(let i in temp) {
            if(!temp[i].nickName && temp[i].realName) {
              temp[i].nickName = temp[i].realName
            }
            temp[i].status = false
            // if(temp[i].roleId && temp[i].roleId.length > 0) {
            //   temp[i].roleId = temp[i].roleId.map(Number)
            // }
            // temp[i].deptId && (temp[i].deptId = Number.parseInt(temp[i].deptId))
            // temp[i].jobId && (temp[i].jobId = Number.parseInt(temp[i].jobId))
            for(let i in this.deptList) {
              this.deptList[i].path = []
              if(this.deptList[i].children && this.deptList[i].children.length > 0) {
                this.getPath(this.deptList[i].children, this.deptList[i].path)
              }
            }
            this.getDeptPath(temp[i].deptId, this.deptList)
            let tp = JSON.parse(JSON.stringify(this.pathArr))
            tp.push(temp[i].deptId)
            temp[i].deptId = tp
          }
          this.fileList = temp
          this.getConst()
          this.importVisible = true
        }
      }else{
        this.$refs.uploadBtn.clearFiles()
        this.$notify({
          title: this.$langt('common.tip'),
          message: res.msg,
          position: 'bottom-right',
          type: 'error'
        });
      }
    },
    // 导入失败
    errHandle (err, file, fileList) {
      this.$refs.uploadBtn.clearFiles()
      this.$notify({
        title: this.$langt('common.tip'),
        message: err,
        position: 'bottom-right',
        type: 'error'
      });
    },
    // 导入用户提交
    importHandle () {
      if(this.fileList.length > 0) {
        let temp = JSON.parse(JSON.stringify(this.fileList))
        for(let i in temp) {
          if(temp[i].deptId && temp[i].deptId.length > 0) {
            temp[i].deptId = temp[i].deptId[temp[i].deptId.length-1]
          }
        }
        this.importLoading = true
        importUser(temp).then(res => {
          if(res.data.code == 0) {
            if(res.data.data) {
              let phone = res.data.data.phone
              let accountName = res.data.data.accountName
              this.repeatPhone = phone
              this.repeatName = accountName
              if(phone.length > 0 || accountName.length > 0) {
                this.$notify({
                  title: this.$langt('common.tip'),
                  message: res.data.msg,
                  position: 'bottom-right',
                  type: 'error'
                });
              }else{
                this.$notify({
                  title: this.$langt('common.tip'),
                  message: res.data.msg,
                  position: 'bottom-right',
                  type: 'success'
                });
              }
            }else{
              this.$notify({
                title: this.$langt('common.tip'),
                message: this.$langt('common.importSuccess'),
                position: 'bottom-right',
                type: 'success'
              });
              this.importClose()
              this.getList()
            }
            this.importLoading = false
          }else{
            this.$notify({
              title: this.$langt('common.tip'),
              message: res.data.msg,
              position: 'bottom-right',
              type: 'error'
            });
            this.importLoading = false
          }
        }).catch(e => {
          this.importLoading = false
        })
      }
    },
    // 重复校验
    repeatHandle (type, val) {
      let bool = false
      let list = []
      if(type == 'phone') {
        list = this.repeatPhone
      }
      if(type == 'accountName') {
        list = this.repeatName
      }
      if(list.indexOf(val) > -1) {
        bool = true
      }else{
        // 必填校验
        // console.log(type) // realName
        if(['accountName', 'realName', 'phone'].indexOf(type) > -1) {
          if(val == '') {
            bool = true
          }else{
            bool = false
          }
        }else{
          bool = false
        }
      }
      return bool
    },
    // 下载模板
    downloadModel () {
      this.$openUrl('/mgr/jvs-auth/user/template/excel/download', '_self')
    },
    // 同步用户
    syncUser() {
      console.log(0)
    },
    // 删除导入行
    delImportRow (row, index) {
      this.fileList.splice(index, 1)
      this.$forceUpdate()
    },
    // 关闭导入弹框
    importClose () {
      this.importVisible = false
      this.fileList = []
      this.repeatPhone = []
      this.repeatName = []
    },
    // 取消导入
    cancelHandle () {
      if(this.cancels) {
        sendMyRequire({
          url: '/mgr/jvs-auth/usermanager/cancelImport',
          httpMethod: 'delete',
          requestContentType: 'application/x-www-form-urlencoded',
          headers: {
            cancels: this.cancels
          }
        }
        ).then(res => {
          this.importClose()
        })
      }
    },
    // 获取常量列表
    async getConst () {
      this.getRoleListHandle()
      await getPostList().then(res => {
        if(res.data.code == 0) {
          this.allPost = res.data.data
          this.formOption.column.filter(item => {
            if(item.prop == 'jobId') {
              item.dicData = res.data.data
            }
          })
          this.$forceUpdate()
        }
      })
    },
    // 邀请成员
    invitationHandle () {
      this.$refs.invitation.openDialog()
    },
    queryAllHandle () {
      this.selectOneId = ""
      this.$refs.deptTree.setCurrentKey(null)
      this.$forceUpdate()
      this.queryParams = {}
      this.page.currentPage = 1
      this.getList(this.page)
    },
    // 修改密码
    editPassHandle (row) {
      this.rowData = row
      this.passVisible = true
    },
    // 提交密码
    submitPassWord (form) {
      let temp = encryption({
        data: form,
        key: enCodePasswordKey, // enCodePasswordKey,
        param: ["password"]
      });
      temp = encryption({
        data: temp,
        key: enCodePasswordKey, // enCodePasswordKey,
        param: ["rePassword"]
      });
      this.passOption.submitLoading = true
      setUserPassWord(this.rowData.id, temp).then(res => {
        if(res.data.code == 0) {
          this.$notify({
            title: this.$langt('common.tip'),
            message: this.$langt('department.passEditSuccess'),
            position: 'bottom-right',
            type: 'success'
          });
          this.passOption.submitLoading = false
          this.passClose()
        }
      }).catch(e => {
        this.passOption.submitLoading = false
      })
    },
    // 关闭弹框
    passClose () {
      this.passForm = {}
      this.passVisible = false
    },
    // 上传文件前钩子
    beforeUpload(file) {
      const xlsx = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      const xls = 'application/vnd.ms-excel'
      const isPassType = (file.type !== xlsx && file.type !== xls)
      const isLt10M = file.size / 1024 / 1024 < 10;
      if (isPassType && !isLt10M) {
        this.$notify({
          title: this.$langt('common.tip'),
          message: this.$langt('department.fileLimit'),
          position: 'bottom-right',
          type: 'error'
        });
      } else if (isPassType) {
        this.$notify({
          title: this.$langt('common.tip'),
          message: this.$langt('department.fileFormat'),
          position: 'bottom-right',
          type: 'error'
        });
      } else if (!isLt10M) {
        this.$notify({
          title: this.$langt('common.tip'),
          message: this.$langt('department.fileSize'),
          position: 'bottom-right',
          type: 'error'
        });
      }
      return !isPassType && isLt10M;
    },
    // 导入数据 弹窗关闭
    handleCloseImport() {
      this.importUserDialogVisible = false
      this.fileList = []
    },
    // 同步部门
    sysDept (type, typeval) {
      if(type == 'DINGTALK_INSIDE') {
        this.sysddLoading = true
      }else if(type === 'WECHAT_ENTERPRISE_WEB') {
        this.sysqwLoading = true
      }else {
        this.sysqyLoading = true
      }
      sysDepartment(['OWN', 'STANDARD_OWN'].indexOf(type) > -1 ? typeval : type).then(res => {
        if(res.data && res.data.code == 0) {
          this.getTreeDept()
          this.sysddLoading = false
          this.sysqwLoading = false
          this.sysqyLoading = false
          this.getList()
        }
      }).catch(e => {
        this.sysddLoading = false
        this.sysqwLoading = false
        this.sysqyLoading = false
      })
    },
    // 获取同步按钮
    getSyncBtns() {
      getSyncBtns().then(res => {
        if (res.data && res.data.code == 0) {
          this.syncObj = JSON.parse(JSON.stringify(res.data.data))
          Object.keys(res.data.data).forEach(item => {
            this.syncBtns.push(item)
          })
          // console.log(this.syncObj)
        }
      })
    },
    resetDeptTree (list) {
      for(let i in list) {
        list[i].disabled = false
        if(list[i].children && list[i].children.length > 0) {
          this.resetDeptTree(list[i].children)
        }
      }
    }
  },
  created () {
    this.langHandle()
    this.getConst()
    this.getSyncBtns()
    this.permissionsList = getStore({name: 'permissions'})
  },
  // 生命周期 - 挂载完成（可以访问DOM元素）
  mounted () {
    this.getTreeDept()
  }
}
</script>
<style lang='scss' scoped>
// @import url(); 引入公共css类
.customize-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
  .more-icon{
    display: none;
    .el-icon-more{
      color: #868BA1;
      font-size: 16px;
    }
  }
}
.customize-tree-node:hover{
  .more-icon{
    display: block;
  }
}

.treeBox {
  position: absolute;
  top: 0;
  left: 0;
  width: 300px;
  height: calc(100% - 20px);
  overflow: hidden;
  overflow-y: auto;
  padding-right: 10px;
  padding-bottom: 20px;
  box-sizing: border-box;
  .treeBox-title{
    font-size: 14px;
    padding-left: 24px;
    display: block;
    background: #fff;
    height: 35px;
    line-height: 35px;
    cursor: pointer;
  }
  .treeBox-title:hover{
    background: #EFF2F7;
  }
  .treeBox-title-check{
    background: #F5F7FA;
  }
}
.treeBox::-webkit-scrollbar{
  display: none;
}
.treeBox:hover::-webkit-scrollbar{
  display: block;
}
.formItem {
  width: 100%;
}
.base-type-list{
  padding: 0;
  margin: 0;
  li{
    display: flex;
    align-items: center;
    margin: 0;
    //margin-bottom: 10px;
    height: 32px;
    line-height: 32px;
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
</style>
<style lang="scss">
.table-btn-box{
  width: calc(100vw - 600px);
  .el-button{
    margin-bottom: 8px;
  }
}
.department-list-manage{
  position: relative;
  height: 100%;
  .treeBox{
    .el-tree{
      min-height: calc(100% - 54px);
    }
    .el-tree-node.is-current{
      >.el-tree-node__content{
        background-color: #F5F7FA;
      }
    }
  }
  .treeBox::-webkit-scrollbar{
    display: none;
  }
  .treeBox:hover::-webkit-scrollbar{
    display: block;
  }
  .header-top-open{
    width: calc(100% - 310px);
    margin-left: 310px;
    box-sizing: border-box;
  }
  .el-table{
    width: calc(100% - 310px);
    margin-left: 310px;
  }
  .dept-treeBox{
    .customize-tree-node{
      .customize-tree-node-label{
        display: block;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }
  }
}
.import-user-dialog:not(.user-import-fullscreen-dialog){
  .el-dialog__body{
    .jvs-table{
      .jvs-table-titleTop{
        .el-card__body{
          display: none;
        }
      }
      .table-body-box{
        .el-table{
          .el-table__body-wrapper{
            height: auto!important;
          }
        }
      }
    }
  }
}
.user-import-fullscreen-dialog{
  .el-dialog.is-fullscreen{
    .el-dialog__body{
      width: 100%;
      position: unset;
    }
  }
}
.import-data-box{
  .import-data-upload{
    text-align: center;
    .el-upload-dragger{
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #f7f7f7;
      width: 600px;
    }
  }
  .upload-explain{
    margin-top: 16px;
    font-size: 12px;
    ul{
      padding: 0 16px;
      margin: 8px 0;
      li{
        line-height: 20px;
        span{
          font-weight: bold;
        }
      }
    }
  }
}
</style>
