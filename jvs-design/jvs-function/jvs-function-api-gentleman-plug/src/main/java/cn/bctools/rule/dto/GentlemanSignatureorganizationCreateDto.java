package cn.bctools.rule.dto;


import cn.bctools.rule.entity.enums.type.RuleFile;
import lombok.Data;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signatureorganization create dto.
 *
 * @author jvs
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignatureorganizationCreateDto extends JunZiQianBaseDto {
    /**
     * The Email or mobile.
     */
    @ParameterValue(info = "邮箱", explain = "(不填入时系统生成)，需要保证邮箱的唯一性，接口不对邮箱真实性做校验，符合邮箱规则即可", necessity = false, type = InputType.input)
    public String emailOrMobile;

    /**
     * The Name.
     */
    @ParameterValue(info = "公司名称", explain = "（注：企业名称如含括号请传中文的括号）", type = InputType.input)
    public String name;

    /**
     * The Organization type.
     */
    @ParameterValue(info = "组织类型", explain = "0企业,1事业单位", type = InputType.number)
    public Integer organizationType;

    /**
     * The Identification type.
     */
    @ParameterValue(info = "证件类型", explain = "：0多证,1多证合一", type = InputType.number)
    public Integer identificationType;

    /**
     * The Organization reg no.
     */
    @ParameterValue(info = "营业执照号或事业单位事证号或统一社会信用代码", type = InputType.input)
    public String organizationRegNo;

    /**
     * The Organization reg img.
     */
    @ParameterValue(info = "营业执照号扫描件", explain = "图片,不能超2MB,接口不对传入的营业执照复印件图片进行真实性校验，需要开发者确保营业执照复印件的真实性。", type = InputType.file)
    public RuleFile organizationRegImg;

    /**
     * The Legal name.
     */
    @ParameterValue(info = "法人姓名", type = InputType.input)
    public String legalName;

    /**
     * The Legal identity card.
     */
    @ParameterValue(info = "法人身份证号", explain = "如果在后面企业签约中需要对法人做认证（人脸识别，运营商三要素，银行卡认证等等）时，则必传", necessity = false, type = InputType.input)
    public String legalIdentityCard;

    /**
     * The Legal mobile.
     */
    @ParameterValue(info = "法人电话号码", explain = "如果在后面企业签约中需要对法人做认证（运营商三要素，银行卡认证，短信验证认证等等）时，则必传", necessity = false, type = InputType.input)
    public String legalMobile;

    /**
     * The Legal identity front img.
     */
    @ParameterValue(info = "法人身份证正面", explain = "图片,不能超2MB，接口不对传入的法人身份证照进行真实性校验，需要开发者确保真实性", necessity = false, type = InputType.file)
    public RuleFile legalIdentityFrontImg;

    /**
     * The Legal identity back img.
     */
    @ParameterValue(info = "法人身份证反面", explain = "图片,不能超2MB，接口不对传入的法人身份证照进行真实性校验，需要开发者确保真实性", necessity = false, type = InputType.file)
    public RuleFile legalIdentityBackImg;

    /**
     * The Sign img.
     */
    @ParameterValue(info = "公章签章图片", explain = "(规格：180*180PX，透明背景，.png格式),不传入可由系统生成,图片,不能超2MB", necessity = false, type = InputType.file)
    public RuleFile signImg;

    /**
     * The Address.
     */
    @ParameterValue(info = "法人住址", necessity = false, type = InputType.input)
    public String address;

    /**
     * The Authorize name.
     */
    @ParameterValue(info = "授权人姓名", explain = "如果在后面企业签约中需要对被授权人做认证（人脸识别，运营商三要素，银行卡认证等等）时，则必传", necessity = false, type = InputType.input)
    public String authorizeName;

    /**
     * The Authorize card.
     */
    @ParameterValue(info = "授权人身份证", explain = "如果在后面企业签约中需要对被授权人做认证（人脸识别，运营商三要素，银行卡认证等等）时，则必传", necessity = false, type = InputType.input)
    public String authorizeCard;

    /**
     * The Authorize mobile phone.
     */
    @ParameterValue(info = "授权人手机号", explain = "如果在后面企业签约中需要对被授权人做认证（运营商三要素，银行卡认证，短信验证认证等等）时，则必传", necessity = false, type = InputType.input)
    public String authorizeMobilePhone;

    /**
     * The Organization code.
     */
    @ParameterValue(info = "组织结构代码", explain = "多证时必传", necessity = false, type = InputType.input)
    public String organizationCode;

    /**
     * The Organization code img.
     */
    @ParameterValue(info = "组织结构代码扫描件", explain = "多证时必传,图片,不能超2MB,接口不对传入的组织机构代码扫描件进行真实性校验，需要开发者确保真实性。", necessity = false, type = InputType.file)
    public RuleFile organizationCodeImg;

    /**
     * The Tax certificate img.
     */
    @ParameterValue(info = "税务登记扫描件", explain = "事业单位选填,其它多证时必传,图片,不能超2M，接口不对传入的税务登记扫描件做真实性校验，需要开发者确保真实性。", necessity = false, type = InputType.file)
    public RuleFile taxCertificateImg;

    /**
     * The Sign application.
     */
    @ParameterValue(info = "签约申请书", explain = "（授权书）扫描图片,不能超2MB，接口不对传入的申请表（授权书）做真实性校验，需要发开发者确保真实性。君子签有提供模板，需要模板者可以联系君子签客服获取。", necessity = false, type = InputType.file)
    public RuleFile signApplication;

    /**
     * The Notify url.
     */
    @ParameterValue(info = "企业认证结果异步通知地址", necessity = false, type = InputType.input)
    public String notifyUrl;


}
