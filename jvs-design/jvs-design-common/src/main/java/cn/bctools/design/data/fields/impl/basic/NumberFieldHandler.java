package cn.bctools.design.data.fields.impl.basic;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.form.html.InputNumberHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.enums.DataQueryType;
import cn.hutool.core.util.NumberUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 表单字段: 计数器
 *
 * @Author: GuoZi
 */
@Slf4j
@Component
@DesignField(value = "计数器", type = DataFieldType.inputNumber)
@AllArgsConstructor
public class NumberFieldHandler implements IDataFieldHandler<InputNumberHtml> {
    @Override
    public List<DataQueryType> getEnabledQueryTypes(InputNumberHtml html) {
        List<DataQueryType> types = new ArrayList<>();
        types.add(DataQueryType.eq);
        types.add(DataQueryType.gt);
        types.add(DataQueryType.ge);
        types.add(DataQueryType.lt);
        types.add(DataQueryType.le);
        return types;
    }

    @Override
    public Object getEchoValue(InputNumberHtml fieldDto, Object data, boolean override, Map<String, Object> lineData, String... paths) {
        if (!NumberUtil.isNumber(data.toString())) {
            throw new BusinessException("请检查配置字段[" + fieldDto.getLabel() + "],不是数字" + data);
        }
        Object value = ObjectNull.isNull(data) && ObjectNull.isNotNull(fieldDto.getDefaultValue()) ? fieldDto.getDefaultValue() : data;
        if (ObjectNull.isNull(value)) {
            return value;
        }
        int precision = Optional.ofNullable(fieldDto.getPrecision()).orElse(0);
        String prefix = (precision > 0) ? "#." : "#";
        String pattern = prefix + IntStream.range(0, precision).mapToObj(i -> "0")
                .collect(Collectors.joining());
        if (value instanceof CharSequence) {
            value = new BigDecimal((String) value);
        }
        return new BigDecimal(NumberUtil.decimalFormat(pattern, value));
    }


    @Override
    public Object checkDataFieldType(InputNumberHtml inputNumberHtml, Object o) throws Exception {
        if (!(o instanceof Number)) {
            try {
                BigDecimal bigDecimal = new BigDecimal(o.toString());
                if (inputNumberHtml.getPrecision() == 0) {
                    o = bigDecimal.longValue();
                } else {
                    o = bigDecimal.setScale(inputNumberHtml.getPrecision(), BigDecimal.ROUND_HALF_UP);
                }
            } catch (Exception e) {
                throw new RuntimeException("正确格式为数字");
            }
        }
        if (ObjectNull.isNotNull(inputNumberHtml.getMax())) {
            if (!inputNumberHtml.getMax().toString().contains("Infnity")) {
                if (o instanceof BigDecimal) {
                    if (((BigDecimal) o).compareTo(BigDecimal.valueOf(Long.valueOf(inputNumberHtml.getMax().toString()))) == 1) {
                        throw new RuntimeException("数据超过最大值");
                    }
                } else if (Double.parseDouble(o.toString()) > Double.parseDouble(inputNumberHtml.getMax().toString())) {
                    //判断是否超过最大最小
                    throw new RuntimeException("数据超过最大值");
                }
            }
        }
        if (ObjectNull.isNotNull(inputNumberHtml.getMin())) {
            if (!inputNumberHtml.getMax().toString().contains("Infnity")) {
                if (o instanceof BigDecimal) {
                    if (((BigDecimal) o).compareTo(BigDecimal.valueOf(Long.valueOf(inputNumberHtml.getMin().toString()))) == -1) {
                        throw new RuntimeException("数据超过最大值");
                    }
                } else if (Double.parseDouble(o.toString()) < Double.parseDouble(inputNumberHtml.getMin().toString())) {
                    throw new RuntimeException("数据小余最小值");
                }
            }
        }
        return o;
    }

    @Override
    public Map<String, Object> generate(String name, String field, List<String> dicData) {
        String str = "{\n" +
                "    \"prop\": \"" + field + "\",\n" +
                "    \"type\": \"inputNumber\",\n" +
                "    \"label\": \"" + name + "\",\n" +
                "    \"span\": 24,\n" +
                "    \"display\": true,\n" +
                "    \"status\": \"\",\n" +
                "    \"tips\": {\n" +
                "        \"text\": \"\",\n" +
                "        \"position\": \"right\"\n" +
                "    },\n" +
                "    \"showFrom\": [\n" +
                "        \"label\",\n" +
                "        \"span\",\n" +
                "        \"prop\",\n" +
                "        \"min\",\n" +
                "        \"max\",\n" +
                "        \"sqlType\",\n" +
                "        \"step\",\n" +
                "        \"stepstrictly\",\n" +
                "        \"precision\",\n" +
                "        \"disabled\",\n" +
                "        \"controlsposition\",\n" +
                "        \"placeholder\",\n" +
                "        \"defaultValue\",\n" +
                "        \"defaultUrl\"\n" +
                "    ],\n" +
                "    \"linkbind\": \"\",\n" +
                "    \"min\": -9007199254740991,\n" +
                "    \"max\": 9007199254740991,\n" +
                "    \"step\": 1,\n" +
                "    \"stepstrictly\": false,\n" +
                "    \"precision\": 0,\n" +
                "    \"disabled\": false,\n" +
                "    \"controlsposition\": \"right\",\n" +
                "    \"placeholder\": \"请输入" + name + "\",\n" +
                "    \"defaultValue\": 0,\n" +
                "    \"defaultUrl\": \"\",\n" +
                "    \"sqlType\": \"double\",\n" +
                "    \"rules\": [\n" +
                "        {\n" +
                "            \"required\": false,\n" +
                "            \"message\": \"请输入" + name + "\",\n" +
                "            \"trigger\": \"change\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"name\": \"" + DataFieldType.inputNumber.getDesc() + "\",\n" +
                "    \"defaultOrigin\": \"\"\n" +
                "}";
        return JSONObject.parseObject(str);
    }
}
