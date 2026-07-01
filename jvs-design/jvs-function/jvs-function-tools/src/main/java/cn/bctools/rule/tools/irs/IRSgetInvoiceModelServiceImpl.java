package cn.bctools.rule.tools.irs;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;


@Slf4j
@AllArgsConstructor
@Rule(value = "IRS_SW数据请求组装参数",
        group = RuleGroup.常用插件,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
//        iconUrl = "rule-ga",
        customStructure = true,
        explain = "IRS_SW授权请求组装参数 "

)
public class IRSgetInvoiceModelServiceImpl implements BaseCustomFunctionInterface<IRSgetInvoiceModelDto> {

    @Override
    public Object execute(IRSgetInvoiceModelDto dto, Map<String, Object> params) {
        return IRSWSUtils.getInvoiceModel(dto);
    }
}

