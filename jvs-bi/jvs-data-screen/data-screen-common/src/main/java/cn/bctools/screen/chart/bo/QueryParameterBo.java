package cn.bctools.screen.chart.bo;

import cn.bctools.screen.enums.QueryParameterTypeEnums;
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
