package cn.bctools.design.data.fields.impl.basic;

import cn.bctools.design.data.fields.DataFieldHandler;
import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.form.html.DatePickerHtml;
import cn.bctools.design.data.fields.dto.form.html.TimePickerHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.enums.DataQueryType;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.bctools.design.data.fields.impl.basic.DatePickerFieldHandler.DateType.dates;
import static cn.hutool.core.date.DatePattern.NORM_TIME_FORMATTER;
import static cn.hutool.core.date.DatePattern.NORM_TIME_PATTERN;

/**
 * 表单字段: 任意时间
 *
 * @Author: GuoZi
 */
@Slf4j
@Component
@DesignField(value = "任意时间", type = DataFieldType.timePicker)
public class TimePickerFieldHandler implements IDataFieldHandler<TimePickerHtml> {

    @Override
    public List<DataQueryType> getEnabledQueryTypes(TimePickerHtml fieldJson) {
        List<DataQueryType> types = new ArrayList<>();
        Boolean isRange = fieldJson.getIsrange();
        if (Boolean.TRUE.equals(isRange)) {
            types.add(DataQueryType.between);
        } else {
            types.add(DataQueryType.eq);
            types.add(DataQueryType.gt);
            types.add(DataQueryType.ge);
            types.add(DataQueryType.lt);
            types.add(DataQueryType.le);
        }
        return types;
    }

    @Override
    public Object getEchoValue(TimePickerHtml fieldDto, Object data, boolean override, Map<String, Object> lineData, String... paths) {
        if (data instanceof List) {
            if (fieldDto.getIsrange()) {
                // 多日期
                return ((List<?>) data).stream().map(String::valueOf).collect(Collectors.joining("~"));
            } else {
                return data.toString();
            }
        }
        return data;
    }

    @Override
    public void checkDataFieldType(TimePickerHtml timePickerHtml, Object o) throws Exception {
        try {
            if (timePickerHtml.getIsrange()) {
                if (!(o instanceof List)) {
                    NORM_TIME_FORMATTER.parse(((List) o).get(0).toString());
                    NORM_TIME_FORMATTER.parse(((List) o).get(1).toString());
                }
            } else {
                NORM_TIME_FORMATTER.parse(o.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException("正确格式为" + NORM_TIME_PATTERN);
        }
    }

    @Override
    public Map<String, Object> generate(String name, String field, List<String> dicData) {
        String str = "{\n" +
                "    \"prop\": \"" + field + "\",\n" +
                "    \"type\": \"timePicker\",\n" +
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
                "        \"clearable\",\n" +
                "        \"pickeroptions\",\n" +
                "        \"prefixicon\",\n" +
                "        \"isrange\",\n" +
                "        \"startplaceholder\",\n" +
                "        \"endplaceholder\",\n" +
                "        \"rangeseparator\",\n" +
                "        \"sqlType\"\n" +
                "    ],\n" +
                "    \"linkbind\": \"\",\n" +
                "    \"disabled\": false,\n" +
                "    \"placeholder\": \"请输入任意时间\",\n" +
                "    \"clearable\": false,\n" +
                "    \"prefixicon\": \"el-icon-time\",\n" +
                "    \"isrange\": false,\n" +
                "    \"startplaceholder\": \"开始时间\",\n" +
                "    \"endplaceholder\": \"结束时间\",\n" +
                "    \"rangeseparator\": \"至\",\n" +
                "    \"sqlType\": \"date\",\n" +
                "    \"rules\": [\n" +
                "        {\n" +
                "            \"required\": false,\n" +
                "            \"message\": \"请输入" + name + "\",\n" +
                "            \"trigger\": \"change\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"name\": \"" + DataFieldType.timePicker.getDesc() + "\",\n" +
                "    \"defaultOrigin\": \"\"\n" +
                "}";
        return JSONObject.parseObject(str);
    }
}
