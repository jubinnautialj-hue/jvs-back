package cn.bctools.design.workflow.support.function.impl;

import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.enums.NodePropertiesModeEnum;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.function.AbstractFunctionHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author jvs
 * 移除审批人
 */
@Slf4j
@Component
@AllArgsConstructor
public class RemoveSignerFunction extends AbstractFunctionHandler<Boolean, RuntimeData> {

    @Override
    public Boolean invoke(Node node, RuntimeData runtimeData) {
        // 移除审批人后，变更审批模式
        int personCount = runtimeData.getFlowTaskNode().getApprovalPersons().size();
        switch (node.getProps().getMode()) {
            case DEFAULT:
                if (personCount > 1) {
                   runtimeData.setCurrentNodeMode(NodePropertiesModeEnum.AND);
                } else {
                    runtimeData.setCurrentNodeMode(NodePropertiesModeEnum.DEFAULT);
                }
                break;
            case NEXT:
            case AND:
            case OR:
                if (personCount == 1) {
                    runtimeData.setCurrentNodeMode(NodePropertiesModeEnum.DEFAULT);
                }
                break;
            default:
                break;
        }
        return true;
    }
}
