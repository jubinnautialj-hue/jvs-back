package cn.bctools.rule.sm3;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.digest.Digester;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author guojing
 */
@Slf4j
@Rule(value = "SM3",
        group = RuleGroup.加密插件,
        test = true,
        returnType = ClassType.文本,
        order = 10,
        explain = "SM3主要用于数字签名及验证、消息认证码生成及验证、随机数生成等"
)
public class Sm3ServiceImpl implements BaseCustomFunctionInterface<Sm3Dto> {

    static final Digester SM_3 = DigestUtil.digester("sm3");

    @Override
    public Object execute(Sm3Dto sm3Dto, Map<String, Object> params) {
        return SM_3.digestHex(sm3Dto.getText());
    }

}
