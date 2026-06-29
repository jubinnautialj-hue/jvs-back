<template>
  <div class="setting-info">
    <jvs-form ref="paramForm" :formData="form" :option="formOption" :notLoading="true"  @formChange="formChange" @submit="submitHandle">
      <div v-for="before in beforeSlotList" :key="'before-slot-'+before.prop" :slot="before.prop+'Before'" class="slot-before-item">
        <span v-html="before.explain"></span>
      </div>
      <div v-for="item in soltList" :key="item.prop" :slot="item.prop+'Form'">
        <!-- list类型 -->
        <div v-if="item.type == 'list'" class="input-list-box">
          <ul v-if="item.list && item.list.length > 0" class="input-list">
            <li v-for="(it, iindex) in item.list" :key="item.prop+'input'+iindex">
              <el-input v-model="item.list[iindex]" @blur="listChangeHandle(item)"></el-input>
              <span class="delete-icon-button" @click="delLine(iindex, item)">
                <span class="border-line"></span>
              </span>
            </li>
          </ul>
          <div class="bottom-button">
            <div class="button" @click="addLine(item, 'string')">
              <div class="icon">
                <svg aria-hidden="true">
                  <use xlink:href="#jvs-ui-icon-xinjian"></use>
                </svg>
              </div>
              <span>新增一行</span>
            </div>
          </div>
        </div>
        <!-- map类型 -->
        <div class="map-box" v-if="item.type == 'map' || item.type == 'filterMap'">
          <p class="table-head">
            <span>参数名</span>
            <span v-if="item.type == 'filterMap'">条件</span>
            <span>参数值</span>
            <span></span>
            <i></i>
          </p>
          <div v-if="item.list && item.list.length > 0" class="map-table">
            <p v-for="(it, iindex) in item.list" :key="item.prop+'map'+iindex">
              <span style="position: relative;">
                <el-input v-model="it.fieldKey" placeholder="请输入参数名" @change="validateKey(it, 'change')" @blur="validateKey(it, 'blur');setValueOfMap(item, item.prop)"></el-input>
                <span v-if="!validateKey(it, 'change')" style="position: absolute;font-size: 12px;bottom: 0;left: 12px;line-height: 12px;color: #F56C6C;">不能以数字开头</span>
              </span>
              <span v-if="item.type == 'filterMap'">
                <el-select v-model="it.enabledQueryTypes" placeholder="请选择" size="mini" @change="setValueOfMap(item, item.prop)">
                  <el-option label="大于" value="gt"></el-option>
                  <el-option label="小于" value="lt"></el-option>
                  <el-option label="等于" value="eq"></el-option>
                  <el-option label="小于等于" value="le"></el-option>
                  <el-option label="大于等于" value="ge"></el-option>
                </el-select>
              </span>
              <span>
                <el-select v-model="it.prop" placeholder="请选择" size="mini" @change="setValueOfMap(item, item.prop)">
                  <el-option label="字段" value="field"></el-option>
                  <el-option label="值" value="value"></el-option>
                  <el-option label="公式" value="formula"></el-option>
                  <el-option label="空" value="empty"></el-option>
                </el-select>
              </span>
              <span>
                <tree-selector
                  v-if="it.prop == 'field' && it.fieldKey"
                  :form="it"
                  :item="{prop: 'value'}"
                  :options="bindParameterOption"
                  :showalllevels="false"
                  :createInit="true"
                  :props="{
                    label: 'shortName',
                    value: 'id',
                    children: 'children',
                    expandTrigger: 'hover',
                    multiple: false,
                    emitPath: false,
                    checkStrictly: true
                  }"
                  :key="item.prop+'map'+iindex+'-'+it.value"
                  @change="setValueOfMap(item, item.prop)"
                ></tree-selector>
                <el-input v-if="it.prop == 'value' && it.fieldKey" v-model="it.value" @blur="setValueOfMap(item, item.prop)"></el-input>
                <el-button v-if="it.prop == 'formula' && it.fieldKey" size="mini" type="text" class="set-formula-button" @click="openFormula(it, item, item.prop)">配置公式</el-button>
              </span>
              <i>
                <span class="delete-icon-button" @click="delItem(iindex, item)">
                  <span class="border-line"></span>
                </span>
              </i>
            </p>
          </div>
          <div class="bottom-button">
            <div class="button" @click="addLine(item, 'obj')">
              <div class="icon">
                <svg aria-hidden="true">
                  <use xlink:href="#jvs-ui-icon-xinjian"></use>
                </svg>
              </div>
              <span>新增一行</span>
            </div>
          </div>
        </div>
        <!-- 富文本： longtext -->
        <div v-if="item.type == 'longtext'" :id="item.prop"></div>
        <!-- <div v-if="item.type == 'longtext' && item.required == true && (!form[item.prop] || form[item.prop] == '')" class="el-form-item__error">必填</div> -->
        <!-- 代码编译器： code 类型  -->
        <div class="codeEditor-box" v-if="['code', 'sql', 'json', 'groovy'].indexOf(item.type) > -1">
          <codeEditor class="codeEditor" :lang="['sql', 'json', 'groovy'].indexOf(item.type) > -1 ? item.type : 'java'" :code="form[item.prop]" :prop="item.prop" @change="codeEditorChange"></codeEditor>
          <div v-if="(item.type == 'code' || item.type == 'sql' ) && item.required == true && (!form[item.prop] || form[item.prop] == '')" class="el-form-item__error">必填</div>
        </div>
        <!-- listMap类型 -->
        <div class="list-map-box" v-if="item.type == 'listMap'">
          <div class="list-map-group-item" v-for="(it, iindex) in item.list" :key="item.prop+'map-list'+iindex">
            <div class="header">
              <div class="head-left">
                <div class="line"></div>
                <div class="text">组{{Number(iindex)+1}}</div>
              </div>
              <div class="head-right">
                <svg :class="{'close-icon': !(openIndex[item.prop] && openIndex[item.prop].indexOf(iindex) > -1)}" aria-hidden="true" @click="openCloseIndex(item.prop, iindex)">
                  <use xlink:href="#icon-jvs-danchuang-shouqi"></use>
                </svg>
                <svg aria-hidden="true" style="width: 14px;height: 14px;" @click="deleteGroup(iindex, item)">
                  <use xlink:href="#icon-jvs-danchuangguanbi1"></use>
                </svg>
              </div>
            </div>
            <el-collapse-transition>
              <div v-show="openIndex[item.prop] && openIndex[item.prop].indexOf(iindex) > -1" class="bottom-body">
                <p class="table-head">
                  <span>参数名</span>
                  <span>参数值</span>
                  <span></span>
                </p>
                <div v-if="it && it.length > 0" class="list-map-table">
                  <p v-for="(ic, ici) in it" :key="item.prop+'map-list'+iindex+'-'+ici">
                    <span>
                      <el-input v-model="ic.key" @input="setValueOfMapList(item)" placeholder="请输入参数名"></el-input>
                    </span>
                    <span>
                      <el-select v-model="ic.prop" placeholder="请选择" size="mini" @change="setValueOfMapList(item)" style="width: 50%;margin-right: 10px;">
                        <el-option label="字段" value="field"></el-option>
                        <el-option label="值" value="value"></el-option>
                        <el-option label="公式" value="formula"></el-option>
                        <el-option label="空" value="empty"></el-option>
                      </el-select>
                      <tree-selector
                        v-if="ic.prop == 'field' && ic.key"
                        :form="ic"
                        :item="{prop: 'value'}"
                        :options="bindParameterOption"
                        :showalllevels="false"
                        :createInit="true"
                        :props="{
                          label: 'shortName',
                          value: 'id',
                          children: 'children',
                          expandTrigger: 'hover',
                          multiple: false,
                          emitPath: false,
                          checkStrictly: true
                        }"
                        :key="item.prop+'map-list'+iindex+'-'+ici+'-'+ic.value"
                        @change="setValueOfMapList(item)"
                      ></tree-selector>
                      <el-input v-if="ic.prop == 'value' && ic.key" v-model="ic.value"  @input="setValueOfMapList(item)"></el-input>
                      <el-button v-if="ic.prop == 'formula' && ic.key" size="mini" type="text" class="set-formula-button" @click="openFormula(ic, item, item.prop, 'listMap')">配置公式</el-button>
                    </span>
                    <span>
                      <span class="delete-icon-button" @click="delItemOfGroup(ici, it, item)">
                        <span class="border-line"></span>
                      </span>
                    </span>
                  </p>
                </div>
                <div v-if="item.type == 'listMap'" class="bottom-button">
                  <div class="button" @click="addLineToGroup(it, 'obj', item)">
                    <div class="icon">
                      <svg aria-hidden="true">
                        <use xlink:href="#jvs-ui-icon-xinjian"></use>
                      </svg>
                    </div>
                    <span>新增一行</span>
                  </div>
                </div>
              </div>
            </el-collapse-transition>
          </div>
          <h4 :class="{'add-one-group-button': true, 'showmargin': (item.list && item.list.length > 0)}">
            <el-button  @click="addNewGroup(item)">
              <svg aria-hidden="true">
                <use xlink:href="#jvs-ui-icon-xinjian"></use>
              </svg>
              <span>添加一组</span>
            </el-button>
          </h4>
        </div>
        <!-- tree树形 -->
        <div class="tree-box" v-if="['treeSelected', 'treeSelectedChild'].indexOf(item.type) > -1" style="padding:10px;">
          <el-cascader
            :ref="item.prop+'Tree'"
            style="width:100%;"
            v-model="form[item.prop]"
            size="mini"
            :options="item.options"
            clearable
            :show-all-levels="true"
            :collapse-tags="true"
            :props="{
              expandTrigger: 'hover',
              multiple: false,
              children: 'childList',
              label: 'label',
              value: 'value',
              emitPath: true,
              checkStrictly: item.type == 'treeSelected' ? true : false
            }"
            @change="formChange(form, item.prop+'Tree')"
          >
          </el-cascader>
        </div>
        <!-- 数据模型表格 -->
        <div :class="{'model-list-box data-model-box': true, 'data-model-box-4': item.type == 'dataModelFilterField', 'data-model-box-2': item.type == 'dataModelOrderField'}" v-if="['dataModelField', 'dataModelFilterField', 'dataModelOrderField'].indexOf(item.type) > -1">
          <div v-if="form[item.prop] && form[item.prop].length > 0" class="data-model-table">
            <p v-for="(row, rindex) in form[item.prop]" :key="'model_row'+rindex" v-if="form[item.prop][rindex]">
              <span>
                <el-select v-model="form[item.prop][rindex].fieldKey" placeholder="请选择" size="mini" filterable @change="fieldKeyChange(item, rindex)">
                  <el-option v-for="(io, iox) in item.options" :key="item.prop+'option-item-'+iox" :label="io.fieldName || io.label" :value="io.fieldKey || io.value" :disabled="isDisabled(io, row, form[item.prop])"></el-option>
                </el-select>
              </span>
              <b v-if="item.type == 'dataModelField'">等于</b>
              <span v-if="item.type == 'dataModelFilterField'">
                <el-select v-model="form[item.prop][rindex].enabledQueryTypes" placeholder="请选择" size="mini" clearable>
                  <el-option v-for="ei in getItemEnabledQueryTypes(row, item)" :key="ei.value+'enabledQueryTypes-item'" :label="ei.label" :value="ei.value"></el-option>
                </el-select>
              </span>
              <span v-if="['dataModelOrderField'].indexOf(item.type) == -1" class="oprator">
                <el-select v-model="form[item.prop][rindex].prop" placeholder="请选择" size="mini">
                  <el-option label="字段" value="field"></el-option>
                  <el-option label="值" value="value"></el-option>
                  <el-option label="公式" value="formula"></el-option>
                  <el-option v-if="item.type == 'dataModelFilterField' ? (['eq', 'ne'].indexOf(form[item.prop][rindex].enabledQueryTypes) > -1) : true" label="空" value="empty"></el-option>
                </el-select>
              </span>
              <span v-if="['dataModelOrderField'].indexOf(item.type) == -1">
                <tree-selector
                  v-if="form[item.prop][rindex].prop == 'field'"
                  :form="form[item.prop][rindex]"
                  :item="{prop: 'value'}"
                  :options="setValueOption"
                  :showalllevels="false"
                  :createInit="true"
                  :props="{
                    label: 'shortName',
                    value: 'id',
                    children: 'children',
                    expandTrigger: 'hover',
                    multiple: false,
                    emitPath: false,
                    checkStrictly: true
                  }"
                  :key="'model_row'+rindex+'-'+form[item.prop][rindex].value"
                ></tree-selector>
                <el-input v-if="form[item.prop][rindex].prop == 'value'" size="mini" v-model="form[item.prop][rindex].value"></el-input>
                <el-button v-if="form[item.prop][rindex].prop == 'formula'" size="mini" type="text" class="set-formula-button" @click="setLinkFormula(row, rindex, item.prop)">配置公式</el-button>
              </span>
              <span v-if="['dataModelOrderField'].indexOf(item.type) > -1">
                <el-select v-model="form[item.prop][rindex].direction" placeholder="请选择" size="mini">
                  <el-option label="升序" value="ASC"></el-option>
                  <el-option label="降序" value="DESC"></el-option>
                </el-select>
              </span>
              <i>
                <span class="delete-icon-button" @click="delRowHandle(form[item.prop][rindex], rindex, form[item.prop], item.prop)">
                  <span class="border-line"></span>
                </span>
              </i>
            </p>
          </div>
          <div v-if="form[item.prop].length < item.options.length" class="bottom-button">
            <div class="button" @click="addItem(item)">
              <div class="icon">
                <svg aria-hidden="true">
                  <use xlink:href="#jvs-ui-icon-xinjian"></use>
                </svg>
              </div>
              <span>新增一行</span>
            </div>
          </div>
        </div>
        <!-- OA流程节点 -->
        <div :class="{'data-model-box': true, 'data-model-box-3': item.type == 'flowNode'}" v-if="['flowNode'].indexOf(item.type) > -1">
          <div v-if="form[item.prop] && form[item.prop].length > 0">
            <p v-for="(row, rindex) in item.options" :key="'flow_row'+rindex" filterable v-if="form[item.prop][rindex]">
              <span style="flex:1;">
                <el-select v-model="form[item.prop][rindex].fieldKey" placeholder="请选择" size="mini" disabled style="width:100%;">
                  <el-option v-for="(io, iox) in item.options" :key="item.prop+'option-item-'+iox" :label="io.label" :value="io.value"></el-option>
                </el-select>
              </span>
              <b>等于</b>
              <span>
                <el-button size="mini" type="text" @click="setLinkFormula(row, rindex, item.prop)">设置公式</el-button>
              </span>
            </p>
          </div>
        </div>
        <!-- 图片选择 -->
        <div v-if="item.type == 'imageSelect'">
          <div v-if="form[item.prop]" class="select-image-show">
            <img :src="form[item.prop]" alt="">
            <div class="delete-select-image-tool">
              <i class="el-icon-delete" @click="delIamgeSelect(item.prop)"></i>
            </div>
          </div>
          <div v-else class="choose-image-box" @click="chooseImage(item.prop)">
            <svg class="icon" aria-hidden="true" style="width: 24px;height: 24px;">
              <use xlink:href="#icon-jvs-a-huaban12"></use>
            </svg>
            <div>选择图片</div>
          </div>
        </div>
        <!-- 绑定变量 -->
        <div class="data-model-box data-model-box-3" v-if="['bind', 'assignment'].indexOf(item.type) > -1">
          <div v-if="form[item.prop] && form[item.prop].length > 0">
            <div v-for="(row, rindex) in form[item.prop]" :key="'bpl-item-'+rindex" class="bind-parameter-list">
              <!-- 条件 -->
              <div class="condition-box">
                <div class="condition-head">
                  <div style="display: flex;align-items: center;">
                    <el-radio-group v-model="row.bindType">
                      <el-radio label="condition">条件规则</el-radio>
                      <el-radio label="formula">公式</el-radio>
                    </el-radio-group>
                  </div>
                  <div style="display: flex;align-items: center;">
                    <!-- 添加公式 -->
                    <svg aria-hidden="true" v-if="row.bindType == 'formula'" class="add-formula-svg" style="position: unset;margin-right: 16px;" @click="setBindGroupFormula(item, rindex, item.prop)">
                      <use :xlink:href="`#${(row.formula && row.formulaContent && row.formulaContent.trim().length > 0) ? 'icon-jvs-a-zu10923' : 'icon-jvs-rongqi1'}`"></use>
                    </svg>
                    <!-- 删除按钮 -->
                    <svg aria-hidden="true" style="width: 14px;height: 14px;" @click="deleteBindGroup(item.prop, rindex)">
                      <use xlink:href="#icon-jvs-danchuangguanbi1"></use>
                    </svg>
                  </div>
                </div>
                <div v-if="row.bindType == 'condition'" class="condition-table">
                  <p v-for="(cit, cindex) in row.conditions" :key="'condi_row'+cindex" class="condi-item">
                    <span>
                      <tree-selector
                        :form="cit"
                        :item="{prop: 'fieldKey'}"
                        :options="bindParameterOption"
                        :showalllevels="false"
                        :createInit="true"
                        :props="{
                          label: 'shortName',
                          value: 'id',
                          children: 'children',
                          expandTrigger: 'hover',
                          multiple: false,
                          emitPath: false,
                          checkStrictly: true
                        }"
                        :key="'condi_row'+cindex+'-fieldKey-'+cit.fieldKey"
                      ></tree-selector>
                    </span>
                    <!-- <b>等于</b> -->
                    <span>
                      <el-select v-model="cit.enabledQueryTypes" placeholder="请选择" size="mini">
                        <el-option label="等于" value="eq"></el-option>
                        <el-option label="大于" value="gt"></el-option>
                        <el-option label="小于" value="lt"></el-option>
                        <el-option label="不等于" value="ne"></el-option>
                        <el-option label="小于等于" value="le"></el-option>
                        <el-option label="大于等于" value="ge"></el-option>
                        <el-option label="包含" value="in"></el-option>
                        <el-option label="不包含" value="notIn"></el-option>
                      </el-select>
                    </span>
                    <span>
                      <el-select v-model="cit.prop" placeholder="请选择" size="mini">
                        <el-option label="字段" value="field"></el-option>
                        <el-option label="值" value="value"></el-option>
                        <el-option label="公式" value="formula"></el-option>
                        <el-option v-if="['eq', 'ne'].indexOf(cit.enabledQueryTypes) > -1" label="空" value="empty"></el-option>
                      </el-select>
                    </span>
                    <span>
                      <tree-selector
                        v-if="cit.prop == 'field'"
                        :form="cit"
                        :item="{prop: 'value'}"
                        :options="bindParameterOption"
                        :showalllevels="false"
                        :createInit="true"
                        :props="{
                          label: 'shortName',
                          value: 'id',
                          children: 'children',
                          expandTrigger: 'hover',
                          multiple: false,
                          emitPath: false,
                          checkStrictly: true
                        }"
                        :key="'condi_row'+cindex+'-value-'+cit.value"
                      ></tree-selector>
                      <el-input v-if="cit.prop == 'value'" size="mini" v-model="cit.value"></el-input>
                      <el-button v-if="cit.prop == 'formula'" size="mini" type="text" class="set-formula-button" @click="setBindFormula(cit, rindex, cindex, item.prop, 'conditions')">配置公式</el-button>
                    </span>
                    <i>
                      <span class="delete-icon-button" @click="delBindParamRowHandle(cit, cindex, form[item.prop][rindex].conditions)">
                        <span class="border-line"></span>
                      </span>
                    </i>
                  </p>
                  <div class="bottom-button">
                    <div class="button" @click="addBindParamRowHandle(form[item.prop][rindex].conditions, 'enabledQueryTypes', 'eq')">
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
              <!-- 变量 -->
              <div class="param-box">
                <h4>{{item.type == 'assignment' ? '变量赋值' : '变量绑定'}}</h4>
                <p v-for="(prow, pindex) in form[item.prop][rindex].params" :key="'param_row-'+rindex+'-'+pindex" class="bind-parameter-list-item">
                  <span>
                    <tree-selector
                      :form="prow"
                      :item="{prop: 'fieldKey'}"
                      :options="bindParameterOption"
                      :showalllevels="false"
                      :createInit="true"
                      :props="{
                        label: 'shortName',
                        value: 'id',
                        children: 'children',
                        expandTrigger: 'hover',
                        multiple: false,
                        emitPath: false,
                        checkStrictly: true
                      }"
                      :key="'param_row-'+rindex+'-'+pindex+'-fieldKey-'+prow.fieldKey"
                    ></tree-selector>
                  </span>
                  <span v-if="item.type == 'assignment'">
                    <el-cascader
                      v-model="prow.fieldDynamicKey"
                      :options="bindParameterOption"
                      :show-all-levels="false"
                      :props="{ label: 'shortName', value: 'id', children: 'children', expandTrigger: 'hover', multiple: false, emitPath: false, checkStrictly: true }"
                      size="mini"
                    >
                    </el-cascader>
                  </span>
                  <span>
                    <el-select v-model="prow.prop" placeholder="请选择" size="mini">
                      <el-option label="字段" value="field"></el-option>
                      <el-option label="值" value="value"></el-option>
                      <el-option label="公式" value="formula"></el-option>
                      <el-option v-if="item.type != 'assignment'" label="空" value="empty"></el-option>
                    </el-select>
                  </span>
                  <span>
                    <tree-selector
                      v-if="prow.prop == 'field'"
                      :form="prow"
                      :item="{prop: 'value'}"
                      :options="setValueOption"
                      :showalllevels="false"
                      :createInit="true"
                      :props="{
                        label: 'shortName',
                        value: 'id',
                        children: 'children',
                        expandTrigger: 'hover',
                        multiple: false,
                        emitPath: false,
                        checkStrictly: true
                      }"
                      :key="'param_row-'+rindex+'-'+pindex+'-value-'+prow.value"
                    ></tree-selector>
                    <el-input v-if="prow.prop == 'value'" size="mini" v-model="prow.value"></el-input>
                    <el-button v-if="prow.prop == 'formula'" size="mini" type="text" class="set-formula-button" @click="setBindFormula(prow, rindex, pindex, item.prop, 'params')">配置公式</el-button>
                  </span>
                  <b>
                    <span class="delete-icon-button" @click="delBindParamRowHandle(prow, pindex, form[item.prop][rindex].params)">
                      <span class="border-line"></span>
                    </span>
                  </b>
                </p>
                <div class="bottom-button">
                  <div class="button" @click="addBindParamRowHandle(form[item.prop][rindex].params)">
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
          <h4 :class="{'add-one-group-button': true, 'showmargin': (form[item.prop] && form[item.prop].length > 0)}">
            <el-button  @click="bindParameters(item, item.prop)">
              <svg aria-hidden="true">
                <use xlink:href="#jvs-ui-icon-xinjian"></use>
              </svg>
              <span>添加一组</span>
            </el-button>
          </h4>
        </div>
        <!-- 数字 -->
        <div v-if="item.type == 'inputNumber'">
          <el-input v-model="form[item.prop]" size="mini" type="number" class="rule-number-input" @change="numberChange(form, item.prop)"></el-input>
        </div>
        <!-- 数组对象 key / value可选 -->
        <div v-if="['mapKeySelected', 'mapValueSelected'].indexOf(item.type) > -1">
          <div class="map-box">
            <p class="table-head">
              <span>参数名</span>
              <span>参数值</span>
              <i></i>
            </p>
            <div v-if="item.list && item.list.length > 0" class="map-table">
              <p v-for="(it, itx) in item.list" :key="'map-value-selected-item-'+itx">
                <span>
                  <el-input v-if="item.type == 'mapValueSelected'" v-model="it.key" placeholder="请输入参数名" @input="setMapValueSelectd(item)" @blur="setMapValueSelectd(item)"></el-input>
                  <el-select v-if="item.type == 'mapKeySelected'" v-model="it.key" placeholder="请选择" size="mini" @change="setMapValueSelectd(item)">
                    <el-option v-for="(op, opi) in item.options" :key="'map-value-select-option-item-'+itx+'-'+opi" :label="op.label" :value="op.value"></el-option>
                  </el-select>
                </span>
                <span>
                  <el-select v-if="item.type == 'mapValueSelected'" v-model="it.value" placeholder="请选择" size="mini" @change="setMapValueSelectd(item)">
                    <el-option v-for="(op, opi) in item.options" :key="'map-value-select-option-item-'+itx+'-'+opi" :label="op.label" :value="op.value"></el-option>
                  </el-select>
                  <el-input v-if="item.type == 'mapKeySelected'" v-model="it.value" @input="setMapValueSelectd(item)" @blur="setMapValueSelectd(item)"></el-input>
                </span>
                <i>
                  <span class="delete-icon-button" @click="deleteItemKeyOfMap(itx, item)">
                    <span class="border-line"></span>
                  </span>
                </i>
              </p>
            </div>
            <div class="bottom-button">
              <div class="button" @click="addKeyToMap(item)">
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
      <div v-for="(it, ix) in appendList" :key="it.key+'-append'" :slot="it.key+'Append'" style="display:flex;align-items:center;display:flex;align-items:center;">
        <i v-if="$store.getters.userInfo.adminFlag" class="el-icon-setting" style="margin-left:10px;cursor:pointer;" @click="openParamSet(it, ix)"></i>
      </div>
      <div v-for="it in needFunList" :key="it.key+'-append-func'" :slot="it.key+'Label'" style="display:flex;align-items:center;">
        <div class="slot-label-text" :title="it.info">{{it.info}}</div>
        <el-popover trigger="hover">
          <div style="padding: 0 10px;">配置公式后默认以公式为准，表单数据将失效。</div>
          <svg slot="reference" aria-hidden="true" class="add-formula-svg" @click="setFuncHandle(it)">
            <use :xlink:href="`#${(it.formula && it.formulaContent && it.formulaContent.trim().length > 0) ? 'icon-jvs-a-zu10923' : 'icon-jvs-rongqi1'}`"></use>
          </svg>
        </el-popover>
      </div>
      <template slot="formButton">
        <jvs-button type="primary" @click="submitHandle(form)">提交</jvs-button>
        <el-popover
          v-if="info && info.test === true"
          v-model="testEndJsonShow"
          placement="top"
          width="700"
          popper-class="test-result-popper"
          trigger="click">
          <div v-if="result && !testLoading" class="result-box">
            <h4 style="display:flex;justify-content: space-between;align-items: center;">
              <span>执行结果</span>
              <span>
                <span v-if="result.time || result.time == 0" style="font-size: 13px;color:#409EFF;">{{result.time}}ms</span>
                <el-button v-if="result.type == 'number' || result.value" icon="el-icon-document-copy" type="text" @click="copyTestJsonHandle(result.value)" style="margin-left: 10px;cursor: pointer;">复制</el-button>
              </span>
            </h4>
            <div class="result-text">
              <p v-if="result.type == 'number'" style="padding:10px;margin:0;">{{result.value}}</p>
              <json-viewer v-else :value="result.value || ''"></json-viewer>
            </div>
            <h4 v-if="result.parameterIn || result.parameterIn == ''" style="display:flex;justify-content: space-between;align-items: center;">
              <span>执行入参</span>
              <span>
                <el-button icon="el-icon-document-copy" type="text" @click="copyTestJsonHandle(result.parameterIn)" style="margin-left: 10px;cursor: pointer;">复制</el-button>
              </span>
            </h4>
            <div v-if="result.parameterIn || result.parameterIn == ''" class="result-text">
              <div class="rule-param-json-editor">
                <jsonEditor ref="ruleParamInEditor" lang="json" :disabled="true" :code="formatJson(result.parameterIn)" prop="parameterIn" :showPrintMargin="false"></jsonEditor>
              </div>
            </div>
            <h4 v-if="result.parameter || result.parameter == ''" style="display:flex;justify-content: space-between;align-items: center;">
              <span>执行变量</span>
            </h4>
            <div v-if="result.parameter || result.parameter == ''" class="result-text">
              <json-viewer :value="result.parameter || ''"></json-viewer>
            </div>
          </div>
          <el-button slot="reference" @click="testHandle" style="margin-left: 10px;background: #36B452;color: #fff;">测试</el-button>
        </el-popover>
        <el-button v-if="info.customStructure" style="margin-left: 10px;background: #E4EDFF;color: #1E6FFF;" @click="customStructureHandle({ prop: 'customStructureBody' })">结构定义</el-button>
        <el-popover
          v-if="result"
          v-model="testJsonShow"
          placement="left"
          width="700"
          trigger="click">
          <div class="result-box">
            <h4 style="display:flex;justify-content: space-between;align-items: center;">
              <span>执行结果</span>
              <span>
                <span v-if="result.time || result.time == 0" style="font-size: 13px;color:#409EFF;">{{result.time}}ms</span>
                <el-button v-if="result.type == 'number' || result.value" icon="el-icon-document-copy" type="text" @click="copyTestJsonHandle(result.value)" style="margin-left: 10px;cursor: pointer;">复制</el-button>
              </span>
            </h4>
            <div class="result-text">
              <p v-if="result.type == 'number'" style="padding:10px;margin:0;">{{result.value}}</p>
              <json-viewer v-else :value="result.value || ''"></json-viewer>
            </div>
            <h4 v-if="result.parameterIn || result.parameterIn == ''" style="display:flex;justify-content: space-between;align-items: center;">
              <span>执行入参</span>
              <span>
                <el-button icon="el-icon-document-copy" type="text" @click="copyTestJsonHandle(result.parameterIn)" style="margin-left: 10px;cursor: pointer;">复制</el-button>
              </span>
            </h4>
            <div v-if="result.parameterIn || result.parameterIn == ''" class="result-text">
              <div class="rule-param-json-editor">
                <jsonEditor ref="ruleParamInEditor" lang="json" :disabled="true" :code="formatJson(result.parameterIn)" prop="parameterIn" :showPrintMargin="false"></jsonEditor>
              </div>
            </div>
            <h4 v-if="result.parameter || result.parameter == ''" style="display:flex;justify-content: space-between;align-items: center;">
              <span>执行变量</span>
            </h4>
            <div v-if="result.parameter || result.parameter == ''" class="result-text">
              <json-viewer :value="result.parameter || ''"></json-viewer>
            </div>
          </div>
          <el-button slot="reference" size="mini" style="margin-left: 10px;">测试结果</el-button>
        </el-popover>
      </template>
    </jvs-form>
    <div v-if="info && info.test === true && (!formOption.column || formOption.column.length == 0)" class="form-item-btn">
      <el-popover
        v-if="info && info.test === true  && (!formOption.column || formOption.column.length == 0)"
        v-model="testEndJsonShow"
        placement="top"
        width="700"
        popper-class="test-result-popper"
        trigger="click">
        <div v-if="result && !testLoading" class="result-box">
          <h4 style="display:flex;justify-content: space-between;align-items: center;">
            <span>执行结果</span>
            <span>
              <span v-if="result.time || result.time == 0" style="font-size: 13px;color:#409EFF;">{{result.time}}ms</span>
              <el-button v-if="result.type == 'number' || result.value" icon="el-icon-document-copy" type="text" @click="copyTestJsonHandle(result.value)" style="margin-left: 10px;cursor: pointer;">复制</el-button>
            </span>
          </h4>
          <div class="result-text">
            <p v-if="result.type == 'number'" style="padding:10px;margin:0;">{{result.value}}</p>
            <json-viewer v-else :value="result.value || ''"></json-viewer>
          </div>
          <h4 v-if="result.parameterIn || result.parameterIn == ''" style="display:flex;justify-content: space-between;align-items: center;">
            <span>执行入参</span>
            <span>
              <el-button icon="el-icon-document-copy" type="text" @click="copyTestJsonHandle(result.parameterIn)" style="margin-left: 10px;cursor: pointer;">复制</el-button>
            </span>
          </h4>
          <div v-if="result.parameterIn || result.parameterIn == ''" class="result-text">
            <div class="rule-param-json-editor">
              <jsonEditor ref="ruleParamInEditor" lang="json" :disabled="true" :code="formatJson(result.parameterIn)" prop="parameterIn" :showPrintMargin="false"></jsonEditor>
            </div>
          </div>
          <h4 v-if="result.parameter || result.parameter == ''" style="display:flex;justify-content: space-between;align-items: center;">
            <span>执行变量</span>
          </h4>
          <div v-if="result.parameter || result.parameter == ''" class="result-text">
            <json-viewer :value="result.parameter || ''"></json-viewer>
          </div>
        </div>
        <el-button slot="reference" @click="testHandle" style="margin-left: 10px;background: #36B452;color: #fff;">测试</el-button>
      </el-popover>
      <el-button v-if="info && info.test === true && (!formOption.column || formOption.column.length == 0) && info.customStructure" type="primary" size="mini" style="margin-left: 10px;background: #E4EDFF;color: #1E6FFF;" @click="customStructureHandle({ prop: 'customStructureBody' })">结构定义</el-button>
      <el-popover
        v-if="result"
        v-model="testJsonShow"
        placement="left"
        width="700"
        trigger="click">
        <div class="result-box">
          <h4 style="display:flex;justify-content: space-between;align-items: center;">
            <span>执行结果</span>
            <span>
              <span v-if="result.time || result.time == 0" style="font-size: 13px;color:#409EFF;">{{result.time}}ms</span>
              <el-button v-if="result.type == 'number' || result.value" icon="el-icon-document-copy" type="text" @click="copyTestJsonHandle(result.value)" style="margin-left: 10px;cursor: pointer;">复制</el-button>
            </span>
          </h4>
          <div class="result-text">
            <p v-if="result.type == 'number'" style="padding:10px;margin:0;">{{result.value}}</p>
            <json-viewer v-else :value="result.value || ''"></json-viewer>
          </div>
          <h4 v-if="result.parameterIn || result.parameterIn == ''" style="display:flex;justify-content: space-between;align-items: center;">
            <span>执行入参</span>
            <span>
              <el-button icon="el-icon-document-copy" type="text" @click="copyTestJsonHandle(result.parameterIn)" style="margin-left: 10px;cursor: pointer;">复制</el-button>
            </span>
          </h4>
          <div v-if="result.parameterIn || result.parameterIn == ''" class="result-text">
            <div class="rule-param-json-editor">
              <jsonEditor ref="ruleParamInEditor" lang="json" :disabled="true" :code="formatJson(result.parameterIn)" prop="parameterIn" :showPrintMargin="false"></jsonEditor>
            </div>
          </div>
          <h4 v-if="result.parameter || result.parameter == ''" style="display:flex;justify-content: space-between;align-items: center;">
            <span>执行变量</span>
          </h4>
          <div v-if="result.parameter || result.parameter == ''" class="result-text">
            <json-viewer :value="result.parameter || ''"></json-viewer>
          </div>
        </div>
        <el-button slot="reference" size="mini" style="margin-left: 10px;">测试结果</el-button>
      </el-popover>
    </div>
    <!-- 参数设置 -->
    <el-dialog
      title="参数配置"
      :visible.sync="paramVisible"
      append-to-body
      :close-on-click-modal="false"
      :before-close="paramClose">
      <div v-if="paramVisible">
        <div class="map-box">
          <p v-if="currentParam && currentParam.customOption && currentParam.customOption.length > 0" class="table-head">
            <span v-for="cp in currentParam.customOption" :key="cp.name+'_'+cp.filed">{{cp.name}}</span>
            <i></i>
          </p>
          <div v-if="currentParam && currentParam.customOption && currentParam.customOption.length > 0" class="map-table">
            <p v-for="(row, rindex) in currentParam.options" :key="'param_row'+rindex">
              <span v-for="(it, iindex) in currentParam.customOption" :key="'row'+rindex+'_'+it.filed+'_column_'+iindex">
                <el-input v-model="row[it.filed]" size="mini" :show-password="it.type == 'password'"></el-input>
              </span>
              <i>
                <span class="delete-icon-button" @click="delRowHandle(row, rindex, currentParam.options)">
                  <span class="border-line"></span>
                </span>
              </i>
            </p>
          </div>
          <div class="bottom-button">
            <div class="button" @click="addRowHandle(currentParam.options)">
              <div class="icon">
                <svg aria-hidden="true">
                  <use xlink:href="#jvs-ui-icon-xinjian"></use>
                </svg>
              </div>
              <span>新增一行</span>
            </div>
          </div>
        </div>
        <el-row style="margin-top: 16px;display: flex;justify-content: center;align-items: center;">
          <jvs-button size="mini" type="primary" @click="saveHandle(currentParam)">保存</jvs-button>
          <jvs-button size="mini" @click="paramClose">取消</jvs-button>
        </el-row>
      </div>
    </el-dialog>
    <!-- 定义结构 -->
    <el-dialog
      title="结构定义"
      :visible.sync="customStructureShow"
      append-to-body
      :close-on-click-modal="false"
      :before-close="customStructureClose">
      <div v-if="customStructureShow" style="position: relative;" class="customStructure-box">
        <div style="display: flex; align-items: center;justify-content: space-between;">
          <el-alert title="" type="warning" :closable="false" style="margin-bottom: 10px;width: 75%;">
            <div slot="title">
              <div>数据数据与结构定义不一致，以结构定义为准</div>
              <div style="margin-top: 10px;">整体执行时会自动结构定义</div>
            </div>
          </el-alert>
          <span>
            <el-button v-if="(result && result.value && typeof result.value == 'object') || (testData && typeof testData == 'object')" size="mini" style="margin-left: 20px;" @click="useTestResult">引用测试</el-button>
            <el-button size="mini" style="margin-left: 20px;" @click="clearAll">清除所有</el-button>
          </span>
        </div>
        <el-table
          :data="form[currentParam.prop]"
          style="width: 100%;"
          row-key="key"
          border
          default-expand-all
          v-loading="customStructureLoading"
          :tree-props="{children: 'children', hasChildren: 'hasChildren'}">
          <el-table-column
            prop="name"
            label="参数名">
            <template slot-scope="scope">
              <el-input v-model="scope.row.name" size="mini"></el-input>
            </template>
          </el-table-column>
          <el-table-column
            prop="jvsParamType"
            label="参数类型">
            <template slot-scope="scope">
              <el-select v-model="scope.row.jvsParamType" placeholder="请选择" size="mini" @change="typeChange(scope.row.jvsParamType, scope.row)">
                <el-option
                  v-for="item in jvsParamTypes"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
                </el-option>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column
            prop="info"
            label="参数说明">
            <template slot-scope="scope">
              <el-input v-model="scope.row.info" size="mini"></el-input>
            </template>
          </el-table-column>
          <el-table-column label="操作">
            <template slot-scope="scope">
              <el-button v-if="['object', 'array'].indexOf(scope.row.jvsParamType) > -1" size="mini" type="text" @click="addCustomStructureRowHandle(scope.row, scope.$index, 'children')">添加</el-button>
              <el-button size="mini" type="text" @click="delTableRowHandle(scope.row, scope.$index, form[currentParam.prop])">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <jvs-button size="mini" style="margin-top: 5px;" @click="addCustomStructureRowHandle(form[currentParam.prop])">添加</jvs-button>
        <el-row style="margin-top:5px;display:flex;justify-content:center;align-items:center;">
          <jvs-button size="mini" type="primary" @click="customStructureSave">保存</jvs-button>
          <jvs-button size="mini" @click="customStructureClose">取消</jvs-button>
        </el-row>
      </div>
    </el-dialog>
    <!-- 图片选择 -->
    <imageSelect
      ref="logoSelect"
      title="选择图片"
      :dialogVisible="chooseAble"
      :paramInfo="{'module': 'application', 'label': '默认'}"
      @handleConfirm="handleConfirm"
      @handleClose="chooseAble = false;"
    ></imageSelect>
  </div>
</template>
<script>
import E from 'wangeditor'
import {testFunction, saveCustomOption, getFuncLink, jvsParamTypeGet, jvsParamTypePost, getRunBind, getSelectOptions} from '../../api/rule'
import {isMobile} from '@/util/validate'
import { encryption, decryption } from "@/util/util";
import {deCodeKey} from "@/const/const"
import eventBus from "../../utils/eventBus";
import loadingImg from '@/styles/loading.gif'
import imageSelect from '@/components/basic-assembly/ImageSelect'
import codeEditor from './coder'
import treeSelector from '@/components/basic-assembly/treeSelector'
import jsonEditor from '@/components/basic-assembly/jsonEditor'
export default {
  name: 'setting-html',
  components: { imageSelect, codeEditor, treeSelector, jsonEditor },
  props: {
    info: {
      type: Object
    },
    ruleInfo: Object,
    activeCanvas: String,
    testData: [Object, String]
  },
  data () {
    return {
      loadingImg: loadingImg,
      form: {}, // 表单对象
      soltList: [], // 需要自定义的字段
      appendList: [], // 需要加后置插槽的字段
      formOption: {}, // 表单项
      editors: [], // 富文本
      codeEdit: [], // 代码编译器
      result: null, // 测试结果
      testEndJsonShow: false, // 测试完成是否显示
      testJsonShow: false, // 测试结果是否显示
      open: false,
      hideOpen: false,
      requiredList: [],
      validatePhone: (rule, value, callback) => {
        if (isMobile(value)) {
          callback()
        } else {
          callback(new Error('手机号格式错误'))
        }
      },
      paramVisible: false, // 参数配置弹框
      currentParam: null,
      currentPIndex: -1,
      jvsAppId: '',
      ruleId: '',
      testLoading: false,
      needFunList: [],
      linkOption: {}, // 关联关系
      modelList: [],
      oldForm: {},
      customStructureShow: false,
      jvsParamTypes: [],
      imgProp: '', // 图片选择
      chooseAble: false,
      bindParameterOption: [],
      customStructureLoading: false,
      setValueOption: [], // 赋值字段选项树
      beforeSlotList: [], // 自定义label，过长且可配置公式
      openIndex: {}
    }
  },
  watch: {
    info: {
      handler(newVal, oldVal) {
        if((newVal && newVal != oldVal && (newVal.id != oldVal.id)) || !oldVal) {
          this.formOption = {}
          this.soltList = []
          this.appendList = []
          this.init()
        }
      },
      deep: true
    },
    form: {
      handler(newVal, oldVal) {
        if(newVal && this.linkOption) {
          for(let k in this.linkOption) {
            if(this.linkOption[k] && this.linkOption[k].type && this.linkOption[k].key) {
              for(let i in this.linkOption[k].key) {
                let kkey = this.linkOption[k].key[i]
                if(newVal[k] && newVal[k] != oldVal[k]) {
                  this.getLinkFuncData(this.linkOption[k].type, newVal[k], kkey, false)
                }
              }
            }
          }
        }
      },
      deep: true
    }
  },
  created () {
    if(this.$route.query) {
      if(this.$route.query.jvsAppId) {
        this.jvsAppId = this.$route.query.jvsAppId
      }
      if(this.$route.query.id) {
        this.ruleId = this.$route.query.id
      }
    }
    this.getFiledOption()
    this.init()
    this.getJvsParamType()
  },
  mounted () {
    this.initEditor()
  },
  methods: {
    init () {
      this.form = {}
      this.oldForm = {}
      this.linkOption = {}
      this.requiredList = []
      this.editors = []
      this.codeEdit = []
      this.needFunList = []
      let temp = {
        submitBtn: false,
        emptyBtn: false,
        cancal: false,
        formAlign: 'top',
        size: "mini",
        column: []
      }
      // console.log(this.info)
      for(let i in this.info.parameters) {
        if(this.info.parameters[i].customOptionValue){
          if(typeof this.info.parameters[i].options == 'string') {
            let tcvDec = decryption({
              data: {data: this.info.parameters[i].options},
              key: deCodeKey,
              param: ["data"]
            })
            this.info.parameters[i].options = JSON.parse(tcvDec.data)
          }
          if(this.info.parameters[i].defaultValue && typeof this.info.parameters[i].defaultValue == 'string') {
            let tcvDec = decryption({
              data: {data: this.info.parameters[i].defaultValue},
              key: deCodeKey,
              param: ["data"]
            })
            this.info.parameters[i].defaultValue = JSON.parse(tcvDec.data)
          }
        }
        let obj = {
          label: this.info.parameters[i].info,
          prop: this.info.parameters[i].key,
          placeholder: this.info.parameters[i].info,
          index: i
        }
        if(this.info && this.info.body && this.info.body[obj.prop] !== undefined) {
          this.$set(this.form, obj.prop, this.info.body[obj.prop])
          this.$set(this.oldForm, obj.prop, this.info.body[obj.prop])
          if(this.info.body[obj.prop+'_1'] !== undefined) {
            this.$set(this.form, obj.prop+'_1', this.info.body[obj.prop+'_1'])
            this.$set(this.oldForm, obj.prop+'_1', this.info.body[obj.prop+'_1'])
          }
        }
        if(this.info.parameters[i].necessity){
          // 'map'
          if(['list', 'html', 'code', 'sql'].indexOf(this.info.parameters[i].inputType) > -1) {
            obj.required = this.info.parameters[i].necessity
            this.requiredList.push(obj.prop)
            let msgstr = '请输入'
            if(this.info.parameters[i].inputType == 'list') {
              msgstr = '请添加'
            }
            obj.rules = [
              { required: this.info.parameters[i].necessity, message: (msgstr+obj.label), trigger: ['blur', 'change'] },
            ]
          }else{
            let msgstr = '请输入'
            if(['map', 'listMap', 'dataModelField', 'dataModelFilterField', 'dataModelOrderField'].indexOf(this.info.parameters[i].inputType) > -1) {
              msgstr = '请添加'
            }
            if(['treeSelected', 'treeSelectedChild', 'multipleSelected', 'imageSelect', 'userList'].indexOf(this.info.parameters[i].inputType) > -1) {
              msgstr = '请选择'
            }
            obj.rules = [
              { required: this.info.parameters[i].necessity, message: (msgstr+obj.label), trigger: ['blur', 'change'] },
            ]
          }
        }
        if(this.info.parameters[i].options) {
          obj.dicData = this.info.parameters[i].options
          obj.tips = {
            text: '',
            position: 'bottom'
          }
        }
        // 手机号
        if(obj.prop == 'phone') {
          if(!obj.rules) {
            obj.rules = []
          }
          obj.rules.push({ validator: this.validatePhone, trigger: 'blur' })
        }
        // 显示explain
        if(this.info.parameters[i].explain) {
          obj.beforeSlot = true
          this.beforeSlotList.push({prop: obj.prop, explain: this.info.parameters[i].explain})
        }
        switch(this.info.parameters[i].inputType) {
          case 'text': obj.type="input";break;
          case 'input': obj.type="input";break;
          case 'onOff': obj.type="switch";break;
          case 'user': obj.type ='user';break;
          case 'dept': obj.type ='department';break;
          case 'role': obj.type ='role';break;
          case 'group': obj.type ='group';break;
          case 'job': obj.type ='job';break;
          case 'userList': obj.type = 'user';obj.multiple = true;break;
          case 'file':
          case 'files':
            if(!this.form[obj.prop]) {
              this.$set(this.form, obj.prop, [])
            }
            obj.type = 'fileUpload';
            obj.fileList = this.form[obj.prop];
            if(this.info.parameters[i].inputType == 'file') {
              obj.limit = 1
            };
            break;
          case 'selected':
          case 'multipleSelected':
            obj.type="select";
            obj.matchClear = !(this.info.skipRefresh === false)
            if(this.info.parameters[i].customOptionValue) {
              let dicTemp = []
              for(let p  in this.info.parameters[i].options) {
                let tp = {
                  data: JSON.stringify(this.info.parameters[i].options[p])
                }
                let temp = encryption({
                  data: tp,
                  key: deCodeKey,
                  param: ["data"]
                });
                dicTemp.push({
                  label: this.info.parameters[i].options[p].name,
                  value: temp.data
                })
              }
              obj.dicData = dicTemp
            }
            // if(this.info.parameters[i].key == "templateCode") {
            //   obj.props = {
            //     label: 'name',
            //     value: 'code'
            //   }
            // }
            if(this.info.parameters[i].inputType == "multipleSelected") {
              obj.multiple = true;
            }
            break;
          case 'number':
            obj.type="inputNumber";
            obj.formSlot=true;
            this.soltList.push({label: obj.label, prop: obj.prop, type: obj.type, required: obj.required, supportFunction: this.info.parameters[i].supportFunction, formula: (this.info.parameters[i].formula || '')});
            break;
          case 'list':
            obj.type="list";
            obj.formSlot=true;
            let tl = []
            if(this.form[obj.prop]) {
              tl = this.form[obj.prop]
            }
            this.soltList.push({label: obj.label, prop: obj.prop, type: obj.type, list: tl, required: obj.required, supportFunction: this.info.parameters[i].supportFunction, formula: (this.info.parameters[i].formula || '')});
            break;
          case 'filterMap':
          case 'map':
            obj.type=this.info.parameters[i].inputType; // "map";
            obj.formSlot=true;
            let tp = this.form[obj.prop] // this.formatMap(this.form[obj.prop]);
            this.soltList.push({label: obj.label, prop: obj.prop, type: obj.type, list: tp, required: obj.required, supportFunction: this.info.parameters[i].supportFunction, formula: (this.info.parameters[i].formula || '')});
            break;
          case 'longtext':
            obj.type="textarea";
            // obj.formSlot=true;
            // this.soltList.push({prop: obj.prop, type: obj.type});
            // this.editors.push(obj.prop)
            break;
          case 'html':
            obj.type="longtext";
            obj.formSlot=true;
            this.soltList.push({label: obj.label, prop: obj.prop, type: obj.type, required: obj.required, supportFunction: this.info.parameters[i].supportFunction, formula: (this.info.parameters[i].formula || '')});
            this.editors.push(obj.prop)
            break;
          case 'code':
            obj.type="code";
            obj.formSlot=true;
            this.soltList.push({label: obj.label, prop: obj.prop, type: obj.type, required: obj.required, supportFunction: this.info.parameters[i].supportFunction, formula: (this.info.parameters[i].formula || '')});
            this.codeEdit.push(obj)
            break;
          case 'sql':
            obj.type="sql";
            obj.formSlot=true;
            this.soltList.push({label: obj.label, prop: obj.prop, type: obj.type, required: obj.required, supportFunction: this.info.parameters[i].supportFunction, formula: (this.info.parameters[i].formula || '')});
            this.codeEdit.push(obj)
            break;
          case 'json':
            obj.type="json";
            obj.formSlot=true;
            this.soltList.push({label: obj.label, prop: obj.prop, type: obj.type, required: obj.required, supportFunction: this.info.parameters[i].supportFunction, formula: (this.info.parameters[i].formula || '')});
            this.codeEdit.push(obj)
            break;
          case 'listMap':
            obj.type="listMap";
            obj.formSlot=true;
            let templ = [];
            if(this.form[obj.prop]) {
              templ = this.form[obj.prop]
            }
            this.soltList.push({label: obj.label, prop: obj.prop, type: obj.type, list: templ,  required: obj.required, supportFunction: this.info.parameters[i].supportFunction, formula: (this.info.parameters[i].formula || '')});
            break;
          case 'dateTime':
            obj.type = 'datePicker';
            obj.datetype = 'datetime';
            break;
          case 'dateFrame':
            obj.type = 'datePicker';
            obj.datetype = 'datetimerange';
            break;
          case 'treeSelected':
          case 'treeSelectedChild':
            obj.formSlot=true;
            obj.type = this.info.parameters[i].inputType;
            obj.options = this.info.parameters[i].options;
            this.soltList.push({label: obj.label, prop: obj.prop, type: obj.type, options: obj.options, required: obj.required, supportFunction: this.info.parameters[i].supportFunction, formula: (this.info.parameters[i].formula || '')});
            if(this.form[obj.prop]) {
              let str = this.form[obj.prop]
              this.$set(this.form, obj.prop, str.split(','))
              this.$set(this.oldForm, obj.prop, str.split(','))
            }
            break;
          case 'dataModelField':
          case 'dataModelFilterField':
          case 'dataModelOrderField':
            obj.formSlot=true;
            obj.type = this.info.parameters[i].inputType;
            this.soltList.push({label: obj.label, prop: obj.prop, type: obj.type, options: this.info.parameters[i].options ? this.info.parameters[i].options : [], required: obj.required, supportFunction: this.info.parameters[i].supportFunction, formula: (this.info.parameters[i].formula || '')});
            break;
          case 'flowNode':
            obj.formSlot = true;
            obj.type = this.info.parameters[i].inputType;
            this.soltList.push({label: obj.label, prop: obj.prop, type: obj.type, options: this.info.parameters[i].options ? this.info.parameters[i].options : [], required: obj.required, supportFunction: this.info.parameters[i].supportFunction, formula: (this.info.parameters[i].formula || '')});
            break;
          case 'imageSelect':
            obj.formSlot = true;
            obj.type = this.info.parameters[i].inputType;
            this.soltList.push({label: obj.label, prop: obj.prop, type: obj.type, required: obj.required, supportFunction: this.info.parameters[i].supportFunction, formula: (this.info.parameters[i].formula || '')});
            break;
          case 'bind':
          case 'assignment':
            obj.formSlot = true;
            obj.type = this.info.parameters[i].inputType;
            this.soltList.push({label: obj.label, prop: obj.prop, type: obj.type, required: obj.required, supportFunction: this.info.parameters[i].supportFunction, formula: (this.info.parameters[i].formula || '')});
            break;
          case 'mapKeySelected':
          case 'mapValueSelected':
            obj.formSlot = true;
            obj.type = this.info.parameters[i].inputType;
            let mvsTemp = [];
            if(this.form[obj.prop]) {
              for(let k in this.form[obj.prop]) {
                mvsTemp.push({
                  key: k,
                  value: this.form[obj.prop][k]
                })
              }
            }
            this.soltList.push({label: obj.label, prop: obj.prop, type: obj.type, list: mvsTemp, options: this.info.parameters[i].options ? this.info.parameters[i].options : [], required: obj.required, supportFunction: false});
            break;
          default: obj.type="input";break;
        }
        // 动态选择项
        if(this.info.skipRefresh === false && this.info.parameters[i].options) {
          getSelectOptions(this.jvsAppId, this.info.functionName, this.info.parameters[i].key).then(res => {
            if(res.data && res.data.code == 0) {
              let options = res.data.data
              if(this.info.parameters[i].customOptionValue) {
                let dicTemp = []
                for(let p in options) {
                  let tp = {
                    data: JSON.stringify(options[p])
                  }
                  let temp = encryption({
                    data: tp,
                    key: deCodeKey,
                    param: ["data"]
                  })
                  dicTemp.push({
                    label: options[p].name,
                    value: temp.data
                  })
                }
                obj.dicData = dicTemp
              }else {
                obj.dicData = options || []
              }
            }
          })
        }
        if(this.info.parameters[i].selectedClassLink && this.info.parameters[i].linkField) {
          this.$set(this.linkOption, this.info.parameters[i].key, {
            type: this.info.parameters[i].selectedClassLink,
            key: this.info.parameters[i].linkField
          });
        }
        // 默认值
        if(this.info.parameters[i].defaultValue && !this.form[obj.prop] && this.form[obj.prop] !== false && this.form[obj.prop] !== 0) {
          if(typeof this.info.parameters[i].defaultValue == 'object') {
            let tdv = {
              data: JSON.stringify(this.info.parameters[i].defaultValue)
            }
            let tdvt = encryption({
              data: tdv,
              key: deCodeKey,
              param: ["data"]
            })
            if(this.info.parameters[i].inputType == 'multipleSelected') {
              this.$set(this.form, obj.prop, [tdvt.data])
              this.$set(this.oldForm, obj.prop, [tdvt.data])
            }else{
              this.$set(this.form, obj.prop, tdvt.data)
              this.$set(this.oldForm, obj.prop, tdvt.data)
            }
          }
          if(['treeSelected', 'treeSelectedChild'].indexOf(this.info.parameters[i].inputType) > -1) {
            this.$set(this.form, obj.prop, this.info.parameters[i].defaultValue.split(','))
            this.$set(this.oldForm, obj.prop, this.info.parameters[i].defaultValue.split(','))
          }
          if(['number', 'selected'].indexOf(this.info.parameters[i].inputType) > -1 || (this.info.parameters[i].selectedClassLink && this.info.parameters[i].linkField)) {
            this.$set(this.form, obj.prop, this.info.parameters[i].defaultValue)
            this.$set(this.oldForm, obj.prop, this.info.parameters[i].defaultValue)
          }
        }
        // 设置
        if(this.info.parameters[i].customOptionValue) {
          obj.appendSlot = true
          this.appendList.push({...this.info.parameters[i], index: Number.parseInt(i)})
        }
        // 函数
        if(this.info.parameters[i].supportFunction) {
          obj.labelSlot = true
          this.needFunList.push(this.info.parameters[i])
        }
        if(this.info.parameters[i].customOptionValue && typeof this.info.parameters[i].defaultValue == 'string') {
          let tcv = {
            data: this.info.parameters[i].defaultValue
          }
          let tcvDec = encryption({
            data: tcv,
            key: decryption,
            param: ["data"]
          })
          obj.defaultValue = tcvDec.data
          this.$set(this.oldForm, obj.prop, tcvDec.data)
        }else{
          obj.defaultValue = this.info.parameters[i].defaultValue
          this.$set(this.oldForm, obj.prop, this.info.parameters[i].defaultValue)
        }
        if(obj.type == 'select') {
          obj.filterable = true
        }
        temp.column.push(obj)
        if(['dataModelField', 'dataModelFilterField', 'flowNode', 'dataModelOrderField'].indexOf(this.info.parameters[i].inputType) > -1) {
          if(this.info && this.info.body && this.info.body[obj.prop] !== undefined) {
            this.$set(this.form, obj.prop, this.info.body[obj.prop])
            this.$set(this.oldForm, obj.prop, this.info.body[obj.prop])
          }
          if(!this.form[this.info.parameters[i].key]) {
            let topform = []
            if(this.info.parameters[i].options) {
              if(this.info.parameters[i].options.length < 31) {
                if(this.info.parameters[i].inputType == 'dataModelField') {
                  for(let ti in this.info.parameters[i].options) {
                    topform.push({
                      fieldKey: this.info.parameters[i].options[ti].value,
                      prop: 'value',
                      value: ''
                    })
                  }
                }
                if(this.info.parameters[i].inputType == 'dataModelFilterField'){
                  for(let ti in this.info.parameters[i].options) {
                    topform.push({
                      fieldKey: this.info.parameters[i].options[ti].value,
                      enabledQueryTypes: (this.info.parameters[i].options[ti].extend && this.info.parameters[i].options[ti].extend.enabledQueryTypes && this.info.parameters[i].options[ti].extend.enabledQueryTypes.length > 0) ? this.info.parameters[i].options[ti].extend.enabledQueryTypes[0].value : '',
                      prop: 'value',
                      value: ''
                    })
                  }
                }
              }
              if(this.info.parameters[i].inputType == 'flowNode'){
                for(let ti in this.info.parameters[i].options) {
                  topform.push({
                    fieldKey: this.info.parameters[i].options[ti].value,
                    prop: 'formula',
                    formula: '',
                    formulaContent: ''
                  })
                }
              }
            }
            this.$set(this.form, this.info.parameters[i].key, topform)
          }
        }
      }
      this.oldForm = JSON.parse(JSON.stringify(this.form))
      this.formOption = temp
      this.$nextTick(()=>{
        this.getTips()
      })
      this.$forceUpdate()
    },
    getFiledOption () {
      let obj = {
        nodeId: this.info.id,
      }
      if(this.activeCanvas != 'main') {
        obj.canvasId = this.activeCanvas
      }
      getRunBind(this.jvsAppId, this.ruleInfo.id, {extendJson: JSON.stringify(obj)}).then(res => {
        if(res.data && res.data.code == 0) {
          this.bindParameterOption = res.data.data
          this.setValueOption = res.data.data
          // console.log(this.bindParameterOption)
        }
      });
    },
    // 新增行
    addLine (item, type) {
      if(!item.list) {
        this.$set(item, 'list', [])
      }
      if(type == 'string') {
        item.list.push('')
        if(item.list.length > 0) {
          this.$refs.paramForm.$refs.ruleForm.clearValidate(item.prop)
        }
      }
      if(type == 'obj') {
        item.list.push({key: '', value: '', type: 'value'})
        if(item.list.length > 0) {
          this.$refs.paramForm.$refs.ruleForm.clearValidate(item.prop)
        }
      }
      this.$forceUpdate()
    },
    // 删除list行
    delLine (index, item) {
      item.list.splice(index, 1)
      this.listChangeHandle(item)
      this.$forceUpdate()
    },
    // 设置list的值
    listChangeHandle (item) {
      this.$set(this.form, item.prop, JSON.parse(JSON.stringify(item.list)))
      this.$refs.paramForm.$refs.ruleForm.model[item.prop] = JSON.parse(JSON.stringify(item.list))
      this.$forceUpdate()
    },
    // 格式化map
    formatMap (obj) {
      if(!obj) {
        obj = {}
      }
      let tempList = []
      let keys = Object.keys(obj)
      for(let i in keys) {
        let tb = {
          key: keys[i],
          value: obj[keys[i]]
        }
        tempList.push(tb)
      }
      return tempList
    },
    // 删除map项
    delItem (index, item) {
      // if(item.list[index].formula) {
      //   // 删除公式
      //   deleteExec(this.ruleInfo.id, item.list[index].formula).then(res => {
      //     if(res.data && res.data.code == 0) {
      //       item.list.splice(index, 1)
      //       this.setValueOfMap(item, item.prop)
      //       this.$forceUpdate()
      //     }
      //   })
      // }else{
      //   item.list.splice(index, 1)
      //   this.setValueOfMap(item, item.prop)
      //   this.$forceUpdate()
      // }
      item.list.splice(index, 1)
      this.setValueOfMap(item, item.prop)
      this.$forceUpdate()
    },
    // 设置map的值
    setValueOfMap (item, prop) {
      this.$set(this.form, prop, [])
      for(let i in item.list) {
        // this.form[prop][item.list[i].key] = item.list[i].value
        let tob = {
          fieldKey: item.list[i].fieldKey,
          value: item.list[i].value
        }
        if(item.list[i].prop) {
          tob.prop = item.list[i].prop
        }
        if(item.list[i].formula) {
          tob.formula = item.list[i].formula
        }
        if(item.list[i].formulaContent) {
          tob.formulaContent = item.list[i].formulaContent
        }
        if(item.list[i].enabledQueryTypes) {
          tob.enabledQueryTypes = item.list[i].enabledQueryTypes
        }
        this.form[prop].push(tob)
      }
      if(item.list.length > 0) {
        this.$refs.paramForm.$refs.ruleForm.clearValidate(prop)
      }else{
        this.$set(this.form, prop, null)
        this.$refs.paramForm.$refs.ruleForm.validateField(prop)
      }
      this.$forceUpdate()
    },
    // 初始化富文本
    initEditor () {
      for(let i in this.editors) {
        let _this = this
        let editor = new E('#'+this.editors[i])
        editor.config.uploadImgShowBase64 = true
        editor.config.menus = [
          'head',
          'bold',
          'fontSize',
          'fontName',
          'italic',
          'underline',
          'strikeThrough',
          'indent',
          'lineHeight',
          'foreColor',
          'backColor',
          'link',
          'list',
          'justify',
          'quote',
          'emoticon',
          'image',
          'table',
          'code',
          'splitLine',
          'undo',
          'redo',
        ]
        editor.config.onblur = function (newHtml) {
          _this.$set(_this.form, _this.editors[i], newHtml)
          _this.$refs.paramForm.$refs.ruleForm.model[_this.editors[i]] = newHtml
          if(newHtml.length > 0 && _this.$refs.paramForm) {
            _this.$refs.paramForm.$refs.ruleForm.clearValidate(_this.editors[i])
          }
        }
        editor.config.onchange = function (newHtml) {
          _this.$set(_this.form, _this.editors[i], newHtml)
          _this.$refs.paramForm.$refs.ruleForm.model[_this.editors[i]] = newHtml
          if(newHtml.length > 0 && _this.$refs.paramForm) {
            _this.$refs.paramForm.$refs.ruleForm.clearValidate(_this.editors[i])
          }
        }
        editor.create()
        editor.txt.html(this.form[this.editors[i]])
      }
    },
    codeEditorChange (con, prop) {
      if(con == 'error') {
        this.$set(this.form, prop, '')
      }else{
        this.$set(this.form, prop, con)
      }
      this.$forceUpdate()
    },
    numberChange (form, prop) {
      this.$set(form, prop, Number(form[prop]))
    },
    // 表单变化
    formChange (form, ref) {
      // this.form = form
      if(this.linkOption) {
        for(let k in this.linkOption) {
          if(this.linkOption[k] && this.linkOption[k].type && this.linkOption[k].key) {
            for(let i in this.linkOption[k].key) {
              let kkey = this.linkOption[k].key[i]
              if(this.form[k]) {
                if(this.oldForm && this.form[k] != this.oldForm[k]) {
                  this.$set(this.form, kkey, [])
                  this.getLinkFuncData(this.linkOption[k].type, this.$refs.paramForm.$refs.ruleForm.model[k] || this.form[k], kkey, true)
                }
              }
            }
          }
        }
      }
      this.oldForm = JSON.parse(JSON.stringify(this.form))
      if(ref && this.$refs[ref]) {
        let node = null
        let list = []
        this.$set(this.formOption.column[1], 'display', false)
        if(this.$refs[ref].length >0) {
          list = this.$refs[ref][0].getCheckedNodes()
        }else{
          list = this.$refs[ref].getCheckedNodes()
        }
        if(list && list.length > 0) {
          node = list[0]
          if(node.data && node.data.type == 'node') {
            this.$set(this.formOption.column[1], 'display', true)
          }
        }
      }
      this.getTips()
      this.$forceUpdate()
      // console.log(this.form)
    },
    // 提交
    submitHandle (form, isClose) {
      this.$refs.paramForm.$refs.ruleForm.validate( (valid, errItems) => {
        let bool = false
        if(valid) {
          bool = true
        }else{
          let vb = true
          for(let i in this.info.parameters) {
            if(this.info.parameters[i].formula && this.info.parameters[i].formulaContent) {
              this.$refs.paramForm.$refs.ruleForm.clearValidate(this.info.parameters[i].key)
            }else{
              if(this.form[this.info.parameters[i].key] === null || this.form[this.info.parameters[i].key] === undefined) {
                if(this.info.parameters[i].defaultValue != null && this.info.parameters[i].defaultValue != undefined) {
                  this.$set(this.form, this.info.parameters[i].key, this.info.parameters[i].defaultValue)
                }else{
                  vb = false
                }
              }
              if(this.info.parameters[i].necessity && !this.form[this.info.parameters[i].key] && this.form[this.info.parameters[i].key]!== false) {
                this.$set(this.form, this.info.parameters[i].key, this.info.parameters[i].defaultValue)
              }
            }
          }
          if(vb) {
            bool = true
          }
        }
        if(bool) {
          let baseData = {}
          if(this.$refs.paramForm.$refs.ruleForm.model) {
            baseData = JSON.parse(JSON.stringify(this.$refs.paramForm.$refs.ruleForm.model))
          }
          if(form) {
            for(let i in this.info.parameters) {
              if(['dataModelField', 'dataModelFilterField', 'imageSelect', 'dataModelOrderField', 'mapKeySelected', 'mapValueSelected', 'map', 'listMap'].indexOf(this.info.parameters[i].inputType) > -1) {
                baseData[this.info.parameters[i].key] = form[this.info.parameters[i].key]
              }
            }
            this.$set(this, 'form', baseData)
          }
          if(form) {
            for(let i in this.codeEdit) {
              this.form[this.codeEdit[i].prop] = form[this.codeEdit[i].prop]
              if(this.form[this.codeEdit[i].prop] == 'error') {
                this.form[this.codeEdit[i].prop] = ''
              }
            }
          }
          for(let i in this.requiredList) {
            if(!this.form[this.requiredList[i]] || this.form[this.requiredList[i]].length == 0) {
              this.info.parameters.filter(pit => {
                if(pit.key == this.requiredList[i] && !this.info.parameters[i].formula) {
                  return false
                }
              })
            }
          }
          let formDataSub = JSON.parse(JSON.stringify(this.form))
          this.formOption.column.filter(item => {
            if(['treeSelected', 'treeSelectedChild'].indexOf(item.type) > -1 && this.form[item.prop]) {
              this.$set(formDataSub, item.prop, this.form[item.prop].join(','))
            }
          })
          if(isClose === false) {
            this.$set(this.info, 'body', formDataSub)
          }else{
            this.$emit('saveSetting', formDataSub, this.info)
          }
          // console.log(formDataSub)
        }
      })
    },
    // 返回
    closeHandle () {
      this.$emit('close', true)
    },
    // 测试
    testHandle () {
      let validate = false
      let vres = {} // 校验失败的字段集状态，默认false
      this.$refs.paramForm.$refs.ruleForm.validate( (bool, prop) => {
        validate = bool
        if(prop) {
          Object.keys(prop).filter(pi => {
            vres[pi] = false
          })
        }
      })
      if(!validate) {
        for(let i in this.info.parameters) {
          if(this.info.parameters[i].formula && this.info.parameters[i].formulaContent) {
            vres[this.info.parameters[i].key] = true // 存在公式解除校验判定
            this.$refs.paramForm.$refs.ruleForm.clearValidate(this.info.parameters[i].key)
          }
        }
        let vbool = true
        for(let v in vres) {
          vbool = vbool && vres[v] // 表单验证失败且存在公式的所有项判定
        }
        if(!vbool) {
          return false
        }
      }
      this.submitHandle(this.form, false)
      let temp = {
        node: JSON.parse(JSON.stringify(this.info)),
        functionName: this.info.functionName
      }
      for(let i in temp.node.parameters) {
        if(temp.node.parameters[i].customOptionValue) {
          // 默认值
          if(typeof temp.node.parameters[i].defaultValue != 'string') {
            let tdefault = {
              data: JSON.stringify(temp.node.parameters[i].defaultValue)
            }
            let tdefaultEnc = encryption({
              data: tdefault,
              key: deCodeKey,
              param: ["data"]
            })
            temp.node.parameters[i].defaultValue = tdefaultEnc.data
          }
          // options
          // if(typeof temp.node.parameters[i].options != 'string') {
          //   let toptions = {
          //     data: JSON.stringify(temp.node.parameters[i].options)
          //   }
          //   let toptionsEnc = encryption({
          //     data: toptions,
          //     key: deCodeKey,
          //     param: ["data"]
          //   })
          //   temp.node.parameters[i].options = toptionsEnc.data
          // }
        }
      }
      if(this.$route.query && this.$route.query.componentId) {
        temp.componentId = this.$route.query.componentId
      }
      if(this.ruleInfo.jvsAppId) {
        temp.jvsAppId = this.ruleInfo.jvsAppId
      }
      if(this.jvsAppId) {
        temp.jvsAppId = this.jvsAppId
      }
      this.testLoading = true
      testFunction(this.ruleId, temp).then(res => {
        if(res.data.code == 0) {
          this.result = res.data.data
          // this.form = res.data.data.body
          this.testLoading = false
        }else{
          this.result = {value: res.data.msg, type: 'string'}
        }
        this.testEndJsonShow = true
      }).catch(e => {
        this.result = null
        this.testEndJsonShow = false
      })
    },
    // 重置测试
    testReset() {
      this.testJsonShow = false
    },
    openclose () {
      let bool = this.open
      this.open = !bool
      // this.hideOpen = true
    },
    // 参数设置
    openParamSet (item, index) {
      if(!item.options) {
        item.options = []
      }
      this.currentParam = JSON.parse(JSON.stringify(item))
      this.currentPIndex = index
      this.paramVisible = true
    },
    // 关闭参数设置弹框
    paramClose () {
      this.paramVisible = false
      this.currentParam = null
      this.currentPIndex = -1
    },
    // 添加配置
    addRowHandle (list) {
      list.push({})
    },
    // 删除配置
    delRowHandle (row, index, list) {
      // if(row.formula) {
      //   // 删除公式
      //   deleteExec(this.ruleInfo.id, row.formula).then(res => {
      //     if(res.data && res.data.code == 0) {
      //       list = list.splice(index, 1)
      //       this.$forceUpdate()
      //     }
      //   })
      // }else{
      //   list = list.splice(index, 1)
      // }
      list = list.splice(index, 1)
      this.$forceUpdate()
    },
    // 保存参数设置
    saveHandle (row) {
      if(row.type == 'multipleSelected') {
        this.$set(this.info.parameters[row.index], 'options', row.options)
        // 更新节点
        this.$emit('updateNode', this.info)
      }else{
        let tp = {
          data: JSON.stringify(row.options)
        }
        let temp = encryption({
          data: tp,
          key: deCodeKey,
          param: ["data"]
        });
        saveCustomOption(row.customOptionValue, temp.data).then(res => {
          if(res.data.code == 0) {
            // this.$message.success('保存成功')
            this.$notify({
              title: '提示',
              message: '保存成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.paramVisible = false
            if(this.currentPIndex > -1) {
              this.$set(this.appendList[this.currentPIndex], 'options', row.options)
            }
            this.formOption.column.filter(col => {
              if(col.prop == row.key){
                if(row.customOptionValue) {
                  let dicTemp = []
                  for(let p  in row.options) {
                    let tp = {
                      data: JSON.stringify(row.options[p])
                    }
                    let temp = encryption({
                      data: tp,
                      key: deCodeKey,
                      param: ["data"]
                    });
                    dicTemp.push({
                      label: row.options[p].name,
                      value: temp.data
                    })
                  }
                  col.dicData = dicTemp
                }else{
                  this.$set(col, 'dicData', row.options)
                }
              }
            })
            this.$set(this.info.parameters[row.index], 'options', row.options)
            // 更新节点
            this.$emit('updateNode', this.info)
            // 重新拉取左侧工具栏
            eventBus.$emit('regetFunc', true)
          }
        })
      }
    },
    // listMap添加组
    addNewGroup (item) {
      if(!item.list) {
        this.$set(item, 'list', [])
      }
      item.list.push([])
      if(item.list.length > 0) {
        this.$refs.paramForm.$refs.ruleForm.clearValidate(item.prop)
      }
      this.setValueOfMapList(item)
      this.$forceUpdate()
    },
    // listMap删除组
    deleteGroup (index, item) {
      item.list.splice(index, 1)
      this.setValueOfMapList(item)
      if(this.openIndex && this.openIndex[item.prop]) {
        let ix = this.openIndex[item.prop].indexOf(index)
        if(ix > -1) {
          this.openIndex[item.prop].splice(ix, 1)
        }
      }
      this.$forceUpdate()
    },
    // 组添加行数据
    addLineToGroup (list, type, item) {
      if(type == 'string') {
        list.push('')
      }
      if(type == 'obj') {
        list.push({key: '', value: ''})
      }
      this.setValueOfMapList(item)
      this.$forceUpdate()
    },
    // 删除行数据
    delItemOfGroup (index, list, item) {
      list.splice(index, 1)
      this.setValueOfMapList(item)
      this.$forceUpdate()
    },
    setValueOfMapList (item) {
      let prop = item.prop
      let list = []
      for(let i in item.list) {
        // let obj = {}
        // for(let j in item.list[i]) {
        //   obj[item.list[i][j].key] = item.list[i][j].value
        // }
        // list.push(obj)
        list.push(item.list[i])
      }
      this.$set(this.form, prop, list)
    },
    // 设置公式
    setFuncHandle (item) {
      let pdata = {
        businessId: item.prop ? item.prop : item.key
      }
      if(this.ruleInfo.jvsAppId) {
        pdata.jvsAppId = this.ruleInfo.jvsAppId
      }
      if(this.$route.query) {
        if(this.$route.query.jvsAppId) {
          pdata.jvsAppId = this.$route.query.jvsAppId
        }
        if(this.ruleInfo.id) {
          pdata.designId = this.ruleInfo.id
        }
      }
      if(this.info && this.info.id) {
        let obj = {
          nodeId: this.info.id
        }
        if(this.activeCanvas != 'main') {
          obj.canvasId = this.activeCanvas
        }
        pdata.extendJson = JSON.stringify(obj)
      }
      this.submitHandle(null, false)
      this.$setFormula({
        title: '公式设置',
        hasModal: true,
        label: item.label ? item.label : item.info,
        execId: item.formula ? item.formula : '',
        apiPrefix: 'jvs-design',
        useCase: 'RULE',
        props: pdata,
        afterSave: (dialog, data) => {
          if(data && data.id) {
            this.info.parameters.filter(it => {
              if(it.key == pdata.businessId) {
                this.$set(it, 'formula', data.id)
                this.$set(it, 'formulaContent', data.body)
                this.$refs.paramForm.$refs.ruleForm.clearValidate(item.prop)
                if(item.type == 'expressionText') {
                  this.$set(this.form, item.prop, data.body)
                }
              }
            })
            // 更新节点
            this.$emit('updateNode', this.info, 'formula')
          }
          dialog.handleClose()
        }
      })
    },
    // 获取关联数据
    getLinkFuncData (type, id, key, reset) {
      let itemType = ''
      let index = -1
      for(let p in this.info.parameters) {
        if(this.info.parameters[p].key == key) {
          itemType = this.info.parameters[p].inputType
          index = p
        }
      }
      if(index > -1) {
        this.$set(this.info.parameters[index], 'options', [])
      }
      getFuncLink(this.$route.query.jvsAppId, type, id, key).then(res => {
        if(res.data && res.data.code == 0) {
          if(['multipleSelected', 'list', 'listMap'].indexOf(itemType) > -1) {
            if(reset) {
              this.$set(this.form, key, [])
            }
          }else{
            if(reset) {
              this.$set(this.form, key, "")
            }
          }
          // console.log(itemType)
          if(['dataModelField', 'dataModelFilterField', 'flowNode', 'dataModelOrderField'].indexOf(itemType) > -1) {
            let temp = []
            this.soltList.filter(item => {
              if(item.prop == key) {
                item.options = res.data.data
                this.$forceUpdate()
              }
            })
            if(res.data.data.length < 31) {
              if(itemType == 'dataModelField') {
                for(let i in res.data.data) {
                  temp.push({
                    fieldKey: res.data.data[i].value,
                    prop: 'value',
                    value: ''
                  })
                }
              }
              if(itemType == 'dataModelFilterField'){
                for(let i in res.data.data) {
                  temp.push({
                    fieldKey: res.data.data[i].value,
                    enabledQueryTypes: '',
                    prop: 'value',
                    value: ''
                  })
                }
              }
            }
            if(itemType == 'flowNode') {
              for(let i in res.data.data) {
                temp.push({
                  fieldKey: res.data.data[i].value,
                  formula: '',
                  formulaContent: ''
                })
              }
              this.$set(this.formOption.column[index], 'display', true)
            }
            if(!this.form[key] || JSON.stringify(this.form[key]) == "[]" || reset) {
              this.$set(this.form, key, temp)
            }
            if(itemType != 'flowNode') {
              return false
            }
            temp.filter(pit => {
              let pbool = this.needAddToArr(this.form[key], 'fieldKey', pit.fieldKey)
              if(pbool) {
                let toj = null
                if(itemType == 'dataModelField'){
                  toj = {
                    fieldKey: pit.fieldKey,
                    prop: 'value',
                    value: ''
                  }
                }
                if(itemType == 'dataModelFilterField'){
                  toj = {
                    fieldKey: pit.fieldKey,
                    enabledQueryTypes: '',
                    prop: 'value',
                    value: ''
                  }
                }
                if(itemType == 'flowNode') {
                  toj = {
                    fieldKey: pit.fieldKey,
                    formula: '',
                    formulaContent: ''
                  }
                }
                if(toj) {
                  this.form[key].push(toj)
                  this.$forceUpdate()
                }
              }
            })
          }
          if(index > -1) {
            let dicTemp = []
            // console.log(res.data.data)
            if(this.info.parameters[index].customOptionValue) {
              for(let p  in res.data.data) {
                let tp = {
                  data: JSON.stringify(res.data.data[p])
                }
                let temp = encryption({
                  data: tp,
                  key: deCodeKey,
                  param: ["data"]
                });
                dicTemp.push({
                  label: res.data.data[p].label,
                  value: temp.data
                })
              }
            }else{
              dicTemp = res.data.data
            }
            this.$set(this.formOption.column[index], 'dicData', dicTemp)
            this.$set(this.info.parameters[index], 'options', res.data.data)
            for(let i in this.soltList) {
              if(this.soltList[i].prop == key) {
                this.$set(this.soltList[i], 'options', res.data.data)
              }
            }
          }
          if(itemType == 'flowNode' && (!res.data.data || res.data.data.length == 0)) {
            this.$set(this.formOption.column[index], 'display', false)
          }
          this.$forceUpdate()
        }
      })
    },
    // 关联设置函数
    setLinkFormula (item, index, prop) {
      let pdata = {
        businessId: item.fieldKey
      }
      if(this.ruleInfo.jvsAppId) {
        pdata.jvsAppId = this.ruleInfo.jvsAppId
      }
      if(this.$route.query) {
        if(this.$route.query.jvsAppId) {
          pdata.jvsAppId = this.$route.query.jvsAppId
        }
        if(this.ruleInfo.id) {
          pdata.designId = this.ruleInfo.id
        }
      }
      if(this.info && this.info.id) {
        let obj = {
          nodeId: this.info.id
        }
        if(this.activeCanvas != 'main') {
          obj.canvasId = this.activeCanvas
        }
        pdata.extendJson = JSON.stringify(obj)
      }
      this.$setFormula({
        title: '公式设置',
        hasModal: true,
        label: item.fieldName ? item.fieldName : '',
        execId: this.form[prop][index].formula ? this.form[prop][index].formula : '',
        apiPrefix: 'jvs-design',
        useCase: 'RULE',
        props: pdata,
        afterSave: (dialog, data) => {
          if(data && data.id) {
            this.info.parameters.filter(it => {
              if(it.key == prop) {
                this.$set(this.form[prop][index], 'formula', data.id)
                this.$set(this.form[prop][index], 'formulaContent', data.body)
              }
            })
          }
          dialog.handleClose()
        }
      })
    },
    // 关闭测试结果
    resultClose () {
      this.testJsonShow = false
    },
    // 复制测试结果
    copyTestJsonHandle (result) {
      const text = document.createElement('input')
      text.value = typeof result == 'string' ? result : JSON.stringify(result)
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
    // 定义结构
    customStructureHandle (item) {
      this.currentParam = item
      if(this.info && this.info.customStructureBody) {
        this.$set(this.form, item.prop, this.info.customStructureBody)
      }
      if(!this.form[item.prop] || this.form[item.prop].length == 0) {
        this.$set(this.form, item.prop, [])
        if(this.result && this.result.value && typeof this.result.value == 'object') {
          this.customStructureLoading = true
          jvsParamTypePost(this.result.value).then(res => {
            if(res.data && res.data.code == 0) {
              this.$set(this.form, item.prop, res.data.data)
              this.eachTree(null, null, this.form[item.prop], 'setKey')
              this.customStructureShow = true
              this.customStructureLoading = false
            }
          })
        }else{
          this.customStructureShow = true
        }
      }else{
        this.eachTree(null, null, this.form[item.prop], 'setKey')
        this.customStructureShow = true
      }
    },
    customStructureSave () {
      this.$set(this.info, 'customStructureBody', this.form.customStructureBody)
      // 更新节点
      this.$emit('updateNode', this.info)
      this.customStructureClose()
    },
    customStructureClose () {
      this.customStructureShow = false
      this.currentParam = null
    },
    getJvsParamType () {
      jvsParamTypeGet().then(res=> {
        if(res.data && res.data.code == 0) {
          // console.log(res.data.data)
          this.jvsParamTypes = res.data.data
        }
      })
    },
    // 配置公式
    openFormula (item, formItem, prop, formType) {
      let pdata = {
        businessId: item.key
      }
      if(this.ruleInfo.jvsAppId) {
        pdata.jvsAppId = this.ruleInfo.jvsAppId
      }
      if(this.$route.query) {
        if(this.$route.query.jvsAppId) {
          pdata.jvsAppId = this.$route.query.jvsAppId
        }
        if(this.ruleInfo.id) {
          pdata.designId = this.ruleInfo.id
        }
      }
      if(this.info && this.info.id) {
        let obj = {
          nodeId: this.info.id
        }
        if(this.activeCanvas != 'main') {
          obj.canvasId = this.activeCanvas
        }
        pdata.extendJson = JSON.stringify(obj)
      }
      this.$setFormula({
        title: '公式设置',
        hasModal: true,
        label: item.key,
        execId: item.formula ? item.formula : '',
        apiPrefix: 'jvs-design',
        useCase: 'RULE',
        props: pdata,
        afterSave: (dialog, data) => {
          if(data && data.id) {
            this.$set(item, 'formula', data.id)
            this.$set(item, 'formulaContent', data.body)
            if(formType == 'listMap') {
              this.setValueOfMapList(formItem)
            }else{
              this.setValueOfMap(formItem, prop)
            }
          }
          dialog.handleClose()
        }
      })
    },
    // 定义结构新增一行
    addCustomStructureRowHandle (list, index, type) {
      if(type == 'children') {
        if(!list.children) {
          this.$set(list, 'children', [])
        }
        list.children.push({
          key: new Date().getTime(),
          name: '',
          jvsParamType: '',
          info: ''
        })
      }else{
        list.push({
          key: new Date().getTime(),
          name: '',
          jvsParamType: '',
          info: ''
        })
      }
    },
    typeChange (type, row) {
      if(['object', 'array'].indexOf(type) > -1) {
        if(!row.children) {
          this.$set(row, 'children', [])
        }
      }else{
        this.$set(row, 'children', [])
      }
    },
    // 定义结构删除行数据
    delTableRowHandle (row, index, list) {
      this.eachTree(row.key, 'key', list, 'delete')
      this.$forceUpdate()
    },
    eachTree (val, attr, list, optype) {
      for(let i in list) {
        if(optype == 'setKey') {
          if(!list[i].key) {
            list[i].key = Math.random().toString(36).substr(3, 10)+''+i
          }
        }
        if(list[i][attr] == val) {
          if(optype == 'delete') {
            list.splice(i, 1)
          }
        }
        if(list[i].children && list[i].children.length > 0) {
          this.eachTree(val, attr, list[i].children, optype)
        }
      }
    },
    needAddToArr (list, prop, value) {
      let bool = true
      for(let i in list) {
        if(list[i][prop] == value) {
          bool = false
        }
      }
      return bool
    },
    getTips () {
      this.formOption.column.filter(fitm => {
        if(fitm.dicData && fitm.dicData.length > 0) {
          fitm.dicData.filter(fidtm => {
            if(this.form[fitm.prop] == fidtm.value && fidtm.describeText) {
              fitm.tips = {
                text: fidtm.describeText,
                position: 'bottom'
              }
            }
          })
          if(!this.form[fitm.prop]) {
            fitm.tips = {
              text: '',
              position: 'bottom'
            }
          }
        }
      })
    },
    // 选择图片
    chooseImage (prop) {
      this.imgProp = prop
      this.$refs.logoSelect.init()
      this.chooseAble = true
    },
    // 确认图片
    handleConfirm (value) {
      this.chooseAble = false;
      if(value && value.fileLink) {
        this.$set(this.form, this.imgProp, value.fileLink)
      }
    },
    delIamgeSelect (prop) {
      this.$set(this.form, prop, '')
    },
    // 绑定变量
    bindParameters (item, prop) {
      if(!this.form[prop]) {
        this.$set(this.form, prop, [])
      }
      this.form[prop].push({
        conditions: [],
        params: []
      })
    },
    // 删除绑定组
    deleteBindGroup (prop, index) {
      let fmlist = []
      if(this.form[prop][index].formula) {
        fmlist.push(this.form[prop][index].formula)
      }
      if(this.form[prop][index].conditions) {
        this.form[prop][index].conditions.filter(cit => {
          if(cit.formula) {
            fmlist.push(cit.formula)
          }
        })
      }
      if(this.form[prop][index].params) {
        this.form[prop][index].params.filter(pit => {
          if(pit.formula) {
            fmlist.push(pit.formula)
          }
        })
      }
      // if(fmlist.length > 0) {
      //   mulDeleteExec(this.ruleInfo.id, fmlist).then(res => {
      //     if(res.data && res.data.code == 0) {
      //       this.form[prop].splice(index, 1)
      //       this.$forceUpdate()
      //     }
      //   })
      // }else{
      //   this.form[prop].splice(index, 1)
      //   this.$forceUpdate()
      // }
      this.form[prop].splice(index, 1)
      this.$forceUpdate()
    },
    // 绑定条件公式
    setBindGroupFormula (item, index, prop) {
      let pdata = {
        businessId: (prop + '-bind-group-' + index)
      }
      if(this.ruleInfo.jvsAppId) {
        pdata.jvsAppId = this.ruleInfo.jvsAppId
      }
      if(this.$route.query) {
        if(this.$route.query.jvsAppId) {
          pdata.jvsAppId = this.$route.query.jvsAppId
        }
        if(this.ruleInfo.id) {
          pdata.designId = this.ruleInfo.id
        }
      }
      if(this.info && this.info.id) {
        let obj = {
          nodeId: this.info.id
        }
        if(this.activeCanvas != 'main') {
          obj.canvasId = this.activeCanvas
        }
        pdata.extendJson = JSON.stringify(obj)
      }
      this.$setFormula({
        title: '公式设置',
        hasModal: true,
        label: (item.label+'组条件'+(Number(index)+1)),
        execId: this.form[prop][index].formula ? this.form[prop][index].formula : '',
        apiPrefix: 'jvs-design',
        useCase: 'RULE',
        props: pdata,
        afterSave: (dialog, data) => {
          if(data && data.id) {
            this.info.parameters.filter(it => {
              if(it.key == prop) {
                this.$set(this.form[prop][index], 'formula', data.id)
                this.$set(this.form[prop][index], 'formulaContent', data.body)
              }
            })
          }
          dialog.handleClose()
        }
      })
    },
    addBindParamRowHandle (list, attr, val) {
      if(attr && val){
        let ob = {}
        ob[attr] = val
        list.push(ob)
      }else{
        list.push({})
      }
      this.$forceUpdate()
    },
    setBindFormula (row, index, pindex, prop, key) {
      let pdata = {
        businessId: (key + '-' +prop + '-' + row.fieldKey + index)
      }
      if(this.ruleInfo.jvsAppId) {
        pdata.jvsAppId = this.ruleInfo.jvsAppId
      }
      if(this.$route.query) {
        if(this.$route.query.jvsAppId) {
          pdata.jvsAppId = this.$route.query.jvsAppId
        }
        if(this.ruleInfo.id) {
          pdata.designId = this.ruleInfo.id
        }
      }
      if(this.info && this.info.id) {
        let obj = {
          nodeId: this.info.id
        }
        if(this.activeCanvas != 'main') {
          obj.canvasId = this.activeCanvas
        }
        pdata.extendJson = JSON.stringify(obj)
      }
      let labelName = ''
      this.bindParameterOption.filter(bp => {
        if(bp.id == row.fieldKey) {
          labelName = bp.name
        }
      })
      this.$setFormula({
        title: '公式设置',
        hasModal: true,
        label: labelName,
        execId: this.form[prop][index][key][pindex].formula ? this.form[prop][index][key][pindex].formula : '',
        apiPrefix: 'jvs-design',
        useCase: 'RULE',
        props: pdata,
        afterSave: (dialog, data) => {
          if(data && data.id) {
            this.info.parameters.filter(it => {
              if(it.key == prop) {
                this.$set(this.form[prop][index][key][pindex], 'formula', data.id)
                this.$set(this.form[prop][index][key][pindex], 'formulaContent', data.body)
              }
            })
          }
          dialog.handleClose()
        }
      })
    },
    delBindParamRowHandle (row, index, list) {
      // if(row.formula) {
      //   // 删除公式
      //   deleteExec(this.ruleInfo.id, row.formula).then(res => {
      //     if(res.data && res.data.code == 0) {
      //       list.splice(index, 1)
      //       this.$forceUpdate()
      //     }
      //   })
      // }else{
      //   list.splice(index, 1)
      //   this.$forceUpdate()
      // }
      list.splice(index, 1)
      this.$forceUpdate()
    },
    userSelectSubmit (list) {
      console.log(list)
    },
    clearAll () {
      this.$set(this.form, this.currentParam.prop, [])
    },
    useTestResult () {
      if(this.testData && this.testData.value && typeof this.testData.value == 'object') {
        this.customStructureLoading = true
        jvsParamTypePost(this.testData.value).then(res => {
          if(res.data && res.data.code == 0) {
            this.$set(this.form, 'customStructureBody', res.data.data)
            this.eachTree(null, null, this.form['customStructureBody'], 'setKey')
            this.customStructureLoading = false
          }
        })
      }else{
        if(this.result && this.result.value && typeof this.result.value == 'object') {
          this.customStructureLoading = true
          jvsParamTypePost(this.result.value).then(res => {
            if(res.data && res.data.code == 0) {
              this.$set(this.form, 'customStructureBody', res.data.data)
              this.eachTree(null, null, this.form['customStructureBody'], 'setKey')
              this.customStructureLoading = false
            }
          })
        }
      }
    },
    addItem (item) {
      let itemType = item.type
      let toj = null
      if(itemType == 'dataModelField'){
        toj = {
          fieldKey: '',
          prop: 'value',
          value: ''
        }
      }
      if(itemType == 'dataModelFilterField'){
        toj = {
          fieldKey: '',
          enabledQueryTypes: '',
          prop: 'value',
          value: ''
        }
      }
      if(itemType == 'dataModelOrderField') {
        toj = {
          fieldKey: '',
          direction: 'ASC'
        }
      }
      if(toj) {
        this.form[item.prop].push(toj)
        this.$forceUpdate()
      }
    },
    getItemEnabledQueryTypes (row, item) {
      let list = []
      item.options.filter(op => {
        if(op.value == row.fieldKey) {
          if(op.extend && op.extend.enabledQueryTypes) {
            list = op.extend.enabledQueryTypes
          }
        }
      })
      return list
    },
    fieldKeyChange (item, index) {
      let itemType = item.type
      if(itemType == 'dataModelField'){
        this.$set(this.form[item.prop][index], 'value', '')
      }
      if(itemType == 'dataModelFilterField'){
        this.$set(this.form[item.prop][index], 'enabledQueryTypes', '')
        this.$set(this.form[item.prop][index], 'value', '')
      }
    },
    isDisabled (item, row, list) {
      let bool = false
      for(let i in list) {
        if(list[i].fieldKey == item.fieldKey || list[i].fieldKey == item.value) {
          bool = true
          if(row.fieldKey == item.fieldKey || row.fieldKey == item.value) {
            bool = false
          }
        }
      }
      return bool
    },
    validateKey (item, type) {
      if(/^\d+/.test(item.fieldKey)) {
        if(type == 'blur') {
          this.$set(item, 'fieldKey', '')
        }
        return false
      }else{
        return true
      }
    },
    addKeyToMap (item) {
      if(!item.list) {
        this.$set(item, 'list', [])
      }
      item.list.push({key: '', value: ''})
      this.setMapValueSelectd(item)
      this.$forceUpdate()
    },
    deleteItemKeyOfMap (index, item) {
      item.list.splice(index, 1)
      this.setMapValueSelectd(item)
    },
    setMapValueSelectd (item) {
      this.$set(this.form, item.prop, {})
      for(let i in item.list) {
        if(item.list[i].key && item.list[i].value) {
          this.$set(this.form[item.prop], item.list[i].key, item.list[i].value)
        }
      }
    },
    formatJson (obj) {
      let str = typeof obj == 'string' ? obj : JSON.stringify(obj)
      return str
    },
    openCloseIndex (prop, index) {
      if(!this.openIndex[prop]){
        this.$set(this.openIndex, prop, [])
      }
      let isIndex = this.openIndex[prop].indexOf(index)
      if(isIndex > -1) {
        this.openIndex[prop].splice(isIndex, 1)
      }else{
        this.openIndex[prop].push(index)
      }
      this.$forceUpdate()
    },
    treeChange (value) {
      console.log(this.form)
      // this.$set(this.form, this.item.prop, value[this.item.prop])
    }
  }
}
</script>
<style lang="scss" scoped>
.add-formula-svg{
  width: 16px;
  height: 16px;
  cursor:pointer;
  margin-left: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.set-formula-button{
  font-family: Microsoft YaHei-Regular, Microsoft YaHei;
  font-weight: 400;
  font-size: 14px;
  color: #1E6FFF;
}
.add-one-group-button{
  margin: 0;
  width: 100%;
  height: 36px;
  border-radius: 4px;
  /deep/.el-button{
    width: 100%;
    height: 100%;
    background: #E4EDFF;
    font-family: Microsoft YaHei-Regular, Microsoft YaHei;
    font-weight: 400;
    font-size: 14px;
    color: #1E6FFF;
    span{
      display: flex;
      align-items: center;
      justify-content: center;
    }
    svg{
      width: 16px;
      height: 16px;
      fill: #1E6FFF;
      margin-right: 4px;
    }
  }
  &.showmargin{
    margin-top: 16px;
  }
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
.input-list-box{
  .input-list{
    margin: 0;
    padding: 0;
    li{
      display: flex;
      align-items: center;
      flex-wrap: nowrap;
      .el-input{
        flex: 1;
        overflow: hidden;
      }
      .delete-icon-button{
        margin-left: 8px;
      }
      &+li{
        margin-top: 8px;
      }
    }
  }
}
.map-box{
  width: 100%;
  background: #F5F6F7;
  border-radius: 4px;
  padding: 16px;
  box-sizing: border-box;
  .table-head{
    span{
      height: 20px;
      font-family: Source Han Sans-Regular, Source Han Sans;
      font-weight: 400;
      font-size: 14px;
      color: #363B4C;
      line-height: 20px;
    }
    i{
      display: block;
      width: 16px;
    }
  }
  p{
    display: flex;
    align-items: center;
    text-align: left;
    margin: 0;
    overflow: hidden;
    >span{
      flex: 1;
      margin-right: 8px;
      display: flex;
      align-items: center;
      justify-content: flex-start;
    }
    >span:nth-last-of-type(1) {
      width: 16px;
    }
  }
}
.list-map-box{
  .list-map-group-item{
    background: #F5F6F7;
    border-radius: 4px;
    &+.list-map-group-item{
      margin-top: 16px;
    }
    .header{
      height: 40px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      .head-left{
        display: flex;
        align-items: center;
        .line{
          width: 3px;
          height: 16px;
          background: #1E6FFF;
          border-radius: 0px 4px 4px 0px;
          margin-right: 13px;
        }
        .text{
          font-family: Microsoft YaHei-Bold, Microsoft YaHei;
          font-weight: 700;
          font-size: 14px;
          color: #363B4C;
          line-height: 18px;
        }
      }
      .head-right{
        svg{
          width: 16px;
          height: 16px;
          margin-right: 16px;
          cursor: pointer;
          transition: all .3s;
        }
        .close-icon{
          transform: rotateZ(-90deg);
        }
      }
    }
    .bottom-body{
      border-top: 1px solid #EEEFF0;
      padding: 16px;
      p{
        display: flex;
        justify-content: space-between;
        text-align: left;
        margin: 0;
        overflow: hidden;
        >span{
          width: calc(58% - 8px);
          display: flex;
          align-items: center;
          justify-content: flex-start;
        }
        >span:nth-of-type(1) {
          width: calc(38% - 8px);
        }
        >span:nth-last-of-type(1) {
          width: 16px;
        }
      }
      .table-head{
        span{
          height: 20px;
          font-family: Source Han Sans-Regular, Source Han Sans;
          font-weight: 400;
          font-size: 14px;
          color: #363B4C;
          line-height: 20px;
        }
      }
    }
  }
}
.model-list-box{
  width: 100%;
  background: #F5F6F7;
  border-radius: 4px;
  padding: 16px;
  box-sizing: border-box;
  .data-model-table{
    p:nth-of-type(1){
      margin-top: 0!important;
    }
  }
}
.bind-parameter-list{
  background-color: #F5F6F7;
  border-radius: 4px;
  position: relative;
  &+.bind-parameter-list{
    margin-top: 16px;
  }
  .bind-parameter-list-item{
    display: flex;
    align-items: center;
    >span{
      margin-right: 8px;
    }
  }
  .condition-box{
    .condition-head{
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 11px 16px;
      border-bottom: 1px solid #EEEFF0;
      svg{
        cursor: pointer;
      }
    }
    .condition-table{
      padding-top: 4px;
      margin: 0 16px;
      padding-bottom: 16px;
      border-bottom: 1px solid #EEEFF0;
    }
    .condi-item{
      margin-bottom: 0;
      padding: 0;
    }
    .condi-item:nth-of-type(1){
      margin-top: 5px;
    }
  }
  .param-box{
    padding: 0 16px;
    padding-bottom: 16px;
    h4{
      margin: 0;
      margin-top: 16px;
      font-family: Source Han Sans-Regular, Source Han Sans;
      font-weight: 400;
      font-size: 14px;
      color: #363B4C;
      line-height: 20px;
    }
    .bind-parameter-list-item{
      padding: 0;
      margin-bottom: 0;
    }
    .bind-parameter-list-item:nth-of-type(1){
      margin-top: 5px;
    }
  }
}
.map-table, .list-map-table, .condition-table, .param-box, .data-model-table{
  p{
    margin: 0;
    margin-top: 8px!important;
    /deep/.el-input{
      height: 32px!important;
      line-height: 32px!important;
    }
    /deep/.el-input__inner{
      height: 32px!important;
      line-height: 32px!important;
      background-color: #fff!important;
      font-family: Microsoft YaHei-Regular, Microsoft YaHei!important;
      font-weight: 400!important;
      font-size: 12px!important;
      color: #363B4C!important;
      border: 0!important;
      &::placeholder{
        font-family: Microsoft YaHei-Regular, Microsoft YaHei!important;
        font-weight: 400!important;
        font-size: 12px!important;
        color: #6F7588!important;
      }
    }
    >span /deep/.el-select > :first-child.el-input .el-input__inner{
      height: 32px!important;
    }
    /deep/.el-input__suffix{
      .el-input__icon{
        line-height: 32px;
      }
    }
    .jvs-tree-select{
      height: 32px;
      /deep/.el-cascader{
        .el-input{
          .input.el-input__inner{
            overflow: hidden;
            white-space: pre;
            text-overflow: ellipsis;
          }
        }
      }
    }
  }
}
/deep/.el-form{
  .el-form-item__label{
    font-family: Source Han Sans-Bold, Source Han Sans;
    font-weight: 700;
    font-size: 14px;
    color: #363B4C;
    line-height: 20px;
  }
  .slot-label-item{
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    .el-form-item__label{
      width: 100%;
      display: flex;
      align-items: center;
      .slot-label{
        display: block;
        flex: 1;
        overflow: hidden;
        .slot-label-text{
          flex: 1;
          height: 20px;
          overflow: hidden;
          white-space: pre;
          text-overflow: ellipsis;
        }
      }
    }
    .el-form-item__content{
      width: 100%;
    }
  }
  .before-append-item{
    .el-form-item__content{
      flex-wrap: wrap;
      .before-append-content{
        display: block;
        width: 100%;
        line-height: 17px;
        margin-bottom: 8px;
        .slot-before-item{
          line-height: 17px;
          span{
            font-family: Source Han Sans-Regular, Source Han Sans;
            font-weight: 400;
            font-size: 12px;
            color: #6F7588;
            line-height: 17px;
          }
        }
      }
      >div{
        width: 100%;
      }
    }
  }
}
.customStructure-box{
  /deep/.el-table{
    .el-table__body-wrapper{
      .el-table__row{
        .cell{
          display: flex;
          align-items: center;
        }
      }
    }
  }
}
.codeEditor-box{
  position: relative;
  width: 100%;
  height: 300px;
  .codeEditor{
    position: absolute;
    width: 100%;
    height: 100%;
  }
}
.data-model-box{
  div{
    p{
      display: flex;
      align-items: center;
      justify-content: space-between;
      >span{
        display: block;
        margin-left: 8px;
        flex: 1;
        overflow: hidden;
        .el-select, .el-input{
          width: 100%;
        }
      }
      >span:nth-of-type(1){
        margin-left: 0;
      }
      b{
        word-break: keep-all;
        margin-left: 8px;
        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
        font-weight: 400;
        font-size: 14px;
        color: #363B4C;
        line-height: 18px;
      }
      >i{
        margin-left: 8px;
      }
    }
  }
}
</style>

<style lang="scss" scoped>
.setting-info{
  padding-bottom: 72px;

  /deep/.form-item-btn{
    position: fixed;
    bottom: 0;
    right: 0;
    width: 0;
    height: 72px;
    border-top: 1px solid #EEEFF0;
    transition: width .3s;
    padding: 0 24px;
    box-sizing: border-box;
    display: flex;
    align-items: center;
    background-color: #fff;
    z-index: 9;
    .el-button{
      width: 88px;
      height: 32px;
      span{
        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
        font-weight: 400;
        font-size: 14px;
      }
    }
    .form-btn-bar{
      margin-bottom: 0;
      height: 100%;
      .el-form-item__content{
        height: 100%;
        display: flex;
        align-items: center;
      }
    }
  }
  .test-loading-box{
    width: 100%;
    display: flex;
    justify-content: center;
    img{
      display:block;
      width: 150px;
      height: 120px;
    }
  }
  .form-item-tips{
    padding: 0 10px;
  }
  .select-image-show{
    position: relative;
    &:hover{
      .delete-select-image-tool{
        display: flex;
      }
    }
    img{
      display: block;
      width: 128px;
      height: 128px;
    }
    .delete-select-image-tool{
      display: none;
      align-items: center;
      justify-content: center;
      position: absolute;
      bottom: 0;
      left: 0;
      width: 128px;
      height: 32px;
      background: #363B4C;
      border-radius: 0px 0px 4px 4px;
      opacity: 0.5;
      i{
        font-size: 16px;
        color: #fff;
        cursor: pointer;
      }
    }
  }
  .choose-image-box{
    width: 128px;
    height: 128px;
    background: #F5F6F7;
    border-radius: 4px 4px 4px 4px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    >div{
      height: 17px;
      font-family: Source Han Sans, Source Han Sans;
      font-weight: 400;
      font-size: 12px;
      color: #6F7588;
      line-height: 17px;
      margin-top: 9px;
    }
  }
  /deep/.w-e-menu{
    .w-e-panel-container{
      width: 400px!important;
      // margin-left: -335px!important;
    }
  }
}
.result-box{
  padding: 0 20px;
  padding-bottom: 15px;
  h4{
    margin: 0;
    height: 32px;
    line-height: 32px;
    margin-top: 10px;
  }
  .result-text{
    margin-top: 5px;
    background: #f5f7fa;
    overflow: hidden;
    border-radius: 5px;
    max-height: 160px;
    overflow-y: auto;
    /deep/.jv-container{
      background: #f5f7fa;
      max-height: calc(100vh - 300px);
      overflow: hidden;
      overflow-y: auto;
    }
    section{
      padding: 10px;
    }
    /deep/.jv-container .jv-code{
      padding: 10px;
    }
  }
}
.rule-param-json-editor{
  width: 100%;
  height: 200px;
  background-color: #fff;
  /deep/.json-editor-entity-showPrintMargin{
    width: 500px;
    .ace_error{
      background-image: none;
    }
    .ace_scroller{
      overflow: unset;
    }
    .ace_text-layer{
      overflow: unset;
    }
  }
}
/deep/.rule-number-input{
  .el-input__inner::-webkit-outer-spin-button,
  .el-input__inner::-webkit-inner-spin-button {
    -webkit-appearance: none !important;
  }
  .el-input__inner[type='number'] {
    -moz-appearance: textfield;
  }
}
</style>
<style lang="scss">
.el-popover.test-result-popper{
  top: unset!important;
  bottom: 52px;
}
</style>style
