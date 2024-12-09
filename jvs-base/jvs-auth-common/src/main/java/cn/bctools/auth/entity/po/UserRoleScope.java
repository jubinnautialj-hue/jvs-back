package cn.bctools.auth.entity.po;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author zhuxiaokang
 * 用户角色管理范围
 */
@Data
@Accessors(chain = true)
public class UserRoleScope implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "部门ID", required = true)
    @NotBlank(message = "请选择部门id")
    private String deptId;

    @ApiModelProperty(value = "FALSE-当前部门 TRUE-当前部门及其所有下级部门")
    private Boolean below;
}
