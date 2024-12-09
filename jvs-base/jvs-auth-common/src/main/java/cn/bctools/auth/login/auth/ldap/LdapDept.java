package cn.bctools.auth.login.auth.ldap;

import cn.bctools.auth.entity.enums.AuthSourceEnum;
import cn.bctools.auth.login.dto.DeptSyncDto;
import cn.bctools.common.enums.SysConfigLdap;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.TenantContextHolder;
import com.alibaba.nacos.common.utils.CollectionUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.ContainerCriteria;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author zhuxiaokang
 * LDAP 部门
 */
@Slf4j
@Component
@AllArgsConstructor
public class LdapDept {

    /**
     * 根节点id（dn为空时，该条数据的id为顶级id）
     */
    private static final String ROOT_ID = "rootId";
    private static final String OU = "ou";
    private final LdapAuthConfig ldapAuthConfig;

    /**
     * 从LDAP获取部门
     *
     * @param appId
     * @param ldapTemplate
     * @return
     */
    public List<DeptSyncDto> getDept(SysConfigLdap config, LdapTemplate ldapTemplate) {
        // ou与id的对应关系
        Map<String, String> ouIdRelation = new HashMap<String, String>(1);
        // 查询LDAP组织架构 (根据指定的ObjectClass查询组织架构)
        String[] objectClassAttr = config.getAccountAttribute().split(",");
        ContainerCriteria ldapQuery = LdapQueryBuilder.query().where(LdapBase.OBJECT_CLASS).is(objectClassAttr[0]);
        for (int i = 1; i < objectClassAttr.length; i++) {
            ldapQuery.and(LdapBase.OBJECT_CLASS).is(objectClassAttr[i]);
        }

        List<DeptSyncDto> ldapOus = ldapTemplate.search(ldapQuery, (ContextMapper<DeptSyncDto>) ctx -> {
            DirContextAdapter dir = (DirContextAdapter) ctx;
            DeptSyncDto deptSyncDto = new DeptSyncDto();
            deptSyncDto.setId(dir.getStringAttribute(config.getAccountAttribute()));
            deptSyncDto.setName(dir.getStringAttribute(OU));
            deptSyncDto.setLdapDn(dir.getDn().toString());
            deptSyncDto.setSort(0);
            deptSyncDto.setSource(AuthSourceEnum.LDAP.name());
            if (StringUtils.isNotBlank(deptSyncDto.getLdapDn())) {
                ouIdRelation.put(deptSyncDto.getLdapDn(), deptSyncDto.getId());
            } else {
                ouIdRelation.put(ROOT_ID, deptSyncDto.getId());
            }
            return deptSyncDto;
        });
        if (CollectionUtils.isEmpty(ldapOus)) {
            return ldapOus;
        }
        // 若有id或name为空的目录，则直接提示同步失败
        Optional<DeptSyncDto> optional = ldapOus.stream().filter(ou -> StringUtils.isBlank(ou.getId()) || StringUtils.isBlank(ou.getName())).findAny();
        if (optional.isPresent()) {
            throw new BusinessException("同步ldap失败_ou_must_have_a_globally_unique_identifier");
        }
        if (ldapOus.size() != ldapOus.stream().map(DeptSyncDto::getId).distinct().count()) {
            throw new BusinessException("同步ldap失败_ou_must_have_a_duplicate_globally_unique_identifier");
        }
        convertDept(ldapOus, ouIdRelation);
        return ldapOus;
    }

    /**
     * 转换数据
     *
     * @param ldapDepts
     * @param ouIdRelation
     */
    private void convertDept(List<DeptSyncDto> ldapDepts, Map<String, String> ouIdRelation) {
        String tenantId = TenantContextHolder.getTenantId();
        ldapDepts.forEach(dept -> {
            // 获取父级id
            String parentDn = dept.getLdapDn().substring(dept.getLdapDn().split(",")[0].length());
            String parentId = StringUtils.isBlank(dept.getLdapDn()) ? tenantId : dept.getLdapDn();
            // 存在上级dn，则获取上级dn的id作为父id
            if (StringUtils.isNotBlank(parentDn)) {
                parentId = ouIdRelation.get(parentDn.substring(1)).toString();
            }
            // 设置父id
            dept.setParentId(parentId);
        });
    }

}
