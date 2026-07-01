package cn.bctools.design.data.fields.dto;

import cn.bctools.rule.dto.LinkTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jvs
 * 动态数据查询条件扩展
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class QueryConditionExtendDto extends QueryConditionDto {

    @ApiModelProperty("条件值类型")
    private LinkTypeEnum prop;

    @ApiModelProperty("公式内容")
    String formulaContent;

    @ApiModelProperty("公式id")
    String formula;
}
