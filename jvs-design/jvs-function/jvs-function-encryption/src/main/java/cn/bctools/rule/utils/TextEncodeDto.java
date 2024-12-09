package cn.bctools.rule.utils;


import cn.bctools.rule.annotations.Inspect;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


/**
 * @author jvs
 * 加解密请求入参
 */
@Accessors(chain = true)
@Inspect
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TextEncodeDto {

    /**
     * The Text.
     */
    @ParameterValue(info = "需要加解密的原始文本", explain = "只支持字符串", type = InputType.longtext, necessity = false)
    public String text;

}
