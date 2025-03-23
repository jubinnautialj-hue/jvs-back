package cn.bctools.index.design.render;

import cn.bctools.index.dto.OptionsBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * 饼状图渲染数据
 *
 * @author jvs
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ComponentChartPieRender extends OptionsBase {

    /**
     * The Series name.
     */
    String seriesName;
    /**
     * 图表类型
     */
    String roseType;

    /**
     * 大小 ["10%","100%"]
     */
    List<String> radius;

    /**
     * 渲染数据
     */
    List<PieData> data;

    /**
     * The type Pie data.
     */
    @Data
    @Accessors(chain = true)
    public static class PieData {
        /**
         * 显示名
         */
        String name;
        /**
         * 数据值
         */
        Number value;
    }

}
