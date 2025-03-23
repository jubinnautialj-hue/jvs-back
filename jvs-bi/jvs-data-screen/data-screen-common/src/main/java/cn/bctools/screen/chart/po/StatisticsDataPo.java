package cn.bctools.screen.chart.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ：
 * [description]：统计
 * @modified By：
 * @version: 1.0.0$
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatisticsDataPo {
    /**
     * 总条数
     */
    private long number;
    /**
     * 实际执行的条数
     */
    private long filterNumber;
    /**
     * 执行时间 毫秒
     */
    private long time;
}
