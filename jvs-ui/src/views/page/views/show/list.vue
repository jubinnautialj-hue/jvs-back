<template>
  <div class="table-show">
    <!-- 无权限 -->
    <div v-if="!permission" class="permission">
      <img src="@/const/img/permission.png" alt=""/>
      <span>暂无访问权限</span>
    </div>
    <!-- 空设计 -->
    <div v-if="emptyView" class="empty-view">
      <img src="/jvs-ui-public/img/contentEmpty.png" alt=""/>
      <div>暂无内容</div>
    </div>
    <!-- 引用页面 -->
    <iframe v-if="iframepage && iframepage.address && iframepage.position == 'top'" frameborder="no" border="0" marginwidth="0" marginheight="0" scrolling="auto" width="100%" :src="iframepage.address" :style="'height:'+(iframepage.height*clientHeight / 100 )+ 'px'"/>
    <jvs-table
      v-if="!emptyView"
      :key="alreadLoad"
      refs="multipleTable"
      :index="indexAbled"
      :loading="tableLoading"
      :pageheadertitle="menuName"
      :isClearSelect="isClearSelect"
      :page="page"
      :selectable="selectable"
      :option="option"
      :data="tableData"
      :class="{'menu-table': option.menu, 'jvs-table-leftTree': tableShowType == 'leftTree', 'jvs-table-card': tableShowType == 'card', 'jvs-table-leftTree-hide': (tableShowType == 'leftTree' && closeLeftTree)}"
      :jvsAppId="$route.query.jvsAppId"
      :sortsList="sortsList"
      :displayType="tableShowType"
      :showGantt="showGantt"
      :ganttOption="(data && data.ganttForm) ? data.ganttForm : null"
      :fromView="true"
      :noStyleHeight="true"
      @on-load="getListData"
      @selection-change="selectChange"
      @openModelDisplay="modelDisplayHandle"
      @openForm="openFormHandle">
      <template slot="headerTop">
        <div>
          <!-- 描述信息 -->
          <div v-if="data && data.description" style="margin-bottom: 20px;">
            <el-alert :closable="false" type="info">
              <template slot="title">
                <div class="alert-box" style="display:flex;align-items:center;">
                  <i class="el-icon-info"/>
                  <div v-html="data.description"></div>
                </div>
              </template>
            </el-alert>
          </div>
          <!-- 查询条件 -->
          <jvs-form v-if="query.length > 0 && searchFormType == 'top'" class="search-form search-form-inline" :option="lineFormOption(searchOption)" :formData="queryParams" :isSearch="true" @submit="queryHandle" @reset="resetQueryHandle" :designId="$route.query.id || ''" :jvsAppId="$route.query.jvsAppId">
            <template v-for="sitem in queryBeforeSlot" :slot="sitem.prop+'Before'">
              <el-select v-model="queryOprator[sitem.prop]" :disabled="sitem.disabled" placeholder="请选择" :key="sitem.prop+'-before-search'" class="bofore-append" size="mini">
                <el-option v-for="op in sitem.list" :key="sitem.prop+'-option-item-'+op.value" :label="op.label" :value="op.value"></el-option>
              </el-select>
            </template>
          </jvs-form>
        </div>
      </template>
      <!-- 顶部按钮 -->
      <template slot="menuLeft">
        <div class="list-use-top-left" style="display: flex;">
          <span v-for="(item, index) in topBtns" :key="item.name+index" style="margin-left: 16px;display:flex;" v-show="showhidebutton(item, {}) && !(item.enable === false)">
            <jvs-button v-if="item.type == 'btn_import'" :type="index == 0 ? 'primary' : 'text'" icon="el-icon-upload2" size="mini">{{item.name}}</jvs-button>
            <jvs-button size="mini" :type="index == 0 ? 'primary' : 'text'" :icon="getBtnIcon(item.type)" v-if="['btn_import', 'btn_print'].indexOf(item.type) == -1" @click="btnClickHandle(null, index, item)">{{item.name}}</jvs-button>
          </span>
          <span v-if="enableDeleteList && enableDeleteList.length > 0" style="margin-left: 16px;display:flex;">
            <jvs-button size="mini" type="text" icon="el-icon-delete">删除</jvs-button>
          </span>
        </div>
      </template>
      <!-- 顶部sql统计 -->
      <template slot="menuRight">
        <el-row class="table-show-right-tool">
          <!-- 顶部sql统计 -->
          <p v-if="customStatistics && customStatistics.length > 0" class="search-p">
            <el-alert
              v-for='(item,index) in customStatistics'
              :key="index+item.name"
              :closable='false'
              :center='true'
              style='width:200px;margin:0 0px 0px 10px;float:right;background: none;padding:0;'
              :title="item.name+':'+ item.sql "
              type="success">
            </el-alert>
          </p>
          <!-- 表格工具 -->
          <p v-if="query.length > 0 && searchFormType == 'right' && !propData.hiddenSearch" class="search-p">
            <el-input placeholder="请输入关键词" prefix-icon="el-icon-search" size="mini" v-model="searchKeyword" clearable @blur="getListData();" @clear="getListData();" @keyup.enter.native="getListData();"></el-input>
          </p>
          <p v-if="query.length > 0 && searchFormType == 'right' && !propData.hiddenSearch">
            <el-popover
              v-model="queryShow"
              placement="right"
              :width="Number(searchWidth)+10"
              trigger="click">
              <div style="padding: 20px 20px 10px 30px;">
                <jvs-form class="search-form" :option="searchOption" :formData="queryParams" @submit="queryHandle" @reset="resetQueryHandle" :designId="$route.query.id || ''" :jvsAppId="$route.query.jvsAppId">
                  <template v-for="sitem in queryBeforeSlot" :slot="sitem.prop+'Before'">
                    <el-select v-model="queryOprator[sitem.prop]" :disabled="sitem.disabled" placeholder="请选择" :key="sitem.prop+'-before-search'" class="bofore-append" size="mini">
                      <el-option v-for="op in sitem.list" :key="sitem.prop+'-option-item-'+op.value" :label="op.label" :value="op.value"></el-option>
                    </el-select>
                  </template>
                </jvs-form>
              </div>
              <span slot="reference" style="font-size: 14px;position: relative;">
                <i class="el-icon-position" :style="`cursor:pointer;transform: rotate(135deg);position: absolute;left: 0;top: 0;`"></i>
                <span style="margin-left: 14px;">筛选</span>
              </span>
            </el-popover>
          </p>
          <p v-if="getSortColumn(option.column).length > 0 && !propData.hiddenSort">
            <el-popover
              v-model="sortShow"
              placement="right"
              width="400"
              trigger="click">
              <div style="padding: 8px 10px;">
                <h5 style="margin:0;padding:0;">排序</h5>
                <span style="font-size:12px;line-height:18px;">以下排序规则从上到下按序执行</span>
                <span style="cursor:pointer;margin-top:10px;display:block;color:#3471FF;">
                  <i class="el-icon-circle-plus-outline"></i>
                  <span @click="addSortHandle">添加排序规则</span>
                </span>
                <div style="margin-top:10px;">
                  <el-row v-for="(sit, six) in sortsList" :key="'sort-item-'+six" style="display:flex;align-items:center;margin-top:10px;">
                    <el-select v-model="sit.fieldKey" placeholder="请选择字段" size="mini" style="margin-right:10px;">
                      <el-option
                        v-for="item in getSortColumn(option.column)"
                        :key="item.prop"
                        :label="item.label"
                        :value="item.prop">
                      </el-option>
                    </el-select>
                    <el-radio-group v-model="sit.direction">
                      <el-radio label="ASC">升序</el-radio>
                      <el-radio label="DESC">降序</el-radio>
                    </el-radio-group>
                  </el-row>
                  <el-row style="display:flex;justify-content:center;align-items:center;margin-top:10px;">
                    <el-button size="mini" type="primary" @click="sortQuerySubmit">确定</el-button>
                    <el-button size="mini" @click="clearSort">清空</el-button>
                  </el-row>
                </div>
              </div>
              <span slot="reference" style="font-size: 14px;">
                <i class="el-icon-sort" style="cursor:pointer;"></i>
                <span>排序</span>
              </span>
            </el-popover>
          </p>
          <p style="font-size: 14px;" v-if="!propData.hiddenRefresh">
            <i class="el-icon-refresh" style="cursor:pointer;"></i>
            <span>刷新</span>
          </p>
          <p v-if="query.length > 0 && !propData.hiddenSearch" @click="changeQueryShow" style="font-size: 14px;">
            <i :class="{'el-icon-arrow-down': searchFormType== 'right', 'el-icon-arrow-up': searchFormType == 'top'}" style="cursor:pointer;"></i>
            <span>{{searchFormType == 'right' ? '展开' : '收起'}}</span>
          </p>
          <!-- <p>
            <span>{{indexAbled ? '关闭' : '显示'}}序号</span>
            <el-switch size="mimi" v-model="indexAbled"></el-switch>
          </p> -->
        </el-row>
      </template>
      <template slot="tableTop">
        <!-- 检索规则 -->
        <div v-if="tableShowType != 'leftTree' && retrievalOption && retrievalOption.length > 0" class="list-use-body-top">
          <div class="left" v-if="retrievalOption && retrievalOption.length > 0">
            <div :class="{'left-item': true, 'active': !retrievalValue}" @click="retrievalSearch({label: '全部', value: ''})">全部</div>
            <div :class="{'left-item': true, 'active': item[retrievalProps.value] == retrievalValue}" v-for="item in retrievalOption" :key="'retrieval-item-'+item[retrievalProps.value]" @click="retrievalSearch(item)">{{item[retrievalProps.label]}}</div>
          </div>
          <div class="right"></div>
        </div>
      </template>
      <!-- 行内按钮 -->
      <template slot="menu" slot-scope="scope">
        <div v-if="tableShowType == 'card'">
          <div v-if="lineBtns && lineBtns.length > 0">
            <jvs-button :style="item.type === 'btn_delete' ? 'color: #F56C6C;' : ''" size="mini" type="text" v-for="(item, index) in lineBtns" :key="item.name+index" v-if="showhidebutton(item, scope.row)" @click="btnClickHandle(scope.row, scope.index, item)">{{item.name}}</jvs-button>
          </div>
        </div>
        <div v-else>
          <div v-if="scope.row.jvsEnabledButtons ? (scope.row.jvsEnabledButtons.length > 0 && scope.row.jvsEnabledButtons.length < 4) : (lineBtns && lineBtns.length > 0 && lineBtns.length < 4)">
            <jvs-button :style="item.type === 'btn_delete' ? 'color: #F56C6C;' : ''" size="mini" type="text" v-for="(item, index) in lineBtns" :key="item.name+index" v-if="showhidebutton(item, scope.row)" @click="btnClickHandle(scope.row, scope.index, item)">{{item.name}}</jvs-button>
          </div>
          <div v-if="scope.row.jvsEnabledButtons ? (scope.row.jvsEnabledButtons && scope.row.jvsEnabledButtons.length > 3) : (lineBtns && lineBtns.length > 3)">
            <jvs-button :style="lineBtns[index].type === 'btn_delete' ? 'color: #F56C6C;' : ''" size="mini" type="text" v-for="(item, index) in lineBtns" :key="lineBtns[index].name+index" v-if="showhidebutton(lineBtns[index], scope.row, 'out', index)" @click="btnClickHandle(scope.row, scope.index, lineBtns[index])">{{lineBtns[index].name}}</jvs-button>
            <el-popover
              v-show="showMore(scope.row, lineBtns)"
              placement="bottom"
              popper-class="line-button-pover"
              trigger="hover">
              <div class="line-button-list-more">
                <div class="more-item" v-for="(item, index) in lineBtns" :key="'more-item-button-'+index" v-show="showhidebutton(item, scope.row, 'in', index)" @click="btnClickHandle(scope.row, scope.index, item)">
                  <jvs-button :style="item.type === 'btn_delete' ? 'color: #F56C6C;' : ''" size="mini" type="text" v-if="index > 1" :key="item.name+'-more-'+index">{{item.name}}</jvs-button>
                </div>
              </div>
              <el-button slot="reference" size="mini" type="text" style="margin-left:10px;">更多</el-button>
            </el-popover>
          </div>
        </div>
      </template>
      <!-- 标签插槽 -->
      <template v-for="(item, key) in getSlotData(option.column)" :slot="item.prop" slot-scope="scope">
        <div :key="item.prop+'-slot-item'" class="page-slot-column-div" style="width: 100%;">
          <el-popover
            v-model="slotPopoverVisible[item.prop+scope.$index]"
            v-if="['tableForm', 'reportTable', 'formbox', 'childrenForm', 'connectForm', 'timeline'].indexOf(item.type) > -1"
            placement="right"
            width="300"
            trigger="click">
            <div class="slot-component-item" style="padding: 10px;">
              <jvs-form :option="getSlotItemCom(item)" :formData="scope.row"></jvs-form>
            </div>
            <div slot="reference" :style="'height: 20px;line-height: 20px;padding: 0 5px;border-radius: 4px;font-size: 14px;cursor: pointer;' + (item.backColor && `background:${item.backColor};border: 0;`)">
              <span :class="{'back-color-text': (item.color && !item.color.startsWith('#'))}" :style="(item.color && (item.color.startsWith('#') ? `color: ${item.color}` : `background:${item.color};`))">{{getSlotItemLabel(scope.row[item.prop], item)}}</span>
            </div>
          </el-popover>
          <el-popover
            v-model="slotPopoverVisible[item.prop+scope.$index]"
            v-else-if="['htmlEditor'].indexOf(item.type) > -1"
            placement="bottom"
            trigger="click">
            <div style="max-width: 70vw;max-height: 50vh;overflow: auto;padding: 0 10px;box-sizing: border-box;">
              <div v-html="scope.row[item.prop+'_1'] || scope.row[item.prop]"></div>
            </div>
            <div slot="reference" :style="'height: 20px;line-height: 20px;padding: 0 5px;border-radius: 4px;font-size: 12px;cursor: pointer;' + (item.backColor && `background:${item.backColor};border: 0;`)">
              <span :class="{'back-color-text': (item.color && !item.color.startsWith('#'))}" :style="(item.color && (item.color.startsWith('#') ? `color: ${item.color}` : `background:${item.color};`))">{{getSlotItemLabel(scope.row[item.prop], item)}}</span>
            </div>
          </el-popover>
        </div>
      </template>
    </jvs-table>
    <div v-if="tableShowType == 'leftTree'" v-show="!closeLeftTree" :class="{'jvstable-left-tree': true, 'with-search': (retrievalItem && retrievalItem.datatype == 'dataModel')}">
      <div v-if="retrievalItem && retrievalItem.datatype == 'dataModel'"  class="search-input">
        <el-input v-model="treeKeyword" size="mini" :placeholder="`输入${(retrievalItem.props && retrievalItem.props.text) ? retrievalItem.props.text : '关键词'}搜索`" clearable @input="treeKeywordChange"></el-input>
      </div>
      <div v-show="propData ? (!emptyView) : true"
        :class="{
        'treeBox-title': true,
        'treeBox-title-check': !retrievalValue,
        'treeBox-title-expandall': (retrievalColumn && retrievalColumn.retrievalOption && retrievalColumn.retrievalOption.expandAll)}"
        @click="retrievalSearch({label: '全部', value: ''})">
        <span v-if="retrievalColumn && retrievalColumn.retrievalOption && retrievalColumn.retrievalOption.expandAll" :class="{'allIcon el-icon-caret-right': true, 'close-all': !expandAll}" style="color: #868BA1;padding: 6px;" @click.stop="expandAll = !expandAll"></span>
        <span>{{(retrievalColumn && retrievalColumn.retrievalOption && retrievalColumn.retrievalOption.allLabel) ? retrievalColumn.retrievalOption.allLabel : '全部'}}</span>
      </div>
      <div v-if="leftTreeLoading" class="left-tree-loading"></div>
      <el-tree
        v-show="propData ? (!emptyView) : true"
        v-else
        ref="jvsTableLeftTree"
        :key="expandAll"
        :data="retrievalOption"
        :node-key="retrievalProps.id ? retrievalProps.id : retrievalProps.value"
        :default-expand-all="expandAll"
        :expand-on-click-node="false"
        :loading="leftTreeLoading"
        :props="retrievalProps"
        @node-click="handleNodeClick"
      >
        <span class="customize-tree-node" slot-scope="{ node, data }">
          <span>
            <span :class="'customize-tree-node-label customize-tree-node-label'+(data.level ? data.level : 0)" :style="'width:'+((193 - (18 * (data.level ? data.level : 0))) > 0 ? (193 - (18 * (data.level ? data.level : 0))) : 0) +'px;'">{{data[retrievalProps.label]}}</span>
          </span>
          <span v-if="(data.extend && data.extend.jvsEnabledButtons && data.extend.jvsEnabledButtons.length > 0) ? true : treeBtns && treeBtns.length > 0" class="more-icon">
            <el-popover
              popper-class="hover-popver-list"
              placement="right"
              width="50"
              v-model="data.moretool"
              trigger="click">
              <ul class="base-type-list">
                <li v-for="(tb, tix) in treeBtns" :key="'tree-button-'+tix" v-show="(data.extend && data.extend.jvsEnabledButtons && data.extend.jvsEnabledButtons.length > 0) ? data.extend.jvsEnabledButtons.indexOf(tb.permissionFlag) > -1 : true" @click.stop="() => leftTreeClick(node, data, tix, tb)">
                  <!-- <i class="el-icon-setting iconhover"></i> -->
                  <span>{{tb.name}}</span>
                </li>
              </ul>
              <i slot="reference" class="el-icon-more iconhover" @click.stop="moreLeftTree(data)"></i>
            </el-popover>
          </span>
        </span>
      </el-tree>
    </div>
    <div v-if="tableShowType == 'leftTree' && (retrievalColumn && retrievalColumn.retrievalOption && retrievalColumn.retrievalOption.closeEnable)"
      :class="{'close-tree-tool': true, 'close-tree-tool-hide': closeLeftTree}"
      @click="closeLeftTree = !closeLeftTree">
      <span v-if="closeLeftTree" class="el-icon-arrow-right"></span>
      <span v-else class="el-icon-arrow-left"></span>
    </div>
    <!-- 引用页面 -->
    <iframe v-if="iframepage && iframepage.address && iframepage.position == 'bottom'" frameborder="no" border="0" marginwidth="0" marginheight="0" scrolling="auto" width="100%" :src="iframepage.address" :style="'height:'+(iframepage.height*clientHeight / 100 )+ 'px'"/>
    <!-- 表单 -->
    <el-dialog
      :title="formTitle"
      v-if="formVisible"
      :visible.sync="formVisible"
      :fullscreen="(formType == 'normalForm' || formType == 'detailForm') ? formOption.fullscreen: false"
      :class="{'form-fullscreen-dialog':(formType == 'normalForm' || formType == 'detailForm') ? (formOption.fullscreen ? hasTabItem(formOption.fullscreen) : formOption.fullscreen): true}"
      append-to-body
      :close-on-click-modal="false"
      :before-close="handleCloseForm">
      <jvs-form class="show-form" v-if="(formType == 'normalForm' || formType == 'detailForm') && formVisible" :option="formOption" :formData="formData" :rowData="rowData" :designId="itemData.formId" @submit="formSubmit">
        <!-- 自定义按钮 -->
        <template slot="formButton" v-if="formOption.flag">
          <jvs-button v-if="hasPrint" size="mini" @click="printHandle">打印</jvs-button>
          <jvs-button size="mini" v-for="(item, index) in formOption.btnSetting" :key="item.name+'slotbtn'+index" @click="slotbtnClickHandle(item, index)">{{item.name}}</jvs-button>
        </template>
        <template v-else slot="formButton">
          <jvs-button v-if="hasPrint" size="mini" @click="printHandle">打印</jvs-button>
        </template>
      </jvs-form>
      <jvs-form-level v-if="formType == 'multiLevelForm'" :option="formOption" :formData="formData" @submit="formSubmit">
      </jvs-form-level>
    </el-dialog>
    <!-- 列打开关联显示信息 -->
    <el-dialog :visible.sync="modelDisplayShow" :modal="false" custom-class="popover-dialog">
      <div v-if="modelDisplayShow" class="el-popover el-popper el-popover--plain modelDisplay-box" x-placement="bottom" :style="`left: ${modelDisplayPos.x}px;top: ${modelDisplayPos.y}px;`" @click.stop="stopHandle" v-click-outside="modelDisplayClose">
        <div class="slot-component-item" style="padding: 10px 10px 0 10px;overflow: auto;max-width: 50vw;max-height: 500px;">
          <div v-loading="linkModelLoading" class="show-other-model-info-table">
            <div class="header-body">
              <div class="header">
                <div v-for="cit in modelDisplayItem.modelDisplay.linkageFieldKeys" :key="'model-coloumn-item-'+cit.prop" class="item" :style="`${(cit.advancedSettings && cit.advancedSettings.showWidth > 0) ? ('width:'+cit.advancedSettings.showWidth+'px;') : ''}`">{{cit.label}}</div>
              </div>
              <div v-if="linkModelData && linkModelData.length > 0" class="body">
                <div v-for="(dit, dix) in linkModelData" :key="'model-column-item-data-row'+'-'+dix" class="body-item">
                  <div v-for="(dct, dcx) in modelDisplayItem.modelDisplay.linkageFieldKeys" :key="'model-column-item-data-row-item'+'-'+dix+dct.prop+'-'+dcx" class="body-item-col" :style="`${(dct.advancedSettings && dct.advancedSettings.showWidth > 0) ? ('width:'+dct.advancedSettings.showWidth+'px;') : ''}`">
                  </div>
                </div>
              </div>
              <div v-else class="empty">
                <span>暂无数据</span>
              </div>
            </div>
            <div class="footer">
              <el-pagination
                background
                :layout="linkModelPage.layout"
                :total="linkModelPage.total"
                :current-page="linkModelPage.currentPage"
                :page-sizes="linkModelPage.pageSizes"
                :page-size="linkModelPage.pageSize"
                @size-change="val => {linkModelPage.pageSize=val;queryLinkModelData(modelDisplayItem, modelDisplayRow);}"
                @current-change="val => {linkModelPage.currentPage=val;queryLinkModelData(modelDisplayItem, modelDisplayRow);}"
              ></el-pagination>
            </div>
          </div>
        </div>
        <div class="popper__arrow"></div>
      </div>
    </el-dialog>

    <!-- popover遮罩 -->
    <div v-show="popoverVisible" class="popover-visible-box" @click="slotPopoverVisible={};"></div>
  </div>
</template>
<script>
import {sendRequire, sendMyRequire, getKeyValue} from '../../api/list'
import {
  delSingleData,
  downloadTemplate,
  exportData,
  getSingleData,
  previewPage
} from '../../api/design'
import {getSystemDictItems} from '@/api/newDesign'
import {getCrudDataPage, getStatistics} from '../../api/newDesign'
import pinyin from 'js-pinyin'
import store from "@/store";
import {getFormInfo} from '../../api/formlist'
import { getPlugins } from '@/api/common'
import { dateFormat } from '@/util/date'
export default {
  props: {
    propData: {
      type: Object
    },
    infoData: {
      type: Object
    },
    menuForm: {
      type: Object
    }
  },
  computed: {
    popoverVisible () {
      let bool = false
      if(this.slotPopoverVisible) {
        for(let k in this.slotPopoverVisible) {
          if(this.slotPopoverVisible[k] === true) {
            bool = true
          }
        }
      }
      return bool
    }
  },
  data () {
    return {
      permission: true,
      menuId: '',
      data: {},
      option: {
        page: true,
        // align: 'center',
        // menuAlign: 'center',
        viewBtn: false,
        addBtn: false,
        editBtn: false,
        // viewBtn: false,
        delBtn: false,
        search: true,
        showOverflow: true,
        cancal: false,
        menuWidth: 150,
        column: [],
      },
      tableData: [],
      tableLoading: false,
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
        pageSizes: [20, 50, 100, 200, 500, 1000], // 分页大小
      },
      selectable: false, // 是否可以多选
      // 查询条件配置
      queryParams: {},
      searchOption: {
        isSearch: true,
        cancal: false,
        submitBtnText: '查询',
        column: [],
        labelWidth: 'auto',
      },
      queryBeforeSlot: [],
      queryOprator: {},
      column: [], // 字段集
      topBtns: [], // 顶部按钮
      lineBtns: [], // 行内按钮
      fileList: [], // 导入 文件列表
      importHeaders: {},
      query: [], // 查询条件
      customStatistics: [], // 自定义统计
      iframepage: {}, // 嵌套页面
      clientHeight: 0,
      // 表单
      formType: '',
      formTitle: '',
      formVisible: false,
      formData: {},
      formOption: {
        column: []
      },
      action: '',
      // 默认请求
      developmentConfig: {},
      rowData: {}, // 行数据
      itemData: {}, // 当前按钮
      selectList: [], // 多选行数据
      isClearSelect: 0, // 是否清空选择, 随机数
      selectFormItems: [], // 表单里的下拉选择项
      menuName: '',
      preHttp: {},
      demoData: {}, // 示例数据
      loadTimes: -1,
      alreadLoad: false,
      pathQuery: {},
      formDesignId: '', // 表单id
      dataId: '',
      hasPrint: false,
      currentQuery: null,
      sortsList: [], // 排序条件
      sortShow: false,
      queryShow: false,
      tableShowType: 'table',
      jvsAppId: '',
      indexAbled: false,
      emptyView: false,
      linkModelLoading: false,
      linkModelPage: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
        pageSizes: [20, 50, 100, 200, 500, 1000], // 分页大小
      },
      linkModelData: [],
      searchFormType: 'right',
      enableDeleteList: [],
      searchWidth: 350,
      searchKeyword: '', // 搜索关键词
      retrievalColumn: null,
      expandAll: true,
      closeLeftTree: false,
      retrievalItem: null, // 检索字段项
      retrievalValue: '', // 检索字段值
      retrievalOption: [], // 字段检索值范围选项
      retrievalProps: {
        label: 'label',
        value: 'value',
        children: 'children'
      },
      leftTreeLoading: true,
      lastLeftNode: null,
      editRetrievalItem: null,
      treeKeyword: '',
      treeBtns: [], // 左树按钮
      checkedNode: '',
      jvsQueryData: {},
      modelDisplayShow: false,
      modelDisplayItem: null,
      modelDisplayRow: null,
      modelDisplayPos: {
        x: 0,
        y: 0
      },
      showGantt: false,
      slotPopoverVisible: {}
    }
  },
  created () {
    this.jvsQueryData = JSON.parse(JSON.stringify(this.$route.query))
    if(this.$store.getters && this.$store.getters.labelValue) {
      this.labelValue = this.$store.getters.labelValue
    }else{
      this.getKeyValueHandle()
    }
    if(this.menuForm) {
      this.menuName = this.menuForm.menuName
    }
    // 预览
    if(this.propData) {
      this.previewInit()
    }else{
      // 在线
      this.initQueryInfo()
    }
    this.clientHeight = document.documentElement.clientHeight
  },
  methods: {
    getSlotData(arr) {
      return arr.filter(item => {
        // item.backColor ||  || item.openFormId || (item.modelDisplay && item.modelDisplay.displayType == 'model' && item.modelDisplay.showModelType == 'oneToMany')
        return item.slot
      })
    },
    // 预览初始化
    previewInit () {
      this.alreadLoad = false
      this.data = JSON.parse(JSON.stringify(this.propData))
      if(!(this.data.dataPage && this.data.dataPage.autoTableFields && this.data.dataPage.autoTableFields.length > 0)) {
        this.emptyView = true
      }else{
        this.emptyView = false
        if(this.infoData.displayType) {
          this.tableShowType = this.infoData.displayType
        }
        this.init()
        this.menuName = this.data.menuName
      }
      this.$forceUpdate()
      // 打印调试
      // this.menuId = this.$route.query.id
      // this.getListData()
    },
    // 获取路由参数初始化
    initQueryInfo () {
      this.queryParams = {}
      this.page = {
        total: 0,
        currentPage: 1,
        pageSize: 20,
        pageSizes: [20, 50, 100, 200, 500, 1000]
      }
      if(this.$route.query && this.$route.query.id) {
        this.importHeaders = {
          "Authorization": 'Bearer ' + store.getters.access_token,
          'designId': this.$route.query.id,
          'operator': encodeURI('导入')
        }
        this.menuId = this.$route.query.id
        this.getDesignInfoHandle()
        this.pathQuery = JSON.parse(JSON.stringify(this.$route.query))
        // delete this.pathQuery['id']
      }
      // if(this.$route.query && this.$route.query.dataModelId) {
      //   this.getListData()
      // }
    },
    // 获取列表数据
    getListData () {
      this.tableLoading = true
      if (!this.menuId) {
        let demoData2 = {}
        for(let key in this.demoData) {
          demoData2[key] = this.demoData[key] + 2
        }
        this.tableData = [this.demoData] // [this.demoData, demoData2]
        this.page.total = 1 // 2
        this.tableLoading = false
        // this.statisticsHandle()   // 仅用于调试
      } else {
        let obj = {}
        obj.size = this.page.pageSize
        obj.current = this.page.currentPage
        obj.conditions = []
        for(let i in this.queryParams) {
          if(this.queryParams[i] || this.queryParams[i] === 0 || this.queryParams[i] === false || (typeof this.queryParams[i] == 'number')) {
            // obj[i] = this.queryParams[i]
            obj.conditions.push({fieldKey: i, enabledQueryTypes: 'eq', value: this.queryParams[i]})
          }
        }
        if(this.data.dataPage && this.data.dataPage.http) {
          let tp = JSON.parse(JSON.stringify(this.data.dataPage.http))
          if(this.$store.getters.labelValue && this.$store.getters.labelValue.requestContentType) {
            tp.requestContentType = this.$store.getters.labelValue.requestContentType[tp.requestContentType]
          }
          obj = Object.assign(obj, tp.parameters)
        }
        this.currentQuery = obj
        getCrudDataPage(this.$route.query.jvsAppId, obj, this.$route.query.dataModelId, this.$route.query.id).then(res => {
          let totalPage = 1
          if(res.data.code == 0 && res.data.data) {
            this.tableData = res.data.data.records
            this.page.total = res.data.data.total
            this.page.currentPage = res.data.data.current
            this.tableLoading = false
            totalPage = res.data.data.pages
          }
          if(this.page.total > 0 && this.tableData.length > 0) {
            if(Math.ceil(this.page.total / this.page.pageSize) !== Number.parseInt(totalPage)) {
              this.page.currentPage = 1
            }
          }else{
            this.page.currentPage = 1
          }
          this.$forceUpdate()
        })
      }
    },
    // 存在id时获取数据
    getDesignInfoHandle () {
      this.designColumn = []
      previewPage(this.$route.query.jvsAppId, this.menuId).then(res => {
        if(res.data.code == 0 && res.data.data) {
          let data = JSON.parse(res.data.data.viewJson)
          const arr = [...data.buttons]
          this.jvsAppId = res.data.data.jvsAppId
          this.tableShowType = data.displayType || res.data.data.displayType || 'table'
          this.topBtns = arr.filter(item => {
            return item.position === 'top' && !(item.enable === false)
          })
          this.upDataHandle(data)
          this.getListData()
          if(!res.data.data.isDeploy) {
            // this.$message.warning('该设计未发布')
            this.$notify({
              title: '提示',
              message: '该设计未发布',
              position: 'bottom-right',
              type: 'warning'
            });
          }
        }
      }).catch( e => {
        this.permission = false
      })
    },
    // 更新设计
    upDataHandle (data) {
      if(data.dataPage && !data.dataPage.headStatisticalData) {
        data.dataPage.headStatisticalData = []
      }
      if(data.dataPage && !data.dataPage.referencePages) {
        data.dataPage.referencePages = [
          {
            address: "",
            enable: false,
            height: 0,
            position: ""
          }
        ]
      }
      this.data = data
      this.init()
    },
    // 初始化
    init () {
      if(!this.data.dataPage) {
        return false
      }
      let tempList = JSON.parse(JSON.stringify(this.data.dataPage.autoTableFields))
      this.column = tempList

      this.showGantt = false
      if(this.data.gantt && this.data.ganttForm) {
        if(this.data.ganttForm.plainStart && this.data.ganttForm.plainEnd && this.data.ganttForm.reallyStart && this.data.ganttForm.reallyEnd) {
          this.showGantt = true
        }
        !this.data.ganttForm.showType &&
          (this.data.ganttForm.showType = "month"); // 甘特图默认按月展示
      }

      // 按钮
      let tb = []
      let lb = []
      let isSelect = false
      let showMenu = false
      for(let i in this.data.buttons) {
        if(this.data.buttons[i].position == 'top') {
          if(!(this.data.buttons[i].enable === false)) {
              tb.push(this.data.buttons[i])
            if(this.data.buttons[i].type == 'btn_export') {
              isSelect = true
            }
          }
        }
        if(this.data.buttons[i].position == 'line') {
          if(this.data.buttons[i].name && this.data.buttons[i].type && !(this.data.buttons[i].enable === false)) {
            lb.push(this.data.buttons[i])
            if(!(this.data.buttons[i].enable === false)) {
              showMenu = true
            }
          }
        }
        if(['btn_export', 'btn_delete'].indexOf(this.data.buttons[i].type) > -1 && this.data.buttons[i].enable) {
          isSelect = true
        }
      }
      // 存在导出删除 或有底部按钮 时，表格可多选
      if(isSelect) {
        this.selectable = true
      }
      this.topBtns = tb
      // 存在导出时，表格可多选
      if(isSelect) {
        this.selectable = true
      }
      let obj = null
      const arr = []
      lb.filter(item => {
        if (item.type === 'btn_delete') {
          obj = JSON.parse(JSON.stringify(item))
        } else {
          arr.push(item)
        }
      })
      if(obj) {
        arr.push(obj)
      }
      this.lineBtns = [...arr]
      this.treeBtns = this.data.leftTreeButton ? JSON.parse(JSON.stringify(this.data.leftTreeButton)) : []
      this.option.menu = showMenu

      // 查询条件
      this.query = []
      // 顶部自定义统计
      this.customStatistics = []
      for(let h in this.data.dataPage.headStatisticalData) {
        if(this.data.dataPage.headStatisticalData[h].enable) {
          this.customStatistics.push({
            name: this.data.dataPage.headStatisticalData[h].name,
            sql: 'xxxxx'
          })
        }
      }
      // 嵌套页面
      this.iframepage = (this.data.dataPage.referencePages && this.data.dataPage.referencePages.length > 0) ? this.data.dataPage.referencePages[0] : {}
      this.formatTable()
      // 在线时才可调接口
      if(this.menuId) {
        // 分页查询
        this.getListData()
      }
      // 表头
      if(this.data.pageTableTitle) {
        let titleTemp = []
        this.data.pageTableTitle.filter(tit => {
          if(tit.tableTitle && tit.fieldKey && tit.fieldKey.length > 0) {
            titleTemp.push(tit)
          }
        })
        this.option.tableTitle = titleTemp
      }
    },
    // 数据配置
    formatTable () {
      // 生成table配置
      this.getTableColumn()
    },
    // 格式化column
    async getTableColumn () {
      let temp = [] // 表格配置项
      let queryTemp = [] // 查询条件项
      let bool = false // 是否显示合计
      this.retrievalItem = null
      this.retrievalOption = []
      this.demoData = {}
      let countShow = 0
      this.column.filter(cit => {
        if(cit.show) {
          countShow += 1
        }
      })
      for(let i in this.column) {
        let obj = {
          label: this.column[i].showChinese,
          prop: this.column[i].aliasColumnName ? this.column[i].aliasColumnName : this.column[i].columnName,
          hide: !this.column[i].show,
          width: countShow > 4 ? this.column[i].showWidth : 'auto',
          fixed: this.column[i].fixed
        }
        // if(this.column[i].showWidth && this.option.menu && i < this.column.length-1) {
        //   obj.width = this.column[i].showWidth || 200
        // }
        if(this.column[i].componentType) {
          obj.type = this.column[i].componentType
        }
        if(this.column[i].dbJavaType == 'field_image') {
          obj.type = 'image'
        }
        if(obj.type == 'SWITCH') {
          obj.type = 'switch'
        }
        if(this.column[i].designJson) {
          if(this.column[i].designJson.type == obj.type) {
            obj = Object.assign(obj, this.column[i].designJson)
          }
          if(['imageUpload', 'image', 'signature'].indexOf(this.column[i].designJson.type) > -1) {
            obj.type = 'image'
          }
          if(['fileUpload', 'file'].indexOf(this.column[i].designJson.type) > -1) {
            obj.type = 'file'
          }
          obj.label = this.column[i].showChinese
        }
        this.demoData[obj.prop] = '示例'
        if(this.column[i].advancedSettings) {
          if(this.column[i].advancedSettings.textcolor) {
            obj.color = this.column[i].advancedSettings.textcolor
          }
          if(this.column[i].advancedSettings.backColor) {
            obj.backColor = this.column[i].advancedSettings.backColor
          }
          if(this.column[i].advancedSettings.dictionary) {
            obj.dicData = this.column[i].advancedSettings.dictionary
          }
          // 字典匹配
          if(this.column[i].advancedSettings.dictSource){
            switch(this.column[i].advancedSettings.dictSource) {
              case 'option':
                if(this.column[i].advancedSettings.dictionary) {
                  obj.dicData = this.column[i].advancedSettings.dictionary
                }
                break;
              case 'url':
                if(this.column[i].advancedSettings.dictionaryHttp) {
                  // 请求接口
                  let tp = JSON.parse(JSON.stringify(this.column[i].advancedSettings.dictionaryHttp))
                  if(this.$store.getters.labelValue && this.$store.getters.labelValue.requestContentType) {
                    tp.requestContentType = this.$store.getters.labelValue.requestContentType[tp.requestContentType]
                  }
                  if(tp && tp.url) {
                    await sendMyRequire(tp, tp.parameters).then(res => {
                      if(res.data.code == 0) {
                        obj.dicData = res.data.data
                        let propsTemp = {
                          label: 'label',
                          value: 'value'
                        }
                        if(this.column[i].advancedSettings.dictLabelFieldName) {
                          propsTemp.label = this.column[i].advancedSettings.dictLabelFieldName
                        }
                        if(this.column[i].advancedSettings.dictValueFieldName) {
                          propsTemp.value = this.column[i].advancedSettings.dictValueFieldName
                        }
                        obj.props = propsTemp
                      }
                    })
                  }
                }
                break;
              case 'system':
                if(this.column[i].advancedSettings.dictionaryUniqId) {
                  // 拉取数据
                  await getSystemDictItems(this.column[i].advancedSettings.dictionaryUniqId).then(res => {
                    if(res.data.code == 0 && res.data.data) {
                      obj.dicData = res.data.data
                    }
                  })
                }
                break;
              default: break;
            }
          }
          if(obj.type == 'image') {
            obj.width = this.column[i].advancedSettings.width < 80 ? 80 : this.column[i].advancedSettings.width
            obj.height = this.column[i].advancedSettings.height < 80 ? 80 :this.column[i].advancedSettings.height
          }
          if(obj.type == 'link') {
            obj.openType = this.column[i].advancedSettings.openType
            obj.text = this.column[i].advancedSettings.text
          }
          // 动态控制
          if(this.column[i].advancedSettings.expressControl) {
            obj.expressControl = this.column[i].advancedSettings.expressControl
          }
          if(this.column[i].advancedSettings.conditionControl && this.column[i].advancedSettings.conditionControlEnable !== false) {
            obj.conditionControl = this.column[i].advancedSettings.conditionControl
          }
          // 显示的工具
          if(this.column[i].advancedSettings.tools) {
            obj.tools = this.column[i].advancedSettings.tools
          }
          // 卡片样式
          if(this.tableShowType == 'card') {
            if(this.column[i].advancedSettings.cardPosition) {
              obj.cardPosition = this.column[i].advancedSettings.cardPosition
            }
          }
          // 打开表单
          if(this.column[i].advancedSettings.openFormId) {
            if(!obj.tools) {
              obj.tools = []
            }
            obj.tools.push('openForm')
            obj.openFormId = this.column[i].advancedSettings.openFormId
          }
          // 关联模型
          if(this.column[i].advancedSettings.modelDisplay && this.column[i].advancedSettings.modelDisplay.dataLinkageModelId &&
            this.column[i].advancedSettings.modelDisplay.linkageFieldKeys && this.column[i].advancedSettings.modelDisplay.linkageFieldKeys.length > 0 &&
            this.column[i].advancedSettings.modelDisplay.showModelType == 'oneToMany') {
            if(!obj.tools) {
              obj.tools = []
            }
            obj.tools.push('dataModelDisplay')
            obj.modelDisplay = {...this.column[i].advancedSettings.modelDisplay}
          }
        }
        // 日期  时间 配置
        if(this.column[i].componentType == 'datePicker' || this.column[i].componentType == 'timePicker') {
          obj.datetype = this.column[i].dataType
          switch(this.column[i].dbJavaType) {
            case 'field_date_time':
              obj.datetype = 'datetime';
              obj.format = "yyyy-MM-dd hh:mm:ss";
              obj.valueFormat = "yyyy-MM-dd hh:mm:ss";
              break;
            case 'field_date':
              obj.datetype = 'date';
              obj.format = "yyyy-MM-dd";
              obj.valueFormat = "yyyy-MM-dd";
              break;
            case 'field_time':
              obj.format = "HH:mm:ss";
              obj.valueFormat = "HH:mm:ss";
              break;
            default : obj.datetype = 'datetime';
              obj.format = "yyyy-MM-dd hh:mm:ss";
              obj.valueFormat = "yyyy-MM-dd hh:mm:ss";
              break;
          }
        }
        // 数据筛选或接口数据来源，取值行数据  字段名_1
        if(obj.url && obj.datatype != 'option') {
          obj.type == 'input'
          obj.dicData = null
        }
        if(obj.type == 'imageUpload' || obj.type == 'image') {
          obj.type = 'image'
          obj.imgWidth = 80
          obj.imgHeight = 80
          this.demoData[obj.prop] = [{url: '/jvs-ui-public/img/headImg.png', name: 'headImg.png'}]
        }
        if(obj.type == 'fileUpload' || obj.type == 'file') {
          obj.type = 'file'
          this.demoData[obj.prop] = [{url: '/jvs-ui-public/img/headImg.png', name: 'headImg.png'}]
        }
        if(obj.type == 'htmlEditor') {
          obj.slot = true
        }
        if(this.column[i].enableStatistics == true) {
          obj.enableStatistics = true
          bool = true
        }
        if(obj.type == 'inputNumber') {
          this.demoData[obj.prop] = 100
        }
        if(obj.type == 'switch') {
          this.demoData[obj.prop] = false
        }
        if(this.showGantt) {
          let now = new Date()
          if(obj.prop == this.data.ganttForm.plainStart) {
            this.demoData[obj.prop] = dateFormat(new Date(now.getTime() - 5 * 24 * 60 * 60 * 1000), 'yyyy-MM-dd')
          }
          if(obj.prop == this.data.ganttForm.plainEnd) {
            this.demoData[obj.prop] = dateFormat(new Date(now.getTime() + 5 * 24 * 60 * 60 * 1000), 'yyyy-MM-dd')
          }
          if(obj.prop == this.data.ganttForm.reallyStart) {
            this.demoData[obj.prop] = dateFormat(new Date(now.getTime() - 4 * 24 * 60 * 60 * 1000), 'yyyy-MM-dd')
          }
          if(obj.prop == this.data.ganttForm.reallyEnd) {
            this.demoData[obj.prop] = ''
          }
        }
        temp.push(obj)
        if(this.column[i].enableStatistics == true) {
          bool = true
        }
        // 查询条件
        if(this.column[i].enableQuery) {
          queryTemp.push(this.column[i])
        }
        // 字段检索
        if(this.column[i].enableRetrieval) {
          this.retrievalColumn = JSON.parse(JSON.stringify(this.column[i]))
          if(this.column[i].designJson) {
            this.retrievalItem = JSON.parse(JSON.stringify(this.column[i].designJson))
            // 字典类
            if(this.column[i].designJson.datatype == 'option') {
              this.retrievalOption = this.column[i].designJson.dicData
            }else{
              this.retrievalOption = []
            }
            this.leftTreeLoading = false
          }
        }
      }
      this.option.column = temp
      this.option.indexLabel = '序号'
      this.option.showsummary = bool
      console.log('表格列重绘')
      this.indexAbled = !this.data.hiddenIndex // 是否展示序号列
      if(this.data.menuFixed) {
        this.option.menuFix = 'right'
      }else{
        this.option.menuFix = false
      }
      if(this.data.menuWidth) {
        this.option.menuWidth = this.data.menuWidth || 150
      }
      this.query = queryTemp
      // console.log(queryTemp)
      // 生成查询条件配置
      this.getQueryColumn()
      this.$nextTick(() => {
        this.alreadLoad = true
      })
      this.$forceUpdate()
    },
    // 格式化查询条件
    getQueryColumn () {
      let temp = []
      this.queryBeforeSlot = []
      let maxDatePicket = false
      let normalDatePicker = false
      let rangeDatePicker = false
      let hasSelect = false
      let maxLabelLen = 0
      for(let i in this.query) {
        // 自定义了查询条件
        if(this.query[i].queryConditionConfig && this.query[i].queryConditionConfig.type) {
          let obj = {...this.query[i].queryConditionConfig, span: 24, beforeSlot: true}
          temp.push(obj)
          this.queryParams[this.query[i].queryConditionConfig.prop] = null
          this.queryBeforeSlot.push({prop: obj.prop, list: this.query[i].queryConditionConfig.queryTypes || [{label: '等于', value: 'eq'}], disabled: obj.disabled})
        }else{
          let fitem = this.$store.getters.labelValue.fieldTypeMore[this.query[i].dbJavaType]
          let name = ""
          if(this.query[i].showChinese) {
            name = pinyin.getFullChars(this.query[i].showChinese)
            name = name[0].toLowerCase() + name.slice(1, name.length)
          }
          let obj = {
            label: this.query[i].showChinese,
            prop: this.query[i].aliasColumnName ? this.query[i].aliasColumnName : name,
            span: 24,
            searchSpan: 24,
          }
          obj.defaultValue = ''
          if(fitem) {
            obj.type = this.query[i].componentType || fitem.componentType
          }
          if(obj.type == 'SWITCH') {
            obj.type = 'switch'
          }
          if(obj.type == 'user') {
            obj.allowinput = false
          }
          let endObj = {
            label: '~',
            prop: (this.query[i].aliasColumnName ? this.query[i].aliasColumnName : name) +'End',
            span: 24,
            placeholder: this.query[i].showChinese
          }
          if(this.query[i].sqlKeyword == 'BETWEEN') {
            obj.prop = (this.query[i].aliasColumnName ? this.query[i].aliasColumnName : name) + 'Start'
          }
          // 下拉框
          if(obj.type == 'select') {
            if(this.query[i].advancedSettings) {
              if(this.query[i].advancedSettings.dictionary) {
                obj.dicData = this.query[i].advancedSettings.dictionary
              }
            }
            if(this.query[i].queryDataOrigin) {
              if(this.query[i].queryDataOrigin.type) {
                switch (this.query[i].queryDataOrigin.type) {
                  case 'data_origin_request' :
                    // 挂载时发送请求
                    if(this.menuId) {
                      obj.dicUrl = this.query[i].queryDataOrigin.http.url;
                      obj.url = this.query[i].queryDataOrigin.http.url;
                      obj.dicUrlHttp = this.query[i].queryDataOrigin.http;
                    }
                    break;
                  case 'data_origin_sql' :
                    obj.dicSql = this.query[i].queryDataOrigin.sql;
                    break;
                  case 'data_origin_js' :
                    obj.dicJS = this.query[i].queryDataOrigin.js;
                    break;
                  default : break;
                }
              }
            }
            // 配置props
            obj.props = {
              label: 'label',
              value: 'value'
            }
            if(this.query[i].associatedFields) {
              obj.props.value = this.query[i].associatedFields.columnName
            }
            if(this.query[i].displayField) {
              obj.props.label = this.query[i].displayField.columnName
            }
          }
          // 日期  时间 配置
          if(obj.type == 'datePicker' || obj.type == 'timePicker') {
            obj.datetype = this.query[i].dataType
            switch(this.query[i].dbJavaType) {
              case 'field_date_time':
                obj.datetype = 'datetime';
                obj.format = "yyyy-MM-dd hh:mm:ss";
                obj.valueFormat = "yyyy-MM-dd hh:mm:ss";
                break;
              case 'field_date' :
                obj.datetype = 'date';
                obj.format = "yyyy-MM-dd";
                obj.valueFormat = "yyyy-MM-dd";
                break;
              case 'field_time' :
                obj.format = "HH:mm:ss";
                obj.valueFormat = "HH:mm:ss";
                break;
              default : obj.datetype = 'datetime';
                obj.format = "yyyy-MM-dd hh:mm:ss";
                obj.valueFormat = "yyyy-MM-dd hh:mm:ss";
                break;
            }
            if(obj.type == 'datePicker' && this.query[i].enableQueryRange) {
              obj.datetype += 'range'
              obj.startplaceholder = '开始时间'
              obj.endplaceholder = '结束时间'
            }
          }
          if(this.query[i].designJson && this.query[i].designJson.type == obj.type) {
            obj = Object.assign(obj, this.query[i].designJson)
            obj.label = this.query[i].showChinese
          }
          if(this.query[i].advancedSettings && this.query[i].advancedSettings.searchSpan) {
            obj.querySpan = this.query[i].advancedSettings.searchSpan
          }
          if(obj.type == 'datePicker') {
            if(obj.datetype == 'date') {
              obj.datetype = 'daterange'
            }
            if(obj.datetype == 'datetime') {
              obj.datetype = 'datetimerange'
            }
            if(obj.datetype == 'month') {
              obj.datetype = 'monthrange'
            }
            if(['datetimerange', 'daterange', 'monthrange'].indexOf(obj.datetype) > -1) {
              rangeDatePicker = true
              if(obj.datetype == 'datetimerange') {
                maxDatePicket = true
              }
              obj.shortcutEnable = true
              obj.searchSpan = 24
            }
            if(obj.datetype == 'datetime') {
              normalDatePicker = true
            }
            delete obj.defaultValue
          }
          if(obj.type == 'timePicker' && obj.isrange) {
            obj.searchSpan = 24
          }
          if(['radio', 'checkbox', 'connectForm'].indexOf(obj.type) > -1) {
            if(['connectForm'].indexOf(obj.type) > -1 && obj.formId) {
              obj.dicUrl = obj.url
              this.$set(obj.props, 'label', obj.prop)
              this.$set(obj.props, 'value', 'id')
            }
            obj.type = 'select'
          }
          obj.disabled = false
          obj.multiple = false
          obj.rules = []
          if(['textarea', 'serialNumber'].indexOf(obj.type) > -1) {
            obj.type = 'input'
          }
          obj.displayExpress = []
          obj.span = 24
          obj.searchSpan = 24
          obj.display = true
          // 查询条件全部去除默认值
          if(!(obj.defaultValue == null)) {
            delete obj.defaultValue
          }
          // 查询条件全部去除前缀后缀内容
          if(obj.prepend || obj.append) {
            obj.prepend = ''
            obj.append = ''
          }
          // 查询条件全部去除触发逻辑
          obj.eventHttp = ''
          // 查询条件选择类默认多选
          if(obj.type == 'select' && (this.query[i].enableQueryRange || (this.query[i].designJson && this.query[i].designJson.type == 'checkbox'))) {
            obj.multiple = true
            obj.collapsetags = false
            hasSelect = true
          }
          if(obj.label.length > maxLabelLen) {
            maxLabelLen = obj.label.length
          }
          temp.push(obj)
          if(this.query[i].enabledQueryTypes && this.query[i].enabledQueryTypes.length > 0) {
            obj.beforeSlot = true
            let queryTypes = []
            for(let q in this.query[i].enabledQueryTypes) {
              let str = ''
              switch(this.query[i].enabledQueryTypes[q]) {
                case 'eq':
                  str = '等于';
                  break;
                case 'ne':
                  str = '不等于';
                  break;
                case 'gt':
                  str = '大于';
                  break;
                case 'ge':
                  str = '大于等于';
                  break;
                case 'lt':
                  str = '小于';
                  break;
                case 'le':
                  str = '小于等于';
                  break;
                case 'in':
                  str = '包含';
                  break;
                case 'like':
                  str = '模糊匹配';
                  break;
                case 'between':
                  str = '之间';
                  break;
                default: ;break;
              }
              if(str) {
                queryTypes.push({label: str, value: this.query[i].enabledQueryTypes[q]})
              }
            }
            let mulBool = false
            if(['datetimerange', 'daterange', 'monthrange'].indexOf(obj.datetype) > -1 || (obj.type == 'timePicker' && obj.isrange)) {
              mulBool = true
            }
            this.queryBeforeSlot.push({prop: obj.prop, list: mulBool ?  [{label: '之间', value: 'between'}] : queryTypes, disabled: obj.disabled})
            this.$set(this.queryOprator, obj.prop, mulBool ? 'between' : (queryTypes && queryTypes.length > 0) ? queryTypes[0].value : '')
          }
          if(this.query[i].sqlKeyword == 'BETWEEN') {
            if(endObj.label.length > maxLabelLen) {
              maxLabelLen = endObj.label.length
            }
            temp.push(endObj)
          }
        }
      }
      if(normalDatePicker) {
        this.searchWidth = 375
      }
      if(hasSelect) {
        this.searchWidth = 390
      }
      if(rangeDatePicker) {
        this.searchWidth = 455
      }
      if(maxDatePicket) {
        this.searchWidth = 520
      }
      if(maxLabelLen > 0) {
        this.searchOption.labelwidth = (maxLabelLen * 18 + 12) + 'px'
      }
      this.searchOption.column = temp
    },
    // 表单配置
    getFormColumn (type, item) {
      if(item.form.formdata && item.form.formdata.length > 0) {
        // 兼容历史设计数据
        this.formatFormItem(item.form.formdata)
        if(type == 'normalForm' || type == 'detailForm') {
          this.formOption = this.formatFormOption(type, item.form.formdata[0].forms, item.form.formdata[0].formsetting)
        }else{
          let ct = []
          for(let i in item.form.column) {
            let obj = {
              defaultData: item.form.column[i].defaultData,
              formOption: {},
              label: item.form.column[i].label,
              name: item.form.column[i].name,
              show: item.form.column[i].show || true
            }
            obj.formOption = {
              btnSetting: item.form.formdata[i].formsetting.btnSetting,
              size: item.form.formdata[i].formsetting.formsize,
              formAlign: item.form.formdata[i].formsetting.labelposition,
              labelWidth: item.form.formdata[i].formsetting.labelwidth + '',
              column: item.form.formdata[i].forms,
            }
            ct.push(obj)
          }
          this.formOption = {
            type: 'card',
            column: ct,
            formdata: item.form.formdata
          }
        }
      }else{
        this.formOption = {
          btnHide: true,
          column: []
        }
      }
      if(this.formOption.btnSetting) {
        for(let i in this.formOption.btnSetting) {
          if(this.formOption.btnSetting[i].buttonType == 'print') {
            this.hasPrint = true
          }
        }
      }
    },
    // 格式化表单配置项
    formatFormOption (type, forms, formsetting) {
      let temp = {
        column: JSON.parse(JSON.stringify(forms)),
        btnSetting: formsetting.btnSetting,
        size: formsetting.formsize,
        formAlign: formsetting.labelposition,
        labelWidth: formsetting.labelwidth + 'px',
        fullscreen: formsetting.fullscreen,
        cancal: false,
        flag: formsetting.flag,
        submitBtn: formsetting.submitBtn,
        emptyBtn: formsetting.emptyBtn
      }
      if(type == 'detailForm') {
        temp.disabled = true
        temp.btnHide = true
      }else{
        temp.disabled = false
        temp.btnHide = false
      }
      return temp
    },
    // 下载文件
    downloadFile(filename, content) {
      var elink = document.createElement('a')
      if(filename) {
        elink.download = filename
      }
      elink.style.display = 'none'

      var blob = new Blob([content], {type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8'}) //,{type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8'})
      elink.href = URL.createObjectURL(blob)

      document.body.appendChild(elink)
      elink.click()

      document.body.removeChild(elink)
    },
    // 获取select项，表单值为数组
    getSelectItem (list) {
      let temp = []
      for(let i in list) {
        for(let j in list[i].forms) {
          if(list[i].forms[j].type == 'select' && list[i].forms[j].multiple) {
            temp.push(list[i].forms[j].prop)
          }
        }
      }
      this.selectFormItems = temp
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
    // 动态显示按钮
    showhidebutton (item, rowData, type, index) {
      let bool = true
      if(item.position == 'line') {
        bool = true
        if(type == 'out') {
          if(index > 1) {
            bool = false
          }
        }
        if(type == 'in') {
          if(index < 2) {
            bool = false
          }
        }
      }else{
        bool = true
      }
      return bool
    },
    // 条件查询
    queryHandle (form) {
      this.queryShow = false
      this.queryParams = form
      this.getListData()
    },
    // 重置条件
    resetQueryHandle (formName) {
      this.queryOprator = {}
    },
    // 多选
    selectChange (data) {
      this.selectList = data
    },
    // 统计查询
    statisticsHandle () {
      if(this.data.dataPage && this.data.dataPage.headStatisticalData && this.data.dataPage.headStatisticalData.length > 0) {
        let list = this.data.dataPage.headStatisticalData
        for(let i in list) {
          if(this.loadTimes === -1 || (this.loadTimes > -1 && list[i].syncQuery)) {
            // 网络请求
            if(list[i].type == 'data_origin_request') {
              if(list[i].http && list[i].http.url) {
                let tp = JSON.parse(JSON.stringify(list[i].http))
                tp.requestContentType  && (tp.requestContentType = this.$store.getters.labelValue.requestContentType[tp.requestContentType])
                sendMyRequire(tp, tp.parameters).then(res => {
                  if(res.data.code == 0) {
                    this.customStatistics[i].sql = res.data.data
                  }
                })
              }
            }
            // sql
            if(list[i].type == 'data_origin_sql') {
              if(list[i].sql) {
                let to = {}
                if(list[i].syncQuery) {
                  to = Object.assign({}, this.queryParams)
                }
                getStatistics(this.$route.query.jvsAppId, this.menuId, list[i].statisticsCode, to).then(res => {
                  if(res.data.code == 0) {
                    this.customStatistics[i].sql = res.data.data[list[i].name]
                  }
                })
              }
            }
            // 逻辑引擎
            if(list[i].type == 'data_origin_rule') {
              if(list[i].rule && list[i].rule.ruleId) {
                let tp = {
                  httpMethod: "GET",
                  parameters: {},
                  requestContentType: "FORM_URLENCODED",
                  responseContentType: "JSON",
                  url: `/api/rule/run/${list[i].rule.ruleId}`,
                }
                tp.requestContentType  && (tp.requestContentType = this.$store.getters.labelValue.requestContentType[tp.requestContentType])
                sendMyRequire(tp, tp.parameters).then(res => {
                  if(res.data.code == 0) {
                    this.customStatistics[i].sql = res.data.data
                  }
                })
              }
            }
          }
        }
      }
      this.loadTimes++
    },
    // 按钮点击事件
    async btnClickHandle (row, index, item) {
      if(this.propData) {
        return false
      }
      if (row && row.id) {
        this.dataId = row.id
        this.rowData = JSON.parse(JSON.stringify(row))
      } else {
        this.dataId = null
      }
      this.formDesignId = item.formId
      // 预览模式不发请求
      // if(!this.menuId) {
      //   return false
      // }
      this.itemData = item
      console.log(this.itemData)
      // // 导出模板
      // if(item.type == 'btn_download_template') {
      //   if(this.developmentConfig.defaultDownloadExportTemplateUri) {
      //     this.$openUrl(this.developmentConfig.defaultDownloadExportTemplateUri, '_self')
      //   }
      // }else{
        // 表单
        if(['btn_add', 'btn_modify', 'btn_detail', 'btn_form'].indexOf(item.type) > -1) {
          await getFormInfo(this.$route.query.jvsAppId, item.formId).then(res => {
            if(res.data.code == 0) {
              if(res.data.data.viewJson) {
                item.form = JSON.parse(res.data.data.viewJson)
              }
            }
          })
          if(!item.form || !item.form.formdata) {
            return false
          }
          this.getSelectItem(item.form.formdata)
          this.formTitle = item.name
          this.formType = item.form.formType
          this.getFormColumn(item.form.formType, item)
          // 修改  详情
          if(item.type == 'btn_modify' || item.type == 'btn_detail'){
            // 修改
            if(item.type == 'btn_modify') {
              // 普通表单
              if(item.form.formType == 'normalForm') {
                if(this.menuId) {
                  getSingleData(this.$route.query.jvsAppId, this.$route.query.dataModelId, row.id, this.$route.query.id).then(res => {
                    if (res.data && res.data.code == 0) {
                      this.formData = res.data.data
                      this.formVisible = true
                    }
                  })
                }else{
                  this.formData = this.demoData
                  // console.log(this.formData )
                  this.formVisible = true
                }
              }
              // 多级表单
              else{
                let tempData = {}
                if(this.menuId) {
                  for(let i in item.form.formdata) {
                    let itemUrl = ''
                    let itemObj = {}
                    if(item.form.formdata[i].formsetting.echoUrl) {
                      itemUrl = item.form.formdata[i].formsetting.echoUrl
                    }
                    if(itemUrl) {
                      this.getItemForm(itemUrl, row, itemObj)
                    }
                    tempData[item.form.column[i].name] = itemObj
                  }
                }
                this.formData = tempData
                this.formVisible = true
              }
            }else{
              // 详情
              if(item.type == 'btn_detail') {
                this.formOption.btnHide = true
              }
              // 回显
              // 挂载时发送请求
              if(this.menuId) {
                getSingleData(this.$route.query.jvsAppId, this.$route.query.dataModelId, row.id, this.$route.query.id).then(res => {
                  if (res.data && res.data.code == 0) {
                    this.formData = res.data.data
                    this.formVisible = true
                  }
                })
              }else{
                this.formData = this.demoData
                this.formVisible = true
              }
            }
          }else{
            this.formData = {}
            this.formVisible = true
          }
        }else{
          if(!this.menuId) {
            return false
          }
          // 删除
          if(item.type == 'btn_delete') {
            let bool = false
            if(this.tableData.length == 1) {
              bool = true
            }
            this.$confirm('确认删除？').then(_ => {
              if(item.preHttp && item.preHttp.url && item.preHttp.httpMethod && item.preHttp.requestContentType) {
                let tp = JSON.parse(JSON.stringify(item.preHttp))
                if(this.$store.getters.labelValue && this.$store.getters.labelValue.requestContentType) {
                  tp.requestContentType = this.$store.getters.labelValue.requestContentType[tp.requestContentType]
                }
                let tob = JSON.parse(JSON.stringify(row))
                sendMyRequire(tp, Object.assign(tob, tp.parameters)).then(res => {
                  if(res.data.code == 0) {
                    // this.$message.success("删除成功")
                    this.$notify({
                      title: '提示',
                      message: '删除成功',
                      position: 'bottom-right',
                      type: 'success'
                    });
                    if(bool) {
                      this.page.currentPage -= 1
                    }
                    // this.getList()
                    this.getListData()
                  }
                })
              } else {
                delSingleData(this.$route.query.jvsAppId, this.$route.query.dataModelId, row.id, this.$route.query.id).then(res => {
                  if (res.data && res.data.code == 0) {
                    // this.$message.success("删除成功")
                    this.$notify({
                      title: '提示',
                      message: '删除成功',
                      position: 'bottom-right',
                      type: 'success'
                    });
                    if(bool) {
                      this.page.currentPage -= 1
                    }
                    this.getListData()
                  }
                })
              }
            }).catch(_ => {})
          }
          // 网络请求
          if(item.type == 'btn_network_request') {
            let tp = JSON.parse(JSON.stringify(item.netHttp))
            if(this.$store.getters.labelValue && this.$store.getters.labelValue.requestContentType) {
              tp.requestContentType = this.$store.getters.labelValue.requestContentType[tp.requestContentType]
            }
            let pa = JSON.parse(JSON.stringify(row))
            pa = Object.assign(pa, tp.parameters)
            sendMyRequire(tp, pa).then(res => {
              if(res.data.code == 0) {
                this.getListData()
              }
            })
          }else{
            // 导出模板 导出
            if(['btn_download_template', 'btn_export'].indexOf(item.type) > -1) {
              if(item.preHttp && item.preHttp.url) {
                if(item.preHttp.httpMethod == 'GET') {
                  let pstring = ''
                  let tarr = []
                  if(item.preHttp.parameters) {
                    for(let p in item.preHttp.parameters) {
                      tarr.push(p + '=' + item.preHttp.parameters[p])
                    }
                    if(tarr.length > 0) {
                      pstring += '?'
                      pstring += tarr.join('&')
                    }
                  }
                  if(this.queryParams) {
                    let trr = []
                    for(let tr in this.queryParams) {
                      trr.push(tr+ '=' + this.queryParams[tr])
                    }
                    if(trr.length > 0) {
                      if(tarr.length == 0) {
                        pstring += '?'
                      }
                      pstring += trr.join('&')
                    }
                  }
                  this.$openUrl(item.preHttp.url + pstring, '_self')
                }else{
                  let tp = JSON.parse(JSON.stringify(item.preHttp))
                  if(this.$store.getters.labelValue && this.$store.getters.labelValue.requestContentType) {
                    tp.requestContentType = this.$store.getters.labelValue.requestContentType[tp.requestContentType]
                  }
                  sendMyRequire(tp, tp.parameters).then(res => {
                    if(res.data.code == 0) {
                      // console.log(res.data.data)
                    }
                  })
                }
              } else {
                const params = {
                  designId: this.$route.query.id
                }
                if (item.type === 'btn_export') {
                  exportData(this.$route.query.jvsAppId, this.$route.query.dataModelId, Object.assign(params, this.queryParams)).then(res => {
                    if (res.data) {
                      let name = res.headers["content-disposition"].split(";")[1]
                      name = name.split("=")[1]
                      name = decodeURI(name)
                      this.downloadFile(name, res.data)
                    }
                  })
                } else {
                  downloadTemplate(this.$route.query.jvsAppId, this.$route.query.dataModelId, params).then(res => {
                    if (res.data) {
                      let name = res.headers["content-disposition"].split(";")[1]
                      name = name.split("=")[1]
                      name = decodeURI(name)
                      this.downloadFile(name, res.data)
                    }
                  })
                }
              }
            }
            // 下载模板
            if(item.type == 'btn_download_template') {
              if(item.preHttp && item.preHttp.url) {
                this.$openUrl(item.preHttp.url, '_self')
              }
            }
            // 外链 内嵌 地址
            if(["btn_embedded_address", "btn_external_link_address"].indexOf(item.type) > -1) {
              if(item.address) {
                this.$openUrl(item.address, item.type == 'btn_embedded_address' ? '_self' : '_blank')
              }
            }
          }
        }
      // }
    },
    // 上传
    uploadChangeHandle (res, file, fileList) {
      if(res.code == 0) {
        // this.$message.success('导入成功')
        this.$notify({
          title: '提示',
          message: '导入成功',
          position: 'bottom-right',
          type: 'success'
        });
        this.getListData()
      }
    },
    // 关闭表单
    handleCloseForm () {
      this.formVisible = false
    },
    // 表单提交
    formSubmit (formsdata) {
      if(!this.menuId) {
        return false
      }
      let form = null // 表单数据
      let url = ''
      let method = ''
      let msg = ''
      let tp = null
      const dataModelId = this.$route.query.dataModelId
      const designId = this.formDesignId
      // 普通表单
      if(this.itemData.form.formType == 'normalForm') {
        form = formsdata
        const http = {
          httpMethod: "POST",
          requestContentType: "application/json",
          responseContentType: "JSON",
          url: this.dataId ? `/mgr/jvs-design/app/use/${this.$route.query.jvsAppId}/dynamic/data/update/${dataModelId}/${this.dataId}` : `/mgr/jvs-design/app/use/${this.$route.query.jvsAppId}/dynamic/data/save/${dataModelId}`,
          headers: {
            'designId': designId,
            'operator': encodeURI('提交')
          }
        }
        if(this.itemData.form.formdata && this.itemData.form.formdata[0] && this.itemData.form.formdata[0].formsetting) {
          tp = JSON.parse(JSON.stringify(http))
          if(this.$store.getters.labelValue && this.$store.getters.labelValue.requestContentType) {
            tp.requestContentType = this.$store.getters.labelValue.requestContentType[tp.requestContentType]
          }
        }
        // if(this.itemData.form.formdata && this.itemData.form.formdata[0] && this.itemData.form.formdata[0].formsetting && this.itemData.form.formdata[0].formsetting.dataSubmissionRequest) {
        //   tp = JSON.parse(JSON.stringify(this.itemData.form.formdata[0].formsetting.dataSubmissionRequest))
        //   if(this.$store.getters.labelValue && this.$store.getters.labelValue.requestContentType) {
        //     tp.requestContentType = this.$store.getters.labelValue.requestContentType[tp.requestContentType]
        //   }
        // }
      }
      // 多级表单
      else{
        if(formsdata.url) {
          url = formsdata.url
        }
        form = formsdata.form
      }
      switch (this.itemData.type){
        case 'btn_add': //'SAVE':
          method = 'post';
          msg = '新增成功';
          break;
        case 'btn_modify': //'EDIT':
          method = 'put';
          msg = '修改成功';
          break;
        case 'btn_form': //'FORM':
          msg = '';
          if(this.itemData.position == 'top') {
            method = 'post';
          }else{
            method = 'put';
          };
          break;
        default : ;break;
      }
      if(tp) {
        let tempObj = JSON.parse(JSON.stringify(form))
        sendMyRequire(tp, Object.assign(tempObj, tp.parameters)).then(res => {
           if(res.data.code == 0) {
            if(msg) {
              // this.$message.success(msg)
              this.$notify({
                title: '提示',
                message: msg,
                position: 'bottom-right',
                type: 'success'
              });
            }else{
              if(res.data.msg) {
                // this.$message.success(res.data.msg)
                this.$notify({
                  title: '提示',
                  message: res.data.msg,
                  position: 'bottom-right',
                  type: 'success'
                });
              }
            }
            this.getListData()
            // this.getList()
            this.handleCloseForm()
          }
        })
      }
    },
    // 自定义按钮事件
    slotbtnClickHandle (row, index) {
      let tp = null
      tp = JSON.parse(JSON.stringify(row))
      if(this.$store.getters.labelValue && this.$store.getters.labelValue.requestContentType) {
        tp.requestContentType = this.$store.getters.labelValue.requestContentType[tp.requestContentType]
      }
      if(tp && tp.url) {
        let tob = JSON.parse(JSON.stringify(this.formData))
        sendMyRequire(tp, Object.assign(tob, tp.parameters)).then(res => {
           if(res.data.code == 0) {
            if(res.data.msg) {
              // this.$message.success(res.data.msg)
              this.$notify({
                title: '提示',
                message: res.data.msg,
                position: 'bottom-right',
                type: 'success'
              });
            }
            this.getListData()
            this.handleCloseForm()
          }
        })
      }
      // if(row.url) {
      //   sendRequire(row.url, 'post', this.formData).then(res => {
      //     this.$message.success(res.data.msg)
      //     this.getList()
      //   })
      // }
    },
    // 同步获取数据
    async getItemForm (itemUrl, row, itemObj) {
      await sendRequire(itemUrl, 'get', row).then(res => {
        if(res.data.code == 0) {
          itemObj = res.data.data
        }
      })
    },
    // 兼容历史设计数据
    formatFormItem (formdata) {
      for(let i in formdata) {
        if(formdata[i].forms) {
          for(let j in formdata[i].forms) {
            let item = this.getItemByValOfArr(formdata[i].forms[j].prop, 'aliasColumnName', formdata[i].autoTableFields)
            // 字典 来源 和 配置
            if(formdata[i].forms[j].url || formdata[i].forms[j].dicUrl) {
              if(this.menuId) {
                formdata[i].forms[j].dicUrl = formdata[i].forms[j].url
              }else{
                formdata[i].forms[j].dicUrl = ''
                formdata[i].forms[j].url = ''
              }
              if(item) {
                if(!formdata[i].forms[j].props) {
                  formdata[i].forms[j].props = {
                    label: '',
                    value: ''
                  }
                }
                if(!formdata[i].forms[j].props.label && item.displayField) {
                  formdata[i].forms[j].props.label = item.displayField.columnName
                }
                if(!formdata[i].forms[j].props.value && item.associatedFields) {
                  formdata[i].forms[j].props.value = item.associatedFields.columnName
                }
              }
            }else{
              formdata[i].forms[j].props = {
                label: 'label',
                value: 'value'
              }
            }
            if(formdata[i].forms[j].type == 'select') {
              // 单选 多选
              if(item.correspondence == "ONE_TO_N") {
                formdata[i].forms[j].multiple = true
              }else{
                formdata[i].forms[j].multiple = false
              }
            }
            // 日期  时间 配置
            if(formdata[i].forms[j].type == 'datePicker' || formdata[i].forms[j].type == 'timePicker') {
              if(item) {
                formdata[i].forms[j].datetype = item.dataType
                if(item) {
                  switch(item.dbJavaType) {
                    case 'field_date_time':
                      formdata[i].forms[j].datetype = 'datetime';
                      formdata[i].forms[j].format = "yyyy-MM-dd hh:mm:ss";
                      formdata[i].forms[j].valueFormat = "yyyy-MM-dd hh:mm:ss";
                      break;
                    case 'field_date':
                      formdata[i].forms[j].datetype = 'date';
                      formdata[i].forms[j].format = "yyyy-MM-dd";
                      formdata[i].forms[j].valueFormat = "yyyy-MM-dd";
                      break;
                    case 'field_time':
                      formdata[i].forms[j].format = "HH:mm:ss";
                      formdata[i].forms[j].valueFormat = "HH:mm:ss";
                      break;
                    default : formdata[i].forms[j].datetype = 'datetime';
                      formdata[i].forms[j].format = "yyyy-MM-dd hh:mm:ss";
                      formdata[i].forms[j].valueFormat = "yyyy-MM-dd hh:mm:ss";
                      break;
                  }
                }
              }
            }
            if(formdata[i].forms[j].type == 'user') {
              formdata[i].forms[j].allowinput = false
            }
            // 开关
            if(formdata[i].forms[j].type == 'SWITCH') {
              formdata[i].forms[j].type = 'switch'
            }
            // 只读文本
            // if(formdata[i].forms[j].type == 'inputReadOnly') {
            //   console.log(formdata[i].forms[j])
            // }
          }
        }
      }
    },
    // 根据val获取数据对应项
    getItemByValOfArr (val, attr, list) {
      for(let i in list) {
        if(list[i][attr] == val) {
          return list[i]
        }
      }
      return false
    },
    // 判断是否有tab组件
    hasTabItem (bool) {
      let result = bool
      if(this.formOption && this.formOption.column) {
        for(let i in this.formOption.column) {
          if(this.formOption.column[i].type == 'tab' && this.formOption.column[i].dicData && this.formOption.column[i].dicData.length > 4) {
            result = false
          }
          if(this.formOption.column[i].type == 'tableForm' && !this.formOption.column[i].editable) {
            result = false
          }
        }
      }
      return bool // result
    },
    // 打印
    printHandle () {
      getPlugins('jvs-design').then(res =>{
        if(res.data && res.data['print-ui-url']) {
          let header = JSON.stringify({designId: this.formDesignId})
          this.$openUrl(`${res.data['print-ui-url']}`+ "jvs-print-ui/#/jvsPrint?"+
            `access_token=${this.$store.getters.access_token}&`+
            `identity=${this.formDesignId}&`+
            `access_fields=/mgr/jvs-design/app/use/${this.$route.query.jvsAppId}/dynamic/data/fields/${this.$route.query.dataModelId}&`+
            `access_data=/mgr/jvs-design/app/use/${this.$route.query.jvsAppId}/dynamic/data/query/single/${this.$route.query.dataModelId}/${this.dataId}&`+
            `access_header=${header}`,
          '_blank')
        }
      })
    },
    // 按钮图标
    getBtnIcon (type) {
      let icon = ''
      switch(type) {
        case 'btn_add': icon = 'el-icon-circle-plus-outline';break;
        case 'btn_import': icon = 'el-icon-upload2';break;
        case 'btn_export': icon = 'el-icon-download';break;
        case 'btn_download_template': icon = 'el-icon-download';break;
        default: icon = '';break;
      }
      return icon
    },
    // 排序查询
    addSortHandle () {
      this.sortsList.push({})
    },
    clearSort () {
      this.sortsList = []
      this.sortShow = false
    },
    sortQuerySubmit () {
      this.queryHandle(this.queryParams)
      this.sortShow = false
    },
    // 排序字段
    getSortColumn (list) {
      let temp = []
      list.filter(li => {
        temp.push({
          label: li.label || li.fieldName,
          prop: li.prop || li.fieldKey
        })
      })
      return temp
    },
    lineFormOption (option) {
      let temp = JSON.parse(JSON.stringify(option))
      temp.isSearch = true
      temp.column.filter(col => {
        col.searchSpan = col.querySpan ? col.querySpan : ((window.devicePixelRatio > 1.5 && document.body.clientWidth < 1500) ? 8 : 6)
      })
      return temp
    },
    // 是否显示更多
    showMore (rowData, list) {
      let bool = true
      if(list.length < 4) {
        bool = false
      }
      return bool
    },
    changeQueryShow () {
      if(this.searchFormType == 'right') {
        this.searchFormType = 'top'
      }else{
        this.searchFormType = 'right'
      }
      this.getListData()
    },
    treeKeywordChange () {
      this.getSelectUrlData(this.retrievalItem, 'retrival')
    },
    // 字段检索可选范围
    getSelectUrlData (item, from) {
      let url=item.dicUrl || item.url
      if (url) {
        // 级联选择
        if(item.type == 'cascader'){
          if(item.datatype == 'dataModel' && item.formId && item.url && item.props.label && item.props.value && item.props.secTitle) {
            // 获取模型树形结构
            this.leftTreeLoading = true
            let paramData = {label: item.props.label, value: item.props.value, secTitle: item.props.secTitle}
            if(from == 'retrival') {
              if(this.retrievalColumn.retrievalOption && this.retrievalColumn.retrievalOption.filterList) {
                paramData.filter = {
                  conditions: [...this.retrievalColumn.retrievalOption.filterList]
                }
              }
              if(this.treeKeyword) {
                paramData.query = {
                  conditions: [{fieldKey: this.retrievalItem.props.label, enabledQueryTypes: 'like', value: this.treeKeyword}]
                }
              }
            }
            getSelectData(item.url, 'post', paramData, (item.method == 'post') ? this.jvsQueryData.id : (item.formId ? item.formId : this.jvsQueryData.id), from == 'retrival' ? {queryBy: 'retrival'}: null).then(res => {
              if(res.data.code === 0) {
                this.retrievalProps = {
                  label: 'name',
                  value: 'value',
                  children: 'children',
                  id: 'id'
                }
                this.retrievalOption = res.data.data || []
                this.retrievalOption = this.getTree(this.retrievalOption , 1, item)
                this.leftTreeLoading = false
                this.$nextTick(()=> {
                  if(this.checkedNode) {
                    this.$refs.jvsTableLeftTree.setCurrentKey(this.checkedNode)
                    this.$forceUpdate()
                  }
                })
              }
            })
          }
        }else{
          if(!item.formId || (item.formId && item.props && item.props.label)) {
            let fs = []
            fs = (item.props && item.props.label) ? [item.props.label] : []
            if(item.props && item.props.secTitle && fs.length > 0) {
              fs.push(item.props.secTitle)
            }
            let postData = {
              fieldList: fs,
              conditions: []
            }
            if(item.props && item.props.sourceFieldId) {
              postData.sourceFieldId = item.props.sourceFieldId
            }
            if(url.startsWith('/mgr/jvs-design/dynamic/data/query/list')) {
              url = `/mgr/jvs-design/app/use/${this.jvsQueryData.jvsAppId}/dynamic/data/query/list/${item.formId}`
            }
            // 兼容2.1.6设计
            if(url == `/mgr/jvs-design/app/${this.jvsQueryData.jvsAppId}/use/dynamic/data/query/list/${item.formId}`) {
              url = `/mgr/jvs-design/app/use/${this.jvsQueryData.jvsAppId}/dynamic/data/query/list/${item.formId}`
            }
            this.leftTreeLoading = true
            if(from == 'retrival') {
              if(this.retrievalColumn.retrievalOption && this.retrievalColumn.retrievalOption.filterList) {
                postData.conditions = [...postData.conditions, ...this.retrievalColumn.retrievalOption.filterList]
              }
            }
            getSelectData(url, item.method ? item.method : 'get', (item.method == 'post') ? postData : null, (item.method == 'post') ? this.jvsQueryData.id : (item.formId ? item.formId : this.jvsQueryData.id), from == 'retrival' ? {queryBy: 'retrival'} : null).then(res => {
              if(res.data.code === 0) {
                this.retrievalOption = []
                for(let sitem in res.data.data){
                  if(typeof res.data.data[sitem] == 'string') {
                    this.retrievalOption.push({
                      label: res.data.data[sitem],
                      value: res.data.data[sitem]
                    })
                  }else{
                    this.retrievalOption.push({
                      label: res.data.data[sitem][item.props.label ? item.props.label : 'label'],
                      value: res.data.data[sitem][item.props.value ? item.props.value : 'value']
                    })
                  }
                }
                this.leftTreeLoading = false
                this.$nextTick(()=> {
                  if(this.checkedNode) {
                    this.$refs.jvsTableLeftTree.setCurrentKey(this.checkedNode)
                    this.$forceUpdate()
                  }
                })
              }
            })
          }
        }
      }else{
        // 部门
        if(item.type == 'department') {
          this.leftTreeLoading = true
          getDeptList().then(res => {
            if(res.data.code == 0) {
              this.retrievalProps = {
                label: 'name',
                value: 'id',
                children: 'children'
              }
              this.retrievalOption = this.getTree(res.data.data, 1)
              this.leftTreeLoading = false
              this.$nextTick(()=> {
                if(this.checkedNode) {
                  this.$refs.jvsTableLeftTree.setCurrentKey(this.checkedNode)
                  this.$forceUpdate()
                }
              })
              this.$forceUpdate()
            }
          })
        }
        // 系统级联选择
        if(item.type == 'cascader'){
          if(item.datatype == 'system' && item.dictName) {
            this.leftTreeLoading = true
            getClassifyDictTree(item.dictName).then(res => {
              if(res.data.code == 0 && res.data.data && res.data.data.children) {
                this.retrievalOption = res.data.data.children ? res.data.data.children : []
                this.retrievalOption = this.getTree(this.retrievalOption, 1, item)
                this.leftTreeLoading = false
                this.$nextTick(()=> {
                  if(this.checkedNode) {
                    this.$refs.jvsTableLeftTree.setCurrentKey(this.checkedNode)
                    this.$forceUpdate()
                  }
                })
              }
            })
          }
        }
      }
      this.editRetrievalItem = null
    },
    getLinkModelData (item, row) {
      this.linkModelPage.currentPage = 1
      this.linkModelData = []
      this.queryLinkModelData(item, row)
    },
    queryLinkModelData (item, row) {
      let temp = []
      if(item.modelDisplay && item.modelDisplay.dataLinkageList && item.modelDisplay.dataLinkageList.length > 0) {
        item.modelDisplay.dataLinkageList.filter(dit => {
          temp.push({
            fieldKey: dit.fieldKey,
            enabledQueryTypes: dit.enabledQueryTypes,
            value: row[dit.value]
          })
        })
      }
      let query = {
        current: this.linkModelPage.currentPage,
        size: this.linkModelPage.pageSize,
      }
      if(temp.length > 0) {
        query.dataLinkageList = temp
      }
      if(item.modelDisplay) {
        if(item.modelDisplay.linkageFieldKeys) {
          query.linkageFieldKeys = item.modelDisplay.linkageFieldKeys
        }
        if(item.modelDisplay.dataLinkageModelId) {
          query.dataLinkageModelId = item.modelDisplay.dataLinkageModelId
        }
      }
      if(query.dataLinkageModelId) {
        this.linkModelLoading = true
        this.linkModelLoading = false
      }
    },
    // 树形结点选中
    handleNodeClick (data, node, dom) {
      if(this.retrievalValue != data[this.retrievalProps.value]) {
        this.retrievalSearch(data)
      }
      this.checkedNode = data.id
    },
    // 字段检索查询
    retrievalSearch (item) {
      this.retrievalValue = item[this.retrievalProps.value]
      if(!this.retrievalValue) {
        this.checkedNode = ''
      }
      if(!item.value) {
        this.checkedNode = ''
        if(this.$refs.jvsTableLeftTree) {
          this.$refs.jvsTableLeftTree.setCurrentKey(null)
        }
      }
    },
    openFormHandle (item, row) {
      this.btnClickHandle(row, -1, {type: 'btn_form', formId: item.openFormId})
    },
    modelDisplayHandle (e, item, row) {
      this.modelDisplayPos = {
        x: e.clientX,
        y: e.clientY
      }
      this.getLinkModelData(item, row)
      this.modelDisplayItem = JSON.parse(JSON.stringify(item))
      this.modelDisplayRow = JSON.parse(JSON.stringify(row))
      this.modelDisplayShow = true
    },
    modelDisplayClose () {
      this.modelDisplayPos = {
        x: 0,
        y: 0
      }
      this.modelDisplayItem = null
      this.modelDisplayRow = null
      this.modelDisplayShow = false
    },
    getSlotItemLabel (rowData, item) {
      let str = ''
      if(rowData) {
        str = '详情'
        this.column.filter(it => {
          if(it.aliasColumnName == item.prop && it.designJson) {
            let icom = JSON.parse(JSON.stringify(it.designJson))
            if(item.type == 'connectForm') {
              str = rowData[icom.props.label]
            }
          }
        })
      }
      return str
    },
  },
  watch: {
    $route (to, from) {
      if(to.fullPath != from.fullPath) {
        location.reload()
      }
    },
    'infoData.displayType': {
      handler(newVal, oldVal) {
        if(newVal) {
          this.tableShowType = newVal
        }
      },
      deep: true
    }
  },
  directives: {
    clickOutside: {
      bind(el, binding, vnode) {
        if (!vnode.context._clickOutside) {
          // 为Vue实例创建事件监听器
          vnode.context._clickOutside = (event) => {
            // 判断点击是否发生在el之外
            if (!(el === event.target || el.contains(event.target) || event.target.className.includes('el-image-viewer') || event.target.className.includes('el-icon-close'))) {
              // 如果指令绑定了函数，调用它
              binding.value(event);
            }
          };
          // 监听文档click事件
          document.addEventListener('click', vnode.context._clickOutside);
        }
      },
      unbind(el, binding, vnode) {
        // 移除事件监听器
        document.removeEventListener('click', vnode.context._clickOutside);
        delete vnode.context._clickOutside;
      }
    },
  },
}
</script>
<style lang="scss" scoped>
$jvsFormItemHeight: 36px;
/deep/.show-form{
  .el-form-item{
    padding: 0 20px;
  }
}
.line-button-list-more{
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  .more-item{
    width: 70px;
    padding: 4px 30px;
    text-align: left;
    cursor: pointer;
    transition: 0.3s;
    height: 32px;
    line-height: 32px;
    &:hover{
      transition: 0.3s;
      background-color: #eff2f7;
    }
  }
  .el-button{
    margin: 0;
    color: #3471ff;
  }
}
// 排序icon
/deep/.jvs-table{
  .table-body-box{
    .sort-column-item-icon{
      display: inline;
    }
  }
}
.search-form{
  /deep/.before-append-item{
    display: flex;
    align-items: center;
    .el-form-item__label{
      min-width: 80px;
    }
    .el-form-item__content{
      margin-left: 0!important;
      flex: 1;
    }
    .before-append-content{
      .bofore-append{
        width: 80px!important;
        display: block;
        .el-input__inner{
          padding-right: 15px;
          border-right: 0;
          border-radius: 4px 0 0 4px;
        }
        .el-input__suffix{
          display: none;
        }
        .el-input.is-focus .el-input__inner, .el-input__inner:focus{
          border-color: #DCDFE6;
        }
      }
    }
    .jvs-form-item{
      width: 100%;
      .el-input__inner{
        border-radius: 0 4px 4px 0;
        padding-right: 30px;
      }
      .el-date-editor{
        width: 100%;
        padding-right: 0!important;
      }
      .jvs-tree-select{
        .el-cascader{
          .el-input__inner{
            overflow: hidden;
          }
        }
      }
      .el-slider, p, .el-input-number, .el-select, .el-date-editor, .form-item-icon-selct, .el-tabs, .el-cascader, .user-info-list{
        height: $jvsFormItemHeight;
      }
    }
  }
  .user-info-list{
    .input-with-select{
      .el-input__inner{
        border-right: 0;
        border-radius: 0;
      }
    }
    .el-input-group__append{
      padding: 0;
      width: 30px;;
      .el-button{
        padding: 0;
      }
      .el-button:nth-of-type(1) {
        display: none;
      }
    }
  }
}
.search-form-inline{
  /deep/.el-row{
    .el-form-item{
      padding: 0;
    }
  }
  /deep/.form-btn-bar{
    .el-form-item__content{
      margin-left: 20px!important;
    }
  }
  /deep/.el-form-item{
    padding-left: 0;
    .el-form-item__label{
      padding-bottom: 0;
      padding-right: 10px;
      text-align: right;
    }
  }
}
.jvstable-left-tree{
  position: absolute;
  top: 0;
  left: 0;
  width: 250px;
  height: 100%;
  overflow: hidden;
  overflow-y: auto;
  padding-left: 0px;
  padding-right: 20px;
  padding-bottom: 20px;
  box-sizing: border-box;
  background-color: #fff;
  .search-input{
    padding-bottom: 20px;
    /deep/.el-input{
      .el-input__inner{
        height: 32px;
        line-height: 32px;
      }
    }
  }
  .treeBox-title{
    font-size: 14px;
    padding-left: 24px;
    display: block;
    background: #fff;
    height: 40px;
    line-height: 40px;
    cursor: pointer;
    .allIcon{
      transform: rotate(90deg);
    }
    .close-all{
      transform: rotate(0);
    }
  }
  .treeBox-title:hover{
    background: #EFF2F7;
  }
  .treeBox-title-check{
    background: #F5F7FA;
  }
  .treeBox-title-expandall{
    padding-left: 0;
  }
  .left-tree-loading{
    min-height: calc(100% - 88px);
    background-image: url('../../../../styles/loading.gif');
    background-repeat: no-repeat;
    background-position: center;
  }
  /deep/.el-tree{
      min-height: calc(100% - 88px);
    }
  /deep/.el-tree-node.is-current{
    >.el-tree-node__content{
      background-color: #F5F7FA;
    }
  }
  .customize-tree-node {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    padding-right: 8px;
    .customize-tree-node-label{
      display: block;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .more-icon{
      display: none;
      .el-icon-more{
        color: #868BA1;
        font-size: 16px;
      }
    }
  }
  .customize-tree-node:hover{
    .more-icon{
      display: block;
    }
  }
}
.jvstable-left-tree::-webkit-scrollbar{
  display: none;
}
.jvstable-left-tree.with-search{
  .left-tree-loading{
    min-height: calc(100% - 54px - 86px);
  }
  /deep/.el-tree{
    min-height: calc(100% - 54px - 86px);
  }
}
.jvstable-left-tree.with-search::-webkit-scrollbar{
  display: none;
}
.jvs-table-leftTree{
  margin-left: 250px;
  width: calc(100% - 250px);
}
.jvs-table-leftTree-hide{
  margin-left: 0;
  width: 100%;
}
.close-tree-tool{
  position: absolute;
  width: 20px;
  height: 40px;
  font-size: 20px;
  text-align: right;
  line-height: 40px;
  border-radius: 40px 0 0 40px;
  overflow: hidden;
  background-color: rgba(0, 0, 0, .3);
  color: #fff;
  top: calc(50% - 20px);
  left: 230px;
  z-index: 1;
  cursor: pointer;
  visibility: hidden;
}
.close-tree-tool-hide{
  text-align: left;
  left: 0;
  border-radius: 0 40px 40px 0;
}
.visible-page .close-tree-tool{
  visibility: visible;
}
/deep/.el-form--label-right.search-form{
  .el-form-item{
    .jvs-form-item:not(.jvs-form-item-disabled), .before-append-content{
      .el-input__inner, .el-textarea__inner{
        border: 1px solid #EEEFF0;
        background: #fff;
        .el-range__icon{
          line-height: 28px;
        }
      }
      .el-input-number{
        .el-input-number__decrease, .el-input-number__increase{
          background: none;
        }
      }
      .unit-span{
        background: none;
        border: 1px solid #EEEFF0;
      }
      .el-input{
        .el-input-group__append{
          background-color: #fff;
          border: 1px solid #EEEFF0;
          border-left: 0;
        }
      }
    }
    .before-append-content{
      .el-input__inner{
        border-right: 0;
      }
    }
  }
}
.slot-component-item{
  padding: 10px;
  /deep/.jvs-form{
    .el-form-item{
      margin-bottom: 0;
    }
  }
  .show-other-model-info-table{
    .header{
      display: flex;
      align-items: center;
      height: 40px;
      background-color: #F5F6F7;
      .item{
        width: 160px;
        padding: 0 10px;
        font-size: 12px;
        font-family: Source Han Sans, Source Han Sans;
        font-weight: 700;
        color: #363B4C;
        box-sizing: border-box;
      }
    }
    .body{
      min-height: 52px;
      .body-item{
        display: flex;
        align-items: center;
        height: 52px;
        .body-item-col{
          width: 160px;
          padding: 0 10px;
          font-size: 14px;
          font-family: Source Han Sans, Source Han Sans;
          font-weight: 400;
          color: #363B4C;
          box-sizing: border-box;
          img{
            display: block;
            width: 30px;
            height: 30px;
            cursor: pointer;
          }
        }
      }
    }
    .empty{
      display: block;
      width: 100%;
      height: 180px;
      color: #909399;
      position: relative;
      &::before{
        content: "";
        display: block;
        width: 100%;
        height: 180px;
        background-image: url('../../../../const/img/emptyImage.svg');
        background-size: 260px 123px;
        background-repeat: no-repeat;
        background-position: center;
      }
      span{
        position: absolute;
        left: calc(50% - 28px);
        bottom: 0;
      }
    }
    .footer{
      margin-top: 20px;
      display: flex;
      align-items: center;
      justify-content: flex-end;
      /deep/.el-pagination{
        display: flex;
        align-items: center;
      }
    }
  }
}
.modelDisplay-box{
  position: fixed;
  left: 0;
  top: 0;
  transform: translateX(-50%);
}
.el-dialog__wrapper{
  transition-duration: 0!important;
  /deep/.popover-dialog.el-dialog{
    margin-top: 0!important;
    background: transparent;
    animation-duration: 0s;
    animation: none;
    .el-dialog__header{
      display: none;
    }
    .el-dialog__body{
      padding: 0;
    }
  }
}
.popover-visible-box{
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}
</style>
<style lang="scss">
.table-show{
  position: relative;
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
  .empty-view{
    position: absolute;
    left: calc(50% - 95px);
    text-align: center;
    z-index: 1;
    img{
      display: block;
    }
    div{
      height: 18px;
      font-family: Microsoft YaHei-Regular, Microsoft YaHei;
      font-weight: 400;
      font-size: 14px;
      color: #3D3D3D;
      line-height: 18px;
    }
  }
  .menu-table .table-body-box .el-table__body-wrapper .el-table__body .el-table__row .el-table__cell:nth-last-of-type(1) .cell div .el-button--text span{
    font-weight: bold!important;
  }
}
.line-button-pover{
  min-width: auto;
  margin-top: 0!important;
}
</style>
