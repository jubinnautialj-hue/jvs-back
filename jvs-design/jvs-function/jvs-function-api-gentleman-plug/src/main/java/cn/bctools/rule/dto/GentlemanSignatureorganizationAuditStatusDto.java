package cn.bctools.rule.dto;


import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signatureorganization audit status dto.
 *
 * @author jvs
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignatureorganizationAuditStatusDto extends JunZiQianBaseDto {
    /**
     * The Email or mobile.
     */
    @ParameterValue(info = "邮箱", type = InputType.input)
    public String emailOrMobile;


}
