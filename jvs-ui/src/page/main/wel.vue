<template>
  <basic-container class="wel-index">
    <!-- 顶部菜单nav -->
    <menuNav />
    <div class="top-outer-custom">
      <dynaIndex @openEvent="openEvent" v-if="iframeHind"></dynaIndex>
      <iframe v-else :src="iurl" class="iframe" width="100%" height="100%" style="border: none;"></iframe>
    </div>
  </basic-container>

</template>

<script>
  import {mapGetters} from 'vuex';
  import dynaIndex from './dynaIndex/index';
  import menuNav from '@/page/main/index/top/menuNav'
  import bus from '@/util/vuebus'
  import { getUserInfo } from '@/api/admin/user'

  export default {
    name: 'wel',
    components: {dynaIndex, menuNav},
    props: {
      jvsDesign: {
        type: Object
      }
    },
    data() {
      return {
        activeNames: ['1', '2', '3', '4'],
        DATA: [],
        text: '',
        actor: '',
        count: 0,
        isText: false,
        url: '',
        iurl:'https://bi-zhjj.jnyjy.com/#/screen/sharelink/0d4b13d4/data-screen',
        showEmpty: false,
        iframeHind: true
      }
    },
    created() {
      // 监听子页面传值
      let _this = this
      this.fetchUserInfo()
      window.addEventListener('message',function(e){
        if(e.data) {
          if(e.data.command == 'isIndex') {
            if(_this.$route.path == '/wel/index') {
              sessionStorage.setItem('lastUrl', location.href)
            }
          }
        }
      },false);
    },

    computed: {
      ...mapGetters(['website'])
    },
    methods: {
        async fetchUserInfo() {
          try {
            const response = await getUserInfo();
            const userInfo = response.data.data;
            if(userInfo.realName == '京能电力智慧运营中心' ){
              this.iframeHind = false
            }else{
              this.iframeHind = true
            }
            console.log('用户姓名:', userInfo.realName);
            console.log('用户手机号:', userInfo.phone);
          } catch (error) {
            console.error('获取用户信息失败:', error);
          }
        },
      //判断浏览器是否支持Web Notifications API
      suportNotify(obj){
        if (window.Notification) {
          // 支持
          console.log("支持"+"Web Notifications API");
          //如果支持Web Notifications API，再判断浏览器是否支持弹出实例
          this.showMess(obj)
        }else {
          // 不支持
          console.log("不支持 Web Notifications API");
          this.$notify({
            title: obj.title,
            message: obj.content,
            position: 'bottom-right',
            duration: 3000
          })
        }
      },
      showMess(obj){
        let _this = this
        let timer = null
        if(timer) {
          clearTimeout(timer)
        }
        timer = setTimeout(function () {
          console.log(Notification.permission)
          //如果支持window.Notification 并且 许可不是拒绝状态
          if(window.Notification && Notification.permission !== "denied") {
              //Notification.requestPermission这是一个静态方法，作用就是让浏览器出现是否允许通知的提示
              Notification.requestPermission(function(status) {
                //如果状态是同意
                if (status === "granted") {
                  var m = new Notification(obj.title, {
                    body: obj.content, //消息体内容
                    icon: obj.icon //消息图片
                  });
                  m.onclick = function () {//点击当前消息提示框后，跳转到当前页面
                    window.focus();
                  }
              } else{
                _this.$notify({
                  title: obj.title,
                  message: obj.content,
                  position: 'bottom-right',
                  duration: 3000
                })
              }
            });
          }else{
            _this.$notify({
              title: obj.title,
              message: obj.content,
              position: 'bottom-right',
              duration: 3000
            })
            clearTimeout(timer)
          }
        },1000)
      },
      openEvent (data) {
        this.$emit('openEvent', data)
      }
    },
    mounted () {
      bus.$on('notice', (e) => {
        if(e) {
          this.suportNotify(
            {
              title: e.title,
              content: e.content,
              icon: '', // 消息图片
            }
          )
        }
      })
      if(this.$route.query && this.$route.query.fresh == 'true') {
        this.$openUrl('/#/wel/index', '_self')
        bus.$emit('refresh', true);
      }
    }
  }
</script>

<style scoped="scoped" lang="scss">
  // .iframe {
  //   height: 100%;
  //   width: 100%;
  //   border: none;
  //   position: absolute;
  //   top: 0;
  //   bottom: 0;
  //   overflow-x: hidden;
  //   overflow-y: scroll;
  // }

  .top-outer-custom{
    position: relative;
    height: calc(100% - 32px);
    box-sizing: border-box;
    overflow: hidden;
  }
  // .top-outer-container {
  //   position: relative;
  //   // overflow: hidden;
  //   width: 100%;
  //   // height: 850px;
  //   height: 100%;
  // }
  // .wel-index {
  //   .top-outer-container {
  //     height: calc(100% - 32px);
  //     overflow: hidden;
  //     overflow-y: auto;
  //     box-sizing: border-box;
  //     padding: 8px 10px;
  //   }
  //   .top-outer-container::-webkit-scrollbar {
  //     display: none;
  //   }
  // }
  // .wel-contailer {
  //   position: relative;
  // }

  // .banner-text {
  //   position: relative;
  //   padding: 0 20px;
  //   font-size: 20px;
  //   text-align: center;
  //   color: #333;
  // }

  // .banner-img {
  //   position: absolute;
  //   top: 0;
  //   left: 0;
  //   width: 100%;
  //   height: 100%;
  //   opacity: 0.8;
  //   display: none;
  // }

  // .actor {
  //   height: 250px;
  //   overflow: hidden;
  //   font-size: 18px;
  //   color: #333;
  // }

  // .actor:after {
  //   content: '';
  //   width: 3px;
  //   height: 25px;
  //   vertical-align: -5px;
  //   margin-left: 5px;
  //   background-color: #333;
  //   display: inline-block;
  //   animation: blink 0.4s infinite alternate;
  // }

  // .typeing:after {
  //   animation: none;
  // }

  // @keyframes blink {
  //   to {
  //     opacity: 0;
  //   }
  // }

  // .homediv {
  //   width: 100%;
  //   min-height: 900px;
  //   display: flex;
  //   justify-content: center;
  //   align-items: center;
  // }
  // .welcom-back-div{
  //   width: 100%;
  //   height: 100%;
  //   background-image: url('../../const/img/welcom.png');
  //   background-size: 60%;
  //   background-repeat: no-repeat;
  //   background-position: center;
  // }
</style>
