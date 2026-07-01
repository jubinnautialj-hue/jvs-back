package cn.bctools.design.permission.dto;

import cn.bctools.design.permission.entity.dto.PermissionMemberDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
@ApiModel("保存权限设置入参")
public class SavePermissionReqDto {

    @ApiModelProperty(value = "权限成员", required = true)
    private PermissionMemberDto member;

    @ApiModelProperty(value = "权限", required = true)
    private List<PermissionSettingDto> permissions;
}
