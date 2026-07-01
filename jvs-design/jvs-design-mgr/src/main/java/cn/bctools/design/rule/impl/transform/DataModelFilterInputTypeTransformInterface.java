package cn.bctools.design.rule.impl.transform;


import cn.bctools.common.exception.BusinessException;
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
public class DataModelFilterInputTypeTransformInterface implements InputTypeTransformInterface {

    @Override
    public InputType name() {
        return InputType.dataModelFilterField;
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
                .filter(e -> ObjectNull.isNotNull(e.getProp(), e.getEnabledQueryTypes()))
                .peek(e -> {
                    if (e.getProp().equals(LinkTypeEnum.empty)) {
                        e.setValue(LinkTypeEnum.empty.toString());
                    }
                    if (e.getProp().equals(LinkTypeEnum.field)) {
                        if (ObjectNull.isNotNull(e.getValue().toString())) {
                            Object obj = SpringContextUtil.getBean(ExpressionHandler.class).calculate("${RULE" + e.getValue().toString() + "}", stringObjectMap, "RULE");
                            //根据判断是否只是入参的key
                            if (ObjectNull.isNull(obj)) {
                                e.setValue(LinkTypeEnum.empty.toString());
                            } else {
                                e.setValue(obj);
                            }
                        }
                    }
                    if (e.getProp().equals(LinkTypeEnum.value)) {
                        if (ObjectNull.isNotNull(e.getValue())) {
                            e.setValue(e.getValue());
                        } else {
                            e.setValue(LinkTypeEnum.empty.toString());
                        }
                    }
                    if (e.getProp().equals(LinkTypeEnum.formula)) {
                        String expression = e.getFormulaContent();
                        List<ExpressionParam> expressionParams = ExpressionUtils.parsePostfixExpression(expression);
                        if (expressionParams.isEmpty()) {

                        } else {
                            try {
                                Object result = SpringContextUtil.getBean(ExpressionHandler.class).calculate(expression, stringObjectMap, "RULE");
                                if (ObjectNull.isNull(result)) {
                                    if (ObjectNull.isNull(result)) {
                                        SpringContextUtil.getBean(FunctionBusinessService.class).checkType(e.getFormula(), result, e.getFieldKey() + "不能为空");
                                    }
                                    e.setValue(LinkTypeEnum.empty.toString());
                                } else {
                                    e.setValue(result);
                                }
                            } catch (BusinessException exception) {
                                throw exception;
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }
                        }
                    }
                }).collect(Collectors.toList());
        body.put(key, rule);
    }
}

