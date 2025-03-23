package cn.bctools.design.workflow.service.impl;

import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.workflow.dto.ApprovalRecordFieldDto;
import cn.bctools.design.workflow.dto.FlowDesignNodeDto;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.dto.ApproveResultDto;
import cn.bctools.design.workflow.entity.dto.CourseDto;
import cn.bctools.design.workflow.enums.NodeOperationTypeEnum;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import cn.bctools.design.workflow.service.ApprovalRecordService;
import cn.bctools.design.workflow.service.FlowDesignService;
import cn.bctools.design.workflow.service.FlowTaskPathService;
import cn.bctools.design.workflow.service.FlowTaskService;
import cn.bctools.design.workflow.utils.FlowUtil;
import cn.bctools.oss.dto.BaseFile;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author jvs
 * 审批记录服务
 */
@Slf4j
@Service
@AllArgsConstructor
public class ApprovalRecordServiceImpl implements ApprovalRecordService {

    private final FlowDesignService flowDesignService;
    private final FlowTaskService flowTaskService;
    private final FlowTaskPathService flowTaskPathService;

    @Override
    public List<ApprovalRecordFieldDto> getApprovalRecordFields(String flowDesignId) {
        if (ObjectNull.isNull(flowDesignId)) {
            log.warn("流程设计id为空");
            return Collections.emptyList();
        }
        return flowDesignService.getNodesById(flowDesignId, Collections.singletonList(NodeTypeEnum.SP))
                .stream()
                .flatMap(node -> Stream.of(
                                // 审批人
                                buildApprovalNodeField(node, ApprovalFieldEnum.USER_NAME),
                                // 审批时间
                                buildApprovalNodeField(node, ApprovalFieldEnum.TIME),
                                // 审批意见
                                buildApprovalNodeField(node, ApprovalFieldEnum.OPINION_CONTENT),
                                // 审批信息
                                buildApprovalNodeField(node, ApprovalFieldEnum.APPROVAL_INFO),
                                // 手写签名
                                buildApprovalNodeField(node, ApprovalFieldEnum.SIGN)
                        )
                )
                .collect(Collectors.toList());
    }


    @Override
    public Map<String, Object> getFlowRecord(String dataId) {
        if (ObjectNull.isNull(dataId)) {
            return Collections.emptyMap();
        }
        // 查询数据最后一个流程任务
        FlowTask flowTask = flowTaskService.getOne(Wrappers.<FlowTask>lambdaQuery()
                .eq(FlowTask::getDataId, dataId)
                .orderByDesc(FlowTask::getCreateTime)
                .last("limit 1"));
        // 转换流程审批过程为审批记录
        return convertTaskCourseToRecord(flowTask);
    }

    @Override
    public Map<String, Object> getFlowRecord(String dataId, String flowDesignId) {
        if (ObjectNull.isNull(dataId) || ObjectNull.isNull(flowDesignId)) {
            return Collections.emptyMap();
        }
        // 查询数据指定流程的最后一个流程任务
        FlowTask flowTask = flowTaskService.getOne(Wrappers.<FlowTask>lambdaQuery()
                .eq(FlowTask::getDataId, dataId)
                .eq(FlowTask::getFlowDesignId, flowDesignId)
                .orderByDesc(FlowTask::getCreateTime)
                .last("limit 1"));
        // 转换流程审批过程为审批记录
        return convertTaskCourseToRecord(flowTask);
    }

    /**
     * 构造审批节点记录字段
     *
     * @param node 节点
     * @param approvalFieldEnum 审批字段枚举
     * @return 审批节点字段
     */
    private ApprovalRecordFieldDto buildApprovalNodeField(FlowDesignNodeDto node, ApprovalFieldEnum approvalFieldEnum) {
        String fieldKey = buildApprovalNodeFieldKey(node.getId(), approvalFieldEnum);
        String fieldName = node.getName() + StringPool.DOT + approvalFieldEnum.getName();
        return new ApprovalRecordFieldDto()
                .setFieldKey(fieldKey)
                .setFieldName(fieldName)
                .setDataFieldType(approvalFieldEnum.getDataFieldType());
    }

    /**
     * 构造审批节点记录字段key
     *
     * @param nodeId 节点id
     * @param approvalFieldEnum 审批字段枚举
     * @return 审批节点字段
     */
    private String buildApprovalNodeFieldKey(String nodeId, ApprovalFieldEnum approvalFieldEnum) {
        return approvalFieldEnum.getKey() + nodeId;
    }


    /**
     * 转换流程审批过程为审批记录
     *
     * @param flowTask 流程实例
     * @return key：对应"流程设计审批记录字段"中的fieldKey， value：记录数据
     */
    private Map<String, Object> convertTaskCourseToRecord(FlowTask flowTask) {
        if (ObjectNull.isNull(flowTask) || ObjectNull.isNull(flowTask.getCourses())) {
            return Collections.emptyMap();
        }
        // 填充流程设计
        flowTaskService.fillTaskDesignBody(flowTask);
        // 获取流程任务可用设计
        List<List<Node>> nodePaths = flowTaskPathService.getNodePaths(flowTask);

        // 解析并转换流程审批记录
        Map<String, Object> recordMap = new HashMap<>();
        // 已转换 和 不需要转换的节点id
        Set<String> excludedIds  = new HashSet<>();
        LinkedList<CourseDto> courses = flowTask.getCourses();
        ListIterator<CourseDto> listIterator = courses.listIterator(courses.size());
        while (listIterator.hasPrevious()) {
            CourseDto courseDto = listIterator.previous();
            // 以开始节点为界限，得到最后一个开始节点之后的所有审批人(因为可能重启过流程)
            // 回退到发起人节点，倒序的第一个节点就是ROOT，需要跳过
            if (NodeTypeEnum.ROOT.equals(courseDto.getNodeType())) {
                break;
            }
            String nodeId = courseDto.getNodeId();

            // 若是回退操作，找到回退目标节点与当前节点之间的所有节点id，这部分节点的审批记录不转换
            List<String> backBetweenNodeIds = FlowUtil.getBackSubListBetween(courseDto, nodePaths);
            if (ObjectNull.isNotNull(backBetweenNodeIds)) {
                excludedIds.addAll(backBetweenNodeIds);
            }
            // 跳过已转换或不需要转换的节点
            if (excludedIds.contains(nodeId)) {
                continue;
            }
            excludedIds.add(nodeId);
            // 审批人
            convertRecordUserName(recordMap, courseDto);
            // 审批时间
            convertRecordTime(recordMap, courseDto);
            // 审批意见
            convertRecordOpinion(recordMap, courseDto);
            // 审批信息
            convertRecordApprovalInfo(recordMap, courseDto);
            // 手写签名
            convertRecordSign(recordMap, courseDto);
        }
        return recordMap;
    }

    /**
     * 转换审批记录——审批人
     *
     * @param recordMap 转换后的审批记录
     * @param course 审批记录
     */
    private void convertRecordUserName(Map<String, Object> recordMap, CourseDto course) {
        String key = buildApprovalNodeFieldKey(course.getNodeId(), ApprovalFieldEnum.USER_NAME);
        String data = filterApproveResult(course).stream().map(ApproveResultDto::getUserName).collect(Collectors.joining(" "));
        recordMap.put(key, data);
    }

    /**
     * 转换审批记录——审批意见
     *
     * @param recordMap 转换后的审批记录
     * @param course 审批记录
     */
    private void convertRecordOpinion(Map<String, Object> recordMap, CourseDto course) {
        String key = buildApprovalNodeFieldKey(course.getNodeId(), ApprovalFieldEnum.OPINION_CONTENT);
        String data = filterApproveResult(course).stream().map(re -> re.getOpinion().getContent()).collect(Collectors.joining(" "));
        recordMap.put(key, data);
    }

    /**
     * 转换审批记录——审批时间
     *
     * @param recordMap 转换后的审批记录
     * @param course 审批记录
     */
    private void convertRecordTime(Map<String, Object> recordMap, CourseDto course) {
        String key = buildApprovalNodeFieldKey(course.getNodeId(), ApprovalFieldEnum.TIME);
        String data = filterApproveResult(course).stream().map(ApproveResultDto::getTime).collect(Collectors.joining(" "));
        recordMap.put(key, data);
    }

    /**
     * 转换审批记录——审批信息
     *
     * @param recordMap 转换后的审批记录
     * @param course 审批记录
     */
    private void convertRecordApprovalInfo(Map<String, Object> recordMap, CourseDto course) {
        String key = buildApprovalNodeFieldKey(course.getNodeId(), ApprovalFieldEnum.APPROVAL_INFO);
        String data = filterApproveResult(course).stream()
                .map(approve -> "审批人：" + approve.getUserName() + "\n" + "审批意见：" +  approve.getOpinion().getContent() + "\n" + "审批时间：" + approve.getTime())
                .collect(Collectors.joining("\n"));
        recordMap.put(key, data);
    }

    /**
     * 转换审批记录——手写签名
     *
     * @param recordMap 转换后的审批记录
     * @param course 审批记录
     */
    private void convertRecordSign(Map<String, Object> recordMap, CourseDto course) {
        String key = buildApprovalNodeFieldKey(course.getNodeId(), ApprovalFieldEnum.SIGN);
        List<BaseFile> data = filterApproveResult(course).stream()
                .map(re -> re.getOpinion().getSign())
                .filter(ObjectNull::isNotNull)
                .flatMap(Collection::stream)
                .map(sign -> BeanCopyUtil.copy(sign, BaseFile.class))
                .collect(Collectors.toList());
        recordMap.put(key, data);
    }

    /**
     * 筛选可转换的记录
     *
     * @param course 审批记录
     * @return 可转换的记录
     */
    private List<ApproveResultDto> filterApproveResult(CourseDto course) {
        LinkedList<ApproveResultDto> approveResultList = new LinkedList<>();
        // 用于存储已转换记录的用户id（一个用户在一个节点下，只转换最后一次操作记录）
        Set<String> excludedUserIds = new HashSet<>();
        ListIterator<ApproveResultDto> listIterator = course.getApproveResultDtos().listIterator(course.getApproveResultDtos().size());
        while (listIterator.hasPrevious()) {
            ApproveResultDto approveResult = listIterator.previous();
            // 筛选可以转换的记录（只要同意，拒绝操作的审批结果）
            boolean condition = NodeOperationTypeEnum.PASS.equals(approveResult.getNodeOperationTypeEnum())
                    || NodeOperationTypeEnum.REFUSE.equals(approveResult.getNodeOperationTypeEnum());
            // 可以转换的记录，且未转换过，才可以处理
            if (condition && !excludedUserIds.contains(approveResult.getUserId())) {
                approveResultList.addFirst(approveResult);
                excludedUserIds.add(approveResult.getUserId());
            }
        }
        return approveResultList;
    }

    @Getter
    public enum ApprovalFieldEnum {
        /**
         * 审批信息
         */
        APPROVAL_INFO("Info", "审批信息", DataFieldType.input),
        /**
         * 审批人
         */
        USER_NAME("User", "审批人", DataFieldType.input),
        /**
         * 审批意见
         */
        OPINION_CONTENT("Opinion", "审批意见", DataFieldType.input),
        /**
         * 审批时间
         */
        TIME("Time", "审批时间", DataFieldType.input),
        /**
         * 手写签名
         */
        SIGN("Sign", "手写签名", DataFieldType.signature),
        ;

        private final String key;
        private final String name;
        private final DataFieldType dataFieldType;

        ApprovalFieldEnum(String key, String name, DataFieldType dataFieldType) {
            this.key = key;
            this.name = name;
            this.dataFieldType = dataFieldType;
        }
    }
}
