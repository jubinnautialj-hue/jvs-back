package cn.bctools.rule.dto;


import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signatureorganization face create dto.
 *
 * @author jvs
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignatureorganizationFaceCreateDto extends JunZiQianBaseDto {
    /**
     * The Order no.
     */
    @ParameterValue(info = "唯一流水号,由商户自行指定", type = InputType.input)
    public String orderNo;

    /**
     * The Email.
     */
    @ParameterValue(info = "唯一邮箱,由商户自行指定", type = InputType.input)
    public String email;

    /**
     * The Enterprise name.
     */
    @ParameterValue(info = "企业证件名称", type = InputType.input)
    public String enterpriseName;

    /**
     * The Identity no.
     */
    @ParameterValue(info = "统一社会信用号", type = InputType.input)
    public String identityNo;

    /**
     * The Legal person name.
     */
    @ParameterValue(info = "法人名称", type = InputType.input)
    public String legalPersonName;

    /**
     * The Legal identity card.
     */
    @ParameterValue(info = "法人证件号", type = InputType.input)
    public String legalIdentityCard;

    /**
     * The Legal mobile.
     */
    @ParameterValue(info = "法人手机号", necessity = false, type = InputType.input)
    public String legalMobile;

    /**
     * The Face per type.
     */
    @ParameterValue(info = "验证人类型1代理人，其它法人", type = InputType.number)
    public Integer facePerType;

    /**
     * The Face agant iden name.
     */
    @ParameterValue(info = "人脸验证代理人-名称", type = InputType.input)
    public String faceAgantIdenName;

    /**
     * The Face agant iden card.
     */
    @ParameterValue(info = "人脸验证代理人-证件号", type = InputType.input)
    public String faceAgantIdenCard;

    /**
     * The Back url.
     */
    @ParameterValue(info = "回调地址", type = InputType.input)
    public String backUrl;


}
