package cn.bctools.data.factory.source.data.po;


import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import cn.bctools.data.factory.source.enums.InParameterTypeEnums;
import cn.bctools.data.factory.source.enums.RequestTypeEnums;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel("条件")
@Accessors(chain = true)
public class InParameterJsonPo implements Serializable {
    private static final long serialVersionUID = 8309091978078491729L;
    @ApiModelProperty("字段key")
    private String key;
    @ApiModelProperty("字段显示名称")
    private String name;
    @ApiModelProperty("字段解释")
    private String explain;
    @ApiModelProperty("字段默认值")
    private Object defaultValue;
    @ApiModelProperty("子集数据")
    private List<InParameterJsonPo> items;
    @ApiModelProperty("是否为必填")
    private Boolean requiredIs;
    @ApiModelProperty("入参分类")
    private InParameterTypeEnums inParameterTypeEnums;
    @ApiModelProperty("请求参数类型")
    private RequestTypeEnums requestTypeEnums;
    @ApiModelProperty("本次请求的值")
    private Object value;
    @ApiModelProperty("字段类型")
    private DataFieldTypeEnum fieldType;
}
