package cn.bctools.screen.dto;

import cn.bctools.screen.chart.bo.DrillSetting;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * @author admin
 */
@Data
@Accessors(chain = true)
@ApiModel("获取单个图表的数据")
public class MonomerDto {

    @ApiModelProperty("查询条件")
    private Map<String, String> queryData;

    @ApiModelProperty("联动数据")
    private LinkDataDto linkData;

    @ApiModelProperty("钻取的数据")
    private DrillSetting drillSetting;
    @ApiModelProperty("设计id")
    private String designId;

    @ApiModelProperty("租户id")
    private String tenantId;

}
