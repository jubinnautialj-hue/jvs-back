<template>
  <div class="trilateral-page">
    <jvs-table
      :page="page"
      pageheadertitle='三方应用登录对接'
      :option="tableOption"
      :data="tableData"
      :loading="tableLoading"
      @on-load="getList"
      @search-change="searchChange"
      @delRow="delRowHandle"
    >
      <template slot="enableDefault" slot-scope="scope">
        <span>{{scope.row.enableDefault ? $langt('common.yes') : $langt('common.no')}}</span>
      </template>
      <template slot="menuLeft">
        <jvs-button size="mini" type="primary" permisionFlag="jvs_other_oauth_config_add" @click="handleAdd">{{$langt('table.add')}}</jvs-button>
      </template>
      <template slot="menu" slot-scope="scope">
        <jvs-button size="mini" type="text" permisionFlag="jvs_other_oauth_config_edit" @click="handleEdit(scope.row)">{{$langt('table.edit')}}</jvs-button>
      </template>
    </jvs-table>
    <el-dialog
      width="57%"
      :title="title"
      :visible.sync="dialogVisible"
      :fullscreen="fullscreen"
      class="drawer-popup-dialog trilateral-dialog"
      :before-close="handleClose">
      <div class="header-div">
        <div class="text">{{title}}</div>
        <div class="tool">
          <i class="el-icon-full-screen" @click="fullscreen=!fullscreen;"></i>
          <i class="el-icon-close" @click="handleClose"></i>
        </div>
      </div>
      <div v-if="dialogVisible" class="trilateral-form">
        <el-form ref="ruleForm" style="" size="mini" :rules="rules" :model="formData" label-position="top" label-width="100px">
          <div class="title">
            <svg class="icon" aria-hidden="true">
              <use xlink:href="#icon-jvs-rongqi"></use>
            </svg>
            <span>{{$langt('appBasicInfo.title.basic')}}</span>
          </div>
          <div class="basic-items">
            <el-form-item :label="$langt('trilateralconfig.column.name.label')" prop="name">
              <el-input v-model="formData.name" size="mini" :placeholder="$langt('trilateralconfig.column.name.placeholder')"></el-input>
            </el-form-item>
            <el-form-item :label="$langt('trilateralconfig.column.enableDefault.label')" prop="enableDefault">
              <el-switch v-model="formData.enableDefault" size="mini"></el-switch>
            </el-form-item>
            <el-form-item :label="$langt('trilateralconfig.column.type.label')" prop="type">
              <el-select
                v-model="formData.type"
                filterable
                allow-create
                :placeholder="$langt('trilateralconfig.column.type.placeholder')"
                style="width: 100%;"
                @change="typeChange">
                <el-option
                  v-for="item in typeList"
                  :key="item.type"
                  :label="item.type"
                  :value="item.type">
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item :label="$langt('trilateralconfig.column.iconUrl.label')" prop="iconUrl">
              <div v-if="formData.iconUrl" class="select-image-show">
                <img :src="formData.iconUrl" alt="">
                <i class="el-icon-delete delete-select-image-tool" @click="delIamgeSelect('iconUrl')"></i>
              </div>
              <jvs-button v-else @click="chooseImage('iconUrl')">{{$langt('trilateralconfig.chooseIcon')}}</jvs-button>
            </el-form-item>
            <el-form-item :label="$langt('trilateralconfig.column.authorize.label')+'(get)'" prop="authorize">
              <el-input v-model="formData.authorize" size="mini" :placeholder="$langt('trilateralconfig.column.authorize.placeholder')"></el-input>
            </el-form-item>
            <el-form-item :label="$langt('trilateralconfig.column.accessToken.label')+'(get)'" prop="accessToken">
              <el-input v-model="formData.accessToken" size="mini" :placeholder="$langt('trilateralconfig.column.accessToken.placeholder')"></el-input>
            </el-form-item>
            <el-form-item :label="$langt('trilateralconfig.column.urlType.label')" prop="urlType">
              <el-select v-model="formData.urlType"  size="mini" :placeholder="$langt('trilateralconfig.column.urlType.placeholder')" clearable style="width: 100%;">
                <el-option label="post" value="post"></el-option>
                <el-option label="get" value="get"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item :label="$langt('trilateralconfig.column.parameterType.label')" prop="parameterType">
              <el-select v-model="formData.parameterType"  size="mini" :placeholder="$langt('trilateralconfig.column.parameterType.placeholder')" clearable style="width: 100%;">
                <el-option v-if="formData.urlType == 'post'" label="form" value="form"></el-option>
                <el-option label="url" value="url"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item :label="$langt('trilateralconfig.column.userInfo.label')+'(get)'" prop="userInfo">
              <el-input v-model="formData.userInfo" size="mini" :placeholder="$langt('trilateralconfig.column.userInfo.placeholder')"></el-input>
            </el-form-item>
            <el-form-item :label="$langt('trilateralconfig.column.logoutUri.label')+'(post)'" prop="logoutUri">
              <el-input v-model="formData.logoutUri" size="mini" :placeholder="$langt('trilateralconfig.column.logoutUri.placeholder')"></el-input>
            </el-form-item>
            <el-form-item :label="$langt('trilateralconfig.column.clientId.label')" prop="clientId">
              <el-input v-model="formData.clientId" size="mini" :placeholder="$langt('trilateralconfig.column.clientId.placeholder')"></el-input>
            </el-form-item>
            <el-form-item :label="$langt('trilateralconfig.column.clientSecret.label')" prop="clientSecret">
              <el-input v-model="formData.clientSecret" size="mini" :placeholder="$langt('trilateralconfig.column.clientSecret.label')"></el-input>
            </el-form-item>
            <el-form-item :label="$langt('trilateralconfig.column.redirectUri.label')" prop="redirectUri">
              <el-input v-model="formData.redirectUri" size="mini" :placeholder="$langt('trilateralconfig.column.redirectUri.placeholder')"></el-input>
            </el-form-item>
            <el-form-item :label="$langt('trilateralconfig.column.scope.label')" prop="scope">
              <el-input v-model="formData.scope" size="mini" :placeholder="$langt('trilateralconfig.column.scope.placeholder')"></el-input>
            </el-form-item>
          </div>
          <div v-if="isCustom(formData.type)">
            <div class="title">
              <svg class="icon" aria-hidden="true">
                <use xlink:href="#icon-jvs-rongqi"></use>
              </svg>
              <span>{{$langt('trilateralconfig.column.filedJson.label')}}</span>
            </div>
            <el-form-item :label="$langt('trilateralconfig.column.pullUserUrlMethod.label')" prop="pullUserUrlMethod">
              <el-input v-model="formData.pullUserUrlMethod" size="mini" :placeholder="$langt('trilateralconfig.column.pullUserUrlMethod.placeholder')"></el-input>
            </el-form-item>
            <el-form-item prop="filedJson">
              <div class="relation-list-box">
                <div class="relation-list" v-for="(item, key) in userMappingList" :key="key" :style="item.span == 24 ? 'width: 100%;' : ''">
                  <el-input style="width: 112px;" disabled show-word-limit :placeholder="item.label"></el-input>
                  <div style="margin: 0 16px;min-width: 28px;height: 36px;text-align: center;">{{$langt('trilateralconfig.equal')}}</div>
                  <el-input style="flex: 1;margin-right: 16px;overflow: hidden;" show-word-limit :placeholder="item.value" v-model="formData.filedJson[item.value]"></el-input>
                  <div class="desc" :style="item.span == 24 ? 'width: auto;min-width: 40px;' : ''">{{item.desc}}</div>
                </div>
              </div>
            </el-form-item>
          </div>
          <div v-if="isCustom(formData.type)">
            <div class="title">
              <svg class="icon" aria-hidden="true">
                <use xlink:href="#icon-jvs-rongqi"></use>
              </svg>
              <span>{{$langt('trilateralconfig.column.extensionJson.label')}}</span>
            </div>
            <el-form-item prop="extensionJson">
              <div style="display: flex;align-items: center;margin-top: -16px;">
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
            </el-form-item>
          </div>
          <div v-if="isCustom(formData.type)">
            <div class="title">
              <svg class="icon" aria-hidden="true">
                <use xlink:href="#icon-jvs-rongqi"></use>
              </svg>
              <span>{{$langt('trilateralconfig.column.deptSyncList.label')}}</span>
            </div>
            <el-form-item :label="$langt('trilateralconfig.column.deptUrl.label')+'(get)'" prop="deptSyncUrl">
              <div class="relation-list">
                <el-input style="width: 50%;" show-word-limit :placeholder="$langt('trilateralconfig.column.deptUrl.placeholder')" v-model="formData.deptUrl"></el-input>
              </div>
            </el-form-item>
            <el-form-item prop="deptSyncList">
              <div class="relation-list-box">
                <div class="relation-list" v-for="(item, key) in deptSyncList" :key="key">
                  <el-input style="width: 112px;" disabled show-word-limit :placeholder="item.label"></el-input>
                  <div style="margin: 0 16px;min-width: 28px;text-align: center;height: 36px;">{{$langt('trilateralconfig.equal')}}</div>
                  <el-input style="flex: 1;margin-right: 16px;overflow: hidden;" show-word-limit :placeholder="item.value" v-model="formData.deptJson[item.value]"></el-input>
                  <div class="desc">{{item.desc}}</div>
                </div>
              </div>
            </el-form-item>
          </div>
        </el-form>
      </div>
      <div v-if="dialogVisible" class="footer-div">
        <el-button type="primary" @click="formSubmit">{{$langt('form.submit')}}</el-button>
        <el-button @click="resetForm">{{$langt('form.reset')}}</el-button>
        <el-button @click="handleClose">{{$langt('form.cancel')}}</el-button>
        <el-button type="success" @click="openTest">{{$langt('trilateralconfig.test')}}</el-button>
      </div>
    </el-dialog>

    <imageSelect ref="logoSelect"
      title="选择图片"
      :dialogVisible="chooseAble"
      :paramInfo="{'module': 'application'}"
      @handleConfirm="handleConfirm"
      @handleClose="chooseAble = false;"
    ></imageSelect>
  </div>
</template>
<script>
import imageSelect from "@/components/basic-assembly/ImageSelect";
import { getOtherTypeList } from '@/api/common'
import {addOtherOauth, deleteOtherOauth, editOtherOauth, getOtherOauth} from './api'
const endLimit = new Date().getFullYear()+'-'+( (new Date().getMonth()+1) > 9 ? (new Date().getMonth()+1) : ('0'+(new Date().getMonth()+1)) )+'-'+( new Date().getDate() > 9 ? new Date().getDate() : ('0'+new Date().getDate()))
export default {
  components: {
    imageSelect
  },
  data() {
    return {
      options: [
        { label: 'GET', value: 'GET' },
        { label: 'POST', value: 'POST' },
      ],
      userMappingList: [
        { label: 'uuid', value: 'uuid', desc: 'String' },
        { label: '用户名称', value: 'username', desc: 'String' },
        { label: '用户昵称', value: 'nickname', desc: 'String' },
        { label: '用户头像', value: 'avatar', desc: 'String' },
        { label: '用户网址', value: 'blog', desc: 'String' },
        { label: '所在公司', value: 'company', desc: 'String' },
        { label: '位置', value: 'location', desc: 'String' },
        { label: '用户邮箱', value: 'email', desc: 'String' },
        { label: '用户备注', value: 'remark', desc: 'String' },
        { label: '手机号', value: 'phone', desc: 'String' },
        { label: '状态', value: 'enabled', desc: 'String' },
        { label: '账号', value: 'account', desc: 'String' },
        { label: '部门id', value: 'deptId', desc: 'String | Array', span: 24 },
        // { label: '用户性别', value: 'gender', desc: 'String' },
      ],
      propMappingList: [
        { label: '响应码', value: 'code', desc: 'String' },
        { label: '数据字段', value: 'data', desc: 'Array' },
      ],
      deptSyncList: [
        { label: '部门ID', value: 'deptId', desc: 'String' },
        { label: '部门名称', value: 'deptName', desc: 'String' },
        { label: '部门编号', value: 'deptCode', desc: 'String' },
        { label: '上级部门', value: 'parentId', desc: 'String' },
        { label: '排序', value: 'sort', desc: 'Integer' },
      ],
      formData: {
        filedJson: {},
        deptJson: {},
        deptUrl: '', // 组织同步接口地址
        urlType: 'post',
        parameterType: 'form'
      },
      rules: {
        name: [
          { required: true, message: '请输入平台名称', trigger: 'blur' },
        ],
        iconUrl: [
          { required: true, message: '请选择平台图标', trigger: 'change' },
        ],
        authorize: [
          { required: true, message: '请输入授权的api', trigger: 'blur' },
        ],
        accessToken: [
          { required: true, message: '请输入获取token的api', trigger: 'blur' },
        ],
        userInfo: [
          { required: true, message: '请输入获取用户信息的api', trigger: 'blur' },
        ],
        clientId: [
          { required: true, message: '请输入clientid', trigger: 'blur' },
        ],
        clientSecret: [
          { required: true, message: '请输入secret', trigger: 'blur' },
        ],
        redirectUri: [
          { required: true, message: '请输入回调地址', trigger: 'blur' },
        ],
        type: [
          { required: true, message: '请选择或输入类型', trigger: 'blur' },
        ],
        loginIndex: [
          { required: true, message: '请输入登陆路由页', trigger: 'blur' },
        ],
      },
      title: '新增',
      isAdd: false,
      dialogVisible: false,
      tableData: [],
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
      },
      queryParams: {}, // 查询条件
      tableLoading: false,
      tableOption: {
        addBtn: false,
        editBtn: false,
        viewBtn: false,
        delBtn: this.$permissionMatch('jvs_other_oauth_config_edit'),
        menu: this.$permissionMatch('jvs_other_oauth_config_edit'),
        page: true,
        search: true,
        showOverflow: true,
        column: [
          {
            label: "平台名称",
            prop: "name",
          },
          {
            label: "默认登录",
            prop: "enableDefault",
            type: 'switch',
            slot: true
          },
          {
            label: "类型",
            prop: "type",
          },
          // {
          //   label: "授权的api",
          //   prop: "authorize",
          // },
          // {
          //   label: "获取token的api",
          //   prop: "accessToken",
          // },
          // {
          //   label: "获取用户信息的api",
          //   prop: "userInfo",
          // },
          // {
          //   label: "退出地址",
          //   prop: "logoutUri",
          // },
          {
            label: "clientid",
            prop: "clientId",
          },
          {
            label: "secret",
            prop: "clientSecret",
          },
          // {
          //   label: "回调地址",
          //   prop: "redirectUri",
          //   // hide: true
          // },
          // {
          //   label: "登陆路由页",
          //   prop: "loginIndex",
          // },
          {
            label: "用户字段映射",
            prop: "filedJson",
            hide: true
          },
          {
            label: "扩展字段",
            prop: "extensionJson",
            hide: true
          },
        ]
      },
      typeList: [],
      chooseAble: false,
      imgProp: '',
      extensionList: [],
      fullscreen: false,
    }
  },
  created () {
    this.tableOption.column.filter(col => {
      if(col.label) {
        col.label = this.$langt(`trilateralconfig.column.${col.prop}.label`)
      }
      if(this.rules[col.prop] && this.rules[col.prop].length > 0) {
        this.rules[col.prop][0].message = this.$langt(`trilateralconfig.column.${col.prop}.placeholder`)
      }
    })
    this.userMappingList.filter(mp => {
      if(mp.label) {
        mp.label = this.$langt(`trilateralconfig.mappingColumn.${mp.value}`)
      }
    })
    this.propMappingList.filter(mp => {
      if(mp.label) {
        mp.label = this.$langt(`trilateralconfig.mappingColumn.${mp.value}`)
      }
    })
    this.deptSyncList.filter(mp => {
      if(mp.label) {
        mp.label = this.$langt(`trilateralconfig.mappingColumn.${mp.value}`)
      }
    })
  },
  methods: {
    radioChange(e) {
      if(e == 'token') {
        let keys = ['authorize', 'accessToken', 'clientId', 'clientSecret', 'redirectUri', 'loginIndex']
        keys.filter(key => {
          this.$set(this.formData, key, '')
        })
      }
      this.$refs.ruleForm.clearValidate()
    },
    handleClose() {
      this.dialogVisible = false
      this.extensionList = []
    },
    async handleAdd() {
      this.isAdd = true
      this.title = this.$langt('table.add') + this.$langt('trilateralconfig.title')
      this.formData = {
        filedJson: {},
        deptJson: {},
        deptUrl: '', // 组织同步接口地址
        extensionJson: {},
        urlType: 'post',
        parameterType: 'form'
      }
      await this.getTypeList()
      this.dialogVisible = true
    },
    async handleEdit(row) {
      this.isAdd = false
      this.$set(this, 'formData', JSON.parse(JSON.stringify(row)))
      this.$set(this.formData, 'filedJson', row.filedJson ? row.filedJson : {})
      this.$set(this.formData, 'deptJson', row.deptJson ? row.deptJson : {})
      this.$set(this.formData, 'extensionJson', row.extensionJson ? row.extensionJson : {})
      let keys = Object.keys(this.formData.extensionJson)
      if(keys.length > 0) {
        this.extensionList = []
        keys.filter(k => {
          this.extensionList.push({
            value: k,
            label: this.formData.extensionJson[k]
          })
        })
      }
      this.title = this.$langt('table.edit') + this.$langt('trilateralconfig.title')
      await this.getTypeList()
      this.dialogVisible = true
    },
    resetForm() {
      this.$refs.ruleForm.resetFields();
    },
    formSubmit() {
      this.$refs.ruleForm.validate((valid) => {
        if (valid) {
          if (this.isAdd) {
            addOtherOauth(this.formData).then(res => {
              if (res.data && res.data.code == 0) {
                this.$notify({
                  title: this.$langt('common.tip'),
                  message: this.$langt('common.addSuccess'),
                  position: 'bottom-right',
                  type: 'success'
                });
                this.getList()
                this.dialogVisible = false
              }
            })
          } else {
            editOtherOauth(this.formData).then(res => {
              if (res.data && res.data.code == 0) {
                this.$notify({
                  title: this.$langt('common.tip'),
                  message: this.$langt('common.editSuccess'),
                  position: 'bottom-right',
                  type: 'success'
                });
                this.getList()
                this.dialogVisible = false
              }
            })
          }
        } else {
          return false
        }
      })
    },
    getList (page) {
      this.tableLoading = true
      let obj = {
        size: this.page.pageSize,
        current: this.page.currentPage
      }
      getOtherOauth(obj).then(res => {
        if(res.data.code == 0) {
          this.tableLoading = false
          this.tableData = res.data.data.records
          this.page.currentPage = res.data.data.current
          this.page.total = res.data.data.total
        }
      })
    },
    searchChange (form) {
      this.queryParams = form
      this.getList()
    },
    delRowHandle(row) {
      deleteOtherOauth(row.id).then(res => {
        if (res.data && res.data.code == 0) {
          this.$notify({
            title: this.$langt('common.tip'),
            message: this.$langt('common.delSuccess'),
            position: 'bottom-right',
            type: 'success'
          });
          this.getList()
        }
      })
    },
    async getTypeList () {
      await getOtherTypeList().then(res => {
        if(res.data && res.data.code == 0) {
          this.typeList = res.data.data
        }
      })
    },
    typeChange (val) {
      if(this.formData.type) {
        this.typeList.filter(fit => {
          if(fit.type == this.formData.type) {
            let keys = ['accessToken', 'authorize','userInfo']
            for(let k in keys) {
              if(fit[keys[k]]) {
                this.$set(this.formData, keys[k], fit[keys[k]])
              }
            }
            if(fit.logo) {
              this.$set(this.formData, 'iconUrl', fit.logo)
            }
          }
        })
      }
    },
    isCustom (type) {
      return true
      let bool = true
      this.typeList.filter(fit => {
        if(fit.type == this.formData.type) {
          bool = false
        }
      })
      return bool
    },
    openTest () {
      this.$openUrl(`/auth/just/oauth2?stats=${this.formData.type}`, '_blank')
    },
    // 选择图片
    chooseImage (prop) {
      this.imgProp = prop
      this.chooseAble = true
      this.$refs.logoSelect.init()
    },
    // 确认图片
    handleConfirm (value) {
      this.chooseAble = false;
      if(value && value.fileLink) {
        this.$set(this.formData, this.imgProp, value.fileLink)
      }
    },
    // 删除已选图片
    delIamgeSelect (prop) {
      this.$set(this.formData, prop, '')
    },
    dealExtensionJson () {
      this.$set(this.formData, 'extensionJson', {})
      for(let i in this.extensionList) {
        if(this.extensionList[i].value) {
          this.$set(this.formData.extensionJson, this.extensionList[i].value, this.extensionList[i].label || '')
        }
      }
    }
  }
}
</script>
<style lang="scss" scoped>
.trilateral-dialog.el-dialog__wrapper{
  /deep/.el-dialog{
    border-radius: 4px;
    .el-dialog__header{
      display: none;
    }
    .el-dialog__body{
      padding: 0!important;
      height: 100%!important;
      overflow-y: hidden;
    }
  }
}
.header-div{
  padding: 0 24px;
  height: 66px;
  border-bottom: 1px solid #EEEFF0;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: space-between;
  .text{
    font-family: Microsoft YaHei-Bold, Microsoft YaHei;
    font-weight: 700;
    font-size: 20px;
    color: #363B4C;
  }
  .tool{
    display: flex;
    align-items: center;
    i{
      font-size: 18px;
      margin-left: 16px;
      font-weight: bold;
      cursor: pointer;
      color: #6F7588;
    }
  }
}
.footer-div{
  border-top: 1px solid #EEEFF0;
  height: 70px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  box-sizing: border-box;
  position: relative;
  background: #fff;
  z-index: 9;
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
.trilateral-form{
  margin-top: 16px;
  height: calc(100% - 152px);
  padding: 0 24px;
  width: 100%;
  box-sizing: border-box;
  overflow: hidden;
  overflow-y: auto;
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
  /deep/.el-form-item{
    margin-bottom: 16px;
    .el-form-item__label{
      height: 18px;
      line-height: 18px;
      margin-bottom: 8px;
      font-family: Microsoft YaHei-Regular, Microsoft YaHei;
      font-weight: 400;
      font-size: 14px;
      color: #363B4C;
      &::before{
        color: #FF194C;
      }
    }
    .el-form-item__content{
      .el-input__inner{
        border: 0;
        background: #F5F6F7;
        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
        font-weight: 400;
        font-size: 14px;
        color: #6F7588;
      }
    }
  }
  .basic-items{
    display: flex;
    flex-wrap: wrap;
    .el-form-item{
      width: calc(50% - 12px);
    }
    .el-form-item:nth-of-type(2n){
      margin-left: 24px;
    }
  }
  .relation-list-box{
    display: flex;
    flex-wrap: wrap;
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
      .el-input.is-disabled{
        /deep/.el-input__inner{
          background: #E4E7EA;
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
.select-image-show{
  position: relative;
  img{
    display: block;
    width: 120px;
    height: 120px;
  }
  .delete-select-image-tool{
    position: absolute;
    top: 3px;
    left: 123px;
    cursor: pointer;
    color: #F56C6C;
  }
}
</style>
