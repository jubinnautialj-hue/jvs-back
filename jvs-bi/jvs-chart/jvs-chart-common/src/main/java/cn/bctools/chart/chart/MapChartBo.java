package cn.bctools.chart.chart;

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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 地图
 *{
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
 * @author ：xh
 */
@Slf4j
@Data
@Component
@AllArgsConstructor
public class MapChartBo implements ChartElementInterface {
    private DorisJdbcTemplate dorisJdbcTemplate;

    @Override
    public ChartReturnObj exec(GetDataParameter getDataParameter) {
        //返回值
        ChartReturnObj<Series> chartReturnObj = new ChartReturnObj<Series>()
                .setSeries(new ArrayList<>())
                .setYAxisData(new ArrayList<>())
                .setXAxisData(new ArrayList<>());
        ChartDesignInParameter chartDesignInParameter = getDataParameter.getLogicSetting();
        String xKey = chartDesignInParameter.getXAxis().get(0).getFieldKey();
        String yKey = chartDesignInParameter.getYAxis().get(0).getFieldKey();
        //基础sql
        StringBuilder sql = new StringBuilder();
        ChartElementInterface.super.buildSql(getDataParameter, sql);
        List<SeriesData> seriesData = dorisJdbcTemplate.getDataPage(getDataParameter.getShowNumber(), 1L, sql.toString(), getDataParameter.getParameter(), getDataParameter.getSortFields())
                .stream()
                .map(e ->
                        new SeriesData()
                                .setName(e.get(xKey))
                                .setValue(e.get(yKey)))
                .collect(Collectors.toList());
        Series series = new Series().setData(seriesData)
                .setName("");
        chartReturnObj.setSeries(Arrays.asList(series));
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
         * 值
         */
        private Object value;
    }
}
