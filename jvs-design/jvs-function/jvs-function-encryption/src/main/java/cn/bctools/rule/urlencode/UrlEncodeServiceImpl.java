package cn.bctools.rule.urlencode;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.TextEncodeDto;
import cn.hutool.core.net.URLEncodeUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * @author guojing
 */
@Slf4j
@Rule(value = "UrlEncode",
        group = RuleGroup.加密插件,
        test = true,
        returnType = ClassType.文本,
        order = 10,
//        iconUrl = "rule-hsmjiamifuwu",
        explain = "可将字符串以URL编码，用于编码处理。"
)
public class UrlEncodeServiceImpl implements BaseCustomFunctionInterface<TextEncodeDto> {

    @Override
    public Object execute(TextEncodeDto dto, Map<String, Object> params) {
        return URLEncodeUtil.encodeAll(dto.getText(),Charset.defaultCharset());
    }

}
