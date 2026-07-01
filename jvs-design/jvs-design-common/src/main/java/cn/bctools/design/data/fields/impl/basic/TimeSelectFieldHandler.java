package cn.bctools.design.data.fields.impl.basic;

import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.form.html.TimeSelectHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.hutool.core.date.DatePattern;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 表单字段: 固定时间
 *
 * @Author: GuoZi
 */
@Slf4j
@Component
@DesignField(value = "固定时间", type = DataFieldType.timeSelect)
@AllArgsConstructor
public class TimeSelectFieldHandler implements IDataFieldHandler<TimeSelectHtml> {

    @Override
    public Object checkDataFieldType(TimeSelectHtml timeSelectHtml, Object o) throws Exception {
        try {
            DatePattern.createFormatter("mm:ss").parse(o.toString());
        } catch (Exception e) {
            throw new RuntimeException("正确格式为mm:ss");
        }
        return o;
    }

    @Override
    public Object getConversionKey(TimeSelectHtml dto, Object o, Map<String, Object> lineData) {
        return o.toString();
    }

    @Override
    public Map<String, Object> generate(String name, String field, List<String> dicData) {
        String str = "{\n" +
                "    \"prop\": \"" + field + "\",\n" +
                "    \"type\": \"timeSelect\",\n" +
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
                "        \"disabled\",\n" +
                "        \"placeholder\",\n" +
                "        \"pickeroptions\",\n" +
                "        \"clearable\",\n" +
                "        \"sqlType\",\n" +
                "        \"prefixicon\"\n" +
                "    ],\n" +
                "    \"linkbind\": \"\",\n" +
                "    \"disabled\": false,\n" +
                "    \"placeholder\": \"请输入" + name + "\",\n" +
                "    \"clearable\": false,\n" +
                "    \"prefixicon\": \"el-icon-time\",\n" +
                "    \"pickeroptions\": {\n" +
                "        \"start\": \"09:00\",\n" +
                "        \"end\": \"18:00\",\n" +
                "        \"step\": \"00:15\"\n" +
                "    },\n" +
                "    \"defaultValue\": \"09:00\",\n" +
                "    \"sqlType\": \"date\",\n" +
                "    \"rules\": [\n" +
                "        {\n" +
                "            \"required\": false,\n" +
                "            \"message\": \"请输入" + name + "\",\n" +
                "            \"trigger\": \"change\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"name\": \"" + DataFieldType.timeSelect.getDesc() + "\"\n" +
                "}";
        return JSONObject.parseObject(str);
    }
}
