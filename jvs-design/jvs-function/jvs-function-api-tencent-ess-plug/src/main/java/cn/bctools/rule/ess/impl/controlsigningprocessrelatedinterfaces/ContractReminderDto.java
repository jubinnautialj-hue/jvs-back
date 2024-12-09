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
 */
@Data
@Accessors(chain = true)
public class ContractReminderDto {

    @NotNull(message = "帐号配置不能为空")
    @ParameterValue(info = "帐号配置", type = InputType.selected, cls = TencenCloudApiSelected.class)
    public String options;
    @ParameterValue(info = "(operator)执行本接口操作的员工信息。", necessity = false, type = InputType.map)
    public Map operator;

    @ParameterValue(info = "(FlowIds)需要查询的流程ID列表，限制最大100个。", type = InputType.list)
    /**
     * 需要查询的流程ID列表，限制最大100个

     如果查询合同组的信息,不要传此参数
     */
    @SerializedName("FlowIds")
    @Expose
    public List<String> flowIds;

}
