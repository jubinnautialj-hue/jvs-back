package cn.bctools.design.data.fields.impl.form;

import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.form.item.BaseItemHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 只用于数据服务。
 *
 * @author jvs
 */
@Slf4j
@Component
@DesignField(value = "动态表单", type = DataFieldType.dynamicForm)
public class DynamicFormHandler implements IDataFieldHandler<BaseItemHtml> {

    @Override
    public Map<String, Object> generate(String name, String field, List<String> dicData) {
        String str = "{\n" +
                "    \"prop\": \"" + field + "\",\n" +
                "    \"type\": \"dynamicForm\",\n" +
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
                "        \"col\",\n" +
                "        \"prop\",\n" +
                "        \"span\",\n" +
                "        \"sqlType\"\n" +
                "    ],\n" +
                "    \"rules\": [\n" +
                "\n" +
                "    ],\n" +
                "    \"column\": [\n" +
                "\n" +
                "    ],\n" +
                "    \"name\":  \"" + DataFieldType.dynamicForm.getDesc() + "\",\n" +
                "    \"disabled\": false,\n" +
                "    \"sqlType\": \"varchar\"\n" +
                "}";
        return JSONObject.parseObject(str);
    }
}

