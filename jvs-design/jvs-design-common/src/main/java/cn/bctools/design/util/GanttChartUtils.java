package cn.bctools.design.util;

import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author jvs
 */
@UtilityClass
public class GanttChartUtils {

    static int day = 2;


    /**
     * @param pageResult 分页返回的数据
     * @param dateField  甘特图设置的那几个字段
     * @return 在分页结果中添加一个额外的字段返回给前端方便展示数据
     */
    public static Map<String, Object> setFiled(Page<Map<String, Object>> pageResult, List<String> dateField) {
        //这里对甘特图特殊字段进行数据处理，根据甘特图的字段进行找出这一批数据最大值和最小值
        List<DateTime> dateList = new ArrayList<>();
        pageResult.getRecords().forEach(e -> {
            for (String field : dateField) {
                if (e.containsKey(field)) {
                    if (ObjectNull.isNotNull(e.get(field))) {
                        String dva = e.get(field).toString();
                        if(!dva.trim().equals("") && dva.contains("-")) {
                            try {
                                dateList.add(DateUtil.parseDate(dva));
                            } catch (Exception ignored) {
                                e.put(field, "请配置单个日期");
                                e.remove(field + DynamicDataUtils.SUFFIX_ECHO);
                            }
                        } else {
                            e.put(field, dva);
                            e.remove(field + DynamicDataUtils.SUFFIX_ECHO);
                        }
                    } else {
                        e.put(field, "-");
                        e.put(field + DynamicDataUtils.SUFFIX_ECHO, "-");
                    }
                } else {
                    e.put(field, "-");
                    e.put(field + DynamicDataUtils.SUFFIX_ECHO, "-");
                }
            }
        });

        if (dateList.size() == 1) {
            //表示只有一个值
            dateList.add(dateList.get(0));
        } else if (dateList.isEmpty()) {
            dateList.add(DateTime.now());
            dateList.add(DateTime.now());
        }
        dateList.sort(Comparator.comparing(e -> e.toTimestamp().toInstant().getEpochSecond()));

        // 定义日期格式化对象
        List<Long> list = new ArrayList<>();
        startTime(dateList, list);
        endTime(dateList, list);
        Map<String, Object> body = BeanCopyUtil.beanToMap(pageResult);
        body.put("dateList", list);
        return body;
    }

    private static void endTime(List<DateTime> dateList, List<Long> list) {
        // 解析字符串为 LocalDate
        LocalDate date = dateList.get(dateList.size() - 1).toLocalDateTime().toLocalDate();
        // 添加 10 天
        LocalDate addedDate = date.plusDays(day);
        // 3. 将 LocalDateTime 转换为 Instant（需要指定时区）
        Instant instant = addedDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        long endTime = instant.toEpochMilli();
        list.add(endTime);
    }

    private static void startTime(List<DateTime> dateList, List<Long> list) {
        // 解析字符串为 LocalDate
        LocalDate date = dateList.get(0).toLocalDateTime().toLocalDate();
        // 减少 10 天
        LocalDate addedDate = date.minusDays(day);
        // 3. 将 LocalDateTime 转换为 Instant（需要指定时区）
        Instant instant = addedDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        // 4. 获取时间戳
        long endTime = instant.toEpochMilli();
        list.add(endTime);
    }

}
