package cn.bctools.auth.api.api;

import cn.bctools.auth.api.dto.SysTenantDto;
import cn.bctools.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * The interface Auth tenant service api.
 *
 * @author zhuxiaokang  租户信息获取接口
 */
@FeignClient(value = "jvs-auth-mgr", contextId = "tenant")
public interface AuthTenantServiceApi {

    /**
     * The constant PREFIX.
     */
    String PREFIX = "/api/tenant";

    /**
     * 查询单个租户信息
     *
     * @param tenantId 租户id
     * @return 租户信息 by id
     */
    @GetMapping(PREFIX + "/query/tenant/by/id/{tenantId}")
    R<SysTenantDto> getById(@PathVariable("tenantId") String tenantId);

    /**
     * 统计个数
     *
     * @return r
     */
    @GetMapping(PREFIX + "/size")
    R<Integer> size();
}
