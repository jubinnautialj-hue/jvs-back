package cn.bctools.design.data.fields.impl.basic;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.form.html.CheckboxHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.enums.DataQueryType;
import cn.bctools.design.data.fields.impl.IMultipleTypeHandler;
import cn.bctools.design.data.fields.impl.ISelectorDataHandler;
import cn.hutool.core.lang.Dict;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 表单字段: 多选框
 *
 * @Author: GuoZi
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Component
@DesignField(value = "多选", type = DataFieldType.checkbox)
public class CheckBoxFieldHandler extends IMultipleTypeHandler implements IDataFieldHandler<CheckboxHtml>, ISelectorDataHandler {

    @Override
    public List<DataQueryType> getEnabledQueryTypes(CheckboxHtml html) {
        html.setMultiple(true);
        return super.getEnabledQueryTypes(html);
    }

    @Override
    public Object getEchoValue(CheckboxHtml fieldDto, Object data, boolean override, Map<String, Object> lineData, String... paths) {
        fieldDto.setMultiple(true);
        return echoValue(fieldDto, data, lineData);
    }

    @Override
    public String getConversionKey(CheckboxHtml dto, Object o, Map<String, Object> lineData, Map<String, Map<String, String>> cascaderFieldPathIdsMap, Map<String, List<Map<String, Object>>> generateCascaderList) {
        dto.setMultiple(true);
        return conversionKey(dto, o, lineData, cascaderFieldPathIdsMap, generateCascaderList);

    }

    @Override
    public Object getConversionKey(CheckboxHtml dto, Object o, Map<String, Object> lineData) {
        dto.setMultiple(true);
        return conversionKey(dto, o, lineData);
    }

    @Override
    public Boolean whetherCoverValue() {
        return Boolean.FALSE;
    }

    @Override
    public void checkDataFieldType(CheckboxHtml checkboxHtml, Object o) throws Exception {
        if (o instanceof String) {
            o = Arrays.stream(o.toString().split(",")).collect(Collectors.toList());
            return;
        }
        if (!(o instanceof List)) {
            throw new RuntimeException("正确格式为数组");
        }
    }

    @Override
    public Map<String, Object> generate(String name, String field, List<String> dicData) {
        if (ObjectNull.isNull(dicData)) {
            dicData = new ArrayList<>();
            dicData.add("未配置选项");
        }
        String jsonString = JSONObject.toJSONString(dicData.stream().map(e -> Dict.create().set("value", e).set("label", e)).collect(Collectors.toList()));

        String str = "{\n" +
                "    \"prop\": \"" + field + "\",\n" +
                "    \"type\": \"checkbox\",\n" +
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
                "        \"sqlType\",\n" +
                "        \"disabled\",\n" +
                "        \"checkboxtype\",\n" +
                "        \"datatype\",\n" +
                "        \"option\",\n" +
                "        \"url\",\n" +
                "        \"min\",\n" +
                "        \"max\",\n" +
                "        \"defaultValue\",\n" +
                "        \"defaultUrl\",\n" +
                "        \"hasChildren\"\n" +
                "    ],\n" +
                "    \"children\": [\n" +
                "\n" +
                "    ],\n" +
                "    \"linkbind\": \"\",\n" +
                "    \"checkboxtype\": \"fang\",\n" +
                "    \"disabled\": false,\n" +
                "    \"min\": 0,\n" +
                "    \"max\": 2,\n" +
                "    \"datatype\": \"option\",\n" +
                "    \"url\": \"\",\n" +
                "    \"dicData\":  " + jsonString + ",\n " +
                "    \"defaultValue\": \"\",\n" +
                "    \"defaultUrl\": \"\",\n" +
                "    \"sqlType\": \"array\",\n" +
                "    \"props\": {\n" +
                "        \"label\": \"\",\n" +
                "        \"value\": \"\"\n" +
                "    },\n" +
                "    \"rules\": [\n" +
                "        {\n" +
                "            \"required\": false,\n" +
                "            \"message\": \"请输入" + name + "\",\n" +
                "            \"trigger\": \"change\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"name\": \"" + DataFieldType.checkbox.getDesc() + "\",\n" +
                "}";
        return JSONObject.parseObject(str);
    }
}
