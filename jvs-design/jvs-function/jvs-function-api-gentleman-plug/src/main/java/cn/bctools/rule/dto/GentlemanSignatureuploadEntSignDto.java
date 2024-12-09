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
 * The type Gentleman signatureupload ent sign dto.
 *
 * @author jvs
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignatureuploadEntSignDto extends JunZiQianBaseDto {
    /**
     * The Sign name.
     */
    @ParameterValue(info = "企业签章名字", type = InputType.input)
    public String signName;

    /**
     * The Email.
     */
    @ParameterValue(info = "企业用户邮箱(必须与“企业注册认证接口”传入的邮箱一致),不传入将公章附与API商户", necessity = false, type = InputType.input)
    public String email;

    /**
     * The Sign img file.
     */
    @ParameterValue(info = "盖章图片,不能超2MB，规格：180*180PX，背景透明，png格式，接口不对传入的公章图片做真实性校验，需要开发者确保真实性", type = InputType.file)
    public RuleFile signImgFile;


}
