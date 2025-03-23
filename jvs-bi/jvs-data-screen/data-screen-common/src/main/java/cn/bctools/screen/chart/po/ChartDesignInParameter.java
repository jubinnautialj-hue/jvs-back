package cn.bctools.screen.chart.po;


import cn.bctools.screen.chart.bo.FieldsData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 公共的设计入参
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ChartDesignInParameter {
    /**
     * 维度
     */
    private List<FieldsData> xAxis;
    /**
     * 指标
     */
    private List<FieldsData> yAxis;

    /**
     * 颜色
     */
    private List<FieldsData> colour;
    /**
     * 尺寸
     */
    private List<FieldsData> dimension;

    /**
     * 对比分类 目前没有使用  可以预留防止 后续 使用
     */
    private List<FieldsData> classifyField;
    /**
     * 表格页码
     */
    private Long current;
    /**
     * 表格每页条数
     */
    private Long size;

}
