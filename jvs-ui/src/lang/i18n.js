import Vue from "vue";
import en from "./en";
import zhCN from "./zh-cn";
import enLocale from "element-ui/lib/locale/lang/en";
import zhLocale from "element-ui/lib/locale/lang/zh-CN";
// 国际化
import VueI18n from "vue-i18n";
Vue.use(VueI18n);

//将数据配置好
const messages = {
  "zh-cn": Object.assign(zhCN, zhLocale),
  en: Object.assign(en, enLocale),
};
const i18n = new VueI18n({
  legacy: false,
  locale: "zh-cn",
  fallbackLocale: "zh-cn",
  globalInjection: true,
  missingWarn: false,
  fallbackWarn: false,
  messages,
  silentTranslationWarn: true, // 去除警告
});

// 这个设置是关键的，其他教程有各种写法，但是这个版本下，这个可以
Vue.prototype.$hit = function (expression) {
  return i18n.t(expression);
};
export default i18n;

export const langList = [
  {
    label: "简体中文",
    value: "zh-cn",
  },
  {
    label: "English",
    value: "en",
  },
];
