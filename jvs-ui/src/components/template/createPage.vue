<template>
  <div class="create-template-page">
    <div class="create-template-page-box">
      <div class="top">
        <h3>开始创建第一个页面</h3>
        <div>
          <!-- 数据统计图、报表、 -->
          点击下方模块快速搭建表单、列表页、流程配置等！
          <span @click="handleHelp('create-page-help')">如有疑问点击查看帮助>></span>
        </div>
      </div>
      <div class="center">
        <div class="item" v-for="(item, index) in cards" :key="'cards-'+index">
          <div class="up">
            <div class="up-item" v-for="(top, topIndex) in item.top" :key="'top-item-'+topIndex" @click="openTemplateList(top)">
              <img :src="top.img" alt="">
              <div>
                <h4>{{top.name}}</h4>
                <span>{{top.desc}}</span>
              </div>
            </div>
          </div>
          <div :class="{'bottom': true, 'bottom-only1': (item.bottom && item.bottom.length == 1)}">
            <div class="bottom-item" v-for="(bottom, boIndex) in item.bottom" :key="'bottom-item-'+boIndex" @click="openTemplateList(bottom)">
              <img :src="bottom.img" alt="">
              <div>
                <h4>{{bottom.name}}</h4>
                <span>{{bottom.desc}}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <el-dialog
      class="custom-header-dialog"
      width="50%"
      :title="title"
      :visible.sync="dialogVisible"
      append-to-body
      :before-close="handleClose">
      <div v-if="dialogVisible" class="custom-header-dialog-body-box">
        <jvs-form :formData="formData" :option="formOption" @submit="createSubmit" @cancalClick="handleClose">
          <template slot="iconForm">
            <div class="icon-image">
              <img v-if="formData.icon" :src="formData.icon" alt="">
              <el-button v-if="!formData.icon" size="mini" @click="handleFocus">选择图标</el-button>
              <i v-if="formData.icon" class="el-icon-delete" @click="formData.icon = ''"></i>
            </div>
          </template>
        </jvs-form>
      </div>
    </el-dialog>
    <!-- 新建逻辑 -->
    <create-rule-dialog ref="createRuleDialog" :jvsAppId="(catalogueItem && catalogueItem.extend) ? catalogueItem.extend.jvsAppId : ''"></create-rule-dialog>
    <!-- 新建模型 -->
    <createDataModelDialog
      ref="createDataModelDialog"
      :generateCrudDesign="true"
      :jvsAppId="(catalogueItem && catalogueItem.extend) ? catalogueItem.extend.jvsAppId : ''"
      @success="$emit('freshAllMenu', true)"
    ></createDataModelDialog>
    <!-- 选图片 -->
    <ImageSelect
      :dialogVisible="iconVisible"
      :title="'选择图标'"
      :defaultLabel="defaultLabel"
      :paramInfo="{bucketName: 'jvs-public', module: 'templatePage', label: '应用目录'}"
      @handleClose="iconClose"
      @handleConfirm="handleConfirm"
    />
  </div>
</template>
<script>
import { getStore } from '@/util/store.js'
import formImg from '@/styles/template/新建表单.png'
import reportImg from '@/styles/template/数据统计.png'
import dapingImg from '@/styles/template/数据大屏.png'
import oaImg from '@/styles/template/OA流程.png'
import dataImg from '@/styles/template/数据管理.png'
import customImg from '@/styles/template/自定义.png'
import ruleImg from '@/styles/template/创建逻辑.png'
import datamodelImg from '@/styles/template/创建模型.png'

import ImageSelect from '@/components/basic-assembly/ImageSelect'
import createRuleDialog from '@/components/template/createRuleDialog'
import createDataModelDialog  from '@/components/template/createDataModelDialog'

import {formColumn, pageColumn, flowColumn, chartColumn, customColumn} from './design'
import {addForm} from '@/views/page/api/formlist'
import * as pageApi from '@/views/page/api/newDesign'
import * as flowApi from '@/views/flowable/api/flowable'
import { createChartPage, createScreenPage, createReportPage  } from './api'
export default {
  name: 'create-page',
  components: { ImageSelect, createRuleDialog, createDataModelDialog },
  props: {
    catalogueItem: {
      type: Object
    }
  },
  data () {
    return {
      cards: [
        {
          top: [
            {
              img: formImg,
              name: '表单设计',
              desc: '快速信息搜集',
              type: 'form'
            },
            {
              img: ruleImg,
              name: '创建逻辑',
              desc: '灵活扩展业务互联',
              type: 'rule'
            },
            {
              img: datamodelImg,
              name: '创建模型',
              desc: '应用的数据结构和管理',
              type: 'dataModel'
            },
          ],
          bottom: [
            // {
            //   img: reportImg,
            //   name: '数据统计图',
            //   desc: '图表展示数据',
            //   type: 'chart'
            // },
          ]
        },
        {
          top: [
            {
              img: dataImg,
              name: '列表页设计',
              desc: '后台快速管理数据',
              type: 'page'
            },
            {
              img: oaImg,
              name: '流程配置',
              desc: '快速的信息搜集',
              type: 'workflow'
            },
            {
              img: customImg,
              name: '自定义页面',
              desc: '引用外部站点',
              type: 'customPage',
            }
          ],
          bottom: [
            // {
            //   img: dapingImg,
            //   name: '报表设计',
            //   desc: '报表展示数据',
            //   type: 'report'
            // },
          ]
        }
      ],
      dialogVisible: false,
      title: '',
      clickItem: null,
      formData: {},
      formOption: {
        emptyBtn: false,
        submitBtnText: '确定',
        submitLoading: false,
        formAlign: 'top',
        column: []
      },
      defaultLabel: '',
      iconVisible: false,
    }
  },
  computed: {
  },
  created () {},
  methods: {
    handleHelp(str) {
      this.$openUrl('', '_blank', str)
    },
    openTemplateList (item) {
      this.clickItem = item
      if(item.type) {
        switch(item.type) {
          case 'form':
            this.formOption.column = formColumn;
            this.title = '创建表单';
            this.createSubmit()
            break;
          case 'page':
            this.formOption.column = pageColumn;
            this.title = '创建列表页';
            this.createSubmit()
            break;
          case 'workflow':
            this.formOption.column = flowColumn;
            this.title = '创建流程';
            this.createSubmit()
            break;
          case 'customPage':
            this.formOption.column = customColumn;
            this.formOption.column.filter(col => {
              if(col.prop == 'url') {
                col.dicUrl = `/mgr/jvs-auth/environment/variable/all/text/${getStore({ name: 'modeUserInfo' }).mode}`
              }
            });
            this.title = '自定义页面';
            this.dialogVisible = true
            break;
          case 'rule':
            this.$refs.createRuleDialog.init();
            break;
          case 'dataModel':
            this.$refs.createDataModelDialog.init();
            break;
          case 'chart':
            this.formOption.column = chartColumn;
            this.title = '创建图表';
            this.createSubmit()
            break;
          case 'screen':
            this.title = '创建数据大屏';
            this.createSubmit()
            break; 
          case 'report':
            this.title = '创建报表';
            this.createSubmit()
            break;
          default: ;break;
        }
      }else{
        // this.$message.warning('此功能未开放，敬请期待！')
        this.$notify({
          title: '提示',
          message: '此功能未开放，敬请期待！',
          position: 'bottom-right',
          type: 'warning'
        });
      }
    },
    handleClose () {
      this.dialogVisible = false
      this.clickItem = null
      this.formData = {}
    },
    createSubmit () {
      this.formOption.submitLoading = true
      let func = null
      this.formData = Object.assign(this.formData, {
        jvsAppId: this.catalogueItem.extend.jvsAppId,
        type: this.catalogueItem.id,
        sort: this.catalogueItem.children ? this.catalogueItem.children.length : 0
      });
      switch(this.clickItem.type) {
        case 'form':
          func = addForm;
          break;
        case 'page':
          func = pageApi.createPage;
          break;
        case 'workflow':
          func = flowApi.createModel;
            this.formData = Object.assign(this.formData, {
            jvsAppId: this.catalogueItem.extend.jvsAppId
          });
          break;
        case 'chart':
          func = createChartPage;
          break;
        case 'customPage':
          func = pageApi.createCustomPage;
          break;
        case 'screen':
          this.formData.name = '未命名大屏'
          func = createScreenPage;
          break;
        case 'report':
          func = createReportPage;
          break;
        default: ;break;
      }
      if(func) {
        func(this.formData).then(res => {
          if(res.data.code == 0 && res.data.data) {
            let str = ''
            switch(this.clickItem.type) {
              case 'form':
                if(res.data.data.id && res.data.data.dataModelId) {
                  str = location.origin + (`/page-design-ui/#/form?jvsAppId=${this.catalogueItem.extend.jvsAppId}&id=`+res.data.data.id + (res.data.data.dataModelId ? `&dataModelId=${res.data.data.dataModelId}` : ''));
                }
                break;
              case 'page':
                if(res.data.data.id && res.data.data.dataModelId) {
                  str = location.origin + (`/page-design-ui/#/crud/design?jvsAppId=${this.catalogueItem.extend.jvsAppId}&id=`+res.data.data.id + (res.data.data.dataModelId ? `&dataModelId=${res.data.data.dataModelId}` : ''));
                }
                break;
              case 'workflow':
                str = location.origin + `/flowable-ui/#/processDesign?id=${res.data.data.id}&jvsAppId=${this.catalogueItem.extend.jvsAppId}`
                break;
              case 'chart':
                if(res.data.data.id) {
                  str = location.origin + ('/chart-design-ui/#/chartDesign?id='+res.data.data.id+`&jvsAppId=${this.catalogueItem.extend.jvsAppId}`);
                }
                break;
              case 'screen':
                if(res.data.data.id) {
                  str = location.origin + ('/data-screen-ui/#/datascreendesign?id=' + res.data.data.id)
                }
                break;
              case 'report':
                if(res.data.data.id) {
                  str = location.origin + ('/chart-design-ui/#/data-report/datareportdesign?id='+res.data.data.id+`&jvsAppId=${this.catalogueItem.extend.jvsAppId}`);
                }
                break;
              default: ;break;
            }
            this.$emit('freshAllMenu', true)
            this.handleClose()
            this.formOption.submitLoading = false
            if(str) {
              let _this = this
              setTimeout( () => {
                _this.$openUrl(str, '_blank')
              }, 1000)
            }
          } else {
            this.formOption.submitLoading = false
          }
        }).catch(e => {
          this.formOption.submitLoading = false
        })
      }
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
      this.$set(this.formData, 'icon', obj.fileLink)
      this.iconClose()
    },
  }
}
</script>
<style lang="scss" scoped>
.create-template-page{
  width: 100%;
  height: 100%;
  overflow: hidden;
  position: relative;
  .create-template-page-box{
    width: 928px;
    height: 75%;
    position: absolute;
    left: 0;
    right: 0;
    top: 0;
    bottom: 0;
    margin: auto;
    .top{
      text-align: left;
      h3{
        height: 23px;
        font-family: Source Han Sans-Bold, Source Han Sans;
        font-weight: 700;
        font-size: 16px;
        color: #363B4C;
        line-height: 23px;
        margin: 0;
      }
      div{
        margin-top: 8px;
        height: 20px;
        font-family: Source Han Sans-Regular, Source Han Sans;
        font-weight: 400;
        font-size: 14px;
        color: #6F7588;
        line-height: 20px;
      }
      span{
        color: #1E6FFF;
        cursor: pointer;
      }
    }
    .center{
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 3%;
      .item{
        width: 49%;
        text-align: center;
        margin-left: 16px;
        .up-item, .bottom-item{
          display: flex;
          align-items: center;
          justify-content: center;
          cursor: pointer;
          overflow: hidden;
          img{
            display: block;
          }
          >div{
            text-align: left;
            margin-left: 5%;
            width: 33%;
          }
          h4{
            height: 20px;
            font-family: Source Han Sans-Bold, Source Han Sans;
            font-weight: 700;
            font-size: 14px;
            color: #363B4C;
            line-height: 20px;
            margin: 0;
          }
          span{
            display: block;
            height: 20px;
            font-family: Source Han Sans-Regular, Source Han Sans;
            font-weight: 400;
            font-size: 14px;
            color: #6F7588;
            line-height: 20px;
            margin-top: 12px;
          }
        }
        .up{
          .up-item{
            width: 100%;
            height: 20%;
            border-radius: 4px;
            border: 1px solid #EEEFF0;
            flex-direction: row;
            box-sizing: border-box;
            img{
              width: 32%;
              height: 32%;
            }
            &:nth-of-type(1){
              background: linear-gradient( 178deg, #E4EDFF -10%, #FFFFFF 40%);
            }
          }
        }
        .bottom{
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-top: 20px;
          .bottom-item{
            width: 220px;
            height: 208px;
            flex-direction: column;
            background: #FFFFFF;
            border-radius: 4px;
            border: 1px solid #EEEFF0;
            margin-left: 16px;
            text-align: center;
            >div{
              margin-left: 0;
            }
            img{
              width: 96px;
              height: 96px;
            }
            span{
              margin-top: 8px;
            }
          }
          .bottom-item:nth-of-type(1){
            margin-left: 0;
          }
          &.bottom-only1{
            .bottom-item{
              width: 456px;
              flex-direction: row;
              >div{
                text-align: left;
                margin-left: 24px;
              }
              img{
                width: 144px;
                height: 144px;
              }
              span{
                margin-top: 12px;
              }
            }
          }
        }
        .up-item+.up-item{
          margin-top: 16px;
        }
      }
      .item:nth-of-type(1){
        margin-left: 0;
      }
    }
  }
}
.custom-header-dialog-body-box{
  height: 100%;
  overflow: hidden;
  /deep/.jvs-form{
    margin-top: 16px;
    height: calc(100% - 16px);
    .el-row{
      display: flex;
      flex-direction: column;
      height: 100%;
      .form-column-tableForm{
        flex: 1;
        overflow: hidden;
        overflow-y: auto;
      }
    }
    .el-form-item:not(.form-btn-bar) {
      margin-left: 20px;
      margin-right: 20px;
    }
    .el-form-item__label{
      font-family: Microsoft YaHei-Bold, Microsoft YaHei;
      font-weight: 700;
      font-size: 14px;
      color: #363B4C;
    }
    .form-item-btn{
      border-top: 1px solid #EEEFF0;
      height: 60px;
      line-height: 60px;
      .form-btn-bar{
        height: 100%;
        margin: 0;
        margin-right: 24px;
        display: flex;
        justify-content: flex-end;
        align-items: center;
        .el-form-item__content{
          line-height: 60px;
          display: flex;
          flex-direction: row-reverse;
          .el-button{
            margin-left: 16px;
          }
        }
      }
    }
  }
}
</style>
