package cn.bctools.rule.tools.groovy;

import cn.bctools.function.handler.impl.BaseFunctionImpl;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import groovy.lang.Binding;
import groovy.lang.Script;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;


/**
 * @author st
 */
@Slf4j
@AllArgsConstructor
@Rule(value = "Groovy工具",
        group = RuleGroup.工具插件,
        test = true,
        enable = false,
        returnType = ClassType.未识别,
        testShowEnum = TestShowEnum.JSON,
        order = 22,
//        iconUrl = "rule-youjian",
        explain = "动态执行Groovy脚本。"
)
public class GroovyServiceImpl implements BaseCustomFunctionInterface<GroovyDto> {

    private final Cache<GroovyDto, Object> groovyCache = CacheUtil.newTimedCache(1000 * 60 * 5);

    @Override
    public Object execute(GroovyDto groovyDto, Map<String, Object> params) {
        if (groovyCache.containsKey(groovyDto)) {
            return groovyCache.get(groovyDto);
        }
        int size = groovyDto.getParameters().size();
        String functionName = Optional.ofNullable(ReUtil.findAllGroup1("def(.*?)\\(", groovyDto.getContent()))
                .map(CollectionUtil::getFirst)
                .map(StrUtil::trim).orElse("groovy");
        Script groovy = BaseFunctionImpl.buildScript(functionName, groovyDto.getContent(), true, size, size);
        Binding binding = BaseFunctionImpl.buildBinding(true, size, groovyDto.getParameters().toArray(new Object[0]));
        groovy.setBinding(binding);
        Object run = groovy.run();
        groovyCache.put(groovyDto, run);
        return run;
    }
}
