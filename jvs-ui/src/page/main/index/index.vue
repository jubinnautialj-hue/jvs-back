<template>
  <div
    id="mainPage"
    :class="{
      'jvs-contail jvs-contail-font': true,
      'jvs--collapse': isCollapse,
      'jvs-contail-loading': !aleadyGeted,
      'only-app': justAppInfo,
      'match-loading': matchingLoading,
      mismatch: mismatchBool,
    }"
  >
    <!-- 顶部导航栏 -->
    <tags
      v-if="aleadyGeted"
      ref="tags"
      :freshAllMenuBool="freshAllMenuBool"
      :showBaseSetting="showBaseSetting"
      :jvsDesign="jvsDesign"
      :justAppInfo="justAppInfo"
      @changeTheme="changeTheme"
      @changeSystem="changeSystem"
      @openAppManage="openAppManage"
      @openAppSetting="openAppSetting"
      @openSystemSetting="openSystemSetting"
      @openWorkplace="openWorkplace"
      @openTemplate="openTemplate"
      @openCatalogue="openCatalogue"
      @openTemplateMore="openTemplateMore"
      @changeModeUser="changeModeUser"
      @mismatch="mismatch"
    />
    <div
      :class="{
        'jvs-layout': true,
        'jvs-layout-tempOpen':
          tempOpen || workPlaceOpen || systemSettingOpen || tempMoreOpen,
      }"
    >
      <!-- 顶部标签卡 -->
      <div
        class="jvs-left"
        :style="'width:' + $store.getters.theme.logo.width"
        v-if="!isProcessIndexRoute"
      >
        <!-- 左侧导航栏 -->
        <sidebar
          ref="sideBar"
          v-if="aleadyGeted"
          :menuType="menuType"
          :isCollapse="isCollapse"
          :systemId="systemId"
          :thisSystem="thisSystem"
          :freshSide="freshSide"
          :jvsDesign="jvsDesign"
          :changeModeUserRadom="changeModeUserRadom"
          :justAppInfo="justAppInfo"
          @openAppManage="openAppManageBySide"
          @openCatalogue="openCatalogue"
          @freshAllMenu="freshAllMenu"
          @closeOther="closeOther"
          @topNavChange="topNavChange"
        />
        <div
          v-if="dynaIndexLeft"
          class="jvs-left-dynal-index-left"
          :style="`top: ${$store.getters.theme.logo.height};height: calc(100vh - ${$store.getters.theme.logo.height});`"
        >
          <indexLeftMenu ref="indexLeftMenu"></indexLeftMenu>
        </div>
      </div>
      <div
        v-show="aleadyGeted"
        class="jvs-main"
        :style="
          isProcessIndexRoute
            ? 'top: ' +
              $store.getters.theme.logo.height +
              ';height:calc(100% - ' +
              $store.getters.theme.logo.height +
              '); width:100%;left:0;'
            : 'top: ' +
              $store.getters.theme.logo.height +
              ';height:calc(100% - ' +
              $store.getters.theme.logo.height +
              '); width:calc(100% - ' +
              $store.getters.theme.logo.width +
              ');left:' +
              $store.getters.theme.logo.width +
              ';'
        "
      >
        <div :class="{ 'jvs-main-loading': !alreadyLoad }"></div>
        <!-- 主体视图层 -->
        <el-scrollbar
          style="height: 100%"
          v-if="alreadyLoad && !catalogueItem && !showApplication"
        >
          <keep-alive>
            <router-view
              class="jvs-view"
              v-if="$route.meta.$keepAlive"
              :jvsDesign="jvsDesign"
              @openEvent="openEvent"
            />
          </keep-alive>
          <router-view
            class="jvs-view"
            v-if="!$route.meta.$keepAlive"
            :jvsDesign="jvsDesign"
            @openEvent="openEvent"
          />
        </el-scrollbar>

        <!-- 应用管理页 -->
        <div v-show="alreadyLoad && showApplication" style="height: 100%">
          <applicationManagePage
            ref="applicationManage"
            :systemId="systemId"
            :changeModeUserRadom="changeModeUserRadom"
            @freshAllMenu="freshAllMenu"
            @closeApp="closeApp"
          ></applicationManagePage>
        </div>

        <!-- 应用目录页 -->
        <div
          v-if="alreadyLoad && catalogueItem"
          class="catalogue-page"
          style="height: 100%"
        >
          <createPage
            :key="catalogueItem.id"
            :catalogueItem="catalogueItem"
            @freshAllMenu="freshAllMenu"
          ></createPage>
        </div>
      </div>

      <!-- 模板管理 -->
      <div
        v-if="tempOpen"
        class="template-content-box"
        :style="
          'top: ' +
          $store.getters.theme.logo.height +
          ';height:calc(100% - ' +
          $store.getters.theme.logo.height +
          ');'
        "
      >
        <templateManage
          :changeModeUserRadom="changeModeUserRadom"
          :jvsDesign="jvsDesign"
          @openSystem="openSystem"
          @openTemplate="openTemplate"
          @openTemplateMore="openTemplateMore"
        ></templateManage>
      </div>

      <!-- 更多模板 -->
      <div
        v-if="tempMoreOpen"
        class="template-content-box"
        :style="
          'top: ' +
          $store.getters.theme.logo.height +
          ';height:calc(100% - ' +
          $store.getters.theme.logo.height +
          ');'
        "
      >
        <template-more
          @openTemplate="openTemplate"
          :jvsDesign="jvsDesign"
        ></template-more>
      </div>

      <!-- 工作台 -->
      <div
        v-if="workPlaceOpen"
        class="template-content-box"
        :style="
          'top: ' +
          $store.getters.theme.logo.height +
          ';height:calc(100% - ' +
          $store.getters.theme.logo.height +
          ');'
        "
      >
        <workplace
          ref="workplace"
          :changeModeUserRadom="changeModeUserRadom"
        ></workplace>
      </div>

      <!-- 系统设置 -->
      <div
        v-if="systemSettingOpen"
        class="template-content-box"
        :style="
          'top: ' +
          $store.getters.theme.logo.height +
          ';height:calc(100% - ' +
          $store.getters.theme.logo.height +
          ');'
        "
      >
        <system-setting />
      </div>
    </div>
    <div class="jvs-shade" @click="showCollapse"></div>
    <el-dialog
      :title="bulletin.title + '公告'"
      :visible.sync="dialogVisible"
      width="800px"
      append-to-body
      :before-close="handleClose"
    >
      <div style="max-height: 800px; overflow-y: auto">
        <div>
          <img
            :src="bulletin.banner"
            alt=""
            style="display: block; width: 100%"
          />
        </div>
        <section v-html="bulletin.content"></section>
      </div>
    </el-dialog>
    <div class="dialog-box" v-if="imgVisible" @click="imgVisible = false">
      <img :src="bulletin.content" alt="" />
    </div>
  </div>
</template>

<script>
import { mapGetters } from "vuex";
import tags from "./tags";
import top from "./top/";
import sidebar from "./sidebar/";
import admin, { getRedirectInfo } from "@/util/admin";
import { validatenull } from "@/util/validate";
import { getStore } from "@/util/store.js";
import {
  getSystemHelpDict,
  getTenantInfo,
  getBulletinList,
  getDynamicDesign,
} from "@/api/admin/home";
import templateManage from "@/components/template/manage";
import createPage from "@/components/template/createPage";
import applicationManagePage from "@/components/template/applicationPage";
import { getDomain } from "@/api/login";
import Workplace from "@/page/main/workplace/workplace";
import SystemSetting from "@/views/upms/views/systemSetting/systemSetting";
import TemplateMore from "@/components/template/templateMore";
import indexLeftMenu from "../dynaIndex/indexLeftMenu";
import { JvsChat } from "jvs-ai-chat";
import "jvs-ai-chat/lib/style.css";
export default {
  components: {
    TemplateMore,
    Workplace,
    SystemSetting,
    top,
    tags,
    sidebar,
    templateManage,
    createPage,
    applicationManagePage,
    indexLeftMenu,
  },
  name: "index",
  data() {
    return {
      dialogVisible: false,
      imgVisible: false,
      bulletin: {},
      //刷新token锁
      refreshLock: false,
      //刷新token的时间
      refreshTime: "",
      systemId: "-1",
      alreadyLoad: false,
      thisSystem: null,
      tempOpen: false,
      tempMoreOpen: false,
      menuType: "platform",
      workPlaceOpen: false,
      systemSettingOpen: false,
      catalogueItem: null,
      freshAllMenuBool: -1, // 是否需要重新拉取所有菜单
      freshSide: -1, // 刷新左侧菜单栏
      showApplication: false,
      showBaseSetting: -1,
      aleadyGeted: false,
      jvsDesign: null,
      changeModeUserRadom: -1,
      changedNoApp: false,
      justAppInfo: false,
      matchingLoading: false,
      mismatchBool: false,
      modeUserInfo: null,
    };
  },
  async created() {
    if (sessionStorage.getItem("justAppInfo") == "justAppInfo") {
      this.justAppInfo = true;
    }
    this.modeUserInfo = getStore({ name: "modeUserInfo" });
    this.getDynamicDesign();
    let stopInit = false;
    if (this.$route.query.login === "isLogin") {
      this.getBulletinList();
      if (this.$store.getters.userInfo && this.$permissionMatch("jvs_app")) {
        this.openTemplate(true);
      }
    } else if (this.$route.hash.includes("jvsAppId")) {
      this.$store.commit("SET_APP_SETTING_OPEN", false);
      this.$route.hash
        .split("?")[1]
        .split("&")
        .filter((hit) => {
          if (hit.includes("jvsAppId")) {
            let sid = hit.split("=")[1];
            this.$store.commit("SET_SYSTEM", sid);
            this.changeSystem(sid);
          }
        });
    } else if (this.$route.query && this.$route.query.jvsAppId) {
      let redirecInfo = {
        stop: false,
        url: "",
      };
      await getRedirectInfo().then((res) => {
        redirecInfo = res;
      });
      if (redirecInfo.stop && redirecInfo.url) {
        stopInit = true;
        this.$openUrl(redirecInfo.url, "_self");
        return false;
      }
      if (!stopInit) {
        this.matchingLoading = true;
        this.justAppInfo = true;
        sessionStorage.setItem("justAppInfo", "justAppInfo");
        this.$store.commit("SET_SYSTEM", this.$route.query.jvsAppId);
        this.$store.commit("SET_APP_SETTING_OPEN", false);
        this.changeSystem(this.$route.query.jvsAppId);
        this.openAppManage(this.$route.query.jvsAppId);
      }
    } else {
      if (
        this.$permissionMatch("jvs_base")
          ? true
          : this.$permissionMatch("jvs_platform")
      ) {
        this.showBaseSetting = this.$permissionMatch("jvs_base")
          ? Math.random() + 1
          : Math.random();
      }
    }
    if (stopInit) return false;
    //实时检测刷新token
    // this.refreshToken()
    if (!getStore({ name: "tenantInfo" })) {
      getTenantInfo()
        .then((res) => {
          if (res.data.code == 0) {
            this.$store.commit("SET_TENANTINFO", res.data.data);
            this.$emit("changeTheme", true);
            this.getDomainHandle(res.data.data);
          }
          this.alreadyLoad = true;
        })
        .catch((e) => {
          this.alreadyLoad = true;
        });
    } else {
      this.getDomainHandle(getStore({ name: "tenantInfo" }));
      this.alreadyLoad = true;
    }
    getSystemHelpDict("jvs-ui-help-url").then((res) => {
      if (res.data && res.data.code == 0) {
        this.$store.commit("SET_SYSTEM_HELP_DICT", res.data.data);
      }
    });
  },
  destroyed() {
    clearInterval(this.refreshTime);
  },
  mounted() {
    this.$nextTick(() => {
      if (this.$permissionMatch("ai")) {
        // 使用插件
        const plugin = new JvsChat({
          draggable: false,
          title: "AI助理",
          requestConfig: {
            hostname: "",
            path: ``,
            headers: {
              Authorization: "Bearer " + this.$store.getters.access_token,
              "Content-Type": "application/json",
            },
            userId: "",
            sceneConfig: {
              name: "",
              permission: "",
            },
          },
          client_id: "",
          sceneIdentification: "Lowcode", // 页面场景标识,用于加载对应页面支持的功能
        });
        plugin.chatInit(document.getElementById("mainPage"));
        // 监听插件抛出的事件
        window.addEventListener("messageFinish", (event) => {
          console.log(event);
        });
      }
    });
    this.init();
    // 登陆后直接打开轻应用
    if (this.$route.query.backlink) {
      let vals = decodeURIComponent(this.$route.query.backlink).split("/");
      if (vals && vals.length > 1 && vals[0] == "jvs") {
        if (vals[1] == "application" && vals.length > 2) {
          this.$refs.tags.entrySystem({ id: vals[2] });
          this.openAppManage(vals[2], false);
          this.changeSystem(vals[2]);
        }
      }
    }
  },
  computed: {
    ...mapGetters([
      "userInfo",
      "isLock",
      "isCollapse",
      "website",
      "expires_in",
      "menuAll",
      "dynaIndexLeft",
    ]),
    isProcessIndexRoute() {
      // 判断当前路由是否为 /process/index
      return this.$route.path === "/process/index";
    },
  },
  props: [],
  methods: {
    getBulletinList() {
      getBulletinList().then((res) => {
        this.bulletin = res.data.data || {};
        if (this.bulletin.content && this.bulletin.content.length > 0) {
          this.bulletin.content = this.bulletin.content.replace(
            "<img ",
            `<img style="max-width: 710px;"`
          );
          if (this.bulletin.contentType === "TEXT") {
            this.dialogVisible = true;
          } else {
            this.imgVisible = true;
          }
        }
      });
    },
    handleClose() {
      this.dialogVisible = false;
    },
    showCollapse() {
      this.$store.commit("SET_COLLAPSE");
    },
    // 屏幕检测
    init() {
      this.$store.commit("SET_SCREEN", admin.getScreen());
      window.onresize = () => {
        setTimeout(() => {
          this.$store.commit("SET_SCREEN", admin.getScreen());
        }, 0);
      };
    },
    // 实时检测刷新token
    refreshToken() {
      this.refreshTime = setInterval(() => {
        const token = getStore({
          name: "access_token",
          debug: true,
        });

        if (validatenull(token)) {
          return;
        }

        if (this.expires_in <= 1000 && !this.refreshLock) {
          this.refreshLock = true;
          this.$store.dispatch("RefreshToken").catch(() => {
            clearInterval(this.refreshTime);
          });
          this.refreshLock = false;
        }
        this.$store.commit("SET_EXPIRES_IN", this.expires_in - 10);
      }, 10000);
    },
    // 通知改变主题
    changeTheme(bool) {
      this.$emit("changeTheme", bool);
    },
    // 切换系统
    changeSystem(id) {
      // console.log('changeSystem')
      this.systemId = id;
      this.freshSide = Math.random();
      if (!this.justAppInfo) {
        this.catalogueItem = null;
      }
      /**
       * 每次切换应用
       * 1.都关掉管理界面
       * 2.都切换回首页（暂时不用，目前哪怕同一个系统里每次切换页签也会触发changeSystem）
       */
      this.showApplication = false; // 关掉管理界面
      // this.$router.push({ path: '/wel/index' })// 切换回首页
    },
    // 打开应用创建页
    openAppManage(id, bool) {
      // console.log('openAppManage')
      if (this.modeUserInfo && this.modeUserInfo.mode == "DEV") {
        this.openCatalogue({ id: id, extend: { jvsAppId: id, id: id } });
      } else {
        this.openAppManageBySide(id);
      }
    },
    // 打开应用管理
    openAppManageBySide(id, bool) {
      console.log('openAppManageBySide')
      if (bool === false) {
        this.showApplication = false;
        return false;
      }
      this.showApplication = this.$permissionMatch("jvs_app");
      if (id != -1 && this.$refs.applicationManage) {
        this.$refs.applicationManage.init(id);
      }
    },
    // 打开系统  应用
    openSystem(id) {
      if (id) {
        this.$refs.tags.entrySystem({ id: id });
      }
      this.changeSystem(id);
      this.openAppManage(id);
    },
    // 打开应用管理
    openTemplate(bool, type) {
      this.tempOpen = bool;
      if (bool) {
        this.showApplication = false;
        this.workPlaceOpen = false;
        this.systemSettingOpen = false;
        this.tempMoreOpen = false;
      }
      this.changedNoApp = false;
      // 切换模式后应用菜单为空，左侧应显示平台管理
      if (type == "changeModeUser") {
        this.changedNoApp = true;
      }
      if (this.$refs.indexLeftMenu) {
        this.$refs.indexLeftMenu.initStore();
      }
    },
    // 打开更多模板
    openTemplateMore(bool) {
      this.tempMoreOpen = bool;
      if (bool) {
        this.showApplication = false;
        this.workPlaceOpen = false;
        this.systemSettingOpen = false;
        this.tempOpen = false;
      }
    },
    // 打开应用设置
    openAppSetting(bool, type) {
      this.$store.commit("SET_APP_SETTING_OPEN", bool);
      this.$store.commit("SET_MENU_TYPE", type);
      if (bool && ["system", "platform"].indexOf(type) > -1) {
        this.showApplication = false;
      }
      if (this.$refs.indexLeftMenu) {
        this.$refs.indexLeftMenu.initStore();
      }
    },
    // 打开系统设置
    openSystemSetting(bool) {
      this.systemSettingOpen = bool;
      if (bool) {
        this.$store.commit(
          "SET_APP_SETTING_OPEN",
          this.tempOpen ? false : getStore({ name: "appSettingOpen" })
        );
        this.showApplication = false;
        this.tempOpen = false;
        this.tempMoreOpen = false;
        this.workPlaceOpen = false;
      }
    },
    // 打开工作台
    openWorkplace(bool) {
      this.workPlaceOpen = bool;
      if (bool) {
        this.$nextTick(() => {
          this.$refs.workplace.init();
        });
        this.$store.commit(
          "SET_APP_SETTING_OPEN",
          this.tempOpen ? false : getStore({ name: "appSettingOpen" })
        );
        this.showApplication = false;
        this.tempOpen = false;
        this.tempMoreOpen = false;
        this.systemSettingOpen = false;
      }
    },
    // 打开应用的目录
    openCatalogue(item) {
      this.catalogueItem = item;
      this.showApplication = false;
    },
    // 刷新所有菜单
    freshAllMenu(bool) {
      if (bool) {
        this.freshAllMenuBool = Math.random();
      }
    },
    // 点击了logo关闭应用相关
    closeOther(bool) {
      if (bool) {
        // 点击logo，应用菜单为空时显示平台管理
        this.$store.commit(
          "SET_APP_SETTING_OPEN",
          this.changedNoApp
            ? true
            : this.tempOpen
            ? false
            : getStore({ name: "appSettingOpen" })
        );
        this.catalogueItem = null;
        this.showApplication = false;
        this.tempOpen = false;
        this.tempMoreOpen = false;
        this.workPlaceOpen = false;
        this.systemSettingOpen = false;
      }
    },
    // 获取域名对应设置信息
    getDomainHandle(data) {
      getDomain().then((res) => {
        if (res.data && res.data.code == 0) {
          this.thisSystem = data;
          if (res.data.data) {
            if (res.data.data.logo) {
              this.$set(this.thisSystem, "logo", res.data.data.logo);
            }
            if (res.data.data.icon) {
              var link = document.createElement("link");
              link.type = "image/x-icon";
              link.rel = "shortcut icon";
              link.href = res.data.data.icon;
              document.getElementsByTagName("head")[0].appendChild(link);
              this.$set(this.thisSystem, "icon", res.data.data.icon);
            }
            if (res.data.data.bgImg) {
              this.$set(this.thisSystem, "bgImg", res.data.data.bgImg);
            }
            console.log("load here");
            if (res.data.data.kkfileUrl) {
              this.$store.commit("SET_KKFILE_URL", res.data.data.kkfileUrl);
            }
            this.$store.commit("SET_TENANTINFO", this.thisSystem);
          }
        }
      });
    },
    getDynamicDesign() {
      getDynamicDesign()
        .then((res) => {
          if (res.data && res.data.code == 0) {
            this.aleadyGeted = true;
            this.jvsDesign = res.data.data;
            let tmu = getStore({ name: "modeUserInfo" });
            if (!tmu) {
              let obj = {
                mode: this.jvsDesign.JVS_DESIGN_DEFAULT_MODE || "GA",
              };
              this.$store.commit("SET_MODEUSER_INFO", obj);
            }
          }
        })
        .catch((e) => {
          this.aleadyGeted = true;
        });
    },
    closeApp(bool) {
      if (bool) {
        this.tempOpen = true;
        this.showApplication = false;
        this.tempMoreOpen = false;
        this.catalogueItem = null;
        this.systemId = "";
        this.freshAllMenu(true);
        this.$forceUpdate();
      }
    },
    changeModeUser() {
      this.changeModeUserRadom = Math.random();
      this.modeUserInfo = getStore({ name: "modeUserInfo" });
    },
    openEvent(data) {
      if (typeof data == "string") {
        switch (data) {
          case "message":
            if (this.$refs["tags"]) {
              this.$refs["tags"].openDrawer("message");
            }
            break;
          case "workplace":
            if (this.$refs["tags"]) {
              this.$refs["tags"].openWorkplace();
            }
            break;
          default:
            break;
        }
      }
    },
    mismatch(bool) {
      this.mismatchBool = bool;
      this.matchingLoading = false;
    },
    topNavChange(id) {
      this.$refs.tags.entrySystem({ id: id }, true);
    },
  },
};
</script>
<style lang="scss" scoped>
.jvs-contail-loading {
  width: 100%;
  height: 100%;
  background-image: url("../../../../public/jvs-ui-public/img/loading.gif");
  background-position: center;
  background-repeat: no-repeat;
  background-size: auto;
  background-color: #fff;
}
.jvs-main-loading {
  position: absolute;
  width: 100%;
  height: 100%;
  background-image: url("../../../../public/jvs-ui-public/img/loading.gif");
  background-position: center;
  background-repeat: no-repeat;
  //background-size: 300px 240px;
}
/deep/ .jvs-layout-tempOpen {
  .el-menu-scrollbar {
    display: none;
  }
  .jvs-main {
    display: none;
  }
  .template-content-box {
    position: absolute;
    width: 100%;
    z-index: 1025;
  }
}
.dialog-box/deep/ {
  position: fixed;
  top: 0;
  left: 0;
  height: 100vh;
  width: 100vw;
  z-index: 9999;
  text-align: center;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 6px;
  img {
    max-width: 800px;
    min-width: 400px;
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translateY(-50%) translateX(-50%);
    z-index: 10000;
  }
}
.jvs-contail.only-app {
  .jvs-tags {
    display: none;
  }
  .jvs-layout {
    .jvs-left {
      /deep/.jvs-logo {
        display: none;
      }
      /deep/.el-menu-scrollbar {
        height: 100% !important;
        margin-top: 0 !important;
      }
    }
    .jvs-main {
      top: 0 !important;
      height: 100% !important;
    }
  }
  &.match-loading {
    background-image: url("../../../../public/jvs-ui-public/img/loading.gif");
    background-position: center;
    background-repeat: no-repeat;
    background-size: auto;
    background-color: #fff;
    .jvs-layout {
      visibility: hidden;
    }
  }
  &.mismatch {
    background-image: url("../../../const/img/emptyImage.svg");
    background-repeat: no-repeat;
    background-position: center;
    background-size: 260px 123px;
    .jvs-layout {
      visibility: hidden;
    }
    &::after {
      content: "没有操作权限";
      line-height: 30px;
      color: #909399;
      font-size: 12px;
      text-align: center;
      display: block;
      width: 100%;
      position: absolute;
      top: calc(50% + 70px);
    }
  }
}
.jvs-left-dynal-index-left {
  position: absolute;
  width: 100%;
  left: 0;
  z-index: 2;
  /deep/.dynaIndex-left-menu {
    .left-menu-top {
      .left-menu-title {
        background: linear-gradient(
          179deg,
          rgba(30, 111, 255, 0.05) 0%,
          rgba(30, 111, 255, 0) 100%
        );
      }
    }
  }
}
</style>
