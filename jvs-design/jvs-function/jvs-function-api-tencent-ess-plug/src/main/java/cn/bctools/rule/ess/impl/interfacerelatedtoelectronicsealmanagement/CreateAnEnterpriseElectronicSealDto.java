package cn.bctools.rule.ess.impl.interfacerelatedtoelectronicsealmanagement;

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
public class CreateAnEnterpriseElectronicSealDto {
    @NotNull(message = "帐号配置不能为空")
    @ParameterValue(info = "帐号配置", type = InputType.selected, cls = TencenCloudApiSelected.class)
    public String options;

    @ParameterValue(info = "(operator)执行本接口操作的员工信息。", necessity = false, type = InputType.map)
    public Map operator;

    @ParameterValue(info = "(SealName)电子印章名字，1-50个中文字符\n" +
            "注:同一企业下电子印章名字不能相同\n" +
            "示例值：公司印章", necessity = false, type = InputType.input)
    @SerializedName("SealName")
    @Expose
    public String sealName;

    @ParameterValue(info = "(GenerateSource)电子印章生成方式\n" +
            "\n" +
            "空值:(默认)使用上传的图片生成印章, 此时需要上传SealImage图片\n" +
            "SealGenerateSourceSystem: 系统生成印章, 无需上传SealImage图片\n" +
            "\n" +
            "\n" +
            "示例值：SealGenerateSourceSystem", necessity = false, type = InputType.input)
    @SerializedName("GenerateSource")
    @Expose
    public String generateSource;

    @ParameterValue(info = "(SealType)电子印章类型 , 可选类型如下:\n" +
            "OFFICIAL: (默认)公章\n" +
            "CONTRACT: 合同专用章;\n" +
            "FINANCE: 财务专用章;\n" +
            "PERSONNEL: 人事专用章\n" +
            "\n" +
            "\n" +
            "注: 同企业下只能有一个公章, 重复创建会报错\n" +
            "示例值：OFFICIAL", necessity = false, type = InputType.input)
    @SerializedName("SealType")
    @Expose
    public String sealType;

    @ParameterValue(info = "(FileName)电子印章图片文件名称，1-50个中文字符。\n" +
            "示例值：印章图片.png", necessity = false, type = InputType.input)
    @SerializedName("FileName")
    @Expose
    public String fileName;

    @ParameterValue(info = "(Image)电子印章图片base64编码，大小不超过10M（原始图片不超过5M），只支持PNG或JPG图片格式\n" +
            "\n" +
            "注: 通过图片创建的电子印章，需电子签平台人工审核\n" +
            "\n" +
            "\n" +
            "示例值：imagecontentbase64", necessity = false, type = InputType.input)
    @SerializedName("Image")
    @Expose
    public String image;

    @ParameterValue(info = "(Width)电子印章宽度,单位px\n" +
            "参数不再启用，系统会设置印章大小为标准尺寸。\n" +
            "示例值：10", necessity = false, type = InputType.number)
    @SerializedName("Width")
    @Expose
    public Long width;

    @ParameterValue(info = "(Height)电子印章高度,单位px\n" +
            "参数不再启用，系统会设置印章大小为标准尺寸。\n" +
            "示例值：20", necessity = false, type = InputType.number)
    @SerializedName("Height")
    @Expose
    public Long height;

    @ParameterValue(info = "(Color)电子印章印章颜色(默认红色RED),RED-红色\n" +
            "\n" +
            "系统目前只支持红色印章创建。\n" +
            "示例值：Red", necessity = false, type = InputType.input)
    @SerializedName("Color")
    @Expose
    public String color;

    @ParameterValue(info = "(SealHorizontalText)企业印章横向文字，最多可填15个汉字 (若超过印章最大宽度，优先压缩字间距，其次缩小字号)\n" +
            "横向文字的位置如下图中的\"印章横向文字在这里\"", necessity = false, type = InputType.input)
    @SerializedName("SealHorizontalText")
    @Expose
    public String sealHorizontalText;

    @ParameterValue(info = "(SealChordText)暂时不支持下弦文字设置\n" +
            "示例值：下弦文字", necessity = false, type = InputType.input)
    @SerializedName("SealChordText")
    @Expose
    public String sealChordText;

    @ParameterValue(info = "(SealCentralType)系统生成的印章只支持STAR\n" +
            "示例值：Star", necessity = false, type = InputType.input)
    @SerializedName("SealCentralType")
    @Expose
    public String sealCentralType;

    @ParameterValue(info = "(FileToken)通过文件上传时，服务端生成的电子印章上传图片的token", necessity = false, type = InputType.input)
    @SerializedName("FileToken")
    @Expose
    public String fileToken;

    @ParameterValue(info = "(SealStyle)印章样式, 可以选择的样式如下:\n" +
            "circle:(默认)圆形印章\n" +
            "ellipse:椭圆印章\n" +
            "\n" +
            "示例值：cycle", necessity = false, type = InputType.input)
    @SerializedName("SealStyle")
    @Expose
    public String sealStyle;

    @ParameterValue(info = "(SealSize)印章尺寸取值描述, 可以选择的尺寸如下:\n" +
            "42_42: 圆形企业公章直径42mm, 当SealStyle是圆形的时候才有效\n" +
            "40_40: 圆形企业印章直径40mm, 当SealStyle是圆形的时候才有效\n" +
            "45_30: 椭圆形印章45mm x 30mm, 当SealStyle是椭圆的时候才有效\n" +
            "\n" +
            "示例值：42_42", necessity = false, type = InputType.input)
    @SerializedName("SealSize")
    @Expose
    public String sealSize;

}
