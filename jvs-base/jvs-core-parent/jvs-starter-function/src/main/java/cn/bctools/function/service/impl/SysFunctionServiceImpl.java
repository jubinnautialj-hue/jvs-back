package cn.bctools.function.service.impl;


import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.function.entity.po.BaseFunctionPo;
import cn.bctools.function.entity.vo.FunctionBusinessTestVo;
import cn.bctools.function.entity.vo.Parameter;
import cn.bctools.function.enums.JvsParamType;
import cn.bctools.function.handler.impl.BaseFunctionImpl;
import cn.bctools.function.mapper.SysFunctionMapper;
import cn.bctools.function.service.SysFunctionService;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import groovy.lang.*;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jvs  The type Sys function service.
 */
@Service
public class SysFunctionServiceImpl extends ServiceImpl<SysFunctionMapper, BaseFunctionPo> implements SysFunctionService {

    @Override
    public void insertBaseFunction(String functionName, String shortName, String info, String type, String body, JvsParamType jvsParamType, List<Parameter> parameters, Boolean dynamicParam) {
        //检查函数名是否重复test

        BaseFunctionPo entity = Optional.ofNullable(getOne(Wrappers.query(new BaseFunctionPo().setName(functionName)))).orElse(new BaseFunctionPo());
        //替换原有函数名
        String regex = functionName +"\\((.*?)\\)";
        List<String> params = ReUtil.findAllGroup1(regex, body);
//        boolean contains = body.contains(functionName + "(...");
        if (ObjectNull.isNull(dynamicParam)) {
            dynamicParam = false;
        }
        boolean contains = params.stream().anyMatch(e -> StrUtil.split(e, StringPool.COMMA).stream().anyMatch(v -> v.contains("...")));
        if (dynamicParam && !contains) {
            throw new BusinessException("不是动态参数,请修改参数体");
        }
        String functionBody = body.replaceAll("groovy\\(", functionName + "(");
        Script groovy = BaseFunctionImpl.buildScript(functionName, functionBody, contains, parameters.size(), parameters.size());
        List<Object> collect = parameters.stream().map(e -> {
            switch (e.getType()) {
                case any:
                    //判断是否任意类型，判断对象和数据
                    if (JSONUtil.isTypeJSONObject(e.getValue().toString())) {
                        if (JSONUtil.isTypeJSONArray(e.getValue().toString())) {
                            return JSONArray.parseArray(e.getValue().toString());
                        } else if (JSONUtil.isTypeJSONObject(e.getValue().toString())) {
                            return JSONObject.parseObject(e.getValue().toString());
                        }
                    }
                    return e.getValue();
                case array:
                    return JSONArray.parseArray(e.getValue().toString());
                case object:
                    return JSONObject.parseObject(e.getValue().toString());
                case bool:
                    return Boolean.valueOf(e.getValue().toString());
                case number:
                    if (NumberUtil.isNumber(e.toString())) {
                        return NumberUtil.parseNumber(e.toString());
                    }
                default:
                    return e.getValue();
            }
        }).collect(Collectors.toList());
        Binding binding = BaseFunctionImpl.buildBinding(true, collect.size(), collect.toArray(new Object[collect.size()]));
        groovy.setBinding(binding);
        Object run = groovy.run();
        JvsParamType byClass = JvsParamType.getByClass(run.getClass());
        if (!jvsParamType.equals(byClass)) {
            throw new BusinessException("返回类型不一致,值为:" + run + ",类型为:" + byClass.getAClass().getSimpleName());
        }
        List inParamTypes = new ArrayList();
        if (ObjectNull.isNotNull(parameters)) {
            inParamTypes = parameters.stream().map(Parameter::getType).collect(Collectors.toList());
        }
        HashSet<Integer> paramCount = new HashSet<>();
        if (!dynamicParam) {
            paramCount.add(inParamTypes.size());
        }
        entity.setInfo(info)
                .setType(type)
                .setBody(functionBody)
                .setInParamTypes(inParamTypes)
                .setJvsParamType(jvsParamType)
                .setEnableCache(true)
                .setParamCount(paramCount)
                .setParameters(parameters)
                .setDynamicParam(dynamicParam)
                .setShortName(shortName)
                .setName(functionName);
        if (ObjectNull.isNotNull(entity.getId())) {
            updateById(entity);
        } else {
            save(entity);
        }
    }

    /**
     * 用于测试要新增的函数是否正确
     *
     * @param body         函数体 示例： def groovy(... x){   return x[0]+x[1]; };
     * @param parameters   所有参数
     * @param jvsParamType 返回类型，返回时会强制校验类型是否正确
     * @return
     */
    @Override
    public Object testBaseFunction(String functionName, String body, List<Parameter> parameters, JvsParamType jvsParamType) {
        Script groovy;
        try {
            functionName = Optional.ofNullable(functionName).orElse("groovy");
            groovy = BaseFunctionImpl.buildScript(functionName, body, true, parameters.size(), parameters.size());
        } catch (Exception e) {
            throw new BusinessException("函数不正确");
        }
        List<Object> collect = parameters.stream().map(e -> {
            switch (e.getType()) {
                case any:
                    //判断是否任意类型，判断对象和数据
                    if (JSONUtil.isTypeJSONObject(e.getValue().toString())) {
                        if (JSONUtil.isTypeJSONArray(e.getValue().toString())) {
                            return JSONArray.parseArray(e.getValue().toString());
                        } else if (JSONUtil.isTypeJSONObject(e.getValue().toString())) {
                            return JSONObject.parseObject(e.getValue().toString());
                        }
                    }
                    return e.getValue();
                case array:
                    return JSONArray.parseArray(e.getValue().toString());
                case object:
                    return JSONObject.parseObject(e.getValue().toString());
                case bool:
                    return Boolean.valueOf(e.getValue().toString());
                case number:
                    if (NumberUtil.isNumber(e.toString())) {
                        return NumberUtil.parseNumber(e.toString());
                    }
                default:
                    return e.getValue();
            }
        }).collect(Collectors.toList());
        Binding binding = BaseFunctionImpl.buildBinding(true, collect.size(), collect.toArray(new Object[collect.size()]));
        groovy.setBinding(binding);
        //校验参数名称
        Method[] methods = groovy.getClass().getMethods();
        String finalFunctionName = functionName;
        Method method = Arrays.stream(methods).filter(e -> StrUtil.equals(finalFunctionName, e.getName())).findFirst().orElseThrow(() -> new BusinessException("函数名称与函数体中名称不一致"));
        //校验参数个数
        if (!method.isVarArgs()) {
            //如果不是动态参数 则判断参数个数
            int parameterCount = method.getParameterCount();
            if (parameterCount > 0 && CollectionUtil.size(parameters) != parameterCount) {
                throw new BusinessException("函数参数列表 与 函数体 中参数个数不一致");
            }
        }

        Object run = groovy.run();

        if (run == null) {
            return null;
        }

        JvsParamType byClass = JvsParamType.getByClass(run.getClass());
        //如果未设置函数返回类型 或 函数返回类型与设置的匹配 则直接返回 值
        if (jvsParamType == null || jvsParamType.equals(byClass)) {
            return run;
        }
        throw new BusinessException("返回类型不一致,值为:" + run + ",函数返回类型为:" + byClass.getDesc());
    }


    public static void main(String[] args) {
        StringBuilder builder = new StringBuilder("def TXT_LEFT() {\n" +
                "  return x.take(y)\n" +
                "}");
        builder.append("\nreturn ");
        builder.append("TXT_LEFT");
        builder.append('(');
        // 拼接固定参数
        for (int i = 0; i < 1; i++) {
            builder.append("x").append(i).append(',');
        }

        // 删除分隔符
        if (',' == builder.charAt(builder.length() - 1)) {
            builder.deleteCharAt(builder.length() - 1);
        }
        builder.append(')');
        // Groovy
        GroovyShell shell = new GroovyShell();
        // 解析函数脚本
        Script parse = shell.parse(builder.toString());

        Method[] methods = parse.getClass().getMethods();
        Method method1 = Arrays.stream(methods).filter(e -> "TXT_LEFT".equals(e.getName())).findFirst().orElse(null);
        int parameterCount = method1.getParameterCount();
        System.out.println("参数个数：" + parameterCount);
    }
}
