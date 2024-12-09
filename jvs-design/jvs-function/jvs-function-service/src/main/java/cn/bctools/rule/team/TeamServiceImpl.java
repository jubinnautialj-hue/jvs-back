package cn.bctools.rule.team;

//import cn.bctools.data.factory.api.DataFactoryApi;
import cn.bctools.rule.annotations.Rule;

import cn.bctools.rule.dto.BindDto;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Order(1)
@Service
@AllArgsConstructor
@Rule(
        value = "创建工作团队",
        group = RuleGroup.服务插件,
        test = true,
        testShowEnum = TestShowEnum.JSON,
        returnType = ClassType.数组,
//        iconUrl = "rule-page_icon",
        customStructure = true,
        enable = false,
        order = 1,
        explain = "智仓分页查询"
)
public class TeamServiceImpl implements BaseCustomFunctionInterface<BindDto> {

    @Override
    public Object execute(BindDto bindDto, Map<String, Object> params) {
        return null;
    }
}
