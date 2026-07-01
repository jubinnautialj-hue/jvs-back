package cn.bctools.design.workflow.dto;

import cn.bctools.auth.api.dto.PersonnelDto;
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
@ApiModel("工作流用户范围查询入参")
public class UserScopeSelectedDto {

    @ApiModelProperty(value = "查询数量")
    private Long size;

    @ApiModelProperty(value = "多纬度查询条件")
    private String key;

    @ApiModelProperty(value = "false-只查询未删除的用户；true-查询所有用户(包括已删除的)")
    private Boolean all;

    @ApiModelProperty(value = "审批人可选范围")
    private List<PersonnelDto> personnelScopes;

}
