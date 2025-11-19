package cn.bctools.rule.data.dm.utils;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import java.util.*;

public class ExpressionParameterValidator {
    
    private Map<String, Object> variables = new HashMap<>();
    private Set<String> missingFields = new HashSet<>();
    
    public ExpressionParameterValidator(Map<String, Object> variables) {
        this.variables = variables;
    }
    
    /**
     * 验证表达式中的参数是否都在variables中存在
     * @param expression 要验证的表达式
     * @throws IllegalArgumentException 如果发现不存在的字段
     */
    public void validateExpressionParameters(Expression expression) {
        missingFields.clear();
        
        if (expression != null) {
            validateExpression(expression);
        }
        
        if (!missingFields.isEmpty()) {
            throw new IllegalArgumentException("条件字段未找到: " + String.join(", ", missingFields));
        }
    }
    
    /**
     * 递归验证表达式
     */
    private void validateExpression(Expression expression) {
        if (expression == null) return;
        
        // 使用访问者模式遍历表达式树
        DataExpressionVisitorAdapter expressionVisitor = new DataExpressionVisitorAdapter(variables);
        expression.accept(expressionVisitor);

    }

}
