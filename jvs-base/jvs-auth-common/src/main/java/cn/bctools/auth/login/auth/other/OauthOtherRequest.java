package cn.bctools.auth.login.auth.other;

import cn.bctools.auth.entity.*;
import cn.bctools.auth.entity.enums.SexTypeEnum;
import cn.bctools.auth.entity.enums.UserTypeEnum;
import cn.bctools.auth.login.LoginHandler;
import cn.bctools.auth.login.dto.SyncUserDto;
import cn.bctools.auth.util.SyncOrgUtils;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.web.utils.HttpRequestUtils;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.xkcoding.http.HttpUtil;
import com.xkcoding.http.support.HttpHeader;
import com.xkcoding.http.support.hutool.HutoolImpl;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.config.AuthSource;
import me.zhyd.oauth.enums.AuthResponseStatus;
import me.zhyd.oauth.exception.AuthException;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthToken;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthDefaultRequest;
import me.zhyd.oauth.utils.AuthChecker;
import me.zhyd.oauth.utils.HttpUtils;
import me.zhyd.oauth.utils.UrlBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author guojing
 */
@Slf4j
@Accessors(chain = true)
@Getter
public class OauthOtherRequest extends AuthDefaultRequest {

    /**
     * 对应的字段映射关系
     */
    private OtherAuthUser field;
    private String scope;
    private OauthOther oauthOther;

    public OauthOtherRequest(OauthOther oauthOther, AuthConfig config, AuthStateCache authStateCache) {


        super(config, new AuthSource() {
            @Override
            public String authorize() {
                return oauthOther.getAuthorize();
            }

            @Override
            public String accessToken() {
                return oauthOther.getAccessToken();
            }

            @Override
            public String userInfo() {
                return oauthOther.getUserInfo();
            }
        });
        this.scope = oauthOther.getScope();
        this.authStateCache = authStateCache;
        this.field = oauthOther.getFiledJson();
        this.oauthOther = oauthOther;
        if (!AuthChecker.isSupportedAuth(config, source)) {
            throw new AuthException(AuthResponseStatus.PARAMETER_INCOMPLETE, source);
        }
        // 校验配置合法性
        AuthChecker.checkConfig(config, source);
    }

    /**
     * 获取应用token
     *
     * @return
     */
    public String getAccessToken() {
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
        param.add("grant_type", "client_credentials");
        param.add("client_id", oauthOther.getClientId());
        param.add("client_secret", oauthOther.getClientSecret());
        com.alibaba.fastjson.JSONObject jsonObject = HttpRequestUtils.postJson(oauthOther.getAccessToken(), param, com.alibaba.fastjson.JSONObject.class, Boolean.FALSE, null);
        log.info("获取三方接口返回为:", jsonObject.toString());
        if (ObjectUtil.isNull(jsonObject)) {
            throw new BusinessException("获取accessToken失败");
        }
        if (!(jsonObject.getIntValue("code") == 1000)) {
            log.error("获取accessToken失败：{}", jsonObject);
            throw new BusinessException("获取accessToken失败");
        }
        return jsonObject.getJSONObject("data").getString("token");
    }

    @Override
    public String authorize(String state) {
        return UrlBuilder.fromBaseUrl(source.authorize())
                .queryParam("response_type", "code")
                .queryParam("client_id", config.getClientId())
                .queryParam("redirect_uri", config.getRedirectUri())
                .queryParam("grant_type", "authorization_code")
                .queryParam("scope", scope)
                .queryParam("state", getRealState(state))
                .build();
    }

    @Override
    protected AuthToken getAccessToken(AuthCallback authCallback) {
        HttpUtil.setHttp(new HutoolImpl());
        String response = null;
        String url = accessTokenUrl(authCallback.getCode());
        log.info("get Token url" + url);
        if (ObjectNull.isNull(oauthOther.getUrlType(), oauthOther.getParameterType())) {
            response = getResponse(url, authCallback);
        } else {
            if ("post".equals(oauthOther.getUrlType()) && "url".equals(oauthOther.getParameterType())) {
                response = doPostAuthorizationCode(authCallback.getCode());
            }
            if ("post".equals(oauthOther.getUrlType()) && "form".equals(oauthOther.getParameterType())) {
                response = getResponse(url, authCallback);
            }
            if ("get".equals(oauthOther.getUrlType())) {
                response = doGetAuthorizationCode(authCallback.getCode());
            }
        }
        log.info("获取的 token 信息为,{}", response);
        JSONObject object = JSONObject.parseObject(response);
        checkResponse(object);
        return AuthToken.builder().accessToken(object.getString("access_token")).refreshToken(object.getString("refresh_token")).idToken(object.getString("id_token")).tokenType(object.getString("token_type")).scope(object.getString("scope")).build();
    }

    private String getResponse(String url, AuthCallback authCallback) {
        try {
            UrlBuilder urlBuilder = UrlBuilder.fromBaseUrl(source.accessToken())
                    .queryParam("code", authCallback.getCode())
                    .queryParam("client_id", config.getClientId())
                    .queryParam("client_secret", config.getClientSecret())
                    .queryParam("redirect_uri", config.getRedirectUri())
                    .queryParam("grant_type", "authorization_code")
                    .queryParam("scope", scope);
            Map<String, Object> readOnlyParams = urlBuilder.getReadOnlyParams();
            HttpRequest body = cn.hutool.http.HttpUtil.createPost(source.accessToken())
                    .header(HttpHeaders.CONTENT_TYPE, ContentType.FORM_URLENCODED.getValue())
                    .form(readOnlyParams);
            log.info("请求路径" + body);
            return body.executeAsync().body();
        } catch (Exception e) {
            log.error("post获取 token 异常 : url : " + url + "\n", e);
            return doGetAuthorizationCode(authCallback.getCode());
        }
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(JSONObject object) {
        String error = "error";
        String errorDescription = "error_description";
        if (object.containsKey(error) || object.containsKey(errorDescription)) {
            throw new AuthException(object.containsKey(error) + ":" + object.getString(errorDescription));
        }
    }

    @Override
    public AuthUser getUserInfo(AuthToken authToken) {
        String userInfo;
        if (oauthOther.getType().contains("oauth2")) {
            HttpHeader httpHeader = new HttpHeader();
            httpHeader.add("Authorization", "Bearer " + authToken.getAccessToken());
            userInfo = new HttpUtils(config.getHttpConfig()).post(userInfoUrl(authToken), null, httpHeader);
        } else {
            userInfo = doGetUserInfo(authToken);
        }
        log.info("登陆其它项目获取用户信息: {}", userInfo);
        JSONObject object = JSONObject.parseObject(userInfo);
        this.checkResponse(object);
        return getUser(object, authToken);
    }

    public OtherAuthUser getUser(JSONObject object, AuthToken authToken) {
        log.info("映射字段关系:{}", JSONObject.toJSONString(field));
        String string = object.getString(field.getUuid());
        if (ObjectNull.isNull(string)) {
            throw new BusinessException("获取三方用户对象失败,未获取到用户id,三方返回信息为:{}", JSONObject.toJSONString(object) + " \n 示例结构: {\n" +
                    "\"uuid\":\"xx\",\n" +
                    "\"name\":\"张三\"\n" +
                    "}");
        }
        SexTypeEnum sexTypeEnum = null;
        try {
            sexTypeEnum = SexTypeEnum.getByDesc(object.getString(field.getGender().getDesc()));
        } catch (Exception e) {
            log.error("获取性别异常");
        }
        OtherAuthUser otherAuthUser = new OtherAuthUser();
        otherAuthUser.setRawUserInfo(com.alibaba.fastjson.JSONObject.parseObject(JSON.toJSONString(object)));
        otherAuthUser.setUuid(object.getString(field.getUuid()));
        otherAuthUser.setUsername(object.getString(field.getUsername()));
        otherAuthUser.setNickname(object.getString(field.getNickname()));
        String headImg = object.getString(field.getAvatar());
        String avatar = LoginHandler.getDurableAvatar(otherAuthUser.getNickname(), headImg);
        otherAuthUser.setAvatar(avatar);
        try {
            otherAuthUser.setEnable(object.getBoolean(field.getEnable().toString()));
        } catch (Exception e) {

        }
        otherAuthUser.setBlog(object.getString(field.getBlog()));
        otherAuthUser.setCompany(JSONObject.toJSONString(object.get(field.getCompany())));
        otherAuthUser.setLocation(object.getString(field.getLocation()));
        otherAuthUser.setEmail(object.getString(field.getEmail()));
        otherAuthUser.setRemark(object.getString(field.getRemark()));
        otherAuthUser.setSex(sexTypeEnum);
        otherAuthUser.setToken(authToken);
        otherAuthUser.setSource(oauthOther.getType());
        otherAuthUser.setAccount(object.getString(field.getAccount()));
        otherAuthUser.setDeptIds(object.get(field.getDeptIds()));
        otherAuthUser.setPhone(object.getString(field.getPhone()));
        return otherAuthUser;
    }

    /**
     * 根据 token 获取 部门
     *
     * @param accessToken
     * @return
     */
    public List<? extends Dept> getDept(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject jsonObject = HttpRequestUtils.getJson(this.oauthOther.getDeptUrl(), JSONObject.class, Boolean.FALSE, headers);
        log.info("三方返回的数据{}", JSONObject.toJSONString(jsonObject));
        if (ObjectUtil.isNull(jsonObject)) {
            throw new BusinessException("拉取用户失败");
        }
        if (!(jsonObject.getIntValue("code") == 1000)) {
            log.error("拉取用户失败：{}", jsonObject);
            throw new BusinessException("拉取部门失败");
        }
        JSONArray objects = jsonObject.getJSONArray("data");
        List<? extends Dept> remoteDepts = objects.stream().map(d -> {
            JSONObject dept = (JSONObject) (d);
            String getParentId = dept.getString(oauthOther.getDeptJson().getParentId());
            String parentId = ObjectNull.isNull(getParentId) ? TenantContextHolder.getTenantId() : getParentId;
            return new Dept()
                    .setId(dept.getString(dept.getString(oauthOther.getDeptJson().getId())))
                    .setName(dept.getString(dept.getString(oauthOther.getDeptJson().getName())))
                    .setParentId(parentId)
                    .setSource(oauthOther.getType());
        }).collect(Collectors.toList());
        return remoteDepts;
    }

    public void getUserList(Map<String, Dept> deptMap, SyncUserDto syncUserDto, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject jsonObject = HttpRequestUtils.getJson(this.oauthOther.getPullUserUrlMethod(), JSONObject.class, Boolean.FALSE, headers);
        log.info("三方返回的数据{}", JSONObject.toJSONString(jsonObject));
        if (ObjectUtil.isNull(jsonObject)) {
            throw new BusinessException("拉取用户失败");
        }
        if (!(jsonObject.getIntValue("code") == 1000)) {
            log.error("拉取用户失败：{}", jsonObject);
            throw new BusinessException("拉取用户失败");
        }
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        List<User> users = new ArrayList<>();
        jsonArray.forEach(u -> {
            JSONObject resUser = (JSONObject) com.alibaba.fastjson.JSONObject.toJSON(u);
            OtherAuthUser otherAuthUser = getUser(resUser, null);
            log.info("组装后的数据结构{}", JSONObject.toJSONString(otherAuthUser));
            Dept dept = null;
            if (CollectionUtils.isNotEmpty(otherAuthUser.getDeptIds())) {
                dept = deptMap.get(otherAuthUser.getDeptIds().get(0));
            }
            String savePhone = otherAuthUser.getPhone();
            User user = new User()
                    .setId(otherAuthUser.getUuid())
                    .setRealName(otherAuthUser.getNickname())
                    .setAccountName(otherAuthUser.getAccount())
                    //这里不创建帐号
                    .setPhone(savePhone)
                    .setEmail(otherAuthUser.getEmail())
                    .setHeadImg(otherAuthUser.getAvatar())
                    .setSex(otherAuthUser.getSex())
                    .setCancelFlag(false)
                    .setUserType(UserTypeEnum.OTHER_USER);
            UserTenant userTenant = new UserTenant()
                    .setUserId(user.getId())
                    .setRealName(otherAuthUser.getNickname())
                    .setPhone(savePhone)
                    .setCancelFlag(false);
            if (ObjectNull.isNotNull(dept)) {
                List<String> userDeptIdsList = Optional.ofNullable(userTenant.getDeptId()).orElseGet(ArrayList::new);
                userDeptIdsList.add(SyncOrgUtils.buildDeptId(TenantContextHolder.getTenantId(), dept.getId()));
                userTenant.setDeptId(userDeptIdsList);
            }
            UserExtension userExtension = new UserExtension()
                    .setExtension(BeanUtil.beanToMap(resUser))
                    .setOpenId(otherAuthUser.getUuid())
                    .setNickname(otherAuthUser.getNickname())
                    .setType(otherAuthUser.getSource())
                    .setUserId(user.getId());
            syncUserDto.getUsers().add(user);
            syncUserDto.getUserTenants().add(userTenant);
            syncUserDto.getUserExtensions().add(userExtension);
        });
    }
}
