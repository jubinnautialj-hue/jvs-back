package cn.bctools.rule.dto;


import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signatureyinxingqiasiyaosuren 8 bc 1 xiangban dto.
 *
 * @author jvs
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignatureyinxingqiasiyaosuren8bc1xiangbanDto extends JunZiQianBaseDto {
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
     * The Card no.
     */
    @ParameterValue(info = "银行卡号", type = InputType.input)
    public String cardNo;

    /**
     * The Mobile phone.
     */
    @ParameterValue(info = "银行卡预留手机号", type = InputType.input)
    public String mobilePhone;


}
