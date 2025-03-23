package cn.bctools.screen.chart;

import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.screen.chart.bo.FieldsData;
import cn.bctools.screen.chart.po.ChartDesignInParameter;
import cn.bctools.screen.chart.po.ChartReturnObj;
import cn.bctools.screen.chart.po.GetDataParameter;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author ：xh
 * [description]：漏斗图，饼图，环图
 * 数据结构如下
 * * {
 * *             xAxisData:[],
 * *             yAxisData:[],
 * *             series:[
 * *                 { value: 60, name: 'Visit' },
 * *                 { value: 40, name: 'Inquiry' },
 * *                 { value: 20, name: 'Order' },
 * *                 { value: 80, name: 'Click' },
 * *                 { value: 100, name: 'Show' }
 * *             ]
 * *          }
 * @modified By：
 * @version: 1.0.0$
 */
@Slf4j
@Data
@Component
@AllArgsConstructor
public class BasicsPieChartBo implements ChartElementInterface {
    private final DorisJdbcTemplate dorisJdbcTemplate;


    @Override
    public ChartReturnObj<JSONObject> exec(GetDataParameter getDataParameter) {
        ChartDesignInParameter chartDesignInParameter = getDataParameter.getLogicSetting();
        StringBuilder sql = new StringBuilder();
        List<FieldsData> xAxis = chartDesignInParameter.getXAxis();
        List<FieldsData> yAxis = chartDesignInParameter.getYAxis();
        ChartElementInterface.super.buildSql(getDataParameter, sql);
        List<Map<Object, Object>> maps = dorisJdbcTemplate.getDataPage(getDataParameter.getShowNumber(), 1L, sql.toString(), getDataParameter.getParameter(), getDataParameter.getSortFields())
                .stream().map(e -> {
                    Map<Object, Object> hashMap = new HashMap<>();
                    hashMap.put("name", e.get(xAxis.get(0).getFieldKey()));
                    hashMap.put("value", e.get(yAxis.get(0).getFieldKey()));
                    return hashMap;
                }).collect(Collectors.toList());
        List<JSONObject> list = JSONArray.parseArray(JSONObject.toJSONString(maps), JSONObject.class);
        return new ChartReturnObj<JSONObject>().setSeries(list)
                .setYAxisData(new ArrayList<>())
                .setXAxisData(new ArrayList<>());
    }

}
