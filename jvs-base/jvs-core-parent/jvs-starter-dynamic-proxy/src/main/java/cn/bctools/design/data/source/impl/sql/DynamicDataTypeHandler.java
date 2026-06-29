package cn.bctools.design.data.source.impl.sql;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @Author: ZhuXiaoKang
 * @Description: 数据类型转换
 *
 * <p> 格式化数据
 */
public class DynamicDataTypeHandler {

    /**
     * 数据类型转换
     *
     * @param resultJSONData
     */
    public static void dataTypeHandler(JSONObject object) {
        for (Map.Entry<String, Object> entry : object.entrySet()) {
            if (entry.getValue() instanceof String) {
                stringHandler(entry);
                continue;
            }
            if (entry.getValue() instanceof LocalDateTime) {
                dateHandler(entry);
            }
        }
    }

    /**
     * String
     *
     * <p> 存储的JSON数据，通过mybatis查询后，默认返回是字符串。需要将其转为JSON对象
     *
     * @param entry
     */
    private static void stringHandler(Map.Entry<String, Object> entry) {
        // json字符串转对象
        if (JSONUtil.isTypeJSONObject(entry.getValue().toString())) {
            entry.setValue(JSON.parseObject(entry.getValue().toString()));
        }
        // json字符串转集合
        if (JSONUtil.isTypeJSONArray(entry.getValue().toString())) {
            entry.setValue(JSON.parseArray(entry.getValue().toString()));
        }
    }

    /**
     * LocalDateTime
     *
     * <p> 格式化时间
     *
     * @param entry
     */
    private static void dateHandler(Map.Entry<String, Object> entry) {
        entry.setValue(LocalDateTimeUtil.format((LocalDateTime) entry.getValue(), DatePattern.NORM_DATETIME_PATTERN));
    }
}
