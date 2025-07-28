package cn.bctools.function.utils;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.function.enums.JvsParamType;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import groovy.json.StringEscapeUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 表达式工具类
 *
 * @Author: GuoZi
 */
@Slf4j
public class ExpressionUtils {

    /**
     * 参数分隔符
     */
    public static final String PARAM_SPLIT = ",";
    /**
     * 参数占位符, 用于统计函数的参数数量
     */
    public static final String PARAM_PLACEHOLDER = "?";
    /**
     * 单目运算符的后缀标记
     */
    public static final String POSTFIX_UNARY_OPERATOR = "_unary";
    /**
     * 参数的前后缀标记
     */
    public static final String PREFIX_PARAM = "${";
    public static final String POSTFIX_PARAM = "}";
    /**
     * 2.1.9
     */
    public static final String PREFIX2_PARAM = "`";

    private ExpressionUtils() {
    }

    public static void main(String[] args) {
        List<ExpressionParam> expressionParams = ExpressionUtils.parsePostfixExpression(
                "${CONTRACT}(\"12321\",\"\\nfadsfa\",\"afsafds\\n\",\u200B\u200B\u200B\"fdasfsa\")");
        System.out.println(expressionParams);
    }

    /**
     * 解析后缀表达式
     *
     * @param expression 原表达式
     * @return 后缀表达式的各参数集合
     */
    public static List<ExpressionParam> parsePostfixExpression(String expression) {
        if (StringUtils.isBlank(expression)) {
            return Collections.emptyList();
        }
        expression = StringEscapeUtils.unescapeJava(expression);
        // 当前是否在处理字符串
        boolean isStr = false;
        // 当前是否为转义符
        boolean isEscape = false;
        // 符号栈, 包括参数占位符, 参数分隔符
        Stack<String> operatorStack = new Stack<>();
        // 暂存参数
        StringBuilder builder = new StringBuilder();
        // 记录后缀表达式
        List<ExpressionParam> postfixExpression = new ArrayList<>();
        for (int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);
            // 对字符串的转义特殊处理
            if (isEscape) {
                isEscape = false;
                builder.append(ch);
                continue;
            }
            if (isStr && ch != '"') {
                if (ch == '\\') {
                    //并且下一个是 n
                    char c = expression.charAt(++i);
                    if (c == 'n') {
                        builder.append(ch);
                        builder.append(c);
                    } else {
                        i--;
                        isEscape = true;
                    }
                } else if (ch == '\n') {
                    builder.append(ch);
                } else {
                    builder.append(ch);
                }
                continue;
            }
            String suffix = "(";
            switch (ch) {
                case '(': {
                    // 数学表达式的左括号, 或函数调用的左括号, 直接入栈, 具体处理见')'
                    String functionName = trimBlank(builder.toString() + suffix);
                    operatorStack.push(functionName);
                    builder.delete(0, builder.length());
                    break;
                }
                case ')': {
                    // 数学表达式的右括号, 或函数调用的右括号, 函数的参数数量由'?'决定
                    // 记录参数数量
                    int paramCount = 0;
                    boolean firstParam = true;
                    // 先保存参数
                    saveParam(builder, operatorStack, postfixExpression, false);
                    // 将最后一个'('之前的符号弹出
                    while (!operatorStack.empty() && !operatorStack.peek().endsWith(suffix)) {
                        String operator = trimBlank(operatorStack.pop());
                        if (PARAM_PLACEHOLDER.equals(operator)) {
                            // 统计参数数量
                            paramCount++;
                        }
                        if (firstParam && PARAM_SPLIT.equals(operator)) {
                            throw new BusinessException("逗号两边的参数值不能为空");
                        }
                        // 保存数学表达式运算符
                        saveOperator(postfixExpression, operator, 2);
                        firstParam = false;
                    }
                    if (operatorStack.empty()) {
                        throw new BusinessException("括号必须成对使用");
                    }
                    // 弹出'('
                    String functionName = trimBlank(operatorStack.pop());
                    if (!suffix.equals(functionName)) {
                        // 保存函数调用
                        functionName = functionName.substring(0, functionName.length() - 1);
                        saveOperator(postfixExpression, functionName, paramCount);
                    }
                    addParamPlaceholder(operatorStack, postfixExpression);
                    break;
                }
                case '[': {
                    // 数组的左方括号, 直接入栈, 处理方式类似'('
                    saveParam(builder, operatorStack, postfixExpression, false);
                    boolean hasParamInStack = !operatorStack.empty() && PARAM_PLACEHOLDER.equals(operatorStack.peek());
                    if (hasParamInStack) {
                        throw new BusinessException("'['前面不能有参数");
                    }
                    operatorStack.push(String.valueOf(ch));
                    builder.delete(0, builder.length());
                    break;
                }
                case ']': {
                    // 数组的右方括号, 两个方括号之间的内容都当作一个数组, 数组长度由'?'决定
                    // 记录参数数量
                    int paramCount = 0;
                    boolean firstParam = true;
                    // 先保存参数
                    saveParam(builder, operatorStack, postfixExpression, false);
                    // 将最后一个'['之前的符号弹出
                    while (!operatorStack.empty()) {
                        String s = "[";
                        if (s.equals(operatorStack.peek())) {
                            break;
                        }
                        String operator = trimBlank(operatorStack.pop());
                        if (PARAM_PLACEHOLDER.equals(operator)) {
                            // 统计参数数量
                            paramCount++;
                        }
                        if (firstParam && PARAM_SPLIT.equals(operator)) {
                            throw new BusinessException("逗号两边的参数值不能为空");
                        }
                        // 保存数学表达式运算符
                        saveOperator(postfixExpression, operator, 2);
                        firstParam = false;
                    }
                    if (operatorStack.empty()) {
                        throw new BusinessException("方括号必须成对使用");
                    }
                    // 弹出'['
                    String operator = trimBlank(operatorStack.pop());
                    saveOperator(postfixExpression, operator, paramCount);
                    addParamPlaceholder(operatorStack, postfixExpression);
                    break;
                }
                case '"': {
                    // 双引号是字符串的边界, 两个双引号之间的字符都当作一个字符串
                    if (isStr) {
                        // 字符串结束
                        saveParam(builder, operatorStack, postfixExpression, true);
                    }
                    isStr = !isStr;
                    break;
                }
                case ',': {
                    // 逗号是各参数之间的边界, 需要弹出符号栈中最后一个逗号、左括号前的所有数学运算符
                    boolean hasParam = false;
                    saveParam(builder, operatorStack, postfixExpression, false);
                    while (!operatorStack.empty()) {
                        String operator = operatorStack.peek();
                        if (PARAM_SPLIT.equals(operator) || operator.endsWith(suffix) || "[".equals(operator)) {
                            break;
                        }
                        hasParam = true;
                        operatorStack.pop();
                        saveOperator(postfixExpression, operator, 2);
                    }
                    // 存入符号栈, 记录参数边界
                    if (!hasParam) {
                        throw new BusinessException("逗号两边的参数值不能为空");
                    }
                    addParamPlaceholder(operatorStack, postfixExpression);
                    operatorStack.push(String.valueOf(ch));
                    break;
                }
                default: {
                    // 处理剩下的字符, 包括参数与数学运算符
                    String mark = String.valueOf(ch);
                    if (OperatorType.SUB.mark.equals(mark)) {
                        //有可能是减法，有可能是负,判断是否是 括号
                        //取出来判断,在放进去
                        if (operatorStack.size() > 0 && (operatorStack.size() - 1 > 0)) {
                            if (operatorStack.get(operatorStack.size() - 1).endsWith("(")) {
                                // 参数
                                builder.append(ch);
                                break;
                            } else if (operatorStack.get(operatorStack.size() - 1).endsWith(",")) {
                                // 参数
                                builder.append(ch);
                                break;
                            } else {
                                //为减号
                            }
                        }
                    }
                    OperatorType operatorType = OperatorType.getByMark(mark);
                    if (Objects.isNull(operatorType)) {
                        // 参数
                        builder.append(ch);
                        break;
                    }
                    // 数学运算符
                    saveParam(builder, operatorStack, postfixExpression, false);
                    if (operatorType.isEnableUnaryOperator()) {
                        // 处理单目运算符
                        if (operatorStack.isEmpty()) {
                            operatorStack.push(ch + POSTFIX_UNARY_OPERATOR);
                            break;
                        }
                        String peek = operatorStack.peek();
                        if (Objects.nonNull(OperatorType.getByMark(peek))) {
                            log.error("不能出现连续的数学运算符: '{}'与'{}'", peek, ch);
                            throw new BusinessException("不能出现连续的数学运算符", peek, ch);
                        }
                        if (!PARAM_PLACEHOLDER.equals(peek)) {
                            operatorStack.push(ch + POSTFIX_UNARY_OPERATOR);
                            break;
                        }
                    }
                    // 弹出优先级较低的符号
                    while (!operatorStack.empty()) {
                        String operator = trimBlank(operatorStack.peek());
                        if (PARAM_PLACEHOLDER.equals(operator)) {
                            operatorStack.pop();
                            continue;
                        }
                        if (operator.startsWith(suffix)) {
                            break;
                        }
                        if (operatorType.getOrder() > OperatorType.getOrder(operator)) {
                            break;
                        }
                        operatorStack.pop();
                        saveOperator(postfixExpression, operator, 2);
                    }
                    operatorStack.push(String.valueOf(ch));
                    break;
                }
            }
        }

        // 保存剩余参数
        saveParam(builder, operatorStack, postfixExpression, false);
        // 保存剩余的数学运算符
        while (!operatorStack.empty()) {
            saveOperator(postfixExpression, operatorStack.pop(), 2);
        }
        return postfixExpression;
    }

    /**
     * 计算后缀表达式
     *
     * @param postfixExpression  后缀表达式
     * @param paramGetter        参数获取
     * @param functionCalculator 函数计算
     * @return 计算结果
     */
    public static Object calculate(List<ExpressionParam> postfixExpression, Function<String, ?> paramGetter, BiFunction<String, Object[], Object> functionCalculator) {
        if (ObjectUtils.isEmpty(postfixExpression)) {
            log.info("表达式为空");
            return null;
        }
        // 存放临时数据
        Stack<Object> calculateStack = new Stack<>();
        for (ExpressionParam param : postfixExpression) {
            if (!param.isOperator()) {
                // 参数入栈
                Object value = param.getValue();
                JvsParamType type = param.getValueType();
                if (JvsParamType.any.equals(type)) {
                    // 自定义变量
                    value = paramGetter.apply((String) value);
                }
                if (value instanceof String) {
                    //直接获取变量值，避免使用\n 换行
                    if (postfixExpression.size() == 1) {
                        return value;
                    }
//                    value = ((String) value).replaceAll("\n", "");
                }
                calculateStack.push(value);
                continue;
            }
            int size = calculateStack.size();
            int paramCount = param.getParamCount();
            String functionName = param.getFunctionName();
            if (size < paramCount) {
                throw new BusinessException("[{}]参数数量异常, 期望的参数数量: {}, 实际数量: {}", functionName, paramCount, size);
            }
            Object[] params = new Object[paramCount];
            for (int i = 0; i < paramCount; i++) {
                // 参数出栈
                params[paramCount - i - 1] = calculateStack.pop();
            }
            Object result;
            if (OperatorType.ARR.name().equals(functionName)) {
                // 数组类型的参数
                result = new ArrayList<>(Arrays.asList(params));
            } else {
                // 计算函数
                result = functionCalculator.apply(functionName, params);
            }
            // 结果入栈
            calculateStack.push(result);
        }
        return calculateStack.pop();
    }

    /**
     * 获取表达式中的自定义参数
     *
     * @param expression 原表达式
     * @return 参数集合
     */
    public static List<String> getCustomParams(String expression) {
        return getCustomParams(parsePostfixExpression(expression), null);
    }

    /**
     * 获取表达式中的自定义参数
     *
     * @param expression 原表达式
     * @param prefix     参数前缀
     * @return 参数集合
     */
    public static List<String> getCustomParams(String expression, String prefix) {
        return getCustomParams(parsePostfixExpression(expression), prefix);
    }

    /**
     * 获取表达式中的自定义参数
     *
     * @param postfixExpression 后缀表达式
     * @return 参数集合
     */
    public static List<String> getCustomParams(List<ExpressionParam> postfixExpression, String prefix) {
        if (ObjectUtils.isEmpty(postfixExpression)) {
            return Collections.emptyList();
        }
        List<String> params = postfixExpression.stream()
                .filter(e -> !e.isOperator())
                .filter(e -> JvsParamType.any.equals(e.getValueType()))
                .map(ExpressionParam::getValue)
                .map(String::valueOf)
                .distinct()
                .collect(Collectors.toList());
        if (StringUtils.isNotBlank(prefix)) {
            params = params.stream()
                    .filter(param -> param.startsWith(prefix))
                    .map(param -> param.substring(prefix.length()))
                    .collect(Collectors.toList());
        }
        return params;
    }

    /**
     * 参数类型转换
     *
     * @param param 表达式中的参数(均为字符串)
     * @param isStr 是否为双引号包裹的字符串
     * @return 可用于函数计算的参数
     */
    private static Object getValue(String param, boolean isStr) {
        if (Objects.isNull(param)) {
            return null;
        }
        // 字符串, 日期
        if (isStr) {
            //999999 会被转为日期
            return tryParseDate(param);
        }
        // null
        String aNull = "null";
        if (StringUtils.equalsIgnoreCase(aNull, param)) {
            return null;
        }
        // true
        if (Boolean.TRUE.toString().equalsIgnoreCase(param)) {
            return Boolean.TRUE;
        }
        // false
        if (Boolean.FALSE.toString().equalsIgnoreCase(param)) {
            return Boolean.FALSE;
        }
        // Number
        if (NumberUtil.isNumber(param)) {
            return tryParseNumber(param);
        }
        // 自定义变量, 与前端约定的格式: ${变量标识}
        if (param.startsWith(PREFIX_PARAM) && param.endsWith(POSTFIX_PARAM)) {
            return param.substring(PREFIX_PARAM.length(), param.length() - POSTFIX_PARAM.length());
        }
        if (param.startsWith(PREFIX2_PARAM) && param.endsWith(PREFIX2_PARAM)) {
            return param.substring(PREFIX2_PARAM.length(), param.length() - PREFIX2_PARAM.length());
        }
        // 未知参数
        throw new BusinessException("未知参数", param);
    }

    /**
     * 尝试转换为日期, 转换失败时原样返回
     *
     * @param param 参数
     * @return 转换后的参数
     */
    private static Object tryParseDate(String param) {
        try {
            Date jdkDate = DateUtil.parse(param).toJdkDate();
            if (jdkDate.getYear() >= 1980 && jdkDate.getYear() > 0 && jdkDate.getYear() <= 2100) {
                return jdkDate;
            } else {
                return param;
            }
        } catch (Exception e) {
            return param;
        }
    }

    /**
     * 尝试转换为数字, 转换失败时返回null
     *
     * @param param 参数
     * @return 转换后的参数
     */
    private static Object tryParseNumber(String param) {
        if (NumberUtil.isInteger(param)) {
            return NumberUtil.parseInt(param);
        }
        if (NumberUtil.isLong(param)) {
            return NumberUtil.parseLong(param);
        }
        if (NumberUtil.isDouble(param)) {
            return NumberUtil.parseDouble(param);
        }
        try {
            return NumberUtil.parseNumber(param);
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * 在符号栈中添加参数占位符
     * <p>
     * 处理单目运算符
     *
     * @param operatorStack     符号栈
     * @param postfixExpression 后缀表达式的暂存集合
     */
    private static void addParamPlaceholder(Stack<String> operatorStack, List<ExpressionParam> postfixExpression) {
        if (!operatorStack.empty()) {
            String operator = operatorStack.peek();
            if (operator.endsWith(POSTFIX_UNARY_OPERATOR)) {
                operatorStack.pop();
                saveOperator(postfixExpression, operator.substring(0, operator.length() - POSTFIX_UNARY_OPERATOR.length()), 1);
            }
            if (PARAM_PLACEHOLDER.equals(operator)) {
                throw new BusinessException("表达式结构异常参数之间需要有运算符");
            }
        }
        operatorStack.push(PARAM_PLACEHOLDER);
    }

    /**
     * 保存表达式中的运算符
     *
     * @param postfixExpression 后缀表达式的暂存集合
     * @param operator          运算符名称
     * @param paramCount        运算符参数数量
     */
    private static void saveOperator(List<ExpressionParam> postfixExpression, String operator, int paramCount) {
        if (PARAM_SPLIT.equals(operator) || PARAM_PLACEHOLDER.equals(operator)) {
            // 逗号只用做区分参数边界, 问号只记录参数数量
            return;
        }
        ExpressionParam param = new ExpressionParam();
        if (operator.endsWith(POSTFIX_UNARY_OPERATOR)) {
            operator = operator.substring(0, operator.length() - POSTFIX_UNARY_OPERATOR.length());
            paramCount = 1;
        }
        OperatorType operatorType = OperatorType.getByMark(operator);
        if (Objects.isNull(operatorType)) {
            param.setFunctionName(operator);
        } else {
            param.setFunctionName(operatorType.name());
        }
        param.setValue(operator);
        param.setOperator(true);
        param.setParamCount(paramCount);
        postfixExpression.add(param);
    }

    /**
     * 保存表达式中的参数
     *
     * @param builder           参数暂存器
     * @param operatorStack     运算符栈
     * @param postfixExpression 后缀表达式的暂存集合
     * @param isStr             是否为双引号包裹的字符串
     */
    private static void saveParam(StringBuilder builder, Stack<String> operatorStack, List<ExpressionParam> postfixExpression, boolean isStr) {
        String param = builder.toString();
        if (!isStr) {
            param = trimBlank(param);
        } else {
            param = param.replace("\u0000", "");
            //兼容新版本公式
            param = param.replace("&nbsp;", "");
        }
        if (StringUtils.isBlank(param) && !isStr) {
            return;
        }
        builder.delete(0, builder.length());
        // 解析参数值
        Object value = getValue(param, isStr);
        JvsParamType valueType;
        if (Objects.isNull(value)) {
            valueType = JvsParamType.none;
        } else {
            valueType = JvsParamType.getByClass(value.getClass());
        }
        if (value instanceof String && !isStr) {
            valueType = JvsParamType.any;
        }
        ExpressionParam functionParam = new ExpressionParam();
        functionParam.setOperator(false);
        functionParam.setValue(value);
        functionParam.setValueType(valueType);
        postfixExpression.add(functionParam);
        addParamPlaceholder(operatorStack, postfixExpression);
    }

    /**
     * 剔除字符串首尾的空格字符
     * <p>
     * ASCII码的160为特殊空格
     *
     * @param str 被处理的字符串
     * @return 处理之后的字符串
     */
    public static String trimBlank(String str) {
        if (str == null) {
            return "";
        }
        // 处理转义符
        str = str.replace("\u0000", "");
        //兼容新版本公式
        str = str.replace("&nbsp;", "");
        int len = str.length();
        int st = 0;
        // 处理特殊空格
        while ((st < len)) {
            char ch = str.charAt(st);
            if (!(ch <= ' ' || ch == 160)) {
                break;
            }
            st++;
        }
        while ((st < len)) {
            char ch = str.charAt(len - 1);
            if (!(ch <= ' ' || ch == 160)) {
                break;
            }
            len--;
        }
        return ((st > 0) || (len < str.length())) ? str.substring(st, len) : str;
    }

    /**
     * 提出字符串的花括号标识
     * <p>
     * 例: ${name} -> name
     *
     * @param str 被处理的字符串
     * @return 处理之后的字符串
     */
    public static String trimCurlyBracket(String str) {
        if (str == null) {
            return "";
        }
        if (str.startsWith(PREFIX_PARAM) && str.endsWith(POSTFIX_PARAM)) {
            str = str.substring(PREFIX_PARAM.length(), str.length() - POSTFIX_PARAM.length());
        }
        if (str.startsWith(PREFIX2_PARAM) && str.endsWith(PREFIX2_PARAM)) {
            str = str.substring(PREFIX2_PARAM.length(), str.length() - PREFIX2_PARAM.length());
        }
        return str;
    }

    /**
     * 数学运算符类型
     */
    @Getter
    @AllArgsConstructor
    public enum OperatorType {

        /**
         * 符号类型
         */
        ADD("+", 1, true),
        SUB("-", 1, true),
        MULTIPLY("*", 2, false),
        DIVIDE("/", 2, false),
        ARR("[", 3, false);

        @ApiModelProperty("符号")
        private final String mark;
        @ApiModelProperty("优先级(数值越大优先级越高)")
        private final int order;
        @ApiModelProperty("是否可以当作单目运算符")
        private final boolean enableUnaryOperator;

        public static int getOrder(String mark) {
            OperatorType operatorType = getByMark(mark);
            if (operatorType == null) {
                return 0;
            }
            return operatorType.getOrder();
        }

        public static OperatorType getByMark(String mark) {
            if (mark == null) {
                return null;
            }
            if (mark.endsWith(POSTFIX_UNARY_OPERATOR)) {
                mark = mark.substring(0, mark.length() - POSTFIX_UNARY_OPERATOR.length());
            }
            for (OperatorType value : values()) {
                if (value.getMark().equals(mark)) {
                    return value;
                }
            }
            return null;
        }

    }

}
