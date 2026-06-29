<template>
  <div>
    <basic-container>
      <!-- 顶部菜单nav -->
      <menuNav ref="tabMenuNav" />
      <div class="outer-container" id="outerbox" :class="{'no-padding':(['/chart-design-ui/'].indexOf($route.query.src)!=-1 || ($route.query.src == '/jvs-upms-ui/' && $route.hash && nopaddingHash.indexOf($route.hash) > -1))}">
        <div v-for="(item, index) in tagListData" :key="item.value+item.hash" :style="`height:${(item.value+item.hash) == ($route.query.src+$route.hash) ? '100%' : '0'};position:relative;`" v-show="getUrlType(item) == 'list' ? true : (item.value+item.hash) == ($route.query.src+$route.hash)" :class="{'visible-page': (item.value+item.hash) == ($route.query.src+$route.hash)}">
          <!-- <iframe v-if="getUrlType(item) == 'iframe'" :id="'mainIframe'+index"  :src="item.value ? (item.value+item.hash) : urlPath" class="iframe" ref="iframe"></iframe> -->
           <div class="fullscreen-btn"  @click="toggleFullScreen" v-if="getUrlType(item) == 'iframe'">
              <img :src="fullImg">
          </div>
          <!-- {{ getUrlType(item) }} -->
          <!-- <iframe v-if="getUrlType(item) == 'iframe'" :id="'mainIframe'+index"  :src="item.value ? (item.value+item.hash) : urlPath" class="iframe" ref="iframe"></iframe> -->
          <iframe v-if="getUrlType(item) == 'iframe'" :id="'mainIframe'+index" :src="item.value ? (item.value+item.hash) : urlPath" class="iframe" :ref="'iframe' + index"> </iframe>
          <form-page v-if="getUrlType(item) == 'form'" :routerQuery="getUrlQuery(item)"></form-page>
          <list-page v-if="getUrlType(item) == 'list'" :routerQuery="getUrlQuery(item)"></list-page>
          <!-- 第四步 源代码接入 自定义页面接入   路由，以组件的方式进行挂载 -->
          <keep-alive v-if="['upms', 'devDemoView','demoIndex'].indexOf(getUrlType(item)) > -1">
            <component :is="getComName(item.hash)" :routerQuery="getUrlQuery(item)"></component>
          </keep-alive>
        </div>
      </div>
    </basic-container>
  </div>
</template>

<script>
import { mapGetters } from "vuex";
import { getStore } from "@/util/store.js";
import menuNav from '@/page/main/index/top/menuNav'
import FormPage from '@/views/page/views/show/formUse'
import ListPage from '@/views/page/views/show/listUse'
import { upmsComList } from '@/views/upms/component.js'
import { devDemoViewComList } from '@/views/devDemoView/component.js'
import fullScreen from '@/assets/images/backimg/fullScreen.png'
// 第四步 源代码接入 自定义页面接入   路由，以组件的方式进行挂载
import { demoIndex } from '@/views/demoIndex/component.js'
import { decryption } from "@/util/util";
import {enCodeKey} from "@/const/const";
export default {
  name: "home",
  components: {
    ...mapGetters(["screen"]),
    ...upmsComList,
    ...devDemoViewComList,
    ...demoIndex,// 第四步 源代码接入 自定义页面接入   路由，以组件的方式进行挂载
    menuNav, FormPage, ListPage
  },
  computed : {
    ...mapGetters(["tagList"]),
    tagListData() {
      return this.tagList
    }
  },
  data() {
    return {
      urlPath: this.getUrlPath(), //iframe src 路径
      src: "",
      isFullscreen: false,
      fullImg:fullScreen,
      nopaddingHash: ['#/systeminfo', '#/orglogout', '#/settings', '#/systemconfig']
    };
  },
  created() {
    // 监听子页面传值
    let _this = this
    window.addEventListener('message',function(e){
      if(e.data) {
        if(e.data.command == 'loginOut') {
          sessionStorage.clear()
          localStorage.clear()
          _this.$openUrl('/#/login', '_self')
        }
        if(e.data.command == 'fresh' && window.top != window.self) {
          location.reload()
        }
        if(e.data.command == 'openUrl' && e.data.url) {
          _this.$openUrl(e.data.url, '_self')
        }
        if(e.data.command == 'closeTab' && e.data.id) {
          if(location.hash.includes('src=')) {
            let tp = location.hash.split('src=')
            let qstring = decodeURIComponent(tp[1])
            if(qstring.includes('?')) {
              let tp = qstring.split('?')[1].split('&')
              for(let i in tp) {
                if(tp[i].startsWith('id=')) {
                  if(tp[i].split('=')[1] == e.data.id && _this.$refs.tabMenuNav) {
                    _this.$refs.tabMenuNav.menuTag(qstring, 'remove')
                    if(e.data.from && e.data.from.length > 0) {
                      let tp = decryption({
                        data: {data: e.data.from},
                        key: enCodeKey,
                        param: ["data"]
                      });
                      tp.data = JSON.parse(tp.data)
                      let tag = _this.$refs.tabMenuNav.findTag(tp.data.url)
                      _this.$refs.tabMenuNav.openTag(tag.tag)
                    }
                  }
                }
              }
            }
          }
        }
      }
    },false);
    window.addEventListener('message', this.openNewTab)
     this.fixElementUIFormError();
  },
 mounted() {
  this.addFullscreenChangeListener() //全屏监听触发
  this.handSendMessage() // 向子iframe传递数据
},
  props: ["routerPath"],
  watch: {
    routerPath: function() {
      // 监听routerPath变化，改变src路径
      this.urlPath = this.getUrlPath();
    }
  },
  deactivated() {
    window.removeEventListener('message', this.openNewTab)
  },
  beforeDestroy() {
    window.removeEventListener('message', this.openNewTab)
  },
  methods: {
    handSendMessage(){
      const currentIndex = this.tagListData.findIndex(
        item => (item.value + item.hash) === (this.$route.query.src + this.$route.hash)
      );

      // 获取当前显示的组件类型
      const currentItem = this.tagListData[currentIndex];
      const currentType = this.getUrlType(currentItem);

      // 根据不同类型的组件获取对应的DOM元素
      if (currentType === 'iframe') {
        const iframeEl = document.getElementById(`mainIframe${currentIndex}`)
        if(iframeEl){
          iframeEl.onload = () => {
            // console.log('iframeViewer已加载')
            const msg = {
              command:'jvs-iframe-msg',
              params: {
                userName: getStore({ name: "userInfo" })?.accountName,
                token: getStore({
                  name: "access_token",
                  debug: true
                })?.content
              }
            }
            iframeEl.contentWindow.postMessage(JSON.stringify(msg), '*')
          }
        }
      }
    },
    openNewTab(e){
      if(e.data.command === 'newTab'){
        // console.log('接受iframe子模块传来的newTab事件', e.data)
        const params = e.data.params || {}
        this.$router.push({
          path: this.$router.$jvsRouter.getPath({
            name: params.name,
            src: `${params.url}?id=${params.id}&dataModelId=${params.dataModelId}&jvsAppId=${params.jvsAppId}`,
          }),
        });
      }
    },
   shouldShowFullscreenBtn(item) {
      return item.label && (item.label.includes('甘特图') || item.label.includes('京能'));
    },
    isCurrentPage(item) {
      // 判断是否为当前显示的页面
      return (item.value + item.hash) === (this.$route.query.src + this.$route.hash);
    },
    fixElementUIFormError() {
      // 保存原始的console.error
      const originalConsoleError = console.error;

      // 重写console.error以忽略特定的Element UI错误
      console.error = function () {
        // 检查是否是Element UI表单的"unpected width"错误
        if (typeof arguments[0] === 'string' && arguments[0].includes('unpected width')) {
          // 忽略该错误，不输出到控制台
          return;
        }
        // 对于其他错误，使用原始的console.error方法
        return originalConsoleError.apply(console, arguments);
      };
    },        //全屏监听
   addFullscreenChangeListener() {
    const self = this;
    document.addEventListener('fullscreenchange', () => {
      self.isFullscreen = !!document.fullscreenElement;
    });

    document.addEventListener('webkitfullscreenchange', () => {
      self.isFullscreen = !!document.webkitFullscreenElement;
    });

    document.addEventListener('mozfullscreenchange', () => {
      self.isFullscreen = !!document.mozFullScreenElement;
    });

    document.addEventListener('msfullscreenchange', () => {
      self.isFullscreen = !!document.msFullscreenElement;
      // 检查是否退出全屏，如果是则重置样式
      if (!document.msFullscreenElement) {
        self.resetFullscreenElementsStyle();
      }
    });
  },
  // 重置全屏元素的样式
  resetFullscreenElementsStyle() {
    // 重置可能被修改的样式
    const elements = document.querySelectorAll('.table-show, .list-page-container, .visible-page');
    elements.forEach(element => {
      element.style.width = '';
      element.style.height = '';
      element.style.overflow = '';
      element.style.boxSizing = '';
      element.style.display = '';
      element.style.alignItems = '';
      element.style.justifyContent = '';
    });

    // 重置.jvs-table样式
    const jvsTables = document.querySelectorAll('.jvs-table');
    jvsTables.forEach(table => {
      table.style.flex = '';
      table.style.maxHeight = '';
      table.style.maxWidth = '';
    });
  },
    // 全屏切换
    toggleFullScreen() {
      const currentIndex = this.tagListData.findIndex(
        item => (item.value + item.hash) === (this.$route.query.src + this.$route.hash)
      );

      // 获取当前显示的组件类型
      const currentItem = this.tagListData[currentIndex];
      const currentType = this.getUrlType(currentItem);

      let element = null;

      // 根据不同类型的组件获取对应的DOM元素
      if (currentType === 'iframe') {
        element = this.$refs[`iframe${currentIndex}`]?.[0];
      } else if (currentType === 'list') {
        // 优先级从高到低尝试多种方式获取元素
        // 1. 通过DOM查询获取list-page容器中的.table-show元素
        element = document.querySelector(`#listPageContainer${currentIndex} .table-show`);

        // 2. 如果上面没找到，尝试获取整个容器
        if (!element) {
          element = document.querySelector(`#listPageContainer${currentIndex}`);
        }

        // 3. 如果仍然没找到，尝试通过ref获取
        if (!element) {
          const listPageComponent = this.$refs[`listPage${currentIndex}`];
          if (listPageComponent && listPageComponent.$el) {
            element = listPageComponent.$el.querySelector('.table-show') || listPageComponent.$el;
          }
        }
      }

      if (!element) {
        console.error('无法找到要全屏的元素');
        return;
      }

      // 在进入全屏前，确保元素具有正确的样式
      if (!document.fullscreenElement) {
        // 进入全屏前的准备工作
        element.style.width = '100vw';
        element.style.height = '100vh';
        element.style.overflow = 'auto';
        element.style.boxSizing = 'border-box';
        element.style.display = 'flex';
        element.style.alignItems = 'center';
        element.style.justifyContent = 'center';

        // 查找并设置.jvs-table样式
        const jvsTable = element.querySelector('.jvs-table');
        if (jvsTable) {
          jvsTable.style.flex = '1';
          jvsTable.style.maxHeight = '100%';
          jvsTable.style.maxWidth = '100%';
        }

        // 进入全屏
        if (element.requestFullscreen) {
          element.requestFullscreen();
        } else if (element.webkitRequestFullscreen) {
          element.webkitRequestFullscreen();
        } else if (element.mozRequestFullScreen) {
          element.mozRequestFullScreen();
        } else if (element.msRequestFullscreen) {
          element.msRequestFullscreen();
        } else {
          console.error('浏览器不支持全屏API');
          // 恢复样式
          element.style.width = '';
          element.style.height = '';
          element.style.overflow = '';
          element.style.boxSizing = '';
          element.style.display = '';
          element.style.alignItems = '';
          element.style.justifyContent = '';

          if (jvsTable) {
            jvsTable.style.flex = '';
            jvsTable.style.maxHeight = '';
            jvsTable.style.maxWidth = '';
          }
        }
      } else {
        // 退出全屏
        if (document.exitFullscreen) {
          document.exitFullscreen();
        } else if (document.webkitExitFullscreen) {
          document.webkitExitFullscreen();
        } else if (document.mozCancelFullScreen) {
          document.mozCancelFullScreen();
        } else if (document.msExitFullscreen) {
          document.msExitFullscreen();
        }
      }
    },
    created() {},
    // 加载浏览器窗口变化自适应
    resize() {
      window.onresize = () => {
        this.iframeInit();
      };
    },
    // 加载组件
    load() {
      var flag = true; //URL是否包含问号
      if (this.$route.query.src.indexOf("?") == -1) {
        flag = false;
      }
      var list = [];
      for (var key in this.$route.query) {
        if (key != "src" && key != "name") {
          list.push(`${key}= this.$route.query[key]`);
        }
      }
      list = list.join("&").toString();
      if (flag) {
        this.$route.query.src = `${this.$route.query.src}${
          list.length > 0 ? `&list` : ""
        }`;
      } else {
        this.$route.query.src = `${this.$route.query.src}${
          list.length > 0 ? `?list` : ""
        }`;
      }
      if(this.$route.query.src.indexOf('?') != -1){
        this.src = this.$route.query.src+this.$route.hash;
      }else{
        this.src = this.$route.query.src+this.$route.hash;
      }
      if(this.src.includes('/mgr')) {
        let str = this.src
        str = str.replace(/mgr\/\#/g, 'mgr')
        this.src = str
      }
      //超时5s自动隐藏等待框，加强用户体验
      let time = 5;
      const timeFunc = setInterval(() => {
        time--;
        if (time == 0) {
          clearInterval(timeFunc);
        }
      }, 1000);
      this.iframeInit();
      $('#mainIframe').attr('src', this.src)
    },
    //iframe窗口初始化
    iframeInit() {
      const iframe = this.$refs.iframe;
      const clientHeight =
        document.documentElement.clientHeight - (screen > 1 ? 200 : 130);
      // iframe.style.height = `${clientHeight}px`
      // iframe.style.height = `100%`;
      if (iframe && iframe.attachEvent) {
        iframe.attachEvent("onload", () => {
        });
      } else {
        iframe.onload = () => {
        };
      }
      this.$forceUpdate()
    },
    getUrlPath: function() {
      //获取 iframe src 路径
      const token = getStore({
        name: "access_token",
        debug: true
      });
      //请求携带token
      let url = window.location.href;
      //  + "%3Ftoken%3D"+token.content;
      url = url.replace("/myiframe", "");
      return url;
    },
    getUrlType (item) {
      let type = 'iframe'
      if(item.value == '/page-design-ui/') {
        if(item.hash.startsWith('#/form/use')) {
          type = 'form'
        }
        if(item.hash.startsWith('#/list/use')) {
          type = 'list'
        }
      }
      if(item.value == '/jvs-upms-ui/') {
        type = 'upms'
      }
      if(item.value == '/jvs-devDemoView-ui/') {
        type = 'devDemoView'
      }
      //第四步 源代码接入 自定义页面接入   路由，以组件的方式进行挂载
      if(item.value == '/jvs-demo-ui/') {
        type = 'demoIndex'
      }
      return type
    },
    getUrlQuery (item) {
      let obj = null
      if(item.hash && item.hash.includes('?')) {
        obj = {}
        let str = item.hash.split('?')[1]
        let params = str.split('&')
        for(let i in params) {
          let temp = params[i].split('=')
          obj[temp[0]] = temp[1]
        }
      }
      return obj
    },
    getComName (hash) {
      let str = hash.slice(2)
      if(str.includes('?')) {
        str = str.split('?')[0]
      }
      return str
    }
  }
};
</script>

<style lang="scss">
@import '@/views/upms/component.scss';
.iframe {
  width: 100%;
  border-radius: 6px;
  // height: calc(100% - 16px);
  height: 100%;
  border: none;
  overflow-x: hidden;
  overflow-y: scroll;
}
.outer-container {
  position: relative;
  width: 100%;
  height: calc(100% - 32px);
  background: #fff;
  box-sizing: border-box;
  padding: 20px;
  overflow: hidden;
  .form-show-info, .table-show{
    height: 100%;
    .permission{
      width: 100%;
      height: 100%;
    }
    .jvstable-left-tree{
      display: none;
    }
  }
  .table-show{
    .empty-view, .my-popover-box{
      display: none;
    }
  }
  .visible-page{
    .table-show{
      .jvstable-left-tree{
        display: block;
      }
      .empty-view, .my-popover-box{
        display: block;
      }
    }
  }
}
.no-padding{
  padding: 0px;
}
.fullscreen-btn{
    width: 30px;
    height: 25px;
    position: absolute;
    z-index: 2;
    top: 5px;
    right: 5px;
    cursor: pointer;
    text-align: center;
    padding-top: 5px;
    background: #f5f6f7;
    border-radius: 4px;

    // 为不同类型的容器添加特定样式，确保在iframe和list-page中显示一致
    &.iframe-fullscreen-btn {
      top: 5px;
      right: 5px;
    }

    &.list-page-fullscreen-btn {
      top: 60px;
      right: 5px;
    }
}
.fullscreen-btn img{
      width: 20px;
    height: 20px;
}
</style>
