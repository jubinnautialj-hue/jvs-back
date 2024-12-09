package cn.bctools.rule.utils.html;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author guojing
 */
@Data
public class HtmlGraph {
    /**
     * 具体的连线
     */
    private List<HtmlEdge> lineList;
    /**
     * 所有的节点
     */
    private List<NodeHtml> nodeList;
    /**
     * 循环画布,用于关联画布节点功能
     */
    private Map<String, HtmlGraph> ergodicCanvas;
    /**
     * 分组信息
     */
    private List<HtmlGroups> groups;
    /**
     * 当前画布的ID，如果为主画布，则为调用Key
     */
    private String canvasId;
    /**
     * 运行的日志Id
     */
    private String tid;


}
