package cn.bctools.chart.chart;

import cn.bctools.chart.chart.bo.FieldsData;
import cn.bctools.chart.chart.po.ChartDesignInParameter;
import cn.bctools.chart.chart.po.ChartReturnObj;
import cn.bctools.chart.chart.po.GetDataParameter;
import cn.bctools.common.utils.IdGenerator;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.data.factory.enums.CollectTypeEnum;
import cn.bctools.data.factory.enums.DataFieldTypeClassifyEnum;
import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson2.JSONArray;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 交叉表格
 *
 * @author Administrator
 */
@Slf4j
@Data
@Component
@AllArgsConstructor
public class CrossListChartBo implements ChartElementInterface {
    private DorisJdbcTemplate dorisJdbcTemplate;

    public void buildSql(GetDataParameter getDataParameter, StringBuilder sql, List<ChartReturnObj.TableHeader> tableHeaders) {
        sql.append("SELECT ");
        List<FieldsData> xAxis = getDataParameter.getLogicSetting().getXAxis();
        //转换字段 为维度的最后一个字段
        FieldsData columnField = xAxis.get(xAxis.size() - 1);
        xAxis = xAxis.subList(0, xAxis.size() - 1);
        FieldsData polymerization = getDataParameter.getLogicSetting().getYAxis().get(0);
        List<String> groupList = xAxis.stream().map(e -> {
            if (DataFieldTypeEnum.isNormalDate(e.getFieldType())) {
                return "cast(`" + e.getFieldKey() + "` as STRING) AS `" + e.getFieldKey() + "`";
            } else {
                return "`" + e.getFieldKey() + "`";
            }
        }).collect(Collectors.toList());
        String groupKey = String.join(",", groupList);
        String groupListNoCast = xAxis.stream().map(e -> "`" + e.getFieldKey() + "`").collect(Collectors.joining(","));
        sql.append(groupKey).append(",");
        String lineRollSelectSql = tableHeaders.stream().map(e -> {
            StringBuffer selectSql = new StringBuffer();
            selectSql.append("map[");
            if (!columnField.getFieldType().getClassifyEnum().equals(DataFieldTypeClassifyEnum.数字)) {
                selectSql.append("'").append(e.getLabel()).append("'");
            } else {
                selectSql.append(e.getLabel());
            }
            selectSql.append("] as `").append(e.getProp()).append("`");
            return selectSql;
        }).collect(Collectors.joining(","));
        sql.append(lineRollSelectSql)
                .append(" from ( select ")
                .append(groupKey)
                .append(",")
                .append("map_agg(`")
                .append(columnField.getFieldKey())
                .append("`,`")
                .append(polymerization.getFieldKey()).append("`) as map FROM ");
        String tableName = getDataParameter.getTableName();
        if (polymerization.getCalculateType().equals(CollectTypeEnum.notCalculate)) {
            sql.append(tableName);
        } else {
            //获取汇总sql
            Integer decimalPlace;
            if (polymerization.getFormatParams() != null && polymerization.getFormatParams().getDecimalPlace() != null) {
                decimalPlace = polymerization.getFormatParams().getDecimalPlace();
            } else {
                decimalPlace = 2;
            }
            String addCondition = SpringContextUtil.getBean(polymerization.getCalculateType().getDorisClass()).addCondition(polymerization.getFieldKey(), decimalPlace, Boolean.FALSE, polymerization.getFieldKey());
            sql.append(" ( select ")
                    .append(groupKey)
                    .append(",`")
                    .append(columnField.getFieldKey())
                    .append("`,")
                    .append(addCondition)
                    .append(" from ")
                    .append(tableName)
                    .append(getDataParameter.getWhere())
                    .append(" group by ")
                    .append(groupListNoCast)
                    .append(",`")
                    .append(columnField.getFieldKey())
                    .append("`) as a");
        }
        sql.append(" group by ").append(groupListNoCast).append(") t");
        tableHeaders.addAll(0, xAxis.stream().map(e -> new ChartReturnObj.TableHeader().setLabel(e.getFieldName()).setProp(e.getFieldKey())).collect(Collectors.toList()));
    }

    @Override
    public ChartReturnObj exec(GetDataParameter getDataParameter) {
        //返回值
        ChartReturnObj chartReturnObj = new ChartReturnObj()
                .setSeries(new ArrayList<>())
                .setCardContent(0)
                .setYAxisData(new ArrayList<>())
                .setXAxisData(new ArrayList<>());
        ChartDesignInParameter logicSetting = getDataParameter.getLogicSetting();
        //基础sql
        StringBuilder sql = new StringBuilder();
        List<ChartReturnObj.TableHeader> column = this.getColumn(logicSetting.getXAxis().get(logicSetting.getXAxis().size() - 1), getDataParameter.getTableName(), getDataParameter.getParameter(), getDataParameter.getWhere());
        this.buildSql(getDataParameter, sql, column);
        List<Object> parameter = JSONArray.copyOf(getDataParameter.getParameter());
        List<Map<String, Object>> page = dorisJdbcTemplate.getDataPage(logicSetting.getSize(), logicSetting.getCurrent(), sql.toString(), getDataParameter.getParameter(), getDataParameter.getSortFields());
        StringBuilder countSql = new StringBuilder();
        countSql.append("SELECT COUNT(*) as count ")
                .append(" FROM (")
                .append(sql)
                .append(") as a");
        String count = dorisJdbcTemplate.queryForList(countSql.toString(), parameter.toArray()).get(0).get("count").toString();
        ChartReturnObj.TableData tableData = new ChartReturnObj.TableData();
        tableData.setCurrent(logicSetting.getCurrent());
        tableData.setSize(logicSetting.getSize());
        tableData.setRecords(page);
        tableData.setTotal(Long.parseLong(count));
        return chartReturnObj
                .setHeader(column)
                .setData(tableData);
    }

    /**
     * 获取所有列
     *
     * @param yAix      行转列key属性
     * @param parameter 过滤条件入参
     * @param tableName 表名称
     * @param whereSql  过滤条件
     * @return 表格header 数据
     */
    private List<ChartReturnObj.TableHeader> getColumn(FieldsData yAix, String tableName, List<Object> parameter, String whereSql) {
        StringBuilder getAllTypeSql = new StringBuilder();
        getAllTypeSql.append("SELECT ");
        if (DataFieldTypeEnum.isNormalDate(yAix.getFieldType())) {
            getAllTypeSql.append("cast(`").append(yAix.getFieldKey()).append("` as STRING) as `").append(yAix.getFieldKey()).append("`");
        } else {
            getAllTypeSql.append("`").append(yAix.getFieldKey()).append("`");
        }
        getAllTypeSql.append(" FROM ")
                .append(tableName)
                .append(whereSql)
                .append(" GROUP BY `").append(yAix.getFieldKey()).append("`").append(" order by `").append(yAix.getFieldKey()).append("`");
        return dorisJdbcTemplate.queryForList(getAllTypeSql.toString(), parameter.toArray())
                .stream().flatMap(e -> e.values().stream()).map(e -> {
                    String object = Optional.ofNullable(e).orElse("").toString();
                    return new ChartReturnObj.TableHeader().setProp(StrUtil.isNotEmpty(object) ? SecureUtil.md5(object) : IdGenerator.getIdStr()).setLabel(object);
                }).collect(Collectors.toList());
    }
}
