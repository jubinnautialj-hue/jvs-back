package cn.bctools.auth.login.auth.ldap;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.stereotype.Component;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import java.util.*;

/**
 * @author zhuxiaokang
 */
@Slf4j
@Component
@AllArgsConstructor
public class LdapBase {
    /**
     * 不保存到数据库的LDAP属性字段
     */
    private static final List<String> IGNORE_ATTRIBUTE = Arrays.asList("userPassword");
    /**
     * LDAP默认登录账号字段
     */
    private static final String DEFAULT_ACCOUNT_ATTRIBUTE = "uid";
    private static final String DISPLAY_NAME = "displayName";
    public static final String OBJECT_CLASS = "objectClass";

    private final LdapAuthConfig ldapAuthConfig;

    public String getUniqueCode(DirContextOperations dirContextOperations, String accountAttribute) {
        String uniqueCode = dirContextOperations.getStringAttribute(accountAttribute);
        if (StringUtils.isBlank(uniqueCode)) {
            log.error("获取LDAP用户的唯一编码失败");
            throw new BusinessException("同步ldap失败");
        }
        return uniqueCode;
    }

    public String getLdapUserName(String openId, Map<String, Object> ldapUser) {
        return ObjectNull.isNull(ldapUser.get(DISPLAY_NAME)) ? openId : String.valueOf(ldapUser.get(DISPLAY_NAME));
    }

    /**
     * 获取LDAP用户属性
     *
     * @param dirContextOperations LDAP返回数据
     * @return LDAP用户属性属性
     */
    public Map<String, Object> getLdapAttributes(DirContextOperations dirContextOperations) {
        Map<String, Object> attributeMap = new LinkedHashMap<>();
        try {
            NamingEnumeration i = dirContextOperations.getAttributes().getAll();
            while (i.hasMore()) {
                Attribute attribute = (Attribute) i.next();
                if (IGNORE_ATTRIBUTE.contains(attribute.getID())) {
                    continue;
                }
                if (attribute.size() == 1) {
                    attributeMap.put(attribute.getID(), attribute.get());
                } else {
                    int j = 0;
                    for (Iterator var5 = ((Iterable) attribute).iterator(); var5.hasNext(); ++j) {
                        Object value = var5.next();
                        attributeMap.put(attribute.getID() + "[" + j + "]", value);
                    }
                }
            }
        } catch (NamingException var7) {
            log.error("获取LDAP属性失败. exception：{}", var7.getMessage());
            throw new BusinessException("同步ldap失败");
        }
        return attributeMap;
    }
}
