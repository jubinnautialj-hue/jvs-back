package cn.bctools.redis.annotation;

import org.springframework.cache.annotation.Cacheable;

/**
 * 跳过租户,判断是否需要添加租户或添加租户
 * 配合{@link Cacheable}使用,因原有cache注解不支持多租户, 某些情况需要缓存是共用的,有些缓存是租户隔离的,
 * 默认不添加此注解为多租户隔离,数据将以租户Id为 主键key拼装后包含租户信息,此只做声明
 * key的生成逻辑查看 {@link JvsCacheKeyGenerator}
 *
 * @author gj
 */
public @interface CacheTenantSkip {

}
