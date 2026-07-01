package cn.bctools.rule.business.map;


import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.RuleElementUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author guojing
 * @describe 执行一个查询方法
 */
@Slf4j
@Order(1)
@Service
@Rule(
        value = "对象变量",
        group = RuleGroup.常用插件,
        order = 3,
        returnType = ClassType.对象,
        explain = "结构示例:{}"
)
public class MapServiceImpl implements BaseCustomFunctionInterface<MapDto> {

    @Override
    public Object execute(MapDto customDto, Map<String, Object> params) {
        return customDto.getContent();
    }

    @Override
    public List<RuleElementVo> structureType(MapDto o) {
        return RuleElementUtils.get(o.getContent());
    }

}
