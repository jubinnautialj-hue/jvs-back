package cn.bctools.chart.chart;

import cn.bctools.chart.chart.bo.FieldsData;
import cn.bctools.chart.chart.po.ChartDesignInParameter;
import cn.bctools.chart.chart.po.ChartReturnObj;
import cn.bctools.chart.chart.po.GetDataParameter;
import cn.bctools.chart.enums.UnitEnums;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.data.factory.doris.condition.DorisCollectCondition;
import cn.bctools.data.factory.dto.DataSourceField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author ：
 * [description]：柱线混合图
 * @modified By：
 * @version: 1.0.0$
 */
@Slf4j
@Data
@Service
@AllArgsConstructor
public class BlendChartsChartBo implements ChartElementInterface {
    private final DorisJdbcTemplate dorisJdbcTemplate;

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
       ChartElementInterface.super.buildSql(getDataParameter,sql);
        //判断是否存在多个维度 目前最多支持两个 如果是多个维度需要 二次加工
        String fieldKey = xAxis.get(0).getFieldKey();
        //合并数据
        this.singleGroup(sql.toString(), fieldKey, yAxis, getDataParameter, chartReturnObj);
        return chartReturnObj;

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
                        .setSeriesType(yAxi.getSeriesType())
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
         * 指标对应的key与名称对应
         */
        private String key;
        /**
         *多Y轴混合图 指标类型 是线还是柱
         * */
        private String seriesType;
        /**
         * 单位
         */
        private UnitEnums unit;
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
