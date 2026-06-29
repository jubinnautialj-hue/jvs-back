<template>
  <div :class="{'table-show': true, 'alone-page': alonePage}" @click.stop="modelDisplayClose">
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
      v-if="alreadLoad"
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
      :jvsAppId="jvsQueryData.jvsAppId"
      :sortsList="sortsList"
      :displayType="tableShowType"
      rowKey="id"
      :fromDialog="fromDialog"
      :dialogHeight="dialogHeight"
      :selectedRows="(openInfo && openInfo.selectedRows) ? openInfo.selectedRows : selectedRows"
      :fromOtherOpen="fromOtherOpen"
      :showGantt="showGantt"
      :ganttOption="(data && data.ganttForm) ? data.ganttForm : null"
      :ganttMin="ganttMin"
      :ganttMax="ganttMax"
      @on-load="getListData('init')"
      @load="nodeLoad"
      @selection-change="selectChange"
      @current-change="handleCurrentChange"
      @sort="sortTableHandle"
      @openModelDisplay="modelDisplayHandle"
      @openForm="openFormHandle">
      <!-- 最顶部 -->
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
          <!-- 顶部显示查询条件 - 展开时 -->
          <jvs-form v-if="query.length > 0 && searchFormType == 'top'" class="search-form search-form-inline" :option="lineFormOption(searchOption)" :formData="queryParams" :isSearch="true" @submit="queryHandle" @reset="resetQueryHandle" :designId="jvsQueryData.id || ''" :jvsAppId="jvsQueryData.jvsAppId">
            <!-- 表单组件前的下拉，input默认模糊匹配，其余默认等于 -->
            <template v-for="sitem in queryBeforeSlot" :slot="sitem.prop+'Before'">
              <el-select v-model="queryOprator[sitem.prop]" :disabled="sitem.disabled" placeholder="请选择" :key="sitem.prop+'-before-search'" class="bofore-append" size="mini" @change="queryOpratorChange(queryOprator[sitem.prop], sitem)">
                <el-option v-for="op in sitem.list" :key="sitem.prop+'-option-item-'+op.value" :label="op.label" :value="op.value"></el-option>
              </el-select>
            </template>
          </jvs-form>
        </div>
      </template>
      <!-- 上方左侧 -->
      <template slot="menuLeft">
        <div class="list-use-top-left" style="display: flex;">
          <span v-for="(item, index) in topBtns" :key="item.name+index" style="margin-left: 16px;display:flex;" v-show="showhidebutton(item, {}) && !(item.enable === false)">
            <jvs-button v-if="item.type == 'btn_import'" :type="index == 0 ? 'primary' : 'text'" icon="el-icon-upload2" size="mini" @click="handleImport(item)">{{item.name}}</jvs-button>
            <jvs-button size="mini" :type="index == 0 ? 'primary' : 'text'" :icon="getBtnIcon(item.type)" v-if="['btn_import', 'btn_print'].indexOf(item.type) == -1" :loading="topBtnLoading[item.permissionFlag]" @click="btnClickHandle(null, index, item)">{{item.name}}</jvs-button>
          </span>
          <span v-if="enableDeleteList && enableDeleteList.length > 0" style="margin-left: 16px;display:flex;">
            <jvs-button size="mini" type="text" icon="el-icon-delete" @click="mulDeleteData">删除</jvs-button>
          </span>
        </div>
      </template>
      <!-- 上方右侧 - 搜索栏收起时 -->
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
          <p v-if="query.length > 0 && searchFormType == 'right' && data && !data.hiddenSearch" class="search-p">
            <el-input placeholder="请输入关键词" prefix-icon="el-icon-search" size="mini" v-model="searchKeyword" clearable @blur="getListData();" @clear="getListData();" @keyup.enter.native="getListData();" :style="(pageSetting && fromOtherOpen) ? 'width: 212px;' : ''"></el-input>
          </p>
          <p v-if="query.length > 0 && searchFormType == 'right' && data && !data.hiddenSearch">
            <el-popover
              v-model="queryShow"
              placement="right"
              :width="Number(searchWidth)+10"
              trigger="click">
              <div style="padding: 20px 20px 10px 30px;position: relative;">
                <div class="el-dialog__headerbtn" style="top: -6px;right: 6px;" @click="queryShow=false;">
                  <i class="el-dialog__close el-icon el-icon-close"></i>
                </div>
                <jvs-form class="search-form" :option="searchOption" :formData="queryParams" @submit="queryHandle" @reset="resetQueryHandle" :designId="jvsQueryData.id || ''" :jvsAppId="jvsQueryData.jvsAppId">
                  <template v-for="sitem in queryBeforeSlot" :slot="sitem.prop+'Before'">
                    <el-select v-model="queryOprator[sitem.prop]" :disabled="sitem.disabled" placeholder="请选择" :key="sitem.prop+'-before-search'" class="bofore-append" size="mini">
                      <el-option v-for="op in sitem.list" :key="sitem.prop+'-tool-option-item-'+op.value" :label="op.label" :value="op.value"></el-option>
                    </el-select>
                  </template>
                </jvs-form>
              </div>
              <span slot="reference" :style="`font-size: 14px;position: relative;${isQuerying ? 'color: #1E6FFF;' : ''}`">
                <i class="el-icon-position" :style="`cursor:pointer;transform: rotate(135deg);position: absolute;left: 0;top: ${(pageSetting && fromOtherOpen) ? '6px' : '0'};`"></i>
                <span style="margin-left: 14px;">筛选</span>
              </span>
            </el-popover>
          </p>
          <p v-if="getSortColumn(option.column).length > 0 && !data.hiddenSort">
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
          <p @click="queryHandle(queryParams)" v-if="data && !data.hiddenRefresh" style="font-size: 14px;">
            <i class="el-icon-refresh-right" style="cursor:pointer;"></i>
            <span>刷新</span>
          </p>
          <p v-if="query.length > 0 && data && !data.hiddenSearch" @click="changeQueryShow" style="font-size: 14px;">
            <i :class="{'el-icon-arrow-down': searchFormType== 'right', 'el-icon-arrow-up': searchFormType == 'top'}" style="cursor:pointer;"></i>
            <span>{{searchFormType == 'right' ? '展开' : '收起'}}</span>
          </p>
          <el-popover v-if="!this.fromOtherOpen && !this.fromDialog && data && !data.hiddenShare" placement="bottom" trigger="hover" content="点击复制分享地址" popper-class="custom-right-tool-poper">
            <div slot="reference" style="margin-left: 10px;cursor: pointer;height: 32px;display: flex;align-items: center;" @click.stop="copyHandle(`${locationOrigin}/page-design-ui/#/list/use?id=${jvsQueryData.id}&dataModelId=${jvsQueryData.dataModelId}&jvsAppId=${jvsQueryData.jvsAppId}`)">
              <svg class="icon" style="width: 14px;height: 14px;">
                <use xlink:href="#icon-jvs-fenxiang"></use>
              </svg>
            </div>
          </el-popover>
          <!-- <p>
            <span>{{indexAbled ? '关闭' : '显示'}}序号</span>
            <el-switch size="mimi" v-model="indexAbled" @change="forceUpdate()"></el-switch>
          </p> -->
        </el-row>
      </template>
      <!-- tab标签 -->
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
          <div v-if="scope.row.jvsEnabledButtons ? (scope.row.jvsEnabledButtons.length > 0) : (lineBtns && lineBtns.length > 0)">
            <jvs-button :style="item.type === 'btn_delete' ? 'color: #F56C6C;' : ''" size="mini" type="text" v-for="(item, index) in lineBtns" :key="item.name+index" v-if="showhidebutton(item, scope.row) && !(item.enable === false)" @click="btnClickHandle(scope.row, scope.index, item)">{{item.name}}</jvs-button>
          </div>
        </div>
        <div v-else>
          <div v-if="scope.row.jvsEnabledButtons ? (scope.row.jvsEnabledButtons.length > 0 && scope.row.jvsEnabledButtons.length < 4) : (lineBtns && lineBtns.length > 0 && lineBtns.length < 4)">
            <jvs-button :style="item.type === 'btn_delete' ? 'color: #F56C6C;' : ''" size="mini" type="text" v-for="(item, index) in lineBtns" :key="item.name+index" v-if="showhidebutton(item, scope.row) && !(item.enable === false)" @click="btnClickHandle(scope.row, scope.index, item)">{{item.name}}</jvs-button>
          </div>
          <div v-if="scope.row.jvsEnabledButtons ? (scope.row.jvsEnabledButtons && scope.row.jvsEnabledButtons.length > 3) : (lineBtns && lineBtns.length > 3)">
            <jvs-button :style="lineBtns[index].type === 'btn_delete' ? 'color: #F56C6C;' : ''" size="mini" type="text" v-for="(item, index) in lineBtns" :key="lineBtns[index].name+index" v-if="showhidebutton(lineBtns[index], scope.row, 'out')" @click="btnClickHandle(scope.row, scope.index, lineBtns[index])">{{lineBtns[index].name}}</jvs-button>
            <el-popover
              v-show="showMore(scope.row, lineBtns)"
              placement="bottom"
              popper-class="line-button-pover"
              trigger="hover">
              <div class="line-button-list-more">
                <div class="more-item" v-for="(item, index) in lineBtns" :key="'more-item-button-'+index" v-show="showhidebutton(item, scope.row, 'in') && !(item.enable === false)" @click="btnClickHandle(scope.row, scope.index, item)">
                  <jvs-button :style="item.type === 'btn_delete' ? 'color: #F56C6C;' : ''" size="mini" type="text" v-if="index > 1" :key="item.name+'-more-'+index">{{item.name}}</jvs-button>
                </div>
              </div>
              <el-button slot="reference" size="mini" type="text" class="border-text-button" style="margin-left: 10px;">更多</el-button>
            </el-popover>
          </div>
        </div>
      </template>
      <!-- 标签插槽 -->
      <template v-for="(item, key) in getSlotData(option.column)" :slot="item.prop" slot-scope="scope">
        <div :key="item.prop+'-slot-item'+key" class="page-slot-column-div" style="width: 100%;">
          <el-popover
            :ref="`slotPopover-${item.prop}-${scope.index}`"
            v-model="slotPopoverVisible[item.prop+scope.index]"
            :placement="['tableForm', 'reportTable', 'formbox', 'childrenForm', 'connectForm', 'timeline'].indexOf(item.type) > -1 ? 'right' : 'bottom'"
            :width="['tableForm', 'reportTable', 'formbox', 'childrenForm', 'connectForm', 'timeline'].indexOf(item.type) > -1 ? 300 : ''"
            trigger="click">
            <div v-if="['tableForm', 'reportTable', 'formbox', 'childrenForm', 'connectForm', 'timeline'].indexOf(item.type) > -1" class="slot-component-item" style="padding: 10px;">
              <jvs-form :option="getSlotItemCom(item)" :formData="scope.row"></jvs-form>
            </div>
            <div v-if="['htmlEditor'].indexOf(item.type) > -1" style="max-width: 70vw;max-height: 50vh;overflow: auto;padding: 0 10px;box-sizing: border-box;">
              <div v-html="scope.row[item.prop+'_1'] || scope.row[item.prop]"></div>
            </div>
            <div slot="reference" :style="'height: 20px;line-height: 20px;padding: 0 5px;border-radius: 4px;font-size: 14px;cursor: pointer;' + (item.backColor && `background:${item.backColor};border: 0;`)" @click.stop.prevent="closePopover">
              <span :class="{'back-color-text': (item.color && !item.color.startsWith('#'))}" :style="(item.color && (item.color.startsWith('#') ? `color: ${item.color}` : `background:${item.color};`))">{{getSlotItemLabel(scope.row[item.prop], item)}}</span>
            </div>
          </el-popover>
        </div>
      </template>
      <!-- tab打开的列表底部按钮 -->
      <template slot="menuLeftBottom">
        <div v-if="openInfo && openInfo.pageSetting && openInfo.pageSetting.pageBottomBtns && openInfo.pageSetting.pageBottomBtns.length > 0" class="page-dialog-footer" style="margin-left: 20px;">
          <el-button v-for="(btn, bix) in openInfo.pageSetting.pageBottomBtns" :key="'page-dialog-bottom-btn-'+bix" type="primary" size="mini" @click="pageBottomButtonClick(btn)">{{btn.name}}</el-button>
          <el-button size="mini" @click="pageClose">关闭</el-button>
        </div>
      </template>
    </jvs-table>
    <div v-if="tableShowType == 'leftTree'" v-show="!closeLeftTree" :class="{'jvstable-left-tree': true, 'with-search': (retrievalItem && retrievalItem.datatype == 'dataModel')}">
      <div v-if="retrievalItem && retrievalItem.datatype == 'dataModel'"  class="search-input">
        <el-input v-model="treeKeyword" size="mini" :placeholder="`输入${(retrievalItem.props && retrievalItem.props.text) ? retrievalItem.props.text : '关键词'}搜索`" clearable @input="treeKeywordChange"></el-input>
      </div>
      <div :class="{
        'treeBox-title': true,
        'treeBox-title-check': !retrievalValue,
        'treeBox-title-expandall': (retrievalColumn && retrievalColumn.retrievalOption && retrievalColumn.retrievalOption.expandAll)}"
        @click="retrievalSearch({label: '全部', value: ''})">
        <span v-if="retrievalColumn && retrievalColumn.retrievalOption && retrievalColumn.retrievalOption.expandAll" :class="{'allIcon el-icon-caret-right': true, 'close-all': !expandAll}" style="color: #868BA1;padding: 6px;" @click.stop="expandAll = !expandAll"></span>
        <span>{{(retrievalColumn && retrievalColumn.retrievalOption && retrievalColumn.retrievalOption.allLabel) ? retrievalColumn.retrievalOption.allLabel : '全部'}}</span>
      </div>
      <div v-if="leftTreeLoading" class="left-tree-loading"></div>
      <el-tree
        v-else
        ref="jvsTableLeftTree"
        :key="expandAll"
        :data="retrievalOption"
        :node-key="retrievalProps.id ? retrievalProps.id : retrievalProps.value"
        :default-expand-all="expandAll"
        @node-click="handleNodeClick"
        :expand-on-click-node="false"
        :loading="leftTreeLoading"
        :props="retrievalProps"
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
      :visible.sync="formVisible"
      :fullscreen="['oa_workflow'].indexOf(itemData.type) > -1  ? infoFullScreen : (formType == 'normalForm' || formType == 'detailForm') ? formOption.fullscreen: false"
      :class="{'form-fullscreen-dialog': (formType == 'normalForm' || formType == 'detailForm') ? (formOption.fullscreen ? hasTabItem(formOption.fullscreen) : formOption.fullscreen): true,
        'drawer-popup-dialog flow-task-info-dialog': ['oa_workflow'].indexOf(itemData.type) > -1,
        'drawer-popup-dialog': (!formOption.fullscreen && formOption.popupType == 'drawer'),
        'form-with-log': (['btn_add', 'oa_workflow'].indexOf(itemData.type) == -1 && itemData.position == 'line' && formOption.dataLogEnable)}"
      append-to-body
      :width="['oa_workflow'].indexOf(itemData.type) > -1 ? '95%' : (formOption.popupWidth ? (formOption.popupWidth + '%') : '')"
      :modal="true"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :before-close="handleCloseForm">
      <el-row v-if="itemData.formId || tagSetting || ['btn_add', 'oa_workflow', 'oa_workflow_restart', 'oa_workflow_cancel'].indexOf(itemData.type) == -1" class="tag-tool-bar" style="position:absolute;">
        <el-popover placement="bottom" trigger="hover" content="点击复制分享地址" popper-class="custom-right-tool-poper">
          <span slot="reference" class="log-tag-span-box" @click.stop="copyHandle(`${locationOrigin}/page-design-ui/#/form/use?id=${itemData.formId}&dataModelId=${jvsQueryData.dataModelId}&jvsAppId=${jvsQueryData.jvsAppId}${dataId ? ('&dataId='+dataId+'&pageId='+jvsQueryData.id) : ''}`)">
            <svg class="icon">
              <use xlink:href="#icon-jvs-fenxiang"></use>
            </svg>
          </span>
        </el-popover>
        <el-popover
          v-if="['btn_add', 'oa_workflow'].indexOf(itemData.type) == -1 && itemData.position == 'line' && formOption.logsEnable !== false"
          placement="bottom"
          v-model="logshow"
          trigger="click">
          <div v-show="logshow" class="time-line-info-box">
            <el-timeline>
              <el-timeline-item
                v-for="(item, index) in logList"
                :key="'log-list-item-'+index"
                :timestamp="item.timestamp">
                <div class="line-item">
                  <div class="line-item-top">
                    <div class="left">
                      <img :src="item.headImg" alt="">
                      <span>{{item.userName}}</span>
                    </div>
                    <div class="right">
                      <span>{{item.timestamp}}</span>
                    </div>
                  </div>
                  <div class="line-item-content" v-html="item.content"></div>
                </div>
              </el-timeline-item>
            </el-timeline>
          </div>
          <span slot="reference" class="log-tag-span-box">
            <svg aria-hidden="true" @click.stop="getFormEditLogsHandle">
              <use xlink:href="#jvs-ui-icon-lishijilu"></use>
            </svg>
          </span>
        </el-popover>
        <el-popover
          v-if="tagSetting"
          v-model="tagshow"
          placement="bottom"
          trigger="click">
          <div v-show="tagshow" class="tag-set-info-box">
            <h3>标签</h3>
            <div id="formTag" style="overflow:hidden;width: 380px;">
              <h4 style="background:#409EFF;color:#fff;font-size:18px;margin:0;padding:15px 10px;font-weight:normal;border-radius: 4px 4px 0 0;">
                <span>{{(tagDataTransform && tagSetting.title.businessId) ? tagSetting.title.businessId : (tagSetting.title.text || '主标题')}}</span>
                <br/>
                <span style="font-size:12px;">{{(tagDataTransform && tagSetting.subTitle.businessId) ? tagSetting.subTitle.businessId : (tagSetting.subTitle.text || '')}}</span>
              </h4>
              <div style="display:flex;justify-content:space-between;padding:10px 15px;background:#f5f7fa;
                border-radius: 0 0 4px 4px;border-top:0;overflow:hidden;box-sizing: content-box;">
                <div style="font-size: 15px;">
                  <p v-for="(fi, fix) in tagSetting.fieldList" :key="'tag-field-item-'+fix">
                    <b style="color:#333;">{{fi.text}}</b>
                    <span v-if="fi.text && fi.businessId && tagDataTransform" style="color:#666;">{{':'+fi.businessId}}</span>
                  </p>
                </div>
                <div style="text-align:right;">
                  <span id="tagQRcode" ref="tagQRcode" style="display:block;width:130px;height:130px;"></span>
                  <!-- <span style="color:#c0c4cc;">created by jvs</span> -->
                </div>
              </div>
            </div>
            <div style="width: 100%;display: flex;justify-content: flex-end;align-items: center;margin-top: 10px;">
              <el-button size="mini" type="text" @click="downloadTag">下载图片</el-button>
            </div>
          </div>
          <span slot="reference" class="log-tag-span-box">
            <svg aria-hidden="true" @click.stop="getTagDetail">
              <use xlink:href="#jvs-ui-icon-changguikapian"></use>
            </svg>
          </span>
        </el-popover>
      </el-row>
      <div :class="{'form-box': true, 'form-box-2': (['btn_add', 'oa_workflow'].indexOf(itemData.type) == -1 && itemData.position == 'line' && formOption.dataLogEnable)}">
        <div class="form-body">
          <jvs-form
            ref="ruleForm"
            :class="{'show-form': true, 'disabled-show-form': (itemData && itemData.type == 'btn_detail')}"
            v-if="(formType == 'normalForm' || formType == 'detailForm') &&  ['oa_workflow', 'oa_workflow_cancel'].indexOf(itemData.type) == -1 && formVisible"
            :option="formOption"
            :formData="formData"
            :rowData="rowData"
            :designId="itemData.formId || formDesignId"
            :dataModelId="editRetrievalItem ? editRetrievalItem.formId : jvsQueryData.dataModelId"
            :execsList="execsList"
            :jvsAppId="jvsQueryData.jvsAppId"
            :associationSettingsFields="associationSettingsFields"
            :openByButton="itemData"
            @submit="formSubmit"
            @cancalClick="handleCloseForm">
            <!-- 自定义按钮 -->
            <template slot="formButton" v-if="true || formOption.flag">
              <span v-if="hasPrint && printTemplateList && printTemplateList.length > 0" style="margin: 0 10px;">
                <el-popover
                  v-if="printTemplateList.length > 1"
                  placement="right"
                  trigger="hover">
                  <div class="print-list-items">
                    <ul>
                      <li v-for="pi in printTemplateList" :key="pi.id" @click="printHandle(pi)">
                        <span>{{pi.name}}</span>
                      </li>
                    </ul>
                  </div>
                  <jvs-button v-if="hasPrint" slot="reference" size="mini">打印</jvs-button>
                </el-popover>
                <jvs-button v-if="hasPrint && printTemplateList.length == 1" slot="reference" size="mini" @click="printHandle(printTemplateList[0])">打印</jvs-button>
              </span>
              <jvs-button size="mini" v-for="(item, index) in formOption.btnSetting" v-if="['submit', 'empty', 'print', 'cancel'].indexOf(item.buttonType) == -1 && item.enable" :key="item.name+'slotbtn'+index" :loading="item.loading" @click="slotbtnClickHandle(item, index)">{{item.name}}</jvs-button>
            </template>
            <template v-else slot="formButton">
              <span v-if="hasPrint && printTemplateList && printTemplateList.length > 0" style="margin-left:10px;">
                <el-popover
                  v-if="printTemplateList.length > 1"
                  placement="right"
                  trigger="hover">
                  <div class="print-list-items">
                    <ul>
                      <li v-for="pi in printTemplateList" :key="pi.id" @click="printHandle(pi)">
                        <span>{{pi.name}}</span>
                      </li>
                    </ul>
                  </div>
                  <jvs-button v-if="hasPrint" slot="reference" size="mini">打印</jvs-button>
                </el-popover>
                <jvs-button v-if="hasPrint && printTemplateList.length == 1" size="mini" @click="printHandle(printTemplateList[0])">打印</jvs-button>
              </span>
            </template>
          </jvs-form>
          <!-- 详情表单打印 -->
          <el-row v-if="itemData.type == 'btn_detail' && printTemplateList && printTemplateList.length > 0" style="display:flex;align-items:center;justify-content: center;">
            <span v-if="printTemplateList.length > 1">
              <el-popover
                placement="right"
                trigger="hover">
                <div class="print-list-items">
                  <ul>
                    <li v-for="pi in printTemplateList" :key="pi.id" @click="printHandle(pi)">
                      <span>{{pi.name}}</span>
                    </li>
                  </ul>
                </div>
                <jvs-button slot="reference" size="mini">打印</jvs-button>
              </el-popover>
            </span>
            <jvs-button v-if="printTemplateList.length == 1" size="mini" @click="printHandle(printTemplateList[0])">打印</jvs-button>
          </el-row>
          <jvs-form-level v-if="formType == 'multiLevelForm'" :option="formOption" :formData="formData" @submit="formSubmit">
          </jvs-form-level>
          <taskForm v-if="itemData.type == 'oa_workflow' && formVisible" :rowData="rowData" @close="closeHandle" @closeHandle="closeHandle" @fullscreenChange="infoFullScreen=!infoFullScreen;" />
          <div v-if="itemData.type == 'oa_workflow_cancel'">
            <el-input type="textarea" :rows="2" placeholder="请输入理由" v-model="formData.reason"></el-input>
            <el-row style="margin-top:20px;display:flex;align-items:center;justify-content:center;">
              <jvs-button size="mini" type="primary" @click="cacelDelSubmit" :reasonLoading="reasonLoading">确定</jvs-button>
              <jvs-button size="mini" @click="reasonClose">取消</jvs-button>
            </el-row>
          </div>
        </div>
        <div class="form-log-box" v-if="['btn_add', 'oa_workflow'].indexOf(itemData.type) == -1 && itemData.position == 'line' && formOption.dataLogEnable">
          <formLog v-if="formVisible" :jvsAppId="jvsQueryData.jvsAppId" :dataModelId="editRetrievalItem ? editRetrievalItem.formId : jvsQueryData.dataModelId" :dataId="rowData.id"></formLog>
        </div>
      </div>
    </el-dialog>
    <!-- 导入数据 -->
    <el-dialog
      title="导入数据"
      :visible.sync="importVisible"
      width="720px"
      append-to-body
      :close-on-click-modal="false"
      :before-close="handleCloseImport">
      <div class="import-data-box">
        <el-upload
          ref="importUpload"
          class="import-data-upload"
          :action="(importBtn.preHttp && importBtn.preHttp.url) ? importBtn.preHttp.url : `/mgr/jvs-design/app/use/${jvsQueryData.jvsAppId}/dynamic/data/import/${jvsQueryData.dataModelId}?designId=${jvsQueryData.id}`"
          :file-list="fileList"
          :headers="importHeaders"
          :on-success="uploadChangeHandle"
          :before-upload="beforeUpload"
          :show-file-list="false"
          :disabled="!menuId"
          :on-error="uploadError"
          drag
          multiple>
          <div class="el-upload__text">
            <svg aria-hidden="true" style="width: 24px; height: 24px;margin-bottom: 16px">
              <use xlink:href="#icon-upload"></use>
            </svg>
            <div>点击或者拖动文件到虚线框内上传</div>
            <div style="color: #a2a3a5;font-size: 12px;margin-top: 8px;">支持xls，xlsx等类型的文件</div>
          </div>
        </el-upload>
        <div class="upload-explain">
          <span style="color: #a2a3a5;">上传的Excel表符合以下规范：</span>
          <ul>
            <li style="list-style: disc">文件大小不超过<span>10M</span>，且单个sheet页数据量不超过5000行</li>
            <li style="list-style: disc">仅支持<span>（*.xls和*.xlsx）</span>文件</li>
            <li style="list-style: disc">请确保您需要导入的sheet表头中<span>不包含空的单元格</span>，否则该sheet页数据系统将不做导入</li>
            <li style="list-style: disc">批量导入的数据不支持“内置变量”作为条件的过滤</li>
            <li style="list-style: disc">导入文件<span>不支持Excel公式计算</span>，如SUM，=H2*J2等</li>
          </ul>
        </div>
      </div>
    </el-dialog>
    <!-- 审核流程 -->
    <el-dialog
      title="节点选择"
      class="place-dialog"
      :visible.sync="nodeSelectVisible"
      width="20%"
      :close-on-click-modal="false"
      :before-close="nodeSelectClose">
      <div class="place-form-desc">选择需要审核的节点</div>
      <div class="place-form-list">
        <div class="palce-form-item" v-for="item in nodeSelectList" :key="item.nodeId+'-place-item'" @click="nodeSelectHandle(item)">
          <!-- <svg aria-hidden="true" style="margin-right: 20px;width: 25px;height: 25px;">
            <use :xlink:href="'#' + item.icon"></use>
          </svg> -->
          <span>{{item.nodeName}}</span>
        </div>
      </div>
    </el-dialog>
    <!-- 流程进度 -->
    <el-dialog
      title="进度"
      :visible.sync="infoVisible"
      append-to-body
      class="drawer-popup-dialog flow-task-info-dialog"
      width="95%"
      :modal="true"
      :close-on-click-modal="false"
      :fullscreen="infoFullScreen"
      :before-close="infoClose">
      <flowInfo v-if="infoVisible" :rowData="rowData" :btnHide="true" @closeHandle="infoClose" @fullscreenChange="infoFullScreen=!infoFullScreen;" />
    </el-dialog>
    <!-- 打开其他列表页 -->
    <el-dialog
      class="form-fullscreen-dialog page-open-fullscreen-dialog"
      :title="(rowData && itemData.dialogTitle && (rowData[itemData.dialogTitle+'_1'] || rowData[itemData.dialogTitle])) || formTitle"
      :visible.sync="pageVisible"
      :fullscreen="itemData.dialogWidth == 100 ? true : false"
      :width="itemData.dialogWidth ? (itemData.dialogWidth+'%') : '70%'"
      append-to-body
      :before-close="pageClose"
      @opened="pageDialogOpend">
      <div v-if="pageVisible" id="outerbox" :class="{'page-dialog-box': true ,'has-bottom-tool': (itemData.pageBottomBtns && itemData.pageBottomBtns.length > 0)}">
        <listPage
          ref="openPage"
          class="jvs-table-opend-by-out"
          :routerQuery="{id: itemData.formId, dataModelId: itemData.dataModelId, jvsAppId: jvsQueryData.jvsAppId}"
          :fromDialog="true"
          :dialogHeight="opendPageDialogHeight"
          :pageQueryFixCondition="getPageFixQuery()"
          :pageSetting="getPageSetting()"
          :originRowData="rowData"
          :selectedRows="getSelectdList()"
        ></listPage>
      </div>
      <div v-if="pageVisible && itemData.pageBottomBtns && itemData.pageBottomBtns.length > 0" class="page-dialog-footer">
        <el-button v-for="(btn, bix) in itemData.pageBottomBtns" :key="'page-dialog-bottom-btn-'+bix" type="primary" size="mini" @click="pageBottomButtonClick(btn)">{{btn.name}}</el-button>
        <el-button size="mini" @click="pageClose">取消</el-button>
      </div>
    </el-dialog>
    <!-- 列打开关联显示信息 -->
    <el-dialog :visible.sync="modelDisplayShow" :modal="false" custom-class="popover-dialog">
      <div v-if="modelDisplayShow" class="el-popover el-popper el-popover--plain modelDisplay-box" :x-placement="(modelDisplayPos && modelDisplayPos.placement) ? modelDisplayPos.placement : 'bottom'" :style="`left: ${modelDisplayPos.x}px;top: ${modelDisplayPos.y}px;transform: translateX(${modelDisplayPos.x > 0 ? '-50%' : '0'});`" @click.stop="stopHandle" v-click-outside="modelDisplayClose">
        <div class="slot-component-item" style="padding: 10px 10px 0 10px;overflow: auto;max-width: 50vw;max-height: 500px;">
          <div v-loading="linkModelLoading" class="show-other-model-info-table">
            <div class="header-body">
              <div class="header">
                <div v-for="cit in modelDisplayItem.modelDisplay.linkageFieldKeys" :key="'model-coloumn-item-'+cit.prop" class="item" :style="`${(cit.advancedSettings && cit.advancedSettings.showWidth > 0) ? ('width:'+cit.advancedSettings.showWidth+'px;') : ''}`">{{cit.label}}</div>
              </div>
              <div v-if="linkModelData && linkModelData.length > 0" class="body">
                <div v-for="(dit, dix) in linkModelData" :key="'model-column-item-data-row'+'-'+dix" class="body-item">
                  <div v-for="(dct, dcx) in modelDisplayItem.modelDisplay.linkageFieldKeys" :key="'model-column-item-data-row-item'+'-'+dix+dct.prop+'-'+dcx" class="body-item-col" :style="`${(dct.advancedSettings && dct.advancedSettings.showWidth > 0) ? ('width:'+dct.advancedSettings.showWidth+'px;') : ''}`">
                    <img v-if="['imageUpload', 'image'].indexOf(dct.type) > -1" :src="(dit[dct.prop] && dit[dct.prop].length > 0) ? dit[dct.prop][0].url : ''" alt="" @click.stop="previewImage(dit[dct.prop])">
                    <div v-else-if="['fileUpload', 'file'].indexOf(dct.type) > -1 && dit[dct.prop] && dit[dct.prop].length > 0">
                      <el-popover v-if="dit[dct.prop].length > 1" trigger="hover" placement="bottom">
                        <div style="padding: 0 20px;">
                          <div v-for="(file, fix) in dit[dct.prop]" :key="'model-display-file-'+fix" class="file-name" :title="file.name" @click.stop="previewFile(file)">{{file.name}}</div>
                        </div>
                        <el-button type="text" slot="reference">查看文件</el-button>
                      </el-popover>
                      <div v-else class="file-name" :title="dit[dct.prop][0].name" @click.stop="previewFile(dit[dct.prop][0])">{{dit[dct.prop][0].name}}</div>
                    </div>
                    <div class="text-div" v-else-if="styleRowItem(dit, dct, 'text')" :style="`background: ${styleRowItem(dit, dct, 'bgcolor')};`+`${styleRowItem(dit, dct, 'bgcolor') ? 'width: fit-content;padding: 0 10px;' : ''}`">
                      <span :style="styleRowItem(dit, dct, 'color').startsWith('#') ? `color: ${styleRowItem(dit, dct, 'color')}` : `${styleRowItem(dit, dct, 'color')}`">{{styleRowItem(dit, dct, 'text')}}</span>
                    </div>
                    <i v-if="dct.advancedSettings && dct.advancedSettings.tools && dct.advancedSettings.tools.indexOf('copy') > -1" class="el-icon-copy-document" style="margin-left: 5px;cursor: pointer;" @click="copyHandle(dit[dct.prop+'_1'] ? dit[dct.prop+'_1'] : (dit[dct.prop] instanceof Array ? (dit[dct.prop].length > 0 ? dit[dct.prop].join(',') : '') : dit[dct.prop]))"></i>
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
        <div class="popper__arrow" :style="{'left': (modelDisplayPos.transX ? `calc(50% + ${modelDisplayPos.transX}px)` : '50%')}"></div>
      </div>
    </el-dialog>

    <!-- 预览图片 -->
    <el-image-viewer v-if="showViewer" ref="imageViewer" :z-index="9999" :initialIndex="previewImageIndex" :url-list="previewSrcList" :on-close="closeViewer"/>

    <!-- popover遮罩 -->
    <div v-show="popoverVisible" class="popover-visible-box" @click="popoverClose"></div>
  </div>
</template>
<script>
import {sendRequire, sendMyRequire, getKeyValue, pageGetLinkModelData} from '../../api/list'
import {
  delSingleData,
  downloadTemplate,
  exportData,
  getDesignTableInfo,
  getSingleData,
  exportListData,
  delMultipleData
} from '../../api/design'
import { getClassifyDictTree, getSystemDictItems } from '@/api/newDesign'
import {getCrudDataPage, getStatistics, association, updatePageRelation, getUrlWithParamter} from '../../api/newDesign'
import pinyin from 'js-pinyin'
import store from "@/store";
import { Base64 } from 'js-base64'
import {getFormInfo, getDetail, getFormTagData, getFormEditLogs, getDataStr} from '../../api/formlist'
import {ruleRun, ruleDownLoad} from "@/api/common";
import {getSelectData} from '@/api/index'
import taskForm from '@/views/flowable/views/taskForm'
import flowInfo from '@/views/flowable/views/info'
import ShowForm from "@/views/flowable/views/componet/info";
import {getUserInfo} from "@/api/admin/user";
import {getDefaultData} from "@/views/page/util/common";
import { getAvailableTemplate } from '@/views/print/api/index'
import {getDeptList, getRoleList, getPostList} from '@/components/api'
import { candelProcess } from '@/views/flowable/api/flowable'
import { restartProcess } from "@/views/flowable/views/componet/api"
import { encryption } from "@/util/util";
import { enCodeKey } from "@/const/const"
import { getRedirectInfo } from "@/util/admin"
import { getStore } from '@/util/store.js'
export default {
  components: {
    taskForm, flowInfo, ShowForm,
    listPage: () => import('@/views/page/views/show/listUse'),
    formLog: () => import('@/views/page/views/show/formLog'),
    'el-image-viewer': () => import('element-ui/packages/image/src/image-viewer')
  },
  props: {
    propData: {
      type: Object
    },
    infoData: {
      type: Object
    },
    menuForm: {
      type: Object
    },
    routerQuery: {
      type: Object
    },
    fromDialog: {
      type: Boolean
    },
    fromOtherOpen: {
      type: Boolean
    },
    pageQueryFixCondition: {
      type: Array
    },
    pageSetting: {
      type: Object
    },
    originRowData: {
      type: Object
    },
    dialogHeight: {
      type: Number
    },
    selectedRows: {
      type: Array
    },
    isView: {
      type: Boolean
    },
    changeRandom: {
      type: Number
    },
    changeDomItem: {
      type: Object
    },
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
      if(this.lastLeftNode && this.lastLeftNode.moretool) {
        bool = true
      }
      return bool
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
  data () {
    return {
      jvsQueryData: {}, // 菜单组件入参
      importBtn: {},
      importVisible: false, // 导入数据弹窗开关
      permission: true,
      menuId: '',
      data: {},
      option: {
        lazy: true,
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
      searchWidth: 350,
      searchOption: {
        // isSearch: true,
        cancal: false,
        submitBtnText: '查询',
        column: [],
        formAlign: 'right',
        labelwidth: 'auto'
      },
      queryBeforeSlot: [], // 顶部搜索栏展开时，表单组件前面的下拉集合
      queryOprator: {},
      column: [], // 字段集
      topBtns: [], // 顶部按钮
      lineBtns: [], // 行内按钮
      treeBtns: [], // 左树按钮
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
      jvsAppId: '',
      sortsList: [], // 排序条件
      sortShow: false,
      indexAbled: false,
      userInfo: {},
      printTemplateList: [],
      execsList: [], // 表单中触发公式的组件
      tagSetting: null, // 标签设置
      tagshow: false,
      tagDataTransform: false,
      logshow: false,
      logList: [],
      nodeSelectVisible: false,
      queryShow: false,
      nodeSelectList: [],
      searchKeyword: '', // 搜索关键词
      retrievalItem: null, // 检索字段项
      retrievalValue: '', // 检索字段值
      retrievalOption: [], // 字段检索值范围选项
      retrievalProps: {
        label: 'label',
        value: 'value',
        children: 'children'
      },
      leftTreeLoading: true,
      tableShowType: 'table', // 'table', // 'card', // 'leftTree',
      lastLeftNode: null,
      editRetrievalItem: null,
      infoVisible: false,
      enableDeleteList: [],
      reasonLoading: false,
      checkedNode: '',
      pageVisible: false,
      fieldsData: [],
      retrievalColumn: null,
      expandAll: true,
      closeLeftTree: false,
      opendPageDialogHeight: 0,
      treeKeyword: '',
      openInfo: null,
      searchFormType: 'right',
      associationSettingsFields: [],
      infoFullScreen: false,
      alonePage: false,
      emptyView: false,
      linkModelLoading: false,
      linkModelData: [],
      linkModelPage: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
        pageSizes: [20, 50, 100, 200, 500, 1000], // 分页大小
      },
      modelDisplayShow: false,
      modelDisplayItem: null,
      modelDisplayRow: null,
      modelDisplayPos: {
        x: 0,
        y: 0
      },
      showViewer: false,
      previewSrcList: [],
      previewImageIndex: 0,
      showGantt: false,
      ganttMin: '',
      ganttMax: '',
      locationOrigin: location.origin,
      slotPopoverVisible: {},
      topBtnLoading: {},
      recordsData: [],
      lazyItem: null,
      isQuerying: false,
    }
  },
  async created () {
    let redirecInfo = {
      stop: false,
      url: ''
    }
    if(location.pathname == '/page-design-ui/' && !this.fromOtherOpen && !this.fromDialog) {
      this.alonePage = true
      await getRedirectInfo().then(res => {
        redirecInfo = res
      })
    }
    if(this.alonePage && redirecInfo.stop && redirecInfo.url) {
      this.$openUrl(redirecInfo.url, '_self')
      return false
    }
    this.createdHandle()
    if(this.$store.getters && this.$store.getters.labelValue) {
      this.labelValue = this.$store.getters.labelValue
    }else{
      this.getKeyValueHandle()
    }
    if(this.$store.getters && this.$store.getters.userInfo) {
      this.userInfo = JSON.parse(JSON.stringify(this.$store.getters.userInfo))
    }else{
      this.getUserInfo()
    }
    if(this.menuForm) {
      this.menuName = this.menuForm.menuName
    }
    // 在线
    this.initQueryInfo()
    this.clientHeight = document.documentElement.clientHeight
    this.getDesignDataStr()
    if(this.jvsQueryData && this.jvsQueryData.openType == 'tab') {
      let tabPage = JSON.parse(localStorage.getItem('tabPage'))
      if(tabPage[this.jvsQueryData.id]) {
        this.openInfo = tabPage[this.jvsQueryData.id]
      }
    }
    // 监听页面传值
    let _this = this
    window.addEventListener('message',function(e){
      if(e.data) {
        if(e.data.command == 'freshPage' && e.data.freshId && e.data.freshId == _this.jvsQueryData.id) {
          _this.getListData()
        }
      }
    })
    if(localStorage.getItem('pageListQueryType')) {
      this.searchFormType = localStorage.getItem('pageListQueryType') == 'top' ? 'top' : 'right'
    }
  },
  methods: {
    createdHandle () {
      if(this.routerQuery) {
        for(let k in this.routerQuery) {
          this.$set(this.jvsQueryData, k, this.routerQuery[k])
        }
      }else{
        this.jvsQueryData = JSON.parse(JSON.stringify(this.$route.query))
      }
    },
    // 分页change
    handleCurrentChange(e) {
      this.getListData()
    },
    getSlotData(arr) {
      return arr.filter(item => {
        // item.backColor ||  || item.openFormId || (item.modelDisplay && item.modelDisplay.displayType == 'model' && item.modelDisplay.showModelType == 'oneToMany')
        return item.slot
      })
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
      if(this.jvsQueryData && this.jvsQueryData.id) {
        this.importHeaders = {
          "Authorization": 'Bearer ' + store.getters.access_token,
          'designId': this.jvsQueryData.id,
          'operator': encodeURI('导入')
        }
        this.menuId = this.jvsQueryData.id
        this.getDesignInfoHandle()
        this.pathQuery = JSON.parse(JSON.stringify(this.jvsQueryData))
      }
    },
    // 获取列表数据
    getListData (oprate) {
      if(this.isView) {
        return false
      }
      this.tableLoading = true
      if (!this.menuId) {
        this.tableData = [this.demoData]
        this.page.total = 1
        this.tableLoading = false
        // this.statisticsHandle()   // 仅用于调试
      } else {
        let obj = {}
        obj.size = this.page.pageSize
        obj.current = this.page.currentPage
        obj.conditions = []
        if(this.searchOption && this.searchOption.column) {
          console.log('searchOption.column', this.searchOption.column)
          for(let i in this.searchOption.column) {
            if(
              this.queryParams[this.searchOption.column[i].prop]
                || this.queryParams[this.searchOption.column[i].prop] === 0
                || this.queryParams[this.searchOption.column[i].prop] === false
                || (typeof this.queryParams[this.searchOption.column[i].prop] == 'number')
                || this.queryOprator[this.searchOption.column[i].prop] == 'isNull'
            ) {
              obj.conditions.push({
                fieldKey: this.searchOption.column[i].prop,
                enabledQueryTypes:
                  this.queryOprator[this.searchOption.column[i].prop] ?
                    this.queryOprator[this.searchOption.column[i].prop] : 'eq',
                value: this.queryParams[this.searchOption.column[i].prop]
              })
            }
          }
        }
        if(obj.conditions && obj.conditions.length > 0) {
          this.isQuerying = true
        }else{
          this.isQuerying = false
        }
        if(this.data.dataPage && this.data.dataPage.http) {
          let tp = JSON.parse(JSON.stringify(this.data.dataPage.http))
          if(this.$store.getters.labelValue && this.$store.getters.labelValue.requestContentType) {
            tp.requestContentType = this.$store.getters.labelValue.requestContentType[tp.requestContentType]
          }
          obj = Object.assign(obj, tp.parameters)
        }
        if(this.retrievalItem && this.retrievalValue) {
          obj.conditions.push({fieldKey: this.retrievalItem.prop, enabledQueryTypes: 'eq', value: this.retrievalValue})
        }
        if(this.searchKeyword) {
          obj.keywords = this.searchKeyword
        }
        if(this.pageQueryFixCondition && this.pageQueryFixCondition.length > 0) {
          obj.conditions = obj.conditions.concat(this.pageQueryFixCondition)
        }
        if(this.openInfo && this.openInfo.pageQueryFixCondition && this.openInfo.pageQueryFixCondition.length > 0) {
          obj.conditions = obj.conditions.concat(this.openInfo.pageQueryFixCondition)
        }
        this.currentQuery = obj
        obj.sorts = this.sortsList.filter( (sit) => {return (sit.fieldKey && sit.direction)})
        getCrudDataPage(this.jvsQueryData.jvsAppId, obj, this.jvsQueryData.dataModelId, this.jvsQueryData.id).then(res => {
          let totalPage = 1
          if(res.data.code == 0 && res.data.data) {
            if(this.tableShowType != 'card') {
              this.setDataFormat(res.data.data.records, 0)
            }
            this.recordsData = res.data.data.records
            this.tableData = this.getDataHandle(this.recordsData)
            this.page.total = res.data.data.total
            this.page.currentPage = res.data.data.current
            this.tableLoading = false
            totalPage = res.data.data.pages
            console.log('tableData', this.tableData, this.option.column)
            this.$emit('pageGetList', this.tableData)
            if(res.data.data.dateList && res.data.data.dateList.length > 1 && this.data.gantt && this.data.ganttForm) {
              this.ganttMin = new Date(res.data.data.dateList[0])
              this.ganttMax = new Date(res.data.data.dateList[1])
            }
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
      if(this.retrievalItem && oprate != 'retrieval' && oprate != 'init') {
        this.getSelectUrlData(this.retrievalItem, 'retrival')
      }
    },
    // 格式化列表数据
    setDataFormat (data, index) {
      for(let i in data) {
        data[i].level = index
        if(data[i].children && data[i].children > 0) {
          data[i].hasChildren = true
          this.setDataFormat(data[i].children, index+1)
        }
      }
    },
    getDataHandle (list) {
      let temp = []
      for(let i in list) {
        let tp = JSON.parse(JSON.stringify(list[i]))
        if(tp.children) {
          tp.hasChildren = true
          tp.children = []
        }
        temp.push(tp)
      }
      return temp
    },
    nodeLoad (tree, treeNode, resolve) {
      if(tree.hasChildren) {
        this.findItem(tree.id, this.recordsData)
        if(this.lazyItem && this.lazyItem.id && this.lazyItem.children && this.lazyItem.children.length > 0) {
          let temp = []
          for(let i in this.lazyItem.children) {
            let tp = JSON.parse(JSON.stringify(this.lazyItem.children[i]))
            if(tp.children) {
              tp.hasChildren = true
              tp.children = []
            }
            temp.push(tp)
          }
          resolve(temp)
        }
      }
    },
    findItem (id, list, item) {
      for(let i in list) {
        if(list[i].id == id) {
          this.lazyItem = list[i]
        }
        if(list[i].children && list[i].children.length > 0) {
          this.findItem(id, list[i].children, item)
        }
      }
    },
    // 存在id时获取数据
    getDesignInfoHandle () {
      this.designColumn = []
      getDesignTableInfo(this.jvsQueryData.jvsAppId, this.menuId).then(res => {
        if(res.data.code == 0 && res.data.data) {
          // console.log(res.data.data)
          if(res.data.data.viewJson) {
            let data = JSON.parse(res.data.data.viewJson)
            console.log('viewJson', data)
            if(data) {
              const arr = [...data.buttons]
              this.jvsAppId = res.data.data.jvsAppId
              this.tableShowType = data.displayType || res.data.data.displayType || 'table'
              this.topBtns = arr.filter(item => {
                return item.position === 'top' && (((item.type == 'btn_import' ) || ['btn_import', 'btn_print'].indexOf(item.type) == -1)) &&  !(item.enable === false)
              })
              // console.log(this.topBtns)
              this.upDataHandle(data)
              // this.getListData()
              if(!res.data.data.isDeploy) {
                this.$notify({
                  title: '提示',
                  message: '该设计未发布',
                  position: 'bottom-right',
                  type: 'warning'
                });
              }
              if(!(data.dataPage && data.dataPage.autoTableFields && data.dataPage.autoTableFields.length > 0)) {
                this.emptyView = true
              }
              if(data.gantt && data.ganttForm) {
                if(data.ganttForm.plainStart && data.ganttForm.plainEnd && data.ganttForm.reallyStart && data.ganttForm.reallyEnd) {
                  this.showGantt = true
                  !this.data.ganttForm.showType &&
                    (this.data.ganttForm.showType = "month"); // 甘特图默认按月展示
                }
              }
            }else{
              this.emptyView = true
            }
          }else{
            this.emptyView = true
          }
        }else{
          this.emptyView = true
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

      // 按钮
      let tb = []
      let lb = []
      let isSelect = false
      let showMenu = false
      for(let i in this.data.buttons) {
        if(this.data.buttons[i].position == 'top') {
          tb.push(this.data.buttons[i])
          if(['btn_form', 'btn_rule_design'].indexOf(this.data.buttons[i].type) > -1 && this.data.buttons[i].enable) {
            isSelect = true
          }
        }
        if(this.data.buttons[i].position == 'line') {
          if(this.data.buttons[i].name && this.data.buttons[i].type) {
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
      if(this.pageSetting && this.pageSetting.pageBottomBtns && this.pageSetting.pageBottomBtns.length > 0) {
        isSelect = true
      }
      if(this.openInfo && this.openInfo.pageSetting && this.openInfo.pageSetting.pageBottomBtns && this.openInfo.pageSetting.pageBottomBtns.length > 0) {
        isSelect = true
      }
      // 存在导出删除 或有底部按钮 时，表格可多选
      if(isSelect) {
        this.selectable = true
      }
      // this.topBtns = tb
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
          width: countShow > 4 ? (this.column[i].showWidth ? this.column[i].showWidth : ((this.column[i].advancedSettings && this.column[i].advancedSettings.showWidth) ? this.column[i].advancedSettings.showWidth : '')) : 'auto',
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
          if(this.column[i].advancedSettings.backColor){
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
          if(obj.type == 'image' && this.column[i].advancedSettings.width && this.column[i].advancedSettings.height) {
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
        // 选项卡、表格、静态表格、表单卡片、子表单、关联表单、时间线
        if(['tab', 'tableForm', 'reportTable', 'formbox', 'childrenForm', 'connectForm', 'timeline'].indexOf(obj.type) > -1){
          // console.log(this.column[i])
          obj.slot = true
          obj.hide = true
          // if(this.column[i].designJson && this.column[i].designJson.formId) {
          //   if(this.column[i].designJson.formId) {
          //     if(this.column[i].designJson.type == 'connectForm' && this.column[i].designJson.url) {
          //       await getSelectData(this.column[i].designJson.url, 'get', null, this.column[i].designJson.formId).then(res => {
          //         if(res.data.code === 0) {
          //           let selectOption = []
          //           for(let sitem in res.data.data){
          //             if(typeof res.data.data[sitem] == 'string') {
          //               selectOption.push({
          //                 label: res.data.data[sitem],
          //                 value: res.data.data[sitem]
          //               })
          //             }else{
          //               selectOption.push(res.data.data[sitem])
          //             }
          //           }
          //           obj.dicData = selectOption
          //           obj.datatype = 'option'
          //           obj.type = 'select'
          //           obj.slot = false
          //           obj.props = {
          //             label: obj.prop,
          //             value: 'id'
          //           }
          //         }
          //       })
          //     }
          //     // this.getFormDesignByFormId(this.column[i].designJson, obj)
          //   }
          // }
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
        }
        if(obj.type == 'fileUpload' || obj.type == 'file') {
          obj.type = 'file'
        }
        if(obj.type == 'htmlEditor') {
          obj.slot = true
        }
        if(this.column[i].enableStatistics == true) {
          obj.enableStatistics = true
          bool = true
        }
        temp.push(obj)
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
              this.leftTreeLoading = false
            }else{
              this.getSelectUrlData(this.column[i].designJson, 'retrival')
            }
          }
        }
      }
      this.option.column = temp
      console.log('column', temp)
      this.option.indexLabel = '序号'
      this.option.showsummary = bool
      console.log('表格列初始化', this.data)
      this.indexAbled = !this.data.hiddenIndex // 是否展示序号列
      if(this.data.menuFixed) {
        this.option.menuFix = 'right'
      }
      if(this.data.menuWidth) {
        this.option.menuWidth = this.data.menuWidth || 150
      }
      this.query = queryTemp
      // console.log(queryTemp)
      // 生成查询条件配置
      this.getQueryColumn()
      this.alreadLoad = true
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
      // console.log('query', this.query)
      for(let i in this.query) {
        // 自定义了查询条件
        if(this.query[i].queryConditionConfig && this.query[i].queryConditionConfig.type) {
          let obj = {...this.query[i].queryConditionConfig, span: 24, beforeSlot: true}
          temp.push(obj)
          this.queryParams[this.query[i].queryConditionConfig.prop] = null
          this.queryBeforeSlot.push({prop: obj.prop, list: this.query[i].queryConditionConfig.queryTypes || [{label: '等于', value: 'eq'}], disabled: obj.disabled})
        }else{
          // console.log('store.getters.labelValue', this.$store.getters.labelValue)
          let fitem = this.$store.getters.labelValue.fieldTypeMore[this.query[i].dbJavaType]
          // console.log('fitem', fitem)
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
          // console.log('obj', obj)
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
              obj.startLimit = ''
              obj.endLimit = ''
            }
            if(obj.datetype == 'datetime') {
              normalDatePicker = true
            }
            delete obj.defaultValue
          }
          if(obj.type == 'timePicker') {
            if(obj.isrange) {
              obj.searchSpan = 24
            }
          }
          if(['timePicker', 'timeSelect'].indexOf(obj.type) > -1) {
            obj.notRightNow = true
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
          if(this.pageSetting && this.pageSetting.pageSearch && this.pageSetting.pageSearch.length > 0) {
            this.pageSetting.pageSearch.filter(ppit => {
              if(ppit.fieldKey == obj.prop) {
                if(this.originRowData && this.originRowData[ppit.value]) {
                  if(ppit.disabled) {
                    obj.disabled = true
                  }
                  if(ppit.value){
                    this.$set(this.queryParams, ppit.fieldKey, this.originRowData[ppit.value+'_1'] ? this.originRowData[ppit.value+'_1'] : this.originRowData[ppit.value])
                  }
                  if(ppit.dataFileterList && ppit.dataFileterList.length > 0) {
                    if(obj.datatype == 'option') {
                      console.log(obj.dicData)
                    }else{
                      if(obj.dataFilterGroupList) {
                        obj.dataFilterGroupList.push(ppit.dataFileterList)
                      }else{
                        obj.dataFilterGroupList = [ppit.dataFileterList]
                      }
                    }
                  }
                }
              }
            })
          }
          // 其他列表通过tab打开
          if(this.openInfo && this.openInfo.pageSetting && this.openInfo.pageSetting.pageSearch && this.openInfo.pageSetting.pageSearch.length > 0) {
            this.openInfo.pageSetting.pageSearch.filter(ppit => {
              if(ppit.fieldKey == obj.prop) {
                if(this.openInfo.originRowData && this.openInfo.originRowData[ppit.value]) {
                  if(ppit.disabled) {
                    obj.disabled = true
                  }
                  if(ppit.value){
                    this.$set(this.queryParams, ppit.fieldKey, this.openInfo.originRowData[ppit.value+'_1'] ? this.openInfo.originRowData[ppit.value+'_1'] : this.openInfo.originRowData[ppit.value])
                  }
                  if(ppit.dataFileterList && ppit.dataFileterList.length > 0) {
                    if(obj.datatype == 'option') {
                      console.log(obj.dicData)
                    }else{
                      if(obj.dataFilterGroupList) {
                        obj.dataFilterGroupList.push(ppit.dataFileterList)
                      }else{
                        obj.dataFilterGroupList = [ppit.dataFileterList]
                      }
                    }
                  }
                }
              }
            })
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
                case 'allin':
                  str = '全包含';
                  break;
                case 'isNull':
                  str = '等于空';
                  break;
                default: ;break;
              }
              let add = true
              // 日期组件，年单位不支持范围
              if(this.query[i].enabledQueryTypes[q] == 'between' && obj.datetype == 'year') {
                add = false
              }
              if(str && add) {
                queryTypes.push({label: str, value: this.query[i].enabledQueryTypes[q]})
              }
            }
            let mulBool = false
            if(['datetimerange', 'daterange', 'monthrange'].indexOf(obj.datetype) > -1 || (obj.type == 'timePicker' && obj.isrange)) {
              mulBool = true
            }
            this.queryBeforeSlot.push({prop: obj.prop, list: mulBool ?  [{label: '之间', value: 'between'}, {label: '等于空', value: 'isNull'}] : queryTypes, disabled: obj.disabled, multiple: mulBool})
            this.$set(
              this.queryOprator, obj.prop,
                mulBool ? 'between'
                  : obj.type === 'input' ? 'like'
                  : (queryTypes && queryTypes.length > 0) ? queryTypes[0].value
                  : ''
            ) // 追加默认值
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
    getUserInfo() {
      getUserInfo().then(res => {
        if(res.data.code == 0) {
          this.userInfo = res.data.data
        }
      })
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
          if(this.formOption.btnSetting[i].buttonType == 'print' && this.formOption.btnSetting[i].enable) {
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
        submitLoading: false,
        emptyBtn: formsetting.emptyBtn,
        popupType: formsetting.popupType || 'dialog',
        popupWidth: formsetting.popupWidth || 50,
        logsEnable: formsetting.logsEnable === false ? false : true,
        dataLogEnable: formsetting.dataLogEnable ? true : false,
        formTitleKey: formsetting.title ? formsetting.title : ''
      }
      if(type == 'detailForm') {
        temp.disabled = true
        temp.btnHide = true
      }else{
        temp.disabled = false
        temp.btnHide = false
      }
      let hasSub = false
      let hasEmpt = false
      let hasCancel = false
      for(let i in temp.btnSetting) {
        if(temp.btnSetting[i].buttonType == 'submit') {
          if(temp.btnSetting[i].enable) {
            hasSub = true
            temp.submitBtn = true
            temp.submitBtnText = temp.btnSetting[i].name || '提交'
          }
        }
        if(temp.btnSetting[i].buttonType == 'empty') {
          if(temp.btnSetting[i].enable) {
            hasEmpt = true
            temp.emptyBtn = true
            temp.emptyBtnText = temp.btnSetting[i].name || '重置'
          }
        }
        if(temp.btnSetting[i].buttonType == 'cancel') {
          if(temp.btnSetting[i].enable) {
            hasCancel = true
            temp.cancal = true
            temp.cancalBtnText = temp.btnSetting[i].name || '取消'
          }
        }
      }
      if(hasSub) {
        temp.submitBtn = true
      }else{
        temp.submitBtn = false
      }
      if(hasEmpt) {
        temp.emptyBtn = true
      }else{
        temp.emptyBtn = false
      }
      if(hasCancel) {
        temp.cancal = true
      }else{
        temp.cancal = false
      }
      return temp
    },
    // 下载文件
    downloadFile (filename, content, attr) {
      var elink = document.createElement('a')
      if(filename) {
        elink.download = filename
      }
      elink.style.display = 'none'

      var blob = new Blob([content], attr == 'type' ? {type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8'} : {})
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
    showhidebutton (item, rowData, type) {
      let bool = false
      // if(item.expressControl) {
      //   var str = item.expressControl
      //   str = str.replace(/\$\{/g,"row.")
      //   str = str.replace(/}/g, "")
      //   let row = rowData
      //   if(eval(str)) {
      //     bool = true
      //   }else{
      //     bool = false
      //   }
      // }
      if(item.position == 'line') {
        // console.log(rowData.jvsEnabledButtons, item.permissionFlag)
        if(rowData.jvsEnabledButtons && item.permissionFlag && rowData.jvsEnabledButtons.indexOf(item.permissionFlag) > -1) {
          bool = true
          if(type == 'out') {
            if(rowData.jvsEnabledButtons.indexOf(item.permissionFlag) > 1) {
              bool = false
            }
          }
          if(type == 'in') {
            if(rowData.jvsEnabledButtons.indexOf(item.permissionFlag) < 2) {
              bool = false
            }
          }
        }
        if(type == 'out') {
          if(item.enable === false) {
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
      // this.queryShow = false
      // this.queryOprator = {}
    },
    // 多选
    selectChange (data) {
      this.selectList = data
      this.enableDeleteList = []
      let delbtnFlag = ''
      this.lineBtns.filter(lbt => {
        if(lbt.type == 'btn_delete' && lbt.enable) {
          delbtnFlag = lbt.permissionFlag
        }
      })
      if(delbtnFlag) {
        data.filter(dit  => {
          if(dit.jvsEnabledButtons.indexOf(delbtnFlag) > -1) {
            this.enableDeleteList.push(dit.id)
          }
        })
      }
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
                getStatistics(this.jvsQueryData.jvsAppId, this.menuId, list[i].statisticsCode, to).then(res => {
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
    // 导入数据 弹窗关闭
    handleCloseImport() {
      this.importVisible = false
    },
    // 导入数据 弹窗
    handleImport(obj) {
      this.importVisible = true
      this.importBtn = JSON.parse(JSON.stringify(obj))
    },
    // 按钮点击事件
    async btnClickHandle (row, index, item, retrievalItem) {
      this.editRetrievalItem = null
      if (row && row.id) {
        this.dataId = row.id
        this.rowData = JSON.parse(JSON.stringify(row))
      } else {
        this.dataId = null
      }
      this.formDesignId = item.type == 'oa_workflow_restart' ? row.jvsFlowTask.sendFormId : item.formId
      this.itemData = item
      // 表单
      if(['btn_add', 'btn_modify', 'btn_detail', 'btn_form', 'oa_workflow_restart'].indexOf(item.type) > -1) {
        if(item.formId || (row && row.jvsFlowTask && row.jvsFlowTask.sendFormId)) {
          if(item.openType == 'tab') {
            let src = `/page-design-ui/form/use?id=${item.formId}&dataModelId=${this.jvsQueryData.dataModelId}&jvsAppId=${this.jvsQueryData.jvsAppId}&pageId=${this.jvsQueryData.id}&buttonType=${item.type}&permissionFlag=${this.itemData.permissionFlag}`
            if(row && row.id) {
              src += `&dataId=${row.id}`
            }
            let temp = encryption({
              data: {url: JSON.stringify({name: this.$route.query.name, url: (this.$route.query.src + this.$route.hash)})},
              key: enCodeKey,
              param: ["url"]
            });
            src += `&from=${temp.url}`
            this.$router.push({
              path: this.$router.$jvsRouter.getPath({
                name: (row && item.dialogTitle && (row[item.dialogTitle+'_1'] || row[item.dialogTitle])) || item.name,
                src: src
              })
            })
            return false
          }
          await getFormInfo(this.jvsQueryData.jvsAppId, item.type == 'oa_workflow_restart' ? row.jvsFlowTask.sendFormId : item.formId).then(res => {
            if(res.data.code == 0) {
              if(res.data.data.name) {
                this.formTitle = res.data.data.name
              }
              if(res.data.data.tagSetting && res.data.data.tagSetting.openTag) {
                this.tagSetting = res.data.data.tagSetting
              }
              if(res.data.data.associationSettingsFields) {
                this.associationSettingsFields = res.data.data.associationSettingsFields
              }
              this.execsList = []
              if(res.data.data.viewJson) {
                item.form = JSON.parse(res.data.data.viewJson)
                if(item.form.execs) {
                  this.execsList = item.form.execs
                }
              }else{
                this.$notify({
                  title: '提示',
                  message: '功能设计不完整，请联系管理员设计！',
                  position: 'bottom-right',
                  type: 'warning'
                });
              }
            }
          })
        }
        if(!item.form || !item.form.formdata) {
          return false
        }
        this.getSelectItem(item.form.formdata)
        if(!this.formTitle) {
          this.formTitle = item.name
        }
        this.formType = item.form.formType
        this.getFormColumn(item.form.formType, item)
        // 修改  详情
        if(item.type == 'btn_modify' || item.type == 'btn_detail' || item.type == 'oa_workflow_restart'){
          // 修改
          if(item.type == 'btn_modify') {
            // 普通表单
            if(item.form.formType == 'normalForm') {
              if(this.menuId) {
                // 无服务状态
                getSingleData(this.jvsQueryData.jvsAppId, this.jvsQueryData.dataModelId, row.id, this.formDesignId, this.jvsQueryData.id).then(res => {
                  if (res.data && res.data.code == 0) {
                    this.formData = {}
                    this.formData = JSON.parse(JSON.stringify(getDefaultData(res.data.data, item.form.formdata[0].forms, this.userInfo)))
                    if(this.formData && this.formOption.formTitleKey && this.formData[this.formOption.formTitleKey]) {
                      this.formTitle = this.formData[this.formOption.formTitleKey]
                    }
                    if(this.formData && this.formData.jvsEnabledButtons && this.formData.jvsEnabledButtons.length > 0) {
                      if(this.formOption.btnSetting) {
                        for(let i in this.formOption.btnSetting) {
                          if(this.formOption.btnSetting[i].buttonType == 'print' && this.formData.jvsEnabledButtons.indexOf(this.formOption.btnSetting[i].permissionFlag) == -1) {
                            this.hasPrint = false
                          }else if(this.formOption.btnSetting[i].buttonType == 'submit' && this.formData.jvsEnabledButtons.indexOf(this.formOption.btnSetting[i].permissionFlag) == -1) {
                            this.formOption.submitBtn = false
                          }else if(this.formOption.btnSetting[i].buttonType == 'empty' && this.formData.jvsEnabledButtons.indexOf(this.formOption.btnSetting[i].permissionFlag) == -1) {
                            this.formOption.emptyBtn = false
                          }else{
                            if(this.formData.jvsEnabledButtons.indexOf(this.formOption.btnSetting[i].permissionFlag) == -1) {
                              this.formOption.btnSetting.splice(i, 1)
                            }
                          }
                        }
                      }
                      if(this.hasPrint) {
                        this.getAvailableTemplateHandle(item)
                      }
                    }else{
                      this.formOption.btnHide = true
                      // this.getAvailableTemplateHandle(item) // 用于调试。。
                    }
                    this.formVisible = true
                  }
                })
              }else{
                this.formData = this.demoData
                // console.log(this.formData)
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
              this.getAvailableTemplateHandle(item)
              this.formOption.btnHide = true
            }
            // 回显
            // 挂载时发送请求
            if(this.menuId) {
              getSingleData(this.jvsQueryData.jvsAppId, this.jvsQueryData.dataModelId, row.id, this.formDesignId, this.jvsQueryData.id).then(res => {
                if (res.data && res.data.code == 0) {
                  this.formData = {}
                  this.formData = JSON.parse(JSON.stringify(getDefaultData(res.data.data, item.form.formdata[0].forms, this.userInfo)))
                  if(this.formData && this.formOption.formTitleKey && this.formData[this.formOption.formTitleKey]) {
                    this.formTitle = this.formData[this.formOption.formTitleKey]
                  }
                  if(item.type == 'btn_detail') {
                    if(this.formData && this.formData.jvsEnabledButtons && this.formData.jvsEnabledButtons.length > 0) {
                      if(this.formOption.btnSetting) {
                        for(let i in this.formOption.btnSetting) {
                          if(this.formOption.btnSetting[i].buttonType == 'print' && this.formData.jvsEnabledButtons.indexOf(this.formOption.btnSetting[i].permissionFlag) == -1) {
                            this.hasPrint = false
                          }else if(this.formOption.btnSetting[i].buttonType == 'submit' && this.formData.jvsEnabledButtons.indexOf(this.formOption.btnSetting[i].permissionFlag) == -1) {
                            this.formOption.submitBtn = false
                          }else if(this.formOption.btnSetting[i].buttonType == 'empty' && this.formData.jvsEnabledButtons.indexOf(this.formOption.btnSetting[i].permissionFlag) == -1) {
                            this.formOption.emptyBtn = false
                          }else{
                            if(this.formData.jvsEnabledButtons.indexOf(this.formOption.btnSetting[i].permissionFlag) == -1) {
                              this.formOption.btnSetting.splice(i, 1)
                            }
                          }
                        }
                      }
                    }else{
                      this.formOption.btnHide = true
                    }
                  }
                  this.formVisible = true
                }
              })
            }else{
              this.formData = this.demoData
              this.formVisible = true
            }
          }
        }else if(item.type == 'btn_form'){
          if(retrievalItem) {
            this.editRetrievalItem = JSON.parse(JSON.stringify(retrievalItem))
          }
          if(item.form.formdata[0].formsetting && item.form.formdata[0].formsetting.dataEchoRequest) {
            let rdata = row ? JSON.parse(JSON.stringify(row)) : {}
            if(rdata && rdata.jvsEnabledButtons) {
              delete rdata.jvsEnabledButtons
            }
            rdata.dataModelId = retrievalItem ? retrievalItem.formId : this.jvsQueryData.dataModelId
            let header = null
            if(row && row.jvsFlowTask && row.jvsFlowTask.formId) {
              header = {
                designId: row.jvsFlowTask.formId,
                pageDesignId: this.jvsQueryData.id
              }
            }
            if(!header) {
              header = {
                formDesignId: this.formDesignId
              }
            }else{
              header.formDesignId = this.formDesignId
            }
            if(item.position == 'top' && this.selectList && this.selectList.length > 0) {
              let ids = []
              this.selectList.filter(sit => {
                ids.push(sit.id)
              })
              rdata.ids = ids
            }
            if(item.position == 'top' && this.retrievalValue) {
              rdata.leftTree = {
                prop: this.retrievalItem.prop,
                value: this.retrievalValue
              }
              if(this.checkedNode) {
                rdata.leftTree.id = this.checkedNode
              }
            }
            if(item.position == 'top') {
              if(this.originRowData && this.originRowData.id) {
                rdata.id = this.originRowData.id
              }
            }
            ruleRun(this.jvsQueryData.jvsAppId, item.form.formdata[0].formsetting.dataEchoRequest, rdata, header).then(res => {
              if(res && res.data && res.headers["output_format"] && res.data.data){
                let name = decodeURI(res.headers["output_format"])
                if(res.data.data.originalName) {
                  name = res.data.data.originalName
                }
                this.ruleDownLoad(name, res.data.data)
              }else if(res && res.data && res.headers["output_type"] == 'preview' && res.data.data) {
                this.previewFile(res.data.data)
              }else{
                if(res.data && res.data.code == 0 && res.data.data) {
                  this.formData = JSON.parse(JSON.stringify(getDefaultData(res.data.data, item.form.formdata[0].forms, this.userInfo)))
                  if(this.formData && this.formOption.formTitleKey && this.formData[this.formOption.formTitleKey]) {
                    this.formTitle = this.formData[this.formOption.formTitleKey]
                  }
                  if(this.formData && this.formData.jvsEnabledButtons && this.formData.jvsEnabledButtons.length > 0) {
                    if(this.formOption.btnSetting) {
                      let delIndexs = []
                      for(let i in this.formOption.btnSetting) {
                        if(this.formOption.btnSetting[i].buttonType == 'print' && this.formData.jvsEnabledButtons.indexOf(this.formOption.btnSetting[i].permissionFlag) == -1) {
                          this.hasPrint = false
                        }else if(this.formOption.btnSetting[i].buttonType == 'submit' && this.formData.jvsEnabledButtons.indexOf(this.formOption.btnSetting[i].permissionFlag) == -1) {
                          this.formOption.submitBtn = false
                        }else if(this.formOption.btnSetting[i].buttonType == 'empty' && this.formData.jvsEnabledButtons.indexOf(this.formOption.btnSetting[i].permissionFlag) == -1) {
                          this.formOption.emptyBtn = false
                        }else{
                          if(this.formData.jvsEnabledButtons.indexOf(this.formOption.btnSetting[i].permissionFlag) == -1) {
                            delIndexs.push(i)
                          }
                        }
                      }
                      if(delIndexs && delIndexs.length > 0) {
                        this.formOption.btnSetting = this.formOption.btnSetting.filter((fit, fix) => {
                          if(delIndexs.indexOf(fix+'') == -1) {
                            return fit
                          }
                        })
                      }
                    }
                  }else{
                    if(item.position == 'line') {
                      this.formOption.btnHide = true
                    }
                  }
                }else{
                  this.formData = getDefaultData(rdata, item.form.formdata[0].forms, this.userInfo)
                }
                if(this.hasPrint) {
                  this.getAvailableTemplateHandle(item)
                }
                if(res.data.msg && res.headers["output_status"]) {
                  this.$notify({
                    title: '提示',
                    message: res.data.msg,
                    position: 'bottom-right',
                    type: (res && res.data && res.headers["output_status"] == 'false') ? 'error' : 'success',
                    duration: (res && res.data && res.headers["output_status"] == 'false' && res.headers["message_close"] == 'false') ? 0 : 4500,
                    dangerouslyUseHTMLString: true,
                  });
                }
                this.formVisible = true
              }
            }).catch(e => {
              this.formVisible = true
            })
          }else{
            if(this.menuId && row && row.id) {
              getSingleData(this.jvsQueryData.jvsAppId, retrievalItem ? retrievalItem.formId : this.jvsQueryData.dataModelId, row.id, this.formDesignId, this.jvsQueryData.id).then(res => {
                if (res.data && res.data.code == 0) {
                  this.formData = JSON.parse(JSON.stringify(getDefaultData(res.data.data, item.form.formdata[0].forms, this.userInfo)))
                  if(this.formData && this.formOption.formTitleKey && this.formData[this.formOption.formTitleKey]) {
                    this.formTitle = this.formData[this.formOption.formTitleKey]
                  }
                  if(this.formData && this.formData.jvsEnabledButtons && this.formData.jvsEnabledButtons.length > 0) {
                    if(this.formOption.btnSetting) {
                      for(let i in this.formOption.btnSetting) {
                        if(this.formOption.btnSetting[i].buttonType == 'print' && this.formData.jvsEnabledButtons.indexOf(this.formOption.btnSetting[i].permissionFlag) == -1) {
                          this.hasPrint = false
                        }else if(this.formOption.btnSetting[i].buttonType == 'submit' && this.formData.jvsEnabledButtons.indexOf(this.formOption.btnSetting[i].permissionFlag) == -1) {
                          this.formOption.submitBtn = false
                        }else if(this.formOption.btnSetting[i].buttonType == 'empty' && this.formData.jvsEnabledButtons.indexOf(this.formOption.btnSetting[i].permissionFlag) == -1) {
                          this.formOption.emptyBtn = false
                        }else{
                          if(this.formData.jvsEnabledButtons.indexOf(this.formOption.btnSetting[i].permissionFlag) == -1) {
                            this.formOption.btnSetting.splice(i, 1)
                          }
                        }
                      }
                    }
                  }else{
                    this.formOption.btnHide = true
                  }
                  if(this.hasPrint) {
                    this.getAvailableTemplateHandle(item)
                  }
                  this.formVisible = true
                }
              })
            }else{
              this.formData = {} // this.demoData
              this.formData = JSON.parse(JSON.stringify(getDefaultData(this.formData, item.form.formdata[0].forms, this.userInfo)))
              this.formVisible = true
            }
          }
        }else{
          this.formData = {}
          this.formData = JSON.parse(JSON.stringify(getDefaultData(this.formData, item.form.formdata[0].forms, this.userInfo)))
          if(item.form.formdata[0].formsetting && item.form.formdata[0].formsetting.dataEchoRequest) {
            let rdata = {}
            rdata.dataModelId = retrievalItem ? retrievalItem.formId : this.jvsQueryData.dataModelId
            if(this.selectList && this.selectList.length > 0) {
              let ids = []
              this.selectList.filter(sit => {
                ids.push(sit.id)
              })
              rdata.ids = ids
            }
            if(this.retrievalValue) {
              rdata.leftTree = {
                prop: this.retrievalItem.prop,
                value: this.retrievalValue
              }
              if(this.checkedNode) {
                rdata.leftTree.id = this.checkedNode
              }
            }
            if(this.originRowData && this.originRowData.id) {
              rdata.id = this.originRowData.id
            }
            ruleRun(this.jvsQueryData.jvsAppId, item.form.formdata[0].formsetting.dataEchoRequest, rdata, null).then(res => {
              if(res && res.data && res.headers["output_format"] && res.data.data){
                let name = decodeURI(res.headers["output_format"])
                if(res.data.data.originalName) {
                  name = res.data.data.originalName
                }
                this.ruleDownLoad(name, res.data.data)
              }else if(res && res.data && res.headers["output_type"] == 'preview' && res.data.data) {
                this.previewFile(res.data.data)
              }else{
                if(res.data && res.data.code == 0 && res.data.data) {
                  this.formData = JSON.parse(JSON.stringify(getDefaultData(res.data.data, item.form.formdata[0].forms, this.userInfo)))
                  if(this.formData && this.formOption.formTitleKey && this.formData[this.formOption.formTitleKey]) {
                    this.formTitle = this.formData[this.formOption.formTitleKey]
                  }
                }
                if(res.data.msg && res.headers["output_status"]) {
                  this.$notify({
                    title: '提示',
                    message: res.data.msg,
                    position: 'bottom-right',
                    type: (res && res.data && res.headers["output_status"] == 'false') ? 'error' : 'success',
                    duration: (res && res.data && res.headers["output_status"] == 'false' && res.headers["message_close"] == 'false') ? 0 : 4500,
                    dangerouslyUseHTMLString: true,
                  });
                }
                this.formVisible = true
              }
            }).catch(e => {
              this.formVisible = true
            })
          }else{
            this.formData = {}
            this.formData = JSON.parse(JSON.stringify(getDefaultData(this.formData, item.form.formdata[0].forms, this.userInfo)))
            // console.log(this.formData)
            this.formVisible = true
          }
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
          const designId = this.formDesignId
          this.$confirm('确认删除？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            if(item.preHttp && item.preHttp.url && item.preHttp.httpMethod && item.preHttp.requestContentType) {
              let tp = JSON.parse(JSON.stringify(item.preHttp))
              if(this.$store.getters.labelValue && this.$store.getters.labelValue.requestContentType) {
                tp.requestContentType = this.$store.getters.labelValue.requestContentType[tp.requestContentType]
              }
              let tob = JSON.parse(JSON.stringify(row))
              if(!tob.headers) {
                tp.headers = {}
              }
              this.$set(tp.headers, 'operator', encodeURI(item.name))
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
                  this.getListData()
                }
              })
            } else {
              delSingleData(this.jvsQueryData.jvsAppId, this.jvsQueryData.dataModelId, row.id, this.jvsQueryData.id).then(res => {
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
          }).catch(e => {})
        }
        // 网络请求
        if(item.type == 'btn_rule_design') {
          if(item.confirm) {
            this.$confirm(item.confirm, '提示', {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            }).then(() => {
              let rdata = row ? JSON.parse(JSON.stringify(row)) : {}
              if(rdata && rdata.jvsEnabledButtons) {
                delete rdata.jvsEnabledButtons
              }
              rdata && (rdata.dataModelId = this.jvsQueryData.dataModelId)
              let header = {
                cruddesignid: this.jvsQueryData.id
              }
              if(row &&  row.jvsFlowTask && row.jvsFlowTask.formId) {
                header.designId = row.jvsFlowTask.formId
                header.pageDesignId = this.jvsQueryData.id
              }
              if(item.position == 'top' && this.selectList && this.selectList.length > 0) {
                let ids = []
                this.selectList.filter(sit => {
                  ids.push(sit.id)
                })
                rdata.ids = ids
              }
              if(item.position == 'top' && this.retrievalValue) {
                rdata.leftTree = {
                  prop: this.retrievalItem.prop,
                  value: this.retrievalValue
                }
                if(this.checkedNode) {
                  rdata.leftTree.id = this.checkedNode
                }
              }
              if(item.customParameterIn) {
                rdata.customParameterIn = item.customParameterIn
              }
              if(item.position == 'top') {
                if(this.originRowData && this.originRowData.id) {
                  rdata.id = this.originRowData.id
                }
              }
              if(item.secret) {
                this.topBtnLoading[item.permissionFlag] = true
                ruleRun(this.jvsQueryData.jvsAppId, item.secret, rdata, header).then(res => {
                  if(res && res.data && res.headers["output_format"] && res.data.data){
                    let name = decodeURI(res.headers["output_format"])
                    if(res.data.data.originalName) {
                      name = res.data.data.originalName
                    }
                    this.ruleDownLoad(name, res.data.data)
                  }else if(res && res.data && res.headers["output_type"] == 'preview' && res.data.data) {
                    this.previewFile(res.data.data)
                  }else{
                    if(res.data.msg && res.headers["output_status"]) {
                      this.$notify({
                        title: '提示',
                        message: res.data.msg,
                        position: 'bottom-right',
                        type: (res && res.data && res.headers["output_status"] == 'false') ? 'error' : 'success',
                        duration: (res && res.data && res.headers["output_status"] == 'false' && res.headers["message_close"] == 'false') ? 0 : 4500,
                        dangerouslyUseHTMLString: true,
                      });
                    }
                    this.getListData()
                  }
                  this.topBtnLoading[item.permissionFlag] = false
                }).catc(e => {
                  this.topBtnLoading[item.permissionFlag] = false
                })
              }else{
                this.getListData()
              }
            })
          }else{
            let rdata = row ? JSON.parse(JSON.stringify(row)) : {}
            if(rdata && rdata.jvsEnabledButtons) {
              delete rdata.jvsEnabledButtons
            }
            rdata && (rdata.dataModelId = this.jvsQueryData.dataModelId)
            let header = {
              cruddesignid: this.jvsQueryData.id
            }
            if(row &&  row.jvsFlowTask && row.jvsFlowTask.formId) {
              header.designId = row.jvsFlowTask.formId
              header.pageDesignId = this.jvsQueryData.id
            }
            if(item.position == 'top' && this.selectList && this.selectList.length > 0) {
              let ids = []
              this.selectList.filter(sit => {
                ids.push(sit.id)
              })
              rdata.ids = ids
            }
            if(item.position == 'top' && this.retrievalValue) {
              rdata.leftTree = {
                prop: this.retrievalItem.prop,
                value: this.retrievalValue
              }
              if(this.checkedNode) {
                rdata.leftTree.id = this.checkedNode
              }
            }
            if(item.customParameterIn) {
              rdata.customParameterIn = item.customParameterIn
            }
            if(item.position == 'top') {
              if(this.originRowData && this.originRowData.id) {
                rdata.id = this.originRowData.id
              }
            }
            if(item.secret) {
              this.topBtnLoading[item.permissionFlag] = true
              ruleRun(this.jvsQueryData.jvsAppId, item.secret, rdata, header).then(res => {
                if(res && res.data && res.headers["output_format"] && res.data.data){
                  let name = decodeURI(res.headers["output_format"])
                  if(res.data.data.originalName) {
                    name = res.data.data.originalName
                  }
                  this.ruleDownLoad(name, res.data.data)
                  this.getListData()
                }else if(res && res.data && res.headers["output_type"] == 'preview' && res.data.data) {
                  this.previewFile(res.data.data)
                  this.getListData()
                }else{
                  if(res.data.msg && res.headers["output_status"]) {
                    this.$notify({
                      title: '提示',
                      message: res.data.msg,
                      position: 'bottom-right',
                      type: (res && res.data && res.headers["output_status"] == 'false') ? 'error' : 'success',
                      duration: (res && res.data && res.headers["output_status"] == 'false' && res.headers["message_close"] == 'false') ? 0 : 4500,
                      dangerouslyUseHTMLString: true,
                    });
                  }
                  this.getListData()
                }
                this.topBtnLoading[item.permissionFlag] = false
              }).catch(e => {
                this.topBtnLoading[item.permissionFlag] = false
              })
            }else{
              this.getListData()
            }
          }
        }
        // 网络请求
        if(item.type == 'btn_network_request') {
          if(item.confirm) {
            this.$confirm(item.confirm, '提示', {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            }).then(() => {
              let tp = JSON.parse(JSON.stringify(item.netHttp))
              if(this.$store.getters.labelValue && this.$store.getters.labelValue.requestContentType) {
                tp.requestContentType = this.$store.getters.labelValue.requestContentType[tp.requestContentType]
              }
              if(!tp.headers) {
                tp.headers = {}
              }
              this.$set(tp.headers, 'permissionFlag', item.permissionFlag)
              this.$set(tp.headers, 'operator', encodeURI(item.name))
              let pa = JSON.parse(JSON.stringify(row))
              pa = Object.assign(pa, tp.parameters)
              this.topBtnLoading[item.permissionFlag] = true
              sendMyRequire(tp, pa).then(res => {
                if(res.data.code == 0) {
                  this.getListData()
                }
                this.topBtnLoading[item.permissionFlag] = false
              }).catch(e => {
                this.topBtnLoading[item.permissionFlag] = false
              })
            })
          }else{
            let tp = JSON.parse(JSON.stringify(item.netHttp))
            if(this.$store.getters.labelValue && this.$store.getters.labelValue.requestContentType) {
              tp.requestContentType = this.$store.getters.labelValue.requestContentType[tp.requestContentType]
            }
            if(!tp.headers) {
              tp.headers = {}
            }
            this.$set(tp.headers, 'permissionFlag', item.permissionFlag)
            this.$set(tp.headers, 'operator', encodeURI(item.name))
            let pa = JSON.parse(JSON.stringify(row))
            pa = Object.assign(pa, tp.parameters)
            this.topBtnLoading[item.permissionFlag] = true
            sendMyRequire(tp, pa).then(res => {
              if(res.data.code == 0) {
                this.getListData()
              }
              this.topBtnLoading[item.permissionFlag] = false
            }).catch(e => {
              this.topBtnLoading[item.permissionFlag] = false
            })
          }
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
              let params = {
                designId: this.jvsQueryData.id,
                pageDesignId: this.jvsQueryData.id,
                keywords: this.searchKeyword
              }
              if (item.type === 'btn_export') {
                let tq = {
                  conditions: [],
                  sorts: this.sortsList
                }
                for(let i in this.queryParams) {
                  if(this.queryParams[i] || this.queryParams[i] === 0 || this.queryParams[i] === false || (typeof this.queryParams[i] == 'number')) {
                    tq.conditions.push({fieldKey: i, enabledQueryTypes: this.queryOprator[i] ? this.queryOprator[i] : 'eq', value: this.queryParams[i]})
                  }
                }
                if(this.retrievalItem && this.retrievalValue) {
                  tq.conditions.push({fieldKey: this.retrievalItem.prop, enabledQueryTypes: 'eq', value: this.retrievalValue})
                }
                let ids = []
                if(this.selectList && this.selectList.length > 0) {
                  this.selectList.filter(sit => {
                    ids.push(sit.id)
                  })
                }
                this.tableLoading = true
                // exportData(this.jvsQueryData.jvsAppId, this.jvsQueryData.dataModelId, Object.assign(params,  {query: encodeURIComponent(JSON.stringify(tq))})).then(res => {
                exportListData(this.jvsQueryData.jvsAppId, this.jvsQueryData.dataModelId, ids, Object.assign(params,  {query: encodeURIComponent(JSON.stringify(tq))})).then(res => {
                  if (res.data) {
                    this.tableLoading = false
                    let name = res.headers["content-disposition"].split(";")[1]
                    name = name.split("=")[1]
                    name = decodeURI(name)
                    this.downloadFile(name, res.data, 'type')
                  }
                }).catch(e => {
                  this.tableLoading = false
                })
              } else {
                this.tableLoading = true
                downloadTemplate(this.jvsQueryData.jvsAppId, this.jvsQueryData.dataModelId, params).then(res => {
                  if (res.data) {
                    this.tableLoading = false
                    let name = res.headers["content-disposition"].split(";")[1]
                    name = name.split("=")[1]
                    name = decodeURI(name)
                    this.downloadFile(name, res.data, 'type')
                  }
                }).catch(e => {
                  this.tableLoading = false
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
              if(item.address.startsWith('<')) {
                if(item.position == 'top') {
                  let str = item.address
                  if(str) {
                    str = str.replaceAll(/<div\b[^>]*>(.*?)<\/div>/gi, '$1')
                    str = str.replaceAll(/<p\b[^>]*>(.*?)<\/p>/gi, '$1')
                    str = str.replaceAll(/<span title="(.*?)".*[^>]*>.*<\/span>/gi, '*$1*')
                    str = str.replaceAll(/<b\b[^>]*>(.*?)<\/b>/gi, '$1')
                    str = str.replaceAll(/&nbsp;/gi, '')
                    str = str.replaceAll(/<a\b[^>]*>(.*?)<\/a>/gi, '$1')
                    str = str.replaceAll(/&amp;/gi, '&')
                    let tp = str.split('*')
                    let tstr = ''
                    for(let t in tp) {
                      if(tp[t] == 'token') {
                        tstr += this.$store.getters.access_token
                      }else if(tp[t] == 'userId') {
                        tstr += this.$store.getters.userInfo.id
                      }else if(tp[t] == 'realName') {
                        tstr += this.$store.getters.userInfo.realName
                      }else{
                        tstr += tp[t]
                      }
                    }
                    this.$openUrl(tstr, item.type == 'btn_embedded_address' ? '_self' : '_blank')
                  }
                }else{
                  getUrlWithParamter(this.jvsQueryData.jvsAppId, this.jvsQueryData.dataModelId, row.id, {address: item.address, permissionFlag: item.permissionFlag}).then(res => {
                    if(res.data && res.data.code == 0 && res.data.data) {
                      this.$openUrl(res.data.data, item.type == 'btn_embedded_address' ? '_self' : '_blank')
                    }
                  })
                }
              }else{
                if(item.address.includes('?')) {
                  if(!item.address.includes('jvsAppId')) {
                    item.address += `&jvsAppId=${this.jvsQueryData.jvsAppId}`
                  }
                }else{
                  item.address += `?jvsAppId=${this.jvsQueryData.jvsAppId}`
                }
                this.$openUrl(item.address, item.type == 'btn_embedded_address' ? '_self' : '_blank')
              }
            }
          }

          // 流程办理
          if(item.type == 'oa_workflow') {
            this.rowData = Object.assign({dataModelId: row.dataModelId ? row.dataModelId : this.jvsQueryData.dataModelId, dataId: row.dataId || row.id, jvsAppId: this.jvsQueryData.jvsAppId}, row.jvsFlowTask)
            if(row.jvsFlowTask.taskNodes) {
              if(row.jvsFlowTask.taskNodes.length > 0) {
                if(row.jvsFlowTask.taskNodes.length == 1) {
                  this.rowData = Object.assign(this.rowData, row.jvsFlowTask.taskNodes[0])
                  this.formVisible = true
                }else{
                  this.nodeSelectList = []
                  let modeUser = getStore({ name: 'modeUserInfo' })
                  row.jvsFlowTask.taskNodes.filter(nit => {
                    if(nit.userIds.indexOf((modeUser && modeUser.userId) ? modeUser.userId : this.$store.getters.userInfo.id) > -1) {
                      this.nodeSelectList.push(nit)
                    }
                  })
                  this.nodeSelectVisible = true
                }
              }
            }else{
              this.formVisible = true
            }
          }

          // 查看进度
          if(item.type == 'oa_workflow_progress') {
            this.rowData = Object.assign({dataModelId: row.dataModelId ? row.dataModelId : this.jvsQueryData.dataModelId, dataId: row.dataId || row.id, jvsAppId: this.jvsQueryData.jvsAppId}, row.jvsFlowTask)
            this.infoVisible = true
          }

          // 取消流程
          if(item.type == 'oa_workflow_cancel') {
            this.rowData = JSON.parse(JSON.stringify(row))
            this.formData = {}
            this.formTitle = item.name
            this.formVisible = true
          }

          // 打开列表页
          if(item.type == 'btn_page') {
            if(item.openType == 'tab') {
              let obj = {}
              if(localStorage.getItem('tabPage')) {
                obj = JSON.parse(localStorage.getItem('tabPage'))
              }
              obj[item.formId] = {
                routerQuery: {id: item.formId, dataModelId: item.dataModelId, jvsAppId: this.jvsQueryData.jvsAppId},
                pageQueryFixCondition: this.getPageFixQuery(),
                pageSetting: this.getPageSetting(),
                originRowData: row,
                selectedRows: this.getSelectdList(),
                openBy: this.menuId
              }
              localStorage.setItem('tabPage', JSON.stringify(obj))
              this.$router.push({
                path: this.$router.$jvsRouter.getPath({
                  name: (row && item.dialogTitle && (row[item.dialogTitle+'_1'] || row[item.dialogTitle])) || item.name,
                  src: `/page-design-ui/list/use?id=${item.formId}&dataModelId=${item.dataModelId}&jvsAppId=${this.jvsQueryData.jvsAppId}&openType=tab`
                })
              })
            }else{
              this.rowData = JSON.parse(JSON.stringify(row))
              this.formTitle = item.name
              this.pageVisible = true
            }
          }
        }
      }
    },
    // 上传
    uploadChangeHandle (res, file, fileList) {
      if(res.code == 0) {
        this.$notify({
          title: '提示',
          message: '导入成功',
          position: 'bottom-right',
          type: 'success'
        });
        this.importVisible = false
        this.getListData()
      }else{
        this.$notify({
          title: '提示',
          message: res.msg || '导入失败',
          position: 'bottom-right',
          type: 'error'
        });
        this.$refs.importUpload.clearFiles()
      }
    },
    // 上传文件前钩子
    beforeUpload(file) {
      const xlsx = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      const xls = 'application/vnd.ms-excel'
      const isPassType = (file.type !== xlsx && file.type !== xls)
      const isLt10M = file.size / 1024 / 1024 < 10;
      if (isPassType && !isLt10M) {
        // this.$message.error('上传文件的格式只能是 xls、xlsx格式，且文件大小不能超过 10MB！');
        this.$notify({
          title: '提示',
          message: '上传文件的格式只能是 xls、xlsx格式，且文件大小不能超过 10MB！',
          position: 'bottom-right',
          type: 'error'
        });
      } else if (isPassType) {
        // this.$message.error('文件格式错误，仅支持上传xls、xlsx格式文件!');
        this.$notify({
          title: '提示',
          message: '文件格式错误，仅支持上传xls、xlsx格式文件!',
          position: 'bottom-right',
          type: 'error'
        });
      } else if (!isLt10M) {
        // this.$message.error('上传的文件大小不能超过 10MB!');
        this.$notify({
          title: '提示',
          message: '上传的文件大小不能超过 10MB!',
          position: 'bottom-right',
          type: 'error'
        });
      }
      return !isPassType && isLt10M;
    },
    // 关闭表单
    handleCloseForm () {
      this.formVisible = false
    },
    // 表单提交
    formSubmit (formsdata) {
      this.formOption.submitLoading = true
      // console.log(formsdata)
      if(!this.menuId) {
        return false
      }
      let form = null // 表单数据
      let url = ''
      let method = ''
      let msg = ''
      let tp = null
      const dataModelId = this.editRetrievalItem ? this.editRetrievalItem.formId : this.jvsQueryData.dataModelId
      const designId = this.formDesignId
      // 普通表单
      if(this.itemData.form.formType == 'normalForm') {
        form = formsdata
        const http = {
          httpMethod: "POST",
          requestContentType: "application/json",
          responseContentType: "JSON",
          url: (this.dataId && this.itemData.type != 'oa_workflow_restart') ? `/mgr/jvs-design/app/use/${this.jvsQueryData.jvsAppId}/dynamic/data/update/${dataModelId}/${this.dataId}` : `/mgr/jvs-design/app/use/${this.jvsQueryData.jvsAppId}/dynamic/data/save/${dataModelId}`,
          headers: {
            'designId': designId,
            'operator': encodeURI('提交'),
            'permissionFlag': this.itemData.permissionFlag
          }
        }
        if(this.itemData.form.formdata && this.itemData.form.formdata[0] && this.itemData.form.formdata[0].formsetting) {
          tp = JSON.parse(JSON.stringify(http))
          if(this.$store.getters.labelValue && this.$store.getters.labelValue.requestContentType) {
            tp.requestContentType = this.$store.getters.labelValue.requestContentType[tp.requestContentType]
          }
        }
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
        if(this.itemData.type == 'oa_workflow_restart') {
          let obj = {
            data: tempObj,
            id: tempObj.jvsFlowTask.id,
          }
          restartProcess(obj, {designId: this.formDesignId}).then(res => {
            if(res.data && res.data.code == 0) {
              if(msg) {
                this.$notify({
                  title: '提示',
                  message: msg,
                  position: 'bottom-right',
                  type: 'success'
                });
              }else{
                if(res.data.msg) {
                  this.$notify({
                    title: '提示',
                    message: res.data.msg,
                    position: 'bottom-right',
                    type: 'success'
                  });
                }
              }
              this.formOption.submitLoading = false
              this.getListData()
              this.handleCloseForm()
            }else {
              this.formOption.submitLoading = false
            }
          }).catch(err => {
            this.formOption.submitLoading = false
          })
        }else{
          let needAssocia = false
          if(this.itemData.form.formdata && this.itemData.form.formdata[0] && this.itemData.form.formdata[0].formsetting && this.itemData.form.formdata[0].formsetting.btnSetting) {
            let bts = this.itemData.form.formdata[0].formsetting.btnSetting
            if(bts[0] && bts[0].enable && bts[0].association && bts[0].association.length > 0) {
              needAssocia = true
            }
          }
          if(needAssocia){
            let bts = this.itemData.form.formdata[0].formsetting.btnSetting
            association(this.jvsAppId, this.jvsQueryData.dataModelId, this.itemData.formId, bts[0].permissionFlag, Object.assign(tempObj, tp.parameters), encodeURI(bts[0].name)).then(res => {
              if(res.data.code == 0) {
                if(msg) {
                  this.$notify({
                    title: '提示',
                    message: msg,
                    position: 'bottom-right',
                    type: 'success'
                  });
                }else{
                  if(res.data.msg) {
                    this.$notify({
                      title: '提示',
                      message: res.data.msg,
                      position: 'bottom-right',
                      type: 'success'
                    });
                  }
                }
                this.formOption.submitLoading = false
                this.getListData()
                this.handleCloseForm()
              } else {
                this.formOption.submitLoading = false
              }
            }).catch(err => {
              this.formOption.submitLoading = false
            })
          }else{
            sendMyRequire(tp, Object.assign(tempObj, tp.parameters)).then(res => {
              if(res.data.code == 0) {
                if(msg) {
                  this.$notify({
                    title: '提示',
                    message: msg,
                    position: 'bottom-right',
                    type: 'success'
                  });
                }else{
                  if(res.data.msg) {
                    this.$notify({
                      title: '提示',
                      message: res.data.msg,
                      position: 'bottom-right',
                      type: 'success'
                    });
                  }
                }
                this.formOption.submitLoading = false
                this.getListData()
                this.handleCloseForm()
              } else {
                this.formOption.submitLoading = false
              }
            }).catch(err => {
              this.formOption.submitLoading = false
            })
          }
        }
      }
    },
    // 自定义按钮事件
    slotbtnClickHandle (row, index) {
      if(!row.loading) {
        this.$set(row, 'loading', false)
      }
      let validate = true
      if(row.validateable) {
        validate = this.$refs.ruleForm.validateForm()
      }
      if(!validate) {
        return false
      }
      let tp = null
      tp = JSON.parse(JSON.stringify(row))
      if(this.$store.getters.labelValue && this.$store.getters.labelValue.requestContentType) {
        tp.requestContentType = this.$store.getters.labelValue.requestContentType[tp.requestContentType]
      }
      let tob = JSON.parse(JSON.stringify(this.formData))
      for(let k in tob) {
        if(tob[k] && tob[k] instanceof Array) {
          for(let n in tob[k]) {
            if(tob[k][n] === null || tob[k][n] === undefined) {
              tob[k].splice(n, 1)
            }
          }
        }
      }
      if(tp.association && tp.association.length > 0){
        row.loading = true
        association(this.jvsAppId, this.jvsQueryData.dataModelId, this.itemData.formId, this.itemData.permissionFlag, Object.assign(tob, tp.parameters), encodeURI(this.itemData.name)).then(res => {
          if(res.data.code == 0) {
            row.loading = false
            if(res.data.msg) {
              this.$notify({
                title: '提示',
                message: res.data.msg,
                position: 'bottom-right',
                type: 'success'
              });
            }
            this.getListData()
            if(row.closeable !== false) {
              this.handleCloseForm()
            }
          }
        }).catch(e => {
          row.loading = false
        })
      }else{
        if(tp) {
          if(tp.url) {
            row.loading = true
            sendMyRequire(tp, Object.assign(tob, tp.parameters)).then(res => {
              if(res.data.code == 0) {
                row.loading = false
                if(res.data.msg) {
                  this.$notify({
                    title: '提示',
                    message: res.data.msg,
                    position: 'bottom-right',
                    type: 'success'
                  });
                }
                this.getListData()
                if(row.closeable !== false) {
                  this.handleCloseForm()
                }
              }
            }).catch(e => {
              row.loading = false
            })
          }
          if(tp.secret) {
            let rdata = JSON.parse(JSON.stringify(this.formData))
            rdata.dataModelId = this.jvsQueryData.dataModelId
            row.loading = true
            ruleRun(this.jvsQueryData.jvsAppId, tp.secret, rdata, {designId: this.itemData.formId, pageDesignId: this.jvsQueryData.id}).then(res => {
              row.loading = false
              if(res && res.data && res.headers["output_format"] && res.data.data){
                let name = decodeURI(res.headers["output_format"])
                if(res.data.data.originalName) {
                  name = res.data.data.originalName
                }
                this.ruleDownLoad(name, res.data.data)
                this.getListData()
              }else if(res && res.data && res.headers["output_type"] == 'preview' && res.data.data) {
                this.previewFile(res.data.data)
                this.getListData()
              }else{
                if(res.data && res.data.code == 0) {
                  if(res.data.msg && res.headers["output_status"]) {
                    this.$notify({
                      title: '提示',
                      message: res.data.msg,
                      position: 'bottom-right',
                      type: (res && res.data && res.headers["output_status"] == 'false') ? 'error' : 'success',
                      duration: (res && res.data && res.headers["output_status"] == 'false' && res.headers["message_close"] == 'false') ? 0 : 4500,
                      dangerouslyUseHTMLString: true,
                    });
                  }else{
                    this.$notify({
                      title: '提示',
                      message: tp.name + '成功',
                      position: 'bottom-right',
                      type: 'success'
                    })
                  }
                  if(res.data.data) {
                    for(let k in res.data.data) {
                      this.$set(this.formData, k, res.data.data[k])
                    }
                  }
                  if(res && res.data && res.headers["output_status"] == 'false') {
                    return false
                  }else{
                    this.getListData()
                  }
                  if(row.closeable !== false) {
                    this.handleCloseForm()
                  }
                }
              }
            }).catch(e => {
              row.loading = false
            })
          }else{
            if(row.closeable !== false) {
              this.handleCloseForm()
            }
          }
        }
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
              if(!formdata[i].forms[j].props && ['user', 'role', 'department', 'group', 'job'].indexOf(formdata[i].forms[j].type) == -1) {
                formdata[i].forms[j].props = {
                  label: 'label',
                  value: 'value'
                }
              }
            }
            if(formdata[i].forms[j].type == 'select') {
              if(item) {
                // 单选 多选
                if(item.correspondence == "ONE_TO_N") {
                  formdata[i].forms[j].multiple = true
                }else{
                  formdata[i].forms[j].multiple = false
                }
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
    // 是否显示更多
    showMore (rowData, list) {
      let bool = true
      if(rowData.jvsEnabledButtons && rowData.jvsEnabledButtons.length > 3) {
        bool = false
        for(let i in list) {
          if(i > 1) {
            if(rowData.jvsEnabledButtons && list[i].permissionFlag && rowData.jvsEnabledButtons.indexOf(list[i].permissionFlag) > -1) {
              bool = true
            }
          }
        }
      }else{
        bool = false
      }
      return bool
    },
    // 表单获取可打印的模板列表
    getAvailableTemplateHandle (item) {
      getAvailableTemplate(this.jvsQueryData.jvsAppId, item.formId).then(res => {
        if(res && res.data && res.data.code == 0) {
          this.printTemplateList = res.data.data
        }
      })
    },
    // 打印
    printHandle (row) {
      if(this.dataId) {
        if(row.designType == 1) {
          let url = `/mgr/jvs-design/app/use/${this.jvsQueryData.jvsAppId}/print/template/file/preview/${row.id}/${this.jvsQueryData.dataModelId}/${row.designId}/${this.dataId}?access_token=${this.$store.getters.access_token}`
          if(this.rowData.jvsFlowTask && this.rowData.jvsFlowTask.id) {
            url += `&taskId=${this.rowData.jvsFlowTask.id}`
          }
          this.$openUrl(url, '_blank')
        }else{
          let url = `/jvs-print-ui/#/print/show?id=${row.id}&name=${row.name}&designId=${row.designId}&dataModelId=${this.jvsQueryData.dataModelId}&dataId=${this.dataId}&jvsAppId=${this.jvsQueryData.jvsAppId}`
          if(this.rowData.jvsFlowTask && this.rowData.jvsFlowTask.id) {
            url += `&taskId=${this.rowData.jvsFlowTask.id}`
          }
          this.$openUrl(url, '_blank')
        }
      }
    },
    // 组件详情
    getSlotItemCom (item) {
      let com = []
      let temp = {
        btnHide: true,
        disabled: true,
        labelposition: 'top',
        formAlign: 'top',
        column: []
      }
      this.column.filter(it => {
        if(it.aliasColumnName == item.prop && it.designJson) {
          let icom = JSON.parse(JSON.stringify(it.designJson))
          icom.span = 24
            icom.label = ''
            icom.disabled = true
          if(item.type == 'connectForm') {
            if(item.childrenOptionColumn) {
              icom.childrenOptionColumn = item.childrenOptionColumn
              icom.url = ''
            }
            for(let i in icom.connectFormColumn) {
              icom.connectFormColumn[i].span = 24
              icom.connectFormColumn[i].disabled = true
            }
          }
          com = [ icom ]
        }
      })
      if(com && com.length > 0) {
        temp.column = com
      }
      return temp
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
    getFormDesignByFormId (formItem, obj) {
      getDetail(formItem.formId).then(res => {
        if (res.data&&res.data.code==0) {
          if (res.data.data.viewJson) {
            let viewJson=JSON.parse(res.data.data.viewJson)
            if (viewJson.formdata&&viewJson.formdata.length>0) {
              let formJson=viewJson.formdata[0]
              // 子表单
              if(formItem.type == 'childrenForm') {
                obj.childrenOptionColumn = formJson.forms
                obj.childrenOptionColumn.filter(item => {
                  item.disabled = formItem.disabled
                })
              }
              // 关联表单
              if(formItem.type == 'connectForm') {
                if(formItem.connectFormColumn) {
                  let tarr = []
                  formItem.connectFormColumn.filter(item => {
                    let needAdd = true
                    for(let i in formJson.forms) {
                      if(formJson.forms[i].prop == item.prop) {
                        tarr.push({...formJson.forms[i], span: 24, url: obj.datatype == 'option' ? '' : formJson.forms[i].url})
                        needAdd = false
                      }
                    }
                    if(needAdd) {
                      if(item.type == 'datePicker' && !item.datetype) {
                        item.datetype = 'date'
                      }
                      tarr.push(item)
                    }
                  })
                  obj.childrenOptionColumn = [{
                    label: formItem.props.text,
                    prop: formItem.prop,
                    type: formItem.type,
                    formSlot: true,
                    url: '',
                    props: formItem.props
                  }, ...tarr]
                  this.$forceUpdate()
                }
              }
            }
          }
        }
      })
    },
    // 工作流表单
    closeHandle (bool) {
      if (bool) {
        this.handleCloseForm()
        this.getListData()
      }
    },
    // 排序查询
    addSortHandle () {
      this.sortsList.push({})
    },
    sortQuerySubmit () {
      this.queryHandle(this.queryParams)
      this.sortShow = false
    },
    clearSort () {
      this.sortsList = []
      this.queryHandle(this.queryParams)
      this.sortShow = false
    },
    // 表单标签
    getTagDetail () {
      getFormTagData(this.jvsQueryData.jvsAppId, this.formDesignId, 'formQrCodeTag', {params: this.formData}).then(res => {
        if(res.data && res.data.code == 0) {
          // console.log('公式。。。。', res.data.data)
          if(res.data.data) {
            this.$set(this, 'tagSetting', res.data.data)
            this.tagDataTransform = true
          }else{
            this.tagDataTransform = false
          }
          this.openCloseHandle()
          this.tagshow = true
        }else{
          this.tagshow = false
          this.tagDataTransform = false
        }
      }).catch(e => {
        this.tagshow = false
        this.tagDataTransform = false
      })
    },
    // 标签二维码
    openCloseHandle () {
      if(this.$refs.tagQRcode) {
        this.$refs.tagQRcode.innerHTML = ''; //清除二维码方法一
        let loc = location.origin // 'http://10.0.0.174:8099'
        let text = `${loc}/jvsCom/form/use?id=${this.formDesignId}&dataModelId=${this.jvsQueryData.dataModelId}&dataId=${this.rowData.id}&jvsAppId=${this.jvsQueryData.jvsAppId}`
        var qrcode = new QRCode(this.$refs.tagQRcode, {
          text: text, //页面地址 ,如果页面需要参数传递请注意哈希模式#
          width: 130,
          height: 130,
          colorDark: '#000000',
          colorLight: '#ffffff',
          correctLevel: QRCode.CorrectLevel.Q,
        })
      }
    },
    // 下载标签
    downloadTag () {
      html2canvas(document.querySelector('#formTag'), {
        useCORS: true, // 【重要】开启跨域配置
        scale: window.devicePixelRatio < 3 ? window.devicePixelRatio : 2,
        allowTaint: true, // 允许跨域图片
      }).then((canvas) => {
        const imgData = canvas.toDataURL('image/png', 1.0);
        let aLink = document.createElement('a')
        let blob = this.base64ToBlob(imgData)
        let evt = document.createEvent('HTMLEvents')
        evt.initEvent('click', true, true)// initEvent 不加后两个参数在FF下会报错  事件类型，是否冒泡，是否阻止浏览器的默认行为
        aLink.download = this.tagSetting.title.text || '下载标签'
        aLink.href = URL.createObjectURL(blob)
        // aLink.dispatchEvent(evt);
        aLink.click()
        document.removeChild(aLink)
        resolve(imgData);
      });
    },
    base64ToBlob(code) {
      let parts = code.split(';base64,')
      let contentType = parts[0].split(':')[1]
      let raw = window.atob(parts[1]) // 解码base64得到二进制字符串
      let rawLength = raw.length
      let uInt8Array = new Uint8Array(rawLength) // 创建8位无符号整数值的类型化数组
      for (let i = 0; i < rawLength; ++i) {
        uInt8Array[i] = raw.charCodeAt(i) // 数组接收二进制字符串
      }
      return new Blob([uInt8Array], {type: contentType})
    },
    // 变更记录
    getFormEditLogsHandle () {
      this.logList = []
      getFormEditLogs(this.jvsQueryData.jvsAppId, this.jvsQueryData.dataModelId, this.dataId).then(res => {
        if(res.data && res.data.code == 0 && res.data.data) {
          this.logList = res.data.data
          this.logshow = true
        }
      })
    },
    nodeSelectClose () {
      this.nodeSelectVisible = false
      this.nodeSelectList = []
    },
    nodeSelectHandle (item) {
      this.rowData = Object.assign(this.rowData, item)
      this.formVisible = true
      this.nodeSelectClose()
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
    // 排序字段
    getSortColumn (list) {
      let temp = []
      // list.filter(li => {
      //   temp.push(li)
      //   // if(li.tools && li.tools.indexOf('sort') > -1){
      //   //   temp.push(li)
      //   // }
      // })
      this.fieldsData.filter(li => {
        temp.push({
          label: li.label || li.fieldName,
          prop: li.prop || li.fieldKey
        })
      })
      return temp
    },
    // 字段排序
    sortTableHandle (list) {
      this.sortsList = list
      this.getListData()
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
      this.getListData('retrieval')
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
          }else if(item.datatype == 'rule') {
            if(item.optionHttp) {
              ruleRun(this.jvsQueryData.jvsAppId, item.optionHttp, {}).then(res => {
                if(res.data && res.data.code == 0 && res.data.data) {
                  this.retrievalOption = res.data.data
                  this.retrievalProps = {
                    label: item.props.label || 'name',
                    value: item.props.value || 'value',
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
              if(this.treeKeyword) {
                postData.conditions.push({fieldKey: this.retrievalItem.props.label, enabledQueryTypes: 'like', value: this.treeKeyword})
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
        if(item.datatype == 'rule') {
          if(item.optionHttp) {
            ruleRun(this.jvsQueryData.jvsAppId, item.optionHttp, {}).then(res => {
              if(res.data && res.data.code == 0 && res.data.data) {
                this.retrievalOption = res.data.data
                if(item.type == 'cascader') {
                  this.retrievalProps = {
                    label: item.props.label || 'name',
                    value: item.props.value || 'value',
                    children: 'children',
                    id: 'id'
                  }
                  this.retrievalOption = res.data.data || []
                  this.retrievalOption = this.getTree(this.retrievalOption , 1, item)
                  this.$nextTick(()=> {
                    if(this.checkedNode) {
                      this.$refs.jvsTableLeftTree.setCurrentKey(this.checkedNode)
                      this.$forceUpdate()
                    }
                  })
                }
                this.leftTreeLoading = false
              }
            })
          }
        }
        // 系统级联选择
        if(item.datatype == 'system' && item.dictName) {
          if(item.type == 'cascader') {
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
    // 树形结点选中
    handleNodeClick (data, node, dom) {
      if(this.retrievalValue != data[this.retrievalProps.value]) {
        this.retrievalSearch(data)
      }
      this.checkedNode = data.id
    },
    // 递归树清掉空的children
    getTree (tree = [], level, com) {
      let arr = [];
      if (tree.length !== 0) {
        tree.forEach(item => {
          let obj = { level: level }
          for(let k in item) {
            if(k != this.retrievalProps.children) {
              obj[k] = item[k]
            }
          }
          if(com && com.datatype == 'system') {
            obj.label = item.name
            obj.value = item.extend ? item.extend.uniqueName : item.id
          }
          if(item.children && item.children.length > 0) {
            obj.children = this.getTree(item.children, level+1, com);
          }
          arr.push(obj);
        });
      }
      return arr
    },
    // 左树更多
    moreLeftTree (item) {
      if(this.lastLeftNode) {
        this.lastLeftNode.moretool = false
      }
      this.lastLeftNode = item
    },
    // 左树按钮操作
    leftTreeClick (node, data, index, button) {
      this.btnClickHandle(data, index, button, this.retrievalItem)
      data.moretool = false
    },
    // 关闭进度
    infoClose () {
      this.infoVisible = false
    },
    // 批量删除
    mulDeleteData () {
      // console.log(this.enableDeleteList)
      this.$confirm('确认删除？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.tableLoading = true
        delMultipleData(this.jvsQueryData.jvsAppId, this.jvsQueryData.dataModelId, this.enableDeleteList, this.jvsQueryData.id).then(res => {
          if(res.data && res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: '删除成功',
              position: 'bottom-right',
              type: 'success'
            })
            this.getListData()
          }else{
            this.tableLoading = false
          }
        }).catch(e => {
          this.tableLoading = false
        })
      })

    },
    // 取消流程
    cacelDelSubmit () {
      if(this.rowData.jvsFlowTask && this.rowData.jvsFlowTask.id) {
        this.reasonLoading = true
        candelProcess(this.rowData.jvsFlowTask.id, this.formData).then(res => {
          if(res.data.code == 0) {
            this.reasonLoading = false
            this.$notify({
              title: '提示',
              message: "撤回成功",
              position: 'bottom-right',
              type: 'success'
            });
            this.getListData()
            this.reasonClose()
          }
        })
      }
    },
    reasonClose () {
      this.formVisible = false
      this.formData = {}
    },
    pageClose () {
      if(this.openInfo && this.openInfo.openBy) {
        window.postMessage({command: 'closeTab', id: this.jvsQueryData.id}, '*')
        let tabPage = JSON.parse(localStorage.getItem('tabPage'))
        if(tabPage[this.jvsQueryData.id]) {
          delete tabPage[this.jvsQueryData.id]
        }
        localStorage.setItem('tabPage', JSON.stringify(tabPage))
      }else{
        this.pageVisible = false
      }
    },
    getPageFixQuery () {
      let temp = []
      if(this.itemData.position == 'line' && this.itemData.pageQuery && this.itemData.pageQuery.length > 0) {
        this.itemData.pageQuery.filter(it => {
          temp.push({
            fieldKey: it.fieldKey,
            enabledQueryTypes: it.enabledQueryTypes,
            value: this.rowData[it.value]
          })
        })
      }
      return temp
    },
    ruleDownLoad (name, data) {
      const dotIndex = name.lastIndexOf('.');
      let downLoadName = name
      if (dotIndex === -1 || dotIndex === name.length - 1) {
          // 没有找到点，没有后缀
          downLoadName = name+data.fileType
      }
      ruleDownLoad(this.jvsQueryData.jvsAppId, {
        ...data,
        name:downLoadName
      }).then(res => {
        if(res && res.data && res.headers["output_format"]){
          this.downloadFile(downLoadName, res.data)
        }else if(res && res.data && res.headers["output_type"] == 'preview' && res.data.data) {
          this.previewFile(res.data.data)
        }
      })
    },
    // 获取设计数据模型字段
    getDesignDataStr() {
      getDataStr(this.jvsQueryData.jvsAppId, this.jvsQueryData.dataModelId).then(res => {
        if(res.data && res.data.code == 0 && res.data.data) {
          const arr = [...res.data.data]
          this.fieldsData = arr.filter(item => {
            return item.fieldName !== ''
          })
        }
      })
    },
    pageDialogOpend () {
      this.opendPageDialogHeight = ($('.page-dialog-box').height() + 68 - 30)
    },
    getPageSetting () {
      let temp = {}
      if(this.itemData.pageSearch && this.itemData.pageSearch.length > 0) {
        temp.pageSearch = this.itemData.pageSearch
      }
      if(this.itemData.pageBottomBtns && this.itemData.pageBottomBtns.length > 0) {
        temp.pageBottomBtns = this.itemData.pageBottomBtns
      }
      if(this.itemData.dialogTitle) {
        temp.dialogTitle = this.itemData.dialogTitle
      }
      if(this.itemData.permissionFlag) {
        temp.permissionFlag = this.itemData.permissionFlag
      }
      return temp
    },
    pageBottomButtonClick (btn) {
      if(btn.rule) {
        let rdata = {
          ruleKey: btn.rule,
          relationTag: this.itemData.permissionFlag,
          data: {}
        }
        if(this.openInfo && this.openInfo.originRowData) {
          rdata.data.id = this.openInfo.originRowData.id
        }else{
          if(this.rowData && this.rowData.id) {
            rdata.data.id = this.rowData.id
          }
        }
        let header = { designId: this.jvsQueryData.id }
        // 其他通过tab打开的列表
        if(this.openInfo &&  this.openInfo.pageSetting) {
          let ids = []
          this.selectList.filter(sit => {
            ids.push(sit.id)
          })
          rdata.data[this.openInfo.pageSetting.permissionFlag] = ids
        }else{
          if(this.$refs.openPage.selectList && this.$refs.openPage.selectList.length > 0) {
            let ids = []
            this.$refs.openPage.selectList.filter(sit => {
              ids.push(sit.id)
            })
            rdata.data[this.itemData.permissionFlag] = ids
          }
        }
        updatePageRelation(this.jvsQueryData.jvsAppId, this.jvsQueryData.dataModelId, (this.openInfo && this.openInfo.originRowData) ? this.openInfo.originRowData.id : this.rowData.id, header, rdata).then(res => {
          if(res && res.data && res.headers["output_format"] && res.data.data){
            let name = decodeURI(res.headers["output_format"])
            if(res.data.data.originalName) {
              name = res.data.data.originalName
            }
            this.ruleDownLoad(name, res.data.data)
            if(this.openInfo) {
              this.getListData()
            }else{
              if(this.$refs.openPage) {
                this.$refs.openPage.getListData()
              }
            }
            if(btn.closable) {
              this.pageClose()
            }
          }else if(res && res.data && res.headers["output_type"] == 'preview' && res.data.data) {
            this.previewFile(res.data.data)
            if(this.openInfo) {
              this.getListData()
            }else{
              if(this.$refs.openPage) {
                this.$refs.openPage.getListData()
              }
            }
            if(btn.closable) {
              this.pageClose()
            }
          }else{
            if(res.data && res.data.code == 0) {
              if(this.openInfo && this.openInfo.openBy) {
                this.getListData()
                window.postMessage({command: 'freshPage', freshId: this.openInfo.openBy}, '*')
              }else{
                if(this.$refs.openPage) {
                  this.$refs.openPage.getListData()
                }
                this.getListData()
              }
              this.getListData()
              if(btn.closable) {
                this.pageClose()
              }
              if(res.data.msg && res.headers["output_status"]) {
                this.$notify({
                  title: '提示',
                  message: res.data.msg,
                  position: 'bottom-right',
                  type: (res && res.data && res.headers["output_status"] == 'false') ? 'error' : 'success',
                  duration: (res && res.data && res.headers["output_status"] == 'false' && res.headers["message_close"] == 'false') ? 0 : 4500,
                  dangerouslyUseHTMLString: true,
                });
              }
            }
          }
        }).catch(e => {
          if(this.openInfo) {
            this.getListData()
          }else{
            if(this.$refs.openPage) {
              this.$refs.openPage.getListData()
            }
          }
        })
      }else{
        if(btn.closable) {
          this.pageClose()
        }
      }
    },
    getSelectdList () {
      let list = []
      if(this.rowData && this.rowData.relationTag && this.itemData.permissionFlag && this.rowData.relationTag[this.itemData.permissionFlag] && this.rowData.relationTag[this.itemData.permissionFlag].length > 0) {
        this.rowData.relationTag[this.itemData.permissionFlag].filter(rit => {
          list.push({id: rit})
        })
      }
      return list
    },
    treeKeywordChange () {
      this.getSelectUrlData(this.retrievalItem, 'retrival')
    },
    uploadError (err) {
      this.$refs.importUpload.clearFiles()
      this.$notify({
        title: '提示',
        message: err,
        position: 'bottom-right',
        type: 'error'
      });
    },
    lineFormOption (option) {
      let temp = JSON.parse(JSON.stringify(option))
      temp.isSearch = true
      temp.column.filter(col => {
        col.searchSpan = col.querySpan ? col.querySpan : ((window.devicePixelRatio > 1.5 && document.body.clientWidth < 1500) ? 8 : 6)
      })
      return temp
    },
    changeQueryShow () {
      if(this.searchFormType == 'right') {
        this.searchFormType = 'top'
      }else{
        this.searchFormType = 'right'
      }
      localStorage.setItem('pageListQueryType', this.searchFormType)
      this.getListData()
    },
    previewFile (row) {
      let protocolhost = this.$store.getters.kkfileUrl || ''
      if(protocolhost && row.url) {
        let view_url = `${protocolhost}/onlinePreview?forceUpdatedCache=true&officePreviewType=pdf&url=` + encodeURIComponent(Base64.encode(decodeURIComponent(row.url)))
        this.$openUrl(view_url, '_blank')
      }
    },
    async getLinkModelData (item, row) {
      this.linkModelPage.currentPage = 1
      this.linkModelData = []
      await this.queryLinkModelData(item, row)
    },
    async queryLinkModelData (item, row) {
      let temp = []
      let addLineData = false
      if(item.modelDisplay && item.modelDisplay.dataLinkageList && item.modelDisplay.dataLinkageList.length > 0) {
        item.modelDisplay.dataLinkageList.filter(dit => {
          let tp = {
            fieldKey: dit.fieldKey,
            enabledQueryTypes: dit.enabledQueryTypes,
            prop: dit.prop
          }
          if(dit.prop == 'formula' && dit.formula) {
            tp.formula = dit.formula
            tp.formulaContent = dit.formulaContent
            addLineData = true
          }else{
            tp.value = row[dit.value]
          }
          temp.push(tp)
        })
      }
      let query = {
        current: this.linkModelPage.currentPage,
        size: this.linkModelPage.pageSize,
      }
      if(temp.length > 0) {
        query.dataLinkageList = temp
      }
      if(addLineData) {
        query.lineData = row
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
        await pageGetLinkModelData(this.jvsQueryData.jvsAppId, query).then(res => {
          if(res.data && res.data.code == 0 && res.data.data) {
            this.linkModelData = res.data.data.records || []
            this.linkModelPage.currentPage = res.data.data.current
            this.linkModelPage.total = res.data.data.total
            this.$forceUpdate()
          }
          this.linkModelLoading = false
        }).catch(e => {
          this.linkModelLoading = false
        })
      }
    },
    async modelDisplayHandle (e, item, row) {
      this.modelDisplayPos = {
        x: e.clientX,
        y: e.clientY
      }
      await this.getLinkModelData(item, row)
      let pageW = Number(document.body.clientWidth)
      let minW = 320 // 一个字段
      let tw = 0
      if(item.modelDisplay && item.modelDisplay.linkageFieldKeys && item.modelDisplay.linkageFieldKeys.length > 0) {
        item.modelDisplay.linkageFieldKeys.filter(dct => {
          if(dct.advancedSettings && dct.advancedSettings.showWidth > 0) {
            tw += Number(dct.advancedSettings.showWidth)
          }else{
            tw += 160
          }
        })
      }
      if(tw < minW) {
        tw = minW
      }
      if(tw > (pageW / 2)) {
        tw = pageW / 2
      }
      let w = (Number(e.clientX) + (tw / 2)) - pageW
      // console.log(w)
      if(w > 0) {
        this.$set(this.modelDisplayPos, 'x', (Number(e.clientX) - (w * 2)))
        this.$set(this.modelDisplayPos, 'transX', w * 2)
      }else{
        if(this.$store.state.common.isCollapse ? true : !(w < 0)) {
          this.$set(this.modelDisplayPos, 'x', 0)
          this.$set(this.modelDisplayPos, 'transX', `-50% + ${e.clientX}`)
        }
      }
      let pageH = Number(document.body.clientHeight)
      let minH = 315 // 无数据
      let th = 0
      if(this.linkModelData && this.linkModelData.length > 0) {
        th = 40 + (this.linkModelData.length * 56) + 52 + 10 + 8
      }else{
        th = minH
      }
      if(th > 500) {
        th = 500
      }
      let h = (th + Number(e.clientY)) - pageH
      if(h > 0) {
        this.$set(this.modelDisplayPos, 'y', (Number(e.clientY) - th - 12 - 8))
        this.$set(this.modelDisplayPos, 'placement', 'top')
      }
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
    previewImage (list) {
      this.previewSrcList = []
      list.filter(li => {
        this.previewSrcList.push(li.url)
      })
      this.showViewer = true
    },
    closeViewer () {
      this.showViewer = false
      this.previewSrcList = []
    },
    stopHandle () {
      return false
    },
    queryOpratorChange (val, item) {
      if(val == 'isNull') {
        if(item.multiple) {
          this.$set(this.queryParams, item.prop, [])
        }else{
          this.$set(this.queryParams, item.prop, '')
        }
      }
    },
    openFormHandle (item, row) {
      this.btnClickHandle(row, -1, {type: 'btn_form', formId: item.openFormId})
    },
    // 动态控制行数据显示
    styleRowItem (row, item, type) {
      let val = row[item.prop+'_1'] ? row[item.prop+'_1'] : row[item.prop]
      let color = ""
      let bgcolor = ''
      if(item.advancedSettings && item.conditionControlEnable && item.advancedSettings.conditionControl && item.advancedSettings.conditionControl.length > 0) {
        let hadFix = false
        for(let i in item.advancedSettings.conditionControl) {
          if(item.advancedSettings.conditionControl[i].value) {
            let arr = item.advancedSettings.conditionControl[i].value.split(',')
            if(arr.indexOf(row[item.advancedSettings.conditionControl[i].key || item.prop]) > -1 || arr.indexOf(row[item.advancedSettings.conditionControl[i].key || item.prop]+'') > -1) {
              hadFix = true
              if(item.advancedSettings.conditionControl[i].text) {
                val = item.advancedSettings.conditionControl[i].text
              }else{
                val = row[item.prop+'_1'] ? row[item.prop+'_1'] : (row[item.prop] instanceof Array ? (row[item.prop].length > 0 ? row[item.prop].join(',') : '') : row[item.prop])
              }
              if(item.advancedSettings.conditionControl[i].color) {
                color = item.advancedSettings.conditionControl[i].color
              }
              if(item.advancedSettings.conditionControl[i].bgcolor) {
                bgcolor = item.advancedSettings.conditionControl[i].bgcolor
              }
              break;
            }
          }
        }
        if(!hadFix) {
          if(item.advancedSettings && item.advancedSettings.textcolor) {
            color = item.advancedSettings.textcolor
          }
          if(item.advancedSettings && item.advancedSettings.backColor) {
            bgcolor = item.advancedSettings.backColor
          }
        }
      }else{
        if(item.advancedSettings && item.advancedSettings.textcolor) {
          color = item.advancedSettings.textcolor
        }
        if(item.advancedSettings && item.advancedSettings.backColor) {
          bgcolor = item.advancedSettings.backColor
        }
      }
      if(type == 'color') {
        return color ? (color.startsWith('#') ? color : `;-webkit-text-fill-color: transparent;background:${color};background-clip: text!important;-webkit-background-clip: text!important;`) : ``
      }else if(type == 'bgcolor') {
        return bgcolor
      }else{
        return this.fixedNumber(val, item)
      }
    },
    fixedNumber (val, item) {
      let num = isNaN(Number(val)) ? val : (item.precision ? Number(val).toFixed(item.precision || 0) : val)
      return item.thoudsandthable ? this.getThousandthNumber(num, item) : num
    },
    // 复制
    copyHandle (value) {
      const text = document.createElement('input')
      text.value = value
      document.body.appendChild(text)
      text.select()
      document.execCommand('Copy')
      document.body.removeChild(text)
      this.$notify({
        title: this.$langt('common.tip'),
        message: this.$langt('common.copySuccess'),
        position: 'bottom-right',
        type: 'success'
      });
    },
    popoverClose () {
      this.slotPopoverVisible = {}
      if(this.lastLeftNode) {
        this.lastLeftNode.moretool = false
      }
    },
    closePopover () {
      for(let key in this.$refs) {
        if(key.indexOf('slotPopover-') !== -1) {
          if(this.$refs[key].length > 0) {
            for(let i = 1; i < this.$refs[key].length; i++) {
              this.$refs[key][i].$refs.popper.hidden = true
            }
          }
        }
      }
    },
  },
  watch: {
    $route (to, from) {
      if(to.fullPath != from.fullPath) {
        // location.reload()
        this.createdHandle()
      }
    },
    changeRandom: {
      handler(newVal, oldVal) {
        if(newVal > -1 && this.pageSetting) {
          if(this.pageSetting.pageQuery && this.pageSetting.pageQuery.length > 0) {
            this.pageSetting.pageQuery.filter(fit => {
              if(fit.value == this.changeDomItem.prop) {
                this.getQueryColumn()
                this.getListData()
                this.$forceUpdate()
              }
            })
          }
          if(this.pageSetting.pageSearch && this.pageSetting.pageSearch.length > 0) {
            this.pageSetting.pageSearch.filter(fit => {
              if(fit.value == this.changeDomItem.prop) {
                this.getQueryColumn()
                this.getListData()
                this.$forceUpdate()
              }
            })
          }
        }
      }
    }
  }
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
  //width: 100px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  //padding: 0 12px;
  .more-item{
    width: 70px;
    padding: 4px 30px;
    //padding: 0 20px;
    //width: calc(100% - 24px);
    text-align: left;
    cursor: pointer;
    transition: 0.3s;
    height: 32px;
    //border-radius: 5px;
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
.slot-component-item{
  padding: 10px;
  /deep/.jvs-form{
    .el-form-item{
      margin-bottom: 0;
    }
    .reportTable-item{
      .report-table-info{
        .report-table{
          th{
            text-align: left!important;
          }
        }
      }
    }
  }
  .show-other-model-info-table{
    overflow: hidden;
    .header-body{
      width: 100%;
      overflow: auto;
    }
    .header{
      display: flex;
      align-items: center;
      height: 40px;
      background-color: #F5F6F7;
      width: fit-content;
      .item{
        width: 160px;
        padding: 0 10px;
        font-size: 12px;
        font-family: Source Han Sans, Source Han Sans;
        font-weight: 700;
        color: #363B4C;
        box-sizing: border-box;
        word-break: keep-all;
      }
    }
    .body{
      width: fit-content;
      min-height: 52px;
      max-height: calc(50vh - 108px);
      overflow: auto;
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
          display: flex;
          align-items: center;
          img{
            display: block;
            width: 30px;
            height: 30px;
            cursor: pointer;
          }
          .text-div{
            max-width: 100%;
            height: 24px;
            line-height: 24px;
            border-radius: 4px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: pre;
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
.no-header-full-dialog{
  /deep/.el-dialog__header{
    height: 0!important;
    .el-dialog__title{
      display: none!important;
    }
  }
  /deep/.el-dialog__body{
    padding: 20px 0;
  }
}
.print-list-items{
  ul{
    list-style: none;
    margin: 0;
    padding: 0;
  }
  li{
    height: 28px;
    line-height: 28px;
    padding: 6px 24px;
    cursor: pointer;
    transition: 0.3s;
    &:hover{
      transition: 0.3s;
      background-color: #eff2f7;
    }
    i{
      margin-right: 10px;
    }
  }
}
.tag-tool-bar{
  right: 50px;
  top: 12px;
  display: flex;
  align-items: center;
}

.form-box{
  display: flex;
  height: 100%;
  box-sizing: border-box;
  &.form-box-2{
    border-top: 1px solid #e5e5e5;
    position: relative;
    min-height: calc(50vh - 48px);
    .form-body{
      width: 60%;
    }
    .form-log-box{
      position: absolute;
      top: 0;
      left: 60%;
      width: 40%;
      height: 100%;
      border-left: 1px solid #e5e5e5;
      box-sizing: border-box;
      overflow: hidden;
      overflow-y: auto;
    }
  }
  .form-body{
    width: 100%;
    height: 100%;
    padding: 20px 0;
    box-sizing: border-box;
    overflow: hidden;
    overflow-y: auto;
  }
}

.form-fullscreen-dialog{
  /deep/.el-dialog__body{
    padding: 0;
    height: calc(100% - 45px);
    .tag-tool-bar{
      right: 50px;
      top: 10px;
    }
    .form-log-box{
      width: 30%;
    }
  }
}
.el-dialog__wrapper.form-with-log{
  /deep/.el-dialog{
    .el-dialog__body{
      padding: 0;
    }
  }
}
.log-tag-span-box{
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  margin-left: 10px;
  svg{
    width: 18px;
    height: 18px;
    fill: #6F7588;
    cursor: pointer;
  }
  &:hover{
    background: #E4E7EA;
    border-radius: 4px;
  }
}
.tag-set-info-box{
  padding: 0 25px;
}
.time-line-info-box{
  width: 432px;
  padding: 16px;
  padding-right: 0;
  padding-bottom: 0;
  max-height: 468px;
  box-sizing: border-box;
  overflow: hidden;
  overflow-y: auto;
  /deep/.el-timeline{
    height: 100%;
    margin: 0;
    padding: 0;
    margin-right: 16px;
    .el-timeline-item{
      .el-timeline-item__tail{
        left: 5px;
        top: 16px;
        height: calc(100% - 20px);
        border-width: 1px;
        border-color: #C2C5CF;
      }
      .el-timeline-item__node{
        background-color: #1E6FFF;
      }
      .el-timeline-item__timestamp.is-bottom{
        display: none;
      }
    }
  }
  .line-item{
    font-family: Source Han Sans-Regular, Source Han Sans;
    font-weight: 400;
    img{
      display: block;
      width: 24px;
      height: 24px;
      border-radius: 4px;
    }
    .line-item-top{
      display: flex;
      align-items: center;
      display: flex;
      align-items: center;
      justify-content: space-between;
      .left, .right{
        display: flex;
        align-items: center;
      }
      .left{
        font-size: 14px;
        color: #363B4C;
        img{
          margin-right: 8px;
        }
      }
      .right{
        font-size: 12px;
        color: #6F7588;
      }
    }
    .line-item-content{
      margin-top: 8px;
      padding: 8px 12px;
      background: #F5F6F7;
      border-radius: 4px;
      font-size: 14px;
      color: #363B4C;
      line-height: 20px;
    }
  }
}
.tag-set-info-box{
  h3{
    font-size: 18px;
    font-weight: 600;
    position: relative;
    padding-left: 10px;
    margin: 0;
    margin-bottom: 10px;
  }
  h3::before{
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
/deep/.place-dialog{
  .el-dialog .el-dialog__body{
    padding: 0 30px 20px;
  }
  .place-form-desc{
    color: #a2a3a5;
    margin-bottom: 16px;
  }
  .place-form-list{
    height: 400px;
    overflow: hidden;
    overflow-y: auto;
    .palce-form-item{
      height: 30px;
      display: flex;
      align-items: center;
      margin-top: 10px;
      cursor: pointer;
    }
    .palce-form-item:nth-of-type(1) {
      margin-top: 0;
    }
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
      word-break: keep-all;
      overflow: hidden;
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
      .jvs-select-multiple{
        .el-input__inner{
          height: 36px!important;
        }
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
  padding-left: 0;
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
  z-index: 9;
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
.base-type-list{
  padding: 0;
  margin: 0;
  li{
    display: flex;
    align-items: center;
    margin: 0;
    height: 32px;
    line-height: 32px;
    cursor: pointer;
    padding: 6px 24px;
    i{
      margin-right: 10px;
      font-size: 14px!important;
    }
  }
  li:hover{
    background: #F5F7FA;
  }
  li:nth-last-of-type(1) {
    margin-bottom: 0;
  }
}
.page-open-fullscreen-dialog{
  .page-dialog-footer{
    height: 30px;
    display: flex;
    align-items: center;
    padding: 0 20px;
  }
  /deep/.el-dialog:not(.is-fullscreen){
    .el-dialog__body{
      height: calc(70vh + 30px);
      padding: 10px;
      padding-top: 0;
      box-sizing: border-box;
      .page-dialog-box{
        position: relative;
        height: 100%;
        overflow: hidden;
        .jvs-table{
          .table-top{
            .table-top-left{
              .list-use-top-left{
                >span:nth-of-type(1){
                  margin-left: 0!important;
                }
              }
            }
          }
        }
      }
      .has-bottom-tool{
        height: calc(100% - 30px);
      }
    }
  }
  /deep/.el-dialog.is-fullscreen{
    .el-dialog__body{
      padding: 0 10px;
      padding-bottom: 8px;
      box-sizing: border-box;
      height: calc(100% - 45px);
      overflow: hidden;
      .table-show{
        height: 100%;
      }
      .page-dialog-box{
        position: relative;
        height: 100%;
      }
      .has-bottom-tool{
        height: calc(100% - 30px);
      }
    }
  }
}
.page-slot-column-div{
  height: 24px;
  .tag-div, /deep/.el-tag{
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: pre;
    border-radius: 4px;
    padding: 0 10px;
  }
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
.file-name{
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: pre;
  cursor: pointer;
  &+.file-name{
    margin-top: 10px;
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
.import-data-box{
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
.table-show{
  overflow: hidden;
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
    top: calc(50% - 155px);
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
  .menu-table{
    .table-body-box{
      .el-table__header-wrapper{
        .el-table__header{
          .has-gutter{
            .headerclass{
              th:nth-last-of-type(2) {
                .cell{
                  text-indent: 10px;
                }
              }
            }
          }
        }
      }
      .el-table__body-wrapper{
        .el-table__body{
          .el-table__row{
            .el-table__cell:nth-last-of-type(1){
              .cell{
                div{
                  .el-button--text{
                    span{
                      display: inline-block;
                      max-width: 120px;
                      white-space: pre;
                      text-overflow: ellipsis;
                      overflow: hidden;
                    }
                  }
                }
              }
            }
            .el-table__cell:not(.is-hidden):nth-last-of-type(1){
              .cell{
                div{
                  .el-button--text:nth-of-type(1){
                    margin-left: 10px;
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
.alone-page{
  position: relative;
  height: 100%;
  padding: 20px;
  box-sizing: border-box;
  .jvstable-left-tree{
    padding-left: 20px;
    top: 20px;
    height: calc(100% - 20px);
  }
  .close-tree-tool{
    visibility: visible;
  }
  .jvs-table{
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    .table-body-box{
      flex: 1;
      overflow: hidden;
      .el-table{
        height: 100%;
        .el-table__body-wrapper{
          height: calc(100% - 40px);
        }
      }
    }
  }
  .jvs-table-leftTree{
    width: calc(100% - 230px);
    margin-left: 230px;
  }
  .jvs-table-leftTree-hide{
    margin-left: 0;
    width: 100%;
  }
}
.line-button-pover{
  min-width: auto;
  margin-top: 0!important;
}
</style>
