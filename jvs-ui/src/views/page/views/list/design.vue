<template>
  <div class="jvs-contail-font design-content">
    <!-- 无权限 -->
    <div v-if="showErrorTip" class="permission">
      <img src="@/const/img/permission.png" alt=""/>
      <span>暂无访问权限</span>
    </div>
    <div v-else>
      <design-header :infoData="infoData" :currentTab="currentTab" :tabList="tabList" type="list" @tabSelect="tabSelect" @handleSave="handleSave"/>
      <div v-show="currentTab === 'design'" class="content-box">
        <designPage
          v-if="activeStep == 1"
          :infoData="infoData"
          :dataPermissionList="dataPermissionList"
          :modelType="modelType"
          :roleType="roleType"
          :designId="designId"
          :dataModelId="dataModelId"
          :role="role"
          :currentTab="currentTab"
          @closeDesign="closeDesignHandle"
          @getDesignData="getDesignData"
          @handleSaveLoading="handleSaveLoading"
          @getButtons="getButtons"
          @getLeftButtons="getLeftButtons"
          @getList="getList"
          @handleSave="saveHandle('save')"
        />
      </div>
      <div v-show="currentTab === 'pageSetting'" class="content-box">
        <div class="page-setting">
          <div class="setting-form basic-form">
            <div class="title">
              <svg aria-hidden="true">
                <use xlink:href="#icon-jvs-rongqi"></use>
              </svg>
              <span>基础设置</span>
            </div>
            <jvs-form v-if="infoDataShow" :option="option" :formData="infoData"></jvs-form>
            <div class="showtype-label">布局设置</div>
            <div class="show-type">
              <div v-for="item in showTypeList" :key="item.value" :class="{'show-type-item': true}" @click="infoData.displayType = item.value;">
                <img :src="item.img" alt="">
                <p>
                  <el-radio v-model="infoData.displayType" :label="item.value">{{item.label}}</el-radio>
                </p>
              </div>
            </div>
          </div>
          <div class="setting-form copy-url">
            <div class="title">
              <svg aria-hidden="true">
                <use xlink:href="#icon-jvs-rongqi"></use>
              </svg>
              <span>预览设计</span>
            </div>
            <div class="info-text">
              <p>可以复制地址直接访问</p>
            </div>
            <div style="width: 100%;display: flex;align-items: center">
              <el-input style="flex: 1;" size="mini" disabled :value="getUrl()"></el-input>
              <el-tooltip effect="dark" content="复制" placement="top">
                <svg class="copy-icon" aria-hidden="true" @click="handleCopy(getUrl())">
                  <use xlink:href="#icon-jvs-fuzhi1"></use>
                </svg>
              </el-tooltip>
              <el-tooltip effect="dark" content="访问" placement="top">
                <svg class="copy-icon" aria-hidden="true" @click="handleView(getUrl())">
                  <use xlink:href="#icon-jvs-lianjie"></use>
                </svg>
              </el-tooltip>
              <el-popover
                v-if="mobileCode"
                placement="bottom"
                width="140"
                trigger="hover">
                <div style="width: 134px;padding: 0 8px;">
                  <img style="width: 100%" :src="mobileCode" alt=""/>
                  <el-button style="width: 100%;margin-top: 2px;" size="mini" icon="el-icon-download" @click="handleDownloadCode">下载二维码</el-button>
                </div>
                <svg slot="reference" class="copy-icon" aria-hidden="true">
                  <use xlink:href="#icon-qrcode"></use>
                </svg>
              </el-popover>
            </div>
          </div>
        </div>
      </div>
      <div v-show="currentTab === 'permission'" class="content-box">
        <div class="permission-box" v-if="infoData && infoData.id">
          <permission
            ref="permission"
            :role="role"
            :operationList="operationList"
            :leftOperationList="leftOperationList"
            :roleType="roleType"
            :dataModelId="dataModelId"
            :jvsAppId="$route.query.jvsAppId"
          />
          <dataPermision
            ref="dataPermission"
            :role="role"
            :operationList="operationList"
            :roleType="roleType"
            :dataModelId="dataModelId"
            :jvsAppId="$route.query.jvsAppId"
            :stepDataPermissionBool="infoData.stepDataPermission"
            :dataRoleTypeString="infoData.dataRoleType"
            :dataRoleList="infoData.dataRole"/>
        </div>
      </div>
      <div v-show="currentTab === 'dataSetting'" class="content-box">
        <div class="page-setting">
          <div class="setting-form">
            <div class="title">
              <svg aria-hidden="true">
                <use xlink:href="#icon-jvs-rongqi"></use>
              </svg>
              <span>业务逻辑</span>
            </div>
            <div class="info-text">
              <p>应用可轻量化与业务系统进行对接，可灵活扩展业务互联能力。<span @click="handleMore('event-auto-help')">了解更多</span></p>
              <p>所有的数据都会以网络请求到此地址进行数据转换。可进行业务二次对接。</p>
            </div>
            <el-form label-width="100px" label-position="top">
              <el-form-item label="">
                <el-table
                  :data="callbackSettingData"
                  :span-method="arraySpanMethod"
                  style="width: 100%;"
                  size='mini'
                >
                  <!-- 表头文字说明 -->
                  <el-table-column label="事件">
                    <template slot-scope="scope">
                      {{ scope.row.eventName }}
                    </template>
                  </el-table-column>
                  <el-table-column label="前置">
                    <template slot-scope="scope">
                      <el-button size="mini" type="text" @click="ruleDesign(scope.$index, scope.row, 'before')">{{scope.row.beforeRuleId ? '编辑' : '设计'}}</el-button>
                      <!-- <el-button v-if="scope.row.beforeRuleId" size="mini" type="text" @click="cancelRule(scope.$index, scope.row, 'before')">取消</el-button> -->
                    </template>
                  </el-table-column>
                  <el-table-column label="是否启用前置">
                    <template slot-scope="scope">
                      <el-switch v-model="scope.row.beforeRuleEnable" :disabled="!scope.row.beforeRuleId"></el-switch>
                    </template>
                  </el-table-column>
                  <el-table-column label="后置">
                    <template slot-scope="scope">
                      <el-button size="mini" type="text" @click="ruleDesign(scope.$index, scope.row, 'after')">{{scope.row.afterRuleId ? '编辑' : '设计'}}</el-button>
                      <!-- <el-button v-if="scope.row.afterRuleId" size="mini" type="text" @click="cancelRule(scope.$index, scope.row, 'after')">取消</el-button> -->
                    </template>
                  </el-table-column>
                  <el-table-column label="是否启用后置">
                    <template slot-scope="scope">
                      <el-switch v-model="scope.row.afterRuleEnable" :disabled="!scope.row.afterRuleId"></el-switch>
                    </template>
                  </el-table-column>
                </el-table>
              </el-form-item>
            </el-form>
          </div>
          <div class="setting-form" v-if="false">
            <div class="title">
              <svg aria-hidden="true">
                <use xlink:href="#icon-jvs-rongqi"></use>
              </svg>
              <span>打印设置</span>
            </div>
            <div class="info-text">
              <p>更多功能敬请期待</p>
            </div>
          </div>
          <div class="setting-form" v-if="false">
            <div class="title">
              <svg aria-hidden="true">
                <use xlink:href="#icon-jvs-rongqi"></use>
              </svg>
              <span>消息设置</span>
            </div>
            <div class="info-text">
              <p>更多功能敬请期待</p>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!-- 设置 -->
    <el-dialog
      title="设置"
      :visible.sync="settingVisible"
      width="60%"
      append-to-body
      :close-on-click-modal="false"
      :before-close="handleSettingClose">
      <div style="height: 50vh">
        <el-tabs v-model="activeName" tab-position="left">
          <el-tab-pane label="权限设置" name="permission">
            <permission
              ref="permissionConfig"
              v-if="settingVisible"
              :operationList="operationList"
              :leftOperationList="leftOperationList"
              :role="role"
              :roleType="roleType"
              @submitPermission="submitPermission"
            />
          </el-tab-pane>
          <el-tab-pane label="数据设置" name="callBack">
            <el-form style="padding: 0 20px;" class="demo-form-inline" label-width="100px" label-position="top">
              <el-form-item label="">
                <span style="color: #a3a3a3">所有的数据都会以网络请求到此地址进行数据转换。可进行业务二次对接。</span>
                <el-table
                  border
                  :data="callbackSettingData"
                  :span-method="arraySpanMethod"
                  class="tb-edit button-set-table"
                  style="width: 100%"
                  size='mini'
                >
                  <!-- 表头文字说明 -->
                  <el-table-column label="事件" align="center">
                    <template slot-scope="scope">
                      {{ scope.row.eventName }}
                    </template>
                  </el-table-column>
                  <el-table-column label="业务逻辑" align="center">
                    <template slot-scope="scope">
                      <el-button size="mini" type="text" @click="ruleDesign(scope.$index, scope.row)">设计</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs></div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="handleSettingClose">取 消</el-button>
        <el-button type="primary" @click="handleSettingSubmit">确 定</el-button>
      </span>
    </el-dialog>
    <!-- 发布到模板 -->
    <templateSet ref="templ" type="page" :id="designId"></templateSet>
  </div>
</template>
<script>
import {getKeyValue} from '../../api/list'
import saveicon from "@/const/img/保存.png"
import designPage from '../design/list'
import {getDesignInfo, updateDesignInfo, createRule} from "../../api/design";
import Permission from "../../components/list/permission";
import DataPermision from "../../components/list/dataPermission";
import {getModelSetting, updateModelSetting} from "@/api/newDesign";
import {deployPage} from "../../api/newDesign";
import templateSet from '@/components/template/set'
import DesignHeader from "@/components/page-header/DesignHeader";
import tableImg from '../../const/img/table.png'
import leftTreeImg from '../../const/img/leftTree.png'
import cardImg from '../../const/img/card.png'

export default {
  components: {DataPermision, designPage, templateSet, Permission, DesignHeader},
  props: {
    // infoData: {
    //   type: Object
    // },
    systemId: {
      type: Number
    },
    menuId: {
      type: Number
    },
    menuForm: {
      type: Object
    }
  },
  data () {
    return {
      mobileCode: '', // 二维码
      option: { // 对应表单设置
        labelWidth: '90px',
        submitBtn: false,
        emptyBtn: false,
        cancal: false,
        btnHide: true,
        formAlign: 'top',
        column: [
          {
            label: '列表名称',
            prop: 'name',
            search: true,
            searchSpan: 4,
            rules: [
              { required: true, message: '请输入名称', trigger: 'blur' }
            ],
          },
          {
            label: '描述',
            prop: 'description',
            type: 'textarea',
            maxlength: 255,
            rows: 5,
            showwordlimit: true,
            autoSize: true
          },
          {
            label: "图标",
            hide: true,
            display: false,
            prop: "icon",
            type: 'iconSelect'
          },
        ],
      },
      currentTab: '',
      tabList: [
        { name: 'pageSetting', label: '页面设置', icon: 'el-icon-collection-tag' },
        { name: 'design', label: '列表设计', icon: 'el-icon-document' },
        { name: 'permission', label: '页面权限', icon: 'el-icon-key' },
        { name: 'dataSetting', label: '数据设置', icon: 'el-icon-setting' },
      ],
      saveLoading: false, // 保存loading
      publishLoading: false, // 发布loading
      saveIcon: saveicon,
      role: [], // 权限设置
      roleType: true, // 权限类型,true 应用 权限，false 自定义权限
      operationList: [], // 操作权限list
      leftOperationList: [], // 左树权限list
      settingVisible: false, // 设置弹框
      activeName: 'permission', // tabs
      infoData: {},
      dataPermissionList: [],
      infoDataShow: false,
      activeStep: 0,
      labelValue: {},
      menuFormData: {},
      modelType: '', // 模式  产品pro  开发dev
      designId: '',
      dataModelId: '',
      designData: {},
      callbackSettingData: [],
      showTypeList: [
        {label: '列表', value: 'table', img: tableImg},
        {label: '目录', value: 'leftTree', img: leftTreeImg},
        {label: '卡片', value: 'card', img: cardImg}
      ],
      showErrorTip: false,
    }
  },
  created () {
    if(this.$store.getters && this.$store.getters.labelValue) {
      this.labelValue = this.$store.getters.labelValue
    }else{
      this.getKeyValueHandle()
    }
    if (this.$route.query && this.$route.query.id) {
      this.getInfoData(this.$route.query.id)
      this.designId = this.$route.query.id
      this.dataModelId = this.$route.query.dataModelId
      this.getModelSetting(this.$route.query.jvsAppId, this.$route.query.dataModelId, this.$route.query.id)
    }
    // if(this.infoData) {
    //   this.activeStep = 1
    //   this.modelType = this.infoData.type
    // }


    if(this.menuForm) {
      this.menuFormData = this.menuForm
    }
    window.onbeforeunload = (e) => {
      e.returnValue = '关闭提示'
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.currentTab = 'pageSetting'
    })
  },
  computed: {
  },
  methods: {
    // 下载二维码
    handleDownloadCode() {
      if (this.mobileCode) {
        this.downloadImage(this.mobileCode, '二维码.png')
      }
    },
    // imageSrc 下载图片的链接
    // name 图片的名称
    downloadImage (imageSrc, name) {
      let image = new Image()
      // 告知请求的服务器 进行跨域请求
      image.setAttribute('crossOrigin', 'anonymous')
      image.src = imageSrc
      image.onload = function () {
        let canvas = document.createElement('canvas')
        canvas.width = image.width
        canvas.height = image.height
        let context = canvas.getContext('2d')
        context.drawImage(image, 0, 0, image.width, image.height)
        let url = canvas.toDataURL('image/png')
        let a = document.createElement('a')
        a.href = url
        a.download = name || 'photo'
        a.click()
      }
    },
    // 复制地址
    handleCopy(url) {
      const text = document.createElement('input')
      text.value = url
      document.body.appendChild(text)
      text.select()
      document.execCommand('Copy')
      document.body.removeChild(text)
      // this.$message.success('复制成功！')
      this.$notify({
        title: '提示',
        message: '复制成功！',
        position: 'bottom-right',
        type: 'success'
      });
    },
    handleView(url) {
      this.$openUrl(url, '_blank')
    },
    // 获取预览地址
    getUrl() {
      return location.origin + `/page-design-ui/#/list/use?id=${this.$route.query.id}&dataModelId=${this.$route.query.dataModelId}&jvsAppId=${this.$route.query.jvsAppId}`
    },
    // 了解更多
    handleMore(str) {
      this.$openUrl('', '_blank', str)
    },
    // 保存
    async handleSave() {
      await this.saveDataPermission()
      await this.callbackSettingSubmit()
      const permissionData = this.$refs.permission.getPermissionData()
      // this.infoData.role = permissionData.role
      // this.infoData.roleType = permissionData.roleType
      this.$set(this.infoData, 'role', permissionData.role)
      this.$set(this.infoData, 'roleType', permissionData.roleType)
      this.$set(this.infoData, 'stepDataPermission', this.$refs.dataPermission.stepDataPermission)
      this.$set(this.infoData, 'dataRoleType', this.$refs.dataPermission.dataRoleType)
      this.$set(this.infoData, 'dataRole', this.dataPermissionList)
      this.$set(this.infoData, 'relationDesignIds', this.getRelationList())
      this.$forceUpdate()
      this.$root.eventBus.$emit('pageEvent', 'save', this.infoData)
    },
    // tab选择结果
    async tabSelect(val) {
      this.currentTab = val
      if (val === 'permission') {
        await this.getInfoData(this.$route.query.id, true, true)
        await this.$refs.permission.initData()
        await this.$refs.dataPermission.initData()
      }
    },
    // 提交数据权限
    saveDataPermission() {
      const params = JSON.parse(JSON.stringify(this.$refs.dataPermission.getPermissionData()))
      this.dataPermissionList = [...params]
      // const dataModelId = this.$route.query.dataModelId
      // saveDataPermission(params, dataModelId).then(res => {
      //   if (res.data && res.data.code == 0) {
      //     console.log(res.data.data)
      //   }
      // })
    },
    // 设置
    handleSetting() {
      this.setPermission('permission')
    },
    // 关闭设置弹窗
    handleSettingClose() {
      this.settingVisible = false
    },
    // 设置提交
    async handleSettingSubmit (url) {
      await this.callbackSettingSubmit()
      await this.$refs.permission.handleConfirm()
      this.settingVisible = false
      // this.$message.success('设置成功')
      this.$notify({
        title: '提示',
        message: '设置成功',
        position: 'bottom-right',
        type: 'success'
      });
      if(url) {
        this.$openUrl(url, '_blank')
      }
    },
    // 发布
    publishClick() {
      this.publishLoading = true
      const row = this.infoData
      let obj = JSON.parse(JSON.stringify(row))
      obj.type = 'page'
      let msg = ''
      if(row.isDeploy){
        msg = '设计已经发布，此次保存可能会影响使用，是否继续操作？'
      }
      if(msg) {
        this.$confirm(msg, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          deployPage(this.$route.query.jvsAppId, obj).then(res => {
            if(res.data.code == 0) {
              this.publishLoading = false
              // this.$message.success('发布成功')
              this.$notify({
                title: '提示',
                message: '发布成功',
                position: 'bottom-right',
                type: 'success'
              });
            }
          })
        }).catch(e => {
          this.publishLoading = false
        })
      }else{
        this.$confirm('是否确认发布？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          deployPage(this.$route.query.jvsAppId, obj).then(res => {
            if(res.data.code == 0) {
              this.publishLoading = false
              // this.$message.success('发布成功')
              this.$notify({
                title: '提示',
                message: '发布成功',
                position: 'bottom-right',
                type: 'success'
              });
            }
          })
        }).catch(e => {})
      }
    },
    // 获取数据关联设置
    getModelSetting(jvsAppId, modelId, designId) {
      getModelSetting(jvsAppId, modelId, designId).then(res => {
        if (res.data && res.data.code == 0) {
          const defaultEventList = [
            {
              eventType: 'DATA_DELETE',
              afterRuleEnable: false,
              eventName: '删除数据',
              beforeRuleEnable: false,
            },
            {
              eventType: 'DATA_IMPORT',
              afterRuleEnable: false,
              eventName: '导入数据',
              beforeRuleEnable: false,
            }
          ]
          this.callbackSettingData = res.data.data.eventList.length > 0 ? [...res.data.data.eventList] : defaultEventList
          let hasImport = false
          let hasDelete = false
          if(this.callbackSettingData) {
            this.callbackSettingData.filter(ei => {
              if(ei.eventType == 'DATA_DELETE') {
                hasDelete = true
              }
              if(ei.eventType == 'DATA_IMPORT') {
                hasImport = true
              }
            })
            if(!hasDelete) {
              this.callbackSettingData.push({
                eventType: 'DATA_DELETE',
                afterRuleEnable: false,
                eventName: '删除数据',
                beforeRuleEnable: false,
              })
            }
            if(!hasImport) {
              this.callbackSettingData.push({
                eventType: 'DATA_IMPORT',
                afterRuleEnable: false,
                eventName: '导入数据',
                beforeRuleEnable: false,
              })
            }
          }
        }
      })
    },
    // 生成地址
    getpageUrl (row) {
      let str = location.origin
      str += ('/page-design-ui/#/show?id='+row.id + (row.dataModelId ? `&dataModelId=${row.dataModelId}` : ''))
      return str
    },
    // 复制
    onCopy (e) {
      console.log(e.text)
      if(e.text) {
        // this.$message.success('复制成功')
        this.$notify({
          title: '提示',
          message: '复制成功',
          position: 'bottom-right',
          type: 'success'
        });
      }
    },
    onError (e) {
      console.log(e)
    },
    // 获取设计数据
    getDesignData(data) {
      this.saveLoading = false
      if (data && data.viewJson) {
        this.designData = JSON.parse(data.viewJson)
      }
    },
    // 操作保存loading
    handleSaveLoading() {
      this.saveLoading = false
    },
    // 获取按钮
    getButtons(arr) {
      let buttonArr = []
      if(arr) {
        buttonArr = arr.map(item => {
          return item.name
        })
      }
      this.operationList = [...buttonArr]
      // this.settingVisible = true
    },
    // 获取左树按钮
    getLeftButtons (arr) {
      let buttonArr = arr.map(item => {
        return item.name
      })
      this.leftOperationList = [...buttonArr]
    },
    // 设置权限
    setPermission(type) {
      this.$root.eventBus.$emit('pageEvent', type)
    },
    // 权限提交
    submitPermission(arr, roleType) {
      // this.roleType = roleType
      // this.role = [...arr]
      this.designData.role = roleType ? undefined : [...arr]
      this.designData.roleType = roleType
      this.designData.relationDesignIds = this.getRelationList()
      updateDesignInfo(this.$route.query.jvsAppId, this.infoData.id, this.designData).then(res => {
        if(res.data.code == 0) {
          this.getInfoData(this.infoData.id)
        }
      }).catch(e => {
        this.submitLoding = false
      })
    },
    // 获取设计详情
    async getInfoData(id, getBool, noUpBase) {
      await getDesignInfo(this.$route.query.jvsAppId, id).then(res => {
        if (res.data && res.data.code == 0 && res.data.data) {
          if(!res.data.data.displayType) {
            res.data.data.displayType = 'table'
          }
          if(!res.data.data.icon) {
            res.data.data.icon = 'icon-haxi'
          }
          if(noUpBase) {
            if(res.data.data.appMenu) {
              this.infoData.appMenu = res.data.data.appMenu
            }            
          }else{
            this.infoData = res.data.data
          }
          this.mobileCode = res.data.data.mobileCode
          this.infoDataShow = true
          if(this.infoData && this.infoData.appMenu) {
            if(this.infoData.appMenu.role) {
              this.role = JSON.parse(JSON.stringify(this.infoData.appMenu.role))
            }
            if(this.infoData.appMenu.roleType) {
              this.roleType = JSON.parse(JSON.stringify(this.infoData.appMenu.roleType))
            }
          }
          this.modelType = 'DEVELOP'
          this.activeStep = 1
          if(res.data.data.viewJson) {
            const viewJson = JSON.parse(res.data.data.viewJson)
            if(!getBool) {
              this.getButtons(viewJson.buttons)
              this.getLeftButtons(viewJson.leftTreeButton ? viewJson.leftTreeButton: [])
            }
          }
          this.$forceUpdate()
        }else{
          this.showErrorTip = true
        }
      }).catch(e => {
        this.showErrorTip = true
      })
    },
    // 获取所有label value 对应值
    getKeyValueHandle () {
      getKeyValue().then(res => {
        if(res.data.code == 0) {
          this.labelValue = res.data.data
          this.$store.commit('SET_LabelValue', this.labelValue)
        }
      })
    },
    // 上一步
    lastStep () {
      if (this.activeStep-- < 0) {
        this.activeStep = 0
      }
    },
    // 下一步
    nextStep () {
      if (this.activeStep++ > 1) this.activeStep = 0
    },
    // 通知关闭
    closeDesignHandle (bool) {
      window.close()
      this.$emit('closeDesign', bool)
    },
    // 通知关闭
    saveHandle (type) {
      if (type === 'save') {
        this.saveLoading = true
      }
      this.$root.eventBus.$emit('pageEvent', type)
    },
    // 刷新列表
    getList() {
      this.getInfoData(this.infoData.id) // 刷新
      this.$emit('getList')
    },
    callbackSettingSubmit() {
      const params = {
        eventList: [...this.callbackSettingData],
        relationDesignIds: this.getRelationList()
      }
      updateModelSetting(this.$route.query.jvsAppId, params, this.$route.query.dataModelId, this.$route.query.id).then(res => {
        if (res.data && res.data.code == 0) {
          this.getModelSetting(this.$route.query.jvsAppId, this.$route.query.dataModelId, this.$route.query.id)
        }
      })
    },
    arraySpanMethod({ row, column, rowIndex, columnIndex }) {
      return [1, 1];
      if (rowIndex < 3) {
        if (columnIndex === 1) {
          return [1, 2];
        } else if (columnIndex === 0) {
          return [1, 1];
        }
      }
    },
    // 发布到模板
    publishTempClick () {
      this.$refs.templ.init()
    },
    // 设计逻辑
    ruleDesign (index, row, pos) {
      let key = 'afterRuleId'
      if(pos == 'before') {
        key = 'beforeRuleId'
      }
      if(row[key]) {
        this.$openUrl(`/rule-design-ui/#/ruleDesign?id=${row[key]}&componentId=${this.$route.query.id}&jvsAppId=${this.infoData.jvsAppId}&name=${this.infoData.name}`, '_blank')
      }else{
        let obj = {
          jvsAppId: this.infoData.jvsAppId,
          name: this.infoData.name+'-'+row.eventName,
          componentId: this.$route.query.id,
          designId: this.$route.query.id,
          componentType: 'page'
        }
        createRule(obj).then(res => {
          if(res.data && res.data.code == 0) {
            if(res.data.data) {
              this.$set(this.callbackSettingData[index], key, res.data.data)
              this.handleSettingSubmit(`/rule-design-ui/#/ruleDesign?id=${res.data.data}&componentId=${this.$route.query.id}&jvsAppId=${this.infoData.jvsAppId}&name=${this.infoData.name}`)
            }
          }
        })
      }
    },
    cancelRule (index, row, pos) {
      this.$confirm('取消后需重新设计，确定取消该设计？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        let key = 'afterRuleId'
        if(pos == 'before') {
          key = 'beforeRuleId'
        }
        this.$set(this.callbackSettingData[index], 'ruleEnable', false)
        // this.$set(this.callbackSettingData[index], key, '')
      }).catch(e => { })
    },
    getRelationList () {
      let list = []
      // 事件
      this.callbackSettingData.filter(item => {
        if(item.beforeRuleId && list.indexOf(item.beforeRuleId) == -1) {
          list.push(item.beforeRuleId)
        }
        if(item.afterRuleId && list.indexOf(item.afterRuleId) == -1) {
          list.push(item.afterRuleId)
        }
      })
      // 设计
      if(this.infoData.viewJson) {
        let viewJson = JSON.parse(this.infoData.viewJson)
        // 左树按钮
        if(viewJson.leftTreeButton &&  viewJson.leftTreeButton.length > 0) {
          viewJson.leftTreeButton.filter(fit => {
            if(fit.formId && list.indexOf(fit.formId) == -1) {
              list.push(fit.formId)
            }
            if(fit.secret && list.indexOf(fit.secret) == -1) {
              list.push(fit.secret)
            }
          })
        }
        // 按钮表单逻辑
        if(viewJson.buttons && viewJson.buttons.length > 0) {
          viewJson.buttons.filter(fit => {
            if(fit.formId && list.indexOf(fit.formId) == -1) {
              if(fit.formId && list.indexOf(fit.formId) == -1) {
                list.push(fit.formId)
              }
              if(fit.secret && list.indexOf(fit.secret) == -1) {
                list.push(fit.secret)
              }
              if(fit.pageBottomBtns && fit.pageBottomBtns.length > 0) {
                fit.pageBottomBtns.filter(pbt => {
                  if(pbt.rule && list.indexOf(pbt.rule) == -1) {
                    list.push(pbt.rule)
                  }
                })
              }
            }
          })
        }
      }
      return list
    }
  }
}
</script>
<style lang="scss">
.el-tabs__item{
  font-weight: bold;
}
</style>
<style lang="scss" scoped>
.permission{
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  width: 100vw;
  img {
    width: 168px;
    height: 157px;
  }
  span{
    height: 18px;
    font-family: Microsoft YaHei-Regular, Microsoft YaHei;
    font-weight: 400;
    font-size: 14px;
    color: #3D3D3D;
    line-height: 18px;
  }
}
.design-content{
  position: relative;
  height: 100%;
  box-sizing: border-box;
  overflow: hidden;
  background: #fff;
  -webkit-box-sizing: border-box;
  .content-box{
    display: flex;
    width: 100%;
    height: calc(100vh - 56px);
    overflow-y: auto;
    background: #fff;
    border-top: 1px solid #EEEFF0;
    .page-setting{
      width: 900px;
      margin: 16px auto;
      /deep/.setting-form{
        &+.setting-form{
          margin-top: 32px;
        }
        .showtype-label{
          margin-bottom: 8px;
          font-family: Microsoft YaHei-Bold, Microsoft YaHei;
          font-weight: 700;
          font-size: 14px;
          color: #363B4C;
        }
        .show-type{
          display: flex;
          justify-content: space-between;
          .show-type-item{
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            text-align: center;
            cursor: pointer;
            border-radius: 10px;
            img{
              display: block;
              width: 260px;
              height: 190px;
            }
            p{
              .el-radio__label{
                color: #959595;
              }
            }
          }
          .show-type-item:hover{
            box-shadow: 0 0 10px #ccc;
          }
        }
        .title {
          height: 21px;
          display: flex;
          align-items: center;
          svg{
            width: 16px;
            height: 16px;
            margin-right: 8px;
          }
          span{
            font-family: Microsoft YaHei-Regular, Microsoft YaHei;
            font-weight: 400;
            font-size: 16px;
            color: #363B4C;
          }
        }
        .info-text{
          padding: 16px 0;
          position: relative;
          p{
            margin: 0;
            font-family: Microsoft YaHei-Regular, Microsoft YaHei;
            font-weight: 400;
            font-size: 14px;
            color: #6F7588;
            span {
              color: #1E6FFF;
              cursor: pointer;
            }
          }
          p+p{
            margin-top: 12px;
          }
          .tag-switch{
            position: absolute;
            right: 0;
            top: 0;
            margin-top: 0;
          }
        }
        .el-table{
          border: 0;
          &::before{
            display: none;
          }
          .el-table__header-wrapper{
            border-radius: 2px;
            overflow: hidden;
            thead{
              tr{
                background: #F5F6F7;
                th{
                  background: #F5F6F7;
                  border: 0;
                }
              }
            }
          }
          .el-table__body-wrapper{
            .el-table__body{
              .el-table__row{
                .el-table__cell{
                  height: 52px;
                  .cell{
                    font-family: Source Han Sans-Regular, Source Han Sans;
                    font-weight: 400;
                    font-size: 14px;
                    color: #363B4C;
                    .el-button--text{
                      font-family: Source Han Sans-Regular, Source Han Sans;
                      font-weight: 400;
                      font-size: 14px;
                      color: #1E6FFF;
                    }
                  }
                }
              }
            }
          }
        } 
      }
      .basic-form{
        /deep/.el-form{
          margin-top: 16px;
          .el-form-item__label{
            padding: 0px !important;
            margin-bottom: 8px;
            font-family: Microsoft YaHei-Bold, Microsoft YaHei;
            font-weight: 700;
            font-size: 14px;
            color: #363B4C;
            &::before{
              color: #FF194C;
            }
          }
        }
      }
      .copy-url.setting-form{
        margin-top: 14px;
        /deep/.el-input{
          .el-input__inner{
            height: 36px;
            background: #F5F6F7;
            border: 0;
            font-family: Source Han Sans-Regular, Source Han Sans;
            font-weight: 400;
            font-size: 14px;
            color: #363B4C;
          }
        }
        .copy-icon{
          margin-left: 16px;
          width: 20px;
          height: 20px;
          fill: #545454;
          cursor: pointer;
        }
      }
    }
    .permission-box{
      width: 850px;
      margin: 15px auto;
    }
  }
  .cont-top {
    overflow: hidden;
    font-size: 12px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    color: #606266;
    background: #fff;
    // margin-top: 8px;
    // padding: 8px 0;
    border-radius: 5px;
    .cont-top-item {
      display: flex;
      justify-content: space-between;
      p {
        margin: 0 10px;
        height: 32px;
        line-height: 32px;
      }
    }
  }
  .title-page-header{
    margin-top: 0;
  }
  .icon {
    width: 20px;
    height: 20px;
    fill: currentColor;
    overflow: hidden;
    cursor: pointer;
  }
}
.form-design-tool{
  font-size: 20px;
  cursor: pointer;
  color: #353535;
}
</style>
