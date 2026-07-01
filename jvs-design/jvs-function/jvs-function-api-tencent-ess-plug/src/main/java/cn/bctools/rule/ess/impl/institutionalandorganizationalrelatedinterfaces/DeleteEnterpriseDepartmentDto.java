package cn.bctools.rule.ess.impl.institutionalandorganizationalrelatedinterfaces;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.rule.ess.impl.TencenCloudApiSelected;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
public class DeleteEnterpriseDepartmentDto {

    @NotNull(message = "帐号配置不能为空")
    @ParameterValue(info = "帐号配置", type = InputType.selected, cls = TencenCloudApiSelected.class)
    public String options;

    @ParameterValue(info = "(operator)执行本接口操作的员工信息。", necessity = false, type = InputType.map)
    public Map operator;

    @ParameterValue(info = "(DeptId)电子签中的部门ID，通过DescribeIntegrationDepartments接口可获得。", necessity = false, type = InputType.input)
    @SerializedName("DeptId")
    @Expose
    public  String deptId;

    @ParameterValue(info = "(ReceiveDeptId)交接部门ID。\n" +
            "待删除部门中的合同、印章和模板数据，将会被交接至该部门ID下；若未填写则交接至公司根部门。", necessity = false, type = InputType.input)
    @SerializedName("ReceiveDeptId")
    @Expose
    public  String receiveDeptId;
}
