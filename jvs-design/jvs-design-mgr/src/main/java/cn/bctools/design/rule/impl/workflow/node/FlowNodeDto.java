package cn.bctools.design.rule.impl.workflow.node;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.design.rule.impl.workflow.run.FlowRunParameterSelected;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * @author wl
 */
@Data
@Accessors(chain = true)
public class FlowNodeDto {

    @ParameterValue(info = "请选OA流程", type = InputType.selected, explain = "只能选择当前应用下的OA流程", cls = FlowRunParameterSelected.class)
    @NotNull(message = "OA流程不能为空")
    public String workflow;

    @ParameterValue(info = "任务ID数据", necessity = false, type = InputType.input, explain = "任务ID可以为空,如果不填写，默认创建一个新的流程，必需选择工作流节点时才能使用")
    public String taskId;

//    @ParameterValue(info = "流程参数值", type = InputType.map, explain = "流程请求参数对象")
//    public Map<String, Object> data;

    @ParameterValue(info = "节点id", explain = "用于检查是否走向此节点,如果没有将不执行")
    public String nodeId;
//
//    @ParameterValue(info = "自选审批人")
//    public List<PersonnelDto> approvers;
//
//
//    @ParameterValue(info = "指定审核人", necessity = false, type = InputType.flowNode, explain = "每一个节点的审核人")
//    private List<SelfSelectApprover> approvers;

}
