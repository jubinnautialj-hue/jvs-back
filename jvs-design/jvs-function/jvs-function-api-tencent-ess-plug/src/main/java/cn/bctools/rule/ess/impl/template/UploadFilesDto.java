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
 * The type Upload files dto.
 */
@Data
@Accessors(chain = true)
public class UploadFilesDto {

    /**
     * The Options.
     */
    @NotNull(message = "帐号配置不能为空")
    @ParameterValue(info = "帐号配置", type = InputType.selected, cls = TencenCloudApiSelected.class)
    public String options;
    /**
     * The Business type.
     */
    @ParameterValue(info = "     * 文件对应业务类型,可以选择的类型如下\n" +
            "     * <ul><li> **TEMPLATE** : 此上传的文件用户生成合同模板，文件类型支持.pdf/.doc/.docx/.html格式，如果非pdf文件需要通过<a href=\"https://qian.tencent.com/developers/companyApis/templatesAndFiles/CreateConvertTaskApi\" target=\"_blank\">创建文件转换任务</a>转换后才能使用</li>\n" +
            "     * <li> **DOCUMENT** : 此文件用来发起合同流程，文件类型支持.pdf/.doc/.docx/.jpg/.png/.xls.xlsx/.html，如果非pdf文件需要通过<a href=\"https://qian.tencent.com/developers/companyApis/templatesAndFiles/CreateConvertTaskApi\" target=\"_blank\">创建文件转换任务</a>转换后才能使用</li>\n" +
            "     * <li> **DOCUMENT** : 此文件用于合同图片控件的填充，文件类型支持.jpg/.png</li>\n" +
            "     * <li> **SEAL** : 此文件用于印章的生成，文件类型支持.jpg/.jpeg/.png</li></ul>\n", necessity = false, type = InputType.input)

    /**
     * 文件对应业务类型,可以选择的类型如下
     * <ul><li> **TEMPLATE** : 此上传的文件用户生成合同模板，文件类型支持.pdf/.doc/.docx/.html格式，如果非pdf文件需要通过<a href="https://qian.tencent.com/developers/companyApis/templatesAndFiles/CreateConvertTaskApi" target="_blank">创建文件转换任务</a>转换后才能使用</li>
     * <li> **DOCUMENT** : 此文件用来发起合同流程，文件类型支持.pdf/.doc/.docx/.jpg/.png/.xls.xlsx/.html，如果非pdf文件需要通过<a href="https://qian.tencent.com/developers/companyApis/templatesAndFiles/CreateConvertTaskApi" target="_blank">创建文件转换任务</a>转换后才能使用</li>
     * <li> **DOCUMENT** : 此文件用于合同图片控件的填充，文件类型支持.jpg/.png</li>
     * <li> **SEAL** : 此文件用于印章的生成，文件类型支持.jpg/.jpeg/.png</li></ul>
     */
    @SerializedName("BusinessType")
    @Expose
    public String businessType;
    /**
     * The Caller.
     */
    @ParameterValue(info = "执行本接口操作的员工信息。其中OperatorId为必填字段，即用户的UserId。\n" +
            "注: `在调用此接口时，请确保指定的员工已获得所需的接口调用权限，并具备接口传入的相应资源的数据权限。`", necessity = false, type = InputType.map)

    /**
     * 执行本接口操作的员工信息。其中OperatorId为必填字段，即用户的UserId。
     * 注: `在调用此接口时，请确保指定的员工已获得所需的接口调用权限，并具备接口传入的相应资源的数据权限。`
     */
    @SerializedName("Caller")
    @Expose
    public Map caller;
    /**
     * The File infos.
     */
    @ParameterValue(info = "上传文件内容数组，最多支持上传20个文件。", type = InputType.listMap)

    /**
     * 上传文件内容数组，最多支持上传20个文件。
     */
    @SerializedName("FileInfos")
    @Expose
    public List fileInfos;
    /**
     * The File type.
     */
    @ParameterValue(info = "文件类型， 默认通过文件内容和文件后缀一起解析得到文件类型，调用接口时可以显示的指定上传文件的类型。\n" +
            "可支持的指定类型如下:\n" +
            "<ul><li>pdf</li>\n" +
            "<li>doc</li>\n" +
            "<li>docx</li>\n" +
            "<li>xls</li>\n" +
            "<li>xlsx</li>\n" +
            "<li>html</li>\n" +
            "<li>jpg</li>\n" +
            "<li>jpeg</li>\n" +
            "<li>png</li></ul>\n" +
            "如：pdf 表示上传的文件 张三入职合同.pdf的文件类型是 pdf", necessity = false, type = InputType.input)

    /**
     * 文件类型， 默认通过文件内容和文件后缀一起解析得到文件类型，调用接口时可以显示的指定上传文件的类型。
     * 可支持的指定类型如下:
     * <ul><li>pdf</li>
     * <li>doc</li>
     * <li>docx</li>
     * <li>xls</li>
     * <li>xlsx</li>
     * <li>html</li>
     * <li>jpg</li>
     * <li>jpeg</li>
     * <li>png</li></ul>
     * 如：pdf 表示上传的文件 张三入职合同.pdf的文件类型是 pdf
     */
    @SerializedName("FileType")
    @Expose
    public String fileType;
    /**
     * The Cover rect.
     */
    @ParameterValue(info = "此参数仅对上传的PDF文件有效。其主要作用是确定是否将PDF中的灰色矩阵置为白色。\n" +
            "<ul><li>**true**：将灰色矩阵置为白色。</li>\n" +
            "<li>**false**：无需处理，不会将灰色矩阵置为白色（默认）。</li></ul>\n" +
            "<p>\n" +
            "注: `该参数仅在关键字定位时，需要去除关键字所在的灰框场景下使用。`", type = InputType.onOff)

    /**
     * 此参数仅对上传的PDF文件有效。其主要作用是确定是否将PDF中的灰色矩阵置为白色。
     * <ul><li>**true**：将灰色矩阵置为白色。</li>
     * <li>**false**：无需处理，不会将灰色矩阵置为白色（默认）。</li></ul>
     * <p>
     * 注: `该参数仅在关键字定位时，需要去除关键字所在的灰框场景下使用。`
     */
    @SerializedName("CoverRect")
    @Expose
    public Boolean coverRect;
}
