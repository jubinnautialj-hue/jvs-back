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
 * @author jvs
 * The type Supplementary signing process signatory information dto.
 */
@Data
@Accessors(chain = true)
public class SupplementarySigningProcessSignatoryInformationDto {
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
     * The Flow id.
     */
    @ParameterValue(info = "(FlowId)合同流程ID，为32位字符串。\n" +
            "建议开发者妥善保存此流程ID，以便于顺利进行后续操作。\n" +
            "可登录腾讯电子签控制台，在 \"合同\"->\"合同中心\" 中查看某个合同的FlowId(在页面中展示为合同ID)。。", necessity = false, type = InputType.input)

    /**
     * 合同流程ID，为32位字符串。
     * 建议开发者妥善保存此流程ID，以便于顺利进行后续操作。
     * 可登录腾讯电子签控制台，在 "合同"->"合同中心" 中查看某个合同的FlowId(在页面中展示为合同ID)。
     */
    @SerializedName("FlowId")
    @Expose
    public String flowId;

    /**
     * The Approvers.
     */
    @ParameterValue(info = "(Approvers)补充企业签署人信息。如果发起方指定的补充签署人是企业微信签署人（ApproverSource=WEWORKAPP），则需要提供企业微信UserId进行补充；", type = InputType.listMap)

    /**
     * 补充企业签署人信息。
     * <p>
     * - 如果发起方指定的补充签署人是企业微信签署人（ApproverSource=WEWORKAPP），则需要提供企业微信UserId进行补充；
     * <p>
     * - 如果不指定，则使用姓名和手机号进行补充。
     */
    @SerializedName("Approvers")
    @Expose
    public List approvers;

    /**
     * The Initiator.
     */
    @ParameterValue(info = "(Initiator)在可定制的企业微信通知中，发起人可以根据具体需求进行自定义设置。", necessity = false, type = InputType.input)

    /**
     * 在可定制的企业微信通知中，发起人可以根据具体需求进行自定义设置。
     */
    @SerializedName("Initiator")
    @Expose
    public String initiator;
    /**
     * The Fill approver type.
     */
    @ParameterValue(info = "(FillApproverType)签署人信息补充方式" +
            "**0**: 补充或签人，支持补充多个企业经办签署人（默认）注: `不可补充个人签署人` \n" +
            "**1**: 补充动态签署人，可补充企业和个人签署人。注: `每个签署方节点签署人是唯一的，一个节点只支持传入一个签署人信息` ", necessity = false, type = InputType.number)


    /**
     * 签署人信息补充方式
     *
     * **0**: 补充或签人，支持补充多个企业经办签署人（默认）注: `不可补充个人签署人`
     * **1**: 补充动态签署人，可补充企业和个人签署人。注: `每个签署方节点签署人是唯一的，一个节点只支持传入一个签署人信息`
     */
    @SerializedName("FillApproverType")
    @Expose
    public Long fillApproverType;

}
