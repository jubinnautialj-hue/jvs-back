package cn.bctools.design.workflow.expression;

import cn.bctools.design.expression.EnvConstant;
import cn.bctools.design.workflow.enums.DefaultParamEnums;
import cn.bctools.function.entity.vo.ElementVo;
import cn.bctools.function.handler.IJvsParam;
import cn.bctools.function.handler.JvsExpression;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 */
@Slf4j
@Service
@AllArgsConstructor
@JvsExpression(groupName = "工作流默认字段", prefix = "DEFAULT", useCase = EnvConstant.FLOW_FUNCTION_ITEM_VALUE)
public class FlowDefaultItemParam implements IJvsParam<ElementVo> {

    private static final List<ElementVo> PARAMS = new ArrayList<>();

    static {
        PARAMS.addAll(Arrays.stream(DefaultParamEnums.values()).map(param ->
                new ElementVo()
                        .setId(param.getId())
                        .setName(param.getName())
                        .setInfo(param.getInfo())
                        .setJvsParamType(param.getJvsParamType())
        ).collect(Collectors.toList()));
    }

    @Override
    public List<ElementVo> getAllElements() {
        return PARAMS;
    }

    @Override
    public Object get(String paramName, Map<String, Object> data) {
        return data.get(paramName);
    }

}
