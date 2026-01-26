package cn.bctools.design.data.fields.impl.basic;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.fields.DataFieldHandler;
import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.form.html.DatePickerHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.enums.DataQueryType;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.bctools.design.data.fields.impl.basic.DatePickerFieldHandler.DateType.*;
import static cn.hutool.core.date.DatePattern.NORM_DATE_PATTERN;

/**
 * 表单字段: 日期
 * <p>
 * 字段数据类型:
 * 1. 单个日期
 * 2. 多个范围
 * 3. 日期范围
 *
 * @Author: GuoZi
 */
@Slf4j
@Component
@DesignField(value = "日期", type = DataFieldType.datePicker)
public class DatePickerFieldHandler implements IDataFieldHandler<DatePickerHtml> {

    /**
     * The enum Date type.
     */
    public enum DateType {

        /**
         * Date date type.
         */
        date("年月日-单个"),
        /**
         * Week date type.
         */
        week("年周-单个"),
        /**
         * Month date type.
         */
        month("年月-单个"),
        /**
         * Year date type.
         */
        year("年-单个"),
        /**
         * Dates date type.
         */
        dates("多日期"),
        /**
         * Datetime date type.
         */
        datetime("年月日时分秒-单个"),
        /**
         * Datetimerange date type.
         */
        datetimerange("年月日时分秒-范围"),
        /**
         * Daterange date type.
         */
        daterange("年月日-范围"),
        /**
         * Monthrange date type.
         */
        monthrange("年月-范围"),
        ;

        /**
         * The Msg.
         */
        String msg;

        DateType(String msg) {
            this.msg = msg;
        }
    }


    /**
     * The constant TYPE_SUFFIX_RANGE.
     */
    public static final String TYPE_SUFFIX_RANGE = "range";

    @Override
    public Object getEchoValue(DatePickerHtml fieldDto, Object data, boolean override, Map<String, Object> lineData, String... paths) {
        if (data instanceof List) {
            List<String> list = (List<String>) data;
            DateType dateType = fieldDto.getDatetype();
            if (dates.equals(dateType)) {
                // 多日期
                return String.join(DataFieldHandler.SEPARATOR_MULTI, list);
            }
            // 日期范围
            return String.join(DataFieldHandler.LINK_MULTI, list);
        }
        return data;
    }

    @Override
    public List<DataQueryType> getEnabledQueryTypes(DatePickerHtml fieldJson) {
        DateType dateType = fieldJson.getDatetype();
        if (ObjectNull.isNull(dateType)) {
            dateType = datetime;
        }
        List<DataQueryType> types = new ArrayList<>();
        types.add(DataQueryType.isNull);
        types.add(DataQueryType.isNotNull);
        types.add(DataQueryType.between);
        if (ObjectNull.isNull(dateType)) {
            return types;
        }
        if (dateType.equals(dates)) {
            // 多日期
            types.add(DataQueryType.in);
            types.add(DataQueryType.allin);
        } else if (dateType.name().endsWith(TYPE_SUFFIX_RANGE)) {
            // 日期范围
            types.add(DataQueryType.between);
        } else {
            // 单个日期
            types.add(DataQueryType.eq);
            types.add(DataQueryType.gt);
            types.add(DataQueryType.ge);
            types.add(DataQueryType.lt);
            types.add(DataQueryType.le);
        }
        return types;
    }

    @Override
    public Object getConversionKey(DatePickerHtml dto, Object o, Map<String, Object> lineData) {
        DateType dateType = dto.getDatetype();
        List<DataQueryType> types = new ArrayList<>();
        if (ObjectNull.isNull(dateType)) {
            return types;
        }
        switch (dateType) {
            case date:
                return DateUtil.parseDate(o.toString()).toDateStr();
            case month:
                DatePattern.createFormatter("yyyy-MM").parse(o.toString()).toString();
                return o.toString();
            case datetime:
                if (o instanceof LocalDateTime) {
                    return o;
                } else {
                    return DateUtil.parseDateTime(o.toString()).toLocalDateTime();
                }
            case daterange:
            case monthrange:
            case datetimerange:
                return Arrays.stream(o.toString().split("~")).collect(Collectors.toList());
            default:

        }
        return o;
    }

    @Override
    public Object checkDataFieldType(DatePickerHtml dto, Object o) {
        DateType dateType = dto.getDatetype();
        switch (dateType) {
            case date:
                try {
                    DateTime dateTime = DateUtil.parseDate(o.toString());
                    dateTime.toLocalDateTime();
                    return o.toString();
                } catch (Exception e) {
                    throw new RuntimeException("正确格式为" + NORM_DATE_PATTERN);
                }
            case week:
                //不校验
                break;
            case month:
                try {
                    DatePattern.createFormatter("yyyy-MM").parse(o.toString());
                    return o.toString();
                } catch (Exception e) {
                    throw new RuntimeException("正确格式为yyyy-MM");
                }
            case year:
                try {
                    DatePattern.createFormatter("yyyy").parse(o.toString());
                    return o.toString();
                } catch (Exception e) {
                    throw new RuntimeException("正确格式为yyyy");

                }
            case dates:
                //不校验
                break;
            case datetime:
                try {
                    DateTimeFormatter formatter = DatePattern.createFormatter("yyyy-MM-dd HH:mm:ss");
                    return LocalDateTime.from(formatter.parse(o.toString()));
                } catch (Exception e) {
                    throw new RuntimeException("正确格式为yyyy-MM-dd HH:mm:ss");
                }
            case datetimerange:
                DateTimeFormatter formatter = DatePattern.createFormatter("yyyy-MM-dd HH:mm:ss");
                if (o instanceof String) {
                    if (((String) o).contains("~")) {
                        try {
                            List<LocalDateTime> collect = Arrays.stream(((String) o).split("~")).map(e -> LocalDateTime.from(formatter.parse(e))).collect(Collectors.toList());
                            o = collect;
                        } catch (Exception e) {
                            throw new RuntimeException("正确格式为yyyy-MM-dd HH:mm:ss");
                        }
                    }
                } else if (o instanceof List) {
                    try {
                        DatePattern.createFormatter("yyyy-MM-dd HH:mm:ss").parse(((List) o).get(0).toString());
                        DatePattern.createFormatter("yyyy-MM-dd HH:mm:ss").parse(((List) o).get(1).toString());
                    } catch (Exception e) {
                        throw new RuntimeException("正确格式为yyyy-MM-dd HH:mm:ss");
                    }
                }
                break;
            case daterange:
                DateTimeFormatter dateTimeFormatter = DatePattern.createFormatter("yyyy-MM-dd");
                if (o instanceof String) {
                    if (((String) o).contains("~")) {
                        try {
                            List<LocalDate> collect = Arrays.stream(((String) o).split("~")).map(e -> LocalDate.from(dateTimeFormatter.parse(e))).collect(Collectors.toList());
                            o = collect;
                        } catch (Exception e) {
                            throw new RuntimeException("正确格式为yyyy-MM-dd");
                        }
                    }
                } else if (o instanceof List) {
                    try {
                        DatePattern.createFormatter("yyyy-MM-dd").parse(((List) o).get(0).toString());
                        DatePattern.createFormatter("yyyy-MM-dd").parse(((List) o).get(1).toString());
                    } catch (Exception e) {
                        throw new RuntimeException("正确格式为yyyy-MM-dd");
                    }
                }
                break;
            case monthrange:
                DateTimeFormatter monthrangeFormatter = DatePattern.createFormatter("yyyy-MM");
                if (o instanceof String) {
                    if (((String) o).contains("~")) {
                        try {
                            Arrays.stream(((String) o).split("~")).map(e -> LocalDateTime.from(monthrangeFormatter.parse(e))).collect(Collectors.toList());
                            o = Arrays.stream(((String) o).split("~")).collect(Collectors.toList());
                        } catch (Exception e) {
                            throw new RuntimeException("正确格式为yyyy-MM");
                        }
                    }
                } else if (o instanceof List) {
                    try {
                        DatePattern.createFormatter("yyyy-MM").parse(((List) o).get(0).toString());
                        DatePattern.createFormatter("yyyy-MM").parse(((List) o).get(1).toString());
                    } catch (Exception e) {
                        throw new RuntimeException("正确格式为yyyy-MM");
                    }
                }
            default:

        }

        return o;
    }

    @Override
    public void checkFieldTypeAttributeChanged(DatePickerHtml html, DatePickerHtml dbHtml) throws Exception {
        if (!html.getDatetype().equals(dbHtml.getDatetype())) {
            throw new BusinessException("(" + dbHtml.getDatetype().msg + ")变为(" + html.getDatetype().msg + ")");
        }
    }

    @Override
    public Map<String, Object> generate(String name, String field, List<String> dicData) {
        String str = "{\n" +
                     "    \"prop\": \"" + field + "\",\n" +
                     "    \"type\": \"datePicker\",\n" +
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
                     "        \"startLimit\",\n" +
                     "        \"prefixicon\",\n" +
                     "        \"datetype\",\n" +
                     "        \"startplaceholder\",\n" +
                     "        \"endplaceholder\",\n" +
                     "        \"rangeseparator\",\n" +
                     "        \"endLimit\",\n" +
                     "        \"sqlType\",\n" +
                     "        \"defaultDate\"\n" +
                     "    ],\n" +
                     "    \"placeholder\": \"请输入" + name + "\",\n" +
                     "    \"clearable\": false,\n" +
                     "    \"disabled\": false,\n" +
                     "    \"prefixicon\": \"el-icon-time\",\n" +
                     "    \"datetype\": \"date\",\n" +
                     "    \"startplaceholder\": \"开始时间\",\n" +
                     "    \"endplaceholder\": \"结束时间\",\n" +
                     "    \"rangeseparator\": \"至\",\n" +
                     "    \"startLimit\": \"\",\n" +
                     "    \"endLimit\": \"\",\n" +
                     "    \"defaultValue\": \"\",\n" +
                     "    \"sqlType\": \"date\",\n" +
                     "    \"linkbind\": \"\",\n" +
                     "    \"rules\": [\n" +
                     "        {\n" +
                     "            \"required\": false,\n" +
                     "            \"message\": \"请输入" + name + "\",\n" +
                     "            \"trigger\": \"change\"\n" +
                     "        }\n" +
                     "    ],\n" +
                     "    \"name\": \"" + DataFieldType.datePicker.getDesc() + "\"\n" +
                     "}";
        return JSONObject.parseObject(str);
    }
}
