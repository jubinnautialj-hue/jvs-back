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
import java.util.zip.DataFormatException;

/**
 * 表单字段: 链接
 *
 * @Author: GuoZi
 */
@Slf4j
@Component
@DesignField(value = "链接", type = DataFieldType.link)
@AllArgsConstructor
public class LinkFieldHandler implements IDataFieldHandler<FieldBasicsHtml> {
    @Override
    public void checkDataFieldType(FieldBasicsHtml fieldBasicsHtml, Object o) throws Exception {
        if (!(o instanceof String)) {
            throw new DataFormatException("类型不正确");
        }
    }

    @Override
    public Map<String, Object> generate(String name, String field, List<String> dicData) {
        String str = "{\n" +
                "    \"prop\": \"" + field + "\",\n" +
                "    \"type\": \"link\",\n" +
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
                "        \"span\",\n" +
                "        \"text\",\n" +
                "        \"contentposition\",\n" +
                "        \"fontsize\",\n" +
                "        \"textcolor\",\n" +
                "        \"fontweight\",\n" +
                "        \"textdecoration\",\n" +
                "        \"openType\",\n" +
                "        \"sqlType\",\n" +
                "        \"defaultValue\"\n" +
                "    ],\n" +
                "    \"text\": \"显示文字\",\n" +
                "    \"contentposition\": \"center\",\n" +
                "    \"fontsize\": 16,\n" +
                "    \"textcolor\": \"#409EFF\",\n" +
                "    \"fontweight\": \"normal\",\n" +
                "    \"textdecoration\": \"none\",\n" +
                "    \"openType\": \"_blank\",\n" +
                "    \"rules\": [\n" +
                "\n" +
                "    ],\n" +
                "    \"fontweightOption\": [\n" +
                "        \"100\",\n" +
                "        \"200\",\n" +
                "        \"300\",\n" +
                "        \"400\",\n" +
                "        \"500\",\n" +
                "        \"600\",\n" +
                "        \"700\",\n" +
                "        \"800\",\n" +
                "        \"900\",\n" +
                "        \"bold\",\n" +
                "        \"bolder\",\n" +
                "        \"lighter\",\n" +
                "        \"normal\",\n" +
                "        \"unset\"\n" +
                "    ],\n" +
                "    \"textdecorationOption\": [\n" +
                "        \"blink\",\n" +
                "        \"dashed\",\n" +
                "        \"dotted\",\n" +
                "        \"double\",\n" +
                "        \"inherit\",\n" +
                "        \"initial\",\n" +
                "        \"line-through\",\n" +
                "        \"none\",\n" +
                "        \"overline\",\n" +
                "        \"solid\",\n" +
                "        \"underline\",\n" +
                "        \"unset\",\n" +
                "        \"wavy\"\n" +
                "    ],\n" +
                "    \"showJurisdiction\": [\n" +
                "        \"所有用户\"\n" +
                "    ],\n" +
                "    \"sqlType\": \"varchar\",\n" +
                "    \"linkbind\": \"\",\n" +
                "    \"name\": \"" + DataFieldType.link.getDesc() + "\",\n" +
                "    \"disabled\": false\n" +
                "}";
        return JSONObject.parseObject(str);
    }
}
