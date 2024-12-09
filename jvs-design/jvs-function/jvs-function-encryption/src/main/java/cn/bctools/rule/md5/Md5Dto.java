package cn.bctools.rule.md5;


import cn.bctools.rule.annotations.Inspect;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.encryption.encryp.EncryptDecryptConsSelected;
import cn.bctools.rule.encryption.encryp.EncryptionEnum;
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
public class Md5Dto {

    @ParameterValue(info = "需要加解密的原始文本", type = InputType.longtext, explain = "只支持字符串")
    public String text;

}
