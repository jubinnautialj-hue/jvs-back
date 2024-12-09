package cn.bctools.rule.ess.impl.personalaccountmanagementrelatedinterfaces;

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
public class TurnOffAutomaticSignatureFunctionForIndividualUsersDto {
    @NotNull(message = "帐号配置不能为空")
    @ParameterValue(info = "帐号配置", type = InputType.selected, cls = TencenCloudApiSelected.class)
    public String options;

    @ParameterValue(info = "(operator)执行本接口操作的员工信息。", necessity = false, type = InputType.map)
    public Map operator;

    @ParameterValue(info = "(SceneKey)自动签使用的场景值, 可以选择的场景值如下:\n" +
            "E_PRESCRIPTION_AUTO_SIGN 电子处方\n" +
            "\n" +
            "示例值：E_PRESCRIPTION_AUTO_SIGN", necessity = false, type = InputType.input)
    @SerializedName("SceneKey")
    @Expose
    public  String sceneKey;

    @ParameterValue(info = "(UserInfo)需要关闭自动签的个人的信息，如姓名，证件信息等。", necessity = false, type = InputType.map)
    @SerializedName("UserInfo")
    @Expose
    public  Map userInfo;
}
