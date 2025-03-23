package cn.bctools.screen.chart;

import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.screen.chart.po.ChartReturnObj;
import cn.bctools.screen.chart.po.GetDataParameter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author ：xh
 * [description]：仪表盘
 *  {
 *             xAxisData:[],
 *             yAxisData:[],
 *             series:[{value: 6.6}]
 *         }
 * @modified By：
 * @version: 1.0.0$
 */
@Slf4j
@Data
@Component
@AllArgsConstructor
public class DashBoarChartBo implements ChartElementInterface {
    private DorisJdbcTemplate dorisJdbcTemplate;

    @Override
    public ChartReturnObj exec(GetDataParameter getDataParameter) {
        //返回值
        ChartReturnObj<Series> chartReturnObj = new ChartReturnObj<Series>()
                .setSeries(new ArrayList<>())
                .setYAxisData(new ArrayList<>())
                .setHeader(new ArrayList<>())
                .setXAxisData(new ArrayList<>());
        StringBuilder sql = new StringBuilder();
        ChartElementInterface.super.buildSql(getDataParameter, sql);
        List<Map<String, Object>> maps = dorisJdbcTemplate.getDataPage(getDataParameter.getShowNumber(), 1L, sql.toString(), getDataParameter.getParameter(), getDataParameter.getSortFields());
        Series series = new Series();
        series.setValue(0);
        if (!maps.isEmpty()) {
            String fieldKey = getDataParameter.getLogicSetting().getYAxis().get(0).getFieldKey();
            series.setValue(maps.get(0).get(fieldKey));
        }
        return chartReturnObj.setSeries(Arrays.asList(series));
    }

    @Data
    private static class Series {
        private Object value;
    }
}
