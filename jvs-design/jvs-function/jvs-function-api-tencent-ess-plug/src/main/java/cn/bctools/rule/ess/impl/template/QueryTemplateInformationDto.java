package cn.bctools.rule.ess.impl.template;

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
 * @author jvs
 * The type Query template information dto.
 */
@Data
@Accessors(chain = true)
public class QueryTemplateInformationDto {


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
     * The Content type.
     */
    @ParameterValue(info = "(ContentType)查询内容控制\n" +
            "     <ul><li>**0**：模板列表及详情（默认）</li>\n" +
            "     <li>**1**：仅模板列表</li></ul>", necessity = false, type = InputType.number)

    /**
     * 查询内容控制
     <ul><li>**0**：模板列表及详情（默认）</li>
     <li>**1**：仅模板列表</li></ul>
     */
    @SerializedName("ContentType")
    @Expose
    public Long contentType;
    /**
     * The Filters.
     */
    @ParameterValue(info = "(Filters)搜索条件，本字段用于指定模板Id进行查询。\n" +
            "     Key：template-id Values：需要查询的模板Id列表。", type = InputType.listMap)
    /**
     * 搜索条件，本字段用于指定模板Id进行查询。
     Key：template-id Values：需要查询的模板Id列表
     */
    @SerializedName("Filters")
    @Expose
    public List filters;
    /**
     * The Offset.
     */
    @ParameterValue(info = "(Offset)查询结果分页返回，指定从第几页返回数据，和Limit参数配合使用。\n" +
            "     注：`1.offset从0开始，即第一页为0。`\n" +
            "     `2.默认从第一页返回。`。", necessity = false, type = InputType.number)

    /**
     * 查询结果分页返回，指定从第几页返回数据，和Limit参数配合使用。
     注：`1.offset从0开始，即第一页为0。`
     `2.默认从第一页返回。`
     */
    @SerializedName("Offset")
    @Expose
    public Long offset;
    /**
     * The Limit.
     */
    @ParameterValue(info = "(Limit)指定每页返回的数据条数，和Offset参数配合使用。\n" +
            "     注：`1.默认值为20，单页做大值为200。`。", necessity = false, type = InputType.number)

    /**
     * 指定每页返回的数据条数，和Offset参数配合使用。
     注：`1.默认值为20，单页做大值为200。`
     */
    @SerializedName("Limit")
    @Expose
    public Long limit;
    /**
     * The Application id.
     */
    @ParameterValue(info = "(ApplicationId)指定查询的应用号，指定后查询该应用号下的模板列表。\n" +
            "     注：`1.ApplicationId为空时，查询所有应用下的模板列表。`。", necessity = false, type = InputType.input)

    /**
     * 指定查询的应用号，指定后查询该应用号下的模板列表。
     注：`1.ApplicationId为空时，查询所有应用下的模板列表。`
     */
    @SerializedName("ApplicationId")
    @Expose
    public String applicationId;
    /**
     * The Is channel.
     */
    @ParameterValue(info = "(IsChannel)默认为false，查询SaaS模板库列表；\n" +
            "     为true，查询第三方应用集成平台企业模板库管理列表。", type = InputType.onOff)

    /**
     * 默认为false，查询SaaS模板库列表；
     为true，查询第三方应用集成平台企业模板库管理列表
     */
    @SerializedName("IsChannel")
    @Expose
    public Boolean isChannel;
    /**
     * The With preview url.
     */
    @ParameterValue(info = "(WithPreviewUrl)是否获取模板预览链接。", type = InputType.onOff)

    /**
     * 是否获取模板预览链接
     */
    @SerializedName("WithPreviewUrl")
    @Expose
    public Boolean withPreviewUrl;

}
