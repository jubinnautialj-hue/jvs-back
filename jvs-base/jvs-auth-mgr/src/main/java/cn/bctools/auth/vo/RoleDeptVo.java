package cn.bctools.auth.vo;

import cn.bctools.auth.entity.DeptRole;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 * 角色部门
 */
@Data
@Accessors(chain = true)
public class RoleDeptVo extends DeptRole {
    @ApiModelProperty(value = "部门名称")
    private String name;
}
