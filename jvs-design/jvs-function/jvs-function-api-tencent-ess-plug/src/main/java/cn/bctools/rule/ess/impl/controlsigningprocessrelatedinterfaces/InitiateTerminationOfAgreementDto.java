package cn.bctools.rule.ess.impl.controlsigningprocessrelatedinterfaces;

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
 * The type Initiate termination of agreement dto.
 *
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class InitiateTerminationOfAgreementDto {

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
     * The Need relieved flow id.
     */
    @ParameterValue(info = "(NeedRelievedFlowId)待解除的签署流程编号（即原签署流程的编号）。", necessity = false, type = InputType.input)

    /**
     * 待解除的签署流程编号（即原签署流程的编号）。
     */
    @SerializedName("NeedRelievedFlowId")
    @Expose
    public String needRelievedFlowId;
    /**
     * The Relive info.
     */
    @ParameterValue(info = "(ReliveInfo)解除协议内容, 包括解除理由等信息。", necessity = false, type = InputType.map)

    /**
     * 解除协议内容, 包括解除理由等信息。
     */
    @SerializedName("ReliveInfo")
    @Expose
    public Map reliveInfo;
    /**
     * The Released approvers.
     */
    @ParameterValue(info = "(ReleasedApprovers) 替换解除协议的签署人， 如不指定替换签署人,  则使用原流程的签署人。 <br/>\n" +
            "     如需更换原合同中的企业端签署人，可通过指定该签署人的RecipientId编号更换此企业端签署人", type = InputType.listMap)

    /**
     * 替换解除协议的签署人， 如不指定替换签署人,  则使用原流程的签署人。 <br/>
     如需更换原合同中的企业端签署人，可通过指定该签署人的RecipientId编号更换此企业端签署人。(可通过接口<a href="https://qian.tencent.com/developers/companyApis/queryFlows/DescribeFlowInfo/">DescribeFlowInfo</a>查询签署人的RecipientId编号)<br/>
     注意：
     `只能更换自己企业的签署人,  不支持更换个人类型或者其他企业的签署人。`
     `可以不指定替换签署人, 使用原流程的签署人 `
     */
    @SerializedName("ReleasedApprovers")
    @Expose
    public List releasedApprovers;
    /**
     * The Deadline.
     */
    @ParameterValue(info = "(Deadline)合同流程的签署截止时间，格式为Unix标准时间戳（秒），如果未设置签署截止时间，则默认为合同流程创建后的7天时截止。\n" +
            "如果在签署截止时间前未完成签署，则合同状态会变为已过期，导致合同作废。", necessity = false, type = InputType.number)

    /**
     * 合同流程的签署截止时间，格式为Unix标准时间戳（秒），如果未设置签署截止时间，则默认为合同流程创建后的7天时截止。
     * 如果在签署截止时间前未完成签署，则合同状态会变为已过期，导致合同作废。
     */
    @SerializedName("Deadline")
    @Expose
    public Long deadline;

    /**
     * The User data.
     */
    @ParameterValue(info = "(UserData)调用方自定义的个性化字段，该字段的值可以是字符串JSON或其他字符串形式，客户可以根据自身需求自定义数据格式并在需要时进行解析。该字段的信息将以Base64编码的形式传输，支持的最大数据大小为20480长度。", necessity = false, type = InputType.input)

    /**
     * 调用方自定义的个性化字段，该字段的值可以是字符串JSON或其他字符串形式，客户可以根据自身需求自定义数据格式并在需要时进行解析。该字段的信息将以Base64编码的形式传输，支持的最大数据大小为20480长度。
     * <p>
     * 在合同状态变更的回调信息等场景中，该字段的信息将原封不动地透传给贵方。
     * <p>
     * 回调的相关说明可参考开发者中心的<a href="https://qian.tencent.com/developers/company/callback_types_v2" target="_blank">回调通知</a>模块。
     */
    @SerializedName("UserData")
    @Expose
    public String userData;
}
