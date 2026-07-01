package cn.bctools.design.rule.impl.transform;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.design.data.fields.enums.LinkTypeDto;
import cn.bctools.function.handler.ExpressionHandler;
import cn.bctools.function.utils.ExpressionParam;
import cn.bctools.function.utils.ExpressionUtils;
import cn.bctools.rule.dto.LinkTypeEnum;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.rule.entity.enums.InputTypeTransformInterface;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wl
 */
@Slf4j
@Service
@AllArgsConstructor
public class ListDataFilterInputTypeTransformInterface implements InputTypeTransformInterface {

    @Override
    public InputType name() {
        return InputType.listMap;
    }

    @Override
    public void transform(String key, Map<String, Object> body, Object data, Map<String, Object> stringObjectMap, String useCase) {
        if (ObjectNull.isNull(data)) {
            return;
        }
        //特殊类型转换
        HashMap<Object, Object> bodyd = new LinkedHashMap<>(1);
        //如果为空直接返回
        if (ObjectNull.isNull(data)) {
            body.put(key, bodyd);
            return;
        }
        Object collect = ((ArrayList) data).stream()
                .map(s -> {
                    HashMap<Object, Object> parBody = new HashMap<>(1);
                    JSONArray.parseArray(JSONObject.toJSONString(s), LinkTypeDto.class).stream().forEach(e -> {
                        if (ObjectNull.isNotNull(e.getProp())) {
                            if (e.getProp().equals(LinkTypeEnum.value)) {
                                if (ObjectNull.isNotNull(e.getValue())) {
                                    parBody.put(e.getFieldKey(), e.getValue());
                                }
                            }
                            if (e.getProp().equals(LinkTypeEnum.formula)) {
                                String expression = e.getFormulaContent();
                                List<ExpressionParam> expressionParams = ExpressionUtils.parsePostfixExpression(expression);
                                if (CollectionUtils.isNotEmpty(expressionParams)) {
                                    try {
                                        Object result = SpringContextUtil.getBean(ExpressionHandler.class).calculate(expression, stringObjectMap, "RULE");
                                        parBody.put(e.getFieldKey(), result);
                                    } catch (Exception ignored) {
                                    }
                                }
                            }
                        }

                    });
                    return parBody;
                }).collect(Collectors.toList());
        body.put(key, collect);
    }

}
