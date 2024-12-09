package cn.bctools.design.workflow.controller;

import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.design.notice.handler.DataNoticeHandler;
import cn.bctools.design.notice.handler.enums.TriggerTypeEnum;
import cn.bctools.design.project.service.JvsAppVersionService;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.design.workflow.dto.*;
import cn.bctools.design.workflow.dto.progress.ProgressDetailResDto;
import cn.bctools.design.workflow.dto.startflow.StartFlowReqDto;
import cn.bctools.design.workflow.dto.startflow.StartFlowVariables;
import cn.bctools.design.workflow.entity.FlowDesign;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.FlowTaskNode;
import cn.bctools.design.workflow.entity.dto.FlowExtendDto;
import cn.bctools.design.workflow.entity.enums.FlowTaskStatusEnum;
import cn.bctools.design.workflow.entity.enums.ProcessStatusEnum;
import cn.bctools.design.workflow.service.*;
import cn.bctools.design.workflow.support.listener.notify.FlowNotifyEvent;
import cn.bctools.design.workflow.utils.FlowUtil;
import cn.bctools.design.workflow.utils.FutureUtil;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 */
@Slf4j
@Api(tags = "[workflow]工作流任务")
@AllArgsConstructor
@RestController
@RequestMapping("/base/workflow/task")
public class FlowTaskController {

    private final FlowDesignService flowDesignService;
    private final FlowTaskService flowTaskService;
    private final TaskService taskService;
    private final TaskStopService taskStopService;
    private final FlowTaskCarbonCopyService flowTaskCarbonCopyService;
    private final TaskStatisticService taskStatisticService;
    private final FlowTaskApprovalRecordService flowTaskApprovalRecordService;
    private final AuthUserServiceApi authUserServiceApi;
    private final FlowTaskNodeService flowTaskNodeService;
    private final JvsAppVersionService appVersionService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Log
    @ApiOperation("启动工作流")
    @PostMapping("/start")
    public R<String> start(@Validated @RequestBody StartFlowReqDto dto) {
        UserDto userDto = UserCurrentUtils.getCurrentUser();
        StartFlowVariables startFlowVariables = BeanCopyUtil.copy(dto, StartFlowVariables.class);
        taskService.start(userDto, startFlowVariables);
        return R.ok();
    }

    @Log
    @ApiOperation("执行工作流任务")
    @PostMapping("/execute")
    public R<String> execute(@Validated @RequestBody FlowReqDto flowDto) {
        UserDto userDto = UserCurrentUtils.getCurrentUser();
        taskService.execute(flowDto, userDto);
        return R.ok();
    }

    @Log
    @ApiOperation("批量执行工作流任务")
    @PostMapping("/batch/execute")
    public R<String> batchExecute(@Validated @RequestBody FlowBatchReqDto batchReqDto) {
        // 得到批量处理任务的任务id
        List<FlowTaskNode> currentNodes = flowTaskNodeService.listByIds(batchReqDto.getIds()).stream()
                .filter(flowTaskNode -> ProcessStatusEnum.PENDING.equals(flowTaskNode.getProcessStatus()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(currentNodes)) {
            throw new BusinessException("任务不存在或已结束");
        }
        // 批量处理任务
        UserDto userDto = UserCurrentUtils.getCurrentUser();
        List<Future<Boolean>> futures = new ArrayList<>();
        currentNodes.stream()
                .map(taskNode -> {
                    FlowReqDto flowReqDto = BeanCopyUtil.copy(batchReqDto, FlowReqDto.class);
                    flowReqDto.setId(taskNode.getFlowTaskId());
                    flowReqDto.setNodeId(taskNode.getNodeId());
                    return flowReqDto;
                }).forEach(flowReqDto -> futures.add(taskService.batchExecute(flowReqDto, userDto)));
        // 等待所有任务处理完才返回
        FutureUtil.waitAllDone(futures);
        // 得到处理失败的
        StringBuilder futureErrorMsg = new StringBuilder();
        for (Future<Boolean> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                futureErrorMsg.append(e.getCause().getMessage()).append(StringPool.SEMICOLON);
            }
        }
        if (futureErrorMsg.length() >= 1) {
            return R.failed(futureErrorMsg.toString());
        }
        return R.ok();
    }

    @Log
    @ApiOperation(value = "重新发起工作流任务", notes = "已结束的工作流任务，可以重新发起")
    @PostMapping("/restart")
    public R<String> restart(@Validated @RequestBody StartFlowReqDto dto) {
        UserDto userDto = UserCurrentUtils.getCurrentUser();
        taskService.restartTask(dto, userDto);
        return R.ok();
    }

    @Log
    @ApiOperation("待我审批列表")
    @GetMapping("/pendingApproves")
    public R<Page<PendingApprovesResDto>> pendingApproves(Page<PendingApprovesResDto> page, PendingApprovesReqDto dto) {
        UserDto userDto = UserCurrentUtils.getCurrentUser();
        // 分页查询待审批任务
        flowTaskService.pendingApprovePage(page, userDto, dto);
        // 封装返回数据
        List<String> createByIds = page.getRecords().stream().map(FlowTask::getCreateById).collect(Collectors.toList());
        Map<String, UserDto> users = authUserServiceApi.getByIds(createByIds).getData().stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));
        // 填充工作流任务使用的设计
        flowTaskService.fillBatchTaskDesignBody(page.getRecords());

        page.getRecords().forEach(res -> {
            // 发起人头像
            UserDto user = users.get(res.getCreateById());
            if (ObjectNull.isNotNull(user)) {
                res.setHeadImg(user.getHeadImg());
            }
            // 审批节点
            res.setNodeName(FlowUtil.findNode(res.getDesignBody(), res.getNodeId()).getName());
            String taskNodeId = flowTaskNodeService.getOne(Wrappers.<FlowTaskNode>lambdaQuery()
                    .select(FlowTaskNode::getId)
                    .eq(FlowTaskNode::getFlowTaskId, res.getId())
                    .eq(FlowTaskNode::getNodeId, res.getNodeId())).getId();
            res.setTaskNodeId(taskNodeId);
            FlowUtil.clearNodeCache();
            res.setFlowDesign(null);
            res.setDesignBody(null);
        });
        return R.ok(page);
    }


    @Log
    @ApiOperation("自己发起的任务列表")
    @GetMapping("/selfs")
    public R<Page<SelfPageResDto>> selfPage(Page<FlowTask> page, SelfPageReqDto reqDto) {
        Page<SelfPageResDto> pageDto = new Page<>(page.getCurrent(), page.getSize());
        // 查询当前模式应用id集合
        List<String> appIds = appVersionService.getVersionTypeAppIds(ModeUtils.getMode());
        if (CollectionUtils.isEmpty(appIds)) {
            return R.ok(pageDto);
        }
        // 分页查询
        UserDto userDto = UserCurrentUtils.getCurrentUser();
        flowTaskService.page(page, Wrappers.<FlowTask>lambdaQuery()
                .eq(FlowTask::getCreateById, userDto.getId())
                .eq(FlowTask::getTest, Boolean.FALSE)
                .in(FlowTask::getJvsAppId, appIds)
                // 无表单，不显示
                .isNotNull(FlowTask::getFormId)
                .like(StringUtils.isNotBlank(reqDto.getName()), FlowTask::getName, reqDto.getName())
                .like(StringUtils.isNotBlank(reqDto.getTaskCode()), FlowTask::getTaskCode, reqDto.getTaskCode())
                .eq(reqDto.getTaskStatus() != null, FlowTask::getTaskStatus, reqDto.getTaskStatus())
                .orderByDesc(FlowTask::getCreateTime));
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return R.ok(pageDto);
        }
        // 填充工作流任务使用的设计
        flowTaskService.fillBatchTaskDesignBody(page.getRecords());

        // 查询工作流扩展配置
        List<String> flowDesignIds = page.getRecords().stream().map(FlowTask::getFlowDesignId).collect(Collectors.toList());
        List<FlowDesign> flowDesigns = flowDesignService.list(Wrappers.<FlowDesign>lambdaQuery().in(FlowDesign::getId, flowDesignIds).select(FlowDesign::getId, FlowDesign::getExtend));

        // 封装响应
        List<SelfPageResDto> selfPageDtos = new ArrayList<>();
        page.getRecords().forEach(task -> {
            SelfPageResDto selfPageDto = BeanCopyUtil.copy(task,SelfPageResDto.class);
            Optional<FlowDesign> flowDesign = flowDesigns.stream().filter(d -> task.getFlowDesignId().equals(d.getId())).findFirst();
            FlowExtendDto extend = flowDesign.isPresent() ? flowDesign.get().getExtend() : new FlowExtendDto();
            selfPageDto.setExtend(extend);
            // 获取人工节点集合
            selfPageDto.setManualNodes(flowDesignService.getManualNodes(task.getDesignBody()));
            // 工作流设计置空，减少返回数据大小
            selfPageDto.setFlowDesign(null);
            selfPageDto.setDesignBody(null);
            selfPageDtos.add(selfPageDto);
        });
        pageDto.setRecords(selfPageDtos);
        pageDto.setTotal(page.getTotal());

        // 获取发起人头像
        List<String> createByIds = page.getRecords().stream().map(FlowTask::getCreateById).collect(Collectors.toList());
        Map<String, UserDto> users = authUserServiceApi.getByIds(createByIds).getData().stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));
        pageDto.getRecords().forEach(res -> {
            UserDto user = users.get(res.getCreateById());
            if (ObjectNull.isNotNull(user)) {
                res.setHeadImg(user.getHeadImg());
            }
        });
        return R.ok(pageDto);
    }

    @Log
    @Transactional(rollbackFor = Exception.class)
    @ApiOperation("发起人撤回流程任务")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "工作流id", required = true)
    })
    @PutMapping("/stop/{id}")
    public R<String> stop(@PathVariable String id, @RequestBody StopTaskReqDto stopTaskDto) {
        // 撤回流程任务
        FlowTask flowTask = taskStopService.withdrawTask(UserCurrentUtils.getCurrentUser(), id, stopTaskDto);
        // 发送消息通知
        applicationEventPublisher.publishEvent(new FlowNotifyEvent(this, flowTask, TriggerTypeEnum.FLOW_APPROVAL_RESULTS, null, TenantContextHolder.getTenantId()));
        return R.ok();
    }

    @Log
    @ApiOperation("任务详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "任务id", required = true)
    })
    @GetMapping("/{id}")
    public R<FlowTask> detail(@PathVariable String id) {
        return R.ok(flowTaskService.getById(id));
    }


    @Log
    @ApiOperation("任务进度")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "任务id", required = true),
            @ApiImplicitParam(name = "nodeId", value = "节点id")
    })
    @GetMapping("/progress/{id}")
    public R<ProgressDetailResDto> getProgressDetail(@PathVariable String id, String nodeId) {
        return R.ok(flowTaskService.getProgressDetail(id, nodeId));
    }

    @Log
    @ApiOperation("批量查询任务进度")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "任务id集合", required = true)
    })
    @PostMapping("/progress/batch")
    public R<List<ProgressDetailResDto>> getBatchProgressDetail(@RequestBody List<String> ids) {
        if (ObjectNull.isNull(ids)) {
            return R.ok();
        }
        List<ProgressDetailResDto> progressDetailResList = ids
                .stream()
                .map(id -> flowTaskService.getProgressDetail(id, null))
                .sorted(Comparator.comparing(ProgressDetailResDto::getCreateTime))
                .collect(Collectors.toList());
        return R.ok(progressDetailResList);
    }

    @Log
    @ApiOperation("抄送给我任务列表")
    @GetMapping("/carbonCopys")
    public R<Page<CarbonCopyResDto>> carbonCopys(Page<CarbonCopyResDto> page, CarbonCopyReqDto dto) {
        flowTaskCarbonCopyService.carbonCopys(page, UserCurrentUtils.getCurrentUser(), dto);
        // 获取发起人头像
        List<String> createByIds = page.getRecords().stream().map(CarbonCopyResDto::getCreateById).collect(Collectors.toList());
        Map<String, UserDto> users = authUserServiceApi.getByIds(createByIds).getData().stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));
        page.getRecords().forEach(res -> {
            UserDto user = users.get(res.getCreateById());
            if (ObjectNull.isNotNull(user)) {
                res.setHeadImg(user.getHeadImg());
            }
        });
        return R.ok(page);
    }

    @Log
    @ApiOperation("我审批的任务记录")
    @GetMapping("/self_approve_log")
    public R<Page<SelfApproveLogResDto>> selfApproveLog(Page<SelfApproveLogResDto> page, SelfApproveLogReqDto dto) {
        Page<SelfApproveLogResDto> pageDto = new Page<>(page.getCurrent(), page.getSize());
        UserDto userDto = UserCurrentUtils.getCurrentUser();
        flowTaskApprovalRecordService.selfApproveLog(page, userDto, dto);
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return R.ok(pageDto);
        }
        // 查询未结束的流程节点,得到当前环节
        Function<List<SelfApproveLogResDto>, Map<String, String>> getPendingTaskNodeNameFunction = selfApproveLogRes -> {
            // 得到待办状态的任务
            List<String> taskIds = selfApproveLogRes.stream().map(FlowTask::getId).collect(Collectors.toList());
            List<FlowTask> pendingTasks = flowTaskService.listByIds(taskIds).stream()
                    .filter(task -> FlowTaskStatusEnum.PENDING.equals(task.getTaskStatus()))
                    .collect(Collectors.toList());
            if (ObjectNull.isNull(pendingTasks)) {
                return Collections.emptyMap();
            }
            // 填充工作流任务使用的设计
            flowTaskService.fillBatchTaskDesignBody(pendingTasks);
            // 的带待办状态任务当前审批节点
            List<String> pendingTaskIds = pendingTasks.stream().map(FlowTask::getId).collect(Collectors.toList());
            List<FlowTaskNode> currentNodes = flowTaskNodeService.getCurrentNodeByTaskIds(pendingTaskIds);
            if (ObjectNull.isNull(currentNodes)) {
                return Collections.emptyMap();
            }
            Map<String, String> taskDesignMap = pendingTasks.stream().collect(Collectors.toMap(FlowTask::getId, FlowTask::getDesignBody));
            Map<String, List<FlowTaskNode>> taskNodeMap = currentNodes.stream().collect(Collectors.groupingBy(FlowTaskNode::getFlowTaskId));
            return taskNodeMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> {
                FlowUtil.parseNodeJsonAndCache(taskDesignMap.get(e.getKey()));
                String nodeName = e.getValue().stream().map(n -> FlowUtil.findNode(n.getNodeId()).getName()).collect(Collectors.joining(StringPool.SEMICOLON));
                FlowUtil.clearNodeCache();
                return nodeName;
            }));
        };
        Map<String, String> pendingTaskNodeNameMap = getPendingTaskNodeNameFunction.apply(page.getRecords());


        // 获取发起人头像
        List<String> createByIds = page.getRecords().stream().map(FlowTask::getCreateById).collect(Collectors.toList());
        Map<String, UserDto> users = authUserServiceApi.getByIds(createByIds).getData().stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));
        List<SelfApproveLogResDto> resPage = BeanCopyUtil.copys(page.getRecords(), SelfApproveLogResDto.class);
        resPage.forEach(res -> {
            UserDto user = users.get(res.getCreateById());
            if (ObjectNull.isNotNull(user)) {
                res.setHeadImg(user.getHeadImg());
            }
            // 审批节点
            res.setNodeName(pendingTaskNodeNameMap.get(res.getId()));
        });

        pageDto.setRecords(resPage);
        pageDto.setTotal(page.getTotal());

        return R.ok(pageDto);
    }

    @Log
    @ApiOperation("任务统计")
    @GetMapping("/statistic")
    public R<FlowTaskStatisticResDto> statistic() {
        UserDto userDto = UserCurrentUtils.getCurrentUser();
        return R.ok(taskStatisticService.statistic(userDto));
    }

    @Log
    @ApiOperation("催办")
    @PutMapping("/urge/{taskId}")
    public R<String> urge(@PathVariable String taskId) {
        UserDto userDto = UserCurrentUtils.getCurrentUser();
        flowTaskService.urge(userDto, taskId);
        return R.ok();
    }

    @Log
    @ApiOperation("获取可回退的节点")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskId", value = "任务id", required = true),
            @ApiImplicitParam(name = "nodeId", value = "节点id")
    })
    @GetMapping("/can/back/node/{taskId}/{nodeId}")
    public R<List<CanBackNodeDto>> getCanBackNode(@PathVariable String taskId, @PathVariable String nodeId) {
        return R.ok(taskService.getCanBackNode(taskId, nodeId));
    }
}
