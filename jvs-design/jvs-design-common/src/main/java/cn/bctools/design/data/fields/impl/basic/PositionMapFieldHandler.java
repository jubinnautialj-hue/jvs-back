package cn.bctools.design.data.fields.impl.basic;

import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author guojing
 */
@Slf4j
@Component
@DesignField(value = "定位", type = DataFieldType.positionMap)
@AllArgsConstructor
public class PositionMapFieldHandler implements IDataFieldHandler<FieldBasicsHtml> {
    @Override
    public void checkDataFieldType(FieldBasicsHtml fieldBasicsHtml, Object o) throws Exception {
        if (!(o instanceof Map)) {
            throw new RuntimeException("正确格式为键值对");
        }
    }

    @Override
    public Map<String, Object> generate(String name, String field, List<String> dicData) {
        String str = "{\n" +
                "    \"prop\": \"" + field + "\",\n" +
                "    \"type\": \"positionMap\",\n" +
                "    \"label\":  \"" + name + "\",\n" +
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
                "        \"disabled\",\n" +
                "        \"sqlType\"\n" +
                "    ],\n" +
                "    \"disabled\": false,\n" +
                "    \"rules\": [\n" +
                "\n" +
                "    ],\n" +
                "    \"name\": \"" + DataFieldType.positionMap.getDesc() + "\",\n" +
                "    \"sqlType\": \"varchar\"\n" +
                "}";
        return JSONObject.parseObject(str);
    }
}

