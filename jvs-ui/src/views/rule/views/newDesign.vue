<template>
  <div class="jvs-contail-font cont design-cont">
    <!-- 头部 -->
    <design-header ref="designHeader" :currentTab="currentTab" :infoData="formData" :tabList="tabList" type="rule" :leftIconSlot="true" @tabSelect="tabSelect" @handleSave="handleSave">
      <!-- 跳转其他同应用逻辑 -->
      <template slot="leftIcon">
        <div style="display: flex;align-items: center;">
          <el-popover
            v-model="showOtherRuleBool"
            popper-class="other-rule-list-popper"
            placement="bottom"
            trigger="click">
            <div class="other-rule-list">
              <div class="search">
                <el-input v-model="ruleSearchName" prefix-icon="el-icon-search" placeholder="搜索业务逻辑名称" @input="getAllRuleHandle"></el-input>
              </div>
              <div class="list-box">
                <div v-for="rule in ruleSearchList" :key="rule.id" :class="{'list-item': true, 'self': (rule.secret == $route.query.id)}">
                  <span class="name-text" :title="rule.name">{{rule.name}}</span>
                  <div v-if="rule.secret != $route.query.id" class="list-item-tool">
                    <el-tooltip content="设计" placement="top" class="custom-right-tool-poper">
                      <div class="setting-icon" @click="viewRule(rule)">
                        <svg aria-hidden="true">
                          <use xlink:href="#jvs-ui-icon-lianjie"></use>
                        </svg>
                      </div>
                    </el-tooltip>
                    <el-tooltip content="引用" placement="top" class="custom-right-tool-poper">
                      <el-popover
                        placement="top"
                        width="160"
                        v-model="rule.visible">
                        <div style="padding: 0 15px;">引用后将覆盖原有设计，是否继续？</div>
                        <div style="text-align: right; margin: 0;padding: 0 15px;">
                          <el-button size="mini" type="text" @click="rule.visible = false">取消</el-button>
                          <el-button type="primary" size="mini" @click="placeRuleHandle(rule)">确定</el-button>
                        </div>
                        <div slot="reference" class="setting-icon" @click="closeOther(rule)">
                          <svg aria-hidden="true" >
                            <use xlink:href="#jvs-ui-icon-lianjie1"></use>
                          </svg>
                        </div>
                      </el-popover>
                    </el-tooltip>
                  </div>
                </div>
              </div>
            </div>
            <div slot="reference" :class="{'rule-list-icon': true, 'open': showOtherRuleBool}" @click="getAllRuleHandle">
              <svg aria-hidden="true">
                <use xlink:href="#jvs-ui-icon-liebiaoshitu"></use>
              </svg>
            </div>
          </el-popover>
          <div class="split-line"></div>
        </div>
      </template>
      <template slot="right">
        <div class="test-button" @click="testHandle">
          <div class="icon">
            <svg aria-hidden="true">
              <use xlink:href="#jvs-ui-icon-a-zu6027"></use>
            </svg>
          </div>
          <span>执行</span>
        </div>
      </template>
    </design-header>
    <!-- 设计部分 -->
    <div v-show="currentTab === 'design'" :class="{'cont-box': true, 'data-cont-box': showDataInfo}">
      <!-- 设计器及工具栏 -->
      <div class="cont-box-right" style="width:100%;">
        <!-- 左侧菜单栏 -->
        <div class="left-menu">
          <div class="icon-tool-list">
            <div v-for="tool in leftMenuList" :key="tool.key" :class="'icon-tool-item ' + tool.key + (tool.key == showToolKey ? ' show' : '')" v-if="tool.display !== false" @click.stop="leftMenuenter(tool)">
              <div class="icon">
                <svg aria-hidden="true">
                  <use :xlink:href="'#'+tool.icon"></use>
                </svg>
                <span>{{tool.text}}</span>
              </div>
              <div v-show="tool.key == showToolKey" class="tool-box">
                <div class="title-box">
                  <h4>{{tool.text}}</h4>
                  <div v-if="['relog'].indexOf(tool.key) > -1" class="title-icon">
                    <el-switch v-model="formData.openLogRecording"></el-switch>
                  </div>
                </div>
                <!-- 组件库 -->
                <div v-if="tool.key == 'assembly'" :class="tool.key+'-rightBox'">
                  <Item :ruleInfo="formData"></Item>
                </div>
                <!-- 画布 -->
                <div v-if="tool.key == 'canvas'" :class="tool.key+'-rightBox'">
                  <div class="canvas-tool">
                    <div :class="{'canvas-tool-item': true, 'active': activeCanvas == 'main'}" @click="viewCanvas('main')">
                      <svg aria-hidden="true">
                        <use xlink:href="#jvs-ui-icon-duoxuan"></use>
                      </svg>
                      <span>主画布</span>
                    </div>
                    <div v-if="canvasList && canvasList.length > 0">
                      <div v-for="(item, index) in canvasList" :key="index+'-canvas-item'" :class="{'canvas-tool-item': true, 'active': activeCanvas == item.id}" @click="viewCanvas(item.id)">
                        <svg aria-hidden="true">
                          <use xlink:href="#jvs-ui-icon-duoxuan"></use>
                        </svg>
                        <span>{{item.name}}</span>
                      </div>
                    </div>
                  </div>
                </div>
                <!-- 入参 -->
                <div v-if="tool.key == 'variable' && formData.parameterIn" :class="tool.key+'-rightBox'">
                  <div class="top">
                    <!-- @formChange="parameterInChange" -->
                    <jvs-form v-if="formData.reqType == 'External_API_logic'" :option="parameterInOption" :formData="formData.parameterIn"></jvs-form>
                    <div class="parameter-in-set-tab">
                      <div v-if="formData.reqType == 'External_API_logic'" :class="{'extend-tab-item': true, 'active': parameterInTab == 'Query'}" @click="parameterInTab='Query';">Query</div>
                      <div :class="{'extend-tab-item': true, 'active': parameterInTab == 'Body'}" @click="parameterInTab='Body';">Body</div>
                      <div v-if="formData.reqType == 'External_API_logic'" :class="{'extend-tab-item': true, 'active': parameterInTab == 'Headers'}" @click="parameterInTab='Headers';">Headers</div>
                      <!-- <div v-if="formData.reqType == 'External_API_logic'" :class="{'extend-tab-item': true, 'active': parameterInTab == 'Path'}" @click="parameterInTab='Path';">Path</div> -->
                    </div>
                  </div>
                  <div class="parameter-in-set-list">
                    <div v-show="parameterInTab == 'Query'" class="query-list">
                      <div class="header">
                        <span class="el-input">参数名称</span>
                        <span class="el-input">参数描述</span>
                        <span style="width: 50px;">必填</span>
                        <span style="width: 50px;">缓存</span>
                        <span style="width: 16px;"></span>
                      </div>
                      <div v-if="formData.parameterIn && formData.parameterIn.queryList && formData.parameterIn.queryList.length > 0" class="body">
                        <div v-for="(query, qix) in formData.parameterIn.queryList" :key="'query-item-'+qix" class="body-item">
                          <el-input v-model="query.key" placeholder="请输入参数名称" size="mini"></el-input>
                          <el-input v-model="query.explain" placeholder="请输入参数描述" size="mini"></el-input>
                          <span style="width: 50px;">
                            <el-checkbox v-model="query.necessity" size="mini"></el-checkbox>
                          </span>
                          <span style="width: 50px;">
                            <el-checkbox v-model="query.cache" size="mini"></el-checkbox>
                          </span>
                          <div class="delete-icon-button" @click="deleteLine(qix, 'queryList')">
                            <span class="border-line"></span>
                          </div>
                        </div>
                      </div>
                      <div class="add-line-button">
                        <div class="button" @click="addLineHandle('queryList')">
                          <div class="icon">
                            <svg aria-hidden="true">
                              <use xlink:href="#jvs-ui-icon-xinjian"></use>
                            </svg>
                          </div>
                          <span>新增一行</span>
                        </div>
                      </div>
                    </div>
                    <div v-show="parameterInTab == 'Body'" class="body-list">
                      <div style="height: 200px;position: relative;border-radius: 4px;overflow: hidden;">
                        <codeEditor class="codeEditor" :code="varialbeData" prop="variableJsonCode" :onlyJSon="true" @change="changeHandle"></codeEditor>
                      </div>
                      <div style="margin: 16px 0;">
                        <el-button type="primary" size="mini" @click="formatBodyList('parameterIn', 'bodyList')">解析body</el-button>
                      </div>
                      <div class="body-list-box">
                        <div class="tip">以解析后的字段为准</div>
                        <table-tree
                          :option="bodyOption"
                          :formData="formData.parameterIn"
                          prop="bodyList">
                        </table-tree>
                      </div>
                    </div>
                    <div v-show="parameterInTab == 'Headers'" class="header-list">
                      <div class="header">
                        <span class="el-input">请求头类型</span>
                        <span class="el-input">请求头内容</span>
                        <span style="width: 50px;">必填</span>
                        <span style="width: 50px;">缓存</span>
                        <span style="width: 16px;"></span>
                      </div>
                      <div v-if="formData.parameterIn && formData.parameterIn.headerList && formData.parameterIn.headerList.length > 0" class="body">
                        <div v-for="(query, qix) in formData.parameterIn.headerList" :key="'query-item-'+qix" class="body-item">
                          <el-select v-model="query.key" placeholder="请选择或输入请求头类型" filterable allow-create size="mini">
                            <el-option v-for="hit in requestHeaderDicData" :key="'query-item-'+qix+'-'+hit" :label="hit" :value="hit"></el-option>
                          </el-select>
                          <el-input v-model="query.explain" placeholder="请输入请求头内容，比如 application/json" size="mini"></el-input>
                          <span style="width: 50px;">
                            <el-checkbox v-model="query.necessity" size="mini"></el-checkbox>
                          </span>
                          <span style="width: 50px;">
                            <el-checkbox v-model="query.cache" size="mini"></el-checkbox>
                          </span>
                          <div class="delete-icon-button" @click="deleteLine(qix, 'headerList')">
                            <span class="border-line"></span>
                          </div>
                        </div>
                      </div>
                      <div class="add-line-button">
                        <div class="button" @click="addLineHandle('headerList')">
                          <div class="icon">
                            <svg aria-hidden="true">
                              <use xlink:href="#jvs-ui-icon-xinjian"></use>
                            </svg>
                          </div>
                          <span>新增一行</span>
                        </div>
                      </div>
                    </div>
                    <!-- <div v-show="parameterInTab == 'Path'" class="header-list">
                      <div class="header">
                        <span style="width: 20%;">Path变量</span>
                        <span class="el-input">变量描述</span>
                      </div>
                      <div v-if="formData.parameterIn && formData.parameterIn.pathList && formData.parameterIn.pathList.length > 0" class="body">
                        <div v-for="(query, qix) in formData.parameterIn.pathList" :key="'query-item-'+qix" class="body-item">
                          <span style="width: 20%;">{{query.key}}</span>
                          <el-input v-model="query.explain" placeholder="请输入变量描述" size="mini"></el-input>
                        </div>
                      </div>
                    </div> -->
                  </div>
                </div>
                <!-- 出参 -->
                <div v-if="tool.key == 'variableOut' && formData.parameterOut" :class="tool.key+'-rightBox'">
                  <div class="parameter-in-set-list">
                    <div class="body-list">
                      <div style="height: 200px;position: relative;border-radius: 4px;overflow: hidden;">
                        <codeEditor class="codeEditor" :code="formData.parameterOut.body" prop="variableJsonCode" @change="outChangeHandle"></codeEditor>
                      </div>
                      <div style="margin: 16px 0;">
                        <el-button type="primary" size="mini" @click="formatBodyList('parameterOut', 'bodyList')">解析body</el-button>
                      </div>
                      <div class="body-list-box">
                        <div class="tip">以解析后的字段为准</div>
                        <table-tree
                          :option="bodyOutOption"
                          :formData="formData.parameterOut"
                          prop="bodyList">
                        </table-tree>
                      </div>
                    </div>
                  </div>
                </div>
                <!-- 描述 -->
                <div v-if="tool.key == 'desc'" :class="tool.key+'-rightBox'">
                  <div class="add-desc-button" @click="formData.description.push('逻辑描述');$forceUpdate();">
                    <svg aria-hidden="true">
                      <use xlink:href="#icon-jvs-xinjian-ffffff"></use>
                    </svg>
                  </div>
                  <div class="desc-list">
                    <div v-for="(desc, dix) in formData.description" :key="'desc-item-'+dix" class="desc-list-item">
                      <div class="top">
                        <div v-if="descEditIndex == dix" class="input-box">
                          <el-input v-model="formData.description[dix]" type="textarea" autosize></el-input>
                        </div>
                        <div v-else class="text">{{desc}}</div>
                      </div>
                      <div class="bottom" :style="descEditIndex == dix ? 'display: flex;' : ''">
                        <div class="button">
                          <div v-if="descEditIndex == dix" @click="descEditIndex=-1;">
                            <svg aria-hidden="true" style="fill: #6F7588;">
                              <use xlink:href="#jvs-ui-icon-a-zu49531"></use>
                            </svg>
                            <span style="color: #6F7588;">{{$langt('common.cancel')}}</span>
                          </div>
                          <div v-else @click="descEditIndex=dix;">
                            <svg aria-hidden="true" style="fill: #1E6FFF;">
                              <use xlink:href="#jvs-ui-icon-fuzhi1"></use>
                            </svg>
                            <span style="color: #1E6FFF;">{{$langt('table.edit')}}</span>
                          </div>
                        </div>
                        <div class="button">
                          <div @click="deleteOneOfList(dix, formData.description)">
                            <svg aria-hidden="true" style="fill: #FF194C;">
                              <use xlink:href="#jvs-ui-icon-shanchuyonghu"></use>
                            </svg>
                            <span style="color: #FF194C;">{{$langt('table.delete')}}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <!-- 日志回放 -->
                <div v-if="tool.key == 'relog'" :class="tool.key+'-rightBox'">
                  <div class="top-seach">
                    <el-input v-model="searchTid" prefix-icon="el-icon-search" :placeholder="`请输入tid进行搜索`" clearable @change="getReLogHandle" @keyup.enter.native="getReLogHandle"></el-input>
                  </div>
                  <div v-loading="reLogShow" class="relog-data-list">
                    <div v-for="(relog,  rlx) in reLogData" :key="relog.id" class="relog-data-list-item" @click="relogClick(relog)">
                      <div class="title">
                        <span>{{`tid:`}} {{relog.tid}}</span>
                      </div>
                      <div class="bottom">
                        <div v-for="(rcol, rcx) in reLogOption.column" :key="'relog-item-'+rcol.prop+'-'+rlx+'-'+rcx">
                          <span>{{rcol.label}}:</span> 
                          <span :class="{'value': true, 'success': (rcol.prop == 'status' && relog[rcol.prop]), 'error': (rcol.prop == 'status' && relog[rcol.prop] === false)}">{{ rcol.prop == 'status' ? (relog[rcol.prop] === false ? $langt('common.fail') : $langt('common.success')) : relog[rcol.prop]}}</span>
                          <span v-if="rcol.prop == 'totalExecutionTime'">ms</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <!-- 定时任务 -->
                <div v-if="tool.key == 'task'" :class="tool.key+'-rightBox'">
                  <div class="task-setting"  v-if="formData && formData.secret">
                    <setForm ref="setForm" :formData="formData"/>
                  </div>
                </div>
                <!-- 监听配置 -->
                <div v-if="tool.key == 'listen'" :class="tool.key+'-rightBox'">
                  <div class="listen-config" v-if="formData && formData.secret">
                    <listen-config ref="listenConfigForm" :formData="formData"></listen-config>
                  </div>
                </div>
                <!-- 安全设置 -->
                <div v-if="tool.key == 'safe'" :class="tool.key+'-rightBox'">
                  <jvs-form :option="safeConfigOption" :formData="safeConfigForm" @formChange="safeChange">
                    <template slot="ipsForm">
                      <div>
                        <div v-for="(sit, six) in safeConfigForm.ips" :key="'ip-'+six" style="margin-bottom: 10px;display: flex;align-items: center;">
                          <el-input v-model="safeConfigForm.ips[six]" size="mini" @change="safeChange"></el-input>
                          <i class="el-icon-delete" style="margin-left: 10px;color: #F56C6C;cursor: pointer;" @click="delIp(six)"></i>
                        </div>
                        <div>
                          <el-button icon="el-icon-circle-plus-outline" type="text" :disabled="safeConfigForm.ips.length > 9" @click="addIp">添加</el-button>
                          <span style="font-size: 12px;color: #C0C4CF;">（最多添加10个）</span>
                        </div>
                      </div>
                    </template>
                  </jvs-form>
                </div>
                <!-- 缓存设置 -->
                <div v-if="tool.key == 'cache'" :class="tool.key+'-rightBox'">
                  <jvs-form :option="basicOption" :formData="formData"></jvs-form>
                </div>
                <!-- doc文档 -->
                <div v-if="tool.key == 'doc' && formData.api" :class="tool.key+'-rightBox'">
                  <div class="iframe-box">
                    <iframe width="100%" height="100%" :src="formData.api" frameborder="0"></iframe>
                  </div>
                </div>
              </div>
            </div>  
          </div>
          <div class="place-line-box"></div>
        </div>
        <div style="flex: 1;height:100%;">
          <!-- 设计器 -->
          <easyflow
            v-if="showEFEditor && activeCanvas == 'main'"
            refs="efContainer"
            v-show="activeCanvas == 'main'"
            :style="activeCanvas == 'main' ? 'height: 100%;width: 100%;' : 'height: 0;width: 0;'"
            :activeCanvas="activeCanvas"
            :ruleInfo="formData"
            :originData="data"
            :nodeResult="nodeResult"
            :activeEl="activeElement"
            :disabled="modeUserInfo ? !(modeUserInfo.mode == 'DEV') : false"
            @addNode="addNode"
            @deleteNode="deleteNode"
            @saveGraph="saveGraph"
            @autoSave="autoSave"
            @fresh="freshCanvas"
            @nodeSelect="hideLeftMenu"
            @canvasClick="hideLeftMenu">
          </easyflow>
          <!-- 循环画布 -->
          <easyflow
            v-for="item in canvasList"
            v-if="activeCanvas == item.id"
            :style="activeCanvas == item.id ? 'height: 100%;width: 100%;' : 'height: 0;width: 0;'"
            :key="item.id+'-canvas-box'"
            :refs="item.id"
            :activeCanvas="activeCanvas"
            :ruleInfo="formData"
            :originData="item.data"
            :nodeResult="canvasResult"
            :activeEl="activeElement"
            :page="canvasPage[item.id]"
            :showPage="activeTid ? true : false"
            :disabled="modeUserInfo ? !(modeUserInfo.mode == 'DEV') : false"
            @addNode="addNode"
            @deleteNode="deleteNode"
            @currentChange="currentChange"
            @saveGraph="saveCanvasGraph"
            @autoSave="autoSave"
            @fresh="freshCanvaList"
            @nodeSelect="hideLeftMenu"
            @canvasClick="hideLeftMenu">
          </easyflow>
        </div>
        <!-- 操作提示 -->
        <div class="tool-bar">
          <el-popover
            placement="top"
            trigger="hover">
            <div class="oprate-info">
              <p>同时按住Ctrl(或Shift)和鼠标左键拖动鼠标进行分组</p>
            </div>
            <div slot="reference" class="oprate-icon">
              <svg aria-hidden="true">
                <use xlink:href="#jvs-ui-icon-yingyongbangzhu"></use>
              </svg>
            </div>
          </el-popover>
        </div>
      </div>
      <div v-if="revertLoading" class="loading-back"/>
    </div>
    <!-- <div v-if="currentTab === 'pageSetting'" class="content-box">
      <div class="page-setting">
        <div class="setting-form" v-if="!hasComponentId && ['Source_code_development_docking_logic', 'External_API_logic'].indexOf(formData.reqType) > -1">
          <div class="title">逻辑凭证</div>
          <div class="rule-token" v-if="formData && formData.id">
            <span>请在应用中心配置标识调用</span> -->
            <!-- <span>{{ formData.id.substring(0,5) + '****************' + formData.id.substring(formData.id.length - 5, formData.id.length) }}</span>
            <div>
              <el-tooltip effect="dark" content="复制" placement="top">
                <svg class="copy-icon" aria-hidden="true" @click="handleCopy(formData.id)">
                  <use xlink:href="#icon-copy"></use>
                </svg>
              </el-tooltip>
              <el-tooltip effect="dark" content="下载API示例代码" placement="top">
                <svg class="copy-icon" aria-hidden="true" @click="handleDownLoad(formData.id)">
                  <use xlink:href="#icon-download"></use>
                </svg>
              </el-tooltip>
            </div> -->
          <!-- </div>
        </div>
      </div>
    </div> -->
    <div v-if="currentTab === 'runLog'" class="content-box">
      <div class="page-setting">
        <run-log :secret="queryId"/>
      </div>
    </div>
    <!-- 参数 -->
    <div class="cont-bottom" v-if="showDataInfo">
      <jvs-table size="mini" :option="tableOption" :data="formData.parameterPos" @on-load="initTable" @delRow="delRowHandle">
        <template slot="menuLeft">
          <jvs-button size="mini" @click="addRowHandle">添加参数</jvs-button>
        </template>
        <template slot="key" slot-scope="scope">
          <el-input size="mini" v-model="scope.row.key"></el-input>
        </template>
        <template slot="info" slot-scope="scope">
          <el-input size="mini" v-model="scope.row.info"></el-input>
        </template>
        <template slot="classType" slot-scope="scope">
          <el-select size="mini" v-model="scope.row.classType" placeholder="请选择类型" style="width:100%;">
            <el-option
              v-for="(item, index) in classTypeList"
              :key="item+index+scope.row.key"
              :label="item"
              :value="item">
            </el-option>
          </el-select>
        </template>
        <template slot="defaultValue" slot-scope="scope">
          <el-input size="mini" v-model="scope.row.defaultValue"></el-input>
        </template>
      </jvs-table>
    </div>

    <!-- 条件填写  判断 结束 -->
    <el-dialog
      title="条件填写"
      append-to-body
      :visible.sync="paramDialogVisible"
      width="50%"
      :close-on-click-modal="false"
      :before-close="handleCloseParamDialog">
      <div v-if="paramDialogVisible">
        <div v-if="endJudgeShow">
          <el-input v-model="paramString"></el-input>
        </div>
        <div class="param-list-box" v-else>
          <div class="param-list-item" v-for="(item, index) in paramObjList" :key="'paramItem'+index">
            <el-select size="mini" v-model="item.operator" placeholder="请选择" v-if="index > 0" style="width:100%;">
              <el-option
                v-for="o in operatorList"
                :key="o.value+'group-'+index"
                :label="o.label"
                :value="o.value">
              </el-option>
            </el-select>
            <div class="param-list-item-left">
              <span v-if="index == 0">判断条件</span>
              <jvs-button v-else size="mini" @click="delOneGroup(index)">删除组</jvs-button>
            </div>
            <div class="param-list-item-right">
              <div class="param-list-item-right-item">
                <div v-for="(it, ix) in item.list" :key="'items'+ix">
                  <span style="margin: 0 10px;" v-if="ix == 0">(</span>
                  <el-input size="mini" v-model="it.str"></el-input>
                  <el-select size="mini" v-model="it.operator" placeholder="请选择" v-if="ix < item.list.length -1">
                    <el-option
                      v-for="o in operatorList"
                      :key="o.value+'pl'+index+'-'+ix"
                      :label="o.label"
                      :value="o.value">
                    </el-option>
                  </el-select>
                  <jvs-button size="mini" v-if="ix > 0" @click="delOneItem(item, ix)">删除</jvs-button>
                  <jvs-button size="mini" v-if="ix == (item.list.length - 1)" @click="addOneItem(item)">添加</jvs-button>
                  <span v-if="ix == (item.list.length - 1)" style="margin: 0 10px;">)</span>
                </div>
              </div>
            </div>
          </div>
          <jvs-button size="mini" @click="addOneGroup">添加一组</jvs-button>
        </div>
        <span slot="footer" class="dialog-footer">
          <jvs-button type="primary" @click="paramSubmit">确 定</jvs-button>
          <jvs-button @click="handleCloseParamDialog">取 消</jvs-button>
        </span>
      </div>
    </el-dialog>

    <!-- 设置变量 -->
    <el-dialog
      title="变量设置"
      class="form-fullscreen-dialog variable-set-dialog"
      :visible.sync="variableVisible"
      fullscreen
      append-to-body
      :close-on-click-modal="false"
      :before-close="variableClose">
      <variableSet v-if="variableVisible" :varialbeData="varialbeData" @variableSave="variableSave" />
    </el-dialog>
  </div>
</template>

<script>
import { getExtendTypes } from "@/api/application";
import eventBus from "../utils/eventBus";
import { setStore } from '@/util/store'
import { getTemplatejson, saveOrUpdateDesign, getClassType, testLJ, getTestLJ, enable, saveParameter, getCanvasRevert, runlogRevert, getRunLogById, getEncryptionList, getRegExpList, placecRule, getRuleListByApp } from '../api/rule'
import setForm from './design/form'
import easyflow from './easyflow/ef/butterFly'
import Item from "./easyflow/ItemPanel/item";
import variableSet from './design/variable'

import codeicon from '../const/img/代码模式.png'
import timeicon from '../const/img/定时.png'
import starticon from '../const/img/启用.png'
import testicon from '../const/img/测试.png'
import DesignHeader from "@/components/page-header/DesignHeader";
import DesignTool from "./components/designTool";
import codeEditor from './design/coder'

import './easyflow/ef/index.css'
import RunLog from "@/views/rule/views/components/runLog";
import { encryption } from "@/util/util";
import {deCodeKey} from "@/const/const"
import {getStore} from "@/util/store"
import ListenConfig from "@/views/rule/views/components/listenConfig";
import tableTree from '@/components/basic-assembly/tableTree';
export default {
  components: { RunLog, setForm, variableSet, easyflow, Item, DesignHeader, DesignTool, codeEditor, ListenConfig, tableTree },
  computed: {
    // 逻辑测试时的入参变量
    interface_constants_user_constants () {
      return this.$store.state.qlConstants.interface_constants_user_constants
    },
    ClassType () {
      return this.$store.state.ClassType
    },
    // 数据
    tableData () {
      return JSON.parse(JSON.stringify(this.$store.state.qlConstants.constants_user_variable)).concat(JSON.parse(JSON.stringify(this.$store.state.qlConstants.interface_constants_user_constants)))
    },
  },
  created () {
    if(this.$route.query.token) {
      this.$store.commit('SET_ACCESS_TOKEN', this.$route.query.token)
    }
    if (this.$route.query.id) {
      this.queryId = this.$route.query.id
      this.versionChange(this.queryId)
    }
    if(this.$route.query.name) {
      this.LjName = this.$route.query.name
    }
    if(this.$route.query.componentId && this.$route.query.componentId != 'undefined') {
      this.hasComponentId = true
      this.tabList = this.tabList.filter(item => {
        if(item.name != 'taskDesign') {
          return item
        }
      })
    }
    this.getExtendTypes()
    this.getEncryptionHandle()
    this.getRegExpListHandle()
    this.modeUserInfo = getStore({ name: 'modeUserInfo' })
    let windowChannel = new BroadcastChannel('tagNotice')
    windowChannel.addEventListener('message', event => {
      if(event.data == 'changeUserMode') {
        this.modeUserInfo = getStore({ name: 'modeUserInfo' })
      }
    })
    window.onbeforeunload = (e) => {
      e.returnValue = '关闭提示'
    }
  },
  data () {
    return {
      basicOption: { // 对应表单设置
        labelWidth: '90px',
        submitBtn: false,
        emptyBtn: false,
        cancal: false,
        btnHide: true,
        formAlign: 'top',
        column: [
          {
            label: '逻辑缓存时间(分钟)',
            prop: 'cacheMinute',
            type: 'inputNumber',
            min: 0,
            controlsposition: 'right',
            tips: {
              position: 'bottom',
              text: '大于 0 时才生效'
            }
          }
        ],
      },
      currentTab: 'design',
      tabList: [
        { name: 'design', label: '逻辑设计', icon: 'el-icon-set-up' },
        { name: 'runLog', label: '执行日志', icon: 'el-icon-document' },
      ],
      queryId: '', // 逻辑的唯一key
      LjName: '', // 逻辑名称
      // 是否正在拖动左侧的组件 1 没有动  2 已经拖动没有进入中间的界面  3拖动进入中间的页面 4 进入设计页面中的组件
      drag1: '1',
      dragCom: null, // 当前拖拽组件
      dialogVisible: false,
      designDrawingJson: null,
      // 逻辑详情信息
      formData: {
        enable: false,
        parameterPos: {},
      },
      graph: {}, // 图形
      version: '', // 版本号
      com: '', // 当前组件名
      startSelect: '', // 开始选择的组件
      startToolSelectShow: false,
      data: {},
      // 表格配置
      tableOption: {
        addBtn: false,
        editBtn: false,
        viewBtn: false,
        // menuAlign: 'center',
        cancal: false,
        column: [
          {
            label: '参数名',
            prop: 'key',
            slot: true
          },
          {
            label: '解释',
            prop: 'info',
            slot: true
          },
          {
            label: '类型',
            prop: 'classType',
            slot: true,
            dicData: [
              {label: '字符串', value: 'String'},
              {label: '时间', value: 'Date'},
              {label: '数字', value: 'Number'},
              {label: '布尔', value: 'Boolean'},
              {label: '集合', value: 'Array'},
              {label: '对象', value: 'Object'},
            ]
          },{
            label: '默认值',
            prop: 'defaultValue',
            slot: true
          },
          {
            label: '执行后的值',
            prop: 'result',
          }
        ]
      },
      showDataInfo: false, // 是否显示数据表格
      count: 1,
      lastItem: 'start', // 上一个节点,不算线
      // 判断的顺序
      Csort: 0,
      // 线的id后缀
      edgeSize: 0,
      // 当前选中的是 节点 还是 线  node/line
      acttype: '',
      // 选择的节点
      actNode: {},
      // 选择的线
      actEdge: {},
      mode: 'edit', // 当前模式
      addingEdge: null, // 添加的线
      noChangeLabel: '', // 修改前的label名称
      editPosition: {}, // 位置参数
      edge: {}, // 连线数据
      delLineBool: false, // 是否显示删除线的工具栏
      deleteLinePosition: {}, // 连线删除工具位置
      classTypeList: [], // 参数类型列表
      firstId: '', // 连线第一个点的id
      noChangeEdgeLabel: '', // 线修改前的label值
      paramDialogVisible: false, // 条件弹框
      // 条件
      paramString: '', // 结束
      paramObjList: [  // 判断
        {
          operator: '',
          list: [
            {
              str: '',
              operator: ''
            }
          ]
        }
      ],
      setType: '', // 设置条件的类型  判断线 还是  结束节点
      designModel: 'graph', // 当前模式  graph图形设计  code代码设计
      codeEditor: null, // 代码编译器
      operatorList: [
        {label: '与', value: '&'},
        {label: '或', value: '|'},
        // {label: '非', value: '!'}
      ],
      endTool: {
        top: 0,
        left: 0,
        functionName: ''
      },
      endJudgeShow: false,
      rightToolIcon: {
        codeIcon: codeicon,
        timeIcon: timeicon,
        startIcon: starticon,
        testIcon: testicon
      },
      needFreshBool: null,
      showEFEditor: false,
      needFreshStr: "",
      variableVisible: false, // 设置变量弹框
      variableJsonList: [],
      variableItems: [],
      varialbeData: null,
      testEnd: false,
      nodeResult: {},
      codeCon: undefined,
      hasComponentId: false,
      activeCanvas: 'main',
      canvasList: [],
      revertLoading: false,
      activeElement: null,
      canvasPage: {},
      activeTid: '',
      canvasResult: null,
      safeConfigForm: {
        accessToken: '',
        ips: []
      },
      safeConfigOption: {
        btnHide: true,
        formAlign: 'top',
        column: [
          {
            label: '自定义校验标识',
            prop: 'accessToken',
          },
          {
            label: 'IP地址（段）',
            prop: 'ips',
            formSlot: true
          }
        ]
      },
      typeLabel: '默认',
      modeUserInfo: null,
      showToolKey: '',
      leftMenuList: [
        {
          key: 'assembly',
          icon: 'jvs-ui-icon-zujian',
          text: '组件库'
        },
        {
          key: 'canvas',
          icon: 'jvs-ui-icon-duoxuan',
          text: '画布'
        },
        {
          key: 'variable',
          icon: 'jvs-ui-icon-xitong',
          text: '入参'
        },
        {
          key: 'variableOut',
          icon: 'jvs-ui-icon-huanjingbianliang',
          text: '出参',
          display: false
        },
        {
          key: 'desc',
          icon: 'jvs-ui-icon-xitongzidian',
          text: '描述'
        },
        {
          key: 'relog',
          icon: 'jvs-ui-icon-denglurizhi1',
          text: '日志回放'
        },
        {
          key: 'task',
          icon: 'jvs-ui-icon-lishijilu',
          text: '定时任务',
          display: false
        },
        {
          key: 'listen',
          icon: 'jvs-ui-icon-wangguanjiamihulve',
          text: '监听配置',
          display: false
        },
        {
          key: 'safe',
          icon: 'jvs-ui-icon-yingyongshouquanzhongxin',
          text: '安全设置',
          display: false
        },
        {
          key: 'cache',
          icon: 'jvs-ui-icon-gudingshijian',
          text: '缓存设置',
          display: false
        },
        {
          key: 'doc',
          icon: 'jvs-ui-icon-jibenxinxi',
          text: '逻辑文档',
          display: false
        }
      ],
      descEditIndex: -1,
      searchTid: '',
      reLogData: [],
      reLogShow: false,
      reLogOption: {
        column: [
          {
            label: '时间',
            prop: 'startTime'
          },
          {
            label: '耗时',
            prop: 'totalExecutionTime'
          },
          {
            label: '执行状态',
            prop: 'status',
            slot: true
          },
        ]
      },
      parameterInOption: {
        btnHide: true,
        formAlign: 'top',
        column: [
          {
            label: '请求地址',
            prop: 'url',
            prepend: location.origin + `/rule/openapi/{appIdentifier}/`,
            rules: [
              { required: true, message: '请输入请求地址', trigger: 'blur' }
            ],
          },
          {
            label: '请求方法',
            prop: 'method',
            type: 'radio',
            dicData: [
              {label: 'GET', value: 'GET'},
              {label: 'POST', value: 'POST'},
              {label: 'DELETE', value: 'DELETE'},
              {label: 'PUT', value: 'PUT'}
            ],
            rules: [
              { required: true, message: '请选择请求方法', trigger: 'change' }
            ],
          },
        ]
      },
      parameterInTab: 'Query',
      extendTypes: [],
      showOtherRuleBool: false,
      ruleSearchName: '',
      ruleSearchList: [],
      requestHeaderDicData: [
        'Host',
        'User-Agent',
        'Accept',
        'Accept-Language',
        'Accept-Encoding',
        'Connection',
        'Content-Type',
        'Content-Length',
        'Authorization',
        'Cookie',
        'Referer',
        'Origin',
        'If-None-Match',
        'If-Modified-Since',
        'Cache-Control',
        'Pragma',
      ],
      bodyOption: {
        rowKey: 'path',
        pathKey: 'key',
        hasChildren: {
          prop: 'inputType',
          value: 'array,object',
          add: 'object'
        },
        childrenType: 'childrenType',
        itemsPathEnd: '[0]',
        column: [
          {
            label: '字段名称',
            prop: 'key',
            width: '320px'
          },
          {
            label: '显示名称',
            prop: 'label'
          },
          {
            label: '字段描述',
            prop: 'explain'
          },
          {
            label: '字段类型',
            prop: 'inputType',
            type: 'select',
            dicData: [
              {label: 'string', value: 'string'},
              {label: 'integer', value: 'integer'},
              {label: 'boolean', value: 'boolean'},
              {label: 'array', value: 'array'},
              {label: 'object', value: 'object'},
              {label: 'number', value: 'number'}
            ]
          },
          {
            label: '必填',
            prop: 'necessity',
            type: 'switch',
            showCheck: true,
            disabledRow: true,
            width: '50px'
          },
          {
            label: '缓存key',
            prop: 'cache',
            type: 'switch',
            showCheck: true,
            disabledRow: true,
            width: '80px'
          },
          {
            label: '默认值',
            prop: 'defaultValue',
            disabledRow: true
          },
          {
            label: '校验规则',
            prop: 'rule',
            disabledRow: true,
            clearable: true,
            type: 'select',
            dicData: [],
            props: {
              label: 'name',
              value: 'expression'
            }
          },
        ]
      },
      bodyOutOption: {
        rowKey: 'path',
        pathKey: 'key',
        hasChildren: {
          prop: 'inputType',
          value: 'array,object',
          add: 'object'
        },
        childrenType: 'childrenType',
        itemsPathEnd: '[0]',
        column: [
          {
            label: '字段名称',
            prop: 'key',
            width: '320px'
          },
          {
            label: '显示名称',
            prop: 'label',
          },
          {
            label: '字段描述',
            prop: 'explain',
          },
          {
            label: '字段类型',
            prop: 'inputType',
            type: 'select',
            dicData: [
              {label: 'string', value: 'string'},
              {label: 'integer', value: 'integer'},
              {label: 'boolean', value: 'boolean'},
              {label: 'array', value: 'array'},
              {label: 'object', value: 'object'},
              {label: 'number', value: 'number'}
            ],
          },
          {
            label: '必填',
            prop: 'necessity',
            type: 'switch',
            showCheck: true,
            disabledRow: true,
            width: '50px'
          },
          {
            label: '脱敏正则',
            prop: 'encryptionExpress',
            type: 'select',
            dicData: [],
            disabledRow: true,
            display: {
              prop: 'inputType',
              value: 'string'
            }
          },
          {
            label: '默认值',
            prop: 'defaultValue',
            disabledRow: true
          },
        ]
      },
    }
  },
  mounted () {
    // 获取参数类型
    getClassType().then(res => {
      if(res.data.code == 0) {
        this.classTypeList = res.data.data
      }
    })
  },
  methods: {
    getRenderData (data) {
      let obj = {
        nodes: data.nodeList,
        edges: data.lineList
      }
      return obj
    },
    // editor赋值
    changeHandle (code) {
      this.codeCon = code
    },
    outChangeHandle (code) {
      if(code == 'error') {
        this.$set(this.formData.parameterOut, 'body', '')
      }else{
        this.$set(this.formData.parameterOut, 'body', code)
      }
    },
    // tab选择结果
    tabSelect(val) {
      this.currentTab = val
    },
    async handleSave (str, data) {
      let task = undefined
      if (this.$refs.setForm && this.$refs.setForm.length > 0) {
        task = JSON.parse(JSON.stringify(this.$refs.setForm[0].getSetFormData()))
        if (!task) {
          if(!str) {
            this.currentTab = 'taskDesign'
            this.$refs.designHeader.handleTabChange('taskDesign')
          }
          return
        }
        // if (task && !task.onTask) {
        //   task = undefined
        // }
      }
      this.formData.task = task
      this.formData.onTask = task ? task.onTask : false
      if(this.codeCon) {
        this.formData.parameterPos = this.codeCon.startsWith('{') ? JSON.parse(this.codeCon) : this.codeCon
        if(this.codeCon == 'error') {
          this.formData.parameterPos = {}
        }
      }
      this.needFreshBool = null
      this.needFreshStr = str || null
      await this.saveHandle(this.needFreshBool, this.needFreshStr, (this.activeCanvas == 'main' && data) ? data : this.data)
    },
    // 测试，对节点通过状态设置
    async testHandle () {
      this.needFreshBool = false
      eventBus.$emit('saveGraph', true)
      await this.handleSave('已自动保存')
      // this.versionChange(this.queryId)
      if(this.designModel == 'graph') {
        this.testEnd = false
        let paramTemp = {}
        // if(this.formData.parameterPos) {
        //   paramTemp = JSON.parse(JSON.stringify(this.formData.parameterPos))
        // }
        paramTemp.jvsAppId= this.formData.jvsAppId
        if(this.$route.query) {
          if(this.$route.query.jvsAppId) {
            paramTemp.jvsAppId= this.$route.query.jvsAppId
          }
          if(this.$route.query.componentId) {
            paramTemp.componentId = this.$route.query.componentId
          }
        }
        this.nodeResult = {clear: true}
        this.revertLoading = true
        this.activeCanvas = 'main'
        testLJ(this.$route.query.jvsAppId, this.formData.id, {variableMap: paramTemp}).then(res => {
          if(res.data && res.data.code == 0) {
            // console.log('开始测试-----------', res.data.data)
            if(this.formData.sync) {
              this.testRequest(res.data.data)
            }else{
              this.nodeResult = res.data.data
              if(res.data.data.tid) {
                this.activeTid = res.data.data.tid
              }
              this.testEnd = true
              this.revertLoading = false
            }
          }else{
            this.testEnd = true
            this.revertLoading = false
          }
        }).catch(e => {
          this.testEnd = true
          this.revertLoading = false
        })
      }
    },
    testRequest (id) {
      getTestLJ(this.$route.query.jvsAppId, id).then(res => {
        if(res.data.code == 0) {
          // console.log('测试结果：-------', res.data.data)
          if(typeof res.data.data == 'object') {
            if(res.data.data){
              this.nodeResult = res.data.data
              if(res.data.data.tid) {
                this.activeTid = res.data.data.tid
              }
              if(res.data.data.isEnd) {
                this.testEnd = true
                this.revertLoading = false
              }else{
                this.testRequest(id)
              }
            }
          }
        }
      })
    },
    // 版本号change
    versionChange (val) {
      getTemplatejson(this.$route.query.jvsAppId, this.queryId, val).then(res => {
        if(res.data.code == 0) {
          this.designInit(res)
        }
      })
    },
    designInit (res) {
      if(res.data.data.componentId) {
        this.hasComponentId = true
        this.tabList = this.tabList.filter(item => {
          if(item.name != 'taskDesign') {
            return item
          }
        })
      }
      if(res.data.data.description && typeof res.data.data.description == 'string') {
        if(res.data.data.description.startsWith('[') && res.data.data.description.endsWith(']')){
          res.data.data.description = JSON.parse(res.data.data.description)
        }else{
          res.data.data.description = [ res.data.data.description ]
        }
      }else{
        res.data.data.description = []
      }
      this.formData = res.data.data || {}
      if(res.data.data.openLogRecording === false) {
        this.$set(this.formData, 'openLogRecording', false)
      }else{
        this.$set(this.formData, 'openLogRecording', true)
      }
      this.formatTabList(res.data.data)
      // 模式
      if(this.formData.designModel) {
        this.designModel = this.formData.designModel
      }
      // 代码
      if(this.formData.executeCode && this.codeEditor) {
        this.codeEditor.setValue(this.formData.executeCode)
      }
      if(this.formData.designDrawingJson) {
        this.data = JSON.parse(this.formData.designDrawingJson)
        if(!this.data.nodeList || this.data.nodeList.length == 0) {
          this.data = {
            nodes: [
              {
                id: 'start',
                name: '开始',
                type: 'START',
                left: '350px',
                top: '20px',
                ico: 'el-icon-video-play',
                state: ''
              },
            ],
            lineList: []
          }
        }
        if(this.data.ergodicCanvas) {
          this.canvasList = []
          this.canvasPage = {}
          for(let k in this.data.ergodicCanvas) {
            let name = this.getCanvasLabel(k)
            if(name) {
              this.canvasList.push({
                id: k,
                name: name,
                data: this.data.ergodicCanvas[k]
              })
              this.canvasPage[k] = {
                currentPage: 1,
                total: 0
              }
            }
          }
          this.viewCanvas(this.activeCanvas)
        }
      }
      if(this.formData.parameterPos) {
        this.varialbeData = (typeof this.formData.parameterPos == 'object') ? JSON.stringify(this.formData.parameterPos) : this.formData.parameterPos
        this.codeCon = this.varialbeData
        this.$forceUpdate()
      }
      if(this.formData.reqType == 'External_API_logic') {
        if(!this.formData.apiCheckDto) {
          this.$set(this.formData, 'apiCheckDto', {ips: '', accessToken: ''})
        }
        if(!this.formData.parameterOut) {
          this.$set(this.formData, 'parameterOut', {bodyList: []})
        }
        if(!this.formData.parameterOut.bodyList) {
          this.$set(!this.formData.parameterOut, 'bodyList', [])
        }
        this.$set(this.safeConfigForm, 'accessToken', this.formData.apiCheckDto.accessToken)
        this.$set(this.safeConfigForm, 'ips', this.formData.apiCheckDto.ips.split(','))
      }
      if(!this.formData.parameterIn) {
        this.$set(this.formData, 'parameterIn', {bodyList: []})
      }
      if(!this.formData.parameterIn.bodyList) {
        this.$set(this.formData.parameterIn, 'bodyList', [])
      }
      this.$nextTick(() => {
        this.showEFEditor = true
      })
      if(this.$refs.setForm && this.$refs.setForm.length > 0) {
        this.$refs.setForm[0].init(this.formData)
      }
      this.$forceUpdate()
    },
    // 展开收起 参数显示
    openCloseData () {
      let bool = this.showDataInfo
      this.showDataInfo = !bool
    },
    // 添加参数
    addRowHandle () {
      this.formData.parameterPos.push({
        classType: '',
        defaultValue: '',
        info: '',
        key: '',
        result: ''
      })
      this.$forceUpdate()
    },
    // 删除参数
    delRowHandle (row) {
      let index = -1
      for(let i in this.formData.parameterPos) {
        if(this.formData.parameterPos[i].key == row.key) {
          index = i
        }
      }
      if(index != -1) {
        this.formData.parameterPos.splice(index, 1)
      }
    },
    // 返回
    goBack () {
      this.$router.push({path: '/ruleList'})
    },
    // 定时设置
    setFromHandle () {
      this.dialogVisible = true
    },
    // 关闭弹框
    handleClose () {
      this.dialogVisible = false
    },
    // 子级通知关闭
    closeHandle (bool) {
      if(bool) {
        this.handleClose()
        this.versionChange(this.formData.version)
      }
    },
    // 保存
    async saveHandle (needFresh, str, data) {
      this.formData.key = this.queryId
      this.formData.designModel = this.designModel
      if(this.designModel == 'graph') {
        let nodeAll = JSON.parse(JSON.stringify(data))
        for(let i in nodeAll.nodeList) {
          nodeAll.nodeList[i].testData = ""
          if(nodeAll.nodeList[i].data && nodeAll.nodeList[i].data.parameters) {
            for(let j in nodeAll.nodeList[i].data.parameters) {
              if(nodeAll.nodeList[i].data.parameters[j].customOptionValue) {
                // 默认值
                if(typeof nodeAll.nodeList[i].data.parameters[j].defaultValue != 'string') {
                  let tdefault = {
                    data: JSON.stringify(nodeAll.nodeList[i].data.parameters[j].defaultValue)
                  }
                  let tdefaultEnc = encryption({
                    data: tdefault,
                    key: deCodeKey,
                    param: ["data"]
                  })
                  nodeAll.nodeList[i].data.parameters[j].defaultValue = tdefaultEnc.data
                }
                // options
                // if(typeof nodeAll.nodeList[i].data.parameters[j].options != 'string') {
                //   let toptions = {
                //     data: JSON.stringify(nodeAll.nodeList[i].data.parameters[j].options)
                //   }
                //   let toptionsEnc = encryption({
                //     data: toptions,
                //     key: deCodeKey,
                //     param: ["data"]
                //   })
                //   nodeAll.nodeList[i].data.parameters[j].options = toptionsEnc.data
                // }
              }
            }
          }
        }
        nodeAll.ergodicCanvas = this.getErgodicCanvas()
        this.formData.designDrawingJson = JSON.stringify(nodeAll)
      }else{
        let code = this.codeEditor.getValue()
        this.formData.executeCode = code
      }
      let subData = Object.assign({}, this.formData)
      if(typeof this.formData.description == 'object' && this.formData.description instanceof Array) {
        subData.description = JSON.stringify(this.formData.description)
      }
      await saveOrUpdateDesign(Object.assign({relationDesignIds: this.getRelationList()}, subData)).then(res => {
        if(res.data.code == 0) {
          if(needFresh === false) {
            if(str) {
              this.$notify({
                title: '提示',
                message: str,
                position: 'bottom-right',
                type: 'success'
              });
            }
            return false
          }
          if(str) {
            this.$notify({
              title: '提示',
              message: str,
              position: 'bottom-right',
              type: 'success'
            });
          }else{
            this.$notify({
              title: '提示',
              message: '保存成功',
              position: 'bottom-right',
              type: 'success'
            });
          }
          this.versionChange(this.queryId)
        }
      })
    },
    // 通知保存
    emitSave (bool) {
      if(bool) {
        this.needFreshBool = null
        this.needFreshStr = null
        this.saveHandle(this.needFreshBool, this.needFreshStr, this.data)
      }
    },
    // 设计的数据保存
    saveGraph (data) {
      if(this.activeCanvas == 'main') {
        this.data = data
      }
    },
    emitClose (bool) {
      if(bool) {
        this.enable = false
        this.variableItems = []
        setStore({ name: 'variableItems', content: this.variableItems, type: 'session' })
        this.goBack()
      }
    },
    // 启用  禁用
    endisabledHandle (bool) {
      let str = '启用成功'
      if(bool) {
        str = '禁用成功'
      }
      enable(this.formData.secret).then(res => {
        if(res.data.code == 0) {
          this.$notify({
            title: '提示',
            message: str,
            position: 'bottom-right',
            type: 'success'
          });
          this.formData.enable = !bool
        }
      })
    },
    // 初始化参数表格
    initTable () {
      if(!this.formData.parameterPos) {
        this.formData.parameterPos = []
      }
    },
    // 设置判断线的条件
    setLineparamHandle () {
      console.log(this.actEdge)
      if(this.actEdge.paramObjList) {
        this.paramObjList = this.actEdge.paramObjList
      }
      this.setType = 'line'
      this.endJudgeShow = false
      this.paramDialogVisible = true
    },
    // 关闭条件弹框
    handleCloseParamDialog () {
      this.paramDialogVisible = false
      this.paramObjList = []
      this.paramString = ""
    },
    // 提交条件
    paramSubmit () {
      if(this.setType == 'line') {
        this.actEdge.paramObjList = this.paramObjList
        let str = ""
        for(let p in this.paramObjList) {
          str += `${this.paramObjList[p].operator}(`
          for(let l in this.paramObjList[p].list) {
            str += `${this.paramObjList[p].list[l].str}${this.paramObjList[p].list[l].operator}`
          }
          str += ')'
        }
        this.actEdge.label = str
        this.graph.updateItem(this.actEdge.id, this.actEdge)
      }else{
        if(this.actNode.nodeType == 'end') {
          this.actNode.paramString = this.paramString
        }else{
          this.actNode.paramObjList = this.paramObjList
        }
        this.graph.updateItem(this.actNode.id, this.actNode)
      }
      this.handleCloseParamDialog()
    },
    // 切换模式
    changeModel () {
      let temp = this.designModel
      if(temp == 'graph') {
        temp = 'code'
        this.showEFEditor = false
      }else{
        temp = 'graph'
        this.showEFEditor = true
      }
      this.designModel = temp
    },
    // 添加一项条件
    addOneItem (item) {
      item.list.push({str: '', operator: ''})
    },
    // 删除一项条件
    delOneItem (item, index) {
      item.list.splice(index, 1)
    },
    // 添加一组
    addOneGroup () {
      this.paramObjList.push({
        operator: '',
        list: [{str: '', operator: ''}]
      })
    },
    // 删除一组
    delOneGroup (index) {
      this.paramObjList.splice(index, 1)
    },
    // 设置变量
    openVariable () {
      this.variableVisible = true
    },
    // 关闭设置变量
    variableClose () {
      this.variableVisible = false
    },
    // 保存变量
    variableSave (data) {
      this.variableJsonList = []
      this.formatJson(data, this.variableJsonList)
      saveParameter(this.$route.query.jvsAppId, {
        secret: this.queryId,
        parameterPo: data.length > 0 ? this.variableJsonList[0] : {}
      }).then(res => {
        if(res.data.code == 0) {
          this.variableItemsHandle()
          this.$notify({
            title: '提示',
            message: '保存成功',
            position: 'bottom-right',
            type: 'success'
          });
          this.variableClose()
        }
      })
    },
    formatJson (list, temp) {
      for(let i in list) {
        let tp = {
          key: list[i].key,
          classType: list[i].classType,
          description: list[i].description
        }
        if(['布尔', '数字', '浮点数', '字符串'].indexOf(list[i].classType) > -1) {
          tp.value = list[i].value
        }else{
          if(list[i].value == '[]' || list[i].value == '{}' || !list[i].value) {
            tp.value = null
          }else{
            tp.value = JSON.parse(list[i].value)
          }
        }
        if(list[i].children && list[i].children.length > 0) {
          tp.children = []
          this.formatJson(list[i].children, tp.children)
        }
        temp.push(tp)
      }
    },
    getVariableItems (list, parent) {
      for(let i in list) {
        let pt = parent.parent ? JSON.parse(JSON.stringify(parent.parent)) : []
        if(parent.key != 'root') {
          pt.push(parent.key)
        }
        let tp = {
          key: list[i].key,
          parent: pt,
          description: list[i].description
        }
        list[i].parent = pt
        if(list[i].children && list[i].children.length > 0) {
          this.getVariableItems(list[i].children, list[i])
        }
        this.variableItems.push(tp)
      }
    },
    variableItemsHandle () {
      let tp = JSON.parse(JSON.stringify(this.variableJsonList))
      tp[0].parent = []
      this.getVariableItems(tp[0].children, tp[0])
      setStore({ name: 'variableItems', content: this.variableItems, type: 'session' })
    },
    freshHandle () {
      this.variableItems = []
      setStore({ name: 'variableItems', content: this.variableItems, type: 'session' })
    },
    // 回放
    revertLogHandle (result, tid) {
      this.activeTid = ''
      this.revertLoading = true
      this.nodeResult = {clear: true}
      this.nodeResult = result
      if(tid) {
        this.activeTid = tid
      }
      this.viewCanvas(this.activeCanvas)
      let _this = this
      setTimeout(()=>{
        _this.revertLoading = false
      }, 300)
    },
    // 添加节点事件
    addNode (node) {
      if(node.data && node.data.functionName == '循环容器') {
        this.canvasList.push({
          id: node.id,
          name: node.name ? node.name : node.data.name,
          data: {}
        })
      }
      this.$forceUpdate()
    },
    // 删除节点
    deleteNode (id) {
      this.checkCanvas(id)
      this.$forceUpdate()
    },
    getCanvasLabel (id) {
      let name = ''
      this.data.nodeList.filter(node => {
        if(node.id == id) {
          name = (node.data && node.data.name) ? node.data.name : node.name
        }
      })
      if(!name) {
        if(this.data.ergodicCanvas) {
          for(let i in this.data.ergodicCanvas) {
            if(this.data.ergodicCanvas[i].nodeList) {
              this.data.ergodicCanvas[i].nodeList.filter(node => {
                if(node.id == id) {
                  name = (node.data && node.data.name) ? node.data.name : node.name
                }
              })
            }
          }
        }
      }
      if(!name) {
        for(let i in this.canvasList) {
          if(this.canvasList[i].data && this.canvasList[i].data.nodeList) {
            this.canvasList[i].data.nodeList.filter(node => {
              if(node.id == id) {
                name = (node.data && node.data.name) ? node.data.name : node.name
              }
            })
          }
        }
      }
      return name
    },
    // 切换画布
    viewCanvas (cav) {
      if(this.activeCanvas != cav) {
        this.activeCanvas = cav
      }
      if(this.activeTid && cav != 'main') {
        this.currentChange()
      }
      this.$forceUpdate()
    },
    saveCanvasGraph (data) {
      if(this.activeCanvas != 'main') {
        this.canvasList.filter(item => {
          if(item.id == this.activeCanvas) {
            item.data = data
          }
        })
      }
      this.$forceUpdate()
    },
    // 刷新画布名称
    freshCanvas (data, act) {
      if(data.nodeList) {
        this.$set(this.data, 'nodeList', data.nodeList)
      }
      this.canvasList.filter(item => {
        item.name = this.getCanvasLabel(item.id)
      })
      if(act){
        this.activeElement = JSON.parse(JSON.stringify(act))
      }else{
        this.activeElement = null
      }
      this.$forceUpdate()
    },
    freshCanvaList (data, act) {
      this.canvasList.filter(item => {
        if(data.nodeList && item.id == this.activeCanvas) {
          item.data = data
        }
        item.name = this.getCanvasLabel(item.id)
      })
      if(act){
        this.activeElement = JSON.parse(JSON.stringify(act))
      }else{
        this.activeElement = null
      }
      this.$forceUpdate()
    },
    getErgodicCanvas () {
      let obj = {}
      this.canvasList.filter(item => {
        let data = JSON.parse(JSON.stringify(item.data))
        for(let i in data.nodeList) {
          if(data.nodeList[i].data && data.nodeList[i].data.parameters) {
            for(let j in data.nodeList[i].data.parameters) {
              if(data.nodeList[i].data.parameters[j].customOptionValue) {
                // 默认值
                if(typeof data.nodeList[i].data.parameters[j].defaultValue != 'string') {
                  let tdefault = {
                    data: JSON.stringify(data.nodeList[i].data.parameters[j].defaultValue)
                  }
                  let tdefaultEnc = encryption({
                    data: tdefault,
                    key: deCodeKey,
                    param: ["data"]
                  })
                  data.nodeList[i].data.parameters[j].defaultValue = tdefaultEnc.data
                }
                // options
                // if(typeof data.nodeList[i].data.parameters[j].options != 'string') {
                //   let toptions = {
                //     data: JSON.stringify(data.nodeList[i].data.parameters[j].options)
                //   }
                //   let toptionsEnc = encryption({
                //     data: toptions,
                //     key: deCodeKey,
                //     param: ["data"]
                //   })
                //   data.nodeList[i].data.parameters[j].options = toptionsEnc.data
                // }
              }
            }
          }
        }
        obj[item.id] = data
      })
      return obj
    },
    async autoSave (data, bool) {
      if(this.activeCanvas != 'main') {
        this.saveCanvasGraph(data)
      }
      if(bool !== false) {
        await this.handleSave('已自动保存', data)
      }
    },
    getRelationList () {
      let list = []
      if(this.formData.designDrawingJson) {
        let designJson = JSON.parse(this.formData.designDrawingJson)
        let nodes = []
        let lines = []
        if(designJson.nodeList && designJson.nodeList.length > 0) {
          nodes = nodes.concat(designJson.nodeList)
        }
        if(designJson.lineList && designJson.lineList.length > 0) {
          lines = lines.concat(designJson.lineList)
        }
        if(designJson.ergodicCanvas) {
          let keys = Object.keys(designJson.ergodicCanvas)
          if(keys && keys.length > 0) {
            keys.filter(k => {
              if(designJson.ergodicCanvas[k]) {
                if(designJson.ergodicCanvas[k].nodeList && designJson.ergodicCanvas[k].nodeList.length > 0) {
                  nodes = nodes.concat(designJson.ergodicCanvas[k].nodeList)
                }
                if(designJson.ergodicCanvas[k].lineList && designJson.ergodicCanvas[k].lineList.length > 0) {
                  lines = lines.concat(designJson.ergodicCanvas[k].lineList)
                }
              }
            })
          }
        }
        if(nodes.length > 0) {
          nodes.filter(nit => {
            if(nit.data && nit.data.parameters && nit.data.parameters.length > 0) {
              nit.data.parameters.filter(pit => {
                if(['dataModelId', 'workflow'].indexOf(pit.key) > -1) {
                  if(nit.data.body && nit.data.body[pit.key]) {
                    if(list.indexOf(nit.data.body[pit.key]) == -1) {
                      list.push(nit.data.body[pit.key])
                    }
                  }else{
                    if(pit.defaultValue) {
                      if(list.indexOf(pit.defaultValue) == -1) {
                        list.push(pit.defaultValue)
                      }
                    }
                  }
                }
              })
            }
          })
        }
        if(lines.length > 0) {}
      }
      return list
    },
    currentChange () {
      let canvasPage = {}
      if(this.canvasPage) {
        for(let i in this.canvasPage) {
          canvasPage[i] = this.canvasPage[i].currentPage
        }
      }
      this.revertLoading = true
      getCanvasRevert(this.$route.query.jvsAppId, this.queryId, this.activeTid, this.activeCanvas, canvasPage).then(res => {
        if(res.data && res.data.code == 0) {
          this.revertLoading = false
          if(res.data.data.total) {
            this.$set(this.canvasPage[this.activeCanvas], 'total', res.data.data.total)
          }
          if(res.data.data.current) {
            this.$set(this.canvasPage[this.activeCanvas], 'current', res.data.data.current)
          }
          if(res.data.data.records && res.data.data.records.length > 0) {
            this.canvasResult = res.data.data.records[0].result
          }else{
            this.$set(this.canvasPage[this.activeCanvas], 'current', 1)
            this.$set(this.canvasPage[this.activeCanvas], 'total', 0)
          }
        }
      }).catch(e => {
        this.revertLoading = false
      })
    },
    checkCanvas (id) {
      let list = JSON.parse(JSON.stringify(this.canvasList))
      let temp = []
      let canvasIdList = []
      list.filter(lit => {
        canvasIdList.push(lit.id)
      })
      let ids = []
      for(let i in list) {
        if(list[i].id == id) {
          ids.push(list[i].id)
        }
        if(ids.indexOf(list[i].id) > -1 && list[i].data && list[i].data.nodeList && list[i].data.nodeList.length > 0) {
          list[i].data.nodeList.filter(node => {
            if(canvasIdList.indexOf(node.id) > -1) {
              ids.push(node.id)
            }
          })
        }
      }
      for(let cindex in list) {
        if(ids.indexOf(list[cindex].id) == -1) {
          temp.push(list[cindex])
        }
      }
      this.$set(this, 'canvasList', temp)
      this.$forceUpdate()
    },
    // 格式化 tabList
    formatTabList(data) {
      this.parameterInTab = 'Body'
      const { reqType } = data
      switch (reqType) {
        case 'External_API_logic':
          this.typeLabel = 'API触发'
          this.leftMenuList.filter(lit => {
            if(lit.key == 'safe') {
              lit.display = true
            }
            if(lit.key == 'variableOut') {
              lit.display = true
            }
            if(lit.key == 'cache') {
              lit.display = true
            }
            if(lit.key == 'doc') {
              lit.display = this.formData.api ? true : false
            }
          })
          this.parameterInTab = 'Query'
          break;
        case 'Source_code_development_docking_logic':
          this.typeLabel = '事件触发'
          this.leftMenuList.filter(lit => {
            if(lit.key == 'cache') {
              lit.display = true
            }
          })
          break;
        case 'Timing_logic':
          this.typeLabel = '定时触发'
          this.leftMenuList.filter(lit => {
            if(lit.key == 'task') {
              lit.display = true
            }
          })
          break;
        case 'Listening_logic':
          this.typeLabel = '监听触发'
          this.leftMenuList.filter(lit => {
            if(lit.key == 'listen') {
              lit.display = true
            }
          })
          break;
        default:
          this.typeLabel = '默认'
          break;
      }
      this.$forceUpdate()
    },
    safeChange () {
      this.$set(this.formData.apiCheckDto, 'accessToken', this.safeConfigForm.accessToken)
      this.$set(this.formData.apiCheckDto, 'ips', this.safeConfigForm.ips.join(','))
    },
    addIp () {
      this.safeConfigForm.ips.push('')
      this.$forceUpdate()
    },
    delIp (index) {
      this.safeConfigForm.ips.splice(index, 1)
      this.safeChange()
      this.$forceUpdate()
    },
    leftMenuenter (tool) {
      this.showToolKey = tool.key
      if(tool.key == 'relog') {
        this.getReLogHandle()
      }
      this.$forceUpdate()
    },
    hideLeftMenu () {
      this.showToolKey = ''
      this.descEditIndex = -1
    },
    deleteOneOfList (index, list) {
      list.splice(index, 1)
      if(this.descEditIndex == index) {
        this.descEditIndex = -1
      }
      this.$forceUpdate()
    },
    getReLogHandle () {
      if(this.formData) {
        let query = {}
        if(this.searchTid) {
          query.tid = this.searchTid
        }
        this.reLogShow = true
        runlogRevert(this.formData.jvsAppId, this.formData.secret, query).then(res => {
          if(res.data && res.data.code == 0) {
            this.reLogData = res.data.data
            this.reLogShow = false
          }
        }).catch(e => {
          this.reLogShow = false
        })
      }
    },
    relogClick (row) {
      getRunLogById(this.formData.jvsAppId, row.id).then(res => {
        if(res.data && res.data.code == 0 && res.data.data) {
          this.revertLogHandle(res.data.data.result, res.data.data.tid)
          this.searchTid = ''
        }
      })
    },
    // 获取参数类型
    getExtendTypes() {
      getExtendTypes().then(res => {
        if (res.data && res.data.code == 0 && res.data.data) {
          this.extendTypes = [...res.data.data]
        }
      })
    },
    // 获取脱敏正则列表
    getEncryptionHandle () {
      getEncryptionList().then(res => {
        if(res.data.code == 0 && res.data.data) {
          this.bodyOutOption.column.filter(item => {
            if(item.prop == 'encryptionExpress') {
              item.dicData = res.data.data
            }
          })
        }
      })
    },
    addLineHandle (key) {
      if(!this.formData.parameterIn[key]) {
        this.$set(this.formData.parameterIn, key, [])
      }
      this.formData.parameterIn[key].push({})
      this.$forceUpdate()
    },
    deleteLine (index, key) {
      this.formData.parameterIn[key].splice(index, 1)
      this.$forceUpdate()
    },
    // parameterInChange (form, item) {
    //   if(item.prop == 'url' && this.formData.parameterIn.url) {
    //     let list = []
    //     let urls = this.formData.parameterIn.url.split('/')
    //     this.$set(this.formData.parameterIn, 'pathList', [])
    //     for(let i in urls) {
    //       if(urls[i].includes('{')) {
    //         let str = urls[i].replace(/\{/g, ' ')
    //         str = str.replace(/\}/g, ' ')
    //         let tl = str.split(' ')
    //         tl.filter(tit => {
    //           if(tit && list.indexOf(tit) == -1) {
    //             list.push(tit)
    //           }
    //         })
    //       }
    //     }
    //     for(let l in list) {
    //       this.formData.parameterIn.pathList.push({
    //         key: list[l],
    //         explain: ''
    //       })
    //     }
    //     this.$forceUpdate()
    //   }
    // },
    formatBodyList (key, attr) {
      let list = []
      if(key == 'parameterIn') {
        if(this.codeCon && this.codeCon != 'error') {
          let json = JSON.parse(this.codeCon)
          this.eachObject(json, list)
          this.$set(this.formData[key], attr, list)
        }
      }
      if(key == 'parameterOut') {
        if(this.formData.parameterOut && this.formData.parameterOut.body) {
          let json = JSON.parse(this.formData.parameterOut.body)
          this.eachObject(json, list)
          this.$set(this.formData[key], attr, list)
        }
      }
      console.log(this.formData)
      this.$forceUpdate()
    },
    eachObject(obj, list, parent, parentItem) {
      if(typeof obj == 'object' && (obj instanceof Array)) {
        for(let index in obj) {
          if((obj[index] instanceof Array) ? (parentItem.childrenType == 'array') : (parentItem.childrenType == typeof obj[index])) {
            this.eachObject(obj[index], list, parent)
          }
        }
      }else{
        for(let k in obj) {
          if(typeof obj[k] == 'object' && (obj[k] instanceof Array)) {
            let tp = {
              explain: '',
              key: k
            }
            if(parent) {
              tp.path = (parent + '.' + k)
            }else{
              tp.path = k
            }
            tp.inputType = 'array'
            tp.disabled = true
            if(obj[k].length > 0) {
              tp.childrenType = typeof obj[k][0]
              if(typeof obj[k][0] == 'object' && obj[k][0] instanceof Array) {
                tp.childrenType = 'array'
              }
              if(tp.childrenType == 'object') {
                let items = {path: tp.path+'[0]',key: 'Items', inputType: tp.childrenType, children: [], childrenType: tp.childrenType}
                this.eachObject(obj[k], items.children, items.path, items)
                tp.children = [items]
              }else if(tp.childrenType == 'array') {
                let items = {path: tp.path+'[0]',key: 'Items', inputType: tp.childrenType, children: [], childrenType: tp.childrenType}
                let citem = {path: items.path+'[0]',key: 'Items', children: []}
                if(obj[k][0] && obj[k][0].length > 0) {
                  if(typeof obj[k][0][0] == 'object' && obj[k][0][0] instanceof Array) {
                    citem.inputType = 'array'
                    citem.childrenType = 'array'
                  }else{
                    citem.inputType = typeof obj[k][0][0]
                    citem.childrenType = citem.inputType
                  }
                }
                for(let c in obj[k]) {
                  if(citem.childrenType == 'array' ? (obj[k][c] instanceof Array) : (typeof obj[k][c] == citem.childrenType)) {
                    this.eachObject(obj[k][c], citem.children, citem.path, citem)
                  }
                }
                items.children = [citem]
                tp.children = [items]
              }else{
                let items = {path: tp.path+'[0]',key: 'Items', inputType: tp.childrenType}
                tp.children = [items]
              }
            }
            let index = -1
            list.filter((li, lx) => {
              if(li.key == tp.key) {
                index = lx
              }
            })
            if(index == -1) {
              list.push(tp)
            }
          }else{
            let tp = {
              explain: '',
              key: k
            }
            if(parent) {
              tp.path = (parent + '.' + k)
            }else{
              tp.path = k
            }
            if(typeof obj[k] == 'object') {
              tp.disabled = true
              tp.inputType = 'object'
              let keys = Object.keys(obj[k])
              if(keys && keys.length > 0) {
                tp.children = []
                this.eachObject(obj[k], tp.children, tp.path ? tp.path : k)
              }
            }else{
              tp.inputType = (typeof obj[k])
            }
            let index = -1
            list.filter((li, lx) => {
              if(li.key == tp.key) {
                index = lx
              }
            })
            if(index == -1) {
              list.push(tp)
            }
          }
        }
      }
    },
    // 获取所有的逻辑列表
    getAllRuleHandle () {
      let obj = {current: 1, size: 2000}
      if(this.ruleSearchName) {
        obj.name = this.ruleSearchName
      }
      getRuleListByApp(this.$route.query.jvsAppId, obj).then(res => {
        if(res.data && res.data.code == 0) {
          this.ruleSearchList = res.data.data.records
        }
      })
    },
    viewRule (row) {
      if(row.secret != this.$route.query.id) {
        this.$openUrl(`/rule-design-ui/#/ruleDesign?id=${row.secret}&name=${row.name}&jvsAppId=${this.$route.query.jvsAppId}`, '_blank')
      }
    },
    getRegExpListHandle () {
      getRegExpList().then(res => {
        if(res.data.code == 0 && res.data.data) {
          this.bodyOption.column.filter(item => {
            if(item.prop == 'rule') {
              item.dicData = res.data.data
            }
          })
        }
      })
    },
    getModeLabel (mode) {
      let str = ''
      switch(mode) {
        case 'DEV': str = '开发';break;
        case 'BETA': str = '测试';break;
        case 'GA': str = '正式';break;
        default: ;break;
      }
      return str
    },
    placeRuleHandle (row) {
      if(row.secret != this.$route.query.id) {
        placecRule(this.$route.query.jvsAppId, this.$route.query.id, row.secret).then(res => {
          if(res.data && res.data.code == 0) {
            if(res.data.data) {
              this.showEFEditor = false
              this.designInit(res)
              this.ruleSearchList.filter(item => {
                item.visible = false
              })
            }
          }
        })
      }
    },
    closeOther (rule) {
      this.ruleSearchList.filter(item => {
        if(item.id != rule.id) {
          item.visible = false
        }
      })
    }
  },
}
</script>

<style lang="scss" scoped>
p {
  padding: 0;
  margin: 0;
}
.other-rule-list{
  width: 176px;
  height: 302px;
  background: #F5F6F7;
  box-shadow: 0px 4px 10px 0px rgba(54,59,76,0.15);
  border-radius: 4px 4px 4px 4px;
  border: 1px solid #EEEFF0;
  box-sizing: border-box;
  padding-top: 12px;
  .search{
    margin: 0 8px;
    /deep/.el-input__inner{
      height: 32px;
      line-height: 32px;
    }
    /deep/.el-input__icon{
      line-height: 32px;
    }
  }
  .title-text{
    margin: 0 16px;
    margin-top: 14px;
    height: 18px;
    font-family: Microsoft YaHei-Regular, Microsoft YaHei;
    font-weight: 400;
    font-size: 14px;
    color: #363B4C;
    line-height: 18px;
  }
  .list-box{
    padding: 0 8px;
    height: calc(100% - 44px);
    overflow: hidden;
    overflow-y: auto;
    box-sizing: border-box;
    .list-item{
      margin-top: 15px;
      height: 36px;
      line-height: 36px;
      border-radius: 4px;
      font-family: Microsoft YaHei-Regular, Microsoft YaHei;
      font-weight: 400;
      font-size: 14px;
      color: #363B4C;
      text-indent: 8px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: pre;
      display: flex;
      align-items: center;
      .name-text{
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: pre;
      }
      .list-item-tool{
        height: 20px;
        line-height: 20px;
        align-items: center;
        display: flex;
        align-items: center;
        display: none;
        margin-right: 5px;
        .setting-icon{
          margin-left: 5px;
          width: 20px;
          height: 20px;
          cursor: pointer;
          display: flex;
          align-items: center;
          justify-content: center;
          box-sizing: border-box;
          svg{
            width: 16px;
            height: 16px;
            fill: #363B4C;
          }
          &:hover{
            background: #fff;
            border-radius: 4px;
            svg{
              fill: #1E6FFF;
            }
          }
        }
      }
      &:hover{
        background: #EEEFF0;
        .list-item-tool{
          display: flex;
        }
      }
      &.self{
         background: #DDEAFF;
        color: #1E6FFF;
      }
    }
  }
}
.rule-list-icon{
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
  &.open{
    background: #DDEAFF;
    svg{
      fill: #1E6FFF;
    }
  }
}
.split-line{
  width: 1px;
  height: 16px;
  background: #C2C5CF;
  margin-left: 10px;
  margin-right: 12px;
}
.test-button{
  width: 80px;
  height: 32px;
  background: #E4EDFF;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  margin-right: 16px;
  .icon{
    margin-right: 3px;
    width: 14px;
    height: 14px;
    border: 2px solid #1E6FFF;
    border-radius: 100%;
    box-sizing: border-box;
    position: relative;
    svg{
      width: 16px;
      height: 16px;
      fill: #1E6FFF;
      transform: rotateZ(-90deg);
      position: absolute;
      top: -3px;
      left: -2px;
    }
  }
  span{
    font-family: Microsoft YaHei-Regular, Microsoft YaHei;
    font-weight: 400;
    font-size: 14px;
    color: #1E6FFF;
  }
}
.cont {
  position: relative;
  background: #f0f2f5;
  height: 100%;
  overflow: hidden;
  overflow-y: auto;
  .content-box{
    display: flex;
    width: 52%;
    margin-left: 24%;
    height: calc(100vh - 56px);
    overflow: hidden;
    .page-setting{
      margin-top: 16px;
      width: 100%;
      .setting-form{
        border-radius: 6px;
        background-color: #ffffff;
        padding: 20px 30px;
        margin-bottom: 16px;
        .title {
          font-weight: bold;
          font-size: 16px;
          margin-bottom: 16px;
        }
        .rule-token{
          display: flex;
          align-items: center;
          justify-content: space-between;
          font-size: 14px;
          padding: 10px;
          border-radius: 6px;
          background-color: #ebebeb;
          .copy-icon{
            margin-left: 16px;
            width: 20px;
            height: 20px;
            fill: #545454;
            cursor: pointer;
          }
        }
        .codeEditor{
          width: 100%;
          height: 360px;
          position: relative;
        }
      }
      .copy-url{
        border-radius: 6px;
        margin-top: 20px;
        background-color: #ffffff;
        padding: 20px 30px;
        p {
          color: #b3b3b3;
        }
        .title {
          font-weight: bold;
          font-size: 16px;
          margin-bottom: 16px;
        }
        .copy-icon{
          margin-left: 16px;
          width: 20px;
          height: 20px;
          fill: #545454;
          cursor: pointer;
        }
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
  .cont-box {
    display: flex;
    height: calc(100% - 56px);
    justify-content: space-between;
    border-top: 1px solid #EEEFF0;
    position: relative;
    box-sizing: border-box;
    .openclose{
      position: absolute;
      left: 50%;
      bottom: 0;
      font-size: 20px;
      cursor: pointer;
      z-index: 2;
    }
    .cont-box-left {
      // border-right: 1px solid #e5e5e5;
      width: 200px;
      overflow: hidden;
      overflow-y: auto;
      scrollbar-width: none; /* firefox */
      -ms-overflow-style: none; /* IE 10+ */
      overflow-x: hidden;
      overflow-y: auto;
      padding-top: 7px;
      background: #F6F6F6;
      min-width: 200px;
      p {
        margin: 13px 0;
        // padding: 5px 0;
        font-size: 16px;
        text-align: center;
        cursor: pointer;
        height: 30px;
        display: flex;
        align-items: center;
        a{
          display: flex;
          align-items: center;
          margin-left: 20px;
        }
        img{
          display: inline-block;
          width: 20px;
          height: 20px;
          margin-right: 20px;
        }
      }
      p:hover{
        background: #EEEEEE;
        a{
          color: #3471FF;
        }
      }
    }
    .cont-box-left::-webkit-scrollbar {
      display: none; /* Chrome Safari */
    }
    .cont-box-right{
      // flex: 1;
      height: 100%;
      position: relative;
      background: #fff;
      display: flex;
      overflow: hidden;
      .left-menu{
        display: flex;
        flex-direction: column;
        height: 100%;
        background: linear-gradient( 146deg, #F2F7FF 0%, #FFFFFF 10%);
        -webkit-user-select: none; /* Safari 3.1+ */
        -moz-user-select: none; /* Firefox 2+ */
        -ms-user-select: none; /* IE 10+ */
        user-select: none; /* 标准语法 */
        .icon-tool-list{
          box-sizing: border-box;
          .icon-tool-item{
            position: relative;
            .icon{
              width: 56px;
              height: 68px;
              display: flex;
              flex-direction: column;
              align-items: center;
              justify-content: center;
              border-right: 1px solid #EEEFF0;
              box-sizing: border-box;
              cursor: pointer;
              svg{
                width: 18px;
                height: 18px;
                margin-bottom: 2px;
              }
              span{
                display: block;
                width: 24px;
                font-family: Microsoft YaHei-Regular, Microsoft YaHei;
                font-weight: 400;
                font-size: 12px;
                color: #6F7588;
                line-height: 16px;
              }
            }
            &.assembly{
              .icon{
                span{
                  width: 36px;
                }
              }
              .tool-box{
                padding: 16px 0;
              }
            }
            &.variable, &.relog{
              .tool-box{
                padding: 16px 0;
              }
            }
            &.variable{
              .tool-box{
                width: 950px;
              }
            }
            &.variableOut{
              .tool-box{
                width: 1100px;
              }
            }
            &.doc{
              .tool-box{
                width: calc(100vw - 56px);
                padding: 0;
                .title-box{
                  display: none;
                }
                .doc-rightBox{
                  position: absolute;
                  width: 100%;
                  height: 100%;
                  overflow: hidden;
                  .iframe-box{
                    width: calc(100% + 430px);
                    height: calc(100% + 111px + 50px);
                    margin-left: calc(-320px - 110px);
                    margin-top: calc(-56px - 55px);
                  }
                }
              }
            }
            .tool-box{
              position: fixed;
              top: 56px;
              left: 56px;
              z-index: 999;
              background-color: #fff;
              width: 272px;
              height: calc(100vh - 56px);
              background: linear-gradient( 180deg, #F7FAFF 0%, #FFFFFF 10%);
              padding: 16px 8px;
              border-top: 1px solid #EEEFF0;
              box-sizing: border-box;
              .title-box{
                display: flex;
                align-items: center;
                justify-content: space-between;
                margin: 0 8px;
                margin-bottom: 10px;
                .title-icon{
                  display: flex;
                  align-items: center;
                  svg{
                    width: 16px;
                    height: 16px;
                    cursor: pointer;
                  }
                }
              }
              h4{
                margin: 0;
                padding: 0;
                height: 20px;
                font-family: Source Han Sans-Bold, Source Han Sans;
                font-weight: 700;
                font-size: 14px;
                color: #363B4C;
                line-height: 20px;
              }
              .assembly-rightBox{
                height: calc(100% - 30px);
              }
              .canvas-rightBox{
                .canvas-tool{
                  width: 100%;
                  box-sizing: border-box;
                  background-color: #fff;
                  border-radius: 5px;
                  .canvas-tool-item{
                    padding: 0 30px;
                    height: 32px;
                    border-radius: 4px;
                    cursor: pointer;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    white-space: pre;
                    font-family: Source Han Sans-Regular, Source Han Sans;
                    font-weight: 400;
                    font-size: 14px;
                    color: #363B4C;
                    line-height: 32px;
                    box-sizing: border-box;
                    svg{
                      width: 12px;
                      height: 12px;
                      margin-right: 8px;
                    }
                  }
                  .canvas-tool-item:hover{
                    background-color: #DDEAFF;
                    color: #1E6FFF;
                  }
                  .canvas-tool-item.active{
                    background-color: #DDEAFF;
                    color: #1E6FFF;
                    svg{
                      fill: #1E6FFF;
                    }
                  }
                  >div{
                    .canvas-tool-item{
                      margin-top: 4px;
                    }
                  }
                }
              }
              .variable-rightBox, .variableOut-rightBox{
                margin-top: 2px;
                height: calc(100% - 32px);
                padding: 0 16px;
                overflow: hidden;
                overflow-y: auto;
                /deep/.el-input__inner{
                  font-family: Microsoft YaHei-Regular, Microsoft YaHei;
                  font-weight: 400;
                }
                .add-line-button{
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
                        margin: 0;
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
                .top{
                  /deep/.jvs-form{
                    .jvs-form-item{
                      .el-input-group__prepend{
                        background-color: #EEEFF0;
                        span{
                          font-family: Microsoft YaHei-Regular, Microsoft YaHei;
                          font-weight: 400;
                        }
                      }
                    }
                  }
                }
                .parameter-in-set-tab{
                  display: flex;
                  align-items: center;
                  border-bottom: 1px solid #EEEFF0;
                  margin-bottom: 16px;
                  .extend-tab-item{
                    padding-bottom: 7px;
                    font-family: Microsoft YaHei-Regular, Microsoft YaHei;
                    font-weight: 400;
                    font-size: 14px;
                    color: #6F7588;
                    cursor: pointer;
                    position: relative;
                    &+.extend-tab-item{
                      margin-left: 40px;
                    }
                    &.active{
                      font-weight: 700;
                      color: #1E6FFF;
                      &::after{
                        content: '';
                        position: absolute;
                        left: 0;
                        bottom: -1px;
                        width: 100%;
                        height: 2px;
                        background: #1E6FFF;
                        border-radius: 2px 0px 2px 0px;
                      }
                    }
                  }
                }
                .parameter-in-set-list{
                  .query-list, .header-list{
                    .header, .body-item{
                      display: flex;
                      align-items: center;
                      overflow: hidden;
                      span{
                        font-size: 14px;
                        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
                        font-weight: 400;
                      }
                      .el-input, .el-select{
                        flex: 1;
                        overflow: hidden;
                        margin-right: 16px;
                        .el-input__inner{
                          border: 0;
                        }
                      }
                    }
                    .body-item{
                      margin-top: 8px;
                    }
                  }
                  .body-list{
                    .body-list-box{
                      .tip{
                        margin-bottom: 8px;
                        height: 16px;
                        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
                        font-weight: 400;
                        font-size: 12px;
                        color: #6F7588;
                        line-height: 16px;
                      }
                      .el-table{
                        .el-table__header-wrapper{
                          tr th{
                            background-color: #EEEFF0;
                          }
                        }
                        .el-table__body-wrapper{
                          .el-table__body{
                            .el-input__inner{
                              border: 0;
                              background: #F5F6F7;
                            }
                            .el-table__row:hover{
                              .el-table__cell{
                                background-color: #fff;
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
                .var-code{
                  padding: 0 16px;
                  padding-bottom: 27px;
                  border-bottom: 1px solid #F5F6F7;
                  position: relative;
                  .top{
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                    box-sizing: border-box;
                    div{
                      height: 20px;
                      font-family: Source Han Sans-Regular, Source Han Sans;
                      font-weight: 400;
                      font-size: 14px;
                      color: #363B4C;
                      line-height: 20px;
                    }
                  }
                  .variable-box{
                    height: 316px;
                    margin-top: 8px;
                    box-sizing: border-box;
                    position: relative;
                    border: 1px solid #EEEFF0;
                    border-radius: 4px;
                  }
                  .bottom-svg{
                    position: absolute;
                    left: calc(50% - 12px);
                    bottom: -12px;
                    width: 24px;
                    height: 24px;
                  }
                }
                .rule-list{
                  height: calc(100% - 392px);
                  margin-top: 20px;
                  padding: 0 12px;
                  padding-right: 0;
                  .top{
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                    padding-left: 4px;
                    margin-bottom: 16px;
                    div{
                      font-family: Source Han Sans-Regular, Source Han Sans;
                      font-weight: 400;
                      font-size: 14px;
                      color: #363B4C;
                      line-height: 20px;
                    }
                  }
                  .rule-box{
                    padding-right: 12px;
                    height: calc(100% - 36px);
                    overflow: hidden;
                    overflow-y: auto;
                    box-sizing: border-box;
                    .variable-rule-list{
                      .variable-rule-list-item{
                        margin-bottom: 16px;
                        background: #F5F6F7;
                        border-radius: 4px;
                        .variable-rule-list-item-form{
                          padding-top: 12px;
                          .variable-rule-list-item-form-item{
                            display: flex;
                            align-items: center;
                            padding: 0 16px;
                            margin-bottom: 12px;
                            .label{
                              width: 94px;
                              font-family: Microsoft YaHei-Regular, Microsoft YaHei;
                              font-weight: 400;
                              font-size: 14px;
                              color: #6F7588;
                              margin-right: 12px;
                            }
                            /deep/.el-input{
                              .el-input__inner{
                                height: 32px;
                                background: #fff;
                                border-radius: 4px;
                                border: 0;
                              }
                            }
                          }
                        }
                        .variable-rule-list-item-button{
                          height: 34px;
                          display: flex;
                          align-items: center;
                          justify-content: center;
                          border-top: 1px solid #EEEFF0;
                          div{
                            cursor: pointer;
                            svg{
                              width: 16px;
                              height: 16px;
                              fill: #FF194C;
                              margin-right: 4px;
                            }
                            span{
                              font-family: Microsoft YaHei-Regular, Microsoft YaHei;
                              font-weight: 400;
                              font-size: 14px;
                              color: #FF194C;
                            }
                          }
                        }
                      }
                    }
                    .button{
                      display: flex;
                      align-items: center;
                      justify-content: center;
                      height: 32px;
                      background: #E4EDFF;
                      border-radius: 4px;
                      cursor: pointer;
                      span{
                        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
                        font-weight: 400;
                        font-size: 14px;
                        color: #1E6FFF;
                      }
                      svg{
                        width: 14px;
                        height: 14px;
                        fill: #1E6FFF;
                        margin-right: 5px;
                      }
                    }
                  }
                }
              }
              .desc-rightBox{
                padding: 0 8px;
                .add-desc-button{
                  width: 18px;
                  height: 18px;
                  background: #1E6FFF;
                  border-radius: 4px;
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  position: absolute;
                  top: 16px;
                  right: 16px;
                  cursor: pointer;
                  svg{
                    width: 13px;
                    height: 13px;
                  }
                }
                .desc-list{
                  .desc-list-item{
                    background: #F5F6F7;
                    border-radius: 4px;
                    &+.desc-list-item{
                      margin-top: 16px;
                    }
                    .top{
                      padding: 8px 16px;
                      cursor: pointer;
                      .text{
                        font-family: Source Han Sans-Regular, Source Han Sans;
                        font-weight: 400;
                        font-size: 14px;
                        color: #363B4C;
                        min-height: 20px;
                        line-height: 20px;
                        word-break: break-word;
                      }
                      .input-box{
                        /deep/.el-textarea{
                          .el-textarea__inner{
                            border: 0;
                          }
                        }
                      }
                    }
                    .bottom{
                      display: none;
                      align-items: center;
                      border-top: 1px solid #EEEFF0;
                      .button{
                        flex: 1;
                        height: 34px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        div{
                          cursor: pointer;
                          display: flex;
                          align-items: center;
                          justify-content: center;
                          svg{
                            margin-right: 4px;
                            width: 16px;
                            height: 16px;
                          }
                          span{
                            font-family: Microsoft YaHei-Regular, Microsoft YaHei;
                            font-weight: 400;
                            font-size: 14px;
                          }
                        }
                        &+.button{
                          border-left: 1px solid #EEEFF0;
                        }
                      }
                    }
                    &:hover{
                      border: 1px solid #1E6FFF;
                      .bottom{
                        display: flex;
                      }
                    }
                  }
                }
              }
              .relog-rightBox{
                height: calc(100% - 30px);
                .top-seach{
                  padding: 0 16px;
                  padding-top: 2px;
                  /deep/.el-input__inner{
                    border-color: #EEEFF0;
                    border-radius: 4px;
                    font-family: Source Han Sans-Regular, Source Han Sans;
                    font-weight: 400;
                    font-size: 14px;
                    color: #6F7588;
                    &::placeholder{
                      font-family: Source Han Sans-Regular, Source Han Sans;
                      font-weight: 400;
                      font-size: 14px;
                      color: #6F7588;
                    }
                  }
                  /deep/.el-input__icon{
                    font-size: 14px;
                    color: #6F7588;
                    font-weight: bold;
                  }
                }
                .relog-data-list{
                  height: calc(100% - 40px);
                  overflow: hidden;
                  overflow-y: auto;
                  .relog-data-list-item{
                    margin-top: 16px;
                    margin-left: 16px;
                    margin-right: 12px;
                    height: 115px;
                    background: #fff;
                    border-radius: 4px 4px 4px 4px;
                    border: 1px solid #EEEFF0;
                    overflow: hidden;
                    cursor: pointer;
                    .title{
                      height: 32px;
                      background: #F5F6F7;
                      border-radius: 4px 4px 0px 0px;
                      border-bottom: 1px solid #EEEFF0;
                      padding: 0 16px;
                      display: flex;
                      align-items: center;
                      justify-content: space-between;
                      span{
                        font-family: Source Han Sans-Medium, Source Han Sans;
                        font-weight: 500;
                        font-size: 14px;
                        color: #363B4C;
                        line-height: 32px;
                      }
                    }
                    .bottom{
                      div{
                        margin-top: 8px;
                        padding: 0 16px;
                        font-family: Source Han Sans-Regular, Source Han Sans;
                        font-weight: 400;
                        font-size: 12px;
                        color: #6F7588;
                        line-height: 17px;
                        .value{
                          margin-left: 12px;
                          &.success{
                            color: #1E6FFF;
                          }
                          &.error{
                            color: #FF194C;
                          }
                        }
                      }
                    }
                  }
                }
              }
              .listen-rightBox, .safe-rightBox{
                padding: 0 8px;
              }
              .other-rightBox{
                padding: 0 8px;
                /deep/.el-input-number{
                  .el-input-number__decrease, .el-input-number__increase{
                    display: none;
                  }
                  .el-input__inner{
                    padding: 0 16px;
                  }
                }
              }
            }
            &.show, &:hover{
              .icon{
                border-right: 0;
                border-top: 1px solid #EEEFF0;
                border-bottom: 1px solid #EEEFF0;
                svg{
                  fill: #1E6FFF;
                }
                span{
                  color: #1E6FFF;
                }
              }
            }
          }
        }
        .place-line-box{
          flex: 1;
          border-right: 1px solid #EEEFF0;
        }
      }
    }
  }
  .data-cont-box{
    height: calc(100vh - 395px);
  }
  .cont-bottom{
    height: 300px;
    padding: 0 20px;
    position: relative;
    background: #fff;
    padding-top: 10px;
    z-index: 9;
  }
}
.tool-bar{
  position: absolute;
  right: 10px;
  bottom: 10px;
  z-index: 200;
  .oprate-icon{
    width: 26px;
    height: 26px;
    cursor: pointer;
    border-radius: 50%;
    background: #D8D8D8;
    display: flex;
    align-items: center;
    justify-content: center;
    svg{
      width: 24px;
      height: 24px;
      fill: #fff;
    }
  }
}
.oprate-info{
  padding: 10px;
}
.other-tool{
  width: 150px;
}
.param-list-box{
  .param-list-item{
    display: flex;
    flex-wrap: wrap;
    .param-list-item-left{
      height: 48px;
      line-height: 48px;
    }
    .param-list-item-right{
      .param-list-item-right-item{
        >div{
          display: flex;
          align-items: center;
          margin: 10px 0;
        }
      }
      .el-input, .el-select{
        margin: 0 5px;
      }
    }
  }
}
.design-cont{
  .loading-back{
    position: absolute;
    width: 100%;
    height: 100%;
    z-index: 99;
    box-sizing: border-box;
    background-color: rgba(255, 255, 255, 0.8);
    background-image: url('../../../../public/jvs-ui-public/img/loading.gif');
    background-repeat: no-repeat;
    background-position: center;
    background-position: center;
  }
}
</style>
<style lang="scss">
.cont-top-item {
  p{
    .el-select{
      height: 32px;
      .el-input{
        height: 100%;
      }
    }
  }
}
.cont-bottom{
  border-top: 1px solid #dcdfe6;
  .el-table__body-wrapper{
    height: 235px;
    overflow: hidden;
    overflow-y: auto;
    scrollbar-width: none; /* firefox */
    -ms-overflow-style: none; /* IE 10+ */
    overflow-x: hidden;
    overflow-y: auto;
    .el-table__empty-block{
      min-height: 135px;
      height: 135px;
    }

  }
  .el-table__body-wrapper::-webkit-scrollbar {
    display: none; /* Chrome Safari */
  }
}
#container {
  position: relative;
}
.edit-input{
  width: 100px;
  height:40px;
  .el-input__inner{
    height: 40px;
    line-height: 40px;
  }
}
.toolTip-box {
  min-width: 100px;
  p{
    margin: 0;
    padding: 0;
    display: flex;
    align-items: center;
    span{
      width: 50%;
    }
  }
}
.ace_scrollbar {
  width: 0!important;
}
.variable-set-dialog{
  .el-dialog.is-fullscreen{
    .el-dialog__body{
      position: unset;
      width: 100%;
      padding: 30px 20px;
      box-sizing: border-box;
    }
  }
}
</style>
