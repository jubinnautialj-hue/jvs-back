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
 * @author jvs
 * The type Revoking single contract process dto.
 */
@Data
@Accessors(chain = true)
public class RevokingSingleContractProcessDto {
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
    @ParameterValue(info = "(FlowId)合同流程ID, 为32位字符串。可登录腾讯电子签控制台，在 \"合同\"->\"合同中心\" 中查看某个合同的FlowId(在页面中展示为合同ID)。", necessity = false, type = InputType.input)

    /**
     * 合同流程ID, 为32位字符串。可登录腾讯电子签控制台，在 "合同"->"合同中心" 中查看某个合同的FlowId(在页面中展示为合同ID)。
     */
    @SerializedName("FlowId")
    @Expose
    public String flowId;
    /**
     * The Cancel message.
     */
    @ParameterValue(info = "(CancelMessage)撤销此合同流程的原因，最多支持200个字符长度。只能由中文、字母、数字、中文标点和英文标点组成（不支持表情）。", necessity = false, type = InputType.input)
    /**
     * 撤销此合同流程的原因，最多支持200个字符长度。只能由中文、字母、数字、中文标点和英文标点组成（不支持表情）。
     */
    @SerializedName("CancelMessage")
    @Expose
    public String cancelMessage;

}
