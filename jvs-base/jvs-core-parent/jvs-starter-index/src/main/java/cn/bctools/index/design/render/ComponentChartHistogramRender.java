package cn.bctools.index.design.render;

import cn.bctools.index.dto.OptionsBase;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class ComponentChartHistogramRender extends OptionsBase {

    /**
     * The Series name.
     */
    String seriesName;

    private List<ComponentChartLineRender.SeriesItem> series;

    @JsonProperty("xAxis")
    private XAxis xAxis;

    /**
     * The type Series item.
     */
    @Data
    @Accessors(chain = true)
    public static class SeriesItem {
        private String name;
        private List<?> data;
    }

    /**
     * The type X axis.
     */
    @Data
    @Accessors(chain = true)
    public static class XAxis {
        private List<?> data;
    }
}
