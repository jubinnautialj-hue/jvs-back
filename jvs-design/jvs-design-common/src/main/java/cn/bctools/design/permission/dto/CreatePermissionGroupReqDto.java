package cn.bctools.design.permission.dto;

import cn.bctools.design.permission.entity.enums.PermissionGroupType;
import cn.bctools.design.permission.entity.enums.PermissionType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
@ApiModel("创建权限组入参")
public class CreatePermissionGroupReqDto {

    @ApiModelProperty(value = "权限组名", required = true)
    @NotBlank(message = "请填写权限组名")
    private String groupName;

    @ApiModelProperty(value = "权限组类型", required = true)
    @NotNull(message = "请指定权限组类型")
    private PermissionGroupType groupType;

    @ApiModelProperty(value = "权限类型类型", required = true)
    @NotNull(message = "请指定权限类型")
    private PermissionType permissionType;
}
