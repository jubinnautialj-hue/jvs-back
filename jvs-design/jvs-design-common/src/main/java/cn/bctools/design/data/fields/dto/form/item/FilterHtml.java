package cn.bctools.design.data.fields.dto.form.item;

import cn.bctools.design.data.fields.enums.DataQueryType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class FilterHtml {

    @ApiModelProperty("处理类型")
    TypeHtml type;

    @ApiModelProperty("字段id")
    private String fieldKey;

    @ApiModelProperty("查询条件值")
    private Object value;

    @ApiModelProperty("查询方式(eq,ne,like等)")
    private DataQueryType enabledQueryTypes;

}
