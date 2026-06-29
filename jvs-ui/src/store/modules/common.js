import { getStore, removeStore, setStore } from "@/util/store";
import website from "@/const/website";
const params = {
  themeColor: '', // 主题颜色
  activeColor: '', // 激活状态颜色
  // 字体
  font: {
    size: '', // 大小
    color: '', // 颜色
  },
  // logo设置
  logo: {
    width: '240px', // 宽
    height: '64px', // 高
    fit: 'contain', // 图片显示填充方式
    color: '#1890ff', // 字体颜色
    fontSize: '16px', // 字体大小
    fontWeight: 400, // 字体粗细
    backgroundColor: '#fff', // 背景颜色
  },
  // 表单设置
  form: {
    size: 'mini', // 表单内组件的尺寸  medium / small / mini
  },
  btn: {
    size: 'mini', // 表单内组件的尺寸  medium / small / mini
  },
  table: {
    size: 'small', // 表单内组件的尺寸  medium / small / mini
  }
}
const common = {
  state: {
    isCollapse: false,
    isFullScreen: false,
    isShade: false,
    screen: -1,
    isLock: getStore({ name: "isLock" }) || false,
    showTag: true,
    showCollapse: true,
    showFullScren: true,
    website: website,
    tenantId: getStore({ name: "tenantId" }) || "",
    theme: getStore({name: 'theme'}) || Object.assign({}, params),
    themeName: getStore({name: 'themeName'}) || "",
    system: getStore({name: 'system'}),
    template: getStore({name: 'template'}) || "",
    tenantInfo: getStore({name: 'tenantInfo'}),
    MobileWidth: getStore({ name: 'MobileWidth' }),
    systemHelpDict: getStore({name: 'systemHelpDict'}) || [],
    autoOpenedDict: getStore({name: 'autoOpenedDict'}) || [],
    appSettingOpen: getStore({name: 'appSettingOpen'}) || false,
    menuType: getStore({name: 'menuType'}) || '',
    functionsObj: getStore({name: 'functionsObj'}) || {},
    modeUserInfo: getStore({name: 'modeUserInfo'} || {}),
    dynaIndexLeft: false,
    dynaIndexLeftList: getStore({name: 'dynaIndexLeftList'}),
    dynaIndexLeftData: getStore({name: 'dynaIndexLeftData'}),
    dynaIndexLeftComList: getStore({name: 'dynaIndexLeftComList'})
  },
  actions: {},
  mutations: {
    SET_SHADE: (state, active) => {
      state.isShade = active;
    },
    SET_COLLAPSE: state => {
      state.isCollapse = !state.isCollapse;
    },
    SET_FULLSCREN: state => {
      state.isFullScreen = !state.isFullScreen;
    },
    SET_SHOWCOLLAPSE: (state, active) => {
      state.showCollapse = active;
      setStore({
        name: "showCollapse",
        content: state.showCollapse
      });
    },
    SET_SHOWTAG: (state, active) => {
      state.showTag = active;
      setStore({
        name: "showTag",
        content: state.showTag
      });
    },
    SET_SHOWMENU: (state, active) => {
      state.showMenu = active;
      setStore({
        name: "showMenu",
        content: state.showMenu
      });
    },
    SET_SHOWLOCK: (state, active) => {
      state.showLock = active;
      setStore({
        name: "showLock",
        content: state.showLock
      });
    },
    SET_SHOWSEARCH: (state, active) => {
      state.showSearch = active;
      setStore({
        name: "showSearch",
        content: state.showSearch
      });
    },
    SET_SHOWFULLSCREN: (state, active) => {
      state.showFullScren = active;
      setStore({
        name: "showFullScren",
        content: state.showFullScren
      });
    },
    SET_SHOWDEBUG: (state, active) => {
      state.showDebug = active;
      setStore({
        name: "showDebug",
        content: state.showDebug
      });
    },
    SET_SHOWTHEME: (state, active) => {
      state.showTheme = active;
      setStore({
        name: "showTheme",
        content: state.showTheme
      });
    },
    SET_SHOWCOLOR: (state, active) => {
      state.showColor = active;
      setStore({
        name: "showColor",
        content: state.showColor
      });
    },
    SET_LOCK: state => {
      state.isLock = true;
      setStore({
        name: "isLock",
        content: state.isLock,
        type: "session"
      });
    },
    SET_SCREEN: (state, screen) => {
      state.screen = screen;
    },
    SET_THEME: (state, theme) => {
      state.theme = theme;
      setStore({
        name: "theme",
        content: state.theme,
        type: "session"
      });
    },
    SET_THEME_NAME: (state, themeName) => {
      state.themeName = themeName;
      setStore({
        name: "themeName",
        content: state.themeName,
        type: "session"
      });
    },
    SET_LOCK_PASSWD: (state, lockPasswd) => {
      state.lockPasswd = lockPasswd;
      setStore({
        name: "lockPasswd",
        content: state.lockPasswd,
        type: "session"
      });
    },
    SET_TENANTId: (state, tenantId) => {
      state.tenantId = tenantId;
      setStore({
        name: "tenantId",
        content: state.tenantId,
        type: "session"
      });
      setStore({
        name: "tenantId",
        content: state.tenantId,
        type: ""
      });
    },
    CLEAR_LOCK: state => {
      state.isLock = false;
      state.lockPasswd = "";
      removeStore({
        name: "lockPasswd"
      });
      removeStore({
        name: "isLock"
      });
    },
    SET_SYSTEM: (state, system) => {
      state.system = system;
      setStore({
        name: "system",
        content: state.system,
        type: "session"
      });
    },
    SET_TEMPLATE: (state, template) => {
      state.template = template;
      setStore({
        name: "template",
        content: state.template,
        type: "session"
      });
    },
    SET_TENANTINFO: (state, info) => {
      state.tenantInfo = info;
      setStore({
        name: "tenantInfo",
        content: state.tenantInfo,
        type: "session"
      });
      if(info.systemName) {
        document.title = info.systemName
      }
    },
    SET_SYSTEM_HELP_DICT: (state, info) => {
      state.systemHelpDict = info;
      setStore({
        name: "systemHelpDict",
        content: state.systemHelpDict,
        type: "session"
      });
    },
    SET_AUTO_OPENED_DICT: (state, info) => {
      state.autoOpenedDict = info;
      setStore({
        name: "autoOpenedDict",
        content: state.autoOpenedDict,
        type: "session"
      });
    },
    SET_APP_SETTING_OPEN: (state, info) => {
      state.appSettingOpen = info;
      setStore({
        name: "appSettingOpen",
        content: state.appSettingOpen,
        type: "session"
      });
    },
    SET_MENU_TYPE: (state, info) => {
      state.menuType = info;
      setStore({
        name: "menuType",
        content: state.menuType,
        type: "session"
      });
    },
    set_MobileWidth (state, data) {
      state.MobileWidth = data
      setStore({
        name: 'MobileWidth',
        content: state.MobileWidth,
        type: 'session'
      })
    },
    SET_FUNCTIONS_OBJ (state, data) {
      state.functionsObj = data
      setStore({
        name: 'functionsObj',
        content: state.functionsObj,
        type: 'session'
      })
    },
    SET_KKFILE_URL (state, kkfileUrl) {
      state.kkfileUrl = kkfileUrl
      setStore({
        name: 'kkfileUrl',
        content: state.kkfileUrl,
        type: 'session'
      })
    },
    SET_MODEUSER_INFO (state, data) {
      state.modeUserInfo = data
      setStore({
        name: 'modeUserInfo',
        content: state.modeUserInfo
      })
    },
    SET_DYNAINDEXLEFT: (state, bool) => {
      state.dynaIndexLeft = bool;
    },
    SET_DYNAINDEXLEFTLIST: (state, data) => {
      state.dynaIndexLeftList = data;
      setStore({
        name: 'dynaIndexLeftList',
        content: state.dynaIndexLeftList
      })
    },
    SET_DYNAINDEXLEFTDATA: (state, data) => {
      state.dynaIndexLeftData = data
      setStore({
        name: 'dynaIndexLeftData',
        content: state.dynaIndexLeftData
      })
    },
    SET_DYNAINDEXLEFTCOMLIST: (state, data) => {
      state.dynaIndexLeftComList = data
      setStore({
        name: 'dynaIndexLeftComList',
        content: state.dynaIndexLeftComList
      })
    }
  }
};
export default common;
