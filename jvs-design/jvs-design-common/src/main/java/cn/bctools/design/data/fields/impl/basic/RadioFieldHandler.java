package cn.bctools.design.data.fields.impl.basic;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.form.item.RadioItemHtml;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;

/**
 * 表单字段: 单选框
 *
 * @Author: GuoZi
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@Component
@AllArgsConstructor
@DesignField(value = "单选", type = DataFieldType.radio)
public class RadioFieldHandler extends IMultipleTypeHandler implements IDataFieldHandler<RadioItemHtml>, ISelectorDataHandler {

    @Override
    public List<DataQueryType> getEnabledQueryTypes(RadioItemHtml html) {
        return super.getEnabledQueryTypes(html);
    }

    @Override
    public Object getEchoValue(RadioItemHtml fieldDto, Object data, boolean override, Map<String, Object> lineData, String... paths) {
        return echoValue(fieldDto, data, lineData);
    }

    @Override
    public String getConversionKey(RadioItemHtml dto, Object o, Map<String, Object> lineData, Map<String, Map<String, String>> cascaderFieldPathIdsMap, Map<String, List<Map<String, Object>>> generateCascaderList) {
        dto.setMultiple(false);
        return conversionKey(dto, o, lineData, cascaderFieldPathIdsMap, generateCascaderList);
    }

    @Override
    public Object getConversionKey(RadioItemHtml dto, Object o, Map<String, Object> oldData) {
        return conversionKey(dto, o, oldData);
    }

    @Override
    public Boolean whetherCoverValue() {
        return Boolean.FALSE;
    }

    @Override
    public void checkDataFieldType(RadioItemHtml radioItemHtml, Object o) throws Exception {
        if (!(o instanceof String)) {
            throw new RuntimeException("正确格式为字符串");
        }
    }

    @Override
    public Map<String, Object> generate(String name, String field, List<String> dicData) {
        if (ObjectNull.isNull(dicData)) {
            dicData = new ArrayList<>();
            dicData.add("未配置选择");
        }
        String jsonString = JSONObject.toJSONString(dicData.stream().map(e -> Dict.create().set("value", e).set("label", e)).collect(Collectors.toList()));

        String str = "{\n" +
                "    \"prop\": \"" + field + "\",\n" +
                "    \"type\": \"radio\",\n" +
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
                "        \"radiotype\",\n" +
                "        \"datatype\",\n" +
                "        \"option\",\n" +
                "        \"url\",\n" +
                "        \"defaultValue\",\n" +
                "        \"defaultUrl\",\n" +
                "        \"hasChildren\"\n" +
                "    ],\n" +
                "    \"hasChildren\": false,\n" +
                "    \"linkbind\": \"\",\n" +
                "    \"radiotype\": \"yuan\",\n" +
                "    \"disabled\": false,\n" +
                "    \"children\": [],\n" +
                "    \"text\": \"\",\n" +
                "    \"currVal\": \"\",\n" +
                "    \"defaultValue\": \"\",\n" +
                "    \"defaultUrl\": \"\",\n" +
                "    \"sqlType\": \"varchar\",\n" +
                "    \"parentKey\": \"\",\n" +
                "    \"datatype\": \"option\",\n" +
                "    \"url\": \"\",\n" +
                "    \"dicData\": " + jsonString + ",\n" +
                "    \"props\": {\n" +
                "        \"label\": \"\",\n" +
                "        \"value\": \"\"\n" +
                "    },\n" +
                "    \"rules\": [\n" +
                "        {\n" +
                "            \"required\": false,\n" +
                "            \"message\": \"请选择" + name + "\",\n" +
                "            \"trigger\": \"change\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"name\": \"" + DataFieldType.radio.getDesc() + "\"\n" +
                "}";


        return JSONObject.parseObject(str);
    }
}
