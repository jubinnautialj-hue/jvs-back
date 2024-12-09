package cn.bctools.rule.ess.impl.institutionalandemployeerelatedinterfaces;

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
public class CreatingEnterpriseEmployeesDto {

    @NotNull(message = "帐号配置不能为空")
    @ParameterValue(info = "帐号配置", type = InputType.selected, cls = TencenCloudApiSelected.class)
    public String options;

    @ParameterValue(info = "(operator)执行本接口操作的员工信息。", necessity = false, type = InputType.map)
    public Map operator;

    @ParameterValue(info = "(Employees)待创建员工的信息，最多不超过20个。\n" +
            "其中入参Mobile和DisplayName必填，OpenId、Email和Department.DepartmentId选填，其他字段暂不支持设置。\n" +
            "在创建企微企业员工场景下，只需传入WeworkOpenId，无需再传其他信息。", type = InputType.listMap)
    @SerializedName("Employees")
    @Expose
    public List<Map> employees;

}
