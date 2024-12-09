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
public class UpdateEnterpriseEmployeeInformationDto {

    @NotNull(message = "帐号配置不能为空")
    @ParameterValue(info = "帐号配置", type = InputType.selected, cls = TencenCloudApiSelected.class)
    public String options;

    @ParameterValue(info = "(operator)执行本接口操作的员工信息。", necessity = false, type = InputType.map)
    public Map operator;

    @ParameterValue(info = "(Employees)员工信息，不超过100个。\n" +
            "根据UserId或OpenId更新员工，必填一个，优先UserId。\n" +
            "可更新Mobile、DisplayName、Email和Department.DepartmentId字段，其他字段暂不支持", type = InputType.listMap)
    @SerializedName("Employees")
    @Expose
    public  List<Map> employees;

}
