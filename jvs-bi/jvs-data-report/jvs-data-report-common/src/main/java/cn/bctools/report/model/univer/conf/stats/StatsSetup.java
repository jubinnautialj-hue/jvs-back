package cn.bctools.report.model.univer.conf.stats;

import cn.bctools.report.model.univer.style.UStyle;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
@ApiModel("统计设置")
public class StatsSetup implements Serializable {

    private static final long serialVersionUID = -4935918601824871979L;

    @ApiModelProperty("是否启用")
    private Boolean enabled = Boolean.FALSE;

    @ApiModelProperty("统计别名")
    private String alias;

    @ApiModelProperty("样式")
    private UStyle style;

    @ApiModelProperty("统计项列表 有序")
    private List<TotalSetup> setupList;

}
