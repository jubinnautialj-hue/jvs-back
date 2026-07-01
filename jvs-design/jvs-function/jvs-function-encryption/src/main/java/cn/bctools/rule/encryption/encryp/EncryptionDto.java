package cn.bctools.rule.encryption.encryp;


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
public class EncryptionDto {

    @ParameterValue(info = "需要加解密的原始文本", type = InputType.longtext,  necessity = false, explain = "为空时获取SM2、AES密钥")
    public String text;

    @ParameterValue(info = "加解密方式", type = InputType.selected,   cls = EncryptDecryptConsSelected.class)
    public EncryptionEnum mode;

    @ParameterValue(info = "加解密类型", type = InputType.selected,   cls = EncryptDecryptSelected.class)
    public boolean type;


    @ParameterValue(info = "AES16位_KEY", type = InputType.input, necessity = false, explain = "为空则随机生成")
    public String aesKey;

    @ParameterValue(info = "私钥", type = InputType.longtext, necessity = false, explain = "为空则随机生成")
    public String privateKey;

    @ParameterValue(info = "公钥", type = InputType.longtext, necessity = false, explain = "为空则随机生成")
    public String publicKey;

}
