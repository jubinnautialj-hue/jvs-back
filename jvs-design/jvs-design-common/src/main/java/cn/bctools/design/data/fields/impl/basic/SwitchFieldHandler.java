package cn.bctools.design.data.fields.impl.basic;

import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.form.html.SwitchHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.enums.DataQueryType;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 表单字段: 开关
 *
 * @Author: GuoZi
 */
@Slf4j
@Component
@DesignField(value = "开关", type = DataFieldType.SWITCH)
@AllArgsConstructor
public class SwitchFieldHandler implements IDataFieldHandler<SwitchHtml> {

    @Override
    public List<DataQueryType> getEnabledQueryTypes(SwitchHtml fieldJson) {
        List<DataQueryType> enabledQueryTypes = new ArrayList<>();
        enabledQueryTypes.add(DataQueryType.eq);
        return enabledQueryTypes;
    }

    @Override
    public void checkDataFieldType(SwitchHtml switchHtml, Object o) throws Exception {
        if (!(o instanceof Boolean)) {
            throw new RuntimeException("正确格式为真假");
        }
    }

    @Override
    public Object getEchoValue(SwitchHtml fieldDto, Object data, boolean override, Map<String, Object> lineData, String... paths) {
        return Boolean.TRUE.equals(data) ? fieldDto.getActivetext() : fieldDto.getInactivetext();
    }

    @Override
    public Map<String, Object> generate(String name, String field, List<String> dicData) {
        String str = "{\n" +
                "    \"prop\": \"" + field + "\",\n" +
                "    \"type\": \"SWITCH\",\n" +
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
                "        \"activetext\",\n" +
                "        \"inactivetext\",\n" +
                "        \"activecolor\",\n" +
                "        \"inactivecolor\",\n" +
                "        \"defaultValue\",\n" +
                "        \"defaultUrl\",\n" +
                "        \"sqlType\",\n" +
                "        \"hasChildren\"\n" +
                "    ],\n" +
                "    \"linkbind\": \"\",\n" +
                "    \"hasChildren\": false,\n" +
                "    \"children\": [],\n" +
                "    \"disabled\": false,\n" +
                "    \"activetext\": \"开\",\n" +
                "    \"inactivetext\": \"关\",\n" +
                "    \"activecolor\": \"#409EFF\",\n" +
                "    \"inactivecolor\": \"#C0CCDA\",\n" +
                "    \"defaultValue\": false,\n" +
                "    \"defaultUrl\": \"\",\n" +
                "    \"sqlType\": \"bit\",\n" +
                "    \"rules\": [\n" +
                "        {\n" +
                "            \"required\": false,\n" +
                "            \"message\": \"请输入" + name + "\",\n" +
                "            \"trigger\": \"change\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"name\": \"" + DataFieldType.SWITCH.getDesc() + "\"\n" +
                "}";
        return JSONObject.parseObject(str);
    }
}
