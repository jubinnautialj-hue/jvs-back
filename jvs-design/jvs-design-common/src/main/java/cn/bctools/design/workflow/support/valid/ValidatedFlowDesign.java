package cn.bctools.design.workflow.support.valid;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.workflow.entity.dto.FlowExtendDto;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.utils.FlowUtil;

import java.util.Map;

/**
 * @author zhuxiaokang
 * 工作流设计校验
 */
public class ValidatedFlowDesign {

    private ValidatedFlowDesign() {
    }

    /**
     * 校验工作流设计是否可发布
     *
     * @param flowDesign 工作流设计
     */
    public static void valid(String flowDesign, FlowExtendDto extend) {
        if (ObjectNull.isNull(flowDesign)) {
            throw new BusinessException("流程设计不存在");
        }
        FlowUtil.parseNodeJsonAndCache(flowDesign);
        validNode(extend, FlowUtil.getNodeCache());
    }

    /**
     * 递归校验所有节点
     *
     * @param extend  工作流高级配置
     * @param nodeMap 所有节点
     */
    private static void validNode(FlowExtendDto extend, Map<String, Node> nodeMap) {
        nodeMap.forEach((k, node) -> SpNodeValidated.valid(extend, node));
    }


}
