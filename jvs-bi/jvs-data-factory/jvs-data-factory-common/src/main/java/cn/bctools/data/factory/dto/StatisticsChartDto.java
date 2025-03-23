package cn.bctools.data.factory.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author admin
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("统计页面的图表返回值")
public class StatisticsChartDto {
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("具体数据")
    private List<IndexData> index;

    @Data
    public static class IndexData {
        private String x;
        private Object y;
    }
}
