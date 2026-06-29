package cn.bctools.auth.login.auth.ding;

import cn.bctools.auth.component.cons.OtherUserDto;
import cn.bctools.auth.entity.User;
import cn.bctools.auth.entity.enums.SysConfigTypeEnum;
import cn.bctools.auth.login.LoginHandler;
import cn.bctools.auth.login.dto.DingtalkDto;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.OtherLoginTypeEnum;
import cn.bctools.common.enums.SysConfigDing;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.PasswordUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONObject;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiSnsGetuserinfoBycodeRequest;
import com.dingtalk.api.request.OapiUserGetbyunionidRequest;
import com.dingtalk.api.request.OapiV2UserGetRequest;
import com.dingtalk.api.response.OapiSnsGetuserinfoBycodeResponse;
import com.dingtalk.api.response.OapiUserGetbyunionidResponse;
import com.dingtalk.api.response.OapiV2UserGetResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author zhuxiaokang
 * 钉钉扫码登录
 */
@Slf4j
@Component("DINGTALK_SCAN")
public class DdScanLoginHandler extends BaseDd implements LoginHandler<DingtalkDto> {
    private static final String GET_USER_INFO_BY_CODE_URL = "https://oapi.dingtalk.com/sns/getuserinfo_bycode";
    private static final String GET_USER_ID_BY_UNIONID_URL = "https://oapi.dingtalk.com/topapi/user/getbyunionid";
    private static final String GET_USER_INFO_BY_ID_URL = " https://oapi.dingtalk.com/topapi/v2/user/get";

    static {
        configType = SysConfigTypeEnum.DING_H5;
    }

    @Override
    public User handle(String code, String appId, DingtalkDto dto) {
//        if (StringUtils.isBlank(AuthConfigUtil.appId(configType)) || StringUtils.isBlank(AuthConfigUtil.agentId(configType)) ||
//                StringUtils.isBlank(AuthConfigUtil.secret(configType)) || StringUtils.isBlank(AuthConfigUtil.redirectUri(configType))) {
//            throw new BusinessException("请完善授权配置");
//        }
        SysConfigDing config = configsService.getConfig(ConfigsTypeEnum.NAIL_APPLICATION_CONFIGURATION);
        config.setAppKey(config.getAppId());
        OapiSnsGetuserinfoBycodeResponse.UserInfo userInfo = getUserInfoByCode(dto.getCode(), config);
        String accessToken = getAccessToken(config);
        String userId = getUserIdByUnionid(userInfo.getUnionid(), accessToken);
        OapiV2UserGetResponse.UserGetResponse userResp = getUserInfoByUserId(userId, accessToken);
        if (userResp == null) {
            throw new BusinessException("同步ldap失败");
        }
        return getUserInfo(LOGIN_TYPE, userResp);
    }

    @SneakyThrows
    private OapiSnsGetuserinfoBycodeResponse.UserInfo getUserInfoByCode(String code, SysConfigDing config) {
        DingTalkClient client = new DefaultDingTalkClient(GET_USER_INFO_BY_CODE_URL);
        OapiSnsGetuserinfoBycodeRequest req = new OapiSnsGetuserinfoBycodeRequest();
        req.setTmpAuthCode(code);
        OapiSnsGetuserinfoBycodeResponse rsp = client.execute(req, config.getAppKey(), config.getAppSecret());
        if (DINGTALK_SUCCESS_CODE.intValue() != rsp.getErrcode().intValue()) {
            log.error("钉钉扫码登录失败。根据临时授权码获取用户信息失败：{}", rsp.getBody());
            throw new BusinessException("dingtalk_登录失败");
        }
        return rsp.getUserInfo();
    }

    @SneakyThrows
    private String getUserIdByUnionid(String unionid, String accessToken) {
        DingTalkClient client = new DefaultDingTalkClient(GET_USER_ID_BY_UNIONID_URL);
        OapiUserGetbyunionidRequest req = new OapiUserGetbyunionidRequest();
        req.setUnionid(unionid);
        OapiUserGetbyunionidResponse rsp = client.execute(req, accessToken);
        if (DINGTALK_SUCCESS_CODE.intValue() != rsp.getErrcode().intValue()) {
            log.error("钉钉扫码登录失败。根据unionid获取用户信息失败：{}", rsp.getBody());
            throw new BusinessException("用户不在组织内");
        }
        return rsp.getResult().getUserid();
    }

    @SneakyThrows
    private OapiV2UserGetResponse.UserGetResponse getUserInfoByUserId(String userId, String accessToken) {
        DingTalkClient client = new DefaultDingTalkClient(GET_USER_INFO_BY_ID_URL);
        OapiV2UserGetRequest req = new OapiV2UserGetRequest();
        req.setUserid(userId);
        req.setLanguage("zh_CN");
        OapiV2UserGetResponse rsp = client.execute(req, accessToken);
        if (DINGTALK_SUCCESS_CODE.intValue() != rsp.getErrcode().intValue()) {
            log.error("钉钉扫码登录失败。根据用户id获取用户信息失败：{}", rsp.getBody());
            throw new BusinessException("用户不在组织内");
        }
        return rsp.getResult();
    }

    @Override
    public void bind(User user, String code, String appId) {
        String decodedPassword = PasswordUtil.decodedPassword(code, appId);
        DingtalkDto dto = JSONObject.parseObject(decodedPassword, DingtalkDto.class);
        SysConfigDing config = configsService.getConfig(ConfigsTypeEnum.NAIL_APPLICATION_CONFIGURATION);
        config.setAppKey(config.getAppId());
        OapiSnsGetuserinfoBycodeResponse.UserInfo authUser = getUserInfoByCode(dto.getCode(), config);
        String accessToken = getAccessToken(config);
        String userId = getUserIdByUnionid(authUser.getUnionid(), accessToken);
        OapiV2UserGetResponse.UserGetResponse userResp = getUserInfoByUserId(userId, accessToken);
        if (userResp == null) {
            throw new BusinessException("用户不在组织内");
        }
        log.info("[bind] 获取钉钉用户信息: {}", JSONUtil.toJsonStr(userResp));
        OtherUserDto otherUserDto = new OtherUserDto()
                .setOpenId(userResp.getUnionid())
                .setUserName(userResp.getName())
                .setLoginType(LOGIN_TYPE)
                .setOtherUser(JSONObject.parseObject(JSONObject.toJSONString(userResp)));
        otherLoginUserInfoComponent.bind(OtherLoginTypeEnum.Ding, user, otherUserDto);
    }

    @Override
    public Object getLoginUserInfo(Object userObj) {
        return userObj;
    }
}
