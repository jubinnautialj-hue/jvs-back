package cn.bctools.auth.service;

import cn.bctools.common.enums.SysApplyConfig;
import cn.bctools.gateway.entity.TenantPo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 公司租户管理
 *
 * @author guojing
 */
public interface TenantService extends IService<TenantPo> {

    /**
     * 获取子租户
     *
     * @return 租户集合
     */
    List<TenantPo> getChild();

    /**
     * 根据请求域名获取租户id
     *
     * @param host      登录域名
     * @param tenantIds 登录用户匹配的租户id集合
     * @param appKey
     * @return 租户id
     */
    Optional<SysApplyConfig> getTenantIdFromHost(String host, Set<String> tenantIds, String appKey);
}
