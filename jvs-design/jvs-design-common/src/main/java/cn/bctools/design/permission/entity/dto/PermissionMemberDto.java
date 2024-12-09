package cn.bctools.design.permission.entity.dto;

import cn.bctools.auth.api.dto.PersonnelDto;
import cn.bctools.auth.api.enums.PersonnelTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhuxiaokang
 */

@Data
@Accessors(chain = true)
@ApiModel("权限成员")
public class PermissionMemberDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("自定义人物，或所有人")
    private PersonnelTypeEnum personType;

    @ApiModelProperty("权限人员/角色")
    private List<PersonnelDto> personnels;
}
