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
public class BindEmployeeRolesDto {
    @NotNull(message = "帐号配置不能为空")
    @ParameterValue(info = "帐号配置", type = InputType.selected, cls = TencenCloudApiSelected.class)
    public String options;

    @ParameterValue(info = "(operator)执行本接口操作的员工信息。", necessity = false, type = InputType.map)
    public Map<String,Object> operator;


    @ParameterValue(info = "(UserIds)绑定角色的用户id列表，不能重复，不能大于 100 个。", type = InputType.list)
    @SerializedName("UserIds")
    @Expose
    public List<String> userIds;


    @ParameterValue(info = "(RoleIds)绑定角色的角色id列表，不能重复，不能大于 100，可以通过DescribeIntegrationRoles接口获取角色信息。", type = InputType.list)
    @SerializedName("RoleIds")
    @Expose
    public List<String> roleIds;

}
