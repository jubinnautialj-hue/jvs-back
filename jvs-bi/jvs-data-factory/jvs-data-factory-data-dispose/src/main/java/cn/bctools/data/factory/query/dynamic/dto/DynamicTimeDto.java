package cn.bctools.data.factory.query.dynamic.dto;

import cn.bctools.data.factory.query.dynamic.enums.DynamicTimeTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author xiaohui
 */
@Data
@Accessors(chain = true)
@ApiModel("动态入参的数据结构")
public class DynamicTimeDto {
    @ApiModelProperty("类型")
    private DynamicTimeTypeEnum type;
    @ApiModelProperty("字段格式")
    private String format;
}
