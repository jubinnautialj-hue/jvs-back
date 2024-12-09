package cn.bctools.design.workflow.utils;

import cn.bctools.design.workflow.constant.SystemConstant;

/**
 * @author zhuxiaokang
 * 工作流审批结果缓存工具
 */
public class FlowApprovalCacheUtil {

    private FlowApprovalCacheUtil() {

    }

    /**
     * 审批缓存
     */
    private static final String APPROVER = "approver";

    /**
     * 审批人key
     *
     * @param id     工作流任务id
     * @param nodeId 工作流节点id
     * @return 审批人key
     */
    public static String getApproverKey(String id, String nodeId) {
        return SystemConstant.SYSTEM_NAME + ":" + APPROVER + ":" + id + ":" + nodeId;
    }
}
