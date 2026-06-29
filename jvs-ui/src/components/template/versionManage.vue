<template>
  <div class="version-manage-page">
    <div v-if="enableVersionFeature === false" class="app-version-upgrade">
      <div class="up-title">功能升级</div>
      <div class="up-box">
        <div class="up-title-little">多模式轻应用升级</div>
        <div class="up-sec-title">为提供更规范的轻应用配置，可以将应用升级多为模式，不影响其它应用：</div>
        <div class="up-con">
          <div class="up-con-item">
            <div class="up-con-item-title">1、升级后应用支持版本迭代 </div>
          </div>
          <div class="up-con-item">
            <div class="up-con-item-title">2、升级后不同模式的业务数据将分离存储 </div>
          </div>
          <div class="up-con-item">
            <div class="up-con-item-title">3、多模式的轻应用更方便源码定制化接入升级 </div>
          </div>
          <div class="up-con-item">
            <div class="up-con-item-title">4、多模式的轻应用将支持版本手动升级，版本回退等功能 </div>
          </div>
        </div>
        <div class="up-title-little">权限功能升级</div>
        <div class="up-sec-title">为提供更便捷的授权体验，升级后对此应用权限调整，不影响其它应用。在这次更新中，您将享受以下改进：</div>
        <div class="up-con">
          <div class="up-con-item">
            <div class="up-con-item-title">1、简化授权流程： </div>
            <div class="up-con-item-info">不再需要在设计阶段设置功能权限和数据权限，使整体使用更为轻松便捷。</div>
          </div>
          <div class="up-con-item">
            <div class="up-con-item-title">2、历史授权信息清理： </div>
            <div class="up-con-item-info">升级后会清空旧版的权限配置，需要重新配置权限</div>
          </div>
          <div class="up-con-item">
            <div class="up-con-item-title">3、模式差异权限支持： </div>
            <div class="up-con-item-info">新功能允许在不同模式之间差异化地设置权限，以更好地满足不同用户和场景的需求。</div>
          </div>
        </div>
        <div class="up-bottom">
          <el-button type="primary" @click="upgradeHandle">同意升级</el-button>
        </div>
      </div>
    </div>
    <div :class="{'version-type': true, 'empty-version-type': !gaInfo}">
      <h3>线上版本</h3>
      <div class="version-item" v-if="gaInfo">
        <div class="left">
          <div class="left-icon"></div>
          <div>
            <div class="number">
              <div>版本号:</div>
              <h4>{{gaInfo.appVersion}}</h4>
            </div>
            <div class="info">
              <p>
                <span>发布者:</span>
                <b>{{gaInfo.updateBy}}</b>
              </p>
              <p>
                <span>发布时间:</span>
                <b>{{gaInfo.updateTime}}</b>
              </p>
            </div>
          </div>
        </div>
        <div class="right">
          <div class="btn-list">
            <el-button type="primary" @click="viewHandle(gaInfo)">版本详情</el-button>
            <el-popover
              v-if="modeUserInfo ? modeUserInfo.mode == 'GA' : true"
              popper-class="more-oprate-popper"
              placement="bottom"
              width="100"
              trigger="click">
              <div class="more-oprate-list">
                <div @click="getHistroyList(gaInfo)">版本回退</div>
                <div v-if="gaInfo.versionStatus == 'USE'" @click="GASuspendHandle">暂停服务</div>
                <div v-if="gaInfo.versionStatus == 'SUSPEND'" @click="GAUseHandle">启用服务</div>
              </div>
              <el-button slot="reference" type="primary" icon="el-icon-arrow-down" style="margin-left: 10px;"></el-button>
            </el-popover>
          </div>
        </div>
      </div>
      <div v-else class="version-empty-box">
        <span>暂无线上版本~</span>
      </div>
    </div>
    <div v-if="modeUserInfo ? (['DEV', 'BETA'].indexOf(modeUserInfo.mode) > -1) : true" :class="{'version-type': true, 'empty-version-type': !betaInfo}">
      <div class="version-title">
        <h3>测试版本</h3>
        <el-button v-if="modeUserInfo ? modeUserInfo.mode == 'BETA' : true" size="mini" type="primary" @click="uploadHandle">上传</el-button>
      </div>
      <div class="version-item" v-if="betaInfo">
        <div class="left">
          <div class="left-icon"></div>
          <div>
            <div class="number">
              <div>版本号:</div>
              <h4>{{betaInfo.appVersion}}</h4>
            </div>
            <div class="info">
              <p>
                <span>发布者:</span>
                <b>{{betaInfo.updateBy}}</b>
              </p>
              <p>
                <span>发布时间:</span>
                <b>{{betaInfo.updateTime}}</b>
              </p>
            </div>
          </div>
        </div>
        <div class="right">
          <div class="btn-list">
            <el-button type="primary" @click="viewHandle(betaInfo)">版本详情</el-button>
            <el-popover
              v-if="modeUserInfo ? modeUserInfo.mode == 'BETA' : true"
              popper-class="more-oprate-popper"
              placement="bottom"
              width="100"
              trigger="click">
              <div class="more-oprate-list">
                <div @click="getHistroyList(betaInfo)">版本回退</div>
                <div @click="betaSubmitGAHandle">发布</div>
                <div @click="downloadBeta">下载</div>
              </div>
              <el-button slot="reference" type="primary" icon="el-icon-arrow-down" style="margin-left: 10px;"></el-button>
            </el-popover>
          </div>
        </div>
      </div>
      <div v-else class="version-empty-box">
        <span>暂无线上版本~</span>
      </div>
    </div>
    <div v-if="modeUserInfo ? modeUserInfo.mode == 'DEV' : true" :class="{'version-type': true, 'empty-version-type': !devInfo}">
      <h3>开发版本</h3>
      <div class="version-item" v-if="devInfo">
        <div class="left">
          <div class="left-icon"></div>
          <div>
            <div class="number">
              <div>版本号:</div>
              <h4>{{devInfo.appVersion}}</h4>
            </div>
            <div class="info">
              <p>
                <span>发布者:</span>
                <b>{{devInfo.updateBy}}</b>
              </p>
              <p>
                <span>发布时间:</span>
                <b>{{devInfo.updateTime}}</b>
              </p>
            </div>
          </div>
        </div>
        <div class="right">
          <div class="btn-list">
            <el-button type="primary" @click="viewHandle(devInfo)">版本详情</el-button>
            <el-popover
              popper-class="more-oprate-popper"
              placement="bottom"
              width="100"
              trigger="click">
              <div class="more-oprate-list">
                <div @click="devSubmitBetaHandle">提交测试</div>
              </div>
              <el-button slot="reference" type="primary" icon="el-icon-arrow-down" style="margin-left: 10px;"></el-button>
            </el-popover>
          </div>
        </div>
      </div>
      <div v-else class="version-empty-box">
        <span>暂无线上版本~</span>
      </div>
    </div>
    <div class="install-info-button">
      <span @click="showInstallList">{{`>>>查看发布列表<<<`}}</span>
    </div>
    <!-- 弹框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      :before-close="handleClose">
      <div v-if="dialogVisible">
        <div v-if="dialogType == 'open'">
          <jvs-form :formData="devToBetaForm" :option="devToBetaFormOption" @submit="devSubmit" @cancalClick="handleClose">
            <template slot="deployDataModelIdsForm">
              <jvs-button @click="selectDataHandle">选择</jvs-button>
            </template>
          </jvs-form>
        </div>
        <div v-if="dialogType == 'back'" :class="{'back-histroy-list': true, 'back-histroy-list-empty': (!histroyList || histroyList.length == 0)}">
          <div style="color: #7E8081;margin-bottom: 10px;">仅显示最近20个历史版本</div>
          <div class="bh-title">
            <span>版本</span>
            <span>项目备注</span>
            <span>更新时间</span>
          </div>
          <div v-if="histroyList && histroyList.length > 0">
            <el-radio-group v-model="backVersionId" class="bh-list">
              <el-radio v-for="item in histroyList" :key="'bh-item-'+item.id" :label="item.id">
                <div class="info">
                  <span>{{item.appVersion}}</span>
                  <span :title="item.description">{{item.description}}</span>
                  <span>{{item.updateTime}}</span>
                </div>
              </el-radio>
            </el-radio-group>
          </div>
          <div v-else class="empty-box">暂无数据</div>
          <div v-if="histroyList && histroyList.length > 0" style="display: flex;justify-content: center;align-items: center;margin-top: 20px;">
            <el-button size="mini" type="primary" @click="backSubmit">确定</el-button>
            <el-button size="mini" @click="handleClose">取消</el-button>
          </div>
        </div>
        <div v-if="dialogType == 'upload'" class="import-data-box">
          <el-upload
            class="import-data-upload"
            ref="uploadBtn"
            :action="`/mgr/jvs-design/base/app/version/fileUpload`"
            :headers="header"
            accept=".jvs"
            :file-list="tempfileList"
            :show-file-list="false"
            :on-change="uploadChange"
            :on-success="handleAppSuccess"
            :on-error="errUploadHandle"
            drag
            multiple>
            <div class="el-upload__text">
              <svg aria-hidden="true" style="width: 24px; height: 24px;margin-bottom: 16px">
                <use xlink:href="#icon-upload"></use>
              </svg>
              <div>点击或者拖动文件到虚线框内上传</div>
              <div style="color: #a2a3a5;font-size: 12px;margin-top: 8px;">仅支持jvs类型的版本文件</div>
            </div>
          </el-upload>
          <div class="upload-explain">
            <span style="color: #a2a3a5;">上传的文件符合以下规范：</span>
            <ul>
              <li style="list-style: disc">仅支持<span>（*.jvs）版本</span>文件</li>
              <li style="list-style: disc">被修改后的加密文件<span>无法被正常导入</span></li>
              <li style="list-style: disc">导入应用后可以操作其它功能不影响使用,导入完成后会自动通知</li>
            </ul>
          </div>
        </div>
        <div v-if="dialogType == 'view'">
          <jvs-form :formData="viewForm" :option="viewFormOption"></jvs-form>
        </div>
      </div>
    </el-dialog>
    <!-- 安装列表 -->
    <install-list :installListVisible="installListVisible" :appInfo="appInfo" @close="installClose"></install-list>
    <!-- 数据选择 -->
    <el-dialog
      class="select-data-dialog"
      title="数据选择"
      append-to-body
      :visible.sync="selectDataVisible"
      :before-close="selectDataClose">
      <div v-if="selectDataVisible" class="data-select-body">
        <div class="tips-text">
          <div>选择需要发布到模板的数据模型，最多不超过5000条数据，一个模型最多100条数据。</div>
          <div>若已选模型无数据则忽略，否则将<b>完全覆盖</b>目标版本对应模型的数据，<b>覆盖后无法还原，请谨慎选择!</b> </div>
        </div>
        <div class="top">
          <div class="left">
            <div class="title">
              <div class="label">可选模型</div>
              <div class="tool">
                <div class="el-pagination" style="display: flex;align-items: center;">
                  <button :disabled="dataSelectPage.currentPage < 2" class="btn-prev" @click="selectSearchPage('prev')">
                    <i class="el-icon-arrow-left"></i>
                  </button>
                  <el-input-number v-model="dataSelectPage.currentPage" @change="getDataList" :min="1" :max="Math.ceil(dataSelectPage.total / dataSelectPage.pageSize)" size="mini"></el-input-number>
                  <div class="text">/ {{Math.ceil(dataSelectPage.total / dataSelectPage.pageSize)}}</div>
                  <button :disabled="dataSelectPage.currentPage == Math.ceil(dataSelectPage.total / dataSelectPage.pageSize)" class="btn-next"  @click="selectSearchPage('next')">
                    <i class="el-icon-arrow-right"></i>
                  </button>
                  <div class="label" style="cursor: pointer;color: #1E6FFF;" @click="selectAll">全选</div>
                </div>
              </div>
            </div>
            <div class="body">
              <div class="search-box">
                <el-input placeholder="请输入模型名称搜索" v-model="keyword" @change="selectSearch" @input="selectSearch">
                  <i slot="prefix" class="el-input__icon el-icon-search" style="color: #6F7588;font-weight: bold;" @click="selectSearch"></i>
                </el-input>
              </div>
              <div class="search-result">
                <div v-for="item in dataPageList" :key="item.id" class="search-result-item" @click="selectHandle(item)">
                  <div class="name" :title="item.name">{{item.name}}</div>
                  <div class="key" :title="item.id">{{item.id}}</div>
                </div>
              </div>
            </div>
          </div>
          <div class="right">
            <div class="title">
              <div class="label">已选({{selectedListInfo.length}})</div>
              <div class="tool" @click="delItemOfSelect(null)">清空</div>
            </div>
            <div class="selected-list">
              <div v-for="item in selectedList" :key="'selected-item-'+item.id" class="selected-list-item">
                <div class="name" :title="item.name + ' : ' + item.id">{{item.name}}</div>
                <i class="el-icon-error" style="font-size: 20px;color: #C2C5CF;cursor: pointer;" @click="delItemOfSelect(item)"></i>
              </div>
            </div>
          </div>
        </div>
        <div class="footer">
          <div class="footer-button">
            <div class="ftb" @click="selectDataClose">取消</div>
            <div class="ftb submit" @click="selectDataSubmit">确定</div>
          </div>
        </div>
      </div>
    </el-dialog>
    <!-- 覆盖确认 -->
    <el-dialog
      :class="{'hide-header-pub-confirm-dialog': true, 'confirm': !(selectedListInfo && selectedListInfo.length > 0)}"
      title="确认发布应用"
      append-to-body
      :visible.sync="pubConfirmVisible"
      :before-close="pubConfirmClose">
      <div v-if="pubConfirmVisible && selectedListInfo && selectedListInfo.length > 0" class="pub-confirm-box">
        <div class="title">
          <i class="el-icon-warning"></i>
          <span>警告</span>
        </div>
        <div class="content">本次提交将<i>完全覆盖</i>已选模型的<b><i>所有生产数据</i></b>，覆盖后<i>无法还原</i>，请谨慎选择！</div>
        <div class="input-box">
          <p>如确认覆盖，请输入"<i style="color">确认覆盖生产数据</i>"</p>
          <el-input v-model="confirmText" size="mini"></el-input>
        </div>
      </div>
      <div v-else class="pub-confirm-box">
        <div class="title">
          <i class="el-icon-warning"></i>
          <span>确定发布？</span>
        </div>
        <div class="content">发布后将覆盖上次版本内容</div>
      </div>
      <div v-if="pubConfirmVisible" class="footer">
        <div class="footer-button">
          <div class="ftb" @click="pubConfirmClose">取消</div>
          <div v-if="selectedListInfo && selectedListInfo.length > 0" :class="{'ftb submit': true, 'disabled': !(confirmText && confirmText == '确认覆盖生产数据')}" @click="pubConfirmSubmit(true)">确定</div>
          <div v-else :class="{'ftb submit': true}" @click="pubConfirmSubmit(false)">确定</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import store from "@/store";
import { getAppVersion, devSubmitBeta, betaSubmitGA, GASuspend, betaUse, getHistroyVersion, backHistroyVersion, getDetailVersion, upgradeVersion, getDataModel } from './api'
import installList from "./installList"
export default {
  name: 'versionManagePage',
  components: { installList },
  props: {
    appInfo: {
      type: Object
    },
    modeUserInfo: {
      type: Object
    }
  },
  data() {
    return {
      gaInfo: null,
      betaInfo: null,
      devInfo: null,
      oprateType: '',
      dialogType: '',
      dialogTitle: '',
      dialogVisible: false,
      devToBetaForm: {},
      devToBetaFormOption: {
        emptyBtn: false,
        column: [
          {
            label: '应用版本号',
            prop: 'appVersion',
            rules: [ { required: true, message: '请输入应用版本号', trigger: 'blur' } ]
          },
          {
            label: '备注',
            prop: 'description',
            type: 'textarea'
          },
          {
            label: '是否发布数据',
            prop: 'deployData',
            type: 'switch',
            defaultValue: false,
            display: false
          },
          {
            label: '数据选择',
            prop: 'deployDataModelIds',
            formSlot: true,
            displayExpress: [{ prop: 'deployData', value: 'true' }]
          },
        ]
      },
      backVersionId: '',
      histroyList: [],
      tempfileList: [],
      header: {
        "Authorization": 'Bearer ' + store.getters.access_token
      },
      viewForm: {},
      viewFormOption: {
        btnHide: true,
        disabled: true,
        column: [
          // {
          //   label: '版本类型',
          //   prop: 'versionType',
          //   type: 'select',
          //   dicData: [
          //     {label: '线上版本', value: 'GA'},
          //     {label: '测试版本', value: 'BETA'},
          //     {label: '开发版本', value: 'DEV'},
          //   ]
          // },
          {
            label: '版本号',
            prop: 'appVersion'
          },
          {
            label: '发布者',
            prop: 'updateBy'
          },
          {
            label: '发布时间',
            prop: 'updateTime'
          },
          {
            label: '项目备注',
            prop: 'description'
          },
          // {
          //   label: '状态',
          //   prop: 'versionStatus',
          //   type: 'select',
          //   dicData: [
          //     {label: '历史版本', value: 'HISTORY'},
          //     {label: '正在使用', value: 'USE'},
          //     {label: '暂停服务', value: 'SUSPEND'},
          //   ]
          // }
        ]
      },
      installListVisible: false,
      enableVersionFeature: false,
      selectDataVisible: false,
      tableLoading: false,
      dataPageList: [],
      dataSelectPage: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 100, // 每页显示多少条
      },
      keyword: '',
      selectedList: [],
      pubConfirmVisible: false,
      confirmText: ''
    }
  },
  computed: {
    selectedListInfo () {
      let list = []
      for(let i in this.selectedList) {
        list.push(this.selectedList[i].id)
      }
      return list
    }
  },
  created () {
    if(this.appInfo.enableVersionFeature) {
      this.enableVersionFeature = true
    }
    this.init()
  },
  methods: {
    // 初始化
    init () {
      this.gaInfo = null
      this.betaInfo = null
      this.devInfo = null
      if(this.appInfo && this.appInfo.id) {
        this.getAppVersionHandle(this.appInfo.id)
      }
    },
    // 获取版本信息
    getAppVersionHandle (id) {
      getAppVersion(id).then(res => {
        if(res.data && res.data.code == 0) {
          // console.log(res.data.data)
          res.data.data.filter(rit => {
            if(rit.versionType == 'GA') {
              this.gaInfo = rit
            }
            if(rit.versionType == 'BETA') {
              this.betaInfo = rit
            }
            if(rit.versionType == 'DEV') {
              this.devInfo = rit
            }
          })
          this.$forceUpdate()
        }
      })
    },
    // 关闭弹框
    handleClose () {
      if(this.dialogType == 'upload') {
        this.uploadHandleClose()
      }
      this.dialogVisible = false
      this.oprateType = ''
      this.dialogTitle = ''
      this.devToBetaForm = {}
      this.backVersionId = ''
      this.viewForm = {}
    },
    // 提交测试
    devSubmitBetaHandle () {
      if(this.modeUserInfo && this.modeUserInfo.mode == 'DEV') {
        this.devToBetaFormOption.column.filter(fit => {
          if(fit.prop == 'deployData') {
            fit.display = true
          }
        })
      }
      this.oprateType = 'DEV'
      this.dialogTitle = '提交测试'
      this.dialogType = 'open'
      this.dialogVisible = true
    },
    devSubmit () {
      devSubmitBeta(this.devInfo.jvsAppId, this.devToBetaForm).then(res => {
        if(res.data && res.data.code == 0) {
          this.handleClose()
          this.init()
          this.$notify({
            title: '提示',
            message: '提交测试成功',
            position: 'bottom-right',
            type: 'success'
          });
          this.showInstallList()
        }
      })
    },
    // 发布到线上
    betaSubmitGAHandle () {
      if(this.modeUserInfo && this.modeUserInfo.mode == 'BETA') {
        this.oprateType = 'BETA'
        this.selectDataHandle()
      }else{
        this.$confirm('确定发布？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          betaSubmitGA(this.betaInfo.jvsAppId, this.betaInfo.id).then(res => {
            if(res.data && res.data.code == 0) {
              this.init()
              this.$notify({
                title: '提示',
                message: '发布成功',
                position: 'bottom-right',
                type: 'success'
              })
              this.showInstallList()
            }
          })
        }).catch(e => {})
      }
    },
    // 暂停
    GASuspendHandle () {
      this.$confirm('确定暂停服务？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        GASuspend(this.appInfo.id).then(res => {
          if(res.data && res.data.code == 0) {
            this.init()
            this.$notify({
              title: '提示',
              message: '暂停服务成功',
              position: 'bottom-right',
              type: 'success'
            })
            this.$emit('freshBasic')
          }
        })
      }).catch(e => {})
    },
    // 启用
    GAUseHandle () {
      this.$confirm('确定启用服务？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        betaUse(this.appInfo.id).then(res => {
          if(res.data && res.data.code == 0) {
            this.init()
            this.$notify({
              title: '提示',
              message: '启用服务成功',
              position: 'bottom-right',
              type: 'success'
            })
            this.$emit('freshBasic')
          }
        })
      }).catch(e => {})
    },
    // 获取历史版本
    getHistroyList (info) {
      getHistroyVersion(info.jvsAppId, info.versionType).then(res => {
        if(res.data && res.data.code == 0) {
          this.dialogTitle = '版本回退'
          this.dialogType = 'back'
          this.oprateType = info.versionType
          if(res.data.data && res.data.data.length > 0) {
            this.histroyList = res.data.data
          }
          this.dialogVisible = true
        }
      })
    },
    // 回退
    backSubmit () {
      if(this.backVersionId) {
        let current = null
        if(this.oprateType == 'GA') {
          current = JSON.parse(JSON.stringify(this.gaInfo))
        }
        if(this.oprateType == 'BETA') {
          current = JSON.parse(JSON.stringify(this.betaInfo))
        }
        if(this.oprateType == 'DEV') {
          current = JSON.parse(JSON.stringify(this.devInfo))
        }
        if(current) {
          backHistroyVersion(current.jvsAppId, {versionId: this.backVersionId, versionType: current.versionType}).then(res => {
            if(res.data && res.data.code == 0) {
              this.handleClose()
              this.init()
              this.$notify({
                title: '提示',
                message: '回退版本成功',
                position: 'bottom-right',
                type: 'success'
              })
              this.$emit('freshApp')
              this.showInstallList()
            }
          })
        }
      }
    },
    // 下载
    downloadBeta () {
      this.$openUrl(`/mgr/jvs-design/app/design/${this.betaInfo.jvsAppId}/version/download/${this.betaInfo.id}`, '_blank')
    },
    // 上传
    uploadHandle () {
      this.oprateType = 'BETA'
      this.dialogType = 'upload'
      this.dialogTitle = '上传'
      this.dialogVisible = true
    },
    uploadChange (file, fileList) {
      this.tempfileList = fileList
    },
    // 文件创建应用成功
    handleAppSuccess (response, fileList) {
      if (response && response.code === 0) {
        this.handleClose()
        this.init()
        this.$notify({
          title: '提示',
          message: '上传成功',
          position: 'bottom-right',
          type: 'success'
        })
        this.showInstallList()
      } else {
        this.$notify({
          title: '提示',
          message: response.msg,
          position: 'bottom-right',
          type: 'error'
        });
      }
    },
    errUploadHandle (err, file, fileList) {
      this.$refs.uploadBtn.clearFiles()
      this.tempfileList = []
      this.$notify({
        title: '提示',
        message: err,
        position: 'bottom-right',
        type: 'error'
      });
    },
    uploadHandleClose () {
      this.tempfileList = []
      this.$refs.uploadBtn.clearFiles()
    },
    submitFile () {
      this.$refs.uploadBtn.submit()
    },
    viewHandle (info) {
      getDetailVersion(info.jvsAppId, info.id).then(res => {
        if(res.data && res.data.code == 0) {
          this.viewForm = res.data.data
          this.dialogTitle = '版本详情'
          this.dialogType = 'view'
          this.oprateType = info.versionType
          this.dialogVisible = true
        }
      })
    },
    showInstallList () {
      this.installListVisible = true
    },
    installClose () {
      this.installListVisible = false
      this.init()
    },
    upgradeHandle () {
      this.$confirm('是否确认升级?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        upgradeVersion(this.appInfo.id).then(res => {
          if(res.data && res.data.code == 0) {
            this.enableVersionFeature = true
            this.$emit('setAppInfo', 'enableVersionFeature', true)
            this.$notify({
              title: '提示',
              message: `升级成功`,
              position: 'bottom-right',
              type: 'success'
            })
            this.init()
            this.showInstallList()
            this.$forceUpdate()
          }
        })
      }).catch(e => {})
    },
    selectDataHandle () {
      this.selectDataVisible = true
      this.getDataList()
    },
    getDataList () {
      this.tableLoading = true
      let obj={
        current: this.dataSelectPage.currentPage,
        size: this.dataSelectPage.pageSize,
        appId: this.appInfo.id
      }
      if(this.keyword) {
        obj.name = this.keyword
      }
      getDataModel(obj).then(res => {
        if(res.data && res.data.code == 0) {
          this.dataPageList = [...res.data.data.records]
          this.dataSelectPage.total = res.data.data.total
          this.dataSelectPage.currentPage = res.data.data.current
          this.tableLoading = false
        } else {
          this.tableLoading = false
        }
      }).catch(err => {
        this.tableLoading = false
      })
    },
    selectSearch () {
      this.dataSelectPage.currentPage = 1
      this.getDataList()
    },
    selectSearchPage (type) {
      if(type == 'prev') {
        this.dataSelectPage.currentPage -= 1
      }
      if(type == 'next') {
        this.dataSelectPage.currentPage += 1
      }
      this.getDataList()
    },
    selectHandle (item) {
      if(this.selectedListInfo.indexOf(item.id) == -1) {
        this.selectedList.push(item)
      }
      this.$forceUpdate()
    },
    delItemOfSelect (item) {
      if(item && item.id) {
        let index = this.selectedListInfo.indexOf(item.id)
        if(index > -1) {
          this.selectedList.splice(index, 1)
        }
      }else{
        this.selectedList = []
      }
      this.$forceUpdate()
    },
    selectDataSubmit () {
      if(this.oprateType == 'DEV') {
        this.$set(this.devToBetaForm, 'deployDataModelIds', this.selectedListInfo)
      }
      if(this.oprateType == 'BETA') {
        this.pubConfirmVisible = true
      }
      this.selectDataClose()
    },
    selectDataClose () {
      this.keyword = ''
      this.dataSelectPage.total = 0
      this.dataSelectPage.currentPage = 1
      this.dataPageList = []
      this.selectDataVisible = false
    },
    selectAll () {
      this.dataPageList.filter(dit => {
        this.selectHandle(dit)
      })
      this.$forceUpdate()
    },
    pubConfirmClose () {
      this.pubConfirmVisible = false
      this.confirmText = ''
    },
    pubConfirmSubmit (bool) {
      if(bool && !(this.confirmText == '确认覆盖生产数据')) {
        return false
      }
      let tp = null
      if(this.selectedListInfo && this.selectedListInfo.length > 0) {
        tp = {
          deployDataModelIds: this.selectedListInfo,
          deployData: true
        }
      }
      betaSubmitGA(this.betaInfo.jvsAppId, this.betaInfo.id, tp).then(res => {
        this.pubConfirmClose()
        if(res.data && res.data.code == 0) {
          this.init()
          this.$notify({
            title: '提示',
            message: '发布成功',
            position: 'bottom-right',
            type: 'success'
          })
          this.showInstallList()
        }
      }).catch(e => {
        this.pubConfirmClose()
      })
    },
  }
}
</script>
<style lang="scss" scoped>
.version-manage-page{
  height: 100%;
  box-sizing: border-box;
  overflow: hidden;
  overflow-y: auto;
  position: relative;
  .app-version-upgrade{
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    box-sizing: border-box;
    background-color: #fff;
    padding: 0 14px;
    box-sizing: border-box;
    z-index: 99;
    .up-title{
      height: 65px;
      font-size: 50px;
      font-family: YouSheBiaoTiHei-Regular, YouSheBiaoTiHei;
      font-weight: 400;
      color: #40486C;
      line-height: 70px;
    }
    .up-title-little{
      margin: 20px 0;
      font-size: 24px;
      font-family: YouSheBiaoTiHei-Regular, YouSheBiaoTiHei;
      font-weight: 400;
      color: #40486C;
    }
    .up-box{
      background: url(../../const/application/apppermissionupgrade.png) no-repeat right top;
      margin-right: 60px;
    }
    .up-sec-title{
      height: 21px;
      font-size: 16px;
      font-family: Microsoft YaHei-Regular, Microsoft YaHei;
      font-weight: 400;
      color: #40486C;
      line-height: 21px;
    }
    .up-con{
      .up-con-item{
        margin-top: 24px;
        height: 38px;
        font-size: 14px;
        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
        color: #6F7588;
        font-weight: 400;
        line-height: 24px;
        .up-con-item-title{
          color: #1E6FFF;
          font-family: Microsoft YaHei-Bold, Microsoft YaHei;
          font-weight: 700;
        }
      }
    }
    .up-bottom{
      margin-top: 40px;
      .el-button--primary{
        width: 152px;
        height: 40px;
        background: #1E6FFF;
        border-radius: 4px;
        box-sizing: border-box;
        font-size: 14px;
        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
        font-weight: 400;
        color: #FFFFFF;
      }
    }
  }
  .version-type{
    width: 100%;
    margin-bottom: 32px;
    h3{
      margin: 0;
      height: 23px;
      font-size: 16px;
      font-family: Source Han Sans-Bold, Source Han Sans;
      font-weight: 700;
      color: #363B4C;
      line-height: 23px;
    }
    .version-title{
      display: flex;
      align-items: center;
      h3{
        margin-right: 30px;
      }
    }
    .version-item{
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 24px;
      background: #F5F6F7;
      border-radius: 4px;
      overflow: hidden;
      margin-top: 12px;
      .left{
        display: flex;
        flex: 1;
        .left-icon{
          width: 64px;
          height: 64px;
          background: linear-gradient(145deg, #79A9FF 0%, #1E6FFF 100%);
          border-radius: 5px;
          overflow: hidden;
          margin-right: 25px;
          background-image: url(../../const/application/appversion.png);
          background-position: center;
          background-size: contain;
        }
        .number{
          display: flex;
          align-items: center;
          height: 23px;
          font-size: 16px;
          font-family: Source Han Sans-Medium, Source Han Sans;
          font-weight: 500;
          color: #363B4C;
          line-height: 23px;
          h4{
            margin-left: 16px;
            height: 23px;
            font-size: 16px;
            font-family: Source Han Sans-Medium, Source Han Sans;
            font-weight: 500;
            color: #363B4C;
            padding: 0;
            word-break: break-word;
            display: -webkit-box;
            -webkit-box-orient: vertical;
            -webkit-line-clamp: 4;
            overflow: hidden;
          }
        }
        .info{
          padding-top: 2px;
          p{
            margin: 0;
            padding: 0;
            margin-top: 6px;
            display: flex;
            align-items: center;
            height: 20px;
            font-size: 14px;
            font-family: Source Han Sans-Regular, Source Han Sans;
            font-weight: 400;
            color: #6F7588;
            line-height: 20px;
            span{
              display: block;
              word-break: keep-all;
              margin-right: 14px;
            }
            b{
              color: #494949;
              font-weight: normal;
              display: inline-block;
              word-break: break-word;  
            }
          }
        }
      }
      .right{
        .btn-list{
          display: flex;
          align-items: center;
          .el-button{
            height: 36px;
            box-sizing: border-box;
            background: #1E6FFF;
            color: #fff;
            font-size: 14px;
            font-family: Microsoft YaHei-Regular, Microsoft YaHei;
            font-weight: 400;
            color: #FFFFFF;
          }
        }
      }
    }
    .version-empty-box{
      width: 100%;
      height: 125px;
      background: #F5F6F7;
      border-radius: 4px;
      overflow: hidden;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-top: 12px;
      span{
        font-size: 14px;
        font-family: Source Han Sans-Regular, Source Han Sans;
        font-weight: 400;
        color: #363B4C;
        line-height: 20px;
      }
    }
  }
  .install-info-button{
    width: 100%;
    text-align: center;
    span{
      cursor: pointer;
      font-size: 12px;
      font-family: Microsoft YaHei-Regular, Microsoft YaHei;
      font-weight: 400;
      color: #1E6FFF;
    }
  }
}
.more-oprate-list{
  text-align: center;
  div{
    height: 32px;
    line-height: 32px;
    color: #525354;
    font-size: 12px;
    cursor: pointer;
  }
  div:hover{
    background: #F6F7F8;
  }
}
.back-histroy-list{
  .bh-title{
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    background-color: #F6F8F9;
    height: 42px;
    color: #7E8081;
    padding: 0 20px;
    span{
      display: block;
    }
    span:nth-of-type(1){
      width: 60px;
      margin-left: 25px;
    }
    span:nth-of-type(2){
      flex: 1;
      margin: 0 20px;
      box-sizing: border-box;
    }
    span:nth-last-of-type(1){
      width: 140px;
    }
  }
  .bh-list{
    width: 100%;
    /deep/.el-radio{
      display: flex;
      align-items: center;
      margin-bottom: 20px;
      margin-right: 0;
      padding: 10px 20px;
      border-bottom: 1px solid #E7E7EB;
      .el-radio__label{
        flex: 1;
        overflow: hidden;
      }
    }
    .info{
      display: flex;
      color: #383838;
      span{
        display: block;
      }
      span:nth-of-type(1) {
        width: 60px;
      }
      span:nth-of-type(2){
        height: 14px;
        flex: 1;
        margin: 0 20px;
        box-sizing: border-box;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: pre;
      }
      span:nth-last-of-type(1) {
        width: 140px;
      }
    }
  }
}
.back-histroy-list-empty{
  .empty-box{
    display: block;
    width: 100%;
    height: 240px;
    color: #909399;
    line-height: 60px;
    text-align: center;
  }
  .empty-box::before{
    content: "";
    display: block;
    width: 100%;
    height: 180px;
    background-image: url('../../const/img/emptyImage.svg');
    background-size: 260px 123px;
    background-repeat: no-repeat;
    background-position: center;
  }
}
/deep/.import-data-box{
  .import-data-upload{
    text-align: center;
    .el-upload-dragger{
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #f7f7f7;
      width: 600px;
    }
  }
  .upload-explain{
    margin-top: 16px;
    font-size: 12px;
    ul{
      padding: 0 16px;
      margin: 8px 0;
      li{
        line-height: 20px;
        span{
          font-weight: bold;
        }
      }
    }
  }
}
.select-data-dialog.el-dialog__wrapper{
  /deep/.el-dialog{
    width: 700px;
    height: 680px;
    margin-top: calc(50vh - 340px)!important;
    border-radius: 6px!important;
    overflow: hidden;
    .el-dialog__header{
      height: 48px;
      background: #F5F6F7;
      border-radius: 6px 6px 0px 0px;
      padding: 0 0 0 24px;
      .el-dialog__title{
        height: 18px;
        font-family: Microsoft YaHei-Bold, Microsoft YaHei;
        font-weight: 700;
        font-size: 14px;
        color: #363B4C;
        line-height: 18px;
      }
      .el-dialog__headerbtn{
        top: 10px;
        right: 17px;
        font-size: 20px;
        .el-dialog__close{
          color: #575E73;;
        }
      }
    }
    .el-dialog__header::before{
      display: none!important;
    }
    .el-dialog__body{
      height: calc(100% - 48px);
      padding: 0!important;
    }
    .data-select-body{
      height: 100%;
      .tips-text{
        padding: 0 32px;
        padding-top: 16px;
        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
        font-weight: 400;
        font-size: 14px;
        color: #6F7588;
        line-height: 18px;
        div{
          height: 18px;
          b{
            color: #FF194C;
          }
        }
        div+div{
          margin-top: 5px;
        }
      }
      .top{
        height: calc(100% - 117px);
        padding: 16px 32px;
        box-sizing: border-box;
        display: flex;
        align-items: center;
        justify-content: space-between;
        .left, .right{
          border: 1px solid #EEEFF0;
          border-radius: 4px;
          height: 100%;
          .title{
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 0 16px;
            height: 36px;
            background: #F5F6F7;
            .label{
              font-family: Microsoft YaHei-Regular, Microsoft YaHei;
              font-weight: 400;
              font-size: 14px;
              color: #363B4C;
            }
            .tool{
              display: flex;
              align-items: center;
            }
          }
        }
        .left{
          width: calc(100% - 216px);
          .title{
            .tool{
              .el-input-number{
                width: 32px;
                .el-input-number__decrease,.el-input-number__increase{
                  display: none;
                }
                .el-input__inner{
                  padding: 0;
                  border: 0;
                  height: 20px;
                  line-height: 20px;
                  font-family: Microsoft YaHei-Regular, Microsoft YaHei;
                  font-weight: 400;
                  font-size: 14px;
                  color: #575E73;
                }
              }
              .btn-prev, .btn-next{
                background: none;
              }
              .text{
                margin-left: 8px;
                font-family: Microsoft YaHei-Regular, Microsoft YaHei;
                font-weight: 400;
                font-size: 14px;
                color: #575E73;
                height: 20px;
                line-height: 20px;
              }
            }
          }
          .body{
            height: calc(100% - 36px);
            .search-box{
              padding: 0 15px;
              padding-top: 8px;
              .el-input{
                .el-input__inner{
                  height: 34px;
                  line-height: 34px;
                  border: 0;
                  border-bottom: 1px solid #EEEFF0;
                  border-radius: 0;
                }
                .el-input__inner::placeholder{
                  font-family: Microsoft YaHei-Regular, Microsoft YaHei;
                  font-weight: 400;
                  font-size: 14px;
                  color: #6F7588;
                }
              }
            }
            .search-result{
              height: calc(100% - 42px);
              padding: 0 15px;
              overflow: hidden;
              overflow-y: auto;
              .search-result-item{
                padding: 16px 0;
                border-bottom: 1px solid #EEEFF0;
                display: flex;
                align-items: center;
                font-family: Microsoft YaHei-Regular, Microsoft YaHei;
                font-weight: 400;
                font-size: 14px;
                color: #363B4C;
                cursor: pointer;
                .name{
                  flex: 1;
                  overflow: hidden;
                  text-overflow: ellipsis;
                  white-space: pre;
                }
                .key{
                  font-size: 12px;
                  color: #6F7588;
                  flex: 1;
                  overflow: hidden;
                  text-overflow: ellipsis;
                  white-space: pre;
                }
              }
            }
          }
        }
        .right{
          width: 200px;
          box-sizing: border-box;
          .title{
            .tool{
              font-family: Microsoft YaHei-Regular, Microsoft YaHei;
              font-weight: 400;
              font-size: 14px;
              color: #1E6FFF;
              cursor: pointer;
            }
          }
          .selected-list{
            padding: 0 16px;
            padding-bottom: 16px;
            height: calc(100% - 36px);
            overflow: hidden;
            overflow-y: auto;
            .selected-list-item{
              margin-top: 16px;
              display: flex;
              align-items: center;
              .name{
                width: calc(100% - 20px);
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: pre;
              }
            }
          }
        }
      }
      .footer{
        width: 100%;
        height: 60px;
        background: #FFFFFF;
        border-radius: 0px 0px 6px 6px;
        box-sizing: border-box;
        border-top: 1px solid #EEEFF0;
        display: flex;
        align-items: center;
        justify-content: flex-end;
        .footer-button{
          display: flex;
          align-items: center;
          .ftb{
            width: 60px;
            height: 32px;
            line-height: 32px;
            text-align: center;
            background: #F5F6F7;
            border-radius: 4px 4px 4px 4px;
            cursor: pointer;
            font-family: Microsoft YaHei-Regular, Microsoft YaHei;
            font-weight: 400;
            font-size: 14px;
            color: #363B4C;
            margin-right: 16px;
          }
          .submit{
            background: #1E6FFF;
            color: #fff;
          }
        }
      }
    }
  }
}
.hide-header-pub-confirm-dialog.el-dialog__wrapper{
  /deep/.el-dialog{
    width: 540px;
    height: 240px;
    margin-top: calc(50vh - 120px)!important;
    border-radius: 6px!important;
    overflow: hidden;
    .el-dialog__header{
      display: none;
    }
    .el-dialog__header::before{
      display: none!important;
    }
    .el-dialog__body{
      height: calc(100% - 48px);
      padding: 0!important;
    }
    .pub-confirm-box{
      padding: 20px;
      box-sizing: border-box;
      .title{
        display: flex;
        align-items: center;
        i{
          font-size: 20px;
          color: #FF194C;
        }
        span{
          font-family: Source Han Sans-Bold, Source Han Sans;
          font-weight: 500;
          font-size: 16px;
          color: #FF194C;
          line-height: 20px;
          margin-left: 5px;
        }
      }
      .content{
        padding: 0 20px;
        margin-top: 10px;
        font-family: Source Han Sans-Regular, Source Han Sans;
        font-weight: 400;
        font-size: 14px;
        color: #363B4C;
        i{
          font-style: normal;
          color: #FF194C;
        }
      }
      .input-box{
        margin-top: 10px;
        padding: 0 20px;
        p{
          margin: 0 0 10px 0;
          font-family: Source Han Sans-Regular, Source Han Sans;
          font-weight: 400;
          font-size: 13px;
          color: #363B4C;
        }
        i{
          font-style: normal;
          color: #FF194C;
        }
      }
    }
    .footer{
      width: 100%;
      height: 60px;
      background: #FFFFFF;
      border-radius: 0px 0px 6px 6px;
      box-sizing: border-box;
      border-top: 1px solid #EEEFF0;
      display: flex;
      align-items: center;
      justify-content: flex-end;
      .footer-button{
        display: flex;
        align-items: center;
        .ftb{
          width: 60px;
          height: 32px;
          line-height: 32px;
          text-align: center;
          background: #F5F6F7;
          border-radius: 4px;
          cursor: pointer;
          font-family: Microsoft YaHei-Regular, Microsoft YaHei;
          font-weight: 400;
          font-size: 14px;
          color: #363B4C;
          margin-right: 16px;
        }
        .submit{
          background: #1E6FFF;
          color: #fff;
          &.disabled{
            cursor: not-allowed;
          }
        }
      }
    }
  }
  &.confirm{
    /deep/.el-dialog{
      height: 150px;
    }
  }
}
</style>
<style lang="scss">
.more-oprate-popper{
  min-width: 100px!important;
}
</style>