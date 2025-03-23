package cn.bctools.report.model.univer.conf;

import cn.bctools.report.model.univer.style.UStyle;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

@Data
@Accessors(chain = true)
@ApiModel("行设置")
public class URowData implements Serializable {

    private static final long serialVersionUID = 393715994849904394L;

    @ApiModelProperty("自动高度")
    private Integer ah;

    @ApiModelProperty(value = "自定义设置",notes = "Map<String,Object>")
    private Map<String,Object> custom;

    @ApiModelProperty("高度 px")
    private Integer h;

    @ApiModelProperty("是否隐藏")
    private Boolean hd;

    @ApiModelProperty("是否根据内容自定义高度")
    private Boolean ia;

    @ApiModelProperty("样式")
    private UStyle s;
}
