package cn.bctools.design.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author hs
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "流水号格式")
public class OrderFormat implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "前缀")
    private String orderPrefix;
    @ApiModelProperty(value = "后缀")
    private String orderSuffix;
    @ApiModelProperty(value = "时间标识")
    private OrderTimeMarkEnum orderTimeMark;
    @ApiModelProperty(value = "序号位数")
    private Integer orderDigit;
    @ApiModelProperty(value = "流水号重置规则")
    private OrderResetRuleEnum orderResetRule;
}
