package cn.bctools.design.menu.mapper;

import cn.bctools.database.interceptor.cache.JvsRedisCache;
import cn.bctools.design.menu.entity.AppMenuType;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author zhuxiaokang
 * 轻应用菜单目录 Mapper 接口
 */
@Mapper
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface AppMenuTypeMapper extends BaseMapper<AppMenuType> {

}
