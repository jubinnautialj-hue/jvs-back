package cn.bctools.design.permission.dto;

import cn.bctools.design.permission.entity.dto.PermissionDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
@ApiModel("权限设置")
public class PermissionSettingDto {
    @ApiModelProperty(value = "设计id", required = true)
    @NotBlank(message = "设计id不能为空")
    private String designId;

    @ApiModelProperty(value = "权限")
    private PermissionDto permission;
}
