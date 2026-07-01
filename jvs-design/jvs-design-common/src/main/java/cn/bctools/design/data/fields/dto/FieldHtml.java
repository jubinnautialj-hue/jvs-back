package cn.bctools.design.data.fields.dto;

import cn.bctools.design.data.fields.enums.DataFieldType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 基础组件属性，用于划分不同的树结构
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class FieldHtml {

    @ApiModelProperty("字段Key")
    private String prop;

    @ApiModelProperty("字段类型")
    private DataFieldType type;

    @ApiModelProperty("字段名")
    private String name;

    @ApiModelProperty("该组件的绝对路径")
    private String path;

    @ApiModelProperty("子集的数据字段结构用于展示，和优化")
    private List<FieldHtml> children;

}
