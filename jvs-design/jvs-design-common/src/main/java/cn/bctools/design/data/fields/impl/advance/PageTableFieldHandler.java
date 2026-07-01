package cn.bctools.design.data.fields.impl.advance;

import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.form.item.PageTableItemHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.impl.IMultipleTypeHandler;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author jvs
 */
@Slf4j
@Component
@DesignField(value = "内嵌列表页", type = DataFieldType.pageTable)
@AllArgsConstructor
public class PageTableFieldHandler extends IMultipleTypeHandler implements IDataFieldHandler<PageTableItemHtml> {

    @Override
    public Map<String, Object> generate(String name, String field, List<String> dicData) {
        String str = "{\n" +
                "    \"prop\": \"" + field + "\",\n" +
                "    \"type\": \"pageTable\",\n" +
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
                "        \"jurisdiction\",\n" +
                "        \"maxheight\",\n" +
                "        \"sqlType\"\n" +
                "    ],\n" +
                "    \"maxheight\": \"\",\n" +
                "    \"showJurisdiction\": [\n" +
                "        \"所有用户\"\n" +
                "    ],\n" +
                "    \"rules\": [\n" +
                "\n" +
                "    ],\n" +
                "    \"sqlType\": \"varchar\",\n" +
                "    \"linkbind\": \"\",\n" +
                "    \"name\": \"列表页\",\n" +
                "    \"disabled\": false\n" +
                "}";
        return JSONObject.parseObject(str);
    }
}
