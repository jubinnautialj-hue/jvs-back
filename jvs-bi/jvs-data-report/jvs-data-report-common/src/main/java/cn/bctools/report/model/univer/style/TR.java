package cn.bctools.report.model.univer.style;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel("文字旋转")
public class TR {

    @ApiModelProperty("文字旋转角度")
    private Integer a;

    @ApiModelProperty(value = "是否垂直",notes = "1 表示垂直，0 表示水平。默认值为 0。当 v 为 1 时，a 无效")
    private Integer v;

}
