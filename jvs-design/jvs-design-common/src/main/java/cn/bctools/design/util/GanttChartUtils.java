package cn.bctools.design.util;

import cn.bctools.common.utils.BeanCopyUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author jvs
 */
public class GanttChartUtils {

    static int day = 10;


    /**
     * @param pageResult 分页返回的数据
     * @param dateField  甘特图设置的那几个字段
     * @return 在分页结果中添加一个额外的字段返回给前端方便展示数据
     */
    public static Map<String, Object> setFiled(Page<Map<String, Object>> pageResult, List<String> dateField) {
        //这里对甘特图特殊字段进行数据处理，根据甘特图的字段进行找出这一批数据最大值和最小值
        List<Object> dateList = new ArrayList<>();
        pageResult.getRecords().forEach(e -> {
            for (String field : dateField) {
                if (e.containsKey(field)) {
                    dateList.add(e.get(field));
                }
            }
        });
        if (dateList.size() == 1) {
            //表示只有一个值
            dateList.add(dateList.get(0));
        } else if (dateList.size() > 2) {
            dateList.add(1, dateList.get(dateList.size() - 1));
            for (int i = 2; i < dateList.size(); i++) {
                dateList.remove(i);
            }
        } else {
            dateList.add(DateUtil.today());
            dateList.add(DateUtil.today());
        }
        // 定义日期格式化对象
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        {
            // 解析字符串为 LocalDate
            LocalDate date = LocalDate.parse(dateList.get(0).toString(), formatter);
            // 减少 10 天
            LocalDate addedDate = date.minusDays(day);
            // 3. 将 LocalDateTime 转换为 Instant（需要指定时区）
            Instant instant = addedDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
            // 4. 获取时间戳
            long endTime = instant.toEpochMilli();
            dateList.set(0, endTime);
        }
        {
            // 解析字符串为 LocalDate
            LocalDate date = LocalDate.parse(dateList.get(1).toString(), formatter);

            // 添加 10 天
            LocalDate addedDate = date.plusDays(day);
            // 3. 将 LocalDateTime 转换为 Instant（需要指定时区）
            Instant instant = addedDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
            long endTime = instant.toEpochMilli();
            dateList.set(1, endTime);
        }

        Map<String, Object> body = BeanCopyUtil.beanToMap(pageResult);
        body.put("dateList", dateList);
        return body;
    }

}
