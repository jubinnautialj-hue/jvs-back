package cn.bctools.rule.ess.impl.rolemanagementrelatedinterfaces;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.rule.ess.impl.TencenCloudApiSelected;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
public class UnbindEmployeeRolesDto {
    @NotNull(message = "帐号配置不能为空")
    @ParameterValue(info = "帐号配置", type = InputType.selected, cls = TencenCloudApiSelected.class)
    public String options;

    @ParameterValue(info = "(operator)执行本接口操作的员工信息。", necessity = false, type = InputType.map)
    public Map operator;

    @ParameterValue(info = "(RoleId)角色id，可以通过DescribeIntegrationRoles接口获取角色信息。", necessity = false, type = InputType.input)
    @SerializedName("RoleId")
    @Expose
    public  String roleId;

    @ParameterValue(info = "(Users)用户信息,最多 200 个用户，并且 UserId 和 OpenId 二选一，其他字段不需要传。", type = InputType.listMap)
    @SerializedName("Users")
    @Expose
    public  List<Map> users;

}
