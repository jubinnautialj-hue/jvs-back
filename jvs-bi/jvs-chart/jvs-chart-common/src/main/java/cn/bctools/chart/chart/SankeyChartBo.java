package cn.bctools.chart.chart;

import cn.bctools.chart.chart.bo.FieldsData;
import cn.bctools.chart.chart.po.ChartDesignInParameter;
import cn.bctools.chart.chart.po.ChartReturnObj;
import cn.bctools.chart.chart.po.GetDataParameter;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 */
@Slf4j
@Data
@Component
@AllArgsConstructor
public class SankeyChartBo implements ChartElementInterface {
    private DorisJdbcTemplate dorisJdbcTemplate;


    @Override
    public void buildSql(GetDataParameter getDataParameter, StringBuilder sql) {
        log.info("基础桑葚图入参{}", JSONObject.toJSONString(getDataParameter));
        String where = getDataParameter.getWhere();
        getDataParameter.setWhere("");
        ChartElementInterface.super.buildSql(getDataParameter, sql);
        //需要对基础查询数据处理
        String string = sql.toString();
        StringBuilder tableSql = new StringBuilder();
        tableSql.append("(select * from ").append(getDataParameter.getTableName()).append(where);
        if (getDataParameter.getShowNumber() > 0) {
            tableSql.append(" LIMIT ?");
        }
        tableSql.append(") as a");
        //重置sql
        string = string.replace(getDataParameter.getTableName(), tableSql.toString());
        sql.delete(0, sql.length());
        sql.append(string);
    }

    @Override
    public ChartReturnObj exec(GetDataParameter getDataParameter) {
        //返回值
        ChartReturnObj<SankeyChartBo.Series> chartReturnObj = new ChartReturnObj<SankeyChartBo.Series>()
                .setSeries(new ArrayList<>())
                .setYAxisData(new ArrayList<>())
                .setXAxisData(new ArrayList<>());
        ChartDesignInParameter chartDesignInParameter = getDataParameter.getLogicSetting();
        //基础sql
        List<FieldsData> xAxis = chartDesignInParameter.getXAxis();
        List<FieldsData> yAxis = chartDesignInParameter.getYAxis();
        //sql 动态参数
        List<Object> parameter = getDataParameter.getParameter();
        //分页条件添加
        if (getDataParameter.getShowNumber() > 0) {
            parameter.add(getDataParameter.getShowNumber());
        }
        //获取所有分类
        List<SeriesData> data = this.getData(getDataParameter);
        List<Links> linksList = new ArrayList<>();
        //获取节点关系与具体值 第一个维度作为基础分组字段
        for (int i = 1; i < xAxis.size(); i++) {
            StringBuilder sql = new StringBuilder();
            //指标字段永远只有一个
            List<FieldsData> newAxis = xAxis.subList(0, i + 1);
            GetDataParameter getDataParameterNew = JSONObject.parseObject(JSONObject.toJSONString(getDataParameter), GetDataParameter.class);
            getDataParameterNew.getLogicSetting().setXAxis(newAxis);
            this.buildSql(getDataParameter, sql);
            List<Map<String, Object>> list = dorisJdbcTemplate.queryForList(sql.toString(), getDataParameterNew.getParameter().toArray());
            int finalI = i;
            list.forEach(e -> {
                Object sourceValue = e.getOrDefault(xAxis.get(finalI - 1).getFieldKey(), "");
                sourceValue = sourceValue != null ? sourceValue.toString() : "";
                Object targetValue = e.getOrDefault(xAxis.get(finalI).getFieldKey(), "");
                targetValue = targetValue != null ? targetValue.toString() : "";
                Links links = new Links().setSource(sourceValue)
                        .setTarget(targetValue)
                        .setValue(e.getOrDefault(yAxis.get(0).getFieldKey(), 0));
                linksList.add(links);
            });
        }
        Series series = new Series().setData(data).setLinks(linksList);
        return chartReturnObj.setSeries(Arrays.asList(series));
    }

    /**
     * 组装data
     */
    private List<SeriesData> getData(GetDataParameter getDataParameter) {
        //获取顶级名称
        List<FieldsData> xAxis = getDataParameter.getLogicSetting().getXAxis();
        StringBuilder topSql = new StringBuilder();
        this.buildSql(getDataParameter, topSql);
        List<SeriesData> data = new ArrayList<>();
        for (FieldsData xAxi : xAxis) {
            //基于上面的基础sql 获取去重后的名称
            List<Map<String, Object>> list = dorisJdbcTemplate.queryForList("select `" + xAxi.getFieldKey() + "` from (" + topSql + ") as a group by `" + xAxi.getFieldKey() + "`", getDataParameter.getParameter().toArray());
            for (Map<String, Object> map : list) {
                Object object = map.get(xAxi.getFieldKey());
                object = object != null ? object.toString() : "";
                data.add(new SeriesData().setName(object.toString()));
            }
        }
        return data;
    }

    /**
     * 具体的数据结构
     */
    @Data
    @Accessors(chain = true)
    private static class Series {

        /**
         * 分类
         */
        private List<SeriesData> data;
        /**
         * 连接关系
         */
        private List<Links> links;
    }

    /**
     * 分类
     */
    @Data
    @Accessors(chain = true)
    private static class SeriesData {
        /**
         * 值
         **/
        Object name;
    }

    /**
     * 关联关系
     */
    @Data
    @Accessors(chain = true)
    private static class Links {
        /**
         * 来源
         */
        Object source;
        /**
         * 去向
         */
        Object target;
        /**
         * 值
         **/
        Object value;
    }
}
