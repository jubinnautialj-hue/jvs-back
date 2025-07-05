package cn.bctools.auth.contoller;

import cn.bctools.common.utils.jvs.JvsSystemConfig;
import cn.bctools.auth.service.ApplyService;
import cn.bctools.auth.service.SysConfigsService;
import cn.bctools.auth.service.TenantService;
import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.SysApplyConfig;
import cn.bctools.common.utils.*;
import cn.bctools.common.utils.function.Get;
import cn.bctools.gateway.entity.Apply;
import cn.bctools.gateway.entity.TenantPo;
import cn.bctools.oauth2.config.JvsDefaultBearerTokenResolver;
import cn.bctools.oauth2.dto.CustomUser;
import cn.bctools.web.utils.WebUtils;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 * 用户访问页面时,获取的域名消息入口
 *
 * @author zhuxiaokang
 */
@RestController
@RequestMapping
@AllArgsConstructor
public class ApplyAuthController {

    private final SysConfigsService sysConfigService;
    private final ApplyService applyService;
    private final TenantService tenantService;
    private final JvsSystemConfig jvsSystemConfig;
    private final OAuth2AuthorizationService oAuth2AuthorizationService;

    /**
     * 根据终端获取对应的应用名称,背景,登陆的login
     *
     * @param clientId
     * @return
     */
    @GetMapping("/api/domain")
    @Cacheable(value = SysConstant.CACHE_CLIENT, key = "'domaon'+#clientId")
    public R<Object> domain(@RequestParam(value = "client_id", required = false) String clientId) {
        //根据域名上下文获取租户
        String tenantId = TenantContextHolder.getTenantId();
        tenantId = Optional.ofNullable(WebUtils.getRequest()).map(e -> new JvsDefaultBearerTokenResolver().resolve(e))
                .map(e -> oAuth2AuthorizationService.findByToken(e, OAuth2TokenType.ACCESS_TOKEN))
                .map(e -> e.getAttribute("user"))
                .map(e -> (CustomUser) e)
                .map(e -> e.getUserDto().getTenantId())
                .orElse(tenantId);
        TenantContextHolder.setTenantId(tenantId);
        String shortName = tenantService.getById(tenantId).getName();
        String finalClientId = clientId;
        ConfigsTypeEnum configsTypeEnum = Arrays.stream(ConfigsTypeEnum.values()).filter(e -> ObjectNull.isNotNull(e.clientId)).filter(e -> e.clientId.equals(finalClientId)).findFirst().get();
        SysApplyConfig base = sysConfigService.getConfig(configsTypeEnum);

        //没有匹配到租户,则直接返回终端的配置
        if (ObjectNull.isNotNull(base)) {
            Map<String, Object> config = BeanToMapUtils.beanToMap(base);
            config.put(Get.name(JvsSystemConfig::getMultiTenantMode), jvsSystemConfig.getMultiTenantMode());
            config.put(Get.name(JvsSystemConfig::getFilingInformation), jvsSystemConfig.getFilingInformation());
            config.put(Get.name(JvsSystemConfig::getKkfileUrl), jvsSystemConfig.getKkfileUrl());
            config.put(Get.name(JvsSystemConfig::getDomain), jvsSystemConfig.getDomain());
            config.put("version", SpringContextUtil.getVersion());
            config.remove("defaultPassword");
            return R.ok(config);
        } else {
            Apply apply = applyService.getOne(new LambdaQueryWrapper<Apply>().eq(Apply::getAppKey, clientId));
            if (ObjectNull.isNotNull(apply)) {
                TenantPo copy = BeanCopyUtil.copy(apply, TenantPo.class);
                copy.setDescMsg(apply.getDescribes());
                copy.setName(shortName);
                String s = JSONObject.toJSONString(copy);
                JSONObject jsonObject = JSONObject.parseObject(s);
                jsonObject.put(Get.name(JvsSystemConfig::getMultiTenantMode), jvsSystemConfig.getMultiTenantMode());
                jsonObject.put(Get.name(JvsSystemConfig::getFilingInformation), jvsSystemConfig.getFilingInformation());
                jsonObject.put(Get.name(JvsSystemConfig::getKkfileUrl), jvsSystemConfig.getKkfileUrl());
                jsonObject.put(Get.name(JvsSystemConfig::getDomain), jvsSystemConfig.getDomain());
                jsonObject.put("version", SpringContextUtil.getVersion());
                return R.ok(jsonObject);
            } else {
                return null;
            }
        }
    }

}
