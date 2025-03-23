package cn.bctools.design.data.util;

import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.design.data.fields.dto.QueryConditionDto;
import cn.bctools.function.handler.ExpressionHandler;
import cn.bctools.function.utils.ExpressionParam;
import cn.bctools.function.utils.ExpressionUtils;
import cn.bctools.rule.dto.LinkTypeEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * @author jvs
 * 动态数据查询条件工具
 */
@Slf4j
public class QueryConditionUtils {

    private QueryConditionUtils() {
    }

    /**
     * 替换条件值
     * <p>
     * 将条件值为字段key的值替换为该字段key对应的真实数据
     *
     * @param conditions 条件集合
     * @param data       数据
     */
    public static void replaceConditionValue(List<QueryConditionDto> conditions, Map<String, Object> data, boolean field) {
        conditions.forEach(condition -> {
            Object value = null;
            if (condition.getProp().equals(LinkTypeEnum.field) && field) {
                String valueFieldKey = (String) condition.getValue();
                value = data.get(valueFieldKey);
                condition.setValue(value);
            }
            if (condition.getProp().equals(LinkTypeEnum.formula)) {
                String expression = condition.getFormulaContent();
                List<ExpressionParam> expressionParams = ExpressionUtils.parsePostfixExpression(expression);
                if (expressionParams.isEmpty()) {

                } else {
                    try {
                        value = SpringContextUtil.getBean(ExpressionHandler.class).calculate(expression, data, "pageButtonDisplay");
                        condition.setValue(value);
                    } catch (Exception exception) {
                        log.error("列表数据筛选公式取值失败", exception);
                    }
                }
            }

        });
    }

    public static void replaceConditionValue(List<QueryConditionDto> conditions, Map<String, Object> data) {
        replaceConditionValue(conditions, data, true);
    }

}
