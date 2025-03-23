package cn.bctools.chart.chart;

import cn.bctools.chart.chart.bo.FieldsData;
import cn.bctools.chart.chart.po.ChartReturnObj;
import cn.bctools.chart.chart.po.GetDataParameter;
import cn.bctools.chart.enums.UnitEnums;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 雷达图
 * {
 * xAxisData:[],
 * yAxisData:[],
 * indicatorData: [
 * { name: 'Sales', max: 6500 },
 * { name: 'Administration', max: 16000 },
 * { name: 'Information Technology', max: 30000 },
 * { name: 'Customer Support', max: 38000 },
 * { name: 'Development', max: 52000 },
 * { name: 'Marketing', max: 25000 }
 * ],
 * series:[
 * {
 * value: [4200, 3000, 20000, 35000, 50000, 18000],
 * name: 'Allocated Budget'
 * },
 * {
 * value: [5000, 14000, 28000, 26000, 42000, 21000],
 * name: 'Actual Spending'
 * }
 * ]
 * }
 *
 * @author ：xh
 */
@Slf4j
@Data
@Component
@AllArgsConstructor
public class RadarChartBo implements ChartElementInterface {
    /**
     * 最大值 key 名称
     */
    private final static String MAX_KEY = "maxValue";
    private DorisJdbcTemplate dorisJdbcTemplate;


    @Override
    public ChartReturnObj exec(GetDataParameter getDataParameter) {
        //返回值
        ChartReturnObj<Series> chartReturnObj = new ChartReturnObj<Series>().setSeries(new ArrayList<>()).setYAxisData(new ArrayList<>()).setIndicatorData(new ArrayList<>()).setXAxisData(new ArrayList<>());
        StringBuilder sql = new StringBuilder();
        ChartElementInterface.super.buildSql(getDataParameter, sql);
        List<Map<String, Object>> mapList = dorisJdbcTemplate.getDataPage(getDataParameter.getShowNumber(), 1L, sql.toString(), getDataParameter.getParameter(), getDataParameter.getSortFields());
        if (!mapList.isEmpty()) {
            List<Series> seriesList = new ArrayList<>();
            //组装数据 每个点的最大值
            List<IndicatorData> indicatorData =
                    mapList.stream().map(e -> {
                        IndicatorData data = new IndicatorData();
                        data.setMax(e.get(MAX_KEY));
                        data.setName(e.get(getDataParameter.getLogicSetting().getXAxis().get(0).getFieldKey()));
                        return data;
                    }).collect(Collectors.toList());
            //每个指标的详细值
            for (FieldsData yAxi : getDataParameter.getLogicSetting().getYAxis()) {
                Series series = new Series();
                List<Object> list = mapList.stream().map(e -> e.getOrDefault(yAxi.getFieldKey(), 0)).collect(Collectors.toList());
                series.setValue(list);
                series.setName(yAxi.getAliasName());
                if (yAxi.getFormatParams() != null && yAxi.getFormatParams().getUnit() != null) {
                    series.setUnit(yAxi.getFormatParams().getUnit());
                }
                seriesList.add(series);
            }
            List<JSONObject> collect = indicatorData.stream().map(e -> JSONObject.parseObject(JSONObject.toJSONString(e))).collect(Collectors.toList());
            chartReturnObj.setIndicatorData(collect)
                    .setSeries(seriesList);
        }
        return chartReturnObj;
    }

    @Data
    private static class Series {
        private List<Object> value;
        /**
         * 单位
         */
        private UnitEnums unit;
        private Object name;
    }

    @Data
    private static class IndicatorData {
        private Object max;
        private Object name;
    }
}
