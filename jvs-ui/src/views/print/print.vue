<template>
  <div :class="{'print_template_container-print': true, 'print_template_container-print-isMobile': isMobileBool}" id="jvs-print-designer">
    <design-header ref="designHeader" :infoData="designData" :tabList="tabList" :hassave="false" type="print" @tabSelect="tabSelect" @close="closeHandle">
      <template slot="rightButton">
        <p class="design-tool">
          <el-tooltip effect="dark" content="打印" placement="top">
            <i class="el-icon-printer form-design-tool" @click="printTemplate(printDemoData)"></i>
          </el-tooltip>
        </p>
        <p class="design-tool" style="margin-left:10px;">
          <el-tooltip effect="dark" content="PDF下载" placement="top">
            <i class="el-icon-download form-design-tool" @click="downloadTemplate"></i>
          </el-tooltip>
        </p>
      </template>
    </design-header>
    <el-container class="print_template_main" v-show="currentTab == 'design'">
      <el-container>
        <!-- 展示区 -->
        <div id="hiprint-printTemplate" class="hiprint-printTemplate" style="display:none;"></div>
        <div id="preview_content" :style="'width: '+ paperWidth * 1 + 'mm;height: '+ paperHeight + 'mm;'"></div>
      </el-container>
    </el-container>
    <div class="print-loading" v-if="loading">
      <img :src="loadingImg" alt="">
    </div>
  </div>
</template>
<script>
import { getTemplate, getPrintFormData, getProcessOfFlow } from './api/index'

import { hiprint } from '../../../public/jvs-ui-public/plugin/hiprint/hiprint.bundle.js'
import { printDemoData } from '../../../public/jvs-ui-public/plugin/hiprint/custom_test/print-data.js'
import { demoPrintJson } from '../../../public/jvs-ui-public/plugin/hiprint/custom_test/custom-print-json.js'

import DesignHeader from "@/components/page-header/DesignHeader";
import loadingImg from '@/styles/loading.gif'

var hiprintTemplate;
export default {
  components: {DesignHeader},
  data () {
    return {
      loadingImg: loadingImg,
      designData: {},
      printDemoData: printDemoData,
      demoPrintJson: demoPrintJson,
      previewDialogStatus: false,
      paramsDrawerStatus: false,
      infoDialogStatus: false,
      fullDialogStatus: false,
      dataDialogStatus: false,
      infoTextarea: '',
      infoDialogTitle: '',
      paramsDrawerTitle: '',
      previewHtml: null,
      loading: false,
      paperWidth: 210,
      paperHeight: 296.6,
      printData: {},
      currentTab: 'design',
      tabList: [
        { name: 'design', label: '打印内容', icon: 'el-icon-set-up' },
      ],
      templateInfo: null,
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
      isMobileBool: false
    }
  },
  methods: {
    init() {
      let _demoPrintJson = {}
      if(this.designData.design) {
        _demoPrintJson = this.designData.design
      }
      hiprintTemplate = new hiprint.PrintTemplate({
        template: _demoPrintJson
      });
      hiprintTemplate.design('#hiprint-printTemplate');
    },
    /**
     * @Desc: 快速预览
     */
    previewTemplate () {
      this.previewDialogStatus=true;
    },
    /**
     * @Desc: 打开预览dialog回调
     */
    handlePreviewOpened (data) {
      $('#preview_content').html(hiprintTemplate.getHtml(data))
    },
    /**
     * @Desc: 打印
     */
    printTemplate () {
      hiprintTemplate.print(this.printData);
    },
    // 获取设计结构
    async getTemplateDesign (id) {
      this.loading = true
      await getTemplate(this.$route.query.jvsAppId, id).then(res => {
        if(res.data && res.data.code == 0 && res.data.data) {
          this.designData = res.data.data
          if(res.data.data.design) {
            this.$set(this.designData, 'design', JSON.parse(res.data.data.design))
            if(this.designData.design.panels && this.designData.design.panels.length > 0) {
              if(this.designData.design.panels[0].paperType) {
                this.$set(this, 'paperWidth', this.paperMap[this.designData.design.panels[0].paperType].width)
                this.$set(this, 'paperHeight', this.paperMap[this.designData.design.panels[0].paperType].height)
              }else{
                this.$set(this, 'paperWidth', this.designData.design.panels[0].width)
                this.$set(this, 'paperHeight', this.designData.design.panels[0].height)
              }
              this.designData.design.panels.filter(pa => {
                if(pa.printElements && pa.printElements.length > 0) {
                  pa.printElements.filter(pit => {
                    if(pit.options && pit.options.dataSourceType == 'workflowProgress') {
                      this.getFlowProcess(pit)
                    }
                  })
                }
              })
            }
          }
          this.loading = false
          this.init()
        }
      })
    },
    // 关闭
    closeHandle () {
      this.$emit('close', true)
    },
    // 获取数据
    getData () {
      // 示例数据
      this.printData = printDemoData
      this.handlePreviewOpened(this.printData)
      if(this.templateInfo.dataId) {
        getPrintFormData(this.$route.query.jvsAppId, this.templateInfo.dataModelId, this.templateInfo.designId, this.templateInfo.dataId).then(res => {
          if(res && res.data && res.data.code == 0) {
            let resData = res.data.data
            let realData = {}
            if(this.designData.design.panels) {
              this.designData.design.panels.filter(pan => {
                if(pan.printElements) {
                  pan.printElements.filter(pit => {
                    if(pit.options && pit.options.field) {
                      if(pit.options.field.includes('.')) {
                        let ks = pit.options.field.split('.')
                        let kda = null
                        for(let k in ks) {
                          if(k == 0) {
                            if(resData[ks[k]]) {
                              kda = resData[ks[k]]
                            }
                          }else if(k == ks.length - 1) {
                            if(kda && kda[ks[k]]) {
                              if(pit.printElementType && pit.printElementType.type == 'image') {
                                realData[pit.options.field] = typeof kda[ks[k]] == 'string' ? kda[ks[k]] : ((kda[ks[k]] && kda[ks[k]].length > 0) ? kda[ks[k]][0].url : '')
                              }else{
                                realData[pit.options.field] = kda[ks[k]]
                              }
                            }
                          }else{
                            if(kda && kda[ks[k]]) {
                              kda = kda[ks[k]]
                            }
                          }
                        }
                      }else{
                        if(pit.printElementType && pit.printElementType.type == 'image') {
                          realData[pit.options.field] = typeof resData[pit.options.field] == 'string' ? resData[pit.options.field] : ((resData[pit.options.field] && resData[pit.options.field].length > 0) ? resData[pit.options.field][0].url : '')
                        }else{
                          realData[pit.options.field] = resData[pit.options.field]
                        }
                      }
                    }
                  })
                }
              })
            }
            for(let rk in realData) {
              if(realData[rk]) {
                this.$set(this.printData, rk , realData[rk])
              }
            }
            this.handlePreviewOpened(this.printData)
          }
        })
      }
    },
    // tab选择结果
    tabSelect(val) {
      this.currentTab = val
      // if (val === 'permission') {
      //   this.$refs.permission.initData()
      // }
    },
    // 下载pdf
    downloadTemplate () {
      hiprintTemplate.toPdf(this.printData, this.designData.name, {scale: 2})
    },
    // 获取工作流进度
    getFlowProcess (item) {
      if(this.$route.query && this.$route.query.taskId) {
        getProcessOfFlow(this.$route.query.taskId).then(res => {
          if(res.data && res.data.code == 0) {
            // console.log('工作流进度数据。。。。。')
            // console.log(res.data.data)
            this.$set(this.printData, item.options.field, res.data.data)
          }
        })
      }
    },
    isMobile () {
      if(window.navigator.userAgent.match(/(phone|pad|pod|iPhone|iPod|ios|iPad|Android|Mobile|BlackBerry|IEMobile|MQQBrowser|JUC|Fennec|wOSBrowser|BrowserNG|WebOS|Symbian|Windows Phone)/i)) {
        return true; // 移动端
      }else{
        return false; // PC端
      }
    },
  },
  async created () {
    this.isMobileBool = this.isMobile() || false
    if(this.$route.query) {
      this.templateInfo = JSON.parse(JSON.stringify(this.$route.query))
      if(!this.$store.getters.access_token && this.$route.query.token) {
        this.$store.commit("SET_ACCESS_TOKEN", this.$route.query.token);
      }
      this.designData = JSON.parse(JSON.stringify(this.templateInfo))
      await this.getTemplateDesign(this.templateInfo.id)
      this.getData()
    }
    // this.$nextTick(() => {
    //   // 禁止右键
    //   document.oncontextmenu = new Function("event.returnValue=false");
    //   // 禁止F12
    //   document.onkeydown = new Function("event.returnValue=false");
    // });
  },
  mounted () {
    let self=this
    $(document).ready(function () {
      if(!self.loading) {
        self.init()
      }
    });
  }
}
</script>
<style lang="scss" scoped>
.hiprint-printTemplate {
  margin: 0;
  overflow-x: auto;
}
/deep/.el-header, /deep/.el-footer {
  background-color: #b3c0d1;
  color: #333;
  line-height: 60px;
}
</style>
<style lang="scss">
.print_template_container-print{
  position: relative;
  height: 100%;
  box-sizing: border-box;
  overflow: hidden;
  background: #f0f2f5;
  -webkit-box-sizing: border-box;
  .design-header-box{
    .header-right{
      .el-button--primary{
        display: none;
      }
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
    margin: 0;
    padding: 0;
    .form-design-tool{
      font-size: 20px;
      cursor: pointer;
      color: #353535;
      cursor: pointer;
      height: unset;
    }
    .icon {
      width: 20px;
      height: 20px;
      fill: currentColor;
      overflow: hidden;
      cursor: pointer;
    }
  }
  .print_template_main{
    position: absolute;
    width: 100%;
    height: calc(100% - 52px);
    overflow: hidden;
    overflow-y: auto;
    .el-container{
      // height: 100%;
      #preview_content{
        position: absolute;
        left: 0;
        right: 0;
        top: 0;
        bottom: 0;
        margin: 0 auto;
        background: #fff;
        .hiprint-printTemplate{
          background-color: #fff;
        }
      }
    }
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
}
.print_template_container-print-isMobile{
  .design-header-box{
    .header-left{
      word-break: keep-all;
      span{
        word-break: keep-all;
      }
      .el-icon-edit{
        display: none;
      }
    }
    .header-center{
      .el-tabs{
        display: none;
      }
    }
  }
  .print_template_main{
    overflow-x: auto;
  }
}
</style>