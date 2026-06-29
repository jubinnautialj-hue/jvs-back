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
@TableName(value = "sys_user_dept")
public class UserDept implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "用户ID")
    @TableId(type = IdType.INPUT)
    private String userId;
    private String deptId;
    @ApiModelProperty(value = "租户id")
    private String tenantId;

}
