package cn.bctools.rule.ess.impl.institutionalandorganizationalrelatedinterfaces;

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
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
public class ModifyingEnterpriseCallbackConfigurationDto {
    @NotNull(message = "帐号配置不能为空")
    @ParameterValue(info = "帐号配置", type = InputType.selected, cls = TencenCloudApiSelected.class)
    public String options;

    @ParameterValue(info = "(operator)执行本接口操作的员工信息。", necessity = false, type = InputType.map)
    public Map operator;

    @ParameterValue(info = "(OperateType)操作类型：\n" +
            "1-新增\n" +
            "2-删除", necessity = false, type = InputType.number)
    @SerializedName("OperateType")
    @Expose
    public Long operateType;

    @ParameterValue(info = "(CallbackInfo)企业应用回调信息", necessity = false, type = InputType.map)
    @SerializedName("CallbackInfo")
    @Expose
    public Map callbackInfo;
}
