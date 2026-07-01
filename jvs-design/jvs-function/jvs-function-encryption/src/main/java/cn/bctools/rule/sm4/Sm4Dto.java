package cn.bctools.rule.sm4;


import cn.bctools.rule.annotations.Inspect;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.rule.selected.EncryptDecryptSelected;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


/**
 * 加解密请求入参
 *
 * @author guojing
 */
@Accessors(chain = true)
@Inspect
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Sm4Dto {

    @ParameterValue(info = "需要加解密的原始文本", type = InputType.longtext, necessity = false)
    public String text;

    @ParameterValue(info = "需要加解密的原始文本,key必须是16位字母或数字", type = InputType.input, defaultValue = "1234567891234567")
    public String key;

    @ParameterValue(info = "加解密类型", type = InputType.selected, cls = EncryptDecryptSelected.class)
    public Boolean type;

}
