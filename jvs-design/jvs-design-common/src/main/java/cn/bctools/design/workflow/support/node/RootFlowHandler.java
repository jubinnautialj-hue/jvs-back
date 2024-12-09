package cn.bctools.design.workflow.support.node;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import cn.bctools.design.workflow.model.enums.PurviewGroupEnum;
import cn.bctools.design.workflow.model.enums.PurviewPersonTypeEnum;
import cn.bctools.design.workflow.model.enums.TargetObjectTypeEnum;
import cn.bctools.design.workflow.model.properties.Purview;
import cn.bctools.design.workflow.service.FlowDesignDynamicService;
import cn.bctools.design.workflow.support.FlowResult;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.context.FlowContext;
import cn.bctools.design.workflow.support.impl.AbstractFlowHandler;
import cn.bctools.design.workflow.support.runtime.RuntimeTaskPathService;
import cn.bctools.design.workflow.utils.FlowContextUtil;
import cn.hutool.core.collection.CollectionUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 发起人节点(起始节点)处理
 */
@Slf4j
@Component
@AllArgsConstructor
public class RootFlowHandler extends AbstractFlowHandler {
    private final FlowDesignDynamicService flowDesignDynamicService;
    private final RuntimeTaskPathService taskPathRuntimeService;

    @Override
    public NodeTypeEnum getType() {
        return NodeTypeEnum.ROOT;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handle(Node node, RuntimeData runtimeData) {
        FlowContext flowContext = Optional.ofNullable(FlowContextUtil.context().getContext()).orElseGet(FlowContext::new);
        // 校验发起人是否可以启动当前工作流
        if (Boolean.FALSE.equals(runtimeData.getFlowTask().getTest()) &&
                flowContext.getStart() &&
                Boolean.FALSE.equals(checkPermission(node.getProps().getPurviews(), runtimeData.getUser()))) {
            throw new BusinessException("无权启动流程");
        }
        // 刷新工作流可执行路径到上下文
        taskPathRuntimeService.refreshTaskPath(runtimeData.getFlowTask());
        // 若有动态增加的节点，则将该节点加入流程设计后，再继续流转
        flowDesignDynamicService.addNode(Boolean.TRUE, runtimeData.getFlowTask(), runtimeData.getFlowExtend(), node, runtimeData.getAddNode());
        FlowContextUtil.refreshContext(new FlowResult().setNextNode(node));
    }

    /**
     * 校验是否有发起工作流的权限
     *
     * @param purviewList 权限配置
     * @param currentUser 用户
     * @return TRUE-可以发起工作流，FALSE-无权发起工作流
     */
    private Boolean checkPermission(List<Purview> purviewList, UserDto currentUser) {
        // 未配置权限，直接放行
        if (ObjectNull.isNull(purviewList)) {
            return Boolean.TRUE;
        }

        // 校验发起工作流权限
        List<Purview> sendFlowPurviewList = purviewList.stream()
                .filter(purview -> PurviewGroupEnum.send_flow.equals(purview.getGroup()))
                .collect(Collectors.toList());
        if (ObjectNull.isNull(sendFlowPurviewList)) {
            return Boolean.TRUE;
        }

        // 所有人可发起工作流，不需要校验
        boolean all = sendFlowPurviewList.stream().anyMatch(purview -> PurviewPersonTypeEnum.all.equals(purview.getPersonType()));
        if (all) {
            return Boolean.TRUE;
        }

        // 校验自定义可发起工作流任务的权限
        return sendFlowPurviewList.stream()
                .anyMatch(purview -> {
                    List<String> userIds = purview.getPersonnelIdByType(TargetObjectTypeEnum.user);
                    List<String> deptIds = purview.getPersonnelIdByType(TargetObjectTypeEnum.dept);
                    List<String> roleIds = purview.getPersonnelIdByType(TargetObjectTypeEnum.role);
                    List<String> jobIds = purview.getPersonnelIdByType(TargetObjectTypeEnum.job);
                    return
                            // 权限配置中包含当前用户的id || 部门 || 岗位 || 角色
                            userIds.contains(currentUser.getId()) ||
                            deptIds.contains(currentUser.getDeptId()) ||
                            jobIds.contains(currentUser.getJobId()) ||
                            // 用户可以有多个角色，所以只要用户的任意角色id包含在权限中（两个集合存在交集）就表示有权限
                            CollectionUtil.containsAny(roleIds, currentUser.getRoleIds()) ;
                });
    }

}
