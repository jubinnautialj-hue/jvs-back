package cn.bctools.design.workflow.service;

import cn.bctools.common.entity.dto.DeptDto;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.*;
import cn.bctools.common.utils.TenantContextHolder;

import cn.bctools.design.project.entity.enums.AppVersionTypeEnum;
import cn.bctools.design.use.UseComponent;
import cn.bctools.design.util.ConditionMergeStrategy;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.design.util.DateUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.bctools.design.workflow.dto.FlowTaskManageNodeDto;
import cn.bctools.design.workflow.dto.manage.TaskManageExcelDto;
import cn.bctools.design.workflow.dto.manage.TaskManagePageDto;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.FlowTaskNode;
import cn.bctools.design.workflow.entity.FlowTaskPerson;
import cn.bctools.design.workflow.entity.dto.ApproveOpinionDto;
import cn.bctools.design.workflow.entity.dto.CourseDto;
import cn.bctools.design.workflow.entity.enums.*;
import cn.bctools.design.workflow.enums.NodeOperationTypeEnum;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import cn.bctools.design.workflow.utils.FlowUtil;
import cn.bctools.oauth2.utils.AuthorityManagementUtils;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.web.utils.IpUtil;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 工作流任务管理异步导出服务 - Mgr模块实现
 * 
 * @author BCTools
 */
@Slf4j
@Service
public class FlowTaskManageAsyncExportService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private FlowTaskService flowTaskService;

    @Autowired
    private FlowTaskNodeService flowTaskNodeService;

    @Autowired
    private FlowTaskPersonService flowTaskPersonService;

    @Autowired
    private UseComponent useComponent;

    /**
     * 异步导出任务管理数据
     *
     * @param params 查询参数
     * @param taskId 任务ID
     * @param currentUser 当前用户信息
     */
    @Async("exportExecutor")
    public void asyncExport(TaskManagePageDto params, String taskId, UserDto currentUser) {
        log.info("开始异步导出任务: {}, 参数: {}", taskId, params);
        updateProgress(taskId, 0, "开始导出");

        try {
            // 1. 查询数据
            updateProgress(taskId, 10, "查询任务数据...");
            List<FlowTask> records = queryFlowTasks(params, currentUser);

            if (records.isEmpty()) {
                log.info("导出任务 {} 没有数据", taskId);
                saveEmptyResult(taskId);
                return;
            }

            log.info("导出任务 {} 查询到 {} 条记录", taskId, records.size());

            // 2. 处理数据
            updateProgress(taskId, 30, "处理数据中...");
            List<TaskManageExcelDto> excelData = handleData(records);

            // 3. 生成Excel
            updateProgress(taskId, 70, "生成Excel文件...");
            byte[] excelBytes = generateExcel(excelData);

            // 4. 保存结果
            updateProgress(taskId, 90, "保存结果...");
            saveExportResult(taskId, excelBytes);

            updateProgress(taskId, 100, "导出完成");
            log.info("异步导出任务 {} 完成,文件大小: {} bytes", taskId, excelBytes.length);

        } catch (Exception e) {
            String errorMsg = "导出失败: " + e.getClass().getSimpleName() + ": " + (e.getMessage() != null ? e.getMessage() : "未知错误");
            log.error("异步导出任务 {} 失败: {}", taskId, errorMsg, e);
            saveErrorResult(taskId, errorMsg);
        }
    }

    /**
     * 查询工作流任务数据
     */
    private List<FlowTask> queryFlowTasks(TaskManagePageDto dto, UserDto currentUser) {
        if (ObjectNull.isNull(currentUser)) {
            return Collections.emptyList();
        }

        // 获取模式
        AppVersionTypeEnum mode = ObjectNull.isNotNull(dto.getMode()) ? dto.getMode() : ModeUtils.getMode();
        // 获取该用户该模式下的所有应用
        List<Tree<Object>> tree;
        try {
            tree = useComponent.menu("", currentUser.getId(), IpUtil.isMobile(), mode, null).getKey();
            if (tree == null || tree.isEmpty()) {
                log.warn("获取用户应用菜单失败,用户ID: {}", currentUser.getId());
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("获取用户应用菜单失败,用户ID: {}", currentUser.getId(), e);
            return Collections.emptyList();
        }
        
        List<String> appIds = tree.stream()
                .map(x -> String.valueOf(x.getId()))
                .collect(Collectors.toList());

        if (appIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 组装查询条件
        LambdaQueryWrapper<FlowTask> wrapper = Wrappers.<FlowTask>lambdaQuery()
                .and(wa -> wa.eq(FlowTask::getTaskStatus, FlowTaskStatusEnum.PENDING)
                        .or()
                        .eq(FlowTask::getTaskStatus, FlowTaskStatusEnum.PASSED))
                .in(FlowTask::getJvsAppId, appIds)
                .like(ObjectNull.isNotNull(dto.getTaskCode()), FlowTask::getTaskCode, dto.getTaskCode())
                .like(ObjectNull.isNotNull(dto.getFlowName()), FlowTask::getName, dto.getFlowName())
                .like(ObjectNull.isNotNull(dto.getTitle()), FlowTask::getTitle, dto.getTitle())
                .like(ObjectNull.isNotNull(dto.getJvsAppId()), FlowTask::getJvsAppId, dto.getJvsAppId())
                .like(ObjectNull.isNotNull(dto.getTaskStatus()), FlowTask::getTaskStatus, dto.getTaskStatus())
                .orderByDesc(FlowTask::getCreateTime);

        return flowTaskService.list(wrapper);
    }

    /**
     * 处理导出数据
     */
    private List<TaskManageExcelDto> handleData(List<FlowTask> records) {
        List<TaskManageExcelDto> res = new ArrayList<>();

        // 填充工作流任务使用的设计
        flowTaskService.fillBatchTaskDesignBody(records);

        // 未结束的任务id
        List<String> taskIds = records.stream()
                .filter(flowTask -> FlowTaskStatusEnum.PENDING.equals(flowTask.getTaskStatus()))
                .map(FlowTask::getId)
                .collect(Collectors.toList());

        // Map<任务id, 节点id集合>
        Map<String, List<String>> taskNodeIdMap = new HashMap<>();
        // Map<任务id, 待审批人>
        Map<String, List<FlowTaskPerson>> pendingTaskPersonMap = new HashMap<>();

        if (ObjectNull.isNotNull(taskIds)) {
            // 获取待办节点
            taskNodeIdMap = Optional.ofNullable(flowTaskNodeService.getCurrentNodeByTaskIds(taskIds))
                    .orElseGet(ArrayList::new)
                    .stream()
                    .collect(Collectors.groupingBy(
                            FlowTaskNode::getFlowTaskId,
                            Collectors.mapping(FlowTaskNode::getNodeId, Collectors.toList())
                    ));

            // 查询任务节点待处理人
            pendingTaskPersonMap = flowTaskPersonService.listPerson(taskIds)
                    .stream()
                    .filter(p -> !ProcessStatusEnum.PROCESSED.equals(p.getProcessStatus()))
                    .collect(Collectors.groupingBy(FlowTaskPerson::getFlowTaskId));
        }

        // 批量查询所有创建人部门信息以优化性能
        Set<String> userIds = records.stream()
                .map(FlowTask::getCreateById)
                .collect(Collectors.toSet());
        String tenantId = TenantContextHolder.getTenantId();
        List<UserDto> userDeptList = AuthorityManagementUtils.getUserDeptInfoByIds(new ArrayList<>(userIds), tenantId);
        Map<String, String> userDeptMap = userDeptList.stream()
                .collect(Collectors.toMap(
                        UserDto::getId,
                        user -> user.getDept().stream()
                                .map(DeptDto::getDeptName)
                                .collect(Collectors.joining(","))
                ));
        
        for (FlowTask task : records) {
            String deptName = userDeptMap.getOrDefault(task.getCreateById(), "");

            LinkedList<CourseDto> courses = task.getCourses();
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

                    if (ObjectNull.isNotNull(excel.getArrivalTime()) && ObjectNull.isNotNull(excel.getTime())) {
                        excel.setHandleTime(
                                DateUtil.between(
                                        DateUtil.parse(excel.getArrivalTime(), DateUtil.PATTERN_DATETIME),
                                        DateUtil.parse(excel.getTime(), DateUtil.PATTERN_DATETIME)
                                ).toHours()
                        );
                    }

                    NodeOperationTypeEnum nodeOperationTypeEnum = approveResultDto.getNodeOperationTypeEnum();
                    excel.setNodeOperation(nodeOperationTypeEnum != null ? nodeOperationTypeEnum.getName() : "");
                    excel.setTaskStatusName(task.getTaskStatus().getValue() == 1 ? "待审批" : "已通过");
                    res.add(excel);
                });
            }

            List<String> taskNodeIds = taskNodeIdMap.get(task.getId());
            if (ObjectNull.isNull(taskNodeIds)) {
                continue;
            }

            StringBuilder currentNodeName = new StringBuilder();
            List<FlowTaskManageNodeDto> nodes = new ArrayList<>();
            List<Node> designNodes = FlowUtil.getNodesByNodeType(
                    task.getDesignBody(),
                    Arrays.asList(NodeTypeEnum.SP, NodeTypeEnum.ROOT)
            );

            for (Node node : designNodes) {
                if (!taskNodeIds.contains(node.getId())) {
                    continue;
                }

                currentNodeName.append(node.getName()).append(",");
                List<UserDto> users = Optional.ofNullable(pendingTaskPersonMap.get(task.getId()))
                        .orElseGet(ArrayList::new)
                        .stream()
                        .filter(flowTaskPerson -> flowTaskPerson.getNodeId().equals(node.getId()))
                        .map(flowTaskPerson -> new UserDto()
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
            excel.setArrivalTime(DateUtil.format(task.getUpdateTime(), DateUtil.PATTERN_DATETIME));
            excel.setUserName(!nodes.isEmpty() 
                    ? nodes.get(0).getUsers().stream()
                            .map(UserDto::getRealName)
                            .collect(Collectors.joining(",")) 
                    : "");
            excel.setTaskStatusName(task.getTaskStatus().getValue() == 1 ? "待审批" : "已通过");
            res.add(excel);
        }

        return res;
    }

    /**
     * 生成Excel文件
     */
    private byte[] generateExcel(List<TaskManageExcelDto> data) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

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

            HorizontalCellStyleStrategy horizontalCellStyleStrategy =
                    new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);

            EasyExcel.write(outputStream, TaskManageExcelDto.class)
                    .registerWriteHandler(horizontalCellStyleStrategy)
                    .registerWriteHandler(new ConditionMergeStrategy<>(
                            data,
                            new int[]{0, 1, 2, 3, 4},  // 合并前两列
                            TaskManageExcelDto::getId,
                            1
                    ))
                    .excelType(ExcelTypeEnum.XLSX)
                    .autoCloseStream(Boolean.TRUE)
                    .sheet("审批进度报表")
                    .doWrite(data);

            return outputStream.toByteArray();
        } finally {
            outputStream.close();
        }
    }

    /**
     * 更新导出进度
     */
    private void updateProgress(String taskId, int progress, String message) {
        Map<String, Object> progressInfo = new HashMap<>();
        progressInfo.put("progress", progress);
        progressInfo.put("message", message);
        progressInfo.put("timestamp", System.currentTimeMillis());

        redisTemplate.opsForHash().putAll("export:progress:" + taskId, progressInfo);
        redisTemplate.expire("export:progress:" + taskId, 30, TimeUnit.MINUTES);
    }

    /**
     * 保存导出结果
     */
    private void saveExportResult(String taskId, byte[] data) {
        // 保存文件数据
        redisTemplate.opsForValue().set("export:data:" + taskId, data, 30, TimeUnit.MINUTES);

        // 保存元信息
        Map<String, Object> metaInfo = new HashMap<>();
        metaInfo.put("status", "completed");
        metaInfo.put("fileSize", data.length);
        metaInfo.put("completedTime", System.currentTimeMillis());

        redisTemplate.opsForHash().putAll("export:meta:" + taskId, metaInfo);
        redisTemplate.expire("export:meta:" + taskId, 30, TimeUnit.MINUTES);
    }

    /**
     * 保存空结果
     */
    private void saveEmptyResult(String taskId) {
        Map<String, Object> metaInfo = new HashMap<>();
        metaInfo.put("status", "empty");
        metaInfo.put("message", "暂无数据可导出");
        metaInfo.put("completedTime", System.currentTimeMillis());

        redisTemplate.opsForHash().putAll("export:meta:" + taskId, metaInfo);
        redisTemplate.expire("export:meta:" + taskId, 30, TimeUnit.MINUTES);
    }

    /**
     * 保存错误信息
     */
    private void saveErrorResult(String taskId, String errorMessage) {
        Map<String, Object> metaInfo = new HashMap<>();
        metaInfo.put("status", "error");
        metaInfo.put("errorMessage", errorMessage);
        metaInfo.put("completedTime", System.currentTimeMillis());

        redisTemplate.opsForHash().putAll("export:meta:" + taskId, metaInfo);
        redisTemplate.expire("export:meta:" + taskId, 30, TimeUnit.MINUTES);
    }

    /**
     * 获取导出进度
     */
    public Map<String, Object> getExportProgress(String taskId) {
        Map<Object, Object> progress = redisTemplate.opsForHash().entries("export:progress:" + taskId);
        Map<Object, Object> meta = redisTemplate.opsForHash().entries("export:meta:" + taskId);

        Map<String, Object> result = new HashMap<>();
        progress.forEach((k, v) -> result.put(String.valueOf(k), v));
        meta.forEach((k, v) -> result.put(String.valueOf(k), v));
        return result;
    }

    /**
     * 获取导出结果
     */
    public byte[] getExportResult(String taskId) {
        return (byte[]) redisTemplate.opsForValue().get("export:data:" + taskId);
    }

    /**
     * 检查导出任务是否存在
     */
    public boolean isTaskExists(String taskId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("export:meta:" + taskId));
    }
}
