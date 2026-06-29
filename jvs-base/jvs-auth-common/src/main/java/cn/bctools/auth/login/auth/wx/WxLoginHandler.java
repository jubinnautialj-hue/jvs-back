package cn.bctools.auth.login.auth.wx;

import cn.bctools.auth.component.OtherLoginUserInfoComponent;
import cn.bctools.auth.component.cons.OtherUserDto;
import cn.bctools.auth.entity.User;
import cn.bctools.auth.login.AuthRequestCustomFactory;
import cn.bctools.auth.login.LoginHandler;
import cn.bctools.common.enums.OtherLoginTypeEnum;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanToMapUtils;
import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.config.AuthDefaultSource;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthWeChatOpenRequest;
import org.springframework.stereotype.Component;

/**
 * @author Administrator
 */
@Slf4j
@AllArgsConstructor
@Component("WECHAT_OPEN")
public class WxLoginHandler implements LoginHandler<AuthCallback> {
    AuthRequestCustomFactory factory;
    OtherLoginUserInfoComponent otherLoginUserInfoComponent;

    @Override
    public User handle(String code, String appId, AuthCallback callback) {
        AuthWeChatOpenRequest authRequest = (AuthWeChatOpenRequest) factory.get(AuthDefaultSource.WECHAT_OPEN.name());
        AuthResponse response = authRequest.login(callback);
        if (!response.ok()) {
            log.error("获取微信信息错误: {}", response.getMsg());
            throw new BusinessException("获取微信信息错误");
        }
        log.info("[login] 获取微信信息: {}", JSONUtil.toJsonStr(response));
        AuthUser authUser = (AuthUser) response.getData();
        OtherUserDto otherUserDto = new OtherUserDto()
                .setOpenId(authUser.getToken().getOpenId())
                .setUserName(authUser.getNickname())
                .setAvatar(authUser.getAvatar())
                .setLoginType(OtherLoginTypeEnum.WECHAT_OPEN.name())
                .setOtherUser(BeanToMapUtils.beanToMap(authUser));
        return otherLoginUserInfoComponent.getUser(otherUserDto);
    }

    @Override
    public void bind(User user, String code, String appId) {
        /*String decodedPassword = PasswordUtil.decodedPassword(code, appId);
        AuthCallback callback = JSONObject.parseObject(decodedPassword, AuthCallback.class);
        AuthWeChatOpenRequest authRequest = (AuthWeChatOpenRequest) factory.get(AuthDefaultSource.WECHAT_OPEN.name());
        AuthResponse response = authRequest.login(callback);
        log.info("[bind] 获取微信信息: {}", JSONUtil.toJsonStr(response));
        AuthUser authUser = (AuthUser) response.getData();
        String nickname = authUser.getNickname();
        String openId = authUser.getToken().getOpenId();
        // 微信头像持久化存储
        String avatar = getDurableAvatar(nickname, authUser.getAvatar());
        user.setHeadImg(avatar);
        UserExtension extension = userExtensionService.getOne(Wrappers.query(new UserExtension().setType(AuthDefaultSource.WECHAT_OPEN.getName()).setOpenId(openId)));
        // 判断是否重复绑定
        if (ObjectUtil.isNotEmpty(extension)) {
            throw new BusinessException("微信已绑定其它帐号");
        }
        // 绑定用户关键信息
        extension = new UserExtension()
                .setOpenId(openId)
                .setNickname(nickname)
                .setUserId(user.getId())
                .setType(AuthDefaultSource.WECHAT_OPEN.name())
                .setExtension(JSONObject.parseObject(JSONObject.toJSONString(authUser)));
        userService.updateById(user);
        userExtensionService.save(extension);*/
    }
}
