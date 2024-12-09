package cn.bctools.rule.speech;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.speech.SpeechDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author guojing
 */
@Slf4j
@AllArgsConstructor
@Rule(value = "语音识别",
        group = RuleGroup.识别插件,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        enable = false
//        iconUrl = "rule-daVrenzheng",

)
public class SpeechServiceImpl implements BaseCustomFunctionInterface<SpeechDto> {

    @Override
    public Object execute(SpeechDto dto, Map<String, Object> params) {
        return "不支持";
    }

}
