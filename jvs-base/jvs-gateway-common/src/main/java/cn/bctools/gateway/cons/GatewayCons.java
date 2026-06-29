package cn.bctools.gateway.cons;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;

import java.util.List;

/**
 * @author guojing
 */
public class GatewayCons {
    /**
     * xss,是否放开,等配置,默认20分钟后自动更新缓存,不使用redis缓存
     */
    public static Cache<String, List> cache = CacheUtil.newTimedCache(60 * 60 * 15);

    public static final String XSS = "gateway:xss";
    public static final String ENCODE = "gateway:encode";
    public static final String IP = "gateway:ip";
    public static final String PATH = "gateway:path";
    public static final String PERMISSIONS = "PERMISSIONS";

}
