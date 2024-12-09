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
public class CreateEnterpriseRoleDto {
    @NotNull(message = "帐号配置不能为空")
    @ParameterValue(info = "帐号配置", type = InputType.selected, cls = TencenCloudApiSelected.class)
    public String options;

    @ParameterValue(info = "(operator)执行本接口操作的员工信息。", necessity = false, type = InputType.map)
    public Map operator;


    @ParameterValue(info = "(Name)角色名称，最大长度为20个字符，仅限中文、字母、数字和下划线组成。", necessity = false, type = InputType.input)
    @SerializedName("Name")
    @Expose
    public String name;

    @ParameterValue(info = "(Description)角色描述，最大长度为50个字符", necessity = false, type = InputType.input)
    @SerializedName("Description")
    @Expose
    public String description;

    @ParameterValue(info = "(IsGroupRole)角色类型，0:saas角色，1:集团角色\n" +
            "默认0，saas角色", necessity = false, type = InputType.number)
    @SerializedName("IsGroupRole")
    @Expose
    public Long isGroupRole;

    @ParameterValue(info = "(PermissionGroups)权限树", necessity = false, type = InputType.listMap)
    @SerializedName("PermissionGroups")
    @Expose
    public List<Map> permissionGroups;

    @ParameterValue(info = "(SubOrganizationIds)集团角色的话，需要传递集团子企业列表，如果是全选，则传1", necessity = false, type = InputType.input)
    @SerializedName("SubOrganizationIds")
    @Expose
    public String subOrganizationIds;

}
