package cn.bctools.rule.data.dm.utils;

import cn.bctools.common.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@AllArgsConstructor
@Accessors(chain = true)
public class DataExpressionVisitorAdapter extends ExpressionVisitorAdapter {

    private Map<String, Object> variables = new HashMap<>();


    @Override
    public void visit(Column column) {
        String columnName = getCleanColumnName(column);
        if (!variables.containsKey(columnName)) {
            throw new BusinessException("字段" + columnName + "数据未找到");
        }
    }

    @Override
    public void visit(EqualsTo expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(NotEqualsTo expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(GreaterThan expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(GreaterThanEquals expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(MinorThan expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(MinorThanEquals expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(LikeExpression expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(Between expr) {
        // 检查BETWEEN表达式的左边字段
        expr.getLeftExpression().accept(this);
        // 继续检查BETWEEN的边界值表达式（可选）
        expr.getBetweenExpressionStart().accept(this);
        expr.getBetweenExpressionEnd().accept(this);
    }

    @Override
    public void visit(InExpression expr) {
        // 检查IN表达式的左边字段
        expr.getLeftExpression().accept(this);

        // 检查IN表达式右边的值列表
        if (expr.getRightItemsList() != null) {
            expr.getRightItemsList().accept(new ItemsListVisitorAdapter() {
                @Override
                public void visit(ExpressionList expressionList) {
                    for (Expression ex : expressionList.getExpressions()) {
                        ex.accept(new DataExpressionVisitorAdapter(variables));
                    }
                }
            });
        }
    }

    @Override
    public void visit(IsNullExpression expr) {
        expr.getLeftExpression().accept(this);
    }

    @Override
    public void visit(IsBooleanExpression expr) {
        expr.getLeftExpression().accept(this);
    }

    @Override
    public void visit(AndExpression expr) {
        expr.getLeftExpression().accept(this);
        expr.getRightExpression().accept(this);
    }

    @Override
    public void visit(OrExpression expr) {
        expr.getLeftExpression().accept(this);
        expr.getRightExpression().accept(this);
    }

    @Override
    public void visit(NotExpression expr) {
        expr.getExpression().accept(this);
    }

    @Override
    public void visit(Parenthesis expr) {
        expr.getExpression().accept(this);
    }

    @Override
    public void visit(Function function) {
        // 检查函数参数中的字段引用
        if (function.getParameters() != null) {
            function.getParameters().accept(this);
        }
    }

    @Override
    public void visit(CaseExpression expr) {
        // 检查CASE表达式的各个部分
        if (expr.getSwitchExpression() != null) {
            expr.getSwitchExpression().accept(this);
        }
        if (expr.getWhenClauses() != null) {
            for (Expression when : expr.getWhenClauses()) {
                when.accept(this);
            }
        }
        if (expr.getElseExpression() != null) {
            expr.getElseExpression().accept(this);
        }
    }

    @Override
    public void visit(WhenClause expr) {
        expr.getWhenExpression().accept(this);
        expr.getThenExpression().accept(this);
    }

    public void visitBinaryExpression(BinaryExpression expr) {
        expr.getLeftExpression().accept(this);
        expr.getRightExpression().accept(this);
    }

    /**
     * 清理列名（去除引号）
     */
    private String getCleanColumnName(Column column) {
        return column.getColumnName().replaceAll("\"", "").replaceAll("'", "");
    }

    /**
     * 获取缺失的字段列表（用于调试）
     */
    public Set<String> getMissingFields() {
        return new HashSet<>();
    }
}
