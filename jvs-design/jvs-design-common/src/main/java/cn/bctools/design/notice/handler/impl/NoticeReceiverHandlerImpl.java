package cn.bctools.design.notice.handler.impl;

import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.auth.api.dto.SearchUserDto;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.JvsJsonPath;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.notice.handler.NoticeReceiverHandler;
import cn.bctools.design.notice.handler.bo.ReceiverBo;
import cn.bctools.design.notice.handler.enums.ReceiverSystemFieldEnum;
import cn.bctools.design.notice.handler.enums.ReceiverTypeEnum;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.FlowTaskNode;
import cn.bctools.design.workflow.entity.dto.ApproveResultDto;
import cn.bctools.design.workflow.entity.dto.CourseDto;
import cn.bctools.design.workflow.enums.FlowDataFieldEnum;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import cn.bctools.design.workflow.service.FlowDynamicDataService;
import cn.bctools.design.workflow.service.FlowTaskNodeService;
import cn.bctools.design.workflow.service.FlowTaskPersonService;
import cn.bctools.design.workflow.service.impl.FlowDynamicDataServiceImpl;
import cn.hutool.core.convert.Convert;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.jayway.jsonpath.JsonPath;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 消息通知-获取接收人
 */
@Slf4j
@Component
@AllArgsConstructor
public class NoticeReceiverHandlerImpl implements NoticeReceiverHandler {

    private final AuthUserServiceApi authUserServiceApi;
    private final FlowTaskNodeService flowTaskNodeService;
    private final FlowTaskPersonService flowTaskPersonService;
    private final FlowDynamicDataService flowDynamicDataService;

    @Override
    public List<String> getUserIds(List<ReceiverBo> receiver, Map<String, Object> data, FlowTask flowTask, List<String> taskNodeIds) {
        List<String> userIds = new ArrayList<>();
        List<FlowTaskNode> flowTaskNodes = null;
        if (ObjectNull.isNotNull(flowTask)) {
            // taskNodeIds不为空，则获取指定任务节点集合；否则获取当前任务所有处理中的任务节点
            flowTaskNodes = CollectionUtils.isNotEmpty(taskNodeIds) ? flowTaskNodeService.getTaskNodes(flowTask.getId(), taskNodeIds) : flowTaskNodeService.getCurrentNodesByTaskId(flowTask.getId());
        }

        // 根据用户类型分组
        Map<ReceiverTypeEnum, List<String>> userGroup = receiver.stream()
                .collect(Collectors.groupingBy(ReceiverBo::getType, Collectors.mapping(ReceiverBo::getId, Collectors.toList())));
        for (Map.Entry<ReceiverTypeEnum, List<String>> e : userGroup.entrySet()) {
            switch (e.getKey()) {
                case user:
                    userIds.addAll(e.getValue());
                    break;
                case role:
                    getUserIdByRole(e.getValue(), userIds);
                    break;
                case system_field:
                    getUserIdBySystemField(e.getValue(), data, flowTask, flowTaskNodes, userIds, taskNodeIds);
                    break;
                case model_field:
                    getUserIdByModelField(e.getValue(), data, userIds);
                    break;
                default:
                    log.warn("消息通知，不支持的接收人类型");
                    break;
            }
        }
        return userIds.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 查询角色对应的用户id
     * @param roleIds 节点
     * @return 用户id集合
     */
    private void getUserIdByRole(List<String> roleIds, List<String> userIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return;
        }
        // 查询角色对应的所有人
        SearchUserDto search = new SearchUserDto();
        search.setRoleIds(roleIds);
        List<String> uIds = authUserServiceApi.userSearch(search).getData().stream().map(UserDto::getId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(uIds)) {
            userIds.addAll(uIds);
        }
    }

    /**
     * 查询系统字段对应的用户id
     * @param fields
     * @param data
     * @param flowTask
     * @param flowTaskNodes
     * @param userIds
     * @param taskNodeIds
     */
    private void getUserIdBySystemField(List<String> fields, Map<String, Object> data, FlowTask flowTask, List<FlowTaskNode> flowTaskNodes, List<String> userIds, List<String> taskNodeIds) {
        if (CollectionUtils.isEmpty(fields)) {
            return;
        }
        fields.forEach(field -> {
            if (ReceiverSystemFieldEnum.CREATE_BY_ID.getValue().equals(field)) {
                getSystemCreateById(flowTask, data, userIds);
            } else if (ReceiverSystemFieldEnum.APPROVED.getValue().equals(field)) {
                getSystemApproved(flowTask, flowTaskNodes, userIds);
            } else if (ReceiverSystemFieldEnum.PENDING_APPROVAL.getValue().equals(field)) {
                getSystemPendingApproval(flowTask, flowTaskNodes, userIds, taskNodeIds);
            }
        });
    }

    /**
     * 系统字段-获取发起人id
     * @param flowTask
     * @param data
     * @param userIds
     */
    private void getSystemCreateById(FlowTask flowTask, Map<String, Object> data, List<String> userIds) {
        // 非工作流通知，得到创建数据人id
        if (ObjectNull.isNull(flowTask)) {
            userIds.add(String.valueOf(data.get(ReceiverSystemFieldEnum.CREATE_BY_ID.getValue())));
            return;
        }
        // 工作流通知，得到工作流发起人
        Object taskObj = data.get(FlowDataFieldEnum.TASK.getFieldKey());
        if (ObjectNull.isNotNull(taskObj)) {
            FlowDynamicDataServiceImpl.FlowTaskModelData flowTaskModelData = flowDynamicDataService.getFlowTaskData(String.valueOf(data.get("id")));
            userIds.add(flowTaskModelData.getCreateById());
        }
    }

    /**
     * 系统字段-待审批人id
     *
     * @param flowTask
     * @param userIds
     * @param flowTaskNodes
     * @param taskNodeIds
     */
    private void getSystemPendingApproval(FlowTask flowTask, List<FlowTaskNode> flowTaskNodes, List<String> userIds, List<String> taskNodeIds) {
        if (CollectionUtils.isEmpty(flowTaskNodes)) {
            return;
        }
        List<String> nodeIds = CollectionUtils.isNotEmpty(taskNodeIds) ? taskNodeIds : flowTaskNodes.stream().map(FlowTaskNode::getNodeId).collect(Collectors.toList());
        List<String> pendingApproveUserIds = flowTaskPersonService.getPendingApproveUserIds(flowTask.getId(), nodeIds);
        if (CollectionUtils.isNotEmpty(pendingApproveUserIds)) {
            userIds.addAll(pendingApproveUserIds);
        }
    }

    /**
     * 系统字段-已审批人id
     * @param flowTask
     * @param flowTaskNodes
     * @param userIds
     */
    private void getSystemApproved(FlowTask flowTask, List<FlowTaskNode> flowTaskNodes, List<String> userIds) {
        List<CourseDto> courses = Optional.ofNullable(flowTask.getCourses()).map(course -> Convert.toList(CourseDto.class, course)).orElseGet(LinkedList::new);
        if (ObjectNull.isNotNull(flowTaskNodes)) {
            for (FlowTaskNode taskNode : flowTaskNodes) {
                courses.add(taskNode.getCourse());
            }
        }
        // 逆序遍历集合，得到最后一批审批人（以开始节点为界限，得到最后一个开始节点之后的所有审批人(因为可能重启过流程)）
        ListIterator<CourseDto> listIterator = courses.listIterator(courses.size());
        while (listIterator.hasPrevious()) {
            CourseDto courseDto = listIterator.previous();
            if (NodeTypeEnum.ROOT.equals(courseDto.getNodeType())) {
                break;
            }
            List<String> uIds = courseDto.getApproveResultDtos().stream().map(ApproveResultDto::getUserId).collect(Collectors.toList());
            if (ObjectNull.isNotNull(uIds)) {
                userIds.addAll(uIds);
            }
        }
    }

    /**
     * 从数据中得到用户组件对应的用户id
     * @param fields
     * @param data
     * @param userIds
     */
    private void getUserIdByModelField(List<String> fields, Map<String, Object> data, List<String> userIds) {
        fields.forEach(field -> {
            Object read = JvsJsonPath.read((data), field);
            if (ObjectNull.isNotNull(read)) {
                if (read instanceof JSONArray) {
                    JSONArray jsonArray = (JSONArray) read;
                    jsonArray.stream().map(String::valueOf).forEach(userIds::add);
                } else {
                    userIds.add(String.valueOf(read));
                }
            }
        });
    }

}
