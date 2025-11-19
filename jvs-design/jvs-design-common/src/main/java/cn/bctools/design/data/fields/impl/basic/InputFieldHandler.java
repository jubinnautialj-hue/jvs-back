package cn.bctools.design.data.fields.impl.basic;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.enums.DataQueryType;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author guojing
 */
@Slf4j
@Component
@DesignField(value = "单行文本", type = DataFieldType.input)
public class InputFieldHandler implements IDataFieldHandler<FieldBasicsHtml> {

    List<DataQueryType> types;

    @Override
    public List<DataQueryType> getEnabledQueryTypes(FieldBasicsHtml fieldJson) {
        if (Objects.isNull(this.types)) {
            List<DataQueryType> types = new ArrayList<>();
            types.add(DataQueryType.eq);
            types.add(DataQueryType.ne);
            types.add(DataQueryType.like);
            types.add(DataQueryType.in);
            this.types = types;
        }
        return this.types;
    }

    @Override
    public Object checkDataFieldType(FieldBasicsHtml fieldBasicsHtml, Object o) throws Exception {
        try {
            if (ObjectNull.isNotNull(fieldBasicsHtml.getEncryptionExpress())) {
                if (!Pattern.compile(fieldBasicsHtml.getEncryptionExpress()).matcher(o.toString()).matches()) {
                    throw new RuntimeException("文本组件格式不正确");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("文本组件校验正则配置错误");
        }
        return o;
    }

    @Override
    public Map<String, Object> generate(String name, String field, List<String> dicData) {

        String str = "{\n" +
                "    \"prop\": \"" + field + "\",\n" +
                "    \"type\": \"input\",\n" +
                "    \"label\": \"" + name + "\",\n" +
                "    \"span\": 24,\n" +
                "    \"display\": true,\n" +
                "    \"componentType\": \"input\",\n" +
                "    \"status\": \"\",\n" +
                "    \"tips\": {\n" +
                "        \"text\": \"\",\n" +
                "        \"position\": \"right\"\n" +
                "    },\n" +
                "    \"showFrom\": [\n" +
                "        \"label\",\n" +
                "        \"span\",\n" +
                "        \"prop\",\n" +
                "        \"minlength\",\n" +
                "        \"maxlength\",\n" +
                "        \"showwordlimit\",\n" +
                "        \"placeholder\",\n" +
                "        \"sqlType\",\n" +
                "        \"clearable\",\n" +
                "        \"showpassword\",\n" +
                "        \"disabled\",\n" +
                "        \"prefixicon\",\n" +
                "        \"suffixicon\",\n" +
                "        \"prepend\",\n" +
                "        \"append\",\n" +
                "        \"defaultValue\",\n" +
                "        \"regular\",\n" +
                "        \"defaultUrl\"\n" +
                "    ],\n" +
                "    \"linkbind\": \"\",\n" +
                "    \"minlength\": 0,\n" +
                "    \"maxlength\": 200,\n" +
                "    \"showwordlimit\": false,\n" +
                "    \"placeholder\": \"请输入" + name + "\",\n" +
                "    \"clearable\": true,\n" +
                "    \"showpassword\": false,\n" +
                "    \"disabled\": false,\n" +
                "    \"prefixicon\": \"el-icon-edit\",\n" +
                "    \"suffixicon\": \"\",\n" +
                "    \"prepend\": \"\",\n" +
                "    \"append\": \"\",\n" +
                "    \"defaultValue\": \"\",\n" +
                "    \"regularExpression\": \"\",\n" +
                "    \"regularMessage\": \"\",\n" +
                "    \"defaultUrl\": \"\",\n" +
                "    \"sqlType\": \"varchar\",\n" +
                "    \"rules\": [\n" +
                "        {\n" +
                "            \"required\": false,\n" +
                "            \"message\": \"请输入" + name + "\",\n" +
                "            \"trigger\": \"change\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"name\": \"" + DataFieldType.input.getDesc() + "\",\n" +
                "    \"defaultOrigin\": \"\"\n" +
                "}";
        return JSONObject.parseObject(str);
    }
}
