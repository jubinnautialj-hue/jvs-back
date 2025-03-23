package cn.bctools.screen.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel("封面与模板分类")
public class TemplateUseDto {
    @ApiModelProperty("模板id")
    private String templateId;
    @ApiModelProperty("菜单id")
    private String menuId;
    @ApiModelProperty("图表名称")
    private String chartName;
}
