package cn.bctools.index.utils;

import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.common.utils.function.Get;
import cn.bctools.index.design.ComponentBaseInfo;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * The type Design cache util.
 *
 * @author jvs The type Design cache util.
 */
@Component
@RequiredArgsConstructor
public class DesignCacheUtil {

    private final RedisUtils redisUtils;

    /**
     * dynamicHome:component:租户id:客户端id:组件key
     */
    private final String CACHE_PREFIX = "dynamicHome:component:{}:{}:{}";

    /**
     * 检查是否开启缓存
     *
     * @param metaData the meta data
     * @return boolean boolean
     */
    public Boolean check(Map<String, Object> metaData) {
        Object key = metaData.get(Get.name(ComponentBaseInfo::getKey));
        //如果没有设置唯一标识 则不管是否开启缓存 都获取实时数据
        if (key == null) {
            return false;
        }
        return Optional.of(metaData).map(e -> e.get(Get.name(ComponentBaseInfo::getEnableCache))).map(StrUtil::toStringOrNull).map(Boolean::valueOf).orElse(Boolean.FALSE);
    }

    /**
     * 获取数据
     *
     * @param metaData the meta data
     * @return object object
     */
    public Object getData(Map<String, Object> metaData) {
        String clientId = UserCurrentUtils.getCurrentUser().getClientId();
        String key = StrUtil.toString(metaData.get(Get.name(ComponentBaseInfo::getKey)));
        String redisKey = StrUtil.format(CACHE_PREFIX, TenantContextHolder.getTenantId(), clientId, key);
        return redisUtils.get(redisKey);
    }

    /**
     * 设置数据
     *
     * @param metaData the meta data
     * @param data     the data
     */
    public void setData(Map<String, Object> metaData, Object data) {
        String clientId = UserCurrentUtils.getCurrentUser().getClientId();
        String key = StrUtil.toString(metaData.get(Get.name(ComponentBaseInfo::getKey)));
        Long expireTime = Optional.of(metaData).map(e -> e.get(Get.name(ComponentBaseInfo::getIntervalTime))).map(StrUtil::toStringOrNull).map(Long::valueOf).orElse(60L);
        String redisKey = StrUtil.format(CACHE_PREFIX, TenantContextHolder.getTenantId(), clientId, key);
        redisUtils.setExpire(redisKey, data, expireTime, TimeUnit.SECONDS);
    }
}
