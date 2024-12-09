package cn.bctools.rule.dto;


import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signaturepdfmobanshanchu dto.
 *
 * @author jvs
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignaturepdfmobanshanchuDto extends JunZiQianBaseDto {
    /**
     * The Template no.
     */
    @ParameterValue(info = "模板ID", type = InputType.input)
    public String templateNo;


}
