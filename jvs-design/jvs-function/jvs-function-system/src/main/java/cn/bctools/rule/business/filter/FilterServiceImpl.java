package cn.bctools.rule.business.filter;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.dto.LinkTypeEnum;
import cn.bctools.rule.dto.QueryConditionDto;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.DataQueryType;
import cn.bctools.rule.entity.enums.FilterType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author guojing
 */
@Slf4j
@Order(1)
@Service
@Rule(
        value = "数据过滤",
        group = RuleGroup.常用插件,
        returnType = ClassType.数组,
        customStructure = true,
        test = true,
        order = 7,
//        iconUrl = "rule-shaixuan",
        explain = "对数组对象进行筛选过滤"
)
public class FilterServiceImpl implements BaseCustomFunctionInterface<FilterDto> {

    @Override
    public Object execute(FilterDto filterDto, Map<String, Object> params) {
        FilterType filterType = FilterType.AND;
        try {
            filterType = FilterType.valueOf(filterDto.getType());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        List<Object> collect = filterDto.getBody();
        switch (filterType){
            case OR:
                collect = filterDto.getBody().stream()
                        .filter(e -> {
                            Map object = (Map) e;
                            return or(object,filterDto.getFilter());
                        }).collect(Collectors.toList());
                break;
            case AND:
                collect = filterDto.getBody().stream()
                        .filter(e -> {
                            Map object = (Map) e;
                            return and(object,filterDto.getFilter());
                        }).collect(Collectors.toList());
                break;
            default:
        }
        return collect;
    }

    private Boolean and(Map obj, List<QueryConditionDto> filter){
        if(ObjectNull.isNull(filter)){
            return true;
        }
        return filter.stream().allMatch(e -> filter(e, obj.get(e.getFieldKey())));
    }

    private Boolean or(Map obj, List<QueryConditionDto> filter){
        if(ObjectNull.isNull(filter)){
            return true;
        }
        return filter.stream().anyMatch(e -> filter(e, obj.get(e.getFieldKey())));
    }

    private boolean filter(QueryConditionDto condition, Object dataValue) {

        Object conditionValue = condition.getValue();
        switch (condition.getEnabledQueryTypes()) {
            case gt:
                // 大于
                return compare(DataQueryType.gt, conditionValue, dataValue);
            case lt:
                // 小于
                return compare(DataQueryType.lt, conditionValue, dataValue);
            case ge:
                // 大于等于
                return compare(DataQueryType.ge, conditionValue, dataValue);
            case le:
                // 小于等于
                return compare(DataQueryType.le, conditionValue, dataValue);
            case ne:
                // 不等于
                if (LinkTypeEnum.empty.equals(condition.getProp())) {
                    return ObjectNull.isNotNull(dataValue);
                } else {
                    return ObjectUtil.notEqual(conditionValue, dataValue);
                }
            case eq:
                // 等于
            default:
                if (LinkTypeEnum.empty.equals(condition.getProp())) {
                    return ObjectNull.isNull(dataValue);
                } else {
                    return ObjectUtil.equals(conditionValue, dataValue);
                }
        }
    }

    /**
     * 比较
     *
     * @param queryType 逻辑运算类型
     * @param conditionValue 条件值
     * @param dataValue 数据值
     * @return true-满足条件，false-不满足条件
     */
    private boolean compare(DataQueryType queryType, Object conditionValue, Object dataValue) {
        if (ObjectNull.isNull(conditionValue) || ObjectNull.isNull(dataValue)) {
            return false;
        }
        if (dataValue instanceof Number) {
           return compareNumber(queryType, conditionValue, dataValue);
        }
        return false;
    }

    /**
     * 比较数字
     *
     * @param queryType 逻辑运算类型
     * @param conditionValue 条件值
     * @param dataValue 数据值
     * @return true-满足条件，false-不满足条件
     */
    private boolean compareNumber(DataQueryType queryType, Object conditionValue, Object dataValue) {
        BigDecimal conditionValueBigDecimal;
        try {
            conditionValueBigDecimal = new BigDecimal(conditionValue.toString());
        } catch (Exception e) {
            throw new BusinessException("字段类型不匹配");
        }
        BigDecimal dataValueBigDecimal = new BigDecimal(dataValue.toString());
        int compare = dataValueBigDecimal.compareTo(conditionValueBigDecimal);
        if (DataQueryType.gt.equals(queryType)) {
            return compare > 0;
        }
        if (DataQueryType.lt.equals(queryType)) {
            return compare < 0;
        }
        if (DataQueryType.ge.equals(queryType)) {
            return compare >= 0;
        }
        if (DataQueryType.le.equals(queryType)) {
            return compare <= 0;
        }
        return false;
    }
}
