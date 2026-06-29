package cn.bctools.auth.mapper;

import cn.bctools.auth.entity.Dept;
import cn.bctools.database.interceptor.cache.JvsRedisCache;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 部门基础
 *
 * @author
 */
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)

public interface DeptMapper extends BaseMapper<Dept> {

    /**
     * 根据删除状态, 和数据
     *
     * @param delFlag 删除状态
     * @param source  这个部门的数据来源,可能是同步fpgor数据
     * @param deptIds 部门数组
     */
    @Update("UPDATE sys_dept SET del_flag = ${delFlag} WHERE id in ${deptIds} AND source = '${source}'")
    void updateDelFlag(@Param("delFlag") Boolean delFlag, @Param("source") String source, @Param("deptIds") String deptIds);
}
