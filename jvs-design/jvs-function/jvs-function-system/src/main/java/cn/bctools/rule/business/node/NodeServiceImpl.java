package cn.bctools.rule.business.node;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author st
 */
@AllArgsConstructor
@Rule(value = "固定变量",
        group = RuleGroup.常用插件,
        test = true,
        returnType = ClassType.未识别,
        testShowEnum = TestShowEnum.TEXT,
        order = 2,
        explain = "支持对象，数组,数组对象，文件等变量"
)
public class NodeServiceImpl implements BaseCustomFunctionInterface<NodeDto> {

    @Override
    public Object execute(NodeDto dto, Map<String, Object> params) {
        LOG.info(JSON.toJSONString(dto));
        if (ObjectNull.isNull(dto.getBody())) {
            //如果是数组则不删除
            if (!(dto.getBody() instanceof List)) {
                return "";
            }
        }
        return dto.getBody();
    }
}
