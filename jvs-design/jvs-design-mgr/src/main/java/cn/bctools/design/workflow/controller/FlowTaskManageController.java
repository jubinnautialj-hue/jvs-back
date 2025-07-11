package cn.bctools.design.workflow.controller;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.*;
import cn.bctools.design.notice.handler.enums.TriggerTypeEnum;
import cn.bctools.design.project.entity.enums.AppVersionTypeEnum;
import cn.bctools.design.project.service.JvsAppVersionService;
import cn.bctools.design.use.UseComponent;
import cn.bctools.design.util.ConditionMergeStrategy;
import cn.bctools.design.util.DateUtil;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.design.workflow.dto.*;
import cn.bctools.design.workflow.dto.manage.TaskAssignDto;
import cn.bctools.design.workflow.dto.manage.TaskManageExcelDto;
import cn.bctools.design.workflow.dto.manage.TaskManagePageDto;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.FlowTaskNode;
import cn.bctools.design.workflow.entity.FlowTaskPerson;
import cn.bctools.design.workflow.entity.dto.ApproveOpinionDto;
import cn.bctools.design.workflow.entity.dto.CourseDto;
import cn.bctools.design.workflow.entity.enums.FlowTaskStatusEnum;
import cn.bctools.design.workflow.entity.enums.ProcessStatusEnum;
import cn.bctools.design.workflow.enums.NodeOperationTypeEnum;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import cn.bctools.design.workflow.service.*;
import cn.bctools.design.workflow.support.listener.asynctask.AsyncTaskDynamicDataEvent;
import cn.bctools.design.workflow.support.listener.notify.FlowNotifyEvent;
import cn.bctools.design.workflow.utils.FlowUtil;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.web.utils.IpUtil;
import cn.hutool.core.lang.tree.Tree;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
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
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.postgresql.util.URLCoder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.time.ZoneId;
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
    UseComponent useComponent;

    @Log
    @ApiOperation("流程任务分页")
    @GetMapping("/page")
    public R<Page<PageFlowTaskManageResDto>> manageTaskPage(Page<FlowTask> page, TaskManagePageDto dto) {
        // 查询当前模式应用id集合
        AppVersionTypeEnum mode = ObjectNull.isNotNull(dto.getMode()) ? dto.getMode() : ModeUtils.getMode();
        //获取该用户该模式下的所有应用
        List<Tree<Object>> tree = useComponent.menu("", ModeUtils.getRealUser().getId(), IpUtil.isMobile(), mode, null).getKey();
        List<String> appIds = tree.stream().map(x -> String.valueOf(x.getId())).collect(Collectors.toList());
//        List<String> appIds = appVersionService.getVersionTypeAppIds(mode);
        if (CollectionUtils.isEmpty(appIds)) {
            return R.ok();
        }
        LambdaQueryWrapper<FlowTask> wrapper = Wrappers.<FlowTask>lambdaQuery()
                .eq(FlowTask::getTaskStatus, FlowTaskStatusEnum.PENDING)
                .in(FlowTask::getJvsAppId, appIds)
                .like(ObjectNull.isNotNull(dto.getTaskCode()), FlowTask::getTaskCode, dto.getTaskCode())
                .like(ObjectNull.isNotNull(dto.getFlowName()), FlowTask::getName, dto.getFlowName())
                .like(ObjectNull.isNotNull(dto.getTitle()), FlowTask::getTitle, dto.getTitle())
                .like(ObjectNull.isNotNull(dto.getJvsAppId()), FlowTask::getJvsAppId, dto.getJvsAppId())
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
                    .collect(Collectors.groupingBy(FlowTaskNode::getFlowTaskId, Collectors.mapping(FlowTaskNode::getNodeId, Collectors.toList())));
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
    @ApiOperation("流程导出")
    @GetMapping("/exportTaskManage")
    public void exportTaskManage(TaskManagePageDto dto, HttpServletResponse response) throws IOException {
        // 获取模式
        AppVersionTypeEnum mode = ObjectNull.isNotNull(dto.getMode()) ? dto.getMode() : ModeUtils.getMode();
        //获取该用户该模式下的所有应用
        List<Tree<Object>> tree = useComponent.menu("", ModeUtils.getRealUser().getId(), IpUtil.isMobile(), mode, null).getKey();
        List<String> appIds = tree.stream().map(x -> String.valueOf(x.getId())).collect(Collectors.toList());
        //组装查询条件
        LambdaQueryWrapper<FlowTask> wrapper = Wrappers.<FlowTask>lambdaQuery()
                .eq(FlowTask::getTaskStatus, FlowTaskStatusEnum.PENDING)
                .in(FlowTask::getJvsAppId, appIds)
                .like(ObjectNull.isNotNull(dto.getTaskCode()), FlowTask::getTaskCode, dto.getTaskCode())
                .like(ObjectNull.isNotNull(dto.getFlowName()), FlowTask::getName, dto.getFlowName())
                .like(ObjectNull.isNotNull(dto.getTitle()), FlowTask::getTitle, dto.getTitle())
                .like(ObjectNull.isNotNull(dto.getJvsAppId()), FlowTask::getJvsAppId, dto.getJvsAppId())
                .orderByDesc(FlowTask::getCreateTime);
        List<FlowTask> records = flowTaskService.list(wrapper);
        List<TaskManageExcelDto> taskManageExcelDtos = this.handleData(records);
        //将文件名称进行编码
        response.setContentType("application/ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = URLCoder.encode("审批进度报表.xlsx");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName);

        try {
            // 头的策略
            WriteCellStyle headWriteCellStyle = new WriteCellStyle();
            WriteFont headWriteFont = new WriteFont();
            headWriteFont.setFontName("宋体");
            headWriteFont.setFontHeightInPoints((short) 12);
            headWriteCellStyle.setWrapped(true);
            headWriteCellStyle.setWriteFont(headWriteFont);
            headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
            // 内容的策略
            WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
            WriteFont contentWriteFont = new WriteFont();
            // 字体大小
            contentWriteFont.setFontHeightInPoints((short) 10);
            contentWriteFont.setFontName("宋体");
            contentWriteCellStyle.setWrapped(true);
            contentWriteCellStyle.setWriteFont(contentWriteFont);
            contentWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            contentWriteCellStyle.setWrapped(true);
            contentWriteCellStyle.setBorderLeft(BorderStyle.THIN);
            contentWriteCellStyle.setBorderBottom(BorderStyle.THIN);
            contentWriteCellStyle.setBorderRight(BorderStyle.THIN);
            contentWriteCellStyle.setBorderTop(BorderStyle.THIN);
            // 这个策略是 头是头的样式 内容是内容的样式 其他的策略可以自己实现
            HorizontalCellStyleStrategy horizontalCellStyleStrategy =
                    new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
            EasyExcel.write(new BufferedOutputStream(response.getOutputStream()), TaskManageExcelDto.class)
                    .registerWriteHandler(horizontalCellStyleStrategy)
                    .registerWriteHandler(new ConditionMergeStrategy<>(
                            taskManageExcelDtos,
                            new int[]{0, 1, 2},  // 合并前两列
                            TaskManageExcelDto::getId, // 动态获取ID
                            1
                    ))
                    .excelType(ExcelTypeEnum.XLSX)
                    // 是否自动关闭输入流
                    .autoCloseStream(Boolean.TRUE).sheet("审批进度报表").doWrite(taskManageExcelDtos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<TaskManageExcelDto> handleData(List<FlowTask> records) {
        List<TaskManageExcelDto> res = new ArrayList<>();
        // 填充工作流任务使用的设计
        flowTaskService.fillBatchTaskDesignBody(records);
        // 未结束的任务id
        List<String> taskIds = records.stream()
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
                    .collect(Collectors.groupingBy(FlowTaskNode::getFlowTaskId, Collectors.mapping(FlowTaskNode::getNodeId, Collectors.toList())));
            // 查询任务节点待处理人
            pendingTaskPersonMap = ObjectNull.isNull(taskIds) ? Collections.emptyMap() :
                    flowTaskPersonService.listPerson(taskIds)
                            .stream()
                            .filter(p -> !ProcessStatusEnum.PROCESSED.equals(p.getProcessStatus()))
                            .collect(Collectors.groupingBy(FlowTaskPerson::getFlowTaskId));
        }
        for (FlowTask task : records) {
            LinkedList<CourseDto> courses = task.getCourses();
            for (int i = 0; i < courses.size(); i++) {
                CourseDto course = courses.get(i);
                int finalI = i;
                course.getApproveResultDtos().forEach(approveResultDto -> {
                    TaskManageExcelDto excel = BeanCopyUtil.copy(task, TaskManageExcelDto.class);
                    excel.setId(task.getId());
                    excel.setNodeName(course.getNodeName());
                    excel.setUserName(approveResultDto.getUserName());
                    ApproveOpinionDto opinion = approveResultDto.getOpinion();
                    excel.setContent(opinion != null ? opinion.getContent() : "");
                    excel.setTime(DateUtil.parse(approveResultDto.getTime(), DateUtil.PATTERN_DATETIME));
                    if (finalI == 0) {
                        excel.setArrivalTime(DateUtil.parse(approveResultDto.getTime(), DateUtil.PATTERN_DATETIME));
                    } else {
                        excel.setArrivalTime(DateUtil.parse(courses.get(finalI - 1).getTime(), DateUtil.PATTERN_DATETIME));
                    }
                    excel.setHandleTime(DateUtil.between(excel.getArrivalTime(), excel.getTime()).toHours());
                    NodeOperationTypeEnum nodeOperationTypeEnum = approveResultDto.getNodeOperationTypeEnum();
                    excel.setNodeOperation(nodeOperationTypeEnum != null ? nodeOperationTypeEnum.getName() : "");
                    res.add(excel);
                });
            }

            List<String> taskNodeIds = taskNodeIdMap.get(task.getId());
            if (ObjectNull.isNull(taskNodeIds)) {
                continue;
            }
            StringBuilder currentNodeName = new StringBuilder();
            List<FlowTaskManageNodeDto> nodes = new ArrayList<>();
            List<Node> designNodes = FlowUtil.getNodesByNodeType(task.getDesignBody(), Arrays.asList(NodeTypeEnum.SP, NodeTypeEnum.ROOT));
            for (Node node : designNodes) {
                if (!taskNodeIds.contains(node.getId())) {
                    continue;
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
            TaskManageExcelDto excel = BeanCopyUtil.copy(task, TaskManageExcelDto.class);
            excel.setId(task.getId());
            excel.setNodeName(currentNodeName.deleteCharAt(currentNodeName.length() - 1).toString());
            excel.setArrivalTime(Date.from(task.getUpdateTime().atZone(ZoneId.systemDefault()).toInstant()));
            excel.setUserName(!nodes.isEmpty() ? nodes.get(0).getUsers().stream().map(UserDto::getRealName).collect(Collectors.joining(",")) : "");
            res.add(excel);
        }
        return res;
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
