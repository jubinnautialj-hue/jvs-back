package cn.bctools.design.identification.dto;

import cn.bctools.design.data.fields.enums.DesignType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
@ApiModel("创建标识入参")
public class CreateIdentificationReqDto {

    @ApiModelProperty(value = "标识符", required = true)
    private String identifier;

    @ApiModelProperty(value = "设计id", required = true)
    @NotBlank(message = "标识符映射的设计不能为空")
    private String designId;

    @ApiModelProperty("设计名称")
    private String designName;

    @ApiModelProperty("设计类型")
    private DesignType designType;
}
