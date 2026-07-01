package cn.bctools.rule.dto;


import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.rule.entity.enums.type.RuleFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signatureorganization reapply dto.
 *
 * @author jvs
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignatureorganizationReapplyDto extends JunZiQianBaseDto {
    /**
     * The Email or mobile.
     */
    @ParameterValue(info = "邮箱(这里必传)", type = InputType.input)
    public String emailOrMobile;

    /**
     * The Name.
     */
    @ParameterValue(info = "公司名称（注：企业名称如含括号请传中文的括号）", type = InputType.input)
    public String name;

    /**
     * The Organization type.
     */
    @ParameterValue(info = "组织类型 0企业,1事业单位", type = InputType.number)
    public Integer organizationType;

    /**
     * The Identification type.
     */
    @ParameterValue(info = "证明类型：0多证,1多证合一", type = InputType.number)
    public Integer identificationType;

    /**
     * The Organization reg no.
     */
    @ParameterValue(info = "营业执照号或事业单位事证号或统一社会信用代码", type = InputType.input)
    public String organizationRegNo;

    /**
     * The Organization reg img.
     */
    @ParameterValue(info = "营业执照号扫描件,图片,不能超2MB,接口不对传入的营业执照复印件图片进行真实性校验，需要开发者确保营业执照复印件的真实性。", type = InputType.file)
    public RuleFile organizationRegImg;

    /**
     * The Legal name.
     */
    @ParameterValue(info = "法人姓名", type = InputType.input)
    public String legalName;

    /**
     * The Legal identity card.
     */
    @ParameterValue(info = "法人身份证号；如果在后面企业签约中需要对法人做认证（人脸识别，运营商三要素，银行卡认证等等）时，则必传", necessity = false, type = InputType.input)
    public String legalIdentityCard;

    /**
     * The Legal mobile.
     */
    @ParameterValue(info = "法人电话号码；如果在后面企业签约中需要对法人做认证（运营商三要素，银行卡认证，短信验证码等等）时，则必传", necessity = false, type = InputType.input)
    public String legalMobile;

    /**
     * The Legal identity front img.
     */
    @ParameterValue(info = "法人身份证正面,图片,不能超2MB", necessity = false, type = InputType.file)
    public RuleFile legalIdentityFrontImg;

    /**
     * The Legal identity back img.
     */
    @ParameterValue(info = "法人身份证反面,图片,不能超2MB", necessity = false, type = InputType.file)
    public RuleFile legalIdentityBackImg;

    /**
     * The Sign img.
     */
    @ParameterValue(info = "公章签章图片(规格：180*180PX，透明背景，.png格式),不传入可由系统生成,图片,不能超2MB", necessity = false, type = InputType.file)
    public RuleFile signImg;

    /**
     * The Address.
     */
    @ParameterValue(info = "法人住址", necessity = false, type = InputType.input)
    public String address;

    /**
     * The Authorize name.
     */
    @ParameterValue(info = "授权人姓名；如果在后面企业签约中需要对企业被授权人做认证（人脸识别，运营商三要素，银行卡认证等等）时，则必传", necessity = false, type = InputType.input)
    public String authorizeName;

    /**
     * The Authorize card.
     */
    @ParameterValue(info = "授权人身份证；如果在后面企业签约中需要对企业被授权人做认证（人脸识别，运营商三要素，银行卡认证等等）时，则必传", necessity = false, type = InputType.input)
    public String authorizeCard;

    /**
     * The Authorize mobile phone.
     */
    @ParameterValue(info = "授权人手机号；如果在后面企业签约中需要对企业被授权人做认证（运营商三要素，银行卡认证，短信验证码等等）时，则必传", necessity = false, type = InputType.input)
    public String authorizeMobilePhone;

    /**
     * The Organization code.
     */
    @ParameterValue(info = "组织结构代码,多证时必传", necessity = false, type = InputType.input)
    public String organizationCode;

    /**
     * The Organization code img.
     */
    @ParameterValue(info = "组织机构代码扫描件,多证时必传,图片,不能超2MB,接口不对传入的组织机构代码扫描件做真实性校验，需要开发者确保真实性。", necessity = false, type = InputType.file)
    public RuleFile organizationCodeImg;

    /**
     * The Tax certificate img.
     */
    @ParameterValue(info = "税务登记扫描件,事业单位选填,其它多证时必传,图片,不能超2M,接口不对传入的税务登记证图片做真实性校验，需要开发者确保真实性。", necessity = false, type = InputType.file)
    public RuleFile taxCertificateImg;

    /**
     * The Sign application.
     */
    @ParameterValue(info = "签约申请书扫描图,图片,不能超2MB,接口不对传入的申请表（授权书）做真实性校验，需要发开发者确保真实性。君子签有提供模板，需要模板者可以联系君子签客服获取。", necessity = false, type = InputType.file)
    public RuleFile signApplication;


}
