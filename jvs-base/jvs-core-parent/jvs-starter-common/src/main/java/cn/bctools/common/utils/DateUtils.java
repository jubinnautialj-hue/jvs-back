package cn.bctools.common.utils;

import cn.hutool.core.date.TemporalAccessorUtil;
import com.alibaba.fastjson2.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;

/**
 * 时间转换器
 * @author xh
 */
public class DateUtils {

    static final Function<LocalDateTime, String> TRANSFORMATION = temporal -> {
        long between = ChronoUnit.DAYS.between(temporal.toLocalDate(), LocalDate.now());
        if (between == 0) {
            between = ChronoUnit.HOURS.between(temporal, LocalDateTime.now());
            if (between > 0) {
                return between + "小时前";
            } else {
                between = ChronoUnit.MINUTES.between(temporal, LocalDateTime.now());
                if (between > 0) {
                    return between + "分钟前";
                } else {
                    between = ChronoUnit.MILLIS.between(temporal, LocalDateTime.now()) / 1000;
                    return between + "秒前";
                }
            }
        }
        if (between == 1) {
            return "一天前";
        } else {
            int i = 20;
            if (between < i) {
                return TemporalAccessorUtil.format(temporal, "MM/dd HH:mm:ss");
            } else {
                int i1 = 90;
                if (between < i1) {
                    return TemporalAccessorUtil.format(temporal, "MM月dd日");
                } else {
                    return TemporalAccessorUtil.format(temporal, "yyyy/MM/dd");
                }
            }
        }
    };

    public static <T extends Object> Object transformation(T e, String name) {
        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(e));
        LocalDateTime time = jsonObject.getObject(name, LocalDateTime.class);
        jsonObject.put(name, TRANSFORMATION.apply(time));
        return jsonObject;
    }

}
