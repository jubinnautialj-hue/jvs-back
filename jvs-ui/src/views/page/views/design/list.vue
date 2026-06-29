<template>
  <div class="design-box">
    <div class="design-page-left"></div>
    <div class="design-page-center">
      <design-tool
        ref="topBar"
        :infoData="infoData"
        :dataModelId="dataModelId"
        :dataModelFields="fieldsData"
        :fresh="submitLoding"
      >
        <template slot="right">
          <div
            v-if="infoData.displayType != 'card'"
            class="table-info-form-left-bottom"
          >
            <div
              class="table-info-form-left-bottom-item"
              style="display: flex; align-items: center"
            >
              <span class="label">甘特图</span>
              <el-switch
                v-model="designData.gantt"
                size="mini"
                @change="getColumnHandle(true)"
              ></el-switch>
              <i
                v-if="designData.gantt"
                class="el-icon-setting"
                style="margin-left: 5px; cursor: pointer"
                @click="ganttOpen"
              ></i>
            </div>
            <div class="table-info-form-left-bottom-item">
              <span class="label">隐藏序号</span>
              <el-switch
                v-model="designData.hiddenIndex"
                size="mini"
                @change="getColumnHandle(true)"
              ></el-switch>
            </div>
            <div class="table-info-form-left-bottom-item">
              <span class="label">操作栏固定</span>
              <el-switch
                v-model="designData.menuFixed"
                size="mini"
                @change="getColumnHandle(true)"
              ></el-switch>
            </div>
            <div class="table-info-form-left-bottom-item">
              <span class="label">隐藏筛选</span>
              <el-switch
                v-model="designData.hiddenSearch"
                size="mini"
              ></el-switch>
            </div>
            <div class="table-info-form-left-bottom-item">
              <span class="label">隐藏排序</span>
              <el-switch
                v-model="designData.hiddenSort"
                size="mini"
              ></el-switch>
            </div>
            <div class="table-info-form-left-bottom-item">
              <span class="label">隐藏刷新</span>
              <el-switch
                v-model="designData.hiddenRefresh"
                size="mini"
              ></el-switch>
            </div>
            <div class="table-info-form-left-bottom-item">
              <span class="label">隐藏分享</span>
              <el-switch
                v-model="designData.hiddenShare"
                size="mini"
              ></el-switch>
            </div>
            <div class="table-info-form-left-bottom-item">
              <span class="label">操作栏宽度</span>
              <div class="number-content">
                <el-input-number
                  v-model="designData.menuWidth"
                  :min="150"
                  :max="500"
                  :controls="false"
                  size="mini"
                  @change="getColumnHandle(true)"
                ></el-input-number>
                <span class="suffix-unit">px</span>
              </div>
            </div>
          </div>
        </template>
      </design-tool>
      <!-- 预览部分 -->
      <div v-if="previewVisible" class="preview-box">
        <showTable
          ref="previewTable"
          :menuForm="menuFormData"
          :propData="designData"
          :infoData="infoData"
        />
      </div>
      <!-- 设计部分  表格配置 -->
      <div class="deign-box-content" style="position: relative" v-if="showTabs">
        <tableInfo
          class="table-form-slot-box"
          :fieldsData="fieldsData"
          :data="designColumn"
          :infoData="infoData"
          :designData="designData"
          :regKey="regKey"
          :tableOption="tableOption"
          :needEmpty="needEmpty"
          :roleList="roleList"
          :designId="designId"
          :dataModelId="dataModelId"
          @getReg="getReg"
          @getColumn="getColumnHandle"
          @getLeftButtons="leftTreePermissionHandle"
          @saveLeftTreeButton="saveLeftTreeButton"
        />
      </div>
    </div>
    <div class="design-page-right">
      <jvs-tab
        :active="activeName"
        :option="tabOption"
        @tab-click="handleClick"
        v-if="setShow"
      >
        <!-- 按钮配置 -->
        <template slot="button">
          <buttonInfo
            class="table-form-slot-box"
            :data="designData.buttons"
            :designColumn="designColumn"
            :designId="designId"
            :dataModelId="dataModelId"
            :infoData="infoData"
            :tableSetNameOption="tableSetNameOption"
            :fieldsData="fieldsData"
            @handleSave="saveHandle"
            @permissionHandle="permissionHandle"
            @getColumn="getColumnHandle"
          />
        </template>
        <!-- 表头设置 -->
        <template slot="headtitle">
          <headtitle
            class="table-form-slot-box"
            :data="designData.pageTableTitle"
            :designColumn="designColumn"
            @getColumn="getColumnHandle"
          ></headtitle>
        </template>
        <!-- 排序条件 -->
        <template slot="sort">
          <div class="sort-param-box">
            <div class="sort-param-item">
              <div class="sort-param-title">排序条件</div>
              <sortInfo
                class="table-form-slot-box"
                :data="designData.sorts"
                :tableSetNameOption="fieldsData"
              />
            </div>
            <div class="sort-param-item">
              <div class="sort-param-title">列表过滤</div>
              <!-- 列表过滤放到排序里面 -->
              <!-- <template slot="parameter"> -->
              <parameterInfo
                class="table-form-slot-box"
                :data="designData.parameters"
                :dataModelId="dataModelId"
              />
              <!-- </template> -->
            </div>
          </div>
        </template>
        <!-- 查询条件 -->
        <template slot="search">
          <searchInfo
            class="table-form-slot-box"
            :data="data.viewJson.query"
            :tableSetNameOption="tableSetNameOption"
          />
        </template>
        <!-- 自定义统计 -->
        <template slot="statistics">
          <customStatisticsInfo
            class="table-form-slot-box"
            :data="designData.dataPage.headStatisticalData"
            :tableSetNameOption="tableSetNameOption"
            :roleList="roleList"
          />
        </template>
        <!-- 嵌入页面 -->
        <template slot="pageurl">
          <iframepageInfo
            class="table-form-slot-box"
            :iframePage="designData.dataPage.referencePages"
          />
        </template>
      </jvs-tab>
    </div>
    <!-- 修改菜单 -->
    <el-dialog
      title="修改菜单"
      v-if="menuVisible"
      :visible.sync="menuVisible"
      :before-close="handleCloseMenu"
      append-to-body
      :close-on-click-modal="false"
    >
      <jvs-form
        :formData="menuForm"
        :option="menuFormOption"
        @submit="submitMenu"
      >
        <template slot="menuParentIdForm">
          <el-cascader
            style="width: 100%"
            size="mini"
            v-model="menuForm.menuParentId"
            :show-all-levels="true"
            :options="secondTreeData"
            :props="{
              expandTrigger: 'hover',
              children: 'children',
              label: 'name',
              value: 'id',
            }"
          >
          </el-cascader>
        </template>
      </jvs-form>
    </el-dialog>
    <!-- 数据结构 -->
    <el-dialog
      title="数据结构"
      v-if="viewDataVisible"
      :visible.sync="viewDataVisible"
      append-to-body
      fullscreen
      class="form-fullscreen-dialog json-show-dialog"
      :close-on-click-modal="false"
      :before-close="viewDataClose"
    >
      <div class="row">
        <div>
          <h4>列表数据结构</h4>
          <json-viewer
            v-if="pageDataString"
            style="overflow: auto; flex: 1; min-height: 300px"
            :value="pageDataString"
            :expand-depth="2000"
            copyable
            boxed
            sort
          >
          </json-viewer>
        </div>
        <div>
          <h4>查询条件数据结构</h4>
          <json-viewer
            v-if="queryDataString"
            style="overflow: auto; flex: 1; min-height: 300px"
            :value="queryDataString"
            :expand-depth="2000"
            copyable
            boxed
            sort
          >
          </json-viewer>
        </div>
      </div>
      <div class="row" v-if="false">
        <div>
          <h4>自定义JSQL</h4>
          <codeEditor
            class="jsql-codeEditor"
            prop="customizeJsonCode"
            :code="designData.dataPage.customizeJsqlJson"
            @change="changeHandle"
          ></codeEditor>
        </div>
        <div>
          <h4>JSQL</h4>
          <codeEditor
            class="jsql-codeEditor"
            prop="variableJsonCode"
            :readOnly="true"
            :code="designData.dataPage.jsqlJson"
          ></codeEditor>
        </div>
      </div>
    </el-dialog>
    <!-- 请求配置 -->
    <el-dialog
      title="列表页请求配置"
      append-to-body
      v-if="dialogVisible"
      :visible.sync="dialogVisible"
      fullscreen
      class="form-fullscreen-dialog"
      :close-on-click-modal="false"
      :before-close="httpClose"
    >
      <dataSourceForm
        sourceType="data_origin_request"
        :form="httpData"
        @submit="submitHandle"
      ></dataSourceForm>
    </el-dialog>
    <!-- 甘特图设置 -->
    <el-dialog
      title="甘特图设置"
      :visible.sync="ganttVisible"
      :before-close="ganttClose"
      width="1000px"
    >
      <div v-if="ganttVisible" class="gantt-setting">
        <jvs-form
          :option="ganttOption"
          :formData="ganttForm"
          @submit="ganttSubmit"
          @cancalClick="ganttClose"
        >
          <template slot="showTypeForm">
            <el-select
              v-model="ganttForm.showType"
              filterable
              placeholder="请选择展示方式"
              size="mini"
              style="width: 100%"
            >
              <el-option label="按月展示" value="month" />
              <el-option label="按天展示" value="day" />
            </el-select>
          </template>
          <template slot="plainStartForm">
            <el-select
              v-model="ganttForm.plainStart"
              filterable
              placeholder="请选择计划开始时间字段"
              size="mini"
              style="width: 100%"
            >
              <el-option
                v-for="(item, key) in fieldsData"
                :key="'plain-start-item-' + '-' + key"
                :disabled="
                  (ganttForm.plainEnd && item.fieldKey == ganttForm.plainEnd) ||
                  (ganttForm.reallyStart &&
                    item.fieldKey == ganttForm.reallyStart) ||
                  (ganttForm.reallyEnd && item.fieldKey == ganttForm.reallyEnd)
                "
                :label="item.fieldName"
                :value="item.fieldKey"
              >
              </el-option>
            </el-select>
          </template>
          <template slot="plainEndForm">
            <el-select
              v-model="ganttForm.plainEnd"
              filterable
              placeholder="请选择计划结束时间字段"
              size="mini"
              style="width: 100%"
            >
              <el-option
                v-for="(item, key) in fieldsData"
                :key="'plain-end-item-' + '-' + key"
                :disabled="
                  (ganttForm.plainStart &&
                    item.fieldKey == ganttForm.plainStart) ||
                  (ganttForm.reallyStart &&
                    item.fieldKey == ganttForm.reallyStart) ||
                  (ganttForm.reallyEnd && item.fieldKey == ganttForm.reallyEnd)
                "
                :label="item.fieldName"
                :value="item.fieldKey"
              >
              </el-option>
            </el-select>
          </template>
          <template slot="reallyStartForm">
            <el-select
              v-model="ganttForm.reallyStart"
              filterable
              placeholder="请选择实际开始时间字段"
              size="mini"
              style="width: 100%"
            >
              <el-option
                v-for="(item, key) in fieldsData"
                :key="'really-start-item-' + '-' + key"
                :disabled="
                  (ganttForm.plainEnd && item.fieldKey == ganttForm.plainEnd) ||
                  (ganttForm.plainStart &&
                    item.fieldKey == ganttForm.plainStart) ||
                  (ganttForm.reallyEnd && item.fieldKey == ganttForm.reallyEnd)
                "
                :label="item.fieldName"
                :value="item.fieldKey"
              >
              </el-option>
            </el-select>
          </template>
          <template slot="reallyEndForm">
            <el-select
              v-model="ganttForm.reallyEnd"
              filterable
              placeholder="请选择实际结束时间字段"
              size="mini"
              style="width: 100%"
            >
              <el-option
                v-for="(item, key) in fieldsData"
                :key="'really-end-item-' + '-' + key"
                :disabled="
                  (ganttForm.plainEnd && item.fieldKey == ganttForm.plainEnd) ||
                  (ganttForm.reallyStart &&
                    item.fieldKey == ganttForm.reallyStart) ||
                  (ganttForm.plainStart &&
                    item.fieldKey == ganttForm.plainStart)
                "
                :label="item.fieldName"
                :value="item.fieldKey"
              >
              </el-option>
            </el-select>
          </template>
          <!-- 实际计划百分比字段选择器 -->
          <template slot="actualPlanPercentForm">
            <el-select
              v-model="ganttForm.actualPlanPercent"
              filterable
              placeholder="请选择实际计划百分比字段"
              size="mini"
              style="width: 100%"
            >
              <el-option
                v-for="(item, key) in fieldsData"
                :key="'actual-plan-percent-item-' + '-' + key"
                :label="item.fieldName"
                :value="item.fieldKey"
              >
              </el-option>
            </el-select>
          </template>
          <template slot="conditionControlEnableForm">
            <el-switch v-model="ganttForm.conditionControlEnable"></el-switch>
          </template>
          <template slot="dynamicColorForm">
            <div
              v-if="ganttForm.conditionControlEnable"
              class="form-column-tableForm"
            >
              <tableForm
                :item="customStyleOption"
                :option="customStyleTableOption"
                :forms="ganttForm"
                :data="ganttForm.conditionControl"
                @setTable="setTableHandle"
              >
                <template slot="menuBtn" slot-scope="scope">
                  <span
                    class="delete-icon-button"
                    @click="deleteStyleRow(scope.row, scope.index)"
                  >
                    <span class="border-line"></span>
                  </span>
                </template>
              </tableForm>
              <div class="bottom-add-button">
                <div
                  class="button"
                  @click="
                    ganttForm.conditionControl.push({});
                    $forceUpdate();
                  "
                >
                  <div class="icon">
                    <svg aria-hidden="true">
                      <use xlink:href="#jvs-ui-icon-xinjian"></use>
                    </svg>
                  </div>
                  <span>新增一行</span>
                </div>
              </div>
            </div>
          </template>
        </jvs-form>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getDesignInfo, updateDesignInfo } from "../../api/design";
import { getRoleList } from "../../api/list";
import { defaultBtnList, columnTypeList } from "../../const/const";
import showTable from "../show/list";
import tableInfo from "./list/table";
import searchInfo from "./list/search";
import sortInfo from "./list/sort";
import buttonInfo from "./list/button";
import customStatisticsInfo from "./list/customStatistics";
import parameterInfo from "./list/parameter";
import headtitle from "./list/headtitle";
import iframepageInfo from "./list/iframe";
import dataSourceForm from "../../plugin/datasource";

import codeEditor from "./coder"; // json编译器
import tableForm from "@/components/basic-assembly/tableForm";

// 表单项
import MInput from "../../plugin/assembly/input";
import MTextarea from "../../plugin/assembly/textarea";
import MInputNumber from "../../plugin/assembly/inputNumber";
import MSwitch from "../../plugin/assembly/switch";
import MTimepicker from "../../plugin/assembly/timepicker";
import MDatePicker from "../../plugin/assembly/datePicker";
import MInputReadOnly from "../../plugin/assembly/inputreadonly";
import MSelect from "../../plugin/assembly/select";
import { getDataStr } from "../../api/formlist";
import DesignTool from "../../components/list/designTool";

export default {
  components: {
    showTable,
    tableInfo,
    searchInfo,
    sortInfo,
    buttonInfo,
    tableForm,
    customStatisticsInfo,
    parameterInfo,
    headtitle,
    iframepageInfo,
    dataSourceForm,
    codeEditor,
    DesignTool,
  },
  props: {
    infoData: {
      type: Object,
    },
    roleType: {
      type: Boolean,
    },
    role: {
      type: Array,
    },
    menuId: {
      type: Number,
    },
    menuName: {
      type: String,
    },
    menuFormData: {
      type: Object,
    },
    menuDicData: {
      type: Array,
    },
    systemId: {
      type: String,
    },
    treeData: {
      type: Array,
    },
    saveRandom: {
      type: String,
    },
    designId: {
      type: String,
    },
    dataModelId: {
      type: String,
    },
    dataPermissionList: {
      type: Array,
    },
    currentTab: {
      type: String,
    },
  },
  computed: {
    secondTreeData: {
      get() {
        let temp = [];
        for (let i in this.treeData) {
          let obj = {
            name: this.treeData[i].name,
            id: this.treeData[i].id,
          };
          if (
            this.treeData[i].children &&
            this.treeData[i].children.length > 0
          ) {
            obj.children = [];
            for (let j in this.treeData[i].children) {
              let tobj = {
                name: this.treeData[i].children[j].name,
                id: this.treeData[i].children[j].id,
              };
              obj.children.push(tobj);
            }
          }
          temp.push(obj);
        }
        return temp;
      },
    },
    customStyleTableOption: {
      get() {
        let obj = {
          addBtn: false,
          viewBtn: false,
          delBtn: false,
          editBtn: false,
          page: false,
          border: this.customStyleOption.border,
          align: this.customStyleOption.align || "left",
          menuAlign: this.customStyleOption.menuAlign || "left",
          cancal: false,
          showOverflow: true,
          hideTop: this.customStyleOption.hideTop || false,
          tableColumn: this.customStyleOption.tableColumn,
        };
        return obj;
      },
      set() {},
    },
  },
  data() {
    return {
      // 数据对象
      data: {
        id: "",
        menuId: "",
        menuName: "",
        dataSourceId: "",
        tableId: "",
        enable: false,
        type: "senior",
        deployVersion: "", // 版本号
        // fields: [], // 默认字段
        // 设置相关参数
        viewJson: {
          column: [], // 表格配置--字段
          query: [], // 查询条件
          sort: [], // 排序条件
          // 按钮
          btns: JSON.parse(JSON.stringify(defaultBtnList)),
          // sql 查询
          customStatistics: [],
          // 自定义参数
          parameters: [],
          // 引用页面
          iframepage: {
            url: "",
            position: "bottom",
            width: 100,
            height: 100,
          },
        },
      },
      setShow: true, // false, // 获取角色列表后显示
      // tab参数
      activeName: "button",
      tabOption: {
        column: [
          { label: "按钮配置", name: "button" },
          { label: "排序过滤", name: "sort" }, // 过滤和排序放一起 // {label: '列表过滤', name: 'parameter'},
          { label: "复杂表头", name: "headtitle", display: true },
          // {label: '自定义统计', name: 'statistics'},
          // {label: '引用页面', name: 'pageurl'},
        ],
      },
      // table参数
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
      },
      option: {
        cancal: false,
        addBtn: false,
        editBtn: false,
        viewBtn: false,
        delBtn: false,
        page: false,
        // align: 'center',
        // menuAlign: 'center',
        selection: true,
        search: true,
        showOverflow: true,
        // 搜索表单设置
        formAlign: "right", //对其方式
        inline: false, // 表单项是否可以同行,当垂直方向空间受限且表单较简单时，可以在一行内放置表单
        labelWidth: "auto", // label宽
        submitBtn: true, // 提交按钮是否显示，默认显示
        submitBtnText: "查询", // 提交按钮文字，默认 提交
        column: [],
      },
      tableData: [],
      tableSetNameOption: [], // 显示的字段
      tableSetNameOptionDefault: [], // 除自定义外的所有字段
      tableSetNameOptionAll: [], // 全部字段
      roleList: [], // 角色列表
      previewVisible: false, // 预览弹框
      keyList: [], // 字段名列表

      menuVisible: false, // 菜单弹框
      // 菜单
      menuForm: {},
      menuFormOption: {
        cancal: false,
        column: [
          {
            label: "上级菜单",
            prop: "menuParentId",
            formSlot: true,
            // type: 'select',
            // dicData: [],
            rules: [
              { required: true, message: "请选择上级菜单", trigger: "change" },
            ],
          },
          {
            label: "名称",
            prop: "menuName",
            rules: [{ required: true, message: "请输入名称", trigger: "blur" }],
          },
          {
            label: "图标",
            prop: "menuIcon",
            type: "iconSelect",
          },
          {
            label: "排序",
            prop: "menuSort",
            type: "inputNumber",
          },
        ],
      },
      // 整体数据结构
      designData: {
        dataPage: {
          referencePages: [
            {
              address: "",
              enable: false,
              height: 0,
              position: "",
            },
          ],
        },
      },
      designColumn: [], // 表格配置数据
      submitLoding: false,
      defaultSubShowData: {}, // 默认业务数据
      masterTable: "", // 表单id
      showTabs: false,
      design_default_btn: [
        {
          name: "新增",
          position: "top",
          type: "btn_add",
          isDefault: true,
          enable: true,
          mobileEnable: true,
          form: {
            formType: "normalForm",
            formdata: [
              {
                formsetting: {
                  labelposition: "top",
                  labelwidth: 80,
                  formsize: "mini",
                  btnSetting: [],
                  fullscreen: false,
                },
                autoTableFields: [],
              },
            ],
          },
        },
        {
          name: "修改",
          position: "line",
          type: "btn_modify",
          isDefault: true,
          enable: true,
          mobileEnable: true,
          form: {
            formType: "normalForm",
            formdata: [
              {
                formsetting: {
                  labelposition: "top",
                  labelwidth: 80,
                  formsize: "mini",
                  btnSetting: [],
                  fullscreen: false,
                },
                autoTableFields: [],
              },
            ],
          },
        },
        {
          name: "删除",
          position: "line",
          type: "btn_delete",
          isDefault: true,
          enable: true,
          mobileEnable: true,
          form: {
            formType: "normalForm",
            formdata: [],
          },
        },
        {
          name: "详情",
          position: "line",
          type: "btn_detail",
          isDefault: true,
          enable: true,
          mobileEnable: true,
          form: {
            formType: "detailForm",
            formdata: [
              {
                formsetting: {
                  labelposition: "top",
                  labelwidth: 80,
                  formsize: "mini",
                  btnSetting: [],
                  fullscreen: false,
                },
                autoTableFields: [],
              },
            ],
          },
        },
        {
          name: "导入",
          position: "top",
          type: "btn_import",
          isDefault: true,
          enable: false,
          mobileEnable: false,
          form: null,
        },
        {
          name: "导出",
          position: "top",
          type: "btn_export",
          isDefault: true,
          enable: false,
          mobileEnable: false,
          form: null,
        },
        {
          name: "下载模板",
          position: "top",
          type: "btn_download_template",
          isDefault: true,
          enable: false,
          mobileEnable: false,
          form: null,
        },
      ],
      selectDataSourceId: "", // 数据源id
      selectDataSourceName: "", // 选择的数据源名称
      dataSourceOption: [], // 数据源列表
      tableOption: [], // 数据表列表
      needEmpty: -1, // 是否切换了数据源
      viewDataVisible: false, // 查看数据结构
      pageDataString: {}, // 列表数据结构
      queryDataString: {}, // 查询条件数据结构
      dialogVisible: false, // 列表页请求配置
      httpData: {}, // 请求配置
      fieldsData: [], // 设计数据结构
      regErr: [], // 正则校验结果
      regKey: "normal",
      ganttVisible: false,
      ganttForm: null,
      ganttOption: {
        emptyBtn: false,
        column: [
          {
            label: "展示方式",
            prop: "showType",
            formSlot: true,
            span: 12,
            rules: [
              {
                required: true,
                message: "请选择展示方式",
                trigger: "blur",
              },
            ],
          },
          {
            label: "计划开始时间",
            prop: "plainStart",
            formSlot: true,
            span: 12,
            rules: [
              {
                required: true,
                message: "请选择计划开始时间",
                trigger: "blur",
              },
            ],
          },
          {
            label: "计划结束时间",
            prop: "plainEnd",
            formSlot: true,
            span: 12,
            rules: [
              {
                required: true,
                message: "请选择计划结束时间",
                trigger: "blur",
              },
            ],
          },
          {
            label: "实际开始时间",
            prop: "reallyStart",
            formSlot: true,
            span: 12,
            rules: [
              {
                required: true,
                message: "请选择实际开始时间",
                trigger: "blur",
              },
            ],
          },
          {
            label: "实际结束时间",
            prop: "reallyEnd",
            formSlot: true,
            span: 12,
            rules: [
              {
                required: true,
                message: "请选择实际结束时间",
                trigger: "blur",
              },
            ],
          },
          // 添加实际计划百分比字段
          {
            label: "实际百分比",
            prop: "actualPlanPercent",
            formSlot: true,
            span: 12,
          },
          {
            label: "计划颜色",
            prop: "plainColor",
            type: "colorPicker",
            clearable: true,
            span: 12,
          },
          {
            label: "实际颜色",
            prop: "reallyColor",
            type: "colorPicker",
            clearable: true,
            span: 12,
          },
          {
            label: "动态颜色",
            prop: "conditionControlEnable",
            formSlot: true,
            span: 12,
          },
          {
            label: "",
            prop: "dynamicColor",
            formSlot: true,
            span: 24,
          },
        ],
      },
      customStyleOption: {
        prop: "conditionControl",
        type: "tableForm",
        editable: true,
        border: true,
        editable: true,
        addBtn: false,
        delBtn: true,
        align: "center",
        menuAlign: "center",
        iconBtn: true,
        showOverflow: false,
        tableColumn: [
          {
            label: "字段",
            prop: "key",
            showOverflow: false,
          },
          {
            label: "值",
            prop: "value",
            showOverflow: false,
          },
          {
            label: "计划颜色",
            prop: "plainColor",
            type: "colorPicker",
            clearable: true,
          },
          {
            label: "实际颜色",
            prop: "reallyColor",
            type: "colorPicker",
            clearable: true,
          },
        ],
      },
    };
  },
  created() {
    this.initHandle();
    this.getDesignDataStr(this.dataModelId);

    this.$root.eventBus.$off("pageEvent");
    this.$root.eventBus.$on("pageEvent", (newVal, infoData) => {
      if (typeof newVal == "string") {
        switch (newVal) {
          case "save":
            this.saveHandle(infoData);
            break;
          case "http":
            this.setHttpHandle();
            break;
          case "json":
            this.viewDataJson();
            break;
          case "download":
            this.downloadHande();
            break;
          case "permission":
            this.permissionHandle();
            break;
          case "pubTemp":
            this.publishTempSubmit();
            break;
          default:
            break;
        }
      } else {
        if (newVal.type == "pubTemp") {
          this.publishTempSubmit(newVal.data);
        }
      }
    });
  },
  beforeDestory() {
    this.$root.eventBus.$off("pageEvent");
  },
  methods: {
    // 获取设计数据结构
    getDesignDataStr(id) {
      getDataStr(this.infoData.jvsAppId, id).then((res) => {
        if (res.data && res.data.code == 0 && res.data.data) {
          const arr = [...res.data.data];
          this.fieldsData = arr.filter((item) => {
            return item.fieldName !== "";
          });
        }
      });
    },
    async initHandle() {
      if (this.infoData.id) {
        await this.getDesignInfoHandle();
      }
    },
    // 拉取设计数据
    async getDesignInfoHandle() {
      await getDesignInfo(this.infoData.jvsAppId, this.infoData.id).then(
        (res) => {
          if (res.data.code == 0 && res.data.data) {
            // 打印甘特图配置
            if (res.data.data.viewJson) {
              const viewJson = JSON.parse(res.data.data.viewJson);
              if (viewJson.ganttForm) {
              }
            }
            if (res.data.data.viewJson) {
              // console.log('viewJson', JSON.parse(res.data.data.viewJson))
              this.upDataHandle(JSON.parse(res.data.data.viewJson));
            } else {
              this.upDataHandle({});
            }
            this.$emit("getDesignData", res.data.data);
          }
        }
      );
    },
    // 更新设计
    async upDataHandle(data) {
      this.showTabs = true;
      this.designColumn = [];
      if (!data.dataPage) {
        data.dataPage = {
          tables: [],
          iframePage: [{}],
        };
      }
      if (data.dataPage) {
        if (!data.dataPage.headStatisticalData) {
          data.dataPage.headStatisticalData = [];
        }
        if (!data.dataPage.referencePages) {
          data.dataPage.referencePages = [
            {
              address: "",
              enable: false,
              height: 0,
              position: "",
            },
          ];
        }
        if (data.dataPage.databaseName) {
          this.selectDataSourceName = data.dataPage.databaseName;
        }
      }
      // 打印甘特图相关数据
      // 特别打印 actualPlanPercent 字段
      if (data.ganttForm) {
      } else {
      }

      // 默认填充按钮数据
      if (!data.buttons) {
        data.buttons = this.design_default_btn;
        this.$emit("getButtons", data.buttons);
      }
      data.buttons.filter((dbitem) => {
        if (dbitem.mobileEnable === false) {
          dbitem.mobileEnable = false;
        } else {
          if (!dbitem.mobileEnable && !dbitem.enable) {
            dbitem.mobileEnable = false;
          } else {
            dbitem.mobileEnable = true;
          }
        }
      });
      if (!data.leftTreeButton) {
        data.leftTreeButton = [];
      }
      if (!data.sorts) {
        data.sorts = [];
      }
      if (!data.parameters) {
        data.parameters = [];
      }
      if (!data.pageTableTitle) {
        data.pageTableTitle = [];
      }
      if (!data.menuFixed) {
        data.menuFixed = false;
      }
      if (!data.menuWidth) {
        data.menuWidth = 150;
      }
      this.designData = data;
      // 确保 ganttForm 存在并有默认值
      if (!this.designData.ganttForm) {
        this.designData.ganttForm = {
          showType: "month", // 甘特图默认按月展示
          conditionControl: null,
          conditionControlEnable: false,
          plainColor: "#02a8fc",
          plainEnd: "",
          plainStart: "",
          reallyColor: "#03d353",
          reallyEnd: "",
          reallyStart: "",
          actualPlanPercent: "", // 确保 actualPlanPercent 字段存在
        };
      } else {
        // 如果 ganttForm 存在，确保 actualPlanPercent 字段存在
        if (
          !this.designData.ganttForm.hasOwnProperty("actualPlanPercent") ||
          this.designData.ganttForm.actualPlanPercent === undefined
        ) {
          this.$set(this.designData.ganttForm, "actualPlanPercent", "");
        } else if (this.designData.ganttForm.actualPlanPercent === null) {
          // 如果字段值为 null，也设置为空字符串
          this.$set(this.designData.ganttForm, "actualPlanPercent", "");
        } else {
        }
        !this.designData.ganttForm.showType &&
          (this.designData.ganttForm.showType = "month"); // 甘特图默认按月展示
      }

      if (
        this.designData.dataPage &&
        this.designData.dataPage.autoTableFields
      ) {
        this.designColumn = this.designData.dataPage.autoTableFields;
        this.setTableColumnNameOption();
      } else {
        if (!(this.designColumn && this.designColumn.length > 0)) {
          this.designColumn = [
            { show: true, supportShow: true, dbJavaType: "field_text" },
          ];
        }
      }
      // 生成默认业务数据
      this.genDefaultData();
      this.previewVisible = true;
      this.$forceUpdate();
    },
    // 选项卡切换
    handleClick(tab) {
      this.activeName = tab;
    },
    // 获取表格数据可选项
    setTableColumnNameOption() {
      this.tableSetNameOption = [];
      this.tableSetNameOptionDefault = [];
      this.tableSetNameOptionAll = [];
      for (let i in this.designColumn) {
        if (this.designColumn[i].show === true) {
          this.tableSetNameOption.push({
            label: this.designColumn[i].showChinese,
            value: this.designColumn[i].aliasColumnName,
            dataType: this.designColumn[i].dataType,
            seniorSetting: this.designColumn[i].seniorSetting || {},
          });
        }
        if (this.designColumn[i].disabled === false) {
        } else {
          this.tableSetNameOptionDefault.push({
            label: this.designColumn[i].showChinese,
            value: this.designColumn[i].aliasColumnName,
            dataType: this.designColumn[i].dataType,
            seniorSetting: this.designColumn[i].seniorSetting || {},
          });
        }
        this.tableSetNameOptionAll.push({
          label: this.designColumn[i].showChinese,
          value: this.designColumn[i].aliasColumnName,
          dataType: this.designColumn[i].dataType,
          seniorSetting: this.designColumn[i].seniorSetting || {},
        });
      }
    },
    // 获取角色列表
    async getRoleListHandle() {
      await getRoleList().then((res) => {
        if (res.data.code == 0) {
          this.roleList = res.data.data;
          this.setShow = true;
        }
      });
    },
    // 返回
    backToList() {
      // this.$router.push("/list")
      this.$emit("closeDesign", true);
    },
    // 表格配置通知获取字段
    getColumnHandle(bool) {
      if (bool) {
        this.setTableColumnNameOption();
        this.formatPageData();
        // this.previewVisible = false
        this.$nextTick(() => {
          this.previewVisible = true;
          this.$refs.previewTable.previewInit();
        });
        this.$forceUpdate();
      }
    },
    // 获取正则校验结果
    getReg(reg) {
      this.regErr = reg;
    },
    // 格式化列表页数据
    formatPageData() {
      // 查询条件  格式
      this.genDefaultData();
      let queryJson = this.genDefaultData("search"); // JSON.parse(JSON.stringify(this.defaultSubShowData))
      queryJson.size = 20;
      queryJson.current = 1;
      this.designData.dataPage.queryJson = JSON.stringify(queryJson);
      // 表格数据  格式
      let dataPageJson = {
        records: JSON.parse(JSON.stringify([this.defaultSubShowData])),
        current: 1,
        total: 0,
      };
      this.designData.dataPage.dataPageJson = JSON.stringify(dataPageJson);
      this.designData.dataPage.autoTableFields = this.designColumn;
      if (this.selectDataSourceName) {
        this.designData.dataPage.databaseName = this.selectDataSourceName;
      }

      // 打印甘特图表单数据
      if (this.designData.ganttForm) {
      }

      // 确保在保存前 ganttForm 中的 actualPlanPercent 字段不为 undefined
      if (
        this.designData.ganttForm &&
        (this.designData.ganttForm.actualPlanPercent === undefined ||
          this.designData.ganttForm.actualPlanPercent === null)
      ) {
        this.$set(this.designData.ganttForm, "actualPlanPercent", "");
      }
    },
    // 设置权限
    permissionHandle() {
      this.$emit("getButtons", this.designData.buttons);
      this.getColumnHandle(true);
    },
    // 左树权限
    leftTreePermissionHandle(data) {
      this.designData.leftTreeButton = data;
      this.$emit("getLeftButtons", data);
      this.getColumnHandle(true);
    },
    // 保存列表设计
    saveHandle(isRule) {
      let infoData = JSON.parse(JSON.stringify(this.infoData));
      if (typeof isRule === "object") {
        infoData = JSON.parse(JSON.stringify(isRule));
        isRule = false;
      }
      if (this.submitLoding) {
        return false;
      }
      this.regKey === "normal";
      if (
        this.designData.dataPage &&
        this.designData.dataPage.autoTableFields
      ) {
        const arr = [...this.designData.dataPage.autoTableFields];
        for (let i in arr) {
          if (!arr[i].aliasColumnName) {
            this.regKey = "err";
          }
          this.regKey = arr[i].aliasColumnName ? "normal" : "err";
          this.fieldsData.forEach((item) => {
            if (item.fieldKey === arr[i].showChinese) {
              arr[i].showChinese = item.fieldName;
            }
          });
        }
      }
      if (this.regKey === "err") {
        this.$notify({
          title: "提示",
          message: "字段名不能包含空格和特殊符号",
          position: "bottom-right",
          type: "error",
        });
        return false;
      }
      this.formatPageData();
      this.designData.role = JSON.parse(JSON.stringify(infoData.role || []));
      this.designData.roleType = JSON.parse(
        JSON.stringify(infoData.roleType || false)
      );
      this.designData.stepDataPermission = infoData.stepDataPermission || false;
      this.designData.dataRole = JSON.parse(
        JSON.stringify(infoData.dataRole || [])
      );
      this.designData.dataRoleType = infoData.dataRoleType || "datamodel";
      this.designData.displayType = infoData.displayType || "table";
      this.designData.dataPage.displayType = infoData.displayType || "table";
      this.designData.relationDesignIds = infoData.relationDesignIds;
      this.designData.description = this.infoData.description || "";
      if (this.infoData.name) {
        this.designData.name = this.infoData.name;
      }
      if (this.infoData.icon) {
        this.designData.icon = this.infoData.icon;
      }
      console.log("saveHandle", this.designData);
      if (infoData.isDeploy && !isRule) {
        this.$confirm(
          "设计已发布，保存使用新的设计，此操作无法恢复，是否确认保存？",
          "提示",
          {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
          }
        )
          .then(() => {
            this.submitLoding = true;
            updateDesignInfo(
              this.$route.query.jvsAppId,
              infoData.id,
              this.designData
            )
              .then((res) => {
                if (res.data.code == 0) {
                  this.$notify({
                    title: "提示",
                    message: "保存成功",
                    position: "bottom-right",
                    type: "success",
                  });
                  this.submitLoding = false;
                  this.showTabs = false;
                  this.getDesignInfoHandle();
                  this.$emit("getList");
                }
              })
              .catch((e) => {
                this.submitLoding = false;
              });
          })
          .catch((e) => {
            this.$emit("handleSaveLoading");
          });
      } else {
        this.submitLoding = true;
        updateDesignInfo(
          this.$route.query.jvsAppId,
          infoData.id,
          this.designData
        )
          .then((res) => {
            if (res.data.code == 0) {
              this.$notify({
                title: "提示",
                message: "保存成功",
                position: "bottom-right",
                type: "success",
              });
              this.submitLoding = false;
              this.showTabs = false;
              this.getDesignInfoHandle();
              this.$emit("getList");
            }
          })
          .catch((e) => {
            this.submitLoding = false;
          });
      }
    },
    // 生成默认按钮设置
    getDefaultFormSet(list) {
      for (let i in list) {
        switch (list[i].type) {
          case "IMPORT":
            list[i].importSetting = JSON.parse(JSON.stringify(this.keyList));
            break;
          case "EXPORT":
            list[i].exportSetting = JSON.parse(JSON.stringify(this.keyList));
            break;
          case "FORM":
            // 详情表单
            let formdata = {
              forms: [],
              formsetting: {
                btnSetting: [],
                formsize: "mini",
                labelposition: "top",
                labelwidth: 80,
              },
            };
            if (list[i].fineGrainedType == "DETAIL") {
              formdata.forms = this.eachColumnList("detail");
              list[i].formDesign.formType = "detailForm";
            } else {
              formdata.forms = this.eachColumnList("form");
              list[i].formDesign.formType = "normalForm";
            }
            list[i].formDesign.formdata = [formdata];
            break;
          case "NETWORK":
            list[i].networkForm = { type: "delete", headers: {}, url: "" };
          default:
            break;
        }
      }
      return JSON.parse(JSON.stringify(list));
    },
    // 遍历字段列表生成表单项
    eachColumnList(type) {
      let temp = [];
      for (let i in this.data.viewJson.column) {
        let columnTemp = this.getIteTypeOfForm(
          this.data.viewJson.column[i].dataType
        );
        if (!columnTemp) {
          columnTemp = {
            type: "select",
          };
        }
        let obj = {};
        if (type == "detail") {
          obj = new MInputReadOnly();
        } else {
          switch (columnTemp.type) {
            case "input":
              obj = new MInput();
              break;
            case "textarea":
              obj = new MTextarea();
              break;
            case "inputNumber":
              obj = new MInputNumber();
              break;
            case "switch":
              obj = new MSwitch();
              break;
            case "timePicker":
              obj = new MTimepicker();
              break;
            case "datePicker":
              obj = new MDatePicker();
              break;
            case "select":
              obj = new MSelect();
              obj.multiple = false;
              if (
                this.data.viewJson.column[i].seniorSetting &&
                this.data.viewJson.column[i].seniorSetting.enumValues
              ) {
                obj.dicData =
                  this.data.viewJson.column[i].seniorSetting.enumValues;
              }
              obj.sqlType = "enum";
              break;
            default:
              obj = new MInput();
              break;
          }
        }
        obj.label = this.data.viewJson.column[i].showChinese;
        obj.prop = this.data.viewJson.column[i].columnName;
        if (columnTemp.datetype) {
          obj.datetype = columnTemp.datetype;
        }
        if (columnTemp.num == "int") {
          obj.precision = 0;
        }
        temp.push(obj);
      }
      return temp;
    },
    // 根据数据类型获取表单项组件类型
    getIteTypeOfForm(type) {
      for (let i in columnTypeList) {
        if (columnTypeList[i].value == type) {
          return columnTypeList[i];
        }
      }
    },
    // 下载源码
    downloadHande() {},
    // 下载文件
    downloadFile(filename, content) {
      var elink = document.createElement("a");
      if (filename) {
        elink.download = filename;
      }
      elink.style.display = "none";

      var blob = new Blob([content]); //,{type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8'})
      elink.href = URL.createObjectURL(blob);

      document.body.appendChild(elink);
      elink.click();

      document.body.removeChild(elink);
    },
    // 关闭修改弹框
    handleCloseMenu() {
      this.menuVisible = false;
    },
    // 提交
    submitMenu(form) {
      let obj = form;
      form.id = this.data.menuId || form.menuId;
      form.parentId = form.menuParentId;
      return false;
    },
    // 删除菜单
    deleteMenu() {
      this.$confirm("确认删除？")
        .then((_) => {
          // this.backToList()
        })
        .catch((_) => {});
    },
    // 生成默认业务数据格式
    genDefaultData(from) {
      let obj = {};
      for (let i in this.designColumn) {
        let type = "";
        let itemObj = {};
        let key = "";
        if (
          from == "search" &&
          this.designColumn[i].queryConditionConfig &&
          this.designColumn[i].queryConditionConfig.type
        ) {
          type = this.designColumn[i].queryConditionConfig.type;
          itemObj = this.designColumn[i].queryConditionConfig;
          key = this.designColumn[i].queryConditionConfig.prop;
        } else {
          type = this.designColumn[i].componentType;
          itemObj = this.designColumn[i];
          key = this.designColumn[i].aliasColumnName;
        }
        switch (type) {
          case "inputNumber":
            if (itemObj.isFloat == true) {
              obj[key] = 1.0001;
            } else {
              obj[key] = 1;
            }
            break;
          case "input":
            obj[key] = "";
            break;
          case "timePicker":
            if (itemObj.isrange) {
              obj[key] = ["10:00:00", "12:00:00"];
            } else {
              obj[key] = "10:00:00";
            }
            break;
          case "datePicker":
            if (
              ["daterange", "datetimerange", "monthrange"].indexOf(
                itemObj.datetype
              ) > -1
            ) {
              if (itemObj.datetype == "daterange") {
                obj[key] = ["2021-04-23", "2021-04-24"];
              }
              if (itemObj.datetype == "datetimerange") {
                obj[key] = ["2021-04-23 10:00:00", "2021-04-24 10:00:00"];
              }
              if (itemObj.datetype == "monthrange") {
                obj[key] = ["2021-04", "2021-05"];
              }
            } else {
              obj[key] = "2021-04-23";
            }
            break;
          case "timeSelect":
            obj[key] = "10:00";
            break;
          case "switch":
            obj[key] = false;
            break;
          case "select":
          case "role":
          case "user":
          case "post":
            if (itemObj.multiple) {
              obj[key] = [];
            } else {
              obj[key] = "";
            }
            break;
          case "checkbox":
            obj[key] = [];
            break;
          case "department":
          case "cascader":
          case "chinaArea":
            if (itemObj.multiple) {
              if (itemObj.emitPath) {
                obj[key] = [
                  [1, 2, 3],
                  [1, 2, 4],
                ];
              } else {
                obj[key] = [3, 4];
              }
            } else {
              if (itemObj.emitPath) {
                obj[key] = [1, 2, 3];
              } else {
                obj[key] = 3;
              }
            }
            break;
          default:
            obj[key] = "";
            break;
        }
      }
      if (from == "search") {
        return obj;
      } else {
        this.defaultSubShowData = obj;
      }
    },
    // 查看数据结构
    viewDataJson() {
      this.genDefaultData();
      this.queryDataString = this.genDefaultData("search"); //JSON.parse(JSON.stringify(this.defaultSubShowData))
      this.queryDataString.size = 20;
      this.queryDataString.current = 1;
      // 表格数据  格式
      this.pageDataString = {
        records: JSON.parse(JSON.stringify([this.defaultSubShowData])),
        current: 1,
        total: 0,
      };
      this.viewDataVisible = true;
    },
    viewDataClose() {
      this.viewDataVisible = false;
    },
    // 打开地址设置
    setHttpHandle() {
      if (
        this.designData &&
        this.designData.dataPage &&
        this.designData.dataPage.http
      ) {
        this.httpData = {
          http: JSON.parse(JSON.stringify(this.designData.dataPage.http)),
        };
      }
      this.dialogVisible = true;
    },
    httpClose() {
      this.dialogVisible = false;
    },
    submitHandle(form) {
      this.$set(this.designData.dataPage, "http", form.http);
      this.httpClose();
    },
    // editor赋值
    changeHandle(code) {
      this.$set(this.designData.dataPage, "customizeJsqlJson", code);
    },
    saveLeftTreeButton(btn) {
      this.designData.leftTreeButton = btn;
      this.saveHandle();
      this.getColumnHandle(true);
      this.$forceUpdate();
    },
    ganttOpen() {
      this.ganttForm = {};
      if (this.designData.ganttForm) {
        this.ganttForm = JSON.parse(JSON.stringify(this.designData.ganttForm));
        // 处理 actualPlanPercent 字段的 undefined 或 null 值
        if (
          this.ganttForm.actualPlanPercent === undefined ||
          this.ganttForm.actualPlanPercent === null
        ) {
          this.ganttForm.actualPlanPercent = "";
        }
        !this.designData.ganttForm.showType &&
          (this.designData.ganttForm.showType = "month"); // 甘特图默认按月展示
        // 打印回显的数据
      } else {
        // 如果没有 ganttForm，初始化一个默认的
        this.ganttForm = {
          showType: "month", // 甘特图默认按月展示
          conditionControl: null,
          conditionControlEnable: false,
          plainColor: "#02a8fc",
          plainEnd: "",
          plainStart: "",
          reallyColor: "#03d353",
          reallyEnd: "",
          reallyStart: "",
          actualPlanPercent: "", // 确保 actualPlanPercent 字段存在
        };
      }
      this.ganttVisible = true;
    },
    deleteStyleRow(row, index) {
      this.ganttForm.conditionControl.splice(index, 1);
      this.$forceUpdate();
    },
    // 同步表格数据
    setTableHandle(data) {
      this.$set(this.ganttForm, "conditionControl", data);
      this.$forceUpdate();
    },
    ganttSubmit() {
      // 确保actualPlanPercent字段被正确处理
      if (
        !this.ganttForm.hasOwnProperty("actualPlanPercent") ||
        this.ganttForm.actualPlanPercent === undefined ||
        this.ganttForm.actualPlanPercent === null
      ) {
        this.$set(this.ganttForm, "actualPlanPercent", "");
      }

      // 确保 designData 中有 ganttForm 对象
      if (!this.designData.ganttForm) {
        this.$set(this.designData, "ganttForm", {});
      }

      // 将 ganttForm 数据保存到 designData 中
      this.$set(
        this.designData,
        "ganttForm",
        JSON.parse(JSON.stringify(this.ganttForm))
      );
      this.getColumnHandle(true);
      this.ganttClose();
    },
    ganttClose() {
      this.ganttForm = null;
      this.ganttVisible = false;
    },
  },
  watch: {
    currentTab: {
      handler(newVal, oldVal) {
        if (newVal == "design") {
          this.tabOption.column.filter((cit) => {
            if (cit.name == "headtitle") {
              cit.display = this.infoData.displayType != "card";
            }
          });
          if (this.infoData.displayType == "card") {
            if (this.activeName == "headtitle") {
              this.setShow = false;
              this.activeName = "button";
              this.$nextTick(() => {
                this.setShow = true;
              });
            }
            this.getColumnHandle(true);
          }
          this.$forceUpdate();
        }
      },
    },
  },
};
</script>
<style lang="scss" scoped>
.bottom-add-button {
  margin-top: 8px;
  .button {
    width: 80px;
    display: flex;
    align-items: center;
    cursor: pointer;
    .icon {
      width: 16px;
      height: 16px;
      background: #1e6fff;
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 4px;
      svg {
        width: 12px;
        height: 12px;
        fill: #fff;
      }
    }
    span {
      font-family: Microsoft YaHei-Regular, Microsoft YaHei;
      font-weight: 400;
      font-size: 14px;
      color: #1e6fff;
      line-height: 18px;
    }
  }
}
.delete-icon-button {
  width: 16px;
  height: 16px !important;
  background: #36b452;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  cursor: pointer;
  .border-line {
    width: 10px;
    height: 2px;
    background: #fff;
    border-radius: 2px;
  }
}
.gantt-setting {
  .jvs-form {
    /deep/.el-col-12 {
      .el-form-item {
        margin-left: 10px;
        margin-right: 10px;
      }
    }
    /deep/.form-column-colorPicker .el-form-item--mini {
      margin-left: 10px !important;
    }
    /deep/.form-column-colorPicker .el-form-item--mini .el-popover__reference {
      min-width: 130px !important;
    }
    /deep/.form-column-colorPicker .el-form-item--mini .el-popover__reference {
      min-width: 130px !important;
    }
    /deep/.el-col-5 {
      width: 22.83333% !important;
    }
    /deep/.jvs-form-item {
      > span {
        width: 100%;
      }
      .jvs-color-picker-show-box {
        width: 100% !important;
        min-width: 136px;
      }
    }
    .form-column-tableForm {
      margin-top: 8px;
      padding: 16px;
      border-radius: 4px;
      background: #f5f6f7;
      overflow: hidden;
      /deep/.table-form {
        .jvs-table {
          background: transparent;
        }
        .table-body-box {
          background: transparent;
          .el-table {
            background: transparent;
            &::before {
              visibility: hidden;
            }
            .el-table__header-wrapper {
              border: 0;
              .el-table__header {
                .headerclass {
                  th {
                    background: #f5f6f7;
                    height: 20px;
                    padding: 0;
                    line-height: 20px;
                    .cell {
                      font-family: Source Han Sans-Regular, Source Han Sans;
                      font-weight: 400;
                      font-size: 14px;
                      color: #363b4c;
                      text-align: left;
                      padding: 0;
                      padding-right: 16px;
                    }
                  }
                }
              }
            }
            .el-table__body-wrapper {
              min-height: unset;
              .el-table__body {
                tr {
                  background: transparent;
                  td {
                    padding: 0;
                    padding-top: 8px;
                    height: 32px;
                    line-height: 32px;
                    border: 0;
                    .cell {
                      padding: 0;
                      padding-right: 16px;
                      .el-form-item__content {
                        min-height: unset;
                        line-height: 32px;
                        .el-input {
                          height: 32px;
                          .el-input__inner {
                            height: 32px;
                            line-height: 32px;
                            background: #fff;
                          }
                        }
                        .jvs-color-picker-show-box {
                          background: #fff;
                        }
                      }
                    }
                    &.table-index-column {
                      .cell {
                        text-indent: 10px;
                      }
                    }
                    &:nth-last-of-type(1) {
                      .cell {
                        padding-right: 0;
                        text-align: center;
                      }
                    }
                  }
                  &:hover {
                    td {
                      background: none;
                    }
                  }
                }
              }
              .el-table__empty-block {
                display: none;
              }
            }
          }
        }
      }
    }
  }
}
/deep/ .jvs-color-picker-show-box .show-color-box-wrap .show-color-hex {
  margin-left: 0;
}
</style>
<style lang="scss">
.design-box {
  position: relative;
  height: 100%;
  width: 100%;
  display: flex;
  .design-page-left {
    // width: 280px;
  }
  .design-page-center {
    flex: 1;
    overflow: hidden;
    background: #f5f6f7;
    display: flex;
    flex-direction: column;
    .tool-box {
      .design-tool {
        background: #fff;
        height: 52px;
      }
      .table-info-form-left-bottom {
        position: absolute;
        right: 0;
        height: 48px;
        box-sizing: border-box;
        display: flex;
        align-items: center;
        justify-content: space-between;
        .table-info-form-left-bottom-item {
          display: flex;
          align-items: center;
          height: 32px;
          margin-left: 24px;
          .label {
            font-family: Microsoft YaHei-Regular, Microsoft YaHei;
            font-weight: 400;
            font-size: 14px;
            color: #363b4c;
            margin-right: 8px;
            word-break: keep-all;
          }
          .number-content {
            display: flex;
            width: 120px;
            .el-input-number {
              flex: 1;
              .el-input__inner {
                height: 32px;
                line-height: 32px;
                border: 0;
                background-color: #f5f6f7;
                border-radius: 4px 0 0 4px;
                text-align: left;
              }
            }
            .suffix-unit {
              display: inline-block;
              width: 32px;
              height: 32px;
              line-height: 32px;
              text-align: center;
              background-color: #f5f6f7;
              border-radius: 0 4px 4px 0;
              font-family: Microsoft YaHei-Regular, Microsoft YaHei;
              font-weight: 400;
              font-size: 14px;
              color: #6f7588;
            }
          }
        }
      }
    }
    .preview-box {
      background: #fff;
      margin: 16px 18px 16px 24px;
      margin-bottom: 0;
      padding: 0 24px;
      padding-top: 16px;
      border-radius: 6px;
      min-height: 210px;
      box-sizing: border-box;
      .jvs-table {
        .table-body-box {
          .jvs-fixed-column-table .el-table__fixed-right {
            right: 0;
          }
          .el-table {
            .el-table__row {
              .el-table__cell {
                .cell {
                  img {
                    width: 30px !important;
                    height: 30px !important;
                  }
                }
              }
            }
          }
          .jvs-table-body-slot {
            .table-body-slot-box {
              .table-body-slot-box-item {
                height: unset;
                margin-bottom: 0;
                .card-top-head {
                  .table-body-slot-box-item-row-title {
                    img {
                      width: 30px;
                      height: 30px;
                    }
                  }
                }
              }
            }
          }
        }
        .tablepagination {
          padding: 10px 0;
        }
      }
      .jvs-table-leftTree {
        .close-tree-tool {
          visibility: visible;
        }
      }
      .jvstable-left-tree {
        .el-tree {
          .el-tree__empty-block {
            &::before {
              top: calc(100% - 30px);
              height: 100%;
              background-size: 100% 100%;
            }
          }
        }
      }
    }
    .deign-box-content {
      background: #fff;
      margin: 16px 18px 16px 24px;
      border-radius: 6px;
      flex: 1;
      overflow: hidden;
      .table-form-slot-box {
        height: 100%;
        box-sizing: border-box;
      }
    }
  }
  .design-page-right {
    width: 320px;
    height: 100%;
    overflow: hidden;
    .el-tabs {
      height: 100%;
      .el-tabs__header {
        margin: 0;
        box-sizing: border-box;
      }
      .el-tabs__content {
        height: calc(100% - 53px);
        overflow: hidden;
        .el-tab-pane {
          height: 100%;
          overflow: hidden;
          > .el-form {
            height: 100%;
          }
        }
      }
      .el-tabs__nav-wrap {
        border-left: 1px solid #eeeff0;
        padding-left: 24px;
        box-sizing: border-box;
        .el-tabs__active-bar {
          height: 3px;
          background: #1e6fff;
          border-radius: 2px 0px 2px 0px;
        }
        .el-tabs__item {
          height: 53px;
          line-height: 53px;
          padding-right: 0 16px;
          font-family: Microsoft YaHei-Regular, Microsoft YaHei;
          font-weight: 400;
          font-size: 14px;
          color: #6f7588;
          &:nth-child(2) {
            padding-left: 0;
          }
          &.is-active {
            font-family: Microsoft YaHei-Bold, Microsoft YaHei;
            font-weight: 700;
            font-size: 14px;
            color: #1e6fff;
          }
        }
        &::after {
          background-color: #eeeff0;
          height: 1px;
        }
      }
    }
    .sort-param-box {
      max-height: 100%;
      overflow: hidden;
      overflow-y: auto;
      background: #f5f6f7;
      .sort-param-item {
        background: #fff;
        padding-top: 16px;
        .sort-param-title {
          margin: 0 16px;
          font-family: Microsoft YaHei-Regular, Microsoft YaHei;
          font-weight: 400;
          font-size: 14px;
          color: #363b4c;
        }
        & + .sort-param-item {
          margin-top: 16px;
        }
      }
    }
  }
  .design-box-top {
    position: absolute;
    top: 17px;
    right: 20px;
    width: 50%;
    display: flex;
    justify-content: flex-end;
    z-index: 1;
  }
}
.jsql-codeEditor {
  width: 100%;
  height: 500px;
  position: relative;
}
.json-show-dialog {
  .el-dialog.is-fullscreen {
    .el-dialog__body {
      width: 100%;
      padding: 20px 10px;
      padding-top: 0;
      position: unset;
      box-sizing: border-box;
      .row {
        display: flex;
        justify-content: space-between;
        > div {
          width: 49%;
        }
      }
    }
  }
}
</style>
