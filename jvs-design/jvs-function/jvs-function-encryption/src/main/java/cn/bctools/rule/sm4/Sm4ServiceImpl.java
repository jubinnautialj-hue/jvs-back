package cn.bctools.rule.sm4;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.KeyUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.symmetric.SM4;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * @author guojing
 */
@Slf4j
@Rule(value = "SM4",
        group = RuleGroup.加密插件,
        test = true,
        returnType = ClassType.文本,
        order = 10,
        explain = "SM4主要用于数字签名及验证、消息认证码生成及验证、随机数生成等"
)
public class Sm4ServiceImpl implements BaseCustomFunctionInterface<Sm4Dto> {


    @Override
    public Object execute(Sm4Dto sm4Dto, Map<String, Object> params) {
        if (sm4Dto.getType()) {
            return new SymmetricCrypto("SM4/ECB/PKCS5Padding", sm4Dto.getKey().getBytes()).encryptHex(sm4Dto.getText());
        }
        return new SymmetricCrypto("SM4/ECB/PKCS5Padding", sm4Dto.getKey().getBytes()).decryptStr(sm4Dto.getText());
    }

}
