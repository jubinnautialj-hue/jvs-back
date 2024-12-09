package cn.bctools.rule.dto;


import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signatureoperators three verify dto.
 *
 * @author jvs
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignatureoperatorsThreeVerifyDto extends JunZiQianBaseDto {
    /**
     * The Name.
     */
    @ParameterValue(info = "姓名", type = InputType.input)
    public String name;

    /**
     * The Identity card.
     */
    @ParameterValue(info = "证件号", type = InputType.input)
    public String identityCard;

    /**
     * The Mobile.
     */
    @ParameterValue(info = "手机号", type = InputType.input)
    public String mobile;


}
