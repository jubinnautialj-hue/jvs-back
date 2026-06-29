package cn.bctools.im.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author ZhuXiaoKang
 * @Description 日期工具列
 */
public class DateUtil {

    private DateUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * LocalDateTime转时间戳(毫秒)
     *
     * @param localDateTime 时间
     * @return 时间戳
     */
    public static Long localDateTimeToMillisecond(LocalDateTime localDateTime) {
        return localDateTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
    }

    /**
     * 时间戳(毫秒)转LocalDateTime
     *
     * @param milliseconds 时间戳
     * @return LocalDateTime
     */
    public static LocalDateTime millisecondToLocalDateTime(Long milliseconds) {
        return Instant.ofEpochMilli(milliseconds).atZone(ZoneOffset.ofHours(8)).toLocalDateTime();
    }
}
