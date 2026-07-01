package cn.bctools.rule.dto;

import cn.bctools.rule.entity.enums.DataQueryType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jvs
 */
@Slf4j
@Data
@Accessors(chain = true)
public class QueryConditionDto {

    private LinkTypeEnum prop;

    @ApiModelProperty("字段id")
    private String fieldKey;

    @ApiModelProperty("查询条件值")
    private Object value;

    @ApiModelProperty("查询方式(eq,ne,like等)")
    private DataQueryType enabledQueryTypes;
}
