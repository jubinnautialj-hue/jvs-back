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
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class CreateContractGroupSigningProcessThroughMultipleFilesDto {

    @NotNull(message = "帐号配置不能为空")
    @ParameterValue(info = "帐号配置", type = InputType.selected, cls = TencenCloudApiSelected.class)
    public String options;

    @ParameterValue(info = "(operator)执行本接口操作的员工信息。", necessity = false, type = InputType.map)
    public Map operator;

    @ParameterValue(info = "(FlowGroupName)合同（流程）组名称（可自定义此名称）", explain = "长度不能超过200，只能由中文、字母、数字和下划线组成。", necessity = false, type = InputType.input)

    /**
     * 合同（流程）组名称（可自定义此名称），长度不能超过200，只能由中文、字母、数字和下划线组成。
     */
    @SerializedName("FlowGroupName")
    @Expose
    public String flowGroupName;

    @ParameterValue(info = "(FlowGroupInfos)合同（流程）组的子合同信息。", explain = "支持2-50个子合同", type = InputType.listMap)
    /**
     * 合同（流程）组的子合同信息，支持2-50个子合同
     */
    @SerializedName("FlowGroupInfos")
    @Expose
    public List flowGroupInfos;

    @ParameterValue(info = "(FlowGroupOptions)合同（流程）组的配置项信息。", explain = "是否通知本企业签署方 。是否通知其他签署方", necessity = false, type = InputType.map)
    /**
     * 合同（流程）组的配置项信息。
     其中包括：
     <ul>
     <li>是否通知本企业签署方</li>
     <li>是否通知其他签署方</li>
     </ul>
     */
    @SerializedName("FlowGroupOptions")
    @Expose
    public Map flowGroupOptions;
}
