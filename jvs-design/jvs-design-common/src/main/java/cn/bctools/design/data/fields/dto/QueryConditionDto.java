package cn.bctools.design.data.fields.dto;

import cn.bctools.design.data.fields.enums.DataQueryType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * 动态数据查询条件
 *
 * @Author: GuoZi
 */
@Slf4j
@Data
@Accessors(chain = true)
public class QueryConditionDto {

    @ApiModelProperty("字段id")
    private String fieldKey;

    @ApiModelProperty("查询条件值")
    private Object value;

    @ApiModelProperty("查询方式(eq,ne,like等)")
    private DataQueryType enabledQueryTypes;

    @ApiModelProperty("是否是列表过滤")
    private Boolean crud = false;

}
