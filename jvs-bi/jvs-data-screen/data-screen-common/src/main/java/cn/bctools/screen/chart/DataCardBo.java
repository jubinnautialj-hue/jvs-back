package cn.bctools.screen.chart;

import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.screen.chart.bo.FieldsData;
import cn.bctools.screen.chart.po.ChartDesignInParameter;
import cn.bctools.screen.chart.po.ChartReturnObj;
import cn.bctools.screen.chart.po.GetDataParameter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * [description]：数据卡片
 *{
 *             name:'系统在线数',
 *             cardContent:1436
 *  }
 * @author xh
 * @modified By：
 * @version: 1.0.0$
 */
@Slf4j
@Data
@Component
@AllArgsConstructor
public class DataCardBo implements ChartElementInterface {
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
        List<FieldsData> yAxis = chartDesignInParameter.getYAxis();
        //基础sql
        StringBuilder sql = new StringBuilder();
       ChartElementInterface.super.buildSql(getDataParameter,sql);
        List<Map<String, Object>> page = dorisJdbcTemplate.getDataPage(getDataParameter.getShowNumber(), 1L, sql.toString(), getDataParameter.getParameter(), getDataParameter.getSortFields());
        if (!page.isEmpty()) {
            Object object = page.get(0).get(yAxis.get(0).getFieldKey());
            chartReturnObj.setCardContent(object);
        }
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
         * 类别
         */
        private Object stack;
        /**
         * 数据
         */
        private List<Object> data;
    }
}
