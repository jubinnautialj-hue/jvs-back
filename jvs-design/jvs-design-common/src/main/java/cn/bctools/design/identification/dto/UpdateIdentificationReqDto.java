package cn.bctools.design.identification.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * @author zhuxiaokang
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel("修改标识入参")
public class UpdateIdentificationReqDto extends CreateIdentificationReqDto {

    @ApiModelProperty(value = "标识id", required = true)
    @NotBlank(message = "标识主键不能为空")
    private String id;
}
