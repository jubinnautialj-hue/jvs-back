package cn.bctools.design.rule.impl.workflow.termination;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
public class FlowTerminationDto {

    @ParameterValue(info = "流程参数值", type = InputType.map, explain = "流程请求参数对象")
    public Map<String, Object> data;

    @ParameterValue(info = "终止流程任务原因", type = InputType.input, explain = "终止流程任务原因")
    public String reason;

}
