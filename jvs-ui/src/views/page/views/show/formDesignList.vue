<template>
  <div :class="{'jvs-contail-font form-design-list': true, 'flowable-form-design-list-no-close': isFlowDesign, 'flowable-form-design-list': data.viewJson.formType == 'flowable'}">
    <!-- 无权限 -->
    <div v-if="showErrorTip" class="permission">
      <img src="@/const/img/permission.png" alt=""/>
      <span>暂无访问权限</span>
    </div>
    <div v-else>
      <design-header :infoData="data" :currentTab="currentTab" :tabList="tabList" type="form" @tabSelect="tabSelect" @handleSave="handleSave"/>
      <div class="design-form-cont flowable-ui-design-form-cont" v-if="formBool" v-show="currentTab === 'design'">
        <div v-if="data.viewJson" style="height:100%;">
          <normalForm
            ref="normalForm"
            v-if="['normalForm', 'flowable', 'detailForm'].indexOf(data.viewJson.formType) > -1"
            :formdata="data.viewJson.formdata[0]"
            :jvsAppId="data.jvsAppId"
            :designId="data.id"
            :dataModelId="data.dataModelId"
            :designName="data.name"
            :mobileCode="data.mobileCode"
            :isFlowable="data.viewJson.isFlowable"
            :flowableDom="data.viewJson.flowableDom"
            :fields="fieldsData"
            :columnNameList="fieldsData"
            :isDetail="isDetail"
            :isAddForm="isAddForm"
            :createOrigin="createOrigin"
            :tableOption="tableOption"
            :masterTable="(designData ? designData.id : data.id)+''"
            :isFlowDesign="isFlowDesign"
            :formType="data.viewJson.formType"
            :saveLoading="saveLoading"
            @save="saveForm"
            @clear="clearDesign"
            @connection="connectionHandle"
          />
          <detailForm
            v-if="false && data.viewJson.formType == 'detailForm'"
            :formdata="data.viewJson.formdata[0]"
            :jvsAppId="data.jvsAppId"
            :designId="data.id"
            :dataModelId="data.dataModelId"
            :formType="data.viewJson.formType"
            :fields="fieldsData"
            :columnNameList="fieldsData"
            :masterTable="(designData ? designData.id : data.id)+''"
            @save="saveForm"
          />
          <levelForm
            v-if="data.viewJson.formType == 'multiLevelForm'"
            :formdata="data.viewJson.formdata"
            :column="data.viewJson.column"
            :isFlowable="data.viewJson.isFlowable"
            :flowableDom="data.viewJson.flowableDom"
            :fields="fieldsData"
            :columnNameList="fieldsData"
            :masterTable="(designData ? designData.id : data.id)+''"
            @save="saveForm"
          />
          <stepForm v-if="data.viewJson.formType == 'stepForm'"
            :formdata="data.viewJson.formdata[0]"
            :column="data.viewJson.column"
            :fields="fieldsData"
            :columnNameList="fieldsData"
            :masterTable="(designData ? designData.id : data.id)+''"
            @save="saveForm" />
          <processForm v-if="false && data.viewJson.formType == 'flowable'"
            :formdata="data.viewJson.formdata"
            :column="data.viewJson.column"
            :fields="fieldsData"
            :columnNameList="fieldsData"
            :masterTable="(designData ? designData.id : data.id)+''"
            @save="saveForm" />
        </div>
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
            <jvs-form v-if="infoShow" :option="option" :formData="data"></jvs-form>
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
        <div class="permission-box">
          <permission
            :infoData="data"
            ref="permission"
            :operationList="operationList"
            :role="role"
            :roleType="roleType"
            :dataModelId="$route.query.dataModelId"
            :jvsAppId="$route.query.jvsAppId"
          />
        </div>
      </div>
      <div v-show="currentTab === 'dataSetting'" class="content-box">
        <div class="page-setting">
          <div v-if="!isDetail" class="setting-form">
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
                    </template>
                  </el-table-column>
                  <el-table-column label="是否启用前置">
                    <template slot-scope="scope">
                      <el-checkbox v-model="scope.row.beforeRuleEnable" :disabled="!scope.row.beforeRuleId"></el-checkbox>
                    </template>
                  </el-table-column>
                  <el-table-column label="后置">
                    <template slot-scope="scope">
                      <el-button size="mini" type="text" @click="ruleDesign(scope.$index, scope.row, 'after')">{{scope.row.afterRuleId ? '编辑' : '设计'}}</el-button>
                    </template>
                  </el-table-column>
                  <el-table-column label="是否启用后置">
                    <template slot-scope="scope">
                      <el-checkbox v-model="scope.row.afterRuleEnable" :disabled="!scope.row.afterRuleId"></el-checkbox>
                    </template>
                  </el-table-column>
                </el-table>
              </el-form-item>
            </el-form>
          </div>
          <div v-if="!isDetail" class="setting-form">
            <div class="title">
              <svg aria-hidden="true">
                <use xlink:href="#icon-jvs-rongqi"></use>
              </svg>
              <span>消息设置</span>
            </div>
            <div class="info-text">
              <p>消息通知可实现在表单提交、编辑、变更等节点或流程开始、结束等。</p>
            </div>
            <jvs-button type="primary" style="margin-bottom:10px;" @click="addMessage">新建消息通知</jvs-button>
            <el-table
              :data="tableData"
              style="width: 100%;">
              <el-table-column
                prop="name"
                label="模板名称">
                <template slot-scope="scope">
                  <div v-html="scope.row.name || scope.row.title"></div>
                </template>
              </el-table-column>
              <el-table-column
                prop="receiver"
                label="发送对象">
                <template slot-scope="scope">
                  <span v-for="(item,index) in scope.row.receiver" :key="index">{{index>0?'、':''}}{{item.name}}</span>
                </template>
              </el-table-column>
              <el-table-column
                prop="createTime"
                label="创建日期">
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template  slot-scope="scope">
                  <jvs-button type="text" @click="openEditMessage(scope)">编辑</jvs-button>
                  <jvs-button type="text" @click="openDelMessage(scope)">删除</jvs-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div class="setting-form" v-if="!isAddForm">
            <div class="title">
              <svg aria-hidden="true">
                <use xlink:href="#icon-jvs-rongqi"></use>
              </svg>
              <span>打印设置</span>
            </div>
            <div class="info-text">
              <p>创建打印模板，设计要打印的内容</p>
            </div>
            <printView v-if="currentTab === 'dataSetting'" :identity="data.id" :dataModelId="modelId"></printView>
          </div>
          <div class="setting-form tag-setting">
            <div class="title">
              <svg aria-hidden="true">
                <use xlink:href="#icon-jvs-rongqi"></use>
              </svg>
              <span>二维码标签设置</span>
            </div>
            <div class="info-text">
              <p>jvs的每条数据可生成二维码标签，例如固定资产标签、物料标签等</p>
              <p class="tag-switch">
                <el-switch v-model="tagSetting.openTag"></el-switch>
              </p>
            </div>
            <el-row v-show="tagSetting.openTag">
              <el-col class="left">
                <div style="width: 100%;height: 100%;background: #F5F6F7;padding: 24px;border-radius: 4px;box-sizing: border-box;">
                  <div style="width: 100%;height: 100%;background: #fff;display: flex;flex-direction: column;border-radius: 4px;overflow: hidden;position: relative;">
                    <div style="position: absolute;left: 32px;width: 28px;height: 40px;background: #1E6FFF;"></div>
                    <div>
                      <div style="height: 90px;padding-top: 48px;margin-left: 32px;border-bottom: 2px solid #EEEFF0;box-sizing: border-box;position: relative;">
                        <span style="display: inline-block;max-width: 50%;height: 29px;font-family: Source Han Sans-Bold, Source Han Sans;font-weight: 700;font-size: 20px;color: #363B4C;line-height: 29px;overflow: hidden;word-break: keep-all;">{{tagSetting.title.text || '主标题'}}</span>
                        <span style="display: inline-block;margin-left: 8px;max-width: calc(50% - 8px);height: 20px;font-family: Source Han Sans-Regular, Source Han Sans;font-weight: 400;font-size: 14px;color: #6F7588;line-height: 20px;overflow: hidden;">{{tagSetting.subTitle.text ? `/ ${tagSetting.subTitle.text}` : ''}}</span>
                        <div style="width: 192px;height: 2px;background: #1E6FFF;position: absolute;left: 0;bottom: -2px;"></div>
                      </div>
                    </div>
                    <div style="height: calc(100% - 90px);display:flex;justify-content:space-between;padding: 16px 32px;background:#fff;">
                      <div style="flex: 1;overflow: hidden;">
                        <div v-for="(fi, fix) in tagSetting.fieldList" :key="'tag-field-item-'+fix" style="width: 100%;overflow: hidden;display: flex;align-items: center;margin-bottom: 10px;">
                          <b style="max-width: calc(100% - 70px);font-family: Source Han Sans-Medium, Source Han Sans;font-weight: 500;font-size: 14px;color: #363B4C;line-height: 20px;overflow: hidden;word-break: keep-all;">{{fi.text}}</b>
                          <b v-if="fi.text" style="font-family: Source Han Sans-Medium, Source Han Sans;font-weight: 500;font-size: 14px;color: #363B4C;line-height: 20px;">：</b>
                          <span v-if="fi.text" style="font-family: Source Han Sans-Regular, Source Han Sans;font-weight: 400;font-size: 14px;color: #6F7588;line-height: 20px;word-break: keep-all;">字段内容</span>
                        </div>
                      </div>
                      <div style="text-align:right;margin-left: 10px;">
                        <span id="tagQRcode" ref="tagQRcode" style="display:block;width:120px;height:120px;"></span>
                        <!-- <span style="font-family: Source Han Sans-Regular, Source Han Sans;font-weight: 400;font-size: 10px;color: #6F7588;line-height: 14px;">created by jvs</span> -->
                      </div>
                    </div>
                  </div>
                </div>
              </el-col>
              <el-col class="right">
                <div class="right-top">
                  <h4>字段设置<span>字段内容超长时，将被自动截断！</span></h4>
                  <div class="right-top-item">
                    <span class="label">主标题</span>
                    <el-input v-model="tagSetting.title.text" size="mini" style="flex:1;margin-left:16px;"></el-input>
                    <el-button size="mini" type="text" style="margin-left: 8px;" @click="setFuction(tagSetting.title)">内容配置</el-button>
                  </div>
                  <div class="right-top-item">
                    <span class="label">副标题</span>
                    <el-input v-model="tagSetting.subTitle.text" size="mini" style="flex:1;margin-left:16px;"></el-input>
                    <el-button size="mini" type="text" style="margin-left: 8px;" @click="setFuction(tagSetting.subTitle)">内容配置</el-button>
                  </div>
                </div>
                <div class="right-field">
                  <p>
                    <span>设置标签需要显示的字段</span>
                    <b v-if="!(tagSetting.fieldList && tagSetting.fieldList.length > 3)" @click="addItemOfFieldList">
                      <svg aria-hidden="true">
                        <use xlink:href="#icon-jvs-xinjian-ffffff"></use>
                      </svg>
                    </b>
                  </p>
                  <div v-if="tagSetting.fieldList" class="right-field-list">
                    <div v-for="(row, rix) in tagSetting.fieldList" :key="rix">
                      <span>{{Number(rix)+1}}</span>
                      <el-input v-model="row.text" size="mini" style="flex:1;margin-left:10px;"></el-input>
                      <el-button size="mini" type="text" style="margin-left:10px;" @click="setFuction(row)">内容配置</el-button>
                      <span class="delete-icon-button" @click="deleteItemOfFieldList(tagSetting.fieldList, rix)" style="margin-left: 8px;">
                        <span class="border-line"></span>
                      </span>
                    </div>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
        </div>
      </div>
    </div>
    <!-- 其他设置 -->
    <el-dialog
      title="其他设置"
      :visible.sync="otherSettingVisible"
      width="30%"
      append-to-body
      :close-on-click-modal="false"
      :before-close="handleClientTypeClose">
      <el-form class="demo-form-inline" label-width="90px">
        <el-form-item label="客户端类型">
          <el-radio-group v-model="clientType" @change="handleClientChange">
            <el-radio :label="'pc'">PC</el-radio>
            <el-radio :label="'mobile'">Mobile</el-radio>
            <el-radio :label="'all'">双端</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否免登录">
          <el-radio-group v-model="checkLogin">
            <el-radio :label="true">是</el-radio>
            <el-radio :label="false">否</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="handleClientTypeClose">取 消</el-button>
        <el-button type="primary" @click="handleClientTypeSubmit">确 定</el-button>
      </span>
    </el-dialog>
    <!-- 发布到模板 -->
    <templateSet ref="templ" type="form" :id="data.id"></templateSet>
    <el-dialog
      title="表单选择"
      class="place-dialog"
      :visible.sync="placeVisible"
      :close-on-click-modal="false"
      :before-close="placeClose">
      <div class="place-form-desc">快速使用其他表单的设计</div>
      <div class="place-form-search">
        <el-input size="mini" v-model="queryParams.name" placeholder="请输入表单名称进行搜索" @input="handleQuery">
          <svg slot="prefix" aria-hidden="true">
            <use xlink:href="#icon-jvs-sousuo"></use>
          </svg>
        </el-input>
      </div>
      <div class="place-form-list">
        <div class="palce-form-item" v-for="item in placeList" :key="item.id+'-place-item'" @click="placeFormHandle(item)">
          <div class="name">
            <span>{{item.name}}</span>
          </div>
        </div>
      </div>
    </el-dialog>
    <!-- 检查字段类型变更 -->
    <el-dialog
      title="警告"
      :visible.sync="fieldTypeChangeVisible"
      :close-on-click-modal="false"
      :before-close="fieldTypeChangeClose">
      <div v-if="fieldTypeChangeVisible">
        <el-alert title="发现以下字段类型发生变更，变更后将影响列表和表单数据的回显，确定操作？" type="warning" :closable="false"></el-alert>
        <el-row style="margin-top: 10px;">
          <el-tag v-for="item in fieldTypeChangeList" :key="item.fieldKey+'-change-item'" style="margin-right: 10px;">{{item.fieldName}}</el-tag>
        </el-row>
        <el-row style="margin-top: 10px;display: flex;align-items:center;justify-content: center;">
          <el-button size="mini" type="primary" @click="fieldTypeChangeSubmit">确定</el-button>
          <el-button size="mini" @click="fieldTypeChangeClose">取消</el-button>
        </el-row>
      </div>
    </el-dialog>
    <!-- 新增消息  编辑消息 -->
    <el-dialog
      class="custom-header-dialog message-dialog"
      :title="messageDialogTitle"
      :visible.sync="messageVisible"
      :close-on-click-modal="false"
      :before-close="closeMessage">
      <div class="content">
        <message-push v-if="messageVisible" :modelId="$route.query.dataModelId" :workflowId="workflowId" :designId='data.id' @closeDialog="closeMessage" ref="messagePush"></message-push>
      </div>
      <div class="footer">
        <el-button size="mini" @click="closeMessage">取消</el-button>
        <el-button size="mini" type="primary" @click="messageSubmit">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import {getNameList, getDetailByName, addForm, updateForm, checkForm, deleteForm, deployForm, getFormCode} from '../../api/form'
import saveicon from "@/const/img/保存.png"
import {getDataStr, getDetail} from '../../api/formlist'
import Permission from "../../components/design/permission";

import PageHeader from '@/components/page-header/PageHeader'
import detailForm from '../../components/basic-design/detailForm'
import normalForm from '../../components/basic-design/normalForm'
import levelForm from '../../components/basic-design/levelForm'
import stepForm from '../../components/basic-design/stepForm'
import processForm from '../../components/basic-design/processForm'
import messagePush from '../../components/basic-design/messagePush.vue'

import {getModelSetting, updateModelSetting} from "@/api/newDesign";
import templateSet from '@/components/template/set'
import {createRule} from '../../api/design'
import DesignHeader from "@/components/page-header/DesignHeader";
import { getPageList } from '../../api/formlist'
import { getModelIdMessage,delModelMessage,getModelDetail } from "../../api/message"

import printView from '@/views/print/index'

import {guid} from "@/util/util";
import QRCode from 'qrcodejs2'

export default {
  components: {PageHeader, detailForm, normalForm, templateSet, levelForm, stepForm, processForm, Permission, DesignHeader, messagePush, printView},
  props: {
    hassave: {
      type: Boolean,
      default: false
    },
    hasclose: {
      type: Boolean,
      default: false
    },
    hasjson: {
      type: Boolean,
      default: false
    },
    hasimport: {
      type: Boolean,
      default: false
    },
    hasexport: {
      type: Boolean,
      default: false
    },
    designData: {
      type: Object
    }
  },
  data(){
    return {
      mobileCode: '', // 二维码
      queryParams: {
        name: ''
      },
      option: { // 对应表单设置
        labelWidth: '90px',
        submitBtn: false,
        emptyBtn: false,
        cancal: false,
        btnHide: true,
        formAlign: 'top',
        column: [
          {
            label: '表单名称',
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
        { name: 'design', label: '表单设计', icon: 'el-icon-document' },
        { name: 'permission', label: '页面权限', icon: 'el-icon-key' },
        { name: 'dataSetting', label: '数据设置', icon: 'el-icon-setting' },
      ],
      saveLoading: false, // 保存loading
      publishLoading: false, // 发布loading
      saveIcon: saveicon,
      role: [], // 权限设置
      roleType: true, // 权限类型,true 应用 权限，false 自定义权限
      operationList: [], // 操作权限list
      permissionVisible: false, // 权限弹框
      activeName: 'pageSetting', // tabs
      otherSettingVisible: false,
      callbackSettingVisible: false,
      clientType: 'pc',
      checkLogin: true,
      data: {
        name: "",
        type: "normalForm", // "flowable"
        id: '',
        viewJson: {
          formType: "normalForm", // "flowable"
          formdata: [
            {
              forms: [],
              formsetting: {
                labelposition: 'top',
                labelwidth: 80,
                formsize: 'mini',
                fullscreen: false,
                submitBtn: true,
                emptyBtn: true,
                cancal: false
              }
            },
            {}
          ]
        }
      },
      infoShow: false,
      formBool: false,
      tpString: '', // 设置类型  新增add   修改 edit
      setForm: {
        name: '',
        type: ''
      },
      setOption: {
        column: [
          {
            label: '表单名称',
            prop: 'name',
            rules: [
              { required: true, message: '请输入表单名称', trigger: 'blur' },
            ]
          },
          {
            label: '表单类型',
            prop: 'type',
            type: 'select',
            disabled: false,
            dicData: [
              {
                label: '普通表单',
                value: 'normalForm' // 'NORMALFORM'
              },
              {
                label: '详情表单',
                value: 'detailForm' // 'DETAILFORM'
              },
              {
                label: '多级表单',
                value: 'multiLevelForm' // 'MULTILEVELFORM'
              },
              {
                label: '流程表单',
                value: 'flowable' // 'FLOWABLE'
              },
            ],
            rules: [
              { required: true, message: '请选择表单类型', trigger: 'change' },
            ]
          }
        ]
      },
      formTypeDict: [], // 已有表单
      columns: [],
      actId: '', // 节点id
      modelId: '', // 模型id
      formFields: [], // 字段设置
      queryFormType: 'normalForm',
      fieldsData: [], // 默认的字段
      fileList: [],
      headers: {
        tenantId: this.$store.getters.userInfo.tenantId,
        Authorization: this.$store.getters.access_token
      },
      toolShow: false,
      isFlowDesign: false, // 是否来自工作流的设计
      isClient: false, // 是否来自应用跳转
      tableOption: [],
      isDetail: false, // 是否为列表页详情按钮
      isAddForm: false, // 是否为列表页新增按钮
      callbackSettingData: [],
      placeList: [], // 可选引用表单列表
      placeVisible: false,
      fieldTypeChangeVisible: false, // 字段类型变更
      fieldTypeChangeList: [],
      tagSetting: {
        openTag: false,
        title: {
          text: '',
          flag: 'titile'
        },
        subTitle: {
          text: '',
          flag: 'subtitle'
        }
      },
      messageVisible:false,
      messageType:"add",
      messageDataList:[],
      tableData:[],
      workflowId:'',
      messageDialogTitle: '',
      linkFormId: '',
      createOrigin: '',
      showErrorTip: false,
    }
  },
  async created () {
    await this.createHandle()
    window.onbeforeunload = (e) => {
      e.returnValue = '关闭提示'
    }
  },
  mounted () {
    this.openCloseHandle()
  },
  computed: {
  },
  methods: {
    async createHandle (type) {
      if(this.$route.query) {
        this.isDetail = this.$route.query.isDetail === 'true'
        this.isAddForm = this.$route.query.isAddForm === 'true'
        this.createOrigin = this.$route.query.origin || ''
        let onlyFormBool = true
        for(let k in this.$route.query) {
          if(['isDetail', 'isAddForm', 'isFlowNode'].indexOf(k) > -1) {
            onlyFormBool = false
          }
        }
        if(onlyFormBool) {
          this.isAddForm = true
        }
        if(this.$route.query.id) {
          this.isClient = true
          await this.getDesignFormInfo({id: this.$route.query.id}, type)
        }
        if(this.$route.query.dataModelId) {
          this.modelId = this.$route.query.dataModelId
          await this.getDesignDataStr(this.$route.query.jvsAppId, this.$route.query.dataModelId)
          await this.getModelSettingHandle(this.$route.query.jvsAppId, this.$route.query.dataModelId, this.$route.query.id)
        }
        if(this.$route.query.formType) {
          this.queryFormType = this.$route.query.formType
        }
        if(this.$route.query && this.$route.query.openByForm) {
          this.openByForm = this.$route.query.openByForm
        }
      }
      if(this.designData) {
        if(this.designData.appMenu){
          if(this.designData.appMenu.role) {
            this.role = JSON.parse(JSON.stringify(this.designData.appMenu.role))
          }
          if(this.designData.appMenu.roleType) {
            this.roleType = JSON.parse(JSON.stringify(this.designData.appMenu.roleType))
          }
        }
        this.getDesignFormInfo(this.designData, type)
      }
    },
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
    // 了解更多
    handleMore(str) {
      this.$openUrl('', '_blank', str)
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
        message: '复制成功',
        position: 'bottom-right',
        type: 'success'
      });
    },
    handleView(url) {
      this.$openUrl(url, '_blank')
    },
    // 获取预览地址
    getUrl() {
      return location.origin + `/page-design-ui/#/form/use?id=${this.$route.query.id}&dataModelId=${this.$route.query.dataModelId}&jvsAppId=${this.$route.query.jvsAppId}`
    },
    // tab选择结果
    tabSelect(val) {
      this.currentTab = val
      if (val === 'permission') {
        this.setPermission()
        this.$refs.permission.initData()
      }
    },
    async handleSave() {
      await this.callbackSettingSubmit()
      const permissionData = this.$refs.permission.getPermissionData()
      this.data.role = permissionData.role
      this.data.roleType = permissionData.roleType
      this.toolClick('save')
    },
    // 发布
    publishClick() {
      this.publishLoading = true
      const row = this.data
      if(row.id) {
        if(row.isDeploy) {
          this.$confirm('设计已经发布，此次保存可能会影响使用，是否继续操作？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            deployForm(this.$route.query.jvsAppId, row.id).then(res => {
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
            deployForm(this.$route.query.jvsAppId, row.id).then(res => {
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
        }
      }
    },
    // 设置列数
    setColumn(num) {
      this.$refs.normalForm.handleSetColumn(num)
    },
    // 清空
    clearAll() {
      this.$refs.normalForm.handleClear()
    },
    // 设置权限
    setPermission() {
      if (this.data && this.data.viewJson && this.data.viewJson.formdata) {
        let arr = []
        if (this.data.viewJson.formdata[0].formsetting.btnSetting) {
          arr = this.data.viewJson.formdata[0].formsetting.btnSetting.map(item => {
            return item.name
          })
          this.operationList = [...arr]
        }
      }
    },
    // 获取数据关联设置
    getModelSettingHandle (jvsAppId, modelId, designId) {
      getModelSetting(jvsAppId, modelId, designId).then(res => {
        if(res.data && res.data.code == 0) {
          let keys = Object.keys(this.$route.query)
          let list = []
          res.data.data.eventList.filter(eit => {
            if(eit.eventType == 'DATA_NEW' && (this.$route.query.isAddForm === 'true' || (keys.indexOf('isAddForm') == -1 && keys.indexOf('isDetail') == -1 && keys.indexOf('isFlowNode') == -1))) {
              list.push(eit)
            }
            if(eit.eventType == 'DATA_UPDATE' && this.$route.query.isAddForm !== 'true' && this.$route.query.isDetail !== 'true') {
              if(!(keys.indexOf('isAddForm') == -1 && keys.indexOf('isDetail') == -1) || keys.indexOf('isFlowNode') != -1) {
                list.push(eit)
              }
            }
          })
          let defaultEventList = []
          if(this.$route.query.isAddForm === 'true') {
            defaultEventList = [{
              eventType: 'DATA_NEW',
              afterRuleEnable: false,
              eventName: '新增数据',
              beforeRuleEnable: false,
            }]
          }
          if(this.$route.query.isAddForm !== 'true' && this.$route.query.isDetail !== 'true') {
            defaultEventList = [{
              eventType: 'DATA_UPDATE',
              afterRuleEnable: false,
              eventName: '修改数据',
              beforeRuleEnable: false,
            }]
          }
          if(keys.indexOf('isAddForm') == -1 && keys.indexOf('isDetail') == -1 && keys.indexOf('isFlowNode') == -1) {
            defaultEventList = [{
              eventType: 'DATA_NEW',
              afterRuleEnable: false,
              eventName: '新增数据',
              beforeRuleEnable: false,
            }]
          }
          this.callbackSettingData = list.length > 0 ? [...list] : defaultEventList
        }
      })
    },
    callbackSettingClose() {
      this.callbackSettingVisible = false
    },
    async callbackSettingSubmit (url) {
      const params = {
        eventList: [...this.callbackSettingData],
        relationDesignIds: this.getLationList()
      }
      await updateModelSetting(this.$route.query.jvsAppId, params, this.$route.query.dataModelId, this.$route.query.id).then(res => {
        if (res.data && res.data.code == 0) {
          this.callbackSettingVisible = false
          this.getModelSettingHandle(this.$route.query.jvsAppId, this.$route.query.dataModelId, this.$route.query.id)
          if(url) {
            this.$openUrl(url, '_blank')
          }
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
    handleClientTypeClose() {
      this.otherSettingVisible = false
    },
    handleClientTypeSubmit() {
      this.otherSettingVisible = false
    },
    handleClientChange() {
      this.$refs.normalForm.handleClear()
    },
    // 生成地址
    getpageUrl (row) {
      let str = location.origin
      str += (`/page-design-ui/#/form/info?jvsAppId=${this.$route.query.jvsAppId}&id=`+this.$route.query.id + (this.$route.query.dataModelId ? `&dataModelId=${this.$route.query.dataModelId}` : ''))
      return str
    },
    // 复制
    onCopy (e) {
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
    getFormName () {
      getNameList().then(res => {
        if(res.data.code == 0) {
          this.formTypeDict = res.data.data
        }
      })
    },
    formNameChange (val) {
      this.formBool = false
      this.data = {
        name: '',
        formType: '',
        id: '',
        viewJson: null
      }
      if(val) {
        getDetailByName({name: val}).then(res => {
          if(res.data.code == 0 && res.data.data) {
            this.data = res.data.data
            if(res.data.data.viewJson) {
              this.data.viewJson = JSON.parse(res.data.data.viewJson)
            }
            this.formBool = true
          }
        })
      }
    },
    // 其他设置
    otherSetting() {
      this.otherSettingVisible = true
    },
    // 数据关联设置
    callbackSetting() {
      this.callbackSettingVisible = true
    },
    saveForm (data, isRuleSave, clientType, checkLogin) {
      for(let i in data.formsetting.btnSetting) {
        if(!data.formsetting.btnSetting[i].permissionFlag) {
          data.formsetting.btnSetting[i].permissionFlag = this.data.id + '-' + data.formsetting.btnSetting[i].buttonType + '-' + guid()
        }
      }
      if(data.column) {
        this.data.viewJson.column = data.column
      }
      // 单个表单表单项数据设置同步
      if(data && data.forms) {
        this.data.viewJson.formdata[0] = data
      }
      // 多个表单
      if(data.formdata) {
        this.data.viewJson.formdata = data.formdata
      }
      let temp = {}
      for(let k in this.data) {
        temp[k] = this.data[k]
      }
      temp.viewJson.flowableFormDesign = null
      temp.viewJson = JSON.stringify(temp.viewJson)
      temp.supportedClientType = this.clientType
      temp.checkLogin = this.checkLogin
      temp.tagSetting = JSON.parse(JSON.stringify(this.tagSetting))
      this.checkFieldTypeChange(temp, isRuleSave)
    },
    // 保存
    saveHandle () {
      let temp = {}
      for(let k in this.data) {
        temp[k] = this.data[k]
      }
      temp.viewJson = JSON.stringify(this.data.viewJson)
      temp.relationDesignIds = this.getLationList()
      if(this.data.id) {
        updateForm(this.$route.query.jvsAppId, temp).then(res => {
          if(res.data.code == 0) {
            // this.$message.success("保存成功")
            this.$notify({
              title: '提示',
              message: '保存成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.getFormName()
            this.data = res.data.data
            if(res.data.data.viewJson) {
              this.data.viewJson = JSON.parse(res.data.data.viewJson)
            }
          }
        })
      }else{
        addForm(this.$route.query.jvsAppId, temp).then(res => {
          if(res.data.code == 0) {
            // this.$message.success("新增成功")
            this.$notify({
              title: '提示',
              message: '新增成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.data = res.data.data
            if(res.data.data.viewJson) {
              this.data.viewJson = JSON.parse(res.data.data.viewJson)
            }
            this.getFormName()
          }
        })
      }
    },
    // 发布
    deployHandle () {
      deployForm(this.$route.query.jvsAppId, this.data.id).then(res => {
        if(res.data.code == 0) {
          // this.$message.success("发布成功")
          this.$notify({
            title: '提示',
            message: '发布成功',
            position: 'bottom-right',
            type: 'success'
          });
          this.data = res.data.data
          if(res.data.data.viewJson) {
            this.data.viewJson = JSON.parse(res.data.data.viewJson)
          }
        }
      })
    },
    // 预览
    toolClick (type) {
      if (type === 'save') {
        this.saveLoading = true
      }
      this.$root.eventBus.$emit('toolEvent', type)
    },
    closeHandle () {
      if (this.isClient) {
        window.close()
      } else {
        this.$emit('close', true)
      }
    },
    uploadSuccess (res, file, fileList) {
      this.formBool = false
      if(res.code == 0) {
        // this.$message.success('导入成功')
        this.$notify({
          title: '提示',
          message: '导入成功',
          position: 'bottom-right',
          type: 'success'
        });
        this.fileList = []
        this.getDesignFormInfo(res.data)
      }else{
        this.$refs.uploadBtn.clearFiles()
        // this.$message.error(res.msg)
        this.$notify({
          title: '提示',
          message: res.msg,
          position: 'bottom-right',
          type: 'error'
        });
      }
    },
    // 导入失败
    errHandle (err, file, fileList) {
      this.$refs.uploadBtn.clearFiles()
      // this.$message.error(err)
      this.$notify({
        title: '提示',
        message: err,
        position: 'bottom-right',
        type: 'error'
      });
    },
    // 获取设计数据结构
    getDesignDataStr(jvsAppId, id) {
      getDataStr(jvsAppId, id).then(res => {
        if(res.data && res.data.code == 0 && res.data.data) {
          this.tableOption = [...res.data.data]
        }
      })
    },
    // 通过表单参数获取设计r数据
    getDesignFormInfo (row, type) {
      this.formBool = false
      let viewJson = {
        formType: "normalForm",
        formdata: [
          {
            forms: [],
            formsetting: {
              labelposition: 'top',
              labelwidth: 80,
              formsize: 'mini',
              fullscreen: false,
              submitBtn: true,
              emptyBtn: true
            }
          }
        ]
      }
      if (row.id) {
        getDetail(this.$route.query.jvsAppId, row.id).then(res => {
          if(res.data.code == 0) {
            this.modelId = res.data.data.dataModelId
            if(!res.data.data.icon) {
              res.data.data.icon = 'icon-shenfenrenzheng'
            }
            this.data = res.data.data
            if(res.data.data.tagSetting) {
              this.tagSetting = res.data.data.tagSetting
              if(!this.tagSetting.title.businessId){
                this.tagSetting.title.businessId = 'formtag'+this.data.id+'title'
              }
              if(!this.tagSetting.subTitle.businessId){
                this.tagSetting.subTitle.businessId = 'formtag'+this.data.id+'subTitle'
              }
              if(this.tagSetting.fieldList) {
                this.tagSetting.fieldList.filter(ti => {
                  if(!ti.businessId) {
                    ti.businessId = 'formtag'+this.data.id+ti.flag
                  }
                })
              }
            }
            this.mobileCode = res.data.data.mobileCode
            this.infoShow = true
            this.clientType = this.data.supportedClientType
            this.checkLogin = this.data.checkLogin
            if(this.data.appMenu) {
              if(this.data.appMenu.role) {
                this.role = JSON.parse(JSON.stringify(this.data.appMenu.role))
              }
              if(this.data.appMenu.roleType) {
                this.roleType = JSON.parse(JSON.stringify(this.data.appMenu.roleType))
              }
              this.$refs.permission.initData(this.roleType, this.role)
            }
            if(!this.data.viewJson || JSON.stringify(this.data.viewJson) == '{}') {
              this.data.viewJson = viewJson
            }else{
              this.data.viewJson = JSON.parse(this.data.viewJson)
            }
            if(this.data.viewJson.linkFormId) {
              this.linkFormId = this.data.viewJson.linkFormId
            }
            for(let f in this.data.viewJson.formdata) {
              for(let i in this.data.viewJson.formdata[f].forms) {
                if(this.data.viewJson.formdata[f].forms[i].type == 'SWITCH'){
                  this.data.viewJson.formdata[f].forms[i].type = 'switch'
                }
                if(this.isDetail) {
                  // 详情表单去除表格组件的新增、删除按钮
                  if(this.data.viewJson.formdata[f].forms[i].type == 'tableForm') {
                    this.data.viewJson.formdata[f].forms[i].addBtn = false
                    this.data.viewJson.formdata[f].forms[i].delBtn = false
                  }
                  if(['tab', 'step'].indexOf(this.data.viewJson.formdata[f].forms[i].type) > -1 && this.data.viewJson.formdata[f].forms[i].column) {
                    for(let ck in this.data.viewJson.formdata[f].forms[i].column) {
                      if(this.data.viewJson.formdata[f].forms[i].column[ck]) {
                        this.data.viewJson.formdata[f].forms[i].column[ck].filter(cit => {
                          if(cit.type == 'tableForm') {
                            cit.addBtn = false
                            cit.delBtn = false
                          }
                        })
                      }
                    }
                  }
                }
              }
              if(this.data.viewJson.formdata[f].formsetting && this.data.viewJson.formdata[f].formsetting.btnSetting) {
                let addPrint = true
                for(let i in this.data.viewJson.formdata[f].formsetting.btnSetting) {
                  if(!this.data.viewJson.formdata[f].formsetting.btnSetting[i].permissionFlag) {
                    this.data.viewJson.formdata[f].formsetting.btnSetting[i].permissionFlag = this.data.id + '-' + this.data.viewJson.formdata[f].formsetting.btnSetting[i].buttonType + '-' + guid()
                  }
                  if(this.data.viewJson.formdata[f].formsetting.btnSetting[i].buttonType == "submit" && !this.data.viewJson.formdata[f].formsetting.btnSetting[i].enable) {
                    this.data.viewJson.formdata[f].formsetting.submitBtn = false
                  }
                  if(this.data.viewJson.formdata[f].formsetting.btnSetting[i].buttonType == "empty" && !this.data.viewJson.formdata[f].formsetting.btnSetting[i].enable) {
                    this.data.viewJson.formdata[f].formsetting.emptyBtn = false
                  }
                  if(this.data.viewJson.formdata[f].formsetting.btnSetting[i].buttonType == "print") {
                    addPrint = false
                  }
                }
                if(addPrint) {
                  let list0 = this.data.viewJson.formdata[f].formsetting.btnSetting.slice(0, 2)
                  let list1 = this.data.viewJson.formdata[f].formsetting.btnSetting.slice(2, this.data.viewJson.formdata[f].formsetting.btnSetting.length)
                  let listTemp = [
                    ...list0,
                    {
                      name: '打印',
                      buttonType: 'print',
                      flag: true,
                      enable: true
                    },
                    ...list1
                  ]
                  this.$set(this.data.viewJson.formdata[f].formsetting, 'btnSetting', listTemp)
                }
              }
            }
            if(this.queryFormType == 'flowable') {
              this.$set(this.data.viewJson, 'formType', 'flowable')
            }
            this.formBool = true
            this.currentTab = (type == 'save' ? 'design' : 'pageSetting')
            this.setPermission()
            this.getMessgeList()
            this.getModelDetail()
            this.$forceUpdate()
          }else{
            this.showErrorTip = true
          }
        }).catch(e => {
          this.showErrorTip = true
        })
      }
    },
    // 获取消息列表
    getMessgeList(){
      getModelIdMessage(this.$route.query.jvsAppId, this.$route.query.dataModelId).then(res=>{
        if(res.data.code == 0) {
          this.tableData = res.data.data
        }
      })
    },
    // 获取模型详情
    getModelDetail(){
      getModelDetail(this.$route.query.jvsAppId, this.$route.query.dataModelId).then(res=>{
        if(res.data.code == 0) {
          this.workflowId = res.data.data.workflowId
        }
      })
    },
    // 发布到模板
    publishTempClick () {
      this.$refs.templ.init()
    },
    // 设计逻辑
    ruleDesign (index, row, pos) {
      let key = 'afterRuleId'
      let keyEnable = 'afterRuleEnable'
      if(pos == 'before') {
        key = 'beforeRuleId'
        keyEnable = 'beforeRuleEnable'
      }
      if(row[key]) {
        this.$openUrl(`/rule-design-ui/#/ruleDesign?id=${row[key]}&componentId=${this.$route.query.id}&jvsAppId=${this.data.jvsAppId}&name=${this.data.name}`, '_blank')
      }else{
        let obj = {
          jvsAppId: this.data.jvsAppId,
          name: (this.data.name+'-'+row.eventName),
          componentId: this.$route.query.id,
          designId: this.$route.query.id,
          componentType: 'form'
        }
        createRule(obj).then(res => {
          if(res.data && res.data.code == 0) {
            if(res.data.data) {
              this.$set(this.callbackSettingData[index], key, res.data.data)
              this.$set(this.callbackSettingData[index], keyEnable, true)
              this.callbackSettingSubmit(`/rule-design-ui/#/ruleDesign?id=${res.data.data}&componentId=${this.$route.query.id}&jvsAppId=${this.data.jvsAppId}&name=${this.data.name}`)
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
        this.$set(this.callbackSettingData[index], key, '')
      }).catch(e => { })
    },
    // 搜索
    handleQuery(e) {
      getPageList(this.data.jvsAppId, {current: 1, size: 100, jvsAppId: this.data.jvsAppId, name: e}).then(res => {
        if(res.data && res.data.code == 0 && res.data.data) {
          this.placeList = res.data.data.records
        }
      })
    },
    // 选用其他表单设计
    connectionHandle () {
      getPageList(this.data.jvsAppId, {current: 1, size: 100, jvsAppId: this.data.jvsAppId}).then(res => {
        if(res.data && res.data.code == 0 && res.data.data) {
          this.placeList = res.data.data.records
          this.placeVisible = true
        }
      })
    },
    placeClose () {
      this.placeVisible = false
      this.placeList = []
    },
    placeFormHandle (item) {
      this.formBool = false
      getDetail(this.$route.query.jvsAppId, item.id).then(res => {
        if(res.data && res.data.code == 0 && res.data.data) {
          this.linkFormId = res.data.data.id
          if(res.data.data.viewJson) {
            let placeData = JSON.parse(res.data.data.viewJson)
            for(let f in this.data.viewJson.formdata) {
              // 详情表单去除表格组件的新增、删除按钮
              if(this.isDetail) {
                for(let pf in placeData.formdata[f].forms) {
                  if(placeData.formdata[f].forms[pf].type == 'tableForm') {
                    placeData.formdata[f].forms[pf].addBtn = false
                    placeData.formdata[f].forms[pf].delBtn = false
                  }
                  if(['tab', 'step'].indexOf(placeData.formdata[f].forms[pf].type) > -1 && placeData.formdata[f].forms[pf].column){
                    for(let cpf in placeData.formdata[f].forms[pf].column) {
                      placeData.formdata[f].forms[pf].column[cpf].filter(pcit => {
                        if(pcit.type == 'tableForm') {
                          pcit.addBtn = false
                          pcit.delBtn = false
                        }
                      })
                    }
                  }
                }
              }
              this.$set(this.data.viewJson.formdata[f], 'forms', placeData.formdata[f].forms)
              this.$set(this.data.viewJson.formdata[f], 'formJson', placeData.formdata[f].formJson)
              this.$set(this.data.viewJson.formdata[f], 'autoTableFields', placeData.formdata[f].autoTableFields)
              this.$set(this.data.viewJson.formdata[f].formsetting, 'labelposition', placeData.formdata[f].formsetting.labelposition)
              this.$set(this.data.viewJson.formdata[f].formsetting, 'labelwidth', placeData.formdata[f].formsetting.labelwidth)
              this.$set(this.data.viewJson.formdata[f].formsetting, 'popupType', placeData.formdata[f].formsetting.popupType)
              this.$set(this.data.viewJson.formdata[f].formsetting, 'popupWidth', placeData.formdata[f].formsetting.popupWidth)
              this.$set(this.data.viewJson.formdata[f].formsetting, 'fullscreen', placeData.formdata[f].formsetting.fullscreen)
            }
            if(this.queryFormType == 'flowable') {
              this.$set(this.data.viewJson, 'formType', 'flowable')
            }
            this.placeClose()
            this.formBool = true
          }else{
            for(let f in this.data.viewJson.formdata) {
              this.$set(this.data.viewJson.formdata[f], 'forms', [])
              this.$set(this.data.viewJson.formdata[f], 'formJson', "")
              this.$set(this.data.viewJson.formdata[f], 'autoTableFields', [])
            }
            this.formBool = true
            this.placeClose()
          }
        }else{
          this.formBool = true
        }
      }).catch(e => {
        this.formBool = true
      })
    },
    // 保存设计前校验字段类型变更
    checkFieldTypeChange (temp, isRuleSave) {
      this.subData = temp
      this.fieldTypeChangeList = []
      checkForm(this.$route.query.jvsAppId, temp).then(res => {
        if(res.data && res.data.code == 0 && res.data.data && res.data.data.length > 0) {
          this.fieldTypeChangeList = res.data.data
          this.fieldTypeChangeVisible = true
        }else{
          this.saveSubmitHandle(temp, isRuleSave)
        }
      }).catch(e => {
        // this.saveSubmitHandle(temp, isRuleSave)
      })
    },
    fieldTypeChangeSubmit () {
      let temp = this.subData
      if(this.fieldTypeChangeList && this.fieldTypeChangeList.length > 0) {
        temp.deleteFieldsKey = this.fieldTypeChangeList
      }
      temp.relationDesignIds = this.getLationList()
      let viewJson = JSON.parse(temp.viewJson)
      viewJson.linkFormId = this.linkFormId
      temp.viewJson = JSON.stringify(viewJson)
      updateForm(this.$route.query.jvsAppId, temp).then(res => {
        if(res.data.code == 0) {
          this.saveLoading = false
          // this.$message.success("保存成功")
          this.$notify({
            title: '提示',
            message: '保存成功',
            position: 'bottom-right',
            type: 'success'
          });
          this.$emit('getList')
          if(this.$store.state.formCode) {
            this.$openUrl(`/page-design-ui/#/form?jvsAppId=${this.$route.query.jvsAppId}&id=${this.$store.state.formCode}&dataModelId=${this.$route.query.dataModelId}`)
            this.$store.state.formCode = ""
          }
          this.fieldTypeChangeClose()
        }
      })
    },
    saveSubmitHandle (temp, isRuleSave) {
      temp.relationDesignIds = this.getLationList()
      if (this.data.isDeploy && !isRuleSave) {
        this.$confirm('设计已发布，保存使用新的设计，此操作无法恢复，是否确认保存？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          let viewJson = JSON.parse(temp.viewJson)
          viewJson.linkFormId = this.linkFormId
          temp.viewJson = JSON.stringify(viewJson)
          updateForm(this.$route.query.jvsAppId, temp).then(res => {
            if(res.data.code == 0) {
              this.saveLoading = false
              // this.$message.success("保存成功")
              this.$notify({
                title: '提示',
                message: '保存成功',
                position: 'bottom-right',
                type: 'success'
              });
              this.$emit('getList')
              if(this.$store.state.formCode) {
                this.$openUrl(`/page-design-ui/#/form?jvsAppId=${this.$route.query.jvsAppId}&id=${this.$store.state.formCode}&dataModelId=${this.$route.query.dataModelId}`)
                this.$store.state.formCode = ""
              }
              this.fieldTypeChangeClose()
              if(this.linkFormId) {
                this.createHandle('save')
              }
            }
          })
        }).catch(e => {
          this.saveLoading = false
        })
      } else {
        let viewJson = JSON.parse(temp.viewJson)
        viewJson.linkFormId = this.linkFormId
        temp.viewJson = JSON.stringify(viewJson)
        updateForm(this.$route.query.jvsAppId, temp).then(res => {
          if(res.data.code == 0) {
            this.saveLoading = false
            // this.$message.success("保存成功")
            this.$notify({
              title: '提示',
              message: '保存成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.$emit('getList')
            if(this.$store.state.formCode) {
              this.$openUrl(`/page-design-ui/#/form?jvsAppId=${this.$route.query.jvsAppId}&id=${this.$store.state.formCode}&dataModelId=${this.$route.query.dataModelId}`)
              this.$store.state.formCode = ""
            }
            this.fieldTypeChangeClose()
            if(this.linkFormId) {
              this.createHandle('save')
            }
          }
        })
      }
    },
    fieldTypeChangeClose () {
      this.fieldTypeChangeList = []
      this.fieldTypeChangeVisible = false
    },
    addItemOfFieldList () {
      if(!this.tagSetting.fieldList) {
        this.$set(this.tagSetting, 'fieldList', [])
      }
      this.tagSetting.fieldList.push({flag: new Date().getTime(), businessId: ('formtag'+this.data.id+new Date().getTime())})
    },
    deleteItemOfFieldList (list, index) {
      list.splice(index, 1)
    },
    // 组件函数设置
    setFuction (row) {
      this.$setFormula({
        title: '公式设置',
        hasModal: true,
        label: row.text,
        execId: row.formula ? row.formula : '',
        apiPrefix: 'jvs-design',
        useCase: 'formQrCodeTag',
        props: {
          jvsAppId: this.data.jvsAppId,
          designId: this.data.id,
          businessId: row.businessId
        },
        afterSave: (dialog, data) => {
          if(data && data.id) {
            this.$set(row, 'formula', data.id)
            this.$set(row, 'formulaContent', data.body)
          }
          dialog.handleClose()
        }
      })
    },
    addMessage(){
      this.messageDialogTitle = '新增消息'
      this.messageVisible = true
      this.$nextTick(()=>{
        this.$refs.messagePush.init()
      })
    },
    closeMessage(val){
      if(val){
        this.getMessgeList()
      }
      this.messageVisible = false
    },
    messageSubmit(){
      this.$refs.messagePush.submitMessage()
    },
    openDelMessage(scope){
      delModelMessage(this.$route.query.jvsAppId, scope.row.id).then(res=>{
        if(res.data.code == 0){
          this.tableData.splice(scope.$index,1)
        }
      })
    },
    openEditMessage(scope){
      this.messageDialogTitle = '编辑消息'
      this.messageVisible = true
      this.$nextTick(()=>{
        this.$refs.messagePush.init(scope.row)
      })
    },
    openCloseHandle () {
      if(this.$refs.tagQRcode) {
        this.$refs.tagQRcode.innerHTML = ''; //清除二维码方法一
        let loc = location.origin // 'http://10.0.0.174:8099'
        let text = `${loc}/jvsCom/form/use?view=tagset`
        var qrcode = new QRCode(this.$refs.tagQRcode, {
          text: text, //页面地址 ,如果页面需要参数传递请注意哈希模式#
          width: 120,
          height: 120,
          colorDark: '#000000',
          colorLight: '#ffffff',
          correctLevel: QRCode.CorrectLevel.H,
        })
      }
    },
    getLationList () {
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
      if(this.data.viewJson && this.data.viewJson.formdata && this.data.viewJson.formdata.length > 0) {
        for(let i in this.data.viewJson.formdata) {
          // 表单设置
          if(this.data.viewJson.formdata[i].formsetting) {
            // 回显
            if(this.data.viewJson.formdata[i].formsetting.dataEchoRequest && list.indexOf(this.data.viewJson.formdata[i].formsetting.dataEchoRequest) == -1) {
              list.push(this.data.viewJson.formdata[i].formsetting.dataEchoRequest)
            }
            // 按钮
            if(this.data.viewJson.formdata[i].formsetting.btnSetting && this.data.viewJson.formdata[i].formsetting.btnSetting.length > 0) {
              this.data.viewJson.formdata[i].formsetting.btnSetting.filter(bit => {
                if(bit.secret && list.indexOf(bit.secret) == -1) {
                  list.push(bit.secret)
                }
              })
            }
          }
          // 表单组件
          if(this.data.viewJson.formdata[i].forms && this.data.viewJson.formdata[i].forms.length > 0) {
            this.data.viewJson.formdata[i].forms.filter(fit => {
              if(fit.formId && list.indexOf(fit.formId) == -1) {
                list.push(fit.formId)
              }
              if(fit.dataLinkageModelId && list.indexOf(fit.dataLinkageModelId) == -1) {
                list.push(fit.dataLinkageModelId)
              }
              if(fit.eventHttp && list.indexOf(fit.eventHttp) == -1) {
                list.push(fit.eventHttp)
              }
            })
          }
        }
      }
      return list
    },
    clearDesign () {
      this.linkFormId = ''
    }
  }
}
</script>
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
/deep/.place-dialog{
  .el-dialog{
    margin-top: calc(50vh - 240px)!important;
    width: 416px;
    height: 480px;
    background: #FFFFFF;
    border-radius: 6px!important;
    overflow: hidden;
    .el-dialog__header{
      height: 64px;
      background: #F5F6F7;
      padding: 0;
      padding-left: 24px;
      padding-right: 16px;
      box-sizing: border-box;
      &::before{
        display: none;
      }
      .el-dialog__title{
        height: 18px;
        font-family: Microsoft YaHei-Bold, Microsoft YaHei;
        font-weight: 700;
        font-size: 14px;
        color: #363B4C;
        line-height: 18px;
        position: absolute;
        top: 12px;
      }
      .el-dialog__headerbtn{
        .el-dialog__close{
          font-weight: bold;
          font-size: 20px;
          color: #575E73;
        }
      }
    }
    .el-dialog__body{
      height: calc(100% - 64px);
      padding: 16px 0 9px 8px!important;
      box-sizing: border-box;
      .place-form-desc{
        position: absolute;
        top: 36px;
        left: 24px;
        height: 16px;
        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
        font-weight: 400;
        font-size: 12px;
        color: #6F7588;
        line-height: 16px;
      }
      .place-form-search{
        padding: 0 24px;
        .el-input{
          .el-input__inner{
            height: 32px;
            background: #F5F6F7;
            border-radius: 4px;
            border: 0;
            padding-left: 52px;
            &::placeholder{
              font-family: Microsoft YaHei-Regular, Microsoft YaHei;
              font-weight: 400;
              font-size: 14px;
              color: #6F7588;
            }
          }
          .el-input__prefix{
            height: 16px;
            left: 12px;
            top: 8px;
            padding-right: 12px;
            border-right: 1px solid #D7D8DB;
            box-sizing: border-box;
            svg{
              width: 16px;
              height: 16px;
              cursor: pointer;
            }
          }
        }
      }
      .place-form-list{
        height: calc(100% - 32px);
        padding-top: 4px;
        padding-right: 8px;
        overflow: hidden;
        overflow-y: auto;
        box-sizing: border-box;
        .palce-form-item{
          height: 50px;
          display: flex;
          align-items: center;
          padding: 0 16px 0 24px;
          border-radius: 4px;
          cursor: pointer;
          .name{
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            border-top: 1px solid #EEEFF0;
          }
          &:hover{
            background: #F5F6F7;
            .name{
              border: 0;
            }
            &+.palce-form-item{
              .name{
                border: 0;
              }
            }
          }
        }
        .palce-form-item:nth-of-type(1) {
          .name{
            border-top: 0;
          }
        }
      }
    }
  }
}
/deep/.message-dialog{
  .el-dialog{
    width: 600px;
    height: 680px;
    margin-top: calc(50vh - 340px);
    .el-dialog__body{
      height: calc(100% - 48px);
      overflow: hidden;
      .content{
        height: calc(100% - 60px);
        overflow: hidden;
        overflow-y: auto;
        padding: 0 32px;
        padding-top: 16px;
        box-sizing: border-box;
      }
      .footer{
        height: 60px;
        display: flex;
        align-items: center;
        justify-content: flex-end;
        border-top: 1px solid #EEEFF0;
        box-sizing: border-box;
        .el-button{
          margin-right: 16px;
        }
      }
    }
  }
}
</style>
<style lang="scss">
.el-tabs__item{
  font-weight: bold;
}
.form-design-list{
  position: relative;
  height: 100%;
  box-sizing: border-box;
  overflow: hidden;
  background: #F5F6F7;
  -webkit-box-sizing: border-box;
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
  .design-title-bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    box-sizing: border-box;
    background-color: #fff;
    box-sizing: border-box;
    div{
      display: flex;
      align-items: center;
    }
    .left{
      .el-select, .el-button {
        margin-right: 20px;
      }
    }
    .right{
      .el-select, .el-button {
        margin-left: 20px;
      }
    }
  }
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
      .setting-form{
        &+.setting-form{
          margin-top: 32px;
        }
        .delete-icon-button{
          width: 16px;
          height: 16px!important;
          background: #36B452;
          border-radius: 4px;
          display: flex;
          align-items: center;
          justify-content: center;
          box-sizing: border-box;
          cursor: pointer;
          .border-line{
            width: 10px;
            height: 2px;
            background: #fff;
            border-radius: 2px;
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
      .tag-setting{
        padding-bottom: 20px;
        .el-form{
          .el-form-item{
            margin-bottom: 0;
          }
        }
        .el-row{
          display: flex;
          .el-col{
            margin: 0;
          }
          .left{
            width: 468px;
            height: 306px;
            background: #F5F6F7;
            border-radius: 4px;
          }
          .right{
            margin-left: 32px;
            flex: 1;
            overflow: hidden;
            .el-input{
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
            .el-button--text{
              width: 80px;
              height: 36px;
              background: #F5F6F7;
              border-radius: 4px;
            }
            .right-top{
              h4{
                margin: 0px;
                height: 20px;
                font-family: Source Han Sans-Bold, Source Han Sans;
                font-weight: 700;
                font-size: 14px;
                color: rgb(54, 59, 76);
                line-height: 20px;
                span{
                  margin-left: 16px;
                  font-family: Source Han Sans, Source Han Sans;
                  font-weight: 400;
                  font-size: 12px;
                  color: #FF194C;
                }
              }
              .right-top-item{
                display: flex;
                align-items: center;
                margin-top: 8px;
                height: 36px;
                &:nth-of-type(1){
                  margin-top: 17px;
                }
                .label{
                  font-family: Source Han Sans-Regular, Source Han Sans;
                  font-weight: 400;
                  font-size: 14px;
                  color: #363B4C;
                }
              }
            }
            .right-field{
              margin-top: 16px;
              border-top: 1px solid #EEEFF0;
              p{
                margin: 16px 0;
                height: 20px;
                display: flex;
                align-items: center;
                justify-content: space-between;
                span{
                  font-family: Source Han Sans-Bold, Source Han Sans;
                  font-weight: 700;
                  font-size: 14px;
                  color: #363B4C;
                  line-height: 20px;
                }
                b{
                  display: block;
                  width: 16px;
                  height: 16px;
                  background: #D7D8DB;
                  border-radius: 4px;
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  cursor: pointer;
                  svg{
                    width: 12px;
                    height: 12px;
                  }
                }
              }
              .right-field-list{
                >div{
                  display: flex;
                  align-items: center;
                  justify-content: space-between;
                  >span{
                    font-family: Source Han Sans-Regular, Source Han Sans;
                    font-weight: 400;
                    font-size: 14px;
                    color: #363B4C;
                  }
                }
                >div+div{
                  margin-top: 8px;
                }
              }
            }
          }
        }
      }
      .basic-form{
        .el-form{
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
        .el-input{
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
    .el-table{
      .el-table__body-wrapper{
        height: auto!important;
        .el-table__empty-block{
          display: none;
        }
      }
    }
  }
  .design-form-cont{
    position: absolute;
    width: 100%;
    height: calc(100% - 102px);
    .level-form-design{
      background-color: #fff;
    }
    .detailForm, .subform{
      .el-card{
        position: relative;
        .el-card__header{
          position: fixed;
          width: calc(100% - 572px);
          z-index: 999;
          background: #fff;
          box-sizing: border-box;
        }
        .el-card__body{
          margin-top: 59px;
        }
      }
    }
  }
  .flowable-ui-design-form-cont{
    height: calc(100% - 56px);
  }
}
.design-tool{
  // position: relative;
  z-index: 999;
  .form-design-tool{
    font-size: 20px;
    cursor: pointer;
    color: #353535;
    cursor: pointer;
    &:hover{
      color: #0D76FC;
    }
  }
  .icon {
    width: 20px;
    height: 20px;
    fill: currentColor;
    overflow: hidden;
    cursor: pointer;
  }
}
.flowable-form-design-list{
  .title-page-header{
    .title-page-header{
      width: calc(100% - 52px);
    }
    .pageheader-top{
      .right-box{
        border-right: 1px solid #dcdfe6;
      }
    }
  }
}
.flowable-form-design-list-no-close{
  .right-box{
    margin-right: 52px;
    border-right: 1px solid #dcdfe6;
  }
}
</style>
