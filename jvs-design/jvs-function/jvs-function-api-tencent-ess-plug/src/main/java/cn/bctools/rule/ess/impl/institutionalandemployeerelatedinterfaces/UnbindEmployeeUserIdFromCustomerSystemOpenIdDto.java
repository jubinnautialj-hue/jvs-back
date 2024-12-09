package cn.bctools.rule.ess.impl.institutionalandemployeerelatedinterfaces;

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
public class UnbindEmployeeUserIdFromCustomerSystemOpenIdDto {

    @NotNull(message = "帐号配置不能为空")
    @ParameterValue(info = "帐号配置", type = InputType.selected, cls = TencenCloudApiSelected.class)
    public String options;

    @ParameterValue(info = "(operator)执行本接口操作的员工信息。", necessity = false, type = InputType.map)
    public Map operator;

    @ParameterValue(info = "(UserId)员工在腾讯电子签平台的唯一身份标识，为32位字符串。\n" +
            "可登录腾讯电子签控制台，在 \"更多能力\"->\"组织管理\" 中查看某位员工的UserId(在页面中展示为用户ID)。", necessity = false, type = InputType.input)
    @SerializedName("UserId")
    @Expose
    public  String userId;

    @ParameterValue(info = "(OpenId)员工在贵司业务系统中的唯一身份标识，用于与腾讯电子签账号进行映射，确保在同一企业内不会出现重复。\n" +
            "该标识最大长度为64位字符串，仅支持包含26个英文字母和数字0-9的字符。", necessity = false, type = InputType.input)
    @SerializedName("OpenId")
    @Expose
    public  String openId;
}
