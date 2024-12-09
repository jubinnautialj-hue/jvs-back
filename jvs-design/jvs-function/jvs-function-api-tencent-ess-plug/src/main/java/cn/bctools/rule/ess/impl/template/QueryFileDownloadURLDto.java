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
 * The type Query file download url dto.
 */
@Data
@Accessors(chain = true)
public class QueryFileDownloadURLDto {


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
     * The Business type.
     */
    @ParameterValue(info = "(BusinessType)文件对应的业务类型，目前支持：。" +
            "<li>**FLOW ** : 如需下载合同文件请选择此项</li>\n" +
            "<li>**TEMPLATE ** : 如需下载模板文件请选择此项</li>\n" +
            "<li>**DOCUMENT  **: 如需下载文档文件请选择此项</li>\n" +
            "<li>**SEAL  **: 如需下载印章图片请选择此项</li>", necessity = false, type = InputType.input)

    /**
     * 文件对应的业务类型，目前支持：
     <ul>
     <li>**FLOW ** : 如需下载合同文件请选择此项</li>
     <li>**TEMPLATE ** : 如需下载模板文件请选择此项</li>
     <li>**DOCUMENT  **: 如需下载文档文件请选择此项</li>
     <li>**SEAL  **: 如需下载印章图片请选择此项</li>
     </ul>
     */
    @SerializedName("BusinessType")
    @Expose
    public String businessType;
    /**
     * The Business ids.
     */
    @ParameterValue(info = "(BusinessIds)业务编号的数组，取值如下 。 流程编号\n" +
            "模板编号\n" +
            "文档编号\n" +
            "印章编号如需下载合同文件请传入FlowId，最大支持20个资源 ", type = InputType.list)

    /**
     * 业务编号的数组，取值如下：
     <ul>
     <li>流程编号</li>
     <li>模板编号</li>
     <li>文档编号</li>
     <li>印章编号</li>
     <li>如需下载合同文件请传入FlowId，最大支持20个资源</li>
     </ul>
     */
    @SerializedName("BusinessIds")
    @Expose
    public List<String> businessIds;
    /**
     * The File name.
     */
    @ParameterValue(info = "(FileName)下载后的文件命名，只有FileType为zip的时候生效。", necessity = false, type = InputType.input)

    /**
     * 下载后的文件命名，只有FileType为zip的时候生效
     */
    @SerializedName("FileName")
    @Expose
    public String fileName;
    /**
     * The File type.
     */
    @ParameterValue(info = "(FileType)要下载的文件类型 JPG PDF ZIP。", necessity = false, type = InputType.input)

    /**
     * 要下载的文件类型，取值如下：
     <ul>
     <li>JPG</li>
     <li>PDF</li>
     <li>ZIP</li>
     </ul>
     */
    @SerializedName("FileType")
    @Expose
    public String fileType;
    /**
     * The Offset.
     */
    @ParameterValue(info = "(Offset)指定分页返回第几页的数据，如果不传默认返回第一页，页码从 0 开始，即首页为 0，最大 1000。。", necessity = false, type = InputType.number)

    /**
     * 指定分页返回第几页的数据，如果不传默认返回第一页，页码从 0 开始，即首页为 0，最大 1000。
     */
    @SerializedName("Offset")
    @Expose
    public Long offset;
    /**
     * The Limit.
     */
    @ParameterValue(info = "(Limit)指定分页每页返回的数据条数，如果不传默认为 20，单页最大支持 100。。", necessity = false, type = InputType.number)

    /**
     * 指定分页每页返回的数据条数，如果不传默认为 20，单页最大支持 100。
     */
    @SerializedName("Limit")
    @Expose
    public Long limit;
    /**
     * The Url ttl.
     */
    @ParameterValue(info = "(UrlTtl)下载url过期时间，单位秒。0: 按默认值5分钟，允许范围：1s~24x60x60s(1天)。", necessity = false, type = InputType.number)

    /**
     * 下载url过期时间，单位秒。0: 按默认值5分钟，允许范围：1s~24x60x60s(1天)
     */
    @SerializedName("UrlTtl")
    @Expose
    public Long urlTtl;

}
