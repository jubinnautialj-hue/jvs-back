package cn.bctools.chart.chart;

import cn.bctools.chart.chart.bo.FieldsData;
import cn.bctools.chart.chart.po.ChartDesignInParameter;
import cn.bctools.chart.chart.po.ChartReturnObj;
import cn.bctools.chart.chart.po.GetDataParameter;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 树形图表
 *
 * @author ：xh
 */
@Slf4j
@Data
@Component
@AllArgsConstructor
public class BoxplotLightVelocityChartBo implements ChartElementInterface {
    private final DorisJdbcTemplate dorisJdbcTemplate;

    @Override
    public ChartReturnObj exec(GetDataParameter getDataParameter) {
        //返回值
        ChartReturnObj<JSONArray> chartReturnObj = new ChartReturnObj<JSONArray>()
                .setSeries(new ArrayList<>())
                .setCardContent(0)
                .setYAxisData(new ArrayList<>())
                .setXAxisData(new ArrayList<>());
        ChartDesignInParameter chartDesignInParameter = getDataParameter.getLogicSetting();
        //基础sql
        StringBuilder sql = new StringBuilder();
        FieldsData xAxis = chartDesignInParameter.getXAxis().get(0);
        FieldsData yAxis = chartDesignInParameter.getYAxis().get(0);
        ChartElementInterface.super.buildSql(getDataParameter, sql);
        List<Map<String, Object>> data = dorisJdbcTemplate.getDataPage(getDataParameter.getShowNumber(), 1L, sql.toString(), getDataParameter.getParameter(), getDataParameter.getSortFields());
        //组装数据
        List<Object> yAxisData = new ArrayList<>();
        List<JSONArray> series = new ArrayList<>();
        data.forEach(e -> {
            yAxisData.add(e.getOrDefault(xAxis.getFieldKey(), ""));
            series.add(JSONArray.parseArray(e.getOrDefault(yAxis.getFieldKey(), "[]").toString()));
        });
        chartReturnObj
                .setYAxisData(yAxisData)
                .setSeries(series);
        return chartReturnObj;
    }


}
