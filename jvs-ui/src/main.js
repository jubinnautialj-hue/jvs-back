import Vue from "vue";
import axios from "./router/axios";
import VueAxios from "vue-axios";
import App from "./App";
import ElementUI from "element-ui";
import cn_locale from "element-ui/lib/locale/lang/en";
import zh_locale from "element-ui/lib/locale/lang/zh-CN";
import "element-ui/lib/theme-chalk/index.css";
import "./permission"; // 权限
import "./error"; // 日志
import router from "./router/router";
import store from "./store";
import { loadStyle, useOptionChain } from "./util/util";
import * as urls from "@/config/env";
import { iconfontUrl, iconfontVersion } from "@/config/env";
import * as filters from "./filters"; // 全局filter
import JsonViewer from "vue-json-viewer";
import VueSignaturePad from "vue-signature-pad";
import draggable from "vuedraggable"; // 可拖拽容器
import VueClipboard from "vue-clipboard2";
import permissionMatch from "./util/permision";
import openUrl from "./util/url";
import downloadUrl from "./util/downloadUrl"; // 不打开链接，直接下载
import DownloadProgress from "@/components/downloadProgress/index"; // 文件下载进度窗口
import videoPreview from "./util/video";
import upgradeDialog from "./util/upgrade";
import loginForm from "./util/login";
import formula from "./util/formula";
import drawImamge from "./util/draw";
import getIconLib from "./plugin/iconLib/index";
import { ElementTiptapPlugin } from "element-tiptap";
import "element-tiptap/lib/index.css";
import "@/styles/themes/index.scss";
import "@/styles/newStyle.scss";
import "./styles/common.scss";
import "./styles/custom.scss";
import "./styles/resetAll.scss"; // fixme 统一表单表格样式，自定义需要注释此代码
import "font-awesome/css/font-awesome.min.css";
/**
 * 全局注册容器、组件
 * 不可删除，添加全局组件引用请修改index.js
 */
import "./components/index";

// 滚动栏
import vuescroll from "vuescroll"; //引入vuescroll
import "vuescroll/dist/vuescroll.css"; //引入vuescroll样式
// 注册封装代码显示器
import vueHljs from "vue-hljs";
import hljs from "highlight.js";
//if you want to use default color, import this css file
import "vue-hljs/dist/style.css";
import codeArea from "@/views/document/views/index/component/codeEditor";

import lang_zh_CN from "@/config/locales/zh_CN.js";
import lang_en from "@/config/locales/en.js";
import { getStore } from "@/util/store";
Vue.use(vueHljs, { hljs });
hljs.highlightAll();
Vue.component("codeArea", codeArea);
Vue.use(vuescroll); //使用

Vue.use(VueAxios, axios);

// if (/Android|webOS|iPhone|iPad|BlackBerry/i.test(navigator.userAgent)) {
//   // 这里配置你的H5页面地址
//   const jvs_session_uid = getCookie('jvs_session_uid')
//   const access_token =  store.getters.access_token
//   if (jvs_session_uid || access_token)  {
//     window.singleLoginRoute = setTimeout(() => {
//       const h5Url = `https://bi-zhjj.jnyjy.com/htmlDataSetId/h5/#/pages/tabBar/wel/index`;
//       window.location.href = h5Url;
//     }, 4000)
//   }
// }

// if (/Android|webOS|iPhone|iPad|BlackBerry/i.test(navigator.userAgent)) {
//   // 这里配置你的H5页面地址
//   const jvs_session_uid = getCookie('jvs_session_uid')
//   const access_token =  store.getters.access_token
//   if (jvs_session_uid || access_token)  {
//     document.cookie = "jvs_session_uid=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/";
//     localStorage.removeItem('jvs-access_token');
//   }
//   pollForCredentialsAndRedirect()
// }

// function pollForCredentialsAndRedirect() {
//   const interval = setInterval(() => {
//     const jvs_session_uid = getCookie('jvs_session_uid');
//     const access_token = store.getters.access_token;
//     if (jvs_session_uid && access_token) {
//       const h5Url = `https://bi-zhjj.jnyjy.com/htmlDataSetId/h5/index.html?jvs_session_uid=${jvs_session_uid}&access_token=${access_token}`;
//       clearInterval(interval);
//       window.location.href = h5Url;
//     }
//   }, 100);
// }

// 国际化
const language = navigator.language || navigator.userLanguage;
import VueI18n from "vue-i18n";
Vue.use(VueI18n);
// i18n 部分的配置，引入语言包，注意路径

let localLang =
  /zh/i.test(language) ||
  /zh-CN/i.test(language) ||
  /zh-TW/i.test(language) ||
  /zh-HK/i.test(language)
    ? "zh-CN"
    : "en";
const i18n = new VueI18n({
  // 默认语言
  locale: localLang,
  // 引入语言文件
  messages: {
    "zh-CN": lang_zh_CN,
    en: lang_en,
  },
});
Vue.prototype._i18n = i18n;
Vue.prototype.$langt = function (expression) {
  return i18n.t(expression);
};

Vue.use(ElementUI, {
  size: "medium",
  menuType: "text",
  locale: localLang != "en" ? zh_locale : cn_locale,
});
Vue.use(JsonViewer);
Vue.use(VueSignaturePad);
Vue.use(permissionMatch); // 权限
Vue.use(openUrl); // 打开链接 用于 预览、下载、打开地址
Vue.use(downloadUrl); // 不打开链接，直接下载
Vue.use(DownloadProgress); // 文件下载进度窗口
Vue.use(ElementTiptapPlugin, { lang: "zh" });

Vue.use(router);

// 注册全局容器
Vue.component("draggable", draggable);
Vue.use(VueClipboard);

Vue.prototype.$openLogin = loginForm.install;
Vue.prototype.$videoOpen = videoPreview.install;
Vue.prototype.$upgradeOpen = upgradeDialog.install;
Vue.prototype.$setFormula = formula.install;
Vue.prototype.$consoleImage = drawImamge.install;

// 加载相关url地址
Object.keys(urls).forEach((key) => {
  Vue.prototype[key] = urls[key];
});

//加载过滤器
Object.keys(filters).forEach((key) => {
  Vue.filter(key, filters[key]);
});

// 动态加载阿里云字体库
iconfontVersion.forEach((ele) => {
  loadStyle(iconfontUrl.replace("$key", ele));
});
// if(store.getters.access_token) {
//     getIconLib()
// }
if (window.self == window.top) {
  getIconLib();
}

Vue.config.productionTip = false;

var instance = axios.create({
  baseURL: "",
  timeout: 3000,
  headers: {
    "Content-Type": "multipart/form-data",
  },
});
Vue.config.productionTip = false;
Vue.prototype.instance = instance;
Vue.prototype.useOptionChain = useOptionChain;
Vue.prototype.$isNotEmpty = function (obj) {
  return obj !== undefined && obj !== null && obj !== "" && obj !== "null";
};
Vue.prototype.$getDefalut = function (obj, key, df) {
  return obj === undefined || key === undefined || !this.$isNotEmpty(obj[key])
    ? df
    : obj[key];
};

// 首页动态组件注册
const requireComponent = require.context(
  // 其组件目录的相对路径
  "../packages",
  // 是否查询其子目录
  true,
  // 匹配基础组件文件名的正则表达式
  /\.vue$/
);
requireComponent.keys().forEach((fileName) => {
  // 获取组件配置
  const componentConfig = requireComponent(fileName);
  // 全局注册组件
  Vue.component(
    componentConfig.default.name,
    // 如果这个组件选项是通过 `export default` 导出的，
    // 那么就会优先使用 `.default`，
    // 否则回退到使用模块的根。
    componentConfig.default || componentConfig
  );
});

new Vue({
  i18n,
  router,
  store,
  render: (h) => h(App),
  data: {
    eventBus: new Vue(),
  },
}).$mount("#app");

function getCookie(cname) {
  let name = cname + "=";
  let ca = document.cookie.split(";");
  for (let i = 0; i < ca.length; i++) {
    let c = ca[i].trim();
    if (c.indexOf(name) == 0) {
      return c.substring(name.length, c.length);
    }
  }
}
