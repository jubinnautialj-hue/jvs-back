package cn.bctools.screen.chart;

import cn.bctools.data.factory.api.dto.RowWhereDto;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.screen.chart.bo.FieldsData;
import cn.bctools.screen.chart.po.ChartDesignInParameter;
import cn.bctools.screen.chart.po.ChartReturnObj;
import cn.bctools.screen.chart.po.GetDataParameter;
import com.alibaba.fastjson2.JSONArray;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Slf4j
@Data
@Component
@AllArgsConstructor
public class BaseListChartBo implements ChartElementInterface {
    private DorisJdbcTemplate dorisJdbcTemplate;

    @Override
    public ChartReturnObj exec(GetDataParameter getDataParameter) {
        //返回值
        ChartReturnObj chartReturnObj = new ChartReturnObj()
                .setSeries(new ArrayList<>())
                .setCardContent(0)
                .setYAxisData(new ArrayList<>())
                .setXAxisData(new ArrayList<>());
        ChartDesignInParameter chartDesignInParameter = getDataParameter.getLogicSetting();
        List<FieldsData> xAxis = chartDesignInParameter.getXAxis();
        //基础sql
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        for (FieldsData xAxi : xAxis) {
            sql.append("`").append(xAxi.getFieldKey()).append("`,");
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
        tableData.setCurrent(chartDesignInParameter.getCurrent());
        tableData.setSize(chartDesignInParameter.getSize());
        tableData.setRecords(page);
        tableData.setTotal(Long.parseLong(count));
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
}
