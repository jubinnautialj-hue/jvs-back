package cn.bctools.function.handler.impl;

import cn.bctools.auth.api.api.EnvironmentVariableApi;
import cn.bctools.auth.api.dto.EnvironmentVariableDto;
import cn.bctools.auth.api.enums.EnvironmentVariableEnum;
import cn.bctools.auth.api.enums.ModeTypeEnum;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.*;
import cn.bctools.function.entity.vo.ElementVo;
import cn.bctools.function.enums.JvsParamType;
import cn.bctools.function.handler.IJvsParam;
import cn.bctools.function.handler.JvsExpression;
import cn.bctools.function.service.ModeTypeService;
import cn.bctools.redis.JvsMessageListenerAdapter;
import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zx
 */
@Slf4j
@Order(1)
@JvsExpression(groupName = "环境变量", prefix = "variable")
public class VariableParamImpl extends JvsMessageListenerAdapter implements IJvsParam<ElementVo> {

    EnvironmentVariableApi environmentVariableApi;
    ModeTypeService modeTypeService;

    public VariableParamImpl(EnvironmentVariableApi variableApi, ModeTypeService modeTypeService) {
        this.environmentVariableApi = variableApi;
        this.modeTypeService = modeTypeService;
    }

    @Override
    public void onMessage(String s) {
        //清除缓存
        timedCache.clear();
    }

    /**
     * 加载系统的环境变量
     *
     * @return
     */
    @Override
    public List<ElementVo> getAllElements() {
        ModeTypeEnum mode = modeTypeService.getMode();
        R<List<EnvironmentVariableDto>> all = environmentVariableApi.getAll(mode);
        if (all.is()) {
            //如果没有环境变量直接返回空
            if (ObjectNull.isNull(all.getData())) {
                return null;
            }
        }
        return all.getData()
                .stream()
                .filter(e -> EnvironmentVariableEnum.text.equals(e.getType()))
                .map(this::paramEnums2Vo)
                .collect(Collectors.toList());
    }

    /**
     * 设置一个 5 分钟的缓存
     */
    Cache<String, Object> timedCache = CacheUtil.newTimedCache(1000 * 60 * 5);

    @Override
    public Object get(String paramName, Map<String, Object> data) {
        ModeTypeEnum mode = modeTypeService.getMode();

        if (timedCache.containsKey(TenantContextHolder.getTenantId() + "_" + mode + paramName)) {
            return timedCache.get(TenantContextHolder.getTenantId() + "_" + mode + paramName);
        }

        Object value = Optional.ofNullable(environmentVariableApi.getByKey(paramName, mode).getData()).orElseThrow(() -> new BusinessException("没有找到环境变量")).getValue();
        if (ObjectNull.isNotNull(value)) {
            timedCache.put(TenantContextHolder.getTenantId() + "_" + mode + paramName, value);
        }
        return value;
    }

    /**
     * 系统变量转表达式元素类
     *
     * @param param 系统变量美剧
     * @return 表达式元素实体类
     */
    private ElementVo paramEnums2Vo(EnvironmentVariableDto param) {
        //根据值判断类型
        return new ElementVo()
                .setId(param.getLabel())
                .setName(param.getLabel())
                .setInfo(param.getRemark())
                .setInParamTypes(new ArrayList<>())
                .setJvsParamType(JvsParamType.text);
    }

}
