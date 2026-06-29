<template>
  <el-card class="attrcont">
    <div v-if="false" class="attrcontzzc" :style="{'z-index':zzcindex}"></div>
    <el-row style="font-size: 14px;margin-bottom:10px;" v-if="(fineGrainedType == 'SAVE' || fineGrainedType == 'EDIT') && (formType == 'normalForm' || formType == 'multiLevelForm')">
      <span style="margin-right: 25px;">关联流程</span>
      <el-switch size="mini" v-model="isFlowableBoolean" active-color="#409EFF" inactive-color="#eee" @change="changeFlowable"></el-switch>
    </el-row>
    <el-row style="font-size: 14px;margin-bottom:10px;" v-if="isFlowableBoolean">
      <el-select size="mini" style="width:100%" v-model="flowableDomString" placeholder="请选择流程" @change="changeFlowable">
        <el-option v-for="ai in availableList" :key="ai.name" :label="ai.name" :value="ai.id"></el-option>
        </el-select>
    </el-row>
    <el-table
      v-if="allForm && allForm.length > 0"
      border
      :data="allForm"
      class="tb-edit"
      style="width: 100%"
      highlight-current-row
      size='mini'
    >
      <el-table-column label="表单名称">
        <template slot-scope="scope">
          <el-input
            size="mini"
            v-model="scope.row.label"
            placeholder="请输入内容"
          ></el-input>
          <span class="el-form-item__error" style="position: unset;" v-if="!scope.row.label">必填</span>
        </template>
      </el-table-column>
      <el-table-column label="表单id">
        <template slot-scope="scope">
          <el-input
            size="mini"
            v-model="scope.row.name"
            placeholder="请输入内容"
            @focus="isActive(scope.row.name)"
            @blur="changeFormId(scope.row.name)"
          ></el-input>
          <span class="el-form-item__error" style="position: unset;" v-if="!scope.row.name">必填</span>
          <span class="el-form-item__error" style="position: unset;" v-if="scope.row.name && isRepeat(scope.row.name)">此id已存在！</span>
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template slot-scope="scope">
          <div>
            <el-button
              v-if="scope.$index > 0"
              size="mini"
              type="text"
              @click="deleteTabForm(scope.$index, scope.row)"
            >删除</el-button>
        </div>
        </template>
      </el-table-column>
    </el-table>
    <el-button v-if="allForm && allForm.length > 0" size='mini' @click="addOneForm" style="margin-top:10px">增加表单</el-button>
    <el-tabs :class="formclass" :stretch="false" v-model="activeName">
      <el-tab-pane label="组件设置" name="1" v-if="form && form.prop">
        <el-collapse v-model="activeAttrs" class="attribute-collapse">
          <el-collapse-item title="基础设置" name="basic" v-if="form.showFrom.indexOf('prop') !== -1 ? true : (['pageTable'].indexOf(form.type) > -1)">
            <el-form :model="form" label-width="88px" label-position="left" size='mini'>
              <!-- 选择其他模型的字段 -->
              <el-form-item v-if="form.showFrom.indexOf('prop') !== -1 && (['formbox', 'tableForm', 'reportTable'].indexOf(form.parentType) > -1)" label="字段绑定" :class="validateBool?'is-error':'copy-prop-item'">
                <span slot="label" class="custom-label-slot-box">
                  <span>字段绑定</span>
                  <el-tooltip class="item" effect="light" :content="attrIntroduce.prop" placement="top">
                    <div class="icon-info">
                      <span>?</span>
                    </div>
                  </el-tooltip>
                </span>
                <el-select v-if="dataItemModelAllFieldList && dataItemModelAllFieldList.length > 0"  style="width:100%" v-model="form.prop" allow-create filterable @change="aliasColumnNameChangeHandle" size="mini">
                  <el-option  v-for='(item,index) in dataItemModelAllFieldList' :key="item.fieldKey + index" :label="item.fieldKey" :value="item.fieldKey">
                    <span style="float: left">{{ item.fieldName }}</span>
                    <span style="float: right; color: #8492a6; font-size: 13px">{{ item.fieldKey}}</span>
                  </el-option>
                </el-select>
                <el-select v-else-if="fieldsdata.length == 0 && tableOption && tableOption.length > 0"  style="width:100%" v-model="form.prop" allow-create filterable @change="aliasColumnNameChangeHandle" size="mini">
                  <el-option  v-for='(item,index) in fieldKeys' :key="item.fieldKey + index" :label="item.fieldKey" :value="item.fieldKey">
                    <span style="float: left">{{ item.fieldName }}</span>
                    <span style="float: right; color: #8492a6; font-size: 13px">{{ item.fieldKey}}</span>
                  </el-option>
                </el-select>
                <el-select v-else-if="fieldsdata.length > 0"  style="width:100%" v-model="form.prop" allow-create filterable @change="keyChangehandle" size="mini" :placeholder="form.prop">
                  <el-option v-for='(item,index) in fieldsdata' :key="index" :label="item.fieldName" :value="item.fieldName">
                    <span style="float: left">{{ item.columnComment }}</span>
                    <span style="float: right; color: #8492a6; font-size: 13px">{{ item.fieldName }}</span>
                  </el-option>
                </el-select>
                <el-input v-else v-model="form.prop" @blur="noKeyWord" size="mini" style="flex: 1;"></el-input>
                <i class="icon-document-copy" @click="copyProp">
                  <svg aria-hidden="true">
                    <use xlink:href="#icon-jvs-fuzhi1"></use>
                  </svg>
                </i>
                <span v-show="validateBool" class="el-form-item__error">字段名称不能为系统关键字</span>
                <span v-show="validateRule && !validateBool" class="el-form-item__error">字段名不能包含空格和特殊符号</span>
              </el-form-item>
              <!-- 字段名称 -->
              <el-form-item v-if="form.showFrom.indexOf('prop') !== -1 && fieldsdata.length == 0 && (['formbox', 'tableForm', 'reportTable'].indexOf(form.parentType) == -1)" label="字段绑定" :class="validateBool?'is-error':'copy-prop-item'">
                <span slot="label" class="custom-label-slot-box">
                  <span>字段绑定</span>
                  <el-tooltip class="item" effect="light" :content="attrIntroduce.prop" placement="top">
                    <div class="icon-info">
                      <span>?</span>
                    </div>
                  </el-tooltip>
                </span>
                <el-select v-if="tableOption && tableOption.length > 0"  style="width:100%" v-model="form.prop" allow-create filterable :placeholder="form.prop" @change="aliasColumnNameChangeHandle" size="mini">
                  <el-option  v-for='(item,index) in fieldKeys' :key="item.fieldKey + index" :label="item.fieldKey" :value="item.fieldKey">
                    <span style="float: left">{{ item.fieldName }}</span>
                    <span style="float: right; color: #8492a6; font-size: 13px">{{ item.fieldKey}}</span>
                  </el-option>
                </el-select>
                <el-input v-else v-model="form.prop" @blur="noKeyWord" size="mini" style="flex: 1;"></el-input>
                <i class="icon-document-copy" @click="copyProp">
                  <svg aria-hidden="true">
                    <use xlink:href="#icon-jvs-fuzhi1"></use>
                  </svg>
                </i>
                <span v-show="validateBool" class="el-form-item__error">字段名称不能为系统关键字</span>
                <span v-show="validateRule && !validateBool" class="el-form-item__error">字段名不能包含空格和特殊符号</span>
              </el-form-item>
              <!-- 字段名称 -->
              <el-form-item v-if="form.showFrom.indexOf('prop') !== -1 && fieldsdata.length > 0 && (['formbox', 'tableForm', 'reportTable'].indexOf(form.parentType) == -1)" label="字段绑定" :class="validateBool?'is-error':'copy-prop-item'">
                <span slot="label" class="custom-label-slot-box">
                  <span>字段绑定</span>
                  <el-tooltip class="item" effect="light" :content="attrIntroduce.prop" placement="top">
                    <div class="icon-info">
                      <span>?</span>
                    </div>
                  </el-tooltip>
                </span>
                <el-select  style="width:100%" v-model="form.prop" allow-create filterable @change="keyChangehandle" size="mini" :placeholder="form.prop">
                  <el-option  v-for='(item,index) in fieldsdata' :key="index" :label="item.fieldName" :value="item.fieldName">
                    <span style="float: left">{{ item.columnComment }}</span>
                    <span style="float: right; color: #8492a6; font-size: 13px">{{ item.fieldName }}</span>
                  </el-option>
                </el-select>
                <i class="icon-document-copy" @click="copyProp">
                  <svg aria-hidden="true">
                    <use xlink:href="#icon-jvs-fuzhi1"></use>
                  </svg>
                </i>
                <span v-show="validateBool" class="el-form-item__error">字段名称不能为系统关键字</span>
                <span v-show="validateRule" class="el-form-item__error">字段名不能包含空格和特殊符号</span>
              </el-form-item>
              <!-- label -->
              <el-form-item v-if="form.showFrom.indexOf('label') !== -1 || form.type == 'iframe'" label="中文名">
                <el-input v-model="form.label" size="mini" @change="changeLabelHandle"></el-input>
              </el-form-item>
              <el-form-item v-if="form.showFrom.indexOf('disabled') !== -1 && !isDetail" label="默认状态">
                <el-radio-group v-model="form.status" size="mini" @change="statusChange" class="radio-group-row">
                  <el-radio-button label="">普通</el-radio-button>
                  <el-radio-button label="disabled">只读
                    <el-tooltip class="item" effect="light" :content="attrIntroduce.status.disabled" placement="top">
                      <div class="icon-info">
                        <span>?</span>
                      </div>
                    </el-tooltip>
                  </el-radio-button>
                  <el-radio-button v-if="!(form.parentType == 'tableForm')" label="hide">隐藏
                    <el-tooltip class="item" effect="light" :content="attrIntroduce.status.hide" placement="top">
                      <div class="icon-info">
                        <span>?</span>
                      </div>
                    </el-tooltip>
                  </el-radio-button>
                </el-radio-group>
              </el-form-item>
              <!-- 数据类型 -->
              <el-form-item v-if="form.showFrom.indexOf('datatype') !== -1 && ['step', 'tab', 'reportTable'].indexOf(form.type) == -1" label="数据来源">
                <el-select style="width:100%" v-model="form.datatype" placeholder="请选择数据来源" size="mini" @change="dataTypeChange">
                  <el-option v-if="form.type != 'cascader'" label="配置数据" value="option"></el-option>
                  <!-- <el-option v-if="form.type != 'cascader'" label="接口数据" value="url"></el-option> -->
                  <el-option v-if="form.type == 'cascader'" label="系统字典" value="system"></el-option>
                  <el-option label="数据模型" value="dataModel"></el-option>
                  <el-option v-if="form.type == 'select'" label="OA流程" value="flowable"></el-option>
                  <el-option v-if="form.type == 'checkbox' || (form.type == 'select' && form.multiple)" label="动态组件" value="dom"></el-option>
                  <el-option label="逻辑引擎" value="rule"></el-option>
                </el-select>
              </el-form-item>
              <div v-if="form.showFrom.indexOf('datatype') !== -1" class="left-line-div">
                <!-- option -->
                <div v-if="form.showFrom.indexOf('option') !== -1 && form.datatype == 'option'" class="place-table-box">
                  <div class="place-table-box-title">
                    <div class="index-item"></div>
                    <div class="title-item">显示值</div>
                    <div class="title-item">传递值
                      <el-tooltip class="item" effect="light" content="传递值均为字符串" placement="top">
                        <div class="icon-info">
                          <span>?</span>
                        </div>
                      </el-tooltip>
                    </div>
                    <div class="svg-icon-item">
                      <svg aria-hidden="true" @click="addoption">
                        <use xlink:href="#icon-jvs-a-tianjia1x"></use>
                      </svg>
                    </div>
                  </div>
                  <div class="place-table-box-content">
                    <div v-for="(row, rix) in form.dicData" :key="form.prop +'-dic-item-'+ rix" class="place-table-box-content-item">
                      <div class="index-item">
                        <svg aria-hidden="true">
                          <use xlink:href="#jvs-ui-icon-tuodong1"></use>
                        </svg>
                      </div>
                      <div class="title-item">
                        <el-input size="mini" v-model="row.label" placeholder="请输入内容" @change="optionLabelChange(scope.row)"></el-input>
                      </div>
                      <div class="title-item title-item2">
                        <el-input
                          v-if="['tab', 'step'].indexOf(form.type) > -1"
                          size="mini"
                          v-model="row.name"
                          placeholder="请输入内容"
                          @change="tabNameChangeHandle(row.name)"
                        ></el-input>
                        <el-input
                          v-else
                          size="mini"
                          v-model="row.value"
                          placeholder="请输入内容"
                        ></el-input>
                      </div>
                      <div class="svg-icon-item" @click="handleDelete(rix, row)">
                        <span class="del-icon"></span>
                      </div>
                    </div>
                  </div>
                </div>
                <!-- dataModel -->
                <el-form-item v-if="form.showFrom.indexOf('url') !== -1 && form.datatype === 'dataModel'" label="关联模型">
                  <el-select
                    style="width:100%"
                    v-model="form.formId" placeholder="请选择数据模型" size="mini"
                    @change="changeModelPropList(form.formId)"
                    filterable
                    clearable
                  >
                    <el-option
                      v-for="item in dataModelList"
                      :key="item.id"
                      :label="item.name"
                      :value="item.id">
                    </el-option>
                  </el-select>
                </el-form-item>
                <el-form-item v-if="form.showFrom.indexOf('url') !== -1 && form.datatype === 'dataModel'" label="显示值" >
                  <el-select
                    style="width:100%"
                    v-model="form.props.label" placeholder="请选择显示值" size="mini"
                    @change="setConnectFormDataUrl"
                  >
                    <el-option
                      v-for="(item, index) in dataModelAllFieldList"
                      :key="'props-label-connect-show-'+item.fieldKey+'-'+index"
                      :label="item.fieldName"
                      :value="item.fieldKey">
                    </el-option>
                  </el-select>
                </el-form-item>
                <el-form-item v-if="form.datatype === 'dataModel'" label="传递值" >
                  <el-select
                    style="width:100%"
                    v-model="form.props.value" placeholder="请选择传递值" size="mini"
                    @change="setConnectFormDataUrl"
                    :disabled="!form.searchable && form.type != 'cascader'"
                  >
                    <el-option
                      v-for="(item, index) in dataModelAllFieldList"
                      :key="'props-value-connect-show-'+item.fieldKey+'-'+index"
                      :label="item.fieldName"
                      :value="item.fieldKey">
                    </el-option>
                  </el-select>
                </el-form-item>
                <el-form-item v-if="form.showFrom.indexOf('url') !== -1 && form.datatype === 'dataModel' && ['select', 'cascader'].indexOf(form.type) > -1" :label="form.type == 'cascader' ? '父级字段' : '副标题'">
                  <el-select v-model="form.props.secTitle" :placeholder="form.type == 'cascader' ? '请选择父级字段' : '请选择副标题'" :clearable="form.type != 'cascader'" size="mini" style="width:100%">
                    <el-option
                      v-for="(item, index) in dataModelAllFieldList"
                      :key="'props-sectitle-connect-show-'+item.fieldKey+'-'+index"
                      :label="item.fieldName"
                      :value="item.fieldKey">
                    </el-option>
                  </el-select>
                </el-form-item>
                <!-- --数据筛选 -->
                <el-form-item v-if="form.showFrom.indexOf('url') !== -1 && form.datatype === 'dataModel' && form.formId" class="line-item">
                  <span slot="label" class="custom-label-slot-box">
                    <span>数据筛选</span>
                    <el-tooltip class="item" effect="light" :content="attrIntroduce.dataFilterable" placement="top">
                      <div class="icon-info">
                        <span>?</span>
                      </div>
                    </el-tooltip>
                  </span>
                  <div>
                    <div>
                      <el-checkbox v-model="form.dataFilterable"></el-checkbox>
                    </div>
                    <div v-if="form.showFrom.indexOf('url') !== -1 && form.datatype === 'dataModel' && form.dataFilterable" style="margin-top: 8px;">
                      <div class="form-icon-btn" @click="dataFilterHandle">
                        <span>设置条件</span>
                      </div>
                    </div>
                  </div>
                </el-form-item>
                <!-- url -->
                <el-form-item v-if="form.showFrom.indexOf('url') !== -1 && form.datatype === 'url'" label="接口地址">
                  <el-input v-model="form.url" size="mini"></el-input>
                </el-form-item>
                <!-- rule -->
                <el-form-item v-if="form.showFrom.indexOf('url') !== -1 && form.datatype === 'rule'" label="数据逻辑">
                  <div class="form-icon-btn" @click="setHttpHandle('optionHttp')">
                    <span>逻辑配置</span>
                  </div>
                </el-form-item>
                <el-form-item v-if="form.showFrom.indexOf('url') !== -1 && form.datatype === 'rule'" label="触发条件">
                  <el-select v-model="form.ruleOptionDom" placeholder="请选择表单字段" multiple clearable size="mini" style="width: 100%;" class="custom-multiple-select">
                    <el-option  v-for='(rt,rx) in formulaEnableDom' :key="rt.prop + '-option-rule-dom-' + rx" :label="rt.label" :value="rt.prop">
                      <span style="float: left">{{ rt.label }}</span>
                      <span style="float: right; color: #8492a6; font-size: 13px">{{ rt.prop}}</span>
                    </el-option>
                  </el-select>
                </el-form-item>
                <el-form-item v-if="form.showFrom.indexOf('url') !== -1 && ['url', 'rule'].indexOf(form.datatype) > -1" label="显示值">
                  <div class="el-input el-input--mini">
                    <input class="el-input__inner" size="mini" v-bind:value="form.props.label" v-on:input="oninput($event.target.value, 'label')">
                  </div>
                </el-form-item>
                <el-form-item v-if="form.showFrom.indexOf('url') !== -1 && ['url', 'rule'].indexOf(form.datatype) > -1" label="传递值">
                  <div class="el-input el-input--mini">
                    <input class="el-input__inner" size="mini" v-bind:value="form.props.value" v-on:input="oninput($event.target.value, 'value')">
                  </div>
                </el-form-item>
                <!-- 级联-系统字典 -->
                <el-form-item v-if="form.showFrom.indexOf('cascaderOption') !== -1 && form.datatype == 'system'" label="选择数据">
                  <el-select style="width:100%" v-model="form.dictName" placeholder="请选择数据" size="mini" filterable>
                    <el-option v-for="c in classifyDictList" :key="(c.extend ? c.extend.uniqueName : c.name) + 'cascader-item'" :label="c.name" :value="c.extend ? c.extend.uniqueName : c.name"></el-option>
                  </el-select>
                </el-form-item>
                <!-- 动态组件 -->
                <el-form-item v-if="form.datatype === 'dom'" label="关联模型">
                  <el-select
                    style="width:100%"
                    v-model="form.formId" placeholder="请选择数据模型" size="mini"
                    filterable
                    clearable
                    @change="domBindChange"
                  >
                    <el-option
                      v-for="item in dataModelList"
                      :key="item.id"
                      :label="item.name"
                      :value="item.id">
                    </el-option>
                  </el-select>
                </el-form-item>
                <!-- 查询文本展示字段 -->
                <el-form-item v-if="form.datatype == 'dom' && dataModelAllFieldList && dataModelAllFieldList.length > 0" label="展示字段">
                  <el-select
                    style="width:100%"
                    v-model="form.others" placeholder="请选择展示字段" size="mini"
                    multiple
                    class="custom-multiple-select"
                  >
                    <el-option
                      v-for="(item, index) in dataModelAllFieldList"
                      :key="'others-connect-show-'+index+'-'+item.fieldKey"
                      :label="item.fieldName"
                      :value="item.fieldKey">
                    </el-option>
                  </el-select>
                </el-form-item>
              </div>
              <!-- 级联选择 -->
              <el-form-item v-if="form.showFrom.indexOf('cascaderOption') !== -1" label="展示类型">
                <el-select style="width:100%" v-model="form.pickType" placeholder="请选择数据" size="mini">
                  <el-option label="级联选择器" value="cascader"></el-option>
                  <el-option label="树形选择器" value="tree"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item v-if="form.showFrom.indexOf('cascaderOption') !== -1">
                <span slot="label" class="custom-label-slot-box">
                  <span>过滤对照</span>
                  <el-tooltip class="item" effect="light" content="来源数据项中与该字段值相等时，本身及子集不可被选中" placement="top">
                    <div class="icon-info">
                      <span>?</span>
                    </div>
                  </el-tooltip>
                </span>
                <el-select v-if="fieldsdata && fieldsdata.length > 0"  style="width:100%" v-model="form.filterProp" allow-create filterable size="mini" clearable placeholder="请选择数据过滤对照字段">
                  <el-option label="id" value="id" v-show="form.prop != 'id'">
                    <span style="float: left">id</span>
                    <span style="float: right; color: #8492a6; font-size: 13px">id</span>
                  </el-option>
                  <el-option  v-for="(item,index) in fieldsdata" :key="index" :label="item.fieldName" :value="item.fieldName" v-show="item.fieldName != form.prop">
                    <span style="float: left">{{ item.columnComment }}</span>
                    <span style="float: right; color: #8492a6; font-size: 13px">{{ item.fieldName }}</span>
                  </el-option>
                </el-select>
                <el-select v-if="(!fieldsdata || fieldsdata.length == 0) && tableOption && tableOption.length > 0" style="width:100%" v-model="form.filterProp" allow-create filterable clearable placeholder="请选择数据过滤对照字段" size="mini">
                  <el-option label="id" value="id" v-show="form.prop != 'id'">
                    <span style="float: left">id</span>
                    <span style="float: right; color: #8492a6; font-size: 13px">id</span>
                  </el-option>
                  <el-option  v-for="(item,index) in fieldKeys" :key="item.fieldKey + index" :label="item.fieldKey" :value="item.fieldKey" v-show="item.fieldKey != form.prop">
                    <span style="float: left">{{ item.fieldName }}</span>
                    <span style="float: right; color: #8492a6; font-size: 13px">{{ item.fieldKey}}</span>
                  </el-option>
                </el-select>
              </el-form-item>
              <!-- 流水号 -->
              <el-form-item v-if="form.showFrom.indexOf('orderPrefix') !== -1" label="前缀">
                <el-input type="textarea" v-model="form.orderPrefix" size="mini" maxlength="32" show-word-limit :rows="4"></el-input>
              </el-form-item>
              <el-form-item v-if="form.showFrom.indexOf('orderTimeMark') !== -1" label="时间标识">
                <el-select style="width:100%" v-model="form.orderTimeMark" filterable size="mini" clearable>
                  <el-option label="不设置" value="n"></el-option>
                  <el-option label="年" value="y"></el-option>
                  <el-option label="年月" value="ym"></el-option>
                  <el-option label="年月日" value="ymd"></el-option>
                  <el-option label="年月日时" value="ymdh"></el-option>
                  <el-option label="年月日时分" value="ymdhm"></el-option>
                  <el-option label="年月日时分秒" value="ymdhms"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item v-if="form.showFrom.indexOf('orderDigit') !== -1" label="序号位数">
                <el-input-number style="width: 100%;" :min="1" :max="9" v-model="form.orderDigit" size="mini"></el-input-number>
              </el-form-item>
              <el-form-item v-if="form.showFrom.indexOf('orderResetRule') !== -1" label="重置规则">
                <el-select style="width:100%" v-model="form.orderResetRule" filterable size="mini" clearable>
                  <el-option label="不重置" value="n"></el-option>
                  <el-option label="按年重置" value="y"></el-option>
                  <el-option label="按月重置" value="m"></el-option>
                  <el-option label="按天重置" value="d"></el-option>
                  <el-option label="按小时重置" value="h"></el-option>
                </el-select>
              </el-form-item>
              <!-- 内嵌列表页 -->
              <el-form-item v-if="form.type == 'pageTable'" label="选择列表页">
                <el-select style="width:100%" v-model="form.formId" filterable size="mini" @change="pageTableIdChange">
                  <el-option  v-for='(item,index) in pageList' :key="'children-formOption-item'+index" :label="item.name" :value="item.id"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item v-if="form.type == 'pageTable' && form.formId" label="列表设置">
                <div class="form-icon-btn" @click="pageSetting">
                  <span>列表设置</span>
                </div>
              </el-form-item>
              <!-- 时间线 流向字段、内容设置 -->
              <el-form-item v-if="form.type == 'timeline'" label="时间字段">
                <el-input v-model="form.timestamp" size="mini"></el-input>
              </el-form-item>
              <el-form-item v-if="form.type == 'timeline'" label="内容">
                <el-input v-model="form.content" size="mini"></el-input>
              </el-form-item>






              <!-- 关联的表单 -->
              <el-form-item v-if="form.type === 'connectForm'" label="关联表单">
                <el-select
                  style="width:100%"
                  v-model="form.formId" placeholder="请选择表单" size="mini"
                  @change="changeOriginFormId"
                  filterable
                >
                  <el-option
                    v-for="item in connectFormOption"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id">
                  </el-option>
                </el-select>
              </el-form-item>
              <!-- 选择关联后表单显示和传递的值 -->
              <el-form-item v-if="form.type === 'connectForm' && form.formId && connectFormPropsList && connectFormPropsList.length > 0" label="显示值" >
                <el-select
                  style="width:100%"
                  v-model="form.props.label" placeholder="请选择显示值" size="mini"
                  @change="setConnectFormDataUrl"
                >
                  <el-option
                    v-for="(item, index) in connectFormPropsList"
                    :key="'props-label-connect-show-'+item.fieldKey+'-'+index"
                    :label="item.fieldName"
                    :value="item.fieldKey">
                  </el-option>
                </el-select>
              </el-form-item>
              <!-- 选择关联后表单扩展显示字段 -->
              <el-form-item v-if="form.type === 'connectForm' && form.formId && connectFormPropsList && connectFormPropsList.length > 0" label="扩展显示" >
                <el-select
                  style="width:100%"
                  v-model="form.others" placeholder="请选择显示值" size="mini"
                  multiple
                  @change="setConnectFormColumn"
                >
                  <el-option
                    v-for="(item, index) in connectFormPropsList"
                    :key="'others-connect-show-'+item.fieldKey+'-'+index"
                    v-if="item.fieldKey != form.props.label"
                    :label="item.fieldName"
                    :value="item.fieldKey">
                  </el-option>
                </el-select>
              </el-form-item>
              <!-- 子表单 -->
              <el-form-item v-if="form.type == 'childrenForm'" label="选择表单">
                <el-select style="width:100%" v-model="form.formId" filterable size="mini">
                  <el-option  v-for='(item,index) in connectFormOption' :key="'children-formOption-item'+index" :label="item.name" :value="item.id"></el-option>
                </el-select>
              </el-form-item>
            </el-form>
          </el-collapse-item>
        </el-collapse>
        <div class="border-top-line"></div>
        <el-collapse v-model="activeAttr" class="attribute-collapse" accordion>
          <el-collapse-item title="扩展功能" name="extend" v-if="['divider', 'p','button', 'image', 'file', 'box', 'link', 'iframe', 'serialNumber', 'positionMap', 'signature', 'pageTable', 'bluetoothBeacon', 'timeline', 'flowNode', 'dynamicForm'].indexOf(form.type) == -1">
            <el-form :model="form" label-width="88px" label-position="left" size='mini'>
              <el-form-item v-if="form.showFrom.indexOf('showpassword') !== -1" label="密码模式" class="line-item">
                <el-checkbox v-model="form.showpassword"></el-checkbox>
              </el-form-item>
              <el-form-item v-if="form.showFrom.indexOf('clearable') !== -1 && form.type !== 'textarea'" label="快速清空" class="line-item">
                <el-checkbox v-model="form.clearable"></el-checkbox>
              </el-form-item>
              <el-form-item v-if="form.showFrom.indexOf('showwordlimit') !== -1" label="字数统计" class="line-item">
                <el-checkbox v-model="form.showwordlimit"></el-checkbox>
              </el-form-item>
              <!-- 下拉框 -->
              <el-form-item v-if="false && '暂时屏蔽' && form.showFrom.indexOf('allowcreate') !== -1 && form.filterable" label="创建选项" class="line-item">
                <el-checkbox v-model="form.allowcreate"></el-checkbox>
              </el-form-item>
              <!-- 下拉框  搜索文本 -->
              <el-form-item v-if="form.showFrom.indexOf('multiple') !== -1 || (form.type == 'input' && form.searchable)" label="多选控制" class="line-item">
                <el-checkbox v-model="form.multiple" :disabled="form.sqlType == 'enum' || form.datatype == 'flowable'" @change="multipleChange"></el-checkbox>
              </el-form-item>
              <el-form-item v-if="form.showFrom.indexOf('collapsetags') !== -1 && form.multiple && ['user', 'role', 'department', 'group', 'job'].indexOf(form.type) == -1" class="line-item">
                <span slot="label" class="custom-label-slot-box">
                  <span>全部展示</span>
                  <el-tooltip class="item" effect="light" :content="attrIntroduce.collapsetags" placement="top">
                    <div class="icon-info">
                      <span>?</span>
                    </div>
                  </el-tooltip>
                </span>
                <el-checkbox v-model="form.collapsetags"></el-checkbox>
              </el-form-item>
              <el-form-item v-if="form.showFrom.indexOf('filterable') !== -1" label="输入搜索" class="line-item">
                <el-checkbox v-model="form.filterable"></el-checkbox>
              </el-form-item>
              <el-form-item v-if="form.type == 'inputNumber'" label="千分位" class="line-item">
                <el-checkbox v-model="form.thoudsandthable"></el-checkbox>
              </el-form-item>
              <!-- 上传组件 -->
              <el-form-item v-if="['imageUpload', 'fileUpload'].indexOf(form.type) > -1" label="上传路径">
                <el-input v-model="form.uploadModule" size="mini"></el-input>
              </el-form-item>
              <!-- 人员选择  设置选择范围 -->
              <el-form-item label="范围设置" prop="formula" v-if="['user', 'dept', 'department'].indexOf(form.type) > -1">
                <div class="form-icon-btn" @click="userRangeHandle">
                  <span>选择范围</span>
                </div>
              </el-form-item>
              <el-form-item v-if="form.rules && form.rules[0] && !form.rules[0].required" label="置空回写" class="line-item">
                <el-checkbox v-model="form.emptyEnable"></el-checkbox>
                <div style="width: 100%;font-family: Microsoft YaHei-Regular, Microsoft YaHei;font-weight: 400;font-size: 12px;color: #6F7588;line-height: 16px;">注：如果字段中存在内容清空后，会将空值回写模型！</div>
              </el-form-item>
              <el-form-item v-if="form.showFrom.indexOf('maxlength') !== -1" label="最大字数">
                <div class="line-number-unit" style="">
                  <!-- :max="2147483647" -->
                  <el-input-number :min="0" v-model="form.maxlength" size="mini" :controls="false"></el-input-number>
                  <div class="unit" style="">字</div>
                </div>
              </el-form-item>
              <el-form-item v-if="form.showFrom.indexOf('label') !== -1 && ['tab', 'tableForm', 'flowTable'].indexOf(form.type) > -1" label="隐藏标签" class="line-item">
                <el-checkbox v-model="form.hideLabel"></el-checkbox>
              </el-form-item>
              <!-- 选项卡 -->
              <el-form-item v-if="false && ['tab'].indexOf(form.type) > -1" class="line-item">
                <span slot="label" class="custom-label-slot-box">
                  <span>脱离数据</span>
                  <el-tooltip class="item" effect="light" content="脱离数据后选项卡组件本身将作为独立容器，不参与表单数据交互" placement="top">
                    <div class="icon-info">
                      <span>?</span>
                    </div>
                  </el-tooltip>
                </span>
                <el-checkbox v-model="form.detachData"></el-checkbox>
              </el-form-item>
              <el-form-item v-if="['tab'].indexOf(form.type) > -1" label="展示方式">
                <el-radio-group v-model="form.status" size="mini" class="radio-group-row">
                  <el-radio-button label="">选项卡</el-radio-button>
                  <el-radio-button label="collapse">折叠面板</el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-form-item v-if="['tab'].indexOf(form.type) > -1 && form.status == 'collapse'" class="line-item">
                <span slot="label" class="custom-label-slot-box">
                  <span>手风琴模式</span>
                  <el-tooltip class="item" effect="light" content="每次只能展开一个面板" placement="top">
                    <div class="icon-info">
                      <span>?</span>
                    </div>
                  </el-tooltip>
                </span>
                <el-checkbox v-model="form.accordion"></el-checkbox>
              </el-form-item>
              <!-- 表格组件 -->
              <el-form-item v-if="form.type == 'tableForm' && !isDetail" label="新增按钮" class="line-item">
                <el-checkbox v-model="form.addBtn"></el-checkbox>
              </el-form-item>
              <div v-if="form.type == 'tableForm' && !isDetail && form.addBtn" class="left-line-div">
                <el-form-item label="按钮名称">
                  <el-input v-model="form.addBtnText"></el-input>
                </el-form-item>
                <el-form-item label="操作方式">
                  <el-radio-group v-model="form.addBtnOrigin" size="mini" class="radio-group-row">
                    <el-radio-button label="">普通
                      <el-tooltip class="item" effect="light" content="直接新增一条数据" placement="top">
                        <div class="icon-info">
                          <span>?</span>
                        </div>
                      </el-tooltip>
                    </el-radio-button>
                    <el-radio-button label="form">表单
                      <el-tooltip class="item" effect="light" content="打开一个表单填写后新增一条数据" placement="top">
                        <div class="icon-info">
                          <span>?</span>
                        </div>
                      </el-tooltip>
                    </el-radio-button>
                    <el-radio-button label="table">选择
                      <el-tooltip class="item" effect="light" content="打开一个列表选择数据后新增" placement="top">
                        <div class="icon-info">
                          <span>?</span>
                        </div>
                      </el-tooltip>
                    </el-radio-button>
                  </el-radio-group>
                </el-form-item>
                <el-form-item v-if="form.addBtnOrigin == 'form'">
                  <div class="form-icon-btn" @click="designBtnForm('add')">
                    <span>设计表单</span>
                    <i v-if="form.addBtnFormCode" class="el-icon-delete" style="margin-left: 5px;color: #FF194C;" @click.stop="deleteFormCode('add')"></i>
                  </div>
                </el-form-item>
                <div v-if="form.addBtnOrigin == 'table'">
                  <el-form-item label="关联模型">
                    <el-select
                      style="width:100%"
                      v-model="form.addBtnFormId" placeholder="请选择数据模型" size="mini"
                      @change="changeModelPropList(form.addBtnFormId, 'openFormDataMModelField')"
                      filterable
                      clearable
                    >
                      <el-option
                        v-for="item in dataModelList"
                        :key="item.id"
                        :label="item.name"
                        :value="item.id">
                      </el-option>
                    </el-select>
                  </el-form-item>
                  <!-- 查询文本展示字段 -->
                  <el-form-item v-if="openFormDataMModelField && openFormDataMModelField.length > 0" label="展示字段" >
                    <el-select v-model="form.others" placeholder="请选择展示字段" size="mini" multiple class="custom-multiple-select" style="width:100%">
                      <el-option
                        v-for="(item, index) in openFormDataMModelField"
                        :key="'others-connect-show-'+item.fieldKey+'-'+index"
                        :label="item.fieldName"
                        :value="item.fieldKey">
                      </el-option>
                    </el-select>
                  </el-form-item>
                  <!-- 查询文本条件字段 -->
                  <el-form-item v-if="openFormDataMModelField && openFormDataMModelField.length > 0" label="查询条件" >
                    <el-select v-model="form.othersQuery" placeholder="请选择查询条件" size="mini" multiple class="custom-multiple-select" style="width:100%">
                      <el-option
                        v-for="(item, index) in openFormDataMModelField"
                        :key="'others-query-connect-show-'+item.fieldKey+'-'+index"
                        :label="item.fieldName"
                        :value="item.fieldKey">
                      </el-option>
                    </el-select>
                  </el-form-item>
                  <!-- 数据筛选 -->
                  <el-form-item class="line-item">
                    <span slot="label" class="custom-label-slot-box">
                      <span>数据筛选</span>
                      <el-tooltip class="item" effect="light" :content="attrIntroduce.dataFilterable" placement="top">
                        <div class="icon-info">
                          <span>?</span>
                        </div>
                      </el-tooltip>
                    </span>
                    <div>
                      <div>
                        <el-checkbox v-model="form.addBtnDataFilterable"></el-checkbox>
                      </div>
                      <div v-if="form.addBtnDataFilterable" style="margin-top: 8px;">
                        <div class="form-icon-btn" @click="openFormFilterHandle">
                          <span>设置条件</span>
                        </div>
                      </div>
                    </div>
                  </el-form-item>
                  <!-- 打开选择是否跳过权限 -->
                  <el-form-item label="跳过权限" class="line-item">
                    <el-checkbox v-model="form.nopermission"></el-checkbox>
                  </el-form-item>
                  <!-- end 搜索文本 -->
                </div>
                <el-form-item v-if="form.addBtnOrigin && (form.addBtnOrigin == 'form' ? (!form.addBtnFormCode) : true)" label="显示宽度">
                  <div style="width: 100%;display: flex;align-items: center;">
                    <el-slider v-model="form.addPopupWidth" :min="10" :max="100" :step="1" :show-input="false" :range="false" style="flex: 1;"></el-slider>
                    <span class="slider-end-unit">{{form.addPopupWidth}}%</span>
                  </div>
                </el-form-item>
              </div>
              <el-form-item v-if="form.type == 'tableForm' && !isDetail" label="删除按钮" class="line-item">
                <el-checkbox v-model="form.delBtn"></el-checkbox>
              </el-form-item>
              <div class="left-line-div" v-if="form.type == 'tableForm' && !isDetail && form.delBtn">
                <el-form-item  label="按钮名称">
                  <el-input v-model="form.delBtnText"></el-input>
                </el-form-item>
              </div>
              <el-form-item v-if="form.type == 'tableForm' && form.addBtnOrigin != 'table'" label="行内编辑" class="line-item">
                <el-checkbox v-model="form.editable"></el-checkbox>
              </el-form-item>
              <el-form-item v-if="form.type == 'tableForm' && form.addBtnOrigin != 'table' && !form.editable" label="编辑按钮" class="line-item">
                <el-checkbox v-model="form.editBtn"></el-checkbox>
              </el-form-item>
              <div class="left-line-div" v-if="form.type == 'tableForm' && form.addBtnOrigin != 'table' && !form.editable && form.editBtn">
                <el-form-item label="按钮名称">
                  <el-input v-model="form.editBtnText"></el-input>
                </el-form-item>
                <el-form-item>
                  <div class="form-icon-btn" @click="designBtnForm('edit')">
                    <span>设计表单</span>
                    <i v-if="form.editBtnFormCode" class="el-icon-delete" style="margin-left: 5px;color: #FF194C;" @click.stop="deleteFormCode('edit')"></i>
                  </div>
                </el-form-item>
                <el-form-item v-if="!form.editBtnFormCode" label="显示宽度">
                  <div style="width: 100%;display: flex;align-items: center;">
                    <el-slider v-model="form.editPopupWidth" :min="10" :max="100" :step="1" :show-input="false" :range="false" style="flex: 1;"></el-slider>
                    <span class="slider-end-unit">{{form.editPopupWidth}}%</span>
                  </div>
                </el-form-item>
              </div>
              <el-form-item v-if="form.type == 'tableForm' && form.addBtnOrigin != 'table' && !form.editable" label="详情按钮" class="line-item">
                <el-checkbox v-model="form.viewBtn"></el-checkbox>
              </el-form-item>
              <div class="left-line-div" v-if="form.type == 'tableForm' && form.addBtnOrigin != 'table' && !form.editable && form.viewBtn">
                <el-form-item label="按钮名称">
                  <el-input v-model="form.viewBtnText"></el-input>
                </el-form-item>
                <el-form-item>
                  <div class="form-icon-btn" @click="designBtnForm('view')">
                    <span>设计表单</span>
                    <i v-if="form.viewBtnFormCode" class="el-icon-delete" style="margin-left: 5px;color: #FF194C;" @click.stop="deleteFormCode('view')"></i>
                  </div>
                </el-form-item>
                <el-form-item v-if="!form.viewBtnFormCode" label="显示宽度">
                  <div style="width: 100%;display: flex;align-items: center;">
                    <el-slider v-model="form.viewPopupWidth" :min="10" :max="100" :step="1" :show-input="false" :range="false" style="flex: 1;"></el-slider>
                    <span class="slider-end-unit">{{form.viewPopupWidth}}%</span>
                  </div>
                </el-form-item>
              </div>
            </el-form>
          </el-collapse-item>
          <el-collapse-item title="交互设置" name="eachother">
            <el-collapse v-model="eachotherActives" class="inner-collapse">
              <el-collapse-item name="setvalue" v-if="['divider', 'p', 'iconSelect', 'box', 'imageUpload', 'fileUpload', 'tab', 'button', 'serialNumber', 'positionMap', 'signature', 'jsonEditor', 'pageTable', 'bluetoothBeacon', 'timeline', 'dynamicForm'].indexOf(form.type) == -1">
                <template slot="title">
                  <div class="slot-title-div">
                    <div class="title">组件赋值</div>
                    <div class="line"></div>
                  </div>
                </template>
                <el-form :model="form" label-width="88px" label-position="left" size='mini'>
                  <!-- 扩展组件默认值 -->
                  <el-form-item v-if="form.showFrom.indexOf('isDefault') !== -1 || ['role', 'job'].indexOf(form.type) > -1" label="默认取值" class="line-item">
                    <div style="display: flex;align-items: center;">
                      <el-checkbox v-model="form.isDefault" @change="isDefaultChange"></el-checkbox>
                      <i v-if="!form.isDefault" class="after-i-icon" @click="setFuction">
                        <svg aria-hidden="true">
                          <use :xlink:href="`#${form.formula ? 'icon-jvs-a-zu10923' : 'icon-jvs-rongqi1'}`"></use>
                        </svg>
                      </i>
                    </div>
                  </el-form-item>
                  <!-- 日期默认值 -->
                  <el-form-item v-if="form.showFrom.indexOf('defaultDate') !== -1" label="默认取值" class="line-item">
                    <el-checkbox v-model="form.defaultDate"></el-checkbox>
                  </el-form-item>
                  <!-- 默认值 -->
                  <el-form-item v-if="form.showFrom.indexOf('defaultValue') !== -1 && formulaEnableList.indexOf(form.type) > -1" label="默认取值">
                    <div class="line-item-div">
                      <el-select v-model="form.defaultOrigin" size="mini" placeholder="请选择" @change="defaultOriginChange" style="flex: 1;overflow: hidden;">
                        <el-option label="固定内容" value=""></el-option>
                        <el-option label="公式" value="formmula"></el-option>
                      </el-select>
                      <i v-if="(form.defaultOrigin == 'formmula' && (formulaEnableList.indexOf(form.type) > -1 || (form.prop && !form.type))) || form.defaultDate" class="after-i-icon" @click="setFuction">
                        <svg aria-hidden="true">
                          <use :xlink:href="`#${form.formula ? 'icon-jvs-a-zu10923' : 'icon-jvs-rongqi1'}`"></use>
                        </svg>
                      </i>
                      <div v-if="form.showFrom.indexOf('defaultValue') !== -1 && !form.defaultOrigin && ['switch', 'slider', 'inputNumber'].indexOf(form.type) > -1" class="default-value-item" style="height: 32px;padding-left: 8px;display: flex;align-items: center;">
                        <div style="display: flex;align-items: center;">
                          <!-- 开关 -->
                          <el-switch
                            v-if="form.type == 'switch'"
                            style="width:100%"
                            v-model="form.defaultValue"
                            size="mini"
                          >
                          </el-switch>
                          <!-- 数字 -->
                          <el-input-number
                            v-if="['slider', 'inputNumber'].indexOf(form.type) > -1"
                            :min="form.min"
                            :max="form.max"
                            :step="form.step"
                            :step-strictly="form.stepstrictly"
                            :precision="form.precision"
                            style="width: 32px;"
                            v-model="form.defaultValue"
                            :controls="false"
                            size="mini">
                          </el-input-number>
                        </div>
                      </div>
                    </div>
                  </el-form-item>
                  <el-form-item v-if="form.showFrom.indexOf('defaultValue') !== -1 && !form.defaultOrigin" :label="formulaEnableList.indexOf(form.type) > -1 ? '' : '默认值'" :class="(['switch','slider'].indexOf(form.type) === -1 && defaultLimit)?'is-error':''">
                    <!-- 正常组件 -->
                    <el-input v-if="['switch', 'slider', 'inputNumber'].indexOf(form.type) === -1" v-model="form.defaultValue" type="textarea" @blur="limitDefaultHandle" size="mini"></el-input>
                    <span v-if="['switch', 'slider', 'inputNumber'].indexOf(form.type) === -1" v-show="defaultLimit" class="el-form-item__error">{{defaultLimitText}}</span>
                  </el-form-item>
                  <!-- 数据联动 -->
                  <el-form-item v-if="(formulaEnableList.indexOf(form.type) > -1 || ['user', 'role', 'department', 'group', 'job', 'textarea', 'flowNode'].indexOf(form.type) > -1) && !form.searchable && ['flowTable'].indexOf(form.parentType) == -1" class="line-item">
                    <span slot="label" class="custom-label-slot-box">
                      <span>联动取值</span>
                      <el-tooltip class="item" effect="light" :content="attrIntroduce.dataLink" placement="top">
                        <div class="icon-info">
                          <span>?</span>
                        </div>
                      </el-tooltip>
                    </span>
                    <div>
                      <div>
                        <el-checkbox v-model="form.dataLinkageEnable"></el-checkbox>
                      </div>
                      <div v-if="form.dataLinkageEnable" class="form-icon-btn" @click="setLinkData" style="margin-top: 8px;">
                        <span>联动配置</span>
                      </div>
                    </div>
                  </el-form-item>
                  <!-- end 数据联动 -->
                  <!-- 搜索文本 -->
                  <el-form-item v-if="form.prop && (!form.type || form.type == 'input') && ['flowTable'].indexOf(form.parentType) == -1" class="line-item">
                    <span slot="label" class="custom-label-slot-box">
                      <span>回填查询</span>
                      <el-tooltip class="item" effect="light" :content="attrIntroduce.searchable" placement="top">
                        <div class="icon-info">
                          <span>?</span>
                        </div>
                      </el-tooltip>
                    </span>
                    <el-checkbox v-model="form.searchable" @change="searchableChange"></el-checkbox>
                  </el-form-item>
                  <div v-if="form.searchable" class="left-line-div">
                    <el-form-item label="关联模型">
                      <el-select
                        style="width:100%"
                        v-model="form.formId" placeholder="请选择数据模型" size="mini"
                        @change="changeModelPropList(form.formId)"
                        filterable
                        clearable
                      >
                        <el-option
                          v-for="item in dataModelList"
                          :key="item.id"
                          :label="item.name"
                          :value="item.id">
                        </el-option>
                      </el-select>
                    </el-form-item>
                    <!-- 查询文本展示字段 -->
                    <el-form-item v-if="form.searchable && dataModelAllFieldList && dataModelAllFieldList.length > 0" label="展示字段" >
                      <el-select v-model="form.others" placeholder="请选择展示字段" size="mini" multiple class="custom-multiple-select" style="width:100%">
                        <el-option
                          v-for="(item, index) in dataModelAllFieldList"
                          :key="'others-connect-show-'+item.fieldKey+'-'+index"
                          :label="item.fieldName"
                          :value="item.fieldKey">
                        </el-option>
                      </el-select>
                    </el-form-item>
                    <!-- 查询文本条件字段 -->
                    <el-form-item v-if="form.searchable && dataModelAllFieldList && dataModelAllFieldList.length > 0" label="查询条件" >
                      <el-select v-model="form.othersQuery" placeholder="请选择查询条件" size="mini" multiple class="custom-multiple-select" style="width:100%">
                        <el-option
                          v-for="(item, index) in otherQueryFilter(dataModelAllFieldList)"
                          :key="'others-query-connect-show-'+item.fieldKey+'-'+index"
                          :label="item.fieldName"
                          :value="item.fieldKey">
                        </el-option>
                      </el-select>
                    </el-form-item>
                    <el-form-item v-if="form.searchable" label="回填字段" >
                      <el-select
                        style="width:100%"
                        v-model="form.props.label" placeholder="请选择回填字段" size="mini"
                        @change="setConnectFormDataUrl"
                      >
                        <el-option
                          v-for="(item, index) in dataModelAllFieldList"
                          :key="'props-label-connect-show-'+item.fieldKey+'-'+index"
                          :label="item.fieldName"
                          :value="item.fieldKey">
                        </el-option>
                      </el-select>
                    </el-form-item>
                    <!-- 数据筛选 -->
                    <el-form-item v-if="form.searchable" class="line-item">
                      <span slot="label" class="custom-label-slot-box">
                        <span>数据筛选</span>
                        <el-tooltip class="item" effect="light" :content="attrIntroduce.dataFilterable" placement="top">
                          <div class="icon-info">
                            <span>?</span>
                          </div>
                        </el-tooltip>
                      </span>
                      <div>
                        <div>
                          <el-checkbox v-model="form.dataFilterable"></el-checkbox>
                        </div>
                        <div v-if="((form.showFrom.indexOf('url') !== -1 && form.datatype === 'dataModel') || form.searchable) && form.dataFilterable">
                          <div class="form-icon-btn" @click="dataFilterHandle">
                            <span>设置条件</span>
                          </div>
                        </div>
                      </div>
                    </el-form-item>
                    <!-- end  数据筛选 -->
                    <el-form-item v-if="form.searchable" label="二次编辑" class="line-item">
                      <el-checkbox v-model="form.editable" @change="searchableChange"></el-checkbox>
                    </el-form-item>
                    <!-- 搜索文本组件是否跳过权限 -->
                    <el-form-item v-if="form.searchable" label="跳过权限" class="line-item">
                      <el-checkbox v-model="form.nopermission"></el-checkbox>
                    </el-form-item>
                    <el-form-item v-if="form.searchable" label="显示宽度">
                      <div style="width: 100%;display: flex;align-items: center;">
                        <el-slider v-model="form.searchPopupWidth" :min="10" :max="100" :step="1" :show-input="false" :range="false" style="flex: 1;"></el-slider>
                        <span class="slider-end-unit">{{form.searchPopupWidth}}%</span>
                      </div>
                    </el-form-item>
                    <!-- end 搜索文本 -->
                  </div>
                  
                  <!-- 文本框是否支持扫码 -->
                  <el-form-item v-if="form.prop && (!form.type || ['input'].indexOf(form.type) > -1) && ['flowTable'].indexOf(form.parentType) == -1" label="扫码取值" class="line-item">
                    <div>
                      <el-checkbox v-model="form.enableScan"></el-checkbox>
                    </div>
                    <div style="width: 100%;font-family: Microsoft YaHei-Regular, Microsoft YaHei;font-weight: 400;font-size: 12px;color: #6F7588;line-height: 16px;margin-top: 6px;">注意：该功能仅在移动端展示扫码！</div>
                  </el-form-item>
                  <!-- 文本框是否脱敏显示 -->
                  <el-form-item v-if="false && form.prop && (!form.type || form.type == 'input') && ['flowTable'].indexOf(form.parentType) == -1" label="脱敏显示" class="line-item">
                    <el-checkbox v-model="form.encryption"></el-checkbox>
                  </el-form-item>
                  <el-form-item v-if="false && form.prop && (!form.type || form.type == 'input') && form.encryption && ['flowTable'].indexOf(form.parentType) == -1" label="脱敏正则">
                    <el-select v-model="form.encryptionExpress" placeholder="请选择脱敏类型" size="mini" clearable style="width:100%">
                      <el-option
                        v-for="item in tmExList"
                        :key="'tm-item-'+item.value"
                        :label="item.label"
                        :value="item.value">
                      </el-option>
                    </el-select>
                  </el-form-item>
                  <!-- 表格类关联其他数据模型 -->
                  <el-form-item v-if="['formbox', 'tableForm', 'reportTable'].indexOf(form.type) > -1" label="关联查询" class="line-item">
                    <el-checkbox v-model="form.dataFilterEnable"></el-checkbox>
                  </el-form-item>
                  <div v-if="['formbox', 'tableForm', 'reportTable'].indexOf(form.type) > -1" class="left-line-div">
                    <el-form-item v-if="['formbox', 'tableForm', 'reportTable'].indexOf(form.type) > -1" label="关联模型">
                      <el-select
                        style="width:100%"
                        v-model="form.formId" placeholder="请选择关联模型" size="mini"
                        @change="changeItemModelPropList(form.formId);"
                        filterable
                        clearable
                      >
                        <el-option
                          v-for="item in dataModelList"
                          :key="item.id"
                          :label="item.name"
                          :value="item.id">
                        </el-option>
                      </el-select>
                    </el-form-item>
                    <!-- 表格设置数据筛选 -->
                    <el-form-item v-if="['formbox', 'tableForm', 'reportTable'].indexOf(form.type) > -1 && form.formId" label="数据筛选">
                      <div class="form-icon-btn" @click="dataFilterHandle">
                        <span>设置条件</span>
                      </div>
                    </el-form-item>
                  </div>
                </el-form>
              </el-collapse-item>
              <el-collapse-item name="conditionoption">
                <template slot="title">
                  <div class="slot-title-div">
                    <div class="title">条件设置</div>
                    <div class="line"></div>
                  </div>
                </template>
                <el-form :model="form" label-width="88px" label-position="left" size='mini'>
                  <el-form-item label="显示控制" prop="showExpress" v-if="form.type && ['flowTable'].indexOf(form.parentType) == -1">
                    <span slot="label" class="custom-label-slot-box">
                      <span>条件显示</span>
                      <el-tooltip class="item" effect="light" :content="attrIntroduce.showExpress" placement="top">
                        <div class="icon-info">
                          <span>?</span>
                        </div>
                      </el-tooltip>
                    </span>
                    <div class="form-icon-btn" @click="setShowHandle('show')">
                      <span>设置条件</span>
                    </div>
                  </el-form-item>
                  <el-form-item label="条件禁用" prop="disabledExpress" v-if="form.type && ['pageTable'].indexOf(form.type) == -1">
                    <div class="form-icon-btn" @click="setShowHandle('disabled')">
                      <span>设置条件</span>
                    </div>
                  </el-form-item>
                  <!-- 动态样式显示 -->
                  <el-form-item v-if="['input'].indexOf(form.type) > -1 && form.disabled" label="条件样式">
                    <div class="form-icon-btn" @click="setExpressDisplay">
                      <span>设置条件</span>
                    </div>
                  </el-form-item>
                  <!-- 动态数据设置 -->
                  <el-form-item v-if="form.showFrom.indexOf('url') !== -1 && ['option', 'dataModel'].indexOf(form.datatype) > -1 && form.type != 'tab'" class="line-item">
                    <span slot="label" class="custom-label-slot-box">
                      <span>动态数据</span>
                      <el-tooltip class="item" effect="light" :content="attrIntroduce.dataItemExpressable" placement="top">
                        <div class="icon-info">
                          <span>?</span>
                        </div>
                      </el-tooltip>
                    </span>
                    <div>
                      <el-checkbox v-model="form.dataItemExpressable"></el-checkbox>
                      <div class="form-icon-btn" @click="dataItemExpressHandle" style="margin-top: 8px;">
                        <span>设置条件</span>
                      </div>
                    </div>
                  </el-form-item>
                  <!-- end  动态数据项 -->
                </el-form>
              </el-collapse-item>
              <el-collapse-item name="blurhandle" v-if="eventList.indexOf(form.type) > -1 && form.type != 'button' && ['flowTable'].indexOf(form.parentType) == -1">
                <template slot="title">
                  <div class="slot-title-div">
                    <div class="title">失焦触发</div>
                    <div class="line"></div>
                  </div>
                </template>
                <el-form :model="form" label-width="88px" label-position="left" size='mini'>
                  <el-form-item label="失焦触发" prop="eventHttp" class="line-item">
                    <span slot="label" class="custom-label-slot-box">
                      <span>失焦触发</span>
                      <el-tooltip class="item" effect="light" :content="attrIntroduce.eventHttp" placement="top">
                        <div class="icon-info">
                          <span>?</span>
                        </div>
                      </el-tooltip>
                    </span>
                    <el-checkbox v-model="form.eventHttpEnable"></el-checkbox>
                    <!-- <div class="form-icon-btn" @click="setHttpHandle('eventHttp')">
                      <span>设置触发逻辑</span>
                    </div> -->
                  </el-form-item>
                  <div v-if="form.eventHttpEnable" class="left-line-div">
                    <el-form-item label="选择逻辑" prop="eventHttp">
                      <div style="display: flex;align-items: center;">
                        <el-select v-model="form.eventHttp" size="mini" filterable clearable @change="eventHttpChange" style="flex: 1;">
                          <el-option v-for="rit in allRuleList" :key="'all-rule-item-'+rit.secret" :label="rit.name" :value="rit.secret"></el-option>
                        </el-select>
                        <i v-if="form.eventHttp" class="el-icon-edit-outline" style="margin-left: 10px;cursor: pointer;" @click="viewRule('eventHttp')"></i>
                      </div>
                      <div v-if="!form.eventHttp" class="form-icon-btn" @click="eventHttpHandle" style="margin-top: 12px;">
                        <i v-if="newRuleSetLoading" class="el-icon-loading" style="margin-right: 5px;"></i>
                        <span>跳转新建逻辑</span>
                      </div>
                    </el-form-item>
                  </div>
                </el-form>
              </el-collapse-item>
              <el-collapse-item name="event" v-if="eventList.indexOf(form.type) > -1 && form.type == 'button'">
                <template slot="title">
                  <div class="slot-title-div">
                    <div class="title">事件设置</div>
                    <div class="line"></div>
                  </div>
                </template>
                <el-form :model="form" label-width="88px" label-position="left" size='mini'>
                  <el-form-item label="设置方式" prop="eventType" v-if="eventList.indexOf(form.type) > -1 && form.type == 'button'">
                    <el-radio-group v-model="form.eventType" size="mini" class="radio-group-row">
                      <el-radio-button label="url">打开地址</el-radio-button>
                      <el-radio-button label="http">触发逻辑</el-radio-button>
                    </el-radio-group>
                  </el-form-item>
                  <div class="left-line-div">
                    <el-form-item v-if="eventList.indexOf(form.type) > -1 && form.type == 'button' && form.eventType == 'url'" prop="openUrl" label="打开地址">
                      <el-select size="mini" style="width:100%" v-model="form.openUrl" placeholder="请选择或输入" filterable allow-create clearable>
                        <el-option v-for="fi in formListOption" :key="fi.id+'_formItem'" :label="fi.name" :value="re.url"></el-option>
                      </el-select>
                    </el-form-item>
                    <el-form-item v-if="eventList.indexOf(form.type) > -1 && form.type == 'button' && form.eventType == 'url'" prop="newWindowOpen" label="新窗口打开" class="line-item">
                      <el-checkbox v-model="form.newWindowOpen"></el-checkbox>
                    </el-form-item>
                    <el-form-item label="触发逻辑" prop="eventHttp" v-if="eventList.indexOf(form.type) > -1 && ( (form.type == 'button' && form.eventType == 'http') || form.type != 'button' )">
                      <div class="form-icon-btn" @click="setHttpHandle('eventHttp')">
                        <span>逻辑配置</span>
                      </div>
                    </el-form-item>
                  </div>
                </el-form>
              </el-collapse-item>
            </el-collapse>
          </el-collapse-item>
          <el-collapse-item title="样式设置" name="style">
            <el-form :model="form" label-width="88px" label-position="left" size='mini'>
              <!-- 栅格格数 -->
              <el-form-item v-if="form.showFrom.indexOf('span') !== -1" label="控件宽度">
                <el-slider
                  v-model="form.span"
                  :min="1"
                  :max="24"
                  :step="1"
                  :show-input="true"
                  :range="false"
                ></el-slider>
              </el-form-item>
              <!-- 表格列 基本宽 默认最小 8px -->
              <el-form-item v-if="['tableForm'].indexOf(form.type) > -1" label="单列基本宽度">
                <div class="slider-with-unit">
                  <el-slider
                    v-model="form.columWidth"
                    :min="8"
                    :max="500"
                    :step="1"
                    :show-input="true"
                    :range="false"
                  ></el-slider>
                  <span class="unit">px</span>
                </div>
              </el-form-item>
              <el-form-item v-if="['divider', 'p', 'colorSelect', 'iconSelect', 'box', 'button', 'iframe', 'jsonEditor'].indexOf(form.type) > -1" label="移动端隐藏" class="line-item">
                <el-checkbox v-model="form.mobileHide"></el-checkbox>
              </el-form-item>
              <!-- 分割线 和 文字 -->
              <el-form-item v-if="form.showFrom.indexOf('text') !== -1" label="显示文字">
                <el-input v-if="form.type == 'box'" v-model="form.text" type="textarea" :rows="15"  maxlength="500" show-word-limit></el-input>
                <el-input v-else v-model="form.text" type="textarea" :rows="7"  maxlength="80" show-word-limit></el-input>
              </el-form-item>
              <!-- 动态流程 -->
              <el-form-item v-if="['flowNode'].indexOf(form.type) > -1" label="节点名称宽度">
                <div class="slider-with-unit">
                  <el-slider
                    v-model="form.nodeNameWidth"
                    :max="70"
                    :step="1"
                    :show-input="false"
                    :range="false"
                  ></el-slider>
                  <span class="unit" style="width: 44px;margin-left: 16px;font-size: 12px;text-align: center;border-radius: 4px;">{{form.nodeNameWidth ? `${form.nodeNameWidth}%` : '不设置'}}</span>
                </div>
              </el-form-item>
              <el-collapse v-model="styleActives" class="inner-collapse">
                <el-collapse-item name="tips" v-if="['divider', 'p', 'pageTable', 'bluetoothBeacon'].indexOf(form.type) == -1">
                  <template slot="title">
                    <div class="slot-title-div">
                      <div class="title">辅助提示</div>
                      <div class="line"></div>
                    </div>
                  </template>
                  <el-form :model="form" label-width="88px" label-position="left" size='mini'>
                    <el-form-item v-if="form.showFrom.indexOf('placeholder') !== -1" label="占位内容">
                      <el-input v-model="form.placeholder" size="mini"></el-input>
                    </el-form-item>
                    <el-form-item v-if="form.showFrom.indexOf('prop') !== -1 && form.tips && form.type != 'p'" label="提示描述" class="line-item">
                      <el-checkbox v-model="form.tipsEnable"></el-checkbox>
                    </el-form-item>
                    <div class="left-line-div" v-if="form.tipsEnable">
                      <el-form-item v-if="form.showFrom.indexOf('prop') !== -1 && form.tips && form.type != 'p'" label="提示内容">
                        <el-input v-model="form.tips.text" size="mini" placeholder="请输入提示内容"></el-input>
                      </el-form-item>
                      <el-form-item v-if="form.showFrom.indexOf('prop') !== -1 && form.tips  && form.type != 'p'" label="展示位置">
                        <el-radio-group v-model="form.tips.position" size="mini" class="radio-group-row">
                          <el-radio-button label="right">右侧</el-radio-button>
                          <el-radio-button label="bottom">换行</el-radio-button>
                        </el-radio-group>
                      </el-form-item>
                    </div>
                  </el-form>
                </el-collapse-item>
                <el-collapse-item name="limit" v-if="['input', 'select', 'file', 'iconSelect', 'htmlEditor', 'tab', 'serialNumber', 'positionMap', 'signature', 'jsonEditor', 'iframe', 'pageTable', 'bluetoothBeacon', 'role', 'user', 'job', 'timeline', 'flowNode', 'dynamicForm'].indexOf(form.type) == -1">
                  <template slot="title">
                    <div class="slot-title-div">
                      <div class="title">其他设置</div>
                      <div class="line"></div>
                    </div>
                  </template>
                  <el-form :model="form" label-width="88px" label-position="left" size='mini'>
                    <!-- 文本域 -->
                    <el-form-item v-if="form.showFrom.indexOf('rows') !== -1" label="默认行数">
                      <el-input-number style="width: 100%;" :min="1" :max="20" v-model="form.rows" size="mini"></el-input-number>
                    </el-form-item>
                    <el-form-item v-if="false && form.showFrom.indexOf('minlength') !== -1" label="最小长度">
                      <el-input-number style="width: 100%;" :min="0" v-model="form.minlength" size="mini" :controls="false"></el-input-number>
                    </el-form-item>
                    <!-- 小标题 -->
                    <el-form-item v-if="['p'].indexOf(form.type) > -1" label="隐藏分隔线" class="line-item">
                      <el-checkbox v-model="form.barHide"></el-checkbox>
                    </el-form-item>
                    <!-- number -->
                    <el-form-item v-if="form.showFrom.indexOf('min') !== -1" :label="['checkbox'].indexOf(form.type) > -1 ? '最少选择' : '最小值'">
                      <!-- :min="-2147483647" :max="2147483647" -->
                      <el-input-number style="width: 100%;" v-model="form.min" size="mini" :controls="false" :min="-9007199254740991" :max="9007199254740991"></el-input-number>
                    </el-form-item>
                    <el-form-item v-if="form.showFrom.indexOf('max') !== -1" :label="['checkbox'].indexOf(form.type) > -1 ? '最多选择' : '最大值'">
                       <!-- :min="-2147483647" :max="2147483647" -->
                      <el-input-number style="width: 100%;" v-model="form.max" :controls="false" :min="-9007199254740991" :max="9007199254740991"></el-input-number>
                    </el-form-item>
                    <el-form-item v-if="['inputNumber', 'slider'].indexOf(form.type) > -1" label="显示单位">
                      <el-input v-model="form.unit" size="mini"></el-input>
                    </el-form-item>
                    <el-form-item v-if="form.showFrom.indexOf('controlsposition') !== -1" label="加减按钮">
                      <el-radio-group v-model="form.controlsposition" size="mini" class="radio-group-row">
                        <el-radio-button label="">两边</el-radio-button>
                        <el-radio-button label="right">右边</el-radio-button>
                        <el-radio-button label="none">隐藏</el-radio-button>
                      </el-radio-group>
                    </el-form-item>
                    <el-form-item v-if="form.showFrom.indexOf('precision') !== -1" label="小数位数">
                      <el-input-number style="width: 100%;" :min="0" :max="10" :step="1" stepStrictly v-model="form.precision" size="mini" :controls="false"></el-input-number>
                    </el-form-item>
                    <el-form-item v-if="form.showFrom.indexOf('step') !== -1" label="步长">
                      <el-input-number style="width: 100%;" :precision="2" v-model="form.step" size="mini" :controls="false"></el-input-number>
                    </el-form-item>
                    <el-form-item v-if="form.showFrom.indexOf('stepstrictly') !== -1" class="line-item">
                      <span slot="label" class="custom-label-slot-box">
                        <span>严格步数</span>
                        <el-tooltip class="item" effect="light" content="是否只能输入 步长 的倍数" placement="top">
                          <div class="icon-info">
                            <span>?</span>
                          </div>
                        </el-tooltip>
                      </span>
                      <el-checkbox v-model="form.stepstrictly"></el-checkbox>
                    </el-form-item>
                    <!-- 滑块 -->
                    <el-form-item v-if="form.showFrom.indexOf('showstops') !== -1" label="显示间断" class="line-item">
                      <el-checkbox v-model="form.showstops"></el-checkbox>
                    </el-form-item>
                    <el-form-item v-if="form.showFrom.indexOf('showinput') !== -1" label="输入框" v-show="!form.range">
                      <el-checkbox v-model="form.showinput"></el-checkbox>
                    </el-form-item>
                    <el-form-item v-if="form.showFrom.indexOf('range') !== -1" label="范围选择">
                      <el-checkbox v-model="form.range"></el-checkbox>
                    </el-form-item>
                    <!-- 开关 -->
                    <el-form-item v-if="form.showFrom.indexOf('width') !== -1" label="开关宽度">
                      <el-input-number style="width: 100%;" :min="40" :max="100" :step="1"  v-model="form.width" :controls="false"></el-input-number>
                    </el-form-item>
                    <el-form-item v-if="form.showFrom.indexOf('activetext') !== -1" label="打开时文字">
                      <el-input v-model="form.activetext" size="mini"></el-input>
                    </el-form-item>
                    <el-form-item v-if="form.showFrom.indexOf('inactivetext') !== -1" label="关闭时文字">
                      <el-input v-model="form.inactivetext" size="mini"></el-input>
                    </el-form-item>
                    <el-form-item v-if="form.showFrom.indexOf('activecolor') !== -1" label="打开时背景色">
                      <el-color-picker v-model="form.activecolor" show-alpha size="mini" :predefine="predefineColors"></el-color-picker>
                    </el-form-item>
                    <el-form-item v-if="form.showFrom.indexOf('inactivecolor') !== -1" label="关闭时背景色">
                      <el-color-picker v-model="form.inactivecolor" show-alpha size="mini" :predefine="predefineColors"></el-color-picker>
                    </el-form-item>
                    <!-- 日期选择 -->
                    <el-form-item v-if="form.showFrom.indexOf('datetype') !== -1" label="选择单位">
                      <el-select style="width:100%" v-model="form.datetype" placeholder="请选择" size="mini">
                        <el-option label="年月日-单个" value="date"></el-option>
                        <el-option label="年周-单个" value="week"></el-option>
                        <el-option label="年月-单个" value="month"></el-option>
                        <el-option label="年-单个" value="year"></el-option>
                        <el-option label="多日期" value="dates"></el-option>
                        <el-option label="年月日时分秒-单个" value="datetime"></el-option>
                        <el-option label="年月日时分秒-范围" value="datetimerange"></el-option>
                        <el-option label="年月日-范围" value="daterange"></el-option>
                        <el-option label="年月-范围" value="monthrange"></el-option>
                      </el-select>
                    </el-form-item>
                    <div class="left-line-div">
                      <div v-if="form.datetype == 'datetimerange' || form.datetype == 'daterange' || form.datetype == 'monthrange'">
                        <el-form-item v-if="form.showFrom.indexOf('startplaceholder') !== -1" label="开始占位">
                          <el-input v-model="form.startplaceholder" size="mini"></el-input>
                        </el-form-item>
                        <el-form-item v-if="form.showFrom.indexOf('endplaceholder') !== -1" label="结束占位">
                          <el-input v-model="form.endplaceholder" size="mini"></el-input>
                        </el-form-item>
                        <el-form-item v-if="form.showFrom.indexOf('rangeseparator') !== -1" label="分隔符">
                          <el-input v-model="form.rangeseparator" size="mini"></el-input>
                        </el-form-item>
                      </div>
                      <el-form-item v-if="form.showFrom.indexOf('startLimit') !== -1" label="最早限制">
                        <el-date-picker
                          style="width:100%"
                          v-if="form.type === 'datePicker' && (form.datetype == 'date' || form.datetype == 'dates' || form.datetype == 'daterange')"
                          v-model="form.startLimit"
                          :type='"date"'
                          :placeholder="form.placeholder"
                          :clearable='form.clearable'
                          :disabled='form.disabled'
                          :prefix-icon='form.prefixicon'
                          value-format="yyyy-MM-dd"
                          :start-placeholder='form.startplaceholder'
                          :end-placeholder='form.endplaceholder'
                          :range-separator='form.rangeseparator'
                          size="mini"
                          >
                        </el-date-picker>
                        <el-date-picker
                          style="width:100%"
                          v-if="form.type === 'datePicker' && form.datetype == 'week'"
                          v-model="form.startLimit"
                          :type='"date"'
                          :placeholder="form.placeholder"
                          :clearable='form.clearable'
                          :disabled='form.disabled'
                          :prefix-icon='form.prefixicon'
                          value-format="yyyy-MM-dd"
                          :start-placeholder='form.startplaceholder'
                          :end-placeholder='form.endplaceholder'
                          :range-separator='form.rangeseparator'
                          size="mini"
                          >
                        </el-date-picker>
                        <el-date-picker
                          style="width:100%"
                          v-if="form.type === 'datePicker' && (form.datetype == 'month' || form.datetype == 'monthrange')"
                          v-model="form.startLimit"
                          :type='"month"'
                          :placeholder="form.placeholder"
                          :clearable='form.clearable'
                          :disabled='form.disabled'
                          :prefix-icon='form.prefixicon'
                          value-format="yyyy-MM"
                          :start-placeholder='form.startplaceholder'
                          :end-placeholder='form.endplaceholder'
                          :range-separator='form.rangeseparator'
                          size="mini"
                          >
                        </el-date-picker>
                        <el-date-picker
                          style="width:100%"
                          v-if="form.type === 'datePicker' && form.datetype == 'year'"
                          v-model="form.startLimit"
                          type='year'
                          :placeholder="form.placeholder"
                          :clearable='form.clearable'
                          :disabled='form.disabled'
                          :prefix-icon='form.prefixicon'
                          value-format="yyyy"
                          :start-placeholder='form.startplaceholder'
                          :end-placeholder='form.endplaceholder'
                          :range-separator='form.rangeseparator'
                          size="mini"
                          >
                        </el-date-picker>
                        <el-date-picker
                          style="width:100%"
                          v-if="form.type === 'datePicker' && (form.datetype == 'datetime' || form.datetype == 'datetimerange')"
                          v-model="form.startLimit"
                          :type='"date"'
                          :placeholder="form.placeholder"
                          :clearable='form.clearable'
                          :disabled='form.disabled'
                          :prefix-icon='form.prefixicon'
                          value-format="yyyy-MM-dd"
                          :start-placeholder='form.startplaceholder'
                          :end-placeholder='form.endplaceholder'
                          :range-separator='form.rangeseparator'
                          size="mini"
                          >
                        </el-date-picker>
                      </el-form-item>
                      <el-form-item v-if="form.showFrom.indexOf('endLimit') !== -1" label="最晚限制">
                        <el-date-picker
                          style="width:100%"
                          v-if="form.type === 'datePicker' && ( form.datetype == 'date' || form.datetype == 'dates' || form.datetype == 'daterange')"
                          v-model="form.endLimit"
                          :type='"date"'
                          :placeholder="form.placeholder"
                          :clearable='form.clearable'
                          :disabled='form.disabled'
                          :prefix-icon='form.prefixicon'
                          value-format="yyyy-MM-dd"
                          :start-placeholder='form.startplaceholder'
                          :end-placeholder='form.endplaceholder'
                          :range-separator='form.rangeseparator'
                          size="mini"
                          >
                        </el-date-picker>
                        <el-date-picker
                          style="width:100%"
                          v-if="form.type === 'datePicker' && form.datetype == 'week'"
                          v-model="form.endLimit"
                          type="date"
                          :placeholder="form.placeholder"
                          :clearable='form.clearable'
                          :disabled='form.disabled'
                          :prefix-icon='form.prefixicon'
                          value-format="yyyy-MM-dd"
                          :start-placeholder='form.startplaceholder'
                          :end-placeholder='form.endplaceholder'
                          :range-separator='form.rangeseparator'
                          size="mini"
                          >
                        </el-date-picker>
                        <el-date-picker
                          style="width:100%"
                          v-if="form.type === 'datePicker' && (form.datetype == 'month' || form.datetype == 'monthrange')"
                          v-model="form.endLimit"
                          :type='"month"'
                          :placeholder="form.placeholder"
                          :clearable='form.clearable'
                          :disabled='form.disabled'
                          :prefix-icon='form.prefixicon'
                          value-format="yyyy-MM"
                          :start-placeholder='form.startplaceholder'
                          :end-placeholder='form.endplaceholder'
                          :range-separator='form.rangeseparator'
                          size="mini"
                          >
                        </el-date-picker>
                        <el-date-picker
                          style="width:100%"
                          v-if="form.type === 'datePicker' && form.datetype == 'year'"
                          v-model="form.endLimit"
                          type='year'
                          :placeholder="form.placeholder"
                          :clearable='form.clearable'
                          :disabled='form.disabled'
                          :prefix-icon='form.prefixicon'
                          value-format="yyyy"
                          :start-placeholder='form.startplaceholder'
                          :end-placeholder='form.endplaceholder'
                          :range-separator='form.rangeseparator'
                          size="mini"
                          >
                        </el-date-picker>
                        <el-date-picker
                          style="width:100%"
                          v-if="form.type === 'datePicker' && (form.datetype == 'datetime' || form.datetype == 'datetimerange')"
                          v-model="form.endLimit"
                          type="date"
                          :placeholder="form.placeholder"
                          :clearable='form.clearable'
                          :disabled='form.disabled'
                          :prefix-icon='form.prefixicon'
                          value-format="yyyy-MM-dd"
                          :start-placeholder='form.startplaceholder'
                          :end-placeholder='form.endplaceholder'
                          :range-separator='form.rangeseparator'
                          size="mini"
                          >
                        </el-date-picker>
                      </el-form-item>
                    </div>
                    <!-- 时间选择器 固定时间 -->
                    <el-form-item v-if="form.type == 'timeSelect'" label="开始时间">
                      <el-time-picker
                        v-model="form.pickeroptions.start"
                        placeholder="开始时间"
                        value-format='HH:mm'
                        format='HH:mm'
                        size="mini"
                        @change="form.defaultValue=form.pickeroptions.start;$forceUpdate();"
                      >
                      </el-time-picker>
                    </el-form-item>
                    <el-form-item v-if="form.type == 'timeSelect'" label="结束时间">
                      <el-time-picker
                        v-model="form.pickeroptions.end"
                        placeholder="结束时间"
                        value-format='HH:mm'
                        format='HH:mm'
                        size="mini"
                      >
                      </el-time-picker>
                    </el-form-item>
                    <el-form-item v-if="form.type == 'timeSelect'" label="步长">
                      <el-time-picker
                        v-model="form.pickeroptions.step"
                        :picker-options="{
                          selectableRange: '00:01:00 - 06:00:00'
                        }"
                        placeholder="步长"
                        value-format='HH:mm'
                        format='HH:mm'
                        size="mini"
                      >
                      </el-time-picker>
                    </el-form-item>
                    <!-- 时间选择器 任意时间 -->
                    <el-form-item v-if="form.showFrom.indexOf('isrange') !== -1" label="范围选择" class="line-item">
                      <el-checkbox v-model="form.isrange"></el-checkbox>
                    </el-form-item>
                    <div v-if="form.isrange" class="left-line-div">
                      <el-form-item v-if="form.showFrom.indexOf('startplaceholder') !== -1" label="开始占位">
                        <el-input v-model="form.startplaceholder" size="mini"></el-input>
                      </el-form-item>
                      <el-form-item v-if="form.showFrom.indexOf('endplaceholder') !== -1" label="结束占位">
                        <el-input v-model="form.endplaceholder" size="mini"></el-input>
                      </el-form-item>
                      <el-form-item v-if="form.showFrom.indexOf('rangeseparator') !== -1" label="分隔符">
                        <el-input v-model="form.rangeseparator" size="mini"></el-input>
                      </el-form-item>
                    </div>
                    <!-- 单选 -->
                    <el-form-item v-if="form.showFrom.indexOf('radiotype') !== -1" label="展示类型">
                      <el-select style="width:100%" v-model="form.radiotype" placeholder="请选择" size="mini">
                        <el-option label="圆圈" value="yuan"></el-option>
                        <el-option label="按钮" value="button"></el-option>
                      </el-select>
                    </el-form-item>
                    <!-- 多选 -->
                    <el-form-item v-if="form.showFrom.indexOf('checkboxtype') !== -1" label="展示类型">
                      <el-select style="width:100%" v-model="form.checkboxtype" placeholder="请选择" size="mini">
                        <el-option label="方块" value="fang"></el-option>
                        <el-option label="按钮" value="button"></el-option>
                      </el-select>
                    </el-form-item>
                    <!-- 单选多选排列方向 -->
                    <el-form-item v-if="['radio', 'checkbox'].indexOf(form.type) > -1 && form.radiotype != 'button' && form.checkboxtype != 'button'" label="排列方式">
                      <el-radio-group v-model="form.arrangeType" size="mini" class="radio-group-row">
                        <el-radio-button label="">横向
                          <el-tooltip class="item" effect="light" content="依次排列" placement="top">
                            <div class="icon-info">
                              <span>?</span>
                            </div>
                          </el-tooltip>
                        </el-radio-button>
                        <el-radio-button label="vertical">纵向
                          <el-tooltip class="item" effect="light" content="每个选项独立一行" placement="top">
                            <div class="icon-info">
                              <span>?</span>
                            </div>
                          </el-tooltip>
                        </el-radio-button>
                      </el-radio-group>
                    </el-form-item> 
                    
                    <!-- 图片尺寸适应 -->
                    <el-form-item v-if="form.showFrom.indexOf('fit') !== -1" label="图片适配">
                      <el-select style="width:100%" v-model="form.fit" placeholder="请选择图片适配方式" size="mini">
                        <el-option label="充满 fill" value="fill"></el-option>
                        <el-option label="包含 contain" value="contain"></el-option>
                        <el-option label="遮盖 cover" value="cover"></el-option>
                        <el-option label="无 none" value="none"></el-option>
                        <el-option label="按比例缩减 scale-down" value="scale-down"></el-option>
                      </el-select>
                    </el-form-item>
                    <!-- 分割线 和 文字 -->
                    <el-form-item v-if="form.showFrom.indexOf('contentposition') !== -1 && ['link'].indexOf(form.type) == -1" label="文字位置">
                      <el-radio-group v-model="form.contentposition" size="mini" class="radio-group-row">
                        <el-radio-button label="left">左</el-radio-button>
                        <el-radio-button label="center">中</el-radio-button>
                        <el-radio-button label="right">右</el-radio-button>
                      </el-radio-group>
                    </el-form-item>
                    <el-form-item v-if="form.showFrom.indexOf('fontsize') !== -1" label="文字大小">
                      <el-slider
                        v-model="form.fontsize"
                        :min="12"
                        :max="24"
                        :step="1"
                        :show-input="false"
                        :range="false"
                      ></el-slider>
                    </el-form-item>
                    <el-form-item v-if="form.showFrom.indexOf('textcolor') !== -1" label="文字颜色">
                      <el-color-picker v-model="form.textcolor" show-alpha size="mini" :predefine="predefineColors"></el-color-picker>
                    </el-form-item>
                    <el-form-item v-if="form.type == 'box'" label="背景颜色">
                      <el-color-picker v-model="form.boxback" show-alpha size="mini" :predefine="predefineColors"></el-color-picker>
                    </el-form-item>
                    <!-- 级联 -->
                    <el-form-item v-if="form.showFrom.indexOf('showalllevels') !== -1 && ['user', 'role', 'group', 'job'].indexOf(form.type) == -1" label="显示路径" class="line-item">
                      <el-checkbox v-model="form.showalllevels"></el-checkbox>
                    </el-form-item>
                    <el-form-item v-if="false && form.showFrom.indexOf('emitPath') !== -1" label="传递路径" class="line-item">
                      <el-checkbox v-model="form.emitPath"></el-checkbox>
                    </el-form-item>
                    <!-- 上传 -->
                    <el-form-item v-if="false && form.showFrom.indexOf('headers') !== -1" label="请求头">
                      <el-input v-model="form.headersStr" @blur="form.setRequestHeaderHandle"></el-input>
                    </el-form-item>
                    <el-form-item v-if="(form.type === 'imageUpload' || form.type === 'fileUpload') && form.showFrom.indexOf('limit') !== -1" label="最多上传个数">
                      <el-input-number style="width: 100%;" v-model="form.limit" :min="0" size="mini" :controls="false" @change="uploadLimitChange"></el-input-number>
                    </el-form-item>
                    <el-form-item v-if="form.showFrom.indexOf('multipleUpload') !== -1 && form.limit > 1" label="多选文件" class="line-item">
                      <el-checkbox v-model="form.multipleUpload"></el-checkbox>
                    </el-form-item>
                    <el-form-item v-if="false && (form.type === 'imageUpload' || form.type === 'fileUpload') && form.showFrom.indexOf('action') !== -1" label="上传配置">
                      <div class="form-icon-btn" @click="setHttpHandle('upload',form, 'uploadHttp')">
                        <svg aria-hidden="true">
                          <use xlink:href="#jvs-ui-icon-zhanghuguanli-weixuanzhong"></use>
                        </svg>
                        <span>设置上传</span>
                      </div>
                    </el-form-item>
                    <el-form-item v-if="(['imageUpload'].indexOf(form.type) > -1) && (form.action || (form.uploadHttp && form.uploadHttp.url))" label="占位图">
                      <el-upload
                        class="avatar-uploader"
                        :action="'/mgr/jvs-auth/upload/jvs-form-design' || form.action || (form.uploadHttp && form.uploadHttp.url) || ''"
                        :show-file-list="false"
                        accept="image/png,image/jpg,image/jpeg"
                        :on-success="handleAvatarSuccess"
                        :before-upload="beforeAvatarUpload"
                        :headers="{
                          tenantId: $store.getters.userInfo.tenantId,
                          Authorization: 'Bearer ' + $store.getters.access_token
                        }"
                        :data="{module: '/jvs-ui/form/'}"
                      >
                        <img v-if="imageUrl" :src="imageUrl" class="avatar">
                        <i v-else class="el-icon-plus avatar-uploader-icon"></i>
                      </el-upload>
                    </el-form-item>
                    <!-- 文件大小限制 -->
                    <el-form-item v-if="form.type === 'fileUpload'" label="文件最大">
                      <div style="display: flex;align-items: center;">
                        <el-input-number style="width: 100%;" v-model="form.fileSize" :min="0" size="mini" :precision="0" :controls="false"></el-input-number>
                        <span style="margin-left: 10px;color: #909398;font-size: 12px;">MB</span>
                      </div>
                    </el-form-item>
                    <!-- 文件类型限制 -->
                    <el-form-item v-if="form.type === 'fileUpload'" label="文件类型">
                      <div>
                        <el-input v-model="form.fileType" size="mini" placeholder="输入文件后缀名"></el-input>
                      </div>
                      <div style="text-align: left;font-size: 12px;color: #909398;">
                        <span>多个文件类型后缀名以英文逗号分割</span>
                      </div>
                    </el-form-item>
                    <!-- 表格 -->
                    <el-form-item v-if="false && form.showFrom.indexOf('border') !== -1" label="是否边框">
                      <el-switch v-model="form.border" active-color="#409EFF" inactive-color="#eee" size="mini"></el-switch>
                    </el-form-item>
                    <el-form-item v-if="false && form.showFrom.indexOf('stripe') !== -1" label="斑马纹">
                      <el-switch v-model="form.stripe" active-color="#409EFF" inactive-color="#eee" size="mini"></el-switch>
                    </el-form-item>
                    <el-form-item v-if="false && form.type == 'tableForm' && !form.editable" label="是否分页">
                      <el-switch v-model="form.page" active-color="#409EFF" inactive-color="#eee" size="mini"></el-switch>
                    </el-form-item>
                    <el-form-item v-if="form.type == 'tableForm'" label="操作栏">
                      <el-select v-model="menuFixTemp" clearable size="mini" style="width:100%;" @change="menuFixChange">
                        <el-option label="左侧" value="left"></el-option>
                        <el-option label="右侧" value="right"></el-option>
                      </el-select>
                    </el-form-item>
                    <!-- 按钮组件  样式 -->
                    <el-form-item v-if="form.showFrom.indexOf('style') !== -1" label="按钮类型" prop="buttonType">
                      <el-select style="width:100%" v-model="form.buttonType" placeholder="请选择按钮类型" size="mini" clearable>
                        <el-option label="主要按钮" value="primary"></el-option>
                        <el-option label="成功按钮" value="success"></el-option>
                        <el-option label="信息按钮" value="info"></el-option>
                        <el-option label="警告按钮" value="warning"></el-option>
                        <el-option label="危险按钮" value="danger"></el-option>
                      </el-select>
                    </el-form-item>
                    <!-- 按钮组件  是否圆角 -->
                    <el-form-item v-if="form.showFrom.indexOf('style') !== -1" prop="buttonRound" label="显示圆角" class="line-item">
                      <el-checkbox v-model="form.buttonRound"></el-checkbox>
                    </el-form-item>
                    <!-- 用户组件 -是否允许输入 -->
                    <el-form-item v-if="false && form.showFrom.indexOf('allowinput') !== -1" label="可否输入" class="line-item">
                      <el-checkbox v-model="form.allowinput"></el-checkbox>
                    </el-form-item>
                  </el-form>
                </el-collapse-item>
              </el-collapse>
              <el-form-item v-if="form.showFrom.indexOf('prefixicon') !== -1 && false" label="头部图标">
                <el-input v-model="form.prefixicon" size="mini"></el-input>
              </el-form-item>
              <el-form-item v-if="form.showFrom.indexOf('suffixicon') !== -1 && false" label="尾部图标">
                <el-input v-model="form.suffixicon"></el-input>
              </el-form-item>
              <el-form-item v-if="form.showFrom.indexOf('prepend') !== -1" label="前置内容">
                <el-input v-model="form.prepend" size="mini" placeholder="请输入前置内容"></el-input>
              </el-form-item>
              <el-form-item v-if="form.showFrom.indexOf('append') !== -1" label="后置内容">
                <el-input v-model="form.append" size="mini" placeholder="请输入后置内容"></el-input>
              </el-form-item>
              <!-- iframe高度 -->
              <el-form-item label="显示高度" v-if="form.type == 'iframe'">
                <el-slider
                  v-model="form.iframeheight"
                  :min="100"
                  :max="1500"
                  :step="1"
                  :show-input="true"
                  :range="false"
                ></el-slider>
              </el-form-item>
              <!-- 列表页最大高度 -->
              <el-form-item label="最大高度" v-if="form.showFrom.indexOf('maxheight') !== -1">
                <el-slider
                  v-model="form.maxheight"
                  :min="300"
                  :max="1500"
                  :step="1"
                  :show-input="true"
                  :range="false"
                ></el-slider>
              </el-form-item>
              
              
              










              

              

              <!-- 步骤条组件 -->
              <el-form-item v-if="form.type == 'step'" label="按钮对齐">
                <el-select style="width:100%" v-model="form.toolAlign" placeholder="请选择">
                  <el-option label="左对齐" value="left"></el-option>
                  <el-option label="居中对齐" value="center"></el-option>
                  <el-option label="右对齐" value="flex-end"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item v-if="form.showFrom.indexOf('linkbind') !== -1" label="控制值">
                <el-input v-model="form.linkbind" size="mini" placeholder="多个值使用英文逗号,隔开"></el-input>
              </el-form-item>
              
              <!-- 地区选择 -->
              <el-form-item v-if="form.showFrom.indexOf('emitKey') !== -1" label="传递key值">
                <el-select style="width:100%" v-model="form.emitKey" placeholder="请选择key值" size="mini">
                  <el-option label="名称" value="name"></el-option>
                  <el-option label="代码" value="code"></el-option>
                </el-select>
              </el-form-item>

              <!-- 下拉 来源 系统字典项 -->
              <el-form-item v-if="form.showFrom.indexOf('datatype') !== -1 && form.datatype == 'system' && form.type != 'cascader'" label="选择字典">
                <el-select style="width:100%" v-model="form.systemDict" placeholder="请选择数据" size="mini" filterable>
                  <el-option v-for="c in systemDictList" :key="c.description+c.uniqId+'select-dic-item'" :label="c.description" :value="c.uniqId"></el-option>
                </el-select>
              </el-form-item>

              <!-- 数据源组件 -->
              <el-form-item v-if="form.showFrom.indexOf('sourceTable') !== -1" label="数据来源">
                <el-select style="width:100%" v-model="form.sourceType" placeholder="请选择数据来源" size="mini" filterable>
                  <el-option label="数据源" value="database"></el-option>
                  <el-option label="接口api" value="api"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item v-if="form.sourceType == 'api' && form.showFrom.indexOf('sourceTable') !== -1" label="接口api">
                <jvs-button size="mini"  @click="setHttpHandle('api',form, 'apiHttp')">配置</jvs-button>
              </el-form-item>
              <!-- 数据模型 -->
              <el-form-item v-if="form.showFrom.indexOf('sourceTable') !== -1 && form.sourceType == 'database'" label="数据模型">
                <el-select style="width:100%" v-model="form.dataModelId" placeholder="请选择数据模型" size="mini" filterable @change="tableChangeHandle(form.tableName, itemTableOption, 'tablesItem')">
                  <!-- <el-option v-for='(it,index) in itemTableOption' :key="'datasource-item'+index" :label="it.tableName" :value="it.tableName"></el-option> -->
                </el-select>
              </el-form-item>
              <el-form-item v-if="form.showFrom.indexOf('sourceTable') !== -1 && form.sourceType" label="展示信息" class="top-align-item">
                <el-table
                  border
                  :data="form.infoColumn"
                  class="tb-edit"
                  style="width: 100%"
                  highlight-current-row
                  size='mini'
                >
                  <el-table-column label="字段名">
                    <template slot-scope="scope">
                      <div>
                        <el-select
                          v-if="form.sourceType == 'database'"
                          style="width:100%"
                          v-model="scope.row.value"
                          allow-create
                          filterable
                          size="mini"
                          clearable
                          @change="changeSource(scope.row, scope.$index, tablesItem, 'infoColumn')"
                        >
                          <el-option  v-for='(item,index) in tablesItem' :key="index" :label="item.fieldName" :value="item.fieldName">
                            <span style="float: left">{{ item.columnComment }}</span>
                            <span style="float: right; color: #8492a6; font-size: 13px">{{ item.fieldName }}</span>
                          </el-option>
                        </el-select>
                        <el-input v-if="form.sourceType == 'api'" v-model="scope.row.value"></el-input>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column label="显示值">
                    <template slot-scope="scope">
                      <el-input
                        size="mini"
                        v-model="scope.row.label"
                        placeholder="请输入内容"
                      ></el-input>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作">
                    <template slot-scope="scope">
                      <div>
                        <el-button
                          size="mini"
                          type="text"
                          @click="deleteItemOfArr(scope.$index, scope.row, 'infoColumn')"
                        >删除</el-button>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
                <el-button size='mini' @click="addItemOfArr('infoColumn')" style="margin-top:10px">增加选项</el-button>
              </el-form-item>
              <el-form-item v-if="form.showFrom.indexOf('sourceTable') !== -1 && form.sourceType" label="查询条件" class="top-align-item">
                <el-table
                  border
                  :data="form.queryProp"
                  class="tb-edit"
                  style="width: 100%"
                  highlight-current-row
                  size='mini'
                >
                  <el-table-column label="字段名">
                    <template slot-scope="scope">
                      <div>
                        <el-select
                          v-if="form.sourceType == 'database'"
                          style="width:100%"
                          v-model="scope.row.value"
                          allow-create filterable
                          size="mini"
                          clearable
                          @change="changeSource(scope.row, scope.$index, tablesItem, 'queryProp')"
                        >
                          <el-option  v-for='(item,index) in tablesItem' :key="index" :label="item.fieldName" :value="item.fieldName">
                            <span style="float: left">{{ item.columnComment }}</span>
                            <span style="float: right; color: #8492a6; font-size: 13px">{{ item.fieldName }}</span>
                          </el-option>
                        </el-select>
                        <el-input v-if="form.sourceType == 'api'" v-model="scope.row.value"></el-input>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column label="显示值">
                    <template slot-scope="scope">
                      <el-input
                        size="mini"
                        v-model="scope.row.label"
                        placeholder="请输入内容"
                      ></el-input>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作">
                    <template slot-scope="scope">
                      <div>
                        <el-button
                          size="mini"
                          type="text"
                          @click="deleteItemOfArr(scope.$index, scope.row, 'queryProp')"
                        >删除</el-button>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
                <el-button size='mini' @click="addItemOfArr('queryProp')" style="margin-top:10px">增加选项</el-button>
              </el-form-item>
              <el-form-item v-if="form.showFrom.indexOf('sourceTable') !== -1 && form.sourceType" label="显示字段">
                <el-select v-if="form.sourceType == 'database'" style="width:100%" v-model="form.showProp" allow-create filterable size="mini" clearable>
                  <el-option  v-for='(item,index) in tablesItem' :key="index" :label="item.fieldName" :value="item.fieldName">
                    <span style="float: left">{{ item.columnComment }}</span>
                    <span style="float: right; color: #8492a6; font-size: 13px">{{ item.fieldName }}</span>
                  </el-option>
                </el-select>
                <el-input v-if="form.sourceType == 'api'" v-model="form.showProp"></el-input>
              </el-form-item>
              <el-form-item v-if="form.showFrom.indexOf('sourceTable') !== -1 && form.sourceType" label="传递字段">
                <el-select v-if="form.sourceType == 'database'" style="width:100%" v-model="form.sendProp" allow-create filterable size="mini" clearable>
                  <el-option  v-for='(item,index) in tablesItem' :key="index" :label="item.fieldName" :value="item.fieldName">
                    <span style="float: left">{{ item.columnComment }}</span>
                    <span style="float: right; color: #8492a6; font-size: 13px">{{ item.fieldName }}</span>
                  </el-option>
                </el-select>
                <el-input v-if="form.sourceType == 'api'" v-model="form.sendProp"></el-input>
              </el-form-item>

              <!-- 静态表格  报表类 -->
              <el-form-item v-if="form.type == 'reportTable'" label="总标题">
                <el-input v-model="form.rowcolumn"></el-input>
              </el-form-item>
            </el-form>
          </el-collapse-item>
          <el-collapse-item title="校验设置" name="validate" v-if="form.rules.length > 0 && ['flowTable'].indexOf(form.parentType) == -1">
            <el-collapse v-model="rulesActives" class="inner-collapse">
              <el-collapse-item name="required">
                <template slot="title">
                  <div class="slot-title-div">
                    <div class="title">必填校验</div>
                    <div class="line"></div>
                  </div>
                </template>
                <el-form :model="form" label-width="88px" label-position="left" size='mini'>
                  <el-form-item label="必填校验" class="line-item">
                    <el-checkbox v-model="form.rules[0].required" @change="requiredChange"></el-checkbox>
                  </el-form-item>
                  <el-form-item label="失败提示">
                    <el-input size="mini" v-model="form.rules[0].message" placeholder="请输入失败提示"></el-input>
                  </el-form-item>
                </el-form>
              </el-collapse-item>
              <el-collapse-item name="regexp" v-if="form.showFrom.indexOf('regular') !== -1">
                <template slot="title">
                  <div class="slot-title-div">
                    <div class="title">正则校验</div>
                    <div class="line"></div>
                  </div>
                </template>
                <el-form :model="form" label-width="88px" label-position="left" size='mini'>
                  <el-form-item label="校验规则" prop="regularExpression" :rules="regCheck">
                    <el-select size="mini" style="width:100%" v-model="form.regularExpression" placeholder="请选择校验规则" filterable allow-create clearable>
                      <el-option v-for="re in regExpOption" :key="re.name+'_regExpItem_'+ re.id" :label="re.name" :value="re.expression"></el-option>
                    </el-select>
                  </el-form-item>
                  <el-form-item label="失败提示">
                    <el-input size="mini" v-model="form.regularMessage" placeholder="请输入失败提示"></el-input>
                  </el-form-item>
                </el-form>
              </el-collapse-item>
              <el-collapse-item name="reqexp" v-if="form.type && ['flowTable'].indexOf(form.parentType) == -1">
                <template slot="title">
                  <div class="slot-title-div">
                    <div class="title">动态校验</div>
                    <div class="line"></div>
                  </div>
                </template>
                <el-form :model="form" label-width="88px" label-position="left" size='mini'>
                  <el-form-item label="动态校验" class="line-item">
                    <el-checkbox v-model="form.requireExpressEnable"></el-checkbox>
                  </el-form-item>
                  <el-form-item label="校验规则" prop="requireExpress">
                    <div class="form-icon-btn" @click="setShowHandle('require')">
                      <span>校验规则</span>
                    </div>
                  </el-form-item>
                </el-form>
              </el-collapse-item>
            </el-collapse>
          </el-collapse-item>
        </el-collapse>
      </el-tab-pane>
      <el-tab-pane label="表单设置" name="3">
        <el-form class="form-set-tab-panel" label-position="top" :model="formsetting" label-width="80px" size='mini'>
          <el-form-item label="对齐方式">
            <div class="align-type-list">
              <!-- 顶对齐 -->
              <div :class="{'align-type-list-item top': true, 'active': formsetting.labelposition == 'top'}" @click="formsetting.labelposition = 'top';">
                <div>
                  <div class="short-line"></div>
                  <div class="long-line"></div>
                  <div class="short-line half"></div>
                  <div class="long-line"></div>
                </div>
              </div>
              <!-- 左对齐 -->
              <div :class="{'align-type-list-item left': true, 'active': formsetting.labelposition == 'left'}" @click="formsetting.labelposition = 'left';">
                <div style="display: flex;">
                  <div>
                    <div class="short-line" style="margin-top: 1px;"></div>
                    <div class="short-line half" style="margin-top: 4px;margin-bottom: 4px"></div>
                    <div class="short-line"></div>
                  </div>
                  <div style="margin-left: 2px;">
                    <div class="long-line"></div>
                    <div class="long-line" style="margin-top: 2px;"></div>
                    <div class="long-line" style="margin-top: 2px;"></div>
                  </div>
                </div>
              </div>
              <!-- 右对齐 -->
              <div :class="{'align-type-list-item right': true, 'active': formsetting.labelposition == 'right'}" @click="formsetting.labelposition = 'right';">
                <div style="display: flex;">
                  <div style="display: flex;flex-direction: column;align-items: flex-end;">
                    <div class="short-line" style="margin-top: 1px;"></div>
                    <div class="short-line half" style="margin-top: 4px;margin-bottom: 4px"></div>
                    <div class="short-line"></div>
                  </div>
                  <div style="margin-left: 2px;">
                    <div class="long-line"></div>
                    <div class="long-line" style="margin-top: 2px;"></div>
                    <div class="long-line" style="margin-top: 2px;"></div>
                  </div>
                </div>
              </div>
            </div>
          </el-form-item>
          <el-form-item label="标签宽度">
            <el-slider
              v-model="formsetting.labelwidth"
              :min="80"
              :max="400"
              :step="1"
              :show-input="true"
              :range="false"
              size='mini'
            ></el-slider>
          </el-form-item>
          <el-form-item label="显示方式">
            <div class="popup-type-list">
              <div :class="{'popup-type-list-item': true, 'active': (!formsetting.fullscreen && formsetting.popupType == 'dialog')}" @click="formsetting.popupType = 'dialog';formsetting.fullscreen=false;">弹框</div>
              <div :class="{'popup-type-list-item': true, 'active': (!formsetting.fullscreen && formsetting.popupType == 'drawer')}" @click="formsetting.popupType = 'drawer';formsetting.fullscreen=false;">抽屉</div>
              <div :class="{'popup-type-list-item': true, 'active': formsetting.fullscreen}" @click="formsetting.fullscreen=true;">全屏</div>
            </div>
          </el-form-item>
          <el-form-item v-if="!formsetting.fullscreen" label="显示宽度">
            <div style="width: 100%;display: flex;align-items: center;">
              <el-slider v-model="formsetting.popupWidth" :min="10" :max="100" :step="1" :show-input="false" :range="false" style="flex: 1;"></el-slider>
              <span class="slider-end-unit">{{formsetting.popupWidth}}%</span>
            </div>
          </el-form-item>
          <el-form-item v-if="fieldsdata.length == 0" label="表单名称">
            <el-select v-if="tableOption && tableOption.length > 0"  style="width:100%" v-model="formsetting.title" filterable placeholder="选择字段值作为表单名称" size="mini" clearable>
              <el-option  v-for='(dit, dindex) in domFieldList' :key="dit.prop + dindex" :label="dit.label" :value="dit.prop">
                <span style="float: left">{{ dit.label }}</span>
                <span style="float: right; color: #8492a6; font-size: 13px">{{ dit.prop}}</span>
              </el-option>
              <el-option  v-for='(item,index) in fieldKeys' :key="item.fieldKey + index" :label="item.fieldName" :value="item.fieldKey">
                <span style="float: left">{{ item.fieldName }}</span>
                <span style="float: right; color: #8492a6; font-size: 13px">{{ item.fieldKey}}</span>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item v-if="fieldsdata.length > 0" label="表单名称">
            <el-select style="width:100%" v-model="formsetting.title" filterable size="mini" placeholder="选择字段值作为表单名称" clearable>
              <el-option  v-for='(dit, dindex) in domFieldList' :key="dit.prop + dindex" :label="dit.label" :value="dit.prop">
                <span style="float: left">{{ dit.label }}</span>
                <span style="float: right; color: #8492a6; font-size: 13px">{{ dit.prop}}</span>
              </el-option>
              <el-option  v-for='(item,index) in fieldsdata' :key="index" :label="item.columnComment" :value="item.fieldName">
                <span style="float: left">{{ item.columnComment }}</span>
                <span style="float: right; color: #8492a6; font-size: 13px">{{ item.fieldName }}</span>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="回显设置" v-if="!isFlowDesign && !($route.query && $route.query.setEcho === 'false')" v-show="position != 'top'">
            <span slot="label" class="custom-label-slot-box">
              <span>回显设置</span>
              <el-tooltip class="item" effect="light" :content="attrIntroduce.formEcho" placement="top">
                <div class="icon-info">
                  <span>?</span>
                </div>
              </el-tooltip>
            </span>
            <div class="all-line-button" @click="setHttpHandle('echo')">
              <svg aria-hidden="true">
                <use xlink:href="#jvs-ui-icon-zhanghuguanli-weixuanzhong"></use>
              </svg>
              <span>回显设置</span>
              <i v-if="formsetting.dataEchoRequest" class="el-icon-delete" style="font-size: 14px;color: #F56C6C;" @click.stop="delFormEcho"/>
            </div>
          </el-form-item>
          <el-form-item v-if="!isAddForm && formType != 'flowable'" label="">
            <div class="full-log-item-box">
              <el-checkbox v-model="formsetting.logsEnable">变更记录</el-checkbox>
            </div>
          </el-form-item>
          <el-form-item v-if="!isAddForm && formType != 'flowable'" label="">
            <div class="full-log-item-box">
              <el-checkbox v-model="formsetting.dataLogEnable">填写评论</el-checkbox>
            </div>
          </el-form-item>
        </el-form>
      </el-tab-pane>
      <el-tab-pane v-if="formType !== 'flowable' && !isDetail" label="按钮设置" name="4">
        <div class="form-button-setting-list">
          <div
            v-for="(button, btix) in formsetting.btnSetting"
            :key="'form-button-set-item-'+btix"
            :draggable="['empty', 'submit', 'print', 'cancel'].indexOf(button.buttonType) == -1"
            @dragstart="moveBtnStart(button)"
            @dragenter="moveBtning(button)"
            @dragend="moveBtnEnd"
            :class="{'form-button-setting-list-item': true, 'open': buttonOpenStatus[button.permissionFlag], 'target': (moveTarget && (button.permissionFlag == moveTarget.permissionFlag))}">
            <div class="heade">
              <div class="name">{{button.name}}</div>
              <div class="heade-tool">
                <div v-if="!button.flag" class="con-box">
                  <svg v-if="formType == 'flowable' ? (!button.flag) : (['empty', 'submit', 'print', 'cancel'].indexOf(button.buttonType) == -1)" aria-hidden="true" @click="setHttpHandle('normal', button, btix)">
                    <use xlink:href="#jvs-ui-icon-quanxianshezhi"></use>
                  </svg>
                  <span v-if="formType == 'flowable' ? (!button.flag) : (['empty', 'submit', 'print', 'cancel'].indexOf(button.buttonType) == -1)" class="divider-line"></span>
                  <svg aria-hidden="true" @click="deleteItemOfBtn(btix, button, formsetting)">
                    <use xlink:href="#jvs-ui-icon-shanchuyonghu"></use>
                  </svg>
                  <span class="divider-line"></span>
                  <svg class="move-icon" aria-hidden="true" @mousedown="moveBtnStart(button)">
                    <use xlink:href="#icon-jvs-tuodong"></use>
                  </svg>
                  <span class="divider-line"></span>
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
                    <el-input v-model="button.name" placeholder="请输入按钮名称" size="mini"></el-input>
                  </div>
                </div>
                <div class="item-body-item">
                  <span class="label">启用</span>
                  <div class="con">
                    <el-checkbox size="mini" v-model="button.enable" @change="buttonEnableHandle(button)"></el-checkbox>
                    <el-button v-if="button.enable" type="text" style="margin-left:10px;" @click="openButtonFormula(button)">配置公式</el-button>
                  </div>
                </div>
                <div v-if="formType == 'flowable' ? (!button.flag) : (['empty', 'submit', 'print', 'cancel'].indexOf(button.buttonType) == -1)" class="item-body-item">
                  <span class="label">业务逻辑</span>
                  <div class="con">
                    <el-button type="text" @click="setHttpHandle('normal', button, btix)">配置</el-button>
                    <el-tooltip class="item" effect="light" :content="attrIntroduce.rule" placement="top">
                      <div class="icon-info">
                        <span>?</span>
                      </div>
                    </el-tooltip>
                  </div>
                </div>
                <div v-if="['empty', 'submit', 'print', 'cancel'].indexOf(button.buttonType) == -1" class="item-body-item">
                  <span class="label">校验</span>
                  <div class="con">
                    <el-checkbox size="mini" v-model="button.validateable"></el-checkbox>
                  </div>
                </div>
                <div v-if="['empty', 'submit', 'print', 'cancel'].indexOf(button.buttonType) == -1" class="item-body-item">
                  <span class="label">关闭弹框</span>
                  <div class="con">
                    <el-checkbox size="mini" v-model="button.closeable"></el-checkbox>
                  </div>
                </div>
              </div>
            </el-collapse-transition>
          </div>
          <div class="add-button-tool" @click="addOneButton">
            <i class="el-icon-plus"></i>
            <span>增加按钮</span>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
    <!-- 步骤条配置按钮 -->
    <el-dialog
      title="按钮配置"
      v-if="ownBtnVisible"
      :visible.sync="ownBtnVisible"
      append-to-body
      fullscreen
      :close-on-click-modal="false"
      :before-close="ownBtnClose">
      <el-table
        border
        :data="stepBtnList"
        class="tb-edit step-button-table"
        style="width: 100%"
        highlight-current-row
        size='mini'
        header-align="center"
        align="center"
      >
        <el-table-column label="按钮名称">
          <template slot-scope="scope">
            <el-form :model="scope.row">
              <el-form-item :style="scope.row.name?'margin-bottom: 0;':'margin-bottom:18px;'" :class="{'is-required':true, 'is-error':(!scope.row.name)}">
                <el-input
                  size="mini"
                  v-model="scope.row.name"
                  placeholder="请输入内容"
                ></el-input>
                <span v-show="!scope.row.name" class="el-form-item__error">必填</span>
              </el-form-item>
            </el-form>
          </template>
        </el-table-column>
        <!-- 请求地址 -->
        <el-table-column label="请求设置" width="200">
          <template slot-scope="scope">
            <el-form :model="scope.row">
              <el-form-item style="margin-bottom:0;">
                <jvs-button type='text' size="mini"  @click="setHttpHandle('step', scope.row, scope.$index)">配置</jvs-button>
              </el-form-item>
            </el-form>
          </template>
        </el-table-column>
        <el-table-column label="跳转设置" width="300">
          <template slot-scope="scope">
            <el-form :model="scope.row">
              <el-form-item v-if="form.type == 'step'" label="">
                <el-radio-group v-model="scope.row.type">
                  <el-radio label="last" v-if="stepBtnIndex !== 0">上一步</el-radio>
                  <el-radio label="next" v-if="stepBtnIndex < form.dicData.length -1">下一步</el-radio>
                  <el-radio label="no">不跳转</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-form>
          </template>
        </el-table-column>
        <el-table-column label="排序" width="200">
          <template slot-scope="scope">
            <div class="sort-row-column">
              <i v-if="scope.$index > 0" class="el-icon-top" @click="sortHandle('up', scope.$index)"></i>
              <i v-if="scope.$index < stepBtnList.length -1" class="el-icon-bottom" @click="sortHandle('down', scope.$index)"></i>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template slot-scope="scope">
            <div style="text-align:center;">
              <el-button
                size="mini"
                type="text"
                @click="deleteItemOfBtn(scope.$index, scope.row, 'step')"
              >删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <jvs-button size="mini" style="margin-top:10px;" @click="addStepBtn">添加</jvs-button>
    </el-dialog>
    <!-- 显示表达式设置 -->
    <el-dialog
      class="custom-header-dialog"
      width="50%"
      :title="showDisabledType == 'disabled' ? '条件禁用' : (showDisabledType == 'require' ? '动态校验' : '显示控制')"
      append-to-body
      :visible.sync="showExpressVisible"
      :close-on-click-modal="false"
      :before-close="showExpressClose">
      <div v-if="showExpressVisible" class="show-express">
        <div class="left">
          <el-tree
            :data="domTreeData"
            :props="{value: 'prop', children: 'children'}"
            :expand-on-click-node="false"
            :default-expand-all="true"
            @node-click="domNodeClick"></el-tree>
        </div>
        <div class="right">
          <jvs-form :formData="showExpressForm" :option="showExpressOption"></jvs-form>
        </div>
      </div>
      <el-row style="margin: 20px 0;display: flex;justify-content: center;align-items:center;">
        <jvs-button size="mini" type="primary" @click="saveShowExpress">保存</jvs-button>
        <jvs-button size="mini" @click="showExpressClose">取消</jvs-button>
      </el-row>
    </el-dialog>
    <!-- 数据筛选 -->
    <el-dialog
      class="custom-header-dialog"
      width="50%"
      title="数据筛选"
      append-to-body
      :visible.sync="dataFilterVisible"
      :close-on-click-modal="false"
      :before-close="dataFilterClose">
      <div v-if="dataFilterVisible" class="data-filter-div" style="padding: 0 20px;">
        <p>数据会按照如下条件进行筛选，前者为关联模型中的字段。</p>
        <div v-for="(dataFilterList, dfi) in dataFilterGroupList" :key="'data-fileter-div-'+dfi" class="data-filter-div-item">
          <el-row v-for="(item, index) in dataFilterList" :key="'data-filter-item-'+index" class="data-filter-item">
            <el-select v-model="item.fieldKey" placeholder="请选择字段" size="mini">
              <!-- connectFormPropsList -->
              <el-option
                v-for="(it, ix) in (dataFilterType == 'openModelTable' ? openFormDataMModelField : (['formbox', 'tableForm', 'reportTable'].indexOf(form.type) > -1 ? dataItemModelAllFieldList : dataModelAllFieldList))"
                v-show="needShow(dataFilterList, 'fieldKey', it.fieldKey)"
                :key="'connect-show-'+ix+'-'+it.fieldKey"
                :label="it.fieldName"
                :value="it.fieldKey">
              </el-option>
            </el-select>
            <el-select v-if="item.fieldKey" v-model="item.enabledQueryTypes" placeholder="请选择匹配规则" size="mini">
              <el-option v-for="(entype, enix) in getFilterEnabledQueryTypes(item.fieldKey, (dataFilterType == 'openModelTable' ? openFormDataMModelField : (['formbox', 'tableForm', 'reportTable'].indexOf(form.type) > -1 ? dataItemModelAllFieldList : dataModelAllFieldList)))" :key="'filter-enable-type-'+entype.value+'-'+enix" :label="entype.label" :value="entype.value"></el-option>
            </el-select>
            <el-select v-else v-model="item.enabledQueryTypes" placeholder="请选择匹配规则" size="mini">
              <el-option label="等于" value="eq"></el-option>
              <el-option label="模糊匹配" value="like"></el-option>
              <el-option label="等于空" value="isNull"></el-option>
            </el-select>
            <el-select v-if="item.enabledQueryTypes != 'isNull'" v-model="item.type" placeholder="请选择匹配类型" size="mini" @change="enabledQueryTypeHandle(item, index, dfi)">
              <el-option label="字段" value="prop"></el-option>
              <el-option label="自定义" value="cust"></el-option>
              <el-option label="角色" value="role"></el-option>
              <el-option label="部门" value="department"></el-option>
              <el-option label="岗位" value="job"></el-option>
              <el-option label="用户" value="user"></el-option>
            </el-select>
            <el-select v-if="item.type == 'prop' && !form.parentType && item.enabledQueryTypes != 'isNull'" v-model="item.value" placeholder="请选择表单字段" size="mini">
              <el-option label="数据id" value="id">
                <span style="float: left">数据id</span>
                <span style="float: right; color: #8492a6; font-size: 13px">id</span>
              </el-option>
              <el-option  v-for='(item,index) in dataFilterDom' :key="item.prop + '-data-filter-' + index" :label="item.label" :value="item.prop">
                <span style="float: left">{{ item.label }}</span>
                <span style="float: right; color: #8492a6; font-size: 13px">{{ item.prop}}</span>
              </el-option>
            </el-select>
            <!--  ['formbox', 'tableForm', 'reportTable'].indexOf(form.parentType) > -1 -->
            <el-cascader
              size="mini"
              v-if="item.type == 'prop' && form.parentType && item.enabledQueryTypes != 'isNull'"
              v-model="item.value"
              :options="dataFilterDom"
              :props="{ expandTrigger: 'hover', checkStrictly: false, label: 'label', value: 'prop' }">
            </el-cascader>
            <!-- <el-input v-if="item.type == 'cust' && item.enabledQueryTypes != 'isNull' && (!form.searchable && form.type != 'tableForm')" v-model="item.value" size="mini" style="width:auto;"></el-input> -->
            <el-select v-if="item.type == 'cust' && item.enabledQueryTypes != 'isNull' && (true || form.searchable || form.type == 'tableForm')" v-model="item.value" placeholder="请选择或输入" size="mini" filterable allow-create multiple collapse-tags>
              <el-option v-for="(fi, fix) in getAttrVal('values', item.fieldKey)" :key="'fitem-'+fix" :label="fi.name" :value="fi.value"></el-option>
            </el-select>
            <userForm :ref="`userForm_${dfi}_${index}`" v-if="['role', 'department', 'job', 'user'].indexOf(item.type) > -1 && item.enabledQueryTypes != 'isNull'" :form="item" prop="value" :selectable="true" :infoable="true" :type="item.type"/>
            <i class="el-icon-delete" style="cursor: pointer;" @click="deleteDataFilter(index, dataFilterList)"></i>
          </el-row>
          <p>
            <el-button size="mini" @click="addDataFilter(dfi)">添加</el-button>
          </p>
          <div class="delete-data-filter-tool">
            <i class="el-icon-delete" style="cursor: pointer;" @click="deleteDataFilter(dfi, dataFilterGroupList)"></i>
          </div>
        </div>
        <p>
          <el-button size="mini" @click="addDataFilterGroup">添加一组</el-button>
        </p>
      </div>
      <el-row style="display: flex;justify-content: center;align-items:center;margin-bottom: 20px;">
        <jvs-button size="mini" type="primary" @click="dataFilterSubmit">确定</jvs-button>
        <jvs-button size="mini" @click="dataFilterClose">取消</jvs-button>
      </el-row>
    </el-dialog>
    <!-- 数据联动 -->
    <el-dialog
      class="custom-header-dialog"
      width="50%"
      title="数据联动"
      append-to-body
      :visible.sync="dataLinkageVisible"
      :close-on-click-modal="false"
      :before-close="dataLinkageClose">
      <div v-if="dataLinkageVisible" style="padding: 0 20px;">
        <el-row>
          <p>关联模型</p>
          <el-select
            style="width:100%"
            v-model="dataLinkageModelId" placeholder="请选择关联模型" size="mini"
            @change="changeLinkModelPropList"
            filterable
            clearable
          >
            <el-option
              v-for="item in dataModelList"
              :key="item.id"
              :label="item.name"
              :value="item.id">
            </el-option>
          </el-select>
        </el-row>
        <p v-if="dataLinkageModelId">数据会按照如下条件进行联动。</p>
        <div v-if="dataLinkageModelId">
          <el-row v-for="(item, index) in dataLinkageList" :key="'data-filter-item-'+index" class="data-filter-item-linkage">
            <el-select v-model="item.value" placeholder="请选择表单字段" size="mini" class="al-item">
              <el-option v-if="createOrigin == 'leftTree'" label="id" value="id"></el-option>
              <el-option  v-for='(item,index) in formulaEnableDom' :key="item.prop + '-data-filter-' + index" :label="item.label" :value="item.prop">
                <span style="float: left">{{ item.label }}</span>
                <span style="float: right; color: #8492a6; font-size: 13px">{{ item.prop}}</span>
              </el-option>
            </el-select>
            <span class="al-item">等于</span>
            <el-select v-model="item.fieldKey" placeholder="请选择字段" size="mini" class="al-item">
              <el-option
                v-for="(it, ix) in dataLinkModelAllFieldList"
                :key="'connect-show-'+it.fieldKey+'-condi-'+ix"
                :label="it.fieldName"
                :value="it.fieldKey">
              </el-option>
            </el-select>
            <i class="el-icon-delete" style="cursor: pointer;" @click="deleteDataFilter(index, dataLinkageList)"></i>
          </el-row>
        </div>
        <p v-if="dataLinkageModelId">
          <el-button size="mini" @click="addDataLinkage">添加</el-button>
        </p>
        <el-row v-if="dataLinkageModelId" class="data-filter-item-linkage">
          <span class="al-item" style="display:block;width:150px;height:28px;line-height: 28px;cursor:not-allowed;background-color:#F5F7FA;
            border-color: #E4E7ED;color: #C0C4CC;border-radius: 4px;border: 1px solid #DCDFE6;padding:0 15px;font-size:12px;">{{form.label}}</span>
          <span class="al-item">联动显示为</span>
          <el-select v-model="form.linkageFieldKey" placeholder="请选择字段" size="mini" class="al-item">
            <el-option
              v-for="(it, ix) in dataLinkModelAllFieldList"
              :key="'connect-show-'+it.fieldKey+'-'+ix"
              v-if="form.type == 'input' ? true :
                (['image', 'imageUpload'].indexOf(form.type) > -1 ?
                (['image', 'imageUpload'].indexOf(it.fieldType) > -1) :
                (['file', 'fileUpload'].indexOf(form.type) > -1 ?
                (['file', 'fileUpload'].indexOf(it.fieldType) > -1) :
                (it.fieldType == form.type)))
                || true"
              :label="it.fieldName"
              :value="it.fieldKey">
              <span style="float: left">{{ it.fieldName }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">{{ it.fieldKey }}</span>
            </el-option>
          </el-select>
          <span class="al-item">的对应值</span>
        </el-row>
      </div>
      <el-row style="display: flex;justify-content: center;align-items:center;margin-top: 20px;margin-bottom: 20px;">
        <jvs-button size="mini" type="primary" @click="dataLinkageSubmit">确定</jvs-button>
        <jvs-button size="mini" @click="dataLinkageClose">取消</jvs-button>
      </el-row>
    </el-dialog>
    <!-- 禁用状态动态显示  设置背景字体颜色 -->
    <el-dialog
      class="custom-header-dialog"
      width="50%"
      title="动态显示"
      append-to-body
      :visible.sync="expressDisplayVisible"
      :close-on-click-modal="false"
      :before-close="expressDisplayClose">
      <div v-if="expressDisplayVisible" style="padding: 20px;" class="express-display-form">
        <jvs-form :option="expressDisplayOption" :formData="expressDisplayForm" @submit="expressDisplaySubmit" @cancalClick="expressDisplayClose"></jvs-form>
      </div>
    </el-dialog>
    <!-- 网络设置  可以选择已有逻辑引擎 -->
    <el-dialog
      class="custom-header-dialog"
      title="网络设置"
      width="30%"
      append-to-body
      :visible.sync="netSetVisible"
      :close-on-click-modal="false"
      :before-close="netSetClose">
      <div v-if="netSetVisible" style="padding: 20px;">
        <div style="display: flex;align-items: center;">
          <el-button size="mini" type="primary" @click="newRuleSet" :loading="newRuleSetLoading">新建业务逻辑</el-button>
        </div>
        <div style="display: flex;align-items: center;margin-top: 15px;">
          <span style="margin-right: 10px;">选择逻辑</span>
          <el-select v-model="form[httpType]" size="mini" filterable clearable @change="eventHttpChange">
            <el-option v-for="rit in allRuleList" :key="'all-rule-item-'+rit.secret" :label="rit.name" :value="rit.secret"></el-option>
          </el-select>
          <i v-if="form[httpType]" class="el-icon-edit-outline form-icon-btn" style="margin-left: 10px;cursor: pointer;" @click="viewRule(httpType)"></i>
        </div>
        <div style="display: flex;align-items: center;margin-top: 15px;">
          <span style="margin-right: 10px;">支持扫描</span>
          <el-switch v-model="form.ruleScan"></el-switch>
          <span style="margin-left: 10px;font-size: 12px;">仅支持移动端扫描</span>
        </div>
      </div>
    </el-dialog>
    <!-- 内嵌列表页设置 -->
    <el-dialog
      class="custom-header-dialog"
      width="50%"
      title="列表设置"
      append-to-body
      :visible.sync="pageVisible"
      :close-on-click-modal="false"
      :before-close="pageSetClose">
      <div v-if="pageVisible" style="padding: 0 20px 20px 20px;">
        <div :style="'display: flex;margin-top: 15px;' + ((!form.pageQuery && form.pageQuery.length == 0) ? 'align-items: center;' : '')">
          <span style="margin-right: 10px;word-break: keep-all;line-height: 28px;">数据过滤</span>
          <div class="custom-edit-table">
            <div class="custom-edit-table-header">
              <span>目标列表字段</span>
              <span>匹配逻辑</span>
              <span>当前表单字段</span>
              <span>操作</span>
            </div>
            <div v-if="form.pageQuery && form.pageQuery.length > 0" class="custom-edit-table-body">
              <div v-for="(item, inx) in form.pageQuery" :key="'page-query-'+inx" class="custom-edit-table-body-row">
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
                  <el-select v-model="item.value" size="mini" filterable placeholder="请选择当前表单字段" style="margin-right: 10px;">
                    <el-option label="数据id" value="id"></el-option>
                    <el-option v-for="fit in domTreeData" :key="'all-field-item-'+fit.prop" :label="fit.label" :value="fit.prop"></el-option>
                    <!-- <el-option v-for="fit in (fieldsdata.length > 0 ? fieldsdata : fieldKeys)" :key="'all-field-item-'+(fieldsdata.length > 0  ? fit.fieldKey : fit.fieldName)" :label="fieldsdata.length > 0  ? fit.fieldName : fit.columnComment" :value="fieldsdata.length > 0 ? fit.fieldKey : fit.fieldName"></el-option> -->
                  </el-select>
                </div>
                <div class="cell">
                  <el-button type="text" size="mini" style="color: #F56C6C;"  @click="delQuery(inx, form.pageQuery)">删除</el-button>
                </div>
              </div>
            </div>
            <div style="margin-top: 15px;">
              <el-button size="mini" type="primary" @click="addQuery">新增数据过滤</el-button>
            </div>
          </div>
        </div>
        <div :style="'display: flex;margin-top: 15px;' + ((!form.pageSearch && form.pageSearch.length == 0) ? 'align-items: center;' : '')">
          <span style="margin-right: 10px;word-break: keep-all;line-height: 28px;">查询条件</span>
          <div class="custom-edit-table">
            <div class="custom-edit-table-header">
              <span>目标列表字段</span>
              <span style="width: 100px;flex: none;">判断逻辑</span>
              <span>当前表单字段</span>
              <span>是否禁用</span>
              <span :style="hasPageSearchFilter(form.pageSearch) ? 'width: 100px;flex: none;' : ''">操作</span>
            </div>
            <div v-if="form.pageSearch && form.pageSearch.length > 0" class="custom-edit-table-body">
              <div v-for="(item, inx) in form.pageSearch" :key="'page-search-'+inx" class="custom-edit-table-body-row">
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
                  <el-select v-model="item.value" size="mini" filterable placeholder="请选择当前表单字段" style="margin-right: 10px;">
                    <el-option label="数据id" value="id"></el-option>
                    <el-option v-for="fit in domTreeData" :key="'all-field-item-search-'+fit.prop" :label="fit.label" :value="fit.prop"></el-option>
                    <!-- <el-option v-for="fit in (fieldsdata.length > 0 ? fieldsdata : fieldKeys)" :key="'all-field-item-'+(fieldsdata.length > 0  ? fit.fieldKey : fit.fieldName)" :label="fieldsdata.length > 0  ? fit.fieldName : fit.columnComment" :value="fieldsdata.length > 0 ? fit.fieldKey : fit.fieldName"></el-option> -->
                  </el-select>
                </div>
                <div class="cell">
                  <el-switch v-model="item.disabled" size="mini"></el-switch>
                </div>
                <div class="cell" :style="hasPageSearchFilter(form.pageSearch) ? 'width: 100px;flex: none;text-align: center;' : ''">
                  <el-button v-if="hasPageSearchFilter(item)" type="text" size="mini" @click="setPageSearchFilter(item, inx)">数据筛选</el-button>
                  <el-button type="text" size="mini" style="color: #F56C6C;" @click="delQuery(inx, form.pageSearch)">删除</el-button>
                </div>
              </div>
            </div>
            <div style="margin-top: 15px;">
              <el-button size="mini" type="primary" @click="addSearch">新增查询条件</el-button>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
    <!-- 动态数据项 -->
    <el-dialog
      class="custom-header-dialog"
      width="50%"
      title="动态数据项设置"
      append-to-body
      :visible.sync="dataItemExpressVisible"
      :close-on-click-modal="false"
      :before-close="dataItemExpressClose">
      <div v-if="dataItemExpressVisible" style="padding: 20px;">
        <div style="margin-bottom: 10px;">
          <h4 style="margin: 0;padding: 0;font-size: 16px;">显示项设置</h4>
          <p style="margin: 10px 0;">数据会按照绑定字段对应的数据值进行显示</p>
          <div>
            <span style="margin-right: 10px;color: #959595;font-size: 14px;">绑定字段</span>
            <el-select v-if="!form.parentType" v-model="dataItmeBindProp" placeholder="请选择表单字段" size="mini" clearable>
              <el-option  v-for='(item,index) in dataFilterDom' :key="item.prop + '-data-filter-' + index" :label="item.label" :value="item.prop">
                <span style="float: left">{{ item.label }}</span>
                <span style="float: right; color: #8492a6; font-size: 13px">{{ item.prop}}</span>
              </el-option>
            </el-select>
            <el-cascader
              v-else
              size="mini"
              v-model="dataItmeBindProp"
              :options="dataFilterDom"
              :props="{ expandTrigger: 'hover', checkStrictly: false, label: 'label', value: 'prop' }">
            </el-cascader>
          </div>
        </div>
        <div v-if="form.datatype == 'dataModel'" class="data-filter-div">
          <h4 style="margin: 0;padding: 0;font-size: 16px;">可选项设置</h4>
          <p style="margin: 10px 0;">数据会按照如下条件进行筛选，不满足条件将禁用，前者为关联模型中的字段。</p>
          <div v-for="(dataFilterList, dfi) in dataFilterGroupList" :key="'data-fileter-div-'+dfi" class="data-filter-div-item">
            <el-row v-for="(item, index) in dataFilterList" :key="'data-filter-item-'+index" class="data-filter-item">
              <el-select v-model="item.fieldKey" placeholder="请选择字段" size="mini">
                <el-option
                  v-for="(it, ix) in (['formbox', 'tableForm', 'reportTable'].indexOf(form.type) > -1 ? dataItemModelAllFieldList : dataModelAllFieldList)"
                  v-show="needShow(dataFilterList, 'fieldKey', it.fieldKey)"
                  :key="'connect-show-'+it.fieldKey+'-'+ix"
                  :label="it.fieldName"
                  :value="it.fieldKey">
                </el-option>
              </el-select>
              <el-select v-model="item.enabledQueryTypes" placeholder="请选择匹配规则" size="mini">
                <el-option label="等于" value="eq"></el-option>
                <el-option label="模糊匹配" value="like"></el-option>
                <el-option label="等于空" value="isNull"></el-option>
                <el-option label="包含" value="in"></el-option>
              </el-select>
              <el-select v-if="item.enabledQueryTypes != 'isNull'" v-model="item.type" placeholder="请选择匹配类型" size="mini" @change="enabledQueryTypeHandle(item, index)">
                <el-option label="字段" value="prop"></el-option>
                <el-option label="自定义" value="cust"></el-option>
              </el-select>
              <el-select v-if="item.type == 'prop' && !form.parentType && item.enabledQueryTypes != 'isNull'" v-model="item.value" placeholder="请选择表单字段" size="mini">
                <el-option  v-for='(item,index) in dataFilterDom' :key="item.prop + '-data-filter-' + index" :label="item.label" :value="item.prop">
                  <span style="float: left">{{ item.label }}</span>
                  <span style="float: right; color: #8492a6; font-size: 13px">{{ item.prop}}</span>
                </el-option>
              </el-select>
              <!--  ['formbox', 'tableForm', 'reportTable'].indexOf(form.parentType) > -1 -->
              <el-cascader
                size="mini"
                v-if="item.type == 'prop' && form.parentType && item.enabledQueryTypes != 'isNull'"
                v-model="item.value"
                :options="dataFilterDom"
                :props="{ expandTrigger: 'hover', checkStrictly: false, label: 'label', value: 'prop' }">
              </el-cascader>
              <!-- <el-input v-if="item.type == 'cust' && item.enabledQueryTypes != 'isNull' && (!form.searchable && form.type != 'tableForm')" v-model="item.value" size="mini" style="width:auto;"></el-input> -->
              <el-select v-if="item.type == 'cust' && item.enabledQueryTypes != 'isNull' && (true || form.searchable || form.type == 'tableForm')" v-model="item.value" placeholder="请选择或输入" size="mini" filterable allow-create multiple collapse-tags>
                <el-option v-for="(fi, fix) in getAttrVal('values', item.fieldKey)" :key="'fitem-'+fix" :label="fi.name" :value="fi.value"></el-option>
              </el-select>
              <i class="el-icon-delete" style="cursor: pointer;" @click="deleteDataFilter(index, dataFilterList)"></i>
            </el-row>
            <p>
              <el-button size="mini" @click="addDataFilter(dfi)">添加</el-button>
            </p>
            <div class="delete-data-filter-tool">
              <i class="el-icon-delete" style="cursor: pointer;" @click="deleteDataFilter(dfi, dataFilterGroupList)"></i>
            </div>
          </div>
          <p>
            <el-button size="mini" @click="addDataFilterGroup">添加一组</el-button>
          </p>
        </div>
        <el-row style="display: flex;justify-content: center;align-items:center;">
          <jvs-button size="mini" type="primary" @click="dataItemExpressSubmit">确定</jvs-button>
          <jvs-button size="mini" @click="dataItemExpressClose">取消</jvs-button>
        </el-row>
      </div>
    </el-dialog>
    <!-- 用户组件 范围设置 -->
    <el-dialog
      class="custom-header-dialog"
      width="40%"
      title="范围设置"
      append-to-body
      :visible.sync="userRangeVisible"
      :close-on-click-modal="false"
      :before-close="userRangeClose">
      <div v-if="userRangeVisible" style="padding: 20px;">
        <div style="display: flex;align-items: center;">
          <span style="color: #959595;font-size: 14px;margin-right: 10px;">范围筛选</span>
          <el-switch v-model="userRangeOption.sortable" size="mini"></el-switch>
        </div>
        <div v-if="userRangeOption.sortable" style="display: flex;align-items: center;margin-top: 10px;">
          <span style="color: #959595;font-size: 14px;margin-right: 10px;">范围类型</span>
          <el-radio-group v-model="userRangeOption.type" @change="userRangeTypeChange">
            <el-radio v-if="form.type == 'user'" label="currentDept">当前部门</el-radio>
            <el-radio v-if="form.type == 'user'" label="dept">指定部门</el-radio>
            <el-radio v-if="form.type == 'user'" label="user">指定人员</el-radio>
            <el-radio v-if="form.type == 'user'" label="job">指定岗位</el-radio>
            <el-radio v-if="form.type == 'user'" label="role">指定角色</el-radio>
            <el-radio v-if="['dept', 'department'].indexOf(form.type) > -1" label="current">当前部门及以下</el-radio>
            <el-radio v-if="['dept', 'department'].indexOf(form.type) > -1" label="samelevel">同级部门及以下</el-radio>
            <el-radio v-if="['dept', 'department'].indexOf(form.type) > -1" label="dept">指定部门及以下</el-radio>
          </el-radio-group>
        </div>
        <div v-if="['dept', 'user', 'job', 'role'].indexOf(userRangeOption.type) > -1" style="margin-top: 10px;">
          <div v-if="userRangeOption.scopes && userRangeOption.scopes.length > 0" style="position: relative; padding: 10px 0;">
            <el-tag
              v-for="tag in userRangeOption.scopes"
              size="small"
              style="margin-right: 4px;margin-bottom: 4px;"
              :key="tag.id"
              @close="handleDelUser(tag.id, userRangeOption.scopes)"
              closable>
              {{tag.name}}
            </el-tag>
          </div>
          <div :style="(userRangeOption.scopes && userRangeOption.scopes.length > 0)? 'margin-top: 10px;' : ''">
            <el-button type="primary" size="mini" icon="el-icon-plus" @click="addUSerRange">{{getDialogTitle(userRangeOption.type)}}</el-button>
          </div>
        </div>
        <div style="margin-top: 10px;display: flex;align-items: center;justify-content: center;">
          <jvs-button size="mini" type="primary" @click="userRangeSubmit">确定</jvs-button>
          <jvs-button size="mini" @click="userRangeClose">取消</jvs-button>
        </div>
      </div>
    </el-dialog>
    <userSeletor
      v-if="userRangeOption"
      ref="userSelector"
      :user-enable="userRangeOption.type === 'user'"
      :role-enable="userRangeOption.type === 'role'"
      :dept-enable="userRangeOption.type === 'dept'"
      :group-enable="userRangeOption.type === 'group'"
      :job-enable="userRangeOption.type === 'job'"
      :current-active-name="userRangeOption.type"
      :selectable="true"
      :is-radio="['dept', 'department'].indexOf(form.type) > -1 ? true : false"
      :dialog-title="getDialogTitle(userRangeOption.type)"
      @submit="userSubmit"
    ></userSeletor>

    <!-- option批量添加item -->
    <el-dialog
      class="custom-header-dialog"
      title="批量添加"
      :visible.sync="multipleAdd"
      width="30%"
      append-to-body
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
  </el-card>
</template>

<script>
import pinyin from 'js-pinyin'
import {getRegExpList, getConnectFormList, getConnectFormProps, getEncryptionList, delRuleBySecret} from '../api/form'
import {getDataModelDataFilter, getAllPageByApplication} from "@/views/page/api/list";
import { getButtonFormId } from '@/views/page/api/newDesign'
import { createRule, getDesignInfo } from '@/views/page/api/design'
import {guid} from "@/util/util";
import { getAllModel, getModelAllFields, getLinkModelAllFields, getAllRule } from '@/components/template/api'
import { deleteExec } from '@/components/basic-container/formula/api'
import userSeletor from '@/components/basic-assembly/userSelector'
import userForm from '@/components/basic-assembly/userForm'
const javaKeyWords =
["abstract","assert","boolean","break","byte","case","catch","char","class","const","continue","default",
"do","double","else","enum","extends","final","finally","float","for","goto","if","implements","import","instanceof"
,"int","interface","long","native","new","package","private","protected","public","return","short","static","strictfp",
"super","switch","synchronized","this","throw","throws","transient","try","void","volatile","while"]
function isReg(reg) {
  let isReg;
  try {
    isReg = eval(reg) instanceof RegExp
  } catch (e) {
    isReg = false
  }
  return isReg
}
export default {
  props: {
    // 中间是否拖动
    drag2: {
      type: String,
      default: () => {
        return '1'
      }
    },
    // 组件对象
    form: {
      type: Object,
      default: () => {
        return {
          // 展示字段
          showFrom: [],
          // 校验
          rules: [],
          props: {
            label: '',
            value: ''
          }
        }
      }
    },
    jvsAppId: {
      type: String
    },
    dataModelId: {
      type: String
    },
    designId: {
      type: String
    },
    designName: {
      type: String
    },
    openByForm: {
      type: String
    },
    // 激活表单key
    activeForm: {
      type: String,
      default: ''
    },
    formsetting: {
      type: Object
    },
    // 是否为流程
    isFlowable: {
      type: Boolean,
      default: () => {
        return false
      }
    },
    // 是否为列表页详情按钮
    isDetail: {
      type: Boolean,
      default: () => {
        return false
      }
    },
    // 是否为列表页新增按钮
    isAddForm: {
      type: Boolean,
      default: () => {
        return false
      }
    },
    // 流程
    flowableDom: {
      type: Object,
      default: () => {
        return {}
      }
    },
    // 按钮类型
    fineGrainedType: {
      type: String
    },
    // 传递过来的可选字段
    fields: {
       type: Array,
       default: () => {
         return []
       }
    },
    // 组件对象
    control: {
      type: Object,
      default: () => {
        return {
          // 展示字段
          show: [],
          hide: []
        }
      }
    },
    // 多级表单 / 步骤表单  结构
    levelOption: {
      type: Object,
      default: () => {
        return {
          column: []
        }
      }
    },
    formType: {
      type: String,
      default: () => {
        return 'normalForm'
      }
    },
    btnPostUrl: {
      type: String,
      default: () => {
        return ''
      }
    },
    btnType: {
      type: String,
      default: () => {
        return ''
      }
    },
    position: {
      type: String,
      default: () => {
        return ''
      }
    },
    columnNameList: {
      type: Array
    },
    classifyDictList: {
      type: Array
    },
    systemDictList: {
      type: Array
    },
    databaseName: {
      type: String
    },
    allTable: {
      type: Array
    },
    isFlowDesign: {
      type: Boolean
    },
    domList: {
      type: Array,
      default: () => {
        return []
      }
    },
    tableOption: {
      type: Array,
      default: () => {
        return []
      }
    },
    showBtnSetting: {
      type: Number
    },
    createOrigin: {
      type: String
    }
  },
  components: {
    userSeletor,
    userForm
  },
  data() {
    var CheckRegExp = (rule, value, callback) => {
      if (!value) {
        callback();
      }
      let r = isReg('/' + value + '/')
      if(r == false) {
        callback(new Error('正则表达式错误'));
      }else{
        callback();
      }
    };
    return {
      userInfo: {},
      activeName: '3',
      needChangeActiveForm: false, // 是否需要改变激活表单key
      formclass: '',
      sqlTypeList: [
        {label:'varchar',value:'varchar'},
        {label:'bit',value:'bit'},
        {label:'char',value:'char'},
        {label:'tinyblob',value:'tinyblob'},
        {label:'tinytext',value:'tinytext'},
        {label:'binary',value:'binary'},
        {label:'clob',value:'clob'},
        {label:'blob',value:'blob'},
        {label:'text',value:'text'},
        {label:'mediumblob',value:'mediumblob'},
        {label:'mediumtext',value:'mediumtext'},
        {label:'longblob',value:'longblob'},
        {label:'longtext',value:'longtext'},
        {label:'tinyint',value:'tinyint'},
        {label:'smallint',value:'smallint'},
        {label:'mediumint',value:'mediumint'},
        {label:'int',value:'int'},
        {label:'integer',value:'integer'},
        {label:'year',value:'year'},
        {label:'bigint',value:'bigint'},
        {label:'float',value:'float'},
        {label:'double',value:'double'},
        {label:'decimal',value:'decimal'},
        {label:'date',value:'date'},
        {label:'time',value:'time'},
        {label:'datetime',value:'datetime'},
        {label:'timestamp',value:'timestamp'},
      ],
      validateBool: false,
      validateRule: false,
      defaultLimit: false,
      defaultLimitText: '',
      allForm: [],
      btnSettingList: [
        {
          name: '提交',
          buttonType: 'submit',
          flag: true,
          enable: true
        },
        {
          name: '重置',
          buttonType: 'empty',
          flag: true,
          enable: true
        },
        {
          name: '打印',
          buttonType: 'print',
          flag: true,
          enable: true
        },
        {
          name: '取消',
          buttonType: 'cancel',
          flag: true,
          enable: true
        },
      ],
      isFlowableBoolean: false, // 是否关联流程
      flowableDomString: "", // 关联的流程标识
      availableList: [], // 可启动的流程列表
      httpData: {}, // 回显 提交 配置
      httpType: '', // 地址类型  submit提交  echo回显
      relationProp: [], // 表格项字段列表   子集的加入parentKey，匹配对应的列表
      tableColumnList: [], // 选表后的字段列表
      btnRowIndex: -1, // 当前自定义按钮设置的index
      connectFormOption: [], // 关联表单列表
      connectFormPropsList: [], // 关联表单显示传递值列表
      tablesItem: [], // 数据源对应的数据表的字段
      regExpOption: [], // 系统的正则
      regCheck: [{validator: CheckRegExp, trigger: ['blur', 'change']}],
      activeAttrs: [], // 属性设置折叠
      activeAttr: '',
      formListOption: [], // 表单列表
      stepBtnList: [], // 步骤条 单项 按钮配置项
      ownBtnVisible: false, // 步骤按钮配置
      stepBtnIndex: -1,
      itemTableOption: [], // 自定义选择组件
      eventList: ['button', 'input', 'textarea', 'inputNumber', 'select', 'slider', 'switch', 'datePicker', 'timeSelect', 'timePicker',
      'radio', 'checkbox', 'imageUpload', 'fileUpload', 'htmlEditor', 'cascader', 'datasource', 'chinaArea', 'department', 'role', 'user', 'post'],
      menuFixTemp: '',
      showExpressVisible: false,
      showDisabledType: '',
      domTreeData: [],
      showExpressForm: {
        relation: []
      },
      showExpressOption: {
        cancal: false,
        btnHide: true,
        column: [
          {
            label: '是否清空值',
            prop: 'empty',
            type: 'switch'
          },
          {
            label: '逻辑判定',
            prop: 'showOperator',
            type: 'radio',
            dicData: [
              {label: '且', value: '&&'},
              {label: '或', value: '||'}
            ],
            defaultValue: '||'
          },
          {
            label: '',
            prop: 'relation',
            type: 'tableForm',
            editable: true,
            addBtn: false,
            delBtn: true,
            hideTop: true,
            align: 'left',
            menuAlign: 'left',
            tableColumn: [
              {
                label: '字段名',
                prop: 'label',
                disabled: true
              },
              {
                label: '字段名',
                prop: 'prop',
                hide: true
              },
              {
                label: '比较值',
                prop: 'value'
              }
            ]
          }
        ]
      },
      imageUrl: '', // 上传图片占位图
      formulaEnableList: ['input', 'inputNumber', 'select', 'switch', 'datePicker', 'timeSelect', 'timePicker', 'radio', 'checkbox',
      'image', 'link', 'iframe', 'htmlEditor', 'cascader', 'file'], // 支持公式的组件
      dataFilterVisible: false, // 数据筛选弹框
      dataFilterGroupList: [], // 筛选条件组
      dataFilterList: [],
      dataLinkageVisible: false, // 数据联动弹框
      dataLinkageModelId: '', // 关联数据模型id
      dataLinkageList: [],
      dataModelList: [], // 数据模型列表
      dataModelAllFieldList: [], // 数据模型下所有字段
      dataLinkModelAllFieldList: [], // 数据联动模型下所有字段
      tmExList: [], // 脱敏规则列表
      dataItemModelAllFieldList: [], // 表格关联模型子组件可选字段
      parentDom: null,
      custFilterList: [], // 权限条件列表
      predefineColors: [
        '#ffd700',
        '#ff8c00',
        '#ff4500',
        '#c71585',
        '#FF99CC',
        '#FF6666',
        '#CCCCFF',
        '#CCCCCC',
        '#99CCFF',
        '#99CC99',
        '#90ee90',
        '#66CC66',
        '#669933',
        '#663366',
        '#490954',
        '#3471ff',
        '#333300',
        '#1e90ff',
        '#00ced1',
        '#003399'
      ],
      expressDisplayVisible: false,
      expressDisplayForm: {},
      expressDisplayOption: {
        emptyBtn: false,
        formAlign: 'top',
        column: [
          {
            label: ' 背景颜色',
            prop: 'backColor',
            type: 'colorPicker',
            clearable: true,
            span: 12
          },
          {
            label: '文本颜色',
            prop: 'textcolor',
            type: 'colorPicker',
            clearable: true,
            span: 12
          },
          {
            label: '动态配置',
            prop: 'conditionControl',
            type: 'tableForm',
            border: true,
            editable: true,
            addBtn: true,
            delBtn: true,
            align: 'center',
            menuAlign: 'center',
            tableColumn: [
              {
                label: '值',
                prop: 'value',
              },
              {
                label: '文本颜色',
                prop: 'color',
                type: 'colorPicker',
                clearable: true,
              },
              {
                label: '背景颜色',
                prop: 'bgcolor',
                type: 'colorPicker',
                clearable: true,
              }
            ]
          }
        ]
      },
      flowList: [], // 流程列表
      allRuleList: [], // 当前模型下的所有的逻辑引擎列表
      newRuleSetLoading: false, // 新增逻辑loading
      netSetVisible: false,
      btnEnableClose: false,
      pageList: [],
      pageVisible: false,
      pageFieldList: [],
      dataItemExpressVisible: false,
      dataItmeBindProp: '',
      userRangeVisible: false,
      userRangeOption: null,
      moveSource: null,
      moveTarget: null,
      buttonOpenStatus: {},
      attrIntroduce: {
        prop: '支持输入后选择或直接选择已有字段。',
        status: {
          disabled: '控件只可被查看不可编辑。',
          hide: '控件会被隐藏掉，表单提交时不会提交数据。'
        },
        searchable: '关联其它模型字段并筛选出某些字段的值直接展示在当前文本框中。',
        showExpress: '根据其它不同组件的值进行判断，控制当前组件是否隐藏。',
        disabledExpress: '根据其它不同组件的值进行判断，控制当前组件是否禁用。',
        eventHttp: '当控件操作完后会将表单的数据作为入参，发起调用当前设计的业务逻辑。',
        dataFilterable: '在表格或搜索中根据关联模型查询出对应的数据，并对数据过条件过滤。',
        dataLink: '根据其它控件的数据值作为查询条件，在其它数据模型中进行搜索，关联查询出某个字段的值，显示在当前组件。',
        dataItemExpressable: '根据绑定的字段值作为选项的显示条件。',
        formEcho: '配置业务逻辑用于表单第一次打开时直接回显相关业务数据。',
        rule: '通过可视化的拖拽编排，把多种不同的基础服务或功能节点拼装成业务功能。',
        collapsetags: '多选时是否将选中值按文字的形式展示，默认展示第一项+合并数量'
      },
      multipleAdd: false,
      multipleAddContent: '',
      eachotherActives: ['setvalue', 'conditionoption', 'blurhandle', 'event'],
      styleActives: ['tips', 'limit'],
      rulesActives: ['required', 'regexp', 'reqexp'],
      openFormDataMModelField: [],
      dataFilterType: '',
    }
  },
  created () {
    // console.log(this.allTable)
    // console.log(this.form)
    // 工作流表单设置
    if(this.isDetail) {
      this.btnSettingList.filter(bi => {
        bi.enable = false
      })
    }
    if(this.isAddForm) {
      this.btnSettingList = this.btnSettingList.filter(bi => {
        if(bi.buttonType != 'print') {
          return bi
        }
      })
    }
    if(this.formType == 'flowable') {
      this.formsetting.flag = true
      this.formsetting.btnSetting = [
        {name: '通过', url: 'ok', enable: true, flag: true},
        {name: '拒绝', url: 'fail', enable: true, flag: true},
        {name: '驳回', url: 'reject', enable: true, flag: true},
        {name: '保存', url: 'save', enable: true, flag: true},
        {name: '指派', url: 'zhipai', enable: true, flag: true},
        // {name: '委派', url: 'weipai', enable: true}
      ]
    }
    this.isFlowableBoolean = this.isFlowable
    this.flowableDomString = this.flowableDom.id
    this.allForm = this.levelOption.column

    // 初始化按钮设置
    if(!this.formsetting.btnSetting || this.formsetting.btnSetting.length < 1) {
      this.$set(this.formsetting, 'btnSetting', JSON.parse(JSON.stringify(this.btnSettingList)))
      if(this.isAddForm) {
        this.formsetting.btnSetting = this.formsetting.btnSetting.filter(bi => {
          if(bi.buttonType != 'print') {
            return bi
          }
        })
      }
    }
    if(!this.formsetting.labelposition || this.formsetting.labelposition == '') {
      this.formsetting.labelposition = 'top'
    }
    if(this.isDetail) {
      this.formsetting.btnSetting.filter(btn => {
        btn.enable = false
      })
    }
    // get post 只能自定义按钮
    if(this.btnType == 'network_post_url' || this.btnType == 'network_get_url') {
      this.formsetting.flag = true
    }
    // 表单弹出方式及宽度
    if(!this.formsetting.popupType) {
      this.$set(this.formsetting, 'popupType', 'dialog')
    }
    if(!this.formsetting.popupWidth) {
      this.$set(this.formsetting, 'popupWidth', 50)
    }
    // 提交记录默认显示
    // if(this.formsetting.logsEnable !== false) {
    //   this.$set(this.formsetting, 'logsEnable', true)
    // }

    if(!this.form.props) {
      this.$set(this.form, 'props', {label: '', value: ''})
    }
    if(!this.form.status) {
      this.$set(this.form, 'status', '')
    }
    if(!this.form.defaultOrigin) {
      this.$set(this.form, 'defaultOrigin', '')
    }

    if(this.form.menuFix) {
      this.menuFixTemp = this.form.menuFix
    }
    this.menuFixChange(this.menuFixTemp)

    this.keyChangehandle()
    this.noKeyWord()
    this.getConnectSourceHandle()
    this.getRegExpListHandle()
    this.getAllRuleListHandle()
    this.getAllPageList()
    if(this.form.type || !this.form.type) {
      this.getTmExList()
    }
    if(this.$route.query) {
      let keys = Object.keys(this.$route.query)
      if(keys.indexOf('isAddForm') > -1 || keys.indexOf('isDetail') > -1) {
        this.btnEnableClose = true
      } 
    }
    // 按钮设置
    if(this.formType !== 'flowable' && !this.isDetail) {
      this.buttonSetting()
    }
    this.$forceUpdate()
  },
  computed:{
    fieldsdata () {
      let ret = []
      if(this.columnNameList) {
        ret = this.columnNameList
      }
      return ret
    },
    fieldKeys () {
      const list = []
      for (let i in this.tableOption) {
        const index = this.domList.findIndex(item => {
          return item.prop === this.tableOption[i].fieldKey
        })
        if (index === -1) {
          list.push({ fieldKey: this.tableOption[i].fieldKey, fieldName: this.tableOption[i].fieldName })
        }
      }
      return list
    },
    zzcindex () {
      return this.drag2 === '1' ? -10 : 10
    },
    otherTable: {
      get () {
        let temp = []
        for(let i in this.allTable) {
          temp.push(this.allTable[i])
        }
        return temp
      },
      set () {}
    },
    formulaEnableDom () {
      if(this.form.parentType && this.form.parentKey) {
        let list = []
        switch(this.form.parentType) {
          case 'tableForm':
            this.domList.filter(item => {
              let pks = this.form.parentKey.split('.')
              if(pks[pks.length - 1] == item.prop) {
                list = item.tableColumn.filter(it => {
                  return it.prop != this.form.prop
                })
              }else{
                if(['tab', 'step'].indexOf(item.type) > -1) {
                  if(item.column) {
                    for(let ci in item.column) {
                      this.findParentDom(item.column[ci], this.form.parentKey)
                      if(this.parentDom && this.parentDom.tableColumn) {
                          list = this.parentDom.tableColumn.filter(it => {
                          return it.prop != this.form.prop
                        })
                      }
                    }
                  }
                }
              }
            });
            break;
          default: list = this.domList.filter(item => { return this.formulaEnableList.indexOf(item.type) > -1 });break;
        }
        return list
      }else{
        return this.domList.filter(item => {
          return this.formulaEnableList.indexOf(item.type) > -1
        })
      }
    },
    dataFilterDom () {
      if(this.form.parentType && this.form.parentKey) {
        let list = []
        this.getFilterDomTree(JSON.parse(JSON.stringify(this.domList)), list)
        return list
      }else {
        return this.domList.filter(item => {
          return item.prop != this.form.prop
        })
      }
    },
    domFieldList () {
      let arr = []
      let enableList = ['input', 'textarea', 'inputNumber', 'select', 'slider', 'datePicker', 'timeSelect', 'timePicker', 'radio', 'checkbox', 'cascader', 'chinaArea', 'department', 'role', 'user', 'post', 'serialNumber']
      let hasIn = false
      this.domList.filter(dom => {
        if(dom.prop == this.formsetting.title) {
          hasIn = true
        }
        if(enableList.indexOf(dom.type) > -1) {
          if(this.fieldsdata && this.fieldsdata.length > 0) {
            let index = -1
            this.fieldsdata.filter((fit, fix) => {
              if(fit.fieldName == dom.prop) {
                index = fix
              }
              if(fit.fieldName == this.formsetting.title) {
                hasIn = true
              }
            })
            if(index == -1) {
              arr.push({ label: dom.label, prop: dom.prop })
            }
          }else{
            let index = -1
            this.fieldKeys.filter((fit, fix) => {
              if(fit.fieldKey == dom.prop) {
                index = fix
              }
              if(fit.fieldKey == this.formsetting.title) {
                hasIn = true
              }
            })
            if(index == -1) {
              arr.push({ label: dom.label, prop: dom.prop })
            }
          }
        }
      })
      if(!hasIn) {
        this.$set(this.formsetting, 'title', '')
      }
      return arr
    },
  },
  methods: {
    // 删除选项
    handleDelete (index) {
      if(this.form.deleteOption){
        this.form.deleteOption(index)
      }else{
        if(this.form.type == 'tab') {
          this.form.dicData.splice(index, 1)
        }else{
          if(this.form.dicData && this.form.dicData.length > 0) {
            this.form.dicData.splice(index, 1)
          }
          if(this.form.option && this.form.option.length > 0) {
            this.form.option.splice(index, 1)
          }
        }
      }
      // 删除dicData时删除对应的column项
      if(this.form.type === 'tab') {
        let temp = {}
        for(let i in this.form.dicData) {
          let keys = Object.keys(this.form.column)
          let name = this.form.dicData[i].name
          if(keys.indexOf(name) > -1) {
            temp[name] = this.form.column[name]
          }
        }
        this.form.column = temp
      }
      // 删除步骤时，调整当前项
      if(this.form.type === 'step') {
        if(this.form.dicData && this.form.dicData.length > 0) {
          this.form.activeName = this.form.dicData[0].name
          this.$forceUpdate()
        }
      }
    },
    // 添加选项
    addoption () {
      if(this.form.type == 'tab') {
        if(this.form.addoption){
          this.form.addoption()
        }else{
          if(this.form.type == 'tab') {
            this.form.dicData.push({label: '新的选择', name: ('newName' + this.form.dicData.length)})
          }else{
            this.form.dicData.push({label: '新的选择', value: ('newValue' + this.form.dicData.length)})
          }
        }
      }else{
        this.multipleAddHandle()
      }
    },
    keyChangehandle (type) {
      for(let i in this.fieldsdata){
        if(this.fieldsdata[i].aliasColumnName === this.form.prop || this.fieldsdata[i].fieldName === this.form.prop) {
          // 下拉选择
          if(this.form.type == 'select') {
            // url
            if(this.fieldsdata[i].associatedFieldHttp) {
              this.form.url = this.fieldsdata[i].associatedFieldHttp
              this.form.datatype = 'url'
            }
            // label value 配置
            if(!this.form.props) {
              this.form.props = {
                label: '',
                value: ''
              }
            }
            if(this.fieldsdata[i].associatedFields) {
              this.form.props.value = this.fieldsdata[i].associatedFields.columnName
            }
            if(this.fieldsdata[i].displayField) {
              this.form.props.label = this.fieldsdata[i].displayField.columnName
            }
          }
          // 修改对应的label placeholder
          this.form.label = this.fieldsdata[i].columnComment
          this.form.placeholder = (this.form.type == 'select' ? '请选择' : '请输入') + this.fieldsdata[i].columnComment
        }
      }
      this.noKeyWord()
      if(type == 'other') {
        for(let f in this.otherTable) {
          if(this.otherTable[f].tableName == this.form.prop) {
            this.relationProp = this.otherTable[f].autoTableFields
          }
        }
      }
    },
    // 过滤关键词
    noKeyWord () {
      if(javaKeyWords.indexOf(this.form.prop) !== -1) {
        this.validateBool = true
        this.form.prop = ''
      }else{
        this.validateBool = false
      }
      this.validateRule = false
      if(/^[A-Za-z]+[A-Za-z0-9_]*$/.test(this.form.prop)) {
        this.validateRule = false
      }else{
        this.validateRule = true
      }
    },
    // 限制长度
    limitDefaultHandle () {
      if(this.form.type === "Checkbox") {
        if(this.form.defaultValue) {
          let arr = this.form.defaultValue.split(",")
          if(arr.length >= this.form.min && arr.length <= this.form.max) {
            this.defaultLimit = false
          }
          if(arr.length < this.form.min) {
            this.defaultLimit = true
            this.defaultLimitText = '最少填写' + this.form.min + '个值'
          }
          if(arr.length > this.form.max) {
            this.defaultLimit = true
            this.defaultLimitText = '最多填写' + this.form.max + '个值'
          }
        }else{
          this.defaultLimit = false
        }
      }
    },
    // 添加表单
    addOneForm () {
      let temp = {
        label: '表单'+ new Date().getTime(),
        name: 'form' + new Date().getTime(),
        defaultData: {},
        formOption: {
          column: []
        }
      }
      this.allForm.push(temp)
      this.$emit('updateForms', this.allForm)
      this.$forceUpdate()
    },
    // 删除表单
    deleteTabForm (index, row) {
      this.allForm.splice(index, 1)
      this.$emit('updateForms', this.allForm)
      // 删除的是当前激活的表单
      if(row.name == this.activeForm) {
        this.$emit('changeActiveForm', null)
      }
      this.$forceUpdate()
    },
    // 改变表单字段
    changeFormId (str) {
      this.$emit('updateForms', this.allForm)
      if(this.needChangeActiveForm) {
        this.$emit('changeActiveForm', str)
      }
    },
    // 改变字段前先判断是否为激活状态
    isActive (str) {
      if(str == this.activeForm) {
        this.needChangeActiveForm = true
      }else{
        this.needChangeActiveForm = false
      }
    },
    deleteItemOfBtn (index, item, type) {
      if(type == 'step') {
        this.form.dicData[this.stepBtnIndex].btns.splice(index, 1)
      }else{
        this.formsetting.btnSetting.splice(index, 1)
      }
      this.$forceUpdate()
    },
    addOneButton () {
      if(this.formType == 'flowable') {
        this.formsetting.btnSetting.push({buttonType: 'custom', permissionFlag: this.designId + '-custom-' + guid()})
      }else{
        this.formsetting.btnSetting.push({name: ('按钮' + (this.formsetting.btnSetting.length + 1)), buttonType: 'custom', permissionFlag: this.designId + '-custom-' + guid(), enable: false, closeable: this.btnEnableClose})// , flag: false
      }
      this.$forceUpdate()
    },
    isRepeat (str) {
      if(!str) {
        return false
      }
      let arr = []
      for(let i in this.allForm) {
        arr.push(this.allForm[i].name)
      }
      let counts = (arr, value) => arr.reduce((a, v) => v === value ? a + 1 : a + 0, 0)
      if(counts(arr, str) < 2) {
        return false
      }else{
        return true
      }
    },
    // 改变flowable设置参数
    changeFlowable () {
      this.$emit('flowChange', {
        isFlowable: this.isFlowableBoolean,
        flowableDom: this.getItemOfList(this.flowableDomString, 'id', this.availableList)
      })
    },
    getItemOfList (val, attr, list) {
      let tp = {}
      for(let i in list) {
        if(list[i][attr] == val) {
          tp = list[i]
        }
      }
      return tp
    },
    oninput (val, attr) {
      this.$set(this.form.props, attr, val)
    },
    // 打开地址设置
    setHttpHandle (type, row, index) {
      this.httpType = type
      if((row && row.buttonType == 'submit') || type == 'submit') {
        this.httpType = row ? row.buttonType : type
        if(this.formsetting.dataSubmissionRequest) {
          this.httpData = {
            http: JSON.parse(JSON.stringify(this.formsetting.dataSubmissionRequest))
          }
        }
      }else if(type == 'echo'){
        if(this.formsetting.dataEchoRequest) {
          this.$openUrl(`/rule-design-ui/#/ruleDesign?id=${this.formsetting.dataEchoRequest}&componentId=${this.designId}&jvsAppId=${this.jvsAppId}&name=${this.designName}-回显`, '_blank')
        } else {
          createRule({jvsAppId: this.jvsAppId, componentId: this.designId, name: this.designName + '-回显', designId: this.designId, componentType: 'form'}).then(res => {
            if (res.data && res.data.code == 0) {
              this.$set(this.formsetting, 'dataEchoRequest', res.data.data)
              this.$openUrl(`/rule-design-ui/#/ruleDesign?id=${res.data.data}&componentId=${this.designId}&jvsAppId=${this.jvsAppId}&name=${this.designName}-回显`, '_blank')
              this.$root.eventBus.$emit('toolEvent', 'ruleSave')
            }
          })
        }
      }else if(type == 'tableEcho') {
        if(this.form.tableEchoRequest) {
          this.httpData = {
            http: JSON.parse(JSON.stringify(this.form.tableEchoRequest))
          }
        }
      }else if(type == 'tableDelete'){
        if(this.form.tableDeleteRequest) {
          this.httpData = {
            http: JSON.parse(JSON.stringify(this.form.tableDeleteRequest))
          }
        }
      }else if(['eventHttp', 'optionHttp'].indexOf(type) > -1){
        // console.log(this.form)
        this.netSetVisible = true
      }else if(type == 'api'){
        if(row.apiHttp) {
          this.httpData = {
            http: JSON.parse(JSON.stringify(row.apiHttp))
          }
        }
      }else if(type == 'upload'){
        if(row.uploadHttp) {
          this.httpData = {
            http: JSON.parse(JSON.stringify(row.uploadHttp))
          }
        }else{
          this.httpData = {
            http: {
              httpMethod: "POST",
              requestContentType: "MULTIPART",
              responseContentType: "JSON",
              url: "/mgr/jvs-auth/upload/jvs-form-design",
              parameters: { module: '/jvs-ui/form/' }
            }
          }
        }
      }else if(type == 'stepEcho'){
        this.btnRowIndex = index
        if (row.echoHttp) {
          this.$openUrl(`/rule-design-ui/#/ruleDesign?id=${row.echoHttp}&componentId=${row.id}&jvsAppId=${this.jvsAppId}&name=${row.label}`, '_blank')
        } else {
          createRule({jvsAppId: this.jvsAppId, componentId: row.id, name: row.label, designId: this.designId, componentType: 'form'}).then(res => {
            if (res.data && res.data.code == 0) {
              row.echoHttp = res.data.data
              this.$set(this.form.dicData[this.btnRowIndex], 'echoHttp', res.data.data)
              this.$openUrl(`/rule-design-ui/#/ruleDesign?id=${row.echoHttp}&componentId=${row.id}&jvsAppId=${this.jvsAppId}&name=${row.label}`, '_blank')
              this.$root.eventBus.$emit('toolEvent', 'ruleSave')
            }
          })
        }
      }else if(type == 'step'){
        if (row.secret) {
          this.$openUrl(`/rule-design-ui/#/ruleDesign?id=${row.secret}&componentId=${row.id}&jvsAppId=${this.jvsAppId}&name=${row.name}`, '_blank')
        } else {
          createRule({jvsAppId: this.jvsAppId, componentId: row.id, name: row.name, designId: this.designId, componentType: 'form'}).then(res => {
            if (res.data && res.data.code == 0) {
              row.secret = res.data.data
              this.$openUrl(`/rule-design-ui/#/ruleDesign?id=${row.secret}&componentId=${row.id}&jvsAppId=${this.jvsAppId}&name=${row.name}`, '_blank')
              this.$root.eventBus.$emit('toolEvent', 'ruleSave')
            }
          })
        }
      }else{
        // 按钮设置
        if(row) {
          if (row.secret) {
            this.$openUrl(`/rule-design-ui/#/ruleDesign?id=${row.secret}&componentId=${row.name}&jvsAppId=${this.jvsAppId}&name=${row.name}`, '_blank')
          } else {
            createRule({jvsAppId: this.jvsAppId, componentId: row.name, name: row.name, designId: this.designId, componentType: 'form'}).then(res => {
              if (res.data && res.data.code == 0) {
                row.secret = res.data.data
                this.$openUrl(`/rule-design-ui/#/ruleDesign?id=${row.secret}&componentId=${row.name}&jvsAppId=${this.jvsAppId}&name=${row.name}`, '_blank')
                for(let i in this.formsetting.btnSetting) {
                  if(this.formsetting.btnSetting[i].buttonType == "submit") {
                    this.$set(this.formsetting, 'submitBtn', this.formsetting.btnSetting[i].enable)
                  }
                  if(this.formsetting.btnSetting[i].buttonType == "empty") {
                    this.$set(this.formsetting, 'emptyBtn', this.formsetting.btnSetting[i].enable)
                  }
                }
                this.$set(this.formsetting, 'btnSetting', this.formsetting.btnSetting)
                this.$root.eventBus.$emit('toolEvent', 'ruleSave')
              }
            })
          }
          this.httpData = {
            http: JSON.parse(JSON.stringify(row))
          }
        }
        this.btnRowIndex = index
      }
      // 流程设计器---查看数据结构时需调整外层弹框的关闭按钮 z-index
      let dom = $("#flowableDesignDialog .el-dialog__header .el-dialog__headerbtn", parent.document)
      if(dom) {
        dom.css({'z-index': 0})
      }
    },
    // 关联数据模型切换
    changeOriginFormModelId (val) {
      this.$set(this.form, 'dataModelId', '')
      if(this.form.formId) {
        this.$set(this.form, 'dataModelId', this.form.formId)
        let arr = []
        if(['tableForm', 'reportTable'].indexOf(this.form.type) > -1) {
          arr = this.form.tableColumn
        }
        if(this.form.type == 'formbox') {
          arr = this.form.children
        }
        for(let i in arr) {
          arr[i].formId = arr[i].searchable ? arr[i].formId : (arr[i].formId || this.form.formId)
          arr[i].dataModelId = this.form.dataModelId
          arr[i].parentType = this.form.type
        }
        if(val != null && this.form.showFrom.indexOf("url") !== -1 && this.form.datatype === "dataModel") {
          this.$set(this.form.props, 'label', '')
          this.$set(this.form.props, 'sourceFieldId', '')
          this.$set(this.form, 'url', '')
        }
      }else{
        let arr = []
        if(['tableForm', 'reportTable'].indexOf(this.form.type) > -1) {
          arr = this.form.tableColumn
        }
        if(this.form.type == 'formbox') {
          arr = this.form.children
        }
        for(let i in arr) {
          arr[i].formId = arr[i].searchable ? arr[i].formId : this.form.formId
          arr[i].dataModelId = this.form.dataModelId
          arr[i].parentType = ''
        }
        if(this.form.showFrom.indexOf("url") !== -1 && this.form.datatype === "dataModel") {
          this.$set(this.form.props, 'label', '')
          this.$set(this.form.props, 'sourceFieldId', '')
          this.$set(this.form, 'url', '')
        }
      }
    },
    // 关联表单切换
    changeOriginFormId () {
      this.connectFormPropsList = []
      this.$set(this.form.props, 'label', '')
      this.$set(this.form.props, 'value', '')
      this.$set(this.form.props, 'sourceFieldId', '')
      this.$set(this.form, 'dataModelId', '')
      this.$set(this.form, 'url', '')
      this.$set(this.form, 'others', [])
      this.$set(this.form, 'connectFormColumn', [])
      for(let i in this.connectFormOption) {
        if(this.form.formId == this.connectFormOption[i].id) {
          this.form.dataModelId = this.connectFormOption[i].dataModelId
          getConnectFormProps(this.jvsAppId, this.connectFormOption[i].dataModelId, this.connectFormOption[i].id).then(res => {
            if(res.data && res.data.code == 0) {
              this.connectFormPropsList = res.data.data
            }
          })
        }
      }
    },
    // 设置关联表单数据来源url
    setConnectFormDataUrl () {
      if(!this.form.searchable) {
        if(this.form.props.label) {
          if(this.form.type == 'cascader') {
            this.form.url = `/mgr/jvs-design/app/use/${this.jvsAppId}/dynamic/data/query/tree/${this.form.formId}`
          }else{
            this.$set(this.form.props, 'value', 'id')
            this.form.url = `/mgr/jvs-design/app/use/${this.jvsAppId}/dynamic/data/query/list/${this.form.formId}`
          }
          this.form.method = 'post'
          for(let i in this.dataModelAllFieldList) {
            if(this.dataModelAllFieldList[i].fieldKey == this.form.props.label) {
              this.form.props.text = this.dataModelAllFieldList[i].fieldName
              if(this.dataModelAllFieldList[i].sourceFieldId){
                this.form.props.sourceFieldId = this.dataModelAllFieldList[i].sourceFieldId
              }
            }
          }
        }else{
          this.$set(this.form, 'url', '')
        }
      }
    },
    // 设置关联表单扩展字段
    setConnectFormColumn () {
      let temp = []
      let labs = []
      if(this.form.props.label) {
        labs = [this.form.props.label]
      }
      for(let i in this.connectFormPropsList) {
        if(this.form.others.indexOf(this.connectFormPropsList[i].fieldKey) > -1) {
          temp.push({
            label: this.connectFormPropsList[i].fieldName,
            prop: this.connectFormPropsList[i].fieldKey,
            type: this.connectFormPropsList[i].fieldType,
            disabled: this.form.disabled
          })
          labs.push(this.connectFormPropsList[i].fieldKey)
        }
      }
      this.$set(this.form, 'connectFormColumn', temp)
      if(labs.length > 0) {
        let str = JSON.stringify(labs)
        this.form.url = `/mgr/jvs-design/app/use/${this.jvsAppId}/dynamic/data/query/list/${this.form.dataModelId}?fieldKey=${str}`
      }
    },
    // 数据表切换
    tableChangeHandle (val, list, attr) {
      for(let i in list) {
        if(list[i].tableName == val) {
          this[attr] = list[i].autoTableFields
        }
      }
    },
    // 字段名称change
    aliasColumnNameChangeHandle (val) {
      const index = this.tableOption.findIndex(item => {
        return item.fieldKey === val
      })
      if(index > -1) {
        this.form.label = this.tableOption[index].fieldName
      }
      for(let i in this.tableColumnList) {
        if(this.tableColumnList[i].fieldName == val || this.tableColumnList[i].aliasColumnName == val) {
          this.form.label = this.tableColumnList[i].columnComment
          this.form.placeholder = this.form.type == 'select' ? ('请选择' + this.form.label) : ('请输入' + this.form.label)
          this.form.columnNameProp = this.tableColumnList[i].columnName
          this.form.foreignKeyProp = this.tableColumnList[i].foreignKey || ''
          if(this.form.rules.length > 0 && ['flowTable'].indexOf(this.form.parentType) == -1) {
            this.form.rules[0].message = this.form.placeholder
          }
        }
      }
      if(["formbox", "tableForm", "reportTable"].indexOf(this.form.parentType) > -1) {
        this.dataItemModelAllFieldList.filter(item => {
          if(item.fieldKey == this.form.prop) {
            this.form.label = item.fieldName
            this.form.placeholder = this.form.type == 'select' ? ('请选择' + this.form.label) : ('请输入' + this.form.label)
            if(this.form.rules.length > 0 && ['flowTable'].indexOf(this.form.parentType) == -1) {
              this.form.rules[0].message = this.form.placeholder
            }
          }
        })
      }
      if(["formbox", "tableForm", "reportTable"].indexOf(this.form.type) > -1) {
        if(this.form.type == 'tableForm') {
          this.form.tableColumn.filter(ta => {
            this.$set(ta, 'parentKey', (this.form.parentKey ? (this.form.parentKey + '.') : '') + this.form.prop)
            this.$set(ta, 'parentType', this.form.type)
          })
        }
      }
      if(["tab", "step"].indexOf(this.form.type) > -1) {
        for(let i in this.form.dicData) {
          if(this.form.column && this.form.column[this.form.dicData[i].name]) {
            this.form.column[this.form.dicData[i].name].filter(cia => {
              this.$set(cia, 'parentKey', (this.form.parentKey ? (this.form.parentKey + '.') : '') + this.form.prop + '.' + this.form.dicData[i].name)
              this.$set(cia, 'parentType', this.form.type)
            })
          }
        }
      }
      // console.log(this.form)
      this.noKeyWord()
      this.changeLabelHandle()
    },
    infoColumnChange (val, prop) {
      let temp = []
      for(let i in this.tablesItem) {
        if(val.indexOf(this.tablesItem[i].fieldName) > -1) {
          temp.push(this.tablesItem[i])
        }
      }
      if(prop == 'infoColumn') {
        this.$set(this.form, 'infoColumnFields', temp)
      }
      if(prop == 'queryProp') {
        this.$set(this.form, 'queryColumnFields', temp)
      }
    },
    // 获取数据源列表
    getConnectSourceHandle () {
      getConnectFormList(this.jvsAppId).then(res => {
        if(res.data.code == 0) {
          this.connectFormOption = res.data.data
        }
      })
    },
    getRegExpListHandle () {
      getRegExpList().then(res => {
        if(res.data.code == 0) {
          this.regExpOption = res.data.data
        }
      })
    },
    // 步骤配置按钮
    stepBtnSettingHandle (index, row) {
      if(row.btns) {
        this.stepBtnList = row.btns
      }
      this.stepBtnIndex = index
      // 流程设计器---查看数据结构时需调整外层弹框的关闭按钮 z-index
      let dom = $("#flowableDesignDialog .el-dialog__header .el-dialog__headerbtn", parent.document)
      if(dom) {
        dom.css({'z-index': 0})
      }
      this.ownBtnVisible = true
    },
    ownBtnClose () {
      this.ownBtnVisible = false
      this.stepBtnList = []
      this.stepBtnIndex = -1
      let dom = $("#flowableDesignDialog .el-dialog__header .el-dialog__headerbtn", parent.document)
      if(dom) {
        dom.css({'z-index': 9})
      }
    },
    addStepBtn () {
      this.stepBtnList.push({name: '按钮' + (this.stepBtnList.length + 1), id: guid()})
      this.$forceUpdate()
    },
    designBtnForm (type) {
      let attr = ''
      let name = this.form.label + '_'
      if(type == 'add') {
        attr = 'addBtnFormCode'
        name += (this.form.addBtnText || '新增')
      }else if(type == 'edit'){
        attr = 'editBtnFormCode'
        name += (this.form.editBtnText || '编辑')
      }else{
        attr = 'viewBtnFormCode'
        name += (this.form.viewBtnText || '详情')
      }
      let dataModelId = this.dataModelId
      if(this.form.dataFilterEnable && this.form.formId) {
        dataModelId = this.form.formId
      }
      if(!this.form[attr]) {
        // 新增表单code
        getButtonFormId(this.jvsAppId, dataModelId, this.designId, name).then(res => {
          if(res.data.code == 0) {
            this.$set(this.form, attr, res.data.data)
            this.$emit('autoSave')
            let str = ''
            str = location.origin + (`/page-design-ui/#/form?createBy=tableFormButton&jvsAppId=${this.jvsAppId}&id=`+res.data.data + (dataModelId ? `&dataModelId=${dataModelId}` : '') + `&isDetail=${type === 'view'}&isAddForm=${type == 'add'}`)
            this.$openUrl(str, '_blank')
          }
        })
      }else{
        // 直接打开设计
        this.$openUrl(`/page-design-ui/#/form?createBy=tableFormButton&jvsAppId=${this.jvsAppId}&id=${this.form[attr]}&dataModelId=${dataModelId}` + `&isDetail=${type === 'view'}&isAddForm=${type == 'add'}`)
      }
    },
    deleteFormCode (type) {
      let attr = ''
      if(type == 'add') {
        attr = 'addBtnFormCode'
      }else if(type == 'edit') {
        attr = 'editBtnFormCode'
      }else{
        attr = 'viewBtnFormCode'
      }
      this.$set(this.form, attr, '')
    },
    addItemOfArr (prop) {
      if(!this.form[prop]) {
        this.$set(this.form, prop, [])
      }
      this.form[prop].push({})
    },
    deleteItemOfArr (index, row, prop) {
      this.form[prop].splice(index, 1)
    },
    // 字段change显示对应label
    changeSource (row, index, list, prop) {
      for(let i in list) {
        if(list[i].fieldName == row.value) {
          this.$set(this.form[prop][index], 'label', list[i].columnComment)
        }
      }
      let temp = []
      for(let i in this.form[prop]) {
        temp.push(this.form[prop][i].value)
      }
      this.infoColumnChange(temp, prop)
    },
    // 表格操作栏固定
    menuFixChange (val) {
      if(val) {
        this.$set(this.form, 'menuFix', val)
      }else{
        this.$set(this.form, 'menuFix', false)
      }
    },
    // 步骤按钮 重排顺序
    sortHandle (type, index) {
      index = Number.parseInt(index)
      let own = JSON.parse(JSON.stringify(this.stepBtnList[index]))
      let change = null
      if(type == 'up') {
        change = JSON.parse(JSON.stringify(this.stepBtnList[index-1]))
        this.$set(this.stepBtnList, (index-1), own)
        this.$set(this.stepBtnList, index, change)
      }
      if(type == 'down') {
        change = JSON.parse(JSON.stringify(this.stepBtnList[index+1]))
        this.$set(this.stepBtnList, (index+1), own)
        this.$set(this.stepBtnList, index, change)
      }
    },
    showExpressClose () {
      this.showExpressVisible = false
      this.showExpressForm = {
        relation: []
      }
    },
    // 设置显示控制
    setShowHandle (oprate) {
      this.showDisabledType = oprate
      if(oprate == 'disabled') {
        if(this.form.disabledExpress) {
          this.$set(this.showExpressForm, 'relation', this.form.disabledExpress)
          this.$set(this.showExpressForm, 'empty', this.form.disabledEmpty)
        }
        this.domTreeData = []
        if(this.form.parentType == 'tableForm') {
          this.getDomTree(this.domList, this.domTreeData, null, this.form.parentKey)
          this.showExpressOption.column.filter(item => {
            if(item.prop == 'empty') {
              item.display = true
            }
          })
        }else{
          this.getDomTree(this.domList, this.domTreeData)
          this.showExpressOption.column.filter(item => {
            if(item.prop == 'empty') {
              item.display = false
            }
          })
        }
      }else if(oprate == 'require') {
        if(this.form.requireExpress) {
          this.$set(this.showExpressForm, 'relation', this.form.requireExpress)
        }
        this.domTreeData = []
        this.showExpressOption.column.filter(item => {
          if(item.prop == 'empty') {
            item.display = false
          }
        })
        if(this.form.parentType == 'tableForm') {
          this.getDomTree(this.domList, this.domTreeData, null, this.form.parentKey)
        }else{
          this.getDomTree(this.domList, this.domTreeData)
        }
      }else{
        if(this.form.displayExpress) {
          this.$set(this.showExpressForm, 'relation', this.form.displayExpress)
        }
        this.domTreeData = []
        this.getDomTree(this.domList, this.domTreeData)
        this.showExpressOption.column.filter(item => {
          if(item.prop == 'empty') {
            item.display = false
          }
        })
      }
      this.$set(this.showExpressForm, 'showOperator', this.form.showOperator)
      this.showExpressVisible = true
    },
    // 获取设计dom树
    getDomTree (list, result, prop, comParentKey) {
      for(let i in list) {
        if(["p", "divider", "box", "tableForm", "reportTable", "button", "link", "iframe"].indexOf(list[i].type) == -1) {
          let temp = {
            label: list[i].label,
            prop: list[i].prop
          }
          if(prop) {
            temp.parent = prop
          }
          if(["formbox"].indexOf(list[i].type) == -1 && list[i].children && list[i].children.length > 0) {
            temp.children = []
            let pa = []
            if(prop) {
              pa  = prop
            }
            pa.push(list[i].prop)
            this.getDomTree(list[i].children, temp.children, pa)
          }
          if(["tab", "step"].indexOf(list[i].type) > -1) {
            temp.children = []
            for(let t in list[i].dicData) {
              if(list[i].dicData[t].name && list[i].column && list[i].column[list[i].dicData[t].name] && list[i].column[list[i].dicData[t].name].length > 0) {
                let tp = {
                  label: list[i].dicData[t].label,
                  prop: list[i].dicData[t].name,
                  children: []
                }
                let dp = []
                if(prop) {
                  dp = prop
                }
                tp.parent = [...dp, list[i].prop]
                if(comParentKey) {
                  this.getDomTree(list[i].column[list[i].dicData[t].name] , result, prop, comParentKey)
                }else{
                  this.getDomTree(list[i].column[list[i].dicData[t].name] , tp.children, [...tp.parent, list[i].dicData[t].name])
                  temp.children.push(tp)
                }
              }
            }
          }
          if(!comParentKey) {
            result.push(temp)
          }
        }else{
          if(['tableForm'].indexOf(list[i].type) > -1 && comParentKey) {
            let pros = comParentKey.split('.')
            if(list[i].prop == pros[pros.length-1]) {
              list[i].tableColumn.filter(ti => {
                result.push({
                  label: ti.label,
                  prop: ti.prop
                })
              })
            }
          }
        }
      }
    },
    domNodeClick (data) {
      let temp = {
        label: data.label,
        prop: data.prop,
        value: ''
      }
      if(data.parent) {
        temp.parent = data.parent
      }
      this.showExpressForm.relation.push(temp)
    },
    // 保存
    saveShowExpress () {
      let temp = []
      if(this.showExpressForm.relation && this.showExpressForm.relation.length > 0) {
        temp = this.showExpressForm.relation
      }
      if(this.showDisabledType == 'disabled') {
        this.$set(this.form, 'disabledExpress', temp)
        this.$set(this.form, 'disabledEmpty', this.showExpressForm.empty || false)
      }else if(this.showDisabledType == 'require'){
        this.$set(this.form, 'requireExpress', temp)
      }else{
        this.$set(this.form, 'displayExpress', temp)
      }
      this.$set(this.form, 'showOperator', this.showExpressForm.showOperator || '||')
      console.log(this.form)
      this.showExpressClose()
    },
    // 按钮统一配置
    buttonSetting () {
      this.buttonOpenStatus = {}
      let addPrint = true
      let addCancel = true
      for(let i in this.formsetting.btnSetting) {
        if(!this.formsetting.btnSetting[i].enable && ['submit', 'empty'].indexOf(this.formsetting.btnSetting[i].buttonType) == -1) {
          this.$set(this.formsetting.btnSetting[i], 'enable', false)
        }
        if(this.formsetting.btnSetting[i].buttonType == "submit") {
          this.$set(this.formsetting.btnSetting[i], 'enable' ,this.formsetting.submitBtn)
        }
        if(this.formsetting.btnSetting[i].buttonType == "empty") {
          this.$set(this.formsetting.btnSetting[i], 'enable', this.formsetting.emptyBtn)
        }
        if(this.formsetting.btnSetting[i].buttonType == "print") {
          addPrint = false
        }
        if(this.formsetting.btnSetting[i].buttonType == "cancel") {
          addCancel = false
        }
        if(!this.formsetting.btnSetting[i].permissionFlag) {
          this.formsetting.btnSetting[i].permissionFlag = this.designId + '-' + this.formsetting.btnSetting[i].buttonType + '-' + guid()
        }
        this.buttonOpenStatus[this.formsetting.btnSetting[i].permissionFlag] = false
      }
      if(addPrint && !this.isAddForm) {
        let list0 = this.formsetting.btnSetting.slice(0, 2)
        let list1 = this.formsetting.btnSetting.slice(2, this.formsetting.btnSetting.length)
        let listTemp = [
          ...list0,
          {
            name: '打印',
            buttonType: 'print',
            flag: true,
            enable: true
          },
          ...list1
        ]
        this.$set(this.formsetting, 'btnSetting', listTemp)
      }
      if(addCancel){
        this.formsetting.btnSetting.push({
          name: '取消',
          buttonType: 'cancel',
          flag: true,
          enable: true
        })
      }
      if(this.isAddForm) {
        this.formsetting.btnSetting = this.formsetting.btnSetting.filter(bi => {
          if(bi.buttonType != 'print') {
            return bi
          }
        })
      }
    },
    // 占位图
    handleAvatarSuccess (res, file) {
      if(res.code === 0){
        let obj = {
          name: res.data.originalFileName,
          url: res.data.fileLink,
          fileName: res.data.fileName,
          bucketName: res.data.bucketName
        }
        this.$set(this, 'imageUrl', res.data.fileLink)
        this.$set(this.form, 'defaultValue', [obj])
      }
    },
    beforeAvatarUpload (file) {
      const isJPG = (['image/jpeg', 'image/png', 'image/jpg'].indexOf(file.type) > -1);
      const isLt20M = file.size / 1024 / 1024 < 20;
      if (!isJPG) {
        this.$notify({
          title: '提示',
          message: '上传头像图片只能是 JPG 格式!',
          position: 'bottom-right',
          type: 'error'
        });
      }
      if (!isLt20M) {
        this.$notify({
          title: '提示',
          message: '上传头像图片大小不能超过 20MB!',
          position: 'bottom-right',
          type: 'error'
        });
      }
      return isJPG && isLt20M;
    },
    // label修改placeholder
    changeLabelHandle () {
      let str = '请输入'
      if(['select', 'switch', 'radio', 'checkbox', 'timePicker', 'datePicker', 'colorSelect', 'iconSelect', 'chinaArea', 'cascader',
        'user', 'role', 'department', 'group', 'job', 'connectForm'].indexOf(this.form.type) > -1) {
        str = '请选择'
      }
      if(['imageUpload', 'fileUpload'].indexOf(this.form.type) > -1) {
        str = '请上传'
      }
      this.$set(this.form, 'placeholder', str + this.form.label)
      if(this.form.rules.length > 0 && ['flowTable'].indexOf(this.form.parentType) == -1) {
        this.form.rules[0].message = this.form.placeholder
      }
    },
    // 组件函数设置
    setFuction () {
      let businessId = this.form.parentKey ? (`${this.form.parentKey }.${this.form.prop}`) : this.form.prop
      if(this.form.parentKey) {
        this.findParentDom(this.domList, this.form.parentKey, 'each')
      }
      if(this.parentDom) {
        if(this.parentDom.type == 'tab' && this.parentDom.detachData) {
          let kps = this.form.parentKey.split('.')
          let dicProp = ''
          this.parentDom.dicData.filter(dic => {
            if(dic.name == kps[kps.length-1] && dic.prop) {
              dicProp = dic.prop
            }
          })
          let temp = []
          for(let kix in kps) {
            if(kps[kix] == this.parentDom.prop) {
              break
            }
            temp.push(kps[kix])
          }
          if(dicProp) {
            temp.push(dicProp)
          }
          temp.push(this.form.prop)
          businessId = temp.join('.')
        }
        if(this.parentDom.parentDetachDataProp) {
          if(this.parentDom.parentDetachDataProp == 'thisIsEmptyStringValue') {
            if(this.parentDom.id == this.form.id) {
              businessId = `${this.form.prop}`
            }else{
              businessId = `${this.parentDom.prop}.${this.form.prop}`
            }
          }else{
            if(this.parentDom.detachData) {
              businessId = `${this.parentDom.parentDetachDataProp}.${this.form.prop}`
            }else{
              businessId = `${this.parentDom.parentDetachDataProp}.${this.parentDom.prop}.${this.form.prop}`
            }
          }
        }
      }
      this.$setFormula({
        title: '公式设置',
        hasModal: true,
        label: this.form.label,
        execId: this.form.formula ? this.form.formula : '',
        apiPrefix: 'jvs-design', // 'rule'
        useCase: 'formItemValue', // 'RULE'
        props: {
          jvsAppId: this.jvsAppId,
          designId: this.designId,
          businessId: businessId,
          type: this.form.type,
          parentType: this.form.parentType ? this.form.parentType : ''
        },
        afterSave: (dialog, data) => {
          if(data && data.id) {
            this.$set(this.form, 'formula', data.id)
          }
          dialog.handleClose()
        }
      })
    },
    // 按钮配置公式
    openButtonFormula (item) {
      this.$setFormula({
        title: '公式设置',
        hasModal: true,
        label: item.name,
        execId: item.formula ? item.formula : '',
        apiPrefix: 'jvs-design',
        useCase: 'formButtonDisplay',
        props: {
          jvsAppId: this.jvsAppId,
          designId: this.designId,
          businessId: item.permissionFlag
        },
        afterSave: (dialog, data) => {
          if(data && data.id) {
            this.$set(item, 'formula', data.id)
          }
          dialog.handleClose()
        }
      })
    },
    // 状态设置
    statusChange (val) {
      if(val == 'disabled') {
        this.$set(this.form, 'disabled', true)
        this.$set(this.form, 'display', true)
      }else if(val == 'hide') {
        this.$set(this.form, 'display', false)
      }else{
        this.$set(this.form, 'disabled', false)
        this.$set(this.form, 'display', true)
      }
    },
    // 默认值来源设置
    defaultOriginChange (val) {
      if(val == 'formula') {
        this.$set(this.form, 'defaultValue', '')
      }else{
        if(this.form.formula) {
          deleteExec(this.designId, this.form.formula).then(res => {
            if(res.data && res.data.code == 0) {
              this.$set(this.form, 'formula', '')
            }
          })
        }
      }
    },
    // 数据类型切换
    dataTypeChange (val) {
      if(val == 'dataModel') {
        if(!this.form.props) {
          this.$set(this.form, 'props', {
            label: '',
            value: '',
            secTitle: ''
          })
        }
        if(!this.form.props.secTitle) {
          this.$set(this.form.props, 'secTitle', '')
        }
        if(!this.connectFormOption || this.connectFormOption.length == 0) {
          this.getConnectSourceHandle()
        }
        this.$set(this.form.props, 'value', '')
      }else{
        this.form.formId = ''
        this.form.props = {
          label: '',
          value: 'value'
        }
      }
      if(val == 'option') {
        this.form.url = ''
        this.form.props = {
          label: '',
          value: ''
        }
      }
      if(val == 'flowable') {
        this.$set(this.form, 'multiple', false)
        this.form.props = {
          label: 'name',
          value: 'id'
        }
      }
      if(val == 'dom') {
        this.form.props = {
          label: 'label',
          value: 'prop'
        }
      }
    },
    // 数据筛选
    async dataFilterHandle () {
      if(this.form.dataFilterGroupList) {
        this.dataFilterGroupList = JSON.parse(JSON.stringify(this.form.dataFilterGroupList))
      }else{
        if(this.form.dataFilterList && this.form.dataFilterList.length > 0) {
          this.dataFilterGroupList = [ JSON.parse(JSON.stringify(this.form.dataFilterList)) ]
        }
      }
      if(true || this.form.searchable || this.form.type == 'tableForm') {
        await this.getDataModelDataFilterHandle(this.form.formId)
      }
      this.dataFilterVisible = true
    },
    async openFormFilterHandle () {
      if(this.form.addBtnFilterGroupList) {
        this.dataFilterGroupList = JSON.parse(JSON.stringify(this.form.addBtnFilterGroupList))
      }
      await this.getDataModelDataFilterHandle(this.form.addBtnFormId)
      this.dataFilterType = 'openModelTable'
      this.dataFilterVisible = true
    },
    addDataFilterGroup () {
      this.dataFilterGroupList.push([{}])
    },
    addDataFilter (index) {
      this.dataFilterGroupList[index].push({})
    },
    deleteDataFilter (index, arr) {
      arr = arr.splice(index, 1)
    },
    dataFilterSubmit () {
      if(this.dataFilterType == 'openModelTable') {
        this.$set(this.form, 'addBtnFilterGroupList', this.dataFilterGroupList)
      }else{
        this.$set(this.form, 'dataFilterGroupList', this.dataFilterGroupList)
        this.$set(this.form, 'dataFilterList', [])
      }
      this.dataFilterClose()
    },
    dataFilterClose () {
      this.dataFilterList = []
      this.dataFilterGroupList = []
      this.dataFilterVisible = false
      this.dataFilterType = ''
    },
    // 数据联动
    async getDataModelList () {
      await getAllModel(this.jvsAppId).then(res => {
        if(res.data && res.data.code == 0) {
          this.dataModelList = res.data.data
        }
      })
    },
    async setLinkData () {
      this.dataLinkageModelId = ''
      if(!this.dataModelList || this.dataModelList.length == 0) {
        await this.getDataModelList()
      }
      if(this.form.dataLinkageList) {
        this.dataLinkageList = JSON.parse(JSON.stringify(this.form.dataLinkageList))
      }
      if(this.form.dataLinkageModelId) {
        this.dataLinkageModelId = this.form.dataLinkageModelId
        this.changeLinkModelPropList()
      }
      this.dataLinkageVisible = true
    },
    addDataLinkage () {
      this.dataLinkageList.push({enabledQueryTypes: 'eq'})
    },
    deleteDataLinkage (index, arr) {
      arr = arr.splice(index, 1)
    },
    dataLinkageSubmit () {
      this.$set(this.form, 'dataLinkageList', this.dataLinkageList)
      this.$set(this.form, 'dataLinkageModelId', this.dataLinkageModelId)
      this.dataLinkageClose()
    },
    dataLinkageClose () {
      this.dataLinkageModelId = ''
      this.dataLinkageList = []
      this.dataLinkageVisible = false
    },
    changeModelPropList (val, optype) {
      if(val) {
        getLinkModelAllFields(this.jvsAppId, val, this.designId).then(res => {
          if(res.data && res.data.code == 0) {
            if(optype == 'openFormDataMModelField') {
              this.$set(this, 'openFormDataMModelField', res.data.data)
            }else{
              this.$set(this, 'dataModelAllFieldList', res.data.data)
            }
          }
        })
      }else{
        if(optype == 'openFormDataMModelField') {
          this.$set(this, 'openFormDataMModelField', [])
        }else{
          this.dataModelAllFieldList = []
        }
      }
      if(optype != 'init' && optype != 'openFormDataMModelField') {
        if(val && !this.form.showFrom.indexOf("url") !== -1 && this.form.datatype === "dataModel") {
          this.$set(this.form.props, 'label', '')
          this.$set(this.form.props, 'sourceFieldId', '')
        }
      }
    },
    changeLinkModelPropList () {
      if(this.dataLinkageModelId) {
        getLinkModelAllFields(this.jvsAppId, this.dataLinkageModelId, this.designId).then(res => {
          if(res.data && res.data.code == 0) {
            this.$set(this, 'dataLinkModelAllFieldList', res.data.data)
          }
        })
      }else{
        this.dataLinkModelAllFieldList = []
      }
    },
    changeItemModelPropList (val) {
      if(val || val == '') {
        this.$set(this.form, 'dataFilterList', [])
        this.$set(this.form, 'dataModelId', '')
      }
      if(this.form.formId) {
        getModelAllFields(this.jvsAppId, this.form.formId, this.designId).then(res => {
          if(res.data && res.data.code == 0) {
            this.$set(this, 'dataItemModelAllFieldList', res.data.data)
            this.changeOriginFormModelId(val)
          }
        })
      }else{
        this.dataItemModelAllFieldList = []
      }
    },
    getTmExList () {
      getEncryptionList().then(res => {
        if(res.data && res.data.code == 0) {
          this.tmExList = res.data.data
        }
      })
    },
    getFilterDomTree (domList, list) {
      for(let i in domList) {
        let obj = JSON.parse(JSON.stringify(domList[i]))
        if(["formbox", "tableForm", "reportTable"].indexOf(obj.type) > -1) {
          switch(obj.type) {
            case 'tableForm':
              if(obj.tableColumn && obj.tableColumn.length > 0) {
                obj.children = [];
                this.getFilterDomTree(obj.tableColumn, obj.children)
              }
              break;
            default: ;break;
          }
        }else if(["tab", "step"].indexOf(obj.type) > -1) {
          if(obj.column && obj.dicData && obj.dicData.length > 0) {
            obj.children = [];
            for(let di in obj.dicData) {
              let td = {
                label: obj.dicData[di].label,
                prop: obj.dicData[di].name
              }
              if(obj.column[td.prop] && obj.column[td.prop].length > 0) {
                if(obj.column[td.prop][0].prop == this.form.prop && obj.column[td.prop].length > 1) {
                  td.children = []
                  let tl = JSON.parse(JSON.stringify(obj.column[td.prop]))
                  tl.splice(0, 1)
                  this.getFilterDomTree(tl, td.children)
                }else{
                  td.children = []
                  this.getFilterDomTree(obj.column[td.prop], td.children)
                }
              }
              td.disabled = false
              obj.children.push(td)
            }
          }
        }else{
          if(obj.children) {
            delete obj.children
          }
        }
        obj.disabled = false
        list.push(obj)
      }
    },
    findParentDom (list, key, eachType, parent) {
      if(list && list.length > 0) {
        list.filter(item => {
          if(key && key.includes(item.prop)) {
            if(eachType == 'each' && parent) {
              if(parent.detachData) {
                let parentDetachDataProp = ''
                let pk = item.parentKey.split('.')
                parent.dicData.filter(dit => {
                  if(dit.name == pk[pk.length-1] && dit.prop) {
                    parentDetachDataProp = dit.prop
                  }
                })
                let tp = []
                for(let p in pk) {
                  if(pk[p] == parent.prop) {
                    break
                  }
                  tp.push(pk[p])
                }
                if(parentDetachDataProp) {
                  tp.push(parentDetachDataProp)
                }
                item.parentDetachDataProp = tp.join('.') || 'thisIsEmptyStringValue'
              }
            }
            this.parentDom = item
          }
          if(['tab', 'step'].indexOf(item.type) > -1) {
            if(item.detachData) {
              let pks = key.split('.')
              let parentDetachDataProp = ''
              item.dicData.filter(dit => {
                if(dit.name == pks[pks.length-1] && dit.prop) {
                  parentDetachDataProp = dit.prop
                  let tp = []
                  if(item.parentKey) {
                    let pk = item.parentKey.split('.')
                    for(let p in pk) {
                      if(pk[p] == parent.prop) {
                        break
                      }
                      tp.push(pk[p])
                    }
                  }
                  if(parentDetachDataProp) {
                    tp.push(parentDetachDataProp)
                  }
                  item.parentDetachDataProp = tp.join('.') || 'thisIsEmptyStringValue'
                  this.parentDom = item
                }
              })
            }
            for(let j in item.column) {
              if(item.column[j] && item.column[j].length > 0) {
                this.findParentDom(item.column[j], key, eachType, item)
              }
            }
          }
        })
      }
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
    copyProp () {
      const text = document.createElement('input')
      text.value = this.form.prop
      document.body.appendChild(text)
      text.select()
      document.execCommand('Copy')
      document.body.removeChild(text)
      this.$notify({
        title: '提示',
        message: '复制成功',
        position: 'bottom-right',
        type: 'success'
      })
    },
    searchableChange () {
      if(!this.form.props) {
        this.$set(this.form, 'props', {
          label: ''
        })
      }
      if(!this.form.searchable) {
        if(!this.form.status) {
          this.form.disabled = false
        }
      }
      if(this.form.searchable) {
        if(!this.form.editable) {
          this.$set(this.form, 'editable', false)
        }
        if(!this.form.searchPopupWidth) {
          this.$set(this.form, 'searchPopupWidth', 50)
        }
      }
    },
    // 获取数据模型字段对应权限列表
    async getDataModelDataFilterHandle (dataModelId) {
      await getDataModelDataFilter(this.jvsAppId, dataModelId).then(res => {
        if(res.data && res.data.code == 0) {
          this.custFilterList = res.data.data
          // console.log(res.data.data)
        }
      })
    },
    // 获取单个字段的权限列表
    getAttrVal (attr, val) {
      let temp = []
      for(let i in this.custFilterList) {
        if(this.custFilterList[i].fieldDto['fieldKey'] == val) {
          if(this.custFilterList[i][attr]) {
            temp = this.custFilterList[i][attr]
          }
        }
      }
      return temp
    },
    enabledQueryTypeHandle (item, index, gix) {
      this.$set(item, 'value', (this.form.searchable || this.form.type == 'tableForm' || ['role', 'department', 'job', 'user'].indexOf(item.type) > -1) ? [] : '')
      // this.$set(this.dataFilterList[index], 'value', (this.form.searchable || this.form.type == 'tableForm') ? [] : '')
      if(['role', 'department', 'job', 'user'].indexOf(item.type) > -1 && this.$refs[`userForm_${gix}_${index}`]) {
        this.$refs[`userForm_${gix}_${index}`][0].clearUser()
      }
      this.$forceUpdate()
    },
    setExpressDisplay () {
      if(this.form.expressDisplay) {
        this.expressDisplayForm = JSON.parse(JSON.stringify(this.form.expressDisplay))
      }else{
        this.expressDisplayForm = {
          conditionControl: []
        }
      }
      this.expressDisplayVisible = true
    },
    expressDisplaySubmit () {
      this.$set(this.form, 'expressDisplay', JSON.parse(JSON.stringify(this.expressDisplayForm)))
      this.expressDisplayClose()
      this.$forceUpdate()
    },
    expressDisplayClose () {
      this.expressDisplayVisible = false
      this.expressDisplayForm = {}
    },
    optionLabelChange (row) {
      if(['tab', 'step'].indexOf(this.form.type) == -1) {
        this.$set(row, 'value', row.label)
      }
      this.$forceUpdate()
    },
    uploadLimitChange () {
      if(this.form.limit < 2) {
        this.$set(this.form, 'multipleUpload', false)
      }
    },
    getAllRuleListHandle () {
      getAllRule(this.jvsAppId, {dataModelId: this.dataModelId}).then(res => {
        if(res.data && res.data.code == 0) {
          this.allRuleList = res.data.data
        }
      })
    },
    newRuleSet (prop) {
      this.newRuleSetLoading = true
      createRule({jvsAppId: this.jvsAppId, componentId: this.form.id, name: this.form.label, designId: this.designId, componentType: 'form'}).then(res => {
        if(res.data && res.data.code == 0) {
          this.newRuleSetLoading = false
          if(prop && (typeof prop == 'string')) {
            this.$set(this.form, prop, res.data.data)
          }else{
            this.$set(this.form, this.httpType, res.data.data)
          }
          this.$root.eventBus.$emit('toolEvent', 'ruleSave')
          this.getAllRuleListHandle()
          this.$openUrl(`/rule-design-ui/#/ruleDesign?id=${res.data.data}&componentId=${this.form.id}&jvsAppId=${this.jvsAppId}&name=${this.form.label}`, '_blank')
          this.netSetClose()
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
    viewRule (attr) {
      if(this.form[attr ? attr : this.httpType]) {
        this.$openUrl(`/rule-design-ui/#/ruleDesign?id=${this.form[attr ? attr : this.httpType]}&componentId=${this.form.id}&jvsAppId=${this.jvsAppId}&name=${this.form.label}`, '_blank')
      }
    },
    netSetClose () {
      this.netSetVisible = false
    },
    requiredChange () {
      if(this.form.rules[0].required) {
        this.$set(this.form, 'emptyEnable', false)
      }
    },
    multipleChange () {
      if(!this.form.multiple && this.form.datatype == 'dom') {
        this.$set(this.form, 'datatype', 'option')
      }
    },
    domBindChange (val) {
      if(this.form.formId) {
        if(val) {
          this.$set(this.form, 'others', [])
        }
        getLinkModelAllFields(this.jvsAppId, this.form.formId, this.designId).then(res => {
          if(res.data && res.data.code == 0) {
            this.$set(this, 'dataModelAllFieldList', res.data.data)
            if(this.form.others && this.form.others.length > 0) {
              let tp = []
              for(let r in res.data.data) {
                if(this.form.others.indexOf(res.data.data[r].prop || res.data.data[r].fieldKey) > -1) {
                  tp.push(res.data.data[r].prop || res.data.data[r].fieldKey)
                }
              }
              this.$set(this.form, 'others', tp)
            }
          }
        })
      }else{
        this.$set(this.form, 'others', [])
      }
      this.$forceUpdate()
    },
    // 选项卡、步骤条 选项名称change
    tabNameChangeHandle (name) {
      console.log(this.form)
    },
    delFormEcho () {
      this.$set(this.formsetting, 'dataEchoRequest', '')
      // this.$confirm('确定删除回显设置?', '提示', {
      //   confirmButtonText: '确定',
      //   cancelButtonText: '取消',
      //   type: 'warning'
      // }).then(() => {
      //   delRuleBySecret(this.jvsAppId, this.formsetting.dataEchoRequest).then(res => {
      //     if(res.data && res.data.code == 0) {
      //       this.$set(this.formsetting, 'dataEchoRequest', '')
      //       this.$emit('autoSave')
      //     }
      //   })
      // })
    },
    // 获取当前应用下所有列表页
    getAllPageList () {
      getAllPageByApplication(this.jvsAppId).then(res => {
        if(res.data && res.data.code == 0) {
          this.pageList = res.data.data ? [...res.data.data] : []
        }
      })
    },
    pageTableIdChange () {
      this.pageList.filter(pit => {
        if(pit.id == this.form.formId) {
          this.$set(this.form, 'dataModelId', pit.dataModelId)
          this.$set(this.form, 'jvsAppId', pit.jvsAppId)
        }
      })
    },
    pageSetting () {
      if(!this.form.pageQuery) {
        this.$set(this.form, 'pageQuery', [])
      }
      if(!this.form.pageSearch) {
        this.$set(this.form, 'pageSearch', [])
      }
      this.domTreeData = []
      this.getDomTree(this.domList, this.domTreeData)
      this.getPageFieldList(this.form.jvsAppId, this.form.formId)
    },
    pageSetClose () {
      this.pageVisible = false
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
          this.pageVisible = true
        }
      })
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
    addQuery () {
      let temp = JSON.parse(JSON.stringify(this.form.pageQuery))
      temp.push({enabledQueryTypes: ''})
      this.$set(this.form, 'pageQuery', temp)
      this.$forceUpdate()
    },
    addSearch () {
      let temp = JSON.parse(JSON.stringify(this.form.pageSearch))
      temp.push({enabledQueryTypes: 'eq', disabled: false, dataFileterList: []})
      this.$set(this.form, 'pageSearch', temp)
      this.$forceUpdate()
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
    userRangeHandle () {
      this.userRangeOption = {
        sortable: false,
        type: ''
      }
      if(this.form.rangeOption) {
        this.userRangeOption = JSON.parse(JSON.stringify(this.form.rangeOption))
      }
      this.userRangeVisible = true
    },
    userRangeClose () {
      this.userRangeVisible = false
      this.userRangeOption = null
    },
    getDialogTitle (type) {
      if (type === 'user') {
        return '用户选择'
      }
      if (type === 'role') {
        return '角色选择'
      }
      if (type === 'dept') {
        return '部门选择'
      }
      if (type === 'group') {
        return '群组选择'
      }
      if (type === 'job') {
        return '岗位选择'
      }
    },
    handleDelUser (id, arr) {
      const index = arr.findIndex(item => {
        return item.id === id
      })
      arr.splice(index, 1)
      this.$forceUpdate()
    },
    userRangeTypeChange () {
      this.$set(this.userRangeOption, 'scopes', [])
    },
    addUSerRange () {
      this.$refs.userSelector.openDialog((this.userRangeOption.scopes && this.userRangeOption.scopes.length > 0) ? this.userRangeOption.scopes : [])
    },
    userSubmit (data) {
      this.$set(this.userRangeOption, 'scopes', data)
    },
    userRangeSubmit () {
      this.$set(this.form, 'rangeOption', this.userRangeOption)
      this.userRangeClose()
    },
    // 动态数据项设置
    async dataItemExpressHandle () {
      if(this.form.formId) {
        await this.getDataModelDataFilterHandle(this.form.formId)
      }
      if(this.openByForm) {
        console.log(this.openByForm)
      }else{
        if(this.form.dataItmeBindProp) {
          this.dataItmeBindProp = this.form.dataItmeBindProp
        }
        if(this.form.dataItemExpressGroupList) {
          this.dataFilterGroupList = JSON.parse(JSON.stringify(this.form.dataItemExpressGroupList))
        }
        this.dataItemExpressVisible = true
      }
    },
    dataItemExpressSubmit () {
      this.$set(this.form, 'dataItmeBindProp', this.dataItmeBindProp)
      if(this.form.datatype == 'dataModel') {
        this.$set(this.form, 'dataItemExpressGroupList', this.dataFilterGroupList)
      }
      this.dataItemExpressClose()
    },
    dataItemExpressClose () {
      this.dataItmeBindProp = ''
      this.dataFilterGroupList = []
      this.dataItemExpressVisible = false
    },
    buttonEnableHandle (button) {
      if(button.buttonType == 'submit') {
        this.$set(this.formsetting, 'submitBtn', button.enable)
      }
      if(button.buttonType == 'empty') {
        this.$set(this.formsetting, 'emptyBtn', button.enable)
      }
      this.$forceUpdate()
    },
    moveBtnStart (button) {
      this.moveSource = JSON.parse(JSON.stringify(button))
    },
    moveBtning (button) {
      if(['empty', 'submit', 'print', 'cancel'].indexOf(button.buttonType) == -1) {
        this.moveTarget = JSON.parse(JSON.stringify(button))
      }
    },
    moveBtnEnd () {
      if(this.moveTarget && this.moveTarget.permissionFlag != this.moveSource.permissionFlag) {
        let from = -1
        this.formsetting.btnSetting.filter((bit, bix) => {
          if(bit.permissionFlag == this.moveSource.permissionFlag) {
            from = bix
          }
        })
        if(from > -1) {
          this.formsetting.btnSetting.splice(from, 1)
          this.$forceUpdate()
        }
        let to = -1
        this.formsetting.btnSetting.filter((bit, bix) => {
          if(bit.permissionFlag == this.moveTarget.permissionFlag) {
            to = bix
          }
        })
        if(to > -1) {
          this.formsetting.btnSetting.splice(to, 0, this.moveSource)
          this.$forceUpdate()
        }
      }
      this.moveSource = null
      this.moveTarget = null
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
          this.form.dicData.push({label: str, value: name})
        }
      }
      this.multipleAddClose()
    },
    eventHttpHandle () {
      if(!this.newRuleSetLoading) {
        this.newRuleSet('eventHttp')
      }
    },
    getFilterEnabledQueryTypes (value, arr) {
      let list = []
      arr.filter(item => {
        if(item.fieldKey == value) {
          if(item.enabledQueryTypes && item.enabledQueryTypes.length > 0) {
            for(let i in item.enabledQueryTypes) {
              let str = ''
              switch(item.enabledQueryTypes[i]) {
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
                case 'notIn':
                  str = '不包含';
                  break;
                case 'isNull':
                  str = '等于空';
                  break;
                default: ;break;
              }
              if(str) {
                list.push({label: str, value: item.enabledQueryTypes[i]})
              }
            }
          }
        }
      })
      return list
    },
    isDefaultChange () {
      if(this.form.form.isDefault) {
        if(this.form.formula) {
          deleteExec(this.designId, this.form.formula).then(res => {
            if(res.data && res.data.code == 0) {
              this.$set(this.form, 'formula', '')
            }
          })
        }
      }
    },
    otherQueryFilter (list) {
      let temp = []
      list.filter(li => {
        if(['imageUpload', 'image', 'fileUpload', 'file'].indexOf(li.type) == -1) {
          temp.push(li)
        }
      })
      return temp
    }
  },
  watch: {
    form: function (newVal, oldVal) {
      console.log(newVal)
      let validateTypes = ["cascader", "datasource", "imageUpload", "fileUpload", "colorSelect", "iconSelect", "htmlEditor", "department", "role", "user", "post", "chinaArea"]
      if(validateTypes.indexOf(newVal.type) > -1) {
        if(!newVal.rules || newVal.rules.length == 0) {
          let msgstr = '请选择'
          if(["imageUpload", "fileUpload"].indexOf(newVal.type) > -1){
            msgstr = '请上传文件'
            if(newVal.type == 'imageUpload') {
              msgstr = '请上传图片'
            }
          }
          if(newVal.type == 'htmlEditor') {
            msgstr = '请输入' + newVal.label
          }
          newVal.rules = [{ required: false, message: msgstr, trigger: 'change' } ]
        }
      }
      if(!this.form.status) {
        this.$set(this.form, 'status', '')
      }
      if(!this.form.defaultOrigin) {
        this.$set(this.form, 'defaultOrigin', '')
      }
      if(['radio', 'checkbox'].indexOf(this.form.type) > -1) {
        if(!this.form.arrangeType) {
          this.$set(this.form, 'arrangeType', '')
        }
      }
      if(this.form.type == 'cascader' && !this.form.pickType) {
        this.$set(this.form, 'pickType', 'cascader')
      }
      if(this.form.searchable) {
        if(!this.form.searchPopupWidth) {
          this.$set(this.form, 'searchPopupWidth', 50)
        }
      }
      if(this.form.showFrom.indexOf('fit') !== -1 && !this.form.fit) {
        this.$set(this.form, 'fit', 'contain')
      }
      if(this.form.eventHttp && this.form.eventHttpEnable !== false) {
        this.$set(this.form, 'eventHttpEnable', true)
      }
      if(this.form.type == 'tab' && this.form.status == 'collapse' && this.form.accordion !== true){
        this.$set(this.form, 'accordion', false)
      }
      if(this.form.type == 'inputNumber') {
        if(!this.form.thoudsandthable && this.form.thoudsandthable !== false) {
          this.$set(this.form, 'thoudsandthable', false)
        }
        // JavaScript中的Number类型能表示的最大安全整数是2^53-1（即 9007199254740991 ）
        if(!this.form.min && this.form.min !== 0) {
          this.$set(this.form, 'min', -9007199254740991)
        }
        if(!this.form.max && this.form.max !== 0) {
          this.$set(this.form, 'max', 9007199254740991)
        }
      }
      if(['tableForm'].indexOf(this.form.type) > -1) {
        if(!this.form.addBtnOrigin) {
          this.$set(this.form, 'addBtnOrigin', '')
        }
        if(!this.form.addBtnText) {
          this.$set(this.form, 'addBtnText', '新增')
        }
        if(!this.form.addPopupWidth) {
          this.$set(this.form, 'addPopupWidth', 50)
        }
        if(!this.form.editBtnText) {
          this.$set(this.form, 'editBtnText', '编辑')
        }
        if(!this.form.editPopupWidth) {
          this.$set(this.form, 'editPopupWidth', 50)
        }
        if(!this.form.viewBtnText) {
          this.$set(this.form, 'viewBtnText', '详情')
        }
        if(!this.form.viewPopupWidth) {
          this.$set(this.form, 'viewPopupWidth', 50)
        }
        if(!this.form.delBtnText) {
          this.$set(this.form, 'delBtnText', '删除')
        }
        if(this.form.addBtnOrigin == 'table' && this.form.addBtnFormId) {
          this.changeModelPropList(this.form.addBtnFormId, 'openFormDataMModelField')
        }
      }
      let thiz = this
      this.formclass = 'animated bounceInRight'
      let timer = setTimeout(function () {
        thiz.formclass = ''
        window.clearTimeout(timer)
      }, 1000)
      console.log('bianhua')
      this.activeName = newVal.prop ? '1' : '3'
      if(newVal.id && newVal.id != oldVal.id) {
        if(newVal.type) {
          this.activeAttrs = ['divider'].indexOf(newVal.type) > -1 ? [] : ['basic'] // , 'event', 'validate', 'style']
          this.activeAttr = ['divider'].indexOf(newVal.type) > -1 ? 'eachother' : ''
        }else{
          this.activeAttrs = []
          this.activeAttr = ''
        }
      }
      if(['imageUpload'].indexOf(newVal.type) > -1 && newVal.defaultValue && newVal.defaultValue.length > 0) {
        this.imageUrl = newVal.defaultValue[0].url
      }else{
        this.imageUrl = ''
      }
      if(['imageUpload', 'fileUpload'].indexOf(newVal.type) > -1) {
        if(!this.form.uploadModule) {
          this.$set(this.form, 'uploadModule', '/jvs-ui/form/')
        }
      }
      this.connectFormPropsList = []
      if(newVal.type == 'connectForm') {
        if(newVal.formId != oldVal.formId) {
          this.connectFormPropsList = []
          for(let i in this.connectFormOption) {
            if(this.form.formId == this.connectFormOption[i].id) {
              this.form.dataModelId = this.connectFormOption[i].dataModelId
              getConnectFormProps(this.jvsAppId, this.connectFormOption[i].dataModelId, this.connectFormOption[i].id).then(res => {
                if(res.data && res.data.code == 0) {
                  this.connectFormPropsList = res.data.data
                }
              })
            }
          }
        }
      }
      if(["formbox", "tableForm", "reportTable"].indexOf(newVal.type) > -1) {
        if(newVal.formId != oldVal.formId) {
          this.dataLinkageModelId = newVal.formId;
          this.changeItemModelPropList(null);
        }
        if(this.form.dataFilterEnable !== false && this.form.formId) {
          this.$set(this.form, 'dataFilterEnable', true)
        }
      }
      if(newVal.parentType && ["formbox", "tableForm", "reportTable"].indexOf(newVal.parentType) > -1) {
        if(newVal.formId != oldVal.formId) {
          this.dataLinkageModelId = newVal.formId;
          this.changeItemModelPropList(null);
        }
      }
      if((this.formulaEnableList.indexOf(newVal.type) > -1 || ['user', 'role', 'department', 'group', 'job', 'textarea', 'flowNode'].indexOf(newVal.type) > -1) && !newVal.searchable && ['flowTable'].indexOf(newVal.parentType) == -1) {
        if(this.form.dataLinkageEnable !== false && this.form.dataLinkageModelId) {
          this.$set(this.form, 'dataLinkageEnable', true)
        }
        if(this.form.dataLinkageEnable !== true) {
          this.$set(this.form, 'dataLinkageEnable', false)
        }
      }
      if(!this.connectFormOption || this.connectFormOption.length == 0) {
        this.getConnectSourceHandle()
      }
      if(!this.dataModelList || this.dataModelList.length == 0) {
        this.getDataModelList()
      }
      if(((newVal.showFrom.indexOf("url") !== -1 && newVal.datatype === "dataModel") || newVal.searchable) && newVal.formId) {
        this.changeModelPropList(newVal.formId, 'init')
      }
      if(newVal.datatype == 'dom' && newVal.formId) {
        this.domBindChange(null)
      }
      if(this.form.type == 'pageTable' && this.form.formId) {
        this.pageList.filter(pit => {
          if(pit.id == this.form.formId) {
            this.$set(this.form, 'dataModelId', pit.dataModelId)
            this.$set(this.form, 'jvsAppId', pit.jvsAppId)
          }
        })
      }
      if(['user', 'department', 'role', 'job'].indexOf(this.form.type) > -1) {
        if(!this.form.isDefault) {
          this.$set(this.form, 'isDefault', false)
        }
      }
      this.$forceUpdate()
    },
    stepBtnList: {
      handler(newVal, oldVal) {
        if(this.stepBtnIndex > -1) {
          this.$set(this.form.dicData[this.stepBtnIndex], 'btns', newVal)
        }
      }
    },
    showBtnSetting: {
      handler(newVal, oldVal){
        if(newVal > -1) {
          this.activeName = '4'
          this.buttonSetting()
        }
      }
    },
  }
};
</script>

<style lang="scss" scoped>
.custom-label-slot-box{
  display: flex;
  align-items: center;
  word-break: keep-all;
}
.icon-info{
  margin-left: 5px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #C2C5CF;
  color: #fff;
  cursor: pointer;
  display: inline-block;
  text-align: center;
  line-height: 10px;
  span{
    font-size: 10px;
  }
}
.attrcont{
  position: relative;
  height: 100%;
  box-sizing: border-box;
   border-radius: 0;
  overflow-y: auto;
  background: #fff;
  /deep/.el-card__body{
    padding: 0;
  }
  /deep/.el-tabs{
    animation: none;
    .el-tabs__content{
      padding: 0;
    }
  }
  .no-label-item{
    .el-form-item__content{
      margin-left: 0!important;
    }
  }
}
.attrcont::-webkit-scrollbar{
  display: none;
}
.attrcontzzc{
  position: absolute;
  top: 0;
  right: 0;
  left: 0;
  bottom: 0;
  background: #ffffff;
  box-shadow: none;
}
/deep/.attribute-collapse{
  border: 0;
  background: none;
  .el-collapse-item{
    .el-collapse-item__content{
      padding-bottom: 10px;
      .el-form-item{
        margin-bottom: 12px;
        .el-form-item__label{
          font-size: 12px!important;
          line-height: 32px!important;
          font-family: Microsoft YaHei-Regular, Microsoft YaHei;
          font-weight: 400;
          font-size: 14px;
          color: #6F7588;
        }
        .form-icon-btn{
          width: 100%;
          height: 32px;
          background: #EDF4FF;
          border-radius: 4px;
          display: flex;
          align-items: center;
          justify-content: center;
          cursor: pointer;
          svg{
            width: 16px;
            height: 16px;
            margin-right: 8px;
          }
          span, i{
            font-family: Microsoft YaHei-Regular, Microsoft YaHei;
            font-weight: 400;
            font-size: 14px;
            color: #1E6FFF;
          }
        }
        .el-form-item__content{
          line-height: 32px;
          >.el-input, .el-select:not(.custom-multiple-select) .el-input, .el-input-number .el-input{
            height: 32px;
            .el-input__suffix{
              line-height: 32px;
            }
            i{
              color: #6F7588;
            }
            .el-input__inner{
              border: 0;
              height: 32px;
              line-height: 32px;
              background: #F5F6F7;
              font-family: Microsoft YaHei-Regular, Microsoft YaHei;
              font-weight: 400;
              font-size: 12px;
              text-align: left;
              &::placeholder{
                font-size: 12px;
                font-family: Microsoft YaHei-Regular, Microsoft YaHei;
                font-weight: 400;
              }
            }
          }
          .el-textarea{
            .el-textarea__inner{
              border: 0;
              background: #F5F6F7;
            }
          }
          .radio-group-row{
            width: 100%;
            height: 32px;
            white-space: nowrap;
            background-color: #F5F6F7;
            padding: 0 6px;
            border-radius: 4px;
            overflow: hidden;
            box-sizing: border-box;
            display: flex;
            align-items: center;
            .el-radio-button{
              flex: 1;
              border-radius: 4px;
              overflow: hidden;
              .el-radio-button__inner{
                width: 100%;
                border: 0;
                height: 24px;
                box-sizing: border-box;
                padding: 0;
                line-height: 24px;
                font-family: Microsoft YaHei-Regular, Microsoft YaHei;
                font-weight: 400;
                font-size: 12px;
                color: #363B4C;
                background: transparent;
                box-shadow: none;
              }
              &.is-active{
                .el-radio-button__inner{
                  background: #fff;
                  color: #1E6FFF;
                }
              }
            }
          }
          .el-slider{
            .el-slider__runway{
              width: 140px;
              height: 4px;
              background: #F5F6F7;
              border-radius: 4px;
              margin: 14px 0;
              .el-slider__bar{
                height: 4px;
                background: #1E6FFF;
              }
              .el-slider__button{
                border: 1px solid #C2C5CF;
                box-shadow: 0px 0px 2px 0px rgba(54,59,76,0.15);
              }
              .el-slider__button-wrapper{
                height: 20px;
                top: -10px;
              }
            }
            .el-slider__input{
              margin-left: 8px;
              width: 44px;
              height: 32px;
              background: #F5F6F7;
              border-radius: 4px;
              margin-top: 0;
              >span.el-input-number__decrease, >span.el-input-number__increase{
                display: none;
              }
              .el-input{
                position: relative;
                .el-input__inner{
                  padding: 0;
                  border: 0;
                  height: 32px;
                  line-height: 32px;
                  background: #F5F6F7;
                  border-radius: 4px;
                  text-align: left;
                  text-indent: 10px;
                }
              }
            }
          }
          .slider-with-unit{
            display: flex;
            align-items: center;
            .el-slider--with-input{
              width: calc(100% - 20px);
              .el-slider__runway.show-input{
                width: 120px;
              }
              .el-slider__input{
                .el-input{
                  .el-input__inner{
                    border-radius: 4px 0 0 4px;
                  }
                }
              }
            }
            .unit{
              padding-right: 5px;
              display: block;
              background: #F5F6F7;
              border-radius: 0 4px 4px 0;
            }
          }
          .el-input-number__decrease, .el-input-number__increase{
            line-height: 32px;
          }
          .after-i-icon{
            margin-left: 8px;
            display: flex;
            align-items: center;
            justify-content: center;
            width: 24px;
            height: 24px;
            border-radius: 4px;
            cursor: pointer;
            svg{
              width: 20px;
              height: 20px;
            }
          }
          .line-item-div{
            height: 32px;
            display: flex;
            align-items: center;
            .default-value-item{
              .el-input-number{
                .el-input__inner{
                  padding: 0;
                  text-align: center;
                }
              }
            }
          }
          .custom-multiple-select{
            .el-input{
              height: auto;
              .el-input__inner{
                border: 0;
                background: #F5F6F7;
                min-height: 32px;
              }
            }
            .el-select__tags{
              padding-left: 4px;
              padding-bottom: 4px;
              .el-tag{
                margin: 4px 0 0 4px;
                padding: 0 2px 0 4px;
                height: 24px;
                border: 0;
                background: #fff;
                box-sizing: border-box;
                .el-select__tags-text{
                  font-family: Source Han Sans-Regular, Source Han Sans;
                  font-weight: 400;
                  font-size: 12px;
                  color: #363B4C;
                }
                .el-tag__close{
                  position: unset;
                  margin-left: 4px;
                  width: 14px;
                  height: 14px;
                  background: transparent;
                  color: #6F7588;
                  transform: scale(1);
                }
              }
            }
          }
        }
      }
      .line-item{
        .el-form-item__label{
          line-height: 16px!important;
        }
        .el-form-item__content{
          line-height: 16px;
        }
      }
      .el-form-item__error{
        color: #FF194C;
      }
      .line-number-unit{
        display: flex;
        align-items: center;
        .el-input-number{
          flex: 1;
          overflow: hidden;
          .el-input__inner{
            border-radius: 4px 0 0 4px;
            text-align: left;
          }
        }
        .unit{
          width: 32px;
          height: 32px;
          line-height: 32px;
          text-align: center;
          font-size: 12px;
          color: #6F7588;
          background: #F5F6F7;
          border-radius: 0 4px 4px 0;
        }
      }
    }
    .el-collapse-item__header{
      height: 52px;
      line-height: 52px;
      position: relative;
      background: #fff;
      padding: 0 13px;
      box-sizing: border-box;
      text-indent: 18px;
      font-family: Source Han Sans-Bold, Source Han Sans;
      font-weight: 700;
      font-size: 14px;
      color: #363B4C;
      .el-collapse-item__arrow{
        position: absolute;
        font-size: 14px;
        left: -3px;
        top: 19px;
        width: 14px;
        height: 14px;
      }
      .el-collapse-item__arrow::before{
        content: "\e791";
      }
    }
    .el-collapse-item__wrap{
      padding: 0 16px;
    }
    &.is-active{
      .el-collapse-item__header.is-active{
        .el-collapse-item__arrow{
          transform: none;
          margin: 0;
        }
        .el-collapse-item__arrow::before{
          content: "\e790";
        }
      }
    }
  }
}
.border-top-line{
  width: 100%;
  height: 1px;
  background: #EBEEF5;
}
.inner-collapse{
  border: 0;
  /deep/.el-collapse-item{
    .el-collapse-item__header{
      height: 16px;
      line-height: 16px;
      font-family: Microsoft YaHei-Regular, Microsoft YaHei;
      font-weight: 400;
      font-size: 12px;
      text-indent: 4px;
      border: 0;
      margin-bottom: 16px;
      .slot-title-div{
        display: flex;
        align-items: center;
        justify-content: space-between;
        .line{
          width: 200px;
          border-top: 1px dashed #C2C5CF;
        }
      }
      .el-collapse-item__arrow{
        font-size: 12px;
        margin-top: 2px;
        left: -4px;
        top: 0;
        font-weight: bold;
      }
      .el-collapse-item__arrow::before{
        content: "\E6E0";
      }
    }
    .el-collapse-item__wrap{
      border: 0;
      padding: 0;
      .el-form-item__content{
        line-height: 32px;
        .el-input{
          height: 32px;
          .el-input__inner{
            border: 0;
            height: 32px;
            line-height: 32px;
            background: #F5F6F7;
            font-family: Microsoft YaHei-Regular, Microsoft YaHei;
            font-weight: 400;
            font-size: 12px;
            text-align: left;
            &::placeholder{
              font-size: 12px;
              font-family: Microsoft YaHei-Regular, Microsoft YaHei;
              font-weight: 400;
            }
          }
        }
      }
    }
    &.is-active{
      .el-collapse-item__header.is-active{
        .el-collapse-item__arrow{
          margin-top: 2px;
        }
        .el-collapse-item__arrow::before{
          content: "\e6df";
        }
      }
    }
  }
}
.avatar-uploader{
  /deep/ .el-upload {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
  }
  .el-upload:hover {
    border-color: #409EFF;
  }
  .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 178px;
    height: 178px;
    line-height: 178px;
    text-align: center;
  }
  .avatar {
    width: 178px;
    height: 178px;
    display: block;
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
.data-filter-item-linkage{
  display: flex;
  justify-content: flex-start;
  align-items: center;
  margin-top: 10px;
  .al-item{
    margin-right: 10px;
  }
}
.copy-prop-item{
  /deep/.el-form-item__content{
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    .el-select{
      width: auto!important;
      flex: 1;
    }
    .icon-document-copy{
      margin-left: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      width: 32px;
      height: 32px;
      border-radius: 4px;
      background-color: #F5F6F7;
      cursor: pointer;
      svg{
        width: 16px;
        height: 16px;
      }
    }
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

// 表单设置
.form-set-tab-panel{
  padding: 0 24px;
  /deep/.el-form-item{
    margin-top: 16px;
    margin-bottom: 0;
    .el-form-item__label{
      padding-bottom: 8px;
    }
    .form-icon-btn{
      cursor: pointer;
      font-size: 16px;
      &:hover{
        color: #3471ff;
      }
    }
    .el-form-item__content{
      line-height: 32px;
      .el-slider{
        .el-slider__runway{
          width: 192px;
          height: 8px;
          background: #F5F6F7;
          border-radius: 4px;
          .el-slider__bar{
            background: #1E6FFF;
          }
          .el-slider__button{
            border: 1px solid #C2C5CF;
            box-shadow: 0px 0px 2px 0px rgba(54,59,76,0.15);
          }
        }
        .el-slider__input{
          margin: 0;
          width: 64px;
          height: 32px;
          >span.el-input-number__decrease, >span.el-input-number__increase{
            display: none;
          }
          .el-input{
            position: relative;
            .el-input__inner{
              padding: 0;
              border: 0;
              height: 32px;
              line-height: 32px;
              background: #F5F6F7;
              border-radius: 4px;
              text-align: left;
              text-indent: 10px;
            }
            &::after{
              content: 'px';
              position: absolute;
              right: 5px;
            }
          }
        }
      }
      .el-select{
        .el-input__inner{
          height: 32px;
          line-height: 32px;
          border: 0;
          background: #F5F6F7;
          border-radius: 4px;
          font-family: Microsoft YaHei-Regular, Microsoft YaHei;
          font-weight: 400;
          font-size: 14px;
          color: #363B4C;
          &::placeholder{
            font-family: Microsoft YaHei-Regular, Microsoft YaHei;
            font-weight: 400;
            font-size: 14px;
            color: #363B4C;
          }
        }
        .el-select__caret.el-icon-arrow-up{
          color: #363B4C;
          font-weight: bold;
        }
      }
    }
  }
  .align-type-list{
    height: 32px;
    background: #F5F6F7;
    border-radius: 4px;
    padding: 0 17px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    .align-type-list-item{
      width: 56px;
      height: 28px;
      border-radius: 3px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      .short-line{
        width: 10px;
        height: 2px;
        background: #6F7588;
        border-radius: 1px;
        &.half{
          width: 5px;
        }
      }
      .long-line{
        width: 22px;
        height: 4px;
        background: #C2C5CF ;
        border-radius: 1px;
      }
      &:hover, &.active{
        background: #fff;
        .short-line{
          background: #1E6FFF;
        }
        .long-line{
          background: #8EB7FF;
        }
      }
      &.top{
        .short-line{
          margin-bottom: 1px;
        }
        .short-line.half{
          margin-top: 2px;
        }
      }
    }
  }
  .popup-type-list{
    height: 32px;
    background: #F5F6F7;
    border-radius: 4px;
    padding: 2px;
    box-sizing: border-box;
    display: flex;
    align-items: center;
    .popup-type-list-item{
      flex: 1;
      text-align: center;
      line-height: 28px;
      font-family: Microsoft YaHei-Regular, Microsoft YaHei;
      font-weight: 400;
      font-size: 14px;
      color: #363B4C;
      border-radius: 4px;
      cursor: pointer;
      &.active{
        color: #1E6FFF;
        background: #fff;
      }
      &:hover{
        color: #1E6FFF;
      }
    }
  }
  .slider-end-unit{
    display: block;
    width: 64px;
    height: 32px;
    background: #F5F6F7;
    border-radius: 4px;
    text-align: center;
    font-family: Microsoft YaHei-Regular, Microsoft YaHei;
    font-weight: 400;
    font-size: 14px;
    color: #363B4C;
  }
  .all-line-button{
    height: 32px;
    background: #F5F6F7;
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-family: Microsoft YaHei-Regular, Microsoft YaHei;
    font-weight: 400;
    font-size: 14px;
    color: #363B4C;
    cursor: pointer;
    svg{
      fill: #363B4C;
      width: 16px;
      height: 16px;
    }
    span{
      padding: 0 8px;
    }
    &:hover{
      color: #1E6FFF;
      svg{
        fill: #1E6FFF;
      }
    }
  }
  .full-log-item-box{
    /deep/.el-checkbox{
      .el-checkbox__input{
        .el-checkbox__inner{
          width: 16px;
          height: 16px;
          border-radius: 4px;
          font-size: 16px;
        }
        &.is-checked{
          .el-checkbox__inner{
            background: #1E6FFF;
            border-color: #1E6FFF;
            &::after{
              left: 5px;
              top: 2px;
              border-width: 2px;
            }
          }
        }
      }
      .el-checkbox__label{
        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
        font-weight: 400;
        font-size: 14px;
        color: #363B4C;
      }
    }
  }
}
// 按钮设置
.form-button-setting-list{
  padding: 0 24px;
  .form-button-setting-list-item{
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
  .add-button-tool{
    margin-top: 16px;
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
.line-item{
  /deep/.el-form-item__content{
    .el-checkbox{
      margin-right: 12px;
      .el-checkbox__input{
        .el-checkbox__inner{
          width: 16px;
          height: 16px;
          border-radius: 4px;
          border-color: #C4C9D4;
        }
        &.is-checked{
          .el-checkbox__inner{
            background: #1E6FFF;
            border-color: #1E6FFF;
            &::after{
              left: 5px;
              top: 2px;
              border-width: 2px;
            }
          }
        }
      }
    }
    .label-text{
      font-family: Microsoft YaHei-Regular, Microsoft YaHei;
      font-weight: 400;
      font-size: 14px;
      color: #363B4C;
      line-height: 18px;
    }
  }
}
.place-table-box{
  margin-bottom: 16px;
  .index-item{
    width: 88px;
    height: 23px;
    display: flex;
    align-items: center;
    svg{
      margin-left: 16px;
      width: 16px;
      height: 16px;
      fill: #D8D8D8;
      transform: rotate(90deg);
    }
  }
  .title-item{
    flex: 1;
    margin-right: 8px;
    overflow: hidden;
    font-family: Microsoft YaHei-Regular, Microsoft YaHei;
    font-weight: 400;
    font-size: 12px;
    color: #3D3D3D;
  }
  .svg-icon-item{
    width: 16px;
    height: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    svg{
      width: 16px;
      height: 16px;
      cursor: pointer;
      fill: #fff;
      border-radius: 4px;
    }
    .del-icon{
      width: 16px;
      height: 16px;
      background: #36B452;
      border-radius: 4px;
      cursor: pointer;
      position: relative;
      &::after{
        position: absolute;
        left: 3px;
        top: 7px;
        content: '';
        width: 10px;
        height: 2px;
        background: #FFFFFF;
        border-radius: 2px;
      }
    }
  }
  .place-table-box-title{
    display: flex;
    align-items: center;
  }
  .place-table-box-content{
    margin-top: 9px;
    .place-table-box-content-item{
      display: flex;
      align-items: center;
      .title-item{
        display: flex;
        align-items: center;
        /deep/.el-input{
          flex: 1;
          overflow: hidden;
          .el-input__inner{
            border: 0;
            background: #F5F6F7;
            padding: 0 8px;
            height: 32px;
            line-height: 32px;
            font-family: Microsoft YaHei-Regular, Microsoft YaHei;
            font-weight: 400;
            font-size: 12px;
            color: #3D3D3D;
          }
        }
      }
      &+.place-table-box-content-item{
        margin-top: 8px;
      }
    }
  }
}
.left-line-div{
  border-left: 1px solid #EEEFF0;
  position: relative;
  .el-form-item{
    /deep/.el-form-item__label{
      text-indent: 10px;
      .custom-label-slot-box .icon-info{
        text-indent: 0;
      }
    }
  }
  &::before{
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 4px;
    border-top: 1px solid #EEEFF0;
  }
  &::after{
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    width: 4px;
    border-top: 1px solid #EEEFF0;
  }
}
.express-display-form{
  /deep/.el-form{
    .form-column-colorPicker{
      .jvs-form-item{
        .jvs-color-picker-show-box{
          min-width: 150px;
        }
      }
    }
  }
}
</style>
<style lang="scss">
.top-align-item{
  .el-form-item__content{
    margin-left: 0!important;
  }
}
.step-button-table{
  .el-table__header-wrapper{
    thead{
      .cell{
        text-align: center;
      }
    }
  }
  .el-table__body{
    tbody{
      .el-form-item__content{
        text-align: center;
      }
    }
  }
  .sort-row-column{
    text-align: center;
    i{
      cursor: pointer;
      font-size: 20px;
      margin-right: 10px;
    }
  }
}
.show-express{
  display: flex;
  width: 100%;
  height: 470px;
  box-sizing: border-box;
  overflow: hidden;
  .left{
    padding-top: 10px;
    height: 100%;
    border-right: 1px solid #DCDFE6;
    overflow: hidden;
    overflow-y: auto;
    box-sizing: border-box;
  }
  .right{
    flex: 1;
    overflow: hidden;
    margin-right: 20px;
    margin-top: 10px;
  }
  .el-tree{
    width: 250px;
    min-width: 250px;
    max-width: 350px;
  }
  .el-tree::-webkit-scrollbar{
    display: none;
  }
  .el-form{
    .el-col{
      margin-bottom: 0;
      margin-left: 10px;
    }
    .el-col:nth-last-of-type(1){
      margin-left: 0;
    }
    .el-form-item{
      margin-bottom: 0;
    }
    .table-form{
      .table-body-box{
        .el-table__body-wrapper {
          height: 375px!important;
        }
      }
      .jvs-table{
        .cell{
          .el-input__inner{
            text-align: left;
          }
        }
      }
    }
  }
}
.button-set-table{
  thead{
    tr{
      th{
        text-align: left;
      }
    }
  }
  tbody{
    tr{
      td{
        .cell{
          text-align: left;
        }
      }
    }
  }
}
</style>
