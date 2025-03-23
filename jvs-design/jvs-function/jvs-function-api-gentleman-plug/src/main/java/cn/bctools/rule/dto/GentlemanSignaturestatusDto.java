package cn.bctools.rule.dto;


import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signaturestatus dto.
 *
 * @author jvs
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignaturestatusDto extends JunZiQianBaseDto {
    /**
     * The Apply no.
     */
    @ParameterValue(info = "合同编号", type = InputType.input)
    public String applyNo;

    /**
     * The Full name.
     */
    @ParameterValue(info = "签约人名称", necessity = false, type = InputType.input)
    public String fullName;

    /**
     * The Identity card.
     */
    @ParameterValue(info = "签约人证件号", necessity = false, type = InputType.input)
    public String identityCard;

    /**
     * The Identity type.
     */
    @ParameterValue(info = "证件类型 1身份证, 2护照, 3台胞证, 4港澳居民来往内地通行证, 11营业执照, 12统一社会信用代码, 20子账号, 99其他", necessity = false, type = InputType.number)
    public Integer identityType;


}
