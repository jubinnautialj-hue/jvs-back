package cn.bctools.rule;

import cn.bctools.rule.utils.html.HtmlEdge;
import cn.bctools.rule.utils.html.NodeHtml;
import cn.bctools.rule.utils.html.RuleExecuteDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AsyncService {
    /**
     * 执行异步线
     *
     * @param edges      线信息
     * @param nodes      点信息
     * @param nextNode   当前线
     * @param executeDto 执行对象信息
     * @param futureList
     * @return
     */
    void async(List<HtmlEdge> edges, List<NodeHtml> nodes, NodeHtml nextNode, RuleExecuteDto executeDto, List<CompletableFuture<RuleExecuteDto>> futureList);
}
