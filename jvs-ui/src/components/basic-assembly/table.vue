<template>
  <div :class="{'jvs-table': true, 'jvs-table-nocolumn': (!option.column || option.column.length == 0)}" :id="tableKey" @click.stop="closeMyPopover">
    <PageHeader :title="pageheadertitle" :class="{'jvs-table-titleTop': true, 'jvs-table-top': !option.search == false, 'jvs-table-hideTop': option.hideTop, 'jvs-table-notitle': !pageheadertitle}">
      <slot name="headerTop"></slot>
      <jvs-form v-if="!option.search == false && searchOption.column && searchOption.column.length > 0" :formData="searchFormData" :defalutFormData="searchFormData" class="search-form" :option="searchOption" @submit="searchHandle" :isSearch="true">
      </jvs-form>
      <div class="table-top">
        <div class="table-top-left">
          <el-button type="primary" :size="$store.state.params.btn.size || 'mini'" v-if="!(option.addBtn==false)" @click="addForm" icon="el-icon-plus">{{option.addBtnText || $langt('table.add')}}</el-button>
          <slot name="menuLeft"></slot>
        </div>
        <div class="table-top-right">
          <slot name="menuRight"></slot>
        </div>
      </div>
    </PageHeader>
    <h4 class="table-title">{{option.title}}</h4>
    <div class="table-body-top">
      <slot name="tableTop"></slot>
    </div>
    <div :class="'table-body-box '+tableKey">
      <!-- 卡片 -->
      <div v-if="displayType == 'card'" :class="{'jvs-table-body-slot': true, 'jvs-table-body-slot-loading': loading}">
        <div v-if="!displaySlot" class="table-body-slot-box">
          <div v-for="(row, index) in data" :key="row.id+'-'+index" class="table-body-slot-box-item">
            <div class="card-top-head">
              <!-- 标题 -->
              <div class="table-body-slot-box-item-row table-body-slot-box-item-row-title">
                <div v-if="titleItem" :style="'display: flex;align-items:center;max-width: 50%;'+(styleRowItem(row, titleItem, 'bgcolor') ? ('padding: 0 10px;border-radius: 4px;background:' + styleRowItem(row, titleItem, 'bgcolor') + ';') : '')">
                  <h4 class="title" :style="styleRowItem(row, titleItem, 'color').startsWith('#') ? `color: ${styleRowItem(row, titleItem, 'color')};` : `${styleRowItem(row, titleItem, 'color')};`">{{titleItem.prepend ? titleItem.prepend : ''}}{{row[titleItem.prop+'_1'] ? fixedNumber(row[titleItem.prop+'_1'], titleItem) : (row[titleItem.prop] instanceof Array ? (row[titleItem.prop].length > 0 ? row[titleItem.prop].join(',') : '') : fixedNumber(row[titleItem.prop], titleItem))}}{{titleItem.append ? titleItem.append : ''}}{{titleItem.unit ? titleItem.unit: ''}}</h4>
                  <i v-if="titleItem.tools && titleItem.tools.indexOf('copy') > -1 && !arrIncludeArr(['openForm', 'dataModelDisplay'], titleItem.tools)" class="el-icon-copy-document" style="margin-left: 5px;cursor: pointer;" @click="copyHandle(row[titleItem.prop+'_1'] ? row[titleItem.prop+'_1'] : (row[titleItem.prop] instanceof Array ? (row[titleItem.prop].length > 0 ? row[titleItem.prop].join(',') : '') : row[titleItem.prop]))"></i>
                  <i v-if="titleItem.tools && titleItem.tools.length > 0 && (arrIncludeArr(['copy', 'openForm', 'dataModelDisplay'], titleItem.tools, 'all') > 1 ? true : !arrIncludeArr(['copy'], titleItem.tools, 'one'))" class="more-tool-i">
                    <div v-if="arrIncludeArr(['copy', 'openForm', 'dataModelDisplay'], titleItem.tools, 'all') > 1" class="table-column-svg-button-icon" @click="moreToolHandle($event, titleItem, row)">
                      <svg class="icon" aria-hidden="true">
                        <use xlink:href="#jvs-ui-icon-a-gengduo1x"></use>
                      </svg>
                    </div>
                    <div v-if="arrIncludeArr(['openForm'], titleItem.tools, 'one')" class="table-column-svg-button-icon" @click.stop="$emit('openForm', titleItem, row)">
                      <svg class="icon" aria-hidden="true">
                        <use xlink:href="#jvs-ui-icon-biaodan"></use>
                      </svg>
                    </div>
                    <div v-if="arrIncludeArr(['dataModelDisplay'], titleItem.tools, 'one')" class="table-column-svg-button-icon" @click.stop="$emit('openModelDisplay', $event, titleItem, row)">
                      <svg class="icon" aria-hidden="true">
                        <use xlink:href="#jvs-ui-icon-fudongchuang"></use>
                      </svg>
                    </div>
                  </i>
                </div>
                <img class="img" v-if="subTitleItem && subTitleItem.type == 'image' && (typeof row[subTitleItem.prop] == 'string' || (row[subTitleItem.prop] && row[subTitleItem.prop].length == 1))" :src="typeof row[subTitleItem.prop] == 'string' ? row[subTitleItem.prop] :  ( (row[subTitleItem.prop] && row[subTitleItem.prop].length > 0) ? row[subTitleItem.prop][0].url : '' )" style="cursor: pointer;" @click="previewImage(row[subTitleItem.prop])" />
                <el-button v-if="subTitleItem && subTitleItem.type == 'image' && (row[subTitleItem.prop] && row[subTitleItem.prop] instanceof Array && row[subTitleItem.prop].length > 1)" type="text" @click.stop="myPopoverHandle($event, subTitleItem, row)">共{{row[subTitleItem.prop].length + (subTitleItem.type == 'image' ? '张图片' : '个文件')}}</el-button>
                <div class="sub-title-item" v-if="subTitleItem && subTitleItem.type != 'image'" :style="(styleRowItem(row, subTitleItem, 'bgcolor') ? `background: ${styleRowItem(row, subTitleItem, 'bgcolor')};padding: 0 10px;` : ``)">
                  <div class="span" :style="(styleRowItem(row, subTitleItem, 'color').startsWith('#') ?
                    `color: ${styleRowItem(row, subTitleItem, 'color')};` :
                    `${styleRowItem(row, subTitleItem, 'color')}`)"
                  >{{subTitleItem.prepend ? subTitleItem.prepend : ''}}{{fixedNumber(styleRowItem(row, subTitleItem, 'text'), subTitleItem)}}{{subTitleItem.append ? subTitleItem.append : ''}}{{subTitleItem.unit ? subTitleItem.unit: ''}}</div>
                </div>
              </div>
              <!-- 小标题 & 描述 -->
              <div class="table-body-slot-box-item-row-subhead-desc">
                <div v-if="subheadingItem" class="table-body-slot-box-item-row-subhead-desc-item" :style="(styleRowItem(row, subheadingItem, 'bgcolor') ? `background: ${styleRowItem(row, subheadingItem, 'bgcolor')};padding: 0 10px;` : ``)">
                  <div :style="styleRowItem(row, subheadingItem, 'color').startsWith('#') ? `color: ${styleRowItem(row, subheadingItem, 'color')};` : `${styleRowItem(row, subheadingItem, 'color')}`">{{subheadingItem.prepend ? subheadingItem.prepend : ''}}{{row[subheadingItem.prop+'_1'] ? row[subheadingItem.prop+'_1'] : (row[subheadingItem.prop] instanceof Array ? (row[subheadingItem.prop].length > 0 ? row[subheadingItem.prop].join(',') : '') : row[subheadingItem.prop])}}{{subheadingItem.append ? subheadingItem.append : ''}}{{subheadingItem.unit ? subheadingItem.unit: ''}}</div>
                </div>
                <span v-if="describeItem" class="divider-bar"></span>
                <div v-if="describeItem" class="table-body-slot-box-item-row-subhead-desc-item" :style="(styleRowItem(row, describeItem, 'bgcolor') ? `background: ${styleRowItem(row, describeItem, 'bgcolor')};padding: 0 10px;` : ``)">
                  <div :style="styleRowItem(row, describeItem, 'color').startsWith('#') ? `color: ${styleRowItem(row, describeItem, 'color')};` : `${styleRowItem(row, describeItem, 'color')}`">{{describeItem.prepend ? describeItem.prepend : ''}}{{row[describeItem.prop+'_1'] ? row[describeItem.prop+'_1'] : row[describeItem.prop]}}{{describeItem.append ? describeItem.append : ''}}{{describeItem.unit ? describeItem.unit: ''}}</div>
                </div>
              </div>
            </div>
            <!-- 其余项 -->
            <div class="table-body-slot-box-row-others">
              <div v-for="(item, cix) in option.column" :key="'card-item-'+index+'-'+cix" :class="{'table-body-slot-box-item-row': true}" v-if="(!item.cardPosition && showByIndex(item))">
                <div v-if="!item.cardPosition && showByIndex(item)" class="info">
                  <span class="label">{{item.label}}</span>
                  <span class="con">
                    <!-- 自定义 -->
                    <slot v-if="item.slot && !item.expand" :name="item.prop" :row="row" :index="index"></slot>
                    <!-- 动态控制 -->
                    <span v-if="!item.slot && !item.expand && ( (item.expressControl && item.expressControl.length > 0) || (item.conditionControl && item.conditionControl.length > 0) )"
                      :style="styleRowItem(row, item, 'bgcolor') ? `background:${styleRowItem(row, item, 'bgcolor')};padding: 0 10px;` : ''">
                      <div :style="styleRowItem(row, item, 'color').startsWith('#') ? `color: ${styleRowItem(row, item, 'color')}` : `${styleRowItem(row, item, 'color')}`">{{item.prepend ? item.prepend : ''}}{{styleRowItem(row, item, 'text')}}{{item.append ? item.append : ''}}{{item.unit ? item.unit: ''}}</div>
                    </span>
                    <span v-if="!item.slot && !item.expand && (!item.expressControl || item.expressControl.length == 0) && (!item.conditionControl || item.conditionControl.length == 0)" :style="styleRowItem(row, item, 'bgcolor') ? `background:${styleRowItem(row, item, 'bgcolor')};padding: 0 10px;`: ''">
                      <!-- 一般列 -->
                      <div v-if="(!item.dicData || item.dicData.length == 0 || item.datatype == 'rule') && (['datetime', 'link', 'image', 'file'].indexOf(item.type) == -1)" :style="(styleRowItem(row, item, 'color').startsWith('#') ? `color: ${styleRowItem(row, item, 'color')};` : `${styleRowItem(row, item, 'color')};`)">{{item.prepend ? item.prepend : ''}}{{row[item.prop+'_1'] ? fixedNumber(row[item.prop+'_1'], item) : (row[item.prop] instanceof Array ? (row[item.prop].length > 0 ? row[item.prop].join(',') : '') : fixedNumber(row[item.prop], item))}}{{item.append ? item.append : ''}}{{item.unit ? item.unit: ''}}</div>
                      <!-- 特殊颜色 -->
                      <div v-if="item.color && (!item.dicData || item.dicData.length == 0) && (['datetime', 'link', 'image', 'file'].indexOf(item.type) == -1) && ( (item.expressControl && item.expressControl.length > 0) || (item.conditionControl && item.conditionControl.length > 0) )" :style="(styleRowItem(row, item, 'color').startsWith('#') ? `color: ${styleRowItem(row, item, 'color')};display: inline-block;` : `${styleRowItem(row, item, 'color')};display: inline-block;`)">{{item.prepend ? item.prepend : ''}}{{row[item.prop+'_1'] ? fixedNumber(row[item.prop+'_1'], item) : (row[item.prop] instanceof Array ? (row[item.prop].length > 0 ? row[item.prop].join(',') : '') : fixedNumber(row[item.prop], item))}}{{item.append ? item.append : ''}}{{item.unit ? item.unit: ''}}</div>
                      <!-- 日期时间 -->
                      <div v-if="item.type == 'datetime'" :style="(styleRowItem(row, item, 'color').startsWith('#') ? `color: ${styleRowItem(row, item, 'color')};` : `${styleRowItem(row, item, 'color')};`)">{{row[item.prop] | dateFormat(item.format)}}</div>
                      <!-- 字典 -->
                      <div v-if="(['datetime', 'link', 'image', 'file'].indexOf(item.type) == -1) && (item.datatype != 'rule' && item.dicData && item.dicData.length > 0)" :style="(styleRowItem(row, item, 'color').startsWith('#') ? `color: ${styleRowItem(row, item, 'color')};` : `${styleRowItem(row, item, 'color')};`)">{{row[item.prop] | dicFormat(item.dicData, item.props)}}</div>
                      <!-- 链接 -->
                      <a :href="formatUrl(row[item.prop])" :target="item.openType || '_blank'" v-if="item.type == 'link'" :style="(styleRowItem(row, item, 'color').startsWith('#') ? `color: ${styleRowItem(row, item, 'color')};` : `${styleRowItem(row, item, 'color')};`)">{{item.text}}</a>
                      <!-- 图片 -->
                      <img v-if="item.type == 'image' && (typeof row[item.prop] == 'string' || (row[item.prop] && row[item.prop].length == 1))" :src="typeof row[item.prop] == 'string' ? row[item.prop] :  ( (row[item.prop] && row[item.prop].length > 0) ? row[item.prop][0].url : '' )" style="display:block;height: 95px;cursor: pointer;" @click="previewImage(row[item.prop])" />
                      <!-- 文件 -->
                      <a v-if="['file'].indexOf(item.type) > -1  && (typeof row[item.prop] == 'string' || (row[item.prop] && row[item.prop].length == 1))" :href="typeof row[item.prop] == 'string' ? row[item.prop] :  ( (row[item.prop] && row[item.prop].length > 0) ? row[item.prop][0].url : '')" :target="'_blank'">{{typeof row[item.prop] == 'string' ? '文件' :  ( (row[item.prop] && row[item.prop].length > 0) ? row[item.prop][0].name : '')}}</a>
                      <el-button v-if="['image', 'file'].indexOf(item.type) > -1 && (row[item.prop] && row[item.prop] instanceof Array && row[item.prop].length > 1)" type="text" @click.stop="myPopoverHandle($event, item, row)" :style="(styleRowItem(row, item, 'color').startsWith('#') ? `color: ${styleRowItem(row, item, 'color')};` : `${styleRowItem(row, item, 'color')};`)">共{{row[item.prop].length + (item.type == 'image' ? '张图片' : '个文件')}}</el-button>
                    </span>
                    <i v-if="item.tools && item.tools.indexOf('copy') > -1 && !arrIncludeArr(['openForm', 'dataModelDisplay'], item.tools)" class="el-icon-copy-document" style="margin-left: 5px;cursor: pointer;" @click="copyHandle(row[item.prop+'_1'] ? row[item.prop+'_1'] : (row[item.prop] instanceof Array ? (row[item.prop].length > 0 ? row[item.prop].join(',') : '') : row[item.prop]))"></i>
                    <i v-if="item.tools && item.tools.length > 0 && (arrIncludeArr(['copy', 'openForm', 'dataModelDisplay'], item.tools, 'all') > 1 ? true : !arrIncludeArr(['copy'], item.tools, 'one'))" class="more-tool-i">
                      <div v-if="arrIncludeArr(['copy', 'openForm', 'dataModelDisplay'], item.tools, 'all') > 1" class="table-column-svg-button-icon" @click="moreToolHandle($event, item, row)">
                        <svg class="icon" aria-hidden="true">
                          <use xlink:href="#jvs-ui-icon-a-gengduo1x"></use>
                        </svg>
                      </div>
                      <div v-if="arrIncludeArr(['openForm'], item.tools, 'one')" class="table-column-svg-button-icon" @click.stop="$emit('openForm', item, row)">
                        <svg class="icon" aria-hidden="true">
                          <use xlink:href="#jvs-ui-icon-biaodan"></use>
                        </svg>
                      </div>
                      <div v-if="arrIncludeArr(['dataModelDisplay'], item.tools, 'one')" class="table-column-svg-button-icon" @click.stop="$emit('openModelDisplay', $event, item, row)">
                        <svg class="icon" aria-hidden="true">
                          <use xlink:href="#jvs-ui-icon-fudongchuang"></use>
                        </svg>
                      </div>
                    </i>
                  </span>
                </div>
              </div>
            </div>
            <!-- 操作栏 -->
            <div class="card-bottom">
              <slot name="menu" :row="row" :index="index"></slot>
            </div>
          </div>
        </div>
        <slot name="tableBody"></slot>
      </div>
      <!-- 表格 -->
      <el-table
        v-else
        header-row-class-name='headerclass'
        :stripe="true"
        :ref="refs"
        :data="data"
        :tooltip-effect="tooltipEffect"
        :show-header="showHeader"
        :border="option.border"
        :showSummary="option.showsummary"
        :summary-method="getSummaries"
        v-loading="loading"
        :size="size || 'mini'"
        :highlight-current-row="option.highlightCurrentRow"
        empty-text=""
        :row-key="rowKey"
        :default-expand-all="false"
        :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
        :lazy="option.lazy || false"
        :load="loadHandle"
        @row-click="rowClick"
        @selection-change="handleSelectionChange"
        @sort-change="handleSort"
        style="margin-top:10px"
        :class="{'jvs-fixed-column-table': hasFixed, 'jvs-table-menu-column': option.menu!==false, 'jvs-title-column-table': ((option.tableTitle && option.tableTitle.length > 0) || showGantt), 'jvs-table-no-menu': option.menu===false}"
      >
        <el-table-column type="selection" width="55" v-if="selectable" class-name="table-select-column"></el-table-column>
        <el-table-column type="index" :width="option.indexWidth || 50" :label="option.indexLabel" v-if="index" class-name="table-index-column"> </el-table-column>
        <template v-if="option.tableTitle && option.tableTitle.length > 0">
          <el-table-column
            v-for="(item, inx) in titleColumn"
            :key="item.prop ? ('table-title-'+inx) : item.prop"
            :label="item.label"
            :prop="item.prop ? item.prop : null"
            :show-overflow-tooltip="item.prop ? (item.type == 'image' ? false : tooltipShow(item, option)) : false"
            :header-align="item.prop ? (option.menuAlign) :'center'"
            :align="item.prop ? (item.align || option.align) :'center'"
            :width="item.prop ? item.width : ''"
            :sortable="item.prop ? (item.sort ? 'custom' : false) : false"
            v-if="!item.hide"
            :type="item.expand"
            :fixed="item.fixed"
            :class-name="item.prop ? 'table-notitle-column' : 'table-title-column'"
          >
            <!-- 表头文字说明 -->
            <template slot="header" slot-scope="scope">
              <span v-if="editable && item.rules && item.rules.length > 0 && item.rules[0].required" style="color:#f56c6c;">*</span>
              <span>{{item.label}}</span>
              <el-tooltip v-if="item.headerExplain" effect="light" :content="item.explainContent" placement="top">
                <i class="el-icon-info info-icon"/>
              </el-tooltip>
              <span v-if="item.tools && item.tools.indexOf('sort') > -1" :class="{'sort-column-item-icon': true, 'desc': getSortStatus(item.prop, 'DESC'), 'asc': getSortStatus(item.prop, 'ASC')}" @click="sortIconClick(item)">
                <i class="el-icon-caret-top"></i>
                <i class="el-icon-caret-bottom"></i>
              </span>
            </template>
            <template slot-scope="scope">
              <span v-if="item.prop" :style="(typeof scope.row[item.prop] == 'string' ? (!scope.row[item.prop]) : ((scope.row[item.prop] instanceof Array && scope.row[item.prop].length < 1) || [undefined, null].indexOf(scope.row[item.prop]) > -1 || JSON.stringify(scope.row[item.prop]) == '{}')) ? 'width: 100%;' : ''">
                <!-- 自定义 -->
                <slot v-if="item.slot && !item.expand" :name="item.prop" :row="scope.row" :index="scope.$index"></slot>
                <!-- 动态控制 -->
                <span v-if="!item.slot && !item.expand && ( (item.expressControl && item.expressControl.length > 0) || (item.conditionControl && item.conditionControl.length > 0) )" class="table-column-span" :style="styleRowItem(scope.row, item, 'bgcolor') ? `background: ${styleRowItem(scope.row, item, 'bgcolor')}` : ''">
                  <div :style="styleRowItem(scope.row, item, 'color').startsWith('#') ? `color: ${styleRowItem(scope.row, item, 'color')}` : `${styleRowItem(scope.row, item, 'color')}`">{{(scope.row[item.prop] && item.prepend) ? item.prepend : ''}}{{styleRowItem(scope.row, item, 'text')}}{{(scope.row[item.prop] && item.append) ? item.append : ''}}{{(scope.row[item.prop] && item.unit) ? item.unit : ''}}</div>
                </span>
                <span v-if="!item.slot && !item.expand && (!item.expressControl || item.expressControl.length == 0) && (!item.conditionControl || item.conditionControl.length == 0)" class="table-column-span" :style="item.backColor ? `background:${item.backColor};padding: 0 10px;` : ''">
                  <!-- 一般列 -->
                  <div v-if="(!item.dicData || item.dicData.length == 0 || item.datatype == 'rule') && (['datetime', 'link', 'image', 'file'].indexOf(item.type) == -1)" :class="{'back-color-text': (item.color && !item.color.startsWith('#'))}" :style="(item.color && (item.color.startsWith('#') ? `color: ${item.color};` : `background:${item.color};`))">{{(scope.row[item.prop] && item.prepend) ? item.prepend : ''}}{{scope.row[item.prop+'_1'] ? fixedNumber(scope.row[item.prop+'_1'], item) : (scope.row[item.prop] instanceof Array ? (scope.row[item.prop].length > 0 ? scope.row[item.prop].join(',') : '') : fixedNumber(scope.row[item.prop], item))}}{{(scope.row[item.prop] && item.append) ? item.append : ''}}{{(scope.row[item.prop] && item.unit) ? item.unit : ''}}</div>
                  <!-- 特殊颜色 -->
                  <div v-if="item.color && (!item.dicData || item.dicData.length == 0) && (['datetime', 'link', 'image', 'file'].indexOf(item.type) == -1) && ( (item.expressControl && item.expressControl.length > 0) || (item.conditionControl && item.conditionControl.length > 0) )" :class="{'back-color-text': (item.color && !item.color.startsWith('#'))}" :style="(item.color && (item.color.startsWith('#') ? `color: ${item.color};display: inline-block;` : `background:${item.color};display: inline-block;`))">{{(scope.row[item.prop] && item.prepend) ? item.prepend : ''}}{{scope.row[item.prop+'_1'] ? fixedNumber(scope.row[item.prop+'_1'], item) : (scope.row[item.prop] instanceof Array ? (scope.row[item.prop].length > 0 ? scope.row[item.prop].join(',') : '') : fixedNumber(row[item.prop], item))}}{{(scope.row[item.prop] && item.append) ? item.append : ''}}{{(scope.row[item.prop] && item.unit) ? item.unit : ''}}</div>
                  <!-- 日期时间 -->
                  <div v-if="item.type == 'datetime'" :class="{'back-color-text': (item.color && !item.color.startsWith('#'))}" :style="(item.color && (item.color.startsWith('#') ? `color: ${item.color};` : `background:${item.color};`))">{{scope.row[item.prop] | dateFormat(item.format)}}</div>
                  <!-- 字典 -->
                  <div v-if="(['datetime', 'link', 'image', 'file'].indexOf(item.type) == -1) && (item.datatype != 'rule' && item.dicData && item.dicData.length > 0)" :class="{'back-color-text': (item.color && !item.color.startsWith('#'))}" :style="(item.color && (item.color.startsWith('#') ? `color: ${item.color};` : `background:${item.color};`))">{{scope.row[item.prop] | dicFormat(item.dicData, item.props)}}</div>
                  <!-- 链接 -->
                  <a :href="formatUrl(scope.row[item.prop])" :target="item.openType || '_blank'" v-if="item.type == 'link'" :class="{'back-color-text': (item.color && !item.color.startsWith('#'))}" :style="(item.color && (item.color.startsWith('#') ? `color: ${item.color};` : `background:${item.color};`))">{{item.text}}</a>
                  <!-- 图片 -->
                  <img v-if="item.type == 'image' && (typeof scope.row[item.prop] == 'string' || (scope.row[item.prop] && scope.row[item.prop].length == 1))" :src="typeof scope.row[item.prop] == 'string' ? scope.row[item.prop] :  ( (scope.row[item.prop] && scope.row[item.prop].length > 0) ? scope.row[item.prop][0].url : '' )" :style="(item.imgWidth ? ('width:' + item.imgWidth + 'px;') : '') + (item.imgHeight ? ('height:' + item.imgHeight + 'px;') : '') + 'cursor: pointer;'" @click="previewImage(scope.row[item.prop])" />
                  <!-- 文件 -->
                  <a class="file-text-a" v-if="['file'].indexOf(item.type) > -1  && (typeof scope.row[item.prop] == 'string' || (scope.row[item.prop] && scope.row[item.prop].length == 1))" :class="{'back-color-text': (item.color && !item.color.startsWith('#'))}" :style="(item.color && (item.color.startsWith('#') ? `color: ${item.color};` : `background:${item.color};`))" @click="getPreviewUrl(scope.row, item, 0)" :target="'_blank'">{{typeof scope.row[item.prop] == 'string' ? '文件' :  ( (scope.row[item.prop] && scope.row[item.prop].length > 0) ? scope.row[item.prop][0].name : '')}}</a>
                  <i v-if="['file'].indexOf(item.type) > -1  && (typeof scope.row[item.prop] == 'string' || (scope.row[item.prop] && scope.row[item.prop].length == 1))" class="el-icon-download down-file-icon" @click="$downloadUrl(typeof scope.row[item.prop] == 'string' ? {url:scope.row[item.prop]} :  ( (scope.row[item.prop] && scope.row[item.prop].length > 0) ? {...scope.row[item.prop][0]} : {}))"></i>
                  <el-button v-if="['image', 'file'].indexOf(item.type) > -1 && (scope.row[item.prop] && scope.row[item.prop] instanceof Array && scope.row[item.prop].length > 1)" type="text" :class="{'back-color-text': (item.color && !item.color.startsWith('#'))}" :style="(item.color && (item.color.startsWith('#') ? `color: ${item.color};` : `background:${item.color};`))" @click.stop="myPopoverHandle($event, item, scope.row)">共{{scope.row[item.prop].length + (item.type == 'image' ? '张图片' : '个文件')}}</el-button>
                </span>
                <i v-if="item.tools && item.tools.indexOf('copy') > -1 && !arrIncludeArr(['openForm', 'dataModelDisplay'], item.tools)" class="el-icon-copy-document" style="margin-left: 5px;cursor: pointer;" @click="copyHandle(scope.row[item.prop+'_1'] ? scope.row[item.prop+'_1'] : (scope.row[item.prop] instanceof Array ? (scope.row[item.prop].length > 0 ? scope.row[item.prop].join(',') : '') : scope.row[item.prop]))"></i>
                <i v-if="item.tools && item.tools.length > 0 && (arrIncludeArr(['copy', 'openForm', 'dataModelDisplay'], item.tools, 'all') > 1 ? true : !arrIncludeArr(['copy'], item.tools, 'one'))" class="more-tool-i">
                  <div v-if="arrIncludeArr(['copy', 'openForm', 'dataModelDisplay'], item.tools, 'all') > 1" class="table-column-svg-button-icon" @click="moreToolHandle($event, item, scope.row)">
                    <svg class="icon" aria-hidden="true">
                      <use xlink:href="#jvs-ui-icon-a-gengduo1x"></use>
                    </svg>
                  </div>
                  <div v-if="arrIncludeArr(['openForm'], item.tools, 'one')" class="table-column-svg-button-icon" @click.stop="$emit('openForm', item, scope.row)">
                    <svg class="icon" aria-hidden="true">
                      <use xlink:href="#jvs-ui-icon-biaodan"></use>
                    </svg>
                  </div>
                  <div v-if="arrIncludeArr(['dataModelDisplay'], item.tools, 'one')" class="table-column-svg-button-icon" @click.stop="$emit('openModelDisplay', $event, item, scope.row)">
                    <svg class="icon" aria-hidden="true">
                      <use xlink:href="#jvs-ui-icon-fudongchuang"></use>
                    </svg>
                  </div>
                </i>
              </span>
            </template>
            <template v-if="item.column && item.column.length > 0">
              <el-table-column
                v-for="it in item.column"
                :key="it.prop"
                :label="it.label"
                :prop="it.prop"
                :show-overflow-tooltip="it.type == 'image' ? false : tooltipShow(it, option)"
                :header-align="option.menuAlign"
                :align="it.align || option.align"
                :width="it.width"
                :sortable="it.sort ? 'custom' : false"
                v-if="!it.hide"
                :type="it.expand"
                :fixed="it.fixed"
              >
                <!-- 表头文字说明 -->
                <template slot="header" slot-scope="scope">
                  <span v-if="editable && it.rules && it.rules.length > 0 && it.rules[0].required" style="color:#f56c6c;">*</span>
                  <span>{{it.label}}</span>
                  <el-tooltip v-if="it.headerExplain" effect="light" :content="it.explainContent" placement="top">
                    <i class="el-icon-info info-icon"/>
                  </el-tooltip>
                  <span v-if="it.tools && it.tools.indexOf('sort') > -1" :class="{'sort-column-item-icon': true, 'desc': getSortStatus(it.prop, 'DESC'), 'asc': getSortStatus(it.prop, 'ASC')}" @click="sortIconClick(it)">
                    <i class="el-icon-caret-top"></i>
                    <i class="el-icon-caret-bottom"></i>
                  </span>
                </template>
                <template slot-scope="scope">
                  <span :style="(typeof scope.row[it.prop] == 'string' ? (!scope.row[it.prop]) : ((scope.row[it.prop] instanceof Array && scope.row[it.prop].length < 1) || [undefined, null].indexOf(scope.row[it.prop]) > -1 || JSON.stringify(scope.row[it.prop]) == '{}')) ? 'width:100%;' : ''">
                    <!-- 自定义 -->
                    <slot v-if="it.slot && !it.expand" :name="it.prop" :row="scope.row" :index="scope.$index"></slot>
                    <!-- 动态控制 -->
                    <span v-if="!it.slot && !it.expand && ( (it.expressControl && it.expressControl.length > 0) || (it.conditionControl && it.conditionControl.length > 0) )" class="table-column-span" :style="styleRowItem(scope.row, it, 'bgcolor') ? `background:${styleRowItem(scope.row, it, 'bgcolor')}` : ''">
                      <div :style="styleRowItem(scope.row, it, 'color').startsWith('#') ? `color: ${styleRowItem(scope.row, it, 'color')};` : `${styleRowItem(scope.row, it, 'color')}`">{{(scope.row[it.prop] && it.prepend) ? it.prepend : ''}}{{styleRowItem(scope.row, it, 'text')}}{{(scope.row[it.prop] && it.append) ? it.append : ''}}{{(scope.row[it.prop] && it.unit) ? it.unit : ''}}</div>
                    </span>
                    <span v-if="!it.slot && !it.expand && (!it.expressControl || it.expressControl.length == 0) && (!it.conditionControl || it.conditionControl.length == 0)" class="table-column-span" :style="it.backColor ? `background:${it.backColor};padding: 0 10px;` : ''">
                      <!-- 一般列 -->
                      <div v-if="(!it.dicData || it.dicData.length == 0 || it.datatype == 'rule') && (['datetime', 'link', 'image', 'file'].indexOf(it.type) == -1)" :class="{'back-color-text': (it.color && !it.color.startsWith('#'))}" :style="(it.color && (it.color.startsWith('#') ? `color: ${it.color};` : `background:${it.color};`))">{{(scope.row[it.prop] && it.prepend) ? it.prepend : ''}}{{scope.row[it.prop+'_1'] ? fixedNumber(scope.row[it.prop+'_1'], it) : (scope.row[it.prop] instanceof Array ? (scope.row[it.prop].length > 0 ? scope.row[it.prop].join(',') : '') : fixedNumber(scope.row[it.prop], it))}}{{(scope.row[it.prop] && it.append) ? it.append : ''}}{{(scope.row[it.prop] && it.unit) ? it.unit : ''}}</div>
                      <!-- 特殊颜色 -->
                      <div v-if="it.color && (!it.dicData || it.dicData.length == 0) && (['datetime', 'link', 'image', 'file'].indexOf(it.type) == -1) && ( (it.expressControl && it.expressControl.length > 0) || (it.conditionControl && it.conditionControl.length > 0) )" :class="{'back-color-text': (it.color && !it.color.startsWith('#'))}" :style="(it.color && (it.color.startsWith('#') ? `color: ${it.color};display: inline-block;` : `background:${it.color};display: inline-block;`))">{{(scope.row[it.prop] && it.prepend) ? it.prepend : ''}}{{scope.row[it.prop+'_1'] ? fixedNumber(scope.row[it.prop+'_1'], it) : (scope.row[it.prop] instanceof Array ? (scope.row[it.prop].length > 0 ? scope.row[it.prop].join(',') : '') : fixedNumber(row[it.prop], it))}}{{(scope.row[it.prop] && it.append) ? it.append : ''}}{{(scope.row[it.prop] && it.unit) ? it.unit : ''}}</div>
                      <!-- 日期时间 -->
                      <div v-if="it.type == 'datetime'" :class="{'back-color-text': (it.color && !it.color.startsWith('#'))}" :style="(it.color && (it.color.startsWith('#') ? `color: ${it.color};` : `background:${it.color};`))">{{scope.row[it.prop] | dateFormat(it.format)}}</div>
                      <!-- 字典 -->
                      <div v-if="(['datetime', 'link', 'image', 'file'].indexOf(it.type) == -1) && (it.datatype != 'rule' && it.dicData && it.dicData.length > 0)" :class="{'back-color-text': (it.color && !it.color.startsWith('#'))}" :style="(it.color && (it.color.startsWith('#') ? `color: ${it.color};` : `background:${it.color};`))">{{scope.row[it.prop] | dicFormat(it.dicData, it.props)}}</div>
                      <!-- 链接 -->
                      <a :href="formatUrl(scope.row[it.prop])" :target="it.openType || '_blank'" v-if="it.type == 'link'" :class="{'back-color-text': (it.color && !it.color.startsWith('#'))}" :style="(it.color && (it.color.startsWith('#') ? `color: ${it.color};` : `background:${it.color};`))">{{it.text}}</a>
                      <!-- 图片 -->
                      <img v-if="it.type == 'image' && (typeof scope.row[it.prop] == 'string' || (scope.row[it.prop] && scope.row[it.prop].length == 1))" :src="typeof scope.row[it.prop] == 'string' ? scope.row[it.prop] :  ( (scope.row[it.prop] && scope.row[it.prop].length > 0) ? scope.row[it.prop][0].url : '' )" :style="(it.imgWidth ? ('width:' + it.imgWidth + 'px;') : '') + (it.imgHeight ? ('height:' + it.imgHeight + 'px;') : '') + 'cursor: pointer;'" @click="previewImage(scope.row[it.prop])" />
                      <!-- 文件 -->
                      <a class="file-text-a" v-if="['file'].indexOf(it.type) > -1  && (typeof scope.row[it.prop] == 'string' || (scope.row[it.prop] && scope.row[it.prop].length == 1))" :class="{'back-color-text': (it.color && !it.color.startsWith('#'))}" :style="(it.color && (it.color.startsWith('#') ? `color: ${it.color};` : `background:${it.color};`))" @click="getPreviewUrl(scope.row, it, 0)" :target="'_blank'">{{typeof scope.row[it.prop] == 'string' ? '文件' :  ( (scope.row[it.prop] && scope.row[it.prop].length > 0) ? scope.row[it.prop][0].name : '')}}</a>
                      <i v-if="['file'].indexOf(item.type) > -1  && (typeof scope.row[item.prop] == 'string' || (scope.row[item.prop] && scope.row[item.prop].length == 1))" class="el-icon-download down-file-icon" @click="$downloadUrl(typeof scope.row[item.prop] == 'string' ? {url:scope.row[item.prop]} :  ( (scope.row[item.prop] && scope.row[item.prop].length > 0) ? {...scope.row[item.prop][0]} : {}))"></i>
                      <el-button v-if="['image', 'file'].indexOf(it.type) > -1 && (scope.row[it.prop] && scope.row[it.prop] instanceof Array && scope.row[it.prop].length > 1)" type="text" :class="{'back-color-text': (it.color && !it.color.startsWith('#'))}" :style="(it.color && (it.color.startsWith('#') ? `color: ${it.color};` : `background:${it.color};`))" @click.stop="myPopoverHandle($event, it, scope.row)">共{{scope.row[it.prop].length + (it.type == 'image' ? '张图片' : '个文件')}}</el-button>
                    </span>
                    <i v-if="it.tools && it.tools.indexOf('copy') > -1&& !arrIncludeArr(['openForm', 'dataModelDisplay'], it.tools)" class="el-icon-copy-document" style="margin-left: 5px;cursor: pointer;" @click="copyHandle(scope.row[it.prop+'_1'] ? scope.row[it.prop+'_1'] : (scope.row[it.prop] instanceof Array ? (scope.row[it.prop].length > 0 ? scope.row[it.prop].join(',') : '') : scope.row[it.prop]))"></i>
                    <i v-if="it.tools && it.tools.length > 0 && (arrIncludeArr(['copy', 'openForm', 'dataModelDisplay'], it.tools, 'all') > 1 ? true : !arrIncludeArr(['copy'], it.tools, 'one'))" class="more-tool-i">
                      <div v-if="arrIncludeArr(['copy', 'openForm', 'dataModelDisplay'], it.tools, 'all') > 1" class="table-column-svg-button-icon" @click="moreToolHandle($event, it, scope.row)">
                        <svg class="icon" aria-hidden="true">
                          <use xlink:href="#jvs-ui-icon-a-gengduo1x"></use>
                        </svg>
                      </div>
                      <div v-if="arrIncludeArr(['openForm'], it.tools, 'one')" class="table-column-svg-button-icon" @click.stop="$emit('openForm', it, scope.row)">
                        <svg class="icon" aria-hidden="true">
                          <use xlink:href="#jvs-ui-icon-biaodan"></use>
                        </svg>
                      </div>
                      <div v-if="arrIncludeArr(['dataModelDisplay'], it.tools, 'one')" class="table-column-svg-button-icon" @click.stop="$emit('openModelDisplay', $event, it, scope.row)">
                        <svg class="icon" aria-hidden="true">
                          <use xlink:href="#jvs-ui-icon-fudongchuang"></use>
                        </svg>
                      </div>
                    </i>
                  </span>
                </template>
              </el-table-column>
            </template>
          </el-table-column>
        </template>
        <template v-else>
          <el-table-column
            v-for="item in option.column"
            :key="item.prop"
            :label="item.label"
            :prop="item.prop"
            :show-overflow-tooltip="item.type == 'image' ? false : tooltipShow(item, option)"
            :header-align="option.menuAlign"
            :align="item.align || option.align"
            :width="item.width"
            :sortable="item.sort ? 'custom' : false"
            v-if="!item.hide"
            :type="item.expand"
            :fixed="item.fixed"
          >
            <!-- 表头文字说明 -->
            <template slot="header" slot-scope="scope">
              <span v-if="editable && item.rules && item.rules.length > 0 && item.rules[0].required" style="color:#f56c6c;">*</span>
              <span>{{item.label}}</span>
              <el-tooltip v-if="item.headerExplain" effect="light" :content="item.explainContent" placement="top">
                <i class="el-icon-info info-icon"/>
              </el-tooltip>
              <span v-if="item.tools && item.tools.indexOf('sort') > -1" :class="{'sort-column-item-icon': true, 'desc': getSortStatus(item.prop, 'DESC'), 'asc': getSortStatus(item.prop, 'ASC')}" @click="sortIconClick(item)">
                <i class="el-icon-caret-top"></i>
                <i class="el-icon-caret-bottom"></i>
              </span>
            </template>
            <template slot-scope="scope">
              <span :style="(typeof scope.row[item.prop] == 'string' ? (!scope.row[item.prop] || item.slot) : ((scope.row[item.prop] instanceof Array && scope.row[item.prop].length < 1) || [undefined, null].indexOf(scope.row[item.prop]) > -1 || JSON.stringify(scope.row[item.prop]) == '{}') || (item.slot && item.showOverflow===false)) ? 'width: 100%;' : ''">
                <!-- 自定义 -->
                <slot v-if="item.slot && !item.expand" :name="item.prop" :row="scope.row" :index="scope.$index"></slot>
                <!-- 动态控制 -->
                <span v-if="!item.slot && !item.expand && ( (item.expressControl && item.expressControl.length > 0) || (item.conditionControl && item.conditionControl.length > 0) )"
                  class="table-column-span"
                  :style="styleRowItem(scope.row, item, 'bgcolor') ? `background:${styleRowItem(scope.row, item, 'bgcolor')};padding: 0 10px;` : ''">
                  <div :style="styleRowItem(scope.row, item, 'color').startsWith('#') ? `color:${styleRowItem(scope.row, item, 'color')}` : `${styleRowItem(scope.row, item, 'color')}`">{{(scope.row[item.prop] && item.prepend) ? item.prepend : ''}}{{styleRowItem(scope.row, item, 'text')}}{{(scope.row[item.prop] && item.append) ? item.append : ''}}{{(scope.row[item.prop] && item.unit) ? item.unit : ''}}</div>
                </span>
                <span v-if="!item.slot && !item.expand && (!item.expressControl || item.expressControl.length == 0) && (!item.conditionControl || item.conditionControl.length == 0)" class="table-column-span" :style="item.backColor ? `background:${item.backColor};padding: 0 10px;`: ''">
                  <!-- 一般列 -->
                  <div v-if="(!item.dicData || item.dicData.length == 0 || item.datatype == 'rule' || ['cascader', 'department'].indexOf(item.type) > -1) && (['datetime', 'link', 'image', 'file'].indexOf(item.type) == -1)" :class="{'back-color-text': (item.color && !item.color.startsWith('#'))}" :style="(item.color && (item.color.startsWith('#') ? `color: ${item.color};` : `background:${item.color};`))">{{(scope.row[item.prop] && item.prepend) ? item.prepend : ''}}{{scope.row[item.prop+'_1'] ? fixedNumber(scope.row[item.prop+'_1'], item) : (scope.row[item.prop] instanceof Array ? (scope.row[item.prop].length > 0 ? scope.row[item.prop].join(',') : '') : fixedNumber(scope.row[item.prop], item))}}{{(scope.row[item.prop] && item.append) ? item.append : ''}}{{(scope.row[item.prop] && item.unit) ? item.unit : ''}}</div>
                  <!-- 特殊颜色 -->
                  <div v-if="item.color && (!item.dicData || item.dicData.length == 0) && (['datetime', 'link', 'image', 'file'].indexOf(item.type) == -1) && ( (item.expressControl && item.expressControl.length > 0) || (item.conditionControl && item.conditionControl.length > 0) )" :class="{'back-color-text': (item.color && !item.color.startsWith('#'))}" :style="(item.color && (item.color.startsWith('#') ? `color: ${item.color};display: inline-block;` : `background:${item.color};display: inline-block;`))">{{(scope.row[item.prop] && item.prepend) ? item.prepend : ''}}{{scope.row[item.prop+'_1'] ? fixedNumber(scope.row[item.prop+'_1'], item) : (scope.row[item.prop] instanceof Array ? (scope.row[item.prop].length > 0 ? scope.row[item.prop].join(',') : '') : fixedNumber(row[item.prop], item))}}{{(scope.row[item.prop] && item.append) ? item.append : ''}}{{(scope.row[item.prop] && item.unit) ? item.unit : ''}}</div>
                  <!-- 日期时间 -->
                  <div v-if="item.type == 'datetime'" :class="{'back-color-text': (item.color && !item.color.startsWith('#'))}" :style="(item.color && (item.color.startsWith('#') ? `color: ${item.color};` : `background:${item.color};`))">{{scope.row[item.prop] | dateFormat(item.format)}}</div>
                  <!-- 字典 -->
                  <div v-if="(['datetime', 'link', 'image', 'file', 'cascader', 'department'].indexOf(item.type) == -1) && (item.datatype != 'rule' && item.dicData && item.dicData.length > 0)" :class="{'back-color-text': (item.color && !item.color.startsWith('#'))}" :style="(item.color && (item.color.startsWith('#') ? `color: ${item.color};` : `background:${item.color};`))">{{scope.row[item.prop] | dicFormat(item.dicData, item.props)}}</div>
                  <!-- 链接 -->
                  <a :href="formatUrl(scope.row[item.prop])" :target="item.openType || '_blank'" v-if="item.type == 'link'" :class="{'back-color-text': (item.color && !item.color.startsWith('#'))}" :style="(item.color && (item.color.startsWith('#') ? `color: ${item.color};` : `background:${item.color};`))">{{item.text}}</a>
                  <!-- 图片 -->
                  <img v-if="item.type == 'image' && (typeof scope.row[item.prop] == 'string' || (scope.row[item.prop] && scope.row[item.prop].length == 1))" :src="typeof scope.row[item.prop] == 'string' ? scope.row[item.prop] :  ( (scope.row[item.prop] && scope.row[item.prop].length > 0) ? scope.row[item.prop][0].url : '' )" :style="(item.imgWidth ? ('width:' + item.imgWidth + 'px;') : '') + (item.imgHeight ? ('height:' + item.imgHeight + 'px;') : '') + 'cursor: pointer;'" @click="previewImage(scope.row[item.prop])" />
                  <!-- 文件 -->
                  <a class="file-text-a" v-if="['file'].indexOf(item.type) > -1  && (typeof scope.row[item.prop] == 'string' || (scope.row[item.prop] && scope.row[item.prop].length == 1))" :class="{'back-color-text': (item.color && !item.color.startsWith('#'))}" :style="(item.color && (item.color.startsWith('#') ? `color: ${item.color};` : `background:${item.color};`))" @click="getPreviewUrl(scope.row, item, 0)" :target="'_blank'">{{typeof scope.row[item.prop] == 'string' ? '文件' :  ( (scope.row[item.prop] && scope.row[item.prop].length > 0) ? scope.row[item.prop][0].name : '')}}</a>
                  <i v-if="['file'].indexOf(item.type) > -1  && (typeof scope.row[item.prop] == 'string' || (scope.row[item.prop] && scope.row[item.prop].length == 1))" class="el-icon-download down-file-icon" @click="$downloadUrl(typeof scope.row[item.prop] == 'string' ? {url:scope.row[item.prop]} :  ( (scope.row[item.prop] && scope.row[item.prop].length > 0) ? {...scope.row[item.prop][0]} : {}))"></i>
                  <el-button v-if="['image', 'file'].indexOf(item.type) > -1 && (scope.row[item.prop] && scope.row[item.prop] instanceof Array && scope.row[item.prop].length > 1)" type="text" :class="{'back-color-text': (item.color && !item.color.startsWith('#'))}" :style="(item.color && (item.color.startsWith('#') ? `color: ${item.color};` : `background:${item.color};`))" @click.stop="myPopoverHandle($event, item, scope.row)">共{{scope.row[item.prop].length + (item.type == 'image' ? '张图片' : '个文件')}}</el-button>
                </span>
                <i v-if="item.tools && item.tools.indexOf('copy') > -1 && !arrIncludeArr(['openForm', 'dataModelDisplay'], item.tools)" class="el-icon-copy-document" style="margin-left: 5px;cursor: pointer;" @click="copyHandle(scope.row[item.prop+'_1'] ? scope.row[item.prop+'_1'] : (scope.row[item.prop] instanceof Array ? (scope.row[item.prop].length > 0 ? scope.row[item.prop].join(',') : '') : scope.row[item.prop]))"></i>
                <i v-if="item.tools && item.tools.length > 0 && (arrIncludeArr(['copy', 'openForm', 'dataModelDisplay'], item.tools, 'all') > 1 ? true : !arrIncludeArr(['copy'], item.tools, 'one'))" class="more-tool-i">
                  <div v-if="arrIncludeArr(['copy', 'openForm', 'dataModelDisplay'], item.tools, 'all') > 1" class="table-column-svg-button-icon" @click="moreToolHandle($event, item, scope.row)">
                    <svg class="icon" aria-hidden="true">
                      <use xlink:href="#jvs-ui-icon-a-gengduo1x"></use>
                    </svg>
                  </div>
                  <div v-if="arrIncludeArr(['openForm'], item.tools, 'one')" class="table-column-svg-button-icon" @click.stop="$emit('openForm', item, scope.row)">
                    <svg class="icon" aria-hidden="true">
                      <use xlink:href="#jvs-ui-icon-biaodan"></use>
                    </svg>
                  </div>
                  <div v-if="arrIncludeArr(['dataModelDisplay'], item.tools, 'one')" class="table-column-svg-button-icon" @click.stop="$emit('openModelDisplay', $event, item, scope.row)">
                    <svg class="icon" aria-hidden="true">
                      <use xlink:href="#jvs-ui-icon-fudongchuang"></use>
                    </svg>
                  </div>
                </i>
              </span>
            </template>
          </el-table-column>
        </template>
        <el-table-column :fixed="option.menuFix" :label="(option.menuText === '') ? '' : $langt('table.oprate')" :width="option.menuWidth" v-if="option.menu!==false" :align="option.menuAlign">
          <template slot-scope="scope">
            <div>
              <el-button type="text" :size="$store.state.params.btn.size || 'mini'" v-if="!(option.viewBtn==false)" @click="viewHandle(scope.row,scope.$index)">{{option.viewBtnText || $langt('table.view')}}</el-button>
              <el-button type="text" :size="$store.state.params.btn.size || 'mini'" v-if="!(option.editBtn==false)" @click="editHandle(scope.row,scope.$index)">{{option.editBtnText || $langt('table.edit')}}</el-button>
              <!-- 操作栏自定义 -->
              <slot name="menu" :row="scope.row" :index="scope.$index"></slot>
              <el-button type="text" :size="$store.state.params.btn.size || 'mini'" v-if="!(option.delBtn==false)" @click="delHandle(scope.row,scope.$index)"><span style="color: #F56C6C;">{{option.delBtnText || $langt('table.delete')}}</span></el-button>
            </div>
          </template>
        </el-table-column>
        <gantt v-if="showGantt" :fromView="fromView" :option="ganttOption" :min="ganttMin" :max="ganttMax" :fresh="loading"></gantt>
      </el-table>
    </div>
    <div class="tablepagination" v-if="option.page">
      <el-pagination
        background
        :layout="page.layout || pagination.layout"
        :total="page.total || pagination.total"
        :current-page="page.currentPage || pagination.currentPage"
        :page-sizes="page.pageSizes || pagination.pageSizes"
        :page-size="page.pageSize || pagination.pageSize"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      ></el-pagination>
      <slot name="menuLeftBottom"></slot>
    </div>
    <el-dialog
      :title="title"
      :visible.sync="dialogVisible"
      :close-on-click-modal='option.dialogClickModal'
      :close-on-press-escape='option.dialogEscape'
      append-to-body
      :width="option.dialogWidth || '75%'"
      :fullscreen="option.dialogWidth == '100%' ? true : false"
      :class="{'form-fullscreen-dialog': option.dialogWidth == '100%'}"
      :before-close="handleClose">
      <jvs-form v-if="dialogVisible" :formData="rowData" :defalutFormData="rowData" :option="formOpton" @submit="submitHandle" :isSearch="false" @cancalClick="handleClose">
      </jvs-form>
    </el-dialog>
    <!-- 自定义popover -->
    <div v-if="myPopoverShow" class="el-popover el-popper el-popover--plain my-popover-box" x-placement="bottom" :style="`left: ${myPopoverPos.x}px;top: ${myPopoverPos.y}px;`" @click.stop="stopHandle">
      <div class="img-file-list" :style="{maxHeight: `${documentHeight-130}px`}">
        <div v-for="(ifit, ifix) in rowFileList" :key="myPopoverItem.type+'-item-'+ifix" class="if-item">
          <img v-if="myPopoverItem.type == 'image'" :src="ifit.url" :alt="ifit.name" style="cursor: pointer;" @click.stop="previewImage(rowFileList, ifix)">
          <span @click.stop="getPreviewUrl({[myPopoverItem.prop]: rowFileList}, myPopoverItem, ifix)" style="cursor: pointer;">{{ifit.name}}</span>
          <i class="el-icon-download" @click.stop="$downloadUrl(ifit)"></i>
        </div>
      </div>
      <div class="popper__arrow"></div>
    </div>
    <!-- 预览图片 -->
    <el-image-viewer v-if="showViewer" :z-index="9999" :initialIndex="previewImageIndex"  :url-list="previewSrcList" :on-close="closeViewer"/>
    <!-- 多工具弹出框 -->
    <el-dialog :visible.sync="moreToolShow" :modal="false" custom-class="popover-dialog">
      <div v-if="moreToolShow" class="el-popover el-popper el-popover--plain more-tool-box" x-placement="bottom" :style="`left: ${moreToolPos.x}px;top: ${moreToolPos.y}px;`" @click.stop="stopHandle">
        <div class="column-tool-list" v-click-outside="moreToolClose">
          <div v-if="moreToolItem.tools.indexOf('copy') > -1" class="column-tool-list-item" @click.stop="copyHandle(moreToolRow[moreToolItem.prop+'_1'] ? moreToolRow[moreToolItem.prop+'_1'] : (moreToolRow[moreToolItem.prop] instanceof Array ? (moreToolRow[moreToolItem.prop].length > 0 ? moreToolRow[moreToolItem.prop].join(',') : '') : moreToolRow[moreToolItem.prop]))">
            <svg class="icon" aria-hidden="true">
              <use xlink:href="#jvs-ui-icon-fuzhi1"></use>
            </svg>
            <span>复制</span>
          </div>
          <div v-if="moreToolItem.tools.indexOf('dataModelDisplay') > -1" class="column-tool-list-item" @click.stop="$emit('openModelDisplay', $event, moreToolItem, moreToolRow)">
            <svg class="icon" aria-hidden="true">
              <use xlink:href="#jvs-ui-icon-fudongchuang"></use>
            </svg>
            <span>打开关联显示</span>
          </div>
          <div v-if="moreToolItem.tools.indexOf('openForm') > -1" class="column-tool-list-item" @click.stop="$emit('openForm', moreToolItem, moreToolRow)">
            <svg class="icon" aria-hidden="true">
              <use xlink:href="#jvs-ui-icon-biaodan"></use>
            </svg>
            <span>打开表单</span>
          </div>
        </div>
        <div class="popper__arrow"></div>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import PageHeader from '../../components/page-header/PageHeader'
import { getSelectData } from '@/api/index.js'
import {guid} from "@/util/util";
import { Base64 } from 'js-base64'
import { arraySort } from "@/util/dataHanding.js";

export default {
  name: "jvs-table",
  components: {
    PageHeader,
    'el-image-viewer': () => import('element-ui/packages/image/src/image-viewer'),
    gantt: () => import('@/components/basic-assembly/gantt'),
  },
  props: {
    pageheadertitle: {
      type:String,
      default: '',
    },
    // 绑定表格 refs
    refs: {
      type: String,
      default: 'multipleTable'
    },
    // 是否显示表格头
    showHeader: {
      type: Boolean,
      default: true
    },
    // tip提示背景
    tooltipEffect: {
      type: String,
      default: 'light', // 'dark'
    },
    // 是否可以多选
    selectable: {
      type: Boolean,
      default: false
    },
    // 是否提示 等待加载loading
    loading: {
      type: Boolean,
      default: false
    },
    size: {
      type: String,
      default: ''
    },
    // 是否显示顺序
    index: {
      type: Boolean,
      default: false
    },
    // 分页配置
    page: {
      type: Object,
      default: (data) => {
        return {
          total: 0, // 总页数
          currentPage: 1, // 当前页数
          pageSize: 20, // 每页显示多少条
          pageSizes: [20, 50, 100, 200, 500, 1000], // 分页大小
          layout: "total, sizes, prev, pager, next, jumper", // 分页工具
        }
      }
    },
    // 表格数据
    data: {
      type: Array,
      default: () => {
        return []
      }
    },
    // 搜索表单
    formData: {
      type: Object,
      default: () => {
        return {}
      }
    },
    // 表格配置
    option: {
      type: Object,
      default: () => {
        return {
          border: false, // 表格是否边框
          page: true, // 是否分页
          align: 'left', // body对齐
          menuAlign: 'left', // 表头对齐
          menuFix: 'right', // 操作栏固定位置
          menuWidth: 200, // 操作栏宽度
          search: false, // 是否开启查询
          showOverflow: true, // 超出是否合并移入悬浮tip显示
          menu: true,
          indexLabel:'序号',
          // 搜索表单设置
          formAlign: 'right', //对其方式
          inline: false, // 表单项是否可以同行,当垂直方向空间受限且表单较简单时，可以在一行内放置表单
          labelWidth: 'auto', // label宽
          searchBtn:true,//搜索的查询按钮
          searchBtnText: "查询", // 提交按钮文字，默认 提交
          submitBtn: true, // 提交按钮是否显示，默认显示
          submitBtnText: '保存', // 提交按钮文字，默认 提交
          emptyBtn: true, // 重置按钮，默认显示
          emptyBtnText: '清空', // 重置按钮文字，默认 重置
          addDialogText:'新增',//新增弹窗title,默认 新增
          editDialogText: '编辑', //编辑弹框title，默认 编辑
          submitLoading: false, // 默认表单提交按钮loading，初始值默认false
          searchLoading: false, // 查询表单提交按钮loading，初始值默认false
          column: [
            {
              label: '', // 文字
              prop: '', // 字段
              search: false, // 是否搜索
              slot: false, // 是否自定义
              hide: true, // 当前列在表格是否隐藏
              color: '', // 颜色特殊显示
              align: '', // 默认与table保持一致，可自定义 left right center
              menuAlign: '', // 默认与table保持一致，可自定义 left right center
              type: '', // 文本类型，默认input
              dicData: [], // 字典数据
              showOverflow: true, // 超出是否合并移入悬浮tip显示

              // 搜索表单对应
              span: 24, // 表单项栅格比，默认24
              formSlot: false, // 表单项是否自定义

              // 新增、编辑、查看对应表单设置
              addDisabled: false, // 表单新增时是否禁止
              addDisplay: true, // 表单新增时是否可见
              editDisabled: false, // 表单编辑时是否禁止
              editDisplay: false, // 表单编辑是否可见
              viewDisplay: true, // 表单查看是否可见
            }
          ]
        }
      }
    },
    // 是否清空多选，随机数
    isClearSelect: {
      type: Number
    },
    // 已选数据
    selectedRows: {
      type: Array
    },
    // 是否默认全选
    defaultAllSelect: {
      type: Boolean,
      default: false
    },
    editable: {
      type: Boolean
    },
    jvsAppId: {
      type: String
    },
    sortsList: {
      type: Array
    },
    displayType: {
      type: String
    },
    displaySlot: {
      type: Boolean,
      default: false
    },
    rowKey: {
      type: String,
      default: () => {
        return 'id'
      }
    },
    fromDialog: {
      type: Boolean
    },
    dialogHeight: {
      type: Number
    },
    fromOtherOpen: {
      type: Boolean
    },
    noStyleHeight: {
      type: Boolean
    },
    fromView: {
      type: Boolean
    },
    showGantt: {
      type: Boolean
    },
    ganttOption: {
      type: Object
    },
    ganttMin: {
      type: [String, Date]
    },
    ganttMax: {
      type: [String, Date]
    }
  },
  data () {
    return {
      tableKey: 'table' + guid(), // new Date().getTime(),
      searchForm: {},
      tempForm: {},
      title: '', // 弹框标题
      dialogVisible: false,
      rowData: {}, // 行数据
      formOpton: {},
      optype: 'addRow', // 提交方式
      pagination: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
        pageSizes: [20, 50, 100, 200, 500, 1000], // 分页大小
        layout: "total, sizes, prev, pager, next, jumper", // 分页工具
      },
      // 搜索表单配置
      searchOption: {},
      daActionIndex:0,
      sortList: [],
      titleItem: null,
      subTitleItem: null,
      subheadingItem: null,
      describeItem: null,
      cardOtherItems: [],
      hasFixed: false,
      titleColumn: [],
      showViewer: false,
      previewSrcList: [],
      previewImageIndex: 0,
      myPopoverShow: false,
      myPopoverPos: {
        x: 0,
        y: 0
      },
      myPopoverItem: null,
      myPopoverRow: null,
      moreToolShow: false,
      moreToolPos: {
        x: 0,
        y: 0
      },
      moreToolItem: null,
      moreToolRow: null,
      documentHeight: document.documentElement.scrollHeight
    };
  },
  filters: {},
  created () {
    let searchObj = {}
    for(let k in this.option) {
      this.$set(this.searchOption, k, this.option[k])
    }
    this.searchOption.labelWidth = 'auto'
    let temp = []
    for(let i in this.searchOption.column) {
      if(this.option.column[i].dicUrl) {
        let _this = this
        getSelectData(this.option.column[i].dicUrl).then(res=>{
          _this.option.column[i].dicData = res.data.data
        })
      }
      if(this.searchOption.column[i] && this.searchOption.column[i].search == true) {
        if(this.searchOption.column[i].formSlot == true) {
          this.searchOption.column[i].formSlot = false
        }
        searchObj[this.searchOption.column[i].prop] = null
        // 弹窗表格的栅格
        if(this.searchOption.column[i] && !this.searchOption.column[i].span) {
          this.searchOption.column[i].span = this.searchOption.span || 6
        }
        // 搜索表格的栅格
        if (this.searchOption.column[i] && !this.searchOption.column[i].searchSpan && this.searchOption.column[i].search) {
          this.searchOption.column[i].searchSpan = this.searchOption.searchSpan || 6
        }
        // 去除提示tips
        if(this.searchOption.column[i] && this.searchOption.column[i].tips) {
          this.searchOption.column[i].tips = null
        }
        temp.push(JSON.parse(JSON.stringify(this.searchOption.column[i])))
      }
      if(this.searchOption.column[i].fixed) {
        this.hasFixed = true
      }
    }
    if(this.option.menuFix == 'right') {
      this.hasFixed = true
    }
    // 去除搜索条件的校验
    for(let j in temp) {
      if(temp[j].rules && temp[j].rules.length > 0) {
        for(let k in temp[j].rules) {
          if(temp[j].rules[k].required && temp[j].rules[k].required === true) {
            temp[j].rules[k].required = false
          }
        }
      }
    }
    this.searchOption.column = temp
    this.searchOption.isSearch = true
    if(JSON.stringify(this.searchFormData) == '{}') {
      this.searchFormData = JSON.parse(JSON.stringify(searchObj))
    }
    // 卡片展示
    if(this.displayType == 'card') {
      this.setCardStyle()
    }
    if(this.option.tableTitle && this.option.tableTitle.length > 0) {
      let tempColumn = []
      for(let c in this.option.column) {
        let isIn = false
        for(let t in this.option.tableTitle) {
          if(this.option.tableTitle[t].fieldKey && this.option.tableTitle[t].fieldKey.length > 0) {
            if(this.option.tableTitle[t].fieldKey.indexOf(this.option.column[c].prop) > -1) {
              let needAdd = true
              tempColumn.filter(tit => {
                if(tit.label == this.option.tableTitle[t].tableTitle) {
                  needAdd = false
                  tit.column.push(this.option.column[c])
                }
              })
              if(needAdd) {
                tempColumn.push({
                  label: this.option.tableTitle[t].tableTitle,
                  column: [this.option.column[c]]
                })
              }
              isIn = true
            }
          }
        }
        if(!isIn) {
          tempColumn.push(this.option.column[c])
        }
      }
      this.titleColumn = tempColumn
    }
    this.$emit('on-load', this.page)
  },
  mounted () {
    // 多选回显
    if(this.data && this.data.length > 0) {
      this.showSelected()
    }
    // 表格自适应高
    this.styleHeight()

    // 添加固定列对齐处理
    if(this.hasFixed && this.displayType !== 'card') {
      this.$nextTick(() => {
        this.syncFixedColumnsScroll()
        this.fixFixedColumnAlignment()
      })

      // 监听窗口大小变化
      window.addEventListener('resize', this.handleWindowResize)
    }

    if(this.displayType != 'card') {
      let _this = this
      $('.'+ this.tableKey + ' ' + ".el-table__body-wrapper").mouseenter(function(){
        _this.styleHeight('hover')
      })
      $('.'+ this.tableKey + ' ' + ".el-table__body-wrapper").mouseleave(function(){
        _this.styleHeight()
      })
    }
  },
  computed: {
    searchFormData: {
      get () {
        return this.formData
      },
      set (newVal) {
        this.tempForm = newVal
      }
    },
    rowFileList() {
      return JSON.parse(JSON.stringify(this.myPopoverRow?.[this.myPopoverItem?.prop])).sort(
        arraySort('name')
      )
    }
  },
  methods: {
    // 排序
    handleSort(row) {
      this.$emit('sort-change', row)
    },
    // 行点击
    rowClick (row, column, cell, event) {
      this.$emit('row-click', { row, column, cell, event })
    },
    // 多选
    handleSelectionChange (selection) {
      this.$emit('selection-change', selection)
    },
    // 搜索
    searchHandle (form) {
      if(this.option.page){
        this.$set(form, 'current', 1)
      }
      this.$set(this, 'searchFormData', form)
      this.$emit('search-change', form)
    },
    // 清空
    emptyHandle () {
      this.searchForm = {}
    },
    // 分页大小变化
    handleSizeChange (val) {
      this.page.pageSize = val
      this.$emit('on-load', this.page)
      this.$emit('size-change', this.page)
    },
    // 当前页改变
    handleCurrentChange (val) {
      this.page.currentPage = val
      if(this.option.page){
        // let obj = this.searchFormData
        let obj = this.tempForm
        this.$set(obj, 'current', val)
        this.$emit('search-change', obj)
      }else{
        this.$emit('on-load', this.page)
      }
      this.$emit('current-change', this.page)
    },
    // 判断是否需要超出文字提示
    tooltipShow (item, option) {
      let temp = true
      temp = (item.showOverflow == false) ? (item.showOverflow && option.showOverflow) : (item.showOverflow || option.showOverflow)
      return temp
    },
    // 关闭弹框
    handleClose () {
      this.formOpton.column.forEach((item,index)=>{
        if(['imageUpload','fileUpload'].indexOf(item.type)!=-1){
          item.fileList = []
        }
      })
      this.rowData = {}
      this.dialogVisible = false
    },
    // 表单提交
    submitHandle (form) {
      if (this.optype == 'addRow') {
        this.$emit('addRow', form)
      }
      if (this.optype == 'editRow') {
        this.$emit('editRow', form, this.daActionIndex)
      }
      this.handleClose()
    },
    // 新增
    addForm () {
      this.formOpton = this.option // JSON.parse(JSON.stringify(this.option))
      this.formOpton.submitBtnText = this.formOpton.submitBtnText || this.$langt('form.submit')
      this.title = this.formOpton.addDialogText || this.$langt('table.add')
      this.optype = 'addRow'
      this.formOpton.disabled = false
      this.formOpton.submitBtn = true
      // this.formOpton.emptyBtn = true
      let temp = []
      for(let i in this.formOpton.column) {
        if(this.formOpton.column[i].addDisabled == true) {
          this.formOpton.column[i].disabled = true
        }else{
          this.formOpton.column[i].disabled = false
        }
        if(this.formOpton.column[i].addDisplay != false) {
          temp.push(this.formOpton.column[i])
          this.formOpton.column[i].display = true
        }else{
          this.formOpton.column[i].display = false
        }
      }
      // this.formOpton.column = temp
      this.dialogVisible = true
    },
    // 查看
    viewHandle (row,index) {
      this.daActionIndex = index
      this.formOpton = JSON.parse(JSON.stringify(this.option))
      this.formOpton.submitBtnText = this.$langt('form.submit')
      this.title = this.$langt('table.view')
      this.optype = 'viewRow'
      this.formOpton.disabled = true
      this.formOpton.submitBtn = false
      this.formOpton.emptyBtn = false
      this.rowData = row
      let temp = []
      for(let i in this.formOpton.column) {
        if(this.formOpton.column[i].viewDisplay != false) {
          temp.push(this.formOpton.column[i])
          this.formOpton.column[i].display = true
        }else{
          this.formOpton.column[i].display = false
        }
      }
      this.formOpton.column = temp
      this.dialogVisible = true
    },
    editHandle (row,index) {
      this.daActionIndex = index
      this.formOpton = this.option // JSON.parse(JSON.stringify(this.option))
      this.formOpton.submitBtnText = this.formOpton.submitBtnText || this.$langt('form.submit')
      this.title = this.formOpton.editDialogText || this.$langt('table.edit')
      this.optype = 'editRow'
      this.formOpton.disabled = false
      this.formOpton.submitBtn = true
      // this.formOpton.emptyBtn = true
      this.rowData = JSON.parse(JSON.stringify(row))
      let temp = []
      for(let i in this.formOpton.column) {
        if(this.formOpton.column[i].editDisabled == true) {
          this.formOpton.column[i].disabled = true
        }
        if(this.formOpton.column[i].editDisplay != false) {
          temp.push(this.formOpton.column[i])
          this.formOpton.column[i].display = true
        }else{
          this.formOpton.column[i].display = false
        }
      }
      // this.formOpton.column = temp
      this.dialogVisible = true
    },
    delHandle (row,index) {
      this.$confirm(this.$langt('common.deleteConfirm'), this.$langt('common.tip'), {
        confirmButtonText: this.$langt('common.confirm'),
        cancelButtonText: this.$langt('common.cancel'),
        type: 'warning'
      }).then(() => {
        this.optype = 'delRow'
        this.$emit('delRow', row,index)
        }).catch(() => {});
    },
    // 多选回显
    showSelected () {
      if(this.selectable == true) {
        if(this.selectedRows && this.selectedRows.length > 0 && this.$refs[this.refs]) {
          this.selectedRows.forEach(row => {
            for(let i in this.data) {
              let keys = Object.keys(this.data[i])
              let k2 = 'id' // Object.keys(row)[0]
              if(keys.indexOf(k2) > -1) {
              }else{
                k2 = 'aliasColumnName'
              }
              if(this.data[i][k2] == row[k2]) {
                this.$refs[this.refs].toggleRowSelection(this.data[i])
              }
            }
          });
        }else {
          if(this.$refs[this.refs]) {
            if(!this.fromDialog) {
              this.$refs[this.refs].clearSelection();
            }
            // 默认全选上
            if(this.defaultAllSelect == true) {
              this.data.forEach(row => {
                this.$refs[this.refs].toggleRowSelection(row);
              })
            }
          }
        }
      }
    },
    // 清空多选
    clearSelect () {
      this.$refs[this.refs].clearSelection()
    },
    // 表格高度适应
    styleHeight (type) {
      if(this.noStyleHeight) {
        return false
      }
      let height = $('#app').height()
      if(window.self == window.top) {
        if(this.fromDialog) {
          height = (this.dialogHeight ? this.dialogHeight : document.body.clientHeight) - 45
        }else{
          if($('#outerbox') && $('#outerbox').length > 0) {
            height = $('#outerbox').height()
          }
        }
        if(!height || height < 0) {
          if($('#otherouterbox') && $('#otherouterbox').length > 0) {
            height = $('#otherouterbox').height()
          }
        }
        if($('.alone-page') && $('.alone-page').length > 0) {
          height -= 40
        }
      }
      if($('#'+this.tableKey + ' .jvs-table-titleTop') && $('#'+this.tableKey + ' .jvs-table-titleTop').length > 0) {
        height -= $('#'+this.tableKey +' .jvs-table-titleTop').height()
      }
      if($('#'+this.tableKey + ' .table-body-top') && $('#'+this.tableKey + ' .table-body-top').length > 0) {
        height -= $('#'+this.tableKey +' .table-body-top').height()
      }
      if(this.showHeader && this.displayType != 'card') {
        height -= 40 // 表头高度
      }
      if(this.option.page) {
        if($('#'+this.tableKey + ' .tablepagination') && $('#'+this.tableKey +' .tablepagination').length > 0) {
          height -= ($('#'+this.tableKey + ' .tablepagination').height() + 40)
        }
      }
      if(this.displayType == 'card') {
        $('.'+ this.tableKey + ' ' + ".jvs-table-body-slot")[0].style.maxHeight = height
        $('.'+ this.tableKey + ' ' + ".jvs-table-body-slot").height(height)
        if(this.option.menuFix) {
          $('.'+ this.tableKey + ' ' + ".jvs-table-body-slot").height(height)
        }
      }else{
        let summaryH = 0
        if(this.option.showsummary) {
          summaryH = $('#'+this.tableKey +' .el-table__footer-wrapper').height()
          if(!(this.data && this.data.length > 0)) {
            summaryH = 0
          }
        }
        let titleH = 0
        if(this.option.tableTitle && this.option.tableTitle.length > 0) {
          titleH = 40
          if(this.showGantt) {
            titleH += 40
          }
        }else{
          if(this.showGantt) {
            titleH = 40
          }
        }
        if(this.hasFixed) {
          let headerH = 40 // 表头高度
          if(this.fromOtherOpen !== true) {
            $('.'+ this.tableKey + ' ' + ".el-table__fixed-body-wrapper").height(height - 4 - summaryH - titleH)
            $('.'+ this.tableKey + ' ' + ".el-table__fixed-right").height(height + headerH)
          }
          $('.'+ this.tableKey + ' ' + ".el-table__fixed").height(height + headerH)
          if(type == 'hover') {
            if(this.fromOtherOpen !== true) {
              $('.'+ this.tableKey + ' ' + ".el-table__fixed-body-wrapper").height(height - 8 - summaryH - titleH)
              $('.'+ this.tableKey + ' ' + ".el-table__fixed-right").height(height + headerH)
            }
            $('.'+ this.tableKey + ' ' + ".el-table__fixed").height(height + headerH)
          }
        }
        if($('.'+ this.tableKey + ' ' + ".el-table__body-wrapper")[0]) {
          $('.'+ this.tableKey + ' ' + ".el-table__body-wrapper")[0].style.maxHeight = height - summaryH - titleH
        }
        $('.'+ this.tableKey + ' ' + ".el-table__body-wrapper").height(height - summaryH - titleH)
      }
    },
    doLayout(){
      let _this = this
      this.$nextTick(() => {
        _this.$refs[_this.refs].doLayout()
      })
    },
    // 添加同步固定列滚动的方法
    syncFixedColumnsScroll() {
      if (!this.hasFixed || this.displayType === 'card') {
        return
      }

      this.$nextTick(() => {
        const table = this.$refs[this.refs]
        if (!table) return

        const tableEl = table.$el
        const bodyWrapper = tableEl.querySelector('.el-table__body-wrapper')
        const fixedWrapper = tableEl.querySelector('.el-table__fixed-body-wrapper')
        const fixedRightWrapper = tableEl.querySelector('.el-table__fixed-right .el-table__fixed-body-wrapper')

        if (!bodyWrapper) return

        // 获取滚动位置并同步到固定列
        const syncScroll = () => {
          const scrollTop = bodyWrapper.scrollTop
          if (fixedWrapper) {
            fixedWrapper.scrollTop = scrollTop
          }
          if (fixedRightWrapper) {
            fixedRightWrapper.scrollTop = scrollTop
          }
        }

        // 绑定滚动事件
        bodyWrapper.addEventListener('scroll', syncScroll)
      })
    },
    // 修复固定列对齐问题
    fixFixedColumnAlignment() {
      if (!this.hasFixed || this.displayType === 'card') {
        return
      }

      this.$nextTick(() => {
        const table = this.$refs[this.refs]
        if (!table) return

        const tableEl = table.$el
        const bodyWrapper = tableEl.querySelector('.el-table__body-wrapper')
        const fixedWrapper = tableEl.querySelector('.el-table__fixed-body-wrapper')
        const fixedRightWrapper = tableEl.querySelector('.el-table__fixed-right .el-table__fixed-body-wrapper')

        if (!bodyWrapper) return

        // 同步行高
        const bodyRows = bodyWrapper.querySelectorAll('.el-table__body tr')
        const fixedRows = fixedWrapper ? fixedWrapper.querySelectorAll('.el-table__body tr') : []
        const fixedRightRows = fixedRightWrapper ? fixedRightWrapper.querySelectorAll('.el-table__body tr') : []

        // 同步行高
        for (let i = 0; i < bodyRows.length; i++) {
          const bodyRowHeight = bodyRows[i].offsetHeight

          if (fixedRows[i]) {
            fixedRows[i].style.height = bodyRowHeight + 'px'
          }

          if (fixedRightRows[i]) {
            fixedRightRows[i].style.height = bodyRowHeight + 'px'
          }
        }
      })
    },
    // 动态控制行数据显示
    styleRowItem (row, item, type) {
      let val = row[item.prop+'_1'] ? row[item.prop+'_1'] : row[item.prop]
      let color = ""
      let bgcolor = ''
      if(item.expressControl && item.expressControl.length > 0) {
        for(let i in item.expressControl) {
          if(item.expressControl[i].express) {
            let str = item.expressControl[i].express.replace(/\$\{/g,"row.")
            str = str.replace(/}/g, "")
            if(eval(str)){
              if(item.expressControl[i].text.includes('${')) {
                let ts = item.expressControl[i].text
                ts = ts.replace(/\$\{/g,"row.")
                ts = ts.replace(/}/g, "")
                if(eval(ts)) {
                  val = eval(ts)
                }
              }else{
                val = item.expressControl[i].text
              }
              color = item.expressControl[i].color
            }
          }
        }
      }
      if(item.conditionControl && item.conditionControl.length > 0) {
        let hadFix = false
        for(let i in item.conditionControl) {
          if(item.conditionControl[i].value) {
            let arr = item.conditionControl[i].value.split(',')
            if(arr.indexOf(row[item.conditionControl[i].key || item.prop]) > -1 || arr.indexOf(row[item.conditionControl[i].key || item.prop]+'') > -1) {
              hadFix = true
              if(item.conditionControl[i].text) {
                val = item.conditionControl[i].text
              }else{
                val = row[item.prop+'_1'] ? row[item.prop+'_1'] : (row[item.prop] instanceof Array ? (row[item.prop].length > 0 ? row[item.prop].join(',') : '') : row[item.prop])
              }
              if(item.conditionControl[i].color) {
                color = item.conditionControl[i].color
              }
              if(item.conditionControl[i].bgcolor) {
                bgcolor = item.conditionControl[i].bgcolor
              }
              break;
            }
          }
        }
        if(!hadFix) {
          if(item.color) {
            color = item.color
          }
          if(item.backColor) {
            bgcolor = item.backColor
          }
        }
      }else{
        if(!color && item.color) {
          color = item.color
        }
        if(!bgcolor && item.backColor) {
          bgcolor = item.backColor
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
    formatUrl (url) {
      if(this.jvsAppId && url) {
        if(url.includes('?')) {
          if(!url.includes('jvsAppId')) {
            url += `&jvsAppId=${this.jvsAppId}`
          }
        }else{
          url += `?jvsAppId=${this.jvsAppId}`
        }
      }
      return url
    },
    sortIconClick (item) {
      let index = -1
      this.sortList.filter((sit, six) => {
        if(sit.fieldKey == item.prop) {
          index = six
        }
      })
      if(index > -1) {
        let str = ''
        if(!this.sortList[index].direction) {
          str = 'DESC'
        }
        if(this.sortList[index].direction == 'DESC') {
          str = 'ASC'
        }
        if(this.sortList[index].direction == 'ASC') {
          str = ''
        }
        this.$set(this.sortList[index], 'direction', str)
      }else{
        this.sortList.push({
          fieldKey: item.prop,
          direction: 'DESC'
        })
      }
      this.$emit('sort', this.sortList)
    },
    getSortStatus (prop, status) {
      let bool = false
      this.sortList.filter(sit => {
        if(sit.fieldKey == prop) {
          if(sit.direction == status) {
            bool = true
          }
        }
      })
      return bool
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
    // 设置卡片样式
    setCardStyle () {
      let cardTitleProp = ''
      let cardSubtitleProp = ''
      let cardSubheadingProp = ''
      let cardDescribeProp = ''
      this.option.column.filter(cit => {
        if(cit.cardPosition) {
          switch(cit.cardPosition) {
            case 'title': cardTitleProp = cit.prop;break;
            case 'subtitle': cardSubtitleProp = cit.prop; this.subTitleItem = JSON.parse(JSON.stringify(cit));break;
            case 'subheading': cardSubheadingProp = cit.prop;break;
            case 'describe': cardDescribeProp = cit.prop; this.describeItem = JSON.parse(JSON.stringify(cit));break;
            default: ;break;
          }
        }
      })
      if(!cardTitleProp) {
        for(let i in this.option.column) {
          if(!this.option.column[i].type || (['image', 'imageUpload', 'file', 'fileUpload'].indexOf(this.option.column[i].type) == -1 && !this.option.column[i].multiple)) {
            this.option.column[i].cardPosition = 'title'
            cardTitleProp = this.option.column[i].prop
            break;
          }
        }
      }
      if(!cardSubtitleProp) {
        for(let i in this.option.column) {
          if([cardTitleProp].indexOf(this.option.column[i].prop) == -1 && (['image', 'imageUpload', 'radio'].indexOf(this.option.column[i].type) > -1 || (this.option.column[i].type == 'select' && !this.option.column[i].multiple))) {
            this.option.column[i].cardPosition = 'subtitle'
            cardSubtitleProp = this.option.column[i].prop
            this.subTitleItem = JSON.parse(JSON.stringify(this.option.column[i]))
            break;
          }
        }
      }
      if(!cardSubheadingProp) {
        if([cardTitleProp, cardSubtitleProp].indexOf('createBy') == -1) {
          cardSubheadingProp = 'createBy'
          let has = false
          this.option.column.filter(cit => {
            if(cit.prop == 'createBy') {
              cit.cardPosition = 'subheading'
              has = true
            }
          })
          if(!has) {
            this.option.column.push({
              label: '创建人',
              prop: 'createBy',
              cardPosition: 'subheading'
            })
          }
        }else{
          for(let i in this.option.column) {
            if([cardTitleProp, cardSubtitleProp].indexOf(this.option.column[i].prop) == -1) {
              this.option.column[i].cardPosition = 'subheading'
              cardSubheadingProp = this.option.column[i].prop
              break;
            }
          }
        }
      }
      if(!cardDescribeProp) {
        if([cardTitleProp, cardSubtitleProp, cardSubheadingProp].indexOf('createTime') == -1) {
          cardDescribeProp = 'createTime'
          let has = false
          this.option.column.filter(cit => {
            if(cit.prop == 'createTime') {
              cit.cardPosition = 'describe'
              this.describeItem = JSON.parse(JSON.stringify(cit))
              has = true
            }
          })
          if(!has) {
            this.describeItem = {
              label: '创建时间',
              prop: 'createTime',
              cardPosition: 'describe'
            }
          }
        }else{
          for(let i in this.option.column) {
            if([cardTitleProp, cardSubtitleProp, cardSubheadingProp].indexOf(this.option.column[i].prop) == -1) {
              this.option.column[i].cardPosition = 'describe'
              cardDescribeProp = this.option.column[i].prop
              this.describeItem = JSON.parse(JSON.stringify(this.option.column[i]))
              break;
            }
          }
        }
      }
      this.cardOtherItems = []
      let hasImg = false
      this.option.column.filter((cit, cix) => {
        if(!cit.cardPosition && !hasImg && cit.hide !== true) {
          this.cardOtherItems.push(cit.prop)
        }
        if(!cit.cardPosition && ['image', 'imageUpload'].indexOf(cit.type) > -1) {
          hasImg = true
        }
        if(cit.cardPosition == 'title') {
          this.titleItem = JSON.parse(JSON.stringify(cit))
        }
        if(cit.cardPosition == 'subheading') {
          this.subheadingItem = JSON.parse(JSON.stringify(cit))
        }
      })
      // console.log(this.cardOtherItems)
      // console.log(this.option.column)
      this.$forceUpdate()
    },
    showByIndex (item) {
      let bool = false
      let index = this.cardOtherItems.indexOf(item.prop)
      if(index > -1 && index < 3) {
        bool = true
      }
      return bool
    },
    getPreviewUrl (row, item, index) {
      this.previewFile(row[item.prop][index])
    },
    previewFile (row) {
      let protocolhost = this.$store.getters.kkfileUrl || ''
      if(protocolhost && row.url) {
        let view_url = `${protocolhost}/onlinePreview?forceUpdatedCache=true&officePreviewType=pdf&url=` + encodeURIComponent(Base64.encode(decodeURIComponent(row.url)))
        this.$openUrl(view_url, '_blank')
      }
    },
    getSummaries (param) {
      const { columns, data } = param;
      const sums = [];
      columns.forEach((column, index) => {
        if (index === 0) {
          sums[index] = '合计';
          return;
        }
        if(column.property) {
          let col = null
          this.option.column.filter(fit => {
            if(fit.prop == column.property) {
              col = fit
            }
          })
          if(col && col.enableStatistics) {
            const values = data.map(item => Number(item[column.property]));
            if(!values.every(value => isNaN(value))) {
              sums[index] = values.reduce((prev, curr) => {
                const value = Number(curr);
                if (!isNaN(value)) {
                  return prev + curr;
                } else {
                  return prev;
                }
              }, 0);
              if(col.precision) {
                sums[index] = Number(sums[index]).toFixed(col.precision)
              }
              if(col.thoudsandthable) {
                sums[index] = this.getThousandthNumber(sums[index], col)
              }
              if(col.unit) {
                sums[index] += ` ${col.unit}`;
              }
            } else {
              sums[index] = '';
            }
          }else{
            sums[index] = '';
          }
        }else{
          sums[index] = '';
        }
      });
      return sums;
    },
    fixedNumber (val, item) {
      let num = (isNaN(Number(val)) || ((val === '' || val === null) && ['inputNumber', 'slider'].indexOf(item.type) == -1)) ? val : (Number(val) == 0 ? 0 : (item.precision ? Number(val).toFixed(item.precision || 0) : val))
      return item.thoudsandthable ? this.getThousandthNumber(num, item) : num
    },
    getThousandthNumber(num, item) {
      let str = ''
      if(typeof num == 'number' || (typeof num == 'string' && num)) {
        str = num + ''
        str = str.replace(/\B(?=(\d{3})+(?!\d))/g, ',')
        if(str.includes('.') == false && item.precision > 0) {
          str += '.'
          for(let i=0; i < item.precision; i++) {
            str += '0'
          }
        }
        if(num == 0) {
          str = '0'
        }
      }
      return str
    },
    previewImage (list, index) {
      this.previewSrcList = []
      list.filter(li => {
        this.previewSrcList.push(li.url)
      })
      if(index > 0) {
        this.previewImageIndex = index
      }
      this.showViewer = true
    },
    closeViewer () {
      this.showViewer = false
      this.previewSrcList = []
      this.previewImageIndex = 0
    },
    myPopoverHandle (e, item, row) {
      this.$nextTick(() => {
        const popoverHeight = (row[item.prop].length * 80) + 60; // 估算高度：每个文件项约80px + padding等约60px
        const popoverWidth = 370; // 固定宽度

        let x = e.clientX;
        let y = e.clientY + 10;

        // 检查右侧是否超出视窗
        if (x + popoverWidth > window.innerWidth) {
          x = window.innerWidth - popoverWidth - 10;
        }

        // 检查底部是否超出视窗
        if (y + popoverHeight > window.innerHeight) {
          // 如果底部空间不足，向上弹出
          y = e.clientY - popoverHeight - 10;
          // 确保不会超出顶部边界
          if (y < 10) {
            y = 10;
          }
        }

        this.myPopoverPos = { x, y };
        this.myPopoverItem = JSON.parse(JSON.stringify(item));
        this.myPopoverRow = JSON.parse(JSON.stringify(row));
        // console.log('myPopoverRow', this.myPopoverRow)
        this.myPopoverShow = true;
      });
    },
    stopHandle () {
      return false
    },
    closeMyPopover () {
      this.myPopoverPos = {
        x: 0,
        y: 0
      }
      this.myPopoverItem = null
      this.myPopoverRow = null
      this.myPopoverShow = false
    },
    arrIncludeArr (arr1, list, type) {
      let arr2 = JSON.parse(JSON.stringify(list))
      let six = arr2.indexOf('sort')
      if(six > -1) {
        arr2.splice(six, 1)
      }
      let bool = false
      let num = 0
      for(let i in arr1) {
        if(arr2.indexOf(arr1[i]) > -1) {
          bool = true
          num++
        }
      }
      if(type == 'one') {
        if(bool && num === 1 && arr2.length == 1) {
          bool = true
        }else{
          bool = false
        }
      }
      return (type == 'all' ? num : bool)
    },
    moreToolHandle (e, item, row) {
      this.moreToolPos = {
        x: e.clientX,
        y: e.clientY
      }
      this.moreToolItem = JSON.parse(JSON.stringify(item))
      this.moreToolRow = JSON.parse(JSON.stringify(row))
      this.moreToolShow = true
    },
    moreToolClose () {
      this.moreToolPos = {
        x: 0,
        y: 0
      }
      this.moreToolItem = null
      this.moreToolRow = null
      this.moreToolShow = false
    },
    loadHandle (tree, treeNode, resolve) {
      this.$emit('load', tree, treeNode, resolve)
    }
  },
  watch: {
    isClearSelect: {
      handler(newVal, oldVal) {
        if(newVal != 0) {
          this.clearSelect()
        }
      }
    },
    data: {
      handler(newVal, oldVal) {
        this.styleHeight()
        if(this.option.menuFix) {
          this.doLayout()
        }
        this.$nextTick(() => {
          if(newVal && newVal.length > 0) {
            this.showSelected()
          }
        })
      }
    },
    'option.searchLoading': {
      handler(newVal, oldVal) {
        this.$set(this.searchOption, 'submitLoading', (newVal || false))
      }
    },
    $route (to, from) {
      if(to.fullPath != from.fullPath) {
        this.styleHeight()
      }
    },
    sortsList: {
      handler(newVal, oldVal) {
        this.$set(this, 'sortList', newVal)
      }
    }
  },
  directives: {
    clickOutside: {
      bind(el, binding, vnode) {
        if (!vnode.context._clickOutside) {
          // 为Vue实例创建事件监听器
          vnode.context._clickOutside = (event) => {
            // 判断点击是否发生在el之外
            if (!(el === event.target)) {
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
};
</script>
<style lang="scss" scoped>
.my-popover-box{
  position: fixed;
  left: 0;
  top: 0;
  transform: translateX(-50%);
}
.more-tool-i{
  margin-left: 4px;
}
.table-column-svg-button-icon{
  width: 24px;
  height: 24px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  svg.icon{
    width: 16px;
    height: 16px;
    cursor: pointer;
    fill: #6F7588;
  }
  &:hover{
    background: #E4E7EA;
  }
}
.column-tool-list{
  width: 144px;
  padding: 0 8px;
  box-sizing: border-box;
  .column-tool-list-item{
    display: flex;
    align-items: center;
    width: 128px;
    height: 32px;
    padding: 0 8px;
    border-radius: 4px;
    box-sizing: border-box;
    cursor: pointer;
    font-family: Microsoft YaHei-Regular, Microsoft YaHei;
    font-weight: 400;
    font-size: 14px;
    color: #363B4C;
    svg.icon{
      width: 16px;
      height: 16px;
      fill: #6F7588;
      margin-right: 8px;
    }
    &:hover{
      background: #F5F6F7;
    }
  }
}
.more-tool-box{
  min-width: 144px;
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
.table-column-span{
  border-radius: 4px;
  display: inline-block;
  >div{
    width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: pre;
  }
}
</style>
<style lang="scss">
.el-table {
  .el-table__body-wrapper {
    overflow-y: auto;
    .el-table__empty-block {
      box-sizing: border-box;
      min-height: 300px;
      display: flex;
      justify-content: center;
      align-items: center;
      border: 1px solid #ebebeb;
    }
  }
  .el-table__body-wrapper:hover::-webkit-scrollbar{
    height: 8px;
  }
  th{
    font-size: 14px;
    font-weight: 600;
    color: #222222;
  }
}
.search-form {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  .el-form-item {
    display: flex;
    align-items: center;
    margin-left: 0;
    margin-bottom: 0;
  }
  .form-item-btn{
    width: auto;
  }
}
.tablepagination {
  padding: 20px 0;
  background-color: #fff;
  overflow: hidden;
  .el-pagination {
    float: right;
    padding: 0;
    margin-right: 20px;
  }
}
.table-top {
  width: 100%;
  overflow: hidden;
  .table-top-left{
    float: left;
  }
  .table-top-right {
    float: right;
    display: flex;
  }
}
.table-title{
  margin: 0;
  text-align: center;
}
// 去除斑马纹
.el-table--striped .el-table__body tr.el-table__row--striped td{
  background-color: #fff;
}
.el-table--enable-row-hover .el-table__body tr:hover>td{
  background-color: #F5F7FA;
}
.jvs-table{
  .pageheader-top{
    display: none;
  }
  .el-table{
    // margin-top: 0!important;
    .cell{
      position: relative;
      .file-text-a{
        cursor: pointer;
        color: #0000EE;
      }
      .down-file-icon{
        position: absolute;
        right: 0;
        top: 5px;
        cursor: pointer;
      }
    }
    .gantt-column-father,
    .gantt-column {
      .cell {
        padding-left: 0;
        padding-right: 0;
      }
    }
    // .headerclass .gantt-column{
    //   width:24px;
    //   position:relative;
    //   .cell{
    //     width:100%;
    //     position:absolute;
    //     left:0;
    //     top:0;
    //   }
    // }
  }
  .table-body-box{
    background-color: #fff;
    .info-icon{
      margin-left: 10px;
      cursor: pointer;
    }
    .sort-column-item-icon{
      position: relative;
      cursor: pointer;
      display: none;
      i{
        color: #909399;
      }
      i:nth-of-type(1) {
        position: absolute;
        top: 0;
      }
      i:nth-of-type(2) {
        position: absolute;
        top: 7px;
      }
    }
    .sort-column-item-icon.asc{
      i:nth-of-type(1) {
        color: #5b8bff;
      }
    }
    .sort-column-item-icon.desc{
      i:nth-of-type(2) {
        color: #5b8bff;
      }
    }
    .table-body-slot-box{
      width: 100%;
      box-sizing: border-box;
      height: 100%;
      overflow: hidden;
      overflow-y: auto;
      .table-body-slot-box-item{
        float: left;
        border: 1px solid #EEEFF0;
        background-color: #fff;
        width: calc(25% - 12px);
        margin-left: 16px;
        margin-bottom: 16px;
        box-sizing: border-box;
        border-radius: 6px;
        overflow: hidden;
        height: 217px;
        cursor: pointer;
        position: relative;
        .card-top-head{
          padding: 12px 16px;
          box-sizing: border-box;
          border-bottom: 1px solid #EEEFF0;
          border-radius: 6px;
          .table-body-slot-box-item-row-title{
            margin-bottom: 0;
            .title{
              margin: 0;
              padding: 0;
              font-size: 16px;
              font-family: Source Han Sans-Bold, Source Han Sans;
              font-weight: 700;
              color: #363B4C;
              height: 23px;
              line-height: 23px;
              width: 100%;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: pre;
            }
            .sub-title-item{
              max-width: calc(50% - 30px);
              border-radius: 4px;
              box-sizing: border-box;
              .span{
                width: 100%;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: pre;
                border-radius: 4px;
                box-sizing: border-box;
              }
            }
            .img{
              width: 20px;
              height: 20px;
            }
          }
          .table-body-slot-box-item-row-subhead-desc{
            margin-top: 4px;
            font-size: 12px;
            line-height: 12px;
            font-family: Source Han Sans-Regular, Source Han Sans;
            font-weight: 400;
            color: #6F7588;
            display: flex;
            align-items: center;
            box-sizing: border-box;
            display: flex;
            align-items: center;
            .divider-bar{
              display: block;
              width: 1px;
              height: 15px;
              background-color: #6F7588;
              margin: 1px 5px;
            }
            .table-body-slot-box-item-row-subhead-desc-item{
              max-width: calc(50% - 6px);
              border-radius: 4px;
              overflow: hidden;
              div{
                width: 100%;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: pre;
              }
            }
          }
        }
        .table-body-slot-box-row-others{
          margin: 16px;
          .info{
            .con{
              flex: 1;
              overflow: hidden;
              >span{
                display: inline-block;
                max-width: 100%;
                box-sizing: border-box;
                border-radius: 4px;
                >div{
                  white-space: nowrap;
                  overflow: hidden;
                  text-overflow: ellipsis;
                }
              }
            }
          }
        }
        .table-body-slot-box-item-row{
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 8px;
          font-size: 14px;
          font-family: Source Han Sans-Regular, Source Han Sans;
          font-weight: 400;
          color: #6F7588;
          line-height: 20px;
          .img{
            display: block;
            width: 80px;
            height: 80px;
            border-radius: 8px;
            overflow: hidden;
          }
        }
        .table-body-slot-box-item-row:nth-last-of-type(1){
          margin-bottom: 2px;
        }
        .info{
          display: flex;
          align-content: center;
          flex: 1;
          overflow: hidden;
          .label{
            margin-right: 10px;
            word-break: keep-all;
          }
        }
        .card-bottom{
          position: absolute;
          width: calc(100% - 4px);
          left: 2px;
          bottom: 2px;
          height: 36px;
          background: #F5F6F7;
          border-radius: 0px 0px 6px 6px;
          overflow: hidden;
          div{
            width: 100%;
            display: flex;
            align-items: center;
            align-items: center;
            .el-button--text{
              flex: 1;
              height: 36px;
              box-sizing: border-box;
              margin-left: 2px;
              margin-right: 2px;
              position: relative;
              border-radius: 0;
              span{
                font-size: 14px;
                font-family: Source Han Sans-Regular, Source Han Sans;
                font-weight: 400;
                color: #363B4C;
              }
            }
            .el-button--text:hover{
              background-color: #1E6FFF;
              span{
                color: #fff;
              }
            }
            .el-button--text::before{
              content: "";
              width: 1px;
              height: 20px;
              background: #EEEFF0;
              position: absolute;
              left: -3px;
              top: 8px;
              z-index: 0;
            }
            .el-button--text:nth-of-type(1){
              margin-left: 0;
            }
            .el-button--text:nth-of-type(1)::before{
              display: none;
            }
            .el-button--text:nth-last-of-type(1){
              margin-right: 0;
            }
          }
        }
      }
      .table-body-slot-box-item:nth-of-type(4n+1){
        margin-left: 0;
      }
    }
    .jvs-table-body-slot-loading{
      background-image: url('../../styles/loading.gif');
      background-repeat: no-repeat;
      background-position: center;
    }
  }
  .jvs-table-top{
    box-shadow: none;
  }
  .jvs-table-hideTop{
    .table-top{
      display: none;
    }
  }
  .jvs-table-notitle{
    .pageheader-top{
      display: none;
    }
  }
  .jvs-table-hideTop.jvs-table-notitle{
    .el-card__body{
      display: none;
    }
  }
  .el-loading-mask{
    background-image: url('../../styles/loading.gif');
    background-repeat: no-repeat;
    background-position: center;
    //background-size: 300px 240px;
    .el-loading-spinner{
      .circular{
        display: none;
      }
    }
  }
  .back-color-text{
    background-clip: text!important;
    -webkit-background-clip :text!important;
    -webkit-text-fill-color: transparent;
  }
}
// 文字提示
.el-tooltip__popper{
  max-width: 70%;
}
.jvs-table-nocolumn{
  width: 100%;
  height: 100vh;
  position: relative;
  .jvs-table-top, .table-title, .table-body-box, .tablepagination{
    display: none;
  }
}
.jvs-table-nocolumn::before{
  content: "";
  display: block;
  width: 457px;
  height: 180px;
  background-image: url(/jvs-ui/static/img/emptyImage.ca3665f2.png);
  background-size: 260px 123px;
  background-repeat: no-repeat;
  background-position: center;
  position: absolute;
  left: calc(50% - 228px);
  top: calc(50% - 180px);
}
.jvs-table-nocolumn::after{
  //content: "抱歉，没有找到相关搜索内容！";
  position: absolute;
  left: calc(50% - 80px);
  top: calc(50%);
}
.img-file-list{
  padding: 30px 35px;
  overflow-y: auto;
  .if-item{
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 15px;
    margin-top: 15px;
    box-sizing: border-box;
    width: 300px;
    flex-wrap: nowrap;
    min-height: 40px;
    background: #ebeef5;
    border-radius: 5px;
    border: 1px solid #ebeef5;
    overflow: hidden;
    img{
      display: block;
      width: 50px;
      height: 50px;
      margin-right: 10px;
    }
    i{
      cursor: pointer;
      font-size: 20px;
    }
    span{
      flex: 1;
      margin-right: 10px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: pre;
      font-size: 16px;
    }
  }
  .if-item:nth-of-type(1){
    margin-top: 0;
  }
  .if-item:hover{
    border-color: #409EFF;
    span{
      color: #409EFF;
    }
  }
}
.card-item-more-tool-list{
  width: 100px;
  div{
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    padding: 0 10px;
    .el-button{
      width: 100%;
      cursor: pointer;
    }
    .el-button:hover{
      transition: 0.3s;
      background-color: #eff2f7;
    }
    .el-button+.el-button{
      margin-left: 0;
    }
  }
}
</style>
