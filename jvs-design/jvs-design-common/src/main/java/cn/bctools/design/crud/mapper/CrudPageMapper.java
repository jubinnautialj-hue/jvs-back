package cn.bctools.design.crud.mapper;

import cn.bctools.database.annotation.JvsCacheLinkClear;
import cn.bctools.database.interceptor.cache.JvsRedisCache;
import cn.bctools.design.crud.dto.CrudPageMenu;
import cn.bctools.design.crud.entity.CrudPage;
import cn.bctools.design.menu.mapper.AppMenuMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


/**
 * 列表的列表配置项
 *
 * @author guojing
 */
@Mapper
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface CrudPageMapper extends BaseMapper<CrudPage> {

}
