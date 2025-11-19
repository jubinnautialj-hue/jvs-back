package cn.bctools.rule.data.dm.utils;

import cn.bctools.common.utils.ObjectNull;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;

import java.util.*;

/**
 * The type Sql statement processor.
 *
 * @author jvs
 */
public class SQLStatementProcessor {
    private boolean strictValidation = true;
    // 是否启用严格验证

    private Map<String, Object> variables = new HashMap<>();

    /**
     * Instantiates a new Sql statement processor.
     *
     * @param variables the variables
     */
    public SQLStatementProcessor(Map<String, Object> variables) {
        if(ObjectNull.isNotNull(variables)) {
            this.variables = variables;
        }
    }

    /**
     * Process sql string.
     *
     * @param sql the sql
     * @return the string
     * @throws JSQLParserException the jsql parser exception
     */
    public String processSQL(String sql) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        ExpressionParameterValidator validator = new ExpressionParameterValidator(variables);

        if (statement instanceof Insert) {
            return processInsert((Insert) statement, validator);
        } else if (statement instanceof Update) {
            return processUpdate((Update) statement, validator);
        } else if (statement instanceof Delete) {
            return processDelete((Delete) statement, validator);
        } else if (statement instanceof Select) {
            return processSelect((Select) statement, validator);
        } else {
            throw new UnsupportedOperationException("Unsupported SQL statement type: " + statement.getClass().getSimpleName());
        }
    }

    // 处理INSERT语句
    private String processInsert(Insert insert, ExpressionParameterValidator validator) {
        List<Column> columns = insert.getColumns();
        if (columns != null && insert.getItemsList() instanceof ExpressionList) {
            ExpressionList expressionList = (ExpressionList) insert.getItemsList();
            List<Expression> expressions = expressionList.getExpressions();

            // 从后往前遍历，避免删除时的索引问题
            for (int i = columns.size() - 1; i >= 0; i--) {
                String columnName = getCleanColumnName(columns.get(i));
                if (!variables.containsKey(columnName) || variables.get(columnName) == null) {
                    columns.remove(i);
                    expressions.remove(i);
                }
            }
        }

        return insert.toString();
    }

    // 处理UPDATE语句 - 新版本适配
    private String processUpdate(Update update, ExpressionParameterValidator validator) {
        // 验证WHERE条件
        if (strictValidation && update.getWhere() != null) {
            validator.validateExpressionParameters(update.getWhere());
        }

        // 处理SET子句
        Iterator<UpdateSet> iterator = update.getUpdateSets().iterator();
        List<UpdateSet> setsToRemove = new ArrayList<>();

        while (iterator.hasNext()) {
            UpdateSet updateSet = iterator.next();
            List<Column> columns = updateSet.getColumns();

            boolean shouldRemove = false;
            for (Column column : columns) {
                String columnName = getCleanColumnName(column);
                if (!variables.containsKey(columnName) || variables.get(columnName) == null) {
                    shouldRemove = true;
                    break;
                }
            }

            if (shouldRemove) {
                setsToRemove.add(updateSet);
            }
        }

        update.getUpdateSets().removeAll(setsToRemove);

        return update.toString();
    }

    // 处理DELETE语句
    private String processDelete(Delete delete, ExpressionParameterValidator validator) {
        // 处理WHERE子句
        if (delete.getWhere() != null) {
            validateExpressionParameters(delete.getWhere());
        }

        return delete.toString();
    }

    // 处理SELECT语句
    private String processSelect(Select select, ExpressionParameterValidator validator) {
        SelectBody selectBody = select.getSelectBody();

        if (selectBody instanceof PlainSelect) {
            PlainSelect plainSelect = (PlainSelect) selectBody;

            // 处理WHERE子句
            if (plainSelect.getWhere() != null) {
                validateExpressionParameters(plainSelect.getWhere());
            }

            // 处理HAVING子句
            if (plainSelect.getHaving() != null) {
                validateExpressionParameters(plainSelect.getHaving());
            }
        }

        return select.toString();
    }

    // 处理WHERE表达式（简化版本）
    private void validateExpressionParameters(Expression expression) {
        // 这里可以实现更复杂的WHERE条件处理逻辑
        // 例如：分析表达式树，移除包含不存在变量的条件分支
        // 当前版本：简单处理，不修改WHERE条件
        // 可以根据需要扩展为递归处理复杂表达式

    }

    // 清理列名（去除引号等）
    private String getCleanColumnName(Column column) {
        return column.getColumnName().replaceAll("\"", "").replaceAll("'", "");
    }
}
