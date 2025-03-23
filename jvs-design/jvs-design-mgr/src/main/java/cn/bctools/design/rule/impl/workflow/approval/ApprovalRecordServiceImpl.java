package cn.bctools.design.rule.impl.workflow.approval;


import cn.bctools.design.workflow.service.ApprovalRecordService;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jvs
 */
@Rule(value = "查询流程审批记录",
        group = RuleGroup.服务插件,
        returnType = ClassType.对象,
        statsMsg = "当前应用未找到流程，请先创建一个流程",
        order = 2,
        test = true,
        explain = "选择的 oa 流程，根据数据 id查询这个流程的审批信息，并将查询的结果使用新的字段名返回新的数据结构。"
)
@AllArgsConstructor
public class ApprovalRecordServiceImpl implements BaseCustomFunctionInterface<ApprovalRecordDto> {

    ApprovalRecordService approvalRecordService;

    @Override
    public Object execute(ApprovalRecordDto approvalRecordDto, Map<String, Object> params) {
        Map<String, Object> flowRecord = approvalRecordService.getFlowRecord(approvalRecordDto.getDataId(), approvalRecordDto.getWorkflow());
        Map<String, Object> objectMap = new HashMap<>();
        approvalRecordDto.getNodeId().forEach((key, value) -> objectMap.put(value, flowRecord.getOrDefault(key, "")));
        return objectMap;
    }
}
