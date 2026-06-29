package cn.bctools.gateway.utils;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;

import java.util.List;

/**
 * 保存配置缓存的工具类，只针对网关
 *
 * @author gx
 */
public class CacheUtils {

    /**
     * The enum Type.
     */
    public enum Type {
        /**
         * Xss type.
         */
        xss,
        /**
         * Encode type.
         */
        encode,
        /**
         * Ip type.
         */
        ip,
        /**
         * Code type.
         */
        code,
        /**
         * Permission type.
         */
        permission,
        /**
         * Path type.
         */
        path,
    }

    /**
     * 缓存对象
     * 用于缓存所有配置信息
     */
    public static Cache<Type, List> cache = CacheUtil.newFIFOCache(20000);
    public static Cache<String, List<String>> pathCache = CacheUtil.newTimedCache(1000 * 5 * 60);



}
