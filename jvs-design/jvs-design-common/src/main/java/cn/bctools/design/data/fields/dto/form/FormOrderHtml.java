package cn.bctools.design.data.fields.dto.form;

import cn.bctools.design.common.OrderResetRuleEnum;
import cn.bctools.design.common.OrderTimeMarkEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 流水号配置
 * <p>
 * 流水号：前缀+时间标识+序号
 * 例：ABCD2022040700001
 *
 * @Author: GuoZi
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "表单设计-流水号配置")
public class FormOrderHtml {

    @ApiModelProperty(value = "流水号前缀")
    private String orderPrefix;
    @ApiModelProperty(value = "时间标识")
    private OrderTimeMarkEnum orderTimeMark;
    @ApiModelProperty(value = "序号位数")
    private Integer orderDigit;
    @ApiModelProperty(value = "流水号重置规则")
    private OrderResetRuleEnum orderResetRule;

}
