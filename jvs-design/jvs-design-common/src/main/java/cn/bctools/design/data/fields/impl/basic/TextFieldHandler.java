package cn.bctools.design.data.fields.impl.basic;

import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.enums.DataQueryType;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 表单字段: 文本类
 *
 * @Author: GuoZi
 */
@Slf4j
@Component
@DesignField(value = "多行文本", type = DataFieldType.textarea)
public class TextFieldHandler implements IDataFieldHandler<FieldBasicsHtml> {

    List<DataQueryType> types;

    @Override
    public List<DataQueryType> getEnabledQueryTypes(FieldBasicsHtml fieldJson) {
        if (Objects.isNull(this.types)) {
            List<DataQueryType> types = new ArrayList<>();
            types.add(DataQueryType.ne);
            types.add(DataQueryType.eq);
            types.add(DataQueryType.like);
            this.types = types;
        }
        return this.types;
    }

    @Override
    public Map<String, Object> generate(String name, String field, List<String> dicData) {
        String str = "{\n" +
                "    \"prop\": \"" + field + "\",\n" +
                "    \"type\": \"textarea\",\n" +
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
                "        \"rows\",\n" +
                "        \"minlength\",\n" +
                "        \"maxlength\",\n" +
                "        \"showwordlimit\",\n" +
                "        \"placeholder\",\n" +
                "        \"clearable\",\n" +
                "        \"disabled\",\n" +
                "        \"defaultValue\",\n" +
                "        \"sqlType\"\n" +
                "    ],\n" +
                "    \"linkbind\": \"\",\n" +
                "    \"rows\": 1,\n" +
                "    \"minlength\": 0,\n" +
                "    \"maxlength\": 100,\n" +
                "    \"showwordlimit\": false,\n" +
                "    \"placeholder\": \"请输入" + name + "\",\n" +
                "    \"clearable\": true,\n" +
                "    \"disabled\": false,\n" +
                "    \"defaultValue\": \"\",\n" +
                "    \"sqlType\": \"varchar\",\n" +
                "    \"rules\": [\n" +
                "        {\n" +
                "            \"required\": false,\n" +
                "            \"message\": \"请输入" + name + "\",\n" +
                "            \"trigger\": \"change\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"name\": \"" + DataFieldType.textarea.getDesc() + "\"\n" +
                "}";

        return JSONObject.parseObject(str);
    }
}
