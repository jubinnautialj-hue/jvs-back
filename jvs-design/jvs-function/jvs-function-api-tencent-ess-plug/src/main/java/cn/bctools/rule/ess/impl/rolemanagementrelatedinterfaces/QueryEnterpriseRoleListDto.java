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
public class QueryEnterpriseRoleListDto {
    @NotNull(message = "帐号配置不能为空")
    @ParameterValue(info = "帐号配置", type = InputType.selected, cls = TencenCloudApiSelected.class)
    public String options;

    @ParameterValue(info = "(operator)执行本接口操作的员工信息。", necessity = false, type = InputType.map)
    public Map operator;

    @ParameterValue(info = "(Limit)指定分页每页返回的数据条数，单页最大支持 200。", necessity = false, type = InputType.number)
    @SerializedName("Limit")
    @Expose
    public  Long limit;

    @ParameterValue(info = "(Filters)查询的关键字段，支持Key-Value单值查询。可选键值对如下：\n" +
            "\n" +
            "Key:\"RoleType\"，查询角色类型，Values可选：\n" +
            "\"1\"：查询系统角色\n" +
            "\"2\"：查询自定义角色\n" +
            "\n" +
            "Key:\"RoleStatus\"，查询角色状态，Values可选：\n" +
            "\"1\"：查询启用角色\n" +
            "\"2\"：查询禁用角色\n" +
            "\n" +
            "Key:\"IsGroupRole\"，是否查询集团角色，Values可选：\n" +
            "\"0\"：查询非集团角色\n" +
            "\"1\"：查询集团角色\n" +
            "\n" +
            "Key:\"IsReturnPermissionGroup\"，是否返回角色对应权限树，Values可选：\n" +
            "\"0\"：接口不返回角色对应的权限树字段\n" +
            "\"1\"：接口返回角色对应的权限树字段。", necessity = false, type = InputType.listMap)
    @SerializedName("Filters")
    @Expose
    public  List<Map> filters;

    @ParameterValue(info = "(Offset)指定分页返回第几页的数据，如果不传默认返回第一页。页码从 0 开始，即首页为 0，最大2000。", necessity = false, type = InputType.number)
    @SerializedName("Offset")
    @Expose
    public  Long offset;

}
