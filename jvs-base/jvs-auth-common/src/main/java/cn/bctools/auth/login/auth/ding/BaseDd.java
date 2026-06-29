package cn.bctools.auth.login.auth.ding;

import cn.bctools.auth.component.OtherLoginUserInfoComponent;
import cn.bctools.auth.component.cons.OtherUserDto;
import cn.bctools.auth.entity.User;
import cn.bctools.auth.entity.enums.SysConfigTypeEnum;
import cn.bctools.auth.service.SysConfigsService;
import cn.bctools.common.enums.OtherLoginTypeEnum;
import cn.bctools.common.enums.SysConfigDing;
import cn.bctools.common.exception.BusinessException;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.response.OapiGettokenResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author zhuxiaokang
 * 钉钉登录通用处理
 */
@Slf4j
@Component
public abstract class BaseDd {
    public static final String LOGIN_TYPE = OtherLoginTypeEnum.Ding.name();
    public static SysConfigTypeEnum configType;
    public static final Long DINGTALK_SUCCESS_CODE = 0L;
    /**
     * 钉钉接口QPS限流code
     */
    public static final String QPS_CODE = "90018";
    /**
     * 员工在当前开发者企业账号范围内的唯一标识
     */
    public static final String UNIONID = "unionid";
    /**
     * 姓名
     */
    private static final String NAME = "name";
    /**
     * 手机号
     */
    private static final String MOBILE = "mobile";
    private static final String GET_TOKEN_URL = "https://oapi.dingtalk.com/gettoken";

    @Resource
    SysConfigsService configsService;
    @Resource
    OtherLoginUserInfoComponent otherLoginUserInfoComponent;

    /**
     * 获取AccessToken
     *
     * @param config
     * @return
     */
    @SneakyThrows
    public String getAccessToken(SysConfigDing config) {
        if (config.getEnable()) {
            // 缓存中无accessToken，发起请求获取accessToken
            DingTalkClient client = new DefaultDingTalkClient(GET_TOKEN_URL);
            OapiGettokenRequest req = new OapiGettokenRequest();
            req.setAppkey(config.getAppKey());
            req.setAppsecret(config.getAppSecret());
            req.setHttpMethod(HttpMethod.GET.name());
            OapiGettokenResponse rsp = client.execute(req);
            if (DINGTALK_SUCCESS_CODE.intValue() != rsp.getErrcode().intValue()) {
                log.error("钉钉免登失败。获取access_token失败：{}", rsp.getBody());
                throw new BusinessException("dingtalk_登录失败");
            }
            String token = rsp.getAccessToken();
            if (StringUtils.isBlank(token)) {
                log.error("登录失败，获取accessToken失败");
                throw new BusinessException("dingtalk_登录失败");
            }
            return token;
        } else {
            throw new BusinessException("系统异常");
        }
    }

    /**
     * 获取用户信息
     *
     * @param loginType
     * @param userResp  三方登录返回的用户信息
     * @return 用户信息
     */
    public User getUserInfo(String loginType, Object userResp) {
        JSONObject userRes = JSON.parseObject(JSON.toJSONString(userResp));
        JSONObject userObj = JSON.parseObject(JSON.toJSONString(getLoginUserInfo(userResp)));
        OtherUserDto otherUserDto = new OtherUserDto()
                .setOpenId(userRes.getString(UNIONID))
                .setPhone(userRes.getString(MOBILE))
                .setAvatar(userRes.getString("avatar"))
                .setUserName(userObj.getString(NAME))
                .setLoginType(loginType)
                .setOtherUser(userObj);
        return otherLoginUserInfoComponent.getUser(otherUserDto);
    }

    /**
     * 获取三方登录用户信息
     *
     * @param obj
     * @return
     */
    public abstract Object getLoginUserInfo(Object obj);
}
