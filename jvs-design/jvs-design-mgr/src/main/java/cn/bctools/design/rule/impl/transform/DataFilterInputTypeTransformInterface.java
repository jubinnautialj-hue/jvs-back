package cn.bctools.design.rule.impl.transform;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.design.data.fields.enums.LinkTypeDto;
import cn.bctools.function.handler.ExpressionHandler;
import cn.bctools.function.service.FunctionBusinessService;
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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wl
 */
@Slf4j
@Service
@AllArgsConstructor
public class DataFilterInputTypeTransformInterface implements InputTypeTransformInterface {

    @Override
    public InputType name() {
        return InputType.map;
    }

    @Override
    public void transform(String key, Map<String, Object> body, Object data, Map<String, Object> stringObjectMap, String useCase) {
        if (ObjectNull.isNull(data)) {
            return;
        }
        //特殊类型转换
        LinkedHashMap<Object, Object> parBody = new LinkedHashMap<>(1);
        //如果为空直接返回
        if (ObjectNull.isNull(data)) {
            body.put(key, parBody);
            return;
        }
        JSONArray.parseArray(JSONObject.toJSONString(data), LinkTypeDto.class)
                .forEach(e -> {
                    if (ObjectNull.isNotNull(e.getProp())) {
                        if (e.getProp().equals(LinkTypeEnum.empty)) {
                            parBody.put(e.getFieldKey(), "");
                        }
                        if (e.getProp().equals(LinkTypeEnum.field)) {
                            if (ObjectNull.isNotNull(e.getValue().toString())) {
                                //根据判断是否只是入参的key
                                Object obj = SpringContextUtil.getBean(ExpressionHandler.class).calculate("${RULE" + e.getValue().toString() + "}", stringObjectMap, "RULE");
                                parBody.put(e.getFieldKey(), obj);
                            }
                        }
                        if (e.getProp().equals(LinkTypeEnum.value)) {
                            if (ObjectNull.isNotNull(e.getValue())) {
                                parBody.put(e.getFieldKey(), e.getValue());
                            }
                        }
                        if (e.getProp().equals(LinkTypeEnum.formula)) {
                            String expression = e.getFormulaContent();
                            List<ExpressionParam> expressionParams = ExpressionUtils.parsePostfixExpression(expression);
                            if (CollectionUtils.isNotEmpty(expressionParams)) {
                                Object result = SpringContextUtil.getBean(ExpressionHandler.class).calculate(expression, stringObjectMap, "RULE");
                                if (ObjectNull.isNull(result)) {
                                    SpringContextUtil.getBean(FunctionBusinessService.class).checkType(e.getFormula(), result, e.getFieldKey() + "不能为空");
                                }
                                parBody.put(e.getFieldKey(), result);
                            }
                        }
                    }
                });
        body.put(key, parBody);
    }

}
