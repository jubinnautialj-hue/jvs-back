package cn.bctools.rule.ess.impl.embedablepagerelatedinterfaces;

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
 * The type Get other embeddable web pages dto.
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class GetOtherEmbeddableWebPagesDto {

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
     * WEB嵌入资源类型。
     * <br/>CREATE_SEAL: 生成创建印章的嵌入页面
     * <br/>CREATE_TEMPLATE：生成创建模板的嵌入页面
     * <br/>MODIFY_TEMPLATE：生成编辑模板的嵌入页面
     * <br/>PREVIEW_TEMPLATE：生成预览模板的嵌入页面
     * <br/>PREVIEW_SEAL_LIST：生成预览印章列表的嵌入页面
     * <br/>PREVIEW_SEAL_DETAIL：生成预览印章详情的嵌入页面
     * <br/>EXTEND_SERVICE：生成拓展服务的嵌入页面
     * <br/>PREVIEW_FLOW：生成预览合同的嵌入页面
     * <br/>PREVIEW_FLOW_DETAIL：生成查看合同详情的嵌入页面
     */
    @ParameterValue(info = "(EmbedType)CREATE_SEAL: 生成创建印章的嵌入页面\n" +
            "CREATE_TEMPLATE：生成创建模板的嵌入页面\n" +
            "MODIFY_TEMPLATE：生成编辑模板的嵌入页面\n" +
            "PREVIEW_TEMPLATE：生成预览模板的嵌入页面\n" +
            "PREVIEW_SEAL_LIST：生成预览印章列表的嵌入页面\n" +
            "PREVIEW_SEAL_DETAIL：生成预览印章详情的嵌入页面\n" +
            "EXTEND_SERVICE：生成拓展服务的嵌入页面\n" +
            "PREVIEW_FLOW：生成预览合同的嵌入页面\n" +
            "PREVIEW_FLOW_DETAIL：生成查看合同详情的嵌入页面。", necessity = false, type = InputType.input)
    @SerializedName("EmbedType")
    @Expose
    public  String embedType;

    /**
     * The Business id.
     */
    @ParameterValue(info = "(BusinessId)WEB嵌入的业务资源ID\n" +
            "PREVIEW_SEAL_DETAIL，必填，取值为印章id\n" +
            "MODIFY_TEMPLATE，PREVIEW_TEMPLATE，必填，取值为模版id\n" +
            "PREVIEW_FLOW，PREVIEW_FLOW_DETAIL，必填，取值为合同id", necessity = false, type = InputType.input)
    /**
     * WEB嵌入的业务资源ID
     * <br/>PREVIEW_SEAL_DETAIL，必填，取值为印章id
     * <br/>MODIFY_TEMPLATE，PREVIEW_TEMPLATE，必填，取值为模版id
     * <br/>PREVIEW_FLOW，PREVIEW_FLOW_DETAIL，必填，取值为合同id
     */
    @SerializedName("BusinessId")
    @Expose
    public  String businessId;

    /**
     * The Reviewer.
     */
    @ParameterValue(info = "(Reviewer)抄送方信息", necessity = false, type = InputType.map)
    /**
     * 抄送方信息
     */
    @SerializedName("Reviewer")
    @Expose
    public  Map<String, Object> reviewer;


}
