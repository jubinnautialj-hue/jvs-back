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
 * The type Gentleman signatureupload pers sign dto.
 *
 * @author jvs
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignatureuploadPersSignDto extends JunZiQianBaseDto {

    /**
     * The Identity card.
     */
    @ParameterValue(info = "个人证件号", necessity = false, type = InputType.input)
    public String identityCard;

    /**
     * The Sign img file.
     */
    @ParameterValue(info = "手写签名图片,png格式,背景透明,建议大小100*50PX,接口不对手写签名图片做真实性校验，需要开发者确保真实性。", type = InputType.file)
    public RuleFile signImgFile;


}
