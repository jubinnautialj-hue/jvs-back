package cn.bctools.auth.login.auth.ldap;

import cn.bctools.auth.entity.enums.SysConfigTypeEnum;
import cn.bctools.common.enums.SysConfigLdap;
import cn.bctools.common.exception.BusinessException;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.ldap.LdapProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.DirContextAuthenticationStrategy;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhuxiaokang
 * ldap配置
 */
@Component
@AllArgsConstructor
public class LdapAuthConfig {

    private static final String URL_SEPARATOR = ";";
    private final Environment environment;
    private final ObjectProvider<DirContextAuthenticationStrategy> dirContextAuthenticationStrategy;

    public LdapTemplate buildLdapTemplate(SysConfigLdap config) {
        LdapProperties properties = properties(config);
        ContextSource contextSource = ldapContextSource(properties);
        LdapProperties.Template template = properties.getTemplate();
        PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();
        LdapTemplate ldapTemplate = new LdapTemplate(contextSource);
        propertyMapper.from(template.isIgnorePartialResultException())
                .to(ldapTemplate::setIgnorePartialResultException);
        propertyMapper.from(template.isIgnoreNameNotFoundException()).to(ldapTemplate::setIgnoreNameNotFoundException);
        propertyMapper.from(template.isIgnoreSizeLimitExceededException())
                .to(ldapTemplate::setIgnoreSizeLimitExceededException);
        return ldapTemplate;
    }

    private LdapContextSource ldapContextSource(LdapProperties properties) {
        properties.getBaseEnvironment().put("com.sun.jndi.ldap.connect.timeout", String.valueOf(3000));
        LdapContextSource source = new LdapContextSource();
        dirContextAuthenticationStrategy.ifUnique(source::setAuthenticationStrategy);
        PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();
        propertyMapper.from(properties.getUsername()).to(source::setUserDn);
        propertyMapper.from(properties.getPassword()).to(source::setPassword);
        propertyMapper.from(properties.getAnonymousReadOnly()).to(source::setAnonymousReadOnly);
        propertyMapper.from(properties.getBase()).to(source::setBase);
        propertyMapper.from(properties.determineUrls(environment)).to(source::setUrls);
        propertyMapper.from(properties.getBaseEnvironment()).to(
                (baseEnvironment) -> source.setBaseEnvironmentProperties(Collections.unmodifiableMap(baseEnvironment)));
        source.afterPropertiesSet();
        return source;
    }

    private LdapProperties properties(SysConfigLdap config) {
        String base = config.getBase();
        String urls = config.getUrls();
        String userName = config.getUsername();
        String password = config.getPassword();
        boolean check = StringUtils.isBlank(base) || StringUtils.isBlank(urls)
                || StringUtils.isBlank(userName) || StringUtils.isBlank(password);
        if (check) {
            throw new BusinessException("LDAP配置错误");
        }

        LdapProperties properties = new LdapProperties();
        properties.setBase(base);
        properties.setUrls(urls.replace(" ", "").split(URL_SEPARATOR));
        properties.setUsername(userName);
        properties.setPassword(password);
        return properties;
    }

}
