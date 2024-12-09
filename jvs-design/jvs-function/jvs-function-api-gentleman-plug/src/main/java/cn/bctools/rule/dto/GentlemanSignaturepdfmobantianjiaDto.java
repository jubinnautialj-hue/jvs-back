package cn.bctools.rule.dto;


import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.rule.entity.enums.type.RuleFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signaturepdfmobantianjia dto.
 *
 * @author jvs
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignaturepdfmobantianjiaDto extends JunZiQianBaseDto {
    /**
     * The File name.
     */
    @ParameterValue(info = "模板名称", necessity = false, type = InputType.input)
    public String fileName;

    /**
     * The File.
     */
    @ParameterValue(info = "PDF模板文件", type = InputType.file)
    public RuleFile file;


}
