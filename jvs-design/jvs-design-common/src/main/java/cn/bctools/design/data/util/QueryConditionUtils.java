package cn.bctools.design.data.util;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.design.data.fields.dto.QueryConditionExtendDto;
import cn.bctools.function.handler.ExpressionHandler;
import cn.bctools.function.utils.ExpressionParam;
import cn.bctools.function.utils.ExpressionUtils;
import cn.bctools.rule.dto.LinkTypeEnum;

import java.util.List;
import java.util.Map;

/**
 * @author jvs
 * 动态数据查询条件工具
 */
public class QueryConditionUtils {

    private QueryConditionUtils() {
    }

    /**
     * 替换条件值
     * <p>
     *     将条件值为字段key的值替换为该字段key对应的真实数据
     *
     * @param conditions 条件集合
     * @param data            数据
     */
    public static void replaceConditionValue(List<QueryConditionExtendDto> conditions, Map<String, Object> data) {
        if (ObjectNull.isNull(data)) {
            return;
        }
        conditions.forEach(condition -> {
            Object value = null;
            if (condition.getProp().equals(LinkTypeEnum.field)) {
                String valueFieldKey = (String) condition.getValue();
                value = data.get(valueFieldKey);
            }
            if (condition.getProp().equals(LinkTypeEnum.formula)) {
                String expression = condition.getFormulaContent();
                List<ExpressionParam> expressionParams = ExpressionUtils.parsePostfixExpression(expression);
                if (expressionParams.isEmpty()) {

                } else {
                    try {
                        Object result = SpringContextUtil.getBean(ExpressionHandler.class).calculate(expression, data, "pageButtonDisplay");
                        value = result;
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }
            condition.setValue(value);
        });
    }

}
