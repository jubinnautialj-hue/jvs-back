package cn.bctools.design.rule.impl.transform;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.design.data.fields.enums.LinkTypeDto;
import cn.bctools.function.handler.ExpressionHandler;
import cn.bctools.function.utils.ExpressionParam;
import cn.bctools.function.utils.ExpressionUtils;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.rule.entity.enums.InputTypeTransformInterface;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author guojing
 */
@Slf4j
@Service
@AllArgsConstructor
public class FlowNodeInputTypeTransformInterface implements InputTypeTransformInterface {

    @Override
    public InputType name() {
        return InputType.flowNode;
    }

    @Override
    public void transform(String key, Map<String, Object> body, Object data, Map<String, Object> stringObjectMap, String useCase) {
        //特殊类型转换
        //如果为空直接返回
        if (ObjectNull.isNull(data)) {
            return;
        }
        List<LinkTypeDto> rule = JSONArray.parseArray(JSONObject.toJSONString(data), LinkTypeDto.class)
                .stream()
                .peek(e -> {
                    String expression = e.getFormulaContent();
                    List<ExpressionParam> expressionParams = ExpressionUtils.parsePostfixExpression(expression);
                    if (expressionParams.isEmpty()) {

                    } else {
                        try {
                            Object result = SpringContextUtil.getBean(ExpressionHandler.class).calculate(expression, stringObjectMap, "RULE");
                            e.setValue(result);
                        } catch (Exception exception) {
                            log.error("工作流节点转换执行公式出错", exception);
                        }
                    }
                }).collect(Collectors.toList());
        body.put(key, rule);
    }
}
