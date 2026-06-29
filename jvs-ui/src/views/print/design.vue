<template>
  <div class="print_template_container" id="jvs-print-designer">
    <design-header ref="designHeader" :infoData="designData" :tabList="tabList" type="print" @tabSelect="tabSelect" @handleSave="savePrintDesign" @close="closeHandle"/>
    <el-container class="print_template_main" v-show="currentTab == 'design'">
			<!-- 左侧组件栏 -->
      <el-aside class="print-assembly-designer" width="320px">
        <el-collapse v-model="activeNames" class="print-assemblycont-tool-type-list">
          <el-collapse-item v-for="dataitem in datamodelToolList" :title="dataitem.title" :name="dataitem.name" :key="dataitem.title" class="custom-types-list rect-printElement-types hiprintEpContainer">
            <div
              v-for="(item, index) in dataitem.list"
              :key="dataitem.name + '-' + 'data-model-tool-item'+index"
              :tid="item.tid"
            >
              <a class="assemblycont_item ep-draggable-item" :tid="item.tid" style :data-options="JSON.stringify(item.options)">
                <svg class="icon svgicon glyphicon" aria-hidden="true">
                  <use :xlink:href="'#'+item.icon"></use>
                </svg>
                <span class="glyphicon-class">{{item.text}}</span>
              </a>
            </div>
          </el-collapse-item>
          <el-collapse-item title="自定义组件" name="0" class="rect-printElement-types hiprintEpContainer">
            <div
              class=""
              v-for="(item, index) in dragToolList"
              :key="'drag-tool-item'+index"
              tid="defaultModule.text"
            >
              <a class="assemblycont_item ep-draggable-item" :tid="item.tid" style>
                <i :class="'icon glyphicon ' + item.icon" aria-hidden="true"></i>
                <span class="glyphicon-class">{{item.text}}</span>
              </a>
            </div>
          </el-collapse-item>
          <el-collapse-item title="辅助组件" name="1" class="rect-printElement-types hiprintEpContainer">
            <div
              class=""
              v-for="(item, index) in assistToolList"
              :key="'assist-tool-item'+index"
              tid="defaultModule.text"
            >
              <a class="assemblycont_item ep-draggable-item" :tid="item.tid" style>
                <i :class="'icon glyphicon ' + item.icon" aria-hidden="true"></i>
                <span class="glyphicon-class">{{item.text}}</span>
              </a>
            </div>
          </el-collapse-item>
        </el-collapse>
      </el-aside>
      <!-- 右侧设计 -->
      <el-container>
        <div class="top-design-tool">
          <div class="left-box">
            <el-button-group>
              <el-button size="mini" :type="currentPaperType === 'A3' ? 'primary' : ''" @click="setPaper('A3')">A3</el-button>
              <el-button size="mini" :type="currentPaperType === 'A4' ? 'primary' : ''" @click="setPaper('A4')">A4</el-button>
              <el-button size="mini" :type="currentPaperType === 'A5' ? 'primary' : ''" @click="setPaper('A5')">A5</el-button>
              <el-button size="mini" :type="currentPaperType === 'B3' ? 'primary' : ''" @click="setPaper('B3')">B3</el-button>
              <el-button size="mini" :type="currentPaperType === 'B4' ? 'primary' : ''" @click="setPaper('B4')">B4</el-button>
              <el-button size="mini" :type="currentPaperType === 'B5' ? 'primary' : ''" @click="setPaper('B5')">B5</el-button>
            </el-button-group>
            <el-popover
              placement="bottom"
              title=""
              trigger="click"
              style="margin: 0 1px;"
            >
              <div class="custompaper">
                <h5>请输入自定义纸张宽高</h5>
                <el-form
                  ref="otherPaperForm"
                  label-position="left"
                  :model="otherPaper"
                  label-width="30px"
                >
                  <el-row>
                    <el-col :span="24">
                      <el-form-item label="宽">
                        <el-input v-model="otherPaper.width">
                          <template slot="append">mm</template>
                        </el-input>
                      </el-form-item>
                    </el-col>
                  </el-row>
                  <el-row>
                    <el-col :span="24">
                      <el-form-item label="高">
                        <el-input v-model="otherPaper.height">
                          <template slot="append">mm</template>
                        </el-input>
                      </el-form-item>
                    </el-col>
                  </el-row>
                  <el-form-item class="btn-form-item" style="display:flex;align-items:center;justify-content: center;">
                    <el-button
                      type="primary"
                      @click="
                        setPaper('other', otherPaper.width, otherPaper.height)
                      "
                      >确定</el-button
                    >
                    <el-button @click="setPaper('other', '', '')"
                      >清空</el-button
                    >
                  </el-form-item>
                </el-form>
              </div>
              <el-button-group  slot="reference">
                <el-button :type="currentPaperType === 'other' ? 'primary' : ''">自定义纸张</el-button>
              </el-button-group>
            </el-popover>
            <p class="design-tool">
                <el-tooltip effect="dark" content="旋转" placement="top">
                  <i class="el-icon-refresh-right form-design-tool" @click="rotatePaper"></i>
                </el-tooltip>
            </p>
            <p class="design-tool">
              <el-tooltip effect="dark" content="清空" placement="top">
                <i class="el-icon-delete form-design-tool" @click="clearTemplate"></i>
              </el-tooltip>
            </p>
            <p class="design-tool">
              <el-popover
                placement="bottom"
                width="100"
                trigger="hover">
                <div>
                  <ul class="preview-tool-ul">
                    <li @click="previewTemplate()">
                      <i class="el-icon-monitor"></i>
                      <span>快速预览</span>
                    </li>
                    <li @click="getJson()">
                      <i class="el-icon-document"></i>
                      <span>预览JSON</span>
                    </li>
                    <li @click="getHtml()">
                      <i class="el-icon-tickets"></i>
                      <span>预览HTML</span>
                    </li>
                  </ul>
                </div>
                <i slot="reference" class="el-icon-view form-design-tool"></i>
              </el-popover>
            </p>
            <p class="design-tool">
              <el-tooltip effect="dark" content="数据结构" placement="top">
                <i class="el-icon-c-scale-to-original form-design-tool" @click="viewDataDemo"></i>
              </el-tooltip>
            </p>
          </div>
        </div>
        <el-main style="background:transparent;padding:0 20px;">
          <el-row style="position:relative;overflow: hidden;">
            <!-- 测量尺 -->
            <div class="hiprint_rul_wrapper" style="top: 32px;left:30px;">
              <img class="h_img" src="data:image/png;base64,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">
              <img class="v_img" src="data:image/png;base64,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9X6eqmP080QdMz9Prcfo56n1GP08UcfMzxN1bN1vT1sfvAtmztM+TPh2MsUAI9/g+OqMOgj2ybUbWok6GHTtD/C3BZFUHdyAa2MMEnW0NpE8rjjx5WdA0nXQd9pE+1mTRB0E++TajepEHf3n1arjatbHUQdwjHxjNR3Q5nH/kz70uemC1XXM6sE2rR7HxhXR+p6qo+030nT07fRxhUek66Df9J820XwvsR5safefw4k66C/tvXzuNh39WGuvSdBBv5kXHINUHWxpp9ejjSuC/WQdbR8SdfTt9HE1+nmijr7PBJpS68GWdvPzVB178cFRR/8atrQTdND33s9TdbClnV6P0c+TdbR9SNTRt9PH1ejniTrod/MKon1upekAtrSbn6fq2IsPznS088XGajr+Z/YiRB1roY61UMdaqGMt1LEW6lgLdayFOtZCHWuhjrVQx1qoYy3UsRbqWAt1rIU61kIda6GOtVDHWtzxb6CLiIiIiIiIiIiIiIiIiIgU9x22e+CPh2066lgLdayFOtZCHWuhjrVQx1qoYy3UsRbqWAt1rIU61kIda6GOtVDHWqhjLdSxFupYC3WshTqW4dat/wKB2hwSL8nDjQAAAABJRU5ErkJggg==">
            </div>
						<!-- 设计区 -->
            <el-col style="padding: 20px;" :span="18" id="hiprint-printTemplate" class="hiprint-printTemplate">
            </el-col>
						<!-- 参数 -->
            <el-col :span="6" style="display: none" class="params_setting_container">
              <el-row class="hinnn-layout-sider" style="padding: 10px">
                <div id="PrintElementOptionSetting"></div>
              </el-row>
            </el-col>
          </el-row>
					<!-- 组件参数设置 -->
          <el-drawer
            size="100%"
            class="params_drawer"
            :modal="false"
            :wrapperClosable="false"
            :visible.sync="paramsDrawerStatus"
            direction="rtl"
          >
            <div v-if="selectEl" :key="selectEl.id" style="padding: 0 10px;">
              <h2 slot="title" style="margin-top:50px;padding:0 10px;">参数设置: {{ paramsDrawerTitle }}</h2>
              <div class="params_form" v-if="paramsDrawerStatus">
                <jvs-form :option="paramFormOption" :formData="paramFormData" @formChange="updateHandle" @submit="paramSubmitHandle">
                  <template slot="uploadForm">
                    <el-upload
                      class="avatar-uploader"
                      ref="imageUploader"
                      action="/mgr/jvs-auth/upload/jvs-public"
                      :show-file-list="false"
                      :limit="1"
                      list-type="picture"
                      accept=".jpg,.jpeg,.png"
                      :headers="headers"
                      :data="{module: '/jvs-ui/print/'}"
                      :on-success="handleAvatarSuccess"
                      :on-error="errorHandle">
                      <img v-if="paramFormData.src" :src="paramFormData.src" class="avatar">
                      <i v-else class="el-icon-plus avatar-uploader-icon"></i>
                      <i v-if="paramFormData.src" class="delImgIcon el-icon-delete" @click.stop="clearImg"></i>
                    </el-upload>
                  </template>
                  <el-button v-if="false" slot="formButton" size="mini" type="danger" @click="delSelectEl">删除</el-button>
                </jvs-form>
              </div>
            </div>
            <div v-else class="content-empty">
              <h2>JVS Print自定义模块打印</h2>
              <div style="color:#f56c6c;">鼠标左键选中组件，鼠标右键唤起操作菜单</div>
              <p>1. 支持上传背景图/悬浮图片/内嵌图片；</p>
              <p>2. 支持设置打印纸张及页边距（A3、A4、A5、B3、B5、自定义）；</p>
              <p>3. 支持设置纸张纵向、横向排版；</p>
              <p></p>4. 支持分页设计打印内容；
              <p>5. 支持二维码组件（可选择显示数据查看页面、表单提交页面、自定义链接页面）；</p>
              <p>6. 支持配置页眉页脚显示内容（发起人、发起时间、打印时间、页面）；</p>
              <p>7. 支持打印手写签名；</p>
              <p>8. 支持水印（文字水印、图片水印）；</p>
              <p>9. 支持打印审批结果签章；</p>
              <p>10. 支持数字组件设置千分符号；</p>
              <p>11. 支持打印字体设置；</p>
              <p>12. 支持“打印人”、“打印时间”系统变量。</p>
            </div>
          </el-drawer>
          <div class="params_dialog" v-if="false && paramsDrawerStatus" @click="paramCloseHandle"></div>
        </el-main>
      </el-container>
    </el-container>
		<!-- 预览 -->
    <el-dialog
      class="preview_dialog"
      ref="previewDialog"
      title="快速预览"
      append-to-body
      @opened="handlePreviewOpened()"
      :width="currentPaper.width * 1 + 'mm'"
      :visible.sync="previewDialogStatus"
      :close-on-click-modal="false"
    >
      <div id="preview_content"></div>
      <span slot="footer" class="dialog-footer" style="display:flex;justify-content:center;align-items:center;">
        <el-button type="primary" @click="previewDialogStatus = false">确 定</el-button>
        <el-button @click="previewDialogStatus = false">取 消</el-button>
      </span>
    </el-dialog>
		<!-- 预览JSON -->
    <el-dialog
      ref="infoDialog"
      :title="infoDialogTitle"
      :lock-scroll="true"
      :visible.sync="infoDialogStatus"
      append-to-body
      :close-on-click-modal="false"
    >
      <el-input
        type="textarea"
        :readonly="true"
        :autosize="{ minRows: 15 }"
        placeholder="请输入内容"
        v-model="infoTextarea"
      ></el-input>
      <span slot="footer" class="dialog-footer" style="display:flex;justify-content:center;align-items:center;">
        <el-button type="primary" @click="copyHandle">复 制</el-button>
        <el-button @click="infoDialogStatus = false">取 消</el-button>
      </span>
    </el-dialog>
    <div class="print-loading" v-if="loading">
      <img :src="loadingImg" alt="">
    </div>
  </div>
</template>
<script>
import { getTemplate, saveTemplate, getParamList } from './api/index'

import { hiprint } from '../../../public/jvs-ui-public/plugin/hiprint/hiprint.bundle.js'
import { defaultElementTypeProvider } from '../../../public/jvs-ui-public/plugin/hiprint/etypes/default-etype-provider.js'
import DesignHeader from "@/components/page-header/DesignHeader";
import saveicon from "@/const/img/保存.png"

import {textParam} from '@/const/paramData/text'
import {imageParam} from '@/const/paramData/image'
import {longtextParam} from '@/const/paramData/longtext'
import {tableParam} from '@/const/paramData/table'
import {otherParam} from '@/const/paramData/other'
import {paperParam} from '@/const/paramData/paper'

import loadingImg from '@/styles/loading.gif'
import { demoPrintJson } from '../../../public/jvs-ui-public/plugin/hiprint/custom_test/custom-print-json.js'

var hiprintTemplate;
export default {
  components: {DesignHeader},
  data () {
    return {
      loadingImg: loadingImg,
      loading: true,
      designData: {},
      activeNames: ['0', '1', '2'],
      datamodelToolList: [],
      dragToolList: [
        {
          icon: 'el-icon-tickets', // 'glyphicon-text-width',
          tid: 'defaultModule.text',
          text: '文本'
        },
        {
          icon: 'el-icon-picture-outline', //'glyphicon-picture',
          tid: 'defaultModule.image',
          text: '图片'
        },
        {
          icon: 'el-icon-notebook-2', // 'glyphicon-subscript',
          tid: 'defaultModule.longText',
          text: '长文'
        },
        {
          icon: 'el-icon-film', // 'glyphicon-th',
          tid: 'defaultModule.tableCustom',
          text: '表格'
        }
      ],
      assistToolList: [
        {
          icon: 'el-icon-minus', // 'glyphicon-resize-horizontal',
          tid: 'defaultModule.hline',
          text: '横线'
        },
        {
          icon: 'el-icon-minus rotate90', // 'glyphicon-resize-vertical',
          tid: 'defaultModule.vline',
          text: '竖线'
        },
        {
          icon: 'el-icon-full-screen', // 'glyphicon-unchecked',
          tid: 'defaultModule.rect',
          text: '矩形'
        },
        {
          icon: 'el-icon-bangzhu', // 'glyphicon-record',
          tid: 'defaultModule.oval',
          text: '椭圆'
        }
      ],
      previewDialogStatus: false,
      paramsDrawerStatus: true,
      infoDialogStatus: false,
      infoTextarea: '',
      infoDialogTitle: '',
      paramsDrawerTitle: '',
      previewHtml: null,
      paperMap: {
        A3: {
          width: 420,
          height: 296.6
        },
        A4: {
          width: 210,
          height: 296.6
        },
        A5: {
          width: 210,
          height: 147.6
        },
        B3: {
          width: 500,
          height: 352.6
        },
        B4: {
          width: 250,
          height: 352.6
        },
        B5: {
          width: 250,
          height: 175.6
        }
      },
      currentPaper: {
        width: null,
        height: null
      },
      otherPaper: {
        width: '',
        height: ''
      },
      saveIcon: saveicon,
      saveLoading: false,
      selectEl: null,
      paramFormData: {},
      paramFormOption: {
        cancal: false,
        emptyBtn: false,
        labelWidth: 'auto',
        formAlign: 'left',
        submitBtn: false,
        column: []
      },
      showEdit: false,
      editName: '',
      currentTab: 'design',
      tabList: [
        { name: 'design', label: '打印设计', icon: 'el-icon-set-up' },
        // { name: 'pageSetting', label: '基础信息', icon: 'el-icon-setting' },
      ],
      identity: '',
      templateId: '',
      templateInfo: null,
      headers: {
        tenantId: this.$store.getters.userInfo.tenantId,
        Authorization: ('Bearer '+this.$store.getters.access_token)
      },
    }
  },
  computed: {
    // 处理当前选中纸张
    currentPaperType () {
      let type=null;
      if (this.otherPaper.width!==''&&this.otherPaper.height!=='') {
        type='other'
      } else {
        for (const key in this.paperMap) {
          let item=this.paperMap[key]
          let { width, height }=this.currentPaper
          if (item.width===width&&item.height===height) {
            type=key
          }
        }
      }

      return type
    }
  },
  methods: {
    // 初始化
    init () {
    //初始化打印插件
      hiprint.init({
        providers: [new defaultElementTypeProvider()]
      });
      //设置左侧拖拽事件
      hiprint.PrintElementTypeManager.buildByHtml($('.ep-draggable-item'));
      // 空白设计默认填充
      let _demoPrintJson = {
        panels: [
          {
            index: 0,
            height: 297,
            width: 210,
            paperHeader: 45,
            paperFooter: 780,
            printElements: [
              {
                options: {
                  left: 175.5,
                  top: 10.5,
                  height: 27,
                  width: 259,
                  title: "JVS Print自定义模块打印",
                  fontSize: 19,
                  fontWeight: "600",
                  textAlign: "center",
                  lineHeight: 26
                },
                printElementType: { title: "自定义文本", type: "text" }
              },
              {
                options: {
                  left: 200,
                  top: 800,
                  height: 13,
                  width: 205,
                  title: "页眉线已上、页尾下以下每页都会重复打印",
                  textAlign: "center"
                },
                printElementType: { title: "自定义文本", type: "text" }
              }
            ]
          }
        ]
      }
      if(this.designData.design) {
        _demoPrintJson = this.designData.design
      }
      hiprintTemplate = new hiprint.PrintTemplate({
        template: _demoPrintJson,
        settingContainer: '#PrintElementOptionSetting',
        paginationContainer: '.hiprint-printPagination'
      });
      //打印设计
      hiprintTemplate.design('#hiprint-printTemplate');
      let paperType = 'A4'
      if(this.designData.design && this.designData.design.panels && this.designData.design.panels.length > 0) {
        paperType = this.designData.design.panels[0].paperType ? this.designData.design.panels[0].paperType : 'A4'
      }
      this.setCurrentPaper(this.paperMap[paperType]);
      this.elementAddEventListen();
      $('.params_drawer').width($('.params_setting_container').width()*1) //动态设置右部参数设置框宽度
    },
    /**
     * 设置纸张属性
     * width 当前纸张宽度 mm
     * height 当前纸张高度 mm
     */
    setCurrentPaper (obj) {
      let { width, height }=obj;
      this.currentPaper.width=width;
      this.currentPaper.height=height;
    },
    /**
     * 设置纸张
     * type [A3, A4, A5, B3, B4, B5, other]
     * width 自定义高度
     * height 自定义高度
     */
    setPaper (type, width, height) {
      try {
        if (type==='other') {
          if (width===''&&height==='') {
            hiprintTemplate.setPaper('A4', null);
            this.setCurrentPaper(this.paperMap['A4'])
            this.otherPaper={ width: '', height: '' }
          } else {
            hiprintTemplate.setPaper(width, height);
            this.setCurrentPaper({ width: width, height: height })
          }
        } else {
          hiprintTemplate.setPaper(type, null);
          this.setCurrentPaper(this.paperMap[type])
          this.otherPaper={ width: '', height: '' }
        }
      } catch (error) {
        this.$message({
          message: '操作失败:'+error,
          type: 'error'
        });
      }
    },
    // 旋转
    rotatePaper () {
      try {
        hiprintTemplate.rotatePaper();
      } catch (error) {
        this.$message({
          message: '操作失败:'+error,
          type: 'error'
        });
      }
    },
    // 清除配置
    clearTemplate () {
      try {
        hiprintTemplate.clear();
        if(this.paramsDrawerStatus) {
          this.paramCloseHandle()
        }
      } catch (error) {
        this.$message({
          message: '操作失败:'+error,
          type: 'error'
        });
      }
    },
    // 快速预览
    previewTemplate () {
      this.previewDialogStatus=true;
    },
    // 打开预览dialog回调
    handlePreviewOpened () {
      $('#preview_content').html(hiprintTemplate.getHtml())
    },
    // 设置element click事件监听
    elementAddEventListen () {
      let self=this
      // 点击容器关闭参数设置
      window.hinnn.event.on(hiprintTemplate.getBuildCustomOptionSettingEventKey(), function (e) {
        // self.paramsDrawerStatus = false
        // self.paramsDrawerTitle = '页面'
        // self.paramFormOption.column = JSON.parse(JSON.stringify(paperParam))
        // self.paramFormData = JSON.parse(JSON.stringify(e.options))
        self.selectEl = null
        // self.paramsDrawerStatus=true
        if(self.paramsDrawerStatus) {
          if($('.hicontextmenu') && $('.hicontextmenu').css('display') == 'block') {
            $('.hicontextmenu').css('display', 'none')
          }
        }
      })
      // 选中单个面板
      window.hinnn.event.on(hiprintTemplate.getPrintElementSelectEventKey(), function (e) {
        if(e.printElement) {
          self.paramFormData = {}
          let t = e.printElement
          self.selectEl = t
          if(t.printElementType) {
            self.paramsDrawerTitle=t.printElementType.title
            self.paramFormOption.column = []
            switch(t.printElementType.type) {
              case 'text':
                self.paramFormOption.column = JSON.parse(JSON.stringify(textParam));
                break;
              case 'image':
                self.paramFormOption.column = JSON.parse(JSON.stringify(imageParam));
                break;
              case 'longText':
                self.paramFormOption.column = JSON.parse(JSON.stringify(longtextParam));
                break;
              case 'tableCustom':
                self.paramFormOption.column = JSON.parse(JSON.stringify(tableParam));
                break;
              default:
                self.paramFormOption.column = JSON.parse(JSON.stringify(otherParam));
                break;
            }
          }
          if(t.options) {
            if(t.printElementType.type != 'tableCustom') {
              self.paramFormData = JSON.parse(JSON.stringify(t.options))
            }else{
              let tp = {}
              for(let ti in tableParam) {
                let k = tableParam[ti].prop
                if(t.options[k]) {
                  tp[k] = t.options[k]
                }
              }
              self.paramFormData = tp
            }
          }
          self.paramFormOption.column.filter(item => {
            item.span = 24 // item.span == 24 ? 24 : 12;
            if(item.prop == 'field'){
              if(t.options.fromDesign) {
                item.display = false
              }else{
                item.display = true
              }
            }
          })
          self.paramsDrawerStatus=true
          if($('.hicontextmenu') && $('.hicontextmenu').css('display') == 'block') {
            $('.hicontextmenu').css('display', 'none')
          }
          self.$forceUpdate()
        }
      })
    },
    // 更新设计
    updateHandle () {
      if(this.selectEl) {
        for(let i in this.paramFormData) {
          this.selectEl.options[i] = this.paramFormData[i]
        }
        this.selectEl.updateDesignViewFromOptions()
      }else{
        // console.log(hiprintTemplate.editingPanel.editingPanel)
        for(let i in this.paramFormData) {
          console.log(i, this.paramFormData[i])
          hiprintTemplate.getPanel()[i] = this.paramFormData[i]
        }
        // hiprintTemplate.getPanel().updateRectPanel()
        console.log(hiprintTemplate)
      }
    },
    // 参数设置提交
    paramSubmitHandle (form) {
      this.updateHandle()
      this.paramCloseHandle()
    },
    // 删除当前面板
    delSelectEl () {
      hiprintTemplate.deletePrintElement(this.selectEl)
      this.paramCloseHandle()
    },
    paramCloseHandle () {
      // this.paramsDrawerStatus = false
      this.paramsDrawerTitle = ''
      this.selectEl = null
      this.paramFormData = {}
      this.paramFormOption.column = []
    },
    // 获取配置JSON
    getJson () {
      this.infoTextarea=JSON.stringify(hiprintTemplate.getJson());
      this.infoDialogTitle='预览JSON'
      this.infoDialogStatus=true;
    },
    // 获取配置HTML
    getHtml () {
      this.infoTextarea=hiprintTemplate.getHtml()[0].outerHTML;
      this.infoDialogTitle='预览HTML'
      this.infoDialogStatus=true;
    },
    // 保存设计
    savePrintDesign (type) {
      this.saveLoading = true
      let param = JSON.parse(JSON.stringify(this.designData))
      this.designData.design = hiprintTemplate.getJson()
      param.design = JSON.stringify(this.designData.design)
      saveTemplate(this.$route.query.jvsAppId, param).then(res => {
        if(res.data && res.data.code == 0) {
          // this.$message.success('保存成功')
          this.$notify({
            title: '提示',
            message: '保存成功',
            position: 'bottom-right',
            type: 'success'
          });
          this.saveLoading = false
          if(type == 'editName') {
            this.$emit('fresh', true)
          }
        }
      })
    },
    // 关闭
    closeHandle () {
      this.$emit('close', true)
    },
    // 获取设计结构
    async getTemplateDesign (id) {
      this.loading = true
      await getTemplate(this.$route.query.jvsAppId, id).then(res => {
        if(res.data && res.data.code == 0) {
          // console.log(res.data.data)
          this.designData = {...res.data.data, icon: 'icon-icon_1-06'}
          if(res.data.data.design) {
            this.$set(this.designData, 'design', JSON.parse(res.data.data.design))
          }
          this.loading = false
          this.init()
        }
      })
    },
    // 修改名称
    shoeEditHandle () {
      this.showEdit = true
      this.editName = this.designData.name
    },
    editNameHanlde () {
      if(this.editName != this.designData.name) {
        this.$set(this.designData, 'name', this.editName)
        this.savePrintDesign('editName')
      }
      this.showEdit = false
      this.editName = ''
    },
    // 复制
    copyHandle () {
      const text = document.createElement('input')
      text.value = this.infoTextarea
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
    // 查看数据结构
    viewDataDemo () {
      let keys = hiprintTemplate.getFieldsInPanel()
      let obj = {}
      let ppls = hiprintTemplate.printPanels
      for(let p in ppls) {
        for(let el in ppls[p].printElements) {
          if(ppls[p].printElements[el].options && ppls[p].printElements[el].options.field) {
            let ky = ppls[p].printElements[el].options.field
            if(keys.indexOf(ky) > -1) {
              switch(ppls[p].printElements[el].printElementType.type) {
                case 'text':
                  obj[ky] = "文本";
                  break;
                case 'image':
                  obj[ky] = "图片地址";
                  break;
                case 'tableCustom':
                  let temp = [];
                  if(ppls[p].printElements[el].columns && ppls[p].printElements[el].columns.length > 0) {
                    for(let c in ppls[p].printElements[el].columns) {
                      let cbj = {}
                      for(let cc in ppls[p].printElements[el].columns[c].columns) {
                        cbj[ppls[p].printElements[el].columns[c].columns[cc].field] = ppls[p].printElements[el].columns[c].columns[cc].title
                      }
                      temp.push(cbj)
                    }
                  }
                  obj[ky] = temp;
                  break;
                case 'longText':
                  obj[ky] = "长文本";
                  break;
                default: ;break;
              }
            }
          }
        }
      }
      this.infoTextarea=JSON.stringify(obj)
      this.infoDialogTitle='数据结构'
      this.infoDialogStatus=true;
    },
    // tab选择结果
    tabSelect(val) {
      this.currentTab = val
      // if (val === 'permission') {
      //   this.$refs.permission.initData()
      // }
    },
    // 获取参数列表
    async getParamListHandle (id) {
      this.datamodelToolList = []
      this.activeNames = ['0', '1']
      await getParamList(id).then(res => {
        if(res.data && res.data.code == 0 && res.data.data) {
          if(Object.keys(res.data.data).length > 0) {
            let k = Object.keys(res.data.data)[0]
            let pks = Object.keys(res.data.data[k])
            for(let i in pks) {
              let pitem = {
                title: pks[i],
                name: Number(i) + 1,
                list: []
              }
              this.activeNames.push(pitem.name)
              let list = res.data.data[k][pks[i]]
              list.filter(item => {
                let obj = {
                  text: item.name
                }
                let emptyName = ''
                switch(item.fieldType) {
                  case 'input':
                    obj.tid = 'defaultModule.text';
                    obj.icon = 'icon-jvs-danhangwenben';
                    obj.options = {
                      title: item.name,
                      field: item.id
                    };
                    emptyName = '单行文本';
                    break;
                  case 'textarea':
                    obj.tid = 'defaultModule.longText';
                    obj.icon = 'icon-jvs-duohangwenben';
                    obj.options = {
                      title: item.name,
                      field: item.id
                    };
                    break;
                  case 'image':
                  case 'imageUpload':
                    obj.tid = 'defaultModule.image';
                    obj.icon = 'icon-jvs-tupian';
                    obj.options = {
                      title: item.name,
                      field: item.id
                    };
                    emptyName = '图片';
                    break;
                  case 'tableForm':
                    obj.tid = 'defaultModule.tableCustom';
                    obj.icon = 'icon-jvs-biaoge';
                    obj.options = {
                      title: item.name,
                      field: item.id
                    };
                    if(item.other && item.other.tableFields){
                      obj.options.columns = [{columns: []}]
                      item.other.tableFields.filter(it => {
                        let tob = {
                          title: it.name,
                          field: it.id,
                          width: 93.5,
                          colspan: 1,
                          rowspan: 1
                        }
                        obj.options.columns[0].columns.push(tob)
                      });
                      emptyName = '表格';
                    };
                    break;
                  default:
                    obj.tid = 'defaultModule.text';
                    obj.icon = 'icon-jvs-danhangwenben';
                    obj.options = {
                      title: item.name,
                      field: item.id
                    };
                    break;
                }
                obj.options.fromDesign = true
                obj.options.dataSourceType = (item.other && item.other.dataSourceType) ? item.other.dataSourceType : 'form'
                if(item.name.endsWith('.') || !item.name) {
                  obj.text += emptyName
                  obj.options.title += emptyName
                }
                pitem.list.push(obj)
              })
              this.datamodelToolList.push(pitem)
            }
          }
        }
      })
    },
    // 上传图片
    handleAvatarSuccess (res, file, fileList) {
      if(res.code == 0) {
        // this.$message.success("上传成功")
        this.$notify({
          title: '提示',
          message: '上传成功',
          position: 'bottom-right',
          type: 'success'
        });
        this.$set(this.paramFormData, 'src', res.data.fileLink)
        this.updateHandle()
      }else{
        this.$refs.imageUploader.clearFiles()
        // this.$message.error(res.msg)
        this.$notify({
          title: '提示',
          message: res.msg,
          position: 'bottom-right',
          type: 'error'
        });
      }
    },
    errorHandle (err, file, fileList) {
      this.$refs.imageUploader.clearFiles()
      // this.$message.error(err)
      this.$notify({
        title: '提示',
        message: err,
        position: 'bottom-right',
        type: 'error'
      });
    },
    clearImg () {
      this.$set(this.paramFormData, 'src', '')
      this.$refs.imageUploader.clearFiles()
      this.updateHandle()
    }
  },
  async mounted () {
    let self=this
    await self.getParamListHandle(self.templateInfo.designId)
    $(document).ready(function () {
      if(self.templateInfo) {
        self.designData = JSON.parse(JSON.stringify(self.templateInfo))
        self.getTemplateDesign(self.templateInfo.id)
      }
    });
  },
  created () {
    if(this.$route.query) {
      this.templateInfo = JSON.parse(JSON.stringify(this.$route.query))
    }
  }
}
</script>
<style lang="scss" scoped>
.custompaper{
  padding: 0 60px;
  .el-form-item{
    margin-bottom: 16px;
  }
  .btn-form-item{
    /deep/.el-form-item__content{
      margin: 0!important;
    }
  }
}
.avatar-uploader{
  display: block;
  width: 100%;
  height: 180px;
  overflow: hidden;
  /deep/.el-upload {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    img{
      display: block;
      width: 178px;
      height: 178px;
    }
    .delImgIcon{
      font-size: 14px;
      position: absolute;
      top: 2px;
      right: 2px;
      color: #F56C6C;
      cursor: pointer;
    }
  }
  /deep/.avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 178px;
    height: 178px;
    line-height: 178px;
    text-align: center;
  }
}
/deep/.hiprint-printTemplate {
  overflow: hidden;
  overflow-x: auto;
  margin: 10px 0 0 10px;
  .hiprint-printPanel{
    display: inline-block;
    overflow: hidden;
    .hiprint-printPaper {
      background-color: #fff;
    }
  }
}
/deep/.design {
  .hiprint-printElement-table-handle{
    margin-left: -21pt;
  }
}
</style>
<style lang="scss">
.print_template_container{
  position: relative;
  height: 100%;
  box-sizing: border-box;
  overflow: hidden;
  background: #f0f2f5;
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
  .design-tool{
    position: relative;
    z-index: 999;
    .form-design-tool{
      font-size: 20px;
      cursor: pointer;
      color: #353535;
      cursor: pointer;
    }
    .icon {
      width: 18px;
      height: 18px;
      fill: currentColor;
      overflow: hidden;
      cursor: pointer;
    }
    img{
      display: block;
      width: 15px;
      height: 15px;
      cursor: pointer;
    }
  }
  .print_template_main{
    position: absolute;
    width: 100%;
    height: calc(100% - 52px);
    .print-assembly-designer{
      width: 320px;
      height: 100%;
      overflow: hidden;
      background: #fff;
      padding: 0;
      overflow-y: auto;
    }
    .print-assembly-designer::-webkit-scrollbar{
      display: none;
    }
    .print-assemblycont-tool-type-list{
      border: 0;
      .el-collapse-item__wrap, .el-collapse-item__header{
        border: 0;
      }
      .el-collapse-item__content{
        display: grid;
        grid-template-columns: calc(50% - 4px) calc(50% - 4px);
        grid-row-gap: 12px;
        grid-column-gap: 8px;
        //margin-bottom: 16px;
        margin: 12px 0;
      }
      .el-collapse-item__wrap{
        padding: 0 10px;
        background: #fff;
      }
      .el-collapse-item__header{
        height: 40px;
        line-height: 40px;
        margin-top: 0;
        background: #ffffff;
        position: relative;
        text-indent: 10px;
        font-size: 14px;
        .el-collapse-item__arrow{
          position: absolute;
          right: 0;
          top: 9px;
        }
        .is-active{
          .el-collapse-item__arrow{
            color: #3471FF;
            //top: 2px;
            transform: rotate(90deg);
          }
        }
      }
      .el-collapse-item__content{
        padding-bottom: 0;
        .assemblycont_item {
          padding: 0 12px;
          display: flex;
          align-items: center;
          height: 32px;
          box-sizing: border-box;
          transition: 0.3s;
          cursor: pointer;
          cursor: move;
          font-size: 12px;
          background: #fff;
          border: 1px solid #DCDFE6;
          border-radius: 4px;
          overflow: hidden;
          box-sizing: border-box;
          color: #333333;
          .ep-draggable-item{
            display: block;
            width: 100%;
            height: 100%;
          }
          i{
            margin-right: 12px;
            font-size: 16px;
            color: #d9d9d9;
          }
          .svgicon{
            width: 20px;
            height: 20px;
            fill: currentColor;
            overflow: hidden;
            cursor: pointer;
            margin-right: 10px;
          }
        }
        .assemblycont_item:hover {
          transition: 0.3s;
          color: #3471FF;
          background: #EEEEEE;
          text-decoration: none;
        }
        .assemblycont_item .rotate90{
          transform: rotate(90deg);
        }
      }
      .custom-types-list{
        .el-collapse-item__wrap{
          .el-collapse-item__content{
            grid-template-columns: calc(100% - 4px);
          }
        }
      }
    }
    .params_drawer{
      width: 350px!important; // 312
      .el-drawer__header{
        padding: 0 10px;
        h4{
          font-size: 16px;
        }
        .el-drawer__close-btn{
          display: none;
        }
      }
      .el-drawer__body{
        overflow-x: hidden;
      }
      .el-drawer__body::-webkit-scrollbar{
        display: none;
      }
      .params_form {
        padding: 0;
        .jvs-form{
          .form-item-btn{
            margin-top: 10px;
          }
          h5{
            font-size: 14px;
            color: #606266;
            vertical-align: middle;
            line-height: 28px;
            margin: 10px 0;
            font-weight: 700;
          }
          .el-form-item{
            margin: 0 10px;
            margin-bottom: 8px;
          }
          .el-col{
            h5{
              text-indent: 10px;
              margin: 0;
            }
          }
        }
      }
      .content-empty{
        padding: 0 20px;
        h2{
          margin-top: 50px;
        }
        p{
          margin: 10px 0;
        }
      }
    }
    .right-side-tool{
      position: absolute;
      top: 20px;
      left: 75%;
      width: 20px;
      box-shadow: 0 0 10px rgba(25, 25, 25, 0.1);
      border-radius: 2px;
      background-color: #FFFFFF;
      padding: 5px;
      box-sizing: content-box;
      i{
        font-size: 20px;
        cursor: pointer;
        margin: 4px 0;
        &:hover{
          color: #0D76FC;
        }
      }
    }
    .right-side-tool-A3, .right-side-tool-B3{
      left: 76%;
    }
    .right-side-tool-A4, .right-side-tool-A5{
      left: 71%;
    }
    .right-side-tool-B4{
      left: 76%;
    }
    .right-side-tool-B5{
      left: 76%;
    }
    .el-container{
      flex-direction: column;
      .top-design-tool{
        padding: 0;
        height: 36px;
        position: relative;
        background: #fff;
        display: flex;
        align-items: center;
        justify-content: space-between;
        box-sizing: border-box;
        .left-box{
          display: flex;
          align-items: center;
          .el-button{
            border-color: #fff;
          }
          p{
            margin: 0;
            padding: 0 17px;
            display: flex;
            -webkit-box-pack: center;
            -ms-flex-pack: center;
            justify-content: center;
            -webkit-box-align: center;
            -ms-flex-align: center;
            align-items: center;
            border-right: 1px solid #DCDFE6;
          }
          p:nth-last-of-type(1) {
            border-right: 0;
          }
        }
      }
    }

    .params_dialog{
      position: fixed;
      top: 0;
      width: 100%;
      height: 100%;
      right: 0;
      background: rgba(0,0,0,.5);
    }
  }
}
.preview-tool-ul{
  list-style: none;
  margin: 0;
  padding: 0;
  li{
    list-style: none;
    height: 30px;
    line-height: 30px;
    cursor: pointer;
    margin: 0 10px;
    i{
      margin-right: 10px;
    }
  }
  li:hover{
    color: #3471FF;
  }
}
.hicontextmenu.hicontextmenuroot{
  z-index: 9999;
  >ul{
    z-index: 10000;
  }
}
.hicontextmenu.justone{
  width: auto;
}
// loading
.print-loading{
  position: fixed;
  z-index: 9999;
  width: 100%;
  height: 100vh;
  top: 0;
  left: 0;
  background: #fff;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  img{
    display:block;
    width: 40px;
    height: 40px;
  }
}
</style>
<style>
.el-header,
.el-footer {
  background-color: #b3c0d1;
  color: #333;
  line-height: 60px;
}

.el-aside {
  background-color: #d3dce6;
  color: #333;
  padding: 6px;
}

.el-main {
  background-color: #e9eef3;
  color: #333;
  padding: 0;
}

.drag_item_box {
  height: 100%;
  padding: 6px;
}

.drag_item_box > div {
  height: 100%;
  width: 100%;
  background-color: #fff;
  display: flex;
  justify-content: center;
  align-items: center;
}

.drag_item_box > div > a {
  text-align: center;
  text-decoration-line: none;
}

.drag_item_box > div > a > span {
  font-size: 28px;
}

.drag_item_box > div > a > p {
  margin: 0;
}

.drag_item_title {
  font-size: 16px;
  padding: 12px 6px 0 6px;
  font-weight: bold;
}

.preview_dialog .el-dialog__body {
  padding: 0!important;
}

.params_drawer {
  left: auto!important;
  top: 47px!important;
}

.params_drawer .el-drawer.rtl {
  overflow: auto;
}

.params_drawer .el-drawer__header {
  margin-bottom: 0;
}

.params_drawer {
  left: auto;
}

.full_screen_dialog .el-dialog__header {
  padding: 0;
  margin: 0;
}

.full_screen_dialog .el-dialog__body {
  padding: 0;
}
.preview_dialog .el-dialog__body .hiprint-printTemplate{
  margin: 0!important;
}
.hiprint-printElement-table.design{
  min-height: 38pt;
}
</style>
