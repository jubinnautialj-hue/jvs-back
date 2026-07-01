package cn.bctools.rule.ess.impl.personalaccountmanagementrelatedinterfaces;

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
public class ObtainImageOfPersonalUserAuthenticationCertificateDto {
    @NotNull(message = "帐号配置不能为空")
    @ParameterValue(info = "帐号配置", type = InputType.selected, cls = TencenCloudApiSelected.class)
    public String options;

    @ParameterValue(info = "(operator)执行本接口操作的员工信息。", necessity = false, type = InputType.map)
    public Map operator;

    @ParameterValue(info = "(UserName)个人用户名称", necessity = false, type = InputType.input)
    @SerializedName("UserName")
    @Expose
    public  String userName;

    @ParameterValue(info = "(IdCardType)证件类型，支持以下类型\n" +
            "ID_CARD : 居民身份证 (默认值)\n" +
            "PASSPORT : 护照\n" +
            "FOREIGN_ID_CARD : 外国人永久居留身份证\n" +
            "HONGKONG_AND_MACAO : 港澳居民来往内地通行证\n" +
            "HONGKONG_MACAO_AND_TAIWAN : 港澳台居民居住证(格式同居民身份证)", necessity = false, type = InputType.input)
    @SerializedName("IdCardType")
    @Expose
    public  String idCardType;

    @ParameterValue(info = "(IdCardNumber)证件号码，应符合以下规则\n" +
            "居民身份证号码应为18位字符串，由数字和大写字母X组成（如存在X，请大写）。\n" +
            "港澳居民来往内地通行证号码应为9位字符串，第1位为“C”，第2位为英文字母（但“I”、“O”除外），后7位为阿拉伯数字。\n" +
            "港澳台居民居住证号码编码规则与中国大陆身份证相同，应为18位字符串。", necessity = false, type = InputType.input)
    @SerializedName("IdCardNumber")
    @Expose
    public  String idCardNumber;
}
