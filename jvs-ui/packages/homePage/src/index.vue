<template>
  <div class="page" :style="{minWidth:`${$homePageOptions.minWidth}px`}">
    <div class="page-top">
      <div v-if="mode == 'show'" class="show">
        <slot name="showSlot"></slot>
      </div>
      <div v-if="mode == 'design'" class="design">
        <slot name="designSlot"></slot>
        <div class="design-tool">
          <gooeyMenu
            v-if="!isReadOnly && !isMobilePage && !$homePageOptions.isMoBileView"
            :isLock="pageIsLock"
            toolType="button"
            @handleSave="handleSave"
            @addComponent="showAddDialog"
            @showGlobalConfig="showGlobalConfig"
            @showAuxiliaryConfig="showAuxiliaryConfig"
            @changeItemAffixRectInfo="editAffixRectInfo"
            @updateIsLock="updateIsLock"
            @roleConfig="roleConfig"
          ></gooeyMenu>
          <div class="right-button">
            <slot name="designRight"></slot>
          </div>
        </div>
      </div>
    </div>
    <div @mousedown="popMenu($event)" class="page-content" @contextmenu.prevent.stop="stopeContextMenu" :id="ids">
      <gridLayouts ref="gridLayout" :list="list" :affix="affix" :colNum="$homePageOptions.layoutColNum" :isLock.sync="pageIsLock" :ids="ids" :isReadOnly="isReadOnly" :materialConfigCloseRadom="materialConfigCloseRadom" :reqUrl="reqUrl" @edit="showEditDialog" @changeItemAffixRectInfo="editAffixRectInfo" @openEvent="openEvent"></gridLayouts>
    </div>
    <ActionConfig ref="actionConfig" @editComponent="editComponent"/>
    <globalConfig :visible.sync="globalConfigVisible" :globalFormData.sync="globalFormData"></globalConfig>
    <cardMaterialSelector ref="cardMaterialSelector" :componentList="componentList" @materialSelect="materialSelect"></cardMaterialSelector>
    <materialConfig ref="materialConfig" @editComponent="editComponent" :reqUrl="reqUrl" :componentList="componentList" @addComponent="addComponent" :isLock.sync="pageIsLock" @close="materialConfigClose"></materialConfig>
    <rightMenu ref="rightMenu" :mousePosition="mousePosition" :menuList="menuList" :visible.sync="mousePositionVisible" v-if="!isReadOnly && mousePositionVisible" :ids="ids" :isLock="pageIsLock"></rightMenu>
    <ActionPopover ref="actionPopover" id="actionPopover">
      <div
        v-if="
          actionElement &&
          actionElement.actionSetting &&
          actionElement.actionSetting.actionType === 1 &&
          actionElement.actionSetting.actionClickType === 1
        "
        class="action-popover-wrapper"
        :style="{
          borderRadius: actionElement.actionSetting.actionClickValue.borderRadius + 'px',
          boxShadow: actionElement.actionSetting.actionClickValue.boxShadow
        }"
      >
        <div
          class="bg"
          :style="{
            background: actionElement.actionSetting.actionClickValue.background,
            filter:
              actionElement.actionSetting.actionClickValue.background.includes('url') &&
              actionElement.actionSetting.actionClickValue.backgroundFilter
          }"
        ></div>
        <component
          :is="actionElement.actionSetting.actionClickValue.material"
          :isLock="isLock"
          :element="actionElement"
          :componentSetting="actionElement.actionSetting.actionClickValue.componentSetting"
          isAction
        ></component>
      </div>
    </ActionPopover>
  </div>
</template>

<script>
import { uid } from '@/utils'
import Vue from 'vue'
const updateLocalGlobal = (global) => localStorage.setItem('global', JSON.stringify(global))
const getLocalGlobal = () => {
    const global = JSON.parse(localStorage.getItem('global') || '{}')
    if (global.background && global.background.includes('/api/randomPhoto')) {
      const w = window.innerWidth
      const h = window.innerHeight
      global.background = global.background.replace(/w=(\d*)/, `w=${w}`).replace(/h=(\d*)/, `h=${h}`)
    }
    updateLocalGlobal(global)
    return global
}
export default {
    name:'homePage',
    props:{
      globalFormData:{
        type:Object,
        default(){
          return  {
            background: '',
            backgroundFilter: 'brightness(0.8)',
            lang: 'zh-cn',
            gutter: 10,
            css: '',
            js: '',
            globalFontFamily: '',
            loadHarmonyOSFont: false,
            siteTitle: '',
            disabledDialogAnimation: false,
            ...getLocalGlobal()
          }
        }
      },
      data:{
        type:Object,
        default(){
          return {
            list:[],
            affix:[]
          }
        }
      },
      componentList:{
        type:Array,
        default(){
          return []
        }
      },
      reqUrl:{
        type:String,
        default(){
          return ''
        }
      },
      isLock:{
        type:Boolean,
        default:true
      },
      isReadOnly:{
        type:Boolean,
        default:false
      },
      ids:{
        type:String,
        default:'home-page'
      },
      options:{
        type:Object,
        default(){
          return {
          }
        }
      },
      mode: {
        type: String,
        default: 'show'
      }
    },
  data() {
    return {
      defaultOption:{
        isMoBileView:false,
        globalConfigHidden:true,
        uploadFileSize:20, //文件上传大小
        uploadUrl:'/mgr/jvs-auth/upload/jvs-public',
        minWidth:null,
        uploadData:{
          module:'test/dynamic'
        },
        layoutColNum:24,
      },
      list:[],
      affix:[],
      globalConfigVisible:false,
      pageIsLock:this.isLock,
      popItems:[
        {
          label: () => this.$hit('添加卡片'),
          fn: () => {
            this.showAddDialog()
          },
          icon: { name: 'add', size: 20 }
        },
        // {
        //   label: () => this.$hit('全局设置'),
        //   fn: () => {
        //     this.showGlobalConfig()
        //   },
        //   icon: { name: 'setting-4', size: 18 }
        // },
        // {
        //   label: () => this.$hit('辅助功能'),
        //   fn: () => {
        //     this.showAuxiliaryConfig()
        //   },
        //   icon:  { name: 'tools', size: 18 }
        // },
        // {
        //   line: true
        // },
        {
          label: () => ( !this.pageIsLock ? this.$hit('进入编辑') : this.$hit('锁定')),
          fn: () => {
            this.updateIsLock(!this.pageIsLock)
          },
          icon: this.pageIsLock ? { name: 'unlock', size: 18 }:{ name: 'lock', size: 18 }
        }
      ],
      menuList: [],
      mousePosition: [-200,-200],
      mousePositionVisible: false,
      actionElement: {},
      materialConfigCloseRadom: -1,
      editingId: ''
    };
  },
  created() {
    Vue.prototype.$eventBus = new Vue()
    Vue.prototype.$homePageOptions = Object.assign({},this.defaultOption,this.options)
    if(this.$homePageOptions.isMoBileView){
      this.pageIsLock = false
    }
    Vue.prototype.$globalFormData = this.globalFormData
    if(this.mode == 'design') {
      this.pageIsLock = true
    }else{
      this.pageIsLock = false
    }
  },
  watch:{
    data:{
      handler(newVal){
        if(newVal){
          this.list = newVal.list
          this.affix = newVal.affix
        }
      },
      immediate:true
    },
    isReadOnly:{
      handler(newVal){
        this.pageIsLock = newVal
        if(newVal){
          this.mousePositionVisible = false
          if(this.$eventBus){
            this.$eventBus.$emit('hideAllRightMenu')
          }
        }
      },
      immediate:true
    },
    options:{
      handler(newVal){
        Vue.prototype.$homePageOptions = Object.assign({},this.defaultOption,newVal)
      },
      immediate:true
    },
    isMobilePage:{
      handler(newVal){
        this.pageIsLock = !!newVal
        this.mousePositionVisible = false
      },
      immediate:true
    },
    mode: {
      handler(newVal) {
        if(newVal == 'design') {
          this.pageIsLock = true
        }else{
          this.pageIsLock = false
        }
      }
    }
  },
  mounted() {
    // window.homePageOptions = Object.assign({},this.defaultOption,this.options)
    this.$eventBus.$on('hideGlobalRightMenu',()=>{
      this.mousePositionVisible = false
    })
    this.$eventBus.$on('openGlobalMenuRight',(mousePosition)=>{
      this.menuList = this.popItems
      this.mousePosition = mousePosition;
      this.mousePositionVisible = true
    })
    this.$eventBus.$on('openComMenuRight',(menuList,mousePosition)=>{
      this.mousePosition = mousePosition
      this.menuList = menuList
      this.mousePositionVisible = true
    })
    this.$eventBus.$on('updateIsLock',(val)=>{
      this.updateIsLock(val)
    })
    this.$eventBus.$on('changeRefresh',(type,index,val)=>{
      this.$set(this[type][index],'refresh',val)
    })
    this.$eventBus.$on('openActionConfig',(value)=>{
      this.$refs.actionConfig.open(value)
    })
    this.$eventBus.$on('deleteCom',(value)=>{
      const id = value.i
      const key = value.position === 1 ? 'list' : 'affix'
      const index = this[key].findIndex(item => item.i === id)
      this[key].splice(index,1)
      if(this.editingId == value.i) {
        this.$refs.materialConfig.close()
        this.editingId = ''
      }
    })
    this.$eventBus.$on('editComponent',(value)=>{
      this.editComponent(value)
    })
    this.$eventBus.$on('copyCom',(value)=>{
      const id = value.i
      const key = value.position === 1 ? 'list' : 'affix'
      const index = this[key].findIndex(item => item.i === id)
      let copyItem = JSON.parse(JSON.stringify(this[key][index]))
      copyItem.i = uid()
      this[key].splice(index+1, 0, copyItem)
    })
    this.$eventBus.$on('updateActionElement',(val)=>{
      this.actionElement = val
    })
    this.$eventBus.$on('updateActionPopover',(val,event)=>{
      this.$refs.actionPopover.toggle(val,event.target)
    })
    this.$eventBus.$on('searchClick',(val)=>{
      this.$emit('searchClick',val)
    })
  },
  computed:{
    isMobilePage(){
        let flag = navigator.userAgent.match(/(phone|pad|pod|iPhone|iPod|ios|iPad|Android|Mobile|BlackBerry|IEMobile|MQQBrowser|JUC|Fennec|wOSBrowser|BrowserNG|WebOS|Symbian|Windows Phone)/i)
        return flag;
    },
  },
  methods: {
    materialSelect(item){
      // console.log(item)
      this.$refs.materialConfig.open(item)
    },
    stopeContextMenu(e){
      if(!this.isReadOnly){
        e.preventDefault();
      }
    },
    popMenu(e){
      return false
      if(!this.isReadOnly){
        let self = this;
        if(e.button ===2){
          // let x = e.layerX;
          // let y = e.layerY;
          let x = e.clientX
          let y = e.clientY
          this.menuList = this.popItems
          this.mousePosition = [x, y];
          this.mousePositionVisible = true
          this.$eventBus.$emit('hideAllRightMenu')
        }else if(e.button ===0){
          this.mousePositionVisible = false
          this.$eventBus.$emit('hideAllRightMenu')
        }
      }
    },
    showAddDialog(){
      // this.$refs.baseConfig.open()
      this.$refs.cardMaterialSelector.open()
    },
    showGlobalConfig(){
      this.globalConfigVisible = true
    },
    showAuxiliaryConfig(){

    },
    showEditDialog(val){
      // this.$refs.baseConfig.open(val)
      this.$refs.materialConfig.open(val)
    },
    updateIsLock(val){
      this.pageIsLock = val
      this.$emit("update:isLock",val)
      this.$emit("updateIsLock",val)
    },

    editComponent (value) {
      this.editingId = value.i
      const id = value.i
      const key = value.position === 1 ? 'list' : 'affix'
      const index = this[key].findIndex(item => item.i === id)
      if(~index) {
        this.$set(this[key],index,value)
      }
      this.$refs.gridLayout.updateRenderData(value)
    },
    addComponent(value){
      const key = value.position === 1 ? 'list' : 'affix'
      if(value.position === 1) {
        value.x = 0
        value.y = 0
      }
      this[key].unshift(value)
      this.$nextTick(() => {
        this.$refs.gridLayout.toolClick('edit', 'list', value, 0)
        this.editingId = value.i
      })
    },
    editAffixRectInfo(value){
      const { i, x, y, w, h } = value
      const index = this.affix.findIndex(item => item.i === i)
      if (~index) {
        this.affix[index].w = w
        this.affix[index].h = h
        if (x && y) {
          (this.affix[index].affixInfo).x = x;
          (this.affix[index].affixInfo).y = y;
        }
      }
    },
    getOptions(){
      return {
        list:this.list,
        affix:this.affix,
        global:this.globalFormData
      }
    },
    handleSave(oprate){
      this.$emit("handleSave",this.getOptions(), oprate)
    },
    roleConfig () {
      this.$emit("roleConfig", this.getOptions())
    },
    openEvent (data) {
      this.$emit('openEvent', data)
    },
    materialConfigClose () {
      this.materialConfigCloseRadom = Math.random()
    }
  }
};
</script>

<style scoped lang="scss">
.page{
  width: 100%;
  min-height: 100%;
  height: 100%;
  position: relative;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  .page-top{
    height: 56px;
    overflow: hidden;
    background: #F5F6F7;
    .show{
      width: calc(100% - 20px);
      border-radius: 0 4px 4px 4px;
      background: #fff;
      height: 100%;
      padding: 0 23px;
      display: flex;
      align-items: center;
      box-sizing: border-box;
      .user-info{
        margin-right: 32px;
        word-break: keep-all;
        white-space: nowrap;
        span{
          font-family: Microsoft YaHei-Bold, Microsoft YaHei;
          font-weight: 700;
          font-size: 14px;
          color: #363B4C;
        }
      }
      .home-list{
        display: flex;
        align-items: center;
        max-width: calc(100% - 92px);
        overflow: hidden;
        overflow-x: auto;
        .home-list-item{
          padding: 0 6px;
          min-width: 80px;
          height: 32px;
          line-height: 32px;
          border-radius: 4px;
          background: #F5F6F7;
          box-sizing: border-box;
          text-align: center;
          font-family: Microsoft YaHei-Regular, Microsoft YaHei;
          font-weight: 400;
          font-size: 12px;
          color: #363B4C;
          cursor: pointer;
          &+.home-list-item{
            margin-left: 16px;
          }
          &.active{
            background: #DDEAFF;
            color: #1E6FFF;
          }
        }
      }
      .manage-tool{
        display: flex;
        align-items: center;
        margin-left: 16px;
        cursor: pointer;
        .normal-svg-icon{
          display: block;
          width: 16px;
          height: 16px;
          margin-right: 4px;
        }
        span{
          font-family: Microsoft YaHei-Regular, Microsoft YaHei;
          font-weight: 400;
          font-size: 14px;
          color: #363B4C;
        }
      }
    }
    .design{
      width: 100%;
      height: 100%;
      background: #fff;
      padding: 0 24px;
      display: flex;
      align-items: center;
      box-sizing: border-box;
      justify-content: space-between;
      .home-name{
        width: 176px;
        /deep/.el-input{
          .el-input__inner{
            height: 32px;
            line-height: 32px;
            border: 0;
            background: #F5F6F7;
            font-family: Microsoft YaHei-Regular, Microsoft YaHei;
            font-weight: 400;
            font-size: 14px;
            color: #363B4C;
            padding: 0;
            padding-left: 8px;
          }
        }
        .show-name{
          width: 100%;
          height: 32px;
          line-height: 32px;
          background: #F5F6F7;
          font-family: Microsoft YaHei-Regular, Microsoft YaHei;
          font-weight: 400;
          font-size: 14px;
          color: #363B4C;
          text-indent: 8px;
          position: relative;
          border-radius: 4px;
          .edit-name-icon{
            position: absolute;
            top: 9px;
            right: 14px;
            display: block;
            width: 14px;
            height: 14px;
            cursor: pointer;
            fill: #1E6FFF;
          }
        }

      }
      .design-tool{
        position: relative;
        display: flex;
        align-items: center;
        /deep/#gooey-meun-page{
          .menu{
            position: unset;
          }
        }
        .right-button{
          display: flex;
          align-items: center;
          margin-left: 8px;
        }
      }
    }
  }
  .page-content{
    flex: 1;
    background-color: #F5F6F7;
    width: 100%;
    position: relative;
    overflow: hidden;
    overflow-y: auto;
    &::-webkit-scrollbar{
      display: none;
    }
  }
}
.action-popover-wrapper {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
  .bg {
    position: absolute;
    width: 100%;
    height: 100%;
    left: 0;
    top: 0;
    background-size: cover;
  }
}
</style>
