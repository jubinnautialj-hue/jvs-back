<template>
  <div class="node-config">
    <el-form label-position="top" label-width="90px">
      <div v-if="isRootNode"><!-- || isCsNode -->
        <el-form-item :label="isRootNode ? '谁可以发起此审批' : '选择要抄送的人员'" prop="text">
          <div v-if="isRootNode">
            <div v-for="(rot, rox) in props.purviews" :key="'root-node-purviews-item'+rox">
              <div>
                <el-radio-group  v-model="rot.personType" :disabled="disabled" style="margin-right:20px;" @change="changePersonType(rot)">
                  <el-radio label="all">所有人</el-radio>
                  <el-radio label="custom">自定义</el-radio>
                </el-radio-group>
              </div>
              <div v-if="rot.personType == 'custom'" class="label-button">
                <div class="label">选择人员</div>
                <div v-if="!(disabled ? (enableChange ? false : true) : false)" class="bottom-button">
                  <div class="button" @click="chooseUser('user', rox, rot)">
                    <div class="icon">
                      <svg>
                        <use xlink:href="#jvs-ui-icon-xinjian"></use>
                      </svg>
                    </div>
                    <span>添加人员</span>
                  </div>
                </div>
              </div>
              <div v-if="rot.personType == 'custom' && rot.personnels && rot.personnels.length > 0" class="tag-list-box">
                <el-tag
                  :type="'dept' === user.type ? 'info' : 'primary'"
                  v-for="(user, index) in rot.personnels"
                  @close="rot.personnels.splice(index, 1)"
                  :closable="disabled ? (enableChange ? true : false) : true"
                  :key="'user-tag'+index"
                >
                  {{ user.name }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-form-item>
      </div>

      <div v-if="isTjNode">
        <el-form-item label="调整优先级" prop="level">
          <el-popover placement="bottom" title="" width="424" trigger="click">
            <div style="padding: 0 10px;">
              <h4 style="margin: 5px 0;">拖拽条件调整优先级顺序</h4>
              <draggable
                style="width: 100%;min-height: 25px;"
                :list="prioritySortList"
                group="from"
                :options="{
                  animation: 300,
                  chosenClass: 'choose',
                  scroll: true,
                  sort: true,
                }"
              >
                <div class="drag-no-choose"
                  :style="list.id === selectedNode.id ? 'background: #1E6FFF; color:#fff;' : ''"
                  v-for="(list, index) in prioritySortList"
                  :key="'drag-no-'+index"
                  v-show="index < (prioritySortList.length-1)"
                >
                  <div>{{ list.name }}</div>
                  <div>优先级 {{ index + 1 }}</div>
                </div>
              </draggable>
            </div>
            <el-button class="sort-drag-button" :disabled="disabled" slot="reference">
              <div>
                <span>优先级{{ getNowNodeIndex + 1 }}</span>
                <svg>
                  <use xlink:href="#icon-jvs-xiala"></use>
                </svg>
              </div>
            </el-button>
          </el-popover>
        </el-form-item>
        <el-form-item label="条件关系组" prop="group">
          <div class="condition-div">
            <div class="condition-div-left">
              <div v-if="selectedNode && selectedNode.groups && selectedNode.groups.length > 0" :class="{'connection': true, 'and': selectedNode.connection == enumConst.logicType['AND']}">
                <div :class="{'connection-text': true, 'disabled': disabled}" @click="connectionChange">{{selectedNode.connection == enumConst.logicType['AND'] ? '且' : '或'}}</div>
              </div>
            </div>
            <div class="condition-div-right">
              <condition :selectedNode="selectedNode" :infoData="infoData"></condition>
            </div>
          </div>
          <div class="add-condi-group-button">
            <el-button type="primary" icon="el-icon-plus" @click="addConditionGroup">添加一组</el-button>
            <!-- <div class="text-p">注：只有必填选项才能作为审批条件</div> -->
          </div>
        </el-form-item>
      </div>

      <div v-if="isSpNode || isCsNode">
        <el-form-item v-if="isSpNode" label="选择审批人" prop="text" class="select-user-type">
          <el-radio-group v-model="props.type" :disabled="disabled" @change="handleRadioChange">
            <el-radio v-for="t in approvalType" :label="t.label" :key="t.label" :disabled="t.disabled">
              {{ t.text }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="isCsNode" label="抄送人类型" prop="text" class="select-user-type">
          <el-radio-group v-model="props.type" :disabled="disabled" @change="handleRadioChange">
            <el-radio v-for="t in approvalCCType" :label="t.label" :key="t.label" :disabled="t.disabled">
              {{ t.text }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <div>
          <div v-if="isCsNode && props.type === 'ASSIGN_CARBON_COPY'">
            <div class="label-button">
              <div v-if="!(disabled ? (enableChange ? false : true) : false)" class="bottom-button">
                <div class="button" @click="chooseUser('user')">
                  <div class="icon">
                    <svg>
                      <use xlink:href="#jvs-ui-icon-xinjian"></use>
                    </svg>
                  </div>
                  <span>{{isCsNode ? '选择' : '选择人员/部门'}}</span>
                </div>
              </div>
            </div>
            <div v-if="props.targetObj.personnels && props.targetObj.personnels.length > 0" class="tag-list-box">
              <el-tag
                :type="'dept' === user.type ? 'info' : 'primary'"
                v-for="(user, index) in props.targetObj.personnels"
                @close="props.targetObj.personnels.splice(index, 1)"
                :closable="disabled ? (enableChange ? true : false) : true"
                :key="'user-tag'+index"
              >
                {{ user.name }}
              </el-tag>
            </div>
          </div>
          <div v-if="isSpNode && props.type === enumConst.approvalType.ASSIGN_USER">
            <div class="label-button">
              <div class="label">选择人员</div>
              <div v-if="!(disabled ? (enableChange ? false : true) : false)" class="bottom-button">
                <div class="button" @click="chooseUser('user', null, props.targetObj)">
                  <div class="icon">
                    <svg>
                      <use xlink:href="#jvs-ui-icon-xinjian"></use>
                    </svg>
                  </div>
                  <span>添加人员</span>
                </div>
              </div>
            </div>
            <div v-if="props.targetObj.personnels && props.targetObj.personnels.length > 0" class="tag-list-box">
              <el-tag
                :type="'dept' === user.type ? 'info' : 'primary'"
                v-for="(user, index) in props.targetObj.personnels"
                @close="props.targetObj.personnels.splice(index, 1)"
                :closable="disabled ? (enableChange ? true : false) : true"
                :key="'choose-user'+index"
                >{{ user.name }}
              </el-tag>
            </div>
          </div>

          <div v-if="props.type === enumConst.approvalType.ROLE">
            <div class="label-button">
              <div class="label">选择角色</div>
              <div v-if="!(disabled ? (enableChange ? false : true) : false)" class="bottom-button">
                <div class="button" @click="chooseUser('role', null, props.targetObj)">
                  <div class="icon">
                    <svg>
                      <use xlink:href="#jvs-ui-icon-xinjian"></use>
                    </svg>
                  </div>
                  <span>添加角色</span>
                </div>
              </div>
            </div>
            <div v-if="props.targetObj.personnels && props.targetObj.personnels.length > 0" class="tag-list-box">
              <el-tag
                :type="'dept' === role.type ? 'info' : 'primary'"
                v-for="(role, index) in props.targetObj.personnels"
                @close="props.targetObj.personnels.splice(index, 1)"
                :closable="disabled ? (enableChange ? true : false) : true"
                :key="'choose-user'+index"
              >{{ role.name }}
              </el-tag>
            </div>
          </div>

          <div v-if="props.type === enumConst.approvalType.JOB">
            <div class="label-button">
              <div class="label">选择岗位</div>
              <div v-if="!(disabled ? (enableChange ? false : true) : false)" class="bottom-button">
                <div class="button" @click="chooseUser('job', null, props.targetObj)">
                  <div class="icon">
                    <svg>
                      <use xlink:href="#jvs-ui-icon-xinjian"></use>
                    </svg>
                  </div>
                  <span>添加岗位</span>
                </div>
              </div>
            </div>
            <div v-if="props.targetObj.personnels && props.targetObj.personnels.length > 0" class="tag-list-box">
              <el-tag
                type="primary"
                v-for="(job, index) in props.targetObj.personnels"
                @close="props.targetObj.personnels.splice(index, 1)"
                :closable="disabled ? (enableChange ? true : false) : true"
                :key="'choose-user'+index"
              >{{ job.name }}
              </el-tag>
            </div>
          </div>

          <div v-if="props.type === enumConst.approvalType.DEPT">
            <div class="label-button">
              <div class="label">选择部门</div>
              <div v-if="!(disabled ? (enableChange ? false : true) : false)" class="bottom-button">
                <div class="button" @click="chooseUser('dept', null, props.targetObj)">
                  <div class="icon">
                    <svg>
                      <use xlink:href="#jvs-ui-icon-xinjian"></use>
                    </svg>
                  </div>
                  <span>添加部门</span>
                </div>
              </div>
            </div>
            <div v-if="props.targetObj.personnels && props.targetObj.personnels.length > 0" class="tag-list-box">
              <el-tag
                type="primary"
                v-for="(job, index) in props.targetObj.personnels"
                @close="props.targetObj.personnels.splice(index, 1)"
                :closable="disabled ? (enableChange ? true : false) : true"
                :key="'choose-user'+index"
              >{{ job.name }}
              </el-tag>
            </div>
          </div>

          <div v-if="props.type === enumConst.approvalType.SELF">
            <p class="text-p">{{isSpNode ? '发起人自己作为审批人进行审批' : '抄送发起人自己'}}</p>
          </div>

          <div v-if="props.type === 'USER_FIELD'" style="margin-top: 8px;">
            <el-select v-model="checkedComs" multiple clearable size="mini" @change="userComChange" class="mutiple-select">
              <el-option v-for="(com, cix) in userComList" :key="'user-com-'+cix" :label="com.label" :value="com.prop"></el-option>
            </el-select>
          </div>

          <div v-if="props.type === 'DEPT_FIELD'" style="margin-top: 8px;">
            <el-select v-model="checkedComs" multiple clearable size="mini" @change="userComChange" class="mutiple-select">
              <el-option v-for="(com, cix) in deptComList" :key="'dept-com-'+cix" :label="com.label" :value="com.prop"></el-option>
            </el-select>
          </div>

          <!-- 范围类型 -->
          <div v-if="[enumConst.approvalType.ASSIGN_USER, enumConst.approvalType.SELF_SELECT].indexOf(props.type) > -1">
            <el-form-item label="选择范围" class="select-user-type" :disabled="disabled">
              <el-radio-group v-model="props.personnelScope.type">
                <el-radio label="ALL">所有人</el-radio>
                <el-radio label="CUSTOM">自定义</el-radio>
              </el-radio-group>
            </el-form-item>
          </div>
          <!-- 范围选择 -->
          <div v-if="[enumConst.approvalType.ASSIGN_USER, enumConst.approvalType.SELF_SELECT].indexOf(props.type) > -1 && (props.personnelScope && props.personnelScope.type == 'CUSTOM')">
            <div class="label-button">
              <div class="label">选择人员</div>
              <div v-if="!(disabled ? (enableChange ? false : true) : false)" class="bottom-button">
                <div class="button" @click="chooseScopeUser('user', props.personnelScope.personnelScopes)">
                  <div class="icon">
                    <svg>
                      <use xlink:href="#jvs-ui-icon-xinjian"></use>
                    </svg>
                  </div>
                  <span>添加人员</span>
                </div>
              </div>
            </div>
            <div class="tag-list-box">
              <el-tag
                :type="'dept' === user.type ? 'info' : 'primary'"
                v-for="(user, index) in props.personnelScope.personnelScopes"
                @close="props.personnelScope.personnelScopes.splice(index, 1)"
                :closable="disabled ? (enableChange ? true : false) : true"
                :key="'user-tag'+index"
              >
                {{ user.name }}
              </el-tag>
            </div>
          </div>
          <!-- 审批人为角色 范围设置 -->
          <div v-if="props.type === enumConst.approvalType.ROLE" class="node-form-design">
            <div class="node-form-design-item">
              <b>范围设置
                <el-tooltip
                  class="item"
                  effect="dark"
                  content="筛选角色管理范围包含指定部门的用户作为审批人，默认以发起人所属部门作为筛选条件，可指定表单内部门组件字段作为筛选条件"
                  placement="top-start">
                  <svg style="width: 20px; height: 20px; margin-left: 12px; cursor: pointer; fill: #878787;">
                    <use xlink:href="#icon-help"></use>
                  </svg>
                </el-tooltip>
              </b>
            </div>
            <el-select v-model="roleScopeCondition" clearable size="mini" @change="roleScopeConditionChange">
              <el-option v-for="(com, cix) in deptComList" :key="'dept-com-'+cix" :label="com.label" :value="com.prop"></el-option>
            </el-select>
          </div>

          <!-- 暂时屏蔽！！！！！！！！！！！！！！！！！ -->
          <div v-if="false && props.type === enumConst.approvalType.SELF_SELECT">
            <el-select
              size="small"
              v-model="props.targetObj.multiple"
              placeholder="选择人数"
              :disabled="disabled ? (enableChange ? false : true) : false"
            >
              <el-option :value="false" label="自选一个人"></el-option>
              <el-option :value="true" label="自选多个人"></el-option>
            </el-select>
          </div>
        </div>

        <el-form-item v-if="false" label="审批同意时是否需要手写签字">
          <el-switch
            :disabled="false"
            inactive-text="不用"
            active-text="需要"
            v-model="props.sign"
            :disable="$store.state.flow.template.baseSetup.sign ? true : disabled"
          ></el-switch>
          <el-tooltip
            class="item"
            effect="dark"
            content="如果全局设置了需要签字，则此处不生效"
            placement="top-start"
          >
            <i
              class="el-icon-question"
              style="margin-left: 10px; font-size: medium; color: #b0b0b1"
            ></i>
          </el-tooltip>
        </el-form-item>

        <el-form-item v-if="isSpNode && [enumConst.approvalType.ASSIGN_USER, enumConst.approvalType.ROLE, enumConst.approvalType.JOB, enumConst.approvalType.DEPT, enumConst.approvalType.SELF_SELECT].indexOf(props.type) > -1" label="禁止动态选择审批人" prop="text" class="select-user-type">
          <div class="node-form-design-item">
            <div style="display: flex;align-items: center;">
              <el-switch v-model="props.disableDynamicApprover" :disabled="disabled || [enumConst.approvalType.SELF_SELECT].indexOf(props.type) > -1" size="mini"></el-switch>
            </div>
          </div>
        </el-form-item>

        <div v-if="[enumConst.approvalType.LEADER, enumConst.approvalType.LEADER_TOP].indexOf(props.type) > -1">
          <el-form-item :label="props.type == enumConst.approvalType.LEADER_TOP ? '主管来源' : '指定主管'" prop="text" class="leader-source">
            <div class="leader-source-content">
              <el-select v-model="props.leader.leaderSource" @change="leaderSourceChange">
                <el-option label="发起人" :value="enumConst.LeaderSourceEnum.SEND_USER"></el-option>
                <el-option label="审批节点" :value="enumConst.LeaderSourceEnum.FLOW_NODE"></el-option>
                <el-option label="成员字段" :value="enumConst.LeaderSourceEnum.USER_FIELD"></el-option>
              </el-select>
              <el-select v-if="props.leader.leaderSource == enumConst.LeaderSourceEnum.FLOW_NODE" v-model="props.leader.flowNodeConfig.nodeIds" multiple clearable size="mini" class="mutiple-select" style="margin-left: 8px;">
                <el-option v-for="(com, cix) in getEnableNodeList(nodePathObj[node.id])" :key="'path-com-'+cix" :label="com.name" :value="com.id"></el-option>
              </el-select>
              <el-select v-if="props.leader.leaderSource == enumConst.LeaderSourceEnum.USER_FIELD" v-model="leaderComs" multiple clearable size="mini" @change="leaderComChange" class="mutiple-select" style="margin-left: 8px;">
                <el-option v-for="(com, cix) in userComList" :key="'user-com-'+cix" :label="com.label" :value="com.prop"></el-option>
              </el-select>
              <div v-if="props.type == enumConst.approvalType.LEADER" style="margin-left: 8px;">
                <span>的第</span>
                <el-input-number
                  :min="1"
                  :max="20"
                  :step="1"
                  size="mini"
                  v-model="props.leader.leaderLevel"
                  :disabled="disabled"
                ></el-input-number>
                <span>级主管</span>
              </div>
            </div>
          </el-form-item>
          <el-form-item v-if="props.type === enumConst.approvalType.LEADER_TOP" label="审批终点" prop="text" class="approve-end">
            <el-radio-group v-model="props.leader.endCondition" :disabled="disabled">
              <el-radio :label="enumConst.endCondition.TOP">直到主管来源最上层主管</el-radio>
              <el-radio :label="enumConst.endCondition.LEAVE">不超过主管来源的</el-radio>
            </el-radio-group>
            <div v-if="enumConst.endCondition.TOP !== props.leader.endCondition" class="approve-end-leave">
              <span style="margin-right: 10px;">第</span>
              <el-input-number
                :min="1"
                :max="20"
                :step="1"
                size="mini"
                v-model="props.leader.leaderLevel"
                :disabled="disabled"
              ></el-input-number>
              <span style="margin-left: 10px;">级主管</span>
            </div>
          </el-form-item>
        </div>

        <el-form-item v-if="isSpNode" label="审批期限（为 0 则不生效）" prop="timeLimit">
          <div style="display: flex;align-items: center;">
            <el-select
              :disabled="disabled"
              v-model="props.timeLimit.type"
              size="mini"
              placeholder="维度 天 / 小时"
              @change="timeLimitTypeChange"
              style="width: 120px;"
            >
              <el-option :value="enumConst.timeLimitType.MINUTE" label="分钟"></el-option>
              <el-option :value="enumConst.timeLimitType.HOUR" label="小时"></el-option>
              <el-option :value="enumConst.timeLimitType.DAY" label="天"></el-option>
            </el-select>
            <el-input-number
              :disabled="disabled"
              :min="0"
              :max="props.timeLimit.type === 'HOUR' ? 100 : 20"
              :step="1"
              size="mini"
              :precision="0"
              v-model="props.timeLimit.limit"
              style="margin-left: 8px;flex: 1;"
            ></el-input-number>
            <span v-if="false">{{props.timeLimit.type === enumConst.timeLimitType.HOUR ? "小时" : (props.timeLimit.type == enumConst.timeLimitType.DAY ? "天" : "分钟")}}</span>
          </div>
        </el-form-item>

        <el-form-item
          label="审批期限超时后执行"
          prop="level"
          v-if="isSpNode && props.timeLimit.limit > 0"
        >
          <el-radio-group v-model="props.timeLimit.event.type" :disabled="disabled">
            <el-radio v-for="evs in timeoutEvents" :label="evs.event" :key="evs.event">{{ evs.name }}</el-radio>
          </el-radio-group>
          <!-- <div>
            <span style="color:#4987ff; font-size: small">提醒 </span>
            <el-select v-model="props.approval.timeoutEvent.userType" size="mini" placeholder="提醒谁" style="width:100px">
              <el-option :value="'sender'" label="发起人"></el-option>
              <el-option :value="'select'" label="指定成员"></el-option>
            </el-select>
          </div> -->
          <div v-if="props.timeLimit.event.type === enumConst.timeoutEvent.NOTIFY">
            <div>
              <el-checkbox v-model="props.timeLimit.event.loop" :disabled="disabled">循环提醒</el-checkbox>
              <span v-if="props.timeLimit.event.loop" style="margin: 0 10px;">每隔</span>
              <el-input-number
                :min="1"
                :max="20"
                :step="1"
                size="mini"
                v-model="props.timeLimit.event.loopTime"
                :disabled="disabled"
              >
              </el-input-number>
              <span style="margin-left: 10px;">天</span>
            </div>
            <div class="text-p" style="margin-top: 8px;">默认提醒当前审批人，不开启循环仅提醒一次</div>
          </div>
        </el-form-item>
      </div>

      <div v-if="showModel && !isRootNode">
        <el-form-item label="多人审批时审批方式" prop="text" class="approve-mode">
          <el-radio-group v-model="props.mode" :disabled="disabled">
            <el-radio :label="enumConst.approvalMode.AND">会签</el-radio>
            <div v-if="props.mode == enumConst.approvalMode.AND" class="radio-item-box">
              <div class="radio-item-box-item">
                <span class="label">会签比例</span>
                <el-input-number v-model="props.modeProps.value" :min="1" :max="100" :precision="0" size="mini" :controls="false"></el-input-number>
                <span class="label unit">%</span>
              </div>
            </div>
            <el-radio :label="enumConst.approvalMode.OR">或签（有一人同意即可）</el-radio>
            <el-radio :label="enumConst.approvalMode.NEXT">按选择顺序依次审批</el-radio>
          </el-radio-group>
          <div v-if="[enumConst.approvalMode.AND, enumConst.approvalMode.OR].indexOf(props.mode) > -1" style="margin-top: 10px;">
            <div style="display: flex;align-items: center;">
              <span style="margin-right: 10px;">立即流转</span>
              <el-switch v-model="props.modeProps.endNow" :disabled="disabled" size="mini"></el-switch>
              <el-tooltip
                class="item"
                effect="dark"
                :content="`${props.mode == enumConst.approvalMode.AND ? '达到会签比例后立即流转到下一节点，否则需所有审批人完成审批后才会流转' : '以第一个提交的审批结果为最终审批结果。若同意即通过，若拒绝则终止。'}`" 
                placement="top-start">
                <svg style="width: 20px; height: 20px; margin-left: 12px; cursor: pointer; fill: #878787;">
                  <use xlink:href="#icon-help"></use>
                </svg>
              </el-tooltip>
            </div>
          </div>
        </el-form-item>
      </div>

      <div v-if="isSpNode && showUserEmpty">
        <el-form-item label="审批人为空时" prop="text" class="approve-mode">
          <el-checkbox v-model="checked" :disabled="disabled" @change="handleCheckChange">自动转交管理员</el-checkbox>
          <!-- <el-radio v-model="props.userEmpty" :label="enumConst.userEmpty.TO_ADMIN">自动转交管理员</el-radio>
          <el-radio-group v-model="props.userEmpty">
            <el-radio :label="enumConst.userEmpty.TO_PASS">自动通过</el-radio>
            <el-radio :label="enumConst.userEmpty.TO_ADMIN">自动转交管理员</el-radio>
            <el-radio :label="enumConst.userEmpty.TO_USER">转交到指定人员</el-radio>
          </el-radio-group> -->
        </el-form-item>
      </div>
    </el-form>
    <!-- 设计表单 -->
    <div v-if="['TJ', 'CS', 'AUTOMATION', 'MESS', 'PB'].indexOf(selectedNode.type) == -1" class="node-form-design">
      <div class="node-form-design-item">
        <b>表单设计</b>
        <el-radio-group v-if="selectedNode.type !='ROOT'" v-model="nodeForm.sendUserForm">
          <el-radio :label="true">使用发起人表单</el-radio>
          <el-radio :label="false">自定义表单</el-radio>
        </el-radio-group>
        <div class="node-form-design-button">
          <jvs-button v-if="!nodeForm.sendUserForm || this.selectedNode.type =='ROOT'" :disabled="disabled" @click="onlineDesign">配置表单</jvs-button>
          <jvs-button v-if="(!nodeForm.sendUserForm || this.selectedNode.type =='ROOT') && (!nodeForm || !nodeForm.formId)" :disabled="disabled" @click="chooseForm">选择已有表单</jvs-button>
          <jvs-button v-if="nodeForm && nodeForm.formId && (this.selectedNode.type =='ROOT' ? true : !nodeForm.sendUserForm)" type="text" :disabled="disabled" @click="cancelDesign">取消设计</jvs-button>
        </div>
      </div>
    </div>
    <!-- 审批节点-自动审批 -->
    <div v-if="isSpNode" class="node-form-design">
      <div class="node-form-design-item">
        <b>自动审批</b>
        <div>
          <el-checkbox v-model="props.autoApproval.selfAuto" :disabled="disabled"></el-checkbox>
          <span style="margin-left: 8px;font-family: Microsoft YaHei-Regular, Microsoft YaHei;font-weight: 400;font-size: 14px;color: #363B4C;">发起人自动审批（当前节点人员为发起人时，自动审批）</span>
        </div>
        <div style="margin-top: 8px;">
          <el-checkbox v-model="props.autoApproval.adjacentNode" :disabled="disabled"></el-checkbox>
          <span style="margin-left: 8px;font-family: Microsoft YaHei-Regular, Microsoft YaHei;font-weight: 400;font-size: 14px;color: #363B4C;">相邻节点自动审批（当前节点人员为上一节点人员时，自动审批）</span>
        </div>
      </div>
    </div>
    <!-- 手写签名 -->
    <div v-if="isSpNode" class="node-form-design">
      <div class="node-form-design-item">
        <b>手写签名</b>
        <div style="display: flex;align-items: center;">
          <el-switch v-model="selectedNode.props.enableSign" :disabled="disabled" size="mini"></el-switch>
          <el-tooltip
            class="item"
            effect="dark"
            :content="`开启手写签名后，处理该节点待办时，必须进行手写签名才能通过/拒绝`" 
            placement="top-start">
            <svg style="width: 20px; height: 20px; margin-left: 12px; cursor: pointer; fill: #878787;">
              <use xlink:href="#icon-help"></use>
            </svg>
          </el-tooltip>
        </div>
      </div>
    </div>
    <!-- 审批按钮 -->
    <div v-if="isSpNode" class="node-form-design">
      <div class="node-form-design-item">
        <b>审批按钮</b>
        <el-table
          :data="selectedNode.props.btn"
          style="width: 100%;"
        >
          <el-table-column label="按钮" width="100">
            <template slot-scope="scope">
              {{ scope.row.name }}
            </template>
          </el-table-column>
          <el-table-column label="显示名称">
            <template slot-scope="scope">
              <el-input v-model="scope.row.displayName" :placeholder="scope.row.name"></el-input>
            </template>
          </el-table-column>
          <el-table-column label="启用" width="100">
            <template slot-scope="scope">
              <el-switch v-model="scope.row.enable" :disabled="disabled" @change="enableChangeHandle(scope.$index)"></el-switch>
            </template>
          </el-table-column>
          <el-table-column label="逻辑" width="100">
            <template slot-scope="scope">
              <div style="display: flex;align-items: center;">
                <el-button v-if="scope.row.enable && ['SAVE', 'APPEND'].indexOf(scope.row.operation) == -1" size="mini" type="text" @click="ruleDesign(scope.row, scope.$index, selectedNode.props.btn)">设计</el-button>
                <div v-if="scope.row.enable && scope.row.operation == 'BACK'" class="split-line"></div>
                <el-button v-if="scope.row.enable && scope.row.operation == 'BACK'" size="mini" type="text" @click="backSetting(scope.$index)">回退规则</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    <!-- 加签设置 -->
    <div v-if="isSpNode && hasAppend" class="node-form-design">
      <div class="node-form-design-item">
        <b>加签位置</b>
        <el-checkbox-group v-model="selectedNode.props.appendApproval.point" :disabled="disabled">
          <el-checkbox label="BEFORE">前加签</el-checkbox>
          <el-checkbox label="AFTER">后加签</el-checkbox>
        </el-checkbox-group>
      </div>
      <div class="node-form-design-item">
        <b>加签审批结果是否生效</b>
        <el-switch size="mini" v-model="selectedNode.props.appendApproval.validApproval" :disabled="disabled"></el-switch>
      </div>
      <div class="node-form-design-item">
        <b>加签按钮设置</b>
        <el-table
          :data="selectedNode.props.appendApproval.btn"
          style="width: 100%;border: 1px solid #EBEEF5;border-bottom: 0;"
        >
          <el-table-column label="按钮" width="100">
            <template slot-scope="scope">
              {{ scope.row.name }}
            </template>
          </el-table-column>
          <el-table-column label="显示名称">
            <template slot-scope="scope">
              <el-input v-model="scope.row.displayName" :placeholder="scope.row.name"></el-input>
            </template>
          </el-table-column>
          <el-table-column label="启用" width="100">
            <template slot-scope="scope">
              <el-switch v-model="scope.row.enable" :disabled="disabled"></el-switch>
            </template>
          </el-table-column>
          <el-table-column label="逻辑" width="100">
            <template slot-scope="scope">
              <el-button v-if="scope.row.enable && ['SAVE', 'APPEND'].indexOf(scope.row.operation) == -1" :disabled="disabled" size="mini" type="text" @click="ruleDesign(scope.row, scope.$index, selectedNode.props.appendApproval.btn, 'appendApproval')">设计</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    <!-- 集成&自动化 -->
    <div v-if="['AUTOMATION'].indexOf(selectedNode.type) > -1" class="node-form-design">
      <div class="node-form-design-item">
        <b>名称</b>
        <el-input size="mini" v-model="selectedNode.props.automation.name" :disabled="disabled" placeholder="请输入逻辑名称" @input="handleInput"></el-input>
      </div>
      <div class="node-form-design-item">
        <b>逻辑设计</b>
        <jvs-button :disabled="disabled" size="mini" @click="designRule()" style="width: 128px;height: 32px;background: #EDF4FF;color: #1E6FFF;font-family: Microsoft YaHei-Regular, Microsoft YaHei;font-weight: 400;font-size: 14px;color: #1E6FFF;">逻辑设计</jvs-button>
      </div>
    </div>

    <!-- 选择人员 -->
    <userSelector
      ref="processUserSelector"
      :selectable="true"
      :autoClose="true"
      :currentActiveName="currentActiveName"
      :userEnable="!isSpRoleNode && !isSpJobNode && !isSpDeptNode"
      :deptEnable="isRootNode || isCsNode || isSpDeptNode"
      :roleEnable="isRootNode || isCsNode || isSpRoleNode"
      :jobEnable="isRootNode || isCsNode || isSpJobNode"
      @submit="submitHandle">
    </userSelector>

    <!-- 人员范围选择 -->
    <userSelector
      ref="scopeUserSelector"
      :selectable="true"
      :autoClose="true"
      :currentActiveName="currentActiveName"
      :userEnable="true"
      :deptEnable="true"
      :roleEnable="true"
      :jobEnable="true"
      @submit="submitScopeHandle">
    </userSelector>

    <!-- 选择表单 -->
    <el-dialog
      title="表单选择"
      class="place-dialog"
      :visible.sync="placeVisible"
      append-to-body
      width="20%"
      :before-close="placeClose">
      <div class="place-form-desc">快速使用其他表单的设计</div>
      <div class="place-form-list">
        <div class="palce-form-item" v-for="item in placeList" :key="item.id+'-place-item'" @click="placeFormHandle(item)">
          <svg style="margin-right: 20px;width: 25px;height: 25px;">
            <use :xlink:href="'#' + item.icon"></use>
          </svg>
          <span>{{item.name}}</span>
        </div>
      </div>
    </el-dialog>

    <!-- 逻辑设计  可以选择已有逻辑引擎 -->
    <el-dialog
      title="业务逻辑"
      width="30%"
      append-to-body
      :visible.sync="netSetVisible"
      :close-on-click-modal="false"
      :before-close="netSetClose">
      <div v-if="netSetVisible">
        <div style="display: flex;align-items: center;">
          <el-button size="mini" type="primary" @click="newRuleSet" :loading="newRuleSetLoading">新建业务逻辑</el-button>
        </div>
        <div style="display: flex;align-items: center;margin-top: 15px;">
          <span style="margin-right: 10px;">选择逻辑</span>
          <el-select v-model="currentRow.automation.key " size="mini" filterable clearable @change="eventHttpChange">
            <el-option v-for="rit in allRuleList" :key="'all-rule-item-'+rit.secret" :label="rit.name" :value="rit.secret"></el-option>
          </el-select>
          <i v-if="currentRow.automation.key" class="el-icon-edit-outline form-icon-btn" style="margin-left: 10px;cursor: pointer;" @click="viewRule"></i>
        </div>
      </div>
    </el-dialog>
    <!-- 回退规则 -->
    <el-dialog
      class="style-dialog"
      title="回退规则"
      append-to-body
      :visible.sync="backVisible"
      :close-on-click-modal="false"
      :before-close="backClose">
      <div v-if="backVisible" class="custom-box style-dialog-box">
        <div class="custom-box-item">
          <div class="label">回退范围</div>
          <div class="content">
            <el-select v-model="currentRow.back.scope" size="mini" style="width:100%;">
              <el-option label="自选已审批节点" value="APPROVED"></el-option>
              <el-option label="上一个审批节点" value="PREVIOUS"></el-option>
              <el-option label="发起人节点" value="ROOT"></el-option>
            </el-select>
          </div>
        </div>
        <div class="custom-box-item">
          <div class="label">被回退的数据重新提交后</div>
          <div class="content">
            <el-select v-model="currentRow.back.resubmit" size="mini" style="width:100%;">
              <el-option label="按流程顺序审批" value="SEQUENCE"></el-option>
              <el-option label="直达当前节点" value="DIRECT_CURRENT_NODE"></el-option>
            </el-select>
          </div>
        </div>
      </div>
      <div v-if="backVisible" class="footer">
        <div class="footer-button">
          <div class="ftb" @click="backClose">取消</div>
          <div class="ftb submit" @click="backSubmit">确定</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import condition from "./condition";
import enumConst, {approvalMode} from "../common/enumConst";
import draggable from "vuedraggable";
import userSelector from '@/components/basic-assembly/userSelector'
import {getRuleIdByComponentId} from "@/views/flowable/api/flowable";
import { getPageList, getAllUserComList } from '../../../api/flowable'
import { getAllRule } from '@/components/template/api'
import { getFormInfo } from '@/views/page/api/formlist'
export default {
  name: "nodeConfig",
  components: { draggable, condition, userSelector },
  props: {
    node: {
      default: () => {
        return {};
      },
      type: Object,
    },
    // 工作流信息
    infoData: {
      type: Object
    },
    // 角色列表
    roleOption: {
      type: Array,
      default: () => {
        return []
      }
    },
    disabled: {
      type: Boolean
    },
    enableChange: {
      type: Boolean
    }
  },
  data() {
    return {
      checked: false,
      enumConst: enumConst,
      select: [],
      approval: [],
      approvalType: [
        { label: enumConst.approvalType.ASSIGN_USER, text: "指定人员" },
        { label: enumConst.approvalType.SELF_SELECT, text: "发起人自选" },
        { label: enumConst.approvalType.LEADER_TOP, text: "连续多级主管", disabled: false },
        { label: enumConst.approvalType.LEADER, text: "直属主管", disabled: false },
        { label: enumConst.approvalType.ROLE, text: "角色" },
        { label: enumConst.approvalType.SELF, text: "发起人自己" },
        { label: enumConst.approvalType.JOB, text: "岗位" },
        { label: 'DEPT', text: "部门" },
        { label: 'USER_FIELD', text: "成员字段" },
        { label: 'DEPT_FIELD', text: "部门字段" },
      ],
      approvalCCType: [
        { label: 'ASSIGN_CARBON_COPY', text: "指定抄送人" },
        { label: enumConst.approvalType.SELF_SELECT, text: "发起人自选" },
        { label: enumConst.approvalType.LEADER, text: "直属主管", disabled: false },
        { label: enumConst.approvalType.SELF, text: "发起人自己" },
        { label: 'USER_FIELD', text: "成员字段" },
      ],
      timeoutEvents: [
        { event: enumConst.timeoutEvent.PASS, name: "自动通过" },
        { event: enumConst.timeoutEvent.REFUSE, name: "自动拒绝" },
        { event: enumConst.timeoutEvent.NOTIFY, name: "发送提醒" },
      ],
      roleList: [], // 选择角色
      isSpRoleNode: false,
      currentActiveName: 'user',
      selectIndex: -1,
      placeVisible: false,
      queryParams: {
        name: ''
      },
      placeList: [], // 可选引用表单列表
      isSpJobNode: false,
      allRuleList: [], // 当前模型下的所有的逻辑引擎列表
      newRuleSetLoading: false, // 新增逻辑loading
      netSetVisible: false,
      currentRow: null,
      currentIndex: -1,
      currentType: '',
      userComList: [],
      leaderComs: [],
      checkedComs: [],
      roleScopeCondition: '',
      deptComList: [],
      backVisible: false,
      nodePathObj: {},
      isSpDeptNode: false,
    };
  },
  computed: {
    selectedNode() {
      if(this.$store.state.flow.selectedNode.props) {
        if(this.$store.state.flow.selectedNode.props.btn){
          let addTrans = true
          let addAppend = true
          for(let i in this.$store.state.flow.selectedNode.props.btn) {
            if(this.$store.state.flow.selectedNode.props.btn[i].operation == 'TRANSFER') {
              addTrans = false
            }
            if(this.$store.state.flow.selectedNode.props.btn[i].operation == 'APPEND') {
              addAppend = false
            }
          }
          if(addTrans) {
            this.$store.state.flow.selectedNode.props.btn.push({name: '转交', loading: false, operation: 'TRANSFER', enable: false, automation: { name: '', key: '' }})
          }
          if(addAppend) {
            this.$store.state.flow.selectedNode.props.btn.push({name: '加签', loading: false, operation: 'APPEND', enable: false, automation: { name: '', key: '' }})
          }
        }
        if(this.$store.state.flow.selectedNode.type == enumConst.nodeType.SP && !this.$store.state.flow.selectedNode.props.appendApproval) {
          this.$set(this.$store.state.flow.selectedNode.props, 'appendApproval', {
            point: ['BEFORE'],
            validApproval: false,
            btn: JSON.parse(JSON.stringify(enumConst.nodeButtonList))
          })
        }
        if(this.$store.state.flow.selectedNode.type === enumConst.nodeType.ROOT) {
          if(!this.$store.state.flow.selectedNode.props.purviews){
           this.$set(this.$store.state.flow.selectedNode.props, 'purviews', [{group: 'send_flow', personType: 'all', personnels: []}])
          }
        }
        if(this.$store.state.flow.selectedNode.type == enumConst.nodeType.SP && !this.$store.state.flow.selectedNode.props.autoApproval) {
          this.$set(this.$store.state.flow.selectedNode.props, 'autoApproval', {selfAuto: false, adjacentNode: false})
        }
        if(this.$store.state.flow.selectedNode.type == enumConst.nodeType.SP && !this.$store.state.flow.selectedNode.props.modeProps) {
          this.$set(this.$store.state.flow.selectedNode.props, 'modeProps', {countersignRule: 'RATIO', value: 100, endNow: true})
        }
        if(this.$store.state.flow.selectedNode.type == enumConst.nodeType.SP && [enumConst.approvalType.LEADER, enumConst.approvalType.LEADER_TOP].indexOf(this.$store.state.flow.selectedNode.props.type) > -1) {
          if(!this.$store.state.flow.selectedNode.props.leader.leaderSource) {
            this.$set(this.$store.state.flow.selectedNode.props.leader, 'leaderSource', enumConst.LeaderSourceEnum.SEND_USER)
          }else{
            this.leaderComs = []
            if(this.$store.state.flow.selectedNode.props.leader.userFieldConfig && this.$store.state.flow.selectedNode.props.leader.userFieldConfig.personnels) {
              this.$store.state.flow.selectedNode.props.leader.userFieldConfig.personnels.filter(fit => {
                this.leaderComs.push(fit.id)
              })
            }
          }
        }
        if(this.$store.state.flow.selectedNode.type == enumConst.nodeType.CS) {
          if(!this.$store.state.flow.selectedNode.props.type) {
            this.$set(this.$store.state.flow.selectedNode.props, 'type', 'ASSIGN_CARBON_COPY')
          }
          if(!this.$store.state.flow.selectedNode.props.leader) {
            this.$set(this.$store.state.flow.selectedNode.props, 'leader', {leaderLevel: 1, endCondition: enumConst.endCondition.TOP})
          }
        }
        if([enumConst.approvalType.ASSIGN_USER, enumConst.approvalType.SELF_SELECT].indexOf(this.$store.state.flow.selectedNode.props.type) > -1 && !this.$store.state.flow.selectedNode.props.personnelScope) {
          this.$set(this.$store.state.flow.selectedNode.props, 'personnelScope', {type: 'ALL', personnelScopes: []})
        }
        if(['USER_FIELD', 'DEPT_FIELD'].indexOf(this.$store.state.flow.selectedNode.props.type) > -1) {
          this.checkedComs = []
          if(this.$store.state.flow.selectedNode.props.targetObj && this.$store.state.flow.selectedNode.props.targetObj.personnels) {
            this.$store.state.flow.selectedNode.props.targetObj.personnels.filter(fit => {
              this.checkedComs.push(fit.id)
            })
          }
        }
        if(this.$store.state.flow.selectedNode.props.targetObj && this.$store.state.flow.selectedNode.props.targetObj.personnelResolver) {
          if(this.$store.state.flow.selectedNode.props.targetObj.personnelResolver.roleScopeConditions && this.$store.state.flow.selectedNode.props.targetObj.personnelResolver.roleScopeConditions.length > 0) {
            this.roleScopeCondition = this.$store.state.flow.selectedNode.props.targetObj.personnelResolver.roleScopeConditions[0].id
          }
        }
      }
      return this.$store.state.flow.selectedNode;
    },
    getNowNodeIndex() {
      for (let i = 0; i < this.prioritySortList.length; i++) {
        if (this.selectedNode.id === this.prioritySortList[i].id) {
          return i;
        }
      }
      return 0;
    },
    prioritySortList() {
      return this.$store.state.flow.parentMap.get(this.selectedNode.pid).conditions || [];
    },
    conditionGroups() {
      return this.$store.state.flow.parentMap.get(this.selectedNode.pid);
    },
    isRootNode() {
      return this.selectedNode.type === enumConst.nodeType.ROOT;
    },
    isTjNode() {
      return this.selectedNode.type === enumConst.nodeType.TJ;
    },
    isCsNode() {
      return this.selectedNode.type === enumConst.nodeType.CS;
    },
    isSpNode() {
      return this.selectedNode.type === enumConst.nodeType.SP;
    },
    onlySelectUser() {
      return (
        this.selectedNode.type === enumConst.nodeType.CS ||
        this.selectedNode.type === enumConst.nodeType.SP ||
        this.props.type === "1"
      );
    },
    props() {
      return this.$store.state.flow.selectedNode.props;
    },
    showModel() {
      if(this.props.type === enumConst.approvalType.ASSIGN_USER && this.node.type != 'CS'){
        if(this.props.targetObj && this.props.targetObj.personnels && this.props.targetObj.personnels.length > 1) {
          if(this.props.mode == 'DEFAULT') {
            this.props.mode = enumConst.approvalMode.AND
          }
        }else{
          this.props.mode = 'DEFAULT'
        }
      }
      return (
        (this.props.targetObj && this.props.targetObj.personnels && this.props.targetObj.personnels.length > 1 && this.props.type === enumConst.approvalType.ASSIGN_USER && this.node.type != 'CS') ||
        ([enumConst.approvalType.SELF_SELECT, enumConst.approvalType.ROLE, enumConst.approvalType.JOB, enumConst.approvalType.DEPT, 'USER_FIELD', 'DEPT_FIELD'].indexOf(this.props.type) > -1 && this.node.type != 'CS') ||
        (this.props.type === enumConst.approvalType.ROLE && this.props.targetObj.personnels && this.props.targetObj.personnels.length > 0 && this.node.type != 'CS')
      );
    },
    showUserEmpty() {
      if(this.props.userEmpty && this.props.userEmpty == this.enumConst.userEmpty.TO_ADMIN) {
        this.checked = true
      }
      return (
        this.props.type === enumConst.approvalType.LEADER_TOP ||
        this.props.type === enumConst.approvalType.LEADER
      );
    },
    nodeForm() {
      if(!this.$store.state.flow.selectedNode.nodeForm) {
        this.$set(this.$store.state.flow.selectedNode, 'nodeForm', {
          formId: "", // 表单id
          sendUserForm: true, // true--使用发起人表单，false-不使用发起人表单
          version: "" // 表单版本
        })
      }
      return this.$store.state.flow.selectedNode.nodeForm
    },
    hasAppend() {
      let bool = false
      this.selectedNode.props.btn.filter(item => {
        if(item.operation == 'APPEND' && item.enable){
          bool = true
        }
      })
      return bool
    },
    nodeDomTree() {
      return this.$store.state.flow.template.process
    }
  },
  methods: {
    // 修改集成&自动化设计名称
    handleInput(e) {
      this.selectedNode.props.automation.name = e
    },
    // 集成&自动化设计
    designRule() {
      if (this.selectedNode.props.automation.key !== '') {
        const secret = this.selectedNode.props.automation.key
        const name = this.selectedNode.props.automation.name
        this.$openUrl(`/rule-design-ui/#/ruleDesign?id=${secret}&componentId=${this.selectedNode.id}&jvsAppId=${this.infoData.jvsAppId}&name=${name}`, '_blank')
      } else {
        this.selectedNode.props.automation.name = this.selectedNode.props.automation.name === '' ? '未命名逻辑' : this.selectedNode.props.automation.name
        getRuleIdByComponentId({jvsAppId: this.infoData.jvsAppId, componentId: this.selectedNode.id, name: this.selectedNode.props.automation.name, componentType: 'data', designId: this.infoData.dataModelId}).then(res => {
          if (res.data && res.data.code == 0) {
            this.selectedNode.props.automation.key = res.data.data
            this.$emit('saveNodeDesign')
            this.$openUrl(`/rule-design-ui/#/ruleDesign?id=${res.data.data}&componentId=${this.selectedNode.id}&jvsAppId=${this.infoData.jvsAppId}&name=${this.selectedNode.props.automation.name}`, '_blank')
          }
        })
      }
    },
    // 设计审批按钮
    ruleDesign (row, index, list, type) {
      this.currentRow = row
      this.currentIndex = index
      this.currentType = type ? type : ''
      this.netSetVisible = true
    },
    handleCheckChange(e) {
      this.props.userEmpty = e ? this.enumConst.userEmpty.TO_ADMIN : undefined
    },
    timeLimitTypeChange(e) {
      this.props.timeLimit.limit = 0
    },
    handleRadioChange(e) {
      if(this.isSpNode) {
        this.props.mode = e === 'LEADER_TOP' ? this.enumConst.approvalMode.NEXT : this.enumConst.approvalMode.AND
        if(!this.showModel){
          this.props.mode = e === 'LEADER_TOP' ? this.enumConst.approvalMode.NEXT : 'DEFAULT'
        }else{
          this.props.mode = enumConst.approvalMode.AND
        }
        if([enumConst.approvalType.SELF_SELECT].indexOf(e) > -1) {
          this.props.disableDynamicApprover = false
        }
      }
      this.props.targetObj.personnels = []
      this.checkedComs = []
    },
    showRoot() {
      return this.selectedNode.type === enumConst.nodeType.ROOT;
    },
    selected (select) {
       if(this.isRootNode) {
        this.$set(this.props.purviews[this.selectIndex], 'personnels', select.map((s) => {
          return { id: s.id, name: s.name, type: s.type ? s.type : 'user' };
        }))
        this.$store.commit("selectedNode", this.selectedNode);
      }else{
        this.$store.commit(
          "selectedApprover",
          select.map((s) => {
            return { id: s.id, name: s.name, type: s.type ? s.type : 'user' };
          })
        );
      }
    },
    addConditionGroup () {
      if (this.selectedNode.groups.length < 5) {
        this.selectedNode.groups.push({
          connection: enumConst.logicType.OR,
          cids: [],
          condition: [],
        });
        this.$store.commit('selectedNode', this.selectedNode)
      } else {
        // this.$message.warning("最多只允许添加5个条件组");
        this.$notify({
          title: '提示',
          message: '最多只允许添加5个条件组',
          position: 'bottom-right',
          type: 'warning'
        });
      }
      this.$forceUpdate()
    },
    // 选择成员
    chooseUser (type, index, row) {
      this.currentActiveName = type
      this.isSpRoleNode = type === 'role'
      this.isSpJobNode = type === 'job'
      this.isSpDeptNode = type === 'dept'
      setTimeout(() => {
        if (row && row.personnels) {
          this.$refs.processUserSelector.openDialog(row.personnels)
        } else {
          this.$refs.processUserSelector.openDialog()
        }
      }, 0)
      if(this.isRootNode) {
        this.selectIndex = index
        this.select = this.props.purviews[index].personnels
      }else{
        this.select = this.props.targetObj.personnels
      }
    },
    // 确定成员
    submitHandle (list) {
      this.selected(list)
    },
    // 选择角色
    roleChangeHandle () {
      let temp = []
      this.roleOption.map((s) => {
        if(this.roleList.indexOf(s.id) > -1) {
          temp.push({ id: s.id, name: s.name, type: s.type})
        }
      })
      this.$store.commit( "selectedRole", temp)
    },
    // 在线设计
    onlineDesign () {
      // 已有设计表单
      let formType = 'normalForm'
      if(this.selectedNode.type != 'ROOT') {
        formType = 'flowable'
      }
      if(this.nodeForm.formId) {
        getFormInfo(this.infoData.jvsAppId, this.nodeForm.formId).then(res => {
          if(res.data && res.data.code == 0) {
            if(res.data.data && res.data.data.viewJson) {
              let viewJson = JSON.parse(res.data.data.viewJson)
              if(viewJson.formType) {
                formType = viewJson.formType
              }
              this.$openUrl(`/page-design-ui/#/form?id=${this.nodeForm.formId}&dataModelId=${this.infoData.dataModelId}&formType=${formType}&jvsAppId=${this.infoData.jvsAppId}&isFlowNode=true`, '_blank')
            }
          }
        })
      }else{
        // 未设计过表单
        this.$emit('saveNodeDesign', this.selectedNode.id)
      }
    },
    // 取消设计
    cancelDesign () {
      this.$set(this.nodeForm, 'formId', '')
    },
    changePersonType () {
      this.$store.commit("selectedNode", this.selectedNode)
    },
    // 校验审批按钮必须有一个启用
    enableChangeHandle (index) {
      if(!this.selectedNode.props.btn[index].enable) {
        let bool = false
        this.selectedNode.props.btn.filter(it => {
          if(it.enable) {
            bool = true
          }
        })
        if(!bool) {
          // this.$message.warning('请至少启用一个审批按钮！')
          this.$notify({
            title: '提示',
            message: '请至少启用一个审批按钮',
            position: 'bottom-right',
            type: 'warning'
          });
          this.$set(this.selectedNode.props.btn[index], 'enable', true)
        }
      }
    },
    // 选择已有表单
    chooseForm () {
      getPageList(this.infoData.jvsAppId, this.infoData.dataModelId).then(res => {
        if(res.data && res.data.code == 0 && res.data.data) {
          this.placeList = res.data.data
          this.placeVisible = true
        }
      })
    },
    placeClose () {
      this.placeVisible = false
      this.placeList = []
    },
    placeFormHandle (item) {
      this.$set(this.nodeForm, 'formId', item.id)
      this.placeClose()
    },
    chooseScopeUser (type, list) {
      this.currentActiveName = type
      this.isSpRoleNode = false
      this.isSpJobNode = false
      this.isSpDeptNode = false
      setTimeout(() => {
        if(list && list.length > 0) {
          this.$refs.scopeUserSelector.openDialog(list)
        }else {
          this.$refs.scopeUserSelector.openDialog()
        }
      }, 0)
      this.select = []
      if(list && list.length > 0) {
        this.select = list
      }
    },
    submitScopeHandle (select) {
      this.$set(this.props.personnelScope, 'personnelScopes', select)
      this.$store.commit("selectedScopeApprover", select)
      this.$forceUpdate()
    },
    getAllRuleListHandle () {
      getAllRule(this.infoData.jvsAppId, {dataModelId: this.infoData.dataModelId}).then(res => {
        if(res.data && res.data.code == 0) {
          this.allRuleList = res.data.data
        }
      })
    },
    newRuleSet () {
      this.newRuleSetLoading = true
      this.currentRow.automation.name = this.currentRow.name
      getRuleIdByComponentId({jvsAppId: this.infoData.jvsAppId, componentId: this.selectedNode.id + this.currentRow.operation, name: this.currentRow.name, componentType: 'data', designId: this.infoData.dataModelId}).then(res => {
        if(res.data && res.data.code == 0) {
          this.newRuleSetLoading = false
          this.currentRow.automation.key = res.data.data
          if(this.currentType == 'appendApproval') {
            this.$set(this.selectedNode.props.appendApproval.btn, this.currentIndex, this.currentRow)
            this.selectedNode.props.appendApproval.btn[this.currentIndex] = this.currentRow
          }else{
            this.$set(this.selectedNode.props.btn, this.currentIndex, this.currentRow)
            this.selectedNode.props.btn[this.currentIndex] = this.currentRow
          }
          this.$store.commit("selectedNode", this.selectedNode);
          this.$emit('saveNodeDesign', null, 'notdetail')
          this.getAllRuleListHandle()
          this.$openUrl(`/rule-design-ui/#/ruleDesign?id=${res.data.data}&componentId=${this.selectedNode.id + this.currentRow.operation}&jvsAppId=${this.infoData.jvsAppId}&name=${this.currentRow.name}`, '_blank')
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
    viewRule () {
      if(this.currentRow.automation.key) {
        this.$openUrl(`/rule-design-ui/#/ruleDesign?id=${this.currentRow.automation.key}&componentId=${this.selectedNode.id + this.currentRow.operation}&jvsAppId=${this.infoData.jvsAppId}&name=${this.currentRow.name}`, '_blank')
      }
    },
    netSetClose () {
      this.netSetVisible = false
      this.currentRow = null
      this.currentIndex = -1
      this.currentType = ''
    },
    getAllUserComListHandle () {
      this.userComList = []
      this.deptComList = []
      if(this.infoData && this.infoData.jvsAppId) {
        getAllUserComList(this.infoData.jvsAppId, this.infoData.dataModelId).then(res => {
          if(res.data && res.data.code == 0) {
            res.data.data.filter(rit => {
              if(rit.type == 'user') {
                this.userComList.push(rit)
              }
              if(rit.type == 'department' || rit.type == 'dept') {
                this.deptComList.push(rit)
              }
            })
          }
        })
      }
    },
    userComChange () {
      let list = []
      if(this.props.type === 'DEPT_FIELD') {
        for(let i in this.deptComList) {
        if(this.checkedComs.indexOf(this.deptComList[i].prop) > -1) {
          list.push({
            id: this.deptComList[i].prop,
            name: this.deptComList[i].label,
            type: this.deptComList[i].type == 'department' ? 'dept' : this.deptComList[i].type
          })
        }
      }
      }else{
        for(let i in this.userComList) {
          if(this.checkedComs.indexOf(this.userComList[i].prop) > -1) {
            list.push({
              id: this.userComList[i].prop,
              name: this.userComList[i].label,
              type: this.userComList[i].type == 'department' ? 'dept' : this.userComList[i].type
            })
          }
        }
      }
      this.$set(this.props.targetObj, 'personnels',  list)
    },
    leaderComChange () {
      let list = []
      for(let i in this.userComList) {
        if(this.leaderComs.indexOf(this.userComList[i].prop) > -1) {
          list.push({
            id: this.userComList[i].prop,
            name: this.userComList[i].label,
            type: this.userComList[i].type == 'department' ? 'dept' : this.userComList[i].type
          })
        }
      }
      if(!this.props.leader.userFieldConfig) {
        this.$set(this.props.leader, 'userFieldConfig', {personnels: []})
      }
      this.$set(this.props.leader.userFieldConfig, 'personnels',  list)
    },
    roleScopeConditionChange () {
      if(!this.props.targetObj.personnelResolver) {
        this.$set(this.props.targetObj, 'personnelResolver', {
          roleScopeConditions: []
        })
      }
      if(this.roleScopeCondition) {
        this.deptComList.filter(dit => {
          if(dit.prop == this.roleScopeCondition) {
            this.$set(this.props.targetObj.personnelResolver, 'roleScopeConditions',  [{id: this.roleScopeCondition, name: dit.label, type: (dit.type == 'department' ? 'dept' : dit.type)}])
          }
        })
      }else{
        this.$set(this.props.targetObj.personnelResolver, 'roleScopeConditions',  [])
      }
    },
    backSetting (index) {
      this.currentRow = JSON.parse(JSON.stringify(this.selectedNode.props.btn[index]))
      if(!this.currentRow.back) {
        this.$set(this.currentRow, 'back', { scope: 'APPROVED', resubmit: 'SEQUENCE' })
      }
      this.currentIndex = index
      this.backVisible = true
    },
    backClose () {
      this.backVisible = false
      this.currentRow = null
      this.currentIndex = -1
    },
    backSubmit () {
      this.$set(this.selectedNode.props.btn[this.currentIndex], 'back', this.currentRow.back)
      this.backClose()
    },
    connectionChange () {
      if(!this.disabled) {
        this.$set(this.selectedNode, 'connection', enumConst.logicType[this.selectedNode.connection == 'AND' ? 'OR' : 'AND'])
      }
    },
    leaderSourceChange () {
      if(this.props.leader.leaderSource == enumConst.LeaderSourceEnum.FLOW_NODE && !this.props.leader.flowNodeConfig) {
        this.$set(this.props.leader, 'flowNodeConfig', {
          nodeIds: []
        })
      }
      if(this.props.leader.leaderSource == enumConst.LeaderSourceEnum.USER_FIELD && !this.props.leader.userFieldConfig) {
        this.$set(this.props.leader, 'userFieldConfig', {
          personnels: []
        })
      }
    },
    getNodePath (node, path) {
      if(path) {
        node.path = [].concat(path)
      }
      node.path.push({id: node.id, name: node.name, type: node.type})
      if(node.type == 'CONDITION') {
        for(let i in node.conditions) {
          this.getNodePath(node.conditions[i], node.path)
        }
        node.childrenList = []
        this.getNodeAllChildNode(node, node.childrenList)
      }else if(node.type == 'PARALLEL') {
        for(let i in node.parallels) {
          this.getNodePath(node.parallels[i], node.path)
        }
        node.childrenList = []
        this.getNodeAllChildNode(node, node.childrenList)
      }
      if(node.node && node.node.id) {
        if(['CONDITION', 'PARALLEL'].indexOf(node.type) > -1) {
          this.getNodePath(node.node, node.path.concat(node.childrenList))
        }else{
          this.getNodePath(node.node, node.path)
        }
      }
      this.nodePathObj[node.id] = node.path
    },
    getNodeAllChildNode (node, list) {
      node.childrenNodes = []
      if(node.type == 'CONDITION') {
        for(let i in node.conditions) {
          list.push({id: node.conditions[i].id, name: node.conditions[i].name, type: node.conditions[i].type})
          if(node.conditions[i].node && node.conditions[i].node.id) {
            this.getNodeAllChildNode(node.conditions[i].node, list)
          }
        }
      }else if(node.type == 'PARALLEL') {
        for(let i in node.parallels) {
          list.push({id: node.parallels[i].id, name: node.parallels[i].name, type: node.parallels[i].type})
          if(node.parallels[i].node && node.parallels[i].node.id) {
            this.getNodeAllChildNode(node.parallels[i].node, list)
          }
        }
      }else{
        list.push({id: node.id, name: node.name, type: node.type})
        if(node.node && node.node.id) {
          this.getNodeAllChildNode(node.node, list)
        }
      }
    },
    getEnableNodeList (list) {
      return list.filter(li => {
        return ([enumConst.nodeType.SP].indexOf(li.type) > -1 && (li.id != this.node.id))
      })
    }
  },
  created () {
    this.getAllRuleListHandle()
    this.getAllUserComListHandle()
    this.getNodePath(JSON.parse(JSON.stringify(this.nodeDomTree)), [])
  },
  watch: {
    selectedNode: {
      handler (newVal, oldVal) {
        this.roleList = []
        if(this.props.targetObj.personnels && this.props.targetObj.personnels.length > 0) {
          for(let i in this.props.targetObj.personnels) {
            this.roleList.push(this.props.targetObj.personnels[i].id)
          }
        }
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.bottom-button{
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
.node-config{
  height: 100%;
  padding: 0 18px 20px 22px;
  overflow: hidden;
  overflow-y: auto;
  /deep/.el-form{
    .el-form-item__label{
      font-family: Source Han Sans-Bold, Source Han Sans;
      font-weight: 700;
      font-size: 14px;
      color: #363B4C;
      padding-bottom: 8px;
    }
    .el-form-item{
      margin: 0;
      margin-top: 16px;
    }
    .el-input{
      height: 32px;
      .el-input__suffix{
        .el-input__icon{
          height: unset;
        }
      }
    }
    .el-input__inner{
      height: 32px;
      line-height: 32px;
      background: #F5F6F7;
      border: 0;
    }
    .el-input-number{
      .el-input-number__decrease, .el-input-number__increase{
        line-height: 32px!important;
        top: 0;
      }
    }
    .el-select{
      &>:first-child.el-input{
        .el-input__inner{
          height: 32px!important;
        }
      }
    }
    .mutiple-select{
      .el-input{
        height: auto;
      }
    }
  }
  .label-button{
    .label{
      line-height: 30px;
      font-family: Source Han Sans-Regular, Source Han Sans;
      font-weight: 400;
      font-size: 14px;
      color: #6F7588;
    }
  }
  .tag-list-box{
    margin-top: 8px;
    padding: 4px 8px;
    background: #F5F6F7;
    border-radius: 4px;
    /deep/.el-tag{
      margin: 4px;
      background: #fff;
      border-radius: 4px;
      border: 0;
      font-family: Microsoft YaHei-Regular, Microsoft YaHei;
      font-weight: 400;
      font-size: 12px;
      color: #363B4C;
      .el-tag__close{
        font-size: 16px;
        color: #6F7588;
        &:hover{
          background: #fff;
        }
      }
    }
  }
  .text-p{
    margin: 0;
    padding: 0;
    font-family: Source Han Sans-Regular, Source Han Sans;
    font-weight: 400;
    font-size: 14px;
    color: #6F7588;
    line-height: 20px;
  }
  .radio-item-box{
    margin-top: 8px;
    margin-left: 24px;
    background: #F5F6F7;
    width: calc(100% - 24px);
    float: left;
    border-radius: 4px;
    padding: 12px 16px;
    box-sizing: border-box;
    .radio-item-box-item{
      width: 100%;
      margin-top: 8px;
      display: flex;
      align-items: center;
      .label{
        margin-right: 16px;
        font-family: Source Han Sans-Regular, Source Han Sans;
        font-weight: 400;
        font-size: 14px;
        color: #363B4C;
      }
      /deep/.el-input-number{
        width: unset;
        flex: 1;
        .el-input__inner{
          background: #fff;
          padding: 0;
          border-radius: 4px 0 0 4px;
        }
      }
      .label.unit{
        margin-right: 0;
        display: block;
        width: 32px;
        height: 32px;
        line-height: 32px;
        color: #363B4C;
        text-align: center;
        background: #fff;
        border-radius: 0 4px 4px 0;
      }
    }
  }
  .condition-div{
    display: flex;
    align-items: center;
    position: relative;
    .connection{
      height: 100%;
      position: relative;
      display: flex;
      align-items: center;
      .connection-text{
        width: 24px;
        height: 24px;
        line-height: 24px;
        text-align: center;
        background: #DFF3E3;
        border-radius: 4px;
        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
        font-weight: 400;
        font-size: 14px;
        color: #36B452;
        cursor: pointer;
        &.disabled{
          cursor: not-allowed;
        }
      }
      &::before, &::after{
        content: "";
        position: absolute;
        height: calc(50% - 12px);
        left: 50%;
        top: 0;
        border-left: 1px solid #9AD9A8;
      }
      &::after{
        top: calc(50% + 12px);
      }
      &.and{
        .connection-text{
          background: #DBE8FF;
          color: #1E6FFF;
        }
        &::before, &::after{
          border-color: #D2E2FF;
        }
      }
    }
    .condition-div-left{
      width: 24px;
      height: 100%;
      position: absolute;
    }
    .condition-div-right{
      margin-left: 32px;
      flex: 1;
      overflow: hidden;
    }
  }
  .add-condi-group-button{
    margin-top: 16px;
    margin-left: 32px;
    .el-button{
      width: 100%;
      height: 36px;
      background: #E4EDFF;
      font-family: Microsoft YaHei-Regular, Microsoft YaHei;
      font-weight: 400;
      font-size: 14px;
      color: #1E6FFF;
      /deep/.el-icon-plus{
        font-weight: bold;
      }
    }
    .text-p{
      margin-top: 8px;
    }
  }
}

/deep/.select-user-type{
  .el-radio-group{
    .el-radio{
      padding: 6px 0;
      width: 110px;
    }
  }
}

/deep/ .approve-mode{
  .el-radio {
    float: left;
    width: 100%;
    display: block;
    margin-top: 15px;
  }
}

/deep/.leader-source{
  .leader-source-content{
    display: flex;
    align-items: center;
    font-family: Microsoft YaHei-Regular, Microsoft YaHei;
    font-weight: 400;
    font-size: 14px;
    color: #363B4C;
    .el-select{
      width: 120px;
    }
    .el-input-number{
      width: 82px;
      margin: 0 8px;
      .el-input-number__decrease, .el-input-number__increase{
        display: none;
      }
    }
  }
}

/deep/ .approve-end{
  position: relative;
  .el-radio-group{
    width: 160px;
  }
  .el-radio{
    margin-top: 15px;
    width: 100%;
  }
  .el-radio:last-child{
    margin-top: 15px;
    width: 50px;
  }
  .approve-end-leave{
    position: absolute;
    bottom: -6px;
    left: 140px;
    line-height: 32px;
    font-size: 12px;
  }
}

.choose{
  border-radius: 5px;
  margin-top: 2px;
  background: #f4f4f4;
  border: 1px dashed #1890ff !important;
}

.sort-drag-button{
  width: 100%;
  height: 32px;
  box-sizing: border-box;
  font-family: Microsoft YaHei-Regular, Microsoft YaHei;
  font-weight: 400;
  font-size: 14px;
  color: #363B4C;
  padding: 0 8px;
  box-sizing: border-box;
  div{
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
  svg{
    width: 16px;
    height: 16px;
  }
}

.drag-no-choose{
  cursor: move;
  background: #f4f4f4;
  border-radius: 5px;
  position: relative;
  margin-top: 10px;
  padding: 5px 10px;
  height: 20px;
  div:nth-child(1){
    font-size: x-small;
    position: absolute;
    width: 160px;
    left: 10px;
    height: 20px;
    overflow: hidden;
  }
  div:nth-child(2){
    position: absolute;
    right: 10px;
  }
}

/deep/.node-form-design{
  .node-form-design-item{
    margin-top: 16px;
    b{
      display: flex;
      align-items: center;
      font-family: Source Han Sans-Bold, Source Han Sans;
      font-weight: 700;
      font-size: 14px;
      color: #363B4C;
      margin-bottom: 8px;
    }
    .node-form-design-button{
      .el-button:not(.el-button--text){
        width: 128px;
        height: 32px;
        padding: 0;
        margin: 0;
        margin-right: 16px;
        background: #EDF4FF;
        span{
          font-family: Microsoft YaHei-Regular, Microsoft YaHei;
          font-weight: 400;
          font-size: 14px;
          color: #1E6FFF;
        }
      }
    }
    .el-table{
      .el-table__header{
        border-radius: 4px;
        overflow: hidden;
        th{
          background: #F5F6F7;
          border: 0;
          padding: 8px 24px;
          line-height: 20px;
          box-sizing: border-box;
          .cell{
            line-height: 20px;
          }
        }
      }
      .el-table__body{
        td{
          padding: 8px 0 8px 24px;
          .cell{
            line-height: 22px;
          }
        }
        .split-line{
          margin: 0 8px;
          border-left: 1px solid #EEEFF0;
          height: 14px;
        }
      }
    }
    .el-radio-group{
      width: 100px;
      margin-bottom: 6px;
      .el-radio {
        padding: 6px 0;
        width: 90px;
      }
    }
    .el-input{
      .el-input__inner{
        border: 0;
        background: #F5F6F7;
        height: 32px;
        line-height: 32px;
        font-family: Microsoft YaHei, Microsoft YaHei;
        font-weight: 400;
        font-size: 14px;
        color: #494F6A;
      }
    }
  }
}

.place-dialog{
  /deep/.el-dialog .el-dialog__body{
    padding: 0 30px 20px;
  }
  .place-form-desc{
    color: #a2a3a5;
    margin-bottom: 16px;
  }
  .place-form-search{
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

.style-dialog.el-dialog__wrapper{
  /deep/.el-dialog{
    width: 380px;
    height: 320px;
    margin-top: calc(50vh - 160px)!important;
    border-radius: 6px;
    overflow: hidden;
    .el-dialog__header{
      height: 48px;
      background: #F5F6F7;
      border-radius: 6px 6px 0px 0px;
      padding: 0 0 0 24px;
      .el-dialog__title{
        height: 18px;
        font-family: Microsoft YaHei-Bold, Microsoft YaHei;
        font-weight: 700;
        font-size: 14px;
        color: #363B4C;
        line-height: 18px;
      }
      .el-dialog__headerbtn{
        top: 10px;
        right: 17px;
        font-size: 20px;
        .el-dialog__close{
          color: #575E73;;
        }
      }
    }
    .el-dialog__header::before{
      display: none!important;
    }
    .el-dialog__body{
      height: calc(100% - 48px);
      padding: 0!important;
    }
    .style-dialog-box{
      height: calc(100% - 60px);
      padding: 10px 30px;
      box-sizing: border-box;
      overflow: hidden;
      overflow-y: auto;
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
          border-radius: 4px 4px 4px 4px;
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
        }
      }
    }
  }
}

.custom-box{
  .custom-box-item{
    width: 100%;
    margin-bottom: 8px;
    .label{
      font-size: 14px;
      color: #959595;
      line-height: 28px;
      word-break: keep-all;
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
    .content{
      flex: 1;
      display: flex;
      align-items: center;
      overflow: hidden;
    }
  }
  .row{
    width: 100%;
    display: flex;
    align-items: center;
    .content{
      margin-left: 10px;
    }
  }
}
</style>
