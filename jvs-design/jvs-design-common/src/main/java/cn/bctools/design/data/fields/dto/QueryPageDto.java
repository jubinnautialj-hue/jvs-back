package cn.bctools.design.data.fields.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * 分页查询条件
 * <p>
 * 字段为空时, 默认所有字段
 *
 * @Author: GuoZi
 */
@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
@Accessors(chain = true)
@ApiModel("分页查询条件")
public class QueryPageDto extends QueryListDto {

    @ApiModelProperty("每页数量")
    private Integer size = 10;

    @ApiModelProperty("查询页数")
    private Integer current = 1;

}
