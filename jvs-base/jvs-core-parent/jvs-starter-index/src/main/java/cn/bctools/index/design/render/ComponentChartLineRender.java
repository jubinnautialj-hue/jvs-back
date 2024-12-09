package cn.bctools.index.design.render;

import cn.bctools.index.dto.OptionsBase;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 饼状图渲染数据
 *
 * @author jvs
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ComponentChartLineRender extends OptionsBase {

    /**
     *
     */
    String seriesName;

    private List<SeriesItem> series;

    @JsonProperty("xAxis")
    private XAxis xAxis;

    @Data
    @Accessors(chain = true)
    public static class SeriesItem {
        private String name;
        private List<?> data;
    }

    @Data
    @Accessors(chain = true)
    public static class XAxis {
        private List<?> data;
    }
}
