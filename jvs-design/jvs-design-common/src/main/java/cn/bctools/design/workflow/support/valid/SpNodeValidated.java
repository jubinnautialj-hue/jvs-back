package cn.bctools.design.workflow.support.valid;

import cn.bctools.auth.api.dto.PersonnelDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.workflow.entity.dto.FlowExtendDto;
import cn.bctools.design.workflow.enums.NodeOperationTypeEnum;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.NodeProperties;
import cn.bctools.design.workflow.model.enums.NodePropertiesTypeEnum;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import cn.bctools.design.workflow.model.enums.TargetObjectTypeEnum;
import cn.bctools.design.workflow.model.properties.AppendApprovalProperties;
import cn.bctools.design.workflow.model.properties.FlowButton;
import cn.bctools.design.workflow.model.properties.Leader;
import cn.bctools.design.workflow.utils.FlowUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 审批节点设计校验
 */
@Slf4j
public class SpNodeValidated {

    /**
     * 需要校验的节点类型
     */
    private static final List<NodeTypeEnum> VALID_NODE_TYPES = new ArrayList<NodeTypeEnum>() {{
        add(NodeTypeEnum.SP);
        add(NodeTypeEnum.CS);
    }};

    private SpNodeValidated() {
    }

    public static void valid(FlowExtendDto extend, Node node) {
        // 跳过不需要校验的节点
        if (Boolean.FALSE.equals(VALID_NODE_TYPES.contains(node.getType()))) {
            return;
        }
        // 审批节点 | 抄送节点审批人不能为空
        checkPendingUser(extend, node);
        // 节点需要配置按钮
        checkButton(node);
        // 加签配置校验
        checkAppendApproval(node);
    }

    /**
     * 节点人员不能为空
     *
     * @param extend 流程扩展配置
     * @param node   节点
     */
    private static void checkPendingUser(FlowExtendDto extend, Node node) {
        if (NodeTypeEnum.SP.equals(node.getType())) {
            checkPendingUserSp(extend, node.getName(), node.getProps());
            return;
        }
        if (NodeTypeEnum.CS.equals(node.getType())) {
            checkPendingUserCs(node.getName(), node.getProps());
        }
    }

    /**
     * 校验审批节点审批人
     *
     * @param extend   流程扩展配置
     * @param nodeName 节点名称
     * @param props    节点配置
     */
    private static void checkPendingUserSp(FlowExtendDto extend, String nodeName, NodeProperties props) {
        // 需要检验审批人的类型
        boolean checkType = NodePropertiesTypeEnum.ASSIGN_USER.equals(props.getType()) ||
                NodePropertiesTypeEnum.ROLE.equals(props.getType()) ||
                NodePropertiesTypeEnum.JOB.equals(props.getType()) ||
                NodePropertiesTypeEnum.USER_FIELD.equals(props.getType()) ||
                NodePropertiesTypeEnum.LEADER.equals(props.getType()) ||
                NodePropertiesTypeEnum.LEADER_TOP.equals(props.getType())
                ;
        if (Boolean.FALSE.equals(checkType)) {
            return;
        }

        switch (props.getType()) {
            case ASSIGN_USER:
                // 指定人员，可选“人员”|"部门"，所以只要其中一个类型有数据就通过校验
                checkPendingUserSp(nodeName, props, TargetObjectTypeEnum.user, extend);
                break;
            case ROLE:
                checkPendingUserSp(nodeName, props, TargetObjectTypeEnum.role, extend);
                break;
            case JOB:
                checkPendingUserSp(nodeName, props, TargetObjectTypeEnum.job, extend);
                break;
            case USER_FIELD:
                checkPendingUserSp(nodeName, props, TargetObjectTypeEnum.user_field);
                break;
            case LEADER:
            case LEADER_TOP:
                checkLeaderUserSp(nodeName, props);
                break;
            default:
                // 其它类型不需要发布时校验
                break;
        }
    }

    /**
     * 校验审批节点审批人
     *
     * @param nodeName 节点名称
     * @param props    节点配置
     * @param type     人员对象类型
     */
    private static void checkPendingUserSp(String nodeName, NodeProperties props, TargetObjectTypeEnum type) {
        List<String> ids = props.getTargetObj().getPersonnelIdByType(type);
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException("节点未设置审批人", nodeName);
        }
    }

    /**
     * 校验审批节点审批人
     *
     * @param nodeName 节点名称
     * @param props    节点配置
     * @param type     人员对象类型
     * @param extend   高级设置
     */
    private static void checkPendingUserSp(String nodeName, NodeProperties props, TargetObjectTypeEnum type, FlowExtendDto extend) {
        // 允许动态选择审批人，且节点未禁止动态选择审批人，则在发布时不需要校验审批人必填
        if (extend.getEnableDynamicApprover() && !FlowUtil.getDisableDynamicApprover(props)) {
            return;
        }
        // 校验审批节点审批人
        checkPendingUserSp(nodeName, props, type);
    }


    /**
     * 校验审批节点审批人类型为直接主管、连续多级主管的审批人配置
     *
     * @param nodeName 节点名称
     * @param props 节点配置
     */
    private static void checkLeaderUserSp(String nodeName, NodeProperties props) {
        Leader leader = props.getLeader();
        switch (leader.getLeaderSource()) {
            case USER_FIELD:
                if (ObjectNull.isNull(leader.getUserFieldConfig().getPersonnels())) {
                    throw new BusinessException("环节未设置成员字段请检查设计", nodeName);
                }
                break;
            case FLOW_NODE:
                if (ObjectNull.isNull(leader.getFlowNodeConfig().getNodeIds())) {
                    throw new BusinessException("环节未设置审批节点请检查设计", nodeName);
                }
                break;
            case SEND_USER:
            default:
                break;
        }
    }

    /**
     * 校验抄送节点抄送人
     *
     * @param nodeName 节点名称
     * @param props    节点配置
     */
    private static void checkPendingUserCs(String nodeName, NodeProperties props) {
        if (ObjectNull.isNull(props.getType())) {
            return;
        }
        boolean pass = true;
        if (NodePropertiesTypeEnum.USER_FIELD.equals(props.getType())) {
            try {
                checkPendingUserSp(nodeName, props, TargetObjectTypeEnum.user_field);
            } catch (Exception e) {
                pass = false;
            }
        }

        if (NodePropertiesTypeEnum.ASSIGN_CARBON_COPY.equals(props.getType())) {
            List<String> ids = Optional.ofNullable(props.getTargetObj().getPersonnels()).orElseGet(ArrayList::new)
                    .stream()
                    .map(PersonnelDto::getId)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(ids)) {
                pass = false;
            }
        }

        if (NodePropertiesTypeEnum.LEADER.equals(props.getType())) {
            try {
                checkLeaderUserSp(nodeName, props);
            } catch (Exception e) {
                pass = false;
            }
        }

        if (!pass) {
            throw new BusinessException("环节未设置抄送人请检查设计", nodeName);
        }

    }

    /**
     * 节点需要配置按钮
     *
     * @param node 节点
     */
    private static void checkButton(Node node) {
        if (NodeTypeEnum.SP.equals(node.getType())) {
            if (node.getProps().getBtn().stream().noneMatch(FlowButton::getEnable)) {
                throw new BusinessException("环节未启用按钮请检查设计", node.getName());
            }
            AppendApprovalProperties appendApprovalProperties = node.getProps().getAppendApproval();
            if (ObjectNull.isNotNull(appendApprovalProperties) && appendApprovalProperties.getBtn().stream().noneMatch(FlowButton::getEnable)) {
                throw new BusinessException("环节加签未启用按钮请检查设计", node.getName());
            }
        }
    }

    /**
     * 加签配置校验
     *
     * @param node 节点
     */
    private static void checkAppendApproval(Node node) {
        boolean enable = Optional.ofNullable(node.getProps().getBtn()).orElse(new ArrayList<>()).stream().anyMatch(btn -> NodeOperationTypeEnum.APPEND.equals(btn.getOperation()) && Boolean.TRUE.equals(btn.getEnable()));
        // 加签按钮未启用，则不校验
        if (Boolean.FALSE.equals(enable)) {
            return;
        }
        // 加签按钮已启用校验
        // 加签位置必选
        if (CollectionUtils.isEmpty(node.getProps().getAppendApproval().getPoint())) {
            throw new BusinessException("环节已启用加签按钮未设置加签位置请检查设计", node.getName());
        }
    }
}
