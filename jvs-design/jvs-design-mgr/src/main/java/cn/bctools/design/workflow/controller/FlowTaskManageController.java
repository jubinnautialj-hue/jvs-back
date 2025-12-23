package cn.bctools.design.workflow.controller;

import cn.bctools.common.entity.dto.DeptDto;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
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
import cn.bctools.oauth2.utils.AuthorityManagementUtils;
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
    private final UseComponent useComponent;

    @Log
    @ApiOperation("流程任务分页")
    @GetMapping("/page")
    public R<Page<PageFlowTaskManageResDto>> manageTaskPage(Page<FlowTask> page, TaskManagePageDto dto) {
        long totalStart = System.currentTimeMillis();
        log.info("工作流任务分页查询开始，参数: current={}, size={}, taskStatus={}, jvsAppId={}",
                page.getCurrent(), page.getSize(), dto.getTaskStatus(), dto.getJvsAppId());

        // 查询当前模式应用id集合
        long menuStart = System.currentTimeMillis();
        AppVersionTypeEnum mode = ObjectNull.isNotNull(dto.getMode()) ? dto.getMode() : ModeUtils.getMode();
        //获取该用户该模式下的所有应用
        List<Tree<Object>> tree = useComponent.menu("", ModeUtils.getRealUser().getId(), IpUtil.isMobile(), mode, null).getKey();
        List<String> appIds = tree.stream().map(x -> String.valueOf(x.getId())).collect(Collectors.toList());
        log.info("查询用户应用菜单耗时: {}ms", System.currentTimeMillis() - menuStart);

        if (CollectionUtils.isEmpty(appIds)) {
            log.info("用户没有可访问的应用，返回空结果");
            return R.ok();
        }

        // 构建查询条件
        long queryStart = System.currentTimeMillis();
        LambdaQueryWrapper<FlowTask> wrapper = Wrappers.<FlowTask>lambdaQuery()
                .and(wa ->
                        wa.eq(FlowTask::getTaskStatus, FlowTaskStatusEnum.PENDING)
                                .or().eq(FlowTask::getTaskStatus, FlowTaskStatusEnum.PASSED))
                .in(FlowTask::getJvsAppId, appIds)
                .like(ObjectNull.isNotNull(dto.getTaskCode()), FlowTask::getTaskCode, dto.getTaskCode())
                .like(ObjectNull.isNotNull(dto.getFlowName()), FlowTask::getName, dto.getFlowName())
                .like(ObjectNull.isNotNull(dto.getTitle()), FlowTask::getTitle, dto.getTitle())
                .like(ObjectNull.isNotNull(dto.getJvsAppId()), FlowTask::getJvsAppId, dto.getJvsAppId())
                .like(ObjectNull.isNotNull(dto.getTaskStatus()), FlowTask::getTaskStatus, dto.getTaskStatus())
                .orderByDesc(FlowTask::getCreateTime);
        log.info("构建查询条件耗时: {}ms", System.currentTimeMillis() - queryStart);

        // 执行分页查询
        long pageStart = System.currentTimeMillis();
        flowTaskService.page(page, wrapper);
        log.info("分页查询耗时: {}ms, 返回记录数: {}", System.currentTimeMillis() - pageStart, page.getRecords().size());

        // 填充工作流任务使用的设计
        long fillStart = System.currentTimeMillis();
        flowTaskService.fillBatchTaskDesignBody(page.getRecords());
        log.info("填充工作流设计耗时: {}ms", System.currentTimeMillis() - fillStart);

        // 转换结果
        Page<PageFlowTaskManageResDto> pageDto = new Page<>(page.getCurrent(), page.getSize());
        if (ObjectNull.isNull(page.getRecords())) {
            log.info("查询结果为空，返回空分页");
            return R.ok(pageDto);
        }

        // 填充数据
        long convertStart = System.currentTimeMillis();
        List<PageFlowTaskManageResDto> resultList = BeanCopyUtil.copys(page.getRecords(), PageFlowTaskManageResDto.class);
        resultList.forEach(task -> {
            long userStart = System.currentTimeMillis();
            UserDto userById = AuthorityManagementUtils.getUserById(task.getCreateById());
            task.setCreateDeptName(userById.getDept().stream().map(DeptDto::getDeptName).collect(Collectors.joining(",")));
            log.debug("查询用户信息耗时: {}ms, 用户ID: {}", System.currentTimeMillis() - userStart, task.getCreateById());
        });
        log.info("转换结果集耗时: {}ms", System.currentTimeMillis() - convertStart);

        pageDto.setRecords(resultList);
        pageDto.setTotal(page.getTotal());

        // 处理待办任务节点信息
        long pendingStart = System.currentTimeMillis();
        // 未结束的任务id
        List<String> taskIds = page.getRecords().stream()
                .filter(flowTask -> FlowTaskStatusEnum.PENDING.equals(flowTask.getTaskStatus()))
                .map(FlowTask::getId).collect(Collectors.toList());
        log.info("待办任务数量: {}", taskIds.size());

        // Map<任务id, 节点id集合>
        Map<String, List<String>> taskNodeIdMap = new HashMap<>();
        // Map<任务id, 待审批人>
        Map<String, List<FlowTaskPerson>> pendingTaskPersonMap = new HashMap<>();
        if (ObjectNull.isNotNull(taskIds) && !taskIds.isEmpty()) {
            // 获取待办节点
            long nodeStart = System.currentTimeMillis();
            taskNodeIdMap = Optional.ofNullable(flowTaskNodeService.getCurrentNodeByTaskIds(taskIds))
                    .orElseGet(ArrayList::new)
                    .stream()
                    .collect(Collectors.groupingBy(FlowTaskNode::getFlowTaskId, Collectors.mapping(FlowTaskNode::getNodeId, Collectors.toList())));
            log.info("查询待办节点耗时: {}ms", System.currentTimeMillis() - nodeStart);

            // 查询任务节点待处理人
            long personStart = System.currentTimeMillis();
            pendingTaskPersonMap = ObjectNull.isNull(taskIds) ? Collections.emptyMap() :
                    flowTaskPersonService.listPerson(taskIds)
                            .stream()
                            .filter(p -> !ProcessStatusEnum.PROCESSED.equals(p.getProcessStatus()))
                            .collect(Collectors.groupingBy(FlowTaskPerson::getFlowTaskId));
            log.info("查询待处理人耗时: {}ms", System.currentTimeMillis() - personStart);
        }

        // 构建返回结果
        long buildStart = System.currentTimeMillis();
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
        log.info("构建返回结果耗时: {}ms", System.currentTimeMillis() - buildStart);

        long totalTime = System.currentTimeMillis() - totalStart;
        log.info("工作流任务分页查询总耗时: {}ms", totalTime);

        return R.ok(pageDto);
    }

    @Log
    @ApiOperation("流程导出")
    @GetMapping("/exportTaskManage")
    public void exportTaskManage(TaskManagePageDto dto, HttpServletResponse response) {
        String traceId = "EXPORT-" + System.currentTimeMillis() + "-" + Thread.currentThread().getId();
        log.info("[{}] 开始执行导出任务，参数: mode={}, taskStatus={}, taskCode={}, flowName={}, title={}, jvsAppId={}",
                traceId, dto.getMode(), dto.getTaskStatus(), dto.getTaskCode(), dto.getFlowName(), dto.getTitle(), dto.getJvsAppId());
        long totalStart = System.currentTimeMillis();

        // 获取模式
        long modeStart = System.currentTimeMillis();
        AppVersionTypeEnum mode = ObjectNull.isNotNull(dto.getMode()) ? dto.getMode() : ModeUtils.getMode();
        log.info("[{}] 获取模式耗时: {}ms", traceId, System.currentTimeMillis() - modeStart);

        //获取该用户该模式下的所有应用
        long menuStart = System.currentTimeMillis();
        List<Tree<Object>> tree = useComponent.menu("", ModeUtils.getRealUser().getId(), IpUtil.isMobile(), mode, null).getKey();
        log.info("[{}] 获取用户应用菜单耗时: {}ms", traceId, System.currentTimeMillis() - menuStart);

        List<String> appIds = tree.stream().map(x -> String.valueOf(x.getId())).collect(Collectors.toList());
        log.info("[{}] 提取应用ID列表耗时: {}ms, 应用数量: {}", traceId, System.currentTimeMillis() - menuStart, appIds.size());

        //组装查询条件
        long queryBuildStart = System.currentTimeMillis();
        LambdaQueryWrapper<FlowTask> wrapper = Wrappers.<FlowTask>lambdaQuery()
                .and(wa ->
                        wa.eq(FlowTask::getTaskStatus, FlowTaskStatusEnum.PENDING)
                                .or()
                                .eq(FlowTask::getTaskStatus, FlowTaskStatusEnum.PASSED))
                .in(FlowTask::getJvsAppId, appIds)
                .like(ObjectNull.isNotNull(dto.getTaskCode()), FlowTask::getTaskCode, dto.getTaskCode())
                .like(ObjectNull.isNotNull(dto.getFlowName()), FlowTask::getName, dto.getFlowName())
                .like(ObjectNull.isNotNull(dto.getTitle()), FlowTask::getTitle, dto.getTitle())
                .like(ObjectNull.isNotNull(dto.getJvsAppId()), FlowTask::getJvsAppId, dto.getJvsAppId())
                .like(ObjectNull.isNotNull(dto.getTaskStatus()), FlowTask::getTaskStatus, dto.getTaskStatus())
                .orderByDesc(FlowTask::getCreateTime);
        log.info("[{}] 构建查询条件耗时: {}ms", traceId, System.currentTimeMillis() - queryBuildStart);

        // 执行查询
        long queryStart = System.currentTimeMillis();
        List<FlowTask> records = flowTaskService.list(wrapper);
        log.info("[{}] 数据库查询耗时: {}ms, 返回记录数: {}", traceId, System.currentTimeMillis() - queryStart, records.size());

        // 处理数据
        long dataProcessStart = System.currentTimeMillis();
        List<TaskManageExcelDto> taskManageExcelDtos = this.handleData(records);
        log.info("[{}] 数据处理耗时: {}ms, 生成Excel数据行数: {}", traceId, System.currentTimeMillis() - dataProcessStart, taskManageExcelDtos.size());

        //将文件名称进行编码
        long responseStart = System.currentTimeMillis();
        response.setContentType("application/ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = URLCoder.encode("审批进度报表.xlsx");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName);
        log.info("[{}] 设置响应头耗时: {}ms", traceId, System.currentTimeMillis() - responseStart);

        try {
            // Excel样式配置
            long styleStart = System.currentTimeMillis();
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
            log.info("[{}] Excel样式配置耗时: {}ms", traceId, System.currentTimeMillis() - styleStart);

            // Excel写入
            long writeStart = System.currentTimeMillis();
            EasyExcel.write(new BufferedOutputStream(response.getOutputStream()), TaskManageExcelDto.class)
                    .registerWriteHandler(horizontalCellStyleStrategy)
                    .registerWriteHandler(new ConditionMergeStrategy<>(
                            taskManageExcelDtos,
                            new int[]{0, 1, 2, 3, 4},  // 合并前两列
                            TaskManageExcelDto::getId, // 动态获取ID
                            1
                    ))
                    .excelType(ExcelTypeEnum.XLSX)
                    // 是否自动关闭输入流
                    .autoCloseStream(Boolean.TRUE).sheet("审批进度报表").doWrite(taskManageExcelDtos);
            log.info("[{}] Excel写入耗时: {}ms", traceId, System.currentTimeMillis() - writeStart);

            log.info("[{}] 导出任务完成，总耗时: {}ms", traceId, System.currentTimeMillis() - totalStart);
        } catch (IOException e) {
            log.error("[{}] 导出任务失败: {}", traceId, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    /**
     * 导出数据处理
     *
     * @param records
     * @return
     */
    private List<TaskManageExcelDto> handleData(List<FlowTask> records) {
        String traceId = "EXPORT-" + System.currentTimeMillis() + "-DATA-" + Thread.currentThread().getId();
        long totalStart = System.currentTimeMillis();
        log.info("[{}] 开始处理导出数据，记录数: {}", traceId, records.size());

        List<TaskManageExcelDto> res = new ArrayList<>();
        // 填充工作流任务使用的设计
        long designStart = System.currentTimeMillis();
        flowTaskService.fillBatchTaskDesignBody(records);
        log.info("[{}] 填充工作流设计耗时: {}ms", traceId, System.currentTimeMillis() - designStart);

        // 未结束的任务id
        long taskIdsStart = System.currentTimeMillis();
        List<String> taskIds = records.stream()
                .filter(flowTask -> FlowTaskStatusEnum.PENDING.equals(flowTask.getTaskStatus()))
                .map(FlowTask::getId).collect(Collectors.toList());
        log.info("[{}] 筛选待办任务ID耗时: {}ms, 待办任务数量: {}", traceId, System.currentTimeMillis() - taskIdsStart, taskIds.size());

        // Map<任务id, 节点id集合>
        Map<String, List<String>> taskNodeIdMap = new HashMap<>();
        // Map<任务id, 待审批人>
        Map<String, List<FlowTaskPerson>> pendingTaskPersonMap = new HashMap<>();
        if (ObjectNull.isNotNull(taskIds)) {
            // 获取待办节点
            long nodeStart = System.currentTimeMillis();
            taskNodeIdMap = Optional.ofNullable(flowTaskNodeService.getCurrentNodeByTaskIds(taskIds))
                    .orElseGet(ArrayList::new)
                    .stream()
                    .collect(Collectors.groupingBy(FlowTaskNode::getFlowTaskId, Collectors.mapping(FlowTaskNode::getNodeId, Collectors.toList())));
            log.info("[{}] 获取待办节点耗时: {}ms", traceId, System.currentTimeMillis() - nodeStart);

            // 查询任务节点待处理人
            long personStart = System.currentTimeMillis();
            pendingTaskPersonMap = ObjectNull.isNull(taskIds) ? Collections.emptyMap() :
                    flowTaskPersonService.listPerson(taskIds)
                            .stream()
                            .filter(p -> !ProcessStatusEnum.PROCESSED.equals(p.getProcessStatus()))
                            .collect(Collectors.groupingBy(FlowTaskPerson::getFlowTaskId));
            log.info("[{}] 查询待处理人耗时: {}ms", traceId, System.currentTimeMillis() - personStart);
        }

        // 处理每条记录
        long processStart = System.currentTimeMillis();
        for (FlowTask task : records) {
            long userStart = System.currentTimeMillis();
            UserDto userById = AuthorityManagementUtils.getUserById(task.getCreateById());
            String deptName = userById.getDept().stream().map(DeptDto::getDeptName).collect(Collectors.joining(","));
            log.debug("[{}] 查询用户信息耗时: {}ms, 用户ID: {}", traceId, System.currentTimeMillis() - userStart, task.getCreateById());

            LinkedList<CourseDto> courses = task.getCourses();
            long coursesStart = System.currentTimeMillis();
            for (int i = 0; i < courses.size(); i++) {
                CourseDto course = courses.get(i);
                int finalI = i;
                course.getApproveResultDtos().forEach(approveResultDto -> {
                    TaskManageExcelDto excel = BeanCopyUtil.copy(task, TaskManageExcelDto.class);
                    excel.setId(task.getId());
                    excel.setNodeName(course.getNodeName());
                    excel.setUserName(approveResultDto.getUserName());
                    excel.setCreateDeptName(deptName);
                    ApproveOpinionDto opinion = approveResultDto.getOpinion();
                    excel.setContent(opinion != null ? opinion.getContent() : "");
                    excel.setTime(approveResultDto.getTime());
                    if (finalI == 0) {
                        excel.setArrivalTime(approveResultDto.getTime());
                    } else {
                        excel.setArrivalTime(courses.get(finalI - 1).getTime());
                    }
                    if (ObjectNull.isNotNull(excel.getArrivalTime()) && ObjectNull.isNotNull(excel.getTime())){
                        excel.setHandleTime(
                                DateUtil.between(
                                        DateUtil.parse(excel.getArrivalTime(),DateUtil.PATTERN_DATETIME), DateUtil.parse(excel.getTime(),DateUtil.PATTERN_DATETIME)
                                ).toHours());
                    }
                    NodeOperationTypeEnum nodeOperationTypeEnum = approveResultDto.getNodeOperationTypeEnum();
                    excel.setNodeOperation(nodeOperationTypeEnum != null ? nodeOperationTypeEnum.getName() : "");
                    excel.setTaskStatusName(task.getTaskStatus().getValue() == 1 ? "待审批" : "已通过");
                    res.add(excel);
                });
            }
            log.debug("[{}] 处理课程数据耗时: {}ms, 课程数量: {}", traceId, System.currentTimeMillis() - coursesStart, courses.size());

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
            excel.setCreateDeptName(deptName);
            excel.setId(task.getId());
            excel.setNodeName(currentNodeName.deleteCharAt(currentNodeName.length() - 1).toString());
            excel.setArrivalTime(DateUtil.format(task.getUpdateTime(),DateUtil.PATTERN_DATETIME));
            excel.setUserName(!nodes.isEmpty() ? nodes.get(0).getUsers().stream().map(UserDto::getRealName).collect(Collectors.joining(",")) : "");
            excel.setTaskStatusName(task.getTaskStatus().getValue() == 1 ? "待审批" : "已通过");
            res.add(excel);
        }
        log.info("[{}] 处理所有记录耗时: {}ms, 生成Excel行数: {}", traceId, System.currentTimeMillis() - processStart, res.size());
        log.info("[{}] handleData总耗时: {}ms", traceId, System.currentTimeMillis() - totalStart);
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