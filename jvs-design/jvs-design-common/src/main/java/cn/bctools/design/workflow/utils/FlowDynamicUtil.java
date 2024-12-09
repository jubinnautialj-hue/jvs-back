package cn.bctools.design.workflow.utils;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.workflow.entity.dto.FlowExtendDto;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.NodeForm;
import cn.bctools.design.workflow.model.NodeProperties;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import cn.bctools.design.workflow.model.properties.Purview;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * @author zhuxiaokang
 * 动态设计工作流
 */
@Slf4j
public class FlowDynamicUtil {

    /**
     * 允许动态添加子节点的类型
     */
    private static final List<NodeTypeEnum> TYPES = Arrays.asList(NodeTypeEnum.ROOT, NodeTypeEnum.SP);

    private FlowDynamicUtil() {
    }

    /**
     * 生成串行流程设计
     * 根据有序的节点设计集合，按节点顺序生成串行流程设计
     *
     * @param nodes 有序的节点设计集合
     * @return 串行流程设计
     */
    public static String buildSerial(List<Node> nodes) {
        // 构造根节点
        Node node = buildRoot();
        if (CollectionUtils.isEmpty(nodes)) {
            return JSON.toJSONString(node);
        }
        // 根据节点集合构造新的流程体
        ListIterator<Node> nodeListIterator = nodes.listIterator(1);
        Node currentNode = node;
        while (nodeListIterator.hasNext()) {
            Node nextNode = nodes.get(nodeListIterator.previousIndex());
            settingNode(currentNode, nextNode);
            currentNode = currentNode.getNode();
            Node next = nodeListIterator.next();
            if (Boolean.FALSE.equals(nodeListIterator.hasNext())) {
                settingNode(currentNode, next);
            }
        }
        return JSON.toJSONString(node);
    }

    /**
     * 节点属性设置
     *
     * @param currentNode 节点
     * @param nextNode    下级节点
     */
    private static void settingNode(Node currentNode, Node nextNode) {
        nextNode.setPid(currentNode.getId());
        currentNode.setNode(nextNode);
    }

    /**
     * 构造发起节点
     *
     * @return 流程根节点
     */
    private static Node buildRoot() {
        Node rootNode = new Node();
        rootNode.setId(NodeTypeEnum.ROOT.getDefaultNodeId())
                .setName(NodeTypeEnum.ROOT.getDefaultNodeName())
                .setType(NodeTypeEnum.ROOT)
                .setNodeForm(NodeForm.buildDefault())
                .setProps(new NodeProperties().setPurviews(Collections.singletonList(Purview.defaultPurview())));
        return rootNode;
    }

    /**
     * 添加下级节点
     * 将新节点添加到流程设计中
     *
     * @param flowDesign 流程设计
     * @param targetNode 目标节点
     * @param newNode    新节点
     * @return 新的流程设计
     */
    public static String addNextNode(String flowDesign, Node targetNode, Node newNode) {
        Node node = FlowUtil.parseNodeJsonAndCache(flowDesign);
        // 校验新加的节点id是否已存在
        if (ObjectNull.isNotNull(FlowUtil.findNode(newNode.getId()))) {
            log.warn("新增工作流节点失败：节点id已存在");
            return JSON.toJSONString(node);
        }
        addNode(node, targetNode, newNode);
        return JSON.toJSONString(node);
    }

    /**
     * 修改Node结构,并将所有节点缓存到本地线程
     *
     * @param node       节点设计
     * @param targetNode 目标节点
     * @param newNode    新节点
     */
    private static void addNode(Node node, Node targetNode, Node newNode) {
        Node nextNode = node.getNode();
        // 找到目标节点，并将新节点添加到该节点后
        if (node.getId().equals(targetNode.getId())) {
            newNode.setPid(node.getId());
            if (nextNode != null && ObjectNull.isNotNull(nextNode.getId())) {
                nextNode.setPid(newNode.getId());
                newNode.setNode(nextNode);
            }
            node.setNode(newNode);
        }

        if (nextNode != null && ObjectNull.isNotNull(nextNode.getId())) {
            if (NodeTypeEnum.CONDITION.equals(node.getType())) {
                node.getConditions().forEach(n -> {
                    addNode(n, targetNode, newNode);
                });
            }
            if (NodeTypeEnum.PARALLEL.equals(node.getType())) {
                node.getParallels().forEach(n -> {
                    addNode(n, targetNode, newNode);
                });
            }
            addNode(nextNode, targetNode, newNode);
        }
    }


    /**
     * 校验是否可动态增加节点
     *
     * @param flowDesign  流程设计
     * @param flowExtend  流程设计高级配置
     * @param currentNode 当前节点
     * @return TRUE-可动态增加节点，FALSE-不能动态增加节点
     */
    public static Boolean checkDynamicNode(String flowDesign, FlowExtendDto flowExtend, Node currentNode) {
        if (ObjectNull.isNull(currentNode)) {
            return Boolean.FALSE;
        }
        // 是否已启用“动态增加流程节点”
        if (Boolean.FALSE.equals(flowExtend.getEnableDynamicNode())) {
            return Boolean.FALSE;
        }
        // 当前节点类型是否在“允许动态添加子节点的类型”中
        if (Boolean.FALSE.equals(TYPES.contains(currentNode.getType()))) {
            return Boolean.FALSE;
        }
        // 满足以上条件，且当前节点的后续节点没有审批节点，则可以增加审批节点
        List<Node> nextNodeAll = FlowUtil.getOrderNodes(flowDesign, currentNode.getId(), Collections.singletonList(NodeTypeEnum.SP));
        if (CollectionUtils.isNotEmpty(nextNodeAll)) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}
