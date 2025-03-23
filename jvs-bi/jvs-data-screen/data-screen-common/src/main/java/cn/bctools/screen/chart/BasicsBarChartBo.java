package cn.bctools.screen.chart;

import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.screen.chart.bo.FieldsData;
import cn.bctools.screen.chart.po.ChartDesignInParameter;
import cn.bctools.screen.chart.po.ChartReturnObj;
import cn.bctools.screen.chart.po.GetDataParameter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author ：xh
 * [description]：基础条形图
 * @modified By：
 * @version: 1.0.0$
 */
@Slf4j
@Data
@Component
@AllArgsConstructor
public class BasicsBarChartBo implements ChartElementInterface {
    private DorisJdbcTemplate dorisJdbcTemplate;

    @Override
    public ChartReturnObj exec(GetDataParameter getDataParameter) {
        //返回值
        ChartReturnObj<Series> chartReturnObj = new ChartReturnObj<Series>()
                .setSeries(new ArrayList<>())
                .setYAxisData(new ArrayList<>())
                .setXAxisData(new ArrayList<>());
        ChartDesignInParameter chartDesignInParameter = getDataParameter.getLogicSetting();
        //基础sql
        StringBuilder sql = new StringBuilder();
        List<FieldsData> xAxis = chartDesignInParameter.getXAxis();
        List<FieldsData> yAxis = chartDesignInParameter.getYAxis();
        ChartElementInterface.super.buildSql(getDataParameter, sql);
        String fieldKey = xAxis.get(0).getFieldKey();
        if (xAxis.size() > 1) {
            multipleGroup(sql.toString(), xAxis, yAxis, getDataParameter, chartReturnObj);
            //合并数据
        } else {
            this.singleGroup(sql.toString(), fieldKey, yAxis, getDataParameter, chartReturnObj);
        }
        return chartReturnObj;

    }

    /**
     * 2个分组的计算
     *
     * @param yAxis            指标
     * @param sql              初级计算的sql
     * @param xAxis            维度
     * @param chartReturnObj   返回值
     * @param getDataParameter 入参
     */
    private void multipleGroup(String sql, List<FieldsData> xAxis, List<FieldsData> yAxis, GetDataParameter getDataParameter, ChartReturnObj<Series> chartReturnObj) {
        List<Map<String, Object>> maps = dorisJdbcTemplate.getDataPage(getDataParameter.getShowNumber(), 1L, sql, getDataParameter.getParameter(), getDataParameter.getSortFields());
        if (maps.isEmpty()) {
            return;
        }
        //获取数据 指标
        List<Object> xAxisData = maps.stream().map(e -> e.get(xAxis.get(0).getFieldKey())).distinct().collect(Collectors.toList());
        //获取二级维度
        List<Object> stackData = maps.stream().map(e -> e.get(xAxis.get(1).getFieldKey())).distinct().collect(Collectors.toList());
        List<Series> seriesList = new ArrayList<>();
        for (Object stackDatum : stackData) {
            //获取数据
            List<Map<String, Object>> list = maps.stream().filter(e -> e.get(xAxis.get(1).getFieldKey()).equals(stackDatum)).collect(Collectors.toList());
            for (FieldsData yAxi : yAxis) {
                Series series = new Series();
                List<Object> data = new ArrayList<>();
                for (Object xAxisDatum : xAxisData) {
                    Optional<Map<String, Object>> first = list.stream().filter(v -> v.get(xAxis.get(0).getFieldKey()).equals(xAxisDatum)).findFirst();
                    Object object = 0;
                    if (first.isPresent()) {
                        object = first.get().get(yAxi.getFieldKey());
                    }
                    data.add(object);
                }
                series.setStack(stackDatum)
                        .setData(data)
                        .setName(stackDatum + yAxi.getAliasName());
                seriesList.add(series);
            }
        }
        chartReturnObj
                .setYAxisData(xAxisData)
                .setSeries(seriesList);

    }

    /**
     * 单个指标的计算
     *
     * @param fieldKey         分组key
     * @param yAxis            指标
     * @param sql              初级计算的sql语句
     * @param chartReturnObj   返回值
     * @param getDataParameter 入参
     */
    private void singleGroup(String sql, String fieldKey, List<FieldsData> yAxis, GetDataParameter getDataParameter, ChartReturnObj<Series> chartReturnObj) {
        List<Map<String, Object>> maps = dorisJdbcTemplate.getDataPage(getDataParameter.getShowNumber(), 1L, sql.toString(), getDataParameter.getParameter(), getDataParameter.getSortFields());
        if (!maps.isEmpty()) {
            //指标
            List<Object> xAxisData = maps.stream().map(e -> e.get(fieldKey)).collect(Collectors.toList());
            List<Series> seriesList = new ArrayList<>();
            //数据
            for (FieldsData yAxi : yAxis) {
                List<Object> collect = maps.stream().map(e -> e.get(yAxi.getFieldKey())).collect(Collectors.toList());
                Series series = new Series().setData(collect)
                        .setName(yAxi.getAliasName());
                seriesList.add(series);
            }
            chartReturnObj.setYAxisData(xAxisData)
                    .setSeries(seriesList);
        }
    }


    /**
     * 具体的数据结构
     */
    @Data
    @Accessors(chain = true)
    private static class Series {
        /**
         * 名称
         */
        private Object name;
        /**
         * 类别
         */
        private Object stack;
        /**
         * 数据
         */
        private List<Object> data;
    }


}
