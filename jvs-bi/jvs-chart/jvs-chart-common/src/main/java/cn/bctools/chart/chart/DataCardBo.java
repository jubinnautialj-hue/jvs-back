package cn.bctools.chart.chart;

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
import java.util.List;
import java.util.Map;

/**
 * [description]：数据卡片
 * {
 * name:'系统在线数',
 * cardContent:1436
 * }
 *
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
                .setSeries(new ArrayList<>())
                .setYAxisData(new ArrayList<>())
                .setXAxisData(new ArrayList<>());
        //基础sql
        StringBuilder sql = new StringBuilder();
        ChartElementInterface.super.buildSql(getDataParameter, sql);
        List<Map<String, Object>> page = dorisJdbcTemplate.getDataPage(getDataParameter.getShowNumber(), 1L, sql.toString(), getDataParameter.getParameter(), getDataParameter.getSortFields());
        ArrayList<Series> list = new ArrayList<>();
        if (!page.isEmpty()) {
            Map<String, Object> map = page.get(0);
            getDataParameter.getLogicSetting().getYAxis().forEach(e -> {
                Series series = new Series().setName(ObjUtil.isNotEmpty(e.getAliasName()) ? e.getAliasName() : e.getFieldName())
                        .setFieldKey(e.getFieldKey())
                        .setValue(map.getOrDefault(e.getFieldKey(), 0L));
                list.add(series);
            });
        }
        return chartReturnObj.setSeries(list);
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
        private String name;
        /**
         * 字段key
         */
        private String fieldKey;
        /**
         * 具体的值
         */
        private Object value;
    }
}
