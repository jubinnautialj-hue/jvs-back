package cn.bctools.rule.tools.random;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.hutool.core.util.RandomUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @Author: GuoZi
 */
@Service
@AllArgsConstructor
@Rule(value = "生成随机整数",
        group = RuleGroup.工具插件,
        explain = "生成随机整数",
        test = true,
        order = 30,
        returnType = ClassType.数字,
//        iconUrl = "rule-ziyuan",
        testShowEnum = TestShowEnum.TEXT
)
public class RandomNumberServiceImpl implements BaseCustomFunctionInterface<RandomRangeDto> {

    @Override
    public Object execute(RandomRangeDto randomRangeDto, Map<String, Object> params) {
        Long min = randomRangeDto.getMin();
        Long max = randomRangeDto.getMax();
        if (min >= max) {
            throw new BusinessException("取值范围异常");
        }
        return RandomUtil.getRandom().nextLong(min, max);
    }

}
