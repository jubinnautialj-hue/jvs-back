package cn.bctools.design.data.mapper;

import cn.bctools.database.interceptor.cache.JvsRedisCache;
import cn.bctools.design.data.entity.DataEventPo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据事件
 *
 * @Author: GuoZi
 */
@Mapper
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface DataEventMapper extends BaseMapper<DataEventPo> {

}
