package cn.bctools.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 角色菜单表
 *
 * @author
 */
@Data
@TableName(value = "sys_role_permission")
@Accessors(chain = true)
@ApiModel(value = "资源权限数据表")
public class RolePermission implements Serializable {
    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "角色ID")
    @TableId(type = IdType.INPUT)
    private String roleId;

    @ApiModelProperty(value = "资源ID或菜单ID")
    private String permissionId;

}
