package cn.bctools.chart.chart.bo;

import cn.bctools.chart.enums.QueryParameterTypeEnums;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class QueryParameterBo {
    /**
     * 值
     */
    private String value;
    /**
     * 格式类型
     */
    private String format;
    /**
     * 类型
     */
    private QueryParameterTypeEnums type;
}
