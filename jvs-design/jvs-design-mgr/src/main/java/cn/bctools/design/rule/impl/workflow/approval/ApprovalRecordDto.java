package cn.bctools.design.rule.impl.workflow.approval;

import cn.bctools.design.rule.impl.workflow.run.FlowRunParameterSelected;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class ApprovalRecordDto {

    @ParameterValue(info = "请选OA流程", type = InputType.selected, explain = "只能选择当前应用下的OA流程", cls = FlowRunParameterSelected.class, linkFields = {"nodeId"}, linkCls = FlowNodeLinkSelected.class)
    @NotNull(message = "OA流程不能为空")
    public String workflow;
    @ParameterValue(info = "选择审批节点", type = InputType.mapKeySelected, explain = "参数名为审批节点信息，参数值为转换字段")
    public Map<String, String> nodeId;
    @ParameterValue(info = "数据id", explain = "查询流程的数据id")
    public String dataId;

}
