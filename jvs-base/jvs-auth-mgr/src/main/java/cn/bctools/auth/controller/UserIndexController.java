package cn.bctools.auth.controller;

import cn.bctools.auth.component.SmsEmailComponent;
import cn.bctools.auth.component.UserInfoComponent;
import cn.bctools.auth.entity.*;
import cn.bctools.auth.entity.enums.BulletinEnum;
import cn.bctools.auth.entity.enums.BulletinTypeEnum;
import cn.bctools.auth.login.auth.OtherLoginHandler;
import cn.bctools.auth.login.auth.ding.BaseDd;
import cn.bctools.auth.login.auth.ding.DdScanLoginHandler;
import cn.bctools.auth.login.auth.ldap.LdapLoginHandler;
import cn.bctools.auth.login.auth.wx.WxAppLoginHandler;
import cn.bctools.auth.login.auth.wx.WxLoginHandler;
import cn.bctools.auth.login.auth.wx.WxOfficialAccountsLoginHandler;
import cn.bctools.auth.login.auth.wx.enterprise.BaseWxEnterprise;
import cn.bctools.auth.login.auth.wx.enterprise.WxEnterpriseHandler;
import cn.bctools.auth.login.dto.RegisterDto;
import cn.bctools.auth.login.enums.LoginTypeEnum;
import cn.bctools.auth.service.*;
import cn.bctools.auth.vo.ResetPasswordDto;
import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.OtherLoginTypeEnum;
import cn.bctools.common.enums.SysApplyConfig;
import cn.bctools.common.enums.SysFrameApplyConfig;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.*;
import cn.bctools.common.utils.function.Get;
import cn.bctools.database.util.SqlFunctionUtil;
import cn.bctools.gateway.entity.TenantPo;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.redis.utils.RedisUtils;
import cn.bctools.web.utils.IpUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HtmlUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Slf4j
@Api(tags = "用户登录信息")
@RestController
@RequestMapping("index")
@AllArgsConstructor
public class UserIndexController {

    public static final String MSG_BIND = "绑定成功";
    public static final String MSG_UNBIND = "解绑成功";
    public static final String STRING_BLANK = " ";

    WxLoginHandler wxLoginHandler;
    WxAppLoginHandler wxAppLoginHandler;
    OtherLoginHandler otherLoginHandler;
    WxOfficialAccountsLoginHandler wxOfficialAccountsLoginHandler;
    WxEnterpriseHandler wxEnterpriseHandler;
    DdScanLoginHandler ddScanLoginHandler;
    LdapLoginHandler ldapLoginHandler;
    SmsEmailComponent smsComponent;
    UserInfoComponent userInfoComponent;
    PasswordEncoder passwordEncoder;
    ApplyService applyService;
    RedisUtils redisUtils;
    RoleService roleService;
    UserService userService;
    TenantService tenantService;
    UserTenantService userTenantService;
    UserExtensionService userExtensionService;
    BulletinService bulletinService;
    IndexService indexService;
    SysConfigsService sysConfigService;
    OpinionService opinionService;

    @Log(back = false)
    @ApiOperation("获取公告")
    @GetMapping("/bulletin/{appKey}")
    public R bulletins(@PathVariable String appKey, @RequestParam(value = "count", required = false, defaultValue = "1") int count) {
        //处理移动端和PC端的显示问题
        BulletinTypeEnum bulletinTypeEnum = IpUtil.isMobile() ? BulletinTypeEnum.MOBILE : BulletinTypeEnum.PC;
        LambdaQueryWrapper<Bulletin> last = new LambdaQueryWrapper<Bulletin>().eq(Bulletin::getPublish, true).eq(Bulletin::getType, bulletinTypeEnum)
                //结束时间大于当前时间
                .gt(Bulletin::getEndTime, DateUtil.date())
                .orderByDesc(Bulletin::getStartTime)
                //开始时间大于大前时间
                .lt(Bulletin::getStartTime, DateUtil.date()).apply(SqlFunctionUtil.jsonContains("app_keys", appKey, "$"))
                .last(" limit " + count);
        List<Bulletin> list = bulletinService.list(last);
        if (count > 1) {
            return R.ok(list);
        }
        if (ObjectNull.isNotNull(list)) {
            return R.ok(list.get(0));
        }
        return R.ok();
    }

    @Log(back = false)
    @ApiOperation("获取公告")
    @GetMapping("/bulletin/{appKey}/page")
    public R bulletinsPage(Page<Bulletin> page, @PathVariable String appKey) {
        //处理移动端和PC端的显示问题
        BulletinTypeEnum bulletinTypeEnum = IpUtil.isMobile() ? BulletinTypeEnum.MOBILE : BulletinTypeEnum.PC;
        LambdaQueryWrapper<Bulletin> last = new LambdaQueryWrapper<Bulletin>().eq(Bulletin::getPublish, true).eq(Bulletin::getType, bulletinTypeEnum)
                //结束时间大于当前时间
                .gt(Bulletin::getEndTime, DateUtil.date())
                .orderByDesc(Bulletin::getStartTime)
                //开始时间大于大前时间
                .lt(Bulletin::getStartTime, DateUtil.date()).apply(SqlFunctionUtil.jsonContains("app_keys", appKey, "$"));
        bulletinService.page(page, last).getRecords().forEach(e -> {
            if (e.getContentType().equals(BulletinEnum.TEXT)) {
                String unescape = HtmlUtil.unescape(HtmlUtil.cleanHtmlTag((e).getContent()).replaceAll("\n", ""));
                e.setContent(unescape);
            }
        });
        return R.ok(page);
    }


    @Log(back = false)
    @ApiOperation("获取当前登录用户所在租户信息")
    @GetMapping("/this/tenant")
    public R<TenantPo> thisTenant() {
        String clientId = UserCurrentUtils.getCurrentUser().getClientId();
        TenantPo byId = tenantService.getById(UserCurrentUtils.getCurrentUser().getTenantId());
        ConfigsTypeEnum configsTypeEnum = Arrays.stream(ConfigsTypeEnum.values()).filter(e -> clientId.equals(e.clientId)).findAny().get();
        SysApplyConfig config = sysConfigService.getConfig(configsTypeEnum);
        byId.setSystemName(config.getSystemName());
        return R.ok(byId);
    }

    /**
     * 缓存当前用户信息
     *
     * @return
     */
    @Log
    @ApiOperation("获取当前用户信息")
    @GetMapping("/user/info")
    @Cacheable(value = SysConstant.USERID)
    public R<UserDto> userInfo() {
        UserDto user = UserCurrentUtils.getCurrentUser();
        User byId = userService.getById(user.getId());
        UserDto userInfoDto = userInfoComponent.getUserInfoDto(byId, user.getTenantId());
        // 获取用户扩展信息
        List<UserExtension> extensions = userExtensionService.list(Wrappers.<UserExtension>lambdaQuery().isNotNull(UserExtension::getType).eq(UserExtension::getUserId, user.getId()));
        if (ObjectNull.isNotNull(extensions)) {
            Map<String, Object> collect = extensions.stream().collect(Collectors.toMap(UserExtension::getType, UserExtension::getExtension));
            userInfoDto.setExceptions(collect);
        }
        return R.ok(userInfoDto);
    }

    @Log
    @ApiOperation("获取当前用户信息")
    @PostMapping("/user/info")
    public R<UserDto> userInfo(@RequestBody RegisterDto registerDto) {

        //校验是否成功
        smsComponent.check(registerDto.getPhone(), registerDto.getCode(), () -> new BusinessException("验证失败"));

        UserDto user = UserCurrentUtils.getCurrentUser();
        // 获取用户扩展信息
        List<UserExtension> extensions = userExtensionService.list(Wrappers.<UserExtension>lambdaQuery().eq(UserExtension::getUserId, user.getId()));
        Map<String, Object> collect = extensions.stream().collect(Collectors.toMap(UserExtension::getType, UserExtension::getExtension));
        User byId = userService.getById(user.getId());
        UserDto userInfoDto = userInfoComponent.getUserInfoDto(byId, user.getTenantId());
        userInfoDto.setExceptions(collect);
        return R.ok(userInfoDto);
    }

    @Log
    @ApiOperation("完善信息")
    @GetMapping("/user/complete")
    public R<UserDto> complete() {
        UserDto user = UserCurrentUtils.getCurrentUser();
        // 获取用户扩展信息
        List<UserExtension> extensions = userExtensionService.list(Wrappers.<UserExtension>lambdaQuery().eq(UserExtension::getUserId, user.getId()));
        Map<String, Object> collect = extensions.stream().collect(Collectors.toMap(UserExtension::getType, UserExtension::getExtension));
        User byId = userService.getById(user.getId());
        UserDto userInfoDto = userInfoComponent.getUserInfoDto(byId, user.getTenantId());
        userInfoDto.setExceptions(collect);
        return R.ok(userInfoDto);
    }

    @Log
    @ApiOperation("修改账号")
    @PutMapping("/update/account")
    @Transactional(rollbackFor = Exception.class)
    public R updateAccount(@RequestParam String accountName) {
        if (StringUtils.isBlank(accountName)) {
            return R.failed("账号不能为空");
        }
        if (accountName.contains(STRING_BLANK)) {
            return R.failed("账号不能为空包含空格");
        }
        String userId = UserCurrentUtils.getUserId();
        long count = userService.count(Wrappers.<User>lambdaQuery().ne(User::getId, userId).eq(User::getAccountName, accountName).last(SysConstant.FOR_UPDATE));
        if (count >= 1) {
            return R.failed("该账号名已被使用");
        }
        userService.updateInfo(User::getAccountName, accountName, userId);
        return R.ok().setMsg("修改成功");
    }

    @Log
    @ApiOperation("修改昵称")
    @PutMapping("/update/realName")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> updateRealName(@RequestParam String realName) {
        if (StringUtils.isBlank(realName)) {
            return R.failed("昵称不能为空");
        }
        if (realName.contains(STRING_BLANK)) {
            return R.failed("昵称不能为空包含空格");
        }
        String userId = UserCurrentUtils.getUserId();
        userTenantService.update(Wrappers.<UserTenant>lambdaUpdate().set(UserTenant::getRealName, realName).eq(UserTenant::getUserId, userId));
        userService.updateInfo(User::getRealName, realName, userId);
        return R.ok(true, "修改成功");
    }

    /**
     * 绑定手机号，
     */
    @Log
    @ApiOperation("绑定手机")
    @PutMapping("/bind/phone")
    public R<Boolean> bindPhone(@RequestParam String phone, @RequestParam String code) {
        smsComponent.check(phone, code, () -> new BusinessException("验证码不正确"));
        String userId = UserCurrentUtils.getUserId();
        //判断是否是合并关系
        userService.updateInfo(User::getPhone, phone, userId);
        userTenantService.update(Wrappers.<UserTenant>lambdaUpdate().set(UserTenant::getPhone, phone).eq(UserTenant::getUserId, userId));
        return R.ok(true, MSG_BIND);
    }

    @Log
    @ApiOperation("解除手机绑定")
    @DeleteMapping("/bind/phone")
    public R<Boolean> unbindPhone() {
        String userId = UserCurrentUtils.getUserId();
        userService.updateInfo(User::getPhone, null, userId);
        userTenantService.update(Wrappers.<UserTenant>lambdaUpdate().set(UserTenant::getPhone, null).eq(UserTenant::getUserId, userId));
        return R.ok(true, MSG_UNBIND);
    }

    @Log
    @ApiOperation("绑定微信")
    @PutMapping("/bind/wx")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> bindWx(@RequestParam String code, @RequestParam String appId) {
        User user = userService.getById(UserCurrentUtils.getUserId());
        wxOfficialAccountsLoginHandler.bind(user, code, appId);
        return R.ok(true, MSG_BIND);
    }

    @Log
    @ApiOperation("绑定小程序")
    @PutMapping("/bind/wxapp")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> bindWxApp(@RequestParam String code, @RequestParam String appId) {
        User user = userService.getById(UserCurrentUtils.getUserId());
        wxAppLoginHandler.bind(user, code, appId);
        return R.ok(true, MSG_BIND);
    }

    @Log
    @ApiOperation("绑定绑定其它三方平台")
    @PutMapping("/bind/other/{type}")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> bindOther(@RequestParam String code, @PathVariable("type") String type, @RequestParam String appId) {
        User user = userService.getById(UserCurrentUtils.getUserId());
        otherLoginHandler.bind(user, code, type, appId);
        return R.ok(true, MSG_BIND);
    }

    @Log
    @ApiOperation("解除微信绑定")
    @DeleteMapping("/bind/wx")
    public R<Boolean> bindWx() {
        String userId = UserCurrentUtils.getUserId();
        userExtensionService.remove(Wrappers.<UserExtension>lambdaQuery().eq(UserExtension::getUserId, userId).in(UserExtension::getType, Arrays.asList(OtherLoginTypeEnum.WECHAT_MP.name(), OtherLoginTypeEnum.wxapp.name())));
        return R.ok(true, MSG_UNBIND);
    }

    @Log
    @ApiOperation("绑定企业微信")
    @PutMapping("/bind/wxenterprise")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> bindWxEnterprise(@RequestParam String code, @RequestParam String appId) {
        User user = userService.getById(UserCurrentUtils.getUserId());
        wxEnterpriseHandler.bind(user, code, appId);
        return R.ok(true, MSG_BIND);
    }

    @Log
    @ApiOperation("解除企业微信绑定")
    @DeleteMapping("/bind/wxenterprise")
    public R<Boolean> bindWxEnterprise() {
        String userId = UserCurrentUtils.getUserId();
        userExtensionService.remove(Wrappers.<UserExtension>lambdaQuery().eq(UserExtension::getUserId, userId).eq(UserExtension::getType, BaseWxEnterprise.LOGIN_TYPE));
        return R.ok(true, MSG_UNBIND);
    }

    @Log
    @ApiOperation("绑定钉钉")
    @PutMapping("/bind/ding")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> bindDing(@RequestParam String code, @RequestParam String appId) {
        User user = userService.getById(UserCurrentUtils.getUserId());
        ddScanLoginHandler.bind(user, code, appId);
        return R.ok(true, MSG_BIND);
    }

    @Log
    @ApiOperation("解除钉钉绑定")
    @DeleteMapping("/bind/ding")
    public R<Boolean> bindDing() {
        String userId = UserCurrentUtils.getUserId();
        userExtensionService.remove(Wrappers.<UserExtension>lambdaQuery().eq(UserExtension::getUserId, userId).eq(UserExtension::getType, BaseDd.LOGIN_TYPE));
        return R.ok(true, MSG_UNBIND);
    }

    @Log
    @ApiOperation("绑定LDAP")
    @PutMapping("/bind/ldap")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> bindLdap(@RequestParam String code, @RequestParam String appId) {
        User user = userService.getById(UserCurrentUtils.getUserId());
        ldapLoginHandler.bind(user, code, appId);
        return R.ok(true, MSG_BIND);
    }

    @Log
    @ApiOperation("解除LDAP绑定")
    @DeleteMapping("/bind/ldap")
    public R<Boolean> bindLdap() {
        String userId = UserCurrentUtils.getUserId();
        userExtensionService.remove(Wrappers.<UserExtension>lambdaQuery().eq(UserExtension::getUserId, userId).eq(UserExtension::getType, LoginTypeEnum.LDAP.getValue()));
        return R.ok(true, MSG_UNBIND);
    }

    @Log
    @ApiOperation("发送邮箱验证码")
    @GetMapping("/send/email/code")
    public R<Boolean> codeEmail(@RequestParam String email) {
        UserDto user = UserCurrentUtils.getCurrentUser();
        TenantPo tenantPo = tenantService.getById(user.getTenantId());
        smsComponent.sendEmailCode(user, email, tenantPo);
        return R.ok(true, "发送成功");
    }

    @Log
    @ApiOperation("绑定邮箱")
    @PutMapping("/bind/email")
    public R<Boolean> bindEmail(@RequestParam String email, @RequestParam String code) {
        smsComponent.checkEmailCode(email, code, () -> new BusinessException("验证码不正确"));
        String userId = UserCurrentUtils.getUserId();
        userService.updateInfo(User::getEmail, email, userId);
        return R.ok(true, MSG_BIND);
    }

    @Log
    @ApiOperation("修改头像")
    @PutMapping("/bind/headImg")
    public R<Boolean> bindEmail(@RequestParam String headImg) {
        String userId = UserCurrentUtils.getUserId();
        userService.updateInfo(User::getHeadImg, headImg, userId);
        return R.ok(true, "修改成功");
    }

    @Log
    @ApiOperation("解除邮箱绑定")
    @DeleteMapping("/bind/email")
    public R<Boolean> bindEmail() {
        String userId = UserCurrentUtils.getUserId();
        userService.updateInfo(User::getEmail, null, userId);
        return R.ok(true, "解除成功");
    }

    @Log
    @ApiOperation("设置密码")
    @PostMapping("/user/change/password")
    public R<Boolean> changePassword(@RequestBody ResetPasswordDto resetPasswordDto) {
        if (!resetPasswordDto.getPassword().equals(resetPasswordDto.getRePassword())) {
            return R.failed("密码不匹配");
        }
        // appId为与前端对应的应用id
        String decodedPassword = PasswordUtil.decodedPassword(resetPasswordDto.getRePassword(), SpringContextUtil.getKey());
        String encodedPassword = passwordEncoder.encode(decodedPassword);
        // 修改密码
        String userId = UserCurrentUtils.getUserId();
        userService.updateInfo(User::getPassword, encodedPassword, userId);
        return R.ok(true, "设置密码成功");
    }

    @Log
    @ApiOperation("我的组织")
    @GetMapping("/my/tenants")
    public R<List<TenantPo>> myTenants() {
        String userId = UserCurrentUtils.getUserId();
        List<TenantPo> list = tenantService.list(Wrappers.query(new TenantPo().setEnable(true).setAdminUserId(userId)));
        list.forEach(e -> {
            TenantContextHolder.setTenantId(e.getId());
            SysFrameApplyConfig config = sysConfigService.getConfig(ConfigsTypeEnum.BACKGROUND_PERSONALIZED_CONFIGURATION);
            if (config.getEnable()) {
                e.setLogo(config.getLogo());
                e.setIcon(config.getIcon());
                e.setName(e.getName());
            }
        });
        return R.ok(list);
    }

    @Log
    @ApiOperation(value = "解散组织", notes = "解散组织后所有用户都不可登录即组织")
    @DeleteMapping("/my/tenant/{tenantId}")
    public R<Boolean> delete(@PathVariable String tenantId) {
        //校验用户是否是管理员
        String userId = UserCurrentUtils.getUserId();
        if (tenantService.getById(tenantId).getAdminUserId().equals(userId)) {
            tenantService.removeById(tenantId);
        }
        return R.ok(true, "解散成功");
    }

    @Log
    @ApiOperation("已经加入的组织")
    @GetMapping("/tenants")
    @Transactional(rollbackFor = Exception.class)
    public R<List<TenantPo>> tenants() {
        String userId = UserCurrentUtils.getUserId();
        TenantContextHolder.clear();
        // 查询自己所在的组织集合
        List<UserTenant> userTenantList = userTenantService.list(Wrappers.<UserTenant>lambdaQuery().select(UserTenant::getTenantId).eq(UserTenant::getUserId, userId).eq(UserTenant::getCancelFlag, false));
        List<String> tenantIds = userTenantList.stream().map(UserTenant::getTenantId).collect(Collectors.toList());
        if (ObjectUtils.isEmpty(tenantIds)) {
            return R.ok(Collections.emptyList());
        }
        List<TenantPo> tenantList = tenantService.list(Wrappers.<TenantPo>lambdaQuery().in(TenantPo::getId, tenantIds)
                // 排除自己创建的组织
                .ne(TenantPo::getAdminUserId, userId));
        return R.ok(tenantList);
    }

    @Log
    @ApiOperation("退出组织")
    @GetMapping("/tenant/{tenantId}")
    public R<Boolean> tenants(@PathVariable String tenantId) {
        //注销对应组织的帐号
        String userId = UserCurrentUtils.getUserId();
        TenantContextHolder.setTenantId(tenantId);
        userTenantService.clearUser(userId);
        return R.ok(true, "退出成功");
    }

    @Log
    @ApiOperation("是否是平台管理员")
    @GetMapping("/administrator")
    public R<Boolean> administrator() {
        //注销对应组织的帐号
        String tenantId = UserCurrentUtils.getCurrentUser().getTenantId();
        TenantPo byId = tenantService.getById(tenantId);
        //租户的父级是空的，则是最上级管理员
        return R.ok(ObjectNull.isNull(byId.getParentId()));
    }

    @Log
    @ApiOperation("注销帐号")
    @PutMapping("/logoff")
    public R<Boolean> logoff() {
        String userId = UserCurrentUtils.getUserId();
        userService.updateInfo(User::getCancelFlag, true, userId);
        return R.ok(true, "注销成功");
    }

    /**
     * 移动端的小程序提交意见
     *
     * @param opinion
     * @return
     */
    @Log
    @ApiOperation("提交意见")
    @PostMapping("/opinion")
    public R opinion(@RequestBody Opinion opinion) {
        String userId = UserCurrentUtils.getUserId();
        opinion.setCreateById(userId);
        opinion.setCreateByHeadImg(UserCurrentUtils.getCurrentUser().getHeadImg());
        opinion.setCreateBy(UserCurrentUtils.getRealName());
        opinionService.save(opinion);
        return R.ok();
    }

    @Log
    @ApiOperation("最新的那条公告信息")
    @GetMapping("/bulletin/index")
    public R bulletinIndex() {
        List<Bulletin> list = bulletinService.list(new LambdaQueryWrapper<Bulletin>().eq(Bulletin::getType, BulletinTypeEnum.MOBILE).eq(Bulletin::getPublish, true).orderByDesc(Bulletin::getCreateTime).last(" limit 1"));
        //截取数据,不需要太长的消息信息
        if (ObjectNull.isNotNull(list)) {
            //最新公告不要内容
            list.forEach(e -> e.setContent(""));
            List<Object> collect = list.stream().map(e -> DateUtils.transformation(e, Get.name(Bulletin::getStartTime))).collect(Collectors.toList());
            return R.ok(collect.get(0));
        } else {
            return R.ok();
        }
    }

    @Log
    @ApiOperation("业务公告信息")
    @GetMapping("/bulletin")
    public R bulletin(@RequestParam(value = "title", required = false) String title) {
        String userId = UserCurrentUtils.getUserId();
        List<Bulletin> list = bulletinService.list(new LambdaQueryWrapper<Bulletin>().like(ObjectNull.isNotNull(title), Bulletin::getTitle, title).or().like(ObjectNull.isNotNull(title), Bulletin::getContent, title).eq(Bulletin::getType, BulletinTypeEnum.MOBILE).eq(Bulletin::getPublish, true)
                //默认所有人
//                .like(Bulletin::getUserIds, userId)
                .orderByDesc(Bulletin::getCreateTime));
        //截取数据,不需要太长的消息信息
        list.forEach(e -> {
            String s = HtmlUtil.unescape(HtmlUtil.cleanHtmlTag(e.getContent()));
            e.setContent(s.substring(0, s.length() < 400 ? s.length() : 400));
        });
        List<Object> collect = list.stream().map(e -> DateUtils.transformation(e, Get.name(Bulletin::getStartTime))).collect(Collectors.toList());
        return R.ok(collect);
    }

    @Log
    @ApiOperation("业务公告信息详情")
    @GetMapping("/bulletin/index/{id}")
    public R bulletinBy(@PathVariable("id") String id) {
        return R.ok(bulletinService.getById(id));
    }


}
