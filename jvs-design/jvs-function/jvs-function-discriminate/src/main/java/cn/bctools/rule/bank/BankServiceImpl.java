package cn.bctools.rule.bank;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author guojing
 */
@Slf4j
@AllArgsConstructor
@Rule(value = "银行卡识别",
        group = RuleGroup.识别插件,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        enable = false
//        iconUrl = "rule-daVrenzheng",

)
public class BankServiceImpl implements BaseCustomFunctionInterface<BankDto> {

    static final String URL = "http://cardiddecode.market.alicloudapi.com/api/decode_cardid_aliyun";

    @Override
    public Object execute(BankDto dto, Map<String, Object> params) {
        return "不支持";
    }

}
