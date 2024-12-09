package cn.bctools.auth.entity;

import cn.bctools.auth.entity.handler.UserRoleScopeTypeHandler;
import cn.bctools.auth.entity.po.UserRoleScope;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 用户角色表
 *
 * @author
 */
@Data
@Accessors(chain = true)
@TableName(value = "sys_user_role", autoResultMap = true)
public class UserRole implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "用户ID")
    @TableId(type = IdType.INPUT)
    private String userId;
    @ApiModelProperty(value = "角色ID")
    private String roleId;
    @ApiModelProperty(value = "管理范围")
    @TableField(value = "scopes", typeHandler = UserRoleScopeTypeHandler.class)
    private List<UserRoleScope> scopes;
    @ApiModelProperty(value = "租户id")
    private String tenantId;

}
