package cn.bctools.data.factory.query;

import cn.bctools.auth.api.api.UserExtensionServiceApi;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.enums.QueryWhereEnums;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author xiaohui
 */
@Component
@Slf4j
public class QueryExecuteFactory {
    @Autowired
    UserExtensionServiceApi userExtensionServiceApi;

    /**
     * 数据筛选
     *
     * @param whereSql        sql语句
     * @param list            条件表达式
     * @param queryWhereEnums 条件拼接符
     */
    public List<Object> execute(List<QueryExecDto> list, StringBuffer whereSql, QueryWhereEnums queryWhereEnums) {
        //获取条件表达式
        String whereJoinMark = queryWhereEnums.equals(QueryWhereEnums.and) ? "and" : "or";
        List<Object> values = new ArrayList<>();
        list.forEach(e -> {
            Query query = SpringContextUtil.getBean(e.getMethod().getCls());
            List<Object> exec = query.exec(e, whereSql);
            values.addAll(exec);
            whereSql.append(" ").append(whereJoinMark).append(" ");
        });
        //删除结尾的连接符
        whereSql.delete(whereSql.length() - whereJoinMark.length() - 1, whereSql.length());
        return values;
    }

    /**
     * 数据筛选 单个 条件生成
     *
     * @param whereSql     sql语句
     * @param queryExecDto 条件表达式
     */
    public List<Object> execute(QueryExecDto queryExecDto, StringBuffer whereSql) {
        //判断是否为动态值
        if (Optional.ofNullable(queryExecDto.getIsDynamic()).orElse(Boolean.FALSE)) {
            String endTime = null;
            String startTime = null;
            switch (queryExecDto.getMethodValue()) {
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
//                case"dept":
//                    //当前用户的部门
//                case"deptAndSub":
//                    //当前用户的部门以及下级部门
//                case"user":
//                    //当前用户
//                case"role":
//                    //当前用户的角色
//                case"job":
//                    //当前用户的岗位
//                case"group":
//                    //当前用户的群组
                default:
                    throw new BusinessException("未知动态类型");
            }
            if (startTime != null && endTime != null) {
                queryExecDto.setMethodValue(JSONObject.toJSONString(Arrays.asList(startTime, endTime)));
            }
        }
        Boolean extension = Optional.ofNullable(queryExecDto.getIsUserExtension()).orElse(Boolean.FALSE);
        if (extension) {
            UserDto currentUser = UserCurrentUtils.getCurrentUser();
            Map<String, Object> exceptions = currentUser.getExceptions();
            log.info("触发扩展字段,获取到当前所有扩展字段为:{},当前设置的key为:{}", JSONObject.toJSONString(exceptions), queryExecDto.getMethodValue());
            Object object = exceptions.get(queryExecDto.getMethodValue());
            queryExecDto.setMethodValue(JSONObject.toJSONString(object));
        }
        //获取条件表达式
        Query query = SpringContextUtil.getBean(queryExecDto.getMethod().getCls());
        return query.exec(queryExecDto, whereSql);
    }

    /**
     * 数据筛选 单个 条件生成
     *
     * @param sql          sql语句--注意如果传入的sql 没有where  会自动添加 如果有 where 需要外成调用程序添加条件 and or
     * @param queryExecDto 条件表达式
     * @param addWhere     是否添加where 关键字
     */
    public List<Object> execute(QueryDto queryExecDto, StringBuffer sql, Boolean addWhere) {
        //条件入参
        List<Object> execute = new ArrayList<>();
        List<QueryExecDto> list = queryExecDto.getNodeTwigs();
        String expression = queryExecDto.getExpression();
        if (StrUtil.isNotEmpty(expression)) {
            //通过前端编号
            if (ArrayUtil.isNotEmpty(list)) {
                boolean isWhere = false;
                if (addWhere) {
                    isWhere = sql.indexOf("where") > 0 || sql.indexOf("WHERE") > 0;
                    if (isWhere) {
                        sql.append(" (");
                    } else {
                        sql.append(" where ");
                    }
                }
                for (QueryExecDto filterParam : list) {
                    StringBuffer whereSql = new StringBuffer().append(" ");
                    List<Object> objectList = this.execute(filterParam, whereSql);
                    execute.addAll(objectList);
                    whereSql.append(" ");
                    //替换
                    expression = expression.replaceAll(filterParam.getRelationshipNo(), whereSql.toString());
                }
                //替换条件拼接符
                expression = expression.replaceAll("&", " and ");
                expression = expression.replaceAll("\\|", " or ");
                //添加条件sql
                sql.append(expression);
                if (isWhere) {
                    sql.append(")");
                }
            }
        }
        return execute;
    }

}