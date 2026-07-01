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
 * The type Create signing process using pdf files dto.
 *
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class CreateSigningProcessUsingPDFFilesDto {

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
     * The Flow name.
     */
    @ParameterValue(info = "(FlowName)合同流程的名称（可自定义此名称），长度不能超过200，只能由中文、字母、数字和下划线组成。\n" +
            "     * 该名称还将用于合同签署完成后的下载文件名。", necessity = false, type = InputType.input)

    /**
     * 合同流程的名称（可自定义此名称），长度不能超过200，只能由中文、字母、数字和下划线组成。
     * 该名称还将用于合同签署完成后的下载文件名。
     */
    @SerializedName("FlowName")
    @Expose
    public String flowName;
    /**
     * The Approvers.
     */
    @ParameterValue(info = "(Approvers)合同流程的参与方列表，最多可支持50个参与方，可在列表中指定企业B端签署方和个人C端签署方的联系和认证方式等信息，具体定义可以参考开发者中心的ApproverInfo结构体。\n" +
            "如果合同流程是有序签署，Approvers列表中参与人的顺序就是默认的签署顺序，请确保列表中参与人的顺序符合实际签署顺序。。", type = InputType.listMap)

    /**
     * 合同流程的参与方列表，最多可支持50个参与方，可在列表中指定企业B端签署方和个人C端签署方的联系和认证方式等信息，具体定义可以参考开发者中心的ApproverInfo结构体。
     * 如果合同流程是有序签署，Approvers列表中参与人的顺序就是默认的签署顺序，请确保列表中参与人的顺序符合实际签署顺序。
     */
    @SerializedName("Approvers")
    @Expose
    public List approvers;
    /**
     * The File ids.
     */
    @ParameterValue(info = "(<)本合同流程需包含的PDF文件资源编号列表，通过<a href=\"https://qian.tencent.com/developers/companyApis/templatesAndFiles/UploadFiles\" target=\"_blank\">UploadFiles</a>接口获取PDF文件资源编号。\n" +
            "注:  `目前，此接口仅支持单个文件发起。`", type = InputType.list)

    /**
     * 本合同流程需包含的PDF文件资源编号列表，通过<a href="https://qian.tencent.com/developers/companyApis/templatesAndFiles/UploadFiles" target="_blank">UploadFiles</a>接口获取PDF文件资源编号。
     * 注:  `目前，此接口仅支持单个文件发起。`
     */
    @SerializedName("FileIds")
    @Expose
    public List<String> fileIds;
    /**
     * The Flow description.
     */
    @ParameterValue(info = "(FlowDescription)合同流程描述信息(可自定义此描述)，最大长度1000个字符。", necessity = false, type = InputType.input)

    /**
     * 合同流程描述信息(可自定义此描述)，最大长度1000个字符。
     */
    @SerializedName("FlowDescription")
    @Expose
    public String flowDescription;

    /**
     * The Flow type.
     */
    @ParameterValue(info = "(FlowType)合同流程的类别分类（可自定义名称，如销售合同/入职合同等），最大长度为200个字符，仅限中文、字母、数字和下划线组成。", necessity = false, type = InputType.input)
    /**
     * 合同流程的类别分类（可自定义名称，如销售合同/入职合同等），最大长度为200个字符，仅限中文、字母、数字和下划线组成。
     */
    @SerializedName("FlowType")
    @Expose
    public String flowType;
    /**
     * The Components.
     */
    @ParameterValue(info = "(Components)模板或者合同中的填写控件列表，列表中可支持下列多种填写控件，控件的详细定义参考开发者中心的Component结构体" +
            " <ul><li> 单行文本控件      </li>\n" +
            "     <li> 多行文本控件      </li>\n" +
            "     <li> 勾选框控件        </li>\n" +
            "     <li> 数字控件          </li>\n" +
            "     <li> 图片控件          </li>\n" +
            "     <li> 动态表格等填写控件</li></ul>", type = InputType.listMap)

    /**
     * 模板或者合同中的填写控件列表，列表中可支持下列多种填写控件，控件的详细定义参考开发者中心的Component结构体
     <ul><li> 单行文本控件      </li>
     <li> 多行文本控件      </li>
     <li> 勾选框控件        </li>
     <li> 数字控件          </li>
     <li> 图片控件          </li>
     <li> 动态表格等填写控件</li></ul>
     */
    @SerializedName("Components")
    @Expose
    public List components;
    /**
     * The Cc infos.
     */
    @ParameterValue(info = "(CcInfos)合同流程的抄送人列表，最多可支持50个抄送人，抄送人可查看合同内容及签署进度，但无需参与合同签署。", type = InputType.listMap)

    /**
     * 合同流程的抄送人列表，最多可支持50个抄送人，抄送人可查看合同内容及签署进度，但无需参与合同签署。

     */
    @SerializedName("CcInfos")
    @Expose
    public List ccInfos;
    /**
     * The Cc notify type.
     */
    @ParameterValue(info = "(CcNotifyType)可以设置以下时间节点来给抄送人发送短信通知来查看合同内容：\n" +
            "<ul><li> **0**：合同发起时通知（默认值）</li>\n" +
            "<li> **1**：签署完成后通知</li></ul>", necessity = false, type = InputType.number)

    /**
     * 可以设置以下时间节点来给抄送人发送短信通知来查看合同内容：
     * <ul><li> **0**：合同发起时通知（默认值）</li>
     * <li> **1**：签署完成后通知</li></ul>
     */
    @SerializedName("CcNotifyType")
    @Expose
    public Long ccNotifyType;
    /**
     * The Need preview.
     */
    @ParameterValue(info = "(NeedPreview)是否为预览模式，取值如下：\n" +
            "<ul><li> **false**：非预览模式（默认），会产生合同流程并返回合同流程编号FlowId。</li>\n" +
            "<li> **true**：预览模式，不产生合同流程，不返回合同流程编号FlowId，而是返回预览链接PreviewUrl，有效期为300秒，用于查看真实发起后合同的样子。</li></ul>", type = InputType.onOff)

    /**
     * 是否为预览模式，取值如下：
     * <ul><li> **false**：非预览模式（默认），会产生合同流程并返回合同流程编号FlowId。</li>
     * <li> **true**：预览模式，不产生合同流程，不返回合同流程编号FlowId，而是返回预览链接PreviewUrl，有效期为300秒，用于查看真实发起后合同的样子。</li></ul>
     */
    @SerializedName("NeedPreview")
    @Expose
    public Boolean needPreview;
    /**
     * The Preview type.
     */
    @ParameterValue(info = "(PreviewType)预览模式下产生的预览链接类型 \n" +
            "<ul><li> **0** :(默认) 文件流 ,点开后后下载预览的合同PDF文件 </li>\n" +
            "<li> **1** :H5链接 ,点开后在浏览器中展示合同的样子</li></ul>\n" +
            "注: `此参数在NeedPreview 为true时有效`", necessity = false, type = InputType.number)

    /**
     * 预览模式下产生的预览链接类型
     * <ul><li> **0** :(默认) 文件流 ,点开后后下载预览的合同PDF文件 </li>
     * <li> **1** :H5链接 ,点开后在浏览器中展示合同的样子</li></ul>
     * 注: `此参数在NeedPreview 为true时有效`
     */
    @SerializedName("PreviewType")
    @Expose
    public Long previewType;
    /**
     * The Deadline.
     */
    @ParameterValue(info = "(Deadline)合同流程的签署截止时间，格式为Unix标准时间戳（秒），如果未设置签署截止时间，则默认为合同流程创建后的365天时截止。\n" +
            "如果在签署截止时间前未完成签署，则合同状态会变为已过期，导致合同作废。", necessity = false, type = InputType.number)
    /**
     * 合同流程的签署截止时间，格式为Unix标准时间戳（秒），如果未设置签署截止时间，则默认为合同流程创建后的365天时截止。
     * 如果在签署截止时间前未完成签署，则合同状态会变为已过期，导致合同作废。
     */
    @SerializedName("Deadline")
    @Expose
    public Long deadline;
    /**
     * The Reminded on.
     */
    @ParameterValue(info = "(RemindedOn)合同到期提醒时间，为Unix标准时间戳（秒）格式，支持的范围是从发起时间开始到后10年内。\n" +
            "到达提醒时间后，腾讯电子签会短信通知发起方企业合同提醒，可用于处理合同到期事务，如合同续签等事宜。", necessity = false, type = InputType.number)

    /**
     * 合同到期提醒时间，为Unix标准时间戳（秒）格式，支持的范围是从发起时间开始到后10年内。
     * 到达提醒时间后，腾讯电子签会短信通知发起方企业合同提醒，可用于处理合同到期事务，如合同续签等事宜。
     */
    @SerializedName("RemindedOn")
    @Expose
    public Long remindedOn;
    /**
     * The Unordered.
     */
    @ParameterValue(info = "(Unordered)合同流程的签署顺序类型：\n" +
            "<ul><li> **false**：(默认)有序签署, 本合同多个参与人需要依次签署 </li>\n" +
            "<li> **true**：无序签署, 本合同多个参与人没有先后签署限制</li></ul>", type = InputType.onOff)

    /**
     * 合同流程的签署顺序类型：
     * <ul><li> **false**：(默认)有序签署, 本合同多个参与人需要依次签署 </li>
     * <li> **true**：无序签署, 本合同多个参与人没有先后签署限制</li></ul>
     */
    @SerializedName("Unordered")
    @Expose
    public Boolean unordered;
    /**
     * The Custom show map.
     */
    @ParameterValue(info = "(CustomShowMap)您可以自定义腾讯电子签小程序合同列表页展示的合同内容模板，模板中支持以下变量：\n" +
            "     <ul><li>{合同名称}   </li>\n" +
            "     <li>{发起方企业} </li>\n" +
            "     <li>{发起方姓名} </li>\n" +
            "     <li>{签署方N企业}</li>\n" +
            "     <li>{签署方N姓名}</li></ul>\n" +
            "     其中，N表示签署方的编号，从1开始，不能超过签署人的数量。\n" +
            "\n" +
            "     例如，如果是腾讯公司张三发给李四名称为“租房合同”的合同，您可以将此字段设置为：`合同名称:{合同名称};发起方: {发起方企业}({发起方姓名});签署方:{签署方1姓名}`，则小程序中列表页展示此合同为以下样子\n" +
            "\n" +
            "     合同名称：租房合同 \n" +
            "     发起方：腾讯公司(张三) \n" +
            "     签署方：李四\n。", necessity = false, type = InputType.input)

    /**
     * 您可以自定义腾讯电子签小程序合同列表页展示的合同内容模板，模板中支持以下变量：
     <ul><li>{合同名称}   </li>
     <li>{发起方企业} </li>
     <li>{发起方姓名} </li>
     <li>{签署方N企业}</li>
     <li>{签署方N姓名}</li></ul>
     其中，N表示签署方的编号，从1开始，不能超过签署人的数量。

     例如，如果是腾讯公司张三发给李四名称为“租房合同”的合同，您可以将此字段设置为：`合同名称:{合同名称};发起方: {发起方企业}({发起方姓名});签署方:{签署方1姓名}`，则小程序中列表页展示此合同为以下样子

     合同名称：租房合同
     发起方：腾讯公司(张三)
     签署方：李四

     */
    @SerializedName("CustomShowMap")
    @Expose
    public String customShowMap;
    /**
     * The Need sign review.
     */
    @ParameterValue(info = "(NeedSignReview)发起方企业的签署人进行签署操作前，是否需要企业内部走审批流程，取值如下：\n" +
            "     <ul><li> **false**：（默认）不需要审批，直接签署。</li>\n" +
            "     <li> **true**：需要走审批流程。当到对应参与人签署时，会阻塞其签署操作，等待企业内部审批完成。</li></ul>\n" +
            "     企业可以通过CreateFlowSignReview审批接口通知腾讯电子签平台企业内部审批结果\n" +
            "     <ul><li> 如果企业通知腾讯电子签平台审核通过，签署方可继续签署动作。</li>\n" +
            "     <li> 如果企业通知腾讯电子签平台审核未通过，平台将继续阻塞签署方的签署动作，直到企业通知平台审核通过。</li></ul>\n" +
            "     注：`此功能可用于与企业内部的审批流程进行关联，支持手动、静默签署合同`", type = InputType.onOff)

    /**
     * 发起方企业的签署人进行签署操作前，是否需要企业内部走审批流程，取值如下：
     <ul><li> **false**：（默认）不需要审批，直接签署。</li>
     <li> **true**：需要走审批流程。当到对应参与人签署时，会阻塞其签署操作，等待企业内部审批完成。</li></ul>
     企业可以通过CreateFlowSignReview审批接口通知腾讯电子签平台企业内部审批结果
     <ul><li> 如果企业通知腾讯电子签平台审核通过，签署方可继续签署动作。</li>
     <li> 如果企业通知腾讯电子签平台审核未通过，平台将继续阻塞签署方的签署动作，直到企业通知平台审核通过。</li></ul>
     注：`此功能可用于与企业内部的审批流程进行关联，支持手动、静默签署合同`
     */
    @SerializedName("NeedSignReview")
    @Expose
    public Boolean needSignReview;
    /**
     * The User data.
     */
    @ParameterValue(info = "(UserData) 调用方自定义的个性化字段(可自定义此名称)，并以base64方式编码，支持的最大数据大小为 20480长度。\n" +
            " 在合同状态变更的回调信息等场景中，该字段的信息将原封不动地透传给贵方。回调的相关说明可参考开发者中心的<a href=\"https://qian.tencent.com/developers/company/callback_types_v2\" target=\"_blank\">回调通知</a>模块。。", necessity = false, type = InputType.input)

    /**
     * 调用方自定义的个性化字段(可自定义此名称)，并以base64方式编码，支持的最大数据大小为 20480长度。
     * 在合同状态变更的回调信息等场景中，该字段的信息将原封不动地透传给贵方。回调的相关说明可参考开发者中心的<a href="https://qian.tencent.com/developers/company/callback_types_v2" target="_blank">回调通知</a>模块。
     */
    @SerializedName("UserData")
    @Expose
    public String userData;
    /**
     * The Approver verify type.
     */
    @ParameterValue(info = "(ApproverVerifyType)执行本接口操作的员工信息。", necessity = false, type = InputType.input)

    /**
     * 指定个人签署方查看合同的校验方式
     <ul><li>   **VerifyCheck**  :（默认）人脸识别,人脸识别后才能合同内容 </li>
     <li>   **MobileCheck**  :  手机号验证, 用户手机号和参与方手机号（ApproverMobile）相同即可查看合同内容（当手写签名方式为OCR_ESIGN时，该校验方式无效，因为这种签名方式依赖实名认证）</li></ul>
     */
    @SerializedName("ApproverVerifyType")
    @Expose
    public String approverVerifyType;
    /**
     * The Sign bean tag.
     */
    @ParameterValue(info = "(SignBeanTag)签署方签署控件（印章/签名等）的生成方式：\n" +
            "     <ul><li> **0**：在合同流程发起时，由发起人指定签署方的签署控件的位置和数量。</li>\n" +
            "     <li> **1**：签署方在签署时自行添加签署控件，可以拖动位置和控制数量。</li></ul>。", necessity = false, type = InputType.number)

    /**
     * 签署方签署控件（印章/签名等）的生成方式：
     <ul><li> **0**：在合同流程发起时，由发起人指定签署方的签署控件的位置和数量。</li>
     <li> **1**：签署方在签署时自行添加签署控件，可以拖动位置和控制数量。</li></ul>
     */
    @SerializedName("SignBeanTag")
    @Expose
    public Long signBeanTag;

    /**
     * The Auto sign scene.
     */
    @ParameterValue(info = "(AutoSignScene)个人自动签名的使用场景包括以下, 个人自动签署(即ApproverType设置成个人自动签署时)业务此值必传：\n" +
            "     <ul><li> **E_PRESCRIPTION_AUTO_SIGN**：处方单（医疗自动签）  </li></ul>\n" +
            "     注: `个人自动签名场景是白名单功能，使用前请与对接的客户经理联系沟通。`。", necessity = false, type = InputType.input)

    /**
     * 个人自动签名的使用场景包括以下, 个人自动签署(即ApproverType设置成个人自动签署时)业务此值必传：
     <ul><li> **E_PRESCRIPTION_AUTO_SIGN**：处方单（医疗自动签）  </li></ul>
     注: `个人自动签名场景是白名单功能，使用前请与对接的客户经理联系沟通。`
     */
    @SerializedName("AutoSignScene")
    @Expose
    public String autoSignScene;


}
