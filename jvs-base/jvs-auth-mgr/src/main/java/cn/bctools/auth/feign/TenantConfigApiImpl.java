package cn.bctools.auth.feign;

import cn.bctools.auth.api.api.AuthTenantConfigServiceApi;
import cn.bctools.auth.service.ApplyService;
import cn.bctools.auth.service.SysConfigsService;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.SysConfigBase;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.gateway.entity.SysConfigs;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author guojing
 */
@Slf4j
@RestController
@AllArgsConstructor
@Api(tags = "租户级配置信息，根据key直接获取值")
public class TenantConfigApiImpl implements AuthTenantConfigServiceApi {

    ApplyService applyService;

    SysConfigsService sysConfigService;

    @Override
    public R<String> key(ConfigsTypeEnum type, String tenantId) {
        TenantContextHolder.setTenantId(tenantId);
        SysConfigBase config = sysConfigService.getConfig(type);
        return R.ok(JSONObject.toJSONString(config));
    }

    @Override
    public R<List<ConfigsTypeEnum>> keys() {
        List<ConfigsTypeEnum> collect = sysConfigService.list(new LambdaQueryWrapper<SysConfigs>().in(SysConfigs::getType, ConfigsTypeEnum.values()))
                .stream()
                .map(e -> e.getType())
                .collect(Collectors.toList());
        return R.ok(collect);
    }

}
