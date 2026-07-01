package cn.bctools.design.data.fields.dto;

import cn.bctools.design.data.fields.enums.DataFieldType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author jvs
 */
@Data
@Accessors(chain = true)
@ApiModel("新增模型")
public class SaveDataModelDto {
    @ApiModelProperty(value = "模型名称", required = true)
    @NotBlank(message = "模型名称不能为空")
    private String name;

    @ApiModelProperty(value = "true-生成crud设计，false-不生成crud设计")
    private Boolean generateCrudDesign;

    @ApiModelProperty(value = "目录id")
    private String menuId;

    @ApiModelProperty("模型字段集合")
    @Valid
    private List<Field> fields;

    @Data
    @Accessors(chain = true)
    public static class Field {
        @ApiModelProperty(value = "字段名", required = true)
        @NotBlank(message = "字段名不能为空")
        String fieldKey;
        @ApiModelProperty(value = "中文名", required = true)
        @NotBlank(message = "字段中文名不能为空")
        String fieldName;
        @ApiModelProperty(value = "字段类型", required = true)
        @NotNull(message = "字段类型不能为空")
        DataFieldType fieldType;
    }
}
