package cn.bctools.design.workflow.service.impl;

import cn.bctools.auth.api.dto.PersonnelDto;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.design.workflow.constant.SystemConstant;
import cn.bctools.design.workflow.dto.*;
import cn.bctools.design.workflow.dto.startflow.SelfSelectApprover;
import cn.bctools.design.workflow.dto.startflow.StartFlowReqDto;
import cn.bctools.design.workflow.dto.startflow.StartFlowResDto;
import cn.bctools.design.workflow.dto.startflow.StartFlowVariables;
import cn.bctools.design.workflow.entity.*;
import cn.bctools.design.workflow.entity.dto.*;
import cn.bctools.design.workflow.entity.enums.FlowDesignVersionStatusEnum;
import cn.bctools.design.workflow.entity.enums.FlowTaskNodeApprovalTypeEnum;
import cn.bctools.design.workflow.entity.enums.FlowTaskStatusEnum;
import cn.bctools.design.workflow.entity.enums.ProcessStatusEnum;
import cn.bctools.design.workflow.enums.BackScopeEnum;
import cn.bctools.design.workflow.enums.NodeOperationTypeEnum;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.NodeForm;
import cn.bctools.design.workflow.model.enums.NodePropertiesModeEnum;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import cn.bctools.design.workflow.model.enums.NodeTypeGroupEnum;
import cn.bctools.design.workflow.model.properties.BackProperties;
import cn.bctools.design.workflow.service.*;
import cn.bctools.design.workflow.support.ExecuteTask;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.RuntimeService;
import cn.bctools.design.workflow.support.StartTask;
import cn.bctools.design.workflow.support.function.dto.TransferRuntimeDto;
import cn.bctools.design.workflow.support.function.impl.TransferFunction;
import cn.bctools.design.workflow.support.listener.taskstart.TaskStartListener;
import cn.bctools.design.workflow.support.listener.taskstart.TastStartEventEnum;
import cn.bctools.design.workflow.utils.FlowPathUtil;
import cn.bctools.design.workflow.utils.FlowUtil;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.index.qual.SameLen;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 */
@Slf4j
@Component
@AllArgsConstructor
public class TaskServiceImpl implements TaskService {

    private static final Integer LOCK_EXPIRE_TIME = 60;

    /**
     * 启动流程锁key
     */
    private static final String START_FLOW_LOCK = "start:flow:lock";
    private final RedisUtils redisUtils;
    private final FlowDesignService flowDesignService;
    private final FlowTaskService flowTaskService;
    private final FlowTaskNodeService flowTaskNodeService;
    private final FlowTaskPersonService flowTaskPersonService;
    private final RuntimeService runtimeService;
    private final FlowDynamicDataService flowDynamicDataService;
    private final FlowTaskPathService flowTaskPathService;
    private final FlowTaskProxyService flowTaskProxyService;
    private final TransferFunction transferFunction;
    private final TaskStartListener taskStartListener;

    /**
     * 启动流程锁
     *
     * @param dataId 数据id
     * @return 流程
     */
    private String getStartFlowLockKey(String dataId) {
        return new StringBuilder()
                .append(SystemConstant.SYSTEM_NAME).append(":")
                .append(START_FLOW_LOCK).append(":")
                .append(dataId)
                .toString();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public StartFlowResDto start(UserDto userDto, StartFlowVariables variables) {
        // 1. 准备可发起工作流的设计
        FlowDesign flowDesign = Optional.ofNullable(flowDesignService.getOne(Wrappers.<FlowDesign>lambdaQuery()
                .eq(FlowDesign::getId, variables.getId())
                .eq(FlowDesign::getPublished, Boolean.TRUE))
        ).orElseThrow(() -> new BusinessException("工作流不存在或未发布"));
        FlowDesignVersion flowDesignVersion = flowDesignService.getDesignVersion(flowDesign, FlowDesignVersionStatusEnum.USE);
        String flowDesignModelId = flowDesign.getDataModelId();
        String dataId = variables.getDataId();
        String modelId = variables.getModelId();
        String dataVersion = variables.getDataVersion();

        // 默认使用工作流设计的模型id，若调用方传递了模型id，则使用该指定模型
        String dataModelId = StringUtils.isNotBlank(modelId) ? modelId : flowDesign.getDataModelId();
        flowDesign.setDataModelId(dataModelId);
        // 发起人自选审核人修改配置
        String publishedDesign = flowDesignVersion.getDesignBody();
        String design = FlowUtil.setSelfSelectApprover(true, publishedDesign, flowDesign.getExtend(), variables.getApprovers());

        // 启动流程前执行逻辑
        JSONObject data = Optional.ofNullable(variables.getData()).orElse(new JSONObject());
        taskStartListener.onTaskStartEvent(TastStartEventEnum.BEFORE_START, data, flowDesign.getExtend());

        // 2. 保存业务数据
        if (ObjectNull.isNull(dataId)) {
            DataDto dataDto = flowDynamicDataService.saveModelData(flowDesign.getJvsAppId(), flowDesign.getDataModelId(), data);
            dataId = dataDto.getDataId();
            dataVersion = dataDto.getVersion();
            data = dataDto.getData();
        }

        String lockKey = getStartFlowLockKey(dataId);
        try {
            if (!redisUtils.tryLock(lockKey, LOCK_EXPIRE_TIME)) {
                throw new BusinessException("正在启动流程,请勿重复操作");
            }
            // 检查是否有未结束的任务，若有未结束的任务，则不启动新流程
            boolean startFlow = havePendingTask(dataId);
            if (startFlow) {
                return new StartFlowResDto();
            }
            FlowTask flowTask = flowTaskService.buildSaveFlowTask(flowDesignModelId, flowDesign, design, dataId, variables.getSendFormId());
            flowTask.setCreateTime(LocalDateTime.now());
            flowTask.setCreateBy(userDto.getRealName());
            flowTask.setCreateById(userDto.getId());
            flowTask.setFlowVersionId(flowDesignVersion.getId());
            flowTask.setDesignBody(design);
            // 有工作流设计版本，且设计未变更，不存具体设计；否则存储已变更的设计
            if (ObjectNull.isNotNull(flowTask.getFlowVersionId()) && FlowUtil.checkDesignChange(publishedDesign, design)) {
                flowTask.setFlowDesign(null);
            }

            // 3. 执行流程
            StartTask startTask = new StartTask();
            startTask.setUser(userDto);
            startTask.setFlowTask(flowTask);
            startTask.setFlowExtend(flowDesign.getExtend());
            startTask.setAddNode(variables.getNode());
            startTask.setData(data);
            startTask.setDataVersion(dataVersion);
            RuntimeData runtimeData = runtimeService.start(startTask);
            return new StartFlowResDto().setFlowTaskId(flowTask.getId()).setData(runtimeData.getData()).setFlowTask(flowTask);
        }  catch (Exception e) {
            log.error("启动流程异常", e);
            throw new BusinessException(e.getMessage());
        } finally {
            redisUtils.unLock(lockKey);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public FlowTask execute(FlowReqDto flowDto, UserDto userDto) {
        // 1. 必要的校验
        // 得到任务正在处理的任务节点
        FlowTaskNode currentTaskNode = flowTaskNodeService.getCurrentPendingNode(flowDto.getId(), flowDto.getNodeId());
        if (ObjectUtil.isNull(currentTaskNode)) {
            throw new BusinessException("任务不存在或已结束");
        }
        // 得到工作流任务
        FlowTask flowTask = flowTaskService.getTaskById(flowDto.getId());
        if (Boolean.FALSE.equals(FlowTaskStatusEnum.PENDING.equals(flowTask.getTaskStatus()))) {
            throw new BusinessException("任务已结束");
        }
        // 当前处理的节点id
        String nodeId = currentTaskNode.getNodeId();
        // 当前处理的节点id与应处理的节点id不同，直接返回
        if (Boolean.FALSE.equals(nodeId.equals(flowDto.getNodeId()))) {
            throw new BusinessException("任务不存在或已结束");
        }
        // 获取节点信息
        Node node = Optional.ofNullable(FlowUtil.findNode(flowTask.getDesignBody(), currentTaskNode.getNodeId())).orElseThrow(() -> new BusinessException("获取节点信息失败"));
        // 若是人工节点，校验审核权限
        if (NodeTypeGroupEnum.MANUAL.equals(node.getType().getGroup())) {
            // 校验用户是否有指定任务的待审批的任务
            checkPendingTask(flowDto.getNodeOperationType(), flowDto.getId(), nodeId, userDto.getId());
        }

        // 2. 不需要流转的处理
        FlowExtendDto flowExtend = flowDesignService.getFlowExtend(flowTask.getFlowDesignId());
        ExecuteTask executeTask = new ExecuteTask();
        executeTask.setUser(userDto);
        executeTask.setNodeId(nodeId);
        executeTask.setCurrentNode(node);
        executeTask.setFlowDto(flowDto);
        executeTask.setData(flowDto.getData());
        executeTask.setFlowTaskNode(currentTaskNode);
        executeTask.setFlowExtend(flowExtend);
        executeTask.setAddNode(flowDto.getNode());
        executeTask.setFlowTask(flowTask);

        // 不处理任务流转的操作,直接结束
        if (noCirculation(executeTask)) {
            return flowTask;
        }

        // 修改数据
        if (ObjectNull.isNotNull(flowDto.getData()) && !NodeOperationTypeEnum.REMOVE_SIGNER.equals(flowDto.getNodeOperationType())) {
            DataDto dataDto = flowDynamicDataService.updateModelData(flowTask.getJvsAppId(), flowDto.getData(), flowTask.getDataModelId(), flowTask.getDataId());
            executeTask.setDataVersion(dataDto.getVersion());
            executeTask.setData(dataDto.getData());
        }

        // 3. 流转处理
        // 自选审核人修改配置
        String design = FlowUtil.setSelfSelectApprover(false, flowTask.getDesignBody(), flowExtend, flowDto.getApprovers());
        // 设计已变更，设置新的工作流设计
        if (Boolean.FALSE.equals(FlowUtil.checkDesignChange(flowTask.getDesignBody(), design))) {
            flowTask.setFlowDesign(design)
                    .setDesignBody(design);
        }

        // 执行流程
        runtimeService.execute(executeTask);
        return flowTask;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Future<Boolean> batchExecute(FlowReqDto flowReqDto, UserDto userDto) {
        FlowTask flowTask = Optional.ofNullable(flowTaskService.getById(flowReqDto.getId())
        ).orElseThrow(() -> new BusinessException("任务不存在或已结束"));

        // 因为更新数据时，需要根据设计id得到字段，而相同的字段名可能存在不同的设计，导致找不到当前审批表单对应的字段
        String formId = "";
        NodeForm node = FlowUtil.getNodeForm(flowTask.getDesignBody(), flowReqDto.getNodeId());
        if (node.getSendUserForm()) {
            NodeForm rootNode = FlowUtil.getNodeForm(flowTask.getDesignBody(), NodeTypeEnum.ROOT.getDefaultNodeId());
            formId = rootNode.getFormId();
        } else {
            formId = node.getFormId();
        }
        FlowUtil.clearNodeCache();
        if (ObjectNull.isNotNull(formId)) {
            DynamicDataUtils.setDesignId(formId);
        }
        // 获取数据
        Map<String, Object> data = flowDynamicDataService.querySingle(flowTask.getDataModelId(), flowTask.getDataId());
        flowReqDto.setData(Convert.convert(JSONObject.class, data));
        // 执行流程
        execute(flowReqDto, userDto);
        return new AsyncResult(Boolean.TRUE);
    }

    /**
     * 校验用户是否有指定任务的待审批的任务
     *
     * @param nodeOperationType 审批操作类型
     * @param flowTaskId 任务id
     * @param nodeId     节点id
     * @param userId     用户id
     */
    private void checkPendingTask(NodeOperationTypeEnum nodeOperationType, String flowTaskId, String nodeId, String userId) {
        if (NodeOperationTypeEnum.REMOVE_SIGNER.equals(nodeOperationType)) {
            return;
        }
        if (Boolean.FALSE.equals(flowTaskPersonService.checkPendingTask(flowTaskId, nodeId, userId))) {
            throw new BusinessException("已审核或无权审核");
        }
    }

    /**
     * 不流转任务但要保存数据的处理
     *
     * @param executeTask 任务流转参数
     * @return TRUE-不继续流转， FALSE-继续流转
     */
    private Boolean noCirculation(ExecuteTask executeTask) {
        if (ObjectNull.isNull(executeTask.getFlowDto().getNodeOperationType())) {
            return Boolean.FALSE;
        }
        switch (executeTask.getFlowDto().getNodeOperationType()) {
            case SAVE:
                // 保存数据
                FlowTask flowTask = executeTask.getFlowTask();
                FlowReqDto flowDto = executeTask.getFlowDto();
                DataDto dataDto = flowDynamicDataService.updateModelData(flowTask.getJvsAppId(), flowDto.getData(), flowTask.getDataModelId(), flowTask.getDataId());
                // 修改当前节点的数据版本
                FlowTaskNode flowTaskNode = executeTask.getFlowTaskNode();
                flowTaskNode.getCourse().setDataVersion(dataDto.getVersion());
                flowTaskNodeService.updateById(flowTaskNode);
                return Boolean.TRUE;
            default:
                return Boolean.FALSE;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public StartFlowResDto restartTask(StartFlowReqDto dto, UserDto userDto) {
        // 未结束的任务不可重启
        FlowTask flowTask = flowTaskService.getTaskById(dto.getId());
        if (FlowTaskStatusEnum.PENDING.equals(flowTask.getTaskStatus())) {
            throw new BusinessException("未结束的任务不能重启");
        }
        if (Boolean.FALSE.equals(userDto.getId().equals(flowTask.getCreateById()))) {
            throw new BusinessException("只能重启自己发起的任务");
        }
        FlowDesign flowDesign = Optional.ofNullable(flowDesignService.getById(flowTask.getFlowDesignId())).orElseThrow(() -> new BusinessException("工作流不存在"));
        // 修改任务数据
        String design = FlowUtil.setSelfSelectApprover(false, flowTask.getDesignBody(), flowDesign.getExtend(), dto.getApprovers());
        if (Boolean.FALSE.equals(FlowUtil.checkDesignChange(flowTask.getDesignBody(), design))) {
            flowTask.setFlowDesign(design);
            flowTask.setDesignBody(design);
        }
        flowTask.setTaskStatus(FlowTaskStatusEnum.PENDING);
        flowTaskService.updateById(flowTask);
        // 修改数据
        DataDto dataDto = flowDynamicDataService.updateModelData(flowTask.getJvsAppId(), dto.getData(), flowTask.getDataModelId(), flowTask.getDataId());

        // 保存开始节点任务
        FlowUtil.parseNodeJsonAndCache(flowTask.getDesignBody());
        Node rootNode = FlowUtil.findNode(null, Boolean.TRUE);
        flowTaskNodeService.saveNextNode(rootNode, flowTask);
        // 执行流程
        ExecuteTask executeTask = new ExecuteTask();
        executeTask.setUser(userDto);
        executeTask.setFlowTask(flowTask);
        executeTask.setData(dataDto.getData());
        executeTask.setDataVersion(dataDto.getVersion());
        executeTask.setNodeId(rootNode.getId());
        RuntimeData runtimeData = runtimeService.execute(executeTask);
        return new StartFlowResDto().setFlowTaskId(flowTask.getId()).setData(runtimeData.getData()).setFlowTask(flowTask);
    }

    @Override
    public List<CanBackNodeDto> getCanBackNode(String taskId, String nodeId) {
        // 获取已处理的人工节点
        FlowTask flowTask = flowTaskService.getTaskById(taskId);
        LinkedList<FlowManualNode> manualNodes = flowTask.getFlowManualNodes();
        if (CollectionUtils.isEmpty(manualNodes)) {
            // 没有已处理的人工节点，不处理
            return Collections.emptyList();
        }
        // 得到回退按钮配置的回退范围
        Node currentNode =  FlowUtil.findNode(flowTask.getDesignBody(), nodeId);
        BackProperties backProperties = FlowUtil.getNodeBackProps(currentNode);
        BackScopeEnum backScope = backProperties.getScope();

        // 根据回退范围返回回退节点
        // 从数据库获取路径，若数据库中没有则从设计中获取路径（兼容已启动且未结束的工作流任务）
        List<List<Node>> currentNodePaths = flowTaskPathService.getNodePaths(flowTask, nodeId);

        // 返回发起人节点
        if (BackScopeEnum.ROOT.equals(backScope)) {
            return Collections.singletonList(BeanCopyUtil.copy(currentNodePaths.get(0).get(0), CanBackNodeDto.class));
        }

        // 当前节点的可执行路径，包含已处理的人工节点可回退
        if (BackScopeEnum.APPROVED.equals(backScope)) {
            return currentNodePaths.stream()
                    .flatMap(path ->
                            manualNodes.stream().filter(manualNode -> path.stream().anyMatch(pathNode -> manualNode.getId().equals(pathNode.getId())))
                    )
                    .distinct()
                    .map(node -> BeanCopyUtil.copy(node, CanBackNodeDto.class))
                    .collect(Collectors.toList());
        }

        // 返回当前节点的上一个人工审批节点
        if (BackScopeEnum.PREVIOUS.equals(backScope)) {
            List<Node> nodes = FlowPathUtil.getPreviousManualNodes(currentNodePaths, nodeId);
            return nodes.stream()
                    .filter(node ->
                            manualNodes.stream().anyMatch(manualNode -> manualNode.getId().equals(node.getId()))
                    )
                    .map(node -> BeanCopyUtil.copy(node, CanBackNodeDto.class))
                    .collect(Collectors.toList());

        }

        return Collections.emptyList();
    }

    @Override
    public Boolean havePendingTask(String businessId) {
        return flowTaskService.havePendingTask(businessId);
    }

    @Override
    public FlowTask getPendingTask(String businessId) {
        return flowTaskService.getPendingTask(businessId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void assign(UserDto userDto, String taskId, List<SelfSelectApprover> approvers) {
        // 校验
        List<String> assignUserIds = approvers.stream()
                .flatMap(approver -> approver.getApprovers().stream()).map(PersonnelDto::getId)
                .collect(Collectors.toList());
        if (ObjectNull.isNull(assignUserIds)) {
            throw new BusinessException("未选择审批人");
        }
        // 增加的审批人不能是已生效被代理的用户
        List<String> proxyUsers = flowTaskProxyService.getEffectiveProxyByUserIds(assignUserIds)
                .stream()
                .filter(proxy -> assignUserIds.contains(proxy.getUserId()))
                .map(FlowTaskProxy::getUserName)
                .collect(Collectors.toList());
        if (ObjectNull.isNotNull(proxyUsers)) {
            throw new BusinessException("您选择的用户已将任务转移给其他人请重新选择", String.join(",", proxyUsers));
        }
        // 只能给待审批的任务增加审批人
        FlowTask flowTask = flowTaskService.getTaskById(taskId);
        if (Boolean.FALSE.equals(FlowTaskStatusEnum.PENDING.equals(flowTask.getTaskStatus()))) {
            throw new BusinessException("任务已结束");
        }
        String designBody = flowTask.getDesignBody();

        // 增加审批人
        // Map<节点id, 增加的审批人集合>
        Map<String, List<UserDto>> assignMap = approvers.stream()
                .filter(a -> ObjectNull.isNotNull(a.getApprovers()))
                .collect(Collectors.toMap(SelfSelectApprover::getNodeId, approver ->
                        approver.getApprovers()
                                .stream()
                                .map(p -> new UserDto().setId(p.getId()).setRealName(p.getName()))
                                .collect(Collectors.toList())
                ));
        // Map<节点id, 审批节点信息>
        Map<String, FlowTaskNode> flowTaskNodeMap = flowTaskNodeService.getCurrentNodesByTaskId(taskId).stream().collect(Collectors.toMap(FlowTaskNode::getNodeId, Function.identity()));
        List<FlowTaskPerson> assignTaskPersons = new ArrayList<>();
        List<FlowTaskNode> assignTaskNodes = new ArrayList<>();
        for (Map.Entry<String, List<UserDto>> assign : assignMap.entrySet()) {
            FlowTaskNode taskNode = flowTaskNodeMap.get(assign.getKey());
            if (ObjectNull.isNull(taskNode)) {
                continue;
            }
            // 增加的审批人若已在当前节点的审批人中，则不允许增加
            List<UserDto> assignUser = assign.getValue();
            List<String> existsUserNames = taskNode.getApprovalPersons().stream()
                    .filter(person -> assignUser.stream().anyMatch(user -> user.getId().equals(person.getId())))
                    .map(UserDto::getRealName)
                    .collect(Collectors.toList());
            Node currentNode = FlowUtil.findNode(designBody, taskNode.getNodeId());
            if (ObjectNull.isNotNull(existsUserNames)) {
                throw new BusinessException("已是节点的审批人不能重复分配", String.join(",", existsUserNames), currentNode.getName());
            }
            // 当前审批节点的审批类型是审批，则将新的审批人添加到待办人表，并在节点中保存增加审批人记录
            if (FlowTaskNodeApprovalTypeEnum.APPROVAL.equals(taskNode.getApprovalType())) {
                int i = taskNode.getApprovalPersons().size() + 1;
                if (NodePropertiesModeEnum.NEXT.equals(currentNode.getProps().getMode())) {
                    i = flowTaskPersonService.listPerson(taskId, Collections.singletonList(taskNode.getNodeId())).stream().map(FlowTaskPerson::getNumber).max(Integer::compare).get() + 1;
                }
                for (UserDto user : assignUser) {
                    FlowTaskPerson person = new FlowTaskPerson();
                    person.setNodeId(taskNode.getNodeId());
                    person.setFlowTaskId(taskNode.getFlowTaskId());
                    person.setUserId(user.getId());
                    person.setUserName(user.getRealName());
                    person.setProcessStatus(ProcessStatusEnum.PENDING);
                    person.setTest(Boolean.FALSE);
                    // 依次审批，则设为准备中状态
                    if (NodePropertiesModeEnum.NEXT.equals(currentNode.getProps().getMode())) {
                        person.setProcessStatus(ProcessStatusEnum.PREPARE);
                    }
                    person.setNumber(i++);
                    person.setJvsAppId(taskNode.getJvsAppId());
                    assignTaskPersons.add(person);
                }
            }
            // 当前审批节点的审批类型是加签审批，则修改当前节点审批人即可
            taskNode.getApprovalPersons().addAll(assignUser);
            // 将新增用户的操作记录到审批记录中
            String addApprovalUserName = assignUser.stream().map(UserDto::getRealName).collect(Collectors.joining(","));
            CourseDto courseDto = taskNode.getCourse();
            ApproveResultDto approveResultDto = new ApproveResultDto()
                    .setUserId(userDto.getId())
                    .setUserName(userDto.getRealName())
                    .setNodeOperationTypeEnum(NodeOperationTypeEnum.ADD_SIGNER)
                    .setOpinion(new ApproveOpinionDto().setContent(addApprovalUserName))
                    .setTime(LocalDateTimeUtil.format(LocalDateTime.now(), DatePattern.NORM_DATETIME_PATTERN));
            courseDto.getApproveResultDtos().add(approveResultDto);
            // 修改当前审批节点的审批模式
            NodePropertiesModeEnum currentNodeNewMode = currentNode.getProps().getMode();
            if (NodePropertiesModeEnum.DEFAULT.equals(currentNodeNewMode)) {
                currentNodeNewMode =  NodePropertiesModeEnum.AND;
            }
            courseDto.setMode(currentNodeNewMode);
            // 若是默认审批类型，则改为会签
            if (NodePropertiesModeEnum.DEFAULT.equals(currentNode.getProps().getMode())) {
                courseDto.setMode(NodePropertiesModeEnum.AND);
            }
            assignTaskNodes.add(new FlowTaskNode()
                    .setId(taskNode.getId())
                    .setApprovalPersons(taskNode.getApprovalPersons())
                    .setCourse(courseDto));
        }

        // 增加审批人入库
        flowTaskNodeService.updateBatchById(assignTaskNodes);
        if (ObjectNull.isNotNull(assignTaskPersons)) {
            flowTaskPersonService.saveBatch(assignTaskPersons);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void transfer(UserDto userDto, String taskId, List<FlowTaskManageTransferReqDto> transferList) {
        Map<String, List<TransferDto>> transferMap = transferList.stream()
                .peek(transfer -> transfer.getTransfers().removeIf(t -> ObjectNull.isNull(t.getUserId()) || ObjectNull.isNull(t.getProxyUserId())))
                .filter(transfer -> ObjectNull.isNotNull(transfer.getTransfers()))
                .collect(Collectors.toMap(FlowTaskManageTransferReqDto::getNodeId, FlowTaskManageTransferReqDto::getTransfers));
        if (ObjectNull.isNull(transferMap)) {
            throw new BusinessException("请选择转交对象");
        }
        // 已结束的任务不能操作
        FlowTask flowTask = flowTaskService.getTaskById(taskId);
        if (Boolean.FALSE.equals(FlowTaskStatusEnum.PENDING.equals(flowTask.getTaskStatus()))) {
            throw new BusinessException("任务已结束");
        }
        FlowUtil.parseNodeJsonAndCache(flowTask.getDesignBody());
        List<FlowTaskNode> flowTaskNodes = flowTaskNodeService.getCurrentNodesByTaskId(taskId);
        if (ObjectNull.isNull(flowTaskNodes)) {
            throw new BusinessException("未找到审批节点");
        }
        for (FlowTaskNode taskNode : flowTaskNodes) {
            List<TransferDto> transferUsers = transferMap.get(taskNode.getNodeId());
            if (ObjectNull.isNull(transferUsers)) {
                continue;
            }
            transferUsers.forEach(transferDto -> {
                UserDto user = new UserDto()
                        .setId(transferDto.getUserId())
                        .setRealName(transferDto.getUserName());
                transferDto.setDirections(userDto.getRealName() + "代为转交");
                TransferRuntimeDto transfer = new TransferRuntimeDto(user, flowTask, transferDto, null);
                transferFunction.invoke(FlowUtil.findNode(taskNode.getNodeId()), transfer);
            });
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeSigner(UserDto userDto, String taskId, List<FlowTaskManageSignerRemoveReqDto> removeSigners) {
        boolean hasRemoveUserIds = removeSigners.stream().anyMatch(s -> ObjectNull.isNotNull(s.getUserIds()));
        if (!hasRemoveUserIds) {
            throw new BusinessException("未选择要移除的审批人");
        }
        // 已结束的任务不能操作
        FlowTask flowTask = flowTaskService.getTaskById(taskId);
        if (Boolean.FALSE.equals(FlowTaskStatusEnum.PENDING.equals(flowTask.getTaskStatus()))) {
            throw new BusinessException("任务已结束");
        }
        List<FlowTaskNode> flowTaskNodes = flowTaskNodeService.getCurrentNodesByTaskId(taskId);
        if (ObjectNull.isNull(flowTaskNodes)) {
            throw new BusinessException("未找到审批节点");
        }

        List<String> nodeIds = flowTaskNodes.stream().map(FlowTaskNode::getNodeId).collect(Collectors.toList());
        List<FlowTaskPerson> flowTaskPersonList = flowTaskPersonService.listPerson(flowTask.getId(), nodeIds);

        // 减员
        FlowUtil.parseNodeJsonAndCache(flowTask.getDesignBody());
        Set<String> removeFlowTaskPersonIds = new HashSet<>();
        List<FlowTaskNode> removePersonTaskNodes = new ArrayList<>();
        // Map<节点id, 移除的审批人姓名>
        Map<String, String> removeNodePersonMap = new HashMap<>();
        for (FlowTaskNode flowTaskNode : flowTaskNodes) {
            Optional<FlowTaskManageSignerRemoveReqDto> removeSignerOptional = removeSigners.stream()
                    .filter(t -> flowTaskNode.getNodeId().equals(t.getNodeId()))
                    .filter(t -> ObjectNull.isNotNull(t.getUserIds()))
                    .findFirst();
            if (!removeSignerOptional.isPresent()) {
                continue;
            }
            List<String> removeUserIds = removeSignerOptional.get().getUserIds();
            // 审批人与待移除审批人数量不能相同（至少保留一个审批人）
            if (flowTaskNode.getApprovalPersons().size() == removeUserIds.size()) {
                throw new BusinessException("至少保留一个审批人", FlowUtil.findNode(flowTaskNode.getNodeId()).getName());
            }
            // 只能移除未执行审批的审批人
            List<FlowTaskPerson> notProcessedFlowTaskPersonList = flowTaskPersonList.stream()
                    .filter(person -> person.getNodeId().equals(flowTaskNode.getNodeId()))
                    .filter(person -> !ProcessStatusEnum.PROCESSED.equals(person.getProcessStatus()))
                    .collect(Collectors.toList());
            if (ObjectNull.isNull(notProcessedFlowTaskPersonList)) {
                continue;
            }
            List<FlowTaskPerson> removePersonList = notProcessedFlowTaskPersonList.stream()
                    .filter(person -> removeUserIds.contains(person.getUserId()))
                    .collect(Collectors.toList());
            if (ObjectNull.isNull(removePersonList)) {
                continue;
            }

            // 移除节点表中的审批人
            Set<String> removeApprovalUserIds = removePersonList.stream().map(FlowTaskPerson::getUserId).collect(Collectors.toSet());
            flowTaskNode.getApprovalPersons().removeIf(person -> removeApprovalUserIds.contains(person.getId()));
            removePersonTaskNodes.add(new FlowTaskNode().setId(flowTaskNode.getId()).setApprovalPersons(flowTaskNode.getApprovalPersons()));
            removeFlowTaskPersonIds.addAll(removePersonList.stream().map(FlowTaskPerson::getId).collect(Collectors.toSet()));

            // 移除审批人后，若没有待审批人，则自动流转。否则只执行移除操作
            String removeApprovalUserName = removePersonList.stream().map(FlowTaskPerson::getUserName).collect(Collectors.joining(","));
            if (ObjectNull.isNotNull(removeApprovalUserName)) {
                removeNodePersonMap.put(flowTaskNode.getNodeId(), removeApprovalUserName);
            }
        }

        // 移除审批人
        if (ObjectNull.isNotNull(removeFlowTaskPersonIds)) {
            flowTaskPersonService.removeByIds(removeFlowTaskPersonIds);
        }
        removePersonTaskNodes.forEach(taskNode -> {
            flowTaskNodeService.update(Wrappers.<FlowTaskNode>lambdaUpdate()
                    .set(FlowTaskNode::getApprovalPersons, JSON.toJSONString(taskNode.getApprovalPersons()))
                    .eq(FlowTaskNode::getId, taskNode.getId()));
        });

        removeNodePersonMap.forEach((nodeId, removeUserName) -> {
            FlowReqDto flowReqDto = new FlowReqDto()
                    .setId(taskId)
                    .setNodeId(nodeId)
                    .setNodeOperationType(NodeOperationTypeEnum.REMOVE_SIGNER)
                    .setOpinion(new ApproveOpinionDto().setContent(removeUserName));
            batchExecute(flowReqDto, userDto);
        });
    }
}
