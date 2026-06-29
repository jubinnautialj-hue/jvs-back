<template>
  <div class="jvs-contail-font process-design-box">
    <!-- 无权限 -->
    <div v-if="showErrorTip" class="permission">
      <img src="@/const/img/permission.png" alt=""/>
      <span>暂无访问权限</span>
    </div>
    <div v-else style="height: 100%;">
      <design-header v-if="infoData && infoData.id" :infoData="infoData" :currentTab="currentTab" :tabList="tabList" type="flow" :hasSave="true || (modeUserInfo ? modeUserInfo.mode !== 'GA' : true)" :saveLoading="saveLoading" @tabSelect="tabSelect" @handleSave="handleSave">
        <template slot="rightButton">
          <div style="margin-left: 15px;position: relative;">
            <jvs-button v-if="true || (modeUserInfo ? modeUserInfo.mode == 'DEV' : true)" size="mini" @click="pagesetHandle">数据管理</jvs-button>
            <jvs-button size="mini" @click="testClick">测试</jvs-button>
            <jvs-button size="mini" :loading="publishLoading" @click="publishClick">发布</jvs-button>
            <jvs-button size="mini" @click="stopClick">停用</jvs-button>
            <div v-if="infoData && infoData.designChanged && (modeUserInfo ? (true || ['DEV', 'BETA'].indexOf(modeUserInfo.mode) > -1) : true)" style="color: red;font-size:12px;position: absolute;right: 0;bottom: -13px;">
              <span><i class="el-icon-warning-outline"></i> 流程已变更请及时发布</span>
            </div>
          </div>
        </template>
      </design-header>
      <div v-if="false" v-show="currentTab === 'design'" class="scale">
        <jvs-button icon="el-icon-plus" size="mini" @click="scale += 10" :disabled="scale >= 150" circle></jvs-button>
        <span>{{ scale }}%</span>
        <jvs-button icon="el-icon-minus" size="mini" @click="scale -= 10" :disabled="scale <= 40" circle ></jvs-button>
      </div>
      <div v-show="currentTab === 'design'" class="flow-design-box">
        <div class="flow-design" :style="'transform: scale('+ scale / 100 +');'">
          <process-tree ref="process-tree" :disabled="false && !(modeUserInfo ? modeUserInfo.mode == 'DEV' : true)" :jvsAppId="$route.query.jvsAppId" @selectedNode="selectNode"/>
        </div>
      </div>
      <div v-if="currentTab === 'pageSetting'" class="content-box">
        <div class="page-setting">
          <div class="setting-form basic-form">
            <jvs-form :option="{...option, disabled: (false && !(modeUserInfo ? modeUserInfo.mode == 'DEV' : true))}" :formData="infoData">
              <template slot="iconForm">
                <div class="icon-image">
                  <img v-if="infoData.icon" :src="infoData.icon" alt="">
                  <el-button v-if="!infoData.icon" size="mini" @click="handleFocus">选择图标</el-button>
                  <i v-if="infoData.icon && (true || (modeUserInfo ? modeUserInfo.mode == 'DEV' : true))" class="el-icon-delete" @click="infoData.icon = ''"></i>
                </div>
              </template>
            </jvs-form>
          </div>
        </div>
      </div>
      <div v-if="currentTab === 'seniorSetting'" class="content-box">
        <div class="page-setting">
          <div class="setting-form basic-form">
            <div class="custom-box">
              <div class="custom-box-item row" style="width: 100%;">
                <div class="showtype-label">流程编号</div>
                <div class="content">
                  <el-select v-model="infoData.extend.codeFormat.custom" size="mini">
                    <el-option label="系统默认" :value="false"></el-option>
                    <el-option label="自定义" :value="true"></el-option>
                  </el-select>
                  <i v-if="infoData.extend.codeFormat.custom" class="el-icon-edit-outline" style="font-size: 20px;cursor: pointer;margin-left: 10px;" @click="codeFormatHandle"></i>
                </div>
              </div>
              <div class="custom-box-item" style="width: 100%;">
                <div class="label">
                  <span class="showtype-label">流程标题</span>
                  <el-popover
                    trigger="click"
                    v-model="titleVisible">
                    <div class="popover-list">
                      <template v-for="(item,index) in fieldKeyOptions">
                        <div class="popover-list-item"  :key="index" v-if="item.list && item.list.length > 0">
                          <div class="title">{{item.title}}</div>
                          <div class="field-list">
                            <div v-for="fit in item.list" :key="item.title + '-field-item-' + fit.id"  @click="popoverClick(fit,'titleEditor', 'titleVisible')">{{fit.name}}</div>
                          </div>
                        </div>
                      </template>
                    </div>
                    <jvs-button slot="reference" type='text'>插入字段</jvs-button>
                  </el-popover>
                </div>
                <div class="content">
                  <div id="flowTitle" style="width: 100%;"></div>
                </div>
              </div>
            </div>
            <jvs-form :option="seniorOption" :formData="infoData">
              <template slot="autoApprovalForm">
                <div>
                  <div>
                    <el-checkbox v-model="infoData.autoApproval.selfAuto"></el-checkbox>
                    <span style="margin-left: 10px;">所有发起人合并（所有节点中审批人为发起人时，自动审批）</span>
                  </div>
                  <div>
                    <el-checkbox v-model="infoData.autoApproval.adjacentNode"></el-checkbox>
                    <span style="margin-left: 10px;">相邻审批人合并（相邻节点的审批人相同时，自动审批）</span>
                  </div>
                </div>
              </template>
              <template slot="beforeStartFlowEventForm">
                <div style="color: #606266;">
                  <div style="display: flex;align-items: center;">
                    <span>启用事件</span>
                    <el-switch v-model="infoData.taskStartTriggerEvent.beforeStartFlowEvent.enableEvent" size="mini" style="margin-left: 10px;"></el-switch>
                    <i class="el-icon-edit-outline" style="font-size: 18px;margin-left: 10px;" @click="setRule('beforeStartFlowEvent')"></i>
                  </div>
                </div>
              </template>
              <template slot="terminatedEventForm">
                <div style="color: #606266;">
                  <div style="display: flex;align-items: center;">
                    <span>启用事件</span>
                    <el-switch v-model="infoData.taskEndTriggerEvent.terminatedEvent.enableEvent" size="mini" style="margin-left: 10px;"></el-switch>
                    <i class="el-icon-edit-outline" style="font-size: 18px;margin-left: 10px;" @click="setRule('terminatedEvent')"></i>
                  </div>
                </div>
              </template>
              <template slot="passedEventForm">
                <div style="color: #606266;">
                  <div style="display: flex;align-items: center;">
                    <span>启用事件</span>
                    <el-switch v-model="infoData.taskEndTriggerEvent.passedEvent.enableEvent" size="mini" style="margin-left: 10px;"></el-switch>
                    <i class="el-icon-edit-outline" style="font-size: 18px;margin-left: 10px;" @click="setRule('passedEvent')"></i>
                  </div>
                </div>
              </template>
              <template slot="rejectedEventForm">
                <div style="color: #606266;">
                  <div style="display: flex;align-items: center;">
                    <span>启用事件</span>
                    <el-switch v-model="infoData.taskEndTriggerEvent.rejectedEvent.enableEvent" size="mini" style="margin-left: 10px;"></el-switch>
                    <i class="el-icon-edit-outline" style="font-size: 18px;margin-left: 10px;" @click="setRule('rejectedEvent')"></i>
                  </div>
                </div>
              </template>
              <template slot="flowButtonForm">
                <div class="flow-button-box">
                  <div style="display: flex;align-items: center;">
                    <span>全局设置</span>
                    <el-switch v-model="infoData.flowButton.enable" size="mini" style="margin-left: 10px;"></el-switch>
                  </div>
                  <div class="form-item-tips">开启全局设置，所有审批节点的按钮未配置显示名称时，将将默认使用全局设置显示名称</div>
                  <div v-if="infoData.flowButton.enable" class="flow-button-list">
                    <div class="flow-button-list-item">
                      <span class="label">操作按钮</span>
                      <span class="box">显示名称</span>
                    </div>
                    <div v-for="btn in infoData.flowButton.buttons" :key="btn.operation" class="flow-button-list-item flow-button-list-item-btn">
                      <span class="label">{{btn.operation | getBtnName}}</span>
                      <div class="box">
                        <el-input v-model="btn.displayName"></el-input>
                      </div>
                    </div>
                  </div>
                </div>
              </template>
            </jvs-form> 
          </div>
        </div>
      </div>
    </div>
    <el-drawer
      class="flowable-node-info-drawer"
      :title="select.name"
      :visible.sync="drawer"
      size="472px"
      direction="rtl"
      :modal="false"
    >
      <div v-if="drawer" slot="title" class="title-slot">
        <svg :style="select.type == 'TJ' ? 'fill: #14C9C9;' : ''">
          <use :xlink:href="`#`+getIcon(select.type)"></use>
        </svg>
        <div class="name-input-box">
          <el-input
            v-if="showInput"
            v-model="select.name"
            :maxlength="20"
            @blur="showInput = false;"
          ></el-input>
          <span v-else>{{select.name}}</span>
        </div>
        <svg style="cursor: pointer;" @click="showInput = !showInput;">
          <use :xlink:href="`#${showInput ? 'icon-jvs-a-xiugaixuanzhong1x' : 'icon-jvs-a-xiugai1x'}`"></use>
        </svg>
      </div>
      <node-config v-if="drawer && (select.type == 'TJ' ? (!select.defaultCondition) : true)" :node="select" :infoData="infoData" :roleOption="roleOption" :disabled="false && !(modeUserInfo ? modeUserInfo.mode == 'DEV' : true)" :enableChange="true || (modeUserInfo ? (['DEV', 'BETA'].indexOf(modeUserInfo.mode) > -1) : true)" @saveNodeDesign="saveDesignData"></node-config>
    </el-drawer>
    <!-- 编辑 -->
    <el-dialog
      width="60%"
      :visible.sync="dialogVisible"
      append-to-body
      :modal="true"
      :before-close="handleClose"
      :fullscreen="dialogType == 'test' ? true : false"
      title="设置"
      :class="{'test-process-dialog': dialogType == 'test'}"
    >
      <div v-if="dialogVisible">
        <jvs-form v-if="dialogType == 'edit'" style="padding: 0 80px;" :option="formOption" :formData="formData" @submit="modelInfoSubmit">
          <template slot="iconForm">
            <div class="icon-image">
              <img v-if="formData.icon" :src="formData.icon" alt="">
              <el-button v-if="!formData.icon" size="mini" @click="handleFocus">选择图标</el-button>
              <i v-if="formData.icon" class="el-icon-delete" @click="formData.icon = ''"></i>
            </div>
          </template>
        </jvs-form>
        <testFlowable v-if="dialogType == 'test'" :testData="testData" :infoData="infoData" @close="closeHandle"></testFlowable>
      </div>
    </el-dialog>
    <!-- 列表页挂载到菜单 -->
    <el-dialog
      title="数据管理"
      width="30%"
      :visible.sync="pageVisible"
      :before-close="pageClose">
      <div v-if="pageVisible">
        <el-form :model="infoData">
          <el-form-item label="选择目录">
            <el-select v-model="infoData.menuId" size="mini" placeholder="请选择目录" clearable>
              <el-option v-for="(item, index) in catalogList" :key="'catalog-item-'+index" :label="item.label" :value="item.value"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="列表设计">
            <i class="el-icon-edit-outline" style="font-size: 18px;line-height: 36px;cursor: pointer;" @click="openPageDesign"></i>
          </el-form-item>
        </el-form>
        <el-row style="display: flex;align-items: center;justify-content: center;">
          <el-button size="mini" type="primary" @click="pageMenuSubmit">提交</el-button>
          <el-button size="mini" @click="pageClose">取消</el-button>
        </el-row>
      </div>
    </el-dialog>
    <!-- 选图片 -->
    <ImageSelect
      :dialogVisible="iconVisible"
      :title="'选择图标'"
      :defaultLabel="defaultLabel"
      @handleClose="iconClose"
      @handleConfirm="handleConfirm"
    />

    <!-- 逻辑设计  可以选择已有逻辑引擎 -->
    <el-dialog
      title="业务逻辑"
      width="30%"
      append-to-body
      :visible.sync="netSetVisible"
      :close-on-click-modal="false"
      :before-close="netSetClose">
      <div v-if="netSetVisible">
        <div style="display: flex;align-items: center;">
          <el-button size="mini" type="primary" @click="newRuleSet" :loading="newRuleSetLoading">新建业务逻辑</el-button>
        </div>
        <div v-if="currentType == 'beforeStartFlowEvent'" style="display: flex;align-items: center;margin-top: 15px;">
          <span style="margin-right: 10px;">选择逻辑</span>
          <el-select v-model="infoData.taskStartTriggerEvent[currentType].ruleKey" size="mini" filterable clearable>
            <el-option v-for="rit in allRuleList" :key="'all-rule-item-'+rit.secret" :label="rit.name" :value="rit.secret"></el-option>
          </el-select>
          <i v-if="infoData.taskStartTriggerEvent[currentType].ruleKey" class="el-icon-edit-outline form-icon-btn" style="margin-left: 10px;cursor: pointer;" @click="viewRule"></i>
        </div>
        <div v-else style="display: flex;align-items: center;margin-top: 15px;">
          <span style="margin-right: 10px;">选择逻辑</span>
          <el-select v-model="infoData.taskEndTriggerEvent[currentType].ruleKey" size="mini" filterable clearable>
            <el-option v-for="rit in allRuleList" :key="'all-rule-item-'+rit.secret" :label="rit.name" :value="rit.secret"></el-option>
          </el-select>
          <i v-if="infoData.taskEndTriggerEvent[currentType].ruleKey" class="el-icon-edit-outline form-icon-btn" style="margin-left: 10px;cursor: pointer;" @click="viewRule"></i>
        </div>
      </div>
    </el-dialog>
    <!-- 编号格式 -->
    <el-dialog
      class="style-dialog"
      title="编号格式"
      append-to-body
      :visible.sync="codeFormatVisible"
      :close-on-click-modal="false"
      :before-close="codeFormatClose">
      <div v-if="codeFormatVisible" class="custom-box style-dialog-box">
        <div class="custom-box-item">
          <div class="label">格式类型</div>
          <div class="content">
            <el-select v-model="codeFormat.formatType" size="mini" style="width:100%;">
              <el-option label="自动计数" value="AUTO"></el-option>
            </el-select>
          </div>
        </div>
        <div v-if="codeFormat.formatType == 'AUTO'">
          <div class="custom-box-item">
            <div class="label">前缀</div>
            <div class="content">
              <el-input v-model="codeFormat.autoFormat.orderPrefix" size="mini" maxlength="10" show-word-limit></el-input>
            </div>
          </div>
          <div class="custom-box-item">
            <div class="label">后缀</div>
            <div class="content">
              <el-input v-model="codeFormat.autoFormat.orderSuffix" size="mini" maxlength="10" show-word-limit></el-input>
            </div>
          </div>
          <div class="custom-box-item">
            <div class="label">时间标识</div>
            <div class="content">
              <el-select v-model="codeFormat.autoFormat.orderTimeMark" filterable size="mini" style="width:100%;">
                <el-option label="不设置" value="n"></el-option>
                <el-option label="年" value="y"></el-option>
                <el-option label="年月" value="ym"></el-option>
                <el-option label="年月日" value="ymd"></el-option>
                <el-option label="年月日时" value="ymdh"></el-option>
                <el-option label="年月日时分" value="ymdhm"></el-option>
                <el-option label="年月日时分秒" value="ymdhms"></el-option>
              </el-select>
            </div>
          </div>
          <div class="custom-box-item">
            <div class="label">序号位数</div>
            <div class="content">
              <el-input-number :min="1" :max="9" v-model="codeFormat.autoFormat.orderDigit" size="mini" style="width:100%;"></el-input-number>
            </div>
          </div>
          <div class="custom-box-item">
            <div class="label">重置规则</div>
            <div class="content">
              <el-select v-model="codeFormat.autoFormat.orderResetRule" filterable size="mini" style="width:100%;">
                <el-option label="不重置" value="n"></el-option>
                <el-option label="按年重置" value="y"></el-option>
                <el-option label="按月重置" value="m"></el-option>
                <el-option label="按天重置" value="d"></el-option>
                <el-option label="按小时重置" value="h"></el-option>
              </el-select>
            </div>
          </div>
        </div>
      </div>
      <div v-if="codeFormatVisible" class="footer">
        <div class="footer-button">
          <div class="ftb" @click="codeFormatClose">取消</div>
          <div class="ftb submit" @click="codeFormatSubmit">确定</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import ProcessTree from './process/ProcessTree.vue'
import nodeConfig from "./process/nodeConfig";
import { nodeType, getDefaultNodeProps, nodeButtonList } from "./common/enumConst";
import saveicon from "@/const/img/保存.png"
import {editModel, designModel, getModelDetail, createWorkflowCurd, saveDeployProcess, flowableType, testPrepare, suspendProcess} from '../../api/flowable'
import {getRoleList} from '@/components/api'
import ImageSelect from "@/components/basic-assembly/ImageSelect";
import DesignHeader from "@/components/page-header/DesignHeader";
import testFlowable from './test'
import { getApplicationMenu, editDesign, getAllRule } from '@/components/template/api'
import {getRuleIdByComponentId, getFlowNameFieldList} from "@/views/flowable/api/flowable";
import {getStore} from "@/util/store"
import E from "wangeditor"
export default {
  name: "processDesign",
  components: { ProcessTree, nodeConfig, ImageSelect, DesignHeader, testFlowable },
  props: {
    rowData: {
      type: Object
    }
  },
  computed: {
    template() {
      return this.$store.state.flow.template
    }
  },
  filters: {
    getBtnName (type) {
      let str = ''
      nodeButtonList.filter(btn => {
        if(btn.operation == type) {
          str = btn.name
        }
      })
      return str
    }
  },
  data() {
    return {
      nodeId: undefined,
      option: { // 对应表单设置
        labelWidth: '90px',
        submitBtn: false,
        emptyBtn: false,
        cancal: false,
        btnHide: true,
        formAlign: 'top',
        column: [
          {
            label: '流程名称',
            prop: 'name',
            search: true,
            searchSpan: 4,
            showwordlimit: true,
            maxlength: 30,
            rules: [
              { required: true, message: '请输入名称', trigger: 'blur' }
            ],
          },
          // {
          //   label: '流程分组',
          //   prop: 'designGroup',
          //   type: 'select',
          //   dicData: [],
          //   dicUrl: `/mgr/jvs-design/base/workflow/design/groups`,
          //   filterable: true,
          //   allowcreate: true,
          //   defaultValue: '未分类',
          //   tips:{
          //     position: 'bottom',
          //     text: '可以输入类型，或选择已经存在的分组'
          //   }
          // },
          {
            label: '流程描述',
            prop: 'description',
            type: 'textarea',
            maxlength: 255,
            rows: 5,
            showwordlimit: true
          },
          {
            label: '流程图标',
            prop: 'icon',
            type: 'iconSelect',
            iconType: 'svg',
            formSlot: true
          }
        ],
      },
      currentTab: '',
      tabList: [
        { name: 'pageSetting', label: '页面设置', icon: 'el-icon-collection-tag' },
        { name: 'design', label: '流程设计', icon: 'el-icon-document' },
        { name: 'seniorSetting', label: '高级设置', icon: 'el-icon-setting' }
      ],
      hasclose: false,
      nodeType: nodeType,
      select: {},
      showInput: false,
      drawer: false,
      scale: 100,
      nodes: [],
      infoData: {},
      saveLoading: false, // 保存loading
      saveIcon: saveicon,
      dialogVisible: false,
      dialogType: '',
      formData: {},
      formOption: {
        cancal: false,
        labelWidth: '80px',
        submitLoading: false,
        column: [
          {
            label: '流程名称',
            prop: 'name',
            search: true,
            showwordlimit: true,
            maxlength: 12,
            rules: [
              { required: true, message: '请输入流程名称', trigger: 'blur' },
            ],
            tips:{
              position: 'bottom',
              text: '流程名称建议使用12字内，请使用中文,默认情况下，业务对接工作流时使用'
            }
          },
          // {
          //   label: '流程分组',
          //   prop: 'designGroup',
          //   type: 'select',
          //   dicData: [],
          //   filterable: true,
          //   allowcreate: true,
          //   defaultValue: '未分类',
          //   showwordlimit: true,
          //   maxlength: 12,
          //   tips:{
          //     position: 'bottom',
          //     text: '可以输入类型，或选择已经存在的分组'
          //   }
          // },
          {
            label: '流程图标',
            prop: 'icon',
            type: 'iconSelect',
            iconType: 'svg',
            formSlot: true
          },
          {
            label: '终止模式',
            prop: 'enableCancel',
            type: 'radio',
            defaultValue: false,
            dicData: [
              {label: '允许申请人强制终止流程', value: true},
              {label: '禁止申请人强制终止流程', value: false}
            ],
            tips:{
              position: 'bottom',
              text: '申请人可以在流程流转到任何节点时进行强制终止，终止后将不能继续流转'
            }
          },
          {
            label: '重启任务',
            prop: 'enableRestart',
            type: 'radio',
            defaultValue: false,
            dicData: [
              {label: '允许申请人重启流程', value: true},
              {label: '禁止申请人重启流程', value: false}
            ],
          },
        ]
      },
      roleOption: [], // 角色列表
      publishLoading: false, // 发布loading
      defaultLabel: '',
      iconVisible: false,
      testData: null,
      seniorOption: {
        btnHide: true,
        formAlign: 'top',
        column: [
          {
            label: '撤回设置',
            prop: 'enableCancel',
            type: 'radio',
            defaultValue: false,
            dicData: [
              {label: '允许申请人撤回审批中流程', value: true},
              {label: '禁止申请人撤回审批中流程', value: false}
            ],
            tips:{
              position: 'bottom',
              text: '申请人可以在流程流转到任何节点时进行撤回，撤回后将不能继续流转，需要发起人重新提交'
            }
          },
          {
            label: '重启任务',
            prop: 'enableRestart',
            type: 'radio',
            defaultValue: false,
            dicData: [
              {label: '允许申请人重启流程', value: true},
              {label: '禁止申请人重启流程', value: false}
            ],
          },
          {
            label: '自动审批规则',
            prop: 'autoApproval',
            formSlot: true,
            tips:{
              position: 'bottom',
              text: '若在节点中修改自动审批设置，全局设置将会失效'
            }
          },
          {
            label: '动态选择审批人',
            prop: 'enableDynamicApprover',
            type: 'radio',
            defaultValue: false,
            dicData: [
              {label: '允许', value: true},
              {label: '禁止', value: false}
            ],
            span: 8,
          },
          {
            label: '动态添加节点',
            prop: 'enableDynamicNode',
            type: 'radio',
            defaultValue: false,
            dicData: [
              {label: '允许', value: true},
              {label: '禁止', value: false}
            ],
            span: 12,
          },
          {
            label: '启动流程前触发事件',
            prop: 'beforeStartFlowEvent',
            formSlot: true,
            span: 24,
          },
          {
            label: '撤回触发事件',
            prop: 'terminatedEvent',
            formSlot: true,
            span: 8,
          },
          {
            label: '审批通过触发事件',
            prop: 'passedEvent',
            formSlot: true,
            span: 8,
          },
          {
            label: '审批不通过触发事件',
            prop: 'rejectedEvent',
            formSlot: true,
            span: 8,
          },
          {
            label: '按钮设置',
            prop: 'flowButton',
            formSlot: true,
          }
        ]
      },
      pageVisible: false,
      catalogList: [],
      netSetVisible: false,
      currentType: '',
      newRuleSetLoading: false,
      allRuleList: [], // 当前模型下的所有的逻辑引擎列表
      currentRuleName: '',
      modeUserInfo: null,
      codeFormatVisible: false,
      codeFormat: null,
      titleVisible: false,
      titleEditor: null, // 流程标题富文本
      fieldKeyOptions: [],
      showErrorTip: false,
    };
  },
  async created () {
    if(this.$route.query) {
      if(this.$route.query.id) {
        await this.getDetail(this.$route.query.id)
      }
    }else{
      if(this.rowData) {
        this.hasclose = true
        this.infoData = JSON.parse(JSON.stringify(this.rowData))
      }
    }
    this.getRoleHandle() // 获取角色列表
    this.getAllRuleListHandle()
    this.getFlowNameFieldListHandle()
    this.modeUserInfo = getStore({ name: 'modeUserInfo' })
    let windowChannel = new BroadcastChannel('tagNotice')
    windowChannel.addEventListener('message', event => {
      if(event.data == 'changeUserMode') {
        this.modeUserInfo = getStore({ name: 'modeUserInfo' })
      }
    })
    window.onbeforeunload = (e) => {
      e.returnValue = '关闭提示'
    }
  },
  methods: {
    // 保存
    handleSave() {
      this.saveClick()
    },
    // tab选择结果
    tabSelect(val) {
      this.currentTab = val
      this.$nextTick(() => {
        if(this.currentTab == 'seniorSetting') {
          this.createEditor('titleEditor', 'flowTitle')
          if(this.infoData.extend.taskTitleFormat) {
            this.titleEditor.txt.html(this.infoData.extend.taskTitleFormat)
          }
        }
      })
    },
    // 获取工作流详情
    async getDetail (id, isSave) {
      await getModelDetail(this.$route.query.jvsAppId, id).then(res => {
        if(res.data.code == 0) {
          if(isSave) {
            this.$set(this.infoData, 'designChanged', res.data.data.designChanged)
          }else{
            this.infoData = JSON.parse(JSON.stringify(res.data.data))
            res.data.data.extend = res.data.data.extend || {}
            if (!isSave) {
              this.currentTab = 'pageSetting'
            }
            this.$set(this.infoData, 'enableCancel', res.data.data.extend.enableCancel)
            this.$set(this.infoData, 'enableRestart', res.data.data.extend.enableRestart)
            this.$set(this.infoData, 'autoApproval', res.data.data.extend.autoApproval || {selfAuto: false, adjacentNode: false})
            this.$set(this.infoData, 'enableDynamicApprover', res.data.data.extend.enableDynamicApprover)
            this.$set(this.infoData, 'enableDynamicNode', res.data.data.extend.enableDynamicNode)
            if(res.data.data.extend.taskStartTriggerEvent) {
              this.$set(this.infoData, 'taskStartTriggerEvent', res.data.data.extend.taskStartTriggerEvent)
            }
            if(res.data.data.extend.taskEndTriggerEvent) {
              this.$set(this.infoData, 'taskEndTriggerEvent', res.data.data.extend.taskEndTriggerEvent)
            }
            if(res.data.data.extend.flowButton) {
              this.$set(this.infoData, 'flowButton', res.data.data.extend.flowButton)
            }
            if(res.data.data && res.data.data.designBody && res.data.data.designBody != '') {
              let data = JSON.parse(res.data.data.designBody)
              this.$store.commit("setIsEdit", true);
              this.$store.commit("setTemplate", this.getTemplateData(data, res.data.data.designGroup));
            }else{
              this.$store.commit("setIsEdit", true);
              this.$store.commit("setTemplate", this.getTemplateData());
            }
            if(!this.infoData.taskStartTriggerEvent) {
              this.$set(this.infoData, 'taskStartTriggerEvent', {
                beforeStartFlowEvent: {enableEvent: false, ruleKey: ''}
              })
            }
            if(!this.infoData.taskEndTriggerEvent) {
              this.$set(this.infoData, 'taskEndTriggerEvent', {
                terminatedEvent: {enableEvent: false, ruleKey: ''},
                passedEvent: {enableEvent: false, ruleKey: ''},
                rejectedEvent: {enableEvent: false, ruleKey: ''}
              })
            }
            if(!this.infoData.flowButton) {
              let btn = []
              nodeButtonList.filter(nbt => {
                btn.push({
                  operation: nbt.operation,
                  displayName: ''
                })
              })
              this.$set(this.infoData, 'flowButton', {
                enable: false,
                buttons: btn
              })
            }
            if(!this.infoData.extend) {
              this.$set(this.infoData, 'extend', {})
            }
            if(!this.infoData.extend.codeFormat) {
              this.$set(this.infoData.extend, 'codeFormat', {
                custom: false,
                formatType: 'AUTO',
                autoFormat: {
                  orderDigit: 5,
                  orderTimeMark: 'n',
                  orderResetRule: 'n'
                }
              })
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
    // 选择节点
    selectNode(node) {
      this.select = node;
      this.drawer = true;
    },
    // 保存设计
    saveClick () {
      this.saveLoading = true
      this.saveDesignData()
    },
    // 保存
    saveDesignData (nodeId, getbool) {
      let form = JSON.parse(JSON.stringify(this.infoData))
      form.extend = JSON.parse(JSON.stringify(this.infoData)).extend || {}
      form.extend.enableCancel = this.infoData.enableCancel
      form.extend.enableRestart = this.infoData.enableRestart
      form.extend.autoApproval = this.infoData.autoApproval
      form.extend.enableDynamicApprover = this.infoData.enableDynamicApprover
      form.extend.enableDynamicNode = this.infoData.enableDynamicNode
      form.extend.taskStartTriggerEvent = {
        beforeStartFlowEvent: this.infoData.taskStartTriggerEvent.beforeStartFlowEvent
      }
      form.extend.taskEndTriggerEvent = {
        terminatedEvent: this.infoData.taskEndTriggerEvent.terminatedEvent,
        passedEvent: this.infoData.taskEndTriggerEvent.passedEvent,
        rejectedEvent: this.infoData.taskEndTriggerEvent.rejectedEvent
      }
      form.extend.flowButton = {
        enable: this.infoData.flowButton.enable,
        buttons: this.infoData.flowButton.buttons
      }
      delete form.enableCancel
      delete form.enableRestart
      delete form.autoApproval
      delete form.enableDynamicApprover
      delete form.enableDynamicNode
      delete form.taskStartTriggerEvent
      delete form.taskEndTriggerEvent
      delete form.flowButton
      let temp = {
        id: this.infoData.id,
        designBody: this.template.process,
        jvsAppId: form.jvsAppId,
        name: form.name,
        designGroup: form.designGroup,
        icon: form.icon,
        extend: form.extend,
        description: form.description,
        nodeId: nodeId
      }
      if(nodeId) {
        this.nodeId = nodeId
      }
      designModel(temp).then(res => {
        if(res.data && res.data.code == 0) {
          this.saveLoading = false
          if(nodeId) {
            if(res.data.data && typeof res.data.data == 'string') {
              this.$set(this.select.nodeForm, 'formId', res.data.data)
              let formType = 'normalForm'
              if(this.select.type != 'ROOT') {
                formType = 'flowable'
              }
              this.$openUrl(`/page-design-ui/#/form?id=${res.data.data}&dataModelId=${this.infoData.dataModelId}&formType=${formType}&jvsAppId=${this.$route.query.jvsAppId}&isFlowNode=true`, '_blank')
            }
          }else{
            this.$notify({
              title: '提示',
              message: '保存成功',
              position: 'bottom-right',
              type: 'success'
            });
          }
          if(getbool != 'notdetail') {
            this.getDetail(this.infoData.id, true)
          }
        } else {
          this.saveLoading = false
        }
      }).catch(err => {
        this.saveLoading = false
      })
    },
    // 编辑工作流
    editProcess () {
      if(this.infoData) {
        if(this.infoData.extend) {
          this.infoData.enableCancel = this.infoData.extend.enableCancel
          this.infoData.enableRestart = this.infoData.extend.enableRestart
        }
        this.formData = JSON.parse(JSON.stringify(this.infoData))
      }
      this.dialogVisible = true
      this.dialogType = 'edit'
    },
    handleClose () {
      this.dialogVisible = false
      this.dialogType = ''
      this.formData = {}
      this.testData = null
    },
    // 模型数据 修改
    modelInfoSubmit () {
      this.formOption.submitLoading = true
      this.editRowHandle(this.infoData)
    },
    // 编辑工作流
    editRowHandle (form) {
      let temp = JSON.parse(JSON.stringify(form))
      temp.extend = JSON.parse(JSON.stringify(form)).extend || {}
      temp.extend.enableCancel = form.enableCancel
      temp.extend.enableRestart = form.enableRestart
      temp.extend.autoApproval = form.autoApproval
      temp.extend.enableDynamicApprover = form.enableDynamicApprover
      temp.extend.enableDynamicNode = form.enableDynamicNode
      delete temp.enableCancel
      delete temp.enableRestart
      delete temp.autoApproval
      delete temp.enableDynamicApprover
      delete temp.enableDynamicNode
      editModel(temp).then(res => {
        if(res.data.code == 0) {
          this.formOption.submitLoading = false
          this.$notify({
            title: '提示',
            message: '修改基本信息成功',
            position: 'bottom-right',
            type: 'success'
          });
          this.$set(this, 'infoData', temp)
          this.handleClose()
        }else{
          this.formOption.submitLoading = false
        }
      }).catch(e => {
        this.formOption.submitLoading = false
      })
    },
    // 设计数据结构
    getTemplateData (data, group){
      return {
        id: this.$getDefalut(data, 'templateId', data ? data.id : null),
        baseSetup: {
          icon: this.$getDefalut(data, 'icon', 'el-icon-s-custom'),
          background: this.$getDefalut(data, 'background', '#FF7800'),
          name: this.$getDefalut(data, 'templateName', '未命名的表单'),
          group: this.$getDefalut(group, 'id', ''),
          sign: this.$getDefalut(data, 'sign', false),
          remark: this.$getDefalut(data, 'remark', ''),
          notify: JSON.parse(this.$getDefalut(data, 'notify', JSON.stringify({types:[], title:''}))),
          whoCommit: JSON.parse(this.$getDefalut(data, 'whoCommit', '[]')),
          whoEdit: JSON.parse(this.$getDefalut(data, 'whoEdit', '[]')),
          whoExport: JSON.parse(this.$getDefalut(data, 'whoExport', '[]')),
        },
        form: [],
        process: data ? data : {
          type: nodeType.ROOT,
          name: '发起人',
          id: '10000',
          props: getDefaultNodeProps(nodeType.ROOT),
          // 节点表单参数配置
          nodeForm:{
            formId: "", // 表单id
            sendUserForm: true, // true--使用发起人表单，false-不使用发起人表单
            version: "" // 表单版本
          }
        },
      }
    },
    getRoleHandle () {
      getRoleList().then(res => {
        if(res.data.code == 0) {
          this.roleOption = res.data.data
        }
      })
    },
    // 发布
    publishClick () {
      this.$confirm('确认发布流程？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.publishLoading = true
        let form = JSON.parse(JSON.stringify(this.infoData))
        form.extend = JSON.parse(JSON.stringify(this.infoData)).extend || {}
        form.extend.enableCancel = this.infoData.enableCancel
        form.extend.enableRestart = this.infoData.enableRestart
        form.extend.autoApproval = this.infoData.autoApproval
        form.extend.enableDynamicApprover = this.infoData.enableDynamicApprover
        form.extend.enableDynamicNode = this.infoData.enableDynamicNode
        form.extend.taskStartTriggerEvent = {
          beforeStartFlowEvent: this.infoData.taskStartTriggerEvent.beforeStartFlowEvent
        }
        form.extend.taskEndTriggerEvent = {
          terminatedEvent: this.infoData.taskEndTriggerEvent.terminatedEvent,
          passedEvent: this.infoData.taskEndTriggerEvent.passedEvent,
          rejectedEvent: this.infoData.taskEndTriggerEvent.rejectedEvent
        }
        form.extend.flowButton = {
          enable: this.infoData.flowButton.enable,
          buttons: this.infoData.flowButton.buttons
        }
        delete form.enableCancel
        delete form.enableRestart
        delete form.autoApproval
        delete form.enableDynamicApprover
        delete form.enableDynamicNode
        delete form.taskEndTriggerEvent
        delete form.flowButton
        let temp = {
          id: this.infoData.id,
          designBody: this.template.process,
          jvsAppId: form.jvsAppId,
          name: form.name,
          designGroup: form.designGroup,
          icon: form.icon,
          extend: form.extend,
          description: form.description,
          nodeId: this.nodeId
        }
        // console.log(temp)
        // return
        saveDeployProcess(temp).then(res => {
          if(res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: '发布成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.publishLoading = false
            this.$set(this.infoData, 'designChanged', false)
          } else {
            this.publishLoading = false
          }
        }).catch(() => {
          this.publishLoading = false
        });
      }).catch(() => {
        this.publishLoading = false
      });
    },
    // 停用
    stopClick () {
      this.$confirm('确认停用流程？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(_ => {
        suspendProcess(this.infoData.jvsAppId, this.infoData.id).then(res => {
          if(res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: '流程停用成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.getDetail(this.infoData.id)
          }
        })
      }).catch(_ => {})
    },
    // 选择图标的图片
    handleFocus() {
      this.defaultLabel = '工作流'
      this.iconVisible = true
    },
    iconClose () {
      this.iconVisible = false
      this.defaultLabel = '工作流'
    },
    // 确认选择
    handleConfirm(obj) {
      this.$set(this.infoData, 'icon', obj.fileLink)
      this.iconClose()
    },
    // 流程分类列表
    getFlowableType () {
      flowableType().then(res => {
        if(res.data.code == 0) {
          let typeList = res.data.data
          let dicTemp = []
          for(let i in typeList) {
            dicTemp.push({label: typeList[i], value: typeList[i]})
          }
          this.formOption.column.filter(item => {
            if(item.prop == 'designGroup') {
              item.dicData = dicTemp
            }
          })
        }
      })
    },
    // 列表页配置
    pagesetHandle () {
      if (this.infoData.pageId) {
        this.catalogList = []
        getApplicationMenu(this.$route.query.jvsAppId).then(res => {
          if (res.data && res.data.code == 0 && res.data.data) {
            let hadMenu = false
            if(res.data.data.children && res.data.data.children.length > 0) {
              res.data.data.children.filter(rit => {
                this.catalogList.push({
                  label: rit.name,
                  value: rit.id
                })
                if(this.infoData.menuId && rit.id == this.infoData.menuId) {
                  hadMenu = true
                }
              })
            }
            if(!hadMenu) {
              this.$set(this.infoData, 'menuId', '')
            }
            this.pageVisible = true
          }
        })
      } else {
        createWorkflowCurd(this.$route.query.jvsAppId, this.infoData.id).then(res => {
          if (res.data && res.data.code == 0) {
            const str = location.origin + (`/page-design-ui/#/crud/design?jvsAppId=${this.$route.query.jvsAppId}&id=`+res.data.data.id + (res.data.data.dataModelId ? `&dataModelId=${res.data.data.dataModelId}` : ''))
            this.getDetail(this.infoData.id)
            this.$openUrl(str, '_blank')
          }
        })
      }
    },
    testClick () {
      if(!this.infoData.designBody) {
        this.infoData.designBody = this.template.process
      }
      let data = JSON.parse(JSON.stringify(this.infoData))
      data.extend = JSON.parse(JSON.stringify(this.infoData)).extend || {}
      data.extend.enableCancel = this.infoData.enableCancel
      data.extend.enableRestart = this.infoData.enableRestart
      data.extend.autoApproval = this.infoData.autoApproval
      data.extend.enableDynamicApprover = this.infoData.enableDynamicApprover
      data.extend.enableDynamicNode = this.infoData.enableDynamicNode
      delete data.enableCancel
      delete data.enableRestart
      delete data.autoApproval
      delete data.enableDynamicApprover
      delete data.enableDynamicNode
      data.designBody = this.template.process
      if(typeof data.designBody == 'string') {
        data.designBody = JSON.parse(data.designBody)
      }
      testPrepare(data).then(res => {
        if(res.data && res.data.code == 0) {
          this.testData = res.data.data
          this.dialogType = 'test'
          this.dialogVisible = true
        }
      })
    },
    closeHandle (bool) {
      if(bool) {
        this.handleClose()
      }
    },
    openPageDesign () {
      const str = location.origin + (`/page-design-ui/#/crud/design?jvsAppId=${this.$route.query.jvsAppId}&id=`+this.infoData.pageId + (this.infoData.dataModelId ? `&dataModelId=${this.infoData.dataModelId}` : ''))
      this.$openUrl(str, '_blank')
    },
    pageMenuSubmit () {
      let obj = {
        appId: this.$route.query.jvsAppId,
        designId: this.infoData.pageId,
        designType: 'page',
        type: this.infoData.menuId
      }
      editDesign(obj).then(res => {
        if(res.data && res.data.code == 0) {
          this.$notify({
            title: '提示',
            message: `提交成功`,
            position: 'bottom-right',
            type: 'success'
          });
          this.pageClose()
        }
      })
    },
    pageClose () {
      this.pageVisible = false
    },
    getAllRuleListHandle () {
      getAllRule(this.$route.query.jvsAppId, {dataModelId: this.infoData.dataModelId}).then(res => {
        if(res.data && res.data.code == 0) {
          this.allRuleList = res.data.data
        }
      })
    },
    setRule (type) {
      this.currentType = type
      this.netSetVisible = true
    },
    netSetClose () {
      this.netSetVisible = false
      this.currentType = ''
    },
    viewRule () {
      let attr = 'taskEndTriggerEvent'
      if(this.currentType == 'beforeStartFlowEvent') {
        attr = 'taskStartTriggerEvent'
      }
      if(this.infoData[attr][this.currentType].ruleKey) {
        let currentRuleName = ''
        this.allRuleList.filter(it => {
          if(it.secret == this.infoData[attr][this.currentType].ruleKey) {
            currentRuleName = it.name
          }
        })
        this.$openUrl(`/rule-design-ui/#/ruleDesign?id=${this.infoData[attr][this.currentType].ruleKey}&componentId=${this.infoData.id + this.currentType}&jvsAppId=${this.$route.query.jvsAppId}&name=${currentRuleName}`, '_blank')
      }
    },
    newRuleSet () {
      let currentRuleName = ''
      switch(this.currentType) {
        case 'terminatedEvent': currentRuleName = '撤回触发事件';break;
        case 'passedEvent': currentRuleName = '审批通过触发事件';break;
        case 'rejectedEvent': currentRuleName = '审批不通过触发事件';break;
        case 'beforeStartFlowEvent': currentRuleName = '启动流程前触发事件';break;
        default: ;break;
      }
      this.newRuleSetLoading = true
      getRuleIdByComponentId({jvsAppId: this.infoData.jvsAppId, componentId: this.infoData.id + this.currentType, name: currentRuleName, componentType: 'data', designId: this.infoData.dataModelId}).then(res => {
        if(res.data && res.data.code == 0) {
          this.newRuleSetLoading = false
          if(this.currentType == 'beforeStartFlowEvent') {
            this.infoData.taskStartTriggerEvent[this.currentType].ruleKey = res.data.data
          }else{
            this.infoData.taskEndTriggerEvent[this.currentType].ruleKey = res.data.data
          }
          this.getAllRuleListHandle()
          this.$openUrl(`/rule-design-ui/#/ruleDesign?id=${res.data.data}&componentId=${this.infoData.id + this.currentType}&jvsAppId=${this.infoData.jvsAppId}&name=${currentRuleName}`, '_blank')
          this.netSetClose()
          this.$forceUpdate()
        }else{
          this.newRuleSetLoading = false
        }
      }).catch(e => {
        this.newRuleSetLoading = false
      })
    },
    codeFormatHandle () {
      if(this.infoData.extend.codeFormat) {
        this.codeFormat = JSON.parse(JSON.stringify(this.infoData.extend.codeFormat))
        this.codeFormatVisible = true
      }
    },
    codeFormatClose () {
      this.codeFormat = null
      this.codeFormatVisible = false
    },
    codeFormatSubmit () {
      this.$set(this.infoData.extend, 'codeFormat', JSON.parse(JSON.stringify(this.codeFormat)))
      this.codeFormatClose()
    },
    getFlowNameFieldListHandle () {
      this.fieldKeyOptions = []
      getFlowNameFieldList(this.$route.query.jvsAppId, this.$route.query.id).then(res => {
        if(res.data && res.data.code == 0 && res.data.data) {
          for(let k in res.data.data) {
            this.fieldKeyOptions.push({
              title: k,
              list: res.data.data[k]
            })
          }
        }
      })
    },
    createEditor (name, domId) {
      if(this[name]){
        this[name].destroy()
        this[name] = null
      }
      this[name] = new E('#'+domId)
      this[name].config.placeholder = '默认为流程名称'
      this[name].config.menus = []
      this[name].config.showFullScreen = false
      this[name].config.height= 80
      this[name].config.autoFocus = false
      this[name].config.onchange = (newHtml)=>{
        this.$set(this.infoData.extend, 'taskTitleFormat', newHtml)
        this.$forceUpdate()
      }
      this[name].create()
    },
    popoverClick (item, editor, visible){
      this[visible] = false
      this[editor].cmd.do('insertHTML', `<span style="background:rgba(126,134,142,.2);font-size:14px;padding:3px 5px;border-radius:5px;" title="${item.id}">${item.name}</span><span class="space" style="font-size: 14px;">&nbsp;</span>`)
    },
    getIcon (type) {
      let icon = ''
      switch(type) {
        case 'ROOT': icon = 'icon-jvs-faqiren';break;
        case 'SP': icon = 'icon-jvs-shenpiren';break;
        case 'CS': icon = 'icon-jvs-chaosongren';break;
        case 'TJ': icon = 'jvs-ui-icon-tiaojianfenzhi';break;
        case 'AUTOMATION': icon = 'icon-jvs-yewuluoji';break;
        case 'PB': icon = 'icon-jvs-fenzhi';break;
        default: break;
      }
      return icon
    }
  },
};
</script>

<style lang="scss" scoped>
::-webkit-scrollbar {
  width: 4px;
  height: 4px;
  background-color: #e1e1e1;
}
::-webkit-scrollbar-thumb {
  border-radius: 16px;
  background-color: #99999a;
}
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
/deep/.el-drawer__body{
  overflow: hidden;
  // &::-webkit-scrollbar{
  //   width: 8px;
  //   height: 8px;
  //   background-color: #e1e1e1;
  // }
  // &::-webkit-scrollbar-thumb{
  //   border-radius: 5px;
  //   background-color: #a8a8a9;
  // }
}
.scale {
  z-index: 999;
  position: fixed;
  top: 80px;
  right: 40px;
  background-color: #fff;
  padding: 10px 15px;
  border-radius: 10px;
  box-shadow: 0 0 10px 0 #bcbcbc;
  span {
    margin: 0 10px;
    font-size: 15px;
    color: #7a7a7a;
    width: 50px;
  }
}
.icon-image{
  display: flex;
  align-items: center;
  position: relative;
  img{
    display: block;
    width: 120px;
    height: 120px;
    margin-right: 10px;
  }
  .el-icon-delete{
    position: absolute;
    top: 0;
    left: 100px;
    color: red;
    cursor: pointer;
  }
}
.custom-box{
  .custom-box-item{
    width: 100%;
    margin-bottom: 8px;
    .label{
      font-size: 14px;
      color: #959595;
      line-height: 28px;
      word-break: keep-all;
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
    .content{
      flex: 1;
      display: flex;
      align-items: center;
      overflow: hidden;
      #flowTitle{
        /deep/.w-e-toolbar{
          z-index: 1000!important;
        }
        /deep/.w-e-text-container{
          z-index: 999!important;
        }
      }
    }
  }
  .row{
    width: 100%;
    display: flex;
    align-items: center;
    .content{
      margin-left: 10px;
    }
  }
}
.style-dialog.el-dialog__wrapper{
  /deep/.el-dialog{
    width: 380px;
    height: 520px;
    margin-top: calc(50vh - 260px)!important;
    border-radius: 6px;
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
    .style-dialog-box{
      height: calc(100% - 60px);
      padding: 10px 30px;
      box-sizing: border-box;
      overflow: hidden;
      overflow-y: auto;
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
.popover-list{
  max-height: 400px;
  overflow-y: auto;
  .popover-list-item{
    .title{
      padding: 5px 10px;
      font-weight: 700;
      font-size: 14px;
      color: #363B4C;
    }
    .field-list div{
      padding: 5px 15px;
      cursor: pointer;
      font-size: 14px;
      &:hover{
        background-color: #eef1f6;
      }
    }
  }
}
.process-design-box{
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
    .design-left{
      height: 100vh;
      width: calc(100% - 320px);
    }
    .page-setting{
      width: 900px;
      margin: 16px auto;
      .setting-form{
        .showtype-label{
          font-family: Microsoft YaHei-Bold, Microsoft YaHei;
          font-weight: 700;
          font-size: 14px;
          color: #363B4C;
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
        .el-icon-edit-outline{
          cursor: pointer;
        }
      }
      /deep/.basic-form{
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
          .form-item-tips{
            line-height: 20px;
          }
        }
        .custom-box-item{
          .el-input__inner{
            height: 36px;
            line-height: 36px;
            border: 0;
            background: #F5F6F7;
            font-family: Source Han Sans-Regular, Source Han Sans;
            font-weight: 400;
            font-size: 14px;
            color: #363B4C;
          }
          .w-e-toolbar{
            border: 0!important;
          }
          .w-e-text-container{
            border: 0!important;
            background: #F5F6F7;
            border-radius: 4px;
          }
        }
      }
    }
  }
  .flow-design-box{
    width:100vw;
    height:calc(100% - 56px);
    background-color:#F5F6F7;
    padding: 10px 0;
    box-sizing: border-box;
    overflow: auto;
    .flow-design{
      display: flex;
      transform-origin: 50% 0px 0px;
    }
  }
}
.flowable-node-info-drawer{
  /deep/.el-drawer__container{
    .el-drawer__header{
      margin: 0;
      padding: 24px 24px 16px 24px;
      border-bottom: 1px solid #EEEFF0;
      .title-slot{
        padding: 0 12px;
        display: flex;
        align-items: center;
        height: 32px;
        background: #F5F6F7;
        border-radius: 4px;
        svg{
          width: 16px;
          height: 16px;
        }
        .name-input-box{
          flex: 1;
          margin: 0 8px;
          font-family: Microsoft YaHei-Regular, Microsoft YaHei;
          font-weight: 400;
          font-size: 14px;
          color: #363B4C;
          .el-input{
            .el-input__inner{
              border: 0;
              background: transparent;
              padding: 0;
            }
          }
        }
      }
      .el-drawer__close-btn{
        display: none;
      }
    }
  }
}
.flow-button-box{
  .flow-button-list{
    width: 100%;
    .flow-button-list-item{
      width: calc(50% - 20px);
      display: flex;
      align-items: center;
      .label{
        display: block;
        width: 100px;
      }
      .box{
        flex: 1;
        overflow: hidden;
      }
    }
    .flow-button-list-item-btn{
      margin-top: 10px;
    }
  }
}
</style>
<style lang="scss">
.test-process-dialog.el-dialog__wrapper:not(.form-fullscreen-dialog){
  .el-dialog{
    border-radius: 0!important;
  }
  .el-dialog__header{
    display: none!important;
  }
  .el-dialog__body{
    padding: 0!important;
    .process-test-box{
      .design-header-box{
        .header-left{
          span{
            .el-icon-edit{
              display: none;
            }
          }
        }
        .header-right{
          .help-entry, .el-button.el-button--primary:nth-last-of-type(1){
            display: none;
          }
        }
      }
    }
  }
}
</style>
