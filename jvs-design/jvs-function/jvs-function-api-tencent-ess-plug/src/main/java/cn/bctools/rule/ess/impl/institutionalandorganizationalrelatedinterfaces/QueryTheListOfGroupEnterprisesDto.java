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
public class QueryTheListOfGroupEnterprisesDto {
    @NotNull(message = "帐号配置不能为空")
    @ParameterValue(info = "帐号配置", type = InputType.selected, cls = TencenCloudApiSelected.class)
    public String options;

    @ParameterValue(info = "(operator)执行本接口操作的员工信息。", necessity = false, type = InputType.map)
    public Map operator;

    @ParameterValue(info = "(Limit)指定分页每页返回的数据条数，单页最大1000", necessity = false, type = InputType.number)
    @SerializedName("Limit")
    @Expose
    public  Long limit;

    @ParameterValue(info = "(Offset)指定分页返回第几页的数据，如果不传默认返回第一页，页码从 0 开始，即首页为 0", necessity = false, type = InputType.number)
    @SerializedName("Offset")
    @Expose
    public  Long offset;

    @ParameterValue(info = "(Name)查询成员企业的企业名，模糊匹配", necessity = false, type = InputType.input)
    @SerializedName("Name")
    @Expose
    public  String name;

    @ParameterValue(info = "(Status)成员企业加入集团的当前状态\n" +
            "1：待授权\n" +
            "2：已授权待激活\n" +
            "3：拒绝授权\n" +
            "4：已解除\n" +
            "5：已加入", necessity = false, type = InputType.number)
    @SerializedName("Status")
    @Expose
    public  Long status;

    @ParameterValue(info = "(Export)是否导出当前成员企业数据\n" +
            "false：不导出（默认值）\n" +
            "true：导出", necessity = false, type = InputType.onOff)
    @SerializedName("Export")
    @Expose
    public  Boolean export;

    @ParameterValue(info = "(Id)成员企业机构 ID，32 位字符串，在PC控制台 集团管理可获取", necessity = false, type = InputType.input)
    @SerializedName("Id")
    @Expose
    public  String id;

}
