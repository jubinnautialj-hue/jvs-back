package cn.bctools.auth.feign;

import cn.bctools.auth.api.api.AuthTenantConfigServiceApi;
import cn.bctools.auth.service.ApplyService;
import cn.bctools.auth.service.SysConfigsService;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.SysApplyConfig;
import cn.bctools.common.enums.SysConfigBase;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.common.utils.jvs.JvsServiceConfig;
import cn.bctools.common.utils.jvs.JvsSystemConfig;
import cn.bctools.gateway.entity.SysConfigs;
import cn.bctools.web.utils.IpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
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
    JvsSystemConfig jvsSystemConfig;

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

    @Override
    public R<String> domain(String tenantId, ConfigsTypeEnum type) {
        Optional<JvsServiceConfig> first = jvsSystemConfig.getService().stream().filter(e -> e.getName().equals(type)).findFirst();
        String shortName = "";
        if (IpUtil.isIpAddress(jvsSystemConfig.getDomain())) {
            //拼接地址
            String url = "http://" + jvsSystemConfig.getDomain() + ":" + first.get().getPort();
            return R.ok(url);
        }
        //如果是多租户模式,则根据租户信息,获取对应的配置信息
        if (jvsSystemConfig.getMultiTenantMode()) {
            SysApplyConfig config = sysConfigService.getConfig(type);
            //如果子租户没有此配置，使用主租户信息
            if (ObjectNull.isNotNull(config) && ObjectNull.isNotNull(config.getDomainName())) {
                shortName = config.getDomainName() + ".";
            } else if (ObjectNull.isNotNull(config.getDomainName()) && !"1".equals(tenantId)) {
                //查询如果是顶级租户，就获取顶级租户的信息
                TenantContextHolder.setTenantId("1");
                config = sysConfigService.getConfig(type);
                shortName = config.getDomainName() + ".";
            }
        }
        String url = "http://" + shortName + jvsSystemConfig.getDomain() + (jvsSystemConfig.getMultiTenantMode() ? "" :
                (":" + first.get().getPort()));
        return R.ok(url);
    }

}
