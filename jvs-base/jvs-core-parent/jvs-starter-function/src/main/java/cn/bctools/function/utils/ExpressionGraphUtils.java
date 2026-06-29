package cn.bctools.function.utils;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.function.entity.po.FunctionBusinessPo;
import cn.bctools.function.graph.ExpressionGraph;
import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 表达式关系工具类-图
 *
 * @Author: GuoZi
 */
public class ExpressionGraphUtils {

    private ExpressionGraphUtils() {
    }

    /**
     * 构建表达式关系图
     *
     * @param functionList 表达式集合
     * @return 有向图集合
     */
    public static ExpressionGraph<String> buildFunctionGraph(List<FunctionBusinessPo> functionList) {
        ExpressionGraph<String> graph = new ExpressionGraph<>();
        for (FunctionBusinessPo function : functionList) {
            String businessId = function.getBusinessId();
            List<String> relatedIds = function.getRelatedIds();
            graph.addNode(businessId);
            if (ObjectUtils.isEmpty(relatedIds)) {
                continue;
            }
            for (String relatedId : relatedIds) {
                graph.addEdge(relatedId, businessId);
            }
        }
        return graph;
    }

    /**
     * 获取表达式计算顺序
     *
     * @param graph     表达式关系图
     * @param startNode 计算起点(为空时会尝试计算所有表达式)
     * @return 表达式顺序
     */
    public static <T> List<T> getCalculateSort(ExpressionGraph<T> graph, T startNode) {
        Set<T> startNodeList;
        if (ObjectUtils.isEmpty(startNode)) {
            startNodeList = ExpressionGraphUtils.getGraphRootList(graph);
        } else {
            startNodeList = Collections.singleton(startNode);
        }
        if (ObjectUtils.isEmpty(startNodeList)) {
            return Collections.emptyList();
        }
        Map<T, Integer> distanceMap = new HashMap<>(1);
        for (T node : startNodeList) {
            Map<T, Integer> farthestFirstSearch = ExpressionGraphUtils.farthestFirstSearch(graph, node);
            for (Map.Entry<T, Integer> entry : farthestFirstSearch.entrySet()) {
                T nodeName = entry.getKey();
                Integer newDistance = entry.getValue();
                Integer oldDistance = distanceMap.get(nodeName);
                if (Objects.isNull(oldDistance) || oldDistance < newDistance) {
                    distanceMap.put(nodeName, newDistance);
                }
            }
        }
        return distanceMap.entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getValue)).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    /**
     * 校验图中是否存在循环
     *
     * @param graph 图
     * @param <T>   节点类型
     */
    public static <T> void checkCircularReference(ExpressionGraph<T> graph) {
        ExpressionGraphUtils.getGraphRootList(graph);
    }

    /**
     * 远距离优先搜索
     *
     * @param graph     图
     * @param startNode 开始节点
     */
    private static <T> Map<T, Integer> farthestFirstSearch(ExpressionGraph<T> graph, T startNode) {
        Set<T> nodes = graph.getNodes();
        if (ObjectUtils.isEmpty(nodes) || ObjectUtils.isEmpty(startNode)) {
            return Collections.emptyMap();
        }
        Map<T, Integer> distanceMap = new HashMap<>(nodes.size());
        Set<T> set = new HashSet<>();
        ExpressionGraphUtils.farthestFirstSearch(graph, distanceMap, startNode, set, 0);
        return distanceMap;
    }

    /**
     * 远距离优先搜索
     *
     * @param graph           图
     * @param distanceMap     存储遍历结果<节点标识, 节点距离>
     * @param currentNode     当前节点
     * @param currentDistance 当前距离
     */
    private static <T> void farthestFirstSearch(ExpressionGraph<T> graph, Map<T, Integer> distanceMap, T currentNode, Set<T> set, int currentDistance) {
        Integer distance = distanceMap.computeIfAbsent(currentNode, v -> -1);
        if (distance < currentDistance) {
            distanceMap.put(currentNode, currentDistance);
        }
        Set<T> nextNodes = BeanCopyUtil.copy(graph.getNextNodes(currentNode), HashSet.class);
        nextNodes.removeAll(set);
        if (ObjectUtils.isEmpty(nextNodes)) {
            return;
        }
        set.addAll(nextNodes);

        for (T nextNode : nextNodes) {
            ExpressionGraphUtils.farthestFirstSearch(graph, distanceMap, nextNode, set, currentDistance + 1);
        }
    }

    /**
     * 获取图中的所有连通分支的根节点
     * <p>
     * 顺便校验是否存在循环
     *
     * @param graph 图
     * @param <T>   节点类型
     * @return 根节点集合
     */
    private static <T> Set<T> getGraphRootList(ExpressionGraph<T> graph) {
        Set<T> nodes = graph.getNodes();
        if (ObjectUtils.isEmpty(nodes)) {
            return Collections.emptySet();
        }
        // 获取图中各个连通分支的根节点
        Set<T> roots = new HashSet<>();
        Set<T> handledNodes = new HashSet<>();
        Set<T> path = new HashSet<>();
        Set<T> branchPath = new HashSet<>();
        for (T node : nodes) {
            if (handledNodes.contains(node)) {
                continue;
            }
            ExpressionGraphUtils.searchRoot(graph, node, roots, path, branchPath);
            handledNodes.addAll(path);
            path.clear();
            branchPath.clear();
        }
        return roots;
    }

    /**
     * 遍历图, 寻找根节点
     *
     * @param graph       图
     * @param currentNode 当前节点
     * @param roots       根节点集合
     * @param path        处理过的节点
     * @param <T>         节点类型
     */
    private static <T> void searchRoot(ExpressionGraph<T> graph, T currentNode, Set<T> roots, Set<T> path, Set<T> branchPath) {
        path.add(currentNode);
        // 校验参数循环依赖
        boolean exist = !branchPath.add(currentNode);
        if (exist) {
            throw new BusinessException("表达式存在循环依赖" + JSONObject.toJSONString(branchPath));
        }
        Set<T> preNodes = graph.getPreNodes(currentNode);
        if (ObjectUtils.isEmpty(preNodes)) {
            roots.add(currentNode);
            branchPath.remove(currentNode);
            return;
        }
        for (T preNode : preNodes) {
            ExpressionGraphUtils.searchRoot(graph, preNode, roots, path, branchPath);
        }
        branchPath.remove(currentNode);
    }

//    public static void main(String[] args) {
//        // 获取该设计下所有的表达式
//        List<FunctionBusinessPo> functionList = new ArrayList<>();
//        functionList.add(new FunctionBusinessPo().setId("1585524197310861314").setType("input").setBusinessId("cailiaomingcheng").setRelatedIds(Arrays.asList("cailiaomingxi.cailiaomingcheng")).setBody("${cailiaomingxi.cailiaomingcheng}"));
//        // 构建变量引用的关系图
//        ExpressionGraph<String> graph = ExpressionGraphUtils.buildFunctionGraph(functionList);
//        // 计算各个参数, 以远距离优先搜索为顺序
//        List<String> paramBusinessIds = ExpressionGraphUtils.getCalculateSort(graph, "cailiaomingxi");
//    }

}
