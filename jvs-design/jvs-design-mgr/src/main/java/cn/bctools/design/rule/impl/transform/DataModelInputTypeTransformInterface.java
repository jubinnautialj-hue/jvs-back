package cn.bctools.design.rule.impl.transform;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.design.constant.DynamicDataConstant;
import cn.bctools.design.data.fields.enums.LinkTypeDto;
import cn.bctools.function.handler.ExpressionHandler;
import cn.bctools.function.utils.ExpressionParam;
import cn.bctools.function.utils.ExpressionUtils;
import cn.bctools.rule.dto.LinkTypeEnum;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.rule.entity.enums.InputTypeTransformInterface;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author guojing
 */
@Slf4j
@Service
@AllArgsConstructor
public class DataModelInputTypeTransformInterface implements InputTypeTransformInterface {

    @Override
    public InputType name() {
        return InputType.dataModelField;
    }

    @Override
    public void transform(String key, Map<String, Object> body, Object data, Map<String, Object> stringObjectMap, String useCase) {
        //特殊类型转换
        HashMap<Object, Object> parBody = new HashMap<>(1);
        //如果为空直接返回
        if (ObjectNull.isNull(data)) {
            body.put(key, parBody);
            return;
        }
        String text = JSONObject.toJSONString(data);

        if (!JSONUtil.isTypeJSONArray(text)) {
            return;
        }
        List<LinkTypeDto> linkTypeDtos = JSONArray.parseArray(text, LinkTypeDto.class);
        for (int i = 0; i < linkTypeDtos.size(); i++) {
            LinkTypeDto e = linkTypeDtos.get(i);
            if (ObjectNull.isNotNull(e.getProp())) {
                if (e.getProp().equals(LinkTypeEnum.empty)) {
                    parBody.put(e.getFieldKey(), DynamicDataConstant.DATA_EMPTY);
                }
                if (e.getProp().equals(LinkTypeEnum.field)) {
                    if (ObjectNull.isNotNull(e.getValue().toString())) {
                        Object obj = SpringContextUtil.getBean(ExpressionHandler.class).calculate("${RULE" + e.getValue().toString() + "}", stringObjectMap, "RULE");
                        //根据判断是否只是入参的key
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
                    if (expressionParams.isEmpty()) {

                    } else {
                        try {
                            Object result = SpringContextUtil.getBean(ExpressionHandler.class).calculate(expression, stringObjectMap, "RULE");
                            parBody.put(e.getFieldKey(), result);
                        } catch (Exception exception) {
                            throw new BusinessException("获取公式数据值错误:" + i + "  " + JSONObject.toJSONString(e) + "  \n" + expression + "  \n" + exception.getMessage());
                        }
                    }
                }
            }
        };
        body.put(key, parBody);
    }
}
