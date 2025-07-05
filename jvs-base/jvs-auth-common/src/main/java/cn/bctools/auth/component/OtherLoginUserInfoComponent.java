package cn.bctools.auth.component;

import cn.bctools.auth.component.cons.OtherUserDto;
import cn.bctools.auth.config.OtherLoginConfig;
import cn.bctools.auth.entity.User;
import cn.bctools.auth.entity.UserExtension;
import cn.bctools.auth.entity.UserTenant;
import cn.bctools.auth.entity.enums.UserTypeEnum;
import cn.bctools.auth.login.LoginHandler;
import cn.bctools.auth.service.UserExtensionService;
import cn.bctools.auth.service.UserService;
import cn.bctools.auth.service.UserTenantService;
import cn.bctools.common.enums.OtherLoginTypeEnum;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.database.util.IdGenerator;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @author zhuxiaokang
 * 扩展的登录方的用户信息
 */
@Slf4j
@Component
@AllArgsConstructor
public class OtherLoginUserInfoComponent {
    private final UserService userService;
    private final UserTenantService userTenantService;
    private final UserRoleComponent userRoleComponent;
    private final UserExtensionService userExtensionService;
    private final OtherLoginConfig otherLoginConfig;

    @Transactional(rollbackFor = Exception.class)
    public User getUser(OtherUserDto otherUser) {
        if (StringUtils.isBlank(otherUser.getLoginType()) || StringUtils.isBlank(otherUser.getOpenId())) {
            throw new BusinessException("登录失败", "openId or loginType error");
        }
        // 获取用户信息
        UserExtension extension = userExtensionService.getOne(Wrappers.<UserExtension>lambdaQuery()
                .eq(UserExtension::getType, otherUser.getLoginType())
                .eq(UserExtension::getOpenId, otherUser.getOpenId()));
        // 新用户自动注册
        return registerUser(otherUser, extension);
    }

    /**
     * 校验是否可自动注册
     */
    private void checkAuthRegisterUser() {
        if (Boolean.FALSE.equals(otherLoginConfig.getEnableAutomaticRegister())) {
            throw new BusinessException("用户名或密码错误");
        }
    }

    /**
     * 注册新用户
     *
     * @param otherUser
     * @param extension
     * @return 自动注册后的用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public User registerUser(OtherUserDto otherUser, UserExtension extension) {
        //判断用户是否存在 ,如果存在 ,直接更新 否则注册一个新的帐号
        User user = null;
        if (ObjectNull.isNotNull(otherUser.getAccountName())) {
            user = userService.getOne(Wrappers.query(new User().setAccountName(otherUser.getAccountName())));
        }
        if (ObjectNull.isNotNull(extension)) {
            user = userService.getById(extension.getUserId());
        }
        if (ObjectNull.isNotNull(user)) {
            if (ObjectNull.isNull(extension)) {
                //创建扩展信息
                UserExtension userExtension = new UserExtension()
                        .setType(otherUser.getLoginType())
                        .setUserId(user.getId())
                        .setOpenId(otherUser.getOpenId())
                        .setNickname(user.getRealName())
                        .setExtension(otherUser.getOtherUser());
                //添加扩展参数
                user.setExtension(otherUser.getOtherUser());
                //绑定三方信息
                userExtensionService.save(userExtension);
                extension = userExtension;
            }
            //更新
            user.setHeadImg(otherUser.getAvatar())
                    .setPhone(otherUser.getPhone())
                    .setRealName(otherUser.getUserName())
                    .setEmail(otherUser.getEmail())
                    .setCancelFlag(false)
                    .setUserType(UserTypeEnum.OTHER_USER);
            userService.updateById(user);
            UserTenant one = userTenantService.getOne(Wrappers.query(new UserTenant().setUserId(user.getId())));
            if (ObjectNull.isNotNull(one)) {
                if (ObjectNull.isNotNull(one.getDeptId())) {
                    if (ObjectNull.isNotNull(otherUser.getDeptId())) {
                        if (JSONUtil.isTypeJSON(otherUser.getDeptId())) {
                            one.getDeptId().addAll(JSONArray.parseArray(otherUser.getDeptId(), String.class));
                        } else {
                            one.getDeptId().add(otherUser.getDeptId());
                        }
                    }
                }
                one.setId(one.getId())
                        .setPhone(otherUser.getPhone())
                        .setUserId(user.getId())
                        .setRealName(user.getRealName())
                        .setCancelFlag(false);
                userTenantService.updateById(one);
                // 默认为游客角色
                userRoleComponent.grandDefaultSysRole(user.getId());
                return user;
            } else {
                UserTenant userTenant = new UserTenant()
                        .setPhone(otherUser.getPhone())
                        .setUserId(user.getId())
                        .setRealName(user.getRealName())
                        .setCancelFlag(false);
                userTenantService.save(userTenant);
                // 默认为游客角色
                userRoleComponent.grandDefaultSysRole(user.getId());
                return user;
            }
        } else {
            // 校验是否可自动注册
            checkAuthRegisterUser();
            // 装配默认值
            assembly(otherUser);
            // 用户
            user = new User()
                    .setPhone(otherUser.getPhone())
                    .setHeadImg(otherUser.getAvatar())
                    .setRealName(otherUser.getUserName())
                    .setAccountName(otherUser.getAccountName())
                    .setEmail(otherUser.getEmail())
                    .setCancelFlag(false)
                    .setUserType(UserTypeEnum.OTHER_USER);
            // 用户租户
            UserTenant userTenant = new UserTenant()
                    .setRealName(user.getRealName())
                    .setCancelFlag(false);
            userService.saveUser(user, userTenant);
        }

        UserExtension userExtension = new UserExtension()
                .setType(otherUser.getLoginType())
                .setUserId(user.getId())
                .setOpenId(otherUser.getOpenId())
                .setNickname(user.getRealName())
                .setExtension(otherUser.getOtherUser());
        if (ObjectNull.isNotNull(extension)) {
            extension.setNickname(user.getRealName())
                    .setExtension(otherUser.getOtherUser());
            //添加扩展参数
            user.setExtension(otherUser.getOtherUser());
            // 用户扩展信息
            userExtensionService.updateById(userExtension);
        } else {
            userExtensionService.saveOrUpdate(userExtension);
            // 默认为游客角色
            userRoleComponent.grandDefaultSysRole(user.getId());
        }
        return user;
    }

    /**
     * 装配默认值
     *
     * @param otherUser
     */
    private void assembly(OtherUserDto otherUser) {
        // 设置姓名
        otherUser.setUserName(Optional.ofNullable(otherUser.getUserName()).orElseGet(() -> IdGenerator.getIdStr(36)));
        // 设置账号
        if (StringUtils.isNotBlank(otherUser.getAccountName())) {
            // 若指定的账号已存在，则生成随机账号
            User one = userService.getOne(Wrappers.query(new User().setAccountName(otherUser.getAccountName())));
            if (ObjectNull.isNotNull(one)) {
                otherUser.setAccountName(IdGenerator.getIdStr(36));
            }
        } else {
            // 未指定账号，则生成随机账号
            otherUser.setAccountName(IdGenerator.getIdStr(36));
        }
        // 设置头像
        otherUser.setAvatar(LoginHandler.getDurableAvatar(otherUser.getUserName(), otherUser.getAvatar()));
    }


    /**
     * 绑定
     *
     * @param otherLoginTypeEnum
     * @param user
     * @param bindUser
     */
    @Transactional(rollbackFor = Exception.class)
    public void bind(OtherLoginTypeEnum otherLoginTypeEnum, User user, OtherUserDto bindUser) {
        UserExtension extension = userExtensionService.getOne(Wrappers.query(new UserExtension().setType(bindUser.getLoginType()).setOpenId(bindUser.getOpenId())));
        // 判断是否重复绑定
        if (ObjectUtil.isNotEmpty(extension)) {
            throw new BusinessException("帐号已绑定");
        }
        // 绑定用户关键信息
        extension = new UserExtension()
                .setUserId(user.getId())
                .setOpenId(bindUser.getOpenId())
                .setNickname(bindUser.getUserName())
                .setType(bindUser.getLoginType())
                .setExtension(bindUser.getOtherUser());
        userService.updateById(user);
        userExtensionService.save(extension);
    }
}
