package cn.bctools.auth.mapper;

import cn.bctools.database.interceptor.cache.JvsRedisCache;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.bctools.auth.entity.User;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author
 */
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface UserMapper extends BaseMapper<User> {

    /**
     * 通过角色id集合获取用户数据
     *
     * @param roleIds 角色id集合
     * @return 用户数据集合
     */
    @Select("SELECT su.*,sur.role_id FROM sys_user_info su LEFT JOIN sys_user_role sur ON su.id = sur.user_id WHERE sur.role_id in #{roleIds} AND su.cancel_flag = 0")
    List<User> getByRoleIds(@Param("roleIds") String roleIds);


}
