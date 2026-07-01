package cn.bctools.rule.sm2;


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
public class Sm2Dto {

    @ParameterValue(info = "需要加解密的原始文本", type = InputType.longtext, necessity = false, explain = "只支持字符串，为空时获取SM2、AES密钥")
    public String text;

    @ParameterValue(info = "加解密类型", type = InputType.selected, cls = EncryptDecryptSelected.class)
    public boolean type;

    @ParameterValue(info = "私钥", type = InputType.longtext, necessity = false, explain = "私钥为base64,为空则随机生成", defaultValue = "MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQgA4+ZTikV9qLNgguTDKyeHfWqA0/qgqAQ6NzUvykHI/WgCgYIKoEcz1UBgi2hRANCAARLQ8Eaql3bwDpg00LruiyNFTKuIA283gN1bWQB0c7q9Fo955czchyZlOb7IIr8mxq4GEEyZUZcZvwklJVv2FIW")
    public String privateKey;

    @ParameterValue(info = "公钥", type = InputType.longtext, necessity = false, explain = "公钥为base64,为空则随机生成", defaultValue = "MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAES0PBGqpd28A6YNNC67osjRUyriANvN4DdW1kAdHO6vRaPeeXM3IcmZTm+yCK/JsauBhBMmVGXGb8JJSVb9hSFg==")
    public String publicKey;

}
