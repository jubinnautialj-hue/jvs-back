package cn.bctools.chart.chart.bo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 数据钻取配置信息
 *
 * @author admin
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("数据钻取")
public class DrillSetting {
    @ApiModelProperty("原字段信息")
    private FieldsData sourceField;
    @ApiModelProperty("值")
    private Object value;
    @ApiModelProperty("目标字段信息")
    private FieldsData targetField;

    public boolean check() {
        return this.sourceField != null && this.targetField != null;
    }
}
