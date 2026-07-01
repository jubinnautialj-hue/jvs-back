package cn.bctools.design.workflow.utils;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 工作流节点路径工具
 */
public class FlowPathUtil {

    private FlowPathUtil() {

    }

    /**
     * 获取工作流设计的所有路径
     *
     * @param flow 工作流设计json
     * @return 所有路径
     */
    public static List<List<Node>> getNodePaths(String flow) {
        List<List<Node>> paths = new ArrayList<>();
        List<Node> path = new ArrayList<>();
        FlowUtil.clearNodeCache();
        FlowUtil.parseNodeJsonAndCache(flow);
        Node rootNode = FlowUtil.getRootNode(flow);
        path.add(rootNode);
        getNodePaths(rootNode, path, paths);
        return paths;
    }

    /**
     * 获取指定节点下所有节点路径
     *
     * @param flow 流程设计
     * @param node 节点
     * @return 指定节点下所有节点路径
     */
    public static List<List<Node>> getNodePaths(String flow, Node node) {
        List<List<Node>> paths = new ArrayList<>();
        List<Node> path = new ArrayList<>();
        FlowUtil.clearNodeCache();
        FlowUtil.parseNodeJsonAndCache(flow);
        getNodePaths(node, path, paths);
        return paths;
    }

    /**
     * 获取指定节点下所有节点路径
     *
     * @param node  指定节点
     * @param path  单条路径
     * @param paths 路径集合
     */
    private static void getNodePaths(Node node, List<Node> path, List<List<Node>> paths) {
        Node nextNode = FlowUtil.getNextNode(node);
        if (FlowUtil.isNullNode(nextNode)) {
            paths.add(path);
            return;
        }
        path.add(new Node()
                .setId(nextNode.getId())
                .setName(nextNode.getName())
                .setType(nextNode.getType())
                .setProps(nextNode.getProps())
        );
        if (nextNode.getType().equals(NodeTypeEnum.PARALLEL)) {
            nextNode.getParallels().forEach(parallel -> {
                List<Node> childPath = new ArrayList<>(path);
                childPath.add(parallel);
                getNodePaths(parallel, childPath, paths);
            });
            return;
        }
        if (nextNode.getType().equals(NodeTypeEnum.CONDITION)) {
            nextNode.getConditions().forEach(condition -> {
                List<Node> childPath = new ArrayList<>(path);
                childPath.add(condition);
                getNodePaths(condition, childPath, paths);
            });
            return;
        }
        getNodePaths(FlowUtil.findNode(nextNode.getId()), path, paths);
    }

    /**
     * 得到指定节点上一个人工节点
     *
     * @param paths  路径
     * @param nodeId 节点id
     * @return 指定节点上一个人工审批节点集合
     */
    public static List<Node> getPreviousManualNodes(List<List<Node>> paths, String nodeId) {
        if (ObjectNull.isNull(paths)) {
            return Collections.emptyList();
        }
        return paths.stream().map(path -> {
                    Optional<Node> nodeOptional = path.stream().filter(p -> nodeId.equals(p.getId())).findFirst();
                    if (nodeOptional.isPresent()) {
                        int index = path.indexOf(nodeOptional.get());
                        ListIterator<Node> listIterator = path.listIterator(index);
                        while (listIterator.hasPrevious()) {
                            Node previousNode = listIterator.previous();
                            if (NodeTypeEnum.SP.equals(previousNode.getType()) || NodeTypeEnum.ROOT.equals(previousNode.getType())) {
                                return previousNode;
                            }
                        }
                    }
                    return null;
                }).filter(ObjectNull::isNotNull)
                .distinct()
                .collect(Collectors.toList());
    }
}
