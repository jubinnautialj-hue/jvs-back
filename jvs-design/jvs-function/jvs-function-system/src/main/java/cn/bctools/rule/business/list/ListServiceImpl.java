package cn.bctools.rule.business.list;


import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author guojing
 * @describe 执行一个查询方法
 */
@Slf4j
@Order(1)
@Service
@Rule(
        value = "数组变量",
        label = "Array variable",
        group = RuleGroup.常用插件,
        order = 4,
        returnType = ClassType.数组,
        explain = "结构示例:[1,2,3]"
)
public class ListServiceImpl implements BaseCustomFunctionInterface<ListDto> {

    @Override
    public Object execute(ListDto listDto, Map<String, Object> params) {
        return listDto.getContent();
    }

}
