package cn.bctools.screen.chart;

import cn.bctools.data.factory.api.dto.RowWhereDto;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.data.factory.dto.OrderField;
import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import cn.bctools.data.factory.enums.SortTypeEnums;
import cn.bctools.screen.chart.bo.FieldsData;
import cn.bctools.screen.chart.po.ChartDesignInParameter;
import cn.bctools.screen.chart.po.ChartReturnObj;
import cn.bctools.screen.chart.po.GetDataParameter;
import com.alibaba.fastjson2.JSONArray;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author ：xh
 * [description]：分类表格
 * @modified By：
 * @version: 1.0.0$
 */
@Slf4j
@Data
@Component
public class ClassifyTableChartBo implements ChartElementInterface {
    @Autowired
    DorisJdbcTemplate dorisJdbcTemplate;
    private final static String ROWSPAN_KEY = "rowspan";

    private final static String COLSPAN_KEY = "colspan";

    /**
     * 分组key
     */
    private final static String ClassifyTableGroupByKey = "ClassifyTableGroupByKey";


    @Override
    public ChartReturnObj exec(GetDataParameter getDataParameter) {
        //返回值
        ChartReturnObj chartReturnObj = new ChartReturnObj()
                .setSeries(new ArrayList<>())
                .setCardContent(0)
                .setYAxisData(new ArrayList<>())
                .setXAxisData(new ArrayList<>());
        ChartDesignInParameter chartDesignInParameter = getDataParameter.getLogicSetting();
        //基础sql
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        for (FieldsData xAxi : chartDesignInParameter.getXAxis()) {
            if (xAxi.getFieldType().equals(DataFieldTypeEnum.DATETIME)) {
                sql.append("cast(`").append(xAxi.getFieldKey()).append("` as STRING) AS `").append(xAxi.getFieldKey()).append("`,");
            } else {
                sql.append("`").append(xAxi.getFieldKey()).append("`,");
            }
        }
        for (FieldsData yAxi : chartDesignInParameter.getYAxis()) {
            if (yAxi.getFieldType().equals(DataFieldTypeEnum.DATETIME)) {
                sql.append("cast(`").append(yAxi.getFieldKey()).append("` as STRING) AS `").append(yAxi.getFieldKey()).append("`,");
            } else {
                sql.append("`").append(yAxi.getFieldKey()).append("`,");
            }
        }
        sql.delete(sql.length() - 1, sql.length());
        sql.append(" FROM ")
                .append(getDataParameter.getTableName())
                .append(getDataParameter.getWhere());
        RowWhereDto dataFactoryDataAuth = ChartElementInterface.super.getDataFactoryDataAuth(getDataParameter.getDataFactoryId());
        if (!dataFactoryDataAuth.getWhereStr().isEmpty()) {
            sql.append(" and (").append(dataFactoryDataAuth.getWhereStr()).append(")");
            getDataParameter.getParameter().addAll(dataFactoryDataAuth.getInParameter());
        }
        List<Object> parameter = JSONArray.copyOf(getDataParameter.getParameter());
        List<OrderField> orderFieldList = chartDesignInParameter.getXAxis().stream().map(e -> new OrderField().setFieldKey(e.getFieldKey()).setSortType(SortTypeEnums.desc)).collect(Collectors.toList());
        if (getDataParameter.getSortFields().isEmpty()) {
            getDataParameter.setSortFields(orderFieldList);
        }
        List<Map<String, Object>> page = dorisJdbcTemplate.getDataPage(chartDesignInParameter.getSize(), chartDesignInParameter.getCurrent(), sql.toString(), getDataParameter.getParameter(), getDataParameter.getSortFields());
        StringBuilder countSql = new StringBuilder();
        countSql.append("SELECT COUNT(*) as count ")
                .append(" FROM ")
                .append(getDataParameter.getTableName())
                .append(getDataParameter.getWhere());
        if (!dataFactoryDataAuth.getWhereStr().isEmpty()) {
            countSql.append(" and (").append(dataFactoryDataAuth.getWhereStr()).append(")");
        }
        String count = dorisJdbcTemplate.queryForList(countSql.toString(), parameter.toArray()).get(0).get("count").toString();
        ChartReturnObj.TableData tableData = new ChartReturnObj.TableData();
        page = this.tableClassify(page, chartDesignInParameter.getXAxis());
        tableData.setCurrent(chartDesignInParameter.getCurrent());
        tableData.setSize(chartDesignInParameter.getSize());
        tableData.setRecords(page);
        tableData.setTotal(Long.parseLong(count));
        List<FieldsData> xAxis = chartDesignInParameter.getXAxis();
        xAxis.addAll(chartDesignInParameter.getYAxis());
        List<ChartReturnObj.TableHeader> headers = xAxis.stream().map(e -> {
            ChartReturnObj.TableHeader tableHeader = new ChartReturnObj.TableHeader();
            tableHeader.setLabel(e.getAliasName());
            tableHeader.setProp(e.getFieldKey());
            return tableHeader;
        }).collect(Collectors.toList());
        return chartReturnObj
                .setHeader(headers)
                .setData(tableData);
    }

    /**
     * 多级分类
     */
    private List<Map<String, Object>> tableClassify(List<Map<String, Object>> list, List<FieldsData> classifyTableField) {
        for (int i = 0; i < classifyTableField.size(); i++) {
            //设置key
            int finalI = i;
            List<Map<String, Object>> maps = list.stream().map(e -> {
                StringBuilder value = new StringBuilder();
                for (int j = 0; j <= finalI; j++) {
                    String fieldKey = classifyTableField.get(j).getFieldKey();
                    Object o = e.get(fieldKey);
                    if (o != null) {
                        value.append(o);
                    }
                }
                e.put(ClassifyTableGroupByKey, value.toString());
                return e;
            }).collect(Collectors.toList());
            Map<Object, Long> longMap = maps.stream().collect(Collectors.groupingBy(e -> e.get(ClassifyTableGroupByKey), Collectors.counting()));
            list = list.stream()
                    .map(e -> {
                        //获取结果
                        List<Integer> rowspanKey = (List<Integer>) e.getOrDefault(ROWSPAN_KEY, new ArrayList<Integer>());
                        List<Integer> colspanKey = (List<Integer>) e.getOrDefault(COLSPAN_KEY, new ArrayList<Integer>());
                        Integer colspanKeyVal = 0;
                        StringBuilder value = new StringBuilder();
                        for (int j = 0; j <= finalI; j++) {
                            String fieldKey = classifyTableField.get(j).getFieldKey();
                            Object oj = e.get(fieldKey);
                            if (oj != null) {
                                value.append(oj);
                            }
                        }
                        if (longMap.get(value.toString()) > 0L) {
                            colspanKeyVal = 1;
                        }
                        rowspanKey.add(Integer.valueOf(longMap.get(value.toString()).toString()));
                        colspanKey.add(colspanKeyVal);
                        longMap.put(value.toString(), 0L);
                        e.put(ROWSPAN_KEY, rowspanKey);
                        e.put(COLSPAN_KEY, colspanKey);
                        return e;
                    })
                    .collect(Collectors.toList());
        }
        return list;
    }
}
