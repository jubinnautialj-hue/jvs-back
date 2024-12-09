package cn.bctools.design.workflow.utils;

import com.alibaba.ttl.TransmittableThreadLocal;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhuxiaokang
 * 工作流节点工具
 */
public class FlowTaskNodeUtil {
    private FlowTaskNodeUtil() {

    }

    /**
     * 工待处理审批节点
     * 用于发送消息
     * List<String>
     */
    private static ThreadLocal<List<String>> flowNodeIdCache = new TransmittableThreadLocal<>();

    /**
     * 增加待处理审批节点
     *
     * @param nodeId 待处理审批节点id
     */
    public static void addTaskNode(String nodeId) {
        List<String> nodeIds = getNodeIds();
        if (CollectionUtils.isEmpty(nodeIds)) {
            nodeIds = new ArrayList<>();
        }
        nodeIds.add(nodeId);
        flowNodeIdCache.set(nodeIds);
    }

    /**
     * 获取工作流节点缓存
     *
     * @return 待处理审批节点id集合
     */
    public static List<String> getNodeIds() {
        return flowNodeIdCache.get();
    }

    /**
     * 清除当前工作流设计的缓存
     */
    public static void clear() {
        flowNodeIdCache.remove();
    }
}
