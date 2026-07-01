package cn.bctools.rule.dto;


import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signaturechexiaopiliangqianyuefaqi dto.
 *
 * @author jvs
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignaturechexiaopiliangqianyuefaqiDto extends JunZiQianBaseDto {
    /**
     * The Business no.
     */
    @ParameterValue(info = "业务编号", type = InputType.input)
    public String businessNo;


}
