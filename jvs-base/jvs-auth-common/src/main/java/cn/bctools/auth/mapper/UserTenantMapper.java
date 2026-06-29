package cn.bctools.auth.mapper;

import cn.bctools.auth.entity.UserTenant;
import cn.bctools.auth.entity.handler.DeptJsonTypeHandler;
import cn.bctools.auth.entity.po.TenantUserData;
import cn.bctools.database.interceptor.cache.JvsRedisCache;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.*;

/**
 * @author
 */
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface UserTenantMapper extends BaseMapper<UserTenant> {

    /**
     * 租户和用户关联表别名
     */
    String SYS_USER_TENANT_ALIAS = "sysUserTenant";
    /**
     * 用户表别名
     */
    String SYS_USER_ALIAS = "sysUser";

    /**
     * 分页查询租户下的用户
     *
     * @param page    分页条件
     * @param wrapper 查询条件
     * @return
     */
    @Select(" SELECT " +
            " sysUserTenant.dept_id as deptId,sysUserTenant.*, " +
            " sysUser.email, sysUser.sex, sysUser.account_name, sysUser.head_img, sysUser.invite, sysUser.user_type, sysUser.birthday  " +
            " FROM sys_user_tenant sysUserTenant LEFT JOIN sys_user_info sysUser ON sysUserTenant.user_id = sysUser.id " +
            " ${ew.customSqlSegment} " +
            " ORDER BY sysUserTenant.create_time desc")
    @Results({
            @Result(property = "deptId", column = "deptId", typeHandler = DeptJsonTypeHandler.class),
            // 其他字段的映射
    })
    IPage<TenantUserData> tenantUsers(Page page, @Param("ew") Wrapper wrapper);
}
