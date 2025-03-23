package cn.bctools.chart.chart;

import cn.bctools.chart.chart.bo.FieldsData;
import cn.bctools.chart.chart.po.ChartDesignInParameter;
import cn.bctools.chart.chart.po.ChartReturnObj;
import cn.bctools.chart.chart.po.GetDataParameter;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author ：xh
 * [description]： 对比漏斗图
 * @modified By：
 * @version: 1.0.0$
 */
@Slf4j
@Data
@Component
@AllArgsConstructor
public class FunnelAlignChartBo implements ChartElementInterface {
    private final DorisJdbcTemplate dorisJdbcTemplate;


    @Override
    public ChartReturnObj exec(GetDataParameter getDataParameter) {
        //返回值
        ChartReturnObj<FunnelAlignChartBo.Series> chartReturnObj = new ChartReturnObj<FunnelAlignChartBo.Series>()
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
        List<Map<String, Object>> list = dorisJdbcTemplate.getDataPage(getDataParameter.getShowNumber(), 1L, sql.toString(), getDataParameter.getParameter(), getDataParameter.getSortFields());
        FieldsData fieldsData = xAxis.get(0);
        List<Series> seriesList = new ArrayList<>();
        //根据指标重组数据
        yAxis.forEach(e -> {
            Series series = new Series();
            series.setName(StrUtil.isNotBlank(fieldsData.getAliasName()) ? fieldsData.getAliasName() : fieldsData.getFieldName());
            List<SeriesData> seriesData = list.stream().map(v -> new SeriesData().setName(v.getOrDefault(fieldsData.getFieldKey(),""))
                            .setValue(v.getOrDefault(e.getFieldKey(), 0)))
                    .collect(Collectors.toList());
            series.setData(seriesData);
            seriesList.add(series);
        });
        chartReturnObj.setSeries(seriesList);
        return chartReturnObj;


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
         * 数据
         */
        private List<SeriesData> data;
    }

    /**
     * 具体的数据结构
     */
    @Data
    @Accessors(chain = true)
    private static class SeriesData {
        /**
         * 名称
         */
        private Object name;
        /**
         * 名称
         */
        private Object value;
    }

}
