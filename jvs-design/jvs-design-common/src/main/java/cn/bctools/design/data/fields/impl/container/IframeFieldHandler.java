package cn.bctools.design.data.fields.impl.container;

import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.form.html.IframeHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 表单字段: 网页
 *
 * @Author: GuoZi
 */
@Slf4j
@Component
@DesignField(value = "网页", type = DataFieldType.iframe)
@AllArgsConstructor
public class IframeFieldHandler implements IDataFieldHandler<IframeHtml> {
    @Override
    public Object checkDataFieldType(IframeHtml iframeHtml, Object o) throws Exception {
        if (!(o instanceof String)) {
            throw new RuntimeException("正确格式为链接地址");
        }
        return o;
    }

    @Override
    public Map<String, Object> generate(String name, String field, List<String> dicData) {
        String str = "{\n" +
                "    \"prop\": \"" + field + "\",\n" +
                "    \"type\": \"iframe\",\n" +
                "    \"label\": \"\",\n" +
                "    \"span\": 24,\n" +
                "    \"display\": true,\n" +
                "    \"status\": \"\",\n" +
                "    \"tips\": {\n" +
                "        \"text\": \"\",\n" +
                "        \"position\": \"right\"\n" +
                "    },\n" +
                "    \"showFrom\": [\n" +
                "        \"prop\",\n" +
                "        \"defaultValue\",\n" +
                "        \"jurisdiction\",\n" +
                "        \"span\",\n" +
                "        \"iframeheight\",\n" +
                "        \"sqlType\"\n" +
                "    ],\n" +
                "    \"iframeheight\": \"\",\n" +
                "    \"iframeurl\": \"\",\n" +
                "    \"showJurisdiction\": [\n" +
                "        \"所有用户\"\n" +
                "    ],\n" +
                "    \"rules\": [\n" +
                "\n" +
                "    ],\n" +
                "    \"sqlType\": \"varchar\",\n" +
                "    \"linkbind\": \"\",\n" +
                "    \"name\": \"" + DataFieldType.iframe.getDesc() + "\",\n" +
                "    \"disabled\": false\n" +
                "}";
        return JSONObject.parseObject(str);
    }
}
