package cn.bctools.chart.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 页面配置
 *
 * @author zqs
 */
@Data
@Accessors(chain = true)
@ApiModel
public class DeleteChartPageDto {

    @ApiModelProperty("应用ID")
    private String jvsAppId;
    @ApiModelProperty("图表id")
    private List<String> ids;

}
