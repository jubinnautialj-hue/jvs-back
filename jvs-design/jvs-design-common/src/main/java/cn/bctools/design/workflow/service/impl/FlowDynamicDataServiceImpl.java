package cn.bctools.design.workflow.service.impl;

import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.entity.DynamicDataPo;
import cn.bctools.design.data.service.DynamicDataService;
import cn.bctools.design.project.dto.SwitchModeDto;
import cn.bctools.design.project.entity.enums.AppVersionTypeEnum;
import cn.bctools.design.project.service.JvsAppVersionService;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.design.workflow.dto.DataDto;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.FlowTaskNode;
import cn.bctools.design.workflow.entity.FlowTaskPerson;
import cn.bctools.design.workflow.entity.dto.FlowManualNode;
import cn.bctools.design.workflow.entity.enums.FlowTaskStatusEnum;
import cn.bctools.design.workflow.entity.enums.ProcessStatusEnum;
import cn.bctools.design.workflow.enums.FlowDataFieldEnum;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.NodeForm;
import cn.bctools.design.workflow.service.*;
import cn.bctools.design.workflow.utils.FlowDataUtil;
import cn.bctools.design.workflow.utils.FlowUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 工作流数据模型交互
 */
@Component
@AllArgsConstructor
public class FlowDynamicDataServiceImpl implements FlowDynamicDataService {

    private final DynamicDataService dynamicDataService;
    private final FlowTaskCarbonCopyService flowTaskCarbonCopyService;
    private final FlowTaskApprovalRecordService flowTaskApprovalRecordService;
    private final FlowTaskPersonService flowTaskPersonService;
    private final FlowTaskNodeService flowTaskNodeService;
    private final FlowTaskService flowTaskService;
    private final JvsAppVersionService appVersionService;

    @Override
    public DataDto saveModelData(String appId, String dataModelId, JSONObject data) {
        String version = dynamicDataService.saveReturnVersion(appId, dataModelId, data);
        return new DataDto().setDataId(data.getString("dataId")).setVersion(version).setData(data);
    }

    @Override
    public DataDto updateModelData(String appId, JSONObject data, String dataModelId, String dataId) {
        JSONObject updateData = FlowDataUtil.excludeData(data);

        Map<String, Object> oldData = dynamicDataService.querySingle(appId, dataModelId, dataId);
        dynamicDataService.clearSensitiveBody(dataModelId, data, oldData);

        String version = dynamicDataService.updateReturnVersion(appId, dataModelId, dataId, updateData);
        return new DataDto().setDataId(data.getString("dataId")).setVersion(version).setData(updateData);
    }

    @Override
    public String onlySave(String modelId, Map<String, Object> data) {
        DynamicDataUtils.clearEcho(data);
        return dynamicDataService.onlySave(modelId, data);
    }

    @Override
    public void onlyUpdate(String modelId, String dataId, Map<String, Object> data) {
        DynamicDataUtils.clearEcho(data);
        Map<String, Object> oldData = dynamicDataService.querySingle(null, modelId, dataId);
        dynamicDataService.clearSensitiveBody(modelId, data, oldData);

        dynamicDataService.onlyUpdate(modelId, dataId, data);
    }

    @Override
    public void updateBatchById(String modelId, List<Map<String, Object>> dataList) {
        dataList.forEach(DynamicDataUtils::clearEcho);
        dynamicDataService.updateBatchById(modelId, dataList);
    }

    @Override
    public Map<String, Object> querySingle(String dataModelId, String dataId) {
        return Optional.ofNullable(dynamicDataService.getById(dataModelId, dataId)).map(DynamicDataPo::getJsonData).orElseGet(HashMap::new);
    }

    @Override
    public FlowTaskModelData getFlowTaskData(String dataId) {
        if (ObjectNull.isNull(dataId)) {
            return new FlowTaskModelData();
        }
        JSONObject data = getFlowTaskDataObj(dataId);
        if (ObjectNull.isNull(data)) {
            return new FlowTaskModelData();
        }
        Object task = Optional.ofNullable(data.get(FlowDataFieldEnum.TASK.getFieldKey())).orElseGet(Object::new);
        return BeanCopyUtil.copy(task, FlowTaskModelData.class);
    }

    @Override
    public JSONObject getFlowTaskDataObj(String dataId) {
        return getMltipleFlowTaskData(Collections.singletonList(dataId)).get(dataId);
    }

    @Override
    public Map<String, JSONObject> getMltipleFlowTaskData(List<String> dataIds) {
        if (ObjectNull.isNull(dataIds)) {
            return Collections.emptyMap();
        }
        List<FlowTask> flowTasks = flowTaskService.listByDataIds(dataIds);
        List<String> taskIds = flowTasks.stream().map(FlowTask::getId).collect(Collectors.toList());
        if (ObjectNull.isNull(taskIds)) {
            return Collections.emptyMap();
        }
        // 填充工作流任务使用的设计
        flowTaskService.fillBatchTaskDesignBody(flowTasks);
        Map<String, List<FlowTask>> dataTaskMap = flowTasks.stream().collect(Collectors.groupingBy(FlowTask::getDataId));

        // 查询待审批任务信息
        List<String> pendingTaskIds = flowTasks.stream()
                .filter(task -> FlowTaskStatusEnum.PENDING.equals(task.getTaskStatus()))
                .map(FlowTask::getId).collect(Collectors.toList());
        Map<String, List<FlowTaskNode>> pendingTaskNodeMap = ObjectNull.isNull(pendingTaskIds) ? Collections.emptyMap() :
                flowTaskNodeService.getCurrentNodeByTaskIds(pendingTaskIds).stream().collect(Collectors.groupingBy(FlowTaskNode::getFlowTaskId));
        Map<String, List<FlowTaskPerson>> pendingTaskPersonMap = ObjectNull.isNull(pendingTaskIds) ? Collections.emptyMap() :
                flowTaskPersonService.listPendingByTaskIds(pendingTaskIds).stream().collect(Collectors.groupingBy(FlowTaskPerson::getFlowTaskId));
        // 查询任务历史审批人
        Map<String, Set<String>> historyPersonMap = flowTaskApprovalRecordService.queryUserIdsByTaskId(taskIds);
        // 查询任务抄送人记录
        Map<String, Set<String>> carbonCopieMap = flowTaskCarbonCopyService.queryUserIdsByTaskId(taskIds);

        // 转换数据结构
        return dataIds.stream()
                .collect(Collectors.toMap(dataId -> dataId, dataId -> {

                    // 转换数据id触发的流程任务信息
                    List<FlowTask> flowTaskList = Optional.ofNullable(dataTaskMap.get(dataId))
                            .orElseGet(ArrayList::new).stream()
                            // 按创建时间顺序排序
                            .sorted(Comparator.comparing(FlowTask::getCreateTime))
                            .collect(Collectors.toList());
                    List<FlowTaskModelData> flowTaskModelDataList = flowTaskList.stream()
                            .map(flowTask -> {
                                List<FlowTaskNode> flowTaskNodes = pendingTaskNodeMap.get(flowTask.getId());
                                List<FlowTaskPerson> flowTaskPersons = pendingTaskPersonMap.get(flowTask.getId());
                                Set<String> historyPersons = historyPersonMap.get(flowTask.getId());
                                Set<String> carbonCopies = carbonCopieMap.get(flowTask.getId());
                                return convertTaskData(flowTask, flowTaskNodes, flowTaskPersons, historyPersons, carbonCopies);
                            }).collect(Collectors.toList());
                    // 设置数据触发的工作流任务信息
                    return setDynamicDataTask(flowTaskModelDataList);
                }));
    }


    /**
     * 转换流程任务数据结构
     *
     * @param flowTask           流程任务
     * @param flowTaskNodes      未结束的流程任务节点集合
     * @param flowTaskPersonList 未结束的流程任务审批人集合
     * @param historyPersons     流程任务历史审批人集合
     * @param carbonCopies       流程任务抄送人集合
     * @return 转换后的流程信息
     */
    private FlowTaskModelData convertTaskData(FlowTask flowTask, List<FlowTaskNode> flowTaskNodes, List<FlowTaskPerson> flowTaskPersonList,
                                              Set<String> historyPersons, Set<String> carbonCopies) {
        String flowTaskId = flowTask.getId();
        // 设置待处理工作流任务的相关数据
        String progress = flowTask.getTaskStatus().getDesc();
        List<String> personIds = new ArrayList<>();
        List<FlowTaskNodeModelData> taskNodes = null;
        if (FlowTaskStatusEnum.PENDING.equals(flowTask.getTaskStatus())) {
            // 设置数据模型中的工作流审批人集合
            if (ObjectNull.isNotNull(flowTaskNodes)) {
                flowTaskPersonList = flowTaskPersonService.list(Wrappers.<FlowTaskPerson>lambdaQuery()
                        .eq(FlowTaskPerson::getFlowTaskId, flowTaskId)
                        .eq(FlowTaskPerson::getProcessStatus, ProcessStatusEnum.PENDING));
                // 设置数据模型中的工作流进度
                progress = flowTaskNodes.size() == 1 ? FlowUtil.findNode(flowTask.getDesignBody(), flowTaskNodes.get(0).getNodeId()).getName() + "处理中" : "处理中";
            }
            if (ObjectNull.isNotNull(flowTaskPersonList)) {
                personIds = flowTaskPersonList.stream().map(FlowTaskPerson::getUserId).distinct().collect(Collectors.toList());
            }
            // 设置工作流当前节点表单id
            // 待办节点：返回当前节点表单id（发起人表单id or 自定义表单id）
            flowTaskPersonList = Optional.ofNullable(flowTaskPersonList).orElseGet(ArrayList::new);
            taskNodes = taskNodes(flowTask, flowTaskNodes, flowTaskPersonList);
        }
        FlowTaskModelData flowTaskModelData = new FlowTaskModelData();
        flowTaskModelData.setId(flowTask.getId());
        flowTaskModelData.setTaskStatus(flowTask.getTaskStatus().getValue());
        flowTaskModelData.setTaskStatusDesc(flowTask.getTaskStatus().getDesc());
        flowTaskModelData.setProgress(progress);
        flowTaskModelData.setSendFormId(flowTask.getFormId());
        flowTaskModelData.setCreateById(flowTask.getCreateById());
        flowTaskModelData.setFlowTaskPersons(personIds);
        flowTaskModelData.setTaskNodes(taskNodes);
        flowTaskModelData.setFlowDesignId(flowTask.getFlowDesignId());
        flowTaskModelData.setFlowTaskName(flowTask.getName());
        flowTaskModelData.setFlowManualNodes(flowTask.getFlowManualNodes());
        flowTaskModelData.setHistoryPersons(historyPersons);
        flowTaskModelData.setCarbonCopyPersons(carbonCopies);
        return flowTaskModelData;
    }

    /**
     * 设置数据触发的工作流任务信息
     *
     * @param flowTaskModelDataList 工作流任务信息集合
     */
    private JSONObject setDynamicDataTask(List<FlowTaskModelData> flowTaskModelDataList) {
        JSONObject data = new JSONObject();
        if (ObjectNull.isNull(flowTaskModelDataList)) {
            return data;
        }
        // 最后一个流程任务信息
        FlowTaskModelData lastFlowTaskModeData = flowTaskModelDataList.get(flowTaskModelDataList.size() - 1);
        // 数据模型中的工作流相关数据
        data.put(FlowDataFieldEnum.TASK_STATE.getFieldKey(), lastFlowTaskModeData.getTaskStatusDesc());
        data.put(FlowDataFieldEnum.TASK_PROGRESS.getFieldKey(), lastFlowTaskModeData.getProgress());
        data.put(FlowDataFieldEnum.TASK.getFieldKey(), lastFlowTaskModeData);
        // 审批历史记录
        data.put(FlowDataFieldEnum.TASK_HISTORY.getFieldKey(), flowTaskModelDataList);
        return data;
    }


    /**
     * 待办节点信息
     *
     * @param flowTask           流程任务
     * @param flowTaskNodes      任务待办节点集合
     * @param flowTaskPersonList 待办人员集合
     * @return 待办节点信息
     */
    private List<FlowTaskNodeModelData> taskNodes(FlowTask flowTask, List<FlowTaskNode> flowTaskNodes, List<FlowTaskPerson> flowTaskPersonList) {
        if (ObjectNull.isNull(flowTaskNodes)) {
            return Collections.emptyList();
        }
        List<FlowTaskNodeModelData> taskNodes = new ArrayList<>();
        // 设置工作流当前节点表单id
        // 待办节点：返回当前节点表单id（发起人表单id or 自定义表单id）
        flowTaskNodes.forEach(taskNode -> {
            Node node = FlowUtil.findNode(flowTask.getDesignBody(), taskNode.getNodeId());
            NodeForm nodeForm = FlowUtil.getNodeForm(node);
            String formId = Boolean.TRUE.equals(nodeForm.getSendUserForm()) ? flowTask.getFormId() : nodeForm.getFormId();
            FlowTaskNodeModelData taskNodeData = new FlowTaskNodeModelData();
            taskNodeData.setNodeId(taskNode.getNodeId());
            taskNodeData.setNodeName(node.getName());
            taskNodeData.setFormId(formId);
            taskNodeData.setUserIds(flowTaskPersonList.stream().filter(person -> taskNodeData.getNodeId().equals(person.getNodeId())).map(FlowTaskPerson::getUserId).collect(Collectors.toList()));
            taskNodes.add(taskNodeData);
            FlowUtil.clearNodeCache();
        });
        return taskNodes;
    }


    @Override
    public void saveTaskToModel(List<FlowTask> tasks) {
        List<String> taskIds = tasks.stream().map(FlowTask::getId).distinct().collect(Collectors.toList());
        List<FlowTask> flowTasks = flowTaskService.listByIds(taskIds);
        if (ObjectNull.isNull(flowTasks)) {
            return;
        }
        // 填充工作流任务使用的设计
        flowTaskService.fillBatchTaskDesignBody(flowTasks);
        // 待审批人Map<任务id, 待审批人id集合>
        Map<String, Set<String>> pendingTaskPersonMap = flowTaskPersonService.listPendingByTaskIds(taskIds)
                .stream().collect(Collectors.groupingBy(FlowTaskPerson::getFlowTaskId, Collectors.mapping(FlowTaskPerson::getUserId, Collectors.toSet())));
        // 查询流程任务所属数据启用的所有任务id Map<数据id, 任务id集合>
        List<String> dataIds = tasks.stream().map(FlowTask::getDataId).distinct().collect(Collectors.toList());
        Map<String, Set<String>> dataAllTaskIdMap = flowTaskService.list(Wrappers.<FlowTask>lambdaQuery()
                        .in(FlowTask::getDataId, dataIds)
                        .select(FlowTask::getId, FlowTask::getDataId))
                .stream()
                .collect(Collectors.groupingBy(FlowTask::getDataId, Collectors.mapping(FlowTask::getId, Collectors.toSet())));
        List<String> allTaskIds = dataAllTaskIdMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        // 查询任务抄送人记录Map<任务id, 抄送人id集合>
        Map<String, Set<String>> carbonCopieMap = flowTaskCarbonCopyService.queryUserIdsByTaskId(allTaskIds);
        // 查询任务历史审批人记录Map<任务id, 历史审批人id集合>
        Map<String, Set<String>> arMap = flowTaskApprovalRecordService.queryUserIdsByTaskId(allTaskIds);

        // 按应用所属模式分组处理
        Set<String> appIds = flowTasks.stream().map(FlowTask::getJvsAppId).collect(Collectors.toSet());
        Map<AppVersionTypeEnum, Set<String>> groupAppIdMap = appVersionService.groupAppIdByVersionType(appIds);
        groupAppIdMap.forEach((key, value) -> {
            List<FlowTask> flowTaskList = flowTasks.stream().filter(task -> value.contains(task.getJvsAppId())).collect(Collectors.toList());
            List<Map<String, Object>> updateList = new ArrayList<>();
            for (FlowTask flowTask : flowTaskList) {
                String taskId = flowTask.getId();
                FlowTaskModelData flowTaskModelData = new FlowTaskModelData();
                flowTaskModelData.setCreateById(flowTask.getCreateById());
                flowTaskModelData.setFlowTaskPersons(new ArrayList<>(Optional.ofNullable(pendingTaskPersonMap.get(taskId)).orElseGet(HashSet::new)));
                String dataId = flowTask.getDataId();
                Set<String> dataTaskIds = dataAllTaskIdMap.get(dataId);
                Set<String> carbonCopyPersons = carbonCopieMap.entrySet().stream()
                        .filter(e -> dataTaskIds.contains(e.getKey()))
                        .flatMap(e -> e.getValue().stream())
                        .collect(Collectors.toSet());
                flowTaskModelData.setCarbonCopyPersons(carbonCopyPersons);
                Set<String> historyPersons = arMap.entrySet().stream()
                        .filter(e -> dataTaskIds.contains(e.getKey()))
                        .flatMap(e -> e.getValue().stream())
                        .collect(Collectors.toSet());
                historyPersons.add(flowTask.getCreateById());
                flowTaskModelData.setHistoryPersons(historyPersons);

                JSONObject updateData = new JSONObject();
                updateData.put("dataId", flowTask.getDataId());
                updateData.put("modelId", flowTask.getDataModelId());
                updateData.put(FlowDataFieldEnum.TASK.getFieldKey(), flowTaskModelData);
                // 设置数据模型中的工作流进度
                setTaskOther(updateData, flowTask);
                updateList.add(updateData);
            }
            ModeUtils.setSwitchModel(new SwitchModeDto().setMode(key));
            Map<String, List<Map<String, Object>>> modelBatchUpdateData = updateList.stream()
                    .collect(Collectors.groupingBy(data -> String.valueOf(data.get("modelId"))));
            modelBatchUpdateData.forEach(this::updateBatchById);
        });
    }


    /**
     * 设置数据模型中的工作流进度
     *
     * @param updateData 待修改的数据
     * @param flowTask   流程任务
     */
    private void setTaskOther(JSONObject updateData, FlowTask flowTask) {
        String progress = flowTask.getTaskStatus().getDesc();
        if (FlowTaskStatusEnum.PENDING.equals(flowTask.getTaskStatus())) {
            List<FlowTaskNode> pendingTaskNodes = flowTaskNodeService.getCurrentNodesByTaskId(flowTask.getId());
            progress = pendingTaskNodes.size() == 1 ? FlowUtil.findNode(flowTask.getDesignBody(), pendingTaskNodes.get(0).getNodeId()).getName() + "处理中" : "处理中";
        }
        updateData.put(FlowDataFieldEnum.TASK_PROGRESS.getFieldKey(), progress);
        updateData.put(FlowDataFieldEnum.TASK_STATE.getFieldKey(), flowTask.getTaskStatus().getDesc());
    }

    @Override
    public Map<String, Object> paresMapWithEcho(String appId, JSONObject data, String modelId) {
        return dynamicDataService.paresMapWithEcho(appId, data, modelId, null, false);
    }

    /**
     * 工作流模型相关数据
     */
    @Getter
    @Setter
    public static class FlowTaskModelData {
        // 工作流任务id
        private String id;
        // 工作流审批状态
        private Integer taskStatus;
        // 工作流审批状态
        private String taskStatusDesc;
        // 数据模型中的工作流进度
        private String progress;
        // 发起工作流的表单id
        private String sendFormId;
        // 工作流任务发起人id
        private String createById;
        // 工作流审批人集合
        private List<String> flowTaskPersons;
        // 待办节点集合
        private List<FlowTaskNodeModelData> taskNodes;
        // 工作流设计id
        private String flowDesignId;
        // 工作流任务名称
        private String flowTaskName;
        // 流转的人工节点
        private LinkedList<FlowManualNode> flowManualNodes;
        // 抄送人集合
        private Set<String> carbonCopyPersons;
        // 历史审批人集合
        private Set<String> historyPersons;
    }

    /**
     * 待办节点信息
     */
    @Getter
    @Setter
    public static class FlowTaskNodeModelData {
        // 待办节点id
        private String nodeId;
        // 待办节点名
        private String nodeName;
        // 待办节点表单id
        private String formId;
        // 当前节点审批人
        private List<String> userIds;
    }

}
