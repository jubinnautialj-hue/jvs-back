package cn.bctools.report.model.univer.style;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@ApiModel("线颜色")
@Data
@Accessors(chain = true)
public class RGB {
    @ApiModelProperty(value = "颜色",notes = "univer 渲染时只会取这个值")
    private String rgb;

    @ApiModelProperty(value = "hsla颜色",notes = "前端自定义的调色盘")
    private String color;
}