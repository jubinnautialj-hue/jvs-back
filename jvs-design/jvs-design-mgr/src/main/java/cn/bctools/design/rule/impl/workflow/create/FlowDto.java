package cn.bctools.design.rule.impl.workflow.create;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class FlowDto {

    @ParameterValue(info = "流程名称", type = InputType.input, explain = "流程名称")
    public String flowName;
    @ParameterValue(info = "流程分组", type = InputType.input, explain = "流程分组")
    public String flowGroup;
    @ParameterValue(info = "流程设置", type = InputType.flowTable, explain = "创建流程,需要使用表单的流程设置高级组件进行配置")
    public List<Object> nodeObjs;

}
