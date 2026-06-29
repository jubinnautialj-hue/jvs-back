package cn.bctools.document.dto;


import cn.bctools.document.entity.DcTemplateVariable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@ApiModel("模板变量")
public class DcTemplateVariableDto {
    @ApiModelProperty("模板名称")
    private String templateName;
    @ApiModelProperty("模板id")
    private String templateId;
    @ApiModelProperty("变量集合")
    private List<DcTemplateVariable> variableList;
}
