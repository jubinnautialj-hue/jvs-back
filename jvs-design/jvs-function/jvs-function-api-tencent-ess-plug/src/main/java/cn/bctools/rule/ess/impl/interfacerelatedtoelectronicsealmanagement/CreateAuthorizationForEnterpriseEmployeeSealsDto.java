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
public class CreateAuthorizationForEnterpriseEmployeeSealsDto {
    @NotNull(message = "帐号配置不能为空")
    @ParameterValue(info = "帐号配置", type = InputType.selected, cls = TencenCloudApiSelected.class)
    public String options;

    @ParameterValue(info = "(operator)执行本接口操作的员工信息。", necessity = false, type = InputType.map)
    public Map operator;

    @ParameterValue(info = "(Users)用户在电子文件签署平台标识信息，具体参考UserInfo结构体。可跟下面的UserIds可叠加起作用", type = InputType.listMap)
    @SerializedName("Users")
    @Expose
    public  List<Map> users;

    @ParameterValue(info = "(SealId)电子印章ID，为32位字符串。\n" +
            "建议开发者保留此印章ID，后续指定签署区印章或者操作印章需此印章ID。\n" +
            "可登录腾讯电子签控制台，在 \"印章\"->\"印章中心\"选择查看的印章，在\"印章详情\" 中查看某个印章的SealId(在页面中展示为印章ID)。", necessity = false, type = InputType.input)
    @SerializedName("SealId")
    @Expose
    public  String sealId;

    @ParameterValue(info = "(Expired)授权有效期。时间戳秒级", necessity = false, type = InputType.number)
    @SerializedName("Expired")
    @Expose
    public  Long expired;

    @ParameterValue(info = "(UserIds)需要授权的用户UserId集合。跟上面的SealId参数配合使用。选填，跟上面的Users同时起作用", necessity = false, type = InputType.list)
    @SerializedName("UserIds")
    @Expose
    public  List<String> userIds;

    @ParameterValue(info = "(Policy)印章授权内容", necessity = false, type = InputType.input)
    @SerializedName("Policy")
    @Expose
    public  String policy;

}
