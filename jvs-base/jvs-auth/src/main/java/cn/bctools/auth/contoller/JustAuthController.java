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
        printRequestSource("/just/oauth2",request);
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
        log.info("/just/redirectUri的client_id为:{}", client_id);
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
            printRequestSource("/just/callback",request);

            String redirectUri = (String) redisUtils.get(SysConstant.redisKey("just", state));
            if (ObjectNull.isNull(redirectUri)) {
                //默认值
                redirectUri = jvsSystemConfig.getProtocol() + jvsSystemConfig.getDomain() + ":" + jvsSystemConfig.getService().get(0).getPort() + "/";
            }
            log.info("/just/callback的redirectUri为:{}", redirectUri);
            if (redirectUri.indexOf("?") > 0) {
                if(redirectUri.contains("doc") || redirectUri.contains("bi")){
                    String[] split = redirectUri.split("\\?");
                    String domain = split[0];
                    String param = split[1];
                    String newUri = domain+"#/other/"+state+"?"+param;
                    log.info("/just/callback的newUri为:{}", newUri);
                    if(redirectUri.contains("code=")){
                        response.sendRedirect(newUri);
                    }else {
                        response.sendRedirect(newUri + "&code=" + code);
                    }
                }else {
                    log.info("/just/callback的newUri2为:{}", redirectUri);
                    response.sendRedirect(redirectUri + "&code=" + code);
                }
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

    // 工具方法：获取请求头，若为空则返回默认值
    private String getHeaderOrDefault(HttpServletRequest request, String headerName, String defaultValue) {
        String value = request.getHeader(headerName);
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }

    public void printRequestSource(String api,HttpServletRequest request) {
        // 1. 构建来源信息字符串
        StringBuilder sourceInfo = new StringBuilder("=== 当前接口请求来源信息 ===\n");
        sourceInfo.append("0.接口：").append(api).append("\n");
        // 2. 基础来源信息（必选）
        sourceInfo.append("1. 请求方式：").append(request.getMethod()).append("\n");
        sourceInfo.append("2. 目标接口完整URL：").append(request.getRequestURL().toString()).append("\n");
        sourceInfo.append("3. 目标接口路径：").append(request.getRequestURI()).append("\n");
        sourceInfo.append("4. 来源页面（Referer）：").append(getHeaderOrDefault(request, "Referer", "无（直接访问/Referer被禁用）")).append("\n");
        sourceInfo.append("5. 客户端IP：").append(getClientRealIp(request)).append("\n");
        sourceInfo.append("6. 客户端设备信息（User-Agent）：").append(getHeaderOrDefault(request, "User-Agent", "无")).append("\n");

        // 3. 反向代理相关来源信息（可选，若用Nginx等代理需配置）
        sourceInfo.append("7. 反向代理原始IP（X-Real-IP）：").append(getHeaderOrDefault(request, "X-Real-IP", "无")).append("\n");
        sourceInfo.append("8. 反向代理转发IP链（X-Forwarded-For）：").append(getHeaderOrDefault(request, "X-Forwarded-For", "无")).append("\n");

        // 4. 目标服务信息（当前服务的域名/端口）
        sourceInfo.append("9. 目标服务域名：").append(request.getServerName()).append("\n");
        sourceInfo.append("10. 目标服务端口：").append(request.getServerPort()).append("\n");
        log.info(sourceInfo.toString());
    }


    // 工具方法：获取客户端真实IP（处理反向代理场景）
    private String getClientRealIp(HttpServletRequest request) {
        // 1. 先从反向代理头中获取（Nginx等代理需配置转发这些头）
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        // 2. 若反向代理头无值，从请求原始IP获取
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 3. 处理 X-Forwarded-For 多IP场景（格式：客户端IP,代理IP1,代理IP2）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim(); // 取第一个IP（原始客户端IP）
        }
        return ip;
    }


}
