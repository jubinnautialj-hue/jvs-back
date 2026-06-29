<template>
  <div class="button-info">
    <div class="page-button-setting-list">
      <div
        v-for="(button, btix) in tableData"
        :key="'page-button-set-item-'+btix"
        :draggable="button.type == 'btn_delete' ? false : true"
        @dragstart="moveBtnStart(button)"
        @dragenter="moveBtning(button)"
        @dragend="moveBtnEnd"
        :class="{'page-button-setting-list-item': true, 'open': buttonOpenStatus[button.permissionFlag], 'target': (moveTarget && (button.permissionFlag == moveTarget.permissionFlag))}">
        <div class="heade">
          <div class="name">{{button.name}}</div>
          <div class="heade-tool">
            <el-popover  v-if="button.type && ['btn_import', 'btn_print', 'btn_delete'].indexOf(button.type) == -1" placement="top" trigger="hover" :content="button.type | getTypeLabel" popper-class="custom-right-tool-poper">
              <svg slot="reference" aria-hidden="true" @click="configBtnHandle(button, btix)">
                <use xlink:href="#jvs-ui-icon-quanxianshezhi"></use>
              </svg>
            </el-popover>
            <span v-if="button.type && ['btn_import', 'btn_print', 'btn_delete'].indexOf(button.type) == -1" class="divider-line"></span>
            <div class="con-box">
              <svg v-if="button.isDefault == false" aria-hidden="true" @click="deleteRow(button, btix)">
                <use xlink:href="#jvs-ui-icon-shanchuyonghu"></use>
              </svg>
              <span v-if="button.isDefault == false" class="divider-line"></span>
              <svg v-if="button.type != 'btn_delete'" class="move-icon" aria-hidden="true" @mousedown="moveBtnStart(button)">
                <use xlink:href="#icon-jvs-tuodong"></use>
              </svg>
              <span v-if="button.type != 'btn_delete'" class="divider-line"></span>
            </div>
            <svg aria-hidden="true" @click="buttonOpenStatus[button.permissionFlag]=!buttonOpenStatus[button.permissionFlag];$forceUpdate();">
              <use :xlink:href="`#${buttonOpenStatus[button.permissionFlag] ? 'icon-jvs-shouqi1' : 'icon-jvs-zhankai'}`"></use>
            </svg>
          </div>
        </div>
        <el-collapse-transition>
          <div v-show="buttonOpenStatus[button.permissionFlag]" class="item-body">
            <div class="item-body-item">
              <span class="label">按钮名称</span>
              <div class="con">
                <el-input v-model="button.name" placeholder="请输入" size="mini" @change="emitGetColumn"></el-input>
              </div>
            </div>
            <div class="item-body-item">
              <span class="label">按钮位置</span>
              <div class="con">
                <el-select v-model="button.position" placeholder="请选择" size="mini" :disabled="defaultBtnTypes.indexOf(button.type) > -1" @change="emitGetColumn">
                  <el-option
                    v-for="item in buttonPosition"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value">
                  </el-option>
                </el-select>
              </div>
            </div>
            <div class="item-body-item">
              <span class="label">按钮类型</span>
              <div class="con">
                <el-select v-model="button.type" placeholder="请选择" size="mini" v-if="defaultBtnTypes.indexOf(button.type) > -1" :disabled="defaultBtnTypes.indexOf(button.type) > -1" @change="emitGetColumn">
                  <el-option
                    v-for="item in buttonTypeList"
                    :key="item.value+'-select-item'"
                    :label="item.label"
                    :value="item.value">
                  </el-option>
                </el-select>
                <el-select size="mini" v-model="button.type" placeholder="请选择" v-if="defaultBtnTypes.indexOf(button.type) == -1" @change="btnTypeChange(button, btix)">
                  <el-option
                    v-for="item in buttonTypeItemshow()"
                    :key="item.value+'-btn-item'"
                    :label="item.label"
                    :value="item.value">
                  </el-option>
                </el-select>
              </div>
            </div>
            <div v-if="button.type && ['btn_import', 'btn_print', 'btn_delete'].indexOf(button.type) == -1" class="item-body-item">
              <span class="label">{{button.type | getTypeLabel}}</span>
              <div class="con">
                <el-button type="text" @click="configBtnHandle(button, btix)">配置</el-button>
                <el-tooltip v-if="button.type == 'btn_rule_design'" class="item" effect="light" :content="attrIntroduce.rule" placement="top">
                  <div class="icon-info">
                    <span>?</span>
                  </div>
                </el-tooltip>
              </div>
            </div>
            <div class="item-body-item">
              <span class="label">pc端显示</span>
              <div class="con">
                <el-checkbox size="mini" v-model="button.enable" @change="emitGetColumn"></el-checkbox>
                <svg v-if="button.position == 'line' && button.enable" aria-hidden="true" class="add-formula-svg" style="margin-left: 10px;" @click="openButtonFormula(button)">
                  <use :xlink:href="`#${button.formula ? 'icon-jvs-a-zu10923' : 'icon-jvs-rongqi1'}`"></use>
                </svg>
              </div>
            </div>
            <div class="item-body-item">
              <span class="label">移动端显示</span>
              <div class="con">
                <el-checkbox size="mini" v-model="button.mobileEnable"></el-checkbox>
                <svg v-if="button.position == 'line' && button.mobileEnable" aria-hidden="true" class="add-formula-svg" style="margin-left: 10px;" @click="openButtonFormula(button, 'mobile')">
                  <use :xlink:href="`#${button.mobileFormula ? 'icon-jvs-a-zu10923' : 'icon-jvs-rongqi1'}`"></use>
                </svg>
              </div>
            </div>
            <div v-if="button.type && ['btn_add', 'btn_modify', 'btn_detail', 'btn_form'].indexOf(button.type) > -1" class="item-body-item">
              <span class="label">打开方式</span>
              <div class="con">
                <el-radio-group v-model="button.openType" size="mini" style="margin-top: 5px;">
                  <el-radio label="dialog">弹框</el-radio>
                  <el-radio label="tab">标签栏</el-radio>
                </el-radio-group>
              </div>
            </div>
          </div>
        </el-collapse-transition>
      </div>
    </div>
    <div class="add-button-tool" @click="addRowHandle">
      <i class="el-icon-plus"></i>
      <span>添加按钮</span>
    </div>
    <!-- 选择配置页面类型 -->
    <el-dialog
      :class="{'custom-header-dialog': true, 'export-select-dialog': currentBtn.type == 'btn_export'}"
      width="50%"
      v-if="formTypeDialogVisible"
      :visible.sync="formTypeDialogVisible"
      :before-close="handleCloseformTypeDialog"
      append-to-body
      :title="currentBtn.name || '设计'"
      :close-on-click-modal="false"
    >
      <div class="dialog-div" style="padding: 16px;">
        <!-- 导出 字段 -->
        <div v-if="['btn_export'].indexOf(currentBtn.type) > -1" class="export-body">
          <div class="select-multiple-table">
            <div class="title-text">共{{exportFieldsColumn.length}}项</div>
            <jvs-table
              ref="exportTable"
              :refs="'select-multiple-table'"
              :selectable="true"
              :defaultAllSelect="true"
              rowKey="aliasColumnName"
              :option="columnTableOption"
              :data="exportFieldsColumn"
              :selectedRows="selectedRows"
              @selection-change="selectionChange"
            ></jvs-table>
          </div>
          <div class="export-field-list">
            <div class="drag-title">
              <span>已选({{currentBtn.exportFields.length}})</span>
              <el-button type="text" @click="clearHandle">清空</el-button>
            </div>
            <div class="drag-list">
              <div
                v-for="field in currentBtn.exportFields"
                :key="'select-field-item-'+field.aliasColumnName"
                :class="{'drag-list-item': true, 'target': (moveFieldTarget && (field.aliasColumnName == moveFieldTarget.aliasColumnName))}"
                :draggable="true"
                @dragstart="moveFieldStart(field)"
                @dragenter="moveFielding(field)"
                @dragend="moveFieldEnd"
              >
                <svg class="move-icon" aria-hidden="true" @mousedown="moveFieldStart(field)">
                  <use xlink:href="#jvs-ui-icon-tuodong1"></use>
                </svg>
                <div>
                  <span class="name">{{field.aliasColumnName}}</span>
                  <span style="margin: 0 5px;">/</span>
                  <span>{{field.showChinese}}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 下载模板 -->
        <div v-if="currentBtn.type == 'btn_download_template'" class="select-multiple-table">
          <jvs-table
            :refs="'select-multiple-table'"
            :selectable="true"
            :defaultAllSelect="true"
            :option="columnTableOption"
            :data="exportColumnData"
            :selectedRows="selectedRows"
            @selection-change="selectionChange"
          >
            <template slot="description" slot-scope="scope">
              <el-input v-model="scope.row.description" size="mini"></el-input>
            </template>
            <template slot="required" slot-scope="scope">
              <el-switch v-model="scope.row.required" size="mini"></el-switch>
            </template>
            <template slot="showChineseAlias" slot-scope="scope">
              <el-input v-model="scope.row.showChineseAlias" size="mini"></el-input>
            </template>
          </jvs-table>
          <div v-if="false" style="padding: 0 20px;">
            <div style="display: flex;align-items: center;margin-top: 10px;">
              <span style="margin-right: 10px;color: #959595;font-size: 14px;word-break: keep-all;display: block;min-width: 65px;">sheel名称</span>
              <el-input v-model="currentBtn.sheelName" size="mini"></el-input>
            </div>
            <div style="display: flex;align-items: center;margin-top: 10px;">
              <span style="margin-right: 10px;color: #959595;font-size: 14px;word-break: keep-all;display: block;min-width: 65px;">模板文件</span>
              <el-button type="primary" size="mini" @click="openUploadTemp">{{currentBtn.templateFileLink ? '重新上传' : '上传'}}</el-button>
              <span v-if="currentBtn.templateFileLink" style="margin-left: 10px;">
                <a :href="currentBtn.templateFileLink">{{currentBtn.templateFileName}}</a>
                <i class="el-icon-delete" @click="delTempFileLink" style="margin-left: 10px;color: #F56C6C;cursor: pointer;"></i>
              </span>
            </div>
            <div style="margin-top: 10px;font-size: 12px;color: #909399;">
              <div style="margin-left: 75px;">
                <span>不上传模板文件时，将自动生成。</span>
                <br/>
                <span>模板文件中存sheel时，导入将自动读取sheel信息。</span>
              </div>
            </div>
          </div>
        </div>
        <el-row v-if="['btn_external_link_address', 'btn_embedded_address'].indexOf(currentBtn.type) > -1">
          <div style="width: 100%;display: flex;align-items: center;justify-content: space-between;">
            <span class="el-form-item__label">地址</span>
            <el-popover
              trigger="click"
              v-model="addressVisible">
              <div class="popover-list">
                <template v-for="(item,index) in addressFields">
                  <div class="popover-list-item"  :key="index" v-if="item.fieldName" v-show="['userId', 'realName', 'token'].indexOf(item.fieldKey) > -1 ? true : (currentBtn.position == 'line')" @click="popoverClick(item,'addressEditor', 'addressVisible')">
                    {{item.fieldName}}
                  </div>
                </template>
              </div>
              <jvs-button slot="reference" type='text'>插入字段</jvs-button>
            </el-popover>
          </div>
          <div>
            <div id="addressContent"></div>
          </div>
          <!-- <el-input size="mini" v-model="currentBtn.address"></el-input> -->
        </el-row>
        <el-row
          style="display: flex; justify-content: center"
          v-if="['btn_delete', 'btn_import', 'btn_export', 'btn_download_template'].indexOf(currentBtn.type) == -1"
        >
          <jvs-button
            type="primary"
            size="mini"
            @click="formTypeSubmit"
            style="margin-top: 10px"
            >确定</jvs-button
          >
          <jvs-button
            size="mini"
            @click="handleCloseformTypeDialog"
            style="margin-top: 10px"
            >取消</jvs-button
          >
        </el-row>
      </div>
      
    </el-dialog>
    <!-- 网络设置  可以选择已有逻辑引擎 -->
    <el-dialog
      title="逻辑设计"
      class="custom-header-dialog"
      width="30%"
      append-to-body
      :visible.sync="netSetVisible"
      :close-on-click-modal="false"
      :before-close="netSetClose">
      <div v-if="netSetVisible" style="padding: 16px;">
        <div style="display: flex;align-items: center;">
          <el-button size="mini" type="primary" @click="newRuleSet" :loading="newRuleSetLoading">新建业务逻辑</el-button>
        </div>
        <div style="display: flex;align-items: center;margin-top: 15px;">
          <span style="margin-right: 10px;">选择逻辑</span>
          <el-select v-model="currentBtn.secret" size="mini" filterable clearable @change="eventHttpChange" style="flex: 1;">
            <el-option v-for="rit in allRuleList" :key="'all-rule-item-'+rit.secret" :label="rit.name" :value="rit.secret"></el-option>
          </el-select>
          <i v-if="currentBtn.secret" class="el-icon-edit-outline form-icon-btn" style="margin-left: 10px;cursor: pointer;" @click="viewRule"></i>
        </div>
        <div style="display: flex;align-items: center;margin-top: 15px;">
          <span style="margin-right: 10px;word-break: keep-all;">二次确认</span>
          <el-input v-model="currentBtn.confirm" type="textarea" autosize size="mini" clearable @change="eventHttpChange" @clear="eventHttpChange"></el-input>
        </div>
        <div style="display: flex;align-items: center;margin-top: 15px;">
          <span style="margin-right: 10px;word-break: keep-all;">执行入参</span>
          <el-input v-model="currentBtn.customParameterIn" size="mini" clearable @change="eventHttpChange" @clear="eventHttpChange"></el-input>
        </div>
      </div>
    </el-dialog>
    <!-- 绑定列表页 -->
    <el-dialog
      class="custom-header-dialog"
      width="50%"
      title="列表设计"
      append-to-body
      :visible.sync="pageVisible"
      :close-on-click-modal="false"
      :before-close="pageSetClose">
      <div v-if="pageVisible" style="padding: 16px;">
        <div style="display: flex;align-items: center;margin-top: 15px;">
          <div style="display: flex;align-items: center;align-items:center;">
            <span style="margin-right: 10px;word-break: keep-all;">选择列表</span>
            <el-select v-model="currentBtn.formId" size="mini" filterable clearable @change="pageBindChange">
              <el-option v-for="pit in pageList" :key="'all-rule-item-'+pit.id" :label="pit.name" :value="pit.id"></el-option>
            </el-select>
            <i v-if="currentBtn.formId" @click="designPage(currentBtn.formId)" class="el-icon-edit-outline" style="color: #5b8bff;margin-left: 20px;font-size: 18px;cursor: pointer;"></i>
            <i @click="newPageHandle" class="el-icon-circle-plus-outline" style="color: #5b8bff;margin-left: 15px;font-size: 20px;cursor: pointer;"></i>
          </div>
          <div style="display: flex;align-items: center;margin-left: 35px;align-items:center;">
            <span style="margin-right: 10px;word-break: keep-all;">打开方式</span>
            <el-radio-group v-model="currentBtn.openType" size="mini" style="margin-top: 5px;">
              <el-radio label="dialog">弹框</el-radio>
              <el-radio label="tab">标签栏</el-radio>
            </el-radio-group>
          </div>
        </div>
        <div v-if="currentBtn.formId && currentBtn.position == 'line'" :style="'display: flex;margin-top: 15px;align-items:center;' + ((!currentBtn.pageQuery && currentBtn.pageQuery.length == 0) ? 'align-items: center;' : '')">
          <span style="margin-right: 10px;word-break: keep-all;">弹框标题</span>
          <el-select v-model="currentBtn.dialogTitle" size="mini" filterable placeholder="请选择" style="margin-right: 10px;">
            <el-option label="数据id" value="id"></el-option>
            <el-option v-for="fit in columnTableData" :key="'all-field-item-'+fit.aliasColumnName" :label="fit.showChinese" :value="fit.aliasColumnName"></el-option>
          </el-select>
          <span style="margin: 0 10px;word-break: keep-all;">弹框宽占比</span>
          <div style="display: flex;width: 250px;margin-left: 10px;align-items: center;">
            <el-slider v-model="currentBtn.dialogWidth" style="flex: 1;"></el-slider>
            <span style="margin-left: 15px;">{{currentBtn.dialogWidth}}%</span>
          </div>
        </div>
        <div v-if="currentBtn.formId && currentBtn.position == 'line'" :style="'display: flex;margin-top: 15px;' + ((!currentBtn.pageQuery && currentBtn.pageQuery.length == 0) ? 'align-items: center;' : '')">
          <span style="margin-right: 10px;word-break: keep-all;line-height: 28px;">数据过滤</span>
          <div class="custom-edit-table">
            <div class="custom-edit-table-header">
              <span>目标列表字段</span>
              <span>匹配逻辑</span>
              <span>当前列表字段</span>
              <span>操作</span>
            </div>
            <div v-if="currentBtn.pageQuery && currentBtn.pageQuery.length > 0" class="custom-edit-table-body">
              <div v-for="(item, inx) in currentBtn.pageQuery" :key="'page-query-'+inx" class="custom-edit-table-body-row">
                <div class="cell">
                  <el-select v-model="item.fieldKey" size="mini" filterable placeholder="请选择目标列表字段" style="margin-right: 10px;">
                    <el-option label="数据id" value="id"></el-option>
                    <el-option v-for="fit in pageFieldList" :key="'all-field-item-'+fit.aliasColumnName" :label="fit.showChinese" :value="fit.aliasColumnName"></el-option>
                  </el-select>
                </div>
                <div class="cell">
                  <el-select v-model="item.enabledQueryTypes" size="mini" filterable placeholder="请选择匹配逻辑" style="margin-right: 10px;">
                    <el-option v-for="(qit, qix) in getEnabledQueryTypes(item)" :key="'enabled-query-types-'+inx+'-'+qix" :label="qit.label" :value="qit.value"></el-option>
                  </el-select>
                </div>
                <div class="cell">
                  <el-select v-model="item.value" size="mini" filterable placeholder="请选择当前列表字段" style="margin-right: 10px;">
                    <el-option label="数据id" value="id"></el-option>
                    <el-option v-for="fit in columnTableData" :key="'all-field-item-'+fit.aliasColumnName" :label="fit.showChinese" :value="fit.aliasColumnName"></el-option>
                  </el-select>
                </div>
                <div class="cell">
                  <el-button type="text" size="mini" style="color: #F56C6C;"  @click="delQuery(inx, currentBtn.pageQuery)">删除</el-button>
                </div>
              </div>
            </div>
            <div style="margin-top: 15px;">
              <el-button size="mini" type="primary" @click="addQuery">新增数据过滤</el-button>
            </div>
          </div>
        </div>
        <div v-if="currentBtn.formId && currentBtn.position == 'line'" :style="'display: flex;margin-top: 15px;' + ((!currentBtn.pageQuery && currentBtn.pageQuery.length == 0) ? 'align-items: center;' : '')">
          <span style="margin-right: 10px;word-break: keep-all;line-height: 28px;">查询条件</span>
          <div class="custom-edit-table">
            <div class="custom-edit-table-header">
              <span>目标列表字段</span>
              <span style="width: 100px;flex: none;">判断逻辑</span>
              <span>当前列表字段</span>
              <span>是否禁用</span>
              <span :style="hasPageSearchFilter(currentBtn.pageSearch) ? 'width: 100px;flex: none;' : ''">操作</span>
            </div>
            <div v-if="currentBtn.pageSearch && currentBtn.pageSearch.length > 0" class="custom-edit-table-body">
              <div v-for="(item, inx) in currentBtn.pageSearch" :key="'page-search-'+inx" class="custom-edit-table-body-row">
                <div class="cell">
                  <el-select v-model="item.fieldKey" size="mini" filterable placeholder="请选择目标列表字段" style="margin-right: 10px;">
                    <el-option label="数据id" value="id"></el-option>
                    <el-option v-for="fit in pageFieldList" :key="'all-field-item-'+fit.aliasColumnName" :label="fit.showChinese" :value="fit.aliasColumnName"></el-option>
                  </el-select>
                </div>
                <div class="cell" style="width: 100px;flex: none;text-align: center;">
                  <span style="margin-right: 10px;font-size: 12px;">等于</span>
                </div>
                <div class="cell">
                  <el-select v-model="item.value" size="mini" filterable placeholder="请选择当前列表字段" style="margin-right: 10px;">
                    <el-option label="数据id" value="id"></el-option>
                    <el-option v-for="fit in columnTableData" :key="'all-field-item-'+fit.aliasColumnName" :label="fit.showChinese" :value="fit.aliasColumnName"></el-option>
                  </el-select>
                </div>
                <div class="cell">
                  <el-switch v-model="item.disabled" size="mini"></el-switch>
                </div>
                <div class="cell" :style="hasPageSearchFilter(currentBtn.pageSearch) ? 'width: 100px;flex: none;text-align: center;' : ''">
                  <el-button v-if="hasPageSearchFilter(item)" type="text" size="mini" @click="setPageSearchFilter(item, inx)">数据筛选</el-button>
                  <el-button type="text" size="mini" style="color: #F56C6C;" @click="delQuery(inx, currentBtn.pageSearch)">删除</el-button>
                </div>
              </div>
            </div>
            <div style="margin-top: 15px;">
              <el-button size="mini" type="primary" @click="addSearch">新增查询条件</el-button>
            </div>
          </div>
        </div>
        <div v-if="currentBtn.formId && currentBtn.position == 'line'" :style="'display: flex;margin-top: 15px;' + ((!currentBtn.pageBottomBtns && currentBtn.pageBottomBtns.length == 0) ? 'align-items: center;' : '')">
          <span style="margin-right: 10px;word-break: keep-all;line-height: 28px;">按钮设置</span>
          <div class="custom-edit-table">
            <div class="custom-edit-table-header">
              <span>按钮名称</span>
              <span>逻辑设置</span>
              <span style="width: 140px;flex: none;">是否关闭弹框</span>
              <span>操作</span>
            </div>
            <div v-if="currentBtn.pageBottomBtns && currentBtn.pageBottomBtns.length > 0" class="custom-edit-table-body">
              <div v-for="(item, inx) in currentBtn.pageBottomBtns" :key="'page-bottom-btn-'+inx" class="custom-edit-table-body-row">
                <div class="cell">
                  <el-input v-model="item.name" size="mini" placeholder="请输入按钮名称"></el-input>
                </div>
                <div class="cell">
                  <el-select v-model="item.rule" size="mini" filterable clearable @change="eventHttpChange" style="flex: 1;">
                    <el-option v-for="rit in allRuleOfPage" :key="'all-rule-item-'+rit.secret" :label="rit.name" :value="rit.secret"></el-option>
                  </el-select>
                  <i v-if="item.rule && item.name" @click="viewPageRule(item)" class="el-icon-edit-outline" style="color: #5b8bff;margin-left: 15px;font-size: 18px;cursor: pointer;"></i>
                  <i v-if="item.name" @click="createPageRule(item)" class="el-icon-circle-plus-outline" style="color: #5b8bff;margin-left: 15px;font-size: 20px;cursor: pointer;"></i>
                </div>
                <div class="cell" style="width: 140px;flex: none;">
                  <el-switch v-model="item.closable" size="mini"></el-switch>
                </div>
                <div class="cell">
                  <el-button type="text" size="mini" style="color: #F56C6C;" @click="delQuery(inx, currentBtn.pageBottomBtns)">删除</el-button>
                </div>
              </div>
            </div>
            <div style="margin-top: 15px;">
              <el-button size="mini" type="primary" @click="addBottomBtn">新增按钮</el-button>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
    <!-- 列表页查询字段数据筛选条件 -->
    <el-dialog
      title="数据筛选"
      append-to-body
      :visible.sync="pageSearchFilterVisible"
      :close-on-click-modal="false"
      :before-close="pageSearchFilterClose">
      <div v-if="pageSearchFilterVisible">
        <div class="data-filter-div">
          <el-row v-for="(item, index) in pageSearchFilterItem.dataFileterList" :key="'data-filter-item-'+index" class="data-filter-item">
            <el-select v-model="item.fieldKey" placeholder="请选择字段" size="mini" @change="fieldKeyChange(item)">
              <el-option label="数据id" value="id"></el-option>
              <el-option
                v-for="it in pageFieldList"
                v-show="needShow(pageSearchFilterItem.dataFileterList, 'fieldKey', it.fieldKey)"
                :key="'connect-show-'+it.aliasColumnName"
                :label="it.showChinese"
                :value="it.aliasColumnName">
              </el-option>
            </el-select>
            <el-select v-if="['select', 'radio', 'checkbox'].indexOf(getSearchFilterDom(item).type) > -1 ? getSearchFilterDom(item).datatype != 'option' : true" v-model="item.enabledQueryTypes" placeholder="请选择匹配规则" size="mini">
              <el-option label="等于" value="eq"></el-option>
              <el-option label="模糊匹配" value="like"></el-option>
              <el-option label="不等于" value="ne"></el-option>
              <el-option label="等于空" value="isNull"></el-option>
            </el-select>
            <span v-else>包含</span>
            <el-select v-if="item.enabledQueryTypes != 'isNull'" v-model="item.type" placeholder="请选择匹配类型" size="mini">
              <el-option label="字段" value="prop"></el-option>
              <el-option label="自定义" value="cust"></el-option>
            </el-select>
            <el-input v-if="item.enabledQueryTypes != 'isNull' && item.type == 'cust' && ['select', 'radio', 'checkbox'].indexOf(getSearchFilterDom(item).type) == -1" v-model="item.value" size="mini" style="width:auto;"></el-input>
            <el-select v-if="item.enabledQueryTypes != 'isNull' && item.type == 'cust' && ['select', 'radio', 'checkbox'].indexOf(getSearchFilterDom(item).type) > -1" v-model="item.value" size="mini">
              <el-option v-for="op in getSearchFilterDom(item).dicData" :key="'value-item-'+op.value" :label="op.label" :value="op.value"></el-option>
            </el-select>
            <i class="el-icon-delete" style="cursor: pointer;" @click="delQuery(index, pageSearchFilterItem.dataFileterList)"></i>
          </el-row>
          <p>
            <el-button size="mini" @click="addDataFilter">添加</el-button>
          </p>
        </div>
        <el-row style="display: flex;align-items: center;justify-content: center;">
          <el-button size="mini" type="primary" @click="dataFilterSubmit">确定</el-button>
          <el-button size="mini" @click="pageSearchFilterClose">取消</el-button>
        </el-row>
      </div>
    </el-dialog>
    <!-- 上传模板 -->
    <el-dialog
      title="上传模板文件"
      :visible.sync="tempFileDialogVisible"
      width="720px"
      append-to-body
      :close-on-click-modal="false"
      :before-close="tempFileHandleClose">
      <div class="import-data-box">
        <el-upload
          class="import-data-upload"
          ref="tempUploader"
          :action="`/mgr/jvs-auth/upload/jvs-public`"
          :headers="headers"
          accept=".xls,.xlsx"
          :data="{module: '/jvs-ui/pageTemplate/'}"
          :file-list="tempfileList"
          :show-file-list="false"
          :on-change="onTempChange"
          :on-success="handleAppSuccess"
          :on-error="errTempHandle"
          drag
          multiple>
          <div class="el-upload__text">
            <svg aria-hidden="true" style="width: 24px; height: 24px;margin-bottom: 16px">
              <use xlink:href="#icon-upload"></use>
            </svg>
            <div>点击或者拖动文件到虚线框内上传</div>
            <div style="color: #a2a3a5;font-size: 12px;margin-top: 8px;">支持Excel类型的文件</div>
          </div>
        </el-upload>
        <div class="upload-explain">
          <span style="color: #a2a3a5;">上传的文件符合以下规范：</span>
          <ul>
            <li style="list-style: disc">仅支持<span>（*.xls, *.xlsx）</span>文件</li>
          </ul>
        </div>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import E from "wangeditor";
import { btnType } from '../../../const/const'

// 表单项
import MInput from '../../../plugin/assembly/input'
import MTextarea from '../../../plugin/assembly/textarea'
import MInputNumber from '../../../plugin/assembly/inputNumber'
import MSwitch from '../../../plugin/assembly/switch'
import MTimepicker from '../../../plugin/assembly/timepicker'
import MDatePicker from '../../../plugin/assembly/datePicker'
import MSelect from '../../../plugin/assembly/select'
import MUser from '../../../plugin/assembly/user'
import MDepartment from '../../../plugin/assembly/department'
import MRole from '../../../plugin/assembly/role'
import MPost from '../../../plugin/assembly/post'
import MInputReadOnly from '../../../plugin/assembly/inputreadonly'
import MTextareaReadOnly from '../../../plugin/assembly/textareareadonly'
import MImage from '../../../plugin/assembly/image'
import MFile from '../../../plugin/assembly/file'

import { getButtonFormId, createPage } from "../../../api/newDesign";
import { createRule, getDesignInfo } from "@/views/page/api/design";

import {guid} from "@/util/util";
import { getAllRule } from '@/components/template/api'
import { getAllPageByApplication } from '../../../api/list'

export default {
  name: 'button-info',
  props: {
    data: {
      type: Array,
      default: () => {
        return []
      }
    },
    designColumn: {
      type: Array
    },
    infoData: {
      type: Object
    },
    designId: {
      type: String
    },
    dataModelId: {
      type: String
    },
    tableSetNameOption: {
      type: Array,
      default: () => {
        return []
      }
    },
    fieldsData: {
      type: Array
    }
  },
  filters: {
    getTypeLabel (type) {
      let str = '按钮设计'
      switch(type) {
        case 'btn_add':
        case 'btn_modify':
        case 'btn_detail':
        case 'btn_form':
          str = '表单设计';break;
        case 'btn_page':
          str = '列表设计';break;
        case 'btn_export':
          str = '导出设计';break;
        case 'btn_rule_design':
          str = '业务逻辑';break;
        case 'btn_download_template':
          str = '下载设计';break;
        case 'btn_embedded_address':
        case 'btn_external_link_address':
          str = '地址设计';break;
        default: break;
      }
      return str
    }
  },
  computed: {
    tableData: {
      get () {
        let temp = this.data
        let index = -1
        let delItem = null
        for(let i in temp) {
          if(!temp[i].type) {
            temp[i].type = 'btn_rule_design'
          }
          if(!temp[i].openType && temp[i].type && ['btn_add', 'btn_modify', 'btn_detail', 'btn_form'].indexOf(temp[i].type) > -1) {
            this.$set(temp[i], 'openType', 'dialog')
          }
          if(["btn_add", "btn_modify", "btn_detail", "btn_form"].indexOf(temp[i].type) > -1) {
            if(!temp[i].form || !temp[i].form.formdata || temp[i].form.formdata.length == 0) {
              this.initDefaultForm(temp[i])
            }else{
              if(temp[i].form) {
                for(let j in temp[i].form.formdata) {
                  if(!temp[i].form.formdata[j].forms) {
                    temp[i].form.formdata[j].forms = this.eachColumnList(temp[i].form.formType, temp[i]).column
                  }
                  if(!temp[i].form.formdata[j].formJson) {
                    temp[i].form.formdata[j].formJson = this.eachColumnList(temp[i].form.formType, temp[i]).formJson
                  }
                  this.formatFormItem(temp[i].form.formdata)
                }
              }
            }
            if(!temp[i].formType) {
              temp[i].formType = temp[i].type == 'btn_detail' ? 'detailForm' : 'normalForm'
            }
          }
          if(!temp[i].permissionFlag) {
            temp[i].permissionFlag = temp[i].position + '-' + temp[i].type + '-' + guid()
          }
          if(temp[i].type == 'btn_delete') {
            index = i
            delItem = JSON.parse(JSON.stringify(temp[i]))
          }
          if(temp[i].type == 'btn_export') {
            if(!temp[i].exportFields) {
              temp[i].exportFields = JSON.parse(JSON.stringify(this.exportFieldsColumn))
            }
          }
          if(temp[i].type == 'btn_download_template') {
            if(!temp[i].importFields) {
              temp[i].importFields = JSON.parse(JSON.stringify(this.exportColumnData))
            }
          }
        }
        if(index > -1) {
          temp.splice(index, 1)
        }
        if(delItem) {
          temp.push(delItem)
        }
        return temp
      },
      set () { }
    },
    columnTableData: {
      get () {
        let temp = []
        for(let i in this.designColumn) {
          temp.push(this.designColumn[i])
        }
        return temp
      },
      set () { }
    },
    exportFieldsColumn: {
      get () {
        let temp = []
        for(let i in this.fieldsData) {
          temp.push({
            aliasColumnName: this.fieldsData[i].fieldKey,
            showChinese: this.fieldsData[i].fieldName
          })
        }
        return temp
      },
      set () { }
    },
    exportColumnData : {
      get () {
        let temp = []
        for(let i in this.designColumn) {
          temp.push(this.designColumn[i])
        }
        return temp
      },
      set () {}
    },
    addressFields () {
      return this.fieldsData.concat([
        {fieldName: 'id', fieldKey: 'id'},
        {fieldName: '当前登录用户名', fieldKey: 'realName'},
        {fieldName: '当前登录用户ID', fieldKey: 'userId'},
        {fieldName: '当前登录token', fieldKey: 'token'}
      ])
    }
  },
  data () {
    return {
      moveSource: null,
      moveTarget: null,
      buttonOpenStatus: {},
      attrIntroduce: {
        rule: '通过可视化的拖拽编排，把多种不同的基础服务或功能节点拼装成业务功能。',
      },
      // 按钮设置表单配置
      option: {
        addBtn: false,
        viewBtn: false,
        delBtn: false,
        editBtn: false,
        page: false,
        border: true,
        menuWidth: '120px',
        cancal: false,
        column: [
          {
            label: '按钮名称',
            prop: 'name',
            needSlot: true,
            watch: true
          },
          {
            label: '按钮位置',
            prop: 'position',
            type: 'select',
            dicData: [],
            disabled: true,
            needSlot: true,
            watch: true
          },
          {
            label: '按钮类型',
            prop: 'type',
            type: 'select',
            dicData: [],
            disabled: true,
            watch: true,
            needSlot: true
          },
          {
            label: 'pc端显示',
            prop: 'enable',
            type: 'switch',
            needSlot: true
          },
          {
            label: '移动端显示',
            prop: 'mobileEnable',
            type: 'switch',
            needSlot: true
          },
        ]
      },
      // 可配置的按钮类型
      enableConfigTypeList: ['save_form', 'update_form', 'show_label', 'network_post_url', 'network_get_url', 'export_excel'],
      currentBtn: {}, // 当前按钮
      formTypeDialogVisible: false, // 选择表单类型
      // 导出配置
      exportSettingOption: {
        btnHide: true,
        column: [
          // {
          //   label: '绑定字段',
          //   prop: 'exportKey',
          //   type: 'select',
          //   dicData: [],
          //   display: true
          // },
          {
            label: '导出字段',
            prop: 'exportName',
            type: 'select',
            multiple: true,
            dicData: []
          },
        ]
      },
      // 导出配置--表单值
      exportSetting: {
        // exportKey: '', // 绑定字段
        exportName: [], // 导出字段
      },
      // 网络请求
      networkForm: {
        type: '',
        url: '',
        headers: [],
        body: ''
      },
      // 网络请求配置
      networkOption: {
        btnHide: true,
        column: [
          {
            label: '请求类型',
            prop: 'type',
            type: 'select',
            dicData: [
              {label: 'GET', value: 'GET'},
              {label: 'POST', value: 'POST'},
              {label: 'DELETE', value: 'DELETE'},
              {label: 'PUT', value: 'PUT'}
            ]
          },
          {
            label: '地址',
            prop: 'url'
          },
          {
            label: '请求头',
            prop: 'headers',
            formSlot: true
          }
        ]
      },
      // 按钮类型列表
      buttonTypeList: [],
      // 按钮位置列表
      buttonPosition: [
        { label: '顶部', value: 'top' },
        { label: '行内', value: 'line' },
      ],
      // 导出 字段显示配置
      columnTableOption: {
        addBtn: false,
        menu: false,
        page: false,
        border: true,
        cancal: false,
        showOverflow: true,
        column: [
          {
            label: '字段名',
            prop: 'aliasColumnName',
          },
          {
            label: '字段中文名',
            prop: 'showChinese',
          },
          {
            label: '必填',
            prop: 'required',
            type: 'switch',
            hide: true,
            slot: true
          },
          // {
          //   label: '模板字段别名',
          //   prop: 'showChineseAlias',
          //   hide: true,
          //   slot: true
          // },
          // {
          //   label: '模板字段格式描述',
          //   prop: 'description',
          //   hide: true,
          //   slot: true
          // },
        ]
      },
      buttonRemark: '', // 按钮备注
      currentIndex: -1, // 当前按钮index
      masterTable: null, // 表单的id
      boolShow: true, // 表单设计内容 显隐
      fileList: [],
      headers: {
        tenantId: this.$store.getters.userInfo.tenantId,
        Authorization: 'Bearer ' + this.$store.getters.access_token
      },
      formId: '',
      dialogType: '', // 高级设置   备注
      defaultBtnTypes: ['btn_add', 'btn_modify', 'btn_detail', 'btn_import', 'btn_export', 'btn_download_template', 'btn_delete'],
      allRuleList: [], // 当前模型下的所有的逻辑引擎列表
      newRuleSetLoading: false, // 新增逻辑loading
      netSetVisible: false,
      pageVisible: false,
      pageList: [],
      pageFieldList: [],
      allRuleOfPage: [], // 列表页所在模型的逻辑列表
      pageSearchFilterVisible: false,
      pageSearchFilterItem: null,
      pageSearchFilterIndex: -1,
      tempFileDialogVisible: false,
      tempfileList: [],
      addressEditor: null,
      addressVisible: false,
      moveFieldSource: null,
      moveFieldTarget: null,
    }
  },
  methods: {
    // 添加按钮
    addRowHandle () {
      this.tableData.push({
        id: 'BTN'+new Date().getTime(),
        name: '按钮名称',
        position: 'top',
        type: 'btn_rule_design',
        netHttp: {}, // 网络请求
        secret: null, // 逻辑引擎唯一标识
        // 进一步配置的数据
        disabled: false,
        fineGrainedType: '',
        form: {
          formdata: [],
          formType: ''
        },
        enable: true,
        isDefault: false
      })
      this.$emit('permissionHandle', true)
      this.$set(this.buttonOpenStatus, this.tableData[this.tableData.length-2].permissionFlag, true)
    },
    // 删除按钮
    deleteRow (row, index) {
      this.tableData.splice(index, 1)
      this.$emit('permissionHandle', true)
    },
    // 配置按钮
    configBtnHandle (row, index) {
      // console.log(row)
      if(['btn_add', 'btn_modify', 'btn_detail', 'btn_form'].indexOf(row.type) > -1) {
        if (row.formId) {
          let str = ''
          str = location.origin + (`/page-design-ui/#/form?jvsAppId=${this.infoData.jvsAppId}&id=`+row.formId + (this.dataModelId ? `&dataModelId=${this.dataModelId}` : '') + `&isDetail=${row.type === 'btn_detail'}&isAddForm=${row.position == 'top'}` + (['btn_modify', 'btn_detail'].indexOf(row.type) > -1 ? `&setEcho=false` : ''))
          this.$openUrl(str, '_blank')
        } else {
          getButtonFormId(this.infoData.jvsAppId, this.dataModelId, this.designId, row.name).then(res => {
            if (res.data && res.data.code == 0) {
              row.formId = res.data.data
              this.$emit('handleSave')
              let str = ''
              str = location.origin + (`/page-design-ui/#/form?jvsAppId=${this.infoData.jvsAppId}&id=`+res.data.data + (this.dataModelId ? `&dataModelId=${this.dataModelId}` : '') + `&isDetail=${row.type === 'btn_detail'}&isAddForm=${row.position == 'top'}` + (['btn_modify', 'btn_detail'].indexOf(row.type) > -1 ? `&setEcho=false` : ''))
              this.$openUrl(str, '_blank')
            }
          })
        }
      }
      if(row.form) {
        if(!row.form.formdata) {
          row.form.formdata = []
        }
      }
      if(!row.formName) {
        row.formName = row.type + row.formType + '_' + new Date().getTime()
      }
      if(['btn_delete', 'btn_import', 'btn_export', 'btn_download_template', 'btn_embedded_address', 'btn_external_link_address', 'btn_rule_design'].indexOf(row.type) > -1) {
        this.currentBtn=row
        // 逻辑引擎
        if(['btn_rule_design'].indexOf(this.currentBtn.type) > -1) {
          this.netSetVisible = true
        }
        // 导出  下载模板
        else if(['btn_export', 'btn_download_template'].indexOf(this.currentBtn.type) > -1){
          if(this.currentBtn.type == 'btn_export') {
            this.selectedRows = this.currentBtn.exportFields || this.exportFieldsColumn
            this.columnTableOption.column.filter(cit => {
              if(['description', 'showChineseAlias', 'required'].indexOf(cit.prop) > -1) {
                cit.hide = true
              }
            })
          }else{
            for(let i in this.exportColumnData) {
              if(!this.exportColumnData[i].showChineseAlias) {
                this.$set(this.exportColumnData[i], 'showChineseAlias', this.exportColumnData[i].showChinese)
              }
            }
            this.selectedRows = this.currentBtn.importFields || this.exportColumnData
            this.columnTableOption.column.filter(cit => {
              if(['description', 'showChineseAlias', 'required'].indexOf(cit.prop) > -1) {
                cit.hide = false
              }
            })
          }
          this.formTypeDialogVisible = true
        }
        // 内嵌地址  外链地址
        else if(["btn_embedded_address", "btn_external_link_address"].indexOf(this.currentBtn.type) > -1) {
          this.formTypeDialogVisible = true
          this.createEditor('addressEditor', 'addressContent')
        }else {}
        if(['btn_delete', 'btn_import', 'btn_export', 'btn_download_template'].indexOf(this.currentBtn.type) > -1) {
          this.formTypeDialogVisible = true
        }
      }
      // 列表
      else if(row.type == 'btn_page') {
        this.currentBtn = row
        if(!this.currentBtn.openType) {
          this.$set(this.currentBtn, 'openType', 'dialog')
        }
        this.getAllUseHandle()
      }else{
      }
    },
    // 按钮位置改变，对应按钮类型改变
    positionChangeHandle (data) {
      let item=data.item
      let row=data.row
      let temp=[]
      for (let i in btnType) {
        if (btnType[i].position.indexOf(row.position)>-1) {
          if (['FORM', 'NETWORK'].indexOf(btnType[i].value) > -1) {
            temp.push(btnType[i])
          }
        }
      }
      this.option.column.filter(it => {
        if (it.prop=='type') {
          it.dicData=temp
        }
      })
    },
    // 按钮类型改变
    typeChangeHandle (data) {
      let item=data.item
      let row=data.row
      if(row.type == 'NETWORK') {
        row.fineGrainedType = 'NETWORK'
      }
      if(row.type == 'FORM') {
        row.fineGrainedType = 'FORM'
      }
      // 导入时，添加一个导出模板按钮
      // if (row.type=='import_excel') {
      //   this.tableData.push({
      //     name: '导出模板',
      //     position: 'top',
      //     showJurisdiction: ['所有用户'],
      //     type: 'export_model'
      //   })
      // }
    },
    // 关闭选择表单类型
    handleCloseformTypeDialog () {
      this.formTypeDialogVisible=false
    },
    // 确定表单类型
    formTypeSubmit () {
      if(this.currentBtn.type == 'NETWORK') {
        let temp = JSON.parse(JSON.stringify(this.networkForm))
        let obj = {}
        for(let i in this.networkForm.headers) {
          obj[this.networkForm.headers[i].key] = this.networkForm.headers[i].value
        }
        temp.headers = obj
        this.currentBtn.networkForm = temp
      }
      if(this.currentBtn.type == 'btn_export') {
        this.currentBtn.exportFields = this.exportSetting.exportName
      }
      if(this.currentBtn.type == 'btn_download_template') {
        this.currentBtn.importFields = this.exportSetting.exportName
      }
      this.handleCloseformTypeDialog()
    },
    // 保存flowable设计参数
    flowChangeHandle (data) {
      if(this.currentBtn.form.formType == 'normalForm' || this.currentBtn.form.formType == 'multiLevelForm') {
        if(data.isFlowable) {
          this.currentBtn.form.isFlowable = data.isFlowable
        }else{
          this.currentBtn.form.isFlowable = false
        }
        if(data.flowableDom) {
          this.currentBtn.form.flowableDom = data.flowableDom
        }else{
          this.currentBtn.form.flowableDom = {}
        }
      }
    },
    // 添加请求头
    addHeader () {
      this.networkForm.headers.push({})
    },
    // 删除请求头
    deleteHeader (row, index) {
      this.networkForm.headers.splice(index, 1)
    },
    getConst () {
      // 按钮类型dic
      this.buttonTypeList = this.setDicData('buttonType', 'type')
    },
    setDicData (key, prop) {
      let labelValue = this.$store.getters.labelValue[key]
      let temp = []
      let hasPrint = false
      for(let i in labelValue) {
        temp.push({
          label: labelValue[i],
          value: i
        })
        if(i == 'btn_print') {
          hasPrint = true
        }
      }
      if(!hasPrint) {
        temp.push({
          label: '打印',
          value: 'btn_print'
        })
      }
      this.option.column.filter(item => {
        if(item.prop == prop) {
          item.dicData = temp
        }
      })
      return temp
    },
    // 按钮类型根据位置和是否默认过滤
    buttonTypeItemshow () {
      let temp = []
      for(let i in this.buttonTypeList) {
        let bool = false
        switch (this.buttonTypeList[i].value) {
          case 'btn_embedded_address' :
          case 'btn_external_link_address':
          case 'btn_rule_design':
          case 'btn_form':
          case 'btn_page':
            bool = true;
            break;
          default : bool = false;break;
        }
        if(bool) {
          temp.push(this.buttonTypeList[i])
        }
      }
      return temp
    },
    // 表单类型是否可以切换
    formTypeDisableHandle (btnType, formType, val) {
      let bool = true
      if(btnType == 'btn_form' && val != 'detailForm') {
        bool = false
      }else{
        bool = true
      }
      return bool
    },
    // 遍历字段列表生成表单项
    eachColumnList (type, currentBtn) {
      let temp = []
      let formJson = {}
      for(let i in currentBtn.form.formdata[0].autoTableFields) {
        let obj = {}
        if(type == 'detailForm') {
          switch(currentBtn.form.formdata[0].autoTableFields[i].componentType) {
            case 'inputReadOnly':
              obj = new MInputReadOnly();
              formJson[currentBtn.form.formdata[0].autoTableFields[i].aliasColumnName] = "";
              break;
            case 'textareaReadOnly':
              obj = new MTextareaReadOnly();
              formJson[currentBtn.form.formdata[0].autoTableFields[i].aliasColumnName] = "";
              break;
            case 'image':
              obj = new MImage();
              break;
            case 'file':
              obj = new MFile();
              break;
            default :
              obj = new MInputReadOnly();
              formJson[currentBtn.form.formdata[0].autoTableFields[i].aliasColumnName] = "";
              break;
          }
        }else{
          switch(currentBtn.form.formdata[0].autoTableFields[i].componentType) {
            case 'input':
              obj = new MInput();
              formJson[currentBtn.form.formdata[0].autoTableFields[i].aliasColumnName] = "";
              break;
            case 'textarea':
              obj = new MTextarea();
              formJson[currentBtn.form.formdata[0].autoTableFields[i].aliasColumnName] = "";
              break;
            case 'inputNumber':
              obj = new MInputNumber();
              // 整数 或 小数
              if(currentBtn.form.formdata[0].autoTableFields[i].isFloat == true) {
                obj.precision = 4
                formJson[currentBtn.form.formdata[0].autoTableFields[i].aliasColumnName] = 1.0001;
              }else{
                obj.precision = 0
                formJson[currentBtn.form.formdata[0].autoTableFields[i].aliasColumnName] = 1;
              }
              break;
            case 'SWITCh':
              obj = new MSwitch();
              formJson[currentBtn.form.formdata[0].autoTableFields[i].aliasColumnName] = false;
              break;
            case 'timePicker':
              obj = new MTimepicker();
              formJson[currentBtn.form.formdata[0].autoTableFields[i].aliasColumnName] = "10:00:00";
              break;
            case 'datePicker':
              obj = new MDatePicker();
              formJson[currentBtn.form.formdata[0].autoTableFields[i].aliasColumnName] = "2021-02-05 10:00:00";
              break;
            case 'select':
              obj = new MSelect();
              obj.multiple = false;
              formJson[currentBtn.form.formdata[0].autoTableFields[i].aliasColumnName] = "";
              if(currentBtn.form.formdata[0].autoTableFields[i].associatedFieldHttp) {
                obj.url = currentBtn.form.formdata[0].autoTableFields[i].associatedFieldHttp
                obj.datatype = 'url'
              }
              if(currentBtn.form.formdata[0].autoTableFields[i].associatedFields) {
                obj.props.value = currentBtn.form.formdata[0].autoTableFields[i].associatedFields.columnName
              }
              if(currentBtn.form.formdata[0].autoTableFields[i].displayField) {
                obj.props.label = currentBtn.form.formdata[0].autoTableFields[i].displayField.columnName
              }
              if(currentBtn.form.formdata[0].autoTableFields[i].advancedSettings && currentBtn.form.formdata[0].autoTableFields[i].advancedSettings.dictionary) {
                obj.dicData = currentBtn.form.formdata[0].autoTableFields[i].advancedSettings.dictionary
              }
              break;
            case 'inputReadOnly':
              obj = new MInputReadOnly();
              formJson[currentBtn.form.formdata[0].autoTableFields[i].aliasColumnName] = "";
              break;
            case 'user':
              obj = new MUser();
              formJson[currentBtn.form.formdata[0].autoTableFields[i].aliasColumnName] = "";
              break;
            case 'department':
              obj = new MDepartment();
              formJson[currentBtn.form.formdata[0].autoTableFields[i].aliasColumnName] = "";
              break;
            case 'role':
              obj = new MRole();
              formJson[currentBtn.form.formdata[0].autoTableFields[i].aliasColumnName] = "";
              break;
            case 'post':
              obj = new MPost();
              formJson[currentBtn.form.formdata[0].autoTableFields[i].aliasColumnName] = "";
              break;
            default :
              obj = new MInput();
              formJson[currentBtn.form.formdata[0].autoTableFields[i].aliasColumnName] = "";
              break;
          }
        }
        obj.label = currentBtn.form.formdata[0].autoTableFields[i].columnComment
        obj.prop = currentBtn.form.formdata[0].autoTableFields[i].aliasColumnName
        // if(columnTemp.datetype) {
        //   obj.datetype = columnTemp.datetype
        // }
        // if(columnTemp.num == 'int') {
        //   obj.precision = 0
        // }
        temp.push(obj)
      }
      return {column: temp, formJson: JSON.stringify(formJson)}
    },
    // 生成默认表单
    initDefaultForm (currentBtn) {
      // 表单
      if (["btn_add", "btn_modify", "btn_detail", "btn_form"].indexOf(currentBtn.type) > -1) {
        if(!currentBtn.form) {
          currentBtn.form = {}
        }
        // 新增的表单按钮 填充 autoTableFields
        if(!currentBtn.form.formdata || currentBtn.form.formdata.length == 0) {
          currentBtn.form.formdata = [
            {
              forms: [],
              formsetting: {
                labelposition: 'top',
                labelwidth: 80,
                formsize: 'mini',
                btnSetting: [],
                fullscreen: false
              },
              autoTableFields: []
            }
          ]
        }
        if(!currentBtn.form.formType) {
          currentBtn.form.formType = currentBtn.type == 'btn_detail' ? 'detailForm' : 'normalForm'
        }
        let tob = this.eachColumnList(currentBtn.form.formType, currentBtn)
        let defaultForm = {
          forms: tob.column,
          formsetting: {
            labelposition: 'top',
            labelwidth: 80,
            formsize: 'mini',
            btnSetting: [],
            fullscreen: false
          },
          formJson: tob.formJson,
        }
        if(!currentBtn.form) {
          currentBtn.form = {
            type: '',
            formdata: [defaultForm],
            autoTableFields: []
          }
        }
        if(!currentBtn.form.formdata || currentBtn.form.formdata.length == 0 || !currentBtn.form.formdata[0].forms) {
          currentBtn.form.formdata = [defaultForm]
        }
      }
    },
    //网络请求配置
    netSubmitHandle (form) {
      if(form) {
        this.currentBtn.netHttp = form.http
        this.handleCloseformTypeDialog()
      }
    },
    // 按钮请求配置
    preHttpSubmitHandle (form) {
      if(form) {
        this.currentBtn.preHttp = form.http
      }
      this.handleCloseformTypeDialog()
    },
    // 导出字段选择
    selectionChange (val) {
      if(this.currentBtn.type == 'btn_export') {
        this.currentBtn.exportFields = val
      }else{
        this.currentBtn.importFields = val
      }
    },
    // 兼容历史数据
    formatFormItem (formdata) {
      for(let i in formdata) {
        if(formdata[i].forms) {
          for(let j in formdata[i].forms) {
            // 下拉配置
            let item = this.getItemByValOfArr(formdata[i].forms[j].prop, 'aliasColumnName', formdata[i].autoTableFields)
            if(item) {
              if(formdata[i].forms[j].type == 'select') {
                if(!formdata[i].forms[j].props) {
                  formdata[i].forms[j].props = {
                    label: 'label',
                    value: 'value'
                  }
                }
                if(item.displayField) {
                  formdata[i].forms[j].props.label = item.displayField.columnName
                }
                if(item.associatedFields) {
                  formdata[i].forms[j].props.value = item.associatedFields.columnName
                }
                if(formdata[i].forms[j].url) {
                  formdata[i].forms[j].dicUrl = formdata[i].forms[j].url
                  formdata[i].forms[j].datatype = 'url'
                }
              }
              // 必填校验
              if(item.isNullable == 'NO'){
                if(formdata[i].forms[j].rules && formdata[i].forms[j].rules.length > 0) {
                  formdata[i].forms[j].rules[0].required = true
                  formdata[i].forms[j].rules[0].message = item.columnComment + '不能为空'
                }else{
                  formdata[i].forms[j].rules.push({required: true, message: (item.columnComment + '不能为空'), trigger: ["blur", "change"]})
                }
              }
            }
            // 兼容switch
            if(formdata[i].forms[j].type == 'SWITCH') {
              formdata[i].forms[j].type = 'switch'
            }
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
    // 按钮名称重复校验
    buttonNameValidate (data) {
      let index = data.index
      let row = data.row
      let count = 0
      for(let i in this.tableData) {
        if(this.tableData[i].name == row.name) {
          count++
        }
      }
      if(count > 1 || !row.name) {
        let name = '新增按钮' + new Date().getTime()
        this.$set(this.tableData[index], 'name', name)
      }
    },
    // 刷新表单的内容
    freshFormDataHandle(bool) {
      if(bool) {
        let row = JSON.parse(JSON.stringify(this.currentBtn))
        this.boolShow = false
      }
    },
    // 预览
    toolClick (type) {
      this.$root.eventBus.$emit('toolEvent', type)
    },
    uploadSuccess (res, file, fileList) {
      if(res.code == 0) {
        this.$notify({
          title: '提示',
          message: '导入成功',
          position: 'bottom-right',
          type: 'success'
        });
        this.fileList = []
        this.freshFormDataHandle(true)
      }else{
        this.$refs.uploadBtn.clearFiles()
        this.$notify({
          title: '提示',
          message: res.msg,
          position: 'bottom-right',
          type: 'error'
        });
      }
    },
    // 导入失败
    errHandle (err, file, fileList) {
      this.$refs.uploadBtn.clearFiles()
      this.$notify({
        title: '提示',
        message: err,
        position: 'bottom-right',
        type: 'error'
      });
    },
    // 按钮配置公式
    openButtonFormula (item, type) {
      this.$setFormula({
        title: '公式设置',
        hasModal: true,
        label: item.name,
        execId: (type == 'mobile') ? (item.mobileFormula ? item.mobileFormula : '') : (item.formula ? item.formula : ''),
        apiPrefix: 'jvs-design',
        useCase: 'pageButtonDisplay',
        props: {
          jvsAppId: this.infoData.jvsAppId,
          designId: this.designId,
          businessId: item.permissionFlag
        },
        afterSave: (dialog, data) => {
          if(data && data.id) {
            if(type == 'mobile') {
              this.$set(item, 'mobileFormula', data.id)
            }else{
              this.$set(item, 'formula', data.id)
            }
          }
          dialog.handleClose()
        }
      })
    },
    getAllRuleListHandle () {
      getAllRule(this.infoData.jvsAppId, {dataModelId: this.dataModelId}).then(res => {
        if(res.data && res.data.code == 0) {
          this.allRuleList = res.data.data
        }
      })
    },
    newRuleSet () {
      this.newRuleSetLoading = true
      createRule({jvsAppId: this.infoData.jvsAppId, componentId: this.currentBtn.permissionFlag, name: this.currentBtn.name, designId: this.designId, componentType: 'page'}).then(res => {
        if(res.data && res.data.code == 0) {
          this.newRuleSetLoading = false
          this.currentBtn.secret = res.data.data
          this.data = this.tableData
          this.getAllRuleListHandle()
          this.$openUrl(`/rule-design-ui/#/ruleDesign?id=${res.data.data}&componentId=${this.currentBtn.permissionFlag}&jvsAppId=${this.infoData.jvsAppId}&name=${this.currentBtn.name}`, '_blank')
          this.netSetClose()
          this.$emit('handleSave', true)
          this.$forceUpdate()
        }else{
          this.newRuleSetLoading = false
        }
      }).catch(e => {
        this.newRuleSetLoading = false
      })
    },
    eventHttpChange (val) {
      this.$forceUpdate()
    },
    viewRule () {
      if(this.currentBtn.secret) {
        this.$openUrl(`/rule-design-ui/#/ruleDesign?id=${this.currentBtn.secret}&componentId=${this.currentBtn.id}&jvsAppId=${this.infoData.jvsAppId}&name=${this.currentBtn.name}`, '_blank')
      }
    },
    netSetClose () {
      this.netSetVisible = false
    },
    // 获取所有列表
    getAllUseHandle () {
      getAllPageByApplication(this.infoData.jvsAppId).then(res => {
        if(res.data && res.data.code == 0) {
          this.pageList = res.data.data ? [...res.data.data] : []
          if(this.currentBtn.formId) {
            this.pageList.filter(pit => {
              if(pit.id == this.currentBtn.formId) {
                this.$set(this.currentBtn, 'dataModelId', pit.dataModelId)
                this.getPageFieldList(this.infoData.jvsAppId, this.currentBtn.formId)
              }
            })
          }
          if(!this.currentBtn.pageQuery) {
            this.$set(this.currentBtn, 'pageQuery', [])
          }
          if(!this.currentBtn.pageSearch) {
            this.$set(this.currentBtn, 'pageSearch', [])
          }
          if(!this.currentBtn.pageBottomBtns) {
            this.$set(this.currentBtn, 'pageBottomBtns', [])
          }
          if(!this.currentBtn.dialogWidth){
            this.$set(this.currentBtn, 'dialogWidth', 70)
          }
          this.getAllRuleOfPage()
          this.pageVisible = true
        }
      })
    },
    pageBindChange () {
      if(this.currentBtn.formId) {
        this.$set(this.currentBtn, 'pageQuery', [])
        this.$set(this.currentBtn, 'dialogTitle', '')
        this.$set(this.currentBtn, 'dialogWidth', 70)
        this.$set(this.currentBtn, 'pageSearch', [])
        this.$set(this.currentBtn, 'pageBottomBtns', [])
        this.pageList.filter(pit => {
          if(pit.id == this.currentBtn.formId) {
            this.$set(this.currentBtn, 'dataModelId', pit.dataModelId)
            this.getPageFieldList(this.infoData.jvsAppId, this.currentBtn.formId)
          }
        })
      }else{
        this.$set(this.currentBtn, 'pageQuery', [])
        this.$set(this.currentBtn, 'dataModelId', '')
        this.$set(this.currentBtn, 'dialogTitle', '')
        this.$set(this.currentBtn, 'dialogWidth', 70)
        this.$set(this.currentBtn, 'pageSearch', [])
        this.$set(this.currentBtn, 'pageBottomBtns', [])
      }
    },
    getPageFieldList (jvsAppId, id) {
      this.pageFieldList = []
      getDesignInfo(jvsAppId, id).then(res => {
        if(res.data.code == 0 && res.data.data) {
          if(res.data.data.viewJson) {
            let viewJson = JSON.parse(res.data.data.viewJson)
            if(viewJson.dataPage && viewJson.dataPage.autoTableFields) {
              this.pageFieldList = viewJson.dataPage.autoTableFields || []
            }
          }
        }
      })
    },
    addQuery () {
      let temp = JSON.parse(JSON.stringify(this.currentBtn.pageQuery))
      temp.push({enabledQueryTypes: ''})
      this.$set(this.currentBtn, 'pageQuery', temp)
      this.$forceUpdate()
    },
    addSearch () {
      let temp = JSON.parse(JSON.stringify(this.currentBtn.pageSearch))
      temp.push({enabledQueryTypes: 'eq', disabled: false, dataFileterList: []})
      this.$set(this.currentBtn, 'pageSearch', temp)
      this.$forceUpdate()
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
    hasPageSearchFilter (item) {
      let bool = false
      if(item instanceof Array) {
        let alldisable = true
        for(let i in item) {
          this.pageFieldList.filter(pit => {
            if(pit.aliasColumnName == item[i].fieldKey) {
              if(['select', 'radio', 'checkbox'].indexOf(pit.designJson.type) > -1) {
                bool = true
              }
            }
            if(!item.disabled) {
              alldisable = false
            }
          })
        }
        if(alldisable) {
          bool = false
        }
      }else{
        this.pageFieldList.filter(pit => {
          if(pit.aliasColumnName == item.fieldKey) {
            if(['select', 'radio', 'checkbox'].indexOf(pit.designJson.type) > -1) {
              bool = true
            }
          }
          if(item.disabled) {
            bool = false
          }
        })
      }
      return bool
    },
    getSearchFilterDom (item) {
      let dom = {}
      this.pageFieldList.filter(pit => {
        if(pit.aliasColumnName == item.fieldKey && pit.designJson) {
          dom = pit.designJson
        }
      })
      console.log(dom)
      return dom
    },
    setPageSearchFilter (item, index) {
      if(!item.dataFileterList) {
        this.$set(item, 'dataFileterList', [])
      }
      this.pageSearchFilterItem = JSON.parse(JSON.stringify(item))
      this.pageSearchFilterIndex = index
      this.pageSearchFilterVisible = true
    },
    addDataFilter () {
      this.pageSearchFilterItem.dataFileterList.push({})
      this.$forceUpdate()
    },
    fieldKeyChange (item) {
      let dom = this.getSearchFilterDom(item)
      if(['select', 'radio', 'checkbox'].indexOf(dom.type) > -1 && dom.datatype == 'option') {
        item.enabledQueryTypes = 'in'
      }
    },
    dataFilterSubmit () {
      // console.log(this.pageSearchFilterItem, this.pageSearchFilterIndex)
      this.$set(this.currentBtn.pageSearch[this.pageSearchFilterIndex], 'dataFileterList', this.pageSearchFilterItem.dataFileterList)
      this.pageSearchFilterClose()
    },
    pageSearchFilterClose () {
      this.pageSearchFilterVisible = false
      this.pageSearchFilterItem = null
      this.pageSearchFilterIndex = -1
    },
    delQuery (index, list) {
      list.splice(index, 1)
      this.$forceUpdate()
    },
    getEnabledQueryTypes (item) {
      let arr = []
      if(item.fieldKey) {
        this.pageFieldList.filter(pif => {
          if(pif.aliasColumnName == item.fieldKey && pif.enabledQueryTypes) {
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
    getAllRuleOfPage () {
      getAllRule(this.infoData.jvsAppId, {dataModelId: this.currentBtn.dataModelId}).then(res => {
        if(res.data && res.data.code == 0) {
          this.allRuleOfPage = res.data.data
          this.$forceUpdate()
        }
      })
    },
    addBottomBtn () {
      let temp = JSON.parse(JSON.stringify(this.currentBtn.pageBottomBtns))
      temp.push({name: '', rule: '', permissionFlag: ('page-button-page-bottomBtn-' + guid())})
      this.$set(this.currentBtn, 'pageBottomBtns', temp)
      this.$forceUpdate()
    },
    pageSetClose () {
      this.pageVisible = false
      this.pageFieldList = []
    },
    newPageHandle () {
      createPage({jvsAppId: this.infoData.jvsAppId}).then(res => {
        if(res.data.code == 0 && res.data.data) {
          this.$set(this.currentBtn, 'formId', res.data.data.id)
          if(res.data.data.dataModelId) {
            this.$set(this.currentBtn, 'dataModelId', res.data.data.dataModelId)
          }
          this.getAllUseHandle()
          this.pageBindChange()
          let str = location.origin + (`/page-design-ui/#/crud/design?jvsAppId=${this.infoData.jvsAppId}&id=`+res.data.data.id + (res.data.data.dataModelId ? `&dataModelId=${res.data.data.dataModelId}` : ''))
          this.$openUrl(str, '_blank')
        }
      })
    },
    designPage (id) {
      if(id) {
        let str = location.origin + (`/page-design-ui/#/crud/design?jvsAppId=${this.infoData.jvsAppId}&id=${id}&dataModelId=${this.currentBtn.dataModelId}`)
        this.$openUrl(str, '_blank')
      }
    },
    createPageRule (item) {
      createRule({jvsAppId: this.infoData.jvsAppId, componentId: this.currentBtn.permissionFlag, name: item.name, designId: this.currentBtn.formId, componentType: 'pageToPage'}).then(res => {
        if (res.data && res.data.code == 0) {
          item.rule = res.data.data
          this.getAllRuleOfPage()
          this.$openUrl(`/rule-design-ui/#/ruleDesign?id=${res.data.data}&componentId=${this.currentBtn.permissionFlag}&jvsAppId=${this.infoData.jvsAppId}&name=${item.name}`, '_blank')
          this.$forceUpdate()
        }
      })
    },
    viewPageRule (item) {
      if(item.rule) {
        this.$openUrl(`/rule-design-ui/#/ruleDesign?id=${item.rule}&componentId=${this.currentBtn.permissionFlag}&jvsAppId=${this.infoData.jvsAppId}&name=${item.name}`, '_blank')
      }
    },
    openUploadTemp () {
      this.tempFileDialogVisible = true
    },
    onTempChange (file, fileList) {
      this.tempfileList = fileList
    },
    errTempHandle (err, file, fileList) {
      this.$refs.tempUploader.clearFiles()
      this.tempfileList = []
      this.$notify({
        title: '提示',
        message: err,
        position: 'bottom-right',
        type: 'error'
      })
    },
    // 文件创建应用成功
    handleAppSuccess (res, fileList) {
      if(res.code == 0) {
        this.$notify({
          title: '提示',
          message: '上传成功',
          position: 'bottom-right',
          type: 'success'
        });
        this.$set(this.currentBtn, 'templateFileLink', res.data.fileLink)
        this.$set(this.currentBtn, 'templateFileName', res.data.originalFileName)
        this.tempFileHandleClose()
      }else{
        this.$refs.tempUploader.clearFiles()
        this.$notify({
          title: '提示',
          message: res.msg,
          position: 'bottom-right',
          type: 'error'
        })
      }
    },
    tempFileHandleClose () {
      this.tempfileList = []
      this.limitShow = false
      this.$refs.tempUploader.clearFiles()
      this.tempFileDialogVisible = false
    },
    delTempFileLink () {
      this.$set(this.currentBtn, 'templateFileLink', '')
      this.$set(this.currentBtn, 'templateFileName', '')
    },
    createEditor (name, domId) {
      this.$nextTick( ()=> {
        if(this[name]){
          this[name].destroy()
          this[name] = null
        }
        this[name] = new E('#'+domId)
        this[name].config.placeholder = '请输入内容...'
        this[name].config.menus = []
        this[name].config.showFullScreen = false
        this[name].config.height= 140
        this[name].config.onchange = (newHtml)=>{
          this.$set(this.currentBtn, 'address', newHtml)
        }
        this[name].create()
        if(this.currentBtn.address) {
          this[name].txt.html(this.currentBtn.address)
        }
      })
    },
    popoverClick (item, editor, visible){
      this[visible] = false
      this[editor].cmd.do('insertHTML', `<span style="background:rgba(126,134,142,.2);font-size:14px;padding:3px 5px;border-radius:5px;" title="${item.fieldKey}">${item.fieldName}</span><span>&nbsp;</span>`)
    },
    emitGetColumn () {
      this.$emit('getColumn', true)
    },
    moveBtnStart (button) {
      this.moveSource = JSON.parse(JSON.stringify(button))
    },
    moveBtning (button) {
      this.moveTarget = JSON.parse(JSON.stringify(button))
    },
    moveBtnEnd () {
      if(this.moveTarget && this.moveTarget.permissionFlag != this.moveSource.permissionFlag) {
        let from = -1
        this.tableData.filter((bit, bix) => {
          if(bit.permissionFlag == this.moveSource.permissionFlag) {
            from = bix
          }
        })
        if(from > -1) {
          this.tableData.splice(from, 1)
          this.$forceUpdate()
        }
        let to = -1
        this.tableData.filter((bit, bix) => {
          if(bit.permissionFlag == this.moveTarget.permissionFlag) {
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
    },
    moveFieldStart (field) {
      this.moveFieldSource = JSON.parse(JSON.stringify(field))
    },
    moveFielding (field) {
      this.moveFieldTarget = JSON.parse(JSON.stringify(field))
    },
    moveFieldEnd () {
      if(this.moveFieldTarget && this.moveFieldTarget.aliasColumnName != this.moveFieldSource.aliasColumnName) {
        let from = -1
        this.currentBtn.exportFields.filter((bit, bix) => {
          if(bit.aliasColumnName == this.moveFieldSource.aliasColumnName) {
            from = bix
          }
        })
        if(from > -1) {
          this.currentBtn.exportFields.splice(from, 1)
          this.$forceUpdate()
        }
        let to = -1
        this.currentBtn.exportFields.filter((bit, bix) => {
          if(bit.aliasColumnName == this.moveFieldTarget.aliasColumnName) {
            to = bix
          }
        })
        if(to > -1) {
          this.currentBtn.exportFields.splice(to, 0, this.moveFieldSource)
          this.$forceUpdate()
        }
      }
      this.moveFieldSource = null
      this.moveFieldTarget = null
    },
    clearHandle () {
      this.selectedRows = []
      this.$set(this.currentBtn, 'exportFields', [])
      this.$refs.exportTable.clearSelect()
    },
    btnTypeChange (button, index) {
      if(['btn_form', 'btn_page'].indexOf(button.type) > -1 && button.formId) {
        this.$set(this.tableData[index], 'formId', '')
      }
      this.emitGetColumn()
    },
  },
  created () {
    this.getConst()
    this.getAllRuleListHandle()
  },
  watch: {
    tableSetNameOption (newVal, oldVal) {
      this.exportSettingOption.column.filter( item => {
        item.dicData = newVal
      })
    },
    'networkForm.type': {
      handler(newVal, oldVal) {
        this.networkOption.column.filter(item => {
          if(item.prop == 'body') {
            if(newVal == 'get') {
              item.display = false
            }else{
              item.display = true
            }
          }
        })
      }
    }
  }
}
</script>
<style lang="scss" scoped>
.icon-info{
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
.add-formula-svg{
  width: 16px;
  height: 16px;
  cursor:pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.button-info{
  height: 100%;
  .page-button-setting-list{
    padding: 0 16px;
    max-height: calc(100% - 64px);
    overflow: hidden;
    overflow-y: auto;
    &::-webkit-scrollbar{
      display: none;
    }
    .page-button-setting-list-item{
      margin-top: 16px;
      position: relative;
      &.open{
        .heade{
          border-radius: 4px 4px 0 0;
        }
      }
      &.target{
        &::after{
          content: '';
          position: absolute;
          top: -8px;
          left: 0;
          width: 100%;
          height: 1px;
          background: #1E6FFF;
        }
      }
      .heade{
        height: 44px;
        background: #F5F6F7;
        border-radius: 4px;
        border: 1px solid #EEEFF0;
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0 16px;
        .name{
          font-family: Source Han Sans-Medium, Source Han Sans;
          font-weight: 500;
          font-size: 14px;
          color: #363B4C;
        }
        .heade-tool{
          display: flex;
          align-items: center;
          svg{
            width: 16px;
            height: 16px;
            fill: #6F7588;
            cursor: pointer;
          }
          .move-icon{
            cursor: move;
          }
          .divider-line{
            display: block;
            width: 1px;
            height: 14px;
            background: #EEEFF0;
            margin: 0 8px;
          }
          .con-box{
            display: flex;
            align-items: center;
          }
        }
      }
      .item-body{
        border: 1px solid #EEEFF0;
        border-top: 0;
        border-radius: 0 0 4px 4px;
        background: #fff;
        padding-bottom: 9px;
        overflow: hidden;
        .item-body-item{
          display: flex;
          align-items: center;
          padding: 0 16px;
          height: 32px;
          margin-top: 9px;
          .label{
            width: 56px;
            margin-right: 16px;
            font-family: Microsoft YaHei-Regular, Microsoft YaHei;
            font-weight: 400;
            font-size: 14px;
            color: #6F7588;
            word-break: keep-all;
          }
          .con{
            width: calc(100% - 72px);
            display: flex;
            align-items: center;
            svg{
              width: 16px;
              height: 16px;
              fill: #6F7588;
              cursor: pointer;
            }
          }
        }
      }
    }
  }
  .add-button-tool{
    margin: 16px;
    height: 32px;
    background: #E4EDFF;
    border-radius: 4px;
    font-family: Microsoft YaHei-Regular, Microsoft YaHei;
    font-weight: 400;
    font-size: 14px;
    color: #1E6FFF;
    text-align: center;
    line-height: 32px;
    cursor: pointer;
    span{
      margin-left: 6px;
    }
  }
}
.design-title-bar {
  position: fixed;
  background: #fff;
  top: 0;
  left: 0;
  z-index: 9;
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-sizing: border-box;
  .el-row {
    .el-select,
    .el-button {
      margin-left: 20px;
    }
  }
}
.design-form-cont{
  margin-top: 10px;
  height: calc(100% - 54px);
  overflow: hidden;
}
.sort-item{
  i{
    cursor: pointer;
  }
}
.custom-edit-table{
  flex: 1;
  .custom-edit-table-header{
    display: flex;
    align-items: center;
    span{
      flex: 1;
      display: block;
      box-sizing: border-box;
      height: 42px;
      background-color: #F6F6F6;
      color: #333333;
      font-size: 14px;
      font-weight: normal;
      border: 0;
      padding: 0 10px;
      line-height: 42px;
    }
    span:nth-last-of-type(1) {
      width: 80px;
      flex: none;
    }
  }
  .custom-edit-table-body{
    .custom-edit-table-body-row{
      display: flex;
      align-items: center;
      border: 1px solid #EBEEF5;
      border-top: 0;
      box-sizing: border-box;
      .cell{
        height: 48px;
        display: flex;
        align-items: center;
        flex: 1;
        padding: 0 10px;
        box-sizing: border-box;
        border-right: 1px solid #EBEEF5;
      }
      .cell:nth-last-of-type(1){
        flex: none;
        width: 80px;
      }
    }
    .custom-edit-table-body-row:hover{
      background: #EFF2F7;
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
/deep/.import-data-box{
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
.popover-list{
  max-height: 200px;
  overflow-y: auto;
  .popover-list-item{
    display: flex;
    align-items: center;
    padding: 5px 10px;
    cursor: pointer;
    font-size: 14px;
    &:hover{
      background-color: #eef1f6;
    }
  }
}
.export-select-dialog{
  /deep/.el-dialog{
    margin-bottom: 15vh;
    height: calc(100vh - 30vh);
    .el-dialog__body{
      height: calc(100% - 48px);
      .dialog-div{
        width: 100%;
        height: 100%;
        padding: 17px 32px 0 32px!important;
        box-sizing: border-box;
        overflow: hidden; 
        .export-body{
          width: 100%;
          height: 100%;
          display: flex;
          .select-multiple-table{
            width: 60%;
            height: 100%;
            border-right: 4px solid #F5F6F7;
            padding-right: 17px;
            padding-bottom: 17px;
            box-sizing: border-box;
            .title-text{
              height: 18px;
              font-family: Microsoft YaHei-Regular, Microsoft YaHei;
              font-weight: 400;
              font-size: 14px;
              color: #363B4C;
              line-height: 18px;
              margin-bottom: 8px;
            }
            .jvs-table{
              height: calc(100% - 26px);
              .table-body-box{
                height: 100%;
                .el-table{
                  border: 0;
                  height: 100%;
                  &::after{
                    display: none;
                  }
                  .el-checkbox{
                    font-size: 16px;
                    .el-checkbox__inner{
                      width: 16px;
                      height: 16px;
                      border-radius: 4px;
                      &::after{
                        top: 1px;
                        width: 4px;
                        height: 8px;
                        left: 4.5px;
                        border-width: 2px;
                      }
                    }
                    .is-indeterminate{
                      .el-checkbox__inner::before{
                        height: 0;
                        width: 10px;
                        border-top: 2px solid #fff;
                        transform: scale(1);
                        left: 2.5px;
                        top: 6px;
                      }
                    }
                  }
                  .el-table__body-wrapper{
                    height: calc(100% - 40px)!important;
                    .el-table__body{
                      tr td{
                        border-right: 0;
                        height: 44px;
                      }
                    }
                    &::-webkit-scrollbar{
                      display: none;
                    }
                  }
                }
              }
            }
          }
          .export-field-list{
            width: 40%;
            height: 100%;
            .drag-title{
              margin-left: 17px;
              padding: 0 8px;
              height: 32px;
              background: #F5F6F7;
              border-radius: 4px;
              display: flex;
              align-items: center;
              justify-content: space-between;
            }
            .drag-list{
              height: calc(100% - 40px);
              overflow: hidden;
              overflow-y: auto;
              .drag-list-item{
                position: relative;
                margin: 12px 0;
                margin-left: 17px;
                padding: 0 10px;
                height: 32px;
                line-height: 32px;
                font-size: 14px;
                font-family: Source Han Sans-Regular, Source Han Sans;
                font-weight: 400;
                color: #6F7588;
                display: flex;
                align-items: center;
                cursor: move;
                div{
                  flex: 1;
                  overflow: hidden;
                  display: flex;
                  align-items: center;
                }
                span{
                  word-break: keep-all;
                }
                .name{
                  color: #363B4C;
                }
                .move-icon{
                  width: 16px;
                  height: 16px;
                  margin-right: 8px;
                  transform: rotate(90deg);
                  fill: #6F7588;
                }
                &:hover{
                  background-color: #EFF2F7;
                }
                &.target{
                  &::after{
                    content: '';
                    position: absolute;
                    top: -4px;
                    left: 0;
                    width: 100%;
                    height: 1px;
                    background: #1E6FFF;
                  }
                }
              }
              &::-webkit-scrollbar{
                display: none;
              }
              &:hover{
                &::-webkit-scrollbar{
                  display: block;
                }
              }
            }
          }
        }
      }
    }
  }
}
</style>
<style lang="scss">
.form-design-no-header-dialog{
  height: 100%;
  overflow: hidden;
  .el-dialog__header{
    display: none!important;
  }
  .el-dialog__body{
    padding: 0;
    height: 100%;
    overflow: hidden;
    padding: 8px 10px;
    background: #f0f2f5;
    box-sizing: border-box;
  }
  .title-page-header{
    margin-top: 0;
    z-index: 99999999;
    position: relative;
  }
  .form-design-tool{
    font-size: 25px;
    cursor: pointer;
    color: #353535;
  }
}
.select-multiple-table{
  .jvs-table{
    .jvs-table-titleTop{
      display: none;
    }
    .table-body-box{
      .el-table{
        margin-top: 0;
        .el-table__header-wrapper{
          margin-top: 0;
        }
        .el-table__body-wrapper{
          height: auto!important;
        }
      }
    }
  }
}
</style>
