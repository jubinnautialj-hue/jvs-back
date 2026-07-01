package cn.bctools.rule.ess.impl.interfacerelatedtoelectronicsealmanagement;

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
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
public class RevokingSealAuthorizationForEnterpriseEmployeesDto {

    @NotNull(message = "帐号配置不能为空")
    @ParameterValue(info = "帐号配置", type = InputType.selected, cls = TencenCloudApiSelected.class)
    public String options;

    @ParameterValue(info = "(operator)执行本接口操作的员工信息。", necessity = false, type = InputType.map)
    public Map operator;

    @ParameterValue(info = "(PolicyIds)印章授权编码数组。这个参数跟下面的SealId其中一个必填，另外一个可选填", necessity = false, type = InputType.list)
    @SerializedName("PolicyIds")
    @Expose
    public  List<String> policyIds;

    @ParameterValue(info = "(SealId)电子印章ID，为32位字符串。\n" +
            "建议开发者保留此印章ID，后续指定签署区印章或者操作印章需此印章ID。\n" +
            "可登录腾讯电子签控制台，在 \"印章\"->\"印章中心\"选择查看的印章，在\"印章详情\" 中查看某个印章的SealId(在页面中展示为印章ID)。\n" +
            "注：印章ID。这个参数跟上面的PolicyIds其中一个必填，另外一个可选填。", necessity = false, type = InputType.input)
    @SerializedName("SealId")
    @Expose
    public  String sealId;

    @ParameterValue(info = "(UserIds)待授权的员工ID，员工在腾讯电子签平台的唯一身份标识，为32位字符串。\n" +
            "可登录腾讯电子签控制台，在 \"更多能力\"->\"组织管理\" 中查看某位员工的UserId(在页面中展示为用户ID)。", necessity = false, type = InputType.list)
    @SerializedName("UserIds")
    @Expose
    public  List<String> userIds;

}
