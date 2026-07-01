package cn.bctools.rule.sm3;


import cn.bctools.rule.annotations.Inspect;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
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
public class Sm3Dto {

    @ParameterValue(info = "需要加解密的原始文本", type = InputType.longtext, necessity = false, explain = "为空时获取SM2、AES密钥")
    public String text;

}
