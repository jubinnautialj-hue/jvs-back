package cn.bctools.auth.vo;

import cn.bctools.auth.api.enums.PersonnelTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
public class UserRoleScopeVo {
    @ApiModelProperty("部门id")
    private String id;
    @ApiModelProperty("部门名称")
    private String name;
    @ApiModelProperty("类型")
    private PersonnelTypeEnum type;
    @ApiModelProperty(value = "FALSE-当前部门 TRUE-当前部门及其所有下级部门")
    private Boolean below;
}
