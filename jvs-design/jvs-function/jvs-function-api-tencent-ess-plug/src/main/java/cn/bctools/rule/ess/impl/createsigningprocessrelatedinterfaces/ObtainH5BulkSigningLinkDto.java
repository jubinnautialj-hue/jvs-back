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
 * The type Obtain h 5 bulk signing link dto.
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class ObtainH5BulkSigningLinkDto {

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
    @ParameterValue(info = "(FlowIds)批量签署的合同流程ID数组。\n" +
            "注: `在调用此接口时，请确保合同流程均为本企业发起，且合同数量不超过100个。`", type = InputType.list)

    /**
     * 批量签署的合同流程ID数组。
     * 注: `在调用此接口时，请确保合同流程均为本企业发起，且合同数量不超过100个。`
     */
    @SerializedName("FlowIds")
    @Expose
    public List<String> flowIds;

    /**
     * The Flow approver info.
     */
    @ParameterValue(info = "(FlowApproverInfo)批量签署的流程签署人，其中姓名(ApproverName)、参与人类型(ApproverType)必传，手机号(ApproverMobile)和证件信息(ApproverIdCardType、ApproverIdCardNumber)可任选一种或全部传入。\n" +
            "注:\n" +
            "`1. ApproverType目前只支持个人类型的签署人。`\n" +
            "`2. 签署人只能有手写签名和时间类型的签署控件，其他类型的填写控件和签署控件暂时都未支持。`\n" +
            "`3. 当需要通过短信验证码签署时，手机号ApproverMobile需要与发起合同时填写的用户手机号一致。`", necessity = false, type = InputType.map)

    /**
     * 批量签署的流程签署人，其中姓名(ApproverName)、参与人类型(ApproverType)必传，手机号(ApproverMobile)和证件信息(ApproverIdCardType、ApproverIdCardNumber)可任选一种或全部传入。
     * 注:
     * `1. ApproverType目前只支持个人类型的签署人。`
     * `2. 签署人只能有手写签名和时间类型的签署控件，其他类型的填写控件和签署控件暂时都未支持。`
     * `3. 当需要通过短信验证码签署时，手机号ApproverMobile需要与发起合同时填写的用户手机号一致。`
     */
    @SerializedName("FlowApproverInfo")
    @Expose
    public Map flowApproverInfo;

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

    /**
     * The Signature types.
     */
    @ParameterValue(info = "(SignatureTypes)指定批量签署合同的签名类型，可传递以下值：0  手写签名(默认) 1 OCR楷体 ,默认情况下，签名类型为手写签名 您可以传递多种值，表示可用多种签名类型。", type = InputType.list)

    /**
     * 指定批量签署合同的签名类型，可传递以下值：
     * <ul><li>**0**：手写签名(默认)</li>
     * <li>**1**：OCR楷体</li></ul>
     * 注：
     * <ul><li>默认情况下，签名类型为手写签名</li>
     * <li>您可以传递多种值，表示可用多种签名类型。</li></ul>
     */
    @SerializedName("SignatureTypes")
    @Expose
    public List<Long> signatureTypes;

    /**
     * The Approver sign types.
     */
    @ParameterValue(info = "(ApproverSignTypes)指定批量签署合同的认证校验方式，可传递以下值：" +
            "1 人脸认证(默认)，需进行人脸识别成功后才能签署合同" +
            "2 密码认证(默认)，需校验成功后才能签署合同" +
            "3 运营商三要素，需到运营商处比对手机号实名信息(名字、手机号、证件号)校验一致才能成功进行合同签署。", type = InputType.list)

    /**
     * 指定批量签署合同的认证校验方式，可传递以下值：
     * <ul><li>**1**：人脸认证(默认)，需进行人脸识别成功后才能签署合同</li>
     * <li>**2**：密码认证(默认)，需校验成功后才能签署合同</li>
     * <li>**3**：运营商三要素，需到运营商处比对手机号实名信息(名字、手机号、证件号)校验一致才能成功进行合同签署。</li></ul>
     * 注：
     * <ul><li>默认情况下，认证校验方式为人脸和密码认证</li>
     * <li>您可以传递多种值，表示可用多种认证校验方式。</li></ul>
     */
    @SerializedName("ApproverSignTypes")
    @Expose
    public List<Long> approverSignTypes;

}
