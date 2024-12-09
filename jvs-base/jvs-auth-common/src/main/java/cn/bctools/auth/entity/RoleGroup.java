package cn.bctools.auth.entity;

import cn.bctools.auth.entity.enums.RoleMemberScopeEnum;
import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色分组
 *
 * @author
 */
@Data
@Accessors(chain = true)
@TableName(value = "sys_role_group")
public class RoleGroup implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    @ApiModelProperty(value = "主键")
    private String id;
    @ApiModelProperty(value = "角色分组")
    private String name;
    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @ApiModelProperty(value = "修改时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @ApiModelProperty(value = "租户ID")
    private String tenantId;
    @ApiModelProperty(value = "删除标识（0-正常,1-删除）")
    @TableLogic
    private Boolean delFlag;

}
