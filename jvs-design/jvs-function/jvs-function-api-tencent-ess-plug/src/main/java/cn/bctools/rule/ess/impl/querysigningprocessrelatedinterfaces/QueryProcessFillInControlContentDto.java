package cn.bctools.rule.ess.impl.querysigningprocessrelatedinterfaces;

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
 * The type Query process fill in control content dto.
 */
@Data
@Accessors(chain = true)
public class QueryProcessFillInControlContentDto {

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

}
