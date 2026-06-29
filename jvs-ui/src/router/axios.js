import { serialize, noEmptyOfObject } from "@/util/util";
import NProgress from "nprogress"; // progress bar
import errorCode from "@/const/errorCode";
import { authCode, authErrorCode } from "@/const/errorCode";
import router from "@/router/router";
import { Notification } from "element-ui";
import "nprogress/nprogress.css";
import store from "@/store"; // progress bar style
import { decryption } from "@/util/util";
import { deCodeKey } from "@/const/const";
import upgradeDialog from "@/util/upgrade";
axios.defaults.timeout = 60000;
// 返回其他状态吗
axios.defaults.validateStatus = function (status) {
  return status >= 200 && status <= 500; // 默认的
};
// 跨域请求，允许保存cookie
axios.defaults.withCredentials = true;
// NProgress Configuration
NProgress.configure({
  showSpinner: false,
});
let countTime = 0;
const freshTokenUrl = "/auth/oauth2/token";
let currentRoutePath = ""; // 当前路由
let lastUrl = "";
const whiteApi = ["/auth/token/logout"];

// HTTPrequest拦截
axios.interceptors.request.use(
  (config) => {
    // NProgress.start(); // start progress bar
    const isToken = (config.headers || {}).isToken === false;
    let token = store.getters.access_token;
    let tenantId = store.getters.tenantId;
    if (token && !isToken) {
      config.headers["Authorization"] = "Bearer " + token;
    }
    // 添加请求头 固定参数
    config.headers["jvs-rule-ua"] = encodeURI("2.3");
    // headers中配置serialize为true开启序列化
    if (config.method === "post" && !config.data) {
      config.data = {};
    }
    if (config.method === "post" && config.headers.serialize) {
      config.data = serialize(config.data);
      delete config.data.serialize;
    }
    // 去除空值参数
    if (config.params) {
      config.params = noEmptyOfObject(config.params);
    }
    if (config.data && config.headers["type"] != "FormData") {
      config.data = noEmptyOfObject(config.data);
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// HTTPresponse拦截
axios.interceptors.response.use(
  (res) => {
    NProgress.done();
    const status = Number(res.status) || 200;
    const message = res.data.msg || errorCode[status] || errorCode["default"];
    if (router.currentRoute.path !== currentRoutePath) {
      currentRoutePath = router.currentRoute.path;
      lastUrl = location.href;
      window.parent.postMessage({ command: "isIndex" }, "*");
    }

    if (status === 401) {
      store.dispatch("FedLogOut").then(() => {
        sessionStorage.setItem("lastUrl", lastUrl);
        router.push({ path: "/login" });
      });
      return;
    }

    if (status === 404) {
      return Promise.reject(new Error(message));
    }

    if (status !== 200 || res.data.code === 1) {
      if (res.config.url == freshTokenUrl) {
        if (res.config.params && !res.config.params.switch) {
          if (res.config.params.refresh_token) {
            Notification.error({
              title: "提示",
              message: message,
              position: "bottom-right",
              dangerouslyUseHTMLString: true,
            });
          }
        }
      } else {
        Notification.error({
          title: "提示",
          message: message,
          position: "bottom-right",
          dangerouslyUseHTMLString: true,
        });
      }
      return Promise.reject(new Error(message));
    } else {
      if (res.data.code == -3) {
        sessionStorage.setItem("lastUrl", lastUrl);
        if (
          !localStorage.getItem("hadRefresh") ||
          localStorage.getItem("hadRefresh") == "" ||
          new Date().getTime() > localStorage.getItem("hadRefresh")
        ) {
          localStorage.setItem("hadRefresh", new Date().getTime());
          window.parent.postMessage({ command: "fresh" }, "*");
        } else {
          store.dispatch("LogOut").then(() => {
            sessionStorage.setItem("lastUrl", lastUrl);
            router.push({ path: "/login" });
            window.parent.postMessage({ command: "loginOut" }, "*");
          });
        }
      }
    }
    if (res.data && res.data.code === 20000) {
      upgradeDialog.install({
        message: res.data.msg,
      });
    }
    if (res.data && res.data.code === -2) {
      if (res.config && whiteApi.indexOf(res.config.url) > -1) {
        store.dispatch("LogOut").then(() => {
          sessionStorage.setItem("lastUrl", lastUrl);
          router.push({ path: "/login" });
          window.parent.postMessage({ command: "loginOut" }, "*");
        });
      } else {
        if (countTime == 0) {
          sessionStorage.setItem("lastUrl", lastUrl);
          store
            .dispatch("RefreshToken", store.getters.tenantId)
            .then((res) => {
              if (res && res.access_token) {
                location.reload();
              } else {
                store.dispatch("LogOut").then(() => {
                  sessionStorage.setItem("lastUrl", lastUrl);
                  router.push({ path: "/login" });
                  window.parent.postMessage({ command: "loginOut" }, "*");
                });
              }
            })
            .catch((e) => {
              store.dispatch("LogOut").then(() => {
                sessionStorage.setItem("lastUrl", lastUrl);
                router.push({ path: "/login" });
                window.parent.postMessage({ command: "loginOut" }, "*");
              });
            });
        }
        countTime += 1;
      }
      return res;
    } else {
      if (
        res.config &&
        res.config.url === "/mgr/jvs-design/base/use/menu" &&
        res.data &&
        res.data.code != 0
      ) {
        store.dispatch("LogOut").then(() => {
          sessionStorage.setItem("lastUrl", lastUrl);
          router.push({ path: "/login" });
          window.parent.postMessage({ command: "loginOut" }, "*");
        });
      } else {
        if (res.data && res.data.code == -1) {
          Notification.error({
            title: "提示",
            message: message,
            position: "bottom-right",
            dangerouslyUseHTMLString: true,
          });
          return Promise.reject(new Error(message));
          // 如果授权码需要返回请在请求体上加上isReturn:true
        } else if (
          authErrorCode.indexOf(res.data.code + "") != -1 &&
          !res.config.isReturn
        ) {
          const authMessage = res.data.msg || authCode[res.data.code + ""];
          Notification.error({
            title: "提示",
            message: authMessage,
            position: "bottom-right",
            dangerouslyUseHTMLString: true,
          });
          return Promise.reject(new Error(authMessage));
        } else {
          if (
            res.config &&
            res.config.url &&
            res.config.url.startsWith("/mgr") &&
            typeof res.data.data == "string" &&
            !res.config.url.includes("uploadPart")
          ) {
            if (res.headers && res.headers.decode == "true") {
              let tp = {
                data: res.data.data,
              };
              let temp = decryption({
                data: tp,
                key: deCodeKey,
                param: ["data"],
              });
              res.data.data = JSON.parse(temp.data);
            }
          }
          if (res.config.url == freshTokenUrl) {
            countTime = 0;
            if (res.data) {
              if (typeof res.data.userDto == "string") {
                let tp = JSON.parse(JSON.stringify(res.data));
                let temp = decryption({
                  data: tp,
                  key: deCodeKey,
                  param: ["userDto"],
                });
                res.data.userDto = JSON.parse(temp.userDto);
              }
            }
          }
          if (res.config.url == "/auth/token") {
            if (res.data && res.data.code == 0 && res.data.data) {
              if (
                res.data.data.userDto &&
                typeof res.data.data.userDto == "string"
              ) {
                let tp = JSON.parse(JSON.stringify(res.data.data));
                let temp = decryption({
                  data: tp,
                  key: deCodeKey,
                  param: ["userDto"],
                });
                res.data.data.userDto = JSON.parse(temp.userDto);
              }
            }
          }
          return res;
        }
      }
    }
  },
  (error) => {
    NProgress.done();
    if (axios.isCancel(error)) {
      return new Promise(() => {});
    }
    return Promise.reject(new Error(error));
    // return Promise.reject(new Error("fail")).then(resolved, rejected);
  }
);

export default axios;
