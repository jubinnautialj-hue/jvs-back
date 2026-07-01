package cn.bctools.design.data.fields.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <显示值-传递值>
 *
 * @author auto
 **/
@Data
@Accessors(chain = true)
public class KvHtml {
    @ApiModelProperty("显示值")
    private Object label;
    @ApiModelProperty("传递值")
    private Object value;
}
