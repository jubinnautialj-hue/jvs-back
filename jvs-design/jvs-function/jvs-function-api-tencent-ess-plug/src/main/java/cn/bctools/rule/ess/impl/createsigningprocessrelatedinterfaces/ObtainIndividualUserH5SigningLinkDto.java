package cn.bctools.rule.ess.impl.createsigningprocessrelatedinterfaces;

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
 * The type Obtain individual user h 5 signing link dto.
 *
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class ObtainIndividualUserH5SigningLinkDto {

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
    @ParameterValue(info = "(<)批量签署的合同流程ID数组。\n" +
            "注: `在调用此接口时，请确保合同流程均为本企业发起，且合同数量不超过100个。`", type = InputType.list)

    /**
     * 批量签署的合同流程ID数组。
     * 注: `在调用此接口时，请确保合同流程均为本企业发起，且合同数量不超过100个。`
     */
    @SerializedName("FlowIds")
    @Expose
    public List<String> flowIds;

    /**
     * The Flow id.
     */
    @ParameterValue(info = "(FlowId)合同流程ID，为32位字符串。\n" +
            "建议开发者妥善保存此流程ID，以便于顺利进行后续操作。\n" +
            "可登录腾讯电子签控制台，在 \"合同\"->\"合同中心\" 中查看某个合同的FlowId(在页面中展示为合同ID)。", necessity = false, type = InputType.input)

    /**
     * 合同流程ID，为32位字符串。
     * 建议开发者妥善保存此流程ID，以便于顺利进行后续操作。
     * 可登录腾讯电子签控制台，在 "合同"->"合同中心" 中查看某个合同的FlowId(在页面中展示为合同ID)。
     */
    @SerializedName("FlowId")
    @Expose
    public String flowId;

    /**
     * The Flow approver infos.
     */
    @ParameterValue(info = "(FlowApproverInfos)流程签署人列表，其中结构体的ApproverName，ApproverMobile和ApproverType必传，其他可不传，" +
            "`1. ApproverType目前只支持个人类型的签署人。`\n" +
            "`2. 签署人只能有手写签名和时间类型的签署控件，其他类型的填写控件和签署控件暂时都未支持。`", type = InputType.listMap)

    /**
     * 流程签署人列表，其中结构体的ApproverName，ApproverMobile和ApproverType必传，其他可不传，
     * <p>
     * 注:
     * `1. ApproverType目前只支持个人类型的签署人。`
     * `2. 签署人只能有手写签名和时间类型的签署控件，其他类型的填写控件和签署控件暂时都未支持。`
     */
    @SerializedName("FlowApproverInfos")
    @Expose
    public List flowApproverInfos;

    /**
     * The Jump url.
     */
    @ParameterValue(info = "(JumpUrl)签署完之后的H5页面的跳转链接，此链接及支持http://和https://，最大长度1000个字符。(建议https协议)。", necessity = false, type = InputType.input)

    /**
     * 签署完之后的H5页面的跳转链接，此链接及支持http://和https://，最大长度1000个字符。(建议https协议)
     */
    @SerializedName("JumpUrl")
    @Expose
    public String jumpUrl;
}
