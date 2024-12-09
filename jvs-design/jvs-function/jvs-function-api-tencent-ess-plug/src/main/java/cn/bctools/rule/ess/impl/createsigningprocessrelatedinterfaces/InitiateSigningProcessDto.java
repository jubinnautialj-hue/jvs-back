package cn.bctools.rule.ess.impl.createsigningprocessrelatedinterfaces;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.rule.ess.impl.TencenCloudApiSelected;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * The type Initiate signing process dto.
 *
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class InitiateSigningProcessDto {

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
    @ParameterValue(info = "(flowId)合同流程ID，为32位字符串。", necessity = false, type = InputType.input)
    public String flowId;
    /**
     * The Cc notify type.
     */
    @ParameterValue(info = "(ccNotifyType)若在创建签署流程时指定了关注人CcInfos，此参数可设定向关注人发送短信通知的类型：\n" +
            "0 :合同发起时通知通知对方来查看合同（默认）\n" +
            "1 : 签署完成后通知对方来查看合同", necessity = false, type = InputType.number)
    public Long ccNotifyType;

}
