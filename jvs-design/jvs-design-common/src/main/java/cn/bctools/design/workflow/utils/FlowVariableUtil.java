package cn.bctools.design.workflow.utils;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.workflow.model.*;
import cn.bctools.design.workflow.model.enums.ConditionPropertiesTypeEnum;
import cn.bctools.design.workflow.model.enums.NodePropertiesTypeEnum;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import cn.bctools.design.workflow.model.properties.PersonnelScope;
import cn.bctools.design.workflow.model.properties.Purview;
import cn.bctools.design.workflow.model.properties.Target;
import com.alibaba.fastjson2.JSON;
import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.*;

/**
 * @author zhuxiaokang
 * 工作流设计变量工具类
 */
public class FlowVariableUtil {
    private FlowVariableUtil() {
    }

    /**
     * 工作流节点缓存
     * Map<节点id, 节点对象>
     */
    private static final ThreadLocal<Map<String, Object>> NODE_CACHE = new TransmittableThreadLocal<>();

    /**
     * 剔除设计中的变量
     *
     * @param flowDesignBody 工作流设计JSON
     * @return 没有变量的工作流设计JSON
     */
    public static String extractVariable(String flowDesignBody) {
        Node flowNode = FlowUtil.parseNodeJson(flowDesignBody);
        // 剔除设计中的变量，得到新的设计体
        extractVariable(flowNode);
        return JSON.toJSONString(flowNode);
    }

    /**
     * 比较设计并填充变量
     *
     * @param designBody     工作流设计
     * @param fillDesignBody 待填充的工作流设计
     * @return 新的工作流设计
     */
    public static String compareAndFillVariable(String designBody, String fillDesignBody) {
        Node fillNode = FlowUtil.parseNodeJson(fillDesignBody);
        setNodeCache(designBody);
        compareAndFillVariable(fillNode);
        clearNodeCache();
        return JSON.toJSONString(fillNode);
    }


    /**
     * 剔除设计中的变量
     * - 剔除用户配置
     *
     * @param node 节点
     */
    private static void extractVariable(Node node) {
        // 剔除变量
        extractRootVariable(node);
        extractSpVariable(node);
        extractCsVariable(node);
        extractTjVariable(node);

        // 继续遍历节点
        Node nextNode = node.getNode();
        if (nextNode != null) {
            if (NodeTypeEnum.CONDITION.equals(node.getType())) {
                node.getConditions().forEach(FlowVariableUtil::extractVariable
                );
            }
            if (NodeTypeEnum.PARALLEL.equals(node.getType())) {
                node.getParallels().forEach(FlowVariableUtil::extractVariable
                );
            }
            extractVariable(nextNode);
        }
    }

    /**
     * 剔除开始节点变量
     *
     * @param node 节点
     */
    private static void extractRootVariable(Node node) {
        if (Boolean.FALSE.equals(NodeTypeEnum.ROOT.equals(node.getType()))) {
            return;
        }
        Optional.ofNullable(node.getProps().getPurviews())
                .orElseGet(ArrayList::new)
                .forEach(purview -> purview.setPersonnels(Collections.emptyList()));
    }

    /**
     * 剔除审批节点变量
     *
     * @param node 节点
     */
    private static void extractSpVariable(Node node) {
        if (Boolean.FALSE.equals(NodeTypeEnum.SP.equals(node.getType()))) {
            return;
        }
        NodePropertiesTypeEnum spType = node.getProps().getType();

        // 人员选择范围，并置空设计
        PersonnelScope personnelScope = node.getProps().getPersonnelScope();
        if (ObjectNull.isNotNull(personnelScope)) {
            personnelScope.setPersonnelScopes(Collections.emptyList());
        }

        // 剔除审批人
        Target target = node.getProps().getTargetObj();
        if (NodePropertiesTypeEnum.ASSIGN_USER.equals(spType) || NodePropertiesTypeEnum.ROLE.equals(spType) || NodePropertiesTypeEnum.JOB.equals(spType)) {
            target.setPersonnels(Collections.emptyList());
        }
    }

    /**
     * 剔除抄送节点变量
     *
     * @param node 节点
     */
    private static void extractCsVariable(Node node) {
        if (Boolean.FALSE.equals(NodeTypeEnum.CS.equals(node.getType()))) {
            return;
        }
        Target target = node.getProps().getTargetObj();
        target.setPersonnels(Collections.emptyList());
    }

    /**
     * 剔除条件节点变量
     *
     * @param node 节点
     */
    private static void extractTjVariable(Node node) {
        if (Boolean.FALSE.equals(NodeTypeEnum.TJ.equals(node.getType()))) {
            return;
        }
        Condition condition = (Condition) node;
        for (ConditionGroup group : condition.getGroups()) {
            if (ObjectNull.isNull(group.getCondition())) {
                continue;
            }
            for (ConditionProperties conditionProperties : group.getCondition()) {
                if (ConditionPropertiesTypeEnum.FUN.equals(conditionProperties.getType())) {
                    continue;
                }
                conditionProperties.setValues(Collections.emptyList());
            }
        }
    }

    /**
     * 比较并填充设计中的变量
     * <p>
     * 以待填充的设计为基准，获取节点id相同的设计对比配置，若配置相同，则填充，否则不处理
     *
     * @param fillNode 待填充的工作流设计
     */
    private static void compareAndFillVariable(Node fillNode) {
        // 比较并填充变量
        Node node = findNode(fillNode.getId(), fillNode.getType());
        if (ObjectNull.isNotNull(node)) {
            compareAndFillRootVariable(node, fillNode);
            compareAndFillSpVariable(node, fillNode);
            compareAndFillCsVariable(node, fillNode);
            compareAndFillTjVariable(node, fillNode);
        }

        // 继续遍历节点
        Node nextNode = fillNode.getNode();
        if (nextNode != null) {
            if (NodeTypeEnum.CONDITION.equals(fillNode.getType())) {
                fillNode.getConditions().forEach(FlowVariableUtil::compareAndFillVariable);
            }
            if (NodeTypeEnum.PARALLEL.equals(fillNode.getType())) {
                fillNode.getParallels().forEach(FlowVariableUtil::compareAndFillVariable
                );
            }
            compareAndFillVariable(nextNode);
        }
    }

    /**
     * 比较并填充开始节点变量
     *
     * @param node     节点
     * @param fillNode 待填充的节点
     */
    private static void compareAndFillRootVariable(Node node, Node fillNode) {
        if (Boolean.FALSE.equals(NodeTypeEnum.ROOT.equals(fillNode.getType()))) {
            return;
        }
        List<Purview> nodePurviewList = Optional.ofNullable(node.getProps().getPurviews()).orElseGet(ArrayList::new);
        if (ObjectNull.isNull(nodePurviewList)) {
            return;
        }
        List<Purview> fillNodePurviewList = Optional.ofNullable(fillNode.getProps().getPurviews()).orElseGet(ArrayList::new);
        if (nodePurviewList.size() != fillNodePurviewList.size()) {
            return;
        }
        int fillPurviewSize = fillNodePurviewList.size();
        for (int i = 0; i < fillPurviewSize; i++) {
            Purview purview = nodePurviewList.get(i);
            Purview fillPurview = fillNodePurviewList.get(i);
            // 成员类型相同，则填充
            if (purview.getPersonType().equals(fillPurview.getPersonType())) {
                fillPurview.setPersonnels(purview.getPersonnels());
            }
        }
    }

    /**
     * 比较并填充审批节点变量
     *
     * @param node     节点
     * @param fillNode 待填充的节点
     */
    private static void compareAndFillSpVariable(Node node, Node fillNode) {
        if (Boolean.FALSE.equals(NodeTypeEnum.SP.equals(fillNode.getType()))) {
            return;
        }
        NodeProperties fillNodeProperties = fillNode.getProps();
        NodePropertiesTypeEnum fillNodeApprovedType = fillNodeProperties.getType();
        boolean flag = NodePropertiesTypeEnum.ASSIGN_USER.equals(fillNodeApprovedType) ||
                NodePropertiesTypeEnum.ROLE.equals(fillNodeApprovedType) ||
                NodePropertiesTypeEnum.JOB.equals(fillNodeApprovedType) ||
                NodePropertiesTypeEnum.SELF_SELECT.equals(fillNodeApprovedType);
        // 不处理不需要填充的类型
        if (Boolean.FALSE.equals(flag)) {
            return;
        }

        NodeProperties nodeProperties = node.getProps();
        NodePropertiesTypeEnum nodeApprovedType = nodeProperties.getType();
        // 类型不同不填充
        if (Boolean.FALSE.equals(fillNodeApprovedType.equals(nodeApprovedType))) {
            return;
        }

        // 填充人员选择范围
        PersonnelScope personnelScope = Optional.ofNullable(nodeProperties.getPersonnelScope()).orElseGet(PersonnelScope::new);
        PersonnelScope fillPersonnelScope = fillNodeProperties.getPersonnelScope();
        if (ObjectNull.isNotNull(fillPersonnelScope) && fillPersonnelScope.getType().equals(personnelScope.getType())) {
            fillPersonnelScope.setPersonnelScopes(personnelScope.getPersonnelScopes());
        }

        // 填充审批人
        Target target = nodeProperties.getTargetObj();
        Target fillTarget = fillNodeProperties.getTargetObj();
        fillTarget.setPersonnels(target.getPersonnels());
    }

    /**
     * 比较并填充抄送节点变量
     *
     * @param node     节点
     * @param fillNode 待填充的节点
     */
    private static void compareAndFillCsVariable(Node node, Node fillNode) {
        if (Boolean.FALSE.equals(NodeTypeEnum.CS.equals(fillNode.getType()))) {
            return;
        }
        Target target = node.getProps().getTargetObj();
        Target fillTarget = fillNode.getProps().getTargetObj();
        fillTarget.setPersonnels(target.getPersonnels());
    }

    /**
     * 比较并填充条件节点变量
     * <p>
     * 以待填充的设计为基准，按数组顺序依次对比条件组内的条件配置，若配置相同，则填充，否则不处理
     *
     * @param node     节点
     * @param fillNode 待填充的节点
     */
    private static void compareAndFillTjVariable(Node node, Node fillNode) {
        if (Boolean.FALSE.equals(NodeTypeEnum.TJ.equals(fillNode.getType()))) {
            return;
        }
        Condition condition = (Condition) node;
        List<ConditionGroup> groups = condition.getGroups();
        Condition fillCondition = (Condition) fillNode;
        List<ConditionGroup> fillGroups = fillCondition.getGroups();

        for (int i = 0; i < fillGroups.size(); i++) {
            if (i >= groups.size()) {
                break;
            }
            ConditionGroup fillGroup = fillGroups.get(i);
            if (ObjectNull.isNull(fillGroup.getCondition())) {
                continue;
            }
            ConditionGroup group = groups.get(i);
            List<ConditionProperties> conditionProperties = group.getCondition();
            List<ConditionProperties> fillConditionProperties = fillGroup.getCondition();
            for (int j = 0; j < fillConditionProperties.size(); j++) {
                if (j >= conditionProperties.size()) {
                    continue;
                }
                ConditionProperties fillConditionProp = fillConditionProperties.get(j);
                if (ConditionPropertiesTypeEnum.FUN.equals(fillConditionProp.getType())) {
                    continue;
                }
                ConditionProperties conditionProp = conditionProperties.get(j);
                fillConditionProp.setValues(conditionProp.getValues());
            }
        }
    }


    /**
     * 设置工作流节点缓存
     *
     * @param designBody 工作流设计
     */
    private static void setNodeCache(String designBody) {
        Node node = FlowUtil.parseNodeJson(designBody);
        Map<String, Object> objectMap = new HashMap<>(8);
        getNodeAll(node, objectMap);
        NODE_CACHE.set(objectMap);
    }

    /**
     * 获取工作流节点缓存
     *
     * @return Map<节点id, 节点对象>
     */
    private static Map<String, Object> getNodeCache() {
        return NODE_CACHE.get();
    }

    /**
     * 清除当前工作流设计的缓存
     */
    private static void clearNodeCache() {
        NODE_CACHE.remove();
    }

    /**
     * 获取节点
     *
     * @param nodeId   节点id
     * @param nodeType 节点类型
     * @return 节点
     */
    private static Node findNode(String nodeId, NodeTypeEnum nodeType) {
        Object nodeObj = getNodeCache().get(nodeId);
        if (NodeTypeEnum.TJ.equals(nodeType)) {
            return (Condition) nodeObj;
        }
        return (Node) nodeObj;
    }


    /**
     * 获取所有节点
     *
     * @param node  节点
     * @param nodes 所有节点
     */
    private static void getNodeAll(Node node, Map<String, Object> nodes) {
        if (ObjectNull.isNotNull(node.getId())) {
            nodes.put(node.getId(), node);
        }
        Node nextNode = node.getNode();
        if (nextNode != null) {
            if (NodeTypeEnum.CONDITION.equals(node.getType())) {
                node.getConditions().forEach(n -> getNodeAll(n, nodes));
            }
            if (NodeTypeEnum.PARALLEL.equals(node.getType())) {
                node.getParallels().forEach(n -> getNodeAll(n, nodes));
            }
            getNodeAll(nextNode, nodes);
        }
    }
}
