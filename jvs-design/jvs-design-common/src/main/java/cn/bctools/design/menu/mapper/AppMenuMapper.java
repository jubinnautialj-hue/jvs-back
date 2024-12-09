package cn.bctools.design.menu.mapper;

import cn.bctools.database.interceptor.cache.JvsRedisCache;
import cn.bctools.design.menu.entity.AppMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author zhuxiaokang
 * 菜单权限表 Mapper 接口
 */
@Mapper
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface AppMenuMapper extends BaseMapper<AppMenu> {

}
