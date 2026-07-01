package cn.bctools.design.rule.impl.workflow.delete;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class FlowDeleteDto {

    @ParameterValue(info = "流程设计id", type = InputType.input, explain = "根据业务删除的工作流ID")
    String flowId;

}
