package cn.bctools.design.data.source.impl.mongo;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.source.impl.sql.DynamicCriteria;
import cn.bctools.design.data.source.impl.sql.DynamicQuery;
import cn.bctools.design.data.source.impl.sql.enums.DynamicKeyword;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author: ZhuXiaoKang
 * @Description: MongoDB条件转换
 * <p> 将通用条件转换为MongoDB条件
 */
public class MongoCriteriaUtil {
    public static final String MONGO_ID = "_id";
    /**
     * 正则表达式相关的特殊字符
     */
    public static final char[] REGULAR_CHARS = "\\^$*+?.{}[]()|".toCharArray();

    private MongoCriteriaUtil() {
    }

    /**
     * 将通用条件转换为mongo的查询语句
     *
     * @param dynamicQuery
     * @return
     */
    protected static Query convert(DynamicQuery dynamicQuery) {
        Query query = new Query();
        where(query, dynamicQuery.getCriteria());
        exclude(query, dynamicQuery.getExclude());
        include(query, dynamicQuery.getInclude());
        sort(query, dynamicQuery.getSort());
        skip(query, dynamicQuery.getOffset());
        limit(query, dynamicQuery.getLimit());
        return query;
    }

    /**
     * 条件
     *
     * @param query
     * @param dynamicCriteria
     */
    private static void where(Query query, DynamicCriteria dynamicCriteria) {
        Criteria criteria = buildCondition(dynamicCriteria);
        if (ObjectNull.isNull(criteria)) {
            return;
        }
        query.addCriteria(criteria);
    }

    /**
     * 排除字段
     *
     * @param query
     * @param exclude
     */
    private static void exclude(Query query, List<String> exclude) {
        if (ObjectNull.isNull(exclude)) {
           return;
        }
        String[] excludeArr = new String[exclude.size()];
        query.fields().exclude(exclude.toArray(excludeArr));
    }

    /**
     * 查询字段
     *
     * @param query
     * @param include
     */
    private static void include(Query query, List<String> include) {
        if (ObjectNull.isNull(include)) {
            return;
        }
        String[] includeArr = new String[include.size()];
        query.fields().include(include.toArray(includeArr));
    }

    /**
     * 排序
     *
     * @param query
     * @param sort
     */
    private static void sort(Query query, Sort sort) {
        if (ObjectNull.isNull(sort)) {
            return;
        }
        query.with(sort);
    }

    /**
     * 跳过的数量
     *
     * @param query
     * @param skip
     */
    private static void skip(Query query, Long skip) {
        if (ObjectNull.isNull(skip)) {
            return;
        }
        query.skip(skip);
    }

    /**
     * 指定查询数量
     *
     * @param query
     * @param limit
     */
    private static void limit(Query query, Long limit) {
        if (ObjectNull.isNull(limit)) {
            return;
        }
        query.limit(limit.intValue());
    }

    /**
     * 组装条件
     *
     * @param dynamicCriteria 动态条件
     */
    private static Criteria buildCondition(DynamicCriteria dynamicCriteria) {
        LinkedList<DynamicCriteria> chain = dynamicCriteria.getChain();
        if (CollectionUtils.isEmpty(chain)) {
            return null;
        }
        Criteria criteria = new Criteria();
        buildCondition(criteria, null, chain);
        return criteria;
    }

    /**
     * 组装条件
     *
     * @param criteria 条件
     * @param logic 逻辑运算符
     * @param chain 条件链
     */
    private static void buildCondition(Criteria criteria, DynamicKeyword logic, LinkedList<DynamicCriteria> chain) {
        List<Criteria> criteriaList = new ArrayList<>();
        for (DynamicCriteria c : chain) {
            Criteria childCriteria = ObjectNull.isNull(c.getKey()) ? new Criteria() : Criteria.where(c.getKey());
            if (DynamicKeyword.AND.equals(c.getLogic()) || DynamicKeyword.OR.equals(c.getLogic()) || (ObjectNull.isNull(c.getLogic()) && ObjectNull.isNotNull(c.getChain()))) {
                buildCondition(childCriteria, c.getLogic(), c.getChain());
            } else {
                // 构造单个条件
                childCriteria = singleCondition(childCriteria, c);
            }
            criteriaList.add(childCriteria);
        }
        if (criteriaList.size() > 0) {
            Criteria[] criteriaArr = new Criteria[criteriaList.size()];
            if (DynamicKeyword.OR.equals(logic)) {
                criteria.orOperator(criteriaList.toArray(criteriaArr));
            } else {
                criteria.andOperator(criteriaList.toArray(criteriaArr));
            }
        }
    }

    /**
     * 构造单个条件
     *
     * @param criteria
     * @param dc
     * @return
     */
    private static Criteria singleCondition(Criteria criteria, DynamicCriteria dc) {
        if (ObjectNull.isNotNull(dc.getTrueCriteria())) {
            if (dc.getTrueCriteria()) {
                return Criteria.where(MONGO_ID).exists(true);
            } else {
                return Criteria.where(MONGO_ID).exists(false);
            }
        }
        DynamicKeyword logic = dc.getLogic();
        Object value = dc.getValue();
        switch (logic) {
            case EQ:
                criteria.is(value);
                break;
            case NE:
                criteria.ne(value);
                break;
            case IN:
                criteria.in(value);
                break;
            case NOTIN:
                criteria.nin(value);
                break;
            case GT:
                criteria.gt(value);
                break;
            case GTE:
                criteria.gte(value);
                break;
            case LT:
                criteria.lt(value);
                break;
            case LTE:
                criteria.lte(value);
                break;
            case LIKE:
                // 模糊查询, mongoTemplate只支持正则匹配
                criteria.regex(".*(" + value.toString() + ").*");
                break;
            case ISNULL:
                criteria.is("");
                break;
            case BETWEEN:
                String field = criteria.getKey();
                List<?> values = (List<?>)value;
                criteria = new Criteria().andOperator(
                        Criteria.where(field).gte(values.get(0)),
                        Criteria.where(field).lte(values.get(1))
                );
                break;
            case JSON_LIKE:
                criteria.regex(".*?" + value + ".*");
                break;
            default:
                break;
        }
        return criteria;
    }


    /**
     * 处理正则的特殊字符
     *
     * @param str 待处理字符串
     * @return 处理后的字符串
     */
/*    public static String parseRegular(String str) {
        if (StringUtils.isBlank(str)) {
            return str;
        }
        for (char regularChar : REGULAR_CHARS) {
            str = str.replace("" + regularChar, "\\" + regularChar);
        }
        return str;
    }*/
}
