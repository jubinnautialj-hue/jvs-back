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
 * The type Create one code multi scan process to sign qr code dto.
 *
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class CreateOneCodeMultiScanProcessToSignQRCodeDto {

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
     * The Template id.
     */
    @ParameterValue(info = "(TemplateId)合同模板ID，为32位字符串。\n" +
            "可登录腾讯电子签控制台，在 \"模板\"->\"模板中心\"->\"列表展示设置\"选中模板 ID 中查看某个模板的TemplateId(在页面中展示为模板ID)。", necessity = false, type = InputType.input)

    /**
     * 合同模板ID，为32位字符串。
     * 可登录腾讯电子签控制台，在 "模板"->"模板中心"->"列表展示设置"选中模板 ID 中查看某个模板的TemplateId(在页面中展示为模板ID)。
     */
    @SerializedName("TemplateId")
    @Expose
    public String templateId;
    /**
     * The Flow name.
     */
    @ParameterValue(info = "(FlowName)合同流程的名称（可自定义此名称），长度不能超过200，只能由中文、字母、数字和下划线组成。\n" +
            "该名称还将用于合同签署完成后的下载文件名。", necessity = false, type = InputType.input)

    /**
     * 合同流程的名称（可自定义此名称），长度不能超过200，只能由中文、字母、数字和下划线组成。
     * 该名称还将用于合同签署完成后的下载文件名。
     */
    @SerializedName("FlowName")
    @Expose
    public String flowName;
    /**
     * The Max flow num.
     */
    @ParameterValue(info = "(MaxFlowNum)通过此二维码可发起的流程最大限额，如未明确指定，默认为5份。\n" +
            "一旦发起流程数超越该限制，该二维码将自动失效。。", necessity = false, type = InputType.number)

    /**
     * 通过此二维码可发起的流程最大限额，如未明确指定，默认为5份。
     * 一旦发起流程数超越该限制，该二维码将自动失效。
     */
    @SerializedName("MaxFlowNum")
    @Expose
    public Long maxFlowNum;
    /**
     * The Qr effective day.
     */
    @ParameterValue(info = "(QrEffectiveDay)二维码的有效期限，默认为7天，最高设定不得超过90天。\n" +
            "一旦超过二维码的有效期限，该二维码将自动失效。", necessity = false, type = InputType.number)

    /**
     * 二维码的有效期限，默认为7天，最高设定不得超过90天。
     * 一旦超过二维码的有效期限，该二维码将自动失效。
     */
    @SerializedName("QrEffectiveDay")
    @Expose
    public Long qrEffectiveDay;
    /**
     * The Flow effective day.
     */
    @ParameterValue(info = "(FlowEffectiveDay)合同流程的签署有效期限，若未设定签署截止日期，则默认为自合同流程创建起的7天内截止。\n" +
            "若在签署截止日期前未完成签署，合同状态将变更为已过期，从而导致合同无效。\n" +
            "最长设定期限不得超过30天。", necessity = false, type = InputType.number)

    /**
     * 合同流程的签署有效期限，若未设定签署截止日期，则默认为自合同流程创建起的7天内截止。
     * 若在签署截止日期前未完成签署，合同状态将变更为已过期，从而导致合同无效。
     * 最长设定期限不得超过30天。
     */
    @SerializedName("FlowEffectiveDay")
    @Expose
    public Long flowEffectiveDay;
    /**
     * The Restrictions.
     */
    @ParameterValue(info = "(Restrictions)指定签署人信息。\n" +
            "在指定签署人后，仅允许特定签署人通过扫描二维码进行签署。", type = InputType.listMap)

    /**
     * 指定签署人信息。
     *  在指定签署人后，仅允许特定签署人通过扫描二维码进行签署。
     */
    @SerializedName("Restrictions")
    @Expose
    public List restrictions;
    /**
     * The User data.
     */
    @ParameterValue(info = "(UserData)执行本接口操作的员工信息。", necessity = false, type = InputType.input)

    /**
     * 调用方自定义的个性化字段(可自定义此字段的值)，并以base64方式编码，支持的最大数据大小为 20480长度。
     * 在合同状态变更的回调信息等场景中，该字段的信息将原封不动地透传给贵方。
     * 回调的相关说明可参考开发者中心的<a href="https://qian.tencent.com/developers/company/callback_types_v2" target="_blank">回调通知</a>模块。
     */
    @SerializedName("UserData")
    @Expose
    public String userData;
    /**
     * The Approver component limit types.
     */
    @ParameterValue(info = "(ApproverComponentLimitTypes)指定签署方在使用个人印章签署控件（SIGN_SIGNATURE） 时可使用的签署方式：自由书写、正楷临摹、系统签名、个人印章。。", type = InputType.listMap)

    /**
     * 指定签署方在使用个人印章签署控件（SIGN_SIGNATURE） 时可使用的签署方式：自由书写、正楷临摹、系统签名、个人印章。
     */
    @SerializedName("ApproverComponentLimitTypes")
    @Expose
    public List approverComponentLimitTypes;
}
