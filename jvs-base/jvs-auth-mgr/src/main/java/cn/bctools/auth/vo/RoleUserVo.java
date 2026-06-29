package cn.bctools.auth.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author zhuxiaokang
 * 角色用户
 */
@Data
@Accessors(chain = true)
public class RoleUserVo extends UserVo {

    @ApiModelProperty(value = "角色ID")
    private String roleId;
    @ApiModelProperty(value = "管理范围")
    private List<UserRoleScopeVo> scopes;
}
