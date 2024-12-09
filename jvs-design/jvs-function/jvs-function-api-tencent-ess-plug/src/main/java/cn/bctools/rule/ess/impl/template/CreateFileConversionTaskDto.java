package cn.bctools.rule.ess.impl.template;

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
 * The type Create file conversion task dto.
 *
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class CreateFileConversionTaskDto {


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
     * The Resource type.
     */
    @ParameterValue(info = "(ResourceType)需要进行转换的资源文件类型\n" +
            "     支持的文件类型如下：\n" +
            "     <ul><li>doc</li>\n" +
            "     <li>docx</li>\n" +
            "     <li>xls</li>\n" +
            "     <li>xlsx</li>\n" +
            "     <li>jpg</li>\n" +
            "     <li>jpeg</li>\n" +
            "     <li>png</li>\n" +
            "     <li>html</li>\n" +
            "     <li>bmp</li>\n" +
            "     <li>txt</li></ul>。", necessity = false, type = InputType.input)

    /**
     * 需要进行转换的资源文件类型
     支持的文件类型如下：
     <ul><li>doc</li>
     <li>docx</li>
     <li>xls</li>
     <li>xlsx</li>
     <li>jpg</li>
     <li>jpeg</li>
     <li>png</li>
     <li>html</li>
     <li>bmp</li>
     <li>txt</li></ul>
     */
    @SerializedName("ResourceType")
    @Expose
    public String resourceType;
    /**
     * The Resource name.
     */
    @ParameterValue(info = "(ResourceName)需要进行转换操作的文件资源名称，带资源后缀名。注:  `资源名称长度限制为256个字符`。", necessity = false, type = InputType.input)

    /**
     * 需要进行转换操作的文件资源名称，带资源后缀名。注:  `资源名称长度限制为256个字符`
     */
    @SerializedName("ResourceName")
    @Expose
    public String resourceName;
    /**
     * The Resource id.
     */
    @ParameterValue(info = "(ResourceId)需要进行转换操作的文件资源Id，通过<a href=\"https://qian.tencent.com/developers/companyApis/templatesAndFiles/UploadFiles\" target=\"_blank\">UploadFiles</a>接口获取文件资源Id。\n" +
            "     注:  `目前，此接口仅支持单个文件进行转换。`。", necessity = false, type = InputType.input)

    /**
     * 需要进行转换操作的文件资源Id，通过<a href="https://qian.tencent.com/developers/companyApis/templatesAndFiles/UploadFiles" target="_blank">UploadFiles</a>接口获取文件资源Id。
     注:  `目前，此接口仅支持单个文件进行转换。`
     */
    @SerializedName("ResourceId")
    @Expose
    public String resourceId;

}
