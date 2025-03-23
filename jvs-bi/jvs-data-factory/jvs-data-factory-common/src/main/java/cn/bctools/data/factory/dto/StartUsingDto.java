package cn.bctools.data.factory.dto;


import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 内置数据类型转换返回值
 *
 * @author admin
 */
@Data
@Accessors(chain = true)
@ApiModel("过滤条件的数据集返回dto")
public class StartUsingDto {
    @ApiModelProperty("id")
    private String id;
    @ApiModelProperty("数据集名称")
    private String name;
    @ApiModelProperty("格式类型例如时间类型:yyyy-MM-dd yyyy/MM/dd")
    private String format;
    @ApiModelProperty("字段key或者表名称 第一级就是表名称 第二级就是字段key")
    private String key;
    @ApiModelProperty("字段类型")
    private DataFieldTypeEnum fieldType;
    @ApiModelProperty("子级")
    private List<StartUsingDto> children;
}
