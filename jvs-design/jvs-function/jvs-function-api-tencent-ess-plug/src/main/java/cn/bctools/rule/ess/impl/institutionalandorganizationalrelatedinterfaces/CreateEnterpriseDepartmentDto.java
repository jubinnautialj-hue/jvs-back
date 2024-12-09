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
public class CreateEnterpriseDepartmentDto {
    @NotNull(message = "帐号配置不能为空")
    @ParameterValue(info = "帐号配置", type = InputType.selected, cls = TencenCloudApiSelected.class)
    public String options;

    @ParameterValue(info = "(operator)执行本接口操作的员工信息。", necessity = false, type = InputType.map)
    public Map operator;

    @ParameterValue(info = "(DeptName)部门名称，最大长度为50个字符。", necessity = false, type = InputType.input)
    @SerializedName("DeptName")
    @Expose
    public  String deptName;

    @ParameterValue(info = "(ParentDeptId)电子签父部门ID。\n" +
            "注：如果同时指定了ParentDeptId与ParentDeptOpenId参数，系统将优先使用ParentDeptId。当二者都未指定时，创建的新部门将自动填充至根节点部门下。", necessity = false, type = InputType.input)
    @SerializedName("ParentDeptId")
    @Expose
    public  String parentDeptId;

    @ParameterValue(info = "(ParentDeptOpenId)第三方平台中父部门ID。\n" +
            "注：如果同时指定了ParentDeptId与ParentDeptOpenId参数，系统将优先使用ParentDeptId。当二者都未指定时，创建的新部门将自动填充至根节点部门下。", necessity = false, type = InputType.input)
    @SerializedName("ParentDeptOpenId")
    @Expose
    public  String parentDeptOpenId;

    @ParameterValue(info = "(DeptOpenId)客户系统部门ID，最大长度为64个字符。", necessity = false, type = InputType.input)
    @SerializedName("DeptOpenId")
    @Expose
    public  String deptOpenId;

    @ParameterValue(info = "(OrderNo)排序号，支持设置的数值范围为1~30000。同一父部门下，排序号越大，部门顺序越靠前。", necessity = false, type = InputType.input)
    @SerializedName("OrderNo")
    @Expose
    public  Long orderNo;
}
