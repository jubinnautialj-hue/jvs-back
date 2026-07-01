package cn.bctools.design.project.entity.dto;

import cn.bctools.auth.api.dto.PersonnelDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhuxiaokang
 * 应用权限
 */
@Data
@ApiModel("应用权限")
@Accessors(chain = true)
public class AppRoleDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "应用管理员", notes = "以此字段名作为角色标识，不可随意更改")
    private List<PersonnelDto> adminMember;

    @ApiModelProperty(value = "开发人员", notes = "以此字段名作为角色标识，不可随意更改")
    private List<PersonnelDto> devMember;

}
