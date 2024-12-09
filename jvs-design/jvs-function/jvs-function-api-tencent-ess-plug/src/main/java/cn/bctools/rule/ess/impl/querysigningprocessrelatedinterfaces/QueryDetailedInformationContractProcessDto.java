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
 * The type Query detailed information contract process dto.
 */
@Data
@Accessors(chain = true)
public class QueryDetailedInformationContractProcessDto {

    /**
     * The Options.
     */
    @NotNull(message = "帐号配置不能为空")
    @ParameterValue(info = "帐号配置", type = InputType.selected, cls = TencenCloudApiSelected.class)
    public String options;
    /**
     * The Operator.
     */
    @ParameterValue(info = "执行本接口操作的员工信息。", necessity = false, type = InputType.map)
    public Map operator;

    /**
     * The Flow ids.
     */
    @ParameterValue(info = "需要查询的流程ID列表，限制最大100个。", type = InputType.list)
    /**
     * 需要查询的流程ID列表，限制最大100个

     如果查询合同组的信息,不要传此参数
     */
    @SerializedName("FlowIds")
    @Expose
    public List<String> flowIds;
    /**
     * The Flow group id.
     */
    @ParameterValue(info = "合同组ID, 如果传此参数会忽略FlowIds入参", necessity = false, type = InputType.input)

    /**
     * 合同组ID, 如果传此参数会忽略FlowIds入参
     所以如传此参数不要传FlowIds参数

     */
    @SerializedName("FlowGroupId")
    @Expose
    public String flowGroupId;

}
