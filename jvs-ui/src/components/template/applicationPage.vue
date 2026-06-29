<template>
  <div class="application-manage-page">
    <div class="back-box">
      <div class="left-menu">
        <div :class="{'menu-list-item': true, 'active': activeName === item.value}" v-for="(item, key) in menuList" :key="key" v-show="(['2', '7'].indexOf(item.value) > -1) ? (appInfo && appInfo.appRoles && appInfo.appRoles.indexOf('adminMember') > -1) : true" @click="handleMenuClick(item)" :tabindex="key">
          <span>{{ item.label }}</span>
        </div>
      </div>
      <div class="left-menu-bootom-line"></div>
      <div class="type-tab-bar">
        <div v-show="activeName === '1'" class="info-box basic-info">
          <div class="info-box-item">
            <div class="top">
              <img v-if="appInfo && appInfo.logo" :src="appInfo.logo" alt="">
              <div class="text-info">
                <div style="display: flex;align-items: center;">
                  <h3>
                    <span>{{appInfo.name}}</span>
                    <el-popover placement="bottom" trigger="hover" :content="$langt('file.copy')" popper-class="custom-right-tool-poper">
                      <b slot="reference" class="el-icon-copy-document" style="margin-left: 5px;cursor: pointer;" @click="copyAppLink"></b>
                    </el-popover>
                  </h3>
                  <span v-if="modeUserInfo ? modeUserInfo.mode == 'GA' : true" :class="{'status-tag': true, 'success': (appInfo && appInfo.isDeploy)}">{{appInfo && appInfo.isDeploy ? '已发布' : '未发布'}}</span>
                </div>
                <div class="desc-box">
                  <span class="desc-span">{{appInfo.createBy}}</span>
                  <span class="desc-slider-span"></span>
                  <span class="desc-span">{{appInfo.createTime}}</span>
                </div>
              </div>
              <div class="button-tool" v-if="appInfo && appInfo.appRoles && appInfo.appRoles.indexOf('adminMember') > -1">
                <jvs-button icon="el-icon-setting" @click="editApp" style="color: #1E6FFF;background: #E4EDFF;border-color: #E4EDFF;">编辑</jvs-button>
<!--                 <jvs-button v-if="appInfo && appInfo.appRoles && appInfo.appRoles.indexOf('adminMember') > -1 && (modeUserInfo ? modeUserInfo.mode == 'GA' : true)" icon="el-icon-setting" @click="viewRelation" style="color: #1E6FFF;background: #E4EDFF;border-color: #E4EDFF;">管理</jvs-button>-->
                <jvs-button v-if="appInfo && appInfo.appRoles && appInfo.appRoles.indexOf('adminMember') > -1 && !appInfo.isDeploy" icon="el-icon-delete" @click="delApp" style="color: #FF194C;background: #FFEDF1;border-color: #FFEDF1;">删除</jvs-button>
                <jvs-button v-if="appInfo && appInfo.appRoles && appInfo.appRoles.indexOf('adminMember') > -1 && (modeUserInfo ? modeUserInfo.mode == 'GA' : true) && appInfo.isDeploy" icon="el-icon-refresh-left" @click="handleUnload(appInfo)">卸载</jvs-button>
                <jvs-button v-if="appInfo && appInfo.appRoles && appInfo.appRoles.indexOf('adminMember') > -1 && (modeUserInfo ? modeUserInfo.mode == 'GA' : true) && (!appInfo.isDeploy)" type="primary" icon="el-icon-s-promotion" @click="handlePublish(appInfo)">发布</jvs-button>
              </div>
            </div>
            <div class="bottom-desc">{{appInfo.description}}</div>
          </div>
          <div v-if="modeUserInfo && modeUserInfo.mode == 'GA'" :class="{'status-info': true, 'isDeploy': (appInfo && appInfo.isDeploy)}">
            <div style="padding: 10px 16px;">
              <i class="el-icon-warning"></i>
              <span style="margin-left: 8px">{{ appInfo && appInfo.isDeploy ? '应用已发布，为可用状态，有权限的成员已经可以使用' : '应用暂未发布，访问应用仅可查看页面，无法提交数据' }}</span>
              <!-- <span v-if="appInfo && appInfo.isDeploy" @click="copyHandle(appInfo)" style="color:#3471ff;font-size:12px;margin-left:10px;cursor:pointer;">复制链接</span> -->
            </div>
            <div></div>
          </div>
          <div class="app-header">
            <div style="display: flex;align-items: center">
              <svg class="icon" aria-hidden="true">
                <use xlink:href="#icon-jvs-rongqi"></use>
              </svg>
              <span>应用凭证</span>
            </div>
          </div>
          <div class="app-info app-info-text">
            <div class="col-2">
              <div class="label">APP ID</div>
              <div class="con">
                <span>{{ appInfo.id }}</span>
                <b class="el-icon-copy-document" @click="copyStr(appInfo.id)"></b>
              </div>
            </div>
            <div class="col-2">
              <div class="label">APP Secret</div>
              <div v-if="secretKey" class="con">
                <span>{{ secretKey }}</span>
                <b class="el-icon-copy-document" @click="copyStr(secretKey)"></b>
              </div>
              <div v-else class="con" style="display: flex;align-items: center">
                <div>•••••••••••••</div>
                <b class="el-icon-view"  @click="handleDisplay"></b>
              </div>
            </div>
          </div>
          <div class="app-header">
            <div style="display: flex;align-items: center">
              <svg class="icon" aria-hidden="true">
                <use xlink:href="#icon-jvs-rongqi"></use>
              </svg>
              <span>权限管理</span>
            </div>
          </div>
          <div class="app-info app-info-text app-info-permanage">
            <div class="desc">
              <span>应用创建后,可以设置多个开发人员,同时进行开发和设计页面。</span>
              <span>为保证设计的信息安全性,轻应用开发人员设计时产生的数据都为测试数据,不具有具体的业务功能。</span>
              <span>清除所有测试数据时将自动把所有开发人员产生的数据全部删除。</span>
            </div>
            <div class="tips">
              <div class="title">
                <span class="el-icon-info" style="color: #1E6FFF;font-size: 14px;"></span>
                <span style="margin-left: 8px;">轻应用介绍</span>
              </div>
              <div class="con-box">
                <div class="secTitle">
                  <span>通过界面化拖拽或灵活简单的配置完成整体业务</span>
                </div>
                <ul class="tips-list">
                  <li>
                    <span class="dot"></span>
                    <span>分别对逻辑、表单、列表、工作流逻辑等配置化进行调整快速应对业务变化</span>
                  </li>
                  <li>
                    <span class="dot"></span>
                    <span>支持源代码二次接入轻应用满足高级定制化需求，使应用更加灵活</span>
                  </li>
                </ul>
              </div>
            </div>
            <div class="tips">
              <div class="title">
                <span class="el-icon-info" style="color: #1E6FFF;font-size: 14px;"></span>
                <span style="margin-left: 8px;">温馨提示</span>
              </div>
              <div class="con-box">
                <ul class="tips-list">
                  <li>
                    <span class="dot"></span>
                    <span>数据模型的数据删除为逻辑删除</span>
                  </li>
                  <li>
                    <span class="dot"></span>
                    <span>设置多个开发人员可以提升开发速度</span>
                  </li>
                  <li>
                    <span class="dot"></span>
                    <span>轻应用测试版本和正式版本建议分为不同的物理环境</span>
                  </li>
                  <li>
                    <span class="dot"></span>
                    <span>轻应用中的相同字段升级时变更类型后需要同步调整数据</span>
                  </li>
                  <li>
                    <span class="dot"></span>
                    <span>轻应用测试版本和正式版本中不能新增或删除设计表单、列表、逻辑等</span>
                  </li>
                  <li>
                    <span class="dot"></span>
                    <span>轻应用测试版本和正式版本可以临时修改设计，修改的设计将在下一次版本升级被覆盖</span>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>
        <div v-if="activeName === '2'" class="info-box" style="height: calc(100% - 10px);">
          <app-permission :appInfo="appInfo" :modeUserInfo="modeUserInfo" @addUser="addUser"/>
        </div>
        <div v-if="activeName === '3'" :class="{'info-box info-page-list': true}">
          <div class="type-list">
            <div v-for="tp in typeList" :key="tp.value" :class="{'type-list-item': true, 'active': tp.value == activeType}" @click="typeChange(tp)">
              <span>{{tp.label}}</span>
            </div>
          </div>
          <jvs-table :key="activeType" :class="{'show-all-design-page-table': true}" :page="designPage" :loading="alluseLoading" :option="pageOption" :data="pageList" @on-load="getAllUseHandle(appId)" @search-change="designSearchChange">
            <template slot="menu" slot-scope="scope">
              <jvs-button size="mini" type="text" style="color: #F56C6C;" @click="delRowHandle(scope.row)">删除</jvs-button>
            </template>
          </jvs-table>
        </div>
        <div v-if="activeName === '4'" class="info-box">
          <div class="tips" style="margin-top: 0;">
            <div class="title">
              <span class="el-icon-info" style="color: #1E6FFF;font-size: 14px;"></span>
              <span style="margin-left: 8px;">审批流程</span>
            </div>
            <div class="con-box" style="border-radius: 4px 4px 0 0;">
              <div class="secTitle">
                <span>此处创建流程，需要使用“业务逻辑”进行启动流程。</span>
              </div>
              <ul class="tips-list">
                <li>
                  <span class="dot"></span>
                  <span>1、流程修改保存后，需要“发布”才会生效。</span>
                </li>
                <li>
                  <span class="dot"></span>
                  <span>2、新发起的流程才会使用发布后的设置，执行中的流程会使用之前保存的设置。</span>
                </li>
                <li>
                  <span class="dot"></span>
                  <span>3、不根据已有数据模型创建流程时，会创建一个新的数据模型。</span>
                </li>
              </ul>
            </div>
          </div>
          <div class="flow-button-div" style="margin-top: 15px;display: flex;align-items: center;">
            <jvs-button class="add-button" type="primary" icon="el-icon-plus" @click="openModelList">根据模型创建</jvs-button>
            <jvs-button class="add-button" type="primary" icon="el-icon-plus" @click="handleAddOA()">直接创建</jvs-button>
            <jvs-button class="add-button" type="primary" icon="el-icon-setting" @click="handleTask()">待办提醒设置</jvs-button>
            <jvs-button class="add-button plain" type="primary" @click="replySetting">快捷回复设置</jvs-button>
          </div>
          <div style="margin-top: 15px;display: flex;align-items: center;padding: 0 10px;width: 400px;">
            <div style="width: 120px;font-size: 13px;">流程名称</div>
            <el-input v-model="flowName" clearable size="mini" style="margin-right: 10px;"></el-input>
            <el-button type="primary" size="mini" @click="flowPage.current=1;getAllWorkflow();">查询</el-button>
            <el-button size="mini" @click="flowName='';">重置</el-button>
          </div>
          <div class="card-list" style="height: calc(100% - 187px - 51px - 43px - 16px - 48px);">
            <div class="card-item" v-for="item in flowList" :key="'rule-item-'+item.id">
              <div class="card-header">
                <div class="card-header-left">
                  <div class="item-icon-box">
                    <svg aria-hidden="true" class="item-icon">
                      <use :xlink:href="'#' + 'icon-layers'"></use>
                    </svg>
                  </div>
                </div>
                <div class="card-header-right">
                  <span :style="'margin-right: 14px;'+ (item.published ? 'color: #36B452 ;' : 'color: #6F7588;')">{{item.published ? '已发布' : '未发布'}}</span>
                </div>
              </div>
              <h5>
                <span>{{item.name}}</span>
              </h5>
              <section :title="item.description">{{item.description}}</section>
              <div class="tool">
                <jvs-button size="mini" type="primary" icon="el-icon-office-building" @click="designFlow(item)">设计</jvs-button>
                <jvs-button v-if="modeUserInfo ? modeUserInfo.mode == 'DEV' : true" size="mini" type="primary" icon="el-icon-delete" @click="delFlow(item)">删除</jvs-button>
              </div>
            </div>
          </div>
          <div class="ruleflow-pagination">
            <el-pagination
              background
              layout="prev, pager, next"
              :page-size="flowPage.size"
              :pager-count="5"
              :current-page="flowPage.current"
              @current-change="flowCurrentChange"
              :total="flowPage.total">
            </el-pagination>
          </div>
        </div>
        <div v-if="activeName === '5'" class="info-box">
          <div class="tips" style="margin-top: 0;">
            <div class="title">
              <span class="el-icon-info" style="color: #1E6FFF;font-size: 14px;"></span>
              <span style="margin-left: 8px;">业务逻辑，应用可轻量化与业务系统进行对接，可灵活扩展业务互联能力。</span>
            </div>
            <div class="con-box" style="border-radius: 4px 4px 0 0;">
              <div class="secTitle">
                <span>业务逻辑目前主要运用于数据的处理、任务处理、消息发送、自定义等场景</span>
              </div>
              <ul class="tips-list">
                <li>
                  <span class="dot"></span>
                  <span>包括模型插件、工具、钉钉、加密、服务、电子签、天眼查、企查查、自定义扩展等几十种组件灵活编排</span>
                </li>
                <li>
                  <span class="dot"></span>
                  <span>支持按钮、网络、定时、队列等执行方式</span>
                </li>
              </ul>
            </div>
          </div>
          <div v-if="modeUserInfo ? modeUserInfo.mode == 'DEV' : true" style="margin-top: 15px;">
            <jvs-button class="add-button" size="mini" type="primary" icon="el-icon-plus" @click="bianj(null)">新增</jvs-button>
          </div>
          <div style="margin-top: 15px;display: flex;align-items: center;padding: 0 10px;">
            <div style="font-size: 13px;word-break: keep-all;margin-right: 10px;">逻辑名称</div>
            <el-input v-model="ruleName" clearable size="mini" style="width: 200px;margin-right: 10px;"></el-input>
            <div style="font-size: 13px;word-break: keep-all;margin-right: 10px;margin-left: 20px;">逻辑分类</div>
            <el-select v-model="reqType" clearable size="mini" style="width: 200px;margin-right: 10px;">
              <el-option label="默认" value="Low_code_logic"></el-option>
              <el-option v-for="req in ruleTypes" :key="req.type" :label="req.name" :value="req.type"></el-option>
            </el-select>
            <el-button type="primary" size="mini" @click="rulePage.current=1;getAllRuleHandle(appId);">查询</el-button>
            <el-button size="mini" @click="ruleName='';reqType='';">重置</el-button>
          </div>
          <div class="card-list" :style="`height: calc(100% - 148px - 43px - 16px - 48px${(modeUserInfo ? modeUserInfo.mode == 'DEV' : true) ? ' - 51px' : ''});`">
            <div class="card-item" v-for="item in ruleList" :key="'rule-item-'+item.id">
              <div class="card-header">
                <div v-if="item.icons && item.icons.length > 0" class="card-header-left">
                  <div class="item-icon-box" v-for="(it, key) in item.icons" :key="key">
                    <svg aria-hidden="true" class="item-icon">
                      <use :xlink:href="'#' + it"></use>
                    </svg>
                  </div>
                </div>
                <div v-else class="card-header-left">
                  <div class="item-icon-box">
                    <svg aria-hidden="true" class="item-icon">
                      <use :xlink:href="'#' + 'icon-layers'"></use>
                    </svg>
                  </div>
                </div>
                <span class="card-header-right">
                  <span :style="'margin-right: 14px;'+ (item.enable ? 'color: #36B452 ;' : 'color: #6F7588;')" @click="pubOprate(item)">{{item.enable ? '已发布' : '未发布'}}</span>
                  <span v-if="item.componentType" :class="{'type-text': true, 'type-text-hasdel': !item.componentId}">{{item.componentType | getTypeLabel}}</span>
                </span>
              </div>
              <h5>
                <span>{{item.name}}</span>
              </h5>
              <section :title="item.description">{{item.description | getDescription}}</section>
              <div class="tool">
                <jvs-button size="mini" type="primary" icon="el-icon-office-building" @click="See(item)">设计</jvs-button>
                <jvs-button v-if="modeUserInfo && modeUserInfo.mode == 'DEV'" size="mini" type="primary" icon="el-icon-delete" @click="del(item)">删除</jvs-button>
              </div>
            </div>
          </div>
          <div class="ruleflow-pagination">
            <el-pagination
              background
              layout="prev, pager, next"
              :page-size="rulePage.size"
              :pager-count="5"
              :current-page="rulePage.current"
              @current-change="ruleCurrentChange"
              :total="rulePage.total">
            </el-pagination>
          </div>
        </div>
        <div v-if="activeName === '6'" class="info-box">
          <div class="tips" style="margin-top: 0;">
            <div class="title">
              <span class="el-icon-info" style="color: #1E6FF;font-size: 14px;"></span>
              <span style="margin-left: 8px;">数据模型</span>
            </div>
            <div class="con-box" style="border-radius: 4px 4px 0 0;">
              <div class="secTitle">
                <span>当前此应用中创建生成的数据模型。</span>
              </div>
              <ul class="tips-list">
                <li>
                  <span class="dot"></span>
                  <span>名称：模型名称，默认在创建列表页和表单、流程时自动生成。</span>
                </li>
                <li>
                  <span class="dot"></span>
                  <span>数据集ID：模型ID，数据中的modelId。</span>
                </li>
                <li>
                  <span class="dot"></span>
                  <span>数据集名称：存储在数据库中的表名称。</span>
                </li>
                <li>
                  <span class="dot"></span>
                  <span>1、可在此处创建并快速生成列表页和表单。</span>
                </li>
                <li>
                  <span class="dot"></span>
                  <span>2、此处删除后，其他关联使用此数据模型处会无法使用，谨慎删除。</span>
                </li>
              </ul>
            </div>
          </div>
          <data-set :appInfo="appInfo"/>
        </div>
        <div v-if="activeName === '7'" class="info-box">
          <app-log :appInfo="appInfo"/>
        </div>
        <!-- <div v-if="activeName === '8'" class="info-box">
          <div class="tips" style="margin-top: 0;">
            <div class="title">
              <span class="el-icon-info" style="color: #1E6FF;font-size: 14px;"></span>
              <span style="margin-left: 8px;">自定义页面接入</span>
            </div>
            <div class="con-box" style="border-radius: 4px 4px 0 0;">
              <div class="secTitle">
                <span>通过自定义页面接入开发丰富的页面样式</span>
              </div>
              <ul class="tips-list">
                <li>
                  <span class="dot"></span>
                  <span>支持挂载外部页面</span>
                </li>
                <li>
                  <span class="dot"></span>
                  <span>支持源代码二次开发页面接入轻应用操作数据或操作逻辑引擎添加业务</span>
                </li>
              </ul>
            </div>
          </div>
          <custom-page :appInfo="appInfo"/>
        </div> -->
        <div v-if="activeName === '9'" class="info-box">
          <code-dev-page :appInfo="appInfo"/>
        </div>
        <div v-if="activeName === '10'" class="info-box">
          <versionManagePage :appInfo="appInfo" :modeUserInfo="modeUserInfo" @freshApp="freshApp" @setAppInfo="setAppInfo" @freshBasic="freshBasic" />
        </div>
        <div v-if="activeName === '11'" class="info-box">
          <singleAppPermission :appInfo="appInfo" @setAppInfo="setAppInfo"></singleAppPermission>
        </div>
        <div v-if="activeName === '12'" class="info-box">
          <modelAppPermission :appInfo="appInfo" @setAppInfo="setAppInfo"></modelAppPermission>
        </div>
      </div>
    </div>
    <!-- 逻辑设置 -->
    <el-dialog
      :class="'setDialog  '+ dialogClass"
      :title="setType == 'add' ? '新增' : '修改'"
      append-to-body
      :visible.sync="setVisible"
      :before-close="setClose"
    >
      <setForm v-if="setVisible" :formData="formData" @save="saveHandle" @close="closeHandle" style="padding-bottom: 10px;"/>
    </el-dialog>
    <!-- 关系图 -->
    <el-dialog
      title="应用管理"
      append-to-body
      class="relation-map-dialog"
      fullscreen
      :visible.sync="relationVisible"
      :before-close="relationClose"
    >
      <div v-if="relationVisible" class="application-relation-manage">
        <div class="left-menu">
          <div class="menu-list-item" v-for="(item, key) in relaMenuList" :key="'relation-item-' + key" @click="handleRelationMenuClick(item)" :style="activeRelation === item.value ? 'background-color: #f3f5f9;' : ''">
            <svg class="icon" aria-hidden="true" style="margin-right: 12px;width: 18px;height: 18px;">
              <use :xlink:href="'#'+ item.icon"></use>
            </svg>
            <span>{{ item.label }}</span>
          </div>
        </div>
        <div class="right-box">
          <relation-map v-if="activeRelation == '1'" :jvsAppId="systemId"></relation-map>
        </div>
      </div>
    </el-dialog>
    <div v-if="loading" class="loading-back"></div>
    <!-- 新建逻辑 -->
    <el-dialog
      title="选择创建逻辑类型"
      :visible.sync="createDialog"
      append-to-body
      :before-close="handleRuleTypeClose"
      width="950px"
      >
      <div class="rule-type-box" v-if="createDialog">
        <div class="rule-type-item" v-for="(item, key) in ruleTypes" :key="key" @click="selectedRuleType(item)">
          <img :src="item.img" alt=""/>
          <div class="item-name">{{ item.name }}</div>
          <div class="item-desc" :style="key < 3 ? 'text-align: center' : ''">{{ item.desc }}</div>
        </div>
      </div>
    </el-dialog>
    <!-- 快捷回复 -->
    <el-drawer
      :visible.sync="replyVisible"
      direction="rtl"
      custom-class="reply-drawer"
      :withHeader="false"
      :wrapperClosable="false"
      :before-close="replyClose">
      <div v-if="replyVisible" class="reply-box">
        <div class="reply-head">
          <h4>快捷回复</h4>
          <i class="el-icon-close" @click="replyClose"></i>
        </div>
        <div class="reply-list">
          <div v-for="(item, index) in replyList" :key="item.id">
            <div v-if="item.editable" class="reply-item">
              <el-input v-model="item.content" size="mini"></el-input>
              <i v-if="item.content" class="el-icon-check" style="margin-left: 10px;color: #409EFF;cursor: pointer;" @click="saveContent(item, index)"></i>
              <i class="el-icon-delete" style="margin-left: 10px;color: #F56C6C;cursor: pointer;" @click="delItem(item, index)"></i>
            </div>
            <div v-else class="reply-item">
              <span @dblclick="editRow(item)">{{item.content}}</span>
              <div class="tool">
                <i class="el-icon-edit-outline" @click="editRow(item)"></i>
                <i class="el-icon-delete" style="margin-left: 10px;color: #F56C6C;" @click="delItem(item, index)"></i>
              </div>
            </div>
          </div>
        </div>
        <div style="height: 30px;padding: 0 20px;margin-top: 10px;">
          <el-button size="mini" type="primary" @click="addReply">新增</el-button>
        </div>
      </div>
    </el-drawer>
    <!-- 编辑应用 名称 & 描述 -->
    <el-dialog
      title="编辑应用"
      :visible.sync="editAppVisible"
      append-to-body
      :before-close="editAppClose">
      <div v-if="editAppVisible">
        <jvs-form :option="appFormOption" :formData="editAppForm" @submit="editAppSubmit" @cancalClick="editAppClose"></jvs-form>
      </div>
    </el-dialog>
    <!-- 删除确认 -->
    <el-dialog
      class="hide-header-delete-dialog"
      title="确认删除应用"
      append-to-body
      :visible.sync="deleteVisible"
      :before-close="deleteClose">
      <div v-if="deleteVisible" class="delete-confirm-box">
        <div class="title">
          <i class="el-icon-warning"></i>
          <span>警告</span>
        </div>
        <div class="content">该操作将<i>永久删除</i>应用"<b>{{`${deleteRow.name}`}}</b>"的<i>所有数据</i>，同时删除关联的所有版本应用及数据，且<i>无法还原</i>，请谨慎操作！</div>
        <div class="input-box">
          <p>如确定删除，请输入应用名称</p>
          <el-input v-model="deleteRow.appName" size="mini"></el-input>
        </div>
      </div>
      <div v-if="deleteVisible" class="footer">
        <div class="footer-button">
          <div class="ftb" @click="deleteClose">取消</div>
          <div :class="{'ftb submit': true, 'disabled': !(deleteRow.appName && deleteRow.name == deleteRow.appName)}" @click="deleteSubmit">确定</div>
        </div>
      </div>
    </el-dialog>
    <!-- 选择模型创建流程 -->
    <el-dialog
      title="选择模型创建流程"
      :visible.sync="addFlowPopover"
      append-to-body
      :before-close="closeCreateModel"
      width="400px"
      class="create-flow-by-model-dialog"
    >
      <div class="model-list-box" v-if="addFlowPopover">
        <!-- 搜索 -->
        <div class="search-input-box">
          <svg class="icon" @click="getDataModelList">
            <use xlink:href="#icon-jvs-sousuo"></use>
          </svg>
          <div class="h-line"></div>
          <el-input v-model="searchKeyword" placeholder="搜索" @input="getDataModelList"></el-input>
        </div>
        <div class="model-list">
          <div v-for="dm in dataPageList" :key="dm.id" class="model-list-item" @click="createFlowByModel(dm)">{{dm.name}}</div>
          <div v-if="tableLoading" class="loading"></div>
        </div>
        <div class="footer">
          <div></div>
          <div class="el-pagination" style="display: flex;align-items: center;">
            <button :disabled="modelPage.currentPage < 2" class="btn-prev" @click="selectSearchPage('prev')">
              <i class="el-icon-arrow-left"></i>
            </button>
            <el-input-number v-model="modelPage.currentPage" @change="getDataModelList" :min="1" :max="Math.ceil(modelPage.total / modelPage.pageSize)" size="mini" :controls="false"></el-input-number>
            <div class="text">/ {{Math.ceil(modelPage.total / modelPage.pageSize)}}</div>
            <button :disabled="modelPage.currentPage == Math.ceil(modelPage.total / modelPage.pageSize)" class="btn-next"  @click="selectSearchPage('next')">
              <i class="el-icon-arrow-right"></i>
            </button>
          </div>
        </div>
      </div>
    </el-dialog>
    <el-dialog
      title="待办提醒设置"
      :visible.sync="editTaskVisible"
      append-to-body
      :before-close="editTaskClose">
      <div v-if="editTaskVisible">
        <jvs-form :option="taskFormOption" :formData="editTaskForm" @submit="editTaskSubmit" @cancalClick="editTaskClose"></jvs-form>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import {
  getApplicationDetail,
  getRuleListByApp,
  getAllModel,
  getPageUseDesign,
  getSecret,
  getPageWorkflowByApp,
  setLongtext, delDesign, del, edit,
  getFlowReply, createFlowReply, editFlowReply, delFlowReply,
  betaUse, GASuspend
} from './api'
import setForm from '@/views/rule/views/design/form'
import { delEdition, editJSON, SavaJSON } from '@/views/rule/api/rule'
import {getUserTreeType} from "@/api/common";
import {createModel, deleteModel, quickCreateModel} from "@/views/flowable/api/flowable";
import {getDataModel} from "@/components/template/api";
import AppPermission from "@/components/template/appPermission";
import DataSet from "@/components/template/dataSet";
import AppLog from "@/components/template/appLog";
import CustomPage from "@/components/template/customPage";
import CodeDevPage from "@/components/template/codeDevPage"
import relationMap from '@/components/template/relationMap'
import versionManagePage from '@/components/template/versionManage';
import singleAppPermission from '@/components/template/singleAppPermission';
import modelAppPermission from '@/components/template/modelAppPermission'
import {getStore} from "@/util/store"
import {ObjectUtils} from "@aesoper/normal-utils";
let Async_logic = require('@/const/img/Async_logic.png')
let Listening_logic = require('@/const/img/Listening_logic.png')
let External_API_logic = require('@/const/img/External_API_logic.png')
let Timing_logic = require('@/const/img/Timing_logic.png')
export default {
  name: 'application-manage-page',
  props: {
    systemId: {
      type: String
    },
    active: {
      type: String
    },
    changeModeUserRadom: {
      type: Number
    },
  },
  components: {AppLog, DataSet, AppPermission, setForm, CustomPage, relationMap, CodeDevPage, versionManagePage, singleAppPermission, modelAppPermission},
  filters: {
    getTypeLabel (type) {
      let str = ''
      switch (type) {
        case 'form': str = '表单';break;
        case 'h5': str = 'H5';break;
        case 'URL': str = '自定义页面';break;
        case 'page': str = '列表页';break;
        case 'screen': str = '大屏';break;
        case 'workflow': str = '工作流';break;
        case 'data': str = '数据模型';break;
        case 'report': str = '报表';break;
        default: str = '其他';break;
      }
      return str
    },
    getDescription (str) {
      let desc = ''
      if(str.startsWith('[') && str.endsWith(']')) {
        desc = JSON.parse(str).join('/')
      }else{
        desc = str
      }
      return desc
    }
  },
  data() {
    return {
      menuList: [
        { icon: 'icon-setting', label: '基础信息', value: '1' },
        { icon: 'icon-layers', label: '数据模型', value: '6' },
        { icon: 'icon-file', label: '列表表单', value: '3' },
        { icon: 'icon-link', label: '审批流程', value: '4' },
        { icon: 'icon-adjust', label: '业务逻辑', value: '5' },
        { icon: 'icon-unlock', label: '设计权限', value: '2' },
        { icon: 'icon-lock', label: '使用权限', value: '11' },
        { icon: 'icon-lock', label: '模型权限', value: '12' },
        { icon: 'icon-explain', label: '源码标识', value: '9' },
        { icon: 'icon-time', label: '版本管理', value: '10' },
        { icon: 'icon-history', label: '应用日志', value: '7' },
        // { icon: 'icon-smile', label: '自定义页面接入', value: '8' },
      ],
      appInfo: {},
      showAppInfo: false,
      appOption: {
        btnHide: true,
        disabled: true,
        formAlign: 'top',
        column: [
          {
            label: '角色',
            prop: 'roleIds',
            type: 'select',
            multiple: true,
            collapsetags: true,
            dicData: [],
            props: {
              label: 'roleName',
              value: 'id'
            }
          },
          {
            label: '用户',
            prop: 'userIds',
            type: 'select',
            multiple: true,
            collapsetags: true,
            dicData: [],
            props: {
              label: 'realName',
              value: 'id'
            }
          }
        ]
      },
      ruleList: [],
      flowList: [], // 审批流程列表
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
      },
      queryParam: {}, // 查询参数
      pageList: [],
      pageOption: {
        addBtn: false,
        delBtn: false,
        editBtn: false,
        viewBtn: false,
        search: true,
        hideTop: false,
        showOverflow: true,
        page: true,
        menu: this.modeUserInfo ? this.modeUserInfo.mode == 'DEV' : true,
        column: [
          {
            label: '页面名称',
            prop: 'name',
            width: 200,
            search: true,
          },
          {
            label: '页面编码',
            prop: 'id',
          },
          {
            label: '创建时间',
            prop: 'createTime',
            datetype: 'datetime',
            format: "yyyy-MM-dd hh:mm:ss",
            valueFormat: "yyyy-MM-dd hh:mm:ss",
            addDisplay: false,
            editDisplay: false,
          },
          {
            label: '更新时间',
            prop: 'updateTime',
            datetype: 'datetime',
            format: "yyyy-MM-dd hh:mm:ss",
            valueFormat: "yyyy-MM-dd hh:mm:ss",
            addDisplay: false,
            editDisplay: false,
          },
        ]
      },
      modelList: [],
      modelOption: {
        addBtn: false,
        search: false,
        hideTop: false,
        column: [
          { label: '字段名', prop: 'fieldKey'},
          { label: '字段类型', prop: 'fieldType'},
          { label: '说明', prop: 'fieldName'}
        ]
      },
      setType: '',
      dialogClass: '',
      setVisible: false,
      formData: {},
      activeName: '1',
      secretKey: null,
      loading: true,
      appId: '',
      editIndex: -1,
      relationVisible: false,
      activeRelation: '1',
      relaMenuList: [
        { icon: 'icon-setting', label: '关联关系', value: '1' }
      ],
      createDialog: false, // 创建逻辑弹窗开关
      ruleTypes: [
        { type: 'Source_code_development_docking_logic', img: Timing_logic, name: '事件触发', desc: '在系统内部调用,可使用表单、列表、工作流、当前用户等功能。用于逻辑复用或源码扩展二次开发使用', },
        { type: 'External_API_logic', img: External_API_logic, name: 'API触发', desc: '其它三方系统需要与平台业务数据进行链接时使用。可下载三方调用示例', },
        { type: 'Timing_logic', img: Async_logic, name: '定时触发', desc: '业务数据需要周期性执行时使用', },
        { type: 'Listening_logic', img: Listening_logic, name: '监听触发', desc: '根据消费mq的数据进行执行。常用于不实现异步传递信息', },
      ],
      replyVisible: false,
      replyList: [],
      editAppVisible: false,
      editAppForm: null,
      appFormOption: {
        emptyBtn: false,
        column: [
          {
            label: '名称',
            prop: 'name',
            maxlength: 30,
            rules: [
              { required: true, message: '请输入名称', trigger: 'blur' }
            ]
          },
          {
            label: '描述',
            type: 'textarea',
            prop: 'description'
          },
        ]
      },
      modeUserInfo: null,
      alluseLoading: false,
      designQueryParams: {},
      designPage: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
      },
      flowName: '',
      flowPage: {
        total: 0,
        size: 40,
        current: 1
      },
      ruleName: '',
      reqType: '',
      rulePage: {
        total: 0,
        size: 40,
        current: 1
      },
      activeType: 'page',
      typeList: [
        {label: '列表页', value: 'page'},
        {label: '表单', value: 'form'},
      ],
      deleteVisible: false,
      deleteRow: null,
      addFlowPopover: false,
      dataPageList: [],
      tableLoading: false,
      searchKeyword: '',
      modelPage: {
        total: 0,
        currentPage: 1,
        pageSize: 10,
      },
      taskInfo:{},
      editTaskForm:{},
      editTaskVisible:false,
      taskFormOption: {
        emptyBtn: false,
        column: [
          {
            label: '开启待办提醒',
            prop: 'enableTask',
            type: 'switch',
            defaultValue: false,
            rules: [{ required: true, message: '请选择是否开启待办提醒', trigger: 'blur' }],
          },
          {
            label: '待办应用ID',
            prop: 'taskAppId',
            rules: [{ required: false, message: '请输入待办应用ID', trigger: 'blur' }],
          },
          {
            label: '待办Secret',
            prop: 'taskAppSecret',
            showpassword: true,
            rules: [{ required: false, message: '请输入待办应用的Secret', trigger: 'blur' }],
          },
          {
            label: '创建单据接口',
            prop: 'taskPushApi',
            rules: [{ required: false, message: '请输入接口地址', trigger: 'blur' }],
          },
          {
            label: '关闭单据接口',
            prop: 'taskCloseApi',
            rules: [{ required: false, message: '请输入接口地址', trigger: 'blur' }],
          },
          {
            label: '撤回单据接口',
            prop: 'taskRecallApi',
            rules: [{ required: false, message: '请输入接口地址', trigger: 'blur' }],
          },
          {
            label: '更新单据接口',
            prop: 'taskUpdateApi',
            rules: [{ required: false, message: '请输入接口地址', trigger: 'blur' }],
          },
          {
            label: '默认路由地址',
            prop: 'taskFormUrl',
            rules: [{ required: false, message: '请输入访问地址', trigger: 'blur' }],
          },
        ]
      }
    }
  },
  created () {
    this.modeUserInfo = getStore({ name: 'modeUserInfo' })
  },
  methods: {
    // 删除
    delRowHandle(row) {
      this.$confirm(`删除后关联的页面或功能将无法使用，请谨慎操作！`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 页面
        delDesign({appId: this.appId, designId: row.id, designType: this.activeType}).then(res => {
          if(res.data && res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: '删除成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.loading = true
            this.getAllUseHandle(this.appId)
            this.$emit('freshAllMenu', true)
            this.$nextTick(() => {
              this.loading = false
            })
          }
        })
      }).catch(e => {});
    },
    // 卸载应用
    handleUnload(obj) {
      this.$confirm('卸载后所有的人无法操作，是否确定？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // unload
        GASuspend(obj.id).then(res => {
          if (res.data && res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: '卸载成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.getApplicationDetailHandle(this.appId)
          }
        })
      }).catch(e => {})
    },
    // 发布应用
    handlePublish(obj) {
      this.$confirm('是否确认发布？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // publish
        betaUse(obj.id).then(res => {
          if (res.data && res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: '发布成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.getApplicationDetailHandle(this.appId)
          }
        })
      }).catch(e => {})
    },
    // 设置权限
    addUser(attr, data) {
      this.appInfo.role[attr] = [...data]
    },
    // 新建审批流程
    handleAddOA() {
      const params = {
        jvsAppId: this.appInfo.id
      }
      createModel(params).then(res => {
        if (res.data && res.data.code == 0) {
          const str = location.origin + `/flowable-ui/#/processDesign?id=${res.data.data.id}&jvsAppId=${this.appInfo.id}`
          this.$openUrl(str, '_blank')
          this.getAllWorkflow()
        }
      })
    },
    // 菜单点击
    handleMenuClick(item) {
      this.activeName = item.value
      // 获取流程
      if(this.activeName == '4') {
        this.getAllWorkflow()
      }
      // 获取逻辑
      if(this.activeName == '5') {
        this.getAllRuleHandle(this.appId)
      }
    },
    // 设计审批流程
    designFlow(item) {
      this.$openUrl(`/flowable-ui/#/processDesign?id=${item.id}&jvsAppId=${this.appInfo.id}`, '_blank')
    },
    // 删除审批流程
    delFlow(item) {
      this.$confirm('确认删除？').then(_ => {
        deleteModel(this.appInfo.id, item.id).then(res => {
          if(res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: '删除成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.getAllWorkflow()
          }
        })
      }).catch(_ => {})
    },
    // 获取应用所有工作流
    getAllWorkflow () {
      this.flowList = []
      let obj = {current: this.flowPage.current, size: this.flowPage.size}
      if(this.flowName) {
        obj.name = this.flowName
      }
      getPageWorkflowByApp(this.appInfo.id, obj).then(res => {
        if (res.data && res.data.code == 0 && res.data.data) {
          this.flowList = res.data.data.records
          this.flowPage.current = res.data.data.current
          this.flowPage.total = res.data.data.total
        }
      })
    },
    // 显示密钥
    handleDisplay() {
      getSecret(this.appInfo.id).then(res => {
        if (res.data && res.data.code == 0) {
          this.secretKey = res.data.data
        }
      })
    },
    // 获取tag名字
    getTagName(obj) {
      let header = ''
      switch (obj.type) {
        case 'user':
          break;
        case 'role':
          header = '（角色）';
          break;
        case 'dept':
          header = '（部门）';
          break;
        case 'group':
          header = '（群组）';
          break;
        default: break;
      }
      return header + obj.name
    },
    // 初始化
    async init (id) {
      this.appId = id
      this.loading = true
      this.secretKey = null
      this.showAppInfo = false
      await this.getUSerRoleLit({ value: '',type: '' })
      this.activeName = '1'
      // 应用详情
      this.getApplicationDetailHandle(id)
      // 所有模型
      // this.getAllModelHandle(id)
      if(this.active) {
        this.activeName = this.active
      }
    },
    // 获取应用详情
    getApplicationDetailHandle (id) {
      getApplicationDetail(id).then(res => {
        if(res.data && res.data.code == 0) {
          this.appInfo = res.data.data
          this.appInfo.role = (res.data.data.role && !(res.data.data.role instanceof Array)) ? res.data.data.role : {adminMember: [], devMember: []}
          this.showAppInfo = true
          this.loading = false
          this.$forceUpdate()
        }
      })
    },
    // 获取所有的逻辑列表
    getAllRuleHandle (id) {
      let obj = {current: this.rulePage.current, size: this.rulePage.size}
      if(this.ruleName) {
        obj.name = this.ruleName
      }
      if(this.reqType) {
        obj.reqType = this.reqType
      }
      getRuleListByApp(id, obj).then(res => {
        if(res.data && res.data.code == 0) {
          this.ruleList = res.data.data.records
          this.rulePage.current = res.data.data.current
          this.rulePage.total = res.data.data.total
        }
      })
    },
    // 获取所有页面
    getAllUseHandle (id) {
      let obj = {designType: this.activeType}
      obj.size = this.designPage.pageSize
      obj.current = this.designPage.currentPage
      this.alluseLoading = true
      getPageUseDesign(id, Object.assign(obj, this.designQueryParams)).then(res => {
        if(res.data && res.data.code == 0) {
          this.pageList = res.data.data.records
          this.designPage.total = res.data.data.total
          this.designPage.currentPage = res.data.data.current
          this.alluseLoading = false
        }
      })
    },
    // 获取所有模型
    getAllModelHandle (id) {
      getAllModel(id).then(res => {
        if(res.data && res.data.code == 0) {
          this.modelList = res.data.data ? [...res.data.data] : []
        }
      })
    },
    /**
     * 逻辑设置
     */
    bianj (row) {
      this.createDialog = true
    },
    saveHandle (form) {
      if(this.setType == 'add') {
        this.addHandle(form)
      }
      if(this.setType == 'edit') {
        this.editHandle(form)
      }
    },
    // 新增一个逻辑引擎
    addHandle (obj) {
      SavaJSON(this.appInfo.id, obj).then(res => {
        if(res.data.code == 0) {
          this.$notify({
            title: '提示',
            message: '新增成功',
            position: 'bottom-right',
            type: 'success'
          });
          this.setClose()
          this.getAllRuleHandle(this.appInfo.id)
        }
      })
    },
    // 编辑
    editHandle (obj) {
      let temp = JSON.parse(JSON.stringify(obj))
      editJSON(this.appInfo.id, obj.id, temp).then(res => {
        if(res.data.code == 0) {
          this.$notify({
            title: '提示',
            message: '修改成功',
            position: 'bottom-right',
            type: 'success'
          });
          this.setClose()
          this.getAllRuleHandle(this.appInfo.id)
        }
      })
    },
    setClose () {
      this.setVisible = false
      this.$set(this, 'formData', {})
      this.setType = ''
    },
    closeHandle (bool) {
      if(bool) {
        this.setClose()
      }
    },
    del (row) {
      this.$confirm('此操作将永久删除该设计,是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        delEdition(this.appInfo.id, row.id).then(res => {
          this.$notify({
            title: '提示',
            message: '删除成功',
            position: 'bottom-right',
            type: 'success'
          });
          this.getAllRuleHandle(this.appInfo.id)
        })
      }).catch(() => {
      })
    },
    See (row) {
      this.$openUrl(`/rule-design-ui/#/ruleDesign?id=${row.secret}&name=${row.name}&jvsAppId=${this.appInfo.id}`, '_blank')
    },
    async getUSerRoleLit (param) {
      await getUserTreeType(param).then(res =>{
        if(res.data && res.data.code == 0) {
          if(res.data.data.roles) {
            this.appOption.column[0].dicData = res.data.data.roles
          }
          if(res.data.data.users) {
            this.appOption.column[1].dicData = res.data.data.users
          }
        }
      })
    },
    copyHandle (info) {
      let con = `${location.origin}/#/login?backlink=jvs/application/${info.id}`
      const text = document.createElement('input')
      text.value = con
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
    pubOprate (item) {
      let obj = JSON.parse(JSON.stringify(item))
      obj.enable = !obj.enable
      this.editHandle(obj)
    },
    copyStr (str) {
      const text = document.createElement('input')
      text.value = str
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
    delApp () {
      this.deleteRow = JSON.parse(JSON.stringify(this.appInfo))
      this.deleteVisible = true
    },
    viewRelation () {
      this.relationVisible = true
    },
    handleRelationMenuClick (item) {
      this.activeRelation = item.value
    },
    relationClose () {
      this.relationVisible = false
    },
    // 选择逻辑
    selectedRuleType (obj) {
      if(['Listening_logic', 'Async_logic'].indexOf(obj.type) > -1) {
        this.$notify({
          title: '提示',
          message: '正在开发',
          position: 'bottom-right',
          type: 'warning'
        });
      }else{
        this.submitRule(obj)
      }
    },
    submitRule (info) {
      const params = {
        jvsAppId: this.appInfo.id,
        reqType: info.type
      }
      SavaJSON(this.appInfo.id, params).then(res => {
        if(res.data && res.data.code == 0) {
          this.$openUrl(`/rule-design-ui/#/ruleDesign?id=${res.data.data}&name=未命名逻辑&jvsAppId=${this.appInfo.id}`, '_blank')
          this.getAllRuleHandle(this.appInfo.id)
          this.handleRuleTypeClose()
        }
      })
    },
    // 选择逻辑类型弹窗 关闭
    handleRuleTypeClose() {
      this.createDialog = false
    },
    async getReplyList () {
      await getFlowReply(this.appInfo.id).then(res => {
        if(res.data && res.data.code == 0) {
          this.replyList = res.data.data
          console.log(this.replyList)
        }
      })
    },
    // 快捷回复
    async replySetting () {
      await this.getReplyList()
      this.replyVisible = true
    },
    replyClose () {
      this.replyVisible = false
    },
    addReply () {
      this.replyList.push({content: '', editable: true})
    },
    editRow(item) {
      this.$set(item, 'editable', true)
    },
    saveContent (item, index) {
      if(item.content) {
        if(item.id) {
          editFlowReply(this.appInfo.id, {content: item.content, id: item.id}).then(res => {
            if(res.data && res.data.code == 0) {
              this.$notify({
                title: '提示',
                message: '修改成功',
                position: 'bottom-right',
                type: 'success'
              })
              item.editable = false
              this.getReplyList()
            }
          })
        }else{
          createFlowReply(this.appInfo.id, {content: item.content}).then(res => {
            if(res.data && res.data.code == 0) {
              this.$notify({
                title: '提示',
                message: '添加成功',
                position: 'bottom-right',
                type: 'success'
              })
              item.editable = false
              this.getReplyList()
            }
          })
        }
      }
    },
    delItem (item, index) {
      if(item.id) {
        delFlowReply(this.appInfo.id, item.id).then(res => {
          if(res.data && res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: '删除成功',
              position: 'bottom-right',
              type: 'success'
            })
            this.getReplyList()
          }
        })
      }else{
        this.replyList.splice(index, 1)
      }
      this.$forceUpdate()
    },
    editApp () {
      this.editAppForm = JSON.parse(JSON.stringify(this.appInfo))
      this.editAppVisible = true
    },
    editAppSubmit () {
      edit(this.editAppForm).then(res => {
        if(res.data && res.data.code == 0) {
          this.$notify({
            title: '提示',
            message: '编辑成功',
            position: 'bottom-right',
            type: 'success'
          })
          this.editAppClose()
          this.getApplicationDetailHandle(this.appId)
          this.$emit('freshAllMenu', true)
        }
      })
    },
    editAppClose () {
      this.editAppForm = null
      this.editAppVisible = false
    },
    freshApp () {
      this.$emit('freshAllMenu', true)
    },
    setAppInfo (key, value) {
      this.$set(this.appInfo, key, value)
      this.$forceUpdate()
    },
    freshBasic () {
      this.freshApp()
      this.init(this.systemId)
    },
    typeChange (tab) {
      this.activeType = tab.value
      this.$set(this.designPage, 'currentPage', 1)
      this.$set(this.designPage, 'total', 0)
      this.designQueryParams = {}
      this.getAllUseHandle(this.appId)
      this.$forceUpdate()
    },
    designSearchChange (form) {
      this.designQueryParams = form
      this.getAllUseHandle(this.appId)
    },
    flowCurrentChange(val) {
      this.flowPage.current = val
      this.getAllWorkflow()
    },
    ruleCurrentChange(val) {
      this.rulePage.current = val
      this.getAllRuleHandle(this.appId)
    },
    deleteSubmit () {
      if(this.deleteRow.appName && this.deleteRow.appName == this.deleteRow.name) {
        del(this.deleteRow.id, {appName: this.deleteRow.appName}).then(res => {
          if(res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: '删除成功',
              position: 'bottom-right',
              type: 'success'
            })
            this.$store.commit("SET_SYSTEM", "")
            this.$emit('closeApp', true)
            this.$store.commit("DEL_APP_TAG", {id: this.deleteRow.id})
            this.deleteClose()
          }
        })
      }
    },
    deleteClose () {
      this.deleteRow = null
      this.deleteVisible = false
    },
    // 获取数据集列表
    getDataModelList () {
      this.tableLoading = true
      let obj = {
        current: this.modelPage.currentPage,
        size: 10,
        appId: this.appInfo.id,
        enableWorkflow: false
      }
      if(this.searchKeyword) {
        obj.name = this.searchKeyword
      }
      this.dataPageList = []
      getDataModel(Object.assign(obj)).then(res => {
        if(res.data && res.data.code == 0) {
          this.dataPageList = [...res.data.data.records]
          this.modelPage.currentPage = res.data.data.current
          this.modelPage.total = res.data.data.total
          this.tableLoading = false
        } else {
          this.tableLoading = false
        }
      }).catch(err => {
        this.tableLoading = false
      })
    },
    selectSearchPage (type) {
      if(type == 'prev') {
        this.modelPage.currentPage -= 1
      }
      if(type == 'next') {
        this.modelPage.currentPage += 1
      }
      this.getDataModelList()
    },
    openModelList () {
      this.addFlowPopover = true
      this.getDataModelList()
    },
    createFlowByModel (item) {
      let params = {
        name: '',
        jvsAppId: this.appInfo.id,
        dataModelId: item.id,
        type: 'flow'
      }
      quickCreateModel(params).then(res => {
        if(res.data && res.data.code == 0) {
          this.$notify({
            title: '提示',
            message: '创建成功',
            position: 'bottom-right',
            type: 'success'
          })
          this.closeCreateModel()
          this.flowPage.current = 1
          this.getAllWorkflow()
          this.$openUrl(`/flowable-ui/#/processDesign?jvsAppId=${this.appInfo.id}&id=${res.data.data.id}`, '_blank')
        }
      })
    },
    closeCreateModel () {
      this.addFlowPopover = false
    },
    copyAppLink () {
      this.copyStr(`${location.origin}/#/wel/index?name=${this.appInfo.name}&jvsAppId=${this.appInfo.id}`)
    },
    handleTask () {
      let appInfo = JSON.parse(JSON.stringify(this.appInfo))
      if (appInfo.taskSetting){
        this.editTaskForm = appInfo.taskSetting;
        this.taskInfo = appInfo.taskSetting;
      }
      this.editTaskVisible = true
    },
    editTaskSubmit () {
      this.appInfo = Object.assign(this.appInfo, {taskSetting:this.editTaskForm});
      edit(this.appInfo).then(res => {
        if(res.data && res.data.code == 0) {
          this.$notify({
            title: '提示',
            message: '设置成功',
            position: 'bottom-right',
            type: 'success'
          })
          this.editTaskClose()
          this.getApplicationDetailHandle(this.appId)
        }
      })
    },
    editTaskClose () {
      this.editTaskForm = this.taskInfo;
      this.editTaskVisible = false
    },
  },
  watch: {
    changeModeUserRadom: {
      handler (newVal, oldVal) {
        if(newVal > -1) {
          this.modeUserInfo = getStore({ name: 'modeUserInfo' })
        }
      }
    }
  }
}
</script>
<style lang="scss" scoped>
.icon{
  width: 30px;
  height: 30px;
  vertical-align: -0.25em;
  fill: #333333;
  overflow: hidden;
}
.application-manage-page{
  padding: 0;
  height: 100%;
  overflow: hidden;
  .back-box{
    width: 100%;
    height: 100%;
    overflow: hidden;
    overflow-y: auto;
    background: #FFFFFF;
    // border-radius: 6px;
    padding: 10px 24px;
    box-sizing: border-box;
    .left-menu{
      display: flex;
      align-items: center;
      box-sizing: border-box;
      width: 100%;
      overflow: hidden;
      overflow-x: auto;
      padding-bottom: 8px;
      .menu-list-item{
        margin-left: 32px;
        font-size: 14px;
        cursor: pointer;
        height: 21px;
        font-size: 16px;
        font-family: Microsoft YaHei, Microsoft YaHei;
        font-weight: 400;
        color: #6F7588;
        line-height: 21px;
        padding: 6px 0;
        border-bottom: 2px solid #fff;
        border-radius: 2px 0px 2px 0px;
        word-break: keep-all;
        svg{
          width: 14px;
          height: 14px;
          margin-right: 9px;
        }
        &:hover, &.active{
          color: #1E6FFF;
          font-weight: 700;
          border-color: #1E6FFF;
        }
      }
      .menu-list-item:nth-of-type(1){
        margin-left: 0;
      }
    }
    .left-menu-bootom-line{
      width: 100%;
      height: 1px;
      background: #EEEFF0;
    }
    .type-tab-bar{
      width: 100%;
      margin-top: 15px;
      height: calc(100% - 61px);
      overflow: hidden;
    }
    .app-info-permanage{
      display: block;
      .desc{
        font-size: 14px;
        font-family: Microsoft YaHei, Microsoft YaHei;
        font-weight: 400;
        color: #6F7588;
        line-height: 32px;
        span{
          display: block;
        }
      }
    }
    .app-info-more-oprate{
      display: block;
      div{
        margin-top: 15px;
        i{
          margin-right: 5px;
        }
        i, span{
          cursor: pointer;
        }
      }
    }
  }
  .top{
    display: flex;
    justify-content: space-between;
    img{
      display: block;
      width: 56px;
      height: 56px;
    }
    .text-info{
      margin-left: 16px;
      flex: 1;
      font-family: Source Han Sans, Source Han Sans;
      h3{
        margin: 0;
        font-size: 16px;
        font-family: Source Han Sans, Source Han Sans;
        font-weight: 700;
        color: #363B4C;
        line-height: 24px;
      }
      .status-tag{
        display: block;
        width: 56px;
        height: 24px;
        text-align: center;
        font-size: 14px;
        font-family: Source Han Sans, Source Han Sans;
        font-weight: 400;
        line-height: 22px;
        border-radius: 4px;
        margin-left: 16px;
        box-sizing: border-box;
        color: #FF9736;
        background: #FFF7EF;
        border: 1px solid #FF9736;
      }
      .status-tag.success{
        color: #36B452;
        background: #DFF3E3;
        border-color: #36B452;
      }
      .desc-box{
        height: 18px;
        margin-top: 9px;
        display: flex;
        align-items: center;
        .desc-span{
          display: block;
          height: 18px;
          font-size: 14px;
          font-family: Microsoft YaHei, Microsoft YaHei;
          font-weight: 400;
          color: #6F7588;
          line-height: 18px;
        }
        .desc-slider-span{
          height: 14px;
          width: 1px;
          background: #6F7588;
          margin: 0 15px;
        }
      }
    }
    .button-tool{
      padding-top: 8px;
      .el-button{
        width: 78px;
        height: 32px;
        box-sizing: border-box;
        font-size: 14px;
        border-radius: 4px;
        /deep/i, /deep/span{
          font-size: 14px;
          font-family: Microsoft YaHei, Microsoft YaHei;
          font-weight: 400;
        }
      }
      .el-button--primary{
        background-color: #1E6FFF;
        color: #FFFFFF;
      }
    }
  }
  .info-box{
    height: 100%;
    box-sizing: border-box;
    h4{
      font-size: 18px;
      margin: 10px 0;
      position: relative;
      text-indent: 15px;
    }
    h4::before{
      content: "";
      display: block;
      width: 4px;
      height: 22px;
      background: #3471ff;
      border-radius: 2px;
      position: absolute;
      top: 1px;
      left: 0;
    }
    .text-desc{
      color: rgb(54, 59, 76);
      font-size: 16px;
      line-height: 21px;
      font-family: "Microsoft YaHei", "Microsoft YaHei";
      margin-top: 9px;
      margin-bottom: 16px;
    }
    .add-button{
      height: 36px;
      background: #1E6FFF;
      border-radius: 4px;
      box-sizing: border-box;
      font-size: 14px;
      font-family: Microsoft YaHei, Microsoft YaHei;
      font-weight: 400;
      color: #FFFFFF;
    }
    .card-list{
      margin-top: 16px;
      height: calc(100% - 130px);
      display: flex;
      align-items: flex-start;
      align-content: flex-start;
      flex-wrap: wrap;
      overflow: hidden;
      overflow-y: auto;
      .card-item{
        padding: 18px 0 0 0;
        margin-right: 18px;
        margin-bottom: 18px;
        box-sizing: border-box;
        width: calc(25% - 18px);
        max-width: 396px;
        height: 152px;
        background: #fff;
        border: 1px solid #EEEFF0;
        border-radius: 6px;
        transition: 0.2s;
        cursor: pointer;
        position: relative;
        &:hover{
          transition: 0.2s;
          border-color: #1E6FFF;
          .card-header{
            .type-text-hasdel{
              display: none;
            }
          }
        }
        .card-header{
          display: flex;
          justify-content: space-between;
          margin: 16px;
          margin-top: 0;
          .card-header-left{
            display: flex;
            .item-icon-box{
              width: 24px;
              height: 24px;
              border-radius: 4px;
              display: flex;
              align-items: center;
              justify-content: center;
              background-color: #E4EDFF;
              margin-left: 10px;
              .item-icon{
                fill: #3471ff;
                width: 16px;
                height: 16px;
              }
            }
            .item-icon-box:nth-of-type(1) {
              margin-left: 0;
            }
          }
          .card-header-right{
            font-size: 14px;
            font-family: Microsoft YaHei, Microsoft YaHei;
            font-weight: 400;
            color: #6F7588;
            display: flex;
            align-items: center;
          }
          .type-text{
            display: inline;
            padding-left: 14px;
            position: relative;
            line-height: 14px;
          }
          .type-text::before{
            display: block;
            content: '';
            width: 1px;
            height: 14px;
            position: absolute;
            left: 0;
            top: 0;
            background-color: #6F7588;
          }
        }
        h5{
          font-size: 16px;
          font-family: Source Han Sans, Source Han Sans;
          font-weight: 700;
          color: #363B4C;
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin: 0 16px;
          i{
            color: #B3B3B3;
            cursor: pointer;
          }
        }
        section{
          margin: 0 16px;
          margin-top: 6px;
          font-size: 14px;
          box-sizing: border-box;
          font-family: Source Han Sans, Source Han Sans;
          font-weight: 400;
          color: #6F7588;
          display: -webkit-box;
          -webkit-line-clamp: 1;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }
        .tool{
          display: flex;
          justify-content: space-between;
          align-items: center;
          background: #F5F6F7;
          width: calc(100% - 4px);
          box-sizing: border-box;
          position: absolute;
          bottom: 2px;
          left: 2px;
          border-radius: 0px 0px 6px 6px;
          overflow: hidden;
          .el-button--primary{
            flex: 1;
            height: 36px;
            border-radius: 0;
            background-color: #F5F6F7;
            border: 0;
            font-size: 14px;
            font-family: Source Han Sans, Source Han Sans;
            font-weight: 400;
            color: #363B4C;
            margin-left: 2px;
            margin-right: 2px;
            position: relative;
          }
          .el-button--primary:hover{
            background-color: #1E6FFF;
            border-color: #1E6FFF;
            color: #fff;
          }
          .el-button--primary::before{
            content: "";
            width: 1px;
            height: 20px;
            background: #EEEFF0;
            position: absolute;
            left: -3px;
            top: 8px;
            z-index: 0;
          }
          .el-button--primary:nth-of-type(1){
            margin-left: 0;
          }
          .el-button--primary:nth-of-type(1)::before{
            display: none;
          }
          .el-button--primary:nth-last-of-type(1){
            margin-right: 0;
          }
        }
      }
    }
    .ruleflow-pagination{
      margin-top: 20px;
      text-align: right;
      /deep/.el-pagination{
        padding: 0;
        .btn-next{
          margin-right: 0;
        }
      }
    }
  }
  .basic-info{
    overflow: hidden;
    overflow-y: auto;
  }
  .info-page-list{
    .type-list{
      margin-bottom: 16px;
      display: flex;
      align-items: center;
      .type-list-item{
        margin-left: 32px;
        font-size: 16px;
        font-family: Microsoft YaHei, Microsoft YaHei;
        font-weight: 400;
        color: #6F7588;
        padding-bottom: 6px;
        cursor: pointer;
      }
      .type-list-item:nth-of-type(1){
        margin-left: 0;
      }
      .active{
        font-size: 16px;
        font-family: Microsoft YaHei, Microsoft YaHei;
        font-weight: 700;
        color: #1E6FFF;
        position: relative;
      }
      .active::after{
        display: block;
        content: "";
        width: 100%;
        height: 2px;
        background: #1E6FFF;
        border-radius: 2px 0px 2px 0px;
        position: absolute;
        bottom: 0;
        left: 0;
      }
    }
    /deep/.show-all-design-page-table{
      height: calc(100% - 96px);
      .jvs-table-top{
        .el-card__body{
          .search-form{
            padding-bottom: 0;
            .el-col{
              margin-bottom: 0;
            }
          }
        }
      }
      .table-body-box{
        height: calc(100% - 48px);
        .el-table{
          height: 100%;
          .el-table__body-wrapper{
            height: calc(100% - 40px);
          }
        }
      }
    }
  }
  .basic-info::-webkit-scrollbar{
    display: none;
  }
  .info-box-item{
    background-color: #F5F6F7;
    padding: 16px;
    border-radius: 4px;
    .bottom-desc{
      margin-top: 12px;
      font-size: 12px;
      font-family: Microsoft YaHei, Microsoft YaHei;
      font-weight: 400;
      color: #6F7588;
      line-height: 16px;
    }
  }
  .status-info{
    height: 36px;
    background: #FFF7EF;
    border-radius: 4px;
    font-size: 12px;
    font-family: Microsoft YaHei, Microsoft YaHei;
    font-weight: 400;
    color: #6F7588;
    line-height: 16px;
    margin-top: 9px;
    i{
      font-size: 16px;
      color: #FF9736;
    }
  }
  .status-info.isDeploy{
    background: #DFF3E3;
    i{
      color: #36B452;
    }
  }
  .loading-back{
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    background-image: url('../../../public/jvs-ui-public/img/loading.gif');
    background-repeat: no-repeat;
    background-position: center;
    background-color: #fff;
    z-index: 99;
  }
}
.relation-map-dialog{
  /deep/.el-dialog.is-fullscreen{
    border-radius: 0;
    .el-dialog__body{
      padding: 0!important;
      height: calc(100% - 60px);
    }
  }
}
.application-relation-manage{
  display: flex;
  width: 100%;
  height: 100%;
  overflow: hidden;
  overflow-y: auto;
  background: #fff;
  border-radius: 6px;
  padding: 20px;
  box-sizing: border-box;
  justify-content: space-between;
  .left-menu{
    width: 200px;
    .menu-list-item{
      border-radius: 4px;
      font-size: 14px;
      padding: 10px 16px;
      cursor: pointer;
      &:hover{
        background-color: #f3f5f9;
      }
    }
  }
  .right-box{
    flex: 1;
    margin-left: 10px;
    .application-relation-map{
      height: 100%;
    }
  }
}
.rule-type-box{
  display: flex;
  align-items: center;
  justify-content: space-evenly;
  .rule-type-item{
    border-radius: 4px;
    cursor: pointer;
    width: 180px;
    height: 250px;
    box-shadow: 0 0 5px #eee;
    //text-align: center;
    display: flex;
    flex-direction: column;
    align-items: center;
    img{
      margin: 20px 0 20px;
      width: 90px;
      height: 90px;
    }
    .item-name{
      color: #333;
      font-weight: bold;
      line-height: 26px;
    }
    .item-desc{
      width: 80%;
      color: #a2a3a5;
      line-height: 20px;
      font-size: 12px;
    }
    &:hover{
      box-shadow: 0 0 5px #ddd;
    }
  }
}
.reply-box{
  height: 100%;
  overflow: hidden;
  box-sizing: border-box;
  .reply-head{
    height: 30px;
    padding: 0 20px;
    box-sizing: border-box;
    h4{
      font-size: 18px;
      margin: 10px 0;
      position: relative;
      text-indent: 15px;
      color: #333;
    }
    h4::before {
      content: "";
      display: inline-block;
      width: 4px;
      height: 22px;
      background: #3471ff;
      border-radius: 2px;
      position: absolute;
      top: 1px;
      left: 0;
    }
    .el-icon-close{
      position: absolute;
      right: 20px;
      top: 15px;
      font-size: 16px;
      cursor: pointer;
    }
  }
  .reply-list{
    max-height: calc(100% - 70px);
    overflow: hidden;
    overflow-y: auto;
    padding: 0 20px;
  }
  .reply-item{
    height: 42px;
    line-height: 42px;
    font-size: 14px;
    width: 100%;
    border-bottom: 1px solid #e4e7ed;
    display: flex;
    align-items: center;
    span{
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: pre;
    }
    .tool{
      i{
        cursor: pointer;
      }
    }
  }
}
.hide-header-delete-dialog.el-dialog__wrapper{
  /deep/.el-dialog{
    width: 540px;
    height: 240px;
    margin-top: calc(50vh - 120px)!important;
    border-radius: 6px!important;
    overflow: hidden;
    .el-dialog__header{
      display: none;
    }
    .el-dialog__header::before{
      display: none!important;
    }
    .el-dialog__body{
      height: calc(100% - 48px);
      padding: 0!important;
    }
    .delete-confirm-box{
      padding: 20px;
      box-sizing: border-box;
      .title{
        display: flex;
        align-items: center;
        i{
          font-size: 20px;
          color: #FF194C;
        }
        span{
          font-family: Source Han Sans-Bold, Source Han Sans;
          font-weight: 500;
          font-size: 16px;
          color: #FF194C;
          line-height: 20px;
          margin-left: 5px;
        }
      }
      .content{
        padding: 0 20px;
        margin-top: 10px;
        font-family: Source Han Sans-Regular, Source Han Sans;
        font-weight: 400;
        font-size: 14px;
        color: #363B4C;
        i{
          font-style: normal;
          color: #FF194C;
        }
      }
      .input-box{
        margin-top: 10px;
        padding: 0 20px;
        p{
          margin: 0 0 10px 0;
          font-family: Source Han Sans-Regular, Source Han Sans;
          font-weight: 400;
          font-size: 13px;
          color: #363B4C;
        }
      }
    }
    .footer{
      width: 100%;
      height: 60px;
      background: #FFFFFF;
      border-radius: 0px 0px 6px 6px;
      box-sizing: border-box;
      border-top: 1px solid #EEEFF0;
      display: flex;
      align-items: center;
      justify-content: flex-end;
      .footer-button{
        display: flex;
        align-items: center;
        .ftb{
          width: 60px;
          height: 32px;
          line-height: 32px;
          text-align: center;
          background: #F5F6F7;
          border-radius: 4px;
          cursor: pointer;
          font-family: Microsoft YaHei-Regular, Microsoft YaHei;
          font-weight: 400;
          font-size: 14px;
          color: #363B4C;
          margin-right: 16px;
        }
        .submit{
          background: #1E6FFF;
          color: #fff;
          &.disabled{
            cursor: not-allowed;
          }
        }
      }
    }
  }
}
.flow-button-div{
  .add-button{
    width: 120px;
    height: 36px;
    padding: 0;
    i{
      margin-left: 10px;
    }
  }
  .add-button.plain{
    background: #E4EDFF;
    color: #1E6FFF;
    margin-left: 8px;
  }
}
.el-dialog__wrapper.create-flow-by-model-dialog{
  /deep/.el-dialog{
    .el-dialog__body{
      padding: 0;
      padding-top: 15px;
    }
  }
}
.model-list-box{
  .search-input-box{
    margin: 0 32px;
    background-color: #f5f6f7;
    border-radius: 4px;
    height: 32px;
    display: flex;
    align-items: center;
    .split-line{
      background-color: #f0f1f2;
      height: 24px;
      width: 1px;
      margin-left: 24px;
      margin-right: 24px;
    }
    /deep/.el-input{
      .el-input__inner{
        height: 32px;
        line-height: 32px;
        background: transparent;
        border: 0;
      }
    }
    .icon{
      width: 14px;
      height: 14px;
      margin: 0 14px;
      cursor: pointer;
    }
    .h-line{
      height: 14px;
      background-color: #d7d8db;
      width: 1px;
      margin-left: 0;
      margin-right: 0;
    }

  }
  .model-list{
    margin: 0 32px;
    margin-top: 10px;
    height: 360px;
    position: relative;
    overflow: hidden;
    overflow-y: auto;
    .model-list-item{
      padding: 0 16px;
      margin-bottom: 4px;
      height: 32px;
      border-radius: 4px;
      font-family: Source Han Sans-Regular, Source Han Sans;
      font-weight: 400;
      font-size: 14px;
      color: #363B4C;
      line-height: 32px;
      box-sizing: border-box;
      cursor: pointer;
      &:hover{
        background: #F5F6F7;
      }
    }
    .loading{
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background-color: #fff;
      background-image: url('../../../public/jvs-ui-public/img/loading.gif');
      background-repeat: no-repeat;
      background-position: center;
    }
  }
  .footer{
    border-top: 1px solid #EEEFF0;
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    .el-pagination{
      padding: 0;
      .el-input-number{
        width: fit-content;
        height: 20px;
        /deep/.el-input{
          height: 20px;
          .el-input__inner{
            padding: 0;
            border: 0;
            width: 32px;
            height: 20px;
            line-height: 20px;
            background: #F5F6F7;
            border-radius: 2px;
            font-family: Microsoft YaHei-Regular, Microsoft YaHei;
            font-weight: 400;
            font-size: 14px;
            color: #575E73;
          }
        }
      }
      button{
        height: 16px;
        line-height: 20px;
        i{
          font-weight: bold;
        }
      }
      .text{
        margin-left: 4px;
        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
        font-weight: 400;
        font-size: 14px;
        color: #575E73;
        height: 20px;
        line-height: 24px;
      }
    }
  }
}
</style>
<style lang="scss">
.application-more-tool{
  min-width: 50px!important;
  width: 50px;
  padding: 0;
  ul{
    margin: 0;
    padding: 0;
    li{
      height: 32px;
      line-height: 32px;
      text-align: center;
      cursor: pointer;
    }
    li:hover{
      background: #eff2f7;
    }
  }
}
.reply-drawer{
  .el-drawer__title{
    margin-bottom: 0;
  }
}
.application-manage-page{
  .info-box{
    .tips{
      margin-top: 24px;
      overflow: hidden;
      .title{
        font-size: 16px;
        font-family: Microsoft YaHei, Microsoft YaHei;
        font-weight: 400;
        color: #1E6FFF;
        line-height: 21px;
      }
      .secTitle{
        color: #363B4C;
        font-size: 14px;
      }
      .tips-list{
        margin: 0;
        padding: 0;
        list-style: none;
        margin-top: 5px;
        li{
          margin: 0;
          padding: 0;
          height: 32px;
          line-height: 32px;
          color: #6F7588;
          list-style: none;
          font-size: 14px;
          display: flex;
          align-items: center;
          .dot{
            display: block;
            width: 3px;
            height: 3px;
            border-radius: 3px;
            background-color: #1E6FFF;
            margin-right: 4px;
          }
          .label{
            font-family: Microsoft YaHei-Bold;
            font-weight: 700;
            color: #363B4C;
          }
        }
      }
      .con-box{
        margin-top: 8px;
        background: #EDF4FF;
        border-radius: 4px;
        padding: 16px;
      }
    }
    .app-header{
      margin-top: 24px;
      height: 21px;
      font-size: 16px;
      font-family: Microsoft YaHei, Microsoft YaHei;
      font-weight: 400;
      color: #363B4C;
      line-height: 21px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      svg{
        width: 12px;
        height: 15px;
        margin-right: 10px;
      }
    }
    .app-header:nth-of-type(1) {
      margin-top: 44px;
    }
    .app-info{
      border-radius: 6px;
      background-color: #f6f7f9;
      font-size: 14px;
      padding: 20px 40px;
      margin: 20px 0;
      display: flex;
      box-sizing: border-box;
      .col-2{
        flex: 1;
        height: 50px;
        display: flex;
        align-items: center;
        color: #363B4C;
        .label{
          width: 160px;
          height: 100%;
          box-sizing: border-box;
          background: #F5F6F7;
          border-radius: 0;
          border: 1px solid #EEEFF0;
          border-left-width: 0;
          text-indent: 24px;
          line-height: 50px;
          font-size: 14px;
          font-family: Microsoft YaHei, Microsoft YaHei;
          font-weight: 400;
        }
        .con{
          height: 100%;
          width: calc(100% - 160px);
          background: #fff;
          opacity: 1;
          border: 1px solid #EEEFF0;
          line-height: 50px;
          box-sizing: border-box;
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 0 16px 0 24px;
          overflow: hidden;
          b{
            color: #333;
            cursor: pointer;
            display: inline-block;
            width: 14px;
            height: 14px;
          }
        }
      }
      .col-2:nth-of-type(1){
        .label{
          border-left-width: 1px;
          border-radius: 4px 0px 0px 4px;
        }
      }
      .col-2:nth-last-of-type(1){
        .con{
          border-radius: 0 4px 4px 0;
        }
      }
    }
    .app-info-text{
      background: none;
      padding: 0;
      margin: 0;
      margin-top: 16px;
    }
  }
}
</style>
