<template>
  <div class="jvs-sidebar">
    <logo
      ref="menuLogo"
      :systemId="systemId"
      :thisSystem="thisSystem"
      :justAppInfo="justAppInfo"
      @childMenu="childMenu"
      @freshList="freshList"
      @setAppItem="setAppItem"
      @closeOther="closeOther"
    ></logo>
    <el-scrollbar class="el-menu-scrollbar" :style="'height: calc(100% - '+$store.getters.theme.logo.height+');margin-top: '+$store.getters.theme.logo.height+';'">
      <!-- 应用详情 -->
<!--      <div :class="{'app-item-info': true, 'app-item-info-hide':catalogOpen, 'app-item-info-onlyshow': !(appItem && appItem.extend && appItem.extend.designRole)}" v-if="!appSettingOpen && appItem && appItem.extend.designRole" @click="showAppItem">-->
      <div :class="{'app-item-info': true, 'app-item-info-hide':catalogOpen, 'app-item-info-onlyshow': !(appItem && appItem.extend && appItem.extend.designRole)}" v-if="!appSettingOpen && appItem" @click="showAppItem">
        <div class="text-info" style="position: relative;">
          <img style="width: 40px; height: 40px;margin-right: 10px" v-if="appItem.extend" :src="appItem.extend.logo" alt=""/>
          <span style="width: 150px;overflow:hidden;position: absolute;text-overflow: ellipsis;" :title="appItem.name">{{appItem.name}}</span>
<!--          <el-tag v-if="appItem && appItem.extend && !appItem.extend.isDeploy" v-show="modeUserInfo && modeUserInfo.mode == 'GA'" size="mini" type="warning"><span style="font-size: 12px">待发布</span></el-tag>-->
<!--          <el-tag v-else v-show="modeUserInfo && modeUserInfo.mode == 'GA'" size="mini" type="success"><span style="font-size: 12px">已发布</span></el-tag>-->
        </div>
        <span v-if="appItem && appItem.extend && appItem.extend.designRole && modeUserInfo && modeUserInfo.mode == 'DEV'" class="more">
          <el-popover
            placement="right-start"
            size="mini"
            trigger="hover">
            <div class="more-box">
              <div class="add-catalogue" v-if="appItem" @click="addCatalogue(null)">
                <span>添加目录</span>
              </div>
              <div v-for="mit in addMenuTypeList" :key="mit.type" class="add-catalogue" @click="openTemplateList(mit)">
                <span>{{mit.label}}</span>
              </div>
            </div>
            <i slot="reference" class="el-icon-plus" style="color: #6F7588;font-weight: 600;"></i>
          </el-popover>
        </span>
      </div>
      <el-menu
        :default-active="nowTagValue"
        mode="vertical"
        :show-timeout="200"
        :collapse="keyCollapse"
        ref="menuRef"
      >
        <sidebar-item
          :menu="menuChildren"
          :menuType="menuType"
          :screen="screen"
          first
          :props="website.menu.props"
          :collapse="keyCollapse"
          :systemId="systemId"
          :appItem="appItem"
          :jvsDesign="jvsDesign"
          @openCatalogue="openCatalogue"
          @freshAllMenu="freshAllMenu"
          @changeMenu='menuChange'
          @addCatalogue="addCatalogue"
          @topNavChange="topNavChange"
        ></sidebar-item>
      </el-menu>
    </el-scrollbar>
    <!-- 添加目录 -->
    <el-dialog
      class="custom-header-dialog"
      width="50%"
      :title="title"
      :visible.sync="dialogVisible"
      append-to-body
      :before-close="handleClose">
      <div v-if="dialogVisible" class="custom-header-dialog-body-box">
        <jvs-form v-if="dialogType == 'catlog'" :formData="catalogueForm" :option="catalogueOption" @submit="submitHandle" @cancalClick="handleClose"></jvs-form>
        <jvs-form v-if="dialogType == 'menu'" :formData="formData" :option="formOption" @submit="createSubmit" @cancalClick="handleClose">
          <template slot="iconForm">
            <div class="icon-image">
              <img v-if="formData.icon" :src="formData.icon" alt="">
              <el-button v-if="!formData.icon" size="mini" @click="handleFocus">选择图标</el-button>
              <i v-if="formData.icon" class="el-icon-delete" @click="formData.icon = ''"></i>
            </div>
          </template>
        </jvs-form>
      </div>
    </el-dialog>
    <!-- 新建逻辑 -->
    <create-rule-dialog ref="createRuleDialog" :jvsAppId="appItem ? appItem.id : ''"></create-rule-dialog>
    <!-- 新建模型 -->
    <createDataModelDialog
      ref="createDataModelDialog"
      :generateCrudDesign="true"
      :jvsAppId="appItem ? appItem.id : ''"
      @success="$emit('freshAllMenu', true)"
    ></createDataModelDialog>
    <!-- 选图片 -->
    <ImageSelect
      :dialogVisible="iconVisible"
      :title="'选择图标'"
      :defaultLabel="defaultLabel"
      :paramInfo="{bucketName: 'jvs-public', module: 'templatePage', label: '应用目录'}"
      @handleClose="iconClose"
      @handleConfirm="handleConfirm"
    />
    <!-- 收缩菜单 logo -->
    <div :class="{'brevity-button': true, 'brevity': isBrevity}" @click="changeBrevity">
      <svg class="svg-icon" aria-hidden="true">
        <use :xlink:href="`#${!isBrevity ? 'icon-jvs-shouqi' : 'icon-jvs-shousuo'}`"></use>
      </svg>
    </div>
  </div>
</template>

<script>
import { mapGetters, mapState } from "vuex";
import logo from "../logo";
import sidebarItem from "./sidebarItem";
import {addCataType} from '@/components/template/api'
import { getStore } from '@/util/store'
import ImageSelect from "@/components/basic-assembly/ImageSelect";
import {formColumn, pageColumn, flowColumn, chartColumn, customColumn} from '@/components/template/design'
import {addForm} from '@/views/page/api/formlist'
import * as pageApi from '@/views/page/api/newDesign'
import * as flowApi from '@/views/flowable/api/flowable'
import { createChartPage, createScreenPage, createReportPage  } from '@/components/template/api'
import createRuleDialog from '@/components/template/createRuleDialog'
import createDataModelDialog  from '@/components/template/createDataModelDialog'
export default {
  name: "sidebar",
  components: { sidebarItem, logo, top, ImageSelect, createRuleDialog, createDataModelDialog },
  props: {
    isCollapse: {
      type: Boolean,
      default: false
    },
    menuType: {
      type: String,
      default: 'platform'
    },
    systemId: {
      type: String
    },
    thisSystem: {
      type: Object
    },
    freshSide: {
      type: Number
    },
    jvsDesign: {
      type: Object
    },
    changeModeUserRadom: {
      type: Number
    },
    justAppInfo: {
      type: Boolean
    }
  },
  data () {
    return {
      tempMenu: [], // 收藏的菜单
      menuChildren: [], // 点击模块的菜单子集
      showHide: true,
      appItem: null,
      appItemData: null,
      dialogVisible: false,
      catalogueForm: {},
      catalogueOption: {
        emptyBtn: false,
        formAlign: 'top',
        submitBtnText: '确定',
        column: [
          {
            label: '目录名称',
            prop: 'name',
            rules: [{ required: true, message: '目录名称不能为空', trigger: 'blur' }],
          },
          {
            label: '图标',
            prop: 'icon',
            type: 'iconSelect'
          },
        ]
      },
      addMenuTypeList: [
        {label: '表单设计', type: 'form'},
        {label: '列表页设计', type: 'page'},
        {label: '流程配置', type: 'workflow'},
        {label: '自定义页面', type: 'customPage'},
        {label: '创建模型', type: 'dataModel'},
        {label: '创建逻辑', type: 'rule'},
        // {label: '数据统计图', type: 'chart'},
        // {label: '报表设计', type: 'report'},
      ],
      dialogType: 'catlog',
      title: '',
      clickItem: null,
      formData: {},
      formOption: {
        emptyBtn: false,
        submitBtnText: '确定',
        submitLoading: false,
        column: []
      },
      defaultLabel: '',
      iconVisible: false,
      modeUserInfo: null,
      isBrevity: false
    };
  },
  computed: {
    ...mapGetters(["website", "menu", "tag", "keyCollapse", "screen", "appSettingOpen"]),
    nowTagValue: function () {
      let str = this.$router.$jvsRouter.formatMenuPath(this.$route);
      if(this.$route.hash && this.$route.hash.includes('dataModelId')) {
        let tarr = this.$route.hash.split('?')[1].split('&')
        for(let i in tarr) {
          if(tarr[i].includes('id')) {
            str = tarr[i].split('=')[1]
          }
        }
      }
      return str
    },
    ...mapState({
      showCollapse: state => state.common.showCollapse,
    }),
  },
  created () {
    this.modeUserInfo = getStore({ name: 'modeUserInfo' })
  },
  methods: {
    // 更新菜单
    menuChange(val){
      this.menuChildren = val
    },
    validatenull (val) {
      if (typeof val === 'boolean') {
        return false
      }
      if (typeof val === 'number') {
        return false
      }
      if (val instanceof Array) {
        if (val.length == 0) return true
      } else if (val instanceof Object) {
        if (JSON.stringify(val) === '{}') return true
      } else {
        if (val == 'null' || val == null || val == 'undefined' || val == undefined || val == '') return true
        return false
      }
      return false
    },
    // 设置点击的子菜单
    childMenu (obj) {
      this.menuChildren = obj
    },
    // 刷新收藏列表
    freshList (bool) {
      if(bool) {
        this.$store.dispatch("GetMenu").then(data => {
          if (data.length === 0) return;
          this.$router.$jvsRouter.formatRoutes(data, true);
        });
      }
    },
    // 判断是否是应用
    setAppItem (item, type) {
      this.appItem = item
      this.appItemData = JSON.parse(JSON.stringify(item))
      this.catalogOpen = false
      if(type == 'changeModeUser') {
        this.showAppItem()
      }
    },
    // 显示应用详情
    showAppItem () {
      if(this.appItem.extend && this.appItem.extend.designRole) {
        if(this.appItemData) {
          this.$emit('openAppManage', this.appItemData.id)
        }
        this.catalogOpen = false
      }
      this.$forceUpdate()
    },
    // 点击了logo关闭应用相关
    closeOther (bool) {
      this.$emit('closeOther', bool)
    },
    // 打开了应用的目录
    openCatalogue (item) {
      this.catalogOpen = true
      this.$emit('openCatalogue', item)
      this.$forceUpdate()
    },
    // 刷新所有系统菜单
    freshAllMenu (bool) {
      this.$emit('freshAllMenu', bool)
    },
    addCatalogue (parentId) {
      this.dialogType = 'catlog'
      this.title = '添加目录'
      if(parentId) {
        this.$set(this.catalogueForm, 'parentId', parentId)
      }
      this.dialogVisible = true
    },
    submitHandle (form) {
      let obj = {
        appId: this.appItem.id,
        type: this.catalogueForm.name,
        icon: this.catalogueForm.icon
      }
      if(this.catalogueForm.parentId) {
        obj.parentId = this.catalogueForm.parentId
      }
      addCataType(obj).then(res => {
        if(res.data && res.data.code == 0) {
          this.$notify({
            title: '提示',
            message: '添加目录成功',
            position: 'bottom-right',
            type: 'success'
          });
          this.handleClose()
          this.freshAllMenu(true)
        }
      })
    },
    handleClose () {
      this.dialogVisible = false
      this.catalogueForm = {
        icon: null
      }
      this.dialogType = ''
      this.title = ''
      this.clickItem = null
    },
    openTemplateList (item) {
      this.clickItem = item
      this.dialogType = 'menu'
      if(item.type) {
        switch(item.type) {
          case 'form':
            this.formOption.column = formColumn;
            this.title = '创建表单';
            this.createSubmit()
            break;
          case 'page':
            this.formOption.column = pageColumn;
            this.title = '创建列表页';
            this.createSubmit()
            break;
          case 'workflow':
            this.formOption.column = flowColumn;
            this.title = '创建流程';
            this.createSubmit()
            break;
          case 'customPage':
            this.formOption.column = customColumn;
            this.formOption.column.filter(col => {
              if(col.prop == 'url') {
                col.dicUrl = `/mgr/jvs-auth/environment/variable/all/text/${getStore({ name: 'modeUserInfo' }).mode}`
              }
            });
            this.title = '自定义页面';
            this.dialogVisible = true
            break;
          case 'dataModel':
            this.$refs.createDataModelDialog.init();
            break;
          case 'rule':
            this.$refs.createRuleDialog.init();
            break;
          case 'chart':
            this.formOption.column = chartColumn;
            this.title = '创建图表';
            this.createSubmit()
            break;
          case 'screen':
            this.title = '创建数据大屏';
            this.createSubmit()
            break;
          case 'report':
            this.title = '创建报表';
            this.createSubmit()
            break;
          default: ;break;
        }
      }else{
        this.$notify({
          title: '提示',
          message: '此功能未开放，敬请期待！',
          position: 'bottom-right',
          type: 'warning'
        });
      }
    },
    createSubmit () {
      this.formOption.submitLoading = true
      let func = null
      this.formData = Object.assign(this.formData, {
        jvsAppId: this.appItem.id,
        type: this.appItem.id,
        sort: this.appItem.children ? this.appItem.children.length : 0
      });
      switch(this.clickItem.type) {
        case 'form':
          func = addForm;
          break;
        case 'page':
          func = pageApi.createPage;
          break;
        case 'workflow':
          func = flowApi.createModel;
            this.formData = Object.assign(this.formData, {
            jvsAppId: this.appItem.id
          });
          break;
        case 'chart':
          func = createChartPage;
          break;
        case 'customPage':
          func = pageApi.createCustomPage;
          break;
        case 'screen':
          this.formData.name = '未命名大屏'
          func = createScreenPage;
          break;
        case 'report':
          func = createReportPage;
          break;
        default: ;break;
      }
      if(func) {
        func(this.formData).then(res => {
          if(res.data.code == 0 && res.data.data) {
            let str = ''
            switch(this.clickItem.type) {
              case 'form':
                if(res.data.data.id && res.data.data.dataModelId) {
                  str = location.origin + (`/page-design-ui/#/form?jvsAppId=${this.appItem.id}&id=`+res.data.data.id + (res.data.data.dataModelId ? `&dataModelId=${res.data.data.dataModelId}` : ''));
                }
                break;
              case 'page':
                if(res.data.data.id && res.data.data.dataModelId) {
                  str = location.origin + (`/page-design-ui/#/crud/design?jvsAppId=${this.appItem.id}&id=`+res.data.data.id + (res.data.data.dataModelId ? `&dataModelId=${res.data.data.dataModelId}` : ''));
                }
                break;
              case 'workflow':
                str = location.origin + `/flowable-ui/#/processDesign?id=${res.data.data.id}&jvsAppId=${this.appItem.id}`
                break;
              case 'chart':
                if(res.data.data.id) {
                  str = location.origin + ('/chart-design-ui/#/chartDesign?id='+res.data.data.id+`&jvsAppId=${this.appItem.id}`);
                }
                break;
              case 'screen':
                if(res.data.data.id) {
                  str = location.origin + ('/data-screen-ui/#/datascreendesign?id=' + res.data.data.id)
                }
                break;
              case 'report':
                if(res.data.data.id) {
                  str = location.origin + ('/chart-design-ui/#/data-report/datareportdesign?id='+res.data.data.id+`&jvsAppId=${this.appItem.id}`);
                }
                break;
              default: ;break;
            }
            this.$emit('freshAllMenu', true)
            this.handleClose()
            this.formOption.submitLoading = false
            if(str) {
              let _this = this
              setTimeout( () => {
                _this.$openUrl(str, '_blank')
              }, 1000)
            }
          } else {
            this.formOption.submitLoading = false
          }
        }).catch(e => {
          this.formOption.submitLoading = false
        })
      }
    },
    // 选择图标的图片
    handleFocus() {
      this.defaultLabel = '工作流'
      this.iconVisible = true
    },
    iconClose () {
      this.iconVisible = false
      this.defaultLabel = '工作流'
    },
    // 确认选择
    handleConfirm(obj) {
      this.$set(this.formData, 'icon', obj.fileLink)
      this.iconClose()
    },
    changeBrevity(){
      this.isBrevity = !this.isBrevity
      this.$store.commit("SET_COLLAPSE")
      this.$forceUpdate()
    },
    topNavChange (id) {
      this.$emit('topNavChange', id)
    }
  },
  watch: {
    freshSide: {
      handler(newVal, oldVal) {
        if(newVal != -1) {
          this.$refs.menuLogo.getAllMenuList()
          this.$forceUpdate()
        }
      }
    },
    changeModeUserRadom: {
      handler(newVal, oldVal) {
        if(newVal > -1) {
          this.modeUserInfo = getStore({ name: 'modeUserInfo' })
        }
      }
    }
  }
};
</script>
<style lang="scss" scoped>
.divider-line{
  height: 1px;
  background: #303133;
  opacity: 0.2;
  display: none;
}
.el-menu-scrollbar {
  position: relative;
  overflow: unset;
  background: #F5F6F7;
  .app-item-info{
    height: 64px;
    font-size: 15px;
    display: flex;
    align-items: center;
    padding: 8px 12px;
    cursor: pointer;
    display: flex;
    justify-content: space-between;
    box-sizing: border-box;
    background: linear-gradient(179deg, rgba(30,111,255,0.05) 0%, rgba(30,111,255,0) 100%);
    box-shadow: 0 1px 2px 0 rgba(0,0,0,0.15);
  }
  .app-item-info-onlyshow{
    cursor: auto;
    .text-info{
      flex: 1;
    }
  }
}
.add-catalogue{
  padding: 6px 38px;
  cursor: pointer;
  transition: 0.3s;
  &:hover{
    transition: 0.3s;
    background-color: #eff2f7;
  }
}
/deep/ .el-popper{
  .more-box{
    padding: 0 6px;
    cursor: pointer;
  }
}
.custom-header-dialog-body-box{
  height: 100%;
  overflow: hidden;
  /deep/.jvs-form{
    margin-top: 16px;
    height: calc(100% - 16px);
    .el-row{
      display: flex;
      flex-direction: column;
      height: 100%;
      .form-column-tableForm{
        flex: 1;
        overflow: hidden;
        overflow-y: auto;
      }
    }
    .el-form-item:not(.form-btn-bar) {
      margin-left: 20px;
      margin-right: 20px;
    }
    .el-form-item__label{
      font-family: Microsoft YaHei-Bold, Microsoft YaHei;
      font-weight: 700;
      font-size: 14px;
      color: #363B4C;
    }
    .form-item-btn{
      border-top: 1px solid #EEEFF0;
      height: 60px;
      line-height: 60px;
      .form-btn-bar{
        height: 100%;
        margin: 0;
        margin-right: 24px;
        display: flex;
        justify-content: flex-end;
        align-items: center;
        .el-form-item__content{
          line-height: 60px;
          display: flex;
          flex-direction: row-reverse;
          .el-button{
            margin-left: 16px;
          }
        }
      }
    }
  }
}
</style>
<style lang="scss">
.jvs-sidebar{
  height: 100%;
  overflow: hidden;
  .el-menu{
    .menu-wrapper{
      .el-menu-item, .el-submenu__title {
        height: 36px;
        line-height: 36px;
      }
      .el-icon-arrow-down::before{
        content: "\e791"
      }
      .is-opened{
        .el-icon-arrow-down::before{
          content: "\e78f"
        }
      }
      .menu-item-li{
        padding: 0;
        box-sizing: border-box;
        span{
          overflow: hidden;
          white-space: pre;
          text-overflow: ellipsis;
        }
      }
    }
  }
  .el-menu-item, .el-submenu__title {
    font-size: 14px;
    .el-submenu__icon-arrow{
      right: 12px;
    }
  }
  .brevity-button{
    cursor: pointer;
    position: absolute;
    right: 0;
    bottom: 55px;
    width: 32px;
    height: 32px;
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all .5s;
    background-color: #fff;
    .svg-icon{
      width: 13px;
      height: 13px;
    }
  }
}
</style>

