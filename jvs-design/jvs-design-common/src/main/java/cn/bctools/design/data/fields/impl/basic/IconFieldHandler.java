package cn.bctools.design.data.fields.impl.basic;

import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.form.html.IconSelectHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 表单字段: 图标选择
 *
 * @Author: GuoZi
 */
@Slf4j
@Component
@DesignField(value = "图标选择", type = DataFieldType.iconSelect)
@AllArgsConstructor
public class IconFieldHandler implements IDataFieldHandler<IconSelectHtml> {

    @Override
    public Map<String, Object> generate(String name, String field, List<String> dicData) {
        String str = "{\n" +
                "        \"prop\": \"" + field + "\",\n" +
                "            \"type\": \"iconSelect\",\n" +
                "            \"label\": \"" + name + "\",\n" +
                "            \"span\": 24,\n" +
                "            \"display\": true,\n" +
                "            \"status\": \"\",\n" +
                "            \"tips\": {\n" +
                "        \"text\": \"\",\n" +
                "                \"position\": \"right\"\n" +
                "    },\n" +
                "        \"showFrom\": [\n" +
                "        \"prop\",\n" +
                "                \"label\",\n" +
                "                \"jurisdiction\",\n" +
                "                \"span\",\n" +
                "                \"fontsize\",\n" +
                "                \"fontweight\",\n" +
                "                \"sqlType\",\n" +
                "                \"disabled\"\n" +
                "                    ],\n" +
                "        \"showJurisdiction\": [\n" +
                "        \"所有用户\"\n" +
                "                    ],\n" +
                "        \"sqlType\": \"varchar\",\n" +
                "            \"linkbind\": \"\",\n" +
                "            \"rules\": [\n" +
                "        {\n" +
                "            \"required\": false,\n" +
                "                \"message\":  \"请选择" + name + "\",\n" +
                "                \"trigger\": \"change\"\n" +
                "        }\n" +
                "                    ],\n" +
                "        \"name\":  \"" + DataFieldType.iconSelect.getDesc() + "\",\n" +
                "            \"disabled\": false,\n" +
                "            \"iconType\": \"svg\"\n" +
                "    }";
        return JSONObject.parseObject(str);
    }



}
