package cn.bctools.design.identification.dto;

import cn.bctools.design.data.fields.enums.DesignType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
@ApiModel("分页查询标识入参")
public class PageIdentificationReqDto {
    @ApiModelProperty("设计类型")
    private DesignType designType;
    @ApiModelProperty("设计名称")
    private String designName;
    @ApiModelProperty("标识符")
    private String identifier;

}
