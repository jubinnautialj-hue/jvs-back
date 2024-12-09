package cn.bctools.rule.dto;


import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.rule.entity.enums.type.RuleFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signaturerenzhengduibi dto.
 *
 * @author jvs
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignaturerenzhengduibiDto extends JunZiQianBaseDto {
    /**
     * The Face img.
     */
    @ParameterValue(info = "人脸图片，图片大小100KB以内，格式：.jpg/.jpeg/.png", necessity = false, type = InputType.file)
    public RuleFile faceImg;

    /**
     * The Idcard.
     */
    @ParameterValue(info = "身份证号", type = InputType.input)
    public String idcard;

    /**
     * The Name.
     */
    @ParameterValue(info = "姓名", type = InputType.input)
    public String name;


}
