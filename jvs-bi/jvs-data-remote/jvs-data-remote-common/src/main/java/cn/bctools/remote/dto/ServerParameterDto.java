package cn.bctools.remote.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel("数据服务参数")
public class ServerParameterDto {

    @ApiModelProperty("键")
    private String key;

    @ApiModelProperty("字段名称")
    private String name;
    @ApiModelProperty("字段别名")
    private String asKey;

    @ApiModelProperty("字段类型")
    private String fieldType;

    @ApiModelProperty("是否必填")
    private Boolean requiredIs;
}
