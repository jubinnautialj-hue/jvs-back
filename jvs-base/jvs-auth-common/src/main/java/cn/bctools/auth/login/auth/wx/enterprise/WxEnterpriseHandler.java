package cn.bctools.auth.login.auth.wx.enterprise;

import cn.bctools.auth.component.cons.OtherUserDto;
import cn.bctools.auth.entity.User;
import cn.bctools.auth.login.AuthRequestCustomFactory;
import cn.bctools.auth.login.LoginHandler;
import cn.bctools.auth.login.enums.LoginTypeEnum;
import cn.bctools.common.enums.OtherLoginTypeEnum;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanToMapUtils;
import cn.bctools.common.utils.PasswordUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthWeChatEnterpriseQrcodeRequest;
import org.springframework.stereotype.Component;

/**
 * @author zhuxiaokang
 * 企业微信扫码登录
 */
@Slf4j
@AllArgsConstructor
@Component("WECHAT_ENTERPRISE")
public class WxEnterpriseHandler extends BaseWxEnterprise implements LoginHandler<AuthCallback> {
    AuthRequestCustomFactory factory;

    @Override
    public User handle(String code, String appId, AuthCallback callback) {
        AuthWeChatEnterpriseQrcodeRequest authRequest = (AuthWeChatEnterpriseQrcodeRequest) factory.get(LoginTypeEnum.WECHAT_ENTERPRISE.getValue());
        AuthResponse response = authRequest.login(callback);
        if (!response.ok()) {
            throw new BusinessException("企业微信扫码登录失败", response.getMsg());
        }
        log.info("[login] 获取企业微信登录信息: {}", JSONUtil.toJsonStr(response));
        AuthUser authUser = (AuthUser) response.getData();
        return getUser(LOGIN_TYPE, authUser);
    }

    @Override
    public void bind(User user, String code, String appId) {
        String decodedPassword = PasswordUtil.decodedPassword(code, appId);
        AuthCallback callback = JSONObject.parseObject(decodedPassword, AuthCallback.class);
        AuthWeChatEnterpriseQrcodeRequest authRequest = (AuthWeChatEnterpriseQrcodeRequest) factory.get(LoginTypeEnum.WECHAT_ENTERPRISE.getValue());
        AuthResponse response = authRequest.login(callback);
        log.info("[bind] 获取企业微信信息: {}", JSONUtil.toJsonStr(response));
        AuthUser authUser = (AuthUser) response.getData();
        String nickname = authUser.getUsername();
        String openId = getOpenId(authUser);
        // 企业微信用户头像持久化存储
        String avatar = LoginHandler.getDurableAvatar(nickname, authUser.getAvatar());
        user.setHeadImg(avatar);
        user.setRealName(nickname);
        OtherUserDto otherUserDto = new OtherUserDto()
                .setOpenId(openId)
                .setUserName(nickname)
                .setLoginType(LOGIN_TYPE)
                .setOtherUser(BeanToMapUtils.beanToMap(authUser));
        otherLoginUserInfoComponent.bind(OtherLoginTypeEnum.WX_ENTERPRISE, user, otherUserDto);
    }

}
