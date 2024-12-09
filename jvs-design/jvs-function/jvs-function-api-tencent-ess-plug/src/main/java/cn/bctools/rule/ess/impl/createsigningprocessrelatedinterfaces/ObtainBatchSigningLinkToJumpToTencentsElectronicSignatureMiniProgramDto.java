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
 * The type Obtain batch signing link to jump to tencents electronic signature mini program dto.
 *
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class ObtainBatchSigningLinkToJumpToTencentsElectronicSignatureMiniProgramDto {

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
     * The Name.
     */
    @ParameterValue(info = "(Name) 签署方经办人的姓名。经办人的姓名将用于身份认证和电子签名，请确保填写的姓名为签署方的真实姓名，而非昵称等代名。\n" +
            "  注：`请确保和合同中填入的一致`", necessity = false, type = InputType.input)
    /**
     * 签署方经办人的姓名。经办人的姓名将用于身份认证和电子签名，请确保填写的姓名为签署方的真实姓名，而非昵称等代名。
     *  注：`请确保和合同中填入的一致`
     */
    @SerializedName("Name")
    @Expose
    public String name;
    /**
     * The Mobile.
     */
    @ParameterValue(info = "(Mobile)手机号码， 支持国内手机号11位数字(无需加+86前缀或其他字符)。请确认手机号所有方为此业务通知方。注：`请确保和合同中填入的一致,  若无法保持一致，请确保在发起和生成批量签署链接时传入相同的参与方证件信息`", necessity = false, type = InputType.input)

    /**
     * 手机号码， 支持国内手机号11位数字(无需加+86前缀或其他字符)。
     * 请确认手机号所有方为此业务通知方。
     * <p>
     * 注：`请确保和合同中填入的一致,  若无法保持一致，请确保在发起和生成批量签署链接时传入相同的参与方证件信息`
     */
    @SerializedName("Mobile")
    @Expose
    public String mobile;
    /**
     * The Id card type.
     */
    @ParameterValue(info = "(IdCardType)证件类型，支持以下类型,ID_CARD : 居民身份证 (默认值),HONGKONG_AND_MACAO : 港澳居民来往内地通行证  HONGKONG_MACAO_AND_TAIWAN : 港澳台居民居住证(格式同居民身份证)", necessity = false, type = InputType.input)


    /**
     * 证件类型，支持以下类型
     * <ul><li>ID_CARD : 居民身份证 (默认值)</li>
     * <li>HONGKONG_AND_MACAO : 港澳居民来往内地通行证</li>
     * <li>HONGKONG_MACAO_AND_TAIWAN : 港澳台居民居住证(格式同居民身份证)</li></ul>
     * <p>
     * 注：`请确保和合同中填入的一致`
     */
    @SerializedName("IdCardType")
    @Expose
    public String idCardType;
    /**
     * The Id card number.
     */
    @ParameterValue(info = "(IdCardNumber)证件号码，应符合以下规则  居民身份证号码应为18位字符串，由数字和大写字母X组成（如存在X，请大写）。港澳居民来往内地通行证号码应为9位字符串，第1位为“C”，第2位为英文字母（但“I”、“O”除外），后7位为阿拉伯数字。港澳台居民居住证号码编码规则与中国大陆身份证相同，应为18位字符串。", necessity = false, type =
            InputType.input)

    /**
     * 证件号码，应符合以下规则
     * <ul><li>居民身份证号码应为18位字符串，由数字和大写字母X组成（如存在X，请大写）。</li>
     * <li>港澳居民来往内地通行证号码应为9位字符串，第1位为“C”，第2位为英文字母（但“I”、“O”除外），后7位为阿拉伯数字。</li>
     * <li>港澳台居民居住证号码编码规则与中国大陆身份证相同，应为18位字符串。</li></ul>
     * <p>
     * 注：`请确保和合同中填入的一致`
     */
    @SerializedName("IdCardNumber")
    @Expose
    public String idCardNumber;
    /**
     * The Notify type.
     */
    @ParameterValue(info = "(NotifyType)**NONE** : 不通知（默认），**SMS** : 短信通知（发送短信通知到Mobile参数所传的手机号）。", necessity = false, type = InputType.input)

    /**
     * 通知用户方式：
     * <ul>
     * <li>**NONE** : 不通知（默认）</li>
     * <li>**SMS** : 短信通知（发送短信通知到Mobile参数所传的手机号）</li>
     * </ul>
     */
    @SerializedName("NotifyType")
    @Expose
    public String notifyType;
    /**
     * The Flow ids.
     */
    @ParameterValue(info = "(FlowIds)本次需要批量签署的合同流程ID列表,可以不传,  如不传则是发给对方的所有待签署合同流程。", necessity = false, type = InputType.list)

    /**
     * 本次需要批量签署的合同流程ID列表。
     * 可以不传,  如不传则是发给对方的所有待签署合同流程。
     */
    @SerializedName("FlowIds")
    @Expose
    public List<String> flowIds;
    /**
     * The Organization name.
     */
    @ParameterValue(info = "(OrganizationName)目标签署人的企业名称，签署人如果是企业员工身份，需要传此参数。" +
            "请确认该名称与企业营业执照中注册的名称一致" +
            "如果名称中包含英文括号()，请使用中文括号（）代替。" +
            "请确保此企业已完成腾讯电子签企业认证", necessity = false, type = InputType.input)

    /**
     * 目标签署人的企业名称，签署人如果是企业员工身份，需要传此参数。
     * <p>
     * 注：
     * <ul>
     * <li>请确认该名称与企业营业执照中注册的名称一致。</li>
     * <li>如果名称中包含英文括号()，请使用中文括号（）代替。</li>
     * <li>请确保此企业已完成腾讯电子签企业认证。</li>
     * </ul>
     */
    @SerializedName("OrganizationName")
    @Expose
    public String organizationName;
    /**
     * The Jump to detail.
     */
    @ParameterValue(info = "(JumpToDetail)是否直接跳转至合同内容页面进行签署,**false**: 会跳转至批量合同流程的列表,  点击需要批量签署合同后进入合同内容页面进行签署(默认)  **true**: 跳过合同流程列表, 直接进入合同内容页面进行签署", necessity = false, type = InputType.onOff)

    /**
     * 是否直接跳转至合同内容页面进行签署
     * <ul>
     * <li>**false**: 会跳转至批量合同流程的列表,  点击需要批量签署合同后进入合同内容页面进行签署(默认)</li>
     * <li>**true**: 跳过合同流程列表, 直接进入合同内容页面进行签署</li>
     * </ul>
     */
    @SerializedName("JumpToDetail")
    @Expose
    public Boolean jumpToDetail;

}
