import request from "@/router/axios";

// 工作流列表
export function getModelList(params) {
  return request({
    url: `/mgr/jvs-design/workflow/design/page`,
    method: "get",
    params: params,
  });
}

// 流程分类列表
export function flowableType() {
  return request({
    url: `/mgr/jvs-design/base/workflow/design/groups`,
    method: "get",
  });
}

// 创建工作流
export function createModel(data) {
  return request({
    url: `/mgr/jvs-design/app/design/${data.jvsAppId}/workflow`,
    method: "post",
    data: data,
  });
}

// 快捷创建工作流
export function quickCreateModel(data, headers) {
  let temp = {
    url: `/mgr/jvs-design/app/design/${data.jvsAppId}/workflow/quick-create`,
    method: "post",
    data: data,
  };
  if (headers) {
    temp.headers = headers;
  }
  return request(temp);
}

// 工作流列表页配置
export const createWorkflowCurd = (jvsAppId, id) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/workflow/create/page/${id}`,
    method: "post",
  });
};

// 工作流详情
export function getModelDetail(jvsAppId, id) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/workflow/${id}`,
    method: "get",
  });
}

// 修改基本信息
export function editModel(data) {
  return request({
    url: `/mgr/jvs-design/app/design/${data.jvsAppId}/workflow`,
    method: "put",
    data: data,
  });
}

// 保存设计
export function designModel(data) {
  return request({
    url: `/mgr/jvs-design/app/design/${data.jvsAppId}/workflow/save`,
    method: "put",
    data: data,
  });
}

// 删除工作流
export function deleteModel(jvsAppId, id) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/workflow/${id}`,
    method: "delete",
  });
}

// 发布流程
export function deployProcess(jvsAppId, id) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/workflow/publish/${id}`,
    method: "put",
  });
}

// 保存并发布流程
export function saveDeployProcess(data) {
  return request({
    url: `/mgr/jvs-design/app/design/${data.jvsAppId}/workflow/saveAndPublish`,
    method: "put",
    data: data,
  });
}

// 停用流程
export function suspendProcess(jvsAppId, id) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/workflow/stop/${id}`,
    method: "put",
  });
}

// 查询任务列表
export function queryTaskList(query) {
  return request({
    url: `/mgr/jvs-design/base/workflow/task/pendingApproves`,
    method: "get",
    params: query,
  });
}

// 我的申请列表
export function applyList(query) {
  return request({
    url: `/mgr/jvs-design/base/workflow/task/selfs`,
    method: "get",
    params: query,
  });
}

// 已处理任务列表
export function taskRecordList(query) {
  return request({
    url: `/mgr/jvs-design/base/workflow/task/self_approve_log`,
    method: "get",
    params: query,
  });
}

// 入口列表
export function entryList(params) {
  return request({
    url: `/mgr/jvs-design/base/workflow/design/havePermissionDesign`,
    method: "get",
    params: params,
  });
}

// 我的申请---查询流程数据，表单回显
export function queryEchoForm(jvsAppId, modelId, dataId, designId) {
  return request({
    url: `/mgr/jvs-design/app/use/${jvsAppId}/dynamic/data/query/single/${modelId}/${dataId}`,
    method: "get",
    headers: {
      designId: designId,
    },
  });
}

// 工作台进入的抄送、审核记录，表单回显
export function queryEchoFormFromWorkPlace(taskId, modelId, dataId, designId) {
  return request({
    url: `/mgr/jvs-design/base/workflow/data/query/single/${taskId}/${modelId}/${dataId}`,
    method: "get",
    headers: {
      designId: designId,
    },
  });
}

// 查询任务进度
export function queryDetailHistoryInfo(id, params) {
  return request({
    url: `/mgr/jvs-design/base/workflow/task/progress/${id}`,
    method: "get",
    params: params,
  });
}

// 我的申请--取消  /  终止  流程
export function candelProcess(id, data) {
  return request({
    url: `/mgr/jvs-design/base/workflow/task/stop/${id}`,
    method: "put",
    data: data,
  });
}

// 节点表单数据变更记录
export function queryChangeInfo(jvsAppId, dataModelId, dataId, version) {
  return request({
    url: `/mgr/jvs-design/app/use/${jvsAppId}/data/log/query/single/${dataModelId}/${dataId}/${version}`,
    method: "get",
  });
}

// 领取任务
export function claimTask(taskId) {
  return request({
    url: `/mgr/jvs-design/task/claim/${taskId}`,
    method: "post",
  });
}

// 分配给其他人
export function assignTask(taskId, userId) {
  return request({
    url: `/mgr/jvs-design/task/assign/${taskId}/to/${userId}`,
    method: "post",
  });
}

// 完成任务
export const completeProcess = (taskId, data) => {
  return request({
    url: `/mgr/jvs-design/task/complete/${taskId}`,
    method: "post",
    data: data,
  });
};

// 保存任务
export const saveProcess = (taskId, data) => {
  return request({
    url: `/mgr/jvs-design/task/save/${taskId}`,
    method: "post",
    data: data,
  });
};

// 指派任务
export const zhipaiProcess = (taskId, userId) => {
  return request({
    url: `/mgr/jvs-design/task/assign/${taskId}/to/${userId}`,
    method: "post",
  });
};

// 委派
export const weipaiProcess = (taskId, userId) => {
  return request({
    url: `/mgr/jvs-design/task/delegate/${taskId}/to/${userId}`,
    method: "post",
  });
};

// 抄送列表
export function queryCCList(params) {
  return request({
    url: `/mgr/jvs-design/base/workflow/task/carbonCopys`,
    method: "get",
    params: params,
  });
}

// 测试准备
export const testPrepare = (data) => {
  return request({
    url: `/mgr/jvs-design/base/workflow/task/test/prepare`,
    method: "post",
    data: data,
  });
};

// 启动测试
export const testStart = (data) => {
  return request({
    url: `/mgr/jvs-design/base/workflow/task/test/start`,
    method: "post",
    data: data,
  });
};

// 测试执行
export const testExecute = (data) => {
  return request({
    url: `/mgr/jvs-design/base/workflow/task/test/execute`,
    method: "post",
    data: data,
  });
};

// 重新发起测试
export const reTestStart = (data) => {
  return request({
    url: `/mgr/jvs-design/base/workflow/task/test/restart`,
    method: "post",
    data: data,
  });
};

// 代理列表
export function queryProxyList(params) {
  return request({
    url: `/mgr/jvs-design/base/workflow/proxy/page`,
    method: "get",
    params: params,
  });
}

// 新增工作流代理
export const addProxy = (data) => {
  return request({
    url: `/mgr/jvs-design/base/workflow/proxy`,
    method: "post",
    data: data,
  });
};

// 撤销代理
export const delProxy = (id) => {
  return request({
    url: `/mgr/jvs-design/base/workflow/proxy/revoke/${id}`,
    method: "put",
  });
};

// 批量审批
export const batchExecute = (data) => {
  return request({
    url: `/mgr/jvs-design/base/workflow/task/batch/execute`,
    method: "post",
    data: data,
  });
};

// 催办
export const urgeTask = (taskId) => {
  return request({
    url: `/mgr/jvs-design/base/workflow/task/urge/${taskId}`,
    method: "put",
  });
};

// 获取可回退的节点
export function queryFlowManualNodes(taskId, nodeId) {
  return request({
    url: `/mgr/jvs-design/base/workflow/task/can/back/node/${taskId}/${nodeId} `,
    method: "get",
  });
}

// 获取可以引用的表单
export const getPageList = (jvsAppId, dataModelId) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/form/model/${dataModelId}/all`,
    method: "get",
  });
};

// 新建逻辑设计
export const getRuleIdByComponentId = (data, headers) => {
  let temp = {
    url: `/mgr/jvs-design/app/design/${data.jvsAppId}/rule`,
    method: "put",
    params: data,
  };
  if (headers) {
    temp.headers = headers;
  }
  return request(temp);
};

// 查询单条数据的所有任务
export const getAllTaskList = (jvsAppId, dataModelId, dataId) => {
  return request({
    url: `/mgr/jvs-design/app/use/${jvsAppId}/dynamic/data/query/single/flow/task/ids/${dataModelId}/${dataId}`,
    method: "get",
  });
};

// 批量查询进度
export const getAllTaskListInfo = (data) => {
  return request({
    url: `/mgr/jvs-design/base/workflow/task/progress/batch`,
    method: "post",
    data: data,
  });
};

// 获取模型的人员选择组件
export const getAllUserComList = (jvsAppId, modelId) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/data/field/user/list?modelId=${modelId}`,
    method: "get",
  });
};

// 流程标题可以选择的字段
export const getFlowNameFieldList = (jvsAppId, id) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/workflow/custom/title/fields/${id}`,
    method: "get",
  });
};

// 工作流任务管理--分页
export const getTaskManage = (params) => {
  return request({
    url: `/mgr/jvs-design/base/workflow/task/manage/page`,
    method: "get",
    params: params,
  });
};

// 工作流任务管理--增员
export const assignManage = (data) => {
  return request({
    url: `/mgr/jvs-design/base/workflow/task/manage/assign`,
    method: "post",
    data: data,
  });
};

// 工作流任务管理--减员
export const reduceManage = (id, data) => {
  return request({
    url: `/mgr/jvs-design/base/workflow/task/manage/signer/remove/${id}`,
    method: "post",
    data: data,
  });
};

// 工作流任务管理--终止
export const stopManage = (id, data) => {
  return request({
    url: `/mgr/jvs-design/base/workflow/task/manage/stop/${id}`,
    method: "post",
    data: data,
  });
};

// 工作流任务管理--批量终止
export const batchStopManage = (data) => {
  return request({
    url: `/mgr/jvs-design/base/workflow/task/manage/stop/batch`,
    method: "post",
    data: data,
  });
};

// 工作流任务管理--转交
export const transManage = (id, data) => {
  return request({
    url: `/mgr/jvs-design/base/workflow/task/manage/transfer/${id}`,
    method: "post",
    data: data,
  });
};

// 复制条件分支节点
export const copyBranchNode = (jvsAppId, data) => {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/workflow/node/copy`,
    method: "put",
    data: data,
  });
};

// 获取应用名称列表
export const getJvsAppList = (mode) => {
  return request({
    url: `/mgr/jvs-design/base/use/menu`,
    method: "get",
    params: { mode },
  });
};
