package cn.bctools.rule.data.dm.utils;


import cn.bctools.common.utils.ObjectNull;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONArray;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用于处理DM语句相关操作
 *
 * @author chenzy
 */
@Slf4j
public class SQLStringUtils {

    static Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");

    /**
     * 清理SQL语句中的多余逗号和空格
     *
     * @param sql 待清理的SQL语句
     * @return 清理后的SQL语句
     */
    private static String cleanSql(String sql) {
        // 保存字符串中的内容，避免清理掉字符串中的逗号
        List<String> stringLiterals = new ArrayList<>();
        Matcher stringMatcher = Pattern.compile("'[^']*'").matcher(sql);
        int stringIndex = 0;

        // 提取并保存字符串字面量
        while (stringMatcher.find()) {
            String literal = stringMatcher.group();
            stringLiterals.add(literal);
            sql = sql.replaceFirst("'[^']*'", "STRING_LITERAL_" + stringIndex);
            stringIndex++;
        }

        // 清理SELECT语句中字段间的多余逗号
        String cleanedSql = sql.replaceAll("\\s*,\\s*(?=\\s*,)", ",")
                .replaceAll("\\(\\s*,", "(")
                .replaceAll(",\\s*\\)", ")")
                .replaceAll("^\\s*SELECT\\s*,", "SELECT ")
                .replaceAll(",\\s*FROM", " FROM")
                .replaceAll("^\\s*,\\s*", "")
                .replaceAll("\\s*,\\s*$", "")
                .replaceAll(",\\s*'", "'");

        // 清理WHERE语句中的多余AND/OR
        cleanedSql = cleanedSql.replaceAll("\\s+AND\\s+(?=AND\\s+)", "AND ")
                .replaceAll("\\s+OR\\s+(?=OR\\s+)", "OR ")
                .replaceAll("\\(\\s*AND\\s+", "(")
                .replaceAll("\\(\\s*OR\\s+", "(")
                .replaceAll("\\s+AND\\s*\\)", ")")
                .replaceAll("\\s+OR\\s*\\)", ")")
                .replaceAll("AND\\s*,", "AND")
                .replaceAll("OR\\s*,", "OR");

        // 清理多余的空格
        cleanedSql = cleanedSql.replaceAll("\\s+", " ").trim();

        // 恢复字符串字面量
        for (int i = 0; i < stringLiterals.size(); i++) {
            cleanedSql = cleanedSql.replaceFirst("STRING_LITERAL_" + i, stringLiterals.get(i));
        }

        return cleanedSql;
    }

    private static final ExpressionParser parser = new SpelExpressionParser();


    private static String extractMissingParameter(String errorMessage) {
        // 从错误消息中提取参数名
        // 示例: "EL1008E: Property or field 'xxx' cannot be found"
        return errorMessage.replaceAll(".*['\"]([^'\"]+)['\"].*", "$1");
    }

    private static void validateSpelExpression(String expressionStr, Map<String, Object> parameters) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        parameters.forEach(context::setVariable);

        try {
            org.springframework.expression.Expression expression = parser.parseExpression(expressionStr);
            expression.getValue(context);

        } catch (org.springframework.expression.spel.SpelEvaluationException e) {
            if (e.getMessage().contains("cannot be found")) {
                String missingParam = SQLStringUtils.extractMissingParameter(e.getMessage());
                throw new RuntimeException("参数不完整: 参数 '" + missingParam + "' 必须提供");
            }
            throw e;
        }
    }


    @SneakyThrows
    public static String replaceMissingVariables(String sql, Map<String, Object> variables) {
        // 匹配 ${variableName} 模式的正则表达式
        Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(sql);

        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String variableName = matcher.group(1); // 获取变量名
            String replacement;

            if (variables.containsKey(variableName) && variables.get(variableName) != null) {
                // 如果变量存在且不为null，使用原值
                Object value = variables.get(variableName);
                if (value instanceof String) {
                    // 字符串类型需要转义单引号
                    replacement = "'" + value.toString().replace("'", "''") + "'";
                } else {
                    replacement = value.toString();
                }
            } else {
                // 变量不存在或为null，替换为空字符串
                replacement = "?";
            }

            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);
//        validateSpelExpression(result.toString(), variables);
        return new SQLStatementProcessor(variables).processSQL(result.toString());
//
//        Statement statement = CCJSqlParserUtil.parse(result.toString());
//        if (statement instanceof Insert) {
//            Insert insert = (Insert) statement;
//            // 获取列和表达式
//            List<Column> columns = insert.getColumns();
//            ExpressionList itemsList = (ExpressionList) insert.getItemsList();
//            for (int i = itemsList.getExpressions().size() - 1; i > 0; i--) {
//                if (!variables.containsKey(columns.get(i).getColumnName().replaceAll("\"",""))) {
//                    itemsList.getExpressions().remove(i);
//                    columns.remove(i);
//                }
//            }
//            // 这里可以精确修改SQL结构
//            // 移除不存在的参数对应的列和值
//            return insert.toString();
//        }
//        return result.toString();
    }

    /**
     * 替换sql语句站位符
     *
     * @param
     * @return
     */
    public static String replace(String sql, Map param) {
        if(ObjectNull.isNull(param)){
            return sql;
        }
        sql = replaceMissingVariables(sql, param);
        List<String> variables = new ArrayList<>();

        // 正则表达式匹配 ${xxx} 格式
        Matcher matcher = pattern.matcher(sql);

        while (matcher.find()) {
            // group(1) 获取括号内的内容
            variables.add(matcher.group(1));
        }
        String s = sql;

        // 如果没有参数，则移除所有变量占位符并清理SQL
        if (CollectionUtil.isEmpty(param)) {
            s = matcher.replaceAll("");
            return cleanSql(s);
        }

        // 先替换提供的参数
        for (Object o : param.keySet()) {
            if(ObjectNull.isNull(param.get(o))){
                continue;
            }
            //判断如果是数组
            if (param.get(o) instanceof JSONArray) {
                String s1 = param.get(o).toString();
                s1 = StrUtil.replaceChars(s1, "[", "(");
                s1 = StrUtil.replaceChars(s1, "]", ")");
                s1 = StrUtil.replaceChars(s1, "\"", "'");
                s = StrUtil.replace(s, "${" + o + "}", s1);
            } else {
                s = StrUtil.replace(s, "${" + o + "}", "'" + param.get(o).toString() + "'");
            }
        }

        // 移除未提供参数的变量占位符并清理SQL
        Matcher finalMatcher = pattern.matcher(s);
        s = finalMatcher.replaceAll("");
        s = cleanSql(s);

        log.info("拼接的 sql为: " + s);
        return s;
    }


}
