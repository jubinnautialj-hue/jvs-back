package cn.bctools.rule.dto;


import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signatureenterprise info match dto.
 *
 * @author jvs
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignatureenterpriseInfoMatchDto extends JunZiQianBaseDto {
    /**
     * The Enterprise name.
     */
    @ParameterValue(info = "企业名称", type = InputType.input)
    public String enterpriseName;

    /**
     * The Identity no.
     */
    @ParameterValue(info = "证件号", type = InputType.input)
    public String identityNo;

    /**
     * The Legal person name.
     */
    @ParameterValue(info = "法人姓名", type = InputType.input)
    public String legalPersonName;


}
