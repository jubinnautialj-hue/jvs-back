package cn.bctools.auth.login.auth.wx.enterprise;

import cn.bctools.auth.component.OtherLoginUserInfoComponent;
import cn.bctools.auth.component.cons.OtherUserDto;
import cn.bctools.auth.entity.User;
import cn.bctools.auth.service.SysConfigsService;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.OtherLoginTypeEnum;
import cn.bctools.common.enums.SysConfigEnterriseWeChat;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanToMapUtils;
import cn.bctools.web.utils.HttpRequestUtils;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.config.AuthDefaultSource;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.utils.UrlBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author zhuxiaokang
 * 企业微信登录通用处理
 */
@Slf4j
@Component
public abstract class BaseWxEnterprise {
    public static final String LOGIN_TYPE = OtherLoginTypeEnum.WX_ENTERPRISE.name();

    /**
     * 企业微信相关属性
     */
    public static final String OPEN_ID = "openid";
    public static final String USER_ID = "userid";
    public static final String ERR_CODE = "errcode";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String CONVERT_TO_OPENID_URL = "https://qyapi.weixin.qq.com/cgi-bin/user/convert_to_openid";

    @Resource
    SysConfigsService sysConfigService;
    @Resource
    OtherLoginUserInfoComponent otherLoginUserInfoComponent;

    public User getUser(String loginType, AuthUser authUser) {
        OtherUserDto otherUserDto = new OtherUserDto()
                .setOpenId(getOpenId(authUser))
                .setUserName(authUser.getUsername())
                .setEmail(authUser.getEmail())
                .setLoginType(loginType)
                .setAvatar(authUser.getAvatar())
                .setOtherUser(BeanToMapUtils.beanToMap(authUser));
        return otherLoginUserInfoComponent.getUser(otherUserDto);
    }

    /**
     * userid转openid
     *
     * @param authUser 授权用户
     * @return openId
     */
    public String getOpenId(AuthUser authUser) {
        return authUser.getUuid();
        /*String openId = Optional.ofNullable(authUser.getRawUserInfo().get(OPEN_ID)).map(String::valueOf).orElse("");
        if (StringUtils.isNotBlank(openId)) {
            return openId;
        }
        // 未返回openid，则用userid转openid
        String url = UrlBuilder.fromBaseUrl(CONVERT_TO_OPENID_URL).queryParam(ACCESS_TOKEN, authUser.getToken().getAccessToken()).build();
        Map<String, Object> param = new HashMap<>();
        param.put(USER_ID, String.valueOf(authUser.getRawUserInfo().get(USER_ID)));
        return getOpenId(url, param);*/
    }

    /**
     * userid转openid
     *
     * @param accessToken
     * @param userId
     */
    public String getOpenId(String accessToken, String userId) {
        return userId;
        /*String url = UrlBuilder.fromBaseUrl(CONVERT_TO_OPENID_URL).queryParam(ACCESS_TOKEN, accessToken).build();
        Map<String, Object> param = new HashMap<>();
        param.put(USER_ID, userId);
        return getOpenId(url, param);*/
    }

    /**
     * userid转openid
     *
     * @param url
     * @param param
     * @return
     */
    private String getOpenId(String url, Map<String, Object> param) {
        JSONObject jsonObject = HttpRequestUtils.postJson(url, param, JSONObject.class, Boolean.FALSE, new HttpHeaders());
        if (ObjectUtil.isNull(jsonObject)) {
            throw new BusinessException("获取openid失败");
        }
        if (jsonObject.containsKey(ERR_CODE) && jsonObject.getIntValue(ERR_CODE) != 0) {
            log.error("企业微信登录获取openid失败：{}", jsonObject);
            throw new BusinessException("获取openid失败");
        }
        return jsonObject.getString(OPEN_ID);
    }

    /**
     * 获取access_token
     *
     * @return
     */
    public String getAccessToken() {
        SysConfigEnterriseWeChat config = sysConfigService.getConfig(ConfigsTypeEnum.ENTERPRISE_WECHAT_APPLICATION_CONFIGURATION);
        if (config.getEnable()) {
            String acessTokenUrl = UrlBuilder.fromBaseUrl(AuthDefaultSource.WECHAT_ENTERPRISE_WEB.accessToken())
                    .queryParam("corpid", config.getAppId())
                    .queryParam("corpsecret", config.getAppSecret())
                    .build();
            JSONObject jsonObject = HttpRequestUtils.getJson(acessTokenUrl, JSONObject.class, Boolean.FALSE, new HttpHeaders());
            if (ObjectUtil.isNull(jsonObject)) {
                throw new BusinessException("获取access_token失败");
            }
            if (jsonObject.containsKey(ERR_CODE) && jsonObject.getIntValue(ERR_CODE) != 0) {
                log.error("企业微信登录获取access_token失败：{}", jsonObject);
                throw new BusinessException("获取access_token失败", jsonObject);
            }
            return jsonObject.getString(ACCESS_TOKEN);
        } else {
            throw new BusinessException("未配置企业微信信息");
        }
    }
}
