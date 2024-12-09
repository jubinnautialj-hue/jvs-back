package cn.bctools.auth.mapper;

import cn.bctools.auth.entity.LoginLog;
import cn.bctools.database.interceptor.cache.JvsRedisCache;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author : GaoZeXi
 */
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface LoginLogMapper extends BaseMapper<LoginLog> {
}
