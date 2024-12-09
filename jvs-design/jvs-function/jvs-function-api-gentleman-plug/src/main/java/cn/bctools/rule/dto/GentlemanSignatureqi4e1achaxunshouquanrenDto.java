package cn.bctools.rule.dto;


import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signatureqi 4 e 1 achaxunshouquanren dto.
 *
 * @author jvs
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignatureqi4e1achaxunshouquanrenDto extends JunZiQianBaseDto {
    /**
     * The Email.
     */
    @ParameterValue(info = "邮箱", necessity = false, type = InputType.input)
    public String email;

    /**
     * The Authorize card.
     */
    @ParameterValue(info = "授权人身份证号", necessity = false, type = InputType.input)
    public String authorizeCard;


}
