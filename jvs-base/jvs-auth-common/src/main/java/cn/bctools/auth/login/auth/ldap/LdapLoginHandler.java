package cn.bctools.auth.login.auth.ldap;

import cn.bctools.auth.component.OtherLoginUserInfoComponent;
import cn.bctools.auth.component.cons.OtherUserDto;
import cn.bctools.auth.entity.User;
import cn.bctools.auth.login.LoginHandler;
import cn.bctools.auth.login.dto.DeptSyncDto;
import cn.bctools.auth.login.dto.LdapDto;
import cn.bctools.auth.login.dto.SyncUserDto;
import cn.bctools.auth.service.SysConfigsService;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.OtherLoginTypeEnum;
import cn.bctools.common.enums.SysConfigLdap;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.PasswordUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.ldap.CommunicationException;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LookupAttemptingCallback;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author zhuxiaokang
 */
@Slf4j
@AllArgsConstructor
@Component("LDAP")
public class LdapLoginHandler implements LoginHandler<LdapDto> {
    public static final String LOGIN_TYPE = OtherLoginTypeEnum.LDAP.name();
    private final LdapAuthConfig ldapAuthConfig;
    private final LdapBase ldapBase;
    private final LdapDept ldapDept;
    private final LdapUser ldapUser;
    private final OtherLoginUserInfoComponent otherLoginUserInfoComponent;
    private final SysConfigsService configsService;

    @Override
    public User handle(String code, String appId, LdapDto ldapDto) {
        SysConfigLdap config = configsService.getConfig(ConfigsTypeEnum.LDAP_CONFIGURATION);
        if (config.getEnable()) {
            //初始化ldaptemplate对象
            LdapTemplate ldapTemplate = ldapAuthConfig.buildLdapTemplate(config);
            // 鉴权
            DirContextOperations dirContextOperations = authenticate(ldapTemplate, ldapDto, config);
            // 返回用户信息
            return getUserInfo(ldapDto.getAccount(), dirContextOperations, config.getAccountAttribute());
        }
        throw new BusinessException("LDAP配置错误");
    }

    /**
     * 鉴权
     *
     * @param ldapDto
     * @param config
     * @return
     */
    private DirContextOperations authenticate(LdapTemplate ldapTemplate, LdapDto ldapDto, SysConfigLdap config) {
        try {
            String accountAttribute = config.getAccountAttribute();
            EqualsFilter filter = new EqualsFilter(accountAttribute, ldapDto.getAccount());
            LdapQuery ldapQuery = LdapQueryBuilder.query().filter(filter);
            return ldapTemplate.authenticate(ldapQuery, ldapDto.getPassword(), new LookupAttemptingCallback());
        } catch (Exception e) {
            log.error("登录失败。exception: {}", e.getMessage());
            String invalidCredentials = "Invalid Credentials";
            if (e.getMessage().contains(invalidCredentials) || e instanceof EmptyResultDataAccessException) {
                throw new BusinessException("用户名或密码错误");
            }
            if (e instanceof CommunicationException) {
                throw new BusinessException("LDAP配置错误");
            }
            throw new BusinessException("LDAP配置错误");
        }
    }

    /**
     * 获取用户信息
     *
     * @param account              LDAP登录账号
     * @param dirContextOperations LDAP返回数据
     * @param accountAttribute
     * @return 用户信息
     */
    private User getUserInfo(String account, DirContextOperations dirContextOperations, String accountAttribute) {
        String openId = ldapBase.getUniqueCode(dirContextOperations, accountAttribute);
        Map<String, Object> ldapUser = ldapBase.getLdapAttributes(dirContextOperations);
        String realName = ldapBase.getLdapUserName(openId, ldapUser);
        OtherUserDto otherUserDto = new OtherUserDto()
                .setOpenId(openId)
                .setUserName(realName)
                .setAccountName(account)
                .setLoginType(LOGIN_TYPE)
                .setOtherUser(ldapUser);
        return otherLoginUserInfoComponent.getUser(otherUserDto);
    }

    @Override
    public void bind(User user, String code, String appId) {
        String decodedPassword = PasswordUtil.decodedPassword(code, appId);
        LdapDto dto = JSONObject.parseObject(decodedPassword, LdapDto.class);
        SysConfigLdap config = configsService.getConfig(ConfigsTypeEnum.LDAP_CONFIGURATION);
        if (config.getEnable()) {
            //初始化ldaptemplate对象
            LdapTemplate ldapTemplate = ldapAuthConfig.buildLdapTemplate(config);
            DirContextOperations dirContextOperations = authenticate(ldapTemplate, dto, config);
            Map<String, Object> authUser = ldapBase.getLdapAttributes(dirContextOperations);
            log.info("[bind] 获取LDAP用户信息: {}", JSONUtil.toJsonStr(authUser));
            String openId = ldapBase.getUniqueCode(dirContextOperations, config.getAccountAttribute());
            String nickname = ldapBase.getLdapUserName(openId, authUser);
            OtherUserDto otherUserDto = new OtherUserDto()
                    .setOpenId(openId)
                    .setUserName(nickname)
                    .setLoginType(LOGIN_TYPE)
                    .setOtherUser(authUser);
            otherLoginUserInfoComponent.bind(OtherLoginTypeEnum.LDAP, user, otherUserDto);
        }
    }

    @Override
    public SyncUserDto syncUserDeptAll() {
        SysConfigLdap config = configsService.getConfig(ConfigsTypeEnum.LDAP_CONFIGURATION);
        if (config.getEnable()) {
            String accountAttribute = config.getAccountAttribute();
            //初始化ldaptemplate对象
            LdapTemplate ldapTemplate = ldapAuthConfig.buildLdapTemplate(config);
            List<DeptSyncDto> deptAll = ldapDept.getDept(config, ldapTemplate);
            SyncUserDto deptUserAll = ldapUser.getDeptUserAll(ldapTemplate, deptAll, accountAttribute);
            return deptUserAll.setList(deptAll);
        }
        throw new BusinessException("Jump_登录失败");
    }

}
