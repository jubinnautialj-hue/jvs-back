package cn.bctools.data.factory.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * @author Administrator
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("首页统计查询条件")
public class StatisticsSumDto {
    @ApiModelProperty("总次数")
    private Long count;
    @ApiModelProperty("成功次数")
    private Long succeedCount;
    @ApiModelProperty("失败次数")
    private Long failCount;
    @ApiModelProperty("最高消耗时间")
    private Long maxTime;
    @ApiModelProperty("最低消耗时间")
    private Long minTime;
    @ApiModelProperty("平均消耗时间")
    private BigDecimal avgTime;
}
