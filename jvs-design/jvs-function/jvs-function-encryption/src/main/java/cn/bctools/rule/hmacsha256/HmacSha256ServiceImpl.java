package cn.bctools.rule.hmacsha256;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author guojing
 */
@Slf4j
@Rule(value = "HmacSha256Encode",
        group = RuleGroup.加密插件,
        test = true,
        returnType = ClassType.文本,
        order = 10,
//        iconUrl = "rule-hsmjiamifuwu",
        explain = "一种基于密钥的哈希算法的消息认证码，结合了一个密钥和一个哈希函数（在这里是 SHA-256）来生成一个消息认证码（MAC）"
)
public class HmacSha256ServiceImpl implements BaseCustomFunctionInterface<HmacSha256Dto> {

    @Override
    public Object execute(HmacSha256Dto dto, Map<String, Object> params) {
        HMac hmacSha256 = new HMac(HmacAlgorithm.HmacSHA256,dto.getSecretKey().getBytes());
        return hmacSha256.digestHex(dto.getText(), CharsetUtil.CHARSET_UTF_8);
    }

}
