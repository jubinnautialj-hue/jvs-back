package cn.bctools.rule.hmacsha256;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.TextEncodeDto;
import cn.hutool.crypto.digest.DigestUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author guojing
 */
@Slf4j
@Rule(value = "Sha256Encode",
        group = RuleGroup.加密插件,
        test = true,
        returnType = ClassType.文本,
        order = 10,
//        iconUrl = "rule-hsmjiamifuwu",
        explain = "提供了多种不一样的加密和解密方式并执行国密"
)
public class Sha256ServiceImpl implements BaseCustomFunctionInterface<TextEncodeDto> {

    @Override
    public Object execute(TextEncodeDto dto, Map<String, Object> params) {
        return DigestUtil.sha256Hex(dto.getText());
    }

}
