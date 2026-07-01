package cn.bctools.design.data.fields.impl.flow;

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
 * @author guojing
 */
@Slf4j
@Component
@DesignField(value = "动态流程", type = DataFieldType.flowNode)
public class FlowNodeHandler implements IDataFieldHandler<BaseItemHtml> {

    @Override
    public Map<String, Object> generate(String name, String field, List<String> dicData) {
        String str = "{\n" +
                "    \"prop\": \"" + field + "\",\n" +
                "    \"type\": \"flowNode\",\n" +
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
                "        \"prop\",\n" +
                "        \"sqlType\",\n" +
                "        \"disabled\"\n" +
                "    ],\n" +
                "    \"sqlType\": \"varchar\",\n" +
                "    \"rules\": [\n" +
                "\n" +
                "    ],\n" +
                "    \"linkbind\": \"\",\n" +
                "    \"name\":  \"" + DataFieldType.flowNode.getDesc() + "\",\n" +
                "    \"disabled\": false\n" +
                "}";
        return JSONObject.parseObject(str);
    }
}
