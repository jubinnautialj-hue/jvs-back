package cn.bctools.rule.dto;


import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signatureyinxingqia 4 yaosuren 8 bc 1 feidalujumin dto.
 *
 * @author jvs
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignatureyinxingqia4yaosuren8bc1feidalujuminDto extends JunZiQianBaseDto {
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

    /**
     * The Identity type.
     */
    @ParameterValue(info = "证件类型：2(护照)、3(台湾通行证(台胞证))、4(港澳通行证(回乡证))", type = InputType.number)
    public Integer identityType;


}
