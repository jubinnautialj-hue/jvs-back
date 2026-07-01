package cn.bctools.design.rule.impl.header;


import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.web.utils.WebUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author guojing
 * @describe 执行获取指定请求头
 */
@Slf4j
@Order(1)
@Service
@Rule(
        value = "获取指定请求头",
        group = RuleGroup.常用插件,
        returnType = ClassType.文本,
        order = 10,
        explain = "获取此次请求header中指定key的值"
)
public class HeaderServiceImpl implements BaseCustomFunctionInterface<HeaderDto> {

    @Override
    public Object execute(HeaderDto listDto, Map<String, Object> params) {
        return WebUtils.getRequest().getHeader(listDto.getContent());
    }

}
