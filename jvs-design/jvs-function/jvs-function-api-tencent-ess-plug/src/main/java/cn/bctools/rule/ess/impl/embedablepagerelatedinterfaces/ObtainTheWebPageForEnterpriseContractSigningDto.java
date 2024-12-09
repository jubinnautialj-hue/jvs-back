package cn.bctools.rule.ess.impl.embedablepagerelatedinterfaces;

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
 * @author jvs
 * The type Obtain the web page for enterprise contract signing dto.
 */
@Data
@Accessors(chain = true)
public class ObtainTheWebPageForEnterpriseContractSigningDto {


    /**
     * The Options.
     */
    @NotNull(message = "帐号配置不能为空")
    @ParameterValue(info = "帐号配置", type = InputType.selected, cls = TencenCloudApiSelected.class)
    public String options;

    /**
     * The Operator.
     */
    @ParameterValue(info = "(operator)执行本接口操作的员工信息。", necessity = false, type = InputType.map)
    public Map operator;


    /**
     * The Flow ids.
     */
    @ParameterValue(info = "(FlowIds)需要查询的流程ID列表，限制最大100个。", type = InputType.list)
    /**
     * 需要查询的流程ID列表，限制最大100个

     如果查询合同组的信息,不要传此参数
     */
    @SerializedName("FlowIds")
    @Expose
    public List<String> flowIds;

    /**
     * The User id.
     */
    @ParameterValue(info = "(UserId)员工的UserId，该UserId对应的员工必须已经加入企业并实名，Name和Mobile为空时该字段不能为空。（优先使用UserId对应的员工）", necessity = false, type = InputType.input)

    /**
     * 员工的UserId，该UserId对应的员工必须已经加入企业并实名，Name和Mobile为空时该字段不能为空。（优先使用UserId对应的员工）
     */
    @SerializedName("UserId")
    @Expose
    public String userId;
    /**
     * The Name.
     */
    @ParameterValue(info = "(Name)员工姓名，该字段需要与Mobile组合使用，UserId为空时该字段不能为空。（UserId为空时，使用Name和Mbile对应的员工）", necessity = false, type = InputType.input)

    /**
     * 员工姓名，该字段需要与Mobile组合使用，UserId为空时该字段不能为空。（UserId为空时，使用Name和Mbile对应的员工）
     */
    @SerializedName("Name")
    @Expose
    public String name;
    /**
     * The Mobile.
     */
    @ParameterValue(info = "(Mobile)员工手机号码，该字段需要与Name组合使用，UserId为空时该字段不能为空。（UserId为空时，使用Name和Mbile对应的员工）", necessity = false, type = InputType.input)

    /**
     * 员工手机号码，该字段需要与Name组合使用，UserId为空时该字段不能为空。（UserId为空时，使用Name和Mbile对应的员工）
     */
    @SerializedName("Mobile")
    @Expose
    public String mobile;


}
