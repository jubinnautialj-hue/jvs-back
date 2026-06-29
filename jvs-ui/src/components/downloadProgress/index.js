import Vue from "vue";
import Notification from "./index.vue";

const NotificationConstructor = Vue.extend(Notification);

let notificationInstance = null;

const initInstance = () => {
  notificationInstance = new NotificationConstructor({
    el: document.createElement("div"),
  });
  document.body.appendChild(notificationInstance.$el);
};

const NotificationPlugin = {
  install(vue) {
    vue.prototype.$showProgress = (config) => {
      if (!notificationInstance) {
        initInstance();
      }
      notificationInstance.handleInitDownload(config);
    };
  },
};

export default NotificationPlugin;
