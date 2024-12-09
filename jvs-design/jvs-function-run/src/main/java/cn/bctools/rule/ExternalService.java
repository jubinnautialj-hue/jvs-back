package cn.bctools.rule;


import java.util.Map;
import java.util.function.Function;

/**
 * The interface External service.
 * 逻辑的扩展节点基础服务
 *
 * @author wl
 */
public interface ExternalService {
    /**
     * 执行扩展方法
     *
     * @param functionName           节点方法名称
     * @param group                  the group
     * @param variableMap            变量对象
     * @param expressionExecFunction 扩展信息字段
     * @return 返回执行对象信息 object
     */
    Object execute(String functionName, String group, Map<String, Object> variableMap, Function<String, Object> expressionExecFunction);

    /**
     * 检查是否存在三方扩展,是否名称重名
     *
     * @param functionName 方法名
     * @param group        分级名
     */
    void checkName(String functionName, String group);

}
