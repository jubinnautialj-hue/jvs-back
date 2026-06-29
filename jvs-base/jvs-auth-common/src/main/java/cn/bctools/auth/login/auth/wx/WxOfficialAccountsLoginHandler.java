package cn.bctools.auth.login.auth.wx;

import cn.bctools.auth.component.OtherLoginUserInfoComponent;
import cn.bctools.auth.component.cons.OtherUserDto;
import cn.bctools.auth.entity.User;
import cn.bctools.auth.entity.UserExtension;
import cn.bctools.auth.login.LoginHandler;
import cn.bctools.auth.login.dto.WxOfficialAccountsDto;
import cn.bctools.auth.service.UserExtensionService;
import cn.bctools.auth.service.UserService;
import cn.bctools.auth.util.AvatarUtils;
import cn.bctools.common.enums.OtherLoginTypeEnum;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.PasswordUtil;
import cn.bctools.database.util.IdGenerator;
import cn.bctools.database.util.SqlFunctionUtil;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.template.OssTemplate;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

/**
 * [description]：公众号扫码关注登录
 *
 * @author Administrator
 */
@Slf4j
@AllArgsConstructor
@Component("WECHAT_MP")
public class WxOfficialAccountsLoginHandler implements LoginHandler<WxOfficialAccountsDto> {
    private static final String LOGIN_TYPE = OtherLoginTypeEnum.WECHAT_MP.name();
    private final static String OPEN_ID = "openId";
    private final static String UNION_ID = "unionId";
    private final static String WX_USER_NAME = "用户";
    private final UserService userService;
    private final RedisUtils redisUtils;
    private final OssTemplate ossTemplate;
    private final UserExtensionService userExtensionService;
    private final OtherLoginUserInfoComponent otherLoginUserInfoComponent;

    @Override
    public User handle(String code, String appId, WxOfficialAccountsDto wxOfficialAccountsDto) {
        Object o = redisUtils.get(wxOfficialAccountsDto.getId());
        if (ObjectUtils.isNotEmpty(o)) {
            JSONObject jsonObject = JSONObject.parseObject((String) o);
            String openId = jsonObject.getString(OPEN_ID);
            String unionId = jsonObject.getString(UNION_ID);
            List<UserExtension> userExtensions = null;
            if (StringUtils.isNotBlank(unionId)) {
                userExtensions = userExtensionService.list(Wrappers.<UserExtension>lambdaQuery()
                        .in(UserExtension::getType, Arrays.asList(OtherLoginTypeEnum.WECHAT_MP.name(), OtherLoginTypeEnum.wxapp.name()))
                        .and(w -> w.apply(SqlFunctionUtil.jsonExtract("extension", "unionId") + "={0}", unionId)
                                .or(wr -> wr.apply(SqlFunctionUtil.jsonExtract("extension", "unionid") + "={0}", unionId))));
            } else {
                userExtensions = userExtensionService.list(Wrappers.<UserExtension>lambdaQuery()
                        .in(UserExtension::getType, Arrays.asList(OtherLoginTypeEnum.WECHAT_MP.name(), OtherLoginTypeEnum.wxapp.name()))
                        .eq(UserExtension::getOpenId, openId));
            }
            User user;
            //判断用户是否存在
            if (CollectionUtils.isEmpty(userExtensions)) {
                OtherUserDto otherUserDto = new OtherUserDto()
                        .setOpenId(openId)
                        .setUserName(WX_USER_NAME + IdGenerator.getIdStr(36).substring(5).toLowerCase())
                        .setLoginType(LOGIN_TYPE)
                        .setOtherUser(jsonObject);
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                String wx = "微信";
                AvatarUtils.generateImg(wx, output);
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(output.toByteArray());
                BaseFile baseFile = ossTemplate.putFile("jvs-public", wx + ".jpg", byteArrayInputStream, "headImg");
                String link = ossTemplate.fileJvsPublicLink(baseFile.getFileName());
                //头像可以使用公共桶
                otherUserDto.setAvatar(link);
                user = otherLoginUserInfoComponent.getUser(otherUserDto);
            } else {
                user = getUser(userExtensions, openId, jsonObject);
            }
            redisUtils.del(wxOfficialAccountsDto.getId());
            return user;
        }
        throw new BusinessException("用户不存在");
    }

    /**
     * 获取用户
     *
     * @param userExtensions
     * @param openId
     * @param jsonObject
     * @return
     */
    private User getUser(List<UserExtension> userExtensions, String openId, JSONObject jsonObject) {
        String userId;
        // 只存在小程序登录信息，则以小程序绑定的用户id，绑定公众号
        if (userExtensions.size() == 1 && OtherLoginTypeEnum.wxapp.name().equals(userExtensions.get(0).getType())) {
            userId = userExtensions.get(0).getUserId();
            UserExtension extension = new UserExtension()
                    .setOpenId(openId)
                    .setNickname(WX_USER_NAME + IdGenerator.getIdStr(36).substring(5).toUpperCase())
                    .setUserId(userId)
                    .setType(LOGIN_TYPE)
                    .setExtension(jsonObject);
            userExtensionService.save(extension);
        } else {
            userId = userExtensions.get(0).getUserId();
        }
        return userService.getById(userId);
    }

    @Override
    public void bind(User user, String code, String appId) {
        String decodedPassword = PasswordUtil.decodedPassword(code, appId);
        WxOfficialAccountsDto dto = JSONObject.parseObject(decodedPassword, WxOfficialAccountsDto.class);
        Object o = redisUtils.get(dto.getId());
        if (ObjectUtils.isNotEmpty(o)) {
            // 绑定公众号
            JSONObject jsonObject = JSONObject.parseObject((String) o);
            String openId = jsonObject.getString(OPEN_ID);
            OtherUserDto otherUserDto = new OtherUserDto()
                    .setOpenId(openId)
                    .setUserName(WX_USER_NAME + IdGenerator.getIdStr(36).substring(5).toUpperCase())
                    .setLoginType(LOGIN_TYPE)
                    .setOtherUser(jsonObject);
            otherLoginUserInfoComponent.bind(OtherLoginTypeEnum.WECHAT_MP, user, otherUserDto);

            // 绑定小程序登录的账号
            String unionId = jsonObject.getString(UNION_ID);
            UserExtension appUserExtension = userExtensionService.getOne(Wrappers.<UserExtension>lambdaQuery()
                    .eq(UserExtension::getType, OtherLoginTypeEnum.wxapp.name())
                    .apply(SqlFunctionUtil.jsonExtract("extension", "unionid") + "={0}", unionId));
            if (ObjectUtil.isNotNull(appUserExtension)) {
                appUserExtension.setUserId(user.getId());
                userExtensionService.updateById(appUserExtension);
            }
            redisUtils.del(dto.getId());
            return;
        }
        throw new BusinessException("绑定失败");
    }

}
