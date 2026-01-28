package cn.bctools.function.handler.impl;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.function.entity.po.BaseFunctionPo;
import cn.bctools.function.entity.vo.ElementVo;
import cn.bctools.function.enums.JvsParamType;
import cn.bctools.function.handler.IJvsFunction;
import cn.bctools.function.handler.JvsExpression;
import cn.bctools.function.mapper.SysFunctionMapper;
import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 基础函数
 *
 * @author guojing
 */
@Slf4j
@Order(-1)
@JvsExpression(groupName = "基础函数")
public class BaseFunctionImpl implements IJvsFunction<ElementVo> {

    SysFunctionMapper sysFunctionMapper;

    public BaseFunctionImpl(SysFunctionMapper sysFunctionMapper) {
        this.sysFunctionMapper = sysFunctionMapper;
    }

    /**
     * 脚本解析缓存
     * <p>
     * 考虑到基础函数体的变动频率不高, 采用了较为简单粗暴的缓存方式
     */
    private final Cache<String, ScriptDto> scriptCache = CacheUtil.newTimedCache(1000 * 3 * 60);

    public void clean() {
        scriptCache.clear();
    }

    /**
     * 变量分隔符
     */
    public static final char CHAR_SPLIT = ',';
    /**
     * 表达式的默认变量名
     */
    public static final String NAME_VARIABLE = "x";

    @Override
    public List<ElementVo> getAllElements() {
        List<BaseFunctionPo> functionList = sysFunctionMapper.selectList(new LambdaQueryWrapper<BaseFunctionPo>());
        List<ElementVo> functions = BeanCopyUtil.copys(functionList, ElementVo.class);
        this.handleFunction(functions);
        return functions;
    }

    @SneakyThrows
    @Override
    public Object calculate(String functionName, Object... params) {
        ScriptDto scriptDto = this.getScriptDtoFromCache(functionName, params);
        //判断类型是否符合
        if (ObjectNull.isNotNull(scriptDto.getInParamTypes())) {
            for (int i = 0; i < scriptDto.getInParamTypes().size(); i++) {
                JvsParamType inParamType = scriptDto.getInParamTypes().get(i);
                switch (inParamType) {
                    case number:
                        if (NumberUtil.isNumber(params[i].toString())) {
                            if (NumberUtil.parseNumber(params[i].toString()).intValue() == NumberUtil.parseNumber(params[i].toString()).doubleValue()) {
                                params[i] = NumberUtil.parseNumber(params[i].toString()).intValue();
                            } else {
                                params[i] = NumberUtil.parseNumber(params[i].toString()).doubleValue();
                            }
                        }
                        break;
                    case bool:
                        if (!(params[i] instanceof Boolean)) {
                            params[i] = Boolean.valueOf(params[i].toString());
                        }
                        break;
                    case text:
                    case date:
                    case any:
                }
            }
        }
        Script script = scriptDto.getScript().newInstance();
        int expectedParamCount = scriptDto.getExpectedParamCount();
        boolean hasDynamicParam = scriptDto.isHasDynamicParam();
        Object result;
        Binding binding = buildBinding(hasDynamicParam, expectedParamCount, params);
        script.setBinding(binding);
        result = script.run();
        return result;
    }

    /**
     * 获取脚本对象
     * <p>
     * 该方法耗时主要集中在: 查库与脚本对象的初始化.
     * 所以加了缓存
     *
     * @param functionName 函数名
     * @param params       参数
     * @return 脚本对象
     */
    private ScriptDto getScriptDtoFromCache(String functionName, Object... params) {
        int paramCount = params.length;
        String cacheKey = functionName + paramCount;
        // 先获取缓存
        ScriptDto scriptDto = scriptCache.get(cacheKey);
        if (Objects.nonNull(scriptDto)) {
            return scriptDto;
        }
        // 查询函数体
        BaseFunctionPo baseFunctionPo = sysFunctionMapper.selectOne(Wrappers.<BaseFunctionPo>lambdaQuery().eq(BaseFunctionPo::getName, functionName));
        if (Objects.isNull(baseFunctionPo)) {
            throw new BusinessException("未知的函数", functionName);
        }
        int expectedParamCount = paramCount;
        Set<Integer> validParamCount = baseFunctionPo.getParamCount();
        boolean hasDynamicParam = Boolean.TRUE.equals(baseFunctionPo.getDynamicParam());
        if (!hasDynamicParam && ObjectUtils.isEmpty(validParamCount)) {
            throw new BusinessException("该函数的参数数量不确定_无法计算", functionName);
        }
        if (hasDynamicParam) {
            // 存在可变参数时, 期望参数数量取最大值
            expectedParamCount = validParamCount.stream().max(Integer::compareTo).orElse(0);
            if (expectedParamCount > paramCount) {
                throw new BusinessException("[{}]参数数量异常, 期望的参数数量: {}, 实际数量: {}", functionName, expectedParamCount, paramCount);
            }
        } else if (!validParamCount.contains(paramCount)) {
            throw new BusinessException("[{}]参数数量异常, 期望的参数数量: {}, 实际数量: {}", functionName, JSONUtil.toJsonStr(validParamCount), paramCount);
        }
        String body = baseFunctionPo.getBody();
        if (StringUtils.isBlank(body)) {
            throw new BusinessException("函数体为空无法计算", functionName);
        }
        Script script = buildScript(functionName, body, hasDynamicParam, paramCount, expectedParamCount);
        // 存入缓存
        scriptDto = new ScriptDto();
        scriptDto.setScript(script.getClass());
        scriptDto.setHasDynamicParam(hasDynamicParam);
        scriptDto.setExpectedParamCount(expectedParamCount);
        if (ObjectNull.isNotNull(baseFunctionPo.getInParamTypes())) {
            List<JvsParamType> collect = (List<JvsParamType>) baseFunctionPo.getInParamTypes().stream().map(e -> JvsParamType.valueOf(e.toString())).collect(Collectors.toList());
            scriptDto.setInParamTypes(collect);
        }
        scriptCache.put(cacheKey, scriptDto);
        return scriptDto;
    }

    /**
     * 构建脚本执行对象
     *
     * @param functionName       函数名
     * @param functionBody       函数体
     * @param hasDynamicParam    是否存在可变参数
     * @param paramCount         实际参数数量
     * @param expectedParamCount 期待的参数数量
     * @return 执行对象
     */
    public static Script buildScript(String functionName, String functionBody, boolean hasDynamicParam, int paramCount, int expectedParamCount) {
        StringBuilder builder = new StringBuilder(functionBody);
        builder.append("\nreturn ");
        builder.append(functionName);
        builder.append('(');
        // 拼接固定参数
        for (int i = 0; i < expectedParamCount; i++) {
            builder.append(NAME_VARIABLE).append(i).append(CHAR_SPLIT);
        }
        // 拼接可变参数
        if (hasDynamicParam && paramCount > expectedParamCount) {
            builder.append(NAME_VARIABLE);
            builder.append(CHAR_SPLIT);
        }
        // 删除分隔符
        if (CHAR_SPLIT == builder.charAt(builder.length() - 1)) {
            builder.deleteCharAt(builder.length() - 1);
        }
        builder.append(')');
        // Groovy
        GroovyShell shell = new GroovyShell();
        // 解析函数脚本
        return shell.parse(builder.toString());
    }

    /**
     * 构建脚本参数对象
     *
     * @param hasDynamicParam    是否存在可变参数
     * @param expectedParamCount 期待的参数数量
     * @param params             实际参数
     * @return 参数对象
     */
    public static Binding buildBinding(boolean hasDynamicParam, int expectedParamCount, Object... params) {
        int paramCount = params.length;
        Binding binding = new Binding();
        // 拼接固定参数
        for (int i = 0; i < expectedParamCount; i++) {
            binding.setProperty(NAME_VARIABLE + i, params[i]);
        }
        // 拼接可变参数
        if (hasDynamicParam && paramCount > expectedParamCount) {
            int dynamicParamCount = paramCount - expectedParamCount;
            Object[] dynamicParam = new Object[dynamicParamCount];
            System.arraycopy(params, expectedParamCount, dynamicParam, 0, dynamicParamCount);
            binding.setProperty(NAME_VARIABLE, dynamicParam);
        }
        return binding;
    }

    /**
     * 函数对象处理
     * <p>
     * 1. 将函数名作为标识
     *
     * @param functions 函数对象集合
     */
    private void handleFunction(List<ElementVo> functions) {
        if (ObjectUtils.isEmpty(functions)) {
            return;
        }
        for (ElementVo function : functions) {
            function.setId(function.getName());
        }
    }


    @Data
    private static class ScriptDto {
        Class<? extends Script> script;
        int expectedParamCount;
        boolean hasDynamicParam;
        private List<JvsParamType> inParamTypes;
    }

}
