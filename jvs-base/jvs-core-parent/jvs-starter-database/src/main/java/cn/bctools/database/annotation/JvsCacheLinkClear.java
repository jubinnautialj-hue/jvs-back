package cn.bctools.database.annotation;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.lang.annotation.*;

/**
 * 缓存关联清除功能,
 * 开启了mybatis plus二级缓存时,并将mapper添加了支持注解
 * <p>
 *
 * @CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
 * <p>
 * 此注解标识,查询数据时第一次查询数据库,并将数据缓存到redis中.下一次相同查询条件,直接访问redis 不再操作数据库
 * 当数据被修改时,会清除缓存.
 * 其它情况
 * 如果此mapper查询有a,b两张表
 * a,b开启了二级缓存
 * 使用到了关联查询
 * 当a发生改变时,a,b清除缓存,a,b关联查询会正常
 * 当b发生改变时,不会清除a,b关联查询缓存,查询将导致数据错误
 * <p>
 * 解决办法:
 * 添加此关联注解{@linkplain  JvsCacheLinkClear}
 * 如a发生变化后,将关联清除其它mapper下所有的缓存
 * <p>
 * 注意,不允许,相互关联,可能会导致系统崩溃
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JvsCacheLinkClear {

    /**
     * 可以添加多个关联清除的Mapper缓存
     *
     * @return
     */
    Class<? extends BaseMapper>[] value();

}
