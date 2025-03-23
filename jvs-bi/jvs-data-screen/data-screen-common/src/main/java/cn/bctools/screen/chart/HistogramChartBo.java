package cn.bctools.screen.chart;

import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.screen.chart.po.ChartReturnObj;
import cn.bctools.screen.chart.po.GetDataParameter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author ：xh
 * 返回值格式为:
 * {
 *             series:[
 *                 {
 *                     name:'中华人民共和国',
 *                     data:[
 *                         {name:"北京市",value:199},
 *                         {name:"天津市",value:42},
 *                         {name:"河北省",value:102},
 *                         {name:"山西省",value:81},
 *                         {name:"内蒙古自治区",value:47},
 *                         {name:"辽宁省",value:67},
 *                         {name:"吉林省",value:82},
 *                         {name:"黑龙江省",value:123},
 *                         {name:"上海市",value:24},
 *                         {name:"江苏省",value:92},
 *                         {name:"浙江省",value:114},
 *                         {name:"安徽省",value:109},
 *                         {name:"福建省",value:116},
 *                         {name:"江西省",value:91},
 *                         {name:"山东省",value:119},
 *                         {name:"河南省",value:137},
 *                         {name:"湖北省",value:116},
 *                         {name:"湖南省",value:114},
 *                         {name:"重庆市",value:91},
 *                         {name:"四川省",value:125},
 *                         {name:"贵州省",value:62},
 *                         {name:"云南省",value:83},
 *                         {name:"西藏自治区",value:9},
 *                         {name:"陕西省",value:80},
 *                         {name:"甘肃省",value:56},
 *                         {name:"青海省",value:10},
 *                         {name:"宁夏回族自治区",value:18},
 *                         {name:"新疆维吾尔自治区",value:180},
 *                         {name:"广东省",value:123},
 *                         {name:"广西壮族自治区",value:59},
 *                         {name:"海南省",value:14}
 *                     ]
 *                 }
 *             ]
 *         }
 * [description]：直方图
 * @modified By：
 * @version: 1.0.0$
 */
@Slf4j
@Data
@Component
@AllArgsConstructor
public class HistogramChartBo implements ChartElementInterface {
    private DorisJdbcTemplate dorisJdbcTemplate;

    @Override
    public ChartReturnObj exec(GetDataParameter getDataParameter) {
        //返回值
        ChartReturnObj<Object> chartReturnObj = new ChartReturnObj<>()
                .setSeries(new ArrayList<>())
                .setYAxisData(new ArrayList<>())
                .setXAxisData(new ArrayList<>());
        String fieldKey = getDataParameter.getLogicSetting().getXAxis().get(0).getFieldKey();
        String sql = " SELECT `" + fieldKey + "` FROM " + getDataParameter.getTableName() + " " + getDataParameter.getWhere();
        List<Map<String, Object>> maps = dorisJdbcTemplate.getDataPage(getDataParameter.getShowNumber(), 1L, sql, getDataParameter.getParameter(), getDataParameter.getSortFields());
        List<Object> list = maps.stream().map(e -> e.get(fieldKey)).collect(Collectors.toList());
        chartReturnObj.setSeries(list);
        return chartReturnObj;
    }
}

