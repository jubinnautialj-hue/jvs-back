package cn.bctools.auth.mapper;

import cn.bctools.auth.entity.UserInvite;
import cn.bctools.database.interceptor.cache.JvsRedisCache;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;

/**
 * @author xh
 */
public interface UserInviteMapper extends BaseMapper<UserInvite> {
}
