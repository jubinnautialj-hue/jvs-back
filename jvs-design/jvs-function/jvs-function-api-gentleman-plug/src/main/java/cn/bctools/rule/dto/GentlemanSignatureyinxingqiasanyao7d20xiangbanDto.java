package cn.bctools.rule.dto;


import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signatureyinxingqiasanyao 7 d 20 xiangban dto.
 *
 * @author jvs
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignatureyinxingqiasanyao7d20xiangbanDto extends JunZiQianBaseDto {
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


}
