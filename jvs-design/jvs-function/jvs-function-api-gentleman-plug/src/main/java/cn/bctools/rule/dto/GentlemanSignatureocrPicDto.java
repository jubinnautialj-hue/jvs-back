package cn.bctools.rule.dto;


import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signatureocr pic dto.
 *
 * @author jvs
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignatureocrPicDto extends JunZiQianBaseDto {
    /**
     * The File.
     */
    @ParameterValue(info = "手写内容图片文件", type = InputType.input)
    public String file;

    /**
     * The Text.
     */
    @ParameterValue(info = "要对比的文字，小于50字符", necessity = false, type = InputType.input)
    public String text;


}
