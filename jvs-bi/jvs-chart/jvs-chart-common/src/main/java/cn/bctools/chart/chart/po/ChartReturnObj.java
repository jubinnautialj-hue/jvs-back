package cn.bctools.chart.chart.po;

import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 返回值
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ChartReturnObj<T> {
    /**
     * 维度
     */
    List<Object> xAxisData = new ArrayList<>();
    /**
     * 执行日志
     */
    StatisticsDataPo statisticsDataPo;
    /**
     * 错误日志
     */
    String error;
    /**
     * 表格title
     */
    List<TableHeader> header;
    /**
     * 表格返回值
     */
    TableData data;
    /**
     * 文件名称 路径 用于数据下载使用
     */
    String filePath;

    /**
     * 指标
     */
    List<Object> yAxisData = new ArrayList<>();
    /**
     * 具体的值
     */
    List<T> series = new ArrayList<>();
    /**
     * 卡片返回值
     */
    Object cardContent;
    /**
     * 雷达图
     * */
    List<JSONObject> indicatorData;

    /**
     * 表格的数据
     */
    List<JSONObject> tableValue = new ArrayList<>();

    @Data
    public static class TableHeader {
        /**
         * 显示名称
         */
        private String label;
        /**
         * 字段key
         */
        private String prop;
    }

    @Data
    public static class TableData {
        /**
         * 页码
         */
        private Long current;
        /**
         * 每页条数
         */
        private Long size;
        /**
         * 具体数据
         */
        private List<Map<String, Object>> records;
        /**
         * 总条数
         */
        private Long total;

    }

}
