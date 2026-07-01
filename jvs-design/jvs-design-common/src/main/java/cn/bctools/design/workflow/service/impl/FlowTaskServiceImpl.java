package cn.bctools.design.workflow.service.impl;

import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.enums.SysNoticeConfig;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.design.notice.handler.enums.TriggerTypeEnum;
import cn.bctools.design.notice.service.DataNoticeService;
import cn.bctools.design.project.service.JvsAppVersionService;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.design.workflow.constant.SystemConstant;
import cn.bctools.design.workflow.dto.FlowReqDto;
import cn.bctools.design.workflow.dto.PendingApprovesReqDto;
import cn.bctools.design.workflow.dto.PendingApprovesResDto;
import cn.bctools.design.workflow.dto.progress.*;
import cn.bctools.design.workflow.entity.*;
import cn.bctools.design.workflow.entity.dto.*;
import cn.bctools.design.workflow.entity.enums.FlowTaskNodeApprovalTypeEnum;
import cn.bctools.design.workflow.entity.enums.FlowTaskStatusEnum;
import cn.bctools.design.workflow.entity.enums.ProcessStatusEnum;
import cn.bctools.design.workflow.mapper.FlowDesignMapper;
import cn.bctools.design.workflow.mapper.FlowTaskMapper;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.NodeForm;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import cn.bctools.design.workflow.model.properties.FlowButton;
import cn.bctools.design.workflow.service.*;
import cn.bctools.design.workflow.support.listener.notify.FlowNotifyEvent;
import cn.bctools.design.workflow.utils.FlowUtil;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.template.OssTemplate;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zhuxiaokang
 * 工作流任务 服务实现类
 */
@Service
@AllArgsConstructor
public class FlowTaskServiceImpl extends ServiceImpl<FlowTaskMapper, FlowTask> implements FlowTaskService {

    private final FlowTaskNodeService flowTaskNodeService;
    private final FlowTaskPersonService flowTaskPersonService;
    private final FlowTaskParallelService flowTaskParallelService;
    private final FlowTaskPathService flowTaskPathService;
    private final FlowTaskApprovalRecordService flowTaskApprovalRecordService;
    private final FlowTaskCarbonCopyService flowTaskCarbonCopyService;
    private final AuthUserServiceApi authUserServiceApi;
    private final FlowDesignMapper flowDesignMapper;
    private final DataNoticeService dataNoticeService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RedisUtils redisUtils;
    private final FlowDesignVersionService flowDesignVersionService;
    private final JvsAppVersionService appVersionService;
    private final OssTemplate ossTemplate;

    /**
     * 构造保存工作流任务数据
     *
     * @param flowDesignModelId 工作流设计原本的模型id
     * @param flowDesign        设计信息
     * @param design            要保存的工作流设计JSON
     * @param dataId            数据id
     * @param sendFormId        指定发起人节点表单id
     * @return 工作流任务信息
     */
    @Override
    public FlowTask buildSaveFlowTask(String flowDesignModelId, FlowDesign flowDesign, String design, String dataId, String sendFormId) {
        FlowTask flowTask = new FlowTask();
        flowTask.setId(IdWorker.getIdStr());
        flowTask.setName(flowDesign.getName());
        flowTask.setFlowDesignId(flowDesign.getId());
        flowTask.setFlowDesign(design);
        flowTask.setDataModelId(flowDesign.getDataModelId());
        flowTask.setDesignModelId(ObjectNull.isNull(flowDesignModelId) ? flowDesign.getDataModelId() : flowDesignModelId);
        // 优先使用指定的表单id作为工作流设计的发起人表单，若没指定，则使用工作流设计的发起人表单作为发起人表单id
        String formId = StringUtils.isNotBlank(sendFormId) ? sendFormId : flowDesign.getFormId();
        if (ObjectNull.isNull(formId)) {
            throw new BusinessException("未设置发起人表单不能启动流程");
        }
        flowTask.setFormId(formId);
        flowTask.setFormVersion(flowDesign.getFormVersion());
        flowTask.setDataId(dataId);
        flowTask.setTaskStatus(FlowTaskStatusEnum.PENDING);
        flowTask.setJvsAppId(flowDesign.getJvsAppId());
        return flowTask;
    }

    @Override
    public FlowTask save(String flowDesignModelId, FlowDesign flowDesign, String design, String dataId) {
        FlowTask flowTask = buildSaveFlowTask(flowDesignModelId, flowDesign, design, dataId, null);
        save(flowTask);
        return flowTask;
    }

    @Override
    public FlowTask saveTest(UserDto userDto, FlowDesign flowDesign, String design, String dataId) {
        FlowTask flowTask = buildSaveFlowTask(null, flowDesign, design, dataId, null);
        flowTask.setCreateById(userDto.getId());
        flowTask.setCreateBy(userDto.getRealName());
        flowTask.setUpdateBy(userDto.getId());
        flowTask.setTest(Boolean.TRUE);
        save(flowTask);
        return flowTask;
    }

    @Override
    public ProgressDetailResDto getProgressDetail(String id, String nodeId) {
        ProgressDetailResDto dto = new ProgressDetailResDto();
        // 查询任务信息
        FlowTask flowTask = getTaskById(id);
        dto.setTaskName(flowTask.getName())
                .setTaskCode(flowTask.getTaskCode())
                .setTaskTitle(flowTask.getTitle())
                .setTaskStatus(flowTask.getTaskStatus())
                .setCreateTime(flowTask.getCreateTime());
        // 得到任务当前处理中的节点
        List<FlowTaskNode> flowTaskNodes = new ArrayList<>();
        if (FlowTaskStatusEnum.PENDING.equals(flowTask.getTaskStatus())) {
            flowTaskNodes = flowTaskNodeService.getCurrentNodesByTaskId(flowTask.getId());
        }
        if (StringUtils.isNotBlank(nodeId)) {
            flowTaskNodes = flowTaskNodes.stream().filter(taskNode -> taskNode.getNodeId().equals(nodeId)).collect(Collectors.toList());
        }
        // 工作流json解析，并缓存
        Node node = FlowUtil.parseNodeJsonAndCache(flowTask.getDesignBody());
        // 封装响应
        dto.setFlowTaskProgress(progress(flowTask, flowTaskNodes));
        dto.setExtend(getFlowExtend(flowTask.getFlowDesignId()));
        // 待办节点
        dto.setNodes(progressNodes(flowTask, flowTaskNodes, dto.getExtend()));
        dto.setJvsAppId(flowTask.getJvsAppId());
        // 增加流程标记
        FlowDesignProgressDto flowDesignProgressDto = BeanCopyUtil.copy(node, FlowDesignProgressDto.class);
        List<String> flowTaskNodeIds = flowTaskNodes.stream().map(FlowTaskNode::getNodeId).collect(Collectors.toList());
        dto.setFlowDesign(flowDesignProgress(flowDesignProgressDto, flowTaskNodeIds));
        // 参与流程的所有用户信息
        dto.setUsers(processUsers(dto.getFlowTaskProgress()));
        return dto;
    }

    /**
     * 得到待办节点信息
     *
     * @param flowTask
     * @param flowTaskNodes
     * @return
     */
    private List<ProgressNodeDetailDto> progressNodes(FlowTask flowTask, List<FlowTaskNode> flowTaskNodes, FlowExtendDto extend) {
        List<ProgressNodeDetailDto> nodes = new ArrayList<>();
        // 待办节点：返回当前节点表单id（发起人表单id or 自定义表单id）
        if (CollectionUtils.isEmpty(flowTaskNodes)) {
            ProgressNodeDetailDto defaultNode = new ProgressNodeDetailDto()
                    .setFormId(flowTask.getFormId())
                    .setFormVersion(flowTask.getFormVersion());
            nodes.add(defaultNode);
            return nodes;
        }
        nodes = flowTaskNodes.stream().map(taskNode -> {
            Node node = FlowUtil.findNode(taskNode.getNodeId());
            NodeForm nodeForm = FlowUtil.getNodeForm(node);
            if (NodeTypeEnum.ROOT.equals(node.getType())) {
                nodeForm.setFormId(flowTask.getFormId());
                nodeForm.setVersion(flowTask.getFormVersion());
            }
            return new ProgressNodeDetailDto()
                    .setFormId(Boolean.TRUE.equals(nodeForm.getSendUserForm()) ? flowTask.getFormId() : nodeForm.getFormId())
                    .setFormVersion(Boolean.TRUE.equals(nodeForm.getSendUserForm()) ? flowTask.getFormVersion() : nodeForm.getVersion())
                    .setNodeId(node.getId())
                    .setNodeName(node.getName())
                    .setApprovalType(taskNode.getApprovalType())
                    .setButtons(progressNodeButton(extend, node, taskNode.getApprovalType()))
                    .setEnableSign(node.getProps().getEnableSign());
        }).collect(Collectors.toList());
        return nodes;
    }

    /**
     * 得到审批节点可用的审批按钮的显示名称
     *
     * @param extend 扩展配置
     * @param node 节点
     * @param approvalType 节点审批类型
     * @return 审批节点可用的审批按钮
     */
    private List<FlowButtonDto> progressNodeButton(FlowExtendDto extend, Node node, FlowTaskNodeApprovalTypeEnum approvalType) {
        List<FlowButton> buttons = null;
        if (FlowTaskNodeApprovalTypeEnum.APPROVAL.equals(approvalType)) {
            buttons = node.getProps().getBtn();
        } else {
            buttons = node.getProps().getAppendApproval().getBtn();
        }
        // 待办节点可能没有按钮，如回退到发起人节点，发起人节点就是待办节点，但发起人节点没有可配置的按钮
        if (ObjectNull.isNull(buttons)) {
            return Collections.emptyList();
        }
        // 从扩展配置中获取审批按钮配置
        CustomFlowButtonDto customFlowButtonDto = Optional.ofNullable(extend.getFlowButton()).orElseGet(CustomFlowButtonDto::new);
        return buttons.stream()
                .filter(FlowButton::getEnable)
                .map(btn -> {
                    FlowButtonDto flowButton = new FlowButtonDto();
                    flowButton.setOperation(btn.getOperation());
                    flowButton.setDisplayName(btn.getDisplayName());
                    // 以节点按钮本身的自定义名称优先
                    if (ObjectNull.isNotNull(flowButton.getDisplayName())) {
                        return flowButton;
                    } else {
                        String displayName = null;
                        // 若节点按钮本身未配置自定义名称，则从扩展配置中获取按钮名
                        if (customFlowButtonDto.getEnable()) {
                            Optional<FlowButtonDto> optionalExtendFlowButton = extend.getFlowButton().getButtons()
                                    .stream()
                                    .filter(flowBtn -> btn.getOperation().equals(flowBtn.getOperation()))
                                    .findFirst();
                            if (optionalExtendFlowButton.isPresent()) {
                                displayName = optionalExtendFlowButton.get().getDisplayName();
                            }
                        }
                        if (ObjectNull.isNull(displayName)) {
                            displayName = btn.getName();
                        }
                        flowButton.setDisplayName(displayName);
                    }
                   return flowButton;
                })
                .collect(Collectors.toList());
    }

    /**
     * 封装任务进度
     *
     * @param flowTask      流程任务
     * @param flowTaskNodes 待办节点集合
     * @return
     */
    private List<FlowTaskProgressDto> progress(FlowTask flowTask, List<FlowTaskNode> flowTaskNodes) {
        // 任务处理结果数组
        LinkedList<CourseDto> courses = Optional.ofNullable(flowTask.getCourses()).orElse(new LinkedList<>());
        // 封装响应
        LinkedList<FlowTaskProgressDto> progressDtos = new LinkedList<>();
        for (CourseDto course : courses) {
            FlowTaskProgressDto progress = flowTaskProgressDto(course, flowTask);
            progressDtos.add(progress);
        }
        // 开始节点
        FlowTaskProgressDto progressOne = ObjectNull.isNotNull(progressDtos) ? progressDtos.get(0) : null;
        if (ObjectNull.isNotNull(progressOne) && NodeTypeEnum.ROOT.equals(progressOne.getNodeType())) {
            progressDtos.get(0)
                    .setFormId(flowTask.getFormId())
                    .setFormVersion(flowTask.getFormVersion());
        } else {
            String startTime = LocalDateTimeUtil.format(flowTask.getCreateTime(), DatePattern.NORM_DATETIME_PATTERN);
            FlowTaskProgressDto rootProgress = new FlowTaskProgressDto();
            rootProgress.setFormId(flowTask.getFormId())
                    .setFormVersion(flowTask.getFormVersion())
                    .setNodeName("开始")
                    .setTime(startTime)
                    .setApproveResultDtos(Arrays.asList(new ApproveResultDto().setUserName(flowTask.getCreateBy()).setUserId(flowTask.getCreateById()).setTime(startTime)));
            rootProgress.setNodeType(NodeTypeEnum.ROOT);
            progressDtos.addFirst(rootProgress);
        }

        // 若是已终止，则作为最后一个进度，不处理
        if (FlowTaskStatusEnum.TERMINATED.equals(flowTask.getTaskStatus())) {
            return progressDtos;
        }
        if (FlowTaskStatusEnum.PENDING.equals(flowTask.getTaskStatus())) {
            //封装处理中的节点进度
            currentPendingNodeProgress(flowTask, progressDtos, flowTaskNodes);
        } else {
            // 任务已结束，插入结束节点
            FlowTaskProgressDto endCourse = new FlowTaskProgressDto();
            endCourse.setNodeName("结束")
                    .setTime(LocalDateTimeUtil.format(flowTask.getUpdateTime(), DatePattern.NORM_DATETIME_PATTERN));
            progressDtos.addLast(endCourse);
        }

        return progressDtos;
    }

    /**
     * 封装处理中的节点进度
     *
     * @param flowTask      任务
     * @param progressDtos  任务进度
     * @param flowTaskNodes 待办节点集合
     * @return
     */
    private void currentPendingNodeProgress(FlowTask flowTask, LinkedList<FlowTaskProgressDto> progressDtos, List<FlowTaskNode> flowTaskNodes) {
        if (CollectionUtils.isEmpty(flowTaskNodes)) {
            return;
        }
        flowTaskNodes.forEach(flowTaskNode -> {
            // 查询任务节点待处理人
            List<FlowTaskPerson> flowTaskPersonList = flowTaskPersonService.list(Wrappers.<FlowTaskPerson>lambdaQuery()
                    .eq(FlowTaskPerson::getFlowTaskId, flowTask.getId())
                    .eq(FlowTaskPerson::getNodeId, flowTaskNode.getNodeId())
                    .eq(FlowTaskPerson::getProcessStatus, ProcessStatusEnum.PENDING)
                    .orderByAsc(FlowTaskPerson::getNumber));
            if (CollectionUtils.isEmpty(flowTaskPersonList)) {
                return;
            }
            // 封装处理中的节点进度
            CourseDto nodeCourse = flowTaskNode.getCourse();
            List<ApproveResultDto> approveResultDtos = nodeCourse.getApproveResultDtos();
            flowTaskPersonList.forEach(person -> {
                // 未处理的用户，加入审核进度（处理中）
                ApproveResultDto approveResultDto = new ApproveResultDto()
                        .setUserId(person.getUserId())
                        .setUserName(person.getUserName());
                approveResultDtos.add(approveResultDto);
            });
            nodeCourse.setApproveResultDtos(approveResultDtos);
            FlowTaskProgressDto progress = flowTaskProgressDto(nodeCourse, flowTask);
            if (Boolean.TRUE.equals(progress.getNodeId().equals(progressDtos.getLast().getNodeId()))) {
                // 最后一个进度节点id与正在执行的节点id相同，则移除该节点进度
                progressDtos.removeLast();
            }
            progressDtos.addLast(progress);
        });
    }

    /**
     * 封装 任务进度响应-工作流任务进度
     *
     * @param course
     * @param flowTask
     * @return
     */
    private FlowTaskProgressDto flowTaskProgressDto(CourseDto course, FlowTask flowTask) {
        FlowTaskProgressDto progress = BeanCopyUtil.copy(course, FlowTaskProgressDto.class);
        if (ObjectNull.isNull(course.getNodeId())) {
            return progress;
        }
        Node node = FlowUtil.findNode(course.getNodeId());
        NodeForm nodeForm = FlowUtil.getNodeForm(node);
        progress.setMode(Optional.ofNullable(course.getMode()).orElseGet(() -> node.getProps().getMode()));
        progress.setModeProps(node.getProps().getModeProps());
        // 设置开始节点的发起人表单为任务的发起人表单
        if (NodeTypeEnum.ROOT.equals(node.getType())) {
            progress.setFormId(flowTask.getFormId())
                    .setFormVersion(flowTask.getFormVersion());
        } else {
            // 非开始节点的发起人表单
            progress.setFormId(Boolean.TRUE.equals(nodeForm.getSendUserForm()) ? flowTask.getFormId() : nodeForm.getFormId());
            progress.setFormVersion(Boolean.TRUE.equals(nodeForm.getSendUserForm()) ? flowTask.getFormVersion() : nodeForm.getVersion());
        }

        progress.setNodeType(node.getType());
        return progress;
    }

    /**
     * 增加流程标记
     *
     * @param node            工作流设计
     * @param flowTaskNodeIds 待办节点id集合
     * @return
     */
    private FlowDesignProgressDto flowDesignProgress(FlowDesignProgressDto node, List<String> flowTaskNodeIds) {
        // 当前节点id为空，视为流程已结束
        if (CollectionUtils.isEmpty(flowTaskNodeIds)) {
            return node;
        }
        // 标记当前节点
        if (ObjectNull.isNotNull(node.getId()) && flowTaskNodeIds.contains(node.getId())) {
            node.setCurrentNode(Boolean.TRUE);
            return node;
        }
        Node nextNode = node.getNode();
        if (nextNode != null) {
            if (NodeTypeEnum.CONDITION.equals(node.getType())) {
                node.getConditions().forEach(n -> {
                    if (n.getNode() != null && ObjectNull.isNotNull(n.getId())) {
                        FlowDesignProgressDto dto = BeanCopyUtil.copy(n.getNode(), FlowDesignProgressDto.class);
                        n.setNode(dto);
                        flowDesignProgress(dto, flowTaskNodeIds);
                    }
                });
            }
            if (NodeTypeEnum.PARALLEL.equals(node.getType())) {
                node.getParallels().forEach(n -> {
                    if (n.getNode() != null && ObjectNull.isNotNull(n.getId())) {
                        FlowDesignProgressDto dto = BeanCopyUtil.copy(n.getNode(), FlowDesignProgressDto.class);
                        n.setNode(dto);
                        flowDesignProgress(dto, flowTaskNodeIds);
                    }
                });
            }
            FlowDesignProgressDto dto = BeanCopyUtil.copy(nextNode, FlowDesignProgressDto.class);
            node.setNode(dto);
            flowDesignProgress(dto, flowTaskNodeIds);
        }

        return node;
    }

    /**
     * 获取参与流程的所有用户信息
     *
     * @param progress 流程进度
     * @return 用户信息
     */
    private List<UserDto> processUsers(List<FlowTaskProgressDto> progress) {
        Set<String> userIds = progress.stream().filter(course -> CollectionUtils.isNotEmpty(course.getApproveResultDtos()))
                .map(CourseDto::getApproveResultDtos)
                .flatMap(Collection::stream).map(ApproveResultDto::getUserId).collect(Collectors.toSet());
        List<UserDto> userDtos = authUserServiceApi.getByIds(new ArrayList<>(userIds)).getData();
        return userDtos.stream().map(u -> new UserDto()
                        .setId(u.getId())
                        .setRealName(u.getRealName())
                        .setHeadImg(u.getHeadImg())
                        .setDept(u.getDept()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProgressPrintResDto> getProgressPrint(String id) {
        // 查询任务信息
        FlowTask flowTask = Optional.ofNullable(getById(id)).orElseThrow(() -> new BusinessException("任务不存在或已结束"));
        if (CollectionUtils.isEmpty(flowTask.getCourses())) {
            return Collections.emptyList();
        }

        ApproveResultDto rootResult = flowTask.getCourses().get(0).getApproveResultDtos().get(0);
        return flowTask.getCourses().stream()
                .map(course -> {
                    if (NodeTypeEnum.ROOT.equals(course.getNodeType())) {
                        ProgressPrintResDto dto = BeanCopyUtil.copy(rootResult, ProgressPrintResDto.class);
                        dto.setNodeId(course.getNodeId());
                        dto.setNodeName(NodeTypeEnum.ROOT.getDefaultNodeName());
                        if (ObjectNull.isNotNull(course.getApproveResultDtos())) {
                            dto.setNodeOperation("提交申请");
                        } else {
                            dto.setNodeOperation("重新提交");
                        }
                        return Stream.of(dto).collect(Collectors.toList());
                    } else {
                        return course.getApproveResultDtos().stream()
                                .map(approve -> {
                                    ProgressPrintResDto dto = BeanCopyUtil.copy(approve, ProgressPrintResDto.class);
                                    dto.setNodeId(course.getNodeId());
                                    dto.setNodeName(course.getNodeName());
                                    if (ObjectNull.isNotNull(approve.getNodeOperationTypeEnum())) {
                                        dto.setNodeOperation(approve.getNodeOperationTypeEnum().getName());
                                    }
                                    if (ObjectNull.isNotNull(approve.getOpinion())) {
                                        dto.setOpinionContent(approve.getOpinion().getContent());
                                        dto.setOpinionSign(Optional.ofNullable(approve.getOpinion().getSign())
                                                .map(signs -> {
                                                    return signs.stream()
                                                            .map(sign -> {
                                                                BaseFile baseFile = BeanCopyUtil.copy(sign, BaseFile.class);
                                                                baseFile.setUrl(ossTemplate.fileLink(baseFile.getFileName(), Optional.ofNullable(baseFile.getBucketName()).orElseGet(() -> "jvs-form-design")));
                                                                return baseFile;
                                                            })
                                                            .collect(Collectors.toList());
                                                })
                                                .orElse(null));
                                    }
                                    return dto;
                                }).collect(Collectors.toList());
                    }

                }
                )
                .flatMap(Collection::stream).collect(Collectors.toList());
    }


    @Override
    public Integer selfCreateCount(List<String> appIds, UserDto userDto) {
        long count = count(Wrappers.<FlowTask>lambdaQuery()
                .in(FlowTask::getJvsAppId, appIds)
                .eq(FlowTask::getTest, Boolean.FALSE)
                .eq(FlowTask::getCreateById, userDto.getId()));
        return ((Long.valueOf(count)).intValue());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void urge(UserDto userDto, String taskId) {
        // 查询是否启用了催办配置
        SysNoticeConfig sysNoticeConfig = Optional.ofNullable(dataNoticeService.getTenantSystemConfig(TriggerTypeEnum.FLOW_URGE)).orElseThrow(() -> new BusinessException("未启用催办提醒"));
        if (Boolean.FALSE.equals(sysNoticeConfig.getEnabled()) || ObjectNull.isNull(sysNoticeConfig.getTimeLimit())) {
            throw new BusinessException("未启用催办提醒");
        }
        String key = SystemConstant.SYSTEM_NAME + ":" + "urge:" + userDto.getId() + ":" + taskId;
        Object val = redisUtils.get(key);
        if (ObjectNull.isNotNull(val)) {
            throw new BusinessException("操作过于频繁请稍后");
        }
        redisUtils.set(key, LocalDateTimeUtil.format(LocalDateTime.now(), DatePattern.NORM_DATETIME_PATTERN), sysNoticeConfig.getTimeLimit().calculateMill());
        // 查询工作流任务信息
        FlowTask flowTask = getOne(Wrappers.<FlowTask>lambdaQuery().eq(FlowTask::getId, taskId).eq(FlowTask::getCreateById, userDto.getId()));
        if (ObjectNull.isNull(flowTask) || Boolean.FALSE.equals(FlowTaskStatusEnum.PENDING.equals(flowTask.getTaskStatus()))) {
            throw new BusinessException("任务已结束");
        }
        List<String> taskNodeIds = flowTaskNodeService.getCurrentNodesByTaskId(taskId).stream().map(FlowTaskNode::getNodeId).collect(Collectors.toList());
        applicationEventPublisher.publishEvent(new FlowNotifyEvent(this, flowTask, TriggerTypeEnum.FLOW_URGE, taskNodeIds, TenantContextHolder.getTenantId()));
    }


    @Override
    public void pendingApprovePage(Page<PendingApprovesResDto> page, UserDto userDto, PendingApprovesReqDto dto) {
        // 查询当前模式应用id集合
        List<String> appIds = appVersionService.getVersionTypeAppIds(ModeUtils.getMode());
        if (CollectionUtils.isEmpty(appIds)) {
            return;
        }
        QueryWrapper queryWrapper = Wrappers.query()
                .eq("taskPerson.user_id", userDto.getId())
                .eq("taskPerson.process_status", ProcessStatusEnum.PENDING)
                .in("taskPerson.jvs_app_id", appIds)
                .eq("taskPerson.hang", Boolean.FALSE)
                .eq("t.task_status", FlowTaskStatusEnum.PENDING)
                .eq("t.test", Boolean.FALSE)
                .eq(StringUtils.isNotBlank(dto.getTaskId()), "t.id", dto.getTaskId())
                // 无表单，不显示
                .isNotNull("t.form_id")
                .like(StringUtils.isNotBlank(dto.getFlowName()), "t.name", dto.getFlowName())
                .like(StringUtils.isNotBlank(dto.getTitle()), "t.title", dto.getTitle())
                .like(StringUtils.isNotBlank(dto.getSendUser()), "t.create_by", dto.getSendUser())
                .like(StringUtils.isNotBlank(dto.getTaskCode()), "t.task_code", dto.getTaskCode());
        baseMapper.pendingApprovePage(page, queryWrapper);
    }


    @Override
    public void departTransfer(FlowTaskProxy flowTaskProxy) {
        // 查询被代理人所有待处理任务
        List<FlowTaskPerson> taskPersons = flowTaskPersonService.list(Wrappers.<FlowTaskPerson>lambdaQuery()
                .eq(FlowTaskPerson::getProcessStatus, ProcessStatusEnum.PENDING)
                .eq(FlowTaskPerson::getUserId, flowTaskProxy.getUserId()));
        if (CollectionUtils.isEmpty(taskPersons)) {
            return;
        }
        // 任务转交
        taskPersons.forEach(person -> {
            person.setUserId(flowTaskProxy.getProxyUserId());
            person.setUserName(flowTaskProxy.getProxyUserName());
        });
        flowTaskPersonService.updateBatchById(taskPersons);
        // 保存转交记录
        ProxyDto transferDto = new ProxyDto();
        transferDto.setProxy(Boolean.TRUE);
        transferDto.setUserId(flowTaskProxy.getUserId());
        transferDto.setUserName(flowTaskProxy.getUserName());
        transferDto.setProxyUserId(flowTaskProxy.getProxyUserId());
        transferDto.setProxyUserName(flowTaskProxy.getProxyUserName());
        transferDto.setTime(LocalDateTimeUtil.format(LocalDateTime.now(), DatePattern.NORM_DATETIME_PATTERN));
        transferDto.setDirections(StringUtils.isBlank(flowTaskProxy.getDescription()) ? "代理设置" : flowTaskProxy.getDescription());
        List<FlowTaskNode> flowTaskNodes = taskPersons.stream()
                .map(person -> new FlowTaskNode().setFlowTaskId(person.getFlowTaskId()).setNodeId(person.getNodeId()))
                .distinct().collect(Collectors.toList());
        flowTaskNodes.forEach(flowTaskNode -> {
            flowTaskNodeService.saveTransfer(Collections.singletonList(transferDto), flowTaskNode.getFlowTaskId(), flowTaskNode.getNodeId());
        });
    }

    @Override
    public void updateDesignApprover(FlowTask flowTask, FlowReqDto flowDto) {
        // 自选审核人修改配置
        String design = FlowUtil.setSelfSelectApprover(false, flowTask.getDesignBody(), getFlowExtend(flowTask.getFlowDesignId()), flowDto.getApprovers());
        // 设计已变更，设置新的工作流设计
        if (FlowUtil.checkDesignChange(flowTask.getDesignBody(), design)) {
            flowTask.setFlowDesign(design);
        }
        updateById(flowTask);
    }

    @Override
    public int countPendingByDesignId(String designId) {
        long count = count(Wrappers.<FlowTask>lambdaQuery().eq(FlowTask::getFlowDesignId, designId).eq(FlowTask::getTaskStatus, FlowTaskStatusEnum.PENDING));
        return Long.valueOf(count).intValue();
    }

    @Override
    public int countByModeId(String modeId) {
        long count = count(Wrappers.<FlowTask>lambdaQuery().eq(FlowTask::getDataModelId, modeId));
        return Long.valueOf(count).intValue();
    }

    @Override
    public Collection<String> pendingFlowDesignIds(Collection<String> flowDesignIds) {
        return list(Wrappers.<FlowTask>lambdaQuery()
                .in(FlowTask::getFlowDesignId, flowDesignIds)
                .eq(FlowTask::getTaskStatus, FlowTaskStatusEnum.PENDING))
                .stream()
                .map(FlowTask::getFlowDesignId).collect(Collectors.toSet());
    }

    @Override
    public boolean checkCanRestart(String userId, String flowTaskId) {
        // 未结束的任务不可重启
        FlowTask flowTask = getOne(Wrappers.<FlowTask>lambdaQuery()
                .eq(FlowTask::getId, flowTaskId)
                .eq(FlowTask::getCreateById, userId));
        if (ObjectNull.isNull(flowTask) || FlowTaskStatusEnum.PENDING.equals(flowTask.getTaskStatus())) {
            return Boolean.FALSE;
        }
        // 不是自己发起的任务，不能重启
        if (Boolean.FALSE.equals(userId.equals(flowTask.getCreateById()))) {
            return Boolean.FALSE;
        }
        // 工作流设计是否允许发起人重启任务
        FlowExtendDto extend = getFlowExtend(flowTask.getFlowDesignId());
        return extend.getEnableRestart();
    }

    @Override
    public boolean checkCancel(String userId, String flowTaskId) {
        FlowTask flowTask = getOne(Wrappers.<FlowTask>lambdaQuery()
                .eq(FlowTask::getId, flowTaskId)
                .eq(FlowTask::getCreateById, userId));
        // 已结束的任务不能取消
        if (ObjectNull.isNull(flowTask) || Boolean.FALSE.equals(FlowTaskStatusEnum.PENDING.equals(flowTask.getTaskStatus()))) {
            return Boolean.FALSE;
        }
        // 工作流设计是否允取消任务
        FlowExtendDto extend = getFlowExtend(flowTask.getFlowDesignId());
        return extend.getEnableCancel();
    }

    private FlowExtendDto getFlowExtend(String designId) {
        return Optional.ofNullable(flowDesignMapper.selectOne(Wrappers.<FlowDesign>lambdaQuery()
                .eq(FlowDesign::getId, designId)
                .select(FlowDesign::getExtend))
        ).orElse(new FlowDesign().setExtend(new FlowExtendDto())).getExtend();
    }

    @Async
    @Override
    public void cleanTaskExecutiveProcess(String taskId) {
        // 删除工作流流转节点数据
        flowTaskNodeService.removeTaskAll(taskId);
        // 删除待办人所有信息
        flowTaskPersonService.removeTaskAll(taskId);
        // 删除并行任务信息
        flowTaskParallelService.removeTaskAll(taskId);
        // 删除工作流可执行路径
        flowTaskPathService.removeTaskPath(taskId);
    }

    @Override
    public Boolean havePendingTask(String dataId) {
        return listByDataIds(Collections.singletonList(dataId)).stream().anyMatch(task -> FlowTaskStatusEnum.PENDING.equals(task.getTaskStatus()));
    }

    @Override
    public FlowTask getPendingTask(String dataId) {
        return listByDataIds(Collections.singletonList(dataId)).stream()
                .filter(task -> FlowTaskStatusEnum.PENDING.equals(task.getTaskStatus()))
                .findAny()
                .orElse(null);
    }

    @Override
    public List<FlowTask> listByDataId(String dataId) {
        return list(Wrappers.<FlowTask>lambdaQuery().eq(FlowTask::getDataId, dataId));
    }

    @Override
    public List<FlowTask> listByDataIds(List<String> dataIds) {
        return list(Wrappers.<FlowTask>lambdaQuery().in(FlowTask::getDataId, dataIds));
    }

    @Override
    public FlowTask getById(Serializable id) {
        FlowTask flowTask = super.getById(id);
        if (ObjectNull.isNull(flowTask)) {
            return null;
        }
        if (ObjectNull.isNotNull(flowTask.getFlowDesign())) {
            flowTask.setDesignBody(flowTask.getFlowDesign());
            return flowTask;
        }
        // 没保存设计，则根据设计版本id获取工作流设计
        setFlowDesignBody(flowTask);
        return flowTask;
    }

    @Override
    public FlowTask getTaskById(String id) {
        FlowTask flowTask = Optional.ofNullable(getById(id)).orElseThrow(() -> new BusinessException("任务不存在或已结束"));
        if (ObjectNull.isNotNull(flowTask.getFlowDesign())) {
            flowTask.setDesignBody(flowTask.getFlowDesign());
            return flowTask;
        }
        // 没保存设计，则根据设计版本id获取工作流设计
        setFlowDesignBody(flowTask);
        return flowTask;
    }

    private void setFlowDesignBody(FlowTask flowTask) {
        FlowDesignVersion flowDesignVersion = Optional.ofNullable(flowDesignVersionService.getById(flowTask.getFlowVersionId()))
                .orElseThrow(() -> new BusinessException("获取工作流设计失败"));
        flowTask.setDesignBody(flowDesignVersion.getDesignBody());
    }

    @Override
    public void fillBatchTaskDesignBody(List<? extends FlowTask> flowTasks) {
        if (ObjectNull.isNull(flowTasks)) {
            return;
        }
        // 没有保存设计的任务，需要根据设计版本id查询设计
        Set<String> flowVersionIds = flowTasks.stream()
                .filter(task -> ObjectNull.isNull(task.getFlowDesign()))
                .map(FlowTask::getFlowVersionId)
                .collect(Collectors.toSet());
        // 数据结构：Map<版本id, 工作流完整设计>
        Map<String, String> flowVersionMap = ObjectNull.isNull(flowVersionIds) ? Collections.emptyMap() : flowDesignVersionService.listByIds(flowVersionIds).stream()
                .collect(Collectors.toMap(FlowDesignVersion::getId, FlowDesignVersion::getDesignBody));
        flowTasks.forEach(task -> {
            if (ObjectNull.isNull(task.getFlowDesign())) {
                task.setDesignBody(flowVersionMap.get(task.getFlowVersionId()));
            } else {
                task.setDesignBody(task.getFlowDesign());
            }
        });
    }

    @Override
    public void fillTaskDesignBody(FlowTask flowTask) {
        if (ObjectNull.isNotNull(flowTask.getFlowDesign())) {
            flowTask.setDesignBody(flowTask.getFlowDesign());
            return;
        }
        // 没保存设计，则根据设计版本id获取工作流设计
        setFlowDesignBody(flowTask);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeTaskAllByDataId(List<String> dataIds) {
        if (ObjectNull.isNull(dataIds)) {
            return;
        }
        List<String> taskIds = listByDataIds(dataIds).stream().map(FlowTask::getId).collect(Collectors.toList());
        removeTaskAll(taskIds);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeTaskAllByDataModelId(String dataModelId) {
        if (ObjectNull.isNull(dataModelId)) {
            return;
        }
        List<String> taskIds = list(Wrappers.<FlowTask>lambdaQuery()
                .in(FlowTask::getDataModelId, dataModelId)
                .select(FlowTask::getId))
                .stream()
                .map(FlowTask::getId)
                .collect(Collectors.toList());
        removeTaskAll(taskIds);
    }

    /**
     * 删除流程任务
     *
     * @param taskIds 任务id集合
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeTaskAll(List<String> taskIds) {
        if (ObjectNull.isNull(taskIds)) {
            return;
        }
        removeBatchByIds(taskIds);
        flowTaskApprovalRecordService.remove(Wrappers.<FlowTaskApprovalRecord>lambdaQuery().in(FlowTaskApprovalRecord::getFlowTaskId, taskIds));
        flowTaskCarbonCopyService.remove(Wrappers.<FlowTaskCopied>lambdaQuery().in(FlowTaskCopied::getFlowTaskId, taskIds));
        flowTaskNodeService.remove(Wrappers.<FlowTaskNode>lambdaQuery().in(FlowTaskNode::getFlowTaskId, taskIds));
        flowTaskParallelService.remove(Wrappers.<FlowTaskParallel>lambdaQuery().in(FlowTaskParallel::getFlowTaskId, taskIds));
        flowTaskPathService.remove(Wrappers.<FlowTaskPath>lambdaQuery().in(FlowTaskPath::getFlowTaskId, taskIds));
        flowTaskPersonService.remove(Wrappers.<FlowTaskPerson>lambdaQuery().in(FlowTaskPerson::getFlowTaskId, taskIds));
    }
}
