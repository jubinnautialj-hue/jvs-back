package cn.bctools.design.rule.service;

import cn.bctools.design.rule.entity.RuleExternalPo;
import cn.bctools.rule.ExternalService;
import cn.bctools.rule.dto.RuleFunctionDto;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * The interface Rule external service.
 * 逻辑引擎扩展接口服务
 *
 * @author guojing
 */
public interface RuleExternalService extends IService<RuleExternalPo>, ExternalService {
    /**
     * 执行扩展方法
     *
     * @param functionName           节点或方法名
     * @param variableMap            参数变量对象
     * @param expressionExecFunction 获取变量时处理时对变量对变量进行公式处理
     * @return 返回节点执行的结果集
     */
    @Override
    Object execute(String functionName, String ruleGroup, Map<String, Object> variableMap, Function<String, Object> expressionExecFunction);

    /**
     * web service 执行
     *
     * @param externalPo             the external po  逻辑的扩展对象
     * @param variableMap            the variable map  变量对象
     * @param expressionExecFunction the expression exec function 获取变量时处理时对变量对变量进行公式处理
     * @param test                   the test  是否是测试，如果是测试执行时，会直接调用真实接口服务
     * @return object object    返回只支持 json
     */
    Object executeWebService(RuleExternalPo externalPo, Map<String, Object> variableMap, Function<String, Object> expressionExecFunction, boolean test);

    /**
     * Execute object.
     *
     * @param externalPo             the external po 逻辑的扩展对象
     * @param variableMap            the variable map 变量对象
     * @param expressionExecFunction the expression exec function 获取变量时处理时对变量对变量进行公式处理
     * @param test                   the test 是否是测试，如果是测试执行时，会直接调用真实接口服务
     * @return object object
     */
    Object execute(RuleExternalPo externalPo, Map<String, Object> variableMap, Function<String, Object> expressionExecFunction, Boolean test);

    /**
     * 注册服务
     * <p>
     * 用于微服务中注册逻辑节点，此方式存在服务上下线问题，后续会借助 nacos的服务进行处理节点和功能，并加上节点操作。
     * 主要用于${@linkplain cn.bctools.rule.annotations.Rule} 注解处理注册节点信息在独立的项目中添加，而不需要对当前逻辑服务进行调整
     *
     * @param obj             the obj  注册的节点或方法信息
     * @param applicationName the application name  注册的微服务名称
     */
    void register(List<RuleFunctionDto> obj, String applicationName);

}
