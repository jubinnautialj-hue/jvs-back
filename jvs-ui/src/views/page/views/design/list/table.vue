<template>
  <div class="table-info-form">
    <div class="table-info-form-left">
      <div :class="{'table-info-form-left-top': true, 'all-height': infoData.displayType == 'card'}">
        <div style="display: flex;align-items: center;justify-content: space-between;">
          <h4>全部字段</h4>
          <div class="add-field-button-row">
            <jvs-button type="primary" size="mini" @click="multipleAddHandle">批量添加</jvs-button>
            <jvs-button type="primary" size="mini" icon="el-icon-plus" class="primary" @click="addRowHandle">新增一列</jvs-button>
          </div>
        </div>
        <div ref="fieldScroll" class="all-field-list">
          <div
            v-for="(row, rix) in tableData"
            :key="'design-table-column-item-'+rix"
            :class="{'all-field-list-item': true, 'target': (moveTarget && (row.aliasColumnName == moveTarget.aliasColumnName))}"
            :draggable="true"
            @dragstart.stop="moveRowStart(row, rix)"
            @dragenter="moveRowing(row)"
            @dragend="moveRowEnd"
          >
            <div :class="{'main-field': true, 'active': (rix == index && childIndex < 0)}" @click.stop="seniorSet(row, rix, 'senior')">
              <div class="choose-icon">
                <svg class="icon" aria-hidden="true">
                  <use xlink:href="#jvs-ui-icon-tuodong1"></use>
                </svg>
              </div>
              <div class="field-select">
                <div class="el-select el-select--mini">
                  <div class="el-input el-input--mini">
                    <div :class="{'el-input__inner': true, 'placeholder': !row.showChinese}">{{row.showChinese || '请在右侧选择字段'}}</div>
                  </div>
                </div>
              </div>
              <div class="svg-icon" @click="row.show=!row.show;$forceUpdate();emitGetColumn();">
                <svg class="icon" aria-hidden="true">
                  <use :xlink:href="`#${row.show ? 'jvs-ui-icon-yanjing1' : 'jvs-ui-icon-yanjing'}`"></use>
                </svg>
              </div>
              <div class="svg-icon" @click="deleteRow(row, rix)">
                <svg class="icon" aria-hidden="true">
                  <use xlink:href="#jvs-ui-icon-shanchuyonghu"></use>
                </svg>
              </div>
              <el-popover placement="top" trigger="hover" content="关联模型" popper-class="custom-right-tool-poper">
                <div slot="reference" class="svg-icon" @click.stop="modelDisplayHandle(row, rix)">
                  <svg class="icon" aria-hidden="true">
                    <use xlink:href="#jvs-ui-icon-guanlianmoxing"></use>
                  </svg>
                </div>
              </el-popover>
            </div>
            <el-collapse-transition v-if="row.advancedSettings && row.advancedSettings.modelDisplay && row.advancedSettings.modelDisplay.linkageFieldKeys && row.advancedSettings.modelDisplay.linkageFieldKeys.length > 0">
              <div v-show="rix == index" class="children-fields">
                <div
                  v-for="(cow, cix) in row.advancedSettings.modelDisplay.linkageFieldKeys"
                  :key="'design-table-model-display-column-item-'+cix"
                  :class="{'all-field-list-item': true, 'target': (moveChildTarget && (cow.aliasProp == moveChildTarget.aliasProp))}"
                  :draggable="true"
                  @dragstart.stop="moveChildRowStart(cow, cix)"
                  @dragenter.stop="moveChildRowing(cow)"
                  @dragend.stop="moveChildRowEnd"
                >
                <div :class="{'main-field': true, 'active': cix == childIndex}" @click.stop="seniorSet(cow, cix, 'child')">
                  <div class="choose-icon">
                    <svg class="icon" aria-hidden="true">
                      <use xlink:href="#jvs-ui-icon-tuodong1"></use>
                    </svg>
                  </div>
                  <div class="field-select">
                    <div class="el-select el-select--mini">
                      <div class="el-input el-input--mini">
                        <div class="el-input__inner">{{cow.label}}</div>
                      </div>
                    </div>
                  </div>
                  <!-- <div class="svg-icon" @click="cow.show=!cow.show;$forceUpdate();">
                    <svg class="icon" aria-hidden="true">
                      <use :xlink:href="`#${row.show ? 'jvs-ui-icon-yanjing1' : 'jvs-ui-icon-yanjing'}`"></use>
                    </svg>
                  </div> -->
                  <div class="svg-icon" @click="deleteOneOfList(cow, cix, row.advancedSettings.modelDisplay.linkageFieldKeys)">
                    <svg class="icon" aria-hidden="true">
                      <use xlink:href="#jvs-ui-icon-shanchuyonghu"></use>
                    </svg>
                  </div>
                </div>
                </div>
              </div>
            </el-collapse-transition>
          </div>
        </div>
      </div>
    </div>
    <div class="table-info-form-right">
      <div class="right-title">
        <span>字段设置</span>
        <span v-if="dialogType != 'child' ? (index > -1 && rowData.showChinese) : (childIndex > -1 && rowData.label)">-{{dialogType != 'child' ? rowData.showChinese : rowData.label}}</span>
      </div>
      <div class="setting-title">
        <svg class="icon" aria-hidden="true" :style="`cursor: pointer;${showBasic ? '' : 'transform: rotate(-90deg);'}`" @click="showBasic=!showBasic;">
          <use xlink:href="#icon-jvs-xiala"></use>
        </svg>
        <span>基础设置</span>
        <div class="border-line"></div>
      </div>
      <el-collapse-transition v-if="index > -1">
        <div v-show="showBasic" class="row-basic-setting" :key="index">
          <div v-for="(cit, cix) in basicOption.column"
            :key="'basic-column-item-'+cix"
            class="row-basic-setting-column"
            v-if="displayHandle(cit)"
          >
            <div class="label">
              <span>{{cit.label}}</span>
              <el-tooltip v-if="cit.explainContent" class="item" effect="light" :content="cit.explainContent" placement="top">
                <div class="quest-icon-info">
                  <span>?</span>
                </div>
              </el-tooltip>
            </div>
            <div :class="['content', cit.prop]">
              <div v-if="cit.slot" style="display: flex;align-items: center;">
                <!-- 显示中文名 -->
                <el-select v-if="cit.prop == 'showChinese'" v-model="rowData.showChinese" clearable allow-create filterable placeholder="请选择或输入中文名" size="mini" @change="chineseChange(rowData, index)">
                  <el-option
                    v-for="(item, key) in fieldsData"
                    :key="'chinese-item-'+'-'+key"
                    :label="item.fieldName"
                    :value="item.fieldName">
                  </el-option>
                </el-select>
                <el-tooltip v-if="rowData && rowData.designJson && ['talbeForm', 'flowNode', 'dynamicForm', 'timeline', 'pageTable', 'tab', 'box', 'divider', 'p'].indexOf(rowData.designJson.type) > -1"
                  class="item" effect="light"
                  content="表格，动态流程、动态表单、时间线、列表页、json 、选项卡、描述框、分割线、小标题，不支持列表数据显示" placement="top">
                  <div class="quest-icon-info">
                    <span>?</span>
                  </div>
                </el-tooltip>
                <!-- 小工具 -->
                <div v-if="cit.prop == 'tools'" style="display: flex;align-items: center;">
                  <el-checkbox-group v-model="rowData.advancedSettings.tools" @change="emitGetColumn">
                    <el-checkbox v-for="dic in cit.dicData" :key="dic.value" :label="dic.value" v-if="['sort'].indexOf(dic.value) > -1 ? (dialogType != 'child') : true ">{{dic.label}}</el-checkbox>
                  </el-checkbox-group>
                  <el-checkbox v-if="displayHandle({prop: 'fixed'})" v-model="rowData.fixed">固定列</el-checkbox>
                  <el-checkbox v-if="rowData.supportQuery" v-model="rowData.enableQuery" @change="enableQueryChange({row: rowData, index: index})">查询</el-checkbox>
                  <!-- 查询支持范围 -->
                  <el-checkbox v-if="rowData && (
                    (rowData.designJson && (
                      (rowData.designJson.type == 'datePicker' && ['date', 'datetime', 'month', 'year'].indexOf(rowData.designJson.datetype) > -1) ||
                      (['radio', 'checkbox', 'select'].indexOf(rowData.designJson.type) > -1))
                    ) ||
                    ['createTime', 'updateTime'].indexOf(rowData.aliasColumnName) > -1) && rowData.enableQuery"
                    v-model="rowData.enableQueryRange" style="margin-left: -18px;">范围</el-checkbox>
                  <el-checkbox v-if="displayHandle({prop: 'enableStatistics'})" v-model="rowData.enableStatistics">统计</el-checkbox>
                  <el-checkbox v-if="displayHandle({prop: 'enableRetrieval'})" v-model="rowData.enableRetrieval" @change="enableRetrievalChange({index: index, row: rowData})">快速检索
                    <el-tooltip class="item" effect="light" content="作为查询条件，与布局相关" placement="top">
                      <div class="quest-icon-info">
                        <span>?</span>
                      </div>
                    </el-tooltip>
                  </el-checkbox>
                  <el-button v-if="rowData.enableRetrieval && (infoData.displayType == 'leftTree' || (rowData.designJson.datatype == 'dataModel' && rowData.designJson.formId))" size="mini" type="text" style="margin-left: -18px;" @click="setLeftBtn(rowData, index)">配置</el-button>
                </div>
              </div>
              <el-switch v-else-if="cit.type == 'switch'" v-model="rowData[cit.prop]" @change="emitGetColumn"></el-switch>
              <el-input v-else v-model="rowData[cit.prop]" :disabled="cit.disabled" @change="emitGetColumn"></el-input>
            </div>
          </div>
        </div>
      </el-collapse-transition>
      <div class="setting-title">
        <svg class="icon" aria-hidden="true" :style="`cursor: pointer;${showSenior ? '' : 'transform: rotate(-90deg);'}`" @click="showSenior=!showSenior;">
          <use xlink:href="#icon-jvs-xiala"></use>
        </svg>
        <span>字段样式</span>
        <div class="border-line"></div>
      </div>
      <el-collapse-transition v-if="index > -1">
        <div v-show="showSenior" :class="{'row-senior-setting': true, 'hide-table': !rowData.advancedSettings.conditionControlEnable}">
          <div v-if="showSenior && seniorVisible" class="jvs-form">
            <div v-if="infoData.displayType != 'card'" class="el-form-item">
              <div class="el-form-item__label">
                <span>显示宽度</span>
              </div>
              <div class="el-form-item__content">
                <div class="custom-slider">
                  <el-slider v-model="rowData.advancedSettings.showWidth" :max="500" @change="seniorChange(rowData.advancedSettings, {prop: 'showWidth'})"></el-slider>
                  <div class="text">
                    <el-input-number v-if="rowData.advancedSettings.showWidth > 0" v-model="rowData.advancedSettings.showWidth" :controls="false" :max="500" @change="seniorChange(rowData.advancedSettings, {prop: 'showWidth'})"></el-input-number>
                    <div v-else class="text-auto">AUTO</div>
                    <span v-if="rowData.advancedSettings.showWidth > 0" class="text-px">px</span>
                  </div>
                </div>
              </div>
            </div>
            <div v-if="rowData.enableQuery" class="el-form-item">
              <div class="el-form-item__label">
                <span>查询宽度</span>
              </div>
              <div class="el-form-item__content">
                <div class="custom-slider">
                  <el-slider v-model="rowData.advancedSettings.searchSpan" :min="6" :max="24" @change="seniorChange(rowData.advancedSettings, {prop: 'searchSpan'})"></el-slider>
                  <div class="text">
                    <el-input-number v-model="rowData.advancedSettings.searchSpan" :controls="false" :min="6" :max="24" @change="seniorChange(rowData.advancedSettings, {prop: 'searchSpan'})"></el-input-number>
                  </div>
                </div>
              </div>
            </div>
            <div v-if="infoData.displayType == 'card' && dialogType != 'child'" class="el-form-item">
              <div class="el-form-item__label">
                <span>卡片样式</span>
              </div>
              <div class="el-form-item__content">
                <el-checkbox-group v-model="checkPosList" @change="checkPosChange" style="width: calc(100% + 30px);">
                  <el-checkbox label="title" style="margin-right: 0px;">标题</el-checkbox>
                  <el-checkbox label="subtitle" style="margin-right: 0px;">副标题</el-checkbox>
                  <el-checkbox label="subheading" style="margin-right: 0px;">小标题</el-checkbox>
                  <el-checkbox label="describe">描述</el-checkbox>
                </el-checkbox-group>
              </div>
            </div>
            <div class="el-form-item">
              <div class="el-form-item__label">
                <span>字段颜色</span>
              </div>
              <div class="el-form-item__content">
                <div class="column-color-picker">
                  <el-popover
                    placement="bottom"
                    width="280"
                    trigger="click"
                  >
                    <div class="color-card-list">
                      <div class="color-card">
                        <div v-for="(citem, cix) in colorList1" :key="'out-item-1-'+cix" class="color-card-list-item" :style="`color: ${citem.color};background: ${citem.back};`" @click="setColorHandle(citem)">{{citem.text || '效果展示'}}</div>
                      </div>
                      <div class="color-card">
                        <div v-for="(citem, cix) in colorList2" :key="'out-item-2-'+cix" class="color-card-list-item" :style="`color: ${citem.color};background: ${citem.back};`" @click="setColorHandle(citem)">{{citem.text || '效果展示'}}</div>
                      </div>
                      <div class="color-card">
                        <div v-for="(citem, cix) in colorList3" :key="'out-item-3-'+cix" class="color-card-list-item" :style="`color: ${citem.color};background: ${citem.back};`" @click="setColorHandle(citem)">{{citem.text || '效果展示'}}</div>
                      </div>
                    </div>
                    <div slot="reference" class="text-info">
                      <div class="box" :style="`${rowData.advancedSettings.backColor ? ('background:'+rowData.advancedSettings.backColor+';') : 'background:#fff;'};`">
                        <span
                          :class="{'text': true, 'back-color-text': (rowData.advancedSettings.textcolor && !rowData.advancedSettings.textcolor.startsWith('#'))}"
                          :style="(rowData.advancedSettings.textcolor && (rowData.advancedSettings.textcolor.startsWith('#') ? `color: ${rowData.advancedSettings.textcolor}` : `background:${rowData.advancedSettings.textcolor};`))"
                        >文本颜色</span>
                      </div>
                      <svg class="icon" aria-hidden="true" :style="`width: 16px;height: 16px;fill: #6F7588;cursor: pointer;${showSenior ? '' : 'transform: rotate(-90deg);'}`">
                        <use xlink:href="#icon-jvs-xiala"></use>
                      </svg>
                    </div>
                  </el-popover>
                  <div class="color-box">
                    <span class="color-label">背景颜色</span>
                    <jvs-colorpicker :modelValue="rowData.advancedSettings.backColor ? rowData.advancedSettings.backColor : undefined"  prop="backColor" :form="rowData.advancedSettings" :allowEmpty="true" @update:modelValue="colorChangeHandle"></jvs-colorpicker>
                  </div>
                  <div class="color-box">
                    <span class="color-label">文本颜色</span>
                    <jvs-colorpicker :modelValue="rowData.advancedSettings.textcolor ? rowData.advancedSettings.textcolor : undefined"  prop="textcolor" :form="rowData.advancedSettings" :allowEmpty="true" @update:modelValue="colorChangeHandle"></jvs-colorpicker>
                  </div>
                </div>
              </div>
            </div>
            <div class="el-form-item">
              <div class="el-form-item__label">
                <span>动态样式</span>
                <el-tooltip class="item" effect="light" content="根据数据值动态设置显示颜色背景及文字" placement="top">
                  <div class="quest-icon-info">
                    <span>?</span>
                  </div>
                </el-tooltip>
              </div>
              <div class="el-form-item__content custom-style-table">
                <div class="jvs-form-item">
                  <div>
                    <el-switch v-model="rowData.advancedSettings.conditionControlEnable"></el-switch>
                  </div>
                  <div v-if="rowData.advancedSettings.conditionControlEnable" class="table-form form-column-tableForm">
                    <tableForm
                      :item="customStyleOption"
                      :option="customStyleTableOption"
                      :forms="rowData.advancedSettings"
                      :data="rowData.advancedSettings.conditionControl"
                      @setTable="setTableHandle"
                    >
                      <template slot="menuBtn" slot-scope="scope">
                        <span class="delete-icon-button" @click="deleteStyleRow(scope.row, scope.index)">
                          <span class="border-line"></span>
                        </span>
                      </template>
                      <template slot="colorTableFormColumn" slot-scope="scope">
                        <div class="column-color-picker">
                          <el-popover
                            placement="bottom"
                            width="280"
                            trigger="click"
                          >
                            <div class="color-card-list">
                              <div class="color-card">
                                <div v-for="(citem, cix) in colorList1" :key="'table-column-item-1-'+cix" class="color-card-list-item" :style="`color: ${citem.color};background: ${citem.back};`" @click="setControlColor(citem, scope.row)">{{citem.text || '效果展示'}}</div>
                              </div>
                              <div class="color-card">
                                <div v-for="(citem, cix) in colorList2" :key="'table-column-item-2-'+cix" class="color-card-list-item" :style="`color: ${citem.color};background: ${citem.back};`" @click="setControlColor(citem, scope.row)">{{citem.text || '效果展示'}}</div>
                              </div>
                              <div class="color-card">
                                <div v-for="(citem, cix) in colorList3" :key="'table-column-item-3-'+cix" class="color-card-list-item" :style="`color: ${citem.color};background: ${citem.back};`" @click="setControlColor(citem, scope.row)">{{citem.text || '效果展示'}}</div>
                              </div>
                            </div>
                            <div slot="reference" class="text-info">
                              <div class="box" :style="`${scope.row.bgcolor ? ('background:'+scope.row.bgcolor+';') : 'background:#fff;'};`">
                                <span
                                  :class="{'text': true, 'back-color-text': (scope.row.color && !scope.row.color.startsWith('#'))}"
                                  :style="(scope.row.color && (scope.row.color.startsWith('#') ? `color: ${scope.row.color}` : `background:${scope.row.color};`))"
                                >文本颜色</span>
                              </div>
                              <svg class="icon" aria-hidden="true" :style="`width: 16px;height: 16px;fill: #6F7588;cursor: pointer;${showSenior ? '' : 'transform: rotate(-90deg);'}`">
                                <use xlink:href="#icon-jvs-xiala"></use>
                              </svg>
                            </div>
                          </el-popover>
                          <div class="color-box">
                            <span class="color-label">背景色</span>
                            <jvs-colorpicker :modelValue="scope.row.bgcolor ? scope.row.bgcolor : undefined"  prop="bgcolor" :form="scope.row" :allowEmpty="true" @update:modelValue="customColorChangeHandle"></jvs-colorpicker>
                          </div>
                          <div class="color-box">
                            <span class="color-label">文本色</span>
                            <jvs-colorpicker :modelValue="scope.row.color ? scope.row.color : undefined"  prop="color" :form="scope.row" :allowEmpty="true" @update:modelValue="customColorChangeHandle"></jvs-colorpicker>
                          </div>
                        </div>
                      </template>
                    </tableForm>
                    <div class="bottom-add-button">
                      <div class="button" @click="addConditionControl">
                        <div class="icon">
                          <svg aria-hidden="true">
                            <use xlink:href="#jvs-ui-icon-xinjian"></use>
                          </svg>
                        </div>
                        <span>新增一行</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-collapse-transition>
      <div v-if="dialogType != 'child'" class="setting-title">
        <svg class="icon" aria-hidden="true" :style="`cursor: pointer;${showExtend ? '' : 'transform: rotate(-90deg);'}`" @click="showExtend=!showExtend;">
          <use xlink:href="#icon-jvs-xiala"></use>
        </svg>
        <span>扩展配置</span>
        <div class="border-line"></div>
      </div>
      <el-collapse-transition v-if="dialogType != 'child' && index > -1">
        <div v-show="showExtend" class="row-senior-setting">
          <jvs-form v-if="showExtend && seniorVisible" :option="seniorSetOption" :formData="rowData.advancedSettings" @formChange="seniorChange">
            <template slot="formulaForm">
              <div style="display: flex;align-items: center;">
                <el-button @click="openButtonFormula" class="plain-button">
                  <span>配置公式</span>
                  <el-popconfirm v-if="rowData.advancedSettings.formula" title="删除后需要重新创建，是否继续？" @confirm="clearAttr('formula')">
                    <i slot="reference" class="el-icon-delete" style="margin-left: 5px;color: #FF194C;" @click.stop="noHandle"></i>
                  </el-popconfirm>
                </el-button>
              </div>
            </template>
            <template slot="openFormIdForm">
              <div style="display: flex;align-items: center;">
                <el-button @click="openViewForm" class="plain-button">
                  <span>打开表单</span>
                  <el-popconfirm v-if="rowData.advancedSettings.openFormId" title="删除后需要重新创建，是否继续？" @confirm="clearAttr('openFormId')">
                    <i slot="reference" class="el-icon-delete" style="margin-left: 5px;color: #FF194C;" @click.stop="noHandle"></i>
                  </el-popconfirm>
                </el-button>
              </div>
            </template>
          </jvs-form>
        </div>
      </el-collapse-transition>
    </div>
    <!-- 批量添加字段 -->
    <el-dialog
      class="custom-header-dialog"
      title="批量添加"
      :visible.sync="multipleAdd"
      width="30%"
      :close-on-click-modal="false"
      :before-close="multipleAddClose">
      <div v-if="multipleAdd">
        <div style="padding: 10px 20px 0 20px;">
          <div style="padding-bottom: 10px;color: #909399;">
            <span>请输入中文名，每一行为一个字段，默认去除空格</span>
          </div>
          <el-input
            type="textarea"
            :autosize="{ minRows: 5}"
            placeholder="请输入内容"
            v-model="multipleAddContent">
          </el-input>
        </div>
        <el-row style="margin-top: 20px;display:flex;align-items:center;justify-content:flex-end;height: 60px;border-top: 1px solid #EEEFF0;box-sizing: border-box;">
          <el-button size="mini" @click="multipleAddClose" style="margin-right: 16px;">取消</el-button>
          <el-button size="mini" type="primary" @click="multipleAddSubmit" style="margin-left: 0;margin-right: 16px;">确定</el-button>
        </el-row>
      </div>
    </el-dialog>
    <!-- 左树按钮配置 -->
    <el-dialog
      class="custom-header-dialog"
      width="50%"
      title="快速检索配置"
      :visible.sync="leftTreeBtnVisible"
      :close-on-click-modal="false"
      :before-close="leftTreeBtnClose">
      <div v-if="leftTreeBtnVisible" style="padding: 20px;">
        <div v-if="infoData.displayType == 'leftTree'">
          <jvs-form :formData="retrievalSetItem.retrievalOption" :option="retrievalFormOption" @change="emitGetColumn"></jvs-form>
        </div>
        <div v-if="leftTreeDesignJson.formId" :style="infoData.displayType == 'leftTree' ? 'margin-top: 10px;' : ''">
          <div style="color: #959595;line-height: 28px;font-size: 14px;margin-bottom: 4px;">筛选条件</div>
          <div class="data-filter-div">
            <el-row v-for="(item, index) in retrievalSetItem.retrievalOption.filterList" :key="'data-filter-item-'+index" class="data-filter-item">
              <el-select v-model="item.fieldKey" placeholder="请选择字段" size="mini" @change="emitGetColumn">
                <el-option
                  v-for="(it, ix) in dataModelAllFieldList"
                  v-show="needShow(retrievalSetItem.retrievalOption.filterList, 'fieldKey', it.fieldKey)"
                  :key="'connect-show-'+it.fieldKey+'-'+ix"
                  :label="it.fieldName"
                  :value="it.fieldKey">
                </el-option>
              </el-select>
              <el-select v-model="item.enabledQueryTypes" placeholder="请选择匹配规则" size="mini" @change="emitGetColumn">
                <el-option label="等于" value="eq"></el-option>
                <el-option label="模糊匹配" value="like"></el-option>
                <el-option label="不等于" value="ne"></el-option>
                <el-option label="等于空" value="isNull"></el-option>
              </el-select>
              <el-input v-if="item.enabledQueryTypes != 'isNull'" v-model="item.value" size="mini" style="width:auto;" @change="emitGetColumn"></el-input>
              <i class="el-icon-delete" style="cursor: pointer;" @click="deleteDataFilter(index, retrievalSetItem.retrievalOption.filterList)"></i>
            </el-row>
            <p>
              <el-button size="mini" @click="addDataFilter">添加</el-button>
            </p>
          </div>
        </div>
        <div v-if="infoData.displayType == 'leftTree' && leftTreeDesignJson.formId" style="margin-top: 10px;">
          <div style="color: #959595;line-height: 28px;font-size: 14px;margin-bottom: 4px;">按钮配置</div>
          <leftTreeButtonInfo
            :data="leftTreeButtonList"
            :infoData="infoData"
            :designId="designId"
            :dataModelId="leftTreeDesignJson.formId"
            @handleSave="saveLeftTreeBtn"
            @permissionHandle="leftTreePermissionHandle"
          />
        </div>
      </div>
    </el-dialog>
    <!-- 关联模型显示 -->
    <el-dialog
      class="custom-header-dialog"
      width="50%"
      title="新增外部模型"
      :visible.sync="modelDiaplayVisible"
      :close-on-click-modal="false"
      :before-close="modelDisplayClose"
      @opened="modelDiaplayOpend">
      <div v-if="modelDiaplayVisible && JSON.stringify(modelDisplayRow) != '{}'" class="model-display-body">
        <div class="el-form-item">
          <div class="el-form-item__label">关联模型</div>
          <el-select
            v-model="modelDisplayRow.advancedSettings.modelDisplay.dataLinkageModelId" placeholder="请选择关联模型" size="mini"
            @change="changeLinkModelPropList(modelDisplayRow.advancedSettings.modelDisplay.dataLinkageModelId)"
            filterable
            clearable
            class="my-select"
          >
            <el-option
              v-for="item in dataModelList"
              :key="item.id"
              :label="item.name"
              :value="item.id">
            </el-option>
          </el-select>
        </div>
        <div class="el-form-item" v-if="modelDisplayRow.advancedSettings.modelDisplay.dataLinkageModelId">
          <div class="el-form-item__label">显示字段</div>
          <el-select ref="linkageFieldKeysRef" v-model="linkageFieldKeys" multiple placeholder="请选择字段" size="mini" class="my-select" @change="linkageFieldKeysChange">
            <el-option
              v-for="(it, ix) in dataLinkModelAllFieldList"
              :key="'connect-show-'+it.fieldKey+'-'+ix"
              :label="it.fieldName"
              :value="it.fieldKey">
              <span style="float: left">{{ it.fieldName }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">{{ it.fieldKey }}</span>
            </el-option>
          </el-select>
        </div>
        <div v-if="modelDisplayRow.advancedSettings.modelDisplay.dataLinkageModelId" class="el-form-item">
          <div class="el-form-item__label">筛选条件</div>
          <div class="datalink-filter-list-box">
            <div class="datalink-filter-list-box-body">
              <el-row class="datalink-filter-list-box-body-header">
                <div>关联模型字段</div>
                <div>匹配规则</div>
                <div>类型</div>
                <div>匹配值</div>
                <span style="width: 16px;margin-left: 16px;"></span>
              </el-row>
              <el-row v-for="(item, index) in modelDisplayRow.advancedSettings.modelDisplay.dataLinkageList" :key="'data-filter-item-'+index" class="datalink-filter-list-box-body-body">
                <div class="item">
                  <el-select v-model="item.fieldKey" placeholder="请选择关联模型字段" size="mini" @change="fieldKeyChange(item, index)">
                    <el-option
                      v-for="(it, ix) in dataLinkModelAllFieldList"
                      :key="'connect-show-'+it.fieldKey+'-condi-'+ix"
                      :label="it.fieldName"
                      :value="it.fieldKey">
                    </el-option>
                  </el-select>
                </div>
                <div class="item">
                  <el-select v-model="item.enabledQueryTypes" placeholder="请选择匹配规则" size="mini">
                    <el-option v-for="(qit, qix) in getEnabledQueryTypes(item)" :key="'enabled-query-types-'+index+'-'+qix" :label="qit.label" :value="qit.value"></el-option>
                  </el-select>
                </div>
                <div class="item">
                  <el-select v-model="item.prop" placeholder="请选择" size="mini">
                    <el-option label="字段" value="field"></el-option>
                    <el-option label="公式" value="formula"></el-option>
                  </el-select>
                </div>
                <div v-if="item.prop == 'field'" class="item">
                  <el-select v-model="item.value" placeholder="请选择当前列表字段" size="mini">
                    <el-option  v-for='(item,index) in fieldsData' :key="item.fieldKey + '-data-filter-' + index" :label="item.fieldName" :value="item.fieldKey">
                      <span style="float: left">{{ item.fieldName }}</span>
                      <span style="float: right; color: #8492a6; font-size: 13px">{{ item.fieldKey}}</span>
                    </el-option>
                  </el-select>
                </div>
                <div v-if="item.prop == 'formula'" class="item">
                  <svg aria-hidden="true" class="add-formula-svg" style="position: unset;" @click="openConditionFormula(item)">
                    <use :xlink:href="`#${(item.formula && item.formulaContent && item.formulaContent.trim().length > 0) ? 'icon-jvs-a-zu10923' : 'icon-jvs-rongqi1'}`"></use>
                  </svg>
                </div>
                <span class="delete-icon-button" @click="deleteDataFilter(index, modelDisplayRow.advancedSettings.modelDisplay.dataLinkageList)">
                  <span class="border-line"></span>
                </span>
              </el-row>
              <div v-if="modelDisplayRow.advancedSettings.modelDisplay.dataLinkageModelId" class="bottom-button">
                <div class="button" @click="addDataLinkage">
                  <div class="icon">
                    <svg aria-hidden="true">
                      <use xlink:href="#jvs-ui-icon-xinjian"></use>
                    </svg>
                  </div>
                  <span>新增一行</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="el-form-item" v-if="modelDisplayRow.advancedSettings.modelDisplay.dataLinkageModelId">
          <div class="el-form-item__label">关联显示方式</div>
          <div style="flex: 1;">
            <el-radio-group v-model="modelDisplayRow.advancedSettings.modelDisplay.showModelType">
              <el-radio label="oneToMany">点击内容弹框展示</el-radio>
              <el-radio label="oneToOne">列表页内扩展字段展示</el-radio>
            </el-radio-group>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import pinyin from 'js-pinyin'
import { columnTypeList } from '../../../const/const'
import dataSourceForm from '../../../plugin/datasource'
import leftTreeButtonInfo from './leftTreeButton'
import { getAllModel, getLinkModelAllFields, delDesign } from '@/components/template/api'
import { getButtonFormId } from '../../../api/newDesign'
import formatKey from '@/views/page/plugin/assembly/format'
import { deleteExec } from '@/components/basic-container/formula/api'
import tableForm from '@/components/basic-assembly/tableForm'
export default {
  name: 'table-info',
  components: { tableForm, dataSourceForm, leftTreeButtonInfo },
  props: {
    data: {
      type: Array,
      default: () => {
        return []
      }
    },
    infoData: {
      type: Object
    },
    designData: {
      type: Object
    },
    regKey: {
      type: String,
    },
    tableOption: {
      type: Array
    },
    fieldsData: {
      type: Array
    },
    needEmpty: {
      type: Number
    },
    roleList: {
      type: Array,
      default: () => {
        return []
      }
    },
    designId: {
      type: String
    },
    dataModelId: {
      type: String
    },
  },
  computed: {
    tableData: {
      get () {
        this.data.filter(row => {
          this.fieldsData.filter(fit => {
            if(fit.fieldKey == row.aliasColumnName) {
              row.designJson = fit.designJson
            }
          })
        })
        return this.data
      },
      set(){}
    },
    // 排序
    sortTypeDicData : {
      get () {
        let temp = []
        for(let i in this.$store.getters.labelValue.sortType) {
          temp.push({
            label: this.$store.getters.labelValue.sortType[i],
            value: i
          })
        }
        return temp
      },
      set(){}
    },
    // 查询
    queryTypeDicData : {
      get () {
        let temp = []
        return temp
      },
      set(){}
    },
    // 高级设置--显示类型
    viewTypeDicData : {
      get () {
        let temp = []
        for(let i in this.$store.getters.labelValue.fieldType) {
          temp.push({
            label: this.$store.getters.labelValue.fieldType[i],
            value: i
          })
        }
        return temp
      },
      set(){}
    },
    // 表字段树形结构
    tableOptionTree () {
      let temp = []
      for(let i in this.tableOption) {
        let obj = {}
        obj.label = this.tableOption[i].tableName
        obj.value = this.tableOption[i].tableName
        obj.columnComment = this.tableOption[i].info
        if(this.tableOption[i].autoTableFields && this.tableOption[i].autoTableFields.length > 0) {
          obj.children = []
          for(let j in this.tableOption[i].autoTableFields) {
            obj.children.push({
              label: this.tableOption[i].autoTableFields[j].columnName,
              value: this.tableOption[i].autoTableFields[j].columnName,
              aliasColumnName: this.tableOption[i].autoTableFields[j].aliasColumnName,
              columnComment: this.tableOption[i].autoTableFields[j].columnComment
            })
          }
        }
        temp.push(obj)
      }
      return temp
    },
    customStyleTableOption: {
      get () {
        let obj = {
          addBtn: false,
          viewBtn: false,
          delBtn: false,
          editBtn: false,
          page: false,
          border: this.customStyleOption.border,
          align: this.customStyleOption.align || 'left',
          menuAlign: this.customStyleOption.menuAlign || 'left',
          cancal: false,
          showOverflow: true,
          hideTop: this.customStyleOption.hideTop || false,
          tableColumn: this.customStyleOption.tableColumn
        }
        return obj
      },
      set () {}
    }
  },
  data () {
    return {
      basicOption: {
        column: [
          {
            label: '显示中文名',
            prop: 'showChinese',
            slot: true,
          },
          {
            label: '显示字段名',
            prop: 'aliasColumnName',
            explainContent: '字段名不能包含空格和特殊符号',
          },
          {
            label: '其他',
            prop: 'tools',
            type: 'checkbox',
            slot: true,
            dicData: [
              {label: '排序', value: 'sort'},
              {label: '复制', value: 'copy'}
            ]
          },
        ]
      },
      advancedSettings: {}, // 高级设置
      customStyleOption: {
        prop: 'conditionControl',
        type: 'tableForm',
        editable: true,
        border: true,
        editable: true,
        addBtn: false,
        delBtn: true,
        align: 'center',
        menuAlign: 'center',
        iconBtn: true,
        showOverflow: false,
        tableColumn: [
          {
            label: '字段',
            prop: 'key',
            showOverflow: false,
          },
          {
            label: '值',
            prop: 'value',
            showOverflow: false,
          },
          {
            label: '字段颜色',
            prop: 'color',
            type: 'colorPicker',
            clearable: true,
            formSlot: true,
            width: '328px'
          },
          {
            label: '显示文字',
            prop: 'text',
            showOverflow: false,
          }
        ],
      },
      seniorSetOption: {
        btnHide: true,
        formAlign: 'left',
        labelWidth: '94px',
        column: [
          {
            label: '内容动态转换',
            prop: 'formula',
            display: true,
            formSlot: true,
          },
          {
            label: '内容触发表单',
            prop: 'openFormId',
            display: true,
            formSlot: true,
          },
        ]
      },
      modelDiaplayVisible: false,
      modelDisplayRow: {},
      modelDisplayIndex: -1,
      index: -1, // 当前行index
      rowData: {}, // 当前行数据
      dicDataList: [],
      dialogType: '', // 高级 senior
      multipleAdd: false,
      multipleAddContent: '',
      leftTreeBtnVisible: false,
      checkPosList: [], // 卡片样式位置
      retrievalItem: null,
      leftTreeButtonList: [],
      retrievalSetItem: null,
      retrievalFormOption: {
        btnHide: true,
        formAlign: 'left',
        labelWidth: 'auto',
        column: [
          {
            label: '顶部显示',
            prop: 'allLabel',
            span: 24
          },
          {
            label: '展开/收起全部',
            prop: 'expandAll',
            type: 'switch',
            span: 6
          },
          {
            label: '可否隐藏',
            prop: 'closeEnable',
            type: 'switch',
            span: 6
          },
          {
            label: '查询所有子集',
            prop: 'allChildren',
            type: 'switch',
            span: 6
          }
        ]
      },
      dataModelAllFieldList: [], // 数据模型下所有字段
      dataModelList: [], // 模型列表
      dataLinkModelAllFieldList: [], // 关联模型下的字段
      linkageFieldKeys: [], // 关联字段key集合
      showBasic: true,
      showSenior: true,
      showExtend: true,
      seniorVisible: false,
      moveSource: null,
      moveTarget: null,
      fieldScrollTop: 0,
      moveChildSource: null,
      moveChildTarget: null,
      childIndex: -1,
      colorList1: [
        {color: '#FFFFFF', back: '#1E6FFF'},
        {color: '#FFFFFF', back: '#36B452'},
        {color: '#FFFFFF', back: '#FF9736'},
        {color: '#FFFFFF', back: '#FF194C'},
        {color: '#FFFFFF', back: '#6F7588'},
        {color: '#FFFFFF', back: '#2EE5CA'},
        {color: '#FFFFFF', back: '#CC7429'},
        {color: '#FFFFFF', back: '#33BFFF'},
        {color: '#FFFFFF', back: '#E62E7B'},
        {color: '#FFFFFF', back: '#8833FF'},
        {color: '#FFFFFF', back: '#FFCB33'},
        {color: '#FFFFFF', back: '#FF6633'},
      ],
      colorList2: [
        {color: '#1E6FFF', back: '#E4EDFF'},
        {color: '#36B452', back: '#E6F6EA'},
        {color: '#FF9736', back: '#FFF2E6'},
        {color: '#FF194C', back: '#FFE3E9'},
        {color: '#6F7588', back: '#EEEFF0'},
        {color: '#49D6D2', back: '#E9FAF9'},
        {color: '#CC7429', back: '#F8EEE5'},
        {color: '#33BFFF', back: '#E6F7FF'},
        {color: '#E62E7B', back: '#FCE6EF'},
        {color: '#8833FF', back: '#F0E6FF'},
        {color: '#FFCB33', back: '#FFF8E6'},
        {color: '#FF6633', back: '#FFECE6'},
      ],
      colorList3: [
        {color: '#363B4C', back: '#FFFFFF', text: '默认颜色'},
        {color: '#363B4C', back: '#DEECDC'},
        {color: '#363B4C', back: '#D6E5EE'},
        {color: '#363B4C', back: '#F6DFCB'},
        {color: '#363B4C', back: '#9283FF'},
        {color: '#363B4C', back: '#F2B5C6'},
        {color: '#363B4C', back: '#F9D054'},
        {color: '#363B4C', back: '#F2E1E9'}
      ],
    };
  },
  methods: {
    // 中文名 change
    chineseChange(e, index) {
      const regularExpression = /^[A-Za-z]+[A-Za-z0-9_]*$/
      const reg = new RegExp(regularExpression)
      if(reg.test(e.showChinese)) {
        this.$set(e, 'aliasColumnName', e.showChinese)
      }else{
        this.$set(e, 'aliasColumnName', '')
      }
      for(let i in this.fieldsData) {
        if(this.fieldsData[i].fieldName == e.showChinese) {
          this.$set(e, 'aliasColumnName', this.fieldsData[i].fieldKey)
          this.$set(e, 'enabledQueryTypes', this.fieldsData[i].enabledQueryTypes || [])
          this.$set(e, 'componentType', this.fieldsData[i].fieldType ? this.fieldsData[i].fieldType : 'input')
          if(this.fieldsData[i].designJson) {
            this.$set(e, 'designJson', this.fieldsData[i].designJson)
          }
        }
      }
      this.noticeGetColumn({row: e})
    },
    // 新增一行
    addRowHandle () {
      this.tableData.push({ supportShow: true, dbJavaType: 'field_text',show:true })
      this.noticeGetColumn()
      this.setValueOfOther(this.tableData[this.tableData.length-1], this.tableData.length-1)
      this.seniorSet(this.tableData[this.tableData.length-1], this.tableData.length-1, 'senior')
      this.$nextTick(() => {
        this.$nextTick(() => {
          this.$refs.fieldScroll.scrollTo({
            top: this.$refs.fieldScroll.scrollHeight,
            behavior: "smooth",
          })
        })
      })
    },
    // 删除一行
    deleteRow (row, index) {
      this.tableData.splice(index, 1)
      this.noticeGetColumn()
      this.rowData = {}
      this.index = -1
      this.$nextTick(() => {
        if(index > 0) {
          this.seniorSet(this.tableData[Number(index)-1], Number(index) - 1, 'senior')
        }
      })
    },
    // 中文名、 name、info、type 改变通知获取column
    noticeGetColumn (data) {
      if(data) {
        let row = data.row
        let index = -1
        index = data.index
        if(index == -1 || (!index && index !== 0)) {
          index = this.getIndexOfTable('showChinese', row.showChinese, this.tableData)
        }
        if(row.showChinese && row.showChinese != '') {
          let name = pinyin.getFullChars(row.showChinese)
          name = name[0].toLowerCase() + name.slice(1, name.length)
          if(row.disabled === false) {
            if(index > -1) {
              this.$set(this.tableData[index], 'columnName', name)
              this.$set(this.tableData[index], 'columnComment', row.showChinese)
              this.$forceUpdate()
            }
          }
          if(!row.aliasColumnName && index > -1) {
            this.$set(this.tableData[index], 'aliasColumnName', name)
          }
        }
        this.setValueOfOther(row, index)
        if(index > -1) {
          this.fieldsData.filter(fi => {
            if(fi.fieldKey == row.aliasColumnName) {
              this.$set(this.tableData[index], 'showChinese', fi.fieldName)
            }
          })
        }
      }
      this.emitGetColumn()
    },
    // 显示字段名 改变  修改查询配置
    aliasColumnNameChange (data) {
      let index = data.index
      this.$emit('getReg', data.regErr)
      if(data.row) {
        let row = data.row
        if(row.queryConditionConfig) {
          row.queryConditionConfig.prop = row.aliasColumnName ? row.aliasColumnName : row.columnName
          this.$set(this.tableData[index], 'queryConditionConfig', row.queryConditionConfig)
        }
        this.setValueOfOther(this.tableData[index], index)
      }
    },
    // 高级设置
    seniorSet (row, index, dialogType, optype) {
      if(dialogType == 'child') {
        if(index == this.childIndex && !optype) {
          return false
        }
        this.childIndex = index
      }else{
        if(index == this.index && this.childIndex == -1 && !optype) {
          return false
        }
        this.index = index
        this.childIndex = -1
      }
      this.seniorVisible = false
      this.rowData = {}
      this.checkPosList = []
      this.dialogType = dialogType
      this.seniorSetOption.column.filter(item => {
        if(item.prop == 'openFormId') {
          if(['image', 'imageUpload', 'file', 'fileUpload', 'link'].indexOf(row.componentType) > -1) {
            item.display = false
          }else{
            item.display = true
          }
        }
      })
      if(!row.advancedSettings) {
        this.$set(row, 'advancedSettings', {})
      }
      if(row.advancedSettings) {
        if(!row.advancedSettings.tools) {
          this.$set(row.advancedSettings, 'tools', [])
        }
        if(row.advancedSettings.conditionControlEnable !== false && row.advancedSettings.conditionControl && row.advancedSettings.conditionControl.length > 0) {
          this.$set(row.advancedSettings, 'conditionControlEnable', true)
        }
        if(!row.advancedSettings.searchSpan) {
          this.$set(row.advancedSettings, 'searchSpan', 6)
        }
      }
      if(row.showWidth && !row.advancedSettings.showWidth) {
        if(row.showWidth == 0) {
          this.$set(row, 'showWidth', '')
        }
        this.$set(row.advancedSettings, 'showWidth', row.showWidth)
      }
      if(row.advancedSettings.cardPosition) {
        this.checkPosList = [row.advancedSettings.cardPosition]
      }
      this.seniorSetOption.column.filter(scol => {
        if(scol.prop == 'conditionControl') {
          scol.tableColumn.filter(stol => {
            if(stol.prop == 'key') {
              stol.defaultValue = row.aliasColumnName
            }
          })
        }
      })
      this.rowData = row
      this.showSenior = true
      this.$nextTick(() => {
        this.seniorVisible = true
        this.fieldScrollTop = this.$refs.fieldScroll.scrollTop
      })
    },
    // 通过字段类型获取对应项
    getItemOfColumnTypeByValue (value, attr) {
      for(let i in columnTypeList) {
        if(columnTypeList[i].value == value || columnTypeList[i].value.toUpperCase() == value.toUpperCase()) {
          return columnTypeList[i][attr]
        }
      }
      return false
    },
    getIndexOfTable(attr, val, list, attr2, val2) {
      let index = -1
      for(let i in list) {
        if(attr2 && val2) {
          if(list[i][attr] == val && list[i][attr2] == val2) {
            index = i
          }
        }else{
          if(list[i][attr] == val) {
            index = i
          }
        }
      }
      return index
    },
    // 添加一个枚举数据
    addEnumItem () {
      if(!this.rowData.advancedSettings.dictionary) {
        this.rowData.advancedSettings.dictionary = []
      }
      this.rowData.advancedSettings.dictionary.push({
        label: '',
        value: ''
      })
      this.$forceUpdate()
    },
    deleteEnumItem (index, row) {
      this.rowData.advancedSettings.dictionary.splice(index, 1)
      this.$forceUpdate()
    },
    // 是否查询
    enableQueryChange (data) {
      if(data) {
        let row = data.row
        let index = -1
        index = this.getIndexOfTable('columnName', row.columnName, this.tableData, 'tableName', row.tableName)
        if(index > -1 && row.dbJavaType) {
          let tarr = Object.keys(this.$store.getters.labelValue.querySpec[row.dbJavaType])[0]
          if(row.associatedFields) {
            tarr = 'query_eq'
          }
          this.$set(this.tableData[index], 'queryType', tarr)
          this.$forceUpdate()
        }
      }
      this.emitGetColumn()
    },
    // 作为检索字段
    enableRetrievalChange (data) {
      let index = data.index
      let row = JSON.parse(JSON.stringify(data.row))
      for(let i in this.tableData) {
        this.$set(this.tableData[i], 'enableRetrieval', false)
      }
      this.$set(this.tableData[index], 'enableRetrieval', row.enableRetrieval || false)
      if(this.retrievalItem && this.retrievalItem.aliasColumnName != row.aliasColumnName) {
        this.leftTreeButtonList = []
      }
      if(row.enableRetrieval) {
        this.retrievalItem = JSON.parse(JSON.stringify(this.tableData[index]))
      }else{
        this.retrievalItem = null
      }
      this.emitGetColumn()
      this.$forceUpdate()
    },
    // 是否排序
    enableSortChange (data) {
      if(data) {
        let row = data.row
        let index = this.getIndexOfTable('columnName', row.columnName, this.tableData, 'tableName', row.tableName,)
        if(index > -1) {
          this.$set(this.tableData[index], 'sortType', this.sortTypeDicData[0].value)
          this.$forceUpdate()
        }
      }
      this.emitGetColumn()
    },
    // 切换数据表,获取字段列表
    getTableColumnHandle (row, index, type) {
      let item = null
      if(type !== 'init' && type !== 'treeChange') {
        this.$set(this.tableData[index], 'columnName', '')
      }
      for(let i in this.tableOption) {
        if(this.tableOption[i].tableName == row.tableName) {
          item = this.tableOption[i]
        }
      }
      if(item) {
        this.$set(this.tableData[index], 'columnOption', item.autoTableFields)
        this.$set(this.tableData[index], 'tableId', item.id)
      }
      this.$forceUpdate()
    },
    // 类型切换改变其他属性
    setValueOfOther (row, index, onlyChange) {
      if(row.dbJavaType) {
        let fitem = this.$store.getters.labelValue.fieldTypeMore[row.dbJavaType]
        let arr = ['supportShow', 'supportSort', 'supportSettings', 'supportStatistics', 'supportQuery']
        if(fitem) {
          for(let i in arr) {
            if(arr[i] == 'supportQuery') {
              if(onlyChange !== 'onlyChange') {
                if(!fitem.supportQueryType || fitem.supportQueryType.length == 0) {
                  this.$set(this.tableData[index], 'enableQuery', false)
                }else{
                  if(fitem.supportQueryType.indexOf(row.queryType) == -1) {
                    this.$set(this.tableData[index], 'queryType', "")
                  }
                }
                this.$set(this.tableData[index], 'supportQuery', (fitem.supportQueryType && fitem.supportQueryType.length > 0)? true : false)
              }
            }else{
              this.$set(this.tableData[index], arr[i], fitem[arr[i]])
            }
          }
          this.$forceUpdate()
        }
      }
      if(row.enableRetrieval) {
        this.retrievalItem = JSON.parse(JSON.stringify(this.tableData[index]))
      }
    },
    // 批量添加字段
    multipleAddHandle () {
      this.multipleAdd = true
    },
    multipleAddClose () {
      this.multipleAdd = false
      this.multipleAddContent = ''
    },
    multipleAddSubmit () {
      let list = this.multipleAddContent.split('\n')
      list = [...new Set(list)]
      for(let i in list) {
        let str = list[i].replace(/\s*/g, '')
        if(str) {
          let name = pinyin.getFullChars(str)
          name = name[0].toLowerCase() + name.slice(1, name.length)
          let row = {
            showChinese: str,
            columnName: name,
            aliasColumnName: name,
            dbJavaType: 'field_text',
            show: true
          }
          if(row.dbJavaType) {
            let fitem = this.$store.getters.labelValue.fieldTypeMore[row.dbJavaType]
            let arr = ['supportShow', 'supportSort', 'supportSettings', 'supportStatistics', 'supportQuery']
            if(fitem) {
              for(let i in arr) {
                if(arr[i] == 'supportQuery') {
                  if(!fitem.supportQueryType || fitem.supportQueryType.length == 0) {
                      this.$set(row, 'enableQuery', false)
                    }else{
                      if(fitem.supportQueryType.indexOf(row.queryType) == -1) {
                        this.$set(row, 'queryType', "")
                      }
                    }
                    this.$set(row, 'supportQuery', (fitem.supportQueryType && fitem.supportQueryType.length > 0)? true : false)
                }else{
                  this.$set(row, arr[i], fitem[arr[i]])
                }
              }
            }
          }
          this.tableData.push(row)
        }
      }
      this.multipleAddClose()
      this.emitGetColumn()
      if(this.index == -1 && this.tableData && this.tableData.length > 0) {
        this.seniorSet(this.tableData[0], 0, 'senior')
      }
      this.$nextTick(() => {
        this.$nextTick(() => {
          this.$refs.fieldScroll.scrollTo({
            top: this.$refs.fieldScroll.scrollHeight,
            behavior: "smooth",
          })
        })
      })
    },
    // 左树权限
    leftTreePermissionHandle () {
      this.$emit('getLeftButtons', this.leftTreeButtonList)
    },
    setLeftBtn (row, index) {
      if(this.infoData.displayType == 'leftTree') {
        if(!row.retrievalOption) {
          row.retrievalOption = {
            allLabel: '全部',
            expandAll: false,
            closeEnable: false,
            allChildren: false,
            filterList: []
          }
        }
      }else{
        if(!row.retrievalOption) {
          this.$set(row, 'retrievalOption', { filterList: [] })
        }
      }
      if(row.retrievalOption && !row.retrievalOption.filterList) {
        this.$set(row.retrievalFormOption, 'filterList', [])
      }
      this.retrievalSetItem = row
      this.leftTreeDesignJson = JSON.parse(JSON.stringify(row.designJson))
      if(row.designJson.datatype == 'dataModel' && row.designJson.formId) {
        getLinkModelAllFields(this.infoData.jvsAppId, this.leftTreeDesignJson.formId, this.infoData.id).then(res => {
          if(res.data && res.data.code == 0) {
            this.$set(this, 'dataModelAllFieldList', res.data.data)
            this.leftTreeBtnVisible = true
          }
        })
      }else{
        this.leftTreeBtnVisible = true
      }
    },
    // 左树按钮配置关闭
    leftTreeBtnClose () {
      this.leftTreeDesignJson = null
      this.leftTreeBtnVisible = false
    },
    saveLeftTreeBtn () {
      this.$emit('saveLeftTreeButton', this.leftTreeButtonList)
    },
    checkPosChange (val) {
      if(val.length > 0) {
        let pos = val[val.length-1]
        for(let i in this.tableData) {
          if(this.tableData[i].advancedSettings && this.tableData[i].advancedSettings.cardPosition && this.tableData[i].advancedSettings.cardPosition == pos) {
            this.$set(this.tableData[i].advancedSettings, 'cardPosition', '')
          }
        }
        this.$set(this.rowData.advancedSettings, 'cardPosition', pos)
        this.$set(this, 'checkPosList', [pos])
      }else{
        this.$set(this.rowData.advancedSettings, 'cardPosition', '')
      }
      this.emitGetColumn()
    },
    needShow (vallist, attr, value) {
      let bool = true
      let val = []
      if(vallist && vallist.length > 0) {
        vallist.filter(v => {
          if(v[attr]) {
            val.push(v[attr])
          }
        })
      }
      if(val.indexOf(value) > -1) {
        bool = false
      }
      return bool
    },
    addDataFilter () {
      this.retrievalSetItem.retrievalOption.filterList.push({})
      this.emitGetColumn()
      this.$forceUpdate()
    },
    deleteDataFilter (index, list) {
      list.splice(index, 1)
      this.emitGetColumn()
      this.$forceUpdate()
    },
    getQuerySpec (type) {
      let temp = {}
      if(this.$store.getters.labelValue && this.$store.getters.labelValue.querySpec) {
        temp = this.$store.getters.labelValue.querySpec[type]
      }
      return temp
    },
    openViewForm () {
      if(this.rowData.advancedSettings.openFormId) {
        let str = ''
        str = location.origin + (`/page-design-ui/#/form?jvsAppId=${this.infoData.jvsAppId}&id=`+ this.rowData.advancedSettings.openFormId + (this.dataModelId ? `&dataModelId=${this.dataModelId}` : '') + `&isDetail=false&isAddForm=false`)
        this.$openUrl(str, '_blank')
      }else{
        getButtonFormId(this.infoData.jvsAppId, this.dataModelId, this.designId, this.rowData.showChinese).then(res => {
          if (res.data && res.data.code == 0) {
            this.rowData.advancedSettings.openFormId = res.data.data
            this.$forceUpdate()
            let str = ''
            str = location.origin + (`/page-design-ui/#/form?jvsAppId=${this.infoData.jvsAppId}&id=`+res.data.data + (this.dataModelId ? `&dataModelId=${this.dataModelId}` : '') + `&isDetail=false&isAddForm=false`)
            this.$openUrl(str, '_blank')
          }
        })
      }
    },
    openButtonFormula () {
      this.$setFormula({
        title: '公式设置',
        hasModal: true,
        label: this.rowData.showChinese,
        execId: this.rowData.advancedSettings.formula ? this.rowData.advancedSettings.formula : '',
        apiPrefix: 'jvs-design',
        useCase: 'pageButtonDisplay',
        props: {
          jvsAppId: this.infoData.jvsAppId,
          designId: this.designId,
          businessId: this.rowData.aliasColumnName
        },
        afterSave: (dialog, data) => {
          if(data && data.id) {
            this.$set(this.rowData.advancedSettings, 'formula', data.id)
            this.$set(this.rowData.advancedSettings, 'combiningFieldFormulaContent', data.body)
          }
          dialog.handleClose()
        }
      })
    },
    // 数据联动
    getDataModelList () {
      getAllModel(this.infoData.jvsAppId).then(res => {
        if(res.data && res.data.code == 0) {
          this.dataModelList = res.data.data
        }
      })
    },
    changeLinkModelPropList (dataLinkageModelId, oprate) {
      if(dataLinkageModelId) {
        getLinkModelAllFields(this.infoData.jvsAppId, dataLinkageModelId, this.designId).then(res => {
          if(res.data && res.data.code == 0) {
            this.$set(this, 'dataLinkModelAllFieldList', res.data.data)
          }
        })
      }else{
        this.dataLinkModelAllFieldList = []
      }
      if(oprate != 'create') {
        this.$set(this.modelDisplayRow.advancedSettings.modelDisplay, 'dataLinkageList', [])
        this.$set(this.modelDisplayRow.advancedSettings.modelDisplay, 'linkageFieldKeys', [])
        this.linkageFieldKeys = []
      }
      this.$forceUpdate()
    },
    addDataLinkage () {
      this.modelDisplayRow.advancedSettings.modelDisplay.dataLinkageList.push({prop: 'field'})
      this.$forceUpdate()
    },
    deleteDataLinkage (index, arr) {
      arr = arr.splice(index, 1)
      this.$forceUpdate()
    },
    linkageFieldKeysChange () {
      if(this.linkageFieldKeys && this.linkageFieldKeys.length > 0) {
        let temp = []
        for(let i in this.linkageFieldKeys) {
          let index = -1
          this.modelDisplayRow.advancedSettings.modelDisplay.linkageFieldKeys.filter((fit, fix) => {
            if(fit.prop == this.linkageFieldKeys[i]) {
              index = fix
              temp.push(fit)
            }
          })
          if(index == -1) {
            this.dataLinkModelAllFieldList.filter(df => {
              if(df.fieldKey == this.linkageFieldKeys[i]) {
                temp.push({
                  label: df.fieldName,
                  prop: df.fieldKey,
                  type: df.fieldType,
                  aliasProp: df.fieldKey + formatKey.numberToString(new Date().getTime())
                })
              }
            })
          }
        }
        this.$set(this.modelDisplayRow.advancedSettings.modelDisplay, 'linkageFieldKeys', temp)
      }else{
        this.$set(this.modelDisplayRow.advancedSettings.modelDisplay, 'linkageFieldKeys', [])
      }
      if(this.modelDisplayIndex == this.index) {
        if(this.modelDisplayRow.advancedSettings.modelDisplay.linkageFieldKeys.length > 0) {
          if(this.childIndex > -1) {
            if(this.childIndex > this.modelDisplayRow.advancedSettings.modelDisplay.linkageFieldKeys.length-1) {
              this.childIndex-= 1
            }
            this.seniorSet(this.modelDisplayRow.advancedSettings.modelDisplay.linkageFieldKeys[this.childIndex], this.childIndex, 'child', 'fresh')
          }
        }else{
          if(this.childIndex > -1) {
            this.childIndex = -1
            this.seniorSet(this.tableData[this.index], this.index, 'senior', 'fresh')
          }
        }
      }
      this.$forceUpdate()
    },
    getEnabledQueryTypes (item) {
      let arr = []
      if(item.fieldKey) {
        this.dataLinkModelAllFieldList.filter(pif => {
          if(pif.fieldKey == item.fieldKey && pif.enabledQueryTypes) {
            for(let i in pif.enabledQueryTypes) {
              let str = ''
              switch(pif.enabledQueryTypes[i]) {
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
                arr.push({label: str, value: pif.enabledQueryTypes[i]})
              }
            }
          }
        })
      }
      if(arr.length == 0) {
        arr = [
          {label: '等于', value: 'eq'},
          {label: '不等于', value: 'ne'},
          {label: '模糊匹配', value: 'like'}
        ]
      }
      return arr
    },
    fieldKeyChange (item, index) {
      this.$set(this.modelDisplayRow.advancedSettings.modelDisplay.dataLinkageList[index], 'enabledQueryTypes', this.getEnabledQueryTypes(item)[0].value)
      this.$set(this.modelDisplayRow.advancedSettings.modelDisplay.dataLinkageList[index], 'prop', 'field')
      this.$set(this.modelDisplayRow.advancedSettings.modelDisplay.dataLinkageList[index], 'value', '')
    },
    // 条件配置公式
    openConditionFormula (item) {
      let label = ''
      this.dataLinkModelAllFieldList.filter(dit => {
        if(dit.fieldKey == item.fieldKey) {
          label = dit.fieldName
        }
      })
      this.$setFormula({
        title: '公式设置',
        hasModal: true,
        label: label,
        execId: (item.formula ? item.formula : ''),
        apiPrefix: 'jvs-design',
        useCase: 'pageButtonDisplay',
        props: {
          jvsAppId: this.infoData.jvsAppId,
          designId: this.designId,
          businessId: item.fieldKey
        },
        afterSave: (dialog, data) => {
          if(data && data.id) {
            this.$set(item, 'formula', data.id)
            this.$set(item, 'formulaContent', data.body)
          }
          dialog.handleClose()
        }
      })
    },
    seniorChange (form, item) {
      if(item && item.prop == 'showWidth') {
        this.$set(this.rowData, 'showWidth', form.showWidth)
      }
      if(item && item.prop == 'conditionControl') {
        this.$set(this.rowData.advancedSettings, 'conditionControl', form.conditionControl)
      }
      this.emitGetColumn()
      this.$forceUpdate()
    },
    displayHandle (cit) {
      let bool = true
      if(cit.prop == 'fixed') {
        bool = this.infoData.displayType != 'card' ? true : false
      }
      if(cit.display) {
        if((!this.rowData[cit.display.key] == cit.display.value)) {
          bool = false
        }
      }
      if(cit.prop == 'enableRetrieval') {
        if(this.rowData && this.rowData.designJson && ((this.rowData.designJson.url || this.rowData.designJson.dicUrl || this.rowData.designJson.dicData) || ['department', 'cascader'].indexOf(this.rowData.designJson.type) > -1)) {
          bool = true
        }else{
          bool = false
        }
      }
      if(cit.prop == 'enableStatistics') {
        if(this.rowData && (this.rowData.designJson ? (this.rowData.designJson && ['inputNumber', 'slider'].indexOf(this.rowData.designJson.type) > -1) : ['inputNumber', 'slider'].indexOf(this.rowData.componentType) > -1)) {
          bool = true
        }else{
          bool = false
        }
      }
      if(this.dialogType == 'child') {
        bool = false
        if(['tools'].indexOf(cit.prop) > -1) {
          bool = true
        }
      }
      return bool
    },
    noHandle () {
      return false
    },
    clearAttr (attr) {
      if(attr == 'openFormId') {
        this.$set(this.rowData.advancedSettings, attr, '')
        // delDesign({appId: this.infoData.jvsAppId, designId: this.rowData.advancedSettings[attr], designType: 'form'}).then(res => {
        //   if(res.data && res.data.code == 0) {
        //     this.$set(this.rowData.advancedSettings, attr, '')
        //   }
        // })
      }
      if(attr == 'formula') {
        deleteExec(this.designId, this.rowData.advancedSettings[attr]).then(res => {
          if(res.data && res.data.code == 0) {
            this.$set(this.rowData.advancedSettings, 'formula', '')
            this.$set(this.rowData.advancedSettings, 'combiningFieldFormulaContent', '')
          }
        })
      }
      this.$forceUpdate()
    },
    emitGetColumn () {
      this.$emit('getColumn', true)
      this.$nextTick(() => {
        this.$refs.fieldScroll.scrollTo({
          top: this.fieldScrollTop,
          behavior: "smooth",
        })
        this.$nextTick(() => {
          this.$refs.fieldScroll.scrollTo({
            top: this.fieldScrollTop,
            behavior: "smooth",
          })
        })
      })
    },
    moveRowStart (row, rix) {
      this.moveSource = JSON.parse(JSON.stringify(row))
      this.seniorSet(row, rix, 'senior')
    },
    moveRowing (row) {
      if(this.dialogType != 'child') {
        this.moveTarget = JSON.parse(JSON.stringify(row))
      }
    },
    moveRowEnd () {
      if(this.dialogType != 'child') {
        if(this.moveTarget && this.moveTarget.aliasColumnName != this.moveSource.aliasColumnName) {
          let from = -1
          this.tableData.filter((bit, bix) => {
            if(bit.aliasColumnName == this.moveSource.aliasColumnName) {
              from = bix
            }
          })
          if(from > -1) {
            this.tableData.splice(from, 1)
            this.$forceUpdate()
          }
          let to = -1
          this.tableData.filter((bit, bix) => {
            if(bit.aliasColumnName == this.moveTarget.aliasColumnName) {
              to = bix
            }
          })
          if(to > -1) {
            this.tableData.splice(to, 0, this.moveSource)
            this.emitGetColumn()
            this.$forceUpdate()
          }
        }
        this.moveSource = null
        this.moveTarget = null
      }
    },
    modelDisplayHandle (row, index) {
      this.modelDisplayRow = {}
      this.linkageFieldKeys = []
      if(!row.advancedSettings.modelDisplay) {
        this.$set(row.advancedSettings, 'modelDisplay', {
          dataLinkageModelId: '',
          dataLinkageList: [],
          linkageFieldKeys: [],
          showModelType: 'oneToMany'
        })
      }else{
        if(row.advancedSettings.modelDisplay.dataLinkageModelId){
          this.changeLinkModelPropList(row.advancedSettings.modelDisplay.dataLinkageModelId, 'create')
        }
        if(row.advancedSettings.modelDisplay.linkageFieldKeys && row.advancedSettings.modelDisplay.linkageFieldKeys.length > 0) {
          row.advancedSettings.modelDisplay.linkageFieldKeys.filter(lit => {
            this.linkageFieldKeys.push(lit.prop)
          })
        }
      }
      this.modelDisplayRow = row
      this.modelDisplayIndex = index
      this.modelDiaplayVisible = true
    },
    modelDisplayClose () {
      this.modelDiaplayVisible = false
      this.modelDisplayIndex = -1
    },
    colorChangeHandle (value, prop, form) {
      if(form) {
        this.$set(form, prop, value)
      }
      this.seniorChange(this.rowData.advancedSettings, {prop: prop})
    },
    customColorChangeHandle (value, prop, form) {
      if(form) {
        this.$set(form, prop, value)
      }
      this.seniorChange(this.rowData.advancedSettings, {prop: 'conditionControl'})
    },
    setControlColor (item, row) {
      this.$set(row, 'color', item.color)
      this.$set(row, 'bgcolor', item.back)
      this.seniorChange(this.rowData.advancedSettings, {prop: 'conditionControl'})
    },
    addConditionControl () {
      if(!this.rowData.advancedSettings.conditionControl) {
        this.$set(this.rowData.advancedSettings, 'conditionControl', [])
      }
      this.rowData.advancedSettings.conditionControl.push({})
      this.$forceUpdate()
    },
    deleteStyleRow (row, index) {
      this.rowData.advancedSettings.conditionControl.splice(index, 1)
      this.seniorChange(this.rowData.advancedSettings, {prop: 'conditionControl'})
      this.$forceUpdate()
    },
    // 同步表格数据
    setTableHandle (data) {
      this.$set(this.rowData.advancedSettings, 'conditionControl', data)
      this.$forceUpdate()
    },
    moveChildRowStart (row, rix) {
      this.moveChildSource = JSON.parse(JSON.stringify(row))
      this.seniorSet(row, rix, 'child')
    },
    moveChildRowing (row) {
      if(this.dialogType == 'child') {
        this.moveChildTarget = JSON.parse(JSON.stringify(row))
      }
    },
    moveChildRowEnd () {
      if(this.dialogType == 'child') {
        if(this.moveChildTarget && this.moveChildTarget.aliasProp != this.moveChildSource.aliasProp) {
          let from = -1
          this.tableData[this.index].advancedSettings.modelDisplay.linkageFieldKeys.filter((bit, bix) => {
            if(bit.aliasProp == this.moveChildSource.aliasProp) {
              from = bix
            }
          })
          if(from > -1) {
            this.tableData[this.index].advancedSettings.modelDisplay.linkageFieldKeys.splice(from, 1)
            this.$forceUpdate()
          }
          let to = -1
          this.tableData[this.index].advancedSettings.modelDisplay.linkageFieldKeys.filter((bit, bix) => {
            if(bit.aliasProp == this.moveChildTarget.aliasProp) {
              to = bix
            }
          })
          if(to > -1) {
            this.tableData[this.index].advancedSettings.modelDisplay.linkageFieldKeys.splice(to, 0, this.moveChildSource)
            this.childIndex = to
            this.emitGetColumn()
            this.$forceUpdate()
          }
        }
        this.moveChildSource = null
        this.moveChildTarget = null
      }
    },
    deleteOneOfList (row, index, list) {
      list.splice(index, 1)
      if(this.childIndex > -1) {
        this.childIndex = index - 1
        if(this.childIndex > -1) {
          this.seniorSet(this.tableData[this.index].advancedSettings.modelDisplay.linkageFieldKeys[this.childIndex], this.childIndex, 'child', 'fresh')
        }else{
          this.seniorSet(this.tableData[this.index], this.index, 'senior', 'fresh')
        }
      }
      this.$forceUpdate()
    },
    setColorHandle (item) {
      this.$set(this.rowData.advancedSettings, 'textcolor', item.color)
      this.$set(this.rowData.advancedSettings, 'backColor', item.back)
      this.seniorChange(this.rowData.advancedSettings, {prop: 'textcolor'})
    },
    modelDiaplayOpend () {
      if(this.$refs.linkageFieldKeysRef) {
        this.$refs.linkageFieldKeysRef.handleResize()
      }
    }
  },
  created () {
    // 初始化赋值组件类型
    for(let i in this.tableData) {
      this.setValueOfOther(this.tableData[i], i, 'senior')
    }
    if(this.designData && this.designData.leftTreeButton) {
      this.leftTreeButtonList = JSON.parse(JSON.stringify(this.designData.leftTreeButton))
    }
    this.getDataModelList()
    if(this.tableData && this.tableData.length > 0) {
      this.seniorSet(this.tableData[0], 0, 'senior')
    }
  },
  watch: {
    needEmpty: {
      handler(newVal, oldVal) {
        if(newVal !== -1) {
          for(let i in this.tableData) {
            this.$set(this.tableData[i], 'tableName', '')
            this.$set(this.tableData[i], 'columnName', '')
          }
          this.$forceUpdate()
        }
      }
    },
    data: {
      handler(newVal, oldVal) {
        if(newVal !== -1) {
          for(let i in this.data) {
            this.setValueOfOther(this.data[i], i, 'senior')
          }
          if(this.tableData && this.tableData.length > 0) {
            if(!(this.tableData.length > this.index && this.index > -1 && this.rowData && this.tableData[this.index].aliasColumnName == this.rowData.aliasColumnName)) {
              this.index = -1
              this.seniorSet(this.tableData[0], 0, 'senior')
              this.$forceUpdate()
            }
          }
        }
      }
    },
    'infoData.displayType': {
      handler(newVal, oldVal) {
        this.seniorSetOption.column.filter(item => {
          if(item.prop == 'cardPosition') {
            if(newVal && newVal == 'card') {
              item.display = true
            }else{
              item.display = false
            }
          }
        })
      },
      deep: true
    }
  }
}
</script>
<style lang="scss" scoped>
.sort-item{
  i{
    cursor: pointer;
  }
}
.quest-icon-info{
  margin-left: 5px;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: #C2C5CF;
  color: #fff;
  cursor: pointer;
  display: inline-block;
  text-align: center;
  line-height: 12px;
  span{
    font-size: 12px;
  }
}
.bottom-add-button{
  margin-top: 8px;
  .button{
    width: 80px;
    display: flex;
    align-items: center;
    cursor: pointer;
    .icon{
      width: 16px;
      height: 16px;
      background: #1E6FFF;
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 4px;
      svg{
        width: 12px;
        height: 12px;
        fill: #fff;
      }
    }
    span{
      font-family: Microsoft YaHei-Regular, Microsoft YaHei;
      font-weight: 400;
      font-size: 14px;
      color: #1E6FFF;
      line-height: 18px;
    }
  }
}
.data-filter-div{
  .data-filter-div-item{
    background-color: #f9f9f9;
    padding: 0 15px;
    padding-top: 10px;
    border-radius: 6px;
    overflow: hidden;
    position: relative;
    margin-top: 15px;
    .delete-data-filter-tool{
      position: absolute;
      right: 15px;
      bottom: 15px;
      i{
        color: #F56C6C;
      }
    }
    /deep/.el-select__tags{
      height: 24px;
    }
  }
  .data-filter-div-item:nth-of-type(1){
    margin-top: 0;
  }
  .data-filter-item{
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 10px;
    .el-select{
      margin-right: 10px;
      flex: 1;
    }
    .el-icon-delete{
      margin-left: 10px;
    }
  }
}
/deep/.jvs-form-item{
  .el-slider__input{
    .el-input-number__decrease.is-disabled, .el-input-number__increase.is-disabled{
      height: 26px;
      line-height: 26px;
      color: #C0C4CC;
    }
  }
}
.add-formula-svg{
  width: 16px;
  height: 16px;
  cursor:pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.bottom-button{
  margin-top: 8px;
  .button{
    width: 80px;
    display: flex;
    align-items: center;
    cursor: pointer;
    .icon{
      width: 16px;
      height: 16px;
      background: #1E6FFF;
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 4px;
      svg{
        width: 12px;
        height: 12px;
        fill: #fff;
      }
    }
    span{
      font-family: Microsoft YaHei-Regular, Microsoft YaHei;
      font-weight: 400;
      font-size: 14px;
      color: #1E6FFF;
      line-height: 18px;
    }
  }
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
.table-info-form{
  display: flex;
  .table-info-form-left{
    width: 360px;
    height: 100%;
    border-right: 1px solid #F5F6F7;
    box-sizing: border-box;
    overflow: hidden;
    .table-info-form-left-top{
      height: 100%; // calc(100% - 48px);
      padding: 16px 0;
      box-sizing: border-box;
      overflow: hidden;
      &.all-height{
        height: 100%;
      }
      h4{
        margin: 0 24px;
        padding: 0;
        height: 18px;
        font-family: Microsoft YaHei-Bold, Microsoft YaHei;
        font-weight: 700;
        font-size: 14px;
        color: #363B4C;
        line-height: 18px;
      }
      .all-field-list{
        margin-left: 8px;
        padding-right: 8px;
        padding-top: 16px;
        height: calc(100% - 32px);
        overflow: hidden;
        overflow-y: auto;
        box-sizing: border-box;
        .all-field-list-item{
          .main-field{
            display: flex;
            align-items: center;
            padding-left: 12px;
            padding-right: 16px;
            height: 48px;
            border-left: 2px solid transparent;
            box-sizing: border-box;
            &.active{
              background: #E4EDFF;
              border-color: #1E6FFF;
              .field-select{
                .el-select{
                  /deep/.el-input__inner{
                    background: #fff;
                  }
                }
              }
              .svg-icon{
                background: #fff;
              }
            }
            &:hover:not(.active){
              background: #EEEFF0;
              .field-select{
                .el-select{
                  /deep/.el-input__inner{
                    color: #6F7588;
                  }
                }
              }
            }
          }
          .choose-icon{
            margin-right: 16px;
            cursor: move;
            svg{
              width: 20px;
              height: 20px;
              transform: rotate(90deg);
              fill: #6F7588;
            }
          }
          .field-select{
            flex: 1;
            height: 28px;
            overflow: hidden;
            .el-select{
              width: 100%;
              overflow: hidden;
              /deep/.el-input__inner{
                width: 100%;
                padding-right: 15px;
                height: 28px;
                line-height: 28px;
                background: #F5F6F7;
                border: 0;
                box-sizing: border-box;
                border: 2px solid #F5F6F7;
                color: #363B4C;
                font-family: Microsoft YaHei-Regular, Microsoft YaHei;
                font-weight: 400;
                font-size: 14px;
                border: 0;
                box-sizing: border-box;
                overflow: hidden;
              }
              .placeholder{
                color: #6F7588;
              }
            }
          }
          .svg-icon{
            margin-left: 12px;
            width: 28px;
            height: 28px;
            background: #F5F6F7;
            border-radius: 4px;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            svg{
              width: 16px;
              height: 16px;
              fill: #363B4C;
            }
            &.up{
              transform: rotate(90deg);
            }
            &.down{
              transform: rotate(-90deg);
            }
            &.disabled{
              cursor: not-allowed;
              svg{
                fill: #D7D8DB;
              }
            }
          }
          &.target{
            position: relative;
            &::after{
              content: '';
              position: absolute;
              top: 0;
              left: 0;
              width: 100%;
              height: 1px;
              background: #1E6FFF;
            }
          }
          .children-fields{
            margin-top: 8px;
            margin-left: 48px;
            background: #F5F6F7;
            border-radius: 4px;
            .field-select{
              .el-select{
                /deep/.el-input__inner{
                  background-color: #fff;
                }
              }
            }
            .svg-icon{
              background: #fff;
            }
          }
        }
      }
      .add-field-button-row{
        margin-right: 24px;
        height: 32px;
        /deep/.el-button{
          height: 32px;
          font-size: 14px;
          background: #DBE8FF;
          color: #1E6FFF;
          font-family: Microsoft YaHei-Regular, Microsoft YaHei;
          font-weight: 400;
          font-size: 14px;
          &+.el-button{
            margin-left: 16px;
          }
          &.primary{
            background-color: #1E6FFF;
            color: #fff;
          }
        }
      }
    }
  }
  .table-info-form-right{
    display: flex;
    flex-direction: column;
    flex: 1;
    padding: 0 24px;
    padding-bottom: 24px;
    box-sizing: border-box;
    overflow: hidden;
    overflow-y: auto;
    .right-title{
      margin-top: 23px;
      margin-bottom: 16px;
      height: 18px;
      font-family: Microsoft YaHei, Microsoft YaHei;
      font-weight: 700;
      font-size: 14px;
      color: #363B4C;
      line-height: 18px;
      .icon{
        width: 14px;
        height: 14px;
        margin-right: 8px;
      }
    }
    .setting-title{
      display: flex;
      align-items: center;
      margin-bottom: 16px;
      .icon{
        width: 14px;
        height: 14px;
        margin-right: 8px;
      }
      span{
        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
        font-weight: 400;
        font-size: 14px;
        color: #363B4C;
      }
      .border-line{
        margin: 0 16px;
        flex: 1;
        border-top: 1px dashed #C2C5CF;
      }
    }
    .row-basic-setting{
      margin: 0 22px;
      .row-basic-setting-column{
        height: 32px;
        display: flex;
        align-items: center;
        margin-bottom: 16px;
        .label{
          width: 94px;
          height: 18px;
          font-family: Microsoft YaHei-Regular, Microsoft YaHei;
          font-weight: 400;
          font-size: 14px;
          color: #6F7588;
          line-height: 18px;
          display: flex;
          align-items: center;
        }
        .content{
          width: 280px;
          overflow: hidden;
          &.tools{
            width: calc(100% - 94px);
            .el-checkbox{
              margin-right: 28px;
              margin-left: 0;
            }
          }
          .el-input, .el-select{
            width: 100%;
            /deep/.el-input__inner{
              height: 32px;
              line-height: 32px;
              background: #F5F6F7;
              border: 0;
              font-family: Microsoft YaHei-Regular, Microsoft YaHei;
              font-weight: 400;
              font-size: 14px;
              color: #363B4C;
              &::placeholder{
                color: #6F7588;
              }
            }
          }
        }
      }
    }
    .row-senior-setting{
      padding: 0 24px;
      box-sizing: border-box;
      .custom-slider{
        display: flex;
        align-items: center;
        width: 280px;
        /deep/.el-slider{
          flex: 1;
          .el-slider__runway{
            height: 8px;
            border-radius: 4px;
            background-color: #F5F6F7;
            .el-slider__bar{
              height: 8px;
              background-color: #1E6FFF;
              border-radius: 4px;
            }
            .el-slider__button{
              box-sizing: border-box;
              border: 1px solid #C2C5CF;
              box-shadow: 0px 0px 2px 0px rgba(54,59,76,0.15);
            }
          }
        }
        .text{
          margin-left: 16px;
          width: 88px;
          height: 32px;
          background: #F5F6F7;
          border-radius: 4px;
          display: flex;
          align-items: center;
          font-family: Microsoft YaHei-Regular, Microsoft YaHei;
          font-weight: 400;
          font-size: 14px;
          color: #6F7588;
          /deep/.el-input-number{
            flex: 1;
            overflow: hidden;
            .el-input{
              height: 32px;
              line-height: 32px;
              .el-input__inner{
                padding: 0 12px;
                height: 32px;
                line-height: 32px;
                text-align: left;
                color: #363B4C;
              }
            }
          }
          .text-px{
            margin-right: 12px;
          }
          .text-auto{
            width: 100%;
            text-align: center;
          }
        }
      }
      .column-color-picker{
        display: flex;
        align-items: center;
        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
        font-weight: 400;
        .text-info{
          width: 280px;
          height: 32px;
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 0 12px;
          background: #F5F6F7;
          border-radius: 4px;
          box-sizing: border-box;
          cursor: pointer;
          .box{
            width: 56px;
            height: 20px;
            border-radius: 4px;
            display: flex;
            align-items: center;
            justify-content: center;
            .text{
              font-size: 10px;
            }
            .back-color-text{
              background-clip: text!important;
              -webkit-background-clip :text!important;
              -webkit-text-fill-color: transparent;
            }
          }
        }
        .color-box{
          margin-left: 16px;
          width: 132px;
          height: 32px;
          display: flex;
          align-items: center;
          justify-content: space-between;
          background: #F5F6F7;
          border-radius: 4px;
          padding: 0 12px;
          box-sizing: border-box;
          .color-label{
            font-size: 14px;
            color: #6F7588;
            word-break: keep-all;
          }
          /deep/.jvs-color-picker-show-box{
            width: auto!important;
            height: 20px;
            padding: 0;
            display: flex;
            align-items: center;
            .show-color-hex{
              display: none;
            }
            .close-box{
              display: none;
            }
            &:hover{
              .close-box{
                display: flex;
              }
            }
          }
        }
      }
      /deep/.jvs-form{
        width: 100%;
        box-sizing: border-box;
        .el-form-item__label{
          width: 94px;
          height: 32px;
          line-height: 32px;
          font-family: Microsoft YaHei-Regular, Microsoft YaHei;
          font-weight: 400;
          font-size: 14px;
          color: #6F7588;
          padding-right: 0;
          display: flex;
          align-items: center;
        }
        .el-form-item__content{
          margin-left: 94px;
          line-height: 32px;
        }
        .form-column-tableForm{
          margin-top: 12px;
          padding: 16px;
          border-radius: 4px;
          background: #F5F6F7;
          overflow: hidden;
          .column-color-picker{
            .text-info{
              width: 120px;
              background: #fff;
            }
            .color-box{
              width: 132px;
              background: #fff;
              margin-left: 8px;
              box-sizing: border-box;
            }
          }
        }
        .table-form{
          .el-form-item{
            margin: 0;
            .el-form-item__content{
              margin-left: 0!important;
            }
            .jvs-form-item{
              padding: 0;
            }
          }
          .jvs-table{
            background: transparent;
          }
          .table-body-box{
            background: transparent;
            .el-table{
              background: transparent;
              &::before{
                visibility: hidden;
              }
              .el-table__header-wrapper{
                border: 0;
                .el-table__header{
                  .headerclass{
                    th{
                      background: #F5F6F7;
                      height: 20px;
                      padding: 0;
                      line-height: 20px;
                      .cell{
                        font-family: Source Han Sans-Regular, Source Han Sans;
                        font-weight: 400;
                        font-size: 14px;
                        color: #363B4C;
                        text-align: left;
                        padding: 0;
                        padding-right: 16px;
                      }
                    }
                  }
                }
              }
              .el-table__body-wrapper{
                min-height: unset;
                .el-table__body{
                  tr{
                    background: transparent;
                    td{
                      padding: 0;
                      padding-top: 8px;
                      height: 32px;
                      line-height: 32px;
                      border: 0;
                      .cell{
                        padding: 0;
                        padding-right: 16px;
                        .el-form-item__content{
                          min-height: unset;
                          line-height: 32px;
                          .el-input{
                            height: 32px;
                            .el-input__inner{
                              height: 32px;
                              line-height: 32px;
                              background: #fff;
                            }
                          }
                        }
                      }
                      &.table-index-column{
                        .cell{
                          text-indent: 10px;
                        }
                      }
                      &:nth-last-of-type(1){
                        .cell{
                          padding-right: 0;
                          text-align: center;
                        }
                      }
                    }
                    &:hover{
                      td{
                        background: none;
                      }
                    }
                  }
                }
                .el-table__empty-block{
                  display: none;
                }
              }
            }
          }
        }
        .el-col{
          margin-bottom: 0;
        }
        .el-form-item{
          margin-bottom: 16px;
        }
        .plain-button{
          width: 132px;
          height: 32px;
          background: #EDF4FF;
          border-radius: 4px;
          span{
            font-family: Microsoft YaHei-Regular, Microsoft YaHei;
            font-weight: 400;
            font-size: 14px;
            color: #1E6FFF;
          }
        }
      }
      &.hide-table{
        /deep/.jvs-form{
          .slot-label-item{
            >.el-form-item__content{
              display: none;
            }
          }
        }
      }
    }
  }
}
.model-display-body{
  width: 100%;
  height: 100%;
  font-family: Source Han Sans-Regular, Source Han Sans;
  font-weight: 400;
  font-size: 14px;
  color: #363B4C;
  padding: 16px 32px;
  box-sizing: border-box;
  .el-form-item{
    margin-bottom: 16px;
    .el-form-item__label{
      width: 100%;
      text-align: left;
      height: 18px;
      font-family: Microsoft YaHei-Bold, Microsoft YaHei;
      font-weight: 700;
      font-size: 14px;
      color: #363B4C;
      line-height: 18px;
      margin-bottom: 8px;
    }
    .my-select{
      width: 434px;
      /deep/.el-select__tags{
        width: auto!important;
        max-width: unset!important;
        .el-tag{
          height: 28px;
          background: #fff;
          border: 0;
          padding: 0 8px;
          .el-select__tags-text{
            font-family: Source Han Sans-Regular, Source Han Sans;
            font-weight: 400;
            font-size: 14px;
            color: #363B4C;
          }
          .el-tag__close{
            background: transparent;
            font-size: 18px;
            color: #6F7588;
          }
        }
      }
      /deep/.el-input{
        min-height: 36px;
        line-height: 36px;
        font-size: 14px;
        .el-input__inner{
          min-height: 36px!important;
          line-height: 36px;
          background: #F5F6F7;
          border: 0;
        }
      }
    }
    .datalink-filter-list-box{
      width: 100%;
      overflow: hidden;
      .datalink-filter-list-box-body{
        width: 100%;
        background: #F5F6F7;
        border-radius: 4px;
        padding: 16px;
        box-sizing: border-box;
        .datalink-filter-list-box-body-header{
          width: 100%;
          display: flex;
          align-items: center;
          height: 20px;
          line-height: 20px;
          overflow: hidden;
          >div{
            flex: 1;
            height: 20px;
            &+div{
              margin-left: 16px;
            }
          }
          >span{
            display: block;
            height: 20px;
          }
        }
        .datalink-filter-list-box-body-body{
          margin-top: 8px;
          display: flex;
          align-items: center;
          .item{
            flex: 1;
            display: flex;
            align-items: center;
            &+.item{
              margin-left: 16px;
            }
          }
          .delete-icon-button{
            margin-left: 16px;
          }
        }
      }
      .el-select{
        width: 100%;
        /deep/.el-input__inner{
          font-size: 14px;
          height: 32px;
          line-height: 32px;
          background: #fff;
          border: 0;
          color: #363B4C;
          &::placeholder{
            font-family: Microsoft YaHei-Regular, Microsoft YaHei;
            font-weight: 400;
            font-size: 14px;
            color: #6F7588;
          }
        }
      }
    }
  }
}
.color-card-list{
  padding: 0 12px;
  padding-top: 4px;
  max-height: 439px;
  box-sizing: border-box;
  overflow: hidden;
  overflow-y: auto;
  display: flex;
  flex-wrap: wrap;
  .color-card-list-item{
    width: 72px;
    height: 26px;
    line-height: 26px;
    border-radius: 4px;
    text-align: center;
    margin-bottom: 8px;
    cursor: pointer;
  }
  .color-card{
    margin-right: 20px;
  }
  .color-card:nth-of-type(3n) {
    margin-right: 0;
  }
}
</style>
