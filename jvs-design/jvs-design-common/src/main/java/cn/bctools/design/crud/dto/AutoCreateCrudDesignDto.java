package cn.bctools.design.crud.dto;

import cn.bctools.design.data.fields.enums.DataFieldType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author jvs
 * 自动生成
 */
@Data
@Accessors(chain = true)
public class AutoCreateCrudDesignDto {
    @ApiModelProperty(value = "应用id", required = true)
    private String appId;

    @ApiModelProperty(value = "模型id", notes = "无模型id会创建模型")
    private String modelId;

    @ApiModelProperty(value = "设计名称", required = true)
    private String designName;

    @ApiModelProperty(value = "目录id", notes = "不指定目录id，则不挂载目录")
    private String menuId;

    @ApiModelProperty(value = "字段集合")
    private List<Field> fields;

    @Data
    @Accessors(chain = true)
    public static class Field {
        @ApiModelProperty("字段名")
        String fieldKey;
        @ApiModelProperty("中文名")
        String fieldName;
        @ApiModelProperty("字段类型")
        String type;
        @ApiModelProperty("字段类型")
        DataFieldType fieldType;
        @ApiModelProperty("可选项")
        List<String> dicData;
    }
}
