package cn.bctools.chart.chart;

import cn.bctools.chart.chart.bo.FieldsData;
import cn.bctools.chart.chart.po.ChartDesignInParameter;
import cn.bctools.chart.chart.po.ChartReturnObj;
import cn.bctools.chart.chart.po.GetDataParameter;
import cn.bctools.chart.enums.UnitEnums;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
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
 * [description]： 面积图，柱状图(基础柱状图，百分比柱状图，柱折，多Y轴)，条形图，折线图，散点图 数据结构如下
 * {
 * xAxisData:['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
 * yAxisData:[],
 * series:[
 * {
 * name:'Email',
 * data:[120, 132, 101, 134, 90, 230, 210]
 * },
 * {
 * name:'Union Ads',
 * data:[220, 182, 191, 234, 290, 330, 310]
 * },
 * {
 * name:'Video Ads',
 * data:[150, 232, 201, 154, 190, 330, 410]
 * },
 * {
 * name:'Direct',
 * data:[320, 332, 301, 334, 390, 330, 320]
 * },
 * {
 * name:'Search Engine',
 * data:[820, 932, 901, 934, 1290, 1330, 1320]
 * },
 * ]
 * }
 * @modified By：
 * @version: 1.0.0$
 */
@Slf4j
@Data
@Component
@AllArgsConstructor
public class BasicsHistogramChartBo implements ChartElementInterface {
    private final DorisJdbcTemplate dorisJdbcTemplate;


    @Override
    public ChartReturnObj exec(GetDataParameter getDataParameter) {
        //返回值
        ChartReturnObj<BasicsHistogramChartBo.Series> chartReturnObj = new ChartReturnObj<BasicsHistogramChartBo.Series>()
                .setSeries(new ArrayList<>())
                .setYAxisData(new ArrayList<>())
                .setHeader(new ArrayList<>())
                .setXAxisData(new ArrayList<>());
        StringBuilder sql = new StringBuilder();
        ChartDesignInParameter chartDesignInParameter = getDataParameter.getLogicSetting();
        //基础sql
        List<FieldsData> xAxis = chartDesignInParameter.getXAxis();
        List<FieldsData> yAxis = chartDesignInParameter.getYAxis();
        getDataParameter.setLogicSetting(chartDesignInParameter);
        ChartElementInterface.super.buildSql(getDataParameter, sql);
        //判断是否存在多个维度 目前最多支持两个 如果是多个维度需要 二次加工
        if (xAxis.size() > 1) {
            multipleGroup(sql.toString(), xAxis, yAxis, getDataParameter, chartReturnObj);
            //合并数据
        } else {
            this.singleGroup(sql.toString(), xAxis.get(0).getFieldKey(), yAxis, getDataParameter, chartReturnObj);
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
            List<Map<String, Object>> list = maps.stream().filter(e -> {
                Object object = e.get(xAxis.get(1).getFieldKey());
                if (stackDatum != null) {
                    return object != null && object.equals(stackDatum);
                } else {
                    return object == null;
                }
            }).collect(Collectors.toList());
            for (FieldsData yAxi : yAxis) {
                Series series = new Series();
                List<Object> data = new ArrayList<>();
                for (Object xAxisDatum : xAxisData) {
                    Optional<Map<String, Object>> first = list.stream().filter(v -> {
                        Object object = v.get(xAxis.get(0).getFieldKey());
                        if (xAxisDatum != null) {
                            return object != null && object.equals(xAxisDatum);
                        } else {
                            return object == null;
                        }
                    }).findFirst();
                    Object object = 0;
                    if (first.isPresent()) {
                        object = first.get().get(yAxi.getFieldKey());
                    }
                    data.add(object);
                }
                series.setStack(stackDatum)
                        .setData(data)
                        .setKey(yAxi.getFieldKey())
                        .setName(stackDatum + yAxi.getAliasName());
                if (yAxi.getFormatParams() != null && yAxi.getFormatParams().getUnit() != null) {
                    series.setUnit(yAxi.getFormatParams().getUnit());
                }
                seriesList.add(series);
            }
        }
        chartReturnObj
                .setXAxisData(xAxisData)
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
                        .setKey(yAxi.getFieldKey())
                        .setName(yAxi.getAliasName());
                if (yAxi.getFormatParams() != null && yAxi.getFormatParams().getUnit() != null) {
                    series.setUnit(yAxi.getFormatParams().getUnit());
                }
                seriesList.add(series);
            }
            chartReturnObj.setXAxisData(xAxisData)
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
         * 指标对应的key与名称对应
         */
        private String key;

        /**
         * 单位
         */
        private UnitEnums unit;
        /**
         * 数据
         */
        private List<Object> data;
    }


}
