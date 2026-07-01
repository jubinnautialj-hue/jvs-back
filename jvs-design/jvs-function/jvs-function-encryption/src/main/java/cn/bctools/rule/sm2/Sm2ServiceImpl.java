package cn.bctools.rule.sm2;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author guojing
 */
@Slf4j
@Rule(value = "SM2",
        group = RuleGroup.加密插件,
        test = true,
        returnType = ClassType.文本,
        order = 10,
//        iconUrl = "rule-hsmjiamifuwu",
        explain = "SM2算法和RSA算法都是公钥密码算法，SM2算法是一种更先进安全的算法，常用于替换RSA算法。"
)
public class Sm2ServiceImpl implements BaseCustomFunctionInterface<Sm2Dto> {

    @Override
    public Object execute(Sm2Dto dto, Map<String, Object> params) {
        if (dto.isType()) {
            SM2 sm2 = SmUtil.sm2(dto.getPrivateKey(), dto.getPublicKey());
            //加密
            return sm2.encryptBase64(dto.getText(), KeyType.PublicKey);
        } else {
            SM2 sm2 = SmUtil.sm2(dto.getPrivateKey(), dto.getPublicKey());
            return StrUtil.utf8Str(sm2.decryptStr(dto.getText(), KeyType.PrivateKey));
        }
    }

}
