package cn.bctools.design.data.fields.impl.basic;

import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 表单字段: 文本类
 *
 * @Author: GuoZi
 */
@Slf4j
@Component
@DesignField(value = "流水号", type = DataFieldType.serialNumber)
public class SerialNumberFieldHandler implements IDataFieldHandler<FieldBasicsHtml> {
    @Override
    public Object checkDataFieldType(FieldBasicsHtml fieldBasicsHtml, Object o) throws Exception {
        if (!(o instanceof String)) {
            throw new RuntimeException("正确格式为字符串");
        }
        return o;
    }

    @Override
    public Map<String, Object> generate(String name, String field, List<String> dicData) {
        String str = "{\n" +
                "    \"prop\": \"" + field + "\",\n" +
                "    \"type\": \"serialNumber\",\n" +
                "    \"label\": \"" + name + "\",\n" +
                "    \"span\": 24,\n" +
                "    \"display\": true,\n" +
                "    \"status\": \"\",\n" +
                "    \"tips\": {\n" +
                "        \"text\": \"\",\n" +
                "        \"position\": \"right\"\n" +
                "    },\n" +
                "    \"showFrom\": [\n" +
                "        \"prop\",\n" +
                "        \"label\",\n" +
                "        \"span\",\n" +
                "        \"orderPrefix\",\n" +
                "        \"orderTimeMark\",\n" +
                "        \"orderDigit\",\n" +
                "        \"orderResetRule\",\n" +
                "        \"sqlType\"\n" +
                "    ],\n" +
                "    \"text\": \"前缀+时间标识+序号\",\n" +
                "    \"rules\": [],\n" +
                "    \"orderPrefix\": \"\",\n" +
                "    \"orderTimeMark\": \"n\",\n" +
                "    \"orderDigit\": 5,\n" +
                "    \"orderResetRule\": \"n\",\n" +
                "    \"name\": \"流水号\",\n" +
                "    \"disabled\": false,\n" +
                "    \"sqlType\": \"varchar\"\n" +
                "}";
        return JSONObject.parseObject(str);
    }
}
