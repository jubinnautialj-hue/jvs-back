<template>
  <div class="menu-wrapper">
    <div class="notmove local-menu" v-if="appSettingOpen">
      <div v-for="(item, key) in localMenu" :key="key">
        <div class="menu-item-head" @click="openClose(item)">
          <svg :class="{'openclose-icon': true, 'trans': item.closeStatus}" aria-hidden="true">
            <use xlink:href="#jvs-ui-icon-a-zu6027"></use>
          </svg>
          <svg v-if="item.icon" class="svg-icon" aria-hidden="true" style="width: 16px;height: 16px;margin-right: 8px;">
            <use :xlink:href="`#${item.icon}`"></use>
          </svg>
          <i v-else class="el-icon-menu" style="font-size: 18px;margin-right: 8px;"></i>
          <span>{{ $langt(`localMenu.${item.langKey}`) || item.name }}</span>
        </div>
        <el-collapse-transition>
          <div v-show="!item.closeStatus">
            <template v-for="(it, itindex) in item.children">
              <el-menu-item
                :key="'navmenu'+itindex"
                :index="it.extend.url"
                v-if="specialRoute.indexOf(it.extend.url) > -1 ? (jvsDesign && jvsDesign.JVS_DESIGN_MGR) : true"
                @click="open(it.extend, it)"
                :class="{'menu-item-li':true,'is-active-item':vaildAvtive(it.extend)}"
              >
                <el-popover v-if="collapse && (it.extend.icon || !(($langt(`localMenu.${it.langKey}`) || it.extend.name) && ($langt(`localMenu.${it.langKey}`) || it.extend.name).length > 0))" placement="right" trigger="hover" :content="$langt(`localMenu.${it.langKey}`) || it.extend.name" popper-class="custom-right-tool-poper">
                  <div slot="reference">
                    <svg v-if="it.extend.icon" class="svg-icon" aria-hidden="true" style="width: 16px;height: 16px;">
                      <use :xlink:href="`#${it.extend.icon}`"></use>
                    </svg>
                    <div v-else class="word-icon">{{$langt(`localMenu.${it.langKey}`)[0] || it.extend.name[0]}}</div>
                  </div>
                </el-popover>
                <div v-else>
                  <svg v-if="it.extend.icon" class="svg-icon" aria-hidden="true" style="width: 16px;height: 16px;margin-right: 8px;">
                    <use :xlink:href="`#${it.extend.icon}`"></use>
                  </svg>
                  <b v-else class="local-menu-item-dot"></b>
                  <span :title="$langt(`localMenu.${it.langKey}`) || it.extend.name" slot="title" :alt="it.extend.url">{{$langt(`localMenu.${it.langKey}`) || it.extend.name}}</span>
                </div>
              </el-menu-item>
            </template>
          </div>
        </el-collapse-transition>
      </div>
    </div>
    <div class="notmove" v-if="!appSettingOpen" :key="menuchangerandom">
      <template v-if="getStore({ name: 'modeUserInfo' }).mode !== 'GA'">
        <draggable v-model="getMenu" :options="{group:'menu-move', disabled: !getRole,ghostClass:'menu-ghost',}"  @end="menuSort" id="menu-sort">
          <sidebarItemNode
            v-for="(item, index) in getMenu"
            :key="'menuNav'+index"
            :item="item"
            :index="index"
            :collapse="collapse"
            :appItem="appItem"
            :labelKey="labelKey"
            :pathKey="pathKey"
            :nowTagValue="nowTagValue"
            :parentSort="0"
            :indexString="'0'"
            :openMenuId="openMenuId"
            @openCatalogue="openCatalogue"
            @open="open"
            @moreHandle="moreHandle"
            @handleLearn="handleLearn"
            @handleCreate="handleCreate"
            @handleDesign="handleDesign"
            @childrenEnd="childrenEnd"
            @addCatalogue="addCatalogue"
          ></sidebarItemNode>
        </draggable>
      </template>
      <template v-else>
        <sidebarItemNode
          v-for="(item, index) in getMenu"
          :key="'menuNav'+index"
          :item="item"
          :index="index"
          :collapse="collapse"
          :appItem="appItem"
          :labelKey="labelKey"
          :pathKey="pathKey"
          :nowTagValue="nowTagValue"
          :parentSort="0"
          :indexString="'0'"
          :openMenuId="openMenuId"
          @openCatalogue="openCatalogue"
          @open="open"
          @moreHandle="moreHandle"
          @handleLearn="handleLearn"
          @handleCreate="handleCreate"
          @handleDesign="handleDesign"
          @childrenEnd="childrenEnd"
          @addCatalogue="addCatalogue"
        ></sidebarItemNode>
      </template>
    </div>

    <!-- 应用类移动位置 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      append-to-body
      :before-close="handleClose">
      <div v-if="dialogVisible">
        <jvs-form :formData="formData" :option="formOption" @submit="submitHandle" @cancalClick="handleClose"></jvs-form>
      </div>
    </el-dialog>
    <!-- 自定义页面修改 -->
    <el-dialog
      title="修改页面"
      :visible.sync="pageVisible"
      append-to-body
      :before-close="handleClosePage">
      <div v-if="pageVisible">
        <jvs-form :formData="pageFormData" :option="pageOption" @submit="submitPage" @cancalClick="handleClosePage"></jvs-form>
      </div>
    </el-dialog>
    <!-- 设置页面为首页 -->
    <el-dialog
      title="设置首页"
      :visible.sync="urlVisible"
      append-to-body
      width="600px"
      :before-close="handleCloseUrl">
      <el-form ref="urlForm" :model="urlFormData" :rules="urlFormRule" label-width="80px">
        <el-form-item label="用户等级" prop="userLevel">
          <el-select style="width: 400px" size="mini" v-model="urlFormData.userLevel" placeholder="请选择用户等级">
            <el-option
              v-for="item in userLevelOption"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button size="mini" type="primary" @click="submitUrl">确定</el-button>
          <el-button size="mini" @click="handleCloseUrl">取消</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>
<script>
import {mapGetters} from "vuex";
import { validatenull } from "@/util/validate";
import config from "./config.js";
import {sortMenuList,draggableMenuSort, mobilePcDisplay} from '@/api/admin/menu'
import {editCataType, delCataType, editDesign, delDesign} from '@/components/template/api'
import {addForm} from "@/views/page/api/formlist";
import {createPage, editCustomPage} from "@/views/page/api/newDesign";
import { localMenu } from './localMenu.js'
import {quickCreateModel} from "@/views/flowable/api/flowable";
import {getUserLevelList, edit} from "@/views/upms/views/userLevel/api";
import { getStore } from "@/util/store.js";
import sidebarItemNode from './sidebarItemNode.vue';
export default {
  name: "sidebarItem",
  components: {
    sidebarItemNode
  },
  data () {
    return {
      config: config,
      falgs: 'movemenu',
      disabled: false,
      sortList: [],
      dialogTitle: '',
      dialogType: '',
      dialogVisible: false,
      urlVisible: false,
      userLevelList: [],
      userLevelOption: [],
      indexUrl: '',
      pageVisible: false,
      itemData: null,
      urlFormData: {
        userLevel: ''
      },
      urlFormRule: {
        userLevel: [{ required: true, message: '请选择用户等级', trigger: 'change' }],
      },
      pageFormData: {},
      pageOption: {
        emptyBtn: false,
        submitBtnText: '确定',
        submitLoading: false,
        column: [
          {
            label: "页面名称",
            prop: "name",
            searchSpan: 4,
            search: true,
            rules: [
              { required: true, message: '请输入页面名称', trigger: 'blur' },
              // { max: 6, message: '名称不得超过6个字符', trigger: 'blur' }
            ],
          },
          {
            label: "目标地址",
            prop: "url",
            searchSpan: 4,
            search: true,
            type: 'select',
            filterable: true,
            allowcreate: true,
            dicUrl: `/mgr/jvs-auth/environment/variable/all/text/${getStore({ name: 'modeUserInfo' }).mode}`,
            rules: [
              { required: true, message: '请输入目标地址', trigger: 'blur' },
            ],
            tipsEnable:true,
            tips:{
              position:'bottom',
              text:`<div >
                <div style="height:20px;display:flex;align-items:center;"><span style="min-width:40px;">示例：</span>/app/source/appIdentificationdemo/list</div>
                <div style="height:20px;display:flex;align-items:center;padding-left:40px;">http://10.0.0.2:9999/list</div>
                <div style="height:20px;display:flex;align-items:center;padding-left:40px;">http://www.baidu.com/</div>
              </div>`
            }
          },
          {
            label:'权限标识',
            prop:'operation',
            formSlot:true,
            displayExpress:[
              {prop:"operation",value:"notZeroLength"}
            ]
          },
          {
            label: "描述",
            prop: "description",
            searchSpan: 4,
            search: true
          },
          {
            label: "图标",
            prop: "icon",
            type: 'iconSelect'
          },
        ]
      },
      formData: {},
      formOption: {
        emptyBtn: false,
        formAlign: 'top',
        submitBtnText: '确定',
        column: [
          {
            label: '选择目录',
            prop: 'type',
            type: 'cascader',
            datatype: 'option',
            multiple: false,
            emitPath: false,
            emitKey: 'id',
            dicData: [],
            rules: [{ required: true, message: '目录不能为空', trigger: 'change' }],
            display: true
          },
          {
            label: '名称',
            prop: 'name',
            rules: [
              { required: true, message: '名称不能为空', trigger: 'blur' },
              // { max: 6, message: '名称不得超过6个字符', trigger: 'blur' }
            ],
            display: true
          },
          {
            label: '图标',
            prop: 'icon',
            type: 'iconSelect'
          },
        ]
      },
      specialRoute: ['/jvs-upms-ui/appOperationLog', '/jvs-upms-ui/systemspace', '/jvs-upms-ui/autoExtend', '/jvs-upms-ui/appDictionary'],
      permissionsList: [],
      brforeMenu: [],
      menuchangerandom: -1,
      openMenuId: '',
      getStore,
    };
  },
  props: {
    menu: {
      type: Array
    },
    screen: {
      type: Number
    },
    first: {
      type: Boolean,
      default: false
    },
    props: {
      type: Object,
      default: () => {
        return {};
      }
    },
    collapse: {
      type: Boolean
    },
    appItem: {
      type: Object
    },
    jvsDesign: {
      type: Object
    }
  },
  created () {
    this.permissionsList = getStore({name: 'permissions'})
  },
  mounted () {
    // 防止火狐浏览器拖拽的时候以新标签打开
    document.body.ondrop = function (event) {
      event.preventDefault()
      event.stopPropagation()
    }
  },
  computed: {
    ...mapGetters(["roles", "appSettingOpen", "menuType"]),
    labelKey () {
      return this.props.label||this.config.propsDefault.label;
    },
    pathKey () {
      return this.props.path||this.config.propsDefault.path;
    },
    iconKey () {
      return this.props.icon||this.config.propsDefault.icon;
    },
    nowTagValue () {
      return this.$router.$jvsRouter.formatMenuPath(this.$route);
    },
    getRole(){
      return this.appItem?.extend?.designRole || false
    },
    localMenu () {
      let temp = this.menuType === 'platform' ? localMenu.platformMenu : localMenu.systemMenu
      let list = []
      temp.filter(tp => {
        if(tp.children && tp.children.length > 0) {
          let obj = {
            name: tp.name,
            icon: tp.icon,
            langKey: tp.langKey,
            children: []
          }
          tp.children.filter(cit => {
            if(this.permissionsList.indexOf(cit.permisionFlag) > -1 || !cit.permisionFlag) {
              if(cit.extend && cit.extend.url == '/jvs-upms-ui/devconfig') {
                if(this.$store.getters.userInfo.platformAdmin) {
                  obj.children.push(cit)
                }
              }else{
                obj.children.push(cit)
              }
            }
          })
          if(obj.children && obj.children.length > 0) {
            list.push(obj)
          }
        }
      })
      return list
    },
    getMenu:{
      get(){
        return this.menu || []
      },
      set(val){
        this.$emit('changeMenu',val)
      }
    }
  },
  methods: {
    // 关闭自定义页面设置弹窗
    handleClosePage() {
      this.pageVisible = false
    },
    // 修改自定义页面 提交
    submitPage(form) {
      editCustomPage(form.jvsAppId, form).then(res => {
        if (res.data && res.data.code == 0) {
          this.pageVisible = false
          // this.$message.success('修改成功')
          this.$notify({
            title: '提示',
            message: '修改成功',
            position: 'bottom-right',
            type: 'success'
          });
          this.$store.commit("SET_SYSTEM", form.jvsAppId)
          this.$emit('freshAllMenu', true)
        }
      })
    },
    // 了解数据模型
    handleLearn(str) {
      this.$openUrl('', '_blank', str)
    },
    // 创建设计
    handleCreate(type, it) {
      const params = {
        name: '',
        jvsAppId: it.extend.jvsAppId,
        dataModelId: it.extend.dataModelId,
        type: (['form', 'list'].indexOf(type) > -1) ? it.parentId : it.extend.type
      }
      if (type === 'form') {
        params.name = '未命名表单'
        addForm(it.extend.jvsAppId, params, {sourceDesignId: it.id}).then(res => {
          if (res.data && res.data.code == 0) {
            const str = location.origin + (`/page-design-ui/#/form?jvsAppId=${it.extend.jvsAppId}&id=`+res.data.data.id + (res.data.data.dataModelId ? `&dataModelId=${res.data.data.dataModelId}` : ''))
            this.$openUrl(str, '_blank')
            this.$store.commit("SET_SYSTEM", it.extend.jvsAppId)
            this.$emit('freshAllMenu', true)
          }
        })
      }
      if (type === 'list') {
        params.name = '未命名列表'
        createPage(it.extend.jvsAppId, params, {sourceDesignId: it.id}).then(res => {
          if (res.data && res.data.code == 0) {
            const str = location.origin + (`/page-design-ui/#/crud/design?jvsAppId=${it.extend.jvsAppId}&id=`+res.data.data.id + (res.data.data.dataModelId ? `&dataModelId=${res.data.data.dataModelId}` : ''))
            this.$openUrl(str, '_blank')
            this.$store.commit("SET_SYSTEM", it.extend.jvsAppId)
            this.$emit('freshAllMenu', true)
          }
        })
      }
      if (type === 'flow') {
        params.name = '未命名流程'
        quickCreateModel(params, {sourceDesignId: it.id}).then(res => {
          if (res.data && res.data.code == 0) {
            this.$openUrl(`/flowable-ui/#/processDesign?jvsAppId=${it.extend.jvsAppId}&id=${res.data.data.id}`, '_blank')
          }
        })
      }
    },
    handleDesign(obj) {
      let str = ''
      if (obj.extend && obj.extend.design) {
        switch (obj.extend.design) {
          case 'page':
            str = location.origin + (`/page-design-ui/#/crud/design?jvsAppId=${obj.extend.jvsAppId}&id=`+obj.extend.id + (obj.extend.dataModelId ? `&dataModelId=${obj.extend.dataModelId}` : ''))
            this.$openUrl(str, '_blank')
            break
          case 'form':
            str = location.origin + (`/page-design-ui/#/form?jvsAppId=${obj.extend.jvsAppId}&id=`+obj.extend.id + (obj.extend.dataModelId ? `&dataModelId=${obj.extend.dataModelId}` : ''))
            this.$openUrl(str, '_blank')
            break
          case 'chart':
            str = location.origin + (`/chart-design-ui/#/chartDesign?jvsAppId=${obj.extend.jvsAppId}&id=`+obj.extend.id)
            this.$openUrl(str, '_blank')
            break
          case 'screen':
            str = location.origin + (`/data-screen-ui/#/datascreendesign?id=${obj.extend.id}`)
            this.$openUrl(str, '_blank')
            break
          case 'report':
            str = location.origin + (`/chart-design-ui/#/data-report/datareportdesign?id=${obj.extend.id}`)
            this.$openUrl(str, '_blank')
            break
          default: ;break;
        }
      }
    },
    vaildAvtive (item) {
      const groupFlag=(item["group"]||[]).some(ele =>
        this.$route.path.includes(ele)
      );
      if(item[this.pathKey]||groupFlag) {
        if(item.design == 'URL') {
          let boolTemp = false
          if(location.hash.includes('src=')) {
            let src = location.hash.split('src=')[1]
            if(item[this.pathKey] == decodeURIComponent(src).split('?')[0] || ('/app'+item[this.pathKey]) == decodeURIComponent(src).split('?')[0] || ('/jvs-ui'+item[this.pathKey]) == decodeURIComponent(src).split('?')[0]) {
              boolTemp = true
            }
          }
          return boolTemp
        }else{
          return this.nowTagValue===item[this.pathKey]||groupFlag;
        }
      }else{
        if(this.nowTagValue.includes('?') && this.nowTagValue.startsWith('/') && !this.nowTagValue.startsWith('/app/') && !this.nowTagValue.startsWith('/jvs-ui/')) {
          let tp = this.nowTagValue.split('?')[1]
          let tarr = tp.split('&')
          let boolTemp = true
          for(let i in tarr) {
            let oba = tarr[i].split('=')
            if(item[oba[0]] != oba[1] && JSON.stringify(item[oba[0]]) != oba[1]) {
              boolTemp = false
            }
          }
          return boolTemp
        }else{
          return false
        }
      }
    },
    isSvg(item) {
      if (item) {
        return true // item.indexOf("icon-") === -1
      }
    },
    vaildRoles (item) {
      item.meta=item.meta||{};
      return item.meta.roles? item.meta.roles.includes(this.roles):true;
    },
    validatenull (val) {
      return validatenull(val);
    },
    open (item, data) {
      this.openMenuId = item.id
      this.$emit('openCatalogue', null)
      let itemUrl = item.url
      if (item.design) {
        let url = ''
        let str = ''
        switch (item.design) {
          case 'chart':
            url = `/chart-design-ui/chartShow?type=pc&id=${item.id}&jvsAppId=${item.jvsAppId}`;
            break;
          case 'page':
            url = `/page-design-ui/list/use?id=${item.id}&dataModelId=${item.dataModelId}&jvsAppId=${item.jvsAppId}`;
            break;
          case 'form':
            url = `/page-design-ui/form/use?id=${item.id}&dataModelId=${item.dataModelId}&jvsAppId=${item.jvsAppId}`;
            break;
          case 'screen':
            url = `/data-screen-ui/#/screenPreview?id=${item.id}&isPreview=true`;
            break;
          case 'URL':
            let addApp = true
            let addJvs = true
            let addAppId = true
            let addMenuId = false
            if(item.variable) {
              itemUrl = item.variable
            }
            if(itemUrl && itemUrl.includes('#')) {
              if(['/jvs-ui/'].indexOf(itemUrl.split('#')[0]) > -1) {
                addApp = false
              }
              if(itemUrl.includes('/jvs-ui')) {
                addJvs = false
              }
            }else{
              // 源码开发路由挂载
              let urlStart = ''
              if(itemUrl.includes('-ui')) {
                urlStart = itemUrl.split('-ui')[0] + '-ui'
              }
              //第五步 源代码接入 demoIndex 根据路由前缀解析url进行跳转
              if(['/jvs-devDemoView-ui', '/jvs-demo-ui'].indexOf(urlStart) > -1) {
                addApp = false
                addJvs = false
                addAppId = false
                addMenuId = true
              }
              // 无源码接入 根据固定路由前缀拼装地址
              if(itemUrl.startsWith('/app/source')) {
                addApp = false
                addJvs = false
                itemUrl = location.origin + itemUrl
              }
            }
            url = itemUrl.startsWith('http') ? itemUrl : ((addApp ? '/app' : (addJvs ? '/jvs-ui' : '')) + itemUrl)
            if(addAppId) {
              if(url.includes('?')) {
                url += `&jvsAppId=${item.jvsAppId}`
              }else{
                url += `?jvsAppId=${item.jvsAppId}`
              }
            }
            if(addMenuId) {
              if(url.includes('?')) {
                url += `&openMenuId=${item.id}`
              }else{
                url += `?openMenuId=${item.id}`
              }
            }
            break;
          case 'report':
            url = `/chart-design-ui/data-report/viewdatareport?type=pc&id=${item.id}&jvsAppId=${item.jvsAppId}`;
            break;
          default: ;break;
        }
        if (str !== '') {
          return
        }
        let tmps = ""
        if(url.indexOf('#') > -1){
          tmps = (url && ('#' + url.split('#')[1])) || ''
        }else{
          let indx = url.indexOf('-ui')
          if(indx > -1) {
            tmps = url.slice(0, indx+3) + '/#' + url.slice(indx+3, url.length)
          }
        }
        if(this.$route.hash && this.$route.query && this.$route.query.src  && tmps == (this.$route.query.src  + this.$route.hash)) {
          return false
        }
        // console.log(item.name,url)
        this.$router.push({
          path: this.$router.$jvsRouter.getPath({
            name: item.name,
            src: url
          }),
        })
        return
      }
      // 重复点击不跳转
      let tempStr = ""
      if(itemUrl.indexOf('#') > -1){
        tempStr = (itemUrl && ('#' + itemUrl.split('#')[1])) || ''
      }else{
        let indx = itemUrl.indexOf('-ui')
        if(indx > -1) {
          tempStr = itemUrl.slice(0, indx+3) + '/#' + itemUrl.slice(indx+3, itemUrl.length)
        }
      }
      if(this.$route.hash && this.$route.query && this.$route.query.src  && tempStr == (this.$route.query.src  + this.$route.hash)) {
        return false
      }
      if (this.screen<=1) this.$store.commit("SET_COLLAPSE");
      this.$router.$jvsRouter.group=item.group;
      if(item.newWindow === true) {
        if(itemUrl.includes('http') || itemUrl.includes('https') || itemUrl.includes('ftp')) {
          this.$openUrl(item[this.pathKey], '_blank')
        }else{
          if(itemUrl.indexOf('-ui') == -1) {
            this.$openUrl(item[this.pathKey], '_blank')
          }else{
            let tinx = itemUrl.indexOf('-ui')
            let tpStr = itemUrl.slice(0, (tinx+3)) + '/#' + itemUrl.slice(tinx+3, itemUrl.length)
            this.$openUrl(tpStr, '_blank')
          }
        }
      }else{
        this.$router.push({
          path: this.$router.$jvsRouter.getPath({
            name: item[this.labelKey],
            src: item[this.pathKey]
          }),
          query: item.query,
          params: item.params
        })
      }
    },
    startMove (event) {
      this.falgs = ''
      this.sortList = JSON.parse(JSON.stringify(this.menu))
    },
    endMove (ev) {
      this.falgs = 'movemenu'
      let temp = []
      for(let i in this.menu) {
        this.menu[i].sort =  i
        temp.push({
          menuId: this.menu[i].id,
          sort: i
        })
      }
      // 权限---修改菜单顺序
      if(this.permissionsList.indexOf("upms_mgr_xiu_gai_cai_dan_shun_xu") > -1) {
        sortMenuList(temp).then(res => {
          if(res.data.code == 0) {
            console.log('成功')
          }
        }).catch(e => {
          this.menu = JSON.parse(JSON.stringify(this.sortList))
        })
      }
    },
    // 打开目录
    openCatalogue (item) {
      if(this.appItem && item.extend.designRole) {
        this.$emit('openCatalogue', item)
      }
    },
    // 应用更多操作
    moreHandle (type, item) {
      switch(type) {
        case 'del':
          let str = '确定删除此'
          if(item.extend && item.extend.design) {
            // str += '页面'
            str = '删除后关联的页面或功能将无法使用，请谨慎操作！'
          }else{
            str += '目录？'
          }
          this.$confirm(`${str}`, '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            if(item.extend && item.extend.design) {
              // 页面
              delDesign({appId: this.appItem.id, designId: item.id, designType: item.extend.design}).then(res => {
                if(res.data && res.data.code == 0) {
                  this.$notify({
                    title: '提示',
                    message: '删除成功',
                    position: 'bottom-right',
                    type: 'success'
                  });
                  this.$store.commit("SET_SYSTEM", this.appItem.id)
                  this.$emit('freshAllMenu', true)
                }
              })
            }else{
              delCataType({appId: this.appItem.id, id: item.id}).then(res => {
                if(res.data && res.data.code == 0) {
                  this.$notify({
                    title: '提示',
                    message: '删除成功',
                    position: 'bottom-right',
                    type: 'success'
                  });
                  this.$store.commit("SET_SYSTEM", this.appItem.id)
                  this.$emit('freshAllMenu', true)
                }
              })
            }
          }).catch(e => {});
          break;
        case 'move':
          this.dialogType = type;
          this.dialogTitle = '移动';
          this.formOption.column.filter(col => {
            if(col.prop == 'type') {
              col.display = true
              let dicData = []
              let obj = {
                id: this.appItem.id,
                name: this.appItem.name,
                children: []
              }
              this.getMoveTree(this.menu, obj, item.extend.parentId, item.id)
              dicData = [obj]
              col.dicData = dicData
            }else{
              col.display = false
            }
          })
          this.itemData = item
          item.type = item.extend.parentId || this.appItem.id
          this.formData = JSON.parse(JSON.stringify(item))
          this.dialogVisible = true;
          break;
        case 'rename':
          this.dialogType = type;
          this.dialogTitle = '重命名';
          this.formOption.column.filter(col => {
            if(col.prop == 'name' || col.prop == 'icon') {
              col.display = true
              if(col.prop == 'icon' && item.extend && item.extend.type != 'directory') {
                col.display = false
              }
            }else{
              col.display = false
            }
          })
          this.itemData = item
          this.formData = JSON.parse(JSON.stringify(item))
          this.formData.icon = JSON.parse(JSON.stringify(item)).extend.icon
          this.dialogVisible = true;
          break;
        case 'edit':
          this.dialogType = type;
          this.itemData = item
          this.pageFormData = {
            id: item.id,
            name: item.name,
            type: item.parentId || item.type,
            url: item.extend.url,
            description: item.extend.description || '',
            icon: item.extend.icon || '',
            jvsAppId: item.extend.jvsAppId
          }
          this.pageVisible = true;
          break;
        case 'index':
          let url = ''
          if(item.extend){
            switch(item.extend.design) {
              case 'URL':
                url = item.extend.url;
                break;
              // case 'page':
              //   url = `/page-design-ui/#/list/use?id=${item.id}&dataModelId=${item.extend.dataModelId}&jvsAppId=${item.extend.jvsAppId}`;
              //   break;
              // case 'form':
              //   url = `/page-design-ui/#/form/use?id=${item.id}&dataModelId=${item.extend.dataModelId}&jvsAppId=${item.extend.jvsAppId}`;
              //   break;
              case 'chart':
                url = `/chart-design-ui/#/chartShow?type=pc&id=${item.id}&jvsAppId=${item.extend.jvsAppId}`;
                break;
               // case 'screen':
               //  url = `/jvs-report-ui/#/report/screenDesign?jvsAppId=${item.extend.jvsAppId}&id=` + item.id;
               //  break;
              case 'report':
                url = `/chart-design-ui/#/data-report/viewdatareport?type=pc&id=${item.id}&jvsAppId=${item.extend.jvsAppId}`;
                break;
              default: ;break;
            }
          }
          this.indexUrl = url
          getUserLevelList().then(res => {
            if (res.data.code==0) {
              this.userLevelList = res.data.data || []
              this.userLevelOption = res.data.data.map(item => {
                return {
                  label: item.name,
                  value: item.name
                }
              })
              this.urlVisible = true
            }
          })
          // this.$confirm('确认设置为首页？', '提示', {
          //   confirmButtonText: '确定',
          //   cancelButtonText: '取消',
          //   type: 'warning'
          // }).then(() => {
          //   setIndexPage({url: url}).then(res => {
          //     if(res.data && res.data.code == 0) {
          //       this.$message.success('设置首页成功')
          //     }
          //   })
          // }).catch(e => {})
          break;
        case 'pc':
          mobilePcDisplay(item.extend.jvsAppId, {
            appId: item.extend.jvsAppId,
            designId: item.extend.id,
            designType: item.extend.design,
            mobileDisplay: item.extend.mobileDisplay,
            pcDisplay: item.extend.pcDisplay === false ? true : false
          }).then(res => {
            if(res.data && res.data.code == 0) {
              // this.$message.success('设置成功')
              this.$notify({
                title: '提示',
                message: '设置成功',
                position: 'bottom-right',
                type: 'success'
              });
              item.extend.pcDisplay = item.extend.pcDisplay === false ? true : false
              this.$forceUpdate()
            }
          })
          break;
        case 'mobile':
          mobilePcDisplay(item.extend.jvsAppId, {
            appId: item.extend.jvsAppId,
            designId: item.extend.id,
            designType: item.extend.design,
            mobileDisplay: item.extend.mobileDisplay === false ? true : false,
            pcDisplay: item.extend.pcDisplay
          }).then(res => {
            if(res.data && res.data.code == 0) {
              // this.$message.success('设置成功')
              this.$notify({
                title: '提示',
                message: '设置成功',
                position: 'bottom-right',
                type: 'success'
              });
              item.extend.mobileDisplay = item.extend.mobileDisplay === false ? true : false
              this.$forceUpdate()
            }
          })
          break;
        default: ;break;
      }
    },
    // 关闭设置首页弹窗
    handleCloseUrl() {
      this.urlVisible = false
      this.urlFormData = {
        userLevel: ''
      }
    },
    // 提交设置首页
    submitUrl() {
      this.$refs.urlForm.validate((valid) => {
        if (valid) {
          const index = this.userLevelList.findIndex(item => {
            return item.name = this.urlFormData.userLevel
          })
          this.userLevelList[index].indexUrl = this.indexUrl
          this.$confirm('确认设置为首页？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            edit(this.userLevelList[index]).then(res => {
              if(res.data && res.data.code == 0) {
                // this.$message.success('设置首页成功')
                this.$notify({
                  title: '提示',
                  message: '设置首页成功',
                  position: 'bottom-right',
                  type: 'success'
                });
                this.handleCloseUrl()
              }
            })
          }).catch(e => {})
        } else {
          return false;
        }
      });
    },
    // 关闭弹框
    handleClose () {
      this.dialogType = ''
      this.dialogTitle = ''
      this.dialogVisible = false
      this.formData = {}
      this.itemData = null
    },
    // 移动  重命名  提交
    submitHandle () {
      // 页面
      if(this.formData.extend && this.formData.extend.design) {
        let obj = {
          appId: this.formData.extend.jvsAppId,
          designId: this.formData.id,
          designType: this.formData.extend.design,
          name: this.formData.name,
          icon: this.formData.icon
        }
        if(this.dialogType == 'rename') {
          obj.type = this.formData.parentId // extend.type
        }else{
          obj.type = this.formData.type
        }
        editDesign(obj).then(res => {
          if(res.data && res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: `${this.dialogType == 'rename' ? '重命名' : '移动'}成功`,
              position: 'bottom-right',
              type: 'success'
            });
            this.handleClose()
            this.$store.commit("SET_SYSTEM", obj.appId)
            this.$emit('freshAllMenu', true)
          }
        })
      }else{
        let obj = {}
        if(this.dialogType == 'rename') {
          obj = {
            appId: this.appItem.id,
            newType: this.formData.name,
            type: this.itemData.name,
            icon: this.formData.icon,
            parentId: this.formData.parentId,
            id: this.itemData.id
          }
        }else{
          obj = {
            appId: this.appItem.id,
            newType: this.formData.name,
            type: this.itemData.name,
            icon: this.formData.icon,
            parentId: this.formData.type,
            id: this.itemData.id
          }
        }
        editCataType(obj).then(res => {
          if(res.data && res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: `${this.dialogType == 'rename' ? '重命名' : '移动'}成功`,
              position: 'bottom-right',
              type: 'success'
            });
            this.handleClose()
            this.$store.commit("SET_SYSTEM", this.appItem.id)
            this.$emit('freshAllMenu', true)
          }
        })
      }
    },
    menuSort(e){
      const domArr = document.getElementById("menu-sort").children
      // 获取目标列ID
      const toMenuCode = e.to.parentNode.parentNode.getAttribute('id') || this.appItem.id;
      let stageSortList = []
      let sortIconList = []
      for (let i = 0, len = domArr.length; i < len; i++) {
        stageSortList.push(domArr[i].getAttribute('id'));
        sortIconList.push(domArr[i].getAttribute('icon'));
      }
      this.brforeMenu = JSON.parse(JSON.stringify(this.getMenu))
      draggableMenuSort(this.appItem.id, {
        sortList:stageSortList,
        toMenuCode,
        sortIconList: sortIconList,
        appId:this.appItem.id
      }).then(res=>{
        // console.log(res)
        if(res.data && res.data.code == 0) {
          this.$store.commit("SET_SYSTEM", this.appItem.id)
          this.$emit('freshAllMenu', true)
        }
      }).catch(e => {
        this.$emit('changeMenu', this.brforeMenu)
        this.$nextTick( () => {
          this.menuchangerandom = Math.random()
        })
      })
    },
    childrenEnd(event){
      // console.log(event)
      let codes = [];
      // 获取目标列ID
      const toMenuCode = event.to.parentNode.parentNode.getAttribute('id');
      // 获取原列ID
      const fromMenuCode = event.from.parentNode.parentNode.getAttribute('id')
      this.$nextTick(()=>{
        for (let i = 0, len = event.to.children.length; i < len; i++) {
          codes.push(event.to.children[i].id)
        }
        this.brforeMenu = JSON.parse(JSON.stringify(this.getMenu))
        draggableMenuSort(this.appItem.id, {
          sortList:codes,
          toMenuCode,
          appId:this.appItem.id
        }).then(res=>{}).catch(e => {
          this.$emit('changeMenu', this.brforeMenu)
          this.$nextTick( () => {
            this.menuchangerandom = Math.random()
          })
        })
      })
    },
    addCatalogue (id) {
      this.$emit('addCatalogue', id)
    },
    getMoveTree (list, obj, key, ower) {
      if(list && list.length > 0) {
        this.$set(obj, 'children', [])
        for(let i in list) {
          if(list[i].id != ower && list[i].extend && list[i].extend.type == 'directory') {
            let tp = {
              id: list[i].id,
              name: list[i].name
            }
            if(list[i].children && list[i].children.length > 0) {
              this.$set(tp, 'children', [])
              this.getMoveTree(list[i].children, tp, key, ower)
            }
            obj.children.push(tp)
          }
        }
        if(!obj.children || obj.children.length == 0) {
          delete obj.children
        }
      }
    },
    openClose (item) {
      this.$set(item, 'closeStatus', !item.closeStatus)
      this.$forceUpdate()
    }
  },
  watch: {
    nowTagValue: {
      handler(newVal, oldVal) {
        if(newVal && newVal.includes('openMenuId')) {
          this.openMenuId = newVal.split('openMenuId=')[1]
        }else{
          this.openMenuId = ''
        }
        if(newVal && newVal != oldVal) {
          if(newVal.includes('jvsAppId')) {
            let str = newVal.split('?')[1]
            let ks = str.split('&')
            for(let k in ks) {
              let karr = ks[k].split('=')
              if(karr[0] == 'jvsAppId') {
                this.$emit('topNavChange', karr[1])
              }
            }
          }else{
            let type = 'system'
            localMenu.platformMenu.filter(pit => {
              if(pit.children && pit.children.length > 0) {
                pit.children.filter(pcit => {
                  if(pcit.extend && pcit.extend.url == newVal) {
                    type = 'platform'
                  }
                })
              }
            })
            this.$store.commit("SET_APP_SETTING_OPEN", true)
            this.$store.commit("SET_MENU_TYPE", type)
          }
        }
      }
    }
  }
};
</script>
<style lang="scss">
.el-popper{
  padding: 8px 0!important;
  .more-box{
    .more-title{
      padding: 5px 10px;
      font-family: Source Han Sans-Bold, Source Han Sans;
      max-width: 170px;
      box-sizing: border-box;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: pre;
      color: #363b4c;
    }
    .more-item{
      height: 32px;
      line-height: 32px;
      padding: 6px 24px;
      cursor: pointer;
      transition: 0.3s;
      &:hover{
        transition: 0.3s;
        background-color: #eff2f7;
      }
    }
    .model-header{
      font-size: 12px;
      padding: 6px 38px;
      color: #bbb3b3;
    }
    .model-box{
      display: flex;
      cursor: pointer;
      padding: 6px 12px;
      transition: 0.3s;
      &:hover{
        transition: 0.3s;
        background-color: #eff2f7;
      }
      .model-title{
        margin-bottom: 4px;
      }
      .model-explain{
        font-size: 12px;
        color: #bbb3b3;
      }
    }
    .model-learn{
      cursor: pointer;
      padding: 6px 16px;
      display: flex;
      align-items: center;
      .help-entry{
        fill: #bbb3b3;
        width: 16px;
        height: 16px;
        margin-right: 4px;
      }
      .model-help{
        color: #bbb3b3;
        font-size: 12px;
      }
    }
  }
}
.menu-item-li{
  overflow: hidden;
  display: flex;
  justify-content: space-between;
  position: relative;
  border-radius: 6px;
  box-sizing: border-box;
  .more{
    position: absolute;
    right: 0px;
    display: none;
  }
  .el-tooltip{
    display: flex!important;
    align-items: center;
  }
  .dragicon{
    cursor: move;
    position: absolute;
    right: 0;
    display: none;
  }
}
.menu-item-li:hover .dragicon{
  display: block;
}
.menu-item-li:hover .more{
  display: block;
}
.menu-item-li:hover .menu-item-it{
  overflow:hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.notmove{
  margin: 0 8px;
  padding-bottom: 10px;
  li{
    margin-top: 8px;
    .local-menu-item-dot{
      display: inline-block;
      width: 0;
      height: 0;
      border: 2px solid #363B4C;
      border-radius: 50%;
    }
    .svg-icon{
      fill: #363B4C;
    }
    &:hover{
      .svg-icon{
        fill: #1E6FFF;
      }
    }
  }
  .menu-item-li.is-active-item{
    background: #DDEAFF;
    color: #1E6FFF;
    .local-menu-item-dot{
      border-color: #1E6FFF;
    }
    .svg-icon{
      fill: #1E6FFF;
    }
    .menu-item-it{
      color: #303133;
    }
  }
  .collapseNav{
    .el-submenu__icon-arrow{
      display: none;
    }
  }
  .el-submenu__title{
    border-radius: 6px;
  }
  .catalogue-item{
    height: 36px;
    line-height: 36px;
    display:flex;
    align-items:center;
    width: calc(100% - 10px);
    justify-content: space-between;
    .more{
      display: none;
      z-index: 100000;
      .el-popover__reference-wrapper{
        display: flex;
      }
    }
  }
  .catalogue-item:hover{
    .more{
      display: block;
    }
  }
}
.local-menu{
  margin: 0;
  background: linear-gradient(179deg, rgba(30,111,255,0.05) 0%, rgba(30,111,255,0) 64px);
  padding-top: 8px;
  >div{
    margin-bottom: 8px;
  }
  .menu-item-head{
    margin-top: 16px;
    font-size: 14px;
    color: #363B4C;
    height: 18px;
    box-sizing: border-box;
    line-height: 18px;
    display: flex;
    align-items: center;
    font-family: Source Han Sans-Regular, Source Han Sans;
    cursor: pointer;
    .openclose-icon{
      width: 14px;
      height: 14px;
      margin-left: 8px;
      margin-right: 9px;

      transition: all .3s;
      fill: #363B4C;
      &.trans{
        transform: rotate(-90deg);
      }
    }
  }
  li{
    margin-left: 8px;
    margin-right: 8px;
    .local-menu-item-dot{
      margin-right: 10px;
    }
  }
}
.menu-ghost {
  background: #e5e5e5 !important;
  div {
    visibility: hidden;
  }
}
.menu-children-ghost {
  border-left: 3px solid #e5e5e5 !important;
  background: #e5e5e5;
  * {
    visibility: hidden;
    //display: none;
  }
}
</style>

