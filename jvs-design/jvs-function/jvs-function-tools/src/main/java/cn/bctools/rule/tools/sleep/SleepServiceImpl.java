package cn.bctools.rule.tools.sleep;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author jvs
 * @describe 休眠时间
 */
@Slf4j
@AllArgsConstructor
@Rule(value = "休眠",
        group = RuleGroup.工具插件,
        test = true,
        returnType = ClassType.数字,
        testShowEnum = TestShowEnum.TEXT,
        order = 31,
        explain = "此节点用于测试并行执行的总时间，根据传递的参数确定休眠时间"

)
public class SleepServiceImpl implements BaseCustomFunctionInterface<SleepDto> {

    @Override
    public Object execute(SleepDto dto, Map<String, Object> params) {
        try {
            Thread.sleep(dto.getSleep() * 1000);
            return dto.getSleep();
        } catch (InterruptedException e) {
            return dto.getSleep();
        }
    }

}
