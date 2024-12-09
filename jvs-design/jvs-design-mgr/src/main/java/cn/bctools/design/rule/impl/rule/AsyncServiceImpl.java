package cn.bctools.design.rule.impl.rule;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * 同步线中聚合操作，如果需要聚合操作时才使用此节点，否则会继续向下执行
 */
@Rule(value = "同步线聚合",
        group = RuleGroup.常用插件,
        returnType = ClassType.文本,
        order = 4,
        explain = "与同步线配合使用，存在多条同步线需要同步等待时此节点会停止"
)
@AllArgsConstructor
public class AsyncServiceImpl implements BaseCustomFunctionInterface<AsyncDto> {

    @Override
    public Object execute(AsyncDto asyncDto, Map<String, Object> params) {
        //返回当前的节点数据
        return System.currentTimeMillis();
    }

}
