package cn.bctools.rule.aes;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author guojing
 */
@Slf4j
@AllArgsConstructor
@Rule(value = "Aes",
        group = RuleGroup.加密插件,
        test = true,
        returnType = ClassType.文本,
        testShowEnum = TestShowEnum.TEXT,
        order = 2,
//        iconUrl = "rule-ipvgateway",
        explain = "是一种广泛使用的对称密钥加密算法，用于保护电子数据的机密性,默认以 base64加密返回"

)
public class AesServiceImpl implements BaseCustomFunctionInterface<AesDto> {

    @Override
    public Object execute(AesDto dto, Map<String, Object> params) {
        if (dto.isType()) {
            String aesKey = dto.getAesKey();
            byte[] decode = Base64.decode(aesKey);
            AES aes = SecureUtil.aes(decode);
            return aes.encryptBase64(dto.getBody());
        } else {
            String aesKey = dto.getAesKey();
            byte[] decode = Base64.decode(aesKey);
            AES aes = SecureUtil.aes(decode);
            return aes.decryptStr(dto.getBody());
        }
    }

}
