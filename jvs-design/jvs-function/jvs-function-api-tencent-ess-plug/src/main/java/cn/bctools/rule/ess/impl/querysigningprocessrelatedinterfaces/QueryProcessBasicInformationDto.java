package cn.bctools.rule.ess.impl.querysigningprocessrelatedinterfaces;

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
 * The type Query process basic information dto.
 */
@Data
@Accessors(chain = true)
public class QueryProcessBasicInformationDto {

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
    @ParameterValue(info = "(FlowIds)查询的合同流程ID列表最多支持100个流程ID。 \n" +
            "如果某个合同流程ID不存在，系统会跳过此ID的查询，继续查询剩余存在的合同流程。\n" +
            "可登录腾讯电子签控制台，在 \"合同\"->\"合同中心\" 中查看某个合同的FlowId(在页面中展示为合同ID)。", type = InputType.list)

    /**
     * 查询的合同流程ID列表最多支持100个流程ID。
     * 如果某个合同流程ID不存在，系统会跳过此ID的查询，继续查询剩余存在的合同流程。
     * 可登录腾讯电子签控制台，在 "合同"->"合同中心" 中查看某个合同的FlowId(在页面中展示为合同ID)。
     */
    @SerializedName("FlowIds")
    @Expose
    public List<String> flowIds;

}
