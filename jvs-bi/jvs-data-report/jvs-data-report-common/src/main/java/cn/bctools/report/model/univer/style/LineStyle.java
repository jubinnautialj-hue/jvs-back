package cn.bctools.report.model.univer.style;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author wl
 */
@Data
@Accessors(chain = true)
@ApiModel("线对象")
public class LineStyle implements Serializable {

    private static final long serialVersionUID = -3180615321387034142L;
    @ApiModelProperty("是否展示线")
    private Integer s;

    @ApiModelProperty("颜色是否跟随字体颜色。当 `c` 为 1（TRUE） 时 cl 不起作用。默认值为 1")
    private Integer c = 1;

    @ApiModelProperty("线颜色")
    private RGB cl;

    @ApiModelProperty("线类型")
    private Integer t;
}