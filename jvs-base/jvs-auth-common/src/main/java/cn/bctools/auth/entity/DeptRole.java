package cn.bctools.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author zhuxiaokang
 * 部门角色表
 */
@Data
@Accessors(chain = true)
@TableName(value = "sys_dept_role")
public class DeptRole implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "部门ID")
    @TableId(type = IdType.INPUT)
    private String deptId;
    @ApiModelProperty(value = "FALSE-当前部门 TRUE-当前部门及其所有下级部门")
    private Boolean below;
    @ApiModelProperty(value = "角色ID")
    private String roleId;
    @ApiModelProperty(value = "租户id")
    private String tenantId;
}
