package cn.bctools.design.project.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 应用新增数据
 *
 * @Author: GuoZi
 */
@Data
@Accessors(chain = true)
@ApiModel("应用新增")
public class JvsAppSaveDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "应用名称不能为空")
    @Length(max = 64, message = "应用名称最多64字")
    @ApiModelProperty("应用名称")
    private String name;
    @Length(max = 500, message = "应用名称最多500字")
    @ApiModelProperty("应用描述")
    private String description;
    @ApiModelProperty("图标")
    private String icon;
    @ApiModelProperty("LOGO")
    private String logo;
    @ApiModelProperty("[使用模板创建时]模板id")
    private String templateId;

}
