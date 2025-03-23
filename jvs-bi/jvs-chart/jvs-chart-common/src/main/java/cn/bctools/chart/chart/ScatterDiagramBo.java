package cn.bctools.chart.chart;

import cn.bctools.chart.chart.bo.FieldsData;
import cn.bctools.chart.chart.po.ChartDesignInParameter;
import cn.bctools.chart.chart.po.ChartReturnObj;
import cn.bctools.chart.chart.po.GetDataParameter;
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
 * 散点图
 * {
 * xAxisData:[],
 * yAxisData:[],
 * series:[
 * {
 * name:'Email',
 * stack:'类别',
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
 *
 * @author ：xh
 */
@Slf4j
@Data
@Component
@AllArgsConstructor
public class ScatterDiagramBo implements ChartElementInterface {
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
        //方便后续数据构建使用
        List<FieldsData> colour = Optional.ofNullable(chartDesignInParameter.getColour()).orElse(new ArrayList<>());
        boolean colourIsNotNll = !colour.isEmpty();
        ChartElementInterface.super.buildSql(getDataParameter, sql);
        List<Map<String, Object>> maps = dorisJdbcTemplate.getDataPage(getDataParameter.getShowNumber(), 1L, sql.toString(), getDataParameter.getParameter(), getDataParameter.getSortFields())
                .stream().peek(e -> {
                    List<Object> arrayValue = new ArrayList<>();
                    for (FieldsData yAxi : yAxis) {
                        arrayValue.add(e.get(yAxi.getFieldKey()));
                    }
                    arrayValue.add(e.get(xAxis.get(0).getFieldKey()));
                    e.put("arrayValue", arrayValue);
                }).collect(Collectors.toList());
        List<Series> seriesList = new ArrayList<>();
        //这里需要区分是否存在二级分组 如果存在需要二次分组
        if (colourIsNotNll) {
            Map<Object, List<Map<String, Object>>> listMap = maps.stream().collect(Collectors.groupingBy(e -> e.get(colour.get(0).getFieldKey()), Collectors.toList()));
            listMap.forEach((e, v) -> {
                List<Object> list = v.stream().map(v1 -> v1.get("arrayValue")).collect(Collectors.toList());
                Series series = new Series()
                        .setData(list)
                        .setName(e);
                seriesList.add(series);
            });
        } else {
            //这里使用array函数后 返回值是一个字符串 需要手动转化为 数组 并把值转化为 bigDecimal
            List<Object> list = maps.stream().map(e -> e.get("arrayValue")).collect(Collectors.toList());
            Series series = new Series()
                    .setData(list)
                    .setName(xAxis.get(0).getAliasName());
            seriesList.add(series);
        }
        return chartReturnObj
                .setSeries(seriesList);
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
        private List<Object> data;
    }
}
