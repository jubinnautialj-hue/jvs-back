package cn.bctools.redis.config;

import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.common.utils.TenantContextHolder;
import org.springframework.cache.annotation.Cacheable;

/**
 * 针对自定义缓存{@link Cacheable}  name的值 支持多租户,或平台或登陆用户的多维度的缓存更新
 *
 * @author gj
 */
public class JvsCacheConvert {

    /**
     * 根据传递的注解value值,确定是否需要进行name转换
     * 此处实现了用户登陆时缓存,平台级缓存,多租户缓存
     * 可以单独清除某个用户的缓存,或清除平台级缓存,或租户级缓存
     *
     * @param name {@link Cacheable} 注解时的value值
     * @return
     */
    public String cacheName(String name) {
        //判断是否有平台前缀,如果没有,默认都获取租户
        if (name.startsWith(SysConstant.CACHE_PLATFORM_PREFIX)) {
            return name;
        } else if (name.startsWith(SysConstant.CACHE_USER_PREFIX)) {
            //判断是否有用户前缀,如果有,则添加用户前缀
            name = SysConstant.CACHE_TENANT_DEFAULT_PREFIX + TenantContextHolder.getTenantId() + ":" + SysConstant.CACHE_USER_PREFIX + SystemThreadLocal.get(SysConstant.USERID) + ":" + name;
        } else {
            //默认添加租户前缀
            name = SysConstant.CACHE_TENANT_DEFAULT_PREFIX + TenantContextHolder.getTenantId() + ":" + name;
        }
        return SysConstant.CACHE + name;
    }

}
