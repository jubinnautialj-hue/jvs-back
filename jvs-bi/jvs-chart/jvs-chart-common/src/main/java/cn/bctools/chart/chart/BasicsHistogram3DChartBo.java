package cn.bctools.chart.chart;

import cn.bctools.chart.chart.bo.FieldsData;
import cn.bctools.chart.chart.po.ChartReturnObj;
import cn.bctools.chart.chart.po.GetDataParameter;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 3d柱状图
 * {
 *             xAxisData:["12a","1a","2a","3a","4a","5a","6a","7a","8a","9a","10a","11a","12p","1p","2p","3p","4p","5p","6p","7p","8p","9p","10p","11p"],
 *             yAxisData:['Saturday', 'Friday', 'Thursday', 'Wednesday', 'Tuesday', 'Monday', 'Sunday'],
 *             series:[{
 *                 name:'test',
 *                 data:[[0,0,5],[1,0,1],[2,0,0],[3,0,0],[4,0,0],[5,0,0],[6,0,0],[7,0,0],[8,0,0],[9,0,0],[10,0,0],[11,0,2],[12,0,4],[13,0,1],[14,0,1],[15,0,3],[16,0,4],[17,0,6],[18,0,4],[19,0,4],[20,0,3],[21,0,3],[22,0,2],[23,0,5],[0,1,7],[1,1,0],[2,1,0],[3,1,0],[4,1,0],[5,1,0],[6,1,0],[7,1,0],[8,1,0],[9,1,0],[10,1,5],[11,1,2],[12,1,2],[13,1,6],[14,1,9],[15,1,11],[16,1,6],[17,1,7],[18,1,8],[19,1,12],[20,1,5],[21,1,5],[22,1,7],[23,1,2],[0,2,1],[1,2,1],[2,2,0],[3,2,0],[4,2,0],[5,2,0],[6,2,0],[7,2,0],[8,2,0],[9,2,0],[10,2,3],[11,2,2],[12,2,1],[13,2,9],[14,2,8],[15,2,10],[16,2,6],[17,2,5],[18,2,5],[19,2,5],[20,2,7],[21,2,4],[22,2,2],[23,2,4],[0,3,7],[1,3,3],[2,3,0],[3,3,0],[4,3,0],[5,3,0],[6,3,0],[7,3,0],[8,3,1],[9,3,0],[10,3,5],[11,3,4],[12,3,7],[13,3,14],[14,3,13],[15,3,12],[16,3,9],[17,3,5],[18,3,5],[19,3,10],[20,3,6],[21,3,4],[22,3,4],[23,3,1],[0,4,1],[1,4,3],[2,4,0],[3,4,0],[4,4,0],[5,4,1],[6,4,0],[7,4,0],[8,4,0],[9,4,2],[10,4,4],[11,4,4],[12,4,2],[13,4,4],[14,4,4],[15,4,14],[16,4,12],[17,4,1],[18,4,8],[19,4,5],[20,4,3],[21,4,7],[22,4,3],[23,4,0],[0,5,2],[1,5,1],[2,5,0],[3,5,3],[4,5,0],[5,5,0],[6,5,0],[7,5,0],[8,5,2],[9,5,0],[10,5,4],[11,5,1],[12,5,5],[13,5,10],[14,5,5],[15,5,7],[16,5,11],[17,5,6],[18,5,0],[19,5,5],[20,5,3],[21,5,4],[22,5,2],[23,5,0],[0,6,1],[1,6,0],[2,6,0],[3,6,0],[4,6,0],[5,6,0],[6,6,0],[7,6,0],[8,6,0],[9,6,0],[10,6,1],[11,6,0],[12,6,2],[13,6,1],[14,6,3],[15,6,4],[16,6,0],[17,6,0],[18,6,0],[19,6,0],[20,6,1],[21,6,2],[22,6,2],[23,6,6]]
 *             }]
 *  }
 *
 * @author ：xh
 */
@Slf4j
@Data
@Component
@AllArgsConstructor
public class BasicsHistogram3DChartBo implements ChartElementInterface {
    private DorisJdbcTemplate dorisJdbcTemplate;
    @Override
    public ChartReturnObj exec(GetDataParameter getDataParameter) {
        StringBuilder sql = new StringBuilder();
        ChartElementInterface.super.buildSql(getDataParameter, sql);
        List<FieldsData> yAxis = getDataParameter.getLogicSetting().getYAxis();
        List<FieldsData> xAxis = getDataParameter.getLogicSetting().getXAxis();
        //返回值
        ChartReturnObj<Series> chartReturnObj = new ChartReturnObj<Series>()
                .setSeries(new ArrayList<>())
                .setYAxisData(new ArrayList<>())
                .setXAxisData(new ArrayList<>());
        List<Map<String, Object>> list = dorisJdbcTemplate.getDataPage(getDataParameter.getShowNumber(), 1L, sql.toString(), getDataParameter.getParameter(), getDataParameter.getSortFields());
        //获取x与y的数据
        List<Object> x = list.stream().map(e -> e.get(xAxis.get(0).getFieldKey())).distinct().collect(Collectors.toList());
        List<Object> y = list.stream().map(e -> e.get(xAxis.get(1).getFieldKey())).distinct().collect(Collectors.toList());
        List<Series> seriesList = new ArrayList<>();
        //组装数据
        for (FieldsData yAxi : yAxis) {
            Map<Object, List<Map<String, Object>>> map = list.stream().collect(Collectors.groupingBy(e -> e.get(xAxis.get(0).getFieldKey()), Collectors.toList()));
            List<Object> xData = new ArrayList<>(map.keySet());
            List<List<Object>> data = new ArrayList<>();
            //x数据的下标就是 坐标系的值 y轴同理
            for (int i = 0; i < xData.size(); i++) {
                List<Map<String, Object>> yData = map.get(xData.get(i));
                for (int i1 = 0; i1 < yData.size(); i1++) {
                    Map<String, Object> map1 = yData.get(i1);
                    data.add(Arrays.asList(i, i1, map1.get(yAxi.getFieldKey())));
                }
            }
            Series series = new Series();
            series.setName(yAxi.getAliasName())
                    .setData(data);
            seriesList.add(series);
        }
        chartReturnObj.setXAxisData(x)
                .setYAxisData(y)
                .setSeries(seriesList);
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
        private List<List<Object>> data;
    }
}
