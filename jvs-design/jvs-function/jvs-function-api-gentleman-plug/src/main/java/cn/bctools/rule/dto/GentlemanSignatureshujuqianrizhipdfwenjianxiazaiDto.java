package cn.bctools.rule.dto;


import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signatureshujuqianrizhipdfwenjianxiazai dto.
 *
 * @author jvs
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignatureshujuqianrizhipdfwenjianxiazaiDto extends JunZiQianBaseDto {
    /**
     * The Seq no.
     */
    @ParameterValue(info = "数据签编号", type = InputType.input)
    public String seqNo;


}
