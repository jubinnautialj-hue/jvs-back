package cn.bctools.design.data.fields.impl.basic;

import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.form.html.BoxHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;

/**
 * 表单字段: 按钮
 *
 * @Author: GuoZi
 */
@Slf4j
@Component
@DesignField(value = "描述框", type = DataFieldType.box)
@AllArgsConstructor
public class BoxFieldHandler implements IDataFieldHandler<BoxHtml> {
    @Override
    public Object checkDataFieldType(BoxHtml boxHtml, Object o) throws Exception {
        if (!(o instanceof String)) {
            throw new DataFormatException("类型不正确");
        }
        return o;
    }


    @Override
    public Map<String, Object> generate(String name, String field, List<String> dicData) {
        String str = "{\n" +
                "        \"prop\": \"" + field + "\",\n" +
                "            \"type\": \"box\",\n" +
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
                "                \"text\",\n" +
                "                \"contentposition\",\n" +
                "                \"fontsize\",\n" +
                "                \"textcolor\",\n" +
                "                \"fontweight\",\n" +
                "                \"sqlType\"\n" +
                "    ],\n" +
                "        \"text\": \"这是一段描述\",\n" +
                "            \"contentposition\": \"left\",\n" +
                "            \"fontsize\": 13,\n" +
                "            \"textcolor\": \"#909399\",\n" +
                "            \"boxback\": \"#f4f4f5\",\n" +
                "            \"fontweight\": \"normal\",\n" +
                "            \"rules\": [\n" +
                "\n" +
                "    ],\n" +
                "        \"fontweightOption\": [\n" +
                "        \"100\",\n" +
                "                \"200\",\n" +
                "                \"300\",\n" +
                "                \"400\",\n" +
                "                \"500\",\n" +
                "                \"600\",\n" +
                "                \"700\",\n" +
                "                \"800\",\n" +
                "                \"900\",\n" +
                "                \"bold\",\n" +
                "                \"bolder\",\n" +
                "                \"lighter\",\n" +
                "                \"normal\",\n" +
                "                \"unset\"\n" +
                "    ],\n" +
                "        \"showJurisdiction\": [\n" +
                "        \"所有用户\"\n" +
                "    ],\n" +
                "        \"sqlType\": \"varchar\",\n" +
                "            \"linkbind\": \"\",\n" +
                "            \"name\": \"" + DataFieldType.box.getDesc() + "\",\n" +
                "            \"disabled\": false\n" +
                "    }";
        return JSONObject.parseObject(str);
    }


}
