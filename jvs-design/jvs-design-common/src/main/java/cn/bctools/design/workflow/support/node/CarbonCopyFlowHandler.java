package cn.bctools.design.workflow.support.node;


import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.design.workflow.entity.FlowTaskCopied;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.enums.NodePropertiesTypeEnum;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import cn.bctools.design.workflow.model.enums.TargetObjectTypeEnum;
import cn.bctools.design.workflow.model.properties.Target;
import cn.bctools.design.workflow.service.FlowTaskCarbonCopyService;
import cn.bctools.design.workflow.support.function.impl.TaskPersonFunction;
import cn.bctools.design.workflow.support.impl.AbstractFlowHandler;
import cn.bctools.design.workflow.support.FlowResult;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.utils.FlowContextUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 抄送节点处理
 */
@Component
@AllArgsConstructor
public class CarbonCopyFlowHandler extends AbstractFlowHandler {

    private final FlowTaskCarbonCopyService flowTaskCarbonCopyService;
    private final AuthUserServiceApi authUserServiceApi;
    private final TaskPersonFunction taskPersonFunction;

    @Override
    public NodeTypeEnum getType() {
        return NodeTypeEnum.CS;
    }

    @Override
    protected void handle(Node node, RuntimeData runtimeData) {
        // 若不是测试任务，则保存抄送信息。否则不保存抄送信息
        if (Boolean.FALSE.equals(runtimeData.getFlowTask().getTest())) {
            saveCarbonCopy(node, runtimeData);
        }
        FlowContextUtil.refreshContext(new FlowResult().setNextNode(node));
    }

    /**
     * 保存抄送信息
     *
     * @param node 节点
     * @param runtimeData 运行时数据
     */
    private void saveCarbonCopy(Node node, RuntimeData runtimeData) {
        // 保存抄送信息
        List<String> userIds = receiverUserIds(node, runtimeData);
        if (CollectionUtils.isNotEmpty(userIds)) {
            List<FlowTaskCopied> flowTaskCarbonCopies = new ArrayList<>();
            userIds.forEach(uid -> {
                // 保存抄送信息
                FlowTaskCopied flowTaskCarbonCopy = new FlowTaskCopied();
                flowTaskCarbonCopy.setFlowTaskId(runtimeData.getFlowTask().getId());
                flowTaskCarbonCopy.setJvsAppId(runtimeData.getFlowTask().getJvsAppId());
                flowTaskCarbonCopy.setUserId(uid);
                flowTaskCarbonCopies.add(flowTaskCarbonCopy);
            });
            List<String> existsUserIds = flowTaskCarbonCopyService.list(Wrappers.<FlowTaskCopied>lambdaQuery()
                    .eq(FlowTaskCopied::getFlowTaskId, runtimeData.getFlowTask().getId())).stream().map(FlowTaskCopied::getUserId).collect(Collectors.toList());
            List<FlowTaskCopied> newFlowTaskCarbonCopies = flowTaskCarbonCopies.stream().filter(flowTaskCopied -> Boolean.FALSE.equals(existsUserIds.contains(flowTaskCopied.getUserId()))).collect(Collectors.toList());
            if (ObjectNull.isNotNull(newFlowTaskCarbonCopies)) {
                flowTaskCarbonCopyService.saveBatch(newFlowTaskCarbonCopies);
            }
        }
    }

    /**
     * 获得抄送接收人id集合
     * @param currentNode 当前节点
     * @param runtimeData 运行时数据
     * @return 抄送接收人id集合
     */
    private List<String> receiverUserIds(Node currentNode, RuntimeData runtimeData) {
        List<String> userIds = new ArrayList<>();
        NodePropertiesTypeEnum carbonCopyUserType = currentNode.getProps().getType();
        // 兼容旧数据（旧数据抄送节点没有指定抄送人类型，没指定抄送人类型的，默认类型为ASSIGN_CARBON_COPY）
        if (ObjectNull.isNull(carbonCopyUserType) || NodePropertiesTypeEnum.ASSIGN_CARBON_COPY.equals(carbonCopyUserType)) {
            // 处理人/部门等信息
            Target targetObj = currentNode.getProps().getTargetObj();
            getUser(userIds, targetObj);
            getUserByDept(userIds, targetObj);
            getUserByRole(userIds, targetObj);
            getUserByJob(userIds, targetObj);
        } else {
            userIds = taskPersonFunction.invoke(currentNode, runtimeData).stream().map(UserDto::getId).collect(Collectors.toList());
        }

        return userIds.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 获取人员类型为“用户”的用户id集合
     * @param userIds 用户id集合
     * @param targetObj 人员信息
     */
    private void getUser(List<String> userIds, Target targetObj) {
        List<String> uIds = targetObj.getPersonnelIdByType(TargetObjectTypeEnum.user);
        if (CollectionUtils.isNotEmpty(uIds)) {
            userIds.addAll(uIds);
        }
    }

    /**
     * 获取人员类型为“部门”的用户id集合
     * @param userIds 用户id集合
     * @param targetObj 人员信息
     */
    private void getUserByDept(List<String> userIds, Target targetObj) {
        List<String> deptIds = targetObj.getPersonnelIdByType(TargetObjectTypeEnum.dept);
        List<String> uIds = Optional.ofNullable(authUserServiceApi.getByDeptIds(deptIds)).orElse(new R<>()).getData()
                .stream()
                .map(UserDto::getId)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(uIds)) {
            userIds.addAll(uIds);
        }
    }

    /**
     * 获取人员类型为“角色”的用户id集合
     * @param userIds 用户id集合
     * @param targetObj 人员信息
     */
    private void getUserByRole(List<String> userIds, Target targetObj) {
        List<String> roleIds = targetObj.getPersonnelIdByType(TargetObjectTypeEnum.role);
        List<String> uIds = Optional.ofNullable(authUserServiceApi.getByRoleIds(roleIds)).orElse(new R<>()).getData()
                .stream()
                .map(UserDto::getId)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(uIds)) {
            userIds.addAll(uIds);
        }
    }

    /**
     * 获取人员类型为“岗位”的用户id集合
     * @param userIds 用户id集合
     * @param targetObj 人员信息
     */
    private void getUserByJob(List<String> userIds, Target targetObj) {
        List<String> jobIds = targetObj.getPersonnelIdByType(TargetObjectTypeEnum.job);
        List<String> uIds = Optional.ofNullable(authUserServiceApi.getByJobIds(jobIds)).orElse(new R<>()).getData()
                .stream()
                .map(UserDto::getId)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(uIds)) {
            userIds.addAll(uIds);
        }
    }



}
