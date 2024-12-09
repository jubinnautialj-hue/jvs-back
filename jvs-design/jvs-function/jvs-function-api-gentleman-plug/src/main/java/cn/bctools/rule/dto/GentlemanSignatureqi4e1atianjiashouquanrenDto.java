package cn.bctools.rule.dto;


import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signatureqi 4 e 1 atianjiashouquanren dto.
 *
 * @author jvs
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignatureqi4e1atianjiashouquanrenDto extends JunZiQianBaseDto {
    /**
     * The Email.
     */
    @ParameterValue(info = "邮箱（必须与“企业注册认证接口”传入的邮箱一致）不传时将授权人附与API主体账号", necessity = false, type = InputType.input)
    public String email;

    /**
     * The Authorize name.
     */
    @ParameterValue(info = "授权人姓名", type = InputType.input)
    public String authorizeName;

    /**
     * The Authorize mobile phone.
     */
    @ParameterValue(info = "授权人手机号", type = InputType.input)
    public String authorizeMobilePhone;

    /**
     * The Authorize card.
     */
    @ParameterValue(info = "授权人身份证号", type = InputType.input)
    public String authorizeCard;

    /**
     * The Id.
     */
    @ParameterValue(info = "授权人ID，修改授权人信息时必传", necessity = false, type = InputType.input)
    public String id;

    /**
     * The Default flag.
     */
    @ParameterValue(info = "1 默认，0 非默认", necessity = false, defaultValue = "0", type = InputType.input)
    public String defaultFlag;


}
