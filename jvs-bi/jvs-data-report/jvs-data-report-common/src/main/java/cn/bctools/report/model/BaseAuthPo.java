package cn.bctools.report.model;

import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.permission.dto.AuthRole;
import cn.bctools.permission.enums.OperationType;
import cn.bctools.permission.handler.ListAuthRoleTypeHandler;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
@Accessors(chain = true)
public class BaseAuthPo extends BasalPo {

    @ApiModelProperty("权限集")
    @TableField(value = "role",typeHandler = ListAuthRoleTypeHandler.class)
    private List<AuthRole> role;

    @ApiModelProperty(value = "权限类型",notes = "true 自定义权限，false 公开权限")
    @TableField("role_type")
    private Boolean roleType;

    @TableField(exist = false)
    private List<OperationType> operationList;
}
