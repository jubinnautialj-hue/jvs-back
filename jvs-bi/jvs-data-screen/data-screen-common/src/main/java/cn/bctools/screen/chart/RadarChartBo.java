package cn.bctools.screen.chart;

import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.data.factory.doris.condition.DorisCollectCondition;
import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.screen.chart.bo.FieldsData;
import cn.bctools.screen.chart.po.ChartDesignInParameter;
import cn.bctools.screen.chart.po.ChartReturnObj;
import cn.bctools.screen.chart.po.GetDataParameter;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    public void buildSql(GetDataParameter getDataParameter, StringBuilder sql) {
        StringBuilder newSql = new StringBuilder();
        ChartElementInterface.super.buildSql(getDataParameter, sql);
        //二次修改
        String max = getDataParameter.getLogicSetting().getYAxis().stream().map(DataSourceField::getFieldKey).collect(Collectors.joining("','"));
        newSql.append("SELECT max_by(`").append(max).append("` as `").append(MAX_KEY).append("`");
        //指标与维度
        List<FieldsData> xAxis = getDataParameter.getLogicSetting().getXAxis();
        String groupKey = "";
        if (!xAxis.isEmpty()) {
            groupKey = xAxis.stream().map(DataSourceField::getFieldKey).collect(Collectors.joining("`,`"));
            groupKey = "`" + groupKey + "`";
            newSql.append(groupKey).append(",");
        }
        //指标数据
        for (FieldsData yAxi : getDataParameter.getLogicSetting().getYAxis()) {
            DorisCollectCondition condition = SpringContextUtil.getBean(yAxi.getCalculateType().getDorisClass());
            newSql.append(condition.addCondition(yAxi.getFieldKey(), 0, Boolean.TRUE, yAxi.getFieldKey())).append(",");
        }
        newSql.delete(newSql.length() - 1, newSql.length());
        newSql.append(" FROM (").append(sql).append(") as a");
        //添加分组
        sql.append(" GROUP BY ").append(groupKey);
    }

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
        private Object name;
    }

    @Data
    private static class IndicatorData {
        private Object max;
        private Object name;
    }
}
