package cn.bctools.auth.login.auth.wx;

import cn.bctools.auth.component.OtherLoginUserInfoComponent;
import cn.bctools.auth.component.cons.OtherUserDto;
import cn.bctools.auth.entity.User;
import cn.bctools.auth.entity.UserExtension;
import cn.bctools.auth.login.LoginHandler;
import cn.bctools.auth.login.dto.WxAppDto;
import cn.bctools.auth.service.UserExtensionService;
import cn.bctools.auth.service.UserService;
import cn.bctools.common.enums.OtherLoginTypeEnum;
import cn.bctools.common.utils.BeanToMapUtils;
import cn.bctools.common.utils.PasswordUtil;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.database.util.SqlFunctionUtil;
import cn.bctools.redis.utils.RedisUtils;
import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.WxMaUserService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 注册登录
 *
 * @author Administrator
 */
@Slf4j
@AllArgsConstructor
@Component("wxapp")
public class WxAppLoginHandler implements LoginHandler<WxAppDto> {
    UserService userService;
    RedisUtils redisUtils;
    UserExtensionService userExtensionService;
    OtherLoginUserInfoComponent otherLoginUserInfoComponent;

    final static String WXAPP_SESSION_KEY = "wxapp:sessionKey:";

    @SneakyThrows
    @Override
    public User handle(String code, String appId, WxAppDto wxAppDto) {
        WxMaUserService wxMaUserService = SpringContextUtil.getBean(WxMaService.class).getUserService();
        WxMaJscode2SessionResult userInfo = wxMaUserService.getSessionInfo(wxAppDto.getCode());
        String sessionKey = userInfo.getSessionKey();

        List<UserExtension> userExtensions = userExtensionService.list(Wrappers.<UserExtension>lambdaQuery()
                .in(UserExtension::getType, Arrays.asList(OtherLoginTypeEnum.WECHAT_MP.name(), OtherLoginTypeEnum.wxapp.name()))
                .and(w -> w.apply(SqlFunctionUtil.jsonExtract("extension", "unionId") + "={0}", userInfo.getUnionid())
                        .or(wr -> wr.apply(SqlFunctionUtil.jsonExtract("extension", "unionid") + "={0}", userInfo.getUnionid()))));
        if (CollectionUtils.isNotEmpty(userExtensions)) {
            return getUser(userExtensions, userInfo);
        }
        // 获取用户
        OtherUserDto otherUserDto = new OtherUserDto()
                .setOpenId(userInfo.getOpenid())
                .setLoginType(OtherLoginTypeEnum.wxapp.name())
                .setOtherUser(BeanToMapUtils.beanToMap(userInfo));
        User user = otherLoginUserInfoComponent.getUser(otherUserDto);
        //设置为10秒内
        redisUtils.set(WXAPP_SESSION_KEY + user.getId(), sessionKey, Long.valueOf(10));
        return user;
    }

    /**
     * 获取用户
     * @param userExtensions
     * @param userInfo
     * @return
     */
    private User getUser(List<UserExtension> userExtensions, WxMaJscode2SessionResult userInfo) {
        String userId;
        // 只存在公众号登录信息，则以公众号绑定的用户id，绑定小程序
        if (userExtensions.size() == 1 && OtherLoginTypeEnum.WECHAT_MP.name().equals(userExtensions.get(0).getType())) {
            userId = userExtensions.get(0).getUserId();
            UserExtension extension = new UserExtension()
                    .setOpenId(userInfo.getOpenid())
                    .setUserId(userId)
                    .setType(OtherLoginTypeEnum.wxapp.name())
                    .setExtension(BeanToMapUtils.beanToMap(userInfo));
            userExtensionService.save(extension);
        } else {
            userId = userExtensions.get(0).getUserId();
        }
        //设置为10秒内
        redisUtils.set(WXAPP_SESSION_KEY + userId, userInfo.getSessionKey(), Long.valueOf(10));
        return userService.getById(userId);
    }

    @Override
    public void bind(User user, String code, String appId) {
        String decodedPassword = PasswordUtil.decodedPassword(code, appId);
        WxAppDto callback = JSONObject.parseObject(decodedPassword, WxAppDto.class);
        WxMaUserService wxMaUserService = SpringContextUtil.getBean(WxMaService.class).getUserService();
        String sessionKey = WXAPP_SESSION_KEY + user.getId();
        if (!redisUtils.exists(sessionKey)) {
            return;
        }
        //从redis获取用户key  通过key调用获取用户信息
        sessionKey = (String) redisUtils.get(sessionKey);
        WxMaUserInfo userInfo = wxMaUserService.getUserInfo(sessionKey, callback.getEncryptedData(), callback.getIvStr());
        user.setRealName(userInfo.getNickName());
        // 微信头像持久化存储
        String avatar = LoginHandler.getDurableAvatar(userInfo.getNickName(), userInfo.getAvatarUrl());
        UserExtension userExtension = userExtensionService.getOne(Wrappers.lambdaQuery(new UserExtension().setType(OtherLoginTypeEnum.wxapp.name()).setOpenId(userInfo.getUnionId())));
        userExtension.setNickname(userInfo.getNickName());
        //设置扩展字段
        userExtension.setExtension(JSONObject.parseObject(JSONObject.toJSONString(userInfo)));
        userExtensionService.updateById(userExtension);
        user.setRealName(userInfo.getNickName()).setHeadImg(avatar);
        userService.updateById(user);
    }


}
