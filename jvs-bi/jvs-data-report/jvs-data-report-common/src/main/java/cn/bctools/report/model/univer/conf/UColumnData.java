package cn.bctools.report.model.univer.conf;

import cn.bctools.report.model.univer.style.UStyle;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
@ApiModel("列设置")
public class UColumnData {

    @ApiModelProperty(value = "自定义设置",notes = "Map<String,Object>")
    private Map<String,Object> custom;

    @ApiModelProperty(value = "是否隐藏")
    private Boolean hd;

    @ApiModelProperty("样式")
    private UStyle s;

    @ApiModelProperty("宽度")
    private Integer w;
}
