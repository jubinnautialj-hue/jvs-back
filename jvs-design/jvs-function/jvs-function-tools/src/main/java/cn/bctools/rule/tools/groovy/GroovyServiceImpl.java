package cn.bctools.rule.tools.groovy;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import groovy.lang.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


/**
 * @author st
 */
@Slf4j
@AllArgsConstructor
@Rule(value = "Groovy工具",
        group = RuleGroup.工具插件,
        test = true,
        returnType = ClassType.未识别,
        testShowEnum = TestShowEnum.JSON,
        order = 22,
        explain = "动态执行Groovy脚本。"
)
public class GroovyServiceImpl implements BaseCustomFunctionInterface<GroovyDto> {

    private final TimedCache<String,Script> scriptCache = CacheUtil.newTimedCache(1000 * 60 * 5);

    @Override
    public Object execute(GroovyDto groovyDto, Map<String, Object> params) {
        Object run = null;
        try {
            String functionName = Optional.ofNullable(ReUtil.findAllGroup1("def(.*?)\\(", groovyDto.getContent()))
                    .map(CollectionUtil::getFirst)
                    .map(StrUtil::trim).orElse("groovy");

            List<Object> parameters = groovyDto.getParameters();
            Script script = getScript(functionName, parameters.size(), groovyDto.getContent());
            if(CollectionUtil.isNotEmpty(parameters)){

                List<Boolean> parameterTypes;
                if(MapUtil.isNotEmpty(groovyDto.getParameterTypes())){
                    parameterTypes = parseContent(groovyDto.getContent(), groovyDto.getParameterTypes());
                }else{
                    //系统自己判断是否为数组
                    parameterTypes = getArgTypes(script,groovyDto.getContent(),functionName);
                }

                Object[] objects = buildParamArr(groovyDto.getParameters(), parameterTypes);
                Binding binding = buildBinding(objects);
                script.setBinding(binding);
                run = script.run();
            }else{
                run = script.run();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return run;
    }

    private Script getScript(String functionName,int parameterSize,String functionContent){
        String scriptCacheKey = functionName+"@"+parameterSize;
        Script groovy;
        if(scriptCache.containsKey(scriptCacheKey)){
            groovy = scriptCache.get(scriptCacheKey);
        }else{
            groovy = buildScript(functionName, functionContent);
        }
        return groovy;
    }

    public Script buildScript(String functionName, String functionBody) {

        StringBuilder builder = new StringBuilder(functionBody);
        builder.append("\nreturn ");
        builder.append(functionName);
        builder.append('(');
        AtomicInteger index = new AtomicInteger();
        List<String> argNames = getArgNames(functionBody);
        String collect = argNames.stream().map(e -> "arg" + index.getAndIncrement()).collect(Collectors.joining(StringPool.COMMA));
        builder.append(collect);
        builder.append(')');

        GroovyShell shell = new GroovyShell();
        return shell.parse(builder.toString());
    }

    private Object[] buildParamArr(List<Object> parameters,List<Boolean> isArrList){
        List<Object> newParams = new ArrayList<>();
        if(CollectionUtil.isEmpty(parameters) || CollectionUtil.isEmpty(isArrList)){
            return newParams.toArray();
        }
        int j = 0;
        for (int i = 0; i < parameters.size(); i++) {
            Object o = parameters.get(i);
            if(j < isArrList.size() && isArrList.get(j) &&  !(o instanceof List)){
                int subEnd = parameters.size();
                int surplus = isArrList.size()-j;
                if(surplus>1){
                    subEnd = parameters.size()-surplus;
                }
                List<Object> list = parameters.subList(i, subEnd);
                o = list.toArray();
                i = subEnd;
            }
            j++;
            newParams.add(o);
        }
        return newParams.toArray();
    }

    private  Binding buildBinding(Object... params) {
        Binding binding = new Binding();

        for (int i1 = 0; i1 < params.length; i1++) {
            binding.setProperty("arg" + i1, params[i1]);
        }

        return binding;
    }

    // 判断方法是否有动态参数
    public List<Boolean> getArgTypes(GroovyObject instance,String content, String methodName) {
        List<String> argNames = getArgNames(content);
        MetaClass metaClass = instance.getMetaClass();
        List<MetaMethod> method = metaClass.getMethods();
        MetaMethod metaMethod = method.stream().filter(e -> methodName.equals(e.getName())).findFirst().orElse(null);
        List<Boolean> arrayList = new ArrayList<>();
        if (metaMethod != null) {
            // 检查参数类型是否是数组
            Class<?>[] parameterTypes = metaMethod.getNativeParameterTypes();
            for (int i1 = 0; i1 < parameterTypes.length; i1++) {
                Class<?> paramType = parameterTypes[i1];
                if(argNames.get(i1).startsWith("...")){
                    arrayList.add(Boolean.TRUE);
                }else{
                    arrayList.add(paramType.isArray());
                }
            }
        }
        return arrayList;
    }

    private List<Boolean> parseContent(String groovyScript,Map<String,Boolean> presetMap){
        List<String> split = getArgNames(groovyScript);

        return split.stream()
                .map(e -> {
                    if(e.startsWith("...")){
                        return false;
                    }
                    return presetMap.getOrDefault(e, Boolean.FALSE);
                })
                .collect(Collectors.toList());
    }

    private static List<String> getArgNames(String groovyScript) {
        int start = groovyScript.indexOf("def");
        int i1 = groovyScript.indexOf( StringPool.LEFT_BRACKET, start);
        int i2 = groovyScript.indexOf( StringPool.RIGHT_BRACKET, i1);
        String substring = groovyScript.substring(i1+1, i2);

        List<String> split = StrUtil.split(substring, StringPool.COMMA);
        return split;
    }

}
