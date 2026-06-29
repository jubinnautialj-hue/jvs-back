<template>
  <!-- 挂载表单 -->
  <div class="form-show-info">
    <!-- 无权限 -->
    <div v-if="!permission" class="permission">
      <img src="@/const/img/permission.png" alt="" />
      <span>暂无访问权限</span>
    </div>
    <!-- 空设计 -->
    <div v-if="emptyView" class="empty-view">
      <img src="/jvs-ui-public/img/contentEmpty.png" alt="" />
      <div>暂无内容</div>
    </div>
    <!-- 标签 -->
    <el-row
      v-if="permission && !emptyView && (tagSetting || jvsQueryData.id)"
      class="tag-tool-bar"
      style="position: absolute"
    >
      <el-popover
        placement="bottom"
        trigger="hover"
        content="点击复制分享地址"
        popper-class="custom-right-tool-poper"
      >
        <span
          slot="reference"
          class="log-tag-span-box"
          @click.stop="
            copyHandle(
              `${locationOrigin}/page-design-ui/#/form/use?id=${
                jvsQueryData.id
              }&dataModelId=${jvsQueryData.dataModelId}&jvsAppId=${
                jvsQueryData.jvsAppId
              }${
                jvsQueryData.dataId && jvsQueryData.pageId
                  ? '&dataId=' +
                    jvsQueryData.dataId +
                    '&pageId=' +
                    jvsQueryData.pageId
                  : ''
              }`
            )
          "
        >
          <svg class="icon">
            <use xlink:href="#icon-jvs-fenxiang"></use>
          </svg>
        </span>
      </el-popover>
      <el-popover
        v-if="tagSetting"
        v-model="tagshow"
        placement="bottom"
        trigger="click"
      >
        <div v-show="tagshow" class="tag-set-info-box">
          <h3>标签</h3>
          <div id="formTag" style="overflow: hidden; width: 380px">
            <h4
              style="
                background: #409eff;
                color: #fff;
                font-size: 18px;
                margin: 0;
                padding: 15px 10px;
                font-weight: normal;
                border-radius: 4px 4px 0 0;
              "
            >
              <span>{{ tagSetting.title.text || "主标题" }}</span>
              <br />
              <span style="font-size: 12px">{{
                tagSetting.subTitle.text || ""
              }}</span>
            </h4>
            <div
              style="
                display: flex;
                justify-content: space-between;
                padding: 10px 15px;
                background: #f5f7fa;
                border-radius: 0 0 4px 4px;
                border-top: 0;
                overflow: hidden;
                box-sizing: content-box;
              "
            >
              <div style="font-size: 15px">
                <p
                  v-for="(fi, fix) in tagSetting.fieldList"
                  :key="'tag-field-item-' + fix"
                >
                  <b style="color: #333">{{ fi.text }}</b>
                  <span
                    v-if="fi.text && fi.businessId && tagDataTransform"
                    style="color: #666"
                    >{{ ":" + fi.businessId }}</span
                  >
                </p>
              </div>
              <div style="text-align: right">
                <span
                  id="tagQRcode"
                  ref="tagQRcode"
                  style="display: block; width: 130px; height: 130px"
                ></span>
                <!-- <span style="color:#c0c4cc;">created by jvs</span> -->
              </div>
            </div>
          </div>
          <div
            style="
              width: 100%;
              display: flex;
              justify-content: flex-end;
              align-items: center;
              margin-top: 10px;
            "
          >
            <el-button size="mini" type="text" @click="downloadTag"
              >下载图片</el-button
            >
          </div>
        </div>
        <span slot="reference" class="log-tag-span-box">
          <svg aria-hidden="true" @click.stop="getTagDetail">
            <use xlink:href="#jvs-ui-icon-changguikapian"></use>
          </svg>
        </span>
      </el-popover>
    </el-row>
    <div
      v-if="data.viewJson && permission"
      :style="alonePage ? 'padding: 15px 10px;' : ''"
    >
      <jvs-form
        ref="ruleForm"
        class="show-form"
        v-if="
          (formType == 'normalForm' || formType == 'detailForm') && showForm
        "
        :key="formKey"
        :option="formOption"
        :formData="formData"
        :designId="jvsQueryData.id || ''"
        :dataModelId="jvsQueryData.dataModelId"
        :execsList="execsList"
        :jvsAppId="jvsQueryData.jvsAppId"
        :associationSettingsFields="associationSettingsFields"
        @submit="formSubmit"
        @cancalClick="cancelClick"
      >
        <!-- 自定义按钮 -->
        <template slot="formButton">
          <span
            v-if="
              jvsQueryData.pageId &&
              hasPrint &&
              printTemplateList &&
              printTemplateList.length > 0
            "
            style="margin: 0 10px"
          >
            <el-popover
              v-if="printTemplateList.length > 1"
              placement="right"
              width="200"
              trigger="hover"
            >
              <div class="print-list-items">
                <ul>
                  <li
                    v-for="pi in printTemplateList"
                    :key="pi.id"
                    @click="printHandle(pi)"
                  >
                    <span>{{ pi.name }}</span>
                  </li>
                </ul>
              </div>
              <jvs-button v-if="hasPrint" slot="reference" size="mini"
                >打印</jvs-button
              >
            </el-popover>
            <jvs-button
              v-if="hasPrint && printTemplateList.length == 1"
              slot="reference"
              size="mini"
              @click="printHandle(printTemplateList[0])"
              >打印</jvs-button
            >
          </span>
          <jvs-button
            size="mini"
            v-for="(item, index) in formOption.btnSetting"
            v-if="
              ['submit', 'empty', 'print', 'cancel'].indexOf(item.buttonType) ==
                -1 && item.enable
            "
            :key="item.name + 'slotbtn' + index"
            :loading="item.loading"
            @click="slotbtnClickHandle(item, index)"
            >{{ item.name }}</jvs-button
          >
        </template>
      </jvs-form>
      <jvs-form-level
        v-if="formType == 'multiLevelForm'"
        :option="formOption"
        :formData="formData"
        @submit="formSubmit"
      >
      </jvs-form-level>
      <!-- 流程表单 -->
      <div v-if="formType == 'flowable'">
        <jvs-form
          class="show-form"
          :option="basicOption"
          @submit="formSubmit"
          :formData="formData.basic"
          :designId="jvsQueryData.id || ''"
          @formChange="formChange"
        >
          <!-- 自定义按钮 -->
          <template slot="formButton" v-if="basicOption.flag">
            <jvs-button
              size="mini"
              v-for="(item, index) in basicOption.btnSetting"
              :key="item.name + 'slotbtn' + index"
              :loading="item.loading"
              @click="slotbtnClickHandle(item, index)"
              >{{ item.name }}</jvs-button
            >
          </template>
        </jvs-form>
        <jvs-form
          class="show-form"
          :option="formOption"
          @submit="formSubmit"
          :formData="formData.form"
          :designId="jvsQueryData.id || ''"
          @formChange="formChange"
        >
          <!-- 自定义按钮 -->
          <template slot="formButton" v-if="formOption.flag">
            <jvs-button
              size="mini"
              v-for="(item, index) in formOption.btnSetting"
              :key="item.name + 'slotbtn' + index"
              :loading="item.loading"
              @click="slotbtnClickHandle(item, index)"
              >{{ item.name }}</jvs-button
            >
          </template>
        </jvs-form>
      </div>
    </div>
    <div
      v-if="permission && !(data && data.viewJson)"
      class="form-show-info-loading"
    ></div>
    <div v-if="afterSubmit" class="submit-after-dialog">
      <div class="submit-after-dialog-body">
        <p><i class="el-icon-success"></i></p>
        <h4>提交成功</h4>
        <p><span>数据已提交并保存</span></p>
        <el-row>
          <el-button size="mini" type="primary" @click="reloadForm"
            >继续提交</el-button
          >
          <el-button size="mini" @click="reloadForm">关闭</el-button>
        </el-row>
      </div>
    </div>
    <el-dialog
      :title="assType == 'zhipai' ? '指派' : '委派'"
      v-if="dialogVisible"
      :visible.sync="dialogVisible"
      :close-on-click-modal="false"
      :before-close="handleClose"
    >
      <div class="ass-box-form">
        <userForm :form="userForm" prop="userId" :enableinput="false" />
      </div>
      <el-row
        style="
          display: flex;
          justify-content: center;
          align-items: center;
          margin-top: 20px;
        "
      >
        <jvs-button size="mini" type="primary" @click="assSubmit"
          >提交</jvs-button
        >
        <jvs-button size="mini" @click="handleClose">取消</jvs-button>
      </el-row>
    </el-dialog>
  </div>
</template>
<script>
import detailForm from "../../components/basic-design/detailForm";
import normalForm from "../../components/basic-design/normalForm";
import levelForm from "../../components/basic-design/levelForm";
import stepForm from "../../components/basic-design/stepForm";
import { getFormInfo, getFormTagData } from "../../api/formlist";
import {
  getFlowableForm,
  startProcess,
  completeProcess,
  zhipaiProcess,
  weipaiProcess,
} from "@/api/flowable";
import userForm from "../../plugin/userForm";
import { sendMyRequire, getKeyValue } from "../../api/list";
import { ruleRun, ruleDownLoad } from "@/api/common";
import { association } from "../../api/newDesign";
import { getUserInfo } from "@/api/admin/user";
import { getDefaultData } from "@/views/page/util/common";
import { getAvailableTemplate } from "@/views/print/api/index";
import { getSingleData } from "../../api/design";
import { Base64 } from "js-base64";
import { getRedirectInfo } from "@/util/admin";
export default {
  name: "form-show-info",
  components: { detailForm, normalForm, levelForm, stepForm, userForm },
  props: {
    routerQuery: {
      type: Object,
    },
  },
  data() {
    return {
      jvsQueryData: {}, // 菜单组件入参
      permission: true,
      name: "",
      data: {
        name: "",
        formType: "",
        id: "",
        viewJson: null,
      },
      // 表单
      formType: "",
      formData: {},
      selectFormItems: [], // 表单里的下拉选择项
      basicOption: {
        disabled: true,
        btnHide: true,
        column: [],
      },
      formOption: {
        submitBtn: false,
        emptyBtn: false,
        column: [],
      },
      flowableInfo: {},
      isFlowable: false,
      processName: "",
      submtData: {},
      userForm: {
        userId: "",
      }, // 用户表单
      assType: "",
      dialogVisible: false,
      labelValue: null,
      showForm: false,
      pathQuery: {},
      hasPrint: false,
      afterSubmit: false,
      formKey: "formUse",
      userInfo: {},
      printTemplateList: [],
      execsList: [], // 触发公式的组件
      tagSetting: null, // 标签设置
      tagshow: false,
      tagDataTransform: false,
      associationSettingsFields: [],
      emptyView: false,
      alonePage: false,
      locationOrigin: location.origin,
    };
  },
  async created() {
    let redirecInfo = {
      stop: false,
      url: "",
    };
    if (location.pathname == "/page-design-ui/") {
      this.alonePage = true;
      await getRedirectInfo().then((res) => {
        redirecInfo = res;
      });
    }
    if (this.alonePage && redirecInfo.stop && redirecInfo.url) {
      this.$openUrl(redirecInfo.url, "_self");
      return false;
    }
    this.createHandle();
    if (this.$store.getters && this.$store.getters.userInfo) {
      this.userInfo = JSON.parse(JSON.stringify(this.$store.getters.userInfo));
    } else {
      this.getUserInfo();
    }
    if (this.$store.getters && this.$store.getters.labelValue) {
      this.labelValue = this.$store.getters.labelValue;
    } else {
      await this.getKeyValueHandle();
    }
    if (this.jvsQueryData && this.jvsQueryData.id) {
      this.id = this.jvsQueryData.id;
      this.pathQuery = JSON.parse(JSON.stringify(this.jvsQueryData));
      delete this.pathQuery["name"];
    } else {
      this.jvsQueryData = JSON.parse(JSON.stringify(this.$route.query));
      this.id = this.jvsQueryData.id;
    }
    // 行数据回显
    if (this.jvsQueryData.dataId && this.jvsQueryData.pageId) {
      await this.getFormData();
    }
    getFormInfo(this.jvsQueryData.jvsAppId, this.id)
      .then((res) => {
        if (res.data.code == 0) {
          if (res.data.data.tagSetting && res.data.data.tagSetting.openTag) {
            this.tagSetting = res.data.data.tagSetting;
          }
          this.data = JSON.parse(JSON.stringify(res.data.data));
          if (res.data.data.viewJson) {
            this.data.viewJson = JSON.parse(res.data.data.viewJson);
            if (this.data.viewJson.formdata) {
              this.initForm(this.data.viewJson);
            } else {
              this.emptyView = true;
            }
            if (this.data.viewJson.execs) {
              this.execsList = this.data.viewJson.execs;
            }
          } else {
            this.emptyView = true;
          }
          if (this.data.associationSettingsFields) {
            this.associationSettingsFields =
              this.data.associationSettingsFields;
          }
          // 挂载的表单
          if (!this.data.isDeploy) {
            // this.$message.warning('该设计未发布')
            this.$notify({
              title: "提示",
              message: "该设计未发布",
              position: "bottom-right",
              type: "warning",
            });
          }
        }
      })
      .catch((e) => {
        this.permission = false;
      });

    // 流程表单
    if (this.jvsQueryData) {
      if (this.jvsQueryData.modelId) {
        this.flowableInfo.modelId = this.jvsQueryData.modelId;
      }
      if (this.jvsQueryData.actId) {
        this.flowableInfo.actId = this.jvsQueryData.actId;
      }
      if (this.jvsQueryData.taskId) {
        this.flowableInfo.taskId = this.jvsQueryData.taskId;
      }
      if (this.jvsQueryData.assignId) {
        this.flowableInfo.assignId = this.jvsQueryData.assignId;
      }
      if (this.flowableInfo.modelId && this.flowableInfo.actId) {
        this.getFlowableFormHandle();
      }
    }
  },
  methods: {
    createHandle() {
      if (this.routerQuery) {
        for (let k in this.routerQuery) {
          this.$set(this.jvsQueryData, k, this.routerQuery[k]);
        }
      }
    },
    getUserInfo() {
      getUserInfo().then((res) => {
        if (res.data.code == 0) {
          this.userInfo = res.data.data;
        }
      });
    },
    // 获取回显数据
    async getFormData() {
      await getSingleData(
        this.jvsQueryData.jvsAppId,
        this.jvsQueryData.dataModelId,
        this.jvsQueryData.dataId,
        this.jvsQueryData.id,
        this.jvsQueryData.pageId
      ).then((res) => {
        if (res.data && res.data.code == 0) {
          this.formData = res.data.data;
          if (
            this.formData &&
            this.formOption.formTitleKey &&
            this.formData[this.formOption.formTitleKey]
          ) {
            this.formTitle = this.formData[this.formOption.formTitleKey];
          }
          this.$forceUpdate();
        }
      });
    },
    initForm(formDesign) {
      this.getSelectItem(formDesign.formdata);
      this.formData = JSON.parse(
        JSON.stringify(
          getDefaultData(
            this.formData,
            formDesign.formdata[0].forms,
            this.userInfo
          )
        )
      );
      this.formType = formDesign.formType;
      this.getFormColumn(formDesign.formType, formDesign);
      // 表单回显
      if (
        ["normalForm", "detailForm", "flowable"].indexOf(formDesign.formType) >
        -1
      ) {
        if (
          formDesign.formdata[0].formsetting &&
          formDesign.formdata[0].formsetting.dataEchoRequest
        ) {
          ruleRun(
            this.jvsQueryData.jvsAppId,
            formDesign.formdata[0].formsetting.dataEchoRequest,
            this.jvsQueryData.dataId ? this.formData : null,
            { formDesignId: this.jvsQueryData.id }
          )
            .then((res) => {
              if (
                res &&
                res.data &&
                res.headers["output_format"] &&
                res.data.data
              ) {
                let name = res.headers["output_format"];
                if (res.data.data.originalName) {
                  name = res.data.data.originalName;
                }
                this.ruleDownLoad(name, res.data.data);
              } else if (
                res &&
                res.data &&
                res.headers["output_type"] == "preview" &&
                res.data.data
              ) {
                this.previewFile(res.data.data);
              } else {
                if (res.data && res.data.code == 0 && res.data.data) {
                  this.formData = res.data.data;
                }
                if (
                  this.formData &&
                  this.formData.jvsEnabledButtons &&
                  this.formData.jvsEnabledButtons.length > 0
                ) {
                  if (this.formOption.btnSetting) {
                    let delIndexs = [];
                    for (let i in this.formOption.btnSetting) {
                      if (
                        this.formOption.btnSetting[i].buttonType == "print" &&
                        this.formData.jvsEnabledButtons.indexOf(
                          this.formOption.btnSetting[i].permissionFlag
                        ) == -1
                      ) {
                        this.hasPrint = false;
                      } else if (
                        this.formOption.btnSetting[i].buttonType == "submit" &&
                        this.formData.jvsEnabledButtons.indexOf(
                          this.formOption.btnSetting[i].permissionFlag
                        ) == -1
                      ) {
                        this.formOption.submitBtn = false;
                      } else if (
                        this.formOption.btnSetting[i].buttonType == "empty" &&
                        this.formData.jvsEnabledButtons.indexOf(
                          this.formOption.btnSetting[i].permissionFlag
                        ) == -1
                      ) {
                        this.formOption.emptyBtn = false;
                      } else if (
                        this.formOption.btnSetting[i].buttonType == "cancel" &&
                        this.formData.jvsEnabledButtons.indexOf(
                          this.formOption.btnSetting[i].permissionFlag
                        ) == -1
                      ) {
                        this.formOption.cancal = false;
                      }
                      if (
                        this.formData.jvsEnabledButtons.indexOf(
                          this.formOption.btnSetting[i].permissionFlag
                        ) == -1
                      ) {
                        delIndexs.push(i);
                      }
                    }
                    if (delIndexs && delIndexs.length > 0) {
                      this.formOption.btnSetting =
                        this.formOption.btnSetting.filter((fit, fix) => {
                          if (delIndexs.indexOf(fix + "") == -1) {
                            return fit;
                          }
                        });
                    }
                  }
                  if (this.hasPrint) {
                    this.getAvailableTemplateHandle();
                  }
                } else {
                  this.hasPrint = false;
                  // this.formOption.btnHide = true // 逻辑引擎回显无按钮权限全放
                  // this.getAvailableTemplateHandle(item) // 用于调试。。
                }
                if (res.data.msg && res.headers["output_status"]) {
                  this.$notify({
                    title: "提示",
                    message: res.data.msg,
                    position: "bottom-right",
                    type:
                      res && res.data && res.headers["output_status"] == "false"
                        ? "error"
                        : "success",
                    duration:
                      res &&
                      res.data &&
                      res.headers["output_status"] == "false" &&
                      res.headers["message_close"] == "false"
                        ? 0
                        : 4500,
                    dangerouslyUseHTMLString: true,
                  });
                }
                this.showForm = true;
              }
            })
            .catch((e) => {
              this.showForm = true;
            });
          // let tp = null
          // tp = JSON.parse(JSON.stringify(formDesign.formdata[0].formsetting.dataEchoRequest))
          // if(this.$store.getters.labelValue && this.$store.getters.labelValue.requestContentType) {
          //   tp.requestContentType = this.$store.getters.labelValue.requestContentType[tp.requestContentType]
          // }
          // if(tp && tp.url) {
          //   this.showForm = false
          //   let tpa = {}
          //   if(tp.parameters) {
          //     tpa = JSON.parse(JSON.stringify(tp.parameters))
          //   }
          //   tpa = Object.assign(tpa, this.pathQuery)
          //   sendMyRequire(tp, tpa).then(res => {
          //     if(res.data.code == 0) {
          //       this.formData = res.data.data
          //       this.showForm = true
          //     }else{
          //       this.showForm = true
          //     }
          //   }).catch(e => {
          //     this.showForm = true
          //   })
          // }else{
          //   this.showForm = true
          // }
        } else {
          if (this.hasPrint) {
            this.getAvailableTemplateHandle();
          }
          this.showForm = true;
        }
      }
      // 多级表单
      if (formDesign.formType == "levelForm") {
        let tempData = {};
        for (let i in formDesign.formdata) {
          let itemUrl = "";
          let itemObj = {};
          if (formDesign.formdata[i].formsetting.echoUrl) {
            itemUrl = formDesign.formdata[i].formsetting.echoUrl;
          }
          if (itemUrl) {
            this.getItemForm(itemUrl, row, itemObj);
          }
          tempData[formDesign.column[i].name] = itemObj;
        }
        this.formData = tempData;
      }
      // // 详情  流程表单
      // if(formDesign.formType == 'detailForm' || formDesign.formType == 'flowable'){
      //   if(formDesign.formdata && formDesign.formdata[0].formsetting && formDesign.formdata[0].formsetting.echoUrl) {
      //     sendRequire(formDesign.formdata[0].formsetting.echoUrl, 'get', row).then(res => {
      //       if(res.data.code == 0) {
      //         this.formData = res.data.data
      //       }
      //     })
      //   }
      // }
    },
    // 表单配置
    getFormColumn(type, formDesign) {
      if (formDesign.formdata && formDesign.formdata.length > 0) {
        if (type == "normalForm" || type == "detailForm") {
          this.formOption = this.formatFormOption(
            type,
            formDesign.formdata[0].forms,
            formDesign.formdata[0].formsetting
          );
        } else if (type == "flowable") {
          this.basicOption = this.formatFormOption(
            "detailForm",
            formDesign.formdata[0].forms,
            formDesign.formdata[0].formsetting
          );
          this.formOption = this.formatFormOption(
            type,
            formDesign.formdata[1].forms,
            formDesign.formdata[1].formsetting
          );
        } else {
          let ct = [];
          for (let i in formDesign.column) {
            let obj = {
              defaultData: formDesign.column[i].defaultData,
              formOption: {},
              label: formDesign.column[i].label,
              name: formDesign.column[i].name,
              show: formDesign.column[i].show || true,
            };
            obj.formOption = {
              btnSetting: formDesign.formdata[i].formsetting.btnSetting,
              size: formDesign.formdata[i].formsetting.formsize,
              formAlign: formDesign.formdata[i].formsetting.labelposition,
              labelWidth: formDesign.formdata[i].formsetting.labelwidth + "",
              column: formDesign.formdata[i].forms,
            };
            ct.push(obj);
          }
          this.formOption = {
            type: "card",
            column: ct,
            formdata: formDesign.formdata,
          };
        }
      } else {
      }
    },
    // 格式化表单配置项
    formatFormOption(type, forms, formsetting) {
      formsetting.submitBtn = false;
      formsetting.emptyBtn = false;
      const arr = formsetting.btnSetting.filter((item) => {
        if (item.buttonType !== "custom" && item.buttonType === "submit") {
          formsetting.submitBtn = true;
          formsetting.submitBtnText = item.name;
          return item;
        }
        if (item.buttonType !== "custom" && item.buttonType === "empty") {
          formsetting.emptyBtn = true;
          formsetting.emptyBtnText = item.name;
          return item;
        }
        if (item.buttonType !== "custom" && item.buttonType === "cancel") {
          formsetting.cancal = true;
          formsetting.cancalBtnText = item.name;
          return item;
        }
        if (item.buttonType == "print") {
          this.hasPrint = true;
        }
        return item.buttonType === "custom";
      });
      if (this.jvsQueryData.pageId) {
        if (
          this.formData &&
          this.formData.jvsEnabledButtons &&
          this.formData.jvsEnabledButtons.length > 0
        ) {
          if (formsetting.btnSetting) {
            for (let i in formsetting.btnSetting) {
              if (
                formsetting.btnSetting[i].buttonType == "print" &&
                this.formData.jvsEnabledButtons.indexOf(
                  formsetting.btnSetting[i].permissionFlag
                ) == -1
              ) {
                this.hasPrint = false;
              } else if (
                formsetting.btnSetting[i].buttonType == "submit" &&
                this.formData.jvsEnabledButtons.indexOf(
                  formsetting.btnSetting[i].permissionFlag
                ) == -1
              ) {
                formsetting.submitBtn = false;
              } else if (
                formsetting.btnSetting[i].buttonType == "empty" &&
                this.formData.jvsEnabledButtons.indexOf(
                  formsetting.btnSetting[i].permissionFlag
                ) == -1
              ) {
                formsetting.emptyBtn = false;
              } else {
                if (
                  this.formData.jvsEnabledButtons.indexOf(
                    formsetting.btnSetting[i].permissionFlag
                  ) == -1
                ) {
                  formsetting.btnSetting.splice(i, 1);
                }
              }
            }
          }
          if (arr && arr.length > 0) {
            for (let i in arr) {
              if (
                this.formData.jvsEnabledButtons.indexOf(
                  arr[i].permissionFlag
                ) == -1
              ) {
                arr.splice(i, 1);
              }
            }
          }
          if (this.hasPrint) {
            this.getAvailableTemplateHandle();
          }
        } else {
          this.formOption.btnHide = true;
        }
      }
      // if (formsetting.emptyBtn !== false)
      let temp = {
        column: JSON.parse(JSON.stringify(forms)),
        // btnSetting: formsetting.btnSetting,
        btnSetting: arr,
        size: formsetting.formsize,
        formAlign: formsetting.labelposition,
        labelWidth: formsetting.labelwidth + "px",
        fullscreen: formsetting.fullscreen,
        flag: formsetting.flag,
        submitBtn: formsetting.submitBtn,
        emptyBtn: formsetting.emptyBtn,
        submitBtnText: formsetting.submitBtnText,
        emptyBtnText: formsetting.emptyBtnText,
        cancal: formsetting.cancal,
        cancalBtnText: formsetting.cancalBtnText,
      };
      if (type == "detailForm") {
        temp.disabled = true;
        temp.btnHide = true;
      } else if (type == "flowable") {
        temp.submitBtn = false;
        temp.emptyBtn = false;
        temp.cancal = false;
      } else {
        temp.disabled = false;
        temp.btnHide = false;
        if (!this.jvsQueryData.pageId) {
          temp.cancal = false;
        }
      }
      return temp;
    },
    // 获取select项，表单值为数组
    getSelectItem(list) {
      let temp = [];
      for (let i in list) {
        for (let j in list[i].forms) {
          if (list[i].forms[j].type == "select") {
            temp.push(list[i].forms[j].prop);
          }
          if (list[i].forms[j].type == "SWITCH") {
            list[i].forms[j].type = "switch";
          }
        }
      }
      this.selectFormItems = temp;
    },
    // 表单提交
    formSubmit(formsdata) {
      const designId = this.jvsQueryData.id;
      const dataModelId = this.jvsQueryData.dataModelId;
      let form = null; // 表单数据
      // 工作流表单提交
      if (this.isFlowable) {
        // 启动流程
        if (this.formType == "normalForm") {
          let obj = {
            params: formsdata,
            modelId: this.flowableInfo.modelId,
          };
          startProcess(obj).then((res) => {
            if (res.data.code == 0) {
              // this.$message.success('流程启动成功')
              this.afterSubmit = true;
            }
          });
        }
      } else {
        let tp = null;
        let http = {};
        if (this.jvsQueryData.pageId) {
          http = {
            httpMethod: "POST",
            requestContentType: "application/json",
            responseContentType: "JSON",
            url: this.jvsQueryData.dataId
              ? `/mgr/jvs-design/app/use/${this.jvsQueryData.jvsAppId}/dynamic/data/update/${dataModelId}/${this.jvsQueryData.dataId}`
              : `/mgr/jvs-design/app/use/${this.jvsQueryData.jvsAppId}/dynamic/data/save/${dataModelId}`,
            headers: {
              designId: designId,
              operator: encodeURI("提交"),
              permissionFlag: this.jvsQueryData.permissionFlag,
            },
          };
        } else {
          http = {
            httpMethod: "POST",
            requestContentType: "application/json",
            responseContentType: "JSON",
            url: dataModelId
              ? `/mgr/jvs-design/app/use/${this.jvsQueryData.jvsAppId}/dynamic/data/save/${dataModelId}`
              : `/mgr/jvs-design/app/use/${this.jvsQueryData.jvsAppId}/dynamic/data/save`,
            headers: {
              designId: designId,
              operator: encodeURI("提交"),
            },
          };
        }
        if (this.data.viewJson.formdata[0].formsetting) {
          // tp = JSON.parse(JSON.stringify(this.data.viewJson.formdata[0].formsetting.dataSubmissionRequest))
          tp = JSON.parse(JSON.stringify(http));
          if (
            this.$store.getters.labelValue &&
            this.$store.getters.labelValue.requestContentType
          ) {
            tp.requestContentType =
              this.$store.getters.labelValue.requestContentType[
                tp.requestContentType
              ];
          }
        }
        // 普通表单
        if (this.formType == "normalForm") {
          form = formsdata;
        }
        // 多级表单
        else {
          form = formsdata.form;
        }
        let tob = JSON.parse(JSON.stringify(form));
        tob = Object.assign(tob, tp.parameters ? tp.parameters : {});
        let needAssocia = false;
        if (
          this.data.viewJson.formdata[0].formsetting &&
          this.data.viewJson.formdata[0].formsetting.btnSetting
        ) {
          let bts = this.data.viewJson.formdata[0].formsetting.btnSetting;
          if (
            bts[0] &&
            bts[0].enable &&
            bts[0].association &&
            bts[0].association.length > 0
          ) {
            needAssocia = true;
          }
        }
        if (needAssocia) {
          let bts = this.data.viewJson.formdata[0].formsetting.btnSetting;
          association(
            this.data.jvsAppId,
            this.data.dataModelId,
            this.data.id,
            bts[0].permissionFlag,
            tob,
            encodeURI(bts[0].name)
          ).then((res) => {
            if (res.data.code == 0) {
              if (this.jvsQueryData.pageId) {
                let msg = "";
                switch (this.jvsQueryData.buttonType) {
                  case "btn_add":
                    msg = "新增成功";
                    break;
                  case "btn_modify":
                    msg = "修改成功";
                    break;
                  case "btn_form":
                    msg = "";
                    break;
                  default:
                    break;
                }
                if (msg) {
                  this.$notify({
                    title: "提示",
                    message: msg,
                    position: "bottom-right",
                    type: "success",
                  });
                } else {
                  if (res.data.msg) {
                    this.$notify({
                      title: "提示",
                      message: res.data.msg,
                      position: "bottom-right",
                      type: "success",
                    });
                  }
                }
                window.postMessage(
                  { command: "freshPage", freshId: this.jvsQueryData.pageId },
                  "*"
                );
                window.postMessage(
                  {
                    command: "closeTab",
                    id: this.jvsQueryData.id,
                    from: this.jvsQueryData.from,
                  },
                  "*"
                );
              } else {
                this.afterSubmit = true;
              }
            }
          });
        } else {
          if (tp && tp.url) {
            sendMyRequire(tp, tob).then((res) => {
              if (res.data.code == 0) {
                if (this.jvsQueryData.pageId) {
                  let msg = "";
                  switch (this.jvsQueryData.buttonType) {
                    case "btn_add":
                      msg = "新增成功";
                      break;
                    case "btn_modify":
                      msg = "修改成功";
                      break;
                    case "btn_form":
                      msg = "";
                      break;
                    default:
                      break;
                  }
                  if (msg) {
                    this.$notify({
                      title: "提示",
                      message: msg,
                      position: "bottom-right",
                      type: "success",
                    });
                  } else {
                    if (res.data.msg) {
                      this.$notify({
                        title: "提示",
                        message: res.data.msg,
                        position: "bottom-right",
                        type: "success",
                      });
                    }
                  }
                  window.postMessage(
                    { command: "freshPage", freshId: this.jvsQueryData.pageId },
                    "*"
                  );
                  window.postMessage(
                    {
                      command: "closeTab",
                      id: this.jvsQueryData.id,
                      from: this.jvsQueryData.from,
                    },
                    "*"
                  );
                } else {
                  this.afterSubmit = true;
                }
              }
            });
          }
        }
      }
    },
    // 自定义按钮事件
    slotbtnClickHandle(row, index) {
      if (!row.loading) {
        this.$set(row, "loading", false);
      }
      let validate = true;
      if (row.validateable) {
        validate = this.$refs.ruleForm.validateForm();
      }
      if (!validate) {
        return false;
      }
      // 流程提交
      if (this.isFlowable) {
        if (index < 3) {
          let obj = {
            params: this.submtData,
            remark: this.submtData.remark,
            result: row.url,
          };
          row.loading = true;
          completeProcess(this.flowableInfo.taskId, obj)
            .then((res) => {
              if (res.data.code == 0) {
                row.loading = false;
                this.$notify({
                  title: "提示",
                  message: row.name + "成功",
                  position: "bottom-right",
                  type: "success",
                });
              }
            })
            .catch((e) => {
              row.loading = false;
            });
        } else if (index > 2 && index < 6) {
          let obj = {
            params: this.submtData,
            remark: this.submtData.remark,
          };
          if (row.url == "save") {
            row.loading = true;
            saveProcess(obj)
              .then((res) => {
                if (res.data.code == 0) {
                  row.loading = false;
                  this.$notify({
                    title: "提示",
                    message: "保存成功",
                    position: "bottom-right",
                    type: "success",
                  });
                }
              })
              .catch((e) => {
                row.loading = false;
              });
          }
          if (row.url == "zhipai") {
            this.dialogVisible = true;
            this.assType = "zhipai";
          }
          if (row.url == "weipai") {
            this.dialogVisible = true;
            this.assType = "weipai";
          }
        }
      } else {
        for (let k in this.formData) {
          if (this.formData[k] && this.formData[k] instanceof Array) {
            for (let n in this.formData[k]) {
              if (
                this.formData[k][n] === null ||
                this.formData[k][n] === undefined
              ) {
                this.formData[k].splice(n, 1);
              }
            }
          }
        }
        if (row.association && row.association.length > 0) {
          row.loading = true;
          association(
            this.data.jvsAppId,
            this.data.dataModelId,
            this.data.id,
            row.permissionFlag,
            this.formData,
            encodeURI(row.name)
          )
            .then((res) => {
              if (res.data.code == 0) {
                row.loading = false;
                if (res.data.msg) {
                  this.$notify({
                    title: "提示",
                    message: res.data.msg,
                    position: "bottom-right",
                    type: "success",
                  });
                }
                if (this.jvsQueryData.pageId) {
                  window.postMessage(
                    { command: "freshPage", freshId: this.jvsQueryData.pageId },
                    "*"
                  );
                  if (row.closeable !== false) {
                    window.postMessage(
                      {
                        command: "closeTab",
                        id: this.jvsQueryData.id,
                        from: this.jvsQueryData.from,
                      },
                      "*"
                    );
                  }
                }
              }
            })
            .catch((e) => {
              row.loading = false;
            });
        } else {
          if (row && row.secret) {
            let rdata = JSON.parse(JSON.stringify(this.formData));
            rdata.dataModelId = this.data.dataModelId;
            row.loading = true;
            ruleRun(this.jvsQueryData.jvsAppId, row.secret, rdata, {
              designId: this.data.id,
            })
              .then((res) => {
                row.loading = false;
                if (
                  res &&
                  res.data &&
                  res.headers["output_format"] &&
                  res.data.data
                ) {
                  let name = res.headers["output_format"];
                  if (res.data.data.originalName) {
                    name = res.data.data.originalName;
                  }
                  this.ruleDownLoad(name, res.data.data);
                } else if (
                  res &&
                  res.data &&
                  res.headers["output_type"] == "preview" &&
                  res.data.data
                ) {
                  this.previewFile(res.data.data);
                } else {
                  if (res.data.code == 0) {
                    if (res.data.msg) {
                      this.$notify({
                        title: "提示",
                        message: res.data.msg,
                        position: "bottom-right",
                        type:
                          res &&
                          res.data &&
                          res.headers["output_status"] == "false"
                            ? "error"
                            : "success",
                        duration:
                          res &&
                          res.data &&
                          res.headers["output_status"] == "false" &&
                          res.headers["message_close"] == "false"
                            ? 0
                            : 4500,
                        dangerouslyUseHTMLString: true,
                      });
                    }
                    if (res.data.data) {
                      for (let k in res.data.data) {
                        this.$set(this.formData, k, res.data.data[k]);
                      }
                    }
                    if (
                      res &&
                      res.data &&
                      res.headers["output_status"] == "false"
                    ) {
                      return false;
                    }
                    if (this.jvsQueryData.pageId) {
                      window.postMessage(
                        {
                          command: "freshPage",
                          freshId: this.jvsQueryData.pageId,
                        },
                        "*"
                      );
                      if (row.closeable !== false) {
                        window.postMessage(
                          {
                            command: "closeTab",
                            id: this.jvsQueryData.id,
                            from: this.jvsQueryData.from,
                          },
                          "*"
                        );
                      }
                    } else {
                      if (row.closeable !== true) {
                        this.afterSubmit = true;
                      }
                    }
                  }
                }
              })
              .catch((e) => {
                row.loading = false;
              });
          } else {
            if (this.jvsQueryData.pageId) {
              window.postMessage(
                { command: "freshPage", freshId: this.jvsQueryData.pageId },
                "*"
              );
              if (row.closeable !== false) {
                window.postMessage(
                  {
                    command: "closeTab",
                    id: this.jvsQueryData.id,
                    from: this.jvsQueryData.from,
                  },
                  "*"
                );
              }
            }
          }
        }
      }
    },
    // 获取工作流表单
    getFlowableFormHandle() {
      let obj = {
        actId: this.flowableInfo.actId,
        modelId: this.flowableInfo.modelId,
      };
      getFlowableForm(obj).then((res) => {
        if (res.data.code == 0) {
          this.processName = res.data.data.name;
          if (res.data.data.viewJson) {
            let view = JSON.parse(res.data.data.viewJson);
            this.data.viewJson = view;
            this.initForm(this.data.viewJson);
          }
          if (res.data.data.type == "flowable") {
            this.isFlowable = true;
          }
        }
      });
    },
    // 表单change
    formChange(form) {
      this.submtData = form;
    },
    // 关闭 指派 委派
    handleClose() {
      this.dialogVisible = false;
      this.userForm = {
        userId: "",
      };
    },
    assSubmit() {
      if (this.assType == "zhipai") {
        zhipaiProcess(this.flowableInfo.taskId, this.userForm.userId).then(
          (res) => {
            if (res.data.code == 0) {
              // this.$message.success('指派成功')
              this.$notify({
                title: "提示",
                message: "指派成功",
                position: "bottom-right",
                type: "success",
              });
              this.handleClose();
            }
          }
        );
      } else {
        weipaiProcess(this.flowableInfo.taskId, this.userForm.userId).then(
          (res) => {
            if (res.data.code == 0) {
              // this.$message.success('委派成功')
              this.$notify({
                title: "提示",
                message: "委派成功",
                position: "bottom-right",
                type: "success",
              });
              this.handleClose();
            }
          }
        );
      }
    },
    // 获取所有label value 对应值
    async getKeyValueHandle() {
      await getKeyValue().then((res) => {
        if (res.data.code == 0) {
          this.labelValue = res.data.data;
          this.$store.commit("SET_LabelValue", this.labelValue);
        }
      });
    },
    // 表单获取可打印的模板列表
    getAvailableTemplateHandle() {
      getAvailableTemplate(this.data.jvsAppId, this.data.id).then((res) => {
        if (res && res.data && res.data.code == 0) {
          this.printTemplateList = res.data.data;
        }
      });
    },
    // 打印
    printHandle(row) {
      if (this.jvsQueryData.dataId) {
        if (row.designType == 1) {
          let url = `/mgr/jvs-design/app/use/${this.jvsQueryData.jvsAppId}/print/template/file/preview/${row.id}/${this.jvsQueryData.dataModelId}/${row.designId}/${this.jvsQueryData.dataId}?access_token=${this.$store.getters.access_token}`;
          if (this.formData.jvsFlowTask && this.formData.jvsFlowTask.id) {
            url += `&taskId=${this.formData.jvsFlowTask.id}`;
          }
          this.$openUrl(url, "_blank");
        } else {
          let url = `/jvs-print-ui/#/print/show?id=${row.id}&name=${row.name}&designId=${row.designId}&dataModelId=${this.jvsQueryData.dataModelId}&dataId=${this.jvsQueryData.dataId}&jvsAppId=${this.jvsQueryData.jvsAppId}`;
          if (this.formData.jvsFlowTask && this.formData.jvsFlowTask.id) {
            url += `&taskId=${this.formData.jvsFlowTask.id}`;
          }
          this.$openUrl(url, "_blank");
        }
      }
    },
    reloadForm() {
      this.afterSubmit = false;
      this.showForm = false;
      this.$set(this, "formData", {});
      this.initForm(this.data.viewJson);
      this.formKey = "formUse" + Math.random();
      this.$forceUpdate();
    },
    // 表单标签
    getTagDetail() {
      getFormTagData(
        this.jvsQueryData.jvsAppId,
        this.jvsQueryData.id,
        "formQrCodeTag",
        { params: this.formData }
      )
        .then((res) => {
          if (res.data && res.data.code == 0) {
            // console.log('公式。。。。', res.data.data)
            if (res.data.data) {
              this.$set(this, "tagSetting", res.data.data);
              this.tagDataTransform = true;
            } else {
              this.tagDataTransform = false;
            }
            this.openCloseHandle();
            this.tagshow = true;
          } else {
            this.tagshow = false;
            this.tagDataTransform = false;
          }
        })
        .catch((e) => {
          this.tagshow = false;
          this.tagDataTransform = false;
        });
    },
    // 标签二维码
    openCloseHandle() {
      if (this.$refs.tagQRcode) {
        this.$refs.tagQRcode.innerHTML = ""; //清除二维码方法一
        let loc = location.origin; // 'http://10.0.0.174:8099'
        let text = `${loc}/jvsCom/form/use?id=${this.id}&dataModelId=${this.jvsQueryData.dataModelId}&jvsAppId=${this.jvsQueryData.jvsAppId}`;
        if (this.formData.id) {
          text += `&dataId=${this.formData.id}`;
        }
        var qrcode = new QRCode(this.$refs.tagQRcode, {
          text: text, //页面地址 ,如果页面需要参数传递请注意哈希模式#
          width: 130,
          height: 130,
          colorDark: "#000000",
          colorLight: "#ffffff",
          correctLevel: QRCode.CorrectLevel.Q,
        });
      }
    },
    // 下载标签
    downloadTag() {
      html2canvas(document.querySelector("#formTag"), {
        useCORS: true, // 【重要】开启跨域配置
        scale: window.devicePixelRatio < 3 ? window.devicePixelRatio : 2,
        allowTaint: true, // 允许跨域图片
      }).then((canvas) => {
        const imgData = canvas.toDataURL("image/png", 1.0);
        let aLink = document.createElement("a");
        let blob = this.base64ToBlob(imgData);
        let evt = document.createEvent("HTMLEvents");
        evt.initEvent("click", true, true); // initEvent 不加后两个参数在FF下会报错  事件类型，是否冒泡，是否阻止浏览器的默认行为
        aLink.download = this.tagSetting.title.text || "下载标签";
        aLink.href = URL.createObjectURL(blob);
        // aLink.dispatchEvent(evt);
        aLink.click();
        document.removeChild(aLink);
        resolve(imgData);
      });
    },
    base64ToBlob(code) {
      let parts = code.split(";base64,");
      let contentType = parts[0].split(":")[1];
      let raw = window.atob(parts[1]); // 解码base64得到二进制字符串
      let rawLength = raw.length;
      let uInt8Array = new Uint8Array(rawLength); // 创建8位无符号整数值的类型化数组
      for (let i = 0; i < rawLength; ++i) {
        uInt8Array[i] = raw.charCodeAt(i); // 数组接收二进制字符串
      }
      return new Blob([uInt8Array], { type: contentType });
    },
    // 下载文件
    downloadFile(filename, content) {
      var elink = document.createElement("a");
      if (filename) {
        elink.download = filename;
      }
      elink.style.display = "none";

      var blob = new Blob([content], {
        type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8",
      }); //,{type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8'})
      elink.href = URL.createObjectURL(blob);
      document.body.appendChild(elink);
      elink.click();
      document.body.removeChild(elink);
    },
    ruleDownLoad(name, data) {
      const dotIndex = name.lastIndexOf(".");
      let downLoadName = name;
      if (dotIndex === -1 || dotIndex === name.length - 1) {
        // 没有找到点，没有后缀
        downLoadName = name + data.fileType;
      }
      ruleDownLoad(this.jvsQueryData.jvsAppId, {
        ...data,
        name: downLoadName,
      }).then((res) => {
        if (res && res.data && res.headers["output_format"]) {
          this.downloadFile(downLoadName, res.data);
        }
      });
    },
    cancelClick() {
      if (this.jvsQueryData.pageId) {
        window.postMessage(
          {
            command: "closeTab",
            id: this.jvsQueryData.id,
            from: this.jvsQueryData.from,
          },
          "*"
        );
      }
    },
    previewFile(row) {
      let protocolhost = this.$store.getters.kkfileUrl || "";
      if (protocolhost && row.url) {
        let view_url =
          `${protocolhost}/onlinePreview?forceUpdatedCache=true&officePreviewType=pdf&url=` +
          encodeURIComponent(Base64.encode(decodeURIComponent(row.url)));
        this.$openUrl(view_url, "_blank");
      }
    },
    // 复制
    copyHandle(value) {
      const text = document.createElement("input");
      text.value = value;
      document.body.appendChild(text);
      text.select();
      document.execCommand("Copy");
      document.body.removeChild(text);
      this.$notify({
        title: this.$langt("common.tip"),
        message: this.$langt("common.copySuccess"),
        position: "bottom-right",
        type: "success",
      });
    },
  },
  watch: {
    $route(to, from) {
      if (to.fullPath != from.fullPath) {
        // location.reload()
        this.createHandle();
      }
    },
  },
};
</script>
<style lang="scss" scoped>
/deep/.show-form {
  .el-form-item {
    padding: 0 20px;
  }
}
.ass-box-form {
}
.form-show-info {
  background: #fff;
  height: 100%;
  overflow-y: auto;
  box-sizing: border-box;
  border-radius: 6px;
  .permission {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    width: 100vw;
    img {
      width: 168px;
      height: 157px;
    }
    span {
      height: 18px;
      font-family: Microsoft YaHei-Regular, Microsoft YaHei;
      font-weight: 400;
      font-size: 14px;
      color: #3d3d3d;
      line-height: 18px;
    }
  }
  .empty-view {
    position: absolute;
    left: calc(50% - 95px);
    top: calc(50% - 155px);
    text-align: center;
    img {
      display: block;
    }
    div {
      height: 18px;
      font-family: Microsoft YaHei-Regular, Microsoft YaHei;
      font-weight: 400;
      font-size: 14px;
      color: #3d3d3d;
      line-height: 18px;
    }
  }
  .form-show-info-loading {
    width: 100%;
    height: 100%;
    position: absolute;
    left: 0;
    top: 0;
    background-color: #fff;
    background-image: url("../../../../styles/loading.gif");
    background-repeat: no-repeat;
    background-position: center;
    z-index: 9;
  }
}
.submit-after-dialog {
  position: absolute;
  width: 100%;
  height: 100vh;
  top: 0;
  left: 0;
  background: #fff;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2005; // 10002;
  .submit-after-dialog-body {
    p {
      text-align: center;
      margin: 10px 0 0 0;
      i {
        color: #5b8bff;
        font-size: 32px;
      }
      span {
        color: #909399;
      }
    }
    h4 {
      text-align: center;
      margin: 10px 0 0 0;
    }
    .el-row {
      display: flex;
      align-items: center;
      justify-content: center;
      margin-top: 10px;
    }
  }
}
.tag-tool-bar {
  right: 4vh;
  top: 10px;
  z-index: 1;
  display: flex;
  align-items: center;
}
.tag-set-info-box {
  padding: 0 25px;
  h3 {
    font-size: 18px;
    font-weight: 600;
    position: relative;
    padding-left: 10px;
    margin: 0;
    margin-bottom: 10px;
  }
  h3::before {
    position: absolute;
    content: "";
    width: 4px;
    height: 18px;
    background: #3471ff;
    border-radius: 2px;
    cursor: pointer;
    left: 0;
    top: 4px;
    cursor: auto;
  }
}
.log-tag-span-box {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  margin-left: 10px;
  svg {
    width: 18px;
    height: 18px;
    fill: #6f7588;
    cursor: pointer;
  }
  &:hover {
    background: #e4e7ea;
    border-radius: 4px;
  }
}
</style>
