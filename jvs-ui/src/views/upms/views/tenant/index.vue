<template>
  <div class="tenant-manage-box">
    <div :class="{'tenant-manage': true, 'is_zh': is_zh}">
      <platform-page-header title="租户组织" >
        <template slot="desc">
          <div>{{$langt('tenant.desc.line1')}}</div>
          <div>{{$langt('tenant.desc.line2')}}<span style="color: #3471ff;cursor: pointer;">{{$langt('tenant.desc.more')}}</span></div>
        </template>
      </platform-page-header>
      <jvs-table
        v-if="$functionEnable('多租户')"
        pageheadertitle='租户管理'
        :index="false"
        :page="page"
        :option="option"
        :data="tableData"
        :loading="tableLoading"
        @on-load="getList"
        @search-change="searchChange"
      >
        <template slot="menuLeft">
          <jvs-button type="primary" icon="el-icon-plus" size="mini" permisionFlag="jvs_tenant_add" @click="editItem(null)">{{$langt('table.add')}}</jvs-button>
        </template>
        <template slot="menu" slot-scope="scope">
          <jvs-button type="text" size="mini" permisionFlag="jvs_tenant_edit" @click="editItem(scope.row)">{{$langt('table.edit')}}</jvs-button>
          <jvs-button type="text" size="mini" permisionFlag="jvs_tenant_enable" @click="disableHandle(scope.row)" v-if="!scope.row.enable && isCurrentTenant(scope.row) && tenantId !== scope.row.id">{{$langt('table.enable')}}</jvs-button>
          <jvs-button type="text" size="mini" permisionFlag="jvs_tenant_disabled" @click="disableHandle(scope.row)" v-if="scope.row.enable && isCurrentTenant(scope.row) && tenantId !== scope.row.id">{{$langt('table.disabled')}}</jvs-button>
          <jvs-button type="text" size="mini" permisionFlag="jvs_tenant_edit" @click="expandItem(scope.row)">{{$langt('tenant.expand')}}</jvs-button>
          <jvs-button type="text" size="mini" permisionFlag="jvs_tenant_delete" @click="delItem(scope.row)" v-if="!scope.row.enable && isCurrentTenant(scope.row)"><span style="color: #F56C6C;">{{$langt('table.delete')}}</span></jvs-button>
        </template>
        <template slot="roleName" slot-scope="scope">
          {{ scope.row.roleName && scope.row.roleName.join(",") }}
        </template>
        <template slot="loginTypes" slot-scope="scope">
          {{scope.row.loginTypes | formatLoginType}}
        </template>
        <template slot="adminUserImg" slot-scope="scope">
          <img v-if="scope.row.adminUserImg" :src="scope.row.adminUserImg" alt="" style="display: inline-block;width: 40px;height: 40px;border-radius: 50%;overflow: hidden;">
        </template>
      </jvs-table>
      <div class="no-permission-img" v-else>
        <img src="@/const/img/noPermission.jpg" alt=""/>
      </div>
    </div>
    <!-- 租户信息 -->
    <el-dialog
      class="custom-header-dialog"
      :visible.sync="dialogVisible"
      :before-close="handleClose"
      :title="title"
      append-to-body
      :close-on-click-modal="false"
    >
      <tenantForm v-if="dialogVisible && dialogType == 'tenant'" :submitType="title == $langt('table.add') ? 'add' : 'edit'" :rowData="rowData" @close="closeHandle" style="padding-left: 0;"></tenantForm>
      <appInfoForm v-if="dialogVisible && dialogType == 'app'" :rowData="rowData"></appInfoForm>
      <div v-if="dialogVisible && dialogType == 'expand'" class="expand-setting">
        <div style="display: flex;align-items: center;">
          <div style="flex: 1;overflow: hidden;">{{$langt(`trilateralconfig.fieldName`)}}</div>
          <div style="flex: 1;overflow: hidden;">{{$langt(`trilateralconfig.chinese`)}}</div>
          <div style="width: 16px;"></div>
        </div>
        <div class="relation-list-box relation-list-box-row">
          <div class="relation-list" v-for="(item, key) in extensionList" :key="key" style="align-items: center;">
            <el-input show-word-limit :placeholder="$langt(`trilateralconfig.fieldName`)" v-model="item.value" style="margin-right: 10px;" @change="dealExtensionJson"></el-input>
            <el-input v-model="item.label" show-word-limit :placeholder="$langt(`trilateralconfig.chinese`)" @change="dealExtensionJson"></el-input>
            <div class="delete-icon-button" style="margin-left: 10px;" @click="extensionList.splice(key, 1);dealExtensionJson();$forceUpdate();">
              <span class="border-line"></span>
            </div>
          </div>
        </div>
        <div class="bottom-button">
          <div class="button" @click="extensionList.push({});$forceUpdate();">
            <div class="icon">
              <svg aria-hidden="true">
                <use xlink:href="#jvs-ui-icon-xinjian"></use>
              </svg>
            </div>
            <span>{{$langt('trilateralconfig.addRow')}}</span>
          </div>
        </div>
        <div class="footer-div">
          <el-button @click="handleClose">{{$langt('form.cancel')}}</el-button>
          <el-button type="primary" :loading="expandLoading" @click="expandSubmit">{{$langt('form.submit')}}</el-button>
        </div>
        </div>
    </el-dialog>
    <userSelector
      ref="userSelector"
      :userEnable="true"
      :roleEnable="false"
      :currentActiveName="'user'"
      :isRadio="true"
      :dialogTitle="$langt('tenant.userSelect')"
      @submit="addCheckUSer"
    />
  </div>
</template>
<script>
import { tableOption } from './option'
import store from "@/store";
import userSelector from '@/components/basic-assembly/userSelector'
import { encryption } from "@/util/util";
import {setTenantManager} from './api'
import { editTenant } from '../../api/tenant'
import {
  getTenantList,
  deleteTenant,
  enableTenant,
} from '../../api/tenant.js'
import tenantForm from './tenantForm'
import appInfoForm from '../appBascSetting/appinfo'
import PlatformPageHeader from "@/components/page-header/PlatformPageHeader";
export default {
  name: 'tenant-manage',
  components: {PlatformPageHeader, tenantForm, userSelector, appInfoForm },
  data () {
    return {
      queryParams: {},
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
      },
      option: tableOption,
      tableData: [],
      title: this.$langt('table.add'),
      dialogVisible: false,
      rowData: {},
      defaultProps: {
        children: 'childList',
        label: 'name'
      },
      userForm: {}, // 用户表单
      roleLsit: [], // 角色列表
      tableLoading: false,
      dialogType: '',
      tenantId: store.state.common.tenantId,
      is_zh: false,
      extensionList: [],
      expandLoading: false,
    }
  },
  created () {
    let language = navigator.language || navigator.userLanguage;
    if(/zh/i.test(language) || /zh-CN/i.test(language) || /zh-TW/i.test(language) || /zh-HK/i.test(language)) {
      this.is_zh = true
    }
    this.option.column.filter(col => {
      if(col.label) {
        col.label = this.$langt(`tenant.column.${col.prop}.label`)
      }
      if(col.rules && col.rules.length > 0) {
        col.rules[0].message = this.$langt(`tenant.column.${col.prop}.placeholder`)
      }
      if(col.tips && col.tips.text) {
        col.tips.text = this.$langt(`tenant.column.${col.prop}.${col.prop == 'adminUserAccount' ? 'placeholder' : 'tips'}`)
      }
      if(col.dicData && col.dicData.length > 0) {
        col.dicData.filter(dit => {
          dit.label = this.$langt(`tenant.column.${col.prop}.dicData[${dit.value}]`)
        })
      }
    })
  },
  filters: {
    formatLoginType (list) {
      let temp = []
      temp.push(this.$langt(`tenant.column.loginTypes.dicData[${list[i]}]`))
      return temp.join(',')
    }
  },
  methods: {
    // 获取数据
    getList (page) {
      let obj={
        size: this.page.pageSize,
        current: this.page.currentPage
      }
      this.tableLoading = true
      getTenantList(Object.assign(obj, this.queryParams)).then(res => {
        if (res.data.code==0) {
          this.tableData=res.data.data.records
          this.page.currentPage=res.data.data.current
          this.page.total=res.data.data.total
          this.tableLoading = false
        }else{
          this.tableLoading = false
        }
      }).catch(e => {
        this.tableLoading = false
      })
    },
    /**
     * 搜索回调
     */
    searchChange (form) {
      this.queryParams=form
      this.getList()
    },
    // 删除
    delItem (row) {
      this.$confirm(this.$langt('tenant.delTenant'), this.$langt('common.tip'), {
        confirmButtonText: this.$langt('common.confirm'),
        cancelButtonText: this.$langt('common.cancel'),
        type: 'warning'
      }).then(() => {
        deleteTenant(row.id).then(res => {
        if (res.data.code==0) {
          this.$notify({
            title: this.$langt('common.tip'),
            message: this.$langt('common.delSuccess'),
            position: 'bottom-right',
            type: 'success'
          });
          this.getList()
        }
      })
      }).catch(e => {})
    },
    // 编辑
    editItem (row) {
      this.rowData={}
      if (row) {
        this.rowData = JSON.parse(JSON.stringify(row))
        this.rowData.password = '******'
        this.title = this.$langt('table.edit')
      } else {
        this.title = this.$langt('table.add')
        this.userForm = {
          loginTypes: []
        }
        this.rowData.defaultPassword = '123456'
      }
      this.dialogType = 'tenant'
      this.dialogVisible=true
    },
    // 关闭弹框
    handleClose () {
      this.dialogType = ''
      this.rowData = {}
      this.dialogVisible=false
      this.iconFileList = []
      this.bgFileList = []
      this.logoFileList = []
      this.extensionList = []
    },
    // 通知关闭
    closeHandle (bool) {
      if(bool) {
        this.handleClose()
        this.getList()
      }
    },
    disableHandle (row) {
      let str = this.$langt('tenant.enableStr')
      if(row.enable) {
        str = this.$langt('tenant.disableStr')
      }
      this.$confirm(str).then(_ => {
        enableTenant(row.id, !row.enable).then(res => {
          if(res.data.code == 0) {
            this.$notify({
              title: this.$langt('common.tip'),
              message: str + this.$langt('common.success'),
              position: 'bottom-right',
              type: 'success'
            });
            this.getList()
          }
        })
      }).catch(_ => {})
    },
    isCurrentTenant (row) {
      let id = row.id
      let tid = this.$store.getters.userInfo.tenantId
      let bool = true
      if(id === tid) {
        bool = false
      }
      bool = true // 当前租户可编辑自己
      return bool
    },
    openUser (row) {
      this.rowData = JSON.parse(JSON.stringify(row))
      this.$refs.userSelector.openDialog()
    },
    addCheckUSer (list) {
      if(list && list.length > 0) {
        setTenantManager(this.rowData.id, list[0].id).then(res => {
          if(res.data && res.data.code == 0) {
            this.$notify({
              title: this.$langt('common.tip'),
              message: this.$langt('tenant.setsuccess'),
              position: 'bottom-right',
              type: 'success'
            });
            this.getList()
            this.rowData = {}
          }
        })
      }
    },
    setAppItem (row) {
      this.rowData = JSON.parse(JSON.stringify(row))
      this.title = this.$langt('tenant.setapp')
      this.dialogType = 'app'
      this.dialogVisible = true
    },
    expandItem (row) {
      this.rowData = {}
      if(row) {
        this.rowData = JSON.parse(JSON.stringify(row))
        this.$set(this.rowData, 'extensionJson', row.extensionJson ? row.extensionJson : {})
        let keys = Object.keys(this.rowData.extensionJson)
        if(keys.length > 0) {
          this.extensionList = []
          keys.filter(k => {
            this.extensionList.push({
              value: k,
              label: this.rowData.extensionJson[k]
            })
          })
        }
        this.rowData.password = '******'
        this.title = this.$langt('tenant.expand')
        this.dialogType = 'expand'
        this.dialogVisible = true
      }
    },
    dealExtensionJson () {
      this.$set(this.rowData, 'extensionJson', {})
      for(let i in this.extensionList) {
        if(this.extensionList[i].value) {
          this.$set(this.rowData.extensionJson, this.extensionList[i].value, this.extensionList[i].label || '')
        }
      }
    },
    expandSubmit () {
      let obj = JSON.parse(JSON.stringify(this.rowData))
      obj.roleName = null
      if(obj.password == '******') {
        obj.password = ''
      }else{
        obj = encryption({
          data: obj,
          key: enCodeKey,
          param: ["password"]
        });
      }
      if(obj.appId) {
        obj = encryption({
          data: obj,
          key: enCodeKey,
          param: ["appId"]
        });
      }
      if(obj.secret) {
        obj = encryption({
          data: obj,
          key: enCodeKey,
          param: ["secret"]
        });
      }
      if(!obj.admin) {
        obj.admin = obj.phone
      }
      if(!obj.loginTypes || obj.loginTypes.length == 0) {
        obj.loginTypes = ['password', 'phone']
      }
      this.expandLoading = true
      editTenant(obj).then(res => {
        if (res.data.code==0) {
          if(this.submitType == 'edit') {
            this.$notify({
              title: this.$langt('common.tip'),
              message: this.$langt('common.saveSuccess'),
              position: 'bottom-right',
              type: 'success'
            });
          }else{
            this.$notify({
              title: this.$langt('common.tip'),
              message: this.$langt('tenant.editTenantSuccess'),
              position: 'bottom-right',
              type: 'success'
            });
          }
          this.expandLoading = false
          this.handleClose()
          this.getList()
        }
      }).catch(e => {
        this.expandLoading = false
      })
    }
  }
}
</script>
<style lang="scss" scoped>
.tenant-manage-box {
  height: 100%;
  background-color: #ffffff;
  /deep/.platform-page-header{
    padding: 0;
    padding-bottom: 15px;
    .title{
      display: none;
    }
  }
  .tenant-manage.is_zh{
    /deep/.el-alert--info.is-light{
      width: 1240px;
    }
    /deep/.table-body-box{
      .el-table{
        .el-table__body-wrapper{
          height: calc(100vh - 271px)!important;
        }
      }
    }
  }
  .tenant-manage:not(.is_zh){
    /deep/.table-body-box{
      .el-table{
        .el-table__body-wrapper{
          height: calc(100vh - 289px)!important;
        }
      }
    }
  }
}
.bottom-button{
  margin-top: 8px;
  .button{
    width: 90px;
    display: flex;
    align-items: center;
    cursor: pointer;
    .icon{
      width: 16px;
      height: 16px;
      background: #1E6FFF;
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 4px;
      svg{
        width: 12px;
        height: 12px;
        fill: #fff;
      }
    }
    span{
      font-family: Microsoft YaHei-Regular, Microsoft YaHei;
      font-weight: 400;
      font-size: 14px;
      color: #1E6FFF;
      line-height: 18px;
    }
  } 
}
.delete-icon-button{
  width: 16px;
  height: 16px!important;
  background: #36B452;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  cursor: pointer;
  .border-line{
    width: 10px;
    height: 2px;
    background: #fff;
    border-radius: 2px;
  }
}
.expand-setting{
  width: 50vw;
  height: 70vh;
  padding-left: 24px;
  padding-top: 24px;
  position: relative;
  box-sizing: border-box;
  .title{
    margin-bottom: 16px;
    display: flex;
    align-items: center;
    .icon{
      display: block;
      width: 16px;
      height: 16px;
      margin-right: 10px;
    }
    span{
      font-family: Microsoft YaHei-Bold, Microsoft YaHei;
      font-weight: 700;
      font-size: 16px;
      color: #363B4C;
    }
  }
  .relation-list-box{
    display: flex;
    flex-wrap: wrap;
    max-height: calc(100% - 100px);
    padding-top: 16px;
    padding-right: 24px;
    box-sizing: border-box;
    overflow: hidden;
    overflow-y: auto;
    .relation-list{
      display: flex;
      width: calc(50% - 12px);
      margin-top: 16px;
      &:nth-of-type(2n){
        margin-left: 24px;
      }
      &:nth-of-type(1), &:nth-of-type(2){
        margin-top: 0;
      }
      .el-input{
        /deep/.el-input__inner{
          border: 0;
          background: #f5f6f7;
          font-family: Microsoft YaHei-Regular, Microsoft YaHei;
          font-weight: 400;
          font-size: 14px;
          color: #6f7588;
          height: 36px;
          line-height: 36px;
        }
      }
      .desc{
        width: 40px;
        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
        font-weight: 400;
        font-size: 14px;
        color: #6F7588;
        word-break: keep-all;
      }
    }
    &.relation-list-box-row{
      .relation-list{
        width: 100%;
        &:nth-of-type(2n){
          margin-left: 0;
        }
        &:nth-of-type(2){
          margin-top: 16px;
        }
        .el-input{
          width: calc(50% - 18px);
        }
      }
    }
  }
  .footer-div{
    border-top: 1px solid #EEEFF0;
    height: 50px;
    display: flex;
    align-items: center;
    padding-right: 24px;
    box-sizing: border-box;
    position: absolute;
    bottom: 0;
    left: 0;
    background: #fff;
    z-index: 9;
    width: 100%;
    display: flex;
    justify-content: flex-end;
    .el-button{
      height: 32px;
      padding: 0 20px;
      span{
        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
        font-weight: 400;
        font-size: 14px;
        color: #FFFFFF;
      }
      &.el-button--primary{
        background: #1E6FFF;
      }
      &.el-button--success{
        background: #36B452;
      }
    }
  }
}

</style>
<style lang="scss">
.no-header-dialog-tenant{
  .el-dialog__header{
    padding: 0;
  }
  .el-dialog.is-fullscreen{
    .el-dialog__body{
      left: 0;
      width: 100%;
      padding: 0;
      .el-card__body{
        background: #f0f2f5;
        padding: 0!important;
        .pageheader-top{
          padding: 10px;
          background: #fff;
          border-radius: 5px;
        }
      }
    }
  }
}
</style>
