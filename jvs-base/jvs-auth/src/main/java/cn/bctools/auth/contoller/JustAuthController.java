package cn.bctools.auth.contoller;

import cn.bctools.auth.entity.OauthOther;
import cn.bctools.auth.entity.enums.OAuthTypeEnum;
import cn.bctools.auth.login.AuthRequestCustomFactory;
import cn.bctools.auth.login.LoginHandler;
import cn.bctools.auth.login.auth.OtherLoginHandler;
import cn.bctools.auth.service.OauthOtherService;
import cn.bctools.auth.service.SysConfigsService;
import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.enums.*;
import cn.bctools.common.utils.*;
import cn.bctools.common.utils.jvs.JvsServiceConfig;
import cn.bctools.common.utils.jvs.JvsSystemConfig;
import cn.bctools.database.util.IdGenerator;
import cn.bctools.gateway.entity.SysConfigs;
import cn.bctools.log.annotation.Log;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.core.lang.Dict;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.useragent.UserAgentUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.request.AuthRequest;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * @author Administrator
 * 所有的三方系统登陆的服务,包含回调,获取登陆二维码
 * 内部系统和三方系统登陆,前端回调
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/just")
public class JustAuthController {

    AuthRequestCustomFactory factory;
    SysConfigsService sysConfigService;
    JvsSystemConfig jvsSystemConfig;
    OauthOtherService oauthOtherService;
    OtherLoginHandler otherLoginHandler;
    Map<String, LoginHandler> handlerMap;
    RedisUtils redisUtils;

    /**
     * 根据类型处理,type 即类型.请求标识
     *
     * @param status   oauth2 类型
     * @param response 请求转发服务
     */
    @SneakyThrows
    @GetMapping("/oauth2")
    public void type(@RequestParam("stats") String status, @RequestParam(value = "callbackUrl", required = false) String callbackUrl, HttpServletRequest request,
                     HttpServletResponse response) {

        if (ObjectNull.isNull(callbackUrl)) {
            callbackUrl = Optional.ofNullable(request.getHeader("Referer")).orElse(callbackUrl);
        }
        String key = status + "__" + IdGenerator.getIdStr();
        redisUtils.set(SysConstant.redisKey("just", key), callbackUrl, Duration.ofMinutes(2));
        String authorize = otherLoginHandler.getAuthDefaultRequest(status).authorize(key);
        log.info("stats: {} callbackUrl : {} 回地地址为: {} ", status, callbackUrl, authorize);
        response.sendRedirect(authorize);
    }

    /**
     * 重定向的首页地址
     * 判断是否配置有三方平台的默认登录地址如果有，则返回数据，没有就返回 false即可
     */
    @GetMapping("/redirectUri")
    public R redirectUri(@RequestParam("client_id") String client_id) {
        Dict dict = Dict.create();
        List<OauthOther> one = oauthOtherService.list(Wrappers.query(new OauthOther().setEnableDefault(true)));
        if (ObjectNull.isNotNull(one)) {
            //只返回某一个三方的授权登录,只取一条数据
            Optional<OauthOther> first = one.stream().limit(1).findFirst();
            OauthOther oauthOther = first.get();
            Optional<JvsServiceConfig> serviceConfig = jvsSystemConfig.getService().stream()
                    .filter(e -> e.getName().clientId.equals(client_id))
                    .findFirst();
            Integer domain = serviceConfig.get().getPort();
            //判断是否启用了域名
            SysApplyConfig config = sysConfigService.getConfig(ConfigsTypeEnum.BACKGROUND_PERSONALIZED_CONFIGURATION);
            if (ObjectNull.isNotNull(config.getDomainName())) {
                dict.set("domain", config.getDomainName() + "." + jvsSystemConfig.getDomain());
            } else {
                dict.set("domain", jvsSystemConfig.getDomain() + ":" + domain);
            }
            dict.set("type", oauthOther.getType());
            return R.ok(dict);
        } else {
            return R.ok();
        }
    }

    /**
     * 返回可用的登录类型
     * 根据对应配置是否为空来判断
     * 默认有帐号密码
     *
     * @return 登录类型集合
     */
    @GetMapping
    public R<List<String>> index(@RequestHeader("User-Agent")String userAgent) {
        List<OAuthTypeEnum> loginTypes = new ArrayList<>();
        // 密码登录
        loginTypes.add(OAuthTypeEnum.password);
        // 短信登录需要校验当前租户是否配置了短信消息,并指定了验证码
        sysConfigService.list(new LambdaQueryWrapper<SysConfigs>().in(SysConfigs::getType, ConfigsTypeEnum.SMS_CONFIGURATION, ConfigsTypeEnum.WECHAT_OFFICIAL_ACCOUNT_CONFIGURATION, ConfigsTypeEnum.ENTERPRISE_WECHAT_APPLICATION_CONFIGURATION, ConfigsTypeEnum.LDAP_CONFIGURATION, ConfigsTypeEnum.NAIL_APPLICATION_CONFIGURATION)).stream().forEach(e -> {
            if (ObjectNull.isNotNull(e)) {
                switch (e.getType()) {
                    case SMS_CONFIGURATION:
                        SysConfigSms base = BeanCopyUtil.copy(e.getContent(), SysConfigSms.class);
                        if (ObjectNull.isNotNull(base.getTemplateCode())) {
                            loginTypes.add(OAuthTypeEnum.phone);
                        }
                        break;
                    case WECHAT_OFFICIAL_ACCOUNT_CONFIGURATION:
                        if (BeanCopyUtil.copy(e.getContent(), SysConfigBase.class).getEnableScan()) {
                            loginTypes.add(OAuthTypeEnum.wxmp);
                        }
                        break;
                    case ENTERPRISE_WECHAT_APPLICATION_CONFIGURATION:
                        if (BeanCopyUtil.copy(e.getContent(), SysConfigBase.class).getEnableScan()) {
                            loginTypes.add(OAuthTypeEnum.wxenterprise);
                        }
                        break;
                    case LDAP_CONFIGURATION:
                        loginTypes.add(OAuthTypeEnum.ldap);
                        break;
                    case NAIL_APPLICATION_CONFIGURATION:
                        if (BeanCopyUtil.copy(e.getContent(), SysConfigBase.class).getEnableScan()) {
                            loginTypes.add(OAuthTypeEnum.dd);
                        }
                        break;
                }
            }
        });
        //2025.09.04 钉钉和微信企业微信的兼容性处理  不要替换
        switch (UserAgentUtil.parse(userAgent).getBrowser().getName()) {
            case "wxwork":{
                //如果是企业微信，则只返回三方登录。
                loginTypes.removeIf(e->!e.equals(OAuthTypeEnum.wxenterprise));
            }
            case "DingTalk":{
                //如果是钉钉，则只返回三方登录。
                loginTypes.removeIf(e->!e.equals(OAuthTypeEnum.dd));
            }
        }
        //2025.09.04 钉钉和微信企业微信的兼容性处理  不要替换
        List<String> collect = loginTypes.stream().map(Enum::toString).distinct().collect(Collectors.toList());

        oauthOtherService.list(new LambdaQueryWrapper<OauthOther>().select(OauthOther::getType).isNotNull(OauthOther::getType)).stream().map(OauthOther::getType).forEach(collect::add);
        return R.ok(collect);
    }


    /**
     * 获取钉钉的配置信息
     *
     * @param redirectUri 根据域名地址不一致,返回不同的回调地址,不管是私有化部署,还是公有化部署都可以使用
     */
    @GetMapping("/config")
    public R getAuthConfig(@RequestParam(value = "redirectUri", required = false) String redirectUri) {
        SysConfigDing ding = sysConfigService.getConfig(ConfigsTypeEnum.NAIL_APPLICATION_CONFIGURATION);
        ding.setAppSecret("");
        ding.setRedirectUri(redirectUri);
        return R.ok(ding);
    }

    /**
     * 登录
     * 对应授权系统的登录类型 {@linkplain LoginHandler}
     *
     * @param response response
     * @throws IOException
     */
    @RequestMapping("/login/{oauthType}")
    public void renderAuth(@PathVariable String oauthType, HttpServletResponse response, @RequestParam("url") String url) throws IOException {
        AuthRequest authRequest = factory.get(oauthType);
        Dict set = Dict.create().set("url", url).set("type", oauthType).set("uid", IdGenerator.getIdStr());
        String state = PasswordUtil.encodePassword(JSONObject.toJSONString(set), SpringContextUtil.getKey());
        String authorize = authRequest.authorize(state);
        log.info("微信回调地址为:{}", authorize);
        response.sendRedirect(authorize);
    }

    /**
     * 替换登陆回调
     * 所有的other三方回调都需要先通过此页面,然后再进行
     *
     * @param code  code码
     * @param state 类型处理
     * @throws IOException
     */
    @SneakyThrows
    @RequestMapping("/callback")
    public void callback(@RequestParam("code") String code, @RequestParam(value = "state", required = false) String state, HttpServletRequest request, HttpServletResponse response) throws IOException {
        //根据类型查询返回的首页地址
        OauthOther one = oauthOtherService.getOne(Wrappers.query(new OauthOther().setType(state.split("__")[0])));
        if (ObjectNull.isNull(one)) {
            //兼容老版本响应处理
            String decodedPassword = PasswordUtil.decodedPassword(state, SpringContextUtil.getKey());
            JSONObject jsonObject = JSONObject.parseObject(decodedPassword);
            jsonObject.put("code", code);
            jsonObject.put("state", state);
            String url1 = jsonObject.getString("url").replace("%23", "#");
            log.info("二次转发地址为:{}", url1);
            jsonObject.remove("url");
            jsonObject.remove("uid");
            String url = HttpUtil.urlWithForm(url1, jsonObject, Charset.defaultCharset(), true);
            response.sendRedirect(url);
        } else {
            String redirectUri = (String) redisUtils.get(SysConstant.redisKey("just", state));
            if (ObjectNull.isNull(redirectUri)) {
                //默认值
                redirectUri = jvsSystemConfig.getProtocol() + jvsSystemConfig.getDomain() + ":" + jvsSystemConfig.getService().get(0).getPort() + "/";
            }
            if (redirectUri.indexOf("?") > 0) {
                response.sendRedirect(redirectUri + "&code=" + code);
            } else {
                response.sendRedirect(redirectUri + "#/other/" + state + "?code=" + code);
            }
        }
    }

    /**
     * 除基础登陆方式以外其它方式,其它所有的三方登陆
     *
     * @return
     */
    @Log
    @ApiOperation("获取其它三方服务方式")
    @GetMapping("/types")
    R<List> types() {
        List<OauthOther> list = oauthOtherService.list(new LambdaQueryWrapper<OauthOther>()
                .select(OauthOther::getType, OauthOther::getIconUrl, OauthOther::getUserInfo, OauthOther::getAccessToken, OauthOther::getAuthorize)
                .isNotNull(OauthOther::getType));
        return R.ok(list);
    }

}
