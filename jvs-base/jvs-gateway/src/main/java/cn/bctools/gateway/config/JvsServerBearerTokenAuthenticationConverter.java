package cn.bctools.gateway.config;

import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.gateway.entity.GatewayIgnorePathPo;
import cn.bctools.gateway.entity.Permission;
import cn.bctools.gateway.mapper.GatewayIgnorePathMapper;
import cn.bctools.gateway.mapper.PermissionMapper;
import cn.bctools.gateway.utils.CacheUtils;
import cn.bctools.gateway.utils.IpUtils;
import cn.bctools.oauth2.config.JvsDefaultBearerTokenResolver;
import cn.bctools.oauth2.config.JvsOAuth2AuthorizationServiceImpl;
import cn.bctools.oauth2.dto.CustomUser;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.server.resource.BearerTokenErrors;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * 默认网关开放地址不处理,存放在sys_gateway_ignore_path 表中,其它的都需要有token,根据资源权限表和角色表确定是否有api的权限
 * api请求时没有匹配到相关权限,则登陆即可访问.
 *
 * @author guojing
 */
@Slf4j
@Component
public class JvsServerBearerTokenAuthenticationConverter implements ServerAuthenticationConverter {

    @Resource
    RedisUtils redisUtils;
    @Resource
    GatewayIgnorePathMapper ignorePathMapper;
    @Resource
    PermissionMapper permissionMapper;

    @Resource
    JvsOAuth2AuthorizationServiceImpl jvsOAuth2AuthorizationService;
    static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    String s = "null";
    String pattern = "/mgr/**";
    public static final String APIDOC = "/mgr/**/v3/api-docs";

    public static String JVS_PERMISSION_LIST = "jvs_permission_list";
    public static String JVS_IP = "jvs_IP";

    public static String REFERER = "REFERER";

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        // 1、从Redis中获取当前路径可访问角色列表
        ServerHttpRequest request = exchange.getRequest();
        String ip = IpUtils.getIp(request);
        //取请求头信息，进行判断
        String referer = request.getHeaders().getFirst("user-agent");
        //处理浏览器标识上有多余空格
        referer = referer.replaceAll("\\s+", " ");
        //去掉代理信息
        for (String key : request.getHeaders().keySet()) {
            if (key.toLowerCase().contains("proxy")) {
                String first = request.getHeaders().getFirst(key);
                referer = referer.replace(first, "").trim();
            }
        }

        HttpMethod method = request.getMethod();
        String url = request.getPath().toString();
        String token = token(exchange.getRequest(), referer);
        log.info("token:{}", token);
        if (ObjectNull.isNotNull(token)) {
            //url匹配地址, 如果没有匹配到,表示接口直接放开没有配置资源地址,如果配置了,这里每一次都需要匹配一下, 因为存在/{变量}/地址
            List<String> permissionList = CacheUtils.pathCache.get(url + method.toString(), () -> matchStart(url, method));
            //将命中的资源放在上下文中
            exchange.getAttributes().put(JVS_PERMISSION_LIST, permissionList);
            exchange.getAttributes().put(REFERER, referer);
            exchange.getAttributes().put(JVS_IP, ip);
            exchange.getAttributes().put(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            return Mono.just(new BearerTokenAuthenticationToken(token));
        }
        //判断是否放开
        if (getIgnorePath(url)) {
            return Mono.empty();
        }
        throw new BusinessException("用户未登录").setCode(-2);
    }


    private String token(ServerHttpRequest request, String referer) {
        String authorizationHeaderToken = resolveFromAuthorizationHeader(request.getHeaders());
        String parameterToken = resolveAccessTokenFromRequest(request);

        if (authorizationHeaderToken != null) {
            if (parameterToken != null) {
                BearerTokenError error = BearerTokenErrors.invalidRequest("Found multiple bearer tokens in the request");
                throw new OAuth2AuthenticationException(error);
            }
            return authorizationHeaderToken;
        }
        if (parameterToken != null && isParameterTokenSupportedForRequest(request)) {
            return parameterToken;
        }

        //如果都没有,判断cookie 是否存在,如果存在,则直接返回对应的token
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        log.info("获取域名的cookie值" + JSON.toJSONString(cookies));
        boolean b = cookies.containsKey(SysConstant.JVS);
        if (b) {
            HttpCookie jvsSessionUid = cookies.getFirst(SysConstant.JVS);
            String value = jvsSessionUid.getValue();
            Optional<List<OAuth2Authorization>> oAuth2Authorizations = Optional.ofNullable(value)
                    .map(s -> jvsOAuth2AuthorizationService.keys(s, OAuth2TokenType.ACCESS_TOKEN));
            String string = oAuth2Authorizations
                    .filter(ObjectNull::isNotNull)
                    .map(e -> e.get(0))
                    .filter(e -> ((CustomUser) e.getAttribute("user")).getUserDto().getIp().contains(IpUtils.getIp(request)))
                    .filter(e -> ((CustomUser) e.getAttribute("user")).getUserDto().getUserAgent().equals(referer))
                    .map(e -> e.getAccessToken().getToken().getTokenValue())
                    .orElseGet(() -> "");
            if (ObjectNull.isNull(string)) {
                if (!redisUtils.hasKey("jvs:token:" + value)) {

//                    try {
//                        //删除过期的信息
//                        oAuth2Authorizations.get().forEach(e -> {
//                            jvsOAuth2AuthorizationService.remove(e);
//                        });
//                    } catch (Exception e) {
//                    }
                }
            }
            return string;
        }
        return "";
    }


    public Boolean getIgnorePath(String path) {
        //如果不是以/mgr/**开头的则表示直接放开
        if (!PATH_MATCHER.matchStart(pattern, path)) {
            return true;
        }
        if (PATH_MATCHER.match(APIDOC, path)) {
            return true;
        }
        //如果是外部开发地址，则直接放开
        List<GatewayIgnorePathPo> list = CacheUtils.cache.get(CacheUtils.Type.path, () -> ignorePathMapper.selectList(Wrappers.query()));
        if (ObjectNull.isNotNull(list)) {
            for (GatewayIgnorePathPo api : list) {
                //匹配是否开放
                if (PATH_MATCHER.matchStart(api.getPath().toString(), path)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * 根据路径,和方法,匹配规则
     *
     * @param path
     * @param method
     * @return
     */
    public List<String> matchStart(String path, HttpMethod method) {
        //需要开启是否使用局部缓存
        List<Permission> permissions = CacheUtils.cache.get(CacheUtils.Type.permission, () -> list());
        //如果是外部开发地址，则直接放开
        return permissions.stream().filter(e -> {
            if (PATH_MATCHER.matchStart(e.getApi(), path)) {
                if (ObjectNull.isNotNull(e.getMethodType())) {
                    return method.toString().toLowerCase().equals(e.getMethodType().toLowerCase());
                }
            }
            return false;
        }).map(Permission::getPermission).collect(Collectors.toList());
    }

    public List<Permission> list() {
        //根据URL找到对应的资源名称
        List<Permission> permissions = permissionMapper.selectList(Wrappers.query());
        return permissions.stream().filter(e -> ObjectUtil.isNotEmpty(e.getApi())).collect(Collectors.toList());
    }

    private boolean isParameterTokenSupportedForRequest(ServerHttpRequest request) {
        return this.allowUriQueryParameter && HttpMethod.GET.equals(request.getMethod());
    }

    private boolean allowUriQueryParameter = true;

    private String bearerTokenHeaderName = HttpHeaders.AUTHORIZATION;

    private String resolveFromAuthorizationHeader(HttpHeaders headers) {
        String authorization = headers.getFirst(this.bearerTokenHeaderName);
        if (!StringUtils.startsWithIgnoreCase(authorization, "bearer")) {
            return null;
        }
        Matcher matcher = JvsDefaultBearerTokenResolver.AUTHORIZATION_PATTERN.matcher(authorization);
        if (!matcher.matches()) {
            BearerTokenError error = invalidTokenError();
            throw new OAuth2AuthenticationException(error);
        }
        return matcher.group("token");
    }

    private static BearerTokenError invalidTokenError() {
        return BearerTokenErrors.invalidToken("Bearer token is malformed");
    }

    private static String resolveAccessTokenFromRequest(ServerHttpRequest request) {
        List<String> parameterTokens = request.getQueryParams().get("access_token");
        if (CollectionUtils.isEmpty(parameterTokens)) {
            return null;
        }
        if (parameterTokens.size() == 1) {
            return parameterTokens.get(0);
        }

        BearerTokenError error = BearerTokenErrors.invalidRequest("Found multiple bearer tokens in the request");
        throw new OAuth2AuthenticationException(error);

    }


}
