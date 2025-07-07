package cn.bctools.design.rule;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.rule.entity.enums.NodeType;
import cn.bctools.rule.utils.html.HtmlEdge;
import cn.bctools.rule.utils.html.HtmlGraph;
import cn.bctools.rule.utils.html.NodeHtml;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HtmlGraphUtils {

    public static void duplicateSyncLine(HtmlGraph graph){
        verifyHtmlGraph(graph,"主画布");
    }

    private static void verifyHtmlGraph(HtmlGraph graph,String graphName){
        //1. 查找所有的结束节点 没有下级节点的就是结束节点
        List<NodeHtml> nodeList = graph.getNodeList();

        List<HtmlEdge> lineList = graph.getLineList();

        Map<String, List<String>> fromMap = lineList.stream().collect(Collectors.groupingBy(HtmlEdge::getFrom, Collectors.mapping(HtmlEdge::getId, Collectors.toList())));

        Map<String, HtmlEdge> lineMap = lineList.stream().collect(Collectors.toMap(HtmlEdge::getId, Function.identity()));

        List<HtmlEdge> asyncLines = lineList.stream().filter(HtmlEdge::isAsync).collect(Collectors.toList());
        if(asyncLines.size()<2){
            return;
        }
        Map<String, String> nodeNameMap = nodeList.stream().collect(Collectors.toMap(NodeHtml::getId, NodeHtml::getName));

        NodeHtml start = nodeList.stream().filter(e -> NodeType.start.equals(e.getType())).findFirst().orElse(null);
        if(start==null){
            return;
        }
        List<String> lineIds = fromMap.get(start.getId());

        for (String lineId : lineIds) {
            HtmlEdge htmlEdge = lineMap.get(lineId);
            try {
                findPath(htmlEdge.isAsync(),fromMap,lineMap,htmlEdge.getTo(), nodeNameMap);
            }catch (BusinessException be){
                String error = graphName+"异常："+be.getMessage();
                throw new BusinessException(error);
            }catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if(MapUtil.isNotEmpty(graph.getErgodicCanvas())){
            for (Map.Entry<String, HtmlGraph> entry : graph.getErgodicCanvas().entrySet()) {
                String key = entry.getKey();
                String nodeName = nodeNameMap.getOrDefault(key,"已删除画布");
                HtmlGraph childrenGraph = entry.getValue();
                verifyHtmlGraph(childrenGraph,"画布:"+nodeName);
            }
        }
    }

    private static void findPath(boolean state,Map<String,List<String>> fromMap,
                                 Map<String,HtmlEdge> lineMap,
                                 String currentNodeId,
                                 Map<String, String> nodeNameMap){
        if(!fromMap.containsKey(currentNodeId)){
            return;
        }
        List<String> lineIds = fromMap.get(currentNodeId);
        for (String lineId : lineIds) {
            //避免修改原状态，导致其他线判断失败
            boolean newState = state;
            HtmlEdge line = lineMap.get(lineId);
            if(state && line.isAsync()){
                String error = StrUtil.format("开始节点 -> {} 节点之间出现两条异步线", nodeNameMap.get(line.getTo()));
                throw new BusinessException(error);
            }
            if(line.isAsync()){
                newState = true;
            }
            findPath(newState,fromMap,lineMap,line.getTo(),nodeNameMap);
        }

    }

    @Data
    @Accessors(chain = true)
    public static class CheckResult{

        private Boolean isError;

        private String message;
    }

}
