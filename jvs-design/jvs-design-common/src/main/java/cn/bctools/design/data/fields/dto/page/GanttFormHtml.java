package cn.bctools.design.data.fields.dto.page;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author jvs
 * 列表-甘特图配置
 */
@Data
@Accessors(chain = true)
public class GanttFormHtml {

    @ApiModelProperty(value = "计划开始时间字段")
    private String plainStart;

    @ApiModelProperty(value = "计划结束时间字段")
    private String plainEnd;

    @ApiModelProperty(value = "实际开始时间字段")
    private String reallyStart;

    @ApiModelProperty(value = "实际结束时间字段")
    private String reallyEnd;

    @ApiModelProperty(value = "实际百分比")
    private String actualPlanPercent;

    @ApiModelProperty(value = "计划颜色")
    private String plainColor;

    @ApiModelProperty(value = "实际颜色")
    private String reallyColor;

    @ApiModelProperty(value = "动态颜色开关")
    private boolean conditionControlEnable;

    @ApiModelProperty(value = "动态颜色")
    private List<ConditionControlHtml> conditionControl;

}
