package cn.bctools.chart.chart;

import cn.bctools.chart.chart.bo.FieldsData;
import cn.bctools.chart.chart.bo.YoYMoMComparisonsData;
import cn.bctools.chart.chart.po.ChartReturnObj;
import cn.bctools.chart.chart.po.GetDataParameter;
import cn.bctools.chart.enums.UnitEnums;
import cn.bctools.chart.enums.YoYMoMCalculateTypeEnums;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 卡片趋势图
 *
 * @author xh
 * @modified By：
 * @version: 1.0.0$
 */
@Slf4j
@Data
@Component
@AllArgsConstructor
public class TrendCardBo implements ChartElementInterface {
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
        //获取基本的汇总指标数据
        //判断是否存在
        GetDataParameter oldGetDataParameter = JSONObject.parseObject(JSONObject.toJSONString(getDataParameter), GetDataParameter.class);
        //清空数据的 同环比与维度 进行基本数据汇总
        getDataParameter.getLogicSetting().setXAxis(new ArrayList<>());
        List<FieldsData> yAxis = getDataParameter.getLogicSetting().getYAxis();
        yAxis.stream().peek(e -> e.setChildren(new ArrayList<>())).collect(Collectors.toList());
        //基础sql
        StringBuilder sql = new StringBuilder();
        ChartElementInterface.super.buildSql(getDataParameter, sql);
        List<Map<String, Object>> page = dorisJdbcTemplate.getDataPage(getDataParameter.getShowNumber(), 1L, sql.toString(), getDataParameter.getParameter(), getDataParameter.getSortFields());
        ArrayList<Series> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>(1);
        if (!page.isEmpty()) {
            map.putAll(page.get(0));
        }
        yAxis.forEach(e -> {
            Series series = new Series().setName(ObjUtil.isNotEmpty(e.getAliasName()) ? e.getAliasName() : e.getFieldName())
                    .setValue(map.getOrDefault(e.getFieldKey(), 0L))
                    .setFieldKey(e.getFieldKey())
                    .setUnit(ObjUtil.isNotEmpty(e.getFormatParams()) ? e.getFormatParams().getUnit() : null);
            list.add(series);
        });

        List<YoYMoMComparisonsData> fieldsData = oldGetDataParameter.getLogicSetting().getYAxis().stream().filter(e -> !e.getChildren().isEmpty()).flatMap(e -> e.getChildren().stream()).collect(Collectors.toList());
        if (!fieldsData.isEmpty()) {
            StringBuilder newSql = new StringBuilder();
            //进行同环比 计算
            String max = getMax(oldGetDataParameter);
            if (StrUtil.isNotBlank(max)) {
                FieldsData xField = oldGetDataParameter.getLogicSetting().getXAxis().get(0);
                boolean normalDate = DataFieldTypeEnum.isNormalDate(xField.getFieldType());
                List<Object> parameter = oldGetDataParameter.getParameter();
                if (normalDate) {
                    parameter.add(max);
                    max = "'" + max + "'";
                }
                //替换 指标key 为固定值
                this.buildSql(oldGetDataParameter, newSql);
                String newSqlString = newSql.toString();
                String replaceValue = "a.`" + xField.getFieldKey() + "`";
                newSqlString = newSqlString.replaceAll(replaceValue, max);
                newSql = new StringBuilder();
                newSql.append("SELECT * FROM (").append(newSqlString).append(") as b where `").append(xField.getFieldKey()).append("` = ?");
                List<Map<String, Object>> dataPage = dorisJdbcTemplate.getData(newSql.toString(), parameter);
                if (!dataPage.isEmpty()) {
                    map.clear();
                    map.putAll(dataPage.get(0));
                }
            }
            fieldsData.forEach(e -> {
                Series series = new Series().setName(ObjUtil.isNotEmpty(e.getAliasName()) ? e.getAliasName() : e.getFieldName())
                        .setFieldKey(e.getFieldKey())
                        .setValue(map.get(e.getFieldKey()));
                if (e.getCalculateType().equals(YoYMoMCalculateTypeEnums.PERCENTAGE)) {
                    series.setUnit(UnitEnums.perCent);
                }
                list.add(series);
            });
        }
        return chartReturnObj.setSeries(list);
    }

    /**
     * 如果存在同环比 需要获取最大值作为 对比源
     */
    private String getMax(GetDataParameter getDataParameter) {
        StringBuilder sql = new StringBuilder();
        String fieldKey = getDataParameter.getLogicSetting().getXAxis().get(0).getFieldKey();
        sql.append("SELECT cast(MAX(`").append(fieldKey)
                .append("`) as STRING ) as `").append(fieldKey)
                .append("` FROM ").append(getDataParameter.getTableName())
                .append(getDataParameter.getWhere());
        log.info("执行的sql:{}", sql.toString());
        //获取数据
        return dorisJdbcTemplate.queryForObject(sql.toString(), String.class, getDataParameter.getParameter().toArray());
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
         * 名称
         */
        private String fieldKey;
        /**
         * 具体的值
         */
        private Object value;
        /**
         * 单位
         */
        private UnitEnums unit;
    }
}
