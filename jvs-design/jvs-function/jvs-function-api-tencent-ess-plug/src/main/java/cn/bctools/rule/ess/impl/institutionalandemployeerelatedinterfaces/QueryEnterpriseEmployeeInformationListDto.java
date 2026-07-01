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
public class QueryEnterpriseEmployeeInformationListDto {

    @NotNull(message = "帐号配置不能为空")
    @ParameterValue(info = "帐号配置", type = InputType.selected, cls = TencenCloudApiSelected.class)
    public String options;

    @ParameterValue(info = "(operator)执行本接口操作的员工信息。", necessity = false, type = InputType.map)
    public Map operator;

    @ParameterValue(info = "(Limit)指定分页每页返回的数据条数，单页最大支持 20。", necessity = false, type = InputType.number)
    @SerializedName("Limit")
    @Expose
    public  Long limit;

    @ParameterValue(info = "(Filters)查询的关键字段，支持Key-Values查询。可选键值如下：\n" +
            "\n" +
            "Key:\"Status\"，根据实名状态查询员工，Values可选：\n" +
            "[\"IsVerified\"]：查询已实名的员工\n" +
            "[\"NotVerified\"]：查询未实名的员工\n" +
            "\n" +
            "Key:\"DepartmentId\"，根据部门ID查询部门下的员工，Values为指定的部门ID：[\"DepartmentId\"]\n" +
            "\n" +
            "Key:\"UserId\"，根据用户ID查询员工，Values为指定的用户ID：[\"UserId\"]\n" +
            "\n" +
            "Key:\"UserWeWorkOpenId\"，根据用户企微账号ID查询员工，Values为指定用户的企微账号ID：[\"UserWeWorkOpenId\"]\n" +
            "\n" +
            "Key:\"StaffOpenId\"，根据第三方系统用户OpenId查询员工，Values为第三方系统用户OpenId列表：[\"OpenId1\",\"OpenId2\",...]\n" +
            "\n" +
            "Key:\"RoleId\"，根据电子签角色ID查询员工，Values为指定的角色ID，满足其中任意一个角色即可：[\"RoleId1\",\"RoleId2\",...]\n", necessity = false, type = InputType.listMap)
    @SerializedName("Filters")
    @Expose
    public  List<Map> filters;

    @ParameterValue(info = "(Offset)指定分页返回第几页的数据，如果不传默认返回第一页。页码从 0 开始，即首页为 0，最大20000。", necessity = false, type = InputType.number)
    @SerializedName("Offset")
    @Expose
    public  Long offset;

}
