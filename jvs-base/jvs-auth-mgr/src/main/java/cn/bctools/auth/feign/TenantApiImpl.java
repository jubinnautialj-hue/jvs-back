package cn.bctools.auth.feign;

import cn.bctools.auth.api.api.AuthTenantServiceApi;
import cn.bctools.auth.api.dto.SysTenantDto;
import cn.bctools.auth.service.TenantService;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.gateway.entity.TenantPo;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 */
@Slf4j
@RestController
@AllArgsConstructor
@Api(tags = "[Feign]租户信息接口")
public class TenantApiImpl implements AuthTenantServiceApi {

    private final TenantService tenantService;

    @Override
    public R<SysTenantDto> getById(String tenantId) {
        TenantPo tenantPo = Optional.ofNullable(tenantService.getById(tenantId)).orElseThrow(() -> new BusinessException("租户不存在"));
        return R.ok(BeanCopyUtil.copy(tenantPo, SysTenantDto.class));
    }

    @Override
    public R<Map<String, Map<String, String>>> getById() {
        TenantContextHolder.clear();
        List<TenantPo> list = tenantService.list().stream().filter(e -> ObjectNull.isNotNull(e.getExtensionJson())).collect(Collectors.toList());
        if (ObjectNull.isNull(list)) {
            return R.ok();
        }
        List<SysTenantDto> copys = BeanCopyUtil.copys(list, SysTenantDto.class);
        Map<String, Map<String, String>> collect = copys.stream().collect(Collectors.toMap(e -> e.getId(), e -> e.getExtensionJson()));
        return R.ok(collect);
    }

    @Override
    public R<Integer> size() {
        long count = tenantService.count();
        return R.ok(Long.valueOf(count).intValue());
    }
}
