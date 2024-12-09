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
public class ObtainTheActivationLinkAutomaticallySignedByIndividualUsersDto {
    @NotNull(message = "帐号配置不能为空")
    @ParameterValue(info = "帐号配置", type = InputType.selected, cls = TencenCloudApiSelected.class)
    public String options;

    @ParameterValue(info = "(operator)执行本接口操作的员工信息。", necessity = false, type = InputType.map)
    public Map operator;

    @ParameterValue(info = "(SceneKey)自动签使用的场景值, 可以选择的场景值如下:\n" +
            "E_PRESCRIPTION_AUTO_SIGN : 电子处方场景\n" +
            "\n" +
            "\n" +
            "注: 现在仅支持电子处方场景\n" +
            "示例值：E_PRESCRIPTION_AUTO_SIGN", necessity = false, type = InputType.input)
    @SerializedName("SceneKey")
    @Expose
    public String sceneKey;

    @ParameterValue(info = "(AutoSignConfig)自动签开通配置信息, 包括开通的人员的信息等。", necessity = false, type = InputType.map)
    @SerializedName("AutoSignConfig")
    @Expose
    public Map autoSignConfig;

    @ParameterValue(info = "(UrlType)生成的链接类型：\n" +
            "不传(即为空值) 则会生成小程序端开通链接(默认)\n" +
            "H5SIGN : 生成H5端开通链接\n" +
            "\n" +
            "示例值：H5SIGN", necessity = false, type = InputType.input)
    @SerializedName("UrlType")
    @Expose
    public String urlType;

    @ParameterValue(info = "(NotifyType)是否通知开通方，通知类型:\n" +
            "默认不设置为不通知开通方\n" +
            "SMS : 短信通知 ,如果需要短信通知则NotifyAddress填写对方的手机号", necessity = false, type = InputType.input)
    @SerializedName("NotifyType")
    @Expose
    public String notifyType;

    @ParameterValue(info = "(NotifyAddress)如果通知类型NotifyType选择为SMS，则此处为手机号, 其他通知类型不需要设置此项\n" +
            "示例值：13200000000。", necessity = false, type = InputType.input)
    @SerializedName("NotifyAddress")
    @Expose
    public String notifyAddress;

    @ParameterValue(info = "(ExpiredTime)链接的过期时间，格式为Unix时间戳，不能早于当前时间，且最大为当前时间往后30天。如果不传，默认过期时间为当前时间往后7天。\n" +
            "示例值：1693290580。", necessity = false, type = InputType.number)
    @SerializedName("ExpiredTime")
    @Expose
    public Long expiredTime;
}
