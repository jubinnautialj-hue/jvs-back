package cn.bctools.rule.ess.impl.billingrelatedinterfaces;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.rule.ess.impl.TencenCloudApiSelected;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
public class QueryTheUsageOfEnterpriseBillingDto {
    @NotNull(message = "帐号配置不能为空")
    @ParameterValue(info = "帐号配置", type = InputType.selected, cls = TencenCloudApiSelected.class)
    public String options;

    @ParameterValue(info = "(StartTime)查询开始时间字符串，格式为yyyymmdd,时间跨度不能大于31天\n" +
            "示例值：20230101", necessity = false, type = InputType.input)
    @SerializedName("StartTime")
    @Expose
    public String startTime;

    @ParameterValue(info = "(EndTime)查询结束时间字符串，格式为yyyymmdd,时间跨度不能大于31天\n" +
            "示例值：20230105", necessity = false, type = InputType.input)
    @SerializedName("EndTime")
    @Expose
    public String endTime;

    @ParameterValue(info = "(Offset)指定分页返回第几页的数据，如果不传默认返回第一页，页码从 0 开始，即首页为 0\n" +
            "示例值：0", necessity = false, type = InputType.number)
    @SerializedName("Offset")
    @Expose
    public Long offset;

    @ParameterValue(info = "(Limit)指定分页每页返回的数据条数，如果不传默认为 50，单页最大支持 50。\n" +
            "示例值：50", necessity = false, type = InputType.number)
    @SerializedName("Limit")
    @Expose
    public Long limit;

    @ParameterValue(info = "(QuotaType)查询的套餐类型 （选填 ）不传则查询所有套餐；\n" +
            "目前支持:\n" +
            "\n" +
            "CloudEnterprise: 企业版合同\n" +
            "SingleSignature: 单方签章\n" +
            "CloudProve: 签署报告\n" +
            "CloudOnlineSign: 腾讯会议在线签约\n" +
            "ChannelWeCard: 微工卡\n" +
            "SignFlow: 合同套餐\n" +
            "SignFace: 签署意愿（人脸识别）\n" +
            "SignPassword: 签署意愿（密码）\n" +
            "SignSMS: 签署意愿（短信）\n" +
            "PersonalEssAuth: 签署人实名（腾讯电子签认证）\n" +
            "PersonalThirdAuth: 签署人实名（信任第三方认证）\n" +
            "OrgEssAuth: 签署企业实名\n" +
            "FlowNotify: 短信通知\n" +
            "AuthService: 企业工商信息查询\n" +
            "\n" +
            "\n" +
            "示例值：CloudEnterprise", necessity = false, type = InputType.input)
    @SerializedName("QuotaType")
    @Expose
    public String quotaType;
}
