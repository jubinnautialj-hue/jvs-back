package cn.bctools.design.data.fields.impl.advance;

import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.enums.DataQueryType;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.DataFormatException;

/**
 * @author guojing
 */
@Slf4j
@Component
@DesignField(value = "JSON编译器", type = DataFieldType.jsonEditor)
public class JsonEditorFieldHandler implements IDataFieldHandler<FieldBasicsHtml> {

    List<DataQueryType> types;

    @Override
    public List<DataQueryType> getEnabledQueryTypes(FieldBasicsHtml fieldJson) {
        if (Objects.isNull(this.types)) {
            List<DataQueryType> types = new ArrayList<>();
            types.add(DataQueryType.eq);
            types.add(DataQueryType.ne);
            types.add(DataQueryType.like);
            this.types = types;
        }
        return this.types;
    }

    @Override
    public void checkDataFieldType(FieldBasicsHtml fieldBasicsHtml, Object o) throws Exception {
        if (!JSONUtil.isTypeJSON(o.toString())) {
            throw new DataFormatException("类型不正确");
        }
    }

    @Override
    public Map<String, Object> generate(String name, String field, List<String> dicData) {
        String str = "{\n" +
                "    \"prop\": \"" + field + "\",\n" +
                "    \"type\": \"jsonEditor\",\n" +
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
                "        \"jurisdiction\",\n" +
                "        \"disabled\",\n" +
                "        \"span\",\n" +
                "        \"sqlType\"\n" +
                "    ],\n" +
                "    \"showJurisdiction\": [\n" +
                "        \"所有用户\"\n" +
                "    ],\n" +
                "    \"sqlType\": \"varchar\",\n" +
                "    \"linkbind\": \"\",\n" +
                "    \"rules\": [\n" +
                "        {\n" +
                "            \"required\": false,\n" +
                "            \"message\":  \"请输入" + name + "\",\n" +
                "            \"trigger\": \"change\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"name\": \"" + DataFieldType.jsonEditor.getDesc() + "\",\n" +
                "    \"disabled\": false\n" +
                "}";
        return JSONObject.parseObject(str);
    }
}
