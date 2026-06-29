package cn.bctools.auth.login.auth.ldap;

import cn.bctools.auth.entity.User;
import cn.bctools.auth.entity.UserExtension;
import cn.bctools.auth.entity.UserTenant;
import cn.bctools.auth.entity.enums.UserTypeEnum;
import cn.bctools.auth.login.dto.DeptSyncDto;
import cn.bctools.auth.login.dto.SyncUserDto;
import cn.bctools.auth.util.SyncOrgUtils;
import cn.bctools.common.enums.OtherLoginTypeEnum;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanToMapUtils;
import cn.bctools.common.utils.ObjectNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.ContainerCriteria;
import org.springframework.stereotype.Component;

import javax.naming.directory.SearchControls;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author zhuxiaokang
 * LDAP用户
 */
@Slf4j
@Component
@AllArgsConstructor
public class LdapUser {
    private final LdapAuthConfig ldapAuthConfig;
    private final LdapBase ldapBase;

    /**
     * 获取部门所有用户
     *
     * @param depts
     * @param accountAttribute
     * @return
     */
    public SyncUserDto getDeptUserAll(LdapTemplate ldapTemplate, List<DeptSyncDto> depts, String accountAttribute) {
//        if (StringUtils.isBlank(AuthConfigUtil.userObjectClass(SysConfigTypeEnum.LDAP))) {
//            throw new BusinessException("未配置用户ObjectClass");
//        }
        SyncUserDto syncUserDto = new SyncUserDto();
        if (CollectionUtils.isEmpty(depts)) {
            return syncUserDto;
        }
        ContainerCriteria ldapQuery = buildLdapQuery();
        depts.forEach(dept -> {
            List<User> deptUsers = getDeptUser(ldapTemplate, dept, ldapQuery, accountAttribute);
            convertUser(syncUserDto, dept, deptUsers);
        });
        if (syncUserDto.getUsers().size() != syncUserDto.getUsers().stream().map(User::getId).distinct().count()) {
            throw new BusinessException("同步ldap失败_ou_must_have_a_duplicate_globally_unique_identifier");
        }
        return syncUserDto;
    }

    /**
     * 构造查询用户条件
     *
     * @return
     */
    private ContainerCriteria buildLdapQuery() {
        // 获取LDAP部门下的用户 (根据指定的ObjectClass查询用户)
//        String[] objectClassAttr = AuthConfigUtil.userObjectClass(SysConfigTypeEnum.LDAP).split(",");
//        ContainerCriteria ldapQuery = LdapQueryBuilder.query().where(LdapBase.OBJECT_CLASS).is(objectClassAttr[0]);
//        for (int i = 1; i < objectClassAttr.length; i++) {
//            ldapQuery.and(LdapBase.OBJECT_CLASS).is(objectClassAttr[i]);
//        }
//        return ldapQuery;
        return null;
    }

    /**
     * 获取LDAP指定部门的用户
     *
     * @param dept
     * @param ldapQuery
     * @param accountAttribute
     * @return
     */
    private List<User> getDeptUser(LdapTemplate ldapTemplate, DeptSyncDto dept, ContainerCriteria ldapQuery, String accountAttribute) {
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        return ldapTemplate.search(dept.getLdapDn(), ldapQuery.filter().toString(), searchControls, (ContextMapper<User>) ctx -> {
            DirContextAdapter dir = (DirContextAdapter) ctx;
            String openId = ldapBase.getUniqueCode(dir, accountAttribute);
            return new User()
                    .setId(openId)
                    .setRealName(ldapBase.getLdapUserName(openId, ldapBase.getLdapAttributes(dir)))
                    .setAccountName(openId);
        });
    }

    /**
     * 得到要同步的用户信息
     *
     * @param syncUserDto 转换后的用户信息
     * @param dept        三方平台部门
     * @param deptUsers   三方平台部门用户集合
     */
    private void convertUser(SyncUserDto syncUserDto, DeptSyncDto dept, List<User> deptUsers) {
        if (deptUsers.isEmpty()) {
            return;
        }
        String tenantId = "1";
        deptUsers.parallelStream().forEach(deptUser -> {
            String openId = deptUser.getId();
            if (StringUtils.isNotBlank(openId)) {
                // 用户
                Optional<User> optionalDeptUser = syncUserDto.getUsers().stream()
                        .filter(user -> user.getId().equals(openId))
                        .findFirst();
                User user = null;
                if (optionalDeptUser.isPresent()) {
                    user = optionalDeptUser.get();
                } else {
                    user = deptUser;
                    user.setId(openId)
                            .setCancelFlag(false)
                            .setUserType(UserTypeEnum.OTHER_USER);

                    UserExtension userExtension = new UserExtension()
                            .setExtension(BeanToMapUtils.beanToMap(deptUser))
                            .setOpenId(openId)
                            .setNickname(user.getRealName())
                            .setType(OtherLoginTypeEnum.LDAP.name())
                            .setUserId(user.getId());

                    syncUserDto.getUsers().add(user);
                    syncUserDto.getUserExtensions().add(userExtension);
                }

                // 用户租户
                Optional<UserTenant> optionalUserTenant = syncUserDto.getUserTenants().stream()
                        .filter(userTenant -> userTenant.getUserId().equals(openId))
                        .findFirst();
                UserTenant userTenant = null;
                if (optionalUserTenant.isPresent()) {
                    userTenant = optionalUserTenant.get();
                } else {
                    userTenant = new UserTenant()
                            .setUserId(user.getId())
                            .setRealName(user.getRealName())
                            .setCancelFlag(false);
                    syncUserDto.getUserTenants().add(userTenant);
                }

                if (ObjectNull.isNotNull(dept)) {
                    List<String> userDeptIdsList = Optional.ofNullable(userTenant.getDeptId()).orElseGet(ArrayList::new);
                    userDeptIdsList.add(SyncOrgUtils.buildDeptId(tenantId, dept.getId()));
                    userTenant.setDeptId(userDeptIdsList);
                }
            }
        });

    }
}
