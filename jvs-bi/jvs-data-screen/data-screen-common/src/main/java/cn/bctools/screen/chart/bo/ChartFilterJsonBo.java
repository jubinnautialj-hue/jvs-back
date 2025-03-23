package cn.bctools.screen.chart.bo;

import cn.bctools.data.factory.query.QueryExecDto;
import cn.bctools.screen.query.enums.ChartQueryTypeEnums;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class ChartFilterJsonBo {
    /**
     * 字段信息
     */
    private List<FilterData> dataFilter;
    /**
     * 是否为and
     */
    private Boolean isAnd;


    @Data
    @Accessors(chain = true)
    @ApiModel("筛选条件")
    public static class FilterData extends QueryExecDto {
        /***
         * key值
         * */
        private String id;
        /**
         * 是否勾选
         */
        private Boolean isCheck;
        /**
         * 条件类型
         */
        private ChartQueryTypeEnums chartQueryType;
    }
}


