package cn.bctools.design.data.fields.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author auto
 */
@Data
@ApiModel("数据来源-逻辑引擎")
public class RuleDesignHtml {

    @ApiModelProperty("标识")
    private String ruleId;
    @ApiModelProperty("名称")
    private String ruleName;

}
