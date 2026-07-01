package cn.bctools.rule.ess.impl.embedablepagerelatedinterfaces;

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
 * The type Create web page for initiating the process dto.
 *
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class CreateWebPageForInitiatingTheProcessDto {

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
     * The Resource id.
     */
    @ParameterValue(info = "(ResourceId)资源Id，通过多文件上传（UploadFiles）接口获得", necessity = false, type = InputType.input)

    /**
     * 资源Id，通过多文件上传（UploadFiles）接口获得
     */
    @SerializedName("ResourceId")
    @Expose
    public String resourceId;
    /**
     * The Flow name.
     */
    @ParameterValue(info = "(FlowName)合同名称", necessity = false, type = InputType.input)

    /**
     * 合同名称
     */
    @SerializedName("FlowName")
    @Expose
    public String flowName;
    /**
     * The Unordered.
     */
    @ParameterValue(info = "(Unordered)是否顺序签署\n" +
            "true:无序签\n" +
            "false:顺序签", necessity = false, type = InputType.input)

    /**
     * 是否顺序签署
     * true:无序签
     * false:顺序签
     */
    @SerializedName("Unordered")
    @Expose
    public Boolean unordered;
    /**
     * The Deadline.
     */
    @ParameterValue(info = "(Deadline)签署流程的签署截止时间。\n" +
            "值为unix时间戳,精确到秒\n" +
            "不传默认为当前时间一年后", necessity = false, type = InputType.number)

    /**
     * 签署流程的签署截止时间。
     * 值为unix时间戳,精确到秒
     * 不传默认为当前时间一年后
     */
    @SerializedName("Deadline")
    @Expose
    public Long deadline;
    /**
     * The User flow type id.
     */
    @ParameterValue(info = "(UserFlowTypeId)用户自定义合同类型Id\n" +
            "该id为电子签企业内的合同类型id", necessity = false, type = InputType.input)

    /**
     * 用户自定义合同类型Id
     * 该id为电子签企业内的合同类型id
     */
    @SerializedName("UserFlowTypeId")
    @Expose
    public String userFlowTypeId;


    /**
     * The Approvers.
     */
    @ParameterValue(info = "(Approvers)签署流程参与者信息，最大限制50方。", type = InputType.listMap)

    /**
     * 签署流程参与者信息，最大限制50方
     */
    @SerializedName("Approvers")
    @Expose
    public List approvers;

    /**
     * The Intelligent status.
     */
    @ParameterValue(info = "(IntelligentStatus)打开智能添加填写区", necessity = false, type = InputType.input)

    /**
     * 打开智能添加填写区
     * (默认开启，打开:"OPEN"
     * 关闭："CLOSE"
     */
    @SerializedName("IntelligentStatus")
    @Expose
    public String intelligentStatus;
    /**
     * The Resource type.
     */
    @ParameterValue(info = "(ResourceType)资源类型，\n" +
            "1：文件，\n" +
            "2：模板\n" +
            "不传默认为1：文件\n" +
            "目前仅支持文件", necessity = false, type = InputType.number)

    /**
     * 资源类型，
     * 1：文件，
     * 2：模板
     * 不传默认为1：文件
     * 目前仅支持文件
     */
    @SerializedName("ResourceType")
    @Expose
    public Long resourceType;

    /**
     * The Components.
     */
    @ParameterValue(info = "(Components)发起方填写控件\n" +
            "该类型控件由发起方完成填写", necessity = false, type = InputType.map)
    /**
     * 发起方填写控件
     * 该类型控件由发起方完成填写
     */
    @SerializedName("Components")
    @Expose
    public Map components;

    /**
     * The Flow option.
     */
    @ParameterValue(info = "(FlowOption)发起合同个性化参数\n" +
            "用于满足创建及页面操作过程中的个性化要求\n" +
            "具体定制化内容详见数据接口说明", necessity = false, type = InputType.map)
    /**
     * 发起合同个性化参数
     * 用于满足创建及页面操作过程中的个性化要求
     * 具体定制化内容详见数据接口说明
     */
    @SerializedName("FlowOption")
    @Expose
    public Map flowOption;
    /**
     * The Need sign review.
     */
    @ParameterValue(info = "(NeedSignReview)是否开启发起方签署审核\n" +
            "true:开启发起方签署审核\n" +
            "false:不开启发起方签署审核\n" +
            "默认false:不开启发起方签署审核", type = InputType.onOff)
    /**
     * 是否开启发起方签署审核
     * true:开启发起方签署审核
     * false:不开启发起方签署审核
     * 默认false:不开启发起方签署审核
     */
    @SerializedName("NeedSignReview")
    @Expose
    public Boolean needSignReview;

    /**
     * The Need create review.
     */
    @ParameterValue(info = "(NeedCreateReview)开启发起方发起合同审核\n" +
            "true:开启发起方发起合同审核\n" +
            "false:不开启发起方发起合同审核\n" +
            "默认false:不开启发起方发起合同审核", type = InputType.onOff)
    /**
     * 开启发起方发起合同审核
     * true:开启发起方发起合同审核
     * false:不开启发起方发起合同审核
     * 默认false:不开启发起方发起合同审核
     */
    @SerializedName("NeedCreateReview")
    @Expose
    public Boolean needCreateReview;
    /**
     * The User data.
     */
    @ParameterValue(info = "(UserData)用户自定义参数", necessity = false, type = InputType.input)
    /**
     * 用户自定义参数
     */
    @SerializedName("UserData")
    @Expose
    public String userData;
    /**
     * The Flow id.
     */
    @ParameterValue(info = "(FlowId)合同id,用于通过已web页面发起的合同id快速生成一个web发起合同链接", necessity = false, type = InputType.input)

    /**
     * 合同id,用于通过已web页面发起的合同id快速生成一个web发起合同链接
     */
    @SerializedName("FlowId")
    @Expose
    public String flowId;
    /**
     * The Flow type.
     */
    @ParameterValue(info = "(FlowType)合同类型名称\n" +
            "该字段用于客户自定义合同类型\n" +
            "建议使用时指定合同类型，便于之后合同分类以及查看", necessity = false, type = InputType.input)

    /**
     * 合同类型名称
     * 该字段用于客户自定义合同类型
     * 建议使用时指定合同类型，便于之后合同分类以及查看
     */
    @SerializedName("FlowType")
    @Expose
    public String flowType;

}
