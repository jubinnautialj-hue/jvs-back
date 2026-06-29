package cn.bctools.function.graph;

import cn.bctools.common.utils.ObjectNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 表达式元素关系图
 * <p>
 * 有向图
 *
 * @Author: GuoZi
 */
public class ExpressionGraph<T> {

    private final Set<T> nodes = new HashSet<>();
    private final Map<T, Set<T>> preEdges = new HashMap<>(8);
    private final Map<T, Set<T>> nextEdges = new HashMap<>(8);

    /**
     * 获取图中所有节点
     *
     * @return 节点集合
     */
    public Set<T> getNodes() {
        return this.nodes;
    }

    /**
     * 添加节点
     *
     * @param node 节点
     * @return 节点是否存在
     */
    public boolean addNode(T node) {
        return nodes.add(node);
    }

    /**
     * 添加边
     *
     * @param from 起点节点
     * @param to   目标节点
     * @return 边是否存在
     */
    public boolean addEdge(T from, T to) {
        addNode(from);
        addNode(to);
        Set<T> preNodes = preEdges.computeIfAbsent(to, v -> new HashSet<>());
        preNodes.add(from);
        Set<T> nextNodes = nextEdges.computeIfAbsent(from, v -> new HashSet<>());
        return nextNodes.add(to);
    }

    /**
     * 删除节点
     *
     * @param node 节点
     * @return 删除的节点是否存在
     */
    public boolean removeNode(T node) {
        boolean exist = nodes.remove(node);
        if (!exist) {
            return false;
        }
        Set<T> preNodes = preEdges.remove(node);
        Set<T> nextNodes = nextEdges.remove(node);
        if (preNodes != null) {
            for (T preNode : preNodes) {
                nextEdges.get(preNode).remove(node);
            }
        }
        if (nextNodes != null) {
            for (T nextNode : nextNodes) {
                preEdges.get(nextNode).remove(node);
            }
        }
        return true;
    }

    /**
     * 删除边
     *
     * @param from 起点节点
     * @param to   目标节点
     * @return 删除的边是否存在
     */
    public boolean removeEdge(T from, T to) {
        if (!nodes.contains(from) || !nodes.contains(to)) {
            return false;
        }
        preEdges.computeIfAbsent(to, v -> new HashSet<>()).remove(from);
        return nextEdges.computeIfAbsent(from, v -> new HashSet<>()).remove(to);
    }

    /**
     * 获取后继节点集合
     *
     * @param node 当前节点
     * @return 节点集合
     */
    public Set<T> getNextNodes(T node) {
        Set<T> postNodes = nextEdges.get(node);
        if (nextEdges.containsKey(node)) {
            return postNodes;
        } else {
            Set<T> collect = nextEdges.keySet().stream().filter(e -> {
                boolean contains = e.toString().contains(node.toString());
                if (contains) {
                    return contains;
                } else {
                    return nextEdges.get(e).stream().anyMatch(a -> a.toString().contains(node.toString()));
                }
            }).collect(Collectors.toSet());
            if (ObjectNull.isNull(collect)) {
                return Collections.emptySet();
            } else {
                return collect;
            }
        }
    }

    /**
     * 获取前序节点集合
     *
     * @param node 当前节点
     * @return 节点集合
     */
    public Set<T> getPreNodes(T node) {
        Set<T> preNodes = preEdges.get(node);
        if (preNodes == null) {
            preNodes = Collections.emptySet();
        }
        return preNodes;
    }

}
