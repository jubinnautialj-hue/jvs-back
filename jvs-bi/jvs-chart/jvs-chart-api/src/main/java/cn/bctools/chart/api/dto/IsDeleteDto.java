package cn.bctools.chart.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xiaohui
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("判断是否存在数据")
public class IsDeleteDto {
    @ApiModelProperty("id")
    private String id;
    @ApiModelProperty("自己数据")
    private String subId;
    @ApiModelProperty("是否为定时任务触发")
    private Boolean isJobExec;
}
