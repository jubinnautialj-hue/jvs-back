package cn.bctools.screen.query;

import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.query.Query;
import cn.bctools.data.factory.query.QueryExecDto;
import cn.bctools.screen.chart.bo.ChartFilterJsonBo;
import cn.bctools.screen.chart.bo.ChartSettingBo;
import cn.bctools.screen.chart.bo.QueryParameterBo;
import cn.bctools.screen.enums.QueryParameterTypeEnums;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class QueryExecuteFactory {
    /**
     * 数据筛选
     */
    public List<Object> execute(ChartSettingBo chartSettingBo, Map<String, String> map, StringBuffer whereSql) {
        whereSql.append(" where 1=1");
        List<ChartFilterJsonBo.FilterData> filterData = chartSettingBo.getDataFilterJson().getDataFilter();
        List<Object> parameter = new ArrayList<>();
        if (!filterData.isEmpty()) {
            String whereJoinMark = chartSettingBo.getDataFilterJson().getIsAnd() ? "and" : "or";
            whereSql.append(" and (");
            List<Object> list = filterData.stream().flatMap(e -> {
                QueryExecDto queryExecDto = JSONObject.parseObject(JSONObject.toJSONString(e), QueryExecDto.class);
                Query query = SpringContextUtil.getBean(e.getMethod().getCls());
                List<Object> exec = query.exec(queryExecDto, whereSql);
                whereSql.append(" ").append(whereJoinMark).append(" ");
                return exec.stream();
            }).collect(Collectors.toList());
            parameter.addAll(list);
            whereSql.delete(whereSql.length() - whereJoinMark.length() - 1, whereSql.length());
            whereSql.append(")");
        }
        //查询条件
        List<ChartFilterJsonBo.FilterData> searchFilterJson = chartSettingBo.getSearchFilterJson();
        if (!searchFilterJson.isEmpty()) {
            List<ChartFilterJsonBo.FilterData> data = searchFilterJson.stream().filter(ChartFilterJsonBo.FilterData::getIsCheck)
                    .filter(e -> {
                        String value = map.getOrDefault(e.getId(), null);
                        return StrUtil.isNotBlank(value);
                    }).filter(e -> {
                        String value = map.getOrDefault(e.getId(), null);
                        QueryParameterBo queryParameterBo = JSONObject.parseObject(value, QueryParameterBo.class);
                        return StrUtil.isNotBlank(queryParameterBo.getValue());
                    }).collect(Collectors.toList());
            if (!data.isEmpty()) {
                whereSql.append("and (");
                data.forEach(e -> {
                    String value = map.get(e.getId());
                    QueryParameterBo queryParameterBo = JSONObject.parseObject(value, QueryParameterBo.class);
                    value = getDataValue(queryParameterBo);
                    QueryExecDto queryExecDto = JSONObject.parseObject(JSONObject.toJSONString(e), QueryExecDto.class);
                    queryExecDto.setMethodValue(value);
                    Query query = SpringContextUtil.getBean(e.getMethod().getCls());
                    List<Object> exec = query.exec(queryExecDto, whereSql);
                    whereSql.append(" and ");
                    parameter.addAll(exec);
                });
                whereSql.delete(whereSql.length() - 4, whereSql.length());
                whereSql.append(")");
            }
            //获取条件成立的
        }
        return parameter;
    }


    /**
     * 过滤添加为时间
     * 根据类型获取时间
     *
     * @param queryParameterBo 入参
     */
    private String getDataValue(QueryParameterBo queryParameterBo) {
        if (queryParameterBo.getType().equals(QueryParameterTypeEnums.select)) {
            String endTime = null;
            String startTime = null;
            Boolean isDay = Boolean.TRUE;
            switch (queryParameterBo.getValue()) {
                case "today":
                    //当天开始时间
                    startTime = DateUtil.format(DateUtil.beginOfDay(DateUtil.date()), DatePattern.NORM_DATETIME_MS_PATTERN);
                    endTime = DateUtil.format(DateUtil.endOfDay(DateUtil.date()), DatePattern.NORM_DATETIME_MS_PATTERN);
                    break;
                case "yesterday":
                    //昨天
                    startTime = DateUtil.format(DateUtil.beginOfDay(DateUtil.yesterday()), DatePattern.NORM_DATETIME_MS_PATTERN);
                    endTime = DateUtil.format(DateUtil.endOfDay(DateUtil.yesterday()), DatePattern.NORM_DATETIME_MS_PATTERN);
                    break;
                case "month":
                    //本月
                    startTime = DateUtil.format(DateUtil.beginOfMonth(DateUtil.date()), DatePattern.NORM_DATETIME_MS_PATTERN);
                    endTime = DateUtil.format(DateUtil.endOfMonth(DateUtil.date()), DatePattern.NORM_DATETIME_MS_PATTERN);
                    break;
                case "lastMonth":
                    //上月
                    startTime = DateUtil.format(DateUtil.beginOfMonth(DateUtil.lastMonth()), DatePattern.NORM_DATETIME_MS_PATTERN);
                    endTime = DateUtil.format(DateUtil.endOfMonth(DateUtil.lastMonth()), DatePattern.NORM_DATETIME_MS_PATTERN);
                    break;
                case "year":
                    //本年
                    startTime = DateUtil.format(DateUtil.beginOfYear(DateUtil.date()), DatePattern.NORM_DATETIME_MS_PATTERN);
                    endTime = DateUtil.format(DateUtil.endOfYear(DateUtil.date()), DatePattern.NORM_DATETIME_MS_PATTERN);
                    break;
                case "lastYear":
                    //去年
                    DateTime offset = DateUtil.offset(DateUtil.date(), DateField.YEAR, -1);
                    startTime = DateUtil.format(DateUtil.beginOfYear(offset), DatePattern.NORM_DATETIME_MS_PATTERN);
                    endTime = DateUtil.format(DateUtil.endOfYear(offset), DatePattern.NORM_DATETIME_MS_PATTERN);
                    break;
                case "week":
                    //本周
                    startTime = DateUtil.format(DateUtil.beginOfWeek(DateUtil.date()), DatePattern.NORM_DATETIME_MS_PATTERN);
                    endTime = DateUtil.format(DateUtil.endOfWeek(DateUtil.date()), DatePattern.NORM_DATETIME_MS_PATTERN);
                    break;
                case "lastWeek":
                    //上周
                    DateTime date = DateUtil.lastWeek();
                    startTime = DateUtil.format(DateUtil.beginOfWeek(date), DatePattern.NORM_DATETIME_MS_PATTERN);
                    endTime = DateUtil.format(DateUtil.endOfWeek(date), DatePattern.NORM_DATETIME_MS_PATTERN);
                    break;
                default:
                    isDay = Boolean.FALSE;
            }
            if (isDay) {
                return JSONObject.toJSONString(Arrays.asList(startTime, endTime));
            }
            return queryParameterBo.getValue();
        }
        switch (queryParameterBo.getType()) {
            case dept:
            case user:
            case role:
            case job:
            case group:
                List<String> ids = JSONArray.parseArray(queryParameterBo.getValue()).stream()
                        .map(e -> JSONObject.parseObject(e.toString()).getString("id"))
                        .collect(Collectors.toList());
                return JSONObject.toJSONString(ids);
            default:
                return queryParameterBo.getValue();
        }

    }
}
