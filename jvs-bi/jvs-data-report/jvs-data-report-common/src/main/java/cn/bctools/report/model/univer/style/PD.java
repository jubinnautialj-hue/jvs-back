package cn.bctools.report.model.univer.style;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@ApiModel("内边距")
@Data
@Accessors(chain = true)
public class PD {

    @ApiModelProperty("上边距")
    private Integer t;

    @ApiModelProperty("下边距")
    private Integer b;

    @ApiModelProperty("左边距")
    private Integer l;

    @ApiModelProperty("右边距")
    private Integer r;

}
