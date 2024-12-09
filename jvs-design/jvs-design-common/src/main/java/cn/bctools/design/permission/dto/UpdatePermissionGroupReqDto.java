package cn.bctools.design.permission.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * @author zhuxiaokang
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel("修改权限组入参")
public class UpdatePermissionGroupReqDto extends CreatePermissionGroupReqDto {
    @ApiModelProperty(value = "权限组id", required = true)
    @NotBlank(message = "权限组id不能为空")
    private String id;
}
