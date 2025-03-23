package cn.bctools.chart.dto;

import cn.bctools.data.factory.dto.OrderField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@ApiModel("下钻配置")
@Data
@Accessors(chain = true)
public class DrillReturnDto {
    @ApiModelProperty("排序字段")
    private OrderField sort;
    @ApiModelProperty("查询条件的值")
    private List<Object> parameter;
}
