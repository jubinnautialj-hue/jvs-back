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
 * The type Create electronic documents dto.
 *
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class CreateElectronicDocumentsDto {

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
    @ParameterValue(info = "(FlowId)签署流程编号,由CreateFlow接口返回。", necessity = false, type = InputType.input)
    public String flowId;

    /**
     * 用户上传的模板ID
     */
    @SerializedName("TemplateId")
    @Expose
    @ParameterValue(info = "(TemplateId)用户上传的模板ID", necessity = false, type = InputType.input)
    public String templateId;
    /**
     * The File names.
     */
    @ParameterValue(info = "(<)文件名列表，单个文件名最大长度200个字符，暂时仅支持单文件发起。设置后流程对应的文件名称当前设置的值。", necessity = false, type = InputType.list)

    /**
     * 文件名列表，单个文件名最大长度200个字符，暂时仅支持单文件发起。设置后流程对应的文件名称当前设置的值。
     */
    @SerializedName("FileNames")
    @Expose
    public List<String> fileNames;
    /**
     * The Form fields.
     */
    @ParameterValue(info = "(FormFields)电子文档的填写控件的填充内容。具体方式可以参考<a href=\"https://qian.tencent.com/developers/companyApis/dataTypes/#formfield\" target=\"_blank\">FormField</a>结构体的定义。", necessity = false, type = InputType.listMap)

    /**
     * 电子文档的填写控件的填充内容。具体方式可以参考<a href="https://qian.tencent.com/developers/companyApis/dataTypes/#formfield" target="_blank">FormField</a>结构体的定义。
     */
    @SerializedName("FormFields")
    @Expose
    public List formFields;
    /**
     * The Need preview.
     */
    @ParameterValue(info = "(NeedPreview)是否为预览模式，取值如下：\n" +
            "<ul><li> **false**：非预览模式（默认），会产生合同流程并返回合同流程编号FlowId。</li>\n" +
            "<li> **true**：预览模式，不产生合同流程，不返回合同流程编号FlowId，而是返回预览链接PreviewUrl，有效期为300秒，用于查看真实发起后合同的样子。</li></ul>\n" +
            "注: `当使用的模板中存在动态表格控件时，预览结果中没有动态表格的填写内容，动态表格合成完后会触发文档合成完成的回调通知`", necessity = false, type = InputType.onOff)

    /**
     * 是否为预览模式，取值如下：
     * <ul><li> **false**：非预览模式（默认），会产生合同流程并返回合同流程编号FlowId。</li>
     * <li> **true**：预览模式，不产生合同流程，不返回合同流程编号FlowId，而是返回预览链接PreviewUrl，有效期为300秒，用于查看真实发起后合同的样子。</li></ul>
     * 注: `当使用的模板中存在动态表格控件时，预览结果中没有动态表格的填写内容，动态表格合成完后会触发文档合成完成的回调通知`
     */
    @SerializedName("NeedPreview")
    @Expose
    public Boolean needPreview;
    /**
     * The Preview type.
     */
//
    @ParameterValue(info = "(PreviewType)预览链接类型 默认:0-文件流, 1- H5链接 注意:此参数在NeedPreview 为true 时有效,", necessity = false, type = InputType.list)

    /**
     * 预览链接类型 默认:0-文件流, 1- H5链接 注意:此参数在NeedPreview 为true 时有效,
     */
    @SerializedName("PreviewType")
    @Expose
    public Long previewType;
    /**
     * The Client token.
     */
    @ParameterValue(info = "(ClientToken)客户端Token", necessity = false, type = InputType.list)

    /**
     * 客户端Token，保持接口幂等性,最大长度64个字符
     */
    @SerializedName("ClientToken")
    @Expose
    public String clientToken;


}
