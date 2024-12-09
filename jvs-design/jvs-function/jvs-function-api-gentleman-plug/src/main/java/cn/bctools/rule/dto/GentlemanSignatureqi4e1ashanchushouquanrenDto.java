package cn.bctools.rule.dto;


import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signatureqi 4 e 1 ashanchushouquanren dto.
 * @author jvs
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignatureqi4e1ashanchushouquanrenDto extends JunZiQianBaseDto {
    /**
     * The Id.
     */
    @ParameterValue(info = "授权人ID", type = InputType.input)
    public String id;

    /**
     * The Email.
     */
    @ParameterValue(info = "邮箱", type = InputType.input)
    public String email;



}
