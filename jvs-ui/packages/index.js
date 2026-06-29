import gridLayout from "./gridLayout"
import homePage from "./homePage"
import gooeyMenu from "./gooeyMenu"
import customDialog from './customDialog'
import baseConfig from './baseConfig'
import MaterialSelector from './materialSelector'
import cardMaterialSelector from './cardMaterialSelector'
import backgroundSelector from './backgroundSelector'
import standardColorPicker from './standardColorPicker'
import backgroundFilterSelector from './backgroundFilterSelector'
import positionSelector from './positionSelector'
import FontSelector from './fontSelector'
import StandardForm from './standardForm'
import ComponentForm from './componentForm'
import CustomForm from './customForm'
import globalConfig from './globalConfig'
import backgroundImage from './backgroundImage'
import rightMenu from "./rightMenu"
import actionConfig from "./actionConfig"
import uploadFile from "./uploadFile"
import JsxRender from './standardForm/src/jsx-render.vue'
import Label from './materials/Empty'
import Day from './materials/Day'
import Clock from './materials/Clock'
import Search from './materials/Search'
import Library from './materials/Library'
import LibraryList from './materials/LibraryList'
import Navigation from "./materials/Navigation"
import icon from './tools/icon'
import tips from './tools/tips'
import customInputNumber from './standardForm/src/customInputNumber.vue'
import customSelectBox from './standardForm/src/customSelectBox.vue'
import ActionPopover from './actionPopover'
import teleport from './teleport'
import MaterialConfig from './materialConfig'
import LibraryItem from './materials/LibraryItem'
import StandardCards from './materials/StandardCards'
import ChartPie from './materials/Charts/ChartPie'
import ChartBar from './materials/Charts/ChartBar'
import ChartLine from './materials/Charts/ChartLine'
import ChartHistogram from './materials/Charts/ChartHistogram'
import StepCard from './materials/StepCard'
import QuickCards from './materials/QuickCards'
import QuickNavigation from './materials/QuickNavigation'
import CodeCards from './materials/CodeCards'
import MediaCards from './materials/MediaCards'
import uploadCropper from "./uploadCropper"

import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import i18n from '@/lang/i18n.js'
import oneMessage from '@/utils/oneMessage'
import DataCards from "./materials/DataCards"
import Banner from "./materials/Banner"
import Calendar from "./materials/Calendar"
import ProjectNavigation from "./materials/ProjectNavigation"
import Notice from "./materials/Notice"
import Message from "./materials/Message"
import OA from "./materials/OA"
import OaTask from "./materials/OaTask"
import LinkNavigation from './materials/LinkNavigation'
import ProcessManagement from "./materials/ProcessManagement"
import CURD from "./materials/CRUD"
const components = [gridLayout,homePage,gooeyMenu,customDialog,baseConfig,MaterialSelector,backgroundSelector,standardColorPicker,actionConfig,uploadFile,
    backgroundFilterSelector,positionSelector,StandardForm,ComponentForm,CustomForm,FontSelector,globalConfig,backgroundImage,rightMenu,JsxRender,teleport,ActionPopover,
    Label,Day,Clock,Search,Library,LibraryList,icon,tips,customInputNumber,cardMaterialSelector,MaterialConfig,Navigation,customSelectBox,LibraryItem,StandardCards,QuickCards,uploadCropper,
    ChartPie,ChartBar,ChartLine,ChartHistogram,StepCard,QuickNavigation,CodeCards,MediaCards,DataCards,Banner,Calendar,ProjectNavigation];

const install = function (Vue, options = {}) {
    if (install.installed) return;
    if(options.lang) i18n.locale = options.lang
    this.$i18n = i18n
    // 循环全局注册组件
    components.map(component => {
        Vue.use(component);
    });
    Vue.prototype.$oneMessage = oneMessage
    Vue.use(ElementUI)
};

// 判断是否时直接引入文件，如果是，就不用调用Vue.use，script直接引用
if ((typeof window !== 'undefined') && window.Vue) {
    install(window.Vue);
}

export default {
    install,
    gridLayout,
    homePage,
    gooeyMenu,
    customDialog,
    baseConfig,
    MaterialSelector,
    backgroundSelector,
    backgroundFilterSelector,
    standardColorPicker,
    positionSelector,
    StandardForm,
    ComponentForm,
    CustomForm,
    FontSelector,
    globalConfig,
    backgroundImage,
    rightMenu,
    actionConfig,
    uploadFile,
    JsxRender,
    Label,
    Day,icon,tips,
    Clock,
    Search,
    Library,
    LibraryList,
    customInputNumber,
    teleport,ActionPopover,
    cardMaterialSelector,
    MaterialConfig,
    Navigation,
    LibraryItem,
    customSelectBox,
    StandardCards,
    QuickCards,
    uploadCropper,
    ChartPie,
    ChartBar,
    ChartLine,
    ChartHistogram,
    StepCard,
    QuickNavigation,
    CodeCards,
    MediaCards,
    DataCards,
    Banner,
    Calendar,
    ProjectNavigation,
    Notice,
    Message,
    OA,
    OaTask,
    LinkNavigation,
    ProcessManagement,
    CURD,
};
export {
    gridLayout,
    homePage,
    gooeyMenu,
    customDialog,
    baseConfig,
    MaterialSelector,
    backgroundSelector,
    backgroundFilterSelector,
    standardColorPicker,
    positionSelector,
    StandardForm,
    ComponentForm,
    CustomForm,
    FontSelector,
    globalConfig,
    backgroundImage,
    rightMenu,
    actionConfig,
    uploadFile,
    JsxRender,
    Label,
    Day,icon,tips,
    Clock,
    Search,
    Library,
    LibraryList,
    customInputNumber,
    teleport,ActionPopover,
    cardMaterialSelector,
    MaterialConfig,
    Navigation,
    LibraryItem,
    customSelectBox,
    StandardCards,
    QuickCards,
    uploadCropper,
    ChartPie,
    ChartBar,
    ChartLine,
    ChartHistogram,
    StepCard,
    QuickNavigation,
    CodeCards,
    MediaCards,
    DataCards,
    Banner,
    Calendar,
    ProjectNavigation,
    Notice,
    Message,
    OA,
    OaTask,
    LinkNavigation,
    ProcessManagement,
    CURD,
};
