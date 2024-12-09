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
public class ObtainListOfEnterpriseDepartmentInformationDto {

    @NotNull(message = "帐号配置不能为空")
    @ParameterValue(info = "帐号配置", type = InputType.selected, cls = TencenCloudApiSelected.class)
    public String options;

    @ParameterValue(info = "(operator)执行本接口操作的员工信息。", necessity = false, type = InputType.map)
    public Map operator;

    @ParameterValue(info = "(QueryType)查询类型，支持以下类型：\n" +
            "0：查询单个部门节点列表，不包含子节点部门信息\n" +
            "1：查询单个部门节点级一级子节点部门信息列表", necessity = false, type = InputType.number)
    @SerializedName("QueryType")
    @Expose
    public  Long queryType;

    @ParameterValue(info = "(DeptId)查询的部门ID。\n" +
            "注：如果同时指定了DeptId与DeptOpenId参数，系统将优先使用DeptId参数进行查询。当二者都未指定时，系统将返回根节点部门数据。", necessity = false, type = InputType.input)
    @SerializedName("DeptId")
    @Expose
    public  String deptId;

    @ParameterValue(info = "(DeptOpenId)查询的客户系统部门ID。\n" +
            "注：如果同时指定了DeptId与DeptOpenId参数，系统将优先使用DeptId参数进行查询。当二者都未指定时，系统将返回根节点部门数据。", necessity = false, type = InputType.input)
    @SerializedName("DeptOpenId")
    @Expose
    public  String deptOpenId;
}
