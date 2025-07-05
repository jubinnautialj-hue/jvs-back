package cn.bctools.auth.controller;


import cn.bctools.auth.constants.AuthConstant;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.jvs.JvsSystemConfig;
import cn.bctools.auth.entity.po.DynamicResource;
import cn.bctools.auth.service.OauthOtherService;
import cn.bctools.auth.service.SysConfigsService;
import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.SysApplyConfig;
import cn.bctools.common.enums.SysConfigBase;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.web.utils.WebUtils;
import cn.hutool.core.lang.Dict;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author guojing
 */
@Slf4j
@AllArgsConstructor
@Api(value = "动态服务资源", tags = "动态服务资源")
@RestController
@RequestMapping("/dynamicResource")
public class DynamicResourceController {

    DiscoveryClient discoveryClient;
    SysConfigsService configService;
    OauthOtherService oauthOtherService;
    JvsSystemConfig jvsSystemConfig;

    public static void main(String[] args) {
        JvsSystemConfig jvsSystemConfig = new JvsSystemConfig();
        jvsSystemConfig.setMultiTenantMode(true);
        jvsSystemConfig.setDomain("jvs.m.prd.szunicom.cn");
        boolean matches = pattern.matcher(jvsSystemConfig.getDomain()).matches();
        boolean b = jvsSystemConfig.getMultiTenantMode() && matches;
        System.out.println(b);
    }

    static boolean data = false;
    // 定义域名的正则表达式
    static String DOMAIN_REGEX =
            "^(?!-)(?!.*--)(?!.*-\\.)[A-Za-z0-9-]{1,63}(?!-)(\\.[A-Za-z]{2,})+$";

    static Pattern pattern = Pattern.compile(DOMAIN_REGEX);

    @Log
    @ApiOperation(value = "是否有低代码资源", notes = "是否有低代码资源,服务不存在,则请求地址就不操作")
    @GetMapping("/design")
    public R design() {
        log.info(WebUtils.getRequest().getScheme());
        if (!data) {
            data = discoveryClient.getInstances(ConfigsTypeEnum.JVS_DESIGN_MGR.name().toLowerCase().replace("_", "-")).size() > 0;
        }
        return R.ok(Dict.create()
                .set(ConfigsTypeEnum.JVS_DESIGN_MGR.name(), data)
                .set(ConfigsTypeEnum.JVS_DESIGN_DEFAULT_MODE.name(), jvsSystemConfig.getDesignDefaultMode()));
    }

    @Log(back = false)
    @ApiOperation(value = "获取服务资源", notes = "后台判断那些服务资源有启动， 根据启动的服务返回资源")
    @GetMapping
    public R<List> index(@RequestParam(value = "client_id", defaultValue = "frame") String clientId) {
        UserDto currentUser = UserCurrentUtils.getCurrentUser();
        //获取当前租户是否配置了域名，如果没有配置域名，则使用主租户的域名
        SysApplyConfig applyConfig = configService.getConfig(ConfigsTypeEnum.BACKGROUND_PERSONALIZED_CONFIGURATION);
        if (ObjectNull.isNull(applyConfig.getDomainName())) {
            TenantContextHolder.setTenantId("1");
        }
        AtomicReference<String> url = new AtomicReference<>("");
        //判断是是多租户(域名),还是单租户Ip(模式)
        //根据配置获取相关其它服务资源的功能
        List<DynamicResource> service = jvsSystemConfig.getService().stream().map(e -> {
            String shortName = "";
            //如果是多租户模式,则根据租户信息,获取对应的配置信息
            if (jvsSystemConfig.getMultiTenantMode()) {

                SysApplyConfig config = configService.getConfig(e.getName());
                //如果子租户没有此配置，使用主租户信息
                if (ObjectNull.isNotNull(config) && ObjectNull.isNotNull(config.getDomainName())) {
                    shortName = config.getDomainName() + ".";
                } else if (ObjectNull.isNotNull(config.getDomainName()) && !"1".equals(currentUser.getTenantId())) {
                    //查询如果是顶级租户，就获取顶级租户的信息
                    TenantContextHolder.setTenantId("1");
                    config = configService.getConfig(e.getName());
                    shortName = config.getDomainName() + ".";
                }
            }
            DynamicResource dynamicResource =
                    new DynamicResource().setClientId(e.getName().clientId).setType(e.getName()).setName(SpringContextUtil.msg(e.getName().msg)).setUrl("http://" + shortName + jvsSystemConfig.getDomain() + (jvsSystemConfig.getMultiTenantMode() && pattern.matcher(jvsSystemConfig.getDomain()).matches() ? "" :
                            (":" + e.getPort()))).setDesc(e.getName().desc).setIconUrl(e.getName().iconUrl);
            if (e.getName().equals(ConfigsTypeEnum.BACKGROUND_PERSONALIZED_CONFIGURATION)) {
                //判断资源类型是否包含，如果不包含就不返回
                if (!UserCurrentUtils.init().getPermissions().contains(AuthConstant.jvs_base_permission)) {
                    return null;
                }
                url.set(dynamicResource.getUrl());
            }
            return dynamicResource;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        TenantContextHolder.setTenantId(UserCurrentUtils.getCurrentUser().getTenantId());
        //加载其它的配置的三方服务模块
        oauthOtherService.list().stream().forEach(e -> {
            try {
                URI uri = new URI(e.getAuthorize());
                DynamicResource dynamicResource = new DynamicResource().setClientId(e.getName()).setName(e.getName()).setUrl(uri.getScheme() + "://" + uri.getHost()).setIconUrl(e.getIconUrl());
                service.add(dynamicResource);
            } catch (URISyntaxException ex) {
            }
        });
        //记录重定向地址由后端处理需要记录
        service.forEach(e -> {
            if (e.getName().equals(ConfigsTypeEnum.BACKGROUND_PERSONALIZED_CONFIGURATION.msg)) {
                e.setUrl("/auth/token/" + e.getClientId() + "?redirect_url=" + url.get() + "/#/myiframe/urlPath?name=基本信息&src=%2Fjvs-upms-ui%2F#/systeminfo");
            } else {
                try {
                    e.setUrl("/auth/token/" + e.getClientId() + "?redirect_url=" + URLEncoder.encode(e.getUrl(), Charset.defaultCharset().name()));
                } catch (UnsupportedEncodingException ex) {

                }
            }
        });
        design();
        if (data) {
            if (UserCurrentUtils.init().getPermissions().contains("jvs_app_add")) {
                service.add(0, new DynamicResource().setUrl(url.get() + "/#/wel/index?login=isLogin").setName("应用开发").setIconUrl("/jvs-ui-public/img/appdev.png"));
            }
        }
        //判断是否是有 ai
        if (UserCurrentUtils.init().getPermissions().contains(AuthConstant.jvs_ai_manager)) {
            if (ObjectNull.isNotNull(jvsSystemConfig.getAidomain())) {
                //域名
                service.add(new DynamicResource().setUrl(jvsSystemConfig.getAidomain()).setName("AI控制台").setIconUrl("/jvs-ui-public/img/aiconsole.png"));
            }
        }
        //判断是否是平台用户
        if (UserCurrentUtils.init().getPermissions().contains(AuthConstant.jvs_platform_permission)) {
            //添加后台和应用开发地址
            service.add(new DynamicResource().setUrl(url.get() + "/#/myiframe/urlPath?name=平台信息&src=%2Fjvs-upms-ui%2F#/appBasicInfo").setName("平台运维").setIconUrl("/jvs-ui-public/img/platformope.png"));
        }
        return R.ok(service);
    }

    @Log
    @ApiOperation(value = "判断此配置是否存在", notes = "判断配置是否存在,不存在 ,则不显示界面")
    @GetMapping("/check/{type}")
    @Cacheable(value = SysConstant.CACHE_SYS_CONFIG)
    public R<SysConfigBase> check(@PathVariable ConfigsTypeEnum type) {
        SysConfigBase config = configService.getConfig(type);
        return R.ok(config);
    }

}
