package cn.bctools.rule.business.listmap;


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
        value = "数组对象变量",
        group = RuleGroup.常用插件,
        order = 5,
        returnType = ClassType.数组,
        explain = "结构示例:[{}]"
)
public class ListMapServiceImpl implements BaseCustomFunctionInterface<ListMapDto> {

    @Override
    public Object execute(ListMapDto listDto, Map<String, Object> params) {
        return listDto.getContent();
    }

}
