<template>
  <div id="template" class="template-view-page template-manage">
    <div class="template-manage-content">
      <div class="template-item-quick">
        <img class="tmp-top-bg" :src="temtopback" alt="">
        <div class="tmp-back">
          <div class="left1"></div>
          <div class="left2"></div>
          <div class="right1"></div>
          <div class="right2"></div>
          <div class="right3"></div>
        </div>
        <div class="tmp-top">
          <div>
            <h1>欢迎使用快速应用搭建！</h1>
            <span class="tmp-top-desc">10分钟完成您的数据信息管理</span>
          </div>
          <div v-if="permissionsList.indexOf('jvs_app') > -1" class="tmp-search">
            <el-input v-model="queryParams.name" size="mini" prefix-icon="el-icon-search" placeholder="关键字搜索应用" @input="page.current=1;getMyApplication();"></el-input>
            <el-button type="primary" size="mini" @click="page.current=1;getMyApplication();">搜索</el-button>
          </div>
          <div class="tmp-bottom">
            <div v-if="modeUserInfo ? modeUserInfo.mode == 'DEV' : true" v-show="((modeUserInfo && modeUserInfo.userId) ? false : true) && permissionsList.indexOf('jvs_app_add') > -1" class="left" @click="handleAdd">
              <i class="el-icon-plus"></i>
              <span>创建空白应用</span>
            </div>
            <div class="center">
              <img class="tmp-center-back" :src="tmpcenterback" alt="">
              <div class="tmp-center-text">
                <h2>模板中心</h2>
                <div>
                  <div>精选行业模板，方案完全基于平台进行设计、开发。</div>
                  <div>平台提供了安全、可靠、稳定的运行环境，为您的业务保驾护航。</div>
                </div>
              </div>
              <div v-show="(modeUserInfo && modeUserInfo.userId) ? false : true" class="center-button" @click="showInstallList">安装记录</div>
            </div>
          </div>
        </div>
      </div>
      <div class="template-recommend-app-box">
        <div class="header-box">
          <div class="header-item">推荐应用模板</div>
        </div>
        <div class="template-recommend-app-back">
          <div class="recommend-app-list">
            <div class="recommend-app-list-item" v-for="(item, key) in recommendAppList" :key="key" @click="handleClick(item)">
              <div class="item-logo">
                <img :src="item.logo" alt=""/>
              </div>
              <div class="item-content">
                <div class="item-name">{{ item.name }}</div>
                <div class="item-description">{{ item.briefDescription }}</div>
              </div>
            </div>
          </div>
          <div class="recommend-app-more">
            <h4>更多应用推荐</h4>
            <p @click="openTemplateMore">进入模板中心</p>
          </div>
        </div>
      </div>
      <div class="template-manage-box">
        <div class="template-manage-top">
          <div class="left">
            <div class="header-box">
              <div class="header-item">我的应用</div>
            </div>
            <div v-if="modeUserInfo ? modeUserInfo.mode == 'GA' : true" class="template-manage-top-type">
              <p v-for="item in publishStatusList" :key="item.value" :class="{'item': true, 'active': item.value == publishStatus}" @click="publishStatus = item.value;getMyApplication();">{{item.label}}</p>
            </div>
          </div>
          <div v-show="(modeUserInfo && modeUserInfo.userId) ? false : true" class="right">
            <div v-if="modeUserInfo ? modeUserInfo.mode == 'DEV' : true" v-show="permissionsList.indexOf('jvs_app_add') > -1" class="right-button" @click="handleAdd">
              <svg class="icon" aria-hidden="true">
                <use xlink:href="#icon-jvs-xinjian-ffffff"></use>
              </svg>
              <span>创建应用</span>
            </div>
            <div v-if="modeUserInfo ? ['DEV', 'BETA'].indexOf(modeUserInfo.mode) > -1 : true" v-show="permissionsList.indexOf('jvs_app_import') > -1" class="right-button" @click="createAppByFile">
              <svg class="icon" aria-hidden="true">
                <use xlink:href="#icon-jvs-daoru-ffffff"></use>
              </svg>
              <span>导入应用</span>
            </div>
          </div>
        </div>
        <div v-if="current === 'app'" class="my-template-list" :key="dataLoading">
          <div class="box" v-if="myTemp && myTemp.length > 0">
            <div v-if="dataLoading" class="loading-back"/>
            <div v-for="(item, index) in myTemp" :key="'my-temp-'+index" class="my-template-list-item">
              <div class="content" @click="handleClickMyApp(item)">
                <div class="content-header">
                  <img :src="item.logo" alt="">
                  <h5 :title="item.name">{{item.name}}</h5>
                </div>
                <div class="content-description">
                  <span>{{item.description}}</span>
                </div>
                <div v-if="modeUserInfo && modeUserInfo.mode == 'GA'" class="tool-div" style="justify-content: space-between;">
                  <el-tag v-if="item.isDeploy">已发布</el-tag>
                  <el-tag v-else type="info">未发布</el-tag>
                </div>
              </div>
              <div
                class="more-handle"
                v-show="(modeUserInfo && modeUserInfo.userId) ? false : true"
                v-if="(modeUserInfo ? (modeUserInfo.mode == 'DEV' && item.appRoles.indexOf('adminMember') > -1) : (item.appRoles.indexOf('adminMember') > -1 || permissionsList.indexOf('jvs_app_unload') > -1)) || permissionsList.indexOf('jvs_app_deploy_template') > -1">
                <el-popover
                  placement="right"
                  width="180"
                  trigger="click">
                  <ul class="application-manage-tool-list">
                    <li v-if="item.appRoles.indexOf('adminMember') > -1" @click="handleEdit(item)">
                      <svg class="icon" aria-hidden="true" style="margin-right: 12px;width: 15px;height: 15px;">
                        <use :xlink:href="'#'+ 'icon-edit-filling'"></use>
                      </svg>编辑应用</li>
                    <li v-if="(item.appRoles.indexOf('adminMember') > -1 && !item.isDeploy) && (modeUserInfo ? (['GA'].indexOf(modeUserInfo.mode) > -1) : true)" @click="handlePublish(item)">
                      <svg class="icon" aria-hidden="true" style="margin-right: 12px;width: 15px;height: 15px;">
                        <use :xlink:href="'#'+ 'icon-top-filling'"></use>
                      </svg>发布应用</li>
                    <li v-if="(permissionsList.indexOf('jvs_app_unload') > -1 && item.isDeploy) && (modeUserInfo ? (modeUserInfo.mode == 'GA') : true)" @click="handleUnload(item)">
                      <svg class="icon" aria-hidden="true" style="margin-right: 12px;width: 15px;height: 15px;">
                        <use :xlink:href="'#'+ 'icon-delete-filling'"></use>
                      </svg>卸载应用</li>
                    <li v-if="permissionsList.indexOf('jvs_app_deploy_template') > -1" @click="handlePublishToTemp(item)">
                      <svg class="icon" aria-hidden="true" style="margin-right: 12px;width: 15px;height: 15px;">
                        <use :xlink:href="'#'+ 'icon-caps-unlock-filling'"></use>
                      </svg>创建模板</li>
                    <li v-if="permissionsList.indexOf('jvs_app_recommendation') > -1" @click="starHandle(item)">
                      <i class="el-icon-star-on" :style="`margin-right: 12px;font-size: 18px;margin-top: 3px;width: 15px;height: 15px;${item.recommend ? 'color: #ff9736;' : ''}`"></i>{{item.recommend ? '取消推荐' : '设置推荐'}}</li>
                    <li v-if="item.appRoles.indexOf('adminMember') > -1 && !item.isDeploy" @click="handleDelete(item)">
                      <svg class="icon" aria-hidden="true" style="margin-right: 12px;width: 15px;height: 15px;fill: #F56C6C;">
                        <use :xlink:href="'#'+ 'icon-delete-filling'"></use>
                      </svg><span style="color: #F56C6C;">删除应用</span></li>
                  </ul>
                  <i class="el-icon-more" slot="reference"></i>
                </el-popover>
              </div>
            </div>
          </div>
          <div v-else class="page-target-none">
            <div style="text-align: center">
              <div style="color: #333333;font-size: 16px;margin-bottom: 50px;">暂无应用服务</div>
              <div style="color: #a2a3a5;font-size: 12px;">通过以上步骤10分钟快速学习如何搭建轻应用！</div>
            </div>
          </div>
          <div v-if="page.total > 15 && myTemp && myTemp.length > 0" class="app-pagination">
            <el-pagination
              background
              layout="prev, pager, next"
              :page-size="page.size"
              :pager-count="5"
              :current-page="page.current"
              @current-change="handleCurrentChange"
              :total="page.total">
            </el-pagination>
          </div>
        </div>
        <div v-if="current === 'temp'" class="other-template-list">
          <div class="header">
            <div class="template-types">
              <div v-for="(item, key) in templateTypes" :key="key" class="types-item" :style="currentType == item ? 'background-color: #e1e2e4;' : ''" @click="handleTypeClick(item)">{{item}}</div>
            </div>
          </div>
          <div class="template-list">
            <div v-if="templateListLoading" class="loading-back"/>
            <div class="template-item" v-for="(item, key) in templateList" :key="key">
              <div @click="handleClick(item)">
                <img style="height: 185px;width: 100%;object-fit: cover;" :src="item.imgs[0]" alt=""/>
                <div class="item-content">
                  <div class="item-header">
                    <h5>{{ item.name }}</h5>
                  </div>
                  <div class="item-description">
                    {{ item.description }}
                  </div>
                  <div class="item-tag">
                    <el-tag size="mini" style="margin-right: 8px" type="info">{{ item.primitive ? '原生应用' : '轻应用' }}</el-tag>
                  </div>
                </div>
              </div>
              <!-- 暂时屏蔽 -->
              <div v-if="false" class="template-type-status">
                <span style="margin-right:10px;">{{ item.platform }}</span>
              </div>
              <div class="more-handle">
                <el-popover
                  v-if="permissionsList.indexOf('jvs_app_template_edit' > -1) ||
                  permissionsList.indexOf('jvs_app_template_down' > -1) ||
                  permissionsList.indexOf('jvs_app_template_delete' > -1)"
                  placement="right"
                  width="50"
                  trigger="click">
                  <ul class="application-manage-tool-list" v-if="permissionsList.indexOf('jvs_app_template_edit' > -1)">
                    <li @click="handleEditTemp(item)">
                      <svg class="icon" aria-hidden="true" style="margin-right: 12px;width: 15px;height: 15px;">
                        <use :xlink:href="'#'+ 'icon-edit-filling'"></use>
                      </svg>编辑模板
                    </li>
                  </ul>
                  <ul class="application-manage-tool-list" v-if="permissionsList.indexOf('jvs_app_template_down' > -1) && $functionEnable('应用模板下载') && !item.primitive">
                    <li @click="handleDownloadTemp(item)">
                      <svg class="icon" aria-hidden="true" style="margin-right: 12px;width: 15px;height: 15px;">
                        <use :xlink:href="'#'+ 'icon-decline-filling'"></use>
                      </svg>下载模板
                    </li>
                  </ul>
                  <ul class="application-manage-tool-list" v-if="permissionsList.indexOf('jvs_app_template_delete' > -1)">
                    <li  @click="handleDeleteTemp(item)">
                      <svg class="icon" aria-hidden="true" style="margin-right: 12px;width: 15px;height: 15px;fill: #F56C6C;">
                        <use :xlink:href="'#'+ 'icon-delete-filling'"></use>
                      </svg><span style="color: #F56C6C;">删除模板</span>
                    </li>
                  </ul>
                  <i class="el-icon-more" slot="reference"></i>
                </el-popover>
                <div v-else style="color: #c5c5c5; font-size: 12px">{{ getTempSize(item.size) }}</div>
              </div>
            </div>
          </div>
          <div class="temp-pagination">
            <el-pagination
              background
              layout="prev, pager, next"
              :page-size="templatePage.size"
              :pager-count="5"
              :current-page="templatePage.current"
              @current-change="templatePageChange"
              :total="templatePage.total">
            </el-pagination>
          </div>
        </div>
      </div>
    </div>

    <!-- 文件创建应用 -->
    <!-- 导入数据 -->
    <el-dialog
      title="导入应用"
      :visible.sync="tempFileDialogVisible"
      width="720px"
      append-to-body
      :close-on-click-modal="false"
      :before-close="tempFileHandleClose">
      <div class="import-data-box">
        <div class="uploading-box" v-show="uploading">
          <div>
            <i class="el-icon-loading"></i>
          </div>
          <div>正在上传...</div>
        </div>
        <el-upload
          v-show="!uploading"
          class="import-data-upload"
          ref="uploadBtn"
          :action="(modeUserInfo && modeUserInfo.mode == 'BETA') ? `/mgr/jvs-design/base/app/version/fileUpload` : `/mgr/jvs-design/base/JvsAppTemplate/fileUpload/app`"
          :headers="header"
          :file-list="tempfileList"
          :show-file-list="false"
          :before-upload="beforeTempUpload"
          :on-change="onTempChange"
          :on-success="handleAppSuccess"
          :on-error="errTempHandle"
          drag
          multiple>
          <div class="el-upload__text">
            <svg aria-hidden="true" style="width: 24px; height: 24px;margin-bottom: 16px">
              <use xlink:href="#icon-upload"></use>
            </svg>
            <div>点击或者拖动文件到虚线框内上传</div>
            <div style="color: #a2a3a5;font-size: 12px;margin-top: 8px;">支持jvs类型的{{(modeUserInfo && modeUserInfo.mode == 'BETA') ? '版本' : ''}}文件</div>
          </div>
        </el-upload>
        <div class="upload-explain">
          <span style="color: #a2a3a5;">上传的文件符合以下规范：</span>
          <ul>
            <li style="list-style: disc">仅支持<span>（*.jvs）{{(modeUserInfo && modeUserInfo.mode == 'BETA') ? '版本' : ''}}</span>文件</li>
            <li style="list-style: disc">被修改后的加密文件<span>无法被正常导入</span></li>
            <li style="list-style: disc">导入应用后可以操作其它功能不影响使用,导入完成后会自动通知</li>
          </ul>
        </div>
      </div>
    </el-dialog>
    <el-dialog
      class="template-dialog"
      :title="title"
      append-to-body
      :visible.sync="dialogVisible"
      :before-close="handleClose">
      <div style="padding: 0 10px;" v-if="dialogVisible">
        <jvs-form :option="formOption" :formData="formData" @submit="handleSubmit" @cancalClick="handleClose">
          <template slot="deployDataModelIdsForm">
            <jvs-button @click="selectDataHandle">选择</jvs-button>
          </template>
          <template slot="logoForm">
            <div v-if="formData.logo" class="select-image-show">
              <img :src="formData.logo" alt="">
              <i v-if="submitType !== 'template'" class="el-icon-delete delete-select-image-tool" @click="delIamgeSelect('logo')"></i>
            </div>
            <jvs-button v-else @click="chooseImage('logo')">选择图片</jvs-button>
          </template>
          <template slot="imgsForm">
            <div style="display: flex;margin-bottom: 12px;width: 100%;flex-wrap: wrap">
              <div class="select-image-show" style="margin: 0 30px 20px 0;" v-for="(item, key) in imgDesc" :key="key">
                <img :src="item" alt="">
                <i class="el-icon-delete delete-select-image-tool" @click="delIamgeSelect('imgs', item)"></i>
              </div>
            </div>
            <jvs-button v-if="['editTemp', 'template', 'addTemplate'].indexOf(submitType) > -1" @click="chooseImage('imgs')">选择图片</jvs-button>
          </template>
          <template slot="bannerForm">
            <div v-if="['editTemp', 'template'].indexOf(submitType) > -1 && formData.banner" class="select-image-show">
              <img :src="formData.banner" alt="">
              <i class="el-icon-delete delete-select-image-tool" @click="delIamgeSelect('banner')"></i>
            </div>
            <jvs-button v-else @click="chooseImage('banner')">选择图片</jvs-button>
          </template>
          <template slot="freeForm">
            <el-switch v-model="formData.free"></el-switch>
          </template>
          <template slot="priceForm">
            <el-input-number :controls="false" :disabled="formData.free" :min="0" size="mini" v-model="formData.price"></el-input-number>
          </template>
          <template slot="typeForm">
            <el-input size="mini" v-model="formData.type"></el-input>
          </template>
        </jvs-form>
      </div>
    </el-dialog>
    <!-- 数据选择 -->
    <el-dialog
      class="select-data-dialog"
      title="数据选择"
      append-to-body
      :visible.sync="selectDataVisible"
      :before-close="selectDataClose">
      <div v-if="selectDataVisible" class="data-select-body">
        <div class="tips-text">选择需要发布到模板的数据模型。最多不超过5000条数据，一个模型最多100条数据</div>
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
    <imageSelect ref="logoSelect"
      title="选择图片"
      :dialogVisible="chooseAble"
      :paramInfo="{'module': 'application'}"
      @handleConfirm="handleConfirm"
      @handleClose="chooseAble = false;"
    ></imageSelect>
    <template-detail
      ref="templateDetail"
      :detailVisible="detailVisible"
      :modeUserInfo="modeUserInfo"
      @handleDetailClose="handleDetailClose"
      @handleSelect="handleSelect"
    />
    <!-- 安装列表 -->
    <install-list :installListVisible="installListVisible" @close="installClose"></install-list>

    <!-- 删除确认 -->
    <el-dialog
      class="select-data-dialog hide-header"
      title="确认删除应用"
      append-to-body
      :visible.sync="deleteVisible"
      :before-close="deleteClose">
      <div v-if="deleteVisible" class="delete-confirm-box">
        <div class="title">
          <i class="el-icon-warning"></i>
          <span>警告</span>
        </div>
        <div class="content">该操作将<i>永久删除</i>应用"<b>{{`${deleteRow.name}`}}</b>"的<i>所有数据</i>，同时删除关联的所有版本应用及数据，且<i>无法还原</i>，请谨慎操作！</div>
        <div class="input-box">
          <p>如确定删除，请输入应用名称</p>
          <el-input v-model="deleteRow.appName" size="mini"></el-input>
        </div>
      </div>
      <div v-if="deleteVisible" class="footer">
        <div class="footer-button">
          <div class="ftb" @click="deleteClose">取消</div>
          <div :class="{'ftb submit': true, 'disabled': !(deleteRow.appName && deleteRow.name == deleteRow.appName)}" @click="deleteSubmit">确定</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import {getStore} from "@/util/store";
import tmpcenterbg from "@/const/application/templatecenter.png"
import temtopbg from "@/const/application/backimg.png"
import helpdoc from "@/const/application/helpdoc.png"
const imgUrl = 'https://img2.baidu.com/it/u=3304620759,1323144983&fm=253&fmt=auto&app=138&f=JPEG?w=756&h=500'
import imageSelect from '@/components/basic-assembly/ImageSelect'
import step1 from '@/styles/template/step1.png'
import step2 from '@/styles/template/step2.png'
import step3 from '@/styles/template/step3.png'
import step4 from '@/styles/template/step4.png'
import {
  add,
  del,
  delTemplate, downloadTemplate,
  edit, editTemplate, getTemplateType, getApplicationMenu,
  pageList,
  publishToTemplate,
  templateList,
  getRecommendApp,
  addTemplate,
  betaUse, GASuspend,
  getDataModel,
  starApplication,
} from "./api";
import TemplateDetail from "./templateDetail";
import store from "@/store";
import {mapGetters} from "vuex";
import PlatformPageHeader from "@/components/page-header/PlatformPageHeader";
import installList from "./installList"
export default {
  props: {
    changeModeUserRadom: {
      type: Number
    },
    jvsDesign: {
      type: Object
    }
  },
  components: {
    PlatformPageHeader,
    TemplateDetail,
    imageSelect,
    installList
  },
  data() {
    return {
      templateTypes: [], // 模板类型
      current: 'app',
      currentType: undefined, // 当前选中类型
      recommendAppList: [], // 推荐应用
      templatePage: {
        total: 0,
        size: 12,
        current: 1
      },
      page: {
        total: 0,
        size: 15,
        current: 1
      },
      dataLoading: false,
      imgs: [],
      fileList: [],
      header: {
        "Authorization": 'Bearer ' + store.getters.access_token,
      },
      paramData: {
        bucketName: 'jvs-public',
        module: 'jvsapplogo',
        label: ''
      }, // 上传参数
      detailVisible: false,
      detailInfo: {},
      queryParams: {},
      templateList: [],
      templateListLoading: false,
      appId: '',
      submitType: 'create',
      img: imgUrl,
      title: '', // 弹框标题
      dialogVisible: false,
      formOption: {
        formAlign: 'top',
        emptyBtn: false,
        submitBtnText: '确定',
        submitLoading: false,
        column: [
          {
            label: '名称',
            prop: 'name',
            maxlength: 30,
            rules: [
              { required: true, message: '请输入名称', trigger: 'blur' }
            ],
          },
          // {
          //   label: "分类",
          //   prop: "type",
          //   type: "select",
          //   dicUrl: '/mgr/jvs-auth//api/dict/type/jvsapp',
          //   dicData: [],
          //   rules: [
          //     { required: true, message: '请选择分类', trigger: 'blur' }
          //   ],
          // },
          {
            label: 'logo（建议64 × 64）',
            prop: 'logo',
            formSlot: true
          },
          {
            label: '描述',
            type: 'textarea',
            prop: 'description'
          },
          {
            label: '图片说明',
            prop: 'imgs',
            formSlot: true,
            // display: false,
          },
          {
            label: '是否发布数据',
            prop: 'deployData',
            type: 'switch',
            defaultValue: false
          },
          {
            label: '数据选择',
            prop: 'deployDataModelIds',
            formSlot: true,
            displayExpress: [{ prop: 'deployData', value: 'true' }]
          },
          {
            label: 'banner',
            prop: 'banner',
            formSlot: true
          },
          {
            label: '是否免费',
            prop: 'free',
            display: false,
            formSlot: true,
          },
          {
            label: '价格（元）',
            prop: 'price',
            display: false,
            formSlot: true,
          },
          {
            label: '类型',
            prop: 'type',
            display: false,
            formSlot: true,
          }
        ]
      },
      formData: {},
      chooseAble: false,
      imgProp: '',
      imgDesc: [],
      // 展示数据
      quickTemplateList: [
        {title: '1.轻应用', desc: '轻应用可以做什么?', img: step1, label: 'jvs-app-help'},
        {title: '2.搭建应用', desc: '如何搭建应用?', img: step2, label: 'jvs-app-setup'},
        {title: '3.创建应用', desc: '如何创建第一个应用?', img: step3, label: 'jvs-app-create'},
        {title: '4.访问应用', desc: '如何访问我的应用?', img: step4, label: 'jvs-app-view'}
      ],
      myTemp: [],
      tempFileDialogVisible: false,
      limitShow: false,
      tempfileList: [],
      currentFile: null,
      queryString: '',
      bannerFileList: [],
      permissionsList: [],
      uploading: false,
      modeUserInfo: null,
      tmpcenterback: tmpcenterbg,
      temtopback: temtopbg,
      helpdocback: helpdoc,
      publishStatus: 'all',
      publishStatusList: [
        {label: '全部', value: 'all'},
        {label: '已发布', value: 'pub'},
        {label: '未发布', value: 'unpub'}
      ],
      installListVisible: false,
      selectDataVisible: false,
      tableLoading: false,
      dataPageList: [],
      dataSelectPage: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 8, // 每页显示多少条
      },
      keyword: '',
      selectedList: [],
      deleteVisible: false,
      deleteRow: null
    }
  },
  computed: {
    ...mapGetters(['menuAll', 'system']),
    selectedListInfo () {
      let list = []
      for(let i in this.selectedList) {
        list.push(this.selectedList[i].id)
      }
      return list
    }
  },
  mounted() {
  },
  async created () {
    if(!sessionStorage.getItem('getTimes')) {
      // this.showInstallList()
      sessionStorage.setItem('getTimes', 'once')
    }
    this.dataLoading = true
    if(this.jvsDesign && this.jvsDesign.JVS_DESIGN_MGR && this.$permissionMatch('jvs_app')) {
      this.modeUserInfo = getStore({ name: 'modeUserInfo' })
    }
    await this.permissionMatch()
    await this.getTemplateType()
    await this.getMyApplication()
    await this.getTemplateList()
    await this.getRecommendApp()
  },
  methods: {
    permissionMatch() {
      this.permissionsList = getStore({name: 'permissions'})
    },
    // 打开更多模板
    openTemplateMore() {
      this.$emit('openTemplateMore', true)
    },
    // 获取模板应用大小
    getTempSize(size) {
      return (size / 1024).toFixed(2) + 'K'
    },
    // handleHeaderClick
    handleHeaderClick(type) {
      this.current = type
    },
    // 模板类型点击事件
    handleTypeClick(type) {
      this.currentType = type
      this.getTemplateList()
    },
    // 文件上传成功
    handleTemplateSuccess (response, fileList) {
      // console.log(response)
      if (response && response.code === 0) {
        this.getTemplateList()
      }
    },
    // 文件创建应用成功
    handleAppSuccess (response, fileList) {
      // console.log(response)
      if (response && response.code === 0) {
        this.uploading = false
        this.tempFileHandleClose()
        this.getMyApplication()
        this.$notify({
          title: '提示',
          message: '导入成功',
          position: 'bottom-right',
          type: 'success'
        });
        this.showInstallList()
      } else {
        this.uploading = false
        this.$notify({
          title: '提示',
          message: response.msg,
          position: 'bottom-right',
          type: 'error'
        });
      }
    },
    // 上传模板前的处理
    beforeTempUpload(file) {
      this.uploading = true
      // 取消校验
      return true
      const isLt20M = file.size / 1024 / 1024 < 20;
      const jvs = file.name.substr(file.name.length - 3, 3)
      if (jvs !== 'jvs') {
        this.$refs.uploadBtn.clearFiles()
        this.tempfileList = []
        // this.$message.error('上传文件的格式只能是JVS格式！');
        this.$notify({
          title: '提示',
          message: '上传文件的格式只能是JVS格式！',
          position: 'bottom-right',
          type: 'error'
        });
      }
      if (!isLt20M)  {
        this.limitShow = true
        this.$refs.uploadBtn.clearFiles()
        this.tempfileList = []
        // this.$message.error('上传的文件大小不能超过 20MB!');
        this.$notify({
          title: '提示',
          message: '上传的文件大小不能超过 20MB!',
          position: 'bottom-right',
          type: 'error'
        });
      }else{
        this.limitShow = false
      }
      return jvs === 'jvs';
    },
    onTempChange (file, fileList) {
      this.uploading = false
      this.tempfileList = fileList
    },
    errTempHandle (err, file, fileList) {
      this.uploading = false
      this.$refs.uploadBtn.clearFiles()
      this.tempfileList = []
      // this.$message.error(err)
      this.$notify({
        title: '提示',
        message: err,
        position: 'bottom-right',
        type: 'error'
      });
    },
    tempFileHandleClose () {
      this.tempfileList = []
      this.limitShow = false
      this.$refs.uploadBtn.clearFiles()
      this.tempFileDialogVisible = false
      this.uploading = false
    },
    submitFile () {
      this.$refs.uploadBtn.submit()
    },
    // 获取tag名字
    getTagName(obj) {
      let header = ''
      switch (obj.type) {
        case 'user':
          break;
        case 'role':
          header = '（角色）';
          break;
        case 'dept':
          header = '（部门）';
          break;
        case 'group':
          header = '（群组）';
          break;
        default: break;
      }
      return header + obj.name
    },
    handleDelUser(id, arr) {
      const index = arr.findIndex(item => {
        return item.id === id
      })
      arr.splice(index, 1)
      this.$forceUpdate()
    },
    // 快捷入口
    handleClickQuick(str, title) {
      if (str.split('-')[0] === 'video') {
        let url = ''
        this.$store.state.common.systemHelpDict.forEach(item => {
          if (item.label === str) {
            url = item.value
          }
        })
        this.$videoOpen({
          title: title,
          videoUrl: url
        })
      } else {
        this.$openUrl('', '_blank', str)
      }
    },
    // 删除模板
    handleDeleteTemp(obj) {
      this.$confirm('此操作将永久删除此数据, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        delTemplate(obj.id).then(res => {
          if(res.data.code == 0) {
            // this.$message.success('删除成功')
            this.$notify({
              title: '提示',
              message: '删除成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.getTemplateList()
          }
        })
      }).catch(e => {})
    },
    // 编辑模板
    handleEditTemp(obj) {
      this.formData = JSON.parse(JSON.stringify(obj))
      this.imgDesc = [...obj.imgs]
      // this.fileList = this.formData.imgs.map(item => {
      //   return { name: '', url: item }
      // })
      this.formOption.column.forEach((item, index) => {
        if (item.prop === 'imgs') {
          item.display = true
        } else {
          item.disabled = false
        }
        if (index > 3) {
          item.display = true
        }
      })
      this.title = '编辑应用模板'
      this.submitType = 'editTemp'
      this.dialogVisible = true
    },
    // 下载模板
    handleDownloadTemp(obj) {
      downloadTemplate(obj).then(res => {
        if (res.data) {
          let name = res.headers["content-disposition"].split(";")[1]
          name = name.split("=")[1]
          this.downloadFile(name, res.data)
        }
      })
    },
    // 下载文件
    downloadFile(filename, content) {
      var elink = document.createElement('a')
      if(filename) {
        elink.download = filename
      }
      elink.style.display = 'none'

      var blob = new Blob([content],{}) //,{type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8'})
      elink.href = URL.createObjectURL(blob)
      document.body.appendChild(elink)
      elink.click()
      document.body.removeChild(elink)
    },
    // 点击我的应用
    async handleClickMyApp(obj) {
      const index = this.menuAll.findIndex(item => {
        return item.id === obj.id
      })
      if(index === -1) {
        await getApplicationMenu(obj.id).then(res => {
          if (res.data && res.data.code == 0 && res.data.data) {
            this.menuAll.push(res.data.data)
            const arr = [...this.menuAll]
            this.$store.commit("SET_MENU_ALL", arr)
          }
        })
      }
      this.$emit('openSystem', obj.id)
      this.$emit('openTemplate', false)
    },
    // 图片上传成功
    handleImgSuccess(res, file, fileList) {
      const imgList = fileList.map(item => {
        return item.response.data.fileLink
      })
      // this.imgs = [...imgList]
      this.formData.imgs = [...imgList]
    },
    // 删除图片操作
    handleRemove(file, fileList) {
      const imgList = fileList.map(item => {
        return item.response.data.fileLink
      })
      this.formData.imgs = [...imgList]
    },
    // 上传图片前的处理
    beforeUpload(file) {
      const isJPG = ['image/jpeg', 'image/png'].indexOf(file.type) > -1
      const isLt2M = file.size / 1024 / 1024 < 20;
      if (!isLt2M) {
        // this.$message.error('上传图片大小不能超过 20MB!');
        this.$notify({
          title: '提示',
          message: '上传图片大小不能超过 20MB!',
          position: 'bottom-right',
          type: 'error'
        });
      }
      if (!isJPG) {
        // this.$message.error('只能上传图片！');
        this.$notify({
          title: '提示',
          message: '只能上传图片！',
          position: 'bottom-right',
          type: 'error'
        });
      }
      return isLt2M && isJPG;
    },
    // banner上传成功
    bannerImgSuccess(res, file, fileList) {
      let imgList = fileList.map(item => {
        return item.response.data.fileLink
      })
      // this.imgs = [...imgList]
      this.formData.banner = [...imgList]
    },
    // 删除banner
    bannerRemove (file, fileList) {
      let imgList = fileList.map(item => {
        return item.response.data.fileLink
      })
      this.formData.banner = [...imgList]
    },
    // 选择模板
    handleSelect(detailInfo, callback) {
      const params = {
        templateId: detailInfo.id,
        online: detailInfo.online,
        platformId: detailInfo.platformId
      }
      add(params).then(res => {
        if (res.data && res.data.code == 0) {
          this.showInstallList()
          callback(true)
        } else {
          callback(false)
        }
      }).catch(err => {
        callback(false)
      })
    },
    // 关闭模板详情弹窗
    handleDetailClose() {
      this.detailVisible = false
    },
    // 点击模板
    handleClick(obj) {
      this.detailVisible = true
      this.$refs.templateDetail.geTemplateDetail(obj)
    },
    // 获取模板类型
    getTemplateType() {
      getTemplateType().then(res => {
        if (res.data && res.data.code == 0) {
          this.templateTypes = res.data.data || []
        }
      })
    },
    // 获取推荐应用
    getRecommendApp() {
      getRecommendApp({size: 8}).then(res => {
        if (res.data && res.data.code == 0) {
          this.recommendAppList = res.data.data || []
        }
      })
    },
    // 获取推荐应用模板
    getTemplateList() {
      // this.templateList = []
      this.templateListLoading = true
      const params = {
        size: this.templatePage.size,
        current: this.templatePage.current,
        type: this.currentType
      }
      templateList(params).then(res => {
        if (res.data && res.data.data && res.data.code == 0) {
          // const localList = res.data.data.localList || []
          // const onlineList = res.data.data.onlineList || []
          this.templateList = res.data.data.records || []
          this.templatePage.total = res.data.data.total
        }
        this.templateListLoading = false
      })
    },
    // 发布应用
    handlePublish(obj) {
      this.$confirm('是否确认发布？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // publish
        betaUse(obj.id).then(res => {
          if (res.data && res.data.code == 0) {
            // this.$message.success('发布成功')
            this.$notify({
              title: '提示',
              message: '发布成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.getMyApplication()
          }
        })
      }).catch(e => {})
    },
    // 推荐/取消推荐
    starHandle (obj) {
      starApplication(obj.id, {recommend: !obj.recommend}).then(res => {
        if (res.data && res.data.code == 0) {
          this.$notify({
            title: '提示',
            message: obj.recommend ? '取消推荐成功' : '推荐成功',
            position: 'bottom-right',
            type: 'success'
          });
          this.getMyApplication()
        }
      })
    },
    // 卸载应用
    handleUnload(obj) {
      this.$confirm('卸载后所有的人无法操作，是否确定？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // unload
        GASuspend(obj.id).then(res => {
          if (res.data && res.data.code == 0) {
            // this.$message.success('卸载成功')
            this.$notify({
              title: '提示',
              message: '卸载成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.getMyApplication()
          }
        })
      }).catch(e => {})
    },
    // 删除应用
    handleDelete (obj) {
      this.deleteRow = JSON.parse(JSON.stringify(obj))
      this.deleteVisible = true
    },
    handleCurrentChange(val) {
      this.page.current = val
      this.getMyApplication()
    },
    templatePageChange(val) {
      this.templatePage.current = val
      this.getTemplateList()
    },
    // 获取我的应用
    getMyApplication () {
      this.dataLoading = true
      let query={
        current: this.page.current,
        size: this.page.size,
        name: this.queryParams.name ? this.queryParams.name : undefined
      }
	  if(this.publishStatus == 'pub') {
        query.isDeploy = true
      }
      if(this.publishStatus == 'unpub') {
        query.isDeploy = false
      }
      this.queryString = this.queryParams.name || ''
      pageList(query).then(res => {
        if (res.data && res.data.code == 0) {
          this.myTemp = [...res.data.data.records] || []
          this.page.total = res.data.data.total
          this.dataLoading = false
        } else {
          this.dataLoading = false
        }
      }).catch(err => {
        this.dataLoading = false
      })
    },
    // 从模板创建应用
    handleAddFromTemp() {
      const el = document.getElementById('template')
      if (el.scrollHeight > el.clientHeight) {
        el.scrollTop = el.scrollHeight
      }
    },
    // 文件创建应用
    createAppByFile () {
      this.tempFileDialogVisible = true
    },
    // 新增应用
    handleAdd () {
      this.formOption.column.forEach((item,index) => {
        if (item.prop === 'imgs' || item.prop == 'deployData' || item.prop == 'deployDataModelIds') {
          item.display = false
        } else {
          item.disabled = false
        }
        if (index > 3) {
          item.display = false
        }
      })
      this.title = '新增应用'
      this.formData = {}
      this.dialogVisible = true
      this.submitType = 'create'
    },
    // 编辑应用
    handleEdit(obj) {
      this.formData = obj
      this.formOption.column.forEach((item, index) => {
        if (item.prop === 'imgs' || item.prop == 'deployData' || item.prop == 'deployDataModelIds') {
          item.display = false
        } else {
          item.disabled = false
        }
        if (index > 3) {
          item.display = false
        }
      })
      this.title = '编辑应用'
      this.dialogVisible = true
      this.submitType = 'edit'
      this.appId = obj.id
    },
    // 添加应用模板
    handleAddTemp() {
      this.formOption.column.forEach((item, index) => {
        if (item.prop === 'imgs') {
          item.display = true
        } else {
          // item.disabled = true
        }
        if (index > 3) {
          item.display = false
        }
      })
      this.title = '添加应用模板'
      this.dialogVisible = true
      this.submitType = 'addTemplate'
    },
    // 发布应用到模板
    handlePublishToTemp(obj) {
      this.formData = JSON.parse(JSON.stringify(obj))
      this.formOption.column.forEach((item, index) => {
        if (item.prop === 'imgs') {
          item.display = true
        } else {
          item.disabled = true
        }
        if (index > 3) {
          item.display = false
        }
        if(['deployData', 'deployDataModelIds'].indexOf(item.prop ) > -1) {
          item.display = true
          item.disabled = false
        }
      })
      this.title = '发布到模板市场'
      this.dialogVisible = true
      this.submitType = 'template'
    },
    // 提交应用表单
    handleSubmit (form) {
      form.imgs = [...this.imgDesc]
      if (this.submitType === 'create') {
        this.formOption.submitLoading = true
        add(form).then(res => {
          this.formOption.submitLoading = false
          if(res.data && res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: '新增成功',
              position: 'bottom-right',
              type: 'success'
            })
            this.dialogVisible = false
            this.getMyApplication()
          }else{
            this.dialogVisible = false
          }
        }).catch(e => {
          this.formOption.submitLoading = false
        })
      }
      if (this.submitType === 'edit') {
        this.formOption.submitLoading = true
        edit(form).then(res => {
          this.formOption.submitLoading = false
          if(res.data && res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: '编辑成功',
              position: 'bottom-right',
              type: 'success'
            })
            this.dialogVisible = false
            this.getMyApplication()
          }
        }).catch(e => {
          this.formOption.submitLoading = false
        })
      }
      if (this.submitType === 'template') {
        // if (!(form.imgs && form.imgs.length > 0)) {
        //   this.$notify({
        //     title: '提示',
        //     message: '请上传图片说明',
        //     position: 'bottom-right',
        //     type: 'error'
        //   });
        //   return
        // }
        this.$confirm('是否确认发布？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.formOption.submitLoading = true
          publishToTemplate(form).then(res => {
            if (res.data && res.data.code == 0) {
              // this.$message.success('发布成功')
              this.$notify({
                title: '提示',
                message: '发布成功',
                position: 'bottom-right',
                type: 'success'
              });
              this.dialogVisible = false
              this.getTemplateList()
            }
            this.formOption.submitLoading = false
          })
        }).catch(e => {
          this.formOption.submitLoading = false
        })
      }
      if (this.submitType === 'addTemplate') {
        addTemplate(form).then(res => {
          if (res.data && res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: '新增成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.dialogVisible = false
            this.getTemplateList()
          }
        })
      }
      if (this.submitType === 'editTemp') {
        editTemplate(form).then(res => {
          if (res.data && res.data.code == 0) {
            // this.$message.success('编辑成功！')
            this.$notify({
              title: '提示',
              message: '编辑成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.dialogVisible = false
            this.getTemplateList()
          }
        })
      }
    },
    handleClose () {
      this.dialogVisible = false
      this.formOption.column.forEach((item, index) => {
        item.disabled = false
      })
      this.formData = {}
      this.fileList = []
      this.selectedList = []
    },
    // 选择图片
    chooseImage (prop) {
      this.imgProp = prop
      this.chooseAble = true
      this.$refs.logoSelect.init()
    },
    // 确认图片
    handleConfirm (value) {
      this.chooseAble = false;
      if(value && value.fileLink) {
        if (this.imgProp === 'logo') {
          this.$set(this.formData, this.imgProp, value.fileLink)
        }
        if (this.imgProp === 'imgs') {
          this.imgDesc.push(value.fileLink)
        }
        if (this.imgProp === 'banner') {
          this.$set(this.formData, this.imgProp, value.fileLink)
        }
      }
    },
    // 删除已选图片
    delIamgeSelect (prop, src) {
      if (prop === 'logo' || prop === 'banner') {
        this.$set(this.formData, prop, '')
      }
      if (prop === 'imgs') {
        if (this.imgDesc.indexOf(src) > -1) {
          this.imgDesc.splice(this.imgDesc.indexOf(src), 1)
        }
      }
    },
    showInstallList () {
      this.installListVisible = true
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
        appId: this.formData.id
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
      this.$set(this.formData, 'deployDataModelIds', this.selectedListInfo)
      this.selectDataClose()
    },
    selectDataClose () {
      this.keyword = ''
      this.dataSelectPage.total = 0
      this.dataSelectPage.currentPage = 1
      this.dataPageList = []
      this.selectDataVisible = false
      this.selectedList = []
    },
    deleteSubmit () {
      if(this.deleteRow.appName && this.deleteRow.appName == this.deleteRow.name) {
        del(this.deleteRow.id, {appName: this.deleteRow.appName}).then(res => {
          if(res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: '删除成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.getMyApplication()
            let index = this.menuAll.findIndex(item => {
              return item.id === this.deleteRow.id
            })
            if(index > -1) {
              let list = JSON.parse(JSON.stringify(this.menuAll))
              list.splice(index, 1)
              this.$store.commit("SET_MENU_ALL", list)
            }
            if(this.deleteRow.id == this.system){
              this.$store.commit("SET_SYSTEM", "");
              this.$store.commit("DEL_APP_TAG", {id: this.deleteRow.id})
            }
            this.deleteClose()
          }
        })
      }
    },
    deleteClose () {
      this.deleteRow = null
      this.deleteVisible = false
    },
    selectAll () {
      this.dataPageList.filter(item => {
        this.selectHandle(item)
      })
      this.$forceUpdate()
    },
    installClose () {
      this.installListVisible = false
      this.getMyApplication()
    }
  },
  watch: {
    changeModeUserRadom: {
      handler (newVal, oldVal) {
        if(newVal > -1) {
          if(this.jvsDesign && this.jvsDesign.JVS_DESIGN_MGR && this.$permissionMatch('jvs_app')) {
            this.modeUserInfo = getStore({ name: 'modeUserInfo' })
          }
          this.getMyApplication()
        }
      }
    }
  }
}
</script>
<style lang="scss" scoped>
.header-box{
  display: flex;
  .header-item{
    height: 29px;
    font-size: 20px;
    font-family: Source Han Sans-Bold, Source Han Sans;
    font-weight: 700;
    color: #363B4C;
    line-height: 29px;
    .tab-bottom{
      height: 3px;
      width: 40px;
      margin: 0 auto;
      margin-top: 10px;
      border-radius: 3px;
      background-color: #3471ff;
    }
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
  .uploading-box{
    display: flex;
    background-color: rgb(247, 247, 247);
    width: 600px;
    border: 1px dashed rgb(217, 217, 217);
    border-radius: 6px;
    height: 180px;
    box-sizing: border-box;
    text-align: center;
    align-items: center;
    justify-content: center;
    flex-direction: column;
    margin-left: calc(50% - 300px);
  }
}
.icon{
  width: 40px;
  height: 40px;
  vertical-align: -0.15em;
  fill: currentColor;
  overflow: hidden;
}
.template-view-page{
  box-sizing: border-box;
  height: 100%;
  padding-bottom: 20px;
  overflow-y: auto;
  overflow-x: hidden;
  .template-manage-content{
    background: linear-gradient(180deg, #DDEBFF 0%, #FFFFFF 55%);
  }
  .template-item-quick{
    height: 582px;
    overflow: hidden;
    padding: 0 240px;
    box-sizing: border-box;
    position: relative;
    .tmp-top-bg{
      position: absolute;
      top: 0;
      right: 112px;
      display: block;
    }
    .tmp-back{
      .left1{
        width: 269px;
        height: 614px;
        background: #ECF4FF;
        opacity: 1;
        filter: blur(122px);
        position: absolute;
        left: 0px;
        top: -210px;
        transform: rotate(157.57deg);
        z-index: 0;
      }
      .left2{
        width: 160px;
        height: 614px;
        background: #E0F5FE;
        opacity: 1;
        filter: blur(122px);
        position: absolute;
        left: 250px;
        top: -250px;
        transform: rotate(157.57deg);
        z-index: 0;
      }
      .right1{
        width: 269px;
        height: 614px;
        background: #CFE6FE;
        opacity: 1;
        filter: blur(122px);
        position: absolute;
        right: -24px;
        top: -135px;
        transform: rotate(22.43deg);
        z-index: 0;
      }
      .right2{
        width: 92px;
        height: 614px;
        background: #F7FBFF;
        opacity: 1;
        filter: blur(122px);
        position: absolute;
        right: 196px;
        top: -146px;
        transform: rotate(22.43deg);
        z-index: 0;
      }
      .right3{
        width: 160px;
        height: 614px;
        background: #E0F5FE;
        opacity: 1;
        filter: blur(122px);
        position: absolute;
        right: 242px;
        top: -156px;
        transform: rotate(22.43deg);
        z-index: 0;
      }
    }
    .tmp-top{
      h1{
        margin: 0;
        margin-top: 47px;
        font-size: 32px;
        font-family: Source Han Sans-Bold, Source Han Sans;
        font-weight: 700;
        color: #363B4C;
        height: 46px;
        line-height: 46px;
        position: relative;
      }
      .tmp-top-desc{
        margin-top: 16px;
        height: 29px;
        font-size: 20px;
        font-family: Source Han Sans-Regular, Source Han Sans;
        font-weight: 400;
        color: #6F7588;
        line-height: 29px;
        position: relative;
      }
    }
    .tmp-search{
      margin-top: 48px;
      width: 700px;
      height: 48px;
      background: #FFFFFF;
      border-radius: 4px 4px 4px 4px;
      display: flex;
      align-items: center;
      /deep/.el-input{
        font-size: 14px;
        font-family: Source Han Sans-Regular, Source Han Sans;
        font-weight: 400;
        color: #6F7588;
        .el-input__inner{
          height: 48px;
          border: 0;
          padding-left: 48px;
        }
        .el-input__prefix{
          margin-top: 2px;
          left: 15px;
          .el-icon-search{
            font-size: 16px;
            color: #6F7588;
          }
        }
      }
      /deep/.el-input::placeholder{
        font-size: 14px;
        font-family: Source Han Sans-Regular, Source Han Sans;
        font-weight: 400;
        color: #6F7588;
      }
      /deep/.el-button--primary{
        width: 120px;
        height: 40px;
        background: #1E6FFF;
        border-radius: 4px 4px 4px 4px;
        margin: 4px;
        font-size: 14px;
        font-family: Source Han Sans-Regular, Source Han Sans;
        font-weight: 400;
        color: #FFFFFF;
      }
    }
    .tmp-bottom{
      margin-top: 49px;
      height: 160px;
      display: flex;
      align-items: center;
      .left{
        width: 160px;
        height: 160px;
        background: #FFFFFF;
        border-radius: 6px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        margin-right: 16px;
        cursor: pointer;
        position: relative;
        i{
          font-size: 18px;
          color: #1E6FFF;
        }
        span{
          font-size: 16px;
          font-family: Source Han Sans, Source Han Sans;
          font-weight: 400;
          color: #363B4C;
          margin-top: 16px;
        }
      }
      .center{
        height: 100%;
        flex: 1;
        display: flex;
        align-items: center;
        border-radius: 6px;
        position: relative;
        .tmp-center-back{
          position: absolute;
          width: 100%;
          height: 100%;
          left: 0;
          top: 0;
        }
        .tmp-center-text{
          position: absolute;
          left: 87px;
          top: 40px;
          font-size: 12px;
          font-family: Source Han Sans-Regular, Source Han Sans;
          font-weight: 400;
          color: #557CC1;
          line-height: 20px;
          word-break: keep-all;
          h2{
            height: 35px;
            font-size: 24px;
            font-family: Source Han Sans-Bold, Source Han Sans;
            font-weight: 700;
            color: #1E6FFF;
            line-height: 35px;
            margin: 0 0 8px 0;
          }
        }
        .center-button{
          position: absolute;
          right: 10px;
          top: 10px;
          border-radius: 4px;
          border: 1px solid #fff;
          padding: 4px 10px;
          color: #fff;
          cursor: pointer;
          font-size: 12px;
          font-family: Microsoft YaHei-Regular, Microsoft YaHei;
          font-weight: 400;
        }
      }
      .right{
        width: 368px;
        height: 100%;
        border-radius: 6px;
        color: #fff;
        margin-left: 16px;
        position: relative;
        overflow: hidden;
        >img{
          position: absolute;
          top: 0;
          right: 0;
        }
        .right-img-color-back{
          position: absolute;
          top: 0;
          right: 0;
          width: 216px;
          height: 160px;
          background: linear-gradient(164deg, #FFA857 0%, #FDC656 99%);
          border-radius: 6px 6px 6px 6px;
          opacity: 0.8;
        }
        .right-back{
          width: 100%;
          height: 100%;
          padding: 12px 24px;
          box-sizing: border-box;
          background: linear-gradient(164deg, #FFA857 0%, #FDC656 99%);
          .tmp-right-title{
            height: 23px;
            line-height: 23px;
            font-size: 16px;
            font-family: Source Han Sans-Bold, Source Han Sans;
            font-weight: 700;
          }
          .tmp-right-list{
            font-size: 14px;
            font-family: Source Han Sans-Regular, Source Han Sans;
            font-weight: 400;
            color: rgba(255,255,255,0.8);
            line-height: 20px;
            .tmp-right-list-item{
              margin-top: 8px;
              display: flex;
              align-items: center;
              justify-content: space-between;
              span{
                cursor: pointer;
                position: relative;
              }
            }
          }
        }
      }
    }
  }
  .template-recommend-app-box{
    margin-top: -123px;
    padding: 0 240px;
    position: relative;
    .template-recommend-app-back{
      margin-top: 16px;
      height: 224px;
      background: #FFFFFF;
      border-radius: 6px;
      overflow: hidden;
      padding: 16px 24px 32px 8px;
      box-sizing: border-box;
      display: flex;
      align-items: center;
      justify-content: space-between;
      .recommend-app-list{
        flex: 1;
        box-sizing: border-box;
        display: flex;
        flex-wrap: wrap;
        overflow: hidden;
        .recommend-app-list-item{
          width: calc(25% - 16px);
          margin-left: 16px;
          margin-top: 16px;
          height: 72px;
          background: #F6F9FF;
          border-radius: 6px;
          display: flex;
          align-items: center;
          cursor: pointer;
          box-sizing: border-box;
          padding: 12px 16px;
          .item-logo{
            border-radius: 6px;
            width: 48px;
            height: 48px;
            overflow: hidden;
            margin-right: 15px;
            img{
              display: block;
              width: 100%;
              height: 100%;
            }
          }
          .item-content{
            width: calc(100% - 48px);
            overflow: hidden;
            .item-name{
              margin: 0;
              overflow:hidden;
              white-space: nowrap;
              text-overflow: ellipsis;
              height: 20px;
              font-size: 14px;
              font-family: Source Han Sans-Medium, Source Han Sans;
              color: #3D3D3D;
              line-height: 20px;
              font-weight: 500;
            }
            .item-description{
              height: 17px;
              font-size: 12px;
              font-family: Source Han Sans-Regular, Source Han Sans;
              font-weight: 400;
              color: #6F7588;
              line-height: 17px;
              overflow:hidden;
              white-space: nowrap;
              text-overflow: ellipsis;
            }
          }
        }
      }
      .recommend-app-more{
        width: 256px;
        height: 160px;
        margin-left: 16px;
        background: linear-gradient(137deg, #B5DCFF 0%, #CCCBFF 100%);
        border-radius: 6px;
        padding: 45px 32px;
        box-sizing: border-box;
        background-image: url(../../const/application/moretemplate.png);
        background-repeat: no-repeat;
        background-size: 100%;
        h4{
          margin: 0;
          height: 23px;
          font-size: 16px;
          font-family: Source Han Sans-Regular, Source Han Sans;
          font-weight: 400;
          color: #333333;
          line-height: 23px;
        }
        p{
          margin: 0;
          margin-top: 15px;
          padding: 6px 10px;
          width: 104px;
          height: 32px;
          background: #FFFFFF;
          border-radius: 6px;
          overflow: hidden;
          box-sizing: border-box;
          font-size: 14px;
          font-family: Source Han Sans, Source Han Sans;
          font-weight: 400;
          color: #1E6FFF;
          cursor: pointer;
        }
      }
    }
  }
  .template-manage-box{
    position: relative;
    padding: 0 240px;
    margin-top: 36px;
    .template-manage-top{
      display: flex;
      align-items: center;
      justify-content: space-between;
      .left{
        display: flex;
        align-items: center;
        .template-manage-top-type{
          display: flex;
          align-items: center;
          margin-left: 32px;
          p{
            margin: 0;
            margin-right: 9px;
            padding: 0 16px;
            height: 32px;
            line-height: 32px;
            font-size: 14px;
            font-family: Source Han Sans-Regular, Source Han Sans;
            font-weight: 400;
            color: #6F7588;
            cursor: pointer;
          }
          p.active, p:hover{
            font-size: 16px;
            font-family: Source Han Sans-Medium, Source Han Sans;
            font-weight: 500;
            color: #1E6FFF;
            background-color: #fff;
            border-radius: 4px;
          }
        }
      }
      .right{
        display: flex;
        align-items: center;
        .right-button{
          width: 104px;
          height: 36px;
          background: #1E6FFF;
          border-radius: 4px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-left: 12px;
          cursor: pointer;
          .icon{
            width: 16px;
            height: 16px;
            fill: #fff;
          }
          span{
            margin-left: 3px;
            font-size: 14px;
            font-family: Microsoft YaHei-Regular, Microsoft YaHei;
            font-weight: 400;
            color: #FFFFFF;
          }
        }
      }
    }
    .my-template-list{
      margin-top: 16px;
      width: 100%;
      overflow: hidden;
      .box{
        display: grid;
        grid-template-columns: calc(20% - 12.8px) calc(20% - 12.8px) calc(20% - 12.8px) calc(20% - 12.8px) calc(20% - 12.8px);
        grid-row-gap: 16px;
        grid-column-gap: 16px;
        .loading-back{
          position: absolute;
          width: 100%;
          height: 100%;
          top: 0;
          left: 0;
          box-sizing: border-box;
          background-color: rgba(255, 255, 255, 0.8);
          background-image: url('../../../public/jvs-ui-public/img/loading.gif');
          background-repeat: no-repeat;
          background-position: center;
          background-position: center;
          //background-size: 200px 160px;
          z-index: 1000;
        }
        .my-template-list-item{
          position: relative;
          background: #FFFFFF;
          height: 216px;
          border-radius: 6px;
          border: 1px solid #EEEFF0;
          box-sizing: border-box;
          padding: 24px;
          box-sizing: border-box;
          .more-handle{
            color: #C2C5CF;
            position: absolute;
            right: 24px;
            top: 24px;
            font-size: 16px;
            cursor: pointer;
          }
          .content{
            box-sizing: border-box;
            cursor: pointer;
            .content-header{
              color: #333333;
              font-size: 15px;
              position: relative;
              img{
                width: 48px;
                height: 48px;
                border-radius: 6px;
                display: block;
              }
              h5{
                height: 20px;
                font-size: 14px;
                font-family: Source Han Sans-Bold, Source Han Sans;
                font-weight: 700;
                color: #363B4C;
                line-height: 20px;
                margin: 16px 0 0 0;
              }
            }
            .content-description{
              height: 40px;
              margin-top: 12px;
              font-size: 14px;
              font-family: Source Han Sans-Regular, Source Han Sans;
              font-weight: 400;
              color: #6F7588;
              line-height: 20px;
              word-wrap: break-word;
              overflow: hidden;
              text-overflow: ellipsis;
              display: -webkit-box;
              -webkit-line-clamp: 2;
              -webkit-box-orient: vertical;
            }
            .tool-div{
              margin-top: 12px;
              display: flex;
              justify-content: space-between;
              align-items: center;
              .el-tag {
                width: 48px;
                height: 20px;
                line-height: 20px;
                font-size: 12px;
                padding: 0;
                font-family: Source Han Sans-Regular, Source Han Sans;
                font-weight: 400;
                border: 0;
                text-align: center;
                border-radius: 4px;
                box-sizing: border-box;
                background: #DFF3E3;
                color: #36B452;
              }
              .el-tag--info{
                background: #FFF7EF;
                color: #FF9736;
              }
            }
          }
        }
      }
      .app-pagination{
        margin-top: 20px;
        text-align: right;
        /deep/.el-pagination{
          padding: 0;
          .btn-next{
            margin-right: 0;
          }
        }
      }
      .page-target-none{
        height: 250px;
        background-color: #ffffff;
        display: flex;
        align-items: center;
        justify-content: center;
        .no-data-img{
          width: 457px;
          height: 180px;
          background-image: url(/jvs-ui/static/img/emptyImage.ca3665f2.png);
          background-size: 260px 123px;
          background-repeat: no-repeat;
          background-position: center;
        }
      }
    }
    .other-template-list{
      padding-bottom: 20px;
      .header{
        display: flex;
        justify-content: space-between;
        margin-bottom: 8px;
        .template-types{
          display: flex;
          flex-wrap: wrap;
          width: 80%;
          .types-item{
            padding: 8px 12px;
            width: 50px;
            margin-bottom: 8px;
            margin-right: 20px;
            border-radius: 6px;
            text-align: center;
            cursor: pointer;
            &:hover{
              background-color: #e1e2e4;
            }
          }
        }
        .upload-btn{
          margin-bottom: 16px;
        }
      }
      .template-list{
        position: relative;
        width: 1120px;
        box-sizing: border-box;
        align-items: center;
        display: grid;
        grid-template-columns: 25% 25% 25% 25%;
        grid-row-gap: 20px;
        grid-column-gap: 20px;
        .loading-back{
          position: absolute;
          width: 1180px;
          height: 100%;
          top: 0;
          left: 0;
          box-sizing: border-box;
          background-color: rgba(255, 255, 255, 0.8);
          background-image: url('../../../public/jvs-ui-public/img/loading.gif');
          background-repeat: no-repeat;
          background-position: center;
          background-position: center;
          //background-size: 200px 160px;
          z-index: 1000;
        }
        .template-item{
          position: relative;
          //padding: 20px;
          border-radius: 10px;
          overflow: hidden;
          box-shadow: 0 0 8px #f0f0f0;
          cursor: pointer;
          box-sizing: border-box;
          display: flex;
          flex-direction: column;
          justify-content: space-between;
          &:hover{
            box-shadow: 0 0 8px #e0e0e0;
            .more-handle{
              color: #333333;
            }
          }
          i {
            font-size: 16px;
            //position: absolute;
            //bottom: 12px;
            //right: 20px;
          }
          .more-handle{
            color: #ffffff;
            position: absolute;
            right: 20px;
            bottom: 10px;
            font-size: 16px;
            cursor: pointer;
          }
          .item-content{
            padding: 20px;
            height: 60%;
            .item-header{
              color: #333333;
              font-size: 15px;
              display: flex;
              align-items: center;
              position: relative;
              img {
                margin-right: 10px;
                border-radius: 4px;
                width: 45px;
                height: 45px;
              }
              h5 {
                height: 28px;
                font-size: 15px;
                margin: 0;
              }
            }
            .item-description{
              height: 36px;
              font-size: 12px;
              margin: 10px 0;
              word-wrap: break-word;
              overflow: hidden;
              text-overflow: ellipsis;
              display: -webkit-box;
              -webkit-line-clamp: 2;
              -webkit-box-orient: vertical;
              color: #a2a3a5;
              line-height: 18px;
            }
            .item-tag{
              display: flex;
              margin-bottom: 16px;
            }
          }
          .template-type-status{
            position: absolute;
            left: 20px;
            bottom: 10px;
          }
        }
      }
      .temp-pagination{
        margin-top: 16px;
        text-align: right;
      }
      h3{
        display: flex;
        justify-content: space-between;
        align-items: center;
        span{
          font-size: 18px;
        }
      }
    }
  }
  .template-manage-box::-webkit-scrollbar{
    display: none;
  }
}
.application-manage-tool-list{
  padding: 0;
  margin: 0;
  li{
    height: 32px;
    line-height: 32px;
    cursor: pointer;
    //padding-left: 20px;
    padding: 6px 24px;
    text-align: left;
  }
  li:hover{
    background: #eff2f7;
  }
}
.select-image-show{
  position: relative;
  img{
    display: block;
    width: 120px;
    height: 120px;
  }
  .delete-select-image-tool{
    position: absolute;
    top: 3px;
    left: 123px;
    cursor: pointer;
    color: #F56C6C;
  }
}
.select-data-dialog.el-dialog__wrapper{
  /deep/.el-dialog{
    width: 680px;
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
        height: 18px;
        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
        font-weight: 400;
        font-size: 14px;
        color: #6F7588;
        line-height: 18px;
      }
      .top{
        height: calc(100% - 94px);
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
.hide-header.el-dialog__wrapper{
  /deep/.el-dialog{
    width: 540px;
    height: 240px;
    margin-top: calc(50vh - 120px)!important;
    .el-dialog__header{
      display: none;
    }
    .delete-confirm-box{
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
}
@media screen and (min-width:1921px) {
.template-view-page{
  width: 1921px!important;
  template-item-quick{
  padding: 0 60px!important;
}
.template-recommend-app-box{
  padding: 0 60px!important;
}
.template-manage-box{
  padding: 0 60px!important;
}
.template-item-quick{
 padding: 0 60px!important;
}
}
}
</style>
