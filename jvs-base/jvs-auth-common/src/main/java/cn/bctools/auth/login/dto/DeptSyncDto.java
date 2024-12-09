package cn.bctools.auth.login.dto;

import cn.bctools.auth.entity.Dept;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 * 同步部门
 */
@Data
@Accessors(chain = true)
public class DeptSyncDto extends Dept {
    @ApiModelProperty(value = "LDAP中部门的dn")
    private String ldapDn;
}
