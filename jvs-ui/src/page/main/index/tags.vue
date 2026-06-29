<template>
  <div class="jvs-tags" v-if="showTag" :style="'height: ' + $store.getters.theme.logo.height + ';'">
    <div
      class="jvs-tags__box"
      :class="{'jvs-tags__box--close':!website.isFirstPage}"
      :style="'height:'+ $store.getters.theme.logo.height"
    >
      <div class="lineBox">
        <div class="top-nav" :style="isProcessIndexRoute ? 'opacity: 0;visibility: hidden' : 'padding-left:calc( '+(keyCollapse ? '64px' : $store.getters.theme.logo.width)+' );width:calc(100% - '+ (keyCollapse ? '64px' : $store.getters.theme.logo.width) +');'">
          <ul class="system-list">
            <el-popover
              v-if="jvsDesign && jvsDesign.JVS_DESIGN_MGR && sysList && sysList.length > 0"
              v-model="showAppListBool"
              placement="bottom-start"
              width="500"
              trigger="click">
              <div class="app-list-box">
                <div class="app-list-item" v-for="app in sysList" :key="app.id+'-item'" @click="enterAppItem(app)">
                  <img :src="app.extend.logo" />
                  <div>
                    <div v-if="app.extend && app.extend.recommend" class="has-design-tag">
                      <span>{{app.name}}</span>
                      <el-tag size="mini" type="warning"><span style="font-size: 12px">推荐</span></el-tag>
                    </div>
                    <div v-else-if="app.extend && app.extend.designRole && !app.extend.recommend" class="has-design-tag">
                      <span>{{app.name}}</span>
                      <el-tag size="mini" type="success"><span style="font-size: 12px">设计</span></el-tag>
                    </div>
                    <span v-else>{{app.name}}</span>
                    <span>{{app.extend.description}}</span>
                  </div>
                </div>
              </div>
              <li slot="reference" :class="{'more-nav': true, 'active-li': showAppListBool}" style="height:100%;">
                <!-- <svg class="normal-svg-icon" aria-hidden="true" style="width: 16px;height: 16px;">
                  <use xlink:href="#icon-jvs-yingyongshezhi-weixuanzhong"></use>
                </svg>
                <svg class="active-svg-icon" aria-hidden="true" style="width: 16px;height: 16px;">
                  <use xlink:href="#icon-jvs-yingyongshezhi-xuanzhong"></use>
                </svg> -->
                <span>{{$langt('topNav.myApp')}}</span>
              </li>
            </el-popover>
            <li v-if="jvsDesign && jvsDesign.JVS_DESIGN_MGR && $permissionMatch('jvs_app')" :class="{'more-nav': true,'active-li': (templateOpen && !showAppListBool)}" @click="openTemplate">
              <span>{{$langt('topNav.appCenter')}}</span>
            </li>
            <!-- <li v-if="jvsDesign && jvsDesign.JVS_DESIGN_MGR" :class="{'more-nav': true, 'active-li': (leftActive == 'work' && !showAppListBool)}" @click="openWorkplace">
              <span>工作台</span>
            </li> -->
            <li v-if="jvsDesign && jvsDesign.JVS_DESIGN_MGR && $permissionMatch('jvs_app')" class="more-nav" @click="changeModeAndUser">
              <span>{{$langt(`topNav.modeUser.${modeUser.mode}`)}}<span v-if="modeUser && modeUser.userId && modeUser.userName">({{modeUser.userName}})</span></span>
            </li>
          </ul>
        </div>
        <!-- 右上角工具栏 -->
        <div class="rightTool">
          <!-- 搜索 -->
          <div class="search-input-box" @click="openDrawer('search')">
            <svg class="icon" aria-hidden="true">
              <use xlink:href="#icon-jvs-sousuo"></use>
            </svg>
            <div class="h-line"></div>
            <span class="text">{{$langt('common.search')}}</span>
          </div>
          <div class="split-line"></div>
          <!-- 内部跳转 -->
          <el-popover
            v-if="linkList && linkList.length > 0"
            placement="bottom"
            width="360"
            trigger="hover">
            <div class="link-list-box">
              <div class="link-list-item" v-for="(lit, lix) in linkList" :key="lit.name+'-item-'+lix" @click="enterLink(lit)">
                <div>
                  <img :src="lit.iconUrl" />
                  <span>{{lit.name}}</span>
                </div>
              </div>
            </div>
            <div slot="reference" class="setting-icon">
              <svg class="icon" aria-hidden="true">
                <use xlink:href="#icon-jvs-xitongtiaozhuan"></use>
              </svg>
              <svg class="icon hover-show" aria-hidden="true">
                <use xlink:href="#icon-jvs-xitongtiaozhuan-hover"></use>
              </svg>
            </div>
          </el-popover>
          <!-- 平台管理 -->
          <div v-if="false && $permissionMatch('jvs_base')" @click="settingApp('system')">
            <el-popover placement="bottom" trigger="hover" :content="$langt('topNav.system')" popper-class="custom-right-tool-poper">
              <div slot="reference" class="setting-icon">
                <svg class="icon" aria-hidden="true">
                  <use xlink:href="#icon-jvs-xitongshezhi"></use>
                </svg>
                <svg class="icon hover-show" aria-hidden="true">
                  <use xlink:href="#icon-jvs-xitongshezhi-hover"></use>
                </svg>
              </div>
            </el-popover>
          </div>
          <!-- 运维设置 -->
          <div v-if="false && $permissionMatch('jvs_platform')" @click="settingApp('platform')">
            <el-popover placement="bottom" trigger="hover" :content="$langt('topNav.platform')" popper-class="custom-right-tool-poper">
              <div slot="reference" class="setting-icon">
                <svg class="icon" aria-hidden="true">
                  <use xlink:href="#icon-jvs-zhanghuguanli-weixuanzhong"></use>
                </svg>
                <svg class="icon hover-show" aria-hidden="true">
                  <use xlink:href="#icon-jvs-zhanghuguanli-xuanzhong"></use>
                </svg>
              </div>
            </el-popover>
          </div>
          <!-- 消息通知 -->
          <el-popover placement="bottom" trigger="hover" :content="$langt('common.message')" popper-class="custom-right-tool-poper">
            <div slot="reference" class="setting-icon" @click="openDrawer('message')">
              <svg v-if="remainingCount > 0" class="icon" aria-hidden="true">
                <use xlink:href="#icon-jvs-tongzhi-youxiaoxi"></use>
              </svg>
              <svg v-else class="icon" aria-hidden="true">
                <use xlink:href="#icon-jvs-tongzhi-wuxiaoxi"></use>
              </svg>
              <svg class="icon hover-show" aria-hidden="true">
                <use xlink:href="#icon-jvs-tongzhi-wuxiaoxi-hover"></use>
              </svg>
            </div>
          </el-popover>
          <!-- 头像 -->
          <div class="user-header">
            <img :src="$store.getters.userInfo.headImg || '/jvs-ui-public/img/headImg.png'" alt="" @click="openDrawer('usercenter')">
          </div>
        </div>
      </div>
    </div>

    <tenantDialog ref="tenantDialog" :usertenantList="usertenantList" :needReload="true" @reload="reload"></tenantDialog>

    <!-- 菜单loading -->
    <div class="menu-loading-back" v-if="menuLoading"></div>
    <invite-user :dialogVisible="inviteUser" @handleClose="handleOrgClose"/>
    <add-user :dialogVisible="addUser" @handleClose="handleCreateOrgClose"/>

    <!-- 切换模式和模拟人员 -->
    <el-dialog
      class="custom-header-dialog"
      :title="$langt(`topNav.modeUser.title`)"
      :visible.sync="modeUserVisible"
      :before-close="modeUserClose">
      <div v-if="modeUserVisible" class="change-model-user-box">
        <div class="model-list">
          <div v-for="mod in modeUserOption.column[0].dicData" :key="mod.value" :class="{'model-list-item': true, 'active': mod.value == modeUserForm.mode}" @click="$set(modeUserForm, 'mode', mod.value);$set(modeUserForm, 'userId', '');$set(modeUserForm, 'userName', '');">
            <img :src="mod.img" alt="">
            <h4>{{mod.label}}</h4>
            <span>{{mod.tip}}</span>
          </div>
        </div>
        <jvs-form :option="modeUserOption" :formData="modeUserForm" @formChange="modeUserFormChange" @submit="modeUserSubmit" @cancalClick="modeUserClose"></jvs-form>
        <div class="footer-btn">
          <div class="btn cancel" @click="modeUserClose">{{$langt(`form.cancel`)}}</div>
          <div class="btn submit" @click="modeUserSubmit">{{$langt(`form.submit`)}}</div>
        </div>
      </div>
    </el-dialog>

    <!-- 搜索 /  消息  /  个人中心  抽屉显示-->
    <el-drawer
      custom-class="right-tool-custom-drawer"
      :modal="false"
      :visible.sync="drawerVisible"
      append-to-body
      :with-header="false"
      :before-close="drawerClose">
      <div v-if="drawerVisible" :class="`drawer-box ${drawerType}`">
        <div class="drawer-top">
          <div class="drawer-top-title">
            <svg v-if="['allMessage', 'accountInfo'].indexOf(drawerType) > -1" class="back" aria-hidden="true" @click="backHandle">
              <use xlink:href="#icon-jvs-fanhuishangyiji"></use>
            </svg>
            <span>{{$langt(`common.rightDrawer.${drawerType}`)}}</span>
          </div>
          <svg class="close-icon" aria-hidden="true" @click="drawerClose">
            <use xlink:href="#icon-jvs-danchuangguanbi1"></use>
          </svg>
        </div>
        <div class="drawer-body">
          <!-- 搜索 -->
          <div v-if="drawerType == 'search'" style="width: 100%;height: 100%;">
            <div class="search-input-box">
              <el-input v-model="searchMenuName" :placeholder="$langt(`common.rightDrawer.search`)" @input="searchNameMenuHandle">
                <svg slot="prefix" aria-hidden="true" style="width: 16px;height: 16px;" @click="searchNameMenuHandle">
                  <use xlink:href="#icon-jvs-sousuo"></use>
                </svg>
                <svg v-if="searchMenuName" slot="append" aria-hidden="true" style="width: 16px;height: 16px;" @click="closeSearchMenuName">
                  <use xlink:href="#icon-jvs-danchuangguanbi"></use>
                </svg>
              </el-input>
            </div>
            <div class="search-title">
              <div class="result">{{$langt(`common.rightDrawer.searchResult`)}}</div>
            </div>
            <div v-if="searchNameMenuList && searchNameMenuList.length > 0" class="search-list">
              <div v-for="item in searchNameMenuList" :key="'search-menu-item'+item.id" :title="item.name" class="search-list-item" @click="searchMenuNameClick(item)">
                <span class="menu-name">{{item.name}}</span>
              </div>
            </div>
            <div v-else class="search-list">
              <div class="empty-box">
                <img class="empty-img" src="/jvs-ui-public/img/searchEmpty.png" alt="">
                <div class="empty-text">{{$langt(`common.rightDrawer.emptyCon`)}}</div>
              </div>
            </div>
          </div>
          <!-- 消息通知  历史消息 -->
          <div v-if="['message', 'allMessage'].indexOf(drawerType) > -1" style="width: 100%;height: 100%;">
            <div class="message-header-box">
              <div class="left">
                <el-badge is-dot class="item">
                  <div class="notice">{{$langt(`common.rightDrawer.notify`)}}</div>
                </el-badge>
              </div>
              <div class="right">
                <el-button type="text" @click="editreadStatus">{{$langt(`common.rightDrawer.allRead`)}}</el-button>
                <div class="line"></div>
                <el-popover placement="bottom" trigger="hover" :content="$langt(`common.rightDrawer.allMessage`)" popper-class="custom-right-tool-poper">
                  <svg slot="reference" style="width: 16px;height: 16px;" @click="openDrawer('allMessage')">
                    <use xlink:href="#icon-jvs-lishijilu"></use>
                  </svg>
                </el-popover>
              </div>
            </div>
            <div v-loading="messageLoading" class="message-body">
              <div v-for="(item,index) in insideList" :key="index" v-show="!messageLoading" class="message-item">
                <div class="create-time">
                  <span>{{item.createTime | formatLogTime}}</span>
                  <span class="delete-text" @click.stop="deleteMessageHandle(item)">删除</span>
                </div>
                <div class="message-item-body">
                  <div>
                    <div class="body-icon-box">
                      <svg>
                        <use :xlink:href="(item.readIs || readStatus) ? '#icon-jvs-tongzhi-yidu' : '#icon-jvs-tongzhi'"></use>
                      </svg>
                    </div>
                    <div v-if="item.msgContent && item.msgContent.oprateType == 'OPEN_FLOW_TASK_TODO' && item.msgContent.oprateParam" class="body-icon-box">
                      <svg style="cursor: pointer;" @click="dealHandle(item)">
                        <use xlink:href="#icon-icon_4-04"></use>
                      </svg>
                    </div>
                  </div>
                  <div class="message-item-content">
                    <div class="message-title" @click.stop="openMessMore(item)">
                      <span class="el-tooltip__trigger">{{item.msgContent.title}}</span>
                    </div>
                    <div v-if="item.open && item.msgContent.content.length > 23" class="cell-message" @click.stop="$set(item, 'open', false);$forceUpdate();">
                      <span>{{$langt(`common.rightDrawer.close`)}}</span>
                      <svg>
                        <use xlink:href="#icon-jvs-danchuang-zhankai"></use>
                      </svg>
                    </div>
                    <div :class="{'message-content': true, 'cell': !(item.open)}">
                      <div v-if="item.open" v-html="item.msgContent.content" class="content-body"></div>
                      <span v-else class="content-body">{{item.msgContent.content | getMessageText}}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div v-if="drawerType == 'allMessage'" class="message-pagination">
              <svg :class="{'icon': true, 'disabled': page.current == 1}" @click="handleCurrentChange(page.current-1)">
                <use xlink:href="#icon-jvs-a-zu4701-copy"></use>
              </svg>
              <el-input-number v-model="page.current" size="mini" controls-position="right" @change="handleCurrentChange(page.current)"></el-input-number>
              <span class="split">/</span>
              <span>{{Math.ceil(page.total / page.size)}}</span>
              <svg :class="{'icon': true, 'disabled': (page.current == Math.ceil(page.total / page.size))}" @click="handleCurrentChange(page.current+1)">
                <use xlink:href="#icon-jvs-a-zu4701"></use>
              </svg>
            </div>
          </div>
          <!-- 个人中心 -->
          <div v-if="drawerType == 'usercenter'" style="width: 100%;height: 100%;">
            <div class="user-info-body">
              <div class="user-info-box">
                <img :src="$store.getters.userInfo.headImg || '/jvs-ui-public/img/headImg.png'" alt="" class="header-img">
                <div class="info">
                  <div class="user-tenant-name">
                    <span>{{userInformation.tenant.shortName}}</span>
                  </div>
                  <div class="user-name">
                    <span>{{userInformation.realName}}</span>
                  </div>
                </div>
              </div>
              <div :class="{'seeting-logout-box': true, 'seeting-logout-box-col2': !$permissionMatch('jvs_create_tenant'), 'seeting-logout-box-more': $permissionMatch('jvs_create_tenant')}">
                <div v-if="$permissionMatch('jvs_create_tenant')" class="button" @click="handleJoinOrg">
                  <span>{{$langt(`common.rightDrawer.joinOrg`)}}</span>
                  <svg class="userInfo-close" aria-hidden="true">
                    <use xlink:href="#jvs-ui-icon-daoru"></use>
                  </svg>
                </div>
                <div v-if="$permissionMatch('jvs_create_tenant')" class="button" @click="handleCreateOrg">
                  <span>{{$langt(`common.rightDrawer.createOrg`)}}</span>
                  <svg class="userInfo-close" aria-hidden="true">
                    <use xlink:href="#jvs-ui-icon-xinjian"></use>
                  </svg>
                </div>
                <div class="button" @click="accountSetHandle">
                  <span>{{$langt(`common.rightDrawer.accSet`)}}</span>
                  <svg class="userInfo-close" aria-hidden="true">
                    <use xlink:href="#jvs-ui-icon-zhanghushezhi"></use>
                  </svg>
                </div>
                <div v-if="['钉钉客户端', '企业微信'].indexOf($store.getters.userInfo.loginType) == -1" class="button" @click="logout">
                  <span>{{$langt(`common.rightDrawer.sigOut`)}}</span>
                  <svg class="userInfo-close" aria-hidden="true">
                    <use xlink:href="#jvs-ui-icon-tuichudenglu"></use>
                  </svg>
                </div>
              </div>
              <div class="tenant-box">
                <div class="tenant-title">{{$langt(`common.rightDrawer.switchTenant`)}}</div>
                <div class="tenant-body">
                  <div class="tenant-lists">
                    <div v-for="item in changeTenantsList" :key="item.id" :class="{'tenant-item': true, 'active': item.id == $store.getters.userInfo.tenantId}" @click="tenantLoginHandle(item)">
                      <img v-if="item.icon" :src="item.icon" alt="" class="tenant-img">
                      <div class="tenant-name">
                        <span>{{item.shortName}}</span>
                      </div>
                      <div v-if="item.id == $store.getters.userInfo.tenantId" class="tenant-check">
                        <svg class="check-icon" aria-hidden="true">
                          <use xlink:href="#icon-jvs-xuanzhong"></use>
                        </svg>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <!-- 账户设置 -->
          <div v-if="drawerType == 'accountInfo'" style="width: 100%;height: 100%;">
            <accountSetting></accountSetting>
          </div>
        </div>
      </div>
    </el-drawer>
    <!-- 消息处理待办 -->
    <task-list ref="taskList" v-show="false" :notRequireData="true" @fresh="freshMess" />
  </div>
</template>
<script>
// 引入默认的主题风格
import {simpleStyle, darkblueStyle, darkredStyle} from '@/const/theme'
import top from './top/index'
import { getStore } from "@/util/store.js";
import { mapGetters, mapState } from "vuex";
import store from '@/store'
import {getAllSystemList, loginoutHandle, getDynamicResource, changeModeUser} from '@/api/admin/home'
import themeForm from './form'
import tenantDialog from '@/views/main/tenant/index'
import {getTenantByUser} from '@/api/login'
import bus from '@/util/vuebus'
import config from "./sidebar/config.js"
import { localMenu } from './sidebar/localMenu.js'
import {client_id} from '@/const/const'
import eventBus from "@/util/vuebus";
import inviteUser from "@/views/upms/views/inviteUser/new";
import addUser from "@/views/upms/views/inviteUser/add";
import { messageAllRead, messageaPage, deleteHideMessage} from "@/api/admin/message";
import {getMyTenantList} from "@/api/admin/user";
import accountSetting from '@/views/main/user/info'
import TaskList from "@/views/flowable/views/taskList";
import devpng from '@/const/img/DEV.png'
import betapng from '@/const/img/BETA.png'
import gapng from '@/const/img/GA.png'
export default {
  name: "tags",
  components: {
    top,
    themeForm,
    tenantDialog,
    inviteUser,
    addUser,
    accountSetting,
    TaskList
  },
  props: {
    freshAllMenuBool: {
      type: Number
    },
    openId: {
      type: String
    },
    showBaseSetting: {
      type: Number
    },
    jvsDesign: {
      type: Object
    },
    justAppInfo: {
      type: Boolean
    }
  },
  data () {
    return {
      changeTenantsList: [], // 可切换的租户
      myTenantList: [],
      // 查询条件
      queryParams: {
        search: '',
        type: ''
      },
      page: {
        total: 0,
        size: 20,
        current: 1
      },
      noList:[],
      insideList:[],
      messageLoading:false,
      readStatus:false,
      isShowInfo: false,
      storeTemp: store,
      active: "",
      contentmenuX: "",
      contentmenuY: "",
      contextmenuFlag: false,
      sysList: [],
      activeSystem: '',
      usertenantList: [],
      isMOreEntry: false,
      bool: false,
      activeMore: null,
      activeMenu: '',
      config: config,
      templateOpen: false,
      infoShow: false,
      lastActiveIndex: -1,
      activeIndex: -1,
      userInformation: {},
      menuLoading: false,
      inviteUser: false, // 加入组织 弹窗
      addUser: false, // 创建组织 弹窗
      searchMenuName: '', // 检索菜单名称
      searchNameMenuList: [], // 菜单名称检索结果
      showAppListBool: false, // 应用列表展示
      client_id: client_id,
      linkList: [],
      leftActive: '',
      modeUser: {
        mode: 'GA',
        userId: ''
      },
      modeUserForm: null,
      modeUserVisible: false,
      modeUserOption: {
        emptyBtn: false,
        btnHide: true,
        formAlign: 'top',
        column: [
          {
            label: '切换模式',
            prop: 'mode',
            type: 'radio',
            display: false,
            dicData: [
              { label: this.$langt(`topNav.modeUser.DEV`), value: 'DEV', tip: this.$langt(`topNav.modeUser.DEV_tip`), img: devpng },
              { label: this.$langt(`topNav.modeUser.BETA`), value: 'BETA', tip: this.$langt(`topNav.modeUser.BETA_tip`), img: betapng },
              { label: this.$langt(`topNav.modeUser.GA`), value: 'GA', tip: this.$langt(`topNav.modeUser.GA_tip`), img: gapng },
              // { label: '开发模式', value: 'DEV', tip: '用于配置和新增设计', img: devpng },
              // { label: '测试模式', value: 'BETA', tip: '模拟不同的人员使用应用测试权限等', img: betapng },
              // { label: '正式模式', value: 'GA', tip: '真实用户使用应用功能', img: gapng }
            ]
          },
          {
            label: this.$langt(`topNav.modeUser.user`), // '模拟人员 (非必选)',
            prop: 'userId',
            type: 'user',
            placeholder: this.$langt(`topNav.modeUser.userPlaceholder`),
            props: {
              label: 'userName',
              value: 'userId'
            },
            displayExpress: [ { prop: 'mode', value: 'BETA,DEV' } ]
          }
        ]
      },
      windowChannel: null,
      drawerVisible: false,
      drawerType: ''
    };
  },
  async created () {
    if(getStore({ name: 'userInfo' })) {
      this.userInformation = getStore({ name: 'userInfo' })
    }
    if(this.jvsDesign && this.jvsDesign.JVS_DESIGN_MGR) {
      // 确定模式
      let tmu = getStore({ name: 'modeUserInfo' })
      if(tmu && tmu.mode) {
        this.modeUser = tmu
      }else{
        if(this.jvsDesign.JVS_DESIGN_DEFAULT_MODE) {
          this.$set(this.modeUser, 'mode', this.jvsDesign.JVS_DESIGN_DEFAULT_MODE)
        }
        this.$store.commit("SET_MODEUSER_INFO", this.modeUser)
      }
      let temp = {mode: this.modeUser.mode}
      if(this.modeUser.userId) {
        temp.analogUser = {
          id: this.modeUser.userId
        }
      }
      await this.changeModeUserEvent(temp, 'create')
      this.getAllSysntem()
    }
    this.getDynamicResource()
    this.getChangeTenants()
    // this.getMyTenantListHandle()
    eventBus.$off("freshUserInfo")
    eventBus.$on("freshUserInfo", data => {
      if(data) {
        this.userInformation = data
        this.$forceUpdate()
      }
    });
    eventBus.$off("freshAllMenu")
    eventBus.$on("freshAllMenu", data => {
      if(data) {
        this.menuLoading = true
        this.getAllSysntem()
      }
    });
    //2025.03.07 后续合并代码时保留这段   开始
    if(this.$route.query && this.$route.hash) {
      let type = ''
      let newVal = this.$route.query.src + this.$route.hash.slice(2, this.$route.hash.length)
      localMenu.systemMenu.filter(pit => {
        if(pit.children && pit.children.length > 0) {
          pit.children.filter(pcit => {
            if(pcit.extend && pcit.extend.url == newVal) {
              type = 'system'
            }
          })
        }
      })
      localMenu.platformMenu.filter(pit => {
        if(pit.children && pit.children.length > 0) {
          pit.children.filter(pcit => {
            if(pcit.extend && pcit.extend.url == newVal) {
              type = 'platform'
            }
          })
        }
      })
      if(type) {
        this.settingApp(type)
      }
    }
    // if(this.showBaseSetting > -1) {
    //   let type = 'system'
    //   if(this.$route.query && this.$route.hash) {
    //     let newVal = this.$route.query.src + this.$route.hash.slice(2, this.$route.hash.length)
    //     localMenu.platformMenu.filter(pit => {
    //       if(pit.children && pit.children.length > 0) {
    //         pit.children.filter(pcit => {
    //           if(pcit.extend && pcit.extend.url == newVal) {
    //             type = 'platform'
    //           }
    //         })
    //       }
    //     })
    //   }
    //   this.settingApp(type)
    // }
    //2025.03.07 后续合并代码时保留这段    结束
    // 监听子页面传值
    let _this = this
    window.addEventListener('message',function(e){
      if(e.data) {
        if(e.data.command == 'openWorkplace') {
          _this.openWorkplace()
        }
      }
    },false);
    this.windowChannel = new BroadcastChannel('tagNotice')
  },
  beforeDestory () {
    eventBus.$off("freshUserInfo")
    eventBus.$off("freshAllMenu")
  },
  mounted () {
    this.connectWebsocket()
  },
  computed: {
    ...mapGetters(["tagWel", "tagList", "tag", "website", "menu", "keyCollapse"]),
    ...mapState({
      showTag: state => state.common.showTag,
       isProcessIndexRoute() {
      // 判断当前路由是否为 /process/index
      return this.$route.path === '/process/index';
    }
    }),
    tagLen () {
      return this.tagList.length||0;
    },
    defalutThemeConst () {
      return [simpleStyle, darkblueStyle, darkredStyle]
    },
    hasSwitch () {
      return getStore({name: 'switchTenant'})
    },
    labelKey () {
      return this.website.menu.props.label||this.config.propsDefault.label;
    },
    pathKey () {
      return this.website.menu.props.path||this.config.propsDefault.path;
    },
    nowTagValue () {
      return this.$router.$jvsRouter.formatMenuPath(this.$route);
    },
    remainingCount(){
      return this.$store.state.socket.messageSocketMsg.remainingCount || 0
    },
  },
  filters: {
    formatLogTime(value) {
      return value // relativelyTime(value)
    },
    getMessageText (html) {
      let str = html
      if(html && html.startsWith('<')) {
        let div = document.createElement('div')
        div.innerHTML = html
        str = div.innerText
      }
      return str
    }
  },
  methods: {
    handleOrgClose() {
      this.inviteUser = false
    },
    // 加入组织
    handleJoinOrg() {
      this.inviteUser = true
    },
    // 创建组织
    handleCreateOrg() {
      this.addUser = true
    },
    handleCreateOrgClose() {
      this.addUser = false
    },
    handleCurrentChange (val) {
      if(val > 0 && val < (Math.ceil(this.page.total / this.page.size) + 1)) {
        this.page.current = val
        this.getMessagePage()
      }else{
        if(val == 0) {
          this.page.current = 1
        }
        if(val == (Math.ceil(this.page.total / this.page.size) + 1)) {
          this.page.current = Math.ceil(this.page.total / this.page.size)
        }
      }
    },
    getMessagePage () {
      this.messageLoading = true
      let obj = {
        current: this.page.current,
        size: this.page.size,
      }
      messageaPage(obj).then(res=>{
        if(res.data.code == 0){
          this.insideList = JSON.parse(JSON.stringify(res.data.data.records))
          this.page.total = res.data.data.total
          this.insideList.forEach(item => {
            item.msgContent = JSON.parse(item.msgContent)
          })
        }
      }).finally(res=>{
        this.messageLoading = false
      })
    },
    connectWebsocket(){
      this.$store.dispatch('MESSAGE_WS_INIT', this.userInformation.id)
    },
    //标记全部已读
    editreadStatus(){
      this.readStatus = true
      messageAllRead().then(res => {
        if(res.data.code == 0){
          this.$store.commit('SET_MESSAGE_DATA',{remainingCount:0})
        }
      })
    },
    // 消息中心
    getMessage (type) {
      this.readStatus = false
      this.messageLoading = true
      let obj = {
        current: 1,
        size: 20,
        largeCategories: type ? type : 'notice',
      }
      if(this.queryParams.search) {
        obj.search = this.queryParams.search
      }
      if(this.queryParams.type) {
        obj.type = this.queryParams.type
      }
      messageaPage(obj).then(res=>{
        if(res.data.code == 0){
          this.insideList = JSON.parse(JSON.stringify(res.data.data.records))
          this.insideList.forEach(item => {
            item.msgContent = JSON.parse(item.msgContent)
          })
        }
      }).finally(res=>{
        this.messageLoading = false
      })
    },
    // 显示基本设置
    handleShowInfo() {
      this.isShowInfo = true
    },
    // 隐藏基本设置
    handleHideInfo() {
      this.isShowInfo = false
    },
    watchContextmenu () {
      if (!this.$el.contains(event.target)||event.button!==0) {
        this.contextmenuFlag=false;
      }
      window.removeEventListener("mousedown", this.watchContextmenu);
    },
    handleContextmenu (event) {
      let target=event.target;
      // 解决 https://github.com/d2-projects/d2-admin/issues/54
      let flag=false;
      if (target.className && target.className.indexOf("el-tabs__item")>-1) flag=true;
      else if (target.parentNode.className.indexOf("el-tabs__item")>-1) {
        target=target.parentNode;if (typeof target.className == 'string' && target.className.indexOf("el-tabs__item")>-1) flag=true;
        else if (typeof target.parentNode.className == 'string' && target.parentNode.className.indexOf("el-tabs__item")>-1) {
          target=target.parentNode;
          flag=true;
        }else if(typeof target.parentNode.className == 'object' && target.parentNode.parentNode.className.indexOf("el-tabs__item")>-1){
          target=target.parentNode.parentNode;
          flag=true;
        }
        flag=true;
      }
      if (flag) {
        event.preventDefault();
        event.stopPropagation();
        this.contentmenuX=event.clientX;
        this.contentmenuY=event.clientY;
        this.tagName=target.getAttribute("aria-controls").slice(5);
        this.contextmenuFlag=true;
      }
    },
    //激活当前选项
    setActive () {
      this.active=this.tag.hash ? (this.tag.value + this.tag.hash) : this.tag.value;
    },
    menuTag (value, action) {
      if (action==="remove") {
        let { tag, key }=this.findTag(value);
        this.$store.commit("DEL_TAG", tag);
        if(tag.hash) {
          if (tag.hash===this.tag.hash) {
            tag=this.tagList[key===0? key:key-1]; //如果关闭本标签让前推一个
            this.openTag(tag);
          }
        }else{
          if (tag.value===this.tag.value) {
            // console.log(this.tag.value)
            tag=this.tagList[key===0? key:key-1]; //如果关闭本标签让前推一个
            this.openTag(tag);
          }
        }
      }
    },
    openTag (item) {
      // 重复点击不处理
      if(item.name == this.tag.value + this.tag.hash){
        return false
      }
      let tag;
      if (item.name) {
        tag=this.findTag(item.name).tag;
      } else {
        tag=item;
      }
      if(tag.label == '首页') {
        tag = this.website.fistPage
      }
      this.$router.push({
        path: this.$router.$jvsRouter.getPath({
          name: tag.label,
          src: tag.value + tag.hash
        }),
        query: tag.query
      });
    },
    closeOthersTags () {
      this.contextmenuFlag=false;
      this.$store.commit("DEL_TAG_OTHER");
    },
    findTag (value) {
      let tag, key;
      this.tagList.map((item, index) => {
        if ((item.value + item.hash) === value) {
          tag=item;
          key=index;
        }else{
          if(item.value === value) {
            tag=item;
            key=index;
          }
        }
      });
      return { tag: tag, key: key };
    },
    closeAllTags () {
      this.contextmenuFlag=false;
      this.$store.commit("DEL_ALL_TAG");
      if(this.tag.value != '/wel/index') {
        this.$router.push({
          path: this.$router.$jvsRouter.getPath({
            src: this.tagWel.value
          }),
          query: this.tagWel.query
        });
      }
    },
    // 我的组织
    getMyTenantListHandle () {
      getMyTenantList().then(res => {
        if(res.data.code == 0) {
          this.myTenantList = res.data.data
          this.getChangeTenants()
        }
      })
    },
    // 获取切换的租户列表
    getChangeTenants () {
      this.changeTenantsList = [...this.$store.getters.userInfo.tenants]
    },
    // 应用设置
    settingApp(type) {
      this.$emit('openAppSetting', true, type)
      this.templateOpen = false
      this.$emit('openTemplate', false)
      this.$emit('openSystemSetting', false)
      this.$emit('openWorkplace', false)
      this.$emit('openTemplateMore', false)
    },
    // 系统设置
    settingSystem(type) {
      this.$emit('openSystemSetting', true)
      this.activeSystem = ""
    },
    logout () {
      this.$confirm(`${this.$langt('common.login.outInfo')}`, `${this.$langt('common.login.tips')}`, {
        confirmButtonText: this.$langt('common.confirm'),
        cancelButtonText: this.$langt('common.cancel'),
        type: "warning"
      }).then(() => {
        loginoutHandle().then(res => {
          if(res.data.code == 0) {
            let path = this.$store.state.common.template || '/login'
            sessionStorage.clear()
            this.$store.dispatch("LogOut")
            this.$router.push({ path: path })
          }
        })
      });
    },
    goInfo (act) {
      this.templateOpen = false
      this.$emit('openTemplate', false)
      this.$emit('openAppSetting', false, '')
      this.$emit('openSystemSetting', false)
      this.$emit('openWorkplace', false)
      this.$emit('openCatalogue', null)
      this.$emit('openTemplateMore', false)
      this.infoShow = false
      if(this.$route.hash != '#/userinfo' && (this.$route.query && this.$route.query.src != '/')) {
        this.$router.push({
          path: this.$router.$jvsRouter.getPath({
            name: "个人信息",
            src: '/#/userinfo'
          })
        })
      }
    },
    // 进入消息中心
    messageCenter () {
      this.$router.push({ path: '/usermessage' })
    },
    // 获取所有系统
 getAllSysntem (optype) {
      getAllSystemList(client_id).then(res => {
        if(res.data && res.data.code == 0) {
          this.sysList = [...res.data.data || []].sort((a, b) =>
        (b.name === '平台级管理驾驶舱') - (a.name === '平台级管理驾驶舱')
      );
          this.$store.commit("SET_MENU_ALL", this.sysList)
          if(this.sysList.length > 0) {
            if(this.$store.getters.system) {
              this.activeSystem = this.$store.getters.system
              let isIn = false
              res.data.data.filter(rit => {
                if(rit.id == this.activeSystem) {
                  isIn = true
                }
              })
              if(this.justAppInfo) {
                this.$emit('mismatch', isIn ? false : true)
              }
            }else{
              if(this.justAppInfo) {
                this.$emit('mismatch', true)
              }else{
                this.activeSystem = this.sysList[0].id
                this.$store.commit("SET_SYSTEM", this.activeSystem)
              }
            }
            this.$emit('changeSystem', this.activeSystem)
          }
          this.menuLoading = false
          if(optype == 'changeModeUser') {
            this.openTemplate((this.sysList && this.sysList.length > 0) ? '' : 'changeModeUser') //  打开应用中心，菜单为空时传参
          }
          this.$forceUpdate()
        }
      })
    },
    // 切换系统
    entrySystem(item, openApp) {
      // console.log('entrySystem')
      this.activeSystem = item.id;
      this.$emit("changeSystem", item.id);
      this.$store.commit("SET_SYSTEM", this.activeSystem);
      this.templateOpen = false;
      this.$emit("openTemplate", false);
      this.$emit("openAppSetting", false, "");
      this.$emit("openSystemSetting", false);
      this.$emit("openWorkplace", false);
      this.$emit("openTemplateMore", false);
      if (
        item &&
        item.extend &&
        item.extend.type === "jvsapp" &&
        openApp &&
        item.extend.designRole
      ) {
        // this.$emit("openAppManage", item.id); // 仅切换应用，无需进入应用管理界面
      }
    },
    // 获取该用户下所有的租户列表
    getTenantByUserList () {
      getTenantByUser().then(res => {
        if(res.data.code == 0) {
          let list = []
          if(res.data.data) {
            list = res.data.data
            if(list.length > 0) {
              // 只有一个租户直接进去
              if(list.length == 1) {
                this.$store.commit("DEL_ALL_TAG") // 清空已打开的页面
                this.$refs.tenantDialog.tenantLoginHandle(list[0])
              }else{
                this.usertenantList = list
                // this.dialogVisible = true
                this.$refs.tenantDialog.init()
              }
            }
          }
        }
      })
    },
    changeTenant () {
      this.templateOpen = false
      this.$emit('openTemplate', false)
      this.$emit('openAppSetting', false, '')
      this.$emit('openSystemSetting', false)
      this.$emit('openWorkplace', false)
      this.$emit('openTemplateMore', false)
      this.getTenantByUserList()
    },
    reload (bool) {
      bus.$emit('refresh', true);
    },
    openMoreMenu (item) {
      if (item.extend && item.extend.design) {
        this.activeMenu = item.id
        this.entrySystem(this.activeMore)
        this.isMOreEntry = false
        let url = ''
        switch (item.extend.design) {
          case 'chart':
            url = `/chart-design-ui/chartShow?type=pc&id=${item.extend.id}`;
            break
          case 'page':
            url = `/page-design-ui/list/use?id=${item.extend.id}&dataModelId=${item.extend.dataModelId}&jvsAppId=${item.extend.jvsAppId}`;
            break
          case 'form':
            url = `/page-design-ui/form/use?id=${item.extend.id}&dataModelId=${item.extend.dataModelId}&jvsAppId=${item.extend.jvsAppId}`;
            break
        }
        this.$router.push({
          path: this.$router.$jvsRouter.getPath({
            name: item.extend.name,
            src: url
          }),
        })
      } else {
        this.activeMenu = item.id
        this.entrySystem(this.activeMore)
        this.openMenuRoute(item)
        this.isMOreEntry = false
      }
    },
    openMenuRoute (item) {
      // 重复点击不跳转
      let tempStr = ""
      if(item.extend && item.extend.url && item.extend.url.indexOf('#') > -1){
        tempStr = (item.extend.url && ('#' + item.extend.url.split('#')[1])) || ''
      }else if (item.extend && item.extend.url && item.extend.url.indexOf('-ui') > -1) {
        let indx = item.extend.url.indexOf('-ui')
        tempStr = item.extend.url.slice(0, indx+3) + '/#' + item.extend.url.slice(indx+3, item.extend.url.length)
      }
      // 应用跳转
      if (item.extend && item.extend.type === 'jvsapp') {
        this.entrySystem(item, true)
        return
      }
      // 设计跳转
      if (item.extend && item.extend.design) {
        let url = ''
        switch (item.extend.design) {
          case 'chart':
            url = `/chart-design-ui/chartShow?type=pc&id=${item.extend.id}`;
            break
          case 'page':
            url = `/page-design-ui/list/use?id=${item.extend.id}&dataModelId=${item.extend.dataModelId}`;
            break
          case 'form':
            url = `/page-design-ui/form/use?id=${item.extend.id}&dataModelId=${item.extend.dataModelId}`;
            break
          case 'screen':
            url = `/data-screen-ui/#/screenPreview?id=${item.extend.id}&isPreview=true`;
            break
        }
        this.$router.push({
          path: this.$router.$jvsRouter.getPath({
            name: item.extend.name,
            src: url
          }),
        })
        return
      }
      if(this.$route.hash && tempStr == this.$route.hash) {
        return false
      }
      if (this.screen<=1) this.$store.commit("SET_COLLAPSE");
      this.$router.$jvsRouter.group=item.group;
      if(item.extend.newWindow === true) {
        if(item.extend.url.includes('http') || item.extend.url.includes('https') || item.extend.url.includes('ftp')) {
          this.$openUrl(item[this.pathKey], '_blank')
        }else{
          if(item.extend.url.indexOf('-ui') == -1) {
            this.$openUrl(item[this.pathKey], '_blank')
          }else{
            let tinx = item.extend.url.indexOf('-ui')
            let tpStr = item.extend.url.slice(0, (tinx+3)) + '/#' + item.extend.url.slice(tinx+3, item.extend.url.length)
            this.$openUrl(tpStr, '_blank')
          }
        }
      }else{
        this.$router.push({
          path: this.$router.$jvsRouter.getPath({
            name: item[this.labelKey],
            src: item.extend[this.pathKey]
          }),
          query: item.query,
          params: item.params
        })
      }
    },
    // 模板管理
    openTemplate (type) {
      this.templateOpen = true
      this.$emit('openTemplate', true, type)
      this.activeSystem = ""
      this.leftActive = ''
    },
    // 工作台
    openWorkplace() {
      this.$emit('openWorkplace', true)
      this.activeSystem = ""
      this.leftActive = 'work'
      this.templateOpen = false
    },
    // 显示菜单检索结果
    searchNameMenuHandle () {
      if(this.searchMenuName) {
        let list = []
        for(let i in this.sysList) {
          if(this.sysList[i].children && this.sysList[i].children.length > 0) {
            this.eachMenuTree(this.sysList[i].children, list, this.sysList[i], i)
          }
        }
        this.searchNameMenuList = list
      }else{
        this.searchNameMenuList = []
      }
    },
    // 遍历应用菜单
    eachMenuTree (array, list, sysItem, sysIndex) {
      for(let i in array) {
        if(!array[i].children && array[i].name.includes(this.searchMenuName)) {
          let temp = JSON.parse(JSON.stringify(array[i]))
          if(sysItem) {
            temp.sysItem = JSON.parse(JSON.stringify(sysItem))
          }
          if(sysIndex) {
            temp.sysIndex = sysIndex
          }
          list.push(temp)
        }
        if(array[i].children && array[i].children.length > 0) {
          this.eachMenuTree(array[i].children, list, sysItem, sysIndex)
        }
      }
    },
    // 从“搜索结果”进入
    searchMenuNameClick(item) {
      console.log('searchMenuNameClick')
      this.activeMore = item.sysItem;
      this.$emit("openAppManage", item.sysItem.id, false);
      this.openMoreMenu(item);
    },
    closeSearchMenuName () {
      this.searchMenuName = ''
      this.searchNameMenuList = []
    },
    // 从“我的应用”进入
    enterAppItem(app) {
      this.entrySystem(app, true);
      this.showAppListBool = false;
      this.leftActive = "";
      this.templateOpen = false;
      this.$forceUpdate();
    },
    getDynamicResource () {
      getDynamicResource().then(res => {
        if(res.data && res.data.code == 0) {
          this.linkList = res.data.data
        }
      })
    },
    enterLink (item) {
      this.$openUrl(`${item.url}`, '_blank')
    },
    changeModeAndUser () {
      this.modeUserForm = JSON.parse(JSON.stringify(this.modeUser))
      this.modeUserVisible = true
    },
    modeUserClose () {
      this.modeUserVisible = false
      this.modeUserForm = null
    },
    changeModeUserEvent (data, origin) {
      changeModeUser(data).then(res => {
        if(res.data && res.data.code == 0) {
          if(origin && origin == 'dialog') {
            this.$notify({
              title: '提示',
              message: '切换成功',
              position: 'bottom-right',
              type: 'success',
              duration: 1000
            })
            this.modeUser = JSON.parse(JSON.stringify(this.modeUserForm))
            this.$store.commit("SET_MODEUSER_INFO", this.modeUser)
            this.modeUserClose()
            this.getAllSysntem('changeModeUser') // 刷新快捷菜单
            if(data.analogUser && data.analogUser.id) {
              eventBus.$emit('dynaIndex', {oprate: 'clear'})
            }else{
              eventBus.$emit('dynaIndex', {oprate: 'fresh'})
            }
          }
          this.$emit('changeModeUser')
          if(origin !== 'create') {
            this.$store.commit("DEL_ALL_TAG")
            this.$router.push({
              path: this.$router.$jvsRouter.getPath({
                src: '/wel/index'
              })
            })
          }
          this.windowChannel.postMessage('changeUserMode')
        }
      })
    },
    modeUserSubmit () {
      let temp = {mode: this.modeUserForm.mode}
      if(this.modeUserForm.userId) {
        temp.analogUser = {
          id: this.modeUserForm.userId
        }
      }
      this.$store.commit("SET_SYSTEM", '')
      this.changeModeUserEvent(temp, 'dialog')
    },
    modeUserFormChange (form, item) {
      if(item.prop == 'mode') {
        if(form[item.prop] == 'GA') {
          this.$set(this.modeUserForm, 'userId', '')
          this.$set(this.modeUserForm, 'userName', '')
        }
      }
    },
    openDrawer (type) {
      this.drawerType = type
      if(type == 'message') {
        this.getMessage()
      }
      if(type == 'allMessage') {
        this.getMessagePage()
      }
      this.drawerVisible = true
    },
    drawerClose () {
      this.drawerVisible = false
    },
    backHandle () {
      if(this.drawerType == 'allMessage') {
        this.openDrawer('message')
      }
      if(this.drawerType == 'accountInfo') {
        this.openDrawer('usercenter')
      }
    },
    openMessMore (item) {
      if(item.msgContent.content.length > 20) {
        this.$set(item, 'open', !(item.open))
        this.$forceUpdate()
      }
    },
    accountSetHandle () {
      this.openDrawer('accountInfo')
    },
    // 切换租户登录
    tenantLoginHandle (item) {
      if(item.id) {
        this.tenantLoading = true
        this.$store.dispatch('RefreshToken', item.id).then(data => {
          if(data) {
            if(data.userDto && data.userDto.callBackUrl && localStorage.getItem('loginQuery')){
              this.$openUrl(`${data.userDto.callBackUrl}?access_token=${data.access_token}&refresh_token=${data.refresh_token}`, '_self')
              localStorage.setItem('loginQuery', '')
            }else{
              this.setUserInfoData(data)
              this.$store.commit("SET_SYSTEM", "");
              this.$store.commit("SET_MENU_TYPE", "");
              this.$store.commit("SET_APP_SETTING_OPEN", false);
              this.$store.commit("DEL_ALL_TAG");
              this.$router.push({
                path: this.$router.$jvsRouter.getPath({
                  src: '/wel/index'
                }),
              });
              window.parent.postMessage({command: 'fresh'}, '*')
            }
          }
        })
      }
    },
    // 登录成功写入数据
    setUserInfoData (data) {
      this.$store.commit("SET_ACCESS_TOKEN", data.access_token);
      this.$store.commit("SET_REFRESH_TOKEN", data.refresh_token);
      this.$store.commit("SET_EXPIRES_IN", data.expires_in);
      this.$store.commit("CLEAR_LOCK");
      this.$store.commit("SET_USER_INFO", data.userDto);
      this.$store.commit("SET_ROLES", data.roles || []);
      this.$store.commit("SET_PERMISSIONS", data.permissions || []);

      this.$store.commit("SET_TENANTId", data.userDto.tenantId);
      this.$store.commit("DEL_ALL_TAG"); // 关闭之前打开的所有tag
      this.$store.commit("SET_THEME_NAME", ""); // 清除主题
      this.$store.commit("SET_TENANTINFO", data.userDto.tenant)
    },
    dealHandle (item, type) {
      let row = item.msgContent.oprateParam
      row.id = row.taskId
      this.$refs.taskList.dealHandle(row, type)
    },
    freshMess () {
      if(this.drawerType == 'message') {
        this.getMessage()
      }
      if(this.drawerType == 'allMessage') {
        this.getMessagePage()
      }
    },
    deleteMessageHandle (item) {
      deleteHideMessage(item.id).then(res => {
        if(res.data && res.data.code == 0) {
          this.$notify({
            title: '提示',
            message: this.$langt(`common.delSuccess`),
            position: 'bottom-right',
            type: 'success',
            duration: 1000
          })
          this.freshMess()
        }
      })
    }
  },
  watch: {
    freshAllMenuBool: {
      handler(newVal, oldVal) {
        if(newVal != -1) {
          this.menuLoading = true
          this.getAllSysntem()
        }
      }
    },
    showBaseSetting: {
      handler(newVal, oldVal) {
        if(newVal > -1) {
          this.settingApp((newVal > 1) ? 'system' : 'platform')
        }
      }
    }
  }
};
</script>
<style lang="scss" scoped>
.setting-icon{
  padding: 5px;
  border-radius: 4px;
  transition: 0.2s;
  cursor: pointer;
  margin-left: 9px;
  width: 32px;
  height: 32px;
  overflow: hidden;
  box-sizing: border-box;
  i{
    font-size: 20px;
    color: #575E73;
  }
  .icon{
    width: 22px;
    height: 22px;
  }
  .hover-show{
    display: none;
  }
  &:hover{
    transition: 0.2s;
    background-color: #F5F6F7;
    .icon{
      display: none;
    }
    .hover-show{
      display: block;
    }
  }
}

.app-list-box{
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  padding: 10px 30px;
  max-height: 560px;
  overflow: hidden;
  overflow-y: auto;
  .app-list-item{
    width: 205px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 10px;
    cursor: pointer;
    box-sizing: border-box;
    padding: 10px;
    border-radius: 5px;
    overflow: hidden;
    img{
      width: 40px;
      height: 40px;
    }
    div{
      display: flex;
      flex-direction: column;
      align-items: center;
      span{
        width: 135px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: pre;
        font-size: 14px;
        color: #333;
      }
      span:nth-last-of-type(1){
        font-size: 12px;
        color: #999;
        max-height: 18px;
      }
    }
    .has-design-tag{
      width: 135px;
      display: flex;
      align-items: center;
      flex-direction: row;
      span:not(.el-tag){
        width: auto;
        max-width: 94px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: pre;
        font-size: 14px;
        color: #333;
      }
      .el-tag{
        width: 36px;
        line-height: 16px;
        margin-left: 5px;
        span{
          display: inline;
        }
        &.el-tag--success{
          color: #67c23a;
          span{
            color: #67c23a;
          }
        }
        &.el-tag--warning{
          color: #E6A23C;
          span{
            color: #E6A23C;
          }
        }
      }
    }
  }
  .app-list-item:hover{
    background: #f5f7fa;
  }
}

.link-list-box{
  padding: 24px;
  box-sizing: border-box;
  display: grid;
  grid-template-columns: repeat(3,1fr);
  column-gap: 24px;
  row-gap: 24px;
  .link-list-item{
    width: 88px;
    height: 88px;
    background: #F5F6F7;
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-direction: column;
    cursor: pointer;
    div{
      display: flex;
      flex-direction: column;
      align-items: center;
      border-radius: 4px;
      box-sizing: border-box;
      cursor: pointer;
      overflow: hidden;
      padding: 8px 3px;
      text-align: center;
      img{
        width: 32px;
        height: 32px;
        display: inline-block;
      }
      span{
        margin-top: 3px;
        font-size: 14px;
        font-family: Source Han Sans-Regular, Source Han Sans;
        color: #363b4c;
        max-width: 100%;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
    &:hover{
      background-color: #eeeff0;
    }
  }
  .link-list-item:hover{
    background: #f5f7fa;
  }
}

.right-tool-menu{
  padding: 0 8px;
  .user-info{
    padding: 4px;
    display: flex;
    justify-content: center;
    margin: 0 auto;
    margin-bottom: 12px;
    img{
      width: 40px;
    }
  }
  .menu-item{
    display: flex;
    align-items: center;
    height: 32px;
    line-height: 32px;
    padding: 6px 24px;
    cursor: pointer;
    transition: 0.3s;
    border-radius: 6px;
    &:hover{
      transition: 0.3s;
      background-color: #eff2f7;
    }
  }
}

.change-model-user-box{
  margin-top: 24px;
  .model-list{
    margin: 0 32px;
    display: flex;
    align-items: center;
    padding-bottom: 24px;
    .model-list-item{
      width: 216px;
      height: 176px;
      border-radius: 4px 4px 4px 4px;
      border: 1px solid #EEEFF0;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      img{
        display: block;
        width: 80px;
        height: 80px;
      }
      h4{
        margin: 0;
        margin-top: 12px;
        padding: 0;
        height: 23px;
        font-family: Source Han Sans-Bold, Source Han Sans;
        font-weight: 700;
        font-size: 16px;
        color: #363B4C;
        line-height: 23px;
      }
      span{
        margin-top: 4px;
        height: 17px;
        font-family: Source Han Sans-Regular, Source Han Sans;
        font-weight: 400;
        font-size: 12px;
        color: #6F7588;
        line-height: 17px;
        word-break: break-word;
        text-align: center;
      }
      &.active{
        background: #EDF4FF;
        border-color: #1E6FFF;
      }
      &:hover{
        background: #EDF4FF;
      }
      &+.model-list-item{
        margin-left: 16px;
      }
    }
  }
  /deep/.jvs-form{
    margin: 0 32px;
  }
  .footer-btn{
    border-top: 1px solid #EEEFF0;
    height: 60px;
    box-sizing: border-box;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    .btn{
      margin-right: 16px;
      cursor: pointer;
      width: 60px;
      height: 32px;
      line-height: 32px;
      border-radius: 4px;
      font-family: Microsoft YaHei-Regular, Microsoft YaHei;
      font-weight: 400;
      font-size: 14px;
      text-align: center;
      &.cancel{
        background: #F5F6F7;
        color: #363B4C;
      }
      &.submit{
        background: #1E6FFF;
        color: #fff;
      }
    }
  }
}
</style>
<style lang="scss">
.jvs-tags{
  .jvs-tags__box{
    padding: 0;
    .lineBox {
      position: relative;
      display: flex;
      justify-content: space-between;
      height: 100%;
      font-size: 13px;
      .top-nav{
        height:100%;
        display: flex;
        align-items: center;
        flex:1;
        box-sizing: border-box;
      }
      .system-list{
        display: flex;
        margin: 0;
        padding: 0;
        box-sizing: border-box;
        overflow: hidden;
        overflow-x: auto;
        width: auto;
        height: 100%;
        margin-left: 1px;
        margin-top: 1px;
        li{
          display: flex;
          align-items: center;
          justify-content: center;
          color: #363B4C;
          font-size: 14px;
          cursor: pointer;
          padding: 0 25px;
          background: linear-gradient(180deg, #F5F6F7 0%, rgba(245,246,247,0) 100%);
          span{
            word-break: keep-all;
            font-family: Source Han Sans-Regular, Source Han Sans;
            font-weight: 400;
          }
          .normal-svg-icon{
            display: block;
          }
          .active-svg-icon{
            display: none;
          }
          &:hover, &.active-li{
            background: linear-gradient(180deg, #E4EDFF 0%, rgba(228,237,255,0) 100%);
            span{
              color: #1E6FFF;
            }
            .normal-svg-icon{
              display: none;
            }
            .active-svg-icon{
              display: block;
            }
          }
        }
      }
      .rightTool {
        display: flex;
        align-items: center;
        padding-right: 12px;
        .search-input-box{
          background-color: #f5f6f7;
          border-radius: 4px;
          height: 36px;
          width: 224px;
          display: flex;
          align-items: center;
          cursor: pointer;
          margin-right: 2px;
          .icon{
            width: 14px;
            height: 14px;
            margin: 0 14px;
          }
          .h-line{
            height: 14px;
            background-color: #d7d8db;
            width: 1px;
            margin-left: 0;
            margin-right: 0;
          }
          .text{
            margin-left: 14px;
            font-size: 14px;
            color: #6f7588;
            font-family: Microsoft YaHei-Regular, Microsoft YaHei;
          }
        }
        .split-line{
          background-color: #f0f1f2;
          height: 24px;
          width: 1px;
          margin-left: 24px;
          margin-right: 24px;
        }
        .customer-service{
          cursor: pointer;
          display: flex;
          align-items: center;
          justify-content: center;
          height: 20px;
          border-radius: 20px;
          padding: 2px 8px;
          margin-left: 9px;
          font-size: 12px;
          background-color: #e5f0fa;
          color: #3471ff;
          i{
            margin-right: 0;
          }
        }
        .user-header{
          width: 36px;
          height: 36px;
          border-radius: 6px;
          overflow: hidden;
          margin-left: 17px;
          img{
            display: block;
            width: 100%;
            height: 100%;
            cursor: pointer;
          }
        }
      }
    }
  }
  .menu-loading-back{
    background-image: url('../../../../public/jvs-ui-public/img/loading.gif');
    background-color: #fff;
    background-repeat: no-repeat;
    background-position: center;
    position: fixed;
    width: 100%;
    height: 100vh;
    top: 0;
    left: 0;
    z-index: 999999;
  }
}

.el-drawer.right-tool-custom-drawer{
  width: auto!important;
  .drawer-box{
    height: 100%;
    overflow: hidden;
    .drawer-top{
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 24px 24px 0;
      margin-bottom: 16px;
      .drawer-top-title{
        font-size: 20px;
        color: #363b4c;
        font-family: Source Han Sans-Bold, Source Han Sans;
        flex: 1;
        .back{
          width: 20px;
          height: 20px;
          margin-right: 8px;
          cursor: pointer;
        }
      }
      .close-icon{
        width: 16px;
        height: 16px;
        cursor: pointer;
      }
    }
    .drawer-body{
      height: calc(100% - 70px);
      overflow: hidden;
      overflow-y: auto;
    }
    &.search{
      width: 360px;
      .drawer-body{
        .search-input-box{
          padding: 0 24px;
          box-sizing: border-box;
          .el-input{
            display: flex;
            align-items: center;
            svg{
              cursor: pointer;
            }
            .el-input__inner{
              border: 0;
              height: 32px;
              border-bottom: 1px solid #EEEFF0;
              box-sizing: border-box;
            }
            .el-input__prefix{
              svg{
                margin-top: 8px;
              }
            }
            .el-input-group__append{
              border: 0;
              background: none;
            }
          }
        }
        .search-title{
          font-size: 14px;
          padding: 16px 24px;
          box-sizing: border-box;
          font-family: Source Han Sans-Regular, Source Han Sans;
          display: flex;
          align-items: center;
          justify-content: space-between;
        }
        .search-list{
          height: calc(100% - 84px);
          overflow: hidden;
          overflow-y: auto;
          .search-list-item{
            padding: 8px 24px;
            margin-bottom: 8px;
            display: flex;
            cursor: pointer;
            .menu-name{
              font-size: 14px;
              font-family: Source Han Sans-Bold, Source Han Sans;
              color: #363b4c;
              overflow: hidden;
              text-overflow: ellipsis;
            }
            &:hover{
              background: #f5f6f7;
            }
          }
          .empty-box{
            display: flex;
            align-items: center;
            justify-content: center;
            flex-direction: column;
            padding: 0 15px;
            width: 100%;
            box-sizing: border-box;
            .empty-img{
              width: 140px;
            }
            .empty-text{
              color: #3d3d3d;
              font-size: 16px;
              text-align: center;
              font-family: Source Han Sans-Medium, Source Han Sans;
            }
          }

        }
      }
    }
    &.message, &.allMessage{
      transition: all .5s;
      .message-header-box{
        padding: 8px 24px;
        display: flex;
        justify-content: space-between;
        .left{
          display: flex;
          align-items: center;
          .notice{
            font-size: 20px;
            font-family: Source Han Sans-Bold, Source Han Sans;
            color: #363b4c;
          }
        }
        .right{
          display: flex;
          align-items: center;
          svg{
            cursor: pointer;
          }
          .line{
            width: 1px;
            height: 12px;
            background: #EEEFF0;
            margin: 0 8px;
          }
        }
      }
      .message-body{
        margin-top: 8px;
        padding-bottom: 16px;
        height: calc(100% - 76px);
        overflow: hidden;
        overflow-y: auto;
        .message-item{
          padding: 0 24px;
          .create-time{
            font-size: 12px;
            color: #6f7588;
            font-family: Source Han Sans-Regular, Source Han Sans;
            .delete-text{
              margin-left: 6px;
              font-size: 10px;
              cursor: pointer;
              display: none;
            }
          }
          .message-item-body{
            display: flex;
            padding: 8px 12px;
            background: #F5F6F7;
            border-radius: 6px;
            margin-top: 6px;
            transition: all .5s;
            .body-icon-box{
              width: 16px;
              height: 16px;
              margin-top: 2px;
              svg{
                width: 16px;
                height: 16px;
              }
            }
            .message-item-content{
              margin-left: 8px;
              position: relative;
              width: calc(100% - 24px);
              .message-title{
                width: calc(100% - 46px);
                font-size: 14px;
                color: #363b4c;
                cursor: pointer;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
                span{
                  font-family: Source Han Sans-Bold, Source Han Sans;
                }
              }
              .message-content{
                font-size: 12px;
                color: #575e73;
                font-family: Source Han Sans-Regular, Source Han Sans;
                .content-body{
                  p{
                    line-height: 28px !important;
                    display: flex;
                    flex-wrap: wrap;
                    grid-row-gap: 4px;
                  }
                }
              }
              .cell{
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
              }
              .cell-message{
                position: absolute;
                right: 0;
                top: 0;
                font-size: 12px;
                color: #363b4c;
                font-family: Source Han Sans-Regular, Source Han Sans;
                cursor: pointer;
                svg{
                  width: 12px;
                  height: 12px;
                }
              }
            }
          }
          &+.message-item{
            margin-top: 16px;
          }
          &:hover{
            .delete-text{
              display: inline;
            }
          }
        }
      }
    }
    &.message{
      width: 360px;
    }
    &.allMessage{
      width: 800px;
      .message-body{
        padding-bottom: 0;
        height: calc(100% - 106px);
      }
      .message-pagination{
        display: flex;
        align-items: center;
        justify-content: flex-end;
        font-size: 14px;
        font-family: Source Han Sans-Regular, Source Han Sans;
        color: #575e73;
        height: 20px;
        padding: 0 24px;
        margin-top: 10px;
        .icon{
          width: 18px;
          min-width: 18px;
          height: 18px;
          cursor: pointer;
          &.disabled{
            cursor: not-allowed;
          }
        }
        .el-input-number{
          min-width: 32px;
          max-width: 50px;
          height: 20px;
          .el-input-number__decrease, .el-input-number__increase{
            display: none;
          }
          .el-input{
            line-height: 20px;
            .el-input__inner{
              border: 0;
              background-color: #f5f6f7;
              padding: 0 4px;
              height: 20px;
              line-height: 20px;
            }
          }
        }
        .split{
          padding: 0 8px 3px;
        }
      }
    }
    &.usercenter, &.accountInfo{
      transition: all .5s;
    }
    &.usercenter{
      width: 360px;
      .user-info-body{
        height: 100%;
        display: flex;
        flex-direction: column;
        .user-info-box{
          height: 104px;
          padding: 24px;
          box-sizing: border-box;
          position: relative;
          display: flex;
          align-items: center;
          background: linear-gradient(180deg, #F4F8FF 5%, #fff 95%);
          .header-img {
            display: block;
            min-width: 56px;
            min-height: 56px;
            width: 56px;
            height: 56px;
            border-radius: 10px;
            overflow: hidden;
          }
          .info{
            margin-left: 16px;
            overflow: hidden;
            z-index: 1;
            .user-tenant-name{
              color: #363b4c;
              font-size: 16px;
              font-family: Source Han Sans-Bold, Source Han Sans;
              margin-bottom: 4px;
              text-overflow: ellipsis;
              white-space: nowrap;
              overflow: hidden;
            }
            .user-name{
              color: #363b4c;
              font-size: 14px;
              font-family: Source Han Sans-Regular, Source Han Sans;
              text-overflow: ellipsis;
              white-space: nowrap;
              overflow: hidden;
            }
          }
        }
        .seeting-logout-box{
          padding: 0 24px;
          margin-top: 8px;
          margin-bottom: 24px;
          display: flex;
          .button{
            display: flex;
            align-items: center;
            color: #363b4c;
            font-size: 14px;
            font-family: Source Han Sans-Regular, Source Han Sans;
            border-radius: 6px;
            box-sizing: border-box;
            cursor: pointer;
          }
        }
        .seeting-logout-box-col2{
          flex-wrap: wrap;
          .button{
            padding: 16px;
            width: 146px;
            height: 48px;
            justify-content: space-between;
            background-color: #f5f6f7;
            .userInfo-close{
              width: 16px;
              height: 16px;
              cursor: pointer;
              fill: #110F33;
            }
            &+.button{
              margin-left: 16px;
            }
          }
        }
        .seeting-logout-box-more{
          justify-content: space-between;
          .button{
            flex-direction: column-reverse;
            justify-content: center;
            padding: 8px;
            cursor: pointer;
            .userInfo-close{
              width: 20px;
              height: 20px;
              cursor: pointer;
              margin-bottom: 8px;
            }
            &:hover{
              background-color: #f5f6f7;
              color: #1D6BF5;
              svg{
                fill: #1D6BF5;
              }
            }
          }
        }
        .tenant-box{
          flex: 1;
          padding: 0 24px 24px;
          box-sizing: border-box;
          overflow: hidden;
          .tenant-title{
            color: #3d3d3d;
            font-size: 16px;
            font-family: Source Han Sans-Bold, Source Han Sans;
          }
          .tenant-body{
            margin-top: 8px;
            padding: 12px 0;
            position: relative;
            height: calc(100% - 22px);
            background: #F5F6F7;
            border-radius: 6px;
            box-sizing: border-box;
            .tenant-lists{
              height: 100%;
              overflow: hidden;
              overflow-y: auto;
              .tenant-item{
                padding: 12px 8px;
                margin: 0 8px;
                box-sizing: border-box;
                height: 64px;
                display: flex;
                border-radius: 6px;
                align-items: center;
                cursor: pointer;
                .tenant-img{
                  display: block;
                  min-width: 40px;
                  width: 40px;
                  height: 40px;
                  border-radius: 6px;
                  overflow: hidden;
                  margin-right: 16px;
                }
                .tenant-name{
                  font-family: Source Han Sans-Regular, Source Han Sans;
                  font-size: 14px;
                  color: #363b4c;
                  width: calc(100% - 76px);
                  text-overflow: ellipsis;
                  white-space: nowrap;
                  overflow: hidden;
                  margin-right: 4px;
                }
                .tenant-check{
                  width: 16px;
                  height: 16px;
                  .check-icon{
                    width: 16px;
                    height: 16px;
                  }
                }
                &.active{
                  background-color: #e4edff;
                }
                &:not(.active):hover{
                  background: #EEEFF0;
                }
              }
            }
          }
        }
      }
    }
    &.accountInfo{
      width: 800px;
    }
  }
}
</style>
