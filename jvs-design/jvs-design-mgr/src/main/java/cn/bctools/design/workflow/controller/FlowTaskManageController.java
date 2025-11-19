package cn.bctools.design.workflow.controller;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.design.notice.handler.enums.TriggerTypeEnum;
import cn.bctools.design.project.entity.enums.AppVersionTypeEnum;
import cn.bctools.design.project.service.JvsAppVersionService;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.design.workflow.dto.*;
import cn.bctools.design.workflow.dto.manage.TaskAssignDto;
import cn.bctools.design.workflow.dto.manage.TaskManagePageDto;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.FlowTaskNode;
import cn.bctools.design.workflow.entity.FlowTaskPerson;
import cn.bctools.design.workflow.entity.enums.FlowTaskStatusEnum;
import cn.bctools.design.workflow.entity.enums.ProcessStatusEnum;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import cn.bctools.design.workflow.service.*;
import cn.bctools.design.workflow.support.listener.asynctask.AsyncTaskDynamicDataEvent;
import cn.bctools.design.workflow.support.listener.notify.FlowNotifyEvent;
import cn.bctools.design.workflow.utils.FlowUtil;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zxk
 */
@Slf4j
@Api(tags = "[workflow]工作流任务管理")
@AllArgsConstructor
@RestController
@RequestMapping("/base/workflow/task/manage")
public class FlowTaskManageController {

    private final FlowTaskService flowTaskService;
    private final TaskService taskService;
    private final JvsAppVersionService appVersionService;
    private final TaskStopService taskStopService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final FlowTaskNodeService flowTaskNodeService;
    private final FlowTaskPersonService flowTaskPersonService;

    @Log
    @ApiOperation("流程任务分页")
    @GetMapping("/page")
    public R<Page<PageFlowTaskManageResDto>> manageTaskPage(Page<FlowTask> page, TaskManagePageDto dto) {
        // 查询当前模式应用id集合
        AppVersionTypeEnum mode = ObjectNull.isNotNull(dto.getMode()) ? dto.getMode() : ModeUtils.getMode();
        List<String> appIds = appVersionService.getVersionTypeAppIds(mode);
        if (CollectionUtils.isEmpty(appIds)) {
            return R.ok();
        }
        LambdaQueryWrapper<FlowTask> wrapper = Wrappers.<FlowTask>lambdaQuery()
                .eq(FlowTask::getTaskStatus, FlowTaskStatusEnum.PENDING)
                .in(FlowTask::getJvsAppId, appIds)
                .like(ObjectNull.isNotNull(dto.getTaskCode()), FlowTask::getTaskCode, dto.getTaskCode())
                .like(ObjectNull.isNotNull(dto.getFlowName()), FlowTask::getName, dto.getFlowName())
                .like(ObjectNull.isNotNull(dto.getTitle()), FlowTask::getTitle, dto.getTitle())
                .orderByDesc(FlowTask::getCreateTime);
        flowTaskService.page(page, wrapper);
        // 填充工作流任务使用的设计
        flowTaskService.fillBatchTaskDesignBody(page.getRecords());

        // 转换结果
        Page<PageFlowTaskManageResDto> pageDto = new Page<>(page.getCurrent(), page.getSize());
        if (ObjectNull.isNull(page.getRecords())) {
            return R.ok(pageDto);
        }
        // 填充数据
        List<PageFlowTaskManageResDto> resultList = BeanCopyUtil.copys(page.getRecords(), PageFlowTaskManageResDto.class);
        pageDto.setRecords(resultList);
        pageDto.setTotal(page.getTotal());
        // 未结束的任务id
        List<String> taskIds = page.getRecords().stream()
                .filter(flowTask -> FlowTaskStatusEnum.PENDING.equals(flowTask.getTaskStatus()))
                .map(FlowTask::getId).collect(Collectors.toList());
        // Map<任务id, 节点id集合>
        Map<String, List<String>> taskNodeIdMap = new HashMap<>();
        // Map<任务id, 待审批人>
        Map<String, List<FlowTaskPerson>> pendingTaskPersonMap = new HashMap<>();
        if (ObjectNull.isNotNull(taskIds)) {
            // 获取待办节点
            taskNodeIdMap = Optional.ofNullable(flowTaskNodeService.getCurrentNodeByTaskIds(taskIds))
                    .orElseGet(ArrayList::new)
                    .stream()
                    .collect(Collectors.groupingBy(FlowTaskNode::getFlowTaskId,Collectors.mapping(FlowTaskNode::getNodeId, Collectors.toList())));
            // 查询任务节点待处理人
            pendingTaskPersonMap = ObjectNull.isNull(taskIds) ? Collections.emptyMap() :
                    flowTaskPersonService.listPerson(taskIds)
                            .stream()
                            .filter(p -> !ProcessStatusEnum.PROCESSED.equals(p.getProcessStatus()))
                            .collect(Collectors.groupingBy(FlowTaskPerson::getFlowTaskId));
        }
        for (PageFlowTaskManageResDto task : pageDto.getRecords()) {
            String flowDesignBody = task.getDesignBody();
            List<String> taskNodeIds = taskNodeIdMap.get(task.getId());
            if (ObjectNull.isNull(taskNodeIds)) {
                task.setDesignBody(null);
                task.setFlowDesign(null);
                continue;
            }
            StringBuilder currentNodeName = new StringBuilder();
            List<FlowTaskManageNodeDto> nodes = new ArrayList<>();
            List<Node> designNodes = FlowUtil.getNodesByNodeType(flowDesignBody, Arrays.asList(NodeTypeEnum.SP, NodeTypeEnum.ROOT));
            boolean rootNode = false;
            for (Node node : designNodes) {
                if (!taskNodeIds.contains(node.getId())) {
                    continue;
                }
                if (NodeTypeEnum.ROOT.equals(node.getType())) {
                    rootNode = true;
                }
                currentNodeName.append(node.getName()).append(",");
                List<UserDto> users = Optional.ofNullable(pendingTaskPersonMap.get(task.getId()))
                        .orElseGet(ArrayList::new)
                        .stream()
                        .filter(flowTaskPerson -> flowTaskPerson.getNodeId().equals(node.getId()))
                        .map(flowTaskPerson ->
                                new UserDto()
                                        .setId(flowTaskPerson.getUserId())
                                        .setRealName(flowTaskPerson.getUserName()))
                        .collect(Collectors.toList());
                // 只返回有审批用户的节点
                if (ObjectNull.isNotNull(users)) {
                    nodes.add(new FlowTaskManageNodeDto()
                            .setNodeId(node.getId())
                            .setNodeName(node.getName())
                            .setUsers(users));
                }
            }
            FlowUtil.clearNodeCache();
            task.setNodes(nodes);
            task.setCurrentNodeName(currentNodeName.deleteCharAt(currentNodeName.length() - 1).toString());
            task.setDesignBody(null);
            task.setFlowDesign(null);
            task.setRootNode(rootNode);
        }
        return R.ok(pageDto);
    }

    @Log
    @ApiOperation("增员审批")
    @PostMapping("/assign")
    public R<String> assign(@Validated @RequestBody TaskAssignDto dto) {
        UserDto userDto = UserCurrentUtils.getCurrentUser();
        taskService.assign(userDto, dto.getTaskId(), dto.getApprovers());
        return R.ok();
    }

    @Log
    @Transactional(rollbackFor = Exception.class)
    @ApiOperation("终止任务")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "工作流任务id", required = true)
    })
    @PostMapping("/stop/{id}")
    public R<String> stop(@PathVariable String id, @RequestBody StopTaskReqDto stopTaskDto) {
        // 查询工作流任务信息
        FlowTask flowTask = flowTaskService.getById(id);
        if (Boolean.FALSE.equals(FlowTaskStatusEnum.PENDING.equals(flowTask.getTaskStatus()))) {
            throw new BusinessException("任务已结束");
        }
        // 终止任务
        FlowTask task = taskStopService.terminationTask(UserCurrentUtils.getCurrentUser(), flowTask, stopTaskDto);
        // 发布事件：同步流程信息到业务数据
        applicationEventPublisher.publishEvent(new AsyncTaskDynamicDataEvent(this, task));
        // 发送消息通知
        applicationEventPublisher.publishEvent(new FlowNotifyEvent(this, flowTask, TriggerTypeEnum.FLOW_APPROVAL_RESULTS, null, TenantContextHolder.getTenantId()));
        return R.ok();
    }

    @Log
    @Transactional(rollbackFor = Exception.class)
    @ApiOperation("转交")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "工作流任务id", required = true)
    })
    @PostMapping("/transfer/{id}")
    public R<String> transfer(@PathVariable String id, @Validated @RequestBody List<FlowTaskManageTransferReqDto> transferList) {
        UserDto userDto = UserCurrentUtils.getCurrentUser();
        taskService.transfer(userDto, id, transferList);
        return R.ok();
    }

    @Log
    @Transactional(rollbackFor = Exception.class)
    @ApiOperation("减员审批")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "工作流任务id", required = true)
    })
    @PostMapping("/signer/remove/{id}")
    public R<String> removeApprover(@PathVariable String id, @Validated @RequestBody List<FlowTaskManageSignerRemoveReqDto> dto) {
        UserDto userDto = UserCurrentUtils.getCurrentUser();
        taskService.removeSigner(userDto, id, dto);
        return R.ok();
    }

}
