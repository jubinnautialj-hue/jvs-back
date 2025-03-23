package cn.bctools.report.model.univer.style;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@ApiModel("边框")
@Data
@Accessors(chain = true)
public class BD {

    @ApiModelProperty("上边框")
    private Style t;

    @ApiModelProperty(value = "上边框",notes = "START_TOP_LEFT_END_BOTTOM_CENTER")
    private Style tl_bc;

    @ApiModelProperty(value = "上边框",notes = "START_TOP_LEFT_END_BOTTOM_RIGHT")
    private Style tl_br;

    @ApiModelProperty(value = "上边框",notes = "START_TOP_LEFT_END_MIDDLE_RIGHT")
    private Style tl_mr;

    @ApiModelProperty("下边框")
    private Style b;

    @ApiModelProperty(value = "下边框",notes = "START_BOTTOM_CENTER_END_TOP_RIGHT")
    private Style bc_tr;

    @ApiModelProperty(value = "下边框",notes = "START_BOTTOM_LEFT_END_TOP_RIGHT")
    private Style bl_tr;

    @ApiModelProperty("左边框")
    private Style l;

    @ApiModelProperty(value = "左边框",notes = "START_MIDDLE_LEFT_END_TOP_RIGHT")
    private Style ml_tr;

    @ApiModelProperty("右边框")
    private Style r;

    @ApiModel("边框样式")
    @Data
    @Accessors(chain = true)
    public static class Style{
        @ApiModelProperty("边框样式")
        private Integer s;
        @ApiModelProperty("边框颜色")
        private RGB cl;
    }
}
