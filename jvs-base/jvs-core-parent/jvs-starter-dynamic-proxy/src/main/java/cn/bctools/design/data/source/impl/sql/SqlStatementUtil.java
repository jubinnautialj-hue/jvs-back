package cn.bctools.design.data.source.impl.sql;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.design.data.source.impl.sql.enums.DynamicKeyword;
import cn.bctools.design.data.source.impl.sql.enums.DynamicSqlMethod;
import cn.bctools.design.data.source.impl.sql.methods.AbstractSqlMethod;
import cn.bctools.design.data.source.impl.sql.methods.DynamicSqlInfo;
import cn.bctools.design.data.source.impl.sql.methods.WhereParam;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: ZhuXiaoKang
 * @Description: 转换mybatis-plus支持的数据库条件sql
 */
public class SqlStatementUtil {
    protected static final SqlFunction SQL_FUNCTION = SpringContextUtil.getBean(SqlFunction.class);

    private SqlStatementUtil() {
    }

    public static <T> SqlStatement getStatement(String tableName, DynamicSqlMethod method, T data) {
       return getStatement(tableName, method, null, data);
    }

    public static SqlStatement getStatement(String tableName, DynamicSqlMethod method, DynamicQuery dynamicQuery) {
        return getStatement(tableName, method, dynamicQuery, null);
    }

    public static <T> SqlStatement getStatement(String tableName, DynamicSqlMethod method, DynamicQuery dynamicQuery, T data) {
        DynamicSqlInfo dynamicSqlInfo = new DynamicSqlInfo()
                .setTableName(tableName)
                .setDynamicQuery(dynamicQuery)
                .setData(data);
        return inject(method, dynamicSqlInfo);
    }

    @SneakyThrows
    private static SqlStatement inject(DynamicSqlMethod sqlMethod, DynamicSqlInfo dynamicSqlInfo) {
        Object[] args = {dynamicSqlInfo};
        Class[] argClass = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            argClass[i] = args[i].getClass();
        }
        Class<?> cls = sqlMethod.getMethod();
        Object obj = cls.newInstance();
        Method method = obj.getClass().getDeclaredMethod(AbstractSqlMethod.METHOD, argClass);
        return (SqlStatement)method.invoke(obj, args);
    }

    /**
     * 条件
     *
     * @param dynamicCriteria
     */
    public static WhereParam where(DynamicCriteria dynamicCriteria) {
        WhereParam whereSql = buildCondition(dynamicCriteria);
        if (ObjectNull.isNull(whereSql)) {
            return WhereParam.empty();
        }
        return whereSql;
    }


    /**
     * 组装条件
     *
     * @param dynamicCriteria 动态条件
     */
    private static WhereParam buildCondition(DynamicCriteria dynamicCriteria) {
        LinkedList<DynamicCriteria> chain = dynamicCriteria.getChain();
        if (CollectionUtils.isEmpty(chain)) {
            return null;
        }
        WhereParam whereParam = new WhereParam();
        StringBuilder sqlCondition = new StringBuilder();
        Map<String, Object> param = new HashMap<>(8);
        buildCondition(sqlCondition, null, chain, param);
        whereParam.setSqlSegment(sqlCondition.toString());
        whereParam.setParam(param);
        return whereParam;
    }

    /**
     * 组装条件
     *
     * @param sqlCondition 条件
     * @param logic 逻辑运算符
     * @param chain 条件链
     */
    private static void buildCondition(StringBuilder sqlCondition, DynamicKeyword logic, LinkedList<DynamicCriteria> chain, Map<String, Object> param) {
        Iterator<DynamicCriteria> iterator = chain.iterator();
        if (chain.size() >= 2) {
            sqlCondition.append(StringPool.LEFT_BRACKET);
        }
        while (iterator.hasNext()) {
            String logicName = DynamicKeyword.OR.equals(logic) ? SqlKeyword.OR.name() : SqlKeyword.AND.name();
            DynamicCriteria criteria = iterator.next();
            QueryWrapper<Object> queryWrapper = null;
            if (DynamicKeyword.AND.equals(criteria.getLogic()) || DynamicKeyword.OR.equals(criteria.getLogic()) || (ObjectNull.isNull(criteria.getLogic()) && ObjectNull.isNotNull(criteria.getChain()))) {
                DynamicKeyword cdLogic = ObjectNull.isNull(criteria.getLogic()) ? logic : criteria.getLogic();
                StringBuilder childSql = new StringBuilder();
                buildCondition(childSql, cdLogic, criteria.getChain(), param);
                sqlCondition.append(childSql);
                if (iterator.hasNext()) {
                    sqlCondition.append(logicName);
                }
            } else {
                queryWrapper = Wrappers.query();
                // 构造单个条件
                singleCondition(queryWrapper, criteria);
            }
            if (ObjectNull.isNull(queryWrapper)) {
                continue;
            }
            Map<String, Object> paramNameValuePairs = queryWrapper.getParamNameValuePairs();
            String sqlSegment = queryWrapper.getSqlSegment();
            if (paramNameValuePairs.size() == 1) {
                // mybatis条件会自动加上(), 这去除两边的括号
                sqlSegment = StrUtil.strip(sqlSegment, StringPool.LEFT_BRACKET, StringPool.RIGHT_BRACKET);
            }
            sqlCondition
                    .append(StringPool.SPACE)
                    .append(sqlSegment.replaceAll("ew.paramNameValuePairs.", Constant.WHERE_PARAM + StringPool.DOT + criteria.getKey()))
                    .append(StringPool.SPACE);
            if (iterator.hasNext()) {
                sqlCondition.append(logicName);
            }
            Map<String, Object> whereParam = queryWrapper.getParamNameValuePairs()
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(entry -> criteria.getKey() + entry.getKey(), Map.Entry::getValue));
            param.putAll(whereParam);
        }
        if (chain.size() >= 2) {
            sqlCondition.append(StringPool.RIGHT_BRACKET);
        }
    }

    /**
     * 构造单个条件
     *
     * @param queryWrapper
     * @param dc
     * @return
     */
    private static void singleCondition(QueryWrapper<Object> queryWrapper, DynamicCriteria dc) {
        if (ObjectNull.isNotNull(dc.getTrueCriteria())) {
            if (dc.getTrueCriteria()) {
                queryWrapper.isNotNull(Constant.PRIMARY_KEY);
            } else {
                queryWrapper.isNull(Constant.PRIMARY_KEY);
            }
            return;
        }
        DynamicKeyword logic = dc.getLogic();
        String field = SqlScriptUtil.backtick(dc.getKey());
        Object value = dc.getValue();
        switch (logic) {
            case EQ:
                SQL_FUNCTION.eq().apply(dc.getKey(), value, queryWrapper);
                break;
            case NE:
                SQL_FUNCTION.ne().apply(dc.getKey(), value, queryWrapper);
                break;
            case IN:
                queryWrapper.in(field, value);
                break;
            case NOTIN:
                queryWrapper.notIn(field, value);
                break;
            case GT:
                queryWrapper.gt(field, value);
                break;
            case GTE:
                queryWrapper.ge(field, value);
                break;
            case LT:
                queryWrapper.lt(field, value);
                break;
            case LTE:
                queryWrapper.le(field, value);
                break;
            case LIKE:
                queryWrapper.like(field, value);
                break;
            case ISNULL:
                queryWrapper.isNull(field);
                break;
            case BETWEEN:
                List<?> values = (List<?>)value;
                queryWrapper.between(field, values.get(0), values.get(1));
                break;
            case JSON_LIKE:
                String applySql = SQL_FUNCTION.jsonLike().apply(field).trim();
                if (ObjectNull.isNotNull(applySql)) {
                    queryWrapper.apply(applySql, value);
                }
                break;
            default:
                break;
        }
    }
}
