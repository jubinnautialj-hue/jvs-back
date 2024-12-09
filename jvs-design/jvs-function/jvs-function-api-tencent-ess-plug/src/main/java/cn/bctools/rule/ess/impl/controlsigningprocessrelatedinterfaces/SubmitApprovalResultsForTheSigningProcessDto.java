package cn.bctools.rule.ess.impl.controlsigningprocessrelatedinterfaces;

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
 * @author jvsThe type Submit approval results for the signing process dto.
 */
@Data
@Accessors(chain = true)
public class SubmitApprovalResultsForTheSigningProcessDto {

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
     * The Review type.
     */
    @ParameterValue(info = "(ReviewType)企业审核结果。PASS: 通过  REJECT: 拒绝", necessity = false, type = InputType.input)

    /**
     * 企业审核结果
     * <ul><li>PASS: 通过</li>
     * <li>REJECT: 拒绝</li></ul>
     */
    @SerializedName("ReviewType")
    @Expose
    public String reviewType;
    /**
     * The Review message.
     */
    @ParameterValue(info = "(ReviewMessage)审核结果原因，\n" +
            "字符串长度不超过200\n" +
            "当ReviewType 是拒绝（REJECT） 时此字段必填。", necessity = false, type = InputType.input)

    /**
     * 审核结果原因，
     * 字符串长度不超过200
     * 当ReviewType 是拒绝（REJECT） 时此字段必填。
     */
    @SerializedName("ReviewMessage")
    @Expose
    public String reviewMessage;
    /**
     * The Recipient id.
     */
    @ParameterValue(info = "(RecipientId)审核签署节点人标识，\n" +
            "用来标识审核的签署方。\n" +
            "如果签署审核节点是个人， 此参数必填。", necessity = false, type = InputType.input)

    /**
     * 审核签署节点人标识，
     * 用来标识审核的签署方。
     * 如果签署审核节点是个人， 此参数必填。
     */
    @SerializedName("RecipientId")
    @Expose
    public String recipientId;
    /**
     * The Operate type.
     */
    @ParameterValue(info = "(OperateType)操作类型：（接口通过该字段区分不同的操作类型）SignReview: 签署审核（默认）CreateReview: 创建审核   ", necessity = false, type = InputType.input)

    /**
     * 操作类型：（接口通过该字段区分不同的操作类型）
     *
     * <ul><li>SignReview: 签署审核（默认）</li>
     * <li>CreateReview: 创建审核</li></ul>
     * <p>
     * 如果审核节点是个人，则操作类型只能为SignReview。
     */
    @SerializedName("OperateType")
    @Expose
    public String operateType;


}
