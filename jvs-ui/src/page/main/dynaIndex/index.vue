<template>
  <div class="sdw-index-page">
    <div v-if="loading" class="jvs-main-loading"></div>
    <div v-else class="dyna-index-page">
      <div v-if="!indexData && !userInfoObj.adminFlag || clearBool" class="welcom-back-div"></div>
      <homePage
        ref="homePage"
        v-else
        :key="homePageKey"
        :reqUrl="reqUrl"
        :mode="currentMode"
        :componentList="componentList"
        :isReadOnly="!userInfoObj.adminFlag"
        :options="options"
        :data="dataObj"
        :globalFormData="dataObj.global"
        :isLock="false"
        @changeHome="getIndex"
        @handleSave="handleSave"
        @roleConfig="roleConfig"
        @openEvent="openEvent"
      >
        <template slot="showSlot">
          <div style="display: flex;align-items: center;width: 100%;">
            <div class="user-info">
              <span>欢迎！</span>
              <span>{{userInfoObj.realName || userInfoObj.accountName}}</span>
            </div>
            <div style="display: flex;align-items: center;flex: 1;overflow: hidden;">
              <div v-if="homeList && homeList.length > 0" class="home-list">
                <div v-for="home in homeList" :key="home.id" :class="{'home-list-item': true, 'active': (indexData && home.id == indexData.id)}" @click="getIndex({id: home.id})">{{home.title}}</div>
              </div>
              <div v-if="userInfoObj.adminFlag" class="manage-tool" @click="changeMode('design')">
                <svg class="normal-svg-icon" aria-hidden="true">
                  <use xlink:href="#icon-jvs-yingyongshezhi-weixuanzhong"></use>
                </svg>
                <span>首页管理</span>
              </div>
            </div>
          </div>
        </template>
        <template slot="designSlot">
          <div style="display: flex;align-items: center;">
            <div v-if="indexData" class="home-name">
              <el-input v-if="editName" ref="nameInput" v-model="indexData.title" size="mini" @blur="showEditName(false)"></el-input>
              <div v-else class="show-name">
                <span>{{indexData.title}}</span>
                <svg class="edit-name-icon" aria-hidden="true" @click="showEditName(true)">
                  <use xlink:href="#jvs-ui-icon-bianji1"></use>
                </svg>
              </div>
            </div>
          </div>
        </template>
        <template slot="designRight">
          <div style="display: flex;align-items: center;">
            <div class="button" @click="changeMode('show')">
              <span>取消</span>
            </div>
          </div>
        </template>
      </homePage>
    </div>
    <!-- 另存设置门户名 -->
    <el-dialog
      class="custom-header-dialog"
      :title="dialogType == 'new' ? '另存为' : '权限设置'"
      :visible.sync="newSaveVisible"
      width="450px"
      :before-close="newSaveClose">
      <div v-if="newSaveVisible" class="index-dialog-box">
        <jvs-form :option="newSaveOption" :formData="newSaveData" @submit="newSaveHandle" @cancalClick="newSaveClose"></jvs-form>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getStore } from "@/util/store.js";
import { getIndexHome, getCurrentAppComponents, getIndexSett, saveIndexSett, delIndexSett } from "./api"
import eventBus from "@/util/vuebus";
export default {
  name: "Home",
  components: {},
  data() {
    return {
      homeList: [],
      loading: false,
      componentList: [],
      isReadOnly: false,
      reqUrl: "jvs-design",
      indexData: null,
      dataObj:{
        "list": [],
        "affix": [],
        "global": {
          "background": "",
          "backgroundFilter": "brightness(0.8)",
          "lang": "zh-cn",
          "gutter": 10,
          "css": "",
          "js": "",
          "globalFontFamily": "",
          "loadHarmonyOSFont": false,
          "siteTitle": "",
          "disabledDialogAnimation": false,
          "realBackgroundURL": ""
        }
      },
      options: {
        isMoBileView: false,
        uploadFileSize: 20, //文件上传大小
        uploadUrl: '/mgr/jvs-auth/upload/jvs-public',
        uploadData: {
          module: '/jvs-ui/dynamic/'
        },
        uploadHeaders: {
          "Authorization": `Bearer ${getStore({name:'access_token'})}`
        },
        reqHeaders: {
          "Authorization": `Bearer ${getStore({name:'access_token'})}`
        }
      },
      dialogType: '',
      newSaveVisible: false,
      newSaveData: null,
      newSaveOption: {
        emptyBtn: false,
        column: [
          {
            label: '门户名称',
            prop: 'title',
            rules: [ {required: true, message: "门户名称不能为空", trigger: ["blur", "change"]} ],
            display: false
          },
          {
            label: '选择角色',
            prop: 'roles',
            type: 'role',
            infoable: true,
            multiple: true,
            display: false
          }
        ]
      },
      userInfoObj: {},
      currentMode: 'show',
      editName: false,
      homePageKey: -1,
      clearBool: false
    };
  },
  created() {
    let tmu = getStore({ name: 'modeUserInfo' })
    if(tmu && tmu.userId) {
      this.clearBool = true
    }else{
      this.clearBool = false
    }
    this.initStore()
    eventBus.$on("dynaIndex", data => {
      if(data && data.oprate) {
        if(data.oprate == 'changeHome' &&  data.data) {
          this.getIndex(data.data)
        }
        if(data.oprate == 'deleteHome' && data.data) {
          this.deleteItem(data.data)
        }
        if(data.oprate == 'addCom' && data.data && this.$refs.homePage) {
          let item = data.data
          let obj = {
            label: item.name,
            text: item.description ? item.description : '',
            img: `#${item.icon}`,
            material:item.type,
            componentMetaData: JSON.parse(JSON.stringify(item))
          }
          delete obj.componentMetaData.formAttributes
          this.$refs.homePage.materialSelect(obj)
        }
        if(data.oprate == 'initStore') {
          this.initStore()
        }
        if(data.oprate == 'fresh') {
          this.initStore()
          this.clearBool = false
        }
        if(data.oprate == 'clear') {
          this.clearBool = true
        }
      }
    })
  },
  mounted () {
    this.userInfoObj = getStore({name:'userInfo'})
  },
  methods: {
    getHomeList () {
      getIndexHome().then(res => {
        if(res.data && res.data.code == 0) {
          this.homeList = res.data.data
          let tp = []
          this.homeList.filter(hit => {
            tp.push({
              id: hit.id,
              title: hit.title,
              defaultHome: hit.defaultHome
            })
          })
          this.$store.commit("SET_DYNAINDEXLEFTLIST", tp)
          this.$forceUpdate()
        }
      })
    },
    getIndex (params) {
      if(!(this.indexData && params && this.indexData.id == params.id)) {
        this.dataObj.list = []
        this.loading = true
        getIndexSett(params).then(res=>{
          if(res.data.code == 0) {
            if(res.data.data) {
              this.indexData = res.data.data || {}
              this.dataObj = JSON.parse(res.data.data.designContent)
              this.$store.commit('SET_DYNAINDEXLEFTDATA', this.indexData)
            }else{
              if(this.userInfoObj.adminFlag) {
                this.indexData = {}
                this.$store.commit('SET_DYNAINDEXLEFTDATA', this.indexData)
              }
            }
            this.loading = false
          }else {
            this.loading = false
          }
          this.$forceUpdate()
        }).catch(() => {
          this.loading = false
        })
      }
    },
    handleSave (data, optype) {
      let temp = {
        designContent: JSON.stringify(data),
        userId: this.$store.getters.userInfo.id,
        title: this.indexData.title
      }
      if(optype == 'new') {
        this.dialogType = 'new'
        this.newSaveData = temp
        this.newSaveOption.column.filter(col => {
          col.display = false
          if(col.prop == 'title') {
            col.display = true
          }
        })
        this.newSaveVisible = true
      }else{
        if(this.indexData && this.indexData.id) {
          temp.id = this.indexData.id
        }
        saveIndexSett(temp).then(res=>{
          if(res.data && res.data.code==0) {
            if(res.data.data) {
              this.indexData = res.data.data
              this.dataObj = JSON.parse(res.data.data.designContent)
              this.$store.commit('SET_DYNAINDEXLEFTDATA', this.indexData)
              this.getHomeList()
            }
            this.$notify({
              title: '提示',
              message: `保存成功`,
              position: 'bottom-right',
              type: 'success'
            })
            this.changeMode('show')
          }
        })
      }
    },
    newSaveHandle () {
      saveIndexSett(this.newSaveData).then(res=>{
        if(res.data && res.data.code==0) {
          this.$notify({
            title: '提示',
            message: `${this.dialogType == 'new' ? '另存成功' : '权限设置成功'}`,
            position: 'bottom-right',
            type: 'success'
          })
          if(this.dialogType == 'new') {
            this.getHomeList()
            if(res.data.data && res.data.data.id) {
              this.getIndex({id: res.data.data.id})
            }
            this.changeMode('show')
          }else{
            if(res.data.data && res.data.data.roles) {
              this.$set(this.indexData, 'roles', res.data.data.roles)
            }
          }
          this.newSaveClose()
        }
      })
    },
    newSaveClose () {
      this.dialogType = ''
      this.newSaveData = null
      this.newSaveVisible = false
    },
    roleConfig (data) {
      this.dialogType = 'role'
      this.newSaveOption.column.filter(col => {
        col.display = false
        if(col.prop == 'roles') {
          col.display = true
        }
      })
      let temp = {
        designContent: JSON.stringify(data),
        userId: this.$store.getters.userInfo.id
      }
      if(this.indexData && this.indexData.id) {
        temp.id = this.indexData.id
      }
      if(this.indexData && this.indexData.roles) {
        temp.roles = this.indexData.roles
      }
      if(!temp.roles) {
        temp.roles = []
      }
      this.newSaveData = temp
      this.newSaveVisible = true
    },
    openEvent (data) {
      this.$emit('openEvent', data)
    },
    deleteItem (item) {
      this.$confirm("确认删除？", '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        delIndexSett(item.id).then(res => {
          if(res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: '删除成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.getHomeList()
            this.getIndex()
            this.changeMode('show')
          }
        })
      }).catch(_ => {})
    },
    changeMode (mode) {
      this.currentMode = mode
      if(mode == 'design') {
        this.pageIsLock = true
        this.$store.commit("SET_DYNAINDEXLEFT", true)
      }else{
        this.pageIsLock = false
        this.$store.commit("SET_DYNAINDEXLEFT", false)
        this.getIndex()
      }
      this.$forceUpdate()
    },
    showEditName (bool) {
      this.editName = bool
      if(bool) {
        this.$nextTick(() => {
          this.$refs.nameInput.focus()
        })
      }
      this.$forceUpdate()
    },
    initStore () {
      this.homePageKey = Math.random()
      this.$store.commit("SET_DYNAINDEXLEFTLIST", [])
      this.$store.commit("SET_DYNAINDEXLEFTCOMLIST", [])
      this.$store.commit("SET_DYNAINDEXLEFTDATA", null)
      getCurrentAppComponents(this.reqUrl).then((res) => {
        if(res.data && res.data.code == 0) {
          this.componentList = res.data.data || []
          this.$store.commit("SET_DYNAINDEXLEFTCOMLIST", this.componentList)
        }
      })
      this.getHomeList()
      this.getIndex()
      this.$store.commit("SET_DYNAINDEXLEFT", false)
      this.changeMode('show')
    }
  },
};
</script>
<style lang="scss" scoped>
*{
  font-family: Source Han Sans-Regular, Source Han Sans;
}
.sdw-index-page{
  width: 100%;
  height: 100%;
  overflow: hidden;
  overflow-y: auto;
  user-select: none;
  .jvs-main-loading{
    position: absolute;
    width: 100%;
    height: 100%;
    background-image: url('../../../../public/jvs-ui-public/img/loading.gif');
    background-position: center;
    background-repeat: no-repeat;
  }
  .welcom-back-div{
    width: 100%;
    height: 100%;
    background-image: url('../../../const/img/welcom.png');
    background-size: 60%;
    background-repeat: no-repeat;
    background-position: center;
  }
}
.dyna-index-page{
  height: 100%;
  position: relative;
  overflow-y: auto;
  /deep/.page{
    .page-top{
      .design-tool{
        .right-button{
          .button{
            height: 32px;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            span {
              font-family: Microsoft YaHei-Regular, Microsoft YaHei;
              font-weight: 400;
              font-size: 14px;
            }
            &+.button{
              margin-left: 8px;
            }
          }   
        }
      }
    }
  }
}
.index-dialog-box{
  padding: 24px 32px;
  padding-bottom: 0;
  position: relative;
  /deep/.jvs-form{
    .form-item-btn{
      height: 32px;
      margin-bottom: 12px;
      margin-top: 24px;
      .form-btn-bar{
        margin-bottom: 0;
        display: flex;
        align-items: center;
        justify-content: flex-end;
        position: relative;
        height: 100%;
        .el-form-item__content{
          position: absolute;
          left: -32px;
          bottom: 0;
          width: 450px;
          border-top: 1px solid #EEEFF0;
          padding-top: 16px;
          display: flex;
          flex-direction: row-reverse;
          align-items: center;
          .el-button{
            margin-right: 16px;
            height: 32px;
            font-family: Microsoft YaHei-Regular, Microsoft YaHei;
            font-weight: 400;
            font-size: 14px;
          }
        }
      }
    }
  }
}
</style>
