package cn.bctools.design.rule.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

/**
 * 逻辑执行结果
 *
 * @author guojing
 */
@Data
@Accessors(chain = true)
@ApiModel("逻辑执行结果")
public class RuleResultDto implements Serializable {

    @ApiModelProperty("执行后的变量参数")
    Map<String, Object> variableMap;
    @ApiModelProperty(value = "逻辑结果后的结果", notes = "每一个逻辑执行之后的结果都不一样，自行操作")
    Object result;

}
