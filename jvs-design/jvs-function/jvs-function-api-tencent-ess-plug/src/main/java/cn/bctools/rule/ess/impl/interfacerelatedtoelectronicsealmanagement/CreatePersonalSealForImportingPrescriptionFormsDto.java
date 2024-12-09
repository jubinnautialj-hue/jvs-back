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
public class CreatePersonalSealForImportingPrescriptionFormsDto {
    @NotNull(message = "帐号配置不能为空")
    @ParameterValue(info = "帐号配置", type = InputType.selected, cls = TencenCloudApiSelected.class)
    public String options;

    @ParameterValue(info = "(operator)执行本接口操作的员工信息。", necessity = false, type = InputType.map)
    public Map operator;

    @ParameterValue(info = "(IdCardNumber)证件号码，应符合以下规则\n" +
            "居民身份证号码应为18位字符串，由数字和大写字母X组成（如存在X，请大写）。\n" +
            "港澳居民来往内地通行证号码应为9位字符串，第1位为“C”，第2位为英文字母（但“I”、“O”除外），后7位为阿拉伯数字。\n" +
            "港澳台居民居住证号码编码规则与中国大陆身份证相同，应为18位字符串。", necessity = false, type = InputType.input)
    @SerializedName("IdCardNumber")
    @Expose
    public String idCardNumber;

    @ParameterValue(info = "(SealName)印章名称，长度1-50个字。", necessity = false, type = InputType.input)
    @SerializedName("SealName")
    @Expose
    public String sealName;


    @ParameterValue(info = "(IdCardType)证件类型，支持以下类型\n" +
            "ID_CARD : 居民身份证 (默认值)\n" +
            "HONGKONG_AND_MACAO : 港澳居民来往内地通行证\n" +
            "HONGKONG_MACAO_AND_TAIWAN : 港澳台居民居住证(格式同居民身份证)", necessity = false, type = InputType.input)
    @SerializedName("IdCardType")
    @Expose
    public String idCardType;

    @ParameterValue(info = "(SealImageCompress)是否开启印章图片压缩处理，默认不开启，如需开启请设置为 true。当印章超过 2M 时建议开启，开启后图片的 hash 将发生变化。", necessity = false, type = InputType.onOff)
    @SerializedName("SealImageCompress")
    @Expose
    public Boolean sealImageCompress;

    @ParameterValue(info = "(Mobile)手机号码；当需要开通自动签时，该参数必传", necessity = false, type = InputType.input)
    @SerializedName("Mobile")
    @Expose
    public String mobile;

    @ParameterValue(info = "(EnableAutoSign)是否开通自动签，该功能需联系运营工作人员开通后使用", necessity = false, type = InputType.onOff)
    @SerializedName("EnableAutoSign")
    @Expose
    public Boolean enableAutoSign;

    @ParameterValue(info = "(SealColor)印章颜色（参数ProcessSeal=true时生效）\n" +
            "默认值：BLACK黑色\n" +
            "取值:\n" +
            "BLACK 黑色,\n" +
            "RED 红色,\n" +
            "BLUE 蓝色。\n" +
            "示例值：BLACK", necessity = false, type = InputType.input)
    @SerializedName("SealColor")
    @Expose
    public String sealColor;

    @ParameterValue(info = "(ProcessSeal)是否处理印章，默认不做印章处理。\n" +
            "取值如下：\n" +
            "\n" +
            "false：不做任何处理；\n" +
            "true：做透明化处理和颜色增强。", necessity = false, type = InputType.onOff)
    @SerializedName("ProcessSeal")
    @Expose
    public Boolean processSeal;

    @ParameterValue(info = "(FileId)印章图片文件 id\n" +
            "取值：\n" +
            "填写的FileId通过UploadFiles接口上传文件获取。", necessity = false, type = InputType.input)
    @SerializedName("FileId")
    @Expose
    public String fileId;

    @ParameterValue(info = "(LicenseType)设置用户开通自动签时是否绑定个人自动签账号许可。一旦绑定后，将扣减购买的个人自动签账号许可一次（1年有效期），不可解绑释放。不传默认为绑定自动签账号许可。 0-绑定个人自动签账号许可，开通后将扣减购买的个人自动签账号许可一次 1-不绑定，发起合同时将按标准合同套餐进行扣减", necessity = false, type = InputType.number)
    @SerializedName("LicenseType")
    @Expose
    public Long licenseType;

}
