package cn.bctools.rule.dto;


import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.rule.entity.enums.type.RuleFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signatureocr identity dto.
 *
 * @author jvs
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignatureocrIdentityDto extends JunZiQianBaseDto {
    /**
     * The File.
     */
    @ParameterValue(info = "图片file,图片不能超2mb,图片分辨率不能超4k,file和imgHttpUrl有且只有一个上传", necessity = false, type = InputType.file)
    public RuleFile file;

    /**
     * The Img http url.
     */
    @ParameterValue(info = "图片url地址（规范的url地址）,图片不能超2mb,图片分辨率不能超4k", necessity = false, type = InputType.input)
    public String imgHttpUrl;

    /**
     * The Is compress.
     */
    @ParameterValue(info = "是否进行图片压缩（是0，否1，如果压缩图片则可能会影响识别效率）,默认为0", necessity = false, type = InputType.input)
    public String isCompress;

    /**
     * The Front.
     */
    @ParameterValue(info = "0，识别身份证国徽面；1，识别身份证人像面", necessity = false, defaultValue = "1", type = InputType.number)
    public Integer front;


}
