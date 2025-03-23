package cn.bctools.chart.chart;

import cn.bctools.chart.chart.bo.FieldsData;
import cn.bctools.chart.chart.po.ChartDesignInParameter;
import cn.bctools.chart.chart.po.ChartReturnObj;
import cn.bctools.chart.chart.po.GetDataParameter;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.hutool.core.util.ObjUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 树形图表
 *
 * @author ：xh
 */
@Slf4j
@Data
@Component
@AllArgsConstructor
public class TreeChartBo implements ChartElementInterface {
    private final DorisJdbcTemplate dorisJdbcTemplate;


    @Override
    public ChartReturnObj exec(GetDataParameter getDataParameter) {
        //返回值
        ChartReturnObj<Series> chartReturnObj = new ChartReturnObj<Series>()
                .setSeries(new ArrayList<>())
                .setCardContent(0)
                .setYAxisData(new ArrayList<>())
                .setXAxisData(new ArrayList<>());
        ChartDesignInParameter chartDesignInParameter = getDataParameter.getLogicSetting();
        //基础sql
        StringBuilder sql = new StringBuilder();
        List<FieldsData> xAxis = chartDesignInParameter.getXAxis();
        List<FieldsData> yAxis = chartDesignInParameter.getYAxis();
        //第一层的数据
        ChartElementInterface.super.buildSql(getDataParameter, sql);
        List<Map<String, Object>> data = dorisJdbcTemplate.getDataPage(getDataParameter.getShowNumber(), 1L, sql.toString(), getDataParameter.getParameter(), getDataParameter.getSortFields());
        List<Series> seriesList = extracted(data, xAxis, 0, yAxis.get(0).getFieldKey());
        chartReturnObj.setSeries(seriesList);
        return chartReturnObj;
    }

    /**
     * @param data  数据
     * @param list  维度
     * @param index 当前下标
     * @return 树形数据 {@link Series}
     */
    private List<Series> extracted(List<Map<String, Object>> data, List<FieldsData> list, int index, String valueKey) {
        FieldsData fieldsData = list.get(index);
        LinkedHashMap<Object, List<Map<String, Object>>> map = data
                .stream()
                .collect(Collectors.groupingBy(e -> ObjUtil.isNull(e.get(fieldsData.getFieldKey())) ? "" : e.get(fieldsData.getFieldKey()), LinkedHashMap::new, Collectors.toList()));
        ++index;
        boolean b = list.size() == index;
        int finalIndex = index;
        return map.keySet().stream().map(e -> {
            Object object = map.get(e).get(0).get(valueKey);
            Series series = new Series()
                    .setValue(object)
                    .setName(e.toString());
            if (!b) {
                List<Series> children = extracted(map.get(e), list, finalIndex, valueKey);
                series.setChildren(children);
            }
            return series;
        }).collect(Collectors.toList());

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
         * 下级
         */
        private List<Series> children;
        /**
         * 值
         */
        private Object value;
    }
}
