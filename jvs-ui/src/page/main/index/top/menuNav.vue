<template>
  <div class="top-nav-bar-box" @click="hideContextMenu">
    <!-- 顶部tab关闭按钮 -->
    <div
      v-if="contextmenuFlag"
      class="jvs-tags__contentmenu"
      :style="{left:contentmenuX+'px',top:contentmenuY+'px'}"
    >
      <div class="item" @click="closeOthersTags">关闭其他</div>
      <div class="item" @click="closeAllTags">关闭全部</div>
    </div>
    <!-- 暂时屏蔽 -->
    <!-- <i v-if="showHide" class="showhide-menu-icon el-icon-s-unfold" @click="setCollapse"></i>
    <i v-else class="showhide-menu-icon el-icon-s-fold" @click="setCollapse"></i> -->
    <el-tabs
      v-model="active"
      type="card"
      :style="'flex:1;width:100%;'"
      @contextmenu.native="handleContextmenu"
      @tab-click="openTag"
      @edit="menuTag"
    >
      <el-tab-pane
        v-for="(item, index) in tagList"
        :key="(item.hash+index) || (item.name+index)"
        :label="item.label"
        :closable="item.label!=='首页' && index != 0"
        :name="item.hash ? (item.value + item.hash) : item.value"
      >
        <template slot="label">
<!--          <svg v-if="item.label=='首页' && index == 0" class="icon" aria-hidden="true" style="width: 16px;height: 16px;">-->
<!--            <use :xlink:href="(active == '/wel/index') ? '#icon-jvs-shouye-xuanzhong' : '#icon-jvs-shouye-weixuanzhong'"></use>-->
<!--          </svg>-->
          <span v-if="item.label=='首页' && index == 0" aria-hidden="true">
            首页
          </span>
          <span v-else>{{ (item.hash && item.value == '/jvs-upms-ui/') ? $langt(`localMenu.${localmenuLang[item.value+item.hash]}`) : item.label}}</span>
        </template>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
<script>
import { mapGetters, mapState } from 'vuex'
import { localMenu } from '../sidebar/localMenu';
export default {
  computed: {
    ...mapGetters(["tagWel", "tagList", "tag", "website"]),
    ...mapState({
      showTag: state => state.common.showTag
    }),
    tagLen () {
      return this.tagList.length||0;
    },
    localmenuLang () {
      let obj = {}
      for(let i in localMenu) {
        for(let j in localMenu[i]) {
          if(localMenu[i][j].children) {
            localMenu[i][j].children.filter(lit => {
              if(lit.extend && lit.extend.url && lit.extend.url.startsWith('/jvs-upms-ui/')) {
                let key = '/jvs-upms-ui/#/' + lit.extend.url.split('/jvs-upms-ui/')[1]
                obj[key]  = lit.langKey
              }
            })
          }
        }
      }
      return obj
    }
  },
  data(){
    return {
      active: "",
      contextmenuFlag: false,
      contentmenuX: "",
      contentmenuY: "",
      showHide: true
    }
  },
  created() {},
  methods: {
    watchContextmenu () {
      if (!this.$el.contains(event.target)||event.button!==0) {
        this.contextmenuFlag=false;
      }
      window.removeEventListener("mousedown", this.watchContextmenu);
    },
    handleContextmenu (event) {
      let target=event.target;
      let flag=false;
      if (typeof target.className == 'string' && target.className.indexOf("el-tabs__item")>-1) flag=true;
      else if (typeof target.parentNode.className == 'string' && target.parentNode.className.indexOf("el-tabs__item")>-1) {
        target=target.parentNode;
        flag=true;
      }else if(typeof target.parentNode.className == 'object' && target.parentNode.parentNode.className.indexOf("el-tabs__item")>-1){
        target=target.parentNode.parentNode;
        flag=true;
      }
      if (flag) {
        event.preventDefault();
        event.stopPropagation();
        this.contentmenuX=event.clientX;
        this.contentmenuY=event.clientY;
        this.tagName=target.getAttribute("aria-controls").slice(5);
        this.contextmenuFlag=true;
      }else{
        this.contextmenuFlag=false;
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
      if(item.name == this.tag.value + this.tag.hash){ //  this.tag.hash || item.name == this.tag.value) {
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
        if(item.hash) {
          if ((item.value + item.hash) === value) {
            tag=item;
            key=index;
          }
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
    // 展开 收起
    setCollapse () {
      this.showHide = !this.showHide
      this.$store.commit("SET_COLLAPSE")
    },
    hideContextMenu () {
      if(this.contextmenuFlag) {
        this.contextmenuFlag = false
      }
    }
  },
  mounted () {
    this.setActive();
  },
  watch: {
    tag () {
      this.setActive();
    },
    contextmenuFlag () {
      window.addEventListener("mousedown", this.watchContextmenu);
    }
  },
}
</script>
<style lang="scss">
.jvs-main{
  .top-nav-bar-box {
    position: relative;
    display: flex;
    justify-content: space-between;
    height: 32px;
    font-size: 14px;
    background: #F5F6F7;
    overflow: hidden;
    border-left: 1px solid #fff;
    box-sizing: border-box;
    .el-tabs.el-tabs--top {
      overflow: hidden;
      .el-tabs__header {
        height: 100%;
        margin: 0;
        border: none;
        .el-tabs__nav-wrap {
          height: 100%;
          box-sizing: border-box;
          padding-top: 4px;
          .el-tabs__nav-scroll {
            height: 100%;
            .el-tabs__nav {
              height: 100%;
              display: flex;
              padding-left: 10px;
              border: 0;
              .el-tabs__item {
                line-height: 100%;
                height: 100%;
                display: flex;
                align-items: center;
                justify-content: center;
                border-left: 0;
                font-size: 14px;
                color: #6F7588;
                font-family: Microsoft YaHei-Regular;
                border-radius: 0px 0px 6px 6px;
                border: 0;
                box-shadow: none!important;
              }
              .el-tabs__item.is-active{
                color: #363B4C;
                background-color: #fff;
                border-radius: 6px 6px 0px 0px;
                position: relative;
                .el-icon-close:hover{
                  color: #fff;
                }
              }
              .el-tabs__item.is-active::before{
                content: "";
                display: block;
                width: 10px;
                height: 10px;
                box-sizing: border-box;
                border-radius: 0 0 20px 0;
                border: 4px solid #fff;
                border-left: 0;
                border-top: 0;
                background-color: #F5F6F7;
                position: absolute;
                left: -7px;
                bottom: -4px;
              }
              .el-tabs__item.is-active::after{
                content: "";
                display: block;
                width: 10px;
                height: 10px;
                box-sizing: border-box;
                border-radius: 0 0 0 20px;
                border: 4px solid #fff;
                border-right: 0;
                border-top: 0;
                background-color: #F5F6F7;
                position: absolute;
                right: -7px;
                bottom: -4px;
              }
              .el-tabs__item:nth-of-type(1){
                padding: 0 15px;
              }
            }
          }
          .el-tabs__nav-prev, .el-tabs__nav-next{
            height: 100%;
            display: flex;
            align-items: center;
          }
        }
      }
    }
  }
  .top-nav-bar-box::before{
    position: absolute;
    content: "";
    width: 100%;
    height: 1px;
    background: linear-gradient(270deg, #EEEFF0 0%, rgba(238,239,240,0) 100%);
  }
}
</style>
