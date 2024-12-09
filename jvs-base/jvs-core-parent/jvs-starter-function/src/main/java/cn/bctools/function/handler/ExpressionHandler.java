package cn.bctools.function.handler;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.function.entity.vo.ElementVo;
import cn.bctools.function.utils.ExpressionParam;
import cn.bctools.function.utils.ExpressionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.OrderUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 表达式统一处理类
 *
 * @Author: GuoZi
 */
@Slf4j
@Component
public class ExpressionHandler {

    public static final String USE_CASE_BLANK = "";
    public static final Pattern patten = Pattern.compile("[\\u4e00-\\u9fa5]");

    /**
     * 参数映射
     * <p>
     * <useCase, List<IJvsParam>>
     */
    private Map<String, List<IJvsParam<? extends ElementVo>>> paramMap;

    /**
     * 函数映射
     * <p>
     * <useCase, List<IJvsFunction>>
     */
    private Map<String, List<IJvsFunction<? extends ElementVo>>> functionMap;

    /**
     * 参数
     */
    private List<IJvsParam<? extends ElementVo>> params;
    /**
     * 函数
     */
    private List<IJvsFunction<? extends ElementVo>> functions;

    public ExpressionHandler(List<IJvsParam<? extends ElementVo>> params, List<IJvsFunction<? extends ElementVo>> functions) {
        this.init(params, functions);
    }

    /**
     * 初始化操作
     * <p>
     * 排除无效的实现类
     */
    private void init(List<IJvsParam<? extends ElementVo>> params, List<IJvsFunction<? extends ElementVo>> functions) {
        if (ObjectUtils.isEmpty(this.params)) {
            this.params = Collections.emptyList();
            this.paramMap = Collections.emptyMap();
        }
        if (ObjectUtils.isEmpty(this.functions)) {
            this.functions = Collections.emptyList();
            this.functionMap = Collections.emptyMap();
        }
        params.removeIf(this::isNotValid);
        functions.removeIf(this::isNotValid);
        // 按Order排序
        params.sort(Comparator.comparingInt(e -> OrderUtils.getOrder(e.getClass(), Ordered.LOWEST_PRECEDENCE)));
        functions.sort(Comparator.comparingInt(e -> OrderUtils.getOrder(e.getClass(), Ordered.LOWEST_PRECEDENCE)));
        this.params = params;
        this.functions = functions;
        this.paramMap = this.parseMap(params);
        this.functionMap = this.parseMap(functions);
    }

    /**
     * 获取所有参数,函数列表(带缓存)
     *
     * @param useCase 使用场景
     * @return 参数, 函数列表
     */
    public Map<String, Object> getAllExpressionElement(String useCase) {
        // 获取各分组下的函数
        Map<String, Object> result = new LinkedHashMap<>(4);
        result.put(IJvsParam.NAME, this.getElementMap(useCase, params, ""));
        result.put(IJvsFunction.NAME, this.getElementMap(useCase, functions, ""));
        return result;
    }

    /**
     * 获取所有参数列表(带缓存)
     *
     * @param useCase        使用场景
     * @param excludeUseCase
     * @return 参数列表
     */
    public Map<String, Map<String, List<ElementVo>>> getAllParamElement(String useCase, String excludeUseCase) {
        // 获取各分组下的参数
        Map<String, Map<String, List<ElementVo>>> result = new LinkedHashMap<>(4);
        result.put(IJvsParam.NAME, this.getElementMap(useCase, params, excludeUseCase));
        return result;
    }

    /**
     * 计算表达式
     *
     * @param expression 表达式
     * @param data       参数
     * @param useCase    表达式使用场景
     * @return 计算结果
     */
    public Object calculate(String expression, Map<String, Object> data, String useCase) {
        return this.calculate(ExpressionUtils.parsePostfixExpression(expression), data, useCase);
    }

    /**
     * 计算表达式
     *
     * @param postfixExpression 前缀表达式
     * @param data              参数
     * @param useCase           表达式使用场景
     * @return 计算结果
     */
    public Object calculate(List<ExpressionParam> postfixExpression, final Map<String, Object> data, final String useCase) {
        return ExpressionUtils.calculate(
                postfixExpression,
                paramName -> this.getParamValue(useCase, paramName, data),
                (functionName, params) -> this.getFunctionValue(useCase, functionName, params)
        );
    }

    /**
     * 校验使用场景是否支持
     *
     * @param useCase 使用场景
     */
    public void checkUseCase(String useCase) {
        if (StringUtils.isBlank(useCase)) {
            throw new BusinessException("表达式使用场景不能为空");
        }
        if (paramMap.containsKey(useCase) || functionMap.containsKey(useCase)) {
            return;
        }
        throw new BusinessException("不支持的使用场景", useCase);
    }

    /**
     * 根据使用场景建立映射关系
     *
     * @param params 表达式元素集合
     * @param <T>    元素处理对象
     * @return map对象
     */
    private <T extends IJvsExpressionElement<? extends ElementVo>> Map<String, List<T>> parseMap(List<T> params) {
        Map<String, List<T>> paramMap = new HashMap<>(8);
        for (T param : params) {
            JvsExpression annotation = this.getAnnotation(param);
            String[] useCases = annotation.useCase();
            if (ObjectUtils.isEmpty(useCases)) {
                paramMap.computeIfAbsent(USE_CASE_BLANK, k -> new ArrayList<>()).add(param);
                continue;
            }
            for (String useCase : useCases) {
                if (StringUtils.isBlank(useCase)) {
                    useCase = USE_CASE_BLANK;
                }
                paramMap.computeIfAbsent(useCase, k -> new ArrayList<>()).add(param);
            }
        }
        return paramMap;
    }

    /**
     * 获取参数值
     * <p>
     * 1. 按前缀逐一匹配自定义参数
     * 2. 从环境参数中获取值
     * 3. 抛出异常
     *
     * @param paramName 参数名
     * @param data      当前环境参数
     * @return 参数值
     */
    private Object getParamValue(String useCase, String paramName, Map<String, Object> data) {
//        if (patten.matcher(paramName).find()) {
//            throw new BusinessException("不支持中文执行,参数异常：" + paramName);
//        }
        //添加临时测试函数使用
        if (ObjectNull.isNull(useCase) && data.containsKey(paramName)) {
            return data.get(paramName);
        }
        IJvsParam<? extends ElementVo> param = this.getJvsElement(useCase, paramName, this.params);
        if (Objects.isNull(param)) {
            throw new BusinessException("未知的参数: {}", paramName);
        }
        JvsExpression annotation = this.getAnnotation(param);
        try {
            return param.get(paramName.substring(annotation.prefix().length()), data);
        } catch (Exception e) {
            log.error(String.format("参数[%s]获取异常, 异常信息: %s", paramName, e.getMessage()), -1, e);
            throw new BusinessException(String.format("参数[%s]获取异常, 异常信息: %s", paramName, e.getMessage()), -1, e);
        }
    }

    /**
     * 计算函数值
     *
     * @param functionName 函数标识
     * @param data         参数
     * @return 计算结果
     */
    private Object getFunctionValue(String useCase, String functionName, Object... data) {
        functionName = ExpressionUtils.trimCurlyBracket(functionName);
        IJvsFunction<? extends ElementVo> function = this.getJvsElement(useCase, functionName, this.functions);
        if (Objects.isNull(function)) {
            throw new BusinessException("未知的函数", functionName);
        }
        JvsExpression annotation = this.getAnnotation(function);
        try {
            return function.calculate(functionName.substring(annotation.prefix().length()), data);
        } catch (Exception e) {
            throw new BusinessException(String.format("函数[%s]计算异常, 异常信息: %s", functionName, e.getMessage()), -1, e);
        }
    }

    /**
     * 根据表达式元素名获取对应的处理类
     *
     * @param elementName 元素名
     * @return 参数或函数处理类
     */
    private <T extends IJvsExpressionElement<? extends ElementVo>> T getJvsElement(String useCase, String elementName, List<T> params) {
        if (StringUtils.isBlank(elementName) || ObjectUtils.isEmpty(params)) {
            return null;
        }
        List<T> withPrefix = new ArrayList<>(params.size());
        List<T> withoutPrefix = new ArrayList<>(params.size());
        for (T param : params) {
            JvsExpression annotation = this.getAnnotation(param);
            // 校验使用场景
            boolean matchUseCase = this.matchUseCase(useCase, annotation.useCase());
            if (!matchUseCase) {
                continue;
            }
            // 校验前缀
            String prefix = annotation.prefix();
            if (StringUtils.isBlank(prefix)) {
                withoutPrefix.add(param);
                continue;
            }
            if (elementName.startsWith(prefix)) {
                withPrefix.add(param);
            }
        }
        List<T> result = withPrefix.isEmpty() ? withoutPrefix : withPrefix;
        if (result.isEmpty()) {
            log.error("未找到对应的处理类, 使用环境: {}, 元素名: {}", useCase, elementName);
            return null;
        }
        if (result.size() != 1) {
            log.error("匹配到多个处理类, 使用环境: {}, 元素名: {}", useCase, elementName);
            return null;
        }
        return result.get(0);
    }

    /**
     * 校验当前环境是否匹配
     *
     * @param currentUseCase 当前环境
     * @param validUseCases  可用环境
     * @return 匹配结果
     */
    private boolean matchUseCase(String currentUseCase, String[] validUseCases) {
        if (StringUtils.isBlank(currentUseCase)) {
            return true;
        }
        if (validUseCases == null || validUseCases.length == 0) {
            return true;
        }
        for (String validUseCase : validUseCases) {
            if (StringUtils.isBlank(validUseCase)) {
                return true;
            }
            if (currentUseCase.equals(validUseCase)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 对表达式元素列表进行处理
     * <p>
     * 1. 添加标识前缀
     * 2. 判断是否为参数
     * 3. 分类不为空时, 进行分组处理
     *
     * @param expectedUseCase 使用场景
     * @param baseFunctions   待处理的集合
     * @param excludeUseCase
     * @return 分组后的表达式元素集合
     */
    private Map<String, List<ElementVo>> getElementMap(String expectedUseCase, List<? extends IJvsExpressionElement<? extends ElementVo>> baseFunctions, String excludeUseCase) {
        if (ObjectUtils.isEmpty(baseFunctions)) {
            return Collections.emptyMap();
        }
        Map<String, List<ElementVo>> result = new LinkedHashMap<>(baseFunctions.size() << 5);
        for (IJvsExpressionElement<? extends ElementVo> param : baseFunctions) {
            JvsExpression annotation = this.getAnnotation(param);
            String prefix = annotation.prefix();
            String[] useCase = annotation.useCase();
            String groupName = annotation.groupName();

            // 匹配到前缀为SYS的，直接执行，不需要判断场景
            if ("SYS".equals(prefix)) {
                if (ObjectNull.isNotNull(excludeUseCase) && excludeUseCase.contains("SYS")) {
                    continue;
                }
                getAllElements(param, result, groupName, prefix);
                continue;
            }
            // 匹配到前缀为variable的，直接执行，不需要判断场景
            if ("variable".equals(prefix)) {
                if (ObjectNull.isNotNull(excludeUseCase) && excludeUseCase.contains("variable")) {
                    continue;
                }
                getAllElements(param, result, groupName, prefix);
                continue;
            }

            if (ObjectUtils.isNotEmpty(useCase)) {
                String[] split = expectedUseCase.split("\\.");
                for (String s : split) {
                    boolean match = this.matchUseCase(s, useCase);
                    if (match) {
                        //如果分组为模型分组，做用特殊处理获取此应用下所有的模型
                        getAllElements(param, result, groupName, prefix);
                    } else {
                        //不做处理
                    }
                }
            }

        }
        return result;
    }

    private void getAllElements(IJvsExpressionElement<? extends ElementVo> param, Map<String, List<ElementVo>> result, String groupName, String prefix) {
        List<? extends ElementVo> allFunctions = param.getAllElements();
        if (ObjectUtils.isEmpty(allFunctions)) {
            //不处理为空变量
            return;
        }
        boolean hasPrefix = StringUtils.isNotBlank(prefix);
        for (ElementVo vo : allFunctions) {
            String group = groupName;
            String type = vo.getType();
            if (StringUtils.isNotBlank(type)) {
                group = type;
            }
            ElementVo nvo = BeanCopyUtil.copy(vo, ElementVo.class);
            if (hasPrefix) {
                nvo.setId(prefix + nvo.getId());
                //递归下级节点
                childrenPrefix(nvo.getChildren(), prefix);
            }
            nvo.setParam(param instanceof IJvsParam);
            List<ElementVo> list = result.computeIfAbsent(group, v -> new ArrayList<>());
            //清理 path 路径
            list.forEach(e -> e.setPath(null));
            list.add(nvo);
        }
    }

    private void childrenPrefix(List<ElementVo> children, String prefix) {
        if (ObjectNull.isNotNull(children)) {
            for (ElementVo child : children) {
                child.setId(prefix + child.getId());
                child.setParam(true);
                childrenPrefix(child.getChildren(), prefix);
            }
        }
    }

    /**
     * 判断表达式元素是否可用
     *
     * @param element 表达式元素
     * @return 判断结果
     */
    private boolean isNotValid(IJvsExpressionElement<? extends ElementVo> element) {
        if (Objects.isNull(element)) {
            return true;
        }
        JvsExpression annotation = this.getAnnotation(element);
        if (Objects.isNull(annotation)) {
            return true;
        }
        return StringUtils.isBlank(annotation.groupName());
    }

    /**
     * 获取Jvs表达式的注解
     *
     * @param handler 实体类
     * @return 注解类
     */
    private JvsExpression getAnnotation(IJvsExpressionElement<? extends ElementVo> handler) {
        return AnnotationUtils.findAnnotation(handler.getClass(), JvsExpression.class);
    }

}
